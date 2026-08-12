# LoginActivity 창고조회 실패원인구분 안내문구 [29 창고목록조회 원인불일치 안내문구]

**작성일**: 2026-07-21
**목적**: LoginActivity의 창고 목록 조회 결과가 0건일 때, "통신/서버 오류로 인한 실패"와 "정상 응답이지만 실제 데이터 0건"을 구분하여 원인에 맞는 안내 문구를 표시한다. 로그인 차단(0건 시 로그인 불가) 로직 자체는 정상 동작이므로 변경하지 않는다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### LoginActivity — WarehouseSearchTask (88~157줄)

```java
// LoginActivity.java:94-105 (doInBackground)
class WarehouseSearchTask extends AsyncTask<Void, Void, String> {
    @Override
    protected String doInBackground(Void... params) {
        try {
            String data = " WHERE 회사코드 = '" + Common.selectCompanyCode + "'";
            return HttpHelper.getInstance().sendDataDb(data, "inno",
                "search_warehouse", Common.URL_SEARCH_WAREHOUSE);
        } catch (Exception e) {
            Log.e(TAG, "창고 목록 조회 실패: " + e.getMessage());
            return "";   // ★ 이 catch는 실질적으로 거의 타지 않음(아래 참고)
        }
    }

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

        // 조회 0건: 안내 다이얼로그 표시, Spinner는 빈 상태 유지
        if (Common.warehouseNames.isEmpty()) {
            new AlertDialog.Builder(LoginActivity.this)
                .setTitle("창고 목록 없음")
                .setMessage("등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요.")
                .setPositiveButton("확인", null)
                .show();
            return;  // Spinner 구성 생략 — selectWarehouse/Code는 "" 상태 유지
        }

        // Spinner 구성 (이하 변경 없음)
        ...
    }
}
```

- `Common.warehouseNames.isEmpty()` 단 하나의 조건으로만 "0건"을 판단하며, 그 원인이 (A) 정상 응답이나 실제 창고 데이터 0건인지, (B) 서버 미기동/연결 실패/DB 연결 실패인지를 전혀 구분하지 않는다.
- 두 경우 모두 동일하게 "등록된 PDA 사용 창고가 없습니다. ERP 창고관리(C0114)에서 PDA여부를 설정하세요." 문구가 표시된다.

### HttpHelper.sendDataDb — 실패를 예외로 던지지 않고 문자열로 흡수 (99-101줄)

```java
// HttpHelper.java:90-102
public String sendDataDb(String data, String dbid, String type, String url) throws Exception {
    try {
        HttpPost request = makeHttpPostDb(data, dbid, type, url);
        HttpClient client = new DefaultHttpClient();
        ResponseHandler<String> reshandler = new BasicResponseHandler();
        String result = client.execute(request, reshandler);
        return result;
    } catch (Exception e) {
        return e.getMessage().toString();   // ★ 서버 미기동/연결 실패 시 "Connection refused..." 등 비어있지 않은 문자열 반환
    }
}
```

- `HttpHelper.sendDataDb`가 통신 예외를 **삼켜서 에러 메시지 문자열로 반환**하므로, `WarehouseSearchTask.doInBackground`의 `catch (Exception e)`는 실질적으로 거의 발동하지 않는다. 즉 서버 미기동/연결 실패 시 `receiveData`는 빈 문자열("")이 아니라 **"Connection refused..." 같은 비어있지 않은 에러 문자열**이 되어 `onPostExecute`에 전달된다.

### search_warehouse.jsp — DB 연결 실패/조회 0건 응답 형태 (25-30, 50-56줄)

```jsp
<%-- search_warehouse.jsp:20-30 --%>
try {
    conn = getMSSQLConnection();
    if(conn != null) { connection = true; }
} catch (Exception e) {
    connection = false;
    System.out.println("DB 연결 실패");
    out.println(e.getMessage().toString());   // ★ DB 연결 실패 시 비어있지 않은 에러 메시지 출력 (이후 NPE로 500 응답)
    e.printStackTrace();
}
```

```jsp
<%-- search_warehouse.jsp:50-56 --%>
while(rs.next()) {
    out.println(
        rs.getString("COMPANY_CODE") + "::" +
        rs.getString("WAREHOUSE_CODE") + "::" +
        rs.getString("WAREHOUSE_NAME") + ";;"
        );
    }
```

