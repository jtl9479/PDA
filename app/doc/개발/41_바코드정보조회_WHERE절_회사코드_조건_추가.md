# 바코드정보조회 JSP WHERE절 회사코드 조건 추가

**작성일**: 2026-04-24
**목적**: `search_barcode_info.jsp`와 `search_barcode_info_nonfixed.jsp`의 WHERE 절에 회사코드 필터를 추가하여 멀티회사 환경에서 타 회사 품목 레코드 혼입 방지. CLAUDE.md 제1원칙의 "회사코드 예외" 규정을 적용한 선제적 개선.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다 (회사코드 예외 적용)

---

## 1. 현재 구조

### 1.1 Java (`ProgressDlgBarcodeSearch.java` L49~69)

```java
String data = " WHERE ";
for (int i = 0; i < list_code_info.size(); i++) {
    if(Common.searchType.equals("4") || Common.searchType.equals("5")){
        if (i == list_code_info.size() - 1) {
            data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "'";
        } else {
            data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
        }
    }else{
        if (i == list_code_info.size() - 1) {
            data = data + "SBI.PPCODE = '" + list_code_info.get(i)[0].toString() + "'";
        } else {
            data = data + "SBI.PPCODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
        }
    }
}

if(list_code_info.size() == 0){
    data = data + "1=0";
}
```

### 1.2 이마트 JSP (`search_barcode_info.jsp` L62~65)

```jsp
+ " FROM CO_품목코드 SBI"
+ qry_where
+ " AND SBI.ppCode != ''"
+ " ORDER BY PACKER_PRODUCT_CODE ASC"
```

### 1.3 비정량 JSP (`search_barcode_info_nonfixed.jsp` L36~64)

```jsp
qry_where = qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드");
String quertystring = "SELECT '이마트용' as PACKER_CLIENT_CODE"
    + ... (중략)
    + " FROM CO_품목코드 SBI"
    + qry_where
    + " ORDER BY ITEMCODE ASC";
```

### 1.4 현재 최종 조립되는 SQL (예시)

**이마트** (searchType=0):
```sql
SELECT ... FROM CO_품목코드 SBI
WHERE SBI.PPCODE = 'PC001' OR SBI.PPCODE = 'PC002' OR SBI.PPCODE = 'PC003'
  AND SBI.ppCode != ''
ORDER BY PACKER_PRODUCT_CODE ASC
```

**비정량** (searchType=4):
```sql
SELECT ... FROM CO_품목코드 SBI
WHERE SBI.품목코드 = 'IT001' OR SBI.품목코드 = 'IT002' OR SBI.품목코드 = 'IT003'
ORDER BY ITEMCODE ASC
```

### 문제점

1. **회사코드 필터 부재** — 멀티회사 운영 환경에서 타 회사 품목이 혼입될 가능성
2. **OR/AND 연산자 우선순위 결함** — 현재 `WHERE A OR B OR C AND 추가조건` 구조는 SQL에서 `WHERE A OR B OR (C AND 추가조건)`으로 해석됨. 이마트의 `AND SBI.ppCode != ''` 조건이 엄밀히는 마지막 OR 조건에만 적용 (다행히 실데이터상 문제 없음). 회사코드 추가 시 이 결함을 반드시 해결해야 정상 동작
3. **원본 Oracle 설계 근거 불명** — 원본 시절부터 회사코드 없었음이 확인됨(소스분석46 참조). 의도된 설계인지 레거시 누락인지 확정되지 않음(문의사항 Q03 대상). 본 작업은 **선제적 개선**이자 **제1원칙 "회사코드 예외" 적용**

---

## 2. 변경 구조

### 데이터 흐름

```
[Java: ProgressDlgBarcodeSearch.java]
  변경 전: "WHERE SBI.PPCODE='a' OR SBI.PPCODE='b' OR ..."
  변경 후: "WHERE (SBI.PPCODE='a' OR SBI.PPCODE='b' OR ...) AND SBI.회사코드='20'"
           ↑ 괄호로 OR 체인 감싸기 + 회사코드 AND 조건 추가
    ↓ HTTP POST (data 파라미터)
[JSP: 두 JSP 모두]
  변경 없음 (qry_where가 회사코드 포함한 형태로 전달되므로 JSP 내부 수정 불요)
    ↓
최종 SQL:
  이마트: WHERE (...) AND SBI.회사코드='20' AND SBI.ppCode != ''
  비정량: WHERE (...) AND SBI.회사코드='20'
```

### 변경 전/후 Java 로직 비교

