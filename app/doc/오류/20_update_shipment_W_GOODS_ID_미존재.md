# update_shipment.jsp W_GOODS_ID 테이블 미존재

## 발견일
2026-04-07

## 에러 발생 시나리오

```
1. 바코드 스캔 → 계근 완료 → 전송 버튼 클릭
2. insert_goods_wet.jsp → SM_출고계근 INSERT 성공
3. 모든 박스 전송 완료 → update_shipment.jsp 호출
4. UPDATE W_GOODS_ID SET CHECK_YN='N' WHERE GI_D_ID=? → 실행
5. W_GOODS_ID 테이블이 MSSQL에 존재하지 않음
6. SQLServerException: 개체 이름 'W_GOODS_ID'이(가) 잘못되었습니다.
```

---

## 현상
- SM_출고계근 INSERT는 성공
- 전송 완료 후 출하대상 상태 변경 시 에러 발생
- Tomcat 로그: `개체 이름 'W_GOODS_ID'이(가) 잘못되었습니다.`

## 원래부터 있던 버그인가?

**NO - Oracle→MSSQL 전환에서 W_GOODS_ID 테이블이 미전환**

- 원본 Oracle: W_GOODS_ID 테이블 존재 → CHECK_YN UPDATE 정상
- 현재 MSSQL: W_GOODS_ID 테이블 없음 → SM_출고상세.계근여부로 대체 필요

## 원인

### update_shipment.jsp (55줄)

```sql
UPDATE W_GOODS_ID SET CHECK_YN='N', MOD_ID=?, MOD_DATE=?, MOD_TIME=?
WHERE GI_D_ID=? AND ITEM_CODE=? AND BRAND_CODE=?
```

- W_GOODS_ID: Oracle 시절 테이블 (MSSQL에 미존재)
- 용도: 계근 완료 후 출하대상 상태를 'N'으로 변경

### 대체 대상

SM_출고상세 테이블의 `계근여부` 컬럼:

```java
// DlivyDetailEntity.java:301
@Column(name = "계근여부", columnDefinition = "VARCHAR(1) DEFAULT 'N'")
private String weighYn;
```

## 영향 범위
- update_shipment.jsp: 55줄 (SQL 전체 변경 필요)
- PDA completeStr 구성: BixolonShipmentActivity 2643줄, 2772줄

## 수정 방안

### update_shipment.jsp 호출 제거

계근 완료 여부를 UPDATE하지 않고, **SM_출고계근 ROW COUNT = SM_출고상세.출고박스수량** 비교로 판단.
- ERP에서 `계근여부` 컬럼을 사용하는 비즈니스 로직 없음 (Entity 정의만 존재)
- update_shipment.jsp 호출 자체를 PDA에서 제거
- completeStr 생성 + URL_UPDATE_SHIPMENT 호출 코드 제거

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`
- `app/doc/소스분석/37_로컬DB_MSSQL전환_현황.md`
