# LabelPrintHelper 분리 계획

**작성일**: 2026-02-04
**대상 파일**: `BixolonShipmentActivity.java`
**신규 파일**: `LabelPrintHelper.java`


## 원칙
1. 무조건 기존과 동일한 기능으로 동작해야 한다.
2. step별 단위테스트로 합/불합 여부를 정한다.
3. 모든 step 완료 후 통합테스트로 최종 합/불합 여부를 정한다.
4. 임의로 개발을 진행하면 안 된다.
5. 나에게 최우선적으로 말을 하고, 내 지시에 따라서만 진행한다.
6. 모든 개발은 문서화 작업을 먼저 진행 후 확인을 받는다.
   7. 단계별 진행 사항을 알기 위해 체크박스를 만든다.
   8. 각 단계마다 확인을 받은 후 다음 단계로 넘어간다.
   9. 완료된 단계는 체크박스를 체크한다.
   10. 개발 진행 전 분석을 진행한다. 그리고 분석글 및 변환 계획을 작성하라
   11. "무엇을, 왜, 어떻게" 변경했는지 명확히 명시한다.
   12. 각 step 완료 시 단위테스트를 진행한다.
   13. 이전 step에 대한 회귀테스트도 함께 진행한다.
    - 예외: step 특성상 오류가 불가피한 경우, 원인/해결 시점을 문서에 기록한다.
14. 주석은 유지보수를 위해 자세하게 작성한다.
15. 모든 진행은 step by step으로 확인 및 재확인하여 정확성을 높인다.

---

## 개요

BixolonShipmentActivity.java (4,581줄)에서 라벨 출력 관련 메소드를 별도 클래스로 분리하여 유지보수성을 향상시킨다.

---

## Part 1. 분석

### 1.1 분리 대상 메소드

| #   | 메서드                     | 범위        | 줄 수        | 용도            |
| --- | ----------------------- | --------- | ---------- | ------------- |
| 1   | `setPrinting_prod()`    | 1948-2034 | 87줄        | 생산 라벨 출력      |
| 2   | `setPrinting()`         | 2071-2730 | 660줄       | 이마트/비정량 라벨 출력 |
| 3   | `setHomeplusPrinting()` | 2751-2858 | 108줄       | 홈플러스 라벨 출력    |
| 4   | `setPrintingLotte()`    | 2883-3145 | 263줄       | 롯데 라벨 출력      |
| 5   | `slcsInit()`            | 3158-3160 | 3줄         | SLCS 초기화 명령   |
| 6   | `slcsLabelSize()`       | 3169-3171 | 3줄         | 라벨 크기 설정      |
| 7   | `slcsText()`            | 3184-3188 | 5줄         | 텍스트 출력 명령     |
| 8   | `slcsBarcode()`         | 3199-3203 | 5줄         | 바코드 출력 명령     |
| 9   | `slcsLine()`            | 3215-3218 | 4줄         | 선 출력 명령       |
| 10  | `slcsBox()`             | 3230-3233 | 4줄         | 박스 출력 명령      |
| 11  | `slcsPrint()`           | 3241-3243 | 3줄         | 인쇄 실행 명령      |
| 12  | `slcsFeedToMark()`      | 3252-3254 | 3줄         | 마크 피드 명령      |
|     | **합계**                  |           | **1,148줄** |               |

**분리 범위 요약**
- 메소드 본문만: 1,148줄
- 주석 포함: 약 1,306줄
- 전체 분리 범위 (1936-3254): **1,319줄**

### 1.2 분리 대상 상수

라벨 출력 메소드(1948-3254줄) 내에서 실제 사용되는 상수를 LabelPrintHelper에 **복사**한다.

> **주의**: 이 상수들은 라벨 출력 범위 밖에서도 사용되므로 BixolonShipmentActivity에서 **삭제하지 않고 유지**한다.

