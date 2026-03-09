# VW_PDA_WID_LIST 컬럼 사용 현황 (종합)

**최종 수정일**: 2026-03-03
**VIEW 용도**: 이마트 출하 계근
**VIEW 위치**: HIGHLAND.VW_PDA_WID_LIST

---

## 1. 전체 요약

| 구분                 |  컬럼 수   | 설명                                   |
| ------------------ | :-----: | ------------------------------------ |
| **VIEW SELECT 출력** | **33개** | CREATE VIEW 정의 컬럼                    |
| **VIEW 내부 전용**     | **13개** | JOIN/WHERE/DECODE에서만 사용 (SELECT 미출력) |
| **과거 제거 완료**       | **5개**  | DB v28에서 JSP/앱에서 삭제 (VIEW 내부에는 잔존)   |
| **총 관련 컬럼**        | **51개** | VIEW 시스템 전체                          |

### 사용 여부별 분류

| 상태 | 수 | 컬럼 |
|:----:|:-:|------|
| ✅ 앱 파싱/사용 | **30** | GI_D_ID ~ EMART_PLANT_CODE |
| ⚠️ 서버 WHERE 조건 | **1** | GR_WAREHOUSE_CODE |
| ⚠️ VIEW 내부 DECODE | **1** | MAJOR_CATEGORY |
| ⚠️ VIEW JOIN/WHERE 필수 | **10** | GI_H_ID, GOODS_R_ID 등 |
| ❌ **미사용 (제거 가능)** | **4** | CONTAINER_TYPE, STORE_NAME, deliverType, SEND_FLAG |
| 🗑️ 과거 제거 완료 | **5** | AMOUNT, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME |

---

## 2. VIEW SELECT 출력 컬럼 (33개)

### 2.1 앱 파싱/사용 컬럼 (30개)

