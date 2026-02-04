# VIEW - 로컬DB 컬럼 비교 (소스 기반 분석)

**작성일**: 2026-01-16
**분석 방법**: 전체 41개 컬럼의 getter 호출 위치를 소스코드 grep으로 검증

---

## 1. 분석 방법

각 컬럼의 getter 메소드 호출 위치를 검색하여 실제 사용 여부 판단:
- **Y**: Activity/Adapter 등 비즈니스 로직에서 getter 호출
- **DB**: DBHandler.java INSERT 쿼리에서만 getter 호출 (저장만, 읽기 없음)
- **N**: getter 호출 없음

검색 패턴: `\.getXXX\(\)` → 호출 위치 분석

---

## 2. 전체 41개 컬럼 소스 분석 결과

### 2.1 VIEW → 로컬DB 매핑 컬럼 (38개)

| # | VIEW Idx | 컬럼명 | 사용여부 | 호출 위치 | 용도 |
|:-:|:--------:|--------|:--------:|-----------|------|
| 1 | 0 | GI_H_ID | **DB** | DBHandler:663 | INSERT 쿼리만 |
| 2 | 1 | GI_D_ID | Y | BixolonShipmentActivity, ShipmentActivity, Adapter 등 다수 | 서버전송 PK, 화면표시 |
| 3 | 2 | EOI_ID | **DB** | DBHandler:665 | INSERT 쿼리만 |
| 4 | 3 | ITEM_CODE | Y | Activity 다수, packet 포함 | 서버전송, 화면표시 |
| 5 | 4 | ITEM_NAME | Y | Activity, Adapter | 화면표시 |
| 6 | 5 | EMARTITEM_CODE | Y | Activity 다수 (substring 바코드생성) | 바코드생성 |
| 7 | 6 | EMARTITEM | Y | Activity (TB_GOODS_WET 저장) | 라벨출력 |
| 8 | 7 | GI_REQ_PKG | Y | Activity 다수 | 화면표시, 로직분기 |
| 9 | 8 | GI_REQ_QTY | Y | Activity | 화면표시 |
| 10 | 9 | AMOUNT | **DB** | DBHandler:670 | INSERT 쿼리만 |
| 11 | 10 | GOODS_R_ID | **DB** | DBHandler:673 | INSERT 쿼리만 |
| 12 | 11 | GR_REF_NO | **DB** | DBHandler:674 | INSERT 쿼리만 |
| 13 | 12 | GI_REQ_DATE | Y | DBHandler:106 WHERE 조건 | DB 조회 필터 |
| 14 | 13 | BL_NO | Y | Activity, Adapter | 화면표시, 선택, 중복방지 |
| 15 | 14 | BRAND_CODE | Y | Activity (packet, complete_string) | 서버전송, 바코드 |
| 16 | 15 | BRANDNAME | **DB** | DBHandler:678 | INSERT 쿼리만 |
| 17 | 16 | CLIENT_CODE | **DB** | DBHandler (파라미터만, WHERE 미사용) | 저장만 |
| 18 | 17 | CLIENTNAME | Y | Activity, Adapter | 화면표시 |
| 19 | 18 | CENTERNAME | Y | Activity | 로직분기, 라벨 |
| 20 | 19 | ITEM_SPEC | Y | Activity (라벨출력) | 라벨 인쇄 (상품명/냉장냉동) |
| 21 | 20 | CT_CODE | Y | Activity | 라벨출력 |
| 22 | 21 | IMPORT_ID_NO | Y | Activity 다수 | 바코드생성, 라벨출력 |
| 23 | 22 | PACKER_CODE | Y | Activity (KILKOY 체크) | 로직분기 |
| 24 | 23 | PACKERNAME | **DB** | DBHandler:686 | INSERT 쿼리만 |
| 25 | 24 | PACKER_PRODUCT_CODE | Y | Activity, packet | 서버전송, 화면표시 |
| 26 | 25 | BARCODE_TYPE | Y | Activity (switch문 다수) | 로직분기 |
| 27 | 26 | ITEM_TYPE | Y | Activity | 로직분기 |
| 28 | 27 | PACKWEIGHT | Y | Activity | 제품계근 계산 |
| 29 | 28 | BARCODEGOODS | Y | Activity | 바코드 매칭 |
| 30 | 29 | STORE_IN_DATE | Y | Activity | 라벨출력 (납품일자) |
| 31 | 30 | EMARTLOGIS_CODE | Y | Activity 다수 (50+ 호출) | 바코드생성, 로직분기 |
| 32 | 31 | EMARTLOGIS_NAME | **DB** | DBHandler:694 | INSERT 쿼리만 |
| 33 | 32 | WH_AREA | Y | Activity 다수 | 라벨출력 (창고구역) |
| 34 | 33 | USE_NAME | Y | Activity | 라벨출력 (용도명) |
| 35 | 34 | USE_CODE | Y | Activity (바코드조합) | 바코드생성 |
| 36 | 35 | CT_NAME | Y | Activity | 라벨출력 (원산지명) |
| 37 | 36 | STORE_CODE | Y | Activity 다수 (20+ 호출) | 로직분기, 바코드, 라벨 |
| 38 | 37 | EMART_PLANT_CODE | Y | Activity (조건분기, 바코드) | 바코드생성, 로직분기 |

