# ProductionActivity 클래스 문서

## 1. 개요

### 파일 정보
- **파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\ProductionActivity.java`
- **총 라인 수**: 466줄
- **패키지**: `com.rgbsolution.highland_emart`

### 상속 관계
```java
public class ProductionActivity extends ScannerActivity
```
- `ScannerActivity`를 상속받아 바코드 스캔 기능을 활용
- Android의 Activity 생명주기를 따름

### 주요 역할
이 클래스는 **생산 계근(계량) 계산** 시스템의 핵심 화면으로, 다음과 같은 기능을 수행합니다:

1. **바코드 스캔 처리**: 바코드 스캐너를 통해 제품의 중량 정보를 자동으로 읽어들임
2. **중량 계산**: 바코드에서 추출한 중량 정보를 파싱하고 단위 변환(LB→KG) 수행
3. **박스 개수 누적**: 스캔된 박스의 총 개수를 계산
4. **총 중량 누적**: 모든 박스의 중량을 합산
5. **수기 입력 지원**: 바코드 스캔이 불가능한 경우 중량을 수기로 입력 가능
6. **중복 체크**: 동일한 바코드의 중복 스캔을 방지 (특정 패커 코드 제외)

---

## 2. 클래스 구조

### 주요 필드

#### 2.1 UI 컴포넌트
```java
private Spinner sp_work;                    // 작업 모드 선택 (바코드/수기)
private EditText edit_barcode;              // 스캔한 바코드 정보
private EditText edit_pp_code;              // PP 코드 입력창
private EditText edit_packer_code;          // PACKER 코드 입력창
private EditText total_box_count;           // 총 박스 개수 표시
private EditText total_weight;              // 총 중량 표시
private EditText weight_of_box;             // 현재 박스 중량 표시
private Button btn_input;                   // 입력 버튼
private Button btn_input_barcode_info;      // 바코드 정보 수신 버튼
private Button btn_reset;                   // 리셋 버튼
```

#### 2.2 작업 제어 변수
```java
private int work_flag = 1;                  // 작업 모드: 1=바코드스캔, 0=수기입력
private boolean communicationOrNot = false; // 바코드 정보 수신 여부
```

#### 2.3 바코드 중량 정보 파라미터
```java
private String weightFrom;                  // 바코드에서 중량 시작 위치
private String weightTo;                    // 바코드에서 중량 종료 위치
private String zeroPoint;                   // 소수점 자리수
private String baseUnit;                    // 기본 단위 (KG/LB)
```

#### 2.4 기타 필드
```java
private final String TAG = "ProductionActivity";  // 로그 태그
private ProgressDialog pDialog = null;            // 진행 상태 다이얼로그
```

---

## 3. 생명주기 메서드

### 3.1 onCreate()
**위치**: 52-90줄

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_production_calc);

    // UI 컴포넌트 초기화
    edit_pp_code = (EditText) findViewById(R.id.edit_pp_code);
    edit_packer_code = (EditText) findViewById(R.id.edit_packer_code);
    edit_barcode = (EditText) findViewById(R.id.edit_barcode);
    total_box_count = (EditText) findViewById(R.id.total_box_count);
    total_weight = (EditText) findViewById(R.id.total_weight);
    weight_of_box = (EditText) findViewById(R.id.weight_of_box);

    // 버튼 리스너 설정
    btn_input.setOnClickListener(inputBtnListener);
    btn_input_barcode_info.setOnClickListener(inputBarcodeBtnListener);
    btn_reset.setOnClickListener(resetBtnListener);

    // 초기값 설정
    total_box_count.setText("0");
    total_weight.setText("0");
    weight_of_box.setText("0");

    // 스피너 설정
    sp_work.setOnItemSelectedListener(workSelectedListener);

    // 인쇄 스위치 비활성화
    swt_print.setChecked(false);
    swt_print.setClickable(false);

    // DB 테이블 초기화
    DBHandler.deleteGoodsWetProductionCalc(getApplicationContext());
}
```

**주요 기능**:
- 레이아웃 설정 (`activity_production_calc`)
- 모든 UI 컴포넌트 초기화 및 리스너 등록
- 계근 정보 초기화 (박스 개수, 총 중량, 박스 중량을 0으로 설정)
- 이전 세션의 DB 데이터 삭제

