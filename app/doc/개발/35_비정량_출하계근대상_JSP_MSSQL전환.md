# 비정량(searchType=4) 출하계근대상받기 JSP MSSQL 전환

**작성일**: 2026-04-07
**목적**: 비정량 JSP(`search_production_nonfixed.jsp`)를 Oracle VIEW 기반 37개 컬럼에서 MSSQL 직접 JOIN 31개 컬럼으로 전환하여, 이마트 JSP(`search_shipment.jsp`)와 동일한 구조로 만든다. Java 파싱(ProgressDlgShipSearch.java)은 이미 searchType "0"/"4" 동일 처리이므로 수정 불필요.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### search_production_nonfixed.jsp (비정량 JSP)

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_production_nonfixed.jsp`

```jsp
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
    + " FROM VW_PDA_WID_LIST_NONFIXED"
    + qry_where
    + " ORDER BY EOI_ID ASC";
```

- Oracle VIEW(`VW_PDA_WID_LIST_NONFIXED`) 기반 37개 컬럼 조회
- out.println으로 37개 컬럼을 "::" 구분자로 출력
- MSSQL에 해당 VIEW가 존재하지 않아 전환 필요

### 문제점

1. **MSSQL에 VW_PDA_WID_LIST_NONFIXED VIEW가 없음** — Oracle VIEW를 직접 JOIN 쿼리로 전환 필요
2. **JSP 37개 vs Java 31개 인덱스 불일치** — 현재 Java 파싱은 이마트 기준 31개이므로, JSP도 31개 컬럼으로 맞춰야 함
3. **8개 미사용 컬럼 존재** — GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME은 Java에서 파싱하지 않음
4. **2개 누락 컬럼** — EMART_PLANT_CODE, GI_L_ID가 비정량 JSP에 없으나 Java 파싱에서 필요

---

## 2. 변경 구조

### 데이터 흐름

```
[변경 전]
PDA 앱 (searchType=4) → search_production_nonfixed.jsp
    → Oracle VW_PDA_WID_LIST_NONFIXED (37개 컬럼)
    → out.println 37개 컬럼
    → Java temp[0]~temp[30] 파싱 (31개 기준) → 인덱스 불일치 발생

[변경 후]
PDA 앱 (searchType=4) → search_production_nonfixed.jsp
    → MSSQL 직접 JOIN (이마트 JSP 기반 + 비정량 차이 4개 컬럼)
    → out.println 31개 컬럼 (이마트 JSP와 동일 구조)
    → Java temp[0]~temp[30] 파싱 (31개 기준) → 인덱스 일치
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_production_nonfixed.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\` | SELECT 쿼리를 VIEW 기반 37개 → MSSQL 직접 JOIN 31개로 전환, out.println 31개 컬럼으로 변경 |

> **Java 파싱 수정 불필요**: `ProgressDlgShipSearch.java`에서 searchType "0"과 "4"는 동일한 파싱 로직(temp[0]~temp[30])을 사용하며, JSP를 31개 컬럼으로 맞추면 인덱스가 자동 일치한다.

---

## 4. 수정 상세

### 4.1 search_production_nonfixed.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_production_nonfixed.jsp`

**변경 전 (SELECT 쿼리):**

```jsp
String quertystring = "SELECT " 
    + "GI_H_ID"
    + ", GI_D_ID"
    // ... (37개 컬럼, Oracle VIEW 기반)
    + " FROM VW_PDA_WID_LIST_NONFIXED"
    + qry_where
    + " ORDER BY EOI_ID ASC";
```

**변경 후 (MSSQL 직접 JOIN 쿼리):**

