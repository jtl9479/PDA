# 홈플러스비정량(searchType=5) 바코드정보조회 JSP MSSQL 전환

**작성일**: 2026-08-04
**목적**: `search_homeplus_nonfixed2.jsp`의 Oracle 테이블(`B_ITEM`) 의존성을 제거하고 `CO_품목코드` 기반으로 교체한다. 이미 전환 완료된 비정량(searchType=4) `search_barcode_info_nonfixed.jsp`와 구조가 동일하므로 그 패턴을 그대로 적용한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

### 추가 제약 조건 (이 가이드 한정)

- 본 작업 범위는 **홈플러스비정량(searchType=5) 바코드정보조회 JSP 1개**에 한정한다.
- 출력 24개 컬럼의 **수와 순서를 변경하지 않는다.** `ProgressDlgBarcodeSearch`가 `temp[0]~temp[23]`으로 파싱한다.
- 앱 소스(`ProgressDlgBarcodeSearch.java` 등)는 수정하지 않는다. JSP 단독 수정으로 완결한다.
- searchType=4(`search_barcode_info_nonfixed.jsp`)의 동작에 영향을 주지 않는다.

---

## 1. 현재 구조

### 1.1 search_homeplus_nonfixed2.jsp (전환 전)

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_homeplus_nonfixed2.jsp`

```java
conn = getMSSQLConnection();          // 접속만 MSSQL 전환 완료

String qry_where = request.getParameter("data");   // 치환 없이 그대로 사용

String quertystring = "SELECT '홈플러스용' as PACKER_CLIENT_CODE"
    + ", sbi.ITEM_CODE as PACKER_PRODUCT_CODE"
    + …
    + " FROM B_ITEM sbi"              // Oracle 테이블
    + qry_where
    + " ORDER BY ITEMCODE ASC";
```

### 1.2 호출 경로

`ProgressDlgBarcodeSearch.java:84~85`
```java
}else if(Common.searchType.equals("5")) {
    receiveData = …sendDataDb(data, "inno", "search_barcode_info", Common.URL_SEARCH_HOMEPLUS_NONFIXED2);
}
```

`ProgressDlgBarcodeSearch.java:51~57` — 조회 조건 생성
```java
if(Common.searchType.equals("4") || Common.searchType.equals("5")){
    data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0] + "'";
}
```

`:71` — 회사코드 조건 (개발41)
```java
data = data + ") AND SBI.회사코드 = '" + Common.selectCompanyCode + "'";
```

### 1.3 문제점

| # | 문제 | 영향 |
|:-:|------|------|
| 1 | `B_ITEM`은 MSSQL에 존재하지 않음 | **조회 시 SQL 오류** |
| 2 | `qry_where`의 `SBI.ITEM_CODE` 미치환 | `CO_품목코드` 전환 후 컬럼명 불일치 |
| 3 | `CO_품목코드`에 없는 컬럼 5개 조회 | `PACKER_PRD_CODE_FROM/TO`, `REG_ID/DATE/TIME` |

### 1.4 현재 상태 — searchType=5 반쪽 동작

| 단계 | JSP | 상태 |
|------|-----|:--:|
| 출하대상받기 | `search_homeplus_nonfixed.jsp` | ✅ 전환 완료 (개발50) |
| **바코드정보조회** | **`search_homeplus_nonfixed2.jsp`** | ❌ **본 작업 대상** |
| 계근 전송 | `insert_goods_wet_new.jsp` | ✅ 전환 완료 |

대상은 받아지나 바코드 정보가 없어 **계근이 시작되지 않는다.**

---

## 2. 변경 구조

### 데이터 흐름

```
[홈플러스비정량 출하대상받기]
    ↓ search_homeplus_nonfixed.jsp        ✅ 전환완료
TB_SHIPMENT
    ↓ ProgressDlgShipSearch.onPostExecute
ProgressDlgBarcodeSearch                   :84
    ↓ SBI.ITEM_CODE = '...' AND SBI.회사코드 = '...'
search_homeplus_nonfixed2.jsp   ★ 본 작업
    ↓ CO_품목코드, 24개 컬럼
TB_BARCODE_INFO
    ↓
