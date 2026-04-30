# search_barcode_info.jsp - 바코드정보조회 JSP MSSQL 전환 전 구조분석

## 개요

정량 바코드정보조회 JSP(이마트·홈플러스·도매·생산·롯데 등 공용)의 현재 MSSQL 전환 상태를 분석한다.
원본(Oracle) 구조와 현재(MSSQL 전환 후) 구조를 대조하고, 홈플러스(searchType=2) MSSQL 전환 개발 전 사전 분석 정보를 제공한다.

- **파일 경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info.jsp`
- **원본 파일 경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_barcode_info.jsp`
- **패키지**: JSP (Tomcat 외부 프로젝트)
- **총 라인 수**: 95줄 (현재), 106줄 (원본)
- **타입**: JSP (바코드정보 조회 API)
- **작성일**: 2026-04-30

---

## 1. 역할

- 정량 출하 searchType(0=이마트, 1=생산, 2=홈플러스, 3=도매, 6=롯데, 7=생산라벨)이 공통으로 호출하는 바코드 파싱 규칙 조회 JSP
- Java(`ProgressDlgBarcodeSearch`)에서 전송한 `WHERE SBI.PPCODE = '...' OR ...` 조건을 수신하여 MSSQL 쿼리 실행
- `CO_품목코드` 단일 테이블에서 바코드 파싱 규칙(상품코드 구간·중량 구간·제조일자 구간·박스시리얼 구간·유통기한)을 조회
- `::` 컬럼 구분자, `;;` 행 구분자로 25개 컬럼을 응답
- PDA 앱 로컬 DB(`TB_BARCODE_INFO`) INSERT를 위한 데이터 공급원

---

## 2. 주요 상수/필드

| 상수/필드 | 타입 | 값/설명 | 용도 |
|----------|------|---------|------|
| `qry_where` | String | Java에서 POST로 수신 (`data` 파라미터) | WHERE 조건 (PPCODE 목록) |
| `dbid` | String | Java에서 POST로 수신 (`dbid` 파라미터) | DB 식별자 (현재 MSSQL 전환 후 미사용) |
| `connection` | boolean | getMSSQLConnection() 성공 여부 | DB 연결 상태 플래그 |
| 행 구분자 | String | `;;` | 응답 데이터 행 분리 |
| 컬럼 구분자 | String | `::` | 응답 데이터 컬럼 분리 |

---

## 3. 주요 메서드

| 동작 블록 | 위치(줄) | 반환 | 용도 |
|---------|:--------:|------|------|
| 파라미터 수신 | L16~17 | - | `qry_where`, `dbid` 수신 |
| MSSQL 연결 | L24 | Connection | `getMSSQLConnection()` 호출 |
| 쿼리 구성 | L35~65 | String | SELECT 25개 컬럼 + FROM + WHERE + ORDER BY |
| ResultSet 실행 | L67 | ResultSet | `stmt.executeQuery()` |
| 응답 출력 | L76~83 | - | `out.println()` 25개 컬럼 `;;` 종결 |
| 리소스 해제 | L86~94 | - | rs/stmt/conn close |

---

## 4. 호출 관계

### 4.1 이 파일이 호출하는 대상

| 호출 대상 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| `db_connection.jsp` | `getMSSQLConnection()` | L24 | MSSQL DB 연결 획득 |
| MSSQL `CO_품목코드` | SELECT | L35~65 | 바코드 파싱 규칙 단일 테이블 조회 |

### 4.2 이 파일을 호출하는 곳

| 호출 위치 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| `ProgressDlgBarcodeSearch.java` | `sendDataDb()` | L78 | searchType=0,2,3,6 (이마트/홈플러스/도매/롯데) |
| `ProgressDlgBarcodeSearch.java` | `sendDataDb()` | L81 | searchType=1 (생산) |
| `ProgressDlgBarcodeSearch.java` | `sendDataDb()` | L87 | searchType=7 (생산라벨) |

**searchType별 URL 분기 전체 목록 (ProgressDlgBarcodeSearch.java L77~88)**

