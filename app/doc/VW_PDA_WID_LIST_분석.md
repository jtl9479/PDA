# VW_PDA_WID_LIST 분석 문서

**소스 기반 재분석**

---

## 개요

| 항목 | 내용 |
|------|------|
| **작성일** | 2026-01-13 (소스 기반 재작성) |
| **VIEW명** | VW_PDA_WID_LIST |
| **용도** | 이마트 출하 계근 (searchType = "0") |
| **서버 컬럼 수** | 38개 (temp[0]~temp[37]) |
| **사용 컬럼** | 33개 |
| **미사용 컬럼** | 5개 |

---

## 1. 분석 소스 파일

| 파일 | 역할 |
|------|------|
| `ProgressDlgShipSearch.java` | 서버 데이터 파싱 (temp[0]~temp[37]) |
| `BixolonShipmentActivity.java` | Activity 비즈니스 로직 |
| `ShipmentActivity.java` | 원본 Activity (참조용) |
| `DBHandler.java` | 로컬 DB (TB_SHIPMENT) 스키마 |
| `Shipments_Info.java` | 데이터 모델 (DTO) |
| `column/VW_PDA_WID_LIST.md` | 기존 상세 분석 문서 |

---

## 2. 서버 응답 컬럼 매핑 (searchType "0")

### 2.1 기본 컬럼 (temp[0]~temp[31])

| Index | 컬럼명 | 설명 | Activity 사용 |
|:-----:|--------|------|:-------------:|
| 0 | GI_H_ID | 출고헤더ID | - |
| 1 | GI_D_ID | 출고상세ID (PK) | **O** |
| 2 | EOI_ID | 이마트 주문ID | - |
| 3 | ITEM_CODE | 상품코드 | **O** |
| 4 | ITEM_NAME | 상품명 | **O** |
| 5 | EMARTITEM_CODE | 이마트 상품코드 | **O** |
| 6 | EMARTITEM | 이마트 상품명 | **O** |
| 7 | GI_REQ_PKG | 출하요청수량 | **O** |
| 8 | GI_REQ_QTY | 출하요청중량 | **O** |
| 9 | AMOUNT | 금액 | **X (미사용)** |
| 10 | GOODS_R_ID | 입고ID | - |
| 11 | GR_REF_NO | 입고참조번호 | **X (미사용)** |
| 12 | GI_REQ_DATE | 출하요청일 | **O** |
| 13 | BL_NO | BL번호 | **O** |
| 14 | BRAND_CODE | 브랜드코드 | **O** |
| 15 | BRANDNAME | 브랜드명 | **X (미사용)** |
| 16 | CLIENT_CODE | 거래처코드 | **X (미사용)** |
| 17 | CLIENTNAME | 거래처명 | **O** |
| 18 | CENTERNAME | 센터명 | **O** |
| 19 | ITEM_SPEC | 상품규격 | **O** |
| 20 | CT_CODE | 원산지코드 | **O** |
| 21 | IMPORT_ID_NO | 수입식별번호 | **O** |
| 22 | PACKER_CODE | 패커코드 | **O** |
| 23 | PACKERNAME | 패커명 | - |
| 24 | PACKER_PRODUCT_CODE | 패커상품코드 | **O** |
| 25 | BARCODE_TYPE | 바코드타입 | **O** |
| 26 | ITEM_TYPE | 아이템타입 | **O** |
| 27 | PACKWEIGHT | 포장중량 | **O** |
| 28 | BARCODEGOODS | 바코드상품코드 | **O** |
| 29 | STORE_IN_DATE | 입고일자 | **O** |
| 30 | EMARTLOGIS_CODE | 이마트물류코드 | **O** |
| 31 | EMARTLOGIS_NAME | 이마트물류명 | - |

### 2.2 추가 컬럼 (temp[32]~temp[37], searchType "0"/"4" 전용)

| Index | 컬럼명 | 설명 | Activity 사용 |
|:-----:|--------|------|:-------------:|
| 32 | WH_AREA | 창고구역 | **O** |
| 33 | USE_NAME | 용도명 | **O** |
| 34 | USE_CODE | 용도코드 | **O** |
| 35 | CT_NAME | 원산지명 | **O** |
| 36 | STORE_CODE | 점포코드 | **O** |
| 37 | EMART_PLANT_CODE | 이마트공장코드 | **O** |

---

## 3. 미사용 컬럼 상세 분석

### 3.1 미사용 컬럼 목록 (5개)

| 컬럼명 | Index | 미사용 근거 |
|--------|:-----:|------------|
| **AMOUNT** | 9 | UI 미노출, 서버 전송 X, 분기 조건 X |
| **GR_REF_NO** | 11 | UI 미노출, 서버 전송 X, 분기 조건 X |
| **BRANDNAME** | 15 | UI 미노출, BRAND_CODE만 사용 |
| **CLIENT_CODE** | 16 | DB 함수 파라미터로 받지만 WHERE에 미사용 |
| **CONTAINER_TYPE** | - | VIEW SELECT에는 있으나 앱에 미전달 |

