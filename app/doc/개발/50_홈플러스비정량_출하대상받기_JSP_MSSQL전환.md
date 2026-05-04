# 홈플러스비정량(searchType=5) 출하대상받기 JSP MSSQL 전환

**작성일**: 2026-05-04
**목적**: `search_homeplus_nonfixed.jsp`의 Oracle VIEW(`VW_PDA_WID_LIST_NONFIXED_HP`) 의존성을 제거하고 MSSQL 직접 JOIN 쿼리로 교체한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### search_homeplus_nonfixed.jsp (현재 — 동작 불가)

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_homeplus_nonfixed.jsp`

```java
conn = getMSSQLConnection(); // MSSQL 접속 (이미 변경)

String quertystring = "SELECT "
    + "GI_H_ID"
    + ", GI_D_ID"
    + ", EOI_ID"
    + ", ITEM_CODE"
    + ", ITEM_NAME"
    + ", EMARTITEM_CODE"
    + ", EMARTITEM"
    + ", GI_REQ_PKG"
    + ", GI_REQ_QTY"
    + ", AMOUNT"
    + ", GOODS_R_ID"
    + ", GR_REF_NO"
    + ", GI_REQ_DATE"
    + ", BL_NO"
    + ", BRAND_CODE"
    + ", BRANDNAME"
    + ", CLIENT_CODE"
    + ", CLIENTNAME"
    + ", CENTERNAME"
    + ", ITEM_SPEC"
    + ", CT_CODE"
    + ", IMPORT_ID_NO"
    + ", PACKER_CODE"
    + ", PACKERNAME"
    + ", PACKER_PRODUCT_CODE"
    + ", BARCODE_TYPE"
    + ", ITEM_TYPE"
    + ", PACKWEIGHT"
    + ", BARCODEGOODS"
    + ", STORE_IN_DATE"
    + ", EMARTLOGIS_CODE"
    + ", EMARTLOGIS_NAME"
    + ", WH_AREA"
    + ", USE_NAME"
    + ", USE_CODE"
    + ", CT_NAME"
    + ", STORE_CODE"
    + " FROM VW_PDA_WID_LIST_NONFIXED_HP"
    + qry_where;

// out.println 37개 컬럼 출력
out.println(rs.getString("GI_H_ID") + "::" + rs.getString("GI_D_ID") + "::"
    + rs.getString("EOI_ID") + "::" + rs.getString("ITEM_CODE") + "::"
    ... (37개 컬럼)
    + rs.getString("STORE_CODE") + ";;");
```

현재 out.println 순서 (37개):

| JSP idx | 컬럼명 | Java temp[] | 비고 |
|:-------:|--------|:-----------:|------|
| 0 | GI_H_ID | - | Java 미파싱 → 제거 대상 |
| 1 | GI_D_ID | temp[0] | |
| 2 | EOI_ID | - | Java 미파싱 → 제거 대상 |
| 3 | ITEM_CODE | temp[1] | |
| 4 | ITEM_NAME | temp[2] | |
| 5 | EMARTITEM_CODE | temp[3] | |
| 6 | EMARTITEM | temp[4] | |
| 7 | GI_REQ_PKG | temp[5] | |
| 8 | GI_REQ_QTY | temp[6] | |
| 9 | AMOUNT | - | Java 미파싱 → 제거 대상 |
| 10 | GOODS_R_ID | - | Java 미파싱 → 제거 대상 |
| 11 | GR_REF_NO | - | Java 미파싱 → 제거 대상 |
| 12 | GI_REQ_DATE | temp[7] | |
| 13 | BL_NO | temp[8] | |
| 14 | BRAND_CODE | temp[9] | |
| 15 | BRANDNAME | - | Java 미파싱 → 제거 대상 |
| 16 | CLIENT_CODE | temp[10] | |
| 17 | CLIENTNAME | temp[11] | |
| 18 | CENTERNAME | temp[12] | |
| 19 | ITEM_SPEC | temp[13] | |
| 20 | CT_CODE | temp[14] | |
| 21 | IMPORT_ID_NO | temp[15] | |
| 22 | PACKER_CODE | temp[16] | |
| 23 | PACKERNAME | - | Java 미파싱 → 제거 대상 |
| 24 | PACKER_PRODUCT_CODE | temp[17] | |
| 25 | BARCODE_TYPE | temp[18] | |
| 26 | ITEM_TYPE | temp[19] | |
| 27 | PACKWEIGHT | temp[20] | |
| 28 | BARCODEGOODS | temp[21] | |
| 29 | STORE_IN_DATE | temp[22] | |
| 30 | EMARTLOGIS_CODE | temp[23] | |
| 31 | EMARTLOGIS_NAME | - | Java 미파싱 → 제거 대상 |
| 32 | WH_AREA | temp[24] | |
| 33 | USE_NAME | temp[25] | |
| 34 | USE_CODE | temp[26] | |
| 35 | CT_NAME | temp[27] | |
| 36 | STORE_CODE | temp[28] | |

**제거 대상 8개**: GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME
→ Oracle VIEW에만 있고 Java에서 파싱하지 않음

### Oracle VIEW DDL (VW_PDA_WID_LIST_NONFIXED_HP)

```sql
SELECT
  ID.GI_D_ID,
  ID.ITEM_CODE,
  EOI.ITEM_NAME AS ITEM_NAME,
  EOI.ITEM_CODE AS EMARTITEM_CODE,
  EOI.ITEM_NAME AS EMARTITEM,
  ID.GI_REQ_PKG,
  ID.GI_REQ_QTY,
  IH.GI_REQ_DATE,
  ID.BL_NO,
  ID.BRAND_CODE,
  IH.CLIENT_CODE,
  EOI.STORE_NAME AS CLIENTNAME,
  DE_COMMON('HOMEPLUS_STORE_CODE', EOI.CENTER_CODE) AS CENTERNAME,
  R.ITEM_SPEC,
  R.CT_CODE,
  'IN67677' AS PACKER_CODE,  -- 하드코딩
  R.IMPORT_ID_NO,
  ID.ITEM_CODE AS PACKER_PRODUCT_CODE,
  BEB.BARCODE_TYPE,
  BEB.ITEM_TYPE,
  NULL AS PACKWEIGHT,
  ID.ITEM_CODE AS BARCODEGOODS,
  EOI.STORE_IN_DATE,
  R.GR_WAREHOUSE_CODE,
  ' ' AS EMARTLOGIS_CODE,
  ' ' AS WH_AREA,
  ' ' AS USE_NAME,
  ' ' AS USE_CODE,
  (SELECT BC.CT_NAME FROM B_COUNTRY bc WHERE BC.CT_CODE = R.CT_CODE) AS CT_NAME,
  EOI.STORE_NAME,
  EOI.STORE_CODE
