# setBarcodeMsg() 생산(searchType=1) 실행 경로 분석

**작성일**: 2026-08-04
**대상**: `BixolonShipmentActivity.java:1142~1539` (397행)
**목적**: 생산 전용 메서드(`setBarcodeMsgProduction()`) 분리를 위해, 생산(1)이 실제로 지나는 분기와 통과하지 않는 분기를 전수 확정한다.

---

## 1. 분석 대상

| 항목 | 내용 |
|------|------|
| 메서드 | `public void setBarcodeMsg(final String msg)` |
| 위치 | `BixolonShipmentActivity.java:1142` |
| 호출 경로 ① | Honeywell 스캐너 → `HoneywellScannerActivity.m_brc.onReceive():217` → `setMessage():246` → `BixolonShipmentActivity.setMessage():1077` → **`setBarcodeMsg()`** |
| 호출 경로 ② | [입력] 버튼 → `inputBtnListener.onClick():629` (`work_flag == 1` 또는 `2`) |
| 호출 경로 ③ | 자기 재귀 `:1220` (1차 스캔 성공 후 2차로 넘길 때) |
| 공유 대상 | searchType 0·1·2·3·4·5·6·7 **전부** |

---

## 2. 메서드 전체 구조

```
setBarcodeMsg(msg)
 ├─ [A] dialog_flag 체크                          :1143
 ├─ [B] 디바운스 (동일 바코드 1초)                  :1147~1157
 ├─ [C] edit_barcode.setText(msg)                 :1161
 │
 ├─ [1차] if (scan_flag)  ── 패커상품 스캔          :1162
 │    ├─ work_flag==1 → find_PackerProduct()      :1169
 │    │  work_flag!=1 → find_PackerProductBarcodeGoods()  :1173
 │    ├─ "null" → 오류 토스트 + 진동                :1179
 │    └─ 정상
 │        ├─ work_ppcode=="" (최초)                :1184
 │        │     └─ ProgressDlgShipSelect 실행       :1192
 │        ├─ work_ppcode==find_ppcode (동일상품)    :1193
 │        │     ├─ duplicatequeryGoodsWet_check()  :1196
 │        │     ├─ ★ 비정량(4,5) → dup=false        :1198
 │        │     ├─ dup → "이미 스캔한 바코드" return :1202
 │        │     └─ set_scanFlag(false) → 재귀 호출  :1211, :1220
 │        └─ 다른 상품 → AlertDialog 확인            :1222
 │
 └─ [2차] else  ── BL 스캔                         :1248
      ├─ BL번호로 current_work_position 검색        :1256~1270
      ├─ ★ 킬코이 미트센터 소비기한 필수 검증         :1278
      ├─ ★ CENTERNAME TRD/E/T/WET + 이마트 소비기한  :1286
      ├─ current_work_position==-1 → 오류 return
      ├─ 계근 완료 확인 → show_wetFinishDialog()
      ├─ duplicatequeryGoodsWet() 4키 중복검사       :1319
      ├─ ★ 비정량(4,5) → dup=false                  :1322
      ├─ dup → "이미 스캔한 바코드" return
      ├─ 중량 추출 (ITEM_TYPE 분기)
      │     ├─ W / HW                              :1359
      │     ├─ S                                   :1412
      │     ├─ J                                   :1463
      │     └─ B                                   :1474
      └─ wet_data_insert()                         :1527
```

> **2단계 스캔 구조** — 최초 스캔은 상품 확정(1차)만 하고, `ProgressDlgShipSelect.onPostExecute`가 `set_scanFlag(false)`를 호출해 이후 스캔이 2차(BL) 경로로 들어간다. `:1211`·`:1220`은 동일 상품 재스캔 시 1차→2차를 즉시 이어붙이는 재귀다.

---

## 3. 생산(1) 분기별 판정

### 3.1 무조건 통과 (공통 로직)

| 구간 | 위치 | 생산 |
|------|:----:|:----:|
| `dialog_flag` 체크 | `:1143` | ✅ 필요 |
| 디바운스 (동일 바코드 1초) | `:1147~1157` | ✅ 필요 (개발56) |
| `edit_barcode.setText()` | `:1161` | ✅ 필요 |
| 1차 상품 스캔 전체 | `:1162~1247` | ✅ 필요 |
| BL번호 매칭 | `:1256~1270` | ✅ 필요 |
| `current_work_position` 검증 | `:1298~1305` | ✅ 필요 |
| 계근 완료 확인 | `:1310~1317` | ✅ 필요 |
| 4키 중복검사 | `:1319` | ✅ 필요 |
| `wet_data_insert()` | `:1527` | ✅ 필요 |

### 3.2 생산 미해당 (조건 불성립)