```java
// searchType (라벨 출력에서 사용하는 것만)
private static final String SEARCH_TYPE_EMART = "0";   // 이마트 출하 (2119줄)
private static final String SEARCH_TYPE_LOTTE = "6";   // 롯데 출하 (2906줄)

// 회사 정보 (169-170줄)
private static final String COMPANY_CODE = "610933";                  // 2164줄
private static final String COMPANY_NAME = "(주)하이랜드이노베이션";  // 2165, 2450, 2762, 2842, 2890줄

// 미트센터 정보 (173-176줄)
private static final String MEAT_CENTER_CODE = "059015";       // 2629, 2680줄
private static final String MEAT_CENTER_STORE_CODE = "9231";   // 2079, 2620, 2673줄
private static final String KILKOY_PACKER_CODE = "30228";      // 2079줄
private static final String LOGIS_CODE_DEFAULT = "0000000";    // 2620, 2673줄

// 바코드 타입 (182-192줄)
private static final String BARCODE_TYPE_M0 = "M0";
private static final String BARCODE_TYPE_M1 = "M1";
private static final String BARCODE_TYPE_M3 = "M3";
private static final String BARCODE_TYPE_M4 = "M4";
private static final String BARCODE_TYPE_M8 = "M8";
private static final String BARCODE_TYPE_M9 = "M9";
private static final String BARCODE_TYPE_E0 = "E0";
private static final String BARCODE_TYPE_E1 = "E1";
private static final String BARCODE_TYPE_E2 = "E2";
private static final String BARCODE_TYPE_E3 = "E3";
private static final String BARCODE_TYPE_P0 = "P0";

// 아이템 타입 (195-199줄)
private static final String ITEM_TYPE_W = "W";    // 바코드 계근 (2187줄)
private static final String ITEM_TYPE_HW = "HW";  // 바코드 계근 확장 (2187줄)
private static final String ITEM_TYPE_S = "S";    // 저울 계근 (2918줄)
private static final String ITEM_TYPE_J = "J";    // 지정 중량 (2195, 2926줄)
private static final String ITEM_TYPE_B = "B";    // 홈플러스 비정량 (2809, 2810줄)

// 센터명 (202-204줄)
private static final String CENTER_NAME_TRD = "TRD";  // 2120줄
private static final String CENTER_NAME_WET = "WET";  // 2120줄
private static final String CENTER_NAME_ET = "E/T";   // 2120, 2414, 2415줄
```

**상수 총 27개** (2 + 2 + 4 + 11 + 5 + 3)

※ 제외된 상수 (라벨 출력 범위 밖에서만 사용):
- SEARCH_TYPE_PRODUCTION, HOMEPLUS, WHOLESALE, NONFIXED, HOMEPLUS_NONFIXED, PRODUCTION_LABEL (6개)

### 1.3 의존성 분석

#### 1.3.1 멤버 변수 참조

| 변수                      | 타입                        | 사용 위치                   | 처리 방법    |
| ----------------------- | ------------------------- | ----------------------- | -------- |
| `work_item_bi_info`     | Barcodes_Info             | 2084, 2125줄             | 파라미터로 전달 |
| `arSM`                  | ArrayList<Shipments_Info> | 2111, 2151줄             | 파라미터로 전달 |
| `current_work_position` | int                       | 2111, 2151줄             | 파라미터로 전달 |
| `edit_barcode`          | EditText                  | 2026, 2722, 2850, 3137줄 | 콜백으로 처리  |
| `TAG`                   | String                    | 156줄에서 정의               | 클래스 내 정의 |

##### 멤버 변수 데이터 흐름 (요약)

| 변수                      | 설명                                | 용도                  |
| ----------------------- | --------------------------------- | ------------------- |
| `arSM`                  | 출하대상 목록 (서버 VIEW → 로컬 DB → 조회)    | 전체 계근 대상 리스트        |
| `current_work_position` | arSM 리스트 인덱스 (-1: 미선택, 0~n: 작업 중) | 현재 작업 항목 지정         |
| `work_item_bi_info`     | 바코드 파싱 정보 (서버 → 로컬 DB → 매칭)       | 유통기한(SHELF_LIFE) 조회 |
| `edit_barcode`          | UI 입력 필드 (바코드/중량 겸용)              | 라벨 출력 후 초기화 (콜백 처리) |

**사용 패턴**: `arSM.get(current_work_position)` → 현재 작업 중인 출하대상 정보

**상세 내용**: [08_출하대상_데이터흐름.md](../기능/08_출하대상_데이터흐름.md) 참조

#### 1.3.2 메소드 참조

| 메소드 | 용도 | 사용 횟수 | 처리 방법 |
|--------|------|----------|----------|
| `sendData(byte[])` | 프린터로 데이터 전송 | 6회 | 콜백 인터페이스로 추상화 |
| `Log.d()` / `Log.e()` | 디버그 로그 | 126회 | android.util.Log 직접 사용 |

#### 1.3.3 외부 클래스 참조

