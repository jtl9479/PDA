# Honeywell 바코드 BroadcastReceiver Action 불일치

## 발견일
2026-04-07

## 현상
- PDA에서 바코드 스캔 시 BroadcastReceiver가 수신하지 못함
- 바코드 데이터가 Keyboard Wedge 모드로 edit_barcode에 직접 입력됨
- 수기 입력 후 입력 버튼을 눌러야 동작함
- Logcat에 `"Receiver action From Honeywell"` 로그 미출력

## 원래부터 있던 버그인가?

**YES - HoneywellScannerActivity 신규 개발 시 Action 문자열이 잘못 설정됨**

## 원인

### 문제 1 (주요): BroadcastReceiver Action 문자열 불일치

#### 코드 위치
- `HoneywellScannerActivity.java` : 58~59줄

#### 현재 문제 코드
```java
// HoneywellScannerActivity.java:58-59
private static final String ACTION_BARCODE_DATA =
        "com.honeywell.scantointent.intent.action.BARCODE_DATA";  // ★ 잘못된 Action
```

#### 실제 PDA 로그 (adb logcat)
```
Sending non-protected broadcast com.honeywell.intent.action.SCAN_RESULT
    from system 2552:com.intermec.datacollectionservice/1000
```

#### 불일치 비교

| 항목 | 현재 코드 | 실제 PDA |
|------|----------|---------|
| Action | `com.honeywell.scantointent.intent.action.BARCODE_DATA` | `com.honeywell.intent.action.SCAN_RESULT` |

### 문제 2 (보조): Extra 키도 확인 필요

#### 코드 위치
- `HoneywellScannerActivity.java` : 62~63줄

```java
private static final String EXTRA_BARCODE_DATA =
        "com.honeywell.scantointent.intent.extra.DATA";  // ★ 확인 필요
```

실제 PDA에서 바코드 데이터를 담는 Extra 키가 다를 수 있음.
Logcat에서 `InputMethodService: Commit scan result` 로그로 보아 Keyboard Wedge로 동작 중.

### 현재 동작 상태

```
PDA 바코드 스캔
    ↓
com.honeywell.intent.action.SCAN_RESULT 브로드캐스트 발송 (실제)
    ↓
HoneywellScannerActivity.m_brc: com.honeywell.scantointent.intent.action.BARCODE_DATA 대기 중
    ↓
Action 불일치 → 수신 못함
    ↓
Keyboard Wedge 모드로 edit_barcode에 텍스트 입력됨
    ↓
TextWatcher 제거 (개발25) → ENTER 키 없이는 자동 처리 안됨
    ↓
수기로 입력 버튼 눌러야 동작
```

## 영향 범위
- `HoneywellScannerActivity.java` : 58~63줄 (Action, Extra 상수)
- BixolonShipmentActivity, ShipmentActivity, ProductionActivity 모두 영향 (HoneywellScannerActivity 상속)

## 수정 방안

### 수정 1: Action 문자열 변경

```java
private static final String ACTION_BARCODE_DATA =
        "com.honeywell.intent.action.SCAN_RESULT";
```

### 수정 2: Extra 키 확인 후 변경

adb logcat으로 실제 Extra 키 확인 필요:
```
adb logcat | grep -i "SCAN_RESULT\|extra\|barcode"
```

확인 후 EXTRA_BARCODE_DATA 상수도 수정.

### 수정 3: TextWatcher 재검토

Action 수정 후 BroadcastReceiver가 정상 동작하면 TextWatcher 불필요 (개발25 유지).
BroadcastReceiver가 동작하지 않으면 Keyboard Wedge 대응 필요 (TextWatcher 복원 검토).

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/개발/25_TextWatcher_제거[TextWatcher_수기입력시_자동실행_문제].md`
- `app/doc/소스분석/33_HoneywellScannerActivity_계획.md`