### 3.2 onResume()
**위치**: 92-96줄

```java
@Override
public void onResume() {
    super.onResume();
    Log.i(TAG, TAG + " onResume");
}
```

### 3.3 onStart()
**위치**: 98-103줄

```java
@Override
protected void onStart() {
    super.onStart();
    Log.i(TAG, TAG + " onStart");
}
```

### 3.4 onPause()
**위치**: 105-109줄

```java
@Override
protected void onPause() {
    super.onPause();
    Log.i(TAG, TAG + " onPause");
}
```

### 3.5 onDestroy()
**위치**: 401-406줄

```java
@Override
public void onDestroy() {
    super.onDestroy();
    Log.i(TAG, TAG + " onDestroy, table data delete");
    DBHandler.deleteGoodsWetProductionCalc(getApplicationContext());
}
```

**주요 기능**:
- 액티비티 종료 시 DB에 저장된 계근 정보 삭제
- 메모리 누수 방지

---

## 4. 주요 메서드 분석

### 4.1 바코드 스캔 처리

#### setMessage(String msg)
**위치**: 211-235줄

바코드 스캐너에서 스캔된 데이터를 수신하는 진입점 메서드입니다.

```java
protected void setMessage(String msg) {
    if (Common.D) {
        Log.d(TAG, "바코드스캐너 입력값 : " + msg);
    }

    // 바코드 정보 수신 여부 확인
    if(!communicationOrNot) {
        Toast.makeText(getApplicationContext(),
            "바코드 정보를 수신한 후에 바코드 스캔을 진행해 주세요.",
            Toast.LENGTH_SHORT).show();
        return;
    }

    // PACKER 코드 및 PP 코드 입력 확인
    if (edit_packer_code.getText().toString().equals("") ||
        edit_pp_code.getText().toString().equals("")) {
        Toast.makeText(getApplicationContext(),
            "PACKER 혹은 PP코드를 입력 후 계근을 진행하세요.",
            Toast.LENGTH_SHORT).show();
        return;
    }

    // 작업 모드에 따라 처리
    if (msg != null) {
        if (work_flag == 1) {       // 바코드 스캐너
            setBarcodeMsg(msg);
        } else if (work_flag == 0) { // 수기 입력
            setBarcodeMsg(msg);      // 수기 모드에서도 바코드 스캔 가능
        } else if (work_flag == 2) {
            setBarcodeMsg(msg);
        }
    }
}
```

**처리 흐름**:
1. 바코드 정보 수신 여부 확인
2. PACKER 코드 및 PP 코드 입력 확인
3. 유효성 검사 통과 시 `setBarcodeMsg()` 호출

#### setBarcodeMsg(String msg)
**위치**: 238-248줄

```java
public void setBarcodeMsg(final String msg) {
    sp_work.setSelection(0);  // 스피너를 바코드로 설정

    try {
        edit_barcode.setText(msg);
        calcAndEnterWeight("scan", msg);  // 중량 계산 및 입력
    } catch (Exception ex) {
        Log.e(TAG, "setBarcodeMsg Exception -> " + ex.getMessage().toString());
    }
}
```

**주요 기능**:
- 스캔된 바코드를 UI에 표시
- `calcAndEnterWeight()` 메서드를 호출하여 중량 계산 시작

### 4.2 바코드 정보 수신

#### getBarcodeInfo()
**위치**: 250-297줄

서버에서 바코드 중량 정보(weightFrom, weightTo, zeroPoint, baseUnit)를 가져옵니다.

