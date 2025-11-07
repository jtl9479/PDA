# MainActivity 클래스 문서

## 1. 개요

### 파일 정보
- **파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\MainActivity.java`
- **총 라인 수**: 663줄
- **패키지명**: `com.rgbsolution.highland_emart`

### 상속 관계
```java
public class MainActivity extends AppCompatActivity
```
- Android Support Library의 `AppCompatActivity`를 상속
- Android 액티비티 생명주기와 UI 기능 제공

### 주요 역할
Highland E-Mart PDA 애플리케이션의 **메인 메뉴 화면**으로서 다음 기능을 담당합니다:
- 6가지 작업 유형(출하대상, 생산대상, 홈플러스, 도매업체, 비정량, 롯데) 선택
- 날짜 선택 및 관리
- 서버로부터 출하/생산 리스트 다운로드
- 각 작업 유형별 계근 입력 화면으로 이동
- 앱 설정 및 종료 관리

---

## 2. 클래스 구조

### 주요 필드

```java
// 로깅 태그
private final String TAG = "MainActivity";

// 진동 서비스
private Vibrator vibrator;

// 생산/출하 구분 플래그
private String chkProdShip = "";

// 날짜 선택용 캘린더
Calendar calendar = Calendar.getInstance();
```

#### 필드 상세 설명
| 필드명 | 타입 | 설명 |
|--------|------|------|
| `TAG` | String | 로그 출력용 태그 (디버깅) |
| `vibrator` | Vibrator | 사용자 피드백용 진동 서비스 |
| `chkProdShip` | String | "ship", "prod", "homplus", "lotte" 등 작업 유형 구분 |
| `calendar` | Calendar | 날짜 선택 및 포맷팅에 사용 |

---

## 3. 생명주기 메서드

### onCreate()
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
}
```
- **역할**: 액티비티 초기화
- **동작**:
  - 레이아웃 설정 (`activity_main`)
  - Vibrator 서비스 초기화

### onResume()
```java
@Override
protected void onResume() {
    super.onResume();
    Log.i(TAG, TAG + " onResume");
}
```
- **역할**: 액티비티가 포그라운드로 돌아올 때 호출
- **라인**: 609-612

### onStart()
```java
@Override
protected void onStart() {
    super.onStart();
    Log.i(TAG, TAG + " onStart");
}
```
- **역할**: 액티비티가 사용자에게 보이기 시작할 때 호출
- **라인**: 615-618

### onPause()
```java
@Override
protected void onPause() {
    super.onPause();
    Log.i(TAG, TAG + " onPause");
}
```
- **역할**: 액티비티가 백그라운드로 이동할 때 호출
- **라인**: 621-624