FROM W_GOODS_IH IH
INNER JOIN W_GOODS_ID ID ON IH.GI_H_ID = ID.GI_H_ID
INNER JOIN W_E_ORDER_ITEM EOI ON ID.EOI_ID = EOI.EOI_ID
INNER JOIN B_EMART_BARCODE BEB ON EOI.ITEM_CODE = BEB.EMARTITEM_CODE
INNER JOIN W_GOODS_R R ON R.GOODS_R_ID = ID.GOODS_R_ID
INNER JOIN B_ITEM BI ON BI.ITEM_CODE = ID.ITEM_CODE
WHERE ID.STATUS = '10'
AND BEB.BARCODE_TYPE = 'H5'
AND R.GR_DATE > '20240101'
ORDER BY BEB.EMRTITEM_NAME, EOI.STORE_CODE
```

### ProgressDlgShipSearch.java searchType="5" 파싱 구조 (현재)

```java
// ProgressDlgShipSearch.java L274~280
// 홈플러스 비정량(5): 추가 5개 필드
} else if(Common.searchType.equals("5")) {
    si.setWH_AREA(temp[24].toString());          // 창고구역
    si.setUSE_NAME(temp[25].toString());         // 용도명
    si.setUSE_CODE(temp[26].toString());         // 용도코드
    si.setCT_NAME(temp[27].toString());          // CT명
    si.setSTORE_CODE(temp[28].toString());       // 점포코드
}
```

Java가 기대하는 컬럼 수: 공통 24개(temp[0]~temp[23]) + 전용 5개(temp[24]~temp[28]) = **총 29개**

중요: searchType="5"는 GI_L_ID setter 없음 (홈플러스 비정량은 GI_L_ID 미사용 — 의도된 설계).
중요: searchType="5" Java에서 창고코드 WHERE 조건 추가 없음 (비정량은 창고 필터 없음).

### 문제점

1. **JSP DB 오류**: `getMSSQLConnection()`으로 MSSQL 연결 후 Oracle 전용 VIEW `VW_PDA_WID_LIST_NONFIXED_HP` 참조 → MSSQL에 해당 VIEW 없음 → 실행 즉시 SQL 오류
2. **인덱스 전부 불일치**: 현재 JSP index 0=GI_H_ID, Java temp[0]=GI_D_ID → 37개 전부 인덱스 불일치
3. **제거 컬럼 8개 포함**: GI_H_ID 등 8개는 Java에서 파싱하지 않으므로 출력 불필요
4. **CENTERNAME 매핑 불가**: Oracle `DE_COMMON('HOMEPLUS_STORE_CODE', EOI.CENTER_CODE)` 함수 → MSSQL `CO_거래처MASTER B` JOIN으로 교체 필요
5. **PACKER_CODE 하드코딩 → 동적 변경**: Oracle `'IN67677'` 하드코딩 → MSSQL `I.패커코드`로 교체 (허용 차이)

---

## 2. 변경 구조

### 데이터 흐름

```
[변경 전]
PDA (searchType=5) → search_homeplus_nonfixed.jsp
    → FROM VW_PDA_WID_LIST_NONFIXED_HP (Oracle VIEW, MSSQL 없음)
    → 37개 컬럼 출력 (index 0=GI_H_ID)
    → Java temp[0]=GI_D_ID로 파싱 → 전부 불일치 → 동작 불가

[변경 후]
PDA (searchType=5) → search_homeplus_nonfixed.jsp
    → MSSQL 직접 JOIN 쿼리
        (SM_출고상세 D, SM_출고머리 H, CO_품목코드 I,
         SM_마트사발주홈플러스 HE, CO_거래처MASTER B,
         CO_매출처품목코드매핑 M1/M2, SM_출고LOT L,
         월품목별재고화일_LOT별_VIEW V, SM_수주상세 SD,
         CO_각종소분류코드 C1)
    → 29개 컬럼 출력 (index 0=GI_D_ID)
    → Java temp[0]=GI_D_ID로 파싱 → 전부 일치 → 정상 동작