| # | 컬럼명 | JSP전송순서 | 앱파싱 | BixolonShipmentActivity | LabelPrintHelper | 핵심 용도 |
|:-:|--------|:---------:|:------:|------------------------|-----------------|----------|
| 1 | GI_D_ID | 0 | temp[0] | PK, 중복체크, DB조회, 서버전송 | - | **Primary Key** |
| 2 | ITEM_CODE | 1 | temp[1] | Goodswets_Info 저장, 완료패킷 | - | 서버 전송 |
| 3 | ITEM_NAME | 2 | temp[2] | UI 표시 3곳 | - | 화면 표시 |
| 4 | EMARTITEM_CODE | 3 | temp[3] | Goodswets_Info 저장 | **pBarcode 생성** (.substring(0,6)) | **바코드 생성** |
| 5 | EMARTITEM | 4 | temp[4] | Goodswets_Info 저장 | **모든 라벨 상품명** | **라벨 상품명** |
| 6 | GI_REQ_PKG | 5 | temp[5] | 작업완료 판단, 센터합계, 전송확인 | - | **작업 완료 판단** |
| 7 | GI_REQ_QTY | 6 | temp[6] | 중량 표시, 센터합계 | - | 중량 표시/합계 |
| 8 | GI_REQ_DATE | 7 | temp[7] | DBHandler WHERE 조건 | - | DB 조회 조건 |
| 9 | BL_NO | 8 | temp[8] | 스피너 구성, BL매칭 | - | **BL 선택/매칭** |
| 10 | BRAND_CODE | 9 | temp[9] | Goodswets_Info 저장, 완료패킷 | - | 서버 전송 |
| 11 | CLIENT_CODE | 10 | temp[10] | DB 조회 조건 3곳 | - | DB 조회 조건 |
| 12 | CLIENTNAME | 11 | temp[11] | 스피너 표시, 상세다이얼로그 | 라벨: 지점명 추출 | **지점명 표시/추출** |
| 13 | CENTERNAME | 12 | temp[12] | 센터유형 판별 (TRD/WET/E/T) | 라벨: 센터명 + 유통기한 분기 | **센터유형 판별** |
| 14 | ITEM_SPEC | 13 | temp[13] | - | 생산라벨: "상품명/규격" | 생산 라벨 |
| 15 | CT_CODE | 14 | temp[14] | - | 홈플러스 라벨: 차량코드 | 홈플러스 라벨 |
| 16 | IMPORT_ID_NO | 15 | temp[15] | 스피너 표시 | **바코드 12자리** (전 유형) | **바코드 생성** |
| 17 | PACKER_CODE | 16 | temp[16] | 킬코이 판별 ("30228") | 킬코이 판별 | **킬코이 판별** |
| 18 | PACKER_PRODUCT_CODE | 17 | temp[17] | DB조회, 중복체크, UI, 서버전송 | - | DB조회/표시/서버 |
| 19 | BARCODE_TYPE | 18 | temp[18] | - (LabelPrintHelper에 위임) | **마스터 스위치** (M0~M9/E0~E3/P0/L0) | **라벨 형식 분기** |
| 20 | ITEM_TYPE | 19 | temp[19] | 계근방식 분기 (W/S/J/B) | 중량처리 방식 분기 | **계근 방식 결정** |
| 21 | PACKWEIGHT | 20 | temp[20] | J타입 지정중량 | J타입 지정중량 | J타입 고정중량 |
| 22 | BARCODEGOODS | 21 | temp[21] | find_work_info() 바코드조회 | - | 바코드 조회 |
| 23 | STORE_IN_DATE | 22 | temp[22] | - | **모든 라벨 납품일자** | **라벨 납품일자** |
| 24 | EMARTLOGIS_CODE | 23 | temp[23] | - | **pBarcode2**, 미트센터 판별, HP/롯데 코드 | **2차 바코드** |
| 25 | WH_AREA | 24 | temp[24] | - | 이마트/미트/롯데 라벨: 창고구역 대형문자 | 라벨 창고구역 |
| 26 | USE_NAME | 25 | temp[25] | - | M9 라벨: "상품명,용도명" | M9 라벨 표시 |
| 27 | USE_CODE | 26 | temp[26] | - | M9 pBarcode2 구성요소 | M9 바코드 생성 |
| 28 | CT_NAME | 27 | temp[27] | - | M9 라벨: 원산지명 | M9 라벨 표시 |
| 29 | STORE_CODE | 28 | temp[28] | 미트센터 판별 ("9231") | sBarcode, 미트센터, M9, HP 점포코드 | **다용도** |
| 30 | EMART_PLANT_CODE | 29 | temp[29] | - | 미트센터 라벨 분기 + 바코드 | 미트센터 라벨 |

> **참고**: VIEW 순서와 JSP 전송 순서 불일치
> - VIEW: CT_CODE(15) → **PACKER_CODE**(16) → **IMPORT_ID_NO**(17)
> - JSP: CT_CODE(14) → **IMPORT_ID_NO**(15) → **PACKER_CODE**(16)

### 2.2 JSP 전송하지만 앱 미파싱 (2개)

| # | 컬럼명 | JSP전송순서 | 상태 | 설명 |
|:-:|--------|:---------:|:----:|------|
| 31 | **MAJOR_CATEGORY** | 30 | ⚠️ VIEW 내부 | VIEW DECODE에서 BARCODE_TYPE='M9' 결정에 사용. JSP에서 전송되나 앱에서 temp[30]을 읽지 않음 |
| 32 | **CONTAINER_TYPE** | 31 | ❌ **미사용** | VIEW 내부/서버/앱 어디에서도 미사용. JSP에서 전송되나 앱에서 temp[31]을 읽지 않음 |

### 2.3 JSP 미전송 — 서버 WHERE 조건용 (1개)

| # | 컬럼명 | 상태 | 설명 |
|:-:|--------|:----:|------|
| 33 | **GR_WAREHOUSE_CODE** | ⚠️ 서버용 | JSP SELECT에 미포함. ProgressDlgShipSearch.java에서 WHERE절로 전달 |

