# S_BARCODE_INFO 분석 문서

**분석일**: 2026-03-04
**분석 기준**: 실제 JSP (`app/doc/JSP/search_barcode_info.jsp`) + Java 소스 코드

---

## 1. 개요

바코드 파싱 규칙 테이블 분석 문서입니다.

| 항목     | 내용                           |
| ------ | ---------------------------- |
| 스키마    | HIGHLAND / INNO              |
| 테이블명   | S_BARCODE_INFO               |
| 용도     | 바코드 스캔 시 중량/제조일/박스번호 추출 규칙 |
| 구조     | 3개 테이블 JOIN                  |
| 총 컬럼 수 | **25개** (비정량은 24개)           |

### 1.1 관련 파일

| 파일 | 역할 |
|------|------|
| `app/doc/JSP/search_barcode_info.jsp` | 서버 조회 JSP |
| `app/doc/JSP/search_barcode_info_nonfixed.jsp` | 비정량 조회 JSP |
| `ProgressDlgBarcodeSearch.java` | 서버 데이터 파싱 |
| `BixolonShipmentActivity.java` | 바코드 파싱 사용 |
| `LabelPrintHelper.java` | 라벨 출력 시 SHELF_LIFE 사용 |
| `DBHandler.java` | 로컬 DB 저장/조회 |
| `Barcodes_Info.java` | 데이터 모델 (DTO) |

---

## 2. JOIN 테이블 구조

### 2.1 메인 테이블

| Alias | 테이블명 | 역할 |
|-------|----------|------|
| SBI | S_BARCODE_INFO | 바코드 파싱 규칙 (주 테이블) |
| BI | B_ITEM | 품목 마스터 (상품명 조회) |
| BSI | B_SUPPLIER_ITEM | 공급사 품목 (유통기한 조회) |

### 2.2 JOIN 관계도

```
S_BARCODE_INFO (SBI)
    │
    ├─── INNER JOIN B_ITEM (BI)
    │         ON SBI.ITEMCODE = BI.ITEM_CODE
    │         AND BI.STATUS = 'Y'
    │
    └─── INNER JOIN B_SUPPLIER_ITEM (BSI)
              ON SBI.PACKER_CLIENT_CODE = BSI.PACKER_CODE
              AND SBI.PACKER_PRODUCT_CODE = BSI.PACKER_PRODUCT_CODE
              AND SBI.STATUS = 'Y'
              AND BSI.STATUS = 'Y'
```

---

## 3. 컬럼 목록 (25개)

### 3.1 JSP SELECT 순서

