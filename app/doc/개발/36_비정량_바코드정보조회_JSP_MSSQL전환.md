# 비정량(searchType=4) 바코드정보조회 JSP MSSQL 전환

**작성일**: 2026-04-13
**목적**: 비정량 JSP(`search_barcode_info_nonfixed.jsp`)를 Oracle `B_ITEM` 테이블 기반 쿼리에서 MSSQL `CO_품목코드` 테이블 기반 쿼리로 전환한다. Java 파싱(`ProgressDlgBarcodeSearch.java`)은 수정하지 않으며, out.println 24개 컬럼 순서·별칭이 현재와 완전히 동일해야 한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### search_barcode_info_nonfixed.jsp (비정량 JSP, 전환 대상)

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp`

```jsp
// DB 접속: getMSSQLConnection() — 이미 MSSQL 접속으로 전환됨
// SELECT 쿼리: B_ITEM 테이블 사용 — MSSQL에 존재하지 않아 실행 불가
String quertystring = "SELECT '이마트용' as PACKER_CLIENT_CODE"
    + ", sbi.ITEM_CODE as PACKER_PRODUCT_CODE"
    + ", sbi.ITEM_NAME_KR as PACKER_PRD_NAME"
    + ", sbi.ITEM_CODE as ITEMCODE"
    + ", sbi.ITEM_NAME_KR as ITEM_NAME_KR"
    + ", '0000' as BRAND_CODE"
    + ", sbi.ITEM_CODE as BARCODEGOODS"
    + ", 'KG' as BASEUNIT"
    + ", SBI.ZEROPOINT"
    + ", SBI.PACKER_PRD_CODE_FROM"
    + ", SBI.PACKER_PRD_CODE_TO"
    + ", SBI.BARCODEGOODS_FROM"
    + ", SBI.BARCODEGOODS_TO"
    + ", SBI.WEIGHT_FROM"
    + ", SBI.WEIGHT_TO"
    + ", SBI.MAKINGDATE_FROM"
    + ", SBI.MAKINGDATE_TO"
    + ", SBI.BOXSERIAL_FROM"
    + ", SBI.BOXSERIAL_TO"
    + ", SBI.STATUS"
    + ", SBI.REG_ID"
    + ", SBI.REG_DATE"
    + ", SBI.REG_TIME"
    + ", '0000' AS memo"
    + " FROM B_ITEM sbi"
    + qry_where                            // Java에서: WHERE SBI.ITEM_CODE = '...' OR ...
    + " ORDER BY ITEMCODE ASC";
```

- out.println 24개 컬럼 (`::` 구분자, `;;` 행 구분)
- Java(`ProgressDlgBarcodeSearch.java`) → `temp[0]`~`temp[23]` 파싱

### 문제점

1. **MSSQL에 B_ITEM 테이블이 존재하지 않음** — DB 접속은 MSSQL로 전환됐으나 쿼리는 Oracle 전용 B_ITEM 사용 중
2. **WHERE 조건 컬럼명 불일치** — Java에서 `SBI.ITEM_CODE` 조건을 전송하지만 CO_품목코드의 실제 컬럼명은 `품목코드` → 컬럼 별칭 또는 qry_where 대체 처리 필요
3. **B_ITEM 컬럼 일부가 CO_품목코드에 없음** — `PACKER_PRD_CODE_FROM`, `PACKER_PRD_CODE_TO`, `REG_ID`, `REG_DATE`, `REG_TIME`은 CO_품목코드 Entity에 해당 컬럼 없음 → 빈값('') 고정 처리 필요
4. **SHELF_LIFE 미전송** — 비정량(searchType=4)은 Java에서 SHELF_LIFE를 파싱하지 않으므로 JSP에서도 출력하지 않음 (이마트 JSP와 달리 25번째 컬럼 없음)

---

## 2. 변경 구조

### 데이터 흐름

```
[Java: ProgressDlgBarcodeSearch.java]
  searchType=4 → selectqueryCodeListForNonFixed() → ITEM_CODE 목록 수집
  WHERE 조건 구성: "WHERE SBI.ITEM_CODE = 'CODE1' OR SBI.ITEM_CODE = 'CODE2' ..."
    ↓ HTTP POST (data 파라미터)
[JSP: search_barcode_info_nonfixed.jsp]
  변경 전: FROM B_ITEM SBI + WHERE SBI.ITEM_CODE = '...'  (Oracle 전용, MSSQL 실행 불가)
  변경 후: FROM CO_품목코드 SBI + qry_where 내 ITEM_CODE → 품목코드 replace 처리
    ↓ out.println 24개 컬럼 (순서 동일 유지)
[Java: ProgressDlgBarcodeSearch.java]
  temp[0]~temp[23] 파싱 → DBHandler.insertqueryBarcodeInfo() → TB_BARCODE_INFO INSERT
