# VW_PDA_WID_LIST 컬럼 사용 현황

**작성일**: 2026-02-02
**VIEW 용도**: 이마트 출하 계근
**VIEW 위치**: HIGHLAND.VW_PDA_WID_LIST

---

## 1. 전체 요약

| 구분 | 컬럼 수 | 설명 |
|------|:-------:|------|
| VIEW 전체 컬럼 | **33개** | CREATE VIEW 정의 |
| 앱 직접 사용 | **30개** | 서버→앱 전송, 파싱 |
| 서버 WHERE 조건용 | **1개** | GR_WAREHOUSE_CODE |
| VIEW 내부 로직용 | **1개** | MAJOR_CATEGORY |
| **완전 미사용 (삭제 가능)** | **1개** | CONTAINER_TYPE |

---

## 2. 컬럼별 상세 현황

|  #  | 컬럼명                   | 의미            |   앱 파싱   | 용도                                  |   상태   | 대체               |                                   |
| :-: | --------------------- | ------------- | :------: | ----------------------------------- | :----: | ---------------- | --------------------------------- |
|  1  | GI_D_ID               | 출고상세ID        | temp[0]  | PK, 서버 전송                           |  ✅ 필수  | SM_출고상세.SEQ      |                                   |
|  2  | ITEM_CODE             | 상품코드          | temp[1]  | 품목코드, 서버 전송                         |  ✅ 필수  | SM_출고상세.출고품목코드   |                                   |
|  3  | ITEM_NAME             | 상품명           | temp[2]  | 품목명, 화면 표시                          |  ✅ 필수  | CO_품목코드.상품명      |                                   |
|  4  | EMARTITEM_CODE        | 이마트 상품코드      | temp[3]  | pBarcode 생성                         |  ✅ 필수  | SM_마트사발주이마트.상품코드 |                                   |
|  5  | EMARTITEM             | 이마트 상품명       | temp[4]  | 화면 표시                               |  ✅ 필수  | SM_마트사발주이마트.상품명  |                                   |
|  6  | GI_REQ_PKG            | 출하요청수량        | temp[5]  | 요청 박스 수량, 작업완료 판단                   |  ✅ 필수  | SM_출고상세.출고수량     |                                   |
|  7  | GI_REQ_QTY            | 출하요청중량        | temp[6]  | 요청 중량(kg), 화면 표시                    |  ✅ 필수  | SM_출고상세.출고중량     |                                   |
|  8  | GI_REQ_DATE           | 출하요청일         | temp[7]  | 요청일자, 조회 조건                         |  ✅ 필수  | SM_출고상세.출고일자     |                                   |
|  9  | BL_NO                 | BL번호 (선하증권번호) | temp[8]  | 스피너 선택, 중복 방지                       |  ✅ 필수  | PM_자재입출고명세서.BLNO |                                   |
| 10  | BRAND_CODE            | 브랜드코드         | temp[9]  | 서버 전송                               |  ✅ 필수  | CO_품목코드.브랜드      |                                   |
| 11  | CLIENT_CODE           | 출고업체코드        | temp[10] | DB 조회 조건                            |  ✅ 필수  | SM_출고머리.출고거래처    |                                   |
| 12  | CLIENTNAME            | 출고업체명         | temp[11] | 화면 표시                               |  ✅ 필수  | CO_거래처MASTER.상호  |                                   |
| 13  | **CENTERNAME**        | 센터명           | temp[12] | 센터유형 판별 (TRD/WET/E/T)               |  ✅ 필수  | SM_마트사발주이마트.센터코드 | 센터유형은 별도로 없음. 기존에 어디서 관리하는지 확인 필요 |
| 14  | ITEM_SPEC             | 스펙 (품목규격)     | temp[13] | 라벨 출력                               |  ✅ 필수  | CO_품목코드.규격       |                                   |
| 15  | CT_CODE               | 원산지코드         | temp[14] | DB 저장                               |  ✅ 필수  | CO_품목코드.원산지      |                                   |
| 16  | PACKER_CODE           | 패커코드          | temp[16] | 킬코이 판별 (30228)                      |  ✅ 필수  |                  | 추가 필요                             |
| 17  | IMPORT_ID_NO          | 수입식별번호        | temp[15] | 바코드 12자리                            |  ✅ 필수  | PM_자재입출고명세서.이력번호 |                                   |
| 18  | PACKER_PRODUCT_CODE   | 패커상품코드        | temp[17] | 서버 전송                               |  ✅ 필수  | CO_품목코드.ppCode   |                                   |
| 19  | BARCODE_TYPE          | 바코드타입         | temp[18] | 라벨 형식 분기 (M0~M9, E0~E3, L0)         |  ✅ 필수  |                  | ?                                 |
| 20  | ITEM_TYPE             | 아이템타입         | temp[19] | 계근 방식 분기 (W:원료육, S:세트, J:제품, B:비정량) |  ✅ 필수  |                  | ?                                 |
| 21  | PACKWEIGHT            | 포장중량          | temp[20] | ITEM_TYPE=J용 지정중량                   |  ✅ 필수  |                  | ?                                 |
| 22  | BARCODEGOODS          | 바코드상품코드       | temp[21] | 바코드 조회                              |  ✅ 필수  |                  | ?                                 |
| 23  | STORE_IN_DATE         | 납품일자          | temp[22] | DB 저장                               |  ✅ 필수  | SM_수주상세.납기일자     |                                   |
| 24  | **GR_WAREHOUSE_CODE** | 입고창고코드        |    -     | **서버 WHERE 조건 (창고 필터링)**            | ⚠️ 서버용 | SM_출고상세.창고코드     |                                   |
| 25  | EMARTLOGIS_CODE       | 물류코드          | temp[23] | pBarcode2 생성                        |  ✅ 필수  |                  | ?                                 |
| 26  | WH_AREA               | 창고구역          | temp[24] | 창고구역 표시                             |  ✅ 필수  |                  | ?                                 |
| 27  | USE_NAME              | 용도명           | temp[25] | 용도명 표시                              |  ✅ 필수  | CO_각종소분류코드       | 043                               |
| 28  | USE_CODE              | 용도코드          | temp[26] | 용도코드 저장                             |  ✅ 필수  | CO_품목코드.제품용도     |                                   |
| 29  | CT_NAME               | 원산지명          | temp[27] | 원산지명 표시                             |  ✅ 필수  | CO_각종소분류코드       | Q14                               |
| 30  | STORE_CODE            | 점포코드          | temp[28] | 미트센터 판별 (9231)                      |  ✅ 필수  | SM_마트사발주이마트.점포코드 |                                   |
| 31  | EMART_PLANT_CODE      | 이마트 가공장코드     | temp[29] | 미트센터 바코드 생성                         |  ✅ 필수  |                  | ?                                 |
| 32  | **MAJOR_CATEGORY**    | 대분류           |    -     | **VIEW 내부: BARCODE_TYPE='M9' 결정**   | ⚠️ 내부용 |                  | ?                                 |
| 33  | **CONTAINER_TYPE**    | 용기타입          |    -     | 미사용                                 | ❌ 삭제가능 |                  |                                   |

