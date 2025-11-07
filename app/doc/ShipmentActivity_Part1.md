# ShipmentActivity 분석 - Part 1: 개요 및 클래스 구조

> **파일 위치**: `app/src/main/java/com/rgbsolution/highland_emart/ShipmentActivity.java`
> **코드 라인**: 4158줄
> **작성일**: 2025-01-27

---

## 📑 목차

- [Part 1: 개요 및 클래스 구조](ShipmentActivity_Part1.md) ✅ 현재 문서
- [Part 2: 바코드 스캔 처리](ShipmentActivity_Part2.md)
- [Part 3: 계근 데이터 처리 및 프린터 출력](ShipmentActivity_Part3.md)
- [Part 4: 서버 전송 및 예외 처리](ShipmentActivity_Part4.md)

---

## 1. 개요

### 1.1 클래스 정보

```java
public class ShipmentActivity extends ScannerActivity
```

- **상속**: `ScannerActivity`
  - PM80 하드웨어 바코드 스캐너 기능 제공
  - `setMessage()` 메서드를 오버라이드하여 스캔 결과 처리
- **주요 역할**: 출하/생산 계근(무게 측정) 작업의 핵심 Activity
- **지원 유형**:
  - 이마트 출하 (searchType="0")
  - 홈플러스 출하 (searchType="2", "5")
  - 롯데 출하 (searchType="6")
  - 생산 계근 (searchType="1", "7")
  - 도매 출하 (searchType="3", "4")

### 1.2 주요 기능

```
┌─────────────────────────────────────────────┐
│              ShipmentActivity                │
├─────────────────────────────────────────────┤
│ 1. 바코드 스캔                               │
│    - PM80 스캐너 연동                        │
│    - 패커상품 코드 추출                      │
│    - BL 번호 확인                           │
│                                             │
│ 2. 중량 추출 및 변환                         │
│    - 바코드에서 중량 데이터 파싱             │
│    - LB → KG 변환                           │
│    - 소수점 처리 (유형별 상이)               │
│                                             │
│ 3. 계근 데이터 저장                          │
│    - 로컬 SQLite DB 저장                    │
│    - 출하대상 정보 업데이트                  │
│                                             │
│ 4. 프린터 출력                               │
│    - Woosim 블루투스 프린터 연동             │
│    - 출하 유형별 라벨 형식                   │
│                                             │
│ 5. 서버 전송                                 │
│    - HTTP POST 방식                         │
│    - 개별/일괄 전송 (유형별 상이)            │
└─────────────────────────────────────────────┘
```

---

## 2. 클래스 구조

### 2.1 주요 필드 (Member Variables)

#### 2.1.1 프린터 관련 필드 (65~94줄)

```java
// 블루투스 프린터 관련
public static final int REQUEST_CONNECT_DEVICE = 1;
public static final int MESSAGE_DEVICE_NAME = 1;
public static final int MESSAGE_TOAST = 2;
public static final int MESSAGE_READ = 3;
public static final int MESSAGE_SEARCH = 4;
public static final int MESSAGE_REPRINT = 5;
public static final int GET_DATA_REQUEST = 8;

private String mConnectedDeviceName = null;         // 연결된 프린터 이름
private BluetoothAdapter mBluetoothAdapter = null;  // 블루투스 어댑터
private BluetoothPrintService mPrintService = null; // 블루투스 프린트 서비스
private WoosimService mWoosim = null;               // Woosim 프린터 서비스

// 사운드 효과
protected SoundPool sound_pool;
protected int sound_success;  // 프린터 연결 성공음
protected int sound_fail;     // 프린터 연결 실패음

// 롯데 전용
private int lotte_TryCount = 0;  // 롯데 박스 순번 카운터 (1~9999 순환)
```

#### 2.1.2 UI 컴포넌트 필드 (98~120줄)