```

### 변경 전/후 테이블 비교

| 항목 | 변경 전 (Oracle B_ITEM) | 변경 후 (MSSQL CO_품목코드) |
|------|------------------------|---------------------------|
| FROM 테이블 | `B_ITEM sbi` | `CO_품목코드 SBI` |
| WHERE 컬럼 | `SBI.ITEM_CODE` | `SBI.품목코드` (qry_where replace) |
| ORDER BY | `ITEMCODE ASC` | `ITEMCODE ASC` (별칭 동일 유지) |
| 컬럼 수 (out.println) | 24개 | 24개 (동일) |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_barcode_info_nonfixed.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\` | SELECT 쿼리 B_ITEM → CO_품목코드 전환, qry_where ITEM_CODE→품목코드 replace |

**수정 불필요 파일**:
- `ProgressDlgBarcodeSearch.java` — Java 파싱 코드 수정 불필요 (temp[0]~temp[23] 구조 동일)
- `DBHandler.java` — TB_BARCODE_INFO INSERT 구조 변경 없음
- `Barcodes_Info.java` — 모델 변경 없음

---

## 4. 수정 상세

### 4.1 search_barcode_info_nonfixed.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp`

**변경 전:**

```jsp
 //SQL 
  Statement stmt = conn.createStatement();
    String quertystring = "SELECT '이마트용' as PACKER_CLIENT_CODE"
        + ", sbi.ITEM_CODE as PACKER_PRODUCT_CODE"
        + ", sbi.ITEM_NAME_KR as PACKER_PRD_NAME"
        + ", sbi.ITEM_CODE as ITEMCODE"
        + ", sbi.ITEM_NAME_KR as ITEM_NAME_KR"
        + ", '0000' as BRAND_CODE"
        + ", sbi.ITEM_CODE as BARCODEGOODS"
        + ", 'KG' as BASEUNIT"
        + ", SBI.ZEROPOINT"
        + ", SBI.PACKER_PRD_CODE_FROM"
        + ", SBI.PACKER_PRD_CODE_TO"
        + ", SBI.BARCODEGOODS_FROM"
        + ", SBI.BARCODEGOODS_TO"
        + ", SBI.WEIGHT_FROM"
        + ", SBI.WEIGHT_TO"
        + ", SBI.MAKINGDATE_FROM"
        + ", SBI.MAKINGDATE_TO"
        + ", SBI.BOXSERIAL_FROM"
        + ", SBI.BOXSERIAL_TO"
        + ", SBI.STATUS"
        + ", SBI.REG_ID"
        + ", SBI.REG_DATE"
        + ", SBI.REG_TIME"
        + ", '0000' AS memo"
        + " FROM B_ITEM sbi"
        + qry_where
        + " ORDER BY ITEMCODE ASC";
```

**변경 후:**

```jsp
 //SQL 
  Statement stmt = conn.createStatement();
  // Java(ProgressDlgBarcodeSearch.java)에서 WHERE SBI.ITEM_CODE = '...' 형태로 전송
  // CO_품목코드의 실제 컬럼명은 '품목코드'이므로 ITEM_CODE → 품목코드로 치환
  qry_where = qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드");
    String quertystring = "SELECT '이마트용' as PACKER_CLIENT_CODE"
        + ", SBI.품목코드 as PACKER_PRODUCT_CODE"
        + ", SBI.품목명 as PACKER_PRD_NAME"
        + ", SBI.품목코드 as ITEMCODE"
        + ", SBI.품목명 as ITEM_NAME_KR"
        + ", '0000' as BRAND_CODE"
        + ", SBI.품목코드 as BARCODEGOODS"
        + ", 'KG' as BASEUNIT"
        + ", SBI.소수점 AS ZEROPOINT"
        + ", '' AS PACKER_PRD_CODE_FROM"
        + ", '' AS PACKER_PRD_CODE_TO"
        + ", SBI.바코드상품코드시작 AS BARCODEGOODS_FROM"
        + ", SBI.바코드상품코드끝 AS BARCODEGOODS_TO"
        + ", SBI.중량시작 AS WEIGHT_FROM"
        + ", SBI.중량끝 AS WEIGHT_TO"
        + ", SBI.제조일자시작 AS MAKINGDATE_FROM"
        + ", SBI.제조일자끝 AS MAKINGDATE_TO"
        + ", SBI.박스시리얼시작 AS BOXSERIAL_FROM"
        + ", SBI.박스시리얼끝 AS BOXSERIAL_TO"
        + ", SBI.생산품상태 AS STATUS"
        + ", '' AS REG_ID"
        + ", '' AS REG_DATE"
        + ", '' AS REG_TIME"
        + ", '0000' AS memo"
        + " FROM CO_품목코드 SBI"
        + qry_where
        + " ORDER BY ITEMCODE ASC";
```

**검증**: out.println 컬럼 번호(1~24)와 Java temp[0]~temp[23] 매핑이 동일함을 확인

---

## 5. 사이드이펙트

### ProgressDlgBarcodeSearch.java (수정 없음, 영향 분석)

```java
// searchType=4 분기 — search_barcode_info_nonfixed.jsp 호출
}else if(Common.searchType.equals("4")) {
    receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_barcode_info",
        Common.URL_SEARCH_BARCODE_INFO_NONFIXED);
}
```

- JSP 출력 컬럼 순서/별칭이 동일하게 유지되므로 Java 파싱 코드 변경 불필요
- `temp[0]`~`temp[23]` 24개 항목 구조 유지됨
- SHELF_LIFE(temp[24])는 searchType=4일 때 파싱하지 않으므로 JSP의 24컬럼 출력 유지

