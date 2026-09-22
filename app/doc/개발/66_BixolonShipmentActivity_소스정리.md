# BixolonShipmentActivity 소스 정리 (로직 영향 없음)

**작성일**: 2026-09-22
**목적**: `BixolonShipmentActivity.java`에서 **로직 영향이 없는 항목**부터 정리한다. Step 1은 Javadoc HTML 태그, Step 2는 미사용 상수다. **모든 Step 이 로직 변경 0건이다.**

> **번호 안내**: 기존 66·67(클래스 분리)은 2026-09-22 원복과 함께 폐기되어 이 번호를 재사용한다. 원복 경위는 `app/doc/일정/2026-09-22.md` 참조.

---

## 1. 배경

소스 정리 1단계로, **로직과 영향이 없는 항목부터** 진행한다.
원복된 원본 `BixolonShipmentActivity.java`(3,737줄)의 Javadoc이 HTML 태그(`<ul>`·`<li>`·`<h2>` 등)로 작성되어 있어, 태그 전용 줄과 빈 주석 줄이 다수 섞여 있었다.

---

## 2. 작업 범위

**대상 파일**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java` (단일 파일)

### 2.1 삭제 대상 — 내용이 없는 주석 줄

| 패턴 | 처리 |
|------|------|
| `* <ul>` / `* </ul>` | 줄 삭제 |
| `* <ol>` / `* </ol>` | 줄 삭제 |
| `* <p>` / `* </p>` | 줄 삭제 |
| `* <pre>` / `* </pre>` | 줄 삭제 |
| `*` (빈 주석 줄) | 줄 삭제 |

### 2.2 태그 제거 대상 — 내용이 있는 주석 줄

태그만 지우고 **본문 텍스트와 들여쓰기는 그대로 유지**한다.

| 태그 | 예시 (전 → 후) |
|------|----------------|
| `<li>` `</li>` | `*   <li>계근 작업: ...</li>` → `*   계근 작업: ...` |
| `<b>` `</b>` | `<b>출하 대상 조회</b>: ...` → `출하 대상 조회: ...` |
| `<h2>` `</h2>` | `* <h2>개요</h2>` → `* 개요` |
| `<h3>` `</h3>` | `* <h3>...</h3>` → `* ...` |

### 2.3 제외 대상

- **`<String>` 20건** — `ArrayList<String>` 등 **제네릭 타입(코드)**이므로 건드리지 않는다
- `//` 한 줄 주석 — 이번 범위 밖
- 주석 외 모든 코드

---

## 3. 처리 방식

정규식이 코드 줄을 건드리지 않도록, **`^\s*\*` 로 시작하는 주석 줄만** 처리 대상으로 한정했다.
(`*/` 종료 줄은 `(?!/)` 로 제외)

```
^(\s*)\*(?!/)(.*)$   ← 이 패턴에 맞는 줄만 처리
</?(?:ul|ol|li|p|pre|h2|h3|b)>   ← 제거 대상 태그 (String 등 제네릭 미포함)
```

---

## 4. 결과

| 항목 | 값 |
|------|---:|
| 삭제된 줄 | **131줄** |
| 태그만 제거된 줄 | **140줄** |
| 파일 줄수 | 3,737 → **3,606줄** (−131) |

### 4.1 태그 제거 내역

| 태그 | 건수 | 태그 | 건수 |
|------|---:|------|---:|
| `<li>` / `</li>` | 123 / 123 | `<ul>` / `</ul>` | 18 / 18 |
| `<p>` / `</p>` | 20 / 20 | `<h2>` / `</h2>` | 9 / 9 |
| `<ol>` / `</ol>` | 8 / 8 | `<h3>` / `</h3>` | 8 / 8 |
| `<b>` / `</b>` | 5 / 5 | `<pre>` / `</pre>` | 1 / 1 |
| 빈 `*` 줄 | 32 | | |

---

## 5. 검증

| # | 항목 | 결과 |
|:-:|------|:----:|
| 1 | 잔여 HTML 태그 확인 | ✅ `<String>` 20건(제네릭)만 남음 |
| 2 | **주석 외 변경 0건** — `git diff` 에서 `*` 로 시작하지 않는 변경 줄 검사 | ✅ 0건 |
| 3 | 빌드 (`compileDebugJavaWithJavac`) | ✅ 통과 |
| 4 | 로직 영향 | ✅ 없음 (주석 전용) |

**검증 2번 명령**
```
git diff --unified=0 <파일> | grep '^[-+]' | grep -v '^[-+][-+]' | grep -vE '^[-+]\s*\*'
→ 출력 0줄
```

---

## 6. Step 2 — 미사용 상수 제거

### 6.1 미사용 원인

15개 전부 **라벨 출력 전용 상수**이며, 라벨 로직이 `LabelPrintHelper`로 분리되면서 선언만 남았다.

```
ShipmentActivity (원형, 상수 실사용 중)
      │
      ├─ 6328f65 "허니웰 전용 scanneractivity, 빅솔론 전용 shipmentactivity 파일"
      │    → 파일 통째 복사로 BixolonShipmentActivity 생성 (상수도 함께 복사)
      │
      └─ LabelPrintHelper 분리
           → 라벨 조립 로직이 LabelPrintHelper 로 이동
           → 상수도 LabelPrintHelper 에 새로 선언 (55~56 등)
           → Activity 쪽 원본 상수만 삭제되지 않고 잔존   ← 정리 대상
```

**사용처 대조** (숫자 `1` = 자기 선언 줄 1개 = 실사용 0회)

| 상수 | Bixolon(현재) | LabelPrintHelper(이동처) | ShipmentActivity(원형) |
|------|:---:|:---:|:---:|
| `COMPANY_CODE` | **1** | 2 | 3 |
| `COMPANY_NAME` | **1** | 4 | 5 |
| `MEAT_CENTER_CODE` | **1** | 3 | 3 |
| `LOGIS_CODE_DEFAULT` | **1** | 3 | 3 |
| `BARCODE_TYPE_M0` | **1** | 5 | 5 |
| `BARCODE_TYPE_M1` | **1** | 3 | 3 |
| `BARCODE_TYPE_M3` | **1** | 10 | 11 |
| `BARCODE_TYPE_M4` | **1** | 10 | 11 |
| `BARCODE_TYPE_M8` | **1** | 3 | 3 |
| `BARCODE_TYPE_M9` | **1** | 2 | 10 |
| `BARCODE_TYPE_E0`~`E3` | **1** | 3 | 3 |
| `BARCODE_TYPE_P0` | **1** | 2 | 2 |

