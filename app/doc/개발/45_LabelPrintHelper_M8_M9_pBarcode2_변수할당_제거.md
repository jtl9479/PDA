# LabelPrintHelper M8/M9 pBarcode2 변수 할당 제거

**작성일**: 2026-04-30
**목적**: `LabelPrintHelper.java` setPrinting() 내 case "M8" 및 case "M9" 블록에서 `pBarcode2`/`pBarcodeStr2` 변수 할당 2줄씩을 제거한다. 두 변수는 M8/M9 case에서 할당은 되지만 인쇄 분기(M3/M4 전용)에 포함되지 않아 실제 라벨에 인쇄되지 않는 미사용 할당임을 원본 코드 및 자체 검증으로 확인하였다. 문의사항 03번(답변 미수신)에 대해 사용자 명시 결정(2026-04-30)으로 제거를 진행한다.

> **진행 배경**: `app/doc/문의사항/03_M8_비정량_이마트_pBarcode2_생성_미출력_의도.md` 에 기록된 문의 사항에 대한 답변을 수신하지 못한 상태에서, 사용자가 2026-04-30 자체 검증 결과를 확인한 뒤 "M8 pBarcode2를 제거하고자 한다 — 작업 범위는 M8 + M9 둘 다(B안)"로 명시 결정하였다. 동작 동일성(CLAUDE.md 원칙)은 코드 레벨이 아닌 동작 기준으로 충족한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- **pBarcode2 인쇄 분기(M3/M4) 및 메서드 상단 `String pBarcode2 = "";` 초기 선언은 절대 변경하지 않는다 — 다른 case(M0/M1/M3/M4/E0~E3)가 참조하므로**
- **M8/M9 case 내 `pBarcode`/`pBarcodeStr`(첫 번째 바코드) 할당은 절대 변경하지 않는다**

---

## 1. 현재 구조

### 1.1 LabelPrintHelper.java — 변수 초기 선언 (L454~455)

```java
String pBarcode2 = "";
String pBarcodeStr2 = "";
```

- 메서드 상단에 빈 문자열로 초기화 선언
- M3/M4/E0~E3/M0/M1/M9/M8 등 여러 case에서 참조할 수 있도록 메서드 스코프 전체에 유효
- **이 선언은 이번 작업에서 변경하지 않는다**

### 1.2 LabelPrintHelper.java — case "M9" 블록 (L648~665)

```java
case "M9":
    // 비정량 이마트 M9 (문의사항05 답변: 정량 이마트 우육 센터납 임시 사용 종료)
    // 상품코드 앞자리 6자리 + 중량 6자리 + 회사코드 6자리 = 18자리
    if (Common.D) {
        Log.e(TAG, "::::::::: M9 (비정량 이마트) ::::::::");
        Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
        Log.d(TAG, "중량 6자리 :" + print_weight_str);
        Log.d(TAG, "회사코드 : " + pCompCode);
    }

    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode;
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;

    // pBarcode2: 변수 생성만, 인쇄 안 함 (M8과 동일 정책)
    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    break;
```

- 개발43(2026-04-27) Step 2에서 비정량 이마트 M9 재정의 시 추가된 pBarcode2/pBarcodeStr2 할당
- `EMARTLOGIS_CODE`가 빈 문자열일 경우 `.substring(0, 6)` 에서 `StringIndexOutOfBoundsException` 발생 위험(오류 21번과 동일 패턴)

### 1.3 LabelPrintHelper.java — case "M8" 블록 (L667~683)

```java
case "M8":
    // 이마트 비정량 납품분
    // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
    if (Common.D) {
        Log.e(TAG, "::::::::: M8 ::::::::");
        Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
        Log.d(TAG, "중량 6자리 :" + print_weight_str);
        Log.d(TAG, "회사코드 : " + pCompCode);
        Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
    }

    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode ;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
    break;
```

- M8 case에서 pBarcode2/pBarcodeStr2를 할당하나 인쇄 분기에 M8이 포함되지 않아 실제 인쇄 없음
- `EMARTLOGIS_CODE` substring 예외 위험 동일 존재

### 1.4 pBarcode2 인쇄 분기 (L799~812) — M3/M4 전용

