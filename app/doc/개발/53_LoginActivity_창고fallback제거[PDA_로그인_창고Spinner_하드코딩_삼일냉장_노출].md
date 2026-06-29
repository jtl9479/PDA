# LoginActivity 창고 fallback 제거 [PDA 로그인 창고Spinner 하드코딩 삼일냉장 노출]

**작성일**: 2026-06-29
**목적**: LoginActivity의 창고 조회 0건 시 "삼일냉장"/"1" 하드코딩 fallback을 제거하고, 안내 메시지 표시 + 로그인 차단으로 대체한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### LoginActivity — WarehouseSearchTask.onPostExecute (88-153줄)

```java
// LoginActivity.java:107-152

@Override
protected void onPostExecute(String receiveData) {
    receiveData = receiveData.replace("\r\n", "").replace("\n", "");

    Common.warehouseNames.clear();
    Common.warehouseCodes.clear();

    if (receiveData != null && !receiveData.isEmpty()) {
        String[] rows = receiveData.split(";;");
        for (String row : rows) {
            if (row.isEmpty()) continue;
            String[] cols = row.split("::", -1);
            if (cols.length >= 3) {
                Common.warehouseCodes.add(cols[1].trim());  // 창고코드
                Common.warehouseNames.add(cols[2].trim());  // 창고명
            }
        }
    }

    // ★ 문제 코드: 조회 실패 시 기본값 하드코딩 (127-130줄)
    if (Common.warehouseNames.isEmpty()) {
        Common.warehouseNames.add("삼일냉장");
        Common.warehouseCodes.add("1");
    }

    // Spinner 구성 (132-141줄)
    ArrayAdapter<String> adapter = new ArrayAdapter<>(
        LoginActivity.this,
        android.R.layout.simple_spinner_item,
        Common.warehouseNames
    );
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    Spinner spin = (Spinner) findViewById(R.id.storeSpinner);
    spin.setAdapter(adapter);

    // onItemSelected 리스너 (143-151줄)
    spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView parent, View v, int position, long id) {
            Common.selectWarehouse = Common.warehouseNames.get(position);
            Common.selectWarehouseCode = Common.warehouseCodes.get(position);
        }
        public void onNothingSelected(AdapterView parent) {}
    });
}
```

```java
// LoginActivity.java:156-178 — 로그인 버튼 onClick (현재 창고 검증 없음)
public void onClick(View v) {
    switch (v.getId()) {
        case R.id.btnLogin:
            if (("").equals(editID.getText().toString())) {
                editID.setError("아이디를 입력하세요.");
                break;
            }
            if (("").equals(editPWD.getText().toString())) {
                editPWD.setError("비밀번호를 입력하세요.");
                break;
            }
            user_id = editID.getText().toString();
            user_pwd = editPWD.getText().toString();
            new ProgressDlgLogin(LoginActivity.this).execute();
            break;
        case R.id.btnClose:
            exitDialog();
            break;
    }
}
```

### Common.java 관련 전역 변수 (41-51줄)

```java
public static String selectCompanyCode = "20";        // 회사코드 기본값
public static String selectWarehouse = "";            // 창고명 (기본값 빈문자열)
public static String selectWarehouseCode = "";        // 창고코드 (기본값 빈문자열)
public static ArrayList<String> warehouseNames = new ArrayList<>();
public static ArrayList<String> warehouseCodes = new ArrayList<>();
```

### 문제점

- **fallback 주입**: 창고 조회 0건(DB 미설정 포함)이면 "삼일냉장"/"1"이 무조건 삽입되어 잘못된 창고로 로그인됨
- **오류 은폐**: 0건임에도 오류 메시지 없이 정상처럼 보여 원인 파악이 어려움
- **로그인 차단 없음**: onClick(btnLogin)에 창고 목록 비어있음 검증이 없어 잘못된 창고코드("1")로 로그인 가능
- **하드코딩**: 프로젝트 규칙(하드코딩 변경 대상)에 해당하는 구 시스템 잔존 코드

---

## 2. 변경 구조

### 데이터 흐름 — 변경 전

```
onCreate → WarehouseSearchTask.doInBackground (search_warehouse.jsp POST)
    ↓
onPostExecute
    ├── [데이터 있음] → Spinner 구성 → onItemSelected → selectWarehouse/Code 설정
    └── [0건]        → "삼일냉장"/"1" 주입 → Spinner에 "삼일냉장" 표시 (★ 문제)
          ↓
onClick(btnLogin) → ID/PWD 검증만 → ProgressDlgLogin 실행 (창고 검증 없음)
```