`BARCODE_TYPE_M9` 만 LabelPrintHelper 2회 / 원형 10회로 차이가 나는데, 개발43(비정량 이마트 M9 바코드 신규 추가)에서 M9 분기를 재구성한 결과다(`144819a`·`4a2593b`).

### 6.2 삭제 내역

| 구분 | 내용 | 줄수 |
|------|------|---:|
| `// 업체 정보 상수` 섹션 전체 | `COMPANY_CODE`·`COMPANY_NAME` + 섹션 주석 + 빈 줄 | 4 |
| `// 미트센터 관련 상수` 일부 | `MEAT_CENTER_CODE`·`LOGIS_CODE_DEFAULT` (2줄만) | 2 |
| `// 바코드 타입 상수` 섹션 전체 | `BARCODE_TYPE_M0`~`P0` 11개 + 섹션 주석 + 빈 줄 | 13 |
| **합계** | 선언 15 + 섹션 주석 2 + 빈 줄 2 | **19** |

### 6.3 유지한 상수 (사용 중)

`SEARCH_TYPE_*` 8개, `MEAT_CENTER_STORE_CODE`(3회), `KILKOY_PACKER_CODE`(3회), `LOTTE_BOX_ORDER_MAX`(5회), `ITEM_TYPE_*` 5개, `CENTER_NAME_*` 3개.
`// 미트센터 관련 상수` 섹션은 2개가 살아 있어 주석을 유지했다.

### 6.4 결과

| 항목 | 값 |
|------|---:|
| 파일 줄수 | 3,606 → **3,588줄** (−19, EOF 개행 포함 계산) |
| 삭제 상수 | 15개 (전부 `private`, 외부 참조 불가) |

### 6.5 검증

| # | 항목 | 결과 |
|:-:|------|:----:|
| 1 | 삭제 대상 15개 실사용 0회 재확인 | ✅ |
| 2 | Step 1 상태 대비 diff — **상수 제거분 외 변경 0건** | ✅ |
| 3 | 빌드 (`compileDebugJavaWithJavac`) | ✅ 통과 |
| 4 | 기능 영향 | ✅ 없음 (값은 `LabelPrintHelper` 가 자체 보유) |

> **작업 중 정정** — 1차 시도에서 섹션 삭제 후 빈 줄을 **파일 전역으로 축소**해 무관한 빈 줄 5곳(483·1954·2248·2581·2834)과 EOF 빈 줄이 함께 지워졌다. 요청 범위 밖이므로 되돌리고, **삭제 구간에 인접한 빈 줄만** 제거하도록 고쳐 다시 적용했다. 위 검증 2번이 이를 확인한다.

---

## 7. Step 3 — 미사용 import 제거

`import` 만 있고 본문 사용이 0회인 7개를 제거했다. 전체 import 를 전수 검사해 이 7개 외에는 없음을 확인했다.

| import | 본문 사용 |
|--------|:---:|
| `java.io.IOException` | 0 |
| `java.text.DecimalFormat` | 0 |
| `java.text.ParseException` | 0 |
| `java.text.SimpleDateFormat` | 0 |
| `java.util.Calendar` | 0 |
| `java.util.Date` | 0 |
| `java.util.Set` | 0 |

**유지**: `java.io.ByteArrayOutputStream`·`java.util.ArrayList`·`import static ...R.id.sp_center`(422 에서 사용) 및 나머지 전부.
**유지**: 43~47 의 Woosim import 제거 기록 주석 — Bixolon 전환 이력이므로 보존한다.

**결과**: 3,588 → 3,581줄 (−7). diff 상 import 7줄 외 변경 0건, 빌드 통과.

---

## 8. Step 4 — 클래스 Javadoc 내용 정정

주석 **내용**이 실제 코드와 다른 부분을 코드로 대조해 정정했다. 로직 변경 0건.

### 8.1 searchType 표 (6줄 → 8줄)

| searchType | 정정 전 | 정정 후 | 코드 근거 |
|:---:|------|------|------|
| 0 | 이마트 출하 | 이마트 출하 (유지) | `setPrinting` |
| 1 | 생산 투입 - **프린터 비활성화** | 생산 계근 - **라벨 인쇄 분기 없음** | 라벨 분기에 1 없음 |
| 2 | **누락** | 홈플러스 출하 - `setHomeplusPrinting` | 1881 |
| 3 | 도매 출하 | 도매 출하 + 라벨 분기 없음 명시 | 365 레이아웃, 라벨 분기 없음 |
| 4 | **홈플러스 출하** ❌ | **비정량 출하** - 이마트와 동일 `setPrinting` | 1887~1889 (로그 "이마트(비정량)") |
| 5 | **롯데 출하** ❌ | **홈플러스 비정량** - `setHomeplusPrinting` | 1881 (2 와 동일 분기) |
| 6 | **원앤원 출하** ❌ | **롯데 출하** - `setPrintingLotte`, 박스 순번 | 1890, 1797 |
| 7 | **누락** | 생산 라벨 - 미사용 (2026-08-04 제외 결정) | 상수 `@deprecated` |

### 8.2 그 외 정정 3건 + 삭제 1건

| 위치 | 정정 전 | 정정 후 |
|------|------|------|
| 클래스 Javadoc 첫 줄 | `ShipmentActivity - 출하 계근 작업 화면` | `BixolonShipmentActivity - ...` |
| 주요 기능 | 라벨 인쇄 ... **(Woosim 프린터)** | ... **(BIXOLON, SLCS 명령어)** |
| `mHandler` Javadoc | `MESSAGE_READ (3): ... Woosim 서비스로 전달` | `... Bixolon 은 별도 처리 불필요(no-op)` (839 와 일치) |
| `mHandler` Javadoc | `WoosimService.MESSAGE_PRINTER: Woosim 프린터 관련 메시지` | **삭제** (869 에서 제거 완료된 경로) |
| `ProgressDlgPrintConnect` Javadoc | `Woosim 블루투스 프린터에 연결한다.` | `BIXOLON 블루투스 프린터에 연결한다.` |

### 8.3 보존한 Woosim 표기

