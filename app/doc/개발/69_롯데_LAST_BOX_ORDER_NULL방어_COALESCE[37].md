# 롯데 `LAST_BOX_ORDER` NULL 방어 — `COALESCE` 적용

**작성일**: 2026-09-28
**대응 오류**: `app/doc/오류/37_롯데_LAST_BOX_ORDER_NULL_parseInt예외_박스순번_0적재[롯데_EDA51_실기기테스트].md`
**목적**: 롯데(searchType=6) 첫 계근 시 `search_shipment_lotte.jsp`의 `LAST_BOX_ORDER` 서브쿼리가 `NULL`을 반환하여 앱 `Integer.parseInt`가 예외를 던지고 박스순번이 영원히 `0`으로 적재되는 문제를, JSP 서브쿼리에 `COALESCE(…, 0)`을 적용해 원본 Oracle 뷰(`VW_PDA_WID_LIST_LOTTE`)가 `0`을 반환하던 동작으로 복원한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

### 추가 제약 조건 (이 가이드 한정)

- 수정 범위는 **`search_shipment_lotte.jsp`의 `LAST_BOX_ORDER` 서브쿼리 1곳**에 한정한다. 다른 SELECT 컬럼·JOIN·WHERE절은 건드리지 않는다.
- **앱 코드(`BixolonShipmentActivity.java` 등)는 수정하지 않는다.** 앱 코드는 원본과 글자 단위로 동일하며(오류37 §3), `Integer.parseInt(si.LAST_BOX_ORDER)`가 항상 숫자 문자열을 받는다는 전제(원본 뷰가 `0`을 반환)를 그대로 유지한다.
- 서브쿼리의 `WHERE`(`W.출고상세SEQ = D.SEQ`, `W.박스순번 IS NOT NULL`)·`ORDER BY W.SEQ DESC`는 변경하지 않는다. `COALESCE`로 **감싸기만** 한다.

---

## 1. 현재 구조

### `search_shipment_lotte.jsp:63~67` — `LAST_BOX_ORDER` 서브쿼리

```java
								+ ", (SELECT TOP 1 W.박스순번"
								+ "    FROM SM_출고계근 W"
								+ "    WHERE W.출고상세SEQ = D.SEQ"
								+ "      AND W.박스순번 IS NOT NULL"
								+ "    ORDER BY W.SEQ DESC) AS LAST_BOX_ORDER"
```

이 값은 `out.println` 25번째 필드(0-base index 25)로 전송된다(`:145`).

```java
					+ rs.getString("LAST_BOX_ORDER") + ";;");  // 25
```

앱은 `ProgressDlgShipSearch.java:305`에서 그대로 저장한다.

```java
                        si.setLAST_BOX_ORDER(temp[25].toString());   // 마지막 박스순번 (1~9999 순환)
```

`BixolonShipmentActivity.java:2603~2606`(롯데 채번 블록)에서 소비한다.

```java
                if(Common.searchType.equals(Common.SEARCH_TYPE_LOTTE)) {

                    Shipments_Info si = arSM.get(0);
                    lotte_TryCount = Integer.parseInt(si.LAST_BOX_ORDER) + 1;
```

이 전체가 `doInBackground`의 단일 `try` 안에 있고, `catch(Exception e)`(`:2620~2624`)가 예외를 로그만 남기고 삼킨다.

### 문제점

