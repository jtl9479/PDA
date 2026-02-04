# ScannerActivity.java PM80 → Honeywell EDA51 마이그레이션 계획

---

## 개요

- **파일 위치**: `app/src/main/java/com/rgbsolution/highland_emart/scanner/ScannerActivity.java`
- **현재 코드 라인**: 311줄
- **작성일**: 2026-01-09
- **마이그레이션 사유**: PDA 디바이스 변경 (Point Mobile PM80 → Honeywell EDA51)

---

## 변경 대상 파일

| 파일 | 변경 내용 |
|------|----------|
| ScannerActivity.java | PM80 SDK 제거, Honeywell Intent 방식으로 변경 |
| AndroidManifest.xml | ScanResultReceiver 등록 제거 |

---

## PM80 vs Honeywell EDA51 비교

| 항목 | PM80 (현재) | Honeywell EDA51 (변경 후) |
|------|-------------|---------------------------|
| 제조사 | Point Mobile | Honeywell |
| SDK | device.sdk.ScanManager | 없음 (Intent 방식) |
| 바코드 수신 | ScanResultReceiver → 내부 브로드캐스트 → m_brc | 직접 m_brc에서 수신 |
| Action | device.common.USERMSG | Honeywell 설정에 따름 (확인 필요) |
| 바코드 데이터 키 | mDecodeResult.toString() | Intent Extra (확인 필요) |

---

## 현재 바코드 수신 흐름 (PM80)

```
[PM80 스캐너 버튼]
    ↓
[PM80 SDK] - device.sdk.ScanManager가 바코드 디코딩
    ↓
[ScanResultReceiver] - AndroidManifest에 등록, Action: device.common.USERMSG
    ↓ mScanner.aDecodeGetResult(mDecodeResult)
    ↓ context.sendBroadcast(RECEIVE_PM80)
[m_brc] - 내부 브로드캐스트 수신, Action: ACTION_RECEIVE_PM80
    ↓
[setMessage()] - 하위 Activity에서 오버라이드
```

---

## 변경 후 바코드 수신 흐름 (Honeywell EDA51)

```
[EDA51 스캐너 버튼]
    ↓
[Honeywell 시스템] - Intent 브로드캐스트 자동 발송
    ↓
[m_brc] - 직접 Honeywell Action 수신
    ↓
[setMessage()] - 하위 Activity에서 오버라이드
```

---

## 마이그레이션 체크리스트

### Step 1. Honeywell EDA51 Intent 정보 확인 (필수)

EDA51 디바이스에서 확인 필요:

- [ ] 바코드 스캔 시 발송되는 Intent Action
- [ ] 바코드 데이터가 담긴 Intent Extra 키
- [ ] 바코드 타입이 담긴 Intent Extra 키 (있다면)

**일반적인 Honeywell Intent 정보 (확인 필요)**:
```java
// Action (예시 - 실제 값 확인 필요)
private static final String ACTION_BARCODE_DATA = "com.honeywell.sample.action.BARCODE_DATA";
// 또는
private static final String ACTION_BARCODE_DATA = "android.intent.ACTION_DECODE_DATA";

// 바코드 데이터 Extra 키 (예시 - 실제 값 확인 필요)
String barcode = intent.getStringExtra("data");
// 또는
String barcode = intent.getStringExtra("barcode_string");
```

---

### Step 2. PM80 SDK import 제거

- [ ] `import device.common.DecodeResult;` (line 23) - 삭제
- [ ] `import device.common.ScanConst;` (line 24) - 삭제
- [ ] `import device.sdk.ScanManager;` (line 25) - 삭제

---

### Step 3. PM80 SDK 변수 제거

- [ ] `public static ScanManager mScanner = null;` (line 68) - 삭제
- [ ] `private static DecodeResult mDecodeResult = null;` (line 71) - 삭제

---

### Step 4. ScanResultReceiver 클래스 삭제

- [ ] ScanResultReceiver 클래스 전체 삭제 (line 83~119)

PM80 SDK에서 바코드를 수신하여 내부 브로드캐스트로 전달하는 역할.
Honeywell은 직접 Intent를 발송하므로 불필요.

---

### Step 5. initScanner() 메서드 삭제

- [ ] initScanner() 메서드 전체 삭제 (line 121~160)

PM80 SDK 초기화 메서드.
Honeywell은 SDK 초기화 불필요.

---

### Step 6. onCreate() 수정

- [ ] `initScanner();` 호출 삭제
- [ ] IntentFilter를 Honeywell Action으로 변경

**현재 코드**:
```java
initScanner();  // 삭제
IntentFilter filter = new IntentFilter(RECEIVE_PM80);  // 수정
```

**변경 후**:
```java
IntentFilter filter = new IntentFilter(ACTION_BARCODE_DATA);
```

---

### Step 7. onDestroy() 수정

- [ ] `mScanner = null;` 삭제

**현재 코드**:
```java
mScanner = null;  // 삭제
unregisterReceiver(m_brc);
```

**변경 후**:
```java
unregisterReceiver(m_brc);
```

---

### Step 8. m_brc BroadcastReceiver 수정

- [ ] Action 비교 대상 변경
- [ ] 바코드 데이터 Extra 키 변경

