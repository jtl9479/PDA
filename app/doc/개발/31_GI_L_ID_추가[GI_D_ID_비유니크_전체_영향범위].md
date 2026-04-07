# GI_L_ID(출고LOTSEQ) 추가

**작성일**: 2026-04-07
**목적**: GI_D_ID 비유니크 문제 해결을 위해 GI_L_ID(SM_출고LOT.SEQ) 추가
**관련 오류**: `app/doc/오류/15_GI_D_ID_LOT별_미분리_계근수량_중복표시.md`, `app/doc/오류/16_GI_D_ID_비유니크_전체_영향범위.md`

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### 문제

- GI_D_ID = SM_출고상세.SEQ → 1개 출고상세에 SM_출고LOT가 여러 개 → GI_D_ID 중복
- GI_D_ID를 유니크 식별자로 사용하는 41곳에서 오동작

### 해결 방향

- GI_D_ID는 기존대로 유지 (서버 인터페이스용)
- GI_L_ID(SM_출고LOT.SEQ) 신규 추가 (LOT별 유니크 식별용)
- MSSQL/JSP 컬럼명: `출고LOTSEQ`
- PDA Java 별칭: `GI_L_ID`

---

## 2. 변경 구조

```
[변경 전]
GI_D_ID(SM_출고상세.SEQ) 단독으로 유니크 식별
→ LOT 여러 개일 때 GI_D_ID 중복 → 오동작

[변경 후]
GI_D_ID(유지) + GI_L_ID(추가) 조합으로 유니크 식별
→ GI_D_ID: 서버 전송용 (SM_출고계근.출고상세SEQ)
→ GI_L_ID: LOT별 유니크 식별 (SM_출고LOT.SEQ)
```

---

## 3. 수정 대상 파일

| # | 파일 | 수정 내용 |
|:-:|------|----------|
| 1 | MSSQL SM_출고계근 | ALTER TABLE ADD `출고LOTSEQ` |
| 2 | search_shipment.jsp | SELECT에 L.SEQ AS GI_L_ID 추가 |
| 3 | insert_goods_wet.jsp | INSERT에 `출고LOTSEQ` 컬럼 추가 |
| 4 | DBInfo.java | GI_L_ID 상수 추가 |
| 5 | Shipments_Info.java | GI_L_ID 필드/getter/setter 추가 |
| 6 | Goodswets_Info.java | GI_L_ID 필드/getter/setter 추가 |
| 7 | DBHandler.java | CREATE TABLE, INSERT, SELECT, UPDATE, DELETE에 GI_L_ID 추가 (약 16곳) |
| 8 | ProgressDlgShipSearch.java | 파싱 시 GI_L_ID 추가, 동기화 비교 조합 |
| 9 | ProgressDlgGoodsWetSearch.java | 파싱 시 GI_L_ID 추가 |
| 10 | BixolonShipmentActivity.java | 계근/매칭/호출에 GI_L_ID 추가 (약 11곳) |
| 11 | ShipmentActivity.java | 동일 패턴 (약 8곳) |

---

## 4. 개발 플랜

### Step 1: MSSQL 테이블 ALTER TABLE

SM_출고계근 테이블에 `출고LOTSEQ` 컬럼 추가

```sql
ALTER TABLE SM_출고계근 ADD 출고LOTSEQ BIGINT NOT NULL DEFAULT 0
```

**체크리스트**
- [ ] ERP 담당자 협의
- [ ] ALTER TABLE 실행
- [ ] 컬럼 추가 확인

---

### Step 2: 모델/상수 추가 (DBInfo, Shipments_Info, Goodswets_Info)

**DBInfo.java:**
```java
public static final String GI_L_ID = "GI_L_ID";
```

**Shipments_Info.java:**
```java
public String GI_L_ID = "";
public String getGI_L_ID() { return GI_L_ID; }
public void setGI_L_ID(String GI_L_ID) { this.GI_L_ID = GI_L_ID; }
```

**Goodswets_Info.java:**
```java
public String GI_L_ID = "";
public String getGI_L_ID() { return GI_L_ID; }
public void setGI_L_ID(String GI_L_ID) { this.GI_L_ID = GI_L_ID; }
```

**체크리스트**
- [ ] DBInfo 상수 추가
- [ ] Shipments_Info 필드/getter/setter 추가
- [ ] Goodswets_Info 필드/getter/setter 추가
- [ ] 컴파일 확인

---

### Step 3: DB CREATE TABLE 수정

