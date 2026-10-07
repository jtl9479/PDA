# BixolonShipmentActivity 역할(책임)별 클래스 분리 (스캔 해석·합산 라벨·AsyncTask·다이얼로그)

**작성일**: 2026-10-07
**목적**: `BixolonShipmentActivity.java`(약 2,967줄)에 섞여 있는 **스캔 바코드 해석, 합산 라벨 출력, 서버/DB 통신 AsyncTask, 다이얼로그** 를 역할별 클래스로 옮겨, Activity 는 **화면(위젯·이벤트·생명주기)과 각 클래스 연결**만 맡게 한다 (프로젝트 목적 4 "소스 정리"). 마트별 차이는 **개발 76 의 `ShipmentMode` 가 계속 담당**하며 마트별 복사는 하지 않는다. **동작은 변경 전과 100% 동일**해야 한다 (구조 변경만).

> **요청(사용자 2026-10-07)**: 위 목적. 목표는 1,000줄 안팎이나, 7절 "감소량 산정"에서 보듯 **본 문서의 Phase 만으로는 약 1,650줄까지** 줄어든다. 1,000줄은 11절 "추가 후보(Phase 10 이후)" 까지 해야 근접하며, 그 부분은 상태 결합이 커서 **사용자 결정 게이트**로 둔다.
> **기준 코드**: 개발 74(비정량 일괄전송 조기종료 제거, `jChk` 루프 후 판정)·75(라벨 마트별 패키지)·76(`ShipmentMode`) 이 **반영된 현재 코드**. 행번호는 모두 이 시점 `BixolonShipmentActivity.java` 기준이다.
> **범위 밖**: `ShipmentActivity`(구버전, 진입 불가·삭제 안 함), `ProductionActivity`, `LabelPrintHelper` 내부 로직, `ShipmentMode` 구현 클래스 내부, `DBHandler`, `ProgressDlgShipSearch` 등 파싱 클래스, 어댑터(`ShipmentListAdapter`·`DetailAdapter`).
> **착수 조건**: 개발 76 실기기 테스트 완료 후. Phase 마다 사용자 지시가 있을 때만 진행한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

추가 제약(본 문서): **새로 만드는 코드에 람다 사용 금지**(익명 클래스·일반 클래스·if/else 만). 이동하는 코드의 값·순서·문자열·좌표·예외 전파 방식은 바꾸지 않는다. Toast 문구·Log 문구는 유지한다(TAG 도 기존 `"BixolonShipmentActivity"` 문자열 그대로 사용해 logcat 필터를 유지). 출력 바이트·SQLite 저장값·서버 전송 packet/URL 은 변경 전과 동일. **기존 코드에 이미 있는 람다(전송 버튼·다이얼로그 버튼 등)는 이동하더라도 그대로 옮기며 문법을 바꾸지 않는다.** Activity 참조를 클래스에 통째로 넘기지 않는다(좁은 인터페이스/값 전달). 원본에 있는 결함처럼 보이는 동작(8.4 "보존해야 할 원본 동작")은 **고치지 않고 그대로** 옮긴다.

---

## 1. 현재 구조

### 1.1 대상 파일

`app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java` — 약 2,967줄. 개발 76 으로 마트 분기는 `shipment/mode/ShipmentMode` 로 이미 분리됨. 이 파일 외부에서 `BixolonShipmentActivity` 를 참조하는 곳은 `MainActivity.java:451`(Intent 진입) 1곳과 `shipment/mode/*`·`LabelPrintHelper` 의 **주석/TAG 문자열**뿐이다(grep 확인). Activity 의 public 메서드(`setBarcodeMsg`·`wet_data_insert`·`refresh_delete`·`showAlertDialog` 등)를 **외부 클래스가 호출하는 곳은 없다**(어댑터 `ShipmentListAdapter`·`DetailAdapter` 는 Activity 를 참조하지 않고 `Handler` 만 받음, grep 확인). 단 `ShipmentActivity`(구버전)에는 동명 메서드 사본이 있으나 별개 클래스이며 이번 대상이 아니다.

### 1.2 덩어리별 현황 (실측 행번호)

| 구간 | 행 | 줄수 | 내용 |
|------|:--:|:----:|------|
| 필드·상수 | 61~314 | 약 255 | 위젯·상태 필드·Handler 상수 |
| 생명주기·onActivityResult | 320~593 | 약 275 | onCreate/onStart/onDestroy, 프린터 선택·BT 결과·소비기한 입력 결과 |
| 메뉴·버튼 리스너·스피너 리스너 | 599~877 | 약 280 | input/back/send/select/init 리스너, 스피너 3개 |
| Handler 2개 | 883~1044 | 약 160 | `mHandler`(목록 체크·재출력), `mBixolonHandler`(프린터 상태) |
| `setBarcodeMsg` | 1069~1460 | 392 | 상품/BL 스캔 흐름 + 중량·제조일·박스시리얼 추출 |
| `setBarcodeMsgProduction` | 1469~1728 | 260 | 생산(searchType 1) 전용 사본 |
| `find_PackerProduct*` / `find_work_info*` | 1730~1875 | 146 | 바코드 정보 조회(DB) + 위젯·필드 갱신 |
| 스캔 플래그 | 1877~1890 | 14 | `scanFlag_init`, `set_scanFlag` |
| `wet_data_insert` | 1904~1994 | 91 | 계근 1건 저장 + 화면 갱신 + 라벨 |
| `calc_info`·`refresh_delete`·getter | 1996~2037 | 42 | |
| `show_wetDetailDialog` | 2043~2237 | 195 | 계근 상세 팝업 + 삭제 + **합산 라벨(SLCS)** (2115~2225, 111줄) |
| `deleteQuestionDialog` | 2240~2290 | 51 | |
| `show_sendFinishDialog` / `show_wetFinishDialog` / `showAlertDialog` | 2293~2378 | 86 | |
| `setMessage`(스캐너 입력) / `startExpiryEnter` | 2380~2417 | 38 | |
| **SLCS 헬퍼 복사본** + `sendData` | 2423~2466 | 44 | `slcsInit`·`slcsLabelSize`·`slcsText`(**미사용**)·`slcsPrint`·`slcsFeedToMark` — `LabelPrintHelper` 에 동일 구현 존재 |
| `ProgressDlgShipSelect` | 2477~2637 | 161 | 출하대상 조회 (UI 갱신 포함) |
| `ProgressDlgShipmentSend` | 2644~2882 | 239 | 서버 전송 (`doInBackground` 191줄 2664~2852 + `onPostExecute` 2860~2881) |
| `ProgressDlgPrintConnect` | 2888~2928 | 41 | 프린터 연결 |
| `ProgressDlgDiscon` | 2935~2965 | 31 | 프린터 연결 해제 |

### 1.3 Activity 상태 (분리 시 접근 방식을 정해야 하는 필드)

| 분류 | 필드 (행) | 쓰는 곳 |
|------|----------|---------|
| 목록·위치 | `arSM`(243), `current_work_position`(254), `select_position`(257), `list_send_info`(246), `list_gi_info`(248) | 스캔·저장·Select/Send 태스크·상세 팝업·스피너 |
| 센터 집계 | `centerTotalCount`(260), `centerWorkCount`(263), `centerTotalWeight`(266), `centerWorkWeight`(269) | `wet_data_insert`, `ProgressDlgShipSelect`, 스캔(완료 판정) |
| 작업 상태 | `work_flag`(277), `scan_flag`(284), `work_item_bi_info`(287), `work_ppcode`(290), `work_bl_no`(293), `work_item_fullbarcode`(296), `work_item_barcodegoods`(299) | 스캔·저장·태스크·스피너·다이얼로그 |
| 플래그 | `dialog_flag`(308), `alert_flag`(305), `lastBarcodeProcessedTime`(311), `lastProcessedBarcode`(314) | 스캔·다이얼로그 |
| 죽은 필드 | `expiryDayTrans`(302) | **대입만 2곳(1203, 1596), 읽는 곳 0** — 그대로 둔다(지우지 않음) |
| 위젯 | `edit_barcode`, `edit_product_name`, `edit_product_code`, `edit_center_tcount/tweight`, `edit_wet_count/weight`, `sp_center_name`, `sp_point_name`, `sp_bl_no`, `sList`+`sListAdapter`, `btn_send/btn_input/…` | 전반 |
| 프린터 | `mBixolonPrinter`(132), `mBluetoothAdapter`(129), `cDialog`(165), `pDialog`(162), `mPreviousBixolonState`(135), `labelPrintHelper`(138), `printerCallback`(1047) | 프린터 태스크·Handler·라벨 |
| 진동·사운드 | `vibrator`(153), `sound_pool/sound_success/sound_fail` | 전반 |
| Mode | `mode`(141) | 전반 |

### 문제점

- 한 파일에 화면·스캔 해석·DB 조회·서버 전송·라벨 조립·다이얼로그가 섞여 있어 수정 시 영향 범위를 파악하기 어렵다.
- **SLCS 헬퍼 5개(2425~2452)가 `LabelPrintHelper` 에 이미 있는 동일 구현의 복사본**이다(`slcsInit`·`slcsLabelSize`·`slcsPrint`·`slcsFeedToMark` 는 문자열이 동일, `slcsText` 는 Activity 안에서 호출처 0).
- 중량·제조일·박스시리얼 추출 블록(W/HW 1288~1340, S 1341~1387, J 1388~1396, B 1398~1446)이 `setBarcodeMsg` 안에 약 170줄로 박혀 있고, S 와 B 블록은 로그 문구 한 줄(`Type S` 로 잘못 표기된 B 블록)만 빼고 동일하다. 생산 사본(1659~1714)에도 S/J 가 한 번 더 있다.
- 내부 클래스 AsyncTask 4개(약 470줄)가 Activity 필드를 직접 읽고 쓰므로 단독 테스트·독해가 불가능하다.

---

## 2. 변경 구조

### 2.1 분리 후 책임 지도

```
BixolonShipmentActivity  (화면: 위젯·리스너·생명주기·Handler·각 클래스 연결)
 ├─ shipment/scan/BarcodeParser        순수 계산: 바코드 문자열 + Barcodes_Info → 중량·제조일·박스시리얼   (Phase 2)
 ├─ shipment/scan/BarcodeInfoFinder    바코드 정보(DB) 조회 → 일치 결과 객체                                (Phase 3)
 ├─ print/label/SumLabel               합산 라벨 SLCS 바이트 조립·전송 (LabelPrintHelper 헬퍼 재사용)       (Phase 1)
 ├─ shipment/task/PrintConnectTask     프린터 연결 AsyncTask                                                (Phase 4)
 ├─ shipment/task/PrintDisconnectTask  프린터 해제 AsyncTask                                                (Phase 4)
 ├─ shipment/task/ShipmentSendTask     서버 전송 AsyncTask (doInBackground 이관, 결과는 listener)           (Phase 5)
 ├─ shipment/task/ShipSelectTask       출하대상 조회 AsyncTask (조회·다이얼로그만 이관, 화면 갱신은 listener)(Phase 6)
 ├─ shipment/dialog/WetDetailDialog    계근 상세 팝업 + 삭제 확인 + 합산 버튼(→SumLabel)                    (Phase 7)
 ├─ shipment/scan/ScanState + ScanProcessor  상품/BL 스캔 흐름 (host 인터페이스로 화면 호출)               (Phase 8, 게이트)
 └─ ShipmentMode (개발 76, 변경 없음)  마트별 판단·계산·동작
```