`// 원본: WoosimCmd...`, `// mWoosim 제거됨 - Bixolon SLCS 명령어로 대체` 등 **전환 이력 기록 주석은 손대지 않았다**(43~47·217·483·510·839·844·869·2321·3283·3294·3298). 프로젝트 목적이 Woosim→BIXOLON 전환이므로 대응 관계 기록이 필요하다.

**결과**: 3,581 → 3,582줄 (표 6→8줄 +2, 삭제 −1). 주석/import 외 변경 0건, 빌드 통과.

---

## 9. Step 5 — `BixolonShipmentActivity_back.java` 삭제

### 9.1 확인 사항

| 항목 | 결과 |
|------|------|
| 줄수 | 3,730줄 (`class BixolonShipmentActivity_back extends HoneywellScannerActivity`) |
| AndroidManifest 등록 | ❌ 없음 → 실행 불가 |
| 코드 참조 | 0건 |
| git 추적 이력 | **없음** (한 번도 커밋된 적 없는 로컬 파일) |
| `.gitignore` 대상 | ❌ 아님 (단순 미추적) |
| 빌드 포함 여부 | ⭕ `src/main/java` 하위라 **컴파일 대상** |

### 9.2 처리 방식 — 보존 후 삭제

`git hash-object` 로 얻은 blob 이 **git 이력 전체에 존재하지 않아** 그냥 지우면 복구가 불가능했다.
따라서 **① 보존 커밋 → ② 삭제 커밋** 2단계로 처리해 롤백 경로를 확보했다.

| 커밋 | 내용 |
|------|------|
| `d2e7ddf` | 삭제 전 보존 (파일을 이력에 등록) |
| 다음 커밋 | 삭제 |

복구가 필요하면 `git show d2e7ddf:app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity_back.java` 로 되살릴 수 있다.

### 9.3 결과

빌드 대상에서 3,730줄 제거. 잔존 참조 0건, 빌드 통과.

---

## 10. Step 6 — Woosim 잔재 주석 제거

`// 원본: import com.woosim...` 처럼 **정보가 없는 전환 잔재 주석 9줄**을 제거했다. git 이력과 원본 프로젝트(`D:\PDA\PDA-INNO(원본)`)에 남아 있어 소스에 둘 이유가 없다.

| 삭제 | 내용 | 줄수 |
|------|------|---:|
| 43~47 | Woosim import 제거 기록 | 5 |
| 219 / 512 / 845 | `// mWoosim 제거됨 - Bixolon SLCS 명령어로 대체` (맥락 없이 떠 있음) | 3 |
| 870 | `// WoosimService.MESSAGE_PRINTER 제거됨` (switch 끝) | 1 |

**유지한 Woosim 표기 6건**

| 위치 | 사유 |
|------|------|
| 479 `Bixolon 첫 사용 시 기존 Woosim MAC 주소 초기화` | **살아있는 로직** 설명 |
| 833 `case MESSAGE_READ:` 의 `// mWoosim.processRcvData 제거됨` | **빈 case 사유** — 지우면 이유 없이 비어 보임 |
| 2313 / 3275 / 3286 / 3290 `// 원본: WoosimCmd...` | SLCS↔Woosim **대응 관계** 기록. 전환 검증에 사용 |

**결과**: 3,582 → 3,573줄 (−9). 주석 외 변경 0건, 빌드 통과.

---

## 11. Step 7 — 업무 도메인 상수 `Common` 이동

### 11.1 배경

`Common.searchType`(값)은 `Common` 에 있는데, 그와 비교하는 **상수만 각 클래스에 흩어져** 있었다. 그 결과 같은 상수가 4개 파일에 중복 선언되고 **주석이 이미 어긋나 있었다**.

| 상수 | BixolonShipmentActivity | MainActivity |
|------|------|------|
| `SEARCH_TYPE_EMART` | `// 이마트 출하` | `// 출하대상` |
| `SEARCH_TYPE_HOMEPLUS` | `// 홈플러스 출하` | `// 홈플러스 하이퍼` |
| `SEARCH_TYPE_NONFIXED` | `// 도매 비정량` ❌ | `// 비정량 출하` ✅ |
| `SEARCH_TYPE_WHOLESALE` | `// 도매 출하` | `// 도매업체` |

현재 상태: **상수 4벌 중복 + 생리터럴 비교 23곳** 공존.

### 11.2 분류 기준

| 구분 | 상수 | 개수 | 처리 |
|------|------|---:|:---:|
| **업무 도메인** — 타 파일에도 중복 선언 | `SEARCH_TYPE_*` 8, `MEAT_CENTER_STORE_CODE`, `KILKOY_PACKER_CODE`, `LOTTE_BOX_ORDER_MAX`, `ITEM_TYPE_*` 5, `CENTER_NAME_*` 3 | **19** | `Common` 이동 |
| **Activity 기구** — 클래스 밖에서 의미 없음 | `REQUEST_*` 3, `MESSAGE_*` 5, `GET_DATA_REQUEST`, `DEVICE_NAME`, `TOAST`, `BARCODE_PROCESS_DEBOUNCE_MS` | 12 | 유지 |

`public` 상수 12개는 **외부 참조 0건**, `Common` 에 이름 충돌 0건을 사전 확인했다.

### 11.3 작업 내용

1. `Common.java` 의 `searchType` 선언 바로 아래에 `public static final` 19개 추가 (+32줄)
   - `SEARCH_TYPE_NONFIXED` 주석은 Step 4 판정대로 **`// 비정량 출하`** 로 확정
   - `SEARCH_TYPE_PRODUCTION_LABEL` 의 `@deprecated` Javadoc 도 함께 이동
2. Activity 의 `private static final` 19개 삭제 + 빈 섹션 주석 6개 + 인접 빈 줄 5개 삭제
3. 참조 **76곳**을 `Common.<상수>` 로 치환

### 11.4 검증

| # | 항목 | 결과 |
|:-:|------|:----:|
| 1 | 잔존 선언 | ✅ 0건 |
| 2 | `Common.Common.` 중복 접두 | ✅ 0건 |
| 3 | **문자열 리터럴 오염** — 전/후 리터럴 전수 비교 | ✅ 삭제된 선언의 값 18개(`"0"`~`"E/T"`)만 감소, 나머지 736개 **완전 동일** |
| 4 | 빌드 (`--rerun-tasks` 캐시 배제) | ✅ 통과 |

