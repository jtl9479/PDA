package com.rgbsolution.highland_emart.shipment.type;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;
import com.rgbsolution.highland_emart.ExpiryEnterActivity;
import com.rgbsolution.highland_emart.R;
import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.common.HttpHelper;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Barcodes_Info;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * 홈플러스 출하(정량) — searchType "2" (개발66)
 *
 * <h3>이 타입의 판정 (개발66 §1.3)</h3>
 * <ul>
 *   <li>중복검사 우회 : X — 원본 1205 · 1329 조건 거짓</li>
 *   <li>트레이더스 소비기한 검증 : X — 원본 1294 조건 거짓</li>
 *   <li>LB 환산 ZEROPOINT 자릿수 : X — 원본 1443 · 1505 조건 거짓 (else 경로)</li>
 *   <li>계근 INSERT : <b>홈플러스 전용</b>(insertqueryGoodsWetHomeplus) + selectMaxBoxOrder</li>
 *   <li>계근중량 반올림 : 소수점 3자리</li>
 *   <li>계근 시 라벨 : 홈플러스 라벨(setHomeplusPrinting)</li>
 *   <li>전송 : <b>건별</b> / insert_goods_wet.jsp — 비정량(5)과 다르다</li>
 * </ul>
 *
 * <p>ITEM_TYPE "B" 블록은 상수 주석이 "홈플러스 비정량"이라고만 적혀 있을 뿐
 * searchType 게이트가 없다. VIEW 실측으로 미출력을 증명하기 전까지 <b>그대로 유지</b>한다(개발66 §1.4).</p>
 *
 * <p>운영 DB에 데이터가 0건일 수 있어 실기기 검증은 테스트 데이터 확보 후 진행한다.
 * 오류 32·33·34가 이 계열이다.</p>
 *
 * <p>{@link #onBarcodeScanned(String)} 는 원본 {@code setBarcodeMsg} 본문을 그대로 옮긴 것이다.
 * 접는 조건이 도매(3)·롯데(6)와 동일하므로 현재 내용이 같으나, 판단으로 묶지 않고 복사해 둔다.
 * 공통화는 Step 12에서 6개 파일을 diff한 뒤 수행한다.</p>
 */
public class HomeplusType implements ShipmentType {

    private static final String TAG = "HomeplusType";

    private final BixolonShipmentActivity a;

    public HomeplusType(BixolonShipmentActivity activity) {
        this.a = activity;
    }

    /**
     * 바코드 스캔 처리 — 원본 BixolonShipmentActivity.setBarcodeMsg 본문 이관 (개발66 Step 4)
     *
     * <p>원본 대비 접은 조건 (홈플러스 정량에서 항상 거짓)</p>
     * <ul>
     *   <li>원본 1213 · 1337 : {@code NONFIXED || HOMEPLUS_NONFIXED} 중복확인 제외 → 블록 삭제
     *       (비정량 5만 해당하며 정량 2는 해당하지 않는다)</li>
     *   <li>원본 1302 : {@code EMART} 트레이더스 소비기한 검증 → 내부 블록 삭제 (else if 골격은 유지)</li>
     *   <li>원본 1451 · 1513 : {@code EMART} LB 환산 자릿수 → else 경로만 유지</li>
     * </ul>
     *
     * <p>재귀 호출(원본 1235)은 Activity가 아니라 <b>자기 자신</b>을 호출한다.</p>
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
                Log.d(TAG, "setBarcodeMsg 중복 호출 무시 (디바운싱)");
                return;
            }

            a.lastProcessedBarcode = msg;
            a.lastBarcodeProcessedTime = now;

            Log.e(TAG, "========================setBarcodeMsg 시작======================");

            a.edit_barcode.setText(msg);
            if (a.scan_flag) { // 패커상품 스캔
                Log.e(TAG, "========================상품스캔======================" + a.work_flag);
                try {
                    String find_ppcodetemp = "";

                    if (a.work_flag == 1) {
                        Log.e(TAG, "========================상품바코드스캔1======================");
                        find_ppcodetemp = this.findPackerProduct(msg, 1);   // 원본 : a.find_PackerProduct(msg)
                        Log.e(TAG, "========================상품바코드스캔1 ppcode ======================" + find_ppcodetemp);
                    }else {
                        Log.e(TAG, "========================상품코드스캔2======================");
                        find_ppcodetemp = this.findPackerProduct(msg, 2);   // 원본 : a.find_PackerProductBarcodeGoods(msg)
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
                            boolean dup = DBHandler.duplicatequeryGoodsWet_check(a.getApplicationContext(), msg);
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

                            // 원본 1213 : 비정량(4,5) 중복확인 제외 분기 — 홈플러스 정량 미해당으로 접음

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
                                this.onBarcodeScanned(msg);       // 원본 1235 : setBarcodeMsg(msg) — 자기 자신 호출
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
                    Log.e(TAG, "setBarcodeMsg's 패커상품 스캔 Exception -> " + ex.getMessage().toString());
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

                        if (a.arSM.get(a.current_work_position).getPACKER_CODE().equals(ShipmentConst.KILKOY_PACKER_CODE) && a.arSM.get(a.current_work_position).getSTORE_CODE().equals(ShipmentConst.MEAT_CENTER_STORE_CODE)) { //킬코이제품이면서 이마트미트센터나갈때, 미트센터는 지점이 없기 때문에 센터코드와 스토어코드가 같다. 현재 뷰에 센터코드가 없어서 스토어코드로 처리
                            if (a.work_item_bi_info.getSHELF_LIFE().equals("") || a.work_item_bi_info.getMAKINGDATE_FROM().equals("") || a.work_item_bi_info.getMAKINGDATE_TO().equals("")) {
                                Toast.makeText(a.getApplicationContext(), "미트센터 납품 - KILKOY 상품의 경우 소비기한정보가 필수로 입력되어야 합니다.\n 현 상품의 계근을 진행할 수 없습니다. 관리자에게 문의하세요.", Toast.LENGTH_LONG).show();
                                a.vibrator.vibrate(1000);
                                a.work_ppcode = "";
                                a.scan_flag = true;
                                return;
                            }
                        } else if (a.arSM.get(a.current_work_position).getCENTERNAME().equals("용인TRD") || a.arSM.get(a.current_work_position).getCENTERNAME().equals("대구TRD") || a.arSM.get(a.current_work_position).getCENTERNAME().equals("시화(W)_TRD") || a.arSM.get(a.current_work_position).getCENTERNAME().equals("여주TRD") || a.arSM.get(a.current_work_position).getCENTERNAME().substring(0, 3).equals(ShipmentConst.CENTER_NAME_ET) || a.arSM.get(a.current_work_position).getCENTERNAME().contains(ShipmentConst.CENTER_NAME_ET)  ||  a.arSM.get(a.current_work_position).getCENTERNAME().contains(ShipmentConst.CENTER_NAME_WET)) {
                            // 원본 1302 : if (EMART) 트레이더스 소비기한 검증 — 홈플러스 미해당으로 접음.
                            // 바깥 else if 는 searchType 게이트가 아니므로 배타 관계 유지를 위해 골격만 남긴다.
                        }

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

                        // 원본 1337 : 비정량(4,5) 중복확인 제외 분기 — 홈플러스 정량 미해당으로 접음

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

                        if (a.arSM.get(a.current_work_position).getITEM_TYPE().equals(ShipmentConst.ITEM_TYPE_W) || a.arSM.get(a.current_work_position).getITEM_TYPE().equals(ShipmentConst.ITEM_TYPE_HW)) {
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
                            Log.i(TAG, "Type W | 절사한 중량값 : " + item_weight);

                            item_pow = Math.pow(10, Integer.parseInt(a.work_item_bi_info.getZEROPOINT()));
                            Log.i(TAG, "Type W | item_pow 확인 : " + item_pow);
                            Log.i(TAG, "Type W | zero point 확인 : " + a.work_item_bi_info.getZEROPOINT());

                            item_weight_double = Double.parseDouble(item_weight) / item_pow;

                            Log.i(TAG, "Type W | item_weight 확인 : " + Double.parseDouble(item_weight));
                            Log.i(TAG, "Type W | item_weight_double 확인 : " + item_weight_double);

                            if ("LB".equals(a.work_item_bi_info.getBASEUNIT())) {
                                // LB(파운드)라면 KG으로 환산 LB * 0.453592 = KG
                                double temp_weight_double = item_weight_double * 0.453592;
                                item_weight_double = Math.floor(temp_weight_double * item_pow) / item_pow;
                                item_weight_str = String.valueOf(item_weight_double);
                                Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + item_weight_double);
                                Log.i(TAG, "LB->KG | 환산 중량 String값 : " + item_weight_str);
                            }

                            item_weight_double = Math.floor(item_weight_double * 10);
                            item_weight_double = item_weight_double / 10.0;

                            String temp_weight = String.format("%.1f", item_weight_double);
                            item_weight_double = Double.parseDouble(temp_weight);
                            item_weight_str = String.valueOf(item_weight_double);
                            Log.i(TAG, "Type W | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
                            Log.i(TAG, "Type W | ZeroPoint 적용 중량 String값 : " + item_weight_str);

                            if (a.work_item_bi_info.getMAKINGDATE_FROM() != "" && a.work_item_bi_info.getMAKINGDATE_TO() != "") {
                                item_making_date = a.work_item_fullbarcode.substring(
                                        Integer.parseInt(a.work_item_bi_info.getMAKINGDATE_FROM()) - 1, Integer.parseInt(a.work_item_bi_info.getMAKINGDATE_TO()));
                                Log.i(TAG, "Type W | 절사한 제조일 : " + item_making_date);
                            }

                            if (a.work_item_bi_info.getBOXSERIAL_FROM() != "" && a.work_item_bi_info.getBOXSERIAL_TO() != "") {
                                item_box_serial = a.work_item_fullbarcode.substring(
                                        Integer.parseInt(a.work_item_bi_info.getBOXSERIAL_FROM()) - 1, Integer.parseInt(a.work_item_bi_info.getBOXSERIAL_TO()));
                                Log.i(TAG, "Type W | 절사한 박스시리얼 : " + item_box_serial);
                            }
                        } else if (a.arSM.get(a.current_work_position).getITEM_TYPE().equals(ShipmentConst.ITEM_TYPE_S)) {
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

                                // 원본 1451 : if (EMART) ZEROPOINT 자릿수 — 홈플러스는 else 경로
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
                            // 이마트 ITEM_TYPE J (지정된 중량 입력) | 바코드에서 중량, 제조일, 박스시리얼 X
                            item_weight = a.arSM.get(a.current_work_position).getPACKWEIGHT();
                            Log.i(TAG, "Type J | 지정된 중량값 : " + item_weight);
                            item_weight_double = Double.parseDouble(item_weight);
                            item_weight_str = String.valueOf(item_weight_double);
                            Log.i(TAG, "Type J | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
                            Log.i(TAG, "Type J | ZeroPoint 적용 중량 String값 : " + item_weight_str);
                        }

                        // Homeplus 비정량 "B"
                        if (a.arSM.get(a.current_work_position).getITEM_TYPE().equals(ShipmentConst.ITEM_TYPE_B)) {
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

                                // 원본 1513 : if (EMART) ZEROPOINT 자릿수 — 홈플러스는 else 경로
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
                        }

                    a.wet_data_insert(item_weight_str, item_weight_double, item_making_date, item_box_serial);
                    } else {
                        Toast.makeText(a.getApplicationContext(), "BL번호가 일치하지않습니다.\n확인 후 다시 스캔해주세요.", Toast.LENGTH_SHORT).show();
                        a.vibrator.vibrate(1000);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "setBarcodeMsg's BL 스캔 Exception -> " + ex.getMessage().toString());
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "setBarcodeMsg Exception -> " + ex.getMessage().toString());
        }
    }

    /**
     * 계근 저장 + 라벨 — 원본 BixolonShipmentActivity.wet_data_insert 본문 이관 (개발66 Step 8)
     *
     * <p>원본 대비 접은 조건 : INSERT(원본 1918~1934) · 중량 반올림 4곳(1940 · 1954 · 1972 · 1982) ·
     * 계근 라벨(1624~1641) 을 이 타입 경로 하나로 고정했다. 그 외 로직은 원본 그대로다.</p>
     */
    @Override
    public void onWeightConfirmed(String weight_str, double weight_double, String making_date, String box_serial) {
        Log.e(TAG, "=========================계근입력 시작=========================" + weight_double);

        if (a.arSM.get(a.current_work_position).getGI_REQ_PKG().equals(String.valueOf(a.arSM.get(a.current_work_position).getPACKING_QTY()))) {
            if ((a.arSM.size() - 1) == a.current_work_position) {
                a.show_wetFinishDialog();
            } else {
                Toast.makeText(a.getApplicationContext(), "계근이 끝난 지점입니다.\n다음 지점을 작업해주세요.", Toast.LENGTH_SHORT).show();
                a.vibrator.vibrate(300);
            }
            return;
        }

        Goodswets_Info gi = new Goodswets_Info();
        gi.setGI_D_ID(a.arSM.get(a.current_work_position).getGI_D_ID());
        gi.setGI_L_ID(a.arSM.get(a.current_work_position).getGI_L_ID());
        gi.setWEIGHT(weight_str);
        gi.setWEIGHT_UNIT(a.work_item_bi_info.getBASEUNIT());
        gi.setPACKER_PRODUCT_CODE(a.arSM.get(a.current_work_position).getPACKER_PRODUCT_CODE());
        gi.setBARCODE(a.work_item_fullbarcode);
        gi.setPACKER_CLIENT_CODE(a.work_item_bi_info.getPACKER_CLIENT_CODE());
        gi.setMAKINGDATE(making_date);
        gi.setBOXSERIAL(box_serial);
        gi.setBOX_CNT(String.valueOf((a.arSM.get(a.current_work_position).getPACKING_QTY() + 1)));
        gi.setEMARTITEM_CODE(a.arSM.get(a.current_work_position).getEMARTITEM_CODE());
        gi.setEMARTITEM(a.arSM.get(a.current_work_position).getEMARTITEM());
        gi.setITEM_CODE(a.arSM.get(a.current_work_position).getITEM_CODE());
        gi.setBRAND_CODE(a.arSM.get(a.current_work_position).getBRAND_CODE());
        gi.setREG_ID(Common.REG_ID);
        gi.setSAVE_TYPE("F");
        gi.setDUPLICATE("F");


        // 원본 1918 : searchType 2(홈플러스) INSERT 경로. 이 타입 고정이라 조건문만 제거
        int maxBoxOrder = DBHandler.selectMaxBoxOrder(a);
        Log.e(TAG, "=======================MAX BOX ORDER ###=========================" + maxBoxOrder);
        DBHandler.insertqueryGoodsWetHomeplus(a, gi, maxBoxOrder);

        Log.e(TAG, "=========================계근중량 변환전=========================" + weight_double);

        String temp_weight = "";

        // 원본 1944 : else 경로(그대로 입력). 이 타입은 EMART(0)가 아니다
        temp_weight = Double.toString(weight_double); //생산일 경우 그대로 입력

        weight_double = Double.parseDouble(temp_weight);

        Log.e(TAG, "=========================계근중량 변환후=========================" + weight_double);

        a.arSM.get(a.current_work_position).setPACKING_QTY(a.arSM.get(a.current_work_position).getPACKING_QTY() + 1);           // 계근수량

        // 원본 1954 : 계근중량 합산 — else 3자리 경로
        double v1 = a.arSM.get(a.current_work_position).getGI_QTY();
        double v2 = weight_double;

        double v3 = v1+v2;
        double v4 = Math.round(v3*1000)/1000.0;

        a.arSM.get(a.current_work_position).setGI_QTY(v4);    // 계근중량 변경 후
        Log.e(TAG, "=========================chk prod 계근중량=========================" + v4);

        a.centerWorkCount++;
        a.centerWorkWeight += weight_double;

        Log.e(TAG, "=========================센터중량 변환전=========================" + a.centerWorkWeight);

        // 원본 1972 : 센터중량 반올림 — else 소수 3자리
        a.centerWorkWeight = Math.round(a.centerWorkWeight*1000)/1000.0; //생산일 경우 소수점 넷째자리에서 반올림

        Log.e(TAG, "=========================센터중량 변환후=========================" + a.centerWorkWeight);

        a.edit_center_tcount.setText(a.centerTotalCount + " / " + a.centerWorkCount);

        // 원본 1982 : 센터중량 표시 — else 경로
        a.edit_center_tweight.setText(a.centerTotalWeight + " / " + a.centerWorkWeight);

        a.edit_wet_count.setText(a.arSM.get(a.current_work_position).getGI_REQ_PKG() + " / " + a.arSM.get(a.current_work_position).getPACKING_QTY());
        a.edit_wet_weight.setText(a.arSM.get(a.current_work_position).getGI_REQ_QTY() + " / " + a.arSM.get(a.current_work_position).getGI_QTY());

        Log.d(TAG, "==================================================");
        Log.d(TAG, "====================계근작업 종료===================");
        Log.i(TAG, "centerWorkCount : " + a.centerWorkCount);
        Log.i(TAG, "centerWorkWeight : " + a.centerWorkWeight);
        Log.d(TAG, "==================================================");

        for (int i = 0; i < a.arSM.size(); i++) {
            a.arSM.get(i).setWORK_FLAG(0);
        }

        a.arSM.get(a.current_work_position).setWORK_FLAG(1);
        a.sListAdapter.notifyDataSetChanged();
        a.sList.setSelection(a.current_work_position);

        if (Common.print_bool) {
            // 원본 2006~2021 : 라벨 분기 — 이 타입 고정이라 searchType 조건만 제거
            Log.d(TAG, "===========홈플 출력 시작 ================");
            a.labelPrintHelper.setHomeplusPrinting(weight_double, a.arSM.get(a.current_work_position), false, a.printerCallback);
        }

        a.set_scanFlag(true);

        if (Integer.parseInt(a.arSM.get(a.current_work_position).getGI_REQ_PKG()) <= a.arSM.get(a.current_work_position).getPACKING_QTY()) {
            // 요청수량과 계근수량이 같을 때 (계근이 끝났을 때)
            if ((a.centerTotalCount > 0) && (a.centerTotalCount == a.centerWorkCount)) {       // 총 계근 완료
                a.show_wetFinishDialog();
            }
        }
    }

    /**
     * 상품 매칭 — 원본 find_PackerProduct · find_PackerProductBarcodeGoods 이관 (개발66 Step 9)
     *
     * <p>원본은 {@code work_flag} 로 두 메서드를 나눠 호출했다. 인자와 실행 순서는 원본과 같다.</p>
     */
    @Override
    public String findPackerProduct(String barcode, int workFlag) {
        if (workFlag == 1) {   // 원본 find_PackerProduct (work_flag 1 : 상품 바코드 스캔)
            try {
                String pp_code = "";
                Log.e(TAG, "========================pp_code 가져오기 시작======================");
                pp_code = find_work_info(barcode, true);
                Log.e(TAG, "========================pp_code 가져오기 끝======================");
                if (!a.edit_product_name.getText().equals("")) {
                    return pp_code;
                } else {
                    return "null";
                }
            } catch (Exception ex) {
                Log.e(TAG, "find_PackerProduct Exception | " + ex.getMessage().toString());
                return "null";
            }
        } else {              // 원본 find_PackerProductBarcodeGoods (work_flag 2 : 상품코드 스캔)
            Log.e(TAG, "find_PackerProductBarcodeGoods");

            try {
                String pp_code = "";
                pp_code = find_work_info_barcodeGoods(barcode, false);
                if (!a.edit_product_name.getText().equals("")) {
                    return pp_code;
                } else {
                    return "null";
                    //scanFlag_swap();
                }
            } catch (Exception ex) {
                Log.e(TAG, "find_PackerProduct Exception | " + ex.getMessage().toString());
                return "null";
            }
        }
    }

    /**
     * 바코드정보 매칭 — 원본 find_work_info 이관 (개발66 Step 9)
     *
     * <p>구간 추출(원본 2121)은 길이 체크가 있는 쪽이며, {@code find_work_info_barcodeGoods}(원본 2184) 와
     * 합치지 않는다. 항상 참인 {@code Editable} 비교(원본 2065 · 2081)도 원본 그대로 둔다.</p>
     */
    private String find_work_info(String req, boolean type) {
        try {
            String pp_code = "";
            int count = 0;
            ArrayList<Barcodes_Info> list_barcode_info = DBHandler.selectqueryBarcodeInfo(a);
            Log.e(TAG, "===================바코드 디비조회 완료=======================");
            Log.e(TAG, "===================    req check !!   ======================="+req); //여기서 풀바코드를 던진다
            for (Barcodes_Info bi : list_barcode_info) {
                String bg = bi.getBARCODEGOODS();
                String bg_from = bi.getBARCODEGOODS_FROM();
                String bg_to = bi.getBARCODEGOODS_TO();
                String temp_bg;

                Log.i(TAG, "BARCODEGOODS \t\tFROM : " + bg_from + "\t TO : " + bg_to);
                Log.i(TAG, "BARCODEGOODS : \t\t" + bg);

                if (type && req.length() >= Integer.parseInt(bg_to)) {              // PACKER_PRODUCT_CODE로 찾을 경우
                    temp_bg = req.substring(Integer.parseInt(bg_from) - 1, Integer.parseInt(bg_to));
                } else {                // false : BL로 찾을 경우
                    temp_bg = req;
                }

                Log.i(TAG, "TEMP BARCODEGOODS : \t" + temp_bg);
                Log.i(TAG, "TEMP BARCODEGOODS eq : \t" + temp_bg.equals(bg));

                if (temp_bg.equals(bg)) {                       // barcodegoods find success
                    Log.i(TAG, "barcodegoods find success");
                    a.work_item_bi_info = bi;
                    a.edit_product_name.setText(bi.getITEM_NAME_KR());
                    a.edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
                    if(count == 0){
                        pp_code = bi.getPACKER_PRODUCT_CODE();
                    }else{
                        pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();
                    }
                    Log.i(TAG, "===================pp_code=================" + pp_code);
                    a.work_item_barcodegoods = bg;
                    count++;
                } else {
                    a.edit_product_name.setText("");
                    a.edit_product_code.setText("");
                    a.work_item_barcodegoods = "";
                }

                // 원본 2149 : searchType 4(이마트 비정량) 전부 매칭 — 이 타입은 미해당으로 접었다
            }

            Log.i(TAG, "===================return pp_code test!!! =================" + pp_code);
            return pp_code;
        } catch (Exception ex) {
            Log.e(TAG, "======== find_work_info Exception ========");
            Log.e(TAG, ex.getMessage().toString());
            return "null";
        }
    }

    /**
     * 상품코드 매칭 — 원본 find_work_info_barcodeGoods 이관 (개발66 Step 9)
     */
    private String find_work_info_barcodeGoods(String req, boolean type) {
        Log.e(TAG, "find_work_info_barcodeGoods");
        try {
            String pp_code = "";
            int count = 0;
            ArrayList<Barcodes_Info> list_barcode_info = DBHandler.selectqueryBarcodeGoodsInfo(a);
            for (Barcodes_Info bi : list_barcode_info) {
                String bg = bi.getBARCODEGOODS();
                String bg_from = bi.getBARCODEGOODS_FROM();
                String bg_to = bi.getBARCODEGOODS_TO();
                String temp_bg;
                if (type) {              // PACKER_PRODUCT_CODE로 찾을 경우
                    temp_bg = req.substring(Integer.parseInt(bg_from) - 1, Integer.parseInt(bg_to));
                } else {                // false : BL로 찾을 경우
                    temp_bg = req;
                }
                Log.i(TAG, "BARCODEGOODS \t\tFROM : " + bg_from + "\t TO : " + bg_to);
                Log.i(TAG, "BARCODEGOODS : \t\t" + bg);
                Log.i(TAG, "TEMP BARCODEGOODS : \t" + temp_bg);

                if (temp_bg.equals(bg)) {                       // barcodegoods find success
                    //pp_name = bi.getITEM_NAME_KR();
                    a.work_item_bi_info = bi;
                    a.edit_product_name.setText(bi.getITEM_NAME_KR());
                    a.edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
                    if(count == 0){
                        pp_code = bi.getPACKER_PRODUCT_CODE();
                    }else{
                        pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();
                    }
                    Log.i(TAG, "===================pp_code=================" + pp_code);
                    //a.work_ppcode = bi.getPACKER_PRODUCT_CODE();
                    a.work_item_barcodegoods = bg;
                    count++;
                } else {
                    a.edit_product_name.setText("");
                    a.edit_product_code.setText("");
                    a.work_item_barcodegoods = "";
                }
            }
            return pp_code;
        } catch (Exception ex) {
            Log.e(TAG, "======== find_work_info_barcodeGoods Exception ========");
            Log.e(TAG, ex.getMessage().toString());
            return "null";
        }
    }

    /**
     * 재출력 라벨 — 원본 mHandler MESSAGE_REPRINT 분기 이관 (개발66 Step 8)
     */
    @Override
    public void reprintLabel(String print_weight_str, String making_date, String box_order, int select_position) {
        // 원본 939 : searchType 2·5 홈플러스 라벨 경로. 이 타입 고정이라 조건문만 제거
        a.labelPrintHelper.setHomeplusPrinting(Double.parseDouble(print_weight_str), a.arSM.get(select_position), true, a.printerCallback);
    }

    /**
     * 출하대상 조회 후처리 — 이 타입은 원본에 해당 분기가 없다 (개발66 Step 8)
     *
     * <p>원본 2549 의 박스순번 초기화는 searchType 6(롯데) 전용이다.</p>
     */
    @Override
    public void onShipmentLoaded(ArrayList<Shipments_Info> arSM) {
        // 원본 2549 : 롯데 전용 블록이라 이 타입은 수행할 작업이 없다
    }

    /**
     * 수기 입력 — 원본 inputBtnListener 의 {@code work_flag == 0} 분기 이관 (개발66 Step 11)
     *
     * <p>입력값 검증(원본 647~655)은 Activity 에 남아 있고, 이 메서드는 검증을 통과한 뒤의 처리만 한다.</p>
     *
     * <p>킬코이 · 미트센터 분기(원본 691~710)와 바깥 {@code else if} 의 CENTERNAME 판정은
     * searchType 게이트가 아니라 데이터 의존이므로 접지 않는다(문서 Step 11 #3).</p>
     */
    @Override
    public void onManualInput() {
        a.work_item_fullbarcode = "";
        String weight_str = a.edit_barcode.getText().toString();    // 입력값 저장

        Log.i(TAG, "=====================weight_str 1==================" + weight_str);

        double weight_double = Double.parseDouble(weight_str);    // 소수점 1자리로 변환

        Log.i(TAG, "=====================weight_double 1==================" + weight_double);

        String temp_weight = "";

        // 원본 675 : else 경로(그대로 입력). 이 타입은 EMART("0")가 아니다
        temp_weight = Double.toString(weight_double); //생산일 경우 그대로 입력
        Log.i(TAG, "=====================temp_weight production==================" + temp_weight);

        Log.i(TAG, "=====================temp_weight out==================" + temp_weight);

        weight_double = Double.parseDouble(temp_weight); //생산이든 출하든 똑같이 타야함

        Log.i(TAG, "=====================weight_double 3==================" + weight_double);

        weight_str = String.valueOf(weight_double);                                         // 반올림값 다시 저장

        Log.i(TAG, "=====================패커코드 체크==================" + a.arSM.get(a.current_work_position).getPACKER_CODE());
        Log.i(TAG, "=====================스토어코드 체크==================" + a.arSM.get(a.current_work_position).getSTORE_CODE());

        if (a.arSM.get(a.current_work_position).getPACKER_CODE().equals(ShipmentConst.KILKOY_PACKER_CODE)
                && a.arSM.get(a.current_work_position).getSTORE_CODE().equals(ShipmentConst.MEAT_CENTER_STORE_CODE)) {
              String makingFrom = a.work_item_bi_info.getMAKINGDATE_FROM();
              String makingTo = a.work_item_bi_info.getMAKINGDATE_TO();

              Intent IntentA = new Intent(a, ExpiryEnterActivity.class);

              String weightStrKey = "weightStrKey";
              String weightDblKey = "weightDblKey";
              String makingFromKey = "makingFromKey";
              String makingToKey = "makingToKey";

              IntentA.putExtra(weightStrKey,weight_str);
              IntentA.putExtra(weightDblKey,weight_double);

              IntentA.putExtra(makingFromKey,makingFrom);
              IntentA.putExtra(makingToKey,makingTo);

              a.startActivityForResult(IntentA,BixolonShipmentActivity.GET_DATA_REQUEST);

        // 원본 711 : 바깥 else if 의 `|| searchType == LOTTE` 는 이 타입에서 거짓이라 그 항만 접었다.
        // CENTERNAME 판정은 데이터 의존이라 그대로 둔다.
        }else if(a.arSM.get(a.current_work_position).getCENTERNAME().contains(ShipmentConst.CENTER_NAME_TRD) || a.arSM.get(a.current_work_position).getCENTERNAME().contains(ShipmentConst.CENTER_NAME_WET) || a.arSM.get(a.current_work_position).getCENTERNAME().contains(ShipmentConst.CENTER_NAME_ET)){
            // 원본 712 : if (EMART || LOTTE) 소비기한 창 — 이 타입은 미해당이라 else(원본 731) 경로만 남긴다
            a.wet_data_insert(weight_str, weight_double, "", "");
        }else{
            a.wet_data_insert(weight_str, weight_double, "", "");
        }
        a.edit_barcode.setText("");
    }
    /**
     * 서버 전송 — 원본 ProgressDlgShipmentSend.doInBackground 의 <b>건별 루프</b>(원본 2938~3014) 이관 (개발66 Step 10)
     *
     * <p>목록 조회(원본 2919~2937)는 Activity 에 남아 있고, 이 메서드는 조회 결과를 받아 전송만 한다.
     * 건별은 계근 1건마다 패킷을 만들어 {@code insert_goods_wet.jsp} 로 보내고 그때마다 로컬DB를 갱신한다.</p>
     */
    @Override
    public String send(Context mContext, ArrayList<Goodswets_Info> list_send_info, ArrayList<Shipments_Info> arSM) {
        String result = "";
        try {
            int iCount = 0;
            int jChk = 0;

            for (int i = 0; i < list_send_info.size(); i++) { //SAVE_TYPE 과 상관 없이 계근 데이터 모두 루프
                if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
                    iCount++;
                    String packet = "";
                    packet += list_send_info.get(i).getGI_D_ID() + "::";
                    packet += list_send_info.get(i).getWEIGHT() + "::";
                    packet += list_send_info.get(i).getWEIGHT_UNIT() + "::";
                    packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::";
                    packet += list_send_info.get(i).getBARCODE() + "::";
                    packet += list_send_info.get(i).getPACKER_CLIENT_CODE() + "::";
                    packet += list_send_info.get(i).getMAKINGDATE() + "::";
                    packet += list_send_info.get(i).getBOXSERIAL() + "::";
                    packet += list_send_info.get(i).getBOX_CNT() + "::";
                    packet += list_send_info.get(i).getREG_ID() + "::";
                    packet += Common.selectCompanyCode + "::";
                    packet += list_send_info.get(i).getBRAND_CODE() + "::";
                    packet += list_send_info.get(i).getCLIENT_TYPE() + "::";
                    packet += list_send_info.get(i).getBOX_ORDER() + "::";
                    packet += list_send_info.get(i).getGI_L_ID();


                    if (Common.D) {
                        Log.d(TAG, "Send Packet : '" + packet + "'");
                    }

                    Log.i(TAG, "=====================Common.searchType==================" + Common.searchType);

                    // 원본 2966~2974 : 디비접속 설정 분기 — 홈플러스(2)는 원본 2968 의 else if 경로(롯데와 같은 호출)
                    result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);

                    //결과값의 앞, 뒤에 공백 제거
                    result = result.replace("\r\n", "");
                    result = result.replace("\n", "");
                    Log.d(TAG, "i number : " + i);
                    Log.v(TAG, "전송결과 : " + result);
                    //s : 성공, f : 실패
                    if (result.equals("s")) {
                        boolean bool = DBHandler.updatequeryGoodsWet(mContext, list_send_info.get(i).getGI_D_ID(), list_send_info.get(i).getBARCODE(), list_send_info.get(i).getBOX_CNT(), list_send_info.get(i).getGI_L_ID());
                        Log.d(TAG, "boolean " + bool);
                        if (bool) {          // 전송 & PDA SQLite update 성공
                            // 원본 2986 : publishProgress("progress", ...) — onProgressUpdate 가 super 호출뿐인 no-op 이라 이관하지 않는다(동작 동일)

                            for (int j = 0; j < arSM.size(); j++) {
                                if (arSM.get(j).getGI_D_ID().equals(list_send_info.get(i).getGI_D_ID())
                                        && arSM.get(j).getGI_L_ID().equals(list_send_info.get(i).getGI_L_ID())) {
                                    arSM.get(j).setSAVE_CNT(arSM.get(j).getSAVE_CNT() + 1);

                                    if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {            // 전송 개수와 요청 개수 비교
                                            Log.v(TAG, "출하대상 계근 완료");
                                            arSM.get(j).setSAVE_TYPE("Y");          // 전부 전송했다면 전송여부 Y로 변경
                                            DBHandler.updatequeryShipment(mContext, arSM.get(j).getGI_D_ID(), arSM.get(j).getPACKER_PRODUCT_CODE(), arSM.get(j).getGI_L_ID());

                                            jChk++;

                                            if (jChk == arSM.size()) {
                                                Log.d(TAG, "arSM.size() when return: " + arSM.size());
                                                Log.d(TAG, "jChk number when return: " + jChk);
                                                return "ss";
                                            }
                                    }
                                }
                            }
                        }
                    } else if (result.equals("f")) {
                        return result;
                    }
                }
            }

            return result;
        } catch (Exception ex) {
            Log.e(TAG, "======== ProgressDlgShipmentSend doInBackgounrd Exception ========");
            Log.e(TAG, ex.toString());
            return null;
        }
    }
}