**DBHandler.createqueryShipment() (35줄):**
- TB_SHIPMENT에 GI_L_ID TEXT NOT NULL 컬럼 추가

**DBHandler.createqueryGoodsWet() (1189줄):**
- TB_GOODS_WET에 GI_L_ID TEXT NOT NULL 컬럼 추가

**주의**: 기존 테이블이 있으면 CREATE TABLE IF NOT EXISTS가 컬럼을 추가하지 않음. 앱 재설치 또는 테이블 삭제 후 재생성 필요.

**체크리스트**
- [ ] createqueryShipment 수정
- [ ] createqueryGoodsWet 수정
- [ ] 컴파일 확인

---

### Step 4: JSP 수정 (search_shipment.jsp + 다른 searchType JSP)

**이마트(searchType=0):**
- search_shipment.jsp: SELECT에 L.SEQ AS GI_L_ID 추가 (마지막 index 30에 추가 → 기존 0~29 유지)

**다른 searchType (현재 작업 범위 외, 별도 진행):**
- search_shipment_homeplus.jsp (searchType=2)
- search_shipment_wholesale.jsp (searchType=3)
- search_production_nonfixed.jsp (searchType=4)
- search_homeplus_nonfixed.jsp / search_homeplus_nonfixed2.jsp (searchType=5)
- search_shipment_lotte.jsp (searchType=6)
- search_production.jsp (searchType=1), search_production_4label.jsp (searchType=7)

**주의**: out.println 출력 순서가 변경되므로 ProgressDlgShipSearch 파싱 인덱스도 변경 필요 (Step 5에서 처리)

**체크리스트**
- [ ] search_shipment.jsp SELECT 컬럼 추가 (index 30)
- [ ] 출력 순서(index) 확인 (기존 0~29 유지)
- [ ] JSP 커밋
- [ ] (별도) 다른 searchType JSP GI_L_ID 추가 (이마트 완료 후 진행)

---

### Step 5: 파싱 + 동기화 수정 (ProgressDlgShipSearch, ProgressDlgGoodsWetSearch)

**5-1. 파싱 추가**

ProgressDlgShipSearch.java (211줄):
- 이마트(searchType=0): temp[30]에 GI_L_ID 추가 → si.setGI_L_ID(temp[30])
- 기존 temp[0]~temp[29] 변경 없음

ProgressDlgGoodsWetSearch.java (101줄):
- 서버 계근 데이터 파싱 시 GI_L_ID 추가 (search_goods_wet.jsp도 수정 필요한지 확인)

**5-2. 동기화 비교 수정**

ProgressDlgShipSearch.java (308, 328줄):
- 현재: `getGI_D_ID().equals(getGI_D_ID())` → GI_D_ID만 비교
- 변경: `getGI_D_ID().equals() && getGI_L_ID().equals()` → GI_D_ID + GI_L_ID 조합 비교

**5-3. 동기화 삭제 로직 자료구조 변경**

ProgressDlgShipSearch.java (299~321줄):
- 현재: `list_delete`가 `ArrayList<String>` (GI_D_ID만 담김)
- 변경: GI_D_ID + GI_L_ID 조합으로 삭제할 수 있도록 자료구조 변경
  - 방안 A: `ArrayList<String[]>` (String[0]=GI_D_ID, String[1]=GI_L_ID)
  - 방안 B: `ArrayList<Shipments_Info>` (객체 전체 전달)
- refreshShipmentList() (1880줄)의 DELETE WHERE도 `GI_D_ID AND GI_L_ID` 조합으로 변경 필요

**5-4. refreshShipmentList() AND/OR 버그 수정 (오류18)**

DBHandler.java refreshShipmentList() (1878~1883줄):
- 현재: 삭제 대상 여러 건을 **AND**로 연결 → 2건 이상이면 삭제 안됨 (기존 버그)
- 변경: 각 삭제 대상을 `(GI_D_ID = ? AND GI_L_ID = ?)` 형태로 만들고 행 간에는 **OR**로 연결
- 관련 오류: `app/doc/오류/18_refreshShipmentList_AND_OR_연산자_버그.md`

```sql
-- 변경 후 예상
WHERE (GI_D_ID = '1330' AND GI_L_ID = '5001') OR (GI_D_ID = '1330' AND GI_L_ID = '5003')
```

