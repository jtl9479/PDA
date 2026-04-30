# 홈플러스정량_출하계근대상_JSP_Java파싱_인덱스분석 - JSP out.println과 Java temp[] 파싱 인덱스 대조 분석

## 개요

홈플러스 정량(searchType=2) 출하계근대상 조회 시 JSP가 출력하는 컬럼과 Java가 파싱하는 인덱스를 대조 분석한다.

- **JSP 파일 경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp`
- **Java 파싱 파일 경로**: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`
- **VIEW**: `VW_PDA_WID_HOMEPLUS_LIST` (현재 Oracle VIEW 사용 중 → MSSQL 전환 필요)
- **타입**: JSP + Java 파싱 인덱스 대조 분석
- **작성일**: 2026-04-30

---

## 1. 역할

- 홈플러스 정량(searchType=2) 출하계근대상 조회 시 서버(JSP)가 출력하는 데이터와 앱(Java)이 파싱하는 인덱스의 일치 여부를 분석
- 원본 JSP/Java와 현재 JSP/Java의 컬럼 구조 차이를 비교
- Oracle VIEW → MSSQL 직접 쿼리 전환 시 필요한 컬럼 매핑 근거 제공
- 이마트 정량(searchType=0) JSP와의 차이점 파악

---

## 2. 주요 상수/필드

| 상수/필드 | 타입 | 값 | 용도 |
|----------|------|-----|------|
| Common.searchType | String | "2" | 홈플러스 정량 출고 구분 |
| Common.URL_SEARCH_SHIPMENT_HOMEPLUS | String | BASE_URL + "/search_shipment_homeplus.jsp" | 홈플러스 정량 JSP URL |
| 구분자 | String | "::" | 컬럼 간 구분자 |
| 행 구분자 | String | ";;" | 행 간 구분자 |

---

## 3. 주요 메서드

| 메서드 | 위치(줄) | 반환 | 용도 |
|--------|:--------:|------|------|
| doInBackground() | ProgressDlgShipSearch.java:94줄 | Integer | 출하대상 조회 및 파싱 메인 로직 |

---

## 4. 호출 관계

### 4.1 이 파일이 호출하는 대상

| 호출 대상 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| HttpHelper | sendDataDb() | 138줄 | 홈플러스 JSP 호출 |
| Shipments_Info | set*() | 211~261줄 | 파싱 결과 객체에 설정 |

### 4.2 이 파일을 호출하는 곳

| 호출 위치 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| MainActivity 등 | execute() | - | 홈플러스 출하대상 조회 실행 |

---

## 5. 데이터 흐름

```
[PDA 앱] searchType="2" → HttpHelper.sendDataDb()
    ↓ URL_SEARCH_SHIPMENT_HOMEPLUS 호출
[JSP] search_shipment_homeplus.jsp
    ↓ SELECT FROM VW_PDA_WID_HOMEPLUS_LIST (Oracle VIEW)
    ↓ out.println (32개 컬럼, "::" 구분)
    ↓
[PDA 앱] receiveData 수신
    ↓ split(";;") → row 분리
    ↓ split("::", -1) → column 분리 → temp[]
    ↓
[파싱] temp[0]~temp[23] 파싱 → Shipments_Info 객체
    ↓ searchType="2" 전용 추가 분기 없음 (temp[24] 이후 미파싱)
```

---

## 6. 핵심 코드 및 인덱스 대조

### 6.1 현재 JSP out.println 인덱스 (search_shipment_homeplus.jsp)

JSP는 32개 컬럼을 출력한다 (search_shipment_homeplus.jsp:89~91줄).

