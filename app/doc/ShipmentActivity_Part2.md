# ShipmentActivity 분석 - Part 2: 바코드 스캔 처리

> **파일 위치**: `app/src/main/java/com/rgbsolution/highland_emart/ShipmentActivity.java`
> **코드 라인**: 4705줄
> **작성일**: 2025-01-27
> **최종 수정일**: 2026-01-06

---

## 📑 목차

- [Part 1: 개요 및 클래스 구조](ShipmentActivity_Part1.md)
- [Part 2: 바코드 스캔 처리](ShipmentActivity_Part2.md) ✅ 현재 문서
- [Part 3: 계근 데이터 처리 및 프린터 출력](ShipmentActivity_Part3.md)
- [Part 4: 서버 전송 및 예외 처리](ShipmentActivity_Part4.md)

---

## 1. 바코드 스캔 진입점

### 1.1 setMessage (892~908줄)

```java
@Override
protected void setMessage(String msg)
```

**역할**: ScannerActivity에서 오버라이드한 메서드로, PM80 스캐너로 바코드 스캔시 호출됨

**동작**:

```java
if (msg != null) {
    if (work_flag == 1) {
        // 바코드 스캔 모드
        setBarcodeMsg(msg);
    } else if(work_flag == 0){
        // 수기 입력 모드 (BL코드로 계근 리스트 조회)
        new ProgressDlgShipSelect(ShipmentActivity.this,
            sp_center_name.getSelectedItem().toString(),
            msg,
            scan_flag).execute();
    } else if(work_flag == 2){
        // 상품코드 모드
        setBarcodeMsg(msg);
    }
}
```

**work_flag 값**:
- `1`: 바코드 스캔 모드 (기본값)
- `0`: 수기 입력 모드
- `2`: 상품코드 모드

---

## 2. setBarcodeMsg 메서드 (959~1485줄) ⭐ 핵심

### 2.1 메서드 개요

```java
public void setBarcodeMsg(final String msg)
```

**총 라인**: 약 526줄
**역할**: 바코드 스캔 데이터를 처리하고 중량을 추출하는 핵심 메서드

**처리 흐름**:

```
┌─────────────────────────────────┐
│  1. 다이얼로그 중복 체크         │
│     (dialog_flag)               │
└─────────────────────────────────┘
                ↓
┌─────────────────────────────────┐
│  2. scan_flag 확인              │
│     true: 패커상품 스캔          │
│     false: BL 스캔              │
└─────────────────────────────────┘
       ↓                ↓
 [패커상품 스캔]     [BL 스캔]
  (967~1098줄)     (1099~1434줄)
```

---

### 2.2 패커상품 스캔 처리 (967~1098줄)

#### 2.2.1 패커상품 코드 추출 (969~981줄)

```java
String find_ppcodetemp = "";
if (work_flag == 1) {
    // 바코드에서 패커상품코드 추출
    find_ppcodetemp = find_PackerProduct(msg);
} else {
    // 상품코드로 직접 검색
    find_ppcodetemp = find_PackerProductBarcodeGoods(msg);
}

final String find_ppcode = find_ppcodetemp;

if (find_ppcode.equals("null")) {
    Toast.makeText(getApplicationContext(),
        "패커상품이 존재하지않거나,\n바코드가 정확하지 않습니다.",
        Toast.LENGTH_SHORT).show();
    vibrator.vibrate(1000);
    work_item_fullbarcode = "";
    work_item_barcodegoods = "";
    return;  // ⚠️ 종료
}
```

#### 2.2.2 최초 스캔 처리 (988~1005줄)

```java
if (work_ppcode.equals("")) {
    // 최초 스캔일 경우
    boolean dup = DBHandler.duplicatequeryGoodsWet_check(getApplicationContext(), msg);

    // 최초 스캔
    work_ppcode = find_ppcode;
    work_item_fullbarcode = msg;

    // 출하대상 조회
    new ProgressDlgShipSelect(this,
        sp_center_name.getSelectedItem().toString(),
        find_ppcode,
        scan_flag).execute();
}
```

#### 2.2.3 동일 상품 재스캔 처리 (1009~1061줄)