| 클래스                 | 용도          | 사용 위치                   | 처리 방법           |
| ------------------- | ----------- | ----------------------- | --------------- |
| `Common.searchType` | 마트사 구분      | 2119, 2906줄             | 파라미터로 전달        |
| `Common.D`          | 디버그 플래그     | 1950, 2029, 2072줄 등 다수  | 직접 참조 유지        |
| `Shipments_Info`    | 출하대상 정보     | 파라미터 타입                 | 파라미터로 전달        |
| `Barcodes_Info`     | 바코드 정보      | work_item_bi_info 타입    | 파라미터로 전달        |
| `SimpleDateFormat`  | 날짜 포맷       | 1979, 2088, 2128줄       | java.text 직접 사용 |
| `Calendar`          | 날짜 계산       | 2089, 2100, 2129, 2140줄 | java.util 직접 사용 |
| `StringBuilder`     | SLCS 명령어 조합 | 6곳 (1991, 2431, 2624줄 등) | java.lang 직접 사용 |

### 1.4 주의할 점

1. **기존 동작 100% 유지 필수**: 라벨 출력 결과가 기존과 완전히 동일해야 함
2. **프린터 연결 유지**: sendData()는 Activity에서 관리하는 프린터 연결 사용
3. **UI 처리**: edit_barcode.setText("")는 Activity에서 처리
4. **예외 처리**: 기존 try-catch 로직 그대로 유지

### 1.5 주석 작성 지침 (원칙 14)

분리된 코드에 다음 주석을 반드시 작성한다:

#### 1.5.1 클래스 주석
```java
/**
 * 라벨 출력 헬퍼 클래스
 * <p>
 * BixolonShipmentActivity에서 분리된 라벨 출력 관련 메소드 모음.
 * SLCS 명령어를 사용하여 Bixolon 프린터로 바코드 라벨을 출력한다.
 * </p>
 *
 * <h3>지원 마트사</h3>
 * <ul>
 *   <li>이마트 (searchType "0")</li>
 *   <li>도매 비정량 (searchType "4")</li>
 *   <li>홈플러스 비정량 (searchType "5")</li>
 *   <li>롯데 (searchType "6")</li>
 *   <li>생산 라벨 (searchType "7")</li>
 * </ul>
 *
 * @author Highland Innovation
 * @since 2026-02-04
 * @see BixolonShipmentActivity
 */
```

#### 1.5.2 메소드 주석
- 각 메소드에 용도, 파라미터, 반환값 설명
- 바코드 타입별 분기 로직 설명
- 특수 처리 케이스 (미트센터, 수입육 등) 명시

#### 1.5.3 상수 주석
- 각 상수 그룹에 용도 설명
- 값의 의미 명시 (예: "0" = 이마트 출하)

#### 1.5.4 인라인 주석
- 복잡한 계산식에 설명 추가
- SLCS 명령어에 원본 Woosim 명령어 대응 표시

---

## Part 2. 변환 계획

### 2.1 신규 클래스 구조

```
com.rgbsolution.highland_emart.print/
└── LabelPrintHelper.java
    ├── [상수] 회사정보, 바코드타입, 아이템타입 등 (27개)
    ├── [인터페이스] PrinterCallback
    ├── [SLCS] slcsInit, slcsLabelSize, slcsText, slcsBarcode, slcsLine, slcsBox, slcsPrint, slcsFeedToMark (8개)
    ├── [라벨] setPrinting_prod()
    ├── [라벨] setPrinting()
    ├── [라벨] setHomeplusPrinting()
    └── [라벨] setPrintingLotte()
```

### 2.2 인터페이스 설계

```java
public interface PrinterCallback {
    /**
     * 프린터로 데이터 전송
     * @param data SLCS 명령어 바이트 배열
     */
    void sendData(byte[] data);

    /**
     * 바코드 입력창 초기화
     */
    void clearBarcodeInput();
}
```

### 2.3 메소드 시그니처 변경

#### 2.3.1 setPrinting_prod() - 변경 최소

```java
// 기존
public String setPrinting_prod(double weight_double, Shipments_Info si, boolean reprint)

// 변경 후
public String setPrinting_prod(
    double weight_double,
    Shipments_Info si,
    boolean reprint,
    PrinterCallback callback              // 추가
)
```

#### 2.3.2 setPrinting() - 파라미터 추가

```java
// 기존
public String setPrinting(double weight_double, Shipments_Info si, boolean reprint, String making_date)

// 변경 후
public String setPrinting(
    double weight_double,
    Shipments_Info si,
    boolean reprint,
    String making_date,
    Barcodes_Info barcodeInfo,           // work_item_bi_info (2084, 2125줄)
    Shipments_Info currentWorkItem,      // arSM.get(current_work_position) (2111, 2151줄)
    String searchType,                   // Common.searchType (2119줄)
    PrinterCallback callback             // 추가
)
```

