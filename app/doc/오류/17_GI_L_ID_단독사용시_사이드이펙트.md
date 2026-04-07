# GI_L_ID 단독 사용 시 사이드이펙트

## 발견일
2026-04-07

## 목적
GI_D_ID를 GI_L_ID로 단독 교체했을 경우 발생하는 사이드이펙트 정리
→ **GI_D_ID + GI_L_ID 조합 채택의 근거 문서**

---

## 전제
- GI_D_ID = SM_출고상세.SEQ (기존)
- GI_L_ID = SM_출고LOT.SEQ (신규)
- "단독 사용" = GI_D_ID를 제거하고 GI_L_ID만 사용

---

## 치명적 사이드이펙트 (3건)

### 1. 서버 전송 packet - SM_출고계근.출고상세SEQ 데이터 정합성 파괴

**현재 코드**: `BixolonShipmentActivity.java:2590`
```java
packet += list_send_info.get(i).getGI_D_ID() + "::";  // splitData[0]
```

**JSP**: `insert_goods_wet.jsp:71`
```java
pstmt.setInt(1, Integer.parseInt(splitData[0]));  // SM_출고계근.출고상세SEQ
```

**GI_L_ID 단독 시**: splitData[0]에 출고LOT SEQ가 들어감 → SM_출고계근.출고상세SEQ에 **잘못된 값 저장** → ERP 데이터 정합성 파괴

---

### 2. search_goods_wet.jsp - 계근 데이터 조회 불가

**현재 코드**: `ProgressDlgGoodsWetSearch.java:62`
```java
data = data + "출고상세SEQ = '" + list_id_info.get(i).toString() + "'";
```

**JSP**: `search_goods_wet.jsp:50`
```sql
SELECT ... FROM SM_출고계근 WHERE 출고상세SEQ = ?
```

**GI_L_ID 단독 시**: `출고상세SEQ = 출고LOT SEQ값` → 조회 결과 **0건**

---

### 3. completeStr - 출하대상 완료 UPDATE 불가

**현재 코드**: `BixolonShipmentActivity.java:2639`
```java
String completeStr = arSM.get(j).getGI_D_ID() + "::" + ...
```

**JSP**: `update_shipment.jsp`
```sql
UPDATE W_GOODS_ID SET CHECK_YN='N' WHERE GI_D_ID = ?
```

**GI_L_ID 단독 시**: `WHERE GI_D_ID = 출고LOT SEQ값` → UPDATE **0건**

---

## 높은 사이드이펙트 (2건)

### 4. selectquerySendGoodsWet WHERE 조건 - 전송 대상 조회 불가

**현재 코드**: `BixolonShipmentActivity.java:2572`
```java
qry_where = qry_where + "GI_D_ID = " + arSM.get(i).getGI_D_ID();
```

**DBHandler.java:1326**
```sql
SELECT ... FROM TB_GOODS_WET WHERE [qry_where]
```

**GI_L_ID 단독 시**: TB_GOODS_WET에 GI_D_ID가 없으면 → WHERE GI_D_ID = ? **컬럼 없음 에러** 또는 조회 결과 0건

---

### 5. 전송 후 arSM 매칭 - 잘못된 대상 매칭

**현재 코드**: `BixolonShipmentActivity.java:2635`
```java
if (arSM.get(j).getGI_D_ID().equals(list_send_info.get(i).getGI_D_ID()))
```

**GI_L_ID 단독 시**: arSM에 GI_D_ID가 없거나, list_send_info에 GI_D_ID가 없으면 → 매칭 실패 → SAVE_CNT 미증가 → 전송 완료 판별 불가

---

## 중간 사이드이펙트 (1건)

### 6. ProgressDlgGoodsWetSearch - 서버 계근 데이터 조회 불가

**현재 코드**: `ProgressDlgGoodsWetSearch.java:58`
```java
list_id_info = DBHandler.selectqueryGIDIDList(mContext);
```

**DBHandler.java:622**
```sql
SELECT DISTINCT GI_D_ID FROM TB_SHIPMENT
```

**GI_L_ID 단독 시**: TB_SHIPMENT에서 GI_D_ID를 SELECT → 서버에 `출고상세SEQ = ?`로 전달 → 정상
**하지만** GI_D_ID를 TB_SHIPMENT에서 제거하면 → 이 조회 자체가 **불가**

---

## 요약

| # | 항목 | 심각도 | 근본 원인 |
|:-:|------|:------:|----------|
| 1 | 서버 전송 packet | **치명적** | SM_출고계근.출고상세SEQ에 잘못된 값 |
| 2 | search_goods_wet.jsp | **치명적** | WHERE 출고상세SEQ 조회 불가 |
| 3 | completeStr update_shipment.jsp | **치명적** | WHERE GI_D_ID UPDATE 불가 |
| 4 | selectquerySendGoodsWet WHERE | **높음** | 전송 대상 조회 불가 |
| 5 | 전송 후 arSM 매칭 | **높음** | 매칭 실패 → 전송 완료 미판별 |
| 6 | ProgressDlgGoodsWetSearch | **중간** | 서버 계근 데이터 조회 불가 |

**결론**: GI_D_ID는 서버 인터페이스(SM_출고계근.출고상세SEQ, W_GOODS_ID.GI_D_ID, search_goods_wet.jsp)와 직결되므로 **절대 제거 불가**. GI_D_ID + GI_L_ID 조합이 유일한 안전한 방안.

---

## 관련 문서
- `app/doc/오류/16_GI_D_ID_비유니크_전체_영향범위.md`
- `app/doc/개발/31_GI_L_ID_추가[GI_D_ID_비유니크_전체_영향범위].md`
