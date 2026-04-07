# GI_REQ_PKG 파싱 수정 (FLOAT→INT 변환)

**작성일**: 2026-04-07
**목적**: ProgressDlgShipSearch.java 파싱 시 GI_REQ_PKG를 정수 문자열로 변환
**관련 오류**: `app/doc/오류/14_GI_REQ_PKG_FLOAT_parseInt_오류.md`

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### ProgressDlgShipSearch.java 파싱

```java
si.setGI_REQ_PKG(temp[5]);  // "2.0" 그대로 저장
```

### 이후 사용처에서 에러

```java
// BixolonShipmentActivity.java:2270
Integer.parseInt("2.0")  // NumberFormatException

// BixolonShipmentActivity.java:1260
"2.0".equals(String.valueOf(2))  // false → 계근 완료 판별 불가
```

### 문제점

- MSSQL SM_출고LOT.박스수량이 FLOAT → "2.0"으로 내려옴
- 11곳에서 정수로 사용하는데 "2.0"이 저장됨

---

## 2. 변경 구조

```
[변경 전]
JSP: "2.0" → temp[5] → setGI_REQ_PKG("2.0") → Integer.parseInt("2.0") → 에러

[변경 후]
JSP: "2.0" → temp[5] → (int)Double.parseDouble("2.0") = 2 → setGI_REQ_PKG("2") → Integer.parseInt("2") → 정상
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **ProgressDlgShipSearch.java** | temp[5] 파싱 | FLOAT→INT 변환 후 저장 |

---

## 4. 수정 상세

### 4.1 ProgressDlgShipSearch.java

**경로**: `app/src/main/java/.../common/ProgressDlgShipSearch.java`

**변경 전:**

```java
si.setGI_REQ_PKG(temp[5]);
```

**변경 후:**

```java
si.setGI_REQ_PKG(String.valueOf((int) Double.parseDouble(temp[5])));
```

- "2.0" → Double.parseDouble → 2.0 → (int) → 2 → String.valueOf → "2"

---

## 5. 사이드이펙트

- BixolonShipmentActivity.java 11곳 수정 불필요 (파싱 시점에서 변환)
- 다른 searchType(홈플러스, 롯데 등)도 동일 파싱 경로 → 함께 해결
- GI_REQ_QTY(중량)는 Double.parseDouble로 파싱하므로 수정 불필요

---

## 6. 개발 플랜

### Step 1: ProgressDlgShipSearch 파싱 수정

**Part 1. 분석**
- 대상: ProgressDlgShipSearch.java
- 범위: temp[5] 파싱 1곳
- 주의할 점: temp[5]가 null이거나 빈값일 경우 예외 처리

**체크리스트**
- [ ] Part 1: 파싱 코드 위치 확인
- [ ] Part 2: 변환 수행
- [ ] Part 3: 컴파일 확인
- [ ] Part 4: 출하대상받기 → 바코드 스캔 → 센터 총수량 정상 표시 확인
- [ ] Part 5: 계근 완료 시 전송 버튼 활성화 확인

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### 개발 순서 요약

```
Step 1: ProgressDlgShipSearch temp[5] 파싱 수정
```

---

## 7. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | ProgressDlgShipSearch 파싱 수정 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/오류/14_GI_REQ_PKG_FLOAT_parseInt_오류.md`
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`

---

**문서 버전**: 1.0
