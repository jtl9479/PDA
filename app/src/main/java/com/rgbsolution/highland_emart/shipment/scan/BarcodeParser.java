package com.rgbsolution.highland_emart.shipment.scan;

import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Barcodes_Info;
import com.rgbsolution.highland_emart.shipment.mode.ShipmentMode;

/**
 * BL 스캔 바코드 → 중량·제조일·박스시리얼 추출 (개발/77 Step 2)
 * BixolonShipmentActivity.setBarcodeMsg 의 추출 블록을 그대로 이동 — 계산·로그·예외 동일
 * 중량 위치 없음 알림은 추출 전에 떠야 하므로 isWeightPositionMissing 으로 분리해 Activity 가 먼저 호출한다
 */
public class BarcodeParser {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    public static class Result {
        public String weightStr = "";       // 상품 String 중량값
        public double weightDouble = 0.0;   // 상품 Double 중량값
        public String makingDate = "";      // 상품 제조일
        public String boxSerial = "";       // 상품 박스시리얼
    }

    /** 중량 위치 정보가 0 인지 (W/HW/S/B 만 검사, 그 외 타입은 false) */
    public static boolean isWeightPositionMissing(String itemType, Barcodes_Info bi) {
        if (itemType.equals(Common.ITEM_TYPE_W) || itemType.equals(Common.ITEM_TYPE_HW)
                || itemType.equals(Common.ITEM_TYPE_S) || itemType.equals(Common.ITEM_TYPE_B)) {
            String weight_from = bi.getWEIGHT_FROM();
            String weight_to = bi.getWEIGHT_TO();

            Log.d(TAG, "weightfrom,to:" + weight_from + ":" + weight_to + ":");
            return weight_from.equals("0") || weight_to.equals("0");
        }
        return false;
    }

    /** 예외(NumberFormat·StringIndexOutOfBounds·NPE)는 호출부로 그대로 전파 */
    public static Result parse(String itemType, String fullBarcode, Barcodes_Info bi, String packWeight, ShipmentMode mode) {
        Result r = new Result();
        String item_weight = "";            // 상품 최초 중량 절사값(XXXX)
        double item_pow = 0;              // 상품 zeroPoint에 대한 pow

        if (itemType.equals(Common.ITEM_TYPE_W) || itemType.equals(Common.ITEM_TYPE_HW)) {
            // 이마트 ITEM_TYPE W (바코드 계근)
            item_weight = fullBarcode.substring(
                    Integer.parseInt(bi.getWEIGHT_FROM()) - 1, Integer.parseInt(bi.getWEIGHT_TO()));
            Log.i(TAG, "Type W | 절사한 중량값 : " + item_weight);

            item_pow = Math.pow(10, Integer.parseInt(bi.getZEROPOINT()));
            Log.i(TAG, "Type W | item_pow 확인 : " + item_pow);
            Log.i(TAG, "Type W | zero point 확인 : " + bi.getZEROPOINT());

            r.weightDouble = Double.parseDouble(item_weight) / item_pow;

            Log.i(TAG, "Type W | item_weight 확인 : " + Double.parseDouble(item_weight));
            Log.i(TAG, "Type W | item_weight_double 확인 : " + r.weightDouble);

            if ("LB".equals(bi.getBASEUNIT())) {
                // LB(파운드)라면 KG으로 환산 LB * 0.453592 = KG
                double temp_weight_double = r.weightDouble * 0.453592;
                r.weightDouble = Math.floor(temp_weight_double * item_pow) / item_pow;
                r.weightStr = String.valueOf(r.weightDouble);
                Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + r.weightDouble);
                Log.i(TAG, "LB->KG | 환산 중량 String값 : " + r.weightStr);
            }

            r.weightDouble = Math.floor(r.weightDouble * 10);
            r.weightDouble = r.weightDouble / 10.0;

            String temp_weight = String.format("%.1f", r.weightDouble);
            r.weightDouble = Double.parseDouble(temp_weight);
            r.weightStr = String.valueOf(r.weightDouble);
            Log.i(TAG, "Type W | ZeroPoint 적용 중량 Double값 : " + r.weightDouble);
            Log.i(TAG, "Type W | ZeroPoint 적용 중량 String값 : " + r.weightStr);

            if (bi.getMAKINGDATE_FROM() != "" && bi.getMAKINGDATE_TO() != "") {
                r.makingDate = fullBarcode.substring(
                        Integer.parseInt(bi.getMAKINGDATE_FROM()) - 1, Integer.parseInt(bi.getMAKINGDATE_TO()));
                Log.i(TAG, "Type W | 절사한 제조일 : " + r.makingDate);
            }

            if (bi.getBOXSERIAL_FROM() != "" && bi.getBOXSERIAL_TO() != "") {
                r.boxSerial = fullBarcode.substring(
                        Integer.parseInt(bi.getBOXSERIAL_FROM()) - 1, Integer.parseInt(bi.getBOXSERIAL_TO()));
                Log.i(TAG, "Type W | 절사한 박스시리얼 : " + r.boxSerial);
            }
        } else if (itemType.equals(Common.ITEM_TYPE_S)) {
            // 이마트 ITEM_TYPE W (바코드 계근)
            item_weight = fullBarcode.substring(
                    Integer.parseInt(bi.getWEIGHT_FROM()) - 1, Integer.parseInt(bi.getWEIGHT_TO()));
            Log.i(TAG, "Type S | 절사한 중량값 : " + item_weight);

            item_pow = Math.pow(10, Integer.parseInt(bi.getZEROPOINT()));
            r.weightDouble = Double.parseDouble(item_weight) / item_pow;

            if ("LB".equals(bi.getBASEUNIT())) {
                Log.i(TAG, "LB로 들어옴, 환산");
                Log.i(TAG, "LB 원 중량 : " + r.weightDouble);
                // LB(파운드)라면 KG으로 환산 LB * 0.453592 = KG
                double temp_weight_double = r.weightDouble * 0.453592;

                r.weightDouble = mode.lbToKgFloor(temp_weight_double, item_pow);   // 이마트 vs 그 외 절사 자릿수 (개발/76)
                r.weightStr = String.valueOf(r.weightDouble);
                Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + r.weightDouble);
                Log.i(TAG, "LB->KG | 환산 중량 String값 : " + r.weightStr);
            }

