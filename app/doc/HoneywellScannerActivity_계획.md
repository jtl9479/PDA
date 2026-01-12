# HoneywellScannerActivity 신규 생성 계획

---

## 최우선 원칙

### ScannerActivity와 100% 동일한 기능을 지원해야 한다

- 바코드 수신 방식만 다르고, 나머지 기능은 **완전히 동일**해야 한다
- 하위 Activity(ShipmentActivity, ProductionActivity)가 **코드 변경 없이** 동작해야 한다

---

## 개요

- **작성일**: 2026-01-09
- **목적**: PM80 전용 ScannerActivity를 유지하고, Honeywell EDA51 전용 Activity를 새로 생성

---

## 현재 구조

```
ScannerActivity (PM80 전용)
    ├── ShipmentActivity
    └── ProductionActivity
```

---

## 변경 후 구조

```
ScannerActivity (PM80 전용) - 기존 유지, 수정 없음

HoneywellScannerActivity (Honeywell EDA51 전용) - 신규 생성
    ├── ShipmentActivity (상속 변경)
    └── ProductionActivity (상속 변경)
```

---

## 장단점

### 장점

| 항목 | 설명 |
|------|------|
| 기존 코드 보존 | PM80 코드를 건드리지 않음 |
| 롤백 용이 | 문제 발생 시 상속만 변경하면 복구 |
| 양쪽 지원 가능 | 필요시 PM80, Honeywell 모두 지원 가능 |

### 단점

| 항목 | 설명 |
|------|------|
| 코드 중복 | ActionBar, UI 코드가 중복됨 |
| 유지보수 | 두 파일을 관리해야 함 |

---

## 변경 대상 파일

| 파일 | 변경 내용 |
|------|----------|
| HoneywellScannerActivity.java | 신규 생성 |
| ShipmentActivity.java | 상속 변경: ScannerActivity → HoneywellScannerActivity |
| ProductionActivity.java | 상속 변경: ScannerActivity → HoneywellScannerActivity |
| AndroidManifest.xml | ScanResultReceiver 등록 제거 (선택) |

---

## HoneywellScannerActivity 설계

### 제거 항목 (PM80 전용)

| 항목 | 이유 |
|------|------|
| PM80 SDK import | Honeywell은 SDK 불필요 |
| mScanner, mDecodeResult | PM80 SDK 변수 |
| ScanResultReceiver | PM80 SDK 결과 수신용 |
| initScanner() | PM80 SDK 초기화 |

### 100% 동일하게 유지해야 하는 항목

| 항목 | ScannerActivity | HoneywellScannerActivity | 동일 |
|------|-----------------|-------------------------|------|
| ActionBar 설정 | 뒤로가기, 초기화 버튼, 인쇄 스위치 | 동일 | ✓ |
| btn_init | protected Button | 동일 | ✓ |
| swt_print | protected SwitchCompat | 동일 | ✓ |
| setMessage(String msg) | protected void | 동일 | ✓ |
| 인쇄 ON/OFF | Common.print_bool | 동일 | ✓ |
| onCheckedChanged() | 인쇄 스위치 콜백 | 동일 | ✓ |
| onResume() | 스위치 리스너 등록 | 동일 | ✓ |
| onDestroy() | Receiver 해제 | 동일 | ✓ |
| onOptionsItemSelected() | 뒤로가기 처리 | 동일 | ✓ |
| 레이아웃 | activity_scanner | 동일 | ✓ |

### 변경 항목 (Honeywell 전용)

| 항목 | PM80 | Honeywell |
|------|------|-----------|
| 바코드 수신 | ScanResultReceiver → m_brc | m_brc 직접 수신 |
| Action | ACTION_RECEIVE_PM80 | Honeywell Action (확인 필요) |
| Extra 키 | EXTRA_BARCODE | Honeywell Extra 키 (확인 필요) |

---

## 마이그레이션 체크리스트

### Step 1. Honeywell Intent 정보 확인 ✅ 완료

- [x] 바코드 스캔 시 발송되는 Intent Action
- [x] 바코드 데이터가 담긴 Intent Extra 키

