# BRAND_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 브랜드코드
**파싱 위치**: temp[9]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | ● |
| 화면표시 | |
| 바코드생성 | |
| 라벨출력 | |
| 로직분기 | |
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
| **서버 전송** | 계근 데이터 전송, 작업 완료 전송 시 포함 |
| **DB 저장** | TB_SHIPMENT, TB_BARCODE_INFO, TB_GOODS_WET 테이블에 저장 |
| **바코드 정보 조회** | 바코드 정보 조회/업데이트 시 조건 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| Goodswets_Info.java | 계근 데이터 DTO |
| Barcodes_Info.java | 바코드 정보 DTO |
| DBHandler.java | TB_SHIPMENT, TB_BARCODE_INFO, TB_GOODS_WET 테이블 CRUD |
| BixolonShipmentActivity.java | 서버 전송 |
| ShipmentActivity.java | 서버 전송 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |
| ProgressDlgBarcodeSearch.java | 바코드 정보 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 브랜드코드 저장 |
| TB_BARCODE_INFO | 바코드 정보 브랜드코드 저장 |
| TB_GOODS_WET | 계근 데이터 브랜드코드 저장 |

---

## 4. 사용 코드

### 4.1 계근 데이터 서버 전송

**파일**: BixolonShipmentActivity.java (Line 3740)

```java
// 계근 데이터 전송 패킷에 브랜드코드 포함
packet += list_send_info.get(i).getBRAND_CODE() + "::";
// 포맷: ITEM_CODE::BRAND_CODE::CLIENT_TYPE::BOX_ORDER
```

### 4.2 작업 완료 서버 전송

**파일**: BixolonShipmentActivity.java (Line 3778)

```java
// 작업 완료 전송 패킷
String completeStr = arSM.get(j).getGI_D_ID() + "::" + arSM.get(j).getITEM_CODE() + "::" + arSM.get(j).getBRAND_CODE() + "::" + Common.REG_ID;
```

### 4.3 계근 데이터 저장 시 설정

**파일**: BixolonShipmentActivity.java (Line 1503)

```java
// 계근 데이터(Goodswets_Info)에 브랜드코드 설정
gi.setBRAND_CODE(arSM.get(current_work_position).getBRAND_CODE());
```

### 4.4 바코드 정보 업데이트 조건

**파일**: DBHandler.java (Line 1017)

```java
// 바코드 정보 업데이트 시 WHERE 조건
+ "BRAND_CODE = '" + hTemp.get("BRAND_CODE") + "'";
```

### 4.5 바코드 정보 삭제 조건

**파일**: DBHandler.java (Line 1705)

```java
// 바코드 정보 삭제 시 WHERE 조건
+ "BRAND_CODE = '" + brand_code + "'";
```

---

## 5. 서버 전송 패킷 구조

### 5.1 계근 데이터 전송

```
GI_D_ID::WEIGHT::WEIGHT_UNIT::PACKER_PRODUCT_CODE::BARCODE::
PACKER_CLIENT_CODE::MAKINGDATE::BOXSERIAL::BOX_CNT::EMARTITEM_CODE::
EMARTITEM::ITEM_CODE::BRAND_CODE::CLIENT_TYPE::BOX_ORDER
```

### 5.2 작업 완료 전송

```
GI_D_ID::ITEM_CODE::BRAND_CODE::REG_ID
```

---

## 6. 공통 Column 특성

DBInfo.java에서 **공통 Column**으로 정의:

```java
//  ::::::::::::::: ↓ 공통 Columns ↓ ::::::::::::::::::
public static final String BRAND_CODE = "BRAND_CODE";  // 브랜드코드, 공통 Column
```

- TB_SHIPMENT, TB_BARCODE_INFO, TB_GOODS_WET 모두에서 사용

---

## 7. 결론

**상태**: ✅ 필수 (삭제 불가)

BRAND_CODE는 **서버 전송의 필수 컬럼**으로:
- **계근 데이터 전송** 시 패킷에 포함
- **작업 완료 전송** 시 패킷에 포함
- **3개 테이블** (TB_SHIPMENT, TB_BARCODE_INFO, TB_GOODS_WET)에서 공통 사용
- 바코드 정보 **조회/업데이트 조건**

---

**최종 수정일**: 2026-02-03