- 데이터가 **정상 조회되나 0건**이면 `while(rs.next())` 루프가 한 번도 실행되지 않아 응답 본문은 공백/개행만 남는다 → 앱이 `replace("\r\n","").replace("\n","")` 처리하면 **완전한 빈 문자열("")**이 된다.
- **서버 미기동**(HTTP 연결 자체 실패)이면 `HttpHelper.sendDataDb`의 catch가 `e.getMessage()`(예: `Connection refused`)를 반환 → **비어있지 않은 문자열**.
- **DB 연결 실패**(서버는 떠 있으나 MSSQL 연결 안 됨)면 JSP가 `out.println(e.getMessage())`로 에러 메시지를 출력 → **비어있지 않은 문자열**.
- 결론: **"파싱 후 창고 0건 + (개행 제거 후) receiveData가 완전히 비어있음" → 진짜 데이터 0건**, **"창고 0건 + receiveData가 비어있지 않음" → 통신/서버/DB 연결 오류**로 구분 가능하다.

### 문제점

- `onPostExecute`가 `Common.warehouseNames.isEmpty()`만으로 0건을 판단하여, receiveData 자체가 비어있는지(진짜 0건) 아니면 에러 메시지가 담겨 있는지(연결 실패)를 구분하지 않는다.
- 그 결과 서버 미기동 상황에서도 "ERP 창고관리(C0114)에서 PDA여부를 설정하세요"라는, 실제 원인과 무관한 안내가 표시된다.
- 사용자가 잘못된 원인(ERP 설정)을 의심하여 불필요한 조치를 시도하게 되고, 정작 필요한 조치(서버 상태 확인)는 안내되지 않는다.
- **로그인 차단(Spinner 미구성 + return, 로그인 시 Toast 후 진행 차단) 자체는 정상 설계이며 본 문서의 수정 대상이 아니다.** 창고 없이 작업을 진행시키는 것이 오히려 더 큰 문제이므로 차단 로직은 그대로 유지한다.

---

## 2. 변경 구조

### 데이터 흐름 — 변경 전

```
onCreate → WarehouseSearchTask.execute()
    ↓
doInBackground → HttpHelper.sendDataDb (예외를 문자열로 흡수)
    ↓
onPostExecute(receiveData)
    ├── 개행 제거 → 파싱 → warehouseNames 채움
    └── warehouseNames.isEmpty() ?
          └── [YES] → 항상 동일한 "ERP 창고관리 설정" AlertDialog 표시 + return
                (진짜 0건이든 연결 실패든 원인 구분 없이 동일 문구) ★ 문제
```

### 데이터 흐름 — 변경 후

```
onCreate → WarehouseSearchTask.execute()
    ↓
doInBackground → HttpHelper.sendDataDb (변경 없음)
    ↓
onPostExecute(receiveData)
    ├── raw receiveData(개행 제거 전 원문) 보관
    ├── 개행 제거 → 파싱 → warehouseNames 채움 (기존 로직 100% 유지)
    └── warehouseNames.isEmpty() ?
          ├── [YES] → 개행 제거된 receiveData.trim().isEmpty() ?
          │     ├── [YES] → 기존 "ERP 창고관리(C0114) 설정" AlertDialog (진짜 0건, 문구 변경 없음)
          │     └── [NO]  → ★ 신규 "서버 연결 실패" AlertDialog
          │                  "서버에 연결할 수 없습니다.\n서버 상태와 네트워크를 확인 후 다시 시도하세요."
          │                  (Step 2에서 "재시도" 버튼 추가 여부 결정)
          └── [NO] → Spinner 구성 (기존 로직 100% 유지)
```

### 원본 대비 정당성