```jsp
String quertystring = "SELECT /* 비정량 출고상세 종합 조회 */"
    + " D.SEQ AS GI_D_ID"
    + ", I.품목코드 AS ITEM_CODE"
    + ", I.품목명 AS ITEM_NAME"
    + ", ME.상품코드 AS EMARTITEM_CODE"
    + ", ME.상품명 AS EMARTITEM"
    + ", L.박스수량 AS GI_REQ_PKG"
    + ", L.중량 AS GI_REQ_QTY"
    + ", D.출고일자 AS GI_REQ_DATE"
    + ", ISNULL(V.BLNO, V.이력번호) AS BL_NO"
    + ", '' AS BRAND_CODE"
    + ", ME.점포코드 AS CLIENT_CODE"
    + ", ME.점포명 AS CLIENTNAME"
    + ", B.상호 AS CENTERNAME"
    + ", I.규격 AS ITEM_SPEC"
    + ", I.원산지 AS CT_CODE"
    + ", '0000' AS PACKER_CODE"                                    // ★ 비정량 차이: 고정값
    + ", V.이력번호 AS IMPORT_ID_NO"
    + ", I.품목코드 AS PACKER_PRODUCT_CODE"                         // ★ 비정량 차이: PPCODE → 품목코드
    + ", COALESCE(M1.바코드타입, M2.바코드타입) AS BARCODE_TYPE"      // 이마트와 동일 (M9분기 없음)
    + ", 'HW' AS ITEM_TYPE"                                        // ★ 비정량 차이: 고정값
    + ", COALESCE(NULLIF(V.평균중량,0), I.박스중량) AS PACKWEIGHT"
    + ", I.품목코드 AS BARCODEGOODS"                                // ★ 비정량 차이: 상품바코드 → 품목코드
    + ", SD.납기일자 AS STORE_IN_DATE"
    + ", COALESCE(M1.물류코드, M2.물류코드) AS EMARTLOGIS_CODE"
    + ", B.창고구역 AS WH_AREA"
    + ", C.명칭 AS USE_NAME"
    + ", I.제품용도 AS USE_CODE"
    + ", C1.명칭 AS CT_NAME"
    + ", ME.점포코드 AS STORE_CODE"
    + ", '' AS EMART_PLANT_CODE"
    + ", L.SEQ AS GI_L_ID"
    + " FROM SM_출고상세 D"
    + " INNER JOIN SM_출고머리 H"
    + "   ON H.회사코드 = D.회사코드"
    + "  AND H.출고사업장 = D.출고사업장"
    + "  AND H.출고일자 = D.출고일자"
    + "  AND H.출고일련번호 = D.출고일련번호"
    + " JOIN CO_품목코드 I"
    + "   ON D.회사코드 = I.회사코드"
    + "  AND D.출고품목코드 = I.품목코드"
    + " JOIN SM_수주머리 SH"
    + "   ON D.회사코드 = SH.회사코드"
    + "  AND D.수주사업장 = SH.수주사업장"
    + "  AND D.수주일자 = SH.수주일자"
    + "  AND D.수주일련번호 = SH.수주일련번호"
    + " JOIN SM_수주상세 SD"
    + "   ON SH.회사코드 = SD.회사코드"
    + "  AND SH.수주사업장 = SD.수주사업장"
    + "  AND SH.수주일자 = SD.수주일자"
    + "  AND SH.수주일련번호 = SD.수주일련번호"
    + "  AND D.순번 = SD.순번"
    + " JOIN SM_마트사발주이마트 ME"
    + "   ON SD.마트사SEQ = ME.SEQ"
    + " JOIN CO_거래처MASTER B"
    + "   ON ME.회사코드 = B.회사코드"
    + "  AND ME.점포코드 = B.마트사거래처코드"
    + "  AND B.마트사구분 = '7'"
    + " JOIN CO_거래처MASTER G"
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
    + " LEFT JOIN CO_각종소분류코드 C"
    + "   ON C.회사코드 = I.회사코드"
    + "  AND C.대분류 = '043'"
    + "  AND C.소분류 = I.제품용도"
    + " LEFT JOIN CO_각종소분류코드 C1"
    + "   ON C1.회사코드 = I.회사코드"
    + "  AND C1.대분류 = 'Q14'"
    + "  AND C1.소분류 = I.원산지"
    + " WHERE H.마트사구분 = '7'"
    + "   AND D.출고수량 > 0"
    + "   AND COALESCE(M1.타입구분, M2.타입구분) IN ('J', 'B')"     // ★ 이마트 'W' → 비정량 IN('J','B')
    + "   AND COALESCE(M1.바코드타입, M2.바코드타입) = 'M8'"         // ★ 비정량 M8 조건 추가
    + "   AND I.PPCODE != ''"                                        // PPCODE 빈값 방어코드 (이마트 JSP와 동일)
    + qry_where
    + " ORDER BY GI_D_ID ASC";
```

**변경 전 (out.println):**

```jsp
out.println(rs.getString("GI_H_ID") + "::" + rs.getString("GI_D_ID") + "::" + rs.getString("EOI_ID") + "::" + rs.getString("ITEM_CODE") + "::"
    // ... 37개 컬럼 출력
    + rs.getString("STORE_CODE") + ";;");
```

