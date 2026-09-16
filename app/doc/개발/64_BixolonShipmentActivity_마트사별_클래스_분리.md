# BixolonShipmentActivity 마트사별 클래스 분리

**작성일**: 2026-08-12
**목적**: 3,738줄 God Activity를 마트사(searchType)별 구체 클래스로 분리하여 마트 간 결합도를 제거한다. 기존 동작은 100% 동일하게 유지한다.

> ⚠️ **이 문서는 폐기되었습니다 (2026-09-16)**
> `66_BixolonShipmentActivity_searchType별_클래스_분리.md` 로 대체되었다.
> 이 문서의 "마트사 5개 클래스 + 정량/비정량 플래그" 설계는 소스 확인 결과 성립하지 않는다 —
> 이마트 0↔4가 7곳, 홈플러스 2↔5가 3곳 다르다(66 §1.3). 그 외 사실관계 오류 12건은 66 §10에 정정했다.
> **이 문서와 아래 65 참조 관계는 더 이상 유효하지 않다. 작업은 66을 따른다.**

> **선행 문서**
> `65_setBarcodeMsg_마트사별_분리.md` 가 이 계획의 **바코드 처리 축만 먼저 수행**한다.
> 65 완료 후 이 문서의 Step 1~5(안전정리·Screen·Context·Factory)와 Step 5~9의 바코드 부분이 함께 완료되므로,
> **진행 현황에 반영한 뒤 나머지(`wet_data_insert`·라벨·AsyncTask 전송)만 진행한다.**

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

### 추가 제약 조건 (이 가이드 한정)

- **이관하지 않은 마트는 `git diff`에 한 줄도 나오지 않아야 한다.** 나오면 공통/차이 분류가 틀린 것이므로 되돌린다
- 원본(`D:\PDA\PDA-INNO(원본)`)과 동작이 다른 코드를 발견해도 **이 문서에서는 고치지 않는다**. `app/doc/오류/`에 별건 문서화만 한다
- 공통 메서드로 추출하는 대상은 **상태를 변경하지 않는 순수 함수만** 허용한다
- 마트 클래스 간에는 상속·공유 골격을 두지 않는다 (인터페이스 선언만 공유)

---

## 1. 현재 구조

### BixolonShipmentActivity.java (3,738줄)

| 구간           | 라인        | 내용                                                    |
| ------------ | --------- | ----------------------------------------------------- |
| 상수·필드        | 154~410   | 5개 섹션으로 분산                                            |
| 생명주기         | 411~605   | onCreate/onResume/onStart/onPause/onDestroy           |
| 버튼 리스너       | 606~843   |                                                       |
| Handler      | 844~1094  | mHandler, mBixolonHandler                             |
| 바코드 처리       | 1095~1883 | `setBarcodeMsg` 434줄(1143~1576) + `setBarcodeMsgProduction` 307줄(1577~1883) |
| 핵심 비즈니스 로직   | 1884~2363 | `wet_data_insert` 150줄(1884~2033), `find_*` 계열(2059~2323), `calc_info`(2324~2363) |
| SLCS 잔재      | 2364~2466 | `slcsInit`~`slcsFeedToMark` 8개. `LabelPrintHelper`와 중복 |
| 프린터 전송       | 2467~2499 | `sendData` 11줄                                        |
| AsyncTask 5종 | 2500~3357 | `onActivityResult`(3214~3308)가 `PrintConnect`와 `Discon` 사이에 끼어 있음 |
| 다이얼로그        | 3358~3738 | `show_wetDetailDialog`(3358~3547) 외                    |

### 측정 지표

| 항목 | 값 |
|---|---:|
| 전체 줄 수 | 3,738 |
| 메서드 수 | 158 |
| `if` 문 | 267 |
| `Common.searchType` 분기 지점 | **51** |
| 인스턴스 필드 | 96 (가변 42) |
| 위젯 필드 | 16 |
| 위젯 조작 호출 | **123** (119개 줄) |
| 최대 들여쓰기 깊이 | 14 |
| 깊이 6단계 이상 줄 | 969 / 3,288 (29.5%) |

### searchType 분기 분포 (총 51곳)

| 구간 | 분기 수 |
|---|---:|
| AsyncTask 5종 | 15 |
| 비즈니스 로직 (`wet_data_insert` 등) | 15 |
| `setBarcodeMsg` | 6 |
| 생명주기 (onCreate/onStart) | 5 |
| Handler | 5 |
| 버튼 리스너 | 3 |
| 상수·필드 | 2 |

### 메서드별 순환복잡도 (상위)

| 순환복잡도 | 줄수 | 최대깊이 | 메서드 |
|---:|---:|---:|---|
| **30** | 434 | 10 | `setBarcodeMsg(String)` |
| 10 | 307 | 10 | `setBarcodeMsgProduction(String)` |
| 6 | 112 | 7 | `inputBtnListener` |
| 5 | 115 | 9 | `mHandler` |
| 4 | 116 | 7 | `onCreate` |

### 대표 코드 — 5가지 관심사 혼재 (`find_work_info` 2106~2172)

```java
private String find_work_info(String req, boolean type) {
    ArrayList<Barcodes_Info> list = DBHandler.selectqueryBarcodeInfo(this);   // ① DB 조회
    for (Barcodes_Info bi : list) {
        temp_bg = req.substring(parseInt(bg_from)-1, parseInt(bg_to));        // ② 순수 계산
        if (temp_bg.equals(bg)) {
            work_item_bi_info = bi;                                           // ③ 상태 쓰기
            edit_product_name.setText(bi.getITEM_NAME_KR());                  // ④ UI 쓰기
            edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
            work_item_barcodegoods = bg;
        }
        if (Common.searchType.equals(SEARCH_TYPE_NONFIXED)) {                 // ⑤ 마트 분기
            // 조건 없이 전부 매칭
        }
    }
}
```

### 문제점

1. **마트 간 결합** — `searchType` 분기 51곳이 7개 구간에 흩어져 있어, 한 마트를 고치면 다른 마트가 조용히 깨진다. 오류 32·33·34·36이 모두 이 유형이다
2. **복사 분리의 전례** — 생산(1)을 `setBarcodeMsgProduction`으로 떼어낸 결과, 유효 250줄 중 **201줄(80%)이 원본과 중복**됐다. 같은 방식으로 8종을 떼면 중복 8벌이 된다
3. **암묵적 상태 기계** — 가변 필드 42개를 메서드 간에 인자가 아닌 필드로 주고받는다. `setBarcodeMsg` 한 곳에서만 Activity 멤버 **25종**을 직접 읽고 쓴다
4. **위젯 조작 산재** — 짝으로 불러야 하는 호출이 흩어져 있다. `btn_send.setEnabled`(4회) ↔ `setBackgroundResource`(4회), `edit_product_name.setText`(10회) ↔ `edit_product_code.setText`(11회) — **개수가 맞지 않는다**
5. **거짓 주석·죽은 코드** — 2350·2359줄 "LabelPrintHelper로 이동됨"이라 적혀 있으나 8개 메서드가 그대로 남아 있다. `slcsBarcode`/`slcsLine`/`slcsBox`는 호출처 0. `if (true)` 2곳. 미사용 import 7개. 클래스 Javadoc의 클래스명·프린터명·searchType 표가 실제와 불일치

---

## 2. 변경 구조

### 데이터 흐름 — 변경 전

```
[바코드 스캔]
    ↓
BixolonShipmentActivity.setBarcodeMsg(msg)          ← 434줄, 순환복잡도 30
    ├── if (searchType == PRODUCTION) → setBarcodeMsgProduction()   (201줄 중복)
    ├── if (NONFIXED || HP_NONFIXED)  → 중복확인 제외
    ├── if (EMART)                    → 유통기한 검증
    ├── if (EMART)                    → 중량 처리
    ├── Activity 필드 25종 직접 읽기/쓰기
    └── 위젯 16개 직접 조작
```

### 데이터 흐름 — 변경 후

```
[바코드 스캔]
    ↓
BixolonShipmentActivity.setBarcodeMsg(msg)
    ↓
handler.onBarcodeScanned(msg)        ← 인터페이스 호출 (switch 없음)
    ↓
[LotteHandler / EmartHandler / ... 중 하나]
    ├── BarcodeUtil.xxx()      순수 함수 (상태 변경 없음)
    ├── WeightUtil.xxx()       순수 함수
    ├── ctx.setXxx()           WeighingContext — 계근 상태
    ├── screen.showXxx()       ShipmentScreen — 화면 조작
    └── nextBoxOrder() 등      마트 고유 private 메서드
```

### 클래스 구성

```
shipment/
  ShipmentTypeHandler.java      ← 인터페이스 (유일)
  ShipmentTypeFactory.java      ← searchType → Handler 생성. searchType 참조 유일 지점
  ShipmentScreen.java           ← 위젯 16개 소유, 화면 조작 123회
  WeighingContext.java          ← 계근 상태 필드 19종
  BarcodeUtil.java              ← static 순수 함수
  WeightUtil.java               ← static 순수 함수

  EmartHandler.java             (0, 4) 이마트 출하 / 이마트 비정량
  ProductionHandler.java        (1, 7) 생산 계근 / 생산 라벨(미사용)
  HomeplusHandler.java          (2, 5) 홈플러스 출하 / 홈플러스 비정량
  WholesaleHandler.java         (3)    도매 출하
  LotteHandler.java             (6)    롯데 출하
```

### 분리 기준 — 마트사 (계근방식은 클래스 내부 플래그)

축이 2개이고 서로 교차한다. **클래스는 마트사로 나누고, 계근방식(정량/비정량)은 생성자 플래그로 처리한다.**

