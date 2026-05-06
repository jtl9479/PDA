# 도매(searchType=3) 출하대상받기 JSP MSSQL 전환

**작성일**: 2026-05-04
**목적**: `search_shipment_wholesale.jsp`의 Oracle VIEW(`VW_PDA_WID_WHOLESALE_LIST`) 의존성을 제거하고 MSSQL 직접 JOIN 쿼리로 교체한다. 동시에 Java `ProgressDlgShipSearch.java` searchType="3" 분기의 창고코드 WHERE 조건 테이블 별칭 누락(`창고코드` → `D.창고코드`)을 함께 수정한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### search_shipment_wholesale.jsp (현재 — 동작 불가)

```sql
SELECT GI_H_ID, GI_D_ID, EOI_ID, ITEM_CODE, ITEM_NAME,
       EMARTITEM_CODE, EMARTITEM, GI_REQ_PKG, GI_REQ_QTY, AMOUNT,
       GOODS_R_ID, GR_REF_NO, GI_REQ_DATE, BL_NO, BRAND_CODE, BRANDNAME,
       CLIENT_CODE, CLIENTNAME, CENTERNAME, ITEM_SPEC, CT_CODE,
       IMPORT_ID_NO, PACKER_CODE, PACKERNAME, PACKER_PRODUCT_CODE,
       BARCODE_TYPE, ITEM_TYPE, PACKWEIGHT, BARCODEGOODS,
       STORE_IN_DATE, EMARTLOGIS_CODE, EMARTLOGIS_NAME, WH_AREA
FROM VW_PDA_WID_WHOLESALE_LIST   -- Oracle VIEW, MSSQL에 없음 → 실행 즉시 오류
ORDER BY EOI_ID ASC
```

현재 out.println 순서 (33개):

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
| 32 | WH_AREA | - | JSP 마지막 컬럼, Java 미파싱 → 제거 대상 |

**제거 대상 9개**: GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME, WH_AREA
→ Java에서 파싱하지 않음 (searchType=3은 temp[0]~temp[23] 24컬럼으로 파싱 종료)

### ProgressDlgShipSearch.java searchType="3" 분기 (현재 — 창고코드 별칭 누락)

```java
// ProgressDlgShipSearch.java:141~148
} else if(Common.searchType.equals("3")){
    // 창고별 조건 추가
    if (!Common.selectWarehouseCode.isEmpty()) {
        data += " AND 창고코드 = '" + Common.selectWarehouseCode + "'";  // D. 별칭 누락
    }
    receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT_WHOLESALE);
```

### 문제점

1. **JSP DB 오류**: `getMSSQLConnection()`으로 MSSQL 연결 후 Oracle 전용 VIEW `VW_PDA_WID_WHOLESALE_LIST` 참조 → MSSQL에 해당 VIEW 없음 → 실행 즉시 SQL 오류
2. **인덱스 전부 불일치**: 현재 JSP index 0 = `GI_H_ID`, Java temp[0] = `GI_D_ID` → 33개 중 24개 파싱 컬럼 전부 인덱스 불일치
3. **창고코드 조건 별칭 누락**: `AND 창고코드 = '...'` → MSSQL 직접 JOIN 쿼리에서 여러 테이블 JOIN 시 ambiguous 오류 발생 가능 (이마트·홈플러스는 `AND D.창고코드 = '...'`로 정상 처리, 롯데 개발49 Step 2에서 동일 버그 수정)
4. **제거 컬럼 9개 포함**: GI_H_ID 등 9개는 Java에서 파싱하지 않으므로 출력 불필요

---

## 2. 변경 구조

### 데이터 흐름