            String temp_weight = String.format("%.2f", r.weightDouble);
            r.weightDouble = Double.parseDouble(temp_weight);
            r.weightStr = String.valueOf(r.weightDouble);
            Log.i(TAG, "Type S | ZeroPoint 적용 중량 Double값 : " + r.weightDouble);
            Log.i(TAG, "Type S | ZeroPoint 적용 중량 String값 : " + r.weightStr);

            if (bi.getMAKINGDATE_FROM() != "" && bi.getMAKINGDATE_TO() != "") {
                r.makingDate = fullBarcode.substring(
                        Integer.parseInt(bi.getMAKINGDATE_FROM()) - 1, Integer.parseInt(bi.getMAKINGDATE_TO()));
                Log.i(TAG, "Type S | 절사한 제조일 : " + r.makingDate);
            }

            if (bi.getBOXSERIAL_FROM() != "" && bi.getBOXSERIAL_TO() != "") {
                r.boxSerial = fullBarcode.substring(
                        Integer.parseInt(bi.getBOXSERIAL_FROM()) - 1, Integer.parseInt(bi.getBOXSERIAL_TO()));
                Log.i(TAG, "Type S | 절사한 박스시리얼 : " + r.boxSerial);
            }
        } else if (itemType.equals(Common.ITEM_TYPE_J)) {
            // 이마트 ITEM_TYPE J (지정된 중량 입력) | 바코드에서 중량, 제조일, 박스시리얼 X
            item_weight = packWeight;
            Log.i(TAG, "Type J | 지정된 중량값 : " + item_weight);
            r.weightDouble = Double.parseDouble(item_weight);
            r.weightStr = String.valueOf(r.weightDouble);
            Log.i(TAG, "Type J | ZeroPoint 적용 중량 Double값 : " + r.weightDouble);
            Log.i(TAG, "Type J | ZeroPoint 적용 중량 String값 : " + r.weightStr);
        }

        // Homeplus 비정량 "B"
        if (itemType.equals(Common.ITEM_TYPE_B)) {
            // 이마트 ITEM_TYPE W (바코드 계근)
            item_weight = fullBarcode.substring(
                    Integer.parseInt(bi.getWEIGHT_FROM()) - 1, Integer.parseInt(bi.getWEIGHT_TO()));
            Log.i(TAG, "Type S | 절사한 중량값 : " + item_weight);

            item_pow = Math.pow(10, Integer.parseInt(bi.getZEROPOINT()));
            r.weightDouble = Double.parseDouble(item_weight) / item_pow;

            if ("LB".equals(bi.getBASEUNIT())) {
                Log.i(TAG, "LB로 들어옴, 환산");
                Log.i(TAG, "LB 원 중량 : " + r.weightDouble);
                // LB(파운드)라면 KG으로 환산 LB * 0.453592 = KG
                double temp_weight_double = r.weightDouble * 0.453592;

                r.weightDouble = mode.lbToKgFloor(temp_weight_double, item_pow);   // 이마트 vs 그 외 절사 자릿수 (개발/76)
                r.weightStr = String.valueOf(r.weightDouble);
                Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + r.weightDouble);
                Log.i(TAG, "LB->KG | 환산 중량 String값 : " + r.weightStr);
            }

            String temp_weight = String.format("%.2f", r.weightDouble);
            r.weightDouble = Double.parseDouble(temp_weight);
            r.weightStr = String.valueOf(r.weightDouble);
            Log.i(TAG, "Type S | ZeroPoint 적용 중량 Double값 : " + r.weightDouble);
            Log.i(TAG, "Type S | ZeroPoint 적용 중량 String값 : " + r.weightStr);

            if (bi.getMAKINGDATE_FROM() != "" && bi.getMAKINGDATE_TO() != "") {
                r.makingDate = fullBarcode.substring(
                        Integer.parseInt(bi.getMAKINGDATE_FROM()) - 1, Integer.parseInt(bi.getMAKINGDATE_TO()));
                Log.i(TAG, "Type S | 절사한 제조일 : " + r.makingDate);
            }

            if (bi.getBOXSERIAL_FROM() != "" && bi.getBOXSERIAL_TO() != "") {
                r.boxSerial = fullBarcode.substring(
                        Integer.parseInt(bi.getBOXSERIAL_FROM()) - 1, Integer.parseInt(bi.getBOXSERIAL_TO()));
                Log.i(TAG, "Type S | 절사한 박스시리얼 : " + r.boxSerial);
            }
        }

        return r;
    }
}