```java
protected void getBarcodeInfo() {
    boolean successFlag = false;

    if(!communicationOrNot) {
        Log.d(TAG, "getting barcode info start");
        try {
            // AsyncTask를 통한 서버 통신
            SelectWeightFromTo task = new SelectWeightFromTo(ProductionActivity.this);
            String[] test = new String[2];
            test[0] = edit_packer_code.getText().toString();  // PACKER_CODE
            test[1] = edit_pp_code.getText().toString();       // PP_CODE
            String temp = task.execute(test).get();

            if(temp != null) {
                // 서버에서 받은 데이터 파싱
                String[] tempArray = temp.split("::");

                weightFrom = tempArray[0].toString();
                weightTo = tempArray[1].toString();
                zeroPoint = tempArray[2].toString();
                baseUnit = tempArray[3].toString();

                successFlag = true;
            }

        } catch (Exception ex) {
            Log.e(TAG, "======== Production Activity Exception ========");
            Log.e(TAG, ex.toString());
        }

        // 성공 시 처리
        if(successFlag) {
            communicationOrNot = true;  // 통신 완료 플래그 설정
            setUseableEditText(edit_pp_code, false);       // PP 코드 수정 불가
            setUseableEditText(edit_packer_code, false);   // PACKER 코드 수정 불가

            Toast.makeText(getApplicationContext(),
                "바코드 중량 정보 세팅됨, 계근을 시작하세요.",
                Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),
                "입력하신 PACKER CODE / PPCODE 에 해당하는 바코드 정보가 없습니다.",
                Toast.LENGTH_SHORT).show();
        }
    }
}
```

**주요 기능**:
1. PACKER_CODE와 PP_CODE를 파라미터로 서버에 요청
2. 서버 응답을 `::`로 구분하여 파싱
3. 바코드 중량 정보를 클래스 필드에 저장
4. 성공 시 코드 입력 필드를 비활성화하여 변경 방지

### 4.3 중량 계산 로직

#### calcAndEnterWeight(String mode, String barcode)
**위치**: 299-392줄

이 메서드는 **가장 핵심적인 중량 계산 로직**을 담당합니다.

```java
protected void calcAndEnterWeight(String mode, String barcode) {
    Log.d(TAG, "calcAndEnterWeight start");

    // ========================================
    // 1단계: 중복 바코드 체크
    // ========================================
    String barcodeCount = DBHandler.selectGoodsWetProductionCalc(
        getApplicationContext(), barcode);

    if(barcodeCount.equals("0")) {
        // 처음 스캔된 바코드 → DB에 저장
        DBHandler.insertGoodsWetProductionCalc(getApplicationContext(), barcode);
    } else {
        // 중복 스캔 감지 (PACKER_CODE 31341 제외)
        if(!edit_packer_code.getText().toString().equals("31341")) {
            Toast.makeText(getApplicationContext(),
                "바코드를 중복으로 스캔하셨습니다. 다른 바코드를 스캔해주세요.",
                Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // ========================================
    // 2단계: 현재 값 가져오기
    // ========================================
    Integer startBoxCnt = Integer.parseInt(total_box_count.getText().toString());
    Float totalWeight = Float.parseFloat(total_weight.getText().toString());

    String totalWeightFormated = "";
    String item_weight_str = "";

    // 박스 개수 증가
    startBoxCnt = startBoxCnt + 1;
    total_box_count.setText(startBoxCnt.toString());

    // ========================================
    // 3단계: 모드별 중량 처리
    // ========================================

    // 3-1. 수기 입력 모드
    if(mode.equals("manual")) {
        totalWeight = totalWeight + Float.parseFloat(edit_barcode.getText().toString());
        totalWeightFormated = String.format("%.2f", totalWeight);
    }
    // 3-2. 바코드 스캔 모드
    else if(mode.equals("scan")) {

        Log.d(TAG, "weightfrom,to:" + weightFrom + ":" + weightTo + ":");

        String item_weight = "";              // 상품 최초 중량 절사값
        Double item_weight_double = 0.0;      // 상품 Double 중량값
        String work_item_fullbarcode = barcode;
        double item_pow = 0;                  // zeroPoint에 대한 pow

        // trim 처리 (DB에서 받은 값에 공백이 있을 수 있음)
        String weightFromVar = weightFrom.trim();
        String weightToVar = weightTo.trim();
        String zeroPointVar = zeroPoint.trim();
        String baseUnitVar = baseUnit.trim();

        // ========================================
        // 4단계: 바코드에서 중량 값 추출
        // ========================================
        item_weight = work_item_fullbarcode.substring(
            Integer.parseInt(weightFromVar) - 1,
            Integer.parseInt(weightToVar)
        );

        Log.i(TAG, "Type W | 절사한 중량값 : " + item_weight);

        // ========================================
        // 5단계: 소수점 처리
        // ========================================
        item_pow = Math.pow(10, Integer.parseInt(zeroPointVar));
        item_weight_double = Double.parseDouble(item_weight) / item_pow;

        // ========================================
        // 6단계: 단위 변환 (LB → KG)
        // ========================================
        if ("LB".equals(baseUnitVar)) {
            // 파운드를 킬로그램으로 변환: LB * 0.453592 = KG
            double temp_weight_double = item_weight_double * 0.453592;

            Log.i(TAG, "temp_weight_double 계산 전 : " + temp_weight_double);
            Log.i(TAG, "math.floor: " + Math.floor(temp_weight_double * item_pow));

            // 소수점 두 자리까지 처리
            item_weight_double = Math.floor(temp_weight_double * 100) / 100;
            item_weight_str = String.valueOf(item_weight_double);

            Log.i(TAG, "LB->KG | 환산 중량 item_pow : " + item_pow);
            Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + item_weight_double);
            Log.i(TAG, "LB->KG | 환산 중량 String값 : " + item_weight_str);
        }

        // ========================================
        // 7단계: 최종 포맷팅
        // ========================================
        String temp_weight = String.format("%.2f", item_weight_double);
        item_weight_double = Double.parseDouble(temp_weight);
        item_weight_str = String.valueOf(item_weight_double);

        Log.i(TAG, "Type W | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
        Log.i(TAG, "Type W | ZeroPoint 적용 중량 String값 : " + item_weight_str);

        // 총 중량에 추가
        totalWeight = totalWeight + Float.parseFloat(item_weight_str);
        totalWeightFormated = String.format("%.2f", totalWeight);
    }

    // ========================================
    // 8단계: UI 업데이트
    // ========================================
    total_weight.setText(totalWeightFormated);

    if(mode.equals("manual")) {
        weight_of_box.setText(edit_barcode.getText().toString());
        Toast.makeText(getApplicationContext(),
            "중량 정보 " + edit_barcode.getText().toString() + " kg 입력됨",
            Toast.LENGTH_SHORT).show();
    } else {
        weight_of_box.setText(item_weight_str);
        Toast.makeText(getApplicationContext(),
            "중량 정보 " + item_weight_str + " kg 입력됨",
            Toast.LENGTH_SHORT).show();
    }
}
```