**창고코드 매핑**:

| 창고명 | GR_WAREHOUSE_CODE | searchType |
|--------|:-----------------:|:----------:|
| 삼일냉장 | IN10273 | 0, 2, 3, 6 |
| SWC | IN60464 | 0, 2, 3, 6 |
| 이천1센터 | 4001 | 0, 2, 3, 6 |
| 부산센터 | 4004 | 0, 2, 3, 6 |
| 탑로지스 | IN63279 | 0, 2, 3, 6 |

---

## 3. VIEW 내부 전용 컬럼 (SELECT 미출력, 13개)

VIEW SQL 내부의 JOIN/WHERE/DECODE에서만 사용되며, SELECT 출력에 포함되지 않는 컬럼.

### 3.1 JOIN 조건 필수 (7개)

| # | 컬럼명 | 소스 테이블 | JOIN 조건 | 비고 |
|:-:|--------|-----------|----------|------|
| 34 | GI_H_ID | W_GOODS_IH / W_GOODS_ID | `IH.GI_H_ID = ID.GI_H_ID` | 출고헤더↔출고상세 연결 |
| 35 | GOODS_R_ID | W_GOODS_ID / W_GOODS_R | `ID.GOODS_R_ID = WR.GOODS_R_ID` | 출고상세↔입고 연결 |
| 36 | BL_D_ID | W_GOODS_R / I_BL_D | `BD.BL_D_ID = WR.BL_D_ID` | 입고↔BL상세 연결 (1st UNION) |
| 37 | BL_S_ID | W_GOODS_R / I_BL_D | `BD.BL_S_ID = WR.BL_S_ID` | 입고↔BL헤더 연결 (1st UNION) |
| 38 | EOI_ID | W_GOODS_ID / EO subquery | `ID.EOI_ID = EO.EOI_ID` | 출고상세↔이마트주문 연결 + JSP ORDER BY |
| 39 | OFFER_D_ID | I_OFFER_D / W_GOODS_R | `OD.OFFER_D_ID = WR.BL_D_ID` | 입고↔오퍼상세 연결 (2nd UNION) |
| 40 | CENTER_CODE | W_EMART_ORDER_ITEM / B_COMMON_CODE | `EOI.CENTER_CODE = BCC.CODE` | 센터코드→센터명/창고구역/센터계근여부 |

### 3.2 WHERE 조건 필수 (2개)

| # | 컬럼명 | 소스 테이블 | WHERE 조건 | 비고 |
|:-:|--------|-----------|-----------|------|
| 41 | PACKING_QTY | W_GOODS_ID | `ID.PACKING_QTY = 0` | 계근 미완료 건만 필터 |
| 42 | CONTRACT_TYPE | W_GOODS_R | 1st: `<> '40'` (해외) / 2nd: `= '40'` (국내) | UNION ALL 분기 기준 |

### 3.3 DECODE 로직 필수 (1개)

| # | 컬럼명 | 소스 테이블 | 용도 | 비고 |
|:-:|--------|-----------|------|------|
| 43 | CENTER_SCALE_USE_YN | B_COMMON_CODE (X subquery) | BARCODE_TYPE='M9' 결정 | DECODE(CENTER_SCALE_USE_YN, 'Y', ...) |

### 3.4 미사용 — 제거 가능 (3개)

| # | 컬럼명 | 소스 테이블 | 현재 상태 | 비고 |
|:-:|--------|-----------|----------|------|
| 44 | **STORE_NAME** | W_EMART_ORDER_ITEM | 서브쿼리에서 SELECT되나 외부 쿼리에서 **미참조** | `EOI.STORE_NAME STORENAME` |
| 45 | **deliverType** | DECODE(EOI.STORE_CODE, EOI.CENTER_CODE, ...) | 서브쿼리에서 DECODE되나 외부 쿼리에서 **미참조** | '직납'/'센터납' |
| 46 | **SEND_FLAG** | W_GOODS_IH | **주석 처리** | `--AND IH.SEND_FLAG = 'Y'` |

