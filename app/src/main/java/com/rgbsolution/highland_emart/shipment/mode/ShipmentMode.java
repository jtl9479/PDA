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
 * 출하 계근 화면(BixolonShipmentActivity)의 마트(searchType)별 판단·계산·동작 (개발/76)
 * <p>
 * Activity 는 화면·공통 흐름만 담당하고, 마트별로 다른 부분은 이 인터페이스 구현 클래스가 결정한다.
 * Mode 는 화면 위젯·Toast 에 접근하지 않는다.
 * </p>
 */
public interface ShipmentMode {

    /** 전송 방식 : 건별 / 일괄 / 없음 */
    enum SendType { PER_ITEM, BATCH, NONE }

    /** 도매 전용 레이아웃 사용 여부 */
    boolean usesWholesaleLayout();

    /** 프린터 사용 여부 (false : 인쇄 스위치 해제, 블루투스·프린터 연결 확인 생략) */
    boolean requiresPrinterSetup();

    /** 생산 전용 바코드 처리(setBarcodeMsgProduction) 사용 여부 */
    boolean usesProductionBarcodeFlow();

    /** 수기 입력 중량 문자열 */
    String formatManualWeight(double weight_double);

    /** 수기 입력 시 소비기한 입력창 표시 여부 (킬코이·미트센터 조건은 Activity 에서 먼저 판단) */
    boolean needsExpiryOnManualInput(boolean importCenter);

    /** 계근 내역 재출력 */
    void reprint(LabelPrintHelper helper, String print_weight_str, ArrayList<Shipments_Info> arSM, int select_position,
                        int current_work_position, String making_date, Bundle msgData, Barcodes_Info work_item_bi_info,
                        LabelPrintHelper.PrinterCallback callback);

    /** 바코드 중복 확인 제외 여부 */
    boolean skipsDuplicateCheck();

    /** 트레이더스 납품 상품 소비기한 정보 필수 여부 */
    boolean requiresShelfLifeForTraders();

    /** LB → KG 환산값 절사 */
    double lbToKgFloor(double temp_weight_double, double item_pow);

    /** 바코드 정보 조회 결과를 일치 여부와 무관하게 채택할지 (상품 찾기) */
    boolean acceptsAnyBarcodeInfoRow();

    /** 계근 1건 로컬 저장, 반환 : 롯데 박스순번 (그 외 "") */
    String insertGoodsWet(Context context, Goodswets_Info gi);

    /** 저장 중량 문자열 */
    String formatSaveWeight(double weight_double);

    /** 출하대상 계근중량 누적값 */
    double accumulateGiQty(double giQty, double weight_double);

    /** 센터 계근중량 반올림 */
    double roundCenterWorkWeight(double centerWorkWeight);

    /** 센터 총중량 표시 문자열 */
    String centerWeightText(double centerTotalWeight, double centerWorkWeight);

    /** 계근 저장 직후 라벨 출력 (Common.print_bool 확인은 Activity) */
    void printOnSave(LabelPrintHelper helper, double weight_double, Shipments_Info current, String making_date,
                            Barcodes_Info work_item_bi_info, String lotteBoxOrder, LabelPrintHelper.PrinterCallback callback);

    /** 출하대상 목록 로드 후 처리 (백그라운드 스레드에서 호출 — 화면 접근 금지) */
    void onShipmentListLoaded(ArrayList<Shipments_Info> arSM);

    /** 서버 전송 방식 */
    SendType getSendType();

    /** 서버 전송 JSP URL */
    String getSendUrl();
}
