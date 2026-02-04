# Constants (Scanner)

## 개요

`Constants` 클래스는 Bluebird PDA 디바이스의 바코드 스캐너 기능을 제어하기 위한 Intent Action 상수 및 관련 상수들을 정의합니다. 이 클래스는 Bluebird BBAPI(Barcode API)와 통신하기 위한 표준 인터페이스를 제공합니다.

**파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\scanner\Constants.java`

**총 라인 수**: 47줄

## Intent Action 상수

### 바코드 제어 액션

| 상수 | 값 | 설명 |
|------|-----|------|
| `ACTION_BARCODE_OPEN` | `kr.co.bluebird.android.bbapi.action.BARCODE_OPEN` | 바코드 스캐너 열기 |
| `ACTION_BARCODE_CLOSE` | `kr.co.bluebird.android.bbapi.action.BARCODE_CLOSE` | 바코드 스캐너 닫기 |
| `ACTION_BARCODE_SET_TRIGGER` | `kr.co.bluebird.android.bbapi.action.BARCODE_SET_TRIGGER` | 바코드 트리거 설정 |
| `ACTION_BARCODE_SET_DEFAULT_PROFILE` | `kr.co.bluebird.android.bbapi.action.BARCODE_SET_DEFAULT_PROFILE` | 기본 프로파일 설정 |

### 바코드 콜백 액션

| 상수 | 값 | 설명 |
|------|-----|------|
| `ACTION_BARCODE_CALLBACK_REQUEST_SUCCESS` | `kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_REQUEST_SUCCESS` | 요청 성공 콜백 |
| `ACTION_BARCODE_CALLBACK_REQUEST_FAILED` | `kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_REQUEST_FAILED` | 요청 실패 콜백 |
| `ACTION_BARCODE_CALLBACK_DECODING_DATA` | `kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_DECODING_DATA` | 바코드 디코딩 데이터 콜백 |
| `ACTION_BARCODE_CALLBACK_DEFAULT_PROFILE_SETTING_COMPLETE` | `kr.co.bluebird.android.bbapi.action.BARCODE_DEFAULT_PROFILE_SETTING_COMPLETE` | 기본 프로파일 설정 완료 콜백 |
| `ACTION_BARCODE_CALLBACK_GET_STATUS` | `kr.co.bluebird.android.action.BARCODE_CALLBACK_GET_STATUS` | 바코드 상태 응답 콜백 |

### MDM 관련 액션

| 상수 | 값 | 설명 |
|------|-----|------|
| `ACTION_MDM_BARCODE_SET_SYMBOLOGY` | `kr.co.bluebird.android.bbapi.action.MDM_BARCODE_SET_SYMBOLOGY` | 심볼로지 설정 (MDM) |
| `ACTION_MDM_BARCODE_SET_MODE` | `kr.co.bluebird.android.bbapi.action.MDM_BARCODE_SET_MODE` | 모드 설정 (MDM) |
| `ACTION_MDM_BARCODE_SET_DEFAULT` | `kr.co.bluebird.android.bbapi.action.MDM_BARCODE_SET_DEFAULT` | 기본값 설정 (MDM) |

### 상태 관련 액션

| 상수 | 값 | 설명 |
|------|-----|------|
| `ACTION_BARCODE_SETTING_CHANGED` | `kr.co.bluebird.android.bbapi.action.BARCODE_SETTING_CHANGED` | 바코드 설정 변경 알림 |
| `ACTION_BARCODE_GET_STATUS` | `kr.co.bluebird.android.action.BARCODE_GET_STATUS` | 바코드 상태 요청 |

## 바코드 상태 상수

| 상수 | 값 | 설명 |
|------|-----|------|
| `BARCODE_CLOSE` | 0 | 바코드 스캐너 닫힌 상태 |
| `BARCODE_OPEN` | 1 | 바코드 스캐너 열린 상태 |
| `BARCODE_TRIGGER_ON` | 2 | 바코드 트리거 활성화 상태 |

## Intent Extra 키

### 데이터 전달 키

| 상수 | 설명 |
|------|------|
| `EXTRA_BARCODE_BOOT_COMPLETE` | 부팅 완료 여부 |
| `EXTRA_BARCODE_PROFILE_NAME` | 바코드 프로파일 이름 |
| `EXTRA_BARCODE_TRIGGER` | 바코드 트리거 상태 |
| `EXTRA_BARCODE_DECODING_DATA` | 디코딩된 바코드 데이터 |
| `EXTRA_HANDLE` | 핸들 값 |
| `EXTRA_INT_DATA2` | 정수 데이터 2 |
| `EXTRA_STR_DATA1` | 문자열 데이터 1 |
| `EXTRA_INT_DATA3` | 정수 데이터 3 |

## 에러 코드

| 상수 | 값 | 설명 |
|------|-----|------|
| `ERROR_FAILED` | -1 | 일반적인 실패 |
| `ERROR_NOT_SUPPORTED` | -2 | 지원되지 않는 기능 |
| `ERROR_NO_RESPONSE` | -4 | 응답 없음 |
| `ERROR_BATTERY_LOW` | -5 | 배터리 부족 |
| `ERROR_BARCODE_DECODING_TIMEOUT` | -6 | 바코드 디코딩 타임아웃 |
| `ERROR_BARCODE_ERROR_USE_TIMEOUT` | -7 | 바코드 사용 타임아웃 오류 |
| `ERROR_BARCODE_ERROR_ALREADY_OPENED` | -8 | 바코드 스캐너 이미 열림 오류 |

## 기타 상수

| 상수 | 값 | 설명 |
|------|-----|------|
| `MDM_MSR_MODE__SET_READING_TIMEOUT` | 0 | MSR 읽기 타임아웃 설정 |

## 사용 방법

### 1. 바코드 스캐너 열기

```java
Intent intent = new Intent(Constants.ACTION_BARCODE_OPEN);
sendBroadcast(intent);
```

### 2. 바코드 스캐너 닫기

```java
Intent intent = new Intent(Constants.ACTION_BARCODE_CLOSE);
sendBroadcast(intent);
```

### 3. 바코드 트리거 제어

```java
Intent intent = new Intent(Constants.ACTION_BARCODE_SET_TRIGGER);
intent.putExtra(Constants.EXTRA_BARCODE_TRIGGER, true); // 트리거 ON
sendBroadcast(intent);
```

### 4. 바코드 디코딩 데이터 수신

BroadcastReceiver를 등록하여 바코드 스캔 결과를 수신합니다:

```java
private BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Constants.ACTION_BARCODE_CALLBACK_DECODING_DATA.equals(action)) {
            String barcodeData = intent.getStringExtra(Constants.EXTRA_BARCODE_DECODING_DATA);
            // 바코드 데이터 처리
        } else if (Constants.ACTION_BARCODE_CALLBACK_REQUEST_SUCCESS.equals(action)) {
            // 요청 성공 처리
        } else if (Constants.ACTION_BARCODE_CALLBACK_REQUEST_FAILED.equals(action)) {
            // 요청 실패 처리
        }
    }
};