### 2.2 로컬DB 전용 컬럼 (3개)

| # | 로컬 # | 컬럼명 | 사용여부 | 호출 위치 | 비고 |
|:-:|:------:|--------|:--------:|-----------|------|
| 1 | 1 | SHIPMENT_ID | **N** | 호출 없음 | PK 자동생성, getter 미호출 |
| 2 | 34 | SAVE_TYPE | Y | Activity, Adapter 다수 (12+ 호출) | 전송상태 확인 ("F", "Y") |
| 3 | 41 | LAST_BOX_ORDER | Y | BixolonShipmentActivity:3348, ShipmentActivity:3263 | 롯데 박스순서 (Log 출력) |

### 2.3 VIEW 전용 (앱 미전달) 컬럼 (3개)

| # | 컬럼명 | 비고 |
|:-:|--------|------|
| 1 | GR_WAREHOUSE_CODE | WHERE 조건만 사용 |
| 2 | MAJOR_CATEGORY | VIEW 내부 BARCODE_TYPE 결정용 |
| 3 | CONTAINER_TYPE | JSP 필터링용 |

---

## 3. 사용여부 요약

| 사용여부 | 컬럼 수 | 설명 |
|:--------:|:-------:|------|
| Y | 29 | Activity/Adapter에서 비즈니스 로직에 사용 |
| DB | 9 | DBHandler INSERT 쿼리에서만 사용 (저장만) |
| N | 1 | getter 호출 없음 (SHIPMENT_ID) |
| - | 3 | VIEW 전용 (앱 미전달) |
| **합계** | **42** | VIEW 38 + 로컬전용 3 + VIEW전용 3 - 중복 2 |

---

## 4. DB만 사용 컬럼 상세 (9개)

| # | 컬럼명 | DBHandler 라인 | 설명 |
|:-:|--------|:--------------:|------|
| 1 | GI_H_ID | 663 | 출고헤더ID |
| 2 | EOI_ID | 665 | 이마트 주문ID |
| 3 | AMOUNT | 670 | 출하상품금액 |
| 4 | GOODS_R_ID | 673 | 입고번호 |
| 5 | GR_REF_NO | 674 | 창고입고번호 |
| 6 | BRANDNAME | 678 | 브랜드명 |
| 7 | CLIENT_CODE | 679 | 거래처코드 (WHERE 미사용) |
| 8 | PACKERNAME | 686 | 패커명 |
| 9 | EMARTLOGIS_NAME | 694 | 이마트물류명 |

---

## 5. 기존 문서 오류 수정 내역

### 5.1 로컬DB_구조.md 수정 필요 (3개)

| 컬럼명 | 기존 | 수정 | 근거 |
|--------|------|------|------|
| GI_H_ID | Y (서버전송 packet) | **DB** | packet에 미포함, DBHandler:663만 호출 |
| EOI_ID | Y (서버전송 packet) | **DB** | packet에 미포함, DBHandler:665만 호출 |
| GOODS_R_ID | Y (서버전송 packet) | **DB** | packet에 미포함, DBHandler:673만 호출 |

### 5.2 VW_PDA_WID_LIST.md - 정확함

ITEM_SPEC: 라벨출력으로 정확히 기재됨 (si.ITEM_SPEC 직접 필드 접근 사용)

### 5.3 LAST_BOX_ORDER 재분류

| 컬럼명 | 기존 | 수정 | 근거 |
|--------|------|------|------|
| LAST_BOX_ORDER | N (getter 미호출) | **Y** | BixolonShipmentActivity:3348, ShipmentActivity:3263에서 Log 출력용 호출 |

