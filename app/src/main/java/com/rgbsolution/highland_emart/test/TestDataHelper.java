package com.rgbsolution.highland_emart.test;

import android.content.Context;
import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.db.DBHelper;
import com.rgbsolution.highland_emart.db.DBInfo;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Barcodes_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 테스트 데이터 삽입/삭제 헬퍼 클래스
 *
 * ★★★ 테스트 완료 후 이 파일 전체 삭제 ★★★
 *
 * 삭제 방법:
 * 1. 이 파일 삭제: test/TestDataHelper.java
 * 2. MainActivity.java에서 TestDataHelper 호출 코드 삭제
 */
public class TestDataHelper {

    private static final String TAG = "TestDataHelper";
    private static final SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat timeformat = new SimpleDateFormat("HHmmss");

    /**
     * 전체 마트사 테스트 데이터 삽입
     */
    public static void insertAllTestData(Context context) {
        Log.d(TAG, "===== 전체 마트사 테스트 데이터 삽입 시작 =====");
        insertTestDataForEmart(context);           // searchType "0"
        insertTestDataForHomeplus(context);        // searchType "2"
        insertTestDataForWholesale(context);       // searchType "3"
        insertTestDataForEmartNonfixed(context);   // searchType "4"
        insertTestDataForHomeplusNonfixed(context);// searchType "5"
        insertTestDataForLotte(context);           // searchType "6"
        Log.d(TAG, "===== 전체 마트사 테스트 데이터 삽입 완료 =====");
    }

