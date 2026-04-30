# 정량/비정량 이마트 출하대상 조회 미계근 조건 NOT EXISTS 추가

**작성일**: 2026-04-30
**목적**: PDA 출하대상 조회 JSP(정량 이마트 `search_shipment.jsp`, 비정량 이마트 `search_production_nonfixed.jsp`)에서 원본 Oracle VIEW의 `ID.PACKING_QTY = 0` 조건(미계근 건만 조회)이 MSSQL 전환 시 누락된 문제를 NOT EXISTS 패턴으로 복원한다. 이미 계근된 건이 다시 출하대상 목록에 노출되지 않도록 하여 "기존 기능 100% 동일" 원칙을 준수한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

### 추가 제약 조건 (이 가이드 한정)

- 본 작업 범위는 **정량 이마트(searchType=0)** + **비정량 이마트(searchType=4)** 2개 JSP에 한정한다. 홈플러스 정량/비정량, 롯데, 도매 등 다른 마트사 JSP는 별도 가이드로 분리한다.
- WHERE 절의 기존 조건(`마트사구분`, `출고수량`, `타입구분`, `바코드타입`)은 절대 변경하지 않는다.
- SELECT 컬럼 / JOIN 절 / ORDER BY 는 변경하지 않는다.
- NOT EXISTS subquery 내 컬럼명(`출고상세SEQ`, `회사코드`)은 ERP `SM_출고계근` 테이블 정의 기준으로 절대 변경하지 않는다.

---

## 배경 및 의사결정 기록

### 원본 Oracle VIEW 조건

원본 PDA 시스템에서는 Oracle VIEW(`이마트출하대상_VIEW` 계열)에 `ID.PACKING_QTY = 0` 조건이 포함되어 있었다. 이 조건은 "아직 계근되지 않은 건(PACKING_QTY = 0)만 출하대상으로 조회"를 의미한다.

MSSQL 전환 시 직접 JOIN 쿼리로 재작성하면서 이 조건이 누락되었다.

### 복원 방법 검토 (2026-04-30 사용자 결정)

| 옵션 | 방법 | 비고 |
|------|------|------|
| 옵션 1 | `D.계근여부 = 'N'` (`SM_출고상세` 컬럼 활용) | SM_출고상세에 계근여부 컬럼 존재 여부 및 갱신 타이밍 확인 필요 |
| **옵션 2** | `NOT EXISTS (SELECT 1 FROM SM_출고계근 W WHERE W.출고상세SEQ = D.SEQ AND W.회사코드 = D.회사코드)` | ERP 표준 패턴, 정합성 강함, **채택** |

**사용자 결정**: 옵션 2 = NOT EXISTS 패턴 사용. 원본 `PACKING_QTY = 0` 의미와 정확히 일치하며, ERP 내 여러 SQL(예: `mapper/ac/evrench/A0808_SQL.xml`, `mapper/co/bizbasic/C0109_SQL.xml`)에서 NOT EXISTS 패턴이 일반적으로 사용되는 선례 확인. (단, 인용 SQL은 SM_출고계근 직접 사용 사례가 아닌 NOT EXISTS 패턴 자체의 사용 선례임.)

### 테이블명 오기 주의

`app/doc/문의사항/04_출하대상받기_4유형_조회조건_종합정리.txt` 1.6/2.5 명세에 **"SM_출하계근"** 으로 표기되어 있으나, ERP 실재 테이블명은 **`SM_출고계근`** 이다(`DlivyWeighEntity.java` 기준). 본 가이드 및 실제 코드에서는 정확한 테이블명 `SM_출고계근`을 사용한다.

---

## 1. 현재 구조

### 1.1 search_shipment.jsp (정량 이마트, L114~118)

```jsp
+ " WHERE H.마트사구분 = '7'"
+ "   AND D.출고수량 > 0"
+ "   AND COALESCE(M1.타입구분, M2.타입구분) = 'W'"
+ qry_where
+ " ORDER BY GI_D_ID ASC"
```

- 미계근 조건 없음 — 계근 완료된 건도 출하대상 목록에 포함되어 반환됨.

### 1.2 search_production_nonfixed.jsp (비정량 이마트, L125~130)