[계근입력시작] → 바코드 스캔 → find_work_info() 매칭
```

### 테이블 대응

| Oracle | HL_ERP | 근거 |
|--------|--------|------|
| `B_ITEM` | `CO_품목코드` | 개발36(searchType=4) 선례 |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_homeplus_nonfixed2.jsp** | `Tomcat/webapps/ROOT/inno/` | `FROM B_ITEM` → `CO_품목코드`, 컬럼명 전환, `qry_where` 치환 |

**앱 소스 수정 없음.**

---

## 4. 수정 상세

### 4.1 컬럼 매핑 (24개)

`search_barcode_info_nonfixed.jsp`(searchType=4, 전환완료)와 **컬럼 수·순서·값이 동일**하며 `PACKER_CLIENT_CODE` 문자열만 다르다.

| Idx | 별칭 | searchType=4 (전환완료) | **searchType=5 (본 작업)** |
|:--:|------|------------------------|---------------------------|
| 0 | `PACKER_CLIENT_CODE` | `'이마트용'` | **`'홈플러스용'`** (유지) |
| 1 | `PACKER_PRODUCT_CODE` | `SBI.품목코드` | `SBI.품목코드` |
| 2 | `PACKER_PRD_NAME` | `SBI.품목명` | `SBI.품목명` |
| 3 | `ITEMCODE` | `SBI.품목코드` | `SBI.품목코드` |
| 4 | `ITEM_NAME_KR` | `SBI.품목명` | `SBI.품목명` |
| 5 | `BRAND_CODE` | `'0000'` | `'0000'` |
| 6 | `BARCODEGOODS` | `SBI.품목코드` | `SBI.품목코드` |
| 7 | `BASEUNIT` | `'KG'` | `'KG'` |
| 8 | `ZEROPOINT` | `SBI.소수점` | `SBI.소수점` |
| 9 | `PACKER_PRD_CODE_FROM` | `''` | **`''`** |
| 10 | `PACKER_PRD_CODE_TO` | `''` | **`''`** |
| 11 | `BARCODEGOODS_FROM` | `SBI.바코드상품코드시작` | `SBI.바코드상품코드시작` |
| 12 | `BARCODEGOODS_TO` | `SBI.바코드상품코드끝` | `SBI.바코드상품코드끝` |
| 13 | `WEIGHT_FROM` | `SBI.중량시작` | `SBI.중량시작` |
| 14 | `WEIGHT_TO` | `SBI.중량끝` | `SBI.중량끝` |
| 15 | `MAKINGDATE_FROM` | `SBI.제조일자시작` | `SBI.제조일자시작` |
| 16 | `MAKINGDATE_TO` | `SBI.제조일자끝` | `SBI.제조일자끝` |
| 17 | `BOXSERIAL_FROM` | `SBI.박스시리얼시작` | `SBI.박스시리얼시작` |
| 18 | `BOXSERIAL_TO` | `SBI.박스시리얼끝` | `SBI.박스시리얼끝` |
| 19 | `STATUS` | `SBI.생산품상태` | `SBI.생산품상태` |
| 20 | `REG_ID` | `''` | **`''`** |
| 21 | `REG_DATE` | `''` | **`''`** |
| 22 | `REG_TIME` | `''` | **`''`** |
| 23 | `memo` | `'0000'` | `'0000'` |

**`''` 처리 5개 근거** — `소스분석/45_비정량_바코드정보조회_JSP_컬럼_사용분석.md`

| Idx | 컬럼 | 판정 |
|:--:|------|------|
| 9·10 | `PACKER_PRD_CODE_FROM/TO` | `:49` **미사용(get미호출)** — Log 출력만 |
| 20 | `REG_ID` | 로컬DB저장용 |
| 21 | `REG_DATE` | `:61` **미사용(파싱O·INSERT제외·get미호출)** |
| 22 | `REG_TIME` | `:62` 〃 |

`CO_품목코드`에 대응 컬럼이 없고 앱이 사용하지 않으므로 searchType=4와 동일하게 `''` 처리한다.

### 4.2 FROM 절

**변경 전:**
```sql
FROM B_ITEM sbi
```

**변경 후:**
```sql
FROM CO_품목코드 SBI
```

별칭을 `SBI`(대문자)로 통일한다. 현재는 `sbi`(소문자)와 `SBI`(대문자)가 혼용되어 있다.

### 4.3 qry_where 컬럼명 치환

**변경 전:** 치환 없음

**변경 후:**
```java
// Java(ProgressDlgBarcodeSearch.java)에서 WHERE SBI.ITEM_CODE = '...' 형태로 전송
// CO_품목코드의 실제 컬럼명은 '품목코드'이므로 ITEM_CODE → 품목코드로 치환
qry_where = qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드");
```

`search_barcode_info_nonfixed.jsp:35~37`과 동일한 처리다.

앱이 보내는 조건
```
 WHERE (SBI.ITEM_CODE = '...' OR SBI.ITEM_CODE = '...') AND SBI.회사코드 = '20'