**변경 후 (out.println):**

```jsp
out.println(
    rs.getString("GI_D_ID") + "::" +           // 0
    rs.getString("ITEM_CODE") + "::" +         // 1
    rs.getString("ITEM_NAME") + "::" +         // 2
    rs.getString("EMARTITEM_CODE") + "::" +    // 3
    rs.getString("EMARTITEM") + "::" +         // 4
    rs.getString("GI_REQ_PKG") + "::" +        // 5
    rs.getString("GI_REQ_QTY") + "::" +        // 6
    rs.getString("GI_REQ_DATE") + "::" +       // 7
    rs.getString("BL_NO") + "::" +             // 8
    rs.getString("BRAND_CODE") + "::" +        // 9
    rs.getString("CLIENT_CODE") + "::" +       // 10
    rs.getString("CLIENTNAME") + "::" +        // 11
    rs.getString("CENTERNAME") + "::" +        // 12
    rs.getString("ITEM_SPEC") + "::" +         // 13
    rs.getString("CT_CODE") + "::" +           // 14
    rs.getString("IMPORT_ID_NO") + "::" +      // 15
    rs.getString("PACKER_CODE") + "::" +       // 16
    rs.getString("PACKER_PRODUCT_CODE") + "::" + // 17
    rs.getString("BARCODE_TYPE") + "::" +      // 18
    rs.getString("ITEM_TYPE") + "::" +         // 19
    rs.getString("PACKWEIGHT") + "::" +        // 20
    rs.getString("BARCODEGOODS") + "::" +      // 21
    rs.getString("STORE_IN_DATE") + "::" +     // 22
    rs.getString("EMARTLOGIS_CODE") + "::" +   // 23
    rs.getString("WH_AREA") + "::" +           // 24
    rs.getString("USE_NAME") + "::" +          // 25
    rs.getString("USE_CODE") + "::" +          // 26
    rs.getString("CT_NAME") + "::" +           // 27
    rs.getString("STORE_CODE") + "::" +        // 28
    rs.getString("EMART_PLANT_CODE") + "::" +  // 29
    rs.getString("GI_L_ID") + ";;"             // 30
    );
```

**검증**: out.println 31개 컬럼 순서가 이마트 JSP와 동일하며, Java temp[0]~temp[30] 파싱 인덱스와 1:1 일치하는지 확인

---

## 5. 사이드이펙트