### search_barcode_info_nonfixed.jsp 호출 분기

- searchType=4 (비정량)만 이 JSP를 호출함
- searchType=5 (홈플러스비정량)는 `URL_SEARCH_HOMEPLUS_NONFIXED2` 를 호출하므로 영향 없음
- searchType=0,1,2,3,6,7은 `search_barcode_info.jsp`(이마트)를 호출하므로 영향 없음

---

## 6. 데이터 저장 구조

### 컬럼 매핑: Oracle B_ITEM → MSSQL CO_품목코드

| Index | SELECT 별칭 | Oracle B_ITEM 컬럼 | MSSQL CO_품목코드 컬럼 | 비고 |
|:-----:|:----------:|--------------------|----------------------|------|
| 0 | PACKER_CLIENT_CODE | `'이마트용'` (고정) | `'이마트용'` (고정) | 변경 없음 |
| 1 | PACKER_PRODUCT_CODE | `sbi.ITEM_CODE` | `SBI.품목코드` | 컬럼명 변경 |
| 2 | PACKER_PRD_NAME | `sbi.ITEM_NAME_KR` | `SBI.품목명` | 컬럼명 변경 |
| 3 | ITEMCODE | `sbi.ITEM_CODE` | `SBI.품목코드` | 컬럼명 변경 |
| 4 | ITEM_NAME_KR | `sbi.ITEM_NAME_KR` | `SBI.품목명` | 컬럼명 변경 |
| 5 | BRAND_CODE | `'0000'` (고정) | `'0000'` (고정) | 변경 없음 |
| 6 | BARCODEGOODS | `sbi.ITEM_CODE` | `SBI.품목코드` | 컬럼명 변경 |
| 7 | BASEUNIT | `'KG'` (고정) | `'KG'` (고정) | 변경 없음 |
| 8 | ZEROPOINT | `SBI.ZEROPOINT` | `SBI.소수점` | 컬럼명 변경 |
| 9 | PACKER_PRD_CODE_FROM | `SBI.PACKER_PRD_CODE_FROM` | `''` (빈값) | CO_품목코드에 해당 컬럼 없음 |
| 10 | PACKER_PRD_CODE_TO | `SBI.PACKER_PRD_CODE_TO` | `''` (빈값) | CO_품목코드에 해당 컬럼 없음 |
| 11 | BARCODEGOODS_FROM | `SBI.BARCODEGOODS_FROM` | `SBI.바코드상품코드시작` | 컬럼명 변경 |
| 12 | BARCODEGOODS_TO | `SBI.BARCODEGOODS_TO` | `SBI.바코드상품코드끝` | 컬럼명 변경 |
| 13 | WEIGHT_FROM | `SBI.WEIGHT_FROM` | `SBI.중량시작` | 컬럼명 변경 |
| 14 | WEIGHT_TO | `SBI.WEIGHT_TO` | `SBI.중량끝` | 컬럼명 변경 |
| 15 | MAKINGDATE_FROM | `SBI.MAKINGDATE_FROM` | `SBI.제조일자시작` | 컬럼명 변경 |
| 16 | MAKINGDATE_TO | `SBI.MAKINGDATE_TO` | `SBI.제조일자끝` | 컬럼명 변경 |
| 17 | BOXSERIAL_FROM | `SBI.BOXSERIAL_FROM` | `SBI.박스시리얼시작` | 컬럼명 변경 |
| 18 | BOXSERIAL_TO | `SBI.BOXSERIAL_TO` | `SBI.박스시리얼끝` | 컬럼명 변경 |
| 19 | STATUS | `SBI.STATUS` | `SBI.생산품상태` | 컬럼명 변경 |
| 20 | REG_ID | `SBI.REG_ID` | `''` (빈값) | CO_품목코드에 해당 컬럼 없음 |
| 21 | REG_DATE | `SBI.REG_DATE` | `''` (빈값) | CO_품목코드에 해당 컬럼 없음 |
| 22 | REG_TIME | `SBI.REG_TIME` | `''` (빈값) | CO_품목코드에 해당 컬럼 없음 |
| 23 | memo | `'0000'` (고정) | `'0000'` (고정) | 변경 없음 |
| - | SHELF_LIFE | 없음 (JSP 미전송) | 없음 (JSP 미전송) | searchType=4 미파싱, 유지 |

### 비정량 고정값 (이마트 JSP와 차이점)

| 컬럼 | 비정량 고정값 | 이마트 JSP 동작 | 이유 |
|------|:----------:|:----------:|------|
| PACKER_CLIENT_CODE | `'이마트용'` | `패커코드` 컬럼값 | 비정량 전용 고정값 |
| PACKER_PRODUCT_CODE | `품목코드` | `ppCode` 컬럼값 | 비정량은 품목코드를 PPCODE로 사용 |
| BARCODEGOODS | `품목코드` | `상품바코드` 컬럼값 | 비정량 바코드 매칭 기준 |
| BASEUNIT | `'KG'` | `기준단위` 컬럼값 | 비정량은 항상 KG 단위 |
| SHELF_LIFE | 미전송 (24컬럼) | 전송 (25컬럼) | Java L131-133: searchType=4,5이면 미파싱 |

