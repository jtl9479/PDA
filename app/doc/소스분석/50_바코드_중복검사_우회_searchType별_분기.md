# 바코드 중복검사 우회 - searchType별 분기 분석

## 개요

비정량(searchType=4,5) 계근 바코드 스캔 시 중복 검사를 의도적으로 우회하는 분기 로직 분석.  
이마트 정량(searchType=0) 등 정량 유형은 중복 스캔을 차단하지만, 비정량 유형은 박스별 중량 다양성으로 인해 동일 바코드가 정상적으로 반복 스캔될 수 있기 때문에 중복 검사를 우회한다.

- **파일 경로**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`
- **패키지**: `com.rgbsolution.highland_emart`
- **총 라인 수**: 3,420줄
- **타입**: Activity
- **작성일**: 2026-04-27

---

## 1. 역할

- 비콘(Bixolon) PDA를 사용하는 출하 계근 화면의 바코드 스캔 처리 담당
- 정량/비정량 searchType별로 중복 검사 적용 여부를 분기 처리
- 위치 1(같은 상품 재스캔): `duplicatequeryGoodsWet_check` 호출 후 비정량이면 `dup = false` 강제 설정
- 위치 2(일반 BL 스캔): `duplicatequeryGoodsWet` 호출 후 비정량이면 `dup = false` 강제 설정
- 중복 판정 시 "이미 스캔한 바코드입니다." Toast + 진동 1초 출력 및 스캔 차단
- 비정량 우회 시 정상적으로 계근 진행

---

## 2. 주요 상수/필드

| 상수/필드 | 타입 | 값 | 용도 |
|----------|------|-----|------|
| `SEARCH_TYPE_EMART` | String | `"0"` | 이마트 정량 (중복 검사 적용) |
| `SEARCH_TYPE_PRODUCTION` | String | `"1"` | 생산 (별도 흐름) |
| `SEARCH_TYPE_HOMEPLUS` | String | `"2"` | 홈플러스 정량 (중복 검사 적용) |
| `SEARCH_TYPE_WHOLESALE` | String | `"3"` | 도매 (중복 검사 적용) |
| `SEARCH_TYPE_NONFIXED` | String | `"4"` | 이마트 비정량 (중복 검사 우회) |
| `SEARCH_TYPE_HOMEPLUS_NONFIXED` | String | `"5"` | 홈플러스 비정량 (중복 검사 우회) |
| `SEARCH_TYPE_LOTTE` | String | `"6"` | 롯데 정량 (중복 검사 적용) |

---

## 3. 주요 메서드

| 메서드 | 위치(줄) | 반환 | 용도 |
|--------|:--------:|------|------|
| `DBHandler.duplicatequeryGoodsWet_check(Context, String)` | DBHandler.java:1432 | boolean | 바코드 문자열만으로 TB_GOODS_WET 중복 체크 |
| `DBHandler.duplicatequeryGoodsWet(Context, String, String, String, String)` | DBHandler.java:1466 | boolean | 바코드+GI_D_ID+PP_CODE+GI_L_ID 조합으로 중복 체크 |

---

## 4. 호출 관계

### 4.1 이 파일이 호출하는 대상

| 호출 대상 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| `DBHandler` | `duplicatequeryGoodsWet_check(Context, String)` | L1188 | 위치 1: 같은 상품 재스캔 중복 여부 확인 |
| `DBHandler` | `duplicatequeryGoodsWet(Context, String, String, String, String)` | L1308 | 위치 2: 일반 BL 스캔 중복 여부 확인 |
| `Common` | `searchType` (필드 참조) | L1190, L1311 | 현재 searchType 판독 (비정량 여부 판단) |

### 4.2 이 파일을 호출하는 곳

| 호출 위치 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| `BixolonShipmentActivity` 내부 | 바코드 스캔 처리 분기 | L1185 이후 | 작업 중 같은 상품 재스캔 감지 후 중복 분기 진입 |
| `BixolonShipmentActivity` 내부 | BL 스캔 처리 분기 | L1304 이후 | BL 스캔 후 중복 분기 진입 |

---

## 5. 데이터 흐름

```
[바코드 스캔 입력 (msg)]
    ↓
