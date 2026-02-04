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

    // ========================================================================================
    // SLCS 헬퍼 메서드 - Bixolon 라벨 프린터 명령어 생성
    // (BixolonShipmentActivity에서 복사)
    // ========================================================================================

    /**
     * SLCS 라벨 초기화
     * - 버퍼 클리어 (CB)
     * - 문자셋 설정 (CS13,0 = 한글)
     *
     * @return SLCS 초기화 명령어 문자열
     */
    private String slcsInit() {
        return "CB\r\n" + "CS13,0\r\n";
    }

    /**
     * SLCS 라벨 크기 설정
     *
     * @param width  라벨 너비 (도트)
     * @param height 라벨 높이 (도트)
     * @return SLCS 라벨 크기 명령어 문자열
     */
    private String slcsLabelSize(int width, int height) {
        return "SW" + width + "\r\n" + "SL" + height + "\r\n";
    }

    /**
     * SLCS 텍스트 출력
     * - V 명령어 사용 (벡터 폰트)
     *
     * @param x      X 좌표
     * @param y      Y 좌표
     * @param width  폰트 너비
     * @param height 폰트 높이
     * @param text   출력할 텍스트
     * @return SLCS 텍스트 명령어 문자열
     */
    private String slcsText(int x, int y, int width, int height, String text) {
        // V x,y,K,w,h,0,N,B,N,0,L,0,'text'
        // K: 한글, 0: 회전없음, N: 일반, B: 굵게, N: 이탤릭없음, 0: 자간, L: 왼쪽정렬, 0: 줄간격
        return "V" + x + "," + y + ",K," + width + "," + height + ",0,N,B,N,0,L,0,'" + text + "'\r\n";
    }

    /**
     * SLCS CODE128 바코드 생성
     *
     * @param x      X 좌표
     * @param y      Y 좌표
     * @param height 바코드 높이
     * @param data   바코드 데이터
     * @return SLCS 바코드 명령어 문자열
     */
    private String slcsBarcode(int x, int y, int height, String data) {
        // BD x,y,barcode_type,narrow,wide,height,rotation,HRI,quiet_zone,'data'
        // CODE128, narrow=2, wide=4, HRI=0(없음), quiet_zone=0
        return "BD" + x + "," + y + ",CODE128,2,4," + height + ",0,0,0,'" + data + "'\r\n";
    }

    /**
     * SLCS 선 그리기
     *
     * @param x1    시작 X 좌표
     * @param y1    시작 Y 좌표
     * @param x2    끝 X 좌표
     * @param y2    끝 Y 좌표
     * @param width 선 두께
     * @return SLCS 선 명령어 문자열
     */
    private String slcsLine(int x1, int y1, int x2, int y2, int width) {
        // LS x1,y1,x2,y2,width
        return "LS" + x1 + "," + y1 + "," + x2 + "," + y2 + "," + width + "\r\n";
    }

    /**
     * SLCS 박스 그리기
     *
     * @param x         X 좌표
     * @param y         Y 좌표
     * @param width     박스 너비
     * @param height    박스 높이
     * @param thickness 선 두께
     * @return SLCS 박스 명령어 문자열
     */
    private String slcsBox(int x, int y, int width, int height, int thickness) {
        // LB x1,y1,x2,y2,thickness
        return "LB" + x + "," + y + "," + (x + width) + "," + (y + height) + "," + thickness + "\r\n";
    }

    /**
     * SLCS 인쇄 실행
     *
     * @param copies 인쇄 매수
     * @return SLCS 인쇄 명령어 문자열
     */
    private String slcsPrint(int copies) {
        return "P" + copies + "\r\n";
    }

    /**
     * SLCS 라벨 피드 (마크 위치로 이동)
     * - 원본: WoosimCmd.feedToMark()에 대응
     * - SLCS T 명령어: Tear-off 위치로 라벨 이동
     *
     * @return SLCS 피드 명령어 문자열
     */
    private String slcsFeedToMark() {
        return "T\r\n";
    }

}
