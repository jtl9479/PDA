package com.rgbsolution.highland_emart.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Barcodes_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.io.ByteArrayOutputStream;
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
    private static final String BARCODE_TYPE_M8 = "M8";
    private static final String BARCODE_TYPE_M9 = "M9";

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

    // Korail 폰트
    private static Typeface customFont = null;

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
        // B1 x,y,barcode_type,narrow,wide,height,rotation,HRI,'data'
        // barcode_type: 1=CODE128, narrow=2, wide=3, HRI=0(없음)
        return "B1" + x + "," + y + ",1,2,3," + height + ",0,0,'" + data + "'\r\n";
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
     * 검은 사각형을 비트맵(LD 명령)으로 그린다.
     * LS/LB 명령이 SPP-L3000 에서 검은 영역으로 인쇄되어, 이마트 텍스트와 같은 LD 비트맵 경로로 선·테두리를 그린다.
     *
     * @param x      X 좌표
     * @param y      Y 좌표
     * @param width  너비 (dot)
     * @param height 높이 (dot)
     * @return LD 비트맵 명령 바이트
     */
    private byte[] slcsBitmapRect(int x, int y, int width, int height) {
        int widthBytes = (width + 7) / 8;
        byte[] bitmapData = new byte[widthBytes * height];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                bitmapData[row * widthBytes + (col / 8)] |= (1 << (7 - (col % 8)));
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write("LD".getBytes());
            out.write((byte) (x & 0xFF));
            out.write((byte) ((x >> 8) & 0xFF));
            out.write((byte) (y & 0xFF));
            out.write((byte) ((y >> 8) & 0xFF));
            out.write((byte) (widthBytes & 0xFF));
            out.write((byte) ((widthBytes >> 8) & 0xFF));
            out.write((byte) (height & 0xFF));
            out.write((byte) ((height >> 8) & 0xFF));
            out.write(bitmapData);
        } catch (Exception e) {
            Log.e("LabelPrintHelper", "LD 사각형 명령 생성 실패: " + e.getMessage());
        }
        return out.toByteArray();
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

    /**
     * SLCS 명령(StringBuilder)의 인쇄 명령(P1) 바로 앞에 글자 비트맵(LD) 데이터를 끼워 전송용 바이트로 합친다.
     * 롯데·홈플러스·미트센터·생산 라벨의 글자를 이마트 라벨과 같은 Korail.ttf 비트맵으로 인쇄하기 위해 사용한다.
     *
     * @param cmd      SLCS 명령 (인쇄·피드 명령 포함)
     * @param textData 글자 비트맵(LD) 명령 바이트
     * @return 전송용 바이트
     */
    /**
     * 글자를 Korail.ttf 비트맵(LD 명령)으로 만든다. 화면(Activity)에서 직접 조립하는 라벨(합계 라벨)용.
     *
     * @param x    X 좌표
     * @param y    Y 좌표
     * @param size 글자 크기
     * @param text 출력할 텍스트
     * @return LD 비트맵 명령 바이트
     */
    public byte[] bitmapText(int x, int y, int size, String text) {
        return slcsBitmapText(x, y, size, text, true);
    }

    private byte[] withBitmapText(StringBuilder cmd, ByteArrayOutputStream textData) throws Exception {
        String command = cmd.toString();
        int printIdx = command.lastIndexOf(slcsPrint(1));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(command.substring(0, printIdx).getBytes("EUC-KR"));
        out.write(textData.toByteArray());
        out.write(command.substring(printIdx).getBytes("EUC-KR"));
        return out.toByteArray();
    }

    /**
     * 커스텀 폰트(Korail) 로드
     */
    public static void loadCustomFont(Context context) {
        if (customFont == null) {
            try {
                customFont = Typeface.createFromAsset(context.getAssets(), "Korail.ttf");
                Log.d("LabelPrintHelper", "코레일 폰트 로드 성공");
            } catch (Exception e) {
                Log.e("LabelPrintHelper", "폰트 로드 실패, 기본 폰트 사용: " + e.getMessage());
                customFont = Typeface.DEFAULT_BOLD;
            }
        }
    }

    /**
     * 텍스트를 비트맵으로 렌더링 후 SLCS LD 명령으로 변환
     *
     * @param x      X 좌표
     * @param y      Y 좌표
     * @param size   폰트 크기 (도트)
     * @param text   출력할 텍스트
     * @param bold   굵게 여부
     * @return SLCS LD 명령 바이트 배열
     */
    private byte[] slcsBitmapText(int x, int y, int size, String text, boolean bold) {
        if (text == null || text.isEmpty()) return new byte[0];

        // Paint 설정
        Paint paint = new Paint();
        paint.setAntiAlias(false);  // 프린터 출력용 - 안티앨리어싱 OFF (1bpp 흑백)
        paint.setTextSize(size);
        paint.setColor(Color.BLACK);
        paint.setTypeface(customFont != null ? customFont : Typeface.DEFAULT_BOLD);
        if (bold) {
            paint.setFakeBoldText(true);
        }

        // 텍스트 크기 측정
        int textWidth = (int) Math.ceil(paint.measureText(text));
        Paint.FontMetrics fm = paint.getFontMetrics();
        int textHeight = (int) Math.ceil(fm.descent - fm.ascent);
        if (textWidth <= 0 || textHeight <= 0) return new byte[0];

        // 비트맵 생성 및 텍스트 그리기
        Bitmap bitmap = Bitmap.createBitmap(textWidth, textHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawText(text, 0, -fm.ascent, paint);

        // 비트맵을 1bpp 데이터로 변환 (흑백, 검정=1)
        int widthBytes = (textWidth + 7) / 8;
        byte[] bitmapData = new byte[widthBytes * textHeight];

        for (int row = 0; row < textHeight; row++) {
            for (int col = 0; col < textWidth; col++) {
                int pixel = bitmap.getPixel(col, row);
                int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                if (gray < 128) { // 검정
                    int byteIndex = row * widthBytes + (col / 8);
                    int bitIndex = 7 - (col % 8);
                    bitmapData[byteIndex] |= (1 << bitIndex);
                }
            }
        }
        bitmap.recycle();

        // LD 명령 구성: LD xL xH yL yH dhL dhH dvL dvH d1~dk
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write("LD".getBytes());
            out.write((byte) (x & 0xFF));         // xL
            out.write((byte) ((x >> 8) & 0xFF));   // xH
            out.write((byte) (y & 0xFF));         // yL
            out.write((byte) ((y >> 8) & 0xFF));   // yH
            out.write((byte) (widthBytes & 0xFF));  // dhL
            out.write((byte) ((widthBytes >> 8) & 0xFF)); // dhH
            out.write((byte) (textHeight & 0xFF)); // dvL
            out.write((byte) ((textHeight >> 8) & 0xFF)); // dvH
            out.write(bitmapData);
        } catch (Exception e) {
            Log.e("LabelPrintHelper", "LD 명령 생성 실패: " + e.getMessage());
        }

        return out.toByteArray();
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
     *   <li>M8: 수입식별번호 포함</li>
     *   <li>M9: 납품일자 포함</li>
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

            expiryDayConvert = "/소비기한 : "+YYYYMMDD;

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

                expiryDayConvert = "/소비기한: "+YYYYMMDD;
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
            case "M9":
                // 비정량 이마트 M9 (문의사항05 답변: 정량 이마트 우육 센터납 임시 사용 종료)
                // 상품코드 앞자리 6자리 + 중량 6자리 + 회사코드 6자리 = 18자리
                if (Common.D) {
                    Log.e(TAG, "::::::::: M9 (비정량 이마트) ::::::::");
                    Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode);
                }

                pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode;
                pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;

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
            ByteArrayOutputStream labelData = new ByteArrayOutputStream();
            labelData.write(slcsInit().getBytes("EUC-KR"));                          // 프린터 초기화
            labelData.write(slcsLabelSize(576, 460).getBytes("EUC-KR"));             // 라벨 크기 설정

            // 센터명 출력
            if (7 < si.CENTERNAME.length()) {
                labelData.write(slcsBitmapText(20, 12, 35, si.CENTERNAME, true));
                if (Common.D)
                    Log.i(TAG, "센터명 > 7 ,  size 30");
            } else {
                labelData.write(slcsBitmapText(20, 10, 40, si.CENTERNAME, true));
                if (Common.D)
                    Log.i(TAG, "센터명 <= 7 ,  size 40");
            }

            // 업체명/지점명 출력
            if (11 < si.CLIENTNAME.toString().length()) {
                labelData.write(slcsBitmapText(20, 60, 35, pointName.toString(), true));          // 지점명 출력
                if (Common.D)
                    Log.i(TAG, "지점명 > 11 ,  size 30");
            } else {
                labelData.write(slcsBitmapText(20, 60, 40, pointName.toString(), true));          // 지점명 출력
                if (Common.D)
                    Log.i(TAG, "지점명 <= 11 ,  size 40");
            }

            // 상품명 출력 (위치, 크기)
            int itemX = 80, itemY = 120;  // 기본 위치
            if (si.EMARTITEM.length() > 14) {
                labelData.write(slcsBitmapText(itemX, itemY, 35, si.EMARTITEM, true));
            } else {
                labelData.write(slcsBitmapText(itemX, itemY, 40, si.EMARTITEM, true));
            }

            Log.i(TAG, "===============EMARTITEM============" + si.EMARTITEM);
            Log.i(TAG, "===============sBarcode============" + sBarcode);

            // sBarcode 바코드 출력
            labelData.write(slcsBarcode(420, 20, 60, sBarcode).getBytes("EUC-KR"));

            Log.i(TAG, "===============sBarcode2============" + sBarcodeStr);

            // sBarcodeStr 텍스트 출력
            labelData.write(slcsBitmapText(450, 80, 25, sBarcodeStr, true));      // 바코드번호(숫자) 출력

            Log.i(TAG, "===============pBarcode============" + pBarcode);
            Log.i(TAG, "===============pBarcode2============" + pBarcode2);

            // 바코드 타입별 메인 바코드 출력 위치 설정
            int barcodeX = 80, barcodeY = 170;  // 기본 위치
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_M8)) {
                barcodeX = 80; barcodeY = 170;
            }
            labelData.write(slcsBarcode(barcodeX, barcodeY, 60, pBarcode).getBytes("EUC-KR"));

            // 바코드 타입별 바코드번호(숫자) 출력
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_M8) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)) {
                labelData.write(slcsBitmapText(75, 240, 20, pBarcodeStr, true));
            }

            // 중량, 납품일자, 업체 정보 출력 (tempDate 가 아래 미트센터 블록과 겹치지 않도록 블록 유지)
            {
                labelData.write(slcsBitmapText(20, 280, 40, "중량 : ", true));
                labelData.write(slcsBitmapText(180, 280, 40, String.valueOf(print_weight_double) + " KG", true));
                Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
                String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
                labelData.write(slcsBitmapText(20, 328, 30, "납품일자 : " + tempDate, true));
                if (reprint) {
                    pCompName = pCompName + "  *";
                }
                labelData.write(slcsBitmapText(20, 368, 30, "업체코드 : " + pCompCode + expiryDayConvert, true));
                labelData.write(slcsBitmapText(20, 408, 30, "업 체 명 : " + pCompName, true));
            }

            // WH_AREA 출력
            whArea = si.getWH_AREA();
            Log.e(TAG, "::::::::: whArea check44 ::::::::"+whArea);
            if(whArea != null || !whArea.equals("")){
                labelData.write(slcsBitmapText(430, 385, 65, whArea, true));
            }

            // 인쇄 실행
            labelData.write(slcsPrint(1).getBytes("EUC-KR"));
            // 라벨 피드 (마크 위치로 이동) - 원본: WoosimCmd.feedToMark()
            labelData.write(slcsFeedToMark().getBytes("EUC-KR"));

            callback.sendData(labelData.toByteArray());

            // ========== 이마트 미트센터 +공장코드 라벨 (SLCS) ==========
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0) && si.getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE) && si.getEMARTLOGIS_CODE().equals(LOGIS_CODE_DEFAULT) && !si.getEMART_PLANT_CODE().equals("")) {
                System.out.println(">>>>>>>>>>>>>>> 이마트 미트센터 +공장코드 >>>>>>>>>>>>>>>");

                try {
                    StringBuilder slcsMeat = new StringBuilder();
                    ByteArrayOutputStream slcsMeatText = new ByteArrayOutputStream(); // 글자 비트맵(Korail.ttf)
                    slcsMeat.append(slcsInit());
                    slcsMeat.append(slcsLabelSize(576, 460));

                    String meatCenterTitle = "ERP-미트센터출하코드";
                    String meatCenterCode = MEAT_CENTER_CODE;
                    String meatCenterBarcodeStr = "";

                    slcsMeatText.write(slcsBitmapText(120, 35, 40, meatCenterTitle, true));

                    if (si.EMARTITEM.length() > 14) {
                        slcsMeatText.write(slcsBitmapText(115, 120, 35, si.EMARTITEM, true));
                    } else {
                        slcsMeatText.write(slcsBitmapText(115, 120, 40, si.EMARTITEM, true));
                    }

                    meatCenterBarcode = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + meatCenterCode + si.getIMPORT_ID_NO() + si.getEMART_PLANT_CODE();
                    meatCenterBarcodeStr = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + meatCenterCode + " " + si.getIMPORT_ID_NO() + " " + si.getEMART_PLANT_CODE();

                    Log.i(TAG, "===============MEATCENTERBARCODE128============" + meatCenterBarcode);

                    slcsMeat.append(slcsBarcode(35, 170, 60, meatCenterBarcode));
                    slcsMeatText.write(slcsBitmapText(40, 240, 25, meatCenterBarcodeStr, true));

                    slcsMeatText.write(slcsBitmapText(15, 280, 40, "중      량 : ", true));
                    slcsMeatText.write(slcsBitmapText(175, 280, 40, String.valueOf(print_weight_double) + " KG", true));

                    Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
                    String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
                    slcsMeatText.write(slcsBitmapText(15, 328, 30, "납품일자 : " + tempDate, true));

                    slcsMeatText.write(slcsBitmapText(15, 368, 30, "업체코드 : " + meatCenterCode + expiryDayConvert, true));
                    slcsMeatText.write(slcsBitmapText(15, 408, 30, "업 체 명 : " + pCompName, true));

                    whArea = si.getWH_AREA();
                    Log.e(TAG, "::::::::: whArea check44 ::::::::"+whArea);
                    if(whArea != null || !whArea.equals("")){
                        slcsMeatText.write(slcsBitmapText(430, 385, 65, whArea, true));
                    }

                    slcsMeat.append(slcsPrint(1));
                    // 라벨 피드 (마크 위치로 이동) - 원본: WoosimCmd.feedToMark()
                    slcsMeat.append(slcsFeedToMark());
                    callback.sendData(withBitmapText(slcsMeat, slcsMeatText));
                } catch (Exception e) {
                    Log.d(TAG, "이마트 공장코드 출력 오류 " +  e.getMessage());
                    e.printStackTrace();
                }
            }

            // ========== 이마트 미트센터 라벨 (SLCS) ==========
            if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0) && si.getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE) && !si.getEMARTLOGIS_CODE().equals(LOGIS_CODE_DEFAULT) && si.getEMART_PLANT_CODE().equals("")) {
                try {
                    StringBuilder slcsMeat2 = new StringBuilder();
                    ByteArrayOutputStream slcsMeat2Text = new ByteArrayOutputStream(); // 글자 비트맵(Korail.ttf)
                    slcsMeat2.append(slcsInit());
                    slcsMeat2.append(slcsLabelSize(576, 460));

                    String meatCenterTitle = "미트센터출하코드";
                    String meatCenterCode = MEAT_CENTER_CODE;
                    String meatCenterBarcodeStr = "";

                    slcsMeat2Text.write(slcsBitmapText(150, 35, 40, meatCenterTitle, true));

                    if (si.EMARTITEM.length() > 14) {
                        slcsMeat2Text.write(slcsBitmapText(80, 120, 35, si.EMARTITEM, true));
                    } else {
                        slcsMeat2Text.write(slcsBitmapText(80, 120, 40, si.EMARTITEM, true));
                    }

                    meatCenterBarcode = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + meatCenterCode + si.getIMPORT_ID_NO();
                    meatCenterBarcodeStr = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + meatCenterCode + " " + si.getIMPORT_ID_NO();

                    Log.i(TAG, "===============MEATCENTERBARCODE128============" + meatCenterBarcode);

                    slcsMeat2.append(slcsBarcode(80, 170, 60, meatCenterBarcode));
                    slcsMeat2Text.write(slcsBitmapText(75, 240, 25, meatCenterBarcodeStr, true));

                    slcsMeat2Text.write(slcsBitmapText(15, 280, 40, "중      량 : ", true));
                    slcsMeat2Text.write(slcsBitmapText(175, 280, 40, String.valueOf(print_weight_double) + " KG", true));

                    Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
                    String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
                    slcsMeat2Text.write(slcsBitmapText(15, 328, 30, "납품일자 : " + tempDate, true));

                    slcsMeat2Text.write(slcsBitmapText(15, 368, 30, "업체코드 : " + meatCenterCode + expiryDayConvert, true));
                    slcsMeat2Text.write(slcsBitmapText(15, 408, 30, "업 체 명 : " + pCompName, true));

                    whArea = si.getWH_AREA();
                    Log.e(TAG, "::::::::: whArea check44 ::::::::"+whArea);
                    if(whArea != null || !whArea.equals("")){
                        slcsMeat2Text.write(slcsBitmapText(430, 385, 65, whArea, true));
                    }

                    slcsMeat2.append(slcsPrint(1));
                    // 라벨 피드 (마크 위치로 이동) - 원본: WoosimCmd.feedToMark()
                    slcsMeat2.append(slcsFeedToMark());
                    callback.sendData(withBitmapText(slcsMeat2, slcsMeat2Text));
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
            ByteArrayOutputStream slcsCmdText = new ByteArrayOutputStream(); // 글자 비트맵(Korail.ttf)

            // 프린터 초기화 (버퍼 클리어 + 한글 설정)
            slcsCmd.append(slcsInit());

            // 라벨 크기 설정 (너비 576, 높이 460)
            slcsCmd.append(slcsLabelSize(576, 460));

            //------------------------상품명 / 냉장냉동------------------------
            // 상품명이 일정 길이 이상 넘어갈 경우 글자 크기 조절
            if (si.EMARTITEM.length() > 14) {
                slcsCmdText.write(slcsBitmapText(50, 120, 35, si.EMARTITEM + " / " + si.ITEM_SPEC, true));
            } else {
                slcsCmdText.write(slcsBitmapText(50, 120, 40, si.EMARTITEM + " / " + si.ITEM_SPEC, true));
            }
            Log.i(TAG, "write------------------------------------>상품명 / 냉장냉동 : " + si.EMARTITEM + " / " + si.ITEM_SPEC);

            //------------------------바코드------------------------
            slcsCmd.append(slcsBarcode(50, 190, 60, pBarcode));
            Log.i(TAG, "write------------------------------------>바코드 : " + pBarcode);

            //------------------------바코드 번호------------------------
            slcsCmdText.write(slcsBitmapText(45, 260, 25, pBarcodeStr, true));
            Log.i(TAG, "write------------------------------------>바코드번호 : " + pBarcodeStr);

            //------------------------중량------------------------
            slcsCmdText.write(slcsBitmapText(50, 340, 40, "중      량   :   " + weight_str + " KG", true));
            Log.i(TAG, "write------------------------------------>중량 : " + weight_str);

            // 인쇄 실행 (1장)
            slcsCmd.append(slcsPrint(1));
            // 라벨 피드 (마크 위치로 이동) - 원본: WoosimCmd.feedToMark()
            slcsCmd.append(slcsFeedToMark());

            // SLCS 명령어 전송
            callback.sendData(withBitmapText(slcsCmd, slcsCmdText));

            callback.clearBarcodeInput();
        } catch (Exception e) {
            e.printStackTrace();
            if (Common.D) {
                Log.d(TAG, "setPrinting_prod Exception\n" + e.getMessage().toString());
            }
        }
        return String.valueOf(weight_double);
    }

    /**
     * 홈플러스 출하용 바코드 라벨 인쇄
     * <p>
     * 홈플러스 비정량 출하 시 Bixolon 블루투스 프린터로 바코드 라벨을 인쇄한다.
     * 지점명, 점포코드, 상품명, 중량, 납품일자, 업체명을 표시한다.
     * </p>
     *
     * <h3>라벨 정보</h3>
     * <ul>
     *   <li>업체명: (주)하이랜드이노베이션</li>
     *   <li>상품명, 중량, 지점코드, 점포코드 표시</li>
     *   <li>소수점 2자리까지 중량 표시</li>
     * </ul>
     *
     * @param weight_double 계근 중량
     * @param si 출하 대상 정보 (Shipments_Info)
     * @param reprint 재인쇄 여부
     * @param callback 프린터 콜백
     * @return 인쇄에 사용된 중량 문자열
     */
    public String setHomeplusPrinting(double weight_double, Shipments_Info si, boolean reprint,
                                      PrinterCallback callback) {
        if (Common.D) {
            Log.d(TAG, "센터명 : '" + si.CENTERNAME + "'\n출고업체명 : '" + si.CLIENTNAME + "'\n이마트상품명 : '"
                    + si.EMARTITEM + "'\n중량 : '" + weight_double + "'");
        }

        Log.d(TAG, "===========홈플 출력 시작 ================");

        String pointCode = "";                // 지점코드
        String storeCode = "";                // 점포코드(홈플러스 비정량)
        String pointName = "";                // 지점명
        String pCompName = COMPANY_NAME;

        //소수점 한자리 이후 절사
        String print_weight_str = "";
        Double print_weight_double = 0.0;
        String weight_ = String.valueOf(weight_double);
        String weight_str = String.valueOf(weight_double);
        String[] weight_sp = weight_str.split("\\.");
        String print_weight = "";

        if(weight_sp[1].length() > 1){
            print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 2);
        }else if(weight_sp[1].length() == 1){
            print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 1);
        }else{
            print_weight = weight_sp[0];
        }

        weight_double = Double.parseDouble(print_weight);

        print_weight_double = weight_double;

        pointCode = si.EMARTLOGIS_CODE.toString();
        storeCode = si.STORE_CODE.toString();
        pointName = si.CLIENTNAME.toString();

        // ========== SLCS 명령어로 홈플러스 라벨 인쇄 (Bixolon 프린터) ==========
        // 원본: Woosim ByteArrayOutputStream + WoosimCmd 명령어
        // 변환: StringBuilder + SLCS 헬퍼 메서드
        // 라벨 레이아웃: 세로 방향 (원본 PM_setDirection(1))
        try {
            StringBuilder slcsCmd = new StringBuilder();
            ByteArrayOutputStream slcsCmdText = new ByteArrayOutputStream(); // 글자 비트맵(Korail.ttf)
            slcsCmd.append(slcsInit());                                              // 프린터 초기화 (CB + CS13,0)
            slcsCmd.append(slcsLabelSize(576, 590));                                 // 라벨 크기: 가로 576(용지 폭, 510 이면 x=510 이후 글자 잘림), 세로 590 (원본 PM_setArea)
            // 참고: 원본 PM_setDirection(1) - SLCS에서는 좌표 체계로 회전 효과 구현

            // [1] 지점명 출력 - 위치(30, 170)
            // 원본: PM_setPosition(30, 170) + getTTFcode(70 or 100)
            // 6자 초과 시 크기 70, 이하 시 크기 100 (긴 이름은 작게)
            if(pointName.length() > 6) {
                slcsCmdText.write(slcsBitmapText(170, 30, 70, pointName.toString(), true));     // 6자 초과: 크기 70
            } else {
                slcsCmdText.write(slcsBitmapText(170, 30, 100, pointName.toString(), true));   // 6자 이하: 크기 100
            }

            // [2] 점포코드/지점코드 출력 - 위치(135, 170), 크기 155
            // 원본: PM_setPosition(135, 170) + getTTFcode(155, 155)
            // ITEM_TYPE_B(비정량)이면 storeCode, 아니면 pointCode 출력
            if (si.getITEM_TYPE().equals(ITEM_TYPE_B)) {
                slcsCmdText.write(slcsBitmapText(170, 135, 155, storeCode.toString(), true));  // 비정량: 점포코드(STORE_CODE)
            } else {
                slcsCmdText.write(slcsBitmapText(170, 135, 155, pointCode.toString(), true));  // 정량: 지점코드(EMARTLOGIS_CODE)
            }

            // [3] 상품명 출력 - 위치(287 or 283, 170)
            // 원본: PM_setPosition + getTTFcode
            // 17자 초과 시 크기 25, 이하 시 크기 30 (긴 상품명은 작게)
            if (si.EMARTITEM.length() > 17) {
                slcsCmdText.write(slcsBitmapText(170, 287, 25, si.EMARTITEM, true));            // 17자 초과: 크기 25
            } else {
                slcsCmdText.write(slcsBitmapText(170, 283, 30, si.EMARTITEM, true));            // 17자 이하: 크기 30
            }

            // [4] BOX 텍스트 - 위치(322, 170), 크기 40
            slcsCmdText.write(slcsBitmapText(170, 322, 40, "BOX", true));

            // [5] CT코드 (차량코드) - 위치(361, 170), 크기 40
            slcsCmdText.write(slcsBitmapText(170, 361, 40, String.valueOf(si.getCT_CODE()), true));

            // [6] 중량/수입식별번호 - 위치(361, 380), 크기 40
            // 형식: "중량/수입식별번호 뒤 4자리"
            slcsCmdText.write(slcsBitmapText(380, 361, 40, String.valueOf(print_weight_double) + "/"+si.getIMPORT_ID_NO().substring(8, 12), true));

            // [7] 납품일자 - 위치(402, 170), 크기 40
            // 형식: "YYYY년 MM월 DD일"
            Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
            String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
            slcsCmdText.write(slcsBitmapText(170, 402, 40, tempDate, true));

            // [8] 업체명 - 위치(441, 170), 크기 40
            // 값: COMPANY_NAME 상수 ("(주)하이랜드이노베이션")
            slcsCmdText.write(slcsBitmapText(170, 441, 40, pCompName, true));

            // [9] 인쇄 실행 - 1장 출력
            slcsCmd.append(slcsPrint(1));
            // 라벨 피드 (마크 위치로 이동) - 원본: WoosimCmd.feedToMark()
            slcsCmd.append(slcsFeedToMark());

            // SLCS 명령어를 EUC-KR 인코딩으로 프린터에 전송
            callback.sendData(withBitmapText(slcsCmd, slcsCmdText));
            callback.clearBarcodeInput();  // 바코드 입력창 초기화
        } catch (Exception e) {
            e.printStackTrace();
            if (Common.D) {
                Log.d(TAG, "setHomeplusPrinting Exception\n" + e.getMessage());
            }
        }
        return String.valueOf(print_weight_double);
    }

    /**
     * 롯데 출하용 바코드 라벨 인쇄
     * <p>
     * 롯데 유통 출하 시 사용하는 바코드 라벨 인쇄.
     * 롯데 전용 바코드 포맷과 박스 순번(box_order)을 포함한다.
     * </p>
     *
     * <h3>라벨 정보</h3>
     * <ul>
     *   <li>업체명: (주)하이랜드이노베이션</li>
     *   <li>업체코드: EMARTLOGIS_CODE에서 가져옴</li>
     *   <li>상품명, 중량, 제조일자, 박스 순번 표시</li>
     *   <li>소수점 2자리까지 중량 표시</li>
     *   <li>박스 순번(lotte_TryCount)으로 바코드 시퀀스 관리</li>
     * </ul>
     *
     * @param weight_double 계근 중량
     * @param si 출하 대상 정보 (Shipments_Info)
     * @param reprint 재인쇄 여부
     * @param making_date 제조일자
     * @param box_order 박스 순번 (바코드 시퀀스용)
     * @param searchType 검색 유형 - 기존 Common.searchType
     * @param callback 프린터 콜백
     * @return 인쇄에 사용된 중량 문자열
     */
    public String setPrintingLotte(double weight_double, Shipments_Info si, boolean reprint, String making_date, String box_order,
                                   String searchType, PrinterCallback callback){
        if (Common.D) {
            Log.d(TAG, "센터명 : '" + si.CENTERNAME + "'\n출고업체명 : '" + si.CLIENTNAME + "'\n이마트상품명 : '"
                    + si.EMARTITEM + "'\n중량 : '" + weight_double + "' \n제조일자 : '" + making_date + "'");
        }

        String pointName = "";                // 이마트 지점명
        String pCompName = COMPANY_NAME;
        String pBarcode = "";
        String pBarcodeStr = "";
        String pBarcode2 = "";
        String pBarcodeStr2 = "";
        String whArea = "";
        String pCompCode_lotte = si.EMARTLOGIS_CODE; // 롯데전용 업체코드 뷰에서 EMARTLOGIS_CODE로 받아옴

        //소수점 한자리 이후 절사
        String print_weight_str = "";
        Double print_weight_double = 0.0;
        String weight_ = String.valueOf(weight_double);
        String weight_str = String.valueOf(weight_double);
        String[] weight_sp = weight_str.split("\\.");
        String print_weight = "";

        if(searchType.equals(SEARCH_TYPE_LOTTE)){
            String chk = weight_sp[1];
            if(chk.length() >=2){
                print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 2); //롯데용, 한자리절사 안함
            }else{
                print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 1); //롯데용, 한자리절사 안함
            }
        }else{
            print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 1); //이마트용 두자리부터 절사
        }
        weight_double = Double.parseDouble(print_weight);

        if (si.getITEM_TYPE().equals(ITEM_TYPE_W) || si.getITEM_TYPE().equals(ITEM_TYPE_S)  ) { //롯데용 임시
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
        int iLen = temp.length();

        Log.d(TAG, "LENGTH TEST !!!! : "+iLen);

        //원앤원은 17.7kg는 001770, 17.36kg는 001736 이렇게 위쪽 바코드에 찍어줘야되는데 소수점 두째자리가 아닌 첫째짜리까지 있는 건의 경우 000177 처럼 표기되는 문제가 있어 아래 로직 추가함(2020.09.01)
        if(iLen == 4){ //17.36의 경우
            for (int i = 0; i < 6 - iLen; i++) {
                temp = "0" + temp;            // ex) 198 -> 000198
            }
        }else if(iLen == 3){ //17.7의 경우
            for (int i = 0; i < 5 - iLen; i++) {
                temp = "0" + temp;            // ex) 198 -> 001980
            }
            // 2가지 경우로 나누기 10 이상인 경우, 10미만인 경우
            if(print_weight_double >= 10) { // 10이상 인경우 3자리는 무조건 소수점 한자리임 뒤에 0붙임 ex 001770 : 17.7kg
                temp = temp + "0";
            } else { // 10미만인 경우 3자리는 두자리 소수점임 앞에 0 붙임 ex) 000177 : 1.77kg
                temp = "0"+ temp;
            }
        }else if(iLen == 2){ //17.7의 경우
            for (int i = 0; i < 4 - iLen; i++) {
                temp = "0" + temp;
            }
            // 2가지 경우로 나누기 10 이상인 경우, 10미만 1이상인 경우. 소수점만 있는경우는 에러로 판단하여 없게나오게하기
            if(print_weight_double >= 10) {  //10이상인 경우 2자리는 무조건 소수점없음 ex 001700 : 17kg
                temp = temp + "00";
            } else if(print_weight_double < 10 && print_weight_double > 1) { // 10미만 1이상인 경우 2자리는 한자리 소수점임 ex) 000170 : 1.7kg
                temp = "0" + temp + "0";
            }
        }

        print_weight_str = temp;

        if (Common.D) {
            Log.d(TAG, "중량 6 자리 : " + print_weight_str);
        }
        Log.d(TAG, "============ 바코드 타입 =================== : " + si.getBARCODE_TYPE());
        Log.d(TAG, "============ 바코드 타입 판별 =================== : " + si.getBARCODE_TYPE().equals("L0"));
        switch (si.getBARCODE_TYPE()) {

            case "L0":
                // 롯데상품코드 형식
                // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
                if (Common.D) {
                    Log.e(TAG, "::::::::: L0 ::::::::");
                    Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
                    Log.d(TAG, "중량 6자리 :" + print_weight_str);
                    Log.d(TAG, "회사코드 : " + pCompCode_lotte);
                    Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
                }

                String boxserial_cnt = "";

                //재출력, 신규 출력 상관없이 전달받은 box_order 파라미터 사용
                if (box_order != null && !box_order.isEmpty()) {
                    boxserial_cnt = String.format("%04d", Integer.parseInt(box_order));
                } else {
                    Log.e(TAG, "setPrintingLotte: box_order가 null 또는 empty입니다.");
                    return "";
                }

                Log.d(TAG, "----------------------pBarcode(회사코드+제조일자+중량+마트제품코드+박스번호) : " + pCompCode_lotte+" + "+making_date+" + "+print_weight_str.substring(print_weight_str.length()-4, print_weight_str.length())+" + "+si.getEMARTITEM_CODE().substring(0, 6)+" + " +boxserial_cnt);
                pBarcode = pCompCode_lotte+making_date+print_weight_str.substring(print_weight_str.length()-4, print_weight_str.length())+si.getEMARTITEM_CODE().substring(0, 6) +boxserial_cnt;
                Log.d(TAG, "바코드 확인용 ---------------------- " + pBarcode);
                pBarcodeStr = pCompCode_lotte+making_date+print_weight_str.substring(print_weight_str.length()-4, print_weight_str.length())+si.getEMARTITEM_CODE().substring(0, 6) +boxserial_cnt;
                Log.d(TAG, "바코드 확인용 ---------------------- " + pBarcode);

                pBarcode2 = si.getIMPORT_ID_NO();
                pBarcodeStr2 = si.getIMPORT_ID_NO();

                break;
        }

        String[] split_name = null;
        pointName = si.CLIENTNAME.toString();

        if (split_name != null && split_name.length > 1) {
            pointName = split_name[1].toString();
        }

        if (Common.D) {
            Log.d(TAG, "print Barcode : " + pBarcode.toString());
            Log.d(TAG, "print Weight : " + print_weight_str);
        }

        // ========== SLCS 명령어로 롯데(원앤원) 라벨 인쇄 (Bixolon 프린터) ==========
        // 원본: Woosim ByteArrayOutputStream + WoosimCmd/WoosimBarcode/WoosimImage 명령어
        // 변환: StringBuilder + SLCS 헬퍼 메서드
        // 출력 항목:
        //   [1] 상품명 (10,12) 35x35 - si.EMARTITEM
        //   [2] 바코드1 - 중량바코드 (100,80) CODE128 h=60
        //   [3] 바코드1 숫자 (114,139) 25x25 - pBarcodeStr
        //   [4] 바코드2 - 이력번호바코드 (150,350) CODE128 h=60
        //   [5] 이력번호 숫자 (155,410) 25x25 - pBarcode2
        //   [6] 중량 라벨 (15,180) 40x40 - "중      량 : "
        //   [7] 중량 값 (175,180) 40x40 - print_weight_double + " KG"
        //   [8] 납품처 (15,228) 30x30 - pCompName
        //   [9] 제조일자 (15,268) 30x30 - tempDate
        //   [10] 이력(묶음)번호 (15,313) 30x30 - si.getIMPORT_ID_NO()
        //   [11] WH_AREA (385,305) 65x65 - whArea
        //   [12] 겉 테두리 박스 (0,0)-(560,440) 두께3
        //   [13~15] 가로선 3개 y=60, y=180, y=345 두께3
        try {
            StringBuilder slcsCmd = new StringBuilder();
            ByteArrayOutputStream slcsCmdText = new ByteArrayOutputStream(); // 글자 비트맵(Korail.ttf)
            ByteArrayOutputStream lineData = new ByteArrayOutputStream(); // 선·테두리 LD 비트맵

            // 초기화: CB(버퍼클리어) + CS13,0(한글문자셋)
            // 원본: WoosimCmd.initPrinter() + setPageMode() + selectTTF()
            slcsCmd.append(slcsInit());

            // 라벨 크기 설정: 576x460 도트
            // 원본: WoosimCmd.PM_setArea(0, 0, 576, 460)
            slcsCmd.append(slcsLabelSize(576, 460));

            Log.i(TAG, "===============EMARTITEM============" + si.EMARTITEM);

            // [1] 상품명 출력 (x=10, y=12, 폰트크기 35x35)
            // 원본: PM_setPosition(10, 12) + getTTFcode(35, 35, si.EMARTITEM)
            slcsCmdText.write(slcsBitmapText(10, 12, 35, si.EMARTITEM, true));

            Log.i(TAG, "===============pBarcode============" + pBarcode);
            Log.i(TAG, "===============이력번호============" + pBarcode2);

            // L0 바코드 타입 (롯데/원앤원 전용)
            if (si.getBARCODE_TYPE().equals("L0")) {
                // [2] 중량바코드 출력 (x=100, y=80, CODE128, 높이60)
                // 원본: WoosimBarcode.createBarcode(CODE128, 2, 60, false, pBarcode.getBytes()) at (100,80)
                slcsCmd.append(slcsBarcode(100, 80, 60, pBarcode));

                // [3] 바코드1 숫자 (중량바코드 아래) (x=114, y=139, 폰트크기 25x25)
                // 원본: PM_setPosition(114, 139) + getTTFcode(25, 25, pBarcodeStr)
                slcsCmdText.write(slcsBitmapText(114, 139, 25, pBarcodeStr, true));

                Log.i(TAG, "===============LOGISCODE128============");

                // [4] 이력번호 바코드 출력 (x=150, y=350, CODE128, 높이60)
                // 원본: WoosimBarcode.createBarcode(CODE128, 2, 60, false, pBarcode2.getBytes()) at (150,350)
                slcsCmd.append(slcsBarcode(150, 350, 60, pBarcode2));

                // [5] 이력번호 숫자 (바코드2 아래) (x=155, y=410, 폰트크기 25x25)
                // 원본: PM_setPosition(155, 410) + getTTFcode(25, 25, pBarcode2)
                slcsCmdText.write(slcsBitmapText(155, 410, 25, pBarcode2, true));

                // [6] 중량 라벨 (x=15, y=180, 폰트크기 40x40)
                // 원본: PM_setPosition(15, 180) + getTTFcode(40, 40, "중      량 : ")
                slcsCmdText.write(slcsBitmapText(15, 180, 40, "중      량 : ", true));

                // [7] 중량 값 (x=175, y=180, 폰트크기 40x40)
                // 원본: PM_setPosition(175, 180) + getTTFcode(40, 40, weight + " KG")
                slcsCmdText.write(slcsBitmapText(175, 180, 40, String.valueOf(print_weight_double) + " KG", true));

                // [8] 납품처 (x=15, y=228, 폰트크기 30x30)
                // 원본: PM_setPosition(15, 228) + getTTFcode(30, 30, "납품처 : " + pCompName)
                slcsCmdText.write(slcsBitmapText(15, 228, 30, "납품처 : " + pCompName, true));

                // 재인쇄 표시
                if (reprint) {
                    pCompName = pCompName + "  *";
                }

                Log.i(TAG, "=====================제조일자==================" + making_date);

                // [9] 제조일자 (x=15, y=268, 폰트크기 30x30)
                // 원본: PM_setPosition(15, 268) + getTTFcode(30, 30, "제조일자 : " + tempDate)
                String tempDate = "20" + making_date.substring(0, 2) + "년 " + making_date.substring(2, 4) + "월 " + making_date.substring(4, 6) + "일";
                slcsCmdText.write(slcsBitmapText(15, 268, 30, "제조일자 : " + tempDate, true));

                // [10] 이력(묶음)번호 (x=15, y=313, 폰트크기 30x30)
                // 원본: PM_setPosition(15, 313) + getTTFcode(30, 30, "이력(묶음)번호 : " + ...)
                slcsCmdText.write(slcsBitmapText(15, 313, 30, "이력(묶음)번호 : " + si.getIMPORT_ID_NO(), true));

                // [13~15] 가로선 3개 (L0 바코드 타입 전용)
                // 원본: WoosimImage.drawLine(0, 60, 560, 60, 3) 등
                lineData.write(slcsBitmapRect(0, 60, 560, 3));   // 가로선1 (상품명 아래)
                lineData.write(slcsBitmapRect(0, 180, 560, 3));  // 가로선2 (바코드1 아래)
                lineData.write(slcsBitmapRect(0, 345, 560, 3));  // 가로선3 (중량정보 아래)
            }

            // [11] WH_AREA 출력 (x=385, y=305, 폰트크기 65x65) - 창고구역 코드
            // 원본: PM_setPosition(385, 305) + getTTFcode(65, 65, whArea)
            whArea = si.getWH_AREA();
            Log.e(TAG, "::::::::: whArea check44 ::::::::" + whArea);

            if (whArea != null || !whArea.equals("")) {
                slcsCmdText.write(slcsBitmapText(385, 305, 65, whArea, true));
            }

            // [12] 겉 테두리 박스 (0,0)에서 (560,440) 크기, 두께 3
            // 원본: WoosimImage.drawBox(0, 0, 560, 440, 3)
            lineData.write(slcsBitmapRect(0, 0, 560, 3));    // 위
            lineData.write(slcsBitmapRect(0, 437, 560, 3));  // 아래
            lineData.write(slcsBitmapRect(0, 0, 3, 440));    // 왼쪽
            lineData.write(slcsBitmapRect(557, 0, 3, 440));  // 오른쪽

            // 텍스트·바코드 명령 + 선·테두리 비트맵 + 인쇄 실행(1장) + 라벨 피드
            // 원본: WoosimCmd.PM_printData() + WoosimCmd.feedToMark()
            ByteArrayOutputStream labelData = new ByteArrayOutputStream();
            labelData.write(slcsCmd.toString().getBytes("EUC-KR"));
            labelData.write(slcsCmdText.toByteArray());
            labelData.write(lineData.toByteArray());
            labelData.write(slcsPrint(1).getBytes("EUC-KR"));
            labelData.write(slcsFeedToMark().getBytes("EUC-KR"));

            // 전송
            callback.sendData(labelData.toByteArray());

            callback.clearBarcodeInput();
        } catch (Exception e) {
            e.printStackTrace();
            if (Common.D) {
                Log.d(TAG, "setPrintingLotte Exception\n" + e.getMessage().toString());
            }
        }
        return String.valueOf(print_weight_double);
    }

}
