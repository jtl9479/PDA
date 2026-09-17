# BixolonShipmentActivity 생산(1)·생산라벨(7) 클래스 분리

**작성일**: 2026-09-17
**목적**: 개발66에서 비생산 6종(0·2·3·4·5·6)을 `shipment/type/`으로 분리하면서 **생산(searchType 1)·생산라벨(searchType 7)은 대상에서 제외**해 `BixolonShipmentActivity.java`(3,103줄)에 본문 744줄이 그대로 남아 있다. 이번 문서는 같은 구조(`ShipmentType` 구현체)로 생산 2종을 이관해 Activity의 잔여 본문을 제거한다. 기존 동작은 100% 동일하게 유지한다.

> **사용자 결정 (2026-09-17)**: `ProductionType`(1)과 `ProductionLabelType`(7) 두 파일을 만든다. 7은 현재 진입 경로가 없는 미사용 기능이지만(§1.4), 원본 동작을 그대로 보존한다.

> **이 문서는 개발66의 결정을 번복한다.** 개발66은 두 곳에서 생산을 분리 대상에서 뺐다.
> - 66 §3 "생산(1, 7)은 파일을 만들지 않는다."
> - 66 Step 13 주의할 점 "**생산 코드는 정리 대상이 아니다**"
>
> 당시 판단은 "생산을 건드리지 않는 것이 가장 안전하다"였고, 그 결과 Activity가 3,739 → 3,103줄로 636줄만 줄었다(삭제는 Step 0의 죽은 코드 293줄과 Step 7의 `setBarcodeMsg` 401줄뿐).
> 2026-09-17 사용자가 "생산도 파일로 관리하라"고 지시해 방침을 바꾼다. **안전 장치는 컷오버를 마지막에 두는 것으로 대체한다**(추가 제약 1).

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

### 추가 제약 조건 (이 가이드 한정)

1. **컷오버는 마지막 Step에서 한 번에 한다.** 개발66의 6종은 `onCreate`가 Step 1부터 `shipmentType`을 만들었다. 이 문서는 반대다 — Activity의 위임 게이트 5곳(`wet_data_insert`·`MESSAGE_REPRINT`·`inputBtnListener`·`ProgressDlgShipSelect.doInBackground`·`ProgressDlgShipmentSend.doInBackground`)이 **이미 `shipmentType != null`을 검사하는 상태로 존재**하므로, `onCreate`의 생산 제외 조건을 먼저 풀면 구현이 끝나지 않은 메서드가 즉시 호출되어 `UnsupportedOperationException`이 난다. `onCreate` 조건 제거와 `setBarcodeMsg`의 생산 분기 제거는 **Step 8(컷오버) 하나로 묶는다.**
2. **생산라벨(7)의 바코드 스캔 본문은 현재 Activity에 없다.** 개발66 Step 7에서 `setBarcodeMsg` 공용 본문이 삭제됐다. 복원 원본은 `BixolonShipmentActivity_Back.java`(Step 0 삭제 이전 백업, 클래스명만 다름) 또는 `git show HEAD~N:...`로 확인하며, 이 문서에서는 백업 파일 라인 번호(1136~1539)를 사용한다.
3. **생산(1)과 생산라벨(7)은 바코드 스캔 본문이 다르다.** 1은 개발60이 이미 W/HW·B 블록을 제거한 `setBarcodeMsgProduction`을, 7은 그 블록이 살아있는 공용 `setBarcodeMsg` 원본을 쓴다. 두 파일을 같은 본문으로 만들지 않는다(§1.3).
4. **`find_work_info`는 Activity에 남긴다.** `ProgressDlgShipSelect.onPostExecute`(2177)가 타입과 무관하게 이 메서드를 직접 호출한다. 대신 개발66 Step 9 전례(`EmartNonfixedType`)와 동일하게, 각 타입 파일에 **자기 전용 사본**을 둔다(§8 Step 5).
5. **원본(`D:\PDA\PDA-INNO(원본)`)과 동작이 다른 코드를 발견해도 이 문서에서는 고치지 않는다.** `app/doc/오류/`에 별건으로 문서화만 한다.
6. **위임 게이트 5곳의 `if (shipmentType != null)` 가드 제거는 별도 Step(§8 Step 9)으로 분리하고, 사용자 승인 후에만 진행한다.** 컷오버(Step 8) 직후에는 가드가 남아 있어도 동작에 문제가 없다(항상 참이 될 뿐이다).

---

## 1. 현재 구조

### 1.1 개발66 이후 Activity 상태

`BixolonShipmentActivity.java` 3,103줄. 비생산 6종은 전부 `shipment/type/` 구현체로 위임되고, 생산 2종만 본문이 남아 있다.

| # | 역할 | 위치 | 대상 |
|:-:|---|---|---|
| 1 | `setBarcodeMsg` 진입점 | 1178~1187 | 6종은 무조건 위임(1186), 생산(1)만 `setBarcodeMsgProduction` 분기(1180~1182) |
| 2 | `setBarcodeMsgProduction` | 1218~1477 (260줄) | 생산(1) 전용. **7은 이 메서드를 타지 않는다** |
| 3 | `wet_data_insert` | 1525~1680 | 1526~1531 위임 게이트(`shipmentType != null`). 이후 본문(1533~1680, 약 148줄)이 1·7의 else 경로 |
| 4 | `find_PackerProduct` / `find_PackerProductBarcodeGoods` / `find_work_info` / `find_work_info_barcodeGoods` | 1707~1856 | `find_PackerProduct`(1707~1722, 16줄)·`find_PackerProductBarcodeGoods`(1724~1740, 17줄)는 **생산(1)만 호출**(1245·1249). `find_work_info`(1742~1808, 67줄)는 생산(1)과 `ProgressDlgShipSelect.onPostExecute`(2177) **공유**. `find_work_info_barcodeGoods`(1810~1856, 47줄)는 생산(1)만 호출(1729) |
| 5 | `ProgressDlgShipSelect.doInBackground` | 2122~2153 | 2142~2144 위임 게이트. 생산 2종은 `onShipmentLoaded`에서 할 일이 없음(§1.3) |
| 6 | `MESSAGE_REPRINT` | 957~983 | 962~967 위임 게이트. 이후 else 경로(969~981)에 홈플·롯데·**생산라벨(7)**·이마트(else) 분기가 섞여 있다 |
| 7 | `ProgressDlgShipmentSend.doInBackground` | 2310~2522 | 2326~2330 위임 게이트. 이후 일괄 분기(2412~2515, 약 104줄)에 **생산(1)·생산라벨(7)·도매(3)·비정량(4·5)**이 함께 걸린다. 실제로 도매·비정량은 이미 각자 타입 파일 `send`로 옮겨졌으므로 **이 분기에 실제 도달하는 것은 생산 2종뿐** |
| 8 | `inputBtnListener` | 646~757 | 667~670 위임 게이트. 이후 else 본문(672~751, 약 80줄)이 1·7의 수기 입력 경로 |

**합계 744줄** (260 + 148 + 147 + 5 + 104 + 80, 위임 게이트 자체 제외).

> 착수 지시 때 오간 "888줄"은 **메서드 전체** 기준(`wet_data_insert` 156 · `ProgressDlgShipmentSend.doInBackground` 213 · `inputBtnListener` 112 …)이라 위임 게이트와 공용 머리부까지 포함한 값이다.
> 그 머리부는 이관 대상이 아니다 — `ProgressDlgShipmentSend` 의 목록 조회(`qry_where` 구성 + `selectquerySendGoodsWet`)와 `inputBtnListener` 의 `work_flag` 1·2 분기는 타입과 무관하게 Activity에 남는다.
> **실제 이관 대상은 744줄**이며, 이 문서는 744를 기준으로 삼는다.

### 1.2 위임 게이트 6곳 — 이미 존재하는 코드

| # | 위치 | 조건 | 생산(1)·생산라벨(7) 현재 동작 |
|:-:|---|---|---|
| 1 | `setBarcodeMsg`(1178~1187) | `if (PRODUCTION) setBarcodeMsgProduction(); return;` → 그 외 **무조건** `shipmentType.onBarcodeScanned(msg)` | **1은 분기, 7은 무조건 위임 경로를 탄다.** `shipmentType`은 7일 때 `null`이므로 **NPE**. 단, §1.4에 따라 7은 현재 도달 경로가 없다 |
| 2 | `wet_data_insert`(1526~1531) | `if (shipmentType != null) { ...; return; }` | 1·7 모두 `shipmentType == null` → 안전하게 else 본문 실행 |
| 3 | `MESSAGE_REPRINT`(962~967) | 동일 | 동일 |
| 4 | `inputBtnListener`(667~670) | 동일 | 동일 |
| 5 | `ProgressDlgShipSelect.doInBackground`(2142~2144) | `if (shipmentType != null) shipmentType.onShipmentLoaded(arSM);` | 1·7 모두 호출되지 않음(안전) |
| 6 | `ProgressDlgShipmentSend.doInBackground`(2326~2330) | `if (shipmentType != null) return shipmentType.send(...);` | 동일 |

**`setBarcodeMsg`만 무조건 위임이고 나머지 5곳은 `!= null` 가드가 있다.** 이 비대칭이 7의 NPE 원인이며, 현재는 §1.4의 이유로 발현되지 않는다.

### 1.3 생산(1)과 생산라벨(7)의 동작 차이