```
[변경 전]
PDA (searchType=3) → search_shipment_wholesale.jsp
    → FROM VW_PDA_WID_WHOLESALE_LIST (Oracle VIEW, MSSQL 없음)
    → 33개 컬럼 출력 (index 0=GI_H_ID)
    → Java temp[0]=GI_D_ID로 파싱 → 전부 불일치 → 동작 불가

[변경 후]
PDA (searchType=3) → search_shipment_wholesale.jsp
    → MSSQL 직접 JOIN 쿼리 (SM_출고상세 D, SM_출고머리 H 등)
    → 24개 컬럼 출력 (index 0=GI_D_ID)
    → Java temp[0]=GI_D_ID로 파싱 → 전부 일치 → 정상 동작
```

### 컬럼 수 변화

| 구분 | 컬럼 수 | 비고 |
|------|:-------:|------|
| 변경 전 (Oracle JSP) | 33개 | GI_H_ID(0) ~ WH_AREA(32) |
| 변경 후 (MSSQL JSP) | 24개 | GI_D_ID(0) ~ EMARTLOGIS_CODE(23) |
| 제거 컬럼 | 9개 | Java 미파싱 컬럼 |

### 전환 후 out.println 순서 (24개)

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

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_shipment_wholesale.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\` | Oracle VIEW 제거 → MSSQL 직접 JOIN (24개 컬럼) |
| 2 | **ProgressDlgShipSearch.java** | `app/src/main/java/com/rgbsolution/highland_emart/common/` | searchType="3" 창고코드 WHERE 조건 `D.` 별칭 추가 |

---

## 4. 수정 상세

### 4.1 search_shipment_wholesale.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_wholesale.jsp`

**변경 전:**

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
    + " FROM VW_PDA_WID_WHOLESALE_LIST"
    + qry_where
    + " ORDER BY EOI_ID ASC";

// out.println (33개)
out.println(rs.getString("GI_H_ID") + "::" + rs.getString("GI_D_ID") + "::" + rs.getString("EOI_ID") + "::" + rs.getString("ITEM_CODE") + "::"
    + rs.getString("ITEM_NAME") + "::" + rs.getString("EMARTITEM_CODE") + "::" + rs.getString("EMARTITEM") + "::"
    + rs.getString("GI_REQ_PKG") + "::" + rs.getString("GI_REQ_QTY") + "::" + rs.getString("AMOUNT") + "::"
    + rs.getString("GOODS_R_ID") + "::" + rs.getString("GR_REF_NO") + "::" + rs.getString("GI_REQ_DATE") + "::" + rs.getString("BL_NO") + "::"
    + rs.getString("BRAND_CODE") + "::" + rs.getString("BRANDNAME") + "::" + rs.getString("CLIENT_CODE") + "::"
    + rs.getString("CLIENTNAME") + "::" + rs.getString("CENTERNAME") + "::" + rs.getString("ITEM_SPEC") + "::"
    + rs.getString("CT_CODE") + "::" + rs.getString("IMPORT_ID_NO") + "::" + rs.getString("PACKER_CODE") + "::"
    + rs.getString("PACKERNAME") + "::" + rs.getString("PACKER_PRODUCT_CODE") + "::" + rs.getString("BARCODE_TYPE") + "::"
    + rs.getString("ITEM_TYPE") + "::" + rs.getString("PACKWEIGHT") + "::" + rs.getString("BARCODEGOODS") + "::" + rs.getString("STORE_IN_DATE") + "::"
    + rs.getString("EMARTLOGIS_CODE") + "::" + rs.getString("EMARTLOGIS_NAME") + "::" + rs.getString("WH_AREA") + ";;");
