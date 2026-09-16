# BixolonShipmentActivity searchType별 클래스 분리

**작성일**: 2026-09-16
**목적**: `BixolonShipmentActivity.java`(3,738줄)의 계근 흐름을 `shipment/type/` 폴더의 **searchType별 파일 6개**로 나누고, Activity가 그 파일들을 호출하도록 바꾼다. 나눈 뒤 6개 파일을 비교해 완전히 동일한 블록만 공통 파일로 뽑는다. 기존 동작은 100% 동일하게 유지한다.

> **기존 문서 대체**
> `64_BixolonShipmentActivity_마트사별_클래스_분리.md`, `65_setBarcodeMsg_마트사별_분리.md`를 이 문서가 대체한다.
> 두 문서는 **마트사 5개 클래스 + 정량/비정량 플래그** 구성이나, 소스 확인 결과 이마트 0↔4가 7곳, 홈플러스 2↔5가 3곳 다르므로 플래그로 묶을 수 없다(§1.3).
> 또한 두 문서의 사실관계 오류 12건을 §10에 정리했다. **64·65는 참조하지 않는다.**

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

### 추가 제약 조건 (이 가이드 한정)

1. **생산(searchType 1, 7) 코드는 그대로 유지한다.** 파일을 만들지 않고, `setBarcodeMsgProduction`(1577~1836)과 생산 전용 분기(518~523, 558, 564, 1145~1148)를 Activity에 그대로 둔다. 생산 줄은 **수정 0줄**을 목표로 한다.
2. **기계적 복사만 한다.** 원본 줄을 그대로 옮기고, §1.3 표에 있는 `searchType` 조건만 접는다. 로그·주석·의미 없어 보이는 줄까지 그대로 유지한다.
3. **§1.3 표에 없는 조건은 접지 않는다.** `ITEM_TYPE` 블록(1366~1532), 킬코이·미트센터 판정(1285~1292), 센터명 판정(1293)은 `searchType` 게이트가 아니라 **데이터에 따라 갈리는 분기**다. VIEW 실측으로 증명하기 전에는 전 타입에 그대로 둔다.
4. **이관하지 않은 타입은 `git diff`에 한 줄도 나오지 않아야 한다.** 나오면 분류가 틀린 것이므로 되돌린다.
5. **공통 추출은 Step 12에서만 한다.** 판단으로 묶지 않고, 6개 파일을 diff해서 **완전히 동일한 블록만** 올린다.
6. 원본(`D:\PDA\PDA-INNO(원본)`)과 동작이 다른 코드를 발견해도 이 문서에서는 고치지 않는다. `app/doc/오류/`에 별건으로 문서화만 한다.
7. **이 문서 본문의 모든 라인 번호는 Step 0(죽은 코드 제거) 이전 기준이다.** Step 0에서 294줄이 삭제되어 현재 파일과 어긋난다. Step 1 이후 작업 시 §8 Step 0 "Part 6. 변경 내용"의 **오프셋 표로 환산**한 뒤 해당 구간을 직접 확인하고 착수한다.

---

## 1. 현재 구조

### 1.1 BixolonShipmentActivity.java (3,738줄)

한 파일에 역할 10가지가 섞여 있다.

| # | 역할 | 라인 | searchType 분기 |
|:-:|---|---|:-:|
| 1 | 상수·필드 (4곳에 분산) | 154~390, 1100~1112, 2873, 3344~3356 | – |
| 2 | 생명주기·프린터 연결 (`onCreate`/`onStart`/`onDestroy`, `mBixolonHandler`, `ProgressDlgPrintConnect`, `ProgressDlgDiscon`, `onActivityResult`, `sendData`) | 411~605, 980~1041, 2467~2477, 3171~3339 | 생산 3곳 |
| 3 | 입력 진입점 (`setMessage`, 키보드 입력, `inputBtnListener`, 스피너 리스너 3종) | 451~470, 630~734, 1078~1093, 2222~2322 | 수기 2곳 |
| 4 | 바코드 스캔 (`setBarcodeMsg` 404줄 / `setBarcodeMsgProduction` 260줄) | 1143~1836 | 6곳 |
| 5 | 상품 매칭 (`find_PackerProduct`, `find_PackerProductBarcodeGoods`, `find_work_info`, `find_work_info_barcodeGoods`) | 2059~2220 | 1곳 |
| 6 | 계근 저장·화면 반영 (`wet_data_insert`, `calc_info`) | 1884~2032, 2324~2346 | 9곳 |
| 7 | 출하대상 조회 (`ProgressDlgShipSelect`) | 2500~2683 | 1곳 |
| 8 | 서버 전송 (`ProgressDlgShipmentSend`) | 2899~3155 | 2곳(내부 URL 분기 11곳) |
| 9 | 라벨 출력 분기 (계근 시 / 재출력) | 2005~2021, 934~951 | 2곳 |
| 10 | 다이얼로그 (상세·삭제·합계라벨 SLCS·완료·경고) | 3358~3733 | – |

### 1.2 호출하는 곳이 없는 코드

Grep으로 전체 소스(`app/src/main/java`)를 확인한 결과다.

| 대상 | 라인 | 줄수 | 확인 |
|---|---|---:|---|
| `ProgressDlgShipSelectBL` | 2694~2870 | 177 | `new ProgressDlgShipSelectBL` 호출 0건 |
| `show_wetNextDialog` | 3642~3672 | 31 | 호출 0건 (2847은 주석) |
| `find_BL` | 2094~2104 | 11 | 호출 0건 |
| `scanFlag_swap` | 2044~2051 | 8 | 호출 0건 (2086·2831·2846·2855는 주석) |
| `slcsBarcode` / `slcsLine` / `slcsBox` | 2405~2439 | 35 | 호출 0건 |
| 미사용 import | 51~59 | 8 | `ByteArrayOutputStream`, `IOException`, `DecimalFormat`, `ParseException`, `SimpleDateFormat`, `Calendar`, `Date`, `Set` |
| 도달 불가 분기 | 3057, 3059 | – | 2938에서 이마트·홈플러스를 이미 걸러 3015 블록에 진입할 수 없음 |

합계 294줄. **그대로 두면 Step 2~7에서 6벌로 복사될 수 있다**(§8 Step 0 참조).

> ✅ **Step 0에서 위 6건을 전부 제거 완료**(2026-09-16). 3,739줄 → 3,445줄. 빌드 통과 확인.
> 단, `slcsInit`·`slcsLabelSize`·`slcsText`·`slcsPrint`·`slcsFeedToMark` 5개는 합계 라벨에서 사용 중이라 남겼고,
> 3057·3059의 도달 불가 분기도 남겼다.

### 1.3 searchType별 차이 — 전수 12곳

생산(1, 7)은 유지 대상이므로 제외했다. **이 표가 Step 2~11에서 접을 수 있는 조건의 전부다.**

| # | 차이점 | 라인 | 0 이마트 | 4 이마트비정량 | 2 홈플 | 5 홈플비정량 | 3 도매 | 6 롯데 |
|:-:|---|---|:-:|:-:|:-:|:-:|:-:|:-:|
| 1 | 중복검사 우회 | 1205, 1329 | | ● | | ● | | |
| 2 | 바코드 전부 매칭 | 2150 | | ● | | | | |
| 3 | 트레이더스 소비기한 필수 | 1294 | ● | | | | | |
| 4 | LB 환산 ZEROPOINT 자릿수 (S·B) | 1443, 1505 | ● | | | | | |
| 5 | 수기 중량 소수 1자리 절사 | 660 | ● | | | | | |
| 6 | 수기 입력 시 소비기한 창 | 703~704 | ●(TRD 센터) | | | | | ●(항상) |
| 7 | 계근 INSERT 방식 | 1918~1934 | 일반 | 일반 | 홈플(MaxBoxOrder) | 일반 | 일반 | 롯데(박스순번) |
| 8 | 계근중량 반올림 | 1940, 1954, 1972, 1982 | 1자리 | 3자리 | 3자리 | 3자리 | 3자리 | 3자리 |
| 9 | 계근 시 라벨 | 2006~2020 | 이마트 | 이마트 | 홈플 | 홈플 | **없음** | 롯데 |
| 10 | 재출력 라벨 | 939~951 | 이마트 | 이마트 | 홈플 | 홈플 | **이마트(else)** | 롯데 |
| 11 | 롯데 박스순번 카운터 | 2550~2566 | | | | | | ● |
| 12 | 전송 방식 / JSP | 2938, 3015 | 건별 / `insert_goods_wet` | 일괄 / `_new` | 건별 / `insert_goods_wet` | 일괄 / `_new` | 일괄 / `_new` | 건별 / `insert_goods_wet` |

추가로 레이아웃 분기(422, 도매만 `activity_shipment_wholesale`)가 있으나 **Activity에 남긴다**(위젯 바인딩보다 먼저 실행돼야 함). 레이아웃 2종의 위젯 id는 완전히 동일함을 확인했다.

**표에서 읽어야 할 것**