| # | 분기 | 위치 | 생산 값 | 판정 근거 |
|:-:|------|:----:|--------|----------|
| 1 | 비정량 중복검사 우회 (1차) | `:1198` | `searchType=1` | `4`·`5`만 해당 |
| 2 | 킬코이 미트센터 소비기한 | `:1278` | `PACKER_CODE=''`, `STORE_CODE=''` | `'30228'`·`'9231'` 불일치 |
| 3 | CENTERNAME TRD/E/T/WET 소비기한 | `:1286` | `CENTERNAME='하이랜드푸드'` | `contains` 전부 불일치 |
| 4 | 비정량 중복검사 우회 (2차) | `:1322` | `searchType=1` | `4`·`5`만 해당 |
| 5 | `ITEM_TYPE` W / HW | `:1359` | — | 생산 VIEW는 `S`/`J`만 출력 |
| 6 | 이마트 LB 환산 자릿수 | `:1436~1440` | `searchType=1` | `0`만 해당 → `else` 분기(소수점 2자리) |
| 7 | `ITEM_TYPE` B | `:1474` | — | 홈플러스 비정량 전용 |

> **2·3번 주의** — 조건 자체는 `false`로 안전하게 통과하나, `:1286`의 `getCENTERNAME().substring(0, 3)`은 값이 3자 미만이면 `StringIndexOutOfBoundsException`이 발생한다. `CENTERNAME='하이랜드푸드'`(6자) 고정이라 현재는 안전하다. **생산 전용 메서드에서 이 분기를 제거하면 해당 위험 자체가 사라진다.**

### 3.3 생산 해당

| # | 분기 | 위치 | 조건 |
|:-:|------|:----:|------|
| 1 | `ITEM_TYPE` S | `:1412` | `CO_품목코드.비정량여부 = 1` |
| 2 | `ITEM_TYPE` J | `:1463` | `비정량여부 = 0` (**현재 데이터 전부 여기**) |

`search_production.jsp`
```sql
CASE WHEN I.비정량여부 = 1 THEN 'S' ELSE 'J' END AS ITEM_TYPE
```

**실측** — `PD_생산작업지시소요량` × `CO_품목코드`(대분류 2) 125건 전부 `비정량여부 = 0` → 현재는 `J` 경로만 동작한다.

---

## 4. ITEM_TYPE S / J 상세

### 4.1 `J` — 지정 중량 (`:1463~1471`, 9행)

```java
} else if (…getITEM_TYPE().equals(ITEM_TYPE_J)) {
    // 이마트 ITEM_TYPE J (지정된 중량 입력) | 바코드에서 중량, 제조일, 박스시리얼 X
    item_weight = arSM.get(current_work_position).getPACKWEIGHT();
    item_weight_double = Double.parseDouble(item_weight);
    item_weight_str = String.valueOf(item_weight_double);
}
```

| 항목 | 결과 |
|------|------|
| 중량 | `PACKWEIGHT` 고정값 (`COALESCE(NULLIF(V.평균중량,0), I.박스중량)`) |
| 제조일자 | `""` |
| 박스시리얼 | `""` |
| 바코드 파싱 | **없음** |

### 4.2 `S` — 저울 계근 (`:1412~1462`, 51행)

```java
} else if (…getITEM_TYPE().equals(ITEM_TYPE_S)) {
    if (weight_from.equals("0") || weight_to.equals("0")) { showAlertDialog("weight", 0); }
    item_weight = work_item_fullbarcode.substring(WEIGHT_FROM-1, WEIGHT_TO);
    item_pow = Math.pow(10, ZEROPOINT);
    item_weight_double = Double.parseDouble(item_weight) / item_pow;

    if ("LB".equals(BASEUNIT)) {
        double temp = item_weight_double * 0.453592;
        if (searchType == EMART) { … } else { item_weight_double = Math.floor(temp*100)/100; }
    }
    item_weight_double = Double.parseDouble(String.format("%.2f", item_weight_double));

    if (MAKINGDATE_FROM != "" && MAKINGDATE_TO != "") { item_making_date = substring(…); }
    if (BOXSERIAL_FROM != "" && BOXSERIAL_TO != "") { item_box_serial = substring(…); }
}
```

| 항목 | 결과 |
|------|------|
| 중량 | 바코드 구간 파싱 + `ZEROPOINT` + LB→KG 환산 |
| 소수점 | `%.2f` (이마트만 `item_pow` 기준) |
| 제조일자 | 바코드에서 추출 |
| 박스시리얼 | 바코드에서 추출 |

생산은 `searchType != EMART`이므로 LB 환산 시 **소수점 2자리 고정** 경로를 탄다.

---

## 5. 생산 전용 메서드 구성안

### 5.1 이식 대상 (필요)

```
① dialog_flag 체크
② 디바운스 (동일 바코드 1초)
③ edit_barcode.setText()
④ 1차 상품 스캔
     find_PackerProduct / find_PackerProductBarcodeGoods
     최초 → ProgressDlgShipSelect
     동일상품 → 중복검사 → set_scanFlag(false) → 재귀
     다른상품 → AlertDialog
⑤ 2차 BL 스캔
     BL번호 매칭 → current_work_position
     계근 완료 확인
     4키 중복검사
⑥ 중량 추출  ── ITEM_TYPE S / J 2분기만
⑦ wet_data_insert()
```

### 5.2 제외 대상 (불필요)