### WHERE 조건 처리 (핵심)

Java에서 전송하는 WHERE 조건:
```
WHERE SBI.ITEM_CODE = 'CODE001' OR SBI.ITEM_CODE = 'CODE002' ...
```

CO_품목코드 실제 컬럼명: `품목코드` (MSSQL 한글 컬럼)

JSP 내 replace 처리로 해결:
```jsp
qry_where = qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드");
```

Java 파싱 코드 수정 없이 처리 가능. 이마트 JSP와의 차이:
- 이마트: Java가 `SBI.PPCODE` 전송 → CO_품목코드 `PPCODE` 컬럼 직접 매핑 (replace 불필요)
- 비정량: Java가 `SBI.ITEM_CODE` 전송 → CO_품목코드 `품목코드` 컬럼으로 replace 필요

### 인덱스 매핑 (Java temp 배열)

```
temp[0]  = PACKER_CLIENT_CODE  ← '이마트용' 고정
temp[1]  = PACKER_PRODUCT_CODE ← SBI.품목코드
temp[2]  = PACKER_PRD_NAME     ← SBI.품목명
temp[3]  = ITEMCODE            ← SBI.품목코드
temp[4]  = ITEM_NAME_KR        ← SBI.품목명
temp[5]  = BRAND_CODE          ← '0000' 고정
temp[6]  = BARCODEGOODS        ← SBI.품목코드
temp[7]  = BASEUNIT            ← 'KG' 고정
temp[8]  = ZEROPOINT           ← SBI.소수점
temp[9]  = PACKER_PRD_CODE_FROM ← '' 빈값 (CO_품목코드 미지원)
temp[10] = PACKER_PRD_CODE_TO  ← '' 빈값 (CO_품목코드 미지원)
temp[11] = BARCODEGOODS_FROM   ← SBI.바코드상품코드시작
temp[12] = BARCODEGOODS_TO     ← SBI.바코드상품코드끝
temp[13] = WEIGHT_FROM         ← SBI.중량시작
temp[14] = WEIGHT_TO           ← SBI.중량끝
temp[15] = MAKINGDATE_FROM     ← SBI.제조일자시작
temp[16] = MAKINGDATE_TO       ← SBI.제조일자끝
temp[17] = BOXSERIAL_FROM      ← SBI.박스시리얼시작
temp[18] = BOXSERIAL_TO        ← SBI.박스시리얼끝
temp[19] = STATUS              ← SBI.생산품상태
temp[20] = REG_ID              ← '' 빈값
temp[21] = REG_DATE            ← '' 빈값
temp[22] = REG_TIME            ← '' 빈값
temp[23] = MEMO                ← '0000' 고정
-- SHELF_LIFE 없음 (searchType=4 미파싱, Java L131-133)
```

---

## 7. 호출 시점

```
[ProgressDlgBarcodeSearch.doInBackground()]
    ↓
selectqueryCodeListForNonFixed() — TB_SHIPMENT에서 ITEM_CODE 목록 추출
    ↓
WHERE 조건 구성: "WHERE SBI.ITEM_CODE = 'CODE1' OR ..."
    ↓
sendDataDb(data, "inno", "search_barcode_info", URL_SEARCH_BARCODE_INFO_NONFIXED)
    ↓ HTTP
[search_barcode_info_nonfixed.jsp]
    ↓
qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드")
    ↓
SELECT FROM CO_품목코드 SBI WHERE SBI.품목코드 = 'CODE1' OR ...
    ↓
out.println 24개 컬럼 ("::" 구분, ";;" 행 구분)
    ↓ HTTP 응답
[ProgressDlgBarcodeSearch.doInBackground()]
    ↓
split(";;") → split("::", -1) → temp[0]~temp[23]
    ↓
DBHandler.insertqueryBarcodeInfo() → TB_BARCODE_INFO INSERT
    ↓
onPostExecute() → ProgressDlgGoodsWetSearch 실행
```

---

## 8. 개발 플랜

### Step 1: Oracle B_ITEM → MSSQL CO_품목코드 SELECT 쿼리 전환

