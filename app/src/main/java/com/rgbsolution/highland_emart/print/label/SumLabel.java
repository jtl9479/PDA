package com.rgbsolution.highland_emart.print.label;

import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;

import java.util.ArrayList;

/**
 * 합산 라벨 (계근 내역 [합산] 버튼) — 파렛트당 박스 수 단위로 개별 중량 + 페이지 번호 + 'N박스 총 중량' 출력
 * BixolonShipmentActivity.printSumLabel 에서 이동 (개발/77 Step 1) — 개발/86 에서 최신 원본 printSumLabel 구조로 교체
 * SLCS 헬퍼는 LabelPrintHelper 의 public 메서드 사용 (Activity 복사본과 문자열 동일)
 */
public class SumLabel {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    // 합산 라벨 인쇄 (파렛트 단위)
    // 최신 원본 ShipmentActivity.printSumLabel(int BOX_PER_PALLET) 구조 그대로 (개발/86), 출력 명령만 SLCS 변환
    public static void print(LabelPrintHelper helper, ArrayList<Goodswets_Info> list, int BOX_PER_PALLET, LabelPrintHelper.PrinterCallback callback) {
        // ========== SLCS 명령어로 합계 라벨 인쇄 (Bixolon 프린터) ==========
        // 원본: Woosim ByteArrayOutputStream + WoosimCmd 명령어
        // 출력 항목:
        //   [1] 개별 중량 - 동적 좌표(pos_x, pos_y) 32x32
        //   [2] 페이지 번호 - (20, 380) 30x30, 장마다 "(현재장/파렛트 장 수)"
        //   [3] 박스 수 문구 - (95, 380) 37x37, 파렛트 마지막 장
        //   [4] 파렛트 총 중량 - (360, 355) 75x75, 파렛트 마지막 장
        try {
            int BOX_PER_PAGE = 36;                                  // 라벨 한 장에 찍을 박스 수 (기본: 가로 6 * 세로 6)

            StringBuilder slcsCmd = new StringBuilder();
            java.io.ByteArrayOutputStream sumText = new java.io.ByteArrayOutputStream(); // 글자 비트맵(Korail.ttf)

            // 초기화 (원본: initPrinter + setPageMode + selectTTF + setTextStyle)
            slcsCmd.append(helper.slcsInit());
            // 라벨 크기 576x460 (원본: PM_setArea(0, 0, 576, 460))
            slcsCmd.append(helper.slcsLabelSize(576, 460));

            double pallet_sum = 0;                                  // 현재 파렛트에 찍힌 박스들의 중량 합
            int pos_x = 0;                                          // 가로 위치
            int pos_y = 0;                                          // 세로 위치

            int total_count = list.size();

            for (int i = 0; i < total_count; i++) {
                int idx_in_pallet = i % BOX_PER_PALLET;             // 현재 파렛트에서 몇번째인지
                int idx_in_page = idx_in_pallet % BOX_PER_PAGE;     // 현재 장에서 몇번째인지

                // 원본: pos_x = 100 * (idx_in_page % 6)
                // 6열(x=500)에서 소수 2자리(5글자) 값이 라벨 폭 576을 넘어 잘려 열 간격 95로 축소 (개발/71)
                pos_x = 95 * (idx_in_page % 6);
                pos_y = 10 + (idx_in_page / 6) * 50;

                // [1] 개별 중량 (원본: getTTFcode(40, 40, weight) → Korail 32, 개발/71)
                sumText.write(helper.bitmapText(pos_x, pos_y, 32, list.get(i).getWEIGHT()));

                pallet_sum += Double.parseDouble(list.get(i).getWEIGHT());

                boolean last_item = (i + 1) == total_count;
                boolean page_end = (idx_in_page + 1) == BOX_PER_PAGE;
                boolean pallet_end = (idx_in_pallet + 1) == BOX_PER_PALLET;

                // 장이 다 찼거나, 파렛트가 바뀌거나, 마지막 박스면 한 장 출력
                if (page_end || pallet_end || last_item) {
                    // 현재 파렛트의 박스 수 (마지막 파렛트는 남은 박스만큼만)
                    int pallet_start = i - idx_in_pallet;
                    int pallet_box_cnt = Math.min(BOX_PER_PALLET, total_count - pallet_start);

                    // [2] 파렛트당 페이지 번호 (예: (1/2))
                    int pallet_page = (pallet_box_cnt + BOX_PER_PAGE - 1) / BOX_PER_PAGE;
                    int cur_page = (idx_in_pallet / BOX_PER_PAGE) + 1;
                    sumText.write(helper.bitmapText(20, 380, 30, "(" + cur_page + "/" + pallet_page + ")"));

                    // 파렛트 마지막 장에만 파렛트 전체 박스 수와 합계 중량 출력
                    if (pallet_end || last_item) {
                        double pallet_weight = Math.floor(pallet_sum * 100);
                        pallet_weight = pallet_weight / 100.0;

                        String temp_weight = String.format("%.1f", pallet_weight);
                        pallet_weight = Double.parseDouble(temp_weight);

                        // [3] 박스 수 문구
                        sumText.write(helper.bitmapText(95, 380, 37, pallet_box_cnt + "박스 총 중량 : "));

                        // [4] 총 중량 (360, 355) 75
                        // 1000kg 이상은 폭 576 초과 → 넘칠 때만 x 그대로, 크기 2씩 축소(최소 40) (사용자 결정 2026-10-22)
                        String weight_text = Double.toString(pallet_weight);
                        int weight_size = 75;
                        int weight_width = helper.bitmapTextWidth(weight_size, weight_text);
                        while (360 + weight_width > 576 && weight_size > 40) {
                            weight_size -= 2;
                            weight_width = helper.bitmapTextWidth(weight_size, weight_text);
                        }
                        if (Common.D) {
                            Log.d(TAG, "합산 총 중량 : " + pallet_box_cnt + "박스 " + weight_text + " size=" + weight_size + " width=" + weight_width);
                        }
                        sumText.write(helper.bitmapText(360, 355, weight_size, weight_text));

                        pallet_sum = 0;
                    }

                    // 인쇄 실행 + 라벨 피드 (원본: PM_setArea + sendData + feedToMark)
                    java.io.ByteArrayOutputStream sumLabel = new java.io.ByteArrayOutputStream();
                    sumLabel.write(slcsCmd.toString().getBytes("EUC-KR"));
                    sumLabel.write(sumText.toByteArray());
                    sumLabel.write((helper.slcsPrint(1) + helper.slcsFeedToMark()).getBytes("EUC-KR"));

                    callback.sendData(sumLabel.toByteArray());

                    // 다음 장 준비 (원본: byteStream.reset() + 프린터 설정)
                    if (!last_item) {
                        slcsCmd.setLength(0);
                        sumText.reset();
                        slcsCmd.append(helper.slcsInit());
                        slcsCmd.append(helper.slcsLabelSize(576, 460));
                    }
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            if (Common.D) {
                Log.d(TAG, "setPrinting Exception\n" + e.getMessage().toString());
            }
        } catch (Exception ex) {
            Log.e(TAG, "==== printSumLabel Exception ====");
            Log.e(TAG, ex.getMessage().toString());
        }
    }
}
