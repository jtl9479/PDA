# setBarcodeMsg 디바운스 수정 — 바코드 값 비교 추가

**작성일**: 2026-07-02
**목적**: setBarcodeMsg 디바운스 로직이 바코드 값을 비교하지 않아 다른 바코드 연속 스캔 및 재귀 호출을 차단하던 문제를 수정한다. 이마트 정량 2박스 계근 시 2번째 바코드가 디바운스에 삼켜져 SM_출고계근 1건만 저장되는 증상을 해소한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### BixolonShipmentActivity.java — 디바운스 블록 (수정 전 상태)

```java
// 필드 (수정 전): lastProcessedBarcode 없음
private long lastBarcodeProcessedTime = 0;
private static final long BARCODE_PROCESS_DEBOUNCE_MS = 1000;

// setBarcodeMsg 상단 (수정 전)
long now = System.currentTimeMillis();
if ((now - lastBarcodeProcessedTime) < BARCODE_PROCESS_DEBOUNCE_MS) {
    Log.d(TAG, "setBarcodeMsg 중복 호출 무시 (디바운싱)");
    return;
}
lastBarcodeProcessedTime = now;

// 재귀 호출 (수정 전)
setBarcodeMsg(msg);   // lastBarcodeProcessedTime 초기화 없이 그대로 재귀
```

**현재 동작:**
- `(now - lastBarcodeProcessedTime) < 1000` 만 검사 → 바코드 값이 달라도 1초 이내이면 차단
- 상품 스캔 → BL 스캔으로 넘어가는 재귀 호출(같은 `msg`, 동일 스레드, 즉시 실행)도 차단

### 문제점

- **다른 바코드 1초 이내 연속 스캔 차단**: 정량 2박스 계근 시 2번째 바코드(B002)가 1초 이내에 스캔되면 완전히 무시됨
- **재귀 호출 차단**: 패커상품 스캔 → `set_scanFlag(false)` 후 `setBarcodeMsg(msg)` 재귀 호출 시 `lastBarcodeProcessedTime`이 갱신된 상태라 BL 스캔 단계로 진입 불가
- **SM_출고계근 누락**: 재귀 호출 차단으로 인해 `wet_data_insert()` 미호출 → DB 1건만 저장

---

## 2. 변경 구조

### 데이터 흐름 (변경 전/후)

**변경 전 — 단순 시간 비교:**

```
setBarcodeMsg(msg)
    ↓
(now - lastBarcodeProcessedTime) < 1000?
    YES → 차단 (바코드 값 무관)
    NO  → 처리 계속
        ↓ lastBarcodeProcessedTime = now
```

**변경 후 — 값 + 시간 이중 조건:**

```
setBarcodeMsg(msg)
    ↓
msg == lastProcessedBarcode AND (now - lastBarcodeProcessedTime) < 1000?
    YES → 차단 (같은 바코드 + 1초 이내 = 하드웨어 이중입력)
    NO  → 처리 계속
        ↓ lastProcessedBarcode = msg
        ↓ lastBarcodeProcessedTime = now
```

**재귀 호출 우회:**

```
setBarcodeMsg(B001)  [패커상품 스캔]
    ↓ 동일 품목 2번째 박스 → set_scanFlag(false)
    ↓ lastBarcodeProcessedTime = 0   ← 리셋
    ↓ setBarcodeMsg(B001)  [BL 스캔 재귀]
        ↓ msg == lastProcessedBarcode ("B001" == "B001")
          AND (now - 0) < 1000?  → (now - 0) 는 수백만 ms → FALSE → 통과
```

### 3가지 케이스 동작표

| # | 케이스 | msg | lastProcessedBarcode | 경과시간 | 결과 |
|:-:|--------|-----|----------------------|---------|------|
| a | 다른 바코드, 1초 이내 | B002 | B001 | 0.3s | msg != last → **통과** |
| b | 같은 바코드, 1초 이내 | B001 | B001 | 0.3s | 동일 + 이내 → **차단** (하드웨어 이중입력) |
| c | 재귀 호출 (리셋 후) | B001 | B001 | `now - 0` | 이내 조건 False → **통과** |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **BixolonShipmentActivity.java** | 384줄 부근 (필드 선언부) | `lastProcessedBarcode` 필드 추가 |
| 2 | **BixolonShipmentActivity.java** | 1142~1152줄 (setBarcodeMsg 상단) | 디바운스 조건에 바코드 값 비교 추가 |
| 3 | **BixolonShipmentActivity.java** | 1211줄 (재귀 호출 직전) | `lastBarcodeProcessedTime = 0` 리셋 추가 |

