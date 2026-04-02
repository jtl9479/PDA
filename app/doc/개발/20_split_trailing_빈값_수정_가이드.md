# split trailing 빈값 수정 가이드

**작성일**: 2026-04-02
**목적**: Java `split("::")`이 trailing empty string을 제거하여 마지막 컬럼 빈값 시 파싱 에러 발생하는 문제 수정

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### 현재 코드

```java
temp = s.split("::");
```

- Java `split(regex)`은 trailing empty string을 제거함
- 서버 응답 `...::2006::;;` → `;;` 제거 후 `...::2006::` → `split("::")` → 마지막 빈값 제거 → length=N-1
- temp[N-1] 접근 시 `ArrayIndexOutOfBoundsException`

### 문제점

- JSP 마지막 컬럼이 빈값일 때 파싱 에러 발생
- 에러로 INSERT 미실행 → 이후 체인(search_barcode_info, search_goods_wet) 전체 실패
- 오류 문서: `app/doc/오류/05_JSP_마지막컬럼_빈값_파싱오류.md`

### 확실히 빈값으로 에러 발생하는 JSP

| JSP | 마지막 컬럼 | SELECT 값 |
|-----|-----------|----------|
| search_shipment.jsp | EMART_PLANT_CODE | `'' AS EMART_PLANT_CODE` |
| search_shipment_wholesale.jsp | WH_AREA | VIEW에서 `'' AS WH_AREA` |
| search_goods_wet.jsp | BOX_ORDER | `'' AS BOX_ORDER` |

### DB NULL 시 에러 발생 가능한 JSP

| JSP | 마지막 컬럼 |
|-----|-----------|
| search_shipment_homeplus.jsp | EMARTLOGIS_NAME |
| search_production.jsp | EMARTLOGIS_NAME |
| search_production_4label.jsp | EMARTLOGIS_NAME |
| search_production_nonfixed.jsp | STORE_CODE |
| search_homeplus_nonfixed.jsp | STORE_CODE |
| search_barcode_info.jsp | SHELF_LIFE |

---

## 2. 변경 구조

### 변경 내용

```java
// 변경 전
temp = s.split("::");

// 변경 후
temp = s.split("::", -1);
```

- `split(regex, -1)` → trailing empty string을 유지
- 마지막 컬럼이 빈값이어도 배열에 포함됨

### 기존 기능 영향

- 마지막 컬럼에 값이 있는 경우: 기존과 동일 (영향 없음)
- 마지막 컬럼이 빈값인 경우: 기존 에러 → 정상으로 변경
- `split(";;")`은 변경하지 않음 (기존대로 유지)

---

## 3. 수정 대상 파일

| # | 파일 | 라인 | 용도 |
|:-:|------|:----:|------|
| 1 | **ProgressDlgShipSearch.java** | 207 | 출하대상 파싱 |
| 2 | **ProgressDlgBarcodeSearch.java** | 106 | 바코드 정보 파싱 |
| 3 | **ProgressDlgGoodsWetSearch.java** | 99 | 계근 내역 파싱 |
| 4 | **LoginActivity.java** | 118 | 창고 목록 파싱 |
| 5 | **ProductionActivity.java** | 420 | 생산 데이터 파싱 |
| 6 | **ProgressDlgNewBarcodeInfo.java** | 75 | 바코드 추가 파싱 |

---

## 4. 수정 상세

### 4.1 ProgressDlgShipSearch.java

**경로**: `app/src/main/java/.../common/ProgressDlgShipSearch.java`

**변경 전 (207행):**
```java
temp = s.split("::");
```

**변경 후:**
```java
temp = s.split("::", -1);
```

**검증**: search_shipment.jsp EMART_PLANT_CODE 빈값 시 정상 파싱 확인

---

### 4.2 ProgressDlgBarcodeSearch.java

**경로**: `app/src/main/java/.../common/ProgressDlgBarcodeSearch.java`

**변경 전 (106행):**
```java
temp = s.split("::");
```

**변경 후:**
```java
temp = s.split("::", -1);
```

**검증**: search_barcode_info.jsp SHELF_LIFE 빈값 시 정상 파싱 확인

---

### 4.3 ProgressDlgGoodsWetSearch.java

**경로**: `app/src/main/java/.../common/ProgressDlgGoodsWetSearch.java`

**변경 전 (99행):**
```java
temp = s.split("::");
```

**변경 후:**
```java
temp = s.split("::", -1);
```

**검증**: search_goods_wet.jsp BOX_ORDER 빈값 시 정상 파싱 확인

---

