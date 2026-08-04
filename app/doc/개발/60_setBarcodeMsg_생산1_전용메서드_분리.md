# setBarcodeMsg() 생산(searchType=1) 전용 메서드 분리

**작성일**: 2026-08-04
**목적**: `setBarcodeMsg()`가 8개 searchType을 한 메서드에서 처리해 유지보수가 어렵다. 생산(1) 전용 `setBarcodeMsgProduction()`을 신설하고 진입점에서 분기시켜, 생산 로직을 독립시킨다. 향후 다른 searchType도 동일 방식으로 분리하기 위한 첫 적용 사례다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

### 추가 제약 조건 (이 가이드 한정)

- **기존 `setBarcodeMsg()` 본문은 한 줄도 수정하지 않는다.** 진입점에 분기 3줄만 추가한다.
- 생산 전용 메서드는 기존 로직을 **그대로 복사**한 뒤, 생산이 타지 않는 분기만 삭제한다. 변수명·연산·순서를 바꾸지 않는다.
- **각 Step은 컴파일과 동작이 모두 보장되어야 한다.** 중간 단계에서 생산 계근이 멈추지 않도록 원본 전체를 먼저 복사한 뒤 분기를 걷어낸다.
- 생산이 타지 않는 분기만 제외한다. 판단 근거는 `소스분석/58`에 전수 기록되어 있으며, 그 범위를 벗어나 제거하지 않는다.
- 멤버 변수를 지역 변수로 바꾸지 않는다. 생명주기가 달라져 동작이 변할 수 있다.
- 다른 searchType(0·2·3·4·5·6·7)의 동작에 영향을 주지 않는다.

---

## 1. 현재 구조

### 1.1 setBarcodeMsg()

**위치**: `BixolonShipmentActivity.java:1142~1539` (397행)

```
setBarcodeMsg(msg)
 ├─ dialog_flag 체크 / 디바운스 / edit_barcode.setText()
 ├─ [1차] if (scan_flag)  패커상품 스캔          :1162
 └─ [2차] else            BL 스캔                :1248
      └─ ITEM_TYPE 분기 → 중량 추출 → wet_data_insert()
```

**호출 경로 3개**

| # | 경로 |
|:-:|------|
| 1 | Honeywell 스캐너 → `m_brc.onReceive():217` → `setMessage():246` → `BixolonShipmentActivity.setMessage():1077` |
| 2 | [입력] 버튼 → `inputBtnListener.onClick():629` (`work_flag == 1` 또는 `2`) |
| 3 | 자기 재귀 `:1220` (1차 성공 후 2차 연결) |

### 1.2 문제점

| # | 문제 | 내용 |
|:-:|------|------|
| 1 | 단일 메서드 397행 | searchType 분기 5곳 + `ITEM_TYPE` 분기 4곳 + `CENTERNAME` 분기 2곳 중첩 |
| 2 | 전 유형 동시 영향 | 한 곳 수정 시 8개 searchType 전부 회귀 검증 필요 |
| 3 | 생산 무관 코드 다수 | 생산은 10개 분기 중 2개만 사용 (`소스분석/58` §3) |
| 4 | 잠재 예외 | `:1286` `getCENTERNAME().substring(0, 3)` — 3자 미만이면 `StringIndexOutOfBoundsException` |

---

## 2. 변경 구조

### 진입점 분기

```
setBarcodeMsg(msg)
 ├─ searchType == "1" → setBarcodeMsgProduction(msg); return;   ★ 신규 3줄
 └─ 그 외            → 기존 397행 (무수정)
```

### 데이터 흐름 (생산)

