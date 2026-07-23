# 홈플러스 STORE_IN_DATE datetime CONVERT 미적용 — 라벨 날짜 오인쇄

## 발견일
2026-07-23

## 에러 발생 시나리오

```
1. BixolonShipmentActivity에서 홈플러스(searchType=2) 출하대상 받기 실행
2. search_shipment_homeplus.jsp 호출 → SD.납기일자(MSSQL datetime 타입)를 STORE_IN_DATE로 SELECT
3. rs.getString("STORE_IN_DATE")가 "2026-05-04 00:00:00.0" 형식(datetime 전체 문자열)으로 반환됨
4. 계근 후 홈플러스 라벨 출력 시 LabelPrintHelper.setHomeplusPrinting()에서
   si.getSTORE_IN_DATE().substring(0,4)/(4,6)/(6,8) 호출 → YYYYMMDD 8자리 형식을 전제로 한 파싱이
   "2026-05-04 00:00:00.0" 문자열에 대해 잘못된 위치를 잘라내어 라벨에 오인쇄되거나 예외 발생 가능
```

---

## 현상
- 홈플러스 라벨의 "납품일자" 항목에 잘못된 날짜(혹은 깨진 문자열)가 인쇄될 수 있음
- 라벨 인쇄 자체가 substring 범위 오류로 예외를 유발할 가능성도 존재
- 우선순위: 높음

## 원래부터 있던 버그인가?

**NO — 이마트(searchType=0) 전환 시 이미 수정·검증된 패턴이 홈플러스 JSP에는 아직 반영되지 않은 미포팅 사례**

- 이마트 오류패턴_분석.md 패턴 A "22번: STORE_IN_DATE datetime 형식 변환 누락"에서 동일 원인이 이미 식별되어 있고, 공통 체크 11번("JSP에서 datetime 타입 컬럼을 CONVERT(VARCHAR(8), 컬럼, 112) 없이 SELECT하고 있지 않은지")으로 사전 체크 항목화까지 완료됨.
- 그러나 이마트 `search_shipment.jsp`는 애초에 STORE_IN_DATE를 `H.출고일자`(SM_출고머리 컬럼, VARCHAR 계열로 추정)에서 직접 취득하여 이 문제 자체가 발생하지 않는 구조다 (59줄: `H.출고일자 AS STORE_IN_DATE`).
- 반면 홈플러스는 `SD.납기일자`(SM_수주상세, datetime 타입)를 CONVERT 없이 그대로 SELECT하고 있어 동일 패턴 A(DB 타입 불일치)가 그대로 재현될 위험이 있다.
- `app/doc/오류/22_잠재오류_18건_목록.md`(2026-05-04)의 "오류 1: STORE_IN_DATE datetime 형식 변환 누락"에서 이미 `search_shipment_homeplus.jsp:59`가 위험 위치로 명시적으로 목록화되었으나 현재까지 미수정 상태다. 본 문서는 해당 항목의 홈플러스 개별 상세 문서다.

```sql
-- search_shipment.jsp (이마트, 59줄): 이 문제가 발생하지 않는 구조
+ ", H.출고일자 AS STORE_IN_DATE"

-- search_shipment_homeplus.jsp (홈플러스, 59줄): datetime CONVERT 미적용
+ ", SD.납기일자 AS STORE_IN_DATE"
```

## 원인

### 문제 1 (주요): datetime 컬럼을 CONVERT 없이 SELECT

#### 코드 위치
- `search_shipment_homeplus.jsp` : 59줄

#### 현재 문제 코드
```sql
-- search_shipment_homeplus.jsp (59줄, 실제 코드 확인)
+ ", SD.납기일자 AS STORE_IN_DATE"
// ★ SD.납기일자가 MSSQL datetime 타입이면 rs.getString()이
//   "2026-05-04 00:00:00.0" 형식(전체 타임스탬프 문자열)을 그대로 반환
//   CONVERT(VARCHAR(8), ..., 112) 미적용
```

#### 발생 시나리오
MSSQL의 datetime 컬럼을 JDBC `rs.getString()`으로 조회하면 드라이버가 `"YYYY-MM-DD HH:mm:ss.S"` 형식 문자열을 반환한다.
이는 원본 Oracle 환경의 `YYYYMMDD` 8자리 문자열 반환 방식과 다르다. LabelPrintHelper.java가 STORE_IN_DATE를 8자리 고정
포맷으로 전제하고 substring을 호출하므로, datetime 문자열이 그대로 넘어오면 연/월/일 파싱 위치가 어긋난다.

