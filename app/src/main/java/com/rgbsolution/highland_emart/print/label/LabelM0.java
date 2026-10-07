package com.rgbsolution.highland_emart.print.label;

import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;

import java.io.ByteArrayOutputStream;

/**
 * 이마트 라벨 — M0 : 이마트 정량 라벨 (미트센터 9231 납품 시 2번째 라벨 추가)
 * LabelPrintHelper.setPrinting 에서 바코드 타입으로 선택되어 호출된다 (개발/75).
 */
public class LabelM0 implements EmartLabel {

    private static final String TAG = "LabelPrintHelper";   // 기존 로그 태그 유지

    private final LabelPrintHelper helper;

    public LabelM0(LabelPrintHelper helper) {
        this.helper = helper;
    }

    @Override
    public void print(Shipments_Info si, boolean reprint, String print_weight_str, Double print_weight_double, String pointName, String expiryDayConvert, LabelPrintHelper.PrinterCallback callback) {
        String pCompCode = LabelPrintHelper.COMPANY_CODE;
        String pCompName = LabelPrintHelper.COMPANY_NAME;
        String sBarcode = si.getSTORE_CODE();
        String sBarcodeStr = si.getSTORE_CODE();
        String whArea = "";

        // 바코드 조립 (M0) : 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
        if (Common.D) {
            Log.e(TAG, "::::::::: M0 ::::::::");
            Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
            Log.d(TAG, "중량 6자리 :" + print_weight_str);
            Log.d(TAG, "회사코드 : " + pCompCode);
            Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
        }

        String pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
        String pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
        String pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();

        if (Common.D) {
            Log.d(TAG, "print Barcode : " + pBarcode.toString());
            Log.d(TAG, "print Weight : " + print_weight_str);
        }

        // ========== SLCS 명령어로 이마트 라벨 인쇄 (Bixolon 프린터) ==========
        try {
            ByteArrayOutputStream labelData = new ByteArrayOutputStream();
            labelData.write(helper.slcsInit().getBytes("EUC-KR"));                          // 프린터 초기화
            labelData.write(helper.slcsLabelSize(576, 460).getBytes("EUC-KR"));             // 라벨 크기 설정

            // 센터명 출력
            if (7 < si.CENTERNAME.length()) {
                labelData.write(helper.slcsBitmapText(20, 12, 35, si.CENTERNAME, true));
                if (Common.D)
                    Log.i(TAG, "센터명 > 7 ,  size 30");
            } else {
                labelData.write(helper.slcsBitmapText(20, 10, 40, si.CENTERNAME, true));
                if (Common.D)
                    Log.i(TAG, "센터명 <= 7 ,  size 40");
            }

            // 업체명/지점명 출력
            if (11 < si.CLIENTNAME.toString().length()) {
                labelData.write(helper.slcsBitmapText(20, 60, 35, pointName.toString(), true));          // 지점명 출력
                if (Common.D)
                    Log.i(TAG, "지점명 > 11 ,  size 30");
            } else {
                labelData.write(helper.slcsBitmapText(20, 60, 40, pointName.toString(), true));          // 지점명 출력
                if (Common.D)
                    Log.i(TAG, "지점명 <= 11 ,  size 40");
            }

            // 상품명 출력 (위치, 크기)
            int itemX = 80, itemY = 120;  // 기본 위치
            if (si.EMARTITEM.length() > 14) {
                labelData.write(helper.slcsBitmapText(itemX, itemY, 35, si.EMARTITEM, true));
            } else {
                labelData.write(helper.slcsBitmapText(itemX, itemY, 40, si.EMARTITEM, true));
            }

            Log.i(TAG, "===============EMARTITEM============" + si.EMARTITEM);
            Log.i(TAG, "===============sBarcode============" + sBarcode);

            // sBarcode 바코드 출력
            labelData.write(helper.slcsBarcode(420, 20, 60, sBarcode).getBytes("EUC-KR"));

            Log.i(TAG, "===============sBarcode2============" + sBarcodeStr);

            // sBarcodeStr 텍스트 출력
            labelData.write(helper.slcsBitmapText(450, 80, 25, sBarcodeStr, true));      // 바코드번호(숫자) 출력

            Log.i(TAG, "===============pBarcode============" + pBarcode);
            Log.i(TAG, "===============pBarcode2============" + pBarcode2);

            // 메인 바코드 (80, 170)
            labelData.write(helper.slcsBarcode(80, 170, 60, pBarcode).getBytes("EUC-KR"));

            // 바코드번호(숫자) 출력
            labelData.write(helper.slcsBitmapText(75, 240, 20, pBarcodeStr, true));

            // 중량, 납품일자, 업체 정보 출력 (tempDate 가 아래 미트센터 블록과 겹치지 않도록 블록 유지)
            {
                labelData.write(helper.slcsBitmapText(20, 280, 40, "중량 : ", true));
                labelData.write(helper.slcsBitmapText(180, 280, 40, String.valueOf(print_weight_double) + " KG", true));
                Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
                String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
                labelData.write(helper.slcsBitmapText(20, 328, 30, "납품일자 : " + tempDate, true));
                if (reprint) {
                    pCompName = pCompName + "  *";
                }
                labelData.write(helper.slcsBitmapText(20, 368, 30, "업체코드 : " + pCompCode + expiryDayConvert, true));
                labelData.write(helper.slcsBitmapText(20, 408, 30, "업 체 명 : " + pCompName, true));
            }

            // WH_AREA 출력
            whArea = si.getWH_AREA();
            Log.e(TAG, "::::::::: whArea check44 ::::::::"+whArea);
            if(whArea != null || !whArea.equals("")){
                labelData.write(helper.slcsBitmapText(430, 385, 65, whArea, true));
            }

            // 인쇄 실행
            labelData.write(helper.slcsPrint(1).getBytes("EUC-KR"));
            // 라벨 피드 (마크 위치로 이동)
            labelData.write(helper.slcsFeedToMark().getBytes("EUC-KR"));

            callback.sendData(labelData.toByteArray());

            // ========== 이마트 미트센터 +공장코드 라벨 (SLCS) ==========
            if (si.getBARCODE_TYPE().equals(LabelPrintHelper.BARCODE_TYPE_M0) && si.getSTORE_CODE().equals(LabelPrintHelper.MEAT_CENTER_STORE_CODE) && si.getEMARTLOGIS_CODE().equals(LabelPrintHelper.LOGIS_CODE_DEFAULT) && !si.getEMART_PLANT_CODE().equals("")) {
                printEmartMeatCenterPlantLabel(si, print_weight_str, print_weight_double, expiryDayConvert, pCompName, callback);
            }

            // ========== 이마트 미트센터 라벨 (SLCS) ==========
            if (si.getBARCODE_TYPE().equals(LabelPrintHelper.BARCODE_TYPE_M0) && si.getSTORE_CODE().equals(LabelPrintHelper.MEAT_CENTER_STORE_CODE) && !si.getEMARTLOGIS_CODE().equals(LabelPrintHelper.LOGIS_CODE_DEFAULT) && si.getEMART_PLANT_CODE().equals("")) {
                printEmartMeatCenterLabel(si, print_weight_str, print_weight_double, expiryDayConvert, pCompName, callback);
            }
            callback.clearBarcodeInput();
        } catch (Exception e) {
            e.printStackTrace();
            if (Common.D) {
                Log.d(TAG, "setPrinting Exception\n" + e.getMessage());
            }
        }
    }

