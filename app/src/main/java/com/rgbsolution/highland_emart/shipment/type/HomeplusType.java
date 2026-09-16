package com.rgbsolution.highland_emart.shipment.type;

import android.content.Context;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * 홈플러스 출하(정량) — searchType "2" (개발66)
 *
 * <h3>이 타입의 판정 (개발66 §1.3)</h3>
 * <ul>
 *   <li>중복검사 우회 : X — 원본 1205 · 1329 조건 거짓</li>
 *   <li>트레이더스 소비기한 검증 : X — 원본 1294 조건 거짓</li>
 *   <li>LB 환산 ZEROPOINT 자릿수 : X — 원본 1443 · 1505 조건 거짓 (else 경로)</li>
 *   <li>계근 INSERT : <b>홈플러스 전용</b>(insertqueryGoodsWetHomeplus) + selectMaxBoxOrder</li>
 *   <li>계근중량 반올림 : 소수점 3자리</li>
 *   <li>계근 시 라벨 : 홈플러스 라벨(setHomeplusPrinting)</li>
 *   <li>전송 : <b>건별</b> / insert_goods_wet.jsp — 비정량(5)과 다르다</li>
 * </ul>
 *
 * <p>ITEM_TYPE "B" 블록은 상수 주석이 "홈플러스 비정량"이라고만 적혀 있을 뿐
 * searchType 게이트가 없다. VIEW 실측으로 미출력을 증명하기 전까지 <b>그대로 유지</b>한다(개발66 §1.4).</p>
 *
 * <p>운영 DB에 데이터가 0건일 수 있어 테스트 데이터 확보 후 착수한다. 오류 32·33·34가 이 계열이다.</p>
 *
 * <p>Step 1은 골격만 만든다. 각 메서드는 해당 Step에서 원본을 그대로 옮겨 채운다.</p>
 */
public class HomeplusType implements ShipmentType {

    private static final String TAG = "HomeplusType";

    private final BixolonShipmentActivity a;

    public HomeplusType(BixolonShipmentActivity activity) {
        this.a = activity;
    }

    @Override
    public void onBarcodeScanned(String msg) {
        throw new UnsupportedOperationException("개발66 Step 4에서 이관 예정");
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
