# 이마트 계근 VIEW 컬럼 사용 현황

## 1. 개요

이 문서는 이마트 출하 계근 리스트 조회 VIEW에서 사용하는 컬럼들의 용도와 사용 위치를 정리한 문서입니다.

- **작성일**: 2025-12-05
- **관련 파일**:
  - `ProgressDlgShipSearch.java` - 서버에서 데이터 조회
  - `ShipmentActivity.java` - 계근 입력 화면
  - `DBHandler.java` - 로컬 DB 저장
  - `Shipments_Info.java` - 데이터 모델

---

## 2. VIEW 테이블 구조

### 2.1 사용 테이블

| 테이블 | 별칭 | 역할 |
|--------|------|------|
| W_GOODS_IH | IH | 출고 헤더 |
| W_GOODS_ID | ID | 출고 상세 |
| I_BL_D | BD | 해외 BL 상세 (해외매입) |
| I_OFFER_D | OD | 국내 Offer 상세 (국내매입) |
| E_EOI | EOI | 이마트 발주 정보 |
| B_EMART_BARCODE | EB | 이마트 바코드 정보 |
| B_SUPPLIER_ITEM | BSI | 공급업체 상품 |
| B_COMMON_CODE | BCC | 공통코드 마스터 |
| W_GOODS_R | WR | 입고 정보 |

### 2.2 사용 함수

| 함수 | 용도 | 입력값 |
|------|------|--------|
| DE_ITEM() | 상품코드 → 상품명 조회 | ITEM_CODE |
| DE_COMMON() | 공통코드 → 브랜드명 조회 | 'BRAND', BRAND_CODE |
| DE_CLIENT() | 거래처코드 → 거래처명 조회 | CLIENT_CODE / PACKER_CODE |
| DE_CLIENT2() | 거래처명 조회 (CJ센터용) | CLIENT_CODE |

---

## 3. 컬럼 사용 현황

### 3.1 사용 컬럼 목록 (38개)