| 항목 | 변경 전 | 변경 후 |
|------|--------|--------|
| data 초기값 | `" WHERE "` | `" WHERE ("` |
| 루프 내 OR 체인 | 동일 | 동일 (변경 없음) |
| size=0 처리 | `"1=0"` 추가 | `"1=0"` 추가 (동일) |
| 루프 종료 후 | (없음) | `") AND SBI.회사코드 = '" + Common.selectCompanyCode + "'"` 추가 |
| 연산자 우선순위 | 결함 (기존 버그) | 괄호로 해결 |
| 회사코드 필터 | ❌ 없음 | ✅ 추가 |

### CLAUDE.md 제1원칙 적용

- CLAUDE.md 명시: "회사코드 및 하드코딩의 경우를 제외한 나머지는 100% 동일해야한다"
- 본 변경은 **회사코드 추가**로 예외 규정 해당
- OR 괄호 추가는 기존 동작의 우발적 버그 수정 효과 (실데이터상 현재도 정상이나 이론적으로 잠재적 버그). 회사코드 추가가 필수 요구하는 구조 변경이므로 같은 커밋에 포함

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **ProgressDlgBarcodeSearch.java** | `app/src/main/java/com/rgbsolution/highland_emart/common/` | L49(data 초기값), L67~69(루프 후), 회사코드 AND 조건 추가 |

**수정 불필요 파일**:
- `search_barcode_info.jsp` — Java가 전달하는 qry_where에 회사코드 포함되므로 JSP 수정 불요
- `search_barcode_info_nonfixed.jsp` — 동일
- `DBHandler.java` — TB_BARCODE_INFO 저장 구조 변경 없음
- `Barcodes_Info.java` — 모델 변경 없음

---

## 4. 수정 상세

### 4.1 ProgressDlgBarcodeSearch.java

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgBarcodeSearch.java`

**변경 전 (L49~69):**

```java
String data = " WHERE ";
for (int i = 0; i < list_code_info.size(); i++) {
    if(Common.searchType.equals("4") || Common.searchType.equals("5")){
        if (i == list_code_info.size() - 1) {
            Log.d( TAG, "TEST DATA : " + list_code_info.get(i)[0].toString() );
            data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "'";
        } else {
            data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
        }
    }else{
        if (i == list_code_info.size() - 1) {
            data = data + "SBI.PPCODE = '" + list_code_info.get(i)[0].toString() + "'";
        } else {
            data = data + "SBI.PPCODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
        }
    }
}

if(list_code_info.size() == 0){
    data = data + "1=0";
}
```

**변경 후:**

```java
String data = " WHERE (";  // 괄호 시작
for (int i = 0; i < list_code_info.size(); i++) {
    if(Common.searchType.equals("4") || Common.searchType.equals("5")){
        if (i == list_code_info.size() - 1) {
            Log.d( TAG, "TEST DATA : " + list_code_info.get(i)[0].toString() );
            data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "'";
        } else {
            data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
        }
    }else{
        if (i == list_code_info.size() - 1) {
            data = data + "SBI.PPCODE = '" + list_code_info.get(i)[0].toString() + "'";
        } else {
            data = data + "SBI.PPCODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
        }
    }
}

if(list_code_info.size() == 0){
    data = data + "1=0";
}

