# 홈플러스 출하대상 조회 행중복 — 월품목별재고화일_LOT별_VIEW 년월조건 누락

## 발견일
2026-07-23

## 에러 발생 시나리오

```
1. BixolonShipmentActivity에서 홈플러스(searchType=2) 출하대상 받기 실행
2. search_shipment_homeplus.jsp 호출 (회사코드/출고사업장/출고일자/창고코드/타입구분 등 조건 전송)
3. 동일 LOT(SM_출고LOT.LOTNO)이 월품목별재고화일_LOT별_VIEW에 여러 월(년월) 재고행으로 존재하는 경우,
   LEFT JOIN 조건에 년월 조건이 없어 해당 LOT이 재고 이력 개월 수만큼 중복 매칭됨
4. 앱 출하대상 리스트에 동일 품목/LOT 행이 N배로 표시되고, 계근 완료 후 SAVE_CNT/PACKING_QTY 집계가
   실제 계근의 N배로 부풀려짐
```

---

## 현상
- 출하대상 조회 시 실제 LOT 건수보다 많은 행이 반환될 수 있음 (동일 LOT이 재고 이력 월 수만큼 중복)
- 앱 화면에서 동일 품목/LOT 행이 중복으로 표시됨
- 계근 수량 집계(SAVE_CNT/PACKING_QTY)가 TB_SHIPMENT 중복 행 기준으로 부풀려짐
- **핵심 현상: 한 LOT이 월별 재고 이력 수만큼 중복 행으로 반환되어 계근 집계가 부풀어오름 (이마트 25번과 동일 구조)**
- 우선순위: 높음

## 원래부터 있던 버그인가?

**NO — 이마트(searchType=0) 전환 시 이미 수정·검증된 패턴이 홈플러스 JSP에는 아직 반영되지 않은 미포팅 사례**

- `search_shipment.jsp`(이마트)는 커밋 `dce6722`("54_search_shipment_V뷰_년월조건_추가")에서 `AND V.년월 = LEFT(D.출고일자, 6)` 조건이 이미 추가되어 검증까지 완료됨 (`app/doc/오류/25_출하대상_조회_행중복_월품목별재고화일LOT별VIEW_년월조건누락.md` 참조).
- 그러나 `search_shipment_homeplus.jsp`는 동일 VIEW를 JOIN하면서도 이 조건이 아직 반영되지 않았다.
- 즉, "MSSQL 전환 시 새로 도입된 VIEW에 년월 조건이 누락"된 근본 원인은 25번과 동일하며, 이마트에서 수정한 패턴이 홈플러스 JSP로 포팅되지 않아 발생한 오류다.

```sql
-- search_shipment.jsp (이마트, 수정 완료 — 100~106줄)
LEFT JOIN 월품목별재고화일_LOT별_VIEW V
  ON V.회사코드 = D.회사코드
 AND V.사업장 = D.출고사업장
 AND V.창고코드 = D.창고코드
 AND V.품목코드 = D.출고품목코드
 AND V.LOTNO = L.LOTNO
 AND V.년월 = LEFT(D.출고일자, 6)   -- ★ 이마트는 이미 반영됨

-- search_shipment_homeplus.jsp (홈플러스, 미반영 — 93~98줄)
LEFT JOIN 월품목별재고화일_LOT별_VIEW V
  ON V.회사코드 = D.회사코드
 AND V.사업장 = D.출고사업장
 AND V.창고코드 = D.창고코드
 AND V.품목코드 = D.출고품목코드
 AND V.LOTNO = L.LOTNO
-- ★ AND V.년월 = LEFT(D.출고일자, 6) 조건 없음 (미포팅)
```

## 원인

### 문제 1 (주요): 월품목별재고화일_LOT별_VIEW JOIN에 년월 조건 누락

#### 코드 위치
- `search_shipment_homeplus.jsp` : 93~98줄

#### 현재 문제 코드
```sql
-- search_shipment_homeplus.jsp (93~98줄, 실제 코드 확인)
+ " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"
+ "   ON V.회사코드 = D.회사코드"
+ "  AND V.사업장 = D.출고사업장"
+ "  AND V.창고코드 = D.창고코드"
+ "  AND V.품목코드 = D.출고품목코드"
+ "  AND V.LOTNO = L.LOTNO"
// ★ AND V.년월 = LEFT(D.출고일자, 6) 조건 누락
//    → 동일 LOTNO가 여러 달 재고행으로 존재하면 모두 매칭 → 행 중복
```

