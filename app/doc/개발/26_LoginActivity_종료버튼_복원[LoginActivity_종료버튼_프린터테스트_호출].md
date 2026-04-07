# LoginActivity 종료 버튼 복원

**작성일**: 2026-04-07
**목적**: LoginActivity 종료 버튼을 원본과 동일하게 exitDialog() 호출로 복원
**관련 오류**: `app/doc/오류/11_LoginActivity_종료버튼_프린터테스트_호출.md`

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### LoginActivity.java (174~179줄)

```java
case R.id.btnClose:
    //exitDialog();
    //프린트 출력 테스트
    this.printTest();
    Toast.makeText(this, "메시지", Toast.LENGTH_SHORT).show();
    break;
```

- exitDialog() 주석처리됨
- 테스트용 printTest() 호출
- 불필요한 Toast 메시지

### 원본 LoginActivity.java (133~135줄)

```java
case R.id.btnClose:
    exitDialog();
    break;
```

### 문제점

- 종료 버튼 클릭 시 프린터 선택 다이얼로그가 뜸
- 앱 종료 불가

---

## 2. 변경 구조

```
[변경 전]
종료 버튼 → printTest() → 프린터 선택 다이얼로그

[변경 후]
종료 버튼 → exitDialog() → 종료 확인 다이얼로그 → 앱 종료
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **LoginActivity.java** | 174~179줄 | exitDialog() 복원, printTest() 및 Toast 제거 |

---

## 4. 수정 상세

### 4.1 LoginActivity.java

**경로**: `app/src/main/java/.../LoginActivity.java`

**변경 전 (174~179줄):**

```java
case R.id.btnClose:
    //exitDialog();
    //프린트 출력 테스트
    this.printTest();
    Toast.makeText(this, "메시지", Toast.LENGTH_SHORT).show();
    break;
```

**변경 후 (원본과 동일):**

```java
case R.id.btnClose:
    exitDialog();
    break;
```

---

## 5. 사이드이펙트

- printTest(), showPrinterSelectDialog() 메서드가 LoginActivity에 남아있지만 호출되지 않음
- 테스트용 코드 정리는 별도 작업으로 진행 (현재는 종료 버튼 복원만)
- MainActivity의 종료 버튼은 이미 정상 (exitDialog() 호출)

---

## 6. 개발 플랜

### Step 1: 종료 버튼 exitDialog() 복원

**Part 1. 분석**
- 대상: LoginActivity.java 174~179줄
- 범위: btnClose case문 내부만 수정
- 주의할 점: exitDialog() 메서드가 LoginActivity에 존재하는지 확인

**체크리스트**
- [x] Part 1: exitDialog() 메서드 존재 확인 (298줄)
- [x] Part 2: 변환 수행
- [x] Part 3: 컴파일 확인 (BUILD SUCCESSFUL)
- [ ] Part 4: 종료 버튼 클릭 → 종료 확인 다이얼로그 표시 확인

**Part 6. 변경 내용**:
- **무엇을**: LoginActivity btnClose에서 printTest() → exitDialog() 복원
- **왜**: 테스트용 코드가 원복되지 않아 종료 버튼이 프린터 테스트를 호출
- **어떻게**: 174~180줄을 원본과 동일하게 exitDialog() 호출로 변경

---

### 개발 순서 요약

```
Step 1: 종료 버튼 exitDialog() 복원
```

---

## 7. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | 종료 버튼 exitDialog() 복원 | ✅ 완료 (실기기 테스트 대기) |

---

## 관련 문서

- `app/doc/오류/11_LoginActivity_종료버튼_프린터테스트_호출.md`
- `app/doc/소스분석/26_LoginActivity.md`

---

**문서 버전**: 1.0
