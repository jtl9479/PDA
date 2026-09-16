package com.rgbsolution.highland_emart.shipment.type;

import android.content.Context;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * 이마트 출하(정량) — searchType "0" (개발66)
 *
 * <h3>이 타입의 판정 (개발66 §1.3)</h3>
 * <ul>
 *   <li>중복검사 우회 : X — 원본 1205 · 1329 조건 거짓</li>
 *   <li>트레이더스 소비기한 검증 : <b>O</b> — 원본 1294 (이 타입 전용)</li>
 *   <li>LB 환산 ZEROPOINT 자릿수 : <b>O</b> — 원본 1443 · 1505 (이 타입 전용)</li>
 *   <li>수기 중량 소수 1자리 절사 : <b>O</b> — 원본 660</li>
 *   <li>수기 입력 시 소비기한 창 : TRD/WET/E/T 센터일 때 — 원본 703 · 704</li>
 *   <li>계근 INSERT : 일반 (insertqueryGoodsWet)</li>
 *   <li>계근중량 반올림 : <b>소수점 1자리</b> — 이 타입 전용</li>
 *   <li>계근 시 라벨 : 이마트 라벨(setPrinting)</li>
 *   <li>전송 : 건별 / insert_goods_wet.jsp</li>
 * </ul>
 *
 * <p>킬코이·미트센터 판정(원본 1285)과 센터명 판정(원본 1293)은 searchType 게이트가 없다.
 * 이 타입에서 접는 것은 <b>안쪽 1294의 searchType 조건뿐</b>이며 바깥 else if 구조는 유지한다(개발66 §1.4).</p>
 *
 * <p>가장 크고 위험하므로 다른 타입 5종으로 골격을 검증한 뒤 마지막(Step 7)에 이관한다.
 * 이마트 출하계근 테스트가 진행 중이므로 테스트 일정과 조율한다.</p>
 *
 * <p>Step 1은 골격만 만든다. 각 메서드는 해당 Step에서 원본을 그대로 옮겨 채운다.</p>
 */
public class EmartType implements ShipmentType {

    private static final String TAG = "EmartType";

    private final BixolonShipmentActivity a;

    public EmartType(BixolonShipmentActivity activity) {
        this.a = activity;
    }

    @Override
    public void onBarcodeScanned(String msg) {
        throw new UnsupportedOperationException("개발66 Step 7에서 이관 예정");
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