#### 발생 시나리오
`월품목별재고화일_LOT별_VIEW`는 년월(월별)로 행이 나뉘는 VIEW다. 이마트 25번 오류에서 DB 실측으로 확인된 바와 같이,
같은 LOT이 2개월치 재고행으로 VIEW에 존재하면 년월 조건 없이 LOTNO만으로 JOIN 시 두 달 행 모두 매칭되어
SM_출고LOT 1행 × V 2행 = 2행이 된다. 홈플러스도 동일 VIEW·동일 JOIN 키(회사코드/사업장/창고코드/품목코드/LOTNO) 구조를
그대로 사용하므로 동일 조건에서 동일하게 재현될 수 있다.

## 상세 흐름

1. **홈플러스 출하대상 받기 요청** (앱 → search_shipment_homeplus.jsp)
   - SM_출고상세(D) JOIN SM_출고LOT(L) → 출고 LOT N행

2. **월품목별재고화일_LOT별_VIEW LEFT JOIN** (조건: 회사코드/사업장/창고코드/품목코드/LOTNO)
   - 동일 LOTNO가 VIEW에서 여러 년월 행으로 존재하는 경우 모두 매칭
   - **년월 조건 없으므로 재고 이력 월 수만큼 팽창**

3. **결과 반환** (중복 행 포함)
   - 출고 LOT N행 × VIEW M달 매칭 = N×M행
   - 각 중복 행의 BL_NO/이력번호/PACKWEIGHT 값은 월별로 상이할 수 있음

4. **ProgressDlgShipSearch 파싱** (중복 행 그대로 파싱)
   - TB_SHIPMENT에 중복 행 INSERT

5. **계근 집계 오류** (BixolonShipmentActivity:2229, 2424 — selectqueryListGoodsWetInfo 호출)
   - **문제 발생 경로**: TB_SHIPMENT 중복 행 수만큼 SAVE_CNT/PACKING_QTY가 부풀려져 표시될 수 있음

## 영향 범위
- `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp` : 93~98줄 — 조회 쿼리 (JOIN 조건 누락 위치)
- `app/src/main/java/.../common/ProgressDlgShipSearch.java` — 출하대상 파싱 (중복 행 그대로 로컬 DB 저장)
- `app/src/main/java/.../BixolonShipmentActivity.java` : 2229, 2424줄 — selectqueryListGoodsWetInfo 호출, SAVE_CNT/PACKING_QTY 부풀림 가능
- `app/src/main/java/.../db/DBHandler.java` : 1387줄 — selectqueryListGoodsWetInfo 함수 (count 집계 쿼리)
- searchType=2(홈플러스) 전체 출하대상 조회 흐름

## 수정 방안

### 수정 1: V JOIN에 년월 조건 추가 (이마트 패턴 포팅)

```sql
-- search_shipment_homeplus.jsp 수정안 (93~98줄)
+ " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"
+ "   ON V.회사코드 = D.회사코드"
+ "  AND V.사업장 = D.출고사업장"
+ "  AND V.창고코드 = D.창고코드"
+ "  AND V.품목코드 = D.출고품목코드"
+ "  AND V.LOTNO = L.LOTNO"
+ "  AND V.년월 = LEFT(D.출고일자, 6)"   // ★ 이마트와 동일하게 추가
```

> 엣지케이스: 출고월에 해당 LOT의 재고화일 행이 없는 경우 LEFT JOIN NULL 반환 → BL_NO·이력번호·PACKWEIGHT NULL/빈값 가능 (이마트 25번과 동일 주의사항).

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/오류/25_출하대상_조회_행중복_월품목별재고화일LOT별VIEW_년월조건누락.md` — 이마트 동일 원인, 이미 수정 완료(커밋 dce6722)
- `app/doc/오류/22_잠재오류_18건_목록.md` — 해당 없음 (본 문서는 25번 패턴의 홈플러스 미반영 케이스)
- `app/doc/참고자료/오류패턴_분석.md` — **패턴 C: 컬럼명/구조 변경** (공통 체크 13번: 년월로 행이 나뉘는 VIEW LEFT JOIN 시 년월 조건 확인, searchType=2 추가 체크 6번)
- 검증 출처: 홈플러스 AI 정적검증(code-verifier), 원본 비교(original-comparator)는 비허용 차이 없음으로 통과
- JSP: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp`
