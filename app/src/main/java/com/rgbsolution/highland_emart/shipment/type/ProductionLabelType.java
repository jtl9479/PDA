package com.rgbsolution.highland_emart.shipment.type;

import android.content.Context;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * 생산 라벨 — searchType "7" (개발67)
 *
 * <h3>이 타입의 판정 (개발67 §1.3 · §4.5)</h3>
 * <ul>
 *   <li>바코드 스캔 : 원본 공용 {@code setBarcodeMsg} 본문. searchType 게이트가 <b>전부 거짓</b>이라
 *       결과적으로 도매(3) · 롯데(6)와 같은 경로다</li>
 *   <li>{@code ITEM_TYPE} : <b>W/HW · S · J · B 전부 유지</b> — 생산(1)과 달리 개발60의 정리를 거치지 않았다</li>
 *   <li>킬코이 · 센터명 판정 : <b>유지</b> (데이터 의존이라 접지 않는다)</li>
 *   <li>중복검사 우회 : X</li>
 *   <li>계근 INSERT : 일반 (insertqueryGoodsWet)</li>
 *   <li>계근중량 반올림 : 소수점 3자리 (else 경로)</li>
 *   <li>계근 시 라벨 : <b>{@code setPrinting_prod}</b> — 생산(1)과 다르다</li>
 *   <li>재출력 라벨 : <b>{@code setPrinting_prod}</b> — 생산(1)과 다르다</li>
 *   <li>전송 : <b>일괄</b> / insert_goods_wet_production.jsp — 생산(1)과 같다</li>
 *   <li>수기 입력 : 중량 절사 없음, 소비기한 창은 CENTERNAME 조건일 때만</li>
 * </ul>
 *
 * <p><b>이 타입의 바코드 스캔 본문은 현재 Activity에 없다.</b> 개발66 Step 7에서 공용
 * {@code setBarcodeMsg} 본문이 삭제됐다. 복원 원본은 개발66 Step 0 직전 커밋의
 * {@code BixolonShipmentActivity.setBarcodeMsg} 이며, 백업 파일
 * {@code BixolonShipmentActivity_Back.java}(1143~1539)와 내용이 같음을 대조로 확인했다(개발67 §4.5).</p>
 *
 * <p><b>현재 진입 경로가 없는 미사용 기능이다.</b> {@code activity_main.xml} 의 생산라벨 버튼 2개가
 * {@code visibility="gone"} 이고 {@code search_production_4label.jsp} 도 삭제됐다(2026-08-04 제외 결정).
 * 그럼에도 원본 동작을 보존하기 위해 파일로 남긴다(2026-09-17 사용자 결정).</p>
 *
 * <p>Step 1은 골격만 만든다. 각 메서드는 해당 Step에서 원본을 그대로 옮겨 채운다.
 * <b>Step 8(컷오버) 전까지 이 클래스는 생성되지 않는다.</b></p>
 */
public class ProductionLabelType implements ShipmentType {

    private static final String TAG = "ProductionLabelType";

    private final BixolonShipmentActivity a;

    public ProductionLabelType(BixolonShipmentActivity activity) {
        this.a = activity;
    }

    @Override
    public void onBarcodeScanned(String msg) {
        throw new UnsupportedOperationException("개발67 Step 3에서 이관 예정");
    }

    @Override
    public void onWeightConfirmed(String weightStr, double weightDouble, String makingDate, String boxSerial) {
        throw new UnsupportedOperationException("개발67 Step 4에서 이관 예정");
    }

    @Override
    public String findPackerProduct(String barcode, int workFlag) {
        throw new UnsupportedOperationException("개발67 Step 5에서 이관 예정");
    }

    @Override
    public void reprintLabel(String weightStr, String makingDate, String boxOrder, int selectPosition) {
        throw new UnsupportedOperationException("개발67 Step 4에서 이관 예정");
    }

    @Override
    public void onShipmentLoaded(ArrayList<Shipments_Info> arSM) {
        throw new UnsupportedOperationException("개발67 Step 4에서 이관 예정");
    }

    @Override
    public void onManualInput() {
        throw new UnsupportedOperationException("개발67 Step 7에서 이관 예정");
    }

    @Override
    public String send(Context context, ArrayList<Goodswets_Info> listSendInfo, ArrayList<Shipments_Info> arSM) {
        throw new UnsupportedOperationException("개발67 Step 6에서 이관 예정");
    }
}