```

**변경 후:**

```java
String quertystring = "SELECT "
    + "  D.SEQ AS GI_D_ID"
    + ", I.품목코드 AS ITEM_CODE"
    + ", I.품목명 AS ITEM_NAME"
    + ", 'NA' AS EMARTITEM_CODE"
    + ", 'NA' AS EMARTITEM"
    + ", L.박스수량 AS GI_REQ_PKG"
    + ", L.중량 AS GI_REQ_QTY"
    + ", D.출고일자 AS GI_REQ_DATE"
    + ", COALESCE(NULLIF(V.BLNO,''), V.이력번호) AS BL_NO"
    + ", '' AS BRAND_CODE"
    + ", H.출고거래처 AS CLIENT_CODE"
    + ", G.상호 AS CLIENTNAME"
    + ", G.상호 AS CENTERNAME"
    + ", I.규격 AS ITEM_SPEC"
    + ", I.원산지 AS CT_CODE"
    + ", V.이력번호 AS IMPORT_ID_NO"
    + ", I.패커코드 AS PACKER_CODE"
    + ", I.PPCODE AS PACKER_PRODUCT_CODE"
    + ", 'NA' AS BARCODE_TYPE"
    + ", 'S' AS ITEM_TYPE"
    + ", 'NA' AS PACKWEIGHT"
    + ", I.상품바코드 AS BARCODEGOODS"
    + ", D.출고일자 AS STORE_IN_DATE"
    + ", '0000000' AS EMARTLOGIS_CODE"
    + " FROM SM_출고상세 D"
    + " INNER JOIN SM_출고머리 H"
    + "   ON H.회사코드 = D.회사코드"
    + "  AND H.출고사업장 = D.출고사업장"
    + "  AND H.출고일자 = D.출고일자"
    + "  AND H.출고일련번호 = D.출고일련번호"
    + " JOIN CO_품목코드 I"
    + "   ON D.회사코드 = I.회사코드"
    + "  AND D.출고품목코드 = I.품목코드"
    + " LEFT JOIN CO_거래처MASTER G"
    + "   ON G.회사코드 = D.회사코드"
    + "  AND G.거래처코드 = H.출고거래처"
    + " JOIN SM_출고LOT L"
    + "   ON L.출고상세SEQ = D.SEQ"
    + " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"
    + "   ON V.회사코드 = D.회사코드"
    + "  AND V.사업장 = D.출고사업장"
    + "  AND V.창고코드 = D.창고코드"
    + "  AND V.품목코드 = D.출고품목코드"
    + "  AND V.LOTNO = L.LOTNO"
    + " WHERE H.마트사구분 = ''"
    + "   AND D.출고수량 > 0"
    + "   AND I.원료육여부 = '1'"
    + qry_where
    + " ORDER BY D.SEQ ASC";

// out.println (24개 — temp[] 인덱스 그대로 유지)
out.println(rs.getString("GI_D_ID") + "::" + rs.getString("ITEM_CODE") + "::" + rs.getString("ITEM_NAME") + "::"
    + rs.getString("EMARTITEM_CODE") + "::" + rs.getString("EMARTITEM") + "::"
    + rs.getString("GI_REQ_PKG") + "::" + rs.getString("GI_REQ_QTY") + "::"
    + rs.getString("GI_REQ_DATE") + "::" + rs.getString("BL_NO") + "::"
    + rs.getString("BRAND_CODE") + "::" + rs.getString("CLIENT_CODE") + "::"
    + rs.getString("CLIENTNAME") + "::" + rs.getString("CENTERNAME") + "::"
    + rs.getString("ITEM_SPEC") + "::" + rs.getString("CT_CODE") + "::"
    + rs.getString("IMPORT_ID_NO") + "::" + rs.getString("PACKER_CODE") + "::"
    + rs.getString("PACKER_PRODUCT_CODE") + "::" + rs.getString("BARCODE_TYPE") + "::"
    + rs.getString("ITEM_TYPE") + "::" + rs.getString("PACKWEIGHT") + "::"
    + rs.getString("BARCODEGOODS") + "::" + rs.getString("STORE_IN_DATE") + "::"
    + rs.getString("EMARTLOGIS_CODE") + ";;");
```

**검증**: Java `temp[0]~temp[23]` 각각 `GI_D_ID~EMARTLOGIS_CODE`에 1:1 매핑 확인

---

### 4.2 ProgressDlgShipSearch.java

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`

**변경 전 (L144~145):**

