# HttpHelper.java

## 개요

`HttpHelper.java`는 Highland E-Mart PDA 애플리케이션의 HTTP 통신을 담당하는 헬퍼 클래스입니다. Apache HttpClient를 사용하여 서버와의 POST 요청을 처리하며, EUC-KR 인코딩을 지원합니다. 싱글톤 패턴으로 구현되어 있습니다.

**파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\common\HttpHelper.java`

**라인 수**: 130줄

---

## 디자인 패턴

### 싱글톤 패턴 (Singleton Pattern)

```java
private static HttpHelper manager = new HttpHelper();

private HttpHelper() {}

public static HttpHelper getInstance() {
    return manager;
}
```

애플리케이션 전체에서 하나의 인스턴스만 사용하도록 보장합니다.

---

## 주요 메서드

### 1. 로그인 관련 메서드

#### `loginData(String name, String password, String type, String url)`

사용자 로그인을 위한 HTTP POST 요청을 수행합니다.

**파라미터**:
- `name` - 사용자 ID
- `password` - 사용자 비밀번호
- `type` - 로그인 타입 (현재 미사용)
- `url` - 로그인 API 엔드포인트 URL

**반환값**:
- 성공 시: 서버 응답 문자열
- 실패 시: 예외 메시지 문자열

**내부 동작**:
1. `makeHttpPost(name, password, type, url)` 호출하여 HttpPost 객체 생성
2. DefaultHttpClient로 요청 실행
3. BasicResponseHandler로 응답 처리
4. 결과 문자열 반환

---

#### `makeHttpPost(String name, String password, String type, String url)` (private)

로그인용 HttpPost 객체를 생성합니다.

**파라미터**:
- `name` - 사용자 ID
- `password` - 사용자 비밀번호
- `type` - 로그인 타입
- `url` - 요청 URL

**반환값**: HttpPost 객체

**내부 동작**:
1. HttpPost 객체 생성
2. "id"와 "pwd" 파라미터를 Vector에 추가
3. EUC-KR 인코딩으로 Entity 생성
4. HttpPost에 Entity 설정

---

### 2. 데이터 전송 메서드

#### `sendData(String data, String type, String url)`

일반 데이터를 서버로 전송합니다.

**파라미터**:
- `data` - 전송할 데이터 (JSON, XML 등)
- `type` - 데이터 타입 (현재 미사용)
- `url` - API 엔드포인트 URL

**반환값**:
- 성공 시: 서버 응답 문자열
- 실패 시: 예외 메시지 문자열

**내부 동작**:
1. `makeHttpPost(data, type, url)` 호출하여 HttpPost 객체 생성
2. DefaultHttpClient로 요청 실행
3. BasicResponseHandler로 응답 처리
4. 결과 문자열 반환

---

#### `makeHttpPost(String data, String type, String url)` (private)

데이터 전송용 HttpPost 객체를 생성합니다.

**파라미터**:
- `data` - 전송할 데이터
- `type` - 데이터 타입
- `url` - 요청 URL

**반환값**: HttpPost 객체

**내부 동작**:
1. HttpPost 객체 생성
2. "data" 파라미터를 Vector에 추가
3. EUC-KR 인코딩으로 Entity 생성
4. HttpPost에 Entity 설정

---

#### `sendDataDb(String data, String dbid, String type, String url)`

데이터베이스 ID를 포함한 데이터를 서버로 전송합니다.

**파라미터**:
- `data` - 전송할 데이터
- `dbid` - 데이터베이스 식별자
- `type` - 데이터 타입 (현재 미사용)
- `url` - API 엔드포인트 URL

**반환값**:
- 성공 시: 서버 응답 문자열
- 실패 시: 예외 메시지 문자열

**내부 동작**:
1. `makeHttpPostDb(data, dbid, type, url)` 호출하여 HttpPost 객체 생성
2. DefaultHttpClient로 요청 실행
3. BasicResponseHandler로 응답 처리
4. 결과 문자열 반환

---

#### `makeHttpPostDb(String data, String dbid, String type, String url)` (private)

데이터베이스 ID 포함 HttpPost 객체를 생성합니다.

**파라미터**:
- `data` - 전송할 데이터
- `dbid` - 데이터베이스 식별자
- `type` - 데이터 타입
- `url` - 요청 URL

**반환값**: HttpPost 객체

**내부 동작**:
1. HttpPost 객체 생성
2. "data"와 "dbid" 파라미터를 Vector에 추가
3. EUC-KR 인코딩으로 Entity 생성
4. HttpPost에 Entity 설정

---

### 3. 유틸리티 메서드

#### `makeEntity(Vector<BasicNameValuePair> nameValue)` (private)

파라미터 Vector를 EUC-KR 인코딩된 HttpEntity로 변환합니다.

**파라미터**:
- `nameValue` - 이름-값 쌍의 Vector

**반환값**: UrlEncodedFormEntity (EUC-KR 인코딩)

**인코딩**: EUC-KR (한글 지원)

---

## 사용 예시

### 1. 싱글톤 인스턴스 획득

```java
HttpHelper httpHelper = HttpHelper.getInstance();
```

---

### 2. 로그인 요청

```java
try {
    HttpHelper httpHelper = HttpHelper.getInstance();
    String result = httpHelper.loginData(
        "user001",              // 사용자 ID
        "password123",          // 비밀번호
        "manager",              // 타입
        Common.URL_LOGIN        // 로그인 URL
    );

    // 응답 처리
    if (result.contains("SUCCESS")) {
        // 로그인 성공
    } else {
        // 로그인 실패
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

---

### 3. 일반 데이터 전송

```java
try {
    HttpHelper httpHelper = HttpHelper.getInstance();

    // JSON 데이터 준비
    String jsonData = "{\"barcode\":\"1234567890\",\"quantity\":10}";

    // 서버로 전송
    String result = httpHelper.sendData(
        jsonData,                      // 전송할 데이터
        "json",                        // 데이터 타입
        Common.URL_SEARCH_BARCODE_INFO // API URL
    );

    // 응답 처리
    Log.d("HttpHelper", "Response: " + result);
} catch (Exception e) {
    e.printStackTrace();
}
```

---

### 4. 데이터베이스 ID 포함 전송

```java
try {
    HttpHelper httpHelper = HttpHelper.getInstance();

    // XML 데이터 준비
    String xmlData = "<shipment><id>12345</id><status>completed</status></shipment>";

    // DB ID와 함께 전송
    String result = httpHelper.sendDataDb(
        xmlData,                     // 전송할 데이터
        "DB_001",                    // 데이터베이스 ID
        "xml",                       // 데이터 타입
        Common.URL_UPDATE_SHIPMENT   // API URL
    );

    // 응답 처리
    if (result.contains("SUCCESS")) {
        // 업데이트 성공
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

---

### 5. AsyncTask와 함께 사용

```java
private class LoginTask extends AsyncTask<Void, Void, String> {
    private String userId;
    private String password;

    public LoginTask(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            HttpHelper httpHelper = HttpHelper.getInstance();
            return httpHelper.loginData(
                userId,
                password,
                "manager",
                Common.URL_LOGIN
            );
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        // UI 업데이트
        if (result.contains("SUCCESS")) {
            // 로그인 성공 처리
            Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show();
        } else {
            // 로그인 실패 처리
            Toast.makeText(context, "로그인 실패: " + result, Toast.LENGTH_SHORT).show();
        }
    }
}

// 실행
new LoginTask("user001", "password123").execute();
```

---

## 특징 및 주의사항

### 특징

1. **싱글톤 패턴**: 메모리 효율적인 인스턴스 관리
2. **EUC-KR 인코딩**: 한글 데이터 전송 지원
3. **예외 처리**: 모든 메서드에서 예외를 문자열로 반환
4. **Apache HttpClient**: 안정적인 HTTP 통신

### 주의사항

1. **Deprecated API**: Apache HttpClient는 Android 6.0(API 23)부터 deprecated되었습니다. 최신 앱에서는 OkHttp 또는 HttpURLConnection 사용을 권장합니다.

2. **메인 스레드 사용 금지**: NetworkOnMainThreadException을 피하기 위해 AsyncTask, Thread, 또는 Coroutine에서 사용해야 합니다.

3. **인코딩**: EUC-KR로 고정되어 있어 다른 인코딩이 필요한 경우 코드 수정이 필요합니다.

4. **예외 처리**: 예외가 발생해도 문자열로 반환되므로, 호출 측에서 응답 내용을 확인해야 합니다.

5. **타입 파라미터**: 현재 `type` 파라미터가 실제로 사용되지 않습니다.

---

## 코드 구조

```
HttpHelper.java
├── 싱글톤 구현
│   ├── manager (static instance)
│   ├── HttpHelper() (private constructor)
│   └── getInstance()
├── 로그인 기능
│   ├── loginData()
│   └── makeHttpPost() (for login)
├── 데이터 전송 기능
│   ├── sendData()
│   ├── makeHttpPost() (for data)
│   ├── sendDataDb()
│   └── makeHttpPostDb()
└── 유틸리티
    └── makeEntity() (EUC-KR encoding)
```

---

## 의존성

```gradle
// Apache HttpClient (Android에서는 더 이상 기본 포함되지 않음)
dependencies {
    implementation 'org.apache.httpcomponents:httpclient-android:4.3.5.1'
}
```

또는 `AndroidManifest.xml`에서:

```xml
<uses-library android:name="org.apache.http.legacy" android:required="false"/>
```