### 2.2 데이터 흐름 (스캔 → 저장, 변경 전/후)

```
[변경 전]  스캐너/입력 → Activity.setBarcodeMsg
              ├ 상품 스캔: find_PackerProduct → find_work_info(위젯 직접 갱신) → ProgressDlgShipSelect
              └ BL 스캔  : 위치 결정 → 중복검사 → 중량/제조일/박스시리얼 추출(인라인 170줄) → wet_data_insert

[변경 후 (Phase 2~3)]  스캐너/입력 → Activity.setBarcodeMsg
              ├ 상품 스캔: BarcodeInfoFinder.find(...) → 결과를 Activity 가 필드·위젯에 반영 → ShipSelectTask
              └ BL 스캔  : 위치 결정 → 중복검사 → BarcodeParser.parse(...) → wet_data_insert (호출 순서·예외 시점 동일)

[변경 후 (Phase 8, 게이트)]  스캐너/입력 → Activity.setBarcodeMsg → ScanProcessor.process(msg)  (state/host 경유)
```

### 2.3 Activity 상태 접근 원칙

| 우선순위 | 방식 | 적용 |
|:--------:|------|------|
| 1 | **값 전달 + 결과 객체 반환** (순수 함수) | BarcodeParser, BarcodeInfoFinder, SumLabel, ShipmentSendTask(doInBackground), ShipSelectTask(doInBackground) |
| 2 | **좁은 listener 인터페이스** (화면 반영·UI 스레드 콜백 몇 개) | 태스크 4종, WetDetailDialog |
| 3 | **상태 보관 객체(`ScanState`) + 좁은 host 인터페이스** — Phase 8 에서만, 사용자 승인 후 | ScanProcessor |
| 금지 | Activity 참조(`BixolonShipmentActivity`/`Activity`)를 클래스에 통째로 전달 | 전 Phase |

- 태스크가 받는 `Context` 는 현재 inner class 가 `mContext` 로 받는 것과 동일한 Activity 컨텍스트이다(`ProgressDialog`·DB 접근용). **Activity 타입이 아닌 `Context` 타입으로만 보유**한다.
- 공유 객체(`arSM` 의 요소, `list_send_info`)는 **참조를 그대로 넘겨 같은 객체를 수정**한다(복사 금지). 그래야 백그라운드에서 `SAVE_CNT`/`SAVE_TYPE` 를 수정한 결과가 `sListAdapter` 에 그대로 보인다.
- `arSM` 필드 **재대입**(`arSM = DBHandler.selectqueryShipment(...)`, 2510)은 Activity 필드이므로 태스크는 새 리스트를 결과로 돌려주고 **Activity 가 필드에 대입**한다(6.1 시점 보존 규칙).

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 | Phase |
|:-:|------|------|----------|:-----:|
| 1 | **SumLabel.java** (신규) | `app/src/main/java/com/rgbsolution/highland_emart/print/label/` | 합산 라벨 SLCS 조립·전송 | 1 |
| 2 | **BixolonShipmentActivity.java** | 2115~2225(합산 클릭), 2423~2454(SLCS 복사본) | 합산 본문 → `SumLabel.print(...)` 호출, SLCS 복사본 5개 삭제 (`sendData` 는 유지) | 1 |
| 3 | **BarcodeParser.java** (신규) | `.../shipment/scan/` | W/HW·S·J·B 중량·제조일·박스시리얼 추출 | 2 |
| 4 | **BixolonShipmentActivity.java** | 1275~1448 | 인라인 추출 → `BarcodeParser` 호출 | 2 |
| 5 | **BarcodeInfoFinder.java** (신규) | `.../shipment/scan/` | `find_work_info`·`find_work_info_barcodeGoods` 의 조회·매칭 | 3 |
| 6 | **BixolonShipmentActivity.java** | 1730~1875, 2558 | 조회 호출 교체 + 결과 반영 메서드 | 3 |
| 7 | **PrintConnectTask / PrintDisconnectTask.java** (신규) | `.../shipment/task/` | 프린터 연결·해제 AsyncTask | 4 |
| 8 | **BixolonShipmentActivity.java** | 481, 500, 532, 2884~2965 | 내부 클래스 삭제, new 호출 교체, listener 구현 | 4 |
| 9 | **ShipmentSendTask.java** (신규) | `.../shipment/task/` | 전송 doInBackground 이관 | 5 |
| 10 | **BixolonShipmentActivity.java** | 726, 2639~2882 | 전송 내부 클래스 삭제, 결과 listener | 5 |
| 11 | **ShipSelectTask.java** (신규) | `.../shipment/task/` | 조회 + 다이얼로그 | 6 |
| 12 | **BixolonShipmentActivity.java** | 1121, 1166, 2080~2086, 2391, 2472~2637 | 조회 호출 교체 + `onPostExecute` 본문을 메서드로 유지 | 6 |
| 13 | **WetDetailDialog.java** (신규) | `.../shipment/dialog/` | 상세 팝업·삭제·합산 버튼 | 7 |
| 14 | **BixolonShipmentActivity.java** | 227~233, 750, 2043~2290 | 상세/삭제 다이얼로그 이관, host 구현 | 7 |
| 15 | **ScanState / ScanProcessor / ScanHost** (신규, **게이트**) | `.../shipment/scan/` | `setBarcodeMsg` 흐름 | 8 |
| 16 | **BixolonShipmentActivity.java** | 1069~1460 + 필드 | `ScanProcessor` 위임 | 8 |
| 17 | (Phase 9) 생산 스캔 이동 | 1469~1728 | ScanProcessor 의 생산 경로 | 9 |

---

## 4. 책임 분류표 (메서드/블록 → 책임 → 이동 대상 → 의존 필드)

| # | 블록 (행) | 책임 | 이동 대상 | 의존하는 Activity 필드·위젯 | 판단 |
|:-:|-----------|------|-----------|-----------------------------|:----:|
| 1 | `show_wetDetailDialog` 합산 클릭 2115~2225 | 라벨(합산) | `SumLabel`(Phase 1) | `list_gi_info`(값), `labelPrintHelper`, `printerCallback.sendData` | **즉시** |
| 2 | SLCS 복사본 2423~2452 | 라벨(중복) | **삭제** → `LabelPrintHelper` 의 public `slcsInit/slcsLabelSize/slcsPrint/slcsFeedToMark` 사용 (`slcsText` 는 호출 0 → 삭제) | 없음 | **즉시** |
| 3 | W/HW·S·J·B 추출 1288~1446 | 스캔 해석(순수) | `BarcodeParser`(Phase 2) | 입력: `work_item_fullbarcode`, `work_item_bi_info`, `arSM[current].ITEM_TYPE/PACKWEIGHT`, `mode` (모두 값). 부수효과: `showAlertDialog("weight",0)`+`alert_flag=true` | **즉시**(알림은 Activity 에 남김, 6.2) |
| 4 | `find_work_info`/`find_work_info_barcodeGoods` 1763~1875, `find_PackerProduct*` 1730~1761 | 스캔 해석 + DB 조회 | `BarcodeInfoFinder`(Phase 3) | 읽기: `mode`, `this`(Context). 쓰기: `work_item_bi_info`, `work_item_barcodegoods`, `edit_product_name/code` | 가능 (결과 객체로 반환) |
| 5 | `ProgressDlgPrintConnect`/`Discon` 2888~2965 | 프린터 연결 | `PrintConnectTask`/`PrintDisconnectTask`(Phase 4) | `cDialog`, `mBluetoothAdapter`, `mBixolonPrinter`(대입 null) | 가능 (listener) |
| 6 | `ProgressDlgShipmentSend.doInBackground` 2664~2852 | 서버 통신 | `ShipmentSendTask`(Phase 5) | `arSM`(참조), `list_send_info`(필드 대입), `mode`, `Common.selectCompanyCode` | 가능 |
| 7 | `ProgressDlgShipmentSend.onPostExecute` 2860~2881 | 화면 반영 | Activity 에 **listener 구현 메서드**로 잔류 | `sListAdapter`, `vibrator`, `show_sendFinishDialog()`, `pDialog` | 잔류 |
| 8 | `ProgressDlgShipSelect` 조회·다이얼로그 2483~2534 | 목록 조회 | `ShipSelectTask`(Phase 6) | `arSM`(재대입), center 집계(onPre 리셋), `mode.onShipmentListLoaded` | 부분 가능 |
| 9 | `ProgressDlgShipSelect.onPostExecute` 2547~2636 | 화면 반영 | Activity 에 **메서드로 잔류** | `arSM`, `sList`, `sListAdapter`, `sp_point_name`, `sp_bl_no`, center 집계, `edit_*`, `work_*`, `vibrator`, `showAlertDialog`, `show_wetFinishDialog`, `set_scanFlag` | **보류(잔류)** |
| 10 | `show_wetDetailDialog`(합산 제외) 2043~2113, 2227~2236 | 다이얼로그 | `WetDetailDialog`(Phase 7) | `dialog_flag`, `sListAdapter.cbStatus`, `edit_barcode`, `edit_wet_*`, `work_flag`, `work_ppcode`, `work_bl_no`, `sp_center_name`, `vibrator`, `mHandler`, `list_gi_info`/`detailAdapter`/`detail_*`(→ 다이얼로그 소유) | 가능 (host 6개) |
| 11 | `deleteQuestionDialog` 2240~2290 + `refresh_delete` 2016~2025 | 다이얼로그 + 집계 | `WetDetailDialog` 내부(Phase 7) | `arSM[select_position]`, `btn_send`, `vibrator`, `detail_btn_back`, `detail_edit_count/weight` | 가능 |
| 12 | `show_sendFinishDialog`/`show_wetFinishDialog` 2293~2340 | 다이얼로그 | **Activity 잔류** (`btn_send`·`work_flag`·`scanFlag_init`·`edit_barcode`·`dialog_flag` 6개 결합, 20줄대 → 이득 < 위험) | — | **보류** |
| 13 | `showAlertDialog` 2343~2378 | 다이얼로그 | **Activity 잔류** (`alert`·`alert_flag`·`Inflater` 필드 결합, 36줄) | — | **보류** |
| 14 | `setBarcodeMsg` 상품/BL 스캔 흐름 1069~1260 | 스캔 흐름 | `ScanProcessor`(Phase 8) | 12개 필드 + 위젯 5종 + 태스크 기동 + 다이얼로그 4종 | **게이트** |
| 15 | `setBarcodeMsgProduction` 1469~1728 | 스캔 흐름(생산) | Phase 9 | 14 와 동일 | **게이트** |
| 16 | `wet_data_insert` 1904~1994 | 계근 저장 + 화면 | **Activity 잔류**(Phase 10 후보) | `arSM`, center 집계, 위젯 6종, `sListAdapter`, `mode`, 라벨 | 11절 |
| 17 | `mHandler`·`mBixolonHandler`·스피너 리스너·`onKey`·`onActivityResult`·`inputBtnListener` | 화면(이벤트·생명주기) | **Activity 잔류** (본래 Activity 책임) | — | 잔류 |
| 18 | `calc_info`·`scanFlag_init`·`set_scanFlag`·`startExpiryEnter`·`sendData` | 화면 보조 | Activity 잔류 (`sendData` 는 `printerCallback` 이 사용) | — | 잔류 |

