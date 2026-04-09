# 이마트(VW_PDA_WID_LIST) vs 비정량(VW_PDA_WID_LIST_NONFIXED) Oracle VIEW 비교 분석

## 개요

이마트 VIEW와 비정량 VIEW의 컬럼 구조, 데이터 출처, WHERE 조건, JOIN 구조를 비교 분석하여
비정량 JSP의 MSSQL 전환 시 주의사항을 도출한다.

- **대상 파일 (이마트 VIEW)**: `app/doc/view/VW_PDA_WID_LIST`
- **대상 파일 (비정량 VIEW)**: `app/doc/view/VW_PDA_WID_LIST_NONFIXED`
- **이마트 MSSQL JSP**: `search_shipment.jsp` (전환 완료, 31개 컬럼 직접 JOIN)
- **비정량 JSP**: `search_production_nonfixed.jsp` (미전환, VIEW 기반 37개)
- **타입**: VIEW 비교 분석
- **작성일**: 2026-04-07

---

## 1. 역할

- 이마트 VIEW(VW_PDA_WID_LIST)와 비정량 VIEW(VW_PDA_WID_LIST_NONFIXED)의 구조적 차이 파악
- 비정량 JSP MSSQL 전환 시 이마트 JSP를 기반으로 재사용 가능한 부분과 변경 필요 부분 식별
- 데이터 출처가 다른 핵심 5개 컬럼의 전환 방법 도출

---

## 2. 주요 상수/필드

| 항목 | 이마트 VIEW | 비정량 VIEW | 비고 |
|------|------------|-----------|------|
| 스키마 | HIGHLAND | INNO | 다름 |
| VIEW명 | VW_PDA_WID_LIST | VW_PDA_WID_LIST_NONFIXED | - |
| 총 컬럼 수 | 33개 (CREATE VIEW 기준) | 30개 | - |
| 분석문서 컬럼 수 | 41개 (JSP 전송 포함) | 37개 (JSP 전송 포함) | - |
| 구조 | UNION ALL (해외매입 + 국내매입) | 단일 SELECT (UNION 없음) | 핵심 차이 |
| searchType | 0 (이마트) | 4 (비정량) | - |

---

## 3. 주요 메서드

해당 없음 (VIEW 비교 분석 문서)

---

## 4. 호출 관계

### 4.1 이마트 VIEW 호출

| 호출 위치 | 용도 |
|----------|------|
| search_shipment.jsp (원본) | 이마트 출하대상 조회 (Oracle VIEW 기반, 현재 미사용) |
| search_shipment.jsp (MSSQL) | 이마트 출하대상 조회 (직접 JOIN, 전환 완료) |

### 4.2 비정량 VIEW 호출

| 호출 위치 | 용도 |
|----------|------|
| search_production_nonfixed.jsp | 비정량 출하대상 조회 (Oracle VIEW 기반, 미전환) |

---

## 5. 데이터 흐름

```
[이마트 전환 완료 상태]
search_shipment.jsp → MSSQL 직접 JOIN (31개 컬럼) → PDA 앱 파싱

[비정량 미전환 상태]
search_production_nonfixed.jsp → Oracle VW_PDA_WID_LIST_NONFIXED (37개 컬럼) → PDA 앱 파싱

[비정량 전환 목표]
search_production_nonfixed.jsp → MSSQL 직접 JOIN (이마트 JSP 기반 + 비정량 차이 반영) → PDA 앱 파싱
```

---

## 6. 핵심 코드 — A. 두 VIEW 컬럼 대조표

### A.1 이마트 VIEW 33개 vs 비정량 VIEW 30개 컬럼 대조

