# EMARTLOGIS_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 물류코드 (지점코드)
**파싱 위치**: temp[23]

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
| **pBarcode2 생성** | 바코드2 앞 6자리 (대부분의 바코드 타입) |
| **라벨 출력** | 정량 라벨에 지점코드 표시 |
| **미트센터 분기** | LOGIS_CODE_DEFAULT("0000000") 비교 |
| **롯데 라벨** | 회사코드로 사용 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 바코드 생성, 라벨 출력 |
| ShipmentActivity.java | 바코드 생성, 라벨 출력 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 물류코드 저장 |

---

## 4. 상수 정의

**파일**: BixolonShipmentActivity.java (Line 176)

```java
private static final String LOGIS_CODE_DEFAULT = "0000000";  // 로지스코드 기본값
```

---

## 5. 사용 코드

### 5.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 291)

```java
si.setEMARTLOGIS_CODE(temp[23].toString());  // 이마트물류코드
```

### 5.2 바코드 타입별 pBarcode2 생성

**파일**: BixolonShipmentActivity.java (Line 2231~2399)

```java
// M0: 기본형
pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();

// M1: 수입식별번호 없음
pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode;

// M3: 직납분
pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();

// M4: 직납분 (수입식별번호 없음)
pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode;

// M8: 비정량 납품분
pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode;

// M9: 우육 센터납
pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();

// E0: 수입식별번호 포함
pBarcode2 = si.getEMARTLOGIS_CODE().toString() + si.getIMPORT_ID_NO();  // 전체 사용

// E1: 수입식별번호 없음
pBarcode2 = si.getEMARTLOGIS_CODE();  // 전체 사용

// E2, E3: 직납분
pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
```

### 5.3 라벨 출력 - 지점코드 (정량)

**파일**: BixolonShipmentActivity.java (Line 2782, 2811)

```java
pointCode = si.EMARTLOGIS_CODE.toString();

// 정량 라벨: 지점코드(EMARTLOGIS_CODE) 출력
if (!si.getITEM_TYPE().equals(ITEM_TYPE_B)) {  // 비정량이 아니면
    slcsCmd.append(slcsText(170, 135, 155, 155, pointCode.toString()));  // 정량: 지점코드
}
```

### 5.4 미트센터 분기 조건

**파일**: BixolonShipmentActivity.java (Line 2618, 2671)

```java
// 미트센터 + 공장코드 라벨
if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0)
    && si.getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)
    && si.getEMARTLOGIS_CODE().equals(LOGIS_CODE_DEFAULT)  // ★ "0000000"
    && !si.getEMART_PLANT_CODE().equals("")) {
    // ERP-미트센터출하코드 라벨
}

// 미트센터 라벨 (공장코드 없음)
if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0)
    && si.getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)
    && !si.getEMARTLOGIS_CODE().equals(LOGIS_CODE_DEFAULT)  // ★ "0000000" 아님
    && si.getEMART_PLANT_CODE().equals("")) {
    // 미트센터출하코드 라벨
}
```

### 5.5 롯데 라벨 - 회사코드

**파일**: BixolonShipmentActivity.java (Line 2894)

```java
String pCompCode_lotte = si.EMARTLOGIS_CODE;  // 롯데전용 업체코드 (VIEW에서 EMARTLOGIS_CODE로 받음)
```

---

## 6. 바코드 타입별 EMARTLOGIS_CODE 사용

| 바코드 타입 | 사용 방식 | 예시 |
|:-----------:|----------|------|
| M0, M3, M9 | substring(0, 6) | "880123" |
| M1, M4 | substring(0, 6) | "880123" |
| M8 | substring(0, 6) | "880123" |
| **E0** | 전체 + IMPORT_ID_NO | "8801234567" |
| **E1** | 전체 그대로 | "8801234567" |
| E2, E3 | substring(0, 6) | "880123" |
| **L0** | 회사코드로 사용 | "LT01" |

---

## 7. pBarcode2 구조 예시 (M0 타입)

```
┌────────────────────────────────────────────────────────┐
│ EMARTLOGIS_CODE(6) │ 중량(6) │ 회사코드 │ 수입식별번호(12) │
│      880123       │ 001350  │   01    │  123456789012   │
└────────────────────────────────────────────────────────┘
         ↑              ↑         ↑           ↑
  EMARTLOGIS_CODE   print_    pCompCode  IMPORT_ID_NO
   .substring(0,6)  weight_str
```

---

## 8. EMARTLOGIS_CODE vs STORE_CODE

| 컬럼 | 의미 | 파싱 위치 | 라벨 출력 조건 |
|------|------|:--------:|---------------|
| **EMARTLOGIS_CODE** | 물류코드/지점코드 | temp[23] | **정량** (ITEM_TYPE ≠ B) |
| STORE_CODE | 점포코드 | temp[28] | **비정량** (ITEM_TYPE = B) |

---

## 9. VIEW 원천

| 항목 | 내용 |
|------|------|
| **VIEW 컬럼** | EMARTLOGIS_CODE |
| **원천 테이블** | B_EMART_BARCODE (별칭 EB), W_EMART_ORDER (별칭 EO) |
| **NULL 처리** | DECODE(NULL, '0000000') |
| **역할** | 물류 지점/센터 식별 |

### VIEW SQL 발췌

```sql
-- VW_PDA_WID_LIST (Line 90)
DECODE(EO.EMARTLOGIS_CODE, NULL, '0000000', EO.EMARTLOGIS_CODE) AS EMARTLOGIS_CODE
-- NULL이면 '0000000' (LOGIS_CODE_DEFAULT)
```

---

## 10. 특수 값

| 값 | 의미 | 처리 |
|:--:|------|------|
| **"0000000"** | 기본값 (LOGIS_CODE_DEFAULT) | 미트센터 분기 조건 |

---

## 11. 라벨 레이아웃 예시

### 정량 라벨 (ITEM_TYPE ≠ B)
```
┌─────────────────────────────────────┐
│  지점명                             │
│  ┌──────────┐                       │
│  │ 880123   │  ← EMARTLOGIS_CODE    │
│  └──────────┘                       │
│  상품명                             │
│  ...                                │
└─────────────────────────────────────┘
```

### 비정량 라벨 (ITEM_TYPE = B)
```
┌─────────────────────────────────────┐
│  지점명                             │
│  ┌──────────┐                       │
│  │  1234    │  ← STORE_CODE (점포코드) │
│  └──────────┘                       │
│  상품명                             │
│  ...                                │
└─────────────────────────────────────┘
```

---

## 12. 결론

**상태**: ✅ 필수 (삭제 불가)

EMARTLOGIS_CODE는 **바코드 생성의 핵심 컬럼**으로:
- **pBarcode2** 생성 시 앞 6자리 사용 (대부분의 타입)
- **정량 라벨**에서 지점코드로 표시
- **미트센터 분기**: "0000000" (LOGIS_CODE_DEFAULT) 비교
- **롯데 라벨**에서 회사코드로 사용
- NULL 값은 VIEW에서 **"0000000"**으로 변환

---

**최종 수정일**: 2026-02-03
