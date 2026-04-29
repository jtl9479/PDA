# 비정량 이마트(M8) pBarcode2 사용 여부 + search_barcode_info_nonfixed 조회 데이터 사용처 종합 분석

## 개요

두 가지 주제를 종합 분석한 문서이다.

- **Q1**: `LabelPrintHelper.java`의 BARCODE_TYPE=M8(비정량 이마트) case에서 `pBarcode2` 변수가 생성되지만 라벨 인쇄 단계에서 실제로 출력되는지 여부 검증
- **Q3**: `search_barcode_info_nonfixed.jsp`가 응답하는 데이터가 `ProgressDlgBarcodeSearch.java` → `TB_BARCODE_INFO` → `BixolonShipmentActivity` 순서로 어떻게 흘러가고 각 필드가 어디서 사용되는지 추적

- **파일 경로 (Q1)**: `app/src/main/java/com/rgbsolution/highland_emart/print/LabelPrintHelper.java`
- **파일 경로 (Q3)**: 아래 4개 파일 연계 분석
  - `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp`
  - `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgBarcodeSearch.java`
  - `app/src/main/java/com/rgbsolution/highland_emart/db/DBHandler.java`
  - `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`
- **타입**: 복합 (Helper / AsyncTask / Handler / Activity)
- **작성일**: 2026-04-27

---

## 1. 역할

- Q1: BARCODE_TYPE별 pBarcode2 생성 구조 및 라벨 인쇄 분기 코드 검증
- Q3: 비정량 바코드정보 조회 JSP의 출력 컬럼 확인, Java 파싱 인덱스 매핑, 로컬DB 저장 컬럼, 바코드 스캔 시 실제 사용처(substring 절사 / 중량 계산) 추적

---

## 2. 주요 상수/필드

| 상수/필드 | 타입 | 값 | 용도 |
|----------|------|-----|------|
| BARCODE_TYPE_M0 | String | "M0" | 이마트 상품코드 형식 1 |
| BARCODE_TYPE_M1 | String | "M1" | 이마트 상품코드 형식 2 |
| BARCODE_TYPE_M3 | String | "M3" | 이마트 상품코드 형식 3 (PC매입) |
| BARCODE_TYPE_M4 | String | "M4" | 이마트 상품코드 형식 4 (PC매입) |
| BARCODE_TYPE_M8 | String | "M8" | 이마트 비정량 납품분 |
| BARCODE_TYPE_M9 | String | "M9" | 이마트 우육 센터납 |
| pBarcode | String | 동적 생성 | 메인 바코드 (라벨 인쇄 대상) |
| pBarcode2 | String | 동적 생성 | 서브 바코드 (일부 타입만 인쇄) |
| work_item_bi_info | Barcodes_Info | 스캔 시 설정 | TB_BARCODE_INFO에서 조회된 상품 바코드 정보 |

---

## 3. 주요 메서드

| 메서드 | 위치(줄) | 반환 | 용도 |
|--------|:--------:|------|------|
| LabelPrintHelper.setPrinting() | BARCODE_TYPE switch (L503) | void | BARCODE_TYPE별 pBarcode/pBarcode2 생성 후 라벨 인쇄 |
| ProgressDlgBarcodeSearch.doInBackground() | L40 | Integer | JSP 호출 → 응답 파싱 → TB_BARCODE_INFO INSERT |
| DBHandler.insertqueryBarcodeInfo() | L1060 | boolean | TB_BARCODE_INFO 로컬DB INSERT |
| DBHandler.selectqueryBarcodeInfo() | L888 | ArrayList | TB_BARCODE_INFO 전체 조회 (BARCODEGOODS_TO != '') |
| BixolonShipmentActivity.setBarcodeMsg() | L1140 | void | 바코드 스캔 수신 → find_PackerProduct() 또는 find_PackerProductBarcodeGoods() 호출 |
| BixolonShipmentActivity.find_work_info() | L1796 | String | TB_BARCODE_INFO 조회 → BARCODEGOODS_FROM/TO로 바코드 절사 후 상품 매칭 |

---

## 4. 호출 관계

### 4.1 이 파일이 호출하는 대상

| 호출 대상 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| DBHandler | selectqueryBarcodeInfo() | BixolonShipmentActivity.java:L1800 | TB_BARCODE_INFO 전체 조회 |
| DBHandler | insertqueryBarcodeInfo() | ProgressDlgBarcodeSearch.java:L140 | 파싱 결과 TB_BARCODE_INFO INSERT |
| HttpHelper | sendDataDb() | ProgressDlgBarcodeSearch.java:L83 | search_barcode_info_nonfixed.jsp 호출 |
| LabelPrintHelper | setPrinting() | BixolonShipmentActivity.java:L1702 | 라벨 인쇄 (work_item_bi_info 전달) |