// 괄호 닫고 회사코드 AND 조건 추가 (멀티회사 환경 대비)
data = data + ") AND SBI.회사코드 = '" + Common.selectCompanyCode + "'";
```

**변경 라인**:
- L49: `" WHERE "` → `" WHERE ("`
- L69 뒤 추가: `data = data + ") AND SBI.회사코드 = '" + Common.selectCompanyCode + "'";`

**검증**: 
- 이마트 최종: `WHERE (SBI.PPCODE='a' OR ...) AND SBI.회사코드='20' AND SBI.ppCode != ''`
- 비정량 최종: `WHERE (SBI.품목코드='a' OR ...) AND SBI.회사코드='20'` (비정량 JSP에서 SBI.ITEM_CODE → SBI.품목코드 replace 후)

---

## 5. 사이드이펙트

### search_barcode_info.jsp (수정 없음)

- 기존 `AND SBI.ppCode != ''` 조건은 그대로 유지
- 연산자 우선순위 문제 해결됨 (Java 괄호로 OR 체인 감싼 효과)

### search_barcode_info_nonfixed.jsp (수정 없음)

- 기존 `qry_where.replace("SBI.ITEM_CODE", "SBI.품목코드")` 로직은 그대로 유지
- Java에서 추가한 `SBI.회사코드` 문자열은 replace 대상 아님 (SBI.ITEM_CODE 리터럴만 매칭)
- 정상 작동

### 영향 범위 (searchType별)

| searchType | 호출 JSP | 영향 |
|:---------:|---------|:----:|
| 0 (이마트) | search_barcode_info.jsp | ✅ 회사코드 필터 적용 |
| 1 (생산) | search_barcode_info.jsp | ✅ 회사코드 필터 적용 |
| 2 (홈플러스) | search_barcode_info.jsp | ✅ 회사코드 필터 적용 |
| 3 (도매) | search_barcode_info.jsp | ✅ 회사코드 필터 적용 |
| 4 (비정량) | search_barcode_info_nonfixed.jsp | ✅ 회사코드 필터 적용 |
| 5 (홈플러스비정량) | search_homeplus_nonfixed2.jsp | ⚠️ 본 Java 수정 영향 받음 — 본 JSP는 수정 대상 아니나 qry_where에 회사코드 포함되어 전달됨. 해당 JSP의 WHERE 구조 확인 필요 |
| 6 (롯데) | search_barcode_info.jsp | ✅ 회사코드 필터 적용 |
| 7 (생산라벨) | search_barcode_info.jsp | ✅ 회사코드 필터 적용 |

**주의**: `search_homeplus_nonfixed2.jsp` (searchType=5)는 본 가이드의 수정 대상 JSP가 아님. Java 수정은 5번 searchType에도 회사코드 조건을 전달하게 되므로, 해당 JSP의 qry_where 처리 방식에 따라 영향 다름. **별도 확인 필요**.

---

## 6. 데이터 저장 구조

변경 없음. TB_BARCODE_INFO INSERT 구조·컬럼 영향 없음.

---

## 7. 호출 시점

변경 없음. 기존 호출 시점(출하대상 조회 완료 후 자동 실행) 그대로.

---

## 8. 개발 플랜

### Step 1: Java WHERE 절에 괄호 + 회사코드 조건 추가

**Part 1. 분석**
- 메서드: `ProgressDlgBarcodeSearch.doInBackground()`
- 범위: L49 (data 초기값), L67~69 (루프 후 회사코드 추가)
- 용도: qry_where 문자열에 괄호와 회사코드 AND 조건 추가
- 주의할 점:
  - 괄호 짝 맞춤 (`WHERE (` ... `)`)
  - 회사코드 컬럼명은 `SBI.회사코드` (MSSQL CO_품목코드 한글 컬럼)
  - `Common.selectCompanyCode` 이미 전역 상수로 존재 (다른 JSP에서 사용 중)
  - `list_code_info.size() == 0` 케이스에서도 괄호 짝 유지 (`WHERE (1=0) AND SBI.회사코드='20'`)

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | data 초기값 | L49 | `" WHERE "` → `" WHERE ("` |
| 2 | 회사코드 AND 추가 | L69 뒤 | `data = data + ") AND SBI.회사코드 = '" + Common.selectCompanyCode + "'";` 1줄 추가 |

**Part 2. 변환 계획**
- 변환 방식: Java 문자열 리터럴 2곳만 변경
- OR 체인 로직(for 루프 내부)은 수정 없음
- Common.selectCompanyCode는 기존에 이미 `ProgressDlgShipSearch.java:108`에서 사용 중인 값 → 재활용
- 주의사항: JSP 파일 수정은 하지 않음. Java 수정만으로 qry_where에 회사코드 포함 전달

**체크리스트**
- [ ] Part 1: 현재 L49, L67~69 구조 분석 완료
- [ ] Part 2: 괄호 + 회사코드 AND 구조 설계 확인
- [ ] Part 3: Java 파일 수정 수행
- [ ] Part 4: 컴파일 오류 없음 확인
- [ ] Part 5: 단위테스트 (Tomcat 로그에 조립된 쿼리 출력 확인)
- [ ] Part 6: 회귀테스트 (searchType=5 search_homeplus_nonfixed2.jsp 동작 확인 필요)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 2: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 이마트(searchType=0) 바코드정보조회 정상 동작 | □ |
| 2 | 비정량(searchType=4) 바코드정보조회 정상 동작 | □ |
| 3 | 이마트 JSP 로그에서 `WHERE (...)` 괄호 구조 확인 | □ |
| 4 | 이마트 JSP 로그에서 `AND SBI.회사코드 = '20'` 포함 확인 | □ |
| 5 | 비정량 JSP 로그에서 `WHERE (...)` 괄호 구조 확인 | □ |
| 6 | 비정량 JSP 로그에서 `AND SBI.회사코드 = '20'` 포함 확인 (replace 후 SBI.ITEM_CODE→SBI.품목코드로만 바뀌고 회사코드 유지) | □ |
| 7 | TB_BARCODE_INFO INSERT 건수가 변경 전과 동일하거나 감소 (타 회사 레코드 제외 시 감소) | □ |
| 8 | `list_code_info.size() == 0` 케이스 `WHERE (1=0) AND SBI.회사코드='20'` 문법 오류 없음 | □ |
| 9 | searchType 1/2/3/6/7 회귀 테스트 | □ |
| 10 | searchType 5 (search_homeplus_nonfixed2.jsp) 영향 확인 | □ |

---

### 개발 순서 요약

```
Step 1: Java WHERE 절 괄호 + 회사코드 조건 추가 (ProgressDlgBarcodeSearch.java 수정)
    ↓