> `LOTTE_BOX_ORDER_MAX` 는 `int` 라 19개 선언 중 문자열 리터럴은 18개다.

### 11.5 결과

| 파일 | 변화 |
|------|---:|
| `BixolonShipmentActivity.java` | 3,573 → **3,543줄** (−30) |
| `Common.java` | 74 → **106줄** (+32) |

### 11.6 남은 작업 (사용자 지시 대기 — 파일별 순차 진행)

| 파일 | 내용 |
|------|------|
| `ShipmentActivity.java` | `SEARCH_TYPE_*` 외 19개 중복 선언 |
| `MainActivity.java` | `SEARCH_TYPE_*` 8개 중복 선언 |
| `LabelPrintHelper.java` | `SEARCH_TYPE_EMART`·`LOTTE` 외 도메인 상수 중복 선언 |
| `ProgressDlgShipSearch.java` | 리터럴 비교 13곳 |
| `ProgressDlgBarcodeSearch.java` | 리터럴 비교 8곳 |
| `ProgressDlgGoodsWetSearch.java` | 리터럴 비교 1곳 |
| `ShipmentListAdapter.java` | 리터럴 비교 1곳 |

---

## 12. Step 8 — 죽은 메서드·클래스 제거

### 12.1 판정 방법

"살아있는 호출"만 센다 — `//` 로 주석 처리된 호출부는 제외한다. 각 건은 **원본(`D:\PDA\PDA-INNO(원본)` `ShipmentActivity.java`)에서도 죽어 있는지** 대조했다.

### 12.2 삭제 내역

| 대상 | 줄수 | 현재 호출부 | 원본 호출부 | 판정 |
|------|---:|------|------|:---:|
| `show_wetNextDialog()` + 주석 호출부 1 | 34 | `//show_wetNextDialog();` 1곳(주석) | 주석 4곳(867·1110·1249·3249), 살아있는 호출 0 | 원본부터 죽음 |
| **`ProgressDlgShipSelectBL`** (내부 AsyncTask + Javadoc) | **183** | `new ...` **0건** | `new ...` **0건** (선언 3100 + 생성자 3105 뿐) | 원본부터 죽음 |
| `find_BL(String)` | 12 | 0건 | `//if (find_BL(msg))` 1곳(주석), 선언 1315 | 원본부터 죽음 |
| `scanFlag_swap()` | 9 | `//scanFlag_swap();` 4곳(전부 주석) | 주석 10곳, 살아있는 호출 0 | 원본부터 죽음 |
| **합계** | **238** | | | |

`ProgressDlgShipSelectBL` 은 `ProgressDlgShipSelect` 의 **BL번호 전용 복사본**이었고 인스턴스화되는 곳이 없었다. 그 안에 들어 있던 `//scanFlag_swap();` 3곳과 `//show_wetNextDialog();` 1곳도 클래스와 함께 제거됐다.

### 12.3 오탐 제외

`onKey` 는 정적 호출이 0건이라 후보로 잡혔으나, `edit_barcode.setOnKeyListener(new View.OnKeyListener(){ ... })` 로 등록된 **프레임워크 콜백**이라 살아있다. 삭제하지 않았다.

### 12.4 검증

| # | 항목 | 결과 |
|:-:|------|:----:|
| 1 | 잔존 참조 | ✅ `//scanFlag_swap();` 1곳(주석)만 — §13 ②로 이월 |
| 2 | 살아있는 내부 클래스 4개 온전 | ✅ `ProgressDlgShipSelect`(9곳 인스턴스화)·`ProgressDlgShipmentSend`·`ProgressDlgPrintConnect`·`ProgressDlgDiscon` |
| 3 | `show_wetFinishDialog` 호출 8곳 유지 | ✅ |
| 4 | 빌드 (`--rerun-tasks` 캐시 배제) | ✅ 통과 |

### 12.5 부수 발견 — `select_flag`

`select_flag = false` 는 `show_wetNextDialog()` 안에만 있었다(원본 4073도 동일). 삭제 후 `select_flag` 는 `= true` 로만 설정되므로 `if (!select_flag)`(현 2168 부근)는 **절대 참이 되지 않는다**.

단 이는 삭제로 생긴 것이 아니다. 유일한 `false` 기록자가 **원래부터 도달 불가 메서드**였으므로 삭제 전에도 항상 거짓이었다. 동작 변화 없이 사실이 드러난 것이며, 정리는 별건으로 남긴다.

### 12.6 결과

3,536 → **3,298줄** (−238)

---

## 13. Step 9 — `select_flag` 죽은 분기 제거

### 13.1 원본 대조

원본 `ShipmentActivity.java` 의 `select_flag` 전수 **5곳**, 원본 프로젝트 타 파일 사용 **0건**.

| 원본 라인 | 코드 | 소속 | 현재 |
|---:|------|------|------|
| 129 | `private boolean select_flag = true;` | 필드 선언 | 삭제 |
| 1576 | `if (!select_flag) { scanFlag_init(); }` | `calc_info()` (1561~) | 삭제 |
| 3026 | `select_flag = true;` | `ProgressDlgShipSelect` (2915~3099, 살아있음) | 삭제 |
| 3181 | `select_flag = true;` | `ProgressDlgShipSelectBL` (3100~3275, 죽음) | Step 8 에서 클래스째 삭제됨 |
| 4073 | **`select_flag = false;`** | **`show_wetNextDialog()`** (4053~, 죽음) | Step 8 에서 메서드째 삭제됨 |

### 13.2 죽어 있던 사슬 (원본 시점)

```
show_wetNextDialog()          호출 0건 (원본 주석 4곳: 867·1110·1249·3249)
    └ select_flag = false      실행 0회
            └ if (!select_flag)   항상 거짓        ← 원본 1576
                    └ scanFlag_init()   실행 0회
```

`false` 로 만드는 유일한 코드가 도달 불가 메서드 안에 있었으므로, **원본에서도 `select_flag` 는 항상 `true`** 였고 `if (!select_flag)` 는 절대 참이 되지 않았다. Step 8 삭제로 생긴 상태가 아니다.

### 13.3 삭제 내역

| 대상 | 줄수 |
|------|---:|
| 필드 Javadoc 4줄 + 선언 1줄 + 앞 빈 줄 | 6 |
| `if (!select_flag) { scanFlag_init(); }` + 앞 빈 줄 | 4 |
| `select_flag = true;` (`ProgressDlgShipSelect` 내) | 1 |
| **합계** | **11** |

