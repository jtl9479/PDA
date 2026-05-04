# 롯데(searchType=6) 출하대상받기 JSP MSSQL 전환

**작성일**: 2026-04-30
**목적**: `search_shipment_lotte.jsp`의 Oracle VIEW(`VW_PDA_WID_LIST_LOTTE`) 의존성을 제거하고 MSSQL 직접 JOIN 쿼리로 교체한다. 동시에 Java `ProgressDlgShipSearch.java` searchType="6" 분기의 창고코드 WHERE 조건 테이블 별칭 누락을 함께 수정한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### search_shipment_lotte.jsp (현재 — 동작 불가)

```sql
SELECT GI_H_ID, GI_D_ID, EOI_ID, ITEM_CODE, ITEM_NAME,
       EMARTITEM_CODE, EMARTITEM, GI_REQ_PKG, GI_REQ_QTY, AMOUNT,
       GOODS_R_ID, GR_REF_NO, GI_REQ_DATE, BL_NO, BRAND_CODE, BRANDNAME,
       CLIENT_CODE, CLIENTNAME, CENTERNAME, ITEM_SPEC, CT_CODE,
       IMPORT_ID_NO, PACKER_CODE, PACKERNAME, PACKER_PRODUCT_CODE,
       BARCODE_TYPE, ITEM_TYPE, PACKWEIGHT, BARCODEGOODS,
       STORE_IN_DATE, EMARTLOGIS_CODE, EMARTLOGIS_NAME,
       WH_AREA, LAST_BOX_ORDER
FROM VW_PDA_WID_LIST_LOTTE   -- Oracle VIEW, MSSQL에 없음 → 실행 즉시 오류
ORDER BY EOI_ID ASC
```

현재 out.println 순서 (34개):

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
| 30 | EMARTLOGIS_CODE | temp[23] | 롯데 전용 업체코드 |
| 31 | EMARTLOGIS_NAME | - | Java 미파싱 → 제거 대상 |
| 32 | WH_AREA | temp[24] | |
| 33 | LAST_BOX_ORDER | temp[25] | |

**제거 대상 8개**: GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME
→ Oracle VIEW에도 없고 Java에서도 파싱하지 않음

### ProgressDlgShipSearch.java searchType="6" 분기 (현재 — 창고코드 별칭 누락)

```java
// ProgressDlgShipSearch.java:162~170
} else if(Common.searchType.equals("6")) {
    if (!Common.selectWarehouseCode.isEmpty()) {
        data += " AND 창고코드 = '" + Common.selectWarehouseCode + "'";  // D. 별칭 누락
    }
    receiveData = HttpHelper.getInstance().sendDataDb(
        data, "highland", "search_shipment", Common.URL_SEARCH_SHIPMENT_LOTTE);
}
```

### 문제점

1. **JSP DB 오류**: `getMSSQLConnection()`으로 MSSQL 연결 후 Oracle 전용 VIEW `VW_PDA_WID_LIST_LOTTE` 참조 → MSSQL에 해당 VIEW 없음 → 실행 즉시 SQL 오류
2. **인덱스 전부 불일치**: 현재 JSP index 0 = `GI_H_ID`, Java temp[0] = `GI_D_ID` → 34개 중 26개 파싱 컬럼 전부 인덱스 불일치
3. **창고코드 조건 별칭 누락**: `AND 창고코드 = '...'` → MSSQL 직접 JOIN 쿼리에서 여러 테이블 JOIN 시 ambiguous 오류 발생 가능 (이마트는 `AND D.창고코드 = '...'`로 정상 처리)
4. **제거 컬럼 8개 포함**: GI_H_ID 등 8개는 Java에서 파싱하지 않으므로 출력 불필요
5. **EMARTLOGIS_CODE 매핑 확정**: Oracle에서 `B_COMMON_CODE.MASTER_CODE LIKE 'LOTTE_STORE_CODE'` 서브쿼리로 조회하던 값 → MSSQL에서 `COALESCE(M1.물류코드, M2.물류코드)` 로 처리 (이마트 search_shipment.jsp L60에서 동일 패턴 확인)

---

## 2. 변경 구조