- 롯데 첫 계근(해당 `출고상세SEQ`에 `W.박스순번 IS NOT NULL`인 계근 이력이 없음) 시 서브쿼리 결과가 `NULL`이다.
- JSP `rs.getString("LAST_BOX_ORDER")`가 `null`(Java null)을 반환하고, `out.println` 시 문자열 `"null"`로 직렬화된다.
- 앱 `Common.nullCheck(...)`(`DBHandler.java:183`)가 로컬DB 저장 시 `""`로 치환하여, 최종적으로 `si.LAST_BOX_ORDER = ""`가 된다.
- `Integer.parseInt("")` → `NumberFormatException`.
- 채번 블록 전체가 `catch`에 삼켜져 `lotte_TryCount`가 필드 초기값 `0`에 머물고, 이후 `insertqueryGoodsWetLotte(this, gi, lotte_TryCount)`에 `0`이 전달되어 `TB_GOODS_WET.BOX_ORDER = 0`으로 적재된다.
- 원본 Oracle 뷰 `VW_PDA_WID_LIST_LOTTE`는 계근 이력이 없을 때 `LAST_BOX_ORDER = 0`을 반환했으므로(오류37 §3), 앱은 항상 숫자 문자열을 받는 것을 전제로 `parseInt`만 하도록 작성되어 있다. MSSQL 전환 시 서브쿼리로 풀어쓰면서 이 NULL 방어가 누락되었다.

---

## 2. 변경 구조

### 데이터 흐름

```
[변경 전]
계근 이력 없음 → 서브쿼리 NULL → rs.getString = null → out.println "null"
    ↓
temp[25] = "null" → nullCheck(..., "") → si.LAST_BOX_ORDER = ""
    ↓
Integer.parseInt("")  → NumberFormatException
    ↓
catch(Exception) 로 채번 블록 스킵 → lotte_TryCount = 0 (필드 초기값 유지)
    ↓
insertqueryGoodsWetLotte(..., 0) → TB_GOODS_WET.BOX_ORDER = 0  (영구)

[변경 후]
계근 이력 없음 → 서브쿼리 NULL → COALESCE(…, 0) = 0 → rs.getString = "0"
    ↓
temp[25] = "0" → si.LAST_BOX_ORDER = "0"
    ↓
Integer.parseInt("0") + 1 = 1
    ↓
lotte_TryCount = 1 (이후 arSM 순회로 PACKING_QTY 누적)
    ↓
insertqueryGoodsWetLotte(..., 1부터) → TB_GOODS_WET.BOX_ORDER = 1, 2, 3 … (정상 채번)
```

계근 이력이 **있는 경우**는 서브쿼리가 실제 값을 반환하므로 `COALESCE`가 개입하지 않고 기존과 동일하게 동작한다.

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_shipment_lotte.jsp** | `Tomcat/webapps/ROOT/inno/` | `LAST_BOX_ORDER` 서브쿼리 전체를 `COALESCE((…), 0)`으로 감싼다 |

앱 소스 수정 없음.

---

## 4. 수정 상세

### 4.1 search_shipment_lotte.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_lotte.jsp`

**변경 전 (`:63~67`):**

```java
								+ ", (SELECT TOP 1 W.박스순번"
								+ "    FROM SM_출고계근 W"
								+ "    WHERE W.출고상세SEQ = D.SEQ"
								+ "      AND W.박스순번 IS NOT NULL"
								+ "    ORDER BY W.SEQ DESC) AS LAST_BOX_ORDER"
```

**변경 후:**

```java
								+ ", COALESCE((SELECT TOP 1 W.박스순번"
								+ "            FROM SM_출고계근 W"
								+ "            WHERE W.출고상세SEQ = D.SEQ"
								+ "              AND W.박스순번 IS NOT NULL"
								+ "            ORDER BY W.SEQ DESC), 0) AS LAST_BOX_ORDER"
```

**변경 내용**: 서브쿼리 자체(WHERE, ORDER BY, TOP 1)는 그대로 두고, `SELECT TOP 1 … DESC)` 결과 전체를 `COALESCE((…), 0)`으로 감싼다. 서브쿼리가 `NULL`을 반환할 때만 `0`으로 대체되고, 값이 있을 때는 그 값이 그대로 반환된다.

**검증**: 앱과 동일한 방식(POST, `data`+`dbid`, UTF-8 폼 인코딩)으로 재현 호출하여
1) 계근 이력이 없는 `출고상세SEQ`에 대해 `LAST_BOX_ORDER` 필드(응답 25번째, `;;` 앞)가 `"0"`으로 오는지,
2) 계근 이력이 있는 건은 기존과 동일한 값(직전 박스순번)이 오는지 확인한다.