---

## 4. 수정 상세

### 4.1 BixolonShipmentActivity.java — 필드 추가

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`

**변경 전:**

```java
/** setBarcodeMsg 마지막 처리 시각 (중복 호출 방지용) */
private long lastBarcodeProcessedTime = 0;
/** 중복 처리 방지 간격 (ms) - 같은 바코드 처리 후 1초 이내 재처리 차단 */
private static final long BARCODE_PROCESS_DEBOUNCE_MS = 1000;
```

**변경 후:**

```java
/** setBarcodeMsg 마지막 처리 시각 (중복 호출 방지용) */
private long lastBarcodeProcessedTime = 0;
/** setBarcodeMsg 마지막 처리 바코드 값 (같은 바코드 여부 판별용) */
private String lastProcessedBarcode = "";
/** 중복 처리 방지 간격 (ms) - 같은 바코드 처리 후 1초 이내 재처리 차단 */
private static final long BARCODE_PROCESS_DEBOUNCE_MS = 1000;
```

**검증**: 빌드 오류 없음 (String 기본값 "", 초기화 완료)

---

### 4.2 BixolonShipmentActivity.java — 디바운스 조건 수정

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`

**변경 전 (1142~1148줄):**

```java
// 중복 호출 방지: 1초 이내 재처리 차단
long now = System.currentTimeMillis();
if ((now - lastBarcodeProcessedTime) < BARCODE_PROCESS_DEBOUNCE_MS) {
    Log.d(TAG, "setBarcodeMsg 중복 호출 무시 (디바운싱)");
    return;
}
lastBarcodeProcessedTime = now;
```

**변경 후 (1144~1152줄):**

```java
// 중복 호출 방지: 동일 바코드가 1초 이내 재처리되면 무시 (다른 바코드는 통과)
long now = System.currentTimeMillis();
if (msg != null && msg.equals(lastProcessedBarcode)
        && (now - lastBarcodeProcessedTime) < BARCODE_PROCESS_DEBOUNCE_MS) {
    Log.d(TAG, "setBarcodeMsg 중복 호출 무시 (디바운싱)");
    return;
}
lastProcessedBarcode = msg;
lastBarcodeProcessedTime = now;
```

**검증**: `msg != null` 보호 포함, 조건 순서 정상 (short-circuit AND)

---

### 4.3 BixolonShipmentActivity.java — 재귀 호출 우회

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`

**변경 전 (1211~1212줄):**

```java
setBarcodeMsg(msg);
```

**변경 후 (1211~1212줄):**

```java
lastBarcodeProcessedTime = 0;   // 의도된 재귀 호출은 디바운스 우회
setBarcodeMsg(msg);
```

**검증**: `lastBarcodeProcessedTime = 0`으로 경과시간 조건 `(now - 0) < 1000` 이 항상 false → 재귀 호출 통과

---

## 5. 사이드이펙트

### setBarcodeMsg 호출부 영향

```java
// (1) BroadcastReceiver — 바코드 스캔 하드웨어 이벤트 (BixolonShipmentActivity 내부)
if (work_flag == 1) {
    setBarcodeMsg(msg);
} else if (work_flag == 2) {
    setBarcodeMsg(msg);
}

// (2) 확인 버튼 OnClickListener (수기 입력 또는 키보드웨지)
setBarcodeMsg(edit_barcode.getText().toString());

