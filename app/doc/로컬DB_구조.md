# 로컬 SQLite DB 구조

**작성일**: 2026-01-15
**소스 기준**: DBHandler.java, DBInfo.java, DBHelper.java

---

## 1. DB 정보

| 항목 | 값 |
|------|-----|
| DB명 | HIGHLAND |
| 버전 | 27 |
| 파일 | DBHelper.java:20-22 |

---

## 2. 테이블 정의 현황

### 2.1 DBInfo.java 정의 (4개)

| 테이블명 | 상수명 | CREATE 위치 |
|----------|--------|-------------|
| TB_SHIPMENT | TABLE_NAME_SHIPMENT | DBHandler.java:32-75 |
| TB_BARCODE_INFO | TABLE_NAME_BARCODE_INFO | DBHandler.java:816-844 |
| TB_GOODS_WET | TABLE_NAME_GOODS_WET | DBHandler.java:1169-1193 |
| TB_GOODS_WET_PRODUCTION_CALC | TABLE_NAME_GOODS_WET_PRODUCTION_CALC | DBHandler.java:1938-1941 |

### 2.2 실제 사용 테이블 (4개)

| 테이블 | 컬럼 수 | 용도 | 데이터 방향 |
|--------|--------|------|------------|
| TB_SHIPMENT | 41 | 출하대상 저장 | 서버 → 앱 |
| TB_BARCODE_INFO | 26 | 바코드 파싱 정보 | 서버 → 앱 |
| TB_GOODS_WET | 22 | 계근 결과 저장 | 앱 → 서버 |
| TB_GOODS_WET_PRODUCTION_CALC | 1 | 생산 계근 바코드 임시저장 | 로컬 전용 |

### 2.3 테이블 생성 위치

LoginActivity.java:65-68에서 앱 시작 시 4개 테이블 생성:

```java
DBHandler.createqueryShipment(getApplicationContext());           // Line 65
DBHandler.createqueryBarcodeInfo(getApplicationContext());        // Line 66
DBHandler.createqueryGoodsWet(getApplicationContext());           // Line 67
DBHandler.createqueryGoodsWetProductionCalc(getApplicationContext()); // Line 68
```

---

## 3. TB_SHIPMENT (출하대상)

**역할**: 서버에서 조회한 출하대상 데이터 저장
**생성**: DBHandler.java:25-86 (createqueryShipment)
**컬럼 수**: 41개
**데이터 방향**: 서버 → 앱 (다운로드 전용, 서버 전송 없음)

### 3.1 컬럼 목록

| # | 컬럼명 | 타입 | NOT NULL | 라인 |
|---|--------|------|:--------:|------|
| 1 | SHIPMENT_ID | INTEGER | PK, AUTO | 34 |
| 2 | GI_H_ID | TEXT | O | 35 |
| 3 | GI_D_ID | TEXT | O | 36 |
| 4 | EOI_ID | TEXT | O | 37 |
| 5 | ITEM_CODE | TEXT | O | 38 |
| 6 | ITEM_NAME | TEXT | O | 39 |
| 7 | EMARTITEM_CODE | TEXT | - | 40 |
| 8 | EMARTITEM | TEXT | - | 41 |
| 9 | GI_REQ_PKG | TEXT | O | 42 |
| 10 | GI_REQ_QTY | TEXT | O | 43 |
| 11 | AMOUNT | TEXT | O | 44 |
| 12 | GOODS_R_ID | TEXT | O | 45 |
| 13 | GR_REF_NO | TEXT | O | 46 |
| 14 | GI_REQ_DATE | TEXT | O | 47 |
| 15 | BL_NO | TEXT | O | 48 |
| 16 | BRAND_CODE | TEXT | O | 49 |
| 17 | BRANDNAME | TEXT | O | 50 |
| 18 | CLIENT_CODE | TEXT | O | 51 |
| 19 | CLIENTNAME | TEXT | O | 52 |
| 20 | CENTERNAME | TEXT | - | 53 |
| 21 | ITEM_SPEC | TEXT | O | 54 |
| 22 | CT_CODE | TEXT | O | 55 |
| 23 | IMPORT_ID_NO | TEXT | O | 56 |
| 24 | PACKER_CODE | TEXT | O | 57 |
| 25 | PACKERNAME | TEXT | O | 58 |
| 26 | PACKER_PRODUCT_CODE | TEXT | O | 59 |
| 27 | BARCODE_TYPE | TEXT | O | 60 |
| 28 | ITEM_TYPE | TEXT | O | 61 |
| 29 | PACKWEIGHT | TEXT | - | 62 |
| 30 | BARCODEGOODS | TEXT | - | 63 |
| 31 | STORE_IN_DATE | TEXT | - | 64 |
| 32 | EMARTLOGIS_CODE | TEXT | - | 65 |
| 33 | EMARTLOGIS_NAME | TEXT | - | 66 |
| 34 | SAVE_TYPE | TEXT | O | 67 |
| 35 | WH_AREA | TEXT | - | 68 |
| 36 | USE_NAME | TEXT | - | 69 |
| 37 | USE_CODE | TEXT | - | 70 |
| 38 | CT_NAME | TEXT | - | 71 |
| 39 | STORE_CODE | TEXT | - | 72 |
| 40 | EMART_PLANT_CODE | TEXT | - | 73 |
| 41 | LAST_BOX_ORDER | INTEGER | - | 74 |