`WarehouseSearchTask` 자체가 원본(`PDA-INNO(원본)`)에는 존재하지 않는다. 원본은 창고를 `{"부산센터","이천1센터","삼일냉장","SWC","탑로지스"}` 하드코딩 배열로 Spinner에 띄웠으며(오류 24 참조), 서버 조회 개념 자체가 없었다. 즉 본 문서에서 다루는 "0건 안내 문구" 로직은 오류 24 수정(개발/53, 하드코딩 fallback 제거) 시 전환 과정에서 신규 도입된 코드다. 따라서 원인별 안내 문구를 분리하는 이번 수정은 "원본 100% 동일" 원칙에 저촉되지 않는다. **단, 로그인 차단 로직 및 데이터 있을 때의 Spinner 구성 동작은 100% 유지된다.**

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **LoginActivity.java** | `app/src/main/java/com/rgbsolution/highland_emart/LoginActivity.java` `onPostExecute` (108~134줄) | receiveData 비어있음 여부로 "연결 실패"/"진짜 0건" 분기하여 안내 문구 분리 |
| 2 | **LoginActivity.java** | 위 동일 파일 `onPostExecute` (108~134줄, Step 2 옵션) | 연결 실패 AlertDialog에 "재시도" 버튼 추가(WarehouseSearchTask 재실행) |

> `HttpHelper.java`, `search_warehouse.jsp`는 이번 수정 대상이 아니다(공용 코드/외부 프로젝트 파일이며, 원인 판별에 필요한 정보는 이미 receiveData 값에 담겨 전달되고 있음이 확인됨).

---

## 4. 수정 상세

### 4.1 LoginActivity.java — onPostExecute 원인 분기 (최소 변경안, Step 1)

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/LoginActivity.java`

**변경 전 (108~134줄):**

```java
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

    // 조회 0건: 안내 다이얼로그 표시, Spinner는 빈 상태 유지
    if (Common.warehouseNames.isEmpty()) {
        new AlertDialog.Builder(LoginActivity.this)
            .setTitle("창고 목록 없음")
            .setMessage("등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요.")
            .setPositiveButton("확인", null)
            .show();
        return;  // Spinner 구성 생략 — selectWarehouse/Code는 "" 상태 유지
    }

    // Spinner 구성 (이하 변경 없음)
    ...
}
```

**변경 후 (제안, Step 1 — 문구 분기만):**

```java
@Override
protected void onPostExecute(String receiveData) {
    String normalized = (receiveData == null) ? "" : receiveData.replace("\r\n", "").replace("\n", "");

    Common.warehouseNames.clear();
    Common.warehouseCodes.clear();

    if (!normalized.isEmpty()) {
        String[] rows = normalized.split(";;");
        for (String row : rows) {
            if (row.isEmpty()) continue;
            String[] cols = row.split("::", -1);
            if (cols.length >= 3) {
                Common.warehouseCodes.add(cols[1].trim());  // 창고코드
                Common.warehouseNames.add(cols[2].trim());  // 창고명
            }
        }
    }

    // 조회 0건: 원인(연결 실패 vs 실제 0건)에 따라 안내 문구 분리
    if (Common.warehouseNames.isEmpty()) {
        if (normalized.trim().isEmpty()) {
            // 정상 응답이나 실제 창고 데이터 0건 — 기존 문구 유지
            new AlertDialog.Builder(LoginActivity.this)
                .setTitle("창고 목록 없음")
                .setMessage("등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요.")
                .setPositiveButton("확인", null)
                .show();
        } else {
            // 응답은 왔으나 비어있지 않음 = 통신/서버/DB 연결 오류 메시지가 담겨 있음
            Log.e(TAG, "창고 목록 조회 오류 응답: " + normalized);
            new AlertDialog.Builder(LoginActivity.this)
                .setTitle("창고 목록 조회 실패")
                .setMessage("서버에 연결할 수 없습니다.\n서버 상태와 네트워크를 확인 후 다시 시도하세요.")
                .setPositiveButton("확인", null)
                .show();
        }
        return;  // Spinner 구성 생략 — selectWarehouse/Code는 "" 상태 유지 (기존과 동일)
    }

    // Spinner 구성 (이하 변경 없음)
    ...
}
```

**검증**: 데이터 있을 때 기존 파싱/Spinner 구성 로직이 100% 동일하게 실행됨을 확인. 진짜 0건일 때 기존 문구가 그대로 유지됨을 확인. 연결 실패일 때만 신규 문구로 분기됨을 확인.

---

### 4.2 LoginActivity.java — onPostExecute 재시도 버튼 추가 (옵션안, Step 2)

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/LoginActivity.java`