### onDestroy()
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    Log.i(TAG, TAG + " onDestroy");
}
```
- **역할**: 액티비티가 소멸될 때 호출
- **라인**: 627-630

---

## 4. 주요 메서드 분석

### 4.1 날짜 선택 기능

#### DatePickerDialog.OnDateSetListener
```java
DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener(){
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));
        if(calendar.get(Calendar.MONTH)+1 < 10) {
            inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
        }else{
            inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
        }

        if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
            inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        }else{
            inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        }

        Common.selectDay = inPutDay;
        Log.i(TAG, TAG + "=====================selectDay======================" + inPutDay);
    }
};
```
- **라인**: 36-58
- **역할**: 사용자가 날짜를 선택할 때 실행되는 리스너
- **동작**:
  1. 선택된 날짜를 Calendar 객체에 저장
  2. "YYYYMMDD" 형식으로 문자열 변환 (예: 20250131)
  3. `Common.selectDay`에 저장하여 전역적으로 사용

#### 날짜 포맷팅 로직
- **연도**: 4자리 (예: 2025)
- **월**: 2자리 (1-9월은 앞에 0 추가, 예: 01, 02, ... 12)
- **일**: 2자리 (1-9일은 앞에 0 추가, 예: 01, 02, ... 31)

### 4.2 메뉴 관리

#### onCreateOptionsMenu()
```java
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
}
```
- **라인**: 68-71
- **역할**: 액션바 메뉴 생성

#### onOptionsItemSelected()
```java
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    Intent i;
    int id = item.getItemId();

    if (id == R.id.action_pinrtsettings) {
        i = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(i);
        return true;
    }

    if (id == R.id.action_daysettings) {
        new DatePickerDialog(MainActivity.this, date,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show();
        return true;
    }

    return super.onOptionsItemSelected(item);
}
```
- **라인**: 74-91
- **역할**: 메뉴 아이템 선택 처리
- **메뉴 항목**:
  - `action_pinrtsettings`: 설정 화면으로 이동
  - `action_daysettings`: 날짜 선택 다이얼로그 표시

### 4.3 키 이벤트 처리

#### onKeyDown()
```java
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
        exitDialog();
    }
    return super.onKeyDown(keyCode, event);
}
```
- **라인**: 634-639
- **역할**: 뒤로가기 버튼 처리
- **동작**: BACK 버튼 누를 시 종료 다이얼로그 표시

#### exitDialog()
```java
public void exitDialog() {
    String alertTitle = getResources().getString(R.string.app_name);
    String buttonMessage = getString(R.string.exit_message);
    String buttonYes = getString(R.string.exit_yes);
    String buttonNo = getString(R.string.exit_no);

    new AlertDialog.Builder(MainActivity.this, R.style.AppCompatDialogStyle)
            .setIcon(R.drawable.highland)
            .setTitle(alertTitle)
            .setMessage(buttonMessage)
            .setCancelable(false)
            .setPositiveButton(buttonYes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton(buttonNo, null).show();
}
```
- **라인**: 642-662
- **역할**: 앱 종료 확인 다이얼로그 표시
- **동작**:
  - "예" 버튼: `finish()` 호출하여 액티비티 종료
  - "아니오" 버튼: 다이얼로그 닫기

---

## 5. UI 리스너 (onClick 메서드)

### onClick() 메서드 구조
```java
public void onClick(View v) {
    Intent i;
    switch (v.getId()) {
        case R.id.btnDay:
            // 날짜 선택
            break;
        case R.id.buttonDelete:
            // 출하대상 삭제
            break;
        // ... 기타 케이스들
    }
}
```
- **라인**: 93-606
- **역할**: 모든 버튼 클릭 이벤트를 중앙에서 처리

### 5.1 날짜 및 데이터 관리 버튼

#### btnDay - 날짜 선택
```java
case R.id.btnDay:
    new DatePickerDialog(MainActivity.this, date,
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)).show();
    break;
```
- **라인**: 96-113
- **역할**: 날짜 선택 다이얼로그 표시

#### buttonDelete - 출하대상 삭제
```java
case R.id.buttonDelete:
    DBHandler.deletequeryShipment(getApplicationContext());
    Toast.makeText(getApplicationContext(), "출하대상이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
    break;
```
- **라인**: 114-117
- **역할**: 로컬 DB의 모든 출하대상 데이터 삭제

### 5.2 데이터 다운로드 버튼 (6가지 유형)

각 다운로드 버튼은 공통적으로 다음 작업을 수행합니다:
1. `Common.searchType` 설정
2. 기존 출하대상 데이터 삭제
3. 날짜가 미선택시 현재 날짜로 설정
4. 바코드 정보 및 계근 데이터 삭제
5. `ProgressDlgShipSearch` 실행하여 서버 통신

#### 1) btnDownload - 출하대상 받기
```java
case R.id.btnDownload:
    Log.i(TAG, TAG + "=====================출하대상받기======================" + Common.selectDay);

    chkProdShip = "ship";
    Common.searchType = "0";

    DBHandler.deletequeryShipment(getApplicationContext());

    if(Common.selectDay == ""){
        // 현재 날짜로 설정
        String inPutDay = String.valueOf(calendar.get(Calendar.YEAR));
        if(calendar.get(Calendar.MONTH)+1 < 10) {
            inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
        }else{
            inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.MONTH)+1);
        }

        if(calendar.get(Calendar.DAY_OF_MONTH) < 10){
            inPutDay = inPutDay + "0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        }else{
            inPutDay = inPutDay + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        }
        Common.selectDay = inPutDay;
    }

    Log.i(TAG, TAG + "=====================Common.selectDay======================" + Common.selectDay);
    Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);

    DBHandler.deletequeryBarcodeInfo(getApplicationContext());
    DBHandler.deletequeryGoodsWet(getApplicationContext());
    new ProgressDlgShipSearch(this).execute();

    break;
```
- **라인**: 118-154
- **searchType**: "0"
- **chkProdShip**: "ship"
- **용도**: 일반 출하대상 데이터 다운로드

#### 2) btnproductionlist - 생산대상 받기
```java
case R.id.btnproductionlist:
    Log.i(TAG, TAG + "=====================생산대상받기======================" + Common.selectDay);

    chkProdShip = "prod";
    Common.searchType = "1";

    DBHandler.deletequeryShipment(getApplicationContext());
    // ... (날짜 설정 로직 - 출하대상과 동일)

    DBHandler.deletequeryBarcodeInfo(getApplicationContext());
    DBHandler.deletequeryGoodsWet(getApplicationContext());
    new ProgressDlgShipSearch(this).execute();

    break;
