# setBarcodeMsg 마트사별 분리

**작성일**: 2026-08-12
**목적**: `BixolonShipmentActivity.setBarcodeMsg()`(434줄, 순환복잡도 30)를 마트사별 클래스로 분리한다. 기존 동작은 100% 동일하게 유지한다.

> ⚠️ **이 문서는 폐기되었습니다 (2026-09-16)**
> `66_BixolonShipmentActivity_searchType별_클래스_분리.md` 로 대체되었다.
> 이 문서의 "마트사별 Handler + 플래그" 설계는 소스 확인 결과 성립하지 않으며(66 §1.3),
> 사실관계 오류 12건(상위 클래스 콜백 오인, 지역변수를 상태 필드로 분류, `else` 없음 서술 등)은 66 §10에 정정했다.
> **아래 개발64 참조 관계는 더 이상 유효하지 않다. 작업은 66을 따른다.**

> **개발64와의 관계**
> 개발64(`64_BixolonShipmentActivity_마트사별_클래스_분리.md`)는 Activity 전체(3,738줄) 분리 계획이다.
> 이 문서는 그중 **바코드 처리 축만 먼저 떼어내는** 선행 작업이다.
> `wet_data_insert`·AsyncTask 5종·라벨 출력 이관은 이 문서 범위 밖이며 개발64에서 이어간다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

### 추가 제약 조건 (이 가이드 한정)

- **이관하지 않은 마트는 `git diff`에 한 줄도 나오지 않아야 한다.** 나오면 공통/차이 분류가 틀린 것이므로 되돌린다
- `setBarcodeMsg(String)` **시그니처는 변경 금지**. `HoneywellScannerActivity`가 호출하는 콜백이다
- **1227줄의 재귀 호출과 1226줄의 디바운스 우회를 반드시 보존**한다 (아래 1.4 참조)
- 공통 메서드로 추출하는 대상은 **상태를 변경하지 않는 순수 함수만** 허용한다
- 원본(`D:\PDA\PDA-INNO(원본)`)과 동작이 다른 코드를 발견해도 이 문서에서는 고치지 않는다. `app/doc/오류/`에 별건 문서화만 한다

---

## 1. 현재 구조

### 1.1 대상 메서드

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`

| 메서드 | 라인 | 줄수 | 순환복잡도 | 최대깊이 |
|---|---|---:|---:|---:|
| `setBarcodeMsg(String)` | 1143~1576 | **434** | **30** | 10 |
| `setBarcodeMsgProduction(String)` | 1577~1883 | 307 | 10 | 10 |

파일 전체(3,738줄) 중 2위 메서드(`inputBtnListener`, 순환복잡도 6)의 **5배**다.

### 1.2 처리 흐름

```
setBarcodeMsg(msg)                                          1143
 ├ 생산(1) 분기 → setBarcodeMsgProduction(msg); return       1145~1148
 ├ dialog_flag 가드 → return                                 1151~1152
 ├ 디바운싱 (동일 바코드 1초 이내 무시)                        1154~1164
 ├ edit_barcode.setText(msg)                                 1168
 │
 ├─[A] 상품 스캔  if (scan_flag)                             1169~1254
 │   ├ work_flag==1 ? find_PackerProduct                     1174~1182
 │   │                : find_PackerProductBarcodeGoods
 │   ├ find_ppcode=="null" → 실패 토스트 + 진동               1186~1190
 │   ├ 최초 스캔 (work_ppcode=="")                            1192~1199
 │   │   └ ProgressDlgShipSelect 실행
 │   ├ 같은 상품 재스캔                                       1200~1228
 │   │   ├ 중복검사 `duplicatequeryGoodsWet_check`(2인자) ★1205  1203~1207
 │   │   ├ dup → 거부 후 return                               1209~1215
 │   │   └ set_scanFlag(false) → BL스캔 전환                  1216~1228
 │   │       └ ★ setBarcodeMsg(msg) 재귀 호출                 1226~1227
 │   └ 다른 상품 스캔 → 확인 다이얼로그                        1229~1250
 │
 └─[B] BL 스캔  else                                         1255~1542
     ├ BL번호는 **스피너**에서 취득 (msg 아님)  sp_bl_no        1262
     ├ arSM 루프로 BL 매칭 → current_work_position            1263~1279
     ├ ★ 센터 판정 + 유통기한 검증                             1285~1304
     │   ├ 킬코이 PACKER_CODE + 미트센터 STORE_CODE           1285~1292
     │   └ 용인/대구/시화(W)/여주TRD, E/T, WET (★1294 이마트)  1293~1304
     ├ current_work_position==-1 처리                         1305~1314
     ├ 계근 완료 판정 → return                                 1315~1320
     ├ 중복검사 `duplicatequeryGoodsWet`(5인자)  ★1329 제외    1326~1344
     ├ ITEM_TYPE 분기 → 중량·제조일·박스시리얼 추출              1366~1533
     │   ├ W / HW                                             1366~1418
     │   ├ S        (★1443 이마트 분기)                        1419~1469
     │   ├ J                                                  1470~1480
     │   └ B        (★1505 이마트 분기)                        1481~1533
     ├ wet_data_insert(...)                                   1534
     └ BL 불일치 → 토스트 + 진동                               1535~1538

★ = searchType 분기 (총 6곳)
```

### 1.3 분기 축 — 4개가 중첩으로 곱해진다

| 분기 축 | 등장 횟수 | 값 |
|---|---:|---|
| **스캔/작업 상태** (`scan_flag`, `work_flag`, `work_ppcode`) | **15** | 상품/BL × 신규/동일/다른 |
| `ITEM_TYPE` | 5 | W, HW, S, J, B |
| **`searchType`** | 6 | 1145, 1205, 1294, 1329, 1443, 1505 |
| `BASEUNIT` (LB 환산) | 3 | LB / 그 외 |
| `CENTERNAME` (TRD/미트센터) | 2 | 6종 하드코딩 |

축을 분리하지 않고 **중첩으로 곱했기 때문에** `if` 30개가 서로 얽힌다.

### 1.4 재귀 호출 — 반드시 보존해야 하는 동작

```java
1216  } else{
1217      Log.e(TAG, "=====================상품스캔일반=========================");
1218      set_scanFlag(false);        // BL스캔 시작
1219      work_ppcode = find_ppcode;
1220      work_item_fullbarcode = msg;
1221
1222      if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {
1223          show_wetFinishDialog();
1224      }
1225
1226      lastBarcodeProcessedTime = 0;   // 의도된 재귀 호출은 디바운스 우회
1227      setBarcodeMsg(msg);             // ★ 자기 자신을 다시 호출
1228  }
```

같은 상품을 재스캔하면 `scan_flag`를 `false`로 바꾼 뒤 **같은 바코드로 자기 자신을 재호출**하여 `[B] BL 스캔` 경로를 태운다. 1226줄에서 디바운스 기준시각을 0으로 만들어 자기 호출이 차단되지 않게 한다.

**재귀가 성립하는 이유** — `[B] BL 스캔`은 스캔된 `msg`를 BL 매칭에 쓰지 않는다. BL번호는 **`sp_bl_no` 스피너 선택값**(1262)에서 가져오고, `msg`는 `work_item_fullbarcode`(1258)로만 저장된다. 그래서 같은 상품 바코드로 재호출해도 BL 매칭이 정상 동작한다.

**분리 시 이 재귀는 Activity가 아니라 Handler 자신을 호출해야 한다.**

```java
// 잘못된 이관 — Activity를 거치면 Factory 조회가 다시 일어나고 흐름이 어긋날 수 있다
activity.setBarcodeMsg(msg);

// 올바른 이관 — 같은 Handler 인스턴스 안에서 재귀
this.onBarcodeScanned(msg);
```

### 1.5 마트별 차이 — 정답지가 이미 있다

`setBarcodeMsgProduction`의 Javadoc(1548~1576)에 **생산이 타지 않는 분기**가 명시되어 있다. 이것이 마트별 차이 도출의 1차 근거다.

```
제외된 분기 (생산 미해당)
  - 비정량(4,5) 중복검사 우회 2곳                       → 1205, 1329
  - 킬코이 미트센터 소비기한 검증                        → 1285~1292
  - CENTERNAME TRD/E/T/WET 소비기한 검증                → 1293~1304
  - ITEM_TYPE W / HW (생산 VIEW 미출력)                 → 1366~1418
  - ITEM_TYPE B (홈플러스 비정량 전용)                   → 1481~1533
  - 이마트 LB 환산 자릿수 분기 (searchType==0 전용)      → 1443, 1505
```

### 1.6 마트사별 분기 대조표

**코드 게이트와 데이터 의존을 구분해야 한다.** 이 구분을 놓치면 잘못 제거하여 동작이 바뀐다.

| 분기 | 라인 | 게이트 종류 | 조건 |
|---|---|---|---|
| 생산 진입 분리 | 1145 | **코드(searchType)** | `PRODUCTION` |
| 중복검사 우회 | 1205, 1329 | **코드(searchType)** | `NONFIXED \|\| HP_NONFIXED` |
| TRD/E-T/WET 유통기한 검증 | **1294** | **코드(searchType)** | `EMART` |
| LB 환산 자릿수 | 1443, 1505 | **코드(searchType)** | `EMART` |
| 킬코이·미트센터 유통기한 검증 | 1285 | **데이터** | `PACKER_CODE`=킬코이 AND `STORE_CODE`=미트센터 |
| TRD/E-T/WET 센터 진입 | 1293 | **데이터** | `CENTERNAME` 6종 |
| `ITEM_TYPE` W/HW · S · J · B | 1366, 1419, 1470, 1481 | **데이터** | `ITEM_TYPE` 값 |

#### 코드 게이트 — 마트 클래스로 분리 대상

| 분기 | 라인 | 이마트(0) | 이마트비정량(4) | 생산(1,7) | 홈플러스(2) | 홈플러스비정량(5) | 도매(3) | 롯데(6) |
|---|---|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| 중복검사 **수행** | 1205, 1329 | O | **X** | O | O | **X** | O | O |
| TRD 유통기한 검증 | 1294 | **O** | X | X | X | X | X | X |
| LB 환산 자릿수 | 1443, 1505 | **O** | X | X | X | X | X | X |

**주의 — 1293/1294 구조**

```java
1293  } else if (CENTERNAME이 용인TRD/대구TRD/시화(W)_TRD/여주TRD/E/T/WET) {
1294      if (Common.searchType.equals(SEARCH_TYPE_EMART)) {        // ← 여기만 코드 게이트
1295          if (SHELF_LIFE=="" || MAKINGDATE_FROM=="" || MAKINGDATE_TO=="") { ... return; }
1296      }
1303  }
```

**바깥 `else if`(1293)는 게이트가 없다.** 전 마트가 진입하지만 이마트만 내부 검증을 수행하고, 나머지 마트는 진입 후 아무 것도 하지 않는다. 이관 시 **비이마트 Handler에서는 1293 블록 전체를 생략해도 동작이 같다**(빈 블록이므로). 단, `else if`이므로 **1285 블록과의 배타 관계는 유지**해야 한다.

#### 데이터 의존 — 전 마트 유지 대상

| 분기 | 라인 | 처리 |
|---|---|---|
| 킬코이·미트센터 유통기한 검증 | 1285~1292 | **전 마트 Handler에 유지.** `searchType` 게이트가 없으므로 특정 마트에서 제거하면 동작이 바뀐다 |
| TRD/E-T/WET 센터 진입 | 1293 | 위 "주의" 참조. 비이마트는 빈 블록이므로 생략 가능하나 `else if` 배타 관계 확인 필수 |
| `ITEM_TYPE` W/HW/S/J/B | 1366~1533 | **전 마트 Handler에 유지.** 아래 참조 |

**`ITEM_TYPE` 제거 판단 주의**

`setBarcodeMsgProduction`의 Javadoc(1.5)은 생산에서 `W/HW`와 `B`를 제외했다고 적고 있으나, 그 근거는 **"생산 VIEW가 해당 값을 출력하지 않는다"는 데이터 근거**이지 코드 게이트가 아니다. 상수 주석도 `ITEM_TYPE_B = "B"; // 홈플러스 비정량`(201줄)일 뿐 코드로 강제되지 않는다.