```java
else if (!work_ppcode.equals("") && work_ppcode.equals(find_ppcode)) {
    // 작업 중이고, 같은 상품을 스캔했을 경우
    work_item_fullbarcode = msg;

    // 중복 바코드 체크
    boolean dup = DBHandler.duplicatequeryGoodsWet_check(getApplicationContext(), work_item_fullbarcode);

    // 비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
    if(Common.searchType.equals("4") || Common.searchType.equals("5")) {
        dup = false;
    }

    if (dup) {
        Toast.makeText(getApplicationContext(),
            "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.",
            Toast.LENGTH_SHORT).show();
        vibrator.vibrate(1000);
        work_item_fullbarcode = "";
        work_item_barcodegoods = "";
        return;  // ⚠️ 종료
    } else {
        set_scanFlag(false);  // BL스캔으로 전환
        work_ppcode = find_ppcode;
        work_item_fullbarcode = msg;

        // 총 계근 완료 체크
        if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {
            show_wetFinishDialog();
        } else {
            // 계속 진행
        }

        setBarcodeMsg(msg);  // 🔄 재귀 호출 (BL스캔 모드로)
    }
}
```

#### 2.2.4 다른 상품 스캔 처리 (1062~1094줄)

```java
else if (!work_ppcode.equals(find_ppcode)) {
    // 작업 중이고, 다른 상품을 스캔했을 경우
    vibrator.vibrate(500);
    dialog_flag = true;

    new AlertDialog.Builder(ShipmentActivity.this, R.style.AppCompatDialogStyle)
        .setIcon(R.drawable.highland)
        .setTitle(R.string.shipment_wet_other)
        .setMessage(R.string.shipment_wet_other_msg)
        .setCancelable(false)
        .setPositiveButton(R.string.shipment_wet_yes,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog_flag = false;
                    work_ppcode = find_ppcode;
                    work_item_fullbarcode = msg;

                    // 새로운 상품으로 출하대상 조회
                    new ProgressDlgShipSelect(ShipmentActivity.this,
                        sp_center_name.getSelectedItem().toString(),
                        find_ppcode,
                        scan_flag).execute();
                }
            }
        )
        .setNegativeButton(R.string.shipment_wet_no,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog_flag = false;
                }
            }
        )
        .show();
}
```

**💡 정리**:

| 상황 | work_ppcode | 동작 |
|------|------------|------|
| **최초 스캔** | 빈 문자열 ("") | 출하대상 조회 → scan_flag=false |
| **동일 상품 재스캔** | 동일 | 중복 체크 → BL스캔 모드로 전환 |
| **다른 상품 스캔** | 다름 | 확인 다이얼로그 표시 |

---

### 2.3 BL 스캔 및 계근 처리 (1099~1434줄)

#### 2.3.1 BL 번호로 작업 position 찾기 (1106~1125줄)

```java
work_item_fullbarcode = msg;

try {
    String temp_bl_no = sp_bl_no.getItemAtPosition(sp_bl_no.getSelectedItemPosition()).toString();

    for (int i = 0; i < arSM.size(); i++) {
        if (temp_bl_no.equals(arSM.get(i).getBL_NO())
            && !arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))) {
            // BL번호 같은 상품 검색 완료 + 계근 미완료
            current_work_position = i;
            work_bl_no = temp_bl_no;
            break;
        } else {
            work_bl_no = "";
            current_work_position = -1;
        }
    }
```

#### 2.3.2 소비기한 정보 검증

**킬코이 + 미트센터 (1131~1138줄)**:

```java
if (arSM.get(current_work_position).getPACKER_CODE().equals("30228")
    && arSM.get(current_work_position).getSTORE_CODE().equals("9231")) {

    if (work_item_bi_info.getSHELF_LIFE().equals("")
        || work_item_bi_info.getMAKINGDATE_FROM().equals("")
        || work_item_bi_info.getMAKINGDATE_TO().equals("")) {

        Toast.makeText(getApplicationContext(),
            "미트센터 납품 - KILKOY 상품의 경우 소비기한정보가 필수로 입력되어야 합니다.\n" +
            "현 상품의 계근을 진행할 수 없습니다. 관리자에게 문의하세요.",
            Toast.LENGTH_LONG).show();
        vibrator.vibrate(1000);
        work_ppcode = "";
        scan_flag = true;
        return;  // ⚠️ 종료
    }
}
```