### 3.2 데이터 원본

| 원본 | VIEW 조회 |
|------|-----------|
| 이마트 | VW_PDA_WID_LIST |
| 홈플러스 | VW_PDA_WID_HOMEPLUS_LIST |
| 도매 | VW_PDA_WID_WHOLESALE_LIST |
| 비정량 | VW_PDA_WID_LIST_NONFIXED |
| 홈플러스 비정량 | VW_PDA_WID_LIST_NONFIXED_HP |
| 롯데 | VW_PDA_WID_LIST_LOTTE |

### 3.3 주요 CRUD 메서드

| 메서드                              | 동작                         | 라인        |
| -------------------------------- | -------------------------- | --------- |
| createqueryShipment()            | CREATE TABLE               | 25-86     |
| insertqueryShipment()            | INSERT                     | 654-760   |
| selectqueryShipment()            | SELECT (조건 조회)             | 89-217    |
| selectqueryShipmentOnly()        | SELECT (바코드 조회)            | 220-322   |
| selectqueryShipmentBL()          | SELECT (BL 조회)             | 324-426   |
| selectqueryAllShipment()         | SELECT ALL                 | 429-465   |
| selectqueryCodeList()            | SELECT DISTINCT 코드 목록      | 507-542   |
| selectqueryCodeListForNonFixed() | SELECT (비고정 코드 목록)         | 544-579   |
| selectqueryGIDIDList()           | SELECT DISTINCT GI_D_ID    | 582-615   |
| selectqueryCenterList()          | SELECT DISTINCT CENTERNAME | 618-651   |
| updatequeryShipment()            | UPDATE                     | 786-809   |
| deletequeryShipment()            | DELETE ALL                 | 763-783   |
| refreshShipmentList()            | DELETE + INSERT (갱신)       | 1854-1893 |

### 3.4 CRUD 호출 시점

| 동작     | 시점                           | 위치                                      |
| ------ | ---------------------------- | --------------------------------------- |
| INSERT | 출하대상받기 버튼 클릭                 | ProgressDlgShipSearch.java              |
| INSERT | refreshShipmentList()        | DBHandler.java:1878                     |
| SELECT | 바코드 스캔 시 출하대상 매칭             | BixolonShipmentActivity.java:3324       |
| SELECT | BL번호 조회                      | BixolonShipmentActivity.java:3515       |
| UPDATE | 서버 전송 성공 후 LAST_BOX_ORDER 갱신 | BixolonShipmentActivity.java:3801, 3934 |
| DELETE | 출하대상받기 재실행 시 전체 삭제           | MainActivity.java:206, 468              |

---

## 4. TB_BARCODE_INFO (바코드 정보)

**역할**: 바코드 파싱 규칙 저장 (S_BARCODE_INFO 서버 테이블에서 조회)
**생성**: DBHandler.java:812-856 (createqueryBarcodeInfo)
**컬럼 수**: 26개
**데이터 방향**: 서버 → 앱 (다운로드 전용, 서버 전송 없음)

### 4.1 컬럼 목록