따라서 **다른 마트에서 `ITEM_TYPE` 블록을 제거하려면 해당 마트 VIEW가 그 값을 내보내지 않음을 실측으로 증명해야 한다.** 증명 전에는 전부 유지한다.

| 마트 | VIEW | 확인 필요 `ITEM_TYPE` |
|---|---|---|
| 이마트(0,4) | `VW_PDA_WID_LIST` | W, HW, S, J, B |
| 생산(1,7) | `VW_PDA_WID_PRO_LIST` | S, J만 (개발60에서 확인됨) |
| 홈플러스(2,5) | `VW_PDA_WID_HOMEPLUS_LIST` | 미확인 |
| 도매(3) | `VW_PDA_WID_WHOLESALE_LIST` | 미확인 |
| 롯데(6) | `VW_PDA_WID_LIST_LOTTE` | 미확인 |

**"미확인"은 해당 Step에서 VIEW DDL 또는 실데이터로 확인한 뒤 표를 갱신한다. 확인 전에는 블록을 제거하지 않는다.**

> **정적 확인 불가 확인됨** — JSP 5종(`search_shipment.jsp`, `_homeplus`, `_lotte`, `_wholesale`, `search_production.jsp`)은 모두 `ITEM_TYPE`을 VIEW에서 받아 그대로 통과시킨다. 값은 품목 마스터 데이터에 달려 있어 **소스만으로는 판정할 수 없다.**
> 따라서 **DB 조회로 마트별 실제 `ITEM_TYPE` 분포를 확인하기 전까지 모든 블록을 전 마트에 유지한다.**

### 1.7 참조하는 Activity 멤버 — 25종

```
work_item_bi_info(50)  current_work_position(35)  arSM(32)  work_item_fullbarcode(23)
work_ppcode(9)  weight_to(9)  weight_from(9)  vibrator(8)  temp_weight_double(8)
temp_weight(6)  scan_flag(5)  work_item_barcodegoods(4)  work_bl_no(4)  sp_bl_no(4)
dialog_flag(4)  centerTotalCount(4)  temp_bl_no(3)  lastBarcodeProcessedTime(3)
work_flag(2)  sp_center_name(2)  lastProcessedBarcode(2)  centerWorkCount(2)
sp_point_name(1)  sList(1)  edit_barcode(1)
```

이 중 **위젯 5종**(`sp_bl_no`, `sp_center_name`, `sp_point_name`, `sList`, `edit_barcode`)과 `vibrator`는 `ShipmentScreen`으로, 나머지 **19종**은 `WeighingContext`로 간다.

### 1.8 중복 블록

`ITEM_TYPE` 블록이 같은 검사를 반복한다.

**구조 주의** — 4개가 하나의 else-if 체인이 아니다. W/HW·S·J는 3단 체인이고, `B`는 **별도 `if` 문**(1481)이다. 값이 상호배타적이라 결과는 같지만, 이관 시 구조를 임의로 `else if`로 바꾸지 않는다.

| 검사 | W/HW | S | B | 중복 수 |
|---|---|---|---|:-:|
| 중량 범위 미설정 확인 | 1371 | 1424 | 1486 | 3 |
| LB 단위 환산 | 1390 | 1437 | 1499 | 3 |
| 제조일 추출 | 1408 | 1459 | 1521 | 3 |
| 박스시리얼 추출 | 1414 | 1465 | 1527 | 3 |

### 문제점

1. **한 메서드가 마트 7종 × 계근방식 5종 × 스캔상태 3종을 전부 처리** — 이론상 경로 100개 이상. 어떤 조합이 도달 가능한지 코드만으로 판단 불가
2. **`void` + 필드 부작용** — 결과를 반환하지 않고 `work_ppcode`, `current_work_position`, `work_item_bi_info` 등을 갱신한다. 호출 후 무엇이 바뀌었는지 추적 불가하고 단위 테스트 불가
3. **쌍둥이 메서드 80% 중복** — `setBarcodeMsgProduction` 본문(1584~1883)의 유효 250줄 중 201줄이 `setBarcodeMsg` 본문(1150~1546, 유효 325줄)과 동일. 한쪽만 고치면 다른 쪽에 버그가 남는데 컴파일러가 알려주지 않음
4. **`if (EMART)`에 `else`가 없다** (1443, 1505) — 나머지 마트 전부가 else 경로다. 이마트 조건을 손대면 롯데·홈플러스 동작이 같이 바뀐다
5. **`ITEM_TYPE` 블록 복붙** — 같은 검사가 3번씩 반복 (1.8)
6. **의미 없는 분기** `if (true)` (1261)

---

## 2. 변경 구조

### 데이터 흐름 — 변경 전

```
[바코드 스캔]  HoneywellScannerActivity
    ↓
BixolonShipmentActivity.setBarcodeMsg(msg)      434줄 / 순환복잡도 30
    ├ if (PRODUCTION)        → setBarcodeMsgProduction()  (201줄 중복)
    ├ if (NONFIXED || HP_NF) → 중복확인 제외
    ├ if (EMART)             → 유통기한 검증
    ├ if (EMART)             → LB 환산 자릿수 (else 없음 = 나머지 전부)
    ├ Activity 필드 25종 직접 읽기/쓰기
    └ 위젯 5종 + vibrator 직접 조작
```

### 데이터 흐름 — 변경 후

```
[바코드 스캔]  HoneywellScannerActivity
    ↓
BixolonShipmentActivity.setBarcodeMsg(msg)      ← 시그니처 유지. 본문 1줄
    ↓
handler.onBarcodeScanned(msg)                   ← switch 없음
    ↓
[EmartHandler / ProductionHandler / HomeplusHandler / WholesaleHandler / LotteHandler]
    ├ BarcodeUtil.isDebounced(...)              순수 함수
    ├ BarcodeUtil.extractBarcodeGoods(...)      순수 함수
    ├ WeightUtil.convertLb(...)                 순수 함수
    ├ WeightUtil.isRangeUnset(...)              순수 함수
    ├ ctx.setXxx() / ctx.currentItem()          WeighingContext
    ├ screen.showProduct() / screen.vibrate()   ShipmentScreen
    └ this.onBarcodeScanned(msg)                ★ 재귀는 자기 자신에게
```

### 클래스 구성

개발64와 동일한 구성을 쓰되, **이 문서에서는 `onBarcodeScanned` 하나만 구현**한다.

```
shipment/
  ShipmentTypeHandler.java      ← 인터페이스. 이 문서에서는 메서드 2개만
  ShipmentTypeFactory.java      ← searchType 8종 → Handler 5종
  ShipmentScreen.java           ← 위젯 (이 문서 범위: 5종 + vibrator)
  WeighingContext.java          ← 계근 상태 19종
  BarcodeUtil.java              ← static 순수 함수
  WeightUtil.java               ← static 순수 함수

  EmartHandler.java             (0, 4)  nonfixed 플래그
  ProductionHandler.java        (1, 7)  labelOnly 플래그
  HomeplusHandler.java          (2, 5)  nonfixed 플래그
  WholesaleHandler.java         (3)
  LotteHandler.java             (6)
```

**이 문서 시점의 인터페이스**

```java
public interface ShipmentTypeHandler {
    void onBarcodeScanned(String msg);
    boolean allowDuplicateBarcode();     // 비정량 계열 true (1205, 1329)
}
```

`printLabel`, `onWeightConfirmed`, `getSearchUrl`, `getSendUrl`, `setupScreen`은 개발64에서 추가한다.

### 목표 지표

| 지표 | 현재 | 목표 |
|---|---:|---:|
| `setBarcodeMsg` 줄수 | 434 | **1** (위임 한 줄) |
| `setBarcodeMsg` 순환복잡도 | 30 | **1** |
| `setBarcodeMsgProduction` | 307줄 (중복 201줄) | **삭제** |
| 마트별 `onBarcodeScanned` 줄수 | – | 마트당 150~250 |
| `ITEM_TYPE` 중복 검사 | 3중복 × 4종 | **1벌** (순수 함수) |
| 바코드 처리 내 `searchType` 참조 | 6 | **0** |

---

## 3. 수정 대상 파일

### 3.1 추가 파일 (신규 생성) — 11개

| # | 파일 | 이 문서에서 구현하는 범위 | 착수 Step |
|:-:|------|------|:-:|
| 1 | **BarcodeUtil.java** | `isDebounced()`, `extractBarcodeGoods()` | Step 2 |
| 2 | **WeightUtil.java** | `convertLb()`, `isRangeUnset()` | Step 2 |
| 3 | **WeighingContext.java** | 상태 19종 + `isCurrentBl()` | Step 3 |
| 4 | **ShipmentScreen.java** | **위젯 5종 + `vibrator`** 만 소유 (나머지 10종은 개발64) | Step 4 |
| 5 | **ShipmentTypeHandler.java** | 메서드 2개 선언 | Step 5 |
| 6 | **ShipmentTypeFactory.java** | searchType 8 → Handler 5 | Step 5 |
| 7 | **ProductionHandler.java** | `onBarcodeScanned` (1, 7) | Step 6 |
| 8 | **LotteHandler.java** | `onBarcodeScanned` (6) | Step 7 |
| 9 | **HomeplusHandler.java** | `onBarcodeScanned` (2, 5) | Step 8 |
| 10 | **WholesaleHandler.java** | `onBarcodeScanned` (3) | Step 9 |
| 11 | **EmartHandler.java** | `onBarcodeScanned` (0, 4) | Step 10 |