### 데이터 흐름

```
[변경 전]
PDA (searchType=6) → search_shipment_lotte.jsp
    → FROM VW_PDA_WID_LIST_LOTTE (Oracle VIEW, MSSQL 없음)
    → 34개 컬럼 출력 (index 0=GI_H_ID)
    → Java temp[0]=GI_D_ID로 파싱 → 전부 불일치 → 동작 불가

[변경 후]
PDA (searchType=6) → search_shipment_lotte.jsp
    → MSSQL 직접 JOIN 쿼리 (SM_출고상세 D, SM_마트사발주롯데마트 LE 등)
    → 26개 컬럼 출력 (index 0=GI_D_ID)
    → Java temp[0]=GI_D_ID로 파싱 → 전부 일치 → 정상 동작
```

### 컬럼 수 변화

| 구분 | 컬럼 수 | 비고 |
|------|:-------:|------|
| 변경 전 (Oracle JSP) | 34개 | GI_H_ID(0) ~ LAST_BOX_ORDER(33) |
| 변경 후 (MSSQL JSP) | 26개 | GI_D_ID(0) ~ LAST_BOX_ORDER(25) |
| 제거 컬럼 | 8개 | Java 미파싱 컬럼 |

### 전환 후 out.println 순서 (26개)

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
| 25 | LAST_BOX_ORDER | temp[25] |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_shipment_lotte.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\` | Oracle VIEW 제거 → MSSQL 직접 JOIN (26개 컬럼) |
| 2 | **ProgressDlgShipSearch.java** | `app/src/main/java/com/rgbsolution/highland_emart/common/` | searchType="6" 창고코드 WHERE 조건 `D.` 별칭 추가 |

---

## 4. 수정 상세

### 4.1 search_shipment_lotte.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_lotte.jsp`

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
    + ", LAST_BOX_ORDER"
    + " FROM VW_PDA_WID_LIST_LOTTE"
    + qry_where
    + " ORDER BY EOI_ID ASC";

// out.println (34개)
out.println(rs.getString("GI_H_ID") + "::" + rs.getString("GI_D_ID") + "::" + rs.getString("EOI_ID") + "::" + rs.getString("ITEM_CODE") + "::"
    + rs.getString("ITEM_NAME") + "::" + rs.getString("EMARTITEM_CODE") + "::" + rs.getString("EMARTITEM") + "::"
    + rs.getString("GI_REQ_PKG") + "::" + rs.getString("GI_REQ_QTY") + "::" + rs.getString("AMOUNT") + "::"
    + rs.getString("GOODS_R_ID") + "::" + rs.getString("GR_REF_NO") + "::" + rs.getString("GI_REQ_DATE") + "::" + rs.getString("BL_NO") + "::"
    + rs.getString("BRAND_CODE") + "::" + rs.getString("BRANDNAME") + "::" + rs.getString("CLIENT_CODE") + "::"
    + rs.getString("CLIENTNAME") + "::" + rs.getString("CENTERNAME") + "::" + rs.getString("ITEM_SPEC") + "::"
    + rs.getString("CT_CODE") + "::" + rs.getString("IMPORT_ID_NO") + "::" + rs.getString("PACKER_CODE") + "::"
    + rs.getString("PACKERNAME") + "::" + rs.getString("PACKER_PRODUCT_CODE") + "::" + rs.getString("BARCODE_TYPE") + "::"
    + rs.getString("ITEM_TYPE") + "::" + rs.getString("PACKWEIGHT") + "::" + rs.getString("BARCODEGOODS") + "::" + rs.getString("STORE_IN_DATE") + "::"
    + rs.getString("EMARTLOGIS_CODE") + "::" + rs.getString("EMARTLOGIS_NAME") + "::" + rs.getString("WH_AREA") + "::" + rs.getString("LAST_BOX_ORDER") + ";;");
