# 홈플러스 GI_L_ID 미전송/미파싱 — GI_D_ID 비유니크 (다중 LOT 구분 불가)

## 발견일
2026-07-23

## 에러 발생 시나리오

```
1. BixolonShipmentActivity에서 홈플러스(searchType=2) 출하대상 받기 실행
2. search_shipment_homeplus.jsp가 GI_L_ID(출고LOTSEQ)를 SELECT/전송하지 않음 (인덱스 0~23만 전송)
3. ProgressDlgShipSearch.java의 searchType별 추가 필드 파싱 분기(263~286줄)에 searchType="2" 케이스가 없어
   si.GI_L_ID는 Shipments_Info 기본값 "" 그대로 유지됨
4. 동일 SM_출고상세(GI_D_ID)에 SM_출고LOT이 여러 건(다중 LOT) 연결된 경우,
   GI_D_ID + GI_L_ID(="") 조합으로 각 LOT 행을 구분해야 하는 로직(동기화 삭제/추가 판정,
   계근수량 집계 등)이 모두 동일한 키로 수렴되어 LOT별 구분이 불가능해짐
```

---

## 현상
- 홈플러스 출하대상의 GI_L_ID 필드가 항상 빈 문자열("")로 유지됨
- 동일 GI_D_ID에 다중 LOT이 존재하는 경우, 계근수량 집계(SAVE_CNT/PACKING_QTY)가 LOT별로 구분되지 않고
  동일하게 표시될 가능성 (이마트 15번과 동일 증상)
- 출하대상 동기화(삭제/추가 판정) 시 GI_D_ID+GI_L_ID 조합 키가 LOT 간에 구분되지 않아 오동작 가능
- 우선순위: 확인 필요 (홈플러스가 실제 1출고상세-다중LOT 운영 케이스를 갖는지 ERP 데이터 확인 필요)

## 원래부터 있던 버그인가?

**NO — 이마트(searchType=0)는 GI_D_ID 비유니크 문제(15/16/17번)를 GI_L_ID 추가로 이미 해결·검증했으나, 홈플러스 JSP/파싱 로직에는 이 패턴이 반영되지 않은 미포팅 사례**

- 이마트도 원래 GI_D_ID(=SM_출고상세.SEQ)만으로는 LOT별 유니크 식별이 불가능한 동일한 구조적 문제를 갖고 있었다 (`app/doc/오류/15_GI_D_ID_LOT별_미분리_계근수량_중복표시.md`, `16_GI_D_ID_비유니크_전체_영향범위.md`, `17_GI_L_ID_단독사용시_사이드이펙트.md`).
- `app/doc/개발/31_GI_L_ID_추가[GI_D_ID_비유니크_전체_영향범위].md` 개발 문서에 따라 SM_출고LOT.SEQ를 GI_L_ID(출고LOTSEQ)로 추가하여 이마트 전 영역(약 41곳)에 적용 완료됨.
- `search_shipment.jsp`(이마트) 67줄: `L.SEQ AS GI_L_ID` SELECT 추가, 167줄: `rs.getString("GI_L_ID")` 인덱스 30 전송 확인.
- `ProgressDlgShipSearch.java` 265~272줄: searchType "0"(이마트)/"4"(비정량)에서 `si.setGI_L_ID(temp[30].toString())` 파싱 확인.
- 그러나 `search_shipment_homeplus.jsp`는 GI_L_ID SELECT/전송 자체가 없고(0~23 인덱스만 전송), `ProgressDlgShipSearch.java`의 searchType별 분기(263~286줄)에도 searchType="2" 전용 케이스가 없어 GI_L_ID가 항상 기본값 ""으로 남는다. 즉 이마트에서 해결한 패턴이 홈플러스로 포팅되지 않았다.

```java
// ProgressDlgShipSearch.java:265~286 (실제 코드 확인)
if(Common.searchType.equals("0") || Common.searchType.equals("4")) {
    ...
    si.setGI_L_ID(temp[30].toString());          // 출고LOTSEQ ← 이마트/비정량만 파싱
} else if(Common.searchType.equals("5")) {
    ...                                           // GI_L_ID 파싱 없음
} else if(Common.searchType.equals("6")) {
    ...                                           // GI_L_ID 파싱 없음
}
// ★ searchType="2"(홈플러스) 분기 자체가 없음 → si.GI_L_ID = "" (Shipments_Info 기본값) 유지
```

## 원인

### 문제 1 (주요): search_shipment_homeplus.jsp가 GI_L_ID를 SELECT/전송하지 않음

