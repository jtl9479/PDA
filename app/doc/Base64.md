# Base64.java

## 개요

`Base64.java`는 Highland E-Mart PDA 애플리케이션에서 Base64 인코딩 및 디코딩을 수행하는 유틸리티 클래스입니다. 바이너리 데이터를 텍스트 형식으로 변환하거나 그 반대 작업을 수행할 때 사용됩니다.

**파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\common\Base64.java`

**라인 수**: 124줄

**버전**: 1.0

**작성일**: 2016.06.27

---

## Base64 인코딩이란?

Base64는 바이너리 데이터를 64개의 ASCII 문자로 표현하는 인코딩 방식입니다. 주로 이메일, URL, 또는 텍스트 기반 프로토콜에서 바이너리 데이터를 안전하게 전송하기 위해 사용됩니다.

**사용되는 64개 문자**: A-Z, a-z, 0-9, +, / (패딩: =)

---

## 주요 구성 요소

### 1. 인코딩 테이블

```java
static byte[] ENCODE_TABLE = {
    0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50,
    0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, // A-Z
    0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70,
    0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, // a-z
    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, // 0-9
    0x2b, 0x2f, 0x3d // +, /, =
};
```

Base64 인코딩에 사용되는 64개의 ASCII 문자와 패딩 문자(=)를 정의합니다.

---

### 2. 디코딩 테이블

```java
static byte[] DECODE_TABLE = {
    // 256개의 바이트 값에 대한 역매핑 테이블
    // ASCII 문자를 Base64 인덱스(0-63)로 변환
};
```

Base64 문자를 원래의 6비트 값으로 변환하기 위한 룩업 테이블입니다.

---

## 주요 메서드

### 1. 인코딩 메서드

#### `Base64Encode(byte[] source)`

바이트 배열을 Base64로 인코딩합니다.

**파라미터**:
- `source` - 인코딩할 원본 바이트 배열

**반환값**:
- Base64로 인코딩된 바이트 배열

**동작 원리**:
1. 입력 데이터 크기에 따라 출력 버퍼 크기 계산: `((size + 2) / 3) * 4`
2. 3바이트씩 읽어서 4개의 Base64 문자로 변환
3. 남은 바이트가 있으면 패딩(=) 추가
4. 인코딩된 결과 반환

**시간 복잡도**: O(n), n은 입력 바이트 배열의 크기

---

#### `encodeBlock(byte[] inputBlock, int inputOffset, byte[] outputBlock, int outputOffset)` (private)

3바이트 블록을 4개의 Base64 문자로 인코딩합니다.

**파라미터**:
- `inputBlock` - 입력 바이트 배열
- `inputOffset` - 입력 시작 위치
- `outputBlock` - 출력 바이트 배열
- `outputOffset` - 출력 시작 위치

**반환값**:
- 항상 4 (생성된 출력 바이트 수)

**인코딩 규칙**:
- 3바이트 입력 → 4바이트 출력
- 2바이트 입력 → 3바이트 출력 + 1바이트 패딩(=)
- 1바이트 입력 → 2바이트 출력 + 2바이트 패딩(==)

---

#### `getEncodeBlockSize(int size)` (private)

인코딩 후 필요한 버퍼 크기를 계산합니다.

**파라미터**:
- `size` - 원본 데이터 크기

**반환값**:
- 인코딩된 데이터를 저장할 버퍼 크기

**공식**: `((size + 2) / 3) * 4`

---

### 2. 디코딩 메서드

#### `Base64Decode(byte[] source)`

Base64로 인코딩된 데이터를 디코딩합니다.

**파라미터**:
- `source` - 디코딩할 Base64 인코딩된 바이트 배열

**반환값**:
- 디코딩된 원본 바이트 배열

**동작 원리**:
1. 입력 데이터 크기에 따라 출력 버퍼 크기 계산: `((size + 3) / 4) * 3`
2. 4개의 Base64 문자씩 읽어서 3바이트로 변환
3. 패딩(=)을 고려하여 실제 데이터 크기 조정
4. 디코딩된 결과 반환

**시간 복잡도**: O(n), n은 입력 바이트 배열의 크기

---

#### `decodeBlock(byte[] inputBlock, int inputOffset, byte[] outputBlock, int outputOffset)` (private)

4개의 Base64 문자를 3바이트로 디코딩합니다.

**파라미터**:
- `inputBlock` - 입력 바이트 배열
- `inputOffset` - 입력 시작 위치
- `outputBlock` - 출력 바이트 배열
- `outputOffset` - 출력 시작 위치

**반환값**:
- 디코딩된 실제 바이트 수 (1-3)

**디코딩 규칙**:
- 패딩이 없는 경우: 3바이트 출력
- 패딩 1개(=): 2바이트 출력
- 패딩 2개(==): 1바이트 출력

---

#### `getDecodeBlockSize(int size)` (private)

디코딩 후 필요한 버퍼 크기를 계산합니다.

**파라미터**:
- `size` - Base64 인코딩된 데이터 크기

**반환값**:
- 디코딩된 데이터를 저장할 버퍼 크기

**공식**: `((size + 3) / 4) * 3`

---

## 사용 예시

### 1. 문자열을 Base64로 인코딩

```java
// 원본 문자열
String originalText = "Hello, Highland E-Mart!";