| #   | 컬럼명                  | 타입      | NOT NULL | 라인  |
| --- | -------------------- | ------- | :------: | --- |
| 1   | BARCODE_INFO_ID      | INTEGER | PK, AUTO | 818 |
| 2   | PACKER_CLIENT_CODE   | TEXT    |    O     | 819 |
| 3   | PACKER_PRODUCT_CODE  | TEXT    |    O     | 820 |
| 4   | PACKER_PRD_NAME      | TEXT    |    O     | 821 |
| 5   | ITEM_CODE            | TEXT    |    O     | 822 |
| 6   | ITEM_NAME_KR         | TEXT    |    -     | 823 |
| 7   | BRAND_CODE           | TEXT    |    O     | 824 |
| 8   | BARCODEGOODS         | TEXT    |    O     | 825 |
| 9   | BASEUNIT             | TEXT    |    O     | 826 |
| 10  | ZEROPOINT            | TEXT    |    O     | 827 |
| 11  | PACKER_PRD_CODE_FROM | TEXT    |    -     | 828 |
| 12  | PACKER_PRD_CODE_TO   | TEXT    |    -     | 829 |
| 13  | BARCODEGOODS_FROM    | TEXT    |    O     | 830 |
| 14  | BARCODEGOODS_TO      | TEXT    |    O     | 831 |
| 15  | WEIGHT_FROM          | TEXT    |    O     | 832 |
| 16  | WEIGHT_TO            | TEXT    |    O     | 833 |
| 17  | MAKINGDATE_FROM      | TEXT    |    -     | 834 |
| 18  | MAKINGDATE_TO        | TEXT    |    -     | 835 |
| 19  | BOXSERIAL_FROM       | TEXT    |    -     | 836 |
| 20  | BOXSERIAL_TO         | TEXT    |    -     | 837 |
| 21  | STATUS               | TEXT    |    -     | 838 |
| 22  | REG_ID               | TEXT    |    O     | 839 |
| 23  | REG_DATE             | TEXT    |    -     | 840 |
| 24  | REG_TIME             | TEXT    |    -     | 841 |
| 25  | MEMO                 | TEXT    |    -     | 842 |
| 26  | SHELF_LIFE           | TEXT    |    -     | 843 |

### 4.2 바코드 파싱 컬럼 설명

| 컬럼 | 용도 | 예시 |
|------|------|------|
| BARCODEGOODS_FROM/TO | 상품코드 추출 위치 | 4~16 → substring(3, 16) |
| WEIGHT_FROM/TO | 중량 추출 위치 | 17~20 → substring(16, 20) |
| MAKINGDATE_FROM/TO | 제조일 추출 위치 | 21~26 → substring(20, 26) |
| BOXSERIAL_FROM/TO | 박스번호 추출 위치 | 27~29 → substring(26, 29) |

### 4.3 주요 CRUD 메서드

| 메서드 | 동작 | 라인 |
|--------|------|------|
| createqueryBarcodeInfo() | CREATE TABLE | 812-856 |
| insertqueryBarcodeInfo() | INSERT | 1031-1100 |
| selectqueryBarcodeInfo() | SELECT (조건 조회) | 859-942 |
| selectqueryBarcodeGoodsInfo() | SELECT (상품 조회) | 945-1028 |
| updatequeryBarcodeInfo() | UPDATE | 1103-1140 |
| deletequeryBarcodeInfo() | DELETE ALL | 1143-1162 |
| selectquerySearchBarcodeInfo() | SELECT (상품코드+브랜드 조회) | 1795-1851 |

### 4.4 CRUD 호출 시점

| 동작     | 시점                  | 위치                                 |
| ------ | ------------------- | ---------------------------------- |
| INSERT | 바코드정보받기 버튼 클릭       | ProgressDlgBarcodeSearch.java:138  |
| INSERT | 신규 바코드 수동 등록        | ProgressDlgNewBarcodeInfo.java:99  |
| SELECT | 바코드 스캔 시 파싱 규칙 조회   | BixolonShipmentActivity.java:1700  |
| UPDATE | **사용 안함** (정의만 존재)  | -                                  |
| DELETE | 바코드정보받기 재실행 시 전체 삭제 | MainActivity.java:483              |
| DELETE | 계근 데이터 동기화 시 함께 삭제  | ProgressDlgGoodsWetSearch.java:150 |

---

## 5. TB_GOODS_WET (계근 결과)

**역할**: 계근 입력 결과 저장 (서버 전송 전 로컬 저장)
**생성**: DBHandler.java:1165-1205 (createqueryGoodsWet)
**컬럼 수**: 22개
**데이터 방향**: 앱 → 서버 (업로드, insert_goods_wet.jsp로 전송)

### 5.1 컬럼 목록