    /**
     * 이마트 미트센터 +공장코드 라벨 (2번째 라벨)
     * 진입 조건(setPrinting): M0 && 점포코드 9231 && 물류코드 "0000000" && 공장코드 있음
     *
     * @param pCompName 재출력 시 "  *" 가 붙은 업체명
     */
    private void printEmartMeatCenterPlantLabel(Shipments_Info si, String print_weight_str, double print_weight_double, String expiryDayConvert, String pCompName, LabelPrintHelper.PrinterCallback callback) {
        String meatCenterBarcode = "";
        String whArea = "";
        System.out.println(">>>>>>>>>>>>>>> 이마트 미트센터 +공장코드 >>>>>>>>>>>>>>>");

        try {
            StringBuilder slcsMeat = new StringBuilder();
            ByteArrayOutputStream slcsMeatText = new ByteArrayOutputStream(); // 글자 비트맵(Korail.ttf)
            slcsMeat.append(helper.slcsInit());
            slcsMeat.append(helper.slcsLabelSize(576, 460));

            String meatCenterTitle = "ERP-미트센터출하코드";
            String meatCenterCode = LabelPrintHelper.MEAT_CENTER_CODE;
            String meatCenterBarcodeStr = "";

            slcsMeatText.write(helper.slcsBitmapText(120, 35, 40, meatCenterTitle, true));

            if (si.EMARTITEM.length() > 14) {
                slcsMeatText.write(helper.slcsBitmapText(115, 120, 35, si.EMARTITEM, true));
            } else {
                slcsMeatText.write(helper.slcsBitmapText(115, 120, 40, si.EMARTITEM, true));
            }

            meatCenterBarcode = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + meatCenterCode + si.getIMPORT_ID_NO() + si.getEMART_PLANT_CODE();
            meatCenterBarcodeStr = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + meatCenterCode + " " + si.getIMPORT_ID_NO() + " " + si.getEMART_PLANT_CODE();

            Log.i(TAG, "===============MEATCENTERBARCODE128============" + meatCenterBarcode);

            slcsMeat.append(helper.slcsBarcode(35, 170, 60, meatCenterBarcode));
            slcsMeatText.write(helper.slcsBitmapText(40, 240, 25, meatCenterBarcodeStr, true));

            slcsMeatText.write(helper.slcsBitmapText(15, 280, 40, "중      량 : ", true));
            slcsMeatText.write(helper.slcsBitmapText(175, 280, 40, String.valueOf(print_weight_double) + " KG", true));

            Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
            String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
            slcsMeatText.write(helper.slcsBitmapText(15, 328, 30, "납품일자 : " + tempDate, true));

            slcsMeatText.write(helper.slcsBitmapText(15, 368, 30, "업체코드 : " + meatCenterCode + expiryDayConvert, true));
            slcsMeatText.write(helper.slcsBitmapText(15, 408, 30, "업 체 명 : " + pCompName, true));

            whArea = si.getWH_AREA();
            Log.e(TAG, "::::::::: whArea check44 ::::::::"+whArea);
            if(whArea != null || !whArea.equals("")){
                slcsMeatText.write(helper.slcsBitmapText(430, 385, 65, whArea, true));
            }

            slcsMeat.append(helper.slcsPrint(1));
            // 라벨 피드 (마크 위치로 이동)
            slcsMeat.append(helper.slcsFeedToMark());
            callback.sendData(helper.withBitmapText(slcsMeat, slcsMeatText));
        } catch (Exception e) {
            Log.d(TAG, "이마트 공장코드 출력 오류 " +  e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 이마트 미트센터 라벨 (2번째 라벨)
     * 진입 조건(setPrinting): M0 && 점포코드 9231 && 물류코드 "0000000" 아님 && 공장코드 없음
     *
     * @param pCompName 재출력 시 "  *" 가 붙은 업체명
     */
    private void printEmartMeatCenterLabel(Shipments_Info si, String print_weight_str, double print_weight_double, String expiryDayConvert, String pCompName, LabelPrintHelper.PrinterCallback callback) {
        String meatCenterBarcode = "";
        String whArea = "";
        try {
            StringBuilder slcsMeat2 = new StringBuilder();
            ByteArrayOutputStream slcsMeat2Text = new ByteArrayOutputStream(); // 글자 비트맵(Korail.ttf)
            slcsMeat2.append(helper.slcsInit());
            slcsMeat2.append(helper.slcsLabelSize(576, 460));

            String meatCenterTitle = "미트센터출하코드";
            String meatCenterCode = LabelPrintHelper.MEAT_CENTER_CODE;
            String meatCenterBarcodeStr = "";

            slcsMeat2Text.write(helper.slcsBitmapText(150, 35, 40, meatCenterTitle, true));

            if (si.EMARTITEM.length() > 14) {
                slcsMeat2Text.write(helper.slcsBitmapText(80, 120, 35, si.EMARTITEM, true));
            } else {
                slcsMeat2Text.write(helper.slcsBitmapText(80, 120, 40, si.EMARTITEM, true));
            }

            meatCenterBarcode = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + meatCenterCode + si.getIMPORT_ID_NO();
            meatCenterBarcodeStr = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + meatCenterCode + " " + si.getIMPORT_ID_NO();

            Log.i(TAG, "===============MEATCENTERBARCODE128============" + meatCenterBarcode);

            slcsMeat2.append(helper.slcsBarcode(80, 170, 60, meatCenterBarcode));
            slcsMeat2Text.write(helper.slcsBitmapText(75, 240, 25, meatCenterBarcodeStr, true));

            slcsMeat2Text.write(helper.slcsBitmapText(15, 280, 40, "중      량 : ", true));
            slcsMeat2Text.write(helper.slcsBitmapText(175, 280, 40, String.valueOf(print_weight_double) + " KG", true));

            Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
            String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
            slcsMeat2Text.write(helper.slcsBitmapText(15, 328, 30, "납품일자 : " + tempDate, true));

            slcsMeat2Text.write(helper.slcsBitmapText(15, 368, 30, "업체코드 : " + meatCenterCode + expiryDayConvert, true));
            slcsMeat2Text.write(helper.slcsBitmapText(15, 408, 30, "업 체 명 : " + pCompName, true));

            whArea = si.getWH_AREA();
            Log.e(TAG, "::::::::: whArea check44 ::::::::"+whArea);
            if(whArea != null || !whArea.equals("")){
                slcsMeat2Text.write(helper.slcsBitmapText(430, 385, 65, whArea, true));
            }

            slcsMeat2.append(helper.slcsPrint(1));
            // 라벨 피드 (마크 위치로 이동)
            slcsMeat2.append(helper.slcsFeedToMark());
            callback.sendData(helper.withBitmapText(slcsMeat2, slcsMeat2Text));
        } catch (Exception e) {
            Log.d(TAG, "이마트 미트센터 출력 오류 " +  e.getMessage());
            e.printStackTrace();
        }
    }
}