| temp[] | 컬럼명 | 데이터 출처 | 설명 | 용도 |
|:------:|--------|-----------|------|------|
| 0 | PACKER_CLIENT_CODE | SBI.PACKER_CLIENT_CODE | 패커 거래처 코드 | 서버전송 |
| 1 | PACKER_PRODUCT_CODE | SBI.PACKER_PRODUCT_CODE | 패커 상품코드 | 화면표시, 상품매칭 |
| 2 | PACKER_PRD_NAME | SBI.PACKER_PRD_NAME | 패커 상품명 | 미사용 |
| 3 | ITEM_CODE | SBI.ITEMCODE | 아이템코드 | 미사용 |
| 4 | ITEM_NAME_KR | BI.ITEM_NAME_KR | 한글 상품명 | 화면표시 |
| 5 | BRAND_CODE | SBI.BRAND_CODE | 브랜드코드 | 미사용 (UPDATE 데드코드) |
| 6 | BARCODEGOODS | SBI.BARCODEGOODS | 바코드 상품코드 | 바코드파싱 (상품매칭) |
| 7 | BASEUNIT | SBI.BASEUNIT | LB/KG 구분 | 바코드파싱, 서버전송 |
| 8 | ZEROPOINT | SBI.ZEROPOINT | 소수점 자리수 | 바코드파싱 (중량계산) |
| 9 | PACKER_PRD_CODE_FROM | SBI.PACKER_PRD_CODE_FROM | 패커코드 시작위치 | 미사용 |
| 10 | PACKER_PRD_CODE_TO | SBI.PACKER_PRD_CODE_TO | 패커코드 끝위치 | 미사용 |
| 11 | BARCODEGOODS_FROM | SBI.BARCODEGOODS_FROM | 바코드상품코드 시작위치 | 바코드파싱 |
| 12 | BARCODEGOODS_TO | SBI.BARCODEGOODS_TO | 바코드상품코드 끝위치 | 바코드파싱, DB조회조건 |
| 13 | WEIGHT_FROM | SBI.WEIGHT_FROM | 중량 시작위치 | 바코드파싱 |
| 14 | WEIGHT_TO | SBI.WEIGHT_TO | 중량 끝위치 | 바코드파싱 |
| 15 | MAKINGDATE_FROM | SBI.MAKINGDATE_FROM | 제조일 시작위치 | 바코드파싱, 로직분기 |
| 16 | MAKINGDATE_TO | SBI.MAKINGDATE_TO | 제조일 끝위치 | 바코드파싱, 로직분기 |
| 17 | BOXSERIAL_FROM | SBI.BOXSERIAL_FROM | 박스번호 시작위치 | 바코드파싱 |
| 18 | BOXSERIAL_TO | SBI.BOXSERIAL_TO | 박스번호 끝위치 | 바코드파싱 |
| 19 | STATUS | SBI.STATUS | 사용여부 (Y/N) | 서버조건만 (앱 미참조) |
| 20 | REG_ID | SBI.REG_ID | 등록자 ID | 미사용 |
| 21 | REG_DATE | SBI.REG_DATE | 등록 날짜 | 미사용 (INSERT 누락) |
| 22 | REG_TIME | SBI.REG_TIME | 등록 시간 | 미사용 (INSERT 누락) |
| 23 | MEMO | SBI.MEMO | 메모 | 미사용 (INSERT 누락) |
| 24 | SHELF_LIFE | BSI.SHELF_LIFE | 유통기한(일수) | 로직분기, 라벨출력 |

### 3.2 용도별 체크표

| temp[] | 컬럼명 | 서버전송 | 화면표시 | 바코드파싱 | 라벨출력 | 로직분기 | 미사용 | 비고 |
|:------:|--------|:------:|:------:|:--------:|:------:|:------:|:----:|------|
| 0 | PACKER_CLIENT_CODE | ✓ | | | | | | Goodswets_Info에 설정 |
| 1 | PACKER_PRODUCT_CODE | | ✓ | | | | | edit_product_code |
| 2 | PACKER_PRD_NAME | | | | | | ✓ | ITEM_NAME_KR 사용 |
| 3 | ITEM_CODE | | | | | | ✓ | 저장만 됨 |
| 4 | ITEM_NAME_KR | | ✓ | | | | | edit_product_name |
| 5 | BRAND_CODE | | | | | | ✓ | 데드코드에서만 사용 |
| 6 | BARCODEGOODS | | | ✓ | | | | 스캔 바코드 상품 매칭 |
| 7 | BASEUNIT | ✓ | | ✓ | | | | LB→KG 환산, WEIGHT_UNIT |
| 8 | ZEROPOINT | | | ✓ | | | | weight / 10^ZEROPOINT |
| 9 | PACKER_PRD_CODE_FROM | | | | | | ✓ | 저장만 됨 |
| 10 | PACKER_PRD_CODE_TO | | | | | | ✓ | 저장만 됨 |
| 11 | BARCODEGOODS_FROM | | | ✓ | | | | substring 시작위치 |
| 12 | BARCODEGOODS_TO | | | ✓ | | | | substring 끝위치 |
| 13 | WEIGHT_FROM | | | ✓ | | | | 중량 추출 시작 |
| 14 | WEIGHT_TO | | | ✓ | | | | 중량 추출 끝 |
| 15 | MAKINGDATE_FROM | | | ✓ | | ✓ | | 제조일 추출, 필수여부 체크 |
| 16 | MAKINGDATE_TO | | | ✓ | | ✓ | | 제조일 추출, 필수여부 체크 |
| 17 | BOXSERIAL_FROM | | | ✓ | | | | 박스번호 추출 시작 |
| 18 | BOXSERIAL_TO | | | ✓ | | | | 박스번호 추출 끝 |
| 19 | STATUS | | | | | | ✓ | 서버 JOIN 조건만 |
| 20 | REG_ID | | | | | | ✓ | 저장만 됨 |
| 21 | REG_DATE | | | | | | ✓ | INSERT 누락 |
| 22 | REG_TIME | | | | | | ✓ | INSERT 누락 |
| 23 | MEMO | | | | | | ✓ | INSERT 누락 |
| 24 | SHELF_LIFE | | | | ✓ | ✓ | | 소비기한 계산, 필수여부 체크 |