### 데이터 흐름 — 변경 후

```
onCreate → WarehouseSearchTask.doInBackground (search_warehouse.jsp POST)
    ↓
onPostExecute
    ├── [데이터 있음] → Spinner 구성 → onItemSelected → selectWarehouse/Code 설정 (정상 유지)
    └── [0건]        → AlertDialog 안내 표시 (★ 변경)
                        "등록된 PDA 사용 창고가 없습니다.
                         ERP 창고관리(C0114)에서 PDA여부를 설정하세요."
          ↓
onClick(btnLogin)
    ├── [창고 목록 비어있음] → Toast 안내 + break (★ 추가 — 로그인 차단)
    ├── [ID 빈값]            → 기존 검증 (변경 없음)
    ├── [PWD 빈값]           → 기존 검증 (변경 없음)
    └── [정상]               → ProgressDlgLogin 실행 (변경 없음)
```

### 원본 대비 정당성

원본(`PDA-INNO(원본)/LoginActivity.java:43`)은 창고를 `{"부산센터","이천1센터","삼일냉장","SWC","탑로지스"}` 하드코딩 배열로 처리했고 창고코드 개념이 없었다. 현재 전환본의 DB 조회 방식 자체가 전환 목표 #3(창고코드 하드코딩 변경)에 부합하는 정당한 변경이다. 이번 fallback 제거 역시 "하드코딩 제거" 대상이며 "원본 100% 동일" 위반이 아니다.

**단, 정상 데이터가 있을 때의 동작(실제 창고 목록 Spinner 표시 + 선택)은 100% 유지된다.**

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **LoginActivity.java** | `app/src/main/java/com/rgbsolution/highland_emart/LoginActivity.java` | ① onPostExecute: fallback 제거 + 0건 AlertDialog 추가 |
| 2 | **LoginActivity.java** | 위 동일 파일 | ② onClick(btnLogin): 창고 목록 비어있음 검증 추가 |

---

## 4. 수정 상세

### 4.1 LoginActivity.java — onPostExecute fallback 제거 및 0건 안내

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/LoginActivity.java`

**변경 전 (127-152줄):**

```java
// 조회 실패 시 기본값
if (Common.warehouseNames.isEmpty()) {
    Common.warehouseNames.add("삼일냉장");
    Common.warehouseCodes.add("1");
}

// Spinner 구성
ArrayAdapter<String> adapter = new ArrayAdapter<>(
    LoginActivity.this,
    android.R.layout.simple_spinner_item,
    Common.warehouseNames
);
adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

Spinner spin = (Spinner) findViewById(R.id.storeSpinner);
spin.setAdapter(adapter);

spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        Common.selectWarehouse = Common.warehouseNames.get(position);
        Common.selectWarehouseCode = Common.warehouseCodes.get(position);
        Log.i(TAG, "창고 선택: " + Common.selectWarehouse
            + " (" + Common.selectWarehouseCode + ")");
    }
    public void onNothingSelected(AdapterView parent) {}
});
```

**변경 후 (제안):**

```java
// 조회 0건: 안내 다이얼로그 표시, Spinner는 빈 상태 유지
if (Common.warehouseNames.isEmpty()) {
    new AlertDialog.Builder(LoginActivity.this)
        .setTitle("창고 목록 없음")
        .setMessage("등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요.")
        .setPositiveButton("확인", null)
        .show();
    return;  // Spinner 구성 생략 — selectWarehouse/Code는 "" 상태 유지
}

// Spinner 구성 (데이터 있을 때만 실행 — 기존 로직 100% 유지)
ArrayAdapter<String> adapter = new ArrayAdapter<>(
    LoginActivity.this,
    android.R.layout.simple_spinner_item,
    Common.warehouseNames
);
adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

Spinner spin = (Spinner) findViewById(R.id.storeSpinner);
spin.setAdapter(adapter);

spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        Common.selectWarehouse = Common.warehouseNames.get(position);
        Common.selectWarehouseCode = Common.warehouseCodes.get(position);
        Log.i(TAG, "창고 선택: " + Common.selectWarehouse
            + " (" + Common.selectWarehouseCode + ")");
    }
    public void onNothingSelected(AdapterView parent) {}
});
```

**검증**: 데이터 있을 때 Spinner 구성 로직 전체가 그대로 실행됨을 확인. 0건 시 return으로 Spinner 구성 코드에 진입하지 않음 확인.

---

### 4.2 LoginActivity.java — onClick(btnLogin) 창고 목록 검증 추가

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/LoginActivity.java`

