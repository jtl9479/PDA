package com.rgbsolution.highland_emart.shipment.scan;

import android.content.Context;
import android.util.Log;

import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Barcodes_Info;

import java.util.ArrayList;

/**
 * 바코드 정보(TB_BARCODE_INFO) 조회·매칭 (개발/77 Step 3)
 * BixolonShipmentActivity.find_work_info / find_work_info_barcodeGoods 의 try 본문을 그대로 이동.
 * 위젯·필드 반영은 원래 루프 안에서 매번 하던 그대로 Sink 로 즉시 반영하고, 예외 처리(catch)는 Activity 에 남긴다.
 */
public class BarcodeInfoFinder {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    /** Activity 반영 (work_item_bi_info, edit_product_name/code, work_item_barcodegoods) */
    public interface Sink {
        void setBarcodeInfo(Barcodes_Info bi);
        void setProduct(String name, String code);
        void setBarcodeGoods(String barcodeGoods);
    }

    /** find_work_info 본문. acceptsAnyRow = mode.acceptsAnyBarcodeInfoRow() (이마트 비정량) */
    public static String byPackerProduct(Context ctx, String req, boolean type, boolean acceptsAnyRow, Sink sink) {
        String pp_code = "";
        int count = 0;
        ArrayList<Barcodes_Info> list_barcode_info = DBHandler.selectqueryBarcodeInfo(ctx);
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
                sink.setBarcodeInfo(bi);
                sink.setProduct(bi.getITEM_NAME_KR(), bi.getPACKER_PRODUCT_CODE());
                if(count == 0){
                    pp_code = bi.getPACKER_PRODUCT_CODE();
                }else{
                    pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();
                }
                Log.i(TAG, "===================pp_code=================" + pp_code);
                sink.setBarcodeGoods(bg);
                count++;
            } else {
                sink.setProduct("", "");
                sink.setBarcodeGoods("");
            }

            if(acceptsAnyRow){   // 이마트 비정량
                sink.setBarcodeInfo(bi);
                sink.setProduct(bi.getITEM_NAME_KR(), bi.getPACKER_PRODUCT_CODE());
                if(count == 0){
                    pp_code = bi.getPACKER_PRODUCT_CODE();
                }else{
                    pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();
                }
                Log.i(TAG, "===================pp_code=================" + pp_code);
                sink.setBarcodeGoods(bg);
                count++;
            }
        }

        Log.i(TAG, "===================return pp_code test!!! =================" + pp_code);
        return pp_code;
    }

    /** find_work_info_barcodeGoods 본문 */
    public static String byBarcodeGoods(Context ctx, String req, boolean type, Sink sink) {
        String pp_code = "";
        int count = 0;
        ArrayList<Barcodes_Info> list_barcode_info = DBHandler.selectqueryBarcodeGoodsInfo(ctx);
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
                sink.setBarcodeInfo(bi);
                sink.setProduct(bi.getITEM_NAME_KR(), bi.getPACKER_PRODUCT_CODE());
                if(count == 0){
                    pp_code = bi.getPACKER_PRODUCT_CODE();
                }else{
                    pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();
                }
                Log.i(TAG, "===================pp_code=================" + pp_code);
                sink.setBarcodeGoods(bg);
                count++;
            } else {
                sink.setProduct("", "");
                sink.setBarcodeGoods("");
            }
        }
        return pp_code;
    }
}
