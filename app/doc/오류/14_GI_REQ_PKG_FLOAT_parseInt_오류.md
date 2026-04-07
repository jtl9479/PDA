# GI_REQ_PKG FLOAT 값 Integer.parseInt 오류

## 발견일
2026-04-07

## 현상
- 1차 바코드 스캔 후 센터 총수량이 0으로 표시
- 계근 완료 판별이 안됨 ("2.0" ≠ "2")
- 전송 시 전송 개수 비교 실패
- Logcat: `NumberFormatException: For input string: "2.0"`

## 원래부터 있던 버그인가?

**NO - DB 전환(Oracle→MSSQL)으로 발생한 신규 오류**

- 원본: Oracle에서 박스수량이 정수("2")로 내려옴 → `Integer.parseInt("2")` 정상
- 현재: MSSQL SM_출고LOT.박스수량이 FLOAT 타입 → "2.0"으로 내려옴 → `Integer.parseInt("2.0")` 에러

```java
// ERP Entity (DlivyLotEntity.java:107)
@Column(name = "박스수량", nullable = false, columnDefinition = "FLOAT")
private BigDecimal boxQy;
```

## 원인

### 문제 1 (주요): Integer.parseInt()로 FLOAT 값 파싱 (5곳)

| 줄 | 코드 | 용도 |
|:--:|------|------|
| 1679 | `Integer.parseInt(arSM.get(pos).getGI_REQ_PKG())` | 계근 완료 여부 체크 |
| 2270 | `Integer.parseInt(arSM.get(i).getGI_REQ_PKG())` | 센터 총 계근요청수량 합산 |
| 2434 | `Integer.parseInt(arSM.get(i).getGI_REQ_PKG())` | 센터 총 계근요청수량 합산 (수기) |
| 2642 | `Integer.parseInt(arSM.get(j).getGI_REQ_PKG())` | 전송 시 전송 개수 비교 |
| 2770 | `Integer.parseInt(arSM.get(j).getGI_REQ_PKG())` | 전송 시 전송 개수 비교 (생산/도매) |

```java
// NumberFormatException 발생
centerTotalCount += Integer.parseInt("2.0");  // ★ 에러
```

### 문제 2 (연쇄): 문자열 비교 불일치 (6곳)

| 줄 | 코드 | 용도 |
|:--:|------|------|
| 1209 | `getGI_REQ_PKG().equals(String.valueOf(getPACKING_QTY()))` | BL번호 매칭 시 계근 미완료 건 검색 |
| 1260 | 동일 패턴 | 요청수량 완료 여부 체크 |
| 1541 | 동일 패턴 | 요청수량 완료 여부 체크 (wet_data_insert) |
| 1891 | 동일 패턴 | 계근 미완료 지점 찾기 |
| 2283 | 동일 패턴 | 계근 미완료 지점 찾기 |
| 2496 | 동일 패턴 | 계근 완료 여부 체크 |

```java
// "2.0".equals("2") → 항상 false → 계근 완료 판별 불가
arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))
// GI_REQ_PKG = "2.0", PACKING_QTY = 2, String.valueOf(2) = "2"
// "2.0".equals("2") → false
```

### 중량(GI_REQ_QTY)은 왜 정상인가

| 컬럼 | MSSQL 타입 | JSP 응답 | Java 파싱 | 결과 |
|------|:--------:|:--------:|----------|:----:|
| 박스수량 (GI_REQ_PKG) | FLOAT | "2.0" | `Integer.parseInt("2.0")` | **에러** |
| 중량 (GI_REQ_QTY) | FLOAT | "2.0" | `Double.parseDouble("2.0")` | **정상** |

## 영향 범위

- BixolonShipmentActivity.java: 11곳 (parseInt 5곳 + equals 6곳)
- 센터 총수량 0 표시
- 계근 완료 판별 불가 → 전송 버튼 활성화 안됨
- 전송 시 전송 완료 비교 실패

## 수정 방안

### 방안 A: JSP에서 정수로 변환하여 내려보내기

```sql
-- search_shipment.jsp
CAST(L.박스수량 AS INT) AS GI_REQ_PKG
```

### 방안 B: PDA 파싱 시 FLOAT→INT 변환

```java
// ProgressDlgShipSearch.java 파싱 시
si.setGI_REQ_PKG(String.valueOf((int) Double.parseDouble(temp[5])));
```

### 방안 C: parseInt 사용처를 Double.parseDouble 후 int 캐스팅으로 변경

```java
centerTotalCount += (int) Double.parseDouble(arSM.get(i).getGI_REQ_PKG());
```

> 방안 A가 근본적 해결 (서버에서 정수로 내려보냄)
> 방안 B가 PDA 측 1곳 수정으로 전체 해결 (파싱 시점에서 변환)
> 방안 C는 11곳 모두 수정 필요 (비효율)

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`
- `app/doc/개발/18_이마트출하대상_서버조회_구현_가이드.md`