**변경 전 (158-173줄):**

```java
case R.id.btnLogin:
    if (("").equals(editID.getText().toString())) {
        editID.setError("아이디를 입력하세요.");
        break;
    }
    if (("").equals(editPWD.getText().toString())) {
        editPWD.setError("비밀번호를 입력하세요.");
        break;
    }
    user_id = editID.getText().toString();
    user_pwd = editPWD.getText().toString();
    new ProgressDlgLogin(LoginActivity.this).execute();
    break;
```

**변경 후 (제안):**

```java
case R.id.btnLogin:
    // ★ 추가: 창고 목록 비어있음 검증 (0건 조회 시 로그인 차단)
    if (Common.warehouseNames.isEmpty()) {
        Toast.makeText(LoginActivity.this,
            "등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요.",
            Toast.LENGTH_LONG).show();
        break;
    }
    if (("").equals(editID.getText().toString())) {
        editID.setError("아이디를 입력하세요.");
        break;
    }
    if (("").equals(editPWD.getText().toString())) {
        editPWD.setError("비밀번호를 입력하세요.");
        break;
    }
    user_id = editID.getText().toString();
    user_pwd = editPWD.getText().toString();
    new ProgressDlgLogin(LoginActivity.this).execute();
    break;
```

**검증**: 창고 목록 있을 때 기존 ID/PWD 검증 + 로그인 흐름이 그대로 동작함 확인.

---

## 5. 사이드이펙트

### 5.1 ProgressDlgShipSearch.java — selectWarehouseCode 참조 (118-165줄)

```java
// ProgressDlgShipSearch.java:118-119 (searchType=0 이마트 등)
if (!Common.selectWarehouseCode.isEmpty()) {
    data += " AND D.창고코드 = '" + Common.selectWarehouseCode + "'";
}
// 동일 패턴이 searchType별로 4개 위치(118, 135, 144, 164줄)에 존재
```

**영향 분석**:
- `Common.selectWarehouseCode`의 기본값은 `""` (Common.java:47)
- 창고 목록 0건 시 fallback 제거 후 `selectWarehouseCode`는 `""` 상태 유지
- `!isEmpty()` 가드가 이미 존재하므로, 빈 문자열일 때 WHERE 조건이 추가되지 않음 → 컴파일/런타임 오류 없음
- **단**, 창고 조건 없이 출하 조회가 실행될 위험이 있으므로, onClick에서 창고 목록 차단(Step 2)이 반드시 함께 적용되어야 한다

**대응 방안**: Step 2(로그인 차단)가 적용되면, 창고 목록 0건 상태에서 로그인 자체가 불가능하므로 ProgressDlgShipSearch 진입 불가. 추가 수정 불필요.

### 5.2 Common.selectWarehouse / selectWarehouseCode — 정상 흐름 영향 없음

- 데이터 있을 때: Spinner onItemSelected가 정상 발동 → 기존 동일
- 데이터 없을 때: fallback 제거 후 `""` 상태 유지 → onClick에서 차단되므로 하위 로직 미진입

---

## 6. 데이터 저장 구조

### 변수 매핑

| 변수 | 타입 | 위치 | 용도 | 변경 전 | 변경 후 |
|------|------|------|------|---------|---------|
| `Common.warehouseNames` | ArrayList\<String\> | Common.java:50 | Spinner 표시용 창고명 목록 | 0건 시 "삼일냉장" 주입 | 0건 시 빈 상태 유지 |
| `Common.warehouseCodes` | ArrayList\<String\> | Common.java:51 | 쿼리 조건용 창고코드 목록 | 0건 시 "1" 주입 | 0건 시 빈 상태 유지 |
| `Common.selectWarehouse` | String | Common.java:46 | 선택된 창고명 (로그 표시) | fallback 시 "삼일냉장" | 빈문자열("") |
| `Common.selectWarehouseCode` | String | Common.java:47 | 선택된 창고코드 (WHERE 조건) | fallback 시 "1" | 빈문자열("") |

### 인덱스 매핑 (Spinner ↔ warehouseNames/warehouseCodes)

```
warehouseNames[0] = "창고명A"  ↔  warehouseCodes[0] = "창고코드A"
warehouseNames[1] = "창고명B"  ↔  warehouseCodes[1] = "창고코드B"
    ...
Spinner position → warehouseNames[position], warehouseCodes[position]
(데이터 있을 때만 — 0건 시 Spinner 비어있음, onItemSelected 미발동)
```