```jsp
+ " WHERE H.마트사구분 = '7'"
+ "   AND D.출고수량 > 0"
+ "   AND COALESCE(M1.타입구분, M2.타입구분) = 'B'"
+ "   AND COALESCE(M1.바코드타입, M2.바코드타입) IN ('M8', 'M9')"
+ qry_where
+ " ORDER BY GI_D_ID ASC"
```

- 동일하게 미계근 조건 없음.

### 문제점

- 원본 Oracle VIEW의 `ID.PACKING_QTY = 0`(미계근 건만 조회) 조건이 MSSQL 전환 과정에서 누락됨.
- 이미 계근된 출하 건이 출하대상 목록에 재노출될 수 있음.
- "기존 기능 100% 동일" 원칙(CLAUDE.md) 위반 가능성.

---

## 2. 변경 구조

### 데이터 흐름

```
[SM_출고계근] ── INSERT (insert_goods_wet.jsp / insert_goods_wet_new.jsp)
    ↓  계근 완료 시 레코드 생성
[search_shipment.jsp / search_production_nonfixed.jsp]
    ↓  NOT EXISTS 로 SM_출고계근 레코드 존재 여부 확인
[출하대상 결과]
    ↓  SM_출고계근에 레코드 없는(미계근) 건만 반환
[PDA 출하대상 목록 표시]
```

### 변경 전/후 WHERE 절 비교

**search_shipment.jsp**

```
[변경 전]
WHERE H.마트사구분 = '7'
  AND D.출고수량 > 0
  AND COALESCE(M1.타입구분, M2.타입구분) = 'W'
  {qry_where}

[변경 후]
WHERE H.마트사구분 = '7'
  AND D.출고수량 > 0
  AND COALESCE(M1.타입구분, M2.타입구분) = 'W'
  AND NOT EXISTS (SELECT 1 FROM SM_출고계근 W WHERE W.출고상세SEQ = D.SEQ AND W.회사코드 = D.회사코드)
  {qry_where}
```

**search_production_nonfixed.jsp**

```
[변경 전]
WHERE H.마트사구분 = '7'
  AND D.출고수량 > 0
  AND COALESCE(M1.타입구분, M2.타입구분) = 'B'
  AND COALESCE(M1.바코드타입, M2.바코드타입) IN ('M8', 'M9')
  {qry_where}

[변경 후]
WHERE H.마트사구분 = '7'
  AND D.출고수량 > 0
  AND COALESCE(M1.타입구분, M2.타입구분) = 'B'
  AND COALESCE(M1.바코드타입, M2.바코드타입) IN ('M8', 'M9')
  AND NOT EXISTS (SELECT 1 FROM SM_출고계근 W WHERE W.출고상세SEQ = D.SEQ AND W.회사코드 = D.회사코드)
  {qry_where}
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_shipment.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\` L117 다음 줄 | WHERE 절 끝(`qry_where` 앞)에 NOT EXISTS 조건 1줄 추가 |
| 2 | **search_production_nonfixed.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\` L129 다음 줄 | WHERE 절 끝(`qry_where` 앞)에 NOT EXISTS 조건 1줄 추가 |

---

## 4. 수정 상세

### 4.1 search_shipment.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment.jsp`

**변경 전 (L114~118):**

```jsp
+ " WHERE H.마트사구분 = '7'"
+ "   AND D.출고수량 > 0"
+ "   AND COALESCE(M1.타입구분, M2.타입구분) = 'W'"
+ qry_where
+ " ORDER BY GI_D_ID ASC";
```

**변경 후:**

```jsp
+ " WHERE H.마트사구분 = '7'"
+ "   AND D.출고수량 > 0"
+ "   AND COALESCE(M1.타입구분, M2.타입구분) = 'W'"
+ "   AND NOT EXISTS (SELECT 1 FROM SM_출고계근 W WHERE W.출고상세SEQ = D.SEQ AND W.회사코드 = D.회사코드)"
+ qry_where
+ " ORDER BY GI_D_ID ASC";
```

