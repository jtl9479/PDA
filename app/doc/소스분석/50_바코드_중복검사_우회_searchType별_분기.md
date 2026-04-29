# 정량(0) vs 비정량(4) searchType별 동작 차이 종합 분석

## 개요

이마트 정량(searchType=0)과 이마트 비정량(searchType=4) 사이에 존재하는 **5개의 의도된 동작 차이**를 종합 분석한 문서.
비정량은 "정량보다 더 유연한 처리"가 필요한 운영 정책에 따라 설계되었으며,
5개 차이점 모두 원본 ShipmentActivity.java에도 동일하게 구현되어 있는 의도된 기능이다.
⑥ original-comparator 정식 검증 결과 원본 100% 동일 확인.

- **파일 경로**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`
- **원본 파일 경로**: `D:\PDA\PDA-INNO(원본)\app\src\main\java\com\rgbsolution\highland_emart\ShipmentActivity.java`
- **패키지**: `com.rgbsolution.highland_emart`
- **총 라인 수**: 3,420줄
- **타입**: Activity
- **작성일**: 2026-04-27

---

## 1. 역할

- 이마트 정량(searchType=0)과 비정량(searchType=4) 사이의 5개 동작 차이점을 종합 정리
- 각 차이점이 의도된 운영 정책임을 원본 코드와 함께 검증
- MSSQL 전환 과정에서 의도적으로 변경된 사항이 없음을 확인
- 비정량 유연 처리 정책: 중복 검사 우회, 다중 품목 매핑, 배치 INSERT JSP, 출하 완료 처리 미호출

---

## 2. 주요 상수/필드

| 상수/필드 | 타입 | 값 | 용도 |
|----------|------|-----|------|
| `SEARCH_TYPE_EMART` | String | `"0"` | 이마트 정량 — 중복 검사 적용, 단건 URL, complete_shipment 호출 |
| `SEARCH_TYPE_NONFIXED` | String | `"4"` | 이마트 비정량 — 중복 검사 우회, 다중 매핑, 배치 URL, complete_shipment 미호출 |
| `SEARCH_TYPE_HOMEPLUS_NONFIXED` | String | `"5"` | 홈플러스 비정량 — 비정량과 동일 정책 적용 |
| `Common.URL_INSERT_GOODS_WET` | String | 서버 URL | 정량 계근 단건 INSERT JSP (insert_goods_wet.jsp) |
| `Common.URL_INSERT_GOODS_WET_NEW` | String | 서버 URL | 비정량/생산/생산라벨 배치 INSERT JSP (insert_goods_wet_new.jsp) |

---

## 3. 주요 메서드

| 메서드 | 위치(줄) | 반환 | 용도 |
|--------|:--------:|------|------|
| `DBHandler.duplicatequeryGoodsWet_check(Context, String)` | DBHandler.java:1432 | boolean | 바코드 문자열만으로 TB_GOODS_WET 중복 체크 (위치 1) |
| `DBHandler.duplicatequeryGoodsWet(Context, String, String, String, String)` | DBHandler.java:1466 | boolean | 바코드+GI_D_ID+PP_CODE+GI_L_ID 조합 중복 체크 (위치 2) |
| `find_work_info(String, boolean)` | BixolonShipmentActivity.java:약 L1790 | String | 바코드로 품목 매핑 — 비정량은 pp_code 콤마 누적 |
| `labelPrintHelper.setPrinting(...)` | BixolonShipmentActivity.java:L1702, L1705 | void | 정량/비정량 공통 라벨 출력 메서드 (내부에서 BARCODE_TYPE 분기) |
| `HttpHelper.sendData(...)` | BixolonShipmentActivity.java:L2743 | String | 정량 계근 단건 전송 |
| `HttpHelper.sendDataDb(...)` | BixolonShipmentActivity.java:L2748 | String | 비정량 계근 배치 전송 |

---

## 4. 호출 관계

### 4.1 이 파일이 호출하는 대상

| 호출 대상 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| `DBHandler` | `duplicatequeryGoodsWet_check(Context, String)` | L1188 | 위치 1: 같은 상품 재스캔 중복 여부 확인 |
| `DBHandler` | `duplicatequeryGoodsWet(Context, String, String, String, String)` | L1308 | 위치 2: 일반 BL 스캔 중복 여부 확인 |
| `Common` | `searchType` (필드 참조) | L1190, L1311, L1839 등 | 현재 searchType 판독 (비정량 여부 판단) |
| `labelPrintHelper` | `setPrinting(...)` | L1702, L1705 | 정량/비정량 라벨 출력 |
| `HttpHelper` | `sendData(...)` | L2743 | 정량 계근 단건 전송 |
| `HttpHelper` | `sendDataDb(...)` | L2748 | 비정량/생산/생산라벨 배치 전송 |

### 4.2 이 파일을 호출하는 곳

| 호출 위치 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| `BixolonShipmentActivity` 내부 | 바코드 스캔 처리 분기 | L1185 이후 | 작업 중 같은 상품 재스캔 감지 후 중복 분기 진입 |
| `BixolonShipmentActivity` 내부 | BL 스캔 처리 분기 | L1304 이후 | BL 스캔 후 중복 분기 진입 |
| `BixolonShipmentActivity` 내부 | `find_work_info()` 호출 | L1839 블록 | 바코드에 매핑된 품목 탐색 후 pp_code 누적 |
| `ProgressDlgShipmentSend` | `doInBackground()` | L2743, L2748 | 계근 데이터 서버 전송 URL 분기 |

---

## 5. 데이터 흐름

```
[바코드 스캔 입력 (msg)]
    ↓
