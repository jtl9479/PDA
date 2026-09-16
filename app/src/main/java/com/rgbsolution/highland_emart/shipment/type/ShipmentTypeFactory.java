package com.rgbsolution.highland_emart.shipment.type;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;

/**
 * searchType 문자열 → 담당 구현체 생성 (개발66)
 *
 * <p>이 프로젝트에서 <b>Common.searchType을 읽는 유일한 지점</b>이 된다
 * (Activity onCreate의 레이아웃 분기와 생산 분기 제외).</p>
 *
 * <p>생산(1, 7)은 이 메서드로 들어오지 않는다. 호출 전에 Activity가 먼저 걸러야 한다.
 * 알 수 없는 값이 들어오면 즉시 실패시켜, 조용히 아무것도 안 하는 상황을 막는다.</p>
 */
public class ShipmentTypeFactory {

    private ShipmentTypeFactory() {
    }

    public static ShipmentType create(String searchType, BixolonShipmentActivity activity) {
        switch (searchType) {
            case "0": return new EmartType(activity);             // 이마트 출하
            case "2": return new HomeplusType(activity);           // 홈플러스 출하
            case "3": return new WholesaleType(activity);          // 도매 출하
            case "4": return new EmartNonfixedType(activity);      // 이마트 비정량
            case "5": return new HomeplusNonfixedType(activity);   // 홈플러스 비정량
            case "6": return new LotteType(activity);              // 롯데 출하
            // 생산(1) · 생산라벨(7) 은 Activity가 직접 처리한다 (setBarcodeMsgProduction)
            default:  throw new IllegalArgumentException("지원하지 않는 searchType: " + searchType);
        }
    }
}