### ProgressDlgShipSearch.java (Java 파싱)

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`

- searchType "0"(이마트)과 "4"(비정량)는 **동일한 파싱 로직**(temp[0]~temp[30])을 사용
- 비정량 JSP를 31개 컬럼으로 전환하면 인덱스가 자동 일치하므로 **수정 불필요**
- 영향 없음

### 다른 searchType JSP

- searchType 0(이마트): search_shipment.jsp — 이미 MSSQL 전환 완료, 영향 없음
- searchType 1(생산), 2(홈플러스), 3(도매), 5(홈플러스비정량), 6(롯데): 별도 JSP, 영향 없음

---

## 6. 데이터 저장 구조

### 이마트 JSP와 비정량 JSP의 핵심 4개 컬럼 차이 매핑

| Index | 컬럼명 | 이마트 JSP (search_shipment.jsp) | 비정량 JSP 전환 후 | 차이 이유 |
|:-----:|--------|--------------------------------|-------------------|----------|
| 16 | PACKER_CODE | `I.패커코드` | **`'0000'` 고정값** | 비정량은 자체 원료육, 패커 없음 |
| 17 | PACKER_PRODUCT_CODE | `I.PPCODE` | **`I.품목코드`** | 패커 없으므로 자사 품목코드 사용 |
| 19 | ITEM_TYPE | `COALESCE(M1.타입구분, M2.타입구분)` | **`'HW'` 고정값** | PDA FLOOR 처리 안 하기 위한 구분자 |
| 21 | BARCODEGOODS | `I.상품바코드` | **`I.품목코드`** | 패커 없으므로 자사 품목코드 사용 |

> **참고**: BARCODE_TYPE(index 18)은 이마트 MSSQL JSP에서도 M9분기 없이 `COALESCE(M1.바코드타입, M2.바코드타입)`만 사용하므로 비정량과 동일 처리. 차이 컬럼에서 제외.

### WHERE 조건 차이 매핑

| 조건 | 이마트 JSP | 비정량 JSP 전환 후 | 비고 |
|------|-----------|-------------------|------|
| 타입구분 | `= 'W'` | **`IN ('J', 'B')`** | 원료육 → 제품/비정량 |
| 바코드타입 | 조건 없음 | **`= 'M8'` 추가** | 비정량 M8 전용 |

### 인덱스 매핑 (31개, Java temp[0]~temp[30])

```
temp[0]  = GI_D_ID             → setGI_D_ID()
temp[1]  = ITEM_CODE           → setITEM_CODE()
temp[2]  = ITEM_NAME           → setITEM_NAME()
temp[3]  = EMARTITEM_CODE      → setEMARTITEM_CODE()
temp[4]  = EMARTITEM           → setEMARTITEM()
temp[5]  = GI_REQ_PKG          → setGI_REQ_PKG()
temp[6]  = GI_REQ_QTY          → setGI_REQ_QTY()
temp[7]  = GI_REQ_DATE         → setGI_REQ_DATE()
temp[8]  = BL_NO               → setBL_NO()
temp[9]  = BRAND_CODE          → setBRAND_CODE()
temp[10] = CLIENT_CODE         → setCLIENT_CODE()
temp[11] = CLIENTNAME          → setCLIENTNAME()
temp[12] = CENTERNAME          → setCENTERNAME()
temp[13] = ITEM_SPEC           → setITEM_SPEC()
temp[14] = CT_CODE             → setCT_CODE()
temp[15] = IMPORT_ID_NO        → setIMPORT_ID_NO()
temp[16] = PACKER_CODE         → setPACKER_CODE()        ★ '0000' 고정
temp[17] = PACKER_PRODUCT_CODE → setPACKER_PRODUCT_CODE() ★ I.품목코드
temp[18] = BARCODE_TYPE        → setBARCODE_TYPE()        ★ M9분기 없음
temp[19] = ITEM_TYPE           → setITEM_TYPE()           ★ 'HW' 고정
temp[20] = PACKWEIGHT          → setPACKWEIGHT()
temp[21] = BARCODEGOODS        → setBARCODEGOODS()        ★ I.품목코드
temp[22] = STORE_IN_DATE       → setSTORE_IN_DATE()
temp[23] = EMARTLOGIS_CODE     → setEMARTLOGIS_CODE()
temp[24] = WH_AREA             → setWH_AREA()
temp[25] = USE_NAME            → setUSE_NAME()
temp[26] = USE_CODE            → setUSE_CODE()
temp[27] = CT_NAME             → setCT_NAME()
temp[28] = STORE_CODE          → setSTORE_CODE()
temp[29] = EMART_PLANT_CODE    → setEMART_PLANT_CODE()
temp[30] = GI_L_ID             → setGI_L_ID()
```

---

## 7. 호출 시점

```
[PDA 앱 시작]
    ↓
[ShipSearchActivity] 사용자가 비정량(searchType=4) 조회 실행
    ↓
[ProgressDlgShipSearch.doInBackground()]
    ├── Common.URL_SEARCH_PRODUCTION_NONFIXED URL 구성
    ├── HttpHelper.sendDataDb() → search_production_nonfixed.jsp 호출
    │       ↓ MSSQL 직접 JOIN 쿼리 실행 (31개 컬럼)
    │       ↓ out.println (31개 컬럼, "::" 구분)
    ├── receiveData 수신
    ├── split(";;") → row 분리
    ├── split("::", -1) → column 분리 → temp[]
    ├── temp[0]~temp[30] → Shipments_Info 객체에 매핑
    └── DB INSERT (TB_SHIPMENT)