[find_work_info() 호출]
    │
    ├─[searchType == 4?]
    │    YES → 매핑된 모든 품목 pp_code에 콤마 누적 (', '로 이어붙임)
    │    NO  → 첫 매칭 pp_code만 사용 (정량: 단일 매칭)
    ↓
[중복 검사 (위치 1: 같은 상품 재스캔)]
    boolean dup = DBHandler.duplicatequeryGoodsWet_check(ctx, barcode)
        │
        [searchType == 4 or 5?]
          YES → dup = false  (비정량: 중복 검사 우회)
          NO  → dup 원값 유지 (정량: 중복 검사 적용)
        │
        [dup == true?]
          YES → Toast + 진동 + return (차단)
          NO  → 계속 진행
    ↓
[중복 검사 (위치 2: 일반 BL 스캔)]
    boolean dup = DBHandler.duplicatequeryGoodsWet(ctx, barcode, gi_d_id, pp_code, gi_l_id)
        │
        [searchType == 4 or 5?]
          YES → dup = false  (비정량: 중복 검사 우회)
          NO  → dup 원값 유지 (정량: 중복 검사 적용)
        │
        [dup == true?]
          YES → Toast + 진동 + return (차단)
          NO  → Goodswets_Info 생성 후 라벨 출력 진행
    ↓
[라벨 출력]
    ├─[searchType == 0?] → labelPrintHelper.setPrinting(...)  [정량]
    └─[searchType == 4?] → labelPrintHelper.setPrinting(...)  [비정량 — 동일 메서드, 내부에서 BARCODE_TYPE 분기]
    ↓
[계근 데이터 서버 전송 (ProgressDlgShipmentSend)]
    ├─[searchType == 0?] → sendData(packet, URL_INSERT_GOODS_WET)      [정량: 단건 INSERT]
    └─[searchType == 4?] → sendDataDb(packet, URL_INSERT_GOODS_WET_NEW) [비정량: 배치 INSERT]
    ↓
[출하 완료 처리]
    ├─[searchType == 0?] → (현재 코드: complete_shipment 호출 제거됨 — W_GOODS_ID 미존재 대응)
    └─[searchType == 4?] → (현재 코드: complete_shipment 호출 없음 — 원본과 동일, 재계근 가능 정책)
