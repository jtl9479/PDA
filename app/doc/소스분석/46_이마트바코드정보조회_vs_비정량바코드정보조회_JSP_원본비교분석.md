# 이마트(search_barcode_info.jsp) vs 비정량(search_barcode_info_nonfixed.jsp) JSP 원본 비교 분석

## 개요

**원본 Oracle JSP 2종의 1:1 비교**. 이마트 정량 바코드정보조회 JSP와 비정량 바코드정보조회 JSP를 **원본 프로젝트(`apache-tomcat-7.0.78_PDA_IN(원본)`) 기준으로** 비교하여 두 JSP가 처음부터 어떻게 다르게 설계되었는지(순수 업무 로직 차이)를 파악한다.

MSSQL 전환 아티팩트를 배제하고 **정량·비정량의 본질적 차이**만 추출하는 것이 본 문서의 목적이다.

- **대상 파일 A (이마트, 원본 Oracle)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_barcode_info.jsp`
- **대상 파일 B (비정량, 원본 Oracle)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp`
- **Java 파싱 공통 클래스**: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgBarcodeSearch.java`
- **로컬 저장 테이블**: `TB_BARCODE_INFO` (SQLite, 공통)
- **비교 시점**: **원본(MSSQL 전환 전)** 기준
- **타입**: JSP 원본 비교 분석
- **작성일**: 2026-04-22

---

## 1. 역할

- 두 JSP는 모두 **바코드 파싱 규칙(상품코드 구간, 중량 구간, 제조일자 구간, 박스시리얼 구간)을 조회**하여 PDA 로컬 DB(`TB_BARCODE_INFO`)에 저장하기 위한 데이터를 반환한다.
- 호출자(Java `ProgressDlgBarcodeSearch`)는 동일 클래스가 `searchType`에 따라 URL만 다르게 호출한다.
- 응답 포맷(`::` 컬럼 구분, `;;` 행 구분)과 파싱 인덱스(`temp[0]`~`temp[23]`, 이마트는 `temp[24]` 추가)는 두 JSP가 동일 규약을 유지한다.
- **역할 자체는 동일하지만, 데이터 출처 테이블과 조회 방식이 원본부터 이미 완전히 다르게 설계되었다.**

---

## 2. 주요 상수/필드 (원본 기준)

| 항목 | 이마트 원본 (A) | 비정량 원본 (B) | 차이 유형 |
|------|----------------|----------------|:--------:|
| DB 접속 방식 | Oracle `DriverManager.getConnection(url, dbid, "DBpassword")` | Oracle `DriverManager.getConnection(url, "DBuser", "DBpassword")` | DB 계정 전달 방식 다름 |
| 메인 테이블 | **`S_BARCODE_INFO`** (바코드 파싱 규칙 전용) | **`B_ITEM`** (품목 마스터만) | **핵심 차이** |
| JOIN 구조 | **3-way JOIN** (`S_BARCODE_INFO` + `B_ITEM` + `B_SUPPLIER_ITEM`) | **단일 테이블** (JOIN 없음) | **핵심 차이** |
| 응답 컬럼 수 | 25개 | 24개 | SHELF_LIFE 유무 |
| ORDER BY 키 | `PACKER_PRODUCT_CODE ASC` | `ITEMCODE ASC` | 정렬 별칭 다름 |
| WHERE qry_where | Java에서 전달 | Java에서 전달 | 동일 |
| 추가 WHERE 조건 | 없음 (JOIN 조건 내 `STATUS='Y'` 필터만) | 없음 | 동일 |
| 행 구분자 | `;;` | `;;` | 동일 |
| 컬럼 구분자 | `::` | `::` | 동일 |

### 2.1 테이블 의미

| 테이블 | 역할 | 이마트 원본 사용 | 비정량 원본 사용 |
|--------|------|:--------------:|:--------------:|
| **S_BARCODE_INFO** | 바코드 파싱 규칙 전용 마스터 (패커·상품별 코드/중량/제조일자/박스시리얼 구간) | ✅ 메인 | ❌ 미사용 |
| **B_ITEM** | 품목 기본 마스터 (품목코드, 품목명 등) | ✅ JOIN (품목명 조회용) | ✅ 단독 사용 |
| **B_SUPPLIER_ITEM** | 공급사-상품 관계 (유통기한, 패커상품코드 등) | ✅ JOIN (SHELF_LIFE 조회용) | ❌ 미사용 |

---

## 3. 주요 메서드 (원본 기준)

두 JSP 모두 단일 스크립트 블록. 주요 동작 블록만 표기한다.

| 동작 | 이마트 원본 (A) 줄 | 비정량 원본 (B) 줄 | 용도 |
|------|:---------------:|:---------------:|------|
| 파라미터 수신 | L17~18 | L17 | `qry_where`, `dbid` 수신 |
| Oracle 드라이버 로드 | L28 | L28 | `Class.forName(driver)` |
| DB 접속 | L29 | L29 | `DriverManager.getConnection()` |
| 쿼리 문자열 구성 | L41~70 | L40~66 | SELECT 쿼리 조립 |
| ResultSet 실행 | L72 | L68 | `stmt.executeQuery()` |
| out.println 반복 | L82~93 | L78~89 | 행 단위 결과 출력 |
| 리소스 해제 | L95~104 | L91~101 | `rs/stmt/conn.close()` |

---

## 4. 호출 관계

### 4.1 Java 호출 분기 (searchType)

| searchType | 호출 URL 상수 | 원본 JSP | Java 분기 위치 |
|:---------:|---------------|---------|:----:|
| 0 (이마트) | `URL_SEARCH_BARCODE_INFO` | **A** | L74~76 |
| 1 (생산) | `URL_SEARCH_BARCODE_INFO` | **A** | L77~78 |
| 2 (홈플러스) | `URL_SEARCH_BARCODE_INFO` | **A** | L74~76 |
| 3 (도매) | `URL_SEARCH_BARCODE_INFO` | **A** | L74~76 |
| 4 (비정량) | `URL_SEARCH_BARCODE_INFO_NONFIXED` | **B** | L79~80 |
| 5 (홈플러스 비정량) | `URL_SEARCH_HOMEPLUS_NONFIXED2` | (다른 JSP) | L81~82 |
| 6 (롯데) | `URL_SEARCH_BARCODE_INFO` | **A** | L74~76 |
| 7 (생산 라벨) | `URL_SEARCH_BARCODE_INFO` | **A** | L83~84 |

### 4.2 Java 파싱 구조 (공통)

| 작업 | 위치 (ProgressDlgBarcodeSearch.java) | 내용 |
|------|:----------------------------------:|------|
| ITEM_CODE 목록 수집 | L43~46 | 비정량(4/5): `selectqueryCodeListForNonFixed()`, 나머지: `selectqueryCodeList()` |
| WHERE 조건 구성 | L55~65 | `SBI.ITEM_CODE = 'xxx' OR ...` 형태로 조립 |
| 응답 split | L95~96 | `;;` 단위 행 분리 → `::` 단위 컬럼 분리 |
| temp[] 파싱 | L107~131 | `temp[0]`~`temp[23]` 공통 |
| SHELF_LIFE 분기 | L132~134 | `searchType != 4 && != 5` 일 때만 `temp[24]` 파싱 |
| 로컬 DB INSERT | L137 | `DBHandler.insertqueryBarcodeInfo()` → `TB_BARCODE_INFO` |

### 4.3 비정량 JSP(B)는 왜 24개인가

- 비정량 JSP는 원본 쿼리 자체가 24개 컬럼만 SELECT → `temp[24]` 위치에 데이터가 없음
- Java는 `searchType=4` 분기로 `temp[24]` 접근을 스킵 → 배열 인덱스 오류 방지
- **역방향 인과 관계**: Java가 스킵하게 설계된 이유는 비정량 JSP가 원본부터 SHELF_LIFE를 반환하지 않기 때문

---

## 5. 데이터 흐름 (원본 기준)

```
[PDA 앱] searchType 결정 → ProgressDlgBarcodeSearch
    ↓ ITEM_CODE 목록 수집 (TB_SHIPMENT)
    ↓ WHERE 조건 조립: "WHERE SBI.ITEM_CODE = 'CODE1' OR SBI.ITEM_CODE = 'CODE2' ..."
    ↓ HTTP POST (data 파라미터)
    │
    ├─ searchType=0,1,2,3,6,7 → [이마트 원본 JSP] (A)
    │     ↓ FROM S_BARCODE_INFO SBI
    │     ↓ INNER JOIN B_ITEM BI ON SBI.ITEMCODE = BI.ITEM_CODE AND BI.STATUS='Y'
    │     ↓ INNER JOIN B_SUPPLIER_ITEM BSI ON (패커·상품 2개 조건) + STATUS='Y'
    │     ↓ WHERE qry_where
    │     ↓ ORDER BY PACKER_PRODUCT_CODE ASC
    │     ↓ out.println 25개 컬럼 (SHELF_LIFE 포함)
    │
    └─ searchType=4 → [비정량 원본 JSP] (B)
          ↓ FROM B_ITEM sbi
          ↓ WHERE qry_where
          ↓ ORDER BY ITEMCODE ASC
          ↓ out.println 24개 컬럼 (대부분 하드코딩·품목코드 재사용)
    │