---

## 5. 사이드이펙트

### 5.1 서브쿼리 외 영향

**없음.** `COALESCE`로 감싸는 위치는 `AS LAST_BOX_ORDER` 별칭이 붙은 서브쿼리 결과값뿐이며, 다른 SELECT 컬럼(`GI_D_ID`~`WH_AREA`)·JOIN·WHERE절·`ORDER BY LE.SEQ ASC`는 그대로다. 출력 컬럼 수(26개, index 0~25)도 변경 없다.

### 5.2 계근 이력이 있는 경우

기존 계근 이력(`W.박스순번 IS NOT NULL`)이 있는 `출고상세SEQ`는 서브쿼리가 `NULL`을 반환하지 않으므로 `COALESCE`가 개입하지 않고 **기존과 동일한 값**을 그대로 반환한다. 즉 본 수정은 "계근 이력이 없는 경우"에만 동작이 바뀐다.

### 5.3 다른 마트사(searchType)

**없음.** `search_shipment_lotte.jsp`는 롯데(마트사구분 `'6'`, `H.마트사구분 = '6'`) 전용 JSP이며, 이마트(0)·홈플러스(2)·도매(3)·비정량(4)·홈플러스비정량(5) 조회는 각각 별도 JSP(`search_shipment.jsp` 등)를 사용하므로 영향받지 않는다.

### 5.4 앱 코드

**없음.** `BixolonShipmentActivity.java:2606`의 `Integer.parseInt(si.LAST_BOX_ORDER)`는 수정하지 않는다. JSP가 항상 숫자 문자열(`"0"` 포함)을 반환하도록 복원하는 것으로 전제 조건을 맞춘다.

### 5.5 개발63과의 관계

`app/doc/개발/63_롯데_박스순번_파이프라인_복구[36].md`에서 복구한 롯데 박스순번 파이프라인(채번 → 로컬 저장 → 전송 `insert_goods_wet_lotte.jsp` → 재조회 `LAST_BOX_ORDER`)은 이 건(오류37)이 막고 있던 마지막 구간이다. 본 수정으로 Step 4(통합 테스트)의 "박스순번 1부터 채번" 전제가 성립한다.

---

## 6. 개발 플랜

### Step 1: search_shipment_lotte.jsp `LAST_BOX_ORDER` `COALESCE` 적용

**Part 1. 분석**
- 메서드/범위: `search_shipment_lotte.jsp:63~67`, `LAST_BOX_ORDER` 서브쿼리 1곳
- 용도: 계근 이력이 없는 첫 계근 시 서브쿼리 `NULL` → `rs.getString` `"null"` 문자열 → 앱 `Integer.parseInt` 예외 → 박스순번 0 적재를 방지
- 주의할 점: 서브쿼리 내부 WHERE(`W.출고상세SEQ = D.SEQ`, `W.박스순번 IS NOT NULL`)·`ORDER BY W.SEQ DESC`·`TOP 1`은 절대 변경하지 않는다. `COALESCE`로 **바깥만** 감싼다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 서브쿼리 전체 | `:63~67` | `COALESCE((…), 0) AS LAST_BOX_ORDER`로 치환 |
| 2 | out.println 순서 | `:145` | 변경 없음 (index 25 그대로) |
| 3 | 앱 파싱 | `ProgressDlgShipSearch.java:305` | 변경 없음 |
| 4 | 앱 소비 | `BixolonShipmentActivity.java:2606` | 변경 없음 |