필드 주석이 `true: 스캔 모드 / false: 선택 모드` 였으나 "선택 모드"는 도달 불가 상태였다.

### 13.4 검증

| # | 항목 | 결과 |
|:-:|------|:----:|
| 1 | 잔존 참조 | ✅ 0건 |
| 2 | `scanFlag_init()` 다른 호출 6곳 유지 | ✅ (삭제한 것은 도달 불가 1곳뿐) |
| 3 | 빌드 | ✅ 통과 |
| 4 | 원본과의 동작 차이 | ✅ 없음 (원본에서도 도달 불가) |

### 13.5 결과

3,298 → **3,287줄** (−11)

---

## 14. Step 10 — `hideKeyboard()` 중복 제거 (`Common` 통합)

### 14.1 배경

소프트 키보드를 내리는 3줄짜리 유틸이 **3개 파일에 동일하게 복사**돼 있었다.

| 파일 | 선언 | 호출처 | 처리 |
|------|---:|------|:---:|
| `BixolonShipmentActivity` | 492 | 356(Keyboard Wedge ENTER/TAB), 518(입력버튼) | 통합 |
| `ProductionActivity` | 200 | 221(계근 입력버튼), 256(바코드정보 수신버튼) | 통합 |
| `ShipmentActivity` | 522 | 556 | **제외** (유지 방침) |

본문 `diff` 결과 **완전 동일**. 차이는 `ProductionActivity` 쪽에만 Javadoc 이 있는 것뿐이었다.

### 14.2 작업 내용

`Common` 에 `static` 유틸로 통합했다. `getSystemService`·`getCurrentFocus` 가 필요해 `Activity` 를 인자로 받는다.

```java
// Common.java
public static void hideKeyboard(Activity activity) {
    InputMethodManager btn_input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    btn_input.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
}
```

**호출부 4곳이 전부 익명 내부 클래스 안**(`View.OnClickListener`·`View.OnKeyListener`)이라 `this` 가 익명 클래스를 가리킨다. 따라서 바깥 클래스를 명시했다.

```java
hideKeyboard();  →  Common.hideKeyboard(BixolonShipmentActivity.this);
hideKeyboard();  →  Common.hideKeyboard(ProductionActivity.this);
```

선언 삭제로 `InputMethodManager` import 가 두 파일 모두 미사용이 되어 함께 제거했다(`Context` 는 각각 12·2회 사용 중이라 유지).

### 14.3 결과

| 파일 | 변화 |
|------|---:|
| `BixolonShipmentActivity.java` | −7줄 (선언 5 + import 1 + 호출부 치환 2줄 상쇄) |
| `ProductionActivity.java` | −9줄 |
| `Common.java` | +12줄 (import 3 + 메서드 9) |
| **순감** | **−4줄** |

### 14.4 검증

| # | 항목 | 결과 |
|:-:|------|:----:|
| 1 | 본문 동일성 (통합 전 `diff`) | ✅ 완전 동일 |
| 2 | 잔존 `private void hideKeyboard()` | ✅ `ShipmentActivity` 만 (유지 대상) |
| 3 | 호출부 4곳 치환 | ✅ |
| 4 | 미사용 `InputMethodManager` import 제거 | ✅ 2파일 |
| 5 | 빌드 (`--rerun-tasks` 캐시 배제) | ✅ 통과 |

### 14.5 남겨둔 사항

`activity.getCurrentFocus()` 가 `null` 이면 NPE 가 난다. **원본에도 동일**하고 방어 코드가 없다. 동작을 바꾸지 않기 위해 그대로 옮겼다.

> 줄수 감소는 4줄로 미미하다. 목적은 **한쪽만 고치면 갈라지는 중복 제거**이며, Step 7(도메인 상수 `Common` 이동)과 같은 성격이다.

### 14.6 Step 10-1 — `CommonUtils` 분리 (같은 날 후속)

`Common` 에 `hideKeyboard()` 를 넣자 **설정·상수 클래스가 Android UI 프레임워크에 의존**하게 됐다.

```java
// 통합 직후 Common.java 의 import
import android.app.Activity;                          // 새로 생김
import android.content.Context;                       // 새로 생김
import android.util.Log;
import android.view.inputmethod.InputMethodManager;   // 새로 생김
```

`Common` 은 이미 ① JSP URL 25개(설정) ② 전역 가변 상태 8개 ③ 도메인 상수 19개가 섞인 117줄이었다. 여기에 ④ UI 동작까지 더하면 성격이 더 흐려진다.

**배치 기준을 정했다.**

| 두는 곳 | 기준 |
|------|------|
| `Common` | **값** — 설정·상수·앱 전역 상태 |
| `CommonUtils` | **동작** — 여러 Activity 가 공통으로 쓰거나 쓸 가능성이 있는 함수 (`Activity`·`Context` 를 인자로 받음) |
| 전용 클래스 | **업무 로직** — 예: 상품 판정 `find_*` 계열은 `BarcodeMatcher`(예정) |

`common/` 패키지에는 이미 `Base64`·`HttpHelper`·`TestDataHelper`·`ProgressDlg*` 가 역할별로 나뉘어 있어, 유틸 클래스를 추가하는 편이 기존 구성과 맞는다.

**작업**: `common/CommonUtils.java` 신설(27줄) → `Common` 에서 `hideKeyboard()` 및 방금 추가된 import 3건 제거(**117 → 106줄, 원래 import 구성으로 복귀**) → 호출부 4곳을 `CommonUtils.hideKeyboard(...)` 로 치환 + 두 Activity 에 import 추가.

`CommonUtils` 는 인스턴스화할 이유가 없어 `private` 생성자로 막았다.

> **이름 결정**: 처음에 `UiUtils` 로 만들었으나, 모을 대상이 UI 에 한정되지 않고 **여러 Activity 가 공통으로 쓰거나 쓸 가능성이 있는 함수 전반**이므로 `CommonUtils` 로 확정했다(사용자 판단). 단 특정 업무에만 쓰이는 로직은 여기 넣지 않고 전용 클래스로 뺀다.

**검증**: `Common.java` import 가 `Log`·`ArrayList` 로 복귀, 호출부 4곳 치환 확인, `--rerun-tasks` 캐시 배제 빌드 통과.