| searchType | 마트사 | 계근방식 | 담당 클래스 |
|:-:|---|---|---|
| 0 | 이마트 | 정량 | `EmartHandler(nonfixed=false)` |
| 4 | 이마트 | 비정량 | `EmartHandler(nonfixed=true)` |
| 1 | 생산 | 계근 | `ProductionHandler(labelOnly=false)` |
| 7 | 생산 | 라벨(미사용) | `ProductionHandler(labelOnly=true)` |
| 2 | 홈플러스 | 정량 | `HomeplusHandler(nonfixed=false)` |
| 5 | 홈플러스 | 비정량 | `HomeplusHandler(nonfixed=true)` |
| 3 | 도매 | 정량 | `WholesaleHandler` |
| 6 | 롯데 | 정량 | `LotteHandler` |

**근거** — 코드가 두 축으로 번갈아 묶는다.

```java
// 마트사 축 — 라벨·전송 (무거운 로직). 클래스로 공유해야 중복이 없다
 939  if (HOMEPLUS || HOMEPLUS_NONFIXED)        // 라벨 재출력
2006  if (HOMEPLUS || HOMEPLUS_NONFIXED)        // setHomeplusPrinting
2969  if (HOMEPLUS || HOMEPLUS_NONFIXED)        // 전송
3061  if (PRODUCTION || PRODUCTION_LABEL)

// 계근방식 축 — 플래그 수준 (가벼운 로직). 필드 하나로 충분하다
1205  if (NONFIXED || HOMEPLUS_NONFIXED)        // 중복확인 제외
1329  if (NONFIXED || HOMEPLUS_NONFIXED)        // 중복확인 제외
3064  if (NONFIXED || HOMEPLUS_NONFIXED)
```

searchType별 8개로 나누면 홈플러스 라벨·전송 로직이 두 클래스에 **중복**되어, 이 문서가 없애려는 문제(문제점 2)가 재발한다. 마트사별 5개면 중복이 없다.

**searchType 4 소속 확정**: 이마트 비정량. 라벨이 이마트 것(`setPrinting`, 2014줄)이고 로그도 "이마트(비정량)"(2013줄)이다. 상수 주석(164줄)만 "도매 비정량"으로 잘못 적혀 있어 Step 1에서 정정한다.

### 목표 지표

| 지표 | 현재 | 목표 |
|---|---:|---:|
| `Common.searchType` 참조 지점 | 51 | **1** (Factory) |
| 마트 클래스가 참조하는 Activity 멤버 | 25 | **0** |
| 마트 클래스의 안드로이드 import | 다수 | **0** |
| 마트 A 수정이 마트 B에 미치는 영향 | 있음 | **없음** |
| `BixolonShipmentActivity` 줄 수 | 3,738 | 250 내외 |

### 공통/차이 판정 규칙

위에서부터 순서대로 적용한다.

```
① searchType 조건문 안에 있는가?                          → 차이 (마트 클래스)
② 마트별 JSP/VIEW/라벨을 참조하는가?                       → 차이 (마트 클래스)
③ 마트 고유 도메인 값이 박혀 있는가?                        → 차이 (마트 클래스)
   (킬코이 PACKER_CODE, 미트센터 STORE_CODE, 용인/대구/시화/여주TRD → 이마트)
④ 그 외 전부                                              → 공통 (골격 유지)
```

**애매하면 ④(공통)로 둔다.** 공통에 남기면 코드가 움직이지 않아 동작이 안 바뀐다. 성급히 "차이"로 분류해 마트 클래스로 옮기면 복사본이 생기고 되돌리기 어렵다.

### 차이점 도출 소스 4종

| # | 소스 | 알려주는 것 | 개수 |
|:-:|---|---|---:|
| 1 | `if (Common.searchType...)` | 앱 내부 동작 차이 | 51곳 |
| 2 | `setBarcodeMsg` ↔ `setBarcodeMsgProduction` diff | 생산의 실제 차이 (정답지) | 유효 250줄 중 49줄 상이 (중복 201줄) |
| 3 | `LabelPrintHelper`의 `setPrinting*` 4종 | 라벨 레이아웃 차이 | 4종 |
| 4 | JSP·VIEW (`search_shipment_*.jsp`, `VW_PDA_WID_*_LIST`) | 서버 계약·컬럼 차이 | 마트별 |

1번만 보면 놓친다. 조건문 없이 마트마다 다른 컬럼을 쓰는 경우는 4번에서만 드러난다 — 롯데 박스순번(개발63), 홈플러스 `STORE_IN_DATE`(오류34)가 그 사례다.

---

## 3. 수정 대상 파일

### 3.1 추가 파일 (신규 생성) — 11개

| # | 파일 | 위치 | 내용 | 착수 Step |
|:-:|------|------|------|:-:|
| 1 | **ShipmentScreen.java** | `.../highland_emart/shipment/` | 위젯 16개 소유, 화면 조작 123회. **개발65에서 6종/89회 선행 이관** → 이 문서는 나머지 10종/62회 추가 | Step 2 |
| 2 | **WeighingContext.java** | `.../highland_emart/shipment/` | 계근 상태 필드 19종 | Step 3 |
| 3 | **ShipmentTypeHandler.java** | `.../highland_emart/shipment/` | 인터페이스 (메서드 7개) | Step 4 |
| 4 | **ShipmentTypeFactory.java** | `.../highland_emart/shipment/` | searchType 8종 → Handler 5종 생성 | Step 4 |
| 5 | **BarcodeUtil.java** | `.../highland_emart/shipment/` | static 순수 함수 (바코드 추출·디바운싱) | Step 4 |
| 6 | **WeightUtil.java** | `.../highland_emart/shipment/` | static 순수 함수 (LB 환산·범위 검증) | Step 4 |
| 7 | **ProductionHandler.java** | `.../highland_emart/shipment/` | 생산 — searchType 1, 7 | Step 5 |
| 8 | **LotteHandler.java** | `.../highland_emart/shipment/` | 롯데 — searchType 6 | Step 6 |
| 9 | **HomeplusHandler.java** | `.../highland_emart/shipment/` | 홈플러스 — searchType 2, 5 | Step 7 |
| 10 | **WholesaleHandler.java** | `.../highland_emart/shipment/` | 도매 — searchType 3 | Step 8 |
| 11 | **EmartHandler.java** | `.../highland_emart/shipment/` | 이마트 — searchType 0, 4 | Step 9 |

#### 기존 소스 → 신규 파일 이관 매핑 (종합)