#### 2.3.3 setHomeplusPrinting() - 변경 최소

```java
// 기존
public String setHomeplusPrinting(double weight_double, Shipments_Info si, boolean reprint)

// 변경 후
public String setHomeplusPrinting(
    double weight_double,
    Shipments_Info si,
    boolean reprint,
    PrinterCallback callback              // 추가
)
```

#### 2.3.4 setPrintingLotte() - searchType 추가

```java
// 기존
public String setPrintingLotte(double weight_double, Shipments_Info si, boolean reprint, String making_date, String box_order)

// 변경 후
public String setPrintingLotte(
    double weight_double,
    Shipments_Info si,
    boolean reprint,
    String making_date,
    String box_order,
    String searchType,                   // Common.searchType (2906줄)
    PrinterCallback callback             // 추가
)
```

#### 2.3.5 시그니처 변경 요약

| 메소드 | 기존 파라미터 | 추가 파라미터 | 변경 규모 |
|--------|---------------|---------------|-----------|
| `setPrinting_prod()` | 3개 | callback | 최소 |
| `setPrinting()` | 4개 | barcodeInfo, currentWorkItem, searchType, callback | 대규모 |
| `setHomeplusPrinting()` | 3개 | callback | 최소 |
| `setPrintingLotte()` | 5개 | searchType, callback | 중규모 |

### 2.4 호출 방식 변경

#### 2.4.1 setPrinting_prod() 호출

```java
// 기존
setPrinting_prod(weight_double, arSM.get(current_work_position), false);

// 변경 후
labelPrintHelper.setPrinting_prod(
    weight_double,
    arSM.get(current_work_position),
    false,
    printerCallback
);
```

#### 2.4.2 setPrinting() 호출

```java
// 기존
setPrinting(weight_double, arSM.get(select_position), true, making_date);

// 변경 후
labelPrintHelper.setPrinting(
    weight_double,
    arSM.get(select_position),
    true,
    making_date,
    work_item_bi_info,
    arSM.get(current_work_position),
    Common.searchType,
    printerCallback
);
```

#### 2.4.3 setHomeplusPrinting() 호출

```java
// 기존
setHomeplusPrinting(weight_double, arSM.get(current_work_position), false);

// 변경 후
labelPrintHelper.setHomeplusPrinting(
    weight_double,
    arSM.get(current_work_position),
    false,
    printerCallback
);
```

#### 2.4.4 setPrintingLotte() 호출

```java
// 기존
setPrintingLotte(weight_double, arSM.get(current_work_position), false, making_date, lotteBoxOrder);

// 변경 후
labelPrintHelper.setPrintingLotte(
    weight_double,
    arSM.get(current_work_position),
    false,
    making_date,
    lotteBoxOrder,
    Common.searchType,
    printerCallback
);
```

### 2.5 파일 구조 변경

| 변경 전 | 변경 후 |
|---------|---------|
| BixolonShipmentActivity.java (4,581줄) | BixolonShipmentActivity.java (~3,370줄) |
| - | LabelPrintHelper.java (~1,350줄) |

**삭제 범위 계산**:
- 라벨 출력 메소드 4개: 1,118줄 (87+660+108+263)
- SLCS 메소드 8개: ~97줄
- 합계: ~1,215줄 삭제
- 상수 27개: **삭제하지 않음** (라벨 출력 범위 밖에서 사용)

※ LabelPrintHelper: 복사 대상 + 인터페이스/import/클래스 선언 추가로 약 1,350줄 예상

---

## Part 3. 세부 작업 단계

### Step 1: LabelPrintHelper.java 파일 생성

#### 작업
- [x] 패키지 생성: `com.rgbsolution.highland_emart.print` (기존 존재)
- [x] 클래스 생성: `LabelPrintHelper.java`
- [x] PrinterCallback 인터페이스 정의

#### 테스트
- [x] 파일 존재 확인 (`app/src/main/java/com/rgbsolution/highland_emart/print/LabelPrintHelper.java`)
- [x] 패키지 선언 확인 (`package com.rgbsolution.highland_emart.print;`)
- [x] PrinterCallback 인터페이스 존재 확인
- [x] sendData(byte[] data) 메소드 선언 확인
- [x] clearBarcodeInput() 메소드 선언 확인

---

### Step 2: 상수 복사 (27개)