```
Honeywell 스캐너 / [입력] 버튼
    ↓
setBarcodeMsg(msg)                    :1142
    ↓ searchType == "1"
setBarcodeMsgProduction(msg)          신규
    ├─ dialog_flag / 디바운스
    ├─ [1차] 패커상품 스캔
    │     find_PackerProduct / find_PackerProductBarcodeGoods
    │     최초 → ProgressDlgShipSelect
    │     동일 → 중복검사 → set_scanFlag(false) → 재귀
    │     다른 → AlertDialog
    ├─ [2차] BL 스캔
    │     BL 매칭 → current_work_position
    │     계근 완료 확인
    │     4키 중복검사
    ├─ 중량 추출  ── ITEM_TYPE S / J
    └─ wet_data_insert()
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **BixolonShipmentActivity.java** | `:1142` | 진입점 분기 3줄 추가 |
| 2 | **BixolonShipmentActivity.java** | `setBarcodeMsg()` 아래 | `setBarcodeMsgProduction()` 신규 |

---

## 4. 수정 상세

### 4.1 진입점 분기

**변경 전:**
```java
public void setBarcodeMsg(final String msg) {
    try {
        if (dialog_flag)
            return;
```

**변경 후:**
```java
public void setBarcodeMsg(final String msg) {
    // 생산(searchType=1) : 전용 메서드로 분리 (개발60)
    if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) {
        setBarcodeMsgProduction(msg);
        return;
    }

    try {
        if (dialog_flag)
            return;
```

**검증**: 0·2·3·4·5·6·7은 분기를 통과하지 않으므로 기존 동작 그대로

### 4.2 이식 대상 / 제외 대상

`소스분석/58` §5 기준

**이식 (필요)**

| 구간 | 원본 위치 |
|------|:--------:|
| `dialog_flag` 체크 | `:1143` |
| 디바운스 (동일 바코드 1초) | `:1147~1157` |
| `edit_barcode.setText()` | `:1161` |
| 1차 패커상품 스캔 전체 | `:1162~1247` |
| BL번호 매칭 | `:1256~1270` |
| `current_work_position` 검증 | `:1298~1305` |
| 계근 완료 확인 | `:1310~1317` |
| 4키 중복검사 | `:1319` |
| **`ITEM_TYPE` S** | **`:1412~1462`** |
| **`ITEM_TYPE` J** | **`:1463~1471`** |
| `wet_data_insert()` | `:1527` |

**제외 (생산 미해당)**

| # | 분기 | 원본 위치 | 근거 |
|:-:|------|:--------:|------|
| 1 | 비정량 중복검사 우회 (1차) | `:1198` | `4`·`5` 전용 |
| 2 | 킬코이 미트센터 소비기한 | `:1278` | `PACKER_CODE=''`, `STORE_CODE=''` |
| 3 | CENTERNAME TRD/E/T/WET 소비기한 | `:1286` | `CENTERNAME='하이랜드푸드'` 고정 |
| 4 | 비정량 중복검사 우회 (2차) | `:1322` | `4`·`5` 전용 |
| 5 | `ITEM_TYPE` W / HW | `:1359` | 생산 VIEW 미출력 |
| 6 | 이마트 LB 환산 자릿수 | `:1436~1440` | `searchType==0` 전용 → `else` 경로 고정 |
| 7 | `ITEM_TYPE` B | `:1474` | 홈플러스 비정량 전용 |

### 4.3 ITEM_TYPE 분기 (이식 후)

원본은 `if / else if / else if` + 별도 `if`(B) 구조다. 생산은 S·J 2개만 남으므로 다음과 같이 된다.

```java
if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_S)) {
    … 원본 :1412~1462 그대로 …
} else if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_J)) {
    … 원본 :1463~1471 그대로 …
}
```

**S 분기 내 이마트 조건 처리** — 원본 `:1436~1440`

```java
if (Common.searchType.equals(SEARCH_TYPE_EMART)) {
    item_weight_double = Math.floor(temp_weight_double * item_pow) / item_pow;
} else {
    item_weight_double = Math.floor(temp_weight_double * 100) / 100;
}
```

생산은 항상 `else`이므로 조건문을 제거하고 `else` 본문만 남긴다.

```java
item_weight_double = Math.floor(temp_weight_double * 100) / 100; //lb 변환 후 소수점 두자리까지 처리
```

> 이는 **분기 제거이지 로직 변경이 아니다.** 생산에서 `searchType == EMART`는 성립할 수 없다.

---

## 5. 사이드이펙트

### 5.1 다른 searchType 영향

**없음.** 기존 `setBarcodeMsg()` 본문을 수정하지 않고, 진입점 분기가 `searchType == "1"`에서만 성립한다.

| searchType | 경로 |
|:--:|------|
| 0·2·3·4·5·6·7 | 기존 `setBarcodeMsg()` 본문 (무변경) |
| **1** | **`setBarcodeMsgProduction()`** |

### 5.2 잠재 예외 해소

원본 `:1286`
```java
getCENTERNAME().substring(0, 3).equals(CENTER_NAME_ET)
```

`CENTERNAME`이 3자 미만이면 `StringIndexOutOfBoundsException`이 발생한다. 생산은 `'하이랜드푸드'`(6자) 고정이라 현재 안전하나, 생산 전용 메서드에서 이 분기가 제외되어 **위험 자체가 사라진다.**

다른 searchType의 해당 위험은 그대로 남는다(이 가이드 범위 밖).

### 5.3 재귀 호출

원본 `:1220`
```java
lastBarcodeProcessedTime = 0;   // 의도된 재귀 호출은 디바운스 우회
setBarcodeMsg(msg);
```

생산 전용 메서드에서는 **`setBarcodeMsgProduction(msg)`를 재귀 호출**해야 한다. `setBarcodeMsg()`를 부르면 진입점 분기로 되돌아와 결과는 같으나, 호출 스택이 불필요하게 깊어진다.

### 5.4 향후 확장

이 방식이 검증되면 다른 searchType도 동일하게 분리 가능하다.

```java
public void setBarcodeMsg(final String msg) {
    if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION))  { setBarcodeMsgProduction(msg); return; }
    if (Common.searchType.equals(SEARCH_TYPE_HOMEPLUS))    { setBarcodeMsgHomeplus(msg);   return; }
    …
}
```

단, **각 searchType의 실기기 검증이 끝난 뒤 진행**해야 한다. 검증 기준선 없이 분리하면 기존 버그와 분리 버그를 구분할 수 없다.

---

## 6. 공유 멤버 변수

지역 변수로 바꾸지 않고 그대로 사용한다.

| 변수 | 역할 |
|------|------|
| `scan_flag` | 1차/2차 스캔 구분 |
| `dialog_flag` | 다이얼로그 표시 중 재진입 차단 |
| `work_flag` | 1=바코드 / 0=수기 / 2=상품코드 |
| `work_ppcode` | 현재 작업 패커상품코드 |
| `work_item_fullbarcode` | 스캔한 전체 바코드 |
| `work_item_barcodegoods` | 바코드 상품코드 |
| `work_item_bi_info` | `Barcodes_Info` (파싱 규칙) |
| `work_bl_no` | 현재 BL번호 |
| `current_work_position` | `arSM` 내 현재 대상 인덱스 |
| `arSM` | 계근 대상 목록 |
| `centerTotalCount` / `centerWorkCount` | 완료 판정 |
| `lastProcessedBarcode` / `lastBarcodeProcessedTime` | 디바운스 |
| `alert_flag` | 경고 다이얼로그 표시 여부 |

미사용: `expiryDayTrans`(소비기한), `lotte_TryCount`(롯데 박스순번)

---

## 7. 호출 시점

```
[BixolonShipmentActivity]
    ├── Honeywell 스캐너 트리거
    │       ↓ HoneywellScannerActivity.m_brc.onReceive()   :217
    │       ↓ setMessage(receive_data)                     :246
    │       ↓ BixolonShipmentActivity.setMessage()         :1077
    │       ↓ setBarcodeMsg(msg)                           :1142
    │       ↓ ★ searchType == "1" → setBarcodeMsgProduction(msg)
    │
    └── [입력] 버튼 (work_flag == 1 또는 2)
            ↓ inputBtnListener.onClick()                   :629
            ↓ setBarcodeMsg(edit_barcode.getText())
            ↓ ★ 동일 분기
