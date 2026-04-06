# search_barcode_info.jsp PPCODE 빈값 방어코드 추가

**작성일**: 2026-04-06
**목적**: PPCODE가 빈값인 품목이 조회되지 않도록 방어코드 추가

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### search_barcode_info.jsp

**파일**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info.jsp:62-64`

```java
+ " FROM CO_품목코드 SBI"
    + qry_where
+ " ORDER BY PACKER_PRODUCT_CODE ASC";
```

- CO_품목코드 테이블에서 PDA 동적 WHERE 조건(qry_where)으로 바코드 정보 조회
- PPCODE(패커상품코드)에 대한 빈값 필터링 없음

### 문제점

- CO_품목코드에 PPCODE가 빈값('')인 품목이 존재할 수 있음
- 빈값 품목이 조회되면 PDA에서 바코드 매칭 시 불필요한 데이터가 TB_BARCODE_INFO에 저장됨
- find_work_info()에서 빈값 PPCODE와 매칭 시도 → 오동작 가능

---

## 2. 변경 구조

### 데이터 흐름

```
[변경 전]
CO_품목코드 WHERE qry_where → PPCODE 빈값 포함 조회
    ↓
PDA TB_BARCODE_INFO에 빈값 PPCODE 품목 저장
    ↓
find_work_info()에서 불필요한 매칭 시도

[변경 후]
CO_품목코드 WHERE qry_where AND SBI.ppCode != '' → PPCODE 있는 품목만 조회
    ↓
PDA TB_BARCODE_INFO에 유효한 품목만 저장
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_barcode_info.jsp** | 62-64줄 | WHERE 조건에 `AND SBI.ppCode != ''` 추가 |

---

## 4. 수정 상세

### 4.1 search_barcode_info.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info.jsp`

**변경 전:**

```java
+ " FROM CO_품목코드 SBI"
    + qry_where
+ " ORDER BY PACKER_PRODUCT_CODE ASC";
```

**변경 후:**

```java
+ " FROM CO_품목코드 SBI"
    + qry_where
    + " AND SBI.ppCode != ''"
+ " ORDER BY PACKER_PRODUCT_CODE ASC";
```

**검증**: PPCODE가 빈값인 품목이 조회 결과에서 제외되는지 확인

---

## 5. 사이드이펙트

### PDA 파싱 (ProgressDlgBarcodeSearch.java)

- 파싱 로직 변경 없음 (temp[] 인덱스 동일)
- 조회 결과 건수만 줄어듬 (PPCODE 빈값 품목 제외)
- 기존 정상 품목(PPCODE 있는)은 그대로 조회됨

### 영향 없는 파일

- DBHandler.java: 변경 없음
- BixolonShipmentActivity.java: 변경 없음
- TB_BARCODE_INFO: 구조 변경 없음

---

## 6. 개발 플랜

### Step 1: search_barcode_info.jsp WHERE 조건 추가

**Part 1. 분석**
- 메서드: search_barcode_info.jsp 쿼리
- 범위: 62-64줄
- 용도: PPCODE 빈값 품목 조회 방지
- 주의할 점: 기존 qry_where 뒤에 AND 조건 추가 (기존 조건 유지)

**Part 2. 변환 계획**
- 변환 방식: `qry_where` 뒤에 `" AND SBI.ppCode != ''"` 1줄 추가
- 주의사항: 기존 WHERE 조건 변경/삭제 없음

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 서버 배포 확인
- [ ] Part 5: PDA 출하대상받기 → 바코드 정보 조회 정상 확인

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### 개발 순서 요약

```
Step 1: search_barcode_info.jsp WHERE 조건 추가 (AND SBI.ppCode != '')
```

---

## 7. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | search_barcode_info.jsp WHERE 조건 추가 | ⏳ 대기 |

---

**문서 버전**: 1.0