```

---

## 8. 개발 플랜

### Step 1: SELECT 쿼리 전환 (VIEW → MSSQL 직접 JOIN)

**Part 1. 분석**
- 메서드: JSP SELECT 쿼리
- 범위: search_production_nonfixed.jsp:37~77줄
- 용도: Oracle VIEW 기반 37개 컬럼 쿼리를 MSSQL 직접 JOIN 31개 컬럼 쿼리로 전환
- 주의할 점: 이마트 JSP를 기반으로 하되, 비정량 차이 4개 컬럼과 WHERE 조건 2개를 반드시 반영

| # | 항목 | 이마트 JSP 값 | 비정량 전환 값 | 비고 |
|---|------|-------------|-------------|------|
| 1 | PACKER_CODE | `I.패커코드` | `'0000'` | 고정값 |
| 2 | PACKER_PRODUCT_CODE | `I.PPCODE` | `I.품목코드` | 자사 품목코드 |
| 3 | BARCODE_TYPE | COALESCE + M9분기 | COALESCE만 | M9분기 제거 |
| 4 | ITEM_TYPE | `COALESCE(M1.타입구분, M2.타입구분)` | `'HW'` | 고정값 |
| 5 | BARCODEGOODS | `I.상품바코드` | `I.품목코드` | 자사 품목코드 |
| 6 | WHERE 타입구분 | `= 'W'` | `IN ('J', 'B')` | 제품/비정량 |
| 7 | WHERE 바코드타입 | 없음 | `= 'M8'` | 추가 조건 |

**Part 2. 변환 계획**
- 변환 방식: 이마트 JSP(`search_shipment.jsp`)의 전체 쿼리를 복사한 후, 위 7개 항목만 비정량 값으로 변경
- 주의사항:
  - JOIN 구조는 이마트 JSP와 100% 동일하게 유지 (FROM, JOIN, LEFT JOIN 모두)
  - logger명과 System.out.println 로그를 `search_production_nonfixed`로 유지
  - `conn = getMSSQLConnection()` 유지 (이미 MSSQL 연결)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: SELECT 쿼리 전환 수행
- [x] Part 4: JSP 문법 오류 없는지 확인
- [x] Part 5: 단위테스트 (쿼리 실행 가능 여부)
- [x] Part 6: 회귀테스트

**Part 6. 변경 내용**:
- **무엇을**: SELECT 쿼리를 Oracle VIEW(`VW_PDA_WID_LIST_NONFIXED`) 기반 37개 컬럼에서 MSSQL 직접 JOIN 31개 컬럼으로 전환
- **왜**: MSSQL에 해당 VIEW가 존재하지 않아 직접 JOIN 쿼리로 전환 필요
- **어떻게**: 이마트 JSP(`search_shipment.jsp`)의 쿼리를 복사 후 비정량 차이 4개 컬럼(PACKER_CODE, PACKER_PRODUCT_CODE, ITEM_TYPE, BARCODEGOODS) + WHERE 2개(타입구분 IN('J','B'), 바코드타입='M8') 반영, PPCODE 빈값 방어코드 추가

---

### Step 2: out.println 전환 (37개 → 31개 컬럼)

**Part 1. 분석**
- 메서드: JSP out.println
- 범위: search_production_nonfixed.jsp:86~98줄
- 용도: 37개 컬럼 출력을 31개 컬럼(이마트 JSP와 동일 구조)으로 전환
- 주의할 점: 컬럼 순서가 Java temp[] 파싱 인덱스(0~30)와 반드시 1:1 일치해야 함

| # | 삭제 컬럼 (8개) | 이유 |
|---|----------------|------|
| 1 | GI_H_ID | Java 미파싱, 로컬DB 미사용 |
| 2 | EOI_ID | Java 미파싱, 로컬DB 미사용 |
| 3 | AMOUNT | Java 미파싱, 로컬DB 미사용 |
| 4 | GOODS_R_ID | Java 미파싱, 로컬DB 미사용 |
| 5 | GR_REF_NO | Java 미파싱, 로컬DB 미사용 |
| 6 | BRANDNAME | Java 미파싱, 로컬DB 미사용 |
| 7 | PACKERNAME | Java 미파싱, 로컬DB 미사용 |
| 8 | EMARTLOGIS_NAME | Java 미파싱, 로컬DB 미사용 |

| # | 추가 컬럼 (2개) | 이유 |
|---|----------------|------|
| 1 | EMART_PLANT_CODE (index 29) | Java 파싱 대상, 이마트와 동일 |
| 2 | GI_L_ID (index 30) | Java 파싱 대상, LOT 식별 |

**Part 2. 변환 계획**
- 변환 방식: 이마트 JSP(`search_shipment.jsp`)의 out.println 부분을 그대로 복사
- 주의사항: while(rs.next()) 구문도 이마트 JSP와 동일한 형식으로 변경

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: out.println 전환 수행
- [x] Part 4: 31개 컬럼 순서 검증 (이마트 JSP와 대조)
- [x] Part 5: 단위테스트 (출력 형식 확인)
- [x] Part 6: 회귀테스트

**Part 6. 변경 내용**:
- **무엇을**: out.println을 37개 컬럼에서 31개 컬럼(이마트 JSP와 동일 구조)으로 전환
- **왜**: Java 파싱(temp[0]~temp[30]) 31개 기준과 인덱스를 일치시키기 위함
- **어떻게**: 이마트 JSP의 out.println 부분을 그대로 복사하여 while(rs.next()) 구문 포함 동일 형식으로 변경

---

### Step 3: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | JSP 쿼리 정상 실행 (DB 연결 + SELECT 결과 반환) | |
| 2 | out.println 31개 컬럼 출력 확인 ("::" 구분, ";;" 행 구분) | |
| 3 | PDA 앱에서 비정량(searchType=4) 출하대상 조회 정상 동작 | |
| 4 | Java temp[0]~temp[30] 파싱 결과 검증 (각 필드에 올바른 데이터 매핑) | |
| 5 | PACKER_CODE = '0000' 확인 | |
| 6 | PACKER_PRODUCT_CODE = 품목코드 확인 | |
| 7 | BARCODE_TYPE = M8 타입 확인 | |
| 8 | ITEM_TYPE = 'HW' 확인 | |
| 9 | BARCODEGOODS = 품목코드 확인 | |
| 10 | 이마트(searchType=0) 출하대상 조회 정상 동작 (기존 기능 유지 확인) | |
| 11 | 로컬DB(TB_SHIPMENT) INSERT 정상 확인 | |

---

### 개발 순서 요약

```
Step 1: SELECT 쿼리 전환 (VIEW → MSSQL 직접 JOIN, 비정량 차이 4개 + WHERE 2개 반영)
    ↓