**중량 계산 상세 예시**:

```
예시 1) KG 단위 바코드
- 바코드: "1234567890123456789012345"
- weightFrom: 10
- weightTo: 15
- zeroPoint: 2
- baseUnit: "KG"

처리 과정:
1. 바코드에서 추출: "67890" (10번째~15번째 문자)
2. 소수점 처리: 67890 / 10^2 = 67890 / 100 = 678.90
3. 단위 변환: KG이므로 변환 불필요
4. 최종 결과: 678.90 kg

예시 2) LB 단위 바코드
- 바코드: "1234567890123456789012345"
- weightFrom: 10
- weightTo: 15
- zeroPoint: 2
- baseUnit: "LB"

처리 과정:
1. 바코드에서 추출: "67890"
2. 소수점 처리: 67890 / 10^2 = 678.90 lb
3. 단위 변환: 678.90 * 0.453592 = 307.928328 kg
4. 소수점 두 자리: Math.floor(307.928328 * 100) / 100 = 307.92 kg
5. 최종 결과: 307.92 kg
```

### 4.4 유틸리티 메서드

#### hideKeyboard()
**위치**: 111-114줄

```java
private void hideKeyboard() {
    InputMethodManager btn_input = (InputMethodManager)
        getSystemService(Context.INPUT_METHOD_SERVICE);
    btn_input.hideSoftInputFromWindow(
        this.getCurrentFocus().getWindowToken(),
        InputMethodManager.HIDE_NOT_ALWAYS);
}
```

**주요 기능**: 버튼 클릭 시 소프트 키보드를 숨김

#### setUseableEditText(EditText et, boolean useable)
**위치**: 394-399줄

```java
private void setUseableEditText(EditText et, boolean useable) {
    et.setClickable(useable);
    et.setEnabled(useable);
    et.setFocusable(useable);
    et.setFocusableInTouchMode(useable);
}
```

**주요 기능**: EditText의 활성화/비활성화 상태를 제어

---

## 5. UI 리스너

### 5.1 입력 버튼 리스너 (inputBtnListener)
**위치**: 116-139줄

