# search_shipment.jsp 출하대상 조회 행 중복 — 월품목별재고화일_LOT별_VIEW 년월 조건 누락

## 발견일
2026-06-29

## 에러 발생 시나리오

```
1. BixolonShipmentActivity에서 이마트(searchType=0) 출하대상 받기 실행
2. search_shipment.jsp 호출 (회사코드='20', 출고일자='20260629', 창고코드='W301', 타입구분='W')
3. 출하대상 2건이 실제로는 4건으로 응답됨
4. 앱 출하대상 리스트에 동일 LOT 행이 2배로 표시되고, 계근 완료 후 SAVE_CNT/PACKING_QTY 집계가 실제 계근의 2배로 부풀려짐
```

---

## 현상
- 출하대상이 2건(LOT 2개)이어야 하는데 4건으로 조회됨
- 앱 화면에서 동일 품목/LOT 행이 중복으로 표시됨
- 계근 수량 집계: 요청 2건 대비 계근 4건으로 표시 (요청2 / 계근4)
- **핵심 현상: 한 LOT이 월별 재고 이력 수만큼 중복 행으로 반환되어 계근 집계가 부풀어오름**

## 원래부터 있던 버그인가?

**NO — MSSQL 전환 시 신규 도입된 VIEW에 년월 조건을 누락한 전환 버그**

Oracle 원본 JSP(`apache-tomcat-7.0.78_PDA_IN(원본)`)에는 `월품목별재고화일_LOT별_VIEW` JOIN 자체가 존재하지 않는다.
해당 VIEW는 MSSQL ERP 전환 시 평균중량(PACKWEIGHT)/BL_NO/이력번호 조회 목적으로 새로 추가된 것으로,
추가 당시 년월 조건이 누락된 채 배포됨.

```sql
-- 원본 Oracle JSP: 월품목별재고화일_LOT별_VIEW JOIN 없음 (grep 결과 0건)
-- MSSQL 전환 JSP: LEFT JOIN 월품목별재고화일_LOT별_VIEW V 추가 (년월 조건 없이)
```

## 원인

### 문제 1 (주요): 월품목별재고화일_LOT별_VIEW JOIN에 년월 조건 누락

#### 코드 위치
- `search_shipment.jsp` : 100~105줄 (수정 전 기준)

#### 현재 문제 코드
```sql
-- search_shipment.jsp (수정 전)
LEFT JOIN 월품목별재고화일_LOT별_VIEW V
  ON V.회사코드 = D.회사코드
 AND V.사업장 = D.출고사업장
 AND V.창고코드 = D.창고코드
 AND V.품목코드 = D.출고품목코드
 AND V.LOTNO = L.LOTNO
-- ★ AND V.년월 = LEFT(D.출고일자, 6) 조건 누락
--    → 동일 LOTNO가 202605·202606 등 여러 달 재고행 모두 매칭 → 행 중복
```

#### 발생 시나리오
`월품목별재고화일_LOT별_VIEW`는 **년월(월별)로 행이 나뉘는 뷰**이다.
같은 LOT(예: P10202605260002)이 202605·202606 두 달 재고로 VIEW에 2행 존재하면 (이력번호 동일 905002407409),
년월 조건 없이 LOTNO만으로 JOIN하면 두 달 행 모두 매칭된다.
결과적으로 SM_출고LOT 1행 × V 2행 = 2행이 된다.
출고 LOT이 2개면 2×2 = 4행 반환.

DB 실측 증거:
- V 제외 조회 = 2행
- V 포함 (년월 조건 없음) = 4행
- V뷰 직접 조회: 동일 LOTNO에 년월 202605·202606 각 1행 (재고수량 60 vs 12)

## 상세 흐름

1. **출하대상 받기 요청** (앱 → search_shipment.jsp)
   - 파라미터: 회사코드='20', 출고일자='20260629', 창고코드='W301', 타입구분='W'
   - SM_출고상세(D) JOIN SM_출고LOT(L) → 출고 LOT 2행

2. **월품목별재고화일_LOT별_VIEW LEFT JOIN** (조건: 회사코드/사업장/창고코드/품목코드/LOTNO)
   - LOTNO=P10202605260002 → VIEW에서 년월 202605·202606 각 1행 매칭
   - **년월 조건 없으므로 두 달 행 모두 JOIN** → 1행이 2행으로 팽창