Step 2: out.println 전환 (37개 → 31개 컬럼, 이마트 JSP와 동일 구조)
    ↓
Step 3: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 비정량 출하대상 조회

```
1. PDA 앱 실행 → 로그인
2. 비정량(searchType=4) 출하대상 조회 메뉴 선택
3. 조회 버튼 클릭
4. 결과 목록에 데이터가 정상적으로 표시되는지 확인
5. 각 항목의 상세 데이터가 올바른지 확인:
   - PACKER_CODE = '0000'
   - ITEM_TYPE = 'HW'
   - BARCODE_TYPE = M8 계열
```

### 시나리오 2: 이마트 출하대상 조회 (회귀 테스트)

```
1. PDA 앱 실행 → 로그인
2. 이마트(searchType=0) 출하대상 조회 메뉴 선택
3. 조회 버튼 클릭
4. 결과 목록이 기존과 동일하게 표시되는지 확인
5. 이마트 JSP(search_shipment.jsp)는 수정하지 않았으므로 동일 결과 기대
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | MSSQL 쿼리 실행 오류 | 테이블명/컬럼명 오타 | 이마트 JSP에서 정상 동작하는 JOIN 구조를 기반으로 하므로 가능성 낮음. 서버 로그에서 쿼리 확인 |
| 2 | 비정량 데이터가 조회되지 않음 | WHERE 조건 `IN ('J','B')` 또는 `= 'M8'`에 해당 데이터 없음 | CO_매출처품목코드매핑 테이블에서 타입구분/바코드타입 값 직접 확인 |
| 3 | BARCODEGOODS에 품목코드 대신 상품바코드가 들어감 | SELECT에서 `I.품목코드` 대신 `I.상품바코드` 사용 | 비정량은 `I.품목코드` (Oracle VIEW 원본과 동일), 이마트는 `I.상품바코드` — 혼동 주의 |
| 4 | Java 파싱 인덱스 불일치 | out.println 컬럼 순서 오류 | 이마트 JSP의 out.println과 정확히 동일한 순서로 출력 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | SELECT 쿼리 전환 (VIEW → MSSQL 직접 JOIN) | ✅ 완료 |
| 2 | out.println 전환 (37개 → 31개 컬럼) | ✅ 완료 |
| 3 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/소스분석/40_비정량_출하계근대상받기_JSP_Java파싱_인덱스분석.md` — JSP 37개 vs Java 31개 인덱스 불일치 분석
- `app/doc/소스분석/41_이마트VIEW_vs_비정량VIEW_비교분석.md` — 이마트 VIEW vs 비정량 VIEW 비교, 핵심 4개 컬럼 차이
- `app/doc/소스분석/42_이마트Oracle_MSSQL_JSP_비정량Oracle_3자비교분석.md` — Oracle VIEW → MSSQL JSP 전환 매핑, 테이블/JOIN/컬럼/WHERE 3자 비교
- `app/doc/view/VW_PDA_WID_LIST_NONFIXED` — 비정량 Oracle VIEW SQL 정의

---

**문서 버전**: 1.0