- 이마트 0과 4는 **7곳**(#1·2·4·5·6·8·12)이 다르다. 홈플러스 2와 5는 **3곳**(#1·7·12)이 다르다. → 회사별로 묶고 플래그로 나누면 파일 안에 분기가 다시 생긴다. **searchType별 6개 파일이 맞다.**
- 도매(3)는 차이가 가장 적다(#9·10·12뿐). → **첫 이관 대상**으로 삼아 골격을 검증한다.

### 1.4 데이터에 따라 갈리는 분기 — 접지 말 것

| 분기 | 라인 | 근거 |
|---|---|---|
| 킬코이 PACKER_CODE + 미트센터 STORE_CODE 소비기한 검증 | 1285~1292 | `searchType` 조건 없음. 전 타입이 진입 가능 |
| 센터명(용인/대구/시화(W)/여주TRD, E/T, WET) 진입 | 1293 | `searchType` 조건 없음. 안쪽 1294만 이마트 게이트 |
| `ITEM_TYPE` W/HW · S · J | 1366, 1419, 1470 | VIEW가 내보내는 값에 따라 갈림 |
| `ITEM_TYPE` B | 1481 | 상수 주석만 "홈플러스 비정량"이고 코드로 강제하지 않음 |

`ITEM_TYPE` 블록은 **W/HW·S·J가 3단 else-if 체인**이고 **B는 별도 `if`문**이다. 값이 상호배타적이라 결과는 같지만, 옮길 때 구조를 `else if`로 바꾸지 않는다.

### 1.5 중요한 동작 — 반드시 보존

| 동작 | 라인 | 내용 |
|---|---|---|
| 재귀 호출 | 1226~1227 | 같은 상품 재스캔 시 `lastBarcodeProcessedTime = 0`으로 디바운스를 우회하고 자기 자신을 호출해 BL 스캔 경로를 탄다. 이관 후에는 **자기 파일의 메서드**를 호출해야 한다 |
| 중복검사 메서드 2종 | 1193·1203 / 1326 | 상품스캔은 `duplicatequeryGoodsWet_check`(2인자), BL스캔은 `duplicatequeryGoodsWet`(5인자). **이름이 비슷한 다른 메서드**다 |
| 중복검사 선조회 | 1203·1326 → 1205·1329 | 원본은 조회한 뒤 `dup = false`로 덮는다. "어차피 false니 건너뛰자"고 최적화하면 동작이 바뀐다 |
| 1193의 미사용 조회 | 1193 | 결과를 쓰지 않지만 호출 자체는 유지한다 |
| `current_work_position == -1` 접근 순서 | 1277·1283·1305 | -1이 될 수 있는데 1283에서 먼저 `arSM.get()`에 접근하고 -1 체크는 1305에 있다. **원본과 동일**하므로 순서를 바꾸지 않는다 |
| `onActivityResult` fall-through | 3267~3283 | `REQUEST_ENABLE_BT`에 `break`가 없어 `GET_DATA_REQUEST`로 넘어간다. 원본 동작이므로 유지한다 |
| `find_PackerProduct` 항상 참 조건 | 2065, 2082 | `Editable`과 `String` 비교라 항상 true다. "고치면" 동작이 바뀐다 |

### 문제점

1. **한 파일에 역할 10가지** — 3,738줄, `searchType` 분기 약 30곳이 7개 구간에 흩어져 있다. 한 타입을 고치면 다른 타입이 조용히 깨진다(오류 32·33·34·36이 모두 이 유형)
2. **복사 분리의 전례** — 생산을 `setBarcodeMsgProduction`으로 떼어낸 결과 유효 250줄 중 201줄(80%)이 원본과 중복됐다
3. **상태가 필드로 오간다** — `setBarcodeMsg` 한 곳에서 Activity 멤버 20종을 직접 읽고 쓴다
4. **호출처 없는 코드 약 270줄**(§1.2)이 남아 있다

---

## 2. 변경 구조

### 데이터 흐름 — 변경 전

```
[바코드 스캔] HoneywellScannerActivity.setMessage(1078)
    ↓
BixolonShipmentActivity.setBarcodeMsg(1143)            404줄
    ├ if (PRODUCTION) → setBarcodeMsgProduction()      260줄 (중복 201줄)
    ├ if (NONFIXED || HP_NONFIXED) → 중복확인 제외
    ├ if (EMART) → 트레이더스 소비기한 검증
    ├ if (EMART) → LB 환산 자릿수
    └ wet_data_insert(1884) ─ searchType 분기 9곳
                           └ ProgressDlgShipmentSend(2899) ─ 분기 2곳
```

### 데이터 흐름 — 변경 후

```
[바코드 스캔] HoneywellScannerActivity.setMessage(1078)
    ↓
BixolonShipmentActivity.setBarcodeMsg
    ├ if (PRODUCTION) → setBarcodeMsgProduction()      ★ 생산은 그대로 유지
    └ shipmentType.onBarcodeScanned(msg)               ← switch 없음
         ↓
    [WholesaleType / LotteType / HomeplusType /
     HomeplusNonfixedType / EmartNonfixedType / EmartType]  중 하나
         ├ 자기 타입의 흐름만 보유 (searchType 조건 없음)
         ├ activity.xxx()  미이관 메서드 호출
         └ Step 12 이후 → common/ 의 공통 블록 호출
```

### 클래스 구성

```
com/rgbsolution/highland_emart/shipment/type/
  ShipmentType.java               ← 인터페이스
  ShipmentTypeFactory.java        ← searchType → 구현체. searchType 읽는 유일한 곳
  WholesaleType.java         (3)  도매 출하
  LotteType.java             (6)  롯데 출하 (박스순번 카운터 보유)
  HomeplusType.java          (2)  홈플러스 출하
  HomeplusNonfixedType.java  (5)  홈플러스 비정량
  EmartNonfixedType.java     (4)  이마트 비정량
  EmartType.java             (0)  이마트 출하

  common/                         ← Step 12에서 생성. 6개 diff 결과만
```

생산(1, 7)은 파일을 만들지 않는다.

### 접근 방식 — 먼저 나누고, 나중에 공통화

| 단계 | 내용 | 산출물 |
|:-:|---|---|
| A (Step 2~11) | 6개 타입 파일에 흐름을 **기계적으로 복사**하고, §1.3 표의 조건만 접는다 | 타입별 파일 6개. 이 시점에는 중복이 존재한다 |
| B (Step 12) | 6개 파일을 diff해서 **완전히 동일한 블록만** `common/`으로 올린다 | 공통 파일 |

A 단계에서는 **중복을 허용한다.** 판단으로 공통화하면 원본과 대조가 불가능해지기 때문이다. 대신 B를 A 직후에 붙여 중복 기간을 짧게 한다.

### 조건 접기 규칙

```java
// 예: EmartType(0) — 조건이 항상 참
if (Common.searchType.equals(SEARCH_TYPE_EMART)) {     ← 이 줄만 삭제
    item_weight_double = Math.floor(temp_weight_double * item_pow) / item_pow;
} else {                                                ← else 블록 전체 삭제
    item_weight_double = Math.floor(temp_weight_double * 100) / 100;
}

// 예: LotteType(6) — 조건이 항상 거짓
if (Common.searchType.equals(SEARCH_TYPE_NONFIXED)
        || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)) {
    dup = false;                                        ← 블록 전체 삭제
}
```

**들여쓰기는 조정하되 줄 내용은 바꾸지 않는다.** 조건을 접은 곳은 원본 라인 번호를 주석으로 남긴다(`// 원본 1443 (EMART 분기)`).

### 목표 지표

| 지표 | 현재 | Step 11 후 | Step 13 후 |
|---|---:|---:|---:|
| `Common.searchType` 참조 지점 (생산 제외) | 약 30 | 1 (Factory) | 1 |
| `BixolonShipmentActivity` 줄 수 | 3,738 | 약 2,400 | 약 1,200 |
| 타입 A 수정이 타입 B에 미치는 영향 | 있음 | 없음 | 없음 |
| 타입 파일 간 중복 | – | 존재(허용) | Step 12에서 제거 |

---

## 3. 수정 대상 파일

### 3.1 추가 파일 (신규 생성) — 8개

경로는 전부 `app/src/main/java/com/rgbsolution/highland_emart/shipment/type/` 이다.

| # | 파일 | 내용 | 착수 Step |
|:-:|------|------|:-:|
| 1 | **ShipmentType.java** | 인터페이스. 메서드 6개 선언 | 1 |
| 2 | **ShipmentTypeFactory.java** | searchType 6종 → 구현체 6종 | 1 |
| 3 | **ShipmentConst.java** | 타입 구현체 공용 상수(ITEM_TYPE, 킬코이·미트센터, 센터명, 디바운스, 롯데 박스순번 최대값). 값은 Activity와 동일 | 1 |
| 4 | **WholesaleType.java** (3) | 도매. 차이 3곳(#9·10·12) | 2 |
| 5 | **LotteType.java** (6) | 롯데. 박스순번 카운터 보유 | 3 |
| 6 | **HomeplusType.java** (2) | 홈플러스 정량 | 4 |
| 7 | **HomeplusNonfixedType.java** (5) | 홈플러스 비정량 | 5 |
| 8 | **EmartNonfixedType.java** (4) | 이마트 비정량 | 6 |
| 9 | **EmartType.java** (0) | 이마트 정량. 가장 크고 위험 → 마지막 | 7 |

`common/` 하위 파일은 Step 12에서 diff 결과를 보고 결정한다. **지금 설계하지 않는다.**

### 3.2 기존 소스 → 타입 파일 이관 매핑

| 축 | 기존 위치 | 라인 | 타입당 줄수 | 차이점(§1.3) | Step |
|---|---|---|---:|---|:-:|
| 바코드 스캔 | `setBarcodeMsg` 본문 | 1150~1545 | 약 400 | #1·3·4 | 2~7 |
| 계근 저장 | `wet_data_insert` | 1884~2032 | 약 150 | #7·8·9 | 8 |
| 상품 매칭 | `find_PackerProduct`, `find_PackerProductBarcodeGoods`, `find_work_info`, `find_work_info_barcodeGoods` | 2059~2220 | 약 160 | #2 | 9 |
| 전송 | `ProgressDlgShipmentSend.doInBackground` | 2919~3125 | 약 210 | #12 | 10 |
| 수기 입력 | `inputBtnListener` 수기 분기 | 638~729 | 약 90 | #5·6 | 11 |
| 재출력 라벨 | `mHandler` MESSAGE_REPRINT | 934~951 | 약 15 | #10 | 8 |
| 조회 후처리 | `ProgressDlgShipSelect.doInBackground` 롯데 블록 | 2549~2566 | 약 18 | #11 | 8 |

### 3.3 Activity 잔류 (이관하지 않음)

| 항목 | 라인 | 사유 |
|---|---|---|
| **생산 전용 전체** | 518~523, 558, 564, 1145~1148, 1577~1836 | **생산 코드 유지 지시**(추가 제약 1) |
| 생명주기 | 411~605 | 안드로이드 프레임워크 콜백 |
| 레이아웃 분기 | 422~426 | 위젯 바인딩보다 먼저 실행돼야 함 |
| `setBarcodeMsg(String)` 시그니처 | 1143 | 호출부 6곳(461, 637, 731, 1085, 1090, 1227)이 참조. 본문만 위임으로 교체 |
| 위젯 필드·`findViewById` | 283~330, 446~502 | 이 문서 범위 밖. 타입 파일은 `activity` 경유로 접근 |
| 상태 필드 | 293, 336~389, 1100~1112 | 이 문서 범위 밖. Step 13에서 재검토 |
| Handler 2종 | 865~1041 | 프린터 콜백. MESSAGE_REPRINT 분기만 Step 8에서 위임 |
| AsyncTask 껍데기 | 2500~2683, 2899~3155 | 클래스 분리는 범위 밖. 타입별 분기만 위임 |
| 다이얼로그 전체 | 3358~3733 | `searchType` 분기 없음 |
| `slcs*` 5개 | 2364~2404, 2447~2460 | `show_wetDetailDialog`(3453~3520)가 사용 중 |
| `calc_info` | 2324~2346 | `searchType` 분기 없음 |

### 3.4 미변경 파일

`LabelPrintHelper.java`, `DBHandler.java`, `HttpHelper.java`, `Common.java`, `Shipments_Info.java`, `Barcodes_Info.java`, `Goodswets_Info.java`, `HoneywellScannerActivity.java`, `BixolonSocketPrinter.java`, `ShipmentListAdapter.java`, `DetailAdapter.java`, `ProductionActivity.java`, `ShipmentActivity.java`(미사용), JSP 전체, 레이아웃 XML 전체, `AndroidManifest.xml`

---

## 4. 수정 상세

### 4.1 ShipmentType.java (신규)

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/shipment/type/ShipmentType.java`

```java
public interface ShipmentType {
    /** 바코드 스캔 진입점 — 원본 setBarcodeMsg 본문(1150~1545) */
    void onBarcodeScanned(String msg);

    /** 계근 저장 — 원본 wet_data_insert(1884~2032) */
    void onWeightConfirmed(String weightStr, double weightDouble, String makingDate, String boxSerial);

    /** 상품 매칭 — 원본 find_PackerProduct 계열(2059~2220) */
    String findPackerProduct(String barcode, int workFlag);

    /** 재출력 라벨 — 원본 mHandler MESSAGE_REPRINT(934~951) */
    void reprintLabel(String weightStr, String makingDate, String boxOrder, int selectPosition);

    /** 조회 후처리 — 원본 ProgressDlgShipSelect 롯데 블록(2549~2566) */
    void onShipmentLoaded(ArrayList<Shipments_Info> arSM);

    /** 서버 전송 — 원본 ProgressDlgShipmentSend.doInBackground(2919~3125) */
    String send(Context context, ArrayList<Goodswets_Info> listSendInfo, ArrayList<Shipments_Info> arSM);
}
```

수기 입력(Step 11)은 Step 11 착수 시 메서드를 추가한다. **미리 선언하지 않는다** — 빈 구현이 6벌 생긴다.

**검증**: 구현체 6개가 전부 구현해야 컴파일된다. 타입 추가 시 누락은 컴파일 에러로 잡힌다

### 4.2 ShipmentTypeFactory.java (신규)

```java
public class ShipmentTypeFactory {
    public static ShipmentType create(String searchType, BixolonShipmentActivity activity) {
        switch (searchType) {
            case "0": return new EmartType(activity);
            case "2": return new HomeplusType(activity);
            case "3": return new WholesaleType(activity);
            case "4": return new EmartNonfixedType(activity);
            case "5": return new HomeplusNonfixedType(activity);
            case "6": return new LotteType(activity);
            // 생산(1, 7)은 Activity가 직접 처리 — 여기로 오지 않는다
            default:  throw new IllegalArgumentException("searchType: " + searchType);
        }
    }
}
```

**검증**: 생산(1, 7)으로 이 메서드가 호출되면 안 된다. Activity에서 생산을 먼저 걸러야 한다(4.4)

### 4.3 WholesaleType.java (Step 2 예시)

**원본**: `setBarcodeMsg` 1150~1545

도매에서 접히는 조건은 3개다(전부 "항상 거짓").

| 원본 라인 | 조건 | 도매 | 처리 |
|---|---|:-:|---|
| 1205 | `NONFIXED \|\| HP_NONFIXED` | 거짓 | 블록 삭제 |
| 1294 | `EMART` | 거짓 | 블록 삭제 (바깥 1293 `else if`는 유지) |
| 1329 | `NONFIXED \|\| HP_NONFIXED` | 거짓 | 블록 삭제 |
| 1443, 1505 | `EMART` | 거짓 | `if`문 삭제, `else` 본문만 남김 |

**변경 전** (1326~1344)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet(getApplicationContext(), work_item_fullbarcode,
        arSM.get(current_work_position).getGI_D_ID(), ...);

if (Common.searchType.equals(SEARCH_TYPE_NONFIXED) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)) {
    dup = false;
}
...
if (dup) { ... return; }
```

**변경 후** (`WholesaleType.onBarcodeScanned`)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet(activity.getApplicationContext(), activity.work_item_fullbarcode,
        activity.arSM.get(activity.current_work_position).getGI_D_ID(), ...);

// 원본 1329 (NONFIXED || HP_NONFIXED 우회) — 도매 미해당으로 접음
...
if (dup) { ... return; }
```

**변경 전** (1437~1451, `ITEM_TYPE S`)

```java
if ("LB".equals(work_item_bi_info.getBASEUNIT())) {
    double temp_weight_double = item_weight_double * 0.453592;
    if (Common.searchType.equals(SEARCH_TYPE_EMART)) {
        item_weight_double = Math.floor(temp_weight_double * item_pow) / item_pow;
    } else {
        item_weight_double = Math.floor(temp_weight_double * 100) / 100;
    }
    ...
}
```

**변경 후**

```java
if ("LB".equals(bi.getBASEUNIT())) {
    double temp_weight_double = item_weight_double * 0.453592;
    // 원본 1443 (EMART 자릿수 분기) — 도매는 else 경로
    item_weight_double = Math.floor(temp_weight_double * 100) / 100;
    ...
}
```

**검증**
- 원본 1150~1545와 `WholesaleType`을 나란히 두고 diff → **삭제된 줄이 전부 위 표의 4개 조건 블록인지** 확인
- `ITEM_TYPE` W/HW·S·J·B 블록 4개가 전부 남아 있는지 확인(§1.4)
- 1285~1292(킬코이), 1293(센터명)이 남아 있는지 확인
- 재귀 호출(1227)이 `this.onBarcodeScanned(msg)`인지 확인. `activity.setBarcodeMsg(msg)`이면 안 된다

### 4.4 BixolonShipmentActivity.setBarcodeMsg

**변경 전** (1143~1149)

```java
public void setBarcodeMsg(final String msg) {
    if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) {
        setBarcodeMsgProduction(msg);
        return;
    }
    try {
        ...404줄...
    } catch (Exception ex) { ... }
}
```

**변경 후** (Step 7 완료 시점)

```java
public void setBarcodeMsg(final String msg) {
    if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) {   // ★ 생산 유지 — 수정 없음
        setBarcodeMsgProduction(msg);
        return;
    }
    shipmentType.onBarcodeScanned(msg);
}
```

**Step 2~6 진행 중 (과도기)** — 이관한 타입만 위임하고 나머지는 원본 코드를 그대로 탄다.

```java
public void setBarcodeMsg(final String msg) {
    if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) {
        setBarcodeMsgProduction(msg);
        return;
    }
    if (shipmentType != null && shipmentType.isMigrated()) {   // 과도기 분기. Step 7에서 제거
        shipmentType.onBarcodeScanned(msg);
        return;
    }
    try {
        ...남은 원본...
    } catch (Exception ex) { ... }
}
```

> 과도기 분기는 Step 7에서 반드시 제거한다. `isMigrated()`도 인터페이스에서 지운다.

**onCreate 추가** (446 앞)

```java
if (!Common.searchType.equals(SEARCH_TYPE_PRODUCTION)
        && !Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)) {
    shipmentType = ShipmentTypeFactory.create(Common.searchType, this);
}
```

**검증**: 생산(1, 7)으로 로그인했을 때 `shipmentType`이 `null`이고 기존 경로를 그대로 타는지 logcat으로 확인

### 4.5 타입 파일의 Activity 접근 규칙

타입 파일은 아직 Activity의 필드·위젯·메서드를 참조한다. Step 13 전까지는 정상이다.

| 접근 대상 | 허용 | 예시 |
|---|:-:|---|
| Activity 메서드 호출 | O | `activity.wet_data_insert(...)`, `activity.show_wetFinishDialog()`, `activity.set_scanFlag(false)` |
| Activity 상태 필드 | O (과도기) | `activity.arSM`, `activity.current_work_position` |
| Activity 위젯 | O (과도기) | `activity.edit_barcode`, `activity.sp_bl_no` |
| 다른 타입 파일 참조 | **X** | `EmartType`이 `LotteType`을 부르면 안 된다 |
| 상속·공통 부모 클래스 | **X** | 인터페이스만 공유한다 |

접근을 위해 Activity 필드의 접근제한자를 `private` → `package-private`로 여는 것은 **Step별로 필요한 것만** 연다. `public`으로 열지 않는다.

---

## 5. 사이드이펙트

### 5.1 생산 경로 (최우선 확인)

```java
// 1145 — 이 분기가 먼저 걸러야 shipmentType이 호출되지 않는다
if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) { setBarcodeMsgProduction(msg); return; }
```

- `setBarcodeMsgProduction`은 `find_PackerProduct`(1604), `find_PackerProductBarcodeGoods`(1608), `wet_data_insert`(1824), `ProgressDlgShipSelect`(1626, 1667), `show_wetFinishDialog`(1646), `set_scanFlag`(1641)를 호출한다
- **대응**: Step 8~9에서 `wet_data_insert`·`find_PackerProduct` 계열을 타입 파일로 옮기더라도, **Activity에 같은 이름의 메서드를 남겨** 생산 본문이 수정되지 않게 한다. 남긴 메서드는 비생산일 때 `shipmentType`으로 위임하고, 생산일 때 기존 본문을 그대로 실행한다

### 5.2 AsyncTask 3종의 상태 참조

- `ProgressDlgShipSelect`(2500), `ProgressDlgShipSelectBL`(2694, 미사용), `ProgressDlgShipmentSend`(2899)가 `arSM`, `current_work_position`, `work_ppcode`를 직접 참조한다
- **대응**: 이 문서에서는 클래스를 분리하지 않는다. Step 8·10에서 **타입별 분기만** 위임하고 나머지는 그대로 둔다

### 5.3 DetailAdapter → mHandler MESSAGE_REPRINT

```java
// DetailAdapter.java:156
msg.what = MESSAGE_REPRINT;  mHandler.sendMessage(msg);
```

- 재출력은 어댑터에서 Handler로 들어온다. Step 8에서 `mHandler`의 934~951 분기를 `shipmentType.reprintLabel(...)`로 바꾼다
- **도매(3)의 재출력은 `else` 경로라 이마트 라벨(`setPrinting`)이 나간다.** `WholesaleType.reprintLabel`은 이 동작을 그대로 구현한다(§1.3 #10). 계근 시에는 라벨이 없는데 재출력만 있는 비대칭이 원본 동작이다

### 5.4 HoneywellScannerActivity

```java
public class BixolonShipmentActivity extends HoneywellScannerActivity
```

- 상위 클래스의 콜백은 `setMessage`(`HoneywellScannerActivity.java:202`)이며 1078에서 오버라이드한다. `setBarcodeMsg`는 **상위 클래스에 없는** 이 Activity의 메서드다(64·65 문서의 "콜백이라 변경 금지" 서술은 사실과 다름)
- **대응**: 그래도 호출부가 6곳(461, 637, 731, 1085, 1090, 1227)이므로 시그니처는 유지한다

### 5.5 LabelPrintHelper

- `setPrinting`(이마트), `setHomeplusPrinting`(홈플), `setPrintingLotte`(롯데), `setPrinting_prod`(생산) 4종은 시그니처가 서로 다르다
- 각 타입 파일이 자기 인자를 채워 호출하므로 **`LabelPrintHelper`는 수정하지 않는다**

### 5.6 ShipmentActivity.java (미사용, 4,460줄)

- `BluetoothPrintService`가 상수 4종(`MESSAGE_DEVICE_NAME`, `DEVICE_NAME`, `MESSAGE_TOAST`, `TOAST`)을 참조해 삭제 시 컴파일 에러
- **대응**: 이 문서에서 건드리지 않는다

---

## 6. 데이터 저장 구조

### 6.1 Activity 상태 필드 (실제 필드만)

| 필드 | 선언 | 타입 | 용도 | 타입 파일 참조 |
|---|---|---|---|:-:|
| `arSM` | 293 | ArrayList\<Shipments_Info\> | 출하 대상 목록 | O |
| `current_work_position` | 350 | int | 작업 대상 인덱스 | O |
| `work_item_bi_info` | 1100 | Barcodes_Info | 현재 바코드 정보 | O |
| `work_ppcode` | 1102 | String | 작업 중 패커상품코드 | O |
| `work_bl_no` | 1104 | String | 작업 중 BL번호 | O |
| `work_item_fullbarcode` | 1106 | String | 스캔된 전체 바코드 | O |
| `work_item_barcodegoods` | 1108 | String | 바코드 상품코드 | O |
| `expiryDayTrans` | 1110 | String | 소비기한 전송용 | O |
| `dialog_flag` | 1112 | boolean | 다이얼로그 표시 중 | O |
| `scan_flag` | 364 | boolean | 상품/BL 스캔 차례 | O |
| `work_flag` | 358 | int | 바코드/수기/상품코드 모드 | O |
| `select_flag` | 370 | boolean | 스캔/선택 모드 | X |
| `select_position` | 345 | int | 상세보기 선택 위치 | O (재출력) |
| `centerTotalCount` / `centerWorkCount` | 337, 339 | int | 센터 수량 합계 | O |
| `centerTotalWeight` / `centerWorkWeight` | 341, 343 | double | 센터 중량 합계 | O |
| `lastProcessedBarcode` / `lastBarcodeProcessedTime` | 387, 385 | String / long | 디바운싱 | O |
| `alert_flag` | 376 | boolean | 경고창 중복 방지 | O |
| `lotte_TryCount` | 277 | int | 롯데 박스순번 | **LotteType로 이동** |
| `vibrator` | 373 | Vibrator | 진동 | O |

### 6.2 지역변수 — 필드로 착각하지 말 것

아래는 **메서드 안의 지역변수**다. 64·65 문서가 이들을 상태 필드로 분류했으나 사실과 다르다.

| 이름 | 선언 위치 | 비고 |
|---|---|---|
| `weight_from` / `weight_to` | 1367·1368, 1420·1421, 1482·1483 | `ITEM_TYPE` 블록마다 새로 선언 |
| `temp_weight` | 1402, 1453, 1515 | 블록별 지역변수 |
| `temp_weight_double` | 1392, 1441, 1503 | `if ("LB")` 블록 안 |
| `temp_bl_no` | 1262 | BL 스캔 블록 |
| `lotteBoxOrder` | 1916 | `wet_data_insert` 지역변수 |
| `item_weight` / `item_weight_double` / `item_weight_str` / `item_pow` / `item_making_date` / `item_box_serial` | 1353~1360 | BL 스캔 블록 |

### 6.3 전송 URL 상수

| 상수 | 값 | 사용 타입 |
|---|---|---|
| `URL_INSERT_GOODS_WET` | `insert_goods_wet.jsp` | 0, 2, 6 (건별) |
| `URL_INSERT_GOODS_WET_NEW` | `insert_goods_wet_new.jsp` | 3, 4, 5 (일괄) |
| `URL_INSERT_GOODS_WET_PRODUCTION` | `insert_goods_wet_production.jsp` | 1, 7 (생산) |
| `URL_INSERT_GOODS_WET_HOMEPLUS` | `insert_goods_wet_homeplus.jsp` | **이 Activity에서 사용하지 않음** |

롯데는 이마트와 같은 `insert_goods_wet.jsp`로 전송한다(2970). 64 문서의 "홈플러스 JSP로 전송" 서술은 사실과 다르다.

---

## 7. 호출 시점

```
[로그인 → 메인 → 출하계근 진입]
    ↓
