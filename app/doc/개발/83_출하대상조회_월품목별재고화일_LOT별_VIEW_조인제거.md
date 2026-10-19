# 출하대상조회 월품목별재고화일_LOT별_VIEW 조인 제거

**작성일**: 2026-10-19
**목적**: 출하대상 조회 JSP 6개에서 `월품목별재고화일_LOT별_VIEW`(V) LEFT JOIN을 제거하고, BL_NO·IMPORT_ID_NO·PACKWEIGHT를 SM_출고LOT(L)·CO_품목코드(I) 값만으로 만든다 (사용자 지시: VIEW를 굳이 쓸 필요가 없음)

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### 출하대상 조회 JSP 6개 (이마트·홈플러스·롯데·도매·비정량·홈플러스비정량)

```java
+ ", COALESCE(NULLIF(V.BLNO, ''), NULLIF(L.이력번호, ''), V.이력번호) AS BL_NO"
+ ", COALESCE(NULLIF(L.이력번호, ''), V.이력번호) AS IMPORT_ID_NO"
+ ", COALESCE(NULLIF(L.평균중량,0), NULLIF(V.평균중량,0), I.박스중량) AS PACKWEIGHT"   // 이마트·홈플러스·비정량만
...
+ " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"
+ "   ON V.회사코드 = D.회사코드"
+ "  AND V.사업장 = D.출고사업장"
+ "  AND V.창고코드 = D.창고코드"
+ "  AND V.품목코드 = D.출고품목코드"
+ "  AND V.LOTNO = L.LOTNO"
+ "  AND V.년월 = LEFT(D.출고일자, 6)"
```

- V는 LEFT JOIN이라 행을 거르지 않고, L 값이 비었을 때의 보조 값(BLNO·이력번호·평균중량)으로만 쓰인다.
- 롯데·도매의 PACKWEIGHT는 각각 `NULL`, `'NA'` 고정이라 V를 쓰지 않는다.

### 문제점

- 월별 재고 VIEW를 출하대상 조회마다 조인한다 (`V.년월` 조건이 없으면 월 수만큼 행이 중복되는 구조).
- 사용자 판단: 출하대상 조회에 VIEW를 쓸 필요가 없음.

---

## 2. 변경 구조

### 데이터 흐름

```
SM_출고머리(H) + SM_출고상세(D) + SM_출고LOT(L) + CO_품목코드(I) + 매핑/거래처
    ↓ (V 조인 없음)
BL_NO        = L.이력번호
IMPORT_ID_NO = L.이력번호
PACKWEIGHT   = COALESCE(NULLIF(L.평균중량,0), I.박스중량)   // 해당 JSP만
    ↓
PDA (ProgressDlgShipSearch, Index 변경 없음)
```

- 원본 Oracle VIEW(VW_PDA_WID_LIST·HOMEPLUS·NONFIXED·WHOLESALE)의 BL_NO는 `DECODE(IMPORT_ID_NO, NULL, BL_NO, IMPORT_ID_NO)`로 **이력번호 우선**이다. 롯데(VW_PDA_WID_LIST_LOTTE)만 BL 우선이다.

---

## 3. 수정 대상 파일

