package com.rgbsolution.highland_emart.print.label.homeplus;

import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;

/**
 * 홈플러스 라벨 바코드 타입별 디자인 공통 인터페이스
 * <p>
 * 신규 바코드 타입 추가 시 : 이 인터페이스를 구현한 클래스를 만들고
 * LabelPrintHelper.setHomeplusPrinting 의 switch 에 case 를 추가한다 (개발/75).
 * </p>
 */
public interface HomeplusLabel {

    /**
     * 바코드 타입별 홈플러스 라벨 출력
     *
     * @param si                  출하 대상 정보
     * @param reprint             재출력 여부 (홈플러스 라벨은 현재 미사용)
     * @param print_weight_double 출력 중량 (소수 2자리 절사)
     * @param callback            프린터 콜백
     */
    void print(Shipments_Info si, boolean reprint, Double print_weight_double, LabelPrintHelper.PrinterCallback callback);
}