---

## 15. Step 12 — Javadoc 단순화 + 죽은 SLCS 헬퍼 제거

### 15.1 Javadoc 단순화 (186 → 51줄)

Javadoc 이 **코드를 그대로 옮겨 적고 있어** 코드가 바뀌면 곧바로 낡는 구조였다(파일의 10%, 328줄).

| 대상 | 전 | 후 |
|------|---:|---:|
| 클래스 Javadoc | 65 | **7** |
| `wet_data_insert` | 27 | 8 |
| `setBarcodeMsgProduction` | 20 | 7 |
| `setBarcodeMsg` | 17 | 6 |
| `ProgressDlgShipmentSend` | 15 | 5 |
| `inputBtnListener` | 12 | 5 |
| `mHandler` | 12 | 4 |
| `ProgressDlgShipSelect` | 11 | 5 |
| `onOptionsItemSelected` | 7 | 4 |

**기준**

| 유지 | 삭제 |
|------|------|
| 한 줄 요약 | "처리 흐름" 단계 나열 — 코드에 이미 있음 |
| 코드로 안 드러나는 이유·주의 | 메서드명이 이미 말하는 것 |
| 의미가 불명확한 `@param` | 분기 목록 — 코드가 더 정확함 |

**실제로 낡아 있던 것들**

- `ProgressDlgShipmentSend` — "홈플러스(2)·롯데(6) → insert_goods_wet_homeplus.jsp" 라고 적혀 있으나 실제로 `homeplus.jsp` 를 쓰는 분기는 **없다**. 홈플러스·롯데는 `URL_INSERT_GOODS_WET`, 비정량은 `_NEW`, 생산은 `_PRODUCTION` 이다
- `mHandler` — `MESSAGE_ROWCHECK`·`COMPLETE`·`SEARCHCHECK` 를 설명하지만 이 3개는 **보내는 쪽이 없는 죽은 case** 다

### 15.2 죽은 SLCS 헬퍼 제거 (−55줄)

| 대상 | 줄수 | 근거 |
|------|---:|------|
| `slcsBarcode` | 14 | Activity 호출 **0건** |
| `slcsLine` | 14 | 호출 0건 |
| `slcsBox` | 14 | 호출 0건 |
| `// Label printing methods moved to LabelPrintHelper` 블록 | 9 | 클래스명으로 자명 |
| `// SLCS 헬퍼 메서드 → LabelPrintHelper로 이동됨` 블록 | 4 | **사실과 다름** — 바로 아래 8개가 그대로 있었다 |

**원본 대조**: 원본 `ShipmentActivity.java` 에 `slcs` 는 **0건**이다(BIXOLON 전환 시 신설). 원본 합계 라벨은 Woosim 명령으로 **텍스트만** 찍었고(`PM_setPosition` + `getTTFcode`), `WoosimBarcode`·`drawLine`·`drawBox` 사용이 **0건**이었다. 즉 이 3개는 전환 시 헬퍼 세트로 만들어두고 한 번도 쓰이지 않은 코드다.

**남긴 5개는 전부 사용 중** — `slcsInit`(2) · `slcsLabelSize`(2) · `slcsText`(3) · `slcsPrint`(2) · `slcsFeedToMark`(2). `show_wetDetailDialog` 의 합계 라벨에서만 쓴다.

> `slcsBarcode` 는 Activity(`BD` 명령, narrow=2 wide=4)와 `LabelPrintHelper`(`B1` 명령, narrow=2 wide=3)의 **본문이 갈라져 있었다.** 다만 Activity 쪽은 호출 0건이라 출력에 영향이 없었다. 복사 후 한쪽만 수정된 전형적인 사례다.

### 15.3 기타

`// SLCS 메소드들 - weight list printing(4143, 4188줄)에서 사용되므로 유지` 의 **줄 번호가 현재와 달라**(실제 2847·2887) 위치 대신 메서드명으로 바꿨다. 줄 번호는 금방 낡는다.

### 15.4 결과

3,275 → **3,077줄** (−198). 주석/죽은코드 외 로직 변경 0건, 빌드 통과.

---

## 16. Step 13 — Javadoc 추가 단순화 + 미사용 필드·주석 코드 제거

### 16.1 Javadoc 2차 (71 → 27줄)

| 대상 | 전 | 후 | 비고 |
|------|---:|---:|------|
| `onCreate` | 10 | 4 | 처리 흐름 나열 제거 |
| `mBixolonHandler` | 9 | **6** | **STATE_NONE 주의사항 유지** |
| `ProgressDlgPrintConnect` | 9 | 4 | |
| `selectBtnListener` | 8 | 3 | |
| `sendBtnListener` | 8 | 3 | |
| `slcsText` | 10 | **1** | `@param x  X 좌표` 식이라 정보 0 |
| `slcsLabelSize` | 6 | 1 | |
| `slcsPrint` | 5 | 1 | |
| `slcsFeedToMark` | 6 | 4 | **Woosim 대응 기록 유지** |

**남긴 판단 근거**

- `mBixolonHandler` — `connect()` 내부에서 `disconnect()` 가 먼저 호출돼 `STATE_NONE` 이 발생하므로 그때 실패 메시지를 띄우면 안 된다는 내용. **코드만 봐서는 알 수 없어** 유지했다
- `slcsFeedToMark` — `WoosimCmd.feedToMark()` 대응 기록(§10 Step 6 방침)
- 반대로 `slcsText` 는 파라미터명이 이미 `x`·`y`·`width`·`height`·`text` 라 `@param` 이 무의미했다. SLCS 명령 형식 설명은 본문 주석에 이미 있다

### 16.2 미사용 인스턴스 필드 (−2줄)

`makingdateInputFlag` — 원본 `ShipmentActivity.java:135` 부터 **선언만 있고 읽기·쓰기 0건**.
`private static final` 이 아니라 Step 2 상수 검사에 걸리지 않았던 건이다.

### 16.3 주석 처리된 코드 (−17줄)

**17줄 전부 원본에서도 주석 처리 상태**임을 대조 확인했다.

