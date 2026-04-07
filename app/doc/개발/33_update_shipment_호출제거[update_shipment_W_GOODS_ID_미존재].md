# update_shipment.jsp 호출 제거

**작성일**: 2026-04-07
**목적**: W_GOODS_ID 미존재로 인한 update_shipment.jsp 호출을 제거. 계근 완료 여부는 SM_출고계근 ROW COUNT로 판단.
**관련 오류**: `app/doc/오류/20_update_shipment_W_GOODS_ID_미존재.md`

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### 전송 완료 후 흐름 (BixolonShipmentActivity)

```java
// 2643줄 (이마트/홈플러스/롯데 구로직)
String completeStr = arSM.get(j).getGI_D_ID() + "::" + arSM.get(j).getITEM_CODE() 
    + "::" + arSM.get(j).getBRAND_CODE() + "::" + Common.REG_ID;
receiveData = HttpHelper.getInstance().sendDataDb(completeStr, "inno", 
    "complete_shipment", Common.URL_UPDATE_SHIPMENT);
```

```java
// 2772줄 (생산/도매/비정량 신로직)
String completeStr = arSM.get(j).getGI_D_ID() + "::" + arSM.get(j).getITEM_CODE() 
    + "::" + arSM.get(j).getBRAND_CODE() + "::" + Common.REG_ID;
receiveData = HttpHelper.getInstance().sendDataDb(completeStr, "inno", 
    "complete_shipment", Common.URL_UPDATE_SHIPMENT);
```

### update_shipment.jsp (55줄)

```sql
UPDATE W_GOODS_ID SET CHECK_YN='N', MOD_ID=?, MOD_DATE=?, MOD_TIME=? 
WHERE GI_D_ID=? AND ITEM_CODE=? AND BRAND_CODE=?
```

### 문제점

- W_GOODS_ID 테이블이 MSSQL에 미존재 → 에러
- ERP에서 계근여부 컬럼을 사용하는 비즈니스 로직 없음
- 계근 완료 여부는 SM_출고계근 ROW COUNT = SM_출고상세.출고박스수량 비교로 판단 가능

---

## 2. 변경 구조

```
[변경 전]
모든 박스 전송 완료 → completeStr 생성 → update_shipment.jsp 호출 → W_GOODS_ID UPDATE → 에러

[변경 후]
모든 박스 전송 완료 → update_shipment.jsp 호출 제거 → 에러 없음
계근 완료 판단: SM_출고계근 COUNT vs SM_출고상세.출고박스수량 (ERP에서 비교)
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **BixolonShipmentActivity.java** | 2643~2665줄 | completeStr + URL_UPDATE_SHIPMENT 호출 제거 (이마트/홈플러스/롯데) |
| 2 | **BixolonShipmentActivity.java** | 2772~2800줄 | completeStr + URL_UPDATE_SHIPMENT 호출 제거 (생산/도매/비정량) |

---

## 4. 개발 플랜

### Step 1: update_shipment.jsp 호출 제거

**Part 1. 분석**
- 대상: BixolonShipmentActivity.java
- 범위: 전송 완료 후 completeStr 생성 및 URL_UPDATE_SHIPMENT 호출 부분 2곳
- 주의할 점: completeStr 이후의 결과 처리(receiveData 체크, SAVE_TYPE 변경, updatequeryShipment) 로직 확인

**체크리스트**
- [ ] 이마트/홈플러스/롯데 구로직 completeStr + 호출 제거 (2643~2665줄)
- [ ] 생산/도매/비정량 신로직 completeStr + 호출 제거 (2772~2800줄)
- [ ] 제거 후 SAVE_TYPE 변경, updatequeryShipment 등 후속 로직이 영향받는지 확인
- [ ] 컴파일 확인
- [ ] 전송 테스트 → 에러 없이 전송 완료 확인

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

## 5. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | update_shipment.jsp 호출 제거 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/오류/20_update_shipment_W_GOODS_ID_미존재.md`
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`

---

**문서 버전**: 1.0
