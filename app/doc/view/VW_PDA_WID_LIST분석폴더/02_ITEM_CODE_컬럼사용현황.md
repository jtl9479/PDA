# ITEM_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 상품코드 (하이랜드 내부 품목코드)
**파싱 위치**: temp[1]

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
| **품목 식별** | 하이랜드 내부 품목코드로 상품 식별 |
| **서버 전송** | 계근 데이터 전송 시 포함 |
| **작업 완료 처리** | 출하 완료 서버 전송 시 식별자로 사용 |
| **DB 저장** | TB_SHIPMENT, TB_BARCODE_INFO, TB_GOODS_WET 테이블 저장 |

---

## 2. 관련 컬럼 비교

| 컬럼명 | 파싱 위치 | 의미 | 용도 |
|--------|-----------|------|------|
| **ITEM_CODE** | temp[1] | 하이랜드 상품코드 | 서버 전송, DB 저장 |
| **EMARTITEM_CODE** | temp[3] | 이마트 상품코드 | 바코드 생성, 라벨 출력 |

---

## 3. 사용 위치

| 파일                            | 용도                                                  |
| ----------------------------- | --------------------------------------------------- |
| Shipments_Info.java           | 출하대상 DTO                                            |
| Goodswets_Info.java           | 계근 데이터 DTO                                          |
| Barcodes_Info.java            | 바코드 정보 DTO                                          |
| DBHandler.java                | TB_SHIPMENT, TB_BARCODE_INFO, TB_GOODS_WET 테이블 CRUD |
| BixolonShipmentActivity.java  | 계근 작업, 서버 전송                                        |
| ShipmentActivity.java         | 계근 작업, 서버 전송                                        |
| ProgressDlgShipSearch.java    | 서버 응답 파싱                                            |
| ProgressDlgBarcodeSearch.java | 바코드 정보 조회                                           |

---

## 4. 저장 테이블

| 테이블 | 컬럼 | 용도 |
|--------|------|------|
| TB_SHIPMENT | ITEM_CODE, EMARTITEM_CODE | 출하대상 저장 |
| TB_BARCODE_INFO | ITEM_CODE | 바코드 정보 저장 |
| TB_GOODS_WET | EMARTITEM_CODE, ITEM_CODE | 계근 데이터 저장 |

---

## 5. 사용 코드

### 5.1 서버 전송 패킷 구성

**파일**: BixolonShipmentActivity.java (Line 3739)

```java
// 계근 데이터 전송
packet += list_send_info.get(i).getITEM_CODE() + "::";
// 포맷: ITEM_CODE::BRAND_CODE::CLIENT_TYPE::BOX_ORDER
```

### 5.2 작업 완료 처리

**파일**: BixolonShipmentActivity.java (Line 3778)

```java
// 출하 완료 시 서버 전송
String completeStr = arSM.get(j).getGI_D_ID() + "::" + arSM.get(j).getITEM_CODE() + "::" + arSM.get(j).getBRAND_CODE() + "::" + Common.REG_ID;
```

### 5.3 바코드 조회 조건

**파일**: ProgressDlgBarcodeSearch.java (Line 55)

```java
// 바코드 정보 조회
data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "'";
```

---

## 6. 결론

**상태**: ✅ 필수 (삭제 불가)

ITEM_CODE는 **하이랜드 내부 품목 식별자**로:
- 서버 전송 시 품목 식별
- 작업 완료 처리 시 식별자
- 바코드 정보 조회 조건

---

**최종 수정일**: 2026-02-03
