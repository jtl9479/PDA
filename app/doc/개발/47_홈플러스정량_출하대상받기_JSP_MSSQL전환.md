# 홈플러스 정량(searchType=2) 출하대상받기 JSP MSSQL 전환

**작성일**: 2026-04-30
**목적**: search_shipment_homeplus.jsp의 Oracle VIEW 의존성 제거 후 MSSQL 직접 JOIN 쿼리로 교체하고, Java의 창고코드 WHERE 조건 D. 별칭 누락 버그를 함께 수정한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### search_shipment_homeplus.jsp (현재 — 동작 불가)

```sql
SELECT GI_H_ID, GI_D_ID, EOI_ID, ITEM_CODE, ITEM_NAME,
       EMARTITEM_CODE, EMARTITEM, GI_REQ_PKG, GI_REQ_QTY, AMOUNT,
       GOODS_R_ID, GR_REF_NO, GI_REQ_DATE, BL_NO, BRAND_CODE, BRANDNAME,
       CLIENT_CODE, CLIENTNAME, CENTERNAME, ITEM_SPEC, CT_CODE,
       IMPORT_ID_NO, PACKER_CODE, PACKERNAME, PACKER_PRODUCT_CODE,
       BARCODE_TYPE, ITEM_TYPE, PACKWEIGHT, BARCODEGOODS,
       STORE_IN_DATE, EMARTLOGIS_CODE, EMARTLOGIS_NAME
FROM VW_PDA_WID_HOMEPLUS_LIST   -- MSSQL에 없음 → 실행 즉시 오류
ORDER BY HOMPLUS_STORE_CODE ASC, PACKER_PRODUCT_CODE ASC, ITEM_NAME ASC, EOI_ID ASC
```

out.println 순서: GI_H_ID(0) ~ EMARTLOGIS_NAME(31), 총 32개

### ProgressDlgShipSearch.java searchType=2 분기 (현재 — 창고코드 별칭 누락)

```java
// ProgressDlgShipSearch.java:133~139
} else if(Common.searchType.equals("2")) {
    if (!Common.selectWarehouseCode.isEmpty()) {
        data += " AND 창고코드 = '" + Common.selectWarehouseCode + "'";  // D. 누락
    }
    receiveData = HttpHelper.getInstance().sendDataDb(
        data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT_HOMEPLUS);
}
```

### 문제점

1. **JSP DB 오류**: `getMSSQLConnection()`으로 MSSQL 연결 후 Oracle 전용 VIEW `VW_PDA_WID_HOMEPLUS_LIST` 참조 → MSSQL에 해당 VIEW 없음 → 실행 즉시 SQL 오류
2. **인덱스 전부 불일치**: 현재 JSP index 0 = `GI_H_ID`, Java temp[0] = `GI_D_ID` → 32개 전부 2칸 밀림
3. **창고코드 조건 별칭 누락**: `AND 창고코드 = '...'` → MSSQL에서 ambiguous 오류 가능 (이마트는 `AND D.창고코드 = '...'`로 정상)
4. **제거 컬럼 8개 포함**: GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME은 Java에서 파싱하지 않으므로 출력 불필요

---

## 2. 변경 구조

### 데이터 흐름

```
[변경 전]
PDA (searchType=2) → search_shipment_homeplus.jsp
    → FROM VW_PDA_WID_HOMEPLUS_LIST (Oracle VIEW, MSSQL 없음)
    → 32개 컬럼 출력 (index 0=GI_H_ID)
    → Java temp[0]=GI_D_ID로 파싱 → 전부 불일치 → 동작 불가

[변경 후]
PDA (searchType=2) → search_shipment_homeplus.jsp
    → MSSQL 직접 JOIN 쿼리 (SM_출고상세 D, SM_마트사발주홈플러스 HE 등)
    → 24개 컬럼 출력 (index 0=GI_D_ID)
    → Java temp[0]=GI_D_ID로 파싱 → 전부 일치 → 정상 동작
```

### 컬럼 수 변화