---

## 5. 신규 클래스 설계

### 5.1 `print/label/SumLabel` (Phase 1) — **타당성 높음**

```java
package com.rgbsolution.highland_emart.print.label;

public class SumLabel {
    /** 36개 단위로 합산 라벨을 만들어 callback.sendData 로 전송. 예외는 호출부와 동일하게 내부에서 잡아 로그. */
    public static void print(LabelPrintHelper helper, ArrayList<Goodswets_Info> list, LabelPrintHelper.PrinterCallback callback)
}
```

- 전달 값: `list_gi_info`(요소의 `getWEIGHT()` 만 사용), 기존 `labelPrintHelper` 인스턴스(`bitmapText`·`slcsInit`·`slcsLabelSize`·`slcsPrint`·`slcsFeedToMark` 는 public 인스턴스 메서드), `printerCallback`(내부에서 `Activity.sendData` 로 연결됨 → 변경 전 `sendData(sumLabel.toByteArray())` 와 동일 경로).
- 돌려주는 값: 없음. `list_gi_info.size()==0` 토스트·진동·`>0` 분기는 호출부(`WetDetailDialog` 또는 Phase 7 전까지 Activity)에 그대로 둔다 — `SumLabel` 은 `>0` 안의 `try {...}` 본문(2130~2219)만 이관.
- **예외 처리 보존**: 본문 안쪽 `catch (Exception e)` 의 `e.printStackTrace()` + `if (Common.D) Log.d(TAG, "setPrinting Exception\n" + ...)` 를 그대로 `SumLabel` 안에 둔다. 바깥쪽 `catch`(`"==== detail_btn_sum Exception ===="`)는 호출부에 남긴다.
- 좌표·폰트 크기·반올림은 **복사만** 한다: `p_hight = 10 + (i/6*50) - (i/36*300)`, `p_weight = 95*(i%6)`, 개별 중량 크기 32, 총 중량 (100,350) 크기 60, `floor(sum*100)/100` → `%.1f` → `Double.toString`, `EUC-KR` 인코딩, 라벨 576x460.

### 5.2 `shipment/scan/BarcodeParser` (Phase 2) — **타당성 높음**

```java
public class BarcodeParser {
    public static class Result {
        public String weightStr = "";      // item_weight_str
        public double weightDouble = 0.0;  // item_weight_double
        public String makingDate = "";     // item_making_date
        public String boxSerial = "";      // item_box_serial
    }
    /** 중량 위치 정보 없음(0) 알림이 필요한지. W/HW/S/B 만 검사, 로그 "weightfrom,to:" 유지 */
    public static boolean isWeightPositionMissing(String itemType, Barcodes_Info bi)
    /** 변경 전 1288~1446 와 동일한 계산. 예외(NumberFormat/StringIndexOutOfBounds/NPE)는 그대로 던진다 */
    public static Result parse(String itemType, String fullBarcode, Barcodes_Info bi, String packWeight, ShipmentMode mode)
}
```

- 전달 값: `arSM.get(current_work_position).getITEM_TYPE()`, `work_item_fullbarcode`, `work_item_bi_info`, `arSM.get(current_work_position).getPACKWEIGHT()`(J 타입), `mode`(`lbToKgFloor`).
- 돌려주는 값: `Result` 4개 필드. 타입이 W/HW/S/J/B 어디에도 해당하지 않으면 기본값(`""`, `0.0`, `""`, `""`)이 그대로 `wet_data_insert` 로 가는 변경 전 동작을 유지한다.
- **구조 보존**: 변경 전과 같이 `if (W||HW) {…} else if (S) {…} else if (J) {…}` 뒤에 **독립 `if (B)`** 를 둔다(`B` 는 앞 체인과 별개 if). W/HW 블록은 `mode.lbToKgFloor` 를 쓰지 않고 `Math.floor(x*item_pow)/item_pow` 를 그대로 쓰며 `%.1f` 로 맺는다. S/B 블록은 `mode.lbToKgFloor` + `%.2f`. 블록별 Log 문구(B 블록의 `Type S` 오표기 포함)도 그대로 둔다.
- **알림 위치 보존(중요)**: 변경 전에는 `weight_from.equals("0") || weight_to.equals("0")` 이면 `showAlertDialog("weight",0); alert_flag = true;` 를 **추출 `substring` 전에** 실행한다. 이후 추출이 예외를 던져도 알림은 이미 떠 있다. 따라서 Activity 가 `parse()` **호출 직전에** `if (BarcodeParser.isWeightPositionMissing(type, bi)) { showAlertDialog("weight", 0); alert_flag = true; }` 를 실행한다. (`parse` 안에서 알림 여부를 결과로 돌려주면 예외 시 알림이 사라져 동작이 달라진다.)
- 문자열 비교 `getMAKINGDATE_FROM() != ""`(참조 비교)는 `equals` 로 바꾸지 않는다 — 변경 전 동작(대부분 `true`)을 유지.

### 5.3 `shipment/scan/BarcodeInfoFinder` (Phase 3) — **타당성 중간**

```java
public class BarcodeInfoFinder {
    public static class Result {
        public String ppCode = "";              // find_work_info 의 반환값 ("null" 포함)
        public Barcodes_Info matchedBi;         // work_item_bi_info 최종값 (없으면 호출 전 값 유지 → null 표시 필요)
        public boolean biChanged;               // matchedBi 를 덮어썼는지
        public String barcodeGoods;             // work_item_barcodegoods 최종값 (마지막 반복의 값)
        public String productName;              // edit_product_name 최종 텍스트 (마지막 반복의 값)
        public String productCode;              // edit_product_code 최종 텍스트
        public boolean widgetTouched;           // 루프가 1회 이상 돌았는지
    }
    public static Result byPackerProduct(Context ctx, String req, boolean type, ShipmentMode mode)   // find_work_info
    public static Result byBarcodeGoods(Context ctx, String req, boolean type)                      // find_work_info_barcodeGoods
}
```

- 변경 전 루프는 **반복마다** 위젯·필드를 쓰고, 일치하지 않는 행에서는 비우므로 **최종 값은 마지막 행의 결과**다(앞서 일치해도 뒤 행이 불일치면 이름·코드는 `""`, `work_item_barcodegoods` 도 `""`; 반면 `work_item_bi_info` 는 일치할 때만 갱신되어 마지막 일치 행이 남음). 결과 객체가 이 **최종 값**을 담고 Activity 가 한 번에 반영한다(UI 스레드에서 연속 대입이라 중간 상태가 화면에 노출되지 않음 → 동치).
- `acceptsAnyBarcodeInfoRow()`(이마트 비정량) 블록은 매 반복 마지막에 무조건 덮어쓰므로 동일 규칙으로 처리.
- **예외 반환값 보존**: `find_work_info` 는 예외 시 `"null"` 반환(`Log.e "======== find_work_info Exception ========"`). 예외 직전까지 이미 위젯/필드에 쓴 값은 남는다 → 예외 시 결과 객체의 **부분 결과를 반영**하도록 Result 에 이미 채운 값을 유지하고 `ppCode="null"` 로 돌려준다.
- 상품 위치 `find_PackerProduct`/`find_PackerProductBarcodeGoods` 의 `!edit_product_name.getText().equals("")`(`Editable.equals(String)` 은 항상 false → **항상 `pp_code` 반환**)는 원본 동작이므로 8.4 에 따라 **그대로 둔다**.
- `ProgressDlgShipSelect.onPostExecute` 2558 의 `find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type)` 도 같은 Finder 를 호출하고 같은 방식으로 반영한다.
- 타당성: DB 조회+매칭은 순수하지만 위젯 3개·필드 2개 반영 규칙이 미묘하다. **Phase 3 에서 반영 로직을 Activity 의 `applyFindResult(Result)` 1곳으로 모은다.**

### 5.4 `shipment/task/PrintConnectTask`, `PrintDisconnectTask` (Phase 4) — **타당성 높음(소폭 이득)**

```java
public class PrintConnectTask extends AsyncTask<Integer, String, Integer> {
    public interface Listener {
        void onConnectDialogCreated(ProgressDialog dialog);   // Activity: cDialog = dialog
    }
    public PrintConnectTask(Context ctx, BluetoothAdapter adapter, BixolonSocketPrinter printer, Listener l)
}
public class PrintDisconnectTask extends AsyncTask<Void, Void, Void> {
    public interface Listener {
        void onDisconnectDialogCreated(ProgressDialog dialog); // Activity: cDialog = dialog
        void onPrinterReleased();                              // Activity: mBixolonPrinter = null (백그라운드 스레드에서 호출됨)
    }
    public PrintDisconnectTask(Context ctx, BixolonSocketPrinter printer, Listener l)
}
```

- `cDialog` 는 `mBixolonHandler`·`onDestroy` 가 읽으므로 **필드는 Activity 에 두고** 태스크는 `onPreExecute` 에서 만든 다이얼로그를 listener 로 넘긴다. **listener 호출은 `show()` 호출 직전/직후와 무관하게 `onPreExecute` 안에서 동기적으로** — `onDestroy` 가 `execute()` 직후 곧바로 `cDialog.isShowing()` 을 검사해 닫기 때문(500~505행).
- `mBixolonPrinter.connect(device)` / `disconnect()` 는 `doInBackground` 안에서 그대로 호출. 연결 태스크에 넘기는 `printer` 는 생성 시점의 `mBixolonPrinter` 참조(변경 전 `doInBackground` 시점에 필드를 읽는 것과 동일하다. 단 `PrintConnect` 는 `mBixolonPrinter` 가 항상 non-null 인 상태에서 기동됨(onStart 475~481, onActivityResult 532)).
- `PrintDisconnectTask` 는 변경 전 `if (mBixolonPrinter != null) mBixolonPrinter.disconnect(); mBixolonPrinter = null;` 를 그대로 수행하되 null 대입만 listener 로 위임.

### 5.5 `shipment/task/ShipmentSendTask` (Phase 5) — **타당성 높음**

```java
public class ShipmentSendTask extends AsyncTask<Void, String, String> {
    public interface Listener {
        void onSendPreExecute(ProgressDialog dialog);                 // Activity: pDialog = dialog
        void onListSendLoaded(ArrayList<Goodswets_Info> listSend);    // Activity: list_send_info = list (doInBackground 중 호출)
        void onSendResult(String result);                             // 기존 onPostExecute 분기 (Toast/진동/show_sendFinishDialog)
    }
    public ShipmentSendTask(Context ctx, ArrayList<Shipments_Info> arSM, ShipmentMode mode, Listener l)
}
```

