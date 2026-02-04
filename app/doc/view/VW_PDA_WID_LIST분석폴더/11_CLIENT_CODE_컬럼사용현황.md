# CLIENT_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 출고업체코드
**파싱 위치**: temp[10]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | |
| 라벨출력 | |
| 로직분기 | ● |
| DB저장 | ● |
| 바코드검증 | |
| 조회조건 | |
| 앱미전달 | |

> **구분 기준**
> - **서버전송**: 서버에 데이터 전송 시 패킷에 포함
> - **화면표시**: 앱 화면(Activity)에 텍스트로 표시
> - **바코드생성**: 바코드 문자열 생성에 사용
> - **라벨출력**: 라벨 인쇄 시 텍스트/값으로 출력
> - **로직분기**: if/switch 등 조건 분기에 사용
> - **DB저장**: 로컬 SQLite(TB_SHIPMENT)에 저장
> - **바코드검증**: 스캔 바코드와 매칭 검증
> - **조회조건**: 서버/로컬 DB 쿼리 WHERE 조건
> - **앱미전달**: 서버 VIEW에서만 사용, 앱에 전달 안 됨

---

## 1. 용도

| 용도 | 설명 |
|------|------|
| **DB 조회 조건** | TB_GOODS_WET 계근 데이터 조회 시 조건 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 계근 데이터 조회 조건 |
| ShipmentActivity.java | 계근 데이터 조회 조건 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 출고업체코드 저장 |

---

## 4. 사용 코드

### 4.1 계근 데이터 조회 조건

**파일**: BixolonShipmentActivity.java (Line 3328)

```java
// TB_GOODS_WET 테이블에서 계근 데이터 조회
row = DBHandler.selectqueryListGoodsWetInfo(mContext,
    arSM.get(i).getGI_D_ID(),
    arSM.get(i).getPACKER_PRODUCT_CODE(),
    arSM.get(i).getCLIENT_CODE());  // 출고업체코드로 필터링
```

### 4.2 계근 데이터 상세 조회

**파일**: BixolonShipmentActivity.java (Line 4377)

```java
// 계근 데이터 상세 조회
list_gi_info = DBHandler.selectqueryGoodsWet(BixolonShipmentActivity.this,
    si.getGI_D_ID(),
    si.getPACKER_PRODUCT_CODE(),
    si.getCLIENT_CODE());
```

---

## 5. 관련 컬럼 비교

| 컬럼명 | 의미 | 테이블 | 용도 |
|--------|------|--------|------|
| **CLIENT_CODE** | 출고업체코드 | TB_SHIPMENT | 출하대상 거래처 |
| **PACKER_CLIENT_CODE** | 패커 거래처코드 | TB_BARCODE_INFO, TB_GOODS_WET | 바코드/계근 패커 정보 |

---

## 6. 조회 함수 파라미터

### selectqueryListGoodsWetInfo

```java
public static int selectqueryListGoodsWetInfo(
    Context context,
    String gi_d_id,           // 출고상세ID
    String packer_product_code,  // 패커상품코드
    String client_code        // 출고업체코드 ★
)
```

### selectqueryGoodsWet

```java
public static ArrayList<Goodswets_Info> selectqueryGoodsWet(
    Context context,
    String gi_d_id,           // 출고상세ID
    String packer_product_code,  // 패커상품코드
    String client_code        // 출고업체코드 ★
)
```

---

## 7. 결론

**상태**: ✅ 필수 (삭제 불가)

CLIENT_CODE는 **DB 조회 조건의 핵심 컬럼**으로:
- TB_GOODS_WET **계근 데이터 조회** 시 필터링 조건
- GI_D_ID, PACKER_PRODUCT_CODE와 함께 **복합 조회 조건** 구성
- PACKER_CLIENT_CODE (패커 거래처코드)와는 **별개 컬럼**

---

**최종 수정일**: 2026-02-03
