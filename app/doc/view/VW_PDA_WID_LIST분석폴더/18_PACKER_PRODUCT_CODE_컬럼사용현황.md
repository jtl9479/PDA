# PACKER_PRODUCT_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 패커상품코드
**파싱 위치**: temp[17]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | ● |
| 화면표시 | ● |
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
| **서버 전송** | 계근 데이터 전송 패킷에 포함 |
| **계근 데이터 저장** | TB_GOODS_WET 테이블에 저장 |
| **화면 표시** | 상품코드 필드에 표시 |
| **바코드 정보 조회** | TB_BARCODE_INFO 조회 조건 |
| **DB 저장** | TB_SHIPMENT, TB_GOODS_WET 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| Goodswets_Info.java | 계근 데이터 DTO |
| DBInfo.java | DB 컬럼 상수 정의 (공통 컬럼) |
| DBHandler.java | TB_SHIPMENT, TB_GOODS_WET 테이블 CRUD |
| BixolonShipmentActivity.java | 서버 전송, 화면 표시, 조회 |
| ShipmentActivity.java | 서버 전송, 화면 표시, 조회 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |
| ProgressDlgBarcodeSearch.java | 바코드 정보 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 패커상품코드 저장 |
| TB_GOODS_WET | 계근 데이터 패커상품코드 저장 |
| TB_BARCODE_INFO | 바코드 정보 패커상품코드 저장 |

---

## 4. 공통 컬럼 특성

**파일**: DBInfo.java (Line 14)

```java
//  ::::::::::::::: ↓ 공통 Columns ↓ ::::::::::::::::::
public static final String PACKER_PRODUCT_CODE = "PACKER_PRODUCT_CODE";  // 패커 상품코드
```

- TB_SHIPMENT, TB_BARCODE_INFO, TB_GOODS_WET **3개 테이블에서 공통 사용**

---

## 5. 사용 코드

### 5.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 285)

```java
si.setPACKER_PRODUCT_CODE(temp[17].toString());  // 패커상품코드
```

### 5.2 서버 전송 - 계근 데이터 패킷

**파일**: BixolonShipmentActivity.java (Line 3728~3742)

```java
String packet = "";
packet += list_send_info.get(i).getGI_D_ID() + "::";           // 1. 출고상세ID
packet += list_send_info.get(i).getWEIGHT() + "::";            // 2. 중량
packet += list_send_info.get(i).getWEIGHT_UNIT() + "::";       // 3. 중량단위
packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::"; // 4. 패커상품코드 ★
packet += list_send_info.get(i).getBARCODE() + "::";           // 5. 바코드
packet += list_send_info.get(i).getPACKER_CLIENT_CODE() + "::"; // 6. 패커거래처코드
packet += list_send_info.get(i).getMAKINGDATE() + "::";        // 7. 제조일
packet += list_send_info.get(i).getBOXSERIAL() + "::";         // 8. 박스번호
packet += list_send_info.get(i).getBOX_CNT() + "::";           // 9. 계근순서
packet += list_send_info.get(i).getREG_ID() + "::";            // 10. 등록자ID
packet += list_send_info.get(i).getITEM_CODE() + "::";         // 11. 상품코드
packet += list_send_info.get(i).getBRAND_CODE() + "::";        // 12. 브랜드코드
packet += list_send_info.get(i).getCLIENT_TYPE() + "::";       // 13. 거래처유형
packet += list_send_info.get(i).getBOX_ORDER();                // 14. 박스순서
```

### 5.3 계근 데이터 저장 시 설정

**파일**: BixolonShipmentActivity.java (Line 1494)

```java
// 계근 데이터(Goodswets_Info)에 패커상품코드 설정
gi.setPACKER_PRODUCT_CODE(arSM.get(current_work_position).getPACKER_PRODUCT_CODE());
```

### 5.4 화면 표시 - 상품코드 필드

**파일**: ShipmentActivity.java (Line 1629)

```java
// 바코드 스캔 후 상품코드 필드에 표시
edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
```

### 5.5 바코드 정보 조회 조건

**파일**: DBHandler.java

```java
// TB_BARCODE_INFO 조회 시 조건
WHERE PACKER_PRODUCT_CODE = ?
```

---

## 6. 서버 전송 패킷 구조

```
GI_D_ID::WEIGHT::WEIGHT_UNIT::PACKER_PRODUCT_CODE::BARCODE::
PACKER_CLIENT_CODE::MAKINGDATE::BOXSERIAL::BOX_CNT::REG_ID::
ITEM_CODE::BRAND_CODE::CLIENT_TYPE::BOX_ORDER
```

| 순서 | 필드 | 설명 |
|:---:|------|------|
| 1 | GI_D_ID | 출고상세ID |
| 2 | WEIGHT | 중량 |
| 3 | WEIGHT_UNIT | 중량단위 |
| **4** | **PACKER_PRODUCT_CODE** | **패커상품코드** |
| 5 | BARCODE | 스캔한 바코드 |
| ... | ... | ... |

---

## 7. 데이터 흐름

```
1. 서버 VIEW → temp[17] 파싱 → Shipments_Info.PACKER_PRODUCT_CODE
                                      ↓
2. 계근 시 → Goodswets_Info.PACKER_PRODUCT_CODE 설정
                                      ↓
3. 로컬 DB 저장 → TB_GOODS_WET.PACKER_PRODUCT_CODE
                                      ↓
4. 서버 전송 → packet (4번째 필드)
```

---

## 8. 결론

**상태**: ✅ 필수 (삭제 불가)

PACKER_PRODUCT_CODE는 **서버 전송의 필수 컬럼**으로:
- **서버 전송 패킷** 4번째 필드로 포함
- **3개 테이블** (TB_SHIPMENT, TB_GOODS_WET, TB_BARCODE_INFO)에서 공통 사용
- **계근 데이터** 연결의 핵심 키
- **바코드 정보 조회** 조건
- VIEW 원천: **CO_품목코드.ppCode**

---

**최종 수정일**: 2026-02-03
