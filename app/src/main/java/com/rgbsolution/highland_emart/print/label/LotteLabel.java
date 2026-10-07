package com.rgbsolution.highland_emart.print.label;

import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;

/**
 * 롯데 라벨 바코드 타입별 디자인 공통 인터페이스
 * <p>
 * 신규 바코드 타입 추가 시 : 이 인터페이스를 구현한 클래스를 만들고
 * LabelPrintHelper.setPrintingLotte 의 switch 에 case 를 추가한다 (개발/75).
 * </p>
 */
public interface LotteLabel {

    /**
     * 바코드 타입별 롯데 라벨 출력
     *
     * @param si                  출하 대상 정보
     * @param reprint             재출력 여부 (업체명 뒤 "  *")
     * @param making_date         제조일자 (YYMMDD)
     * @param box_order           박스순번
     * @param print_weight_str    중량 6자리 문자열 (바코드용)
     * @param print_weight_double 출력 중량
     * @param callback            프린터 콜백
     * @return 출력 중량 문자열, 박스순번이 없어 출력하지 않은 경우 ""
     */
    String print(Shipments_Info si, boolean reprint, String making_date, String box_order, String print_weight_str, Double print_weight_double, LabelPrintHelper.PrinterCallback callback);
}