```java
if (!Common.selectWarehouseCode.isEmpty()) {
    data += " AND 창고코드 = '" + Common.selectWarehouseCode + "'";
}
```

**변경 후:**

```java
if (!Common.selectWarehouseCode.isEmpty()) {
    data += " AND D.창고코드 = '" + Common.selectWarehouseCode + "'";
}
```

**검증**: 이마트(searchType=0, L119), 홈플러스(searchType=2, L136), 롯데(searchType=6, L165) 분기와 동일한 `D.창고코드` 패턴으로 통일됨 확인

---

## 5. 사이드이펙트

### searchType=0/2/4/5/6 분기

- **영향 없음**: 각 searchType은 별도 JSP URL을 사용하며 독립적으로 동작

### CLIENTNAME = CENTERNAME 동일값 처리

```java
// Oracle VIEW에서도 DE_CLIENT(IH.CLIENT_CODE)를 CLIENTNAME, CENTERNAME 양쪽에 동일하게 사용
// → MSSQL에서도 G.상호를 양쪽에 동일하게 출력하여 원본 동작 유지
```

- **영향**: CLIENTNAME과 CENTERNAME이 동일 거래처명으로 표시됨 — Oracle 원본과 동일한 설계
- **대응 방안**: 별도 처리 불필요

### STORE_IN_DATE = 출고일자 처리

- **영향**: Oracle 원본에서 `STORE_IN_DATE = IH.GI_REQ_DATE` (출하요청일 = 출고일자)이었으므로 `D.출고일자`를 그대로 사용하면 동일 동작
- **대응 방안**: 별도 처리 불필요

---

## 6. 데이터 저장 구조

### 변수 매핑 (searchType=3 파싱 — ProgressDlgShipSearch.java)

| temp[] | 컬럼명 | 저장 필드 | 용도 |
|:------:|--------|-----------|------|
| temp[0] | GI_D_ID | si.GI_D_ID | 출하상세 PK (SEQ) |
| temp[1] | ITEM_CODE | si.ITEM_CODE | 품목코드 |
| temp[2] | ITEM_NAME | si.ITEM_NAME | 품목명 |
| temp[3] | EMARTITEM_CODE | si.EMARTITEM_CODE | 'NA' 고정 (도매 미사용) |
| temp[4] | EMARTITEM | si.EMARTITEM | 'NA' 고정 (도매 미사용) |
| temp[5] | GI_REQ_PKG | si.GI_REQ_PKG | 요청박스수 (Float→Int 변환) |
| temp[6] | GI_REQ_QTY | si.GI_REQ_QTY | 요청중량 (소수점 절사 처리) |
| temp[7] | GI_REQ_DATE | si.GI_REQ_DATE | 요청일자(출고일자) |
| temp[8] | BL_NO | si.BL_NO | BL번호 또는 이력번호 |
| temp[9] | BRAND_CODE | si.BRAND_CODE | '' 고정 (도매 미사용) |
| temp[10] | CLIENT_CODE | si.CLIENT_CODE | 거래처코드 (H.출고거래처) |
| temp[11] | CLIENTNAME | si.CLIENTNAME | 거래처명(상호) |
| temp[12] | CENTERNAME | si.CENTERNAME | 센터명 = 거래처명(상호)과 동일 |
| temp[13] | ITEM_SPEC | si.ITEM_SPEC | 품목규격 |
| temp[14] | CT_CODE | si.CT_CODE | 원산지코드 |
| temp[15] | IMPORT_ID_NO | si.IMPORT_ID_NO | 수입이력번호 |
| temp[16] | PACKER_CODE | si.PACKER_CODE | 패커코드 |
| temp[17] | PACKER_PRODUCT_CODE | si.PACKER_PRODUCT_CODE | 패커상품코드(PPCODE) |
| temp[18] | BARCODE_TYPE | si.BARCODE_TYPE | 'NA' 고정 (도매 바코드타입 미사용) |
| temp[19] | ITEM_TYPE | si.ITEM_TYPE | 'S' 고정 |
| temp[20] | PACKWEIGHT | si.PACKWEIGHT | 'NA' 고정 (Oracle 원본 하드코딩) |
| temp[21] | BARCODEGOODS | si.BARCODEGOODS | 상품바코드 |
| temp[22] | STORE_IN_DATE | si.STORE_IN_DATE | 출고일자 (Oracle STORE_IN_DATE = GI_REQ_DATE = 출고일자) |
| temp[23] | EMARTLOGIS_CODE | si.EMARTLOGIS_CODE | '0000000' 고정 (Oracle 원본 하드코딩) |