| 항목 | 생산(1) | 생산라벨(7) |
|---|---|---|
| 바코드 스캔 본문 | `setBarcodeMsgProduction`(1218~1477) 전용 — 개발60이 W/HW·B·킬코이·센터명·비정량 분기를 이미 제거함 | **Activity에 없음.** 공용 `setBarcodeMsg` 원본(백업 1143~1539)을 그대로 씀. searchType 게이트가 전부 거짓이라 **도매·롯데와 동일한 경로**(§1.4) |
| ITEM_TYPE 블록 | S·J만 유지 | W/HW·S·J·B **전부 유지**(게이트 없음) |
| 계근 라벨(`wet_data_insert` 내 `Common.print_bool`) | **해당 분기 없음 — 라벨 출력 안 함** | `SEARCH_TYPE_PRODUCTION_LABEL` 분기 → `setPrinting_prod` |
| 재출력 라벨(`MESSAGE_REPRINT`) | 해당 분기 없음 → **else 경로(이마트 라벨 `setPrinting`)** | `SEARCH_TYPE_PRODUCTION_LABEL` 분기 → `setPrinting_prod` |
| 계근 INSERT | 둘 다 else 경로(일반 `insertqueryGoodsWet`) — 홈플·롯데 분기 미해당 | 동일 |
| 계근중량 반올림 | 둘 다 else 경로(소수 3자리) — 이마트만 1자리 | 동일 |
| 전송 | 둘 다 일괄 누적(2412~2515) + `URL_INSERT_GOODS_WET_PRODUCTION`(2458~2460) | 동일 |
| 수기 입력 절사 | 둘 다 else 경로(절사 없음, `Double.toString`) | 동일 |
| 수기 입력 소비기한 창 | CENTERNAME이 TRD/WET/E·T를 포함할 때만(데이터 의존, 생산은 `'하이랜드푸드'` 고정이라 사실상 진입 안 함) | 동일 |

### 1.4 생산라벨(7) 진입 경로 — 개발66 Step 7에서 확인된 사실 (그대로 인용)

| # | 차단 지점 | 근거 |
|:-:|---|---|
| 1 | 화면 | `activity_main.xml` — 생산라벨 버튼 2개(`btnproductionlist4print`·`btnProdWet4print`)를 감싼 `TableRow`가 `android:visibility="gone"` |
| 2 | 진입점 | `Common.searchType`에 `"7"`을 넣는 곳은 위 두 버튼의 핸들러(`MainActivity` 301·347)뿐 |
| 3 | 상태 | `Common.searchType`은 `public static String searchType = "0"`으로 저장·복원 없이 실행마다 `"0"`에서 시작 |

추가로 서버측 `search_production_4label.jsp`도 삭제되어(2026-08-04 제외 결정) 대상 리스트를 받을 수 없다. **이 문서는 이 상태를 바꾸지 않는다.** `ProductionLabelType`은 화면이 살아나는 미래를 대비해 원본 동작을 보존하는 목적으로만 만든다.

### 1.5 데이터 의존 분기 — 접지 말 것 (개발66 §1.4·§1.5 계승)

| 분기 | 근거 |
|---|---|
| 킬코이(`PACKER_CODE`)·미트센터(`STORE_CODE`) 소비기한 검증 | `searchType` 게이트 없음. 생산 2종도 그대로 진입 가능(현재는 값이 없어 미진입) |
| 센터명(용인/대구/시화(W)/여주TRD, `E/T`, `WET`) 진입 | `searchType` 게이트 없음. 생산은 `CENTERNAME='하이랜드푸드'` 고정이라 사실상 미진입 |
| 재귀 호출 시 `lastBarcodeProcessedTime = 0` | 디바운스 우회 목적. 자기 자신을 호출해야 한다(개발66 §1.5 동일 원칙) |
| 중복검사 조회는 결과를 버려도 호출 자체는 유지 | 해당 없음(생산 2종은 항상 조회 후 판정에 사용한다) |

### 문제점

1. **생산 2종만 여전히 Activity 안에 있다** — 744줄이 6곳 위임 게이트의 else 경로로 남아, 위임 게이트가 이미 존재함에도 실제로는 아무 타입에도 위임되지 않는다
2. **`setBarcodeMsg`의 비대칭** — 6종은 무조건 위임인데 생산(1)만 별도 분기다. 7이 살아나면 이 비대칭이 NPE로 드러난다(§1.2)
3. **7의 원본 본문이 소스에서 사라짐** — 개발66 Step 7에서 지워졌기 때문에, 되살리려면 백업 파일이나 git 이력에서 복원해야 한다
4. **`find_PackerProduct` 계열 4개 메서드가 사실상 생산 전용인데 이름이 일반적이다** — 다른 6종은 이미 자기 타입 파일에 사본을 두고 있어(개발66 Step 9), 이 4개는 생산만 쓰는 코드로 정리 대상이다

---

## 2. 변경 구조

### 데이터 흐름 — 변경 전 (현재)

```
[바코드 스캔] setMessage → setBarcodeMsg(1178)
    ├─ if (PRODUCTION) → setBarcodeMsgProduction(1218)   260줄, 생산(1) 전용
    └─ else            → shipmentType.onBarcodeScanned   6종 위임 (7은 shipmentType == null → NPE, 현재 미도달)

wet_data_insert / MESSAGE_REPRINT / inputBtnListener /
ProgressDlgShipSelect / ProgressDlgShipmentSend
    ├─ if (shipmentType != null) → 6종 위임
    └─ else(본문 그대로)         → 생산(1)·생산라벨(7)이 공유
```

### 데이터 흐름 — 변경 후 (Step 8 컷오버 이후)

```
[바코드 스캔] setMessage → setBarcodeMsg
    └─ shipmentType.onBarcodeScanned(msg)   ← switch 없음. 8종 전부 위임

onCreate
    └─ shipmentType = ShipmentTypeFactory.create(Common.searchType, this);  ← 생산 제외 조건 삭제

wet_data_insert / MESSAGE_REPRINT / inputBtnListener /
ProgressDlgShipSelect / ProgressDlgShipmentSend
    └─ if (shipmentType != null) → 8종 전부 위임 (가드는 Step 9에서 검토 후 제거)
         ↓
    [ProductionType(1) / ProductionLabelType(7)] 중 하나
         ├─ 자기 타입의 흐름만 보유
         └─ activity.xxx() 미이관 메서드(find_work_info 등) 호출
```

### 클래스 구성

```
com/rgbsolution/highland_emart/shipment/type/
  ShipmentType.java               ← 기존 인터페이스 재사용 (수정 없음)
  ShipmentTypeFactory.java        ← case "1", "2" 추가만
  ShipmentConst.java              ← 기존 상수 재사용 (수정 없음)
  EmartType / HomeplusType / WholesaleType /
  EmartNonfixedType / HomeplusNonfixedType / LotteType   ← 개발66 산출물, 무변경

  ProductionType.java        (1)  생산 계근 — setBarcodeMsgProduction 이관
  ProductionLabelType.java   (7)  생산 라벨 — 원본 setBarcodeMsg 이관 (미사용이지만 원본 보존)
```

### 접근 방식 — 골격을 먼저 만들되, 실제 연결(컷오버)은 맨 마지막에

| 단계 | 내용 | 안전한 이유 |
|:-:|---|---|
| A (Step 1~7) | `ProductionType`·`ProductionLabelType`을 만들고 7개 메서드를 전부 채운다. **`onCreate`는 손대지 않는다** | `ShipmentTypeFactory.create("1"/"7", ...)`가 호출되지 않으므로, 구현이 틀려도 런타임에 영향이 없다. 컴파일 오류만으로 검증 가능 |
| B (Step 8) | `onCreate` 생산 제외 조건 삭제 + `setBarcodeMsg`의 생산 분기 삭제 + `setBarcodeMsgProduction` 삭제. **한 번에** | 이미 존재하는 5곳의 `if (shipmentType != null)` 게이트가 이 순간부터 생산 2종에도 참이 된다. A단계가 끝나 있어야 안전하다 |
| C (Step 9, 선택) | 게이트 5곳 단순화 + Activity의 이제 죽은 메서드(생산 전용이던 4개) 정리 | 8종 전부 `shipmentType != null`이 항상 참이 되므로 가드가 무의미해진다. 사용자 승인 후 진행 |

개발66과 반대 순서다. 개발66은 게이트가 없는 상태에서 시작해 Step마다 게이트를 하나씩 추가했지만, 이 문서는 **게이트가 이미 다 있는 상태에서 시작**하므로 구현을 다 채운 뒤 마지막에 연결한다.

---

## 3. 수정 대상 파일

### 3.1 추가 파일 (신규 생성) — 2개

| # | 파일 | 내용 | 착수 Step |
|:-:|------|------|:-:|
| 1 | **ProductionType.java** (1) | 생산 계근. `setBarcodeMsgProduction` 이관 | 2, 4, 5, 6, 7 |
| 2 | **ProductionLabelType.java** (7) | 생산 라벨. 원본 공용 `setBarcodeMsg` 이관(W/HW·B 블록 보존) | 3, 4, 5, 6, 7 |

### 3.2 기존 소스 → 타입 파일 이관 매핑