#### 코드 위치
- `search_shipment_homeplus.jsp` : SELECT 절(36~60줄) 및 전송 out.println(122~147줄) 전체 — GI_L_ID 없음, 0~23 인덱스만 전송

#### 현재 문제 코드
```sql
-- search_shipment_homeplus.jsp: SELECT 절에 L.SEQ AS GI_L_ID 없음
-- (참고: L.LOTNO는 월품목별재고화일_LOT별_VIEW JOIN 조건에만 사용, GI_L_ID로 전송되지 않음)
```
```java
// search_shipment_homeplus.jsp:122~147 (실제 코드 확인)
out.println(
    rs.getString("GI_D_ID") + "::" +           // 0
    ...
    rs.getString("EMARTLOGIS_CODE") + ";;"      // 23  ★ GI_L_ID 없이 23에서 종료
);
```

#### 발생 시나리오
`search_shipment_homeplus.jsp`도 이마트와 동일하게 `JOIN SM_출고LOT L ON L.출고상세SEQ = D.SEQ` 구조를 사용한다 (91~92줄).
즉 동일 SM_출고상세(D.SEQ = GI_D_ID)에 SM_출고LOT이 여러 건 연결되면 여러 행이 반환되는 구조는 이마트와 동일하다.
다만 이 LOT행들을 구분할 GI_L_ID가 전송되지 않으므로, 다중 LOT 발생 시 각 행이 서버 응답상으로는 서로 다른 LOT을
나타내지만 앱 내부적으로는 GI_D_ID(+빈 GI_L_ID)로만 식별되어 동일 키로 수렴된다.

### 문제 2 (연쇄): ProgressDlgShipSearch.java에 searchType="2" 전용 파싱 분기 없음

#### 코드 위치
- `ProgressDlgShipSearch.java` : 263~286줄

#### 문제 코드
```java
// ProgressDlgShipSearch.java:263~286 (실제 코드 확인)
// ----- searchType별 추가 필드 설정 (24~) -----
if(Common.searchType.equals("0") || Common.searchType.equals("4")) {
    si.setGI_L_ID(temp[30].toString());
} else if(Common.searchType.equals("5")) {
    // GI_L_ID 파싱 없음 (WH_AREA~STORE_CODE만 파싱)
} else if(Common.searchType.equals("6")) {
    // GI_L_ID 파싱 없음 (WH_AREA, LAST_BOX_ORDER만 파싱)
}
// ★ searchType="2" 분기 없음 → 이 if/else if 블록에 전혀 진입하지 않음
```

## 상세 흐름

1. **홈플러스 출하대상 받기 요청** (앱 → search_shipment_homeplus.jsp)
   - SM_출고상세(D) JOIN SM_출고LOT(L ON L.출고상세SEQ = D.SEQ)
   - 동일 D.SEQ(GI_D_ID)에 대해 SM_출고LOT N건이면 N행 반환 (구조상 이마트와 동일)

2. **서버 응답** (인덱스 0~23만 전송, GI_L_ID 없음)
   - 각 LOT 행의 GI_D_ID는 동일, GI_L_ID에 해당하는 값(L.SEQ)은 전송되지 않음

3. **ProgressDlgShipSearch 파싱** (263~286줄, searchType="2" 분기 없음)
   - si.GI_L_ID = "" (Shipments_Info.java:7 기본값) 그대로 유지
   - 모든 LOT 행이 동일한 GI_D_ID + GI_L_ID("") 조합으로 저장됨

4. **로컬 DB 동기화(추가/삭제 판정)** (ProgressDlgShipSearch.java:309~310, 330~331)
   - `getGI_D_ID().equals(...) && getGI_L_ID().equals(...)` 비교 시 다중 LOT 행이 서로 구분되지 않고 동일 키로 매칭
   - **문제 발생 경로**: 다중 LOT 중 일부만 서버 목록에 남아도 삭제/추가 판정이 LOT 단위로 정확히 이뤄지지 않을 수 있음

5. **DBHandler.refreshShipmentList DELETE WHERE** (DBHandler.java:1909~1911)
   - `(GI_D_ID = '...' AND GI_L_ID = '...')` 조건도 GI_L_ID가 항상 빈 값이므로 LOT 단위 구분 없이 GI_D_ID 단위로만 동작

6. **계근수량 집계** (BixolonShipmentActivity.java:2229, 2424 — selectqueryListGoodsWetInfo 호출, DBHandler.java:1387~1405)
   - WHERE 절이 `GI_D_ID='...' AND GI_L_ID='...'`(항상 "")로 구성되므로, 동일 GI_D_ID를 가진 다중 LOT 행 모두 같은 계근수량이 조회됨
   - **문제 발생 경로**: 이마트 15번과 동일하게, LOT 하나만 계근해도 같은 GI_D_ID의 다른 LOT 행 모두 동일 계근수량으로 표시될 위험