**검증**: 쿼리 로그(`##search_shipment query :`)에서 NOT EXISTS 구문 포함 여부 확인. 계근된 건의 `SM_출고계근` 레코드 존재 시 해당 건이 결과에서 제외되는지 확인.

---

### 4.2 search_production_nonfixed.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_production_nonfixed.jsp`

**변경 전 (L125~130):**

```jsp
+ " WHERE H.마트사구분 = '7'"
+ "   AND D.출고수량 > 0"
+ "   AND COALESCE(M1.타입구분, M2.타입구분) = 'B'"
+ "   AND COALESCE(M1.바코드타입, M2.바코드타입) IN ('M8', 'M9')"
+ qry_where
+ " ORDER BY GI_D_ID ASC";
```

**변경 후:**

```jsp
+ " WHERE H.마트사구분 = '7'"
+ "   AND D.출고수량 > 0"
+ "   AND COALESCE(M1.타입구분, M2.타입구분) = 'B'"
+ "   AND COALESCE(M1.바코드타입, M2.바코드타입) IN ('M8', 'M9')"
+ "   AND NOT EXISTS (SELECT 1 FROM SM_출고계근 W WHERE W.출고상세SEQ = D.SEQ AND W.회사코드 = D.회사코드)"
+ qry_where
+ " ORDER BY GI_D_ID ASC";
```

**검증**: 쿼리 로그(`##search_production_nonfixed query :`)에서 NOT EXISTS 구문 포함 여부 확인. 계근된 비정량 건이 결과에서 제외되는지 확인.

---

## 5. 사이드이펙트

### 5.1 출하대상 결과 수 변화

- 계근 완료된 건이 출하대상 목록에서 제외되므로 조회 결과 수가 줄어들 수 있음.
- **이것이 의도된 동작**이며 원본 Oracle 시스템과 동일한 결과.

### 5.2 부분 계근 케이스 (운영 확인 필요)

- `SM_출고계근`에 해당 `출고상세SEQ`로 1건이라도 레코드가 존재하면 NOT EXISTS 조건에 의해 출하대상에서 제외됨.
- **부분 계근 케이스**(여러 박스 중 일부만 계근한 경우) 시, 해당 출하 건 전체가 목록에서 사라짐.
- 운영 정책상 부분 계근된 건을 출하대상에 계속 남겨야 한다면 NOT EXISTS 대신 `LEFT JOIN + COUNT = 0` 또는 `SUM(계근수량) < 출고수량` 형태의 조건으로 수정 필요.
- **현재는 원본 `PACKING_QTY = 0` 조건과 동일한 의미로 NOT EXISTS 적용** — 운영팀 확인 권장.

### 5.3 다른 마트사 JSP 동일 문제 가능성

- 홈플러스 정량(`search_homplus.jsp`), 홈플러스 비정량, 롯데(`search_lotte.jsp`), 도매 등 다른 마트사 JSP에도 동일하게 미계근 조건이 누락되어 있을 가능성이 있음.
- **본 가이드 범위 외** — 별도 가이드로 분리하여 진행할 것.

### 5.4 SM_출고계근 인덱스 없는 경우 성능

- NOT EXISTS subquery가 `SM_출고계근`을 `출고상세SEQ`와 `회사코드`로 검색함.
- 해당 컬럼에 인덱스가 없으면 출하 건수가 많을 때 성능 저하 가능성.
- ERP DB에서 인덱스 존재 여부 확인 권장.

---

## 6. 데이터 저장 구조

### SM_출고계근 주요 컬럼 (ERP DlivyWeighEntity.java 기준)

| 컬럼명 | 용도 |
|--------|------|
| `출고상세SEQ` | SM_출고상세(D).SEQ와 JOIN 키 |
| `회사코드` | 다중 회사 구분 키 |

### NOT EXISTS 연결 구조

```
SM_출고상세 (D)
    D.SEQ          ←→    SM_출고계근 (W).출고상세SEQ
    D.회사코드      ←→    SM_출고계근 (W).회사코드
```

SM_출고계근에 해당 SEQ + 회사코드 조합의 레코드가 없으면 = 미계근 = 출하대상 포함.

---

## 7. 호출 시점

