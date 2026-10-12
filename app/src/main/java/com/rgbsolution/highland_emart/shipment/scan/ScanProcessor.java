package com.rgbsolution.highland_emart.shipment.scan;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.rgbsolution.highland_emart.R;
import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.shipment.mode.ShipmentMode;

/**
 * 스캔 바코드 처리 흐름 (개발/77 Step 8) — BixolonShipmentActivity.setBarcodeMsg 본문을 그대로 이동
 * 상태는 ScanState(Activity 와 공유), 화면·태스크 호출은 ScanHost 로 한다.
 */
public class ScanProcessor {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    /** setBarcodeMsg 중복 호출 방지 간격 (ms) */
    private static final long BARCODE_PROCESS_DEBOUNCE_MS = 1000;

    private final Context mContext;
    private final ScanState scan;
    private final ShipmentMode mode;
    private final ScanHost host;

    public ScanProcessor(Context context, ScanState scan, ShipmentMode mode, ScanHost host) {
        this.mContext = context;
        this.scan = scan;
        this.mode = mode;
        this.host = host;
    }

    /**
     * 스캔된 바코드 처리 (생산 제외 6종)
     * {@code scan_flag} 가 true 면 상품 바코드, false 면 BL번호 차례다.
     * 생산(1)은 진입 즉시 {@link ScanHost#setBarcodeMsgProduction(String)} 으로 위임한다.
     * @param msg 스캔된 바코드 문자열
     */
    public void process(final String msg) {
        // 생산(searchType=1) : 전용 메서드로 분리 (개발60)
        if (mode.usesProductionBarcodeFlow()) {
            host.setBarcodeMsgProduction(msg);
            return;
        }

        try {
            if (scan.dialog_flag)
                return;

            // 중복 호출 방지: 동일 바코드가 1초 이내 재처리되면 무시 (다른 바코드는 통과)
            long now = System.currentTimeMillis();

            if (msg != null && msg.equals(scan.lastProcessedBarcode)
                    && (now - scan.lastBarcodeProcessedTime) < BARCODE_PROCESS_DEBOUNCE_MS) {
                Log.d(TAG, "setBarcodeMsg 중복 호출 무시 (디바운싱)");
                return;
            }

            scan.lastProcessedBarcode = msg;
            scan.lastBarcodeProcessedTime = now;

            Log.e(TAG, "========================setBarcodeMsg 시작======================");

            host.setBarcodeText(msg);
            if (scan.scan_flag) { // 패커상품 스캔
                Log.e(TAG, "========================상품스캔======================" + scan.work_flag);
                try {
                    String find_ppcodetemp = "";

                    if (scan.work_flag == 1) {
                        find_ppcodetemp = host.findPackerProduct(msg);
                        Log.e(TAG, "========================상품바코드스캔1 ppcode ======================" + find_ppcodetemp);
                    }else {
                        find_ppcodetemp = host.findPackerProductBarcodeGoods(msg);
                        Log.e(TAG, "========================상품코드스캔2 ppcode ======================" + find_ppcodetemp);
                    }

                    Log.e(TAG, "========================바코드 정보가져옴======================");
                    final String find_ppcode = find_ppcodetemp;

                    if (find_ppcode.equals("null")) {
                        Toast.makeText(mContext.getApplicationContext(), "패커상품이 존재하지않거나,\n바코드가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        host.vibrate(1000);
                        scan.work_item_fullbarcode = "";
                        scan.work_item_barcodegoods = "";
                    } else {
                        if (scan.work_ppcode.equals("")) {
                            Log.e(TAG, "========================find_ppcode test!!======================"+find_ppcode);
                            scan.work_ppcode = find_ppcode;
                            scan.work_item_fullbarcode = msg;
                            host.startShipSelect(host.getSelectedCenterName(), find_ppcode, scan.scan_flag);
                        } else if (!scan.work_ppcode.equals("") && scan.work_ppcode.equals(find_ppcode)) {         // 작업 중이고, 같은 상품을 스캔했을 경우
                            scan.work_item_fullbarcode = msg;
                            Log.e("바코드", "" + scan.work_item_fullbarcode);
                            boolean dup = DBHandler.duplicatequeryGoodsWet_check(mContext.getApplicationContext(), scan.work_item_fullbarcode);

                            if(mode.skipsDuplicateCheck()){ //비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
                                dup = false;
                            }

                            if (dup) {
                                Log.e(TAG, "=====================오류지점1=========================");
                                Toast.makeText(mContext.getApplicationContext(), "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.", Toast.LENGTH_SHORT).show();
                                host.vibrate(1000);
                                scan.work_item_fullbarcode = "";
                                scan.work_item_barcodegoods = "";
                                return;
                            } else{
                                Log.e(TAG, "=====================상품스캔일반=========================");
                                host.setScanFlag(false);        // BL스캔 시작
                                scan.work_ppcode = find_ppcode;
                                scan.work_item_fullbarcode = msg;

                                if ((host.getCenterTotalCount() > 0) && (host.getCenterTotalCount() == host.getCenterWorkCount())) {       // 총 계근 완료
                                    host.showWetFinishDialog();
                                }

                                scan.lastBarcodeProcessedTime = 0;   // 의도된 재귀 호출은 디바운스 우회
                                process(msg);
                            }
                        } else if (!scan.work_ppcode.equals(find_ppcode)) {                                   // 작업 중이고, 다른 상품을 스캔했을 경우
                            Log.e(TAG, "=====================작업중다른상품스캔=========================");
                            Log.i(TAG, "작업 중 다른 상품 스캔 !");
                            host.vibrate(500);
                            scan.dialog_flag = true;

                            new AlertDialog.Builder(mContext, R.style.AppCompatDialogStyle)
                                    .setIcon(R.drawable.highland)
                                    .setTitle(R.string.shipment_wet_other)
                                    .setMessage(R.string.shipment_wet_other_msg)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.shipment_wet_yes, (dialog, which) -> {
                                        scan.dialog_flag = false;
                                        scan.work_ppcode = find_ppcode;
                                        scan.work_item_fullbarcode = msg;
                                        host.startShipSelect(host.getSelectedCenterName(), find_ppcode, scan.scan_flag);
                                    })
                                    .setNegativeButton(R.string.shipment_wet_no, (dialog, which) -> {
                                        scan.dialog_flag = false;
                                    })
                                    .show();
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "setBarcodeMsg's 패커상품 스캔 Exception -> " + ex.getMessage().toString());
                }
            } else {//BL스캔
                Log.e(TAG, "========================BL스캔======================" + host.getSelectedBlNo());

                scan.work_item_fullbarcode = msg;

                try {
                    if (true) {
                        String temp_bl_no = host.getSelectedBlNo();
                        for (int i = 0; i < host.getShipments().size(); i++) {
                            if (temp_bl_no.equals(host.getShipments().get(i).getBL_NO()) && !host.getShipments().get(i).getGI_REQ_PKG().equals(String.valueOf(host.getShipments().get(i).getPACKING_QTY()))) {
                                // BL번호 같은 상품 검색 완료
                                scan.current_work_position = i;

                                Log.e(TAG, "========================current_work_position======================" + i);

                                scan.work_bl_no = temp_bl_no;

                                Log.e(TAG, "========================work_bl_no======================" + scan.work_bl_no);

                                break;
                            } else {
                                scan.work_bl_no = "";
                                scan.current_work_position = -1;
                            }
                        }

                        scan.expiryDayTrans = ""; //일단 스캔할 때 마다 초기화

                        Log.e(TAG, "========================TEST TEST======================" + host.getShipments().get(scan.current_work_position).getCENTERNAME()); //센터 선택하고 스캔할떄 여기 탐

                        if (host.getShipments().get(scan.current_work_position).getPACKER_CODE().equals(Common.KILKOY_PACKER_CODE) && host.getShipments().get(scan.current_work_position).getSTORE_CODE().equals(Common.MEAT_CENTER_STORE_CODE)) { //킬코이제품이면서 이마트미트센터나갈때, 미트센터는 지점이 없기 때문에 센터코드와 스토어코드가 같다. 현재 뷰에 센터코드가 없어서 스토어코드로 처리
                            if (scan.work_item_bi_info.getSHELF_LIFE().equals("") || scan.work_item_bi_info.getMAKINGDATE_FROM().equals("") || scan.work_item_bi_info.getMAKINGDATE_TO().equals("")) {
                                Toast.makeText(mContext.getApplicationContext(), "미트센터 납품 - KILKOY 상품의 경우 소비기한정보가 필수로 입력되어야 합니다.\n 현 상품의 계근을 진행할 수 없습니다. 관리자에게 문의하세요.", Toast.LENGTH_LONG).show();
                                host.vibrate(1000);
                                scan.work_ppcode = "";
                                scan.scan_flag = true;
                                return;
                            }
                        } else if (host.getShipments().get(scan.current_work_position).getCENTERNAME().equals("용인TRD") || host.getShipments().get(scan.current_work_position).getCENTERNAME().equals("대구TRD") || host.getShipments().get(scan.current_work_position).getCENTERNAME().equals("시화(W)_TRD") || host.getShipments().get(scan.current_work_position).getCENTERNAME().equals("여주TRD") || host.getShipments().get(scan.current_work_position).getCENTERNAME().substring(0, 3).equals(Common.CENTER_NAME_ET) || host.getShipments().get(scan.current_work_position).getCENTERNAME().contains(Common.CENTER_NAME_ET)  ||  host.getShipments().get(scan.current_work_position).getCENTERNAME().contains(Common.CENTER_NAME_WET)) {
                            if (mode.requiresShelfLifeForTraders()) {
                                if (scan.work_item_bi_info.getSHELF_LIFE().equals("") || scan.work_item_bi_info.getMAKINGDATE_FROM().equals("") || scan.work_item_bi_info.getMAKINGDATE_TO().equals("")) {
                                    Toast.makeText(mContext.getApplicationContext(), "트레이더스 납품 상품의 경우 소비기한정보가 필수로 입력되어야 합니다.\n 현 상품의 계근을 진행할 수 없습니다. 관리자에게 문의하세요.", Toast.LENGTH_LONG).show();
                                    host.vibrate(1000);
                                    scan.work_ppcode = "";
                                    scan.scan_flag = true;
                                    return;
                                }
                            }
                        }

                        if (scan.current_work_position == -1) {
                            Toast.makeText(mContext.getApplicationContext(), "해당하는 BL상품이 없습니다.\nBL번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                            host.vibrate(300);
                            return;
                        } else {
                            host.selectPoint(scan.current_work_position);
                        }

                        host.selectListPosition(scan.current_work_position);      // 현재 계근지점으로 위치 변경

                        if (host.getShipments().get(scan.current_work_position).getGI_REQ_PKG().equals(String.valueOf(host.getShipments().get(scan.current_work_position).getPACKING_QTY()))) {
                            if ((host.getCenterTotalCount() > 0) && (host.getCenterTotalCount() == host.getCenterWorkCount())) {       // 총 계근 완료
                                host.showWetFinishDialog();
                            }
                            return;
                        }

                        Log.e(TAG, "=====================work_item_fullbarcode=========================" + scan.work_item_fullbarcode);
                        Log.e(TAG, "=====================host.getShipments().get(current_work_position).getGI_D_ID()=========================" + host.getShipments().get(scan.current_work_position).getGI_D_ID());
                        Log.e(TAG, "=====================host.getShipments().get(current_work_position).getPACKER_PRODUCT_CODE()=========================" + host.getShipments().get(scan.current_work_position).getPACKER_PRODUCT_CODE());

                        boolean dup = DBHandler.duplicatequeryGoodsWet(mContext.getApplicationContext(), scan.work_item_fullbarcode,
                                host.getShipments().get(scan.current_work_position).getGI_D_ID(), host.getShipments().get(scan.current_work_position).getPACKER_PRODUCT_CODE(), host.getShipments().get(scan.current_work_position).getGI_L_ID());

                        if (mode.skipsDuplicateCheck()) { //비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
                            dup = false;
                        }

                        Log.e(TAG, "=====================체크1=========================" + host.getShipments().get(scan.current_work_position).getPACKER_CODE());
                        Log.e(TAG, "=====================체크2=========================" + host.getShipments().get(scan.current_work_position).getSTORE_CODE());
                        Log.e(TAG, "=====================체크3=========================" + scan.work_item_bi_info.getSHELF_LIFE());

                        if (dup) {
                            Log.e(TAG, "=====================오류지점2=========================");
                            Toast.makeText(mContext.getApplicationContext(), "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.", Toast.LENGTH_SHORT).show();
                            host.vibrate(1000);
                            scan.work_item_fullbarcode = "";
                            scan.work_item_barcodegoods = "";
                            return;
                        }

                        Goodswets_Info gi = new Goodswets_Info();
                        Log.i(TAG, "## 패커상품코드 & BL번호 확인 완료. 계근 시작 ##");
                        Log.i(TAG, "현재 계근할 FULL 바코드                     : " + scan.work_item_fullbarcode);
                        Log.i(TAG, "현재 계근할 바코드의 BarcodeGodos            : " + scan.work_item_barcodegoods);
                        Log.d(TAG, "******************current_work_position:" + host.getShipments().get(scan.current_work_position).getITEM_TYPE());

                        // 중량·제조일·박스시리얼 추출 (개발/77 Step 2) — 중량 위치 없음 알림은 추출 전
                        if (BarcodeParser.isWeightPositionMissing(host.getShipments().get(scan.current_work_position).getITEM_TYPE(), scan.work_item_bi_info)) {
                            host.showAlertDialog("weight", 0);
                            scan.alert_flag = true;
                        }
                        BarcodeParser.Result parsed = BarcodeParser.parse(host.getShipments().get(scan.current_work_position).getITEM_TYPE(), scan.work_item_fullbarcode,
                                scan.work_item_bi_info, host.getShipments().get(scan.current_work_position).getPACKWEIGHT(), mode);

                    host.wetDataInsert(parsed.weightStr, parsed.weightDouble, parsed.makingDate, parsed.boxSerial);
                    } else {
                        Toast.makeText(mContext.getApplicationContext(), "BL번호가 일치하지않습니다.\n확인 후 다시 스캔해주세요.", Toast.LENGTH_SHORT).show();
                        host.vibrate(1000);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "setBarcodeMsg's BL 스캔 Exception -> " + ex.getMessage().toString());
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "setBarcodeMsg Exception -> " + ex.getMessage().toString());
        }
    }
}