| 위치 | 내용 |
|------|------|
| `//scanFlag_swap();` | Step 8 에서 삭제한 메서드를 가리키던 잔재 |
| `//current_work_position = -1;`, `//setBarcodeMsg(msg);` | 원본부터 막혀 있던 로직 |
| `//arSM.get(i).setWH_AREA("A-01");`, `//lotte_TryCount = 1;` | 원본 동일 |
| `//Log.d(...)`, `//Double weight_double1 = 0.0;` | 죽은 로그·변수 |
| 입력필드 초기화, Toast·vibrate 등 | 원본 동일 |

> 1차 대조에서 `setWH_AREA`·`롯데 재출력` 2건이 원본과 다른 것처럼 보였으나, `//` 뒤 공백 유무 때문에 생긴 grep 오탐이었다. 실제로는 **원본도 주석 처리** 상태다.

### 16.4 결과

3,077 → **3,014줄** (−63). 주석/선언 외 변경 0건, 캐시 배제 빌드 통과.

### 16.5 현황

| 항목 | 상태 |
|------|------|
| 호출 0건 메서드 | **0개** (전부 정리됨) |
| 미사용 필드 | **0개** |
| 주석 처리된 코드 | **0줄** |
| Javadoc | 328 → 약 120줄 |

---

## 17. Step 14 — 멤버 재배치 (종류별 그룹화)

### 17.1 배경

멤버가 종류별로 모이지 않고 **18번 뒤섞여** 있었다.

| 흩어진 지점 | 문제 |
|------|------|
| 118 | `printerCallback`(익명 객체)이 필드 한가운데 |
| 849~861 | 계근 상태 필드 7개가 `setBarcodeMsg` 바로 앞에 따로 |
| 1863~1963 | 스피너 리스너 3개가 메서드 사이에 끼어 있음 |
| 2221 | `list_send_info` 필드가 내부 클래스 사이에 |
| 2533 | `onActivityResult`(생명주기)가 내부 클래스 사이에 |
| 2659~2671 | 상세 팝업 위젯 13개가 파일 뒤쪽에 따로 |

**필드가 4덩어리**(62~185 / 849~861 / 2221 / 2659~2671), **생명주기도 2덩어리**로 나뉘어 있었다.

### 17.2 새 구조 — 15개 섹션

```
 1 상수 - Handler 메시지 / 요청 코드 / Intent 키
 2 필드 - 프린터 / 블루투스 / 사운드
 3 필드 - 화면 위젯 (메인)
 4 필드 - 화면 위젯 (계근 상세 팝업)
 5 필드 - 계근 상태
 6 생명주기          onCreate · onResume · onStart · onPause · onDestroy · onActivityResult
 7 액션바 메뉴
 8 리스너 - 버튼     5개
 9 리스너 - 스피너   3개
10 핸들러 / 콜백     mHandler · mBixolonHandler · printerCallback
11 바코드 스캔 처리  setBarcodeMsg · setBarcodeMsgProduction · find_* 4 · scanFlag_*
12 계근 저장 / 집계  wet_data_insert · calc_info · refresh_delete · 선택자 3
13 다이얼로그        show_wetDetailDialog · deleteQuestionDialog · show_* 3 · showAlertDialog · startExpiryEnter
14 프린터 - SLCS     slcs* 5 · sendData
15 내부 클래스       ProgressDlg* 4종
```

### 17.3 작업 방식

멤버 134개(필드 84 · 메서드 35 · 리스너 8 · 핸들러 3 · 내부클래스 4)를 파싱해 **앞의 Javadoc·주석·어노테이션을 함께** 묶어 통째로 이동했다. 본문은 한 글자도 건드리지 않았다.

기존 구역 배너(`// =====` 블록) 12개는 새 섹션 헤더로 대체되므로 제거했다.

### 17.4 검증 — 4가지 전부 통과

| # | 항목 | 결과 |
|:-:|------|------|
| 1 | **실행 코드 줄 집합** (주석·빈줄 제거, 공백 정규화 후 다중집합 비교) | ✅ before/after **2,260줄 완전 동일** — 순서만 다름 |
| 2 | **문자열 리터럴** 전수 비교 | ✅ **305종 완전 동일** |
| 3 | **멤버 선언 집합** | ✅ **130개 동일** (추가·삭제 0) |
| 4 | **필드 초기화 상호 의존** | ✅ 다른 필드를 참조하는 초기화 **0건** → 선언 순서 무관 |
| 5 | 빌드 (`--rerun-tasks`) | ✅ 통과 |

> 자바는 멤버 선언 순서가 동작에 영향을 주지 않는다. 유일한 예외가 **필드 초기화 순서**인데(4번), 이 파일의 초기값은 전부 상수·`new ArrayList<>()` 수준이라 서로 의존하지 않음을 확인했다.

### 17.5 결과

3,015 → **3,095줄** (+80). 섹션 헤더 15개(45줄)와 그룹 간 빈 줄이 늘어난 것이며, **실행 코드는 2,260줄로 동일**하다.

> diff 가 매우 크다(약 2,000줄 이동). 읽기 어려우므로 **이 작업만 단독 커밋**으로 분리했다.

---

## 18. Step 15 — 멤버변수 → 지역변수 (계근 상세 팝업 위젯 8개)

### 18.1 배경

`show_wetDetailDialog` 에서만 쓰이는 위젯이 **멤버변수로 선언**돼 있었다. IDE 경고 `Field can be converted to a local variable` 대상이다.

멤버변수면 다이얼로그를 닫아도 값이 남고, 값이 어디서 바뀌는지 찾으려면 파일 전체를 봐야 한다.

### 18.2 판정 — 13개 중 8개만 안전

| 필드 | 쓰이는 곳 | 판정 |
|------|------|:---:|
| `detail_layout`·`detail_edit_position_name`·`detail_edit_ppname`·`detail_edit_ppcode`·`detail_list` | `show_wetDetailDialog` 만, **람다 밖** | ✅ 이관 |
| `detail_dialog`(2174)·`detail_btn_delete`(2193)·`detail_btn_sum`(2218) | 람다에서 읽지만 **대입이 람다 정의보다 앞** | ✅ `final` 붙여 이관 |
| `detail_edit_count`·`detail_edit_weight` | `refresh_delete` 에서도 사용 | ❌ 유지 |
| `detailAdapter`·`detail_btn_back` | `deleteQuestionDialog` 에서도 사용 | ❌ 유지 |
| **`list_gi_info`** | 람다(2196·2199·2203·2221)에서 읽는데 **대입이 2321** | ❌ **유지** |