```
[MainActivity]
    ↓ 출하대상받기 메뉴 선택 / downloadShipmentList(searchType) 호출
[ProgressDlgShipSearch.doInBackground()]
    ├── searchType = 0 (정량 이마트)
    │       ↓ HTTP 요청
    │   [search_shipment.jsp]
    │       ↓ NOT EXISTS 조건 포함 MSSQL 쿼리 실행
    │   [SM_출고상세, SM_출고헤더, SM_출고계근 서브쿼리]
    │       ↓ 미계근 건만 결과 반환
    │   [ProgressDlgShipSearch.onPostExecute() → 로컬DB 저장]
    │
    └── searchType = 4 (비정량 이마트)
            ↓ HTTP 요청
        [search_production_nonfixed.jsp]
            ↓ NOT EXISTS 조건 포함 MSSQL 쿼리 실행
        [SM_출고상세, SM_출고헤더, SM_출고계근 서브쿼리]
            ↓ 미계근 + M8/M9 바코드타입 건만 결과 반환
        [ProgressDlgShipSearch.onPostExecute() → 로컬DB 저장]
```

---

## 8. 개발 플랜

### Step 1: search_shipment.jsp NOT EXISTS 추가 (정량 이마트)

**Part 1. 분석**
- 메서드: JSP WHERE 절 문자열 연결부
- 범위: `search_shipment.jsp` L114~118
- 용도: 정량 이마트(searchType=0) 출하대상 조회에서 미계근 건만 반환하도록 조건 추가
- 주의할 점:
  - `qry_where` 앞에 삽입해야 함 — `qry_where` 뒤에 추가하면 AND 연결 오류 가능
  - `SM_출고계근` 테이블명 오기(`SM_출하계근`) 절대 금지
  - 기존 3개 WHERE 조건(`마트사구분`, `출고수량`, `타입구분`) 변경 금지

| # | 항목 | 위치 | 내용 |
|:-:|------|------|------|
| 1 | 현재 WHERE 절 끝 | L117 (`COALESCE...= 'W'` 줄) | NOT EXISTS 삽입 위치 확인 |
| 2 | qry_where 연결 | L117~118 | NOT EXISTS 이후 qry_where가 오도록 순서 유지 |
| 3 | 테이블 별칭 충돌 | JSP 전체 FROM/JOIN | 별칭 `W`가 기존 JOIN에서 사용되지 않는지 확인 |

**Part 2. 변환 계획**
- 변환 방식: L117(`COALESCE(M1.타입구분, M2.타입구분) = 'W'` 줄) 다음에 NOT EXISTS 줄 1개 추가
- 주의사항: `qry_where` 위치를 변경하지 않고 NOT EXISTS 줄만 중간에 삽입

**체크리스트**
- [ ] Part 1: search_shipment.jsp L114~118 현재 코드 확인
- [ ] Part 2: 별칭 `W` 충돌 없음 확인
- [ ] Part 3: NOT EXISTS 줄 삽입
- [ ] Part 4: JSP 저장 후 Tomcat 로그에서 쿼리 문자열 확인
- [ ] Part 5: 미계근 데이터 조회 → 목록에 표시 확인
- [ ] Part 6: 회귀테스트 (다른 searchType 영향 없음 확인)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 2: search_production_nonfixed.jsp NOT EXISTS 추가 (비정량 이마트)

**Part 1. 분석**
- 메서드: JSP WHERE 절 문자열 연결부
- 범위: `search_production_nonfixed.jsp` L125~130
- 용도: 비정량 이마트(searchType=4) 출하대상 조회에서 미계근 건만 반환하도록 조건 추가
- 주의할 점:
  - Step 1과 동일한 NOT EXISTS 문장 사용 (컬럼명 동일)
  - `qry_where` 앞, 기존 4개 조건 변경 없이 삽입
  - 개발43/44에서 추가된 `바코드타입 IN ('M8', 'M9')` 조건을 절대 제거하거나 수정하지 않음

