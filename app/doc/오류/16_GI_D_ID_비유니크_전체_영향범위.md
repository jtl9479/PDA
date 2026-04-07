# GI_D_ID 비유니크 전체 영향 범위

## 발견일
2026-04-07

## 현상
- Oracle→MSSQL 전환 후 GI_D_ID(=SM_출고상세.SEQ)가 LOT별로 동일해짐
- GI_D_ID를 유니크 식별자로 사용하는 모든 곳에서 오동작 가능

## 원래부터 있던 버그인가?

**NO - Oracle→MSSQL 전환에서 발생한 구조 차이**

- Oracle: GI_D_ID = LOT별 고유값
- MSSQL: GI_D_ID = SM_출고상세.SEQ (1개 SEQ에 SM_출고LOT 여러 개 → GI_D_ID 중복)

## 해결 방향

GI_D_ID는 기존대로 유지하고, **GI_L_ID(SM_출고LOT.SEQ)를 추가 컬럼으로 신규 추가**
- MSSQL/JSP 컬럼명: `출고LOTSEQ`
- PDA Java 별칭: `GI_L_ID`
- search_shipment.jsp에서 L.SEQ AS GI_L_ID 추가
- SM_출고계근 INSERT 시 `출고LOTSEQ` 컬럼 추가

---

## GI_D_ID 사용처 전수 확인 (GI_L_ID 추가 필요 여부)

### A그룹: GI_L_ID 추가 필요 (GI_D_ID로 유니크 식별하는 곳)

#### 모델/상수

| 파일 | 줄 | 내용 |
|------|:--:|------|
| DBInfo.java | 11 | GI_L_ID 상수 추가 필요 |
| Shipments_Info.java | 6, 75-81 | GI_L_ID 필드/getter/setter 추가 |
| Goodswets_Info.java | 6, 38-43 | GI_L_ID 필드/getter/setter 추가 |

#### DB 테이블 CREATE

| 파일 | 줄 | 테이블 |
|------|:--:|--------|
| DBHandler.java | 35 | TB_SHIPMENT CREATE - GI_L_ID 컬럼 추가 |
| DBHandler.java | 1189 | TB_GOODS_WET CREATE - GI_L_ID 컬럼 추가 |

#### DB SELECT (GI_L_ID 조건 추가 필요)

| 파일 | 줄 | 메서드 | 현재 WHERE |
|------|:--:|--------|-----------|
| DBHandler.java | 1253 | selectqueryGoodsWet() | GI_D_ID + PP_CODE |
| DBHandler.java | 1385 | selectqueryListGoodsWetInfo() | GI_D_ID + PP_CODE |
| DBHandler.java | 1459 | duplicatequeryGoodsWet() | GI_D_ID + PP_CODE + BARCODE |

#### DB INSERT (GI_L_ID 컬럼/값 추가 필요)

| 파일 | 줄 | 메서드 |
|------|:--:|--------|
| DBHandler.java | 694 | insertqueryShipment() |
| DBHandler.java | 1496 | insertqueryGoodsWet() |
| DBHandler.java | 1566 | insertqueryGoodsWetHomeplus() |
| DBHandler.java | 1639 | insertqueryGoodsWetLotte() |

#### DB UPDATE (GI_L_ID 조건 추가 필요)

| 파일 | 줄 | 메서드 | 현재 WHERE |
|------|:--:|--------|-----------|
| DBHandler.java | 811 | updatequeryShipment() | GI_D_ID + PP_CODE |

#### DB DELETE (GI_L_ID 조건 추가 필요)

| 파일 | 줄 | 메서드 |
|------|:--:|--------|
| DBHandler.java | 1880 | refreshShipmentList() |

#### 파싱

| 파일 | 줄 | 용도 |
|------|:--:|------|
| ProgressDlgShipSearch.java | 211 | 출하대상 파싱 시 GI_L_ID 추가 |
| ProgressDlgShipSearch.java | 308, 328 | 동기화 비교 시 GI_D_ID+GI_L_ID 조합 |
| ProgressDlgGoodsWetSearch.java | 101 | 서버 계근 데이터 파싱 시 GI_L_ID 추가 |