```
out.println(
  rs.getString("GI_H_ID") + "::"       // index 0
  rs.getString("GI_D_ID") + "::"       // index 1
  rs.getString("EOI_ID") + "::"        // index 2
  rs.getString("ITEM_CODE") + "::"     // index 3
  rs.getString("ITEM_NAME") + "::"     // index 4
  rs.getString("EMARTITEM_CODE") + "::" // index 5
  rs.getString("EMARTITEM") + "::"     // index 6
  rs.getString("GI_REQ_PKG") + "::"    // index 7
  rs.getString("GI_REQ_QTY") + "::"   // index 8
  rs.getString("AMOUNT") + "::"       // index 9
  rs.getString("GOODS_R_ID") + "::"   // index 10
  rs.getString("GR_REF_NO") + "::"    // index 11
  rs.getString("GI_REQ_DATE") + "::"  // index 12
  rs.getString("BL_NO") + "::"        // index 13
  rs.getString("BRAND_CODE") + "::"   // index 14
  rs.getString("BRANDNAME") + "::"    // index 15
  rs.getString("CLIENT_CODE") + "::"  // index 16
  rs.getString("CLIENTNAME") + "::"   // index 17
  rs.getString("CENTERNAME") + "::"   // index 18
  rs.getString("ITEM_SPEC") + "::"    // index 19
  rs.getString("CT_CODE") + "::"      // index 20
  rs.getString("IMPORT_ID_NO") + "::" // index 21
  rs.getString("PACKER_CODE") + "::"  // index 22
  rs.getString("PACKERNAME") + "::"   // index 23
  rs.getString("PACKER_PRODUCT_CODE") + "::" // index 24
  rs.getString("BARCODE_TYPE") + "::" // index 25
  rs.getString("ITEM_TYPE") + "::"    // index 26
  rs.getString("PACKWEIGHT") + "::"   // index 27
  rs.getString("BARCODEGOODS") + "::" // index 28
  rs.getString("STORE_IN_DATE") + "::" // index 29
  rs.getString("EMARTLOGIS_CODE") + "::" // index 30
  rs.getString("EMARTLOGIS_NAME") + ";;" // index 31
)
```

### 6.2 현재 Java temp[] 파싱 인덱스 (ProgressDlgShipSearch.java)

Java는 searchType="2"(홈플러스)에 대한 전용 파싱 분기가 없다.
공통 파싱 블록(211~261줄)으로 temp[0]~temp[23]까지만 파싱하고,
searchType별 추가 분기에서 "2"는 해당하지 않으므로 temp[24] 이후는 전혀 파싱하지 않는다.

```java
// 공통 파싱 (모든 searchType 공통): temp[0]~temp[23]
si.setGI_D_ID(temp[0]);              // 0
si.setITEM_CODE(temp[1]);            // 1
si.setITEM_NAME(temp[2]);            // 2
si.setEMARTITEM_CODE(temp[3]);       // 3
si.setEMARTITEM(temp[4]);            // 4
si.setGI_REQ_PKG(...temp[5]...);     // 5
si.setGI_REQ_QTY(temp[6]);          // 6 (소수점 처리)
si.setGI_REQ_DATE(temp[7]);          // 7
si.setBL_NO(temp[8]);                // 8
si.setBRAND_CODE(temp[9]);           // 9
si.setCLIENT_CODE(temp[10]);         // 10
si.setCLIENTNAME(temp[11]);          // 11
si.setCENTERNAME(temp[12]);          // 12
si.setITEM_SPEC(temp[13]);           // 13
si.setCT_CODE(temp[14]);             // 14
si.setIMPORT_ID_NO(temp[15]);        // 15
si.setPACKER_CODE(temp[16]);         // 16
si.setPACKER_PRODUCT_CODE(temp[17]); // 17
si.setBARCODE_TYPE(temp[18]);        // 18
si.setITEM_TYPE(temp[19]);           // 19
si.setPACKWEIGHT(temp[20]);          // 20
si.setBARCODEGOODS(temp[21]);        // 21
si.setSTORE_IN_DATE(temp[22]);       // 22
si.setEMARTLOGIS_CODE(temp[23]);     // 23

// searchType별 추가 분기 (ProgressDlgShipSearch.java:265~286줄)
if ("0" || "4") → temp[24]~temp[30] 파싱 (7개)
if ("5")        → temp[24]~temp[28] 파싱 (5개)
if ("6")        → temp[24]~temp[25] 파싱 (2개)
// "2"(홈플러스 정량)는 어느 분기에도 해당 없음 → temp[24] 이후 파싱 없음
```