```java
private View.OnClickListener inputBtnListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Log.i(TAG, "계근 입력버튼 클릭");
        hideKeyboard();

        if (work_flag == 1) {
            // 바코드 스캔 작업: 별도 처리 없음 (스캔 시 자동 처리)

        } else if (work_flag == 0) {
            // 수기 입력 작업
            if (edit_barcode.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(),
                    "중량을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (edit_packer_code.getText().toString().equals("") ||
                edit_pp_code.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(),
                    "PACKER 혹은 PP코드를 입력 후 계근을 진행하세요.",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            calcAndEnterWeight("manual", "");
        }
    }
};
```

**주요 기능**:
- 수기 입력 모드일 때만 동작
- 중량 값 입력 확인
- PACKER/PP 코드 입력 확인
- `calcAndEnterWeight()` 호출

### 5.2 바코드 정보 수신 버튼 리스너 (inputBarcodeBtnListener)
**위치**: 141-153줄

```java
private View.OnClickListener inputBarcodeBtnListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Log.i(TAG, "바코드 정보 수신버튼 클릭");

        if (edit_packer_code.getText().toString().equals("") ||
            edit_pp_code.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(),
                "PACKER 혹은 PP코드를 입력 후 다운로드를 바코드정보 수신을 진행하세요.",
                Toast.LENGTH_SHORT).show();
            return;
        }

        getBarcodeInfo();  // 바코드 정보 수신
        hideKeyboard();
    }
};
```

**주요 기능**:
- PACKER/PP 코드 입력 확인
- `getBarcodeInfo()` 메서드를 호출하여 서버에서 바코드 정보 다운로드

### 5.3 리셋 버튼 리스너 (resetBtnListener)
**위치**: 155-182줄

```java
private View.OnClickListener resetBtnListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ProductionActivity.this);

        dialog.setTitle("리셋 여부 확인")
            .setMessage("계근 정보를 리셋 하시겠습니까?(계근한 바코드 정보도 같이 리셋됩니다.)")
            .setIcon(android.R.drawable.ic_menu_save)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // 확인 시 처리 로직
                    total_box_count.setText("0");
                    total_weight.setText("0");
                    weight_of_box.setText("0");
                    edit_barcode.setText("");
                    sp_work.setSelection(0);
                    DBHandler.deleteGoodsWetProductionCalc(getApplicationContext());
                    Toast.makeText(ProductionActivity.this,
                        "리셋을 완료했습니다.", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // 취소 시 처리 로직
                    Toast.makeText(ProductionActivity.this,
                        "취소하였습니다.", Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }
};
```

**주요 기능**:
- 확인 다이얼로그 표시
- 모든 계근 정보 초기화
- DB에 저장된 바코드 정보 삭제

### 5.4 작업 모드 선택 리스너 (workSelectedListener)
**위치**: 184-208줄

```java
private Spinner.OnItemSelectedListener workSelectedListener =
    new Spinner.OnItemSelectedListener() {

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        try {
            switch (arg2) {
                case 0:  // 바코드
                    work_flag = 1;
                    Log.i(TAG, "바코드 클릭");
                    break;

                case 1:  // 수기
                    work_flag = 0;
                    Log.i(TAG, "수기 클릭");
                    edit_barcode.setText("");
                    break;
            }
        } catch (Exception ex) {
            Log.e(TAG, "======== workSelectedListener Exception ========");
            Log.e(TAG, ex.getMessage().toString());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
};
```

**주요 기능**:
- 스피너 선택에 따라 `work_flag` 변경
- 수기 모드 선택 시 바코드 입력창 초기화

---

## 6. 처리 흐름도

### 6.1 전체 작업 흐름