| 번호 | 컬럼명 | 데이터타입 | 사용 목적 | 사용 위치 |
|------|--------|------------|-----------|-----------|
| 1 | GI_H_ID | NUMBER | 출고 헤더 ID, 로컬 DB 저장 | DBHandler:705 |
| 2 | GI_D_ID | NUMBER | 출고 상세 ID (PK), 계근 데이터 매핑, 전송 시 식별자 | ShipmentActivity 전반 |
| 3 | EOI_ID | NUMBER(22) | 이마트 발주 ID, 로컬 DB 저장 | DBHandler:707 |
| 4 | ITEM_CODE | VARCHAR2(20) | 상품코드, 계근완료 전송 시 사용 | ShipmentActivity:3370, 3502 |
| 5 | ITEM_NAME | VARCHAR2(32767) | 상품명, 화면 표시 | ShipmentActivity:1573, 3171 |
| 6 | EMARTITEM_CODE | VARCHAR2(20) | 이마트 상품코드, 바코드 생성 | ShipmentActivity:1125, 1877 등 |
| 7 | EMARTITEM | VARCHAR2(50) | 이마트 상품명, 라벨 출력 | ShipmentActivity:1126, 2031 |
| 8 | GI_REQ_PKG | NUMBER | 출하요청수량, 진행률 표시/완료 판단 | ShipmentActivity:862, 1104, 1207 등 |
| 9 | GI_REQ_QTY | NUMBER | 출하요청중량, 진행률 표시 | ShipmentActivity:1208, 1571 등 |
| 10 | AMOUNT | NUMBER | 금액, 로컬 DB 저장 | DBHandler:714 |
| 11 | GOODS_R_ID | NUMBER | 입고번호, 로컬 DB 저장 | DBHandler:715 |
| 12 | GR_REF_NO | VARCHAR2(20) | 창고입고번호, 로컬 DB 저장 | DBHandler:716 |
| 13 | GI_REQ_DATE | CHAR(8) | 출하요청일, 로컬 DB 저장 | DBHandler |
| 14 | BL_NO | VARCHAR2(50) | BL번호/이력번호, 중복 스캔 방지/BL 선택 | ShipmentActivity:772, 1318, 1544 등 |
| 15 | BRAND_CODE | VARCHAR2(5) | 브랜드코드, 계근 데이터 전송 | ShipmentActivity:1128, 3370 |
| 16 | BRANDNAME | VARCHAR2(32767) | 브랜드명, 로컬 DB 저장 | DBHandler |
| 17 | CLIENT_CODE | VARCHAR2(20) | 출고업체(점포)코드, 계근 데이터 조회 | ShipmentActivity:2953, 3135 |
| 18 | CLIENTNAME | VARCHAR2(32767) | 출고업체명, 리스트 표시 | ShipmentActivity:3019, 3020, 3175 |
| 19 | CENTERNAME | VARCHAR2(150) | 이마트 센터명, TRD/WET/E/T 판별 | ShipmentActivity:367, 791 |
| 20 | ITEM_SPEC | VARCHAR2(3000) | 상품 스펙, 로컬 DB 저장 | DBHandler:724 |
| 21 | CT_CODE | VARCHAR2(5) | 원산지코드, 로컬 DB 저장 | DBHandler:725 |
| 22 | IMPORT_ID_NO | VARCHAR2(50) | 수입식별번호(이력번호), 바코드/라벨 출력 | ShipmentActivity:1877, 3020 등 |
| 23 | PACKER_CODE | VARCHAR2(30) | 패커코드, 킬코이 제품 판별 (30228) | ShipmentActivity:344, 347, 793 등 |
| 24 | PACKERNAME | VARCHAR2(32767) | 패커명, 로컬 DB 저장 | DBHandler |
| 25 | PACKER_PRODUCT_CODE | VARCHAR2(30) | 패커 상품코드, 화면 표시/계근 데이터 매핑 | ShipmentActivity:874, 1119, 1574 등 |
| 26 | BARCODE_TYPE | VARCHAR2(10) | 바코드 타입, M0/M3/M4 분기 처리 | ShipmentActivity:833, 1735, 2349 |
| 27 | ITEM_TYPE | VARCHAR2(1 CHAR) | 상품유형 (W/J/B/S/HW), 계근 로직 분기 | ShipmentActivity:916, 917, 970, 1024, 1035 |
| 28 | PACKWEIGHT | VARCHAR2(20) | 팩 중량, 제품(J) 계근 시 중량 계산 | ShipmentActivity:1026 |
| 29 | BARCODEGOODS | VARCHAR2(50) | 바코드 상품코드, 작업 아이템 검색 | ShipmentActivity:3014 |
| 30 | STORE_IN_DATE | CHAR(8) | 점입점 일자, 로컬 DB 저장 | DBHandler |
| 31 | EMARTLOGIS_CODE | VARCHAR2(50) | 이마트 물류 상품코드, 바코드 생성 | ShipmentActivity:1880, 2349, 2379 |
| 32 | EMARTLOGIS_NAME | VARCHAR2(50) | 이마트 물류 상품명, 로컬 DB 저장 | DBHandler |
| 33 | WH_AREA | VARCHAR2(50) | 이마트 창고코드, 라벨 출력 | ShipmentActivity:2325 |
| 34 | USE_NAME | VARCHAR2(150) | 원료육 용도명, 라벨 출력 | ShipmentActivity:2021, 2031, 2265 |
| 35 | USE_CODE | VARCHAR2(10) | 원료육 용도코드, 바코드 생성 | ShipmentActivity:2027, 2028 |
| 36 | CT_NAME | VARCHAR2(152) | 원산지명, 라벨 출력 | ShipmentActivity:2258 |
| 37 | STORE_CODE | VARCHAR2(20) | 이마트 점포코드, 미트센터(9231) 판별/바코드 | ShipmentActivity:345, 347, 1848 등 |
| 38 | EMART_PLANT_CODE | VARCHAR2(20) | 이마트 가공장 코드, 미트센터 바코드 생성 | ShipmentActivity:2349, 2379, 2380 |