- `doInBackground` 2664~2852 를 **한 글자도 바꾸지 않고** 이동(건별 PER_ITEM / 일괄 BATCH, packet 구분자 `::`/`##`, 개발 74 의 `jChk` 루프 후 판정, `return "ss"`, `"af"`, `"f"`, `null`). `mode.getSendType()`/`mode.getSendUrl()`/`HttpHelper.getInstance().sendDataDb(packet,"inno","goodswet_insert", url)` 그대로.
- `arSM` 은 **참조 전달**(SAVE_CNT·SAVE_TYPE 수정이 Activity 의 리스트에 반영). 전송 도중 Activity 가 `arSM` 을 재대입하는 경로는 없다(조회는 별도 사용자 조작).
- `list_send_info` 는 변경 전 Activity 필드로 대입된다(2677). 외부에서 읽는 곳은 이 태스크뿐(grep: 필드 사용 6곳 모두 `ProgressDlgShipmentSend` 안) 이지만, **필드는 유지**하고 listener 로 대입해 변경 전 상태와 동일하게 둔다(불필요 필드 삭제는 이번 범위 밖).
- `onPostExecute`: `pDialog.dismiss()` → `_result.equals(...)` 분기. `dismiss` 는 태스크 안에서(자신이 만든 다이얼로그), 결과 분기(Toast·`sListAdapter.notifyDataSetChanged`·`vibrator`·`show_sendFinishDialog`)는 Activity 의 `onSendResult` 로. **`_result` 가 null(doInBackground 예외)일 때 `_result.equals` NPE 가 나는 원본 동작을 유지**(8.4).

### 5.6 `shipment/task/ShipSelectTask` (Phase 6) — **타당성 낮음~중간 (부분 분리, 선택)**

```java
public class ShipSelectTask extends AsyncTask<Integer, String, Integer> {
    public interface Listener {
        void onSelectPreExecute(ProgressDialog dialog);                      // pDialog 보관 + center* 4개 0 리셋 (변경 전 onPreExecute 순서 유지)
        void onShipmentLoaded(ArrayList<Shipments_Info> list);               // arSM = list (null 이면 대입 생략)
        void onSelectPostExecute(boolean type);                              // 기존 onPostExecute(dismiss 이후) 본문 = Activity 메서드
    }
    public ShipSelectTask(Context ctx, String centerName, String condition, boolean type, ShipmentMode mode, Listener l)
}
```

- 이관: 다이얼로그 생성/`dismiss`, `doInBackground`(`selectqueryShipment` + 행별 `selectqueryListGoodsWetInfo` + `mode.onShipmentListLoaded`).
- **잔류**: `onPostExecute` 의 화면 반영 90줄(2549~2635)은 필드·위젯 15종 이상을 만지므로 Activity 메서드(`onShipSelectPostExecute(boolean type)`)로 **그대로 남긴다.** 이득은 약 55줄에 그쳐, 값어치 판단은 사용자 결정(Phase 6 선택).
- `arSM` 대입 시점 보존: 변경 전에는 `doInBackground` 에서 `selectqueryShipment` 가 **반환되자마자** `arSM` 에 대입되고, 이후 행별 루프가 예외를 던져도(catch 에서 로그) `arSM` 은 부분 상태로 남아 `onPostExecute` 가 그 값을 쓴다. 이를 보존하려면 태스크가 `selectqueryShipment` 결과를 받은 직후 `listener.onShipmentLoaded(list)` 를 호출(백그라운드 스레드에서 필드 대입 — 변경 전과 동일 스레드)해야 한다. `selectqueryShipment` 자체가 예외면 대입이 없으므로 `arSM` 은 이전 값 유지.

### 5.7 `shipment/dialog/WetDetailDialog` (Phase 7) — **타당성 중간**

```java
public class WetDetailDialog {
    public interface Host {
        void setDialogFlag(boolean flag);                            // dialog_flag
        void onDetailBack();                                         // 변경 전 detail_btn_back 클릭의 화면 정리 + 재조회 (아래 설명)
        void onItemsDeleted();                                       // vibrator.vibrate(500) + btn_send 비활성
        void vibrate(long ms);
        void toastShort(String msg);
        Handler getHandler();                                        // DetailAdapter 에 넘기는 mHandler
    }
    public WetDetailDialog(Context ctx, LayoutInflater inflater, Host host, LabelPrintHelper helper, LabelPrintHelper.PrinterCallback cb)
    public void show(Shipments_Info si, ArrayList<Shipments_Info> arSM, int selectPosition)   // arSM 은 refresh_delete 용 참조
}
```

