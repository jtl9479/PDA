package com.rgbsolution.highland_emart.print.label;

import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;

import java.io.ByteArrayOutputStream;

/**
 * 롯데 라벨 — L0 : 롯데(원앤원) 라벨 (회사코드+제조일자+중량4+상품코드6+박스순번4 바코드, 이력번호 바코드, 가로선 3개)
 * LabelPrintHelper.setPrintingLotte 에서 바코드 타입으로 선택되어 호출된다 (개발/75).
 */
public class LabelL0 implements LotteLabel {

    private static final String TAG = "LabelPrintHelper";   // 기존 로그 태그 유지

    private final LabelPrintHelper helper;

    public LabelL0(LabelPrintHelper helper) {
        this.helper = helper;
    }

    @Override
    public String print(Shipments_Info si, boolean reprint, String making_date, String box_order, String print_weight_str, Double print_weight_double, LabelPrintHelper.PrinterCallback callback) {
        String pointName = "";                // 이마트 지점명
        String pCompName = LabelPrintHelper.COMPANY_NAME;
        String pBarcode = "";
        String pBarcodeStr = "";
        String pBarcode2 = "";
        String pBarcodeStr2 = "";
        String whArea = "";
        String pCompCode_lotte = si.EMARTLOGIS_CODE; // 롯데전용 업체코드 뷰에서 EMARTLOGIS_CODE로 받아옴

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
        // StringBuilder + SLCS 헬퍼 메서드
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
            slcsCmd.append(helper.slcsInit());

            // 라벨 크기 설정: 576x460 도트
            slcsCmd.append(helper.slcsLabelSize(576, 460));

            Log.i(TAG, "===============EMARTITEM============" + si.EMARTITEM);

            // [1] 상품명 출력 (x=10, y=12, 폰트크기 35x35)
            slcsCmdText.write(helper.slcsBitmapText(10, 12, 35, si.EMARTITEM, true));

            Log.i(TAG, "===============pBarcode============" + pBarcode);
            Log.i(TAG, "===============이력번호============" + pBarcode2);

            // L0 바코드 타입 (롯데/원앤원 전용)
            // [2] 중량바코드 출력 (x=100, y=80, CODE128, 높이60)
            slcsCmd.append(helper.slcsBarcode(100, 80, 60, pBarcode));

            // [3] 바코드1 숫자 (중량바코드 아래) (x=114, y=139, 폰트크기 25x25)
            slcsCmdText.write(helper.slcsBitmapText(114, 139, 25, pBarcodeStr, true));

            Log.i(TAG, "===============LOGISCODE128============");

            // [4] 이력번호 바코드 출력 (x=150, y=350, CODE128, 높이60)
            slcsCmd.append(helper.slcsBarcode(150, 350, 60, pBarcode2));

            // [5] 이력번호 숫자 (바코드2 아래) (x=155, y=410, 폰트크기 25x25)
            slcsCmdText.write(helper.slcsBitmapText(155, 410, 25, pBarcode2, true));

            // [6] 중량 라벨 (x=15, y=180, 폰트크기 40x40)
            slcsCmdText.write(helper.slcsBitmapText(15, 180, 40, "중      량 : ", true));

            // [7] 중량 값 (x=175, y=180, 폰트크기 40x40)
            slcsCmdText.write(helper.slcsBitmapText(175, 180, 40, String.valueOf(print_weight_double) + " KG", true));

            // [8] 납품처 (x=15, y=228, 폰트크기 30x30)
            slcsCmdText.write(helper.slcsBitmapText(15, 228, 30, "납품처 : " + pCompName, true));

            // 재인쇄 표시
            if (reprint) {
                pCompName = pCompName + "  *";
            }

            Log.i(TAG, "=====================제조일자==================" + making_date);

            // [9] 제조일자 (x=15, y=268, 폰트크기 30x30)
            String tempDate = "20" + making_date.substring(0, 2) + "년 " + making_date.substring(2, 4) + "월 " + making_date.substring(4, 6) + "일";
            slcsCmdText.write(helper.slcsBitmapText(15, 268, 30, "제조일자 : " + tempDate, true));

            // [10] 이력(묶음)번호 (x=15, y=313, 폰트크기 30x30)
            slcsCmdText.write(helper.slcsBitmapText(15, 313, 30, "이력(묶음)번호 : " + si.getIMPORT_ID_NO(), true));

            // [13~15] 가로선 3개 (L0 바코드 타입 전용)
            lineData.write(helper.slcsBitmapRect(0, 60, 560, 3));   // 가로선1 (상품명 아래)
            lineData.write(helper.slcsBitmapRect(0, 180, 560, 3));  // 가로선2 (바코드1 아래)
            lineData.write(helper.slcsBitmapRect(0, 345, 560, 3));  // 가로선3 (중량정보 아래)

            // [11] WH_AREA 출력 (x=385, y=305, 폰트크기 65x65) - 창고구역 코드
            whArea = si.getWH_AREA();
            Log.e(TAG, "::::::::: whArea check44 ::::::::" + whArea);

            if (whArea != null || !whArea.equals("")) {
                slcsCmdText.write(helper.slcsBitmapText(385, 305, 65, whArea, true));
            }

            // [12] 겉 테두리 박스 (0,0)에서 (560,440) 크기, 두께 3
            lineData.write(helper.slcsBitmapRect(0, 0, 560, 3));    // 위
            lineData.write(helper.slcsBitmapRect(0, 437, 560, 3));  // 아래
            lineData.write(helper.slcsBitmapRect(0, 0, 3, 440));    // 왼쪽
            lineData.write(helper.slcsBitmapRect(557, 0, 3, 440));  // 오른쪽

            // 텍스트·바코드 명령 + 선·테두리 비트맵 + 인쇄 실행(1장) + 라벨 피드
            ByteArrayOutputStream labelData = new ByteArrayOutputStream();
            labelData.write(slcsCmd.toString().getBytes("EUC-KR"));
            labelData.write(slcsCmdText.toByteArray());
            labelData.write(lineData.toByteArray());
            labelData.write(helper.slcsPrint(1).getBytes("EUC-KR"));
            labelData.write(helper.slcsFeedToMark().getBytes("EUC-KR"));

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