| # | 컬럼명 | 타입 | NOT NULL | 라인 |
|---|--------|------|:--------:|------|
| 1 | GOODS_WET_ID | INTEGER | PK, AUTO | 1171 |
| 2 | GI_D_ID | TEXT | O | 1172 |
| 3 | WEIGHT | TEXT | O | 1173 |
| 4 | WEIGHT_UNIT | TEXT | O | 1174 |
| 5 | PACKER_PRODUCT_CODE | TEXT | O | 1175 |
| 6 | BARCODE | TEXT | - | 1176 |
| 7 | PACKER_CLIENT_CODE | TEXT | O | 1177 |
| 8 | MAKINGDATE | TEXT | - | 1178 |
| 9 | BOXSERIAL | TEXT | - | 1179 |
| 10 | BOX_CNT | INTEGER | O | 1180 |
| 11 | EMARTITEM_CODE | TEXT | - | 1181 |
| 12 | EMARTITEM | TEXT | - | 1182 |
| 13 | ITEM_CODE | TEXT | - | 1183 |
| 14 | BRAND_CODE | TEXT | - | 1184 |
| 15 | REG_ID | TEXT | - | 1185 |
| 16 | REG_DATE | TEXT | - | 1186 |
| 17 | REG_TIME | TEXT | O | 1187 |
| 18 | SAVE_TYPE | TEXT | O | 1188 |
| 19 | MEMO | TEXT | - | 1189 |
| 20 | DUPLICATE | TEXT | - | 1190 |
| 21 | CLIENT_TYPE | TEXT | - | 1191 |
| 22 | BOX_ORDER | INTEGER | - | 1192 |

### 5.2 서버 전송 필드 (insert_goods_wet.jsp)

TB_GOODS_WET에서 서버로 전송되는 필드:

| 전송 필드 | TB_GOODS_WET 컬럼 |
|-----------|-------------------|
| goods_r_id | TB_SHIPMENT.GOODS_R_ID |
| eoi_id | TB_SHIPMENT.EOI_ID |
| gi_h_id | TB_SHIPMENT.GI_H_ID |
| gi_d_id | GI_D_ID |
| weight | WEIGHT |
| weight_unit | WEIGHT_UNIT |
| packer_code | TB_SHIPMENT.PACKER_CODE |
| packer_product_code | PACKER_PRODUCT_CODE |
| barcode | BARCODE |
| packer_client_code | PACKER_CLIENT_CODE |
| makingdate | MAKINGDATE |
| boxserial | BOXSERIAL |
| item_code | ITEM_CODE |
| brand_code | BRAND_CODE |

### 5.3 주요 CRUD 메서드

| 메서드 | 동작 | 라인 |
|--------|------|------|
| createqueryGoodsWet() | CREATE TABLE | 1165-1205 |
| insertqueryGoodsWet() | INSERT | 1467-1535 |
| insertqueryGoodsWetHomeplus() | INSERT (홈플러스) | 1537-1608 |
| insertqueryGoodsWetLotte() | INSERT (롯데) | 1610-1681 |
| selectqueryGoodsWet() | SELECT (조건 조회) | 1208-1275 |
| selectquerySendGoodsWet() | SELECT (전송용) | 1278-1350 |
| updatequeryGoodsWet() | UPDATE | 1685-1711 |
| deletequerySelectGoodsWet() | DELETE (조건 삭제) | 1714-1747 |
| deletequeryGoodsWet() | DELETE (전송완료분) | 1750-1770 |
| deletequeryAllGoodsWet() | DELETE ALL | 1773-1792 |
| selectqueryListGoodsWetInfo() | SELECT (GI_D_ID별 정보) | 1352-1393 |
| duplicatequeryGoodsWet_check() | SELECT (바코드 중복 체크) | 1396-1427 |
| duplicatequeryGoodsWet() | SELECT (바코드+GI_D_ID+PP_CODE 중복 체크) | 1430-1464 |
| selectMaxBoxOrder() | SELECT MAX(BOX_ORDER) | 1891-1931 |

### 5.4 CRUD 호출 시점