경로: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\`

| # | 파일 | searchType | 수정 내용 |
|:-:|------|:-:|----------|
| 1 | **search_shipment.jsp** | 0 이마트 | BL_NO·IMPORT_ID_NO·PACKWEIGHT에서 V 제거, V 조인 제거 |
| 2 | **search_shipment_homeplus.jsp** | 2 홈플러스 | 위와 같음 |
| 3 | **search_shipment_wholesale.jsp** | 3 도매 | BL_NO·IMPORT_ID_NO에서 V 제거, V 조인 제거 |
| 4 | **search_production_nonfixed.jsp** | 4 비정량 | BL_NO·IMPORT_ID_NO·PACKWEIGHT에서 V 제거, V 조인 제거 |
| 5 | **search_homeplus_nonfixed.jsp** | 5 홈플러스비정량 | BL_NO·IMPORT_ID_NO에서 V 제거, V 조인 제거 (PACKWEIGHT는 V 미사용) |
| 6 | **search_shipment_lotte.jsp** | 6 롯데 | BL_NO·IMPORT_ID_NO에서 V 제거, V 조인 제거 |

- 제외: `search_production.jsp` (생산 searchType=1, 미사용 결정)

---

## 4. 수정 상세

### 4.1 공통 (6개 JSP)

**변경 전:**

```java
+ ", COALESCE(NULLIF(V.BLNO, ''), NULLIF(L.이력번호, ''), V.이력번호) AS BL_NO"
+ ", COALESCE(NULLIF(L.이력번호, ''), V.이력번호) AS IMPORT_ID_NO"
+ ", COALESCE(NULLIF(L.평균중량,0), NULLIF(V.평균중량,0), I.박스중량) AS PACKWEIGHT"
+ " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"
+ "   ON ... (6줄)"
```

**변경 후:**

```java
+ ", L.이력번호 AS BL_NO"
+ ", L.이력번호 AS IMPORT_ID_NO"
+ ", COALESCE(NULLIF(L.평균중량,0), I.박스중량) AS PACKWEIGHT"
// V 조인 7줄 삭제
```

**검증**: 변경 전·후 같은 조건(`D.출고일자 >= '20260101'`)으로 6개 JSP를 호출해 행 수와 Index별 값을 비교한다.

---

## 5. 사이드이펙트

### BL_NO 값 변경 (사전 조회 결과, 2026-10-19)

| JSP | 행 수 | BL_NO ≠ IMPORT_ID_NO (V.BLNO 사용) | L.이력번호 빈값 | V.평균중량 대체 |
|---|:-:|---|:-:|:-:|
| search_shipment | 17 | 0 | 0 | 0 |
| search_shipment_homeplus | 4 | 0 | 0 | 0 |
| search_shipment_lotte | 8 | 0 | 0 | 0 |
| search_shipment_wholesale | 9 | 1 (408: 202609290001 → 202609290000) | 0 | 0 |
| search_homeplus_nonfixed | 1 | 1 (499: HLCUTOR260101958 → 905002407409) | 0 | 0 |
| search_production_nonfixed | 1 | 1 (508: HLCUTOR260101958 → 905002407409) | 0 | 0 |

- BL_NO는 앱에서 목록 끝 4자리 표시·BL 스피너·같은 BL 행 찾기·빈 값 경고에만 쓰인다. 라벨·전송에는 쓰지 않는다 (`ShipmentListAdapter.java:149`, `BixolonShipmentActivity.java:710`, `:1221`, `ScanProcessor.java:160`).
- 비롯데 5종: 원본 VIEW와 같은 이력번호 기준이 된다.
- 롯데: 원본은 BL 우선이지만, 현재 데이터에서 롯데 8건은 모두 BL_NO = 이력번호라 결과 차이가 없다.
- L.이력번호·L.평균중량이 비는 출고건이 생기면 보조 값이 없어진다 (현재 0건).

---

## 6. 데이터 저장 구조

### 변수 매핑

| 출력 컬럼 | Index | 변경 전 | 변경 후 |
|------|:-:|------|------|
| BL_NO | 8 | V.BLNO → L.이력번호 → V.이력번호 | L.이력번호 |
| IMPORT_ID_NO | 15 | L.이력번호 → V.이력번호 | L.이력번호 |
| PACKWEIGHT | 20 | L.평균중량 → V.평균중량 → I.박스중량 | L.평균중량 → I.박스중량 |

### 인덱스 매핑

- out.println 순서와 Index는 바뀌지 않는다. `ProgressDlgShipSearch.java` 수정 없음.

---

## 7. 호출 시점

```
[메인 화면 출하대상받기]
    ↓