**변경 전 (Step 1 적용 후, 연결 오류 분기):**

```java
} else {
    Log.e(TAG, "창고 목록 조회 오류 응답: " + normalized);
    new AlertDialog.Builder(LoginActivity.this)
        .setTitle("창고 목록 조회 실패")
        .setMessage("서버에 연결할 수 없습니다.\n서버 상태와 네트워크를 확인 후 다시 시도하세요.")
        .setPositiveButton("확인", null)
        .show();
}
```

**변경 후 (제안, Step 2 — 재시도 버튼 추가):**

```java
} else {
    Log.e(TAG, "창고 목록 조회 오류 응답: " + normalized);
    new AlertDialog.Builder(LoginActivity.this)
        .setTitle("창고 목록 조회 실패")
        .setMessage("서버에 연결할 수 없습니다.\n서버 상태와 네트워크를 확인 후 다시 시도하세요.")
        .setPositiveButton("재시도", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new WarehouseSearchTask().execute();  // 동일 조회 재실행
            }
        })
        .setNegativeButton("취소", null)
        .show();
}
```

**검증**: "재시도" 클릭 시 `WarehouseSearchTask`가 재실행되어 동일한 onPostExecute 분기를 다시 타는지 확인. 반복 실패 시에도 무한 루프(자동 재시도)가 아닌 사용자 클릭 트리거이므로 별도의 횟수 제한은 불필요함을 확인.

---

## 5. 사이드이펙트

### 5.1 onClick(btnLogin) — 로그인 차단 로직 (161~170줄)

```java
// LoginActivity.java:161-170
case R.id.btnLogin:
    // 창고 목록 비어있음 검증 (0건 조회 시 로그인 차단)
    if (Common.warehouseNames.isEmpty()) {
        Toast.makeText(LoginActivity.this,
            "등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요.",
            Toast.LENGTH_LONG).show();
        break;
    }
```

- 본 문서의 수정 범위는 `onPostExecute`의 AlertDialog 문구 분기뿐이며, `onClick(btnLogin)`의 Toast 문구는 변경하지 않는다(문서 범위 밖).
- 다만 참고: 연결 실패 케이스에서도 `warehouseNames`가 비어있는 상태는 동일하므로, 사용자가 로그인 버튼을 누르면 여전히 "ERP 창고관리..." Toast가 표시된다. 이는 이미 오류 29 범위 밖의 별건이며, 필요 시 별도 오류/개발 문서로 분리 검토(본 문서에서는 손대지 않음).

### 5.2 HttpHelper.java — 미변경 확인

- `sendDataDb`(90~102줄)는 앱 전역에서 공용으로 사용되는 통신 유틸리티이며, LoginActivity 외 다른 화면(출하대상 조회, 바코드 조회, 계근 등)도 동일 메서드를 사용한다. 본 문서는 LoginActivity 내부 분기만 추가하며 HttpHelper는 수정하지 않으므로 다른 화면에 영향이 없다.

### 5.3 오류 30(빈응답 split ArrayIndexOutOfBoundsException)과의 관계

- 오류 30은 `ProgressDlgShipSearch`/`ProgressDlgBarcodeSearch`에서 빈 응답 처리 시 발생하는 별건 문제이며, 뿌리는 동일하게 "빈 응답과 실패 응답을 구분하지 않는" 패턴이다. 본 문서는 LoginActivity의 창고 조회에 한정하며, 오류 30의 수정은 별도 개발 문서에서 다룬다(본 문서 범위 아님).

---

## 6. 데이터 저장 구조

### 변수 매핑

| 변수 | 타입 | 위치 | 용도 | 변경 전 | 변경 후 |
|------|------|------|------|---------|---------|
| `receiveData` (파라미터) | String | onPostExecute 인자 | 서버 원본 응답(개행 제거 전) | 즉시 개행 제거 후 덮어씀 | 원본 파라미터명은 유지하되, 개행 제거 결과를 `normalized` 변수로 분리 저장 |
| `normalized` | String (신규) | onPostExecute 지역변수 | 개행 제거된 응답 문자열 — 0건 원인 판별 기준 | 없음(기존엔 receiveData를 직접 재대입) | `normalized.trim().isEmpty()`로 진짜 0건/연결 오류 분기 |
| `Common.warehouseNames` | ArrayList\<String\> | Common.java | Spinner 표시용 창고명 목록 | 파싱 실패/0건 시 비어있음 | 변경 없음 |
| `Common.warehouseCodes` | ArrayList\<String\> | Common.java | 쿼리 조건용 창고코드 목록 | 파싱 실패/0건 시 비어있음 | 변경 없음 |