```java
// 레이아웃 및 다이얼로그
private LayoutInflater Inflater;
private ProgressDialog pDialog = null;
private ProgressDialog cDialog = null;

// 데이터 리스트
private ArrayList<Shipments_Info> arSM;      // 출하대상 List
private ArrayList<Goodswets_Info> arBcode;   // 계근 List

// 상단 작업 선택 영역
private Spinner sp_work;                     // 작업유형 (바코드/수기/상품코드)
private EditText edit_barcode;               // 스캔한 바코드 정보
private Button btn_input;                    // 입력 버튼

// 센터 및 상품 정보 영역
private Spinner sp_center_name;              // 이마트 센터명
private EditText edit_product_name;          // 패커 상품명
private EditText edit_product_code;          // 패커 상품코드
private Spinner sp_bl_no;                    // BL 번호
private EditText edit_center_tcount;         // 센터 총 수량
private EditText edit_center_tweight;        // 센터 총 중량

// 지점 및 계근 정보 영역
private Spinner sp_point_name;               // 이마트 지점명
private EditText edit_wet_count;             // 지점 계근 수량
private EditText edit_wet_weight;            // 지점 계근 중량

// 리스트 및 버튼
private ShipmentListAdapter sListAdapter;    // 출하대상 어댑터
private ListView sList;                      // 작업 중인 지점 List
private Button btn_back;                     // 뒤로 버튼
private Button btn_send;                     // 계근 완료된 정보 G3 전송
private Button btn_select;                   // 선택 지점의 계근 상세정보 popup
```

#### 2.1.3 상태 관리 필드 (121~137줄)

```java
// 센터 누적 정보
private int centerTotalCount;       // 센터 총 계근요청수량
private int centerWorkCount;        // 센터 총 계근수량 (완료)
private double centerTotalWeight;   // 센터 총 계근요청중량
private double centerWorkWeight;    // 센터 총 계근중량 (완료)

// 작업 위치 관리
private int select_position;        // 선택된 지점 position
private int current_work_position;  // 현재 계근작업 position (-1이면 미선택)

// 작업 모드 플래그
private int work_flag = 1;          // 1:바코드스캔, 0:수기입력, 2:상품코드
private boolean scan_flag = true;   // true:패커상품스캔, false:BL스캔
private boolean select_flag = true; // true:스캔, false:선택
private boolean finish_flag = false;

// 기타
private Vibrator vibrator;          // 진동 피드백
private Toast toast;
AlertDialog alert;
boolean alert_flag = false;         // 중복 제어 변수
boolean makingdateInputFlag = false;

private String storeCode = LoginActivity.store[0];  // 창고 코드
```

#### 2.1.4 작업 관련 필드 (612~619줄)

```java
Barcodes_Info work_item_bi_info;    // 현재 작업중인 상품 바코드 정보
String work_ppcode = "";             // 작업중인 패커상품코드
String work_bl_no = "";              // 작업중인 BL번호
String work_item_fullbarcode = "";   // 작업중인 전체 바코드
String work_item_barcodegoods = "";  // 작업중인 바코드상품코드
String expiryDayTrans = "";          // 소비기한 (출력용)
boolean dialog_flag = false;         // 다이얼로그 중복 방지
String fullbarcode = "";
```

### 2.2 상수 정의

#### 2.2.1 Message Types (60~74줄)

```java
private final String TAG = "ShipmentActivity";
private final int MESSAGE_ROWCHECK = 1000;
private final int MESSAGE_COMPLETE = 1001;
private final int MESSAGE_SEARCHCHECK = 1002;

public static final int REQUEST_CONNECT_DEVICE = 1;
public static final int MESSAGE_DEVICE_NAME = 1;
public static final int MESSAGE_TOAST = 2;
public static final int MESSAGE_READ = 3;
public static final int MESSAGE_SEARCH = 4;
public static final int MESSAGE_REPRINT = 5;
public static final int GET_DATA_REQUEST = 8;

private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
private static final int REQUEST_ENABLE_BT = 3;
```

---

## 3. 생명주기 메서드

### 3.1 onCreate (140~226줄)