### 3.3 용도별 컬럼 수

| 용도 | 컬럼 수 |
|------|:------:|
| 서버전송 | 2 |
| 화면표시 | 2 |
| 바코드파싱 | 11 |
| 라벨출력 | 1 |
| 로직분기 | 3 |
| 미사용 | 10 |

---

## 4. searchType별 JSP 분기

| searchType | 설명 | JSP 엔드포인트 | 컬럼 수 | WHERE 조건 |
|:---:|---|---|:---:|---|
| 0 | 이마트 | search_barcode_info.jsp | 25 | PACKER_PRODUCT_CODE |
| 1 | 생산 | search_barcode_info.jsp | 25 | PACKER_PRODUCT_CODE |
| 2 | 홈플러스 | search_barcode_info.jsp | 25 | PACKER_PRODUCT_CODE |
| 3 | 도매 | search_barcode_info.jsp | 25 | PACKER_PRODUCT_CODE |
| 4 | 비정량 | search_barcode_info_nonfixed.jsp | **24** (SHELF_LIFE 없음) | ITEM_CODE |
| 5 | 홈플러스 비정량 | search_homeplus_nonfixed2.jsp | **24** (SHELF_LIFE 없음) | ITEM_CODE |
| 6 | 롯데 | search_barcode_info.jsp | 25 | PACKER_PRODUCT_CODE |
| 7 | 생산(라벨) | search_barcode_info.jsp | 25 | PACKER_PRODUCT_CODE |

---

## 5. WHERE 조건

### 5.1 서버 WHERE (JSP)

```sql
-- STATUS 조건은 WHERE가 아닌 JOIN ON 절에 위치
FROM S_BARCODE_INFO SBI
  INNER JOIN B_ITEM BI
    ON SBI.ITEMCODE = BI.ITEM_CODE AND BI.STATUS = 'Y'
  INNER JOIN B_SUPPLIER_ITEM BSI
    ON SBI.PACKER_CLIENT_CODE = BSI.PACKER_CODE
   AND SBI.PACKER_PRODUCT_CODE = BSI.PACKER_PRODUCT_CODE
   AND SBI.STATUS = 'Y' AND BSI.STATUS = 'Y'
-- WHERE 절은 PACKER_PRODUCT_CODE 조건만 포함
WHERE SBI.PACKER_PRODUCT_CODE = '<패커상품코드1>'
   OR SBI.PACKER_PRODUCT_CODE = '<패커상품코드2>'
   ...
```

### 5.2 앱에서 WHERE 구성

```java
// ProgressDlgBarcodeSearch.java:44-66
// TB_SHIPMENT에서 DISTINCT PACKER_PRODUCT_CODE 목록 조회
ArrayList<String[]> list_code_info = DBHandler.selectqueryCodeList(mContext);
for (int i = 0; i < list_code_info.size(); i++) {
    if (i == list_code_info.size() - 1) {
        data = data + "SBI.PACKER_PRODUCT_CODE = '" + list_code_info.get(i)[0].toString() + "'";
    } else {
        data = data + "SBI.PACKER_PRODUCT_CODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
    }
}
// 비정량(searchType 4/5)은 selectqueryCodeListForNonFixed() + SBI.ITEM_CODE 사용
```

