# TextWatcher 수기입력 시 자동실행 문제

## 발견일
2026-04-06

## 현상
- BixolonShipmentActivity에서 work_flag=1(바코드 스캔 모드) 상태로 수기 입력 시
- 5자 이상 입력하면 300ms 후 자동으로 `setBarcodeMsg()` 실행
- 더 입력해야 하는데 **강제로 상품변경 팝업이 뜸**

## 원래부터 있던 버그인가?

**NO - 원본에는 TextWatcher가 없음. 현재 프로젝트에서 신규 추가된 코드에서 발생**

```java
// 원본 ShipmentActivity.java - TextWatcher/addTextChangedListener 코드 없음
// 원본은 ENTER/TAB 키 감지만 사용
```

## 원인

### 문제 1 (주요): TextWatcher가 work_flag=1에서 수기 입력도 자동 처리

#### 코드 위치
- `BixolonShipmentActivity.java` : 485~516줄

#### 현재 문제 코드
```java
// 485줄: TextWatcher
edit_barcode.addTextChangedListener(new TextWatcher() {
    @Override
    public void afterTextChanged(Editable s) {
        if (isInternalTextChange) return;
        if (work_flag == 0) return;  // ★ 수기 입력 모드(0)만 제외

        final String text = s.toString().trim();
        if (text.length() >= 5) {           // ★ 5자 이상이면
            scanAutoRunnable = new Runnable() {
                @Override
                public void run() {
                    setBarcodeMsg(text);     // ★ 300ms 후 자동 실행
                }
            };
            scanAutoHandler.postDelayed(scanAutoRunnable, 300);  // ★ 300ms
        }
    }
});
```

#### 발생 시나리오
1. work_flag=1 (바코드 스캔 모드) 상태
2. 사용자가 `edit_barcode`에 수기로 바코드 번호 입력 시작
3. 5자 입력 시점에서 TextWatcher 동작 → 300ms 타이머 시작
4. 300ms 내 추가 입력하면 타이머 리셋되지만, 잠시 멈추면 자동 실행
5. 아직 전체 바코드(예: 20자리)를 다 입력하지 않았는데 `setBarcodeMsg()` 호출
6. 불완전한 바코드로 상품 매칭 시도 → 상품변경 팝업 또는 오류

### 문제 2 (보조): TextWatcher 추가 배경

#### 추가 이유
- Honeywell EDA51 스캐너가 Keyboard Wedge 모드로 동작
- 일부 스캐너 설정에서 ENTER/TAB 키를 보내지 않는 경우 대응
- 300ms 자동 감지로 스캐너 입력 완료를 판별

#### 원본과의 차이
- 원본: ENTER/TAB 키 감지만 사용 (457~481줄) → 수기 입력 시 ENTER 누를 때까지 대기
- 현재: ENTER/TAB + TextWatcher 300ms 자동 감지 → 수기 입력 시에도 자동 실행

## 영향 범위
- `BixolonShipmentActivity.java` : 485~516줄 (TextWatcher)
- work_flag=1(바코드 스캔), work_flag=2(상품코드) 모드에서 수기 입력 시 발생
- work_flag=0(수기 입력)에서는 발생 안함 (`if (work_flag == 0) return;`으로 제외)

## 수정 방안

### 방안 A: TextWatcher 제거
스캐너가 ENTER/TAB을 보내는 설정이라면 TextWatcher 불필요

```java
// 485~516줄 TextWatcher 코드 제거
// ENTER/TAB 키 감지만 사용 (원본과 동일)
```

### 방안 B: TextWatcher 대기시간 증가
300ms → 1000ms 등으로 늘려 수기 입력 시간 확보

### 방안 C: 최소 길이 조건 강화
5자 → 실제 바코드 최소 길이(예: 13자)로 변경

> 방안 선택 시 Honeywell 스캐너 Keyboard Wedge 설정 확인 필요.
> 스캐너가 ENTER/TAB을 보내는 설정이면 방안 A(제거)가 가장 안전.

## 상태
- [ ] 미수정