```java
// M3, M4 추가 바코드 출력
if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3)) {
    labelData.write(slcsBarcode(70, 205, 60, pBarcode2).getBytes("EUC-KR"));
} else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
    labelData.write(slcsBarcode(145, 205, 60, pBarcode2).getBytes("EUC-KR"));
}

// M3, M4 PC출하 텍스트 출력
if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3)) {
    labelData.write(slcsBitmapText(25, 265, 25, pBarcodeStr2 + "  PC출하", true));
} else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
    labelData.write(slcsBitmapText(117, 265, 25, pBarcodeStr2 + "  PC출하", true));
}
```

- M3/M4만 인쇄 분기에 포함. M8/M9/M0/M1/E0~E3는 포함되지 않음.

### 문제점

- M8/M9 case에서 `pBarcode2`/`pBarcodeStr2`를 할당하지만 인쇄 분기에 포함되지 않아 할당이 무효(미사용 코드)
- 특히 `EMARTLOGIS_CODE`가 빈 문자열일 때 `.substring(0, 6)` 에서 예외 발생 위험(오류 21번 동일 패턴) — 사용되지도 않는 연산에서 런타임 오류가 발생할 수 있는 구조
- 자체 검증 근거: 원본 `ShipmentActivity.java` L2216~2218에 LOGISCODE128 무조건 출력 코드가 주석 처리된 흔적이 있어 **의도적 미출력**임을 확인. 소스분석 51번 및 문의사항 03번에 상세 기록.

---

## 2. 변경 구조

### 데이터 흐름 (변경 전)

```
setPrinting() 호출 (M8 또는 M9)
    ↓
String pBarcode2 = "";   // L454 초기 선언
String pBarcodeStr2 = ""; // L455 초기 선언
    ↓
case "M9":
    pBarcode2 = EMARTLOGIS_CODE.substring(0,6) + ...  [할당] ← 미사용
    pBarcodeStr2 = EMARTLOGIS_CODE.substring(0,6) + ...  [할당] ← 미사용
case "M8":
    pBarcode2 = EMARTLOGIS_CODE.substring(0,6) + ...  [할당] ← 미사용
    pBarcodeStr2 = EMARTLOGIS_CODE.substring(0,6) + ...  [할당] ← 미사용
    ↓
인쇄 분기 (L799~812):
    M3 → pBarcode2 인쇄
    M4 → pBarcode2 인쇄
    M8 → (분기 없음, 빈 문자열 상태이더라도 동일)
    M9 → (분기 없음)
    ↓
결과: M8/M9 라벨에 pBarcode2 인쇄 없음
```

### 데이터 흐름 (변경 후)

```
setPrinting() 호출 (M8 또는 M9)
    ↓
String pBarcode2 = "";   // L454 초기 선언 (유지)
String pBarcodeStr2 = ""; // L455 초기 선언 (유지)
    ↓
case "M9":
    pBarcode, pBarcodeStr 만 할당 (EMARTLOGIS_CODE 미참조)
case "M8":
    pBarcode, pBarcodeStr 만 할당 (EMARTLOGIS_CODE 미참조)
    ↓
인쇄 분기 (L799~812):
    M3 → pBarcode2 인쇄 (유지)
    M4 → pBarcode2 인쇄 (유지)
    M8 → (분기 없음, pBarcode2 = "" 상태)
    M9 → (분기 없음, pBarcode2 = "" 상태)
    ↓
결과: M8/M9 라벨에 pBarcode2 인쇄 없음 (동작 동일, EMARTLOGIS_CODE 참조 위험 제거)
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **LabelPrintHelper.java** | `app/src/main/java/com/rgbsolution/highland_emart/print/` | case "M9" 내 pBarcode2/pBarcodeStr2 할당 2줄 + 관련 주석 1줄 삭제 |
| 2 | **LabelPrintHelper.java** | 동일 파일 | case "M8" 내 pBarcode2/pBarcodeStr2 할당 2줄 삭제 |

---

## 4. 수정 상세

### 4.1 case "M9" — pBarcode2/pBarcodeStr2 할당 제거

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/print/LabelPrintHelper.java`

**변경 전 (L648~665 전체):**

