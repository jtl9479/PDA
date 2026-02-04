# SettingActivity 문서

## 1. 개요

- **파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\SettingActivity.java`
- **라인 수**: 136줄
- **주요 역할**: 프린터 설정 ON/OFF 관리 및 계근내역 삭제 기능 제공
- **패키지**: `com.rgbsolution.highland_emart`
- **상속**: `AppCompatActivity`

이 액티비티는 사용자가 프린터 사용 여부를 설정하고, 숨겨진 기능으로 전체 계근내역을 삭제할 수 있는 설정 화면을 제공합니다.

## 2. 클래스 구조

```java
public class SettingActivity extends AppCompatActivity {
    // 멤버 변수
    private final String TAG = "SettingActivity";
    private Button btn_on;                    // 프린터 ON 버튼
    private Button btn_off;                   // 프린터 OFF 버튼
    private int clkCount = 0;                 // 숨겨진 기능 활성화를 위한 클릭 카운터

    SharedPreferences spfBluetooth;           // 블루투스 설정 저장소
    SharedPreferences.Editor editor;          // 설정 편집기
}
```

### 주요 구성 요소

| 구성 요소 | 타입 | 설명 |
|----------|------|------|
| `btn_on` | Button | 프린터 사용 ON 버튼 |
| `btn_off` | Button | 프린터 사용 OFF 버튼 |
| `clkCount` | int | 이미지뷰 클릭 횟수 (7회 이상 시 삭제 다이얼로그 표시) |
| `spfBluetooth` | SharedPreferences | 프린터 설정 저장 |
| `editor` | SharedPreferences.Editor | 설정 값 수정 |

## 3. 주요 메서드 분석

### 3.1 onCreate - 초기화 (라인 28-49)

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);

    btn_on = (Button) findViewById(R.id.btn_on);
    btn_off = (Button) findViewById(R.id.btn_off);

    spfBluetooth = getSharedPreferences("spfBluetooth", Activity.MODE_PRIVATE);
    editor = spfBluetooth.edit();

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    // 현재 프린터 설정 상태에 따라 버튼 UI 업데이트
    if (Common.printer_setting) {
        btn_on.setBackgroundResource(R.drawable.setting_on_green);
        btn_off.setBackgroundResource(R.drawable.setting_off_gray);
    } else {
        btn_on.setBackgroundResource(R.drawable.setting_on_gray);
        btn_off.setBackgroundResource(R.drawable.setting_off_green);
    }
}
```

**주요 기능**:
- UI 컴포넌트 초기화
- SharedPreferences 설정 (이름: "spfBluetooth")
- ActionBar에 뒤로가기 버튼 활성화
- `Common.printer_setting` 값에 따라 버튼 UI 상태 설정

