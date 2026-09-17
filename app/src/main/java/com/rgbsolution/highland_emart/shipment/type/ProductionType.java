package com.rgbsolution.highland_emart.shipment.type;

import android.content.Context;
import android.widget.Toast;
import android.util.Log;
import android.app.AlertDialog;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.R;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * 생산 계근(이노이천) — searchType "1" (개발67)
 *
 * <h3>이 타입의 판정 (개발67 §1.3)</h3>
 * <ul>
 *   <li>바코드 스캔 : 원본 {@code setBarcodeMsgProduction}(개발60이 분리한 생산 전용 본문)</li>
 *   <li>{@code ITEM_TYPE} : <b>S · J 만</b> — W/HW · B 블록은 개발60이 이미 제거했다(생산 VIEW 미출력)</li>
 *   <li>킬코이 · 센터명 판정 : <b>없음</b> — 개발60이 제거했다('하이랜드푸드' 고정)</li>
 *   <li>중복검사 우회 : X</li>
 *   <li>계근 INSERT : 일반 (insertqueryGoodsWet)</li>
 *   <li>계근중량 반올림 : 소수점 3자리 (else 경로)</li>
 *   <li>계근 시 라벨 : <b>없음</b> — 원본 라벨 분기에 생산(1) 케이스가 없다</li>
 *   <li>재출력 라벨 : else 경로(이마트 라벨)</li>
 *   <li>전송 : <b>일괄</b> / insert_goods_wet_production.jsp</li>
 *   <li>수기 입력 : 중량 절사 없음({@code Double.toString}), 소비기한 창은 CENTERNAME 조건일 때만</li>
 * </ul>
 *
 * <p><b>생산라벨(7)과 같은 본문이 아니다.</b> 7은 개발60의 정리를 거치지 않은 공용
 * {@code setBarcodeMsg} 원본을 쓰므로 W/HW · B 블록과 킬코이 판정이 살아 있다(개발67 §4.5).</p>
 *
 * <p>Step 1은 골격만 만든다. 각 메서드는 해당 Step에서 원본을 그대로 옮겨 채운다.
 * <b>Step 8(컷오버) 전까지 이 클래스는 생성되지 않는다</b> — {@code onCreate} 가 생산을 제외하기 때문이다.</p>
 */
public class ProductionType implements ShipmentType {

    private static final String TAG = "ProductionType";

    private final BixolonShipmentActivity a;

    public ProductionType(BixolonShipmentActivity activity) {
        this.a = activity;
    }

