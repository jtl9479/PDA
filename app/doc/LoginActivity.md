# LoginActivity 분석 문서

## 1. 개요

### 파일 정보
- **파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\LoginActivity.java`
- **라인 수**: 265줄
- **패키지**: `com.rgbsolution.highland_emart`
- **상속 관계**: `AppCompatActivity` 상속

### 주요 역할
LoginActivity는 Highland E-MART PDA 애플리케이션의 로그인 화면을 담당하는 Activity입니다. 주요 기능은 다음과 같습니다:

1. **사용자 인증**: 사용자 ID와 비밀번호를 입력받아 서버와 통신하여 로그인 처리
2. **데이터베이스 초기화**: 앱 실행 시 필요한 SQLite 테이블 생성
3. **URL 설정**: 서버 통신에 필요한 각종 API URL 초기화
4. **창고 선택**: 스피너를 통해 작업할 창고(센터) 선택
5. **프린터 설정**: SharedPreferences를 통한 모바일 프린터 설정 로드

---

## 2. 클래스 구조

### 주요 필드

#### 상수 및 로그 태그 (라인 32)
```java
private final String TAG = "LoginActivity";
```
- 로그 출력 시 사용되는 TAG 상수

#### UI 컴포넌트 (라인 34-38)
```java
private ProgressDialog pDialog = null;
private EditText editID;      // ID 입력창
private EditText editPWD;     // PWD 입력창
```
- `pDialog`: 로그인 처리 중 표시되는 프로그레스 다이얼로그
- `editID`: 사용자 ID 입력 필드
- `editPWD`: 사용자 비밀번호 입력 필드

#### 사용자 정보 (라인 40-41)
```java
private String user_id = "";
private String user_pwd = "";
```
- 입력된 사용자 ID와 비밀번호를 저장하는 변수

### 상수 정의

#### 창고 목록 (라인 43)
```java
static final String[] store = {"부산센터","이천1센터","삼일냉장","SWC","탑로지스"};
```
- 선택 가능한 창고(물류센터) 목록을 정의한 배열
- 스피너에서 사용되며, 선택된 값은 `Common.selectWarehouse`에 저장됨

---

## 3. 생명주기 메서드

### onCreate (라인 46-112)
Activity가 생성될 때 호출되는 메서드로, 가장 많은 초기화 작업이 수행됩니다.

#### 데이터베이스 초기화 (라인 51-54)
```java
DBHandler.createqueryShipment(getApplicationContext());                // 출하대상 Table Create
DBHandler.createqueryBarcodeInfo(getApplicationContext());            // 바코드정보 Table Create
DBHandler.createqueryGoodsWet(getApplicationContext());                // 계근내역 Table Create
DBHandler.createqueryGoodsWetProductionCalc(getApplicationContext());
```
- 앱에서 사용할 4개의 SQLite 테이블 생성
- 각각 출하대상, 바코드정보, 계근내역, 생산계산 테이블

#### URL 초기화 (라인 56-76)
```java
Common.URL_VERSION = getString(R.string.URL_VERSION);
Common.URL_LOGIN = getString(R.string.URL_LOGIN);
Common.URL_SEARCH_SHIPMENT = getString(R.string.URL_SEARCH_SHIPMENT);
// ... 총 21개의 URL 설정
```
- `strings.xml`에 정의된 서버 API URL들을 `Common` 클래스의 정적 변수에 저장
- 로그인, 출하조회, 생산조회, 바코드정보, 계근내역 등 다양한 API URL 초기화

#### 프린터 설정 로드 (라인 78-88)
```java
SharedPreferences spfBluetooth = getSharedPreferences("spfBluetooth", MODE_PRIVATE);
Common.printer_setting = spfBluetooth.getBoolean("printer_setting", true);
Common.printer_address = spfBluetooth.getString("printer_address", "");

if (!Common.printer_setting)
    Common.print_bool = false;
else
    Common.print_bool = true;