```

---

## 8. 개발 플랜

### Step 1: 진입점 분기 + 원본 전체 복사

**Part 1. 분석**
- 메서드: `setBarcodeMsg()`
- 범위: `BixolonShipmentActivity.java:1142~1539` (본문 397행)
- 용도: 생산 전용 메서드를 만들고 원본 로직을 그대로 확보
- 주의할 점: 기존 `setBarcodeMsg()` 본문 무수정. 재귀 호출·로그 태그만 치환

**Part 2. 변환 계획**
- 진입점 6줄 추가 (주석 1 + 분기 4 + 공백 1)
- `setBarcodeMsgProduction()` 신설 후 원본 본문 397행 복사
- 치환 2건
  - `setBarcodeMsg(msg);` → `setBarcodeMsgProduction(msg);` (재귀)
  - 로그 문자열 `setBarcodeMsg …` → `setBarcodeMsgProduction …`

> **원안(빈 메서드) 변경 사유** — 빈 메서드로 두면 Step 4가 끝날 때까지 생산 바코드 스캔이 동작하지 않는다. 원본을 먼저 복사하면 **모든 Step에서 컴파일과 동작이 함께 보장**되고, Step 2~4는 분기 삭제만 하면 된다. 최종 결과물은 동일하다.

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용**
- **무엇을**: 진입점 분기 6줄 + `setBarcodeMsgProduction()` 신설(원본 397행 복사)
- **왜**: 생산 로직을 독립시켜 다른 searchType 수정 영향을 차단
- **어떻게**: §4.1 참조. 재귀 호출·로그 태그만 치환하고 로직은 무변경

---

### Step 2: 1차 패커상품 스캔 — 비정량 분기 삭제

**Part 1. 분석**
- 범위: `setBarcodeMsgProduction()` 내 1차 스캔 구간
- 삭제: 비정량(4,5) 중복검사 우회 1곳
- 주의할 점: `dup` 변수 자체는 유지. 우회 조건문만 제거

**Part 2. 변환 계획**
```java
// 삭제 대상
if(Common.searchType.equals(SEARCH_TYPE_NONFIXED) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)){
    dup = false;
}
```

**체크리스트**
- [ ] Part 1~4
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

---

### Step 3: 2차 BL 스캔 — 소비기한·비정량 분기 삭제

**Part 1. 분석**
- 범위: `setBarcodeMsgProduction()` 내 2차 BL 스캔 구간
- 삭제 3건
  - 킬코이 미트센터 소비기한 검증
  - CENTERNAME TRD/E/T/WET 소비기한 검증 (`substring(0,3)` 예외 위험 동반 제거)
  - 비정량(4,5) 중복검사 우회
- 주의할 점: `expiryDayTrans = ` 초기화는 유지. 삭제 후 `if/else if` 구조가 깨지지 않도록 확인

**체크리스트**
- [ ] Part 1~4
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

---

### Step 4: ITEM_TYPE W/HW·B 삭제 + 이마트 LB 분기 정리

**Part 1. 분석**
- 범위: `setBarcodeMsgProduction()` 내 중량 추출 구간
- 삭제 2건: `ITEM_TYPE` W/HW, `ITEM_TYPE` B
- 정리 1건: S 분기 내 이마트 LB 자릿수 조건 → `else` 본문만 유지
- 주의할 점
  - 변수 선언부(`item_weight`, `item_weight_double`, `item_weight_str`, `item_pow`, `item_making_date`, `item_box_serial`) 6개는 **유지**
  - W/HW 삭제 후 S가 `if`로 시작하도록 구조 조정
  - B는 별도 `if`문이므로 블록 통째로 삭제

**체크리스트**
- [ ] Part 1~4
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

---

### Step 5: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 컴파일 통과 | □ |
| 2 | 생산 대상받기 → 계근 화면 진입 | □ |
| 3 | 1차 상품 스캔 → 대상 목록 로드 | □ |
| 4 | 2차 BL 스캔 → 중량 추출 | □ |
| 5 | `ITEM_TYPE = 'J'` → `PACKWEIGHT` 고정값 기록 | □ |
| 6 | `ITEM_TYPE = 'S'` → 바코드 실측 + 제조일자·박스시리얼 | □ |
| 7 | 동일 바코드 재스캔 → 중복 차단 | □ |
| 8 | 다른 상품 스캔 → AlertDialog | □ |
| 9 | 계근 완료 시 완료 다이얼로그 | □ |
| 10 | 수기 입력([입력] 버튼) 경로 정상 | □ |
| 11 | 전송 → `PD_생산계근` INSERT | □ |
| 12 | **이마트(0) 회귀** — 스캔·중량·소비기한·라벨 | □ |
| 13 | **홈플러스(2) 회귀** | □ |
| 14 | **비정량(4) 회귀** — 중복검사 우회 동작 | □ |
| 15 | **롯데(6) 회귀** — 박스순번 | □ |

---

### 개발 순서 요약

```
Step 1: 진입점 분기 + 원본 397행 복사     ← 컴파일·동작 보장
    ↓
