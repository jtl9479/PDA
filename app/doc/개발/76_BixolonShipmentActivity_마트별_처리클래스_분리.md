# BixolonShipmentActivity 마트(searchType)별 분기를 마트별 처리 클래스(ShipmentMode)로 분리

**작성일**: 2026-10-07
**목적**: `BixolonShipmentActivity.java`(3091줄) 곳곳에 흩어진 `Common.searchType` 분기(마트별 판단·호출)를 `ShipmentMode` 인터페이스 구현 클래스(마트별 1개)로 이동한다. Activity 는 화면·공통 흐름만 담당하고 마트별 판단·호출은 `mode.xxx()` 로만 접근한다. 신규 마트/바코드 정책 추가 시 수정 위치를 "새 Mode 클래스 + 팩토리 1줄"로 모으는 것이 목적이다 (프로젝트 목적 4 "소스 정리"). **동작은 변경 전과 100% 동일**해야 한다 (구조 변경만).

> **사용자 결정(2026-10-07)**: A안(마트별 처리 클래스/전략 패턴). 분리 단위 = 이마트(0), 이마트비정량(4), 도매(3), 홈플러스(2), 홈플러스비정량(5), 롯데(6), 생산.
> **착수 조건**: 개발 74·75 실기기 테스트 완료 후. 기준 코드는 **개발 74(일괄전송 조기종료 제거)가 반영된 현재 코드**(2026-10-07 시점 `BixolonShipmentActivity.java` 3091줄).
> **범위 밖**: `ProgressDlgShipSearch`·`ProgressDlgBarcodeSearch`·`ProgressDlgGoodsWetSearch`·`DBHandler`·`MainActivity`·`ShipmentListAdapter`·`ProductionActivity`·`ShipmentActivity`(구버전)·`LabelPrintHelper` 내부의 searchType 분기는 이번 대상이 아니다.

---

## ⚠ 구현 결정 (2026-10-07, 사용자)

