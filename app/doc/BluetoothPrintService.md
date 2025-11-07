# BluetoothPrintService

## 개요

`BluetoothPrintService`는 블루투스 프린터와의 연결 및 데이터 전송을 관리하는 서비스 클래스입니다. SPP(Serial Port Profile) UUID를 사용하여 블루투스 디바이스와 통신하며, 비동기 스레드 기반으로 연결 및 데이터 송수신을 처리합니다.

**파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\print\BluetoothPrintService.java`

**총 라인 수**: 316줄

## 주요 상수

### 연결 상태 (Connection States)

```java
public static final int STATE_NONE = 0;       // 아무 작업도 수행하지 않는 상태
public static final int STATE_LISTEN = 1;     // 수신 연결 대기 중
public static final int STATE_CONNECTING = 2; // 연결 시도 중
public static final int STATE_CONNECTED = 3;  // 원격 디바이스에 연결됨
```

### SPP UUID

```java
private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
```

블루투스 시리얼 포트 프로파일을 위한 표준 UUID입니다.

## 주요 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `mAdapter` | `BluetoothAdapter` | 블루투스 어댑터 |
| `mHandler` | `Handler` | UI 스레드와 통신을 위한 핸들러 |
| `mState` | `int` | 현재 연결 상태 |
| `mConnectThread` | `ConnectThread` | 연결을 담당하는 스레드 |
| `mConnectedThread` | `ConnectedThread` | 데이터 송수신을 담당하는 스레드 |

## 주요 메서드

### 생성자

```java
public BluetoothPrintService(Context context, Handler handler)
```

**파라미터**:
- `context`: Android Context
- `handler`: UI 업데이트를 위한 Handler

**설명**: BluetoothAdapter를 초기화하고 초기 상태를 `STATE_NONE`으로 설정합니다.

### start()

```java
public synchronized void start()
```

**설명**: 서비스를 시작하고 모든 진행 중인 스레드를 취소합니다. 상태를 `STATE_LISTEN`으로 변경합니다.

### connect()

```java
public synchronized void connect(BluetoothDevice device, boolean secure)
```

**파라미터**:
- `device`: 연결할 블루투스 디바이스
- `secure`: 보안 연결 여부 (true: Secure, false: Insecure)

**설명**: 지정된 블루투스 디바이스에 대한 연결을 시작합니다. `ConnectThread`를 생성하여 연결을 수행하고 상태를 `STATE_CONNECTING`으로 변경합니다.

### connected()

```java
public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType)
```

**파라미터**:
- `socket`: 연결된 블루투스 소켓
- `device`: 연결된 블루투스 디바이스
- `socketType`: 소켓 타입 ("Secure" 또는 "Insecure")

**설명**: 연결이 성공한 후 호출됩니다. `ConnectedThread`를 시작하여 데이터 송수신을 처리하고, UI에 디바이스 이름을 전달합니다. 상태를 `STATE_CONNECTED`로 변경합니다.

### write()

```java
public void write(byte[] out)
```

**파라미터**:
- `out`: 전송할 바이트 데이터

**설명**: 연결된 디바이스로 데이터를 전송합니다. 현재 상태가 `STATE_CONNECTED`인 경우에만 동작합니다. 동기화된 블록에서 `ConnectedThread`의 참조를 얻은 후, 비동기로 데이터를 전송합니다.

### stop()

```java
public synchronized void stop()
```

**설명**: 모든 스레드를 중지하고 서비스를 종료합니다. 상태를 `STATE_NONE`으로 변경합니다.

### setState() / getState()

```java
private synchronized void setState(int state)
public synchronized int getState()
```

**설명**: 현재 연결 상태를 설정하거나 조회합니다.

## 내부 클래스

### ConnectThread

**역할**: 블루투스 디바이스에 대한 연결을 수행하는 스레드

**주요 메서드**:
- `run()`: 블루투스 소켓 연결을 시도합니다. 연결 성공 시 `connected()` 메서드를 호출하고, 실패 시 `connectionFailed()`를 호출합니다.
- `cancel()`: 소켓을 닫고 스레드를 종료합니다.

**연결 프로세스**:
1. 디바이스 검색 중지 (`mAdapter.cancelDiscovery()`)
2. 블루투스 소켓 연결 시도 (`mmSocket.connect()`) - 블로킹 호출
3. 성공 시 `connected()` 호출, 실패 시 `connectionFailed()` 호출

### ConnectedThread

**역할**: 연결된 블루투스 디바이스와 데이터를 송수신하는 스레드

**주요 필드**:
- `mmSocket`: 블루투스 소켓
- `mmInStream`: 입력 스트림
- `mmOutStream`: 출력 스트림

**주요 메서드**:
- `run()`: InputStream에서 데이터를 지속적으로 읽어 Handler를 통해 UI로 전달합니다.
- `write(byte[] buffer)`: OutputStream으로 데이터를 전송합니다.
- `cancel()`: 모든 스트림과 소켓을 닫습니다.

## 에러 처리

### connectionFailed()

```java
private void connectionFailed()
```

**설명**: 연결 실패 시 호출됩니다. UI에 "Unable to connect device" 토스트 메시지를 전송하고 서비스를 재시작합니다.

### connectionLost()

```java
private void connectionLost()
```

**설명**: 연결이 끊어졌을 때 호출됩니다. UI에 "Device connection was lost" 토스트 메시지를 전송하고 서비스를 재시작합니다.

## 사용 방법

### 1. 서비스 초기화

```java
Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case ShipmentActivity.MESSAGE_DEVICE_NAME:
                String deviceName = msg.getData().getString(ShipmentActivity.DEVICE_NAME);
                // 디바이스 이름 처리
                break;
            case ShipmentActivity.MESSAGE_TOAST:
                String toast = msg.getData().getString(ShipmentActivity.TOAST);
                // 토스트 메시지 표시
                break;
            case ShipmentActivity.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // 수신된 데이터 처리
                break;
        }
    }
};