### 4.2 이 파일을 호출하는 곳

| 호출 위치 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| BixolonShipmentActivity | new ProgressDlgBarcodeSearch().execute() | (조회 버튼 또는 자동 호출) | 바코드정보 조회 시작 |
| BixolonShipmentActivity | setBarcodeMsg() | L1140 | 스캐너 수신 이벤트 발생 시 호출 |

---

## 5. 데이터 흐름

### Q1: pBarcode2 라벨 인쇄 흐름

```
[LabelPrintHelper.setPrinting()]
    ↓ switch(si.getBARCODE_TYPE())
    ↓
  [각 case] pBarcode, pBarcode2 변수 생성
    ↓
  L811: labelData.write(slcsBarcode(barcodeX, barcodeY, 60, pBarcode))  ← pBarcode 무조건 인쇄
    ↓
  L832-843: if(M3) slcsBarcode(pBarcode2)
            else if(M4) slcsBarcode(pBarcode2)
            else if(M9) slcsBarcode(pBarcode2)   ← pBarcode2 인쇄 (M3/M4/M9만)
    ↓
  M0, M1, M8: pBarcode2 인쇄 코드 없음 → 미인쇄
```

### Q3: search_barcode_info_nonfixed 데이터 흐름

```
[search_barcode_info_nonfixed.jsp]
  ↓ CO_품목코드 테이블 SELECT (24개 컬럼 출력)
  ↓ out.println(...) "::" 구분자로 24개 컬럼 출력, ";;" 행 구분

[ProgressDlgBarcodeSearch.doInBackground()] — searchType=4
  ↓ sendDataDb(data, "inno", "search_barcode_info", URL_SEARCH_BARCODE_INFO_NONFIXED) — L83
  ↓ receiveData.split(";;") 행 분리 — L98
  ↓ temp = s.split("::", -1) 컬럼 분리 — L109
  ↓ Barcodes_Info 객체에 temp[0]~temp[23] 파싱 — L111~L134
    (searchType=4/5이면 temp[24] SHELF_LIFE 파싱 생략)
  ↓ DBHandler.insertqueryBarcodeInfo(bi) — L140

[TB_BARCODE_INFO (SQLite 로컬DB)]
  ↓ (저장 후 바코드 스캔 이벤트 발생)

[BixolonShipmentActivity.setBarcodeMsg()] — L1140
  ↓ work_flag=1 → find_PackerProduct(msg) → find_work_info() — L1162
  ↓ work_flag=2 → find_PackerProductBarcodeGoods(msg) → find_work_info_barcodeGoods() — L1166

[find_work_info()] — L1796
  ↓ DBHandler.selectqueryBarcodeInfo(this) — L1800
  ↓ for 각 bi:
      bg_from = bi.getBARCODEGOODS_FROM()   ← 바코드 상품코드 시작 위치
      bg_to   = bi.getBARCODEGOODS_TO()     ← 바코드 상품코드 끝 위치
      temp_bg = req.substring(bg_from-1, bg_to)  ← 풀바코드에서 상품코드 절사
      if(temp_bg == bg) → work_item_bi_info = bi  ← 매칭 성공 시 저장 — L1822
    (searchType=4이면 조건 없이 무조건 work_item_bi_info = bi 로 덮어씀 — L1839~L1851)

[setBarcodeMsg() BL스캔 단계] — L1348~L1514
  ↓ ITEM_TYPE=W/HW: work_item_fullbarcode.substring(WEIGHT_FROM-1, WEIGHT_TO) → 중량 절사 — L1359-1360
  ↓ ZEROPOINT: Math.pow(10, ZEROPOINT) → 중량 계산 — L1363
  ↓ BASEUNIT="LB": KG 환산 — L1372
  ↓ MAKINGDATE_FROM/TO: 제조일 절사 — L1391-1392
  ↓ BOXSERIAL_FROM/TO: 박스시리얼 절사 — L1397-1398
  ↓ ITEM_TYPE=S: 동일 흐름 (소수점 2자리) — L1401~L1450
  ↓ ITEM_TYPE=B(홈플러스비정량): 동일 흐름 — L1462~L1513
  ↓ wet_data_insert() 호출 — L1516

[계근 데이터 삽입 시]
  ↓ gi.setWEIGHT_UNIT(work_item_bi_info.getBASEUNIT()) — L1592
  ↓ gi.setPACKER_CLIENT_CODE(work_item_bi_info.getPACKER_CLIENT_CODE()) — L1595
```

---

## 6. 핵심 코드

### Q1-1: M8 case pBarcode2 생성 (LabelPrintHelper.java:L671-687)

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