    /**
     * 바코드 스캔 처리 — 원본 BixolonShipmentActivity.setBarcodeMsgProduction 본문 이관 (개발67 Step 2)
     *
     * <p><b>접은 조건이 없다.</b> 개발60이 이 메서드를 만들 때 이미 생산이 타지 않는 분기
     * (비정량 중복검사 우회 · 킬코이/센터명 소비기한 검증 · ITEM_TYPE W/HW · B · 이마트 LB 자릿수)를
     * 전부 걷어냈다. 그래서 기계적으로 옮기기만 한다.</p>
     *
     * <p>치환 3종 — {@code ProgressDlgShipSelect} 생성 2곳은 Activity 래퍼({@code startShipSelect})로,
     * 재귀 호출은 자기 자신으로, Activity 필드·메서드는 {@code a.} 접두어로 바꿨다.
     * 상품 매칭 호출({@code a.find_PackerProduct} 계열)은 <b>Step 5</b> 에서 자기 타입 메서드로 바꾼다.</p>
     */
    @Override
    public void onBarcodeScanned(final String msg) {
        try {
            if (a.dialog_flag)
                return;

            // 중복 호출 방지: 동일 바코드가 1초 이내 재처리되면 무시 (다른 바코드는 통과)
            long now = System.currentTimeMillis();

            if (msg != null && msg.equals(a.lastProcessedBarcode)
                    && (now - a.lastBarcodeProcessedTime) < ShipmentConst.BARCODE_PROCESS_DEBOUNCE_MS) {
                Log.d(TAG, "setBarcodeMsgProduction 중복 호출 무시 (디바운싱)");
                return;
            }

            a.lastProcessedBarcode = msg;
            a.lastBarcodeProcessedTime = now;

            Log.e(TAG, "========================setBarcodeMsgProduction 시작======================");

            a.edit_barcode.setText(msg);
            if (a.scan_flag) { // 패커상품 스캔
                Log.e(TAG, "========================상품스캔======================" + a.work_flag);
                try {
                    String find_ppcodetemp = "";

                    if (a.work_flag == 1) {
                        Log.e(TAG, "========================상품바코드스캔1======================");
                        find_ppcodetemp = a.find_PackerProduct(msg);
                        Log.e(TAG, "========================상품바코드스캔1 ppcode ======================" + find_ppcodetemp);
                    }else {
                        Log.e(TAG, "========================상품코드스캔2======================");
                        find_ppcodetemp = a.find_PackerProductBarcodeGoods(msg);
                        Log.e(TAG, "========================상품코드스캔2 ppcode ======================" + find_ppcodetemp);
                    }
                    Log.e(TAG, "========================바코드 정보가져옴======================");
                    final String find_ppcode = find_ppcodetemp;

                    if (find_ppcode.equals("null")) {
                        Toast.makeText(a.getApplicationContext(), "패커상품이 존재하지않거나,\n바코드가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        a.vibrator.vibrate(1000);
                        a.work_item_fullbarcode = "";
                        a.work_item_barcodegoods = "";
                    } else {
                        if (a.work_ppcode.equals("")) {
                            // 최초 스캔일 경우
                            Log.e(TAG, "========================최초 스캔11======================");
                            Log.e(TAG, "========================find_ppcode test!!======================"+find_ppcode);
                            a.work_ppcode = find_ppcode;
                            a.work_item_fullbarcode = msg;
                            a.startShipSelect(a.sp_center_name.getSelectedItem().toString(), find_ppcode, a.scan_flag);
                        } else if (!a.work_ppcode.equals("") && a.work_ppcode.equals(find_ppcode)) {         // 작업 중이고, 같은 상품을 스캔했을 경우
                            a.work_item_fullbarcode = msg;
                            Log.e("바코드", "" + a.work_item_fullbarcode);
                            boolean dup = DBHandler.duplicatequeryGoodsWet_check(a.getApplicationContext(), a.work_item_fullbarcode);

                            if (dup) {
                                Log.e(TAG, "=====================오류지점1=========================");
                                Toast.makeText(a.getApplicationContext(), "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.", Toast.LENGTH_SHORT).show();
                                a.vibrator.vibrate(1000);
                                a.work_item_fullbarcode = "";
                                a.work_item_barcodegoods = "";
                                return;
                            } else{
                                Log.e(TAG, "=====================상품스캔일반=========================");
                                a.set_scanFlag(false);        // BL스캔 시작
                                a.work_ppcode = find_ppcode;
                                a.work_item_fullbarcode = msg;

                                if ((a.centerTotalCount > 0) && (a.centerTotalCount == a.centerWorkCount)) {       // 총 계근 완료
                                    a.show_wetFinishDialog();
                                }

                                a.lastBarcodeProcessedTime = 0;   // 의도된 재귀 호출은 디바운스 우회
                                this.onBarcodeScanned(msg);       // 원본의 재귀 호출 — 자기 자신을 부른다
                            }
                        } else if (!a.work_ppcode.equals(find_ppcode)) {                                   // 작업 중이고, 다른 상품을 스캔했을 경우
                            Log.e(TAG, "=====================작업중다른상품스캔=========================");
                            Log.i(TAG, "작업 중 다른 상품 스캔 !");
                            a.vibrator.vibrate(500);
                            a.dialog_flag = true;

                            new AlertDialog.Builder(a, R.style.AppCompatDialogStyle)
                                    .setIcon(R.drawable.highland)
                                    .setTitle(R.string.shipment_wet_other)
                                    .setMessage(R.string.shipment_wet_other_msg)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.shipment_wet_yes, (dialog, which) -> {
                                        a.dialog_flag = false;
                                        a.work_ppcode = find_ppcode;
                                        a.work_item_fullbarcode = msg;
                                        a.startShipSelect(a.sp_center_name.getSelectedItem().toString(), find_ppcode, a.scan_flag);
                                    })
                                    .setNegativeButton(R.string.shipment_wet_no, (dialog, which) -> {
                                        a.dialog_flag = false;
                                    })
                                    .show();
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "setBarcodeMsgProduction's 패커상품 스캔 Exception -> " + ex.getMessage().toString());
                }
            } else {//BL스캔
                Log.e(TAG, "========================BL스캔======================" + a.sp_bl_no.getItemAtPosition(a.sp_bl_no.getSelectedItemPosition()).toString());

                a.work_item_fullbarcode = msg;

                try {
                    if (true) {
                        String temp_bl_no = a.sp_bl_no.getItemAtPosition(a.sp_bl_no.getSelectedItemPosition()).toString();
                        for (int i = 0; i < a.arSM.size(); i++) {
                            if (temp_bl_no.equals(a.arSM.get(i).getBL_NO()) && !a.arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(a.arSM.get(i).getPACKING_QTY()))) {
                                // BL번호 같은 상품 검색 완료
                                a.current_work_position = i;

                                Log.e(TAG, "========================current_work_position======================" + i);

                                a.work_bl_no = temp_bl_no;

                                Log.e(TAG, "========================work_bl_no======================" + a.work_bl_no);

                                break;
                            } else {
                                a.work_bl_no = "";
                                a.current_work_position = -1;
                            }
                        }

                        a.expiryDayTrans = ""; //일단 스캔할 때 마다 초기화

                        Log.e(TAG, "========================TEST TEST======================" + a.arSM.get(a.current_work_position).getCENTERNAME()); //센터 선택하고 스캔할떄 여기 탐

                        if (a.current_work_position == -1) {
                            Toast.makeText(a.getApplicationContext(), "해당하는 BL상품이 없습니다.\nBL번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                            a.vibrator.vibrate(300);
                            return;
                        } else {
                            a.sp_point_name.setSelection(a.current_work_position);
                        }

                        a.sList.setSelection(a.current_work_position);      // 현재 계근지점으로 위치 변경

                        if (a.arSM.get(a.current_work_position).getGI_REQ_PKG().equals(String.valueOf(a.arSM.get(a.current_work_position).getPACKING_QTY()))) {
                            if ((a.centerTotalCount > 0) && (a.centerTotalCount == a.centerWorkCount)) {       // 총 계근 완료
                                a.show_wetFinishDialog();
                            }
                            return;
                        }

                        Log.e(TAG, "=====================work_item_fullbarcode=========================" + a.work_item_fullbarcode);
                        Log.e(TAG, "=====================arSM.get(current_work_position).getGI_D_ID()=========================" + a.arSM.get(a.current_work_position).getGI_D_ID());
                        Log.e(TAG, "=====================arSM.get(current_work_position).getPACKER_PRODUCT_CODE()=========================" + a.arSM.get(a.current_work_position).getPACKER_PRODUCT_CODE());

                        boolean dup = DBHandler.duplicatequeryGoodsWet(a.getApplicationContext(), a.work_item_fullbarcode,
                                a.arSM.get(a.current_work_position).getGI_D_ID(), a.arSM.get(a.current_work_position).getPACKER_PRODUCT_CODE(), a.arSM.get(a.current_work_position).getGI_L_ID());

                        // 개발60 Step3 : 비정량(4,5) 중복검사 우회 분기 삭제 (생산 미해당)

                        Log.e(TAG, "=====================체크1=========================" + a.arSM.get(a.current_work_position).getPACKER_CODE());
                        Log.e(TAG, "=====================체크2=========================" + a.arSM.get(a.current_work_position).getSTORE_CODE());
                        Log.e(TAG, "=====================체크3=========================" + a.work_item_bi_info.getSHELF_LIFE());

                        if (dup) {
                            Log.e(TAG, "=====================오류지점2=========================");
                            Toast.makeText(a.getApplicationContext(), "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.", Toast.LENGTH_SHORT).show();
                            a.vibrator.vibrate(1000);
                            a.work_item_fullbarcode = "";
                            a.work_item_barcodegoods = "";
                            return;
                        }

                        Goodswets_Info gi = new Goodswets_Info();
                        Log.i(TAG, "## 패커상품코드 & BL번호 확인 완료. 계근 시작 ##");
                        Log.i(TAG, "현재 계근할 FULL 바코드                     : " + a.work_item_fullbarcode);
                        Log.i(TAG, "현재 계근할 바코드의 BarcodeGodos            : " + a.work_item_barcodegoods);
                        /*
                         * 계근 필드값
                         */
                        String item_weight = "";            // 상품 최초 중량 절사값(XXXX)
                        Double item_weight_double = 0.0;    // 상품 Double 중량값
                        String item_weight_str = "";        // 상품 String 중량값

                        double item_pow = 0;              // 상품 zeroPoint에 대한 pow

                        String item_making_date = "";       // 상품 제조일
                        String item_box_serial = "";        // 상품 박스시리얼
                        /*
                         *      중량(LB체크) / 제조일 / 박스번호 Find
                         */
                        Log.d(TAG, "******************current_work_position:" + a.arSM.get(a.current_work_position).getITEM_TYPE());

                        if (a.arSM.get(a.current_work_position).getITEM_TYPE().equals(ShipmentConst.ITEM_TYPE_S)) {
                            String weight_from = a.work_item_bi_info.getWEIGHT_FROM();
                            String weight_to = a.work_item_bi_info.getWEIGHT_TO();

                            Log.d(TAG, "weightfrom,to:" + weight_from + ":" + weight_to + ":");
                            if (weight_from.equals("0") || weight_to.equals("0")) {
                                a.showAlertDialog("weight", 0);
                                a.alert_flag = true;
                            }

                            // 이마트 ITEM_TYPE W (바코드 계근)
                            item_weight = a.work_item_fullbarcode.substring(
                                    Integer.parseInt(a.work_item_bi_info.getWEIGHT_FROM()) - 1, Integer.parseInt(a.work_item_bi_info.getWEIGHT_TO()));
                            Log.i(TAG, "Type S | 절사한 중량값 : " + item_weight);

                            item_pow = Math.pow(10, Integer.parseInt(a.work_item_bi_info.getZEROPOINT()));
                            item_weight_double = Double.parseDouble(item_weight) / item_pow;

                            if ("LB".equals(a.work_item_bi_info.getBASEUNIT())) {
                                Log.i(TAG, "LB로 들어옴, 환산");
                                Log.i(TAG, "LB 원 중량 : " + item_weight_double);
                                // LB(파운드)라면 KG으로 환산 LB * 0.453592 = KG
                                double temp_weight_double = item_weight_double * 0.453592;

                                // 개발60 Step4 : 이마트 LB 자릿수 분기 제거 (생산은 항상 소수점 2자리)
                                item_weight_double = Math.floor(temp_weight_double * 100) / 100; //lb 변환 후 소수점 두자리까지 처리하도록 변경
                                item_weight_str = String.valueOf(item_weight_double);
                                Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + item_weight_double);
                                Log.i(TAG, "LB->KG | 환산 중량 String값 : " + item_weight_str);
                            }

                            String temp_weight = String.format("%.2f", item_weight_double);
                            item_weight_double = Double.parseDouble(temp_weight);
                            item_weight_str = String.valueOf(item_weight_double);
                            Log.i(TAG, "Type S | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
                            Log.i(TAG, "Type S | ZeroPoint 적용 중량 String값 : " + item_weight_str);

                            if (a.work_item_bi_info.getMAKINGDATE_FROM() != "" && a.work_item_bi_info.getMAKINGDATE_TO() != "") {
                                item_making_date = a.work_item_fullbarcode.substring(
                                        Integer.parseInt(a.work_item_bi_info.getMAKINGDATE_FROM()) - 1, Integer.parseInt(a.work_item_bi_info.getMAKINGDATE_TO()));
                                Log.i(TAG, "Type S | 절사한 제조일 : " + item_making_date);
                            }

                            if (a.work_item_bi_info.getBOXSERIAL_FROM() != "" && a.work_item_bi_info.getBOXSERIAL_TO() != "") {
                                item_box_serial = a.work_item_fullbarcode.substring(
                                        Integer.parseInt(a.work_item_bi_info.getBOXSERIAL_FROM()) - 1, Integer.parseInt(a.work_item_bi_info.getBOXSERIAL_TO()));
                                Log.i(TAG, "Type S | 절사한 박스시리얼 : " + item_box_serial);
                            }
                        } else if (a.arSM.get(a.current_work_position).getITEM_TYPE().equals(ShipmentConst.ITEM_TYPE_J)) {
                            item_weight = a.arSM.get(a.current_work_position).getPACKWEIGHT();
                            Log.i(TAG, "Type J | 지정된 중량값 : " + item_weight);
                            item_weight_double = Double.parseDouble(item_weight);
                            item_weight_str = String.valueOf(item_weight_double);
                            Log.i(TAG, "Type J | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
                            Log.i(TAG, "Type J | ZeroPoint 적용 중량 String값 : " + item_weight_str);
                        }

                    a.wet_data_insert(item_weight_str, item_weight_double, item_making_date, item_box_serial);
                    } else {
                        Toast.makeText(a.getApplicationContext(), "BL번호가 일치하지않습니다.\n확인 후 다시 스캔해주세요.", Toast.LENGTH_SHORT).show();
                        a.vibrator.vibrate(1000);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "setBarcodeMsgProduction's BL 스캔 Exception -> " + ex.getMessage().toString());
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "setBarcodeMsgProduction Exception -> " + ex.getMessage().toString());
        }
    }

    @Override
    public void onWeightConfirmed(String weightStr, double weightDouble, String makingDate, String boxSerial) {
        throw new UnsupportedOperationException("개발67 Step 4에서 이관 예정");
    }

    @Override
    public String findPackerProduct(String barcode, int workFlag) {
        throw new UnsupportedOperationException("개발67 Step 5에서 이관 예정");
    }

    @Override
    public void reprintLabel(String weightStr, String makingDate, String boxOrder, int selectPosition) {
        throw new UnsupportedOperationException("개발67 Step 4에서 이관 예정");
    }

    @Override
    public void onShipmentLoaded(ArrayList<Shipments_Info> arSM) {
        throw new UnsupportedOperationException("개발67 Step 4에서 이관 예정");
    }

    @Override
    public void onManualInput() {
        throw new UnsupportedOperationException("개발67 Step 7에서 이관 예정");
    }

    @Override
    public String send(Context context, ArrayList<Goodswets_Info> listSendInfo, ArrayList<Shipments_Info> arSM) {
        throw new UnsupportedOperationException("개발67 Step 6에서 이관 예정");
    }
}