## 영향 범위
- `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp` — GI_L_ID SELECT/전송 없음 (전체)
- `app/src/main/java/.../common/ProgressDlgShipSearch.java` : 263~286줄 — searchType="2" 전용 GI_L_ID 파싱 분기 없음, 309~310/330~331줄 — 동기화 비교 로직
- `app/src/main/java/.../db/DBHandler.java` : 1909~1911줄(refreshShipmentList DELETE WHERE), 1387~1405줄(selectqueryListGoodsWetInfo)
- `app/src/main/java/.../BixolonShipmentActivity.java` : 1592줄(gi.setGI_L_ID 전달), 2229/2424줄(selectqueryListGoodsWetInfo 호출)
- `app/src/main/java/.../db/DBHandler.java` : 1577~1650줄(insertqueryGoodsWetHomeplus) — GI_L_ID 컬럼은 존재하나 항상 빈 값 저장
- searchType=2(홈플러스) 출하대상 동기화·계근수량 집계 전체

## 수정 방안

### 수정 1: search_shipment_homeplus.jsp에 GI_L_ID SELECT/전송 추가 (이마트 패턴 포팅)

```sql
-- search_shipment_homeplus.jsp SELECT 절 수정안 (60줄 이후 추가)
+ ", L.SEQ AS GI_L_ID"
```
```java
// search_shipment_homeplus.jsp out.println 수정안 (147줄)
rs.getString("EMARTLOGIS_CODE") + "::" +      // 23
rs.getString("GI_L_ID") + ";;"                // 24  ★ 추가
```

### 수정 2: ProgressDlgShipSearch.java에 searchType="2" 파싱 분기 추가

```java
// ProgressDlgShipSearch.java 수정안 (263~286줄 영역)
if(Common.searchType.equals("0") || Common.searchType.equals("4")) {
    ...
    si.setGI_L_ID(temp[30].toString());
} else if(Common.searchType.equals("2")) {           // ★ 홈플러스 분기 추가
    si.setGI_L_ID(temp[24].toString());               // 출고LOTSEQ
} else if(Common.searchType.equals("5")) {
    ...
} else if(Common.searchType.equals("6")) {
    ...
}
```

> 인덱스 번호는 실제 search_shipment_homeplus.jsp 수정 후 확정된 out.println 순서에 맞춰 code-verifier로 재검증 필요.
> 추가 확인 필요: 홈플러스가 실제로 1개 출고상세(SM_출고상세)에 다중 LOT(SM_출고LOT)이 연결되는 운영 케이스를 갖는지
> ERP 데이터로 확인 필요 (이마트는 이 케이스가 확인되어 15/16/17번으로 문서화·수정되었으나, 홈플러스는 정적 코드
> 구조상 동일 위험이 존재함을 확인한 것이며 실데이터 재현 여부는 별도 확인 필요).

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/오류/15_GI_D_ID_LOT별_미분리_계근수량_중복표시.md` — 이마트 동일 원인, GI_L_ID 추가로 해결
- `app/doc/오류/16_GI_D_ID_비유니크_전체_영향범위.md` — GI_D_ID 유니크 식별자 사용처 전수 목록 (41곳)
- `app/doc/오류/17_GI_L_ID_단독사용시_사이드이펙트.md` — GI_D_ID 제거(단독 GI_L_ID 사용) 시 부작용, GI_D_ID는 유지하고 GI_L_ID 추가하는 방향으로 결론
- `app/doc/개발/31_GI_L_ID_추가[GI_D_ID_비유니크_전체_영향범위].md` — 이마트 GI_L_ID 추가 개발 문서 (수정 대상 파일 목록·단계별 플랜)
- `app/doc/참고자료/오류패턴_분석.md` — **패턴 D: ID 비유니크** (15/16/17번, searchType=2 추가 체크 2번: insertqueryGoodsWetHomeplus()에 GI_L_ID 컬럼 추가 — 컬럼 자체는 존재하나 값이 항상 빈 문자열인 점은 본 문서에서 신규 확인)
- 검증 출처: 홈플러스 AI 정적검증(code-verifier), 원본 비교(original-comparator)는 비허용 차이 없음으로 통과 — 단 GI_L_ID 자체가 이마트 전환 후 신규 추가된 필드이므로 원본(Oracle 시절) 비교 대상 아님
- JSP: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp`