```
- SharedPreferences에서 모바일 프린터 설정 정보를 읽어옴
- 프린터 사용 여부와 프린터 주소를 `Common` 클래스에 저장

#### UI 초기화 (라인 90-110)
```java
editID = (EditText) findViewById(R.id.editID);
editPWD = (EditText) findViewById(R.id.editPWD);

ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,store);
adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

Spinner spin = (Spinner) findViewById(R.id.storeSpinner);
spin.setAdapter(adapter);

spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
    TextView storetv = (TextView) findViewById(R.id.TextView01);

    public void onItemSelected(AdapterView parent, View v, int position, long id){
        storetv.setText(store[position]);
        Common.selectWarehouse = storetv.getText().toString();
        Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);
    }
    public void onNothingSelected(AdapterView parent){
        storetv.setText("");
    }
});
```
- EditText 컴포넌트 초기화
- 창고 선택 스피너 설정 및 이벤트 리스너 등록
- 선택된 창고는 `Common.selectWarehouse`에 저장되어 전역적으로 사용됨

### onResume (라인 140-143)
```java
@Override
protected void onResume() {
    super.onResume();
    Log.i(TAG, TAG + " onResume");
}
```
- Activity가 다시 포커스를 받을 때 호출
- 로그만 출력

### onStart (라인 146-149)
```java
@Override
protected void onStart() {
    super.onStart();
    Log.i(TAG, TAG + " onStart");
}
```
- Activity가 사용자에게 보이기 시작할 때 호출
- 로그만 출력

### onPause (라인 152-155)
```java
@Override
protected void onPause() {
    super.onPause();
    Log.i(TAG, TAG + " onPause");
}
```
- Activity가 백그라운드로 이동할 때 호출
- 로그만 출력

### onDestroy (라인 158-161)
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    Log.i(TAG, TAG + " onDestroy");
}
```
- Activity가 종료될 때 호출
- 로그만 출력

---

## 4. 주요 메서드 분석

### onClick 메서드 (라인 115-137)
버튼 클릭 이벤트를 처리하는 메서드입니다.

```java
public void onClick(View v) {
    switch (v.getId()) {
        case R.id.btnLogin:
            if (("").equals(editID.getText().toString())) {
                editID.setError("아이디를 입력하세요.");
                break;
            }
            if (("").equals(editPWD.getText().toString())) {
                editPWD.setError("비밀번호를 입력하세요.");
                break;
            }

            user_id = editID.getText().toString();
            user_pwd = editPWD.getText().toString();

            new ProgressDlgLogin(LoginActivity.this).execute();
            break;
        case R.id.btnClose:
            exitDialog();
            break;
    }
}
```

#### 처리 로직:
1. **로그인 버튼 (R.id.btnLogin)**:
   - ID 입력 검증 (라인 119-122)
   - 비밀번호 입력 검증 (라인 123-126)
   - 입력값 저장 (라인 128-129)
   - 비동기 로그인 작업 시작 (라인 131)

2. **종료 버튼 (R.id.btnClose)**:
   - 종료 확인 다이얼로그 표시 (라인 134)

### ProgressDlgLogin AsyncTask (라인 164-233)
서버와 비동기 통신하여 로그인을 처리하는 내부 클래스입니다.

#### 클래스 구조
```java
class ProgressDlgLogin extends AsyncTask<Integer, String, Integer> {
    private Context mContext;
    String receiveData = "";  // 조회결과를 Received

    public ProgressDlgLogin(Context context) {
        mContext = context;
    }
    // ...
}
```

#### onPreExecute 메서드 (라인 173-182)
```java
@Override
protected void onPreExecute() {
    pDialog = new ProgressDialog(mContext);
    pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    pDialog.setTitle("로그인중 입니다.");
    pDialog.setMessage("잠시만 기다려 주세요..");
    pDialog.setCancelable(false);
    pDialog.show();

    super.onPreExecute();
}
```
- 백그라운드 작업 시작 전 프로그레스 다이얼로그 표시
- 사용자가 취소할 수 없도록 설정 (`setCancelable(false)`)