### 6.3 핵심 문제: JSP는 32개 컬럼 출력, Java는 24개(temp[0]~temp[23])만 파싱

| 구분 | 컬럼 수 |
|------|:-------:|
| 현재 홈플러스 JSP 출력 | 32개 |
| Java 파싱 (searchType=2) | 24개 (temp[0]~temp[23]) |
| 미파싱 컬럼 | 8개 (index 24~31) |

### 6.4 JSP out.println 인덱스 vs Java temp[] 파싱 인덱스 대조표

| JSP Index | JSP 컬럼명 | Java temp[] | Java 세터 | 매핑 일치 | 비고 |
|:---------:|-----------|:-----------:|----------|:---------:|------|
| 0 | GI_H_ID | temp[0] | setGI_D_ID() | **X** | JSP=GI_H_ID인데 Java는 GI_D_ID로 파싱 |
| 1 | GI_D_ID | temp[1] | setITEM_CODE() | **X** | JSP=GI_D_ID인데 Java는 ITEM_CODE로 파싱 |
| 2 | EOI_ID | temp[2] | setITEM_NAME() | **X** | JSP=EOI_ID인데 Java는 ITEM_NAME으로 파싱 |
| 3 | ITEM_CODE | temp[3] | setEMARTITEM_CODE() | **X** | 2칸 밀림 |
| 4 | ITEM_NAME | temp[4] | setEMARTITEM() | **X** | 2칸 밀림 |
| 5 | EMARTITEM_CODE | temp[5] | setGI_REQ_PKG() | **X** | 2칸 밀림 |
| 6 | EMARTITEM | temp[6] | setGI_REQ_QTY() | **X** | 2칸 밀림 (소수점 처리 대상) |
| 7 | GI_REQ_PKG | temp[7] | setGI_REQ_DATE() | **X** | |
| 8 | GI_REQ_QTY | temp[8] | setBL_NO() | **X** | |
| 9 | AMOUNT | temp[9] | setBRAND_CODE() | **X** | |
| 10 | GOODS_R_ID | temp[10] | setCLIENT_CODE() | **X** | |
| 11 | GR_REF_NO | temp[11] | setCLIENTNAME() | **X** | |
| 12 | GI_REQ_DATE | temp[12] | setCENTERNAME() | **X** | |
| 13 | BL_NO | temp[13] | setITEM_SPEC() | **X** | |
| 14 | BRAND_CODE | temp[14] | setCT_CODE() | **X** | |
| 15 | BRANDNAME | temp[15] | setIMPORT_ID_NO() | **X** | |
| 16 | CLIENT_CODE | temp[16] | setPACKER_CODE() | **X** | |
| 17 | CLIENTNAME | temp[17] | setPACKER_PRODUCT_CODE() | **X** | |
| 18 | CENTERNAME | temp[18] | setBARCODE_TYPE() | **X** | |
| 19 | ITEM_SPEC | temp[19] | setITEM_TYPE() | **X** | |
| 20 | CT_CODE | temp[20] | setPACKWEIGHT() | **X** | |
| 21 | IMPORT_ID_NO | temp[21] | setBARCODEGOODS() | **X** | |
| 22 | PACKER_CODE | temp[22] | setSTORE_IN_DATE() | **X** | |
| 23 | PACKERNAME | temp[23] | setEMARTLOGIS_CODE() | **X** | |
| 24 | PACKER_PRODUCT_CODE | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 25 | BARCODE_TYPE | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 26 | ITEM_TYPE | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 27 | PACKWEIGHT | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 28 | BARCODEGOODS | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 29 | STORE_IN_DATE | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 30 | EMARTLOGIS_CODE | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 31 | EMARTLOGIS_NAME | - | (파싱 없음) | - | Java에서 파싱하지 않음 |

**결론: 32개 컬럼 전부 매핑 불일치. 현재 홈플러스 JSP는 사실상 동작 불가 상태.**