기준 파일: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java` (3,738줄)

| 기존 위치 | 라인 | 기존 메서드/블록 | → 신규 파일 | Step |
|---|---|---|---|:-:|
| 위젯 필드 선언 | 280~332 | `sp_*`, `edit_*`, `btn_*`, `sList` 16개 | `ShipmentScreen` | 2 |
| `onCreate` findViewById | 411~526 | `findViewById` 16회 | `ShipmentScreen` 생성자 | 2 |
| 위젯 조작 (전역) | 전역 | `setText`/`setSelection`/`setEnabled` 123회 | `ShipmentScreen` 메서드 | 2 |
| 상태 필드 선언 | 333~380 | `arSM`, `work_*`, `weight_*`, `center*` 19종 | `WeighingContext` | 3 |
| `find_BL` | 2094~2105 | BL번호 일치 판정 | `WeighingContext.isCurrentBl()` | 3 |
| 디바운싱 판정 | 1154~1164 | `lastProcessedBarcode` 비교 | `BarcodeUtil.isDebounced()` | 4 |
| 바코드 구간 추출 | 2122~2126 | `req.substring(from-1, to)` | `BarcodeUtil.extractBarcodeGoods()` | 4 |
| LB 단위 환산 | 1390, 1437, 1499 | `"LB".equals(BASEUNIT)` 3중복 | `WeightUtil.convertLb()` | 4 |
| 중량 범위 검증 | 1371, 1424, 1486 | `weight_from/to.equals("0")` 3중복 | `WeightUtil.isRangeUnset()` | 4 |
| searchType 분기 51곳 | 7개 구간 | `if (Common.searchType...)` | `ShipmentTypeFactory` | 4 |
| `setBarcodeMsgProduction` | 1577~1883 | 생산 바코드 처리 307줄 | `ProductionHandler` | 5 |
| 생산 프린터 비활성 | 518, 558, 564 | `onStart` 블루투스 분기 | `ProductionHandler.setupScreen()` | 5 |
| 생산 라벨 | 947, 2018~2020 | `setPrinting_prod` 호출 | `ProductionHandler.printLabel()` | 5 |
| 생산 전송 | 2971, 2973, 3015, 3061 | `ProgressDlgShipmentSend` 분기 | `ProductionHandler.getSendUrl()` | 5 |
| 롯데 박스순번 | 1916~1924 (`wet_data_insert` 지역변수) | 채번 로직 | `LotteHandler` *private* | 6 |
| 롯데 라벨 | 2015~2017 | `setPrintingLotte` 호출 | `LotteHandler.printLabel()` | 6 |
| 롯데 전송 | AsyncTask | `insert_goods_wet_lotte.jsp` | `LotteHandler.getSendUrl()` | 6 |
| 홈플러스 라벨 | 939, 2006~2008 | `setHomeplusPrinting` (2·5 공통) | `HomeplusHandler.printLabel()` | 7 |
| 홈플러스 전송 | 2938, 2969 | 전송 분기 (2·5 공통) | `HomeplusHandler.getSendUrl()` | 7 |
| 홈플러스 중복확인 제외 | 1205, 1329, 3064 | `NONFIXED \|\| HP_NONFIXED` | `HomeplusHandler.allowDuplicateBarcode()` | 7 |
| 홈플러스 정량 전용 | 1918, 3059 | `HOMEPLUS`만 해당 | `HomeplusHandler` (`nonfixed=false`) | 7 |
| 도매 전송 | 2967, 3015, 3067 | `WHOLESALE` 분기 | `WholesaleHandler` | 8 |
| 이마트 바코드 처리 | 1150~1576 | `setBarcodeMsg` 본문 434줄 | `EmartHandler.onBarcodeScanned()` | 9 |
| 이마트 센터 판정 | 1285~1293 | 킬코이·미트센터·TRD·WET·E/T | `EmartHandler` *private* | 9 |
| 이마트 유통기한 검증 | 1294~1304 | `SHELF_LIFE`/`MAKINGDATE` 확인 | `EmartHandler` (`nonfixed=false`) | 9 |
| 이마트 중량 처리 | 1443, 1505 | `if (EMART)` 분기 | `EmartHandler` | 9 |
| 이마트 라벨 | 2009~2014 | `setPrinting` (0·4 공통) | `EmartHandler.printLabel()` | 9 |
| 비정량 무조건 매칭 | 2150~2162 | `if (NONFIXED)` 블록 | `EmartHandler` (`nonfixed=true`) | 9 |
| 상품 매칭 공통 | 2059~2093, 2106~2323 | `find_PackerProduct*`, `find_work_info*` | 마트별 `findPackerProduct()` + `BarcodeUtil` | 5~9 |
| 계근 현황 계산 | 2324~2363 | `calc_info` | 계산→`WeighingContext`, 표시→`ShipmentScreen` | 3 |
| 계근 저장 | 1884~2033 | `wet_data_insert` | 마트별 `onWeightConfirmed()` | 5~9 |

**Activity 잔류 (이관하지 않음)**

| 항목 | 라인 | 사유 |
|---|---|---|
| 생명주기 | 411~605 | 안드로이드 프레임워크 콜백 |
| 레이아웃 분기 | 422~425 | `ShipmentScreen` 생성보다 앞서야 함 |
| `setBarcodeMsg(String)` 시그니처 | 1143 | `HoneywellScannerActivity` 콜백. 본문만 위임으로 교체 |
| Handler 2종 | 844~1094 | 프린터 콜백. 별건 |
| AsyncTask 5종 | 2500~3357 | 클래스 분리는 이 문서 범위 밖. 필드 접근만 `ctx` 경유로 교체 |
| `sendData` | 2467~2499 | 프린터 전송. 별건 |
| 다이얼로그 | 3358~3738 | `show_wetDetailDialog` 등. 별건 |
| `slcs*` 5개 | 2364~2404, 2447~2466 | `show_wetDetailDialog`가 사용 중. 중복 제거는 별건 (5.3) |

---

#### 파일별 상세 역할

---

**① ShipmentScreen.java** — 화면 조작 전담

| 항목 | 내용 |
|---|---|
| **역할** | 위젯을 소유하고, 화면에 무엇을 보여줄지를 "의도 단위 메서드"로 제공한다 |
| **담당** | 위젯 필드 16개, `findViewById` 16회, 위젯 조작 123회 |
| **이관 출처** | `BixolonShipmentActivity.java:280~332`(필드), `411~526`(onCreate), 조작은 파일 전역 |
| **하지 않는 것** | 계근 상태 보관, 업무 판단, DB 접근, 서버 통신. **화면에 그리고 읽는 것만** |

메서드는 위젯 이름이 아니라 **의도**로 짓는다. `setBlSelection(3)`이 아니라 `moveToWorkItem(pos)` — 호출자가 "BL 스피너가 있고 3번 위치"를 몰라도 되게 한다.

흩어진 짝 호출을 묶는 것이 핵심 목적이다.

| 묶을 대상 | 현재 호출 수 | 묶은 메서드 |
|---|---:|---|
| `edit_product_name.setText` + `edit_product_code.setText` | 10 + 11 | `showProduct(bi)` / `clearProduct()` |
| `btn_send.setEnabled` + `btn_send.setBackgroundResource` | 4 + 4 | `setSendEnabled(boolean)` |
| `sList.setSelection` + `sp_point_name.setSelection` | 6 + 6 | `moveToWorkItem(int)` |
| `edit_wet_count/weight`, `edit_center_tcount/tweight` | 22 | `showWetProgress(...)`, `showCenterTotal(...)` |
| `edit_barcode` 읽기/쓰기 | 14 | `showBarcode()`, `readBarcode()`, `clearBarcode()` |

`edit_product_name`(10회) ≠ `edit_product_code`(11회)로 **개수가 맞지 않는다.** 한쪽만 호출하는 지점이 있다는 뜻이므로, 무조건 묶지 말고 그 지점을 찾아 개별 메서드로 보존한다.

레이아웃 2종(`activity_shipment.xml`, `activity_shipment_wholesale.xml`)의 위젯 id가 **완전히 동일**함을 확인했으므로 클래스 1개로 충분하며 `null` 가드가 필요 없다.

---

**② WeighingContext.java** — 계근 상태 보관

| 항목 | 내용 |
|---|---|
| **역할** | 계근 작업 진행 중 유지되는 값을 한 객체에 모은다. Activity 필드로 흩어진 암묵적 상태 기계를 명시화한다 |
| **담당** | 상태 필드 19종 (6장 매핑표) + 상태 기반 단순 조회 메서드 |
| **이관 출처** | `BixolonShipmentActivity.java:333~380`(필드 선언), `2094~2104`(`find_BL`) |
| **하지 않는 것** | 화면 조작, DB 접근, 서버 통신, 마트별 판단. **값을 들고 있는 것과 그 값으로 답할 수 있는 질문만** |

주요 보관 값 (참조 횟수는 `setBarcodeMsg` 내 기준)

```
workItemBiInfo(50)  currentWorkPosition(35)  arSM(32)  workItemFullbarcode(23)
workPpcode(9)  weightFrom/To(18)  tempWeightDouble(8)  scanFlag(5)  workFlag(2)
centerTotalCount(4)  centerWorkCount(2)  dialogFlag(4)  tempBlNo(3) 등
```

편의 메서드는 **상태만으로 답할 수 있는 것**에 한정한다.

```java
public Shipments_Info currentItem()           { return arSM.get(currentWorkPosition); }
public boolean isCurrentBl(String barcode)    { return barcode.equals(currentItem().getBL_NO()); }
```

AsyncTask 5종(`ProgressDlgShipSelect` 등)도 이 필드들을 직접 참조하므로 Step 3에서 함께 교체한다. **Activity에 필드를 남긴 채 Context에도 추가하면 이중 상태가 되어 조용히 깨진다.**

---

**③ ShipmentTypeHandler.java** — 마트 공통 규약

| 항목 | 내용 |
|---|---|
| **역할** | 마트 클래스 5종이 반드시 제공해야 할 메서드를 선언한다. **구현은 없다** |
| **담당** | 메서드 선언 7개 |
| **이관 출처** | 없음 (신규 설계) |
| **하지 않는 것** | 흐름 강제. 추상 클래스가 아니므로 골격을 공유하지 않는다 |

```java
void    onBarcodeScanned(String msg);                                  // 바코드 스캔 진입점
void    onWeightConfirmed(double weight, String makingDate, String boxSerial);
void    printLabel(double weight, boolean reprint);
void    setupScreen();                                                 // 마트별 화면 초기화
String  getSearchUrl();                                                // 조회 JSP
String  getSendUrl();                                                  // 전송 JSP
boolean allowDuplicateBarcode();                                       // 비정량 계열 true
```

**존재 이유는 컴파일 시점 누락 검증이다.** 마트를 추가하면서 라벨 분기를 빠뜨리면, 인터페이스가 없으면 컴파일이 통과하고 현장에서 발견된다. 인터페이스가 있으면 "구현하지 않았습니다" 에러로 빌드가 막힌다. 이 프로젝트의 사고 유형(오류 32·33·34·36)이 전부 "한 마트 누락"이므로 직결되는 장치다.

마트 고유 로직(롯데 박스순번 채번 등)은 **인터페이스에 넣지 않고** 각 클래스의 `private` 메서드로 숨긴다.

---

**④ ShipmentTypeFactory.java** — searchType 해석

| 항목 | 내용 |
|---|---|
| **역할** | `Common.searchType` 문자열을 읽어 담당 Handler를 만들어 준다 |
| **담당** | searchType 8종 → Handler 5종 매핑 (계근방식은 생성자 플래그) |
| **이관 출처** | 현재 파일 전역에 흩어진 `searchType` 분기 51곳 |
| **하지 않는 것** | 업무 로직. **매핑과 생성만** |

**이 프로젝트에서 `Common.searchType`을 읽는 유일한 지점**이 된다 (레이아웃 분기 1곳 제외). 현재 51곳 → 1곳.

`onCreate`에서 1회 호출하여 `handler` 필드에 담고, 이후 모든 호출은 `handler.xxx()` 형태가 되므로 `switch`가 사라진다. 알 수 없는 searchType이 들어오면 `IllegalArgumentException`으로 즉시 실패시켜, 조용히 아무것도 안 하는 상황을 막는다.

---

**⑤ BarcodeUtil.java** — 바코드 순수 계산

| 항목 | 내용 |
|---|---|
| **역할** | 바코드 문자열을 다루는 계산을 담당한다. **입력만 받고 결과만 반환** |
| **담당** | 바코드 구간 추출, 디바운싱 판정 |
| **이관 출처** | `1154~1164`(디바운싱), `2122~2126`(구간 추출, 3중복) |
| **하지 않는 것** | 상태 변경, 화면 조작, DB 접근. 전부 `static` |

```java
static String  extractBarcodeGoods(String req, boolean type, String from, String to)
static boolean isDebounced(String msg, String lastBarcode, long lastTime, long now, long thresholdMs)
```

**순수 함수만 허용하는 이유**: 지금 `find_work_info`처럼 필드를 몰래 고치는 코드를 공통으로 빼면, 마트마다 호출 시점이 달라지면서 상태가 꼬인다. 입력→출력만 하는 함수는 어느 마트가 언제 불러도 결과가 같다.

---

**⑥ WeightUtil.java** — 중량 순수 계산

| 항목 | 내용 |
|---|---|
| **역할** | 중량 환산·검증 계산을 담당한다 |
| **담당** | LB 단위 환산, 중량 범위 유효성 판정 |
| **이관 출처** | `1390 / 1437 / 1499`(LB 환산, 3중복), `1371 / 1424 / 1486`(범위 검증, 3중복) |
| **하지 않는 것** | ⑤와 동일. 전부 `static` |

```java
static double  convertLb(String baseUnit, double weight)
static boolean isRangeUnset(String from, String to)
```

`setBarcodeMsg` 안에서 `ITEM_TYPE` 블록(W/HW·S·J 3단 else-if + 별도 `if (B)`)이 같은 검사를 3번씩 반복하고 있다. 이를 한 곳으로 모은다.

---

**⑦ ProductionHandler.java** — 생산 (searchType 1, 7)

| 항목 | 내용 |
|---|---|
| **역할** | 생산 계근(1)과 생산 라벨(7)의 전체 흐름을 소유한다 |
| **담당** | 바코드 스캔 → 매칭 → 중량 확정 → 저장 → 전송 |
| **이관 출처** | `setBarcodeMsgProduction`(1577~1883), `onStart`(518), `onStart`(558·564 프린터 비활성), `mHandler`(947), `wet_data_insert`(2018), `ProgressDlgShipmentSend`(2971·2973·3015·3061) |
| **구분 플래그** | `labelOnly` — 1은 `false`, 7은 `true`(미사용) |

생산(1)은 **블루투스 프린터를 사용하지 않는다**(`558`, `564`). 생산라벨(7)만 `setPrinting_prod`를 호출한다.

이미 `setBarcodeMsgProduction`으로 분리돼 있어 **첫 이관 대상**으로 삼는다. 골격 검증용이며, 이관 후 원본 메서드는 Step 10에서 삭제되어 중복 201줄이 소멸한다.

---

**⑧ LotteHandler.java** — 롯데 (searchType 6)

| 항목 | 내용 |
|---|---|
| **역할** | 롯데 출하 계근의 전체 흐름을 소유한다 |
| **담당** | 위 + **박스순번 채번 파이프라인** |
| **이관 출처** | `setBarcodeMsg` 롯데 분기, `2015~2017`(라벨), AsyncTask 전송부 |
| **고유 로직** | `nextBoxOrder()` — `private`. 다른 마트에서 호출 불가 |

라벨은 `setPrintingLotte`를 쓴다. 전송용 `insert_goods_wet_lotte.jsp`는 개발63에서 신규 작성 예정이며 **아직 없다**(현재 롯데는 홈플러스 JSP로 전송되어 박스순번이 적재되지 않음). 개발63과 구간이 겹치지 않으므로 **순서 제약은 없다.**

---

**⑨ HomeplusHandler.java** — 홈플러스 (searchType 2, 5)

| 항목 | 내용 |
|---|---|
| **역할** | 홈플러스 정량(2)·비정량(5) 흐름을 하나의 클래스로 소유한다 |
| **담당** | 위 + `STORE_IN_DATE` 날짜 매핑 |
| **이관 출처** | `939·2006~2008`(라벨), `2938·2969`(전송), `1205·1329·3064`(중복확인), `1918·3059`(정량 전용) |
| **구분 플래그** | `nonfixed` — 2는 `false`, 5는 `true` |

**두 축이 교차하는 대표 사례다.** 라벨(`setHomeplusPrinting`)과 전송은 2·5가 **공유**하고, 중복확인 제외만 5에서 다르다. searchType별로 클래스를 쪼개면 라벨·전송이 중복되므로 하나로 유지한다.

오류 32·33·34가 이 계열이며, 운영 DB에 데이터가 0건일 수 있어 **테스트 데이터 확보 후 착수**한다.

---

**⑩ WholesaleHandler.java** — 도매 (searchType 3)

| 항목 | 내용 |
|---|---|
| **역할** | 도매 출하 계근의 전체 흐름을 소유한다 |
| **담당** | 바코드 스캔 → 매칭 → 중량 확정 → 저장 → 전송 |
| **이관 출처** | `2967·3015·3067`(전송 분기) |
| **특이사항** | 레이아웃이 `activity_shipment_wholesale.xml` |

레이아웃 분기(`422`)는 `ShipmentScreen` 생성보다 앞서야 하므로 **Activity `onCreate`에 남긴다.** 단, 위젯 id가 기본 레이아웃과 동일하여 `ShipmentScreen` 자체는 공용이다.

비정량(4)은 이마트 소속이므로 이 클래스에 포함되지 않는다.

---

**⑪ EmartHandler.java** — 이마트 (searchType 0, 4)

| 항목 | 내용 |
|---|---|
| **역할** | 이마트 정량(0)·비정량(4) 흐름을 하나의 클래스로 소유한다. **기준 구현** |
| **담당** | 위 + 유통기한 검증, 센터별 특수 판정 |
| **이관 출처** | `1150~1576`(setBarcodeMsg 잔여), `2009~2014`(라벨), `2150~2162`(비정량 매칭) |
| **구분 플래그** | `nonfixed` — 0은 `false`, 4는 `true` |

이마트 고유 판정 (`1285~1293`)

```
킬코이 PACKER_CODE + 미트센터 STORE_CODE  → 유통기한 검증
용인TRD / 대구TRD / 시화(W)_TRD / 여주TRD / WET / E/T  → 센터별 분기
```

하드코딩된 센터명 6종은 **문자열 그대로 유지**한다. 상수화·외부화는 이 문서 범위 밖이다.

**가장 크고 위험하므로 마지막에 이관한다.** 다른 마트 4종으로 골격을 검증한 뒤 옮긴다. 비정량(4)의 매칭 순서 주의사항은 4.6 참조.

---

### 3.2 수정 파일 — 1개

| # | 파일 | 위치 | 수정 내용 | 관련 Step |
|:-:|------|------|----------|:-:|
| 1 | **BixolonShipmentActivity.java** | `app/src/main/java/.../highland_emart/` | 죽은 코드·거짓 주석 제거 → 위젯/상태 이관 → 마트 로직 이관 → Activity 축소. **3,738줄 → 250줄 내외** | Step 1~10 |

**Step별 수정 범위**

| Step | 수정 내용 |
|:-:|---|
| 1 | 미사용 메서드 3개·import 7개 제거, 거짓 주석 3곳 정정(2350·2359·164), Javadoc 정정 |
| 2 | 잔여 위젯 10종(호출 62회) 제거 → `screen` 위임. 6종은 개발65에서 선행 완료 |
| 3 | 상태 필드 19종 제거 → `ctx` 위임, `find_BL` 이동 |
| 4 | `handler` 필드 추가, `setBarcodeMsg` 위임으로 교체 |
| 5~9 | 마트별 로직 제거 (이관 대상 마트 코드만) |
| 10 | `setBarcodeMsgProduction`(307줄) 삭제, 위임 껍데기 제거 |

### 3.3 미변경 파일

`LabelPrintHelper.java`, `DBHandler.java`, `Shipments_Info.java`, `Barcodes_Info.java`, `Goodswets_Info.java`, `HoneywellScannerActivity.java`, `BluetoothPrintService.java`, `MainActivity.java`, `AndroidManifest.xml`, JSP 전체, 레이아웃 XML 전체

**`ShipmentActivity.java`(미사용, 4,460줄)도 미변경.** `BluetoothPrintService`가 상수 4종을 참조하여 삭제 불가 (5.1 참조)

---

## 4. 수정 상세

### 4.1 ShipmentTypeHandler.java (신규)

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/shipment/ShipmentTypeHandler.java`

