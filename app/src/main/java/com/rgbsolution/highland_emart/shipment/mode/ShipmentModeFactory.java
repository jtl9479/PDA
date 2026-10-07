package com.rgbsolution.highland_emart.shipment.mode;

import com.rgbsolution.highland_emart.common.Common;

/**
 * searchType 에 맞는 ShipmentMode 생성 (개발/76)
 */
public class ShipmentModeFactory {

    /** null 이면 NPE (기존 onCreate 의 searchType.equals 와 동일). 등록되지 않은 값은 UnregisteredMode (기존 else 경로). */
    public static ShipmentMode create(String searchType) {
        if (searchType.equals(Common.SEARCH_TYPE_EMART)) {
            return new EmartMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_NONFIXED)) {
            return new EmartNonfixedMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_WHOLESALE)) {
            return new WholesaleMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_HOMEPLUS)) {
            return new HomeplusMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_HOMEPLUS_NONFIXED)) {
            return new HomeplusNonfixedMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_LOTTE)) {
            return new LotteMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_PRODUCTION)) {
            return new ProductionMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_PRODUCTION_LABEL)) {
            return new ProductionLabelMode();
        }
        return new UnregisteredMode();
    }
}