---

## 4. 과거 제거 완료 컬럼 (5개)

DB v28에서 로컬DB(TB_SHIPMENT) 스키마와 JSP SELECT/out.println에서 삭제된 컬럼.
VIEW 내부 JOIN에 일부 잔존하나 앱에서는 완전히 제거됨.

| # | 컬럼명 | 과거 JSP idx | 제거 사유 | VIEW 내부 잔존 |
|:-:|--------|:----------:|----------|:----------:|
| 47 | **AMOUNT** | idx 9 | 비즈니스 미사용 | X |
| 48 | **GR_REF_NO** | idx 11 | 비즈니스 미사용 | X |
| 49 | **BRANDNAME** | idx 15 | BRAND_CODE로 대체 | X |
| 50 | **PACKERNAME** | idx 23 | PACKER_CODE로 대체 | X |
| 51 | **EMARTLOGIS_NAME** | idx 31 | EMARTLOGIS_CODE로 대체 | X |

> **참고**: GI_H_ID(과거 idx 0), EOI_ID(과거 idx 2), GOODS_R_ID(과거 idx 10)도
> 과거 JSP에서 전송했으나, 현재는 JSP/앱에서 제거됨. 단, VIEW 내부 JOIN에서 필수이므로 3.1절에 분류.

---

## 5. 미사용 컬럼 상세

### 5.1 CONTAINER_TYPE (VIEW SELECT #32)

```
위치: B_ITEM.CONTAINER_TYPE
상태: VIEW SELECT 출력 → JSP 전송(idx 31) → 앱 미파싱
```

- VIEW 내부 로직: 미사용
- JSP WHERE: 미사용
- 앱 파싱(temp[]): 미파싱 (temp[31] 미참조)
- Shipments_Info DTO: 필드 없음
- 로컬 DB: 컬럼 없음
- BixolonShipmentActivity: 미사용
- LabelPrintHelper: 미사용
- **조치**: VIEW SELECT, JSP SELECT/out.println에서 제거 가능

### 5.2 STORE_NAME (VIEW 내부 #44)

```
위치: W_EMART_ORDER_ITEM.STORE_NAME
상태: 서브쿼리 SELECT → 외부 쿼리 미참조
```

- EO 서브쿼리에서 `EOI.STORE_NAME STORENAME`으로 SELECT
- 외부 쿼리에서 `EO.STORENAME` 참조 없음 (STORE_CODE인 `EO.STORECODE`만 사용)
- **조치**: 서브쿼리 SELECT에서 제거 가능

### 5.3 deliverType (VIEW 내부 #45)

```
위치: EO 서브쿼리 DECODE 결과
상태: 서브쿼리 DECODE → 외부 쿼리 미참조
```

- `DECODE(EOI.STORE_CODE, EOI.CENTER_CODE, '직납', '센터납') deliverType`
- 외부 쿼리에서 `EO.deliverType` 참조 없음
- **조치**: 서브쿼리 SELECT에서 제거 가능

### 5.4 SEND_FLAG (VIEW 내부 #46)

```
위치: W_GOODS_IH.SEND_FLAG
상태: 주석 처리
```

- `--AND IH.SEND_FLAG = 'Y'` (두 UNION 모두 주석)
- 현재 WHERE 조건에서 비활성화
- **조치**: 주석 제거 가능 (기능 영향 없음)

---

## 6. 데이터 흐름