```java
public interface ShipmentTypeHandler {
    void onBarcodeScanned(String msg);
    void onWeightConfirmed(double weight, String makingDate, String boxSerial);
    void printLabel(double weight, boolean reprint);
    void setupScreen();
    String getSearchUrl();
    String getSendUrl();
    boolean allowDuplicateBarcode();
}
```

**검증**: 마트 클래스 8개가 전부 구현해야 컴파일된다. 마트 추가 시 메서드 누락은 컴파일 에러로 잡힌다

### 4.2 ShipmentTypeFactory.java (신규)

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/shipment/ShipmentTypeFactory.java`

```java
public class ShipmentTypeFactory {
    public static ShipmentTypeHandler create(String searchType,
                                             ShipmentScreen screen,
                                             WeighingContext ctx,
                                             Context appContext) {
        switch (searchType) {
            // 이마트 — 0:정량, 4:비정량
            case "0": return new EmartHandler(screen, ctx, appContext, false);
            case "4": return new EmartHandler(screen, ctx, appContext, true);
            // 생산 — 1:계근, 7:라벨(미사용)
            case "1": return new ProductionHandler(screen, ctx, appContext, false);
            case "7": return new ProductionHandler(screen, ctx, appContext, true);
            // 홈플러스 — 2:정량, 5:비정량
            case "2": return new HomeplusHandler(screen, ctx, appContext, false);
            case "5": return new HomeplusHandler(screen, ctx, appContext, true);
            // 도매
            case "3": return new WholesaleHandler(screen, ctx, appContext);
            // 롯데
            case "6": return new LotteHandler(screen, ctx, appContext);
            default:  throw new IllegalArgumentException("searchType: " + searchType);
        }
    }
}
```

**검증**: `Common.searchType`을 읽는 코드가 이 파일 하나로 줄어든다 (현재 51곳)

### 4.3 ShipmentScreen.java (신규)

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/shipment/ShipmentScreen.java`

**변경 전** — Activity에 산재