| # | 항목 | 위치 | 내용 |
|:-:|------|------|------|
| 1 | 현재 WHERE 절 끝 | L128 (`바코드타입 IN ('M8', 'M9')` 줄) | NOT EXISTS 삽입 위치 확인 |
| 2 | qry_where 연결 | L129 | NOT EXISTS 이후 qry_where가 오도록 순서 유지 |
| 3 | 테이블 별칭 충돌 | JSP 전체 FROM/JOIN | 별칭 `W`가 기존 JOIN에서 사용되지 않는지 확인 |

**Part 2. 변환 계획**
- 변환 방식: L128(`바코드타입 IN ('M8', 'M9')` 줄) 다음에 NOT EXISTS 줄 1개 추가
- 주의사항: `qry_where` 위치를 변경하지 않고 NOT EXISTS 줄만 중간에 삽입

**체크리스트**
- [ ] Part 1: search_production_nonfixed.jsp L125~130 현재 코드 확인
- [ ] Part 2: 별칭 `W` 충돌 없음 확인
- [ ] Part 3: NOT EXISTS 줄 삽입
- [ ] Part 4: JSP 저장 후 Tomcat 로그에서 쿼리 문자열 확인
- [ ] Part 5: 미계근 비정량 데이터 조회 → 목록에 표시 확인
- [ ] Part 6: 회귀테스트 (searchType=0 등 다른 타입 영향 없음 확인)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 3: 통합 테스트

**Part 1. 분석**
- 메서드: 전체 출하대상 조회 흐름
- 범위: PDA 앱 → JSP → MSSQL → 결과 반환 전 과정
- 용도: Step 1/2 적용 후 미계근 조건이 정상 동작하는지 확인
- 주의할 점: 실제 계근 데이터(`SM_출고계근`)가 DB에 존재하는 환경에서 테스트

**Part 2. 변환 계획**
- 변환 방식: code-verifier(⑤) + original-comparator(⑥) 사후 검증 후 PDA 실기 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 정량 이마트: 미계근 출하 건이 목록에 표시됨 | □ |
| 2 | 정량 이마트: 계근 완료 후 동일 건이 목록에서 사라짐 | □ |
| 3 | 비정량 이마트: 미계근 비정량 출하 건이 목록에 표시됨 | □ |
| 4 | 비정량 이마트: 계근 완료 후 동일 건이 목록에서 사라짐 | □ |
| 5 | 부분 계근(일부 박스만 계근) 시 출하대상에서 제외 여부 — 운영 정책 일치 여부 확인 | □ |
| 6 | searchType=0/4 이외의 다른 searchType(홈플, 롯데 등) 정상 동작 영향 없음 | □ |
| 7 | Tomcat 쿼리 로그에서 두 JSP 모두 NOT EXISTS 구문 포함 확인 | □ |

**체크리스트**
- [ ] Part 1: Step 1 변경 적용 확인
- [ ] Part 2: Step 2 변경 적용 확인
- [ ] Part 3: code-verifier(⑤) 검증 완료
- [ ] Part 4: original-comparator(⑥) 검증 완료
- [ ] Part 5: PDA 실기 테스트 7개 항목 전체 통과
- [ ] Part 6: 통합 테스트 완료 처리

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### 개발 순서 요약

```
Step 1: search_shipment.jsp NOT EXISTS 추가 (정량 이마트)
    ↓
Step 2: search_production_nonfixed.jsp NOT EXISTS 추가 (비정량 이마트)
    ↓
Step 3: 통합 테스트 (code-verifier + original-comparator + PDA 실기)
```

---

## 9. 테스트 시나리오

### 시나리오 1: 정량 이마트 미계근 건만 조회

```
1. PDA 앱 실행 → searchType=0 (정량 이마트) 선택
2. 출하대상 받기 실행
3. SM_출고계근에 레코드가 없는 출하 건 → 목록에 표시됨 확인
4. 기대값: 미계근 건만 출하대상 목록에 표시
```

### 시나리오 2: 정량 이마트 계근 후 목록에서 제거

```
1. 특정 출하 건(GI_D_ID 확인)이 출하대상 목록에 표시됨 확인
2. 해당 건에 대해 계근 수행 (insert_goods_wet.jsp → SM_출고계근 INSERT)
3. 출하대상 다시 조회 (출하대상 받기 재실행)
4. 기대값: 계근된 건이 목록에서 사라짐
```