3. **결과 반환** (4행)
   - 출고 LOT 2행 × VIEW 2달 = 4행
   - 각 중복 행의 BL_NO/이력번호/PACKWEIGHT 값은 달별로 다름 (재고수량 60 vs 12)

4. **ProgressDlgShipSearch 파싱** (4행 그대로 파싱)
   - TB_SHIPMENT에 4행 INSERT
   - GI_D_ID+GI_L_ID 기준으로 동일 KEY가 중복 저장됨

5. **계근 집계 오류** (BixolonShipmentActivity:2224 호출)
   - `selectqueryListGoodsWetInfo(GI_D_ID, pp_code, client_code, GI_L_ID)`
   - TB_GOODS_WET의 `count(GI_D_ID)` → TB_SHIPMENT 중복 행 기준으로 집계
   - **문제 발생 경로**: 실제 계근 2건이더라도 TB_SHIPMENT 행이 4건이면 SAVE_CNT=4, PACKING_QTY=4 반환

## 영향 범위
- `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment.jsp` — 조회 쿼리 (JOIN 조건 누락 위치)
- `app/src/main/java/.../common/ProgressDlgShipSearch.java` — 출하대상 파싱 (중복 행 그대로 로컬 DB 저장)
- `app/src/main/java/.../BixolonShipmentActivity.java` : 2224, 2419줄 — selectqueryListGoodsWetInfo 호출, SAVE_CNT/PACKING_QTY 부풀림
- `app/src/main/java/.../ShipmentActivity.java` : 3239, 3430줄 — 동일 함수 호출 (원본 Activity 동일 영향)
- `app/src/main/java/.../db/DBHandler.java` : 1387줄 — selectqueryListGoodsWetInfo 함수 (count 집계 쿼리)

## 수정 방안

### 수정 1: V JOIN에 년월 조건 추가 (이미 적용 완료)

```sql
-- search_shipment.jsp 수정 후 (100~106줄)
LEFT JOIN 월품목별재고화일_LOT별_VIEW V
  ON V.회사코드 = D.회사코드
 AND V.사업장 = D.출고사업장
 AND V.창고코드 = D.창고코드
 AND V.품목코드 = D.출고품목코드
 AND V.LOTNO = L.LOTNO
 AND V.년월 = LEFT(D.출고일자, 6)   -- ★ 출고월 기준 재고행만 매칭
```

수정 후 DB 검증:
- V 포함 (년월 조건 추가) = 2행 → 정상

> **엣지케이스 주의**: 출고월에 해당 LOT의 재고화일 행이 없는 경우 LEFT JOIN NULL 반환 → BL_NO·이력번호·PACKWEIGHT가 NULL/빈값.
> 이 경우 BL_NO에 대해 `COALESCE(NULLIF(V.BLNO, ''), V.이력번호)`가 NULL을 반환하므로, 앱에서 BL_NO 빈값 처리가 필요한지 별도 확인 필요.

### 수정 2 (권고): 동일 패턴 VIEW 전수 점검

월품목별재고화일_LOT별_VIEW와 같이 **년월 컬럼으로 행이 나뉘는 VIEW**를 JOIN하는 모든 JSP에 대해 년월 조건 누락 여부를 점검한다.
오류패턴_분석.md 공통 체크 13번 항목("LEFT JOIN 대상 테이블에 1:N 관계 발생 가능성 확인") 참조.

## 상태
- [x] 수정 완료 (커밋 dce6722 "54_search_shipment_V뷰_년월조건_추가" — 이마트 search_shipment.jsp에 `AND V.년월 = LEFT(D.출고일자, 6)` 조건 추가)
  - 참고: 동일 패턴이 홈플러스 search_shipment_homeplus.jsp에는 미반영 → 오류 32번으로 별도 문서화

## 관련 문서
- `app/doc/오류/15_GI_D_ID_LOT별_미분리_계근수량_중복표시.md` — 동류 오류 (재고LOT뷰 중복 → 계근수량 부풀림)
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` — 전체 흐름 분석
- `app/doc/참고자료/오류패턴_분석.md` — **패턴 C: 컬럼명/구조 변경** (공통 체크 13번: LEFT JOIN 1:N 관계)
