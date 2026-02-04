package com.rgbsolution.highland_emart.print;

import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Barcodes_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    // ========================================================================================
    // 라벨 인쇄 메서드 (BixolonShipmentActivity에서 복사)
    // ========================================================================================

    /**
     * 이마트 출하용 바코드 라벨 인쇄
     * <p>
     * 이마트/트레이더스 출하 시 Bixolon 블루투스 프린터로 바코드 라벨을 인쇄한다.
     * BARCODE_TYPE에 따라 라벨 형식이 달라진다.
     * </p>
     *
     * <h3>바코드 타입별 라벨 형식</h3>
     * <ul>
     *   <li>M0: 기본형 - 미트센터 납품 시 특별 처리 (EMARTLOGIS_CODE로 분기)</li>
     *   <li>M1: 타입1 - 상품명, 바코드, 중량 표시</li>
     *   <li>M3: 타입3 - 소비기한 포함</li>
     *   <li>M4: 타입4 - 소비기한 포함 (M3과 유사)</li>
     *   <li>M8: 수입식별번호 포함</li>
     *   <li>M9: 납품일자 포함</li>
     *   <li>E0, E1, E2, E3: 이마트 확장 타입</li>
     *   <li>P0: 기본 바코드</li>
     * </ul>
     *
     * <h3>특별 처리 케이스</h3>
     * <ul>
     *   <li>킬코이 미트센터(패커코드 30228, 스토어 9231): 제조일에서 소비기한 계산</li>
     *   <li>수입육 센터(TRD/WET/E/T): 소비기한 계산하여 라벨에 표시</li>
     * </ul>
     *
     * @param weight_double 계근 중량
     * @param si 출하 대상 정보 (Shipments_Info)
     * @param reprint 재인쇄 여부 (true: 재인쇄, false: 최초 인쇄)
     * @param making_date 제조일자 (소비기한 계산용)
     * @param barcodeInfo 바코드 정보 (SHELF_LIFE 조회용) - 기존 work_item_bi_info
     * @param currentWorkItem 현재 작업 항목 - 기존 arSM.get(current_work_position)
     * @param searchType 검색 유형 - 기존 Common.searchType
     * @param callback 프린터 콜백
     * @return 인쇄에 사용된 중량 문자열
     */
    public String setPrinting(double weight_double, Shipments_Info si, boolean reprint, String making_date,
                              Barcodes_Info barcodeInfo, Shipments_Info currentWorkItem, String searchType,
                              PrinterCallback callback){
        if (Common.D) {
            Log.d(TAG, "센터명 : '" + si.CENTERNAME + "'\n출고업체명 : '" + si.CLIENTNAME + "'\n이마트상품명 : '"
                    + si.EMARTITEM + "'\n중량 : '" + weight_double + "'");
        }

        String expiryDayConvert = "";

        if (si.getPACKER_CODE().equals(KILKOY_PACKER_CODE) && si.getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)) { //킬코이 , 미트센터 납품분으로 makingdate 이용해 소비기한 변조해 출력

            String rawExp = "20"+making_date;
            Log.e(TAG, "rawExp chk : " + rawExp);

            int shelfLiftToInt = Integer.parseInt(barcodeInfo.getSHELF_LIFE()) - 1; //1일을 뺀다.

            Log.e(TAG, "shelf life chk : " + shelfLiftToInt);

            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd");
            Calendar cal = Calendar.getInstance();
            Date dt = null;

            try {
                dt = dtFormat.parse(rawExp);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            cal.setTime(dt);
            //쉘프라이프를 더한다.
            cal.add(Calendar.DATE,  shelfLiftToInt);
            String expireDateCalc = dtFormat.format(cal.getTime());
            Log.e(TAG, "계산된 날짜 원본 : " + expireDateCalc);

            //dash 처리 위해서 잘라서 다시 붙임
            String YYYY = expireDateCalc.substring(0,4);
            String MM = expireDateCalc.substring(4,6);
            String DD = expireDateCalc.substring(6);

            String YYYYMMDD = YYYY+"-"+MM+"-"+DD;

            if(currentWorkItem.getBARCODE_TYPE().equals(BARCODE_TYPE_M3) || currentWorkItem.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)){
                expiryDayConvert = "소비기한: "+YYYYMMDD;
            }else{
                expiryDayConvert = "/소비기한 : "+YYYYMMDD;
            }

        }

        if(searchType.equals(SEARCH_TYPE_EMART)){ //이마트 수입육 계근일때만
            if (si.getCENTERNAME().contains(CENTER_NAME_ET)  ||  si.getCENTERNAME().contains(CENTER_NAME_WET)  ||  si.getCENTERNAME().contains(CENTER_NAME_TRD)) { //트레이더스 납품분

                String rawExp = "20"+making_date;
                Log.e(TAG, "rawExp chk : " + rawExp);

                int shelfLiftToInt = Integer.parseInt(barcodeInfo.getSHELF_LIFE()) - 1;
                Log.e(TAG, "shelf life chk : " + shelfLiftToInt);

                SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd");
                Calendar cal = Calendar.getInstance();
                Date dt = null;

                try {
                    dt = dtFormat.parse(rawExp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                cal.setTime(dt);
                //쉘프라이프를 더한다.
                cal.add(Calendar.DATE,  shelfLiftToInt);
                String expireDateCalc = dtFormat.format(cal.getTime());
                Log.e(TAG, "계산된 날짜 원본 : " + expireDateCalc);

                //dash 처리 위해서 잘라서 다시 붙임
                String YYYY = expireDateCalc.substring(0,4);
                String MM = expireDateCalc.substring(4,6);
                String DD = expireDateCalc.substring(6);

                String YYYYMMDD = YYYY+"-"+MM+"-"+DD;

                if(currentWorkItem.getBARCODE_TYPE().equals(BARCODE_TYPE_M3) || currentWorkItem.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)){
                    expiryDayConvert = "소비기한: "+YYYYMMDD;
                }else{
                    expiryDayConvert = "/소비기한: "+YYYYMMDD;
                }
            }
        }

        String pointName = "";                // 이마트 지점명
        String pCompCode = "";
        String pCompName = "";

        // 모든 건 이노베이션으로 출고
        pCompCode = COMPANY_CODE;
        pCompName = COMPANY_NAME;

        String sBarcode = "";
        String sBarcodeStr = "";
        String pBarcode = "";
        String pBarcodeStr = "";
        String pBarcode2 = "";
        String pBarcodeStr2 = "";
        String meatCenterBarcode = "";
        String pBarcodeStr3 = "";
        String whArea = "";

        //소수점 한자리 이후 절사
        String print_weight_str = "";
        Double print_weight_double = 0.0;
        String weight_ = String.valueOf(weight_double);
        String weight_str = String.valueOf(weight_double);
        String[] weight_sp = weight_str.split("\\.");
        String print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 1);
        weight_double = Double.parseDouble(print_weight);

        // 이마트 상품 W, J 구분
        if (si.getITEM_TYPE().equals(ITEM_TYPE_W) || si.getITEM_TYPE().equals(ITEM_TYPE_HW) ) {
            print_weight_double = weight_double;
            print_weight_str = String.valueOf(print_weight_double);

            if (Common.D) {
                Log.d(TAG, "print_weight_str : " + print_weight_str);
                Log.d(TAG, "ITEM_TYPE : W");
            }
        } else if (si.getITEM_TYPE().equals(ITEM_TYPE_J)) {
            print_weight_str = si.getPACKWEIGHT();
            print_weight_double = Double.parseDouble(print_weight_str);
            if (Common.D) {
                Log.d(TAG, "ITEM_TYPE : J");
            }
        }

        // .을 지워서 숫자만으로 표시
        String temp = print_weight_str.replace(".", "");
        sBarcode = si.getSTORE_CODE();
        sBarcodeStr = si.getSTORE_CODE();
        int iLen = temp.length();

        for (int i = 0; i < 6 - iLen; i++) {
            temp = "0" + temp;            // ex) 198 -> 000198
        }

        Log.e(TAG, "::::::::: STORE CODE TEST ::::::::" + si.getSTORE_CODE());

        print_weight_str = temp;
        if (Common.D) {
            Log.d(TAG, "중량 6 자리 : " + print_weight_str);
        }

        switch (si.getBARCODE_TYPE()) {
            case "M0":
                // 이마트상품코드 형식 1
                // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
                if (Common.D) {
                    Log.e(TAG, "::::::::: M0 ::::::::");
                    Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode);
                    Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
                }

                pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
                pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

                pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
                pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
                break;
            case "M1":
                // 이마트 상품코드 형식 2
                // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드
                if (Common.D) {
                    Log.e(TAG, "::::::::: M1 ::::::::");
                    Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode);
                }

                pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode;
                pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;

                pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode;
                pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;
                break;
            case "M3":
                // 이마트상품코드 형식 3
                // 납품코드 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
                if (Common.D) {
                    Log.e(TAG, "::::::::: M3 ::::::::");
                    Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode);
                    Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
                }

                pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
                pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

                pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
                pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
                break;
            case "M4":
                // 이마트 상품코드 형식 4
                // 납품코드 + 중량 6자리 + 회사코드
                if (Common.D) {
                    Log.e(TAG, "::::::::: M4 ::::::::");
                    Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode);
                }

                pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode;
                pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;

                pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode;
                pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;
                break;
            case "E0":
                // 에브리데이 상품코드 형식 1
                //상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
                if (Common.D) {
                    Log.e(TAG, "::::::::: E0 ::::::::");
                    Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode);
                    Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
                }

                pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
                pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

                pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
                pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
                break;
            case "E1":
                // 에브리데이 상품코드 형식 2
                // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 111111111111
                if (Common.D) {
                    Log.e(TAG, "::::::::: E1 ::::::::");
                    Log.d(TAG, "full itemcode : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode);
                }

                pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + "111111111111";
                pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " 111111111111";

                pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + "111111111111";
                pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " 111111111111";
                break;
            case "E2":
                // 에브리데이 상품코드 형식 3
                // XXXXXXXXXXXXXXXXXXXX 상품코드 앞자리 6 자리 + 수입식별번호(12자리) XXXXXXXXXXXXX
                // 상품코드 13자리 + 수입식별번호(12자리)  = 25
                if (Common.D) {
                    Log.e(TAG, "::::::::: E2 ::::::::");
                    Log.d(TAG, "full itemcode : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
                }
                pBarcode = si.getEMARTITEM_CODE().toString() + si.getIMPORT_ID_NO();
                pBarcodeStr = si.getEMARTITEM_CODE().toString() + " " + si.getIMPORT_ID_NO();

                pBarcode2 = si.getEMARTLOGIS_CODE().toString() + si.getIMPORT_ID_NO();
                pBarcodeStr2 = si.getEMARTLOGIS_CODE().toString() + " " + si.getIMPORT_ID_NO();
                break;
            case "E3":
                // 에브리데이 상품코드 형식 4
                // 상품코드 13자리
                if (Common.D) {
                    Log.e(TAG, "::::::::: E3 ::::::::");
                }

                pBarcode = si.getEMARTITEM_CODE();
                pBarcodeStr = si.getEMARTITEM_CODE();

                pBarcode2 = si.getEMARTLOGIS_CODE();
                pBarcodeStr2 = si.getEMARTLOGIS_CODE();
                break;
            case "P0":
                // 생산투입시
                if (Common.D) {
                    Log.e(TAG, "::::::::: P0 ::::::::");
                    Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode);
                    Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
                }

                pBarcode = si.getEMARTITEM_CODE() + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
                pBarcodeStr = si.getEMARTITEM_CODE() + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

                pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
                pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
                break;

            case "M9":
                // 이마트 우육 센터납
                // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
                if (Common.D) {
                    Log.e(TAG, "::::::::: M9 ::::::::");
                    Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode);
                    Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
                    Log.d(TAG, "용도명 : " + si.getUSE_NAME());
                }

                pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
                pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

                pBarcode2 = si.getEMARTITEM_CODE().substring(0, 6) +  si.getIMPORT_ID_NO() +  si.getUSE_CODE();
                pBarcodeStr2 = si.getEMARTITEM_CODE().substring(0, 6) + " " + si.getIMPORT_ID_NO() + " " + si.getUSE_CODE();

                //제품명 + 용도
                pBarcodeStr3 = si.EMARTITEM +","+si.getUSE_NAME();

                break;

            case "M8":
                // 이마트 비정량 납품분
                // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
                if (Common.D) {
                    Log.e(TAG, "::::::::: M8 ::::::::");
                    Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode);
                    Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
                }

                pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
                pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

                pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode ;
                pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
                break;
        }

        String[] split_name = null;
        if (si.CLIENTNAME.contains("이마트")) {
            split_name = si.CLIENTNAME.split("이마트");
        } else if (si.CLIENTNAME.contains("신세계백화점")) {
            split_name = si.CLIENTNAME.split("백화점");
        } else if (si.CLIENTNAME.contains("EVERY")) {
            split_name = si.CLIENTNAME.split("EVERY");
        } else if (si.CLIENTNAME.contains(CENTER_NAME_ET)) {
            split_name = si.CLIENTNAME.split(CENTER_NAME_ET);
        } else {
            pointName = si.CLIENTNAME.toString();
        }

        if (split_name != null && split_name.length > 1) {
            pointName = split_name[1].toString();
        }

        if (Common.D) {
            Log.d(TAG, "print Barcode : " + pBarcode.toString());
            Log.d(TAG, "print Weight : " + print_weight_str);
        }

        // ========== SLCS 명령어로 이마트 확장 라벨 인쇄 (Bixolon 프린터) ==========
        try {
            StringBuilder slcsCmd = new StringBuilder();
            slcsCmd.append(slcsInit());                                              // 프린터 초기화
            slcsCmd.append(slcsLabelSize(576, 460));                                 // 라벨 크기 설정

            // 센터명 출력
            if (7 < si.CENTERNAME.length()) {
                slcsCmd.append(slcsText(10, 12, 35, 35, si.CENTERNAME));
                if (Common.D)
                    Log.i(TAG, "센터명 > 7 ,  size 30");
            } else {
                slcsCmd.append(slcsText(10, 10, 40, 40, si.CENTERNAME));
                if (Common.D)
                    Log.i(TAG, "센터명 <= 7 ,  size 40");
            }

            // 바코드 타입별 업체명/지점명 출력
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
                // M3, M4는 여기서 출력 없음
            } else if(si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)){
                String vendorName = "[" + COMPANY_NAME + "]";
                slcsCmd.append(slcsText(330, 13, 25, 25, vendorName));                // 업체명 출력

                String storeNamePlusCode = pointName + "(" +  si.getSTORE_CODE() +")";

                if (11 < si.CLIENTNAME.toString().length()) {
                    slcsCmd.append(slcsText(10, 270, 35, 35, storeNamePlusCode.toString()));  // 지점명 출력
                    if (Common.D)
                        Log.i(TAG, "지점명 > 11 ,  size 30");
                } else {
                    slcsCmd.append(slcsText(10, 270, 40, 40, storeNamePlusCode.toString()));  // 지점명 출력
                    if (Common.D)
                        Log.i(TAG, "지점명 <= 11 ,  size 40");
                }
                // 저울스캔용 표시
                String usePurpose = "[저울 스캔용]";
                slcsCmd.append(slcsText(400, 270, 25, 25, usePurpose));
            } else {
                if (11 < si.CLIENTNAME.toString().length()) {
                    slcsCmd.append(slcsText(10, 60, 35, 35, pointName.toString()));          // 지점명 출력
                    if (Common.D)
                        Log.i(TAG, "지점명 > 11 ,  size 30");
                } else {
                    slcsCmd.append(slcsText(10, 60, 40, 40, pointName.toString()));          // 지점명 출력
                    if (Common.D)
                        Log.i(TAG, "지점명 <= 11 ,  size 40");
                }
            }

            // 상품명 출력 (바코드 타입별 위치, 크기)
            int itemX = 80, itemY = 120;  // 기본 위치
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
                itemX = 15; itemY = 65;
            } else if(si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)){
                itemX = 15; itemY = 70;
            }
            if (si.EMARTITEM.length() > 14) {
                slcsCmd.append(slcsText(itemX, itemY, 35, 35, si.EMARTITEM));
            } else {
                slcsCmd.append(slcsText(itemX, itemY, 40, 40, si.EMARTITEM));
            }

            Log.i(TAG, "===============EMARTITEM============" + si.EMARTITEM);
            Log.i(TAG, "===============sBarcode============" + sBarcode);

            // sBarcode 바코드 출력 (M9 제외)
            if (!si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)){
                slcsCmd.append(slcsBarcode(420, 20, 60, sBarcode));
            }

            Log.i(TAG, "===============sBarcode2============" + sBarcodeStr);

            // sBarcodeStr 텍스트 출력 (M9 제외)
            if (!si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)){
                slcsCmd.append(slcsText(450, 80, 25, 25, sBarcodeStr));               // 바코드번호(숫자) 출력
            }

            Log.i(TAG, "===============pBarcode============" + pBarcode);
            Log.i(TAG, "===============pBarcode2============" + pBarcode2);

            // 바코드 타입별 메인 바코드 출력 위치 설정
            int barcodeX = 80, barcodeY = 170;  // 기본 위치
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_E0)
                    || si.getBARCODE_TYPE().equals(BARCODE_TYPE_E1) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_M8)) {
                barcodeX = 80; barcodeY = 170;
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M1)) {
                barcodeX = 145; barcodeY = 170;
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_E2)) {
                barcodeX = 90; barcodeY = 170;
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_E3)) {
                barcodeX = 160; barcodeY = 170;
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3)) {
                barcodeX = 70; barcodeY = 115;
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
                barcodeX = 145; barcodeY = 115;
            } else if(si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)){
                barcodeX = 90; barcodeY = 125;
            }
            slcsCmd.append(slcsBarcode(barcodeX, barcodeY, 60, pBarcode));

            // 바코드 타입별 바코드번호(숫자) 출력
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_E0) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_E1) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_M8)) {
                slcsCmd.append(slcsText(75, 240, 25, 25, pBarcodeStr));
            }
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M1)) {
                slcsCmd.append(slcsText(147, 240, 25, 25, pBarcodeStr));
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_E2)) {
                slcsCmd.append(slcsText(100, 240, 25, 25, pBarcodeStr));
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_E3)) {
                slcsCmd.append(slcsText(190, 240, 25, 25, pBarcodeStr));
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3)) {
                slcsCmd.append(slcsText(25, 175, 25, 25, pBarcodeStr + "  PC매입"));
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
                slcsCmd.append(slcsText(117, 175, 25, 25, pBarcodeStr + "  PC매입"));
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)) {
                slcsCmd.append(slcsText(90, 192, 25, 25, pBarcodeStr));
            }

            // M3, M4, M9 추가 바코드 출력
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3)) {
                slcsCmd.append(slcsBarcode(70, 205, 60, pBarcode2));
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
                slcsCmd.append(slcsBarcode(145, 205, 60, pBarcode2));
            } else if(si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)){
                slcsCmd.append(slcsBarcode(125, 325, 60, pBarcode2));
                String ctName = si.getCT_NAME();
                slcsCmd.append(slcsText(450, 330, 25, 25, ctName));
                slcsCmd.append(slcsText(125, 390, 25, 25, pBarcodeStr2));
                String belowBarcodeString = si.EMARTITEM +","+si.getUSE_NAME();
                slcsCmd.append(slcsText(80, 420, 25, 25, belowBarcodeString));
            }

            // M3, M4 PC출하 텍스트 출력
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3)) {
                slcsCmd.append(slcsText(25, 265, 25, 25, pBarcodeStr2 + "  PC출하"));
            } else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
                Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
                slcsCmd.append(slcsText(117, 265, 25, 25, pBarcodeStr2 + "  PC출하"));
            }

            // 바코드 타입별 중량, 납품일자, 업체 정보 출력
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
                slcsCmd.append(slcsText(15, 300, 40, 40, "중     량 : "));
                slcsCmd.append(slcsText(175, 300, 40, 40, String.valueOf(print_weight_double) + " KG"));
                String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
                slcsCmd.append(slcsText(15, 348, 30, 30, "납품일자 : " + tempDate));
                if (reprint) {
                    pCompName = pCompName + "  *";
                }
                slcsCmd.append(slcsText(15, 388, 30, 30, "업        체 : " + pCompCode + "   " + pCompName));
                slcsCmd.append(slcsText(15, 428, 30, 30, expiryDayConvert));          // 소비기한

            } else if(si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)) {
                Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
                String tempDate = si.getSTORE_IN_DATE().substring(0, 4) + "년 " + si.getSTORE_IN_DATE().substring(4, 6) + "월 " + si.getSTORE_IN_DATE().substring(6, 8) + "일";
                slcsCmd.append(slcsText(90, 220, 30, 30, "납품일 : " + tempDate));

            } else {
                slcsCmd.append(slcsText(15, 280, 40, 40, "중      량 : "));
                slcsCmd.append(slcsText(175, 280, 40, 40, String.valueOf(print_weight_double) + " KG"));
                Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
                String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
                slcsCmd.append(slcsText(15, 328, 30, 30, "납품일자 : " + tempDate));
                if (reprint) {
                    pCompName = pCompName + "  *";
                }
                slcsCmd.append(slcsText(15, 368, 30, 30, "업체코드 : " + pCompCode + expiryDayConvert));
                slcsCmd.append(slcsText(15, 408, 30, 30, "업 체 명 : " + pCompName));
            }

            // WH_AREA 출력
            whArea = si.getWH_AREA();
            Log.e(TAG, "::::::::: whArea check44 ::::::::"+whArea);
            if(whArea != null || !whArea.equals("")){
                slcsCmd.append(slcsText(430, 385, 65, 65, whArea));
            }

            // M9 가로선 그리기
            if(si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)) {
                slcsCmd.append(slcsLine(0, 260, 560, 260, 5));
            }

            // 인쇄 실행
            slcsCmd.append(slcsPrint(1));

            if( !si.getBARCODE_TYPE().equals(BARCODE_TYPE_P0) ) {
                callback.sendData(slcsCmd.toString().getBytes("EUC-KR"));
            }

            // ========== 이마트 미트센터 +공장코드 라벨 (SLCS) ==========
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0) && si.getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE) && si.getEMARTLOGIS_CODE().equals(LOGIS_CODE_DEFAULT) && !si.getEMART_PLANT_CODE().equals("")) {
                System.out.println(">>>>>>>>>>>>>>> 이마트 미트센터 +공장코드 >>>>>>>>>>>>>>>");

                try {
                    StringBuilder slcsMeat = new StringBuilder();
                    slcsMeat.append(slcsInit());
                    slcsMeat.append(slcsLabelSize(576, 460));

                    String meatCenterTitle = "ERP-미트센터출하코드";
                    String meatCenterCode = MEAT_CENTER_CODE;
                    String meatCenterBarcodeStr = "";

                    slcsMeat.append(slcsText(120, 35, 40, 40, meatCenterTitle));

                    if (si.EMARTITEM.length() > 14) {
                        slcsMeat.append(slcsText(115, 120, 35, 35, si.EMARTITEM));
                    } else {
                        slcsMeat.append(slcsText(115, 120, 40, 40, si.EMARTITEM));
                    }

                    meatCenterBarcode = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + meatCenterCode + si.getIMPORT_ID_NO() + si.getEMART_PLANT_CODE();
                    meatCenterBarcodeStr = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + meatCenterCode + " " + si.getIMPORT_ID_NO() + " " + si.getEMART_PLANT_CODE();

                    Log.i(TAG, "===============MEATCENTERBARCODE128============" + meatCenterBarcode);

                    slcsMeat.append(slcsBarcode(35, 170, 60, meatCenterBarcode));
                    slcsMeat.append(slcsText(40, 240, 25, 25, meatCenterBarcodeStr));

                    slcsMeat.append(slcsText(15, 280, 40, 40, "중      량 : "));
                    slcsMeat.append(slcsText(175, 280, 40, 40, String.valueOf(print_weight_double) + " KG"));

                    Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
                    String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
                    slcsMeat.append(slcsText(15, 328, 30, 30, "납품일자 : " + tempDate));

                    slcsMeat.append(slcsText(15, 368, 30, 30, "업체코드 : " + meatCenterCode + expiryDayConvert));
                    slcsMeat.append(slcsText(15, 408, 30, 30, "업 체 명 : " + pCompName));

                    whArea = si.getWH_AREA();
                    Log.e(TAG, "::::::::: whArea check44 ::::::::"+whArea);
                    if(whArea != null || !whArea.equals("")){
                        slcsMeat.append(slcsText(430, 385, 65, 65, whArea));
                    }

                    slcsMeat.append(slcsPrint(1));
                    callback.sendData(slcsMeat.toString().getBytes("EUC-KR"));
                } catch (Exception e) {
                    Log.d(TAG, "이마트 공장코드 출력 오류 " +  e.getMessage());
                    e.printStackTrace();
                }
            }

            // ========== 이마트 미트센터 라벨 (SLCS) ==========
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0) && si.getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE) && !si.getEMARTLOGIS_CODE().equals(LOGIS_CODE_DEFAULT) && si.getEMART_PLANT_CODE().equals("")) {
                try {
                    StringBuilder slcsMeat2 = new StringBuilder();
                    slcsMeat2.append(slcsInit());
                    slcsMeat2.append(slcsLabelSize(576, 460));

                    String meatCenterTitle = "미트센터출하코드";
                    String meatCenterCode = MEAT_CENTER_CODE;
                    String meatCenterBarcodeStr = "";

                    slcsMeat2.append(slcsText(150, 35, 40, 40, meatCenterTitle));

                    if (si.EMARTITEM.length() > 14) {
                        slcsMeat2.append(slcsText(80, 120, 35, 35, si.EMARTITEM));
                    } else {
                        slcsMeat2.append(slcsText(80, 120, 40, 40, si.EMARTITEM));
                    }

                    meatCenterBarcode = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + meatCenterCode + si.getIMPORT_ID_NO();
                    meatCenterBarcodeStr = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + meatCenterCode + " " + si.getIMPORT_ID_NO();

                    Log.i(TAG, "===============MEATCENTERBARCODE128============" + meatCenterBarcode);

                    slcsMeat2.append(slcsBarcode(80, 170, 60, meatCenterBarcode));
                    slcsMeat2.append(slcsText(75, 240, 25, 25, meatCenterBarcodeStr));

                    slcsMeat2.append(slcsText(15, 280, 40, 40, "중      량 : "));
                    slcsMeat2.append(slcsText(175, 280, 40, 40, String.valueOf(print_weight_double) + " KG"));

                    Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
                    String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
                    slcsMeat2.append(slcsText(15, 328, 30, 30, "납품일자 : " + tempDate));

                    slcsMeat2.append(slcsText(15, 368, 30, 30, "업체코드 : " + meatCenterCode + expiryDayConvert));
                    slcsMeat2.append(slcsText(15, 408, 30, 30, "업 체 명 : " + pCompName));

                    whArea = si.getWH_AREA();
                    Log.e(TAG, "::::::::: whArea check44 ::::::::"+whArea);
                    if(whArea != null || !whArea.equals("")){
                        slcsMeat2.append(slcsText(430, 385, 65, 65, whArea));
                    }

                    slcsMeat2.append(slcsPrint(1));
                    callback.sendData(slcsMeat2.toString().getBytes("EUC-KR"));
                } catch (Exception e) {
                    Log.d(TAG, "이마트 미트센터 출력 오류 " +  e.getMessage());
                    e.printStackTrace();
                }
            }
            callback.clearBarcodeInput();
        } catch (Exception e) {
            e.printStackTrace();
            if (Common.D) {
                Log.d(TAG, "setPrinting Exception\n" + e.getMessage());
            }
        }
        return String.valueOf(print_weight_double);
    }

    /**
     * 생산 투입용 바코드 라벨 인쇄
     * <p>
     * 생산 라벨 출력 시 Bixolon 블루투스 프린터로 바코드 라벨을 인쇄한다.
     * 상품명, 상품코드, 중량만 표시하는 기본 바코드 형식.
     * </p>
     *
     * @param weight_double 계근 중량 (소수점 2자리)
     * @param si 출하 대상 정보 (Shipments_Info)
     * @param reprint 재인쇄 여부
     * @param callback 프린터 콜백
     * @return 인쇄에 사용된 중량 문자열
     */
    public String setPrinting_prod(double weight_double, Shipments_Info si, boolean reprint,
                                   PrinterCallback callback){
        Log.e(TAG, "======================::::::::: setPrinting_prod ::::::::======================");
        if (Common.D) {
            Log.d(TAG, "'\n상품명 : '" + si.EMARTITEM + "'\n상품코드 : '" + si.EMARTITEM_CODE + "'\n중량 : '" + weight_double + "'");
        }

        String pBarcode = "";
        String pBarcodeStr = "";

        String weight_str = String.valueOf(weight_double);
        Log.d(TAG, "weight_str : " + weight_str);

        String print_weight_str = String.valueOf(weight_double);
        Log.d(TAG, "print_weight_str : " + print_weight_str);

        // 소수점 둘째자리까지 채우기
        String weight_00 = "";
        DecimalFormat decimalFormat = new DecimalFormat("###.00");
        weight_00 = decimalFormat.format(weight_double);
        Log.d(TAG, "weight_00 : " + weight_00);

        // .을 지워서 숫자만으로 표시
        String temp = weight_00.replace(".", "");
        int iLen = temp.length();

        for (int i = 0; i < 6 - iLen; i++) {
            temp = "0" + temp;            // ex) 198 -> 000198
        }
        print_weight_str = temp;

        // 바코드 형식 : 상품코드 + 중량 6자리 + 00 + 연월일시분초
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssSS");
        Date time = new Date();
        String now = dateFormat.format(time);

        pBarcode = si.getEMARTITEM_CODE() + print_weight_str + "00" + now;
        pBarcodeStr = si.getEMARTITEM_CODE() + print_weight_str + "00" + now;

        Log.d(TAG, "** 바코드 :상품코드 + 중량 + 00 + 연월일시분초 = " + si.getEMARTITEM_CODE() + " + " + print_weight_str + " + 00 + " + now);


        // ========== SLCS 명령어로 라벨 인쇄 (Bixolon 프린터) ==========
        try {
            StringBuilder slcsCmd = new StringBuilder();

            // 프린터 초기화 (버퍼 클리어 + 한글 설정)
            slcsCmd.append(slcsInit());

            // 라벨 크기 설정 (너비 576, 높이 460)
            slcsCmd.append(slcsLabelSize(576, 460));

            //------------------------상품명 / 냉장냉동------------------------
            // 상품명이 일정 길이 이상 넘어갈 경우 글자 크기 조절
            if (si.EMARTITEM.length() > 14) {
                slcsCmd.append(slcsText(50, 120, 35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC));
            } else {
                slcsCmd.append(slcsText(50, 120, 40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC));
            }
            Log.i(TAG, "write------------------------------------>상품명 / 냉장냉동 : " + si.EMARTITEM + " / " + si.ITEM_SPEC);

            //------------------------바코드------------------------
            slcsCmd.append(slcsBarcode(50, 190, 60, pBarcode));
            Log.i(TAG, "write------------------------------------>바코드 : " + pBarcode);

            //------------------------바코드 번호------------------------
            slcsCmd.append(slcsText(45, 260, 25, 25, pBarcodeStr));
            Log.i(TAG, "write------------------------------------>바코드번호 : " + pBarcodeStr);

            //------------------------중량------------------------
            slcsCmd.append(slcsText(50, 340, 40, 40, "중      량   :   " + weight_str + " KG"));
            Log.i(TAG, "write------------------------------------>중량 : " + weight_str);

            // 인쇄 실행 (1장)
            slcsCmd.append(slcsPrint(1));

            // SLCS 명령어 전송
            callback.sendData(slcsCmd.toString().getBytes("EUC-KR"));

            callback.clearBarcodeInput();
        } catch (Exception e) {
            e.printStackTrace();
            if (Common.D) {
                Log.d(TAG, "setPrinting Exception\n" + e.getMessage().toString());
            }
        }
        return String.valueOf(weight_double);
    }

}
