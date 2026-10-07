package com.rgbsolution.highland_emart.shipment.mode;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Barcodes_Info;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;

import java.util.ArrayList;

/**
 * 출하 계근 화면(BixolonShipmentActivity) 마트별 처리 — 롯데 (searchType 6)
 * <p>
 * 마트별 분리 단계: 19개 메서드를 모두 직접 구현한다. 공통 부분은 마지막 단계에서 기본 클래스로 묶는다 (개발/76).
 * </p>
 */
public class LotteMode implements ShipmentMode {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    /** 롯데 박스순번 카운터 (기존 BixolonShipmentActivity.lotte_TryCount) */
    private int lotte_TryCount = 0;

    @Override
    public boolean usesWholesaleLayout() {
        return false;
    }

    @Override
    public boolean requiresPrinterSetup() {
        return true;
    }

    @Override
    public boolean usesProductionBarcodeFlow() {
        return false;
    }

    @Override
    public String formatManualWeight(double weight_double) {
        String temp_weight = Double.toString(weight_double); //생산일 경우 그대로 입력
        Log.i(TAG, "=====================temp_weight production==================" + temp_weight);
        return temp_weight;
    }

    @Override
    public boolean needsExpiryOnManualInput(boolean importCenter) {
        return true;              // 롯데는 수기 입력 시 항상 소비기한 창
    }

    @Override
    public void reprint(LabelPrintHelper helper, String print_weight_str, ArrayList<Shipments_Info> arSM, int select_position,
                        int current_work_position, String making_date, Bundle msgData, Barcodes_Info work_item_bi_info,
                        LabelPrintHelper.PrinterCallback callback) {
        // 롯데의 경우 바코드 시퀀스를 위해 BOX_ORDER 가져옴.
        String box_order = msgData.getString("BOX_ORDER").toString();

        helper.setPrintingLotte(Double.parseDouble(print_weight_str), arSM.get(select_position), true, making_date, box_order, Common.searchType, callback);
    }

    @Override
    public boolean skipsDuplicateCheck() {
        return false;
    }

    @Override
    public boolean requiresShelfLifeForTraders() {
        return false;
    }

    @Override
    public double lbToKgFloor(double temp_weight_double, double item_pow) {
        return Math.floor(temp_weight_double * 100) / 100; //lb 변환 후 소수점 두자리까지 처리하도록 변경
    }

    @Override
    public boolean acceptsAnyBarcodeInfoRow() {
        return false;
    }

    @Override
    public String insertGoodsWet(Context context, Goodswets_Info gi) {
        // 1. 현재 lotte_TryCount 값을 이 계근 건의 박스 순번으로 확정
        String lotteBoxOrder = String.valueOf(lotte_TryCount);
        // 2. 확정된 번호를 사용하여 DB에 저장
        DBHandler.insertqueryGoodsWetLotte(context, gi, lotte_TryCount);
        // 3. DB 저장이 끝난 직후, 다음 계근을 위해 카운터 즉시 증가
        lotte_TryCount++;
        if (lotte_TryCount > Common.LOTTE_BOX_ORDER_MAX) {
            lotte_TryCount = 1;
        }
        return lotteBoxOrder;
    }

    @Override
    public String formatSaveWeight(double weight_double) {
        return Double.toString(weight_double); //생산일 경우 그대로 입력
    }

    @Override
    public double accumulateGiQty(double giQty, double weight_double) {
        double v1 = giQty;
        double v2 = weight_double;

        double v3 = v1+v2;
        double v4 = Math.round(v3*1000)/1000.0;

        Log.e(TAG, "=========================chk prod 계근중량=========================" + v4);
        return v4;
    }

    @Override
    public double roundCenterWorkWeight(double centerWorkWeight) {
        return Math.round(centerWorkWeight*1000)/1000.0; //생산일 경우 소수점 넷째자리에서 반올림
    }

    @Override
    public String centerWeightText(double centerTotalWeight, double centerWorkWeight) {
        return centerTotalWeight + " / " + centerWorkWeight;
    }

    @Override
    public void printOnSave(LabelPrintHelper helper, double weight_double, Shipments_Info current, String making_date,
                            Barcodes_Info work_item_bi_info, String lotteBoxOrder, LabelPrintHelper.PrinterCallback callback) {
        Log.d(TAG, "===========롯데 출력 시작 ================");
        helper.setPrintingLotte(weight_double, current, false, making_date, lotteBoxOrder, Common.searchType, callback);
    }

    @Override
    public void onShipmentListLoaded(ArrayList<Shipments_Info> arSM) {
        // 롯데의 경우만 lotte_TryCount 사용, 초기화 후 현재 찍힌 수량 더해서 전역변수로 만들기.
        Shipments_Info si = arSM.get(0);
        lotte_TryCount = Integer.parseInt(si.LAST_BOX_ORDER) + 1;
        if (lotte_TryCount > Common.LOTTE_BOX_ORDER_MAX) {
            lotte_TryCount = 1;
        }
        Log.e(TAG, "***************************LAST_BOX_ORDER : " +si.getLAST_BOX_ORDER());
        for (int i = 0; i < arSM.size(); i++) {
            lotte_TryCount += arSM.get(i).getPACKING_QTY();
        }
        if (lotte_TryCount > Common.LOTTE_BOX_ORDER_MAX) {
            lotte_TryCount = lotte_TryCount % Common.LOTTE_BOX_ORDER_MAX; //찍힌 수량까지 더했을 때 9999 넘는 경우 1번대로 다시 회귀한 넘버링 적용 (9999로 나눈 나머지)
        }
        Log.d(TAG, "======================== lotte_TryCount ========================="+ lotte_TryCount);
    }

    @Override
    public SendType getSendType() {
        return SendType.PER_ITEM;
    }

    @Override
    public String getSendUrl() {
        return Common.URL_INSERT_GOODS_WET_LOTTE;
    }
}