| # | 컬럼명 | 이마트 VIEW | 비정량 VIEW | 동일여부 | 비고 |
|:-:|--------|:----------:|:---------:|:------:|------|
| 1 | GI_D_ID | O | O | 동일 | ID.GI_D_ID |
| 2 | ITEM_CODE | O | O | 동일 | ID.ITEM_CODE |
| 3 | ITEM_NAME | O | O | 동일 | DECODE(EO.ITEM_NAME, NULL, DE_ITEM()) |
| 4 | EMARTITEM_CODE | O | O | 동일 | EO.ITEM_CODE |
| 5 | EMARTITEM | O | O | 동일 | EO.ITEM_NAME |
| 6 | GI_REQ_PKG | O | O | 동일 | ID.GI_REQ_PKG |
| 7 | GI_REQ_QTY | O | O | 동일 | ID.GI_REQ_QTY |
| 8 | GI_REQ_DATE | O | O | 동일 | GI_REQ_DATE |
| 9 | BL_NO | O | O | 동일 | DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) |
| 10 | BRAND_CODE | O | O | 동일 | ID.BRAND_CODE |
| 11 | CLIENT_CODE | O | O | 동일 | IH.CLIENT_CODE |
| 12 | CLIENTNAME | O | O | 동일 | DECODE(센터명CJ...) |
| 13 | CENTERNAME | O | O | 동일 | EO.CENTERNAME |
| 14 | ITEM_SPEC | O | O | 동일 | WR.ITEM_SPEC |
| 15 | CT_CODE | O | O | 동일 | WR.CT_CODE |
| 16 | **PACKER_CODE** | O | O | **다름** | 이마트: BD.PACKER_CODE / OD.PACKER_CODE, 비정량: **'0000' 고정값** |
| 17 | IMPORT_ID_NO | O | O | 동일 | WR.IMPORT_ID_NO |
| 18 | **PACKER_PRODUCT_CODE** | O | O | **다름** | 이마트: BD/OD.PACKER_PRODUCT_CODE, 비정량: **ID.ITEM_CODE** |
| 19 | **BARCODE_TYPE** | O | O | **다름** | 이마트: CENTER_SCALE_USE_YN DECODE 로직, 비정량: **EO.BARCODE_TYPE 직접** |
| 20 | **ITEM_TYPE** | O | O | **다름** | 이마트: EO.ITEM_TYPE, 비정량: **'HW' 고정값** |
| 21 | PACKWEIGHT | O | O | 동일 | EO.PACKWEIGHT |
| 22 | **BARCODEGOODS** | O | O | **다름** | 이마트: S_BARCODE_INFO 서브쿼리, 비정량: **ID.ITEM_CODE** |
| 23 | STORE_IN_DATE | O | O | 동일 | STORE_IN_DATE |
| 24 | GR_WAREHOUSE_CODE | O | O | 동일 | WR.GR_WAREHOUSE_CODE |
| 25 | EMARTLOGIS_CODE | O | O | 동일 | DECODE(NULL, '0000000', EO.EMARTLOGIS_CODE) |
| 26 | WH_AREA | O | O | 동일 | WH_AREA (BCC2.REF_CODE2) |
| 27 | USE_NAME | O | O | 동일 | EO.USE_NAME |
| 28 | USE_CODE | O | O | 동일 | EO.USE_CODE |
| 29 | CT_NAME | O | O | 동일 | B_COMMON_CODE HOMEPLUS_ORIGIN_CODE + '산' |
| 30 | STORE_CODE | O | O | 동일 | EO.STORECODE |
| 31 | EMART_PLANT_CODE | O | X | - | 이마트에만 존재 |
| 32 | MAJOR_CATEGORY | O | X | - | 이마트에만 존재 |
| 33 | CONTAINER_TYPE | O | X | - | 이마트에만 존재 |

### A.2 요약

| 구분 | 수량 |
|------|:----:|
| 양쪽 동일 컬럼 | 25개 |
| 컬럼명 동일 + 데이터 출처 다름 | **5개** |
| 이마트에만 존재 | 3개 (EMART_PLANT_CODE, MAJOR_CATEGORY, CONTAINER_TYPE) |
| 비정량에만 존재 | 0개 |

---

## 6-B. 컬럼명은 동일하지만 데이터 출처가 다른 컬럼 (핵심 5개)

### B.1 PACKER_CODE

| 항목 | 이마트 VIEW | 비정량 VIEW |
|------|-----------|-----------|
| 해외매입 | `BD.PACKER_CODE` (I_BL_D 테이블) | - |
| 국내매입 | `OD.PACKER_CODE` (I_OFFER_D 테이블) | - |
| 비정량 | - | **`'0000'` 고정 하드코딩** |
| 이유 | 실제 패커 정보 존재 | 비정량은 하이랜드 자체 원료육이므로 패커 없음 |
| MSSQL 전환 | `I.패커코드` | **`'0000'` 고정값 유지** |