### 분기 판단 매핑

```
warehouseNames.isEmpty() == false          → Spinner 구성 (변경 없음)
warehouseNames.isEmpty() == true
    ├── normalized.trim().isEmpty() == true   → "ERP 창고관리(C0114) 설정" 안내 (기존 문구 유지)
    └── normalized.trim().isEmpty() == false  → "서버 연결 실패" 안내 (신규 문구, Step 2에서 재시도 버튼 추가 옵션)
```

---

## 7. 호출 시점

```
[앱 시작]
    ↓
LoginActivity.onCreate()
    └── new WarehouseSearchTask().execute()
              ↓ doInBackground (백그라운드)
          HttpHelper.sendDataDb("search_warehouse", ...)
              ├── [정상 응답 + 데이터 있음]        → "창고A::...;;창고B::...;;"
              ├── [정상 응답 + 데이터 0건]          → "" (빈 문자열, JSP 루프 미실행)
              └── [서버 미기동/DB연결실패/타임아웃] → "Connection refused..." 등 에러 메시지 문자열
              ↓ onPostExecute (UI 스레드)
          normalized = 개행 제거
          파싱 → warehouseNames/Codes 채움
              ├── [데이터 있음] → Spinner 구성 (변경 없음)
              └── [0건]
                    ├── normalized 비어있음    → ★ 기존 "ERP 창고관리 설정" AlertDialog
                    └── normalized 비어있지 않음 → ★ 신규 "서버 연결 실패" AlertDialog (Step 2: 재시도 버튼)
                          ↓
[사용자 로그인 버튼 클릭] — onClick(btnLogin)
    [창고 목록 비어있음] → Toast 안내 + break (기존 유지, 원인 불문 동일 차단)
```

---

## 8. 개발 플랜

### Step 1: onPostExecute — receiveData 비어있음 여부로 원인 분기(문구만 분리)

**Part 1. 분석**
- 메서드: `WarehouseSearchTask.onPostExecute(String)`
- 범위: `LoginActivity.java:108~134`
- 용도: 창고 0건 판정 시, 서버가 실제로 빈 응답(0건)을 준 것인지 통신/서버 오류 메시지를 준 것인지 구분하여 안내 문구를 분리한다
- 주의할 점:
  - 파싱 로직(114~124줄)과 Spinner 구성 로직(136줄 이하)은 절대 변경하지 않는다
  - 개행 제거 전 원본 `receiveData`를 그대로 유지하지 않고 `normalized` 지역변수로 옮겨 담아, 원인 판별에 사용한다(파라미터 재대입 자체는 기존과 동일 동작이나 변수명을 분리해 가독성 확보)
  - "0건"과 "연결 실패" 두 분기 모두 `return`으로 Spinner 구성 코드 진입을 막는 것은 기존과 동일하게 유지한다 — 로그인 차단 결과는 두 분기 모두 동일
  - `HttpHelper`, `search_warehouse.jsp`는 수정하지 않는다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | normalized 변수 도입 | LoginActivity.java:109 | receiveData 개행 제거 결과를 별도 변수로 저장 |
| 2 | null 방어 추가 | LoginActivity.java:109 | receiveData == null 대비 삼항 처리(HttpHelper가 null을 반환하는 경로는 현재 없으나 방어적으로 포함) |
| 3 | 0건 분기 세분화 | LoginActivity.java:126~134 | `normalized.trim().isEmpty()` 여부로 AlertDialog 문구 2종 분리 |
| 4 | 기존 0건 문구 유지 | LoginActivity.java:128~132 상당 | "ERP 창고관리(C0114)..." 문구/타이틀 100% 동일하게 유지 |
| 5 | 신규 연결 오류 문구 | 신규 else 분기 | "서버에 연결할 수 없습니다.\n서버 상태와 네트워크를 확인 후 다시 시도하세요." |

