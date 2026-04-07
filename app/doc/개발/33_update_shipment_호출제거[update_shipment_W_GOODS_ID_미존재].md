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

## 4. 영향 분석

### 현재 코드 구조 (이마트/홈플러스/롯데 구로직, 2642~2682줄)

```java
if (SAVE_CNT == GI_REQ_PKG) {                          // 모든 박스 전송 완료
    String completeStr = GI_D_ID + "::" + ...;          // ← 제거 대상
    receiveData = sendDataDb(completeStr, URL_UPDATE_SHIPMENT); // ← 제거 대상
    
    if (receiveData.equals("s")) {                      // ← 서버 응답 "s"일 때만 실행
        arSM.get(j).setSAVE_TYPE("Y");                  // ★ PDA 전송상태 변경
        DBHandler.updatequeryShipment(...);              // ★ TB_SHIPMENT UPDATE
        jChk++;
        if (jChk == arSM.size()) return "ss";           // ★ 전체 완료
    }
}
```

### 문제점

completeStr + 서버 호출을 단순 제거하면 `receiveData`가 "s"가 아니게 되어 **후속 로직 3가지가 전부 실행되지 않음:**
- arSM.setSAVE_TYPE("Y") 미실행
- DBHandler.updatequeryShipment() 미실행
- jChk++ → return "ss" 미실행 → 완료 다이얼로그 안 뜸

### 수정 방법

completeStr + 서버 호출 + `if (receiveData.equals("s"))` 체크를 제거하고, **후속 로직을 바로 실행**

```java
if (SAVE_CNT == GI_REQ_PKG) {
    // completeStr + URL_UPDATE_SHIPMENT 호출 제거
    // receiveData 체크 제거
    // 후속 로직 바로 실행
    arSM.get(j).setSAVE_TYPE("Y");
    DBHandler.updatequeryShipment(...);
    jChk++;
    if (jChk == arSM.size()) return "ss";
}
```

---

## 5. 개발 플랜

### Step 1: update_shipment.jsp 호출 제거 + 후속 로직 유지

**Part 1. 분석**
- 대상: BixolonShipmentActivity.java
- 범위: 이마트 구로직(2642~2682줄), 생산/도매 신로직(동일 패턴)
- 핵심: completeStr 생성 + 서버 호출 + receiveData 체크 제거, SAVE_TYPE 변경 + updatequeryShipment + jChk 유지

**체크리스트**
- [ ] 이마트/홈플러스/롯데 구로직: completeStr + sendDataDb + receiveData 체크 제거, 후속 로직 유지 (2642~2682줄)
- [ ] 생산/도매/비정량 신로직: 동일 패턴 수정
- [ ] 컴파일 확인
- [ ] 전송 테스트 → 에러 없이 전송 완료 + 완료 다이얼로그 표시 확인

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