| 동작 | 시점 | 위치 |
|------|------|------|
| INSERT | 계근 완료 (일반: 이마트, 도매, 비정량) | BixolonShipmentActivity.java:1525 |
| INSERT | 계근 완료 (홈플러스, searchType 2) | BixolonShipmentActivity.java:1513 |
| INSERT | 계근 완료 (롯데, searchType 6) | BixolonShipmentActivity.java:1518 |
| INSERT | 서버에서 미전송 계근 데이터 동기화 | ProgressDlgGoodsWetSearch.java:116 |
| SELECT | 바코드 스캔 전 중복 체크 | BixolonShipmentActivity.java:1080, 1210 |
| SELECT | 특정 품목 계근 내역 조회 | BixolonShipmentActivity.java:4377 |
| SELECT | 전송용 데이터 조회 (SAVE_TYPE='N') | DBHandler.java:1278 |
| UPDATE | 서버 전송 성공 후 SAVE_TYPE='Y' 변경 | BixolonShipmentActivity.java:3768, 3897 |
| DELETE | 서버 전송 완료 후 SAVE_TYPE='Y' 삭제 | DBHandler.java:1750 |
| DELETE | 사용자가 개별 계근 삭제 | BixolonShipmentActivity.java:4416 |
| DELETE | 설정에서 전체 삭제 | SettingActivity.java:133 |
| DELETE | 바코드정보받기 시 함께 삭제 | MainActivity.java:484 |

---

## 6. TB_GOODS_WET_PRODUCTION_CALC (생산계근 바코드)

**역할**: 생산 계근 시 바코드 임시 저장 (중복 체크용)
**생성**: DBHandler.java:1934-1953 (createqueryGoodsWetProductionCalc)
**컬럼 수**: 1개
**데이터 방향**: 로컬 전용 (서버 통신 없음)

### 6.1 컬럼 목록

| # | 컬럼명 | 타입 | NOT NULL | 라인 |
|---|--------|------|:--------:|------|
| 1 | BARCODE | TEXT | - | 1940 |

### 6.2 주요 CRUD 메서드

| 메서드 | 동작 | 라인 |
|--------|------|------|
| createqueryGoodsWetProductionCalc() | CREATE TABLE | 1934-1953 |
| insertGoodsWetProductionCalc() | INSERT | 1956-1983 |
| selectGoodsWetProductionCalc() | SELECT (중복 체크) | 1986-2014 |
| deleteGoodsWetProductionCalc() | DELETE ALL | 2016-2033 |

### 6.3 CRUD 호출 시점

| 동작 | 시점 | 위치 |
|------|------|------|
| INSERT | 생산 계근 바코드 스캔 (중복 아닐 때만) | ProductionActivity.java:496 |
| SELECT | 바코드 스캔 시 중복 체크 | ProductionActivity.java:492 |
| UPDATE | **없음** (해당 테이블은 UPDATE 미사용) | - |
| DELETE | 생산 화면 진입 시 초기화 | ProductionActivity.java:171 |
| DELETE | 생산 작업 완료 후 | ProductionActivity.java:283 |
| DELETE | 화면 종료 시 (onDestroy) | ProductionActivity.java:623 |

---

## 7. 테이블 관계도

```
┌─────────────────────────────────────────────────────────┐
│                    서버 데이터 흐름                       │
└─────────────────────────────────────────────────────────┘
              │                              │
              ▼                              ▼
    ┌─────────────────┐           ┌───────────────────────┐
    │ VW_PDA_WID_LIST │           │    S_BARCODE_INFO     │
    │ (출하대상 VIEW)  │           │  (바코드 파싱 정보)   │
    └────────┬────────┘           └───────────┬───────────┘
             │                                │
             ▼                                ▼
    ┌─────────────────┐           ┌───────────────────────┐
    │  TB_SHIPMENT    │◄──────────│   TB_BARCODE_INFO     │
    │ (출하대상 저장)  │  바코드    │  (파싱 규칙 저장)     │
    │   41 컬럼       │  매칭     │     26 컬럼           │
    └────────┬────────┘           └───────────────────────┘
             │
             │ 계근 입력
             ▼
    ┌─────────────────┐
    │  TB_GOODS_WET   │─────────► 서버 전송
    │  (계근 결과)    │           (insert_goods_wet.jsp)
    │   22 컬럼       │
    └─────────────────┘
```

---

## 8. 데이터 흐름

### 8.1 출하대상 받기

```
1. MainActivity → "출하대상받기" 버튼 클릭
2. ProgressDlgShipSearch → 서버 VIEW 조회
3. DBHandler.insertqueryShipment() → TB_SHIPMENT에 저장 (Line 654)
```

### 8.2 바코드 정보 받기

```
1. MainActivity → "바코드정보받기" 버튼 클릭
2. ProgressBarcodeinfoSearch → S_BARCODE_INFO 조회
3. DBHandler.insertqueryBarcodeInfo() → TB_BARCODE_INFO에 저장 (Line 1031)
```

### 8.3 계근 입력