**Part 1. 분석**
- 메서드: JSP quertystring 문자열 구성 (34~61행)
- 범위: `search_barcode_info_nonfixed.jsp` 전체
- 용도: B_ITEM 기반 24개 컬럼 SELECT를 CO_품목코드 기반으로 전환
- 주의할 점:
  - out.println 컬럼 수 반드시 24개 유지 (이마트 JSP는 25개 — SHELF_LIFE 포함)
  - Java 전송 WHERE 조건 `SBI.ITEM_CODE` → `SBI.품목코드` replace 처리 필요 (Java 수정 불가)
  - PACKER_PRD_CODE_FROM / PACKER_PRD_CODE_TO는 CO_품목코드에 없으므로 `''` 빈값 고정
  - REG_ID / REG_DATE / REG_TIME도 CO_품목코드에 없으므로 `''` 빈값 고정
  - BASEUNIT은 비정량 특성상 `'KG'` 고정 유지 (CO_품목코드.기준단위 사용 금지)
  - PACKER_CLIENT_CODE는 `'이마트용'` 고정 유지

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | FROM 절 | L59 | `B_ITEM sbi` → `CO_품목코드 SBI` |
| 2 | qry_where 치환 | L34 이전 | `qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드")` 추가 |
| 3 | ITEM_CODE 컬럼 | L36,38,41 | `sbi.ITEM_CODE` → `SBI.품목코드` |
| 4 | ITEM_NAME_KR 컬럼 | L37,39 | `sbi.ITEM_NAME_KR` → `SBI.품목명` |
| 5 | ZEROPOINT | L43 | `SBI.ZEROPOINT` → `SBI.소수점 AS ZEROPOINT` |
| 6 | PACKER_PRD_CODE_FROM | L44 | `SBI.PACKER_PRD_CODE_FROM` → `'' AS PACKER_PRD_CODE_FROM` |
| 7 | PACKER_PRD_CODE_TO | L45 | `SBI.PACKER_PRD_CODE_TO` → `'' AS PACKER_PRD_CODE_TO` |
| 8 | BARCODEGOODS_FROM | L46 | `SBI.BARCODEGOODS_FROM` → `SBI.바코드상품코드시작 AS BARCODEGOODS_FROM` |
| 9 | BARCODEGOODS_TO | L47 | `SBI.BARCODEGOODS_TO` → `SBI.바코드상품코드끝 AS BARCODEGOODS_TO` |
| 10 | WEIGHT_FROM | L48 | `SBI.WEIGHT_FROM` → `SBI.중량시작 AS WEIGHT_FROM` |
| 11 | WEIGHT_TO | L49 | `SBI.WEIGHT_TO` → `SBI.중량끝 AS WEIGHT_TO` |
| 12 | MAKINGDATE_FROM | L50 | `SBI.MAKINGDATE_FROM` → `SBI.제조일자시작 AS MAKINGDATE_FROM` |
| 13 | MAKINGDATE_TO | L51 | `SBI.MAKINGDATE_TO` → `SBI.제조일자끝 AS MAKINGDATE_TO` |
| 14 | BOXSERIAL_FROM | L52 | `SBI.BOXSERIAL_FROM` → `SBI.박스시리얼시작 AS BOXSERIAL_FROM` |
| 15 | BOXSERIAL_TO | L53 | `SBI.BOXSERIAL_TO` → `SBI.박스시리얼끝 AS BOXSERIAL_TO` |
| 16 | STATUS | L54 | `SBI.STATUS` → `SBI.생산품상태 AS STATUS` |
| 17 | REG_ID | L55 | `SBI.REG_ID` → `'' AS REG_ID` |
| 18 | REG_DATE | L56 | `SBI.REG_DATE` → `'' AS REG_DATE` |
| 19 | REG_TIME | L57 | `SBI.REG_TIME` → `'' AS REG_TIME` |

**Part 2. 변환 계획**
- 변환 방식: JSP의 쿼리 문자열 내 B_ITEM 컬럼명 → CO_품목코드 컬럼명으로 직접 교체
- qry_where replace는 쿼리 문자열 구성 직전에 수행
- ORDER BY `ITEMCODE ASC`는 별칭 기준이므로 변경 불필요
- 주의사항: out.println의 컬럼 카운트(rsmd.getColumnName 1~24)가 변경 전과 동일해야 함

**체크리스트**
- [x] Part 1: B_ITEM 컬럼 vs CO_품목코드 컬럼 19개 매핑 분석 완료 (소스분석46 + 11장 사전 시뮬레이션)
- [x] Part 2: qry_where replace 방식 확인 (사전 시뮬레이션 샘플 추적 PASS)
- [x] Part 3: JSP 쿼리 문자열 전환 수행 (2026-04-22)
- [ ] Part 4: JSP Tomcat 재시작 후 문법 오류 없음 확인 (실기기 테스트 시 수행)
- [ ] Part 5: 단위테스트 (MSSQL에서 SELECT 직접 실행) (실기기 테스트 시 수행)
- [x] Part 6: 회귀테스트 없음 (비정량 단독 JSP) (Step 3에서 이마트 회귀 테스트만 별도 수행)

**Part 6. 변경 내용** (완료):
- **무엇을**: `search_barcode_info_nonfixed.jsp`의 SELECT 쿼리를 Oracle `B_ITEM` 기반 → MSSQL `CO_품목코드` 기반으로 전환. 쿼리 조립 직전 `qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드")` 추가.
- **왜**: DB 접속은 이미 MSSQL로 전환됐으나 SELECT 쿼리가 Oracle 전용 B_ITEM 테이블을 사용하여 실행 불가 상태. CO_품목코드의 한글 컬럼명에 맞춰 19개 컬럼을 매핑하고, Java 측 전송 조건(`SBI.ITEM_CODE`)을 CO_품목코드 실컬럼명(`품목코드`)으로 치환하는 replace 처리가 필요.
- **어떻게**: 
  1. 쿼리 조립 직전 `qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드")` 1줄 추가
  2. FROM 절: `B_ITEM sbi` → `CO_품목코드 SBI`
  3. 컬럼명 전환 11건: 품목명, 소수점, 바코드상품코드시작/끝, 중량시작/끝, 제조일자시작/끝, 박스시리얼시작/끝, 생산품상태
  4. 품목코드 재사용 3건(PACKER_PRODUCT_CODE, ITEMCODE, BARCODEGOODS): `sbi.ITEM_CODE` → `SBI.품목코드`
  5. CO_품목코드에 없는 5개 컬럼(PACKER_PRD_CODE_FROM/TO, REG_ID/DATE/TIME): `''` 빈값 고정
  6. 하드코딩 4건(`'이마트용'`, `'0000'` BRAND_CODE, `'KG'`, `'0000'` memo) 유지
  7. ORDER BY `ITEMCODE ASC` 유지
  8. out.println 블록(L76~83) 및 기타 블록 수정 없음