### 4.4 LoginActivity.java

**경로**: `app/src/main/java/.../LoginActivity.java`

**변경 전 (118행):**
```java
String[] cols = row.split("::");
```

**변경 후:**
```java
String[] cols = row.split("::", -1);
```

**검증**: search_warehouse.jsp 마지막 컬럼 빈값 시 정상 파싱 확인

---

### 4.5 ProductionActivity.java

**경로**: `app/src/main/java/.../ProductionActivity.java`

**변경 전 (420행):**
```java
String[] tempArray = temp.split("::");
```

**변경 후:**
```java
String[] tempArray = temp.split("::", -1);
```

**검증**: 생산 데이터 마지막 컬럼 빈값 시 정상 파싱 확인

---

### 4.6 ProgressDlgNewBarcodeInfo.java

**경로**: `app/src/main/java/.../common/ProgressDlgNewBarcodeInfo.java`

**변경 전 (75행):**
```java
String[] split_data = addData.split("::");
```

**변경 후:**
```java
String[] split_data = addData.split("::", -1);
```

**검증**: 바코드 추가 데이터 마지막 컬럼 빈값 시 정상 파싱 확인

---

## 5. 사이드이펙트

### split(";;")`은 변경하지 않음

- `receiveData.split(";;")` — 기존대로 유지
- JSP 응답이 `데이터;;`로 끝나면 trailing empty string 제거 → 기존 동작 유지

### 정상 데이터에 대한 영향

- 마지막 컬럼에 값이 있는 경우: `split("::")` == `split("::", -1)` → 동일
- 기존 정상 동작하던 케이스에 영향 없음

---

## 6. 데이터 저장 구조

해당 없음 (split 옵션 변경만, 데이터 구조 변경 없음)

---

## 7. 호출 시점

해당 없음 (기존 호출 흐름 변경 없음, split 동작만 변경)

---

## 8. 개발 플랜

### Step 1: 6개 파일 split 수정

**Part 1. 분석**
- 메서드: 각 파일의 서버 응답 파싱 부분
- 범위: 6개 파일, 각 1행
- 용도: `split("::")` → `split("::", -1)` 변경
- 주의할 점: `split(";;")`은 변경하지 않음

| # | 파일 | 라인 | 변경 |
|---|------|:----:|------|
| 1 | ProgressDlgShipSearch.java | 207 | `split("::")` → `split("::", -1)` |
| 2 | ProgressDlgBarcodeSearch.java | 106 | `split("::")` → `split("::", -1)` |
| 3 | ProgressDlgGoodsWetSearch.java | 99 | `split("::")` → `split("::", -1)` |
| 4 | LoginActivity.java | 118 | `split("::")` → `split("::", -1)` |
| 5 | ProductionActivity.java | 420 | `split("::")` → `split("::", -1)` |
| 6 | ProgressDlgNewBarcodeInfo.java | 75 | `split("::")` → `split("::", -1)` |

**Part 2. 변환 계획**
- 변환 방식: 6개 파일 일괄 수정
- 주의사항: `split(";;")`은 변경하지 않음

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 2: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | search_shipment.jsp 마지막 컬럼 빈값 → 파싱 정상 | □ |
| 2 | search_barcode_info.jsp 파싱 정상 | □ |
| 3 | search_goods_wet.jsp 파싱 정상 | □ |
| 4 | search_warehouse.jsp 파싱 정상 (기존 동작 유지) | □ |
| 5 | 이마트 출하대상받기 → ①②③ 전체 체인 정상 | □ |
| 6 | 앱 비정상 종료 없음 | □ |

---

### 개발 순서 요약

```
Step 1: 6개 파일 split 수정
    ↓
Step 2: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 이마트 출하대상받기 (마지막 컬럼 빈값)

```
1. 앱 실행 → 로그인
2. 날짜/창고 선택
3. 이마트 출하대상받기
4. search_shipment.jsp 응답에서 EMART_PLANT_CODE 빈값
5. 파싱 정상 완료 확인
6. search_barcode_info.jsp → search_goods_wet.jsp 체인 정상 확인
```

### 시나리오 2: 기존 정상 동작 확인

```
1. 마지막 컬럼에 값이 있는 데이터로 조회
2. 기존과 동일하게 파싱 정상 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | split 변경 후 배열 길이 증가 | trailing empty string 유지 | 파싱 인덱스가 고정이므로 영향 없음 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | 6개 파일 split 수정 | ⏳ 대기 |
| 2 | 통합 테스트 | ⏳ 대기 |

---

**문서 버전**: 1.0