```

### 컬럼 수 변화

| 구분 | 컬럼 수 | 비고 |
|------|:-------:|------|
| 변경 전 (Oracle JSP) | 37개 | GI_H_ID(0) ~ STORE_CODE(36) |
| 변경 후 (MSSQL JSP) | 29개 | GI_D_ID(0) ~ STORE_CODE(28) |
| 제거 컬럼 | 8개 | GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME |

### 전환 후 out.println 순서 (29개)

| 새 idx | 컬럼명 | Java temp[] |
|:------:|--------|:-----------:|
| 0 | GI_D_ID | temp[0] |
| 1 | ITEM_CODE | temp[1] |
| 2 | ITEM_NAME | temp[2] |
| 3 | EMARTITEM_CODE | temp[3] |
| 4 | EMARTITEM | temp[4] |
| 5 | GI_REQ_PKG | temp[5] |
| 6 | GI_REQ_QTY | temp[6] |
| 7 | GI_REQ_DATE | temp[7] |
| 8 | BL_NO | temp[8] |
| 9 | BRAND_CODE | temp[9] |
| 10 | CLIENT_CODE | temp[10] |
| 11 | CLIENTNAME | temp[11] |
| 12 | CENTERNAME | temp[12] |
| 13 | ITEM_SPEC | temp[13] |
| 14 | CT_CODE | temp[14] |
| 15 | IMPORT_ID_NO | temp[15] |
| 16 | PACKER_CODE | temp[16] |
| 17 | PACKER_PRODUCT_CODE | temp[17] |
| 18 | BARCODE_TYPE | temp[18] |
| 19 | ITEM_TYPE | temp[19] |
| 20 | PACKWEIGHT | temp[20] |
| 21 | BARCODEGOODS | temp[21] |
| 22 | STORE_IN_DATE | temp[22] |
| 23 | EMARTLOGIS_CODE | temp[23] |
| 24 | WH_AREA | temp[24] |
| 25 | USE_NAME | temp[25] |
| 26 | USE_CODE | temp[26] |
| 27 | CT_NAME | temp[27] |
| 28 | STORE_CODE | temp[28] |

### Oracle → MSSQL 컬럼 매핑

| 컬럼 | Oracle | MSSQL |
|------|--------|-------|
| GI_D_ID | ID.GI_D_ID | D.SEQ |
| ITEM_CODE | ID.ITEM_CODE | I.품목코드 |
| ITEM_NAME | EOI.ITEM_NAME | HE.상품명 |
| EMARTITEM_CODE | EOI.ITEM_CODE | HE.상품코드 |
| EMARTITEM | EOI.ITEM_NAME | HE.상품명 |
| GI_REQ_PKG | ID.GI_REQ_PKG | L.박스수량 |
| GI_REQ_QTY | ID.GI_REQ_QTY | L.중량 |
| GI_REQ_DATE | IH.GI_REQ_DATE | D.출고일자 |
| BL_NO | ID.BL_NO | COALESCE(NULLIF(V.BLNO,''), V.이력번호) |
| BRAND_CODE | ID.BRAND_CODE | '' (빈문자) |
| CLIENT_CODE | IH.CLIENT_CODE | HE.납품처코드 |
| CLIENTNAME | EOI.STORE_NAME | HE.납품처명 |
| CENTERNAME | DE_COMMON('HOMEPLUS_STORE_CODE', EOI.CENTER_CODE) | B.상호 (CO_거래처MASTER B ON HE.납품처코드=B.마트사거래처코드 AND B.마트사구분='4') |
| ITEM_SPEC | R.ITEM_SPEC | I.규격 |
| CT_CODE | R.CT_CODE | I.원산지 |
| IMPORT_ID_NO | R.IMPORT_ID_NO | V.이력번호 |
| PACKER_CODE | 'IN67677' (하드코딩) | I.패커코드 (하드코딩→동적 변경, 허용) |
| PACKER_PRODUCT_CODE | ID.ITEM_CODE | I.품목코드 |
| BARCODE_TYPE | BEB.BARCODE_TYPE | COALESCE(M1.바코드타입, M2.바코드타입) |
| ITEM_TYPE | BEB.ITEM_TYPE | COALESCE(M1.타입구분, M2.타입구분) |
| PACKWEIGHT | NULL | NULL |
| BARCODEGOODS | ID.ITEM_CODE | I.품목코드 |
| STORE_IN_DATE | EOI.STORE_IN_DATE | SD.납기일자 (LEFT JOIN SM_수주상세 SD ON SD.마트사SEQ=HE.SEQ) |
| EMARTLOGIS_CODE | ' ' (하드코딩) | '' (빈문자) |
| WH_AREA | ' ' (하드코딩) | '' (빈문자) |
| USE_NAME | ' ' (하드코딩) | '' (빈문자) |
| USE_CODE | ' ' (하드코딩) | '' (빈문자) |
| CT_NAME | (SELECT BC.CT_NAME FROM B_COUNTRY WHERE BC.CT_CODE=R.CT_CODE) | C1.명칭 (LEFT JOIN CO_각종소분류코드 C1 ON C1.회사코드=I.회사코드 AND C1.대분류='Q14' AND C1.소분류=I.원산지) |
| STORE_CODE | EOI.STORE_CODE | HE.납품처코드 |

### 허용 차이 (Oracle VIEW → MSSQL 전환)

| # | 항목 | Oracle | MSSQL | 허용 근거 |
|---|------|--------|-------|----------|
| 1 | ID.STATUS='10' 조건 | WHERE에 포함 | 제거 | MSSQL SM_출고상세에 STATUS 컬럼 없음 (개발49 동일 패턴) |
| 2 | R.GR_DATE>'20240101' 조건 | WHERE에 포함 (임시 하드코딩) | 제거 | Oracle 임시 날짜 필터, MSSQL에서는 qry_where로 날짜 조건 전달 |
| 3 | ORDER BY | BEB.EMRTITEM_NAME, EOI.STORE_CODE | HE.납품처코드 ASC, I.품목코드 ASC | Oracle 정렬과 의미상 유사한 홈플러스 비정량 적합 정렬로 교체 |
| 4 | PACKER_CODE | 'IN67677' 하드코딩 | I.패커코드 | 하드코딩 변경 허용 (CLAUDE.md 규칙) |
| 5 | DB 접속 | Oracle JDBC | getMSSQLConnection() | 이미 적용됨 |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_homeplus_nonfixed.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\` | Oracle VIEW 제거 → MSSQL 직접 JOIN (29개 컬럼) |

---

## 4. 수정 상세

### 4.1 search_homeplus_nonfixed.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_homeplus_nonfixed.jsp`

**변경 전 (L37~96):**