```

치환 결과
```sql
 WHERE (SBI.품목코드 = '...' OR SBI.품목코드 = '...') AND SBI.회사코드 = '20'
```

`회사코드`는 `CO_품목코드`에 실재하므로 치환 불필요하다.

**검증**: Tomcat 로그 `##search_barcode_info query :` 에서 치환 결과 확인

---

## 5. 사이드이펙트

### 5.1 searchType=4 영향

**없음.** `search_barcode_info_nonfixed.jsp`는 수정하지 않는다. 두 JSP는 파일이 분리되어 있고 앱이 searchType으로 분기 호출한다.

### 5.2 앱 소스 영향

**없음.** 출력 24개 컬럼의 수와 순서를 유지하므로 `ProgressDlgBarcodeSearch`의 `temp[0]~temp[23]` 파싱이 그대로 동작한다.

### 5.3 원본 대비 동작 변경 1건

| 항목 | 원본 | 전환 후 | 근거 |
|------|------|---------|------|
| `PACKER_PRD_CODE_FROM/TO`, `REG_ID/DATE/TIME` | `B_ITEM` 실제 값 | `''` | `CO_품목코드`에 대응 컬럼 부재. 앱 미사용(`소스분석/45`). searchType=4 전환분과 동일 처리 |

---

## 6. 데이터 저장 구조

### 인덱스 매핑

```
JSP out.println[0]  = PACKER_CLIENT_CODE   ↔  temp[0]  → bi.setPACKER_CLIENT_CODE
JSP out.println[1]  = PACKER_PRODUCT_CODE  ↔  temp[1]  → bi.setPACKER_PRODUCT_CODE
…
JSP out.println[23] = memo                 ↔  temp[23] → bi.setMEMO
```

`ProgressDlgBarcodeSearch`가 `rsmd.getColumnName(n)` 순서로 받으므로 **SELECT 순서 = 출력 순서**다.

---

## 7. 호출 시점

```
[MainActivity]
    └── (비정량)홈플러스 출하대상받기 클릭
            ↓ downloadShipmentList(SEARCH_TYPE_HOMEPLUS_NONFIXED, …)
        ProgressDlgShipSearch
            ↓ search_homeplus_nonfixed.jsp → TB_SHIPMENT
            ↓ onPostExecute
        ProgressDlgBarcodeSearch                          :84
            ↓ ★ search_homeplus_nonfixed2.jsp
            ↓ TB_BARCODE_INFO
        ProgressDlgGoodsWetSearch
            ↓ search_goods_wet.jsp
[계근입력시작] → BixolonShipmentActivity → 바코드 스캔
```

---

## 8. 개발 플랜

### Step 1: JSP MSSQL 전환

**Part 1. 분석**
- 파일: `search_homeplus_nonfixed2.jsp`
- 범위: SELECT 24개 컬럼 + FROM + `qry_where` 치환
- 용도: `B_ITEM` → `CO_품목코드` 전환
- 주의할 점: 출력 컬럼 수·순서 불변, `PACKER_CLIENT_CODE`는 `'홈플러스용'` 유지