```java
// BixolonShipmentActivity 필드
private Spinner sp_work, sp_center_name, sp_bl_no, sp_point_name;
private EditText edit_barcode, edit_product_name, edit_product_code,
                 edit_wet_count, edit_wet_weight, edit_center_tcount, edit_center_tweight;
private ListView sList;
private Button btn_input, btn_back, btn_send, btn_select;

// 조작 123회가 setBarcodeMsg / Handler / AsyncTask / 다이얼로그에 흩어짐
edit_product_name.setText(bi.getITEM_NAME_KR());
edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
...
btn_send.setEnabled(true);
btn_send.setBackgroundResource(R.drawable.btn_on);
```

**변경 후**

```java
public class ShipmentScreen {
    private final Spinner sp_work, sp_center_name, sp_bl_no, sp_point_name;
    private final EditText edit_barcode, edit_product_name, edit_product_code,
                           edit_wet_count, edit_wet_weight,
                           edit_center_tcount, edit_center_tweight;
    private final ListView sList;
    private final Button btn_input, btn_back, btn_send, btn_select;

    public ShipmentScreen(Activity a) {
        sp_center_name      = a.findViewById(R.id.sp_center);
        sp_bl_no            = a.findViewById(R.id.sp_bl_no);
        sp_point_name       = a.findViewById(R.id.sp_point);
        sp_work             = a.findViewById(R.id.sp_work);
        edit_barcode        = a.findViewById(R.id.edit_barcode);
        edit_product_name   = a.findViewById(R.id.edit_product_name);
        edit_product_code   = a.findViewById(R.id.edit_product_code);
        edit_wet_count      = a.findViewById(R.id.edit_wet_count);
        edit_wet_weight     = a.findViewById(R.id.edit_wet_weight);
        edit_center_tcount  = a.findViewById(R.id.edit_center_tcount);
        edit_center_tweight = a.findViewById(R.id.edit_center_tweight);
        sList               = a.findViewById(R.id.sList);
        btn_input           = a.findViewById(R.id.btn_input);
        btn_back            = a.findViewById(R.id.btn_back);
        btn_send            = a.findViewById(R.id.btn_send);
        btn_select          = a.findViewById(R.id.btn_select);
    }

    // 상품 정보 (현재 21곳)
    public void showProduct(Barcodes_Info bi) {
        edit_product_name.setText(bi.getITEM_NAME_KR());
        edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
    }
    public void clearProduct() {
        edit_product_name.setText("");
        edit_product_code.setText("");
    }

    // 계근 현황 (현재 22곳)
    public void showWetProgress(String reqCnt, String wetCnt, String reqW, String wetW) { ... }
    public void showCenterTotal(String cnt, String weight) { ... }

    // 작업 대상 이동 (현재 12곳)
    public void moveToWorkItem(int position) {
        sList.setSelection(position);
        sp_point_name.setSelection(position);
    }

    // 바코드 입력 (현재 14곳)
    public void showBarcode(String s) { edit_barcode.setText(s); }
    public String readBarcode()       { return edit_barcode.getText().toString(); }

    // 전송 버튼 (현재 8곳)
    public void setSendEnabled(boolean on) {
        btn_send.setEnabled(on);
        btn_send.setBackgroundResource(on ? R.drawable.btn_on : R.drawable.btn_off);
    }

    // 원본 동작 보존: Editable vs String 비교라 항상 true
    // (PDA-INNO(원본) ShipmentActivity.java:1285 와 동일. 수정 금지)
    public boolean isProductNameSet() {
        return !edit_product_name.getText().equals("");
    }
}
```

**검증**:
- `activity_shipment.xml` / `activity_shipment_wholesale.xml` 위젯 id **완전 동일** 확인 완료 → `ShipmentScreen` 1개로 충분, `null` 가드 불필요
- `btn_send` 짝 호출, `edit_product_*` 짝 호출, `sList`/`sp_point_name` 짝 호출이 각각 하나로 묶인다

### 4.4 WeighingContext.java (신규)

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/shipment/WeighingContext.java`

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
    public boolean isCurrentBl(String barcode) {          // find_BL 이관
        return barcode.equals(currentItem().getBL_NO());
    }
    // getter / setter
}
```

**검증**: Activity에서 해당 필드 19종이 사라져야 한다. 남아 있으면 이관 누락

### 4.5 BarcodeUtil.java / WeightUtil.java (신규)

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/shipment/`

```java
public class BarcodeUtil {
    // find_work_info 2122~2126 에서 추출
    public static String extractBarcodeGoods(String req, boolean type, String from, String to) {
        if (type && req.length() >= Integer.parseInt(to)) {
            return req.substring(Integer.parseInt(from) - 1, Integer.parseInt(to));
        }
        return req;
    }

    // setBarcodeMsg 1154~1164 에서 추출
    public static boolean isDebounced(String msg, String lastBarcode, long lastTime, long now, long thresholdMs) {
        return msg != null && msg.equals(lastBarcode) && (now - lastTime) < thresholdMs;
    }
}

public class WeightUtil {
    // setBarcodeMsg 1390 / 1437 / 1499 (3중복) 에서 추출
    public static double convertLb(String baseUnit, double weight) { ... }

    // setBarcodeMsg 1371 / 1424 / 1486 (3중복) 에서 추출
    public static boolean isRangeUnset(String from, String to) {
        return from.equals("0") || to.equals("0");
    }
}
```

**검증**: 전부 `static` 순수 함수. Activity 필드·위젯을 참조하면 안 된다

### 4.6 마트 클래스 예시 — EmartHandler (0, 4)

**변경 전** — `find_work_info` 하나에 정량·비정량 규칙이 섞임 (2131 / 2150)

```java
if (temp_bg.equals(bg)) {          // 일반 매칭
    work_item_bi_info = bi;  ...
} else {
    edit_product_name.setText("");  ...
}
if (Common.searchType.equals(SEARCH_TYPE_NONFIXED)) {   // 비정량은 조건 없이 또 매칭
    work_item_bi_info = bi;  ...
}
```

**변경 후** — 이마트 클래스 하나가 정량·비정량을 플래그로 구분

```java
public class EmartHandler implements ShipmentTypeHandler {
    private final ShipmentScreen screen;
    private final WeighingContext ctx;
    private final Context appContext;
    private final boolean nonfixed;        // searchType 0 → false, 4 → true

    public EmartHandler(ShipmentScreen screen, WeighingContext ctx,
                        Context appContext, boolean nonfixed) { ... }

    // 중복확인 제외 여부 (원본 1205, 1329)
    @Override public boolean allowDuplicateBarcode() { return nonfixed; }

    // 라벨 — 0·4 모두 setPrinting (원본 2011, 2014)
    @Override public void printLabel(double weight, boolean reprint) {
        labelPrintHelper.setPrinting(weight, ctx.currentItem(), reprint,
                ctx.getMakingDate(), ctx.getWorkItemBiInfo(), ctx.currentItem(),
                Common.searchType, printerCallback);
    }