| 구분 | 컬럼 수 | 비고 |
|------|:-------:|------|
| 변경 전 (Oracle JSP) | 32개 | GI_H_ID~EMARTLOGIS_NAME |
| 변경 후 (MSSQL JSP) | 24개 | GI_D_ID~EMARTLOGIS_CODE |
| 제거 | 8개 | Java 미파싱 컬럼 전부 제거 |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_shipment_homeplus.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp` | Oracle VIEW → MSSQL 직접 JOIN 쿼리 교체, 출력 컬럼 32개 → 24개 |
| 2 | **ProgressDlgShipSearch.java** | `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java:136` | searchType=2 창고코드 조건 `AND 창고코드` → `AND D.창고코드` 수정 |

---

## 4. 수정 상세

### 4.1 search_shipment_homeplus.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp`

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
    + " FROM VW_PDA_WID_HOMEPLUS_LIST"
    + qry_where
    + "ORDER BY HOMPLUS_STORE_CODE ASC, PACKER_PRODUCT_CODE ASC, ITEM_NAME ASC, EOI_ID ASC";

// out.println (32개)
out.println(rs.getString("GI_H_ID") + "::" + rs.getString("GI_D_ID") + "::" + rs.getString("EOI_ID") + "::" ...
    + rs.getString("EMARTLOGIS_NAME") + ";;");
```

**변경 후:**

```java
String quertystring = "SELECT /* 홈플러스 출하대상 조회 */"
    + " D.SEQ AS GI_D_ID"
    + ", I.품목코드 AS ITEM_CODE"
    + ", I.품목명 AS ITEM_NAME"
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
    + ", I.PPCODE AS PACKER_PRODUCT_CODE"
    + ", COALESCE(M1.바코드타입, M2.바코드타입) AS BARCODE_TYPE"
    + ", 'S' AS ITEM_TYPE"
    + ", COALESCE(NULLIF(V.평균중량,0), I.박스중량) AS PACKWEIGHT"
    + ", I.상품바코드 AS BARCODEGOODS"
    + ", SD.납기일자 AS STORE_IN_DATE"
    + ", HE.납품처코드 AS EMARTLOGIS_CODE"
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
    + " WHERE D.출고수량 > 0"
    + qry_where
    + " ORDER BY HE.납품처코드 ASC, I.PPCODE ASC, I.품목명 ASC";

// out.println (24개)
out.println(
    rs.getString("GI_D_ID") + "::" +           // 0
    rs.getString("ITEM_CODE") + "::" +          // 1
    rs.getString("ITEM_NAME") + "::" +          // 2
    rs.getString("EMARTITEM_CODE") + "::" +     // 3
    rs.getString("EMARTITEM") + "::" +          // 4
    rs.getString("GI_REQ_PKG") + "::" +         // 5
    rs.getString("GI_REQ_QTY") + "::" +         // 6
    rs.getString("GI_REQ_DATE") + "::" +        // 7
    rs.getString("BL_NO") + "::" +              // 8
    rs.getString("BRAND_CODE") + "::" +         // 9
    rs.getString("CLIENT_CODE") + "::" +        // 10
    rs.getString("CLIENTNAME") + "::" +         // 11
    rs.getString("CENTERNAME") + "::" +         // 12
    rs.getString("ITEM_SPEC") + "::" +          // 13
    rs.getString("CT_CODE") + "::" +            // 14
    rs.getString("IMPORT_ID_NO") + "::" +       // 15
    rs.getString("PACKER_CODE") + "::" +        // 16
    rs.getString("PACKER_PRODUCT_CODE") + "::" + // 17
    rs.getString("BARCODE_TYPE") + "::" +       // 18
    rs.getString("ITEM_TYPE") + "::" +          // 19
    rs.getString("PACKWEIGHT") + "::" +         // 20
    rs.getString("BARCODEGOODS") + "::" +       // 21
    rs.getString("STORE_IN_DATE") + "::" +      // 22
    rs.getString("EMARTLOGIS_CODE") + ";;"      // 23
);
```

**검증**: out.println index 0=GI_D_ID → Java temp[0]=setGI_D_ID() 일치, 23개 "::" + 마지막 ";;" 확인

---

### 4.2 ProgressDlgShipSearch.java

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`

**변경 전 (136번 줄):**

```java
} else if(Common.searchType.equals("2")) {
    if (!Common.selectWarehouseCode.isEmpty()) {
        data += " AND 창고코드 = '" + Common.selectWarehouseCode + "'";  // D. 누락
    }
    receiveData = HttpHelper.getInstance().sendDataDb(
        data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT_HOMEPLUS);
```

