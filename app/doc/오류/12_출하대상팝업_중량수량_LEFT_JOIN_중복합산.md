# 출하대상 팝업 중량/수량 LEFT JOIN 중복 합산

## 발견일
2026-04-07

## 현상
- 출하대상 팝업에서 중량/수량이 실제보다 크게 표시됨
- 예: 중량=2, 수량=2 이어야 하는데 6으로 표시
- 계근 데이터가 있는 품목에서만 발생

## 원래부터 있던 버그인가?

**NO - 개발24 Step 3에서 LEFT JOIN TB_GOODS_WET을 추가하면서 발생**

## 원인

### 문제 1 (주요): LEFT JOIN으로 인한 TB_SHIPMENT 행 중복

#### 코드 위치
- `DBHandler.java` : selectqueryShipmentForPopup() 420~432줄

#### 현재 문제 코드
```sql
SELECT S.ITEM_CODE, S.ITEM_NAME,
       SUM(CAST(S.GI_REQ_QTY AS REAL)) AS GI_REQ_QTY,   -- ★ JOIN 중복으로 뻥튀기
       SUM(CAST(S.GI_REQ_PKG AS REAL)) AS GI_REQ_PKG,   -- ★ JOIN 중복으로 뻥튀기
       COUNT(W.GI_D_ID) AS WET_CNT
FROM TB_SHIPMENT S
LEFT JOIN TB_GOODS_WET W ON S.GI_D_ID = W.GI_D_ID       -- ★ 계근 건수만큼 행 중복
GROUP BY S.ITEM_CODE, S.ITEM_NAME
```

#### 발생 시나리오
```
TB_SHIPMENT: 품목A, GI_D_ID=100, 중량=2, 수량=2 (1행)
TB_GOODS_WET: GI_D_ID=100에 대해 계근 3건

LEFT JOIN 결과:
  품목A | 중량=2 | 수량=2 | W.GI_D_ID=100  ← 1행
  품목A | 중량=2 | 수량=2 | W.GI_D_ID=100  ← 2행 (중복)
  품목A | 중량=2 | 수량=2 | W.GI_D_ID=100  ← 3행 (중복)

SUM(중량) = 2 + 2 + 2 = 6  ← 잘못된 값 (정상: 2)
SUM(수량) = 2 + 2 + 2 = 6  ← 잘못된 값 (정상: 2)
COUNT(W.GI_D_ID) = 3       ← 정상
```

## 영향 범위
- `DBHandler.java` : selectqueryShipmentForPopup() 420~432줄
- 출하대상 팝업 표시에서만 발생 (다른 기능에 영향 없음)

## 수정 방안

### 수정: LEFT JOIN 제거, 서브쿼리로 계근수량 조회

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

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/개발/24_출하대상목록_팝업_추가.md` - Step 3에서 LEFT JOIN 추가