```
┌─────────────────────────────────────────────────────────────┐
│ VIEW (33 SELECT 출력 + 13 내부 전용)                          │
│   UNION ALL: 해외(CONTRACT_TYPE≠'40') + 국내(='40')          │
│   WHERE: PACKING_QTY=0, GI_REQ_PKG≠0, GI_REQ_DATE≥오늘     │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ JSP (search_shipment.jsp)                                    │
│   SELECT: 32컬럼 (33 - GR_WAREHOUSE_CODE)                    │
│   WHERE: GR_WAREHOUSE_CODE 조건 추가 (앱에서 전달)              │
│   ORDER BY: EOI_ID ASC                                       │
│   out.println: 32컬럼을 "::" 구분자로 전송                     │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ ProgressDlgShipSearch.java (앱 파싱)                          │
│   기본 파싱: temp[0]~temp[23] = 24컬럼                        │
│   searchType별 추가:                                         │
│     0,4: temp[24]~temp[29] = +6컬럼 (총 30)                  │
│     5:   temp[24]~temp[28] = +5컬럼 (총 29)                  │
│     6:   temp[24]~temp[25] = +2컬럼 (총 26)                  │
│     1,2,3,7: 추가 없음 (총 24)                                │
│   ※ temp[30](MAJOR_CATEGORY), temp[31](CONTAINER_TYPE) 미파싱 │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────────┐
│ Shipments_Info DTO → DBHandler (TB_SHIPMENT) → 비즈니스 로직   │
│   로컬 DB 저장: 30컬럼 + SAVE_TYPE + SHIPMENT_ID(PK)          │
│     + LAST_BOX_ORDER (searchType=6 전용)                     │
│   BixolonShipmentActivity: 계근, 서버전송, UI표시               │
│   LabelPrintHelper: 바코드 생성, 라벨 출력                     │
└──────────────────────────────────────────────────────────────┘
```

---

## 7. searchType별 파싱 차이

| searchType | 유형 | 기본 24컬럼 | 추가 컬럼 | 합계 |
|:----------:|------|:----------:|----------|:----:|
| 0 | 이마트 출하 | O | WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE | **30** |
| 1 | 생산 계근 | O | - | **24** |
| 2 | 홈플러스 출하 | O | - | **24** |
| 3 | 도매업체 출하 | O | - | **24** |
| 4 | 비정량 출고 | O | WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE | **30** |
| 5 | HP 비정량 | O | WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE | **29** |
| 6 | 롯데 출하 | O | WH_AREA, LAST_BOX_ORDER | **26** |
| 7 | 생산 라벨 | O | - | **24** |

---

## 8. MAJOR_CATEGORY VIEW 내부 로직 상세

```sql
DECODE(
  CENTER_SCALE_USE_YN,          -- B_COMMON_CODE 센터 계근 사용 여부
  'Y',
    DECODE(
      BI.ITEM_TYPE,             -- B_ITEM 아이템타입
      '10',
        DECODE(
          BI.MAJOR_CATEGORY,    -- ★ B_ITEM 대분류
          '10', 'M9',           -- → BARCODE_TYPE을 'M9'로 변환
          EO.BARCODE_TYPE
        ),
      EO.BARCODE_TYPE
    ),
  EO.BARCODE_TYPE
) AS BARCODE_TYPE
```

| CENTER_SCALE_USE_YN | ITEM_TYPE | MAJOR_CATEGORY | 결과 |
|:-------------------:|:---------:|:--------------:|:----:|
| Y | 10 | 10 | **M9** |
| Y | 10 | 그 외 | 원래 값 |
| Y | 그 외 | - | 원래 값 |
| N | - | - | 원래 값 |

---

## 9. 소스 테이블 관계도