#### doInBackground 메서드 (라인 185-208)
서버와의 실제 통신이 이루어지는 메서드입니다.

```java
@Override
protected Integer doInBackground(Integer... params) {
    try {
        Common.REG_ID = user_id.toString();
        String pwdBase = Base64.encode(user_pwd.toString().getBytes());
        MessageDigest sh = MessageDigest.getInstance("SHA-256");
        sh.update(pwdBase.getBytes());
        byte byteData[] = sh.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        if (Common.D) {
            Log.d(TAG, "SHA256 : " + sb.toString());
            Log.d(TAG, "::: ID = '" + Common.REG_ID.toString() + "', PWD = '" + user_pwd.toString() + "' AND '" + pwdBase + "' :::");
        }
        receiveData = HttpHelper.getInstance().loginData(Common.REG_ID, pwdBase, "login", Common.URL_LOGIN);
    } catch (Exception e) {
        if (Common.D) {
            e.printStackTrace();
            Log.e(TAG, "e : " + e.toString());
        }
    }
    return 0;
}
```

##### 보안 처리 과정:
1. **사용자 ID 저장** (라인 187): `Common.REG_ID`에 입력된 ID 저장
2. **Base64 인코딩** (라인 188): 비밀번호를 Base64로 인코딩
3. **SHA-256 해싱** (라인 189-195):
   - Base64 인코딩된 비밀번호를 SHA-256으로 해싱
   - 바이트 배열을 16진수 문자열로 변환
   - 이중 암호화로 보안 강화
4. **서버 통신** (라인 200):
   - `HttpHelper.getInstance().loginData()` 호출
   - ID와 Base64 인코딩된 비밀번호를 서버로 전송
   - 로그인 결과를 `receiveData`에 저장

#### onPostExecute 메서드 (라인 211-232)
서버 응답을 처리하는 메서드입니다.

```java
@Override
protected void onPostExecute(Integer result) {
    // 결과값의 앞, 뒤에 공백 제거
    receiveData = receiveData.replace("\r\n", "");
    receiveData = receiveData.replace("\n", "");
    if (Common.D) {
        Log.d(TAG, "receiveData : '" + receiveData + "'");
    }
    pDialog.dismiss();

    if (receiveData.toString().equals("fail")) {
        editID.setError("아이디와 비밀번호를 확인해주세요.");
        Toast.makeText(mContext, "로그인 실패", Toast.LENGTH_SHORT).show();
    } else {
        Toast.makeText(mContext, "로그인 성공", Toast.LENGTH_SHORT).show();
        Common.USER_TYPE = receiveData;
        // 테스트 시 제외
        //new ProgressDlgShipSearch(LoginActivity.this).execute();		// 로그인 성공 시 출하대상 조회
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
```

##### 처리 로직:
1. **데이터 정제** (라인 213-214): 개행문자 제거
2. **프로그레스 다이얼로그 종료** (라인 218)
3. **로그인 실패** (라인 220-222):
   - 서버 응답이 "fail"인 경우
   - ID 필드에 에러 메시지 표시
   - Toast 메시지 출력
4. **로그인 성공** (라인 224-231):
   - 서버에서 받은 사용자 타입을 `Common.USER_TYPE`에 저장
   - MainActivity로 화면 전환
   - 현재 Activity 종료

### exitDialog 메서드 (라인 245-263)
앱 종료 확인 다이얼로그를 표시하는 메서드입니다.