**변경 후:**

```java
} else if(Common.searchType.equals("2")) {
    if (!Common.selectWarehouseCode.isEmpty()) {
        data += " AND D.창고코드 = '" + Common.selectWarehouseCode + "'";  // D. 별칭 추가
    }
    receiveData = HttpHelper.getInstance().sendDataDb(
        data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT_HOMEPLUS);
```

**검증**: searchType=0 분기(119번 줄)의 `AND D.창고코드 = '...'`와 동일 형식으로 통일

---

## 5. 사이드이펙트

### ProgressDlgShipSearch.java 파싱 로직 (영향 없음)

공통 파싱 블록 (211~261줄)은 temp[0]~temp[23]을 이마트 기준 순서로 파싱한다.
JSP 변경 후 index 0=GI_D_ID부터 시작하는 24개 컬럼 구조가 공통 파싱 블록과 정확히 일치하므로 **Java 파싱 코드 수정 불필요**.

searchType=2 전용 추가 분기(temp[24] 이후) 없음 → 이마트(0)·비정량(4)·롯데(6)와 달리 추가 파싱 분기 미적용이 현재 설계 의도.

### 동기화 로직 GI_L_ID (현재 설계 범위 외)

PDA 동기화 비교(309~310줄)는 `GI_D_ID + GI_L_ID` 복합키를 사용한다.
홈플러스 JSP는 이번 전환에서 GI_L_ID를 출력하지 않으므로 GI_L_ID는 여전히 빈 문자열("") 저장된다.
이 동작은 원본 Oracle JSP 시절과 동일하므로 현재 범위에서는 변경하지 않는다.
(소스분석 52 문서에 누락 항목으로 기록됨, 별도 검토 필요)

### CENTERNAME 매핑 (CO_거래처MASTER.상호)

Oracle VIEW에서 CENTERNAME은 `EO.CENTERNAME` (B_COMMON_CODE 조인 결과 = 'HOMEPLUS_STORE_CODE' 기준 코드명칭)을 사용했다.
MSSQL 전환 후에는 `CO_거래처MASTER.상호`를 사용한다.
이 값이 실제 홈플러스 센터명으로 동일하게 출력되는지 테스트 시나리오에서 확인이 필요하다.

---

## 6. 데이터 저장 구조

### 변환 후 JSP → Java 인덱스 매핑 (24개)

| Index | JSP 별칭 | MSSQL 소스 | Java setter | 비고 |
|:-----:|:--------:|-----------|:----------:|------|
| 0 | GI_D_ID | D.SEQ | setGI_D_ID | |
| 1 | ITEM_CODE | I.품목코드 | setITEM_CODE | |
| 2 | ITEM_NAME | I.품목명 | setITEM_NAME | |
| 3 | EMARTITEM_CODE | HE.상품코드 | setEMARTITEM_CODE | SM_마트사발주홈플러스 |
| 4 | EMARTITEM | HE.상품명 | setEMARTITEM | SM_마트사발주홈플러스 |
| 5 | GI_REQ_PKG | L.박스수량 | setGI_REQ_PKG | FLOAT→INT 변환 (Java) |
| 6 | GI_REQ_QTY | L.중량 | setGI_REQ_QTY | 소수점 4자리 이상 → 1자리 절사 (Java) |
| 7 | GI_REQ_DATE | D.출고일자 | setGI_REQ_DATE | |
| 8 | BL_NO | COALESCE(NULLIF(V.BLNO,''), V.이력번호) | setBL_NO | |
| 9 | BRAND_CODE | '' 빈값 | setBRAND_CODE | 상수 |
| 10 | CLIENT_CODE | HE.납품처코드 | setCLIENT_CODE | |
| 11 | CLIENTNAME | HE.납품처명 | setCLIENTNAME | |
| 12 | CENTERNAME | B.상호 | setCENTERNAME | CO_거래처MASTER |
| 13 | ITEM_SPEC | I.규격 | setITEM_SPEC | |
| 14 | CT_CODE | I.원산지 | setCT_CODE | |
| 15 | IMPORT_ID_NO | V.이력번호 | setIMPORT_ID_NO | |
| 16 | PACKER_CODE | I.패커코드 | setPACKER_CODE | |
| 17 | PACKER_PRODUCT_CODE | I.PPCODE | setPACKER_PRODUCT_CODE | |
| 18 | BARCODE_TYPE | COALESCE(M1.바코드타입, M2.바코드타입) | setBARCODE_TYPE | |
| 19 | ITEM_TYPE | 'S' 하드코딩 | setITEM_TYPE | Oracle VIEW에서도 'S' 하드코딩 |
| 20 | PACKWEIGHT | COALESCE(NULLIF(V.평균중량,0), I.박스중량) | setPACKWEIGHT | |
| 21 | BARCODEGOODS | I.상품바코드 | setBARCODEGOODS | |
| 22 | STORE_IN_DATE | SD.납기일자 | setSTORE_IN_DATE | SM_수주상세 |
| 23 | EMARTLOGIS_CODE | HE.납품처코드 | setEMARTLOGIS_CODE | Oracle: EO.STORECODE 동일 역할 |