```java
@Override
protected void onCreate(Bundle savedInstanceState)
```

**주요 동작**:

#### 1단계: 레이아웃 설정 (146~150줄)
```java
if(Common.searchType.equals("3")) {
    setContentView(R.layout.activity_shipment_wholesale);  // 도매용
} else {
    setContentView(R.layout.activity_shipment);            // 일반용
}
```

#### 2단계: 변수 초기화 (152~161줄)
```java
current_work_position = -1;  // 작업 position 초기화

centerTotalCount = 0;
centerTotalWeight = 0.0;
centerWorkCount = 0;
centerWorkWeight = 0.0;

work_flag = 1;         // 바코드 스캔 모드
scan_flag = true;      // 패커상품 스캔부터 시작
vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
```

#### 3단계: 사운드 설정 (163~167줄)
```java
sound_pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
sound_success = sound_pool.load(getBaseContext(), R.raw.beep, 1);   // 성공음
sound_fail = sound_pool.load(getBaseContext(), R.raw.e, 1);         // 실패음
```

#### 4단계: UI 컴포넌트 초기화 (168~206줄)
```java
Inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

// Spinner 및 EditText 초기화
sp_work = (Spinner) findViewById(R.id.sp_work);
sp_work.setOnItemSelectedListener(workSelectedListener);

edit_barcode = (EditText) findViewById(R.id.edit_barcode);

sp_center_name = (Spinner) findViewById(sp_center);

// 센터 리스트 로드 및 설정
Common.list_center_info = DBHandler.selectqueryCenterList(this);
ArrayAdapter<String> center_adapter = new ArrayAdapter<String>(
    ShipmentActivity.this,
    android.R.layout.simple_spinner_item,
    Common.list_center_info
);
center_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
sp_center_name.setAdapter(center_adapter);
sp_center_name.setOnItemSelectedListener(emartCenterSelectedListener);

// 기타 UI 컴포넌트 초기화
btn_input = (Button) findViewById(R.id.btn_input);
btn_input.setOnClickListener(inputBtnListener);
// ... (기타 컴포넌트)
```

#### 5단계: 블루투스 어댑터 설정 (210~217줄)
```java
mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

if (mBluetoothAdapter == null) {
    Toast.makeText(this, R.string.toast_bt_na, Toast.LENGTH_LONG).show();
    finish();
    return;
}
```

#### 6단계: 이노이천 생산 계근 특수 처리 (220~225줄)
```java
if(Common.searchType.equals("1")) {
    Log.i(TAG, "===================PRINTER DISABLE==================");
    swt_print.setChecked(false);   // 인쇄 안함으로 세팅
    swt_print.setClickable(false); // 스위치 불가능하도록 변경
    Common.print_bool = false;     // 이마트 스티커 출력 로직 타지 않도록 false처리
}
```

**💡 주요 포인트**:
- 도매 출하(searchType="3")는 별도 레이아웃 사용
- 이노이천 생산 계근(searchType="1")은 프린터 비활성화
- 센터 리스트는 DB에서 동적 로드

---

### 3.2 onStart (235~264줄)

```java
@Override
protected void onStart()
```

**주요 동작**:

#### 1단계: 블루투스 활성화 확인 (242~246줄)
```java
if (!mBluetoothAdapter.isEnabled() && !Common.searchType.equals("1")) {
    // 블루투스 OFF + 생산 계근이 아닐 때
    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
}
```

#### 2단계: 프린터 연결 시도 (248~263줄)
```java
else {
    if (Common.printer_setting && !Common.searchType.equals("1")) {
        // 프린터 설정 ON + 생산 계근이 아닐 때

        if (mPrintService == null) {
            mPrintService = new BluetoothPrintService(ShipmentActivity.this, mHandler);
            mWoosim = new WoosimService(mHandler);

            if (Common.printer_address.equals("")) {
                // 프린터 주소 없음 → 장비 선택 화면으로
                Intent i = new Intent(ShipmentActivity.this, DeviceListActivity.class);
                startActivityForResult(i, REQUEST_CONNECT_DEVICE);
            } else {
                // 프린터 주소 있음 → 자동 연결
                new ProgressDlgPrintConnect(ShipmentActivity.this).execute();
            }
        }
    }
}
```