```
W_GOODS_IH (출고헤더)
    ├── GI_H_ID ──→ W_GOODS_ID.GI_H_ID (INNER JOIN)
    ├── CLIENT_CODE → CLIENTNAME (DECODE/함수)
    └── GI_REQ_DATE → WHERE 조건

W_GOODS_ID (출고상세)
    ├── GI_D_ID (PK 출력)
    ├── GOODS_R_ID ──→ W_GOODS_R.GOODS_R_ID (INNER JOIN)
    ├── EOI_ID ──→ EO.EOI_ID (INNER JOIN)
    ├── ITEM_CODE ──→ B_ITEM.ITEM_CODE (INNER JOIN)
    ├── PACKING_QTY → WHERE (=0)
    └── GI_REQ_PKG → WHERE (≠0)

W_GOODS_R (입고)
    ├── BL_D_ID ──→ I_BL_D.BL_D_ID (1st UNION) / I_OFFER_D.OFFER_D_ID (2nd)
    ├── BL_S_ID ──→ I_BL_D.BL_S_ID (1st UNION)
    ├── CONTRACT_TYPE → UNION ALL 분기 (≠'40' / ='40')
    └── GR_WAREHOUSE_CODE → 서버 WHERE 조건

I_BL_D (BL상세, 1st UNION) / I_OFFER_D (오퍼상세, 2nd UNION)
    ├── PACKER_CODE
    └── PACKER_PRODUCT_CODE

B_ITEM (품목마스터)
    ├── MAJOR_CATEGORY → BARCODE_TYPE M9 결정
    └── CONTAINER_TYPE → ❌ 미사용

B_SUPPLIER_ITEM (공급처품목, LEFT OUTER JOIN)
    └── EMART_PLANT_CODE → 미트센터 바코드

EO 서브쿼리 (W_EMART_ORDER_ITEM + B_EMART_BARCODE + B_COMMON_CODE)
    ├── CENTERNAME, STORE_CODE, ITEM_CODE, ITEM_NAME
    ├── BARCODE_TYPE, ITEM_TYPE, PACKWEIGHT
    ├── STORE_IN_DATE, EMARTLOGIS_CODE, WH_AREA
    ├── USE_NAME, USE_CODE, CENTER_SCALE_USE_YN
    ├── STORE_NAME → ❌ 미사용 (미참조)
    └── deliverType → ❌ 미사용 (미참조)
```

---

## 10. 결론

| 구분 | 수 | 조치 |
|------|:-:|------|
| 유지 필요 | **42** | 앱/서버/VIEW JOIN/WHERE/DECODE에서 사용 |
| **제거 가능** | **4** | CONTAINER_TYPE, STORE_NAME, deliverType, SEND_FLAG |
| 제거 완료 | **5** | AMOUNT, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME |

### 제거 가능 컬럼별 수정 위치

| 컬럼 | 수정 파일 | 수정 내용 |
|------|----------|----------|
| **CONTAINER_TYPE** | VW_PDA_WID_LIST | CREATE VIEW 컬럼 목록에서 제거 |
| | VW_PDA_WID_LIST (line 97, 234) | SELECT에서 `BI.CONTAINER_TYPE` 제거 |
| | search_shipment.jsp (line 74, 99) | SELECT/out.println에서 제거 |
| **STORE_NAME** | VW_PDA_WID_LIST (line 112, 249) | 서브쿼리 SELECT에서 `EOI.STORE_NAME STORENAME` 제거 |
| **deliverType** | VW_PDA_WID_LIST (line 123, 259) | 서브쿼리 SELECT에서 `DECODE(...) deliverType` 제거 |
| **SEND_FLAG** | VW_PDA_WID_LIST (line 160, 298) | 주석 라인 제거 |

---

## 11. 관련 파일

| 파일 | 용도 |
|------|------|
| `app/doc/view/VW_PDA_WID_LIST` | VIEW SQL 정의 |
| `app/doc/JSP/search_shipment.jsp` | 서버 조회 JSP (이마트, searchType=0) |
| `ProgressDlgShipSearch.java` | 서버 조회 및 응답 파싱 |
| `Shipments_Info.java` | 출하대상 DTO |
| `DBInfo.java` | 컬럼명 상수 정의 |
| `DBHandler.java` | 로컬 DB 저장/조회 (TB_SHIPMENT) |
| `BixolonShipmentActivity.java` | 계근/라벨 출력 비즈니스 로직 |
| `LabelPrintHelper.java` | 라벨 인쇄 로직 |
| `app/doc/column/10_VW_PDA_WID_LIST.md` | 개별 컬럼 상세 분석 (과거 38컬럼 포함) |