**Part 2. 변환 계획**
- 변환 방식: `search_barcode_info_nonfixed.jsp`(searchType=4) 패턴 적용
- 주의사항: `CO_품목코드`에 없는 5개 컬럼은 `''` 처리 (§4.1)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 쿼리 실행 확인 (MSSQL 직접 실행 — 24개 컬럼, 순서 일치, 오류 없음)
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용**
- **무엇을**: `FROM B_ITEM sbi` → `FROM CO_품목코드 SBI`, 컬럼명 19개 한글 전환, `qry_where` 치환 추가
- **왜**: `B_ITEM`은 MSSQL에 부재하여 searchType=5 바코드정보조회가 실패, 계근 진행 불가
- **어떻게**: searchType=4(`search_barcode_info_nonfixed.jsp`) 패턴 적용. 출력 24개 컬럼 수·순서 유지, `CO_품목코드` 미대응 5개는 `''` 처리

---

### Step 2: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | Tomcat 로그에 SQLException 없음 | □ |
| 2 | `##search_barcode_info query :` 치환 결과 확인 (`SBI.품목코드`) | □ |
| 3 | 홈플러스비정량 출하대상받기 → 대상 수신 | □ |
| 4 | `TB_BARCODE_INFO` INSERT 정상 (24개 컬럼) | □ |
| 5 | 계근입력시작 → 바코드 스캔 → 상품 매칭 성공 | □ |
| 6 | 중량 추출 정상 (`ITEM_TYPE` 분기) | □ |
| 7 | 전송 → `SM_출고계근` INSERT | □ |
| 8 | **비정량(4) 회귀** — 바코드정보조회·계근 정상 | □ |

---

### 개발 순서 요약

```
Step 1: JSP MSSQL 전환
    ↓
Step 2: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 홈플러스비정량 계근

```
1. PDA 로그인 → 회사/날짜 선택
2. (비정량)홈플러스 출하대상받기 → 대상 목록 수신
3. Tomcat 로그 확인
     ##search_barcode_info query : … FROM CO_품목코드 SBI WHERE (SBI.품목코드 = '…') AND SBI.회사코드 = '20'
4. (비정량)홈플러스 계근입력시작
5. 바코드 스캔 → 상품명/PP코드 표시 확인
6. 중량 추출 → 계근 저장
7. 전송 → SM_출고계근 확인
```

### 시나리오 2: 회귀 테스트

```
1. 비정량(4) 출하대상받기 → 계근 → 전송
   search_barcode_info_nonfixed.jsp 정상 동작 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|:-:|--------|------|----------|
| 1 | 조회 0건 | `CO_품목코드`에 해당 품목코드 없음 | 대상 품목의 마스터 등록 확인 |
| 2 | 바코드 매칭 실패 | `상품바코드`·`바코드상품코드시작/끝` 미등록 | ERP 품목코드관리(C0705)에서 등록 필요 |
| 3 | `NumberFormatException` | `바코드상품코드끝`이 빈값 → `Integer.parseInt("")` | `find_work_info()` 진입 전 마스터 확인 |
| 4 | 치환 누락으로 컬럼 오류 | `qry_where` replace 미적용 | Tomcat 로그의 쿼리 전문 확인 |
| 5 | 회사코드 조건 오류 | `CO_품목코드.회사코드` 부재 시 | 실재 확인됨 (`search_barcode_info.jsp` 사용 중) |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | JSP MSSQL 전환 | ✅ 완료 (쿼리 검증 통과) |
| 2 | 통합 테스트 | ⏳ 대기 |

---

## 12. 관련 문서

- `app/doc/개발/36_비정량_바코드정보조회_JSP_MSSQL전환.md` — searchType=4 전환 (본 작업의 원형)
- `app/doc/개발/50_홈플러스비정량_출하대상받기_JSP_MSSQL전환.md` — searchType=5 출하대상받기
- `app/doc/개발/41_바코드정보조회_WHERE절_회사코드_조건_추가.md` — 회사코드 조건 근거
- `app/doc/소스분석/45_비정량_바코드정보조회_JSP_컬럼_사용분석.md` — 24개 컬럼 사용여부 판정
- `app/doc/소스분석/53_바코드정보조회_JSP_MSSQL전환전_구조분석.md`
- `app/doc/개발/00_개발진행현황.md` — §4.1 부수 JSP 전환 현황
- `app/doc/문의사항/01_비정량_출하대상_타입구분_J코드_포함여부.md` — 비정량 타입구분

---

**문서 버전**: 1.0