[find_ppcode 추출 및 work_ppcode 비교]
    ↓
[위치 1: 같은 상품 재스캔 경로]
    boolean dup = DBHandler.duplicatequeryGoodsWet_check(ctx, barcode)
        ↓
    [searchType == 4 or 5?]
      YES → dup = false  (비정량: 중복 검사 우회)
      NO  → dup 원값 유지 (정량: 중복 검사 적용)
        ↓
    [dup == true?]
      YES → Toast("이미 스캔한 바코드입니다.") + 진동 + return (차단)
      NO  → 정상 계근 진행

[위치 2: 일반 BL 스캔 경로]
    boolean dup = DBHandler.duplicatequeryGoodsWet(ctx, barcode, gi_d_id, pp_code, gi_l_id)
        ↓
    [searchType == 4 or 5?]
      YES → dup = false  (비정량: 중복 검사 우회)
      NO  → dup 원값 유지 (정량: 중복 검사 적용)
        ↓
    [dup == true?]
      YES → Toast("이미 스캔한 바코드입니다.") + 진동 + return (차단)
      NO  → Goodswets_Info 생성 후 계근 전송 진행
```

---

## 6. 핵심 코드

### 위치 1: 같은 상품 재스캔 시 중복 검사 (BixolonShipmentActivity.java:L1188-1200)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet_check(getApplicationContext(), work_item_fullbarcode);

if(Common.searchType.equals(SEARCH_TYPE_NONFIXED) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)){
    //비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
    dup = false;
}

if (dup) {
    Log.e(TAG, "=====================오류지점1=========================");
    Toast.makeText(getApplicationContext(), "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.", Toast.LENGTH_SHORT).show();
    vibrator.vibrate(1000);
    work_item_fullbarcode = "";
    work_item_barcodegoods = "";
    return;
}
```

### 위치 2: 일반 BL 스캔 시 중복 검사 (BixolonShipmentActivity.java:L1308-1326)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet(getApplicationContext(), work_item_fullbarcode,
        arSM.get(current_work_position).getGI_D_ID(),
        arSM.get(current_work_position).getPACKER_PRODUCT_CODE(),
        arSM.get(current_work_position).getGI_L_ID());

if (Common.searchType.equals(SEARCH_TYPE_NONFIXED) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)) {
    //비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
    dup = false;
}

