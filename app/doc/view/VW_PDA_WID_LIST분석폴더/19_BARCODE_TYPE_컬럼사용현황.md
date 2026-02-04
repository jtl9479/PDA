# BARCODE_TYPE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 바코드타입
**파싱 위치**: temp[18]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | ● |
| 라벨출력 | ● |
| 로직분기 | ● |
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
| **바코드 생성 분기** | 타입별 바코드 구성 방식 결정 |
| **라벨 출력 분기** | 타입별 라벨 레이아웃/위치 결정 |
| **로직 분기** | M3/M4 미트센터, M9 우육센터 등 특수 처리 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 바코드 생성, 라벨 출력 분기 |
| ShipmentActivity.java | 바코드 생성, 라벨 출력 분기 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 바코드타입 저장 |

---

## 4. 바코드 타입 상수 정의

**파일**: BixolonShipmentActivity.java (Line 182~192)

```java
private static final String BARCODE_TYPE_M0 = "M0";  // 이마트 기본
private static final String BARCODE_TYPE_M1 = "M1";  // 이마트 (수입식별번호 없음)
private static final String BARCODE_TYPE_M3 = "M3";  // 미트센터 (PC매입)
private static final String BARCODE_TYPE_M4 = "M4";  // 미트센터 (PC매입, 수입식별번호 없음)
private static final String BARCODE_TYPE_M8 = "M8";  // 이마트 비정량
private static final String BARCODE_TYPE_M9 = "M9";  // 이마트 우육 센터납
private static final String BARCODE_TYPE_E0 = "E0";  // 에브리데이 (수입식별번호 포함)
private static final String BARCODE_TYPE_E1 = "E1";  // 에브리데이 (고정 수입식별번호)
private static final String BARCODE_TYPE_E2 = "E2";  // 이마트 냉동 (13자리+12자리)
private static final String BARCODE_TYPE_E3 = "E3";  // 이마트 냉장 (13자리만)
private static final String BARCODE_TYPE_P0 = "P0";  // 생산투입
// L0 = WMS 물류라벨 (코드에서 문자열로 직접 사용)
```

---

## 5. 바코드 타입별 구성

| 타입 | 명칭 | 바코드 구성 | 길이 |
|------|------|-----------|:----:|
| **M0** | 이마트 기본 | 상품6 + 중량6 + 회사 + 수입식별12 | 27 |
| **M1** | 이마트 | 상품6 + 중량6 + 회사 | 15 |
| **M3** | 미트센터 PC매입 | 납품6 + 중량6 + 회사 + 수입식별12 | 27 |
| **M4** | 미트센터 PC매입 | 납품6 + 중량6 + 회사 | 15 |
| **M8** | 비정량 | 상품6 + 중량6 + 회사 + 수입식별12 | 27 |
| **M9** | 우육 센터납 | 상품6 + 수입식별12 + 용도코드 | 가변 |
| **E0** | 에브리데이 | 상품6 + 중량6 + 회사 + 수입식별12 | 27 |
| **E1** | 에브리데이 | 상품6 + 중량6 + 회사 + 111111111111 | 27 |
| **E2** | 냉동 | 상품13 + 수입식별12 | 25 |
| **E3** | 냉장 | 상품13 | 13 |
| **P0** | 생산투입 | 상품13 + 중량6 + 회사 + 수입식별12 | 34 |
| **L0** | WMS 물류라벨 | 별도 형식 | - |

---

## 6. 사용 코드

### 6.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 286)

```java
si.setBARCODE_TYPE(temp[18].toString());    // 바코드타입
```

### 6.2 바코드 생성 분기

**파일**: BixolonShipmentActivity.java (Line 2216~2401)

```java
switch (si.getBARCODE_TYPE()) {
    case "M0":
        // 상품코드6 + 중량6 + 회사코드 + 수입식별번호12
        pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
        break;
    case "M1":
        // 상품코드6 + 중량6 + 회사코드
        pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode;
        break;
    case "E2":
        // 상품코드13 + 수입식별번호12
        pBarcode = si.getEMARTITEM_CODE() + si.getIMPORT_ID_NO();
        break;
    case "E3":
        // 상품코드13
        pBarcode = si.getEMARTITEM_CODE();
        break;
    // ... 기타 타입
}
```

