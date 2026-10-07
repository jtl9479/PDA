package com.rgbsolution.highland_emart.print.label.lotte;

import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;

import java.io.ByteArrayOutputStream;

/**
 * 롯데 라벨 — 등록되지 않은 바코드 타입 (바코드·L0 항목 없이 상품명·WH_AREA·테두리만 출력, 기존 동작 유지)
 * LabelPrintHelper.setPrintingLotte 에서 바코드 타입으로 선택되어 호출된다 (개발/75).
 */
public class LabelLotteUnregistered implements LotteLabel {

    private static final String TAG = "LabelPrintHelper";   // 기존 로그 태그 유지

    private final LabelPrintHelper helper;

    public LabelLotteUnregistered(LabelPrintHelper helper) {
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
