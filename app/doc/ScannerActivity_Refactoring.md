# ScannerActivity.java 리팩토링 계획

---

## 최우선 원칙

### 기존 동작과 100% 동일하게 동작해야 한다

이것이 리팩토링의 **제일 중요한 원칙**이다.

- 모든 변경은 **기능 변화 없이** 코드 구조만 개선해야 한다
- 리팩토링 후 **동일한 입력에 동일한 출력**이 보장되어야 한다
- 의심스러운 변경은 **하지 않는다**

---

## 개요

- **파일 위치**: `app/src/main/java/com/rgbsolution/highland_emart/scanner/ScannerActivity.java`
- **현재 코드 라인**: 316줄
- **작성일**: 2026-01-09
- **소스 확인**: 전체 소스 코드 직접 확인 완료

---

## 현재 코드 구조 (실제 라인 기준)

| 영역 | 라인 범위 | 줄 수 | 설명 |
|-----|----------|------|------|
| import 및 클래스 선언 | 1~53 | 53 | 패키지, import, 클래스 Javadoc |
| 상수 | 55~62 | 8 | TAG, RECEIVE_PM80 |
| PM80 SDK 변수 | 64~72 | 9 | mScanner, mDecodeResult |
| UI 컴포넌트 | 74~82 | 9 | btn_init, swt_print |
| ScanResultReceiver | 84~120 | 37 | PM80 SDK 결과 수신 |
| initScanner() | 122~161 | 40 | PM80 스캐너 초기화 |
| Activity 생명주기 | 163~269 | 107 | onCreate, onResume, onDestroy 등 |
| 바코드 수신 콜백 | 271~285 | 15 | setMessage() |
| m_brc | 287~316 | 30 | 내부 브로드캐스트 수신 |

---

## 리팩토링 체크리스트

### Step 1. 미사용 import 삭제

**현재 코드 (삭제 대상)**:
```java
import android.app.Activity;              // line 3 - 미사용
import android.content.SharedPreferences; // line 8 - 미사용
```

**동작 변경**: 없음

---

### Step 2. 매직 문자열 상수화

**현황**: Intent Extra 키, 스캔 실패 문자열이 하드코딩됨

| 값 | 의미 | 사용 위치 |
|----|------|----------|
| `"READ_FAIL"` | 스캔 실패 결과 | line 112 |
| `"BARCODE"` | Intent Extra 키 | line 114, 311 |

**변경 내용**:
```java
// 상수 섹션에 추가
/** 스캔 실패 결과 문자열 */
private static final String SCAN_RESULT_FAIL = "READ_FAIL";

/** 바코드 데이터 Intent Extra 키 */
private static final String EXTRA_BARCODE = "BARCODE";
```

**동작 변경**: 없음

---

### Step 3. 불필요한 조건문 정리

**현재 코드** (line 227):
```java
if (!isChecked) {
    Toast.makeText(getApplicationContext(), "인쇄 : OFF", Toast.LENGTH_SHORT).show();
    Common.print_bool = false;
} else if (isChecked) {  // 불필요한 조건
    Toast.makeText(getApplicationContext(), "인쇄 : ON", Toast.LENGTH_SHORT).show();
    Common.print_bool = true;
}
```

**변경 후**:
```java
if (!isChecked) {
    Toast.makeText(getApplicationContext(), "인쇄 : OFF", Toast.LENGTH_SHORT).show();
    Common.print_bool = false;
} else {
    Toast.makeText(getApplicationContext(), "인쇄 : ON", Toast.LENGTH_SHORT).show();
    Common.print_bool = true;
}
```

**동작 변경**: 없음 (논리적으로 동일)

---

### Step 4. 인쇄 스위치 초기값 설정 간소화

**현재 코드** (line 201-206):
```java
if (Common.print_bool) {
    swt_print.setChecked(true);
} else {
    swt_print.setChecked(false);
}
```

**변경 후**:
```java
swt_print.setChecked(Common.print_bool);
```

**동작 변경**: 없음 (논리적으로 동일)

---

## 리팩토링 우선순위

| 순위 | Step | 항목 | 동작 변경 | 난이도 |
|-----|------|------|----------|-------|
| 1 | Step 1 | 미사용 import 삭제 | 없음 | 낮음 |
| 2 | Step 2 | 매직 문자열 상수화 | 없음 | 낮음 |
| 3 | Step 3 | 불필요한 조건문 정리 | 없음 | 낮음 |
| 4 | Step 4 | 인쇄 스위치 초기값 간소화 | 없음 | 낮음 |

---

## 테스트 체크리스트

리팩토링 후 반드시 확인해야 할 항목:

### 바코드 스캔 테스트
- [ ] PM80 디바이스에서 바코드 스캔 정상 동작
- [ ] ScanResultReceiver → m_brc → setMessage() 흐름 정상
- [ ] 스캔 실패 시 "READ_FAIL" 처리 정상

### Activity 생명주기 테스트
- [ ] Activity 진입 시 BroadcastReceiver 정상 등록
- [ ] Activity 종료 시 BroadcastReceiver 정상 해제

### ActionBar 테스트
- [ ] 뒤로가기 버튼 동작
- [ ] 인쇄 스위치 ON/OFF 동작
- [ ] 인쇄 스위치 초기값 설정 정상

---

## 관련 문서

- [ScannerActivity.md](ScannerActivity.md) - ScannerActivity 분석 문서
- [ScannerActivity_Migration_EDA51.md](ScannerActivity_Migration_EDA51.md) - Honeywell EDA51 마이그레이션 문서
- [ShipmentActivity_Refactoring.md](ShipmentActivity_Refactoring.md) - ShipmentActivity 리팩토링 계획

---

## 완료 기록

| 단계 | 작업 내용 | 완료 일자 | 비고 |
|-----|----------|----------|------|
| - | 주석 추가 | 2026-01-09 | 클래스, 멤버 변수, 메서드 주석 |
| - | 리팩토링 계획 문서 작성 | 2026-01-09 | 전체 소스 확인 후 작성 |
| Step 1 | 미사용 import 삭제 | - | - |
| Step 2 | 매직 문자열 상수화 | - | - |
| Step 3 | 불필요한 조건문 정리 | - | - |
| Step 4 | 인쇄 스위치 초기값 간소화 | - | - |

---

**최종 수정일**: 2026-01-09