BixolonShipmentActivity.onCreate(411)
    ├── 레이아웃 분기(422)                        ← Activity 유지
    ├── 위젯 바인딩(446~502)                      ← Activity 유지
    ├── ★ shipmentType = Factory.create(...)      ← Step 1 추가. 생산이면 생성 안 함
    └── 생산 프린터 비활성화(518)                  ← 생산 유지
    ↓
[센터 선택 → 상품 바코드 스캔]
    ↓
setMessage(1078) → setBarcodeMsg(1143)
    ├── 생산 → setBarcodeMsgProduction(1577)      ← 생산 유지
    └── ★ shipmentType.onBarcodeScanned(msg)
            ├── 상품 매칭  → findPackerProduct    (Step 9)
            ├── ProgressDlgShipSelect 실행         → onShipmentLoaded (Step 8)
            ├── [재스캔] this.onBarcodeScanned(msg) ← 재귀 (§1.5)
            └── [BL스캔] 중량 추출 → onWeightConfirmed (Step 8)
                                        ├── DB 저장
                                        ├── 화면 반영
                                        └── 라벨 출력
    ↓
[전송 버튼] → ProgressDlgShipmentSend → ★ shipmentType.send(...)   (Step 10)
    ↓
[상세 팝업 재출력] → DetailAdapter → mHandler → ★ reprintLabel(...) (Step 8)
```

---

## 8. 개발 플랜

### Step 0: 호출처 없는 코드 정리 (선택 — 사용자 승인 필요)

**Part 1. 분석**
- 메서드: §1.2 목록
- 범위: `BixolonShipmentActivity.java:51~59, 2044~2051, 2094~2104, 2405~2439, 2694~2870, 3642~3672`
- 용도: Step 2~7에서 죽은 코드가 6벌로 복사되는 것을 막는다
- 주의할 점: **삭제는 사용자 승인 후에만 한다.** 승인 없으면 이 Step을 건너뛴다

| # | 항목 | 위치 | 줄수 |
|---|------|------|---:|
| 1 | `ProgressDlgShipSelectBL` | 2694~2870 | 177 |
| 2 | `show_wetNextDialog` | 3642~3672 | 31 |
| 3 | `find_BL` | 2094~2104 | 11 |
| 4 | `scanFlag_swap` | 2044~2051 | 8 |
| 5 | `slcsBarcode` / `slcsLine` / `slcsBox` | 2405~2439 | 35 |
| 6 | 미사용 import 8개 | 51~59 | 8 |

**Part 2. 변환 계획**
- 변환 방식: 메서드 단위 삭제. 호출처가 0건임을 Grep으로 재확인한 뒤 삭제
- 주의사항: 3057·3059의 도달 불가 분기는 **삭제하지 않는다.** 실행되지 않을 뿐 코드 구조의 일부이며, 판단 착오 시 전송 경로가 바뀐다

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 — `gradlew assembleDebug` → `BUILD SUCCESSFUL` (2026-09-16)
- [ ] Part 5: 단위테스트 — 실기기 확인 필요
- [ ] Part 6: 회귀테스트 — 실기기 확인 필요

**Part 6. 변경 내용** (완료):
- **무엇을**: `BixolonShipmentActivity.java`에서 호출처가 0건인 코드 6건 294줄 삭제. 3,739줄 → 3,445줄
- **왜**: Step 2~7에서 `setBarcodeMsg` 본문을 타입 파일 6개로 복사하므로, 죽은 코드를 먼저 지우지 않으면 6벌로 늘어난다
- **어떻게**: 삭제 전 각 구간의 시작 줄·닫는 괄호·다음 줄을 대조해 위치가 일치하는 경우에만 삭제. `git diff`는 삭제 294줄뿐이고 추가 0줄

| 삭제 대상 | 삭제 전 라인 | 줄수 |
|---|---|---:|
| 미사용 import 8개 | 51~55, 57~59 | 8 |
| `scanFlag_swap` | 2044~2052 | 9 |
| `find_BL` | 2094~2105 | 12 |
| `slcsBarcode`·`slcsLine`·`slcsBox` (+Javadoc) | 2396~2440 | 45 |
| `ProgressDlgShipSelectBL` (+Javadoc) | 2685~2871 | 187 |
| `show_wetNextDialog` | 3641~3673 | 33 |

**★ 라인 번호 오프셋 표 (이 문서 본문 번호 → 현재 파일)**

| 문서 본문 라인(삭제 전) | 보정 | 현재 파일 |
|---|:-:|---|
| 1 ~ 50 | 0 | 그대로 |
| 60 ~ 2043 | **-8** | 52 ~ 2035 |
| 2053 ~ 2093 | **-17** | 2036 ~ 2076 |
| 2106 ~ 2395 | **-29** | 2077 ~ 2366 |
| 2441 ~ 2684 | **-74** | 2367 ~ 2610 |
| 2872 ~ 3640 | **-261** | 2611 ~ 3379 |
| 3674 ~ 3739 | **-294** | 3380 ~ 3445 |

주요 앵커 실측 확인 (2026-09-16)

| 메서드 | 문서 본문 | 현재 |
|---|---:|---:|
| `inputBtnListener` | 630 | 622 |
| `mHandler` | 865 | 857 |
| `setBarcodeMsg` | 1143 | 1135 |
| `setBarcodeMsgProduction` | 1577 | 1569 |
| `wet_data_insert` | 1884 | 1876 |
| `find_PackerProduct` | 2059 | 2042 |
| `find_work_info` | 2106 | 2077 |
| `calc_info` | 2324 | 2295 |
| `sendData` | 2467 | 2393 |
| `ProgressDlgShipSelect` | 2500 | 2426 |
| `ProgressDlgShipmentSend` | 2899 | 2638 |
| `onActivityResult` | 3214 | 2953 |
| `show_wetDetailDialog` | 3358 | 3097 |

> **백업 파일**: `BixolonShipmentActivity_Back.java`(3,731줄)는 삭제 전 원본의 복사본이며, 클래스명을 `BixolonShipmentActivity_Back`으로 바꿔 중복 클래스 오류를 피했다. **이 문서의 라인 번호는 백업 파일 기준과도 다르다**(백업은 이미 클래스명 변경분만큼 차이가 있음). 원본 대조는 `D:\PDA\PDA-INNO(원본)` 또는 `git show HEAD:app/src/main/java/.../BixolonShipmentActivity.java`로 한다.

---

### Step 1: 인터페이스 + Factory 골격 (동작 변화 0)

**Part 1. 분석**
- 메서드: 신규
- 범위: `shipment/type/ShipmentType.java`, `ShipmentTypeFactory.java`, Activity 필드 1개 추가
- 용도: 이후 Step이 올라탈 골격 확보
- 주의할 점: 이 Step에서는 **Activity의 기존 코드를 한 줄도 고치지 않는다.** 필드 선언과 `onCreate`의 생성 1줄만 추가한다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | `ShipmentType` | 신규 | 메서드 6개 선언 (4.1) |
| 2 | `ShipmentTypeFactory` | 신규 | searchType 6종 매핑 (4.2) |
| 3 | 구현체 6개 | 신규 | 빈 구현 + `isMigrated()` false |
| 4 | Activity 필드 | 330 아래 | `private ShipmentType shipmentType;` |
| 5 | Activity 생성 | 446 앞 | 생산 제외 후 Factory 호출 (4.4) |

**Part 2. 변환 계획**
- 변환 방식: 신규 파일 9개 생성 + Activity 추가 16줄(수정·삭제 0줄)
- 주의사항: 생산(1, 7)으로 진입 시 Factory가 호출되지 않아야 한다. `IllegalArgumentException`이 나면 조건이 틀린 것이다

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 — `gradlew assembleDebug` → `BUILD SUCCESSFUL` (2026-09-16, 9s)
- [ ] Part 5: 단위테스트 — 실기기 확인 필요
- [ ] Part 6: 회귀테스트 — 실기기 확인 필요

**Part 6. 변경 내용** (완료):
- **무엇을**: `shipment/type/` 패키지에 파일 9개 생성. Activity에 import 2줄 + 필드 1개 + `onCreate` 생성 블록 추가(총 추가 16줄, 수정·삭제 0줄)
- **왜**: Step 2 이후 타입별 이관이 올라탈 골격 확보. 이 Step에서는 생성만 하고 호출하지 않으므로 동작이 바뀌지 않는다
- **어떻게**: 아래 구성대로 생성하고, Activity는 기존 줄을 건드리지 않고 추가만 했다

| 파일 | 내용 |
|---|---|
| `ShipmentType.java` | 인터페이스. 메서드 6개 선언 |
| `ShipmentTypeFactory.java` | searchType 6종 → 구현체. 생산(1·7)이 오면 `IllegalArgumentException` |
| `ShipmentConst.java` | 공용 상수. **값은 Activity와 동일**. 하드코딩 센터명(용인TRD 등)은 리터럴 유지 원칙에 따라 옮기지 않음 |
| `EmartType`(0) · `HomeplusType`(2) · `WholesaleType`(3) · `EmartNonfixedType`(4) · `HomeplusNonfixedType`(5) · `LotteType`(6) | 골격. 각 클래스 Javadoc에 §1.3의 타입별 판정을 명시. 메서드는 `UnsupportedOperationException("Step N에서 이관 예정")` |

**Activity 변경 (추가 16줄)**

```java
// import 2줄
import com.rgbsolution.highland_emart.shipment.type.ShipmentType;
import com.rgbsolution.highland_emart.shipment.type.ShipmentTypeFactory;