### B.2 PACKER_PRODUCT_CODE

| 항목 | 이마트 VIEW | 비정량 VIEW |
|------|-----------|-----------|
| 해외매입 | `BD.PACKER_PRODUCT_CODE` (I_BL_D) | - |
| 국내매입 | `DECODE(OD.PACKER_PRODUCT_CODE, NULL, ID.ITEM_CODE, OD.PACKER_PRODUCT_CODE)` | - |
| 비정량 | - | **`ID.ITEM_CODE`** (상품코드를 그대로 사용) |
| 이유 | 패커별 고유 상품코드 존재 | 패커 없으므로 자사 품목코드를 대체 사용 |
| MSSQL 전환 | `I.PPCODE` | **`I.품목코드`** (ITEM_CODE 그대로) |

### B.3 BARCODE_TYPE

| 항목 | 이마트 VIEW | 비정량 VIEW |
|------|-----------|-----------|
| 로직 | `DECODE(CENTER_SCALE_USE_YN, 'Y', DECODE(BI.ITEM_TYPE, '10', DECODE(BI.MAJOR_CATEGORY, '10', 'M9', EO.BARCODE_TYPE), EO.BARCODE_TYPE), EO.BARCODE_TYPE)` | **`EO.BARCODE_TYPE`** (직접 사용) |
| 설명 | 저울바코드 사용센터(Y) + 축산(10) + 소(10) = M9 변환, 그 외 EO.BARCODE_TYPE | CENTER_SCALE_USE_YN 분기 없이 바로 EO.BARCODE_TYPE |
| 이유 | 원료육(W)은 M9 변환이 필요한 케이스 존재 | 비정량은 M8 타입만 해당 (EB.BARCODE_TYPE = 'M8' 조건) |
| MSSQL 전환 | `COALESCE(M1.바코드타입, M2.바코드타입)` + CENTER_SCALE 분기 | **`COALESCE(M1.바코드타입, M2.바코드타입)`** (M9 분기 불필요) |

### B.4 ITEM_TYPE

| 항목 | 이마트 VIEW | 비정량 VIEW |
|------|-----------|-----------|
| 값 | `EO.ITEM_TYPE` (W/J/B/S 등 실제 타입) | **`'HW'` 고정 하드코딩** |
| 설명 | B_EMART_BARCODE.ITEM_TYPE (EB.ITEM_TYPE = 'W') | 하이랜드 원료육 전용 구분자 |
| 이유 | 원료육(W) 타입으로 필터링됨 | PDA에서 FLOOR 처리 안 하기 위한 별도 구분자 |
| MSSQL 전환 | `COALESCE(M1.타입구분, M2.타입구분)` | **`'HW'` 고정값 유지** |

### B.5 BARCODEGOODS

| 항목 | 이마트 VIEW | 비정량 VIEW |
|------|-----------|-----------|
| 해외매입 | `(SELECT BARCODEGOODS FROM S_BARCODE_INFO WHERE PACKER_CLIENT_CODE = BD.PACKER_CODE AND PACKER_PRODUCT_CODE = BD.PACKER_PRODUCT_CODE)` | - |
| 국내매입 | `DECODE(OD.PACKER_PRODUCT_CODE, NULL, ID.ITEM_CODE, (SELECT...S_BARCODE_INFO...))` | - |
| 비정량 | - | **`ID.ITEM_CODE`** (상품코드를 그대로 사용) |
| 이유 | 패커 바코드 정보 테이블에서 조회 | 패커 없으므로 자사 품목코드를 대체 사용 |
| MSSQL 전환 | `I.상품바코드` | **`I.품목코드`** (또는 `I.상품바코드`) |

---

## 6-C. 비정량에만 있는 WHERE 조건 차이

### C.1 WHERE 조건 비교