```
[앱 시작]
    ↓
[onCreate()]
    ↓
[PACKER CODE / PP CODE 입력]
    ↓
[바코드 정보 수신 버튼 클릭] ← inputBarcodeBtnListener
    ↓
[getBarcodeInfo()] → [SelectWeightFromTo AsyncTask]
    ↓                        ↓
    ↓                   [서버 통신]
    ↓                        ↓
    ↓              [weightFrom, weightTo, zeroPoint, baseUnit 수신]
    ↓                        ↓
    ←←←←←←←←←←←←←←←←←←←←
    ↓
[communicationOrNot = true]
[PP CODE / PACKER CODE 수정 불가 처리]
    ↓
┌───────────────────┬───────────────────┐
│                   │                   │
│  [바코드 스캔]    │  [수기 입력]      │
│       ↓           │       ↓           │
│  setMessage()     │  입력 버튼 클릭   │
│       ↓           │       ↓           │
│  setBarcodeMsg()  │  calcAndEnter..() │
│       ↓           │       ↓           │
│       └───────────┴───────┘           │
│               ↓                       │
│       calcAndEnterWeight()            │
│               ↓                       │
│       [중복 바코드 체크]              │
│               ↓                       │
│       [DB에 바코드 저장]              │
│               ↓                       │
│       [박스 개수 증가]                │
│               ↓                       │
│   ┌───────────┴───────────┐           │
│   │                       │           │
│ [scan]                 [manual]       │
│   ↓                       ↓           │
│ 바코드에서 중량 추출     입력값 사용  │
│   ↓                       ↓           │
│ 소수점 처리               │           │
│   ↓                       │           │
│ LB→KG 변환 (필요시)      │           │
│   ↓                       │           │
│   └───────────┬───────────┘           │
│               ↓                       │
│       [총 중량 계산]                  │
│               ↓                       │
│       [UI 업데이트]                   │
│               ↓                       │
│       [Toast 메시지]                  │
└───────────────────────────────────────┘
```

### 6.2 바코드 중량 추출 상세 흐름

```
[바코드 스캔: "1234567890123456789012345"]
         ↓
[weightFrom=10, weightTo=15]
         ↓
[substring(9, 15)] → "67890"
         ↓
[zeroPoint=2]
         ↓
[67890 / 10^2] → 678.90
         ↓
[baseUnit 확인]
         ↓
    ┌────┴────┐
    ↓         ↓
  [KG]      [LB]
    ↓         ↓
  유지    678.90 * 0.453592
    ↓         ↓
    ↓     307.928328
    ↓         ↓
    ↓    Math.floor(307.928328 * 100) / 100
    ↓         ↓
    ↓     307.92
    ↓         ↓
    └────┬────┘
         ↓
    [최종 중량]
         ↓
    [총 중량에 누적]
```

### 6.3 중복 바코드 체크 흐름

```
[바코드 스캔]
     ↓
[DB 조회: selectGoodsWetProductionCalc()]
     ↓
┌────┴────┐
↓         ↓
[결과="0"] [결과="1" 이상]
↓         ↓
처음 스캔  중복 스캔
↓         ↓
DB 저장   [PACKER_CODE 확인]
↓         ↓
계속 진행  ┌────┴────┐
          ↓         ↓
      [31341]    [기타]
          ↓         ↓
      계속 진행   에러 메시지 + 종료
```

---

## 7. AsyncTask 클래스

### SelectWeightFromTo
**위치**: 408-464줄

서버에서 바코드 중량 정보를 비동기로 가져오는 내부 클래스입니다.

```java
class SelectWeightFromTo extends AsyncTask<String, String, String> {

    private Context mContext;
    String receiveData = "";

    public SelectWeightFromTo(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(mContext);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setTitle("전송 중 입니다.");
        pDialog.setMessage("잠시만 기다려 주세요..");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String ... values) {
        String result = "";

        try {
            String packerCode = values[0];
            String packerProductCode = values[1];

            Log.d(TAG, "============ packerCode ============== : " + packerCode);
            Log.d(TAG, "============ packerProductCode ========: " + packerProductCode);

            String packet = "AND a.PACKER_CODE = '" + packerCode + "'" +
                          " AND a.PACKER_PRODUCT_CODE = '" + packerProductCode + "'";
            result = HttpHelper.getInstance().sendDataDb(
                packet, "inno", "search_production_calc",
                Common.URL_WET_PRODUCTION_CALC);

        } catch (Exception ex) {
            Log.e(TAG, "======== selectWeightFromTo doInBackgounrd Exception ========");
            Log.e(TAG, ex.toString());
            return null;
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String _result) {
        Log.v(TAG, "전송결과 : " + _result);
        pDialog.dismiss();
        super.onPostExecute(_result);
    }
}
```

**주요 기능**:
- `onPreExecute()`: ProgressDialog 표시
- `doInBackground()`: 서버에 HTTP 요청 전송
- `onPostExecute()`: ProgressDialog 닫기

