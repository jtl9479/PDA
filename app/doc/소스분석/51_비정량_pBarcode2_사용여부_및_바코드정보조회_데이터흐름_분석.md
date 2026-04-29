# 이마트 비정량(M8) pBarcode2 사용 여부 + search_barcode_info_nonfixed 조회 데이터 사용처 분석

## 개요

**이마트 비정량(searchType=4, BARCODE_TYPE=M8) 한정 분석 문서**이다. 다른 BARCODE_TYPE, 다른 searchType은 본 분석 범위에서 제외한다.

- **Q1**: `LabelPrintHelper.java`의 M8 case에서 `pBarcode2` 변수가 라벨에 실제로 인쇄되는지 검증
- **Q3**: `search_barcode_info_nonfixed.jsp`가 응답하는 데이터가 PDA 어디에서 사용되는지 추적 (이마트 비정량 흐름만)

- **분석 대상 파일**:
  - `app/src/main/java/com/rgbsolution/highland_emart/print/LabelPrintHelper.java` (Q1)
  - `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp` (Q3)
  - `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgBarcodeSearch.java` (Q3)
  - `app/src/main/java/com/rgbsolution/highland_emart/db/DBHandler.java` (Q3)
  - `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java` (Q3)
- **작성일**: 2026-04-29

---

## AI 제약 조건

- 이 문서는 **이마트 비정량(M8, searchType=4) 한정** 분석이다
- 다른 BARCODE_TYPE(M0/M1/M3/M4/M9) 또는 다른 searchType(0/1/2/3/5/6/7)에 대한 분석/비교 금지
- 코드 인용 시 다른 타입이 등장하더라도 그것에 대한 별도 설명은 하지 않는다
- 임의 판단으로 범위를 넓히지 않는다

---

## 1. Q1 — 이마트 비정량(M8) pBarcode2 사용 여부

### 1.1 M8 case에서 pBarcode2 변수 생성 (LabelPrintHelper.java:L671-687)

```java
case "M8":
    // 이마트 비정량 납품분
    // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
    break;
```

→ M8 case에서 `pBarcode2` 및 `pBarcodeStr2` 변수가 생성된다.

> **자릿수 참고**: `pCompCode = LabelPrintHelper.COMPANY_CODE = "610933"` (6자리, L55).
> M8 pBarcode 총 자릿수 = 상품코드(6) + 중량(6) + 회사코드(6) + 수입식별번호(12) = **30자리**.

### 1.2 pBarcode 라벨 인쇄 (LabelPrintHelper.java:L811)

```java
labelData.write(slcsBarcode(barcodeX, barcodeY, 60, pBarcode).getBytes("EUC-KR"));
```

→ M8의 `pBarcode`는 라벨에 인쇄된다.

### 1.3 pBarcode2 라벨 인쇄 분기 (LabelPrintHelper.java:L831-843)

```java
// M3, M4, M9 추가 바코드 출력
if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M3)) {
    labelData.write(slcsBarcode(70, 205, 60, pBarcode2).getBytes("EUC-KR"));
} else if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M4)) {
    labelData.write(slcsBarcode(145, 205, 60, pBarcode2).getBytes("EUC-KR"));
} else if(si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)){
    labelData.write(slcsBarcode(125, 325, 60, pBarcode2).getBytes("EUC-KR"));
    String ctName = si.getCT_NAME();
    labelData.write(slcsBitmapText(450, 330, 25, ctName, true));
    labelData.write(slcsBitmapText(125, 390, 25, pBarcodeStr2, true));
    String belowBarcodeString = si.EMARTITEM + "," + si.getUSE_NAME();
    labelData.write(slcsBitmapText(80, 420, 25, belowBarcodeString, true));
}
```

→ **이 분기에 M8 case가 없다.** M8(이마트 비정량)의 `pBarcode2`는 라벨 인쇄 호출이 발생하지 않는다.

### 1.4 결론

| 항목 | 결과 | 근거 |
|---|:---:|---|
| M8 pBarcode2 변수 생성 | ✅ 생성됨 | LabelPrintHelper.java:L685 |
| M8 pBarcode2 라벨 인쇄 | ❌ **인쇄되지 않음** | LabelPrintHelper.java:L831-843 인쇄 분기에 M8 미포함 |

**이마트 비정량(M8)의 pBarcode2는 변수만 생성되고 라벨에 출력되지 않는다.** 의도된 설계 여부는 [문의사항 03](../문의사항/03_M8_비정량_이마트_pBarcode2_생성_미출력_의도.md)에서 별도 검토 중이며, 임의로 M8 분기 추가는 금지한다.

---

## 2. Q3 — search_barcode_info_nonfixed 조회 데이터 사용처 (이마트 비정량 한정)

### 2.1 데이터 출처: JSP 응답

**JSP 파일**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp`

CO_품목코드 테이블에서 SELECT 후 24개 컬럼을 `"::"` 구분자로 출력하고 행은 `";;"`로 구분한다.

### 2.2 데이터 흐름 단계

```
[1. JSP 호출 — searchType=4 분기]
    ProgressDlgBarcodeSearch.java:L82-83
    └─ HttpHelper.sendDataDb(..., URL_SEARCH_BARCODE_INFO_NONFIXED)
       ↓