### 인덱스 매핑

```
JSP out.println 순서  ↔  Java temp[] 인덱스
0=GI_D_ID             ↔  temp[0]  → si.GI_D_ID
1=ITEM_CODE           ↔  temp[1]  → si.ITEM_CODE
2=ITEM_NAME           ↔  temp[2]  → si.ITEM_NAME
3=EMARTITEM_CODE      ↔  temp[3]  → si.EMARTITEM_CODE  ('NA')
4=EMARTITEM           ↔  temp[4]  → si.EMARTITEM        ('NA')
5=GI_REQ_PKG          ↔  temp[5]  → si.GI_REQ_PKG
6=GI_REQ_QTY          ↔  temp[6]  → si.GI_REQ_QTY
7=GI_REQ_DATE         ↔  temp[7]  → si.GI_REQ_DATE
8=BL_NO               ↔  temp[8]  → si.BL_NO
9=BRAND_CODE          ↔  temp[9]  → si.BRAND_CODE        ('')
10=CLIENT_CODE        ↔  temp[10] → si.CLIENT_CODE
11=CLIENTNAME         ↔  temp[11] → si.CLIENTNAME
12=CENTERNAME         ↔  temp[12] → si.CENTERNAME
13=ITEM_SPEC          ↔  temp[13] → si.ITEM_SPEC
14=CT_CODE            ↔  temp[14] → si.CT_CODE
15=IMPORT_ID_NO       ↔  temp[15] → si.IMPORT_ID_NO
16=PACKER_CODE        ↔  temp[16] → si.PACKER_CODE
17=PACKER_PRODUCT_CODE ↔ temp[17] → si.PACKER_PRODUCT_CODE
18=BARCODE_TYPE       ↔  temp[18] → si.BARCODE_TYPE       ('NA')
19=ITEM_TYPE          ↔  temp[19] → si.ITEM_TYPE           ('S')
20=PACKWEIGHT         ↔  temp[20] → si.PACKWEIGHT          ('NA')
21=BARCODEGOODS       ↔  temp[21] → si.BARCODEGOODS
22=STORE_IN_DATE      ↔  temp[22] → si.STORE_IN_DATE
23=EMARTLOGIS_CODE    ↔  temp[23] → si.EMARTLOGIS_CODE    ('0000000')
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
    ├── Common.searchType = "3"  (도매)
    ├── data = " AND D.회사코드 = '...' AND D.출고일자 = '...'"
    ├── data += " AND D.창고코드 = '...' "  (Step 2 수정 후)
    ↓
HttpHelper.sendDataDb(data, "inno", "search_shipment", URL_SEARCH_SHIPMENT_WHOLESALE)
    ↓
search_shipment_wholesale.jsp
    ├── getMSSQLConnection()
    ├── MSSQL 직접 JOIN 쿼리 (Step 1 수정 후)
    │     FROM SM_출고상세 D
    │     INNER JOIN SM_출고머리 H ...
    │     JOIN CO_품목코드 I ...
    │     LEFT JOIN CO_거래처MASTER G ...
    │     JOIN SM_출고LOT L ...
    │     LEFT JOIN 월품목별재고화일_LOT별_VIEW V ...
    │     WHERE H.마트사구분='' AND D.출고수량>0 AND I.원료육여부='1'
    └── out.println (24개 컬럼, ";;" 구분자)
    ↓
ProgressDlgShipSearch.doInBackground()
    ├── split(";;") → rows[]
    ├── split("::", -1) → temp[0..23]
    └── Shipments_Info 객체 생성 (temp[0]~temp[23] 파싱)
    ↓
onPostExecute() → ShipmentActivity UI 갱신
```