> **주의**: 상수들이 라벨 출력 범위 밖에서도 사용되므로 "이동"이 아닌 "복사"로 진행한다.
> BixolonShipmentActivity의 상수는 유지하고, LabelPrintHelper에 동일 상수를 복사한다.

#### 작업
- [x] searchType 상수 복사 (2개: EMART, LOTTE)
- [x] 회사 정보 상수 복사 (2개: COMPANY_CODE, COMPANY_NAME)
- [x] 미트센터 정보 상수 복사 (4개: MEAT_CENTER_CODE, MEAT_CENTER_STORE_CODE, KILKOY_PACKER_CODE, LOGIS_CODE_DEFAULT)
- [x] 바코드 타입 상수 복사 (11개: M0~M9, E0~E3, P0)
- [x] 아이템 타입 상수 복사 (5개: W, HW, S, J, B)
- [x] 센터명 상수 복사 (3개: TRD, WET, ET)

#### 테스트 (코드 비교)
- [x] LabelPrintHelper에 상수 총 27개 존재 확인
- [x] searchType 상수 값 일치 확인 (EMART="0", LOTTE="6")
- [x] 회사 정보 상수 값 일치 확인 (COMPANY_CODE="610933", COMPANY_NAME="(주)하이랜드이노베이션")
- [x] 미트센터 상수 값 일치 확인 (MEAT_CENTER_CODE="059015", MEAT_CENTER_STORE_CODE="9231", KILKOY_PACKER_CODE="30228", LOGIS_CODE_DEFAULT="0000000")
- [x] 바코드 타입 상수 값 일치 확인 (M0, M1, M3, M4, M8, M9, E0, E1, E2, E3, P0)
- [x] 아이템 타입 상수 값 일치 확인 (W, HW, S, J, B)
- [x] 센터명 상수 값 일치 확인 (TRD, WET, E/T)
- [x] 상수 주석 존재 확인 (각 그룹별 용도 설명)
- [x] BixolonShipmentActivity의 원본 상수 유지 확인

---

### Step 3: SLCS 메소드 이동

#### 작업
- [x] slcsInit() 이동
- [x] slcsLabelSize() 이동
- [x] slcsText() 이동
- [x] slcsBarcode() 이동
- [x] slcsLine() 이동
- [x] slcsBox() 이동
- [x] slcsPrint() 이동
- [x] slcsFeedToMark() 이동

#### 테스트 (코드 비교)
- [x] slcsInit() - 초기화 명령어 일치 확인 ("CB\r\n" + "CS13,0\r\n")
- [x] slcsLabelSize(int width, int height) - 라벨 크기 명령어 일치 확인 ("SW" + width + "\r\n" + "SL" + height + "\r\n")
- [x] slcsText(int x, int y, int width, int height, String text) - 텍스트 명령어 일치 확인 (V 명령어)
- [x] slcsBarcode(int x, int y, int height, String data) - 바코드 명령어 일치 확인 (BD CODE128)
- [x] slcsLine(int x1, int y1, int x2, int y2, int width) - 선 명령어 일치 확인 (LS 명령어)
- [x] slcsBox(int x, int y, int width, int height, int thickness) - 박스 명령어 일치 확인 (LB 명령어)
- [x] slcsPrint(int copies) - 인쇄 명령어 일치 확인 ("P" + copies + "\r\n")
- [x] slcsFeedToMark() - 피드 명령어 일치 확인 ("T\r\n")
- [x] 모든 메소드 반환 타입 일치 확인 (**String**)

---

### Step 4-1: setPrinting() 이동 (660줄)

> 이마트/비정량 라벨 출력 메소드 (가장 복잡, 2071-2730줄)

#### 작업
- [x] setPrinting() 메소드 복사 (2071-2730줄)
- [x] 시그니처 변경 (barcodeInfo, currentWorkItem, searchType, callback 추가)
- [x] 멤버 변수 참조 → 파라미터로 변경
- [x] sendData() → callback.sendData() 변경
- [x] edit_barcode.setText("") → callback.clearBarcodeInput() 변경