[2. 응답 수신·파싱]
    ProgressDlgBarcodeSearch.java:L98 receiveData.split(";;")
    L109 temp = s.split("::", -1)
    L111-L134 Barcodes_Info 객체에 temp[0]~temp[23] 파싱
       ↓
[3. 로컬DB INSERT]
    ProgressDlgBarcodeSearch.java:L140 DBHandler.insertqueryBarcodeInfo(bi)
    DBHandler.java:L1060 INSERT INTO TB_BARCODE_INFO ...
       ↓
[4. 바코드 스캔 시 사용]
    BixolonShipmentActivity.java
    └─ setBarcodeMsg() → find_work_info() → DBHandler.selectqueryBarcodeInfo()
       → work_item_bi_info에 로드
       → 풀바코드 절사 + 중량 계산 + 라벨 출력 호출
```

### 2.3 단계 1: searchType=4 분기 (ProgressDlgBarcodeSearch.java:L82-83)

```java
} else if(Common.searchType.equals("4")) {
    receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO_NONFIXED);
}
```

### 2.4 단계 2-3: 파싱 + INSERT (24개 컬럼)

이마트 비정량(searchType=4)에서 JSP가 출력하는 24개 컬럼과 PDA 사용처:

| temp[] | 컬럼명 | 값/특성 | 비정량 사용처 |
|:---:|---|---|---|
| [0] | PACKER_CLIENT_CODE | '이마트용' (JSP 하드코딩) | 계근 데이터 INSERT 시 사용 (BixolonShipmentActivity.java:L1595) |
| [1] | PACKER_PRODUCT_CODE | 품목코드 | 출하대상 매칭 키 (TB_SHIPMENT.PACKER_PRODUCT_CODE 비교) |
| [2] | PACKER_PRD_NAME | 품목명 | 표시용 |
| [3] | ITEMCODE | 품목코드 | 저장용 |
| [4] | ITEM_NAME_KR | 품목명 | edit_product_name 표시 (BixolonShipmentActivity.java:L1842) |
| [5] | BRAND_CODE | '0000' (JSP 하드코딩) | 저장용 |
| [6] | BARCODEGOODS | 품목코드 | 바코드 절사값과 매칭 (find_work_info 내부) |
| [7] | BASEUNIT | 'KG' (JSP 하드코딩) | gi.setWEIGHT_UNIT (L1592), LB 환산 분기 (L1372) |
| [8] | ZEROPOINT | 소수점 자릿수 | 중량 계산 item_pow (L1363) |
| [9] | PACKER_PRD_CODE_FROM | '' (빈값) | 미사용 |
| [10] | PACKER_PRD_CODE_TO | '' (빈값) | 미사용 |
| [11] | BARCODEGOODS_FROM | 바코드 상품코드 시작 위치 | 풀바코드에서 상품코드 절사 (L1812) |
| [12] | BARCODEGOODS_TO | 바코드 상품코드 끝 위치 | 풀바코드에서 상품코드 절사 (L1812) |
| [13] | WEIGHT_FROM | 중량 시작 위치 | 풀바코드에서 중량 절사 (L1359-1360) |
| [14] | WEIGHT_TO | 중량 끝 위치 | 풀바코드에서 중량 절사 (L1359-1360) |
| [15] | MAKINGDATE_FROM | 제조일 시작 위치 | 풀바코드에서 제조일 절사 (L1391-1392) |
| [16] | MAKINGDATE_TO | 제조일 끝 위치 | 풀바코드에서 제조일 절사 (L1391-1392) |
| [17] | BOXSERIAL_FROM | 박스시리얼 시작 위치 | 풀바코드에서 박스시리얼 절사 (L1397-1398) |
| [18] | BOXSERIAL_TO | 박스시리얼 끝 위치 | 풀바코드에서 박스시리얼 절사 (L1397-1398) |
| [19] | STATUS | 생산품상태 | 저장용 |
| [20] | REG_ID | '' (빈값) | 저장용 |
| [21] | REG_DATE | '' (빈값) | 저장용 |
| [22] | REG_TIME | '' (빈값) | 저장용 |
| [23] | memo | '0000' (JSP 하드코딩) | 저장용 |

> 주의: 이마트 비정량(searchType=4)은 ProgressDlgBarcodeSearch에서 SHELF_LIFE(temp[24]) 파싱이 적용되지 않는다. JSP가 24개 컬럼만 출력하기 때문이다.

### 2.5 단계 4: 바코드 스캔 시 사용 (BixolonShipmentActivity)

#### 2.5.1 find_work_info — 비정량 무조건 매칭 (L1839-1851)

```java
if(Common.searchType.equals(SEARCH_TYPE_NONFIXED)){
    work_item_bi_info = bi;
    edit_product_name.setText(bi.getITEM_NAME_KR());
    edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
    if(count == 0){
        pp_code = bi.getPACKER_PRODUCT_CODE();
    }else{
        pp_code = pp_code + "', '" + bi.getPACKER_PRODUCT_CODE();
    }
    work_item_barcodegoods = bg;
    count++;
}
```

→ 이마트 비정량은 BARCODEGOODS 매칭 결과와 무관하게 루프 매 반복마다 `work_item_bi_info`를 덮어쓴다(여러 품목이 매핑된 경우 마지막 품목이 최종 저장). `pp_code`는 콤마로 누적된다.

#### 2.5.2 풀바코드에서 중량 절사 (L1359-1367)

```java
item_weight = work_item_fullbarcode.substring(
        Integer.parseInt(work_item_bi_info.getWEIGHT_FROM()) - 1,
        Integer.parseInt(work_item_bi_info.getWEIGHT_TO()));