**트레이더스/수입육 (1177~1186줄)**:

```java
if (arSM.get(current_work_position).getCENTERNAME().equals("용인TRD")
    || arSM.get(current_work_position).getCENTERNAME().equals("대구TRD")
    || arSM.get(current_work_position).getCENTERNAME().contains("TRD")
    || arSM.get(current_work_position).getCENTERNAME().contains("E/T")
    || arSM.get(current_work_position).getCENTERNAME().contains("WET")) {

    if (Common.searchType.equals("0")) {
        if (work_item_bi_info.getSHELF_LIFE().equals("")
            || work_item_bi_info.getMAKINGDATE_FROM().equals("")
            || work_item_bi_info.getMAKINGDATE_TO().equals("")) {

            Toast.makeText(getApplicationContext(),
                "트레이더스 납품 상품의 경우 소비기한정보가 필수로 입력되어야 합니다.\n" +
                "현 상품의 계근을 진행할 수 없습니다. 관리자에게 문의하세요.",
                Toast.LENGTH_LONG).show();
            vibrator.vibrate(1000);
            work_ppcode = "";
            scan_flag = true;
            return;  // ⚠️ 종료
        }
    }
}
```

#### 2.3.3 current_work_position 확인 (1189~1196줄)

```java
if (current_work_position == -1) {
    Toast.makeText(getApplicationContext(),
        "해당하는 BL상품이 없습니다.\nBL번호를 확인해주세요.",
        Toast.LENGTH_SHORT).show();
    vibrator.vibrate(300);
    return;  // ⚠️ 종료
} else {
    sp_point_name.setSelection(current_work_position);
}

sList.setSelection(current_work_position);  // 리스트 스크롤
```

#### 2.3.4 계근 완료 체크 (1200~1208줄)

```java
if (arSM.get(current_work_position).getGI_REQ_PKG()
    .equals(String.valueOf(arSM.get(current_work_position).getPACKING_QTY()))) {

    // 이미 계근 완료됨
    if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {
        show_wetFinishDialog();  // 총 계근 완료
    } else {
        // 다음 지점 작업 필요
    }
    return;  // ⚠️ 종료
}
```

#### 2.3.5 중복 바코드 체크 (1214~1233줄)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet(getApplicationContext(),
    work_item_fullbarcode,
    arSM.get(current_work_position).getGI_D_ID(),
    arSM.get(current_work_position).getPACKER_PRODUCT_CODE());

// 비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
if (Common.searchType.equals("4") || Common.searchType.equals("5")) {
    dup = false;
}

