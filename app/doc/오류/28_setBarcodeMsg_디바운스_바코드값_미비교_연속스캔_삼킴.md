# setBarcodeMsg 디바운스가 바코드 값 미비교 → 연속 스캔 및 의도된 재귀 호출 삼킴 (계근 2건 중 1건만 저장)

## 발견일
2026-07-02

## 에러 발생 시나리오

```
1. 이마트 정량(searchType=0) 출하 계근 화면 진입
2. 상품 A의 박스 1번 바코드(B001) 스캔 → 계근 처리 완료
3. 1초 이내에 박스 2번 바코드(B002, 박스시리얼만 다름) 스캔
4. 디바운스 블록이 msg 값을 비교하지 않고 시간(1초 이내)만으로 차단
5. 2번째 setBarcodeMsg 호출이 무시됨 → SM_출고계근 1건만 저장됨
```

---

## 현상
- 같은 품목 박스 2개 이상 계근 시 2번째 이후 바코드가 처리되지 않음
- SM_출고계근에 1건만 저장됨 (2건이 정상)
- 화면에 에러 없이 조용히 누락 (Toast 없음, 로그에 "setBarcodeMsg 중복 호출 무시 (디바운싱)"만 출력)
- **1초 이내 연속 스캔 시에만 발동** — 1초 이상 간격이면 정상 처리됨

## 원래부터 있던 버그인가?

**NO - 전환 신규 코드 버그**

원본 `ShipmentActivity.java`에 디바운스 블록이 존재하지 않음 (621줄: `dialog_flag` 체크 후 바로 처리):

```java
// ShipmentActivity.java:621 (원본) — 디바운스 없음
public void setBarcodeMsg(final String msg) {
    try {
        if (dialog_flag)
            return;
        // 바로 처리 진행
        Log.e(TAG, "========================setBarcodeMsg 시작======================");
        edit_barcode.setText(msg);
        ...
```

Honeywell PDA 전환 시 이중 입력 방지를 위해 신규 추가한 디바운스 7줄이 원본의 재귀 계근 경로를 파괴한 전환 버그.

## 원인

### 문제 1 (주요): 디바운스 조건이 바코드 값을 비교하지 않고 시간만 비교

#### 코드 위치
- `BixolonShipmentActivity.java` : 1142~1148줄 (버그 상태 기준)

#### 현재 문제 코드 (버그 상태)
```java
long now = System.currentTimeMillis();
if ((now - lastBarcodeProcessedTime) < BARCODE_PROCESS_DEBOUNCE_MS) {  // ★ 시간만 비교, msg 미비교
    Log.d(TAG, "setBarcodeMsg 중복 호출 무시 (디바운싱)");
    return;  // ★ 다른 바코드(B002)도 1초 이내면 무조건 차단
}
lastBarcodeProcessedTime = now;
// ★ lastProcessedBarcode 필드 없음 — 바코드 값 저장/비교 불가
```

#### 발생 시나리오
B001 스캔 → `lastBarcodeProcessedTime = 현재시각` 저장 → B002 스캔이 1초 이내 도착 →
조건에서 msg 비교 없이 `(now - lastBarcodeProcessedTime) < 1000` true → `return` 실행 → B002 처리 전체 누락

---

### 문제 2 (보조): 재귀 호출 직전 `lastBarcodeProcessedTime` 초기화 없음

#### 코드 위치
- `BixolonShipmentActivity.java` : 1207줄 직전 (버그 상태 기준)

#### 문제 코드
```java
// 버그 상태 — 재귀 호출 직전에 lastBarcodeProcessedTime 리셋 없음
setBarcodeMsg(msg);  // ★ 직전에 lastBarcodeProcessedTime이 세팅된 상태 → 항상 디바운스 차단
```

같은 상품 2번째 박스 스캔 경로(work_ppcode.equals(find_ppcode) 분기)에서 `BL스캔으로 전환하기 위해` 동일 msg로 setBarcodeMsg를 재귀 호출하는데, 바로 직전에 `lastBarcodeProcessedTime`이 업데이트된 상태이므로 재귀 호출도 무조건 디바운스에 걸려 차단됨.