### SM_마트사발주홈플러스 컬럼 확인 사항

Oracle에서 `W_E_ORDER_ITEM`의 `STORE_CODE`, `STORE_NAME`을 CLIENTNAME/EMARTLOGIS_CODE로 사용.
MSSQL `SM_마트사발주홈플러스` Entity에는 **점포코드/점포명 컬럼 없음**.
대신 `납품처코드(dvyfgOfficCode)`, `납품처명(dvyfgOfficNm)` 컬럼이 존재하여 동일 역할을 한다.

| Oracle (W_E_ORDER_ITEM) | MSSQL (SM_마트사발주홈플러스) | 용도 |
|:----------------------:|:---------------------------:|------|
| EO.STORECODE | HE.납품처코드 | CLIENT_CODE, EMARTLOGIS_CODE |
| EO.STORENAME | HE.납품처명 | CLIENTNAME |

> **주의**: 실제 납품처코드/납품처명 데이터가 Oracle STORE_CODE/STORE_NAME과 동일한 값 체계인지 테스트 시 확인 필요.

### ORDER BY 컬럼 매핑

| Oracle ORDER BY | MSSQL 대응 |
|:--------------:|:----------:|
| HOMPLUS_STORE_CODE | HE.납품처코드 |
| PACKER_PRODUCT_CODE | I.PPCODE |
| ITEM_NAME | I.품목명 |
| EOI_ID | (제거 — Java 미파싱 컬럼, ORDER BY에서 제외) |

---

## 7. 호출 시점

```
[사용자] 홈플러스 정량 대상받기 버튼 클릭
    ↓
[MainActivity] ProgressDlgShipSearch.execute()
    ↓
[ProgressDlgShipSearch.doInBackground()]
    ├── WHERE 조건 생성 (D.회사코드, D.출고일자, D.창고코드)
    ├── HttpHelper.sendDataDb() → search_shipment_homeplus.jsp 호출
    │       ↓ MSSQL 직접 JOIN 쿼리 실행 (변경 후)
    │   응답: GI_D_ID::ITEM_CODE::...::EMARTLOGIS_CODE;; (24개 컬럼)
    ├── split(";;") → row 분리
    ├── split("::", -1) → temp[0]~temp[23] 파싱
    ├── Shipments_Info 객체 생성 및 리스트 추가
    └── DBHandler.refreshShipmentList() → TB_SHIPMENT 동기화
    ↓
[ProgressDlgShipSearch.onPostExecute()]
    └── new ProgressDlgBarcodeSearch(mContext).execute()  → 바코드정보 조회 자동 시작
```

---

## 8. 개발 플랜

### Step 1: search_shipment_homeplus.jsp MSSQL 직접 JOIN 쿼리 작성 (24개 컬럼)