```
- **라인**: 155-190
- **searchType**: "1"
- **chkProdShip**: "prod"
- **용도**: 생산대상 데이터 다운로드

#### 3) btnDownloadHomeplus - 홈플러스 출하대상 받기
```java
case R.id.btnDownloadHomeplus:
    Log.i(TAG, TAG + "=====================홈플러스출하대상받기======================" + Common.selectDay);

    chkProdShip = "homplus";
    Common.searchType = "2";

    DBHandler.deletequeryShipment(getApplicationContext());
    // ... (날짜 설정 로직)

    DBHandler.deletequeryBarcodeInfo(getApplicationContext());
    DBHandler.deletequeryGoodsWet(getApplicationContext());
    new ProgressDlgShipSearch(this).execute();

    break;
```
- **라인**: 191-226
- **searchType**: "2"
- **chkProdShip**: "homplus"
- **용도**: 홈플러스 하이퍼 출하대상 다운로드

#### 4) btnDownloadWholesale - 도매업체 출하대상 받기
```java
case R.id.btnDownloadWholesale:
    Log.i(TAG, TAG + "=====================도매업체출하대상받기======================" + Common.selectDay);

    Common.searchType = "3";

    DBHandler.deletequeryShipment(getApplicationContext());
    // ... (날짜 설정 로직)

    DBHandler.deletequeryBarcodeInfo(getApplicationContext());
    DBHandler.deletequeryGoodsWet(getApplicationContext());
    new ProgressDlgShipSearch(this).execute();

    break;
```
- **라인**: 227-261
- **searchType**: "3"
- **용도**: 도매업체 출하대상 다운로드

#### 5) btnproductionNonfixedlist - 비정량 출하대상 받기
```java
case R.id.btnproductionNonfixedlist:
    Log.i(TAG, TAG + "=====================비정량출하대상받기======================" + Common.selectDay);

    Common.searchType = "4";

    DBHandler.deletequeryShipment(getApplicationContext());
    // ... (날짜 설정 로직)

    DBHandler.deletequeryBarcodeInfo(getApplicationContext());
    DBHandler.deletequeryGoodsWet(getApplicationContext());
    new ProgressDlgShipSearch(this).execute();

    break;
```
- **라인**: 262-296
- **searchType**: "4"
- **용도**: 비정량 출하대상 다운로드

#### 6) btnDownloadLotte - 롯데 출하대상 받기
```java
case R.id.btnDownloadLotte:
    Log.i(TAG, TAG + "=====================롯데출하대상받기======================" + Common.selectDay);

    chkProdShip = "lotte";
    Common.searchType = "6"; // searchType == 5는 홈플러스 비정량

    DBHandler.deletequeryShipment(getApplicationContext());
    // ... (날짜 설정 로직)

    DBHandler.deletequeryBarcodeInfo(getApplicationContext());
    DBHandler.deletequeryGoodsWet(getApplicationContext());
    new ProgressDlgShipSearch(this).execute();

    break;
```
- **라인**: 297-332
- **searchType**: "6"
- **chkProdShip**: "lotte"
- **용도**: 롯데 출하대상 다운로드
- **참고**: searchType "5"는 홈플러스 비정량용으로 예약됨

#### 7) btnWetHomeplusNon - 홈플러스 비정량 출하대상 받기
```java
case R.id.btnWetHomeplusNon:
    Log.i(TAG, TAG + "=====================홈플러스 비정량 출하대상받기======================" + Common.selectDay);

    Common.searchType = "5";

    DBHandler.deletequeryShipment(getApplicationContext());
    // ... (날짜 설정 로직)

    DBHandler.deletequeryBarcodeInfo(getApplicationContext());
    DBHandler.deletequeryGoodsWet(getApplicationContext());
    new ProgressDlgShipSearch(this).execute();

    break;
