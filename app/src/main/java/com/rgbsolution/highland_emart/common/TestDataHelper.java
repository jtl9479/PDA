package com.rgbsolution.highland_emart.common;

import android.content.Context;
import android.util.Log;

import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Barcodes_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

/**
 * M0 바코드 타입 테스트 데이터 헬퍼
 *
 * [사용법]
 * 1. MainActivity의 이마트 출하대상받기 버튼에서 호출
 * 2. 테스트 완료 후 이 파일과 관련 호출 코드 삭제
 *
 * [테스트 바코드]
 * 스캔 바코드: 002000100567260310B002
 *   - 위치 3~7 (PACKER_PRD_CODE): 20001
 *   - 위치 8~12 (중량): 00567 → 5.67 (ZEROPOINT=2)
 *   - 위치 13~18 (제조일): 260310
 *   - 위치 19~22 (박스시리얼): B002
 *
 * [M0 라벨 바코드 구성]
 * 상품코드(6) + 중량(6) + 회사코드(610933) + 수입식별번호(12) = 30자리
 */
public class TestDataHelper {

    private static final String TAG = "TestDataHelper";

    /** 테스트 모드 ON/OFF */
    public static final boolean TEST_MODE = false;

    /**
     * 이마트 M0 테스트 데이터 삽입
     * - S_BARCODE_INFO: 1건 (바코드 파싱 규칙)
     * - TB_SHIPMENT: 1건 (M0 출하대상)
     */
    public static void insertTestDataForEmartM0(Context context) {
        Log.d(TAG, "===== M0 테스트 데이터 삽입 시작 =====");

        // 1. 바코드 정보 (S_BARCODE_INFO) 삽입
        insertBarcodeInfo(context);

        // 2. 출하대상 (TB_SHIPMENT) 삽입
        insertShipment(context);

        Log.d(TAG, "===== M0 테스트 데이터 삽입 완료 =====");
    }

    /**
     * 테스트 데이터 전체 삭제
     */
    public static void deleteAllTestData(Context context) {
        DBHandler.deletequeryShipment(context);
        DBHandler.deletequeryBarcodeInfo(context);
        DBHandler.deletequeryAllGoodsWet(context);
        Log.d(TAG, "테스트 데이터 전체 삭제 완료");
    }

    /**
     * S_BARCODE_INFO 테스트 데이터 (1건)
     *
     * 스캔 바코드 예시: 002000100567260310B002 (22자리)
     *   BARCODEGOODS(상품코드) = "20001" (위치 3~7)
     *   중량 = 위치 8~12 → 00567 → /100 → 5.67 KG
     *   제조일 = 위치 13~18 → 260310
     *   박스시리얼 = 위치 19~22 → B002
     */
    private static void insertBarcodeInfo(Context context) {
        Barcodes_Info bi = new Barcodes_Info();
        bi.setPACKER_CLIENT_CODE("TEST_CLIENT");
        bi.setBRAND_CODE("BR01");
        bi.setPACKER_PRODUCT_CODE("20001");
        bi.setPACKER_PRD_NAME("테스트 원료육(M0)");
        bi.setITEM_CODE("ITEM001");
        bi.setITEM_NAME_KR("호주산 척아이롤");
        bi.setBARCODEGOODS("20001");
        bi.setBASEUNIT("KG");
        bi.setZEROPOINT("2");
        bi.setPACKER_PRD_CODE_FROM("3");
        bi.setPACKER_PRD_CODE_TO("7");
        bi.setBARCODEGOODS_FROM("3");
        bi.setBARCODEGOODS_TO("7");
        bi.setWEIGHT_FROM("8");
        bi.setWEIGHT_TO("12");
        bi.setMAKINGDATE_FROM("13");
        bi.setMAKINGDATE_TO("18");
        bi.setBOXSERIAL_FROM("19");
        bi.setBOXSERIAL_TO("22");
        bi.setSTATUS("Y");
        bi.setREG_ID("TEST");
        bi.setSHELF_LIFE("30");

        DBHandler.insertqueryBarcodeInfo(context, bi);
        Log.d(TAG, "S_BARCODE_INFO 삽입 완료 - BARCODEGOODS: 20001");
    }

    /**
     * TB_SHIPMENT 테스트 데이터 (1건) - M0 바코드 타입
     *
     * BARCODE_TYPE = M0
     * ITEM_TYPE = W (원료육, VIEW 고정값)
     * EMARTITEM_CODE = 2800010000001 (앞 6자리 "280001"이 라벨 바코드에 사용)
     * IMPORT_ID_NO = 123456789012 (12자리, 라벨 바코드에 사용)
     * PACKER_PRODUCT_CODE = 20001 (S_BARCODE_INFO와 매칭)
     * BARCODEGOODS = 20001
     * GI_REQ_DATE = Common.selectDay (오늘 날짜)
     */
    private static void insertShipment(Context context) {
        Shipments_Info si = new Shipments_Info();
        si.setGI_D_ID("TEST_GI_D_001");
        si.setITEM_CODE("ITEM001");
        si.setITEM_NAME("캐나다냉장앞돼지목심");
        si.setEMARTITEM_CODE("2493160000001");
        si.setEMARTITEM("캐나다냉장앞돼지목심");
        si.setGI_REQ_PKG("10");
        si.setGI_REQ_QTY("100.0");
        si.setGI_REQ_DATE(Common.selectDay);
        si.setBL_NO("BL2026030001");
        si.setBRAND_CODE("BR01");
        si.setCLIENT_CODE("CL001");
        si.setCLIENTNAME("E/T스타필드위례점(1416)");
        si.setCENTERNAME("여주TRD");
        si.setITEM_SPEC("돼지목심/냉장");
        si.setCT_CODE("CA");
        si.setIMPORT_ID_NO("123456789012");
        si.setPACKER_CODE("PC001");
        si.setPACKER_PRODUCT_CODE("20001");
        si.setBARCODE_TYPE("M0");
        si.setITEM_TYPE("W");
        si.setPACKWEIGHT("");
        si.setBARCODEGOODS("20001");
        si.setSTORE_IN_DATE(Common.selectDay);
        si.setEMARTLOGIS_CODE("0000000");
        si.setWH_AREA("");
        si.setUSE_NAME("원료육");
        si.setUSE_CODE("01");
        si.setCT_NAME("캐나다산");
        si.setSTORE_CODE("1416");
        si.setEMART_PLANT_CODE("");
        si.setLAST_BOX_ORDER("");

        DBHandler.insertqueryShipment(context, si, "0");
        Log.d(TAG, "TB_SHIPMENT 삽입 완료 - M0, PACKER_PRODUCT_CODE: 20001");
    }
}