| 축 | 기존 위치 | 대상 | Step |
|---|---|---|:-:|
| 바코드 스캔(1) | `setBarcodeMsgProduction`(1218~1477, 260줄) | `ProductionType.onBarcodeScanned` | 2 |
| 바코드 스캔(7) | 백업 `setBarcodeMsg`(1143~1539) — 개발66 Step 7에서 삭제된 공용 본문 | `ProductionLabelType.onBarcodeScanned` | 3 |
| 계근 저장 + 라벨 + 조회후처리 | `wet_data_insert`(1533~1680) · `MESSAGE_REPRINT` else(969~981) · `ProgressDlgShipSelect.onShipmentLoaded` 게이트(2142~2144, 둘 다 no-op) | `onWeightConfirmed` · `reprintLabel` · `onShipmentLoaded` | 4 |
| 상품 매칭 | `find_PackerProduct`(1707~1722) · `find_PackerProductBarcodeGoods`(1724~1740) · `find_work_info` 사본(1742~1808) · `find_work_info_barcodeGoods`(1810~1856) | `findPackerProduct(barcode, workFlag)` | 5 |
| 전송 | `ProgressDlgShipmentSend.doInBackground` 일괄 분기 중 PRODUCTION·PRODUCTION_LABEL 경로(2412~2515) | `send(...)` | 6 |
| 수기 입력 | `inputBtnListener` else 본문(672~751) | `onManualInput()` | 7 |

### 3.3 Activity 잔류 (이관하지 않음)

| 항목 | 위치 | 사유 |
|---|---|---|
| `find_work_info`(원본) | 1742~1808 | `ProgressDlgShipSelect.onPostExecute`(2177)가 타입 무관하게 직접 호출한다(§1.4 제약 4) |
| 생명주기 생산 분기 | 439~442(onCreate 생성 조건, Step 8에서 수정), 534, 574, 580 | 블루투스·프린터 연결 관련. 이 문서 범위 밖(이관 대상 표에 없음) |
| 레이아웃·위젯·상태 필드 | 개발66과 동일 | 이 문서 범위 밖 |

### 3.4 미변경 파일

`ShipmentType.java`, `ShipmentConst.java`(값 재사용, 수정 없음), `LabelPrintHelper.java`, `DBHandler.java`, `HttpHelper.java`, `Common.java`, `Shipments_Info.java`, `Barcodes_Info.java`, `Goodswets_Info.java`, 개발66이 만든 6개 타입 파일 전부, JSP 전체, 레이아웃 XML 전체

---

## 4. 수정 상세

### 4.1 ShipmentTypeFactory.java

**변경 전** (기존):

```java
public static ShipmentType create(String searchType, BixolonShipmentActivity activity) {
    switch (searchType) {
        case "0": return new EmartType(activity);
        case "2": return new HomeplusType(activity);
        case "3": return new WholesaleType(activity);
        case "4": return new EmartNonfixedType(activity);
        case "5": return new HomeplusNonfixedType(activity);
        case "6": return new LotteType(activity);
        // 생산(1) · 생산라벨(7) 은 Activity가 직접 처리한다 (setBarcodeMsgProduction)
        default:  throw new IllegalArgumentException("지원하지 않는 searchType: " + searchType);
    }
}
```

**변경 후**:

```java
public static ShipmentType create(String searchType, BixolonShipmentActivity activity) {
    switch (searchType) {
        case "0": return new EmartType(activity);
        case "2": return new HomeplusType(activity);
        case "3": return new WholesaleType(activity);
        case "4": return new EmartNonfixedType(activity);
        case "5": return new HomeplusNonfixedType(activity);
        case "6": return new LotteType(activity);
        case "1": return new ProductionType(activity);         // 개발67
        case "7": return new ProductionLabelType(activity);    // 개발67 — 미사용이지만 원본 보존
        default:  throw new IllegalArgumentException("지원하지 않는 searchType: " + searchType);
    }
}
```

**검증**: Step 1~7에서는 `onCreate`가 여전히 1·7을 걸러내므로 이 case에 도달하지 않는다. Step 8에서 처음 도달한다.

### 4.2 onCreate — 생산 제외 조건 삭제 (Step 8)

**변경 전** (439~442):

```java
if (!Common.searchType.equals(SEARCH_TYPE_PRODUCTION)
        && !Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)) {
    shipmentType = ShipmentTypeFactory.create(Common.searchType, this);
}
```

**변경 후**:

```java
// 개발67 Step 8 : 생산(1)·생산라벨(7)도 타입 구현체로 이관 완료. 예외 조건 제거
shipmentType = ShipmentTypeFactory.create(Common.searchType, this);
```

### 4.3 setBarcodeMsg — 생산 분기 삭제 (Step 8)

**변경 전** (1178~1187):

```java
public void setBarcodeMsg(final String msg) {
    if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) {
        setBarcodeMsgProduction(msg);
        return;
    }
    shipmentType.onBarcodeScanned(msg);
}
```

**변경 후**:

```java
public void setBarcodeMsg(final String msg) {
    // 개발67 Step 8 : 생산(1)·생산라벨(7) 포함 8종 전부 타입 구현체로 이관 완료
    shipmentType.onBarcodeScanned(msg);
}
```

`setBarcodeMsgProduction`(1218~1477) 본문은 `ProductionType.onBarcodeScanned`로 완전히 옮겨졌으므로 이 시점에 삭제한다.

### 4.4 ProductionType.java — 바코드 스캔 예시 (Step 2)

**원본**: `setBarcodeMsgProduction`(1218~1477). 접는 조건은 **없다** — 개발60이 이미 생산 미해당 분기를 전부 제거했으므로 기계적으로 그대로 옮긴다.

**치환 3곳** (개발66 4.3과 동일한 종류의 치환)

| 원본 | 변경 후 | 사유 |
|---|---|---|
| `new ProgressDlgShipSelect(this, ...)`(1267) | `a.startShipSelect(...)` | inner 클래스라 외부 패키지에서 생성 불가 |
| `new ProgressDlgShipSelect(BixolonShipmentActivity.this, ...)`(1308) | `a.startShipSelect(...)` | 동일 |
| `setBarcodeMsgProduction(msg)`(1291, 재귀) | `this.onBarcodeScanned(msg)` | 재귀는 Activity가 아니라 자기 자신 |
| `find_PackerProduct(msg)` / `find_PackerProductBarcodeGoods(msg)` | `this.findPackerProduct(msg, 1)` / `this.findPackerProduct(msg, 2)` | Step 5에서 상품 매칭이 자기 타입 메서드로 바뀜(개발66 Step 9와 동일 패턴) |

**검증**: 원본 260줄과 나란히 diff → 치환 4곳 외에 변경 0줄인지 확인. `ITEM_TYPE` S·J 2블록만 있어야 하며 W/HW·B가 없어야 한다(§1.3).

### 4.5 ProductionLabelType.java — 바코드 스캔 예시 (Step 3)

**원본**: 백업 파일 `setBarcodeMsg`(1143~1539, 397줄). 접는 조건은 **개발66 §1.3과 동일한 4개**(전부 거짓 — 도매·롯데와 같은 패턴).

| 백업 라인 | 조건 | 처리 |
|---|---|---|
| 1198 | `NONFIXED \|\| HP_NONFIXED` 중복확인 제외 | 거짓 → 블록 삭제 |
| 1287 (바깥 1286 골격 유지) | `EMART` 트레이더스 소비기한 검증 | 거짓 → 내부 블록만 삭제 |
| 1322 | `NONFIXED \|\| HP_NONFIXED` | 거짓 → 블록 삭제 |
| 1436, 1498 | `EMART` LB 환산 자릿수 | 거짓 → `else` 경로만 유지 |

**생산(1)과의 차이 — 유지해야 할 블록**

| 블록 | 생산(1) | 생산라벨(7) |
|---|:-:|:-:|
| `ITEM_TYPE` W/HW(1359~1411) | 없음(개발60이 제거) | **유지** |
| `ITEM_TYPE` S(1412~1462) | 유지 | 유지 |
| `ITEM_TYPE` J(1463~1471) | 유지 | 유지 |
| `ITEM_TYPE` B(1474~1525) | 없음(개발60이 제거) | **유지** |
| 킬코이·센터명 판정(1278~1296) | 없음(개발60이 제거) | **유지**(데이터 의존, §1.5) |

**검증**: 원본 397줄 대비 접은 조건 4곳(약 23줄)만 삭제되고 그 외 전부 일치하는지 확인. `WholesaleType`·`LotteType`과 대조했을 때 접는 방식이 동일해야 한다(개발66 §8 Step 2·3 패턴 재현).

---

## 5. 사이드이펙트

### 5.1 컷오버 순서 (최우선 확인)

- Step 8 이전에는 `ShipmentTypeFactory.create("1"/"7", ...)`가 절대 호출되지 않는다. `onCreate` 조건과 `setBarcodeMsg` 분기를 **동시에** 바꾸지 않으면, 둘 중 하나만 바뀐 중간 상태에서 미완성 구현체가 호출될 수 있다
- **대응**: Step 8을 하나의 커밋·하나의 검증 단위로 묶는다. 부분 적용 금지

### 5.2 `find_PackerProduct` 계열 4개 메서드의 운명

- Step 5까지는 Activity의 `find_PackerProduct`·`find_PackerProductBarcodeGoods`·`find_work_info_barcodeGoods`(원본)가 여전히 존재하고 `setBarcodeMsgProduction`이 호출 중이다
- Step 8에서 `setBarcodeMsgProduction`이 삭제되면 이 3개 메서드는 **호출처 0건**이 된다(`find_work_info`는 제외 — §3.3)
- **대응**: 이 문서에서는 즉시 삭제하지 않는다. Step 9(선택, 사용자 승인 필요)에서 개발66 Step 0과 같은 방식으로 처리 여부를 결정한다