### 6.3 라벨 출력 위치 분기

**파일**: BixolonShipmentActivity.java (Line 2510~2525)

```java
// 바코드 타입별 메인 바코드 출력 위치 설정
int barcodeX = 80, barcodeY = 170;  // 기본 위치
if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_E0)
        || si.getBARCODE_TYPE().equals(BARCODE_TYPE_E1) || si.getBARCODE_TYPE().equals(BARCODE_TYPE_M8)) {
    barcodeX = 80; barcodeY = 170;
} else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M1)) {
    barcodeX = 145; barcodeY = 170;
} else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_E2)) {
    barcodeX = 90; barcodeY = 170;
} else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_E3)) {
    barcodeX = 160; barcodeY = 170;
} // ...
```

### 6.4 L0 (WMS 물류라벨) 분기

**파일**: BixolonShipmentActivity.java (Line 2972~2974)

```java
switch (si.getBARCODE_TYPE()) {
    case "L0":
        // WMS 물류라벨 별도 처리
        break;
}
```

---

## 7. 타입별 특수 처리

| 타입 | 특수 처리 |
|------|----------|
| **M3/M4** | 미트센터 - PC매입/PC출하 텍스트 출력 |
| **M9** | 우육 센터납 - 가로선 그리기, 용도명 출력 |
| **P0** | 생산투입 - 라벨 전송 안 함 |
| **L0** | WMS 물류라벨 - 별도 라벨 형식 |

---

## 8. 바코드 타입 그룹

### 이마트 계열
| 타입 | 수입식별번호 | 용도 |
|------|:-----------:|------|
| M0 | ✅ 포함 | 이마트 기본 |
| M1 | ❌ 미포함 | 이마트 |
| M8 | ✅ 포함 | 비정량 |
| E2 | ✅ 포함 | 냉동 |
| E3 | ❌ 미포함 | 냉장 |

### 미트센터 계열
| 타입 | 수입식별번호 | 용도 |
|------|:-----------:|------|
| M3 | ✅ 포함 | PC매입 |
| M4 | ❌ 미포함 | PC매입 |
| M9 | ✅ 포함 | 우육 센터납 |

### 에브리데이 계열
| 타입 | 수입식별번호 | 용도 |
|------|:-----------:|------|
| E0 | ✅ 포함 | 기본 |
| E1 | ✅ 고정값 | 111111111111 |

### 기타
| 타입 | 용도 |
|------|------|
| P0 | 생산투입 |
| L0 | WMS 물류라벨 |

---

## 9. VIEW에서 BARCODE_TYPE 결정 로직

```sql
DECODE(CENTER_SCALE_USE_YN, 'Y',
    DECODE(BI.ITEM_TYPE, 'A', 'M3', 'B', 'M4',
        DECODE(BI.MAJOR_CATEGORY, '10', 'M9', EO.BARCODE_TYPE),
        EO.BARCODE_TYPE),
    EO.BARCODE_TYPE)
```

| 조건 | BARCODE_TYPE |
|------|-------------|
| CENTER_SCALE_USE_YN='Y' + ITEM_TYPE='A' | M3 |
| CENTER_SCALE_USE_YN='Y' + ITEM_TYPE='B' | M4 |
| CENTER_SCALE_USE_YN='Y' + MAJOR_CATEGORY='10' | M9 |
| 그 외 | EO.BARCODE_TYPE (B_EMART_BARCODE 테이블 값) |

---

## 10. 결론

**상태**: ✅ 필수 (삭제 불가)

BARCODE_TYPE은 **라벨 형식 결정의 핵심 컬럼**으로:
- **바코드 구성** 방식 결정 (수입식별번호 포함 여부, 길이 등)
- **라벨 레이아웃** 결정 (출력 위치, 폰트 크기 등)
- **특수 처리** 분기 (미트센터, 우육센터, WMS 등)
- 서버 VIEW에서 **DECODE 로직**으로 자동 결정

---

**최종 수정일**: 2026-02-03