| 항목 | 사유 |
|------|------|
| 비정량 중복검사 우회 2곳 | `4`·`5` 전용 |
| 킬코이 미트센터 소비기한 | `PACKER_CODE`·`STORE_CODE` 미사용 |
| CENTERNAME TRD/E/T/WET 소비기한 | `'하이랜드푸드'` 고정, 이마트·롯데 전용 |
| `ITEM_TYPE` W / HW | 생산 VIEW 미출력 |
| `ITEM_TYPE` B | 홈플러스 비정량 전용 |
| 이마트 LB 자릿수 분기 | `searchType==0` 전용 → `else` 경로 고정 |

**397행 → 약 190행** 예상 (제외 분기 ~130행, 조건문 단순화 ~80행)

### 5.3 진입점 분기

```java
public void setBarcodeMsg(final String msg) {
    if (Common.searchType.equals(SEARCH_TYPE_PRODUCTION)) {
        setBarcodeMsgProduction(msg);
        return;
    }
    … 기존 코드 그대로 …
}
```

기존 코드를 한 줄도 수정하지 않으므로 **0·2·3·4·5·6·7 회귀 위험이 없다.**

---

## 6. 공유 멤버 변수

분리 시 함께 옮겨야 할 액티비티 멤버 변수다. 지역 변수로 바꾸면 동작이 달라질 수 있으므로 **그대로 멤버 변수를 사용**한다.

| 변수 | 역할 | 생산 사용 |
|------|------|:--:|
| `scan_flag` | 1차/2차 스캔 구분 | ✅ |
| `dialog_flag` | 다이얼로그 표시 중 재진입 차단 | ✅ |
| `work_flag` | 1=바코드 / 0=수기 / 2=상품코드 | ✅ |
| `work_ppcode` | 현재 작업 패커상품코드 | ✅ |
| `work_item_fullbarcode` | 스캔한 전체 바코드 | ✅ |
| `work_item_barcodegoods` | 바코드 상품코드 | ✅ |
| `work_item_bi_info` | `Barcodes_Info` (파싱 규칙) | ✅ |
| `work_bl_no` | 현재 BL번호 | ✅ |
| `current_work_position` | `arSM` 내 현재 대상 인덱스 | ✅ |
| `arSM` | 계근 대상 목록 | ✅ |
| `centerTotalCount` / `centerWorkCount` | 완료 판정 | ✅ |
| `lastProcessedBarcode` / `lastBarcodeProcessedTime` | 디바운스 | ✅ |
| `expiryDayTrans` | 소비기한 입력값 | ❌ 생산 미사용 |
| `alert_flag` | 경고 다이얼로그 표시 여부 | ✅ |
| `lotte_TryCount` | 롯데 박스순번 | ❌ |

---

## 7. 미확정 사항

### 7.1 생산 중복검사 정책

`소스분석/50_바코드_중복검사_우회_searchType별_분기.md:307`
> **searchType 1(생산), 7(생산라벨)**: 계근 흐름 자체가 다르므로 이 문서의 분석 대상에 포함되지 않는다.

생산의 중복검사 정책이 문서화되어 있지 않다. 현재 코드상으로는 비정량 우회(`:1198`, `:1322`)에 해당하지 않아 **일반 중복검사가 그대로 적용**된다.

| 판단 필요 | 내용 |
|---|---|
| 현행 유지 | 동일 바코드 재스캔 차단 (원본 동일) |
| 비정량처럼 우회 | 생산 원료는 같은 바코드가 반복될 수 있는지 확인 필요 |

`ITEM_TYPE = 'J'`인 현재 데이터에서는 **바코드를 읽지 않고 `PACKWEIGHT` 고정값**을 쓰므로, 같은 박스를 여러 번 스캔해도 구분이 되지 않는다. 중복검사가 유일한 방어선이다.

### 7.2 `ITEM_TYPE` 실제 분포

`CO_품목코드.비정량여부`가 대분류 2 전 건에서 `0`이다. 마스터 미입력인지 실제로 정량(고정중량) 대상인지 확인이 필요하다.

- `0` 유지 → `J` 경로 (박스마다 동일 중량 기록)
- `1` 체크 → `S` 경로 (바코드 실측)

---

## 8. 관련 문서

- `app/doc/개발/59_생산_계근대상받기_JSP_MSSQL전환.md` — 생산 전환 개발 가이드
- `app/doc/column/11_생산_계근대상_컬럼매핑_Oracle_MSSQL.md` — `ITEM_TYPE` 매핑 근거
- `app/doc/소스분석/57_생산_계근대상받기_JSP_컬럼_사용분석.md` — 25개 컬럼 사용처
- `app/doc/소스분석/50_바코드_중복검사_우회_searchType별_분기.md` — 중복검사 정책 (생산 제외됨)
- `app/doc/개발/56_setBarcodeMsg_디바운스_수정[...].md` — 디바운스 로직 근거
- `app/doc/오류/28_setBarcodeMsg_디바운스_바코드값_미비교_연속스캔_삼킴.md`
- `app/doc/기능/11_바코드스캔후_전체프로세스.md`
- `app/doc/오류/31_이마트_소숫점_반올림_주석_버림동작_불일치[...].md` — 중량 버림 사양
