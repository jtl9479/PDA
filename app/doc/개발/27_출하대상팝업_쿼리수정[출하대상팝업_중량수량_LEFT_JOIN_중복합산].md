# 출하대상 팝업 쿼리 수정 (LEFT JOIN 중복 합산 수정)

**작성일**: 2026-04-07
**목적**: selectqueryShipmentForPopup() 쿼리에서 LEFT JOIN으로 인한 중량/수량 중복 합산 수정
**관련 오류**: `app/doc/오류/12_출하대상팝업_중량수량_LEFT_JOIN_중복합산.md`

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### DBHandler.java selectqueryShipmentForPopup() (420~432줄)

```sql
SELECT S.ITEM_CODE, S.ITEM_NAME,
       SUM(CAST(S.GI_REQ_QTY AS REAL)) AS GI_REQ_QTY,
       SUM(CAST(S.GI_REQ_PKG AS REAL)) AS GI_REQ_PKG,
       COUNT(W.GI_D_ID) AS WET_CNT
FROM TB_SHIPMENT S
LEFT JOIN TB_GOODS_WET W ON S.GI_D_ID = W.GI_D_ID
GROUP BY S.ITEM_CODE, S.ITEM_NAME
ORDER BY S.ITEM_CODE ASC
```

### 문제점

- LEFT JOIN으로 TB_GOODS_WET 계근 건수만큼 TB_SHIPMENT 행이 중복
- SUM(중량), SUM(수량)이 계근 건수배로 뻥튀기
- 예: 중량=2, 계근 3건 → SUM=6 (정상: 2)

---

## 2. 변경 구조

```sql
SELECT S.ITEM_CODE, S.ITEM_NAME,
       SUM(CAST(S.GI_REQ_QTY AS REAL)) AS GI_REQ_QTY,
       SUM(CAST(S.GI_REQ_PKG AS REAL)) AS GI_REQ_PKG,
       (SELECT COUNT(*) FROM TB_GOODS_WET W 
        WHERE W.GI_D_ID IN (SELECT GI_D_ID FROM TB_SHIPMENT 
                             WHERE ITEM_CODE = S.ITEM_CODE)) AS WET_CNT
FROM TB_SHIPMENT S
GROUP BY S.ITEM_CODE, S.ITEM_NAME
ORDER BY S.ITEM_CODE ASC
```

- LEFT JOIN 제거 → TB_SHIPMENT 행 중복 없음 → SUM 정확
- 서브쿼리로 계근수량 별도 조회 → COUNT 정확

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **DBHandler.java** | selectqueryShipmentForPopup() 420~432줄 | LEFT JOIN → 서브쿼리 변경 |

---

## 4. 수정 상세

### 4.1 DBHandler.java

**경로**: `app/src/main/java/.../db/DBHandler.java`

**변경 전:**

```java
String sqlStr = "SELECT "
        + "S." + DBInfo.ITEM_CODE + ", "
        + "S." + DBInfo.ITEM_NAME + ", "
        + "SUM(CAST(S." + DBInfo.GI_REQ_QTY + " AS REAL)) AS GI_REQ_QTY, "
        + "SUM(CAST(S." + DBInfo.GI_REQ_PKG + " AS REAL)) AS GI_REQ_PKG, "
        + "COUNT(W." + DBInfo.GI_D_ID + ") AS WET_CNT"
        + " FROM "
        + DBInfo.TABLE_NAME_SHIPMENT + " S"
        + " LEFT JOIN " + DBInfo.TABLE_NAME_GOODS_WET + " W"
        + " ON S." + DBInfo.GI_D_ID + " = W." + DBInfo.GI_D_ID
        + " GROUP BY S." + DBInfo.ITEM_CODE
        + ", S." + DBInfo.ITEM_NAME
        + " ORDER BY S." + DBInfo.ITEM_CODE + " ASC";
```

**변경 후:**

```java
String sqlStr = "SELECT "
        + "S." + DBInfo.ITEM_CODE + ", "
        + "S." + DBInfo.ITEM_NAME + ", "
        + "SUM(CAST(S." + DBInfo.GI_REQ_QTY + " AS REAL)) AS GI_REQ_QTY, "
        + "SUM(CAST(S." + DBInfo.GI_REQ_PKG + " AS REAL)) AS GI_REQ_PKG, "
        + "(SELECT COUNT(*) FROM " + DBInfo.TABLE_NAME_GOODS_WET + " W"
        + " WHERE W." + DBInfo.GI_D_ID + " IN"
        + " (SELECT " + DBInfo.GI_D_ID + " FROM " + DBInfo.TABLE_NAME_SHIPMENT
        + " WHERE " + DBInfo.ITEM_CODE + " = S." + DBInfo.ITEM_CODE + ")) AS WET_CNT"
        + " FROM "
        + DBInfo.TABLE_NAME_SHIPMENT + " S"
        + " GROUP BY S." + DBInfo.ITEM_CODE
        + ", S." + DBInfo.ITEM_NAME
        + " ORDER BY S." + DBInfo.ITEM_CODE + " ASC";
```

---

## 5. 사이드이펙트

- selectqueryShipmentForPopup()만 수정 (팝업 전용 메서드)
- 다른 SELECT 메서드 변경 없음
- MainActivity.showShipmentListDialog() 변경 없음 (cursor 매핑 동일)

---

## 6. 개발 플랜

### Step 1: LEFT JOIN → 서브쿼리 변경

**Part 1. 분석**
- 대상: DBHandler.java selectqueryShipmentForPopup() 420~432줄
- 범위: SQL 쿼리만 변경 (cursor 매핑은 동일)
- 주의할 점: cursor.getColumnIndex("WET_CNT") 별칭 유지

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 수행
- [ ] Part 3: 컴파일 확인
- [ ] Part 4: 출하대상 팝업에서 중량/수량이 정확한 값으로 표시되는지 확인

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### 개발 순서 요약

```
Step 1: LEFT JOIN → 서브쿼리 변경
```

---

## 7. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | LEFT JOIN → 서브쿼리 변경 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/오류/12_출하대상팝업_중량수량_LEFT_JOIN_중복합산.md`
- `app/doc/개발/24_출하대상목록_팝업_추가.md`

---

**문서 버전**: 1.0