**현재 코드**:
```java
if (RECEIVE_PM80.equals(action)) {
    String receive_data = intent.getStringExtra(EXTRA_BARCODE);
    setMessage(receive_data);
}
```

**변경 후**:
```java
if (ACTION_BARCODE_DATA.equals(action)) {
    String receive_data = intent.getStringExtra(EXTRA_BARCODE_DATA);
    setMessage(receive_data);
}
```

---

### Step 9. 상수 변경

- [ ] `RECEIVE_PM80` 상수 삭제
- [ ] `EXTRA_BARCODE` 상수를 `EXTRA_BARCODE_DATA`로 변경
- [ ] `ACTION_BARCODE_DATA` 상수 추가

**현재 상수**:
```java
private static final String RECEIVE_PM80 = "ACTION_RECEIVE_PM80";
private static final String EXTRA_BARCODE = "BARCODE";
```

**변경 후**:
```java
/** Honeywell EDA51 바코드 스캔 Action */
private static final String ACTION_BARCODE_DATA = "com.honeywell.sample.action.BARCODE_DATA";

/** Honeywell EDA51 바코드 데이터 Extra 키 */
private static final String EXTRA_BARCODE_DATA = "data";
```

---

### Step 10. AndroidManifest.xml 수정

- [ ] ScanResultReceiver 등록 삭제 (line 60~68)

**현재 코드 (삭제)**:
```xml
<receiver
    android:name="com.rgbsolution.highland_emart.scanner.ScannerActivity$ScanResultReceiver"
    android:enabled="true"
    android:permission="com.highland.scanner.permission.SCANNER_RESULT_RECEIVER"
    android:priority="0" >
    <intent-filter>
        <action android:name="device.common.USERMSG" />
    </intent-filter>
</receiver>
```

---

## 마이그레이션 우선순위

| 순위 | Step | 항목 | 비고 |
|-----|------|------|------|
| 0 | Step 1 | Honeywell Intent 정보 확인 | **필수 - 먼저 확인** |
| 1 | Step 2 | PM80 SDK import 제거 | - |
| 2 | Step 3 | PM80 SDK 변수 제거 | - |
| 3 | Step 4 | ScanResultReceiver 삭제 | - |
| 4 | Step 5 | initScanner() 삭제 | - |
| 5 | Step 6 | onCreate() 수정 | - |
| 6 | Step 7 | onDestroy() 수정 | - |
| 7 | Step 8 | m_brc 수정 | - |
| 8 | Step 9 | 상수 변경 | - |
| 9 | Step 10 | AndroidManifest.xml 수정 | - |

---

## 테스트 체크리스트

마이그레이션 후 반드시 확인해야 할 항목:

### 바코드 스캔 테스트
- [ ] EDA51 디바이스에서 바코드 스캔 후 setMessage() 호출 확인
- [ ] ShipmentActivity - 바코드 스캔 정상 동작
- [ ] ProductionActivity - 바코드 스캔 정상 동작
- [ ] 바코드 데이터 정확성 확인 (기존과 동일한 값)

### Activity 생명주기 테스트
- [ ] Activity 진입 시 BroadcastReceiver 정상 등록
- [ ] Activity 종료 시 BroadcastReceiver 정상 해제
- [ ] 화면 회전 시 정상 동작

### ActionBar 테스트
- [ ] 뒤로가기 버튼 동작
- [ ] 인쇄 스위치 ON/OFF 동작

---

## 확인 필요 사항

### Honeywell EDA51 스캐너 설정

1. **DataWedge 설정 확인**
   - Intent 출력 활성화 여부
   - Intent Action 설정값
   - Intent Extra 키 설정값

2. **기본 Intent 정보 (일반적인 Honeywell 설정)**
   ```
   Action: com.honeywell.sample.action.BARCODE_DATA
   Extra: data (바코드 문자열)
   Extra: dataBytes (바코드 바이트 배열)
   Extra: labelType (바코드 타입)
   ```

3. **대안 Intent 정보**
   ```
   Action: android.intent.ACTION_DECODE_DATA
   Extra: barcode_string
   ```

---

## 관련 문서

- [ScannerActivity.md](ScannerActivity.md) - ScannerActivity 분석 문서
- [ScannerActivity_Refactoring.md](ScannerActivity_Refactoring.md) - ScannerActivity 리팩토링 계획
- [ShipmentActivity_Refactoring.md](ShipmentActivity_Refactoring.md) - ShipmentActivity 리팩토링 계획

---

## 완료 기록

| 단계 | 작업 내용 | 완료 일자 | 비고 |
|-----|----------|----------|------|
| - | 마이그레이션 계획 문서 작성 | 2026-01-09 | - |
| Step 1 | Honeywell Intent 정보 확인 | - | **대기 중** |
| Step 2 | PM80 SDK import 제거 | - | - |
| Step 3 | PM80 SDK 변수 제거 | - | - |
| Step 4 | ScanResultReceiver 삭제 | - | - |
| Step 5 | initScanner() 삭제 | - | - |
| Step 6 | onCreate() 수정 | - | - |
| Step 7 | onDestroy() 수정 | - | - |
| Step 8 | m_brc 수정 | - | - |
| Step 9 | 상수 변경 | - | - |
| Step 10 | AndroidManifest.xml 수정 | - | - |

---

**최종 수정일**: 2026-01-09