```

**변경 후:**

```java
String quertystring = "SELECT "
    + "  D.SEQ AS GI_D_ID"
    + ", I.품목코드 AS ITEM_CODE"
    + ", I.품목명 AS ITEM_NAME"
    + ", LE.상품코드 AS EMARTITEM_CODE"
    + ", LE.상품명 AS EMARTITEM"
    + ", L.박스수량 AS GI_REQ_PKG"
    + ", L.중량 AS GI_REQ_QTY"
    + ", D.출고일자 AS GI_REQ_DATE"
    + ", COALESCE(NULLIF(V.BLNO,''), V.이력번호) AS BL_NO"
    + ", '' AS BRAND_CODE"
    + ", H.출고거래처 AS CLIENT_CODE"
    + ", G.상호 AS CLIENTNAME"
    + ", LE.센터명 AS CENTERNAME"
    + ", I.규격 AS ITEM_SPEC"
    + ", I.원산지 AS CT_CODE"
    + ", V.이력번호 AS IMPORT_ID_NO"
    + ", I.패커코드 AS PACKER_CODE"
    + ", I.PPCODE AS PACKER_PRODUCT_CODE"
    + ", COALESCE(M1.바코드타입, M2.바코드타입) AS BARCODE_TYPE"
    + ", 'S' AS ITEM_TYPE"
    + ", NULL AS PACKWEIGHT"
    + ", I.상품바코드 AS BARCODEGOODS"
    + ", SD.납기일자 AS STORE_IN_DATE"
    + ", COALESCE(M1.물류코드, M2.물류코드) AS EMARTLOGIS_CODE"
    + ", '' AS WH_AREA"
    + ", (SELECT TOP 1 W.박스순번"
    + "    FROM SM_출고계근 W"
    + "    WHERE W.출고상세SEQ = D.SEQ"
    + "      AND W.박스순번 IS NOT NULL"
    + "    ORDER BY W.계근ID DESC) AS LAST_BOX_ORDER"
    + " FROM SM_출고상세 D"
    + " INNER JOIN SM_출고머리 H"
    + "   ON H.회사코드=D.회사코드 AND H.출고사업장=D.출고사업장"
    + "   AND H.출고일자=D.출고일자 AND H.출고일련번호=D.출고일련번호"
    + " JOIN CO_품목코드 I ON D.회사코드=I.회사코드 AND D.출고품목코드=I.품목코드"
    + " JOIN SM_마트사발주롯데마트 LE ON D.마트사SEQ=LE.SEQ"
    + " LEFT JOIN CO_거래처MASTER G ON G.회사코드=D.회사코드 AND G.거래처코드=H.출고거래처"
    + " LEFT JOIN CO_매출처품목코드매핑 M1 ON M1.회사코드=D.회사코드 AND M1.품목코드=D.출고품목코드 AND M1.거래처코드=H.출고거래처"
    + " LEFT JOIN CO_거래처MASTER G2 ON G2.회사코드=D.회사코드 AND G2.계층코드=LEFT(G.계층코드,5) AND G2.거래처코드!=H.출고거래처"
    + " LEFT JOIN CO_매출처품목코드매핑 M2 ON M2.회사코드=D.회사코드 AND M2.품목코드=D.출고품목코드 AND M2.거래처코드=G2.거래처코드"
    + " JOIN SM_출고LOT L ON L.출고상세SEQ=D.SEQ"
    + " LEFT JOIN 월품목별재고화일_LOT별_VIEW V ON V.회사코드=D.회사코드 AND V.사업장=D.출고사업장 AND V.창고코드=D.창고코드 AND V.품목코드=D.출고품목코드 AND V.LOTNO=L.LOTNO"
    + " LEFT JOIN SM_수주상세 SD ON SD.마트사SEQ=LE.SEQ"
    + " WHERE H.마트사구분='6'"
    + "   AND D.출고수량>0"
    + "   AND COALESCE(M1.타입구분, M2.타입구분) = 'W'"
    + qry_where
    + " ORDER BY LE.SEQ ASC";

// out.println (26개 — temp[] 인덱스 그대로 유지)
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
    + rs.getString("EMARTLOGIS_CODE") + "::" + rs.getString("WH_AREA") + "::"
    + rs.getString("LAST_BOX_ORDER") + ";;");