**Part 2. 변환 계획**
- 변환 방식: `if (Common.warehouseNames.isEmpty())` 블록 내부를 `if (normalized.trim().isEmpty()) {...} else {...}`로 세분화
- 주의사항:
  - `normalized`는 이미 `\r\n`, `\n` 제거가 끝난 상태이므로 `trim()`은 공백류(스페이스 등) 방어용으로만 추가
  - 로그(`Log.e`)로 연결 오류 원문(에러 메시지)을 남겨 추후 원인 추적이 가능하게 함
  - 두 분기 모두 `Common.warehouseNames`, `Common.warehouseCodes`는 `clear()` 상태 그대로 유지(추가 조작 없음)
  - AlertDialog 타이틀/버튼 구성은 기존 스타일(`setPositiveButton("확인", null)`)을 유지, Step 2 전까지는 재시도 버튼 없음

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행 (2026-07-21, 사용자 지시로 진행)
- [x] Part 4: 컴파일 확인 (`./gradlew compileDebugJavaWithJavac` → BUILD SUCCESSFUL)
- [ ] Part 5: 단위테스트 (실기기 재연결 후 — 시나리오 2·3 미수행)
- [ ] Part 6: 회귀테스트 (실기기 재연결 후 — 시나리오 1 미수행)

**Part 6. 변경 내용**:
- **무엇을**: `WarehouseSearchTask.onPostExecute`에서 개행 제거 결과를 `normalized` 지역변수로 분리하고, 창고 0건 분기를 `normalized.trim().isEmpty()` 여부로 "진짜 0건"과 "연결/서버 오류" 두 갈래로 세분화. 연결 오류 시 신규 문구("서버에 연결할 수 없습니다.\n서버 상태와 네트워크를 확인 후 다시 시도하세요.") + `Log.e` 원문 기록 추가.
- **왜**: 서버 미기동/연결 실패 시에도 "ERP 창고관리 설정" 문구가 표시되어 실제 원인(서버 상태)과 무관한 오안내를 하던 오류 29 해결. `HttpHelper`가 통신 예외를 `e.getMessage()` 문자열로 반환하고 JSP 0건은 빈 문자열이 되므로, receiveData가 비어있는지로 두 원인을 구분 가능함(검증 완료).
- **어떻게**: 파싱 로직·Spinner 구성·로그인 차단(`return`)은 100% 유지하고, 0건 `if` 블록 내부만 `if/else`로 분기. 기존 "ERP 설정" 문구는 진짜 0건 분기에서 문자 그대로 보존. `HttpHelper`·JSP는 미수정.

---

### Step 2 (옵션): AlertDialog에 "재시도" 버튼 추가

**Part 1. 분석**
- 메서드: `WarehouseSearchTask.onPostExecute(String)` — 연결 오류 분기 부분
- 범위: `LoginActivity.java` (Step 1 적용 후 신규 else 분기)
- 용도: 연결 실패 시 앱 강제 종료/재시작 없이 화면 내에서 재조회할 수 있는 수단 제공
- 주의할 점:
  - 사용자 클릭 트리거 방식만 사용하며, 자동 재시도/폴링은 도입하지 않는다(무한 루프 방지)
  - "재시도" 클릭 시 `new WarehouseSearchTask().execute()`를 그대로 재사용 — 새로운 통신 로직을 만들지 않는다
  - "취소" 버튼은 아무 동작 없이 다이얼로그만 닫음(Spinner는 계속 빈 상태, 로그인 차단 유지)

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 재시도 버튼 추가 | 연결 오류 AlertDialog | setPositiveButton("재시도", ...) → WarehouseSearchTask 재실행 |
| 2 | 취소 버튼 추가 | 연결 오류 AlertDialog | setNegativeButton("취소", null) |