### 6.5 이마트 JSP(search_shipment.jsp) out.println 인덱스 vs Java temp[] 파싱 (정상 매핑 참고)

참고: 이마트 JSP는 Java 파싱과 1:1로 일치한다.

| JSP Index | JSP 컬럼명 | Java temp[] | Java 세터 | 매핑 일치 |
|:---------:|-----------|:-----------:|----------|:---------:|
| 0 | GI_D_ID | temp[0] | setGI_D_ID() | O |
| 1 | ITEM_CODE | temp[1] | setITEM_CODE() | O |
| 2 | ITEM_NAME | temp[2] | setITEM_NAME() | O |
| 3 | EMARTITEM_CODE | temp[3] | setEMARTITEM_CODE() | O |
| 4 | EMARTITEM | temp[4] | setEMARTITEM() | O |
| 5 | GI_REQ_PKG | temp[5] | setGI_REQ_PKG() | O |
| 6 | GI_REQ_QTY | temp[6] | setGI_REQ_QTY() | O |
| 7 | GI_REQ_DATE | temp[7] | setGI_REQ_DATE() | O |
| 8 | BL_NO | temp[8] | setBL_NO() | O |
| 9 | BRAND_CODE | temp[9] | setBRAND_CODE() | O |
| 10 | CLIENT_CODE | temp[10] | setCLIENT_CODE() | O |
| 11 | CLIENTNAME | temp[11] | setCLIENTNAME() | O |
| 12 | CENTERNAME | temp[12] | setCENTERNAME() | O |
| 13 | ITEM_SPEC | temp[13] | setITEM_SPEC() | O |
| 14 | CT_CODE | temp[14] | setCT_CODE() | O |
| 15 | IMPORT_ID_NO | temp[15] | setIMPORT_ID_NO() | O |
| 16 | PACKER_CODE | temp[16] | setPACKER_CODE() | O |
| 17 | PACKER_PRODUCT_CODE | temp[17] | setPACKER_PRODUCT_CODE() | O |
| 18 | BARCODE_TYPE | temp[18] | setBARCODE_TYPE() | O |
| 19 | ITEM_TYPE | temp[19] | setITEM_TYPE() | O |
| 20 | PACKWEIGHT | temp[20] | setPACKWEIGHT() | O |
| 21 | BARCODEGOODS | temp[21] | setBARCODEGOODS() | O |
| 22 | STORE_IN_DATE | temp[22] | setSTORE_IN_DATE() | O |
| 23 | EMARTLOGIS_CODE | temp[23] | setEMARTLOGIS_CODE() | O |
| 24 | WH_AREA | temp[24] | setWH_AREA() | O |
| 25 | USE_NAME | temp[25] | setUSE_NAME() | O |
| 26 | USE_CODE | temp[26] | setUSE_CODE() | O |
| 27 | CT_NAME | temp[27] | setCT_NAME() | O |
| 28 | STORE_CODE | temp[28] | setSTORE_CODE() | O |
| 29 | EMART_PLANT_CODE | temp[29] | setEMART_PLANT_CODE() | O |
| 30 | GI_L_ID | temp[30] | setGI_L_ID() | O |

---

## 7. 원본 비교

### 7.1 원본 JSP vs 현재 JSP (search_shipment_homeplus.jsp)