```java
String quertystring = "SELECT "
    + "GI_H_ID"
    + ", GI_D_ID"
    + ", EOI_ID"
    + ", ITEM_CODE"
    + ", ITEM_NAME"
    + ", EMARTITEM_CODE"
    + ", EMARTITEM"
    + ", GI_REQ_PKG"
    + ", GI_REQ_QTY"
    + ", AMOUNT"
    + ", GOODS_R_ID"
    + ", GR_REF_NO"
    + ", GI_REQ_DATE"
    + ", BL_NO"
    + ", BRAND_CODE"
    + ", BRANDNAME"
    + ", CLIENT_CODE"
    + ", CLIENTNAME"
    + ", CENTERNAME"
    + ", ITEM_SPEC"
    + ", CT_CODE"
    + ", IMPORT_ID_NO"
    + ", PACKER_CODE"
    + ", PACKERNAME"
    + ", PACKER_PRODUCT_CODE"
    + ", BARCODE_TYPE"
    + ", ITEM_TYPE"
    + ", PACKWEIGHT"
    + ", BARCODEGOODS"
    + ", STORE_IN_DATE"
    + ", EMARTLOGIS_CODE"
    + ", EMARTLOGIS_NAME"
    + ", WH_AREA"
    + ", USE_NAME"
    + ", USE_CODE"
    + ", CT_NAME"
    + ", STORE_CODE"
    + " FROM VW_PDA_WID_LIST_NONFIXED_HP"
    + qry_where;

// out.println (37개)
out.println(rs.getString("GI_H_ID") + "::" + rs.getString("GI_D_ID") + "::"
    + rs.getString("EOI_ID") + "::" + rs.getString("ITEM_CODE") + "::"
    + rs.getString("ITEM_NAME") + "::" + rs.getString("EMARTITEM_CODE") + "::" + rs.getString("EMARTITEM") + "::"
    + rs.getString("GI_REQ_PKG") + "::" + rs.getString("GI_REQ_QTY") + "::" + rs.getString("AMOUNT") + "::"
    + rs.getString("GOODS_R_ID") + "::" + rs.getString("GR_REF_NO") + "::" + rs.getString("GI_REQ_DATE") + "::" + rs.getString("BL_NO") + "::"
    + rs.getString("BRAND_CODE") + "::" + rs.getString("BRANDNAME") + "::" + rs.getString("CLIENT_CODE") + "::"
    + rs.getString("CLIENTNAME") + "::" + rs.getString("CENTERNAME") + "::" + rs.getString("ITEM_SPEC") + "::"
    + rs.getString("CT_CODE") + "::" + rs.getString("IMPORT_ID_NO") + "::" + rs.getString("PACKER_CODE") + "::"
    + rs.getString("PACKERNAME") + "::" + rs.getString("PACKER_PRODUCT_CODE") + "::" + rs.getString("BARCODE_TYPE") + "::"
    + rs.getString("ITEM_TYPE") + "::" + rs.getString("PACKWEIGHT") + "::" + rs.getString("BARCODEGOODS") + "::" + rs.getString("STORE_IN_DATE") + "::"
    + rs.getString("EMARTLOGIS_CODE") + "::" + rs.getString("EMARTLOGIS_NAME") + "::" + rs.getString("WH_AREA") + "::" + rs.getString("USE_NAME") + "::" + rs.getString("USE_CODE") + "::" + rs.getString("CT_NAME") + "::" + rs.getString("STORE_CODE") + ";;");
```

**변경 후:**