### 5.3 `MESSAGE_REPRINT`의 이마트(else) 경로

- 현재 else 경로(979~981, "이마트수기프린팅")는 생산(1)이 재출력할 때도 타는 경로다(§1.3). `ProductionType.reprintLabel`은 이 동작을 그대로 구현해야 한다 — "생산인데 이마트 라벨이 나가는" 원본 동작을 보존한다
- **대응**: 코드 검증 시 `ProductionType.reprintLabel`이 `setPrinting`(이마트)을 호출하는지, `ProductionLabelType.reprintLabel`이 `setPrinting_prod`를 호출하는지 이름만 보고 혼동하지 않도록 확인한다

### 5.4 `ProgressDlgShipmentSend`의 일괄 분기 공유

- 2412 조건 `PRODUCTION || WHOLESALE || NONFIXED || HOMEPLUS_NONFIXED || PRODUCTION_LABEL`은 5개 searchType을 함께 걸지만, 도매·비정량 2종은 이미 개발66에서 자기 `send`로 옮겨져 **실제로는 도달하지 않는다**(위임 게이트가 먼저 가로챈다)
- **대응**: 이 조건 자체를 건드리지 않는다. `ProductionType.send`·`ProductionLabelType.send`는 이 분기의 본문을 복사하되, `Common.searchType.equals(SEARCH_TYPE_PRODUCTION) || ...equals(SEARCH_TYPE_PRODUCTION_LABEL)`로 좁혀 URL을 결정하는 안쪽 조건(2458)만 남긴다

### 5.5 생명주기의 생산 분기 (변경하지 않음)

```java
// 534, 574, 580 — 블루투스/프린터 연결 관련. 이관 대상 표(§1.1)에 없다
if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) { ... }
```

- 이 문서는 바코드 스캔·계근 저장·상품 매칭·전송·수기 입력 축만 다룬다. 생명주기 분기는 `ShipmentType` 인터페이스의 책임이 아니므로 손대지 않는다

---

## 6. 데이터 저장 구조

### 6.1 타입 파일이 참조하는 Activity 상태 (개발66과 동일 목록, 재확인만)

| 필드 | 참조 | 비고 |
|---|:-:|---|
| `arSM`, `current_work_position`, `work_ppcode`, `work_bl_no`, `work_item_fullbarcode`, `work_item_barcodegoods`, `work_item_bi_info`, `expiryDayTrans`, `dialog_flag`, `scan_flag`, `work_flag`, `lastProcessedBarcode`, `lastBarcodeProcessedTime`, `alert_flag`, `vibrator` | O | 개발66에서 이미 `public`으로 공개됨. 추가 공개 불필요 |
| `centerTotalCount`/`Weight`, `centerWorkCount`/`Weight` | O | 동일 |
| `select_position` | X → 필요 시 재공개 | 개발66 Step 13에서 `private`로 되돌림(§1.3 원문). `ProductionType.reprintLabel`이 인자로만 받으면 재공개 불필요 |
| `labelPrintHelper`, `printerCallback`, `edit_center_tcount` 등 위젯류 | O | 개발66 Step 8에서 공개됨 |

**새로 공개할 필드는 없다.** 개발66이 이미 필요한 접근제한자를 열어 두었다.

### 6.2 이 타입만의 상태

없음. 롯데의 `lotte_TryCount` 같은 타입 전용 카운터가 생산 2종에는 없다(§1.3, `onShipmentLoaded`는 두 타입 다 no-op).

---

## 7. 호출 시점

```
[로그인 → 메인 → 생산계근/생산라벨 진입]
    ↓
BixolonShipmentActivity.onCreate
    └── shipmentType = Factory.create(Common.searchType, this)   ← Step 8부터 1·7도 포함
    ↓
[상품 바코드 스캔]
    ↓
setMessage → setBarcodeMsg → shipmentType.onBarcodeScanned(msg)
    ├── ProductionType(1)      : findPackerProduct → startShipSelect → onWeightConfirmed
    └── ProductionLabelType(7) : 동일 흐름, ITEM_TYPE W/HW·B 추가 지원
            ↓
        [BL 스캔] 중량 추출 → onWeightConfirmed
                        ├── DB 저장 (일반 INSERT, 3자리 반올림)
                        ├── 화면 반영
                        └── 라벨 출력 (1: 없음 / 7: setPrinting_prod)
    ↓
[전송 버튼] → ProgressDlgShipmentSend → shipmentType.send(...)   ← URL_INSERT_GOODS_WET_PRODUCTION
    ↓
[상세 팝업 재출력] → DetailAdapter → mHandler → reprintLabel(...)  ← 1: 이마트 라벨 / 7: setPrinting_prod
```

---

## 8. 개발 플랜

### Step 1: 골격 준비 (동작 변화 0)