// 바이트 배열로 변환
byte[] originalBytes = originalText.getBytes("UTF-8");

// Base64 인코딩
byte[] encodedBytes = Base64.Base64Encode(originalBytes);

// 인코딩된 결과를 문자열로 변환
String encodedText = new String(encodedBytes, "UTF-8");

System.out.println("Original: " + originalText);
System.out.println("Encoded: " + encodedText);
// 출력: SGVsbG8sIEhpZ2hsYW5kIEUtTWFydCE=
```

---

### 2. Base64 문자열을 디코딩

```java
// Base64 인코딩된 문자열
String encodedText = "SGVsbG8sIEhpZ2hsYW5kIEUtTWFydCE=";

// 바이트 배열로 변환
byte[] encodedBytes = encodedText.getBytes("UTF-8");

// Base64 디코딩
byte[] decodedBytes = Base64.Base64Decode(encodedBytes);

// 원본 문자열로 변환
String decodedText = new String(decodedBytes, "UTF-8");

System.out.println("Encoded: " + encodedText);
System.out.println("Decoded: " + decodedText);
// 출력: Hello, Highland E-Mart!
```

---

### 3. 이미지 데이터 인코딩

```java
// 이미지 파일 읽기
FileInputStream fis = new FileInputStream("image.png");
ByteArrayOutputStream baos = new ByteArrayOutputStream();

byte[] buffer = new byte[1024];
int bytesRead;
while ((bytesRead = fis.read(buffer)) != -1) {
    baos.write(buffer, 0, bytesRead);
}
fis.close();

byte[] imageBytes = baos.toByteArray();

// Base64 인코딩
byte[] encodedImage = Base64.Base64Encode(imageBytes);

// 서버로 전송하거나 저장
String imageString = new String(encodedImage, "UTF-8");
```

---

### 4. 인증 토큰 생성

```java
// 사용자 인증 정보
String username = "user001";
String password = "password123";
String credentials = username + ":" + password;

// Base64 인코딩
byte[] encodedCredentials = Base64.Base64Encode(credentials.getBytes("UTF-8"));
String authToken = "Basic " + new String(encodedCredentials, "UTF-8");

// HTTP 헤더에 추가
// Authorization: Basic dXNlcjAwMTpwYXNzd29yZDEyMw==
```

---

### 5. 데이터 무결성 검증

```java
// 원본 데이터
String originalData = "Important Data";
byte[] originalBytes = originalData.getBytes("UTF-8");

// 인코딩
byte[] encoded = Base64.Base64Encode(originalBytes);

// 전송 또는 저장...

// 디코딩
byte[] decoded = Base64.Base64Decode(encoded);
String recoveredData = new String(decoded, "UTF-8");

// 검증
if (originalData.equals(recoveredData)) {
    System.out.println("Data integrity verified!");
} else {
    System.out.println("Data corruption detected!");
}
```

---

### 6. JSON 데이터 인코딩 (HTTP 전송용)

```java
// JSON 데이터
String jsonData = "{\"barcode\":\"1234567890\",\"quantity\":10}";

// Base64 인코딩
byte[] encodedJson = Base64.Base64Encode(jsonData.getBytes("UTF-8"));
String encodedString = new String(encodedJson, "UTF-8");

// HTTP 요청으로 전송
HttpHelper httpHelper = HttpHelper.getInstance();
String result = httpHelper.sendData(
    encodedString,
    "base64",
    Common.URL_SEARCH_BARCODE_INFO
);