## 상세 흐름

1. **B001 스캔 수신** (work_flag=1, scan_flag=true)
   - setBarcodeMsg("B001") 호출
   - 디바운스 통과 (최초 호출 또는 1초 이상 경과)
   - `lastBarcodeProcessedTime = now` 갱신
   - find_ppcode 조회 → work_ppcode 미설정 → 최초 스캔 분기 → ProgressDlgShipSelect 실행

2. **B001 계근 완료** (ProgressDlgShipSelect 콜백)
   - scan_flag=false → BL스캔 모드로 전환 등 처리
   - 계근 1건 정상 저장

3. **B002 스캔 수신** (1초 이내, work_ppcode 이미 설정됨)
   - setBarcodeMsg("B002") 호출
   - 디바운스 진입: `(now - lastBarcodeProcessedTime) < 1000` → **true**
   - ★ **경로 A (버그)**: msg 값 비교 없이 `return` → B002 처리 전체 누락
   - ★ **경로 B (정상, 1초 이상 경과 시)**: 디바운스 통과 → B002 처리 진행

4. **재귀 호출 경로** (work_ppcode.equals(find_ppcode) 분기)
   - 같은 상품 확인 → 중복 체크 통과 → `set_scanFlag(false)` 후 `setBarcodeMsg(msg)` 재귀 호출
   - ★ **버그**: 직전에 `lastBarcodeProcessedTime = now` 세팅됨 → 재귀 호출 즉시 디바운스 차단
   - 결과: BL스캔 분기로 진입 불가 → 계근 전송 미실행

## 영향 범위
- **searchType 공통**: `setBarcodeMsg()` 를 사용하는 모든 searchType에서 1초 이내 연속 스캔 또는 재귀 경로 시 발동 가능
- **이마트 정량(searchType=0)**: 확인된 영향. 같은 품목 2박스 이상 계근 시 2번째 바코드 누락 가능
- **계근 데이터 손실**: SM_출고계근에 1건만 저장, 실제 2건 박스를 출하했으나 시스템 미기록
- **영향 파일**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`

## 수정 방안

### 수정 1: 디바운스 조건에 바코드 값 비교 추가 (`lastProcessedBarcode` 필드 신설)

```java
// 필드 추가 (클래스 상단)
private String lastProcessedBarcode = "";

// setBarcodeMsg 내 디바운스 블록 수정
long now = System.currentTimeMillis();
if (msg != null && msg.equals(lastProcessedBarcode)   // ★ 동일 바코드일 때만 차단
        && (now - lastBarcodeProcessedTime) < BARCODE_PROCESS_DEBOUNCE_MS) {
    Log.d(TAG, "setBarcodeMsg 중복 호출 무시 (디바운싱)");
    return;
}
lastProcessedBarcode = msg;           // ★ 바코드 값 저장
lastBarcodeProcessedTime = now;
```

### 수정 2: 재귀 호출 직전 `lastBarcodeProcessedTime = 0` 리셋 (의도된 재귀 우회)

```java
// work_ppcode.equals(find_ppcode) 분기 — 재귀 호출 직전
if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {
    show_wetFinishDialog();
}
lastBarcodeProcessedTime = 0;   // ★ 의도된 재귀 호출은 디바운스 우회
setBarcodeMsg(msg);
```

> 수정 시 주의: `lastProcessedBarcode`를 `""` 초기화하는 타이밍 — 계근 완료/취소 후 작업 초기화 시점에도 함께 초기화하면 더 안전하다. 재귀 호출(`lastBarcodeProcessedTime = 0`)은 `lastProcessedBarcode`를 초기화하지 않아도 됨 — `lastBarcodeProcessedTime = 0`으로 시간 조건이 false가 되어 통과하기 때문.

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` — setBarcodeMsg 전체 흐름 분석
- `app/doc/참고자료/오류패턴_분석.md` — **패턴 E**: 기기 전환 (Honeywell PDA 전환 중 추가된 코드), **패턴 F**: UI/로직 버그 (신규 코드 로직 오류)