**💡 주요 포인트**:
- 생산 계근(searchType="1")은 블루투스/프린터 연결 안함
- 프린터 주소가 저장되어 있으면 자동 연결 시도
- 프린터 주소가 없으면 DeviceListActivity로 이동하여 선택

---

### 3.3 onResume (229~232줄)

```java
@Override
public void onResume() {
    super.onResume();
    Log.i(TAG, TAG + " onResume");
}
```

**동작**: 로그만 출력 (특별한 처리 없음)

---

### 3.4 onPause (267~270줄)

```java
@Override
protected void onPause() {
    super.onPause();
    Log.i(TAG, TAG + " onPause");
}
```

**동작**: 로그만 출력 (특별한 처리 없음)

---

### 3.5 onDestroy (273~282줄)

```java
@Override
public void onDestroy() {
    super.onDestroy();
    Log.i(TAG, TAG + " onDestroy");

    // 프린터 연결 해제
    if (mPrintService != null) {
        new ProgressDlgDiscon(ShipmentActivity.this).execute();
    }

    // ProgressDialog 종료
    if (cDialog != null && cDialog.isShowing()) {
        cDialog.dismiss();
    }
}
```

**주요 동작**:
1. 프린터 연결 해제 (`ProgressDlgDiscon` AsyncTask)
2. 연결 대기 ProgressDialog 종료

---

## 4. Handler 및 메시지 처리

### 4.1 mHandler (490~592줄)

```java
public Handler mHandler = new Handler() {
    public void handleMessage(Message msg) {
        // ...
    }
};
```

**처리 메시지 유형**:

| Message | 값 | 처리 내용 |
|---------|---|----------|
| MESSAGE_ROWCHECK | 1000 | 선택된 상품이 존재할 때 리스트 스크롤 |
| MESSAGE_COMPLETE | 1001 | 계근이 완료된 상품 Toast 표시 |
| MESSAGE_SEARCHCHECK | 1002 | 검색된 상품으로 리스트 스크롤 및 체크 |
| MESSAGE_DEVICE_NAME | 1 | 프린터 연결 성공 (프린터 이름 표시) |
| MESSAGE_TOAST | 2 | Toast 메시지 표시 (프린터 에러 등) |
| MESSAGE_READ | 3 | Woosim 프린터 응답 데이터 처리 |
| MESSAGE_SEARCH | 4 | 프린터 재검색 (DeviceListActivity 호출) |
| MESSAGE_REPRINT | 5 | 재출력 (계근 상세에서) |
| WoosimService.MESSAGE_PRINTER | - | Woosim 프린터 이벤트 |

#### 4.1.1 프린터 연결 성공 처리 (522~533줄)

```java
case MESSAGE_DEVICE_NAME:
    // 프린터 이름 저장
    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);

    // 성공 Toast 및 사운드
    Toast.makeText(getApplicationContext(),
        "Connected to " + mConnectedDeviceName,
        Toast.LENGTH_SHORT).show();
    sound_pool.play(sound_success, 1.0f, 1.0f, 0, 0, 1.0f);

    // 연결 대기 다이얼로그 종료
    if (cDialog != null && cDialog.isShowing()) {
        cDialog.dismiss();
    }
    break;
```

#### 4.1.2 재출력 처리 (559~578줄)