| 조건 | 이마트 VIEW | 비정량 VIEW | 비고 |
|------|:----------:|:---------:|------|
| `1 = 1` | O | O | 동일 |
| `ID.PACKING_QTY = 0` | O | O | 동일 (미계근 건만) |
| `ID.GI_REQ_PKG <> 0` | O | O | 동일 (요청수량 있는 건만) |
| `EO.EOI_ID IS NOT NULL` | O | O | 동일 |
| `WR.CONTRACT_TYPE` | **`<> '40'` (해외) / `= '40'` (국내)** | **`= '40'`** | **핵심 차이** |
| `IH.GI_REQ_DATE >= SYSDATE` | O | O | 동일 |

### C.2 EO 서브쿼리 내부 조건 차이

| 조건 | 이마트 VIEW | 비정량 VIEW | 비고 |
|------|-----------|-----------|------|
| `EB.ITEM_TYPE` | **`= 'W'`** (원료육) | **`IN ('J', 'B')`** (제품, 비정량) | **핵심 차이** |
| `BCC.MASTER_CODE` | `'EMART_STORE_CODE'` | **`'EMART_BRANCH_CODE'`** | **다름** |
| `BCC2.MASTER_CODE` | `'EMART_STORE_CODE'` | **`'EMART_BRANCH_CODE'`** | **다름** |
| `EB.BARCODE_TYPE` | 조건 없음 | **`= 'M8'`** | 비정량에만 존재 |

### C.3 MSSQL 전환 시 WHERE 조건 요약

이마트 JSP(`search_shipment.jsp`)의 기존 WHERE:
```sql
WHERE H.마트사구분 = '7'
  AND D.출고수량 > 0
  AND COALESCE(M1.타입구분, M2.타입구분) = 'W'
```

비정량 JSP 전환 시 WHERE 변경 필요:
```sql
WHERE H.마트사구분 = '7'
  AND D.출고수량 > 0
  AND COALESCE(M1.타입구분, M2.타입구분) IN ('J', 'B')  -- W → J, B
  AND COALESCE(M1.바코드타입, M2.바코드타입) = 'M8'       -- 비정량 M8 조건 추가
```

> **주의**: 이마트 JSP에는 `PACKING_QTY = 0`, `GI_REQ_PKG <> 0` 조건이 없다.
> 이는 MSSQL 테이블 구조가 다르거나, LOT 기반 조회로 변경되었기 때문일 수 있다.
> 비정량 전환 시 이 조건의 필요 여부를 확인해야 한다.

---

## 6-D. 비정량에만 있는 JOIN 차이

### D.1 메인 JOIN 테이블 비교

| JOIN 테이블 | 이마트 VIEW | 비정량 VIEW | 비고 |
|------------|:----------:|:---------:|------|
| W_GOODS_IH (IH) | O | O | 동일 |
| W_GOODS_ID (ID) | O | O | 동일 |
| W_GOODS_R (WR) | O | O | 동일 |
| **I_BL_D (BD)** | **O** (해외매입) | **X** | 이마트에만 존재 |
| **I_OFFER_D (OD)** | **O** (국내매입) | **X** | 이마트에만 존재 |
| B_SUPPLIER_ITEM (BSI) | O | X | 이마트에만 (EMART_PLANT_CODE용) |
| B_ITEM (BI) | O | O | 동일 |
| EO 서브쿼리 | O | O | 내부 조건 다름 (섹션 C.2 참조) |

### D.2 핵심 차이 — UNION ALL vs 단일 SELECT

| 항목 | 이마트 VIEW | 비정량 VIEW |
|------|-----------|-----------|
| 구조 | **UNION ALL** (해외매입 + 국내매입) | **단일 SELECT** |
| 해외매입 | `INNER JOIN I_BL_D BD` + `WR.CONTRACT_TYPE <> '40'` | 없음 |
| 국내매입 | `INNER JOIN I_OFFER_D OD` + `WR.CONTRACT_TYPE = '40'` | `WR.CONTRACT_TYPE = '40'` (국내만) |
| 이유 | 이마트는 해외/국내 매입 모두 발생 | 비정량은 국내매입(CONTRACT_TYPE='40')만 발생 |

### D.3 MSSQL 전환 시 이마트 JSP의 JOIN 구조

이마트 MSSQL JSP는 UNION ALL 대신 **단일 SELECT + 다중 LEFT JOIN**으로 전환되었다:

