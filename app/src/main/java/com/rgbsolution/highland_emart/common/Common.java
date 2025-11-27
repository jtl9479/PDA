package com.rgbsolution.highland_emart.common;

import android.util.Log;

import java.util.ArrayList;

public class Common {
    private static final String TAG = "Common";
    public static boolean D = true;        // Debug ? true : false

    // 베이스 URL (서버 변경 시 여기만 수정)
    // public static final String BASE_URL = "http://175.120.155.125:4040/inno";  // 운영서버
    public static final String BASE_URL = "http://49.50.173.44:4040/inno";  // 개발서버


    // URL 변수
    public static final String URL_VERSION = BASE_URL + "/check_version.jsp";
    public static final String URL_LOGIN = BASE_URL + "/manager_login.jsp";
    public static final String URL_SEARCH_SHIPMENT = BASE_URL + "/search_shipment.jsp";
    public static final String URL_SEARCH_SHIPMENT_HOMEPLUS = BASE_URL + "/search_shipment_homeplus.jsp";
    public static final String URL_SEARCH_SHIPMENT_WHOLESALE = BASE_URL + "/search_shipment_wholesale.jsp";
    public static final String URL_SEARCH_SHIPMENT_LOTTE = BASE_URL + "/search_shipment_lotte.jsp";
    public static final String URL_SEARCH_PRODUCTION = BASE_URL + "/search_production.jsp";
    public static final String URL_SEARCH_PRODUCTION_4LABEL = BASE_URL + "/search_production_4label.jsp";
    public static final String URL_SEARCH_BARCODE_INFO = BASE_URL + "/search_barcode_info.jsp";
    public static final String URL_SEARCH_BARCODE_INFO_NONFIXED = BASE_URL + "/search_barcode_info_nonfixed.jsp";
    public static final String URL_SEARCH_GOODS_WET = BASE_URL + "/search_goods_wet.jsp";
    public static final String URL_INSERT_GOODS_WET = BASE_URL + "/insert_goods_wet.jsp";
    public static final String URL_INSERT_GOODS_WET_NEW = BASE_URL + "/insert_goods_wet_new.jsp";
    public static final String URL_INSERT_GOODS_WET_HOMEPLUS = BASE_URL + "/insert_goods_wet_homeplus.jsp";
    public static final String URL_INSERT_BARCODE_INFO = BASE_URL + "/insert_barcode_info.jsp";
    public static final String URL_UPDATE_BARCODE_INFO = BASE_URL + "/update_barcode_info.jsp";
    public static final String URL_UPDATE_SHIPMENT = BASE_URL + "/update_shipment.jsp";
    public static final String URL_WET_PRODUCTION_CALC = BASE_URL + "/search_production_calc.jsp";
    public static final String URL_SEARCH_PRODUCTION_NONFIXED = BASE_URL + "/search_production_nonfixed.jsp";
    public static final String URL_SEARCH_HOMEPLUS_NONFIXED = BASE_URL + "/search_homeplus_nonfixed.jsp";
    public static final String URL_SEARCH_HOMEPLUS_NONFIXED2 = BASE_URL + "/search_homeplus_nonfixed2.jsp";

    public static String COMPANY_CODE = "20";                   // 사용자 ID
    public static String REG_ID = "";                   // 사용자 ID
    public static String USER_TYPE = "";                // 사용자 권한
    public static boolean search_bool = false;
    public static String selectDay = "";                // 계근 선택 날짜
    public static String selectWarehouse = "";          // 창고
    public static String searchType = "0";              // 계근대상 종류

    public static String printer_address = "";          // 모바일프린터 MAC주소
    public static boolean printer_setting = true;       // 모바일프린터 사용여부
    public static boolean print_bool = true;            // 인쇄 여부

    public static ArrayList<String> list_center_info;

    //	데이터 null 체크
    public static String nullCheck(String value, String defaultValue) {
        try {
            return (value == null || "".equals(value) || "null".equals(value)) ? defaultValue : value;
        } catch (Exception ex) {
            Log.e(TAG, "======== Common nullCheck Exception ========");
            Log.e(TAG, ex.getMessage().toString());
            return defaultValue;
        }
    }
}