// 필드 1개
private ShipmentType shipmentType;

// onCreate — 레이아웃 분기 직후
if (!Common.searchType.equals(SEARCH_TYPE_PRODUCTION)
        && !Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)) {
    shipmentType = ShipmentTypeFactory.create(Common.searchType, this);
}
```

**검증 결과**

| 항목 | 결과 |
|---|---|
| Activity diff | 추가 16줄 / 삭제 0줄. 기존 줄 수정 없음 |
| 빌드 | `BUILD SUCCESSFUL` (9s) |
| Factory 도달 값 | `Common.searchType` 대입 지점 전수 확인 — `MainActivity` 호출부 8곳이 상수 `"0"`~`"7"`만 사용, 기본값 `"0"`. 따라서 Factory에는 0·2·3·4·5·6만 도달하며 예외 발생 값 없음 |
| 생산 경로 | 생성 자체를 건너뛰므로 `shipmentType`은 null. `setBarcodeMsgProduction` 경로 그대로 |
| 골격 오호출 방지 | 구현체 메서드는 전부 `UnsupportedOperationException`. Step 2 이후 배선 실수가 조용히 넘어가지 않고 즉시 드러난다 |

---

### Step 2: 바코드 스캔 이관 — 도매(3) WholesaleType

**Part 1. 분석**
- 메서드: `setBarcodeMsg` 본문
- 범위: `BixolonShipmentActivity.java:1150~1545` → `WholesaleType.onBarcodeScanned`
- 용도: 차이가 가장 적은 타입으로 **복사 규칙과 검증 절차를 확정**한다
- 주의할 점: 재귀(1227)는 `this.onBarcodeScanned(msg)`. 접는 조건은 아래 4개뿐

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 중복검사 우회 | 1205, 1329 | 항상 거짓 → 블록 삭제 |
| 2 | 트레이더스 소비기한 | 1294 | 항상 거짓 → 안쪽 블록만 삭제. 1293 `else if`는 유지 |
| 3 | LB 자릿수 | 1443, 1505 | 항상 거짓 → `else` 본문만 남김 |
| 4 | 그 외 전부 | 1150~1545 | **그대로 복사** |

**Part 2. 변환 계획**
- 변환 방식: 원본을 그대로 복사한 뒤 위 4개 조건만 접는다. 접은 자리에 원본 라인 주석을 남긴다
- 주의사항: `ITEM_TYPE` 4블록·킬코이(1285)·센터명(1293)은 **남긴다**(§1.4). Activity에는 과도기 분기(4.4)를 넣어 도매만 위임한다

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 — `gradlew assembleDebug` → `BUILD SUCCESSFUL` (2026-09-16, 4s)
- [ ] Part 5: 단위테스트 — 실기기 확인 필요 (도매 스캔 → 계근 → 전송)
- [ ] Part 6: 회귀테스트 — 실기기 확인 필요 (이마트·롯데·홈플러스 무영향 확인)

**Part 6. 변경 내용** (완료):
- **무엇을**: 원본 `setBarcodeMsg` 본문을 `WholesaleType.onBarcodeScanned` 로 기계적 복사. Activity는 접근 권한 공개 + 래퍼 1개 + 과도기 위임 분기 추가
- **왜**: 도매는 타입별 차이가 3곳뿐(§1.3 #9·10·12)이라 복사 규칙과 검증 절차를 확정하기에 가장 안전한 첫 대상이다
- **어떻게**: 원본 줄을 그대로 옮기고, 도매에서 항상 거짓인 `searchType` 조건 4곳만 접었다. 접은 자리마다 원본 라인 번호를 주석으로 남겼다

**접은 조건 (전부 도매에서 거짓)**

| 원본 라인 | 조건 | 처리 | 줄수 |
|---|---|---|---:|
| 1213 | `NONFIXED \|\| HOMEPLUS_NONFIXED` 중복확인 제외 | 블록 삭제 | 3 |
| 1337 | 동일 | 블록 삭제 | 3 |
| 1302 | `EMART` 트레이더스 소비기한 검증 | 내부 블록 삭제. **바깥 `else if`(1301) 골격은 유지** — searchType 게이트가 아니므로 1285 블록과의 배타 관계 보존 | 9 |
| 1451 | `EMART` LB 환산 ZEROPOINT 자릿수 | `else` 경로만 유지 | 4 |
| 1513 | 동일 | `else` 경로만 유지 | 4 |

**치환 3곳**

| 원본 | 변경 후 | 사유 |
|---|---|---|
| `new ProgressDlgShipSelect(this, …)` (1207) | `a.startShipSelect(…)` | inner 클래스라 외부 패키지에서 생성 불가 |
| `new ProgressDlgShipSelect(BixolonShipmentActivity.this, …)` (1252) | `a.startShipSelect(…)` | 동일 |
| `setBarcodeMsg(msg)` (1235) | `this.onBarcodeScanned(msg)` | 재귀는 Activity가 아니라 자기 자신 (§1.5) |

**Activity 변경 (추가 41 / 삭제 23 — 전부 접근 권한·주석·래퍼)**

| 항목 | 내용 |
|---|---|
| 접근 권한 공개 | 위젯 5종(`edit_barcode`, `sp_center_name`, `sp_bl_no`, `sp_point_name`, `sList`), 상태 10종(`arSM`, `current_work_position`, `centerTotalCount`, `centerWorkCount`, `work_flag`, `scan_flag`, `vibrator`, `alert_flag`, `lastProcessedBarcode`, `lastBarcodeProcessedTime`), `work_*` 7종 → `public`. **값·용도 변경 없음** |
| 메서드 공개 | `show_wetFinishDialog` `private` → `public` (본문 변경 없음) |
| 래퍼 추가 | `startShipSelect(centerName, condition, type)` — `new ProgressDlgShipSelect(this, …).execute()` 를 감싸기만 함 |
| 과도기 분기 | `setBarcodeMsg` 에 도매만 위임. **Step 7에서 제거** |

**검증 결과**

| 항목 | 결과 |
|---|---|
| 빌드 | `BUILD SUCCESSFUL` (4s) |
| 원본 대조 | 정규화(`a.`·`ShipmentConst.` 제거) 후 비교 — 원본 334줄 → 신규 317줄, **삭제 26 / 추가 9**, 산술 일치 |
| 삭제 26줄 내역 | 접힌 블록 23줄(3+3+9+4+4) + 치환 3줄. **전부 예상한 것** |
| 추가 9줄 내역 | 래퍼 호출 2 + 재귀 1 + 주석 6. **로직으로 추가된 줄 0** |
| 유지 확인 | `ITEM_TYPE` W/HW·S·J·B 4블록, 킬코이·미트센터(1285), 센터명(1293), 중복검사 선조회, `if (true)`(1269) 모두 그대로 |
| 타 타입 영향 | Activity diff에 로직 변경 0줄. 이마트·롯데·홈플러스·비정량은 기존 경로 유지 |
| 생산 영향 | 생산 분기가 위임 분기보다 앞에 있어 그대로 유지 |

---

### Step 3: 바코드 스캔 이관 — 롯데(6) LotteType

**Part 1. 분석**
- 메서드: `setBarcodeMsg` 본문
- 범위: `1150~1545` → `LotteType.onBarcodeScanned`
- 용도: 두 번째 타입으로 규칙 재현성 확인
- 주의할 점: 접는 조건은 Step 2와 동일(1205·1294·1329·1443·1505 전부 거짓). 박스순번은 이 Step 범위가 아니다(Step 8)

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 접는 조건 | 1205, 1294, 1329, 1443, 1505 | Step 2와 동일 |
| 2 | 개발63 충돌 확인 | 2899~3170 | 구간이 겹치지 않음. 순서 제약 없음 |

**Part 2. 변환 계획**
- 변환 방식: Step 2와 동일
- 주의사항: `WholesaleType`과 결과가 같아도 **복사해서 만든다.** 공통화는 Step 12에서 한다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 4: 바코드 스캔 이관 — 홈플러스(2) HomeplusType

**Part 1. 분석**
- 메서드: `setBarcodeMsg` 본문
- 범위: `1150~1545` → `HomeplusType.onBarcodeScanned`
- 용도: 홈플러스 정량 이관
- 주의할 점: 운영 DB에 홈플러스 데이터가 0건일 수 있다. **테스트 데이터 확보 후 착수**한다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 접는 조건 | 1205, 1294, 1329, 1443, 1505 | 전부 거짓 |
| 2 | `ITEM_TYPE B` | 1481~1532 | 상수 주석이 "홈플러스 비정량"이나 **코드 게이트 없음 → 유지** |

**Part 2. 변환 계획**
- 변환 방식: Step 2와 동일
- 주의사항: 오류 32·33·34가 이 타입 계열이다. 전송·라벨은 이 Step 범위가 아니다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 5: 바코드 스캔 이관 — 홈플러스 비정량(5) HomeplusNonfixedType

**Part 1. 분석**
- 메서드: `setBarcodeMsg` 본문
- 범위: `1150~1545` → `HomeplusNonfixedType.onBarcodeScanned`
- 용도: 비정량 계열 첫 이관
- 주의할 점: 1205·1329는 **항상 참**이다. `dup = false`가 남고 조건만 사라진다. 조회(1203·1326)는 그대로 둔다(§1.5)

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 중복검사 우회 | 1205, 1329 | 항상 참 → 조건 삭제, `dup = false` 유지 |
| 2 | 트레이더스·LB | 1294, 1443, 1505 | 항상 거짓 → 삭제 |

**Part 2. 변환 계획**
- 변환 방식: Step 2와 동일
- 주의사항: 중복검사 조회를 건너뛰면 안 된다. `DBHandler` 내부 `Log.v`로 호출 여부를 logcat에서 확인할 수 있다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 6: 바코드 스캔 이관 — 이마트 비정량(4) EmartNonfixedType

**Part 1. 분석**
- 메서드: `setBarcodeMsg` 본문
- 범위: `1150~1545` → `EmartNonfixedType.onBarcodeScanned`
- 용도: 이마트 비정량 이관
- 주의할 점: 4는 **이마트가 아니다.** 1294·1443·1505의 `EMART` 조건은 **거짓**이다. 상수 주석(164)이 "도매 비정량"으로 잘못 적혀 있으나 라벨은 이마트(2012~2014)다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 중복검사 우회 | 1205, 1329 | 항상 참 |
| 2 | 트레이더스·LB | 1294, 1443, 1505 | **거짓** (0만 참) |

**Part 2. 변환 계획**
- 변환 방식: Step 2와 동일
- 주의사항: 상품 매칭의 전부 매칭(2150)은 Step 9 범위다. 이 Step에서 건드리지 않는다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 7: 바코드 스캔 이관 — 이마트(0) EmartType + 과도기 분기 제거

**Part 1. 분석**
- 메서드: `setBarcodeMsg` 본문
- 범위: `1150~1545` → `EmartType.onBarcodeScanned`. 이후 원본 본문 삭제
- 용도: 마지막 타입 이관 및 위임 완성
- 주의할 점: **현재 이마트 출하계근 테스트가 진행 중이다.** 이 Step은 테스트 일정과 겹치지 않게 진행한다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 중복검사 우회 | 1205, 1329 | 거짓 → 삭제 |
| 2 | 트레이더스 소비기한 | 1294 | **참** → 조건만 삭제, 안쪽 유지 |
| 3 | LB 자릿수 | 1443, 1505 | **참** → `if` 본문만 남기고 `else` 삭제 |
| 4 | 원본 본문 삭제 | 1150~1545 | 위임 1줄로 교체 (4.4) |
| 5 | 과도기 분기 제거 | 4.4 | `isMigrated()` 삭제 |

**Part 2. 변환 계획**
- 변환 방식: Step 2와 동일 + 원본 본문 제거
- 주의사항: 생산 분기(1145~1148)는 그대로 남는다. 삭제하면 생산이 깨진다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 8: 계근 저장 + 라벨 이관 (6종)

**Part 1. 분석**
- 메서드: `wet_data_insert`, `mHandler` MESSAGE_REPRINT, `ProgressDlgShipSelect` 롯데 블록
- 범위: `1884~2032`, `934~951`, `2549~2566`
- 용도: 차이 4곳(#7·8·9·10·11)을 타입 파일로 이동
- 주의할 점: **Activity에 `wet_data_insert`를 남긴다.** 생산 본문(1824)과 `onActivityResult`(3297), `inputBtnListener`(723·726)가 호출한다

| # | 항목 | 위치 | 타입별 차이 |
|---|------|------|---|
| 1 | INSERT 방식 | 1918~1934 | 2=홈플, 6=롯데, 나머지=일반 |
| 2 | 중량 반올림 | 1940, 1954, 1972, 1982 | 0=1자리, 나머지=3자리 |
| 3 | 계근 라벨 | 2005~2021 | 0·4=이마트, 2·5=홈플, 6=롯데, 3=없음 |
| 4 | 재출력 라벨 | 934~951 | 3은 `else`라 이마트 라벨 |
| 5 | 롯데 카운터 | 2549~2566 | `lotte_TryCount`를 `LotteType`으로 이동 |

**Part 2. 변환 계획**
- 변환 방식: 타입 하나씩(3 → 6 → 2 → 5 → 4 → 0) 이동하고 매번 빌드·대조
- 주의사항: 생산은 `wet_data_insert`의 else 경로(일반 INSERT, 3자리 반올림, 라벨 없음)를 탄다. **생산 경로가 바뀌지 않는지 매번 확인**한다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 9: 상품 매칭 이관 (6종)

**Part 1. 분석**
- 메서드: `find_PackerProduct`, `find_PackerProductBarcodeGoods`, `find_work_info`, `find_work_info_barcodeGoods`
- 범위: `2059~2220`
- 용도: 차이 1곳(#2 전부 매칭)을 타입 파일로 이동
- 주의할 점: `find_work_info`는 `ProgressDlgShipSelect.onPostExecute`(2599)도 호출한다. **Activity에 남겨 위임**한다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 전부 매칭 | 2150~2162 | 4만 참. 일반 매칭(2131) **뒤에 이어서** 실행되어 같은 `bi`가 2회 반영될 수 있다 |
| 2 | 구간 추출 차이 | 2122 vs 2185 | 2122는 길이 체크 있음, 2185는 없음. **합치지 않는다** |
| 3 | 항상 참 조건 | 2065, 2082 | `Editable` vs `String` 비교. 그대로 유지 |

**Part 2. 변환 계획**
- 변환 방식: 타입 하나씩 이동
- 주의사항: 4번 타입에서 일반 매칭과 전부 매칭의 **실행 순서를 바꾸지 않는다.** `pp_code` 결과 문자열을 logcat(`return pp_code test!!!`)으로 원본과 대조한다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 10: 전송 이관 (6종)

**Part 1. 분석**
- 메서드: `ProgressDlgShipmentSend.doInBackground`
- 범위: `2919~3125`
- 용도: 차이 1곳(#12)을 타입 파일로 이동
- 주의할 점: **건별과 일괄은 패킷 구성과 후처리가 전혀 다르다.** 0·2·6은 건별 루프(2939~3014), 3·4·5는 일괄 누적(3020~3117)이다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 건별 전송 | 2938~3014 | 0, 2, 6 — `insert_goods_wet.jsp` |
| 2 | 일괄 전송 | 3015~3117 | 3, 4, 5 — `insert_goods_wet_new.jsp` |
| 3 | 도달 불가 분기 | 3057, 3059 | **삭제하지 않는다.** 해당 타입 파일에서 자연히 빠진다 |
| 4 | 생산 분기 | 2971~2974, 3015, 3061 | 생산 유지 — Activity에 그대로 남긴다 |

**Part 2. 변환 계획**
- 변환 방식: 타입 하나씩 이동. `ProgressDlgShipmentSend`는 껍데기로 남기고 `shipmentType.send(...)`를 호출한다
- 주의사항: 전송은 실패 시 되돌릴 수 없다. **테스트 서버 또는 소량 데이터로 먼저 확인**한다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 11: 수기 입력 이관 (6종)

**Part 1. 분석**
- 메서드: `inputBtnListener` 수기 분기
- 범위: `638~729`
- 용도: 차이 2곳(#5·6)을 타입 파일로 이동
- 주의할 점: 703의 `else if` 조건에 `CENTERNAME` 판정과 `searchType` 판정이 **섞여 있다.** 센터 판정은 데이터 의존이므로 남긴다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 소수 1자리 절사 | 660~670 | 0만 참 |
| 2 | 소비기한 창 | 703~704 | 0은 TRD 센터일 때, 6은 항상 |
| 3 | 킬코이 분기 | 683~702 | `searchType` 게이트 없음 → 전 타입 유지 |

**Part 2. 변환 계획**
- 변환 방식: `ShipmentType`에 메서드를 추가하고 타입 하나씩 이동
- 주의사항: 703 조건의 `|| Common.searchType.equals(SEARCH_TYPE_LOTTE)`는 **바깥 `else if` 진입 조건**이다. 안쪽 704와 역할이 다르므로 함께 접지 않는다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 12: 공통 추출 (6개 파일 diff)

**Part 1. 분석**
- 메서드: 타입 파일 6개 전체
- 범위: `shipment/type/*.java`
- 용도: A 단계에서 허용한 중복을 제거한다
- 주의할 점: **판단으로 묶지 않는다.** diff 결과 완전히 동일한 블록만 올린다

| # | 항목 | 기준 | 내용 |
|---|------|------|------|
| 1 | 6개 전부 동일 | diff 일치 | `common/`으로 이동 |
| 2 | 일부만 동일 | 2~5개 일치 | **이동하지 않는다.** 나중에 갈릴 수 있다 |
| 3 | 상태를 바꾸는 코드 | – | 이동하지 않는다. 순수 계산만 대상 |

**Part 2. 변환 계획**
- 변환 방식: 6개 파일을 diff → 동일 블록 목록 작성 → 사용자 확인 → 이동
- 주의사항: 이동 후 각 타입 파일에서 **호출 순서가 원본과 같은지** 확인한다. 순서가 바뀌면 상태가 꼬인다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 13: Activity 정리

**Part 1. 분석**
- 메서드: `BixolonShipmentActivity` 전체
- 범위: 잔여 코드
- 용도: 과도기 접근(공개된 필드 등)을 정리한다
- 주의할 점: **생산 코드는 정리 대상이 아니다**

| # | 항목 | 내용 |
|---|------|------|
| 1 | 접근제한자 | Step별로 열었던 필드 중 더 이상 필요 없는 것을 되돌린다 |
| 2 | 빈 위임 메서드 | 생산이 호출하는 것은 남긴다 |
| 3 | Javadoc | 클래스 주석의 searchType 표(79~87)가 실제와 다르다 → 정정 |

**Part 2. 변환 계획**
- 변환 방식: 컴파일 기준으로 축소
- 주의사항: 생산 호출 경로(§5.1)를 먼저 확인하고 정리한다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 14: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 생산(1) 바코드 스캔 → 계근 → 전송이 이관 전과 동일 | □ |
| 2 | 도매(3) 스캔 → 계근 → 전송, 라벨 미출력 | □ |
| 3 | 롯데(6) 스캔 → 계근 → 박스순번 증가 → 라벨 → 전송 | □ |
| 4 | 홈플러스(2) 스캔 → MaxBoxOrder INSERT → 라벨 → 건별 전송 | □ |
| 5 | 홈플러스 비정량(5) 동일 바코드 연속 스캔 허용 → 일괄 전송 | □ |
| 6 | 이마트 비정량(4) 전부 매칭 → 라벨 → 일괄 전송 | □ |
| 7 | 이마트(0) TRD 센터 소비기한 검증 → LB 환산 → 1자리 반올림 → 건별 전송 | □ |
| 8 | 수기 입력: 0은 소수 1자리, 6은 소비기한 창 | □ |
| 9 | 상세 팝업 재출력: 타입별 라벨(3은 이마트 라벨) | □ |
| 10 | 같은 상품 재스캔 시 BL 스캔 전환(재귀) 동작 | □ |
| 11 | 중복 바코드 거부 동작(0·2·3·6) / 허용 동작(4·5) | □ |
| 12 | 합계 라벨(상세 팝업 SUM) 출력 | □ |

---

### 개발 순서 요약

```
Step 0: 호출처 없는 코드 정리 (승인 시)
    ↓
Step 1: 인터페이스 + Factory 골격
    ↓
Step 2~7: 바코드 스캔 이관  3 → 6 → 2 → 5 → 4 → 0
    ↓
Step 8: 계근 저장 + 라벨
    ↓
Step 9: 상품 매칭
    ↓
Step 10: 전송
    ↓
Step 11: 수기 입력
    ↓
Step 12: 공통 추출 (diff)
    ↓
Step 13: Activity 정리
    ↓
Step 14: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 타입 이관 직후 회귀 (Step 2~7 공통)

```
1. 이관한 타입으로 로그인 → 출하대상 다운로드
2. 센터 선택 → 상품 바코드 스캔 → 상품명/상품코드 표시 확인
3. 같은 바코드 재스캔 → BL 스캔 경로 전환 확인 (logcat "상품스캔일반" → "BL스캔")
4. 계근 완료 → 수량/중량 증가값이 이관 전과 동일한지 확인
5. git diff 확인 → 이관하지 않은 타입의 코드가 바뀌지 않았는지 확인
```

### 시나리오 2: 생산 미영향 확인 (모든 Step 후 필수)

```
1. 생산(1)으로 로그인 → 계근대상 다운로드
2. 바코드 스캔 → logcat "setBarcodeMsgProduction 시작" 출력 확인
3. 계근 완료 → 중량 3자리 반올림 확인
4. git diff 확인 → 1577~1836 구간에 변경 0줄
```

### 시나리오 3: 중복검사 동작 (Step 5·6 후)

```
1. 비정량(4 또는 5)으로 같은 바코드를 연속 3회 스캔
2. 3회 모두 계근되는지 확인
3. logcat에서 DBHandler 중복조회 로그가 3회 모두 찍히는지 확인 (조회는 하고 결과만 무시)
```

### 시나리오 4: 전송 경로 (Step 10 후)

```
1. 0 또는 2 또는 6 → 건별 전송. 패킷이 건당 1회 전송되는지 logcat 확인
2. 3 또는 4 또는 5 → 일괄 전송. 패킷이 "##"로 이어붙어 1회 전송되는지 확인
3. 전송 후 SAVE_TYPE이 Y로 바뀌는지 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 생산 동작이 깨짐 | `wet_data_insert`·`find_PackerProduct`를 타입 파일로 옮기면서 Activity에서 삭제 | Activity에 같은 이름으로 남기고 위임한다(§5.1). 생산 시나리오(§9-2)를 매 Step 확인 |
| 2 | **데이터 의존 분기를 조건으로 오인해 삭제** | `ITEM_TYPE`·킬코이·센터명은 `searchType` 게이트가 없다 | §1.3 표에 있는 것만 접는다. 표에 없으면 유지 |
| 3 | 중복검사 조회가 사라짐 | "어차피 `false`로 덮으니 건너뛰자"고 최적화 | 원본은 조회 후 덮는다. 조회는 그대로 수행(§1.5) |
| 4 | 재귀 유실 | 1227을 `activity.setBarcodeMsg(msg)`로 옮김 | `this.onBarcodeScanned(msg)`로 자기 자신 호출 |
| 5 | 디바운스에 재귀가 막힘 | 1226 `lastBarcodeProcessedTime = 0` 누락 | 재귀 직전 대입을 반드시 유지 |
| 6 | 이마트 4를 이마트 0과 같게 만듦 | 이름이 "이마트 비정량"이라 같은 취급 | 4는 1294·1443·1505가 전부 거짓. 전송도 일괄(§1.3) |
| 7 | 홈플러스 2와 5를 같게 만듦 | 라벨이 같아 전송도 같다고 판단 | 2는 건별, 5는 일괄. INSERT도 다르다(§1.3 #7·12) |
| 8 | 도매 재출력 라벨 누락 | 계근 시 라벨이 없어 재출력도 없다고 판단 | 3의 재출력은 `else` 경로라 이마트 라벨이 나간다(§5.3) |
| 9 | 중복 6벌 기간이 길어짐 | Step 2~11이 지연 | Step 12를 Step 11 직후에 붙인다. 지연 시 그 기간의 버그 수정은 6곳에 동일 적용 |
| 10 | 공통 추출 과잉 | "비슷하니 묶자" | diff 완전 일치만 대상(§2 B단계) |
| 11 | 중복검사 메서드 혼동 | `_check`(2인자)와 5인자 메서드 이름이 비슷 | 경로별 호출 메서드를 그대로 유지(§1.5) |
| 12 | 이마트 테스트와 충돌 | 현재 이마트 출하계근 테스트 진행 중 | 이마트(0)는 Step 7로 마지막에 배치. 테스트 일정과 조율 |
| 13 | 홈플러스 데이터 없음 | 운영 DB 0건 | 테스트 데이터 확보 후 Step 4·5 진행 |
| 14 | 개발63(롯데 박스순번)과 충돌 | 동일 파일 수정 | 개발63은 2899~3170 + JSP. Step 10과 겹치므로 **개발63 완료 후 Step 10 진행** |
| 15 | `onActivityResult` fall-through 제거 | `break` 누락을 버그로 오인 | 원본 동작이므로 유지(§1.5) |

### 64·65 문서의 사실관계 오류 (이 문서에서 정정)

| # | 64·65 서술 | 실제 |
|---|---|---|
| 1 | `setBarcodeMsg`는 `HoneywellScannerActivity` 콜백 | 콜백은 `setMessage`(상위 202). `setBarcodeMsg`는 이 Activity 고유 메서드 |
| 2 | 상태 필드 19종(333~380) | `weight_from/to`, `temp_weight`, `temp_weight_double`, `temp_bl_no`는 지역변수(§6.2) |
| 3 | 1443·1505의 `if (EMART)`에 `else` 없음 | `else`가 있다(1445, 1507) |
| 4 | LB 환산 3중복 → 1벌 | W/HW(1390)는 마트 분기 없이 `%.1f`, S·B는 분기 있고 `%.2f`. 동일하지 않다 |
| 5 | 구간 추출 3중복 | 2122(길이 체크 있음)와 2185(없음) 2곳이며 서로 다르다 |
| 6 | 홈플러스 2·5가 전송 공유(2969 인용) | 2969는 `HOMEPLUS \|\| LOTTE`. 2는 건별, 5는 일괄 |
| 7 | 롯데는 홈플러스 JSP로 전송 | 이마트와 같은 `insert_goods_wet.jsp`(2970) |
| 8 | 미사용 import 7개 | 8개(51~59) |
| 9 | 마트사 5개 + 플래그 | 0↔4가 7곳, 2↔5가 3곳 달라 플래그로 묶이지 않는다 |
| 10 | 수기 입력 분기 미기재 | 660·703~704에 타입 차이가 있다 |
| 11 | `onActivityResult` fall-through 미기재 | 3267~3283에 존재 |
| 12 | 도달 불가 분기 미기재 | 3057·3059는 실행되지 않는다 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 0 | 호출처 없는 코드 정리 | ✅ 완료 (2026-09-16, 294줄 삭제 · 빌드 통과) |
| 1 | 인터페이스 + Factory 골격 | ✅ 완료 (2026-09-16, 파일 9개 · Activity 추가 16줄 · 빌드 통과) |
| 2 | 바코드 스캔 — 도매(3) | ✅ 완료 (2026-09-16, 원본 대조 통과 · 빌드 통과 · 실기기 테스트 대기) |
| 3 | 바코드 스캔 — 롯데(6) | ⏳ 대기 |
| 4 | 바코드 스캔 — 홈플러스(2) | ⏳ 대기 |
| 5 | 바코드 스캔 — 홈플러스비정량(5) | ⏳ 대기 |
| 6 | 바코드 스캔 — 이마트비정량(4) | ⏳ 대기 |
| 7 | 바코드 스캔 — 이마트(0) + 과도기 분기 제거 | ⏳ 대기 |
| 8 | 계근 저장 + 라벨 (6종) | ⏳ 대기 |
| 9 | 상품 매칭 (6종) | ⏳ 대기 |
| 10 | 전송 (6종) | ⏳ 대기 |
| 11 | 수기 입력 (6종) | ⏳ 대기 |
| 12 | 공통 추출 (diff) | ⏳ 대기 |
| 13 | Activity 정리 | ⏳ 대기 |
| 14 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/개발/64_BixolonShipmentActivity_마트사별_클래스_분리.md` — **이 문서로 대체됨**
- `app/doc/개발/65_setBarcodeMsg_마트사별_분리.md` — **이 문서로 대체됨**
- `app/doc/개발/60_setBarcodeMsg_생산1_전용메서드_분리.md` — 생산 분리 이력(생산 코드 유지 근거)
- `app/doc/개발/56_setBarcodeMsg_디바운스_수정[setBarcodeMsg_디바운스_바코드값_미비교].md` — 1154~1164 디바운싱 도입
- `app/doc/개발/63_롯데_박스순번_파이프라인_복구[36].md` — Step 10 선행
- `app/doc/오류/32_홈플러스_월품목별재고_V뷰_년월조건_누락_출하대상_행중복[홈플러스_출하계근_AI정적검증].md`
- `app/doc/오류/33_홈플러스_SM_수주상세_LEFT_JOIN_다중행_팽창_출하건_중복[홈플러스_출하계근_AI정적검증].md`
- `app/doc/오류/34_홈플러스_STORE_IN_DATE_datetime_CONVERT_미적용_라벨_날짜_오인쇄[홈플러스_출하계근_AI정적검증].md`
- `app/doc/오류/36_롯데_search_shipment_lotte_박스순번_계근ID_미존재컬럼_출하대상조회_전면실패[전체JSP_오류검증].md` — §1의 "한 타입 수정이 다른 타입을 깨뜨린 사례" 근거
- `app/doc/참고자료/오류패턴_분석.md`

---

**문서 버전**: 1.0