### 3.2 미사용 판정 기준

```
┌─────────────────────────────────────────────────────────────┐
│  미사용 판정 = 아래 조건을 모두 만족                           │
├─────────────────────────────────────────────────────────────┤
│  1. UI 노출: X (화면에 표시 안됨)                             │
│  2. 분기 조건: X (if문 조건으로 미사용)                        │
│  3. 서버 전송: X (계근 완료 시 서버로 미전송)                   │
│  4. 바코드/라벨: X (바코드 생성, 라벨 출력에 미사용)            │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. 사용 컬럼 상세 분석

### 4.1 핵심 식별자 (3개)

| 컬럼명 | 용도 | 사용 위치 |
|--------|------|----------|
| GI_D_ID | 출고상세 PK, 서버 전송 식별자 | 전역 사용 |
| EOI_ID | 이마트 주문번호 | DB 저장 |
| GI_H_ID | 출고헤더 ID | VIEW JOIN |

### 4.2 UI 표시용 (6개)

| 컬럼명 | 표시 내용 | 사용 위치 |
|--------|----------|----------|
| ITEM_NAME | 상품명 | edit_product_name |
| CLIENTNAME | 지점명 | 리스트 표시 |
| PACKER_PRODUCT_CODE | 패커상품코드 | edit_product_code |
| GI_REQ_PKG | 요청수량 | edit_wet_count |
| GI_REQ_QTY | 요청중량 | edit_wet_weight |
| CENTERNAME | 센터명 | 스피너 표시 |

### 4.3 계근 로직 분기용 (5개)

| 컬럼명 | 분기 조건 | 설명 |
|--------|----------|------|
| ITEM_TYPE | W/HW/S/J/B | 계근 방식 결정 |
| BARCODE_TYPE | M0~M9, E0~E3, P0 | 라벨 출력 분기 |
| PACKER_CODE | 30228 (킬코이) | 미트센터 특수 처리 |
| STORE_CODE | 9231 (미트센터) | 미트센터 판별 |
| CENTERNAME | TRD/WET/E/T 포함 | 센터 유형 판별 |

### 4.4 바코드/라벨 출력용 (10개)

| 컬럼명 | 용도 |
|--------|------|
| EMARTITEM_CODE | 바코드 앞 6자리 |
| EMARTLOGIS_CODE | 물류 바코드 |
| IMPORT_ID_NO | 수입식별번호/이력번호 |
| USE_CODE | 원료육 용도코드 |
| USE_NAME | 원료육 용도명 |
| CT_NAME | 원산지명 |
| WH_AREA | 창고코드 |
| STORE_CODE | 점포코드 |
| EMART_PLANT_CODE | 가공장 코드 |
| ITEM_SPEC | 상품 스펙 |

### 4.5 서버 전송용 (6개)

| 컬럼명 | 전송 용도 |
|--------|----------|
| GI_D_ID | 식별자 |
| ITEM_CODE | 상품코드 |
| BRAND_CODE | 브랜드코드 |
| EMARTITEM_CODE | 이마트 상품코드 |
| EMARTITEM | 이마트 상품명 |
| PACKER_PRODUCT_CODE | 패커 상품코드 |

---

## 5. ITEM_TYPE별 처리

| ITEM_TYPE | 설명 | 중량 추출 방식 | 파일:라인 |
|-----------|------|--------------|----------|
| **W** | 바코드 계근 | 바코드에서 중량 추출 (WEIGHT_FROM~TO) | :1278 |
| **HW** | 바코드 계근 (LB) | LB→KG 환산 (×0.453592) | :1302 |
| **S** | 저울 계근 | 저울에서 중량 입력 (소수점 2자리) | :1331 |
| **J** | 지정 중량 | PACKWEIGHT 값 사용 | :1382 |
| **B** | 홈플러스 비정량 | 바코드에서 중량 추출 | :1393 |

---

## 6. 조회 조건

### 6.1 서버 WHERE 조건 (ProgressDlgShipSearch.java)

```java
// 기본 조건
String data = " WHERE GI_REQ_DATE = '" + Common.selectDay + "'";