Step 2: 비정량 분기 삭제 (1차 스캔)
    ↓
Step 3: 소비기한·비정량 분기 삭제 (2차 BL)
    ↓
Step 4: ITEM_TYPE W/HW·B 삭제 + LB 분기 정리
    ↓
Step 5: 통합 테스트
```

**원본 복사 후 분기를 걷어내는 방식**이라 각 Step에서 컴파일과 동작이 모두 보장된다.

---

## 9. 테스트 시나리오

### 시나리오 1: 생산 계근 (ITEM_TYPE J)

```
1. P0101에서 작업지시 생성 (비정량여부 = 0 품목)
2. 생산계근대상받기 → 생산계근입력시작
3. 상품 바코드 스캔 (1차) → 대상 목록 표시
4. BL 스캔 (2차) → PACKWEIGHT 고정값으로 계근
5. logcat "Type J | 지정된 중량값" 확인
6. 동일 바코드 재스캔 → "이미 스캔한 바코드입니다" 확인
7. 전송 → PD_생산계근 확인
```

### 시나리오 2: 생산 계근 (ITEM_TYPE S)

```
1. 품목코드 관리에서 비정량여부 체크
2. 1~4 동일
5. logcat "Type S | 절사한 중량값" / "Type S | 절사한 제조일" 확인
6. 박스마다 다른 중량이 기록되는지 확인
```

### 시나리오 3: 회귀 테스트

```
1. 이마트(0) 출하대상받기 → 계근 → 라벨 출력 → 전송
2. 홈플러스(2) 동일
3. 비정량(4) — 동일 바코드 연속 스캔이 허용되는지 확인
4. 롯데(6) — 박스순번 1~9999 순환
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|:-:|--------|------|----------|
| 1 | 삭제 과정에서 구조 깨짐 | `if/else if` 체인 중간 분기 제거 | Step 단위로 나누고 각 단계 컴파일 확인 |
| 2 | 재귀 호출이 기존 메서드로 감 | `setBarcodeMsg(msg)` 그대로 복사 | Step 1에서 `setBarcodeMsgProduction(msg)`로 치환 완료 (§5.3) |
| 3 | 멤버 변수 초기화 시점 차이 | 지역 변수로 전환 시 발생 | 멤버 변수 그대로 사용 |
| 4 | 다른 searchType 회귀 | 기존 본문 실수로 수정 | `git diff`로 기존 메서드 무변경 확인 |
| 5 | `ITEM_TYPE` S 미검증 | 현재 데이터 전부 `J` | 품목코드에서 비정량여부 체크 후 테스트 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | 진입점 분기 + 원본 397행 복사 | ✅ 완료 |
| 2 | 비정량 분기 삭제 (1차 스캔) | ⏳ 대기 |
| 3 | 소비기한·비정량 분기 삭제 (2차 BL) | ⏳ 대기 |
| 4 | ITEM_TYPE W/HW·B 삭제 + LB 분기 정리 | ⏳ 대기 |
| 5 | 통합 테스트 | ⏳ 대기 |

---

## 12. 관련 문서

- `app/doc/소스분석/58_setBarcodeMsg_생산1_경로분석.md` — 분기별 판정 근거 (본 가이드의 전제)
- `app/doc/개발/59_생산_계근대상받기_JSP_MSSQL전환.md` — 생산 전환 개발 가이드
- `app/doc/column/11_생산_계근대상_컬럼매핑_Oracle_MSSQL.md` — `ITEM_TYPE` S/J 매핑 근거
- `app/doc/개발/56_setBarcodeMsg_디바운스_수정[...].md` — 디바운스 로직
- `app/doc/오류/28_setBarcodeMsg_디바운스_바코드값_미비교_연속스캔_삼킴.md`
- `app/doc/소스분석/50_바코드_중복검사_우회_searchType별_분기.md` — 중복검사 정책 (생산 제외)
- `app/doc/기능/11_바코드스캔후_전체프로세스.md`

---

**문서 버전**: 1.0