Step 2: 통합 테스트 (8개 searchType 전부 회귀)
```

---

## 9. 테스트 시나리오

### 시나리오 1: 이마트 바코드정보조회 정상 흐름

```
1. searchType=0 (이마트) 선택
2. 출하대상 조회 → 바코드정보조회 자동 실행
3. Tomcat 로그 확인: "WHERE (SBI.PPCODE='PC001' OR SBI.PPCODE='PC002' ...) AND SBI.회사코드='20' AND SBI.ppCode != ''"
4. 응답 수신 건수가 기존 동작과 일치 (회사 '20' 레코드만 반환)
5. TB_BARCODE_INFO INSERT 정상
```

### 시나리오 2: 비정량 바코드정보조회 정상 흐름

```
1. searchType=4 (비정량) 선택
2. 출하대상 조회 → 바코드정보조회 자동 실행
3. Tomcat 로그 확인 (replace 후): "WHERE (SBI.품목코드='IT001' OR SBI.품목코드='IT002' ...) AND SBI.회사코드='20'"
4. 응답 수신 건수가 기존 동작과 일치
5. TB_BARCODE_INFO INSERT 정상
```

### 시나리오 3: 빈 리스트 케이스

```
1. list_code_info.size() == 0 (드문 케이스, TB_SHIPMENT에 ITEM_CODE 없을 때)
2. data = " WHERE (1=0) AND SBI.회사코드='20'"
3. 문법 오류 없이 정상 실행, 0건 반환
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 괄호 짝 불일치로 SQL 문법 오류 | `WHERE (` 시작 후 닫는 `)` 누락 | Step 1 Part 4 컴파일 확인 + 로그 확인 |
| 2 | `SBI.회사코드` 컬럼명 오타 | MSSQL 한글 컬럼명 오기재 | ERP Entity `ItemCodeEntity.java` 확인 — `BaseEntity`에서 `회사코드` 정의됨 |
| 3 | `Common.selectCompanyCode` null/빈값 | 로그인 시 설정 안 된 경우 | 기존 다른 JSP 호출 시 동일하게 사용 중이므로 실운영상 문제없음 |
| 4 | searchType 5 (홈플러스 비정량) 영향 | `search_homeplus_nonfixed2.jsp`가 회사코드 포함된 qry_where 받음 | 해당 JSP 구조 확인 후 별도 대응 (본 가이드 범위 외, 필요 시 개발42 신설) |
| 5 | 변경 후 반환 건수 감소 | 기존에 타 회사 레코드가 응답에 포함됐던 경우 | 업무 확인 필수 — 원래 타 회사 데이터가 필요했는지 확인 (문의사항 Q03으로 등록됨) |
| 6 | 회사코드 NULL인 레코드 누락 | CO_품목코드 일부 레코드의 회사코드가 NULL인 경우 | 실데이터 확인 후 NULL 허용 여부 결정 |

---

## 11. 사전 시뮬레이션 결과 (⑤ code-verifier)

**실행 일자**: 2026-04-24
**검증 단계**: 코드 수정 **전** 사전 시뮬레이션 (실제 Java 파일 미수정)
**최종 판정**: ✅ **GO** (searchType=5 별도 대응 조건부)

### 11.1 검증 항목별 결과

