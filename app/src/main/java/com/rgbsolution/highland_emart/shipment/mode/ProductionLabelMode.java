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
 * 출하 계근 화면(BixolonShipmentActivity) 마트별 처리 — 생산 라벨 (searchType 7)
 * <p>
 * 마트별 분리 단계: 19개 메서드를 모두 직접 구현한다. 공통 부분은 마지막 단계에서 기본 클래스로 묶는다 (개발/76).
 * </p>
 */
public class ProductionLabelMode implements ShipmentMode {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

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
        return false;
    }

    @Override
    public void reprint(LabelPrintHelper helper, String print_weight_str, ArrayList<Shipments_Info> arSM, int select_position,
                        int current_work_position, String making_date, Bundle msgData, Barcodes_Info work_item_bi_info,
                        LabelPrintHelper.PrinterCallback callback) {
        helper.setPrinting_prod(Double.parseDouble(print_weight_str), arSM.get(select_position), true, callback);
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
        DBHandler.insertqueryGoodsWet(context, gi);
        return "";
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
        Log.d(TAG, "===========생산 출력 시작 ================");
        helper.setPrinting_prod(weight_double, current, false, callback);
    }

    @Override
    public void onShipmentListLoaded(ArrayList<Shipments_Info> arSM) {
        // 목록 로드 후 처리 없음
    }

    @Override
    public SendType getSendType() {
        return SendType.BATCH;
    }

    @Override
    public String getSendUrl() {
        return Common.URL_INSERT_GOODS_WET_PRODUCTION;
    }
}
