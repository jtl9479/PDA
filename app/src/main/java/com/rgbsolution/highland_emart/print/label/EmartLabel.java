package com.rgbsolution.highland_emart.print.label;

import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;

/**
 * 이마트 라벨 바코드 타입별 디자인 공통 인터페이스
 * <p>
 * 신규 바코드 타입 추가 시 : 이 인터페이스를 구현한 LabelXX 클래스를 만들고
 * LabelPrintHelper.setPrinting 의 switch 에 case 를 추가한다 (개발/75).
 * </p>
 */
public interface EmartLabel {

    /**
     * 바코드 타입별 이마트 라벨 출력
     *
     * @param si                  출하 대상 정보
     * @param reprint             재출력 여부 (업체명 뒤 "  *")
     * @param print_weight_str    중량 6자리 문자열 (바코드용)
     * @param print_weight_double 출력 중량
     * @param pointName           지점명
     * @param expiryDayConvert    소비기한 문구 (킬코이/트레이더스, 없으면 "")
     * @param callback            프린터 콜백
     */
    void print(Shipments_Info si, boolean reprint, String print_weight_str, Double print_weight_double, String pointName, String expiryDayConvert, LabelPrintHelper.PrinterCallback callback);
}