| Oracle VIEW 테이블 | MSSQL JSP 대응 테이블 |
|-------------------|---------------------|
| I_BL_D (BD) / I_OFFER_D (OD) | 없음 (패커 정보를 CO_품목코드에서 직접 조회) |
| B_SUPPLIER_ITEM (BSI) | 없음 |
| W_EMART_ORDER_ITEM (EOI) | SM_마트사발주이마트 (ME) |
| B_EMART_BARCODE (EB) | CO_매출처품목코드매핑 (M1, M2) |
| B_COMMON_CODE (BCC) | CO_거래처MASTER (B) |
| S_BARCODE_INFO | 없음 (CO_품목코드.상품바코드로 대체) |
| B_ITEM (BI) | CO_품목코드 (I) |

비정량 전환 시에도 이 구조를 기반으로 하되, UNION ALL이 원래 없으므로 더 단순하다.

---

## 6-E. MSSQL 전환 시 주의사항 — Oracle 함수 전환

### E.1 Oracle 함수 → MSSQL 전환 매핑

| Oracle 함수 | 사용 위치 | MSSQL 전환 방법 | 이마트 JSP 전환 예시 |
|-------------|---------|---------------|-------------------|
| `DECODE(A, B, C, D)` | ITEM_NAME, BL_NO, CLIENTNAME, BARCODE_TYPE, EMARTLOGIS_CODE, EMART_PLANT_CODE | `CASE WHEN A = B THEN C ELSE D END` 또는 `ISNULL()`, `COALESCE()` | `ISNULL(V.BLNO, V.이력번호) AS BL_NO` |
| `DE_ITEM(ITEM_CODE)` | ITEM_NAME | MSSQL에서 CO_품목코드 JOIN으로 대체 | `I.품목명 AS ITEM_NAME` |
| `DE_CLIENT(CLIENT_CODE)` | CLIENTNAME | MSSQL에서 CO_거래처MASTER JOIN으로 대체 | `ME.점포명 AS CLIENTNAME` |
| `DE_CLIENT2(CLIENT_CODE)` | CLIENTNAME (CJ센터) | MSSQL에서 CO_거래처MASTER JOIN으로 대체 | 동일 |
| `DE_COMMON('BRAND', CODE)` | BRANDNAME | 사용하지 않음 (BRAND_CODE = '' 빈값) | `'' AS BRAND_CODE` |
| `TO_CHAR(SYSDATE, 'YYYYMMDD')` | WHERE 조건 | `CONVERT(VARCHAR(8), GETDATE(), 112)` 또는 `FORMAT(GETDATE(), 'yyyyMMdd')` | JSP에서 Java 날짜 파라미터 전달 |
| `SUBSTR(A, 1, 2)` | CLIENTNAME CJ 분기 | `LEFT(A, 2)` 또는 `SUBSTRING(A, 1, 2)` | - |
| `ROWNUM < 2` | BARCODEGOODS 서브쿼리 | `TOP 1` | 불필요 (CO_품목코드.상품바코드로 대체) |
| `\|\|` (문자열 연결) | CT_NAME, CLIENTNAME | `+` 또는 `CONCAT()` | `C1.명칭 AS CT_NAME` (JOIN으로 대체) |

### E.2 비정량 전용 전환 주의사항

| 항목 | 상세 |
|------|------|
| PACKER_CODE | `'0000'` 고정값 유지 — Oracle/MSSQL 동일 |
| PACKER_PRODUCT_CODE | `ID.ITEM_CODE` → `D.출고품목코드` 또는 `I.품목코드` |
| ITEM_TYPE | `'HW'` 고정값 유지 — Oracle/MSSQL 동일 |
| BARCODEGOODS | `ID.ITEM_CODE` → `I.품목코드` 또는 `I.상품바코드` (확인 필요) |
| BARCODE_TYPE | CENTER_SCALE_USE_YN 분기 불필요, M8 조건만 |
| BCC MASTER_CODE | `EMART_STORE_CODE` → `EMART_BRANCH_CODE` (비정량은 BRANCH 코드 사용) |
| EB.ITEM_TYPE 조건 | `'W'` → `IN ('J', 'B')` |

---