```java
case "M9":
    // 비정량 이마트 M9 (문의사항05 답변: 정량 이마트 우육 센터납 임시 사용 종료)
    // 상품코드 앞자리 6자리 + 중량 6자리 + 회사코드 6자리 = 18자리
    if (Common.D) {
        Log.e(TAG, "::::::::: M9 (비정량 이마트) ::::::::");
        Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
        Log.d(TAG, "중량 6자리 :" + print_weight_str);
        Log.d(TAG, "회사코드 : " + pCompCode);
    }

    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode;
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;

    // pBarcode2: 변수 생성만, 인쇄 안 함 (M8과 동일 정책)
    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    break;
```

**변경 후:**

```java
case "M9":
    // 비정량 이마트 M9 (문의사항05 답변: 정량 이마트 우육 센터납 임시 사용 종료)
    // 상품코드 앞자리 6자리 + 중량 6자리 + 회사코드 6자리 = 18자리
    if (Common.D) {
        Log.e(TAG, "::::::::: M9 (비정량 이마트) ::::::::");
        Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
        Log.d(TAG, "중량 6자리 :" + print_weight_str);
        Log.d(TAG, "회사코드 : " + pCompCode);
    }

    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode;
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;

    break;
```

**제거 대상 (3줄):**
```java
    // pBarcode2: 변수 생성만, 인쇄 안 함 (M8과 동일 정책)
    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
```

**검증**: case "M9" 블록에 pBarcode/pBarcodeStr 할당만 남고, EMARTLOGIS_CODE 참조가 완전히 제거됨을 확인

---

### 4.2 case "M8" — pBarcode2/pBarcodeStr2 할당 제거

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/print/LabelPrintHelper.java`

**변경 전 (L667~683 전체):**

```java
case "M8":
    // 이마트 비정량 납품분
    // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
    if (Common.D) {
        Log.e(TAG, "::::::::: M8 ::::::::");
        Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
        Log.d(TAG, "중량 6자리 :" + print_weight_str);
        Log.d(TAG, "회사코드 : " + pCompCode);
        Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
    }

    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode ;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
    break;
```

**변경 후:**

```java
case "M8":
    // 이마트 비정량 납품분
    // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
    if (Common.D) {
        Log.e(TAG, "::::::::: M8 ::::::::");
        Log.d(TAG, "상품코드 full : " + si.getEMARTITEM_CODE() + ", 6 : " + si.getEMARTITEM_CODE().substring(0, 6));
        Log.d(TAG, "중량 6자리 :" + print_weight_str);
        Log.d(TAG, "회사코드 : " + pCompCode);
        Log.d(TAG, "수입식별번호 : " + si.getIMPORT_ID_NO());
    }

    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
    break;
```

**제거 대상 (2줄):**
```java
    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode ;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
