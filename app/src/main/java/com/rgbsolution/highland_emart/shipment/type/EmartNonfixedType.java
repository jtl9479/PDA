package com.rgbsolution.highland_emart.shipment.type;

import android.content.Context;

import com.rgbsolution.highland_emart.BixolonShipmentActivity;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * 이마트 비정량 — searchType "4" (개발66)
 *
 * <h3>이 타입의 판정 (개발66 §1.3)</h3>
 * <ul>
 *   <li>중복검사 우회 : <b>O</b> — 원본 1205 · 1329 조건 참 (dup = false)</li>
 *   <li>바코드 전부 매칭 : <b>O</b> — 원본 2150 (이 타입 전용)</li>
 *   <li>트레이더스 소비기한 검증 : <b>X</b> — 원본 1294는 searchType "0" 전용</li>
 *   <li>LB 환산 ZEROPOINT 자릿수 : <b>X</b> — 원본 1443 · 1505도 "0" 전용 (else 경로)</li>
 *   <li>계근 INSERT : 일반 (insertqueryGoodsWet)</li>
 *   <li>계근중량 반올림 : 소수점 3자리 — 정량(0)과 다르다</li>
 *   <li>계근 시 라벨 : 이마트 라벨(setPrinting) — 정량(0)과 동일</li>
 *   <li>전송 : <b>일괄</b> / insert_goods_wet_new.jsp — 정량(0)과 다르다</li>
 * </ul>
 *
 * <p><b>주의</b> — 상수 주석(원본 164줄)에 "도매 비정량"으로 적혀 있으나 실제로는 이마트다.
 * 라벨이 이마트 것이고(원본 2012~2014) 로그도 "이마트(비정량)"이다.
 * 이름이 이마트라고 해서 정량(0)과 같게 만들면 안 된다. 위 표대로 7곳이 다르다.</p>
 *
 * <p>전부 매칭(원본 2150)은 일반 매칭(원본 2131) <b>뒤에 이어서</b> 실행되어
 * 같은 bi가 두 번 반영될 수 있다. 실행 순서를 그대로 재현한다(개발66 §10 참조).</p>
 *
 * <p>Step 1은 골격만 만든다. 각 메서드는 해당 Step에서 원본을 그대로 옮겨 채운다.</p>
 */
public class EmartNonfixedType implements ShipmentType {

    private static final String TAG = "EmartNonfixedType";

    private final BixolonShipmentActivity a;

    public EmartNonfixedType(BixolonShipmentActivity activity) {
        this.a = activity;
    }

    @Override
    public void onBarcodeScanned(String msg) {
        throw new UnsupportedOperationException("개발66 Step 6에서 이관 예정");
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