**체크리스트**
- [ ] ProgressDlgShipSearch 파싱 수정 (temp[30])
- [ ] ProgressDlgShipSearch 동기화 비교 수정 (GI_D_ID + GI_L_ID)
- [ ] ProgressDlgShipSearch list_delete 자료구조 변경
- [ ] refreshShipmentList() AND→OR 수정 + GI_L_ID 조건 추가 (오류18)
- [ ] DBHandler.refreshShipmentList() DELETE WHERE 변경
- [ ] ProgressDlgGoodsWetSearch 파싱 수정
- [ ] 컴파일 확인

---

### Step 6: DBHandler 수정

**INSERT (GI_L_ID 컬럼/값 추가):**
- insertqueryShipment() (694줄)
- insertqueryGoodsWet() (1496줄)
- insertqueryGoodsWetHomeplus() (1566줄)
- insertqueryGoodsWetLotte() (1639줄)

**SELECT - WHERE에 GI_L_ID 조건 추가:**
- selectqueryGoodsWet() (1253줄): WHERE에 GI_L_ID 추가
- selectqueryListGoodsWetInfo() (1385줄): WHERE에 GI_L_ID 추가
- duplicatequeryGoodsWet() (1459줄): WHERE에 GI_L_ID 추가

**SELECT - GI_L_ID 컬럼/매핑 추가 (TB_SHIPMENT 조회):**
- selectqueryShipment() (SELECT 100줄, 매핑 148~180줄)
- selectqueryShipmentOnly() (SELECT 205줄, 매핑 244~269줄)
- selectqueryShipmentBL() (SELECT 293줄, 매핑 332~358줄)
- selectqueryAllShipment() (SELECT 380줄, 매핑 395줄): 동기화 비교에 GI_L_ID 필요

**SELECT - GI_L_ID 컬럼/매핑 추가 (TB_GOODS_WET 조회):**
- selectquerySendGoodsWet() (SELECT 1305줄, 매핑 1338줄): packet에 GI_L_ID 추가하려면 여기서 SELECT/매핑 필수

**UPDATE (GI_L_ID 조건 추가):**
- updatequeryShipment() (811줄): WHERE에 GI_L_ID 추가

**DELETE (GI_L_ID 조건 추가):**
- refreshShipmentList() (1880줄): 삭제 조건에 GI_L_ID 추가

**체크리스트**
- [ ] INSERT 4곳 수정
- [ ] SELECT WHERE 3곳 수정
- [ ] SELECT TB_SHIPMENT 조회 4곳 컬럼/매핑 추가 (selectqueryShipment, ShipmentOnly, ShipmentBL, AllShipment)
- [ ] SELECT TB_GOODS_WET 조회 1곳 컬럼/매핑 추가 (selectquerySendGoodsWet)
- [ ] UPDATE 1곳 수정
- [ ] DELETE 1곳 수정 (refreshShipmentList - Step 5에서 자료구조 변경과 연동)
- [ ] 컴파일 확인

---

### Step 7: BixolonShipmentActivity 수정

**계근 저장:**
- 1552줄: gi.setGI_L_ID(arSM.get(pos).getGI_L_ID()) 추가

**WHERE 조건 호출:**
- 1272줄: duplicatequeryGoodsWet 호출 시 GI_L_ID 파라미터 추가
- 2189줄: selectqueryListGoodsWetInfo 호출 시 GI_L_ID 파라미터 추가
- 2380줄: 동일 (수기입력)
- 2472줄: duplicatequeryGoodsWet 호출 시 GI_L_ID 파라미터 추가
- 3238줄: selectqueryGoodsWet 호출 시 GI_L_ID 파라미터 추가

**매칭:**
- 880줄: GI_D_ID + GI_L_ID 조합 비교
- 2635줄: 전송 후 arSM 매칭에 GI_L_ID 비교 추가
- 2763줄: 동일 (생산/도매)

**전송:**
- 2662줄: updatequeryShipment 호출 시 GI_L_ID 파라미터 추가
- 2795줄: 동일 (생산/도매)

**서버 전송 packet:**
- 2590줄: packet에 GI_L_ID 추가
- 2696줄: 동일 (생산/도매)

**체크리스트**
- [ ] 계근 저장 1곳
- [ ] WHERE 호출 5곳
- [ ] 매칭 3곳
- [ ] 전송 2곳
- [ ] packet 2곳
- [ ] 컴파일 확인

---

### Step 8: JSP 수정 (insert_goods_wet.jsp)

SM_출고계근 INSERT에 `출고LOTSEQ` 컬럼 추가
- packet에서 GI_L_ID 값을 받아서 `출고LOTSEQ` 컬럼에 INSERT

