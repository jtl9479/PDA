package com.rgbsolution.highland_emart.shipment.type;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;
import com.rgbsolution.highland_emart.R;
import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * 이마트 비정량 — searchType "4" (개발66)
 *
 * <h3>이 타입의 판정 (개발66 §1.3)</h3>
 * <ul>
 *   <li>중복검사 우회 : <b>O</b> — 원본 1205 · 1329 조건 참 (dup = false)</li>
 *   <li>바코드 전부 매칭 : <b>O</b> — 원본 2150 (이 타입 전용). 상품 매칭 축이므로 Step 9 범위</li>
 *   <li>트레이더스 소비기한 검증 : <b>X</b> — 원본 1294는 searchType "0" 전용</li>
 *   <li>LB 환산 ZEROPOINT 자릿수 : <b>X</b> — 원본 1443 · 1505도 "0" 전용 (else 경로)</li>
 *   <li>계근 INSERT : 일반 (insertqueryGoodsWet)</li>
 *   <li>계근중량 반올림 : 소수점 3자리 — 정량(0)과 다르다</li>
 *   <li>계근 시 라벨 : 이마트 라벨(setPrinting) — 정량(0)과 동일</li>
 *   <li>전송 : <b>일괄</b> / insert_goods_wet_new.jsp — 정량(0)과 다르다</li>
 * </ul>
 *
 * <p><b>주의</b> — 상수 주석(원본 164줄)에 "도매 비정량"으로 적혀 있으나 실제로는 이마트다.
 * 라벨이 이마트 것이고(원본 2012~2014) 로그도 "이마트(비정량)"이다.
 * 다만 이름이 이마트라고 해서 정량(0)과 같게 만들면 안 된다. {@code SEARCH_TYPE_EMART} 는 "0"이므로
 * 이 타입은 트레이더스 검증(1302)과 LB 자릿수(1451 · 1513)에 해당하지 않는다.</p>
 *
 * <p><b>중복검사 조회 자체는 건너뛰지 않는다.</b> 원본은 조회한 뒤 결과를 false로 덮는다(§1.5).</p>
 *
 * <p>{@link #onBarcodeScanned(String)} 는 원본 {@code setBarcodeMsg} 본문을 그대로 옮긴 것이다.
 * 공통화는 Step 12에서 6개 파일을 diff한 뒤 수행한다.</p>
 */
public class EmartNonfixedType implements ShipmentType {

    private static final String TAG = "EmartNonfixedType";

    private final BixolonShipmentActivity a;

    public EmartNonfixedType(BixolonShipmentActivity activity) {
        this.a = activity;
    }

    /**
     * 바코드 스캔 처리 — 원본 BixolonShipmentActivity.setBarcodeMsg 본문 이관 (개발66 Step 6)
     *
     * <p>원본 대비 접은 조건</p>
     * <ul>
     *   <li>원본 1213 · 1337 : {@code NONFIXED || HOMEPLUS_NONFIXED} → <b>항상 참</b>.
     *       조건문만 제거하고 {@code dup = false;} 는 남긴다</li>
     *   <li>원본 1302 : {@code EMART}("0") 트레이더스 소비기한 검증 → <b>거짓</b>이므로 내부 블록 삭제
     *       (else if 골격은 유지)</li>
     *   <li>원본 1451 · 1513 : {@code EMART}("0") LB 환산 자릿수 → <b>거짓</b>이므로 else 경로만 유지</li>
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

                            // 원본 1213 : 비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외 (조건 항상 참이라 조건문만 제거)
                            dup = false;

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
                            // 원본 1302 : if (EMART) 트레이더스 소비기한 검증 — searchType "0" 전용이라 이마트 비정량("4")은 미해당으로 접음.
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

                        // 원본 1337 : 비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외 (조건 항상 참이라 조건문만 제거)
                        dup = false;

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

                                // 원본 1451 : if (EMART) ZEROPOINT 자릿수 — 이마트 비정량("4")은 else 경로
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

                                // 원본 1513 : if (EMART) ZEROPOINT 자릿수 — 이마트 비정량("4")은 else 경로
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
     * <p>원본 대비 접은 조건 : INSERT(원본 1537~1553) · 중량 반올림 4곳(1559 · 1573 · 1591 · 1601) ·
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


        // 원본 1551 : 일반 INSERT(else 경로). 이 타입은 홈플러스·롯데 분기에 해당하지 않는다
        DBHandler.insertqueryGoodsWet(a, gi);

        Log.e(TAG, "=========================계근중량 변환전=========================" + weight_double);

        String temp_weight = "";

        // 원본 1563 : else 경로(그대로 입력). 이 타입은 EMART(0)가 아니다
        temp_weight = Double.toString(weight_double); //생산일 경우 그대로 입력

        weight_double = Double.parseDouble(temp_weight);

        Log.e(TAG, "=========================계근중량 변환후=========================" + weight_double);

        a.arSM.get(a.current_work_position).setPACKING_QTY(a.arSM.get(a.current_work_position).getPACKING_QTY() + 1);           // 계근수량

        // 원본 1573 : 계근중량 합산 — else 3자리 경로
        double v1 = a.arSM.get(a.current_work_position).getGI_QTY();
        double v2 = weight_double;

        double v3 = v1+v2;
        double v4 = Math.round(v3*1000)/1000.0;

        a.arSM.get(a.current_work_position).setGI_QTY(v4);    // 계근중량 변경 후
        Log.e(TAG, "=========================chk prod 계근중량=========================" + v4);

        a.centerWorkCount++;
        a.centerWorkWeight += weight_double;

        Log.e(TAG, "=========================센터중량 변환전=========================" + a.centerWorkWeight);

        // 원본 1591 : 센터중량 반올림 — else 소수 3자리
        a.centerWorkWeight = Math.round(a.centerWorkWeight*1000)/1000.0; //생산일 경우 소수점 넷째자리에서 반올림

        Log.e(TAG, "=========================센터중량 변환후=========================" + a.centerWorkWeight);

        a.edit_center_tcount.setText(a.centerTotalCount + " / " + a.centerWorkCount);

        // 원본 1601 : 센터중량 표시 — else 경로
        a.edit_center_tweight.setText(a.centerTotalWeight + " / " + a.centerWorkWeight);

        a.edit_wet_count.setText(a.arSM.get(a.current_work_position).getGI_REQ_PKG() + " / " + a.arSM.get(a.current_work_position).getPACKING_QTY());
        a.edit_wet_weight.setText(a.arSM.get(a.current_work_position).getGI_REQ_QTY() + " / " + a.arSM.get(a.current_work_position).getGI_QTY());

        Log.d(TAG, "==================================================");
        Log.d(TAG, "====================계근작업 종료===================");
        Log.i(TAG, "a.centerWorkCount : " + a.centerWorkCount);
        Log.i(TAG, "a.centerWorkWeight : " + a.centerWorkWeight);
        Log.d(TAG, "==================================================");

        for (int i = 0; i < a.arSM.size(); i++) {
            a.arSM.get(i).setWORK_FLAG(0);
        }

        a.arSM.get(a.current_work_position).setWORK_FLAG(1);
        a.sListAdapter.notifyDataSetChanged();
        a.sList.setSelection(a.current_work_position);

        if (Common.print_bool) {
            // 원본 1625~1640 : 라벨 분기 — 이 타입 고정이라 searchType 조건만 제거
            Log.d(TAG, "===========이마트(비정량) 출력 시작 ================");
            a.labelPrintHelper.setPrinting(weight_double, a.arSM.get(a.current_work_position), false, making_date, a.work_item_bi_info, a.arSM.get(a.current_work_position), Common.searchType, a.printerCallback);
        }

        a.set_scanFlag(true);

        if (Integer.parseInt(a.arSM.get(a.current_work_position).getGI_REQ_PKG()) <= a.arSM.get(a.current_work_position).getPACKING_QTY()) {
            // 요청수량과 계근수량이 같을 때 (계근이 끝났을 때)
            if ((a.centerTotalCount > 0) && (a.centerTotalCount == a.centerWorkCount)) {       // 총 계근 완료
                a.show_wetFinishDialog();
            }
        }
    }

    @Override
    public String findPackerProduct(String barcode, int workFlag) {
        throw new UnsupportedOperationException("개발66 Step 9에서 이관 예정");
    }

    /**
     * 재출력 라벨 — 원본 mHandler MESSAGE_REPRINT 분기 이관 (개발66 Step 8)
     */
    @Override
    public void reprintLabel(String print_weight_str, String making_date, String box_order, int select_position) {
        // 원본 957 : else 경로(이마트 수기 프린팅). 이 타입은 홈플러스·롯데·생산라벨 분기에 해당하지 않는다
        a.labelPrintHelper.setPrinting(Double.parseDouble(print_weight_str), a.arSM.get(select_position), true, making_date, a.work_item_bi_info, a.arSM.get(a.current_work_position), Common.searchType, a.printerCallback);
    }

    /**
     * 출하대상 조회 후처리 — 이 타입은 원본에 해당 분기가 없다 (개발66 Step 8)
     *
     * <p>원본 2112 의 박스순번 초기화는 searchType 6(롯데) 전용이다.</p>
     */
    @Override
    public void onShipmentLoaded(ArrayList<Shipments_Info> arSM) {
        // 원본 2112 : 롯데 전용 블록이라 이 타입은 수행할 작업이 없다
    }

    @Override
    public String send(Context context, ArrayList<Goodswets_Info> listSendInfo, ArrayList<Shipments_Info> arSM) {
        throw new UnsupportedOperationException("개발66 Step 10에서 이관 예정");
    }
}