| # | 검증 항목 | 결과 | 핵심 발견 |
|:-:|----------|:----:|----------|
| 1 | CO_품목코드.회사코드 컬럼 존재 | ✅ PASS | `BaseEntity.java:42` `@Column(name = "회사코드", VARCHAR(2))` 정의. `ItemCodeEntity extends BaseEntity` |
| 2 | Common.selectCompanyCode 사용 선례 | ✅ PASS | `ProgressDlgShipSearch.java:108`에서 동일 패턴 정상 사용 중 |
| 3 | 이마트 케이스 SQL 문법 유효성 | ✅ PASS | `WHERE (SBI.PPCODE=... OR ...) AND SBI.회사코드='20' AND SBI.ppCode != ''` 문법 유효 |
| 4 | 비정량 케이스 SQL 문법 유효성 | ✅ PASS | JSP replace 후 `WHERE (SBI.품목코드=... OR ...) AND SBI.회사코드='20'` 문법 유효 |
| 5 | size=0 빈 배열 케이스 | ✅ PASS | `WHERE (1=0) AND SBI.회사코드='20'` 유효 |
| 6 | 괄호 짝 일치 | ✅ PASS | `" WHERE ("` 시작 + `") AND SBI.회사코드='...'"` 종료 1:1 대응 |
| 7 | 비정량 JSP replace 영향 없음 | ✅ PASS | `"SBI.ITEM_CODE"` 리터럴과 `"SBI.회사코드"` 문자열 겹치지 않음 |
| 8 | 연산자 우선순위 결함 해결 | ✅ PASS | 괄호 추가로 `WHERE (A OR B OR C) AND extra` 정상 해석 |
| 9 | **searchType=5 영향** | ⚠️ **WARN** | `search_homeplus_nonfixed2.jsp`는 `FROM B_ITEM sbi` 사용. MSSQL에 B_ITEM 테이블 존재 여부 및 회사코드 컬럼 유무 불명. **별도 대응 필요** |
| 10 | TB_BARCODE_INFO INSERT 영향 | ✅ PASS | INSERT 구조 변경 없음 |

### 11.2 searchType=5 상세 분석 (WARN)

**현황**:
- `ProgressDlgBarcodeSearch.java` L81~82: searchType=5 → `URL_SEARCH_HOMEPLUS_NONFIXED2` (별도 JSP)
- `search_homeplus_nonfixed2.jsp:64` `FROM B_ITEM sbi` (Oracle 레거시 테이블 사용, MSSQL 미전환 상태 가능성)
- ERP Entity에서 `B_ITEM` 대응 Entity 미발견

**본 변경의 영향**:
- Java 수정 후 searchType=5 호출 시 qry_where에 `AND SBI.회사코드 = '20'` 포함
- B_ITEM 테이블에 회사코드 컬럼이 없다면 SQL 오류 발생 가능
- 단, B_ITEM 자체가 MSSQL에 없다면 **본 변경 전에도 이미 실행 오류 상태** (기존 미검증)

**결론**: searchType=5는 본 가이드 scope 외. 필요 시 **개발42 별도 신설**하여 `search_homeplus_nonfixed2.jsp` MSSQL 전환 + 회사코드 대응 병행 처리.

### 11.3 결론

- searchType 0/1/2/3/4/6/7 (7개): Step 1 착수 가능
- searchType 5: 별도 처리 대상 (본 변경의 새로운 문제가 아니라 기존부터 미검증 상태)
- **Step 1 착수 가능**. Step 2 통합 테스트 시 searchType=5 실제 영향 확인 필요

---

## 12. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 사전 시뮬레이션 | ⑤ code-verifier 정합성 검증 | ✅ PASS (2026-04-24, searchType=5 WARN) |
| 1 | Java WHERE 절 괄호 + 회사코드 조건 추가 | ⏳ 대기 |
| 2 | 통합 테스트 (7개 searchType 회귀 + searchType=5 별도 확인) | ⏳ 대기 |

---

## 관련 문서

- `app/doc/개발/36_비정량_바코드정보조회_JSP_MSSQL전환.md` — 비정량 JSP 전환 선행 작업
- `app/doc/소스분석/46_이마트바코드정보조회_vs_비정량바코드정보조회_JSP_원본비교분석.md` — 원본 비교 (회사코드 부재 확인)
- `app/doc/개발/32_회사코드_packet수정[SM출고계근_회사코드_ITEM_CODE_잘림오류].md` — 회사코드 packet 선행 수정 (INSERT 측)
- `app/doc/오류/19_SM출고계근_회사코드_ITEM_CODE_잘림오류.md` — 회사코드 관련 선행 오류
- Java: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgBarcodeSearch.java`
- JSP 이마트: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info.jsp`
- JSP 비정량: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp`
- ERP Entity: `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\java\com\sgis\domain\co\bizbasic\entity\ItemCodeEntity.java`
- 참조: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java:108` (회사코드 사용 선례)

---

**문서 버전**: 1.0