경로는 전부 `app/src/main/java/com/rgbsolution/highland_emart/shipment/` 이다.

#### 추가 클래스 상세

---

**① BarcodeUtil.java** — 바코드 순수 계산 (Step 2)

| 항목 | 내용 |
|---|---|
| 종류 | `public class` / 전 메서드 `static` |
| 역할 | 바코드 문자열 계산. 입력만 받고 결과만 반환 |
| 이관 출처 | `1154~1164`(디바운싱), `2122~2126`(구간 추출) |
| 상태 접근 | **없음.** Activity 필드·위젯 참조 금지 |

```java
public class BarcodeUtil {
    /** 원본 1154~1164. 동일 바코드가 thresholdMs 이내 재입력이면 true */
    public static boolean isDebounced(String msg, String lastBarcode,
                                      long lastTime, long now, long thresholdMs) {
        return msg != null && msg.equals(lastBarcode) && (now - lastTime) < thresholdMs;
    }

    /** 원본 2122~2126. type=true면 BARCODEGOODS 구간 절사, false면 원문 반환 */
    public static String extractBarcodeGoods(String req, boolean type, String from, String to) {
        if (type && req.length() >= Integer.parseInt(to)) {
            return req.substring(Integer.parseInt(from) - 1, Integer.parseInt(to));
        }
        return req;
    }
}
```

---

**② WeightUtil.java** — 중량 순수 계산 (Step 2)

| 항목 | 내용 |
|---|---|
| 종류 | `public class` / 전 메서드 `static` |
| 역할 | 중량 환산·검증 계산 |
| 이관 출처 | `1371·1424·1486`(범위 검증, 3중복), `1390·1437·1499`(LB 환산, 3중복) |
| 상태 접근 | **없음** |

```java
public class WeightUtil {
    /** 원본 1371 / 1424 / 1486 */
    public static boolean isRangeUnset(String from, String to) {
        return from.equals("0") || to.equals("0");
    }

    /** 원본 1390 / 1437 / 1499. 이마트 자릿수 분기(1443·1505)는 포함하지 않는다 */
    public static double convertLb(String baseUnit, double weight) { ... }
}
```

**주의**: 이마트 LB 자릿수 분기(1443, 1505)는 **코드 게이트(마트별 차이)** 이므로 여기 넣지 않고 `EmartHandler`에 남긴다.

---

**③ WeighingContext.java** — 계근 상태 보관 (Step 3)

| 항목 | 내용 |
|---|---|
| 종류 | `public class` (POJO) |
| 역할 | 계근 진행 중 유지되는 값을 한 객체에 모은다 |
| 담당 | 상태 필드 **19종** (§6 매핑표, 표는 18행) + 상태 기반 조회 메서드 |
| 이관 출처 | `333~380`(필드 선언), `2094~2105`(`find_BL`) |
| 하지 않는 것 | 화면 조작, DB 접근, 서버 통신, 마트별 판단 |

```java
public class WeighingContext {
    private ArrayList<Shipments_Info> arSM;
    private int currentWorkPosition;
    private Barcodes_Info workItemBiInfo;
    private String workPpcode, workBlNo, workItemFullbarcode, workItemBarcodegoods;
    private String tempBlNo, tempWeight;
    private double tempWeightDouble;
    private boolean scanFlag;
    private int workFlag;
    private String weightFrom, weightTo;
    private int centerTotalCount, centerWorkCount;
    private boolean dialogFlag;
    private String lastProcessedBarcode;
    private long lastBarcodeProcessedTime;

    public Shipments_Info currentItem() { return arSM.get(currentWorkPosition); }

    /** 원본 find_BL(2094~2105) 이관 */
    public boolean isCurrentBl(String barcode) {
        return barcode.equals(currentItem().getBL_NO());
    }
    // getter / setter
}
```

**주의 2가지**
- **`lotteBoxOrder`는 포함하지 않는다.** `wet_data_insert`(1884~2033)의 지역변수(1916)이며 `setBarcodeMsg` 밖이다
- AsyncTask 5종(`arSM`·`current_work_position` **79건** 참조), `mHandler`(**8건**)도 같은 필드를 쓴다. **Step 3에서 함께 교체**하고 Activity 필드는 반드시 제거한다

---

**④ ShipmentScreen.java** — 화면 조작 전담 (Step 4)

| 항목 | 내용 |
|---|---|
| 종류 | `public class` |
| 역할 | 위젯을 소유하고 화면 조작을 의도 단위 메서드로 제공 |
| 담당 | **6종만** — `sp_center_name`, `sp_bl_no`, `sp_point_name`, `sList`, `edit_barcode`, `vibrator` |
| 이관 규모 | 해당 6종의 **전체 호출 89회** (위젯 61 + `vibrator` 28) |
| 이관 출처 | `280~332`(필드), `411~526`(`findViewById`) |
| 하지 않는 것 | 상태 보관, 업무 판단, DB·통신. **나머지 위젯 10종은 Activity 잔류** |

```java
public class ShipmentScreen {
    // 이 문서 범위: 6종만 소유. 나머지 10종은 개발64 Step 2에서 추가한다.
    private final Spinner  sp_center_name, sp_bl_no, sp_point_name;
    private final EditText edit_barcode;
    private final ListView sList;
    private final Vibrator vibrator;

    public ShipmentScreen(Activity a) { /* findViewById 5회 + vibrator */ }

    // 진동 (전역 28회)
    public void vibrate(int ms) { vibrator.vibrate(ms); }

    // 센터 스피너 (전역 12회)
    public String getSelectedCenter()              { return sp_center_name.getSelectedItem().toString(); }
    public void   setCenterAdapter(SpinnerAdapter a) { sp_center_name.setAdapter(a); }
    public void   setCenterListener(OnItemSelectedListener l) { sp_center_name.setOnItemSelectedListener(l); }

    // BL 스피너 (전역 14회)
    public String getBlAt(int pos)   { return sp_bl_no.getItemAtPosition(pos).toString(); }
    public int    getBlPosition()    { return sp_bl_no.getSelectedItemPosition(); }
    public int    getBlCount()       { return sp_bl_no.getCount(); }
    public void   setBlSelection(int p)            { sp_bl_no.setSelection(p); }
    public void   setBlAdapter(SpinnerAdapter a)   { sp_bl_no.setAdapter(a); }

    // 지점 스피너 + 리스트 (전역 20회) — 짝 호출을 묶는다
    public void   moveToWorkItem(int pos) { sList.setSelection(pos);
                                            sp_point_name.setSelection(pos); }
    public int    getPointPosition() { return sp_point_name.getSelectedItemPosition(); }
    public String getSelectedPoint() { return sp_point_name.getSelectedItem().toString(); }
    public void   setPointAdapter(SpinnerAdapter a) { sp_point_name.setAdapter(a); }
    public void   setPointListener(OnItemSelectedListener l) { sp_point_name.setOnItemSelectedListener(l); }
    public void   setListAdapter(ListAdapter a)     { sList.setAdapter(a); }

    // 바코드 입력 (전역 15회)
    public void   showBarcode(String s) { edit_barcode.setText(s); }
    public String readBarcode()         { return edit_barcode.getText().toString(); }
    public void   setBarcodeKeyListener(View.OnKeyListener l) { edit_barcode.setOnKeyListener(l); }
}
```

**범위 결정 근거 (안 A)** — 6종을 옮기면 그 6종의 **모든 호출처 89회**를 함께 바꿔야 한다. `setBarcodeMsg` 내 9회만 바꾸고 나머지를 남기면 Activity에서 위젯에 접근할 수 없어 컴파일이 깨진다. 반대로 16종 전부를 옮기면 123회 + Activity 전 구간이 대상이 되어 개발64 Step 2를 흡수하게 된다.

| 안 | 이관 위젯 | 변경 호출 수 | 판정 |
|---|:-:|---:|---|
| 6종만 (**채택**) | 6 | **89** | 범위 최소. 나머지는 개발64 |
| 전체 | 16 | 123 | 개발64 Step 2 흡수 |
| 미도입 | 0 | 0 | Handler가 위젯 직접 접근 → 결합 유지 |

**주의**: 레이아웃 2종(`activity_shipment.xml`, `activity_shipment_wholesale.xml`)의 위젯 id가 **완전히 동일**함을 확인했으므로 클래스 1개로 충분하며 `null` 가드가 불필요하다.

---

**⑤ ShipmentTypeHandler.java** — 마트 공통 규약 (Step 5)

| 항목 | 내용 |
|---|---|
| 종류 | `public interface` — **선언만. 구현 없음** |
| 역할 | 마트 클래스 5종이 반드시 제공해야 할 메서드를 규정 |
| 이 문서 범위 | 메서드 **2개** |
| 하지 않는 것 | 흐름 강제. 추상 클래스가 아니므로 골격을 공유하지 않는다 |

```java
public interface ShipmentTypeHandler {
    /** 바코드 스캔 진입점. 원본 setBarcodeMsg 본문에 대응 */
    void onBarcodeScanned(String msg);

    /** 중복검사 우회 여부. 원본 1205, 1329 */
    boolean allowDuplicateBarcode();
}
```

`printLabel`, `onWeightConfirmed`, `getSearchUrl`, `getSendUrl`, `setupScreen`은 **개발64에서 추가**한다.

**존재 이유는 컴파일 시점 누락 검증이다.** 마트 추가 시 메서드를 빠뜨리면 빌드가 막힌다.

---

**⑥ ShipmentTypeFactory.java** — searchType 해석 (Step 5)

| 항목 | 내용 |
|---|---|
| 종류 | `public class` / `static` 팩토리 |
| 역할 | `Common.searchType` 8종 → Handler 5종 생성 |
| 이관 출처 | 바코드 처리 내 searchType 분기 6곳 (전체 51곳 중) |
| 하지 않는 것 | 업무 로직. 매핑과 생성만 |