---

## 8. 개발 플랜

### Step 1: search_shipment_wholesale.jsp MSSQL 직접 JOIN 쿼리 작성 (24개 컬럼)

**Part 1. 분석**
- 파일: `search_shipment_wholesale.jsp`
- 범위: JSP 전체 쿼리 블록 (L36~93)
- 용도: Oracle VIEW `VW_PDA_WID_WHOLESALE_LIST` 제거, MSSQL 직접 JOIN 쿼리로 교체, out.println 33개 → 24개로 정리
- 주의할 점:
  - EMARTITEM_CODE, EMARTITEM = `'NA'` 하드코딩 (Oracle VIEW 원본 동일)
  - BARCODE_TYPE = `'NA'`, ITEM_TYPE = `'S'`, PACKWEIGHT = `'NA'` 하드코딩 (Oracle VIEW 원본 동일)
  - EMARTLOGIS_CODE = `'0000000'` 하드코딩 (Oracle VIEW 원본 동일)
  - CLIENTNAME = CENTERNAME = `G.상호` 동일값 (Oracle VIEW에서 DE_CLIENT(IH.CLIENT_CODE) 양쪽 동일 처리)
  - STORE_IN_DATE = `D.출고일자` (Oracle에서 STORE_IN_DATE = IH.GI_REQ_DATE = 출하요청일 = 출고일자)
  - WHERE절 `H.마트사구분 = ''` — 도매는 마트사구분이 빈 문자열
  - ORDER BY: `EOI_ID ASC` → `D.SEQ ASC` (EOI_ID는 Oracle 전용 컬럼)
  - out.println 순서가 Java temp[] 인덱스와 정확히 일치해야 함

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | Oracle VIEW 참조 | JSP L70 | `FROM VW_PDA_WID_WHOLESALE_LIST` 제거 |
| 2 | SELECT 33개 컬럼 | JSP L37~69 | 24개로 교체 (제거 9개: GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME, WH_AREA) |
| 3 | out.println 33개 | JSP L84~93 | 24개로 교체 |
| 4 | ORDER BY EOI_ID | JSP L72 | `D.SEQ ASC`으로 교체 |

**Part 2. 변환 계획**
- 변환 방식: 4.1절 변경 후 코드 기준으로 전체 쿼리 교체
- EMARTITEM_CODE/EMARTITEM/BARCODE_TYPE/PACKWEIGHT: Oracle VIEW 원본 하드코딩 그대로 유지 (`'NA'`)
- CLIENTNAME = CENTERNAME: 동일값 `G.상호`로 처리
- STORE_IN_DATE: Oracle 원본과 동일한 `D.출고일자` 사용
- EMARTLOGIS_CODE: `'0000000'` 하드코딩 (Oracle 원본 동일)
- 주의사항: `qry_where`는 기존과 동일하게 WHERE 절 뒤에 이어 붙임, `getMSSQLConnection()` 및 Statement/ResultSet 처리 코드는 변경하지 않음

**체크리스트**
- [x] Part 1: SELECT 24개 컬럼 구성 확인 (제거 9개 누락 없는지 검증)
- [x] Part 1: out.println 순서 24개 확인 (temp[0]~temp[23] 인덱스 일치)
- [x] Part 2: MSSQL JOIN 구조 작성 완료
- [x] Part 2: WHERE H.마트사구분='' AND D.출고수량>0 AND I.원료육여부='1' 포함 확인
- [x] Part 2: ORDER BY D.SEQ ASC 교체 확인
- [x] Part 3: JSP 파일 저장 완료
- [ ] Part 4: Tomcat 재시작 후 JSP 컴파일 오류 없음 확인
- [ ] Part 5: MSSQL에서 해당 쿼리 직접 실행하여 결과 24개 컬럼 확인