### 5.3 로컬 DB SELECT 조건

```sql
-- selectqueryBarcodeInfo()
SELECT * FROM TB_BARCODE_INFO WHERE BARCODEGOODS_TO != ''
```

---

## 6. 데이터 흐름

```
[Oracle DB]
  S_BARCODE_INFO (SBI) + B_ITEM (BI) + B_SUPPLIER_ITEM (BSI)
    ↓ HTTP 요청 (ProgressDlgBarcodeSearch)
    ↓ 응답 형식: row1;;row2;; (행 구분: ;;)
    ↓ 컬럼 형식: col1::col2:: (열 구분: ::)
[파싱]
    ↓ Barcodes_Info 객체 생성 (temp[0]~temp[24])
[로컬 DB 저장]
    ↓ DBHandler.insertqueryBarcodeInfo() → TB_BARCODE_INFO (22개 컬럼)
[바코드 스캔 시 사용]
    ↓ selectqueryBarcodeInfo() → ArrayList<Barcodes_Info>
    ↓ find_work_info() → 바코드 상품 매칭
    ↓ setBarcodeMsg() → 중량/제조일/박스번호 추출
[라벨 출력]
    ↓ LabelPrintHelper → SHELF_LIFE로 소비기한 계산
```

---

## 7. 바코드 파싱 로직

### 7.1 상품 매칭 (find_work_info)

```java
// BixolonShipmentActivity.java:1720-1736
String bg = bi.getBARCODEGOODS();               // DB 바코드 상품코드
String bg_from = bi.getBARCODEGOODS_FROM();     // 추출 시작 위치
String bg_to = bi.getBARCODEGOODS_TO();         // 추출 끝 위치

temp_bg = barcode.substring(bg_from - 1, bg_to); // 스캔 바코드에서 추출
if (temp_bg.equals(bg)) { /* 매칭 성공 */ }
```

### 7.2 중량 추출

```java
// BixolonShipmentActivity.java:1278-1290
item_weight = barcode.substring(WEIGHT_FROM - 1, WEIGHT_TO);
double item_pow = Math.pow(10, Integer.parseInt(ZEROPOINT));
item_weight_double = Double.parseDouble(item_weight) / item_pow;

if ("LB".equals(BASEUNIT)) {
    item_weight_double = item_weight_double * 0.453592; // LB → KG 변환
}
```

### 7.3 제조일 추출

```java
item_making_date = barcode.substring(MAKINGDATE_FROM - 1, MAKINGDATE_TO);
```

### 7.4 박스번호 추출

```java
item_box_serial = barcode.substring(BOXSERIAL_FROM - 1, BOXSERIAL_TO);
```

### 7.5 소비기한 계산 (라벨 출력)

```java
// LabelPrintHelper.java:270-311
// 소비기한 = 제조일 + (SHELF_LIFE - 1)일
int shelfLifeDays = Integer.parseInt(bi.getSHELF_LIFE()) - 1;
Calendar cal = Calendar.getInstance();
cal.setTime(makingDateFormat.parse(making_date));
cal.add(Calendar.DATE, shelfLifeDays);
```

---

## 8. INSERT/SELECT 불일치

서버에서 25개 컬럼을 파싱하지만 SQLite에는 **22개만 INSERT**됨.

| 컬럼 | 파싱 (temp[]) | SQLite INSERT | 비고 |
|------|:---:|:---:|------|
| REG_DATE | temp[21] ✓ | **X** | INSERT문에 누락 |
| REG_TIME | temp[22] ✓ | **X** | INSERT문에 누락 |
| MEMO | temp[23] ✓ | **X** | INSERT문에 누락 |

---

## 9. DB 메서드 목록