item_pow = Math.pow(10, Integer.parseInt(work_item_bi_info.getZEROPOINT()));
item_weight_double = Double.parseDouble(item_weight) / item_pow;
```

→ `WEIGHT_FROM/TO`로 풀바코드에서 중량 부분만 substring으로 절사한 뒤, `ZEROPOINT`로 소수점 위치 적용해 실제 중량값 산출.

> 이마트 비정량은 search_production_nonfixed.jsp에서 ITEM_TYPE='HW' 하드코딩으로 응답되어 PDA 분기상 W/HW 분기로 진입한다.

#### 2.5.3 풀바코드에서 제조일·박스시리얼 절사 (L1391-1398)

```java
// 제조일 절사
item_making_date = work_item_fullbarcode.substring(
        Integer.parseInt(work_item_bi_info.getMAKINGDATE_FROM()) - 1,
        Integer.parseInt(work_item_bi_info.getMAKINGDATE_TO()));

// 박스시리얼 절사
item_box_serial = work_item_fullbarcode.substring(
        Integer.parseInt(work_item_bi_info.getBOXSERIAL_FROM()) - 1,
        Integer.parseInt(work_item_bi_info.getBOXSERIAL_TO()));
```

#### 2.5.4 라벨 출력 호출 (L1702-1705)

```java
}else if(Common.searchType.equals(SEARCH_TYPE_NONFIXED)){
    Log.d(TAG, "===========이마트(비정량) 출력 시작 ================");
    labelPrintHelper.setPrinting(weight_double, arSM.get(current_work_position), false, making_date, work_item_bi_info, arSM.get(current_work_position), Common.searchType, printerCallback);
}
```

→ `work_item_bi_info` 객체를 LabelPrintHelper에 전달. M8 case에서 pBarcode 생성 시 이 정보가 사용된다.

### 2.6 결론 (이마트 비정량 한정)

| 데이터 항목 | 사용처 |
|---|---|
| BARCODEGOODS_FROM/TO | 풀바코드에서 상품코드 절사 (find_work_info) |
| WEIGHT_FROM/TO + ZEROPOINT | 풀바코드에서 중량 절사 + 소수점 적용 |
| MAKINGDATE_FROM/TO | 풀바코드에서 제조일 절사 |
| BOXSERIAL_FROM/TO | 풀바코드에서 박스시리얼 절사 |
| BASEUNIT | 단위 저장 + LB→KG 환산 분기 |
| PACKER_CLIENT_CODE | 계근 데이터 INSERT 시 |
| ITEM_NAME_KR | 화면 표시용 |
| 그 외(BRAND_CODE/STATUS/memo 등) | 저장용 또는 미사용 |

→ search_barcode_info_nonfixed의 핵심 데이터는 **풀바코드 절사 위치(상품코드/중량/제조일/박스시리얼)와 ZEROPOINT**이며, 이는 바코드 스캔 후 풀바코드 문자열에서 각 정보를 추출하는 데 사용된다.

---

## 3. 종합 결론 (이마트 비정량 한정)

### Q1
이마트 비정량(M8) `pBarcode2`는 변수만 생성되고 라벨에 인쇄되지 않는다. (LabelPrintHelper.java:L831-843 인쇄 분기에 M8 미포함)

### Q3
search_barcode_info_nonfixed가 응답한 24개 컬럼은 ProgressDlgBarcodeSearch에서 파싱 후 TB_BARCODE_INFO에 INSERT 되며, 바코드 스캔 시 BixolonShipmentActivity가 `work_item_bi_info`로 로드해 풀바코드 절사(상품코드/중량/제조일/박스시리얼)와 중량 계산(ZEROPOINT)에 사용한다.

---

## 4. 관련 문서

- `app/doc/문의사항/03_M8_비정량_이마트_pBarcode2_생성_미출력_의도.md` — Q1 직접 근거
- `app/doc/소스분석/46_이마트바코드정보조회_vs_비정량바코드정보조회_JSP_원본비교분석.md` — Q3 JSP 원본 비교
- `app/doc/개발/36_비정량_바코드정보조회_JSP_MSSQL전환.md` — Q3 JSP MSSQL 전환
- `app/doc/개발/43_비정량_이마트_M9_바코드_신규_추가.md` — Q1 결론 활용 (비정량 M9 pBarcode2 인쇄 가드 설계)