---

## 7. 호출 시점

```
[앱 시작]
    ↓
LoginActivity.onCreate()
    ├── DBHandler 초기화 (SQLite Table Create)
    ├── SharedPreferences 읽기 (프린터 설정)
    ├── EditText 초기화 (ID/PWD 기본값)
    └── ★ new WarehouseSearchTask().execute()  ← 창고 목록 비동기 조회
              ↓ doInBackground (백그라운드)
          search_warehouse.jsp POST 호출
          WHERE 회사코드 = '20' AND PDA여부 = '1'
              ↓ onPostExecute (UI 스레드)
          [데이터 있음]
              → Spinner 구성 + onItemSelected 등록
              → 사용자가 창고 선택 → selectWarehouse/Code 설정
          [0건 — 변경 후]
              → AlertDialog 안내 표시
              → Spinner 비어있음 유지, selectWarehouse/Code = ""
              ↓
[사용자 로그인 버튼 클릭]
    onClick(btnLogin)
        [창고 목록 비어있음 — 변경 후 추가]
            → Toast 안내 + break (로그인 차단)
        [정상]
            → ID/PWD 검증 → ProgressDlgLogin → MainActivity
```

---

## 8. 개발 플랜

### Step 1: onPostExecute — fallback 제거 + 0건 안내 AlertDialog 추가

**Part 1. 분석**
- 메서드: `WarehouseSearchTask.onPostExecute(String)`
- 범위: `LoginActivity.java:107-152`
- 용도: 창고 조회 결과 처리 — Spinner 구성 및 fallback 제거
- 주의할 점:
  - 127-130줄의 fallback 블록만 제거. 나머지 파싱 로직(114-124줄), Spinner 구성(132-151줄)은 변경하지 않는다
  - AlertDialog는 `LoginActivity.this` 컨텍스트로 생성한다 (inner class에서 접근 가능)
  - 0건 분기에서 `return`으로 Spinner 구성 코드 진입을 막는다 — `Common.warehouseNames`가 비어있는 상태에서 adapter가 구성되면 Spinner에 빈 목록이 표시되지만, `return` 처리가 더 명확하다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | fallback 제거 | LoginActivity.java:127-130 | if (warehouseNames.isEmpty()) { add("삼일냉장"); add("1"); } 블록 전체 삭제 |
| 2 | 0건 AlertDialog 추가 | LoginActivity.java:127 위치 | isEmpty() 시 AlertDialog 표시 후 return |
| 3 | Spinner 구성 범위 확인 | LoginActivity.java:132-151 | 데이터 있을 때만 도달하므로 변경 없이 유지 |

**Part 2. 변환 계획**
- 변환 방식: 127-130줄 fallback 블록을 AlertDialog 안내 + return 으로 교체
- AlertDialog 메시지: "등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요."
- AlertDialog 타이틀: "창고 목록 없음"
- 버튼: "확인" 버튼 1개 (setPositiveButton, null listener)
- 주의사항:
  - AlertDialog를 show()한 후 반드시 `return`을 추가하여 이하 Spinner 구성 코드가 실행되지 않게 할 것
  - `Common.warehouseNames`, `Common.warehouseCodes`는 clear() 상태(빈 ArrayList) 그대로 유지 — 추가 조작 없음
  - Spinner 구성 코드(132-151줄)는 수정하지 않음

**체크리스트**
- [x] Part 1: 127-130줄 fallback 코드 위치 확인
- [x] Part 2: AlertDialog 구성 계획 확인
- [x] Part 3: fallback 제거 + AlertDialog + return 코드 작성
- [x] Part 4: 컴파일 오류 없음 확인 (code-verifier: import/문맥/return 정상)
- [ ] Part 5: 단위테스트 — 0건 시 AlertDialog 표시 (실기기 테스트 대기)
- [x] Part 6: 회귀테스트 — 기존 Spinner 구성 로직 영향 없음 (code-verifier·original-comparator 통과)

**Part 6. 변경 내용**:
- **무엇을**: onPostExecute 0건 fallback("삼일냉장"/"1") 제거 → AlertDialog 안내 + return
- **왜**: 하드코딩 fallback이 조회 0건(데이터 미설정)을 은폐하고 잘못된 창고코드"1"로 로그인되게 함
- **어떻게**: `if(warehouseNames.isEmpty())` 블록을 AlertDialog("창고 목록 없음"/C0114 안내).show() + return으로 교체. 정상 데이터 경로(Spinner 구성)는 무변경