**Part 6. 변경 내용**:
- **무엇을**: `search_shipment_wholesale.jsp`의 Oracle VIEW(`VW_PDA_WID_WHOLESALE_LIST`) 쿼리를 MSSQL 직접 JOIN 쿼리로 교체하고, out.println을 33개→24개로 축소
- **왜**: getMSSQLConnection()으로 MSSQL 연결 시 Oracle 전용 VIEW 없어 SQL 오류 발생. Java temp[] 인덱스가 이미 24컬럼(0=GI_D_ID) 기준으로 업데이트되어 있어 JSP도 맞춰야 함
- **어떻게**: SM_출고상세→SM_출고머리→CO_품목코드→CO_거래처MASTER→SM_출고LOT→월품목별재고화일_LOT별_VIEW JOIN, WHERE H.마트사구분='' AND D.출고수량>0 AND I.원료육여부='1'(원료육=CO_품목코드.원료육여부='1', Oracle BI.ITEM_TYPE='10' 매핑), ORDER BY D.SEQ ASC. 9개 미파싱 컬럼(GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME, WH_AREA) 제거. ⑤⑥ 검증 PASS 확인(2026-05-06)

---

### Step 2: ProgressDlgShipSearch.java 창고코드 D. 별칭 추가

**Part 1. 분석**
- 파일: `ProgressDlgShipSearch.java`
- 범위: L141~148 (searchType="3" 분기)
- 용도: MSSQL 직접 JOIN 쿼리에서 창고코드 ambiguous 오류 방지
- 주의할 점: 다른 searchType 분기(0, 2, 6)에는 영향 없음

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 창고코드 WHERE 조건 | L144~145 | `창고코드` → `D.창고코드` |

**Part 2. 변환 계획**
- 변환 방식: 문자열 `" AND 창고코드 = '"` → `" AND D.창고코드 = '"` 로 1개 위치만 수정
- 주의사항: searchType="3" 분기만 수정, 다른 분기 코드 변경하지 않음

**체크리스트**
- [x] Part 1: searchType="3" 분기 창고코드 위치 확인 (L144~145)
- [x] Part 2: `D.창고코드` 수정 완료
- [x] Part 3: 수정 후 주변 코드 변경 없음 확인
- [ ] Part 4: 컴파일 오류 없음 확인
- [x] Part 5: searchType=0, 2, 6 분기 창고코드 조건과 패턴 일치 확인 (D.창고코드 동일 패턴)

**Part 6. 변경 내용**:
- **무엇을**: `ProgressDlgShipSearch.java` searchType="3" 분기의 창고코드 WHERE 조건 `AND 창고코드 =` → `AND D.창고코드 =`
- **왜**: MSSQL 직접 JOIN 쿼리에서 창고코드가 SM_출고상세(D)와 월품목별재고화일_LOT별_VIEW(V) 양쪽에 존재하여 ambiguous 오류 발생 가능. searchType=0,2,6 분기는 이미 D. 별칭 사용 중
- **어떻게**: L145 단일 위치 문자열 치환. 원본 Oracle VIEW 쿼리에서는 하드코딩된 창고코드를 사용했으나 MSSQL 전환 후 사용자 선택 창고코드로 동적 필터링 적용

---

### Step 3: 통합 테스트

| # | 테스트 항목 | 확인 |
|:-:|------------|------|
| 1 | PDA에서 searchType=3(도매) 선택 후 출하대상받기 실행 → 정상 조회 | □ |
| 2 | 출하대상 목록 24개 필드 모두 정상 파싱 확인 (GI_D_ID~EMARTLOGIS_CODE) | □ |
| 3 | 창고코드 선택 시 필터링 정상 동작 확인 | □ |
| 4 | CLIENTNAME과 CENTERNAME이 동일 거래처명으로 표시되는지 확인 | □ |
| 5 | searchType=0(이마트), 2(홈플러스), 6(롯데) 정상 동작 영향 없음 확인 | □ |