```java
public class ShipmentTypeFactory {
    public static ShipmentTypeHandler create(String searchType, ShipmentScreen screen,
                                             WeighingContext ctx, BixolonShipmentActivity activity) {
        switch (searchType) {
            case "0": return new EmartHandler(screen, ctx, activity, false);       // 이마트 정량
            case "4": return new EmartHandler(screen, ctx, activity, true);        // 이마트 비정량
            case "1": return new ProductionHandler(screen, ctx, activity, false);  // 생산 계근
            case "7": return new ProductionHandler(screen, ctx, activity, true);   // 생산 라벨(미사용)
            case "2": return new HomeplusHandler(screen, ctx, activity, false);    // 홈플러스 정량
            case "5": return new HomeplusHandler(screen, ctx, activity, true);     // 홈플러스 비정량
            case "3": return new WholesaleHandler(screen, ctx, activity);          // 도매
            case "6": return new LotteHandler(screen, ctx, activity);              // 롯데
            default:  throw new IllegalArgumentException("searchType: " + searchType);
        }
    }
}
```

`onCreate`에서 1회 호출하여 `handler` 필드에 담는다. 이후 호출은 `handler.onBarcodeScanned(msg)` 한 줄이므로 `switch`가 사라진다.

---

**⑦~⑪ 마트 Handler 5종** (Step 6~10)

다섯 클래스 모두 아래 형태를 공유한다. **공통 골격이나 상속은 없고, 각자 자기 흐름을 온전히 소유**한다.

```java
public class XxxHandler implements ShipmentTypeHandler {
    private final ShipmentScreen screen;
    private final WeighingContext ctx;
    private final BixolonShipmentActivity activity;   // 미이관 메서드 호출용 (5.4)
    private final boolean nonfixed;                   // 해당 마트만

    @Override public void onBarcodeScanned(String msg) { ... }
    @Override public boolean allowDuplicateBarcode()   { return nonfixed; }
}
```

| # | 클래스 | searchType | 플래그 | 고유 로직 | 이관 출처 | Step |
|:-:|---|:-:|---|---|---|:-:|
| ⑦ | **ProductionHandler** | 1, 7 | `labelOnly` | 프린터 미사용 | `setBarcodeMsgProduction` 1577~1883 | 6 |
| ⑧ | **LotteHandler** | 6 | – | 박스순번(개발63 예정) | `setBarcodeMsg` 롯데 경로 | 7 |
| ⑨ | **HomeplusHandler** | 2, 5 | `nonfixed` | `ITEM_TYPE B` 확인 | `1205·1329`(우회), `1481~1533` | 8 |
| ⑩ | **WholesaleHandler** | 3 | – | 전용 분기 없음 | 공통 경로만 | 9 |
| ⑪ | **EmartHandler** | 0, 4 | `nonfixed` | 유통기한 검증, LB 자릿수, 센터 판정 | `1285~1304`, `1443·1505`, `2150~2162` | 10 |

**공통 규칙 3가지**

1. **재귀는 자기 자신에게** — 원본 1227의 `setBarcodeMsg(msg)`는 `this.onBarcodeScanned(msg)`로 옮긴다. `activity.setBarcodeMsg(msg)`로 옮기면 안 된다 (§1.4)
2. **`activity` 참조는 메서드 호출만** — `find_PackerProduct`, `ProgressDlgShipSelect`, `show_wetFinishDialog`, `wet_data_insert` 등 미이관 메서드 호출에만 쓴다. **필드·위젯 직접 접근 금지** (§5.4)
3. **데이터 의존 분기는 제거하지 않는다** — 킬코이·미트센터(1285), `ITEM_TYPE` 블록(1366~1533)은 `searchType` 게이트가 없다. VIEW 실측으로 미출력을 증명하기 전까지 전 마트에 유지 (§1.6)

---

### 3.2 수정 클래스 — 1개

| # | 파일 | 경로 | 수정 규모 | 관련 Step |
|:-:|------|---|---|:-:|
| 1 | **BixolonShipmentActivity.java** | `.../highland_emart/` | 3,738줄 → 바코드 처리 축 제거 | Step 1~11 |

#### 수정 상세

**제거되는 것**

| 대상 | 라인 | 줄수 | Step |
|---|---|---:|:-:|
| `setBarcodeMsg` 본문 | 1150~1546 | 397 | 5~10 |
| `setBarcodeMsgProduction` 전체 | 1577~1883 | 307 | 11 |
| 위젯 필드 16종 + `findViewById` 16회 | 280~332, 411~526 | – | 4 |
| 상태 필드 19종 | 333~380 | – | 3 |
| `find_BL` | 2094~2105 | 12 | 3 |
| 미사용 조건 `if (true)` | 1261 | – | 1 |

**추가되는 것**

```java
private ShipmentScreen      screen;
private WeighingContext     ctx;
private ShipmentTypeHandler handler;
```

**남는 것 (시그니처 유지)**

```java
@Override
public void setBarcodeMsg(final String msg) {
    handler.onBarcodeScanned(msg);      // 434줄 → 1줄
}
```

`HoneywellScannerActivity`의 콜백이므로 **시그니처는 절대 변경하지 않는다.**

**이 문서에서 수정하지 않는 것**

| 항목 | 라인 | 사유 |
|---|---|---|
| `find_PackerProduct*`, `find_work_info*` | 2059~2323 | 개발64에서 이관. Handler가 `activity` 통해 호출 |
| `wet_data_insert` | 1884~2033 | 개발64 범위 |
| 라벨 출력 분기 | 2005~2022 | 개발64 범위 |
| AsyncTask 5종 | 2500~3357 | 클래스 분리는 개발64 범위. **단 Step 3에서 필드 접근만 `ctx` 경유로 교체** |
| Handler 2종, 다이얼로그 | 844~1094, 3358~3738 | 위와 동일 |
| `SEARCH_TYPE_*` 상수 | 158~168 | 다른 메서드에서도 사용. 개발64 완료 후 정리 |

**Step 완료 시점의 예상 규모**

| 제거 항목 | 줄수 |
|---|---:|
| `setBarcodeMsg` 본문 (1150~1546) | 397 |
| `setBarcodeMsgProduction` (1577~1883) | 307 |
| `find_BL` (2094~2105) | 12 |
| **메서드 소계** | **716** |
| 위젯 필드 16 + `findViewById` 16 + 상태 필드 19 | 약 50 |
| **합계** | **약 766** |

| 시점 | `BixolonShipmentActivity.java` |
|---|---:|
| 현재 | 3,738줄 |
| Step 11 완료 후 | 약 **2,970줄** |
| 개발64 완료 후 | 약 250줄 |

### 3.3 기존 소스 → 신규 파일 이관 매핑

| 기존 위치 | 라인 | 내용 | → 신규 | Step |
|---|---|---|---|:-:|
| `if (true)` | 1261 | 무의미 분기 | 조건만 제거 (블록 유지) | 1 |
| 디바운싱 판정 | 1154~1164 | `lastProcessedBarcode` 비교 | `BarcodeUtil.isDebounced()` | 2 |
| 바코드 구간 추출 | 2122~2126 | `substring(from-1, to)` | `BarcodeUtil.extractBarcodeGoods()` | 2 |
| 중량 범위 검증 | 1371, 1424, 1486 | 3중복 | `WeightUtil.isRangeUnset()` | 2 |
| LB 단위 환산 | 1390, 1437, 1499 | 3중복 | `WeightUtil.convertLb()` | 2 |
| 상태 필드 19종 | 333~380 | `arSM`, `work_*`, `weight_*` 등 | `WeighingContext` | 3 |
| `find_BL` | 2094~2105 | BL 일치 판정 | `WeighingContext.isCurrentBl()` | 3 |
| 위젯 5종 + `vibrator` | 280~332 | `sp_center_name`·`sp_bl_no`·`sp_point_name`·`sList`·`edit_barcode`·`vibrator` — 전체 호출 **89회** | `ShipmentScreen` | 4 |
| searchType 분기 6곳 | 1145, 1205, 1294, 1329, 1443, 1505 | 마트 분기 | `ShipmentTypeFactory` + 마트 클래스 | 5~10 |
| `setBarcodeMsgProduction` | 1577~1883 | 생산 바코드 처리 307줄 | `ProductionHandler.onBarcodeScanned()` | 6 |
| 롯데 바코드 분기 | `setBarcodeMsg` 내 | 박스순번 관련 | `LotteHandler.onBarcodeScanned()` | 7 |
| 중복검사 제외 | 1205, 1329 | `NONFIXED \|\| HP_NONFIXED` | `allowDuplicateBarcode()` | 8, 10 |
| 킬코이·미트센터 검증 | 1285~1292 | 유통기한 | `EmartHandler` *private* | 10 |
| TRD/E-T/WET 검증 | 1293~1304 | 유통기한 | `EmartHandler` *private* | 10 |
| LB 환산 자릿수 분기 | 1443, 1505 | `if (EMART)` | `EmartHandler` | 10 |
| 상품 스캔 블록 | 1169~1254 | `[A]` 경로 | 마트별 `onBarcodeScanned` | 6~10 |
| BL 스캔 블록 | 1255~1542 | `[B]` 경로 | 마트별 `onBarcodeScanned` | 6~10 |

### 3.4 Activity 잔류 (이관하지 않음)

| 항목 | 라인 | 사유 |
|---|---|---|
| `setBarcodeMsg(String)` 시그니처 | 1143 | `HoneywellScannerActivity` 콜백. **본문만 위임으로 교체** |
| `find_PackerProduct*` | 2059~2093 | 이 문서 범위 밖. Handler가 Activity 메서드를 호출 (개발64에서 이관) |
| `find_work_info*` | 2106~2323 | 위와 동일 |
| `wet_data_insert` | 1884~2033 | 이 문서 범위 밖. Handler가 호출만 함 |
| `ProgressDlgShipSelect` 등 | 2500~3357 | 이 문서 범위 밖. 필드 접근만 `ctx` 경유로 교체 |
| `show_wetFinishDialog` 등 | 3358~3738 | 이 문서 범위 밖. Handler가 호출만 함 |
| **위젯 10종** | 280~332 | `sp_work`, `edit_product_name/code`, `edit_wet_*`, `edit_center_t*`, `btn_*` — 호출 **62회**. 개발64 Step 2에서 `ShipmentScreen`에 추가 |
| 라벨 출력 | 2005~2022 | 이 문서 범위 밖 |

**이 문서에서는 `onBarcodeScanned`만 마트별로 나누고, 그 안에서 호출하는 Activity 메서드는 그대로 둔다.** 개발64에서 순차 이관한다.

---

## 4. 수정 상세