| 메서드 | 라인 | 설명 |
|--------|------|------|
| createqueryBarcodeInfo() | 699 | CREATE TABLE |
| selectqueryBarcodeInfo() | 746 | 전체 조회 (WHERE BARCODEGOODS_TO != '') |
| selectqueryBarcodeGoodsInfo() | 832 | 위와 동일 (중복 메서드) |
| insertqueryBarcodeInfo() | 918 | INSERT 22개 컬럼 |
| updatequeryBarcodeInfo() | 990 | UPDATE (데드코드 - 미사용) |
| deletequeryBarcodeInfo() | 1030 | 전체 삭제 + autoincrement 리셋 |

---

## 10. 컬럼별 상세 사용처 (25개)

### 10.1 상품 식별 그룹 (temp[0]~temp[5])

| temp[] | 컬럼명                 | 언제          | 어디서           | 어떻게                                                           |
| :----: | ------------------- | ----------- | ------------- | ------------------------------------------------------------- |
|   0    | PACKER_CLIENT_CODE  | 계근 데이터 생성 시 | BSA:1512      | `gi.setPACKER_CLIENT_CODE(bi.getPACKER_CLIENT_CODE())` → 서버전송 |
|   1    | PACKER_PRODUCT_CODE | 바코드 매칭 성공 시 | BSA:1740,1758 | `edit_product_code.setText(bi.getPACKER_PRODUCT_CODE())`      |
|   2    | PACKER_PRD_NAME     | 사용 안함       | -             | SQLite 저장만, ITEM_NAME_KR 사용                                   |
|   3    | ITEM_CODE           | 사용 안함       | -             | SQLite 저장만                                                    |
|   4    | ITEM_NAME_KR        | 바코드 매칭 성공 시 | BSA:1739,1757 | `edit_product_name.setText(bi.getITEM_NAME_KR())`             |
|   5    | BRAND_CODE          | 사용 안함       | -             | updatequeryBarcodeInfo() WHERE 조건 (데드코드)                      |

### 10.2 바코드 파싱 핵심 그룹 (temp[6]~temp[18])

| temp[] | 컬럼명                  | 언제       | 어디서           | 어떻게                                  |
| :----: | -------------------- | -------- | ------------- | ------------------------------------ |
|   6    | BARCODEGOODS         | 바코드 스캔 시 | BSA:1720,1736 | 스캔 바코드 substring과 비교하여 상품 매칭         |
|   7    | BASEUNIT             | 중량 환산 시  | BSA:1290,1509 | "LB"이면 ×0.453592, WEIGHT_UNIT으로 서버전송 |
|   8    | ZEROPOINT            | 중량 계산 시  | BSA:1281      | `weight / 10^ZEROPOINT` 소수점 적용       |
|   9    | PACKER_PRD_CODE_FROM | 사용 안함    | -             | SQLite 저장만                           |
|   10   | PACKER_PRD_CODE_TO   | 사용 안함    | -             | SQLite 저장만                           |
|   11   | BARCODEGOODS_FROM    | 바코드 스캔 시 | BSA:1721,1728 | `barcode.substring(FROM-1, TO)` 시작위치 |
|   12   | BARCODEGOODS_TO      | 바코드 스캔 시 | BSA:1722,1727 | `barcode.substring(FROM-1, TO)` 끝위치  |
|   13   | WEIGHT_FROM          | 바코드 스캔 시 | BSA:1267,1278 | 중량 문자열 추출 시작위치                       |
|   14   | WEIGHT_TO            | 바코드 스캔 시 | BSA:1268,1278 | 중량 문자열 추출 끝위치                        |
|   15   | MAKINGDATE_FROM      | 바코드 스캔 시 | BSA:1308,1310 | 제조일 추출 시작; 빈값이면 소비기한 필수 체크           |
|   16   | MAKINGDATE_TO        | 바코드 스캔 시 | BSA:1308,1310 | 제조일 추출 끝; 빈값이면 소비기한 필수 체크            |
|   17   | BOXSERIAL_FROM       | 바코드 스캔 시 | BSA:1314,1316 | 박스번호 추출 시작위치                         |
|   18   | BOXSERIAL_TO         | 바코드 스캔 시 | BSA:1314,1316 | 박스번호 추출 끝위치                          |

