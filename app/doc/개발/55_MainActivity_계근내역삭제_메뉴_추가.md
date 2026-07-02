# MainActivity 소메뉴 "계근내역 삭제" 항목 추가

**작성일**: 2026-07-02
**목적**: MainActivity 오버플로우 소메뉴에 "계근내역 삭제" 항목을 추가하고, SettingActivity 7회 클릭 숨김 기능과 동일한 전체 계근내역 삭제 동작을 연결한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### main.xml — 변경 전 메뉴 항목 (5개)

```xml
<!-- app/src/main/res/menu/main.xml -->
<item android:id="@+id/action_pinrtsettings"  android:title="프린터설정"  app:showAsAction="never"/>
<item android:id="@+id/action_shipmentlist"   android:title="출하대상"    app:showAsAction="never"/>
<item android:id="@+id/action_barcodeinfolist" android:title="바코드정보" app:showAsAction="never"/>
<item android:id="@+id/action_goodswetlist"   android:title="계근데이터"  app:showAsAction="never"/>
<item android:id="@+id/action_daysettings"    android:title="날짜설정"    app:showAsAction="always|withText"/>
```

- 계근데이터 조회(팝업)는 있으나, 계근내역을 직접 삭제하는 메뉴 항목은 없음
- 삭제 기능은 SettingActivity 로고 이미지 7회 클릭이라는 숨김 경로로만 접근 가능

### SettingActivity — 7회 클릭 숨김 기능 (변경 없음)

```java
// SettingActivity.java:115-139
case R.id.imageView:
    clkCount = clkCount + 1;
    if (clkCount > 6) {
        new AlertDialog.Builder(SettingActivity.this, R.style.AppCompatDialogStyle)
                .setIcon(R.drawable.highland)
                .setTitle(alertTitle)
                .setMessage("전체계근내역을 삭제 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBHandler.deletequeryAllGoodsWet(getApplicationContext());
                        Toast.makeText(getApplicationContext(), "전체계근내역이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(buttonNo, null).show();
        clkCount = 0;
    }
    break;
```

### MainActivity.onOptionsItemSelected — 변경 전 분기

```java
// MainActivity.java:129-170 (변경 전)
if (id == R.id.action_pinrtsettings) { startActivity(...); return true; }
if (id == R.id.action_shipmentlist)  { showShipmentListDialog(); return true; }
if (id == R.id.action_barcodeinfolist) { showBarcodeInfoDialog(); return true; }
if (id == R.id.action_goodswetlist)  { showGoodsWetDialog(); return true; }
// action_delete_goodswet 분기 없음
if (id == R.id.action_daysettings)   { new DatePickerDialog(...).show(); return true; }
```

### 문제점

- 계근내역 삭제 경로가 숨김 기능(7회 클릭)에만 있어 사용자가 직관적으로 접근 불가
- 운영 중 계근 오류 데이터 초기화가 필요할 때 접근 경로가 불편함

---

## 2. 변경 구조

### 데이터 흐름

```
[사용자] ⋮ 오버플로우 메뉴 클릭
    ↓
[main.xml] action_delete_goodswet 항목 (계근내역 삭제) — 신규 추가
    ↓
[MainActivity.onOptionsItemSelected] id == R.id.action_delete_goodswet — 신규 분기
    ↓
[showDeleteGoodsWetDialog()] AlertDialog 표시 — 신규 메서드
    ├── 취소 → 아무 동작 없음
    └── 삭제(확인) → DBHandler.deletequeryAllGoodsWet(context)
            ↓
        TB_GOODS_WET 전체 DELETE
        sqlite_sequence 리셋
            ↓
        Toast("전체계근내역이 삭제 되었습니다.")
```

### 변경 전/후 비교

| 구분 | 변경 전 | 변경 후 |
|------|---------|---------|
| 메뉴 항목 수 | 5개 | 6개 |
| 계근내역 삭제 경로 | SettingActivity 로고 7회 클릭(숨김) | 소메뉴 "계근내역 삭제" (신규) + 기존 숨김 기능 유지 |
| showDeleteGoodsWetDialog | 없음 | 신규 추가 |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **main.xml** | `app/src/main/res/menu/main.xml` | action_goodswetlist 아래에 action_delete_goodswet 항목 추가 |
| 2 | **MainActivity.java** | `app/src/main/java/.../MainActivity.java` | onOptionsItemSelected에 action_delete_goodswet 분기 추가 + showDeleteGoodsWetDialog() 신규 메서드 추가 |

---

## 4. 수정 상세

### 4.1 main.xml

**경로**: `app/src/main/res/menu/main.xml`

**변경 전:**

```xml
<item
    android:id="@+id/action_goodswetlist"
    android:orderInCategory="100"
    android:title="계근데이터"
    app:showAsAction="never"/>

<item
    android:id="@+id/action_daysettings"
    android:orderInCategory="100"
    android:title="날짜설정"
    app:showAsAction="always|withText"/>
```

