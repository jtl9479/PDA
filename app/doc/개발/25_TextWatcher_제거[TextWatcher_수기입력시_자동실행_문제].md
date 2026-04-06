# TextWatcher 제거 (수기입력 시 자동실행 문제 수정)

**작성일**: 2026-04-06
**목적**: BixolonShipmentActivity의 TextWatcher(300ms 자동감지) 제거하여 원본과 동일하게 변경
**관련 오류**: `app/doc/오류/10_TextWatcher_수기입력시_자동실행_문제.md`

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### BixolonShipmentActivity.java (485~516줄)

```java
// Keyboard Wedge 모드 지원 (2): TextWatcher - ENTER 키 없는 스캐너 대응
// 텍스트 입력 후 300ms 동안 추가 입력 없으면 자동 처리
edit_barcode.addTextChangedListener(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override
    public void afterTextChanged(Editable s) {
        if (isInternalTextChange) return;
        if (work_flag == 0) return;

        if (scanAutoRunnable != null) {
            scanAutoHandler.removeCallbacks(scanAutoRunnable);
        }

        final String text = s.toString().trim();
        if (text.length() >= 5) {
            scanAutoRunnable = new Runnable() {
                @Override
                public void run() {
                    setBarcodeMsg(text);
                }
            };
            scanAutoHandler.postDelayed(scanAutoRunnable, 300);
        }
    }
});
```

### 추가 배경
- Honeywell 스캐너 Keyboard Wedge 모드 대응으로 추가
- 원본(ShipmentActivity)에는 없는 코드

### 문제점
- work_flag=1(바코드 스캔 모드)에서 수기 입력 시 5자 이상 → 300ms 후 자동 실행
- 바코드 전체를 다 입력하기 전에 강제로 상품변경 팝업이 뜸

### 제거 근거
- HoneywellScannerActivity가 BroadcastReceiver로 바코드 직접 수신 (setMessage())
- BroadcastReceiver 경로는 edit_barcode를 거치지 않음 → TextWatcher 관여 안함
- ENTER/TAB 키 감지 (457~481줄)는 유지 → 수기 입력 시 ENTER 누를 때까지 대기
- 원본과 동일한 동작으로 복원

---

## 2. 변경 구조

### 바코드 입력 경로 (변경 후)

```
[스캐너 하드웨어 버튼]
  → BroadcastReceiver → setMessage() → setBarcodeMsg()  (유지)

[수기 입력]
  → edit_barcode 타이핑 → ENTER/TAB 키 → setBarcodeMsg()  (유지)
  → TextWatcher 300ms 자동감지  (제거)
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **BixolonShipmentActivity.java** | 485~516줄 | TextWatcher 코드 제거 |
| 2 | **BixolonShipmentActivity.java** | 관련 필드 | scanAutoHandler, scanAutoRunnable, isInternalTextChange 사용처 확인 |

---

## 4. 수정 상세

### 4.1 BixolonShipmentActivity.java - TextWatcher 제거

**경로**: `app/src/main/java/.../BixolonShipmentActivity.java`

**삭제 대상 (485~516줄):**

```java
// Keyboard Wedge 모드 지원 (2): TextWatcher - ENTER 키 없는 스캐너 대응
// 텍스트 입력 후 300ms 동안 추가 입력 없으면 자동 처리
edit_barcode.addTextChangedListener(new TextWatcher() {
    // ... 전체 삭제
});
```

**유지 대상 (457~481줄):** ENTER/TAB 키 감지 (원본과 동일)

```java
edit_barcode.setOnKeyListener(new View.OnKeyListener() {
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_TAB)
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            // ... 유지
        }
    }
});
```

### 4.2 관련 필드 확인

scanAutoHandler, scanAutoRunnable, isInternalTextChange가 TextWatcher 외에 다른 곳에서도 사용되는지 확인 필요.
사용되지 않으면 필드 선언도 함께 제거.

---

## 5. 사이드이펙트

- ENTER/TAB 키 감지는 유지되므로 수기 입력 기능 정상 동작
- BroadcastReceiver 바코드 수신은 TextWatcher와 무관하므로 스캐너 기능 정상 동작
- setMessage() 내부의 scanAutoRunnable 취소 코드 (1096~1098줄)도 함께 제거 필요

---

## 6. 개발 플랜

### Step 1: TextWatcher 코드 및 관련 필드 제거

**Part 1. 분석**
- 대상: BixolonShipmentActivity.java
- 범위: TextWatcher (485~516줄), 관련 필드, setMessage() 내 타이머 취소 코드
- 주의할 점: ENTER/TAB 키 감지 (457~481줄) 변경 없음

**체크리스트**
- [ ] Part 1: scanAutoHandler, scanAutoRunnable, isInternalTextChange 사용처 전수 확인
- [ ] Part 2: TextWatcher 코드 제거
- [ ] Part 3: 관련 필드/코드 정리
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 수기 입력 시 ENTER 누를 때까지 대기하는지 확인

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### 개발 순서 요약

```
Step 1: TextWatcher 코드 및 관련 필드 제거
```

---

## 7. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | TextWatcher 코드 및 관련 필드 제거 | ⏳ 대기 |

---

**문서 버전**: 1.0
