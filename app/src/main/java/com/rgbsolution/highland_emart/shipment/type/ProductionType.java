package com.rgbsolution.highland_emart.shipment.type;

import android.content.Context;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * 생산 계근(이노이천) — searchType "1" (개발67)
 *
 * <h3>이 타입의 판정 (개발67 §1.3)</h3>
 * <ul>
 *   <li>바코드 스캔 : 원본 {@code setBarcodeMsgProduction}(개발60이 분리한 생산 전용 본문)</li>
 *   <li>{@code ITEM_TYPE} : <b>S · J 만</b> — W/HW · B 블록은 개발60이 이미 제거했다(생산 VIEW 미출력)</li>
 *   <li>킬코이 · 센터명 판정 : <b>없음</b> — 개발60이 제거했다('하이랜드푸드' 고정)</li>
 *   <li>중복검사 우회 : X</li>
 *   <li>계근 INSERT : 일반 (insertqueryGoodsWet)</li>
 *   <li>계근중량 반올림 : 소수점 3자리 (else 경로)</li>
 *   <li>계근 시 라벨 : <b>없음</b> — 원본 라벨 분기에 생산(1) 케이스가 없다</li>
 *   <li>재출력 라벨 : else 경로(이마트 라벨)</li>
 *   <li>전송 : <b>일괄</b> / insert_goods_wet_production.jsp</li>
 *   <li>수기 입력 : 중량 절사 없음({@code Double.toString}), 소비기한 창은 CENTERNAME 조건일 때만</li>
 * </ul>
 *
 * <p><b>생산라벨(7)과 같은 본문이 아니다.</b> 7은 개발60의 정리를 거치지 않은 공용
 * {@code setBarcodeMsg} 원본을 쓰므로 W/HW · B 블록과 킬코이 판정이 살아 있다(개발67 §4.5).</p>
 *
 * <p>Step 1은 골격만 만든다. 각 메서드는 해당 Step에서 원본을 그대로 옮겨 채운다.
 * <b>Step 8(컷오버) 전까지 이 클래스는 생성되지 않는다</b> — {@code onCreate} 가 생산을 제외하기 때문이다.</p>
 */
public class ProductionType implements ShipmentType {

    private static final String TAG = "ProductionType";

    private final BixolonShipmentActivity a;

    public ProductionType(BixolonShipmentActivity activity) {
        this.a = activity;
    }

    @Override
    public void onBarcodeScanned(String msg) {
        throw new UnsupportedOperationException("개발67 Step 2에서 이관 예정");
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