```java
String quertystring = "SELECT /* 홈플러스 비정량 출하대상 조회 */"
    + " D.SEQ AS GI_D_ID"
    + ", I.품목코드 AS ITEM_CODE"
    + ", HE.상품명 AS ITEM_NAME"
    + ", HE.상품코드 AS EMARTITEM_CODE"
    + ", HE.상품명 AS EMARTITEM"
    + ", L.박스수량 AS GI_REQ_PKG"
    + ", L.중량 AS GI_REQ_QTY"
    + ", D.출고일자 AS GI_REQ_DATE"
    + ", COALESCE(NULLIF(V.BLNO, ''), V.이력번호) AS BL_NO"
    + ", '' AS BRAND_CODE"
    + ", HE.납품처코드 AS CLIENT_CODE"
    + ", HE.납품처명 AS CLIENTNAME"
    + ", B.상호 AS CENTERNAME"
    + ", I.규격 AS ITEM_SPEC"
    + ", I.원산지 AS CT_CODE"
    + ", V.이력번호 AS IMPORT_ID_NO"
    + ", I.패커코드 AS PACKER_CODE"
    + ", I.품목코드 AS PACKER_PRODUCT_CODE"
    + ", COALESCE(M1.바코드타입, M2.바코드타입) AS BARCODE_TYPE"
    + ", COALESCE(M1.타입구분, M2.타입구분) AS ITEM_TYPE"
    + ", NULL AS PACKWEIGHT"
    + ", I.품목코드 AS BARCODEGOODS"
    + ", SD.납기일자 AS STORE_IN_DATE"
    + ", '' AS EMARTLOGIS_CODE"
    + ", '' AS WH_AREA"
    + ", '' AS USE_NAME"
    + ", '' AS USE_CODE"
    + ", C1.명칭 AS CT_NAME"
    + ", HE.납품처코드 AS STORE_CODE"
    + " FROM SM_출고상세 D"
    + " INNER JOIN SM_출고머리 H"
    + "   ON H.회사코드 = D.회사코드"
    + "  AND H.출고사업장 = D.출고사업장"
    + "  AND H.출고일자 = D.출고일자"
    + "  AND H.출고일련번호 = D.출고일련번호"
    + " JOIN CO_품목코드 I"
    + "   ON D.회사코드 = I.회사코드"
    + "  AND D.출고품목코드 = I.품목코드"
    + " JOIN SM_마트사발주홈플러스 HE"
    + "   ON D.마트사SEQ = HE.SEQ"
    + " JOIN CO_거래처MASTER B"
    + "   ON HE.회사코드 = B.회사코드"
    + "  AND HE.납품처코드 = B.마트사거래처코드"
    + "  AND B.마트사구분 = '4'"
    + " LEFT JOIN CO_거래처MASTER G"
    + "   ON G.회사코드 = D.회사코드"
    + "  AND G.거래처코드 = H.출고거래처"
    + " LEFT JOIN CO_매출처품목코드매핑 M1"
    + "   ON M1.회사코드 = D.회사코드"
    + "  AND M1.품목코드 = D.출고품목코드"
    + "  AND M1.거래처코드 = H.출고거래처"
    + " LEFT JOIN CO_거래처MASTER G2"
    + "   ON G2.회사코드 = D.회사코드"
    + "  AND G2.계층코드 = LEFT(G.계층코드, 5)"
    + "  AND G2.거래처코드 != H.출고거래처"
    + " LEFT JOIN CO_매출처품목코드매핑 M2"
    + "   ON M2.회사코드 = D.회사코드"
    + "  AND M2.품목코드 = D.출고품목코드"
    + "  AND M2.거래처코드 = G2.거래처코드"
    + " JOIN SM_출고LOT L"
    + "   ON L.출고상세SEQ = D.SEQ"
    + " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"
    + "   ON V.회사코드 = D.회사코드"
    + "  AND V.사업장 = D.출고사업장"
    + "  AND V.창고코드 = D.창고코드"
    + "  AND V.품목코드 = D.출고품목코드"
    + "  AND V.LOTNO = L.LOTNO"
    + " LEFT JOIN SM_수주상세 SD"
    + "   ON SD.마트사SEQ = HE.SEQ"
    + " LEFT JOIN CO_각종소분류코드 C1"
    + "   ON C1.회사코드 = I.회사코드"
    + "  AND C1.대분류 = 'Q14'"
    + "  AND C1.소분류 = I.원산지"
    + " WHERE H.마트사구분 = '4'"
    + "   AND D.출고수량 > 0"
    + "   AND COALESCE(M1.바코드타입, M2.바코드타입) = 'H5'"
    + qry_where
    + " ORDER BY HE.납품처코드 ASC, I.품목코드 ASC";

// out.println (29개 — temp[] 인덱스 그대로 유지)
out.println(
    rs.getString("GI_D_ID") + "::"          // 0
  + rs.getString("ITEM_CODE") + "::"         // 1
  + rs.getString("ITEM_NAME") + "::"         // 2
  + rs.getString("EMARTITEM_CODE") + "::"    // 3
  + rs.getString("EMARTITEM") + "::"         // 4
  + rs.getString("GI_REQ_PKG") + "::"        // 5
  + rs.getString("GI_REQ_QTY") + "::"        // 6
  + rs.getString("GI_REQ_DATE") + "::"       // 7
  + rs.getString("BL_NO") + "::"             // 8
  + rs.getString("BRAND_CODE") + "::"        // 9
  + rs.getString("CLIENT_CODE") + "::"       // 10
  + rs.getString("CLIENTNAME") + "::"        // 11
  + rs.getString("CENTERNAME") + "::"        // 12
  + rs.getString("ITEM_SPEC") + "::"         // 13
  + rs.getString("CT_CODE") + "::"           // 14
  + rs.getString("IMPORT_ID_NO") + "::"      // 15
  + rs.getString("PACKER_CODE") + "::"       // 16
  + rs.getString("PACKER_PRODUCT_CODE") + "::" // 17
  + rs.getString("BARCODE_TYPE") + "::"      // 18
  + rs.getString("ITEM_TYPE") + "::"         // 19
  + rs.getString("PACKWEIGHT") + "::"        // 20
  + rs.getString("BARCODEGOODS") + "::"      // 21
  + rs.getString("STORE_IN_DATE") + "::"     // 22
  + rs.getString("EMARTLOGIS_CODE") + "::"   // 23
  + rs.getString("WH_AREA") + "::"           // 24
  + rs.getString("USE_NAME") + "::"          // 25
  + rs.getString("USE_CODE") + "::"          // 26
  + rs.getString("CT_NAME") + "::"           // 27
  + rs.getString("STORE_CODE") + ";;");      // 28
```

**검증**: Java `temp[0]~temp[28]` 각각 `GI_D_ID~STORE_CODE`에 1:1 매핑 확인 (29개)

---

## 5. 사이드이펙트

### searchType=5 이외 분기

```java
// ProgressDlgShipSearch.java
if(Common.searchType.equals("0") || Common.searchType.equals("4")) { ... }
else if(Common.searchType.equals("5")) { ... }  // 홈플러스 비정량
else if(Common.searchType.equals("6")) { ... }  // 롯데
```

- **영향 없음**: searchType별로 별도 JSP URL을 사용하며 독립적으로 동작
- searchType=2(홈플러스 정량), 4(비정량 이마트) 등 다른 분기는 전혀 영향받지 않음

### LabelPrintHelper (홈플러스 비정량 라벨 출력)

```java
// LabelPrintHelper.java — searchType=5 홈플러스 비정량 라벨 출력 관련
// PACKER_CODE, BARCODE_TYPE, ITEM_TYPE, BARCODEGOODS 등이 라벨 출력에 사용됨
```

- **영향**: PACKER_CODE가 Oracle 하드코딩 'IN67677'에서 `I.패커코드`(동적)로 변경됨 → 라벨 출력 시 패커코드 값 달라질 수 있음
- **대응 방안**: Step 2 통합 테스트에서 실제 데이터로 라벨 출력 값 검증 필수
- **BARCODE_TYPE = 'H5'**: WHERE 조건에 포함되어 조회 결과 일치 유지

---

## 6. 데이터 저장 구조

### 변수 매핑 (searchType=5 파싱 — ProgressDlgShipSearch.java)