**Honeywell EDA51 Intent 정보**:
```java
Action: "com.honeywell.scantointent.intent.action.BARCODE_DATA"
Extra: "com.honeywell.scantointent.intent.extra.DATA"
```

**참고**: EDA51 디바이스에서 `Scan To Intent` 설정이 활성화되어 있어야 함

---

### Step 2. HoneywellScannerActivity.java 생성 ✅ 완료

- [x] 파일 생성: `scanner/HoneywellScannerActivity.java`
- [x] ScannerActivity에서 공통 코드 복사
- [x] PM80 관련 코드 제거
- [x] Honeywell Intent 수신 코드 추가

---

### Step 3. ShipmentActivity 상속 변경 ✅ 완료

- [x] import 변경: `ScannerActivity` → `HoneywellScannerActivity`
- [x] `extends ScannerActivity` → `extends HoneywellScannerActivity`

---

### Step 4. ProductionActivity 상속 변경 ✅ 완료

- [x] import 변경: `ScannerActivity` → `HoneywellScannerActivity`
- [x] `extends ScannerActivity` → `extends HoneywellScannerActivity`

---

### Step 5. AndroidManifest.xml 수정 ✅ 완료

- [x] PM80 관련 permission 제거
- [x] ScanResultReceiver 등록 제거

---

## HoneywellScannerActivity 예상 코드 구조

```java
package com.rgbsolution.highland_emart.scanner;

public class HoneywellScannerActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "HoneywellScannerActivity";

    // Honeywell EDA51 상수
    private static final String ACTION_BARCODE_DATA =
        "com.honeywell.scantointent.intent.action.BARCODE_DATA";
    private static final String EXTRA_BARCODE_DATA =
        "com.honeywell.scantointent.intent.extra.DATA";

    // UI 컴포넌트
    protected Button btn_init;
    protected SwitchCompat swt_print;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // Honeywell 바코드 수신 등록
        IntentFilter filter = new IntentFilter(ACTION_BARCODE_DATA);
        registerReceiver(m_brc, filter);

        // ActionBar 설정 (기존과 동일)
        ...
    }

    // 바코드 콜백 (하위에서 오버라이드)
    protected void setMessage(String msg) {
    }

    // Honeywell 바코드 수신
    BroadcastReceiver m_brc = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_BARCODE_DATA.equals(action)) {
                String barcode = intent.getStringExtra(EXTRA_BARCODE_DATA);
                setMessage(barcode);
            }
        }
    };
}
```

---

## 바코드 수신 흐름 비교

### PM80 (ScannerActivity)

```
[PM80 스캐너]
    ↓ device.common.USERMSG
[ScanResultReceiver] - AndroidManifest 등록
    ↓ ACTION_RECEIVE_PM80
[m_brc]
    ↓
[setMessage()]
```

### Honeywell (HoneywellScannerActivity)

```
[Honeywell 스캐너]
    ↓ ACTION_BARCODE_DATA
[m_brc] - 직접 수신
    ↓
[setMessage()]
```

---

## Honeywell EDA51 스캐너 설정

### DataWedge 설정 경로

```
설정(Settings)
  → Honeywell Settings
    → Scan Settings
      → Internal Scanner
        → Default profile
          → Data Processing Settings
            → Scan To Intent (활성화)
```

### Intent 정보 (확인됨)

| 항목 | 값 |
|------|---|
| Action | `com.honeywell.scantointent.intent.action.BARCODE_DATA` |
| Extra | `com.honeywell.scantointent.intent.extra.DATA` |

---

## 진행 순서

| 순서 | 작업 | 비고 |
|-----|------|------|
| 1 | Honeywell Intent 정보 확인 | **필수 - 먼저 확인** |
| 2 | HoneywellScannerActivity.java 생성 | - |
| 3 | ShipmentActivity 상속 변경 | - |
| 4 | ProductionActivity 상속 변경 | - |
| 5 | 테스트 | EDA51 디바이스에서 확인 |
| 6 | AndroidManifest.xml 정리 (선택) | PM80 미사용 시 |

---

**최종 수정일**: 2026-01-09
