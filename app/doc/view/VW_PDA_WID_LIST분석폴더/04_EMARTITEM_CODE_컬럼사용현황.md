# EMARTITEM_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 이마트 상품코드
**파싱 위치**: temp[3]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | ● |
| 라벨출력 | |
| 로직분기 | |
| DB저장 | ● |
| 바코드검증 | |
| 조회조건 | |
| 앱미전달 | |

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
| **pBarcode 생성** | 라벨 출력용 바코드 생성의 핵심 구성 요소 |
| **DB 저장** | TB_SHIPMENT, TB_GOODS_WET 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| Goodswets_Info.java | 계근 데이터 DTO |
| DBHandler.java | TB_SHIPMENT, TB_GOODS_WET 테이블 CRUD |
| BixolonShipmentActivity.java | pBarcode 생성 |
| ShipmentActivity.java | pBarcode 생성 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 이마트 상품코드 저장 |
| TB_GOODS_WET | 계근 데이터 이마트 상품코드 저장 |

---

## 4. 바코드 생성 사용 방식

### 4.1 사용 형태

| 형태 | 설명 | 예시 |
|------|------|------|
| **전체 사용** | 이마트 상품코드 전체 사용 | `si.getEMARTITEM_CODE()` |
| **6자리 추출** | 앞 6자리만 사용 | `si.getEMARTITEM_CODE().substring(0, 6)` |

---

## 5. 바코드 타입별 사용 패턴

### 5.1 E0 (이마트 기본)

**파일**: BixolonShipmentActivity.java (Line 1983)

```java
// 바코드 = 상품코드 + 중량 + "00" + 연월일시분초
pBarcode = si.getEMARTITEM_CODE() + print_weight_str + "00" + now;
```

### 5.2 M0~M5 (일반 이마트)

**파일**: BixolonShipmentActivity.java (Line 2228)

```java
// 바코드 = 상품코드(6) + 중량 + 회사코드 + 수입식별번호
pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
```

### 5.3 M6 (상품코드 전체 + 수입식별번호)

**파일**: BixolonShipmentActivity.java (Line 2325)

```java
// 바코드 = 상품코드(전체) + 수입식별번호
pBarcode = si.getEMARTITEM_CODE().toString() + si.getIMPORT_ID_NO();
```

### 5.4 M7 (상품코드만)

**파일**: BixolonShipmentActivity.java (Line 2338)

```java
// 바코드 = 상품코드(전체)
pBarcode = si.getEMARTITEM_CODE();
```

### 5.5 M8 (상품코드 전체 + 중량 + 회사코드 + 수입식별번호)

**파일**: BixolonShipmentActivity.java (Line 2354)

```java
// 바코드 = 상품코드(전체) + 중량 + 회사코드 + 수입식별번호
pBarcode = si.getEMARTITEM_CODE() + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
```

### 5.6 M9 (미트센터용)

**파일**: BixolonShipmentActivity.java (Line 2373)

```java
// pBarcode = 상품코드(6) + 중량 + 회사코드 + 수입식별번호
pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();

// pBarcode2 = 상품코드(6) + 수입식별번호 + 용도코드
pBarcode2 = si.getEMARTITEM_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + si.getUSE_CODE();
```

### 5.7 L0 (롯데)

**파일**: BixolonShipmentActivity.java (Line 2996)

```java
// 바코드 = 회사코드 + 제조일자 + 중량(4자리) + 마트제품코드(6) + 박스번호
pBarcode = pCompCode_lotte + making_date + print_weight_str.substring(print_weight_str.length()-4, print_weight_str.length()) + si.getEMARTITEM_CODE().substring(0, 6) + boxserial_cnt;
```

---

## 6. 바코드 구조 요약

| 바코드 타입 | 구조 | EMARTITEM_CODE 사용 |
|-------------|------|---------------------|
| E0 | 상품코드 + 중량 + 00 + 연월일시분초 | 전체 |
| M0~M5 | 상품코드(6) + 중량 + 회사코드 + 수입식별번호 | substring(0, 6) |
| M6 | 상품코드 + 수입식별번호 | 전체 |
| M7 | 상품코드 | 전체 |
| M8 | 상품코드 + 중량 + 회사코드 + 수입식별번호 | 전체 |
| M9 | 상품코드(6) + 중량 + 회사코드 + 수입식별번호 | substring(0, 6) |
| L0 | 회사코드 + 제조일자 + 중량(4) + 상품코드(6) + 박스번호 | substring(0, 6) |

---

## 7. 결론

**상태**: ✅ 필수 (삭제 불가)

EMARTITEM_CODE는 **pBarcode 생성의 핵심 컬럼**으로, 모든 바코드 타입에서 사용됩니다.
- 대부분 **앞 6자리**를 추출하여 사용
- 일부 타입(M6, M7, M8, E0)은 **전체 코드** 사용

---

**최종 수정일**: 2026-02-03