| temp[] | 컬럼명 | 저장 필드 | 용도 |
|:------:|--------|-----------|------|
| temp[0] | GI_D_ID | si.GI_D_ID | 출하상세 PK (SEQ) |
| temp[1] | ITEM_CODE | si.ITEM_CODE | 품목코드 |
| temp[2] | ITEM_NAME | si.ITEM_NAME | 홈플러스 발주서 상품명 |
| temp[3] | EMARTITEM_CODE | si.EMARTITEM_CODE | 홈플러스 상품코드 |
| temp[4] | EMARTITEM | si.EMARTITEM | 홈플러스 상품명 |
| temp[5] | GI_REQ_PKG | si.GI_REQ_PKG | 요청박스수 (Float→Int 변환) |
| temp[6] | GI_REQ_QTY | si.GI_REQ_QTY | 요청중량 (소수점 처리) |
| temp[7] | GI_REQ_DATE | si.GI_REQ_DATE | 출고일자 |
| temp[8] | BL_NO | si.BL_NO | BL번호 또는 이력번호 |
| temp[9] | BRAND_CODE | si.BRAND_CODE | 브랜드코드 ('' 고정) |
| temp[10] | CLIENT_CODE | si.CLIENT_CODE | 납품처코드 |
| temp[11] | CLIENTNAME | si.CLIENTNAME | 납품처명 |
| temp[12] | CENTERNAME | si.CENTERNAME | 홈플러스 거래처 상호 |
| temp[13] | ITEM_SPEC | si.ITEM_SPEC | 품목규격 |
| temp[14] | CT_CODE | si.CT_CODE | 원산지코드 |
| temp[15] | IMPORT_ID_NO | si.IMPORT_ID_NO | 이력번호 |
| temp[16] | PACKER_CODE | si.PACKER_CODE | 패커코드 (동적, Oracle 하드코딩 → 변경) |
| temp[17] | PACKER_PRODUCT_CODE | si.PACKER_PRODUCT_CODE | 패커상품코드 (품목코드) |
| temp[18] | BARCODE_TYPE | si.BARCODE_TYPE | 바코드타입 (H5) |
| temp[19] | ITEM_TYPE | si.ITEM_TYPE | 아이템타입 |
| temp[20] | PACKWEIGHT | si.PACKWEIGHT | 포장중량 (NULL 고정) |
| temp[21] | BARCODEGOODS | si.BARCODEGOODS | 상품바코드 (품목코드) |
| temp[22] | STORE_IN_DATE | si.STORE_IN_DATE | 납기일자 |
| temp[23] | EMARTLOGIS_CODE | si.EMARTLOGIS_CODE | 물류코드 ('' 고정) |
| temp[24] | WH_AREA | si.WH_AREA | 창고구역 ('' 고정) |
| temp[25] | USE_NAME | si.USE_NAME | 용도명 ('' 고정) |
| temp[26] | USE_CODE | si.USE_CODE | 용도코드 ('' 고정) |
| temp[27] | CT_NAME | si.CT_NAME | 국가명 (CO_각종소분류코드에서 조회) |
| temp[28] | STORE_CODE | si.STORE_CODE | 점포코드 (납품처코드) |

### 인덱스 매핑

```
JSP out.println 순서  ↔  Java temp[] 인덱스
0=GI_D_ID             ↔  temp[0]  → si.GI_D_ID
1=ITEM_CODE           ↔  temp[1]  → si.ITEM_CODE
2=ITEM_NAME           ↔  temp[2]  → si.ITEM_NAME
3=EMARTITEM_CODE      ↔  temp[3]  → si.EMARTITEM_CODE
4=EMARTITEM           ↔  temp[4]  → si.EMARTITEM
5=GI_REQ_PKG          ↔  temp[5]  → si.GI_REQ_PKG
6=GI_REQ_QTY          ↔  temp[6]  → si.GI_REQ_QTY
7=GI_REQ_DATE         ↔  temp[7]  → si.GI_REQ_DATE
8=BL_NO               ↔  temp[8]  → si.BL_NO
9=BRAND_CODE          ↔  temp[9]  → si.BRAND_CODE
10=CLIENT_CODE        ↔  temp[10] → si.CLIENT_CODE
11=CLIENTNAME         ↔  temp[11] → si.CLIENTNAME
12=CENTERNAME         ↔  temp[12] → si.CENTERNAME
13=ITEM_SPEC          ↔  temp[13] → si.ITEM_SPEC
14=CT_CODE            ↔  temp[14] → si.CT_CODE
15=IMPORT_ID_NO       ↔  temp[15] → si.IMPORT_ID_NO
16=PACKER_CODE        ↔  temp[16] → si.PACKER_CODE
17=PACKER_PRODUCT_CODE ↔ temp[17] → si.PACKER_PRODUCT_CODE
18=BARCODE_TYPE       ↔  temp[18] → si.BARCODE_TYPE
19=ITEM_TYPE          ↔  temp[19] → si.ITEM_TYPE
20=PACKWEIGHT         ↔  temp[20] → si.PACKWEIGHT
21=BARCODEGOODS       ↔  temp[21] → si.BARCODEGOODS
22=STORE_IN_DATE      ↔  temp[22] → si.STORE_IN_DATE
23=EMARTLOGIS_CODE    ↔  temp[23] → si.EMARTLOGIS_CODE
24=WH_AREA            ↔  temp[24] → si.WH_AREA
25=USE_NAME           ↔  temp[25] → si.USE_NAME
26=USE_CODE           ↔  temp[26] → si.USE_CODE
27=CT_NAME            ↔  temp[27] → si.CT_NAME
28=STORE_CODE         ↔  temp[28] → si.STORE_CODE
```

---

## 7. 호출 시점

```
[PDA 앱 — 출하대상 받기 버튼 터치]
    ↓
ShipmentActivity.onClickReceive()
    ↓
ProgressDlgShipSearch (AsyncTask)
    ↓
doInBackground()
    ├── Common.searchType = "5"  (홈플러스 비정량)
    ├── 창고코드 WHERE 조건 추가 없음  (searchType=5는 창고 필터 미적용 — 의도된 설계)
    ↓
HttpHelper.sendDataDb(data, "highland", "search_shipment", URL_SEARCH_SHIPMENT_HOMEPLUS_NONFIXED)
    ↓
search_homeplus_nonfixed.jsp
    ├── getMSSQLConnection()
    ├── MSSQL 직접 JOIN 쿼리 (Step 1 수정 후)
    │     FROM SM_출고상세 D
    │     JOIN SM_마트사발주홈플러스 HE ...
    │     WHERE H.마트사구분='4' AND D.출고수량>0
    │       AND COALESCE(M1.바코드타입,M2.바코드타입)='H5'
    └── out.println (29개 컬럼, ";;" 구분자)
    ↓
ProgressDlgShipSearch.doInBackground()
    ├── split(";;") → rows[]
    ├── split("::", -1) → temp[0..28]
    └── Shipments_Info 객체 생성 (temp[0]~temp[28] 파싱)
        └── searchType="5" 분기: temp[24]~temp[28] 추가 5개 파싱
    ↓
onPostExecute() → ShipmentActivity UI 갱신
```