    /**
     * 전체 테스트 데이터 삭제
     */
    public static void deleteAllTestData(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String deleteShipment = "DELETE FROM " + DBInfo.TABLE_NAME_SHIPMENT
                    + " WHERE GI_H_ID LIKE 'TEST%'";
            dbHelper.executeSql(deleteShipment);

            String deleteBarcodeInfo = "DELETE FROM " + DBInfo.TABLE_NAME_BARCODE_INFO
                    + " WHERE PACKER_PRODUCT_CODE LIKE 'TEST%'";
            dbHelper.executeSql(deleteBarcodeInfo);

            // TB_GOODS_WET 테이블에서 테스트 바코드 삭제 (중복 스캔 방지 해제)
            String deleteGoodsWet = "DELETE FROM " + DBInfo.TABLE_NAME_GOODS_WET
                    + " WHERE BARCODE LIKE '010%8801234567890%'"
                    + " OR BARCODE LIKE '010%2288012345000%'"
                    + " OR BARCODE LIKE '010%3388012345000%'"
                    + " OR BARCODE LIKE '010%4488012345000%'"
                    + " OR BARCODE LIKE '010%5588012345000%'"
                    + " OR BARCODE LIKE '010%6688012345000%'";
            dbHelper.executeSql(deleteGoodsWet);
            Log.d(TAG, "===== 전체 테스트 데이터 삭제 완료 (TB_GOODS_WET 포함) =====");
        } catch (Exception e) {
            Log.e(TAG, "테스트 데이터 삭제 실패: " + e.getMessage());
        }
        dbHelper.close();
    }

    // ========== 이마트 (searchType = "0") ==========
    public static void insertTestDataForEmart(Context context) {
        DBHandler.createqueryShipment(context);  // 테이블 생성 보장
        insertBarcodeInfo(context, "TEST_PKR_001", "8801234567890", "이마트 테스트 상품");

        Shipments_Info si = createBaseShipmentInfo();
        si.setGI_H_ID("TEST_GI_H_001");
        si.setGI_D_ID("TEST_GI_D_001");
        si.setEOI_ID("EOI_TEST_001");
        si.setITEM_CODE("ITEM_TEST_001");
        si.setITEM_NAME("이마트 테스트 상품");
        si.setEMARTITEM_CODE("123456");            // 이마트 상품코드 6자리 (바코드용)
        si.setEMARTITEM("이마트 테스트");
        si.setCENTERNAME("이마트");
        si.setCLIENTNAME("이마트 테스트점");
        si.setPACKER_PRODUCT_CODE("TEST_PKR_001");
        si.setBARCODE_TYPE("M0");
        si.setITEM_TYPE("W");
        si.setBARCODEGOODS("8801234567890");
        si.setWH_AREA("A-1");
        si.setUSE_CODE("01");
        si.setUSE_NAME("일반");
        si.setSTORE_CODE("1234");
        si.setEMART_PLANT_CODE("610933");

        DBHandler.insertqueryShipment(context, si, "N");
        Log.d(TAG, "이마트 테스트 데이터 삽입 완료");

        // ===== 삽입 검증 로그 =====
        verifyInsertedData(context, "이마트", "TEST_PKR_001", "8801234567890");
    }

    // ========== 홈플러스 (searchType = "2") ==========
    public static void insertTestDataForHomeplus(Context context) {
        DBHandler.createqueryShipment(context);  // 테이블 생성 보장
        insertBarcodeInfo(context, "TEST_HP_PKR_001", "2288012345000", "홈플러스 테스트 상품");

        Shipments_Info si = createBaseShipmentInfo();
        si.setGI_H_ID("TEST_HP_H_001");
        si.setGI_D_ID("TEST_HP_D_001");
        si.setEOI_ID("HP_EOI_001");
        si.setITEM_CODE("HP_ITEM_001");
        si.setITEM_NAME("홈플러스 테스트 등심");
        si.setEMARTITEM_CODE("234567");            // 홈플러스 상품코드 6자리
        si.setEMARTITEM("홈플러스 등심");
        si.setCENTERNAME("홈플러스물류센터");
        si.setCLIENTNAME("홈플러스 잠실점");
        si.setPACKER_PRODUCT_CODE("TEST_HP_PKR_001");
        si.setBARCODE_TYPE("H0");
        si.setITEM_TYPE("W");
        si.setBARCODEGOODS("2288012345000");
        si.setSTORE_CODE("HP1234");               // 홈플러스 점포코드 (라벨 출력용)
        si.setEMARTLOGIS_CODE("HP1234");          // 홈플러스 지점코드 (라벨 출력용)

        DBHandler.insertqueryShipment(context, si, "N");
        Log.d(TAG, "홈플러스 테스트 데이터 삽입 완료");
    }

    // ========== 롯데마트 (searchType = "6") ==========
    public static void insertTestDataForLotte(Context context) {
        DBHandler.createqueryShipment(context);  // 테이블 생성 보장
        insertBarcodeInfo(context, "TEST_LT_PKR_001", "6688012345000", "롯데마트 테스트 상품");

        Shipments_Info si = createBaseShipmentInfo();
        si.setGI_H_ID("TEST_LT_H_001");
        si.setGI_D_ID("TEST_LT_D_001");
        si.setEOI_ID("LT_EOI_001");
        si.setITEM_CODE("LT_ITEM_001");
        si.setITEM_NAME("롯데마트 테스트 양고기");
        si.setEMARTITEM_CODE("123456");            // 롯데 상품코드 6자리 (바코드용)
        si.setEMARTITEM("롯데마트 양고기");
        si.setCENTERNAME("롯데물류센터");
        si.setCLIENTNAME("롯데마트 서울역점");
        si.setPACKER_PRODUCT_CODE("TEST_LT_PKR_001");
        si.setBARCODE_TYPE("L0");
        si.setITEM_TYPE("W");
        si.setBARCODEGOODS("6688012345000");
        si.setWH_AREA("LT_A01");
        si.setLAST_BOX_ORDER("0");
        si.setEMARTLOGIS_CODE("LT01");             // 롯데 회사코드 (바코드 앞부분)
        si.setIMPORT_ID_NO("123456789012");        // 이력번호 12자리 (바코드2용)

        DBHandler.insertqueryShipment(context, si, "N");
        Log.d(TAG, "롯데마트 테스트 데이터 삽입 완료");
    }

    // ========== 도매업체 (searchType = "3") ==========
    // 참고: 도매업체는 라벨 출력 없음
    public static void insertTestDataForWholesale(Context context) {
        DBHandler.createqueryShipment(context);  // 테이블 생성 보장
        insertBarcodeInfo(context, "TEST_WS_PKR_001", "3388012345000", "도매 테스트 상품");

        Shipments_Info si = createBaseShipmentInfo();
        si.setGI_H_ID("TEST_WS_H_001");
        si.setGI_D_ID("TEST_WS_D_001");
        si.setEOI_ID("WS_EOI_001");
        si.setITEM_CODE("WS_ITEM_001");
        si.setITEM_NAME("도매 테스트 부채살");
        si.setEMARTITEM_CODE("345678");            // 도매 상품코드 6자리
        si.setEMARTITEM("도매 부채살");
        si.setCENTERNAME("도매물류센터");
        si.setCLIENTNAME("도매업체A");
        si.setPACKER_PRODUCT_CODE("TEST_WS_PKR_001");
        si.setBARCODE_TYPE("NA");
        si.setITEM_TYPE("W");
        si.setBARCODEGOODS("3388012345000");

        DBHandler.insertqueryShipment(context, si, "N");
        Log.d(TAG, "도매업체 테스트 데이터 삽입 완료");
    }

    // ========== 이마트 비정량 (searchType = "4") ==========
    public static void insertTestDataForEmartNonfixed(Context context) {
        DBHandler.createqueryShipment(context);  // 테이블 생성 보장
        insertBarcodeInfo(context, "TEST_NF_PKR_001", "4488012345000", "이마트 비정량 테스트");

        Shipments_Info si = createBaseShipmentInfo();
        si.setGI_H_ID("TEST_NF_H_001");
        si.setGI_D_ID("TEST_NF_D_001");
        si.setEOI_ID("NF_EOI_001");
        si.setITEM_CODE("NF_ITEM_001");
        si.setITEM_NAME("이마트 비정량 안심");
        si.setEMARTITEM_CODE("456789");            // 이마트 비정량 상품코드 6자리 (바코드용)
        si.setEMARTITEM("이마트 안심 비정량");
        si.setCENTERNAME("이마트 비정량센터");
        si.setCLIENTNAME("이마트 판교점");
        si.setPACKER_PRODUCT_CODE("TEST_NF_PKR_001");
        si.setBARCODE_TYPE("M0");
        si.setITEM_TYPE("B");
        si.setBARCODEGOODS("4488012345000");
        si.setWH_AREA("NF_A01");
        si.setUSE_CODE("NF");
        si.setUSE_NAME("비정량용");
        si.setSTORE_CODE("1234");
        si.setEMART_PLANT_CODE("610933");

        DBHandler.insertqueryShipment(context, si, "N");
        Log.d(TAG, "이마트 비정량 테스트 데이터 삽입 완료");
    }

    // ========== 홈플러스 비정량 (searchType = "5") ==========
    public static void insertTestDataForHomeplusNonfixed(Context context) {
        DBHandler.createqueryShipment(context);  // 테이블 생성 보장
        insertBarcodeInfo(context, "TEST_HPF_PKR_001", "5588012345000", "홈플러스 비정량 테스트");

        Shipments_Info si = createBaseShipmentInfo();
        si.setGI_H_ID("TEST_HPF_H_001");
        si.setGI_D_ID("TEST_HPF_D_001");
        si.setEOI_ID("HPF_EOI_001");
        si.setITEM_CODE("HPF_ITEM_001");
        si.setITEM_NAME("홈플러스 비정량 채끝");
        si.setEMARTITEM_CODE("789012");            // 홈플러스 비정량 상품코드 6자리
        si.setEMARTITEM("홈플러스 채끝 비정량");
        si.setCENTERNAME("홈플러스 비정량센터");
        si.setCLIENTNAME("홈플러스 목동점");
        si.setPACKER_PRODUCT_CODE("TEST_HPF_PKR_001");
        si.setBARCODE_TYPE("H0");
        si.setITEM_TYPE("B");
        si.setBARCODEGOODS("5588012345000");
        si.setWH_AREA("HPF_A01");
        si.setUSE_CODE("HPF");
        si.setUSE_NAME("비정량용");
        si.setSTORE_CODE("5678");                  // 홈플러스 비정량 점포코드 (라벨에서 사용)
        si.setEMARTLOGIS_CODE("HPF123");           // 홈플러스 비정량 지점코드

        DBHandler.insertqueryShipment(context, si, "N");
        Log.d(TAG, "홈플러스 비정량 테스트 데이터 삽입 완료");
    }

    // ========== 공통 헬퍼 메서드 ==========

    private static Shipments_Info createBaseShipmentInfo() {
        Shipments_Info si = new Shipments_Info();
        si.setGI_REQ_PKG("10");
        si.setGI_REQ_QTY("100.0");
        si.setAMOUNT("0");
        si.setGI_REQ_DATE(dateformat.format(new Date()));
        si.setBRAND_CODE("TEST_BRAND");
        si.setBRANDNAME("테스트브랜드");
        si.setCLIENT_CODE("TEST_CLIENT");
        si.setPACKER_CODE("TEST_PACKER");
        si.setPACKERNAME("테스트패커");
        si.setPACKWEIGHT("10.0");
        si.setBL_NO("TEST_BL_001");
        si.setGOODS_R_ID("TEST_GR_001");
        si.setGR_REF_NO("TEST_REF_001");
        si.setITEM_SPEC("테스트 스펙");
        si.setCT_CODE("KR");
        si.setCT_NAME("한국");
        si.setIMPORT_ID_NO("123456789012");        // 12자리 필수 (substring(8,12) 사용)
        si.setSTORE_IN_DATE(dateformat.format(new Date()));
        si.setEMARTLOGIS_CODE("123456");           // 6자리 필수 (substring(0,6) 사용)
        si.setEMARTLOGIS_NAME("테스트물류");
        return si;
    }

    private static void insertBarcodeInfo(Context context, String packerProductCode, String barcodeGoods, String itemName) {
        // 테이블 생성 보장 (서버 통신 건너뛰면 테이블이 없을 수 있음)
        DBHandler.createqueryBarcodeInfo(context);

        DBHelper dbHelper = new DBHelper(context);
        dbHelper.open();
        try {
            String sqlStr = "INSERT INTO " + DBInfo.TABLE_NAME_BARCODE_INFO + " ("
                    + DBInfo.PACKER_CLIENT_CODE + ", "
                    + DBInfo.PACKER_PRODUCT_CODE + ", "
                    + DBInfo.PACKER_PRD_NAME + ", "
                    + DBInfo.ITEM_CODE + ", "
                    + DBInfo.ITEM_NAME_KR + ", "
                    + DBInfo.BRAND_CODE + ", "
                    + DBInfo.BARCODEGOODS + ", "
                    + DBInfo.BASEUNIT + ", "
                    + DBInfo.ZEROPOINT + ", "
                    + DBInfo.PACKER_PRD_CODE_FROM + ", "
                    + DBInfo.PACKER_PRD_CODE_TO + ", "
                    + DBInfo.BARCODEGOODS_FROM + ", "
                    + DBInfo.BARCODEGOODS_TO + ", "
                    + DBInfo.WEIGHT_FROM + ", "
                    + DBInfo.WEIGHT_TO + ", "
                    + DBInfo.MAKINGDATE_FROM + ", "
                    + DBInfo.MAKINGDATE_TO + ", "
                    + DBInfo.BOXSERIAL_FROM + ", "
                    + DBInfo.BOXSERIAL_TO + ", "
                    + DBInfo.STATUS + ", "
                    + DBInfo.REG_ID + ", "
                    + DBInfo.REG_DATE + ", "
                    + DBInfo.REG_TIME + ", "
                    + DBInfo.MEMO + ", "
                    + DBInfo.SHELF_LIFE
                    + ") VALUES ("
                    + "'TEST_CLIENT', "
                    + "'" + packerProductCode + "', "
                    + "'" + itemName + "', "
                    + "'TEST_ITEM', "
                    + "'" + itemName + "', "
                    + "'TEST_BRAND', "
                    + "'" + barcodeGoods + "', "
                    + "'KG', '2', '', '', '4', '16', '17', '20', '21', '26', '27', '29', "
                    + "'Y', 'TEST', '" + dateformat.format(new Date()) + "', '" + timeformat.format(new Date()) + "', "
                    + "'테스트용', '30')";
            dbHelper.executeSql(sqlStr);
        } catch (Exception e) {
            Log.e(TAG, "바코드 정보 삽입 실패: " + e.getMessage());
        }
        dbHelper.close();
    }

    /**
     * 삽입된 데이터 검증 (디버깅용)
     */
    private static void verifyInsertedData(Context context, String expectedCenterName, String expectedPPCode, String expectedBarcodeGoods) {
        Log.d(TAG, "========== 테스트 데이터 검증 시작 ==========");

        // 1. TB_SHIPMENT 검증
        ArrayList<Shipments_Info> shipments = DBHandler.selectqueryAllShipment(context);
        Log.d(TAG, "[TB_SHIPMENT] 총 건수: " + shipments.size());

        // 2. TB_BARCODE_INFO 검증
        ArrayList<Barcodes_Info> barcodes = DBHandler.selectqueryBarcodeInfo(context);
        Log.d(TAG, "[TB_BARCODE_INFO] 총 건수: " + barcodes.size());

        for (Barcodes_Info bi : barcodes) {
            Log.d(TAG, "[TB_BARCODE_INFO] PACKER_PRODUCT_CODE: " + bi.getPACKER_PRODUCT_CODE());
            Log.d(TAG, "[TB_BARCODE_INFO] BARCODEGOODS: " + bi.getBARCODEGOODS());
            Log.d(TAG, "[TB_BARCODE_INFO] BARCODEGOODS_FROM: " + bi.getBARCODEGOODS_FROM());
            Log.d(TAG, "[TB_BARCODE_INFO] BARCODEGOODS_TO: " + bi.getBARCODEGOODS_TO());
        }

        // 3. 센터 목록 검증
        ArrayList<String> centers = DBHandler.selectqueryCenterList(context);
        Log.d(TAG, "[센터목록] 총 건수: " + centers.size());
        for (String center : centers) {
            Log.d(TAG, "[센터목록] CENTERNAME: " + center);
        }

        // 4. Common.selectDay 검증
        Log.d(TAG, "[Common.selectDay]: " + Common.selectDay);
        Log.d(TAG, "[오늘날짜]: " + dateformat.format(new Date()));

        Log.d(TAG, "========== 테스트 데이터 검증 완료 ==========");
    }
}