**서버 요청 형식**:
```
URL: Common.URL_WET_PRODUCTION_CALC
Method: sendDataDb()
Parameters:
  - packet: "AND a.PACKER_CODE = '{packerCode}' AND a.PACKER_PRODUCT_CODE = '{ppCode}'"
  - database: "inno"
  - query: "search_production_calc"

응답 형식: "weightFrom::weightTo::zeroPoint::baseUnit"
예시: "10::15::2::KG"
```

---

## 8. 코드 위치 참조

### 주요 메서드 위치 맵

| 메서드명 | 시작 라인 | 종료 라인 | 설명 |
|---------|---------|---------|------|
| `onCreate()` | 52 | 90 | 액티비티 초기화 |
| `onResume()` | 92 | 96 | 액티비티 재개 |
| `onStart()` | 98 | 103 | 액티비티 시작 |
| `onPause()` | 105 | 109 | 액티비티 일시 정지 |
| `onDestroy()` | 401 | 406 | 액티비티 종료 및 DB 정리 |
| `hideKeyboard()` | 111 | 114 | 키보드 숨김 |
| `inputBtnListener` | 116 | 139 | 입력 버튼 리스너 |
| `inputBarcodeBtnListener` | 141 | 153 | 바코드 정보 수신 버튼 리스너 |
| `resetBtnListener` | 155 | 182 | 리셋 버튼 리스너 |
| `workSelectedListener` | 184 | 208 | 작업 모드 선택 리스너 |
| `setMessage()` | 211 | 235 | 바코드 스캔 진입점 |
| `setBarcodeMsg()` | 238 | 248 | 바코드 메시지 처리 |
| `getBarcodeInfo()` | 250 | 297 | 바코드 정보 수신 |
| `calcAndEnterWeight()` | 299 | 392 | 중량 계산 및 입력 (핵심 로직) |
| `setUseableEditText()` | 394 | 399 | EditText 활성화 제어 |
| `SelectWeightFromTo` | 408 | 464 | AsyncTask 클래스 |

### 중요 코드 블록

#### 중량 추출 (346-348줄)
```java
item_weight = work_item_fullbarcode.substring(
    Integer.parseInt(weightFromVar) - 1,
    Integer.parseInt(weightToVar)
);
```

#### 소수점 처리 (352-353줄)
```java
item_pow = Math.pow(10, Integer.parseInt(zeroPointVar));
item_weight_double = Double.parseDouble(item_weight) / item_pow;
```

#### LB→KG 변환 (355-368줄)
```java
if ("LB".equals(baseUnitVar)) {
    double temp_weight_double = item_weight_double * 0.453592;
    item_weight_double = Math.floor(temp_weight_double * 100) / 100;
    item_weight_str = String.valueOf(item_weight_double);
}
```

#### 중복 체크 (303-312줄)
```java
String barcodeCount = DBHandler.selectGoodsWetProductionCalc(
    getApplicationContext(), barcode);

if(barcodeCount.equals("0")) {
    DBHandler.insertGoodsWetProductionCalc(getApplicationContext(), barcode);
} else {
    if(!edit_packer_code.getText().toString().equals("31341")) {
        // 중복 에러 처리
        return;
    }
}
```

---

## 9. DB 처리

이 클래스는 `DBHandler` 클래스를 통해 로컬 SQLite 데이터베이스와 상호작용합니다.

### 사용되는 DB 메서드

| 메서드명 | 설명 | 호출 위치 |
|---------|------|---------|
| `deleteGoodsWetProductionCalc()` | 계근 정보 테이블 초기화 | onCreate(89줄), onDestroy(405줄), 리셋(172줄) |
| `selectGoodsWetProductionCalc()` | 바코드 중복 확인 | calcAndEnterWeight(303줄) |
| `insertGoodsWetProductionCalc()` | 바코드 정보 저장 | calcAndEnterWeight(306줄) |

### DB 사용 패턴

```java
// 1. 앱 시작 시 테이블 초기화
DBHandler.deleteGoodsWetProductionCalc(getApplicationContext());

// 2. 바코드 스캔 시 중복 체크
String barcodeCount = DBHandler.selectGoodsWetProductionCalc(
    getApplicationContext(), barcode);

// 3. 첫 스캔이면 DB에 저장
if(barcodeCount.equals("0")) {
    DBHandler.insertGoodsWetProductionCalc(getApplicationContext(), barcode);
}

// 4. 앱 종료 시 테이블 정리
DBHandler.deleteGoodsWetProductionCalc(getApplicationContext());
```