원본 JSP 경로: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_shipment_homeplus.jsp`

| 항목 | 원본 JSP | 현재 JSP | 동일 |
|------|---------|---------|:----:|
| VIEW | VW_PDA_WID_HOMEPLUS_LIST | VW_PDA_WID_HOMEPLUS_LIST | O |
| 컬럼 수 | 32개 | 32개 | O |
| out.println 순서 | GI_H_ID부터 EMARTLOGIS_NAME까지 | GI_H_ID부터 EMARTLOGIS_NAME까지 | O |
| DB 연결 | Oracle (getConnection 직접) | MSSQL (getMSSQLConnection) | X |
| 인코딩 | euc-kr | UTF-8 | X |
| ORDER BY | HOMPLUS_STORE_CODE ASC, PACKER_PRODUCT_CODE ASC, ITEM_NAME ASC, EOI_ID ASC | 동일 | O |

**현재 JSP는 DB 연결만 MSSQL로 변경되었고, 컬럼 구조는 원본과 완전히 동일하다.**
**그러나 MSSQL에는 Oracle VIEW(VW_PDA_WID_HOMEPLUS_LIST)가 없으므로 현재 JSP는 실행 불가 상태.**

### 7.2 Oracle VIEW 컬럼 목록 vs 현재 JSP SELECT 컬럼 비교

Oracle VIEW `VW_PDA_WID_HOMEPLUS_LIST`가 제공하는 26개 컬럼과 JSP가 SELECT하는 32개 컬럼 비교.

| VIEW 컬럼 | JSP SELECT | JSP 출력 | 비고 |
|----------|:----------:|:--------:|------|
| GI_D_ID | O | index 1 | VIEW에 있음 |
| ITEM_CODE | O | index 3 | VIEW에 있음 |
| ITEM_NAME | O | index 4 | VIEW에 있음 |
| EMARTITEM_CODE | O | index 5 | VIEW에 있음 |
| EMARTITEM | O | index 6 | VIEW에 있음 |
| GI_REQ_PKG | O | index 7 | VIEW에 있음 |
| GI_REQ_QTY | O | index 8 | VIEW에 있음 |
| GI_REQ_DATE | O | index 12 | VIEW에 있음 |
| BL_NO | O | index 13 | VIEW에 있음 |
| BRAND_CODE | O | index 14 | VIEW에 있음 |
| CLIENT_CODE | O | index 16 | VIEW에 있음 |
| CLIENTNAME | O | index 17 | VIEW에 있음 |
| CENTERNAME | O | index 18 | VIEW에 있음 |
| ITEM_SPEC | O | index 19 | VIEW에 있음 |
| CT_CODE | O | index 20 | VIEW에 있음 |
| PACKER_CODE | O | index 22 | VIEW에 있음 |
| IMPORT_ID_NO | O | index 21 | VIEW에 있음 |
| PACKER_PRODUCT_CODE | O | index 24 | VIEW에 있음 |
| BARCODE_TYPE | O | index 25 | VIEW에 있음 |
| ITEM_TYPE | O | index 26 | VIEW에 있음 |
| PACKWEIGHT | O | index 27 | VIEW에 있음 |
| BARCODEGOODS | O | index 28 | VIEW에 있음 |
| STORE_IN_DATE | O | index 29 | VIEW에 있음 |
| GR_WAREHOUSE_CODE | **X** | **미사용** | VIEW에 있으나 JSP에서 미출력 |
| EMARTLOGIS_CODE | O | index 30 | VIEW에 있음 (VIEW에서는 STORECODE=EMARTLOGIS_CODE) |
| HOMPLUS_STORE_CODE | **X** | **미사용** (ORDER BY에만 사용) | VIEW에 있으나 out.println에 미포함 |
| GI_H_ID | O | index 0 | **VIEW에 없음** - Oracle VIEW 컬럼 목록에 없는 컬럼 |
| EOI_ID | O | index 2 | **VIEW에 없음** - Oracle VIEW 컬럼 목록에 없는 컬럼 |
| AMOUNT | O | index 9 | **VIEW에 없음** - Oracle VIEW 컬럼 목록에 없는 컬럼 |
| GOODS_R_ID | O | index 10 | **VIEW에 없음** - Oracle VIEW 컬럼 목록에 없는 컬럼 |
| GR_REF_NO | O | index 11 | **VIEW에 없음** - Oracle VIEW 컬럼 목록에 없는 컬럼 |
| BRANDNAME | O | index 15 | **VIEW에 없음** - Oracle VIEW 컬럼 목록에 없는 컬럼 |
| PACKERNAME | O | index 23 | **VIEW에 없음** - Oracle VIEW 컬럼 목록에 없는 컬럼 |
| EMARTLOGIS_NAME | O | index 31 | **VIEW에 없음** - Oracle VIEW 컬럼 목록에 없는 컬럼 |

**VIEW에 없는 컬럼 8개**: GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME
→ JSP가 VIEW에 없는 컬럼을 SELECT하고 있어 Oracle에서도 오류가 발생했을 가능성이 있음.
→ 실제 Oracle에서 VIEW 내부 서브쿼리가 해당 컬럼을 노출하는 구조이거나, 별도 처리되었을 것으로 추정.

---

## 8. 주의사항

### 8.1 핵심 인덱스 불일치 문제 (비정량과 동일한 구조적 결함)

- **현재 홈플러스 JSP는 32개 컬럼을 출력하지만, Java는 이마트 기준 24개(temp[0]~temp[23])만 파싱한다**
- 홈플러스 JSP는 index 0에 `GI_H_ID`, index 1에 `GI_D_ID`, index 2에 `EOI_ID` 출력
- 그러나 Java는 temp[0]을 `GI_D_ID`로 파싱 (이마트 JSP 기준)
- **결과: 모든 컬럼이 밀려서 잘못된 데이터가 매핑됨. 홈플러스 정량 출하대상 조회는 현재 동작 불가 상태**

### 8.2 현재 JSP의 이중 문제

1. **DB 연결**: MSSQL로 변경되었으나 Oracle VIEW `VW_PDA_WID_HOMEPLUS_LIST`를 그대로 참조 → MSSQL에 VIEW 없음 → 쿼리 실행 즉시 오류
2. **컬럼 구조**: 32개 컬럼 구조를 유지하고 있어 Java 파싱 인덱스(0부터 시작하는 이마트 기준 24개)와 불일치

### 8.3 이마트 JSP와의 주요 차이

| 항목 | 이마트(searchType=0) | 홈플러스(searchType=2) |
|------|:-------------------:|:---------------------:|
| 출력 컬럼 수 | 31개 | 32개 |
| 첫 컬럼 | GI_D_ID (index 0) | GI_H_ID (index 0) |
| VIEW 여부 | 직접 JOIN 쿼리 | Oracle VIEW 사용 |
| DB 연결 | getMSSQLConnection() (정상) | getMSSQLConnection() (VIEW 없어 오류) |
| Java 파싱 일치 | O (31개 1:1 매핑) | X (32개 vs 24개, 전부 불일치) |
| MSSQL 전환 상태 | 완료 | 미완료 (VIEW 전환 필요) |
| ORDER BY | GI_D_ID ASC | HOMPLUS_STORE_CODE ASC, PACKER_PRODUCT_CODE ASC, ITEM_NAME ASC, EOI_ID ASC |
| WHERE 조건 | H.마트사구분='7', ITEM_TYPE='W' | ORDER BY에 HOMPLUS_STORE_CODE, EOI_ID 사용 |
| GI_L_ID 포함 | O (index 30) | **X (누락)** |
| STORE_CODE(홈플러스 점포) | STORE_CODE (index 28) | EMARTLOGIS_CODE와 동일값 사용 (STORECODE) |
| WH_AREA, USE_NAME, USE_CODE, CT_NAME | O (index 24~27) | **X (누락)** |
| EMART_PLANT_CODE | O (index 29) | **X (누락)** |

### 8.4 홈플러스 JSP 32개 컬럼 사용/미사용 분류

| JSP Index | 컬럼명 | Java 파싱 | 로컬DB | 이마트 포함 | 판정 |
|:---------:|--------|:---------:|:------:|:----------:|------|
| 0 | GI_H_ID | X | X | X | **미사용** |
| 1 | GI_D_ID | O | O | O (이마트 0) | 사용 필요 |
| 2 | EOI_ID | X | X | X | **미사용** |
| 3 | ITEM_CODE | O | O | O (이마트 1) | 사용 필요 |
| 4 | ITEM_NAME | O | O | O (이마트 2) | 사용 필요 |
| 5 | EMARTITEM_CODE | O | O | O (이마트 3) | 사용 필요 |
| 6 | EMARTITEM | O | O | O (이마트 4) | 사용 필요 |
| 7 | GI_REQ_PKG | O | O | O (이마트 5) | 사용 필요 |
| 8 | GI_REQ_QTY | O | O | O (이마트 6) | 사용 필요 |
| 9 | AMOUNT | X | X | X | **미사용** |
| 10 | GOODS_R_ID | X | X | X | **미사용** |
| 11 | GR_REF_NO | X | X | X | **미사용** |
| 12 | GI_REQ_DATE | O | O | O (이마트 7) | 사용 필요 |
| 13 | BL_NO | O | O | O (이마트 8) | 사용 필요 |
| 14 | BRAND_CODE | O | O | O (이마트 9) | 사용 필요 |
| 15 | BRANDNAME | X | X | X | **미사용** |
| 16 | CLIENT_CODE | O | O | O (이마트 10) | 사용 필요 |
| 17 | CLIENTNAME | O | O | O (이마트 11) | 사용 필요 |
| 18 | CENTERNAME | O | O | O (이마트 12) | 사용 필요 |
| 19 | ITEM_SPEC | O | O | O (이마트 13) | 사용 필요 |
| 20 | CT_CODE | O | O | O (이마트 14) | 사용 필요 |
| 21 | IMPORT_ID_NO | O | O | O (이마트 15) | 사용 필요 |
| 22 | PACKER_CODE | O | O | O (이마트 16) | 사용 필요 |
| 23 | PACKERNAME | X | X | X | **미사용** |
| 24 | PACKER_PRODUCT_CODE | O | O | O (이마트 17) | 사용 필요 |
| 25 | BARCODE_TYPE | O | O | O (이마트 18) | 사용 필요 |
| 26 | ITEM_TYPE | O | O | O (이마트 19) | 사용 필요 |
| 27 | PACKWEIGHT | O | O | O (이마트 20) | 사용 필요 |
| 28 | BARCODEGOODS | O | O | O (이마트 21) | 사용 필요 |
| 29 | STORE_IN_DATE | O | O | O (이마트 22) | 사용 필요 |
| 30 | EMARTLOGIS_CODE | O | O | O (이마트 23) | 사용 필요 |
| 31 | EMARTLOGIS_NAME | X | X | X | **미사용** |
| - | STORE_CODE | O | O | O (이마트 28) | **누락** (홈플러스 점포코드, EMARTLOGIS_CODE와 동일값 가능) |
| - | GI_L_ID | O | O | O (이마트 30) | **누락** (LOT SEQ, 동기화 비교에 필수) |
| - | WH_AREA | O | O | O (이마트 24) | **누락** (홈플러스 불필요시 공백 처리 가능) |
| - | USE_NAME | O | O | O (이마트 25) | **누락** |
| - | USE_CODE | O | O | O (이마트 26) | **누락** |
| - | CT_NAME | O | O | O (이마트 27) | **누락** |
| - | EMART_PLANT_CODE | O | O | O (이마트 29) | **누락** (홈플러스 불필요시 공백 처리 가능) |

**사용 컬럼 22개 / 미사용 컬럼 8개 / 누락 컬럼 7개**

미사용 8개 (삭제 대상): GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME
누락 7개 (추가 대상): STORE_CODE, GI_L_ID, WH_AREA, USE_NAME, USE_CODE, CT_NAME, EMART_PLANT_CODE
전환 결과: 32 - 8 + 7 = **31개** (이마트 JSP와 동일 구조)

### 8.5 GI_L_ID 누락의 심각성

`ProgressDlgShipSearch.java`에서 PDA-서버 동기화 비교 시 `GI_D_ID + GI_L_ID` 복합 키로 비교한다 (309~310줄).
현재 홈플러스 JSP는 `GI_L_ID`를 출력하지 않으므로, Java 파싱 시 `GI_L_ID`가 빈 문자열("")로 저장된다.
이로 인해 동기화 로직이 의도대로 동작하지 않을 수 있다.

### 8.6 해결 방향

**방법 A: 홈플러스 JSP를 이마트 JSP와 동일한 31개 컬럼 구조로 수정 (권장)**
- search_shipment_homeplus.jsp의 out.println을 search_shipment.jsp와 동일한 31개 컬럼으로 변경
- Oracle VIEW 대신 MSSQL 직접 JOIN 쿼리 사용
- Java 파싱 수정 불필요 (이마트와 동일한 공통 파싱 블록 사용)
- 단, HOMPLUS_STORE_CODE 기반 ORDER BY는 MSSQL 쿼리에 반영 필요

**방법 B: Java 파싱에 홈플러스 전용 분기 추가**
- searchType "2"일 때 32개 컬럼 파싱 로직 추가
- JSP 수정 최소화
- Java 코드 변경 필요

---

## 9. TB_SHIPMENT 로컬DB 컬럼 vs JSP 전달 컬럼

### 9.1 TB_SHIPMENT 로컬DB 컬럼 목록 (DBHandler.java:32~68줄)

| 로컬DB 컬럼 | NOT NULL | JSP 전달 여부 | Java 파싱 여부 | 비고 |
|------------|:--------:|:------------:|:-------------:|------|
| SHIPMENT_ID | PK AUTO | - | - | 자동증가 |
| GI_D_ID | O | index 1 | O | 키 컬럼 |
| GI_L_ID | O | **X (미전달)** | **X** | 동기화 키 누락 |
| ITEM_CODE | O | index 3 | O | |
| ITEM_NAME | O | index 4 | O | |
| EMARTITEM_CODE | - | index 5 | O | |
| EMARTITEM | - | index 6 | O | |
| GI_REQ_PKG | O | index 7 | O | |
| GI_REQ_QTY | O | index 8 | O | |
| GI_REQ_DATE | O | index 12 | O | |
| BL_NO | O | index 13 | O | |
| BRAND_CODE | O | index 14 | O | |
| CLIENT_CODE | O | index 16 | O | |
| CLIENTNAME | O | index 17 | O | |
| CENTERNAME | - | index 18 | O | |
| ITEM_SPEC | O | index 19 | O | |
| CT_CODE | O | index 20 | O | |
| IMPORT_ID_NO | O | index 21 | O | |
| PACKER_CODE | O | index 22 | O | |
| PACKER_PRODUCT_CODE | O | index 24 | O | |
| BARCODE_TYPE | O | index 25 | O | |
| ITEM_TYPE | O | index 26 | O | |
| PACKWEIGHT | - | index 27 | O | |
| BARCODEGOODS | - | index 28 | O | |
| STORE_IN_DATE | - | index 29 | O | |
| EMARTLOGIS_CODE | - | index 30 | O | |
| SAVE_TYPE | O | - | 하드코딩 "F" | |
| WH_AREA | - | **X (미전달)** | **X** | 홈플러스 추가 필요 |
| USE_NAME | - | **X (미전달)** | **X** | 홈플러스 추가 필요 |
| USE_CODE | - | **X (미전달)** | **X** | 홈플러스 추가 필요 |
| CT_NAME | - | **X (미전달)** | **X** | 홈플러스 추가 필요 |
| STORE_CODE | - | **X (미전달)** | **X** | 홈플러스 점포코드 누락 |
| EMART_PLANT_CODE | - | **X (미전달)** | **X** | 홈플러스 미사용 가능 |
| LAST_BOX_ORDER | - | - | - | 롯데 전용 |

---

## 10. 관련 문서

- `app/doc/소스분석/40_비정량_출하계근대상받기_JSP_Java파싱_인덱스분석.md` — 동일 구조 비정량(searchType=4) 분석 문서
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`
- `app/doc/소스분석/48_8개_출하대상받기_VIEW_WHERE조건_비교.md`
- `app/doc/소스분석/49_출하대상받기_4유형_조회조건_종합정리.md`
- `app/doc/view/VW_PDA_WID_HOMEPLUS_LIST` — Oracle VIEW DDL
- `app/doc/소스분석/27_ProgressDlgShipSearch.md`