// 창고별 조건 (searchType "0")
if(Common.selectWarehouse.equals("삼일냉장")){
    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
}else if(Common.selectWarehouse.equals("SWC")){
    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
}else if(Common.selectWarehouse.equals("이천1센터")){
    data += " AND GR_WAREHOUSE_CODE = '4001'";
}else if(Common.selectWarehouse.equals("부산센터")){
    data += " AND GR_WAREHOUSE_CODE = '4004'";
}else if(Common.selectWarehouse.equals("탑로지스")){
    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
}
```

### 6.2 VIEW 내부 WHERE 조건 (고정)

```sql
WHERE ID.PACKING_QTY = 0           -- 미계근 건만
  AND ID.GI_REQ_PKG <> 0           -- 요청수량 있는 건만
  AND EO.EOI_ID IS NOT NULL        -- 이마트 주문 있는 건만
  AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')  -- 오늘 이후
```

---

## 7. 로컬 DB 스키마 (TB_SHIPMENT)

**DBHandler.java:32~75**

```sql
CREATE TABLE IF NOT EXISTS TB_SHIPMENT (
    SHIPMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    GI_H_ID TEXT NOT NULL,
    GI_D_ID TEXT NOT NULL,
    EOI_ID TEXT NOT NULL,
    ITEM_CODE TEXT NOT NULL,
    ITEM_NAME TEXT NOT NULL,
    EMARTITEM_CODE TEXT,
    EMARTITEM TEXT,
    GI_REQ_PKG TEXT NOT NULL,
    GI_REQ_QTY TEXT NOT NULL,
    AMOUNT TEXT NOT NULL,
    GOODS_R_ID TEXT NOT NULL,
    GR_REF_NO TEXT NOT NULL,
    GI_REQ_DATE TEXT NOT NULL,
    BL_NO TEXT NOT NULL,
    BRAND_CODE TEXT NOT NULL,
    BRANDNAME TEXT NOT NULL,
    CLIENT_CODE TEXT NOT NULL,
    CLIENTNAME TEXT NOT NULL,
    CENTERNAME TEXT,
    ITEM_SPEC TEXT NOT NULL,
    CT_CODE TEXT NOT NULL,
    IMPORT_ID_NO TEXT NOT NULL,
    PACKER_CODE TEXT NOT NULL,
    PACKERNAME TEXT NOT NULL,
    PACKER_PRODUCT_CODE TEXT NOT NULL,
    BARCODE_TYPE TEXT NOT NULL,
    ITEM_TYPE TEXT NOT NULL,
    PACKWEIGHT TEXT,
    BARCODEGOODS TEXT,
    STORE_IN_DATE TEXT,
    EMARTLOGIS_CODE TEXT,
    EMARTLOGIS_NAME TEXT,
    SAVE_TYPE TEXT NOT NULL,
    WH_AREA TEXT,
    USE_NAME TEXT,
    USE_CODE TEXT,
    CT_NAME TEXT,
    STORE_CODE TEXT,
    EMART_PLANT_CODE TEXT,
    LAST_BOX_ORDER INTEGER
)
```

---

## 8. 기존 문서 참조

| 문서 | 내용 |
|------|------|
| `column/VW_PDA_WID_LIST.md` | 컬럼별 상세 분석 (UI/VIEW/Activity/DDL 사용 여부) |
| `VW_PDA_WID_LIST.md` | 컬럼 사용 현황 요약 (2025-12-05) |
| `column/VIEW_COLUMN_COMPARISON.md` | VIEW간 컬럼 비교 |
| `column/COMMON_COLUMN_USAGE_BY_SEARCHTYPE.md` | searchType별 공통 컬럼 |

---

## 9. 요약

### 9.1 컬럼 사용 현황

| 분류 | 개수 | 컬럼명 |
|------|:----:|--------|
| 사용 | 33 | GI_H_ID, GI_D_ID, EOI_ID, ITEM_CODE, ITEM_NAME, EMARTITEM_CODE, EMARTITEM, GI_REQ_PKG, GI_REQ_QTY, GOODS_R_ID, GI_REQ_DATE, BL_NO, BRAND_CODE, CLIENTNAME, CENTERNAME, ITEM_SPEC, CT_CODE, IMPORT_ID_NO, PACKER_CODE, PACKERNAME, PACKER_PRODUCT_CODE, BARCODE_TYPE, ITEM_TYPE, PACKWEIGHT, BARCODEGOODS, STORE_IN_DATE, EMARTLOGIS_CODE, EMARTLOGIS_NAME, WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE |
| 미사용 | 5 | AMOUNT, GR_REF_NO, BRANDNAME, CLIENT_CODE, CONTAINER_TYPE |

### 9.2 미사용 컬럼 제거 시 영향

| 컬럼명 | JSP 수정 | 앱 수정 | VIEW 수정 |
|--------|:--------:|:-------:|:---------:|
| AMOUNT | O | O | X |
| GR_REF_NO | O | O | X |
| BRANDNAME | O | O | X |
| CLIENT_CODE | O | O | X |
| CONTAINER_TYPE | X | X | O |

---

**최종 수정일**: 2026-01-13 (소스 기반 재분석)