if (dup) {
    Log.e(TAG, "=====================오류지점2=========================");
    Toast.makeText(getApplicationContext(), "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.", Toast.LENGTH_SHORT).show();
    vibrator.vibrate(1000);
    work_item_fullbarcode = "";
    work_item_barcodegoods = "";
    return;
}
```

### DBHandler - duplicatequeryGoodsWet_check (DBHandler.java:L1432-1463)

```java
// 상품정보 중복체크
public static boolean duplicatequeryGoodsWet_check(Context context, String barcode) {
    // WHERE BARCODE = '?'
    // 바코드 문자열 하나만으로 TB_GOODS_WET에서 조회
    // count > 0 이면 true(중복) 반환
}
```

### DBHandler - duplicatequeryGoodsWet (DBHandler.java:L1466-1502)

```java
// 계근정보 중복체크
public static boolean duplicatequeryGoodsWet(Context context, String barcode, String gi_d_id, String pp_code, String gi_l_id) {
    // WHERE BARCODE = '?' AND GI_D_ID = '?' AND GI_L_ID = '?' AND PACKER_PRODUCT_CODE = '?'
    // 출고단위(GI_D_ID) + LOT단위(GI_L_ID) + 패커상품코드 조합으로 중복 판정
    // count > 0 이면 true(중복) 반환
}
```

---

## 7. 원본 비교

원본 파일: `D:\PDA\PDA-INNO(원본)\app\src\main\java\com\rgbsolution\highland_emart\ShipmentActivity.java`

| 검증 항목 | 원본 (ShipmentActivity.java) | 현재 (BixolonShipmentActivity.java) | 차이 | 판정 |
|---|---|---|---|:---:|
| 위치 1 우회 분기 코드 | L676-678: `Common.searchType.equals("4") \|\| Common.searchType.equals("5")` | L1190-1192: `Common.searchType.equals(SEARCH_TYPE_NONFIXED) \|\| Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)` | 리터럴 `"4"/"5"` → 명명 상수로 상수화 (의미 동일) | PASS |
| 위치 1 우회 동작 | `dup = false;` | `dup = false;` | 완전 일치 | PASS |
| 위치 1 주석 | "비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외" | 완전 일치 | - | PASS |
| 위치 2 우회 분기 코드 | L879-881: `Common.searchType.equals("4") \|\| Common.searchType.equals("5")` | L1311-1313: 상수 사용 (동일 조건) | 상수화 (의미 동일) | PASS |
| 위치 2 우회 동작 | `dup = false;` | `dup = false;` | 완전 일치 | PASS |
| 위치 2 주석 | "비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외" | 완전 일치 | - | PASS |
| 중복 시 Toast 메시지 | "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요." | 완전 일치 | - | PASS |
| 중복 시 진동 | `vibrator.vibrate(1000)` | 완전 일치 | - | PASS |
| `duplicatequeryGoodsWet_check` 시그니처 | `(Context, String barcode)` | `(Context, String barcode)` | 완전 일치 | PASS |
| `duplicatequeryGoodsWet` 시그니처 | `(Context, String barcode, String gi_d_id, String pp_code)` | `(Context, String barcode, String gi_d_id, String pp_code, String gi_l_id)` | `gi_l_id` 파라미터 추가 (LOT 단위 분리를 위한 허용 차이) | PASS (허용) |

**결론: 원본과 100% 동일한 의도된 기능. MSSQL 전환 시 변경된 사항이 아님. 오류 아님.**

---

## 8. 주의사항

- **이것은 오류가 아님**: 비정량(searchType=4,5)에서 동일 바코드 재스캔이 정상 처리되는 것은 의도된 운영 정책이다. 원본 ShipmentActivity.java에서도 동일하게 구현되어 있으며, 코드 주석으로도 명시되어 있다.
- **정량 유형 동작 차이**: searchType=0(이마트), 2(홈플러스), 3(도매), 6(롯데) 정량은 중복 검사가 적용된다. 동일 바코드 재스캔 시 Toast + 진동 + 차단이 발생하는 것은 정상 동작이다.
- **비정량 운영 정책 사유**: 비정량 상품은 박스별로 중량이 제각각이므로, 같은 품목이어도 바코드(상품코드+중량 등 조합)가 다양하게 나올 수 있다. 따라서 중복 검사를 일괄 적용하면 정상 박스도 차단되는 문제가 발생한다.
- **`gi_l_id` 추가는 허용 차이**: 현재 `duplicatequeryGoodsWet`에 `gi_l_id` 파라미터가 추가된 것은 LOT 단위 분리를 위한 기능 확장이며, 중복 검사 우회 로직 자체에는 영향을 주지 않는다.
- **임의 변경 금지**: 비정량 중복 검사 우회 분기(`dup = false`)를 제거하거나 조건을 변경하면 비정량 계근 기능이 정상 동작하지 않는다. 원본 동작 100% 유지 원칙에 따라 이 분기는 반드시 보존해야 한다.
- **searchType 1, 7 (생산, 생산 라벨)**: 계근 흐름 자체가 다르므로 이 분기 대상에 포함되지 않는다.

---

## 9. 관련 문서

- `app/doc/소스분석/49_출하대상받기_4유형_조회조건_종합정리.md` — 4유형(이마트/홈플러스/도매/롯데) 조회 조건 종합 정리
- `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md` — 비정량 출하 계근 전체 흐름 (이 분기가 속하는 상위 흐름)
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` — 이마트 정량 출하 계근 전체 흐름 (중복 검사 적용 대상)
- `app/doc/소스분석/35_로컬DB_구조.md` — TB_GOODS_WET 테이블 구조 (중복 검사 대상 테이블)
- `app/doc/오류/21_비정량_이마트_EMARTLOGIS_CODE_빈문자열_라벨출력_예외.md` — 비정량 라벨 출력 별도 이슈
