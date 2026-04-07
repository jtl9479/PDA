# LoginActivity 종료 버튼이 프린터 테스트를 호출

## 발견일
2026-04-07

## 현상
- LoginActivity 화면에서 종료 버튼 클릭 시 앱 종료 다이얼로그가 아닌 **프린터 선택 다이얼로그**가 뜸
- 앱을 종료할 수 없음

## 원래부터 있던 버그인가?

**NO - 원본에서는 `exitDialog()` 호출. 현재 프로젝트에서 테스트용으로 변경한 것이 원복되지 않음**

```java
// 원본 LoginActivity.java:133-135
case R.id.btnClose:
    exitDialog();
    break;
```

## 원인

### 문제 1 (주요): 종료 버튼에 테스트용 printTest() 코드가 남아있음

#### 코드 위치
- `LoginActivity.java` : 174~179줄

#### 현재 문제 코드
```java
case R.id.btnClose:
    //exitDialog();              // ★ 원래 종료 다이얼로그 주석처리됨
    //프린트 출력 테스트
    this.printTest();            // ★ 테스트용 프린터 호출로 변경됨
    Toast.makeText(this, "메시지", Toast.LENGTH_SHORT).show();
    break;
```

#### 원본 코드
```java
case R.id.btnClose:
    exitDialog();
    break;
```

#### 발생 시나리오
1. LoginActivity 화면에서 "종료" 버튼 클릭
2. `exitDialog()` 대신 `printTest()` 호출
3. 블루투스 프린터 선택 다이얼로그 표시
4. 앱 종료 불가

## 영향 범위
- `LoginActivity.java` : 174~179줄
- LoginActivity 화면에서만 발생 (MainActivity의 종료 버튼은 정상)

## 수정 방안

### 수정: 원본과 동일하게 exitDialog() 복원

```java
case R.id.btnClose:
    exitDialog();
    break;
```

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/소스분석/26_LoginActivity.md`