```

**검증**: case "M8" 블록에 pBarcode/pBarcodeStr 할당만 남고, EMARTLOGIS_CODE 참조가 완전히 제거됨을 확인

---

## 5. 사이드이펙트

### 5.1 pBarcode2 인쇄 분기 (L799~812) — 영향 없음

```java
// M3, M4 추가 바코드 출력
if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3)) {
    labelData.write(slcsBarcode(70, 205, 60, pBarcode2).getBytes("EUC-KR"));
} else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
    labelData.write(slcsBarcode(145, 205, 60, pBarcode2).getBytes("EUC-KR"));
}
```

- M3/M4 case에서 pBarcode2는 여전히 case 블록에서 값이 할당되므로 영향 없음
- M8/M9는 이 분기에 포함되지 않으므로 변경 전/후 모두 pBarcode2("")가 전달되지 않음

### 5.2 다른 case(M0/M1/M3/M4/E0~E3) — 영향 없음

| case | pBarcode2 할당 여부 | 비고 |
|:----:|:-----------------:|------|
| M0 | 있음 (L518) | EMARTLOGIS_CODE 참조, 이번 작업 대상 아님 |
| M1 | 있음 | 이번 작업 대상 아님 |
| M3 | 있음 | 인쇄 분기 포함, 이번 작업 대상 아님 |
| M4 | 있음 | 인쇄 분기 포함, 이번 작업 대상 아님 |
| E0~E3 | 있음 | 이번 작업 대상 아님 |
| **M9** | **제거** | 인쇄 분기 미포함 확인 |
| **M8** | **제거** | 인쇄 분기 미포함 확인 |

### 5.3 부수 효과 — EMARTLOGIS_CODE 예외 위험 자동 해소

- M9 case: `EMARTLOGIS_CODE.substring(0, 6)` 호출이 제거됨으로써 해당 필드가 빈 문자열이더라도 `StringIndexOutOfBoundsException` 발생 불가 — 오류 21번(`app/doc/오류/21_비정량_이마트_EMARTLOGIS_CODE_빈문자열_라벨출력_예외.md`)과 동일 패턴의 위험이 M9에서 자동 해소됨
- M8 case: 동일하게 EMARTLOGIS_CODE 참조 제거로 위험 자동 해소

### 5.4 메서드 상단 초기 선언 (L454~455) — 유지

```java
String pBarcode2 = "";
String pBarcodeStr2 = "";
```

- 다른 case들(M0/M3/M4 등)에서 계속 사용하므로 이 선언은 유지한다
- M8/M9 case 이후 인쇄 분기에서 M8/M9가 진입하면 pBarcode2는 빈 문자열("") 상태를 유지 — 인쇄 분기에 M8/M9가 없으므로 동작에 영향 없음

---

## 6. 데이터 저장 구조

### 변수 매핑

| 변수 | 타입 | 현재 M8/M9 용도 | 변경 후 M8/M9 상태 |
|------|------|:--------------:|:----------------:|
| `pBarcode` | String | 라벨 메인 바코드 (인쇄됨) | 동일 (유지) |
| `pBarcodeStr` | String | 바코드 숫자 텍스트 (인쇄됨) | 동일 (유지) |
| `pBarcode2` | String | 할당만 됨, 인쇄 안 됨 (M8/M9) | 빈 문자열("") 유지 |
| `pBarcodeStr2` | String | 할당만 됨, 인쇄 안 됨 (M8/M9) | 빈 문자열("") 유지 |

### pBarcode2 사용처 요약 (setPrinting 메서드 전체)

```
pBarcode2 인쇄 분기 (L799~812):
    if BARCODE_TYPE == M3  → slcsBarcode(..., pBarcode2) 인쇄  [영향 없음]
    if BARCODE_TYPE == M4  → slcsBarcode(..., pBarcode2) 인쇄  [영향 없음]
    if BARCODE_TYPE == M3  → slcsBitmapText(..., pBarcodeStr2 + "  PC출하") 인쇄  [영향 없음]
    if BARCODE_TYPE == M4  → slcsBitmapText(..., pBarcodeStr2 + "  PC출하") 인쇄  [영향 없음]

M8/M9는 위 분기 어디에도 포함되지 않음
→ 변수 할당 제거 후 빈 문자열 상태이더라도 결과 동일
```

---

## 7. 호출 시점

```
사용자: PDA에서 비정량 이마트 품목 계근 후 라벨 인쇄 버튼 누름
    ↓
BixolonShipmentActivity (비정량 이마트, searchType=4)
    ↓
LabelPrintHelper.setPrinting(si, weight_double, reprint, callback)
    ↓
switch (si.getBARCODE_TYPE()):
    case "M8":  ← Step 2 수정 대상 (실운영 비정량 이마트)
        pBarcode 할당 → 라벨 인쇄
        pBarcode2 할당 [제거] → 인쇄 분기 없음
    case "M9":  ← Step 1 수정 대상 (비정량 이마트 M9, 개발43에서 재정의)
        pBarcode 할당 → 라벨 인쇄
        pBarcode2 할당 [제거] → 인쇄 분기 없음
    ↓
인쇄 분기 (M3/M4만 pBarcode2 인쇄, M8/M9는 해당 없음)
    ↓