```java
case MESSAGE_REPRINT:
    String print_weight_str = msg.getData().getString("WEIGHT").toString();
    String making_date = msg.getData().getString("MAKINGDATE").toString();

    if (Common.searchType.equals("2") || Common.searchType.equals("5")) {
        // 홈플러스
        setHomeplusPrinting(Double.parseDouble(print_weight_str),
            arSM.get(select_position), true);
    } else if (Common.searchType.equals("6")) {
        // 롯데
        String box_order = msg.getData().getString("BOX_ORDER").toString();
        setPrintingLotte(Double.parseDouble(print_weight_str),
            arSM.get(select_position), true, making_date, box_order);
    } else if (Common.searchType.equals("7")) {
        // 생산
        setPrinting_prod(Double.parseDouble(print_weight_str),
            arSM.get(select_position), true);
    } else {
        // 이마트
        setPrinting(Double.parseDouble(print_weight_str),
            arSM.get(select_position), true, making_date);
    }
    break;
```

---

## 5. 키보드 제어

### 5.1 hideKeyboard (284~287줄)

```java
private void hideKeyboard() {
    InputMethodManager btn_input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    btn_input.hideSoftInputFromWindow(
        this.getCurrentFocus().getWindowToken(),
        InputMethodManager.HIDE_NOT_ALWAYS
    );
}
```

**사용처**:
- 입력 버튼 클릭시 키보드 자동 숨김 (296줄)

---

## 6. 출하 유형별 특징

### 6.1 searchType 값과 의미

| searchType | 출하 유형 | 프린터 | 레이아웃 | 특이사항 |
|-----------|----------|--------|---------|---------|
| **"0"** | 이마트 출하 | 필수 | activity_shipment | 소수점 1자리 |
| **"1"** | 생산 계근 (이노이천) | **비활성화** | activity_shipment | 프린터 OFF |
| **"2"** | 홈플러스 출하 | 필수 | activity_shipment | BOX_ORDER 사용 |
| **"3"** | 도매 출하 | 필수 | **activity_shipment_wholesale** | complete API 미호출 |
| **"4"** | 도매 비정량 | 필수 | activity_shipment | 중복 허용 |
| **"5"** | 홈플러스 비정량 | 필수 | activity_shipment | 중복 허용 |
| **"6"** | 롯데 출하 | 필수 | activity_shipment | lotte_TryCount |
| **"7"** | 생산 계근 | 필수 | activity_shipment | - |

### 6.2 주요 차이점 요약

```
이마트 (searchType="0")
├─ 프린터: 필수
├─ 소수점: 1자리
├─ 바코드 형식: M0~M9, E0~E3
├─ 미트센터 특수 처리: 추가 라벨 출력
└─ 전송: 개별 건

홈플러스 (searchType="2","5")
├─ 프린터: 필수
├─ 소수점: 2자리
├─ BOX_ORDER 관리
├─ 비정량("5"): 중복 바코드 허용
└─ 전송: 개별 건

롯데 (searchType="6")
├─ 프린터: 필수
├─ 소수점: 2자리
├─ lotte_TryCount: 1~9999 순환
├─ 바코드 형식: L0
└─ 전송: 개별 건

생산 (searchType="1","7")
├─ 프린터: "1"은 비활성화, "7"은 필수
├─ 소수점: 2자리
├─ 바코드 형식: P0
└─ 전송: 일괄 (##구분)

도매 (searchType="3","4","5")
├─ 프린터: 필수
├─ 레이아웃: "3"은 wholesale
├─ 비정량("4","5"): 중복 바코드 허용
├─ 전송: 일괄 (##구분)
└─ complete_shipment API 미호출 ⚠️
```

---

## 7. 다음 문서 안내

Part 1에서는 ShipmentActivity의 개요, 클래스 구조, 생명주기 메서드를 살펴보았습니다.

**다음 문서에서 계속됩니다**:
- [Part 2: 바코드 스캔 처리](ShipmentActivity_Part2.md)
  - setBarcodeMsg() 메서드 상세 분석
  - ITEM_TYPE별 중량 추출 로직
  - 패커상품 스캔 및 BL 스캔 처리
  - 중복 바코드 체크
  - 소비기한 검증

---

**작성일**: 2025-01-27
**Part**: 1/4
**다음**: [ShipmentActivity Part 2 →](ShipmentActivity_Part2.md)
