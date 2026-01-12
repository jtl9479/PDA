# 빅솔론 프린터 연동 테스트

---

## 개요

- **작성일**: 2026-01-12
- **최종 수정일**: 2026-01-12
- **목적**: BixolonSocketPrinter.java와 BixolonShipmentActivity.java의 SLCS 명령어가 실제 빅솔론 프린터에서 정상 출력되는지 테스트
- **대상 파일**:
  - `print/BixolonSocketPrinter.java`
  - `BixolonShipmentActivity.java`

---

## 테스트 환경

| 항목 | 내용 |
|------|------|
| PDA 디바이스 | Honeywell EDA51 |
| 프린터 | 빅솔론 라벨 프린터 |
| 통신 방식 | Bluetooth SPP (Serial Port Profile) |
| 명령어 | SLCS (Samsung Label Command Set) |

---

## 테스트 대상 파일

| 파일 | 역할 | 라인 수 |
|------|------|---------|
| BixolonSocketPrinter.java | 블루투스 소켓 통신, SLCS 명령어 전송 | 447줄 |
| BixolonShipmentActivity.java | 이마트 출하 계근 Activity, SLCS 라벨 인쇄 | 약 3200줄 |

---

## 변경 대상 파일

| 파일 | 변경 내용 |
|------|----------|
| BixolonShipmentActivity.java | Bug #1: 프린터 연결 다이얼로그 닫기 버그 수정 |
| BixolonShipmentActivity.java | Bug #2: 프린터 연결 확인 메시지 기존과 동일하게 수정 |
| BixolonShipmentActivity.java | Bug #3: 거짓 실패 메시지 방지 (mPreviousBixolonState 변수 추가) |
| BixolonShipmentActivity.java | 개선 #1: Bixolon 첫 사용 시 기존 Woosim MAC 주소 자동 초기화 |
| SettingActivity.java | 개선 #1 연계: 프린터 OFF 시 bixolon_initialized 플래그도 초기화 |

---

## 프린터 연결 방식 분석