#### BixolonShipmentActivity.java

|  줄   | 용도                                               |
| :--: | ------------------------------------------------ |
| 880  | GI_D_ID로 행 선택 → GI_L_ID 비교 추가                    |
| 1272 | duplicatequeryGoodsWet 호출 → GI_L_ID 파라미터 추가      |
| 1552 | gi.setGI_D_ID → gi.setGI_L_ID 추가                 |
| 2189 | selectqueryListGoodsWetInfo 호출 → GI_L_ID 파라미터 추가 |
| 2380 | 동일 (수기입력)                                        |
| 2472 | duplicatequeryGoodsWet 호출 → GI_L_ID 파라미터 추가      |
| 2635 | 전송 후 arSM 매칭 → GI_L_ID 비교 추가                     |
| 2662 | updatequeryShipment 호출 → GI_L_ID 파라미터 추가         |
| 2763 | 전송 후 arSM 매칭 (생산/도매) → GI_L_ID 비교 추가             |
| 2795 | updatequeryShipment 호출 (생산/도매) → GI_L_ID 파라미터 추가 |
| 3238 | selectqueryGoodsWet 호출 → GI_L_ID 파라미터 추가         |

#### ShipmentActivity.java (동일 패턴)

| 줄 | 용도 |
|:--:|------|
| 806 | GI_D_ID로 행 선택 |
| 1116 | duplicatequeryGoodsWet 호출 |
| 1396 | gi.setGI_D_ID |
| 3239, 3430 | selectqueryListGoodsWetInfo 호출 |
| 3522 | duplicatequeryGoodsWet 호출 |
| 3686, 3814 | 전송 후 arSM 매칭 |
| 3713, 3846 | updatequeryShipment 호출 |
| 4261 | selectqueryGoodsWet 호출 |

---

### B그룹: GI_L_ID 불필요 (기존 GI_D_ID로 충분)

| 파일 | 줄 | 용도 | 이유 |
|------|:--:|------|------|
| BixolonShipment | 1268 | 로그 출력 | 표시용 |
| BixolonShipment | 2572-2574 | 전송 WHERE 조건 | 넓게 조회 OK |
| BixolonShipment | 2590, 2696 | packet GI_D_ID 유지 | SM_출고계근.출고상세SEQ (D.SEQ 유지, C그룹에서 GI_L_ID 추가) |
| DBHandler | 381-395 | selectqueryAllShipment | 존재 여부만 체크 |
| DBHandler | 426-427 | selectqueryShipmentForPopup | ITEM_CODE 기준 GROUP BY |
| DBHandler | 623-636 | selectqueryGIDIDList | 서버 WHERE 조건용 |
| DBHandler | 509-523 | selectqueryGoodsWetForPopup | 표시용 |

---

### B-1그룹: GI_L_ID 추가 권장 (WHERE에 GI_D_ID 포함, BARCODE+BOX_CNT로 사실상 특정 가능하나 안전을 위해 추가 권장)

| 파일 | 줄 | 메서드 | 현재 WHERE | 비고 |
|------|:--:|--------|-----------|------|
| DBHandler | 1710 | updatequeryGoodsWet() | GI_D_ID + BARCODE + BOX_CNT | 전송 성공 시 SAVE_TYPE='Y' UPDATE |
| DBHandler | 1739 | deletequerySelectGoodsWet() | BARCODE + GI_D_ID + BOX_CNT + SAVE_TYPE | 계근 선택 삭제 |
| BixolonShipment | 2629, 2758 | updatequeryGoodsWet 호출 | 상동 | |
| BixolonShipment | 3278 | deletequerySelectGoodsWet 호출 | 상동 | |

---

### C그룹: GI_L_ID 신규 추가 (서버 전송 패킷)

| 파일 | 줄 | 용도 |
|------|:--:|------|
| BixolonShipment | 2590 | 이마트/홈플러스/롯데 packet에 GI_L_ID 추가 |
| BixolonShipment | 2696 | 생산/도매/비정량 packet에 GI_L_ID 추가 |
| insert_goods_wet.jsp | - | SM_출고계근 INSERT에 `출고LOTSEQ` 컬럼 추가 |
| ShipmentActivity | 3640, 3747 | 동일 패턴 |