```

**검증**: Java `temp[0]~temp[25]` 각각 `GI_D_ID~LAST_BOX_ORDER`에 1:1 매핑 확인

---

### 4.2 ProgressDlgShipSearch.java

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`

**변경 전 (L164~165):**

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

**검증**: 이마트(searchType=0), 홈플러스(searchType=2) 분기와 동일한 `D.창고코드` 패턴으로 통일됨 확인

---

## 5. 사이드이펙트

### LabelPrintHelper.setPrintingLotte

```java
// LabelPrintHelper.java:1240
String pCompCode_lotte = si.EMARTLOGIS_CODE; // 롯데전용 업체코드

// LabelPrintHelper.java:1342
pBarcode = pCompCode_lotte + making_date + print_weight_str.substring(...) + si.getEMARTITEM_CODE().substring(0, 6) + boxserial_cnt;
```

- **영향**: EMARTLOGIS_CODE가 정확히 매핑되어야 L0 바코드 앞부분(업체코드)이 정상 출력됨
- **대응 방안**: Step 3(EMARTLOGIS_CODE 확정) 완료 후 L0 바코드 출력값 직접 검증 필수

### searchType=0/2/4/5 분기

- **영향 없음**: 각 searchType은 별도 JSP URL을 사용하며 독립적으로 동작

---

## 6. 데이터 저장 구조

### 변수 매핑 (searchType=6 파싱 — ProgressDlgShipSearch.java)

| temp[] | 컬럼명 | 저장 필드 | 용도 |
|:------:|--------|-----------|------|
| temp[0] | GI_D_ID | si.GI_D_ID | 출하상세 PK (SEQ) |
| temp[1] | ITEM_CODE | si.ITEM_CODE | 품목코드 |
| temp[2] | ITEM_NAME | si.ITEM_NAME | 품목명 |
| temp[3] | EMARTITEM_CODE | si.EMARTITEM_CODE | 롯데 상품코드 |
| temp[4] | EMARTITEM | si.EMARTITEM | 롯데 상품명 |
| temp[5] | GI_REQ_PKG | si.GI_REQ_PKG | 요청박스수 (Float→Int 변환) |
| temp[6] | GI_REQ_QTY | si.GI_REQ_QTY | 요청중량 (소수점 절사 처리) |
| temp[7] | GI_REQ_DATE | si.GI_REQ_DATE | 요청일자 |
| temp[8] | BL_NO | si.BL_NO | BL번호 |
| temp[9] | BRAND_CODE | si.BRAND_CODE | 브랜드코드 ('' 고정) |
| temp[10] | CLIENT_CODE | si.CLIENT_CODE | 거래처코드 |
| temp[11] | CLIENTNAME | si.CLIENTNAME | 거래처명 |
| temp[12] | CENTERNAME | si.CENTERNAME | 센터명 |
| temp[13] | ITEM_SPEC | si.ITEM_SPEC | 품목규격 |
| temp[14] | CT_CODE | si.CT_CODE | 원산지코드 |
| temp[15] | IMPORT_ID_NO | si.IMPORT_ID_NO | 수입신고번호/이력번호 |
| temp[16] | PACKER_CODE | si.PACKER_CODE | 패커코드 |
| temp[17] | PACKER_PRODUCT_CODE | si.PACKER_PRODUCT_CODE | 패커상품코드(PPCODE) |
| temp[18] | BARCODE_TYPE | si.BARCODE_TYPE | 바코드타입 (L0 등) |
| temp[19] | ITEM_TYPE | si.ITEM_TYPE | 아이템타입 ('S' 고정) |
| temp[20] | PACKWEIGHT | si.PACKWEIGHT | 포장중량 |
| temp[21] | BARCODEGOODS | si.BARCODEGOODS | 상품바코드 |
| temp[22] | STORE_IN_DATE | si.STORE_IN_DATE | 납기일자 |
| temp[23] | EMARTLOGIS_CODE | si.EMARTLOGIS_CODE | 롯데 업체코드 (L0 바코드용) |
| temp[24] | WH_AREA | si.WH_AREA | 창고구역 ('' 고정) |
| temp[25] | LAST_BOX_ORDER | si.LAST_BOX_ORDER | 마지막 박스순번 |

