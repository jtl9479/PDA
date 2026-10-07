package com.rgbsolution.highland_emart.print.label.homeplus;

import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;

import java.io.ByteArrayOutputStream;

/**
 * 홈플러스 라벨 — 등록되지 않은 바코드 타입 (H2 와 같은 형식, 기존 동작 유지)
 * LabelPrintHelper.setHomeplusPrinting 에서 바코드 타입으로 선택되어 호출된다 (개발/75).
 * 정량/비정량 차이(점포코드·지점코드)는 ITEM_TYPE 으로 구분한다.
 */
public class LabelHomeplusUnregistered implements HomeplusLabel {

    private static final String TAG = "LabelPrintHelper";   // 기존 로그 태그 유지

    private final LabelPrintHelper helper;

    public LabelHomeplusUnregistered(LabelPrintHelper helper) {
        this.helper = helper;
    }

    @Override
    public void print(Shipments_Info si, boolean reprint, Double print_weight_double, LabelPrintHelper.PrinterCallback callback) {
        String pointCode = "";                // 지점코드
        String storeCode = "";                // 점포코드(홈플러스 비정량)
        String pointName = "";                // 지점명
        String pCompName = LabelPrintHelper.COMPANY_NAME;

        pointCode = si.EMARTLOGIS_CODE.toString();
        storeCode = si.STORE_CODE.toString();
        pointName = si.CLIENTNAME.toString();

        // ========== SLCS 명령어로 홈플러스 라벨 인쇄 (Bixolon 프린터) ==========
        // StringBuilder + SLCS 헬퍼 메서드
        // 라벨 레이아웃: 세로 방향
        try {
            StringBuilder slcsCmd = new StringBuilder();
            ByteArrayOutputStream slcsCmdText = new ByteArrayOutputStream(); // 글자 비트맵(Korail.ttf)
            slcsCmd.append(helper.slcsInit());                                              // 프린터 초기화 (CB + CS13,0)
            slcsCmd.append(helper.slcsLabelSize(576, 590));                                 // 라벨 크기: 가로 576(용지 폭, 510 이면 x=510 이후 글자 잘림), 세로 590

            // [1] 지점명 출력 - 위치(30, 170)
            // 6자 초과 시 크기 70, 이하 시 크기 100 (긴 이름은 작게)
            if(pointName.length() > 6) {
                slcsCmdText.write(helper.slcsBitmapText(170, 30, 70, pointName.toString(), true));     // 6자 초과: 크기 70
            } else {
                slcsCmdText.write(helper.slcsBitmapText(170, 30, 100, pointName.toString(), true));   // 6자 이하: 크기 100
            }

            // [2] 점포코드/지점코드 출력 - 위치(135, 170), 크기 155
            // LabelPrintHelper.ITEM_TYPE_B(비정량)이면 storeCode, 아니면 pointCode 출력
            if (si.getITEM_TYPE().equals(LabelPrintHelper.ITEM_TYPE_B)) {
                slcsCmdText.write(helper.slcsBitmapText(170, 135, 155, storeCode.toString(), true));  // 비정량: 점포코드(STORE_CODE)
            } else {
                slcsCmdText.write(helper.slcsBitmapText(170, 135, 155, pointCode.toString(), true));  // 정량: 지점코드(EMARTLOGIS_CODE)
            }

            // [3] 상품명 출력 - 위치(287 or 283, 170)
            // 17자 초과 시 크기 25, 이하 시 크기 30 (긴 상품명은 작게)
            if (si.EMARTITEM.length() > 17) {
                slcsCmdText.write(helper.slcsBitmapText(170, 287, 25, si.EMARTITEM, true));            // 17자 초과: 크기 25
            } else {
                slcsCmdText.write(helper.slcsBitmapText(170, 283, 30, si.EMARTITEM, true));            // 17자 이하: 크기 30
            }

            // [4] BOX 텍스트 - 위치(322, 170), 크기 40
            slcsCmdText.write(helper.slcsBitmapText(170, 322, 40, "BOX", true));

            // [5] CT코드 (차량코드) - 위치(361, 170), 크기 40
            slcsCmdText.write(helper.slcsBitmapText(170, 361, 40, String.valueOf(si.getCT_CODE()), true));

            // [6] 중량/수입식별번호 - 위치(361, 380), 크기 40
            // 형식: "중량/수입식별번호 뒤 4자리"
            slcsCmdText.write(helper.slcsBitmapText(380, 361, 40, String.valueOf(print_weight_double) + "/"+si.getIMPORT_ID_NO().substring(8, 12), true));

            // [7] 납품일자 - 위치(402, 170), 크기 40
            // 형식: "YYYY년 MM월 DD일"
            Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
            String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
            slcsCmdText.write(helper.slcsBitmapText(170, 402, 40, tempDate, true));

            // [8] 업체명 - 위치(441, 170), 크기 40
            // 값: LabelPrintHelper.COMPANY_NAME 상수 ("(주)하이랜드이노베이션")
            slcsCmdText.write(helper.slcsBitmapText(170, 441, 40, pCompName, true));

            // [9] 인쇄 실행 - 1장 출력
            slcsCmd.append(helper.slcsPrint(1));
            // 라벨 피드 (마크 위치로 이동)
            slcsCmd.append(helper.slcsFeedToMark());

            // SLCS 명령어를 EUC-KR 인코딩으로 프린터에 전송
            callback.sendData(helper.withBitmapText(slcsCmd, slcsCmdText));
            callback.clearBarcodeInput();  // 바코드 입력창 초기화
        } catch (Exception e) {
            e.printStackTrace();
            if (Common.D) {
                Log.d(TAG, "setHomeplusPrinting Exception\n" + e.getMessage());
            }
        }
    }
}