**체크리스트**
- [ ] insert_goods_wet.jsp 수정
- [ ] splitData 인덱스 확인
- [ ] JSP 커밋

---

### Step 9: ShipmentActivity 수정 (동일 패턴)

BixolonShipmentActivity와 동일 패턴 (약 8곳)

**체크리스트**
- [ ] 806줄: 행 선택 매칭
- [ ] 1116줄: duplicatequeryGoodsWet 호출
- [ ] 1396줄: gi.setGI_L_ID 추가
- [ ] 3239, 3430줄: selectqueryListGoodsWetInfo 호출
- [ ] 3522줄: duplicatequeryGoodsWet 호출
- [ ] 3686, 3814줄: 전송 후 매칭
- [ ] 3713, 3846줄: updatequeryShipment 호출
- [ ] 4261줄: selectqueryGoodsWet 호출
- [ ] 컴파일 확인

---

### Step 10: B-1그룹 수정 (권장)

**DBHandler:**
- updatequeryGoodsWet() (1710줄): WHERE에 GI_L_ID 추가
- deletequerySelectGoodsWet() (1739줄): WHERE에 GI_L_ID 추가

**BixolonShipmentActivity:**
- 2629, 2758줄: updatequeryGoodsWet 호출 시 GI_L_ID 추가
- 3278줄: deletequerySelectGoodsWet 호출 시 GI_L_ID 추가

**체크리스트**
- [ ] DBHandler 2곳 수정
- [ ] BixolonShipmentActivity 3곳 수정
- [ ] 컴파일 확인

---

### Step 11: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 출하대상받기 → TB_SHIPMENT에 GI_L_ID 저장 확인 | □ |
| 2 | 바코드 스캔 → 센터 총수량 LOT별 정상 분리 | □ |
| 3 | 계근 → TB_GOODS_WET에 GI_L_ID 저장 확인 | □ |
| 4 | 뒤로가기 → 재진입 → 계근수량 LOT별 정상 분리 | □ |
| 5 | 계근 완료 → 전송 버튼 활성화 | □ |
| 6 | 전송 → SM_출고계근에 출고상세SEQ + 출고LOTSEQ 정상 INSERT | □ |
| 7 | ERP에서 데이터 확인 | □ |

---

### 개발 순서 요약

```
Step 1:  MSSQL ALTER TABLE (ERP 담당자 협의)
    ↓
Step 2:  모델/상수 추가
    ↓
Step 3:  DB CREATE TABLE 수정
    ↓
Step 4:  search_shipment.jsp 수정
    ↓
Step 5:  파싱 수정
    ↓
Step 6:  DBHandler 수정
    ↓
Step 7:  BixolonShipmentActivity 수정
    ↓
Step 8:  insert_goods_wet.jsp 수정
    ↓
Step 9:  ShipmentActivity 수정
    ↓
Step 10: B-1그룹 수정
    ↓
Step 11: 통합 테스트
```

---

## 5. 사이드이펙트

- GI_D_ID는 모든 곳에서 유지 → 서버 인터페이스 영향 없음
- GI_L_ID는 추가만 하므로 기존 동작 깨지지 않음
- CREATE TABLE 변경 시 기존 테이블 재생성 필요 (앱 재설치 또는 테이블 삭제)
- search_shipment.jsp SELECT 컬럼 추가 시 out.println 인덱스 변경 → 파싱 인덱스 동기화 필수

---

## 6. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | MSSQL ALTER TABLE | ⏳ 대기 |
| 2 | 모델/상수 추가 | ⏳ 대기 |
| 3 | DB CREATE TABLE 수정 | ⏳ 대기 |
| 4 | search_shipment.jsp 수정 | ⏳ 대기 |
| 5 | 파싱 수정 | ⏳ 대기 |
| 6 | DBHandler 수정 | ⏳ 대기 |
| 7 | BixolonShipmentActivity 수정 | ⏳ 대기 |
| 8 | insert_goods_wet.jsp 수정 | ⏳ 대기 |
| 9 | ShipmentActivity 수정 | ⏳ 대기 |
| 10 | B-1그룹 수정 | ⏳ 대기 |
| 11 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/오류/15_GI_D_ID_LOT별_미분리_계근수량_중복표시.md`
- `app/doc/오류/16_GI_D_ID_비유니크_전체_영향범위.md`
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`
- `app/doc/소스분석/37_로컬DB_MSSQL전환_현황.md`

---

**문서 버전**: 1.0