if (dup) {
    Toast.makeText(getApplicationContext(),
        "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.",
        Toast.LENGTH_SHORT).show();
    vibrator.vibrate(1000);
    work_item_fullbarcode = "";
    work_item_barcodegoods = "";
    return;  // ⚠️ 종료
}
```

---

### 2.4 중량 추출 로직 (1255~1425줄)

#### 2.4.1 ITEM_TYPE별 처리

**ITEM_TYPE "W" (1255~1307줄)**: 이마트 바코드 계근

```java
if (arSM.get(current_work_position).getITEM_TYPE().equals("W")
    || arSM.get(current_work_position).getITEM_TYPE().equals("HW")) {

    String weight_from = work_item_bi_info.getWEIGHT_FROM();
    String weight_to = work_item_bi_info.getWEIGHT_TO();

    // 중량 위치 정보 확인
    if (weight_from.equals("0") | weight_to.equals("0")) {
        showAlertDialog("weight", 0);
        alert_flag = true;
    }

    // 1. 바코드에서 중량 문자열 추출
    item_weight = work_item_fullbarcode.substring(
        Integer.parseInt(work_item_bi_info.getWEIGHT_FROM()) - 1,
        Integer.parseInt(work_item_bi_info.getWEIGHT_TO())
    );
    // 예: "012345" (바코드 7~12번째 문자)

    // 2. ZeroPoint 적용
    item_pow = Math.pow(10, Integer.parseInt(work_item_bi_info.getZEROPOINT()));
    item_weight_double = Double.parseDouble(item_weight) / item_pow;
    // 예: 12345 / 100 = 123.45 (ZeroPoint=2)

    // 3. LB → KG 변환
    if ("LB".equals(work_item_bi_info.getBASEUNIT())) {
        // LB * 0.453592 = KG
        double temp_weight_double = item_weight_double * 0.453592;
        item_weight_double = Math.floor(temp_weight_double * item_pow) / item_pow;
        item_weight_str = String.valueOf(item_weight_double);
    }

    // 4. 소수점 1자리로 절사
    item_weight_double = Math.floor(item_weight_double * 10);
    item_weight_double = item_weight_double / 10.0;

    String temp_weight = String.format("%.1f", item_weight_double);
    item_weight_double = Double.parseDouble(temp_weight);
    item_weight_str = String.valueOf(item_weight_double);

    // 5. 제조일 추출
    if (work_item_bi_info.getMAKINGDATE_FROM() != ""
        && work_item_bi_info.getMAKINGDATE_TO() != "") {
        item_making_date = work_item_fullbarcode.substring(
            Integer.parseInt(work_item_bi_info.getMAKINGDATE_FROM()) - 1,
            Integer.parseInt(work_item_bi_info.getMAKINGDATE_TO())
        );
    }

    // 6. 박스시리얼 추출
    if (work_item_bi_info.getBOXSERIAL_FROM() != ""
        && work_item_bi_info.getBOXSERIAL_TO() != "") {
        item_box_serial = work_item_fullbarcode.substring(
            Integer.parseInt(work_item_bi_info.getBOXSERIAL_FROM()) - 1,
            Integer.parseInt(work_item_bi_info.getBOXSERIAL_TO())
        );
    }
}
```

**ITEM_TYPE "S" (1308~1361줄)**: 소수점 둘째자리 처리

```java
else if (arSM.get(current_work_position).getITEM_TYPE().equals("S")) {
    // ... (W와 동일한 처리)

    // 🔥 차이점: 소수점 2자리로 처리
    String temp_weight = String.format("%.2f", item_weight_double);
    item_weight_double = Double.parseDouble(temp_weight);
    item_weight_str = String.valueOf(item_weight_double);
}
```

**ITEM_TYPE "J" (1362~1370줄)**: 지정 중량

```java
else if (arSM.get(current_work_position).getITEM_TYPE().equals("J")) {
    // 이마트 ITEM_TYPE J (지정된 중량 입력)
    // 바코드에서 중량, 제조일, 박스시리얼 X

    item_weight = arSM.get(current_work_position).getPACKWEIGHT();
    item_weight_double = Double.parseDouble(item_weight);
    item_weight_str = String.valueOf(item_weight_double);
}
```

**ITEM_TYPE "B" (1373~1424줄)**: 홈플러스 비정량

```java
// Homeplus 비정량 "B"
if (arSM.get(current_work_position).getITEM_TYPE().equals("B")) {
    // ... (S와 동일한 처리)

    // 소수점 2자리
    String temp_weight = String.format("%.2f", item_weight_double);
    item_weight_double = Double.parseDouble(temp_weight);
    item_weight_str = String.valueOf(item_weight_double);
}
```

#### 2.4.2 wet_data_insert 호출 (1426줄)

```java
wet_data_insert(item_weight_str, item_weight_double, item_making_date, item_box_serial);
```

---

## 3. 보조 메서드

### 3.1 find_PackerProduct (1663~1680줄)

```java
public String find_PackerProduct(String barcode)
```

**역할**: 바코드에서 패커상품코드 추출

```java
String pp_code = "";
pp_code = find_work_info(barcode, true);  // true: 바코드에서 추출

if (!edit_product_name.getText().equals("")) {
    return pp_code;
} else {
    return "null";
}
```

### 3.2 find_work_info (1712~1795줄)

```java
private String find_work_info(String req, boolean type)
```

**역할**: 바코드 정보 조회 및 패커상품코드 반환

**처리 흐름**:

```java
String pp_code = "";
int count = 0;