### 3.2 프린터 설정 저장 - onDestroy (라인 122-136)

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    Log.i(TAG, TAG + " onDestroy");

    if (Common.printer_setting) {
        // 프린터 사용 ON 상태 저장
        editor.putBoolean("printer_setting", true);
        editor.commit();
    } else {
        // 프린터 사용 OFF 상태 저장 및 프린터 주소 초기화
        editor.putBoolean("printer_setting", false);
        editor.putString("printer_address", "");
        editor.commit();
        Common.printer_address = "";
    }
}
```

**주요 기능**:
- 액티비티 종료 시 프린터 설정 상태를 SharedPreferences에 저장
- 프린터 OFF 시 저장된 프린터 주소를 초기화

**저장 키**:
- `printer_setting`: boolean (프린터 사용 여부)
- `printer_address`: String (프린터 블루투스 주소)

## 4. UI 리스너

### 4.1 onClick - 버튼 클릭 처리 (라인 51-90)

```java
public void onClick(View v) {
    switch (v.getId()) {
        case R.id.btn_on:
            Log.d(TAG, "==== ON Click ====");
            Common.printer_setting = true;
            btn_on.setBackgroundResource(R.drawable.setting_on_green);
            btn_off.setBackgroundResource(R.drawable.setting_off_gray);
            break;

        case R.id.btn_off:
            Log.d(TAG, "==== OFF Click ====");
            Common.printer_setting = false;
            btn_on.setBackgroundResource(R.drawable.setting_on_gray);
            btn_off.setBackgroundResource(R.drawable.setting_off_green);
            break;

        case R.id.imageView:
            clkCount = clkCount + 1;

            if(clkCount > 6){
                // 7회 이상 클릭 시 전체 삭제 다이얼로그 표시
                new AlertDialog.Builder(SettingActivity.this, R.style.AppCompatDialogStyle)
                    .setIcon(R.drawable.highland)
                    .setTitle(alertTitle)
                    .setMessage("전체계근내역을 삭제 하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("삭제",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBHandler.deletequeryAllGoodsWet(getApplicationContext());
                                Toast.makeText(getApplicationContext(),
                                    "전체계근내역이 삭제 되었습니다.",
                                    Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(buttonNo, null).show();
                clkCount = 0;
            }
            break;
    }
}
```

**처리 이벤트**:
1. **btn_on 클릭**: 프린터 사용 활성화, UI 업데이트
2. **btn_off 클릭**: 프린터 사용 비활성화, UI 업데이트
3. **imageView 클릭**: 7회 이상 클릭 시 전체 계근내역 삭제 다이얼로그 표시 (숨겨진 기능)

### 4.2 onOptionsItemSelected - 메뉴 선택 처리 (라인 92-102)

```java
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
        case android.R.id.home:
            finish();  // 뒤로가기 버튼 클릭 시 액티비티 종료
            break;
    }
    return super.onOptionsItemSelected(item);
}
```

## 5. 코드 위치 참조

### 주요 메서드 위치

| 메서드 | 라인 범위 | 설명 |
|--------|----------|------|
| `onCreate()` | 28-49 | 액티비티 초기화 및 UI 설정 |
| `onClick()` | 51-90 | 버튼 클릭 이벤트 처리 |
| `onOptionsItemSelected()` | 92-102 | ActionBar 메뉴 처리 |
| `onResume()` | 104-108 | 액티비티 재개 시 로그 출력 |
| `onStart()` | 110-114 | 액티비티 시작 시 로그 출력 |
| `onPause()` | 116-120 | 액티비티 일시정지 시 로그 출력 |
| `onDestroy()` | 122-136 | 액티비티 종료 시 설정 저장 |

### 멤버 변수 위치

| 변수 | 라인 | 설명 |
|------|------|------|
| `TAG` | 20 | 로그 태그 |
| `btn_on` | 21 | 프린터 ON 버튼 |
| `btn_off` | 22 | 프린터 OFF 버튼 |
| `clkCount` | 23 | 클릭 카운터 |
| `spfBluetooth` | 25 | SharedPreferences 객체 |
| `editor` | 26 | SharedPreferences 편집기 |

### 핵심 로직 위치

| 기능 | 라인 | 설명 |
|------|------|------|
| SharedPreferences 초기화 | 36-37 | "spfBluetooth" 이름으로 생성 |
| 프린터 ON 처리 | 53-58 | `Common.printer_setting = true` |
| 프린터 OFF 처리 | 59-64 | `Common.printer_setting = false` |
| 숨겨진 삭제 기능 | 65-88 | 이미지뷰 7회 클릭 시 활성화 |
| 설정 저장 | 126-134 | onDestroy에서 SharedPreferences에 저장 |

## 6. 주요 특징

### 6.1 프린터 설정 관리
- **전역 상태**: `Common.printer_setting`으로 앱 전체에서 프린터 사용 여부 관리
- **영구 저장**: SharedPreferences를 통해 앱 재시작 후에도 설정 유지
- **연동 초기화**: 프린터 OFF 시 블루투스 주소 자동 초기화

### 6.2 숨겨진 관리 기능
- 이미지뷰를 7회 이상 클릭하면 전체 계근내역 삭제 다이얼로그 표시
- `DBHandler.deletequeryAllGoodsWet()` 메서드로 데이터 삭제
- 관리자용 숨겨진 기능으로 일반 사용자의 실수 방지

### 6.3 UI 피드백
- 프린터 ON/OFF 상태에 따라 버튼 배경 이미지 변경
  - ON 활성화: `setting_on_green`, OFF: `setting_off_gray`
  - OFF 활성화: `setting_off_green`, ON: `setting_on_gray`

## 7. 연관 클래스

- **Common**: 전역 설정 값 관리 (`printer_setting`, `printer_address`)
- **DBHandler**: 데이터베이스 작업 처리 (`deletequeryAllGoodsWet()`)
- **R.layout.activity_setting**: 설정 화면 레이아웃

## 8. 개선 가능 사항

1. **SharedPreferences 저장 타이밍**: 현재는 onDestroy에서만 저장하므로, 강제 종료 시 설정이 유실될 수 있음. onPause에서 저장하는 것이 안전함.
2. **프린터 연결 상태 확인**: 현재 코드에는 실제 프린터 목록 조회나 연결 기능이 없음 (제목과 불일치).
3. **하드코딩된 문자열**: "전체계근내역을 삭제 하시겠습니까?" 등의 문자열을 strings.xml로 분리 권장.
4. **클릭 카운터 초기화**: imageView 클릭 카운터가 특정 시간이 지나면 자동 리셋되도록 개선 가능.