### 인덱스 매핑

```
JSP out.println 순서  ↔  Java temp[] 인덱스
0=GI_D_ID             ↔  temp[0]  → si.GI_D_ID
1=ITEM_CODE           ↔  temp[1]  → si.ITEM_CODE
...
23=EMARTLOGIS_CODE    ↔  temp[23] → si.EMARTLOGIS_CODE  (L0 바코드 앞부분)
24=WH_AREA            ↔  temp[24] → si.WH_AREA
25=LAST_BOX_ORDER     ↔  temp[25] → si.LAST_BOX_ORDER
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
    ├── Common.searchType = "6"  (롯데)
    ├── data += " AND D.창고코드 = '...' "  (Step 2 수정 후)
    ↓
HttpHelper.sendDataDb(data, "highland", "search_shipment", URL_SEARCH_SHIPMENT_LOTTE)
    ↓
search_shipment_lotte.jsp
    ├── getMSSQLConnection()
    ├── MSSQL 직접 JOIN 쿼리 (Step 1 수정 후)
    │     FROM SM_출고상세 D
    │     JOIN SM_마트사발주롯데마트 LE ...
    │     WHERE H.마트사구분='6' AND D.출고수량>0
    └── out.println (26개 컬럼, ";;" 구분자)
    ↓
ProgressDlgShipSearch.doInBackground()
    ├── split(";;") → rows[]
    ├── split("::", -1) → temp[0..25]
    └── Shipments_Info 객체 생성 (temp[0]~temp[25] 파싱)
    ↓
onPostExecute() → ShipmentActivity UI 갱신
```

---

## 8. 개발 플랜

### Step 1: search_shipment_lotte.jsp MSSQL 직접 JOIN 쿼리 작성 (26개 컬럼)

**Part 1. 분석**
- 파일: `search_shipment_lotte.jsp`
- 범위: JSP 전체 쿼리 블록 (L37~94)
- 용도: Oracle VIEW `VW_PDA_WID_LIST_LOTTE` 제거, MSSQL 직접 JOIN 쿼리로 교체, out.println 34개 → 26개로 정리
- 주의할 점:
  - EMARTLOGIS_CODE = `COALESCE(M1.물류코드, M2.물류코드)` (이마트 JSP L60 패턴 확정)
  - PACKWEIGHT = `NULL` (Oracle VIEW 하드코딩 NULL 그대로 유지 — 이마트/홈플러스와 다름)
  - WHERE절 `COALESCE(M1.타입구분, M2.타입구분) = 'W'` 포함 필수
  - out.println 순서가 Java temp[] 인덱스와 정확히 일치해야 함

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | Oracle VIEW 참조 | JSP L72 | `FROM VW_PDA_WID_LIST_LOTTE` 제거 |
| 2 | SELECT 34개 컬럼 | JSP L38~71 | 26개로 교체 (제거 8개: GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME) |
| 3 | out.println 34개 | JSP L85~94 | 26개로 교체 |
| 4 | ORDER BY EOI_ID | JSP L74 | `LE.SEQ ASC`으로 교체 (Oracle ORDER BY EOI_ID ASC 대응) |

**Part 2. 변환 계획**
- 변환 방식: 4.1절 변경 후 코드 기준으로 전체 쿼리 교체
- EMARTLOGIS_CODE: `COALESCE(M1.물류코드, M2.물류코드)` 확정 적용 (Step 3 별도 수정 불필요)
- PACKWEIGHT: Oracle 원본 `NULL AS PACKWEIGHT` 그대로 유지 (이마트/홈플러스와 달리 하드코딩 NULL)
- 주의사항: `qry_where`는 기존과 동일하게 WHERE 절 뒤에 이어 붙임, `getMSSQLConnection()` 및 Statement/ResultSet 처리 코드는 변경하지 않음