### 10.3 관리/기타 그룹 (temp[19]~temp[24])

| temp[] | 컬럼명        | 언제        | 어디서                         | 어떻게                              |
| :----: | ---------- | --------- | --------------------------- | -------------------------------- |
|   19   | STATUS     | 사용 안함     | JSP JOIN 조건                 | `SBI.STATUS = 'Y'` 서버에서만 필터      |
|   20   | REG_ID     | 사용 안함     | DBHandler                   | SQLite 저장만                       |
|   21   | REG_DATE   | 사용 안함     | -                           | 파싱만 되고 INSERT 누락                 |
|   22   | REG_TIME   | 사용 안함     | -                           | 파싱만 되고 INSERT 누락                 |
|   23   | MEMO       | 사용 안함     | -                           | 파싱만 되고 INSERT 누락                 |
|   24   | SHELF_LIFE | 소비기한 계산 시 | BSA:1186,1235 / LPH:270,311 | 빈값이면 에러, 제조일+SHELF_LIFE로 소비기한 계산 |

### 10.4 컬럼별 데이터 출처 상세

> W_GOODS_WET 10.2와 동일 형식. **PDBS** = ProgressDlgBarcodeSearch.java, **DBH** = DBHandler.java

|  #  | 컬럼                   | 서버 출처 | 코드 위치                               | 설명                                                              | 비고                  |
| :-: | -------------------- | ----- | ----------------------------------- | --------------------------------------------------------------- | ------------------- |
|  1  | PACKER_CLIENT_CODE   | SBI   | PDBS:109 `temp[0]` → DBH:949        | 패커 거래처 코드. 계근 시 Goodswets_Info에 설정 → 서버전송                       |                     |
|  2  | PACKER_PRODUCT_CODE  | SBI   | PDBS:110 `temp[1]` → DBH:951        | 패커 상품코드. edit_product_code 화면표시, TB_SHIPMENT 매칭 키               |                     |
|  3  | PACKER_PRD_NAME      | SBI   | PDBS:111 `temp[2]` → DBH:952        | 패커 상품명. **미사용** — ITEM_NAME_KR이 대신 사용됨                          | 미사용                 |
|  4  | ITEM_CODE            | SBI   | PDBS:112 `temp[3]` → DBH:953        | 하이랜드 상품코드. **미사용** — SQLite 저장만                                 | 미사용                 |
|  5  | ITEM_NAME_KR         | BI    | PDBS:113 `temp[4]` → DBH:954        | 한글 상품명. edit_product_name 화면표시                                  |                     |
|  6  | BRAND_CODE           | SBI   | PDBS:114 `temp[5]` → DBH:950        | 브랜드코드. **미사용** — updatequeryBarcodeInfo 데드코드에서만 참조              | 미사용                 |
|  7  | BARCODEGOODS         | SBI   | PDBS:115 `temp[6]` → DBH:955        | 바코드 상품코드. 스캔 바코드 substring과 비교하여 상품 매칭 (BSA:1720)               |                     |
|  8  | BASEUNIT             | SBI   | PDBS:116 `temp[7]` → DBH:956        | 중량 단위 (KG/LB). LB이면 ×0.453592 환산 (BSA:1290). WEIGHT_UNIT으로 서버전송 |                     |
|  9  | ZEROPOINT            | SBI   | PDBS:117 `temp[8]` → DBH:957        | 소수점 자리수. weight / 10^ZEROPOINT 계산 (BSA:1281)                    |                     |
| 10  | PACKER_PRD_CODE_FROM | SBI   | PDBS:118 `temp[9]` → DBH:958        | 패커코드 추출 시작위치. **미사용** — SQLite 저장만                              | 미사용                 |
| 11  | PACKER_PRD_CODE_TO   | SBI   | PDBS:119 `temp[10]` → DBH:959       | 패커코드 추출 끝위치. **미사용** — SQLite 저장만                               | 미사용                 |
| 12  | BARCODEGOODS_FROM    | SBI   | PDBS:120 `temp[11]` → DBH:960       | 바코드 상품코드 추출 시작위치. barcode.substring(FROM-1, TO)                 |                     |
| 13  | BARCODEGOODS_TO      | SBI   | PDBS:121 `temp[12]` → DBH:961       | 바코드 상품코드 추출 끝위치. selectqueryBarcodeInfo WHERE 조건                |                     |
| 14  | WEIGHT_FROM          | SBI   | PDBS:122 `temp[13]` → DBH:962       | 중량 추출 시작위치. barcode.substring(FROM-1, TO) (BSA:1278)            |                     |
| 15  | WEIGHT_TO            | SBI   | PDBS:123 `temp[14]` → DBH:963       | 중량 추출 끝위치. barcode.substring(FROM-1, TO) (BSA:1278)             |                     |
| 16  | MAKINGDATE_FROM      | SBI   | PDBS:124 `temp[15]` → DBH:964       | 제조일 추출 시작위치. 빈값이면 소비기한 수동입력 필수 (BSA:1308)                       |                     |
| 17  | MAKINGDATE_TO        | SBI   | PDBS:125 `temp[16]` → DBH:965       | 제조일 추출 끝위치. 빈값이면 소비기한 수동입력 필수 (BSA:1310)                        |                     |
| 18  | BOXSERIAL_FROM       | SBI   | PDBS:126 `temp[17]` → DBH:966       | 박스번호 추출 시작위치. barcode.substring(FROM-1, TO) (BSA:1314)          |                     |
| 19  | BOXSERIAL_TO         | SBI   | PDBS:127 `temp[18]` → DBH:967       | 박스번호 추출 끝위치. barcode.substring(FROM-1, TO) (BSA:1316)           |                     |
| 20  | STATUS               | SBI   | PDBS:128 `temp[19]` → DBH:968       | 사용여부 (Y/N). **서버 JOIN 조건만** — 앱에서 미참조                           |                     |
| 21  | REG_ID               | SBI   | PDBS:129 `temp[20]` → DBH:969       | 등록자 ID. **미사용** — SQLite 저장만                                    |                     |
| 22  | REG_DATE             | SBI   | PDBS:130 `temp[21]` → **INSERT 누락** | 등록 날짜. 파싱만 되고 SQLite에 저장 안됨 (CREATE TABLE에는 컬럼 존재)              |                     |
| 23  | REG_TIME             | SBI   | PDBS:131 `temp[22]` → **INSERT 누락** | 등록 시간. 파싱만 되고 SQLite에 저장 안됨 (CREATE TABLE에는 컬럼 존재)              |                     |
| 24  | MEMO                 | SBI   | PDBS:132 `temp[23]` → **INSERT 누락** | 메모. 파싱만 되고 SQLite에 저장 안됨 (CREATE TABLE에는 컬럼 존재)                 | 미사용                 |
| 25  | SHELF_LIFE           | BSI   | PDBS:134 `temp[24]` → DBH:970       | 유통기한(일수). 제조일 + (SHELF_LIFE-1)일 = 소비기한. 라벨출력 사용 (LPH:270)       | CO_품목코드.적용단위, 적용 사용 |