| searchType | 호출 JSP | Java 분기 위치 | 비고 |
|:---------:|----------|:------------:|------|
| 0 (이마트) | `search_barcode_info.jsp` (본 파일) | L77~79 | 정량 공용 |
| 1 (생산) | `search_barcode_info.jsp` (본 파일) | L80~81 | 정량 공용 |
| 2 (홈플러스) | `search_barcode_info.jsp` (본 파일) | L77~79 | 정량 공용 |
| 3 (도매) | `search_barcode_info.jsp` (본 파일) | L77~79 | 정량 공용 |
| 4 (비정량) | `search_barcode_info_nonfixed.jsp` | L82~83 | 비정량 전용 |
| 5 (홈플러스비정량) | `search_barcode_info_nonfixed.jsp` (URL_SEARCH_HOMEPLUS_NONFIXED2) | L84~85 | 홈플러스비정량 전용 |
| 6 (롯데) | `search_barcode_info.jsp` (본 파일) | L77~79 | 정량 공용 |
| 7 (생산라벨) | `search_barcode_info.jsp` (본 파일) | L86~87 | 정량 공용 |

---

## 5. 데이터 흐름

```
[PDA 앱] searchType 결정
    ↓
ProgressDlgBarcodeSearch.doInBackground()
    ↓
  searchType=4,5 → selectqueryCodeListForNonFixed() → TB_SHIPMENT.ITEM_CODE 목록
  searchType=0,1,2,3,6,7 → selectqueryCodeList() → TB_SHIPMENT.PACKER_PRODUCT_CODE 목록
    ↓
WHERE 조건 조립:
  비정량: "WHERE (SBI.ITEM_CODE = 'CODE1' OR ...) AND SBI.회사코드 = 'SGI'"
  정량:   "WHERE (SBI.PPCODE = 'CODE1' OR ...) AND SBI.회사코드 = 'SGI'"
    ↓
HTTP POST → data 파라미터
    ↓
[search_barcode_info.jsp] ← 본 파일 (searchType=0,1,2,3,6,7)
    ↓
getMSSQLConnection() → MSSQL 연결
    ↓
SELECT 25컬럼 FROM CO_품목코드 SBI
  + qry_where (WHERE SBI.PPCODE = ... AND SBI.회사코드 = ...)
  + AND SBI.ppCode != ''
  ORDER BY PACKER_PRODUCT_CODE ASC
    ↓
out.println col1::col2::...::col25;;  (행당 1줄)
    ↓
[PDA 앱] split(";;") → split("::", -1) → temp[]
    ↓
temp[0]~temp[23] 공통 파싱
temp[24] (SHELF_LIFE) — searchType != 4,5 일 때만 파싱
    ↓
DBHandler.insertqueryBarcodeInfo() → TB_BARCODE_INFO INSERT
```

---

## 6. 핵심 코드

### 6.1 현재 MSSQL JSP 쿼리 (전체)

```jsp
String quertystring = "SELECT 패커코드        AS PACKER_CLIENT_CODE"
                     + ", ppCode    AS PACKER_PRODUCT_CODE"
                     + ", ''        AS PACKER_PRD_NAME"
                     + ", ''        AS ITEMCODE"
                     + ", 품목명    AS ITEM_NAME_KR"
                     + ", ''        AS BRAND_CODE"
                     + ", 상품바코드 AS BARCODEGOODS"
                     + ", 기준단위  AS BASEUNIT"
                     + ", 소수점    AS ZEROPOINT"
                     + ", ''        AS PACKER_PRD_CODE_FROM"
                     + ", ''        AS PACKER_PRD_CODE_TO"
                     + ", 바코드상품코드시작 AS BARCODEGOODS_FROM"
                     + ", 바코드상품코드끝 AS BARCODEGOODS_TO"
                     + ", 중량시작 AS WEIGHT_FROM"
                     + ", 중량끝 AS WEIGHT_TO"
                     + ", 제조일자시작 AS MAKINGDATE_FROM"
                     + ", 제조일자끝 AS MAKINGDATE_TO"
                     + ", 박스시리얼시작 AS BOXSERIAL_FROM"
                     + ", 박스시리얼끝 AS BOXSERIAL_TO"
                     + ", '' AS STATUS"
                     + ", '' AS REG_ID"
                     + ", '' AS REG_DATE"
                     + ", '' AS REG_TIME"
                     + ", '' AS MEMO"
                     + ", CASE WHEN 적용단위 = 1 THEN 적용값 * 365"
                     + "       WHEN 적용단위 = 2 THEN 적용값 * 30"
                     + "       ELSE 적용값 END AS SHELF_LIFE"
                  + " FROM CO_품목코드 SBI"
                     + qry_where
                     + " AND SBI.ppCode != ''"
                  + " ORDER BY PACKER_PRODUCT_CODE ASC";
```