**체크리스트**
- [x] Part 1: SELECT 26개 컬럼 구성 확인 (제거 8개 누락 없는지 검증)
- [x] Part 1: out.println 순서 26개 확인 (temp[0]~temp[25] 인덱스 일치)
- [x] Part 2: MSSQL JOIN 구조 작성 완료
- [x] Part 2: WHERE H.마트사구분='6' AND D.출고수량>0 포함 확인
- [x] Part 2: ORDER BY 교체 확인
- [x] Part 3: JSP 파일 저장 완료
- [ ] Part 4: Tomcat 재시작 후 JSP 컴파일 오류 없음 확인
- [ ] Part 5: MSSQL에서 해당 쿼리 직접 실행하여 결과 26개 컬럼 확인

**Part 6. 변경 내용**:
- **무엇을**: `search_shipment_lotte.jsp` 전체 쿼리 블록을 Oracle VIEW `VW_PDA_WID_LIST_LOTTE` SELECT에서 MSSQL 직접 JOIN 쿼리(26개 컬럼)로 교체, out.println 34개 → 26개 축소
- **왜**: Oracle VIEW는 MSSQL에 존재하지 않아 실행 즉시 오류 발생; 필요한 컬럼만 직접 JOIN으로 구성하여 MSSQL에서 동작 가능하게 전환
- **어떻게**: GI_H_ID·EOI_ID·AMOUNT·GOODS_R_ID·GR_REF_NO·BRANDNAME·EMARTLOGIS_NAME·PACKERNAME 8개 미파싱 컬럼 제거, EMARTLOGIS_CODE=`COALESCE(M1.물류코드,M2.물류코드)`, STORE_IN_DATE=`SD.납기일자`, ORDER BY=`LE.SEQ ASC`, WHERE에 `COALESCE(M1.타입구분,M2.타입구분)='W'` 추가

---

### Step 2: ProgressDlgShipSearch.java 창고코드 D. 별칭 추가

**Part 1. 분석**
- 파일: `ProgressDlgShipSearch.java`
- 범위: L162~170 (searchType="6" 분기)
- 용도: MSSQL 직접 JOIN 쿼리에서 창고코드 ambiguous 오류 방지
- 주의할 점: 다른 searchType 분기(0, 2, 5)에는 영향 없음

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 창고코드 WHERE 조건 | L164~165 | `창고코드` → `D.창고코드` |

**Part 2. 변환 계획**
- 변환 방식: 문자열 `" AND 창고코드 = '"` → `" AND D.창고코드 = '"` 로 1개 위치만 수정
- 주의사항: searchType="6" 분기만 수정, 다른 분기 코드 변경하지 않음

**체크리스트**
- [x] Part 1: searchType="6" 분기 창고코드 위치 확인 (L164~165)
- [x] Part 2: `D.창고코드` 수정 완료
- [x] Part 3: 수정 후 주변 코드 변경 없음 확인
- [ ] Part 4: 컴파일 오류 없음 확인
- [x] Part 5: searchType=0, 2 분기 창고코드 조건과 패턴 일치 확인

**Part 6. 변경 내용**:
- **무엇을**: `ProgressDlgShipSearch.java` L165 searchType="6" 분기의 `" AND 창고코드 = '"` → `" AND D.창고코드 = '"`로 수정
- **왜**: MSSQL 직접 JOIN 쿼리에서 여러 테이블에 `창고코드` 컬럼이 존재하여 테이블 별칭 없으면 ambiguous 오류 발생 가능; 출고상세 테이블(D) 기준이 정확
- **어떻게**: searchType="6" 분기 L165 문자열 1곳만 수정, 도매(searchType="3") 등 다른 분기는 건드리지 않음

---

### Step 3: EMARTLOGIS_CODE 확정 적용 (Step 1에 포함)

**Part 1. 분석**

**Oracle 원본 로직**:
```sql
( SELECT NVL(REF_CODE2,'회사코드없음')
  FROM B_COMMON_CODE bcc
  WHERE bcc.MASTER_CODE LIKE 'LOTTE_STORE_CODE'
    AND bcc.code = wmoi.CENTER_CODE
) AS EMARTLOGIS_CODE
```

**MSSQL 확정 처리**:
- `COALESCE(M1.물류코드, M2.물류코드) AS EMARTLOGIS_CODE`
- **근거**: 이마트 `search_shipment.jsp` L60에서 동일 패턴으로 EMARTLOGIS_CODE 처리 확인
- Oracle B_COMMON_CODE → MSSQL CO_매출처품목코드매핑.물류코드 (M1: 직접 거래처 / M2: 부모 거래처)