- 이관 대상: `detail_edit_count`·`detail_edit_weight`·`detailAdapter`·`detail_btn_back`·`list_gi_info` 필드(1.3 의 위젯 중 팝업 전용) + `show_wetDetailDialog`·`deleteQuestionDialog`·`refresh_delete`. 이 필드들은 팝업 밖에서 쓰이지 않는다(전수 확인: 227~233, 248, 2043~2290).
- `onDetailBack()` 은 변경 전 뒤로가기 람다(2070~2088)의 **팝업 닫기 이후 화면 작업**(`cbStatus` 전부 false, `edit_barcode`/`edit_wet_*` 비움, `work_flag` 별 `ProgressDlgShipSelect` 재조회)을 Activity 가 수행. 팝업 `dismiss()` 와 `dialog_flag=false` 는 순서 보존을 위해 **WetDetailDialog 가 먼저 수행 후 host.onDetailBack() 호출**(변경 전 순서: dismiss → dialog_flag=false → cbStatus → 위젯 → 재조회).
- 삭제 확인 성공 경로(2262~2280)의 **정확한 순서**를 유지: 삭제 for 루프(`deletequerySelectGoodsWet` → `refresh_delete`) → `vibrator.vibrate(500)` → `btn_send` 비활성/배경 변경 → `detail_btn_back.performClick()` → "삭제 성공" Toast. 예외 시 "삭제 실패" Toast. `host.onItemsDeleted()` 가 vibrate+btn_send 두 줄을 수행.
- 합산 버튼은 `SumLabel.print(...)`(Phase 1) 호출 + 빈 목록 토스트/진동.
- 삭제/완료/알림 다이얼로그 중 **`show_sendFinishDialog`·`show_wetFinishDialog`·`showAlertDialog` 는 이관하지 않는다**(4절 #12·#13, 필드 결합 대비 이득 작음).

### 5.8 `shipment/scan/ScanState`·`ScanHost`·`ScanProcessor` (Phase 8, 게이트) — **타당성 낮음 → 사용자 결정 필요**

- `setBarcodeMsg` 의 상품/BL 스캔 흐름은 **상태 변경과 화면 호출이 교차**한다(예: `set_scanFlag(false)` 직후 `lastBarcodeProcessedTime=0; setBarcodeMsg(msg)` 재귀, `work_ppcode`/`work_item_fullbarcode` 대입 후 `ProgressDlgShipSelect.execute`, 다이얼로그 `dialog_flag` 설정 후 `show()`). 결과 객체만 돌려주는 구조로는 이 순서를 보존할 수 없다.
- 따라서 Phase 8 은 **`ScanState`**(POJO: `current_work_position`, `work_*` 5종, `scan_flag`, `dialog_flag`, `alert_flag`, `lastProcessedBarcode`, `lastBarcodeProcessedTime`, `centerTotalCount`, `centerWorkCount`) + **`ScanHost`**(약 14개 메서드: 입력창 설정, 진동, Toast, 계근완료 다이얼로그, 알림 다이얼로그, 선택 센터명으로 `ShipSelectTask` 기동, 다른 상품 확인 다이얼로그, 선택 BL번호 조회, 지점 스피너/리스트 위치 이동, `wet_data_insert` 호출 등)를 전제로 한다. Activity 필드 약 15개를 `state.xxx` 로 치환해야 하므로 변경 지점이 많고 diff 가 크다.
- **판단**: 이득(순감 약 -180줄) 대비 위험이 가장 크다. P1~P7 완료 후 실기기 결과와 남은 줄수를 보고 **사용자가 진행 여부를 결정**한다. 진행 시 아래 6.3 의 보존 규칙을 따른다.
- 대안(저위험): Phase 8 을 하지 않고 `setBarcodeMsg` 의 **순수 판단 2개만** 별도 함수로 추출 — (a) BL 번호로 `arSM` 에서 지점 위치를 찾는 루프(1185~1201), (b) 킬코이/트레이더스 소비기한 필수 조건 판정(1207~1225). 순감 -20줄 정도로 이득이 작아 **기본안에서는 제외**.

### 5.9 Phase 9 — 생산 스캔(`setBarcodeMsgProduction`) 이동 (게이트, Phase 8 선행 시에만)

- 생산(searchType 1)은 개발/테스트 드롭(미사용) 방침이며 본문이 `setBarcodeMsg` 와 **다르다**(개발 60): 비정량 중복검사 우회 없음, 킬코이·트레이더스 소비기한 검사 없음, W/HW/B 타입 없음(S/J 만), LB 환산 항상 `floor(x*100)/100`, 재귀 호출 대상이 자기 자신, 로그 문구가 `setBarcodeMsgProduction` 으로 다름.
- **통합(공통화)은 하지 않는다**(문서에 없는 로직 변경). Phase 8 이후 별도 메서드(`ScanProcessor.processProduction`)로 **복사 이동**만 하고, S/J 추출만 `BarcodeParser` 재사용 가능한지는 Phase 9 Part 1 에서 `parse()` 의 S/J 결과가 바이트 단위로 같은지 확인한 뒤 결정(같으면 재사용, 다르면 이동만).
- Phase 8 을 보류하면 Phase 9 도 보류하고 `setBarcodeMsgProduction` 은 Activity 에 남긴다.

---

## 6. 변경 시 보존해야 할 규칙

### 6.1 비동기 시점 규칙

| 항목 | 변경 전 | 보존 방법 |
|------|---------|----------|
| `arSM` 대입 | 백그라운드 스레드에서 `selectqueryShipment` 반환 즉시 대입, 이후 루프 예외여도 부분 상태 유지 | 5.6 — `onShipmentLoaded` 를 같은 시점·스레드에서 호출 |
| `cDialog`/`pDialog` | `onPreExecute`(UI 스레드)에서 생성·대입·`show` | listener 를 `onPreExecute` 안에서 호출 (`onDestroy` 직후 검사 때문) |
| `mBixolonPrinter = null` | `PrintDisconnect.doInBackground` 안(백그라운드) | listener 를 `doInBackground` 안에서 호출 |
| 센터 집계 리셋 | `ProgressDlgShipSelect.onPreExecute` 에서 다이얼로그 `show` 후, `super.onPreExecute` 앞 | listener 호출 위치 동일 |
| 전송 중 `list_send_info` | 백그라운드에서 필드 대입 | `onListSendLoaded` 를 같은 위치에서 호출 |

### 6.2 알림·예외 순서

- 중량 위치 없음 알림은 추출 계산보다 **먼저** (5.2).
- BL 스캔에서 `current_work_position == -1` 이면 `arSM.get(-1)`(1205행 로그 인자)에서 `ArrayIndexOutOfBounds` 가 먼저 터져 "해당하는 BL상품이 없습니다" 토스트(1228)에 **도달하지 않고** 바깥 `catch` 로그("setBarcodeMsg's BL 스캔 Exception")만 남는다. 이동 시 이 평가 순서를 바꾸지 않는다.
- 예외는 호출부(`setBarcodeMsg` 의 try/catch)에서 잡히던 것을 그대로 Parser/Finder 밖으로 전파한다.

### 6.3 Phase 8 보존 규칙

- 디바운스: `lastBarcodeProcessedTime`/`lastProcessedBarcode` 판정·갱신 위치, 재귀 직전 `lastBarcodeProcessedTime = 0`.
- `edit_barcode.setText(msg)` 는 디바운스 통과 직후·`scan_flag` 분기 앞.
- 다이얼로그 `setPositiveButton` 안의 상태 변경 순서(`dialog_flag=false` → `work_ppcode` → `work_item_fullbarcode` → 태스크 기동), `sp_center_name.getSelectedItem().toString()` 평가 시점(버튼 클릭 시점).
- `sp_point_name.setSelection(current_work_position)` 가 `emartPointSelectedListener`(`calc_info`)를 트리거하는 비동기 타이밍은 **호출 시점이 같으면 동일**하므로 host 에서 그대로 `setSelection` 만 호출.

---

## 7. 감소량 산정 (줄 수, 추정)

| Phase | 작업 | 이동/삭제 | Activity 에 남는 호출·host·listener | 순감소 | 누계 Activity |
|:-----:|------|:---------:|:----------------------------------:|:------:|:-------------:|
| 시작 | | | | | 약 2,967 |
| 1 | SumLabel + SLCS 복사본 삭제 | 111 + 31(slcs 5개 메서드 본문) | 약 10 | **-135** | 약 2,830 |
| 2 | BarcodeParser | 약 172 | 약 30 | **-140** | 약 2,690 |
| 3 | BarcodeInfoFinder | 약 146 | 약 35(`applyFindResult` 등) | **-110** | 약 2,580 |
| 4 | PrintConnect/Disconnect Task | 약 72 | 약 22(listener) | **-50** | 약 2,530 |
| 5 | ShipmentSendTask | 약 215 | 약 30(`onSendResult`) | **-185** | 약 2,345 |
| 6 | ShipSelectTask (선택) | 약 75 | 약 20 | **-55** | 약 2,290 |
| 7 | WetDetailDialog (+삭제·refresh_delete) | 약 240 | 약 55(host) | **-185** | 약 2,105 |
| 8 | ScanProcessor (게이트) | 약 250(+Parser 이관 후 잔여) | 약 75(host 구현·state 치환) | **-180** | 약 1,925 |
| 9 | 생산 스캔 이동 (게이트) | 약 260 | 약 8 | **-250** | 약 1,675 |
| — | 합계 | | | **약 -1,290** | **약 1,650~1,700** |

- **목표 1,000줄에 못 미치는 이유**: 남는 화면 책임이 크다 — 필드·상수 255, 생명주기·onActivityResult 275, 리스너 280, Handler 2개 160, `wet_data_insert`+`calc_info` 등 약 135, 잔류 다이얼로그 3개(86), `ShipSelect.onPostExecute`(90) 등.
- 1,000줄 안팎에는 **11절 추가 후보**(필드 주석 정리 제외 시 약 -250~-350)가 더 필요하나 모두 상태 결합이 커서 본 문서 범위에서는 **보류 + 사용자 결정**으로 둔다.
- 줄 수는 현행 포맷(주석·로그 다수) 기준 추정이며 실제는 Phase 완료 시 `Part 6` 에 실측으로 기록한다.

---

## 8. 사이드이펙트

### 8.1 Activity 외부 영향

| 영향 | 내용 |
|------|------|
| 외부 호출 | `MainActivity.java:451` 의 Intent 진입만 — 클래스명·패키지 불변 |
| `ShipmentMode`(개발 76) | 시그니처 변경 없음. `BarcodeParser`/`ShipmentSendTask`/`BarcodeInfoFinder` 가 `ShipmentMode` 를 받아 호출만 한다 |
| `LabelPrintHelper` | 코드 변경 없음. 단 Activity 의 SLCS 복사본이 사라지고 `SumLabel` 이 헬퍼의 public 메서드를 사용 — **`LabelPrintHelper.slcs*` 메서드의 시그니처·반환 문자열을 바꾸면 합산 라벨이 바뀐다**(75 와 후속 라벨 개발 시 유의) |
| `ShipmentListAdapter`·`DetailAdapter` | 변경 없음(둘 다 Handler 만 받음). `DetailAdapter` 에 넘기는 `mHandler` 는 `WetDetailDialog.Host.getHandler()` 가 같은 객체를 반환 |
| 리소스 | `R.layout.dialog_detailshipment`, `R.layout.list_detailshipment`, `R.style.AppCompatDialogStyle`, `R.drawable.*` 등 참조는 이동하는 클래스에서 `com.rgbsolution.highland_emart.R` 임포트 필요 |
| AndroidManifest/proguard | 변경 없음(신규 클래스는 Activity/Service 가 아님) |

### 8.2 내부 클래스 → 외부 클래스 전환 시 달라지는 점

| 항목 | 내부 클래스(현재) | 외부 클래스(변경 후) | 대응 |
|------|------------------|---------------------|------|
| 필드 접근 | `arSM`, `pDialog` 등 직접 접근 | 접근 불가 | 생성자 인자(참조)·listener |
| `getApplicationContext()` | Activity 메서드 | 없음 | `ctx.getApplicationContext()` 로 동일 컨텍스트(Toast 용은 Activity 쪽 listener 에 남기므로 대부분 불필요) |
| `Log` TAG | `TAG` 필드 | 각 클래스 상수 | 값 `"BixolonShipmentActivity"` 동일 |
| `R.style.*` 접근 | Activity 의 `R` | import 필요 | import |
| 정적 vs 인스턴스 | — | `BarcodeParser`·`SumLabel` 은 static 유틸 | 상태 없음 확인 |
| `Activity.this` 를 컨텍스트로 쓰던 `new AlertDialog.Builder(BixolonShipmentActivity.this, ...)` | Activity | `Context`(Activity 인스턴스 주입) | `ProgressDialog`/`AlertDialog` 는 Activity 토큰 컨텍스트가 필요하므로 `getApplicationContext()` 로 바꾸면 안 됨(8.4) |

### 8.3 생명주기·스레드 위험

| 위험 | 변경 전 | 변경 후 대응 |
|------|---------|-------------|
| Activity 종료 중 AsyncTask 완료 | inner class 가 Activity 를 암묵 참조 → `onPostExecute` 에서 `pDialog.dismiss()`/위젯 접근 | **같은 위험을 동일하게 유지**한다. 별도 `isFinishing()`·취소 처리를 **추가하지 않는다**(동작 변경). 생성자로 받은 `Context` 와 listener 는 Activity 를 강하게 참조하므로 누수 양상도 동일 |
| UI 스레드 접근 | `onPostExecute`/`onPreExecute` 만 위젯 접근, `doInBackground` 는 `arSM`·`DBHandler`·`Http` 만 | listener 를 호출하는 스레드를 6.1 표대로 고정. **백그라운드에서 호출하는 listener 메서드(`onShipmentLoaded`, `onListSendLoaded`, `onPrinterReleased`)는 위젯을 만지지 않는다** |
| `onDestroy` 순서 | `execute()` 직후 `cDialog.isShowing()` 검사 | 6.1 — listener 를 `onPreExecute` 에서 동기 호출 |
| `onActivityResult` fall-through | `REQUEST_ENABLE_BT` case 에 `break` 가 없어 `GET_DATA_REQUEST` 로 흘러내림(578) | **고치지 않는다**(Activity 잔류 코드, 이번 범위 밖) |
| 동시에 두 태스크 | `pDialog` 필드를 Select/Send 가 공유 | `pDialog` 필드·대입 위치 유지(listener 로 대입), 동시 기동 시 마지막 대입이 이기는 동작 동일 |

### 8.4 보존해야 할 원본 동작 (고치지 말 것)

| # | 원본 동작 | 위치 | 비고 |
|:-:|-----------|------|------|
| 1 | `!edit_product_name.getText().equals("")` 는 `Editable.equals(String)` 이라 항상 false → `find_PackerProduct*` 는 항상 `pp_code` 반환 | 1736, 1752 | Finder/메서드 이동 후에도 동일 |
| 2 | `getMAKINGDATE_FROM() != ""` 참조 비교 | 1330, 1336, 1377, 1383, 1435, 1441 | `equals` 로 바꾸지 말 것 |
| 3 | `expiryDayTrans` 는 대입만 있고 읽지 않음 | 302, 1203, 1596 | 필드·대입 유지 |
| 4 | `Goodswets_Info gi = new Goodswets_Info();` 미사용 지역변수(BL 스캔) | 1268, 1639 | 이동 시 불필요 제거 가능하나 **제거하지 않는다**(Phase 8 본문 복사 원칙, 8.4 목록에서 완료 후 Part 6 에 "미사용 변수 유지" 기록) |
| 5 | `if (true) {...} else {...}` 도달 불가 else(BL 번호 불일치 토스트) | 1183, 1449 | 그대로 이동 |
| 6 | `ShipmentSend.onPostExecute` 는 `_result == null`(doInBackground 예외) 일 때 NPE | 2863 | 그대로 |
| 7 | 건별 전송 `case "f"` 즉시 return, 일괄 `result` 분기, `"af"` 처리 | 2747, 2833, 2798 | 개발 74 반영본 그대로 |
| 8 | B 블록 로그 `Type S` 오표기 | 1412, 1432, 1438, 1444 | 문구 유지 |
| 9 | 합산 라벨 `weight_sum` 36개 단위/마지막 페이지 번호 계산 `((i+1)/36)+1` | 2172, 2201 | 그대로 복사 |

---

## 9. 데이터 저장 구조

해당 없음 — 이번 개발은 SQLite·서버 packet·전송 URL 을 **변경하지 않는다**(구조 변경만). 이동하는 데이터 객체와 매핑은 아래와 같다.

### 변수 매핑 (이동 시 이름 대응)

| 변경 전 (Activity 지역/필드) | 변경 후 | 타입 | 비고 |
|------------------------------|---------|------|------|
| `item_weight_str` | `BarcodeParser.Result.weightStr` | String | `wet_data_insert` 의 `weight_str` |
| `item_weight_double` | `Result.weightDouble` | double | `weight_double` |
| `item_making_date` | `Result.makingDate` | String | `making_date` |
| `item_box_serial` | `Result.boxSerial` | String | `box_serial` |
| `item_weight`·`item_pow` | `parse()` 내부 지역 | String/double | 외부 노출 없음 |
| `find_work_info` 반환 | `BarcodeInfoFinder.Result.ppCode` | String | `"null"` 규칙 유지 |
| `list_gi_info`/`detailAdapter`/`detail_*` | `WetDetailDialog` 필드 | — | Phase 7 |
| `pDialog` | Activity 필드 유지 + listener 대입 | ProgressDialog | |
| `cDialog` | Activity 필드 유지 + listener 대입 | ProgressDialog | |

---

## 10. 호출 시점

```
[Activity.onCreate]  mode 생성 → 위젯·리스너 연결
[Activity.onStart]   BT/프린터 확인
    └→ new PrintConnectTask(this, mBluetoothAdapter, mBixolonPrinter, listener).execute()     ← Phase 4
[스캐너/Enter/입력버튼] setMessage / onKey / inputBtnListener → setBarcodeMsg
    ├ 상품 스캔 → BarcodeInfoFinder.byPackerProduct(...)                                      ← Phase 3
    │            → new ShipSelectTask(...).execute()                                          ← Phase 6
    └ BL 스캔   → (알림) BarcodeParser.isWeightPositionMissing → BarcodeParser.parse(...)      ← Phase 2
                 → wet_data_insert → mode.printOnSave
[선택 버튼]         → WetDetailDialog.show(...)                                                ← Phase 7
    ├ 합산 버튼    → SumLabel.print(helper, list_gi_info, printerCallback)                     ← Phase 1
    ├ 삭제 버튼    → 삭제 확인 → refresh_delete → host.onItemsDeleted → 뒤로가기 → 재조회
    └ 뒤로가기     → host.onDetailBack → new ShipSelectTask(...)
[전송 버튼]         → 확인 다이얼로그 → new ShipmentSendTask(this, arSM, mode, listener).execute()  ← Phase 5
[Activity.onDestroy] → new PrintDisconnectTask(this, mBixolonPrinter, listener).execute()      ← Phase 4
```

---

## 11. 추가 후보 (Phase 10 이후, 본 문서 범위 밖 — 사용자 결정)

| 후보 | 줄수 | 보류 사유 |
|------|:----:|-----------|
| `wet_data_insert`(1904~1994)를 저장 서비스로 분리 (`gi` 조립 + `mode.insertGoodsWet` + 수량·중량 집계) 하고 화면 반영만 Activity 에 | 약 -40 | 집계(`centerWork*`)·위젯 6종·`sListAdapter`·라벨 호출이 한 흐름. 순서 보존이 어려움 |
| `mBixolonHandler`(981~1044) → 별도 Handler 클래스 | 약 -45 | `cDialog`·`sound_pool`·`getSupportActionBar()`·`mPreviousBixolonState` 결합 |
| `onActivityResult` 프린터/BT 처리 → 별도 클래스 | 약 -60 | `Common.printer_*`·`swt_print`(상위 클래스 필드)·SharedPreferences·Activity 종료(`finish()`) 결합 |
| `inputBtnListener` 수기 입력 → 별도 클래스 | 약 -40 | 위젯·`startExpiryEnter`(Activity Intent) 결합 |
| 필드 주석·미사용 필드·`import` 정리 | 약 -50 | 행 수 줄이기용 정리는 "문서에 없는 리팩토링" 이므로 별도 승인 필요 |

---

## 12. 개발 플랜

각 Phase 는 **독립 커밋·독립 실기기 검증**이 가능하며 위험이 낮은 순이다. Phase N 완료 후 사용자 지시가 있을 때만 N+1 을 진행한다. 커밋 메시지는 `77_BixolonShipmentActivity_역할별_클래스_분리 ### Step N: …` 형식.

### Step 1: SumLabel 분리 + Activity SLCS 복사본 제거

**Part 1. 분석**
- 메서드: `show_wetDetailDialog` 내 `detail_btn_sum` 클릭 본문, `slcsInit`/`slcsLabelSize`/`slcsText`/`slcsPrint`/`slcsFeedToMark`
- 범위: `BixolonShipmentActivity.java:2115~2225`, `:2423~2454`
- 용도: 합산 라벨 조립을 `LabelPrintHelper` 의 헬퍼를 쓰는 별도 클래스로 옮기고 중복 복사본 제거
- 주의할 점: 5개 헬퍼의 문자열이 `LabelPrintHelper` 와 완전히 같은지 대조(`slcsInit`="CB\r\nCS13,0\r\n", `slcsLabelSize`="SW{w}\r\nSL{h}\r\n", `slcsPrint`="P{n}\r\n", `slcsFeedToMark`="T\r\n" — LabelPrintHelper 132~234행). `slcsText` 는 Activity 내 호출 0 이므로 삭제. `sendData` 는 `printerCallback` 이 사용하므로 **삭제 금지**.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 합산 본문 | 2130~2219 | `SumLabel.print` 로 이동. 안쪽 `try/catch`(printStackTrace + `Common.D` 로그) 포함 |
| 2 | 빈 목록 분기 | 2118~2121 | 호출부에 유지(토스트·진동) |
| 3 | SLCS 복사본 | 2423~2452 | 삭제 |
| 4 | `sendData` | 2456~2466 | 유지 |

**Part 2. 변환 계획**
- 변환 방식: 본문을 복사하되 `slcsXxx()` → `helper.slcsXxx()`, `sendData(x)` → `callback.sendData(x)`, `labelPrintHelper.bitmapText` → `helper.bitmapText`. 변수·좌표·분기 불변.
- 주의사항: 람다 없음. `Common.D` 로그 TAG 문자열 유지. `callback.sendData` 가 `Activity.sendData`(BT 상태 확인·`sendCommandBytes`)로 연결되는 경로가 변경 전과 동일함을 확인.
- **동치 검증(바이트 비교)**: 변경 전/후 동일 입력(예: 중량 1건·36건·37건·72건)에서 `sendData` 직전 `byte[]` 의 SHA-1 을 임시 `Log.i` 로 찍어 비교(검증 후 임시 로그 제거, 커밋 제외). 페이지 분기(36개 단위·마지막 페이지 `((i+1)/36)+1` 번호)를 포함.

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (바이트 SHA 일치: 1건/36건/37건/72건)
- [ ] Part 6: 회귀테스트 (이마트 1건 계근 라벨·합산 라벨 실물 출력, 홈플러스·롯데 합산)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 2: BarcodeParser 분리 (바코드 → 중량·제조일·박스시리얼)

**Part 1. 분석**
- 메서드: `setBarcodeMsg` BL 스캔 분기의 추출 블록 (W/HW 1288~1340, S 1341~1387, J 1388~1396, B 1398~1446)
- 범위: `BixolonShipmentActivity.java:1275~1448`
- 용도: 순수 계산을 정적 유틸로 분리해 단위 검증 가능하게 함
- 주의할 점: 알림(1293~1296 등)은 추출 전에 실행 → `isWeightPositionMissing` 으로 분리(5.2). `parse` 는 `showAlertDialog`·`alert_flag` 를 건드리지 않는다. 블록별 `%.1f`(W/HW) vs `%.2f`(S/B) 와 LB 환산 방식(W/HW 는 `item_pow` floor, S/B 는 `mode.lbToKgFloor`)을 구분. 생산 사본(1659~1714)은 **이번 Step 에서 건드리지 않는다**.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | W/HW | 1288~1340 | `%.1f`, LB 는 pow floor, `item_weight_str` 마지막에 재계산 |
| 2 | S | 1341~1387 | `mode.lbToKgFloor`, `%.2f` |
| 3 | J | 1388~1396 | `PACKWEIGHT` 그대로 |
| 4 | B | 1398~1446 | S 와 동일 계산, 별도 `if` |
| 5 | 호출부 | 1448 | `wet_data_insert(r.weightStr, r.weightDouble, r.makingDate, r.boxSerial)` |

**Part 2. 변환 계획**
- 변환 방식: 지역변수 4개를 `Result` 로 대체. `Double item_weight_double`(박싱) → `double` 이나 `Double.parseDouble`·`Math.floor` 연산 결과는 동일. 타입 비교 문자열은 `Common.ITEM_TYPE_*` 상수 그대로.
- 주의사항: 예외(NumberFormatException·StringIndexOutOfBoundsException·NPE)가 `parse` 밖으로 전파되어 기존 바깥 `catch`(1453)가 같은 로그를 남기게 한다. `getMAKINGDATE_FROM() != ""` 참조 비교 유지.
- **동치 검증**: ① 하이드레이션 입력 표(타입 W/HW/S/J/B × LB/KG × zeropoint 0·1·2 × 제조일/박스시리얼 위치 유/무 × 마트 0·2·3·4·5·6) 를 만들어 변경 전 APK 와 변경 후 APK 의 `wet_data_insert` 인자 로그(`weight_str`, `making_date`, `box_serial`)를 비교. ② code-verifier(Parser 와 변경 전 블록 diff), original-comparator.

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (입력 표 인자 로그 일치)
- [ ] Part 6: 회귀테스트 (이마트 W·S·J·LB, 홈플러스 B, 비정량, 롯데, 트레이더스 소비기한)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 3: BarcodeInfoFinder 분리 (바코드 정보 조회)

**Part 1. 분석**
- 메서드: `find_PackerProduct`(1730), `find_PackerProductBarcodeGoods`(1747), `find_work_info`(1763), `find_work_info_barcodeGoods`(1831), 호출부 1101·1104·2558
- 범위: `BixolonShipmentActivity.java:1730~1875`
- 용도: DB 조회·매칭을 분리하고 위젯/필드 반영은 Activity 한 곳으로
- 주의할 점: 5.3 의 "마지막 반복 값이 최종" 규칙, 예외 시 `"null"`+부분 결과 반영, `mode.acceptsAnyBarcodeInfoRow()` 덮어쓰기, 8.4 #1.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | find_work_info | 1763~1829 | 조회 + 일치 판정 + 위젯/필드 갱신 |
| 2 | find_work_info_barcodeGoods | 1831~1875 | `type`=true 면 `substring`(범위 확인 없음 — 예외 가능) |
| 3 | wrapper | 1730~1761 | `edit_product_name.getText().equals("")` 판정 유지 |

**Part 2. 변환 계획**
- 변환 방식: Finder 는 `ArrayList<Barcodes_Info>` 를 `DBHandler.selectqueryBarcodeInfo`/`selectqueryBarcodeGoodsInfo` 로 읽고 루프 최종값을 `Result` 로 반환. Activity `applyFindResult(Result)` 가 `work_item_bi_info`·`work_item_barcodegoods`·`edit_product_name/code` 에 반영.
- 주의사항: 루프 도중 로그(`BARCODEGOODS FROM/TO`, `TEMP BARCODEGOODS` 등)는 Finder 안에서 동일 순서로 출력. `find_work_info` 는 `type && req.length() >= to` 조건, `find_work_info_barcodeGoods` 는 조건 없이 `type` 만 — 두 메서드의 차이를 **합치지 않는다**.
- **동치 검증**: 바코드 정보 N건(일치 1건·복수 일치·불일치·마지막 행만 불일치) 로컬 DB 로 두 APK 의 `work_ppcode`·위젯 텍스트·`work_item_barcodegoods` 를 로그로 비교. 이마트 비정량(`acceptsAny`) 포함.

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (일치/복수/불일치/마지막 불일치/비정량)
- [ ] Part 6: 회귀테스트 (상품 바코드 스캔, 상품코드(work_flag=2) 스캔, 이마트 비정량)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 4: PrintConnectTask / PrintDisconnectTask 분리

**Part 1. 분석**
- 메서드: `ProgressDlgPrintConnect`(2888~2928), `ProgressDlgDiscon`(2935~2965), 호출부 481·500·532
- 범위: `BixolonShipmentActivity.java:2884~2965`
- 용도: 프린터 연결·해제 AsyncTask 외부화
- 주의할 점: `cDialog` 대입 시점(5.4/6.1), `mBixolonPrinter = null` 백그라운드 대입, `onDestroy` 의 `cDialog.isShowing()` 직후 검사.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | Connect | 2888~2928 | `onPostExecute` 는 빈 본문 — 그대로 비워 둠 |
| 2 | Disconnect | 2935~2965 | `onPostExecute` 없음 |
| 3 | 호출부 | 481, 500, 532 | `new Task(...).execute()` 로 교체 |

**Part 2. 변환 계획**
- 변환 방식: 5.4 의 listener 방식. 다이얼로그 제목/메시지/스타일 문자열 그대로.
- 주의사항: 연결 태스크에 `mBixolonPrinter` 참조를 생성 시점에 넘기므로 481·532 호출 시점에 non-null 임을 확인. 기존 `Common.printer_address` 로 `getRemoteDevice` 하는 위치(doInBackground)는 그대로.
- **동치 검증**: 실기기에서 프린터 연결 성공/실패(전원 OFF)·Activity 종료 시 해제 다이얼로그 표시·재진입 시 재연결 확인, logcat 의 `print_address ::` 로그 확인.

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (연결 성공·실패·종료 해제)
- [ ] Part 6: 회귀테스트 (프린터 첫 선택 → 연결, 재진입 자동 연결, 생산(1) 은 프린터 미연결)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 5: ShipmentSendTask 분리 (서버 전송)

**Part 1. 분석**
- 메서드: `ProgressDlgShipmentSend.doInBackground`(2664~2852), `onPostExecute`(2860~2881), 호출부 726
- 범위: `BixolonShipmentActivity.java:2639~2882`
- 용도: 서버 통신 AsyncTask 외부화 (가장 큰 단일 덩어리)
- 주의할 점: 개발 74 의 일괄 전송 `jChk` 루프 후 판정(2838~2844), 건별 `return "ss"`(2741), packet 구분자, `Common.selectCompanyCode`, `result` 문자열 정리(`\r\n`/`\n` 제거), 8.4 #6·#7.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 건별 PER_ITEM | 2683~2751 | `sendDataDb(... mode.getSendUrl())`, `updatequeryGoodsWet`, `SAVE_CNT`, `updatequeryShipment`, `jChk` |
| 2 | 일괄 BATCH | 2752~2845 | packet `##` 조립, `sendOrNot`, `"af"`, 루프 후 `jChk>0 && jChk==arSM.size()` → `"ss"` |
| 3 | onPostExecute | 2860~2881 | 결과별 Toast/진동/`show_sendFinishDialog` → listener |

**Part 2. 변환 계획**
- 변환 방식: `doInBackground` 본문을 그대로 이동(변수명·로그·순서 불변). `arSM` 은 생성자 참조, `list_send_info` 대입은 `listener.onListSendLoaded`, `publishProgress` 는 그대로(`onProgressUpdate` 는 `super` 만 호출하므로 빈 동작 유지).
- 주의사항: `Context` 는 `mContext` 와 동일 컨텍스트(`DBHandler` 호출용). 결과 분기의 `Toast.makeText(getApplicationContext(), ...)` 는 Activity 쪽(`onSendResult`)에서 수행해 컨텍스트 동일 유지.
- **동치 검증**: 변경 전/후 APK 로 같은 계근 데이터를 로컬 DB 에 두고 전송 → 서버 수신 packet 로그(`Send Packet`/`send packet 확인`), `result`, 로컬 `SAVE_TYPE`·`SAVE_CNT`, 마지막 Toast 비교. 건별(0·2·6)·일괄(3·4·5) 모두. 부분 전송 실패(서버 `f`) 경로 포함. code-verifier 로 doInBackground 라인 diff 가 `listener` 호출 3곳 외 0 인지 확인.

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (packet·URL·결과 문자열 일치)
- [ ] Part 6: 회귀테스트 (이마트·홈플러스·롯데 건별, 도매·비정량·홈플러스비정량 일괄, 전체 전송 완료 "ss" 다이얼로그, 재전송 시 "af")

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 6: ShipSelectTask 분리 (출하대상 조회, 선택 Phase)

**Part 1. 분석**
- 메서드: `ProgressDlgShipSelect`(2477~2637), 호출부 1121·1166·2080·2084·2086·2391
- 범위: `BixolonShipmentActivity.java:2472~2637`
- 용도: 조회·다이얼로그만 이관, 화면 갱신은 Activity 메서드로 잔류 (이득 약 -55줄 — 가치 판단은 사용자)
- 주의할 점: 5.6, 6.1 `arSM` 대입 시점, `onPreExecute` 센터 집계 리셋 순서, `onPostExecute` 의 `if (!this.type) work_ppcode = find_work_info(...)`(Step 3 이후 Finder 사용).

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | onPreExecute | 2490~2504 | 다이얼로그 → center 4개 리셋 |
| 2 | doInBackground | 2506~2534 | 조회·행별 집계·`mode.onShipmentListLoaded` |
| 3 | onProgressUpdate | 2536~2544 | 그대로 |
| 4 | onPostExecute | 2546~2636 | `dismiss` 후 본문은 Activity 메서드 |

**Part 2. 변환 계획**
- 변환 방식: 5.6 listener 3개. `onPostExecute` 본문은 `onShipSelectPostExecute(boolean type)` 으로 **그대로 이동**(필드 직접 접근 유지, 같은 Activity 안).
- 주의사항: 호출부 6곳의 인자(센터명 `sp_center_name.getSelectedItem().toString()`, 코드, `type`) 평가 시점 동일. 예외로 인한 `arSM` 부분 대입 동작 보존.
- **동치 검증**: 상품 스캔 조회·BL(수기) 조회·조회 결과 없음·상세 팝업 뒤로가기 재조회 4경로에서 `arSM.size()`, 센터 집계 위젯, 스피너 선택 위치 비교. 롯데는 `mode.onShipmentListLoaded` 의 박스순번(`lotte_TryCount`) 초기화 값 로그 비교.

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (4경로 + 롯데 박스순번)
- [ ] Part 6: 회귀테스트 (이마트·홈플러스·롯데·도매·비정량 조회 후 첫 미완료 지점 선택, 총 계근 완료 다이얼로그)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 7: WetDetailDialog 분리 (계근 상세·삭제·합산 버튼)

**Part 1. 분석**
- 메서드: `show_wetDetailDialog`(2043~2237), `deleteQuestionDialog`(2240~2290), `refresh_delete`(2016~2025), 호출부 750
- 범위: `BixolonShipmentActivity.java:227~233, 2016~2025, 2043~2290`
- 용도: 상세 팝업 전용 필드·로직을 한 클래스로
- 주의할 점: 5.7 순서(삭제 성공 경로·뒤로가기 순서), `detail_btn_back.performClick()` 이 `dialog_flag=false`·재조회를 유발, `select_position`·`arSM` 참조, `mHandler`(DetailAdapter), `selectBtnListener` 의 `show_wetDetailDialog(arSM.get(getSelect_Position()), work_item_bi_info, getSelect_Position())` 호출 인자(`bi`·`position` 은 현재 **본문에서 사용되지 않음** — 시그니처 유지 여부는 Part 2 에서 결정, 유지 시 `bi`/`position` 은 무시).

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 팝업 필드 | 227~233, 248 | `detail_*`, `detailAdapter`, `list_gi_info` → 다이얼로그 소유 |
| 2 | 뒤로가기 | 2070~2088 | dismiss → `dialog_flag=false` → `cbStatus` false → 위젯 비움 → 재조회 |
| 3 | 삭제 | 2090~2113, 2240~2290 | 선택 항목 수집 → 확인 → 삭제 → 집계 갱신 |
| 4 | 합산 | 2115~2225 | Step 1 의 `SumLabel` 호출만 남음 |
| 5 | 목록 로드 | 2227~2232 | `selectqueryGoodsWet` + `DetailAdapter(…, mHandler)` |

**Part 2. 변환 계획**
- 변환 방식: 5.7 `WetDetailDialog` + `Host`. 람다는 기존 그대로 복사 가능하나 **새 코드에는 익명 클래스** 사용 원칙에 따라 이동하면서 새로 쓰는 리스너는 `View.OnClickListener` 익명 클래스로 작성하지 말고 **기존 람다를 그대로 이동**(문법 변경 금지 규칙 우선) — 결정: 이동 코드는 문법 유지.
- 주의사항: `AlertDialog.Builder(this, R.style.AppCompatDialogStyle)` 컨텍스트는 Activity 인스턴스(`Context` 타입 주입). `ListView`/`EditText` 조회는 팝업 레이아웃(`dialog_detailshipment`) id 그대로.
- **동치 검증**: 팝업 열기 → 목록 표시, 항목 체크 삭제(1건·복수), 삭제 후 수량/중량 갱신·전송 버튼 비활성, 뒤로가기 후 `work_flag` 0/1/2 별 재조회, 합산 라벨 출력, 항목 없을 때 토스트. 변경 전 APK 대비 화면 동일(스크린샷) + 로컬 DB 행 수 동일.

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (삭제·합산·뒤로가기 경로)
- [ ] Part 6: 회귀테스트 (이마트 상세 삭제 후 재계근, 합산 라벨, 홈플러스·롯데 재출력(DetailAdapter→MESSAGE_REPRINT) 동작)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 8: ScanProcessor 분리 (스캔 흐름) — **게이트: Step 1~7 결과 확인 후 사용자 승인 시에만**

**Part 1. 분석**
- 메서드: `setBarcodeMsg`(1069~1460, Step 2·3 이후 약 260줄)
- 범위: `BixolonShipmentActivity.java:1069~1460`
- 용도: 상품/BL 스캔 흐름을 `ScanProcessor` + `ScanState` + `ScanHost` 로
- 주의할 점: 5.8·6.3. `ScanState` 로 옮길 필드 15개의 읽기/쓰기 지점 전수(스피너 리스너·Handler·`wet_data_insert`·다이얼로그·태스크 listener)를 grep 으로 목록화한 뒤 진행. 진행 전 `상태 치환 지점 수` 와 diff 크기를 사용자에게 보고.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 디바운스 | 1080~1090 | 상태 2개 |
| 2 | 상품 스캔 | 1095~1176 | find → 태스크 기동/중복검사/다른 상품 다이얼로그 |
| 3 | BL 스캔 | 1177~1456 | 위치 결정 → 소비기한 필수 검사 → 중복검사 → Parser → `wet_data_insert` |

**Part 2. 변환 계획**
- 변환 방식: `ScanState` 에 상태 이전, Activity 필드 접근을 `state.xxx` 로 치환(이 치환 자체를 먼저 별도 커밋으로 검증: 동작 불변 확인 후 흐름 이동). `ScanHost` 구현은 Activity 내부 익명/멤버 클래스.
- 주의사항: 6.2·6.3, 8.4 #4·#5. 재귀 호출은 `ScanProcessor.process(msg)` 자기 호출.
- **동치 검증**: 상품 스캔 6경로(코드 없음/최초/같은 상품 신규/같은 상품 중복/다른 상품 Yes·No) × BL 스캔 경로(소비기한 필수 걸림/BL 없음/계근 끝난 지점/중복/정상 저장) × 마트 6종의 로그 시퀀스(`Log.e` 문구) diff. 디바운스(1초 내 동일 바코드) 확인.

**체크리스트**
- [ ] Part 1: 분석 완료 확인 (치환 지점 전수 목록)
- [ ] Part 2: 변환 계획 확인 (사용자 승인)
- [ ] Part 3: 변환 수행 (3-1 상태 치환 커밋 / 3-2 흐름 이동 커밋)
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트 (경로별 로그 시퀀스 diff)
- [ ] Part 6: 회귀테스트 (전 마트 스캔 → 계근 → 라벨 → 전송)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 9: 생산 스캔(setBarcodeMsgProduction) 이동 — **게이트: Step 8 완료 시에만**

**Part 1. 분석**
- 메서드: `setBarcodeMsgProduction`(1469~1728)
- 범위: `BixolonShipmentActivity.java:1469~1728`
- 용도: Step 8 의 `ScanProcessor` 에 생산 경로를 **복사 이동**(통합 금지)
- 주의할 점: 5.9. 생산은 미사용 방침(searchType=1) — 이동 후 검증 비용 대비 가치 판단은 사용자. 개발 60 의 차이점 6가지 유지.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 상품 스캔 | 1489~1569 | 비정량 우회 없음, 재귀 자기호출 |
| 2 | BL 스캔 | 1570~1724 | 킬코이/트레이더스 검사 없음, S/J 만 |

**Part 2. 변환 계획**
- 변환 방식: Step 8 과 같은 state/host 로 이동. S/J 추출은 `BarcodeParser.parse` 가 변경 전 생산 블록과 동일 결과인지 확인 후 재사용(5.9).
- 주의사항: 로그 문구 `setBarcodeMsgProduction ...` 유지, `ProductionMode.lbToKgFloor`(=`floor(x*100)/100`) 와 생산 블록 인라인 `Math.floor(temp*100)/100` 동치 확인(ProductionMode.java:71~73).
- **동치 검증**: searchType=1 으로 진입(MainActivity 경로 또는 테스트 하네스)해 BL 스캔 → 저장 → 일괄 전송(`URL_INSERT_GOODS_WET_PRODUCTION`) 로그 비교. 미사용 방침상 **컴파일 + code-verifier 비교만으로 갈음할지 사용자 결정**.

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

### Step 10: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 이마트(0) 상품 스캔 → BL 스캔 → 계근 저장 → 라벨 출력 → 합산 라벨 → 전송(건별) | □ |
| 2 | 이마트 비정량(4) 일괄 전송 + 중복 바코드 재스캔 허용 | □ |
| 3 | 홈플러스(2) 재출력(setHomeplusPrinting)·건별 전송 | □ |
| 4 | 홈플러스 비정량(5) 일괄 전송 | □ |
| 5 | 도매(3) 전용 레이아웃·일괄 전송 | □ |
| 6 | 롯데(6) 박스순번·소비기한 입력 화면·건별 전송 | □ |
| 7 | 수기 입력(work_flag=0): 이마트 소수 1자리 버림, 킬코이/미트센터·수입육 소비기한 입력 | □ |
| 8 | 상품코드 입력(work_flag=2) | □ |
| 9 | 상세 팝업 삭제·뒤로가기·합산 | □ |
| 10 | 프린터 연결 실패/성공/종료 해제, 전송 실패(서버 `f`) | □ |
| 11 | 전 Phase 변경 전 APK 대비 logcat 시퀀스·로컬 DB·서버 packet 일치 (code-verifier + original-comparator) | □ |

---

### 개발 순서 요약

```
Step 1: SumLabel + SLCS 복사본 제거        (-135, 바이트 SHA 비교)
    ↓
Step 2: BarcodeParser                       (-140, 입력 표 로그 비교)
    ↓
Step 3: BarcodeInfoFinder                   (-110)
    ↓
Step 4: PrintConnectTask / DisconnectTask   (-50)
    ↓
Step 5: ShipmentSendTask                    (-185, packet·URL·결과 비교)
    ↓
Step 6: ShipSelectTask (선택)               (-55)
    ↓
Step 7: WetDetailDialog                     (-185)
    ↓  ── 여기까지 약 -860줄 (Activity 약 2,105줄) ──
Step 8: ScanProcessor (게이트)              (-180)
    ↓
Step 9: 생산 스캔 이동 (게이트)             (-250)
    ↓
Step 10: 통합 테스트
```

---

## 13. 테스트 시나리오

### 시나리오 1: 합산 라벨 바이트 동일 (Step 1)

```
1. 변경 전 APK 에서 임시 로그로 sendData 직전 byte[] SHA-1 출력
2. 이마트 상세 팝업 → 합산 클릭 (계근 1건 / 36건 / 37건 / 72건 각각)
3. 변경 후 APK 에서 같은 데이터로 반복 → SHA-1·출력물 육안 동일 확인
```

### 시나리오 2: 중량 추출 인자 동일 (Step 2~3)

```
1. 이마트 W(LB/KG)·S·J, 홈플러스 B, 롯데, 비정량 바코드 각각을 변경 전/후 APK 에서 스캔
2. wet_data_insert 인자 로그(weight_str·making_date·box_serial)와 위젯(상품명·코드) 비교
3. 중량 위치 정보 0 인 바코드: 알림 다이얼로그가 뜨는 시점·후속 예외 로그 동일 확인
```

### 시나리오 3: 전송 동일 (Step 5)

```
1. 계근 데이터를 만든 뒤 변경 전/후 APK 로 각각 전송 (건별 3종, 일괄 3종)
2. Send Packet 로그, 서버 적재 건수, 로컬 SAVE_TYPE/SAVE_CNT, 최종 Toast/다이얼로그 비교
3. 서버 오류 상황(f) 과 이미 전송된 경우(af) 확인
```

### 시나리오 4: 프린터 연결·종료 (Step 4)

```
1. 프린터 ON 상태 진입 → 자동 연결·Connected 토스트·성공음
2. 프린터 OFF 상태 진입 → 실패 토스트·실패음
3. 뒤로가기 종료 시 해제 다이얼로그 후 정상 종료
```

### 시나리오 5: 상세 팝업 (Step 7)

```
1. 지점 체크 → 선택 → 상세 팝업 → 일부 항목 체크 삭제 → 수량/중량 갱신·전송 버튼 비활성
2. 뒤로가기 → work_flag 별 재조회 → 센터 집계 복원
3. 합산 버튼·재출력(DetailAdapter 의 MESSAGE_REPRINT) 동작
```

### 시나리오 6: 스캔 흐름 (Step 8, 승인 시)

```
1. 상품 스캔: 없는 바코드 / 최초 / 같은 상품 / 중복 / 작업 중 다른 상품(예·아니오)
2. BL 스캔: BL 없음 / 계근 끝난 지점 / 중복 / 소비기한 필수 걸림 / 정상 저장
3. 1초 내 동일 바코드 재스캔 무시(디바운스)
4. 변경 전/후 logcat(Log.e 문구) 시퀀스 diff
```

---

## 14. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 중량 위치 알림이 뜨지 않거나 늦게 뜸 | Parser 가 알림 여부를 결과로 반환하도록 잘못 설계 | 5.2 — `parse()` **전에** `isWeightPositionMissing` 로 알림 실행 |
| 2 | 상품명/코드 위젯 값이 달라짐 | 변경 전은 반복마다 위젯 갱신, 최종값=마지막 행 | 5.3 — Finder 가 루프 최종값을 Result 로, 예외 시 부분 결과 반영 |
| 3 | `arSM` 이 조회 실패 시 달라짐 | `selectqueryShipment` 직후 대입 시점·예외 시 부분 상태 | 5.6/6.1 — `onShipmentLoaded` 호출 시점 고정, null 이면 대입 생략 |
| 4 | Activity 종료 직후 `onPostExecute` 에서 크래시 양상 변화 | listener/Context 보유 방식 차이 | 8.3 — 추가 방어 코드 넣지 않고 변경 전과 동일 구조(Activity 강참조)로 유지 |
| 5 | `onDestroy` 에서 해제 다이얼로그가 즉시 닫히지 않음/닫혀버림 | `cDialog` 대입 시점이 `execute()` 이후로 밀림 | 6.1 — `onPreExecute` 동기 listener |
| 6 | 전송 중 `SAVE_CNT` 가 화면에 반영되지 않음 | `arSM` 요소를 복사해 전달 | 참조 전달 (2.3) |
| 7 | 합산 라벨 출력이 달라짐 | `LabelPrintHelper.slcs*` 와 Activity 복사본의 미세 차이 | Step 1 Part 1 에서 5개 메서드 문자열 대조 + 바이트 SHA 비교 |
| 8 | 삭제 후 화면/버튼 상태 순서 차이 | 삭제 성공 경로의 vibrate→btn_send→performClick→Toast 순서 변경 | 5.7 — 순서 고정, host 메서드 순서 호출 |
| 9 | 컴파일 오류: 외부 클래스에서 `R`·`BixolonSocketPrinter` 등 참조 | 내부 클래스 암묵 접근 해제 | import 추가, 필요한 값만 생성자 주입 |
| 10 | 상태 치환(Step 8) 누락으로 필드와 `ScanState` 값 불일치 | 15개 필드 읽기/쓰기 지점이 여러 파일 구역에 분산 | 치환 커밋을 별도로 두고 전수 grep 목록 대조, 기능 변경 없이 먼저 검증 |
| 11 | 새로 만든 코드에 람다가 섞임 | 습관적 변환 | 이동 코드는 문법 유지, 신규 코드는 익명 클래스/일반 클래스 (제약 조건) |
| 12 | 목표 1,000줄 미달 | 화면 책임(필드·생명주기·리스너·Handler)이 약 1,100줄 | 7·11절 — 사용자 결정으로 Phase 10 후보 추가 여부 판단 |

---

## 15. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | SumLabel 분리 + Activity SLCS 복사본 제거 | ⏳ 대기 |
| 2 | BarcodeParser 분리 | ⏳ 대기 |
| 3 | BarcodeInfoFinder 분리 | ⏳ 대기 |
| 4 | PrintConnectTask / PrintDisconnectTask 분리 | ⏳ 대기 |
| 5 | ShipmentSendTask 분리 | ⏳ 대기 |
| 6 | ShipSelectTask 분리 (선택) | ⏳ 대기 |
| 7 | WetDetailDialog 분리 | ⏳ 대기 |
| 8 | ScanProcessor 분리 (게이트) | ⏳ 대기 |
| 9 | 생산 스캔 이동 (게이트) | ⏳ 대기 |
| 10 | 통합 테스트 | ⏳ 대기 |

---

## 16. 관련 문서

- `app/doc/개발/76_BixolonShipmentActivity_마트별_처리클래스_분리.md` — `ShipmentMode` 설계(마트 분기), 본 문서의 선행
- `app/doc/개발/75_이마트라벨_바코드타입별_메서드_분리.md` — 라벨 클래스·`LabelPrintHelper` 구조
- `app/doc/개발/74_비정량_일괄전송_Y처리_조기종료_제거[비정량_일괄전송_조기종료_SAVE_TYPE_F잔류_계근순번_중복전송].md` — 일괄 전송 `jChk` 루프 후 판정 (Step 5 보존 대상)
- `app/doc/개발/60_setBarcodeMsg_생산1_전용메서드_분리.md` — `setBarcodeMsgProduction` 분리 이력 (Step 9)
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`, `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md` — 조회→계근→전송 흐름
- 오류 문서 없음 (구조 개선 개발)

---

**문서 버전**: 1.0