---

### Step 2: onClick(btnLogin) — 창고 목록 비어있음 검증 추가 (로그인 차단)

**Part 1. 분석**
- 메서드: `LoginActivity.onClick(View)`
- 범위: `LoginActivity.java:156-178` — case R.id.btnLogin 분기 (158-173줄)
- 용도: 창고 목록 0건 상태에서 로그인 버튼이 눌렸을 때 차단
- 주의할 점:
  - 검증 순서: 창고 목록 비어있음 → ID 빈값 → PWD 빈값 → 로그인 실행 (창고 검증이 최우선)
  - 기존 ID/PWD 검증 로직은 변경하지 않는다
  - Spinner에 빈 상태일 때 onItemSelected가 미발동하여 `Common.selectWarehouseCode = ""`임을 전제로 한다. `Common.warehouseNames.isEmpty()`가 더 명확한 조건이므로 이를 사용한다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 창고 목록 검증 위치 | LoginActivity.java:159 (btnLogin case 진입 직후) | Common.warehouseNames.isEmpty() 조건 추가 |
| 2 | 차단 메시지 | Toast (LONG) | "등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요." |
| 3 | 기존 ID/PWD 검증 | LoginActivity.java:160-167 | 변경 없이 유지 |

**Part 2. 변환 계획**
- 변환 방식: case R.id.btnLogin 진입 직후, 기존 editID 검증 이전에 warehouseNames 검증 블록 삽입
- Toast 방식 선택 이유: Step 1에서 이미 AlertDialog로 안내했으므로, 버튼 클릭 시점에는 Toast로 간결하게 재안내
- 주의사항:
  - `break`로 switch 탈출 — `return`이 아닌 `break`를 사용해야 switch-case 구조 유지
  - `LoginActivity.this` 컨텍스트 사용 (Toast)

**체크리스트**
- [x] Part 1: onClick(btnLogin) 분기 위치(158-173줄) 확인
- [x] Part 2: 검증 순서 및 Toast 메시지 계획 확인
- [x] Part 3: 창고 목록 검증 코드 삽입
- [x] Part 4: 컴파일 오류 없음 확인 (code-verifier: Toast import·break 구조 정상)
- [ ] Part 5: 단위테스트 — 창고 목록 없을 때 Toast + 로그인 미진행 (실기기 테스트 대기)
- [x] Part 6: 회귀테스트 — 기존 ID/PWD 검증 영향 없음 (code-verifier 통과)

**Part 6. 변경 내용**:
- **무엇을**: onClick(btnLogin) 진입 직후 `warehouseNames.isEmpty()` 검증(Toast + break) 추가
- **왜**: 창고 0건 상태에서 잘못된 창고코드로 로그인되어 하위 출하/계근이 틀어지는 것을 차단
- **어떻게**: 기존 ID/PWD 검증 이전에 최우선으로 창고 목록 비어있음 검사 삽입, 비면 Toast 안내 후 break

---

### Step 3: 통합 테스트

| # | 테스트 | 시나리오 | 확인 |
|:-:|--------|---------|------|
| 1 | [정상] DB에 PDA여부='1' 창고 있을 때 | 앱 실행 → LoginActivity → Spinner에 실제 창고명 표시 → 창고 선택 → ID/PWD 입력 → 로그인 성공 → MainActivity 진입 | □ |
| 2 | [정상] Spinner 창고 선택 변경 | 창고 Spinner에서 다른 창고 선택 → selectWarehouse/Code가 변경된 창고명/코드로 갱신됨 | □ |
| 3 | [차단] DB에 PDA여부='1' 창고 없을 때 — AlertDialog | 앱 실행 → LoginActivity → WarehouseSearchTask 0건 반환 → AlertDialog "등록된 PDA 사용 창고가 없습니다..." 표시 | □ |
| 4 | [차단] 창고 목록 없을 때 로그인 버튼 클릭 | AlertDialog 확인 후 → ID/PWD 입력 → 로그인 버튼 클릭 → Toast "등록된 PDA 사용 창고가 없습니다..." 표시 → 로그인 미진행 | □ |
| 5 | [차단] "삼일냉장"/"1" 미노출 확인 | 창고 조회 0건 시 Spinner에 "삼일냉장"이 표시되지 않음 | □ |
| 6 | [사이드이펙트] ProgressDlgShipSearch 창고코드 조건 | 정상 로그인 후 출하대상 조회 → Common.selectWarehouseCode가 실제 창고코드로 설정되어 WHERE 조건에 포함됨 | □ |