**Part 1. 분석**
- 메서드: 신규
- 범위: `shipment/type/ProductionType.java`, `ProductionLabelType.java`, `ShipmentTypeFactory.java`
- 용도: 이후 Step이 채워 넣을 골격 확보. `onCreate`는 손대지 않아 런타임 영향이 0이 되게 한다
- 주의할 점: `ShipmentType` 인터페이스의 **메서드 선언**과 `ShipmentConst`는 **수정하지 않는다**(이미 7개 메서드·모든 상수가 존재). Javadoc 문구만 정정한다(아래 #4·#5)

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | `ProductionType` | 신규 | 빈 구현 7개, 전부 `UnsupportedOperationException("Step N에서 이관 예정")` |
| 2 | `ProductionLabelType` | 신규 | 동일 |
| 3 | `ShipmentTypeFactory` | 수정 | case "1", "7" 추가(§4.1) |
| 4 | `ShipmentType` Javadoc | 수정 | 현재 "생산(1, 7)은 구현체를 만들지 않는다. `setBarcodeMsgProduction` 경로를 그대로 사용한다"로 적혀 있다. 이 문서 실행 후 사실과 어긋나므로 **구현체 8종**으로 정정하고, 구현체 목록에 `ProductionType`·`ProductionLabelType` 을 추가한다 |
| 5 | `ShipmentTypeFactory` Javadoc | 수정 | 현재 "생산(1, 7)은 이 메서드로 들어오지 않는다"로 적혀 있다. **1·7도 처리한다**로 정정한다. `default: throw` 는 유지하므로 그 설명은 그대로 둔다 |

> Javadoc 정정 시점 주의 — #4·#5는 **Step 8(컷오버) 이후에 사실이 된다.** Step 1에서는 "Step 8 컷오버 후 적용"임을 주석에 명시하거나, Javadoc 정정만 Step 8로 미룬다. 어느 쪽이든 **Step 1~7 동안 문서와 코드가 어긋난 채로 방치하지 않는다**

**Part 2. 변환 계획**
- 변환 방식: 신규 파일 2개 + Factory 2줄 추가
- 주의사항: `onCreate`의 생산 제외 조건을 이 Step에서 **절대 건드리지 않는다.** 건드리면 스텁이 즉시 호출되어 런타임 크래시가 난다

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 — `gradlew assembleDebug` → `BUILD SUCCESSFUL` (2026-09-17)
- [x] Part 5: 단위테스트 — **해당 없음**(호출되지 않음). `onCreate` 미변경으로 실행 경로 변화 0건임을 코드검증에서 확인
- [ ] Part 6: 회귀테스트 — 실기기 확인 필요 (생산(1) 로그인 → 기존 `setBarcodeMsgProduction` 경로 그대로 동작, 스텁 미호출)

**Part 6. 변경 내용** (완료):
- **무엇을**: `ProductionType`(1) · `ProductionLabelType`(7) 골격 2개를 만들고 Factory에 case 2개를 추가했다. `ShipmentType` · `ShipmentTypeFactory` 의 Javadoc 도 정정했다
- **왜**: Step 2~7이 올라탈 골격 확보. 이 Step에서는 **아무도 호출하지 않는 상태**로 두어 런타임 영향을 0으로 만든다
- **어떻게**: `onCreate` 의 생산 제외 조건을 **건드리지 않았다.** 그래서 searchType 1·7은 여전히 Factory를 타지 않고 `shipmentType` 이 null 이며, 새 case는 Step 8에서 처음 도달한다

**산출물**

| 파일 | 상태 | 내용 |
|---|---|---|
| `shipment/type/ProductionType.java` | 신규 | 스텁 7개 + 타입 판정 Javadoc (ITEM_TYPE S·J만 · 킬코이/센터명 판정 없음 · 라벨 없음 · 전송 production JSP) |
| `shipment/type/ProductionLabelType.java` | 신규 | 스텁 7개 + 생산(1)과의 차이 Javadoc (W/HW·B 유지 · 킬코이/센터명 유지 · 라벨 `setPrinting_prod`) |
| `shipment/type/ShipmentTypeFactory.java` | 수정 | `case "1"` · `case "7"` 추가, `default: throw` 유지, Javadoc 정정 |
| `shipment/type/ShipmentType.java` | 수정 | 구현체 목록에 2종 추가, "생산은 구현체를 만들지 않는다" 서술 정정 |
| `BixolonShipmentActivity.java` | **미변경** | 이 Step의 핵심. `onCreate` · `setBarcodeMsg` 모두 그대로 |

**Javadoc 시점 처리** — `ShipmentType` · `Factory` 의 정정된 서술은 **Step 8 이후에야 완전히 사실**이 된다.
두 파일 모두 "Step 8(컷오버) 전까지는 `onCreate` 가 두 타입을 걸러내므로 도달하지 않는다"는 단서를 함께 적어, Step 1~7 동안 주석과 코드가 어긋나지 않게 했다.

**검증 결과**

| 항목 | 예상 | 실제 |
|---|---|---|
| 빌드 | — | `BUILD SUCCESSFUL` |
| 실행 경로 변화 | 0건 | **0건** (`onCreate` 439~442 미변경, `shipmentType` 여전히 null) |
| Activity 변경 | 없음 | **없음** (`git status` 미포함) |
| 스텁 시그니처 | 인터페이스 7개와 일치 | **일치** (이름·타입·순서·개수) |
| 스텁의 Step 번호 배정 | §8과 일치 | **일치** (2·3·4·5·6·7) |
| Javadoc 타입 판정 | 원본과 일치 | **일치** — `setBarcodeMsgProduction` 에 S·J만 존재(W/HW·B 없음), 백업 원본에는 4블록 전부 존재, 라벨 분기에 생산(1) 케이스 없음, 전송 URL 공용 확인 |

> 코드검증에서 `Factory` Javadoc 의 "Common.searchType 을 읽는 유일한 지점(레이아웃 분기 제외)" 서술이 지적됐다.
> Step 8 전까지는 `onCreate` 의 생산 제외 조건도 `searchType` 을 읽으므로, 그 예외를 괄호에 되살려 정정했다.

---

### Step 2: 바코드 스캔 이관 — 생산(1) ProductionType

**Part 1. 분석**
- 메서드: `setBarcodeMsgProduction`
- 범위: `BixolonShipmentActivity.java:1218~1477`(260줄) → `ProductionType.onBarcodeScanned`
- 용도: 개발60이 이미 정리해 둔 생산 전용 본문을 그대로 옮긴다
- 주의할 점: **접는 조건이 없다.** 치환 4곳(§4.4)만 있고 나머지는 100% 동일해야 한다

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 치환 | 1267, 1308 | `new ProgressDlgShipSelect(...)` → `a.startShipSelect(...)` |
| 2 | 치환 | 1291 | 재귀 `setBarcodeMsgProduction(msg)` → `this.onBarcodeScanned(msg)` |
| 3 | 치환 | 1245, 1249 | `find_PackerProduct`/`find_PackerProductBarcodeGoods` → `this.findPackerProduct(msg, 1/2)` (Step 5 완료 전까지는 임시로 `a.find_PackerProduct(msg)` 유지 가능 — Step 5에서 정리) |

**Part 2. 변환 계획**
- 변환 방식: 원본을 그대로 복사, 치환 4곳만 적용
- 주의사항: `ITEM_TYPE` S·J 2블록만 있어야 한다. W/HW·B가 섞여 들어가면 개발60의 결과를 되돌리는 것이므로 즉시 되돌린다

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 — `gradlew assembleDebug` → `BUILD SUCCESSFUL` (2026-09-17)
- [x] Part 5: 단위테스트 — **해당 없음**(Step 8 전까지 미호출). `onCreate` 미변경 확인
- [x] Part 6: 회귀테스트 — 원본과 diff **차이 0줄**(211줄 ↔ 211줄), 문자열 리터럴 79개도 전부 일치

**Part 6. 변경 내용** (완료):
- **무엇을**: 원본 `setBarcodeMsgProduction`(1218~1477) 258줄을 `ProductionType.onBarcodeScanned` 로 이관
- **왜**: 생산(1)의 바코드 스캔 흐름을 타입 파일이 소유하게 한다. 개발60이 만든 전용 본문이라 그대로 옮기면 된다
- **어떻게**: **접은 조건 0곳.** 치환만 했다. Activity 본문은 **삭제하지 않았다** — Step 8 컷오버 전까지 생산이 그 본문으로 동작해야 한다

**치환 내역**

| # | 치환 | 곳 |
|:-:|---|:-:|
| 1 | Activity 필드·메서드 → `a.` 접두어 | 다수 |
| 2 | `new ProgressDlgShipSelect(...).execute()` → `a.startShipSelect(...)` | 2 |
| 3 | 재귀 호출 → `this.onBarcodeScanned(msg)` | 1 |
| 4 | `BARCODE_PROCESS_DEBOUNCE_MS` · `ITEM_TYPE_S` · `ITEM_TYPE_J` → `ShipmentConst.*` | 3 |
| 5 | `new AlertDialog.Builder(BixolonShipmentActivity.this, …)` → `(a, …)` | 1 |

> 상품 매칭 호출(`a.find_PackerProduct` 계열)은 **Step 5**에서 자기 타입 메서드로 바꾼다(§4.4). 이번 Step에서는 Activity 메서드를 그대로 호출한다.

**검증 결과**

| 항목 | 예상 | 실제 |
|---|---|---|
| 빌드 | — | `BUILD SUCCESSFUL` |
| 원본 대조 (정규화 후) | 차이 0줄 | **0줄** (211 ↔ 211) |
| `ITEM_TYPE` 블록 | S · J 2개만 | **2개만** (W/HW · B 없음) |
| 킬코이 · 센터명 판정 | 없음(로그만) | **없음** — 원본도 로그 출력용뿐 |
| 디바운스 · 재귀 우회 | 원본 동일 | **동일** (`lastBarcodeProcessedTime = 0` 후 재귀) |
| 재귀 무한루프 | 없음 | **없음** (`set_scanFlag(false)` 후 재진입 → BL스캔 경로) |
| 런타임 영향 | 0건 | **0건** (Activity 미변경, `shipmentType` 여전히 null) |

**발견·정정 — 로그 문자열에 `a.` 접두어가 섞여 들어갔다** (코드검증 지적)

기계 치환이 Log 메시지의 **표시용 문자열 리터럴 안**까지 적용돼 logcat 문구가 원본과 달라졌다.
자체 대조 스크립트는 정규화 단계에서 `a.` 를 제거해 이 차이를 가렸다.

| 대상 | 위치 | 곳수 |
|---|---|:-:|
| `ProductionType` (이번 Step) | `current_work_position` · `work_bl_no` · `work_item_fullbarcode` · `arSM…` 로그 | 8 |
| **개발66 타입 파일 6종** (이미 커밋됨) | `onWeightConfirmed` 의 `centerWorkCount` · `centerWorkWeight` 로그 | 12 |

문자열 리터럴 내부만 원복했다(코드의 `a.` 는 그대로). 수정 후 `ProductionType` 문자열 79개 전부 원본과 일치,
개발66 6종도 `onWeightConfirmed` 문자열이 전부 원본 `wet_data_insert` 에 존재함을 확인했다.

> **동작에는 영향이 없다**(출력 텍스트만 달랐다). 다만 이 문서 Step 5 가 `pp_code` 결과를 **logcat 으로 원본과 대조**하라고 지시하듯 로그는 실기기 테스트의 판정 기준이므로 원본과 같아야 한다.
> **재발 방지** — 이후 Step 의 대조 스크립트는 문자열 리터럴을 따로 떼어 비교한다.

---

### Step 3: 바코드 스캔 이관 — 생산라벨(7) ProductionLabelType

**Part 1. 분석**
- 메서드: 원본 공용 `setBarcodeMsg`(개발66 Step 7에서 삭제됨)
- 범위: `BixolonShipmentActivity_Back.java:1143~1539`(397줄, 백업 파일 기준) → `ProductionLabelType.onBarcodeScanned`
- 용도: 7 전용 본문을 원본 그대로 복원한다
- 주의할 점: 생산(1)과 달리 **접는 조건이 4개 있다**(§4.5). W/HW·B 블록과 킬코이·센터명 판정은 **삭제하지 않는다**

| # | 항목 | 위치(백업 기준) | 내용 |
|---|------|------|------|
| 1 | 접는 조건 | 1198, 1287, 1322, 1436, 1498 | §4.5 표. 전부 거짓 → WholesaleType·LotteType과 동일 패턴 |
| 2 | 유지 | 1278~1296, 1359~1411, 1474~1525 | 킬코이·센터명 판정, `ITEM_TYPE` W/HW·B |
| 3 | 치환 | §4.4와 동일 종류(`ProgressDlgShipSelect` 생성, 재귀, `find_PackerProduct` 계열) | — |

**Part 2. 변환 계획**
- 변환 방식: 개발66 Step 2(WholesaleType)와 동일한 절차 — 원본을 그대로 복사한 뒤 4개 조건만 접는다
- 주의사항: `WholesaleType`·`LotteType`과 결과를 비교했을 때, ITEM_TYPE 4블록을 제외하면 접는 패턴이 같아야 한다. 다르면 조건 판정을 잘못한 것이다

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 — `gradlew assembleDebug` → `BUILD SUCCESSFUL` (2026-09-17)
- [x] Part 5: 단위테스트 — **해당 없음**(Step 8 전까지 미호출)
- [x] Part 6: 회귀테스트 — 원본(`dc3e4d3^`) 대조 결과 접은 조건 5곳 외 차이 0줄, 문자열 리터럴 오염 0건

**Part 6. 변경 내용** (완료):
- **무엇을**: 현재 Activity 에 **없는** 원본 공용 `setBarcodeMsg` 본문을 `dc3e4d3^`(Step 0 직전 커밋)에서 복원해 `ProductionLabelType.onBarcodeScanned` 로 이관
- **왜**: 개발66 Step 7 이 공용 본문을 삭제하면서 7 의 스캔 경로가 사라졌다. 7 을 파일로 유지하려면 원본을 되살려야 한다
- **어떻게**: 7 은 searchType 게이트 5곳이 **전부 거짓**이라 접는 결과가 도매(3)와 같다. 그래서 이미 검증된 `WholesaleType.onBarcodeScanned` 본문을 기준으로 삼고 **주석만** 7 기준으로 바꿨다. 원본과의 직접 대조는 별도로 수행했다

**접은 조건 5곳** (원본 라인 = `dc3e4d3^` 기준)

| 원본 라인 | 조건 | 7 판정 | 처리 |
|---|---|:-:|---|
| 1205 · 1329 | `NONFIXED \|\| HOMEPLUS_NONFIXED` 중복확인 제외 | 거짓 | 블록 삭제(`dup = false;` 포함) |
| 1294 | `EMART` 트레이더스 소비기한 검증 | 거짓 | 내부 블록만 삭제, 바깥 `else if` 골격 유지 |
| 1443 · 1505 | `EMART` LB 환산 자릿수 | 거짓 | `else` 경로만 유지 |

> §4.5 표는 백업 파일(`BixolonShipmentActivity_Back.java`) 기준 라인(1198 · 1287 · 1322 · 1436 · 1498)을 썼다.
> 코드 주석과 이 표는 **커밋 기준**(`dc3e4d3^`)을 쓴다. 두 기준은 7줄 차이가 나며 가리키는 코드는 같다.

**생산(1)과의 차이 — 이 Step 의 핵심**

| 항목 | `ProductionType`(1) | `ProductionLabelType`(7) |
|---|:-:|:-:|
| `ITEM_TYPE` 블록 | S · J **2종** | W/HW · S · J · B **5종 전부** |
| 킬코이(`KILKOY_PACKER_CODE`) · 미트센터 판정 | 없음 | **있음** |
| 센터명(TRD/WET/E·T) 판정 | 없음 | **있음** |

개발60 이 생산(1) 전용 메서드를 만들면서 걷어낸 블록들이 7 에서는 살아 있어야 한다. 검증에서 확인했다.

**검증 결과**

| 항목 | 예상 | 실제 |
|---|---|---|
| 빌드 | — | `BUILD SUCCESSFUL` |
| 원본 대조 | 접은 조건 5곳만 차이 | **삭제 24줄 = 5곳**, 실질 추가 0줄 |
| 문자열 리터럴 | 오염 0건 | **0건** (이관본에만 있는 문자열 0개) |
| `WholesaleType` 과 diff | 주석만 차이 | **주석 6곳뿐**, 실행 코드 차이 0 |
| `ITEM_TYPE` · 킬코이 · 센터명 | 전부 유지 | **전부 유지** |
| 런타임 영향 | 0건 | **0건** (Activity 미변경) |

> **과도기 상태** — `ProductionLabelType` 은 `this.findPackerProduct(msg, 1/2)`(Step 5 스텁)를,
> `ProductionType` 은 아직 `a.find_PackerProduct` 를 호출한다. 둘 다 Step 8 전까지 호출되지 않아 무해하며 **Step 5 에서 통일**한다.

**대조 방식 개선** — Step 2 에서 로그 문자열 오염을 놓친 뒤, 이 Step 부터 대조 스크립트가 **문자열 리터럴을 마스킹해 코드와 따로 비교**한다. 블록이 접히면 문자열 순서가 밀리므로 위치 비교가 아니라 **집합 비교**로 판정한다.

---

### Step 4: 계근 저장 + 라벨 + 조회 후처리 이관 (2종)

**Part 1. 분석**
- 메서드: `wet_data_insert`, `MESSAGE_REPRINT` else 경로, `ProgressDlgShipSelect.onShipmentLoaded`
- 범위: `1533~1680`, `969~981`, (`onShipmentLoaded`는 원본에 생산 전용 분기가 없음)
- 용도: 계근 저장·라벨 출력·조회 후처리 축을 이관
- 주의할 점: **1과 7은 계근 라벨만 다르다**(§1.3 표). INSERT·반올림·재출력 else 분기는 동일

| # | 항목 | 위치 | 1 | 7 |
|---|------|------|---|---|
| 1 | 계근 라벨 | 969~981 안의 `Common.print_bool` 블록(1653~1670) | 분기 없음(출력 안 함) | `SEARCH_TYPE_PRODUCTION_LABEL` → `setPrinting_prod` |
| 2 | 재출력 라벨 | 977 | else 경로(이마트 라벨) | `setPrinting_prod` |
| 3 | `onShipmentLoaded` | 해당 없음 | no-op | no-op |

**Part 2. 변환 계획**
- 변환 방식: 개발66 Step 8과 동일 절차. 1을 먼저, 7을 다음에 이동하고 매번 빌드
- 주의사항: `ProductionType.onWeightConfirmed`에는 라벨 분기가 **아예 없어야 한다**(생산은 계근 시 라벨을 찍지 않는 것이 원본 동작)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 — `gradlew assembleDebug` → `BUILD SUCCESSFUL` (2026-09-17)
- [ ] Part 5: 단위테스트 — Step 8 이후 실기기 확인 필요 (생산 계근 저장 · 생산라벨 출력)
- [ ] Part 6: 회귀테스트 — Step 8 이후 실기기 확인 필요 (비생산 6종 무영향)

**Part 6. 변경 내용** (완료):
- **무엇을**: `wet_data_insert` 잔여 본문 · `MESSAGE_REPRINT` else 경로 · 조회 후처리를 생산 2종의 `onWeightConfirmed` · `reprintLabel` · `onShipmentLoaded` 로 이관
- **왜**: 계근 저장·라벨 축을 타입 파일이 소유하게 한다. 1과 7은 INSERT·반올림이 같고 **라벨만 다르다**
- **어떻게**: 개발66 Step 8과 같은 절차로 접었다. Activity 본문은 **삭제하지 않았다** — Step 8 컷오버 전까지 생산이 그 본문으로 동작해야 한다

**타입별 접기 결과**

| 항목 | 원본 | 생산(1) | 생산라벨(7) |
|---|---|---|---|
| 계근 INSERT | 1566~1582 | else 일반(`insertqueryGoodsWet`) | 동일 |
| 중량 반올림 4곳 | 1588 · 1602 · 1620 · 1630 | else 3자리 | 동일 |
| `lotteBoxOrder` 지역변수 | 1564 | 삭제(롯데 전용) | 삭제 |
| **계근 라벨** | 1653~1670 | **블록 전체 삭제** — 원본 if/else-if 체인에 생산(1) 케이스가 **없다** | **`setPrinting_prod` 만 유지**(원본 1666) |
| **재출력 라벨** | 969~981 | else 경로 `setPrinting`(이마트 수기 프린팅) | `setPrinting_prod`(원본 977) |
| 조회 후처리 | — | no-op | no-op |

> **생산(1)이 계근 시 라벨을 찍지 않는 것이 원본 동작이다.** 원본 라벨 분기는 홈플러스 → 이마트 → 비정량 → 롯데 → 생산라벨 순인데 **생산(1) 케이스가 없다.** 코드검증에서 원본 소스로 확인했다.

**검증 결과**

| 항목 | 예상 | 실제 |
|---|---|---|
| 빌드 | — | `BUILD SUCCESSFUL` |
| 원본 대조 (2종) | 추가 0줄 | **추가 0줄** (삭제 52 / 48줄 — 차이 4줄이 라벨 블록) |
| 두 타입 `onWeightConfirmed` diff | 라벨 블록만 차이 | **라벨 블록 1곳뿐**, 그 외 100% 동일 |
| 문자열 리터럴 오염 | 0건 | **0건** |
| `setPrinting_prod` 인자 | 원본 동일 | **동일** (4개 인자) |
| `setPrinting` 인자(재출력) | 원본 동일 | **동일** (8개 인자 순서까지) |
| 인터페이스 시그니처 | 일치 | **일치** |
| 런타임 영향 | 0건 | **0건** (Activity 미변경, 게이트 3곳 여전히 else 경로) |
| 개발66 6종 | 무영향 | **무영향** (변경 파일은 생산 2종뿐) |

---

### Step 5: 상품 매칭 이관 (2종) + find_work_info 잔류 방식 결정

**Part 1. 분석**
- 메서드: `find_PackerProduct`, `find_PackerProductBarcodeGoods`, `find_work_info`, `find_work_info_barcodeGoods`
- 범위: `1707~1856`
- 용도: 차이 없는 상품 매칭 축을 두 타입에 각각 이관
- 주의할 점: `find_work_info`는 `ProgressDlgShipSelect.onPostExecute`(2177)가 **타입과 무관하게** 직접 호출한다

**find_work_info를 어떻게 남길지 — 선택지 분석**

| 옵션 | 내용 | 판정 |
|---|---|---|
| A. Activity 원본 유지 + 타입 파일에 사본 duplicate | Activity의 `find_work_info`(1742~1808)는 그대로 두고, `ProductionType`·`ProductionLabelType`에 각각 `private` 사본을 만들어 자기 `findPackerProduct` 안에서 쓴다 | **선택.** 개발66 Step 9가 `EmartNonfixedType`에 이미 이 방식을 썼다(§1.4 제약 4). 검증된 전례를 따른다 |
| B. Activity 원본을 위임 구조로 변경 | `find_work_info`를 상태 갱신부와 반환값부로 쪼개 `onPostExecute`와 타입 파일이 공유하게 리팩토링 | **기각.** 문서에 없는 개선이며(AI 제약), `onPostExecute`가 6종 전부에 영향을 주므로 위험이 이 문서의 범위를 넘는다 |

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 이관 | 1707~1722, 1724~1740, 1810~1856 | `ProductionType`·`ProductionLabelType` 각각에 사본 생성. Activity 원본은 이 시점부터 호출처가 사라짐(Step 8 이후) |
| 2 | 유지 | 1742~1808 | `find_work_info`는 Activity에 그대로 둔다(옵션 A) |

**Part 2. 변환 계획**
- 변환 방식: 개발66 Step 9와 동일 — `findPackerProduct(barcode, workFlag)` 하나로 진입, 내부에서 `find_work_info`/`find_work_info_barcodeGoods` 사본을 호출
- 주의사항: 두 메서드(구간 추출 유무 차이)를 합치지 않는다(개발66 §1.3 #2 계승)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 — `gradlew assembleDebug` → `BUILD SUCCESSFUL` (2026-09-17)
- [ ] Part 5: 단위테스트 — Step 8 이후 실기기 확인 필요 (생산 상품 스캔 · 상품코드 스캔)
- [x] Part 6: 회귀테스트 — `ProgressDlgShipSelect.onPostExecute`(2177)가 여전히 Activity 의 `find_work_info` 를 호출함을 확인. 8종 공통 경로 무영향

**Part 6. 변경 내용** (완료):
- **무엇을**: `findPackerProduct` 래퍼 + `find_work_info` · `find_work_info_barcodeGoods` private 사본을 생산 2종에 각각 만들었다. `ProductionType.onBarcodeScanned` 의 호출처도 자기 메서드로 통일했다
- **왜**: 상품 매칭 축을 타입 파일이 소유하게 한다. 생산 2종은 이 축에 차이가 없어 두 파일의 사본이 동일하다
- **어떻게**: **옵션 A** 를 택했다 — Activity 의 `find_work_info` 는 그대로 두고 타입 파일에 사본을 둔다. 개발66 Step 9 가 `EmartNonfixedType` 에 쓴 방식과 같다

**옵션 A 를 지킨 결과** (검증 확인)

| 대상 | 상태 |
|---|---|
| Activity `find_work_info`(1742~1808) | **그대로 유지** — 삭제하지 않았다 |
| Activity `find_work_info` 안의 searchType 4 전부 매칭 블록(1786~1798) | **유지** — 비정량(4)이 이 경로로도 동작한다 |
| `ProgressDlgShipSelect.onPostExecute`(2177) | **Activity 메서드를 그대로 호출** — 8종 공통 경로라 타입 파일 사본과 무관 |

**검증 결과**

| 항목 | 예상 | 실제 |
|---|---|---|
| 빌드 | — | `BUILD SUCCESSFUL` |
| `find_work_info` 사본 (2종) | 전부 매칭 13줄만 삭제 | **삭제 13줄 / 추가 0줄** |
| `find_work_info_barcodeGoods` 사본 (2종) | 차이 없음 | **차이 0줄** (두 메서드 병합하지 않음 — 구간 추출 길이 체크 유무 유지) |
| `findPackerProduct` 래퍼 | 원본 두 메서드 로직 누락 0 | **누락 0** (항상 참인 `Editable` 비교 포함) |
| 생산 2종 상호 비교 | 3개 메서드 동일 | **완전 일치** |
| 문자열 리터럴 오염 | 0건 | **0건** |
| 개발66 6종 | 무영향 | **무영향** |
| 런타임 영향 | 0건 | **0건** (Activity 미변경, Step 8 전까지 미호출) |

**호출처 통일** — Step 3 에서 `ProductionLabelType` 만 `this.findPackerProduct` 를 쓰고 `ProductionType` 은 `a.find_PackerProduct` 를 쓰던 과도기 불일치를 이 Step 에서 해소했다. stale 해진 주석·Javadoc 참조 3곳도 함께 정리했다.

---

### Step 6: 전송 이관 (2종)

**Part 1. 분석**
- 메서드: `ProgressDlgShipmentSend.doInBackground`
- 범위: 일괄 분기(2412~2515) 중 `PRODUCTION`·`PRODUCTION_LABEL` 경로
- 용도: 생산 2종의 전송을 `send(...)`로 이관
- 주의할 점: 2412의 5중 조건(`PRODUCTION || WHOLESALE || NONFIXED || HOMEPLUS_NONFIXED || PRODUCTION_LABEL`)은 **건드리지 않는다.** 도매·비정량 2종은 이미 자기 `send`에 위임되어 이 분기에 도달하지 않는다(§5.4)

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 이관 | 2412~2515 | 패킷 조립(2420~2433)은 5개 타입 공용이지만, **판단으로 공통화하지 않고** 두 타입에 각각 복사(개발66 Step 12 방침 계승 — 이동 0건 판정과 동일 기준) |
| 2 | URL 분기 | 2458~2460 | `PRODUCTION || PRODUCTION_LABEL` → `URL_INSERT_GOODS_WET_PRODUCTION`. 이 조건만 좁혀서 유지 |

**Part 2. 변환 계획**
- 변환 방식: 개발66 Step 10과 동일. `send(...)`는 조회 이후만 담당, `publishProgress`는 no-op이라 이관하지 않는다(개발66 §8 Step 10 판정 계승)
- 주의사항: 전송은 되돌릴 수 없다. 소량 데이터로 먼저 확인

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 — Step 8 이후, 소량 데이터로 전송 확인
- [ ] Part 6: 회귀테스트 — 비생산 6종 전송 무영향

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 7: 수기 입력 이관 (2종)

**Part 1. 분석**
- 메서드: `inputBtnListener`의 `work_flag == 0` 분기
- 범위: else 본문(672~751)
- 용도: 수기 입력 축 이관
- 주의할 점: 킬코이·센터명 판정은 데이터 의존이라 접지 않는다(§1.5)

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 절사 없음 | 683(else 경로) | `Double.toString`, 그대로 유지 |
| 2 | 소비기한 창 | 726(바깥), 727(안쪽) | 바깥 CENTERNAME 판정 유지, 안쪽 `EMART \|\| LOTTE` 조건은 생산 2종에서 거짓 → else(746·749) 경로만 남긴다 |

**Part 2. 변환 계획**
- 변환 방식: 개발66 Step 11과 동일. 입력값 검증(655~663)은 Activity에 남기고 통과 후 위임
- 주의사항: 726의 `|| SEARCH_TYPE_LOTTE`는 바깥 진입 조건이고 727의 `EMART || LOTTE`는 안쪽 조건이다. 함께 접지 않는다(개발66 §8 Step 11 원칙 계승)

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 — Step 8 이후 실기기 확인 필요
- [ ] Part 6: 회귀테스트 — 킬코이·미트센터 상품 수기 입력, 비생산 6종 무영향

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 8: 컷오버 — onCreate + setBarcodeMsg 전환 (최우선 검증 대상)

**Part 1. 분석**
- 메서드: `onCreate`, `setBarcodeMsg`, `setBarcodeMsgProduction`
- 범위: `439~442`, `1178~1477`
- 용도: Step 1~7에서 완성한 두 타입을 실제로 연결한다
- 주의할 점: **Step 1~7이 전부 끝나야 착수 가능하다.** 하나라도 스텁이 남아 있으면 즉시 크래시

| # | 항목 | 내용 |
|---|------|------|
| 1 | `onCreate` | 생산 제외 조건 삭제(§4.2) |
| 2 | `setBarcodeMsg` | 생산 분기 삭제, 무조건 위임으로 통일(§4.3) |
| 3 | `setBarcodeMsgProduction` | 삭제(1218~1477, 260줄) |

**Part 2. 변환 계획**
- 변환 방식: 3개 변경을 한 커밋으로 묶는다
- 주의사항: 삭제 직후 `find_PackerProduct`·`find_PackerProductBarcodeGoods`·`find_work_info_barcodeGoods`(Activity 원본)가 호출처 0건이 된다. **이 Step에서는 삭제하지 않는다**(Step 9로 분리)

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 — **필수**: 생산(1) 전체 흐름(스캔→계근→전송) 실기기 확인
- [ ] Part 6: 회귀테스트 — **필수**: 비생산 6종 전체 흐름 무영향 확인(위임 게이트 5곳이 처음으로 8종 전부에 대해 참이 되는 지점)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 9: 위임 게이트 단순화 + 잔여 죽은 코드 정리 (선택 — 사용자 승인 필요)

**Part 1. 분석**
- 메서드: 위임 게이트 5곳, `find_PackerProduct`·`find_PackerProductBarcodeGoods`·`find_work_info_barcodeGoods`(Activity 원본)
- 범위: `1526~1531`, `962~967`, `667~670`, `2142~2144`, `2326~2330`, `1707~1740`, `1810~1856`
- 용도: 8종 전부 `shipmentType != null`이 항상 참이 되므로 가드를 제거해 코드를 단순화한다. **이 Step은 필수가 아니다**
- 주의할 점: 개발66 Step 0과 동일하게 **사용자 승인 없이 진행하지 않는다**

| # | 항목 | 내용 |
|---|------|------|
| 1 | 가드 단순화 | `if (shipmentType != null) { X; return; } Y;` → `return X;` 형태로. else 본문(Y)은 이제 도달 불가능하므로 삭제 |
| 2 | 죽은 메서드 정리 | Step 8 이후 호출처가 0건이 된 3개 메서드 삭제 여부 결정 |

**Part 2. 변환 계획**
- 변환 방식: 게이트 5곳을 하나씩 단순화하고 매번 빌드
- 주의사항: 가드를 제거하면 `shipmentType`이 이론상 `null`일 수 있는 경로(예: `Factory.create`가 예외를 던지는 미지의 searchType)에서 NPE 대신 `IllegalArgumentException`이 `onCreate`에서 먼저 발생하는 것으로 대체된다는 점을 확인한다

**체크리스트**
- [ ] Part 1: 분석 완료 확인 — **사용자 승인 대기**
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트 — 8종 전체 회귀

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 10: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 생산(1) 계근대상 다운로드 → 상품 스캔 → BL 스캔 → 중량 추출(S·J) | □ |
| 2 | 생산(1) 계근 완료 → 라벨 출력 없음(원본 동작) | □ |
| 3 | 생산(1) 상세 팝업 재출력 → 이마트 라벨(`setPrinting`) 출력 | □ |
| 4 | 생산(1) 전송 → `insert_goods_wet_production.jsp` 적재 | □ |
| 5 | 생산(1) 수기 입력 → 절사 없음, 소비기한 창 미노출(CENTERNAME 미해당) | □ |
| 6 | 생산라벨(7) — 현재 진입 경로 없음을 재확인(`TableRow` gone 유지) | □ |
| 7 | 생산라벨(7) 코드 검증 — `ProductionLabelType`이 W/HW·B 블록을 포함하는지 정적 확인(실기기 불가) | □ |
| 8 | 이마트(0)·홈플러스(2)·도매(3)·이마트비정량(4)·홈플비정량(5)·롯데(6) 전체 회귀 | □ |
| 9 | `git diff`로 개발66이 만든 6개 타입 파일에 변경 0줄 확인 | □ |

---

### 개발 순서 요약

```
Step 1: 골격 준비 (ProductionType·ProductionLabelType 스텁 + Factory case)
    ↓
Step 2: 바코드 스캔 — 생산(1)
    ↓
Step 3: 바코드 스캔 — 생산라벨(7) (백업 파일에서 복원)
    ↓
Step 4: 계근 저장 + 라벨 + 조회후처리 (2종)
    ↓
Step 5: 상품 매칭 (2종) + find_work_info 잔류 방식 확정
    ↓
Step 6: 전송 (2종)
    ↓
Step 7: 수기 입력 (2종)
    ↓
Step 8: 컷오버 — onCreate + setBarcodeMsg 전환 (최우선 검증)
    ↓
Step 9: 위임 게이트 단순화 + 죽은 코드 정리 (선택, 승인 필요)
    ↓
Step 10: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 골격 단계 무영향 확인 (Step 1~7 공통)

```
1. 생산(1)으로 로그인 → 계근대상 다운로드
2. 바코드 스캔 → logcat "setBarcodeMsgProduction 시작" 출력 확인 (여전히 기존 경로)
3. git diff 확인 → BixolonShipmentActivity.java에 변경 0줄 (Step 1~7은 신규 파일만 추가)
```

### 시나리오 2: 컷오버 직후 생산 회귀 (Step 8 후 필수)

```
1. 생산(1)으로 로그인 → 계근대상 다운로드 → 상품 스캔 → BL 스캔
2. logcat에서 "ProductionType" 태그로 로그가 찍히는지 확인 (더 이상 "setBarcodeMsgProduction"이 아님)
3. 계근 완료 → 중량 3자리 반올림, 라벨 미출력 확인
4. 상세 팝업 재출력 → 이마트 라벨 출력 확인
5. 전송 → insert_goods_wet_production.jsp 적재 확인
```

### 시나리오 3: 비생산 6종 무영향 확인 (Step 8 후 필수)

```
1. 이마트(0)·홈플러스(2)·도매(3)·이마트비정량(4)·홈플비정량(5)·롯데(6) 각각 스캔→계근→전송
2. git diff 확인 → 개발66이 만든 6개 타입 파일에 변경 0줄
```

### 시나리오 4: 생산라벨(7) 정적 검증 (실기기 불가)

```
1. activity_main.xml의 생산라벨 버튼 TableRow가 여전히 gone인지 확인
2. ProductionLabelType.java를 열어 ITEM_TYPE W/HW·B 블록이 있는지, 접은 조건 4곳의 주석이 정확한지 확인
3. 코드 검증 에이전트로 ProductionLabelType과 WholesaleType의 폴딩 패턴이 동일한지 대조
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | Step 1~7 중 실수로 onCreate를 먼저 고침 | 습관적으로 개발66 순서(먼저 생성)를 따라감 | 이 문서의 추가 제약 1을 재확인. onCreate는 Step 8에서만 수정 |
| 2 | 생산(1)에 W/HW·B 블록이 실수로 들어감 | `ProductionLabelType`과 혼동 | §4.4·§4.5 표로 두 타입의 ITEM_TYPE 구성을 항상 대조 |
| 3 | 생산라벨(7)에 킬코이·센터명 판정을 빠뜨림 | 개발60이 생산(1)에서 제거한 것을 7에도 적용하는 착각 | §1.3, §4.5의 "유지" 열을 기준으로 삼는다 |
| 4 | `find_work_info`를 Activity에서 삭제 | "호출처가 줄었으니 정리하자"는 임의 판단 | §1.4 제약 4, §8 Step 5의 옵션 A 유지. `ProgressDlgShipSelect.onPostExecute`가 여전히 호출한다 |
| 5 | Step 8에서 부분 적용(onCreate만 또는 setBarcodeMsg만 변경) | 커밋을 나누고 싶은 습관 | §5.1. 반드시 한 커밋으로 묶는다 |
| 6 | 도매·비정량이 생산 전송 분기(2412)에 다시 걸림 | 조건을 좁히다가 실수로 범위를 넓힘 | §5.4, §8 Step 6. 2412 바깥 조건은 건드리지 않고 안쪽 URL 분기만 좁힌다 |
| 7 | Step 9를 승인 없이 진행 | "이미 필요없어 보이니 지우자" | §8 Step 9. 개발66 Step 0과 동일하게 승인 게이트를 둔다 |
| 8 | 생산라벨(7) 실기기 테스트를 시도 | 진입 경로가 없다는 사실을 잊음 | §1.4, §9 시나리오 4. 정적 검증으로 대체 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | 골격 준비 (스텁 2개 + Factory case) | ✅ 완료 (2026-09-17, 신규 2 · Factory case 2 · Javadoc 정정 · 실행 경로 변화 0건 · 빌드 통과) |
| 2 | 바코드 스캔 — 생산(1) ProductionType | ✅ 완료 (2026-09-17, 접은 조건 0 · 원본 대조 차이 0줄 · 로그 문자열 정정 · 빌드 통과) |
| 3 | 바코드 스캔 — 생산라벨(7) ProductionLabelType | ✅ 완료 (2026-09-17, 원본 복원 · 접은 조건 5곳 · W/HW·B 및 킬코이·센터명 유지 · 빌드 통과) |
| 4 | 계근 저장 + 라벨 + 조회후처리 (2종) | ✅ 완료 (2026-09-17, 추가 0줄 · 라벨만 차이 · 생산(1) 라벨 없음 확인 · 빌드 통과) |
| 5 | 상품 매칭 (2종) + find_work_info 잔류 확정 | ✅ 완료 (2026-09-17, 옵션 A · 추가 0줄 · 2종 사본 동일 · 공통 경로 무영향 · 빌드 통과) |
| 6 | 전송 (2종) | ⏳ 대기 |
| 7 | 수기 입력 (2종) | ⏳ 대기 |
| 8 | 컷오버 — onCreate + setBarcodeMsg 전환 | ⏳ 대기 |
| 9 | 위임 게이트 단순화 + 죽은 코드 정리 (선택) | ⏳ 대기 |
| 10 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/개발/66_BixolonShipmentActivity_searchType별_클래스_분리.md` — 이 문서가 계승하는 구조·용어·검증 절차의 원본. `ShipmentType` 인터페이스, `ShipmentTypeFactory`, 위임 게이트 5곳, `EmartNonfixedType`의 `find_work_info` 사본 전례(§1.4 제약 4의 근거)
- `app/doc/개발/60_setBarcodeMsg_생산1_전용메서드_분리.md` — `setBarcodeMsgProduction` 신설 이력. 생산(1)이 W/HW·B·킬코이·센터명·비정량 분기를 제거한 근거(§1.3, §4.4)
- `app/doc/오류/28_setBarcodeMsg_디바운스_바코드값_미비교_연속스캔_삼킴.md` — 디바운스 로직(재귀 시 `lastBarcodeProcessedTime = 0`) 근거
- `app/doc/기능/11_바코드스캔후_전체프로세스.md`
- `app/doc/참고자료/오류패턴_분석.md`

---

**문서 버전**: 1.0