- **기본 클래스(DefaultShipmentMode) 없이 마트별 전부 구현으로 시작**: 각 Mode 클래스가 19개 메서드를 모두 직접 구현한다. 공통 부분을 기본 클래스로 묶는 작업은 **분리·검증이 끝난 뒤 마지막 단계**로 진행한다(사용자 지시). 아래 2~4절의 DefaultShipmentMode 상속 설계는 그 마지막 단계의 목표 구조다.
- 미등록 searchType 은 `UnregisteredMode` (기존 else 경로 동작)가 담당한다 (DefaultShipmentMode 대신).
- 생산은 `ProductionMode`(1) / `ProductionLabelMode`(7) 분리 (권장안 채택).
- 일괄 전송 중복 로그 4줄(13절 #7)은 유지.
- 전송(N, Step 4): 사용자 승인으로 **A안(정리)** 채택 — 건별/일괄 바깥 조건을 mode.getSendType(), 안쪽 URL 체인을 mode.getSendUrl() 로 치환. 도달 불가 분기(건별 안 도매·생산·생산라벨 URL, 일괄 안 이마트 sendData·홈플러스 URL)는 이관하지 않음.

### 구현 결과 (Step 1~3)
- `shipment/mode/`: ShipmentMode(인터페이스·SendType), ShipmentModeFactory, EmartMode·EmartNonfixedMode·WholesaleMode·HomeplusMode·HomeplusNonfixedMode·LotteMode·ProductionMode·ProductionLabelMode·UnregisteredMode (각 19개 메서드 전부 구현)
- BixolonShipmentActivity: 전송(N)을 제외한 분기 A1·A2·A3·B·C·D·E·F1·F2·G·H1·H2·I·J·K1~K4·L·M 을 mode 호출로 치환, `lotte_TryCount` 필드 → LotteMode 이동. 남은 searchType 분기는 전송(ProgressDlgShipmentSend)뿐.
- 검증: 컴파일 성공, code-verifier PASS (searchType 0~7·미등록·null 전 지점 HEAD 동일, 19개 메서드 매트릭스 일치, 허용 차이: "chk prod 계근중량" 로그가 setGI_QTY 직전으로 이동).

### 구현 결과 (Step 4)
- ProgressDlgShipmentSend: `if (mode.getSendType() == PER_ITEM)` / `else if (== BATCH)`, 전송은 `HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", mode.getSendUrl())`.
- Activity 에 남은 searchType 참조: onCreate 의 `ShipmentModeFactory.create(Common.searchType)` 1곳뿐 (마트 분기 0).
- 검증: 컴파일 성공, code-verifier PASS — 0~7·미등록 진입 블록·HttpHelper 메서드·URL 이 HEAD 실제 도달 경로와 동일, 제거 분기는 바깥 조건 배타성으로 도달 불가 증명, packet·결과처리·개발74 jChk 무변경.
- 차이: 로그만 — 일괄 "send packet 확인" 로그 1줄로 통일, 도매 전용 "여기로 들어옴" 로그 1줄 제거.

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

추가 제약(본 문서): 람다 등 새 문법 도입 금지(익명 클래스·일반 클래스·if/else 만 사용). 이동하는 코드의 값·순서·문자열·좌표는 바꾸지 않는다. Toast 문구·호출 순서 유지, Log 문구는 유지(예외는 13절 #7 승인 항목 1건). 출력 바이트·SQLite 저장값·서버 전송 packet/URL 은 변경 전과 동일. `searchType` 분기가 아닌 조건(킬코이·미트센터·센터명 문자열 등)은 Activity 에 그대로 둔다.

---

## 1. 현재 구조

### 1.1 대상 파일

`app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java` (3091줄). `Common.searchType` 값 상수(`common/Common.java` 58~66행):

| 상수 | 값 | 의미 | 비고 |
|------|:--:|------|------|
| `SEARCH_TYPE_EMART` | "0" | 이마트 출하 | |
| `SEARCH_TYPE_PRODUCTION` | "1" | 생산 계근(이노이천) | 개발/테스트 드롭 방침(메모), 코드는 존재 |
| `SEARCH_TYPE_HOMEPLUS` | "2" | 홈플러스 출하 | |
| `SEARCH_TYPE_WHOLESALE` | "3" | 도매 출하 | |
| `SEARCH_TYPE_NONFIXED` | "4" | 비정량 출하(이마트 비정량) | |
| `SEARCH_TYPE_HOMEPLUS_NONFIXED` | "5" | 홈플러스 비정량 | |
| `SEARCH_TYPE_LOTTE` | "6" | 롯데 출하 | |
| `SEARCH_TYPE_PRODUCTION_LABEL` | "7" | 생산 라벨 | Common 주석 "미사용", 단 `MainActivity` 303·349행에 다운로드/계근 진입 코드 존재 |

`Common.searchType` 대입 위치는 전체 소스에서 `Common.java:54`(초기값 "0"), `MainActivity.java:259`·`:471` 3곳뿐이다 (grep 확인). `BixolonShipmentActivity` 가 살아있는 동안 값이 바뀌는 경로는 없다.

### 1.2 searchType 분기 전수 (grep `searchType|SEARCH_TYPE_` 결과 58행 = 아래 분기 지점 + 주석/로그)

주석(53·54·332·1091·1492행)과 로그 출력(324·2811·2893행)은 분기가 아니다. 문자열 리터럴 `"0"~"7"` 직접 비교(`equals("N")`)는 이 파일에 **0건**(grep 확인). `switch` 에 searchType 를 쓰는 곳도 0건. 분기 지점은 아래 표의 **24개 행**(A1~N4)이다.

| # | 위치(행) | 메서드 | 현재 코드(요지) | 처리 구분 |
|:-:|:--------:|--------|-----------------|-----------|
| A1 | 333 | onCreate | `if(searchType == WHOLESALE) setContentView(activity_shipment_wholesale) else activity_shipment` | 판단 |
| A2 | 426 | onCreate | `if(searchType == PRODUCTION){ swt_print.setChecked(false); swt_print.setClickable(false); Common.print_bool=false; }` | 판단 |
| A3 | 465, 470 | onStart | `!isEnabled() && searchType != PRODUCTION` / `Common.printer_setting && searchType != PRODUCTION` | 판단 |
| B | 666~675 | inputBtnListener(수기 입력) | 이마트만 `floor(w*10)/10` + `%.1f`, 그 외 `Double.toString` | 계산 |
| C | 689~697 | inputBtnListener | 킬코이/미트센터 외: 수입센터명(TRD/WET/ET) 또는 롯데이면 → (이마트\|\|롯데) 소비기한 입력창, 아니면 저장 | 판단 |
| D | 970~981 | mBixolonHandler MESSAGE_REPRINT | 홈플/홈플비정량 → setHomeplusPrinting, 롯데 → setPrintingLotte(+BOX_ORDER), 생산라벨 → setPrinting_prod, 그 외 → setPrinting | 동작 |
| E | 1092 | setBarcodeMsg | `searchType == PRODUCTION` 이면 `setBarcodeMsgProduction(msg); return;` | 판단 |
| F1 | 1148 | setBarcodeMsg(상품 스캔) | 비정량(4)·홈플비정량(5)이면 `dup = false` | 판단 |
| F2 | 1272 | setBarcodeMsg(BL 스캔) | 비정량(4)·홈플비정량(5)이면 `dup = false` | 판단 |
| G | 1237 | setBarcodeMsg | 트레이더스/수입센터(센터명 조건) 중 **이마트(0)만** 소비기한 정보 필수 검사 | 판단 |
| H1 | 1386 | setBarcodeMsg(ITEM_TYPE S, LB 환산) | 이마트 `floor(kg*pow)/pow`, 그 외 `floor(kg*100)/100` | 계산 |
| H2 | 1448 | setBarcodeMsg(ITEM_TYPE B, LB 환산) | 위와 동일 | 계산 |
| I | 1836 | find_work_info | 비정량(4)이면 매칭 여부와 무관하게 해당 bi 를 `work_item_bi_info`/패커상품코드로 채택 | 판단 |
| J | 1967~1983 | wet_data_insert | 홈플(2): `selectMaxBoxOrder`+`insertqueryGoodsWetHomeplus`, 롯데(6): `lotte_TryCount` 확정+`insertqueryGoodsWetLotte`+카운터 증가·순환, 그 외: `insertqueryGoodsWet` | 동작 |
| K1 | 1989~1997 | wet_data_insert | 이마트: floor+`%.1f`, 그 외 `Double.toString` | 계산 |
| K2 | 2003~2014 | wet_data_insert | 이마트: `round((GI_QTY+w)*10)/10`, 그 외 `round(v3*1000)/1000` | 계산 |
| K3 | 2021~2025 | wet_data_insert | 이마트: `round(x*100)/100`, 그 외 `round(x*1000)/1000` | 계산 |
| K4 | 2031~2035 | wet_data_insert | 이마트: `round(total*10)/10.0 + " / " + work`, 그 외 `total + " / " + work` | 계산 |
| L | 2055~2070 | wet_data_insert | `Common.print_bool` 일 때 홈플/홈플비정량 → setHomeplusPrinting, 0 → setPrinting, 4 → setPrinting, 6 → setPrintingLotte, 7 → setPrinting_prod, **1·3 은 출력 없음** | 동작 |
| M | 2612~2627 | ProgressDlgShipSelect.doInBackground | 롯데만 `lotte_TryCount = LAST_BOX_ORDER+1 + Σ PACKING_QTY` (1~9999 순환) | 동작 |
| N1 | 2785 | ProgressDlgShipmentSend | `0 \|\| 2 \|\| 6` → **건별 전송** | 판단 |
| N2 | 2814~2824 | (건별) URL 선택 | 0/3→`URL_INSERT_GOODS_WET`, 2→`..._HOMEPLUS`, 6→`..._LOTTE`, 1/7→`URL_INSERT_GOODS_WET` | 판단 |
| N3 | 2864 | ProgressDlgShipmentSend | `1 \|\| 3 \|\| 4 \|\| 5 \|\| 7` → **일괄 전송** | 판단 |
| N4 | 2907~2921 | (일괄) URL 선택 | 0→`sendData(URL_INSERT_GOODS_WET)`, 2→`..._HOMEPLUS`, 1/7→`URL_INSERT_GOODS_WET_PRODUCTION`, 4/5→`URL_INSERT_GOODS_WET_NEW`, 3→`URL_INSERT_GOODS_WET_NEW` | 판단 |

> 조사 요청 목록 대비: 요청서의 8개 묶음은 위 A~N 에 모두 포함된다. 추가 확인된 지점은 **A3 의 465행**(블루투스 확인), **G(1237)가 센터명 조건 안의 searchType 분기**라는 점, **F1(1148)** 이 BL 스캔 전 상품 스캔 단계에도 있다는 점이다. 누락 분기 없음(`searchType`·`SEARCH_TYPE_`·`equals("0"~"7")` 전수 grep 완료).

### 1.3 현재 코드 발췌 (대표)

```java
// 666~675 (B)
if (Common.searchType.equals(Common.SEARCH_TYPE_EMART)) { //이마트 출하대상일경우
    weight_double = Math.floor(weight_double * 10);
    ...
    temp_weight = String.format("%.1f", weight_double);
} else { //이노 생산계근 or 홈플러스 추가계근일경우
    temp_weight = Double.toString(weight_double);
    ...
}
```

```java
// 2785 (N1) / 2864 (N3)
if(Common.searchType.equals(Common.SEARCH_TYPE_EMART) || Common.searchType.equals(Common.SEARCH_TYPE_HOMEPLUS) || Common.searchType.equals(Common.SEARCH_TYPE_LOTTE)){ ... 건별 }
else if(Common.searchType.equals(Common.SEARCH_TYPE_PRODUCTION) || ...WHOLESALE || ...NONFIXED || ...HOMEPLUS_NONFIXED || ...PRODUCTION_LABEL){ ... 일괄 }
```

### 1.4 마트별 동작 매트릭스

열: 이마트(0) / 이마트비정량(4) / 도매(3) / 홈플(2) / 홈플비정량(5) / 롯데(6) / 생산(1) / 생산라벨(7).

| 분기 | 0 이마트 | 4 비정량 | 3 도매 | 2 홈플 | 5 홈플비정량 | 6 롯데 | 1 생산 | 7 생산라벨 |
|------|:-------:|:-------:|:-----:|:-----:|:-----------:|:-----:|:-----:|:--------:|
| A1 레이아웃 | 일반 | 일반 | **도매 전용** | 일반 | 일반 | 일반 | 일반 | 일반 |
| A2 인쇄스위치 해제 | - | - | - | - | - | - | **해제** | - |
| A3 BT/프린터 확인 | 함 | 함 | 함 | 함 | 함 | 함 | **생략** | 함 |
| B 수기 중량 문자열 | **floor+%.1f** | toString | toString | toString | toString | toString | toString | toString |
| C 수기 소비기한창 | 수입센터명일 때 | 안 띄움 | 안 띄움 | 안 띄움 | 안 띄움 | **항상** | 안 띄움 | 안 띄움 |
| D 재출력 | setPrinting | setPrinting | setPrinting | setHomeplusPrinting | setHomeplusPrinting | setPrintingLotte(BOX_ORDER) | setPrinting | **setPrinting_prod** |
| E 바코드 처리 | 공통 | 공통 | 공통 | 공통 | 공통 | 공통 | **setBarcodeMsgProduction 위임** | 공통 |
| F 중복체크 제외 | - | **제외** | - | - | **제외** | - | - | - |
| G 트레이더스 소비기한 필수 | **필수** | - | - | - | - | - | - | - |
| H LB→KG 자릿수 | **pow 자릿수** | 2자리 | 2자리 | 2자리 | 2자리 | 2자리 | 2자리 | 2자리 |
| I find_work_info 채택 | 일치분 | **전체 채택** | 일치분 | 일치분 | 일치분 | 일치분 | 일치분 | 일치분 |
| J 로컬 저장 | insertqueryGoodsWet | 〃 | 〃 | **Homeplus(maxBoxOrder)** | **insertqueryGoodsWet(기본)** | **Lotte(TryCount)** | 〃 | 〃 |
| K 계근중량 계산 | **이마트 규칙** | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 |
| L 저장 직후 라벨 | setPrinting | setPrinting | 없음 | setHomeplusPrinting | setHomeplusPrinting | setPrintingLotte | 없음 | setPrinting_prod |
| M 목록 로드 후 | - | - | - | - | - | **박스순번 초기화** | - | - |
| N 전송 방식 | 건별 | 일괄 | 일괄 | 건별 | 일괄 | 건별 | 일괄 | 일괄 |
| N URL | `URL_INSERT_GOODS_WET` | `..._NEW` | `..._NEW` | `..._HOMEPLUS` | `..._NEW` | `..._LOTTE` | `..._PRODUCTION` | `..._PRODUCTION` |

핵심 관찰:
- 홈플러스비정량(5)은 **저장은 기본(insertqueryGoodsWet)**, 라벨은 홈플러스(setHomeplusPrinting), 전송은 비정량 URL — 홈플러스(2)와 갈라지는 지점이 3곳(J, N 방식, N URL)이다.
- 이마트비정량(4)은 K(계근중량 계산)가 이마트가 아니라 **기본 규칙**이며, 라벨은 setPrinting(이마트와 동일 호출)이다.
- 생산(1)은 `setBarcodeMsgProduction`(1498행~) 안에서도 `wet_data_insert`(1745행)를 호출하므로 J·K 를 탄다. L 은 `Common.print_bool=false`(A2) + 분기에 1 이 없어 출력 없음.

### 문제점

- 마트 판단이 24개 지점에 흩어져, 신규 마트·정책 추가 시 `||` 조합을 전 지점에서 찾아 수정해야 한다(예: 홈플러스비정량 추가 때 D·F·L·N 각각 수정).
- 홈플러스(2)와 홈플러스비정량(5)처럼 "일부만 같은" 마트 조합이 `||` 로 흩어져, 어떤 마트가 어떤 동작인지 매트릭스 없이는 파악이 어렵다.
- N2/N4 에는 도달 불가능한 분기(건별 안의 도매·생산 URL, 일괄 안의 이마트·홈플 URL)가 남아 있어 혼동을 유발한다(5.3 참조).
- Activity 가 라벨·저장·전송 정책까지 알고 있어 화면 책임과 마트 정책이 섞여 있다.

---

## 2. 변경 구조

### 2.1 패키지·클래스 구성

패키지: `com.rgbsolution.highland_emart.shipment.mode` (신규, 경로 `app/src/main/java/com/rgbsolution/highland_emart/shipment/mode/`)

```
shipment/mode/
  ShipmentMode.java            interface — 마트별 판단·계산·동작 19개 메서드 (3절)
  DefaultShipmentMode.java     concrete — "그 외(기본)" 동작 구현. 미지원/알 수 없는 searchType 의 동작도 이 클래스
  EmartMode.java               searchType "0"   extends DefaultShipmentMode
  EmartNonfixedMode.java       searchType "4"   extends DefaultShipmentMode
  WholesaleMode.java           searchType "3"   extends DefaultShipmentMode
  HomeplusMode.java            searchType "2"   extends DefaultShipmentMode
  HomeplusNonfixedMode.java    searchType "5"   extends DefaultShipmentMode
  LotteMode.java               searchType "6"   extends DefaultShipmentMode
  ProductionMode.java          searchType "1"   extends DefaultShipmentMode
  ProductionLabelMode.java     searchType "7"   extends DefaultShipmentMode
  ShipmentModeFactory.java     static ShipmentMode create(String searchType)
```

- 개발 75(`print/label/`)의 "인터페이스 + 마트별 클래스 + 미등록 클래스" 구조와 같은 방식이다.
- **DefaultShipmentMode 를 두는 이유**: 현재 코드의 `else`(그 외) 경로가 마트 7종 중 대부분에 공통이다(예: 계근중량 계산은 이마트만 다르고 나머지 6종 동일). 7개 클래스에 같은 기본 구현을 복붙하면 한 곳을 고칠 때 6곳을 고쳐야 하고 복붙 오류 위험이 크다. 기본 구현을 한 곳에 두고 마트 클래스는 **자기가 다른 메서드만 override** 한다. 각 클래스가 override 하는 목록은 4절 표가 정본이다.
  - 대안(기각): 7개 클래스 전부 19개 메서드를 전부 구현. 클래스 하나만 읽으면 마트 동작이 다 보이는 장점이 있으나 중복이 133개 메서드에 이른다. 사용자가 이 방식을 원하면 Step 1 에서 "기본 구현을 각 클래스에 복사" 로 바꿀 수 있다(동작 동일, 결정 사항 13절 #1).
- 알 수 없는 searchType(예: "8") → `DefaultShipmentMode`: 현재 코드의 else 경로와 동일하게 동작(레이아웃 일반, 프린터 확인함, 기본 계산, 재출력 setPrinting, 저장 insertqueryGoodsWet, 라벨 없음, 전송 없음(`result` 가 ""인 채 반환)). 상세는 5절.
- `null`: 변경 전 `Common.searchType.equals(...)`(333행)에서 NPE. 팩토리가 `searchType.equals(...)` 체인을 쓰므로 `create(null)` 도 **NPE 로 동일**하게 실패한다(onCreate 같은 시점, `setContentView` 이전). 별도 null 처리를 추가하지 않는다(동작 불변).

### 2.2 생산(1)·생산라벨(7): 분리 vs 묶음 비교 → **권장: 분리(ProductionMode + ProductionLabelMode)**

| 구분 | 생산(1) | 생산라벨(7) |
|------|---------|-------------|
| A2 인쇄스위치 해제 | 해제 | 없음 |
| A3 BT/프린터 확인 | 생략 | 함 |
| E setBarcodeMsgProduction 위임 | 위임 | 공통 흐름 |
| D 재출력 | setPrinting(기본) | setPrinting_prod |
| L 저장 직후 라벨 | 없음 | setPrinting_prod |
| N 전송 | 일괄 + `..._PRODUCTION` | 일괄 + `..._PRODUCTION` (동일) |
| 그 외(B,C,F,G,H,I,J,K,M) | 기본 | 기본 (동일) |

| 안 | 장점 | 단점 |
|----|------|------|
| **분리(권장)** | 다른 지점 5곳(A2·A3·E·D·L)이 클래스로 갈라져 `if (searchType == 1)` 류 내부 분기가 없다. 개발 60 이 생산(1)을 별도 메서드(`setBarcodeMsgProduction`)로 뺀 방향과 일치. 7 이 "미사용" 표기라 나중에 7만 삭제하기 쉽다. 개발 75 도 마트별 클래스 방침 | 클래스 1개 증가. 전송 URL 상수(`..._PRODUCTION`)가 두 클래스에 중복(상수 1줄) |
| 묶음(`ProductionMode`) | 클래스 1개 | 5개 메서드 안에 `Common.searchType.equals("1")` 같은 **내부 분기가 다시 생긴다** → 분리 목적(분기 제거)에 반함. 생산(1) 테스트 드롭 방침이라 7 의 동작이 1 에 가려진다 |

권장안: **분리**. 두 클래스는 모두 `DefaultShipmentMode` 를 상속하고 생산(1)은 A2/A3/E 2개 메서드(`requiresPrinterSetup`, `usesProductionBarcodeFlow`) + 전송 2개, 생산라벨(7)은 D/L 2개 메서드(`reprint`, `printOnSave`) + 전송 2개(방식·URL)를 override 한다.

### 2.3 데이터 흐름 (변경 전/후)

```
[변경 전]
Activity 메서드 N곳
   └ if (Common.searchType.equals(...) || ...) { 마트 A 동작 } else if (...) { 마트 B 동작 } else { 기본 }

[변경 후]
Activity.onCreate
   └ mode = ShipmentModeFactory.create(Common.searchType)      ← 1회 (setContentView 이전)
Activity 메서드 N곳
   └ 공통 흐름(화면·arSM·위젯·current_work_position 은 Activity 가 보유)
       └ mode.xxx(필요한 값만 전달)  ← 마트 판단/계산·라벨·저장 호출
              EmartMode / EmartNonfixedMode / WholesaleMode / HomeplusMode /
              HomeplusNonfixedMode / LotteMode / ProductionMode / ProductionLabelMode
              (override 하지 않은 메서드는 DefaultShipmentMode 기본 구현)
```

---

## 3. ShipmentMode 인터페이스 설계

### 3.1 설계 원칙

1. **상태 접근 최소화**: Mode 는 Activity 참조를 **갖지 않는다**. 필요한 값(double/String/boolean/객체)을 **인자로 전달**하고 결과를 **반환값**으로 돌려준다. 위젯(`edit_*`, `sp_*`, `swt_print`)·`Toast`·`vibrator`·`sound_pool` 은 Mode 가 건드리지 않는다 — 화면 갱신은 Activity 가 한다. (Activity 참조 전달안은 Mode 가 화면 상태 전체에 접근해 책임 분리가 약해져 기각.)
2. 예외: 이미 Activity 와 분리된 협력 객체(`LabelPrintHelper`, `PrinterCallback`, `Context`, `Shipments_Info`, `Goodswets_Info`, `Barcodes_Info`, `DBHandler`)는 인자로 받는다. `arSM` 위치 접근이 지연 평가(조건 분기 안에서만 `get`)에 의존하는 곳(재출력)은 `ArrayList` 와 인덱스를 함께 전달해 평가 시점을 보존한다.
3. **롯데 박스순번 상태(`lotte_TryCount`)** 는 Activity 필드에서 `LotteMode` 필드로 이동한다. 이 값은 롯데 저장(J)·롯데 목록 로드(M)에서만 쓰이고 다른 곳에서 읽는 코드가 없다(grep `lotte_TryCount` = 282·1973~1979·2615~2626행뿐). Mode 인스턴스는 onCreate 에서 만들어 Activity 와 수명이 같으므로 필드 초기값 0 도 동일.
4. 메서드 수 묶음 기준: ① 호출 지점(행)마다 메서드 1개, 단 같은 판단을 하는 지점은 1개로 공유(예: A2·A3 → `requiresPrinterSetup`, F1·F2 → `skipsDuplicateCheck`, H1·H2 → `lbToKgFloor`). ② 계산 분기(B·K1~K4)는 입력·출력 형이 서로 달라 분리 유지(수기 입력 B 는 로그 포함·`%.1f`, 저장 K1 은 로그 없음). ③ 매개변수가 많은 동작형(재출력·저장 직후 라벨)은 별도 파라미터 객체를 만들지 않는다(문서 외 구조 추가 금지) — 대신 인자 순서를 현재 호출식과 동일하게 맞춘다. ④ 마트 2종만 갈리는 계산 묶음(이마트 vs 나머지)은 인터페이스를 나누지 않고 `DefaultShipmentMode`(기본)와 `EmartMode`(override)로 해결한다.
5. 로그: Mode 안의 `Log` 는 태그 `"BixolonShipmentActivity"`(현재 `TAG` 값, 63행 선언 `private final String TAG = "BixolonShipmentActivity"`)를 `DefaultShipmentMode` 상수 `TAG` 로 동일하게 쓴다 → logcat 필터 호환.

### 3.2 메서드 목록 (분기 지점 → 메서드 1:1)

총 **19개**. 표의 "기본 구현"은 `DefaultShipmentMode` 구현.

| # | 분기 | 메서드 시그니처 | 전달 값 | 반환 | 기본 구현(=현재 else) |
|:-:|:---:|----------------|---------|------|----------------------|
| 1 | A1 | `boolean usesWholesaleLayout()` | - | true 면 도매 레이아웃 | false |
| 2 | A2·A3 | `boolean requiresPrinterSetup()` | - | false 면 프린터 비활성(426)·BT/프린터 확인 생략(465·470) | true |
| 3 | E | `boolean usesProductionBarcodeFlow()` | - | true 면 `setBarcodeMsgProduction` 위임 | false |
| 4 | B | `String formatManualWeight(double weight)` | 수기 입력 double | `temp_weight` 문자열 | `Double.toString` (+로그) |
| 5 | C | `boolean needsExpiryOnManualInput(boolean importCenter)` | Activity 가 센터명으로 계산한 수입센터 여부 | true 면 `startExpiryEnter` | false |
| 6 | D | `void reprint(LabelPrintHelper helper, String printWeightStr, ArrayList<Shipments_Info> arSM, int selectPosition, int currentWorkPosition, String makingDate, Bundle msgData, Barcodes_Info workItemBiInfo, LabelPrintHelper.PrinterCallback callback)` | 재출력 핸들러 값 | - | `helper.setPrinting(...)` (재출력, true) |
| 7 | F1·F2 | `boolean skipsDuplicateCheck()` | - | true 면 `dup=false` | false |
| 8 | G | `boolean requiresShelfLifeForTraders()` | - | true 면 소비기한 필수 검사 | false |
| 9 | H1·H2 | `double lbToKgFloor(double kgBeforeFloor, double itemPow)` | LB×0.453592 값, `item_pow` | 절사된 kg | `Math.floor(v*100)/100` |
| 10 | I | `boolean acceptsAnyBarcodeInfoRow()` | - | true 면 일치 여부 무관 채택 | false |
| 11 | J | `String insertGoodsWet(Context context, Goodswets_Info gi)` | 저장할 계근 1건 | 박스순번 문자열(롯데만 값, 그 외 "") | `DBHandler.insertqueryGoodsWet`, "" |
| 12 | K1 | `String formatSaveWeight(double weight)` | 계근 중량 double | `temp_weight` | `Double.toString` |
| 13 | K2 | `double accumulateGiQty(double currentGiQty, double addWeight)` | 현재 GI_QTY, 추가 중량 | 새 GI_QTY | `round((a+b)*1000)/1000.0` (+로그) |
| 14 | K3 | `double roundCenterWorkWeight(double centerWorkWeight)` | 누적 센터 중량 | 반올림값 | `round(x*1000)/1000.0` |
| 15 | K4 | `String centerWeightText(double centerTotalWeight, double centerWorkWeight)` | 총/작업 중량 | 화면 표시 문자열 | `total + " / " + work` |
| 16 | L | `void printOnSave(LabelPrintHelper helper, double weight, Shipments_Info current, String makingDate, Barcodes_Info workItemBiInfo, String boxOrder, LabelPrintHelper.PrinterCallback callback)` | 저장 직후 값 | - | 아무것도 안 함 |
| 17 | M | `void onShipmentListLoaded(ArrayList<Shipments_Info> arSM)` | 로드된 출하대상 목록 | - | 아무것도 안 함 |
| 18 | N1·N3 | `SendType getSendType()` | - | `PER_ITEM`/`BATCH`/`NONE` | `NONE` |
| 19 | N2·N4 | `String getSendUrl()` | - | 전송 URL(`Common.URL_*`) | `null` |

- `SendType` 은 `ShipmentMode` 안의 중첩 enum `enum SendType { PER_ITEM, BATCH, NONE }` (일반 Java enum, 람다 아님).
- 6번 `reprint` 의 `Bundle msgData` 는 롯데만 `msgData.getString("BOX_ORDER")` 를 읽는다(`android.os.Bundle` import). 현재 코드는 `msg.getData()` 를 그대로 전달한 것과 동일.
- 16번 `printOnSave` 는 `Common.print_bool` 검사를 **Activity 에 남기고**(`if (Common.print_bool) { mode.printOnSave(...) }`) 마트 분기만 Mode 로 옮긴다.
- 4·13번의 로그(`Log.i/e`)는 현재 해당 분기 안에 있는 것을 Mode 로 함께 이동한다.
- 타입 import 경로: `com.rgbsolution.highland_emart.items.{Shipments_Info, Barcodes_Info, Goodswets_Info}`, `com.rgbsolution.highland_emart.print.LabelPrintHelper`, `com.rgbsolution.highland_emart.db.DBHandler`, `com.rgbsolution.highland_emart.common.Common`.

### 3.3 Activity 에서 하지 않는 일 / 하는 일

| 계속 Activity | Mode 로 이동 |
|---------------|--------------|
| 위젯 접근·Toast·진동·사운드, `arSM`·`current_work_position`·`select_position`·`work_*` 필드 | searchType 에 따른 판단·계산·라벨 호출·저장 호출·URL 선택 |
| 킬코이/미트센터/센터명 문자열 조건(분기가 searchType 가 아님) | - |
| packet 조립·`HttpHelper` 호출·결과 처리 루프(전송 AsyncTask 의 공통 흐름) | 전송 방식(건별/일괄)·URL 선택 |
| `Common.print_bool` 검사 | 마트별 라벨 메서드 선택 |

---

## 4. 각 Mode 클래스별 구현 표

표기: 「기본」= DefaultShipmentMode 상속(override 없음). 굵은 글씨 = 해당 클래스가 override 하는 값.

| # | 메서드 | Default(그 외·미지원) | EmartMode(0) | EmartNonfixedMode(4) | WholesaleMode(3) | HomeplusMode(2) | HomeplusNonfixedMode(5) | LotteMode(6) | ProductionMode(1) | ProductionLabelMode(7) |
|:-:|--------|------|------|------|------|------|------|------|------|------|
| 1 | usesWholesaleLayout | false | 기본 | 기본 | **true** | 기본 | 기본 | 기본 | 기본 | 기본 |
| 2 | requiresPrinterSetup | true | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 | **false** | 기본 |
| 3 | usesProductionBarcodeFlow | false | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 | **true** | 기본 |
| 4 | formatManualWeight | toString | **floor(w*10)/10 → %.1f** | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 |
| 5 | needsExpiryOnManualInput | false | **importCenter** | 기본 | 기본 | 기본 | 기본 | **true** | 기본 | 기본 |
| 6 | reprint | setPrinting | 기본 | 기본 | 기본 | **setHomeplusPrinting** | **setHomeplusPrinting** | **setPrintingLotte(BOX_ORDER)** | 기본 | **setPrinting_prod** |
| 7 | skipsDuplicateCheck | false | 기본 | **true** | 기본 | 기본 | **true** | 기본 | 기본 | 기본 |
| 8 | requiresShelfLifeForTraders | false | **true** | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 |
| 9 | lbToKgFloor | floor(v*100)/100 | **floor(v*pow)/pow** | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 |
| 10 | acceptsAnyBarcodeInfoRow | false | 기본 | **true** | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 |
| 11 | insertGoodsWet | insertqueryGoodsWet, "" | 기본 | 기본 | 기본 | **Homeplus(maxBoxOrder), ""** | 기본(!) | **Lotte(TryCount 확정·증가), box 순번** | 기본 | 기본 |
| 12 | formatSaveWeight | toString | **floor→%.1f** | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 |
| 13 | accumulateGiQty | round(v*1000)/1000 | **round(v*10)/10** | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 |
| 14 | roundCenterWorkWeight | *1000 | **\*100** | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 |
| 15 | centerWeightText | total + " / " + work | **round(total*10)/10.0 + " / " + work** | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 | 기본 |
| 16 | printOnSave | 없음 | **setPrinting("이마트 출력 시작")** | **setPrinting("이마트(비정량) 출력 시작")** | 기본(없음) | **setHomeplusPrinting("홈플 출력 시작")** | **setHomeplusPrinting("홈플 출력 시작")** | **setPrintingLotte("롯데 출력 시작")** | 기본(없음) | **setPrinting_prod("생산 출력 시작")** |
| 17 | onShipmentListLoaded | 없음 | 기본 | 기본 | 기본 | 기본 | 기본 | **LAST_BOX_ORDER+Σ수량 초기화** | 기본 | 기본 |
| 18 | getSendType | NONE | **PER_ITEM** | **BATCH** | **BATCH** | **PER_ITEM** | **BATCH** | **PER_ITEM** | **BATCH** | **BATCH** |
| 19 | getSendUrl | null | **URL_INSERT_GOODS_WET** | **URL_INSERT_GOODS_WET_NEW** | **URL_INSERT_GOODS_WET_NEW** | **URL_INSERT_GOODS_WET_HOMEPLUS** | **URL_INSERT_GOODS_WET_NEW** | **URL_INSERT_GOODS_WET_LOTTE** | **URL_INSERT_GOODS_WET_PRODUCTION** | **URL_INSERT_GOODS_WET_PRODUCTION** |

공유 분기 배치 원칙:
- **홈플러스(2) vs 홈플러스비정량(5)**: 재출력·저장 직후 라벨은 **같은 코드(2줄)를 두 클래스에 각각 복사**한다(개발 75 가 H2/H5 라벨 클래스를 같은 레이아웃이라도 독립 클래스로 둔 방침과 동일: 클래스 하나를 고쳐도 다른 쪽이 따라가지 않도록). 저장(11)은 홈플(2)만 override, 비정량(5)은 기본(= 현재 코드에서 5 는 `insertqueryGoodsWet`). 전송은 2 건별/HOMEPLUS URL, 5 일괄/_NEW.
- **이마트비정량(4)·홈플러스비정량(5) 중복체크 제외**: 두 클래스가 각자 `skipsDuplicateCheck()=true` override. 이마트(0)·도매(3) 등은 기본 false.
- **이마트(0) vs 나머지 반올림**(4·12·13·14·15·9): 이마트만 override, 나머지 7종은 기본 상속. 이마트비정량(4)은 override 하지 않는다(현재 코드가 else 경로).
- **이마트(0)·이마트비정량(4) 저장 직후 라벨**: 같은 `setPrinting` 호출을 각 클래스에 독립 작성(로그 문구만 다름).

---

## 5. 동치 분석 (변경 전 = 변경 후)

### 5.1 분기 조건별 실행 경로 동일성

| # | 분기 | 변경 전 조건 → 경로 | 변경 후 호출 → 경로 | 마트 7종+미지원 동일 증명 |
|:-:|:---:|--------------------|---------------------|----------------------------|
| 1 | A1 | `== "3"` → 도매 레이아웃, 그 외 → 일반 | `mode.usesWholesaleLayout()` | 3 만 true. 나머지 false |
| 2 | A2 | `== "1"` → 인쇄 비활성 3줄 | `!mode.requiresPrinterSetup()` → 동일 3줄 | 1 만 requiresPrinterSetup=false → 같은 조건 |
| 3 | A3-465 | `!enabled && != "1"` | `!enabled && mode.requiresPrinterSetup()` | `!= "1"` ≡ requiresPrinterSetup(1 만 false) |
| 4 | A3-470 | `printer_setting && != "1"` | `printer_setting && mode.requiresPrinterSetup()` | 동일 |
| 5 | B | `== "0"` → floor/%.1f, 그 외 toString | `mode.formatManualWeight(w)` | 0 만 EmartMode override. 로그 3종 Mode 내 동일 순서 |
| 6 | C | 아래 5.2 상세 | `mode.needsExpiryOnManualInput(importCenter)` | 아래 진리표 |
| 7 | D | 홈플/홈플비 → homeplus, 6 → lotte, 7 → prod, 그 외 → setPrinting | `mode.reprint(...)` | 2·5 → homeplus override, 6 lotte, 7 prod, 그 외(0,1,3,4,미지원) 기본 setPrinting ✓ |
| 8 | E | `== "1"` → 위임 후 return | `mode.usesProductionBarcodeFlow()` | 1 만 true |
| 9 | F1·F2 | `== "4" \|\| == "5"` → dup=false | `mode.skipsDuplicateCheck()` | 4·5 true, 그 외 false |
| 10 | G | `== "0"` → 소비기한 필수 검사 | `mode.requiresShelfLifeForTraders()` | 0 만 true. 바깥 센터명 `else if` 조건(searchType 무관)은 Activity 유지 |
| 11 | H1·H2 | `== "0"` → floor(kg*pow)/pow, 그 외 floor(kg*100)/100 | `mode.lbToKgFloor(kg, pow)` | 0 만 override. `temp_weight_double` 계산(×0.453592)은 Activity 유지 |
| 12 | I | `== "4"` → 채택 블록 | `mode.acceptsAnyBarcodeInfoRow()` | 4 만 true |
| 13 | J | 2 → homeplus 저장, 6 → lotte 저장, 그 외 → 기본 저장 | `mode.insertGoodsWet(...)` | 2 homeplus, 6 lotte, **5 는 기본**(현재 코드 `== "2"` 만 홈플 저장이므로 5 는 else) ✓ |
| 14 | K1~K4 | `== "0"` 이마트 규칙, 그 외 기본 규칙 | 4개 메서드 | 0 만 override, 4 포함 나머지 기본 |
| 15 | L | 2·5 homeplus / 0 setPrinting / 4 setPrinting / 6 lotte / 7 prod / 그 외(1,3,미지원) 없음 | `mode.printOnSave(...)` | 2·5·0·4·6·7 override, 1·3·미지원 기본(없음) ✓ |
| 16 | M | `== "6"` → 카운터 초기화 | `mode.onShipmentListLoaded(arSM)` | 6 만 override |
| 17 | N1·N3·N2·N4 | 아래 5.3 | `getSendType()`/`getSendUrl()` | 아래 5.3 |

### 5.2 C(수기 소비기한창) 진리표

변경 전:
```java
if (킬코이 && 미트센터) startExpiryEnter();
else if (센터명TRD || WET || ET || searchType==6) {
    if (searchType==0 || searchType==6) startExpiryEnter(); else wet_data_insert(...);
} else wet_data_insert(...);
```
변경 후: 킬코이/미트센터 분기는 Activity 유지. 나머지는 `else if (mode.needsExpiryOnManualInput(importCenter)) startExpiryEnter(); else wet_data_insert();` (`importCenter` = 센터명 TRD‖WET‖ET, 해당 `else if` 위치에서 계산 — 평가 위치·`contains` 호출 순서 동일, `getCENTERNAME()` null 시 NPE 도 같은 지점)

| searchType | importCenter | 변경 전 결과 | needsExpiry...(importCenter) | 변경 후 결과 |
|:---------:|:------------:|:-----------:|:----------------------------:|:-----------:|
| 0 | true | startExpiryEnter | true | startExpiryEnter ✓ |
| 0 | false | wet_data_insert | false | wet_data_insert ✓ |
| 6 | true | startExpiryEnter | true | startExpiryEnter ✓ |
| 6 | false | startExpiryEnter(searchType==6 이 조건) | true | startExpiryEnter ✓ |
| 1,2,3,4,5,7,미지원 | true | wet_data_insert(안쪽 조건 불충족) | false | wet_data_insert ✓ |
| 1,2,3,4,5,7,미지원 | false | wet_data_insert | false | wet_data_insert ✓ |

### 5.3 전송(N) 동치 — 도달 불가 분기 처리

변경 전 구조: 바깥 `if (0||2||6) {건별} else if (1||3||4||5||7) {일괄}`. 안쪽 URL 체인은 건별 안에 5종(0/3, 2, 6, 1, 7), 일괄 안에 5종(0, 2, 1/7, 4/5, 3).

| 구분 | 바깥 조건으로 도달 가능한 searchType | 안쪽 체인에서 도달 가능한 분기 | 도달 불가 분기(바깥 조건에 의해 제외) |
|------|------------------------------------|-------------------------------|----------------------------------------|
| 건별(N2) | 0, 2, 6 | 0 → `URL_INSERT_GOODS_WET`, 2 → `_HOMEPLUS`, 6 → `_LOTTE` | 3(`URL_INSERT_GOODS_WET`), 1·7(`URL_INSERT_GOODS_WET`) — 바깥에서 이미 제외 |
| 일괄(N4) | 1, 3, 4, 5, 7 | 1·7 → `_PRODUCTION`(`sendDataDb`), 4·5 → `_NEW`, 3 → `_NEW` | 0(`HttpHelper.sendData(.., URL_INSERT_GOODS_WET)`), 2(`_HOMEPLUS`) — 바깥에서 0·2 가 건별로 먼저 처리됨 |

- 따라서 `getSendType()` 가 `PER_ITEM`(0,2,6)/`BATCH`(1,3,4,5,7)/`NONE`(그 외) 이고 `getSendUrl()` 은 각 마트가 도달 가능한 단일 URL 만 반환하면 **도달 가능한 모든 경로의 URL·호출(`sendDataDb(packet, "inno", "goodswet_insert", url)`)이 동일**하다. 건별·일괄 모두 도달 가능 경로는 현재 `sendDataDb` 4인자 호출이므로 URL 만 Mode 에서 받는다.
- **도달 불가 분기는 이관하지 않는다**(건별 안의 3·1·7 URL, 일괄 안의 0(`HttpHelper.sendData`)·2 URL). 근거는 위 표(바깥 조건이 배타적). 데드 코드 제거는 "임의 제거 금지" 제약과 충돌 가능하므로 **13절 #6 에 사용자 승인 항목**으로 올린다. 승인 전에는 Activity 의 원래 체인을 그대로 두는 대안(Step 4 에서 선택)도 가능.
- **미지원 searchType**: 변경 전 바깥 두 조건 모두 불성립 → 두 블록 건너뜀 → `return result;`(= `""`). 변경 후 `NONE` → 두 블록 건너뜀 → 동일.
- 전송 후 처리(`updatequeryGoodsWet`·`SAVE_CNT`·`jChk`·`"ss"`)는 searchType 분기가 아니므로 Activity 에 그대로 둔다. 개발 74(일괄 `jChk > 0 && jChk == arSM.size()`) 코드는 손대지 않는다.

### 5.4 상태 이동(롯데 박스순번) 동치

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 저장 위치 | Activity 필드 `lotte_TryCount`(282행, 초기 0) | `LotteMode` 필드(초기 0) |
| 수명 | Activity 인스턴스 | Mode 인스턴스(onCreate 생성 = Activity 와 동일) |
| 저장(J) | `lotteBoxOrder = valueOf(count)` → `insertqueryGoodsWetLotte(this, gi, count)` → `count++` → 9999 초과 시 1 | `LotteMode.insertGoodsWet` 같은 순서. 반환값이 `lotteBoxOrder` |
| 초기화(M) | doInBackground(백그라운드)에서 대입 | `LotteMode.onShipmentListLoaded` 를 같은 위치(`try` 안, 백그라운드)에서 호출 |
| 스레드 | 필드 비-volatile, UI/백그라운드 접근 | 동일(비-volatile 유지, 새로운 동기화 도입 안 함) → 위험 증가 없음 |
| 예외 | `arSM.get(0)` 빈 목록 시 IndexOutOfBounds → 바깥 catch | 동일 위치 try 내부 → 동일 |
| 다른 마트 | lotte_TryCount 미사용 | LotteMode 외 접근 불가 |

### 5.5 예외·실행 순서 동치

| 지점 | 변경 전 | 변경 후 | 동치 |
|------|---------|---------|------|
| 팩토리 시점 | 333행 `searchType.equals` 에서 비교 시작 | onCreate 의 같은 위치(333행)에서 `create(Common.searchType)` | O (null 은 NPE) |
| 재출력 D | 홈플/롯데/prod 분기에서는 `arSM.get(current_work_position)` 를 평가하지 않음 | Mode 에 `arSM`·인덱스를 넘기고 setPrinting 기본 경로에서만 `get` | O (current=-1 일 때 홈플 재출력의 IndexOutOfBounds 가 새로 생기지 않음) |
| 재출력 D (BOX_ORDER) | 롯데에서만 `msg.getData().getString("BOX_ORDER").toString()` 먼저, 그 다음 `parseDouble` | `LotteMode.reprint` 안에서 같은 순서 | O |
| 재출력 D (파싱) | `Double.parseDouble(print_weight_str)` 를 호출식 인자로 평가 | Mode 에서 같은 시점에 파싱(예외는 바깥 `catch(Exception)` 로그 동일) | O |
| 저장 직후 L | 분기 안에서 `arSM.get(current_work_position)` | Activity 가 현재 항목 객체를 미리 전달. 이 시점에 `arSM.get(current_work_position)` 은 이미 위쪽(1936·1947행 등)에서 반복 사용되어 예외 선행 불가 | O |
| 수기 소비기한 C | `contains` 평가 → searchType 비교 | `contains` 평가(Activity) → Mode 호출 | O |
| G(1237) | 센터명 `else if` 안에서 searchType 비교 | 같은 `else if` 안에서 `mode.requiresShelfLifeForTraders()` | O |

---

## 6. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **ShipmentMode.java** (신규) | `app/src/main/java/com/rgbsolution/highland_emart/shipment/mode/` | 인터페이스 + `SendType` enum |
| 2 | **DefaultShipmentMode.java** (신규) | 동일 | 기본(=else) 구현 + `TAG` 상수 |
| 3 | **EmartMode / EmartNonfixedMode / WholesaleMode / HomeplusMode / HomeplusNonfixedMode / LotteMode / ProductionMode / ProductionLabelMode.java** (신규 8개) | 동일 | 4절 표의 굵은 항목 override |
| 4 | **ShipmentModeFactory.java** (신규) | 동일 | `create(String searchType)` |
| 5 | **BixolonShipmentActivity.java** | `mode` 필드 추가(136행 `labelPrintHelper` 선언 옆), 333·426·465·470·666~700·970~981·1092·1148·1237·1272·1386·1448·1836·1967~2070·2612~2627·2785~2921 | 24개 분기 지점을 `mode.xxx()` 로 치환, `lotte_TryCount` 필드(282행) 삭제 |
| 6 | (변경 없음) | `common/Common.java` | `SEARCH_TYPE_*` 상수는 팩토리·Mode 가 그대로 참조 |

---

## 7. 수정 상세

### 7.1 ShipmentModeFactory.java (신규)

```java
package com.rgbsolution.highland_emart.shipment.mode;

import com.rgbsolution.highland_emart.common.Common;

public class ShipmentModeFactory {
    /** searchType 에 맞는 Mode 를 만든다. null 이면 NPE(변경 전 333행과 동일). 미지원 값은 DefaultShipmentMode(=기존 else 동작). */
    public static ShipmentMode create(String searchType) {
        if (searchType.equals(Common.SEARCH_TYPE_EMART)) {
            return new EmartMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_NONFIXED)) {
            return new EmartNonfixedMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_WHOLESALE)) {
            return new WholesaleMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_HOMEPLUS)) {
            return new HomeplusMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_HOMEPLUS_NONFIXED)) {
            return new HomeplusNonfixedMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_LOTTE)) {
            return new LotteMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_PRODUCTION)) {
            return new ProductionMode();
        } else if (searchType.equals(Common.SEARCH_TYPE_PRODUCTION_LABEL)) {
            return new ProductionLabelMode();
        }
        return new DefaultShipmentMode();
    }
}
```

### 7.2 Activity 치환 (지점별 변경 전/후)

**(mode 필드·생성)**

```java
// 변경 전 333행
if(Common.searchType.equals(Common.SEARCH_TYPE_WHOLESALE)){
    setContentView(R.layout.activity_shipment_wholesale);
}else{
    setContentView(R.layout.activity_shipment);
}
```
```java
// 변경 후
private ShipmentMode mode;   // 필드 (labelPrintHelper 선언 근처)

mode = ShipmentModeFactory.create(Common.searchType);
if(mode.usesWholesaleLayout()){
    setContentView(R.layout.activity_shipment_wholesale);
}else{
    setContentView(R.layout.activity_shipment);
}
```

**A2 (426행)·A3 (465·470행)**
```java
// 변경 전
if(Common.searchType.equals(Common.SEARCH_TYPE_PRODUCTION)){ ... }
if (!mBluetoothAdapter.isEnabled() && !Common.searchType.equals(Common.SEARCH_TYPE_PRODUCTION)) { ...
if (Common.printer_setting && !Common.searchType.equals(Common.SEARCH_TYPE_PRODUCTION)) { ...
```
```java
// 변경 후 (본문·주석 그대로)
if(!mode.requiresPrinterSetup()){ ... }
if (!mBluetoothAdapter.isEnabled() && mode.requiresPrinterSetup()) { ...
if (Common.printer_setting && mode.requiresPrinterSetup()) { ...
```

**B (666~675행) + C (689~700행)**
```java
// 변경 전 B
String temp_weight = "";
if (Common.searchType.equals(Common.SEARCH_TYPE_EMART)) { //이마트 출하대상일경우
    weight_double = Math.floor(weight_double * 10);
    Log.i(TAG, "=====================weight_double 1-1==================" + weight_double);
    weight_double = weight_double / 10.0;
    Log.i(TAG, "=====================weight_double 1-2==================" + weight_double);
    temp_weight = String.format("%.1f", weight_double);
} else { //이노 생산계근 or 홈플러스 추가계근일경우
    temp_weight = Double.toString(weight_double);
    Log.i(TAG, "=====================temp_weight production==================" + temp_weight);
}
```
```java
// 변경 후 B
String temp_weight = mode.formatManualWeight(weight_double);
// 변경 전 weight_double 재대입(floor 값)은 곧바로 678행 weight_double = Double.parseDouble(temp_weight) 로 덮어써지며 그 사이에는 로그만 있어 영향 없음
```
```java
// 변경 후 C (킬코이 분기는 그대로)
if (킬코이 && 미트센터) {
      startExpiryEnter(weight_str, weight_double);
} else if (mode.needsExpiryOnManualInput(
        arSM.get(current_work_position).getCENTERNAME().contains(Common.CENTER_NAME_TRD) ||
        arSM.get(current_work_position).getCENTERNAME().contains(Common.CENTER_NAME_WET) ||
        arSM.get(current_work_position).getCENTERNAME().contains(Common.CENTER_NAME_ET))) {
    startExpiryEnter(weight_str, weight_double);
} else {
    wet_data_insert(weight_str, weight_double, "", "");
}
```

**D (970~981행)**
```java
// 변경 후
String print_weight_str = msg.getData().getString("WEIGHT").toString();
String making_date = msg.getData().getString("MAKINGDATE").toString();
mode.reprint(labelPrintHelper, print_weight_str, arSM, select_position, current_work_position,
        making_date, msg.getData(), work_item_bi_info, printerCallback);
break;
```
Mode 쪽(예시):
```java
// HomeplusMode / HomeplusNonfixedMode
helper.setHomeplusPrinting(Double.parseDouble(printWeightStr), arSM.get(selectPosition), true, callback);
// LotteMode
String box_order = msgData.getString("BOX_ORDER").toString();
helper.setPrintingLotte(Double.parseDouble(printWeightStr), arSM.get(selectPosition), true, makingDate, box_order, Common.searchType, callback);
// ProductionLabelMode
helper.setPrinting_prod(Double.parseDouble(printWeightStr), arSM.get(selectPosition), true, callback);
// DefaultShipmentMode(0,1,3,4,미지원)
helper.setPrinting(Double.parseDouble(printWeightStr), arSM.get(selectPosition), true, makingDate, workItemBiInfo, arSM.get(currentWorkPosition), Common.searchType, callback);
```

**E (1092행)**
```java
if (mode.usesProductionBarcodeFlow()) {
    setBarcodeMsgProduction(msg);
    return;
}
```

**F1 (1148)·F2 (1272)**
```java
if(mode.skipsDuplicateCheck()){ //비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
    dup = false;
}
```
**G (1237)**
```java
if (mode.requiresShelfLifeForTraders()) {   // 기존 if(searchType == EMART) 와 동일 본문
    if (work_item_bi_info.getSHELF_LIFE().equals("") || ...) { Toast ... return; }
}
```
**H1 (1386)·H2 (1448)**
```java
// 변경 전
if (Common.searchType.equals(Common.SEARCH_TYPE_EMART)) {
    item_weight_double = Math.floor(temp_weight_double * item_pow) / item_pow;
} else {
    item_weight_double = Math.floor(temp_weight_double * 100) / 100; //lb 변환 후 소수점 두자리까지 처리하도록 변경
}
// 변경 후
item_weight_double = mode.lbToKgFloor(temp_weight_double, item_pow);
```
**I (1836)**
```java
if(mode.acceptsAnyBarcodeInfoRow()){ ... 기존 블록 그대로 ... }
```
**J + K + L (wet_data_insert 1965~2070)**
```java
// 변경 후
String lotteBoxOrder = mode.insertGoodsWet(this, gi);   // 1965~1983 치환 (변수명 유지)

Log.e(TAG, "=========================계근중량 변환전=========================" + weight_double);
String temp_weight = mode.formatSaveWeight(weight_double);
weight_double = Double.parseDouble(temp_weight);
Log.e(TAG, "=========================계근중량 변환후=========================" + weight_double);

arSM.get(current_work_position).setPACKING_QTY(arSM.get(current_work_position).getPACKING_QTY() + 1);
arSM.get(current_work_position).setGI_QTY(mode.accumulateGiQty(arSM.get(current_work_position).getGI_QTY(), weight_double));

centerWorkCount++;
centerWorkWeight += weight_double;
Log.e(TAG, "=========================센터중량 변환전=========================" + centerWorkWeight);
centerWorkWeight = mode.roundCenterWorkWeight(centerWorkWeight);
Log.e(TAG, "=========================센터중량 변환후=========================" + centerWorkWeight);

edit_center_tcount.setText(centerTotalCount + " / " + centerWorkCount);
edit_center_tweight.setText(mode.centerWeightText(centerTotalWeight, centerWorkWeight));
...
if (Common.print_bool) {
    mode.printOnSave(labelPrintHelper, weight_double, arSM.get(current_work_position), making_date, work_item_bi_info, lotteBoxOrder, printerCallback);
}
```
Mode 쪽:
```java
// EmartMode.printOnSave
Log.d(TAG, "===========이마트 출력 시작 ================");
helper.setPrinting(weight, current, false, makingDate, workItemBiInfo, current, Common.searchType, callback);
// EmartNonfixedMode.printOnSave
Log.d(TAG, "===========이마트(비정량) 출력 시작 ================");
helper.setPrinting(weight, current, false, makingDate, workItemBiInfo, current, Common.searchType, callback);
// HomeplusMode / HomeplusNonfixedMode
Log.d(TAG, "===========홈플 출력 시작 ================");
helper.setHomeplusPrinting(weight, current, false, callback);
// LotteMode
Log.d(TAG, "===========롯데 출력 시작 ================");
helper.setPrintingLotte(weight, current, false, makingDate, boxOrder, Common.searchType, callback);
// ProductionLabelMode
Log.d(TAG, "===========생산 출력 시작 ================");
helper.setPrinting_prod(weight, current, false, callback);
```
(위 `Common.searchType` 는 현재 호출식 그대로 정적 값을 읽는다.)

**M (2612~2627)**
```java
// 변경 후 (try 내부 같은 위치)
mode.onShipmentListLoaded(arSM);
```
**N (2785~2921) — Activity 공통 흐름은 유지, 분기만 치환**
```java
// 변경 후
if(mode.getSendType() == ShipmentMode.SendType.PER_ITEM){ //건별 전송(이마트·홈플러스·롯데)
    ... packet 조립 동일 ...
    Log.i(TAG, "=====================Common.searchType==================" + Common.searchType);
    result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", mode.getSendUrl());
    ... 결과 처리 동일 ...
}else if(mode.getSendType() == ShipmentMode.SendType.BATCH){ //일괄 전송(생산·도매·이마트비정량·홈플비정량·생산라벨)
    ... packet 조립 동일 ...
    if(sendOrNot){
        result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", mode.getSendUrl());
    }else{
        result = "af";
    }
    ... 개발 74 이후 결과 처리 동일 ...
}
```
**lotte_TryCount 필드(282행) 삭제**, `LotteMode` 로 이동:
```java
public class LotteMode extends DefaultShipmentMode {
    private int lotte_TryCount = 0;

    @Override
    public String insertGoodsWet(Context context, Goodswets_Info gi) {
        String lotteBoxOrder = String.valueOf(lotte_TryCount);
        DBHandler.insertqueryGoodsWetLotte(context, gi, lotte_TryCount);
        lotte_TryCount++;
        if (lotte_TryCount > Common.LOTTE_BOX_ORDER_MAX) {
            lotte_TryCount = 1;
        }
        return lotteBoxOrder;
    }

    @Override
    public void onShipmentListLoaded(ArrayList<Shipments_Info> arSM) {
        Shipments_Info si = arSM.get(0);
        lotte_TryCount = Integer.parseInt(si.LAST_BOX_ORDER) + 1;
        ... 기존 2616~2626행 본문 그대로 (Log 포함) ...
    }
}
```
`DefaultShipmentMode.insertGoodsWet`: `DBHandler.insertqueryGoodsWet(context, gi); return "";` / `HomeplusMode.insertGoodsWet`: `int maxBoxOrder = DBHandler.selectMaxBoxOrder(context); Log.e(TAG, "=======================MAX BOX ORDER ###=========================" + maxBoxOrder); DBHandler.insertqueryGoodsWetHomeplus(context, gi, maxBoxOrder); return "";`

> 위 후 코드는 설계 스케치다. 실제 구현은 Step 별로 현재 코드를 복사 이동하고 접두어(`this`→`context`, `Common.`)만 바꾼다.

**검증**: Step 5(code-verifier 24개 지점 전/후 라인 대조 + original-comparator), Step 6(실기기 회귀).

---

## 8. 사이드이펙트

### 8.1 BixolonShipmentActivity 내부

| 영향 | 내용 | 대응 |
|------|------|------|
| AsyncTask 내부 클래스 접근 | `ProgressDlgShipSelect`·`ProgressDlgShipmentSend` 는 Activity 의 비-static 내부 클래스(2564·2746행) → 바깥 필드 `mode` 접근 가능 | `mode` 는 onCreate 에서 1회 대입 후 변경 없음(재대입 금지). AsyncTask 실행 전에 대입되므로 가시성 문제 없음 |
| 백그라운드 스레드에서 Mode 호출 | M(`onShipmentListLoaded`), N 전체가 `doInBackground` | Mode 메서드는 위젯·UI 접근 없음(3.1 원칙 1). `Toast` 등 UI 호출 금지(추가 시 위반) |
| `lotte_TryCount` 이동 | 282행 필드 삭제, LotteMode 내부로 | 5.4 동치. 다른 참조 없음(grep) |
| Activity 생명주기 | onCreate 에서 생성, onDestroy 에서 해제할 자원 없음. 회전/재생성 시 새 Activity → 새 Mode(원래 `lotte_TryCount` 도 새 Activity 에서 0 으로 초기화) | 동작 동일 |
| searchType 중간 변경 | `Common.searchType` 는 정적 변수. Activity 생존 중 변경 경로 없음(1.1) → onCreate 시점 스냅샷과 매 호출 시 읽기가 동일 | 앞으로 변경 경로를 추가하면 Mode 재생성 필요 → 주석으로 명시 |
| 메모리/성능 | Mode 객체 1개 | 무시 가능 |

### 8.2 다른 파일

| 파일 | 영향 | 비고 |
|------|------|------|
| `LabelPrintHelper` | 호출 시그니처 불변(Mode 가 같은 메서드를 호출) | 75 의 `print/label` 구조와 충돌 없음 |
| `DBHandler` | 호출 메서드·인자 불변 | `insertqueryGoodsWet/Homeplus/Lotte`, `selectMaxBoxOrder` |
| `HttpHelper`/`Common.URL_*` | `sendDataDb` 호출 동일, URL 상수 동일 | 일괄 안의 `HttpHelper.sendData`(이마트) 호출은 도달 불가라 이관 제외(5.3, 13절 #6) |
| `MainActivity` | `Common.searchType` 설정 후 Bixolon Activity 시작 — 변경 없음 | 범위 밖 |
| `ProgressDlgShipSearch`·`ProgressDlgBarcodeSearch`·`ProgressDlgGoodsWetSearch`·`DBHandler`·`ShipmentListAdapter`·`ProductionActivity`·`ShipmentActivity` | 각자의 searchType 분기는 **이번 대상 아님**(유지) | `ShipmentActivity`(구버전)는 삭제하지 않고 정리 대상에서 제외(기존 방침) |
| `setBarcodeMsgProduction`(개발 60) | 본문 변경 없음. 진입 판단(E)만 Mode 호출로 치환 | |

### 8.3 null / 미지원 searchType

| 입력 | 변경 전 | 변경 후 |
|------|---------|---------|
| null | 333행에서 NPE (`Common.searchType` 기본값 "0" 이라 사실상 발생 안 함) | `create(null)` NPE (같은 시점) |
| "8" 등 정의 없는 값 | 레이아웃 일반, 프린터 확인함, 기본 계산, 재출력 setPrinting, 저장 기본, 라벨 없음, 전송 블록 둘 다 건너뜀 → result "" | `DefaultShipmentMode` 동일 |

---

## 9. 데이터 저장 구조

### 변수 매핑

| 변수 | 타입 | 용도 | 이동 후 |
|------|------|------|---------|
| `Common.searchType` | String | 마트 구분 "0"~"7" | 팩토리 입력으로만 사용(+Mode 내부 라벨 호출 인자에 정적 값 그대로) |
| `mode` | ShipmentMode | 마트 처리 객체 | Activity 필드(신규), onCreate 에서 대입 |
| `lotte_TryCount` | int | 롯데 박스 순번 카운터 | Activity → `LotteMode` 필드 |
| `lotteBoxOrder` | String | 저장 직후 라벨에 쓰는 박스순번 | Activity 지역변수 유지, 값은 `mode.insertGoodsWet` 반환 |
| `temp_weight` | String | 중량 문자열 | 값만 `mode.formatManualWeight`/`formatSaveWeight` 반환 |
| `arSM`, `current_work_position`, `select_position`, `work_item_bi_info` | - | Activity 상태 | **Activity 에 유지**, Mode 에는 필요한 값/참조만 인자로 전달 |

### 인덱스 매핑 (searchType → Mode)

```
"0" ↔ EmartMode              "4" ↔ EmartNonfixedMode
"3" ↔ WholesaleMode          "2" ↔ HomeplusMode
"5" ↔ HomeplusNonfixedMode   "6" ↔ LotteMode
"1" ↔ ProductionMode         "7" ↔ ProductionLabelMode
그 외 ↔ DefaultShipmentMode
```

---

## 10. 호출 시점

```
[MainActivity] Common.searchType 설정 → BixolonShipmentActivity 시작
    ↓
[onCreate]
    ├── ★ mode = ShipmentModeFactory.create(Common.searchType)
    ├── mode.usesWholesaleLayout()           (A1)
    └── mode.requiresPrinterSetup()          (A2: 인쇄스위치)
[onStart]
    └── mode.requiresPrinterSetup()          (A3: BT/프린터 확인)
[목록 선택] ProgressDlgShipSelect.doInBackground
    └── mode.onShipmentListLoaded(arSM)      (M: 롯데 박스순번)
[스캔] setBarcodeMsg
    ├── mode.usesProductionBarcodeFlow()     (E)
    ├── mode.skipsDuplicateCheck()           (F1, F2)
    ├── mode.requiresShelfLifeForTraders()   (G)
    └── mode.lbToKgFloor()                   (H1, H2)
[수기 입력] inputBtnListener
    ├── mode.formatManualWeight()            (B)
    └── mode.needsExpiryOnManualInput()      (C)
[계근 저장] wet_data_insert
    ├── mode.insertGoodsWet()                (J)
    ├── mode.formatSaveWeight() / accumulateGiQty() / roundCenterWorkWeight() / centerWeightText()   (K)
    └── mode.printOnSave()                   (L, Common.print_bool 일 때)
[재출력] MESSAGE_REPRINT
    └── mode.reprint()                       (D)
[바코드 정보] find_work_info
    └── mode.acceptsAnyBarcodeInfoRow()      (I)
[전송] ProgressDlgShipmentSend.doInBackground
    ├── mode.getSendType()                   (N1, N3)
    └── mode.getSendUrl()                    (N2, N4)
```

---

## 11. 개발 플랜

> **착수 조건**: 개발 74·75 실기기 테스트 완료. 각 Step 은 사용자 지시 후 진행하고 다음 Step 은 지시를 기다린다. 코드 수정은 메인 에이전트가 수행.

### Step 1: 인터페이스·팩토리·Mode 클래스 골격

**Part 1. 분석**
- 메서드: 신규 파일 생성(기존 Activity 코드는 이 Step 에서 수정하지 않음)
- 범위: `shipment/mode/` 패키지 11개 파일 (ShipmentMode, DefaultShipmentMode, 8개 마트 Mode, ShipmentModeFactory)
- 용도: 3절 인터페이스 19개 메서드와 4절 override 표대로 골격을 만든다. 본문은 4절 값대로 구현하되(기본 구현은 현재 else 코드를 복사 이동) Activity 는 아직 사용하지 않는다
- 주의할 점: Mode 는 Activity 를 import 하지 않는다. 람다·새 문법 금지. `TAG` 값은 `"BixolonShipmentActivity"` 그대로

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 인터페이스 | ShipmentMode.java | 19개 메서드 + `enum SendType` |
| 2 | 기본 구현 | DefaultShipmentMode.java | 현재 else 경로 복사(로그 포함) |
| 3 | 마트 Mode 8개 | 각 파일 | 4절 굵은 항목만 override |
| 4 | 팩토리 | ShipmentModeFactory.java | 7.1 코드 |

**Part 2. 변환 계획**
- 변환 방식: 현재 코드를 복사 이동(접두어 `this`→`context` 등만 변경). override 표(4절)와 1:1 대조
- 주의사항: 홈플(2)/홈플비정량(5)의 재출력·저장 직후 라벨은 동일 코드를 각 클래스에 독립 작성. HomeplusNonfixed 의 `insertGoodsWet` 는 override 하지 않음

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (Mode 8종 × 메서드 19개 반환값이 4절 표와 일치하는지 문서 대조)
- [ ] Part 6: 회귀테스트 (Activity 미변경이므로 앱 빌드 후 기존 동작 무영향 확인)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 2: 단순 판단형·계산형 분기 이관

**Part 1. 분석**
- 메서드: onCreate(A1·A2), onStart(A3), inputBtnListener(B·C), setBarcodeMsg(E·F1·F2·G·H1·H2), find_work_info(I), wet_data_insert(K1~K4)
- 범위: `BixolonShipmentActivity.java` 333, 426, 465, 470, 666~700, 1092, 1148, 1237, 1272, 1386, 1448, 1836, 1989~2035
- 용도: searchType 비교만 있고 외부 호출(라벨·DB·전송)이 없는 분기를 Mode 호출로 치환. Activity 에 `private ShipmentMode mode;` 추가, onCreate 333행에서 팩토리 호출
- 주의할 점: ① C 는 킬코이/미트센터 분기를 Activity 에 남기고 `importCenter` 를 같은 `else if` 위치에서 계산. ② B·K1 에서 `weight_double` 재대입 제거 시 직후 `parseDouble(temp_weight)` 로 덮어써지는 점 확인(5.5). ③ 로그 문구·순서 유지

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | A1 | 333 | `mode.usesWholesaleLayout()` |
| 2 | A2·A3 | 426, 465, 470 | `mode.requiresPrinterSetup()` |
| 3 | B | 666~675 | `mode.formatManualWeight` |
| 4 | C | 689~700 | `mode.needsExpiryOnManualInput(importCenter)` |
| 5 | E | 1092 | `mode.usesProductionBarcodeFlow()` |
| 6 | F1·F2 | 1148, 1272 | `mode.skipsDuplicateCheck()` |
| 7 | G | 1237 | `mode.requiresShelfLifeForTraders()` |
| 8 | H1·H2 | 1386, 1448 | `mode.lbToKgFloor` |
| 9 | I | 1836 | `mode.acceptsAnyBarcodeInfoRow()` |
| 10 | K1~K4 | 1989~2035 | 4개 메서드 |

**Part 2. 변환 계획**
- 변환 방식: 지점별로 5.1 동치 표 행과 대조하며 치환. 분기 안 본문은 문자 그대로 유지
- 주의사항: 이 Step 완료 시 `Common.SEARCH_TYPE_*` 사용이 남는 곳(재출력·저장 라벨·전송·롯데 카운터)을 확인 — 다음 Step 에서 제거

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (마트×분기 진리표 5.1·5.2 대조)
- [ ] Part 6: 회귀테스트 (이마트 수기 입력 중량 / LB 환산 / 비정량 중복 스캔 허용 / 생산 진입)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 3: 동작형 분기(재출력·저장·저장 직후 라벨·롯데 박스순번) 이관

**Part 1. 분석**
- 메서드: mBixolonHandler MESSAGE_REPRINT(D), wet_data_insert(J·L), ProgressDlgShipSelect.doInBackground(M)
- 범위: 970~981, 1965~1983, 2054~2070, 2612~2627, 282(필드 삭제)
- 용도: 라벨·DB 저장 호출이 걸린 분기 이관. **롯데 박스순번 카운터(`lotte_TryCount`)는 J(저장)·M(목록 로드)가 같은 상태를 공유하므로 두 분기를 같은 Step 에서 함께 이관**한다(사용자 계획의 Step 4 "목록" 중 M 만 Step 3 으로 당김 — 분리하면 중간 상태에서 카운터 이중 관리로 롯데 박스순번이 깨짐)
- 주의할 점: ① 재출력은 `arSM`·인덱스를 넘겨 `get` 평가 시점 보존(5.5). ② BOX_ORDER 읽기 순서. ③ `Common.print_bool` 검사는 Activity 유지. ④ 홈플(2)·홈플비정량(5) 코드는 각 클래스에 독립 작성. ⑤ 5(홈플비정량)는 저장이 **기본**(insertqueryGoodsWet)임을 재확인

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | D | 970~981 | `mode.reprint(...)` |
| 2 | J | 1965~1983 | `mode.insertGoodsWet(this, gi)` → `lotteBoxOrder` |
| 3 | L | 2054~2070 | `if (print_bool) mode.printOnSave(...)` |
| 4 | M | 2612~2627 | `mode.onShipmentListLoaded(arSM)` |
| 5 | 필드 | 282 | `lotte_TryCount` 삭제 → LotteMode |

**Part 2. 변환 계획**
- 변환 방식: 현재 분기 본문을 해당 Mode 메서드로 복사 이동 후 Activity 호출 치환
- 주의사항: 롯데 `Integer.parseInt(si.LAST_BOX_ORDER)`·`Common.LOTTE_BOX_ORDER_MAX` 순환 로직은 한 글자도 바꾸지 않는다(개발 63·69 재확인)

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (D·J·L 마트별 호출 메서드/인자 대조, 롯데 카운터 시퀀스 대조)
- [ ] Part 6: 회귀테스트 (이마트/비정량/홈플/홈플비정량/롯데 계근 후 라벨·재출력, 롯데 박스순번 연속)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 4: 전송 이관

**Part 1. 분석**
- 메서드: ProgressDlgShipmentSend.doInBackground
- 범위: 2785(N1), 2814~2824(N2), 2864(N3), 2907~2921(N4)
- 용도: 건별/일괄 판단과 URL 선택을 `getSendType()`/`getSendUrl()` 로 치환
- 주의할 점: ① 5.3 도달 불가 분기 처리(이관 제외 vs Activity 원 체인 유지)는 13절 #6 승인 결과에 따른다. ② 결과 처리·`jChk`·개발 74 코드는 변경 금지. ③ 일괄 안의 분기별 중복 로그(2912·2915·2918·2919행)는 13절 #7 승인에 따른다. ④ 미지원 searchType 은 `NONE` 으로 두 블록 모두 건너뛰고 `return result` 유지

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | N1·N3 | 2785, 2864 | `mode.getSendType()` 비교 |
| 2 | N2·N4 | 2814~2824, 2907~2921 | `mode.getSendUrl()` |

**Part 2. 변환 계획**
- 변환 방식: 바깥 `if/else if` 조건만 치환, packet 조립·루프·결과 처리 본문 불변
- 주의사항: URL 은 `Common.URL_*` 상수 그대로 사용. 마트×(방식, URL) 표(4절 18·19)와 대조

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (마트별 sendType·URL 진리표 대조)
- [ ] Part 6: 회귀테스트 (건별/일괄 전송 결과 s/ss/f, 서버 SM_출고계근 적재)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 5: 컴파일 + code-verifier + original-comparator

**Part 1. 분석**
- 메서드: 변경 전체
- 범위: `BixolonShipmentActivity.java` 전체 + `shipment/mode/` 11개 파일
- 용도: ① `compileDebugJavaWithJavac` 성공. ② `grep Common.searchType|SEARCH_TYPE_` 로 Activity 에 남은 분기 확인(남는 것은 로그 출력 324·2811·2893행 및 `ShipmentModeFactory.create(Common.searchType)` 호출 정도여야 함. Mode 내부 라벨 호출 인자의 `Common.searchType` 는 허용). ③ code-verifier: 24개 분기 지점 × 8종 마트의 전/후 경로 대조(5.1·5.2·5.3 표 기준, 기준 커밋 = 착수 직전 HEAD). ④ original-comparator: PDA-INNO(원본)과 기능 동일성(개발 74 의 일괄전송 의도적 차이 제외)
- 주의할 점: Mode 클래스에 `Toast`·위젯·`Activity` 참조가 없는지 확인. 도달 불가 분기 이관 제외가 승인된 항목과 일치하는지 확인

**Part 2. 변환 계획**
- 변환 방식: 서브 에이전트 검증(읽기 전용). 결과 리포트에서 불일치 발견 시 해당 Step 으로 되돌려 수정
- 주의사항: 검증 PASS 전에는 Step 6 으로 넘어가지 않는다

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행 (검증 실행)
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (Mode 메서드 반환값 대조)
- [ ] Part 6: 회귀테스트 (code-verifier PASS / original-comparator PASS)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 6: 통합 테스트 (EDA51 마트별 전 흐름 회귀)

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 이마트(0): 출하대상 → 스캔 → 계근 → 저장 → 라벨(M0/M8/M9) → 재출력 → 건별 전송 → 서버 SM_출고계근 | □ |
| 2 | 이마트 수기 입력(소수1자리 절사)·수입센터 소비기한창·트레이더스 소비기한 필수 | □ |
| 3 | 이마트비정량(4): 중복 바코드 스캔 허용, 라벨, 일괄 전송 + 서버 적재 | □ |
| 4 | 홈플러스(2): 계근 → 홈플 라벨 → 재출력 → 건별 전송(_HOMEPLUS) | □ |
| 5 | 홈플러스비정량(5): 중복 허용, 홈플 라벨, 일괄 전송(_NEW), 저장은 기본 테이블 경로 | □ |
| 6 | 롯데(6): 박스순번 초기화·연속, 라벨, 재출력(BOX_ORDER), 건별 전송(_LOTTE), 수기 입력 시 소비기한창 | □ |
| 7 | 도매(3): 도매 전용 레이아웃, 계근/저장(라벨 없음), 일괄 전송(_NEW) — 데이터 제약 시 코드 동치 검증으로 대체 | □ |
| 8 | 생산(1)·생산라벨(7): 개발/테스트 드롭 방침 → 컴파일+코드 동치(5.1)로 검증, 데이터 확보 시 진입/계근/전송(_PRODUCTION) 확인 | □ |
| 9 | 인쇄 OFF/프린터 미연결, 프린터 설정 ON/OFF 별 BT 확인 흐름(생산 제외 동일) | □ |
| 10 | 변경 전 APK 와 변경 후 APK 의 동일 입력 → 로컬 SQLite·서버 packet·출력 바이트 비교 | □ |

테스트 데이터 제약:
- 이마트·이마트비정량·홈플러스·홈플러스비정량·롯데: 기존 개발 테스트(개발 74·75 및 70 xlsx)와 동일 회사코드 20 데이터 사용.
- 도매(3)·생산(1)·생산라벨(7): 서버 테스트 데이터 존재 여부를 착수 시 확인한다. 없으면 해당 마트는 실기기 대신 "코드 동치(5.1 표) + 컴파일" 로 PASS 처리하고 사유를 기록한다(생산(1)은 방침상 개발/테스트 드롭). 판정 기준: 원본과 동일하면 PASS("원본과 동일" 명시).

### 개발 순서 요약

```
Step 1: 인터페이스·팩토리·Mode 클래스 골격 (신규 파일만)
    ↓
Step 2: 단순 판단형·계산형 분기 이관
    ↓
Step 3: 동작형 분기(재출력·저장·저장 직후 라벨·롯데 박스순번) 이관
    ↓
Step 4: 전송 이관
    ↓
Step 5: 컴파일 + code-verifier + original-comparator
    ↓
Step 6: 통합 테스트 (EDA51 마트별 전 흐름 회귀)
```

---

## 12. 테스트 시나리오

### 시나리오 1: 이마트(0) 정량 전 흐름

```
1. 출하대상받기 → 이마트 계근 진입
2. 상품 바코드 스캔 → BL 스캔 → 계근 저장
3. 라벨 출력(바이트 비교) → 재출력
4. 수기 입력 1.27 입력 → 1.2(절사) 저장 확인, 수입센터 지점이면 소비기한창 표시
5. 전송 → 건별 전송 로그(`goodswet_insert`) → SM_출고계근 적재 확인
```

### 시나리오 2: 비정량(4)·홈플러스비정량(5)

```
1. 동일 바코드를 2회 연속 스캔 → "이미 스캔한 바코드" 없이 계근 저장(두 마트 공통)
2. 이마트비정량: setPrinting 라벨, 홈플비정량: 홈플 라벨
3. 일괄 전송 → `_NEW` URL, 서버 적재 확인(개발 74 동작 유지: 로컬 Y 처리 누락 없음)
```

### 시나리오 3: 홈플러스(2)

```
1. 계근 저장 → TB_GOODS_WET BOX_ORDER(maxBoxOrder) 확인, 홈플 라벨
2. 재출력 → 홈플 라벨
3. 건별 전송 → `_HOMEPLUS` URL
```

### 시나리오 4: 롯데(6)

```
1. 목록 선택 → 박스순번 초기화 로그(lotte_TryCount) 확인
2. 계근 저장 → BOX_ORDER 연속 증가, 9999 초과 시 1 순환(코드 대조)
3. 라벨 → 재출력(BOX_ORDER 포함) → 건별 전송 `_LOTTE`
4. 수기 입력 → 항상 소비기한창
```

### 시나리오 5: 도매(3)·생산(1)·생산라벨(7)

```
1. 도매: 도매 레이아웃 표시, 계근 저장(라벨 없음), 일괄 전송 `_NEW`
2. 생산(1): 인쇄스위치 비활성, BT 확인 없음, setBarcodeMsgProduction 진입, 일괄 전송 `_PRODUCTION`
3. 생산라벨(7): 프린터 확인 함, setPrinting_prod 라벨/재출력, 일괄 전송 `_PRODUCTION`
(데이터 없으면 코드 동치 검증으로 대체)
```

### 시나리오 6: 미지원 searchType / 공통

```
1. Common.searchType 를 "8" 로 강제(디버그)했을 때 기존 else 동작 확인(코드 대조로 대체 가능)
2. 프린터 미연결·인쇄 OFF 상태 각 마트별 동작 변경 전과 동일
```

---

## 13. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 기본 구현 상속 vs 마트별 완전 구현 선택 | 7개 클래스에 기본 구현 복붙 시 중복이 큼 | 권장: DefaultShipmentMode 상속(2.1). 사용자 선호 시 Step 1 에서 복사 방식으로 변경(동작 동일). **결정 필요** |
| 2 | 재출력에서 `arSM.get(current_work_position)` 평가 시점 | 홈플/롯데/prod 분기는 current 를 평가하지 않음 | `ArrayList`+인덱스 전달, setPrinting 경로에서만 `get` (5.5) |
| 3 | 홈플러스비정량(5)의 저장이 홈플(2)과 다름 | 현재 코드 `if(searchType == HOMEPLUS)` 만 홈플 저장 | HomeplusNonfixedMode 는 insertGoodsWet 미override(4절 표 11번 "기본(!)") |
| 4 | 롯데 박스순번 카운터를 Mode 로 이동 | 저장(J)·목록(M) 공유 상태 | 두 분기를 Step 3 에서 함께 이관(분리 시 순번 파손) |
| 5 | 백그라운드 스레드에서 Mode 호출 | M·N 이 AsyncTask.doInBackground | Mode 는 UI 접근 금지 원칙(3.1). 새 동기화 도입 없음 |
| 6 | 도달 불가 분기(N2·N4) 처리 | 바깥 조건이 배타적이라 안쪽 일부 URL 분기는 실행되지 않음 | 이관 제외를 권장하나 "임의 제거 금지" 와 충돌 가능 → **사용자 승인 필요**(대안: Step 4 에서 Activity 원 체인 유지) |
| 7 | 일괄 전송 안 분기별 중복 로그(2912·2915·2918·2919행) | 같은 내용(`send packet 확인` 등)이 2897행과 중복, 분기마다 개별 출력 | 로그 유지 원칙과 충돌하는 **유일한 항목**. 권장: 중복 제거(로그 전용, 동작 무관) — 사용자 승인 필요, 미승인 시 Mode 에 `logBatchSend(packet)` 추가 |
| 8 | 새 searchType 상수 추가 시 팩토리 누락 | 팩토리 if 체인 수동 관리 | 미지원 값은 Default(기존 else)로 안전하게 동작. 팩토리 한 곳 수정으로 끝 |
| 9 | `Common.searchType` 가 onCreate 시점 값으로 고정 | 변경 전에는 매 호출 시 읽음 | 생존 중 변경 경로 없음(1.1). 변경 경로 추가 시 Mode 재생성 필요 |
| 10 | Mode 인자 목록이 길어 호출 실수 가능 | 재출력·저장 직후 라벨의 라벨 메서드 인자 수 | 인자 순서를 현재 호출식과 동일하게 고정, Step 5 라인 대조로 확인(파라미터 객체 신설은 문서 외 구조라 하지 않음) |
| 11 | 생산(1)·도매(3) 실기기 데이터 부재 | 테스트 드롭/데이터 제약 | 코드 동치 + 컴파일로 대체, 사유 기록(Step 6 표) |

---

## 14. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | 인터페이스·팩토리·Mode 클래스 골격 | ⏳ 대기 |
| 2 | 단순 판단형·계산형 분기 이관 | ⏳ 대기 |
| 3 | 동작형 분기(재출력·저장·저장 직후 라벨·롯데 박스순번) 이관 | ⏳ 대기 |
| 4 | 전송 이관 | ⏳ 대기 |
| 5 | 컴파일 + code-verifier + original-comparator | ⏳ 대기 |
| 6 | 통합 테스트 (EDA51 마트별 전 흐름 회귀) | ⏳ 대기 |

---

## 관련 문서

- 개발: `app/doc/개발/74_비정량_일괄전송_Y처리_조기종료_제거[비정량_일괄전송_조기종료_SAVE_TYPE_F잔류_계근순번_중복전송].md` (전송 N3 코드 기준)
- 개발: `app/doc/개발/75_이마트라벨_바코드타입별_메서드_분리.md` (라벨 인터페이스+마트별 클래스 구조 선례, 재출력·저장 직후 라벨의 호출 대상)
- 개발: `app/doc/개발/60_setBarcodeMsg_생산1_전용메서드_분리.md` (생산 `setBarcodeMsgProduction`)
- 개발: `app/doc/개발/64_BixolonShipmentActivity_마트사별_클래스_분리.md`, `app/doc/개발/65_setBarcodeMsg_마트사별_분리.md`, `app/doc/개발/66_BixolonShipmentActivity_소스정리.md` (선행 정리 이력)
- 개발: `app/doc/개발/63_롯데_박스순번_파이프라인_복구[36].md`, `app/doc/개발/69_롯데_LAST_BOX_ORDER_NULL방어_COALESCE[37].md` (롯데 박스순번)
- 소스분석: `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`, `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md`, `app/doc/소스분석/50_바코드_중복검사_우회_searchType별_분기.md`, `app/doc/소스분석/56_홈플러스정량_출하_전체흐름분석.md`, `app/doc/소스분석/58_setBarcodeMsg_생산1_경로분석.md`, `app/doc/소스분석/32_BixolonShipmentActivity_계획.md`
- 수정 리스트: `app/doc/개발/70_테스트후_수정리스트.xlsx`

---

**문서 버전**: 1.0