> **BSA** = BixolonShipmentActivity.java, **LPH** = LabelPrintHelper.java

#### 출처별 분류 요약

| 서버 출처 | 컬럼 수 | 컬럼 목록 |
|---------|:------:|----------|
| SBI (S_BARCODE_INFO) | 23 | PACKER_CLIENT_CODE, PACKER_PRODUCT_CODE, PACKER_PRD_NAME, ITEM_CODE, BRAND_CODE, BARCODEGOODS, BASEUNIT, ZEROPOINT, PACKER_PRD_CODE_FROM/TO, BARCODEGOODS_FROM/TO, WEIGHT_FROM/TO, MAKINGDATE_FROM/TO, BOXSERIAL_FROM/TO, STATUS, REG_ID, REG_DATE, REG_TIME, MEMO |
| BI (B_ITEM) | 1 | ITEM_NAME_KR |
| BSI (B_SUPPLIER_ITEM) | 1 | SHELF_LIFE |

#### SQLite 저장 상태 요약

| 상태 | 컬럼 수 | 컬럼 목록 |
|------|:------:|----------|
| INSERT 포함 | 22 | temp[0]~temp[20] + temp[24] (SHELF_LIFE) |
| INSERT 누락 | 3 | REG_DATE (temp[21]), REG_TIME (temp[22]), MEMO (temp[23]) |
| CREATE TABLE 전용 | 1 | BARCODE_INFO_ID (AUTOINCREMENT PK) |

