package com.rgbsolution.highland_emart.common;

import android.util.Log;

import java.util.ArrayList;

public class Common {

    private static final String TAG = "Common";

    public static boolean D = true;        // Debug ? true : false

//	public static String URL_VERSION = "http://192.168.0.11:8080/highland/check_version.jsp";
//	public static String URL_LOGIN = "http://192.168.0.11:8080/manager_login.jsp";
//	public static String URL_SEARCH_SHIPMENT = "http://192.168.0.11:8080/search_shipment.jsp";
//	public static String URL_SEARCH_BARCODE_INFO = "http://192.168.0.11:8080/search_barcode_info.jsp";
//	public static String URL_SEARCH_GOODS_WET = "http://192.168.0.11:8080/search_goods_wet.jsp";
//	public static String URL_INSERT_GOODS_WET = "http://192.168.0.11:8080/insert_goods_wet.jsp";
//	public static String URL_INSERT_BARCODE_INFO = "http://192.168.0.11:8080/insert_barcode_info.jsp";
//	public static String URL_UPDATE_BARCODE_INFO = "http://192.168.0.11:8080/update_barcode_info.jsp";
//	public static String URL_UPDATE_SHIPMENT = "http://192.168.0.11:8080/update_shipment.jsp";

    //	데이터 조회, 추가, 수정을 위한 주소			test
    public static String URL_VERSION = "";
    public static String URL_LOGIN = "";
    public static String URL_SEARCH_SHIPMENT = "";
    public static String URL_SEARCH_SHIPMENT_HOMEPLUS = "";
    public static String URL_SEARCH_SHIPMENT_WHOLESALE = "";
    public static String URL_SEARCH_SHIPMENT_LOTTE = "";
    public static String URL_SEARCH_PRODUCTION = "";
    public static String URL_SEARCH_PRODUCTION_4LABEL = "";
    public static String URL_SEARCH_BARCODE_INFO = "";
    public static String URL_SEARCH_GOODS_WET = "";
    public static String URL_INSERT_GOODS_WET = "";
    public static String URL_INSERT_GOODS_WET_NEW = "";
    public static String URL_INSERT_GOODS_WET_HOMEPLUS = "";
    public static String URL_INSERT_BARCODE_INFO = "";
    public static String URL_UPDATE_BARCODE_INFO = "";
    public static String URL_UPDATE_SHIPMENT = "";
    public static String URL_WET_PRODUCTION_CALC = "";
    public static String URL_SEARCH_PRODUCTION_NONFIXED = "";
    public static String URL_SEARCH_BARCODE_INFO_NONFIXED = "";
    // homeplus 비정량
    public static String URL_SEARCH_HOMEPLUS_NONFIXED = "";
    public static String URL_SEARCH_HOMEPLUS_NONFIXED2 = "";
//

    //	데이터 조회, 추가, 수정을 위한 주소			real
//	public static String URL_LOGIN = "http://183.111.165.158:8080/highland/real/manager_login.jsp";
//	public static String URL_SEARCH_SHIPMENT = "http://183.111.165.158:8080/highland/real/search_shipment.jsp";
//	public static String URL_SEARCH_BARCODE_INFO = "http://183.111.165.158:8080/highland/real/search_barcode_info.jsp";
//	public static String URL_SEARCH_GOODS_WET = "http://183.111.165.158:8080/highland/real/search_goods_wet.jsp";
//	public static String URL_INSERT_GOODS_WET = "http://183.111.165.158:8080/highland/real/insert_goods_wet.jsp";
//	public static String URL_INSERT_BARCODE_INFO = "http://183.111.165.158:8080/highland/real/insert_barcode_info.jsp";
//	public static String URL_UPDATE_BARCODE_INFO = "http://183.111.165.158:8080/highland/real/update_barcode_info.jsp";
//	public static String URL_UPDATE_SHIPMENT = "http://183.111.165.158:8080/highland/real/update_shipment.jsp";
//
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
