package com.rgbsolution.highland_emart.shipment.type;

import android.content.Context;

import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;

/**
 * searchType별 출하 계근 흐름 규약 (개발66)
 *
 * <p>구현체는 searchType 하나씩 담당하며, 자기 타입의 흐름만 소유한다.
 * 상속·공통 골격은 두지 않고 이 인터페이스 선언만 공유한다.</p>
 *
 * <h3>구현체</h3>
 * <ul>
 *   <li>{@link EmartType}            (0) 이마트 출하</li>
 *   <li>{@link HomeplusType}         (2) 홈플러스 출하</li>
 *   <li>{@link WholesaleType}        (3) 도매 출하</li>
 *   <li>{@link EmartNonfixedType}    (4) 이마트 비정량</li>
 *   <li>{@link HomeplusNonfixedType} (5) 홈플러스 비정량</li>
 *   <li>{@link LotteType}            (6) 롯데 출하</li>
 * </ul>
 *
 * <p>생산(1, 7)은 구현체를 만들지 않는다. BixolonShipmentActivity의
 * setBarcodeMsgProduction 경로를 그대로 사용한다.</p>
 *
 * <p>존재 이유는 컴파일 시점 누락 검증이다. 타입을 추가하면서 분기를 빠뜨리면
 * "구현하지 않았습니다" 에러로 빌드가 막힌다.</p>
 *
 * @see ShipmentTypeFactory
 */
public interface ShipmentType {

    /** 바코드 스캔 진입점 — 원본 setBarcodeMsg 본문 (개발66 Step 2~7) */
    void onBarcodeScanned(String msg);

    /** 계근 저장 — 원본 wet_data_insert (개발66 Step 8) */
    void onWeightConfirmed(String weightStr, double weightDouble, String makingDate, String boxSerial);

    /** 상품 매칭 — 원본 find_PackerProduct 계열 (개발66 Step 9) */
    String findPackerProduct(String barcode, int workFlag);

    /** 재출력 라벨 — 원본 mHandler MESSAGE_REPRINT 분기 (개발66 Step 8) */
    void reprintLabel(String weightStr, String makingDate, String boxOrder, int selectPosition);

    /** 조회 후처리 — 원본 ProgressDlgShipSelect 내 타입별 분기 (개발66 Step 8) */
    void onShipmentLoaded(ArrayList<Shipments_Info> arSM);

    /** 서버 전송 — 원본 ProgressDlgShipmentSend.doInBackground (개발66 Step 10) */
    String send(Context context, ArrayList<Goodswets_Info> listSendInfo, ArrayList<Shipments_Info> arSM);
}