### 6.2 원본 Oracle JSP 쿼리 (참고)

```jsp
String quertystring = "SELECT SBI.PACKER_CLIENT_CODE"
    + ", SBI.PACKER_PRODUCT_CODE"
    + ", SBI.PACKER_PRD_NAME"
    + ", SBI.ITEMCODE"
    + ", BI.ITEM_NAME_KR"
    + ", SBI.BRAND_CODE"
    + ", SBI.BARCODEGOODS"
    + ", SBI.BASEUNIT"
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
    + ", SBI.MEMO"
    + ", BSI.SHELF_LIFE"
    + " FROM S_BARCODE_INFO SBI"
    + " INNER JOIN B_ITEM BI ON SBI.ITEMCODE = BI.ITEM_CODE AND BI.STATUS = 'Y'"
    + " INNER JOIN B_SUPPLIER_ITEM BSI ON SBI.PACKER_CLIENT_CODE = BSI.PACKER_CODE"
    + "   AND SBI.PACKER_PRODUCT_CODE = BSI.PACKER_PRODUCT_CODE"
    + "   AND SBI.STATUS = 'Y' AND BSI.STATUS = 'Y'"
    + qry_where
    + " ORDER BY PACKER_PRODUCT_CODE ASC";
```

---

## 7. JSP out.println ↔ Java temp[] 인덱스 1:1 대조표