### 3.2 롯데 전용 컬럼 (1개)

| 번호 | 컬럼명 | 데이터타입 | 사용 목적 |
|------|--------|------------|-----------|
| 39 | LAST_BOX_ORDER | VARCHAR2 | 롯데 박스 순서 (searchType "6"에서만 사용) |

### 3.3 미사용 컬럼 (3개)

| 컬럼명 | 데이터타입 | 설명 | 미사용 사유 |
|--------|------------|------|-------------|
| GR_WAREHOUSE_CODE | VARCHAR2(20) | 입고창고코드 | WHERE 조건으로만 사용, 앱에 저장 안함 |
| MAJOR_CATEGORY | VARCHAR2(5) | 축종 | 앱에서 미사용 |
| CONTAINER_TYPE | VARCHAR2(10) | 보관(냉장/냉동) | 앱에서 미사용 |

---

## 4. 용도별 분류

### 4.1 화면 표시용 (5개)

사용자에게 정보를 표시하기 위한 컬럼

| 컬럼명 | 표시 내용 |
|--------|-----------|
| ITEM_NAME | 상품명 |
| CLIENTNAME | 출고업체명 (점포명) |
| PACKER_PRODUCT_CODE | 패커 상품코드 |
| GI_REQ_PKG | 출하요청수량 (진행률: 요청/완료) |
| GI_REQ_QTY | 출하요청중량 (진행률: 요청/완료) |

### 4.2 계근 로직 분기용 (5개)

계근 처리 로직을 분기하기 위한 컬럼

| 컬럼명 | 분기 조건 | 설명 |
|--------|-----------|------|
| ITEM_TYPE | W/J/B/S/HW | W:원료육, J:제품, B:비정량, S:세트, HW:홈플러스원료육 |
| BARCODE_TYPE | M0/M3/M4 | 바코드 유형에 따른 처리 분기 |
| PACKER_CODE | 30228 | 킬코이 제품 판별 |
| STORE_CODE | 9231 | 이마트 미트센터 판별 |
| CENTERNAME | TRD/WET/E/T 포함 여부 | 센터 유형 판별 |

### 4.3 바코드/라벨 출력용 (9개)

바코드 생성 및 라벨 출력에 사용되는 컬럼

| 컬럼명 | 용도 |
|--------|------|
| EMARTITEM_CODE | 이마트 상품코드 (바코드 앞 6자리) |
| EMARTLOGIS_CODE | 이마트 물류 상품코드 (바코드 앞 6자리) |
| IMPORT_ID_NO | 수입식별번호/이력번호 (바코드 구성) |
| USE_CODE | 원료육 용도코드 (바코드 구성) |
| USE_NAME | 원료육 용도명 (라벨 출력) |
| CT_NAME | 원산지명 (라벨 출력) |
| WH_AREA | 이마트 창고코드 (라벨 출력) |
| STORE_CODE | 점포코드 (바코드 구성) |
| EMART_PLANT_CODE | 가공장 코드 (미트센터 바코드) |

### 4.4 서버 전송용 (6개)

계근 완료 시 서버로 전송하는 데이터

| 컬럼명 | 전송 용도 |
|--------|-----------|
| GI_D_ID | 출고 상세 ID (식별자) |
| ITEM_CODE | 상품코드 |
| BRAND_CODE | 브랜드코드 |
| EMARTITEM_CODE | 이마트 상품코드 |
| EMARTITEM | 이마트 상품명 |
| PACKER_PRODUCT_CODE | 패커 상품코드 |

### 4.5 로컬 DB 저장용 (12개)

로컬 SQLite에 저장하여 추후 활용 가능한 컬럼

