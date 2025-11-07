# ScannerActivity

## 개요
PM80 스캐너 연동을 위한 기본 Activity 클래스입니다. 바코드 스캔 결과를 BroadcastReceiver를 통해 수신하고 처리하는 기능을 제공합니다.

**파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\scanner\ScannerActivity.java`
**전체 라인 수**: 207줄

## 클래스 구조

### 상속 및 구현
```java
public class ScannerActivity extends AppCompatActivity
    implements CompoundButton.OnCheckedChangeListener
```

### 주요 필드

#### Static 필드
- `TAG` (String): 로그 태그 - "ScannerActivity"
- `RECEIVE_PM80` (String): 브로드캐스트 액션명 - "ACTION_RECEIVE_PM80"
- `mScanner` (ScanManager): PM80 스캐너 관리 객체 (static)
- `mDecodeResult` (DecodeResult): 스캔 결과 저장 객체 (static)

#### Instance 필드
- `btn_init` (Button): 초기화 버튼
- `swt_print` (SwitchCompat): 인쇄 ON/OFF 스위치
- `m_brc` (BroadcastReceiver): 스캔 결과 수신용 리시버

## 주요 메서드

### 1. ScanResultReceiver (내부 클래스)
**위치**: 40-63줄

PM80 스캐너의 스캔 결과를 최초로 받아오는 BroadcastReceiver입니다.

```java
public static class ScanResultReceiver extends BroadcastReceiver
```

**동작 과정**:
1. 스캔 이벤트 수신
2. `mScanner.aDecodeGetResult(mDecodeResult)`로 결과 획득
3. 바코드 값이 "READ_FAIL"이 아닌 경우
4. Intent에 바코드 값을 담아 `RECEIVE_PM80` 액션으로 브로드캐스트 전송

**핵심 코드** (48-60줄):
```java
mScanner.aDecodeGetResult(mDecodeResult);
String barcode = mDecodeResult.toString();
if (!barcode.equals("READ_FAIL")) {
    Intent i = new Intent();
    i.putExtra("BARCODE", barcode);
    i.setAction(RECEIVE_PM80);
    context.sendBroadcast(i);
}
```

### 2. initScanner()
**위치**: 66-101줄

PDA 스캐너를 초기화하는 메서드입니다.

**초기화 순서**:
1. `ScanManager` 객체 생성 (68-73줄)
2. `DecodeResult` 객체 생성 (74-79줄)
3. API 초기화: `aDecodeAPIInit()` (81줄)
4. 500ms 대기 (82-85줄)
5. 디코드 활성화: `aDecodeSetDecodeEnable(1)` (86줄)
6. 결과 타입 설정: `aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG)` (87줄)

### 3. onCreate()
**위치**: 104-134줄

Activity 생성 시 초기화 작업을 수행합니다.

**초기화 작업**:
1. 레이아웃 설정: `R.layout.activity_scanner` (106줄)
2. 스캐너 초기화 호출 (111줄)
3. BroadcastReceiver 등록 (112-113줄)
   ```java
   IntentFilter filter = new IntentFilter(RECEIVE_PM80);
   this.registerReceiver(m_brc, filter);
   ```
4. ActionBar 커스텀뷰 설정 (115-124줄)
5. 버튼 및 스위치 초기화 (126-132줄)

### 4. setMessage() - 추상 메서드
**위치**: 186-187줄

```java
protected void setMessage(String msg) {
}
```

**특징**:
- 빈 구현체로 정의되어 있음
- 하위 클래스에서 오버라이드하여 스캔된 바코드를 처리
- `m_brc` 리시버에서 호출됨 (203줄)

**사용 패턴**:
```java
String receive_data = intent.getStringExtra("BARCODE");
setMessage(receive_data);
```

### 5. m_brc (BroadcastReceiver)
**위치**: 190-206줄

`ScanResultReceiver`에서 전달한 바코드 값을 최종적으로 수신하는 리시버입니다.

**처리 과정**:
1. Intent에서 액션 확인 (196줄)
2. `RECEIVE_PM80` 액션인 경우 (198줄)
3. "BARCODE" Extra 데이터 추출 (202줄)
4. `setMessage(receive_data)` 호출하여 하위 클래스에 전달 (203줄)

### 6. onCheckedChanged()
**위치**: 142-156줄

인쇄 ON/OFF 스위치 상태 변경 시 호출됩니다.

**동작**:
- 스위치 OFF: `Common.print_bool = false` (149줄)
- 스위치 ON: `Common.print_bool = true` (152줄)
- Toast 메시지로 상태 표시

### 7. onDestroy()
**위치**: 165-169줄

Activity 종료 시 리소스 정리를 수행합니다.

**정리 작업**:
1. 스캐너 객체 null 처리 (167줄)
2. BroadcastReceiver 등록 해제 (168줄)

## BroadcastReceiver 처리 흐름

```
[PM80 스캐너]
    ↓
[ScanResultReceiver] (static 내부 클래스)
    ↓ (스캔 결과 획득)
    ↓ (RECEIVE_PM80 액션으로 브로드캐스트)
    ↓
[m_brc] (인스턴스 리시버)
    ↓
[setMessage()] (추상 메서드)
    ↓
[하위 클래스에서 구현]
```

## 설정 및 상태 관리

### SharedPreferences
- `spfBluetooth` (143줄): 블루투스 설정 저장용

### 전역 상태
- `Common.print_bool`: 인쇄 ON/OFF 상태
- `Common.D`: 디버그 모드 플래그

## 사용 방법

### 상속 예시
```java
public class MyActivity extends ScannerActivity {
    @Override
    protected void setMessage(String msg) {
        // 스캔된 바코드(msg) 처리 로직 구현
        Log.d(TAG, "Scanned barcode: " + msg);
        // 바코드 값을 이용한 검색, 조회 등 수행
    }
}
```

### 주요 특징
1. **Static Scanner 객체**: 앱 전체에서 하나의 스캐너 인스턴스 공유
2. **이중 BroadcastReceiver 구조**:
   - `ScanResultReceiver`: 스캐너 SDK로부터 직접 수신
   - `m_brc`: Activity 내부에서 처리
3. **템플릿 메서드 패턴**: `setMessage()`를 하위 클래스에서 구현

## 코드 위치 요약

| 항목 | 라인 |
|------|------|
| 클래스 선언 | 29 |
| ScanResultReceiver | 40-63 |
| initScanner() | 66-101 |
| onCreate() | 104-134 |
| onCheckedChanged() | 142-156 |
| onResume() | 159-162 |
| onDestroy() | 165-169 |
| setMessage() | 186-187 |
| m_brc 리시버 | 190-206 |
