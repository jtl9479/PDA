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

## 리팩토링 체크리스트

### Step 1. 미사용 import 삭제 ✅ 완료

- [x] `import android.app.Activity;` (line 3) - 미사용
- [x] `import android.content.SharedPreferences;` (line 8) - 미사용

**동작 변경**: 없음

---

### Step 2. 매직 문자열 상수화 ✅ 완료

**현황**: Intent Extra 키가 하드코딩됨

| 값 | 의미 | 사용 위치 | 상수화 |
|----|------|----------|--------|
| `"BARCODE"` | Intent Extra 키 | 2곳 | ✓ |
| `"READ_FAIL"` | 스캔 실패 결과 | 1곳 | ✗ (1곳만 사용) |

**변경 내용**:
- [x] `EXTRA_BARCODE = "BARCODE"` - 바코드 데이터 Intent Extra 키
- [x] 2곳 사용처 변경

**동작 변경**: 없음

---

### Step 3. 불필요한 조건문 정리 ✅ 완료

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

**변경 내용**:
- [x] `else if (isChecked)` → `else` 변경

**동작 변경**: 없음 (논리적으로 동일)

---

### Step 4. 인쇄 스위치 초기값 설정 간소화 ✅ 완료

**현재 코드** (line 201-206):
```java
if (Common.print_bool) {
    swt_print.setChecked(true);
} else {
    swt_print.setChecked(false);
}
```

**변경 내용**:
- [x] `swt_print.setChecked(Common.print_bool);` 로 간소화

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
| Step 1 | 미사용 import 삭제 | 2026-01-09 | 완료 |
| Step 2 | 매직 문자열 상수화 | 2026-01-09 | 완료 |
| Step 3 | 불필요한 조건문 정리 | 2026-01-09 | 완료 |
| Step 4 | 인쇄 스위치 초기값 간소화 | 2026-01-09 | 완료 |

---

**최종 수정일**: 2026-01-09