```
1. BixolonShipmentActivity → 바코드 스캔
2. TB_BARCODE_INFO에서 파싱 규칙 조회 (selectqueryBarcodeInfo, Line 859)
3. TB_SHIPMENT에서 매칭 데이터 조회 (selectqueryShipment, Line 89)
4. 계근값 입력 후 DBHandler.insertqueryGoodsWet() → TB_GOODS_WET에 저장 (Line 1467)
5. "작업내역전송" → insert_goods_wet.jsp로 서버 전송
```

---

## 9. 관련 파일

| 파일 | 역할 |
|------|------|
| DBInfo.java | 테이블명, 컬럼명 상수 정의 |
| DBHandler.java | CRUD 메서드 구현 |
| DBHelper.java | SQLiteOpenHelper 래퍼 |
| LoginActivity.java | 테이블 생성 호출 (Line 65-68) |
| BixolonShipmentActivity.java | TB_GOODS_WET 데이터 생성 |
| ProgressDlgShipSearch.java | TB_SHIPMENT 데이터 생성 |
| ProgressBarcodeinfoSearch.java | TB_BARCODE_INFO 데이터 생성 |

---

## 10. DEFAULT 값

| 테이블 | 컬럼 | DEFAULT | 라인 |
|--------|------|---------|------|
| TB_GOODS_WET | BOX_ORDER | 0 | 1192 |

**참고**: 다른 컬럼들은 DEFAULT 값이 없음. NULL 허용 컬럼은 빈 문자열 저장 가능.

---

## 11. 중복 체크 로직

### 11.1 duplicatequeryGoodsWet_check()

**용도**: 바코드 단순 중복 체크 (생산 계근)
**위치**: DBHandler.java:1396-1427

```
바코드 스캔
    │
    ▼
SELECT COUNT(*) FROM TB_GOODS_WET WHERE BARCODE = ?
    │
    ├─ count > 0 → 중복 (true 반환)
    │
    └─ count = 0 → 중복 아님 (false 반환)
```

### 11.2 duplicatequeryGoodsWet()

**용도**: 바코드 + GI_D_ID + PACKER_PRODUCT_CODE 복합 중복 체크 (출하 계근)
**위치**: DBHandler.java:1430-1464

```
바코드 스캔
    │
    ▼
SELECT COUNT(*) FROM TB_GOODS_WET
WHERE BARCODE = ?
  AND GI_D_ID = ?
  AND PACKER_PRODUCT_CODE = ?
    │
    ├─ count > 0 → 중복 (true 반환)
    │
    └─ count = 0 → 중복 아님 (false 반환)
```

**차이점**:
| 메서드 | 조건 | 사용 화면 |
|--------|------|----------|
| duplicatequeryGoodsWet_check | BARCODE만 | 생산 계근 (searchType 1, 7) |
| duplicatequeryGoodsWet | BARCODE + GI_D_ID + PP_CODE | 출하 계근 (그 외) |

---

## 12. searchType별 VIEW 매핑

| searchType | 기능 | 서버 VIEW |
|:----------:|------|-----------|
| 0 | 이마트 출하 | VW_PDA_WID_LIST |
| 1 | 생산 계근 | VW_PDA_WID_PRODUCTION_LIST |
| 2 | 홈플러스 출하 | VW_PDA_WID_HOMEPLUS_LIST |
| 3 | 도매업체 출하 | VW_PDA_WID_WHOLESALE_LIST |
| 4 | 비정량 출하 | VW_PDA_WID_LIST_NONFIXED |
| 5 | 홈플러스 비정량 | VW_PDA_WID_LIST_NONFIXED_HP |
| 6 | 롯데 출하 | VW_PDA_WID_LIST_LOTTE |
| 7 | 생산 라벨 | VW_PDA_WID_PRODUCTION_LIST |

---

## 13. INDEX 정보

**현재 상태**: 별도 INDEX 생성 없음

모든 테이블은 PRIMARY KEY (AUTOINCREMENT)만 사용:
- TB_SHIPMENT: SHIPMENT_ID
- TB_BARCODE_INFO: BARCODE_INFO_ID
- TB_GOODS_WET: GOODS_WET_ID

**검색 성능 고려 사항**:
- TB_SHIPMENT.GI_D_ID - 자주 조회되나 INDEX 없음
- TB_GOODS_WET.BARCODE - 중복 체크에 사용되나 INDEX 없음
- 데이터량이 적어 INDEX 미적용 상태

---

**최종 수정일**: 2026-01-15