BluetoothPrintService printService = new BluetoothPrintService(context, handler);
```

### 2. 블루투스 디바이스 연결

```java
BluetoothDevice device = ...; // 페어링된 디바이스 선택
printService.connect(device, true); // true: Secure 연결
```

### 3. 데이터 전송 (프린트)

```java
byte[] printData = ...; // 프린트할 데이터 (ESC/POS 명령 등)
printService.write(printData);
```

### 4. 서비스 종료

```java
printService.stop();
```

## 주의사항

1. **권한 필요**: `BLUETOOTH`, `BLUETOOTH_ADMIN` 권한이 필요합니다.
2. **UI 스레드**: Handler를 통해 UI 업데이트를 수행해야 합니다.
3. **동기화**: 모든 주요 메서드는 `synchronized`로 스레드 안전성을 보장합니다.
4. **상태 확인**: `write()` 호출 전에 `getState()`로 연결 상태를 확인할 수 있습니다.
5. **자동 재연결**: 연결 실패 또는 연결 끊김 시 자동으로 `start()`를 호출하여 대기 상태로 전환합니다.

## Handler 메시지 타입

- `MESSAGE_DEVICE_NAME`: 연결된 디바이스 이름 전달
- `MESSAGE_TOAST`: 토스트 메시지 전달 (에러 등)
- `MESSAGE_READ`: 디바이스로부터 수신한 데이터 전달

## 스레드 안전성

이 클래스는 멀티스레드 환경에서 안전하게 동작하도록 설계되었습니다:
- 모든 public 메서드는 `synchronized`로 보호됩니다.
- 스레드 간 참조는 동기화 블록 내에서만 공유됩니다.
- 각 스레드는 독립적인 소켓 인스턴스를 사용합니다.