**Part 1. 분석**
- 대상: `search_shipment_homeplus.jsp` 전체 쿼리 및 out.println 블록
- 범위: JSP 36번~91번 줄 (quertystring 선언 ~ out.println 블록)
- 용도: Oracle VIEW 제거 후 MSSQL 직접 JOIN 쿼리로 교체, 출력 24개로 축소
- 주의할 점:
  - 이마트 JSP(search_shipment.jsp)의 JOIN 구조를 기반으로 홈플러스 전용 변경점만 반영
  - 이마트는 `SM_마트사발주이마트(ME)` JOIN, 홈플러스는 `SM_마트사발주홈플러스(HE)` JOIN
  - `CO_거래처MASTER B` JOIN 조건 확인 필요 (`HE.납품처코드 = B.마트사거래처코드` + 마트사구분 조건)
  - ITEM_TYPE은 Oracle VIEW와 동일하게 `'S'` 하드코딩 유지
  - ORDER BY: HOMPLUS_STORE_CODE → `HE.납품처코드 ASC`, EOI_ID는 Java 미파싱이므로 ORDER BY에서도 제거
  - STORE_IN_DATE: Oracle VIEW에서 `W_GOODS_R.STORE_IN_DATE` 사용, MSSQL에서는 `SM_수주상세.납기일자`로 LEFT JOIN

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 현재 JSP | search_shipment_homeplus.jsp:36~71 | Oracle VIEW 참조, 32개 컬럼 |
| 2 | 이마트 JSP 참고 | search_shipment.jsp:36~118 | MSSQL 직접 JOIN (완료 상태, 동일 패턴 적용) |
| 3 | 홈플러스 테이블 | SM_마트사발주홈플러스 | 납품처코드, 납품처명, 상품코드, 상품명 |
| 4 | Oracle VIEW 구조 | app/doc/view/VW_PDA_WID_HOMEPLUS_LIST | JOIN 구조 참고 |

**Part 2. 변환 계획**
- 변환 방식: search_shipment.jsp(이마트) 기반으로 홈플러스 전용 부분 변경
  - `SM_마트사발주이마트 ME` → `SM_마트사발주홈플러스 HE`
  - `ME.점포코드`, `ME.점포명` → `HE.납품처코드`, `HE.납품처명`
  - `CO_거래처MASTER B JOIN` 조건: `B.마트사거래처코드` 매핑 대상을 HE.납품처코드로 변경, 마트사구분 조건은 홈플러스 구분값으로 변경 또는 제거 (이마트는 `B.마트사구분 = '7'` 사용)
  - `STORE_IN_DATE`: `SM_수주상세 SD LEFT JOIN ON SD.마트사SEQ = HE.SEQ`로 납기일자 가져오기
  - `ITEM_TYPE`: `'S'` 하드코딩 (Oracle에서도 동일)
  - `EMARTLOGIS_CODE`: Oracle에서 `EO.STORECODE` 동일 역할 → `HE.납품처코드`
  - ORDER BY: `HE.납품처코드 ASC, I.PPCODE ASC, I.품목명 ASC` (EOI_ID 제거)
  - WHERE 고정 조건: `D.출고수량 > 0`
  - out.println: 24개 컬럼, index 0=GI_D_ID ~ index 23=EMARTLOGIS_CODE, 마지막 ";;"
- 주의사항:
  - qry_where 변수는 반드시 WHERE 구문 내 AND 절 뒤에 위치 (`WHERE D.출고수량 > 0 [qry_where] ORDER BY`)
  - 공백 확인: qry_where 앞뒤 공백이 있어야 ORDER BY와 붙지 않음

**체크리스트**
- [x] Part 1: JSP 현재 코드 및 이마트 JSP 구조 확인 완료
- [x] Part 2: 변환 계획 검토 완료
- [x] Part 3: JSP 쿼리 + out.println 수정 수행
- [x] Part 4: 구문 오류 없이 저장 확인
- [ ] Part 5: MSSQL 쿼리 직접 실행 (SQL 도구에서 홈플러스 데이터 있는 날짜로 확인)
- [x] Part 6: 24개 컬럼 index 순서 재검증

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**: search_shipment_homeplus.jsp의 Oracle VIEW(`VW_PDA_WID_HOMEPLUS_LIST`) 참조 쿼리를 MSSQL 직접 JOIN 쿼리로 교체하고, out.println 출력을 32개에서 24개 컬럼으로 축소
- **왜**: MSSQL 환경에 Oracle 전용 VIEW가 존재하지 않아 실행 즉시 SQL 오류 발생 — 직접 JOIN 쿼리로 대체하여 MSSQL에서 정상 동작하도록 함
- **어떻게**: `SM_출고상세 D`를 기준으로 `SM_출고머리 H`, `CO_품목코드 I`, `SM_마트사발주홈플러스 HE`, `CO_거래처MASTER B`, `SM_출고LOT L`, `월품목별재고화일_LOT별_VIEW V`, `SM_수주상세 SD` 등을 JOIN하여 24개 컬럼(index 0=GI_D_ID ~ index 23=EMARTLOGIS_CODE) 출력. Java temp[0]~temp[23] 파싱 구조와 정확히 일치하도록 out.println 순서 맞춤