---

### 개발 순서 요약

```
Step 1: onPostExecute fallback 제거 + 0건 AlertDialog
    ↓
Step 2: onClick(btnLogin) 창고 목록 비어있음 검증 + Toast 차단
    ↓
Step 3: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 정상 창고 데이터 있을 때

```
1. ERP C0114에서 회사코드='20' 창고 1개 이상 PDA여부='1' 설정
2. PDA 앱 실행 → LoginActivity 진입
3. WarehouseSearchTask 실행 → search_warehouse.jsp 응답 1건 이상
4. Spinner에 실제 창고명이 표시됨 확인
5. Spinner에서 창고 선택 → Log에 "창고 선택: xxx (코드)" 출력 확인
6. ID/PWD 입력 후 로그인 버튼 클릭
7. ProgressDlgLogin 실행 → 로그인 성공 → MainActivity 진입
8. MainActivity에서 출하대상 조회 시 Common.selectWarehouseCode가 선택 창고코드와 일치 확인
```

### 시나리오 2: 창고 데이터 없을 때 (0건)

```
1. ERP C0114에서 회사코드='20' 창고 PDA여부 모두 '0' 상태 (또는 등록 없음)
2. PDA 앱 실행 → LoginActivity 진입
3. WarehouseSearchTask 실행 → search_warehouse.jsp 응답 0건
4. AlertDialog 표시 확인:
   - 타이틀: "창고 목록 없음"
   - 메시지: "등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요."
5. "확인" 버튼 클릭
6. Spinner가 비어있음(항목 없음) 확인 — "삼일냉장" 미노출
7. ID/PWD 입력 후 로그인 버튼 클릭
8. Toast "등록된 PDA 사용 창고가 없습니다..." 표시 확인
9. MainActivity로 이동하지 않음 (로그인 차단) 확인
10. Common.selectWarehouseCode = "" 상태 유지 확인 (Log 확인)
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | AlertDialog 표시 후 앱이 응답 없음처럼 보임 | 창고 조회 실패 상태에서 UI가 멈춘 것처럼 보일 수 있음 | AlertDialog에 명확한 안내 메시지 + "확인" 버튼으로 사용자 액션 유도. ProgressDialog가 있다면 먼저 닫힌 상태여야 함 (WarehouseSearchTask는 ProgressDialog 없이 백그라운드 실행하므로 해당 없음) |
| 2 | 로그인 후 WarehouseSearchTask 재조회 없음 | AlertDialog 확인 후 앱 사용 불가, 재시도 방법이 없음 | 운영 전 ERP C0114 설정이 전제. 앱 재시작이 재시도 방법. 필요 시 AlertDialog에 "재조회" 버튼 추가 검토 (현재 step 범위 밖) |
| 3 | Spinner가 완전히 비어 있어 UI가 어색함 | 빈 Spinner가 화면에 표시됨 | AlertDialog가 먼저 표시되므로 사용자는 이미 안내를 받은 상태. 별도 Spinner 숨김 처리는 현재 step 범위 밖 |
| 4 | 네트워크 오류와 DB 미설정을 동일하게 처리 | doInBackground 예외 시 "" 반환 → 0건과 동일 분기 | 현재 설계상 두 경우 모두 동일 AlertDialog 표시. 네트워크 오류 별도 분기는 현재 step 범위 밖 |
| 5 | 기존 테스트용 하드코딩 editID/PWD ("12345678"/"5411")와 충돌 | 창고 미설정 시 ID/PWD가 입력되어 있어도 로그인 차단됨 | 의도된 동작. 창고 설정 없이 로그인 차단이 목표이므로 정상 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | onPostExecute fallback 제거 + 0건 AlertDialog 추가 | ✅ 완료 (코드+검증) |
| 2 | onClick(btnLogin) 창고 목록 비어있음 검증 + Toast 차단 | ✅ 완료 (코드+검증) |
| 3 | 통합 테스트 | ⏳ 실기기 테스트 대기 |

---

**문서 버전**: 1.0

## 관련 문서
- `app/doc/오류/24_PDA_로그인_창고Spinner_하드코딩_삼일냉장_노출.md`
- `app/doc/참고자료/오류패턴_분석.md` — 패턴 F (하드코딩 잔존)
- `app/src/main/java/com/rgbsolution/highland_emart/LoginActivity.java`
- `app/src/main/java/com/rgbsolution/highland_emart/common/Common.java`
- `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`
- `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_warehouse.jsp`