**용도**:
- `LabelPrintHelper.java:1240`: `pCompCode_lotte = si.EMARTLOGIS_CODE`
- `LabelPrintHelper.java:1342`: L0 바코드 앞부분 = `pCompCode_lotte + making_date + 중량 + 마트상품코드 + 박스순번`

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | EMARTLOGIS_CODE | Step 1 JSP 수정 시 함께 적용 | `COALESCE(M1.물류코드, M2.물류코드)` |

**체크리스트**
- [x] Part 1: MSSQL 등가 표현식 확정 (`COALESCE(M1.물류코드, M2.물류코드)`) — Step 1 적용 시 함께 처리
- [ ] Part 3: MSSQL에서 실제 롯데 출하 데이터로 EMARTLOGIS_CODE 출력값 검증
- [ ] Part 4: LabelPrintHelper L0 바코드 출력값이 기존과 동일한지 확인

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 4: 통합 테스트

| # | 테스트 항목 | 확인 |
|:-:|------------|------|
| 1 | PDA에서 searchType=6(롯데) 선택 후 출하대상받기 실행 → 정상 조회 | □ |
| 2 | 출하대상 목록 26개 필드 모두 정상 파싱 확인 (GI_D_ID~LAST_BOX_ORDER) | □ |
| 3 | 창고코드 선택 시 필터링 정상 동작 확인 | □ |
| 4 | L0 바코드 계근 후 출력 → EMARTLOGIS_CODE 포함 바코드 값 정상 확인 | □ |
| 5 | LAST_BOX_ORDER 서브쿼리 — SM_출고계근 데이터 있는 경우/없는 경우 각각 확인 | □ |
| 6 | searchType=0(이마트), 2(홈플러스), 4(비정량) 정상 동작 영향 없음 확인 | □ |
| 7 | 전체 롯데 출하 → 계근 → 라벨 출력 전체 흐름 1건 End-to-End 검증 | □ |

---

### 개발 순서 요약

```
Step 1: search_shipment_lotte.jsp MSSQL 직접 JOIN (26개 컬럼, EMARTLOGIS_CODE 확정 포함)
    ↓
Step 2: ProgressDlgShipSearch.java D.창고코드 별칭 추가
    ↓
Step 3: EMARTLOGIS_CODE 실제 값 검증 (L0 바코드 출력값 확인)
    ↓
Step 4: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 롯데 출하대상 정상 조회

```
1. PDA 앱 로그인
2. 마트사 구분 → 롯데(searchType=6) 선택
3. 창고 선택 (선택 안 함 / 특정 창고 선택 각각)
4. 출하대상 받기 버튼 터치
5. 서버로부터 출하 목록 수신 확인
6. 목록 항목 선택 → 상세 정보 26개 필드 값 확인 (GI_D_ID, ITEM_CODE, CENTERNAME, EMARTLOGIS_CODE 등)
```

### 시나리오 2: L0 바코드 출력 정상 확인

```
1. 롯데 출하대상 조회 (시나리오 1 완료)
2. 계근 화면 진입
3. 바코드타입 = L0 인 품목 선택
4. 중량 입력 후 라벨 출력 실행
5. 출력된 L0 바코드 값 확인:
   - 바코드 = pCompCode_lotte(EMARTLOGIS_CODE) + 제조일자 + 중량4자리 + 마트상품코드6자리 + 박스순번4자리