// (3) 재귀 호출 (패커상품 → BL 스캔 전환 시, 수정 대상)
setBarcodeMsg(msg);
```

**영향 분석:**

| 호출부 | 변경 영향 | 대응 |
|--------|----------|------|
| BroadcastReceiver (하드웨어 스캔) | 다른 바코드 연속 스캔은 통과, 동일 바코드 이중입력 차단 유지 | 정상 |
| 확인 버튼 (수기/키보드웨지) | 동일 바코드 1초 이내 재클릭만 차단 | 정상 |
| 재귀 호출 (패커→BL 전환) | `lastBarcodeProcessedTime = 0` 리셋으로 원본 동작 복원 | 정상 |
| 다른 Activity (ProductionActivity 등) | `setBarcodeMsg` 공유 없음 (각 Activity 독립) | 무영향 |

### 원본 대비 기능 동일성

- 원본 `ShipmentActivity.java`에는 디바운스 자체가 없어 모든 호출이 즉시 처리됨
- 이 수정 후 동작: 다른 바코드 스캔 및 재귀 호출 → 원본과 동일하게 즉시 처리
- 유지되는 차이: 동일 바코드 1초 이내 하드웨어 이중입력 차단 (Honeywell PDA 대응, 의도된 차이)

---

## 6. 데이터 저장 구조

### 변수 매핑

| 변수 | 타입 | 용도 | 초기값 |
|------|------|------|--------|
| `lastBarcodeProcessedTime` | long | setBarcodeMsg 마지막 처리 시각 (ms) | 0 |
| `lastProcessedBarcode` | String | setBarcodeMsg 마지막 처리 바코드 값 | "" |
| `BARCODE_PROCESS_DEBOUNCE_MS` | long (상수) | 중복 차단 기준 시간 | 1000 |

### 조건 평가 매핑

```
차단 조건 (3개 AND):
  [1] msg != null
  [2] msg.equals(lastProcessedBarcode)   ← 동일 바코드
  [3] (now - lastBarcodeProcessedTime) < 1000   ← 1초 이내

재귀 우회 (1줄):
  lastBarcodeProcessedTime = 0   → [3] 조건 강제 false
```

---

## 7. 호출 시점

```
[하드웨어 바코드 스캔]
    ↓ BroadcastReceiver.onReceive()
    ↓ work_flag == 1 또는 2
    ↓ setBarcodeMsg(msg)
        ├─ [디바운스 체크] 동일 바코드 + 1초 이내 → return (차단)
        └─ 통과
            ↓ lastProcessedBarcode = msg
            ↓ lastBarcodeProcessedTime = now
            ↓
            [scan_flag == true] 패커상품 스캔
                ├─ 최초 스캔 → ProgressDlgShipSelect
                ├─ 동일 품목 2번째 박스
                │       ↓ set_scanFlag(false)
                │       ↓ lastBarcodeProcessedTime = 0   ← 리셋
                │       ↓ setBarcodeMsg(msg) [재귀]
                │           ↓ [디바운스 체크] (now - 0) >= 1000 → 통과
                │           └─ [scan_flag == false] BL 스캔 진입
                │               ↓ wet_data_insert()
                │               ↓ SM_출고계근 저장
                └─ 다른 품목 스캔 → AlertDialog

[수기/키보드웨지 확인 버튼]
    ↓ work_flag == 1 또는 2
    ↓ setBarcodeMsg(edit_barcode.getText())
        └─ 동일 흐름