| Index | SELECT 별칭 | 현재 MSSQL 출처 (CO_품목코드) | 원본 Oracle 출처 | Java temp[] setter | 업무 의미 | 비고 |
|:-----:|------------|---------------------------|-----------------|--------------------|---------|------|
| 0 | PACKER_CLIENT_CODE | `패커코드` (CO_품목코드) | `SBI.PACKER_CLIENT_CODE` (S_BARCODE_INFO) | `setPACKER_CLIENT_CODE(temp[0])` | 패커업체코드 | 원본은 S_BARCODE_INFO 직접 컬럼 |
| 1 | PACKER_PRODUCT_CODE | `ppCode` (CO_품목코드) | `SBI.PACKER_PRODUCT_CODE` (S_BARCODE_INFO) | `setPACKER_PRODUCT_CODE(temp[1])` | 패커상품코드 | 현재: CO_품목코드.ppCode |
| 2 | PACKER_PRD_NAME | `''` (빈값) | `SBI.PACKER_PRD_NAME` (S_BARCODE_INFO) | `setPACKER_PRD_NAME(temp[2])` | 패커상품명 | MSSQL 전환 후 빈값 |
| 3 | ITEMCODE | `''` (빈값) | `SBI.ITEMCODE` (S_BARCODE_INFO) | `setITEM_CODE(temp[3])` | 품목코드 | MSSQL 전환 후 빈값 |
| 4 | ITEM_NAME_KR | `품목명` (CO_품목코드) | `BI.ITEM_NAME_KR` (B_ITEM JOIN) | `setITEM_NAME_KR(temp[4])` | 품목명 | 현재: 단일 테이블 |
| 5 | BRAND_CODE | `''` (빈값) | `SBI.BRAND_CODE` (S_BARCODE_INFO) | `setBRAND_CODE(temp[5])` | 브랜드코드 | MSSQL 전환 후 빈값 |
| 6 | BARCODEGOODS | `상품바코드` (CO_품목코드) | `SBI.BARCODEGOODS` (S_BARCODE_INFO) | `setBARCODEGOODS(temp[6])` | 바코드상품코드 | 현재: CO_품목코드.상품바코드 |
| 7 | BASEUNIT | `기준단위` (CO_품목코드) | `SBI.BASEUNIT` (S_BARCODE_INFO) | `setBASEUNIT(temp[7])` | 기준단위 | 현재: CO_품목코드.기준단위 |
| 8 | ZEROPOINT | `소수점` (CO_품목코드) | `SBI.ZEROPOINT` (S_BARCODE_INFO) | `setZEROPOINT(temp[8])` | 소수점 | 현재: CO_품목코드.소수점 |
| 9 | PACKER_PRD_CODE_FROM | `''` (빈값) | `SBI.PACKER_PRD_CODE_FROM` (S_BARCODE_INFO) | `setPACKER_PRD_CODE_FROM(temp[9])` | 패커상품코드 시작 | MSSQL 전환 후 빈값 |
| 10 | PACKER_PRD_CODE_TO | `''` (빈값) | `SBI.PACKER_PRD_CODE_TO` (S_BARCODE_INFO) | `setPACKER_PRD_CODE_TO(temp[10])` | 패커상품코드 끝 | MSSQL 전환 후 빈값 |
| 11 | BARCODEGOODS_FROM | `바코드상품코드시작` (CO_품목코드) | `SBI.BARCODEGOODS_FROM` (S_BARCODE_INFO) | `setBARCODEGOODS_FROM(temp[11])` | 바코드 시작위치 | 현재: CO_품목코드.바코드상품코드시작 |
| 12 | BARCODEGOODS_TO | `바코드상품코드끝` (CO_품목코드) | `SBI.BARCODEGOODS_TO` (S_BARCODE_INFO) | `setBARCODEGOODS_TO(temp[12])` | 바코드 끝위치 | 현재: CO_품목코드.바코드상품코드끝 |
| 13 | WEIGHT_FROM | `중량시작` (CO_품목코드) | `SBI.WEIGHT_FROM` (S_BARCODE_INFO) | `setWEIGHT_FROM(temp[13])` | 중량 시작 | 현재: CO_품목코드.중량시작 |
| 14 | WEIGHT_TO | `중량끝` (CO_품목코드) | `SBI.WEIGHT_TO` (S_BARCODE_INFO) | `setWEIGHT_TO(temp[14])` | 중량 끝 | 현재: CO_품목코드.중량끝 |
| 15 | MAKINGDATE_FROM | `제조일자시작` (CO_품목코드) | `SBI.MAKINGDATE_FROM` (S_BARCODE_INFO) | `setMAKINGDATE_FROM(temp[15])` | 제조일자 시작 | 현재: CO_품목코드.제조일자시작 |
| 16 | MAKINGDATE_TO | `제조일자끝` (CO_품목코드) | `SBI.MAKINGDATE_TO` (S_BARCODE_INFO) | `setMAKINGDATE_TO(temp[16])` | 제조일자 끝 | 현재: CO_품목코드.제조일자끝 |
| 17 | BOXSERIAL_FROM | `박스시리얼시작` (CO_품목코드) | `SBI.BOXSERIAL_FROM` (S_BARCODE_INFO) | `setBOXSERIAL_FROM(temp[17])` | 박스시리얼 시작 | 현재: CO_품목코드.박스시리얼시작 |
| 18 | BOXSERIAL_TO | `박스시리얼끝` (CO_품목코드) | `SBI.BOXSERIAL_TO` (S_BARCODE_INFO) | `setBOXSERIAL_TO(temp[18])` | 박스시리얼 끝 | 현재: CO_품목코드.박스시리얼끝 |
| 19 | STATUS | `''` (빈값) | `SBI.STATUS` (S_BARCODE_INFO) | `setSTATUS(temp[19])` | 상태 | MSSQL 전환 후 빈값 |
| 20 | REG_ID | `''` (빈값) | `SBI.REG_ID` (S_BARCODE_INFO) | `setREG_ID(temp[20])` | 등록자 | MSSQL 전환 후 빈값 |
| 21 | REG_DATE | `''` (빈값) | `SBI.REG_DATE` (S_BARCODE_INFO) | `setREG_DATE(temp[21])` | 등록일 | MSSQL 전환 후 빈값 |
| 22 | REG_TIME | `''` (빈값) | `SBI.REG_TIME` (S_BARCODE_INFO) | `setREG_TIME(temp[22])` | 등록시간 | MSSQL 전환 후 빈값 |
| 23 | MEMO | `''` (빈값) | `SBI.MEMO` (S_BARCODE_INFO) | `setMEMO(temp[23])` | 메모 | MSSQL 전환 후 빈값 |
| 24 | SHELF_LIFE | `CASE 적용단위/적용값 계산` (CO_품목코드) | `BSI.SHELF_LIFE` (B_SUPPLIER_ITEM JOIN) | `setSHELF_LIFE(temp[24])` | 유통기한(일) | 계산식 대체 |