---

## 11. 미사용 컬럼 (10개)

| temp[] | 컬럼명 | 사유 |
|:------:|--------|------|
| 2 | PACKER_PRD_NAME | ITEM_NAME_KR 사용 (중복) |
| 3 | ITEM_CODE | 저장만 됨, 비즈니스 로직 미사용 |
| 5 | BRAND_CODE | updatequeryBarcodeInfo() 데드코드에서만 참조 |
| 9 | PACKER_PRD_CODE_FROM | 저장만 됨, 앱에서 미사용 |
| 10 | PACKER_PRD_CODE_TO | 저장만 됨, 앱에서 미사용 |
| 19 | STATUS | 서버 JOIN 조건만, 앱에서 미참조 |
| 20 | REG_ID | 저장만 됨 |
| 21 | REG_DATE | INSERT 누락 (파싱 후 소멸) |
| 22 | REG_TIME | INSERT 누락 (파싱 후 소멸) |
| 23 | MEMO | INSERT 누락 (파싱 후 소멸) |

---

## 12. 실제 사용 컬럼 (15개)

| temp[] | 컬럼명 | 용도 |
|:------:|--------|------|
| 0 | PACKER_CLIENT_CODE | 서버전송 (Goodswets_Info에 설정) |
| 1 | PACKER_PRODUCT_CODE | 화면표시 (edit_product_code) |
| 4 | ITEM_NAME_KR | 화면표시 (edit_product_name) |
| 6 | BARCODEGOODS | 바코드파싱 (상품 매칭) |
| 7 | BASEUNIT | 바코드파싱 (LB/KG), 서버전송 (WEIGHT_UNIT) |
| 8 | ZEROPOINT | 바코드파싱 (소수점 계산) |
| 11 | BARCODEGOODS_FROM | 바코드파싱 (상품코드 추출 시작) |
| 12 | BARCODEGOODS_TO | 바코드파싱 (상품코드 추출 끝) |
| 13 | WEIGHT_FROM | 바코드파싱 (중량 추출 시작) |
| 14 | WEIGHT_TO | 바코드파싱 (중량 추출 끝) |
| 15 | MAKINGDATE_FROM | 바코드파싱 (제조일 추출), 로직분기 (필수여부) |
| 16 | MAKINGDATE_TO | 바코드파싱 (제조일 추출), 로직분기 (필수여부) |
| 17 | BOXSERIAL_FROM | 바코드파싱 (박스번호 추출 시작) |
| 18 | BOXSERIAL_TO | 바코드파싱 (박스번호 추출 끝) |
| 24 | SHELF_LIFE | 로직분기 (필수여부), 라벨출력 (소비기한 계산) |

---

## 13. TB_SHIPMENT과의 관계

```
TB_SHIPMENT.PACKER_PRODUCT_CODE
    ↓ (다운로드 WHERE 조건)
S_BARCODE_INFO.PACKER_PRODUCT_CODE
    ↓ (바코드 파싱 규칙 제공)
바코드 스캔 → 중량/제조일/박스번호 추출
    ↓ (결과 저장)
TB_GOODS_WET
```

---

**문서 작성일**: 2026-03-04
**문서 버전**: 1.1

### 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 1.0 | 2026-03-04 | 초안 작성 (25개 컬럼 분석) |
| 1.1 | 2026-03-04 | 10.4절 컬럼별 데이터 출처 상세 추가 (출처별/저장상태 요약 포함) |