```
- **라인**: 432-468
- **searchType**: "5"
- **용도**: 홈플러스 비정량 출하대상 다운로드

#### 8) btnproductionlist4print - 생산대상 받기 (라벨)
```java
case R.id.btnproductionlist4print:
    Log.i(TAG, TAG + "=====================생산대상받기(라벨)======================" + Common.selectDay);

    chkProdShip = "prod";
    Common.searchType = "7";

    DBHandler.deletequeryShipment(getApplicationContext());
    // ... (날짜 설정 로직)

    DBHandler.deletequeryBarcodeInfo(getApplicationContext());
    DBHandler.deletequeryGoodsWet(getApplicationContext());
    new ProgressDlgShipSearch(this).execute();

    break;
```
- **라인**: 533-568
- **searchType**: "7"
- **chkProdShip**: "prod"
- **용도**: 라벨 출력용 생산대상 다운로드

### 5.3 계근 입력 버튼

각 계근 입력 버튼은 공통적으로 다음 작업을 수행합니다:
1. `Common.searchType` 확인 (올바른 데이터가 다운로드되었는지 검증)
2. DB에서 출하대상 리스트 조회
3. 리스트가 있으면 `ShipmentActivity`로 이동
4. 리스트가 없으면 에러 메시지 표시 및 진동

#### 1) btnWet - 출하 계근 입력
```java
case R.id.btnWet:
    if (!Common.searchType.equals("0")) {
        Toast.makeText(getApplicationContext(),
            "출하를 위해 출하 리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
        break;
    }

    Common.searchType = "0";
    ArrayList<Shipments_Info> list_si = DBHandler.selectqueryAllShipment(MainActivity.this);

    if (list_si.size() > 0) {
        i = new Intent(this, ShipmentActivity.class);
        startActivity(i);
    } else {
        Toast.makeText(getApplicationContext(),
            "출하대상 리스트가 없습니다.\n리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
    }
    break;
```
- **라인**: 333-357
- **검증 searchType**: "0"
- **이동 화면**: `ShipmentActivity`

#### 2) btnProdWet - 생산 계근 입력
```java
case R.id.btnProdWet:
    if (!Common.searchType.equals("1")) {
        Toast.makeText(getApplicationContext(),
            "생산 계근을 위해 생산 리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
        break;
    }

    Common.searchType = "1";
    ArrayList<Shipments_Info> list_prod_si = DBHandler.selectqueryAllShipment(MainActivity.this);

    if (list_prod_si.size() > 0) {
        i = new Intent(this, ShipmentActivity.class);
        startActivity(i);
    } else {
        Toast.makeText(getApplicationContext(),
            "생산대상 리스트가 없습니다.\n리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
    }
    break;
```
- **라인**: 385-408
- **검증 searchType**: "1"
- **이동 화면**: `ShipmentActivity`

#### 3) btnWetHomeplus - 홈플러스 계근 입력
```java
case R.id.btnWetHomeplus:
    if (!Common.searchType.equals("2")) {
        Toast.makeText(getApplicationContext(),
            "홈플러스 출고분 계근을 위해 홈플러스 하이퍼 출고 리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
        break;
    }

    Common.searchType = "2";
    ArrayList<Shipments_Info> list_homeplus_si = DBHandler.selectqueryAllShipment(MainActivity.this);

    if (list_homeplus_si.size() > 0) {
        i = new Intent(this, ShipmentActivity.class);
        startActivity(i);
    } else {
        Toast.makeText(getApplicationContext(),
            "홈플러스 하이퍼 출고 리스트가 없습니다.\n리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
    }
    break;
```
- **라인**: 410-429
- **검증 searchType**: "2"
- **이동 화면**: `ShipmentActivity`

#### 4) btnWetWholesale - 도매업체 계근 입력
```java
case R.id.btnWetWholesale:
    if (!Common.searchType.equals("3")) {
        Toast.makeText(getApplicationContext(),
            "출하를 위해 출하 리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
        break;
    }

    Common.searchType = "3";
    ArrayList<Shipments_Info> list_wholesale_si = DBHandler.selectqueryAllShipment(MainActivity.this);

    if (list_wholesale_si.size() > 0) {
        i = new Intent(this, ShipmentActivity.class);
        startActivity(i);
    } else {
        Toast.makeText(getApplicationContext(),
            "출하대상 리스트가 없습니다.\n리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
    }
    break;
```
- **라인**: 359-383
- **검증 searchType**: "3"
- **이동 화면**: `ShipmentActivity`

#### 5) btnProdNonfixedWet - 비정량 계근 입력
```java
case R.id.btnProdNonfixedWet:
    if (!Common.searchType.equals("4")) {
        Toast.makeText(getApplicationContext(),
            "비정량 출고분 계근을 위해 비정량 출고 리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
        break;
    }

    Common.searchType = "4";
    ArrayList<Shipments_Info> list_prodNonfixed_si = DBHandler.selectqueryAllShipment(MainActivity.this);

    if (list_prodNonfixed_si.size() > 0) {
        i = new Intent(this, ShipmentActivity.class);
        startActivity(i);
    } else {
        Toast.makeText(getApplicationContext(),
            "비정량 출고 리스트가 없습니다.\n리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
    }
    break;
```
- **라인**: 493-512
- **검증 searchType**: "4"
- **이동 화면**: `ShipmentActivity`

#### 6) btnWetHomeplusNon2 - 홈플러스 비정량 계근 입력
```java
case R.id.btnWetHomeplusNon2:
    if (!Common.searchType.equals("5")) {
        Toast.makeText(getApplicationContext(),
            "홈플러스 출고분 계근을 위해 홈플러스 비정량 출고 리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
        break;
    }

    Common.searchType = "5";
    ArrayList<Shipments_Info> list_homeplus_si2 = DBHandler.selectqueryAllShipment(MainActivity.this);

    if (list_homeplus_si2.size() > 0) {
        i = new Intent(this, ShipmentActivity.class);
        startActivity(i);
    } else {
        Toast.makeText(getApplicationContext(),
            "홈플러스 비정량 출고 리스트가 없습니다.\n리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
    }
    break;
```
- **라인**: 471-491
- **검증 searchType**: "5"
- **이동 화면**: `ShipmentActivity`

#### 7) btnWetLotte - 롯데 계근 입력
```java
case R.id.btnWetLotte:
    if (!Common.searchType.equals("6")) {
        Toast.makeText(getApplicationContext(),
            "롯데 출고분 계근을 위해 롯데 출고 리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
        break;
    }

    Common.searchType = "6";
    ArrayList<Shipments_Info> list_Lotte_si = DBHandler.selectqueryAllShipment(MainActivity.this);

    if (list_Lotte_si.size() > 0) {
        i = new Intent(this, ShipmentActivity.class);
        startActivity(i);
    } else {
        Toast.makeText(getApplicationContext(),
            "롯데 출고 리스트가 없습니다.\n리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
    }
    break;
```
- **라인**: 513-532
- **검증 searchType**: "6"
- **이동 화면**: `ShipmentActivity`

#### 8) btnProdWet4print - 생산 계근 입력 (라벨)
```java
case R.id.btnProdWet4print:
    Log.i(TAG, TAG + "=====================생산입력시작(라벨)======================" + Common.selectDay);

    if (!Common.searchType.equals("7")) {
        Toast.makeText(getApplicationContext(),
            "생산 계근을 위해 생산 리스트(라벨)를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
        break;
    }

    Common.searchType = "7";
    ArrayList<Shipments_Info> list_prod_si2 = DBHandler.selectqueryAllShipment(MainActivity.this);

    if (list_prod_si2.size() > 0) {
        i = new Intent(this, ShipmentActivity.class);
        startActivity(i);
    } else {
        Toast.makeText(getApplicationContext(),
            "생산대상 리스트(라벨)가 없습니다.\n리스트를 받아주세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
    }
    break;
```
- **라인**: 569-588
- **검증 searchType**: "7"
- **이동 화면**: `ShipmentActivity`

### 5.4 기타 버튼

#### btnProdWetCalc - 생산 계산
```java
case R.id.btnProdWetCalc:
    i = new Intent(this, ProductionActivity.class);
    startActivity(i);
    break;
```
- **라인**: 590-595
- **역할**: `ProductionActivity`로 이동 (생산 계산 기능)

#### btnClose - 종료
```java
case R.id.btnClose:
    exitDialog();
    break;
```
- **라인**: 602-604
- **역할**: 종료 다이얼로그 호출

---

## 6. searchType별 처리

### searchType 매핑 테이블

| searchType | 작업 유형 | 다운로드 버튼 | 계근 입력 버튼 | chkProdShip |
|-----------|----------|-------------|-------------|-------------|
| "0" | 출하대상 | btnDownload | btnWet | "ship" |
| "1" | 생산대상 | btnproductionlist | btnProdWet | "prod" |
| "2" | 홈플러스 하이퍼 | btnDownloadHomeplus | btnWetHomeplus | "homplus" |
| "3" | 도매업체 | btnDownloadWholesale | btnWetWholesale | - |
| "4" | 비정량 출하 | btnproductionNonfixedlist | btnProdNonfixedWet | - |
| "5" | 홈플러스 비정량 | btnWetHomeplusNon | btnWetHomeplusNon2 | - |
| "6" | 롯데 | btnDownloadLotte | btnWetLotte | "lotte" |
| "7" | 생산(라벨) | btnproductionlist4print | btnProdWet4print | "prod" |

### searchType 검증 로직

모든 계근 입력 버튼에서는 다음 패턴의 검증을 수행합니다:

```java
if (!Common.searchType.equals("X")) {
    Toast.makeText(getApplicationContext(),
        "적절한 리스트를 받아주세요.",
        Toast.LENGTH_SHORT).show();
    vibrator.vibrate(300);
    break;
}
```

이는 사용자가 올바른 순서로 작업을 진행하도록 보장합니다:
1. 먼저 데이터 다운로드 버튼 클릭 (searchType 설정)
2. 그 다음 해당 searchType에 맞는 계근 입력 버튼 클릭

### searchType 전역 관리

`Common.searchType`은 전역 변수로 관리되며:
- 데이터 다운로드 시 설정
- 계근 입력 시 검증
- 서버 통신 시 파라미터로 사용
- 화면 전환 시 전달

---

## 7. 처리 흐름도

### 7.1 데이터 다운로드 흐름

```
[사용자]
   ↓
[다운로드 버튼 클릭]
   ↓
[searchType 설정]
   ↓
[기존 데이터 삭제]
   ├─ DBHandler.deletequeryShipment() - 출하대상 삭제
   ├─ DBHandler.deletequeryBarcodeInfo() - 바코드정보 삭제
   └─ DBHandler.deletequeryGoodsWet() - 계근정보 삭제
   ↓
[날짜 확인]
   ├─ Common.selectDay가 비어있으면
   └─ 현재 날짜로 설정 (YYYYMMDD 형식)
   ↓
[서버 통신]
   └─ new ProgressDlgShipSearch(this).execute()
       ↓
   [서버에서 데이터 다운로드]
       ↓
   [로컬 DB에 저장]
```

### 7.2 계근 입력 흐름

```
[사용자]
   ↓
[계근 입력 버튼 클릭]
   ↓
[searchType 검증]
   ├─ 일치하지 않으면
   │   ├─ 에러 메시지 표시
   │   ├─ 진동 (300ms)
   │   └─ 처리 중단
   └─ 일치하면 계속
   ↓
[DB에서 리스트 조회]
   └─ DBHandler.selectqueryAllShipment()
   ↓
[리스트 크기 확인]
   ├─ 크기 > 0
   │   └─ ShipmentActivity로 이동
   │       └─ Intent로 searchType 전달
   └─ 크기 == 0
       ├─ 에러 메시지 표시
       └─ 진동 (300ms)
```

### 7.3 날짜 선택 흐름

```
[사용자]
   ↓
[날짜 선택 버튼 클릭]
   ├─ 메뉴 > action_daysettings
   ├─ 버튼 > btnDay
   └─ 옵션메뉴 > action_daysettings
   ↓
[DatePickerDialog 표시]
   ↓
[사용자가 날짜 선택]
   ↓
[OnDateSetListener 실행]
   ↓
[날짜 포맷팅]
   ├─ 연도: YYYY (4자리)
   ├─ 월: MM (2자리, 01-12)
   └─ 일: DD (2자리, 01-31)
   ↓
[Common.selectDay에 저장]
   └─ 형식: "YYYYMMDD"
```

### 7.4 앱 종료 흐름

```
[사용자]
   ↓
[종료 트리거]
   ├─ BACK 버튼 누름
   └─ btnClose 버튼 클릭
   ↓
[exitDialog() 호출]
   ↓
[AlertDialog 표시]
   ├─ 제목: 앱 이름
   ├─ 메시지: "종료하시겠습니까?"
   ├─ 긍정 버튼: "예"
   └─ 부정 버튼: "아니오"
   ↓
[사용자 선택]
   ├─ "예" → finish() → 앱 종료
   └─ "아니오" → 다이얼로그 닫기
```

---

## 8. 코드 위치 참조

### 클래스 선언 및 필드
- **라인 28-34**: 클래스 선언 및 필드 정의

### 날짜 관련
- **라인 34-58**: Calendar 및 DatePickerDialog 리스너
- **라인 96-113**: 날짜 선택 버튼 (btnDay)

### 생명주기 메서드
- **라인 61-65**: onCreate()
- **라인 609-612**: onResume()
- **라인 615-618**: onStart()
- **라인 621-624**: onPause()
- **라인 627-630**: onDestroy()

### 메뉴 관련
- **라인 68-71**: onCreateOptionsMenu()
- **라인 74-91**: onOptionsItemSelected()

### 데이터 다운로드 버튼
- **라인 118-154**: 출하대상 받기 (searchType "0")
- **라인 155-190**: 생산대상 받기 (searchType "1")
- **라인 191-226**: 홈플러스 출하대상 받기 (searchType "2")
- **라인 227-261**: 도매업체 출하대상 받기 (searchType "3")
- **라인 262-296**: 비정량 출하대상 받기 (searchType "4")
- **라인 432-468**: 홈플러스 비정량 출하대상 받기 (searchType "5")
- **라인 297-332**: 롯데 출하대상 받기 (searchType "6")
- **라인 533-568**: 생산대상 받기 라벨 (searchType "7")

### 계근 입력 버튼
- **라인 333-357**: 출하 계근 입력 (searchType "0")
- **라인 385-408**: 생산 계근 입력 (searchType "1")
- **라인 410-429**: 홈플러스 계근 입력 (searchType "2")
- **라인 359-383**: 도매업체 계근 입력 (searchType "3")
- **라인 493-512**: 비정량 계근 입력 (searchType "4")
- **라인 471-491**: 홈플러스 비정량 계근 입력 (searchType "5")
- **라인 513-532**: 롯데 계근 입력 (searchType "6")
- **라인 569-588**: 생산 계근 입력 라벨 (searchType "7")

### 기타 버튼
- **라인 114-117**: 출하대상 삭제 버튼
- **라인 590-595**: 생산 계산 버튼
- **라인 602-604**: 종료 버튼

### 키 이벤트 및 다이얼로그
- **라인 634-639**: onKeyDown() (BACK 버튼 처리)
- **라인 642-662**: exitDialog() (종료 다이얼로그)

---

## 9. 주요 의존성 및 연관 클래스

### 9.1 Activity 간 연결
```
MainActivity
   ├─→ SettingActivity (설정 화면)
   ├─→ ShipmentActivity (계근 입력 화면)
   └─→ ProductionActivity (생산 계산 화면)
```

### 9.2 데이터베이스 핸들러 (DBHandler)
```java
DBHandler.deletequeryShipment(Context)        // 출하대상 삭제
DBHandler.deletequeryBarcodeInfo(Context)     // 바코드정보 삭제
DBHandler.deletequeryGoodsWet(Context)        // 계근정보 삭제
DBHandler.selectqueryAllShipment(Context)     // 출하대상 조회
```

### 9.3 공통 데이터 (Common 클래스)
```java
Common.selectDay          // 선택된 날짜 (YYYYMMDD 형식)
Common.searchType         // 작업 유형 식별자 ("0"~"7")
Common.selectWarehouse    // 선택된 창고 정보
```

### 9.4 비동기 작업
```java
ProgressDlgShipSearch     // 서버 통신 및 데이터 다운로드
                          // AsyncTask 기반 백그라운드 작업
```

### 9.5 데이터 모델
```java
Shipments_Info            // 출하/생산 정보를 담는 데이터 클래스
```

---

## 10. 특이사항 및 주의점

### 10.1 주석 처리된 코드
**라인 99-112**: 날짜 변경 시 확인 다이얼로그 (현재 비활성화)
```java
/*new AlertDialog.Builder(MainActivity.this, R.style.AppCompatDialogStyle)
        .setIcon(R.drawable.highland)
        .setTitle(getResources().getString(R.string.app_name))
        .setMessage("기존 출하 대상이 삭제 됩니다. 날짜를 변경 하시겠습니까?")
        ...*/
```
- 원래는 날짜 변경 시 경고 메시지를 표시하려 했으나 현재는 직접 변경

**라인 335-339, 361-365, 392-396**: searchType 대신 chkProdShip로 검증하는 로직 (주석 처리됨)
```java
/*if (chkProdShip.equals("prod")) {
    Toast.makeText(getApplicationContext(), "출하를 위해 출하 리스트를 받아주세요.", Toast.LENGTH_SHORT).show();
    vibrator.vibrate(300);
    break;
}*/
```
- 과거 코드로 보이며, 현재는 `Common.searchType`으로 통일

### 10.2 searchType 번호 순서
searchType이 순차적이지 않습니다:
- 0, 1, 2, 3, 4, 5, 6, 7 순서로 할당
- searchType "5"는 홈플러스 비정량용으로 나중에 추가됨
- 코드 주석(라인 301): "searchType == 5 홈플러스 비정량"

### 10.3 날짜 검증
`Common.selectDay == ""`로 비교하지만, Java에서는 `"".equals(Common.selectDay)` 또는 `Common.selectDay.isEmpty()` 사용이 권장됩니다.
- **위치**: 라인 127, 163, 199, 234, 269, 305, 441, 541

### 10.4 진동 피드백
에러 발생 시 300ms 진동으로 사용자에게 피드백 제공:
```java
vibrator.vibrate(300);
```

### 10.5 데이터 삭제 타이밍
데이터 다운로드 전에 기존 데이터를 모두 삭제:
1. `deletequeryShipment()` - 출하대상
2. `deletequeryBarcodeInfo()` - 바코드정보
3. `deletequeryGoodsWet()` - 계근정보

이는 데이터 중복을 방지하지만, 네트워크 실패 시 기존 데이터도 손실될 위험이 있습니다.

### 10.6 로깅
모든 주요 액션에 대해 Log.i()를 사용하여 디버깅 정보 기록:
```java
Log.i(TAG, TAG + "=====================출하대상받기======================" + Common.selectDay);
```

---

## 11. 개선 제안 사항

### 11.1 코드 중복 제거
날짜 설정 로직이 8개의 케이스에서 반복됩니다 (라인 127-145 등). 별도 메서드로 추출 권장:
```java
private String getCurrentOrSelectedDate() {
    if (Common.selectDay.isEmpty()) {
        Calendar cal = Calendar.getInstance();
        return String.format("%04d%02d%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        );
    }
    return Common.selectDay;
}
```

### 11.2 문자열 비교 개선
```java
// 현재
if(Common.selectDay == "")

// 권장
if(Common.selectDay == null || Common.selectDay.isEmpty())
```

### 11.3 searchType enum 활용
현재 문자열로 관리되는 searchType을 enum으로 변경:
```java
public enum SearchType {
    SHIPMENT("0"),
    PRODUCTION("1"),
    HOMEPLUS("2"),
    WHOLESALE("3"),
    NONFIXED("4"),
    HOMEPLUS_NONFIXED("5"),
    LOTTE("6"),
    PRODUCTION_LABEL("7");

    private final String code;
    // constructor, getter
}
```

### 11.4 리소스 관리
하드코딩된 문자열을 strings.xml로 이동:
```java
// 현재
Toast.makeText(getApplicationContext(), "출하대상이 삭제 되었습니다.", ...)

// 권장
Toast.makeText(getApplicationContext(), getString(R.string.msg_shipment_deleted), ...)
```

### 11.5 에러 처리 강화
데이터베이스 작업 및 네트워크 통신에 대한 예외 처리 추가

---

## 12. 테스트 시나리오

### 12.1 정상 흐름 테스트
1. 앱 실행 → MainActivity 표시 확인
2. 날짜 선택 → Common.selectDay 설정 확인
3. 출하대상 받기 → DB 저장 확인
4. 출하 계근 입력 → ShipmentActivity 이동 확인

### 12.2 에러 케이스 테스트
1. 데이터 다운로드 없이 계근 입력 시도 → 에러 메시지 확인
2. 잘못된 searchType으로 계근 입력 시도 → 진동 및 메시지 확인
3. 빈 리스트 상태에서 계근 입력 시도 → 에러 메시지 확인

### 12.3 UI 테스트
1. BACK 버튼 → 종료 다이얼로그 표시 확인
2. 종료 다이얼로그 "예" → 앱 종료 확인
3. 종료 다이얼로그 "아니오" → 다이얼로그 닫기 확인

---

## 부록: 주요 메서드 Quick Reference

| 메서드명 | 역할 | 라인 |
|---------|------|------|
| onCreate() | 액티비티 초기화 | 61-65 |
| onCreateOptionsMenu() | 메뉴 생성 | 68-71 |
| onOptionsItemSelected() | 메뉴 선택 처리 | 74-91 |
| onClick() | 버튼 클릭 처리 (중앙 라우터) | 93-606 |
| onResume() | 액티비티 재개 | 609-612 |
| onKeyDown() | 키 이벤트 처리 | 634-639 |
| exitDialog() | 종료 다이얼로그 표시 | 642-662 |

---

**문서 작성일**: 2025-10-31
**대상 파일**: MainActivity.java (663줄)
**작성 목적**: 코드 분석 및 유지보수 가이드