```

---

## 8. 개발 플랜

### Step 1: 분석 및 lastProcessedBarcode 필드 추가

**Part 1. 분석**
- 메서드: `setBarcodeMsg(final String msg)`
- 범위: `BixolonShipmentActivity.java:384` (필드 선언부), `:1139~1153` (디바운스 블록)
- 용도: 기존 디바운스 로직 파악 및 `lastProcessedBarcode` 필드 추가
- 주의할 점: `lastBarcodeProcessedTime`, `BARCODE_PROCESS_DEBOUNCE_MS`와 같은 블록에 선언하여 관리 통일

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 필드 선언 | :383~388 | `lastBarcodeProcessedTime`, `BARCODE_PROCESS_DEBOUNCE_MS` 옆에 추가 |
| 2 | 타입 | String | 빈 문자열로 초기화 (`""`) |

**Part 2. 변환 계획**
- 변환 방식: 기존 두 필드 다음 줄에 `private String lastProcessedBarcode = "";` 한 줄 추가
- 주의사항: `null`로 초기화하지 않음 (이후 `equals` 비교 시 NPE 방지)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료):
- **무엇을**: `lastProcessedBarcode` 필드 (String, 초기값 `""`)
- **왜**: 디바운스 조건에서 이전에 처리한 바코드 값과 현재 바코드 값을 비교하기 위해
- **어떻게**: `BixolonShipmentActivity.java` 384~388줄 필드 선언부에 1줄 추가

---

### Step 2: 디바운스 조건에 바코드 값 비교 추가

**Part 1. 분석**
- 메서드: `setBarcodeMsg(final String msg)`
- 범위: `BixolonShipmentActivity.java:1142~1152`
- 용도: `(now - lastBarcodeProcessedTime) < 1000` 단독 조건을 `msg.equals(lastProcessedBarcode) AND 시간 조건`으로 변경
- 주의할 점: `msg != null` 보호 조건 선행 필요 (null 바코드 발생 가능성 대비)

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 기존 조건 | :1144~1147 | `(now - lastBarcodeProcessedTime) < 1000` 단독 |
| 2 | 변경 조건 | :1146~1149 | `msg != null && msg.equals(lastProcessedBarcode) && 시간 조건` |
| 3 | 갱신 추가 | :1151 | `lastProcessedBarcode = msg` |

**Part 2. 변환 계획**
- 변환 방식: if 조건 앞에 `msg != null && msg.equals(lastProcessedBarcode) &&` 추가, `lastProcessedBarcode = msg` 한 줄 추가
- 주의사항: `lastBarcodeProcessedTime = now` 는 차단 분기가 아닌 통과 분기에서만 갱신 (기존 위치 유지)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료):
- **무엇을**: `setBarcodeMsg` 디바운스 조건 (`if` 블록 1~2줄)
- **왜**: 기존 조건은 바코드 값 무관하게 1초 이내 모든 호출을 차단 → 다른 바코드 스캔 및 재귀 호출 모두 차단
- **어떻게**: `msg != null && msg.equals(lastProcessedBarcode) &&` 조건 추가, `lastProcessedBarcode = msg` 갱신 추가

---

### Step 3: 재귀 호출 디바운스 우회 처리

**Part 1. 분석**
- 메서드: `setBarcodeMsg(final String msg)` — 패커상품 스캔 분기 내 재귀 호출
- 범위: `BixolonShipmentActivity.java:1211~1212`
- 용도: 패커상품 스캔 완료 후 BL 스캔으로 전환 시 재귀 호출이 `lastProcessedBarcode = msg` (동일 바코드) + 갱신된 `lastBarcodeProcessedTime` 조건에 걸려 차단되는 것을 방지
- 주의할 점: 이 재귀 호출은 원본 `ShipmentActivity`에도 동일하게 존재하며 정상 실행이 의도된 흐름

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 재귀 호출 | :1212 | `setBarcodeMsg(msg)` — 패커→BL 전환 |
| 2 | 우회 처리 | :1211 | 재귀 직전 `lastBarcodeProcessedTime = 0` |

**Part 2. 변환 계획**
- 변환 방식: `setBarcodeMsg(msg)` 재귀 호출 직전 `lastBarcodeProcessedTime = 0;` 한 줄 추가
- 주의사항: `lastProcessedBarcode`는 리셋하지 않음 (`msg.equals(lastProcessedBarcode)`는 true지만, `(now - 0) < 1000` 이 false가 되어 통과)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료):
- **무엇을**: 재귀 `setBarcodeMsg(msg)` 직전 `lastBarcodeProcessedTime = 0` 추가
- **왜**: 패커상품 스캔 후 BL 스캔으로 전환하는 재귀 호출이 디바운스에 의해 차단되어 `wet_data_insert()` 미호출로 SM_출고계근 미저장
- **어떻게**: 시간 조건 `(now - lastBarcodeProcessedTime) < 1000`에서 `lastBarcodeProcessedTime = 0` 으로 경과시간을 수억 ms로 강제하여 항상 통과

---

### Step 4: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 이마트 정량 — B001 스캔 후 1초 이내 B002 스캔 → SM_출고계근 2건 저장 | □ |
| 2 | 이마트 정량 — B001 스캔 후 1초 이내 B001 재스캔 → "이미 스캔한 바코드" 토스트, 1건만 저장 | □ |
| 3 | 이마트 정량 — B001 스캔 후 1초 이상 경과 후 B001 재스캔 → 2건 저장 | □ |
| 4 | SM_출고계근 전송 → 서버 정상 수신 2건 확인 | □ |
| 5 | 동일 바코드 하드웨어 이중입력(0.1s 이내) → 디바운스 차단, 1건만 저장 | □ |
| 6 | 기존 정상 계근 흐름 (패커→BL 전환 재귀) → wet_data_insert 정상 호출 | □ |

---

### 개발 순서 요약

```
Step 1: lastProcessedBarcode 필드 추가 [완료]
    ↓