### 기존 ShipmentActivity (Woosim 프린터) 연결 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. onStart() - 프린터 서비스 초기화                               │
├─────────────────────────────────────────────────────────────────┤
│  mPrintService = new BluetoothPrintService(this, mHandler)      │
│  mWoosim = new WoosimService(mHandler)                          │
│                                                                 │
│  if (Common.printer_address == "") {                            │
│      → DeviceListActivity 호출 (프린터 선택)                      │
│  } else {                                                       │
│      → ProgressDlgPrintConnect 실행 (자동 연결)                   │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. ProgressDlgPrintConnect.doInBackground()                     │
├─────────────────────────────────────────────────────────────────┤
│  cDialog 표시: "프린터 연결중..."                                 │
│  mPrintService.connect(device, true)  ← BluetoothPrintService   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. mHandler - MESSAGE_DEVICE_NAME (연결 성공)                    │
├─────────────────────────────────────────────────────────────────┤
│  case MESSAGE_DEVICE_NAME:                                      │
│      mConnectedDeviceName = msg.getData().getString(...)        │
│      cDialog.dismiss()  ← 다이얼로그 닫기                         │
│      sound_pool.play(sound_success, ...)  ← 성공음               │
└─────────────────────────────────────────────────────────────────┘
```

### BixolonShipmentActivity (Bixolon 프린터) 연결 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. onStart() - 프린터 서비스 초기화                               │
├─────────────────────────────────────────────────────────────────┤
│  mBixolonPrinter = new BixolonSocketPrinter(this, mBixolonHandler)│
│                                                                 │
│  if (Common.printer_address == "") {                            │
│      → DeviceListActivity 호출 (프린터 선택)                      │
│  } else {                                                       │
│      → ProgressDlgPrintConnect 실행 (자동 연결)                   │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. ProgressDlgPrintConnect.doInBackground()                     │
├─────────────────────────────────────────────────────────────────┤
│  cDialog 표시: "프린터 연결중..."                                 │
│  mBixolonPrinter.connect(device)  ← BixolonSocketPrinter        │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. mBixolonHandler - MESSAGE_STATE_CHANGE (연결 성공)            │
├─────────────────────────────────────────────────────────────────┤
│  case MESSAGE_STATE_CHANGE:                                     │
│      case STATE_CONNECTED:                                      │
│          cDialog.dismiss()  ← 다이얼로그 닫기 (수정 후)            │
│          Toast "Printer connected!"                             │
│          sound_pool.play(sound_success, ...)                    │
└─────────────────────────────────────────────────────────────────┘
```

### 기존 vs BixolonShipmentActivity 비교

| 항목 | 기존 (ShipmentActivity) | Bixolon (BixolonShipmentActivity) |
|------|------------------------|-----------------------------------|
| 프린터 서비스 | `BluetoothPrintService` | `BixolonSocketPrinter` |
| 명령어 서비스 | `WoosimService` | 없음 (SLCS 헬퍼 메서드 직접 사용) |
| 연결 메서드 | `mPrintService.connect(device, true)` | `mBixolonPrinter.connect(device)` |
| 성공 메시지 | `MESSAGE_DEVICE_NAME` | `MESSAGE_STATE_CHANGE → STATE_CONNECTED` |
| 다이얼로그 닫기 | `cDialog.dismiss()` ✅ | `cDialog.dismiss()` ✅ (버그 수정 완료) |

---

## 버그 수정 이력

### Bug #1: 프린터 연결 다이얼로그가 닫히지 않는 문제

**Part 1. 분석**
- 메서드: mBixolonHandler.handleMessage()
- 범위: 라인 891~911 (BixolonShipmentActivity.java)
- 용도: 프린터 연결 상태 변경 처리
- 주의할 점: 기존 ShipmentActivity의 mHandler와 동일한 동작 필요
- 호출 수: Handler 메시지 수신 시

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | STATE_CONNECTED | 라인 893-900 | N/A | 연결 성공 시 다이얼로그 닫기 누락 |
| 2 | STATE_NONE | 라인 906-910 | N/A | 연결 실패 시 다이얼로그 닫기 누락 |

**Part 2. 변환 계획**
- 변환 방식: 기존 ShipmentActivity mHandler 로직 참조
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 (ShipmentActivity) | 변환 후 (BixolonShipmentActivity) |
  |------------------------|-----------------------------------|
  | MESSAGE_DEVICE_NAME → cDialog.dismiss() | STATE_CONNECTED → cDialog.dismiss() |
  | (연결 실패 처리) | STATE_NONE → cDialog.dismiss() |
- 주의사항: cDialog null 체크 및 isShowing() 체크 필요

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [x] Part 7: 주석 보강
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용**:
- **무엇을**: mBixolonHandler의 STATE_CONNECTED, STATE_NONE 처리 로직
- **왜**: 프린터 연결 성공/실패 시 "프린터 연결중..." 다이얼로그가 닫히지 않는 버그
- **어떻게**: 기존 ShipmentActivity의 MESSAGE_DEVICE_NAME 처리와 동일하게 cDialog.dismiss() 추가

**수정 코드** (BixolonShipmentActivity.java 라인 891-911):
```java
case BixolonSocketPrinter.MESSAGE_STATE_CHANGE:
    switch (msg.arg1) {
        case BixolonSocketPrinter.STATE_CONNECTED:
            // 연결 성공 시 다이얼로그 닫기 (추가됨)
            if (cDialog != null && cDialog.isShowing()) {
                cDialog.dismiss();
            }
            Log.d(TAG, "Bixolon connected");
            Toast.makeText(...);
            break;
        case BixolonSocketPrinter.STATE_NONE:
            // 연결 실패/해제 시 다이얼로그 닫기 (추가됨)
            if (cDialog != null && cDialog.isShowing()) {
                cDialog.dismiss();
            }
            break;
    }
    break;
```

---

### Bug #2: 프린터 연결 확인 메시지가 기존과 다른 문제

**Part 1. 분석**
- 메서드: mBixolonHandler.handleMessage()
- 범위: 라인 887~932 (BixolonShipmentActivity.java)
- 용도: 프린터 연결 상태 변경 시 사용자 알림
- 주의할 점: 기존 ShipmentActivity와 동일한 메시지/사운드 유지 필요
- 호출 수: Handler 메시지 수신 시

| # | 항목 | 기존 (ShipmentActivity) | 변경 전 (BixolonShipmentActivity) | 문제점 |
|---|------|------------------------|----------------------------------|--------|
| 1 | 연결 성공 Toast | "Connected to [프린터명]" | "Printer connected!" | 프린터명 미표시 |
| 2 | 연결 성공음 | sound_success | sound_success | 동일 |
| 3 | 연결 실패 Toast | "접속이 원할하지 않습니다..." | 없음 | 누락 |
| 4 | 연결 실패음 | sound_fail | 없음 | 누락 |
| 5 | ActionBar 부제목 | 없음 | "Printer Connected" 등 | 추가 기능 (유지) |

**Part 2. 변환 계획**
- 변환 방식: 기존 ShipmentActivity mHandler 로직과 동일하게 변경
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 (ShipmentActivity) | 변환 후 (BixolonShipmentActivity) |
  |------------------------|-----------------------------------|
  | MESSAGE_DEVICE_NAME → mConnectedDeviceName 저장, Toast, 성공음 | MESSAGE_DEVICE_NAME → 동일하게 처리 |
  | MESSAGE_TOAST (STATE_NONE) → 실패 Toast, 실패음 | STATE_NONE → 실패 Toast, 실패음 |
- 주의사항: ActionBar 부제목은 기존에 없던 기능이므로 유지

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [x] Part 7: 주석 보강
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용**:
- **무엇을**: mBixolonHandler의 연결 성공/실패 알림 로직
- **왜**: 기존 ShipmentActivity와 동일한 사용자 경험 유지
- **어떻게**:
  - MESSAGE_DEVICE_NAME에서 프린터명 저장 + "Connected to [프린터명]" Toast + 성공음
  - STATE_NONE에서 "접속이 원할하지 않습니다..." Toast + 실패음 추가
  - ActionBar 부제목은 추가 기능으로 유지

**수정 코드** (BixolonShipmentActivity.java mBixolonHandler):
```java
case BixolonSocketPrinter.MESSAGE_STATE_CHANGE:
    switch (msg.arg1) {
        case BixolonSocketPrinter.STATE_CONNECTED:
            // 연결 성공 시 다이얼로그 닫기 (Toast와 성공음은 MESSAGE_DEVICE_NAME에서 처리)
            if (cDialog != null && cDialog.isShowing()) {
                cDialog.dismiss();
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("Printer Connected");
            }
            break;
        case BixolonSocketPrinter.STATE_NONE:
            // 연결 실패/해제 시 다이얼로그 닫기 + 실패 알림 (기존과 동일)
            if (cDialog != null && cDialog.isShowing()) {
                cDialog.dismiss();
                Toast.makeText(getApplicationContext(),
                    "접속이 원할하지 않습니다\n스캐너의 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                sound_pool.play(sound_fail, 1.0f, 1.0f, 0, 0, 1.0f);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("Not Connected");
            }
            break;
    }
    break;
case BixolonSocketPrinter.MESSAGE_DEVICE_NAME:
    // 연결된 프린터 디바이스명 저장 및 연결 성공 알림 (기존과 동일)
    mConnectedDeviceName = (String) msg.obj;
    Toast.makeText(getApplicationContext(),
        "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
    sound_pool.play(sound_success, 1.0f, 1.0f, 0, 0, 1.0f);
    break;
```

**수정 전후 비교**:
| 항목 | 수정 전 | 수정 후 | 기존 동일 |
|------|--------|--------|:--------:|
| 연결 성공 Toast | "Printer connected!" | "Connected to [프린터명]" | ✅ |
| 연결 성공음 | sound_success | sound_success | ✅ |
| 연결 실패 Toast | 없음 | "접속이 원할하지 않습니다..." | ✅ |
| 연결 실패음 | 없음 | sound_fail | ✅ |
| ActionBar 부제목 | 있음 | 있음 (유지) | 추가 기능 |

---

### Bug #3: 프린터 선택 직후 거짓 실패 메시지 표시 문제

**Part 1. 분석**
- 메서드: mBixolonHandler.handleMessage(), BixolonSocketPrinter.connect()
- 범위:
  - BixolonShipmentActivity.java 라인 887~932 (mBixolonHandler)
  - BixolonSocketPrinter.java 라인 93~108 (connect)
- 용도: 프린터 연결 시 상태 변경 처리
- 주의할 점: connect() 내부에서 disconnect() 호출 → STATE_NONE 발생
- 호출 수: Handler 메시지 수신 시

**문제 발생 흐름**:
```
┌─────────────────────────────────────────────────────────────────┐
│ 1. DeviceListActivity에서 프린터 선택                            │
│    → onActivityResult()에서 mBixolonPrinter.connect(device) 호출 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. BixolonSocketPrinter.connect() 내부                          │
├─────────────────────────────────────────────────────────────────┤
│  public void connect(BluetoothDevice device) {                  │
│      disconnect();  ← ★ 여기서 STATE_NONE 발생                  │
│      mConnectThread = new ConnectThread(device);                │
│      mConnectThread.start();                                    │
│      setState(STATE_CONNECTING);                                │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. disconnect() → setState(STATE_NONE)                          │
│    → mBixolonHandler에 MESSAGE_STATE_CHANGE (STATE_NONE) 전달   │
│    → Bug #2에서 추가한 실패 메시지 표시됨 (★ 거짓 실패)          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. 이후 STATE_CONNECTING → STATE_CONNECTED 또는 진짜 STATE_NONE │
│    (실제 연결 결과와 무관하게 이미 실패 메시지가 표시됨)          │
└─────────────────────────────────────────────────────────────────┘
```

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | connect() | BixolonSocketPrinter.java 라인 93 | N/A | disconnect() 먼저 호출 |
| 2 | disconnect() | BixolonSocketPrinter.java 라인 115-141 | N/A | setState(STATE_NONE) 호출 |
| 3 | STATE_NONE 핸들러 | BixolonShipmentActivity.java | N/A | cDialog 표시 중이면 실패 메시지 |

**Part 2. 변환 계획**
- 변환 방식: 이전 상태 추적 변수 추가, 조건부 실패 메시지 표시
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | STATE_NONE → 무조건 실패 메시지 | STATE_CONNECTING → STATE_NONE 일 때만 실패 메시지 |
- 주의사항:
  - 초기 STATE_NONE (connect 시작 시) → 실패 메시지 표시하지 않음
  - STATE_CONNECTING → STATE_NONE (실제 연결 실패) → 실패 메시지 표시

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [x] Part 7: 주석 보강
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용**:
- **무엇을**: mBixolonHandler의 STATE_NONE 처리 로직 + 이전 상태 추적 변수
- **왜**: connect() 시작 시 disconnect() 호출로 인한 거짓 실패 메시지 방지
- **어떻게**:
  - `mPreviousBixolonState` 변수 추가하여 이전 상태 추적
  - STATE_NONE 핸들러에서 이전 상태가 STATE_CONNECTING일 때만 실패 메시지 표시

**수정 코드** (BixolonShipmentActivity.java):

1. 변수 추가 (라인 249 부근):
```java
private BixolonSocketPrinter mBixolonPrinter = null;
/** Bixolon 프린터 이전 상태 (연결 실패 판단용) */
private int mPreviousBixolonState = BixolonSocketPrinter.STATE_NONE;
```

2. mBixolonHandler 수정:
```java
case BixolonSocketPrinter.MESSAGE_STATE_CHANGE:
    int newState = msg.arg1;
    Log.d(TAG, "Bixolon state: " + mPreviousBixolonState + " -> " + newState);

    switch (newState) {
        case BixolonSocketPrinter.STATE_CONNECTED:
            if (cDialog != null && cDialog.isShowing()) {
                cDialog.dismiss();
            }
            Log.d(TAG, "Bixolon connected");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("Printer Connected");
            }
            break;
        case BixolonSocketPrinter.STATE_CONNECTING:
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("Connecting...");
            }
            break;
        case BixolonSocketPrinter.STATE_NONE:
            // ★ STATE_CONNECTING → STATE_NONE 일 때만 실패 메시지 표시
            // (connect() 초기화 시 STATE_NONE은 무시)
            if (mPreviousBixolonState == BixolonSocketPrinter.STATE_CONNECTING) {
                if (cDialog != null && cDialog.isShowing()) {
                    cDialog.dismiss();
                }
                Toast.makeText(getApplicationContext(),
                    "접속이 원할하지 않습니다\n스캐너의 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                sound_pool.play(sound_fail, 1.0f, 1.0f, 0, 0, 1.0f);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("Not Connected");
            }
            break;
    }
    // 현재 상태를 이전 상태로 저장
    mPreviousBixolonState = newState;
    break;
```

**상태 전이 시나리오**:
| 시나리오 | 상태 전이 | 실패 메시지 | 비고 |
|----------|-----------|:-----------:|------|
| 연결 시작 | NONE → NONE (disconnect) | ❌ | NONE→NONE이므로 무시 |
| 연결 중 | NONE → CONNECTING | ❌ | 연결 시도 중 |
| 연결 성공 | CONNECTING → CONNECTED | ❌ | 성공 메시지만 표시 |
| 연결 실패 | CONNECTING → NONE | ✅ | 실제 실패이므로 표시 |
| 연결 해제 | CONNECTED → NONE | ❌ | 정상 해제이므로 무시 |

---

### 개선 #1: Bixolon 첫 사용 시 기존 Woosim MAC 주소 자동 초기화

**Part 1. 분석**
- 메서드: onStart()
- 범위: 라인 477~517 (BixolonShipmentActivity.java)
- 용도: BixolonShipmentActivity 최초 진입 시 기존 Woosim 프린터 주소 자동 초기화
- 주의할 점: 1회만 실행되어야 함 (플래그로 관리)
- 호출 수: SharedPreferences 읽기/쓰기

**문제 상황**:
```
┌─────────────────────────────────────────────────────────────────┐
│ 기존 상태                                                        │
├─────────────────────────────────────────────────────────────────┤
│  SharedPreferences (spfBluetooth):                              │
│    printer_address = "40:19:20:5B:90:10" (Woosim 프린터 MAC)    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ BixolonShipmentActivity 진입                                    │
├─────────────────────────────────────────────────────────────────┤
│  Common.printer_address = "40:19:20:5B:90:10" (Woosim 주소)     │
│  → Bixolon 프린터에 Woosim MAC으로 연결 시도                     │
│  → 연결 실패                                                     │
└─────────────────────────────────────────────────────────────────┘
```

**해결 방안**:
| 방식 | 장점 | 단점 |
|------|------|------|
| 매번 초기화 | 항상 프린터 선택 화면 표시 | 매번 선택 필요 (불편) |
| 기존 방식 (저장/재사용) | 자동 연결 | 프린터 변경 시 수동 초기화 필요 |
| **첫 사용 시 1회 초기화** | 자동 연결 + 기존 주소 정리 | 없음 (채택) |

**Part 2. 변환 계획**
- 변환 방식: SharedPreferences 플래그 추가 (bixolon_initialized)
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 조건 | 동작 |
  |------|------|
  | bixolon_initialized = false | printer_address 초기화, 플래그 true로 설정 |
  | bixolon_initialized = true | 기존 로직 유지 |
- 주의사항: Common.printer_address도 함께 초기화 필요

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [x] Part 7: 주석 보강
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용**:
- **무엇을**: onStart()에 Bixolon 첫 사용 감지 및 MAC 주소 초기화 로직 추가
- **왜**: 기존 Woosim 프린터 MAC 주소가 저장되어 있으면 Bixolon 연결 실패
- **어떻게**:
  - `bixolon_initialized` 플래그로 첫 사용 여부 확인
  - 첫 사용 시 printer_address 초기화 및 플래그 설정
  - 이후에는 정상적으로 Bixolon 주소 저장/재사용

**수정 코드** (BixolonShipmentActivity.java onStart()):
```java
@Override
protected void onStart() {
    super.onStart();
    Log.i(TAG, TAG + " onStart");

    // ============================================================
    // Bixolon 프린터 첫 사용 시 기존 Woosim MAC 주소 초기화
    // - BixolonShipmentActivity 최초 진입 시 1회만 실행
    // - 이후에는 Bixolon 프린터 주소 저장/재사용
    // ============================================================
    SharedPreferences spfBluetooth = getSharedPreferences("spfBluetooth", Activity.MODE_PRIVATE);
    boolean bixolonInitialized = spfBluetooth.getBoolean("bixolon_initialized", false);

    if (!bixolonInitialized) {
        // 첫 사용: 기존 프린터 주소 초기화 및 플래그 설정
        SharedPreferences.Editor editor = spfBluetooth.edit();
        editor.putString("printer_address", "");
        editor.putBoolean("bixolon_initialized", true);
        editor.apply();

        Common.printer_address = "";
        Log.i(TAG, "Bixolon 첫 사용: 기존 프린터 주소 초기화 완료");
    }

    // ... 기존 로직 계속 ...
}
```

**동작 흐름**:
```
┌─────────────────────────────────────────────────────────────────┐
│ BixolonShipmentActivity 첫 진입                                  │
├─────────────────────────────────────────────────────────────────┤
│  bixolon_initialized = false (초기값)                           │
│  → printer_address = "" (초기화)                                │
│  → bixolon_initialized = true (플래그 설정)                      │
│  → DeviceListActivity 표시 (프린터 선택)                         │
│  → Bixolon 프린터 선택 → MAC 주소 저장                           │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ BixolonShipmentActivity 재진입                                   │
├─────────────────────────────────────────────────────────────────┤
│  bixolon_initialized = true                                      │
│  → 초기화 로직 건너뜀                                             │
│  → printer_address = "XX:XX:XX:XX:XX:XX" (Bixolon MAC)          │
│  → 자동 연결                                                      │
└─────────────────────────────────────────────────────────────────┘
```

**연계 수정: SettingActivity.java onDestroy()**

프린터 OFF 시 `bixolon_initialized` 플래그도 함께 초기화하여, 다음 프린터 ON 시 새로운 프린터 선택이 가능하도록 함.

```java
} else {
    // OFF: 프린터 미사용, 주소 및 Bixolon 초기화 플래그도 초기화
    editor.putBoolean("printer_setting", false);
    editor.putString("printer_address", "");
    editor.putBoolean("bixolon_initialized", false);  // Bixolon 초기화 플래그 리셋
    editor.commit();
    Common.printer_address = "";
}
```

**프린터 재선택 흐름**:
```
SettingActivity: 프린터 OFF
    → printer_address = ""
    → bixolon_initialized = false
        ↓
SettingActivity: 프린터 ON
        ↓
BixolonShipmentActivity 진입
    → bixolon_initialized = false 감지
    → DeviceListActivity 표시 (프린터 재선택)
```

---

## Step 1. 프린터 블루투스 페어링 확인

**Part 1. 분석**
- 메서드: N/A (디바이스 설정)
- 범위: 라인 N/A
- 용도: PDA와 빅솔론 프린터 블루투스 페어링 상태 확인
- 주의할 점: 프린터 전원 ON, 블루투스 활성화 필요
- 호출 수: N/A

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | PDA 블루투스 | 설정 | N/A | ON 상태 확인 |
| 2 | 프린터 전원 | 프린터 | N/A | ON 상태 확인 |
| 3 | 페어링 상태 | 설정 → 블루투스 | N/A | 빅솔론 프린터 페어링 확인 |
| 4 | MAC 주소 | 페어링된 디바이스 | N/A | 프린터 MAC 주소 기록 |

**Part 2. 변환 계획**
- 변환 방식: N/A (테스트)
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | N/A | N/A |
- 주의사항: 페어링이 안 된 경우 수동으로 페어링 필요

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

## Step 2. BixolonSocketPrinter 연결 테스트

**Part 1. 분석**
- 메서드: connect(String address)
- 범위: 라인 93~108 (BixolonSocketPrinter.java)
- 용도: MAC 주소로 빅솔론 프린터에 블루투스 연결
- 주의할 점: 연결 실패 시 3가지 방법 순차 시도 (표준, Reflection, Insecure)
- 호출 수: BluetoothAdapter 1회, BluetoothSocket 1회

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | connect() | 라인 93-108 | N/A | MAC 주소로 연결 시작 |
| 2 | ConnectThread | 라인 178-272 | N/A | 블루투스 연결 스레드 |
| 3 | connectionEstablished() | 라인 147-165 | N/A | 연결 성공 처리 |
| 4 | connectionFailed() | 라인 170-173 | N/A | 연결 실패 처리 |

**Part 2. 변환 계획**
- 변환 방식: N/A (테스트)
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | N/A | N/A |
- 주의사항: STATE_CONNECTED (2) 상태 확인 필요

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

## Step 3. SLCS 명령어 전송 테스트

**Part 1. 분석**
- 메서드: sendCommand(String command)
- 범위: 라인 288~306 (BixolonSocketPrinter.java)
- 용도: SLCS 명령어 문자열을 프린터로 전송
- 주의할 점: UTF-8 인코딩으로 전송, 연결 상태 확인 필요
- 호출 수: OutputStream.write() 1회

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | sendCommand() | 라인 288-306 | N/A | SLCS 명령어 전송 |
| 2 | write() | 라인 277-283 | N/A | 바이트 배열 전송 |
| 3 | isConnected() | 라인 72-74 | N/A | 연결 상태 확인 |

**Part 2. 변환 계획**
- 변환 방식: N/A (테스트)
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | N/A | N/A |
- 주의사항: 명령어 전송 후 MESSAGE_PRINT_COMPLETE 메시지 확인

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

## Step 4. 테스트 라벨 출력 테스트

**Part 1. 분석**
- 메서드: printTestLabel()
- 범위: 라인 311~369 (BixolonSocketPrinter.java)
- 용도: preview.bmp 이미지를 1비트 모노크롬 변환하여 라벨 출력
- 주의할 점: assets/preview.bmp 파일 필요, EG 명령어 사용
- 호출 수: BitmapFactory.decodeStream() 1회, printBitmap() 1회

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | printTestLabel() | 라인 311-369 | N/A | 테스트 라벨 출력 |
| 2 | printBitmap() | 라인 389-444 | N/A | 비트맵 → SLCS EG 명령어 변환 |
| 3 | preview.bmp | assets/ | N/A | 테스트용 라벨 이미지 |

**Part 2. 변환 계획**
- 변환 방식: N/A (테스트)
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | N/A | N/A |
- 주의사항: EG 명령어 형식 확인, 라벨 크기 일치 확인

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

## Step 5. BixolonShipmentActivity SLCS 라벨 출력 테스트

**Part 1. 분석**
- 메서드: SLCS 헬퍼 메서드 (slcsInit, slcsText, slcsBarcode 등)
- 범위: 라인 3097~3200 (BixolonShipmentActivity.java)
- 용도: 이마트/홈플러스/롯데 라벨 SLCS 명령어 생성 및 출력
- 주의할 점: EUC-KR 인코딩 필수, 라벨 크기 600x480
- 호출 수: StringBuilder 다수, BixolonSocketPrinter.sendCommand() 1회

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | slcsInit() | 라인 3097-3108 | N/A | SLCS 초기화 명령어 |
| 2 | slcsLabelSize() | 라인 3113-3118 | N/A | 라벨 크기 설정 (SW, SL) |
| 3 | slcsText() | 라인 3123-3130 | N/A | 텍스트 출력 (V 명령어) |
| 4 | slcsBarcode() | 라인 3135-3143 | N/A | 바코드 출력 (BD 명령어) |
| 5 | slcsBox() | 라인 3148-3153 | N/A | 박스 그리기 (SB 명령어) |
| 6 | slcsLine() | 라인 3158-3164 | N/A | 선 그리기 (SL 명령어) |
| 7 | slcsPrint() | 라인 3169-3175 | N/A | 인쇄 실행 (P 명령어) |
| 8 | slcsFeedToMark() | 라인 3180-3185 | N/A | 마크 이동 (SM 명령어) |

**Part 2. 변환 계획**
- 변환 방식: N/A (테스트)
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | N/A | N/A |
- 주의사항: 5가지 라벨 유형 모두 테스트 필요 (이마트, 홈플러스, 롯데, 합계)

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

## Step 6. 라벨 유형별 출력 테스트

**Part 1. 분석**
- 메서드: 각 라벨 출력 블록
- 범위: BixolonShipmentActivity.java 전체
- 용도: searchType별 라벨 출력 정상 동작 확인
- 주의할 점: 각 라벨 유형별 레이아웃/항목 상이
- 호출 수: N/A

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | 이마트 기본 라벨 | searchType 0 | 600x480 | 점포명, BL번호, 상품명, 중량 등 |
| 2 | 이마트 확장 라벨 (E0~E3) | searchType 0 | 600x480 | 박스번호, 유통기한 추가 |
| 3 | 홈플러스 라벨 | searchType 2, 5 | 600x480 | 홈플러스 형식 |
| 4 | 롯데 라벨 | searchType 6 | 600x480 | 롯데 형식 |
| 5 | 합계 라벨 | 합계 버튼 | 600x480 | 총중량, 박스 수 |

**Part 2. 변환 계획**
- 변환 방식: N/A (테스트)
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | N/A | N/A |
- 주의사항: 각 라벨 유형별 항목 누락 없이 출력 확인

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

## 테스트 절차

### 사전 준비

1. Honeywell EDA51 PDA 준비
2. 빅솔론 라벨 프린터 준비
3. 라벨 용지 장착
4. APK 설치 (app-debug.apk)

### 테스트 순서

```
1. PDA 블루투스 ON
2. 프린터 전원 ON
3. 블루투스 페어링 확인
4. 앱 실행 → 로그인
5. 설정 → 프린터 ON
6. 프린터 선택 (DeviceListActivity)
7. 연결 상태 확인 (STATE_CONNECTED)
8. 계근입력시작 → 바코드 스캔
9. 라벨 출력 확인
```

---

## 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 연결 실패 | 페어링 안됨 | 수동 페어링 |
| 2 | 연결 실패 | MAC 주소 오류 | 주소 재확인 |
| 3 | 출력 안됨 | 연결 끊김 | 재연결 |
| 4 | 글자 깨짐 | 인코딩 오류 | EUC-KR 확인 |
| 5 | 레이아웃 오류 | 좌표 오류 | SLCS 좌표 수정 |
| 6 | 바코드 안 읽힘 | 바코드 크기 | BD 명령어 파라미터 조정 |

---

## 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| - | Bug #1: 프린터 연결 다이얼로그 닫기 버그 수정 | ✅ 완료 |
| - | Bug #2: 프린터 연결 확인 메시지 기존과 동일하게 수정 | ✅ 완료 |
| - | Bug #3: 프린터 선택 직후 거짓 실패 메시지 표시 문제 수정 | ✅ 완료 |
| - | 개선 #1: Bixolon 첫 사용 시 기존 Woosim MAC 주소 자동 초기화 | ✅ 완료 |
| 1 | 프린터 블루투스 페어링 확인 | ⏳ 대기 |
| 2 | BixolonSocketPrinter 연결 테스트 | ⏳ 대기 |
| 3 | SLCS 명령어 전송 테스트 | ⏳ 대기 |
| 4 | 테스트 라벨 출력 테스트 | ⏳ 대기 |
| 5 | BixolonShipmentActivity SLCS 라벨 출력 테스트 | ⏳ 대기 |
| 6 | 라벨 유형별 출력 테스트 | ⏳ 대기 |

---

**최종 수정일**: 2026-01-12