**총 컬럼 수: 25개** (인덱스 0~24)
**비정량 JSP(search_barcode_info_nonfixed.jsp)는 24개 — SHELF_LIFE(index 24) 없음**

---

## 8. 원본 비교

### 8.1 DB 연결 방식 비교

| 항목 | 원본 (Oracle) | 현재 (MSSQL) | 동일 |
|------|-------------|-------------|:----:|
| 드라이버 로드 | `Class.forName("oracle.jdbc.driver.OracleDriver")` | 없음 (db_connection.jsp 위임) | X |
| DB 연결 | `DriverManager.getConnection(url, dbid, "DBpassword")` | `getMSSQLConnection()` | X |
| include | 없음 | `<%@ include file="common/db_connection.jsp" %>` | X |
| 인코딩 | `euc-kr` | `UTF-8` | X |

### 8.2 쿼리 구조 비교

| 항목 | 원본 (Oracle) | 현재 (MSSQL) | 동일 |
|------|-------------|-------------|:----:|
| 메인 테이블 | `S_BARCODE_INFO SBI` | `CO_품목코드 SBI` | X |
| JOIN 구조 | 3-way JOIN (S_BARCODE_INFO + B_ITEM + B_SUPPLIER_ITEM) | 단일 테이블 (CO_품목코드만) | X |
| 출력 컬럼 수 | 25개 | 25개 | O |
| SHELF_LIFE 계산 | `BSI.SHELF_LIFE` (직접 컬럼) | `CASE WHEN 적용단위=1 THEN 적용값*365 WHEN 적용단위=2 THEN 적용값*30 ELSE 적용값 END` | X |
| 추가 WHERE 조건 | 없음 (JOIN 조건에 STATUS='Y' 내포) | `AND SBI.ppCode != ''` | X |
| ORDER BY | `PACKER_PRODUCT_CODE ASC` | `PACKER_PRODUCT_CODE ASC` | O |
| 행 구분자 | `;;` | `;;` | O |
| 컬럼 구분자 | `::` | `::` | O |

### 8.3 컬럼별 원본 vs 현재 (MSSQL 전환 후 빈값 처리 현황)

| Index | 별칭 | 원본 값 | 현재 값 | 빈값 여부 |
|:-----:|------|--------|--------|:--------:|
| 2 | PACKER_PRD_NAME | S_BARCODE_INFO.PACKER_PRD_NAME | `''` | 빈값 |
| 3 | ITEMCODE | S_BARCODE_INFO.ITEMCODE | `''` | 빈값 |
| 5 | BRAND_CODE | S_BARCODE_INFO.BRAND_CODE | `''` | 빈값 |
| 9 | PACKER_PRD_CODE_FROM | S_BARCODE_INFO.PACKER_PRD_CODE_FROM | `''` | 빈값 |
| 10 | PACKER_PRD_CODE_TO | S_BARCODE_INFO.PACKER_PRD_CODE_TO | `''` | 빈값 |
| 19 | STATUS | S_BARCODE_INFO.STATUS | `''` | 빈값 |
| 20 | REG_ID | S_BARCODE_INFO.REG_ID | `''` | 빈값 |
| 21 | REG_DATE | S_BARCODE_INFO.REG_DATE | `''` | 빈값 |
| 22 | REG_TIME | S_BARCODE_INFO.REG_TIME | `''` | 빈값 |
| 23 | MEMO | S_BARCODE_INFO.MEMO | `''` | 빈값 |

**빈값 처리 컬럼 10개**: S_BARCODE_INFO, B_SUPPLIER_ITEM이 MSSQL/HL ERP에 없어 CO_품목코드로 통합되면서 해당 전용 컬럼이 소실됨

---

## 9. MSSQL 전환 시 테이블 매핑 분석

### 9.1 Oracle → MSSQL 테이블 매핑 (현재 전환 결과)

| Oracle 테이블 | 역할 | MSSQL 대응 | 전환 방식 |
|-------------|------|-----------|---------|
| `S_BARCODE_INFO` | 바코드 파싱 규칙 마스터 (패커·상품별) | `CO_품목코드` | 통합 — 패커별 규칙 → 품목별 규칙으로 단순화 |
| `B_ITEM` | 품목 기본 마스터 (품목명 등) | `CO_품목코드` | 통합 — 품목명은 CO_품목코드.품목명 |
| `B_SUPPLIER_ITEM` | 공급사-상품 관계 (SHELF_LIFE 등) | `CO_품목코드` | 부분 통합 — SHELF_LIFE는 `적용단위`/`적용값` CASE 계산식으로 대체 |