    // 상품 매칭 — 원본 실행 순서 그대로 재현
    private String findPackerProduct(String barcode) {
        String ppCode = ""; int count = 0;
        for (Barcodes_Info bi : DBHandler.selectqueryBarcodeInfo(appContext)) {
            String bg = BarcodeUtil.extractBarcodeGoods(barcode, true,
                            bi.getBARCODEGOODS_FROM(), bi.getBARCODEGOODS_TO());

            // ① 일반 매칭 블록 (원본 2131~2148) — 0·4 공통으로 먼저 실행
            if (bg.equals(bi.getBARCODEGOODS())) {
                ctx.setWorkItemBiInfo(bi);
                ctx.setWorkItemBarcodegoods(bi.getBARCODEGOODS());
                screen.showProduct(bi);
                ppCode = (count++ == 0) ? bi.getPACKER_PRODUCT_CODE()
                                        : ppCode + "', '" + bi.getPACKER_PRODUCT_CODE();
            } else {
                ctx.setWorkItemBarcodegoods("");
                screen.clearProduct();
            }

            // ② 비정량 무조건 매칭 (원본 2150~2162) — ①에 이어 순차 실행
            if (nonfixed) {
                ctx.setWorkItemBiInfo(bi);
                ctx.setWorkItemBarcodegoods(bi.getBARCODEGOODS());
                screen.showProduct(bi);
                ppCode = (count++ == 0) ? bi.getPACKER_PRODUCT_CODE()
                                        : ppCode + "', '" + bi.getPACKER_PRODUCT_CODE();
            }
        }
        return ppCode;
    }
}
```

**주의**: 원본은 `if (temp_bg.equals(bg))` 블록 실행 후 `if (NONFIXED)` 블록이 **같은 `bi`에 대해 이어서** 실행된다. 둘 다 매칭되면 `count`가 2회 증가하고 `ppCode`에 같은 코드가 두 번 들어간다. 위 코드는 이 순서를 그대로 재현한 것이며, **"중복이니 하나로 합치자"고 정리하면 동작이 바뀐다.** Step 9에서 원본 로그(`return pp_code test!!!`)와 대조 확인할 것.

**검증**: 비정량 바코드 스캔 시 `ppCode` 문자열과 `work_item_bi_info` 최종값이 원본과 동일해야 한다

### 4.7 BixolonShipmentActivity.java (최종)

**변경 후**

```java
public class BixolonShipmentActivity extends HoneywellScannerActivity {
    private ShipmentScreen screen;
    private WeighingContext ctx;
    private ShipmentTypeHandler handler;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Common.searchType.equals("3")) setContentView(R.layout.activity_shipment_wholesale);
        else                               setContentView(R.layout.activity_shipment);

        screen  = new ShipmentScreen(this);
        ctx     = new WeighingContext();
        handler = ShipmentTypeFactory.create(Common.searchType, screen, ctx, getApplicationContext());
        handler.setupScreen();
        LabelPrintHelper.loadCustomFont(this);
    }

    @Override
    public void setBarcodeMsg(String msg) {
        handler.onBarcodeScanned(msg);        // switch 없음
    }
}
```

**검증**: 최종 250줄 내외. `Common.searchType` 참조가 `onCreate`의 레이아웃 분기 1곳 + Factory 전달 1곳으로 줄어든다

---

## 5. 사이드이펙트

### 5.1 ShipmentActivity.java (미사용 — 삭제 대상 아님)

```java
// BluetoothPrintService.java:117,119,160,162,290
Message msg = mHandler.obtainMessage(ShipmentActivity.MESSAGE_DEVICE_NAME);
bundle.putString(ShipmentActivity.DEVICE_NAME, device.getName());
```

- `ShipmentActivity`(4,460줄)는 `MainActivity`에서 진입 경로가 없어 런타임 미사용이나, `BluetoothPrintService`가 상수 4종을 참조하여 삭제 시 컴파일 에러
- `AndroidManifest.xml:26`에 등록도 남아 있음
- **대응**: 이 문서 범위에서 건드리지 않는다. 별건으로 처리

### 5.2 LabelPrintHelper.java

```java
labelPrintHelper.setPrinting(weight, si, reprint, making_date, bi, si2, Common.searchType, printerCallback);
labelPrintHelper.setPrintingLotte(weight, si, reprint, making_date, box_order, Common.searchType, printerCallback);
```

- 마트별 시그니처가 다르나, 인자는 전부 `WeighingContext`에서 공급 가능
- 각 Handler가 내부에서 자기 인자를 채우므로 인터페이스는 `printLabel(double, boolean)` 하나로 통일
- **`LabelPrintHelper` 자체는 수정하지 않는다**

### 5.3 show_wetDetailDialog 의 SLCS 직접 사용 (3453~3517)

```java
slcsCmd.append(slcsInit());
slcsCmd.append(slcsLabelSize(576, 460));
slcsCmd.append(slcsText(p_weight, p_hight, 40, 40, ...));
```

- 합계 라벨 인쇄가 Activity의 `slcs*` 메서드를 직접 호출 중 (`slcsInit`, `slcsLabelSize`, `slcsText`, `slcsPrint`, `slcsFeedToMark` 5개)
- `LabelPrintHelper`(128~228줄)에 동일 메서드가 중복 존재하나 `private`
- **대응**: Step 1에서는 미사용 3개(`slcsBarcode`/`slcsLine`/`slcsBox`)만 제거하고, 사용 중인 5개는 유지한다. 중복 제거는 이 문서 범위 밖

### 5.4 HoneywellScannerActivity 상속

```java
public class BixolonShipmentActivity extends HoneywellScannerActivity
```

- `setBarcodeMsg(String)`는 상위 클래스가 호출하는 콜백. 시그니처 변경 불가
- **대응**: 시그니처 유지하고 본문만 `handler.onBarcodeScanned(msg)` 위임으로 교체

### 5.5 AsyncTask 5종의 Activity 필드 접근

- `ProgressDlgShipSelect`, `ProgressDlgShipSelectBL`, `ProgressDlgShipmentSend`, `ProgressDlgPrintConnect`, `ProgressDlgDiscon`이 `arSM`, `current_work_position` 등을 직접 참조
- `ProgressDlgShipSelect` ↔ `ProgressDlgShipSelectBL`은 유효줄 167/177 중 **112줄 중복**
- **대응**: Step 3에서 `WeighingContext` 참조로 교체. 클래스 분리·중복 제거는 이 문서 범위 밖 (별건)

---

## 6. 데이터 저장 구조

### WeighingContext 필드 매핑 (Activity → Context)

| Activity 필드 | Context 필드 | 타입 | 용도 | setBarcodeMsg 내 참조 |
|---|---|---|---|---:|
| `work_item_bi_info` | `workItemBiInfo` | Barcodes_Info | 현재 작업 바코드 정보 | 50 |
| `current_work_position` | `currentWorkPosition` | int | 작업 대상 리스트 인덱스 | 35 |
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
| `work_flag` | `workFlag` | int | 작업 상태 | 2 |
| `lastProcessedBarcode` | `lastProcessedBarcode` | String | 디바운싱 기준 바코드 | 2 |
| `centerWorkCount` | `centerWorkCount` | int | 센터 계근 완료수량 | 2 |

**Context 이관 대상 19종. 위젯 16종은 `ShipmentScreen`으로, 나머지는 Activity 잔류.**

> 위 표는 **18행 / 필드 19개**다. `weight_from`·`weight_to`가 한 행에 묶여 있다.

> `lotteBoxOrder`(1916)는 `wet_data_insert`의 **지역변수**이므로 이관 대상이 아니다.

### searchType 매핑

```
"0" → EmartHandler(nonfixed=false)        이마트 출하
"4" → EmartHandler(nonfixed=true)         이마트 비정량
"1" → ProductionHandler(labelOnly=false)  생산 계근 (이노이천)
"7" → ProductionHandler(labelOnly=true)   생산 라벨 (미사용)
"2" → HomeplusHandler(nonfixed=false)     홈플러스 출하
"5" → HomeplusHandler(nonfixed=true)      홈플러스 비정량
"3" → WholesaleHandler                    도매 출하
"6" → LotteHandler                        롯데 출하
```

**마트사 5종 → 클래스 5개. searchType 8종은 생성자 플래그로 구분한다.**

---

## 7. 호출 시점

```
[앱 시작]
    ↓
[LoginActivity]  로그인 + 창고 선택
    ↓
[MainActivity]   출하대상받기(searchType 결정) → 계근입력시작
    ↓
[BixolonShipmentActivity.onCreate()]
    ├── setContentView (searchType=3 → wholesale, 그 외 → 기본)
    ├── screen  = new ShipmentScreen(this)
    ├── ctx     = new WeighingContext()
    ├── ★ handler = ShipmentTypeFactory.create(Common.searchType, screen, ctx, ...)
    ├── handler.setupScreen()
    └── LabelPrintHelper.loadCustomFont(this)
            ↓
[바코드 스캔 발생]  HoneywellScannerActivity → setBarcodeMsg(msg)
            ↓
    ★ handler.onBarcodeScanned(msg)
            ├── BarcodeUtil / WeightUtil   순수 계산
            ├── ctx.setXxx()               상태 갱신
            └── screen.showXxx()           화면 갱신
            ↓
[중량 확정]  handler.onWeightConfirmed(...)
            ├── DBHandler.insertqueryGoodsWet()   로컬 저장
            └── handler.printLabel(weight, false) 라벨 출력
            ↓