---

## 3. 컬럼 분류별 정리

### 3.1 앱 직접 사용 (30개)

서버에서 앱으로 전송되어 `ProgressDlgShipSearch.java`에서 파싱되는 컬럼

```
GI_D_ID, ITEM_CODE, ITEM_NAME, EMARTITEM_CODE, EMARTITEM,
GI_REQ_PKG, GI_REQ_QTY, GI_REQ_DATE, BL_NO, BRAND_CODE,
CLIENT_CODE, CLIENTNAME, CENTERNAME, ITEM_SPEC, CT_CODE,
PACKER_CODE, IMPORT_ID_NO, PACKER_PRODUCT_CODE, BARCODE_TYPE, ITEM_TYPE,
PACKWEIGHT, BARCODEGOODS, STORE_IN_DATE, EMARTLOGIS_CODE, WH_AREA,
USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE
```

### 3.2 서버 WHERE 조건용 (1개)

```
GR_WAREHOUSE_CODE
```

**사용 위치**: ProgressDlgShipSearch.java (Line 117~196)

**사용 방식**:
```java
if(Common.selectWarehouse.equals("삼일냉장")){
    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
}else if(Common.selectWarehouse.equals("SWC")){
    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
}else if(Common.selectWarehouse.equals("이천1센터")){
    data += " AND GR_WAREHOUSE_CODE = '4001'";
}else if(Common.selectWarehouse.equals("부산센터")){
    data += " AND GR_WAREHOUSE_CODE = '4004'";
}else if(Common.selectWarehouse.equals("탑로지스")){
    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
}
```