```java
public void exitDialog() {
    String alertTitle = getResources().getString(R.string.app_name);
    String buttonMessage = getString(R.string.exit_message);
    String buttonYes = getString(R.string.exit_yes);
    String buttonNo = getString(R.string.exit_no);

    new AlertDialog.Builder(LoginActivity.this, R.style.AppCompatDialogStyle)
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

#### 특징:
- 리소스 파일에서 문자열을 읽어와 다국어 지원 가능
- Highland 로고 아이콘 표시
- "예" 버튼 클릭 시 Activity 종료
- "아니오" 버튼 클릭 시 다이얼로그만 닫힘

---

## 5. UI 리스너

### 창고 선택 스피너 리스너 (라인 99-110)
```java
spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
    TextView storetv = (TextView) findViewById(R.id.TextView01);

    public void onItemSelected(AdapterView parent, View v, int position, long id){
        storetv.setText(store[position]);
        Common.selectWarehouse = storetv.getText().toString();
        Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);
    }
    public void onNothingSelected(AdapterView parent){
        storetv.setText("");
    }
});
```

#### 동작:
- **onItemSelected**: 사용자가 창고를 선택했을 때
  - 선택된 창고명을 TextView에 표시
  - `Common.selectWarehouse`에 저장하여 앱 전역에서 사용 가능
  - 로그 출력으로 선택 상태 확인
- **onNothingSelected**: 선택 해제 시 TextView를 빈 문자열로 초기화

### Back 버튼 리스너 (라인 236-242)
```java
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
        exitDialog();
    }

    return super.onKeyDown(keyCode, event);
}
```

#### 동작:
- 디바이스의 Back 버튼 클릭 시
- 앱 종료 확인 다이얼로그 표시
- 사용자의 실수로 인한 앱 종료 방지

---

## 6. 처리 흐름도

### 전체 로그인 프로세스
```
[앱 시작]
    ↓
[onCreate 실행]
    ├─ SQLite 테이블 생성 (출하대상, 바코드정보, 계근내역, 생산계산)
    ├─ 서버 API URL 초기화 (21개 URL)
    ├─ SharedPreferences에서 프린터 설정 로드
    ├─ UI 컴포넌트 초기화 (EditText, Spinner)
    └─ 창고 선택 스피너 설정
    ↓
[사용자 입력 대기]
    ├─ ID 입력
    ├─ 비밀번호 입력
    └─ 창고 선택
    ↓
[로그인 버튼 클릭]
    ↓
[onClick 메서드]
    ├─ ID 입력 검증
    ├─ 비밀번호 입력 검증
    └─ ProgressDlgLogin 실행
         ↓
    [onPreExecute]
         ├─ 프로그레스 다이얼로그 표시
         └─ "로그인중 입니다." 메시지
         ↓
    [doInBackground]
         ├─ 비밀번호 Base64 인코딩
         ├─ SHA-256 해싱
         ├─ HttpHelper를 통한 서버 통신
         └─ 응답 데이터 수신
         ↓
    [onPostExecute]
         ├─ 프로그레스 다이얼로그 종료
         ├─ 응답 데이터 정제 (개행문자 제거)
         └─ 결과 분기
              ├─ [실패: "fail"]
              │    ├─ ID 필드 에러 표시
              │    └─ "로그인 실패" Toast
              │
              └─ [성공: 사용자 타입 반환]
                   ├─ Common.USER_TYPE 저장
                   ├─ "로그인 성공" Toast
                   ├─ MainActivity로 화면 전환
                   └─ LoginActivity 종료
```

### 창고 선택 프로세스
```
[스피너 터치]
    ↓
[창고 목록 표시]
    ├─ 부산센터
    ├─ 이천1센터
    ├─ 삼일냉장
    ├─ SWC
    └─ 탑로지스
    ↓
[사용자 선택]
    ↓
[onItemSelected 호출]
    ├─ TextView에 선택된 창고명 표시
    ├─ Common.selectWarehouse에 저장
    └─ 로그 출력
```

### 앱 종료 프로세스
```
[종료 버튼 클릭 또는 Back 버튼]
    ↓
[exitDialog 메서드 호출]
    ↓
[AlertDialog 표시]
    ├─ 아이콘: Highland 로고
    ├─ 제목: 앱 이름
    └─ 메시지: "종료하시겠습니까?"
    ↓