### 문제 2 (연쇄): LabelPrintHelper 라벨 인쇄 시 substring 파싱 오류

#### 코드 위치
- `LabelPrintHelper.java` : 1177줄 (setHomeplusPrinting 함수 내)

#### 문제 코드
```java
// LabelPrintHelper.java:1177 (실제 코드 확인)
String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 "
        + si.getSTORE_IN_DATE().substring(4,6) + "월 "
        + si.getSTORE_IN_DATE().substring(6,8) + "일";
// ★ STORE_IN_DATE가 "2026-05-04 00:00:00.0"이면
//   substring(0,4)="2026", substring(4,6)="-0", substring(6,8)="5-"
//   → "2026년 -0월 5-일" 형태로 잘못된 날짜가 라벨에 인쇄됨
```

## 상세 흐름

1. **홈플러스 출하대상 받기** (앱 → search_shipment_homeplus.jsp)
   - SD.납기일자(datetime) → STORE_IN_DATE로 SELECT, CONVERT 없음

2. **JDBC 조회** (rs.getString("STORE_IN_DATE"))
   - datetime → `"2026-05-04 00:00:00.0"` 형식 문자열 반환

3. **ProgressDlgShipSearch 파싱** (temp[22] 그대로 저장)
   - si.setSTORE_IN_DATE(temp[22]) — 원본 문자열 그대로 저장, 별도 가공 없음

4. **계근 완료 후 홈플러스 라벨 인쇄** (BixolonShipmentActivity → labelPrintHelper.setHomeplusPrinting())
   - **문제 발생 경로**: `si.getSTORE_IN_DATE().substring(0,4)+"년 "+...substring(4,6)+"월 "+...substring(6,8)+"일"` 호출
   - 8자리(YYYYMMDD) 전제 파싱이 datetime 전체 문자열에 대해 잘못된 구간을 잘라내어 라벨에 오인쇄

## 영향 범위
- `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp` : 59줄 — 조회 쿼리 (CONVERT 미적용 위치)
- `search_homeplus_nonfixed.jsp` (60줄 추정, 22번 목록 기재) — 동일 패턴 (홈플러스 비정량, searchType=5)
- `search_shipment_lotte.jsp` (60줄 추정, 22번 목록 기재) — 동일 패턴 (롯데, searchType=6)
- `app/src/main/java/.../print/LabelPrintHelper.java` : 1177줄 — setHomeplusPrinting() 라벨 인쇄 (searchType 2, 5 공통 사용, BixolonShipmentActivity.java:936, 1701 / ShipmentActivity.java:860, 1505에서 호출)
- searchType=2(홈플러스), searchType=5(홈플러스비정량) 라벨 인쇄 전체

## 수정 방안

### 수정 1: JSP에서 datetime → VARCHAR(8) CONVERT 적용

```sql
-- search_shipment_homeplus.jsp 수정안 (59줄)
-- 변경 전
+ ", SD.납기일자 AS STORE_IN_DATE"

-- 변경 후
+ ", CONVERT(VARCHAR(8), SD.납기일자, 112) AS STORE_IN_DATE"
```

> 이마트 오류패턴_분석.md 공통 체크 11번과 동일한 수정 패턴. SD.납기일자가 NULL인 경우 CONVERT 결과도 NULL이 되므로,
> LabelPrintHelper.java의 substring NULL 방어 여부(오류패턴_분석.md 패턴 F 관련, 22_잠재오류_18건_목록.md 오류 7 "STORE_IN_DATE substring NULL 방어 없음")는 별도 확인 필요.

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/오류/22_잠재오류_18건_목록.md` — 오류 1(STORE_IN_DATE datetime 형식 변환 누락)에 이미 목록화(미수정), 오류 7(STORE_IN_DATE substring NULL 방어 없음, 원본 동일)과 연쇄 확인 필요
- `app/doc/참고자료/오류패턴_분석.md` — **패턴 A: DB 타입 불일치** (22번: STORE_IN_DATE datetime 형식 변환 누락, 공통 체크 11번, searchType=2 추가 체크 5번)
- 검증 출처: 홈플러스 AI 정적검증(code-verifier), 원본 비교(original-comparator)는 비허용 차이 없음으로 통과
- JSP: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp`