**창고코드 매핑**:

| 창고명 | GR_WAREHOUSE_CODE |
|--------|-------------------|
| 삼일냉장 | IN10273 |
| SWC | IN60464 |
| 이천1센터 | 4001 |
| 부산센터 | 4004 |
| 탑로지스 | IN63279 |

### 3.3 VIEW 내부 로직용 (1개)

```
MAJOR_CATEGORY
```

**사용 위치**: VW_PDA_WID_LIST (Line 78, 211)

**사용 방식**:
```sql
DECODE(
  CENTER_SCALE_USE_YN,          -- 센터 계근 사용 여부
  'Y',                          -- 사용하는 센터일 경우
    DECODE(
      BI.ITEM_TYPE,             -- B_ITEM 테이블의 아이템타입
      '10',                     -- 아이템타입이 '10'이면
        DECODE(
          BI.MAJOR_CATEGORY,    -- ★ MAJOR_CATEGORY 체크
          '10',                 -- MAJOR_CATEGORY가 '10'이면
          'M9',                 -- → BARCODE_TYPE을 'M9'로 변환
          EO.BARCODE_TYPE       -- 그 외는 원래 바코드타입 유지
        ),
      EO.BARCODE_TYPE           -- ITEM_TYPE이 '10'이 아니면 원래 값
    ),
  EO.BARCODE_TYPE               -- 센터 계근 미사용 시 원래 값
) AS BARCODE_TYPE
```

**조건 분기**:

| CENTER_SCALE_USE_YN | ITEM_TYPE | MAJOR_CATEGORY | 결과 BARCODE_TYPE |
|:-------------------:|:---------:|:--------------:|:-----------------:|
| Y | 10 | 10 | **M9** (변환) |
| Y | 10 | 그 외 | 원래 값 유지 |
| Y | 그 외 | - | 원래 값 유지 |
| N | - | - | 원래 값 유지 |

### 3.4 완전 미사용 - 삭제 가능 (1개)

```
CONTAINER_TYPE
```

- VIEW 내부 로직에서 미사용
- 서버 WHERE 조건에서 미사용
- 앱에서 파싱/사용 안함
- **삭제 가능**

---

## 4. VIEW ↔ 앱 파싱 순서 불일치

### 4.1 순서 차이

| VIEW 순서 | VIEW 컬럼명 | 앱 파싱 순서 | 앱 파싱 컬럼명 |
|:---------:|-------------|:------------:|----------------|
| 16 | PACKER_CODE | temp[16] | PACKER_CODE |
| 17 | IMPORT_ID_NO | temp[15] | IMPORT_ID_NO |

**주의**: VIEW 정의와 서버 전송 순서가 다름
- VIEW: PACKER_CODE (16번) → IMPORT_ID_NO (17번)
- 서버: IMPORT_ID_NO (temp[15]) → PACKER_CODE (temp[16])

### 4.2 미전송 컬럼

서버에서 앱으로 전송하지 않는 컬럼 (3개):
1. GR_WAREHOUSE_CODE (24번) - WHERE 조건용
2. MAJOR_CATEGORY (32번) - VIEW 내부 로직용
3. CONTAINER_TYPE (33번) - 미사용

---

## 5. 관련 파일

| 파일 | 용도 |
|------|------|
| VW_PDA_WID_LIST | VIEW SQL 정의 |
| ProgressDlgShipSearch.java | 서버 조회 및 응답 파싱 |
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | 컬럼명 상수 정의 |
| DBHandler.java | 로컬 DB 저장/조회 |
| BixolonShipmentActivity.java | 계근/라벨 출력 로직 |

---

## 6. 결론

| 구분 | 컬럼 수 | 조치 |
|------|:-------:|------|
| 유지 필요 | **32개** | 앱/서버/VIEW 내부에서 사용 |
| **삭제 가능** | **1개** | CONTAINER_TYPE |

---

**최종 수정일**: 2026-02-02