// 서버 응답 디코딩
byte[] responseBytes = result.getBytes("UTF-8");
byte[] decodedResponse = Base64.Base64Decode(responseBytes);
String responseJson = new String(decodedResponse, "UTF-8");
```

---

## 성능 특성

### 인코딩

- **크기 증가**: 원본 크기의 약 133% (4/3배)
- **시간 복잡도**: O(n)
- **공간 복잡도**: O(n)

### 디코딩

- **크기 감소**: Base64 크기의 약 75% (3/4배)
- **시간 복잡도**: O(n)
- **공간 복잡도**: O(n)

---

## 주의사항

1. **인코딩 오버헤드**: Base64 인코딩은 데이터 크기를 약 33% 증가시킵니다.

2. **보안**: Base64는 암호화가 아닌 인코딩입니다. 데이터를 숨기는 것이 아니라 전송 가능한 형태로 변환하는 것입니다.

3. **문자 인코딩**: 문자열을 바이트 배열로 변환할 때 인코딩(UTF-8, EUC-KR 등)을 명시적으로 지정해야 합니다.

4. **메모리 사용**: 큰 파일을 인코딩/디코딩할 때는 메모리 사용량에 주의해야 합니다.

5. **안드로이드 내장 클래스**: Android에서는 `android.util.Base64` 클래스를 사용하는 것이 더 간편할 수 있습니다.

---

## Android 내장 Base64와 비교

### 커스텀 Base64 클래스 (현재)

```java
byte[] encoded = Base64.Base64Encode(data);
byte[] decoded = Base64.Base64Decode(encoded);
```

**장점**:
- 외부 의존성 없음
- 동작 원리를 완전히 제어 가능

**단점**:
- 유지보수 필요
- 플래그 옵션 없음

---

### Android 내장 Base64

```java
byte[] encoded = android.util.Base64.encode(data, android.util.Base64.DEFAULT);
byte[] decoded = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT);
```

**장점**:
- 플랫폼 최적화
- 다양한 플래그 옵션 (NO_WRAP, NO_PADDING, URL_SAFE 등)
- Google에서 유지보수

**단점**:
- Android 플랫폼에 종속

---

## 코드 구조

```
Base64.java
├── 인코딩 테이블 (ENCODE_TABLE)
├── 디코딩 테이블 (DECODE_TABLE)
├── 인코딩 기능
│   ├── Base64Encode() (public)
│   ├── encodeBlock() (private)
│   └── getEncodeBlockSize() (private)
└── 디코딩 기능
    ├── Base64Decode() (public)
    ├── decodeBlock() (private)
    └── getDecodeBlockSize() (private)
```

---

## 실무 활용 시나리오

### 1. API 통신에서의 활용

```java
// 바코드 이미지를 서버로 전송
Bitmap barcodeBitmap = generateBarcode("1234567890");
ByteArrayOutputStream baos = new ByteArrayOutputStream();
barcodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
byte[] imageBytes = baos.toByteArray();

// Base64 인코딩
byte[] encodedImage = Base64.Base64Encode(imageBytes);
String imageString = new String(encodedImage, "UTF-8");

// JSON으로 감싸서 전송
String jsonData = "{\"barcode\":\"" + imageString + "\"}";
HttpHelper.getInstance().sendData(jsonData, "json", Common.URL_INSERT_BARCODE_INFO);
```

### 2. 로컬 캐시 저장

```java
// 민감한 데이터를 인코딩하여 SharedPreferences에 저장
String sensitiveData = "user_token_12345";
byte[] encoded = Base64.Base64Encode(sensitiveData.getBytes("UTF-8"));
String encodedString = new String(encoded, "UTF-8");

SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
prefs.edit().putString("token", encodedString).apply();

// 읽을 때 디코딩
String storedToken = prefs.getString("token", "");
byte[] decoded = Base64.Base64Decode(storedToken.getBytes("UTF-8"));
String token = new String(decoded, "UTF-8");
```

### 3. QR 코드 데이터 처리

```java
// QR 코드 스캔 결과를 Base64로 인코딩하여 서버로 전송
String qrContent = scanQRCode();
byte[] encoded = Base64.Base64Encode(qrContent.getBytes("UTF-8"));
String encodedQR = new String(encoded, "UTF-8");

HttpHelper.getInstance().sendData(encodedQR, "qr", Common.URL_SEARCH_SHIPMENT);
```
