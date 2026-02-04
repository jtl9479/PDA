package com.rgbsolution.highland_emart.print;

/**
 * 라벨 출력 헬퍼 클래스
 * <p>
 * BixolonShipmentActivity에서 분리된 라벨 출력 관련 메소드 모음.
 * SLCS 명령어를 사용하여 Bixolon 프린터로 바코드 라벨을 출력한다.
 * </p>
 *
 * <h3>지원 마트사</h3>
 * <ul>
 *   <li>이마트 (searchType "0")</li>
 *   <li>도매 비정량 (searchType "4")</li>
 *   <li>홈플러스 비정량 (searchType "5")</li>
 *   <li>롯데 (searchType "6")</li>
 *   <li>생산 라벨 (searchType "7")</li>
 * </ul>
 *
 * @author Highland Innovation
 * @since 2026-02-04
 * @see com.rgbsolution.highland_emart.BixolonShipmentActivity
 */
public class LabelPrintHelper {

    private static final String TAG = "LabelPrintHelper";

    // ========================================================================================
    // 상수 정의 (BixolonShipmentActivity에서 복사)
    // ========================================================================================

    // searchType 상수 (라벨 출력에서 사용)
    private static final String SEARCH_TYPE_EMART = "0";             // 이마트 출하
    private static final String SEARCH_TYPE_LOTTE = "6";             // 롯데 출하

    // 업체 정보 상수
    private static final String COMPANY_CODE = "610933";                    // 회사 코드
    private static final String COMPANY_NAME = "(주)하이랜드이노베이션";    // 회사명

    // 미트센터 관련 상수
    private static final String MEAT_CENTER_CODE = "059015";         // 미트센터 업체코드
    private static final String MEAT_CENTER_STORE_CODE = "9231";     // 미트센터 지점코드
    private static final String KILKOY_PACKER_CODE = "30228";        // 킬코이 패커코드
    private static final String LOGIS_CODE_DEFAULT = "0000000";      // 로지스코드 기본값

    // 바코드 타입 상수
    private static final String BARCODE_TYPE_M0 = "M0";
    private static final String BARCODE_TYPE_M1 = "M1";
    private static final String BARCODE_TYPE_M3 = "M3";
    private static final String BARCODE_TYPE_M4 = "M4";
    private static final String BARCODE_TYPE_M8 = "M8";
    private static final String BARCODE_TYPE_M9 = "M9";
    private static final String BARCODE_TYPE_E0 = "E0";
    private static final String BARCODE_TYPE_E1 = "E1";
    private static final String BARCODE_TYPE_E2 = "E2";
    private static final String BARCODE_TYPE_E3 = "E3";
    private static final String BARCODE_TYPE_P0 = "P0";

    // 계근 방식 상수
    private static final String ITEM_TYPE_W = "W";    // 바코드 계근
    private static final String ITEM_TYPE_HW = "HW";  // 바코드 계근 확장
    private static final String ITEM_TYPE_S = "S";    // 저울 계근
    private static final String ITEM_TYPE_J = "J";    // 지정 중량
    private static final String ITEM_TYPE_B = "B";    // 홈플러스 비정량

    // 센터명 상수 (수입육 센터 판별용)
    private static final String CENTER_NAME_TRD = "TRD";
    private static final String CENTER_NAME_WET = "WET";
    private static final String CENTER_NAME_ET = "E/T";

    // ========================================================================================
    // 인터페이스 정의
    // ========================================================================================

    /**
     * 프린터 콜백 인터페이스
     * <p>
     * Activity와 LabelPrintHelper 간의 통신을 위한 인터페이스.
     * 프린터 데이터 전송 및 UI 업데이트를 Activity에 위임한다.
     * </p>
     */
    public interface PrinterCallback {
        /**
         * 프린터로 데이터 전송
         * @param data SLCS 명령어 바이트 배열
         */
        void sendData(byte[] data);

        /**
         * 바코드 입력창 초기화
         */
        void clearBarcodeInput();
    }

}