### 4.1 BarcodeUtil.java / WeightUtil.java (Step 2)

**변경 전** — `setBarcodeMsg` 내 3중복

```java
// 1371 / 1424 / 1486 — 동일 검사 3회
if (weight_from.equals("0") || weight_to.equals("0")) { ... }

// 1390 / 1437 / 1499 — 동일 환산 3회
if ("LB".equals(work_item_bi_info.getBASEUNIT())) { ... }
```

**변경 후**

```java
public class WeightUtil {
    public static boolean isRangeUnset(String from, String to) {
        return from.equals("0") || to.equals("0");
    }
    public static double convertLb(String baseUnit, double weight) { ... }
}

public class BarcodeUtil {
    public static boolean isDebounced(String msg, String lastBarcode,
                                      long lastTime, long now, long thresholdMs) {
        return msg != null && msg.equals(lastBarcode) && (now - lastTime) < thresholdMs;
    }
    public static String extractBarcodeGoods(String req, boolean type, String from, String to) {
        if (type && req.length() >= Integer.parseInt(to)) {
            return req.substring(Integer.parseInt(from) - 1, Integer.parseInt(to));
        }
        return req;
    }
}
```

**검증**: 전부 `static` 순수 함수. Activity 필드·위젯을 참조하면 안 된다. `convertLb`는 이마트 자릿수 분기(1443, 1505)를 **포함하지 않는다** — 그건 마트별 차이이므로 `EmartHandler`에 남긴다

### 4.2 BixolonShipmentActivity.setBarcodeMsg (Step 5 이후)

**변경 전** (1143~1546)

```java
public void setBarcodeMsg(final String msg) {
    if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) {
        setBarcodeMsgProduction(msg);
        return;
    }
    try {
        if (dialog_flag) return;
        long now = System.currentTimeMillis();
        if (msg != null && msg.equals(lastProcessedBarcode) && ...) { return; }
        ...
        // 434줄
    } catch (Exception ex) { ... }
}
```

**변경 후**

```java
@Override
public void setBarcodeMsg(final String msg) {
    handler.onBarcodeScanned(msg);
}
```

**검증**: 시그니처 동일. `HoneywellScannerActivity`의 콜백 계약이 유지되는지 컴파일로 확인

### 4.3 마트 클래스 예시 — 재귀 호출 보존 (Step 6~10 공통)

**변경 전** (1226~1227)

```java
lastBarcodeProcessedTime = 0;
setBarcodeMsg(msg);
```

**변경 후**

```java
ctx.setLastBarcodeProcessedTime(0);
this.onBarcodeScanned(msg);        // ★ Activity가 아닌 자기 자신
```

**검증**: 같은 상품 재스캔 시 BL 스캔 경로로 전환되는지 실기기 확인. logcat에서 `"상품스캔일반"` → `"BL스캔"` 순서가 원본과 동일해야 한다

### 4.4 마트 클래스 예시 — EmartHandler 중복검사 (Step 10)

**중요 — 중복검사는 경로별로 서로 다른 메서드를 쓴다**

| 경로 | 라인 | 호출 메서드 | 인자 |
|---|---|---|---|
| [A] 상품 스캔 | 1193, 1203 | `DBHandler.duplicatequeryGoodsWet_check` | `(context, barcode)` — 2개 |
| [B] BL 스캔 | **1326** | **`DBHandler.duplicatequeryGoodsWet`** | `(context, barcode, GI_D_ID, PACKER_PRODUCT_CODE, GI_L_ID)` — 5개 |

**이름이 비슷하나 다른 메서드다**(`DBHandler.java:1432` / `1466`). 이관 시 혼동하면 중복 판정 기준이 바뀐다.

**변경 전** — [A] 상품 스캔 (1203~1215)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet_check(getApplicationContext(), work_item_fullbarcode);
if (Common.searchType.equals(SEARCH_TYPE_NONFIXED) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)) {
    dup = false;
}
if (dup) { ... return; }        // 오류지점1 (1210)
```

**변경 전** — [B] BL 스캔 (1326~1344)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet(getApplicationContext(), work_item_fullbarcode,
        arSM.get(current_work_position).getGI_D_ID(),
        arSM.get(current_work_position).getPACKER_PRODUCT_CODE(),
        arSM.get(current_work_position).getGI_L_ID());
if (Common.searchType.equals(SEARCH_TYPE_NONFIXED) || Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)) {
    dup = false;
}
if (dup) { ... return; }        // 오류지점2 (1338)
```

**변경 후** — 두 경로 모두 `allowDuplicateBarcode()`만 교체하고 **호출 메서드는 그대로 유지**

```java
// [A]
boolean dup = DBHandler.duplicatequeryGoodsWet_check(appContext, ctx.getWorkItemFullbarcode());
if (allowDuplicateBarcode()) { dup = false; }
if (dup) { ... return; }

// [B]
boolean dup = DBHandler.duplicatequeryGoodsWet(appContext, ctx.getWorkItemFullbarcode(),
        ctx.currentItem().getGI_D_ID(), ctx.currentItem().getPACKER_PRODUCT_CODE(),
        ctx.currentItem().getGI_L_ID());
if (allowDuplicateBarcode()) { dup = false; }
if (dup) { ... return; }
```

**검증**: 두 메서드 모두 **`dup`가 무시되는 경우에도 그대로 호출**되어야 한다. 원본이 먼저 조회한 뒤 `false`로 덮으므로, 조회 자체를 건너뛰면 동작이 달라진다. `DBHandler` 내부에 `Log.v`가 있어(각 4건·8건) logcat으로 호출 여부를 관측할 수 있다

---

## 5. 사이드이펙트

### 5.1 HoneywellScannerActivity 콜백

```java
public class BixolonShipmentActivity extends HoneywellScannerActivity
```

- `setBarcodeMsg(String)`는 상위 클래스가 호출하는 콜백. **시그니처 변경 불가**
- **대응**: 본문만 `handler.onBarcodeScanned(msg)` 위임으로 교체

### 5.2 AsyncTask 5종의 상태 필드 참조

- `ProgressDlgShipSelect`(2500), `ProgressDlgShipSelectBL`(2694), `ProgressDlgShipmentSend`(2899)가 `arSM`, `current_work_position` 등을 직접 참조
- **Step 3에서 함께 `ctx` 경유로 교체해야 한다.** Activity에 필드를 남기면 이중 상태가 되어 조용히 깨진다
- 클래스 분리·중복 제거(`ShipSelect` ↔ `ShipSelectBL` 112줄 중복)는 이 문서 범위 밖

### 5.3 Handler·다이얼로그의 상태 필드 참조

- `mHandler`(844~1094), `show_wetDetailDialog`(3358~3547) 등도 동일 필드를 참조
- **대응**: Step 3에서 함께 교체. 로직은 그대로

### 5.4 setBarcodeMsg 내에서 호출하는 Activity 메서드

```
find_PackerProduct(1176)  find_PackerProductBarcodeGoods(1180)
new ProgressDlgShipSelect(1199, 1244)  show_wetFinishDialog(1223)
set_scanFlag(1218)  wet_data_insert(1534)
```

- 이 문서에서는 **이관하지 않는다.** Handler가 Activity 참조를 통해 그대로 호출한다
- 개발64에서 순차 이관하며, 그때까지 Handler는 Activity 참조를 1개 보유한다
- **대응**: Handler 생성자에 Activity를 전달하되, **위젯·상태 필드 접근은 금지**하고 위 메서드 호출만 허용한다

### 5.5 개발64와의 중복

- 개발64의 Step 2~9와 이 문서의 Step 2~11이 겹친다
- **대응**: 이 문서를 먼저 완료한 뒤, 개발64의 진행 현황에서 완료분을 반영하고 나머지(라벨·전송·AsyncTask)만 진행한다

---

## 6. 데이터 저장 구조

### WeighingContext 이관 대상 (setBarcodeMsg 참조 기준)

| Activity 필드 | Context 필드 | 타입 | 용도 | 참조 |
|---|---|---|---|---:|
| `work_item_bi_info` | `workItemBiInfo` | Barcodes_Info | 현재 작업 바코드 정보 | 50 |
| `current_work_position` | `currentWorkPosition` | int | 작업 대상 인덱스 | 35 |
| `arSM` | `arSM` | ArrayList\<Shipments_Info\> | 출하 대상 목록 | 32 |
| `work_item_fullbarcode` | `workItemFullbarcode` | String | 스캔된 전체 바코드 | 23 |
| `work_ppcode` | `workPpcode` | String | 작업 중 패커상품코드 | 9 |
| `weight_from` / `weight_to` | `weightFrom` / `weightTo` | String | 중량 유효 범위 | 18 |
| `temp_weight_double` | `tempWeightDouble` | double | 임시 중량 | 8 |
| `temp_weight` | `tempWeight` | String | 임시 중량 문자열 | 6 |
| `scan_flag` | `scanFlag` | boolean | 상품스캔(true)/BL스캔(false) | 5 |
| `work_item_barcodegoods` | `workItemBarcodegoods` | String | 매칭된 BARCODEGOODS | 4 |
| `work_bl_no` | `workBlNo` | String | 작업 중 BL번호 | 4 |
| `dialog_flag` | `dialogFlag` | boolean | 다이얼로그 표시 중 가드 | 4 |
| `centerTotalCount` | `centerTotalCount` | int | 센터 총 요청수량 | 4 |
| `temp_bl_no` | `tempBlNo` | String | 스캔된 BL번호 | 3 |
| `lastBarcodeProcessedTime` | `lastBarcodeProcessedTime` | long | 디바운싱 기준시각 | 3 |
| `work_flag` | `workFlag` | int | 작업 상태 (1=상품바코드) | 2 |
| `lastProcessedBarcode` | `lastProcessedBarcode` | String | 디바운싱 기준 바코드 | 2 |
| `centerWorkCount` | `centerWorkCount` | int | 센터 계근 완료수량 | 2 |

### ShipmentScreen 이관 대상 (setBarcodeMsg 참조 기준)

| Activity 멤버 | Screen 메서드 | 참조 |
|---|---|---:|
| `vibrator.vibrate(ms)` | `vibrate(int ms)` | 8 |
| `sp_bl_no` | `getSelectedBl()` | 4 |
| `sp_center_name` | `getSelectedCenter()` | 2 |
| `sp_point_name` | `moveToWorkItem(int)` | 1 |
| `sList` | `moveToWorkItem(int)` | 1 |
| `edit_barcode` | `showBarcode(String)` | 1 |