[사용자 선택]
    ├─ [예] → finish() → 앱 종료
    └─ [아니오] → 다이얼로그 닫기 → 계속 사용
```

---

## 7. 코드 위치 참조

### 주요 섹션별 라인 번호

#### 패키지 및 Import 선언
- **라인 1-28**: 패키지 선언 및 필요한 라이브러리 import

#### 클래스 선언 및 필드
- **라인 30**: 클래스 선언 시작
- **라인 32**: TAG 상수 정의
- **라인 34**: ProgressDialog 선언
- **라인 37-38**: UI 컴포넌트 (EditText) 선언
- **라인 40-41**: 사용자 정보 변수
- **라인 43**: 창고 목록 배열

#### onCreate 메서드
- **라인 46-112**: onCreate 전체
  - **라인 48**: 레이아웃 설정
  - **라인 51-54**: SQLite 테이블 생성
  - **라인 56-76**: API URL 초기화
  - **라인 79-88**: 프린터 설정 로드
  - **라인 90-91**: EditText 초기화
  - **라인 93-110**: 스피너 설정 및 리스너 등록

#### onClick 메서드
- **라인 115-137**: onClick 전체
  - **라인 117-132**: 로그인 버튼 처리
  - **라인 119-122**: ID 입력 검증
  - **라인 123-126**: 비밀번호 입력 검증
  - **라인 128-129**: 입력값 저장
  - **라인 131**: AsyncTask 실행
  - **라인 133-135**: 종료 버튼 처리

#### 생명주기 메서드
- **라인 140-143**: onResume
- **라ین 146-149**: onStart
- **라인 152-155**: onPause
- **라인 158-161**: onDestroy

#### ProgressDlgLogin AsyncTask
- **라인 164-233**: ProgressDlgLogin 클래스 전체
  - **라인 165-166**: 필드 선언
  - **라인 168-170**: 생성자
  - **라인 173-182**: onPreExecute (프로그레스 다이얼로그 표시)
  - **라인 185-208**: doInBackground (서버 통신)
    - **라인 187**: ID 저장
    - **라인 188**: Base64 인코딩
    - **라인 189-195**: SHA-256 해싱
    - **라인 200**: HTTP 통신
  - **라인 211-232**: onPostExecute (결과 처리)
    - **라인 213-214**: 데이터 정제
    - **라인 220-222**: 로그인 실패 처리
    - **라인 224-231**: 로그인 성공 처리

#### Back 버튼 처리
- **라인 236-242**: onKeyDown (Back 버튼 이벤트)

#### 종료 다이얼로그
- **라인 245-263**: exitDialog 메서드

---

## 8. 의존성 분석

### 사용된 Android 컴포넌트
- **AppCompatActivity**: 하위 호환성을 지원하는 Activity
- **AsyncTask**: 비동기 작업 처리
- **ProgressDialog**: 로딩 표시
- **AlertDialog**: 확인 다이얼로그
- **EditText**: 텍스트 입력
- **Spinner**: 드롭다운 선택
- **SharedPreferences**: 로컬 데이터 저장
- **Toast**: 짧은 메시지 표시
- **Intent**: Activity 전환

### 외부 라이브러리
- **org.kobjects.base64.Base64**: Base64 인코딩
- **java.security.MessageDigest**: SHA-256 해싱

### 프로젝트 내부 의존성
- **Common**: 앱 전역 상수 및 변수 관리
- **HttpHelper**: HTTP 통신 헬퍼 클래스
- **DBHandler**: SQLite 데이터베이스 관리
- **MainActivity**: 로그인 성공 후 이동할 메인 화면

---

## 9. 보안 고려사항

### 비밀번호 암호화
1. **Base64 인코딩**: 비밀번호를 Base64로 먼저 인코딩
2. **SHA-256 해싱**: Base64 인코딩된 값을 다시 SHA-256으로 해싱
3. **서버 전송**: Base64 인코딩된 값만 서버로 전송 (SHA-256 값은 로그용)

### 주의사항
- 비밀번호가 평문으로 전송되지는 않지만, Base64는 복호화 가능
- HTTPS 사용을 권장
- SHA-256 해시값은 현재 로그용으로만 사용되며 서버 전송에는 사용되지 않음

### 로그 출력
- `Common.D` 플래그를 통해 디버그 로그 제어
- 프로덕션 환경에서는 `Common.D = false`로 설정하여 민감한 정보 로그 비활성화

---

## 10. 개선 제안사항

### 1. AsyncTask 대체
- **현재**: AsyncTask 사용 (Android 11부터 deprecated)
- **제안**: Kotlin Coroutines 또는 RxJava로 대체

### 2. 보안 강화
- **현재**: Base64 인코딩만 서버로 전송
- **제안**: SHA-256 해시값을 서버로 전송하거나, 서버 측 Salt 추가

### 3. UI/UX 개선
- **현재**: ProgressDialog 사용 (deprecated)
- **제안**: ProgressBar를 포함한 커스텀 레이아웃 사용

### 4. 입력 검증 강화
- **현재**: 빈 문자열 체크만 수행
- **제안**: ID 형식 검증, 비밀번호 최소 길이 체크 추가

### 5. 에러 처리 개선
- **현재**: Exception을 catch하고 로그만 출력
- **제안**: 네트워크 에러, 타임아웃 등 구체적인 에러 메시지 표시

### 6. 생명주기 메서드 활용
- **현재**: 로그만 출력
- **제안**: 필요시 리소스 정리, 상태 저장 등 실제 로직 추가

---

## 11. 테스트 시나리오

### 정상 로그인 시나리오
1. 앱 실행
2. 유효한 ID 입력
3. 유효한 비밀번호 입력
4. 창고 선택
5. 로그인 버튼 클릭
6. 프로그레스 다이얼로그 표시 확인
7. "로그인 성공" Toast 확인
8. MainActivity로 화면 전환 확인

### 로그인 실패 시나리오
1. 앱 실행
2. 잘못된 ID 또는 비밀번호 입력
3. 로그인 버튼 클릭
4. "로그인 실패" Toast 확인
5. ID 필드 에러 메시지 확인
6. LoginActivity에 계속 머물러 있는지 확인

### 입력 검증 시나리오
1. ID 빈 칸으로 로그인 시도 → "아이디를 입력하세요." 에러 확인
2. 비밀번호 빈 칸으로 로그인 시도 → "비밀번호를 입력하세요." 에러 확인

### 창고 선택 시나리오
1. 스피너 클릭
2. 각 창고 선택 시 TextView 업데이트 확인
3. 로그에서 `Common.selectWarehouse` 값 확인

### 앱 종료 시나리오
1. 종료 버튼 클릭 → 종료 다이얼로그 확인
2. Back 버튼 클릭 → 종료 다이얼로그 확인
3. 다이얼로그에서 "예" 선택 → 앱 종료 확인
4. 다이얼로그에서 "아니오" 선택 → 앱 계속 실행 확인

---

## 12. 요약

LoginActivity는 Highland E-MART PDA 애플리케이션의 진입점으로, 사용자 인증과 초기 설정을 담당합니다. 주요 특징은 다음과 같습니다:

- **데이터베이스 초기화**: 4개의 SQLite 테이블 생성
- **서버 통신**: 21개의 API URL 초기화
- **보안**: Base64 인코딩 및 SHA-256 해싱을 통한 비밀번호 보안
- **비동기 처리**: AsyncTask를 통한 논블로킹 로그인 처리
- **사용자 경험**: 프로그레스 다이얼로그, Toast 메시지, 입력 검증
- **창고 선택**: 5개 물류센터 중 작업 대상 선택 가능
- **프린터 설정**: SharedPreferences를 통한 모바일 프린터 설정 관리

전체적으로 잘 구조화된 로그인 화면이지만, AsyncTask의 deprecated 상태와 보안 강화 필요성 등 개선 여지가 있습니다.