### 시나리오 3: 비정량 이마트 미계근 건만 조회

```
1. PDA 앱 실행 → searchType=4 (비정량 이마트) 선택
2. 출하대상 받기 실행
3. SM_출고계근에 레코드가 없는 비정량 출하 건 → 목록에 표시됨 확인
4. 기대값: 미계근 비정량(M8/M9 바코드타입) 건만 출하대상 목록에 표시
```

### 시나리오 4: 비정량 이마트 계근 후 목록에서 제거

```
1. 특정 비정량 출하 건(GI_D_ID 확인)이 출하대상 목록에 표시됨 확인
2. 해당 건에 대해 계근 수행 (insert_goods_wet_new.jsp → SM_출고계근 INSERT)
3. 출하대상 다시 조회 (출하대상 받기 재실행)
4. 기대값: 계근된 비정량 건이 목록에서 사라짐
```

### 시나리오 5: 부분 계근 동작 확인 (운영 정책 검증)

```
1. 여러 박스로 구성된 출하 건 중 박스 1개만 계근 수행
   (SM_출고계근에 해당 출고상세SEQ로 1건 INSERT)
2. 출하대상 다시 조회
3. 확인 사항: 해당 출하 건 전체가 목록에서 제거되는지 여부
4. 운영팀과 정책 일치 여부 확인:
   - "1건이라도 계근 시 전체 제외" → 현재 NOT EXISTS 동작이 맞음
   - "전량 계근 후에만 제외" → 조건 수정 필요 (별도 가이드)
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|:-:|--------|------|----------|
| 1 | NOT EXISTS subquery 성능 저하 | SM_출고계근의 `출고상세SEQ`, `회사코드` 컬럼에 인덱스 없음 | ERP DB에서 인덱스 존재 여부 확인 후 없으면 DBA에 인덱스 생성 요청 |
| 2 | 부분 계근 운영 정책 불일치 | NOT EXISTS는 1건이라도 계근 시 전체 제외 | 운영팀 정책 확인 후 "전량 계근 후 제외" 가 필요하면 `LEFT JOIN + SUM 카운트 = 0` 조건으로 변경 |
| 3 | 다른 마트사 JSP 동일 문제 | MSSQL 전환 시 홈플/롯데/도매 JSP에도 동일하게 미계근 조건 누락 가능 | 별도 가이드 작성 후 각 JSP에 동일 조건 추가 |
| 4 | 별칭 `W` 충돌 | JSP 내 기존 JOIN에서 별칭 `W`를 이미 사용하는 경우 | 두 JSP에서 별칭 `W` 사용 여부 사전 확인, 충돌 시 `WG` 등 다른 별칭으로 변경 |
| 5 | SM_출고계근 테이블명 오기 | 문의사항 04번에 "SM_출하계근"으로 잘못 표기됨 | 코드 작성 시 반드시 `SM_출고계근` 사용 — 본 가이드 및 ERP DlivyWeighEntity.java 기준 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|:----:|------|:----:|
| 1 | search_shipment.jsp NOT EXISTS 추가 (정량 이마트) | ⏳ 대기 |
| 2 | search_production_nonfixed.jsp NOT EXISTS 추가 (비정량 이마트) | ⏳ 대기 |
| 3 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/문의사항/04_출하대상받기_4유형_조회조건_종합정리.txt` — 1.6/2.5 명세 (단, SM_출하계근은 오기, 실제는 SM_출고계근)
- `app/doc/소스분석/49_출하대상받기_4유형_조회조건_종합정리.md` — 4유형 조회 조건 종합 정리
- ERP NOT EXISTS 패턴 선례 (SM_출고계근 직접 사용 아님, 패턴 사용 사례): `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\resources\mapper\ac\evrench\A0808_SQL.xml`
- ERP NOT EXISTS 패턴 선례 (동일): `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\resources\mapper\co\bizbasic\C0109_SQL.xml`
- ERP Entity: `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\java\com\sgis\domain\sm\release\entity\DlivyWeighEntity.java`
- 계근 INSERT JSP: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet.jsp`
- 비정량 계근 INSERT JSP: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_new.jsp`

---

**문서 버전**: 1.0