Step 2: 디바운스 조건 바코드 값 비교 추가 [완료]
    ↓
Step 3: 재귀 호출 디바운스 우회 처리 [완료]
    ↓
Step 4: 통합 테스트 [대기]
```

---

## 9. 테스트 시나리오

### 시나리오 1: 이마트 정량 2박스 계근 (핵심 시나리오)

```
1. PDA 앱 실행 → 이마트(searchType=0) 로그인
2. 출하대상받기 → 2박스 이상 품목(GI_REQ_PKG >= 2) 선택
3. 바코드 스캔 버튼 터치 → scan_flag = true
4. B001 바코드 스캔
5. 1초 이내 B002 바코드 스캔
6. TB_GOODS_WET 확인 → 2건 저장 확인
7. SM_출고계근 전송 → 서버 2건 수신 확인
```

### 시나리오 2: 동일 바코드 하드웨어 이중입력 차단 확인

```
1. B001 바코드 스캔
2. 즉시 (0.1초 이내) B001 바코드 재스캔 (Honeywell PDA 이중 발사 시뮬레이션)
3. 두 번째 스캔 → "setBarcodeMsg 중복 호출 무시" 로그 확인
4. TB_GOODS_WET 확인 → 1건만 저장 확인
```

### 시나리오 3: 패커상품→BL 전환 재귀 호출 정상 동작 확인

```
1. 이마트 정량 — 작업 중(work_ppcode 설정된 상태)
2. 동일 품목 두 번째 박스 스캔 (work_ppcode == find_ppcode 분기)
3. set_scanFlag(false) → scan_flag = false 전환
4. lastBarcodeProcessedTime = 0 → setBarcodeMsg(msg) 재귀
5. BL 스캔 분기(scan_flag == false) 진입 확인
6. wet_data_insert() 호출 → SM_출고계근 저장 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | msg가 null로 전달되는 경우 | 하드웨어 이벤트 이상 or 수기 입력 빈 값 | `msg != null` 보호 조건 선행 배치 (이미 적용) |
| 2 | 1초 이상 동일 바코드 재스캔 시 중복 저장 | 시간 조건이 1000ms 초과 → 통과 | `duplicatequeryGoodsWet` 중복 체크로 이중 방어 (기존 로직) |
| 3 | `lastBarcodeProcessedTime = 0` 리셋이 다른 재귀 경로에 영향 | 다른 재귀 호출이 있을 경우 의도치 않은 우회 | 재귀 호출은 해당 위치 1곳뿐 — 다른 호출부는 BroadcastReceiver/버튼으로 일반 진입 |
| 4 | `lastProcessedBarcode`가 빈 문자열일 때 첫 스캔 차단 | `"".equals(msg)` 가 참이 될 경우 | 빈 문자열 바코드가 스캔되지 않으므로 실질적 위험 없음, 첫 스캔은 시간 조건 불충족으로 통과 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | lastProcessedBarcode 필드 추가 | ✅ 완료 |
| 2 | 디바운스 조건 바코드 값 비교 추가 | ✅ 완료 |
| 3 | 재귀 호출 디바운스 우회 처리 | ✅ 완료 |
| 4 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- 오류: `app/doc/오류/26_setBarcodeMsg_디바운스_바코드값_미비교.md` (예정)
- 소스분석: `app/doc/소스분석/` (BixolonShipmentActivity setBarcodeMsg 분석)

---

**문서 버전**: 1.0