[전송 버튼]  ProgressDlgShipmentSend → handler.getSendUrl()
```

---

## 8. 개발 플랜

### Step 1: 안전 정리 (죽은 코드 · 거짓 주석 · 미사용 import)

**Part 1. 분석**
- 메서드: `slcsBarcode`, `slcsLine`, `slcsBox` / 클래스 Javadoc / import 블록
- 범위: `BixolonShipmentActivity.java:63~151, 164, 1261, 1684, 2350~2363, 2405~2446`
- 용도: 이후 step에서 코드를 읽을 때 오도되지 않도록 시야 확보
- 주의할 점: **동작에 영향을 주는 코드는 한 줄도 건드리지 않는다.** 사용 중인 `slcs*` 5개는 유지

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 미사용 메서드 3개 | 2405, 2421, 2436 | `slcsBarcode`/`slcsLine`/`slcsBox` — 호출처 0. 제거 |
| 2 | 거짓 주석 | 2350, 2359 | "LabelPrintHelper로 이동됨" → 실제 잔류. 문구 정정 |
| 3 | `if (true)` | 1261, 1684 | 무의미 분기. **블록은 유지하고 조건만 제거** |
| 4 | 미사용 import 7개 | import 블록 | IOException, DecimalFormat, ParseException, SimpleDateFormat, Calendar, Date, Set |
| 5 | 클래스 Javadoc | 63~151 | 클래스명(ShipmentActivity→Bixolon…), 프린터(Woosim→Bixolon), searchType 표 정정 |
| 6 | 상수 주석 오류 | 164 | `SEARCH_TYPE_NONFIXED = "4"` 주석 "도매 비정량" → **"이마트 비정량"**. 라벨(2014 `setPrinting`)·로그(2013 "이마트(비정량)")가 이마트 |

**Part 2. 변환 계획**
- 변환 방식: 순수 삭제 + 주석 문구 수정. 로직 이동 없음
- 주의사항: `if (true) { ... }`에서 조건 제거 시 **중괄호 블록과 들여쓰기를 유지**하여 diff를 최소화한다. 3번은 위험하면 생략 가능

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

### Step 2: ShipmentScreen 도입

**Part 1. 분석**
- 메서드: `onCreate` findViewById 블록 + 위젯 조작 123회
- 범위: `BixolonShipmentActivity.java:280~332(필드), 411~526(onCreate)` 및 파일 전역
- 용도: 위젯 16개와 화면 조작을 Activity 밖으로 분리
- 주의할 점: `isProductNameSet()`은 원본의 `Editable` vs `String` 비교를 **표현식 그대로** 옮긴다. `toString()` 추가 금지

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 위젯 필드 16개 | 280~332 | `ShipmentScreen`으로 이동 |
| 2 | findViewById 16회 | onCreate | 생성자로 이동 |
| 3 | 짝 호출 3쌍 | 전역 | `btn_send`(8), `edit_product_*`(21), `sList`+`sp_point_name`(12) → 메서드로 묶음 |
| 4 | 레이아웃 2종 | res/layout | 위젯 id 완전 동일 확인 완료 → 클래스 1개로 충분 |

**Part 2. 변환 계획**
- 변환 방식: Activity는 `screen` 필드를 통해 호출. 이 step에서는 **마트 분리 없이 치환만** 수행
- 주의사항: 순수 치환이어야 한다. diff에 조건문이 새로 생기면 잘못된 것
- `edit_product_name.setText` 10회 vs `edit_product_code.setText` 11회로 개수가 다르다. **한쪽만 호출하는 지점을 찾아 그대로 보존**할 것 (`showProduct()`로 묶으면 동작이 바뀜)

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
- 메서드: 파일 전역 (필드 접근 전부)
- 범위: `BixolonShipmentActivity.java:333~380(필드)` 및 참조 지점 전역
- 용도: 계근 상태 19종을 Activity 밖으로 분리
- 주의할 점: AsyncTask 5종도 이 필드들을 직접 참조한다. 함께 교체

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 상태 필드 19종 | 333~380 | 6장 매핑표대로 이동 |
| 2 | `find_BL` | 2094~2104 | `ctx.isCurrentBl(barcode)`로 이동 |
| 3 | AsyncTask 참조 | 2500~3357 | `arSM`, `current_work_position` 등을 `ctx` 경유로 교체 |

**Part 2. 변환 계획**
- 변환 방식: 필드 직접 접근 → getter/setter 치환. 초기값·초기화 시점을 원본과 동일하게 유지
- 주의사항: `setBarcodeMsg` 하나에서만 190여 곳이 바뀐다. 치환 누락 시 컴파일 에러로 잡히므로 필드는 **반드시 Activity에서 제거**할 것 (남겨두면 이중 상태가 되어 조용히 깨짐)

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

### Step 4: 인터페이스 + Factory 골격 (동작 변화 0)

**Part 1. 분석**
- 메서드: `ShipmentTypeHandler`, `ShipmentTypeFactory`, 마트 클래스 8개 (빈 껍데기)
- 범위: 신규 파일 10개
- 용도: 이후 마트 이관의 착지점 마련
- 주의할 점: 이 step에서는 **각 Handler가 Activity의 기존 메서드를 그대로 호출**한다. 로직 이동 없음

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 인터페이스 | 신규 | 4.1 참조. 메서드 7개 |
| 2 | Factory | 신규 | 4.2 참조 |
| 3 | 마트 클래스 8개 | 신규 | 전부 Activity 메서드로 위임하는 껍데기 |
| 4 | Activity 연결 | onCreate, setBarcodeMsg | `handler` 필드 추가 + 위임 |

**Part 2. 변환 계획**
- 변환 방식: 껍데기 Handler가 `activity.setBarcodeMsg_legacy(msg)` 형태로 기존 로직 호출
- 주의사항: **이 step 완료 후 앱 동작이 완전히 동일해야 한다.** 실기기로 이마트 1건 계근하여 확인

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

### Step 5: 생산(1, 7) 이관 — ProductionHandler

**Part 1. 분석**
- 메서드: `setBarcodeMsgProduction`(1577~1883, 307줄) 및 생산 관련 분기
- 범위: `BixolonShipmentActivity.java:518, 558, 564, 947, 1145, 1577~1883, 2018, 2971, 2973, 3015, 3061`
- 용도: 첫 이관 대상. 이미 분리돼 있어 위험 최소이며 골격 검증용
- 주의할 점: 생산은 개발60에서 분리된 이력이 있다. 개발62 롤백은 `cbbfd22`로 커밋 완료되어 선행조건 충족

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | `setBarcodeMsgProduction` | 1577~1883 | `ProductionHandler.onBarcodeScanned`로 이동 |
| 2 | 생산 분기 제거 | 1145 | `setBarcodeMsg` 진입부 분기 삭제 |
| 3 | 프린터 비활성 | 518, 558, 564 | 생산(1)은 블루투스 미사용 → `setupScreen()` |
| 4 | 라벨 | 2018~2020, `setPrinting_prod` | `labelOnly=true`(7)일 때만 호출 |
| 5 | `PRODUCTION \|\| PRODUCTION_LABEL` | 3061 | 마트사 축 묶음 → 클래스 내부로 흡수 |

**Part 2. 변환 계획**
- 변환 방식: 메서드 본문을 그대로 옮기고 필드 접근만 `ctx`/`screen` 경유로 교체. 1과 7의 차이는 `labelOnly` 플래그로 구분
- 주의사항: **이관 후 `git diff`에 생산 외 마트 코드가 나오면 안 된다**. searchType 7은 미사용(2026-08-04 제외 결정)이므로 기존 분기를 그대로 옮기기만 한다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (생산 계근 실기기)
- [ ] Part 6: 회귀테스트 (이마트·롯데·홈플러스 정상 동작 확인)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 6: 롯데(6) 이관 — LotteHandler

**Part 1. 분석**
- 메서드: 롯데 관련 분기 전체
- 범위: `setBarcodeMsg`, `wet_data_insert`(2015~2017), AsyncTask 전송부
- 용도: 박스순번 파이프라인이 뚜렷한 차이라 분리 검증에 적합
- 주의할 점: **개발63과 순서 제약 없음** (구간 미중첩). 사용자 결정에 따라 클래스 분리 완료 후 개발63 진행

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 박스순번 채번 | 1916~1924 (`wet_data_insert` 지역변수) | `LotteHandler` private 메서드로. 필드가 아니므로 `WeighingContext` 대상 아님 |
| 2 | 라벨 | 2015~2017, `setPrintingLotte` | `LotteHandler.printLabel` |
| 3 | 전송 URL | AsyncTask | `getSendUrl()` — `insert_goods_wet_lotte.jsp` |

**Part 2. 변환 계획**
- 변환 방식: Step 5와 동일
- 주의사항: 롯데는 `search_shipment_lotte.jsp` 미존재컬럼 오류(36) 이력이 있다. JSP 계약 확인 후 진행

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (롯데 계근 실기기, 박스순번 채번 확인)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 7: 홈플러스(2, 5) 이관 — HomeplusHandler

**Part 1. 분석**
- 메서드: 홈플러스 관련 분기
- 범위: `BixolonShipmentActivity.java:939, 1205, 1329, 1918, 2006~2008, 2938, 2969, 3059, 3064`
- 용도: 마트사 축(라벨·전송)과 계근방식 축(중복확인)이 교차하는 대표 사례. 플래그 방식 검증
- 주의할 점: 오류 32·33·34가 홈플러스 계열. 개발58(이마트 패턴 포팅) 반영 상태 확인 필요

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 라벨 (2·5 공통) | 939, 2006~2008 | `setHomeplusPrinting` — 플래그 무관, 클래스 공유 |
| 2 | 전송 (2·5 공통) | 2938, 2969 | `getSendUrl()` |
| 3 | 중복확인 제외 | 1205, 1329, 3064 | `nonfixed=true`(5)일 때만 |
| 4 | 정량 전용 분기 | 1918, 3059 | `nonfixed=false`(2)일 때만 |
| 5 | `STORE_IN_DATE` | 오류34 | 라벨 날짜 매핑 확인 |

**Part 2. 변환 계획**
- 변환 방식: `HomeplusHandler(nonfixed)` 하나로 2·5 모두 처리. 라벨·전송은 공유, 중복확인만 플래그 분기
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

### Step 8: 도매(3) 이관 — WholesaleHandler

**Part 1. 분석**
- 메서드: 도매 관련 분기
- 범위: `BixolonShipmentActivity.java:422, 2967, 3015, 3067`
- 용도: 레이아웃이 다른 유일한 마트
- 주의할 점: `activity_shipment_wholesale.xml`은 위젯 id가 기본 레이아웃과 **완전히 동일**함을 확인했다. `setContentView` 분기만 유지

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 레이아웃 분기 | 422 | Activity `onCreate`에 잔류 (Handler 생성 전이므로) |
| 2 | 전송 분기 | 2967, 3015, 3067 | `WholesaleHandler` |

**Part 2. 변환 계획**
- 변환 방식: Step 5와 동일
- 주의사항: 레이아웃 분기는 `ShipmentScreen` 생성보다 앞서야 하므로 Activity에 남긴다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (도매 계근, wholesale 레이아웃 확인)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 9: 이마트(0, 4) 이관 — EmartHandler

**Part 1. 분석**
- 메서드: `setBarcodeMsg` 잔여 전체, `find_work_info`
- 범위: `BixolonShipmentActivity.java:1150~1576, 2009~2014, 2150~2162, 2967`
- 용도: 기준 구현. 다른 마트로 골격을 검증한 뒤 마지막에 이관
- 주의할 점: **4.6의 실행 순서 주의사항 반드시 확인.** 원본은 일반 매칭 블록(2131) 실행 후 NONFIXED 블록(2150)이 같은 `bi`에 대해 이어 실행되어 `count`가 2회 증가할 수 있다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 유통기한 검증 | 1294 | 정량(0)만. `nonfixed=false` 조건 |
| 2 | 중량 처리 | 1443, 1505 | 이마트 전용 분기 |
| 3 | 센터 판정 | 1285~1293 | 킬코이 PACKER_CODE, 미트센터 STORE_CODE, 용인/대구/시화/여주TRD, WET, E/T |
| 4 | 무조건 매칭 | 2150~2162 | `nonfixed=true`(4)일 때. **원본 실행 순서 그대로 재현** |
| 5 | 중복확인 제외 | 1205, 1329 | `nonfixed=true`(4)일 때 |
| 6 | 라벨 (0·4 공통) | 2009~2014 | 둘 다 `setPrinting`. 로그 문구만 다름 |

**Part 2. 변환 계획**
- 변환 방식: `EmartHandler(nonfixed)` 하나로 0·4 모두 처리. 4.6 예시 코드 참조
- 주의사항: 하드코딩된 센터명 6종을 **문자열 그대로 유지**한다. 상수화·외부화는 이 문서 범위 밖. `ppCode` 문자열(`"코드1', '코드2"` 형태)과 `work_item_bi_info` 최종값을 원본 logcat과 대조

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

### Step 10: Activity 정리

**Part 1. 분석**
- 메서드: `BixolonShipmentActivity` 잔여 전체
- 범위: `BixolonShipmentActivity.java` 전역
- 용도: 마트 5종 이관 완료 후 Activity를 250줄 수준으로 축소
- 주의할 점: `setBarcodeMsg(String)` 시그니처는 `HoneywellScannerActivity` 콜백이므로 유지

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | `setBarcodeMsgProduction` 제거 | 1577~1883 | 이관 완료 후 삭제 (중복 201줄 소멸) |
| 2 | 위임 껍데기 제거 | 전역 | Step 4에서 만든 `_legacy` 메서드 삭제 |
| 3 | Activity 축소 | 전역 | 생명주기 + findViewById + 위임만 잔류 |
| 4 | searchType 참조 확인 | 전역 | Factory + 레이아웃 분기 2곳만 남는지 확인 |

**Part 2. 변환 계획**
- 변환 방식: 잔여 코드 제거 및 정리
- 주의사항: AsyncTask 5종은 이 문서 범위 밖이므로 Activity에 잔류시킨다. 별건 처리

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

### Step 11: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 이마트(0) 계근 → 라벨 출력 → 전송 정상 | □ |
| 2 | 생산(1) 계근 → 전송 정상 (프린터 비활성) | □ |
| 3 | 홈플러스(2) 계근 → 라벨 날짜 정확 → 전송 정상 | □ |
| 4 | 도매(3) 계근 → wholesale 레이아웃 정상 표시 | □ |
| 5 | 비정량(4) 동일 바코드 연속 스캔 시 중복확인 미발생 | □ |
| 6 | 홈플러스비정량(5) 계근 → 전송 정상 | □ |
| 7 | 롯데(6) 계근 → 박스순번 채번 → 라벨 출력 정상 | □ |
| 8 | 프린터 연결/해제, 재출력 정상 | □ |
| 9 | BL 스캔 → 상품 스캔 순서 정상 | □ |
| 10 | 계근 완료 다이얼로그, 전송완료 다이얼로그 정상 | □ |
| 11 | 삭제(계근 취소) 후 수량·중량 재계산 정확 | □ |
| 12 | `Common.searchType` 참조가 Factory + 레이아웃 분기 2곳뿐인지 확인 | □ |
| 13 | 마트 클래스 5개에 안드로이드 `import`가 없는지 확인 | □ |
| 15 | 홈플러스 2·5가 동일 라벨(`setHomeplusPrinting`)을 쓰는지 확인 — 중복 구현 없음 | □ |
| 16 | 이마트 0·4가 동일 라벨(`setPrinting`)을 쓰는지 확인 — 중복 구현 없음 | □ |
| 14 | `BixolonShipmentActivity` 250줄 내외인지 확인 | □ |

---

### 개발 순서 요약

```
Step 1: 안전 정리 (죽은 코드·거짓 주석·미사용 import)
    ↓