---

## 10. 특이사항 및 주의사항

### 10.1 특정 PACKER_CODE 예외 처리
**위치**: 308-311줄

```java
if(!edit_packer_code.getText().toString().equals("31341")) {
    Toast.makeText(getApplicationContext(),
        "바코드를 중복으로 스캔하셨습니다.", Toast.LENGTH_SHORT).show();
    return;
}
```

PACKER_CODE `31341`은 중복 스캔이 허용됩니다. 이는 특수한 업무 요구사항으로 보입니다.

### 10.2 인쇄 스위치 비활성화
**위치**: 83-84줄

```java
swt_print.setChecked(false);  // 인쇄 안함으로 세팅
swt_print.setClickable(false); // 스위치 불가능하도록 변경
```

생산 계근 화면에서는 인쇄 기능이 사용되지 않습니다.

### 10.3 수기 입력 시에도 바코드 스캔 가능
**위치**: 229-230줄

```java
} else if (work_flag == 0) { // 수기로 입력
    setBarcodeMsg(msg);      // 수기에 놓고도 바코드 스캔할 수 있음
}
```

작업 모드가 수기 입력으로 설정되어 있어도 바코드 스캔이 가능합니다.

### 10.4 trim() 처리의 필요성
**위치**: 337-340줄

```java
String weightFromVar = weightFrom.trim();
String weightToVar = weightTo.trim();
String zeroPointVar = zeroPoint.trim();
String baseUnitVar = baseUnit.trim();
```

DB에서 받아온 파라미터 값에 공백이 포함될 수 있어 `parseInt()` 시 에러를 방지하기 위해 `trim()` 처리가 필요합니다.

### 10.5 LB→KG 변환 계수
**위치**: 357줄

```java
double temp_weight_double = item_weight_double * 0.453592;
```

1 파운드(LB) = 0.453592 킬로그램(KG)

---

## 11. 개선 제안

### 11.1 AsyncTask 대체
`AsyncTask`는 Android API Level 30에서 deprecated 되었습니다. `Kotlin Coroutines` 또는 `RxJava`로 대체하는 것이 권장됩니다.

### 11.2 하드코딩된 값 상수화
```java
// 현재
if(!edit_packer_code.getText().toString().equals("31341"))

// 제안
private static final String SPECIAL_PACKER_CODE = "31341";
if(!edit_packer_code.getText().toString().equals(SPECIAL_PACKER_CODE))
```

### 11.3 중량 계산 로직 분리
`calcAndEnterWeight()` 메서드가 너무 길어서 가독성이 떨어집니다. 중량 추출, 단위 변환, UI 업데이트를 별도 메서드로 분리하는 것이 좋습니다.

```java
// 제안
private Double extractWeightFromBarcode(String barcode) { ... }
private Double convertLbToKg(Double weightInLb) { ... }
private void updateWeightUI(Double weight, String mode) { ... }
```

### 11.4 Magic Number 제거
```java
// 현재
work_flag = 1;  // 바코드
work_flag = 0;  // 수기

// 제안
private static final int MODE_BARCODE = 1;
private static final int MODE_MANUAL = 0;
```

### 11.5 에러 처리 강화
현재 일부 예외는 로그만 출력하고 사용자에게 알리지 않습니다. 사용자 친화적인 에러 메시지 표시가 필요합니다.

---

## 12. 결론

`ProductionActivity`는 생산 계근 시스템의 핵심 화면으로, 다음과 같은 복잡한 비즈니스 로직을 처리합니다:

1. **바코드 스캔**: 하드웨어 스캐너 연동
2. **중량 추출**: 바코드에서 중량 정보 파싱
3. **단위 변환**: LB ↔ KG 변환
4. **중복 방지**: DB를 활용한 중복 스캔 방지
5. **누적 계산**: 박스 개수 및 총 중량 누적

코드는 전반적으로 잘 구조화되어 있으나, AsyncTask deprecated 문제와 메서드 길이 등 개선의 여지가 있습니다.

---

**문서 생성일**: 2025-10-31
**분석 파일**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\ProductionActivity.java`
**총 라인 수**: 466줄