6. 기존 Oracle 환경 출력값과 동일한지 대조
```

### 시나리오 3: 창고코드 필터링 동작 확인

```
1. PDA 설정 → 창고 선택 (특정 창고코드 선택)
2. 롯데 출하대상 받기 실행
3. WHERE 조건에 D.창고코드='선택값' 포함 여부 서버 로그 확인
4. 해당 창고 품목만 목록에 표시되는지 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | EMARTLOGIS_CODE 값이 잘못되어 L0 바코드 오출력 | Oracle `B_COMMON_CODE` 등가 MSSQL 테이블 미특정 | Step 3에서 운영팀 확인 후 최종 표현식 적용 필수 |
| 2 | SM_마트사발주롯데마트 JOIN 결과 0건 | `D.마트사SEQ=LE.SEQ` 조건 불일치 (ERP 전환 후 SEQ 체계 변경 가능) | ERP 담당자에게 롯데 마트사 발주 연결 키 재확인 |
| 3 | 월품목별재고화일_LOT별_VIEW V LEFT JOIN 결과 NULL | 해당 VIEW가 MSSQL에 없거나 데이터 없음 | 홈플러스 JSP(개발47) 동작 확인 후 동일 패턴 적용 |
| 4 | LAST_BOX_ORDER 서브쿼리 TOP 1 결과 NULL | SM_출고계근에 해당 출고상세SEQ 데이터 없음 | LEFT JOIN 패턴이므로 NULL 허용 — Java에서 null-safe 처리 확인 |
| 5 | 창고코드 D. 별칭 없이 ambiguous 오류 | 여러 테이블에 창고코드 컬럼 중복 | Step 2에서 D.창고코드로 수정하면 해결 |
| 6 | GI_REQ_PKG FLOAT → Int 파싱 오류 | MSSQL에서 박스수량이 FLOAT으로 반환 | 기존 `(int) Double.parseDouble(temp[5])` 처리 유지 (이미 개발30에서 수정됨) |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 사전 | ⑤⑥ 사전 검증 | ✅ 완료 (PASS) |
| 1 | search_shipment_lotte.jsp MSSQL 직접 JOIN (26개 컬럼) | ✅ 완료 |
| 2 | ProgressDlgShipSearch.java D.창고코드 별칭 추가 | ✅ 완료 |
| 3 | EMARTLOGIS_CODE 실제 값 검증 (L0 바코드 출력값 확인) | ⏳ 대기 |
| 4 | 통합 테스트 | ⏳ 대기 |

**⑤ code-verifier 사전 검증 결과**:
- 전환 후 26개 인덱스 시뮬레이션 26/26 PASS (Java temp[0]~temp[25] 완전 일치)
- Java 파싱 코드는 이미 전환 후 26개 기준으로 작성됨 (JSP만 전환 전)
- 컴파일 BUILD SUCCESSFUL
- WARN: 창고코드 `D.` 별칭 없음 → Step 2에서 수정

**⑥ original-comparator 사전 검증 결과**:
- 현재 JSP 34개 컬럼·순서·WHERE·ORDER BY → 원본과 완전 일치 PASS
- 허용 차이: DB접속 Oracle→MSSQL, 인코딩 UTF-8, 로그 방식 변경

**가이드 주요 변경 이력 (2026-05-04)**:
- EMARTLOGIS_CODE: placeholder 제거 → `COALESCE(M1.물류코드, M2.물류코드)` 확정 (이마트 JSP L60 근거)
- PACKWEIGHT: `COALESCE(NULLIF(V.평균중량,0), I.박스중량)` → `NULL AS PACKWEIGHT` (Oracle 원본 하드코딩 NULL 따름)
- WHERE절: `COALESCE(M1.타입구분, M2.타입구분) = 'W'` 추가 (이마트/홈플러스 동일 조건)
- ORDER BY: `LE.센터코드/PPCODE/품목명` → `LE.SEQ ASC` (Oracle ORDER BY EOI_ID ASC 대응)

---

**관련 문서**:
- `app/doc/개발/47_홈플러스정량_출하대상받기_JSP_MSSQL전환.md` — 동일 패턴 홈플러스 JSP 전환 참고
- `app/doc/view/VW_PDA_WID_LIST_LOTTE` — Oracle VIEW DDL (UNION 2블록 구조)
- JSP 현재: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_lotte.jsp`
- Java: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`
- LabelPrintHelper: `app/src/main/java/com/rgbsolution/highland_emart/print/LabelPrintHelper.java` (L1226~1489, setPrintingLotte)
- ERP: `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\java\com\sgis\domain\sm\order\entity\MartCmnpyOrderLotteEntity.java`

**문서 버전**: 1.0