---

## 8. 개발 플랜

### Step 1: search_homeplus_nonfixed.jsp MSSQL 직접 JOIN 쿼리 작성 (29개 컬럼)

**Part 1. 분석**
- 파일: `search_homeplus_nonfixed.jsp`
- 범위: JSP 전체 쿼리 블록 (L37~96)
- 용도: Oracle VIEW `VW_PDA_WID_LIST_NONFIXED_HP` 제거, MSSQL 직접 JOIN 쿼리로 교체, out.println 37개 → 29개로 정리
- 주의할 점:
  - CENTERNAME = `B.상호` (CO_거래처MASTER B ON HE.납품처코드=B.마트사거래처코드 AND B.마트사구분='4') — Oracle DE_COMMON 함수 대체
  - BARCODE_TYPE WHERE 조건 = `COALESCE(M1.바코드타입, M2.바코드타입) = 'H5'` 포함 필수
  - PACKWEIGHT = `NULL` (Oracle VIEW 하드코딩 NULL 그대로 유지)
  - EMARTLOGIS_CODE, WH_AREA, USE_NAME, USE_CODE = `''` (Oracle 하드코딩 공백 동일 처리)
  - out.println 순서가 Java temp[] 인덱스와 정확히 일치해야 함 (29개)
  - searchType="5" Java 파싱은 temp[0]~temp[28], GI_L_ID 없음 (의도된 설계)

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | Oracle VIEW 참조 | JSP L75 | `FROM VW_PDA_WID_LIST_NONFIXED_HP` 제거 |
| 2 | SELECT 37개 컬럼 | JSP L38~74 | 29개로 교체 (제거 8개: GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME) |
| 3 | out.println 37개 | JSP L87~96 | 29개로 교체 |
| 4 | ORDER BY 없음 | JSP L76 | `ORDER BY HE.납품처코드 ASC, I.품목코드 ASC` 추가 |

**Part 2. 변환 계획**
- 변환 방식: 4.1절 변경 후 코드 기준으로 전체 쿼리 교체
- CENTERNAME: Oracle `DE_COMMON` 함수 → `CO_거래처MASTER B` JOIN (`B.상호`) — search_shipment_homeplus.jsp 동일 패턴
- CT_NAME: Oracle 서브쿼리 → `CO_각종소분류코드 C1` LEFT JOIN (`C1.명칭`) — search_production_nonfixed.jsp 동일 패턴
- BARCODE_TYPE 조건: `COALESCE(M1.바코드타입, M2.바코드타입) = 'H5'` (홈플러스 비정량 전용)
- 주의사항: `qry_where`는 기존과 동일하게 WHERE 절 뒤에 이어 붙임, `getMSSQLConnection()` 및 Statement/ResultSet 처리 코드는 변경하지 않음

**사전 ⑤⑥ 검증 결과 (코드 수정 전)**

| 검증 | 결과 | 비고 |
|------|------|------|
| ⑤ code-verifier | PASS | 인덱스 29개, WHERE 조건, 로컬DB 정합성, GI_L_ID 미사용, NULL 처리 전체 이상 없음 |
| ⑥ original-comparator | PASS | 허용 차이 9건 (STATUS/GR_DATE 조건 제거, ORDER BY 변경, PACKER_CODE 동적화 등), 비허용 차이 0건 |

**체크리스트**
- [x] Part 1: SELECT 29개 컬럼 구성 확인 (제거 8개 누락 없는지 검증)
- [x] Part 1: out.println 순서 29개 확인 (temp[0]~temp[28] 인덱스 일치)
- [x] Part 2: MSSQL JOIN 구조 작성 완료
- [x] Part 2: WHERE H.마트사구분='4' AND D.출고수량>0 AND COALESCE(M1.바코드타입,M2.바코드타입)='H5' 포함 확인
- [x] Part 2: CENTERNAME = B.상호 (CO_거래처MASTER JOIN) 확인
- [x] Part 2: CT_NAME = C1.명칭 (CO_각종소분류코드 LEFT JOIN) 확인
- [x] Part 2: ORDER BY 추가 확인
- [x] Part 3: JSP 파일 저장 완료
- [ ] Part 4: Tomcat 재시작 후 JSP 컴파일 오류 없음 확인
- [ ] Part 5: MSSQL에서 해당 쿼리 직접 실행하여 결과 29개 컬럼 확인

**Part 6. 변경 내용**:
- **무엇을**: `search_homeplus_nonfixed.jsp` Oracle VIEW `VW_PDA_WID_LIST_NONFIXED_HP` 참조 제거 → MSSQL 직접 JOIN 쿼리로 교체, out.println 37개 → 29개로 정리
- **왜**: MSSQL 접속 후 Oracle 전용 VIEW 참조로 실행 즉시 SQL 오류 발생, Java temp[0]~temp[28] 기대 인덱스와 37개 출력 인덱스가 전부 불일치(index shift 8) 상태
- **어떻게**: Oracle `DE_COMMON` 함수 → `CO_거래처MASTER B (B.마트사구분='4')` JOIN, Oracle 서브쿼리 CT_NAME → `CO_각종소분류코드 C1 (대분류='Q14')` LEFT JOIN, 미파싱 8개 컬럼(GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME) 제거, BARCODE_TYPE 조건 `COALESCE(M1.바코드타입,M2.바코드타입)='H5'` 적용

---

### Step 2: 통합 테스트