#### 테스트 (코드 비교)
- [x] 메소드 본문 일치 확인 (2071-2730줄 → LabelPrintHelper)
- [x] 시그니처 변경 확인 (barcodeInfo, currentWorkItem, searchType, callback 추가)
- [x] 멤버 변수 참조 제거 확인 (work_item_bi_info → barcodeInfo 파라미터)
- [x] 멤버 변수 참조 제거 확인 (arSM.get(current_work_position) → currentWorkItem 파라미터)
- [x] 멤버 변수 참조 제거 확인 (Common.searchType → searchType 파라미터)
- [x] sendData() 호출 → callback.sendData() 변경 확인
- [x] edit_barcode.setText("") → callback.clearBarcodeInput() 변경 확인
- [x] 바코드 타입 분기 로직 일치 확인 (M0~M9, E0~E3, P0)
- [x] 미트센터/수입육 분기 로직 일치 확인

---

### Step 4-2: setPrinting_prod() 이동 (87줄)

> 생산 라벨 출력 메소드 (1948-2034줄)

#### 작업
- [x] setPrinting_prod() 메소드 복사 (1948-2034줄)
- [x] 시그니처 변경 (callback 추가)
- [x] sendData() → callback.sendData() 변경
- [x] edit_barcode.setText("") → callback.clearBarcodeInput() 변경

#### 테스트 (코드 비교)
- [x] 메소드 본문 일치 확인 (1948-2034줄 → LabelPrintHelper)
- [x] 시그니처 변경 확인 (callback 추가)
- [x] sendData() 호출 → callback.sendData() 변경 확인
- [x] edit_barcode.setText("") → callback.clearBarcodeInput() 변경 확인

---

### Step 4-3: setHomeplusPrinting() 이동 (108줄)

> 홈플러스 라벨 출력 메소드 (2751-2858줄)

#### 작업
- [ ] setHomeplusPrinting() 메소드 복사 (2751-2858줄)
- [ ] 시그니처 변경 (callback 추가)
- [ ] sendData() → callback.sendData() 변경
- [ ] edit_barcode.setText("") → callback.clearBarcodeInput() 변경

#### 테스트 (코드 비교)
- [ ] 메소드 본문 일치 확인 (2751-2858줄 → LabelPrintHelper)
- [ ] 시그니처 변경 확인 (callback 추가)
- [ ] sendData() 호출 → callback.sendData() 변경 확인
- [ ] edit_barcode.setText("") → callback.clearBarcodeInput() 변경 확인
- [ ] H5 바코드 포맷 로직 일치 확인

---

### Step 4-4: setPrintingLotte() 이동 (263줄)

> 롯데 라벨 출력 메소드 (2883-3145줄)

#### 작업
- [ ] setPrintingLotte() 메소드 복사 (2883-3145줄)
- [ ] 시그니처 변경 (searchType, callback 추가)
- [ ] 멤버 변수 참조 → 파라미터로 변경
- [ ] sendData() → callback.sendData() 변경
- [ ] edit_barcode.setText("") → callback.clearBarcodeInput() 변경

#### 테스트 (코드 비교)
- [ ] 메소드 본문 일치 확인 (2883-3145줄 → LabelPrintHelper)
- [ ] 시그니처 변경 확인 (searchType, callback 추가)
- [ ] 멤버 변수 참조 제거 확인 (Common.searchType → searchType 파라미터)
- [ ] sendData() 호출 → callback.sendData() 변경 확인
- [ ] edit_barcode.setText("") → callback.clearBarcodeInput() 변경 확인
- [ ] box_order 처리 로직 일치 확인

---

### Step 5: BixolonShipmentActivity 수정

#### 작업
- [ ] LabelPrintHelper 인스턴스 생성
- [ ] PrinterCallback 구현
- [ ] 라벨 출력 메소드 호출부 수정
- [ ] 기존 메소드/상수 삭제

#### 테스트 (코드 비교)

##### 인스턴스 및 콜백
- [ ] import 문 추가 확인 (com.rgbsolution.highland_emart.print.LabelPrintHelper)
- [ ] LabelPrintHelper 인스턴스 변수 선언 확인
- [ ] PrinterCallback 구현 확인
- [ ] callback.sendData() → 기존 sendData() 호출 확인
- [ ] callback.clearBarcodeInput() → edit_barcode.setText("") 확인

##### 호출부 수정
- [ ] setPrinting() 호출부 수정 확인 (2.4.2 형식)
- [ ] setPrinting_prod() 호출부 수정 확인 (2.4.1 형식)
- [ ] setHomeplusPrinting() 호출부 수정 확인 (2.4.3 형식)
- [ ] setPrintingLotte() 호출부 수정 확인 (2.4.4 형식)
- [ ] 호출 위치별 파라미터 정확성 확인

