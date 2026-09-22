package com.rgbsolution.highland_emart.common;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * Activity 공통 유틸
 * 여러 Activity 가 공통으로 쓰거나 쓸 가능성이 있는 함수를 모은다.
 * 값(설정·상수·전역 상태)은 {@link Common} 에 두고, 동작은 이쪽에 둔다.
 * 단, 특정 업무에만 쓰이는 로직은 전용 클래스로 뺀다(예: 상품 판정).
 */
public class CommonUtils {

    private CommonUtils() {
    }

    /**
     * 소프트 키보드 숨김
     * BixolonShipmentActivity·ProductionActivity 가 각자 갖고 있던 동일 구현을 통합한 것이다.
     * 주의: {@code activity.getCurrentFocus()} 가 null 이면 NPE 가 난다. 원본과 동일하게 방어 코드를 두지 않는다.
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager btn_input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        btn_input.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