**Part 2. 변환 계획**
- 변환 방식: 원본 Oracle 뷰(`VW_PDA_WID_LIST_LOTTE`)가 계근 이력이 없을 때 `0`을 반환하던 동작을 `COALESCE(…, 0)`으로 복원. 다른 전환 JSP의 `COALESCE(NULLIF(V.BLNO, ''), V.이력번호)`(`:46`) 패턴과 동일 계열
- 주의사항: 문자열 연결(`+`) 형태의 Java 소스이므로 괄호 짝을 정확히 맞춘다(`COALESCE((SELECT … DESC), 0)`). 들여쓰기·주석 스타일은 기존 파일 컨벤션을 따른다

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 쿼리 실행 확인 (HTTP 200, `LAST_BOX_ORDER` 필드가 계근 이력 없을 때 `"0"` 반환) — 20260805 롯데 5건(268~272) 모두 `'0'`, 컬럼 수 26 불변
- [x] Part 5: 단위테스트 — 앱 재조회 시 logcat `LAST_BOX_ORDER : 0` → `lotte_TryCount 1`
- [x] Part 6: 회귀테스트 (계근 이력이 있는 건의 `LAST_BOX_ORDER` 값 불변 확인) — 30건 전송 후 재조회 6/12/18·6/12 반환 (COALESCE 미개입)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**: `search_shipment_lotte.jsp` 63~67행 `LAST_BOX_ORDER` 서브쿼리
- **왜**: 계근 이력 없을 때 NULL → 앱 `Integer.parseInt` 예외 → 박스순번 0 적재(오류37)
- **어떻게**: 서브쿼리 전체를 `COALESCE((SELECT TOP 1 … ORDER BY W.SEQ DESC), 0)`으로 감쌈 (내부 WHERE/ORDER BY/TOP 1 불변)

---

### Step 2: 통합 테스트 (실기기 EDA51)

**Part 1. 분석**
- 대상: 롯데(searchType=6) 계근 전 구간 — 대상받기 → 계근 → 로컬 저장 → 전송 → 재조회
- 범위: `TB_GOODS_WET.BOX_ORDER`, `SM_출고계근.박스순번`, 재조회 `LAST_BOX_ORDER`
- 용도: 박스순번이 `0`이 아닌 `1`부터 채번되는지, 이어지는 계근에서 연속 채번되는지 확인
- 주의할 점: 테스트 대상 `출고상세SEQ`에 기존 계근 이력이 없어야 "첫 계근" 시나리오가 재현된다(오류37 §6 재현 절차와 동일 전제)

**Part 2. 변환 계획**
- 변환 방식: 해당 없음(코드 변경 없는 실기기 검증 단계)
- 주의사항: `PDA테스트문서.xlsx` `5.계근-롯데` 시트 `LT-19`(첫 계근 박스순번 1 채번)·`LT-22`(연속 채번)·`LT-30`(라벨 박스순번 인쇄 반영) TC와 연계하여 결과를 기록한다

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 실기기 테스트 수행 (EDA51) — 2026-09-28, 김해저온센타 18박스
- [x] Part 4: logcat에 `NumberFormatException` 미발생 확인 (조치 전 `For input string: ""` 재현 → 조치 후 미발생)
- [x] Part 5: 로컬DB `TB_GOODS_WET.BOX_ORDER = 1` 확인 (첫 계근) — 이후 1~18 연속, 라벨 바코드 끝 4자리 0001~
- [ ] Part 6: 회귀테스트 (이마트·홈플러스·도매·비정량·홈플러스비정량 정상 동작 불변) — 미실시 (타 마트는 별도 JSP 로 영향 없음)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**: 코드 변경 없음 (실기기 검증)
- **왜**: Step 1 조치 효과 확인
- **어떻게**: PDA테스트문서.xlsx `5.계근-롯데` LT-19 PASS. LT-30 전송은 별건(GI_L_ID 누락, insert_goods_wet_lotte.jsp HTTP 500) 조치(search_shipment_lotte.jsp GI_L_ID 조회 + ProgressDlgShipSearch 파싱) 후 30건 전송, 서버 박스순번 1~18·1~12 적재 확인

---

### 개발 순서 요약

```
Step 1: search_shipment_lotte.jsp LAST_BOX_ORDER COALESCE 적용
    ↓
Step 2: 통합 테스트 (실기기 EDA51)
```

---

## 7. 테스트 시나리오

### 시나리오 1: 첫 계근 — 박스순번 1부터 채번