[ProgressDlgShipSearch → search_*.jsp]
    ├── ★ SELECT (V 조인 제거)
    ↓
[TB_SHIPMENT INSERT] → 계근 화면
```

---

## 8. 개발 플랜

### Step 1: JSP 6개 V 조인 제거

**Part 1. 분석**
- 범위: 3장 표의 JSP 6개 SELECT·FROM 절
- 용도: V 사용 제거
- 주의할 점: WHERE 조건·다른 조인·출력 순서는 손대지 않는다

**Part 2. 변환 계획**
- 변환 방식: 4.1의 변경 전 → 변경 후 치환
- 주의사항: 롯데·도매 PACKWEIGHT(`NULL`, `'NA'`)는 그대로 둔다

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 (JSP 호출 오류 없음)
- [x] Part 5: 단위테스트 (변경 전·후 출력 비교)
- [ ] Part 6: 회귀테스트 (PDA 출하대상받기)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**: JSP 6개에서 `월품목별재고화일_LOT별_VIEW` LEFT JOIN 7줄 삭제, BL_NO·IMPORT_ID_NO = `L.이력번호`, PACKWEIGHT = `COALESCE(NULLIF(L.평균중량,0), I.박스중량)` (이마트·홈플러스·비정량)
- **왜**: 출하대상 조회에 월별 재고 VIEW가 필요 없음 (사용자 지시)
- **어떻게**: 변경 전·후 `data=" AND D.출고일자 >= '20260101'"` 호출 비교 (2026-10-19)

| JSP | 행 수 (전→후) | 값 차이 |
|---|:-:|---|
| search_shipment | 17 → 17 | 없음 |
| search_shipment_homeplus | 4 → 4 | 없음 |
| search_shipment_lotte | 8 → 8 | 없음 |
| search_shipment_wholesale | 9 → 9 | 408 BL_NO 202609290001 → 202609290000 |
| search_homeplus_nonfixed | 1 → 1 | 499 BL_NO HLCUTOR260101958 → 905002407409 |
| search_production_nonfixed | 1 → 1 | 508 BL_NO HLCUTOR260101958 → 905002407409 |

- 차이는 5장 사전 조회 결과와 같은 BL_NO(Index 8) 3건뿐이다.

---

### Step 2: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 6개 JSP 행 수 변경 전·후 동일 | ✅ |
| 2 | BL_NO 외 Index 값 변경 전·후 동일 | ✅ |
| 3 | PDA 출하대상받기 정상 (마트사별) | □ |

---

### 개발 순서 요약

```
Step 1: JSP 6개 V 조인 제거
    ↓
Step 2: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: JSP 출력 비교

```
1. 변경 전 6개 JSP를 data=" AND D.출고일자 >= '20260101'"로 호출해 저장
2. JSP 수정
3. 같은 조건으로 다시 호출해 행 수·Index별 값 비교 (BL_NO 3건 외 동일해야 함)
```

### 시나리오 2: PDA 출하대상받기

```
1. PDA에서 마트사별 출하대상받기
2. 목록 BL 끝 4자리 표시, 계근 화면 BL 스피너 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 이력번호 빈칸·BL 경고 팝업 | L.이력번호 미입력 출고건 | ERP LOT 출고 시 이력번호 입력 확인 |
| 2 | PACKWEIGHT가 품목 박스중량으로 표시 | L.평균중량 0 | ERP LOT 평균중량 입력 확인 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | JSP 6개 V 조인 제거 | ✅ 완료 |
| 2 | 통합 테스트 | 🔄 JSP 비교 완료, PDA 확인 대기 |

---

## 관련 문서

- `app/doc/view/VW_PDA_WID_LIST` 외 원본 VIEW DDL (BL_NO 정의)
- `app/doc/개발/78_출하대상조회_거래명세발행여부_N_조건추가.md`

---

**문서 버전**: 1.0