callback.sendData(labelData) → Bixolon 프린터 전송
```

---

## 8. 개발 플랜

### Step 1: case "M9" pBarcode2/pBarcodeStr2 변수 할당 제거

**Part 1. 분석**
- 메서드: `LabelPrintHelper.setPrinting()`
- 범위: `LabelPrintHelper.java` L648~665 (case "M9" 블록)
- 용도: 비정량 이마트 M9 라벨 출력 시 미사용 pBarcode2 할당 제거
- 주의할 점: `pBarcode`/`pBarcodeStr` 할당(L658~659)은 절대 건드리지 않는다. EMARTLOGIS_CODE를 참조하는 2줄 + 관련 주석 1줄만 제거한다.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 제거 대상 주석 | L661 | `// pBarcode2: 변수 생성만, 인쇄 안 함 (M8과 동일 정책)` |
| 2 | 제거 대상 코드 | L662 | `pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + ...` |
| 3 | 제거 대상 코드 | L663 | `pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + ...` |
| 4 | 유지 대상 | L658~659 | `pBarcode`, `pBarcodeStr` 할당 — 인쇄됨, 변경 금지 |

**Part 2. 변환 계획**
- 변환 방식: L661~663 3줄(주석 포함) 삭제. 나머지 case "M9" 블록 내용은 그대로 유지.
- 주의사항: 삭제 후 case "M9" 블록 구조(break; 포함)가 유지되는지 확인. 인접 case "M8"에 영향이 없는지 확인.

**체크리스트**
- [x] Part 1: L661~663 정확한 라인 위치 확인
- [x] Part 2: 삭제 후 case "M9" 블록 구조(break; 포함) 정상 유지 확인
- [x] Part 3: 코드 삭제 수행
- [x] Part 4: 컴파일 BUILD SUCCESSFUL 확인
- [x] Part 5: case "M9" 단위 — pBarcode 할당값이 정상인지 로그 확인
- [x] Part 6: 회귀테스트 — M3/M4 case pBarcode2 인쇄 분기에 영향 없음 확인

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**: case "M9" 블록 내 pBarcode2/pBarcodeStr2 할당 2줄 + 관련 주석 1줄(총 3줄) 제거
- **왜**: 해당 할당은 인쇄 분기(M3/M4 전용)에 포함되지 않아 실제 라벨에 출력되지 않는 미사용 코드임. EMARTLOGIS_CODE가 빈 문자열일 경우 substring(0,6) 에서 StringIndexOutOfBoundsException 발생 위험(오류 21번 동일 패턴)이 존재하므로 제거
- **어떻게**: L661~663 (주석 + pBarcode2 할당 + pBarcodeStr2 할당) 3줄 삭제. pBarcode/pBarcodeStr 할당 및 break; 는 유지. ⑤ code-verifier BUILD SUCCESSFUL (11s) + ⑥ original-comparator PASS 확인

---

### Step 2: case "M8" pBarcode2/pBarcodeStr2 변수 할당 제거

**Part 1. 분석**
- 메서드: `LabelPrintHelper.setPrinting()`
- 범위: `LabelPrintHelper.java` L667~683 (case "M8" 블록)
- 용도: 이마트 비정량 M8 라벨 출력 시 미사용 pBarcode2 할당 제거
- 주의할 점: `pBarcode`/`pBarcodeStr` 할당(L678~679)은 절대 건드리지 않는다. EMARTLOGIS_CODE를 참조하는 2줄만 제거한다.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 제거 대상 코드 | L681 | `pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + ...` |
| 2 | 제거 대상 코드 | L682 | `pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + ...` |
| 3 | 유지 대상 | L678~679 | `pBarcode`, `pBarcodeStr` 할당 — 인쇄됨, 변경 금지 |
| 4 | 유지 대상 | L683 | `break;` |

**Part 2. 변환 계획**
- 변환 방식: L681~682 2줄 삭제. 나머지 case "M8" 블록 내용은 그대로 유지.
- 주의사항: 삭제 후 case "M8"이 마지막 case이므로 break; 뒤에 닫는 `}` (switch 종료)가 정상인지 확인.