- **검증**: ⑤ code-verifier(6항목 PASS) + ⑥ original-comparator(전 항목 원본 동일/허용차이) 모두 COMMIT OK 판정

---

### Step 2: out.println 컬럼 수/순서 검증

**Part 1. 분석**
- 메서드: JSP while(rs.next()) out.println 블록 (71~80행)
- 범위: `search_barcode_info_nonfixed.jsp` L71~L80
- 용도: 변경 후 out.println 출력 컬럼 수가 24개로 동일한지 확인
- 주의할 점:
  - 현재 코드는 `rsmd.getColumnName(1)`~`rsmd.getColumnName(24)` 24개 출력
  - 이마트 JSP는 25개 (`rsmd.getColumnName(25)` SHELF_LIFE 포함) — 비정량은 24개 유지
  - Java `temp[0]`~`temp[23]` 파싱 코드와 완전히 일치해야 함

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | out.println 컬럼 수 | L71-80 | rsmd.getColumnName(1)~(24) 24개 확인 |
| 2 | 이마트 JSP 비교 | search_barcode_info.jsp L76-83 | 이마트는 (25)까지 — 비정량은 (24)까지가 정상 |
| 3 | Java 파싱 코드 | ProgressDlgBarcodeSearch.java L107-130 | temp[0]~temp[23] 매핑 일치 확인 |

**Part 2. 변환 계획**
- 변환 방식: 확인 후 out.println 블록은 수정 없음 (현재 24개 유지)
- 주의사항: SELECT에 컬럼이 24개임을 rsmd.getColumnCount()로 확인하여 검증

**체크리스트**
- [x] Part 1: out.println 컬럼 수 24개 확인 (수정 후 JSP L76~83: `rsmd.getColumnName(1)~(24)`)
- [x] Part 2: 이마트 JSP(25개)와의 차이 확인 (이마트 L83에 `getColumnName(25)` 존재, 비정량은 없음)
- [x] Part 3: Java temp[] 매핑 최종 일치 확인 (ProgressDlgBarcodeSearch.java L107~131 temp[0]~temp[23] 1:1, L132~134에서 searchType=4 분기로 temp[24] 스킵)
- [ ] Part 4: 컴파일 확인 (실기기 테스트 시 수행 — Step 3 통합 테스트로 이동)
- [ ] Part 5: 단위테스트 (실기기 테스트 시 수행 — Step 3 통합 테스트로 이동)
- [ ] Part 6: 회귀테스트 (실기기 테스트 시 수행 — Step 3 통합 테스트로 이동)

**Part 6. 변경 내용** (완료):
- **무엇을**: `search_barcode_info_nonfixed.jsp` L76~83의 `out.println` 블록이 24개 컬럼을 출력하는지 검증. 이마트 JSP의 25개(SHELF_LIFE 포함)와의 구조적 차이를 재확인.
- **왜**: Step 1에서 SELECT 쿼리를 24컬럼으로 수정했으므로, `out.println`이 참조하는 `rsmd.getColumnName(N)`의 N 범위가 실제 SELECT 컬럼 수와 일치하는지 확인이 필요. Java의 `searchType=4` 분기가 `temp[24]` 접근을 스킵하도록 설계되어 있으므로 비정량은 반드시 24개 유지.
- **어떻게**:
  1. 비정량 JSP L76~83 확인: `getColumnName(1)` ~ `getColumnName(24)` + `";;"` 행 구분자 → 24개 정상
  2. 이마트 JSP L76~83 확인: `getColumnName(1)` ~ `getColumnName(25)` → 25개 (SHELF_LIFE 포함)
  3. Java `ProgressDlgBarcodeSearch.java` L108~131 setter 확인: temp[0]~temp[23] 24개
  4. Java L132~134 `if (!searchType.equals("4") && !searchType.equals("5"))` 분기 확인: 비정량은 `temp[24]` 접근 없음
  5. out.println 블록은 코드 변경 없이 기존 구조 유지 — Step 1에서 SELECT 쿼리를 24개로 맞춰 쓴 것만으로 검증 완료
- **검증**: 본 검증은 정적 코드 구조 확인이며, 컴파일/단위테스트/회귀테스트(Part 4~6)는 실기기 환경 의존 작업이므로 Step 3 통합 테스트 단계로 이동

---