### 9.2 CO_품목코드 컬럼 → JSP SELECT 대응표

ERP `C0102_SQL.xml` 확인 결과 CO_품목코드에 존재하는 컬럼:

| CO_품목코드 컬럼 | ERP 별칭 | JSP에서 사용 | JSP SELECT 별칭 |
|----------------|---------|:-----------:|----------------|
| `패커코드` | packerCode | O | PACKER_CLIENT_CODE |
| `PPCODE` | ppCode | O | PACKER_PRODUCT_CODE |
| `품목명` | itemNm | O | ITEM_NAME_KR |
| `상품바코드` | goodsBrcd | O | BARCODEGOODS |
| `기준단위` | stdrUnit | O | BASEUNIT |
| `소수점` | dcmlPoint | O | ZEROPOINT |
| `바코드상품코드시작` | brcdGoodsBegin | O | BARCODEGOODS_FROM |
| `바코드상품코드끝` | brcdGoodsEnd | O | BARCODEGOODS_TO |
| `중량시작` | wtBegin | O | WEIGHT_FROM |
| `중량끝` | wtEnd | O | WEIGHT_TO |
| `제조일자시작` | mnfcturDeBegin | O | MAKINGDATE_FROM |
| `제조일자끝` | mnfcturDeEnd | O | MAKINGDATE_TO |
| `박스시리얼시작` | boxSerialBegin | O | BOXSERIAL_FROM |
| `박스시리얼끝` | boxSerialEnd | O | BOXSERIAL_TO |
| `적용단위` | applcUnit | O (SHELF_LIFE 계산용) | - |
| `적용값` | applcValue | O (SHELF_LIFE 계산용) | - |
| `회사코드` | - | O (WHERE 조건) | - |

### 9.3 SHELF_LIFE 계산 로직

```sql
CASE WHEN 적용단위 = 1 THEN 적용값 * 365   -- 단위: 연 → 일수 환산
     WHEN 적용단위 = 2 THEN 적용값 * 30    -- 단위: 월 → 일수 환산
     ELSE 적용값                           -- 단위: 일 → 그대로
END AS SHELF_LIFE
```

원본은 `B_SUPPLIER_ITEM.SHELF_LIFE` 직접 컬럼(일수 값)이었으나, MSSQL 전환 후 CO_품목코드의 `적용단위`+`적용값` 조합으로 계산.

---

## 10. search_barcode_info.jsp vs search_barcode_info_nonfixed.jsp 비교

### 10.1 현재 MSSQL 버전 구조 비교

| 항목 | 정량 (search_barcode_info.jsp) | 비정량 (search_barcode_info_nonfixed.jsp) |
|------|-------------------------------|------------------------------------------|
| DB 연결 | `getMSSQLConnection()` | `getMSSQLConnection()` |
| 메인 테이블 | `CO_품목코드 SBI` | `CO_품목코드 SBI` |
| JOIN 구조 | 단일 테이블 | 단일 테이블 |
| qry_where replace | **없음** (PPCODE 조건 그대로 사용) | **있음**: `SBI.ITEM_CODE` → `SBI.품목코드` 치환 |
| 추가 WHERE 조건 | `AND SBI.ppCode != ''` | 없음 |
| 출력 컬럼 수 | **25개** (SHELF_LIFE 포함) | **24개** (SHELF_LIFE 없음) |
| SHELF_LIFE | `CASE 적용단위/적용값 계산` | **없음** |
| ORDER BY | `PACKER_PRODUCT_CODE ASC` | `ITEMCODE ASC` |
| 하드코딩 컬럼 | 없음 (모두 실제 컬럼 또는 빈값) | PACKER_CLIENT_CODE='이마트용', BRAND_CODE='0000', BASEUNIT='KG', MEMO='0000' |

### 10.2 WHERE 조건 파라미터 차이