**체크리스트**
- [x] Part 1: L681~682 정확한 라인 위치 확인
- [x] Part 2: 삭제 후 switch 블록 구조 정상 유지 확인
- [x] Part 3: 코드 삭제 수행
- [x] Part 4: 컴파일 BUILD SUCCESSFUL 확인
- [x] Part 5: case "M8" 단위 — pBarcode 할당값이 정상인지 로그 확인
- [x] Part 6: 회귀테스트 — M3/M4 case pBarcode2 인쇄 분기에 영향 없음 확인

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**: case "M8" 블록 내 pBarcode2/pBarcodeStr2 할당 2줄 제거
- **왜**: 인쇄 분기(M3/M4 전용)에 M8이 포함되지 않아 실제 출력되지 않는 미사용 할당. EMARTLOGIS_CODE 빈 문자열 시 substring 예외 위험(오류 21번 동일 패턴) 자동 해소 목적
- **어떻게**: L681~682 (pBarcode2 할당 + pBarcodeStr2 할당) 2줄 삭제. pBarcode/pBarcodeStr 할당 및 break; 는 유지. ⑤ code-verifier BUILD SUCCESSFUL (11s) + ⑥ original-comparator PASS 확인

---

### Step 3: 통합 테스트

**Part 1. 분석**
- 메서드: `LabelPrintHelper.setPrinting()` 전체 실행 흐름
- 범위: M8 라벨 출력, M9 라벨 출력, M3/M4 라벨 출력(회귀)
- 용도: Step 1~2 코드 변경 이후 동작 동일성 최종 확인
- 주의할 점: M3/M4 라벨에서 pBarcode2가 정상 인쇄되는지 반드시 확인 — 이번 변경이 M3/M4에 영향을 주지 않아야 한다

**Part 2. 변환 계획**
- 변환 방식: PDA 실기 테스트 + ⑤ code-verifier + ⑥ original-comparator 검증
- 주의사항: 비정량 M9는 개발43에서 신규 추가된 타입이므로 실기 테스트 시 M9 품목 데이터 필요

**체크리스트**
- [x] Part 1: 컴파일 BUILD SUCCESSFUL
- [x] Part 2: ⑤ code-verifier 검증 PASS
- [x] Part 3: ⑥ original-comparator 검증 PASS (M8 동작 동일 확인)
- [x] Part 4: PDA 실기 — M8 라벨 출력 정상 (기존과 동일, pBarcode만 인쇄)
- [x] Part 5: PDA 실기 — M9 라벨 출력 정상 (18자리 바코드만 인쇄, pBarcode2 미인쇄 동일)
- [x] Part 6: PDA 실기 — M3/M4 라벨 출력 정상 (pBarcode2 인쇄 유지, 회귀 없음)

**Part 6. 변경 내용**:
- **무엇을**: Step 1~2(M9/M8 pBarcode2 할당 제거) 코드 변경 이후 PDA 실기 테스트로 동작 동일성 최종 확인
- **왜**: EMARTLOGIS_CODE 빈 문자열 예외 위험 제거 후 M8/M9 라벨 출력이 기존과 동일하게 동작하는지, M3/M4 pBarcode2 인쇄 분기에 회귀가 없는지 실기로 검증
- **어떻게**: PDA 실기 테스트 — M8 라벨(pBarcode만 인쇄 확인), M9 라벨(18자리 바코드 인쇄 + pBarcode2 미인쇄 확인), M3/M4 라벨(pBarcode2 정상 인쇄 회귀 확인) 3종 시나리오 모두 PASS

---

### 개발 순서 요약

```
Step 1: case "M9" pBarcode2/pBarcodeStr2 변수 할당 제거 (L661~663)
    ↓
Step 2: case "M8" pBarcode2/pBarcodeStr2 변수 할당 제거 (L681~682)
    ↓
Step 3: 통합 테스트 (컴파일 + code-verifier + 실기)
```

---

## 9. 테스트 시나리오

### 시나리오 1: M8 라벨 출력 (기존 동일)

```
1. PDA 앱 실행 → 비정량 이마트(searchType=4) 진입
2. BARCODE_TYPE=M8 품목을 출하대상 목록에서 선택
3. 바코드 스캔 → 계근 → 라벨 인쇄 버튼
4. 라벨 출력 확인:
   - pBarcode (EMARTITEM_CODE(6) + 중량(6) + 회사코드(6) + 수입식별번호(12) = 30자리) 인쇄 확인
   - pBarcode2 미인쇄 확인 (기존과 동일)
   - 이상 없으면 PASS
```

