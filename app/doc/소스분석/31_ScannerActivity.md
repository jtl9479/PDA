# ScannerActivity.java 분석 문서

---

## 개요

- **파일 위치**: `app/src/main/java/com/rgbsolution/highland_emart/scanner/ScannerActivity.java`
- **현재 코드 라인**: 311줄
- **작성일**: 2026-01-09
- **마지막 수정**: 2026-01-09 (리팩토링 완료)

---

## 클래스 설명

바코드 스캐너 기능을 제공하는 기본 Activity.

PDA 디바이스의 바코드 스캐너를 초기화하고 스캔 결과를 수신하는 기능을 제공한다.
계근 관련 Activity들(ShipmentActivity, ProductionActivity 등)이 이 클래스를 상속받아
바코드 스캔 기능을 사용한다.

### 상속 구조

```
AppCompatActivity
    └── ScannerActivity (implements CompoundButton.OnCheckedChangeListener)
            ├── ShipmentActivity
            └── ProductionActivity
```

---

## 현재 코드 구조

| 영역 | 라인 범위 | 설명 |
|-----|----------|------|
| import | 1~25 | 패키지, import 문 |
| 클래스 Javadoc | 27~48 | 클래스 설명 주석 |
| 클래스 선언 | 49 | ScannerActivity 선언 |
| 상수 | 51~61 | TAG, RECEIVE_PM80, EXTRA_BARCODE |
| PM80 SDK 변수 | 63~71 | mScanner, mDecodeResult |
| UI 컴포넌트 | 73~81 | btn_init, swt_print |
| ScanResultReceiver | 83~119 | PM80 SDK 결과 수신 |
| initScanner() | 121~160 | PM80 스캐너 초기화 |
| Activity 생명주기 | 162~264 | onCreate, onResume, onDestroy 등 |
| setMessage() | 266~280 | 바코드 수신 콜백 |
| m_brc | 282~310 | 내부 브로드캐스트 수신 |

---

## 상수

| 상수명 | 값 | 설명 | 라인 |
|--------|---|------|------|
| TAG | "ScannerActivity" | 로그 태그 | 51 |
| RECEIVE_PM80 | "ACTION_RECEIVE_PM80" | 내부 브로드캐스트 Action | 58 |
| EXTRA_BARCODE | "BARCODE" | 바코드 데이터 Intent Extra 키 | 61 |

---

## 멤버 변수

### PM80 SDK 변수

| 변수명 | 타입 | 접근제어자 | 설명 | 라인 |
|--------|------|-----------|------|------|
| mScanner | ScanManager | public static | PM80 스캐너 매니저 (싱글톤) | 68 |
| mDecodeResult | DecodeResult | private static | PM80 바코드 디코딩 결과 저장 객체 | 71 |

### UI 컴포넌트

| 변수명 | 타입 | 접근제어자 | 설명 | 라인 |
|--------|------|-----------|------|------|
| btn_init | Button | protected | 초기화 버튼 | 78 |
| swt_print | SwitchCompat | protected | 인쇄 ON/OFF 스위치 | 81 |

---

## 주요 메서드

### ScanResultReceiver (내부 클래스)

**위치**: line 96~119

PM80 스캐너 결과 수신 BroadcastReceiver.
AndroidManifest.xml에 등록되어 PM80 SDK로부터 스캔 결과를 수신한다.

**동작 과정**:
1. PM80 SDK에서 `device.common.USERMSG` Action으로 브로드캐스트 수신
2. `mScanner.aDecodeGetResult(mDecodeResult)`로 결과 획득
3. 바코드 값이 "READ_FAIL"이 아닌 경우
4. 내부 브로드캐스트(`RECEIVE_PM80`)로 재전송

---

### initScanner()

**위치**: line 131~160

PM80 스캐너를 초기화하는 메서드.

**초기화 순서**:
1. ScanManager 객체 생성 (싱글톤)
2. DecodeResult 객체 생성
3. `aDecodeAPIInit()` 호출
4. 500ms 대기 (SDK 초기화 대기)
5. `aDecodeSetDecodeEnable(1)` - 스캐너 활성화
6. `aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG)` - 결과 타입 설정

---

### onCreate()

**위치**: line 173~202

Activity 생성 시 호출.

**초기화 작업**:
1. 레이아웃 설정: `R.layout.activity_scanner`
2. PM80 스캐너 초기화: `initScanner()`
3. BroadcastReceiver 등록: `RECEIVE_PM80` Action
4. ActionBar 커스텀뷰 설정
5. UI 컴포넌트 초기화 (btn_init, swt_print)
6. 인쇄 스위치 초기값 설정: `swt_print.setChecked(Common.print_bool)`

---

### setMessage()

**위치**: line 279~280

바코드 수신 콜백 메서드 (하위 Activity에서 오버라이드).

```java
protected void setMessage(String msg) {
}
```

**특징**:
- 빈 구현체로 정의
- 하위 클래스에서 오버라이드하여 바코드 처리 로직 구현
- `m_brc` 리시버에서 호출됨

---

### m_brc (BroadcastReceiver)

**위치**: line 292~310

내부 브로드캐스트 수신 Receiver.
ScanResultReceiver에서 전달된 바코드를 수신하여 setMessage()를 호출한다.

**처리 과정**:
1. Intent에서 Action 확인
2. `RECEIVE_PM80` Action인 경우
3. `EXTRA_BARCODE` Extra 데이터 추출
4. `setMessage(receive_data)` 호출

---

### onDestroy()

**위치**: line 243~247

Activity 종료 시 호출.

**정리 작업**:
1. `mScanner = null` - 스캐너 참조 해제
2. `unregisterReceiver(m_brc)` - BroadcastReceiver 등록 해제

---

## 바코드 수신 흐름

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

## ActionBar 구성

| 항목 | 설명 |
|------|------|
| 뒤로가기 버튼 | `setDisplayHomeAsUpEnabled(true)` |
| 초기화 버튼 | btn_init (커스텀뷰) |
| 인쇄 스위치 | swt_print (커스텀뷰) |

---

## 사용 방법

### 상속 예시

```java
public class MyActivity extends ScannerActivity {
    @Override
    protected void setMessage(String msg) {
        // 스캔된 바코드(msg) 처리 로직 구현
        Log.d(TAG, "Scanned barcode: " + msg);
    }
}
```

---

## 관련 문서

- [ScannerActivity_Refactoring.md](ScannerActivity_Refactoring.md) - 리팩토링 계획
- [ScannerActivity_Migration_EDA51.md](ScannerActivity_Migration_EDA51.md) - EDA51 마이그레이션 계획

---

## 수정 이력

| 일자 | 작업 내용 | 비고 |
|-----|----------|------|
| 2026-01-06 | 미사용 코드 정리 | 주석 처리 코드, 미사용 변수 삭제 |
| 2026-01-09 | 주석 추가 | 클래스, 멤버 변수, 메서드 주석 |
| 2026-01-09 | 리팩토링 | Step 1~4 완료 |

---

**최종 수정일**: 2026-01-09