Step 2: ShipmentScreen 도입          ← 동작 변화 0 (순수 치환)
    ↓
Step 3: WeighingContext 도입         ← 동작 변화 0 (순수 치환)
    ↓
Step 4: 인터페이스 + Factory 골격     ← 동작 변화 0 (위임만)
    ↓
Step 5: 생산(1,7)   → ProductionHandler   ← 여기서부터 마트사별 검증
    ↓
Step 6: 롯데(6)     → LotteHandler
    ↓
Step 7: 홈플러스(2,5) → HomeplusHandler
    ↓
Step 8: 도매(3)     → WholesaleHandler
    ↓
Step 9: 이마트(0,4) → EmartHandler
    ↓
Step 10: Activity 정리 (setBarcodeMsgProduction 제거)
    ↓
Step 11: 통합 테스트
```

**Step 2~4는 동작이 한 줄도 바뀌지 않아야 한다. Step 5 이후로는 이관하지 않은 마트가 `git diff`에 나오지 않아야 한다.**

이마트를 마지막에 두는 이유: 기준 구현이자 데이터가 가장 많아, 다른 마트로 골격을 검증한 뒤 옮기는 것이 안전하다.

---

## 9. 테스트 시나리오

### 시나리오 1: Step 2~4 무변화 검증

```
1. Step 진행 전 이마트 계근 1건 수행 → logcat 저장
2. Step 진행
3. 동일 조건으로 이마트 계근 1건 수행 → logcat 저장
4. 두 logcat의 처리 순서·값 대조 → 동일해야 함
5. git diff 확인 → 조건문이 새로 추가되지 않았는지 확인
```

### 시나리오 2: 마트 이관 후 격리 검증 (Step 5~9 공통)

```
1. git diff --stat 실행
2. 이관 대상 마트 Handler + Activity 위임부만 변경되었는지 확인
3. 다른 마트 Handler 파일이 변경 목록에 없는지 확인
4. 있으면 → 공통/차이 분류 오류. 되돌리고 재분류
```

### 시나리오 3: 이마트 비정량 매칭 규칙 검증 (Step 9)

```
1. 이마트 비정량 출하대상 받기 (searchType=4)
2. 바코드정보에 등록된 상품 바코드 스캔
3. 상품명·상품코드 표시값 확인 → 원본과 동일해야 함
4. 동일 바코드 재스캔 → 중복확인 다이얼로그 미발생 확인
5. logcat의 "return pp_code test!!!" 값을 원본과 대조
```

### 시나리오 4: 라벨 출력 검증 (Step 5~9 공통)

```
1. 해당 마트 계근 1건 수행
2. 라벨 출력물 실물 확인 (폰트·레이아웃·날짜·중량·바코드)
3. 재출력 버튼 → 동일 라벨 재출력 확인
4. 원본 앱 출력물과 실물 대조
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | Step 3에서 상태 이중화 | Activity 필드를 남긴 채 Context에도 추가 | 필드를 **반드시 Activity에서 제거**. 남기면 컴파일은 되나 조용히 깨짐 |
| 2 | `isProductNameSet()` 동작 변경 | `getText().toString()` 으로 "고침" | 원본은 `Editable` vs `String` 비교라 항상 true. 표현식 그대로 유지. 주석 명시 |
| 3 | `showProduct()` 묶음으로 동작 변경 | `edit_product_name`(10회) ≠ `edit_product_code`(11회) | 한쪽만 호출하는 지점을 찾아 개별 메서드로 보존 |
| 4 | 이마트 비정량 `ppCode` 값 불일치 | 원본은 일반 매칭(2131) 후 NONFIXED(2150) 블록이 같은 `bi`에 이어 실행되어 `count` 2회 증가 가능 | 원본 실행 순서 그대로 재현 후 logcat 대조 (4.6, Step 9 참조) |
| 5 | 마트 추가 시 분기 누락 | — | 인터페이스 미구현은 컴파일 에러로 차단 |
| 6 | AsyncTask가 Activity 필드 참조 | 내부 클래스라 직접 접근 | Step 3에서 `ctx` 경유로 교체. 클래스 분리는 별건 |
| 7 | `ShipmentActivity` 삭제 불가 | `BluetoothPrintService`가 상수 4종 참조 | 이 문서 범위 밖. 별건 처리 |
| 8 | 홈플러스 테스트 데이터 없음 | 운영 DB 0건 | 테스트 데이터 확보 후 Step 7 진행 |
| 9 | 개발62와 충돌 | 동일 파일 수정 | 개발62 롤백은 `cbbfd22`로 커밋 완료. **선행조건 충족** |
| 10 | 개발63과 충돌 | 롯데 박스순번 건 | **순서 제약 없음.** 개발63은 2899~3170 + JSP. **클래스 분리 완료 후 개발63 진행** |
| 11 | 마트사 묶음 오판 | 라벨·전송은 마트사 축, 중복확인은 계근방식 축으로 교차 | 2장 "분리 기준" 표 근거대로 마트사 5개 유지. searchType별 8개로 쪼개면 홈플러스 라벨이 중복됨 |
| 12 | 메모리 릭 | `ShipmentScreen`이 위젯 참조 보유 | Activity가 소유하고 `onDestroy`에서 함께 해제. Handler도 Activity 소유 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | 안전 정리 (죽은 코드·거짓 주석·미사용 import) | ⏳ 대기 |
| 2 | ShipmentScreen 위젯 10종 추가 (개발65에서 6종 선행 완료) | ⏳ 대기 |
| 3 | WeighingContext 도입 | ⏳ 대기 |
| 4 | 인터페이스 + Factory 골격 | ⏳ 대기 |
| 5 | 생산(1,7) 이관 — ProductionHandler | ⏳ 대기 |
| 6 | 롯데(6) 이관 — LotteHandler | ⏳ 대기 |
| 7 | 홈플러스(2,5) 이관 — HomeplusHandler | ⏳ 대기 |
| 8 | 도매(3) 이관 — WholesaleHandler | ⏳ 대기 |
| 9 | 이마트(0,4) 이관 — EmartHandler | ⏳ 대기 |
| 10 | Activity 정리 | ⏳ 대기 |
| 11 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/개발/56_setBarcodeMsg_디바운스_수정[setBarcodeMsg_디바운스_바코드값_미비교].md`
- `app/doc/개발/58_홈플러스_search_shipment_homeplus_이마트패턴_포팅[32_33_34].md`
- `app/doc/개발/60_setBarcodeMsg_생산1_전용메서드_분리.md`
- `app/doc/개발/62_생산_계근대상_지시일자_이후조회.md`
- `app/doc/개발/63_롯데_박스순번_파이프라인_복구[36].md`
- `app/doc/오류/32_홈플러스_월품목별재고_V뷰_년월조건_누락_출하대상_행중복[홈플러스_출하계근_AI정적검증].md`
- `app/doc/오류/33_홈플러스_SM_수주상세_LEFT_JOIN_다중행_팽창_출하건_중복[홈플러스_출하계근_AI정적검증].md`
- `app/doc/오류/34_홈플러스_STORE_IN_DATE_datetime_CONVERT_미적용_라벨_날짜_오인쇄[홈플러스_출하계근_AI정적검증].md`
- `app/doc/참고자료/오류패턴_분석.md`

---

**문서 버전**: 1.0