---

## 변경 범위 요약

| 대상 | A그룹 | B그룹 | B-1그룹 | C그룹 |
|------|:-----:|:-----:|:-------:|:-----:|
| MSSQL 테이블 | 1 (ALTER TABLE SM_출고계근) | - | - | - |
| JSP | 1 (search_shipment.jsp SELECT 추가) | - | - | 1 (insert_goods_wet.jsp INSERT 추가) |
| DBInfo.java | 1 | - | - | - |
| Shipments_Info.java | 1 | - | - | - |
| Goodswets_Info.java | 1 | - | - | - |
| DBHandler.java | 14 | 7 | 2 | - |
| ProgressDlgShipSearch.java | 3 | - | - | - |
| ProgressDlgGoodsWetSearch.java | 1 | - | - | - |
| BixolonShipmentActivity.java | 11 | 3 | 3 | 2 |
| ShipmentActivity.java | 8 | 2 | 2 | 2 |

**A그룹(수정 필요): 약 41곳**
**B-1그룹(추가 권장): 약 7곳**
**C그룹(신규 추가): 약 5곳**

---

## 사이드이펙트 조사 결과

### GI_L_ID 단독 교체 시 치명적 사이드이펙트 (3건)

|  #  | 항목                                | 문제                                         | 심각도 |
| :-: | --------------------------------- | ------------------------------------------ | :-: |
|  1  | 서버 전송 packet (splitData[0])       | SM_출고계근.출고상세SEQ에 GI_L_ID가 들어감 → 데이터 정합성 파괴 | 치명적 |
|  2  | search_goods_wet.jsp              | `WHERE 출고상세SEQ = GI_L_ID` → 조회 결과 0건       | 치명적 |
|  3  | completeStr (update_shipment.jsp) | `WHERE GI_D_ID = GI_L_ID` → UPDATE 불가      | 치명적 |

### GI_D_ID + GI_L_ID 조합 시 사이드이펙트 (0건)

| 항목 | GI_L_ID 단독 | GI_D_ID + GI_L_ID |
|------|:----------:|:----------------:|
| 서버 전송 packet (출고상세SEQ) | 치명적 | **없음** (GI_D_ID 유지) |
| search_goods_wet.jsp WHERE | 치명적 | **없음** (GI_D_ID 유지) |
| completeStr (update_shipment.jsp) | 치명적 | **없음** (GI_D_ID 유지) |
| selectquerySendGoodsWet WHERE | 깨짐 | **없음** (GI_D_ID 유지) |
| 계근수량 분리 (핵심 문제) | 해결 | **해결** (GI_L_ID 추가) |
| 전송 후 arSM 매칭 | 깨짐 | **해결** (GI_D_ID + GI_L_ID) |
| 동기화 비교 | 깨짐 | **해결** (GI_D_ID + GI_L_ID) |

### 결론: GI_D_ID + GI_L_ID 조합 채택

```
GI_D_ID(기존 유지) = SM_출고상세.SEQ → 서버 인터페이스용
GI_L_ID(신규 추가) = SM_출고LOT.SEQ → LOT별 유니크 식별용
```

**A그룹 수정 방법:**
- WHERE 조건: `GI_D_ID` → `GI_D_ID AND GI_L_ID` (조합)
- equals 매칭: `getGI_D_ID().equals()` → `getGI_D_ID().equals() && getGI_L_ID().equals()` (조합)
- INSERT: GI_D_ID 유지 + GI_L_ID 컬럼/값 추가

**C그룹 (서버 전송용):**
- packet에서 GI_D_ID 유지 (SM_출고계근.출고상세SEQ)
- packet에 GI_L_ID 추가 (SM_출고계근.`출고LOTSEQ` 신규)

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/오류/15_GI_D_ID_LOT별_미분리_계근수량_중복표시.md`
- `app/doc/소스분석/37_로컬DB_MSSQL전환_현황.md`
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`