---

### Step 2: ProgressDlgShipSearch.java searchType=2 창고코드 D. 별칭 수정

**Part 1. 분석**
- 메서드: `doInBackground()` 내 searchType=2 분기
- 범위: `ProgressDlgShipSearch.java:133~139`
- 용도: 창고코드 WHERE 조건에서 테이블 별칭 D. 누락 버그 수정
- 주의할 점:
  - searchType=0(이마트, 119번 줄), searchType=3(도매, 145번 줄), searchType=6(롯데, 165번 줄)의 창고코드 조건도 확인하여 패턴 통일
  - Java 파싱 로직(temp[0]~temp[23]) 수정 불필요

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 현재 코드 | ProgressDlgShipSearch.java:136 | `AND 창고코드 = '...'` (D. 누락) |
| 2 | 이마트 참고 | ProgressDlgShipSearch.java:119 | `AND D.창고코드 = '...'` (정상) |

**Part 2. 변환 계획**
- 변환 방식: 136번 줄 `" AND 창고코드 = '"` → `" AND D.창고코드 = '"` (D. 추가)
- 주의사항: 이 한 줄만 수정. 다른 searchType 분기는 이미 D. 포함되어 있으므로 건드리지 않음

**체크리스트**
- [ ] Part 1: 현재 코드 136번 줄 확인 완료
- [ ] Part 2: 변환 계획 검토 완료
- [ ] Part 3: 136번 줄 D. 추가 수정 수행
- [ ] Part 4: 컴파일 확인 (Android Studio 빌드 오류 없음)
- [ ] Part 5: searchType=2로 창고코드 선택 후 조회 조건 로그 확인
- [ ] Part 6: 회귀테스트 (다른 searchType 창고코드 조건 영향 없음 확인)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 3: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 홈플러스 정량(searchType=2) 대상받기 실행 → SQL 오류 없이 정상 응답 수신 | □ |
| 2 | 수신 데이터 index 0=GI_D_ID 값이 숫자(SEQ) 형태로 파싱됨 | □ |
| 3 | 수신 데이터 index 4=EMARTITEM(홈플러스 상품명) 정상 출력 | □ |
| 4 | 수신 데이터 index 19=ITEM_TYPE 값이 'S'임 | □ |
| 5 | 수신 데이터 index 23=EMARTLOGIS_CODE 값이 점포코드(납품처코드) 형태임 | □ |
| 6 | CENTERNAME(index 12) 홈플러스 센터명으로 출력됨 | □ |
| 7 | CLIENTNAME(index 11) 홈플러스 점포명(납품처명)으로 출력됨 | □ |
| 8 | 창고코드 선택 후 조회 → `AND D.창고코드 = '...'` 조건이 로그에 찍힘 | □ |
| 9 | 창고코드 미선택 후 조회 → 창고코드 조건 없이 정상 조회됨 | □ |
| 10 | TB_SHIPMENT에 searchType=2 데이터 정상 INSERT 확인 | □ |
| 11 | 이마트(searchType=0) 대상받기 정상 동작 (사이드이펙트 없음) | □ |

---

### 개발 순서 요약