---

## 6. 서버 전송 패킷 분석

**BixolonShipmentActivity.java:3729-3742** 서버 전송 패킷 구성:

```java
packet += GI_D_ID + "::"
packet += WEIGHT + "::"
packet += WEIGHT_UNIT + "::"
packet += PACKER_PRODUCT_CODE + "::"
packet += BARCODE + "::"
packet += PACKER_CLIENT_CODE + "::"
packet += MAKINGDATE + "::"
packet += BOXSERIAL + "::"
packet += BOX_CNT + "::"
packet += REG_ID + "::"
packet += ITEM_CODE + "::"
packet += BRAND_CODE + "::"
packet += CLIENT_TYPE + "::"
packet += BOX_ORDER
```

**패킷에 포함된 TB_SHIPMENT 컬럼**: GI_D_ID, ITEM_CODE, BRAND_CODE
**패킷에 미포함된 컬럼**: GI_H_ID, EOI_ID, GOODS_R_ID (기존 문서 오류)

---

## 7. VIEW idx ↔ 로컬DB # 인덱스 오프셋

VIEW와 로컬DB 컬럼 순서가 다름. 로컬 전용 컬럼이 중간에 삽입되어 오프셋 발생.

### 7.1 로컬 전용 컬럼 위치

| 로컬DB # | 컬럼명 | VIEW idx | 삽입 위치 |
|:--------:|--------|:--------:|----------|
| 1 | SHIPMENT_ID | - | 맨 앞 (PK) |
| 34 | SAVE_TYPE | - | 중간 (EMARTLOGIS_NAME 다음) |
| 41 | LAST_BOX_ORDER | - | 맨 뒤 (롯데만 VIEW 수신) |

### 7.2 오프셋 계산

| 구간 | VIEW idx | 로컬DB # | 오프셋 | 이유 |
|------|:--------:|:--------:|:------:|------|
| 전반부 | 0 ~ 31 | 2 ~ 33 | **+2** | #1 SHIPMENT_ID |
| 후반부 | 32 ~ 37 | 35 ~ 40 | **+3** | #1 SHIPMENT_ID + #34 SAVE_TYPE |

### 7.3 오프셋 발생 지점

```
VIEW idx 31: EMARTLOGIS_NAME → 로컬DB #33
             ─────────────────────────────
             로컬DB #34: SAVE_TYPE (로컬 전용)
             ─────────────────────────────
VIEW idx 32: WH_AREA        → 로컬DB #35  (+3)
VIEW idx 33: USE_NAME       → 로컬DB #36  (+3)
VIEW idx 34: USE_CODE       → 로컬DB #37  (+3)
VIEW idx 35: CT_NAME        → 로컬DB #38  (+3)
VIEW idx 36: STORE_CODE     → 로컬DB #39  (+3)
VIEW idx 37: EMART_PLANT_CODE → 로컬DB #40  (+3)
```

### 7.4 소스 위치

**DBHandler.java:32-75** (CREATE TABLE)

```java
+ DBInfo.EMARTLOGIS_NAME + " TEXT, "   // Line 66, 로컬DB #33
+ DBInfo.SAVE_TYPE + " TEXT NOT NULL," // Line 67, 로컬DB #34 ← 여기서 삽입
+ DBInfo.WH_AREA + " TEXT,"            // Line 68, 로컬DB #35
```

---

## 8. 데이터 흐름

```
서버 VIEW (VW_PDA_WID_LIST 등)
       │
       │ SELECT (38개 컬럼, 인덱스 기반)
       ▼
ProgressDlgShipSearch.java
       │
       │ temp[0], temp[1], ... temp[37] 파싱
       ▼
Shipments_Info 객체 (41 필드)
       │
       ├─→ DBHandler.insertqueryShipment() → TB_SHIPMENT (41 컬럼)
       │   (VIEW 38 + 로컬전용 3: SHIPMENT_ID, SAVE_TYPE, LAST_BOX_ORDER)
       │
       └─→ Activity 비즈니스 로직 (29개 컬럼 사용)
               │
               └─→ 서버 전송 packet (14개 필드)
```

---

**분석 완료일**: 2026-01-16
**최종 수정일**: 2026-01-16
**분석 방법**: grep `\.getXXX\(\)` 패턴으로 전체 소스코드 검색