## 7. 원본 비교 — 이마트 MSSQL JSP vs 비정량 JSP 컬럼 구조

### 7.1 이마트 MSSQL JSP 출력 컬럼 (31개, Index 0~30)

```
0:GI_D_ID, 1:ITEM_CODE, 2:ITEM_NAME, 3:EMARTITEM_CODE, 4:EMARTITEM,
5:GI_REQ_PKG, 6:GI_REQ_QTY, 7:GI_REQ_DATE, 8:BL_NO, 9:BRAND_CODE,
10:CLIENT_CODE, 11:CLIENTNAME, 12:CENTERNAME, 13:ITEM_SPEC, 14:CT_CODE,
15:IMPORT_ID_NO, 16:PACKER_CODE, 17:PACKER_PRODUCT_CODE, 18:BARCODE_TYPE,
19:ITEM_TYPE, 20:PACKWEIGHT, 21:BARCODEGOODS, 22:STORE_IN_DATE,
23:EMARTLOGIS_CODE, 24:WH_AREA, 25:USE_NAME, 26:USE_CODE, 27:CT_NAME,
28:STORE_CODE, 29:EMART_PLANT_CODE, 30:GI_L_ID
```

### 7.2 비정량 JSP 출력 컬럼 (37개, Index 0~36)

```
0:GI_H_ID, 1:GI_D_ID, 2:EOI_ID, 3:ITEM_CODE, 4:ITEM_NAME,
5:EMARTITEM_CODE, 6:EMARTITEM, 7:GI_REQ_PKG, 8:GI_REQ_QTY, 9:AMOUNT,
10:GOODS_R_ID, 11:GR_REF_NO, 12:GI_REQ_DATE, 13:BL_NO, 14:BRAND_CODE,
15:BRANDNAME, 16:CLIENT_CODE, 17:CLIENTNAME, 18:CENTERNAME, 19:ITEM_SPEC,
20:CT_CODE, 21:IMPORT_ID_NO, 22:PACKER_CODE, 23:PACKERNAME,
24:PACKER_PRODUCT_CODE, 25:BARCODE_TYPE, 26:ITEM_TYPE, 27:PACKWEIGHT,
28:BARCODEGOODS, 29:STORE_IN_DATE, 30:EMARTLOGIS_CODE, 31:EMARTLOGIS_NAME,
32:WH_AREA, 33:USE_NAME, 34:USE_CODE, 35:CT_NAME, 36:STORE_CODE
```

### 7.3 비정량 JSP에는 있지만 이마트 MSSQL JSP에는 없는 컬럼 (8개)

| 컬럼명 | 비정량 Index | 이마트 JSP | 용도 | 전환 시 처리 |
|--------|:----------:|:--------:|------|-----------|
| GI_H_ID | 0 | 없음 | 미사용(로컬DB저장) | 유지 또는 제거 검토 |
| EOI_ID | 2 | 없음 | 미사용(로컬DB저장) | 유지 또는 제거 검토 |
| AMOUNT | 9 | 없음 | 미사용 | 유지 또는 제거 검토 |
| GOODS_R_ID | 10 | 없음 | 미사용 | 유지 또는 제거 검토 |
| GR_REF_NO | 11 | 없음 | 미사용 | 유지 또는 제거 검토 |
| BRANDNAME | 15 | 없음 | 미사용 | 유지 또는 제거 검토 |
| PACKERNAME | 23 | 없음 | 미사용 | 유지 또는 제거 검토 |
| EMARTLOGIS_NAME | 31 | 없음 | 미사용 | 유지 또는 제거 검토 |

### 7.4 이마트 MSSQL JSP에는 있지만 비정량 JSP에는 없는 컬럼 (2개)

| 컬럼명 | 이마트 Index | 용도 | 전환 시 처리 |
|--------|:----------:|------|-----------|
| EMART_PLANT_CODE | 29 | 바코드생성, 로직분기 | 비정량에서 불필요 (이마트 VIEW에만 존재) |
| GI_L_ID | 30 | LOT 식별 | MSSQL 신규 추가 컬럼 |

---

## 8. 주의사항