```
1. PDA 로그인 (부산센터 / 12345678)
2. 날짜설정 → 계근 이력이 없는 대상 날짜 선택
3. [롯데 출하대상받기] → 대상 수신
4. [(롯데출하) 계근입력시작]
5. 바코드 스캔 1차 : 상품 식별
6. 바코드 스캔 2차 : 중량 추출 + 계근
7. logcat 확인 → NumberFormatException 미발생
8. 로컬DB 확인 → SELECT BOX_ORDER FROM TB_GOODS_WET → 1
```

### 시나리오 2: 연속 계근 — 박스순번 +1 이어짐

```
1. 시나리오 1 진행 후 동일 출고상세SEQ(또는 동일 PPCODE)로 재계근
2. 재조회 시 LAST_BOX_ORDER가 직전 값(1)을 반환하는지 확인
3. 로컬DB BOX_ORDER = 2 확인
```

### 시나리오 3: 기존 계근 이력이 있는 건 — 값 불변 회귀

```
1. 이미 박스순번이 적재된 출고상세SEQ로 조회
2. LAST_BOX_ORDER가 COALESCE 적용 전과 동일한 값(직전 박스순번)을 반환하는지 확인
3. COALESCE 개입 없이 서브쿼리 원 값이 그대로 나오는 것을 확인
```

---

## 8. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|:-:|--------|------|----------|
| 1 | `COALESCE` 적용 후에도 예외 재현 | 문자열 연결 괄호 오류로 SQL 구문 오류 발생 | 재현 호출(curl 등)로 HTTP 200·쿼리 로그(`##search_shipment_lotte query :`) 확인 후 배포 |
| 2 | 계근 이력이 있는 건의 값이 바뀜 | `COALESCE` 위치가 서브쿼리 내부까지 침범 | 서브쿼리 전체를 그대로 두고 바깥만 감싸는지 재검토 |
| 3 | 실기기 테스트 시 계근 이력이 이미 존재해 "첫 계근" 재현 불가 | 이전 테스트 데이터 잔존 | 신규 `출고상세SEQ`(다른 날짜/대상) 사용 또는 `SM_출고계근`에서 해당 건 삭제 후 재현 |
| 4 | 박스순번이 1이 아닌 다른 값으로 시작 | `arSM.get(i).getPACKING_QTY()` 누적 로직(`:2611~2613`)이 정상 동작 중이라 발생하는 원본 사양 | 원본 로직이므로 정상. `lotte_TryCount`는 `LAST_BOX_ORDER+1`에 이미 찍힌 수량을 더하는 것이 원본 사양 |

---

## 9. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | `search_shipment_lotte.jsp` `LAST_BOX_ORDER` `COALESCE` 적용 | ✅ 완료 |
| 2 | 통합 테스트 (실기기 EDA51) | ✅ 완료 (서버 박스순번 적재 확인, 타 마트 회귀 Part 6 미실시) |

---

## 10. 관련 문서

| 폴더 | 문서 | 관계 |
|------|------|------|
| 오류/ | `37_롯데_LAST_BOX_ORDER_NULL_parseInt예외_박스순번_0적재[롯데_EDA51_실기기테스트].md` | 본 작업의 원인 문서 |
| 개발/ | `63_롯데_박스순번_파이프라인_복구[36].md` | 선행 파이프라인 복구 작업 — 본 건이 그 Step 4(통합 테스트) 완결을 막고 있었음 |
| 오류/ | `36_롯데_search_shipment_lotte_박스순번_계근ID_미존재컬럼_출하대상조회_전면실패[전체JSP_오류검증].md` | 같은 JSP의 선행 오류 |
| 테스트/ | `PDA테스트문서.xlsx` — `5.계근-롯데` 시트 | `LT-19`(첫 계근 박스순번 1), `LT-22`(연속 채번), `LT-30`(라벨 반영) TC |
| 테스트/ | `증빙/2026-09-28_롯데_EDA51/` | 오류37 재현 증빙 |

---

**문서 버전**: 1.0
