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

GI_D_ID는 기존대로 유지하고, **LOT_SEQ(SM_출고LOT.SEQ)를 추가 컬럼으로 신규 추가**
- search_shipment.jsp에서 L.SEQ AS LOT_SEQ 추가
- SM_출고계근 INSERT 시 출고LOTSEQ 컬럼 추가

---

## GI_D_ID 사용처 전수 확인 (LOT_SEQ 추가 필요 여부)

### A그룹: LOT_SEQ 추가 필요 (GI_D_ID로 유니크 식별하는 곳)

#### 모델/상수

| 파일 | 줄 | 내용 |
|------|:--:|------|
| DBInfo.java | 11 | LOT_SEQ 상수 추가 필요 |
| Shipments_Info.java | 6, 75-81 | LOT_SEQ 필드/getter/setter 추가 |
| Goodswets_Info.java | 6, 38-43 | LOT_SEQ 필드/getter/setter 추가 |

#### DB 테이블 CREATE

| 파일 | 줄 | 테이블 |
|------|:--:|--------|
| DBHandler.java | 35 | TB_SHIPMENT CREATE - LOT_SEQ 컬럼 추가 |
| DBHandler.java | 1189 | TB_GOODS_WET CREATE - LOT_SEQ 컬럼 추가 |

#### DB SELECT (LOT_SEQ 조건 추가 필요)

| 파일 | 줄 | 메서드 | 현재 WHERE |
|------|:--:|--------|-----------|
| DBHandler.java | 1253 | selectqueryGoodsWet() | GI_D_ID + PP_CODE |
| DBHandler.java | 1385 | selectqueryListGoodsWetInfo() | GI_D_ID + PP_CODE |
| DBHandler.java | 1459 | duplicatequeryGoodsWet() | GI_D_ID + PP_CODE + BARCODE |

#### DB INSERT (LOT_SEQ 컬럼/값 추가 필요)

| 파일 | 줄 | 메서드 |
|------|:--:|--------|
| DBHandler.java | 694 | insertqueryShipment() |
| DBHandler.java | 1496 | insertqueryGoodsWet() |
| DBHandler.java | 1566 | insertqueryGoodsWetHomeplus() |
| DBHandler.java | 1639 | insertqueryGoodsWetLotte() |

#### DB UPDATE (LOT_SEQ 조건 추가 필요)

| 파일 | 줄 | 메서드 | 현재 WHERE |
|------|:--:|--------|-----------|
| DBHandler.java | 811 | updatequeryShipment() | GI_D_ID + PP_CODE |

#### DB DELETE (LOT_SEQ 조건 추가 필요)

| 파일 | 줄 | 메서드 |
|------|:--:|--------|
| DBHandler.java | 1880 | refreshShipmentList() |

#### 파싱

| 파일 | 줄 | 용도 |
|------|:--:|------|
| ProgressDlgShipSearch.java | 211 | 출하대상 파싱 시 LOT_SEQ 추가 |
| ProgressDlgShipSearch.java | 308, 328 | 동기화 비교 시 GI_D_ID+LOT_SEQ 조합 |
| ProgressDlgGoodsWetSearch.java | 101 | 서버 계근 데이터 파싱 시 LOT_SEQ 추가 |

#### BixolonShipmentActivity.java

|  줄   | 용도                                               |
| :--: | ------------------------------------------------ |
| 880  | GI_D_ID로 행 선택 → LOT_SEQ 비교 추가                    |
| 1272 | duplicatequeryGoodsWet 호출 → LOT_SEQ 파라미터 추가      |
| 1552 | gi.setGI_D_ID → gi.setLOT_SEQ 추가                 |
| 2189 | selectqueryListGoodsWetInfo 호출 → LOT_SEQ 파라미터 추가 |
| 2380 | 동일 (수기입력)                                        |
| 2472 | duplicatequeryGoodsWet 호출 → LOT_SEQ 파라미터 추가      |
| 2635 | 전송 후 arSM 매칭 → LOT_SEQ 비교 추가                     |
| 2662 | updatequeryShipment 호출 → LOT_SEQ 파라미터 추가         |
| 2763 | 전송 후 arSM 매칭 (생산/도매) → LOT_SEQ 비교 추가             |
| 2795 | updatequeryShipment 호출 (생산/도매) → LOT_SEQ 파라미터 추가 |
| 3238 | selectqueryGoodsWet 호출 → LOT_SEQ 파라미터 추가         |

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

### B그룹: LOT_SEQ 불필요 (기존 GI_D_ID로 충분)

| 파일 | 줄 | 용도 | 이유 |
|------|:--:|------|------|
| BixolonShipment | 1268 | 로그 출력 | 표시용 |
| BixolonShipment | 2572-2574 | 전송 WHERE 조건 | 넓게 조회 OK |
| BixolonShipment | 2590, 2696 | packet GI_D_ID | SM_출고계근.출고상세SEQ (D.SEQ 유지) |
| DBHandler | 381-395 | selectqueryAllShipment | 존재 여부만 체크 |
| DBHandler | 426-427 | selectqueryShipmentForPopup | ITEM_CODE 기준 GROUP BY |
| DBHandler | 623-636 | selectqueryGIDIDList | 서버 WHERE 조건용 |
| DBHandler | 509-523 | selectqueryGoodsWetForPopup | 표시용 |

---

### B-1그룹: LOT_SEQ 추가 권장 (WHERE에 GI_D_ID 포함, BARCODE+BOX_CNT로 사실상 특정 가능하나 안전을 위해 추가 권장)

| 파일 | 줄 | 메서드 | 현재 WHERE | 비고 |
|------|:--:|--------|-----------|------|
| DBHandler | 1710 | updatequeryGoodsWet() | GI_D_ID + BARCODE + BOX_CNT | 전송 성공 시 SAVE_TYPE='Y' UPDATE |
| DBHandler | 1739 | deletequerySelectGoodsWet() | BARCODE + GI_D_ID + BOX_CNT + SAVE_TYPE | 계근 선택 삭제 |
| BixolonShipment | 2629, 2758 | updatequeryGoodsWet 호출 | 상동 | |
| BixolonShipment | 3278 | deletequerySelectGoodsWet 호출 | 상동 | |

---

### C그룹: LOT_SEQ 신규 추가 (서버 전송 패킷)

| 파일 | 줄 | 용도 |
|------|:--:|------|
| BixolonShipment | 2590 | 이마트/홈플러스/롯데 packet에 LOT_SEQ 추가 |
| BixolonShipment | 2696 | 생산/도매/비정량 packet에 LOT_SEQ 추가 |
| insert_goods_wet.jsp | - | SM_출고계근 INSERT에 출고LOTSEQ 컬럼 추가 |
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

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/오류/15_GI_D_ID_LOT별_미분리_계근수량_중복표시.md`
- `app/doc/소스분석/37_로컬DB_MSSQL전환_현황.md`
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`