1. **비정량은 UNION ALL이 없다**: 이마트 VIEW는 해외매입(I_BL_D) + 국내매입(I_OFFER_D) UNION ALL이지만, 비정량은 국내매입(CONTRACT_TYPE='40')만 단일 SELECT이므로 MSSQL 전환이 더 단순하다.

2. **5개 핵심 컬럼의 데이터 출처를 반드시 유지해야 한다**:
   - PACKER_CODE = `'0000'` (고정)
   - PACKER_PRODUCT_CODE = `I.품목코드` (ITEM_CODE 그대로)
   - BARCODE_TYPE = M9 분기 불필요, `COALESCE(M1.바코드타입, M2.바코드타입)` 직접 사용
   - ITEM_TYPE = `'HW'` (고정)
   - BARCODEGOODS = `I.품목코드` (ITEM_CODE 그대로)

3. **EO 서브쿼리 MASTER_CODE가 다르다**: 이마트는 `EMART_STORE_CODE`, 비정량은 `EMART_BRANCH_CODE`를 사용한다. MSSQL의 CO_거래처MASTER JOIN 시 마트사구분 코드가 다를 수 있다.

4. **EB.ITEM_TYPE 필터가 다르다**: 이마트는 `= 'W'`, 비정량은 `IN ('J', 'B')`. MSSQL의 CO_매출처품목코드매핑 JOIN 조건에 반영 필요.

5. **EB.BARCODE_TYPE = 'M8' 추가 조건**: 비정량 VIEW에만 BCC2 JOIN에 `AND EB.BARCODE_TYPE = 'M8'` 조건이 있다. MSSQL 전환 시 반드시 반영해야 한다.

6. **비정량 JSP는 37개 컬럼이지만 8개는 미사용**: GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME은 로컬DB 저장만 되고 비즈니스 로직에서 사용되지 않는다. 이마트 JSP처럼 31개로 줄일지, 기존 37개를 유지할지 결정 필요.

7. **Java 파싱 클래스 변경 주의**: 비정량 JSP의 컬럼 순서/개수가 변경되면 `ProgressDlgShipSearch.java`의 temp[] 파싱 인덱스도 함께 변경해야 한다.

---

## 9. 관련 문서

- `app/doc/view/VW_PDA_WID_LIST분석폴더/VW_PDA_WID_LIST.md` — 이마트 VIEW 상세 분석 (41개 컬럼)
- `app/doc/view/VW_PDA_WID_LIST` — 이마트 Oracle VIEW SQL 정의
- `app/doc/view/VW_PDA_WID_LIST_NONFIXED` — 비정량 Oracle VIEW SQL 정의
- `app/doc/소스분석/40_비정량_출하계근대상받기_JSP_Java파싱_인덱스분석.md` — 비정량 JSP-Java 파싱 인덱스 분석

---

## F. 결론

비정량 JSP(`search_production_nonfixed.jsp`)를 이마트 MSSQL JSP(`search_shipment.jsp`)와 동일한 직접 JOIN 구조로 전환하되, 아래 5개 컬럼은 비정량 VIEW의 데이터 출처를 반드시 유지해야 한다:

| 컬럼명 | 이마트 MSSQL JSP 값 | 비정량 전환 시 값 |
|--------|-------------------|----------------|
| PACKER_CODE | `I.패커코드` | **`'0000'`** (고정값) |
| PACKER_PRODUCT_CODE | `I.PPCODE` | **`I.품목코드`** (= ITEM_CODE) |
| BARCODE_TYPE | `COALESCE(M1.바코드타입, M2.바코드타입)` + M9 분기 | **`COALESCE(M1.바코드타입, M2.바코드타입)`** (M9 분기 불필요) |
| ITEM_TYPE | `COALESCE(M1.타입구분, M2.타입구분)` | **`'HW'`** (고정값) |
| BARCODEGOODS | `I.상품바코드` | **`I.품목코드`** (= ITEM_CODE) |

추가로 WHERE 조건도 변경 필요:
- `COALESCE(M1.타입구분, M2.타입구분) = 'W'` → `IN ('J', 'B')`
- `COALESCE(M1.바코드타입, M2.바코드타입) = 'M8'` 조건 추가
- CO_거래처MASTER JOIN의 마트사구분/MASTER_CODE 차이 확인 필요
