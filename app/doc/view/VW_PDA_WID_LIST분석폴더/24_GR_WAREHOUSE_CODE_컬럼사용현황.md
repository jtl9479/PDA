# GR_WAREHOUSE_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 입고창고코드
**파싱 위치**: - (앱에 전달 안 됨)

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | |
| 라벨출력 | |
| 로직분기 | |
| DB저장 | |
| 바코드검증 | |
| 조회조건 | ● |
| 앱미전달 | ● |

> **구분 기준**
> - **서버전송**: 서버에 데이터 전송 시 패킷에 포함
> - **화면표시**: 앱 화면(Activity)에 텍스트로 표시
> - **바코드생성**: 바코드 문자열 생성에 사용
> - **라벨출력**: 라벨 인쇄 시 텍스트/값으로 출력
> - **로직분기**: if/switch 등 조건 분기에 사용
> - **DB저장**: 로컬 SQLite(TB_SHIPMENT)에 저장
> - **바코드검증**: 스캔 바코드와 매칭 검증
> - **조회조건**: 서버/로컬 DB 쿼리 WHERE 조건
> - **앱미전달**: 서버 VIEW에서만 사용, 앱에 전달 안 됨

---

## 1. 용도

| 용도 | 설명 |
|------|------|
| **서버 WHERE 조건** | 출하대상 조회 시 창고 필터링 |

---

## 2. 특성

| 항목 | 내용 |
|------|------|
| **앱 전달** | ❌ 전달 안 됨 |
| **로컬 DB 저장** | ❌ 저장 안 함 |
| **DTO 필드** | ❌ 없음 |
| **용도** | 서버 쿼리 WHERE 조건만 |

---

## 3. 사용 위치

| 파일 | 용도 |
|------|------|
| ProgressDlgShipSearch.java | 서버 조회 WHERE 조건 |

---

## 4. 창고 코드 목록

| 창고 코드 | 창고명 (추정) |
|----------|--------------|
| IN10273 | 창고 1 |
| IN60464 | 창고 2 |
| 4001 | 창고 3 |
| 4004 | 창고 4 |
| IN63279 | 창고 5 |

---

## 5. 사용 코드

### 5.1 서버 조회 WHERE 조건

**파일**: ProgressDlgShipSearch.java (Line 118~196)

```java
// 선택한 창고에 따라 WHERE 조건 추가
if (Common.selectWarehouse.equals("삼일냉장")) {
    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
} else if (Common.selectWarehouse.equals("SWC")) {
    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
} else if (Common.selectWarehouse.equals("창고3")) {
    data += " AND GR_WAREHOUSE_CODE = '4001'";
} else if (Common.selectWarehouse.equals("창고4")) {
    data += " AND GR_WAREHOUSE_CODE = '4004'";
} else if (Common.selectWarehouse.equals("창고5")) {
    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
}
```

---

## 6. 조회 흐름

```
1. 사용자가 창고 선택 (Common.selectWarehouse)
        ↓
2. 선택한 창고에 해당하는 GR_WAREHOUSE_CODE 추가
        ↓
3. 서버에 WHERE 조건 포함하여 요청
   "... AND GR_WAREHOUSE_CODE = 'IN10273'"
        ↓
4. 서버에서 해당 창고의 출하대상만 응답
        ↓
5. 앱에서 응답 데이터 수신 (GR_WAREHOUSE_CODE 컬럼은 미포함)
```

---

## 7. VIEW에서의 역할

```sql
-- 서버 VIEW (VW_PDA_WID_LIST)
SELECT ...
FROM ...
WHERE GR_WAREHOUSE_CODE = 'IN10273'  -- 앱에서 전달한 조건
  AND ...
```

- VIEW 내부에서 **필터링 조건**으로만 사용
- SELECT 컬럼 목록에 포함되지 않음
- 앱에 **결과값 전달 안 됨**

---

## 8. VIEW 원천

| 항목 | 내용 |
|------|------|
| **VIEW 원천** | SM_출고상세.창고코드 |
| **역할** | 창고별 출하대상 필터링 |

---

## 9. 결론

**상태**: ⚠️ 서버용 (앱 미전달)

GR_WAREHOUSE_CODE는 **서버 WHERE 조건 전용 컬럼**으로:
- **앱에 전달되지 않음** (파싱 위치 없음)
- **로컬 DB에 저장하지 않음**
- 서버 조회 시 **창고 필터링** 조건으로만 사용
- `Common.selectWarehouse` 값에 따라 조건 추가

---

**최종 수정일**: 2026-02-03