**변경 후:**

```xml
<item
    android:id="@+id/action_goodswetlist"
    android:orderInCategory="100"
    android:title="계근데이터"
    app:showAsAction="never"/>

<item
    android:id="@+id/action_delete_goodswet"
    android:orderInCategory="100"
    android:title="계근내역 삭제"
    app:showAsAction="never"/>

<item
    android:id="@+id/action_daysettings"
    android:orderInCategory="100"
    android:title="날짜설정"
    app:showAsAction="always|withText"/>
```

**검증**: 소메뉴에서 "계근데이터" 아래에 "계근내역 삭제" 항목이 표시되는지 확인

---

### 4.2 MainActivity.java — onOptionsItemSelected 분기 추가

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/MainActivity.java`

**변경 전:**

```java
// 계근데이터 팝업
if (id == R.id.action_goodswetlist) {
    showGoodsWetDialog();
    return true;
}

// 날짜 설정 메뉴 - DatePickerDialog 표시
if (id == R.id.action_daysettings) {
```

**변경 후:**

```java
// 계근데이터 팝업
if (id == R.id.action_goodswetlist) {
    showGoodsWetDialog();
    return true;
}

// 계근내역 삭제 - 전체 계근내역 삭제 다이얼로그 (프린터설정 7회 클릭 숨김 기능과 동일 동작)
if (id == R.id.action_delete_goodswet) {
    showDeleteGoodsWetDialog();
    return true;
}

// 날짜 설정 메뉴 - DatePickerDialog 표시
if (id == R.id.action_daysettings) {
```

**검증**: 소메뉴 "계근내역 삭제" 클릭 시 showDeleteGoodsWetDialog() 호출 확인

---

### 4.3 MainActivity.java — showDeleteGoodsWetDialog() 신규 메서드

**변경 전:** 메서드 없음

**변경 후:**

```java
/**
 * 전체 계근내역 삭제 다이얼로그 표시
 * <p>
 * 프린터설정(SettingActivity) 이미지 7회 클릭 숨김 기능과 동일한 동작.
 * 확인 시 로컬 DB의 전체 계근내역(TB_GOODS_WET)을 삭제한다.
 * </p>
 */
private void showDeleteGoodsWetDialog() {
    new AlertDialog.Builder(MainActivity.this, R.style.AppCompatDialogStyle)
            .setIcon(R.drawable.highland)
            .setTitle(getResources().getString(R.string.app_name))
            .setMessage("전체계근내역을 삭제 하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("삭제", (dialog, which) -> {
                DBHandler.deletequeryAllGoodsWet(getApplicationContext());
                Toast.makeText(getApplicationContext(), "전체계근내역이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(getString(R.string.exit_no), null)
            .show();
}
```

**검증**: SettingActivity 숨김 기능(115~139줄)과 다이얼로그 타이틀/메시지/버튼/삭제 호출이 동일한지 확인

---

## 5. 사이드이펙트

### DBHandler.deletequeryAllGoodsWet — 기존 호출부

```java
// SettingActivity.java:133 — 유지 (변경 없음)
DBHandler.deletequeryAllGoodsWet(getApplicationContext());

// TestDataHelper.java:57 — 유지 (변경 없음)
DBHandler.deletequeryAllGoodsWet(context);
```

- `deletequeryAllGoodsWet`은 TB_GOODS_WET 전체 DELETE + sqlite_sequence 리셋을 수행하는 기존 메서드를 재사용
- 신규 호출부(MainActivity)에서 동일 동작을 트리거할 뿐, 메서드 자체 변경 없음
- SettingActivity 7회 클릭 숨김 기능은 그대로 유지되며 제거하지 않음

### 원본 대비

- 원본(PDA-INNO 원본) MainActivity에는 해당 메뉴 항목이 없음
- 사용자 요청에 의한 신규 UI 기능 추가이며, 기존 메뉴 항목·분기·동작은 무변경
- 기존 기능 100% 동일성 훼손 없음 (기능 추가이지 변경이 아님)

---

## 6. 데이터 저장 구조

### TB_GOODS_WET 삭제 쿼리 흐름

```java
// DBHandler.deletequeryAllGoodsWet (1819줄)
DELETE FROM TB_GOODS_WET;
DELETE FROM sqlite_sequence WHERE name = 'TB_GOODS_WET';
```

| 구분 | 내용 |
|------|------|
| 대상 테이블 | TB_GOODS_WET (로컬 SQLite) |
| 삭제 범위 | 전체 행 (WHERE 조건 없음) |
| 시퀀스 리셋 | sqlite_sequence에서 TB_GOODS_WET 행 삭제 (AUTO_INCREMENT 초기화) |

---

## 7. 호출 시점

```
[사용자] 앱 실행 → MainActivity 표시
    ↓
[사용자] 액션바 ⋮(오버플로우) 클릭
    ↓
[소메뉴 표시]
    ├── 프린터설정
    ├── 출하대상
    ├── 바코드정보
    ├── 계근데이터
    ├── 계근내역 삭제   ← 신규
    └── 날짜설정
         ↓ (계근내역 삭제 선택)
[onOptionsItemSelected] id == R.id.action_delete_goodswet
    ↓
[showDeleteGoodsWetDialog()] AlertDialog 표시 (setCancelable=false)
    ├── 취소 → dismiss (아무 동작 없음)
    └── 삭제 → DBHandler.deletequeryAllGoodsWet(context)
                ↓
            SQLite TB_GOODS_WET 전체 삭제
                ↓
            Toast "전체계근내역이 삭제 되었습니다."
```

---

## 8. 개발 플랜

### Step 1: main.xml 메뉴 항목 추가

**Part 1. 분석**
- 파일: `app/src/main/res/menu/main.xml`
- 범위: action_goodswetlist 항목 아래
- 용도: 소메뉴에 "계근내역 삭제" 항목 표시
- 주의할 점: orderInCategory="100" 유지, showAsAction="never" (오버플로우에만 표시)

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 신규 item | action_goodswetlist 아래 | android:id="@+id/action_delete_goodswet", title="계근내역 삭제" |
| 2 | 기존 item 순서 | action_daysettings | 기존 마지막 항목 순서 유지 |

**Part 2. 변환 계획**
- 변환 방식: action_goodswetlist 닫는 태그 아래에 신규 `<item>` 블록 삽입
- 주의사항: 기존 항목 ID·순서 변경 금지

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료):
- **무엇을**: main.xml에 action_delete_goodswet 항목 추가
- **왜**: 소메뉴에서 계근내역 삭제 기능을 직접 접근하기 위해
- **어떻게**: action_goodswetlist 아래에 `<item android:id="@+id/action_delete_goodswet" ... android:title="계근내역 삭제" app:showAsAction="never"/>` 삽입

---

### Step 2: onOptionsItemSelected 분기 추가

**Part 1. 분석**
- 메서드: `MainActivity.onOptionsItemSelected(MenuItem)`
- 범위: `MainActivity.java:128-171`
- 용도: action_delete_goodswet 선택 시 showDeleteGoodsWetDialog() 호출
- 주의할 점: 기존 분기(action_goodswetlist 등) 로직 변경 금지, action_daysettings 분기 앞에 삽입

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 신규 if 분기 | action_goodswetlist 분기 아래 | id == R.id.action_delete_goodswet → showDeleteGoodsWetDialog() |
| 2 | 기존 분기 | action_daysettings | 기존 위치·로직 유지 |

**Part 2. 변환 계획**
- 변환 방식: action_goodswetlist 블록 종료 후, action_daysettings 블록 시작 전에 신규 if 블록 삽입
- 주의사항: 기존 분기 중 어느 것도 변경하지 않음

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료):
- **무엇을**: onOptionsItemSelected에 action_delete_goodswet 분기 추가
- **왜**: 메뉴 항목 클릭 이벤트를 showDeleteGoodsWetDialog()로 연결하기 위해
- **어떻게**: `if (id == R.id.action_delete_goodswet) { showDeleteGoodsWetDialog(); return true; }` 삽입

---

### Step 3: showDeleteGoodsWetDialog() 신규 메서드 추가

**Part 1. 분석**
- 메서드: 신규 `showDeleteGoodsWetDialog()` (private void)
- 범위: MainActivity.java 내 다른 showXxxDialog() 메서드들과 같은 영역
- 용도: SettingActivity 7회 클릭 숨김 기능과 동일한 다이얼로그+삭제 동작 제공
- 주의할 점: SettingActivity.onClick(115~139줄)과 다이얼로그 구성 요소 동일성 유지

| # | 항목 | SettingActivity 기준값 | MainActivity 적용값 |
|---|------|----------------------|-------------------|
| 1 | Builder context | SettingActivity.this | MainActivity.this |
| 2 | style | R.style.AppCompatDialogStyle | R.style.AppCompatDialogStyle |
| 3 | icon | R.drawable.highland | R.drawable.highland |
| 4 | title | app_name 리소스 | app_name 리소스 |
| 5 | message | "전체계근내역을 삭제 하시겠습니까?" | "전체계근내역을 삭제 하시겠습니까?" |
| 6 | setCancelable | false | false |
| 7 | Positive 버튼 | "삭제" | "삭제" |
| 8 | 삭제 호출 | deletequeryAllGoodsWet | deletequeryAllGoodsWet |
| 9 | Toast 메시지 | "전체계근내역이 삭제 되었습니다." | "전체계근내역이 삭제 되었습니다." |
| 10 | Negative 버튼 | exit_no 리소스 | exit_no 리소스 |

**Part 2. 변환 계획**
- 변환 방식: 기존 showGoodsWetDialog() 또는 showShipmentListDialog() 등 동일 영역에 신규 메서드 추가
- 주의사항: lambda((dialog, which) ->) 사용 — MainActivity import 상태 확인 불필요 (AlertDialog/Toast/DBHandler 이미 존재)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료):
- **무엇을**: showDeleteGoodsWetDialog() 신규 private 메서드 추가
- **왜**: SettingActivity 7회 클릭 숨김 기능과 동일한 삭제 동작을 소메뉴에서도 제공하기 위해
- **어떻게**: AlertDialog.Builder로 확인 다이얼로그 구성 후 확인 시 deletequeryAllGoodsWet() 호출, Toast 표시

---

### Step 4: 통합 테스트

| # | 테스트 항목 | 확인 |
|:-:|-----------|------|
| 1 | 소메뉴 ⋮ 클릭 시 "계근내역 삭제" 항목이 "계근데이터" 아래에 표시됨 | □ |
| 2 | "계근내역 삭제" 클릭 시 확인 다이얼로그 표시됨 | □ |
| 3 | 다이얼로그 제목: 앱 이름, 메시지: "전체계근내역을 삭제 하시겠습니까?" | □ |
| 4 | "취소" 클릭 시 다이얼로그 닫히고 TB_GOODS_WET 데이터 유지됨 | □ |
| 5 | "삭제" 클릭 시 TB_GOODS_WET 전체 삭제됨 (소메뉴 → 계근데이터로 확인) | □ |
| 6 | 삭제 후 Toast "전체계근내역이 삭제 되었습니다." 표시됨 | □ |
| 7 | SettingActivity 이미지 7회 클릭 숨김 기능이 기존과 동일하게 동작함 | □ |
| 8 | 기존 소메뉴 항목(프린터설정/출하대상/바코드정보/계근데이터/날짜설정) 동작 무변경 확인 | □ |

---

### 개발 순서 요약

```
Step 1: main.xml 메뉴 항목 추가
    ↓
Step 2: onOptionsItemSelected 분기 추가
    ↓
Step 3: showDeleteGoodsWetDialog() 신규 메서드 추가
    ↓
Step 4: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 계근내역 삭제 — 정상 삭제

```
1. PDA 앱 실행 → MainActivity 진입
2. 출하대상 받기 → 계근 데이터 수신 (TB_GOODS_WET에 데이터 존재 상태)
3. 액션바 ⋮ 소메뉴 클릭
4. "계근내역 삭제" 항목 클릭
5. 확인 다이얼로그 표시 확인 (제목·메시지·버튼 문구 확인)
6. "삭제" 버튼 클릭
7. Toast "전체계근내역이 삭제 되었습니다." 확인
8. 소메뉴 → "계근데이터" 클릭 → 빈 목록 확인
```

### 시나리오 2: 계근내역 삭제 — 취소

```
1. 시나리오 1의 1~5 동일
2. "취소" 버튼 클릭
3. 다이얼로그 닫힘 확인
4. 소메뉴 → "계근데이터" 클릭 → 기존 데이터 그대로 유지 확인
```

### 시나리오 3: SettingActivity 숨김 기능 유지 확인

```
1. 소메뉴 → "프린터설정" 클릭 → SettingActivity 진입
2. 로고 이미지를 7회 연속 클릭
3. 확인 다이얼로그 표시 확인
4. "삭제" 클릭 후 TB_GOODS_WET 삭제 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 메뉴 항목이 소메뉴에 표시되지 않음 | main.xml 항목 누락 또는 showAsAction 값 오류 | showAsAction="never" 확인, R.id.action_delete_goodswet 중복 여부 확인 |
| 2 | 클릭 시 아무 반응 없음 | onOptionsItemSelected 분기 누락 또는 return true 미작성 | 분기 삽입 위치·return true 확인 |
| 3 | 삭제 후에도 계근데이터 남아있음 | deletequeryAllGoodsWet context 오류 | getApplicationContext() 전달 확인 |
| 4 | 기존 메뉴 항목 동작 이상 | 분기 삽입 시 기존 if 블록 수정 | 변경 전/후 diff 검토, 기존 분기 코드 일치 확인 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | main.xml 메뉴 항목 추가 | ✅ 완료 |
| 2 | onOptionsItemSelected 분기 추가 | ✅ 완료 |
| 3 | showDeleteGoodsWetDialog() 신규 메서드 추가 | ✅ 완료 |
| 4 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/소스분석/` — DBHandler.deletequeryAllGoodsWet 소스 분석

---

**문서 버전**: 1.0
