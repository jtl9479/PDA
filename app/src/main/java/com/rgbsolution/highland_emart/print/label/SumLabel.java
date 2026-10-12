package com.rgbsolution.highland_emart.print.label;

import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;

import java.util.ArrayList;

/**
 * 합산 라벨 (계근 내역 [합산] 버튼) — 파렛트당 박스 수 단위로 개별 중량 + 'N번 총 중량' 출력
 * BixolonShipmentActivity.printSumLabel 에서 이동 (개발/77 Step 1, 본문 변경 없음 — 개발/79 파렛트 단위 포함)
 * SLCS 헬퍼는 LabelPrintHelper 의 public 메서드 사용 (Activity 복사본과 문자열 동일)
 */
public class SumLabel {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    // 합산 라벨 인쇄 (파렛트 단위)
    // 원본: detail_btn_sum 리스너 내 36건 단위 인쇄 → 개발 79에서 메서드로 옮기고 파렛트당 박스 수 단위로 끊음
    // pallet_box_cnt = 36 이면 원본과 동일 출력
    public static void print(LabelPrintHelper helper, ArrayList<Goodswets_Info> list, int pallet_box_cnt, LabelPrintHelper.PrinterCallback callback) {
        // ========== SLCS 명령어로 합계 라벨 인쇄 (Bixolon 프린터) ==========
        // 원본: Woosim ByteArrayOutputStream + WoosimCmd 명령어
        // 변환: StringBuilder + SLCS 헬퍼 메서드
        // 용도: 계근 내역 합계 버튼 클릭 시 중량 합산 라벨 인쇄
        // 출력 항목:
        //   [1] 개별 중량 - 동적 좌표(p_weight, p_hight) 32x32
        //   [2] 파렛트별 총 중량 - (100, 350) 60x60
        // 주의: 라벨 1장 최대 36개 항목 (6열 x 6행), 파렛트가 36개를 넘으면 이어지는 라벨에 총 중량 없이 인쇄
        try {
            StringBuilder slcsCmd = new StringBuilder();
            java.io.ByteArrayOutputStream sumText = new java.io.ByteArrayOutputStream(); // 글자 비트맵(Korail.ttf)

            // 초기화: CB(버퍼클리어) + CS13,0(한글문자셋)
            // 원본: WoosimCmd.initPrinter() + setPageMode() + selectTTF() + setTextStyle()
            slcsCmd.append(helper.slcsInit());

            // 라벨 크기 설정: 576x460 도트
            // 원본: WoosimCmd.PM_setArea(0, 0, 576, 460)
            slcsCmd.append(helper.slcsLabelSize(576, 460));

            double weight_sum = 0;
            int p_weight = 0;
            int p_hight = 0;

            for (int i = 0; i < list.size(); i++) {
                int pallet_no = i / pallet_box_cnt + 1;     // 파렛트 번호
                int k = i % pallet_box_cnt;                 // 파렛트 내 순번

                // 동적 좌표 계산 (6열 x 6행 = 36개 단위)
                // p_hight: 행 위치 (0~5행 반복, 36개마다 리셋)
                // p_weight: 열 위치 (0~5열 반복)
                // 원본: i 기준 → 파렛트 내 순번 k 기준
                p_hight = 10 + (k / 6 * 50) - (k / 36 * 300);
                // 원본: p_weight = 100 * (i%6)
                // 6열(x=500)에서 소수 2자리(5글자) 값이 라벨 폭 576을 넘어 잘려 열 간격 95로 축소
                p_weight = 95 * (k % 6);

                // [1] 개별 중량 출력 (동적 좌표, 폰트크기 32x32)
                // 원본: PM_setPosition(p_weight, p_hight) + getTTFcode(40, 40, weight)
                // Korail 비트맵 40 크기는 소수 2자리(5글자) 폭이 열 간격과 같아 숫자가 붙어 32로 축소
                sumText.write(helper.bitmapText(p_weight, p_hight, 32, list.get(i).getWEIGHT()));

                weight_sum += Double.parseDouble(list.get(i).getWEIGHT());

                if ((k + 1) == pallet_box_cnt || (i + 1) == list.size()) {
                    // 파렛트 끝 또는 마지막 항목 → 총 중량 인쇄
                    weight_sum = Math.floor(weight_sum * 100);
                    weight_sum = weight_sum / 100.0;

                    String temp_weight = String.format("%.1f", weight_sum);
                    weight_sum = Double.parseDouble(temp_weight);

                    // [2] 파렛트별 총 중량 (x=100, y=350, 폰트크기 60x60)
                    // 원본: PM_setPosition(100, 350) + getTTFcode(60, 60, "N번 총 중량 : XX.X")
                    // 총 중량 5자리 이상이면 라벨 폭 576을 넘어 잘림 → 넘칠 때만 x를 왼쪽으로(최소 10), 그래도 넘치면 글자 크기 축소(최소 40)
                    String total_text = pallet_no + "번 총 중량 : " + Double.toString(weight_sum);
                    int total_x = 100;
                    int total_size = 60;
                    int total_width = helper.bitmapTextWidth(total_size, total_text);
                    if (total_x + total_width > 576) {
                        total_x = Math.max(10, 576 - total_width);
                    }
                    while (total_x + total_width > 576 && total_size > 40) {
                        total_size -= 2;
                        total_width = helper.bitmapTextWidth(total_size, total_text);
                    }
                    if (Common.D) {
                        Log.d(TAG, "합산 총 중량 : " + total_text + " x=" + total_x + " size=" + total_size + " width=" + total_width);
                    }
                    sumText.write(helper.bitmapText(total_x, 350, total_size, total_text));

                    // 인쇄 실행 + 라벨 피드
                    // 원본: PM_printData() + feedToMark()
                    java.io.ByteArrayOutputStream sumLabel = new java.io.ByteArrayOutputStream();
                    sumLabel.write(slcsCmd.toString().getBytes("EUC-KR"));
                    sumLabel.write(sumText.toByteArray());
                    sumLabel.write((helper.slcsPrint(1) + helper.slcsFeedToMark()).getBytes("EUC-KR"));

                    // 전송
                    callback.sendData(sumLabel.toByteArray());

                    // StringBuilder 초기화 (원본: byteStream.reset())
                    slcsCmd.setLength(0);
                    sumText.reset();
                    slcsCmd.append(helper.slcsInit());
                    slcsCmd.append(helper.slcsLabelSize(576, 460));
                    weight_sum = 0;

                } else if ((k + 1) % 36 == 0) {
                    // 라벨 1장(36개) 가득 참, 파렛트 진행 중 → 총 중량 없이 인쇄, 합계 유지
                    java.io.ByteArrayOutputStream sumLabel = new java.io.ByteArrayOutputStream();
                    sumLabel.write(slcsCmd.toString().getBytes("EUC-KR"));
                    sumLabel.write(sumText.toByteArray());
                    sumLabel.write((helper.slcsPrint(1) + helper.slcsFeedToMark()).getBytes("EUC-KR"));

                    // 전송
                    callback.sendData(sumLabel.toByteArray());

                    slcsCmd.setLength(0);
                    sumText.reset();
                    slcsCmd.append(helper.slcsInit());
                    slcsCmd.append(helper.slcsLabelSize(576, 460));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (Common.D) {
                Log.d(TAG, "setPrinting Exception\n" + e.getMessage().toString());
            }
        }
    }
}
