package com.rgbsolution.highland_emart.shipment.type;

/**
 * 타입 구현체가 공유하는 상수 (개발66)
 *
 * <p>값은 BixolonShipmentActivity의 기존 상수와 <b>완전히 동일</b>하다.
 * 이관 과정에서 값을 바꾸지 않는다. Activity 쪽 상수는 생산 경로가 계속 사용하므로 그대로 둔다.</p>
 *
 * <p>하드코딩 센터명(용인TRD, 대구TRD, 시화(W)_TRD, 여주TRD)은 원본이 문자열 리터럴로
 * 비교하므로 여기에 옮기지 않고 <b>리터럴 그대로</b> 유지한다.</p>
 */
public class ShipmentConst {

    private ShipmentConst() {
    }

    // ── 계근 방식 (ITEM_TYPE) ──────────────────────────────────────────────
    /** 바코드 계근 */
    public static final String ITEM_TYPE_W = "W";
    /** 바코드 계근 확장 */
    public static final String ITEM_TYPE_HW = "HW";
    /** 저울 계근 */
    public static final String ITEM_TYPE_S = "S";
    /** 지정 중량 */
    public static final String ITEM_TYPE_J = "J";
    /** 홈플러스 비정량 */
    public static final String ITEM_TYPE_B = "B";

    // ── 미트센터 관련 ─────────────────────────────────────────────────────
    /** 미트센터 업체코드 */
    public static final String MEAT_CENTER_CODE = "059015";
    /** 미트센터 지점코드 */
    public static final String MEAT_CENTER_STORE_CODE = "9231";
    /** 킬코이 패커코드 */
    public static final String KILKOY_PACKER_CODE = "30228";

    // ── 센터명 (수입육 센터 판별용) ────────────────────────────────────────
    public static final String CENTER_NAME_TRD = "TRD";
    public static final String CENTER_NAME_WET = "WET";
    public static final String CENTER_NAME_ET = "E/T";

    // ── 기타 ─────────────────────────────────────────────────────────────
    /** 중복 처리 방지 간격 (ms) — 같은 바코드 처리 후 1초 이내 재처리 차단 */
    public static final long BARCODE_PROCESS_DEBOUNCE_MS = 1000;
    /** 롯데 박스 순번 최대값 */
    public static final int LOTTE_BOX_ORDER_MAX = 9999;
}
