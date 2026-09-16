package com.rgbsolution.highland_emart.shipment.type;

import android.content.Context;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * 홈플러스 비정량 — searchType "5" (개발66)
 *
 * <h3>이 타입의 판정 (개발66 §1.3)</h3>
 * <ul>
 *   <li>중복검사 우회 : <b>O</b> — 원본 1205 · 1329 조건 참 (dup = false)</li>
 *   <li>트레이더스 소비기한 검증 : X — 원본 1294 조건 거짓</li>
 *   <li>LB 환산 ZEROPOINT 자릿수 : X — 원본 1443 · 1505 조건 거짓 (else 경로)</li>
 *   <li>계근 INSERT : 일반 (insertqueryGoodsWet) — 정량(2)과 다르다</li>
 *   <li>계근중량 반올림 : 소수점 3자리</li>
 *   <li>계근 시 라벨 : 홈플러스 라벨(setHomeplusPrinting) — 정량(2)과 동일</li>
 *   <li>전송 : <b>일괄</b> / insert_goods_wet_new.jsp — 정량(2)과 다르다</li>
 * </ul>
 *
 * <p><b>중복검사 조회 자체는 건너뛰지 않는다.</b> 원본은 조회한 뒤 결과를 false로 덮는다.
 * 조회를 생략하면 동작이 달라진다(개발66 §1.5).</p>
 *
 * <p>Step 1은 골격만 만든다. 각 메서드는 해당 Step에서 원본을 그대로 옮겨 채운다.</p>
 */
public class HomeplusNonfixedType implements ShipmentType {

    private static final String TAG = "HomeplusNonfixedType";

    private final BixolonShipmentActivity a;

    public HomeplusNonfixedType(BixolonShipmentActivity activity) {
        this.a = activity;
    }

    @Override
    public void onBarcodeScanned(String msg) {
        throw new UnsupportedOperationException("개발66 Step 5에서 이관 예정");
    }

    @Override
    public void onWeightConfirmed(String weightStr, double weightDouble, String makingDate, String boxSerial) {
        throw new UnsupportedOperationException("개발66 Step 8에서 이관 예정");
    }

    @Override
    public String findPackerProduct(String barcode, int workFlag) {
        throw new UnsupportedOperationException("개발66 Step 9에서 이관 예정");
    }

    @Override
    public void reprintLabel(String weightStr, String makingDate, String boxOrder, int selectPosition) {
        throw new UnsupportedOperationException("개발66 Step 8에서 이관 예정");
    }

    @Override
    public void onShipmentLoaded(ArrayList<Shipments_Info> arSM) {
        throw new UnsupportedOperationException("개발66 Step 8에서 이관 예정");
    }

    @Override
    public String send(Context context, ArrayList<Goodswets_Info> listSendInfo, ArrayList<Shipments_Info> arSM) {
        throw new UnsupportedOperationException("개발66 Step 10에서 이관 예정");
    }
}