```

---

## 6. 핵심 코드

### 차이점 #1 — 바코드 중복 검사 우회

#### 위치 1: 같은 상품 재스캔 시 (BixolonShipmentActivity.java:L1188-1120)

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

#### 위치 2: 일반 BL 스캔 시 (BixolonShipmentActivity.java:L1308-1326)

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

---

### 차이점 #2 — find_work_info 다중 매핑 누적 (BixolonShipmentActivity.java:L1839-1851)

```java
if(Common.searchType.equals(SEARCH_TYPE_NONFIXED)){
    work_item_bi_info = bi;
    edit_product_name.setText(bi.getITEM_NAME_KR());
    edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
    if(count == 0){
        pp_code = bi.getPACKER_PRODUCT_CODE();
    }else{
        pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();  // ★ 다중 매핑 누적
    }
    Log.i(TAG, "===================pp_code=================" + pp_code);
    work_item_barcodegoods = bg;
    count++;
}
```

정량(searchType=0)은 별도 조건 없이 첫 번째 매칭 결과만 pp_code에 설정한다.
비정량은 한 바코드에 매핑된 모든 품목을 `', '`로 누적하여 `search_production_nonfixed.jsp`의 WHERE 절에서 `IN ('A', 'B', ...)` 형태로 사용한다.

---

### 차이점 #3 — 라벨 출력 메서드 동일 (BixolonShipmentActivity.java:L1700-1705)

```java
}else if(Common.searchType.equals(SEARCH_TYPE_EMART)){
    Log.d(TAG, "===========이마트 출력 시작 ================");
    labelPrintHelper.setPrinting(weight_double, arSM.get(current_work_position), false, making_date, work_item_bi_info, arSM.get(current_work_position), Common.searchType, printerCallback);
}else if(Common.searchType.equals(SEARCH_TYPE_NONFIXED)){
    Log.d(TAG, "===========이마트(비정량) 출력 시작 ================");
    labelPrintHelper.setPrinting(weight_double, arSM.get(current_work_position), false, making_date, work_item_bi_info, arSM.get(current_work_position), Common.searchType, printerCallback);
}
```

정량과 비정량 모두 `setPrinting()` 동일 메서드를 호출한다.
메서드 내부에서 `BARCODE_TYPE` switch case로 분기하여 M0/M1(정량) vs M8(비정량) 라벨 레이아웃을 구분한다.
원본 대비 시그니처가 확장된 것(work_item_bi_info, Common.searchType, printerCallback 추가)은 Bixolon 프린터 전환에 따른 허용 차이다.

---

### 차이점 #4 — 계근 데이터 전송 URL 차이 (BixolonShipmentActivity.java:L2742-2748)

```java
if(Common.searchType.equals(SEARCH_TYPE_EMART)) {
    // 정량: 단건 INSERT
    result = HttpHelper.getInstance().sendData(packet, "goodswet_insert", Common.URL_INSERT_GOODS_WET);
}else if(Common.searchType.equals(SEARCH_TYPE_HOMEPLUS)){
    result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS);
}else if(Common.searchType.equals(SEARCH_TYPE_PRODUCTION) || Common.searchType.equals(SEARCH_TYPE_NONFIXED)|| Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)|| Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)){
    Log.i(TAG, "===================send packet 확인==================" + packet);
    // 비정량/생산/생산라벨: 배치 INSERT
    result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_NEW);
}
```

- 정량(0): `URL_INSERT_GOODS_WET` → `insert_goods_wet.jsp` (단건 INSERT)
- 비정량(4): `URL_INSERT_GOODS_WET_NEW` → `insert_goods_wet_new.jsp` (배치 INSERT 가능)
- 개발37에서 `insert_goods_wet_new.jsp` MSSQL 전환 완료

---

### 차이점 #5 — complete_shipment 호출 여부

현재 `BixolonShipmentActivity.java`에는 `complete_shipment` / `URL_UPDATE_SHIPMENT` 호출 코드가 존재하지 않는다.
원본 `ShipmentActivity.java` L3509-3524에는 다음과 같이 구현되어 있다.

```java
// 원본 ShipmentActivity.java L3513-3522
if (Common.searchType.equals("0") || Common.searchType.equals("2")) {
    receiveData = HttpHelper.getInstance().sendDataDb(completeStr, "inno", "complete_shipment", Common.URL_UPDATE_SHIPMENT);
} else if (Common.searchType.equals("1")) {
    receiveData = HttpHelper.getInstance().sendDataDb(completeStr, "inno", "complete_shipment", Common.URL_UPDATE_SHIPMENT);
} else if (Common.searchType.equals("3")||Common.searchType.equals("4")||Common.searchType.equals("5")) {
    //도매계근은 아래 URL을 호출하지 않는다. GI_D_ID별 CHECK_YN으로 대상을 구분하는데 아래 URL이 CHECK_YN을 N으로 꺾어버리기 때문에 박스 일부 재계근이 불가능해짐
    //receiveData = HttpHelper.getInstance().sendDataDb(completeStr, "inno", "complete_shipment", Common.URL_UPDATE_SHIPMENT);
    receiveData = "s";  // ★ 호출 안 함, 재계근 가능
}
```

원본 기준으로도 비정량(4)/도매(3)/홈플러스비정량(5)은 `complete_shipment`를 호출하지 않고 `"s"`를 직접 반환한다.
현재 코드에서는 정량(0)도 `W_GOODS_ID` 미존재 대응으로 해당 호출이 제거되어 있다.
비정량의 미호출 정책은 원본과 현재 모두 동일하게 유지된다.

---

## 7. 원본 비교

원본 파일: `D:\PDA\PDA-INNO(원본)\app\src\main\java\com\rgbsolution\highland_emart\ShipmentActivity.java`

| # | 검증 항목 | 원본 (ShipmentActivity) | 현재 (BixolonShipmentActivity) | 차이 | 판정 |
|:-:|---|---|---|---|:---:|
| 1 | 중복 검사 우회 조건 (위치 1) | L676-678: `"4" \|\| "5"` | L1190-1192: 명명 상수 사용 | 리터럴 → 상수화 (의미 동일) | PASS |
| 1 | 중복 검사 우회 동작 (위치 1) | `dup = false;` | `dup = false;` | 완전 일치 | PASS |
| 1 | 중복 검사 우회 주석 (위치 1) | "비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외" | 완전 일치 | - | PASS |
| 1 | 중복 검사 우회 조건 (위치 2) | L879-881: `"4" \|\| "5"` | L1311-1313: 명명 상수 사용 | 상수화 (의미 동일) | PASS |
| 1 | 중복 시 Toast 메시지 | "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요." | 완전 일치 | - | PASS |
| 1 | 중복 시 진동 | `vibrator.vibrate(1000)` | 완전 일치 | - | PASS |
| 2 | find_work_info 비정량 조건 | L1372: `"4"` 조건 | L1839: `SEARCH_TYPE_NONFIXED` 상수 | 상수화 (의미 동일) | PASS |
| 2 | pp_code 누적 로직 | L1379: `pp_code + "', '" + ...` | L1846: `pp_code + "', '" + ...` | 완전 일치 | PASS |
| 2 | count++ 누적 | `count++` | `count++` | 완전 일치 | PASS |
| 3 | 라벨 출력 메서드명 | L1227-1232: `setPrinting(...)` | L1700-1705: `setPrinting(...)` | 동일 메서드 | PASS |
| 3 | 라벨 출력 시그니처 | `(weight, sm, false, making_date)` | `(weight, sm, false, making_date, bi_info, sm, searchType, callback)` | Bixolon 전환 허용 확장 | PASS (허용) |
| 4 | 정량 계근 URL | L3465: `URL_INSERT_GOODS_WET` (sendData) | L2743: `URL_INSERT_GOODS_WET` (sendData) | 완전 일치 | PASS |
| 4 | 비정량 계근 URL | L3468: `URL_INSERT_GOODS_WET_NEW` (sendDataDb) | L2748: `URL_INSERT_GOODS_WET_NEW` (sendDataDb) | 완전 일치 | PASS |
| 5 | 비정량 complete_shipment | L3518-3522: `receiveData = "s"` (미호출) | 호출 코드 자체 없음 | 미호출 정책 동일 | PASS |
| 5 | 정량 complete_shipment | L3513-3515: `URL_UPDATE_SHIPMENT` 호출 | 현재: W_GOODS_ID 미존재 대응으로 제거 | 허용 차이 (정량 전용 변경) | PASS (허용) |

**결론: 5개 차이점 모두 원본과 동일한 의도된 기능. MSSQL 전환 시 의도적 변경 없음. 모두 오류 아님.**

---

## 8. 비정량 운영 정책 종합

비정량(searchType=4) = "정량보다 더 유연한 처리"

| 정책 | 정량(0) | 비정량(4) | 사유 |
|------|---------|----------|------|
| 바코드 중복 검사 | 적용 (동일 바코드 재스캔 차단) | 우회 (`dup = false`) | 박스별 중량 다양성 — 같은 품목이어도 바코드 조합이 다양하게 나올 수 있음 |
| 품목 매핑 방식 | 단일 매칭 (첫 번째만) | 다중 매핑 누적 (`pp_code` 콤마 누적) | 한 바코드에 여러 품목이 매핑될 수 있는 구조 |
| 라벨 출력 메서드 | `setPrinting()` | `setPrinting()` (동일) | 메서드 내부 BARCODE_TYPE switch로 분기 |
| 계근 전송 URL | `URL_INSERT_GOODS_WET` (단건) | `URL_INSERT_GOODS_WET_NEW` (배치) | 배치 처리 필요 시 신규 JSP 사용 |
| 출하 완료 처리 | (현재 코드: W_GOODS_ID 미존재로 제거) | complete_shipment 미호출 | GI_D_ID별 CHECK_YN으로 대상 구분 — 호출 시 CHECK_YN이 N으로 꺾여 박스 일부 재계근 불가 |

---

## 9. 주의사항

- **5개 차이점 모두 오류가 아님**: 비정량 운영 정책에 따른 의도된 기능이다. 원본 ShipmentActivity.java에도 동일하게 구현되어 있으며, 코드 주석으로도 명시되어 있다.
- **임의 변경 금지**: 비정량 중복 검사 우회 분기(`dup = false`), pp_code 누적 로직, URL 분기, complete_shipment 미호출을 제거하거나 조건을 변경하면 비정량 계근 기능이 정상 동작하지 않는다. 원본 동작 100% 유지 원칙에 따라 모두 보존해야 한다.
- **pp_code 누적과 JSP 연동**: 비정량 pp_code 누적(`', '` 연결)은 `search_production_nonfixed.jsp`의 WHERE 절 `IN ('A', 'B', ...)` 구조와 정확히 연동된다. 누적 로직 변경 시 JSP 쿼리와의 정합성이 깨진다.
- **배치 URL 전환 완료**: `URL_INSERT_GOODS_WET_NEW` → `insert_goods_wet_new.jsp`는 개발37에서 MSSQL 전환 완료되었다. 정량(`URL_INSERT_GOODS_WET`) → `insert_goods_wet.jsp`도 별도로 MSSQL 전환 완료된 상태다.
- **`gi_l_id` 추가는 허용 차이**: 현재 `duplicatequeryGoodsWet`에 `gi_l_id` 파라미터가 추가된 것은 LOT 단위 분리를 위한 기능 확장이며, 중복 검사 우회 로직 자체에는 영향을 주지 않는다.
- **searchType 1(생산), 7(생산라벨)**: 계근 흐름 자체가 다르므로 이 문서의 분석 대상에 포함되지 않는다.
- **searchType 5(홈플러스 비정량)**: 중복 검사 우회(#1) 정책은 4와 동일하게 적용된다. 계근 URL도 searchType=4와 동일하게 `URL_INSERT_GOODS_WET_NEW`를 사용한다.
- **라벨 출력 시그니처 확장은 허용 차이**: 원본 대비 `work_item_bi_info`, `Common.searchType`, `printerCallback` 파라미터가 추가된 것은 Bixolon 프린터 전환에 따른 필수 변경이며, 동작 의미는 동일하다.

---

## 10. 관련 문서

- `app/doc/소스분석/49_출하대상받기_4유형_조회조건_종합정리.md` — 4유형(이마트/홈플러스/도매/롯데) 조회 조건 종합 정리
- `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md` — 비정량 출하 계근 전체 흐름 (이 분기가 속하는 상위 흐름)
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` — 이마트 정량 출하 계근 전체 흐름 (중복 검사 적용 대상)
- `app/doc/소스분석/35_로컬DB_구조.md` — TB_GOODS_WET 테이블 구조 (중복 검사 대상 테이블)
- `app/doc/개발/37_비정량_계근데이터전송_JSP_MSSQL전환.md` — 차이점 #4 — 비정량 새 JSP(`insert_goods_wet_new.jsp`) MSSQL 전환
- `app/doc/오류/21_비정량_이마트_EMARTLOGIS_CODE_빈문자열_라벨출력_예외.md` — 비정량 라벨 출력 별도 이슈 (해결됨)
- `app/doc/문의사항/03_M8_비정량_이마트_pBarcode2_생성_미출력_의도.md` — 차이점 #3 라벨 출력 차이 관련