##### 삭제 확인
- [ ] 기존 setPrinting() 메소드 삭제 확인
- [ ] 기존 setPrinting_prod() 메소드 삭제 확인
- [ ] 기존 setHomeplusPrinting() 메소드 삭제 확인
- [ ] 기존 setPrintingLotte() 메소드 삭제 확인
- [ ] 기존 slcs* 메소드 8개 삭제 확인
- [ ] 상수 27개는 **삭제하지 않음** (라벨 출력 범위 밖에서도 사용되므로 유지)

---

### Step 6: 컴파일 확인

#### 작업
- [ ] Gradle 빌드 실행

#### 테스트 (컴파일)
- [ ] `./gradlew assembleDebug` 성공 확인
- [ ] 컴파일 경고 검토 (새로 발생한 경고 없음 확인)
- [ ] LabelPrintHelper.java 컴파일 성공 확인
- [ ] BixolonShipmentActivity.java 컴파일 성공 확인
- [ ] APK 파일 생성 확인 (`app/build/outputs/apk/debug/app-debug.apk`)

#### 컴파일 오류 시 체크포인트
- [ ] import 문 누락 확인
- [ ] 파라미터 타입 불일치 확인
- [ ] 접근 제어자 확인 (public/private)
- [ ] 반환 타입 일치 확인

---

### Step 7: 단위 테스트

각 마트사별 라벨 출력이 기존과 동일하게 동작하는지 검증한다.

#### 7.1 이마트 라벨 출력 테스트 (searchType "0")
- [ ] 바코드 타입 M0: 기본형 라벨 출력
- [ ] 바코드 타입 M1: 타입1 라벨 출력
- [ ] 바코드 타입 M3: 소비기한 포함 라벨 출력
- [ ] 바코드 타입 M4: 소비기한 포함 라벨 출력
- [ ] 바코드 타입 M8: 수입식별번호 포함 라벨 출력
- [ ] 바코드 타입 M9: 납품일자 포함 라벨 출력
- [ ] 바코드 타입 E0~E3: 이마트 확장 타입 라벨 출력
- [ ] 바코드 타입 P0: 기본 바코드 라벨 출력
- [ ] 미트센터 납품분 (킬코이, 스토어코드 9231) 소비기한 계산
- [ ] 수입육 센터 (TRD/WET/E/T) 소비기한 계산

#### 7.2 도매 비정량 라벨 출력 테스트 (searchType "4")
- [ ] 도매 비정량 라벨 출력
- [ ] 중량 표시 정확성 (소수점 2자리)

#### 7.3 홈플러스 라벨 출력 테스트 (searchType "5")
- [ ] 홈플러스 비정량 라벨 출력 (setHomeplusPrinting)
- [ ] H5 바코드 포맷 정확성
- [ ] 지점코드, 점포코드 표시

#### 7.4 롯데 라벨 출력 테스트 (searchType "6")
- [ ] 롯데 라벨 출력 (setPrintingLotte)
- [ ] 박스 순번 (box_order) 표시
- [ ] 제조일자 표시

#### 7.5 생산 라벨 출력 테스트 (searchType "7")
- [ ] 생산 라벨 출력 (setPrinting_prod)
- [ ] 상품명, 상품코드, 중량 표시

#### 7.6 공통 검증 항목
- [ ] sendData() 콜백 정상 호출
- [ ] clearBarcodeInput() 콜백 정상 호출
- [ ] EUC-KR 인코딩 정상 동작
- [ ] 프린터 출력 결과물이 기존과 동일

---

### Step 8: 회귀 테스트

분리 작업으로 인해 기존 기능에 영향이 없는지 검증한다.

#### 8.1 계근 입력 테스트
- [ ] 바코드 스캔 후 중량 추출 정상
- [ ] LB → KG 변환 정상 (× 0.453592)
- [ ] 무게 표시 정확성

#### 8.2 바코드 스캔 테스트
- [ ] ITEM_TYPE W (바코드 계근) 처리
- [ ] ITEM_TYPE HW (바코드 계근 확장) 처리
- [ ] ITEM_TYPE S (저울 계근) 처리
- [ ] ITEM_TYPE J (지정 중량) 처리
- [ ] ITEM_TYPE B (홈플러스 비정량) 처리

#### 8.3 데이터 처리 테스트
- [ ] 계근 데이터 DB 저장 정상
- [ ] 서버 전송 정상
- [ ] 완료 처리 정상

#### 8.4 UI 동작 테스트
- [ ] 라벨 출력 후 바코드 입력창 초기화
- [ ] 리스트 갱신 정상
- [ ] 토스트 메시지 표시 정상

---

### Step 9: 통합 테스트