**Part 2. 변환 계획**
- 변환 방식: Step 1의 `setPositiveButton("확인", null)`을 `setPositiveButton("재시도", OnClickListener)` + `setNegativeButton("취소", null)`로 교체(연결 오류 분기에만 적용, 0건 분기는 기존 "확인" 버튼 유지)
- 주의사항: "0건(ERP 설정 미비)" 분기는 재시도해도 서버 데이터가 바뀌지 않으므로 재시도 버튼을 넣지 않는다(연결 오류 분기에만 한정)

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행 (코드 수정은 사람 승인 게이트)
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 3: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 서버 정상 기동 + PDA여부='1' 창고 존재 → Spinner 실제 창고명 표시(변경 없음) | □ |
| 2 | 서버 정상 기동 + PDA여부='1' 창고 0건 → "ERP 창고관리(C0114) 설정" 문구(기존 문구 그대로) | □ |
| 3 | 서버 미기동(연결 거부) → "서버에 연결할 수 없습니다..." 신규 문구 표시, "ERP 설정" 문구 미표시 | □ |
| 4 | (Step 2 적용 시) 서버 미기동 상태에서 "재시도" 클릭 → 재조회 실행됨 확인 | □ |
| 5 | (Step 2 적용 시) 재시도 중 서버 기동 완료 → 재시도 클릭 시 정상 Spinner 구성으로 전환 | □ |
| 6 | 로그인 버튼(onClick btnLogin) 차단 동작 — 0건/연결오류 두 경우 모두 기존과 동일하게 로그인 차단 유지 | □ |

---

### 개발 순서 요약

```
Step 1: onPostExecute — receiveData 비어있음 여부로 원인 분기(문구 분리)
    ↓
Step 2(옵션): 연결 오류 AlertDialog에 재시도 버튼 추가
    ↓
Step 3: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 서버 정상 + 창고 데이터 0건 (기존 문구 유지 확인)

```
1. ERP C0114에서 회사코드='20' 창고 PDA여부 전부 '0' 상태로 둔다(또는 등록 없음)
2. 서버(weberp_dev)를 정상 기동 상태로 둔다
3. PDA 앱 실행 → LoginActivity 진입
4. WarehouseSearchTask 실행 → search_warehouse.jsp가 정상 응답하되 0건(공백 응답)
5. AlertDialog 확인: 타이틀 "창고 목록 없음", 메시지 "등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요." (기존과 동일)
6. Spinner가 비어있음 확인
```

### 시나리오 2: 서버 미기동 (신규 문구 확인)

```
1. weberp_dev 서버 프로세스를 중지시킨다
2. PDA 앱 실행 → LoginActivity 진입
3. WarehouseSearchTask 실행 → HttpHelper.sendDataDb가 연결 실패 메시지 문자열 반환
4. AlertDialog 확인: 타이틀 "창고 목록 조회 실패", 메시지 "서버에 연결할 수 없습니다.\n서버 상태와 네트워크를 확인 후 다시 시도하세요." (★ 기존 "ERP 창고관리..." 문구가 표시되지 않아야 함)
5. logcat에서 Log.e(TAG, "창고 목록 조회 오류 응답: ...") 출력 확인(연결 실패 원문 메시지 포함)
6. Spinner가 비어있음 확인, 로그인 버튼 클릭 시 Toast로 로그인 차단(기존 동작 유지) 확인
```

### 시나리오 3 (Step 2 적용 시): 재시도 버튼 동작 확인

```
1. weberp_dev 서버 프로세스를 중지시킨 상태에서 앱 실행 → "서버 연결 실패" AlertDialog 표시
2. 서버를 기동시킨다
3. AlertDialog의 "재시도" 버튼 클릭
4. WarehouseSearchTask가 재실행되어 정상 창고 목록이 조회됨 확인
5. Spinner에 실제 창고명이 표시됨 확인 → 정상 로그인 가능 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | JSP가 DB 연결 실패 시 `out.println(e.getMessage())` 이후 NPE로 500 에러를 던져, `HttpHelper`가 HTTP 상태코드 예외로 처리하며 응답 바디를 못 받을 가능성 | `search_warehouse.jsp:25-30`에서 conn이 null인 채로 이후 `stmt = conn.createStatement()`(35줄) 호출 시 NPE 발생 → 서블릿 컨테이너가 500 응답 | `BasicResponseHandler`는 상태코드 300 이상이면 `HttpResponseException`을 던지고, 이는 `sendDataDb`의 catch에서 `e.getMessage()`(예: "Internal Server Error")로 잡혀 비어있지 않은 문자열이 되므로 본 설계의 "비어있지 않음=오류" 분기로 정상 처리됨. 별도 대응 불필요 |
| 2 | 정상 0건 응답이 실제로는 순수 공백이 아니라 미세한 공백 문자(스페이스, 탭)를 포함할 가능성 | JSP `out.println` 특성상 극히 드물게 발생 가능 | `normalized.trim().isEmpty()`로 trim 처리하여 방어(Step 1에 이미 반영) |
| 3 | Step 2(재시도) 도입 시 사용자가 여러 번 빠르게 클릭하여 `WarehouseSearchTask`가 중복 실행될 가능성 | AsyncTask는 재사용 불가 객체이므로 매 클릭마다 `new WarehouseSearchTask()`로 새로 생성하지만, 클릭 여러 번 시 여러 요청이 동시에 나갈 수 있음 | 다이얼로그가 "확인"으로 닫힌 후에만 재클릭 가능한 구조(다이얼로그 1회성)이므로 동시 다중 실행 가능성은 낮음. 필요 시 재시도 버튼 클릭 후 다이얼로그 자동 dismiss(AlertDialog 기본 동작)로 중복 방지됨 |
| 4 | onClick(btnLogin)의 Toast 문구는 원인 불문 "ERP 창고관리..." 고정이라, 연결 오류로 차단된 사용자가 로그인 시도 시 다시 오원인 문구를 볼 수 있음 | onClick(btnLogin)은 본 문서 수정 범위 밖(오류 29는 onPostExecute의 최초 안내 문구 한정) | 본 문서에서는 손대지 않음. 필요 시 별도 오류/개발 문서로 분리하여 onClick(btnLogin)도 동일한 원인 분기를 적용할지 검토 |
| 5 | 원본과 다른 신규 AlertDialog 문구 추가가 "원본 100% 동일" 원칙 위반으로 오인될 가능성 | 원본에는 WarehouseSearchTask 자체가 없어 비교 대상이 존재하지 않음 | 2장 "원본 대비 정당성" 및 오류 24 문서를 근거로, 본 로직이 오류 24 수정 시 신규 도입된 코드임을 문서에 명시하여 원본 비교 대상이 아님을 명확히 함 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | onPostExecute — receiveData 비어있음 여부로 원인 분기(문구 분리) | 🔨 코드수정·컴파일 완료 (실기기 테스트 대기) |
| 2 | (옵션) 연결 오류 AlertDialog 재시도 버튼 추가 | ⏳ 대기 (사용자 지시 대기) |
| 3 | 통합 테스트 | ⏳ 대기 (실기기 재연결 후) |

