# 홈플러스 SM_수주상세 LEFT JOIN 다중행 팽창 — 출하 건 중복

## 발견일
2026-07-23

## 에러 발생 시나리오

```
1. BixolonShipmentActivity에서 홈플러스(searchType=2) 출하대상 받기 실행
2. search_shipment_homeplus.jsp 호출
3. 동일 마트사SEQ(HE.SEQ)에 대해 SM_수주상세(SD) 행이 여러 건 존재하는 경우,
   LEFT JOIN SM_수주상세 SD ON SD.마트사SEQ = HE.SEQ 조건이 단순 1:1 매칭을 전제로 하여
   실제로는 Cartesian product가 발생
4. 출하대상 조회 결과가 SM_수주상세 매칭 건수만큼 배로 증가하여 반환됨
```

---

## 현상
- 동일 마트사SEQ에 SM_수주상세 N건이 존재하면 출하 건이 N배로 중복 조회됨
- 앱 출하대상 리스트에 동일 GI_D_ID 행이 여러 번 표시됨
- 계근/전송 처리 시 중복 행 기준으로 집계가 왜곡될 수 있음
- 우선순위: 높음

## 원래부터 있던 버그인가?

**NO — 이 오류는 홈플러스/롯데/홈플러스비정량 JSP 계열 공통으로 이미 목록화되었으나(app/doc/오류/22_잠재오류_18건_목록.md 오류2), 아직 미수정 상태이며 이마트 JSP에는 애초에 이 JOIN 구조 자체가 없다.**

이마트(searchType=0) `search_shipment.jsp`는 SM_수주상세를 아예 JOIN하지 않는다 (STORE_IN_DATE를 `H.출고일자`에서 직접 취득). 즉 이 JOIN은 홈플러스/롯데 계열 JSP에서만 사용되는 구조이며, 원본 Oracle JSP에도 없던 MSSQL 전환 시 신규 추가 구조다.
`app/doc/오류/22_잠재오류_18건_목록.md`(2026-05-04 작성)의 "오류 2: SM_수주상세 LEFT JOIN 다중 행 팽창"에서 이미 위험이 식별·목록화되었으나 현재까지 미수정 상태로 남아있다. 본 문서는 22번 목록 항목을 홈플러스 개별 오류로 상세 문서화한 것이다.

```sql
-- search_shipment.jsp (이마트): SM_수주상세 JOIN 자체 없음
+ ", H.출고일자 AS STORE_IN_DATE"   -- 59줄, SD 없이 H.출고일자 직접 사용

-- search_shipment_homeplus.jsp (홈플러스): SM_수주상세 LEFT JOIN 존재 (99~100줄)
+ " LEFT JOIN SM_수주상세 SD"
+ "   ON SD.마트사SEQ = HE.SEQ"
```

## 원인

### 문제 1 (주요): SM_수주상세 LEFT JOIN이 1:N 관계를 고려하지 않음

#### 코드 위치
- `search_shipment_homeplus.jsp` : 99~100줄

#### 현재 문제 코드
```sql
-- search_shipment_homeplus.jsp (99~100줄, 실제 코드 확인)
+ " LEFT JOIN SM_수주상세 SD"
+ "   ON SD.마트사SEQ = HE.SEQ"
// ★ HE.SEQ(마트사SEQ) 기준 SM_수주상세가 N건이면 Cartesian product 발생
//    → 출하 건이 N배로 중복 반환
```

#### 발생 시나리오
`SM_수주상세.마트사SEQ`가 유니크 키가 아니라면(동일 마트사SEQ에 대해 여러 납기일자/수주상세 행이 존재할 수 있는 구조라면),
단순 `LEFT JOIN ... ON SD.마트사SEQ = HE.SEQ`는 SD 매칭 건수만큼 D(출고상세) 행을 팽창시킨다.
결과적으로 SM_출고상세 1건 × SM_수주상세 N건 = N행이 반환되어, 출하대상 목록에 동일 GI_D_ID 행이 중복 표시된다.

## 상세 흐름

1. **홈플러스 출하대상 받기 요청** (앱 → search_shipment_homeplus.jsp)
   - SM_출고상세(D) → SM_마트사발주홈플러스(HE) JOIN → HE.SEQ(마트사SEQ) 확보

2. **SM_수주상세(SD) LEFT JOIN** (조건: SD.마트사SEQ = HE.SEQ)
   - HE.SEQ 1건에 대해 SD가 N건 매칭되는 경우 그대로 N행으로 팽창