| # | 테스트 항목 | 확인 |
|:-:|------------|------|
| 1 | PDA에서 searchType=5(홈플러스 비정량) 선택 후 출하대상받기 실행 → 정상 조회 | □ |
| 2 | 출하대상 목록 29개 필드 모두 정상 파싱 확인 (GI_D_ID~STORE_CODE) | □ |
| 3 | CENTERNAME(B.상호) 값이 홈플러스 거래처명으로 정상 출력 확인 | □ |
| 4 | CT_NAME(C1.명칭) 값이 원산지 국가명으로 정상 출력 확인 | □ |
| 5 | BARCODE_TYPE = 'H5' 인 데이터만 조회되는지 확인 | □ |
| 6 | PACKER_CODE — 동적 값(I.패커코드)이 정상 출력되는지 확인 | □ |
| 7 | STORE_CODE 및 STORE_IN_DATE 값 정상 출력 확인 | □ |
| 8 | searchType=2(홈플러스 정량), 4(비정량 이마트), 0(이마트) 정상 동작 영향 없음 확인 | □ |
| 9 | 전체 홈플러스 비정량 출하 → 계근 → 라벨 출력 전체 흐름 1건 End-to-End 검증 | □ |

---

### 개발 순서 요약

```
Step 1: search_homeplus_nonfixed.jsp MSSQL 직접 JOIN (29개 컬럼)
    ↓
Step 2: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 홈플러스 비정량 출하대상 정상 조회

```
1. PDA 앱 로그인
2. 마트사 구분 → 홈플러스 비정량(searchType=5) 선택
3. 출하대상 받기 버튼 터치 (창고 필터 없음 — 비정량 설계)
4. 서버로부터 출하 목록 수신 확인
5. 목록 항목 선택 → 상세 정보 29개 필드 값 확인
   (GI_D_ID, ITEM_CODE, ITEM_NAME, CENTERNAME, CT_NAME, BARCODE_TYPE='H5' 등)
6. STORE_CODE, STORE_IN_DATE, PACKER_CODE 값 정상 여부 확인
```

### 시나리오 2: BARCODE_TYPE 필터링 동작 확인

```
1. 홈플러스 비정량 출하대상 조회 (시나리오 1 완료)
2. 조회된 전체 목록의 BARCODE_TYPE이 모두 'H5'인지 확인
3. 'H5' 이외 바코드타입 데이터가 목록에 포함되지 않는지 확인
```

### 시나리오 3: 홈플러스 비정량 라벨 출력 확인

```
1. 홈플러스 비정량 출하대상 조회 (시나리오 1 완료)
2. 계근 화면 진입
3. 비정량 품목 선택
4. 중량 입력 후 라벨 출력 실행
5. 라벨 내용 확인:
   - PACKER_CODE(패커코드) 정상 출력
   - CT_NAME(국가명) 정상 출력
   - STORE_CODE(점포코드) 정상 출력
6. 기존 Oracle 환경 출력값과 동일한지 대조
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | SM_마트사발주홈플러스 JOIN 결과 0건 | `D.마트사SEQ=HE.SEQ` 조건 불일치 (ERP 전환 후 SEQ 체계 변경 가능) | ERP 담당자에게 홈플러스 비정량 마트사 발주 연결 키 재확인 |
| 2 | CENTERNAME(B.상호) NULL | CO_거래처MASTER에 HE.납품처코드 - 마트사구분='4' 조합 미존재 | JOIN → LEFT JOIN 변경 후 운영 데이터 확인, 홈플러스 정량 JSP 동작과 비교 |
| 3 | CT_NAME NULL | CO_각종소분류코드에 대분류='Q14', 소분류=I.원산지 데이터 없음 | LEFT JOIN이므로 NULL 허용 — Java에서 nullCheck 처리 확인 |
| 4 | BARCODE_TYPE NULL로 H5 필터 결과 0건 | CO_매출처품목코드매핑에 해당 품목 M1/M2 둘 다 없음 | MSSQL DB에서 홈플러스 비정량 품목 바코드 매핑 데이터 존재 여부 확인 |
| 5 | GI_REQ_PKG FLOAT → Int 파싱 오류 | MSSQL에서 박스수량이 FLOAT으로 반환 | 기존 `(int) Double.parseDouble(temp[5])` 처리 유지 (이미 개발30에서 수정됨) |
| 6 | 월품목별재고화일_LOT별_VIEW V LEFT JOIN 결과 NULL | 해당 VIEW가 MSSQL에 없거나 해당 LOT 데이터 없음 | LEFT JOIN이므로 BL_NO, IMPORT_ID_NO NULL 허용 — 홈플러스 정량(개발47) 동작 확인 후 동일 패턴 적용 |
| 7 | PACKER_CODE 빈값 | I.패커코드가 CO_품목코드에 미등록 | Oracle 하드코딩 'IN67677'에서 동적으로 변경됨 — 실제 DB 데이터 확인 필요 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | search_homeplus_nonfixed.jsp MSSQL 직접 JOIN (29개 컬럼) | ✅ 완료 |
| 2 | 통합 테스트 | ⏳ 대기 |

---

**관련 문서**:
- `app/doc/소스분석/56_홈플러스정량_출하_전체흐름분석.md` — 홈플러스 정량 전체 흐름 (동일 패턴)
- `app/doc/column/04_VW_PDA_WID_LIST_NONFIXED_HP.md` — 컬럼 사용 분석
- `app/doc/개발/47_홈플러스정량_출하대상받기_JSP_MSSQL전환.md` — 홈플러스 정량 MSSQL 전환 (CO_거래처MASTER CENTERNAME JOIN 패턴)
- `app/doc/개발/49_롯데_출하대상받기_JSP_MSSQL전환.md` — 동일 패턴 Oracle VIEW → MSSQL 전환 참고
- JSP 현재: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_homeplus_nonfixed.jsp`
- JSP 참조 (홈플러스 정량): `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp`
- JSP 참조 (비정량 이마트): `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_production_nonfixed.jsp`
- Java: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`

**문서 버전**: 1.0
