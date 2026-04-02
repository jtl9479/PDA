# JSP 마지막 컬럼 빈값 파싱 오류

## 발견일
2026-04-02

## 현상
- search_shipment.jsp에서 30개 컬럼 정상 반환
- 마지막 컬럼(EMART_PLANT_CODE)이 빈값일 때 앱에서 `ArrayIndexOutOfBoundsException: length=29; index=29` 발생
- **TB_SHIPMENT INSERT 실패 → 이후 search_barcode_info, search_goods_wet 모두 `WHERE 1=0`으로 빈 조회**

## 원래부터 있던 버그인가?

**확인 불가** — 원본도 `split("::")` (-1 옵션 없음)을 동일하게 사용하므로, 마지막 컬럼이 빈값이면 동일 문제가 발생하는 구조. 원본에서 실제로 에러가 발생했는지는 서버 로그/운영 데이터가 없어 확인 불가.

## 원인

### 문제 1 (주요): Java String.split이 trailing empty string을 제거

#### 코드 위치
- `ProgressDlgShipSearch.java` : 207행
- `search_shipment.jsp` : 168행

#### 현재 문제 코드

JSP out.println:
```java
rs.getString("STORE_CODE") + "::" +        // 28
rs.getString("EMART_PLANT_CODE") + ";;"    // 29  ← 마지막 컬럼
```

서버 응답:
```
...::2006::;;
```
- STORE_CODE = `2006`, EMART_PLANT_CODE = `` (빈값)
- `2006` + `::` + `` + `;;` = `2006::;;`

앱 파싱:
```java
temp = s.split("::");  // "...::2006::" → length=29 (trailing empty string 제거)
si.setEMART_PLANT_CODE(temp[29].toString());  // ArrayIndexOutOfBoundsException
```

#### 발생 시나리오
1. JSP에서 마지막 컬럼(EMART_PLANT_CODE)이 빈값(`''`)
2. 서버 응답: `...::2006::;;` → `;;` 제거 후 `...::2006::`
3. Java `split("::")` → trailing empty string 제거 → length=29
4. temp[29] 접근 시 ArrayIndexOutOfBoundsException
5. catch에서 에러 처리 → INSERT 미실행
6. TB_SHIPMENT 빈 상태 → ②③ 모두 `WHERE 1=0`

## 상세 흐름

1. **search_shipment.jsp** (서버)
   - 30개 컬럼 정상 조회
   - EMART_PLANT_CODE = `''` (빈값)
   - 응답: `1329::...::2006::;;`

2. **ProgressDlgShipSearch** (앱)
   - `receiveData.split(";;")` → `["1329::...::2006::"]`
   - `"1329::...::2006::".split("::")` → length=29 (마지막 빈값 제거)
   - temp[29] 접근 → **ArrayIndexOutOfBoundsException**
   - INSERT 미실행

3. **ProgressDlgBarcodeSearch** (앱)
   - `selectqueryCodeList` → TB_SHIPMENT 비어있음 → 0건
   - `WHERE 1=0` → 빈 조회

4. **ProgressDlgGoodsWetSearch** (앱)
   - `selectqueryGIDIDList` → TB_SHIPMENT 비어있음 → 0건
   - `WHERE 1=0` → 빈 조회

## 영향 범위
- search_shipment.jsp뿐 아니라, **모든 JSP에서 마지막 컬럼이 빈값이면 동일 문제 발생**
- 해당 JSP: search_shipment.jsp, search_barcode_info.jsp, search_goods_wet.jsp, search_shipment_homeplus.jsp 등

## 수정 방안

### 방법 A: JSP에서 ISNULL 처리

마지막 컬럼이 NULL/빈값이 될 수 있는 경우 ISNULL로 기본값 부여:

```sql
-- 변경 전
, '' AS EMART_PLANT_CODE

-- 변경 후
, ISNULL(EMART_PLANT_CODE, ' ') AS EMART_PLANT_CODE
```

> 주의: 빈값(`''`)이 아닌 공백(`' '`)을 넣어야 split에서 제거되지 않음

### 방법 B: 앱에서 split 후 length 체크

```java
// 변경 전
si.setEMART_PLANT_CODE(temp[29].toString());

// 변경 후
if (temp.length > 29) {
    si.setEMART_PLANT_CODE(temp[29].toString());
} else {
    si.setEMART_PLANT_CODE("");
}
```

### 방법 C (권장): split에 limit -1 옵션 사용

```java
// 변경 전
temp = s.split("::");

// 변경 후
temp = s.split("::", -1);  // trailing empty string 유지
```

> `split(regex, -1)` → trailing empty string을 제거하지 않음. 모든 JSP 파싱에 공통 적용 가능.

| 방법 | 수정 위치 | 수정 범위 | 비고 |
|------|----------|----------|------|
| A | 각 JSP | JSP마다 수정 | JSP 수가 많으면 누락 위험 |
| B | 앱 파싱 코드 | 파싱마다 수정 | 코드 양 증가 |
| **C** | **앱 split 호출** | **공통 1곳** | **가장 안전하고 간단** |

## 상태
- [ ] 미수정