전체 출하 프로세스가 정상 동작하는지 검증한다.

#### 9.1 End-to-End 테스트
- [ ] 로그인 → 마트사 선택 → 데이터 다운로드 → 바코드 스캔 → 계근 → 라벨 출력 → 서버 전송
- [ ] 각 마트사별 전체 프로세스 1회 이상 수행

#### 9.2 예외 상황 테스트
- [ ] 프린터 미연결 시 오류 처리
- [ ] 네트워크 오류 시 처리
- [ ] 잘못된 바코드 스캔 시 처리

---

## 체크리스트

### 분석 및 계획
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인

### 개발 단계
- [x] Step 1: 파일 생성 (LabelPrintHelper.java, PrinterCallback 인터페이스)
- [x] Step 2: 상수 복사 (27개)
- [x] Step 3: SLCS 메소드 이동 (8개)
- [x] Step 4-1: setPrinting() 이동 (660줄)
- [x] Step 4-2: setPrinting_prod() 이동 (87줄)
- [ ] Step 4-3: setHomeplusPrinting() 이동 (108줄)
- [ ] Step 4-4: setPrintingLotte() 이동 (263줄)
- [ ] Step 5: BixolonShipmentActivity 수정 (호출부, 콜백 구현)
- [ ] Step 6: 컴파일 오류 없음 확인

### 테스트 단계
- [ ] Step 7: 단위 테스트
  - [ ] 7.1 이마트 라벨 (10개 케이스)
  - [ ] 7.2 도매 비정량 라벨 (2개 케이스)
  - [ ] 7.3 홈플러스 라벨 (3개 케이스)
  - [ ] 7.4 롯데 라벨 (3개 케이스)
  - [ ] 7.5 생산 라벨 (2개 케이스)
  - [ ] 7.6 공통 검증 (4개 케이스)
- [ ] Step 8: 회귀 테스트
  - [ ] 8.1 계근 입력 (3개 케이스)
  - [ ] 8.2 바코드 스캔 (5개 케이스)
  - [ ] 8.3 데이터 처리 (3개 케이스)
  - [ ] 8.4 UI 동작 (3개 케이스)
- [ ] Step 9: 통합 테스트
  - [ ] 9.1 End-to-End (2개 케이스)
  - [ ] 9.2 예외 상황 (3개 케이스)

### 완료 단계
- [ ] 주석 보강 (1.5 주석 작성 지침 준수)
- [ ] Part 8: 변경 내용 작성

---

## Part 8. 변경 내용 (완료 후 작성)

- **무엇을**: BixolonShipmentActivity.java의 라벨 출력 메소드들
- **왜**: 파일이 4,581줄로 너무 커서 유지보수 및 수정이 어려움
- **어떻게**: LabelPrintHelper.java로 분리하고 콜백 인터페이스로 프린터 연결 처리

---

## 위험 요소 및 대응 방안

| 위험 요소 | 영향도 | 대응 방안 |
|----------|--------|----------|
| 파라미터 전달 실수 | 높음 | 기존 메소드와 동일한 테스트 케이스로 검증 |
| 바코드 생성 오류 | 높음 | 각 바코드 타입별 출력 결과 비교 테스트 |
| 소비기한 계산 오류 | 높음 | M3/M4 타입 라벨의 소비기한 날짜 비교 테스트 |
| 미트센터/수입육 분기 오류 | 높음 | 킬코이(9231), TRD/WET/E/T 센터 라벨 출력 테스트 |
| 프린터 통신 오류 | 중간 | 콜백 인터페이스 동작 확인 |
| searchType 분기 오류 | 중간 | 각 마트사별 올바른 메소드 호출 확인 |
| 인코딩 문제 | 중간 | EUC-KR 인코딩 유지 확인 |

---

## 롤백 계획

### 롤백 트리거 조건
- Step 6 컴파일 실패 시
- Step 7~9 테스트 실패 시 (수정 불가한 경우)

### 롤백 절차

1. 신규 파일/폴더 삭제
   ```bash
   rm -rf app/src/main/java/com/rgbsolution/highland_emart/print/
   ```

2. BixolonShipmentActivity.java 복원
   ```bash
   git checkout HEAD -- app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java
   ```

3. 롤백 후 검증
   - 컴파일 성공 확인
   - 기존 라벨 출력 기능 정상 동작 확인

4. 원인 분석 후 재시도

---

**최종 수정일**: 2026-02-04 (Step 4 → 4-1, 4-2, 4-3, 4-4 분리)
