# GI_D_ID LOT별 미분리로 계근수량 중복 표시

## 발견일
2026-04-07

## 현상
- 같은 품목에 LOT가 2개인 경우, 1건만 계근해도 **모든 BL 행에 계근수량이 동일하게 증가**
- 뒤로가기 후 다시 바코드 스캔 시 확인됨
- 계근데이터(TB_GOODS_WET)는 1건인데, 화면에서는 2개 행 모두 계근수량=1로 표시

## 원래부터 있던 버그인가?

**NO - Oracle→MSSQL 전환에서 발생한 구조 차이**

- 원본 Oracle: GI_D_ID가 LOT별로 고유 (W_GOODS_ID 기준)
- 현재 MSSQL: GI_D_ID = SM_출고상세.SEQ → 1개 출고상세에 SM_출고LOT가 여러 개 → **GI_D_ID가 LOT별로 동일**

## 원인

### 문제 1 (주요): 같은 출고상세에 LOT가 여러 개일 때 GI_D_ID가 동일

#### search_shipment.jsp에서 조회

```sql
SELECT D.SEQ AS GI_D_ID, ...
FROM SM_출고상세 D
    JOIN SM_출고LOT L ON D.SEQ = L.출고상세SEQ
```

SM_출고LOT가 2건이면 GI_D_ID(=D.SEQ)가 동일한 2행이 나옴:

```
TB_SHIPMENT 저장 결과:
행1: GI_D_ID=1330, BL=202601270001, 수량=2
행2: GI_D_ID=1330, BL=202601270002, 수량=1
         ↑ 동일
```

#### selectqueryListGoodsWetInfo()에서 계근수량 조회

**DBHandler.java:1369~1410**

```sql
SELECT sum(WEIGHT) as WEIGHT, count(GI_D_ID) as COUNT, ...
FROM TB_GOODS_WET
WHERE GI_D_ID = '1330' AND PACKER_PRODUCT_CODE = 'PC001'
```

두 행 모두 GI_D_ID=1330이므로 **같은 계근수량이 조회됨**

#### ProgressDlgShipSelect.doInBackground()에서 각 행에 세팅

**BixolonShipmentActivity.java:2187~2192**

```java
for (int i = 0; i < arSM.size(); i++) {
    row = DBHandler.selectqueryListGoodsWetInfo(mContext, 
        arSM.get(i).getGI_D_ID(),           // 행1: 1330, 행2: 1330 ← 동일
        arSM.get(i).getPACKER_PRODUCT_CODE(),
        arSM.get(i).getCLIENT_CODE());
    arSM.get(i).setPACKING_QTY(Integer.parseInt(row[1]));  // 두 행 모두 같은 값
}
```

### 문제 2 (연쇄): 계근 완료 판별 오류

GI_D_ID가 동일하므로 한쪽 BL 계근이 완료되면 다른 BL도 완료로 판별될 수 있음

### Oracle vs MSSQL 구조 차이

```
[Oracle 원본]
W_GOODS_ID (출고상세) 1건 → GI_D_ID 1개 → LOT 개념 없음
→ GI_D_ID가 LOT별로 고유

[MSSQL 현재]
SM_출고상세 1건 (SEQ=1330)
  → SM_출고LOT 2건 (LOT1, LOT2)
  → GI_D_ID=1330이 2행으로 조회
→ GI_D_ID가 LOT별로 동일
```

## 영향 범위

- 계근수량/중량 표시 (센터 총수량, 지점 수량)
- 계근 완료 판별 (전송 버튼 활성화 조건)
- 전송 시 전송 개수 비교
- 같은 품목에 LOT가 여러 개인 경우에만 발생

## 수정 방안

### 방안 A: JSP에서 LOT별 고유 ID 생성

search_shipment.jsp에서 GI_D_ID를 `D.SEQ + '_' + L.SEQ` 등으로 LOT별 고유값 생성

### 방안 B: TB_GOODS_WET에 BL_NO 컬럼 추가

계근 시 BL_NO도 함께 저장하고, selectqueryListGoodsWetInfo()에서 BL_NO 조건 추가

### 방안 C: GI_D_ID를 SM_출고LOT.SEQ로 변경

search_shipment.jsp에서 `L.SEQ AS GI_D_ID`로 변경하여 LOT별 고유 ID 사용

> 방안 선택 시 서버 전송(insert_goods_wet.jsp)의 출고상세SEQ 매핑도 함께 검토 필요

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`
- `app/doc/소스분석/37_로컬DB_MSSQL전환_현황.md`