// DB에서 바코드 정보 조회
ArrayList<Barcodes_Info> list_barcode_info = DBHandler.selectqueryBarcodeInfo(this);

for (Barcodes_Info bi : list_barcode_info) {
    String bg = bi.getBARCODEGOODS();
    String bg_from = bi.getBARCODEGOODS_FROM();
    String bg_to = bi.getBARCODEGOODS_TO();
    String temp_bg;

    if (type && req.length() >= Integer.parseInt(bg_to)) {
        // PACKER_PRODUCT_CODE로 찾을 경우
        temp_bg = req.substring(Integer.parseInt(bg_from) - 1, Integer.parseInt(bg_to));
    } else {
        // BL로 찾을 경우
        temp_bg = req;
    }

    if (temp_bg.equals(bg)) {
        // barcodegoods find success
        work_item_bi_info = bi;
        edit_product_name.setText(bi.getITEM_NAME_KR());
        edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());

        if(count == 0){
            pp_code = bi.getPACKER_PRODUCT_CODE();
        } else {
            pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();
        }

        work_item_barcodegoods = bg;
        count++;
    } else {
        edit_product_name.setText("");
        edit_product_code.setText("");
        work_item_barcodegoods = "";
    }

    // 비정량은 모든 상품 반환
    if(Common.searchType.equals("4")) {
        // ... (동일 처리)
        count++;
    }
}

return pp_code;
```

**특징**:
- 여러 개의 패커상품코드가 일치하면 `', '`로 연결하여 반환
  - 예: `'PROD001', 'PROD002'` → SQL IN 절에 사용
- 비정량(searchType="4")은 모든 상품 반환

---

## 4. ITEM_TYPE별 처리 요약

| ITEM_TYPE | 설명 | 중량 처리 | 소수점 | LB→KG 변환 | 제조일 | 박스시리얼 | 코드 위치 |
|-----------|------|----------|--------|-----------|--------|----------|---------|
| **W** | 이마트 바코드 계근 | 바코드 추출 | **1자리** | ✅ | ✅ | ✅ | 1255~1307 |
| **HW** | 이노베이션 비정량 | 바코드 추출 | **1자리** | ✅ | ✅ | ✅ | 1255 (W와 동일) |
| **S** | 바코드 계근 | 바코드 추출 | **2자리** | ✅ | ✅ | ✅ | 1308~1361 |
| **J** | 지정 중량 | **PACKWEIGHT** | - | ❌ | ❌ | ❌ | 1362~1370 |
| **B** | 홈플러스 비정량 | 바코드 추출 | **2자리** | ✅ | ✅ | ✅ | 1373~1424 |

---

## 5. 중량 계산 예시

### 5.1 예시 1: 이마트 호주산 소고기 (ITEM_TYPE "W")

**바코드**: `12345600123456789012345678901234`
**바코드 정보**:
- WEIGHT_FROM = 7
- WEIGHT_TO = 12
- ZEROPOINT = 2
- BASEUNIT = LB

**처리 과정**:

```
1단계: 중량 문자열 추출
    substring(6, 12) = "001234"

2단계: ZeroPoint 적용
    item_pow = Math.pow(10, 2) = 100
    item_weight_double = 1234 / 100 = 12.34 (LB)

3단계: LB → KG 변환
    temp_weight_double = 12.34 * 0.453592 = 5.597...
    item_weight_double = Math.floor(5.597... * 100) / 100 = 5.59 (KG)

4단계: 소수점 1자리 절사
    item_weight_double = Math.floor(5.59 * 10) / 10.0 = 5.5 (KG)

최종 중량: 5.5 KG
```

### 5.2 예시 2: 홈플러스 비정량 (ITEM_TYPE "B")

**바코드**: `98765400567890123456789012345678`
**바코드 정보**:
- WEIGHT_FROM = 7
- WEIGHT_TO = 12
- ZEROPOINT = 2
- BASEUNIT = KG

**처리 과정**:

```
1단계: 중량 문자열 추출
    substring(6, 12) = "005678"

2단계: ZeroPoint 적용
    item_pow = Math.pow(10, 2) = 100
    item_weight_double = 5678 / 100 = 56.78 (KG)