**전체 위젯 16종 중 6종만 이 문서 범위.** 나머지는 개발64 Step 2에서 이관한다.

> 위 `WeighingContext` 표는 **18행 / 필드 19개**다. `weight_from`·`weight_to`가 한 행에 묶여 있다.
> `lotteBoxOrder`(1916)는 `wet_data_insert`의 **지역변수**이므로 이관 대상이 아니다.

### searchType 매핑

```
"0" → EmartHandler(nonfixed=false)         "4" → EmartHandler(nonfixed=true)
"1" → ProductionHandler(labelOnly=false)   "7" → ProductionHandler(labelOnly=true)
"2" → HomeplusHandler(nonfixed=false)      "5" → HomeplusHandler(nonfixed=true)
"3" → WholesaleHandler                     "6" → LotteHandler
```

---

## 7. 호출 시점

```
[앱 시작]
    ↓
[MainActivity]  출하대상받기 → searchType 확정 → 계근입력시작
    ↓
[BixolonShipmentActivity.onCreate()]
    ├── screen  = new ShipmentScreen(this)
    ├── ctx     = new WeighingContext()
    └── ★ handler = ShipmentTypeFactory.create(Common.searchType, screen, ctx, this)
            ↓
[PDA 스캐너 트리거 또는 Keyboard Wedge 입력]
    ↓
[HoneywellScannerActivity]  바코드 수신
    ↓
[BixolonShipmentActivity.setBarcodeMsg(msg)]     ← 콜백. 본문 1줄
    ↓
★ handler.onBarcodeScanned(msg)
    │
    ├─[1차] scan_flag=true  상품 스캔
    │   ├── find_PackerProduct(msg)              ← Activity 메서드 (잔류)
    │   ├── ProgressDlgShipSelect 실행           ← Activity 내부클래스 (잔류)
    │   └── set_scanFlag(false) → this.onBarcodeScanned(msg)  ★ 재귀
    │
    └─[2차] scan_flag=false  BL 스캔
        ├── ctx.isCurrentBl(msg)                 ← WeighingContext
        ├── WeightUtil.convertLb(...)            ← 순수 함수
        └── wet_data_insert(...)                 ← Activity 메서드 (잔류)
                ↓
        [로컬 SQLite 저장 → 라벨 출력 → 전송 대기]
```

---

## 8. 개발 플랜

### Step 1: setBarcodeMsg 안전 정리

**Part 1. 분석**
- 메서드: `setBarcodeMsg`
- 범위: `BixolonShipmentActivity.java:1261`
- 용도: 이후 Step에서 코드를 읽을 때 오도되지 않도록 시야 확보
- 주의할 점: **동작에 영향을 주는 코드는 한 줄도 건드리지 않는다**

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | `if (true)` | 1261 | 조건만 제거. **중괄호 블록과 들여쓰기는 유지**하여 diff 최소화 |

**Part 2. 변환 계획**
- 변환 방식: 조건식만 삭제
- 주의사항: 들여쓰기를 바꾸면 이후 Step의 diff가 커져 검증이 어려워진다. 위험하면 이 Step은 생략 가능

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

### Step 2: 순수 함수 추출 — BarcodeUtil / WeightUtil

**Part 1. 분석**
- 메서드: `setBarcodeMsg` 내 `ITEM_TYPE` 블록 + 디바운싱
- 범위: `1154~1164`, `1371·1424·1486`, `1390·1437·1499`, `2122~2126`
- 용도: **setBarcodeMsg 단독으로 가능한 유일한 축소.** 434줄 → 약 380줄
- 주의할 점: 이마트 LB 자릿수 분기(1443, 1505)는 **마트별 차이이므로 유틸에 넣지 않는다**

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 디바운싱 판정 | 1154~1164 | `BarcodeUtil.isDebounced()` |
| 2 | 바코드 구간 추출 | 2122~2126 | `BarcodeUtil.extractBarcodeGoods()` |
| 3 | 중량 범위 검증 | 1371, 1424, 1486 | `WeightUtil.isRangeUnset()` — 3중복 → 1벌 |
| 4 | LB 단위 환산 | 1390, 1437, 1499 | `WeightUtil.convertLb()` — 3중복 → 1벌 |

**Part 2. 변환 계획**
- 변환 방식: 표현식을 그대로 옮기고 호출로 치환. **순수 치환이어야 한다**
- 주의사항: diff에 조건문이 새로 생기면 잘못된 것. `setBarcodeMsgProduction`(1577~1883)에도 동일 블록이 있으면 함께 치환한다

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

### Step 3: WeighingContext 도입

**Part 1. 분석**
- 메서드: 파일 전역 (상태 필드 접근 전부)
- 범위: `333~380`(필드 선언), `2094~2105`(`find_BL`), 참조 지점 전역
- 용도: 계근 상태 19종을 Activity 밖으로 분리
- 주의할 점: **AsyncTask 5종·Handler·다이얼로그도 같은 필드를 참조한다. 함께 교체해야 한다**

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 상태 필드 19종 | 333~380 | 6장 매핑표대로 이동 |
| 2 | `find_BL` | 2094~2105 | `ctx.isCurrentBl(barcode)` |
| 3 | AsyncTask 참조 | 2500~3357 | `ctx` 경유로 교체 |
| 4 | Handler·다이얼로그 참조 | 844~1094, 3358~3738 | `ctx` 경유로 교체 |

**Part 2. 변환 계획**

이 Step이 이 문서에서 가장 큰 단일 변경이다(`setBarcodeMsg` 190여 곳 + AsyncTask 79건 + Handler 8건). **3단계로 쪼개 컴파일러가 누락을 잡게 한다.**

| 하위 | 작업 | 검증 수단 |
|---|---|---|
| **3-a** | `WeighingContext` 클래스 생성. 필드 19종을 **복사**(Activity 필드는 아직 유지). Activity에 `ctx` 필드 추가 후 생성자에서 초기화 | 컴파일 통과. **동작 변화 0** (아무도 `ctx`를 안 씀) |
| **3-b** | 참조를 `ctx.getXxx()`/`ctx.setXxx()`로 치환. 구간별로 나눠 진행 — ① `setBarcodeMsg` → ② `setBarcodeMsgProduction` → ③ AsyncTask 5종 → ④ Handler·다이얼로그 | 각 구간 후 컴파일 + 실기기 1건 |
| **3-c** | **Activity 필드 19종 삭제** | **컴파일 에러 목록 = 치환 누락 목록.** 0건이 될 때까지 반복 |

