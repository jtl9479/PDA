# Honeywell BroadcastReceiver Action 수정

**작성일**: 2026-04-07
**목적**: HoneywellScannerActivity의 BroadcastReceiver Action 문자열을 실제 PDA에 맞게 수정
**관련 오류**: `app/doc/오류/13_Honeywell_바코드_BroadcastReceiver_Action불일치.md`

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### HoneywellScannerActivity.java (58~63줄)

```java
private static final String ACTION_BARCODE_DATA =
        "com.honeywell.scantointent.intent.action.BARCODE_DATA";

private static final String EXTRA_BARCODE_DATA =
        "com.honeywell.scantointent.intent.extra.DATA";
```

### adb logcat 확인 결과

```
Sending non-protected broadcast com.honeywell.intent.action.SCAN_RESULT
    from system 2552:com.intermec.datacollectionservice/1000

InputMethodService: Commit scan result to a editable field: 111010001550610933202604020001
```

- 실제 Action: `com.honeywell.intent.action.SCAN_RESULT`
- 현재 Keyboard Wedge 모드로 edit_barcode에 직접 입력 중

### 문제점

- Action 문자열 불일치 → BroadcastReceiver 수신 불가
- Extra 키도 확인 필요

---

## 2. 변경 구조

```
[변경 전]
PDA 스캔 → com.honeywell.intent.action.SCAN_RESULT 발송
         → 코드: com.honeywell.scantointent.intent.action.BARCODE_DATA 대기
         → 불일치 → 수신 못함

[변경 후]
PDA 스캔 → com.honeywell.intent.action.SCAN_RESULT 발송
         → 코드: com.honeywell.intent.action.SCAN_RESULT 대기
         → 일치 → 수신 성공 → setMessage()
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **HoneywellScannerActivity.java** | 58~63줄 | ACTION, EXTRA 상수 수정 |

---

## 4. 수정 상세

### 4.1 HoneywellScannerActivity.java

**경로**: `app/src/main/java/.../scanner/HoneywellScannerActivity.java`

**변경 전 (58~63줄):**

```java
private static final String ACTION_BARCODE_DATA =
        "com.honeywell.scantointent.intent.action.BARCODE_DATA";

private static final String EXTRA_BARCODE_DATA =
        "com.honeywell.scantointent.intent.extra.DATA";
```

**변경 후:**

```java
private static final String ACTION_BARCODE_DATA =
        "com.honeywell.intent.action.SCAN_RESULT";

private static final String EXTRA_BARCODE_DATA =
        "data";
```

**참고**: Honeywell `SCAN_RESULT` Intent의 Extra 키는 일반적으로 `"data"`임.
Step 1에서 Action 수정 후 실기기 테스트로 Extra 키 확인.

---

## 5. 사이드이펙트

- HoneywellScannerActivity를 상속하는 모든 Activity에 영향:
  - BixolonShipmentActivity
  - ShipmentActivity
  - ProductionActivity
- 상수만 변경이므로 하위 Activity 코드 수정 불필요
- BroadcastReceiver 정상 동작 시 개발25(TextWatcher 제거) 유지 가능

---

## 6. 개발 플랜

### Step 1: Action 문자열 수정 + Extra 키 확인

**Part 1. 분석**
- 대상: HoneywellScannerActivity.java 58~63줄
- 범위: ACTION_BARCODE_DATA, EXTRA_BARCODE_DATA 상수 2개 수정
- 주의할 점: Extra 키는 실기기 테스트로 확인 필요

**체크리스트**
- [x] Part 1: Action 문자열 수정 (`com.honeywell.intent.action.SCAN_RESULT`)
- [x] Part 2: Extra 키 수정 (`decode_rslt` - adb logcat으로 확인)
- [x] Part 3: 컴파일 확인 (BUILD SUCCESSFUL)
- [ ] Part 4: 실기기 바코드 스캔 → Logcat에 "Receiver action From Honeywell" 출력 확인
- [ ] Part 5: setMessage() → setBarcodeMsg() 정상 호출 확인

**Part 6. 변경 내용**:
- **무엇을**: ACTION_BARCODE_DATA, EXTRA_BARCODE_DATA 상수 2개 수정
- **왜**: PDA 실제 Action(`com.honeywell.intent.action.SCAN_RESULT`)과 코드 Action 불일치
- **어떻게**: Action → `com.honeywell.intent.action.SCAN_RESULT`, Extra → `data`

---

### 개발 순서 요약

```
Step 1: Action 문자열 수정 + Extra 키 확인
```

---

## 7. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | Action 문자열 수정 + Extra 키 확인 | ✅ 코드 완료 (실기기 테스트 대기) |

---

## 관련 문서

- `app/doc/오류/13_Honeywell_바코드_BroadcastReceiver_Action불일치.md`
- `app/doc/개발/25_TextWatcher_제거[TextWatcher_수기입력시_자동실행_문제].md`
- `app/doc/소스분석/33_HoneywellScannerActivity_계획.md`

---

**문서 버전**: 1.0