// BroadcastReceiver 등록
IntentFilter filter = new IntentFilter();
filter.addAction(Constants.ACTION_BARCODE_CALLBACK_DECODING_DATA);
filter.addAction(Constants.ACTION_BARCODE_CALLBACK_REQUEST_SUCCESS);
filter.addAction(Constants.ACTION_BARCODE_CALLBACK_REQUEST_FAILED);
registerReceiver(barcodeReceiver, filter);
```

### 5. 바코드 상태 확인

```java
// 상태 요청
Intent statusIntent = new Intent(Constants.ACTION_BARCODE_GET_STATUS);
sendBroadcast(statusIntent);

// 상태 응답 수신
private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.ACTION_BARCODE_CALLBACK_GET_STATUS.equals(intent.getAction())) {
            int status = intent.getIntExtra(Constants.EXTRA_INT_DATA2, -1);
            switch (status) {
                case Constants.BARCODE_CLOSE:
                    // 스캐너 닫힘
                    break;
                case Constants.BARCODE_OPEN:
                    // 스캐너 열림
                    break;
                case Constants.BARCODE_TRIGGER_ON:
                    // 트리거 활성화
                    break;
            }
        }
    }
};
```

### 6. 에러 처리

```java
int errorCode = intent.getIntExtra(Constants.EXTRA_INT_DATA2, 0);

switch (errorCode) {
    case Constants.ERROR_FAILED:
        // 일반 실패 처리
        break;
    case Constants.ERROR_NOT_SUPPORTED:
        // 지원되지 않는 기능
        break;
    case Constants.ERROR_BATTERY_LOW:
        // 배터리 부족 경고
        break;
    case Constants.ERROR_BARCODE_ERROR_ALREADY_OPENED:
        // 이미 열려있음
        break;
    // 기타 에러 코드 처리
}
```

## 주의사항

1. **Bluebird 전용**: 이 상수들은 Bluebird PDA 디바이스의 BBAPI를 위한 것입니다.
2. **BroadcastReceiver 등록**: 바코드 이벤트를 수신하려면 적절한 IntentFilter로 BroadcastReceiver를 등록해야 합니다.
3. **라이프사이클 관리**: Activity/Service의 라이프사이클에 맞춰 BroadcastReceiver를 등록/해제해야 합니다.
4. **권한**: 바코드 스캐너 사용을 위해 필요한 권한이 있을 수 있습니다.
5. **동시 접근**: `ERROR_BARCODE_ERROR_ALREADY_OPENED` 에러를 방지하기 위해 스캐너가 이미 열려있는지 확인해야 합니다.

## 통합 예제

```java
public class BarcodeManager {
    private Context context;
    private BarcodeDataListener listener;

    private BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Constants.ACTION_BARCODE_CALLBACK_DECODING_DATA.equals(action)) {
                String data = intent.getStringExtra(Constants.EXTRA_BARCODE_DECODING_DATA);
                if (listener != null) {
                    listener.onBarcodeScanned(data);
                }
            } else if (Constants.ACTION_BARCODE_CALLBACK_REQUEST_FAILED.equals(action)) {
                int error = intent.getIntExtra(Constants.EXTRA_INT_DATA2, -1);
                if (listener != null) {
                    listener.onError(error);
                }
            }
        }
    };

    public void open() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_DECODING_DATA);
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_REQUEST_SUCCESS);
        filter.addAction(Constants.ACTION_BARCODE_CALLBACK_REQUEST_FAILED);
        context.registerReceiver(barcodeReceiver, filter);

        Intent intent = new Intent(Constants.ACTION_BARCODE_OPEN);
        context.sendBroadcast(intent);
    }

    public void close() {
        Intent intent = new Intent(Constants.ACTION_BARCODE_CLOSE);
        context.sendBroadcast(intent);

        try {
            context.unregisterReceiver(barcodeReceiver);
        } catch (Exception e) {
            // Already unregistered
        }
    }

    public interface BarcodeDataListener {
        void onBarcodeScanned(String data);
        void onError(int errorCode);
    }
}
```

## 참고

- MDM (Mobile Device Management) 관련 액션은 디바이스 관리 시스템과의 통합을 위해 사용됩니다.
- Symbology는 바코드의 종류(QR, Code128, EAN13 등)를 의미합니다.
- Profile은 스캐너의 설정 프로파일을 의미하며, 다양한 스캔 모드를 저장할 수 있습니다.