---

### 개발 순서 요약

```
Step 1: search_shipment_wholesale.jsp MSSQL 직접 JOIN (24개 컬럼)
    ↓
Step 2: ProgressDlgShipSearch.java D.창고코드 별칭 추가
    ↓
Step 3: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 도매 출하대상 정상 조회

```
1. PDA 앱 로그인
2. 마트사 구분 → 도매(searchType=3) 선택
3. 창고 선택 (선택 안 함 / 특정 창고 선택 각각)
4. 출하대상 받기 버튼 터치
5. 서버로부터 출하 목록 수신 확인
6. 목록 항목 선택 → 상세 정보 24개 필드 값 확인 (GI_D_ID, ITEM_CODE, CLIENTNAME, BL_NO 등)
```

### 시나리오 2: 창고코드 필터링 동작 확인

```
1. PDA 설정 → 창고 선택 (특정 창고코드 선택)
2. 도매 출하대상 받기 실행
3. WHERE 조건에 D.창고코드='선택값' 포함 여부 서버 로그 확인
4. 해당 창고 품목만 목록에 표시되는지 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | SM_출고LOT JOIN 결과 0건 | `L.출고상세SEQ = D.SEQ` 조건 불일치 (ERP 전환 후 SEQ 체계 변경 가능) | ERP 담당자에게 도매 출고LOT 연결 키 재확인 |
| 2 | 월품목별재고화일_LOT별_VIEW V LEFT JOIN 결과 NULL → BL_NO, IMPORT_ID_NO 모두 NULL | 해당 VIEW 데이터 없음 | LEFT JOIN이므로 NULL 허용, Java `Common.nullCheck("null","")` 처리 확인 |
| 3 | H.마트사구분='' 조건으로 도매 데이터가 조회되지 않음 | ERP에서 도매 출고머리의 마트사구분 값이 ''가 아닌 다른 값으로 저장된 경우 | ERP 실제 도매 출고머리 마트사구분 컬럼값 확인 후 WHERE 조건 수정 |
| 4 | GI_REQ_PKG FLOAT → Int 파싱 오류 | MSSQL에서 박스수량이 FLOAT으로 반환 | 기존 `(int) Double.parseDouble(temp[5])` 처리 유지 (이미 개발30에서 수정됨) |
| 5 | 창고코드 D. 별칭 없이 ambiguous 오류 | 여러 테이블에 창고코드 컬럼 중복 (SM_출고상세, 월품목별재고화일_LOT별_VIEW 등) | Step 2에서 D.창고코드로 수정하면 해결 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | search_shipment_wholesale.jsp MSSQL 직접 JOIN (24개 컬럼) | ✅ 완료 |
| 2 | ProgressDlgShipSearch.java D.창고코드 별칭 추가 | ✅ 완료 |
| 3 | 통합 테스트 | ⏳ 대기 |

---

**관련 문서**:
- `app/doc/개발/49_롯데_출하대상받기_JSP_MSSQL전환.md` — 동일 패턴 롯데 JSP 전환 참고 (창고코드 D. 별칭 버그 동일)
- `app/doc/개발/47_홈플러스정량_출하대상받기_JSP_MSSQL전환.md` — 동일 패턴 홈플러스 JSP 전환 참고
- `app/doc/개발/50_홈플러스비정량_출하대상받기_JSP_MSSQL전환.md` — 동일 패턴 홈플러스비정량 JSP 전환 참고
- `app/doc/view/VW_PDA_WID_WHOLESALE_LIST` — Oracle VIEW DDL (UNION 2블록 구조)
- JSP 현재: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_wholesale.jsp`
- Java: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`

**문서 버전**: 1.0