3단계: LB → KG 변환
    BASEUNIT = KG이므로 변환 없음

4단계: 소수점 2자리 처리
    temp_weight = String.format("%.2f", 56.78) = "56.78"

최종 중량: 56.78 KG
```

---

## 6. 중복 바코드 체크

### 6.1 첫 번째 체크: 패커상품 스캔 직후 (989~998줄)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet_check(getApplicationContext(), msg);
```

**쿼리**:
```sql
SELECT COUNT(*) FROM TB_GOODS_WET WHERE BARCODE = ?
```

### 6.2 두 번째 체크: BL 스캔 직후 (1214~1233줄)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet(getApplicationContext(),
    work_item_fullbarcode,
    arSM.get(current_work_position).getGI_D_ID(),
    arSM.get(current_work_position).getPACKER_PRODUCT_CODE());
```

**쿼리**:
```sql
SELECT COUNT(*) FROM TB_GOODS_WET
WHERE BARCODE = ?
  AND GI_D_ID = ?
  AND PACKER_PRODUCT_CODE = ?
```

### 6.3 비정량 예외 처리

```java
// 비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
if(Common.searchType.equals("4") || Common.searchType.equals("5")) {
    dup = false;
}
```

**이유**: 비정량 상품은 박스마다 중량이 다르지만 바코드는 동일할 수 있음

---

## 7. 플로우 차트

```
                        setBarcodeMsg(msg)
                               ↓
                    ┌──────────┴──────────┐
                    │  dialog_flag 체크   │
                    └──────────┬──────────┘
                               ↓
                    ┌──────────┴──────────┐
                    │   scan_flag 확인    │
                    └──────────┬──────────┘
                               ↓
                  ┌────────────┴────────────┐
                  ↓                         ↓
          [scan_flag = true]       [scan_flag = false]
          (패커상품 스캔)           (BL 스캔)
                  ↓                         ↓
      ┌───────────┴───────────┐   ┌────────┴────────┐
      │ find_PackerProduct()  │   │ BL번호로 검색   │
      └───────────┬───────────┘   └────────┬────────┘
                  ↓                        ↓
      ┌───────────┴───────────┐   ┌────────┴────────┐
      │    null 체크          │   │ 소비기한 검증   │
      └───────────┬───────────┘   └────────┬────────┘
                  ↓                        ↓
      ┌───────────┴───────────┐   ┌────────┴────────┐
      │  work_ppcode 확인     │   │ 중복 바코드 체크│
      └───────────┬───────────┘   └────────┬────────┘
                  ↓                        ↓
      ┌───────────┴───────────┐   ┌────────┴────────┐
      │ 최초/동일/다른 상품   │   │ ITEM_TYPE 확인  │
      └───────────┬───────────┘   └────────┬────────┘
                  ↓                        ↓
      ┌───────────┴───────────┐   ┌────────┴────────┐
      │ 중복 바코드 체크      │   │  중량 추출      │
      └───────────┬───────────┘   └────────┬────────┘
                  ↓                        ↓
      ┌───────────┴───────────┐   ┌────────┴────────┐
      │ scan_flag = false     │   │ LB → KG 변환    │
      └───────────┬───────────┘   └────────┬────────┘
                  ↓                        ↓
                  └────────────┬───────────┘
                               ↓
                    ┌──────────┴──────────┐
                    │  wet_data_insert()  │
                    └─────────────────────┘
```

---

## 8. 다음 문서 안내

Part 2에서는 바코드 스캔 처리 로직을 상세히 살펴보았습니다.

**다음 문서에서 계속됩니다**:
- [Part 3: 계근 데이터 처리 및 프린터 출력](ShipmentActivity_Part3.md)
  - wet_data_insert() 메서드
  - 프린터 출력 메서드 (setPrinting, setHomeplusPrinting, setPrintingLotte, setPrinting_prod)
  - 바코드 형식별 라벨 생성
  - Woosim 프린터 명령

---

**작성일**: 2025-01-27
**Part**: 2/4
**이전**: [← ShipmentActivity Part 1](ShipmentActivity_Part1.md)
**다음**: [ShipmentActivity Part 3 →](ShipmentActivity_Part3.md)