### Step 3: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 비정량(searchType=4) 출하대상 조회 성공 후 바코드정보 조회 자동 실행 | [x] |
| 2 | JSP Tomcat 로그에 SELECT 쿼리 출력 확인 (System.out.println) | [x] |
| 3 | 응답 데이터가 24개 컬럼 "::" 구분, ";;" 행 구분으로 수신됨 | [x] |
| 4 | temp[0] = '이마트용' 확인 | [x] |
| 5 | temp[1] = 품목코드 값 확인 | [x] |
| 6 | temp[4] = 품목명 값 확인 | [x] |
| 7 | temp[6] = 품목코드 값 (BARCODEGOODS) 확인 | [x] |
| 8 | temp[7] = 'KG' 확인 | [x] |
| 9 | temp[8] = 소수점 숫자값 확인 | [x] |
| 10 | temp[9] = '' (빈값, PACKER_PRD_CODE_FROM) 확인 | [x] |
| 11 | temp[11]~temp[18] = 바코드 파싱 위치 숫자값 확인 | [x] |
| 12 | temp[23] = '0000' (memo) 확인 | [x] |
| 13 | TB_BARCODE_INFO에 정상 INSERT 확인 (DBHandler.insertqueryBarcodeInfo) | [x] |
| 14 | 바코드 스캔 시 find_work_info() 정상 매칭 확인 | [x] |
| 15 | 계근 후 서버 전송 패킷에 PACKER_CLIENT_CODE='이마트용' 포함 확인 | [x] |

---

### 개발 순서 요약

```
Step 1: B_ITEM → CO_품목코드 SELECT 쿼리 전환 (JSP 수정)
    ↓
Step 2: out.println 24개 컬럼 수/순서 검증
    ↓
Step 3: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 비정량 바코드정보 조회 전체 흐름

```
1. MainActivity → searchType=4 (비정량) 선택
2. 출하대상 조회 실행 (search_production_nonfixed.jsp) → TB_SHIPMENT에 ITEM_CODE 저장
3. ProgressDlgBarcodeSearch 자동 실행
4. selectqueryCodeListForNonFixed() → TB_SHIPMENT에서 ITEM_CODE 목록 추출
5. WHERE 조건 구성: "WHERE SBI.ITEM_CODE = 'XXXXXX' OR ..."
6. search_barcode_info_nonfixed.jsp 호출
7. JSP 내부: qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드")
8. CO_품목코드 테이블에서 SELECT 실행
9. out.println 24컬럼 응답 수신
10. temp[0]~temp[23] 파싱 → TB_BARCODE_INFO INSERT 확인
```

### 시나리오 2: Java 파싱 검증 (temp[] 인덱스 확인)

```
1. Tomcat 로그에서 JSP 출력 데이터 확인
   - 형식: '이마트용'::품목코드::품목명::품목코드::품목명::'0000'::품목코드::'KG'::소수점값::''::''::...
2. "::" 기준으로 split 시 24개 토큰 확인
3. temp[0]='이마트용', temp[7]='KG', temp[23]='0000' 고정값 확인
4. temp[8] (ZEROPOINT) = 정수값 (예: 1, 2 등) 확인
5. temp[11]~temp[18] (바코드 파싱 위치) = 숫자 문자열 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | WHERE 조건 실행 오류: `SBI.ITEM_CODE` 컬럼 없음 | CO_품목코드에 ITEM_CODE 컬럼 없음, 실제 컬럼명은 `품목코드` | JSP에서 `qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드")` 처리 |
| 2 | ZEROPOINT NULL 반환 | CO_품목코드.소수점이 NULL인 경우 | ISNULL(SBI.소수점, 0) 적용 검토 (데이터 현황 확인 후 결정) |
| 3 | 바코드 파싱 위치 NULL 반환 | 바코드상품코드시작/끝 등 컬럼이 NULL인 경우 | ISNULL(SBI.바코드상품코드시작, '') 적용 검토 |
| 4 | out.println 컬럼 수 25개로 증가 | 이마트 JSP(SHELF_LIFE 포함)를 그대로 복사할 경우 | 비정량 JSP는 24컬럼 유지 (SHELF_LIFE 미포함) |
| 5 | CO_품목코드에 해당 ITEM_CODE 데이터 없음 | 출하대상의 품목코드가 CO_품목코드에 미등록 | 데이터 등록 여부 확인 (운영 환경 문제, JSP 코드와 무관) |
| 6 | PACKER_PRD_CODE_FROM/TO 값 사용 불가 | CO_품목코드에 해당 컬럼 없어 빈값 처리 | 소스분석45에 따르면 이 두 컬럼은 Log 출력만 사용(get 미호출), 기능 영향 없음 |

---

## 11. 사전 시뮬레이션 결과 (⑤ code-verifier)

**실행 일자**: 2026-04-22
**검증 단계**: 코드 수정 **전** 사전 시뮬레이션 (실제 JSP 파일 미수정)
**최종 판정**: ✅ **GO** (착수 가능)

### 11.1 검증 항목별 결과