```
정량 Java가 전송하는 qry_where:
  " WHERE (SBI.PPCODE = 'CODE1' OR SBI.PPCODE = 'CODE2' ...) AND SBI.회사코드 = 'SGI'"
  → 정량 JSP: qry_where 그대로 사용 (replace 없음)
  → CO_품목코드에 PPCODE 컬럼 존재 확인됨 (ERP C0102_SQL.xml L130)

비정량 Java가 전송하는 qry_where:
  " WHERE (SBI.ITEM_CODE = 'CODE1' OR ...) AND SBI.회사코드 = 'SGI'"
  → 비정량 JSP: qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드") 치환 후 사용
```

### 10.3 전환 복잡도 비교

| 항목 | 정량 전환 복잡도 | 비정량 전환 복잡도 |
|------|:--------------:|:--------------:|
| 원본 테이블 수 | 3개 (S_BARCODE_INFO + B_ITEM + B_SUPPLIER_ITEM) | 1개 (B_ITEM) |
| MSSQL 전환 후 테이블 수 | 1개 (CO_품목코드) | 1개 (CO_품목코드) |
| SHELF_LIFE 처리 | CASE 계산식 필요 | 불필요 |
| 빈값 처리 컬럼 | 10개 | 5개 |
| 전환 난이도 | **높음** (3-way JOIN → 단일 테이블, SHELF_LIFE 계산식 설계) | **낮음** (테이블명 교체만) |
| 현재 상태 | **완료** | **완료** (개발36) |

---

## 11. 주의사항

- 현재 `search_barcode_info.jsp`는 이미 MSSQL로 전환 완료된 상태 — Oracle 연결 코드 없음
- MSSQL 전환 후 빈값 처리된 10개 컬럼(`PACKER_PRD_NAME`, `ITEMCODE`, `BRAND_CODE`, `PACKER_PRD_CODE_FROM/TO`, `STATUS`, `REG_ID/DATE/TIME`, `MEMO`)은 Java에서 파싱은 하지만 로컬DB INSERT 시 `insertqueryBarcodeInfo()`에서 `REG_DATE`, `REG_TIME`이 INSERT 대상에서 제외되어 있음 — 실제 업무 영향 없음 확인 필요
- **searchType=2(홈플러스 정량)는 searchType=0(이마트)와 동일한 JSP를 공용으로 호출** — 홈플러스 전용 별도 JSP 없음
- `AND SBI.ppCode != ''` 조건은 MSSQL 전환 시 추가된 조건 — CO_품목코드에는 ppCode가 비어있는 품목도 있으므로 바코드 정보가 없는 품목 제외 목적
- SHELF_LIFE 계산식에서 `적용단위=1`은 연(year), `=2`는 월(month), 나머지는 일(day) — ERP `M0103_SQL.xml` 확인됨
- Java `ProgressDlgBarcodeSearch.java L77~79`에서 searchType 0,2,3,6을 동일 분기로 처리하므로 홈플러스(2) 관련 별도 로직 없음
- `insertqueryBarcodeInfo()` INSERT 쿼리는 `REG_DATE`, `REG_TIME`, `BARCODEGOODS_FROM`(사실은 포함됨) 확인 필요 — DBHandler.java L1066~1112 참고
- `qry_where`에는 이미 `AND SBI.회사코드 = 'SGI'` 조건이 포함되어 전송됨 (`ProgressDlgBarcodeSearch.java L72`)

---

## 12. 관련 문서

- `app/doc/소스분석/17_ProgressDlgBarcodeSearch.md` — Java 파싱 클래스 소스분석
- `app/doc/소스분석/46_이마트바코드정보조회_vs_비정량바코드정보조회_JSP_원본비교분석.md` — Oracle 원본 기준 정량/비정량 1:1 비교
- `app/doc/소스분석/45_비정량_바코드정보조회_JSP_컬럼_사용분석.md` — 비정량 24개 컬럼 사용/미사용 분석
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` — 이마트 정량 전체 흐름
- `app/doc/소스분석/52_홈플러스정량_출하계근대상_JSP_Java파싱_인덱스분석.md` — 홈플러스 정량 출하대상 JSP 분석
- **JSP 현재 (MSSQL 전환 완료)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info.jsp`
- **JSP 원본 (Oracle)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_barcode_info.jsp`
- **비정량 JSP (MSSQL 전환 완료)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp`
- **Java 파싱 클래스**: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgBarcodeSearch.java`
- **로컬DB CREATE**: `app/src/main/java/com/rgbsolution/highland_emart/db/DBHandler.java` — `createqueryBarcodeInfo()` L841