[PDA 앱] split(";;") → split("::", -1) → temp[]
    ↓ temp[0]~temp[23] 공통 파싱
    ↓ searchType != 4,5 일 때만 temp[24] (SHELF_LIFE) 추가 파싱
    ↓
DBHandler.insertqueryBarcodeInfo() → TB_BARCODE_INFO INSERT
```

---

## 6. 핵심 코드 — 원본 SQL 전문

### 6.1 이마트 원본 (A) 쿼리

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
    + "                              AND SBI.PACKER_PRODUCT_CODE = BSI.PACKER_PRODUCT_CODE"
    + "                              AND SBI.STATUS = 'Y' AND BSI.STATUS = 'Y'"
    + qry_where
    + " ORDER BY PACKER_PRODUCT_CODE ASC";
```

### 6.2 비정량 원본 (B) 쿼리

```jsp
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

### 6.3 SELECT 컬럼 1:1 대조표 (원본 기준)

| Index | SELECT 별칭            | 이마트 원본 (A) 출처                               | 비정량 원본 (B) 출처                       | 업무 의미       | 동일  |
| :---: | -------------------- | ------------------------------------------- | ----------------------------------- | ----------- | :-: |
|   0   | PACKER_CLIENT_CODE   | `SBI.PACKER_CLIENT_CODE` (S_BARCODE_INFO)   | `'이마트용'` (하드코딩)                     | 패커업체코드      |  ✕  |
|   1   | PACKER_PRODUCT_CODE  | `SBI.PACKER_PRODUCT_CODE` (S_BARCODE_INFO)  | `sbi.ITEM_CODE` (B_ITEM)            | 패커상품코드      |  ✕  |
|   2   | PACKER_PRD_NAME      | `SBI.PACKER_PRD_NAME` (S_BARCODE_INFO)      | `sbi.ITEM_NAME_KR` (B_ITEM)         | 패커상품명       |  ✕  |
|   3   | ITEMCODE             | `SBI.ITEMCODE` (S_BARCODE_INFO)             | `sbi.ITEM_CODE` (B_ITEM)            | 품목코드        |  ≈  |
|   4   | ITEM_NAME_KR         | `BI.ITEM_NAME_KR` (B_ITEM JOIN)             | `sbi.ITEM_NAME_KR` (B_ITEM)         | 품목명         |  ≈  |
|   5   | BRAND_CODE           | `SBI.BRAND_CODE` (S_BARCODE_INFO)           | `'0000'` (하드코딩)                     | 브랜드코드       |  ✕  |
|   6   | BARCODEGOODS         | `SBI.BARCODEGOODS` (S_BARCODE_INFO)         | `sbi.ITEM_CODE` (B_ITEM)            | 바코드상품코드     |  ✕  |
|   7   | BASEUNIT             | `SBI.BASEUNIT` (S_BARCODE_INFO)             | `'KG'` (하드코딩)                       | 기준단위        |  ✕  |
|   8   | ZEROPOINT            | `SBI.ZEROPOINT` (S_BARCODE_INFO)            | `SBI.ZEROPOINT` (B_ITEM)            | 소수점         |  ≈  |
|   9   | PACKER_PRD_CODE_FROM | `SBI.PACKER_PRD_CODE_FROM` (S_BARCODE_INFO) | `SBI.PACKER_PRD_CODE_FROM` (B_ITEM) | 패커상품코드 시작위치 |  ≈  |
|  10   | PACKER_PRD_CODE_TO   | `SBI.PACKER_PRD_CODE_TO` (S_BARCODE_INFO)   | `SBI.PACKER_PRD_CODE_TO` (B_ITEM)   | 패커상품코드 끝위치  |  ≈  |
|  11   | BARCODEGOODS_FROM    | `SBI.BARCODEGOODS_FROM` (S_BARCODE_INFO)    | `SBI.BARCODEGOODS_FROM` (B_ITEM)    | 바코드 시작위치    |  ≈  |
|  12   | BARCODEGOODS_TO      | `SBI.BARCODEGOODS_TO` (S_BARCODE_INFO)      | `SBI.BARCODEGOODS_TO` (B_ITEM)      | 바코드 끝위치     |  ≈  |
|  13   | WEIGHT_FROM          | `SBI.WEIGHT_FROM` (S_BARCODE_INFO)          | `SBI.WEIGHT_FROM` (B_ITEM)          | 중량 시작위치     |  ≈  |
|  14   | WEIGHT_TO            | `SBI.WEIGHT_TO` (S_BARCODE_INFO)            | `SBI.WEIGHT_TO` (B_ITEM)            | 중량 끝위치      |  ≈  |
|  15   | MAKINGDATE_FROM      | `SBI.MAKINGDATE_FROM` (S_BARCODE_INFO)      | `SBI.MAKINGDATE_FROM` (B_ITEM)      | 제조일자 시작위치   |  ≈  |
|  16   | MAKINGDATE_TO        | `SBI.MAKINGDATE_TO` (S_BARCODE_INFO)        | `SBI.MAKINGDATE_TO` (B_ITEM)        | 제조일자 끝위치    |  ≈  |
|  17   | BOXSERIAL_FROM       | `SBI.BOXSERIAL_FROM` (S_BARCODE_INFO)       | `SBI.BOXSERIAL_FROM` (B_ITEM)       | 박스시리얼 시작위치  |  ≈  |
|  18   | BOXSERIAL_TO         | `SBI.BOXSERIAL_TO` (S_BARCODE_INFO)         | `SBI.BOXSERIAL_TO` (B_ITEM)         | 박스시리얼 끝위치   |  ≈  |
|  19   | STATUS               | `SBI.STATUS` (S_BARCODE_INFO)               | `SBI.STATUS` (B_ITEM)               | 상태          |  ≈  |
|  20   | REG_ID               | `SBI.REG_ID` (S_BARCODE_INFO)               | `SBI.REG_ID` (B_ITEM)               | 등록자         |  ≈  |
|  21   | REG_DATE             | `SBI.REG_DATE` (S_BARCODE_INFO)             | `SBI.REG_DATE` (B_ITEM)             | 등록일         |  ≈  |
|  22   | REG_TIME             | `SBI.REG_TIME` (S_BARCODE_INFO)             | `SBI.REG_TIME` (B_ITEM)             | 등록시간        |  ≈  |
|  23   | MEMO                 | `SBI.MEMO` (S_BARCODE_INFO)                 | `'0000'` (하드코딩)                     | 메모          |  ✕  |
|  24   | SHELF_LIFE           | `BSI.SHELF_LIFE` (B_SUPPLIER_ITEM JOIN)     | **없음**                              | 유통기한        |  ✕  |

**범례**
- `≈` : 값은 동일하나 출처 테이블이 다름 (A는 S_BARCODE_INFO, B는 B_ITEM)
- `✕` : 완전히 다름 (테이블/하드코딩/출력여부 차이)

---

## 7. 비즈니스 로직 차이 분석 (원본 기준)

### 7.1 설계 철학의 근본적 차이

| 설계 요소 | 이마트 정량 | 비정량 |
|---------|-----------|--------|
| **바코드 파싱 규칙 관리 방식** | S_BARCODE_INFO 테이블로 **패커·상품별로 개별 관리** | **품목(B_ITEM)에 직접 포함** (패커 개념 없음) |
| **패커 체계** | 존재 (PACKER_CLIENT_CODE, PACKER_PRODUCT_CODE, PACKER_PRD_NAME) | **불필요** (PACKER_CLIENT_CODE='이마트용' 고정, PACKER_PRODUCT_CODE=품목코드) |
| **브랜드 체계** | 존재 (BRAND_CODE) | **불필요** (BRAND_CODE='0000' 고정) |
| **기준단위** | 가변 (BASEUNIT 컬럼) | **KG 고정** (비정량은 본질적으로 중량 제품) |
| **유통기한 관리** | B_SUPPLIER_ITEM JOIN으로 SHELF_LIFE 조회 | **관리 안 함** (라벨 출력에만 필요, 비정량은 현장 계근이므로 미사용) |
| **바코드 상품코드 매칭** | S_BARCODE_INFO.BARCODEGOODS (별도 바코드 코드) | **품목코드를 바코드로 직접 사용** |
| **상품명 출처** | B_ITEM JOIN 결과 | B_ITEM 단독 (JOIN 불요) |

### 7.2 진짜 업무 차이 5가지

1. **패커(Packer) 개념의 유무**
   - 이마트: 원료육은 여러 패커(공급사)가 공급 → 패커별로 다른 바코드 포맷 사용 → S_BARCODE_INFO로 패커별 규칙 분리 관리
   - 비정량: 사내 자체 계근 → 패커 구분 불필요 → `'이마트용'` 고정

2. **바코드 파싱 규칙의 소스**
   - 이마트: S_BARCODE_INFO (바코드 파싱 규칙 마스터) ← 패커와 협의한 포맷
   - 비정량: B_ITEM (품목 마스터에 직접 파싱 규칙 속성 보유) ← 회사 내부 포맷

3. **SHELF_LIFE(유통기한) 처리**
   - 이마트: 라벨 출력 필요 → B_SUPPLIER_ITEM 조인
   - 비정량: 현장 계근 → 유통기한은 별도 입력 또는 미사용 → 조회 불요

4. **3-way JOIN vs 단일 테이블**
   - 이마트: 바코드·품목·공급사 3개 영역의 정보 통합 필요 → 3-way JOIN
   - 비정량: 품목 1개 영역만 필요 → 단일 테이블

5. **상품코드 체계**
   - 이마트: BARCODEGOODS(바코드 상품코드) ≠ ITEMCODE(품목코드) ≠ PACKER_PRODUCT_CODE(패커상품코드) — 3종 코드 별도 관리
   - 비정량: BARCODEGOODS = ITEMCODE = PACKER_PRODUCT_CODE = `품목코드` — 단일 코드

### 7.3 공통 설계 규약

- temp[0]~temp[23] 24개 공통 구조 유지 → Java 파싱 코드 재사용
- SHELF_LIFE(temp[24])만 이마트 전용으로 확장

---

## 8. 원본 대비 현재(MSSQL 전환) 상태 요약 (참고)

본 문서는 **원본 비교가 주목적**이지만, 현재 전환 작업의 진척을 이해하기 위한 간략한 상태표를 포함한다.

| JSP | 원본 (Oracle) | 현재 | 전환 상태 |
|-----|-------------|------|:--------:|
| 이마트 (A) | `S_BARCODE_INFO` + `B_ITEM` + `B_SUPPLIER_ITEM` (3-way JOIN) | `CO_품목코드 SBI` 단일 | ✅ 완료 |
| 비정량 (B) | `B_ITEM sbi` 단일 | `B_ITEM sbi` 단일 (DB 접속만 MSSQL) | ❌ 미전환 (개발36) |

### 8.1 이마트 MSSQL 전환 시 구조 변화 (참고)

- **S_BARCODE_INFO가 MSSQL/HL ERP에 없음** → CO_품목코드로 통합, 일부 컬럼(`PACKER_CLIENT_CODE`, `PACKER_PRODUCT_CODE`, `BRAND_CODE`, `STATUS`, `REG_ID/DATE/TIME`, `MEMO`)이 **빈값 처리**됨
- **B_SUPPLIER_ITEM이 MSSQL/HL ERP에 없음** → SHELF_LIFE를 CO_품목코드의 `적용단위`/`적용값` CASE 계산식으로 대체
- 결과적으로 **이마트 MSSQL JSP는 원본 대비 일부 정보가 축소된 상태**로 동작 중 (업무상 영향 확인 필요할 수 있음)

### 8.2 비정량 MSSQL 전환 방향 (개발36 Step 1~3)

- `B_ITEM` → `CO_품목코드` 테이블 교체만 수행
- B_SUPPLIER_ITEM JOIN 없음 → 원래 SHELF_LIFE 미사용이라 영향 없음
- S_BARCODE_INFO 사용 없음 → 이마트 JSP 대비 전환 범위 훨씬 단순
- 하드코딩 값(`'이마트용'`, `'0000'`, `'KG'`, `'0000'`) 유지
- CO_품목코드에 없는 5개 컬럼(`PACKER_PRD_CODE_FROM/TO`, `REG_ID/DATE/TIME`)은 빈값 처리

---

## 9. 주의사항

- 본 문서의 모든 비교는 **원본 Oracle JSP(`apache-tomcat-7.0.78_PDA_IN(원본)`) 기준**이다.
- **이마트 JSP의 현재 MSSQL 버전은 원본과 구조가 다르므로**, 본 문서로 현재 JSP 간 비교를 대체할 수 없다.
- **비정량 JSP는 원본부터 `S_BARCODE_INFO`를 사용하지 않는다** — 이마트의 바코드 파싱 규칙 마스터 개념이 비정량에는 처음부터 없었음
- 비정량의 대부분 필드가 "하드코딩 + 품목코드 재사용"으로 구성된 것은 **버그나 미완성이 아니라 의도된 설계**
- MSSQL 전환 시 "이마트 방식을 그대로 적용"하려 하지 말 것 — 두 JSP는 본질적으로 다른 데이터 모델
- 개발36 Step 진행 시 원본 비정량 JSP의 24컬럼 구조를 그대로 유지하는 것이 CLAUDE.md 제1원칙 준수
- **이마트 MSSQL JSP가 이미 전환되었지만 원본 대비 정보 축소 상태**라는 점은 별도 검토 필요 사안 (본 문서 범위 외)

---

## 10. 관련 문서

- `app/doc/개발/36_비정량_바코드정보조회_JSP_MSSQL전환.md` — 비정량 JSP MSSQL 전환 개발 가이드 (Step 1~3)
- `app/doc/소스분석/45_비정량_바코드정보조회_JSP_컬럼_사용분석.md` — 비정량 24개 컬럼 사용/미사용 분석
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` — 이마트 전체 흐름 (정량 JSP 포함)
- `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md` — 비정량 전체 흐름 (비정량 JSP 포함)
- `app/doc/소스분석/41_이마트VIEW_vs_비정량VIEW_비교분석.md` — 출하대상 VIEW 레벨 원본 비교 (참고)
- `app/doc/소스분석/42_이마트Oracle_MSSQL_JSP_비정량Oracle_3자비교분석.md` — 출하대상 JSP 3자 비교 (참고)
- `app/doc/소스분석/43_이마트(0)_비정량(4)_Java파싱_공유분리_분석.md` — 출하대상 Java 파싱 공유분리 (참고)
- `app/doc/view/S_BARCODE_INFO.md` — S_BARCODE_INFO 테이블 분석 (이마트 원본 전용 테이블)
- **JSP 원본 이마트**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_barcode_info.jsp`
- **JSP 원본 비정량**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp`
- **JSP 현재 이마트 (MSSQL 전환 완료)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info.jsp`
- **JSP 현재 비정량 (MSSQL 미전환)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp`
- **Java 파싱**: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgBarcodeSearch.java`
