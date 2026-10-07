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
 * 출하 계근 화면(BixolonShipmentActivity) 마트별 처리 — 이마트 정량 (searchType 0)
 * <p>
 * 마트별 분리 단계: 19개 메서드를 모두 직접 구현한다. 공통 부분은 마지막 단계에서 기본 클래스로 묶는다 (개발/76).
 * </p>
 */
public class EmartMode implements ShipmentMode {

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
        weight_double = Math.floor(weight_double * 10);
        Log.i(TAG, "=====================weight_double 1-1==================" + weight_double);
        weight_double = weight_double / 10.0;
        Log.i(TAG, "=====================weight_double 1-2==================" + weight_double);
        return String.format("%.1f", weight_double); //출하일 경우 소숫점 첫째 자리까지 반올림, 위 단계에서 Math.floor로 소숫점 둘째 자리부터 날려서 의미는 없는 코드이나 일단 남겨놓음
    }

    @Override
    public boolean needsExpiryOnManualInput(boolean importCenter) {
        return importCenter;      // 수입육 센터(TRD/WET/ET) 수기 입력 시 소비기한 창
    }

    @Override
    public void reprint(LabelPrintHelper helper, String print_weight_str, ArrayList<Shipments_Info> arSM, int select_position,
                        int current_work_position, String making_date, Bundle msgData, Barcodes_Info work_item_bi_info,
                        LabelPrintHelper.PrinterCallback callback) {
        helper.setPrinting(Double.parseDouble(print_weight_str), arSM.get(select_position), true, making_date, work_item_bi_info, arSM.get(current_work_position), Common.searchType, callback); //이마트수기프린팅
    }

    @Override
    public boolean skipsDuplicateCheck() {
        return false;
    }

    @Override
    public boolean requiresShelfLifeForTraders() {
        return true;
    }

    @Override
    public double lbToKgFloor(double temp_weight_double, double item_pow) {
        return Math.floor(temp_weight_double * item_pow) / item_pow;
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
        weight_double = Math.floor(weight_double * 10);
        weight_double = weight_double / 10.0;
        return String.format("%.1f", weight_double);
    }

    @Override
    public double accumulateGiQty(double giQty, double weight_double) {
        return Math.round((giQty + weight_double) * 10.0) / 10.0;
    }

    @Override
    public double roundCenterWorkWeight(double centerWorkWeight) {
        return Math.round(centerWorkWeight * 100.0) / 100.0; //출하일 경우에만 round 처리
    }

    @Override
    public String centerWeightText(double centerTotalWeight, double centerWorkWeight) {
        return Math.round(centerTotalWeight * 10) / 10.0 + " / " + centerWorkWeight;
    }

    @Override
    public void printOnSave(LabelPrintHelper helper, double weight_double, Shipments_Info current, String making_date,
                            Barcodes_Info work_item_bi_info, String lotteBoxOrder, LabelPrintHelper.PrinterCallback callback) {
        Log.d(TAG, "===========이마트 출력 시작 ================");
        helper.setPrinting(weight_double, current, false, making_date, work_item_bi_info, current, Common.searchType, callback);
    }

    @Override
    public void onShipmentListLoaded(ArrayList<Shipments_Info> arSM) {
        // 목록 로드 후 처리 없음
    }

    @Override
    public SendType getSendType() {
        return SendType.PER_ITEM;
    }

    @Override
    public String getSendUrl() {
        return Common.URL_INSERT_GOODS_WET;
    }
}
