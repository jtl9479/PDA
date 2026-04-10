# 이마트 Oracle VIEW -> MSSQL JSP -> 비정량 Oracle VIEW 3자 비교 분석

## 개요

이마트 Oracle VIEW(A)가 MSSQL JSP(B)로 어떻게 전환되었는지 매핑을 도출하고,
그 매핑을 비정량 Oracle VIEW(C) -> 비정량 MSSQL JSP(D) 전환에 적용할 때 변경점을 분석한다.

- **A (이마트 Oracle VIEW)**: `app/doc/view/VW_PDA_WID_LIST`
- **B (이마트 MSSQL JSP)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment.jsp`
- **C (비정량 Oracle VIEW)**: `app/doc/view/VW_PDA_WID_LIST_NONFIXED`
- **타입**: VIEW/JSP 3자 비교 분석
- **작성일**: 2026-04-07

---

## 1. 테이블 매핑

### 1.1 A(이마트 Oracle) -> B(이마트 MSSQL) 테이블 매핑

| Oracle 테이블 | MSSQL 테이블 | 별칭 | 비고 |
|--------------|-------------|------|------|
| W_GOODS_IH | SM_출고머리 | H | 출고 헤더 |
| W_GOODS_ID | SM_출고상세 | D | 출고 상세 (B에서 메인 FROM) |
| W_GOODS_R | 월품목별재고화일_LOT별_VIEW | V | 입고/재고 정보 (LEFT JOIN으로 변경) |
| I_BL_D (해외매입) | - | - | B에서 제거됨 (UNION ALL 해외매입 파트) |
| I_OFFER_D (국내매입) | - | - | B에서 제거됨 (UNION ALL 국내매입 파트) |
| B_SUPPLIER_ITEM | - | - | B에서 제거됨 (EMART_PLANT_CODE 하드코딩 '') |
| B_ITEM | CO_품목코드 | I | 품목 마스터 |
| W_EMART_ORDER_ITEM | SM_마트사발주이마트 | ME | 이마트 발주 |
| B_EMART_BARCODE | CO_매출처품목코드매핑 | M1, M2 | 바코드/타입 정보 (2단계 조회) |
| B_COMMON_CODE (EMART_STORE_CODE) | CO_거래처MASTER | B | 센터/점포 정보 |
| B_COMMON_CODE (EMART_RAWMEAT_USE_TYPE) | CO_각종소분류코드 (대분류='043') | C | 제품용도명 |
| B_COMMON_CODE (HOMEPLUS_ORIGIN_CODE) | CO_각종소분류코드 (대분류='Q14') | C1 | 원산지명 |
| S_BARCODE_INFO (서브쿼리) | CO_품목코드.상품바코드 | I | B에서 직접 컬럼으로 대체 |
| - (없음) | SM_수주머리 | SH | B에서 추가 (수주 연결) |
| - (없음) | SM_수주상세 | SD | B에서 추가 (납기일자 조회) |
| - (없음) | SM_출고LOT | L | B에서 추가 (LOT별 박스수량/중량) |
| - (없음) | CO_거래처MASTER G, G2 | G, G2 | B에서 추가 (계층코드 기반 2단계 매핑) |

### 1.2 C(비정량 Oracle)에서 사용하는 테이블의 매핑 해당 여부

| C의 Oracle 테이블 | A에도 있는지 | B의 MSSQL 매핑 | 비고 |
|------------------|:----------:|---------------|------|
| W_GOODS_IH | O | SM_출고머리 (H) | 동일 매핑 |
| W_GOODS_ID | O | SM_출고상세 (D) | 동일 매핑 |
| W_GOODS_R | O | 월품목별재고화일_LOT별_VIEW (V) | 동일 매핑 |
| B_ITEM | O | CO_품목코드 (I) | 동일 매핑 |
| W_EMART_ORDER_ITEM | O | SM_마트사발주이마트 (ME) | 동일 매핑 |
| B_EMART_BARCODE | O | CO_매출처품목코드매핑 (M1, M2) | 동일 매핑 |
| B_COMMON_CODE (EMART_BRANCH_CODE) | 유사 (EMART_STORE_CODE) | CO_거래처MASTER (B) | MASTER_CODE 다름 |
| B_COMMON_CODE (EMART_SCALE_BARCODE_USE_CENTER) | O | - | B에서 제거됨 |
| B_COMMON_CODE (EMART_RAWMEAT_USE_TYPE) | O | CO_각종소분류코드 (C) | 동일 매핑 |
| B_COMMON_CODE (HOMEPLUS_ORIGIN_CODE) | O | CO_각종소분류코드 (C1) | 동일 매핑 |

**C에만 있는 테이블**: 없음 (A의 부분집합)
**A에는 있지만 C에 없는 테이블**: I_BL_D, I_OFFER_D, B_SUPPLIER_ITEM (C는 UNION ALL 없이 국내매입만)

---

## 2. JOIN 매핑

### 2.1 A(이마트 Oracle) -> B(이마트 MSSQL) JOIN 구조 변환

| # | A (Oracle) JOIN | B (MSSQL) JOIN | 변환 내용 |
|---|----------------|----------------|-----------|
| 1 | W_GOODS_IH -> W_GOODS_ID (GI_H_ID) | SM_출고상세 D -> SM_출고머리 H (회사코드+출고사업장+출고일자+출고일련번호) | 복합키 JOIN으로 변경 |
| 2 | W_GOODS_ID -> W_GOODS_R (GOODS_R_ID) | SM_출고상세 D -> 월품목별재고화일_LOT별_VIEW V (회사코드+사업장+창고코드+품목코드+LOTNO) | LEFT JOIN, 조건 변경 |
| 3 | W_GOODS_R -> I_BL_D (BL_D_ID, BL_S_ID) | **제거** | UNION ALL 해외매입 파트 제거 |
| 4 | W_GOODS_R -> I_OFFER_D (OFFER_D_ID) | **제거** | UNION ALL 국내매입 파트 제거 |
| 5 | B_ITEM ON ID.ITEM_CODE | CO_품목코드 I ON D.출고품목코드 = I.품목코드 | 컬럼명 변경 |
| 6 | B_SUPPLIER_ITEM LEFT JOIN (PACKER_CODE, PACKER_PRODUCT_CODE) | **제거** | EMART_PLANT_CODE를 '' 하드코딩 |
| 7 | W_EMART_ORDER_ITEM 서브쿼리 ON EOI_ID | SM_마트사발주이마트 ME ON SD.마트사SEQ = ME.SEQ | 수주상세 경유 JOIN |
| 8 | B_EMART_BARCODE ON EMARTITEM_CODE, ITEM_TYPE='W' | CO_매출처품목코드매핑 M1 ON 품목코드+거래처코드 | 매핑 방식 변경, 2단계 fallback (M2) |
| 9 | B_COMMON_CODE (EMART_STORE_CODE) -> CENTERNAME | CO_거래처MASTER B ON 마트사거래처코드 | 테이블 변경 |
| 10 | B_COMMON_CODE (EMART_STORE_CODE) -> WH_AREA (REF_CODE2) | CO_거래처MASTER B.창고구역 | 컬럼 직접 조회 |
| 11 | B_COMMON_CODE 서브쿼리 -> USE_NAME | CO_각종소분류코드 C (대분류='043') LEFT JOIN | 테이블 변경 |
| 12 | B_COMMON_CODE 서브쿼리 -> CT_NAME | CO_각종소분류코드 C1 (대분류='Q14') LEFT JOIN | 테이블 변경 |
| 13 | S_BARCODE_INFO 서브쿼리 -> BARCODEGOODS | CO_품목코드.상품바코드 | 서브쿼리 제거, 직접 컬럼 |
| 14 | - | SM_수주머리 SH + SM_수주상세 SD JOIN | **B에서 추가** (수주 연결) |
| 15 | - | SM_출고LOT L JOIN | **B에서 추가** (LOT별 데이터) |
| 16 | - | CO_거래처MASTER G, G2 + CO_매출처품목코드매핑 M2 | **B에서 추가** (2단계 fallback 매핑) |
| 17 | B_COMMON_CODE (EMART_SCALE_BARCODE_USE_CENTER) -> CENTER_SCALE_USE_YN | **제거** | B에서 미사용 |

### 2.2 C(비정량 Oracle) JOIN과 B(이마트 MSSQL)의 차이점

| 항목 | C (비정량 Oracle) | B (이마트 MSSQL) | D 전환 시 |
|------|-----------------|-----------------|----------|
| UNION ALL | 없음 (단일 SELECT) | 없음 | 동일 - 단일 SELECT |
| I_BL_D / I_OFFER_D | 없음 | 없음 | 동일 - 사용하지 않음 |
| B_SUPPLIER_ITEM | 없음 | 없음 | 동일 |
| CONTRACT_TYPE 조건 | = '40' (국내매입만) | 없음 (마트사구분='7'로 대체) | D에서도 마트사구분으로 필터 |
| B_EMART_BARCODE ITEM_TYPE | IN ('J', 'B') | COALESCE(M1.타입구분, M2.타입구분) = 'W' | D에서 타입구분 IN ('J', 'B') 필요 |
| MASTER_CODE | 'EMART_BRANCH_CODE' | 'EMART_STORE_CODE'에 해당하는 CO_거래처MASTER | D에서 동일하게 CO_거래처MASTER 사용 |
| EB.BARCODE_TYPE = 'M8' 조건 | BCC2 JOIN에 추가 조건 있음 | 없음 | **D에서 추가 필요** (M8 바코드 필터) |
| CENTER_SCALE_USE_YN | 있음 (SELECT에서 사용) | 없음 | D에서도 불필요 (B에서 제거됨) |

---

## 3. 컬럼별 SELECT 매핑

B의 31개 컬럼 각각에 대해 A/C 출처와 D 전환 방법을 정리한다.

| # | 별칭 | A (이마트 Oracle) | B (이마트 MSSQL) | C (비정량 Oracle) | D (비정량 MSSQL) 전환 |
|---|------|------------------|-----------------|-----------------|---------------------|
| 0 | GI_D_ID | ID.GI_D_ID | D.SEQ | ID.GI_D_ID | D.SEQ (동일) |
| 1 | ITEM_CODE | ID.ITEM_CODE | I.품목코드 | ID.ITEM_CODE | I.품목코드 (동일) |
| 2 | ITEM_NAME | DECODE(EO.ITEM_NAME, NULL, DE_ITEM(ID.ITEM_CODE), EO.ITEM_NAME) | I.품목명 | DECODE(EO.ITEM_NAME, NULL, DE_ITEM(ID.ITEM_CODE), EO.ITEM_NAME) | I.품목명 (동일) |
| 3 | EMARTITEM_CODE | EO.ITEM_CODE | ME.상품코드 | EO.ITEM_CODE | ME.상품코드 (동일) |
| 4 | EMARTITEM | EO.ITEM_NAME | ME.상품명 | EO.ITEM_NAME | ME.상품명 (동일) |
| 5 | GI_REQ_PKG | ID.GI_REQ_PKG | L.박스수량 | ID.GI_REQ_PKG | L.박스수량 (동일) |
| 6 | GI_REQ_QTY | ID.GI_REQ_QTY | L.중량 | ID.GI_REQ_QTY | L.중량 (동일) |
| 7 | GI_REQ_DATE | GI_REQ_DATE | D.출고일자 | GI_REQ_DATE | D.출고일자 (동일) |
| 8 | BL_NO | DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) | ISNULL(V.BLNO, V.이력번호) | DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) | ISNULL(V.BLNO, V.이력번호) (동일) |
| 9 | BRAND_CODE | ID.BRAND_CODE | '' (빈문자열) | ID.BRAND_CODE | '' (동일) |
| 10 | CLIENT_CODE | IH.CLIENT_CODE | ME.점포코드 | IH.CLIENT_CODE | ME.점포코드 (동일) |
| 11 | CLIENTNAME | DECODE(SUBSTR(EO.CENTERNAME,1,2), 'CJ', DE_CLIENT2(IH.CLIENT_CODE)\|\|'('\|\|EO.STORECODE\|\|')', DE_CLIENT(IH.CLIENT_CODE)) | ME.점포명 | 동일 DECODE 로직 | ME.점포명 (동일) |
| 12 | CENTERNAME | EO.CENTERNAME (B_COMMON_CODE) | B.상호 (CO_거래처MASTER) | EO.CENTERNAME (B_COMMON_CODE) | B.상호 (동일) |
| 13 | ITEM_SPEC | WR.ITEM_SPEC | I.규격 | WR.ITEM_SPEC | I.규격 (동일) |
| 14 | CT_CODE | WR.CT_CODE | I.원산지 | WR.CT_CODE | I.원산지 (동일) |
| 15 | IMPORT_ID_NO | WR.IMPORT_ID_NO | V.이력번호 | WR.IMPORT_ID_NO | V.이력번호 (동일) |
| 16 | PACKER_CODE | BD.PACKER_CODE / OD.PACKER_CODE | I.패커코드 | **'0000' (하드코딩)** | **'0000' 하드코딩 유지 또는 I.패커코드** |
| 17 | PACKER_PRODUCT_CODE | BD.PACKER_PRODUCT_CODE / OD.PACKER_PRODUCT_CODE | I.PPCODE | **ID.ITEM_CODE (하드코딩)** | **I.PPCODE 또는 I.품목코드** |
| 18 | BARCODE_TYPE | 복잡한 DECODE (CENTER_SCALE_USE_YN 분기) | COALESCE(M1.바코드타입, M2.바코드타입) | EO.BARCODE_TYPE (단순) | COALESCE(M1.바코드타입, M2.바코드타입) (동일) |
| 19 | ITEM_TYPE | EO.ITEM_TYPE ('W') | COALESCE(M1.타입구분, M2.타입구분) | **'HW' (하드코딩)** | **'HW' 하드코딩 유지 필요** |
| 20 | PACKWEIGHT | EO.PACKWEIGHT | COALESCE(NULLIF(V.평균중량,0), I.박스중량) | EO.PACKWEIGHT | COALESCE(NULLIF(V.평균중량,0), I.박스중량) (동일) |
| 21 | BARCODEGOODS | S_BARCODE_INFO 서브쿼리 | I.상품바코드 | **ID.ITEM_CODE (하드코딩)** | **I.품목코드 또는 I.상품바코드** |
| 22 | STORE_IN_DATE | STORE_IN_DATE (EOI) | SD.납기일자 | STORE_IN_DATE (EOI) | SD.납기일자 (동일) |
| 23 | EMARTLOGIS_CODE | DECODE(EO.EMARTLOGIS_CODE, NULL, '0000000', EO.EMARTLOGIS_CODE) | COALESCE(M1.물류코드, M2.물류코드) | DECODE(EO.EMARTLOGIS_CODE, NULL, '0000000', EO.EMARTLOGIS_CODE) | COALESCE(M1.물류코드, M2.물류코드) (동일) |
| 24 | WH_AREA | BCC2.REF_CODE2 (B_COMMON_CODE) | B.창고구역 (CO_거래처MASTER) | BCC2.REF_CODE2 (B_COMMON_CODE) | B.창고구역 (동일) |
| 25 | USE_NAME | B_COMMON_CODE 서브쿼리 (EMART_RAWMEAT_USE_TYPE) | C.명칭 (CO_각종소분류코드, 대분류='043') | 동일 서브쿼리 | C.명칭 (동일) |
| 26 | USE_CODE | EB.USE_CODE | I.제품용도 | EB.USE_CODE | I.제품용도 (동일) |
| 27 | CT_NAME | B_COMMON_CODE 서브쿼리 (HOMEPLUS_ORIGIN_CODE) + '산' | C1.명칭 (CO_각종소분류코드, 대분류='Q14') | 동일 서브쿼리 | C1.명칭 (동일) |
| 28 | STORE_CODE | EO.STORECODE | ME.점포코드 | EO.STORECODE | ME.점포코드 (동일) |
| 29 | EMART_PLANT_CODE | DECODE(EO.STORECODE, '9820', BSI.EMART_PLANT_CODE, NULL) | '' (빈문자열) | **C에 없음** | '' (동일, B와 같이 하드코딩) |
| 30 | GI_L_ID | **A에 없음** | L.SEQ (SM_출고LOT) | **C에 없음** | L.SEQ (동일, B에서 추가된 컬럼) |

### 3.1 비정량(C)에서 핵심 차이가 나는 컬럼 (5개)

| # | 컬럼 | C의 특수 로직 | D 전환 시 처리 방법 |
|---|------|-------------|-------------------|
| 16 | PACKER_CODE | '0000' 하드코딩 | SELECT에서 '0000' AS PACKER_CODE 하드코딩 유지 |
| 17 | PACKER_PRODUCT_CODE | ID.ITEM_CODE (품목코드를 패커상품코드로 사용) | I.품목코드 AS PACKER_PRODUCT_CODE |
| 19 | ITEM_TYPE | 'HW' 하드코딩 (PDA에서 FLOOR 미처리 구분) | SELECT에서 'HW' AS ITEM_TYPE 하드코딩 유지 |
| 21 | BARCODEGOODS | ID.ITEM_CODE (S_BARCODE_INFO 서브쿼리 대신) | I.품목코드 AS BARCODEGOODS |
| 18 | BARCODE_TYPE | EO.BARCODE_TYPE (단순, CENTER_SCALE_USE_YN 분기 없음) | COALESCE(M1.바코드타입, M2.바코드타입) (B와 동일 가능) |

---

## 4. WHERE 조건 매핑

### 4.1 A(이마트 Oracle) -> B(이마트 MSSQL) WHERE 전환

| A (Oracle) WHERE | B (MSSQL) WHERE | 변환 내용 |
|-----------------|-----------------|-----------|
| ID.PACKING_QTY = 0 | D.출고수량 > 0 | **조건 변경** (PACKING_QTY=0 -> 출고수량>0) |
| ID.GI_REQ_PKG <> 0 | - | **제거** (B에 없음) |
| WR.CONTRACT_TYPE <> '40' (해외) / = '40' (국내) | - | **제거** (UNION ALL 제거로 불필요) |
| EO.EOI_ID IS NOT NULL | - | **제거** (INNER JOIN으로 보장) |
| IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD') | + qry_where (파라미터) | **파라미터화** (Java에서 날짜 조건 전달) |
| EB.ITEM_TYPE = 'W' (서브쿼리 내) | COALESCE(M1.타입구분, M2.타입구분) = 'W' | WHERE로 이동 |
| - | H.마트사구분 = '7' | **추가** (이마트 구분 필터) |

### 4.2 C(비정량 Oracle) WHERE와 B(이마트 MSSQL)의 차이

| C (비정량 Oracle) WHERE | B에 해당하는 조건 | D 전환 시 |
|------------------------|------------------|----------|
| ID.PACKING_QTY = 0 | D.출고수량 > 0 | D.출고수량 > 0으로 변경 |
| ID.GI_REQ_PKG <> 0 | - | 제거 |
| EO.EOI_ID IS NOT NULL | - | 제거 (INNER JOIN으로 보장) |
| WR.CONTRACT_TYPE = '40' | - | 제거 (UNION ALL 없으므로 불필요) |
| IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD') | + qry_where | 파라미터화 |
| EB.ITEM_TYPE IN ('J', 'B') (서브쿼리 내) | COALESCE(M1.타입구분, M2.타입구분) = 'W' | **COALESCE(M1.타입구분, M2.타입구분) IN ('J', 'B')로 변경** |
| EB.BARCODE_TYPE = 'M8' (BCC2 JOIN 조건) | - | **D에서 추가 필요: AND COALESCE(M1.바코드타입, M2.바코드타입) = 'M8'** |
| - | H.마트사구분 = '7' | **D에서도 H.마트사구분 = '7' 추가** |

---

## 5. Oracle 함수 -> MSSQL 전환 매핑

| Oracle 함수/구문 | 사용 위치 | MSSQL 전환 (B에서의 처리) |
|-----------------|----------|------------------------|
| DECODE(A, NULL, B, A) | BL_NO, ITEM_NAME, BARCODE_TYPE 등 | ISNULL(A, B) 또는 COALESCE(A, B) |
| DECODE(A, val, B, C) | CLIENTNAME, EMART_PLANT_CODE, BARCODE_TYPE 등 | CASE WHEN A = val THEN B ELSE C END (B에서는 구조 변경으로 대부분 제거) |
| DE_ITEM(code) | ITEM_NAME | CO_품목코드 JOIN으로 대체 (I.품목명) |
| DE_CLIENT(code) / DE_CLIENT2(code) | CLIENTNAME | ME.점포명으로 대체 (JOIN으로 직접 조회) |
| TO_CHAR(SYSDATE, 'YYYYMMDD') | WHERE 날짜 조건 | Java에서 파라미터로 전달 (qry_where) |
| SUBSTR(str, 1, 2) | CLIENTNAME CJ 판별 | B에서 로직 자체 제거 (ME.점포명으로 단순화) |
| ROWNUM < 2 | S_BARCODE_INFO 서브쿼리 | TOP 1 또는 서브쿼리 제거 (I.상품바코드로 대체) |
| NVL 계열 | EMARTLOGIS_CODE | COALESCE 또는 ISNULL |
| UNION ALL | 해외매입 + 국내매입 | **제거** (단일 쿼리로 통합) |

---

## 6. 비정량 전환 시 변경 요약

B(이마트 MSSQL JSP)를 기반으로 C -> D 전환할 때 변경해야 할 항목만 정리한다.

### 6.1 B에서 그대로 사용 가능한 부분 (변경 불필요)

- JOIN 구조 대부분 (SM_출고상세, SM_출고머리, CO_품목코드, SM_수주머리/상세, SM_마트사발주이마트, SM_출고LOT, CO_거래처MASTER, CO_각종소분류코드, 월품목별재고화일_LOT별_VIEW)
- 컬럼 0~15, 20, 22~30번 (총 26개 컬럼)
- 기본 WHERE 구조 (마트사구분='7', 출고수량>0, qry_where)
- CO_매출처품목코드매핑 2단계 fallback JOIN (M1 -> G2 -> M2)

### 6.2 변경 필요 항목 (7건)

| # | 항목 | B (이마트 MSSQL) | D (비정량 MSSQL) 변경 내용 | 이유 |
|---|------|-----------------|--------------------------|------|
| 1 | PACKER_CODE (16번) | I.패커코드 | **'0000' AS PACKER_CODE** | C에서 '0000' 하드코딩 |
| 2 | PACKER_PRODUCT_CODE (17번) | I.PPCODE | **I.품목코드 AS PACKER_PRODUCT_CODE** | C에서 ID.ITEM_CODE 사용 |
| 3 | ITEM_TYPE (19번) | COALESCE(M1.타입구분, M2.타입구분) | **'HW' AS ITEM_TYPE** | C에서 'HW' 하드코딩 (PDA FLOOR 미처리 구분) |
| 4 | BARCODEGOODS (21번) | I.상품바코드 | **I.품목코드 AS BARCODEGOODS** | C에서 ID.ITEM_CODE 사용 |
| 5 | WHERE 타입 조건 | COALESCE(M1.타입구분, M2.타입구분) = 'W' | **COALESCE(M1.타입구분, M2.타입구분) IN ('J', 'B')** | C에서 EB.ITEM_TYPE IN ('J', 'B') |
| 6 | WHERE 바코드 조건 | 없음 | **AND COALESCE(M1.바코드타입, M2.바코드타입) = 'M8'** 추가 | C에서 EB.BARCODE_TYPE = 'M8' 조건 있음 |
| 7 | JSP 파일명 | search_shipment.jsp | **search_shipment_nonfixed.jsp** (새 파일) | searchType=4 비정량 전용 |

### 6.3 검토 필요 항목

| # | 항목 | 검토 사항 |
|---|------|----------|
| 1 | BARCODE_TYPE = 'M8' 조건 위치 | C에서는 BCC2 JOIN 조건에 있었음. D에서 WHERE로 이동 시 결과 동일한지 확인 |
| 2 | EMART_PLANT_CODE | C에 컬럼 자체가 없음. D에서 B처럼 '' 하드코딩으로 추가하면 됨 |
| 3 | GI_L_ID | C에 없는 컬럼. B에서 추가된 것이므로 D에서도 L.SEQ로 포함 |
| 4 | MAJOR_CATEGORY, CONTAINER_TYPE | A에는 33개 컬럼(이 2개 포함)이지만 B에서는 31개로 줄어듦. D에서도 미포함 |
| 5 | CT_NAME '산' 접미사 | A에서 '산' 붙임. B(C1.명칭)에서는 안 붙임. C에서도 '산' 붙임. D에서 '산' 붙여야 하는지 확인 |

---

## 7. 원본 비교

| 항목 | A (이마트 Oracle VIEW) | B (이마트 MSSQL JSP) | C (비정량 Oracle VIEW) |
|------|----------------------|---------------------|----------------------|
| 스키마 | HIGHLAND | MSSQL 직접 JOIN | INNO |
| 구조 | UNION ALL (해외+국내) | 단일 SELECT | 단일 SELECT |
| 컬럼 수 | 33개 (CREATE VIEW) | 31개 (out.println) | 30개 (CREATE VIEW) |
| ITEM_TYPE 필터 | 'W' (원료육) | 'W' | IN ('J', 'B') (제품, 비정량) |
| BARCODE_TYPE 필터 | 없음 | 없음 | = 'M8' |
| PACKER_CODE | I_BL_D.PACKER_CODE / I_OFFER_D.PACKER_CODE | I.패커코드 | '0000' 하드코딩 |
| ITEM_TYPE 출력 | EO.ITEM_TYPE | COALESCE(M1.타입구분, M2.타입구분) | 'HW' 하드코딩 |
| EMART_PLANT_CODE | DECODE 로직 | '' | 없음 |
| MAJOR_CATEGORY | BI.MAJOR_CATEGORY | 없음 | 없음 |
| CONTAINER_TYPE | BI.CONTAINER_TYPE | 없음 | 없음 |
| MASTER_CODE (센터) | EMART_STORE_CODE | CO_거래처MASTER | EMART_BRANCH_CODE |

---

## 8. 주의사항

- C의 ITEM_TYPE 'HW' 하드코딩은 PDA Java 코드에서 FLOOR 처리를 하지 않기 위한 구분자이므로 반드시 유지해야 한다
- C의 BARCODE_TYPE = 'M8' 조건은 BCC2 JOIN의 ON 절에 있었으나, D에서는 WHERE 절로 이동하면 결과가 달라질 수 있으므로 검증 필요
- C의 B_COMMON_CODE MASTER_CODE가 'EMART_BRANCH_CODE'인 반면 A는 'EMART_STORE_CODE'임. CO_거래처MASTER로 전환 시 마트사구분 값이 동일한지 확인 필요
- B에서 CT_NAME에 '산' 접미사가 없지만 A/C 원본에서는 있음. 기존 기능 100% 동일 원칙에 따라 D에서도 '산' 접미사 포함 여부를 확인해야 함
- PACKER_CODE '0000', BARCODEGOODS = ITEM_CODE 등 비정량 고유 하드코딩은 기존 기능 유지 원칙에 따라 반드시 보존

---

## 9. 관련 문서

- `app/doc/소스분석/41_이마트VIEW_vs_비정량VIEW_비교분석.md` - 이마트/비정량 VIEW 2자 비교
- `app/doc/view/VW_PDA_WID_LIST` - 이마트 Oracle VIEW DDL
- `app/doc/view/VW_PDA_WID_LIST_NONFIXED` - 비정량 Oracle VIEW DDL
- `app/doc/기능/` - 관련 기능 문서
