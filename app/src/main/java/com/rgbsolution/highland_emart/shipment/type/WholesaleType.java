package com.rgbsolution.highland_emart.shipment.type;

import android.content.Context;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * 도매 출하 — searchType "3" (개발66)
 *
 * <h3>이 타입의 판정 (개발66 §1.3)</h3>
 * <ul>
 *   <li>중복검사 우회 : X — 원본 1205 · 1329 조건 거짓</li>
 *   <li>트레이더스 소비기한 검증 : X — 원본 1294 조건 거짓</li>
 *   <li>LB 환산 ZEROPOINT 자릿수 : X — 원본 1443 · 1505 조건 거짓 (else 경로)</li>
 *   <li>계근 INSERT : 일반 (insertqueryGoodsWet)</li>
 *   <li>계근중량 반올림 : 소수점 3자리</li>
 *   <li>계근 시 라벨 : <b>출력 없음</b></li>
 *   <li>재출력 라벨 : <b>이마트 라벨</b>(setPrinting) — 원본 mHandler else 경로</li>
 *   <li>전송 : 일괄 / insert_goods_wet_new.jsp</li>
 *   <li>레이아웃 : activity_shipment_wholesale (Activity onCreate에서 처리)</li>
 * </ul>
 *
 * <p>Step 1은 골격만 만든다. 각 메서드는 해당 Step에서 원본을 그대로 옮겨 채운다.</p>
 */
public class WholesaleType implements ShipmentType {

    private static final String TAG = "WholesaleType";

    private final BixolonShipmentActivity a;

    public WholesaleType(BixolonShipmentActivity activity) {
        this.a = activity;
    }

    @Override
    public void onBarcodeScanned(String msg) {
        throw new UnsupportedOperationException("개발66 Step 2에서 이관 예정");
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