3. **결과 반환** (중복 행 포함)
   - 출고상세 1건이 SD 매칭 수만큼 중복되어 반환됨
   - STORE_IN_DATE(SD.납기일자) 값이 행마다 다를 수 있음

4. **ProgressDlgShipSearch 파싱** (중복 행 그대로 파싱)
   - TB_SHIPMENT에 동일 GI_D_ID를 가진 중복 행 다수 INSERT

5. **출하대상/계근 집계 왜곡**
   - **문제 발생 경로**: 앱 화면에 동일 건이 여러 번 노출되거나, GI_D_ID 기준 집계 시 실제 건수보다 부풀려질 수 있음

## 영향 범위
- `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp` : 99~100줄 — 조회 쿼리 (JOIN 구조 위치)
- `search_homeplus_nonfixed.jsp` (105~106줄 추정, 22번 목록 기재) — 동일 패턴 (홈플러스 비정량, searchType=5)
- `search_shipment_lotte.jsp` (102~103줄 추정, 22번 목록 기재) — 동일 패턴 (롯데, searchType=6)
- `app/src/main/java/.../common/ProgressDlgShipSearch.java` — 출하대상 파싱 (중복 행 그대로 로컬 DB 저장)
- searchType=2/5/6 전체 출하대상 조회 흐름

## 수정 방안

### 수정 1: LEFT JOIN → TOP 1 서브쿼리 전환

```sql
-- search_shipment_homeplus.jsp 수정안 (99~100줄)
-- 변경 전
+ " LEFT JOIN SM_수주상세 SD"
+ "   ON SD.마트사SEQ = HE.SEQ"

-- 변경 후 (예시, ORDER BY 기준은 ERP 담당자와 협의 필요)
+ " OUTER APPLY (SELECT TOP 1 납기일자 FROM SM_수주상세"
+ "               WHERE 마트사SEQ = HE.SEQ ORDER BY SEQ DESC) SD"
```

또는 서브쿼리 컬럼 형태:
```sql
+ ", (SELECT TOP 1 납기일자 FROM SM_수주상세"
+ "    WHERE 마트사SEQ = HE.SEQ ORDER BY SEQ DESC) AS STORE_IN_DATE"
```

> 어떤 SM_수주상세 행을 대표로 선택할지(ORDER BY 기준)는 ERP 담당자 확인 후 결정 필요. 임의로 최신순(SEQ DESC)을 가정하지 말고 업무 요건 확인 필요.

## 상태
- [x] 수정 완료 (2026-07-23)
  - **채택안**: 문서 제안이던 OUTER APPLY(TOP 1) 전환 대신, `search_shipment_homeplus.jsp:99~100`의 `LEFT JOIN SM_수주상세 SD`를 **완전 제거**하여 1:N 팽창을 원천 차단.
  - STORE_IN_DATE는 `SD.납기일자` → `H.출고일자`로 대체(이마트 기준 일관, 오류 34와 동반 해소). 위 "ORDER BY 대표행 기준 ERP 확인 필요" 쟁점은 SD 자체를 제거하므로 소멸.
  - 근거·결정: 개발문서 58 개정이력(2026-07-23), STORE_IN_DATE 매핑 통일 결정. code-verifier 통과(컬럼 24개 불변).

## 관련 문서
- `app/doc/개발/58_홈플러스_search_shipment_homeplus_이마트패턴_포팅[32_33_34].md` — 개정이력에 실제 채택안(H.출고일자 + SD JOIN 제거) 명시.
- `app/doc/오류/34_홈플러스_STORE_IN_DATE_datetime_CONVERT_미적용_라벨_날짜_오인쇄[홈플러스_출하계근_AI정적검증].md` — 동일 수정으로 동반 해소.
- `app/doc/오류/22_잠재오류_18건_목록.md` — 오류 2(SM_수주상세 LEFT JOIN 다중 행 팽창)에 이미 목록화. 본 문서는 홈플러스 개별 상세 문서.
- `app/doc/참고자료/오류패턴_분석.md` — **패턴 C: 컬럼명/구조 변경** (공통 체크 14번: SM_수주상세 LEFT JOIN 1:N 관계 확인, searchType=2 추가 체크 6번)
- 검증 출처: 홈플러스 AI 정적검증(code-verifier), 원본 비교(original-comparator)는 비허용 차이 없음으로 통과 — 단, 이 JOIN 자체가 이마트에는 없는 구조이므로 원본 비교 대상에서 제외됨
- JSP: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp`