---

**문서 버전**: 1.0

## 관련 문서
- `app/doc/오류/29_창고목록조회_원인불일치_안내문구[이마트_출하계근_EDA51실기기_자동테스트].md` — 본 개발 문서의 근거 오류
- `app/doc/오류/24_PDA_로그인_창고Spinner_하드코딩_삼일냉장_노출.md` — WarehouseSearchTask/0건 안내 로직 최초 도입 배경
- `app/doc/개발/53_LoginActivity_창고fallback제거[PDA_로그인_창고Spinner_하드코딩_삼일냉장_노출].md` — 하드코딩 fallback 제거 + 0건 안내/로그인 차단 도입 개발 문서(본 문서가 다루는 코드의 직전 변경 이력)
- `app/doc/오류/30_빈응답_split_ArrayIndexOutOfBoundsException_상시발생[이마트_출하계근_EDA51실기기_자동테스트].md` — 동일 뿌리(빈 응답/실패 응답 미구분) 패턴의 별건 오류. 본 문서는 LoginActivity 창고 조회에 한정하며 해당 오류의 수정은 포함하지 않음
- `app/doc/테스트/07_이마트_출하계근_EDA51실기기_자동테스트.md` — 오류 29 발견 테스트
- `app/doc/참고자료/오류패턴_분석.md` — 패턴 F: UI/로직 버그(조회 실패 원인 미구분), 패턴 G: 인코딩/파싱
- `app/src/main/java/com/rgbsolution/highland_emart/LoginActivity.java`
- `app/src/main/java/com/rgbsolution/highland_emart/common/HttpHelper.java`
- `app/src/main/java/com/rgbsolution/highland_emart/common/Common.java`
- `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_warehouse.jsp`