| 컬럼명 | 저장 목적 |
|--------|-----------|
| GI_H_ID | 출고 헤더 ID |
| EOI_ID | 이마트 발주 ID |
| AMOUNT | 금액 |
| GOODS_R_ID | 입고번호 |
| GR_REF_NO | 창고입고번호 |
| GI_REQ_DATE | 출하요청일 |
| BRANDNAME | 브랜드명 |
| ITEM_SPEC | 상품 스펙 |
| CT_CODE | 원산지코드 |
| PACKERNAME | 패커명 |
| STORE_IN_DATE | 점입점 일자 |
| EMARTLOGIS_NAME | 이마트 물류 상품명 |

---

## 5. searchType별 사용 컬럼 차이

### 5.1 searchType 매핑

| searchType | 용도 | 특이 컬럼 |
|------------|------|-----------|
| "0" | 이마트 출하대상 | WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE |
| "1" | 생산대상 | - |
| "2" | 홈플러스 하이퍼 | - |
| "3" | 도매업체 | - |
| "4" | 비정량 출하 | WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE |
| "5" | 홈플러스 비정량 | WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE |
| "6" | 롯데 | WH_AREA, LAST_BOX_ORDER |
| "7" | 생산(라벨) | - |

### 5.2 컬럼 파싱 분기 (ProgressDlgShipSearch.java)

```java
// searchType "0" 또는 "4" (이마트 출하, 비정량 출하)
if(Common.searchType.equals("0") || Common.searchType.equals("4")) {
    si.setWH_AREA(temp[32].toString());
    si.setUSE_NAME(temp[33].toString());
    si.setUSE_CODE(temp[34].toString());
    si.setCT_NAME(temp[35].toString());
    si.setSTORE_CODE(temp[36].toString());
    si.setEMART_PLANT_CODE(temp[37].toString());
}
// searchType "5" (홈플러스 비정량)
else if(Common.searchType.equals("5")) {
    si.setWH_AREA(temp[32].toString());
    si.setUSE_NAME(temp[33].toString());
    si.setUSE_CODE(temp[34].toString());
    si.setCT_NAME(temp[35].toString());
    si.setSTORE_CODE(temp[36].toString());
}
// searchType "6" (롯데)
else if(Common.searchType.equals("6")) {
    si.setWH_AREA(temp[32].toString());
    si.setLAST_BOX_ORDER(temp[33].toString());
}
```

---

## 6. 데이터 흐름

```
[서버 VIEW]
    ↓ HTTP 요청 (ProgressDlgShipSearch)
    ↓ 응답 형식: row1;;row2;;row3 (행 구분: ;;)
    ↓ 컬럼 형식: col1::col2::col3 (열 구분: ::)
[파싱]
    ↓ Shipments_Info 객체 생성
[로컬 DB 저장]
    ↓ DBHandler.insertqueryShipment()
[화면 표시]
    ↓ ShipmentActivity에서 조회/표시
[계근 처리]
    ↓ 바코드 스캔 → 중량 입력 → 라벨 출력
[서버 전송]
    ↓ 계근 완료 데이터 전송
```

---

## 7. 참고 사항

### 7.1 킬코이 제품 특수 처리
- **조건**: PACKER_CODE = '30228' AND STORE_CODE = '9231'
- **내용**: 킬코이 제품이면서 이마트 미트센터 납품 시 소비기한 별도 처리

### 7.2 센터 유형별 처리
- **TRD/WET/E/T 센터**: CENTERNAME에 해당 문자열 포함 시 별도 처리
- **롯데 (searchType "6")**: 별도 라벨 출력 로직

### 7.3 바코드 타입별 처리
- **M0**: 기본 바코드
- **M3/M4**: 특수 바코드 처리 로직 적용

---

## 8. 변경 이력

| 날짜 | 내용 | 작성자 |
|------|------|--------|
| 2025-12-05 | 최초 작성 | - |