**`list_gi_info` 제외 이유**: 대입이 람다 정의보다 뒤에 있다. 지역변수로 바꾸려면 선언을 람다 앞으로 올려야 하고, 그러면 **DB 조회가 다이얼로그 표시보다 먼저 일어나 실행 순서가 바뀐다.**

### 18.3 작업 중 실패와 원인

1차 시도에서 **빌드 실패**했다. 필드 앞 그룹 주석이 `/** */` 가 아니라 **블록 주석 `/* */`** 였는데, 스크립트가 닫는 `*/` 만 지워 **주석이 닫히지 않고 뒤따르는 선언 4개를 삼켰다.**

```java
    /*
        계근 상세내역 팝업 필드
     */                              ← 이 줄만 지워짐
    private View detail_layout;      ← 함께 지워짐
```

→ 백업본으로 복원 후, **선언 줄 한 줄만 지우고 주석은 손대지 않는 방식**으로 고쳐 재적용했다.

### 18.4 검증

| # | 항목 | 결과 |
|:-:|------|------|
| 1 | 변경 내역 전수 (주석·빈줄 제거 후 diff) | ✅ 필드 선언 8줄 삭제 + 대입 8줄에 타입 추가, **그 외 0건** |
| 2 | 문자열 리터럴 | ✅ **644개 동일** |
| 3 | 빌드 (`--rerun-tasks`) | ✅ 통과 |

### 18.5 결과

3,094 → **3,078줄** (−16). 실행 코드 2,326 → 2,318줄.

---

## 19. 미조치 / 후속 후보

이번 범위 밖이며, **사용자 지시 대기** 상태다.

| # | 항목 | 내용 |
|:-:|------|------|
| 1 | **주석 처리된 코드 19줄** (②) | `//scanFlag_swap();`(1906, 이제 존재하지 않는 메서드 지칭), `//current_work_position = -1;`, `//setBarcodeMsg(msg);` 등. 의도적으로 막아둔 로직일 수 있어 원본 대조 후 판단 |
| 2 | `ShipmentActivity.java` (4,460줄) | Manifest 등록돼 있으나 `startActivity` 0건. 진입 불가 |
| 3 | `setBarcodeMsg`(404줄) 분리 | `WeighingState` + 콜백 인터페이스 방식. 실행 코드 재작성이라 **실기기 기준선 확보 후** 착수 |
| 4 | 회사코드 `610933` 중복 선언 | `LabelPrintHelper`·`ShipmentActivity` 2곳에 각각 선언. CLAUDE.md "5. 회사코드 추가" 와 함께 별건 |


---

## 20. 진행 현황

| Step | 작업 | 상태 |
|------|------|:----:|
| 1 | Javadoc 태그 전용 줄 삭제 + 인라인 태그 제거 | ✅ 완료 (2026-09-22, −131줄, 주석 외 변경 0건, 빌드 통과) |
| 2 | 미사용 상수 15개 제거 | ✅ 완료 (2026-09-22, −19줄, 원인=LabelPrintHelper 분리 잔재, Step1 대비 diff 상수분만, 빌드 통과) |
| 3 | 미사용 import 7개 제거 | ✅ 완료 (2026-09-22, −7줄, 전수 검사로 7개 확정, 빌드 통과) |
| 4 | 클래스 Javadoc 내용 정정 | ✅ 완료 (2026-09-22, searchType 표 6→8줄·오류 3건 정정, Woosim 표기 3건 정정+1건 삭제, 전환 이력 주석은 보존, 빌드 통과) |
| 5 | `BixolonShipmentActivity_back.java` 삭제 | ✅ 완료 (2026-09-22, 3,730줄, 보존 커밋 `d2e7ddf` 후 삭제, 참조 0건, 빌드 통과) |
| 6 | Woosim 잔재 주석 9줄 제거 | ✅ 완료 (2026-09-22, 유지 6건은 살아있는 로직·빈 case 사유·SLCS 대응 기록, 빌드 통과) |
| 7 | 업무 도메인 상수 19개 `Common` 이동 | ✅ 완료 (2026-09-22, 참조 76곳 치환, 리터럴 오염 0건, Activity −30줄 / Common +32줄, 캐시 배제 빌드 통과) |
| 8 | 죽은 메서드·클래스 4건 제거 | ✅ 완료 (2026-09-22, −238줄, 전부 원본에서도 죽어 있음 대조 완료, onKey 오탐 제외, 캐시 배제 빌드 통과) |
| 9 | `select_flag` 죽은 분기 제거 | ✅ 완료 (2026-09-22, −11줄, 원본 5곳 전수 대조, 원본에서도 항상 true, 빌드 통과) |
| 10 | `hideKeyboard()` 중복 제거 | ✅ 완료 (2026-09-22, 3파일 중복 → 1벌, ShipmentActivity 제외, 호출부 4곳 치환, 캐시 배제 빌드 통과) |
| 10-1 | `CommonUtils` 분리 | ✅ 완료 (2026-09-22, Common 의 Android 의존 제거, 값/동작 배치 기준 확립, 빌드 통과) |
| 11 | 소비기한 입력 화면 호출 중복 제거 | ✅ 완료 (2026-09-22, Intent 키 멤버변수화 + startExpiryEnter 추출, 분기 구조 유지, 빌드 통과) |
| 12 | Javadoc 단순화 + 죽은 SLCS 헬퍼 제거 | ✅ 완료 (2026-09-22, −198줄, 낡은 주석 2건 확인, 원본 대조 완료, 빌드 통과) |
| 13 | Javadoc 2차 + 미사용 필드·주석 코드 제거 | ✅ 완료 (2026-09-22, −63줄, 17줄 전부 원본 대조, 캐시 배제 빌드 통과) |
| 15 | 멤버변수 → 지역변수 8개 | ✅ 완료 (2026-09-23, −16줄, list_gi_info 는 실행 순서 변경 위험으로 제외, 리터럴 644개 동일, 빌드 통과) |
| 14 | 멤버 재배치 (종류별 15개 섹션) | ✅ 완료 (2026-09-22, 멤버 134개 이동, 실행 코드 2,260줄 동일 검증, 리터럴 305종 동일, 캐시 배제 빌드 통과) |

---

## 관련 문서

- `app/doc/일정/2026-09-22.md` — 개발66·67 원복 경위 및 당일 작업 기록