### Q1-2: pBarcode2 인쇄 분기 (LabelPrintHelper.java:L831-843)

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
    String belowBarcodeString = si.EMARTITEM +","+si.getUSE_NAME();
    labelData.write(slcsBitmapText(80, 420, 25, belowBarcodeString, true));
}
// M8은 이 블록에 포함되지 않음 → pBarcode2 미인쇄
```

### Q3-1: ProgressDlgBarcodeSearch searchType=4 분기 (L82-83)

```java
} else if(Common.searchType.equals("4")) {
    receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO_NONFIXED);
}
```

### Q3-2: find_work_info에서 searchType=4 무조건 매칭 (L1839-1851)

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

### Q3-3: 바코드에서 중량 절사 (BixolonShipmentActivity.java:L1359-1367)

```java
item_weight = work_item_fullbarcode.substring(
        Integer.parseInt(work_item_bi_info.getWEIGHT_FROM()) - 1, Integer.parseInt(work_item_bi_info.getWEIGHT_TO()));

item_pow = Math.pow(10, Integer.parseInt(work_item_bi_info.getZEROPOINT()));
item_weight_double = Double.parseDouble(item_weight) / item_pow;
```

---

## 7. 원본 비교

| 항목 | 원본 | 현재 | 동일 |
|------|------|------|:----:|
| M8 pBarcode2 생성 구조 | EMARTLOGIS_CODE 기반 생성 | 동일 | O |
| pBarcode2 인쇄 분기 (M3/M4/M9만) | M3/M4/M9만 인쇄 | 동일 | O |
| M8 pBarcode2 미인쇄 | 미인쇄 | 동일 | O |
| search_barcode_info_nonfixed JSP | Oracle 기반 쿼리 | MSSQL CO_품목코드 기반 쿼리 (전환됨) | X (DB 전환) |
| ProgressDlgBarcodeSearch 파싱 인덱스 | temp[0]~temp[23] | temp[0]~temp[23] (동일) | O |
| BARCODEGOODS_FROM/TO 절사 로직 | 동일 | 동일 | O |
| WEIGHT_FROM/TO + ZEROPOINT 계산 | 동일 | 동일 | O |

---

## 8. 주의사항

### Q1 관련
- M8(비정량 이마트)은 pBarcode2가 L685에서 생성되지만, L831-843의 인쇄 분기에 M8 조건이 없으므로 라벨에 출력되지 않는다. 이는 의도된 설계이며 임의로 추가하면 안 된다.
- M0, M1도 pBarcode2가 생성되지만 인쇄 분기에 포함되지 않아 미인쇄이다.
- pBarcode2 인쇄 대상은 M3, M4, M9 세 타입뿐이다.
- M8의 pBarcode2 구성은 M0/M1과 다르다. M0/M1은 `EMARTLOGIS_CODE(6) + 중량 + 회사코드 + 수입식별번호` 구조이나, M8은 `EMARTLOGIS_CODE(6) + 수입식별번호 + 중량 + 회사코드` 순서로 필드 배치가 다르다.

### Q3 관련
- searchType=4(비정량 이마트)의 경우 `find_work_info()` 내부에서 BARCODEGOODS 매칭 성공/실패 여부와 무관하게 루프의 매 반복마다 `work_item_bi_info = bi`로 덮어쓴다(L1839-1851). 이는 비정량 상품이 바코드 패턴 매칭이 아니라 단일 상품코드로 운영되기 때문이며, 의도된 로직이다.
- BARCODEGOODS_FROM/TO는 1-based 인덱스이므로 substring 시 반드시 `-1` 처리가 필요하다(L1360, L1812).
- searchType=4/5(비정량)는 ProgressDlgBarcodeSearch에서 SHELF_LIFE(temp[24]) 파싱을 생략한다(L135-137). 비정량 JSP가 24개 컬럼만 출력하기 때문이다.
- insertqueryBarcodeInfo에서 MEMO, REG_DATE, REG_TIME 컬럼은 INSERT 대상에서 제외되어 있으며, SHELF_LIFE는 포함된다(DBHandler.java:L1089).
- search_barcode_info_nonfixed.jsp의 PACKER_CLIENT_CODE는 '이마트용' 하드코딩 값이다(JSP L38).

---

## 9. 관련 문서

- `app/doc/소스분석/46_이마트바코드정보조회_vs_비정량바코드정보조회_JSP_원본비교분석.md`
- `app/doc/소스분석/50_바코드_중복검사_우회_searchType별_분기.md`
- `app/doc/소스분석/45_비정량_바코드정보조회_JSP_컬럼_사용분석.md`
- `app/doc/개발/36_비정량_바코드정보조회_JSP_MSSQL전환.md`