| # | 검증 항목 | 결과 | 핵심 발견 |
|:-:|----------|:----:|----------|
| 1 | CO_품목코드 스키마 대조 (12컬럼) | ✅ PASS | ItemCodeEntity.java(L100~640)에 품목코드·품목명·소수점·바코드상품코드시작/끝·중량시작/끝·제조일자시작/끝·박스시리얼시작/끝·생산품상태 전부 존재 |
| 2 | Java temp[] 파싱 인덱스 (24개 1:1) | ✅ PASS | JSP 출력 24컬럼 순서와 `ProgressDlgBarcodeSearch.java` L107~131 파싱 인덱스 1:1 일치 |
| 3 | qry_where replace 동작 | ✅ PASS | `SBI.ITEM_CODE` → `SBI.품목코드` 전환 후 잔존 없음, MSSQL 문법 유효 |
| 4 | TB_BARCODE_INFO INSERT 정합성 | ✅ PASS | 22컬럼 INSERT 정상, `SHELF_LIFE`는 nullable(L872)이라 빈값 허용 |
| 5 | 전체 흐름 시뮬레이션 (11단계) | ✅ PASS | ProgressDlgBarcodeSearch → URL 분기 → JSP → replace → SELECT → out.println → split → 파싱 → setter → INSERT 전 과정 정상 |
| 6 | 원본 JSP 동작 동일성 | ✅ PASS | 24컬럼·하드코딩 4건·품목코드 재사용 3건 구조 유지 |

### 11.2 검증 중 도출된 주의사항

| # | 항목 | 내용 |
|:-:|------|------|
| 1 | `PACKER_PRD_CODE_FROM/TO` 빈값 처리 | CO_품목코드에 해당 컬럼 없음 → `''` 고정. 바코드 매칭 로직(`BARCODEGOODS_TO != ''`)과 무관, 기능 영향 없음 |
| 2 | `REG_DATE`/`REG_TIME`/`MEMO` | JSP SELECT로 읽히지만 `insertqueryBarcodeInfo()` INSERT 컬럼 목록에 원래부터 없음 (기존 동작 유지) |
| 3 | `temp[24]` 스킵 | Java L132~134의 `searchType != 4 && != 5` 분기로 스킵, 24컬럼 쿼리여도 배열 범위 오버 없음 |
| 4 | ORDER BY | `ITEMCODE ASC`는 SELECT 별칭 기반. MSSQL에서 별칭 ORDER BY 사용 가능 (변경 불요) |

### 11.3 11장 10장의 예상 문제점 재평가 결과

| 예상 문제점 # | 시뮬레이션 결과 | 비고 |
|:-:|:-:|------|
| 1. WHERE `SBI.ITEM_CODE` 오류 | ✅ 해결됨 | replace 처리로 완전 방지 확인 |
| 2. ZEROPOINT NULL | ⚠️ 시뮬레이션 불가 | 데이터 환경 의존 — Step 3 실기기 테스트에서 확인 |
| 3. 바코드 파싱 위치 NULL | ⚠️ 시뮬레이션 불가 | 데이터 환경 의존 — Step 3 실기기 테스트에서 확인 |
| 4. 컬럼 수 25개로 증가 | ✅ 방지됨 | 쿼리 설계상 24개 고정, SHELF_LIFE 미포함 |
| 5. ITEM_CODE 데이터 없음 | ⚠️ 시뮬레이션 불가 | 운영 환경 의존 — Step 3 실기기 테스트에서 확인 |
| 6. PACKER_PRD_CODE_FROM/TO 사용 불가 | ✅ 확인됨 | Log 출력만 사용, 기능 영향 없음 확정 |

### 11.4 결론

- 코드 정합성 레벨(스키마·파싱·쿼리 문법·INSERT)은 **전 항목 통과**
- 데이터 값 레벨(NULL 여부·등록 여부)은 실기기/실데이터 의존 → Step 3 통합 테스트에서 검증
- **Step 1 착수 가능**. Step 1~2 완료 후 Step 3 통합 테스트로 데이터 레벨 최종 확인

---

## 12. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 사전 시뮬레이션 | ⑤ code-verifier 정합성 검증 | ✅ PASS (2026-04-22) |
| 1 | B_ITEM → CO_품목코드 SELECT 쿼리 전환 | ✅ 완료 (2026-04-22, ⑤⑥ 사후 검증 PASS) |
| 2 | out.println 24개 컬럼 수/순서 검증 | ✅ 완료 (2026-04-22, 정적 구조 검증 PASS, 컴파일/테스트는 Step 3 이관) |
| 3 | 통합 테스트 | ✅ 완료 |

---

## 관련 문서

- `app/doc/소스분석/46_이마트바코드정보조회_vs_비정량바코드정보조회_JSP_원본비교분석.md` — 이마트 vs 비정량 바코드 JSP 원본 Oracle 기준 1:1 비교 분석 (사전자료)
- `app/doc/소스분석/45_비정량_바코드정보조회_JSP_컬럼_사용분석.md` — 24개 컬럼 사용/미사용 전체 분석
- `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md` — 비정량 전체 흐름 (1-1단계)
- `app/doc/개발/35_비정량_출하계근대상_JSP_MSSQL전환.md` — search_production_nonfixed.jsp 전환 (동일 패턴 참고)
- JSP 이마트 참고: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info.jsp`
- ERP Entity: `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\java\com\sgis\domain\co\bizbasic\entity\ItemCodeEntity.java`

---

**문서 버전**: 1.0