```
Step 1: search_shipment_homeplus.jsp MSSQL 직접 JOIN 쿼리 작성 (24개 컬럼)
    ↓
Step 2: ProgressDlgShipSearch.java searchType=2 창고코드 D. 별칭 수정
    ↓
Step 3: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 홈플러스 정량 출하대상 정상 조회

```
1. PDA 앱 실행 → 로그인
2. searchType=2 (홈플러스) 선택
3. 날짜 선택 (홈플러스 출하 데이터가 있는 날짜)
4. 창고 미선택 상태로 [대상받기] 버튼 클릭
5. 로딩 다이얼로그 표시 → 조회 완료
6. 출하대상 목록이 화면에 표시됨 확인
7. 목록의 품목코드, 품목명, 홈플러스 상품명, 요청중량 등 값 정상 확인
```

### 시나리오 2: 창고코드 조건 포함 조회

```
1. PDA 앱 실행 → 로그인
2. searchType=2 (홈플러스) 선택
3. 날짜 선택, 창고코드 선택
4. [대상받기] 버튼 클릭
5. 로그캣에서 "출하리스트 조회조건 홈플러스로 들어옴" 로그 확인
6. 로그에 "AND D.창고코드 = '창고코드값'" 형태로 출력됨 확인
7. 해당 창고코드의 홈플러스 데이터만 조회됨 확인
```

### 시나리오 3: 사이드이펙트 확인 (이마트 정량)

```
1. PDA 앱 실행 → 로그인
2. searchType=0 (이마트) 선택
3. 이마트 출하 데이터가 있는 날짜 선택
4. [대상받기] 버튼 클릭
5. 이마트 출하대상 목록 정상 조회 확인 (홈플러스 변경 영향 없음)
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | SM_마트사발주홈플러스에 점포코드/점포명 없음 | Entity에 납품처코드/납품처명만 존재 | 납품처코드(dvyfgOfficCode), 납품처명(dvyfgOfficNm) 사용. 실제 값이 Oracle STORE_CODE와 동일 체계인지 테스트 확인 필요 |
| 2 | CO_거래처MASTER B JOIN 마트사구분 조건 불명확 | 이마트는 `B.마트사구분 = '7'` 사용, 홈플러스 구분값 미확인 | 마트사구분 조건 없이 `HE.납품처코드 = B.마트사거래처코드`만으로 JOIN, 데이터 확인 후 조건 추가 여부 결정 |
| 3 | CENTERNAME 값이 기존과 다를 수 있음 | Oracle: B_COMMON_CODE 조인으로 HOMEPLUS_STORE_CODE 코드명, MSSQL: CO_거래처MASTER.상호 | 테스트 시 CENTERNAME 출력값 육안 확인 필요 |
| 4 | STORE_IN_DATE NULL 가능 | SM_수주상세가 없는 출고상세 존재 시 LEFT JOIN 결과 NULL | LEFT JOIN 유지, NULL 허용 (이마트도 동일 처리) |
| 5 | D.마트사SEQ → HE.SEQ JOIN 조건 미존재 가능 | SM_출고상세에 마트사SEQ 컬럼 없을 경우 | 이마트 JSP에서 `D.마트사SEQ = ME.SEQ` 패턴 사용 중이므로 동일 컬럼 존재 전제. 없다면 SM_수주상세 등 중간 테이블 경유 필요 |
| 6 | G.계층코드 LEFT JOIN 오류 | CO_거래처MASTER G가 H.출고거래처로 없을 경우 | G JOIN을 LEFT JOIN으로 변경하거나 이마트 패턴 그대로 유지 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | search_shipment_homeplus.jsp MSSQL 직접 JOIN 쿼리 작성 (24개 컬럼) | ✅ 완료 |
| 2 | ProgressDlgShipSearch.java searchType=2 창고코드 D. 별칭 수정 | ⏳ 대기 |
| 3 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/소스분석/52_홈플러스정량_출하계근대상_JSP_Java파싱_인덱스분석.md` — JSP/Java 인덱스 상세 대조
- `app/doc/소스분석/54_홈플러스_계근데이터전송_JSP_MSSQL전환전_구조분석.md` — 계근 전송 JSP 구조 분석 (전체 흐름 맥락 파악)
- `app/doc/소스분석/55_홈플러스정량_출하대상받기_원본비교분석.md` — 원본 vs 현재 비교, 변경 사유
- `app/doc/소스분석/56_홈플러스정량_출하_전체흐름분석.md` — 전체 흐름 분석 (1~4단계)
- `app/doc/view/VW_PDA_WID_HOMEPLUS_LIST` — Oracle VIEW DDL (JOIN 구조 참조)
- `app/doc/column/01_VW_PDA_WID_HOMEPLUS_LIST.md` — 컬럼 사용/미사용 분류
- JSP 현재: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp`
- JSP 이마트 참고: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment.jsp`
- Java: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`

---

**문서 버전**: 1.0