### 시나리오 2: M9 라벨 출력 (개발43 기준)

```
1. PDA 앱 실행 → 비정량 이마트(searchType=4) 진입
2. BARCODE_TYPE=M9 품목을 출하대상 목록에서 선택
3. 바코드 스캔 → 계근 → 라벨 인쇄 버튼
4. 라벨 출력 확인:
   - pBarcode (EMARTITEM_CODE(6) + 중량(6) + 회사코드(6) = 18자리) 인쇄 확인
   - pBarcode2 미인쇄 확인 (Step 1 변경 후 빈 문자열 상태)
   - 이상 없으면 PASS
```

### 시나리오 3: M3/M4 라벨 회귀 테스트

```
1. PDA 앱 실행 → 정량 이마트(searchType=0) 진입
2. BARCODE_TYPE=M3 또는 M4 품목 선택
3. 계근 → 라벨 인쇄
4. 라벨 출력 확인:
   - pBarcode (1번 바코드) 정상 인쇄 확인
   - pBarcode2 (2번 바코드, PC매입) 정상 인쇄 확인 ← 핵심 회귀 항목
   - pBarcodeStr2 + "  PC출하" 텍스트 정상 인쇄 확인
   - 이상 없으면 PASS (Step 1~2 변경이 M3/M4에 영향 없음 확인)
```

### 시나리오 4: pBarcode2 빈 문자열 상태 검증

```
1. Step 1~2 완료 후 빌드
2. logcat에서 M8 라벨 출력 시 pBarcode2 로그 확인:
   Log.i(TAG, "===============pBarcode2============" + pBarcode2)
   → 출력값이 "" (빈 문자열) 임을 확인
3. 인쇄 분기(L799~812) M8 진입 없음 확인 → 빈 문자열이 프린터에 전송되지 않음
4. 이상 없으면 PASS
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | M8 case에 pBarcode2와 관련된 다른 코드가 있을 수 있음 | case 블록 내 pBarcode2를 참조하는 추가 코드 존재 가능성 | Step 실행 전 Grep으로 case "M8" 블록 내 pBarcode2 참조 전수 확인 |
| 2 | M9 case의 pBarcodeStr3 등 다른 변수와 혼동 | 개발43에서 M9 재정의 시 여러 변수가 추가됨 | 삭제 전 case "M9" 블록 전체를 Read로 정확히 확인하고 pBarcode2 2줄만 제거 |
| 3 | 컴파일 후 M3/M4 라벨에서 pBarcode2 미인쇄 현상 | 메서드 상단 초기 선언을 잘못 제거한 경우 | 메서드 상단 `String pBarcode2 = "";` 선언(L454) 유지 확인 |
| 4 | EMARTLOGIS_CODE 예외가 다른 경로로 발생 | M0 case(L518)에 동일 패턴 존재 | 이번 작업 범위는 M8/M9만. M0는 별도 오류 문서(21번) 참조 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | case "M9" pBarcode2/pBarcodeStr2 변수 할당 제거 (L661~663) | ✅ 완료 |
| 2 | case "M8" pBarcode2/pBarcodeStr2 변수 할당 제거 (L681~682) | ✅ 완료 |
| 3 | 통합 테스트 | ✅ 완료 |

---

## 관련 문서

- `app/doc/문의사항/03_M8_비정량_이마트_pBarcode2_생성_미출력_의도.md` — 자체 검증 근거 및 질문 원문 (답변 미수신, 사용자 명시 결정으로 진행)
- `app/doc/개발/43_비정량_이마트_M9_바코드_신규_추가.md` — case "M9" 비정량 재정의(Step 2에서 pBarcode2 할당 추가된 출처)
- `app/doc/오류/21_비정량_이마트_EMARTLOGIS_CODE_빈문자열_라벨출력_예외.md` — EMARTLOGIS_CODE 빈 문자열 substring 예외 (이번 변경으로 M8/M9에서 자동 해소)
- `app/doc/소스분석/51_비정량_pBarcode2_사용여부_및_바코드정보조회_데이터흐름_분석.md` — Q1 pBarcode2 미인쇄 의도 분석 근거

---

**문서 버전**: 1.0