- 변환 방식: 필드 직접 접근 → getter/setter 치환. 초기값·초기화 시점을 원본과 동일하게 유지
- 주의사항: **3-c를 반드시 수행한다.** 3-b에서 멈추고 Activity 필드를 남기면 컴파일은 되지만 두 곳에 상태가 생겨 조용히 깨진다 (예상 문제점 #4)
- 주의사항: `find_BL`(2094~2105)을 `ctx.isCurrentBl()`로 옮길 때 호출처도 함께 교체

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

### Step 4: ShipmentScreen 도입 (setBarcodeMsg 범위)

**Part 1. 분석**
- 메서드: 파일 전역 (대상 6종의 모든 호출처)
- 범위: `280~332`(위젯 필드), `411~526`(findViewById), 호출 지점 전역
- 용도: `setBarcodeMsg`가 쓰는 위젯 6종을 Activity 밖으로 분리
- 주의할 점: **6종만 옮기되 그 6종의 호출 89회를 전부 바꾼다.** 일부만 바꾸면 컴파일이 깨진다. 나머지 위젯 10종(62회)은 Activity 잔류 — 개발64 Step 2

| # | 항목 | 위치 | 호출 수 | 내용 |
|---|------|------|---:|------|
| 1 | `vibrator` | 필드 | 28 | `screen.vibrate(int)` |
| 2 | `edit_barcode` | 280~332 | 15 | `showBarcode`/`readBarcode`/`setBarcodeKeyListener` |
| 3 | `sp_bl_no` | 280~332 | 14 | `getBlAt`/`getBlPosition`/`getBlCount`/`setBlSelection`/`setBlAdapter` |
| 4 | `sp_center_name` | 280~332 | 12 | `getSelectedCenter`/`setCenterAdapter`/`setCenterListener` |
| 5 | `sp_point_name` | 280~332 | 12 | `moveToWorkItem`/`getPointPosition`/`getSelectedPoint`/`setPointAdapter`/`setPointListener` |
| 6 | `sList` | 280~332 | 8 | `moveToWorkItem`/`setListAdapter` |
| 7 | `findViewById` 5회 + vibrator | onCreate | – | 생성자로 이동 |
| **소계** | | | **89** | |

**Part 2. 변환 계획**
- 변환 방식: 순수 치환. 레이아웃 2종의 위젯 id가 완전 동일함을 확인했으므로 `null` 가드 불필요
- 주의사항 1: `sp_center_name.getSelectedItem().toString()`(1199, 1244)은 `ProgressDlgShipSelect` 생성자 인자다. 값이 동일하게 전달되는지 확인
- 주의사항 2: `sList.setSelection` + `sp_point_name.setSelection`은 **항상 짝으로 호출**된다(각 6회). `moveToWorkItem()`으로 묶되, **한쪽만 호출하는 지점이 있는지 먼저 확인**하고 있으면 개별 메서드로 보존한다
- 주의사항 3: Activity 필드 6종을 **반드시 제거**한다. 남기면 Activity와 Screen이 같은 위젯을 각각 들고 있게 된다

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

### Step 5: 인터페이스 + Factory 골격 (동작 변화 0)

**Part 1. 분석**
- 메서드: `ShipmentTypeHandler`, `ShipmentTypeFactory`, 마트 클래스 5개 (빈 껍데기)
- 범위: 신규 파일 7개 + `onCreate`, `setBarcodeMsg`
- 용도: 이후 마트 이관의 착지점 마련
- 주의할 점: 이 Step에서는 **각 Handler가 Activity의 기존 메서드를 그대로 호출**한다. 로직 이동 없음

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 인터페이스 | 신규 | `onBarcodeScanned`, `allowDuplicateBarcode` 2개 |
| 2 | Factory | 신규 | searchType 8 → Handler 5 (플래그 구분) |
| 3 | 마트 클래스 5개 | 신규 | `activity.setBarcodeMsg_legacy(msg)` 위임 껍데기 |
| 4 | Activity 연결 | onCreate, 1143 | `handler` 필드 추가 + 위임 |

**Part 2. 변환 계획**
- 변환 방식: 기존 `setBarcodeMsg` 본문을 `setBarcodeMsg_legacy`로 이름만 바꾸고, `setBarcodeMsg`는 `handler.onBarcodeScanned(msg)`로 교체
- 주의사항: **이 Step 완료 후 앱 동작이 완전히 동일해야 한다.** 실기기로 이마트 1건 계근하여 확인

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

### Step 6: 생산(1, 7) 이관 — ProductionHandler.onBarcodeScanned

**Part 1. 분석**
- 메서드: `setBarcodeMsgProduction`(1577~1883)
- 범위: `1145`(진입 분기), `1577~1883`
- 용도: 첫 이관 대상. 이미 분리돼 있어 위험 최소이며 골격 검증용
- 주의할 점: 1.5의 Javadoc "제외된 분기" 6종이 정답지다. 개발62 롤백은 `cbbfd22`로 커밋 완료되어 선행조건 충족
- **검증 기준 주의**: 원본 프로젝트(`PDA-INNO(원본)`)에는 `setBarcodeMsgProduction`이 **없다**(개발60에서 신설). 따라서 이 Step은 원본 대조가 불가능하며, **현행 코드 동작 + 개발60 문서**를 기준으로 검증한다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | `setBarcodeMsgProduction` 본문 | 1577~1883 | `ProductionHandler.onBarcodeScanned`로 이동 |
| 2 | 생산 진입 분기 제거 | 1145~1148 | `setBarcodeMsg_legacy`에서 삭제 |
| 3 | 재귀 호출 | 본문 내 | `this.onBarcodeScanned(msg)`로 교체 (1.4) |

**Part 2. 변환 계획**
- 변환 방식: 메서드 본문을 그대로 옮기고 필드 접근만 `ctx`/`screen` 경유로 교체
- 주의사항: **이관 후 `git diff`에 생산 외 마트 코드가 나오면 안 된다**

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (생산 계근 실기기)
- [ ] Part 6: 회귀테스트 (이마트·롯데 정상 확인)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 7: 롯데(6) 이관 — LotteHandler.onBarcodeScanned

**Part 1. 분석**
- 메서드: `setBarcodeMsg_legacy` 중 롯데 경로
- 범위: `1150~1546` 전체를 롯데 기준으로 복제 후 불필요 분기 제거
- 용도: 박스순번 파이프라인이 뚜렷한 차이라 검증에 적합
- 주의할 점: **개발63과 순서 제약 없음.** 개발63은 전송(`ProgressDlgShipmentSend` 2899~3170)과 JSP를 다루고 이 Step은 `setBarcodeMsg`(1143~1576)만 다뤄 구간이 겹치지 않는다. 사용자 결정에 따라 **클래스 분리를 먼저 완료한 뒤 개발63을 진행**한다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 상품 스캔 블록 | 1169~1254 | 롯데 경로만 남김 |
| 2 | BL 스캔 블록 | 1255~1542 | 롯데 경로만 남김 |
| 3 | 중복검사 | 1205, 1329 | 롯데는 수행 (`allowDuplicateBarcode()=false`) |
| 4 | 이마트 전용 분기 제거 | 1294, 1443, 1505 | 코드 게이트. 롯데 미해당이므로 제거 |
| 5 | 킬코이·미트센터 검증 | 1285~1292 | **데이터 의존. 유지** |
| 6 | `ITEM_TYPE` 블록 | 1366~1533 | VIEW 확인 전까지 **전부 유지** |
| 7 | `lotteBoxOrder` | 1916 | **이관 대상 아님.** `wet_data_insert`의 지역변수이며 `setBarcodeMsg` 밖이다 |

**Part 2. 변환 계획**
- 변환 방식: 1.6 **코드 게이트** 표 기준으로만 제거한다. 데이터 의존 분기(킬코이·미트센터, `ITEM_TYPE`)는 **유지**
- 주의사항: `VW_PDA_WID_LIST_LOTTE`가 내보내는 `ITEM_TYPE` 값을 확인하고 1.6 표를 갱신한다. 확인 전에는 블록을 제거하지 않는다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (롯데 계근 실기기)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 8: 홈플러스(2, 5) 이관 — HomeplusHandler.onBarcodeScanned

**Part 1. 분석**
- 메서드: `setBarcodeMsg_legacy` 중 홈플러스 경로
- 범위: `1205`, `1329`, `1481~1533`(ITEM_TYPE B), `1150~1546`
- 용도: 정량·비정량을 플래그 하나로 처리하는 방식 검증
- 주의할 점: `ITEM_TYPE B`는 상수 주석(201줄)과 1.5 Javadoc이 "홈플러스 비정량"이라 하지만 **코드 게이트가 없다.** VIEW 실측 전에는 다른 마트에서도 제거하지 않는다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 중복검사 제외 | 1205, 1329 | `nonfixed=true`(5)일 때만 |
| 2 | `ITEM_TYPE B` | 1481~1533 | **데이터 의존.** `VW_PDA_WID_HOMEPLUS_LIST` 확인 후 1.6 표 갱신 |
| 3 | 이마트 전용 분기 제거 | 1294, 1443, 1505 | 코드 게이트. 홈플러스 미해당이므로 제거 |
| 4 | 킬코이·미트센터 검증 | 1285~1292 | **데이터 의존. 유지** |

**Part 2. 변환 계획**
- 변환 방식: `HomeplusHandler(nonfixed)` 하나로 2·5 모두 처리
- 주의사항: 홈플러스는 운영 DB에 데이터가 0건일 수 있다. 테스트 데이터 확보 후 진행

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (홈플러스 정량·비정량 각 1건)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 9: 도매(3) 이관 — WholesaleHandler.onBarcodeScanned

**Part 1. 분석**
- 메서드: `setBarcodeMsg_legacy` 중 도매 경로
- 범위: `1150~1546`
- 용도: 남은 마트 중 가장 단순. 골격 안정성 확인
- 주의할 점: 도매는 `setBarcodeMsg` 내 전용 분기가 없다. 공통 경로만 탄다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 공통 경로 | 1150~1546 | 도매 기준으로 복제 |
| 2 | 이마트·비정량 분기 제거 | 1205, 1294, 1329, 1443, 1505 | 코드 게이트. 도매 미해당이므로 제거 |
| 3 | 킬코이·미트센터 검증 | 1285~1292 | **데이터 의존. 유지** |
| 4 | `ITEM_TYPE` 블록 | 1366~1533 | `VW_PDA_WID_WHOLESALE_LIST` 확인 전까지 유지 |

**Part 2. 변환 계획**
- 변환 방식: Step 7과 동일
- 주의사항: 도매는 레이아웃이 다르나 위젯 id가 동일하므로 `ShipmentScreen`은 공용

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (도매 계근 실기기)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 10: 이마트(0, 4) 이관 — EmartHandler.onBarcodeScanned

**Part 1. 분석**
- 메서드: `setBarcodeMsg_legacy` 잔여 전체
- 범위: `1150~1546`
- 용도: 기준 구현. 다른 마트 4종으로 골격을 검증한 뒤 마지막에 이관
- 주의할 점: 유통기한 검증(1285~1304), LB 자릿수 분기(1443, 1505), 센터명 6종 하드코딩

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 킬코이·미트센터 검증 | 1285~1292 | `EmartHandler` private |
| 2 | TRD/E-T/WET 검증 | 1293~1304 | `EmartHandler` private. 센터명 6종 문자열 유지. `else if`(1293)의 1285와의 배타 관계 유지 |
| 3 | LB 환산 자릿수 | 1443, 1505 | `if (EMART)` — `else`가 없으므로 나머지 마트 경로도 확인 |
| 4 | 중복검사 제외 | 1205, 1329 | `nonfixed=true`(4)일 때만 |
| 5 | 잔여 `setBarcodeMsg_legacy` 제거 | 1150~1546 | 전 마트 이관 완료 후 삭제 |

**Part 2. 변환 계획**
- 변환 방식: `EmartHandler(nonfixed)` 하나로 0·4 처리
- 주의사항: 1443·1505의 `if (EMART)`에 `else`가 없다. 다른 마트 Handler에는 **else 경로 코드**가 들어가야 한다. Step 7~9에서 이미 반영됐는지 교차 확인

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (이마트 정량·비정량 각 1건)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 11: setBarcodeMsgProduction 제거 + 정리

**Part 1. 분석**
- 메서드: `setBarcodeMsgProduction`, `setBarcodeMsg_legacy`
- 범위: `1143~1883`
- 용도: 중복 201줄 소멸 및 Activity 축소
- 주의할 점: 전 마트 이관 완료 후에만 진행

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | `setBarcodeMsg_legacy` 삭제 | 1150~1546 | Step 10에서 비워졌는지 확인 후 |
| 2 | `setBarcodeMsgProduction` 삭제 | 1577~1883 | 307줄 |
| 3 | `setBarcodeMsg` 최종 확인 | 1143 | 본문 1줄 (`handler.onBarcodeScanned`) |
| 4 | 미사용 상수 확인 | 158~168 | `SEARCH_TYPE_*` 사용처가 Factory로 옮겨졌는지 |

**Part 2. 변환 계획**
- 변환 방식: 잔여 코드 삭제
- 주의사항: `SEARCH_TYPE_*` 상수는 다른 메서드(`onStart`, `wet_data_insert`, AsyncTask)에서도 쓰이므로 **삭제하지 않는다.** 개발64 완료 후 정리

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

### Step 12: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 이마트(0) 상품 스캔 → BL 스캔 → 계근 정상 | □ |
| 2 | 이마트 비정량(4) 동일 바코드 연속 스캔 시 중복확인 미발생 | □ |
| 3 | 생산(1) 계근 정상 (프린터 비활성 상태 유지) | □ |
| 4 | 홈플러스(2) 계근 정상 | □ |
| 5 | 홈플러스비정량(5) 중복확인 미발생 + `ITEM_TYPE B` 처리 정상 | □ |
| 6 | 도매(3) 계근 정상 (wholesale 레이아웃) | □ |
| 7 | 롯데(6) 계근 + 박스순번 채번 정상 | □ |
| 8 | **같은 상품 재스캔 → BL 스캔 자동 전환 (재귀 호출)** | □ |
| 9 | 다른 상품 스캔 → 확인 다이얼로그 → 예/아니오 각각 정상 | □ |
| 10 | 동일 바코드 1초 이내 재스캔 시 무시 (디바운싱) | □ |
| 11 | BL 불일치 바코드 스캔 시 토스트 + 진동 | □ |
| 12 | 총 계근 완료 시 완료 다이얼로그 표시 | □ |
| 13 | `ITEM_TYPE` W/HW/S/J/B 각각 중량 추출 정확 | □ |
| 14 | LB 단위 상품 환산값 정확 (이마트 자릿수 포함) | □ |
| 15 | `setBarcodeMsg` 본문이 1줄인지 확인 | □ |
| 16 | 바코드 처리 경로에 `Common.searchType` 참조가 0인지 확인 | □ |

---

### 개발 순서 요약

```
Step 1: setBarcodeMsg 안전 정리 (if(true))
    ↓
Step 2: 순수 함수 추출 ← setBarcodeMsg 단독 가능. 434줄 → 약 380줄
    ↓
Step 3: WeighingContext 도입 (3-a/3-b/3-c) ← 전역 불가피 (AsyncTask 동반)
    ↓
Step 4: ShipmentScreen 도입 (6종/89회) ← 전역 불가피
    ↓
Step 5: 인터페이스 + Factory 골격   ← 동작 변화 0 (위임만)
    ↓
Step 6: 생산(1,7)     → ProductionHandler   ← 여기서부터 마트사별 검증
    ↓
Step 7: 롯데(6)       → LotteHandler
    ↓
Step 8: 홈플러스(2,5) → HomeplusHandler
    ↓
Step 9: 도매(3)       → WholesaleHandler
    ↓
Step 10: 이마트(0,4)  → EmartHandler
    ↓
Step 11: setBarcodeMsgProduction 제거 + 정리
    ↓
Step 12: 통합 테스트
```

**Step 1~5는 동작이 한 줄도 바뀌지 않아야 한다. Step 6 이후로는 이관하지 않은 마트가 `git diff`에 나오지 않아야 한다.**

---

## 9. 테스트 시나리오

### 시나리오 1: Step 1~5 무변화 검증

```
1. Step 진행 전 이마트 계근 1건 수행 → logcat 저장
   (상품스캔 → BL스캔 → wet_data_insert 전 구간)
2. Step 진행
3. 동일 조건으로 이마트 계근 1건 수행 → logcat 저장
4. 두 logcat의 처리 순서·값 대조 → 완전 동일해야 함
5. git diff 확인 → 조건문이 새로 추가되지 않았는지 확인
```

### 시나리오 2: 재귀 호출 보존 검증 (Step 6~10 공통)

```
1. 해당 마트 출하대상 받기
2. 상품 바코드 스캔 (1차)
3. 동일 상품 바코드 재스캔 (2차)
4. logcat 확인:
   "상품스캔일반" → set_scanFlag(false) → "BL스캔" 순서로 이어지는지
5. 디바운스에 막혀 2차 스캔이 무시되면 실패 (1226줄 우회 누락)
```

### 시나리오 3: 마트 이관 후 격리 검증 (Step 6~10 공통)

```
1. git diff --stat 실행
2. 이관 대상 마트 Handler + setBarcodeMsg_legacy만 변경되었는지 확인
3. 다른 마트 Handler 파일이 변경 목록에 없는지 확인
4. 있으면 → 공통/차이 분류 오류. 되돌리고 재분류
```

### 시나리오 4: 중복검사 호출 보존 검증 (Step 8, 10)

```
1. 비정량(4 또는 5) 출하대상 받기
2. 이미 계근한 바코드로 상품 스캔 → BL 스캔까지 진행
3. logcat 확인 — 두 메서드가 모두 호출되어야 한다
   [A] 상품 스캔 : "duplicatequeryGoodsWet_check -> ..."   (DBHandler:1445~1447)
   [B] BL 스캔   : "duplicatequeryGoodsWet -> ..."          (DBHandler:1466~)
4. 둘 중 하나라도 호출이 사라졌으면 실패 (4.4 참조)
5. 정량(0 또는 2)으로 같은 시나리오 → "오류지점1" 또는 "오류지점2" 로그와
   "이미 스캔한 바코드입니다" 토스트가 떠야 한다
```

### 시나리오 5-1: 데이터 의존 분기 보존 검증 (Step 7~10 공통)

```
1. 이관한 Handler 소스에서 다음이 남아 있는지 확인
   - 킬코이 PACKER_CODE + 미트센터 STORE_CODE 검증 (1285~1292)
   - ITEM_TYPE W/HW, S, J, B 블록 (VIEW 미확인 마트)
2. 제거했다면 → 해당 마트 VIEW DDL로 미출력을 증명했는지 확인
3. 증명 없이 제거했으면 되돌린다
```

### 시나리오 5-2: ITEM_TYPE 전수 검증 (Step 10 이후)

```
1. ITEM_TYPE별 테스트 데이터 준비 (W, HW, S, J, B)
2. 각 타입 1건씩 계근
3. 중량·제조일·박스시리얼 추출값을 원본 앱과 대조
4. LB 단위 상품은 환산값까지 확인 (이마트 자릿수 분기 포함)
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 재귀 호출 유실 | 1227줄을 `activity.setBarcodeMsg()`로 옮김 | `this.onBarcodeScanned(msg)`로 자기 자신 호출 (4.3) |
| 2 | 디바운스에 재귀가 막힘 | 1226줄 `lastBarcodeProcessedTime = 0` 누락 | 재귀 직전 `ctx.setLastBarcodeProcessedTime(0)` 반드시 유지 |
| 3 | 중복검사 호출 자체가 사라짐 | "어차피 `false`로 덮으니 건너뛰자"고 최적화 | 원본은 조회 후 덮는다. **조회는 그대로 수행** (4.4) |
| 4 | Step 3에서 상태 이중화 | Activity 필드를 남긴 채 Context에도 추가 | 필드를 **반드시 Activity에서 제거** |
| 5 | `if (EMART)` else 경로 누락 | 1443·1505에 `else`가 없어 나머지 마트 동작을 놓침 | Step 7~9에서 else 경로를 각 Handler에 반영. Step 10에서 교차 확인 |
| 6 | **데이터 의존 분기를 코드 게이트로 오인해 제거** | 킬코이·미트센터(1285), `ITEM_TYPE`(1366~1533)은 `searchType` 게이트가 없다. 주석·Javadoc만 보고 "이 마트 전용"이라 판단 | 1.6의 **코드 게이트 표에 있는 것만 제거**한다. 데이터 의존은 VIEW 실측으로 증명 전까지 전 마트 유지 |
| 7 | `setBarcodeMsg` 시그니처 변경 | 파라미터 추가 유혹 | `HoneywellScannerActivity` 콜백. 변경 금지 |
| 8 | Handler가 Activity 필드를 직접 조작 | 5.4의 Activity 메서드 호출 허용을 확대 해석 | **메서드 호출만 허용, 필드·위젯 접근 금지** |
| 9 | 개발62와 충돌 | 동일 파일 수정 | 개발62 롤백은 `cbbfd22`로 커밋 완료(`DBHandler.java:99` 확인). **선행조건 충족** |
| 10 | 개발63과 충돌 | 롯데 박스순번 건 | **순서 제약 없음.** 개발63은 2899~3170 + JSP, 이 문서는 1143~1883. 겹치지 않음. **이 문서 완료 후 개발63 진행** |
| 11 | 홈플러스 테스트 데이터 없음 | 운영 DB 0건 | 테스트 데이터 확보 후 Step 8 진행 |
| 12 | 개발64와 중복 진행 | 두 문서가 같은 파일 수정 | 이 문서 완료 후 개발64 진행 현황에 반영하고 나머지만 진행 |
| 13 | **중복검사 메서드 혼동** | `duplicatequeryGoodsWet_check`(2인자, 상품스캔 1193·1203)와 `duplicatequeryGoodsWet`(5인자, BL스캔 1326)은 **다른 메서드**다 | 4.4 표 기준으로 경로별 호출 메서드를 그대로 유지. 이름이 비슷해 바꿔 쓰면 중복 판정 기준이 달라진다 |
| 14 | `current_work_position == -1` 접근 순서 | 1277에서 `-1`이 될 수 있는데 **1283에서 먼저 `arSM.get()` 접근**하고 `-1` 체크는 1305에 있다 | **원본(`ShipmentActivity.java:791`)과 동일**하므로 순서를 바꾸지 않는다. `IndexOutOfBoundsException` 소지는 별건으로 `app/doc/오류/`에 문서화 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | setBarcodeMsg 안전 정리 (`if (true)`) | ⏳ 대기 |
| 2 | 순수 함수 추출 — BarcodeUtil / WeightUtil | ⏳ 대기 |
| 3 | WeighingContext 도입 (3-a 생성 / 3-b 치환 / 3-c 필드삭제) | ⏳ 대기 |
| 4 | ShipmentScreen 도입 (위젯 6종 · 호출 89회) | ⏳ 대기 |
| 5 | 인터페이스 + Factory 골격 | ⏳ 대기 |
| 6 | 생산(1,7) 이관 — ProductionHandler | ⏳ 대기 |
| 7 | 롯데(6) 이관 — LotteHandler | ⏳ 대기 |
| 8 | 홈플러스(2,5) 이관 — HomeplusHandler | ⏳ 대기 |
| 9 | 도매(3) 이관 — WholesaleHandler | ⏳ 대기 |
| 10 | 이마트(0,4) 이관 — EmartHandler | ⏳ 대기 |
| 11 | setBarcodeMsgProduction 제거 + 정리 | ⏳ 대기 |
| 12 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/개발/64_BixolonShipmentActivity_마트사별_클래스_분리.md` — 전체 분리 계획 (이 문서의 상위)
- `app/doc/개발/56_setBarcodeMsg_디바운스_수정[setBarcodeMsg_디바운스_바코드값_미비교].md` — 1154~1164 디바운싱 도입
- `app/doc/개발/60_setBarcodeMsg_생산1_전용메서드_분리.md` — `setBarcodeMsgProduction` 분리 (1.5 정답지)
- `app/doc/개발/62_생산_계근대상_지시일자_이후조회.md` — Step 6 선행 확인
- `app/doc/개발/63_롯데_박스순번_파이프라인_복구[36].md` — Step 7 선행 확인
- `app/doc/오류/32_홈플러스_월품목별재고_V뷰_년월조건_누락_출하대상_행중복[홈플러스_출하계근_AI정적검증].md`
- `app/doc/오류/33_홈플러스_SM_수주상세_LEFT_JOIN_다중행_팽창_출하건_중복[홈플러스_출하계근_AI정적검증].md`
- `app/doc/오류/34_홈플러스_STORE_IN_DATE_datetime_CONVERT_미적용_라벨_날짜_오인쇄[홈플러스_출하계근_AI정적검증].md`
- `app/doc/참고자료/오류패턴_분석.md`

---

**문서 버전**: 1.0
