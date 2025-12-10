# VW_PDA_WID_LIST 컬럼 사용 여부 분석

## VIEW 정보
- **VIEW 명**: VW_PDA_WID_LIST
- **용도**: 이마트 출하 계근 리스트
- **searchType**: 0

---

## 컬럼 분석 결과

| 컬럼명                                         | UI 노출 | VIEW 내부 | 안드로이드 소스 | DDL 사용 | 비즈니스 영향 | 판정                    |
| ------------------------------------------- | ----- | ------- | -------- | ------ | ------- | --------------------- |
| [GI_H_ID](#gi_h_id)                         | X     | **O**   | X        | X      | **있음**  | **사용**                |
| [CLIENT_CODE](#client_code)                 | X     | X       | X        | X      | 없음      | 미사용                   |
| [GI_REQ_DATE](#gi_req_date)                 | X     | O       | O        | X      | **있음**  | **사용**                |
| [GI_D_ID](#gi_d_id)                         | X     | O       | O        | O      | **있음**  | **사용**                |
| [EOI_ID](#eoi_id)                           | X     | **O**   | X        | X      | **있음**  | **사용**                |
| [ITEM_CODE](#item_code)                     | X     | O       | O        | O      | **있음**  | **사용**                |
| [GI_REQ_PKG](#gi_req_pkg)                   | O     | **O**   | O        | X      | **있음**  | **사용**                |
| [GI_REQ_QTY](#gi_req_qty)                   | O     | X       | O        | X      | **있음**  | **사용**                |
| [AMOUNT](#amount)                           | X     | X       | X        | X      | 없음      | 미사용                   |
| [GOODS_R_ID](#goods_r_id)                   | X     | **O**   | X        | X      | **있음**  | **사용**                |
| [GR_REF_NO](#gr_ref_no)                     | X     | X       | X        | X      | 없음      | 미사용                   |
| [BRAND_CODE](#brand_code)                   | X     | O       | O        | O      | **있음**  | **사용**                |
| PACKING_QTY                                 | -     | O       | -        | -      | -       | **VIEW SELECT 컬럼 아님** |
| [IMPORT_ID_NO](#import_id_no)               | X     | X       | O        | X      | **있음**  | **사용**                |
| [BL_NO](#bl_no)                             | X     | O       | O        | X      | **있음**  | **사용**                |
| [ITEM_SPEC](#item_spec)                     | X     | X       | O        | X      | **있음**  | **사용**                |
| [CT_CODE](#ct_code)                         | X     | O       | O        | X      | **있음**  | **사용**                |
| BL_D_ID                                     | -     | O       | -        | -      | -       | **VIEW SELECT 컬럼 아님** |
| BL_S_ID                                     | -     | O       | -        | -      | -       | **VIEW SELECT 컬럼 아님** |
| [GR_WAREHOUSE_CODE](#gr_warehouse_code)     | X     | X       | O        | X      | **있음**  | **사용**                |
| CONTRACT_TYPE                               | -     | O       | -        | -      | -       | **VIEW SELECT 컬럼 아님** |
| [PACKER_CODE](#packer_code)                 | X     | O       | O        | X      | **있음**  | **사용**                |
| [PACKER_PRODUCT_CODE](#packer_product_code) | O     | O       | O        | O      | **있음**  | **사용**                |
| OFFER_D_ID                                  | -     | O       | -        | -      | -       | **VIEW SELECT 컬럼 아님** |
| [ITEM_TYPE](#item_type)                     | X     | O       | O        | X      | **있음**  | **사용**                |
| [MAJOR_CATEGORY](#major_category)           | X     | O       | X        | X      | **있음**  | **사용**                |
| [CONTAINER_TYPE](#container_type)           | X     | X       | X        | X      | 없음      | 미사용                   |
| STORE_NAME                                  | -     | O       | -        | -      | -       | **VIEW SELECT 컬럼 아님** |
| [STORE_CODE](#store_code)                   | X     | O       | O        | X      | **있음**  | **사용**                |
| CENTER_CODE                                 | -     | O       | -        | -      | -       | **VIEW SELECT 컬럼 아님** |
| [EMARTITEM_CODE](#emartitem_code)           | X     | O       | O        | X      | **있음**  | **사용**                |
| [ITEM_NAME](#item_name)                     | O     | O       | O        | X      | **있음**  | **사용**                |
| [STORE_IN_DATE](#store_in_date)             | X     | X       | O        | X      | **있음**  | **사용**                |
| [BARCODE_TYPE](#barcode_type)               | X     | O       | O        | X      | **있음**  | **사용**                |
| [PACKWEIGHT](#packweight)                   | X     | X       | O        | X      | **있음**  | **사용**                |
| [EMARTLOGIS_CODE](#emartlogis_code)         | X     | O       | O        | X      | **있음**  | **사용**                |
| [USE_CODE](#use_code)                       | X     | X       | O        | X      | **있음**  | **사용**                |
| [CENTERNAME](#centername)                   | X     | O       | O        | X      | **있음**  | **사용**                |
| [WH_AREA](#wh_area)                         | X     | X       | O        | X      | **있음**  | **사용**                |
| [CT_NAME](#ct_name)                         | X     | O       | O        | X      | **있음**  | **사용**                |
| [USE_NAME](#use_name)                       | X     | X       | O        | X      | **있음**  | **사용**                |
| CENTER_SCALE_USE_YN                         | -     | O       | -        | -      | -       | **VIEW SELECT 컬럼 아님** |
| [BRANDNAME](#brandname)                     | X     | O       | X        | X      | 없음      | 미사용                   |
| [BARCODEGOODS](#barcodegoods)               | X     | O       | O        | X      | **있음**  | **사용**                |
| [EMART_PLANT_CODE](#emart_plant_code)       | X     | O       | O        | X      | **있음**  | **사용**                |


---

## 상세 분석

### GI_H_ID

#### 1. UI 노출: X
- Activity, Adapter에서 사용 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST:121 → `INNER JOIN W_GOODS_ID ID ON IH.GI_H_ID = ID.GI_H_ID`
- W_GOODS_IH(출고헤더)와 W_GOODS_ID(출고상세) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- Activity에서 분기, 계산, 서버 전송 사용 없음
- 로컬 DB 저장/조회 경유만 존재

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- VIEW에서 출고헤더-출고상세 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST (VIEW) | 121 | **JOIN 조건 - 제거 불가** |
| search_shipment.jsp | 43 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp | 97 | out.println()에서 제거 |
| (외 13개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 157 | temp[0] 파싱 제거, 인덱스 조정 |
| DBHandler.java | 47 | 로컬 DB 테이블 스키마에서 컬럼 제거 |
| DBHandler.java | 705 | INSERT 문에서 컬럼 제거 |
| DBHandler.java | 111, 232, 336 | SELECT 문에서 컬럼 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CLIENT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: X
- JSP에서 WHERE, JOIN, ORDER BY, GROUP BY 사용 없음
- SELECT/out.println() 경유만 존재

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 DB 조회 함수 파라미터로 전달
- 그러나 **해당 함수들에서 WHERE 조건에 사용하지 않음** (파라미터로만 받고 미사용)
  - `selectqueryGoodsWet()`: client_code 파라미터 받지만 WHERE에서 미사용 (DBHandler.java:1269)
  - `selectqueryListGoodsWetInfo()`: client_code 파라미터 받지만 WHERE에서 미사용 (DBHandler.java:1413)
- 분기, 계산, 서버 전송에 사용 없음

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 CLIENT_CODE 사용 없음
- 참고: PACKER_CLIENT_CODE는 별도 컬럼 (insert_goods_wet.jsp:56 등에서 사용)

#### 5. 비즈니스 영향: 없음
- 화면에 표시되지 않음
- 서버로 전송되지 않음
- 조건 분기/계산에 사용되지 않음
- DB 조회 조건에도 사용되지 않음 (파라미터로만 받고 실제 미사용)
- **제거해도 비즈니스 기능에 영향 없음**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | - | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | - | out.println()에서 제거 |
| ProgressDlgShipSearch.java | temp[16] | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 로컬 DB 스키마/INSERT/SELECT에서 제거 |
| DBHandler.java | 1269 | selectqueryGoodsWet() 파라미터 제거 |
| DBHandler.java | 1413 | selectqueryListGoodsWetInfo() 파라미터 제거 |
| ShipmentActivity.java | 2953, 3135, 3938 | 함수 호출 시 파라미터 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GI_REQ_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 직접 표시되지 않음

#### 2. VIEW 내부 사용: O
- JSP에서 **WHERE 조건으로 사용**
- ProgressDlgShipSearch.java:48 → 서버 조회 시 WHERE 조건
  ```java
  String data = " WHERE GI_REQ_DATE = '" + Common.selectDay + "'";
  ```

#### 3. 안드로이드 소스 사용: O
- **로컬 DB WHERE 조건으로 사용**
- DBHandler.java:107 → 로컬 DB 조회 시 날짜 필터링
  ```java
  qry_condition = qry_condition + " AND GI_REQ_DATE = " + Common.selectDay;
  ```

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **서버 조회 시 날짜별 데이터 필터링에 필수**
- **로컬 DB 조회 시 날짜별 데이터 필터링에 필수**
- **제거 시 날짜별 출하 대상 조회 기능 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp | 55 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp | 100 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 48 | **WHERE 조건 수정 필요** (핵심) |
| ProgressDlgShipSearch.java | 196 | temp[12] 파싱 제거, 인덱스 조정 |
| DBHandler.java | 47 | 로컬 DB 테이블 스키마에서 컬럼 제거 |
| DBHandler.java | 107 | **WHERE 조건 수정 필요** (핵심) |
| DBHandler.java | 123, 244, 348, 676 | SELECT 문에서 컬럼 제거 |
| DBHandler.java | 717 | INSERT 문에서 컬럼 제거 |
| Shipments_Info.java | 18, 183-188 | 필드, getter/setter 제거 |
| DBInfo.java | 38 | 상수 제거 |

---

### GI_D_ID

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 직접 표시되지 않음

#### 2. VIEW 내부 사용: O
- JSP에서 **WHERE 조건, ORDER BY로 사용**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ?`
- insert_goods_wet_homeplus.jsp:109 → `WHERE GI_D_ID = ?`
- insert_goods_wet_ono.jsp:80 → `WHERE GI_D_ID = ?`
- search_goods_wet.jsp:70 → `ORDER BY GI_D_ID ASC`

#### 3. 안드로이드 소스 사용: O
- **핵심 식별자로 광범위하게 사용**
- ShipmentActivity.java:511 → 데이터 매핑 비교 (`getGI_D_ID().equals()`)
- ShipmentActivity.java:877 → DB 조회 파라미터
- ShipmentActivity.java:1116 → 계근 데이터에 GI_D_ID 설정
- ShipmentActivity.java:3302-3304 → WHERE 조건 생성
- ShipmentActivity.java:3320, 3430 → **서버 전송 패킷에 포함**
- ShipmentActivity.java:3366, 3498 → 데이터 비교
- ShipmentActivity.java:3370, 3502 → **계근 완료 서버 전송**
- DBHandler.java:797 → 로컬 DB WHERE 조건

#### 4. DDL 사용: O
- **JSP INSERT 문에 사용**
- insert_goods_wet.jsp:51 → `INSERT INTO W_GOODS_WET(..., GI_D_ID, ...)`
- insert_goods_wet_homeplus.jsp:51 → 동일
- insert_goods_wet_ono.jsp:41 → 동일
- insert_goods_wet_new.jsp:55 → 동일

#### 5. 비즈니스 영향: **있음 (핵심)**
- **출고 상세 ID (PK) - 계근 데이터의 핵심 식별자**
- **서버 전송 시 필수 파라미터**
- **계근 완료 처리 시 필수 식별자**
- **로컬/서버 DB 조회 WHERE 조건**
- **제거 시 계근 기능 전체 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 42 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | - | out.println()에서 제거 |
| insert_goods_wet.jsp | 51, 105 | **INSERT, WHERE 조건 수정** (핵심) |
| insert_goods_wet_homeplus.jsp | 51, 109 | **INSERT, WHERE 조건 수정** (핵심) |
| insert_goods_wet_ono.jsp | 41, 80 | **INSERT, WHERE 조건 수정** (핵심) |
| insert_goods_wet_new.jsp | 55 | INSERT 문 수정 |
| search_goods_wet.jsp | 56, 70 | SELECT, ORDER BY 수정 |
| ProgressDlgShipSearch.java | temp[1] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 511, 873, 877, 1116, 2953, 3135, 3227, 3302-3304, 3320, 3360, 3366, 3370, 3396, 3430, 3493, 3498, 3502, 3533, 3938 | **핵심 로직 전면 수정 필요** |
| DBHandler.java | 36, 112, 167, 233, 279, 337, 385, 439, 454, 478, 493, 584, 593, 596, 606, 665, 706, 797, 1233, 1279 | 스키마/INSERT/SELECT/WHERE 전면 수정 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| Goodswets_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### BRAND_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 직접 표시되지 않음

#### 2. VIEW 내부 사용: O
- JSP에서 **WHERE 조건으로 사용**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet_homeplus.jsp:109 → 동일
- insert_goods_wet_ono.jsp:80 → 동일

#### 3. 안드로이드 소스 사용: O
- **서버 전송 시 필수 파라미터**
- ShipmentActivity.java:1128 → 계근 데이터에 BRAND_CODE 설정
- ShipmentActivity.java:3331, 3441 → **서버 전송 패킷에 포함**
- ShipmentActivity.java:3370, 3502 → **계근 완료 서버 전송**

#### 4. DDL 사용: O
- **JSP에서 WHERE 조건으로 UPDATE 대상 지정**
- insert_goods_wet.jsp:105, 114 → UPDATE WHERE 조건 및 파라미터
- insert_goods_wet_homeplus.jsp:109, 118 → 동일
- insert_goods_wet_ono.jsp:80, 89 → 동일

#### 5. 비즈니스 영향: **있음**
- **서버 전송 시 필수 파라미터**
- **계근 완료 처리 시 필수 식별자**
- **UPDATE WHERE 조건으로 사용**
- **제거 시 계근 완료/서버 전송 기능 불가**

#### 6. 수정 위치
| 파일                            | 라인                           | 수정 내용                  |
| ----------------------------- | ---------------------------- | ---------------------- |
| search_shipment.jsp 등         | -                            | SELECT 문에서 컬럼 제거       |
| search_shipment.jsp 등         | -                            | out.println()에서 제거     |
| insert_goods_wet.jsp          | 105, 114                     | **WHERE 조건 수정** (핵심)   |
| insert_goods_wet_homeplus.jsp | 109, 118                     | **WHERE 조건 수정** (핵심)   |
| insert_goods_wet_ono.jsp      | 80, 89                       | **WHERE 조건 수정** (핵심)   |
| ProgressDlgShipSearch.java    | temp[14]                     | 파싱 제거, 인덱스 조정          |
| ShipmentActivity.java         | 1128, 3331, 3370, 3441, 3502 | **서버 전송 로직 수정** (핵심)   |
| DBHandler.java                | -                            | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java           | 20, 199-204                  | 필드, getter/setter 제거   |
| Goodswets_Info.java           | 18, 133-138                  | 필드, getter/setter 제거   |
| Barcodes_Info.java            | 6, 40-45                     | 필드, getter/setter 제거   |
| DBInfo.java                   | 14                           | 상수 제거                  |

---

### GOODS_R_ID

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST:123 → `INNER JOIN W_GOODS_R WR ON ID.GOODS_R_ID = WR.GOODS_R_ID`
- W_GOODS_ID(출고상세)와 W_GOODS_R(입고) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- Activity에서 분기, 계산, 서버 전송에 사용 없음
- 로컬 DB 저장/조회 경유만 존재

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수** (출고상세 ↔ 입고 연결)
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST (VIEW) | 123 | **JOIN 조건 - 제거 불가** |
| search_shipment.jsp 등 | 53 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 100 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 194 (temp[10]) | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 45, 121, 176, 242, 288, 346, 394, 674, 715 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 16, 167-172 | 필드, getter/setter 제거 |
| DBInfo.java | 36 | 상수 제거 |

---

### AMOUNT

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: X
- JSP에서 WHERE, JOIN, 계산 등에 사용 없음
- SELECT/out.println() 경유만 존재

#### 3. 안드로이드 소스 사용: X
- Activity에서 분기, 계산, 서버 전송에 사용 없음
- 로컬 DB 저장/조회 경유만 존재

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: 없음
- 화면에 표시되지 않음
- 서버로 전송되지 않음
- 조건 분기/계산에 사용되지 않음
- **제거해도 비즈니스 기능에 영향 없음**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 52 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 99 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 193 (temp[9]) | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 44, 120, 175, 241, 287, 345, 393, 673, 714 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 15, 159-164 | 필드, getter/setter 제거 |
| DBInfo.java | 35 | 상수 제거 |

---

### GR_REF_NO

#### 1. UI 노출: X
- Activity, Adapter에서 사용 없음

#### 2. VIEW 내부 사용: X
- VIEW SELECT 컬럼으로 존재 (라인 15, 69, 209)
- WHERE/JOIN 조건에 사용 없음
- 단순 SELECT/출력 경유만

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity.java에서 사용 없음
- 파싱/로컬DB 저장/조회 경유만 존재
- 분기, 계산, 서버 전송에 사용 없음

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: 없음
- 화면에 표시되지 않음
- 서버로 전송되지 않음
- 조건 분기/계산에 사용되지 않음
- **제거해도 비즈니스 기능에 영향 없음**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 54 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 100 | out.println()에서 제거 |
| (외 9개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 195 (temp[11]) | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 46, 122, 177, 243, 289, 347, 395, 675, 716 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 17, 175-180 | 필드, getter/setter 제거 |
| DBInfo.java | 37 | 상수 제거 |

---

### PACKING_QTY

#### 1. VIEW SELECT 컬럼 여부: X
- VIEW SELECT 목록에 없음
- **VIEW WHERE 조건에만 사용** (`AND ID.PACKING_QTY = 0`, 라인 179, 331)
- 계근 미완료 항목만 조회하는 필터 조건

#### 2. 앱의 PACKING_QTY와의 관계
- VIEW의 PACKING_QTY: W_GOODS_ID 테이블 컬럼, WHERE 조건용
- 앱의 PACKING_QTY: Shipments_Info 필드, 로컬 DB(TB_GOODS_WET) COUNT로 계산
- **이름만 같고 서로 다른 것**

#### 3. 결론
- **VIEW SELECT 컬럼이 아니므로 분석 대상 아님**
- 앱으로 전달되지 않음

---

### IMPORT_ID_NO

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음

#### 2. VIEW 내부 사용: X
- VIEW SELECT 컬럼으로 존재 (라인 26, 95, 238)
- WHERE/JOIN 조건에 사용 없음
- BL_NO 계산에 사용: `DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO)` (라인 73, 213)

#### 3. 안드로이드 소스 사용: O
- **바코드 생성에 사용**
- ShipmentActivity.java:1877 → `pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO()`
- ShipmentActivity.java:1878, 1880, 1881, 1910, 1911, 1913, 1914, 1943, 1944, 1946, 1947, 1976, 1977 → 바코드 문자열 생성

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **바코드 생성에 필수**
- 수입식별번호/이력번호로 바코드 구성
- **제거 시 바코드 생성 기능 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 64 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 103 | out.println()에서 제거 |
| (외 9개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[21] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1874-1977 | **바코드 생성 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GI_REQ_QTY

#### 1. UI 노출: O
- **화면에 중량 진행률로 표시**
- ShipmentActivity.java:1208 → `setText(getGI_REQ_QTY() + " / " + getGI_QTY())`
- ShipmentActivity.java:1571 → 동일
- ShipmentActivity.java:3798, 4019 → 동일

#### 2. VIEW 내부 사용: X
- JSP에서 WHERE, JOIN, 계산 등에 사용 없음
- SELECT/out.println() 경유만 존재

#### 3. 안드로이드 소스 사용: O
- **총 계근요청중량 계산에 사용**
- ShipmentActivity.java:3031 → `centerTotalWeight += Double.parseDouble(getGI_REQ_QTY())`
- ShipmentActivity.java:3186 → 동일

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **중량 진행률 UI 표시**
- **총 계근요청중량 계산**
- **제거 시 중량 진행률 표시 기능 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 51 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 99 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 192 (temp[8]) | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1208, 1571, 3031, 3186, 3798, 4019 | **중량 표시/계산 로직 수정** |
| DBHandler.java | 43, 119, 174, 240, 286, 344, 392, 672, 713 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 14, 151-156 | 필드, getter/setter 제거 |
| DBInfo.java | 34 | 상수 제거 |

---

### GI_REQ_PKG

#### 1. UI 노출: O
- **화면에 진행률로 표시**
- ShipmentListAdapter.java:142 → `setText(arSrc.get(pos).getGI_REQ_PKG() + "/" + arSrc.get(pos).getPACKING_QTY())`
- ShipmentActivity.java:1207, 1570, 3797, 4018 → `setText("요청수량 / 완료수량")`

#### 2. VIEW 내부 사용: O
- **VIEW WHERE 조건으로 사용**
- VW_PDA_WID_LIST:180 → `AND ID.GI_REQ_PKG <> 0`
- 출하요청수량이 0이 아닌 항목만 조회 (요청이 있는 항목만 표시)

#### 3. 안드로이드 소스 사용: O
- **계근 완료 여부 판단에 핵심적으로 사용**
- ShipmentActivity.java:772 → 중복 스캔 방지 시 완료 체크
- ShipmentActivity.java:862, 1104 → **완료 판단 분기** `getGI_REQ_PKG().equals(String.valueOf(getPACKING_QTY()))`
- ShipmentActivity.java:1244 → 수량 비교 분기
- ShipmentActivity.java:1478, 3043 → 미완료 아이템 검색
- ShipmentActivity.java:3030, 3185 → **총 계근요청수량 계산**
- ShipmentActivity.java:3247 → 완료 판단 분기
- ShipmentActivity.java:3369, 3501 → **서버 전송 완료 판단** (요청수량 == 전송수량)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **계근 완료 여부 판단의 핵심 기준**
- **진행률 UI 표시**
- **총 계근요청수량 계산**
- **서버 전송 완료 판단**
- **제거 시 계근 완료 판단/진행률 표시 기능 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 50 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 99 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 164 (temp[7]) | 파싱 제거, 인덱스 조정 |
| ShipmentListAdapter.java | 142 | **UI 표시 로직 수정** (핵심) |
| ShipmentActivity.java | 772, 862, 1104, 1207, 1244, 1478, 1570, 3030, 3043, 3185, 3247, 3369, 3501, 3797, 4018 | **완료 판단/진행률 로직 전면 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 13, 143-148 | 필드, getter/setter 제거 |
| DBInfo.java | 33 | 상수 제거 |

---

### ITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 직접 표시되지 않음

#### 2. VIEW 내부 사용: O
- JSP에서 **WHERE 조건, JOIN 키로 사용**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet_homeplus.jsp:109 → 동일
- insert_goods_wet_ono.jsp:80 → 동일
- search_barcode_info.jsp:67 → `INNER JOIN B_ITEM BI ON SBI.ITEMCODE = BI.ITEM_CODE`

#### 3. 안드로이드 소스 사용: O
- **서버 전송 시 필수 파라미터**
- ShipmentActivity.java:1127 → 계근 데이터에 ITEM_CODE 설정
- ShipmentActivity.java:3330, 3440 → **서버 전송 패킷에 포함**
- ShipmentActivity.java:3370, 3502 → **계근 완료 서버 전송**

#### 4. DDL 사용: O
- **JSP에서 WHERE 조건으로 UPDATE 대상 지정**
- insert_goods_wet.jsp:105, 113 → UPDATE WHERE 조건 및 파라미터
- insert_goods_wet_homeplus.jsp:109, 117 → 동일
- insert_goods_wet_ono.jsp:80, 88 → 동일

#### 5. 비즈니스 영향: **있음**
- **서버 전송 시 필수 파라미터**
- **계근 완료 처리 시 필수 식별자**
- **UPDATE WHERE 조건으로 사용**
- **제거 시 계근 완료/서버 전송 기능 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | - | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | - | out.println()에서 제거 |
| insert_goods_wet.jsp | 105, 113 | **WHERE 조건 수정** (핵심) |
| insert_goods_wet_homeplus.jsp | 109, 117 | **WHERE 조건 수정** (핵심) |
| insert_goods_wet_ono.jsp | 80, 88 | **WHERE 조건 수정** (핵심) |
| search_barcode_info.jsp | 67 | JOIN 조건 수정 |
| ProgressDlgShipSearch.java | temp[3] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1127, 3330, 3370, 3440, 3502 | **서버 전송 로직 수정** (핵심) |
| DBHandler.java | 38, 114, 169, 235, 281, 339, 387, 667, 708, 1127, 1572, 1643, 1716 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| Goodswets_Info.java | - | 필드, getter/setter 제거 |
| Barcodes_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 29 | 상수 제거 |

---

### EOI_ID

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **VIEW WHERE 조건 + JOIN 조건으로 사용**
- VW_PDA_WID_LIST:177 → `ON ID.EOI_ID = EO.EOI_ID` (JOIN)
- VW_PDA_WID_LIST:183 → `AND EO.EOI_ID IS NOT NULL` (WHERE)
- JSP에서 **ORDER BY로 사용**
- search_shipment.jsp:85 → `ORDER BY EOI_ID ASC`

#### 3. 안드로이드 소스 사용: X
- Activity에서 분기, 계산, 서버 전송에 사용 없음
- 로컬 DB ORDER BY로만 사용 (DBHandler.java:443, 482)
- 단순 경유만 존재 (파싱/저장/조회)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수** (W_GOODS_ID ↔ W_EMART_ORDER_ITEM 연결)
- **VIEW WHERE 조건으로 필수** (이마트 발주 존재 여부 확인)
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST (VIEW) | 177, 183 | **JOIN/WHERE 조건 - 제거 불가** |
| search_shipment.jsp | 45, 85 | SELECT, ORDER BY에서 제거 |
| search_production.jsp | 44, 75 | SELECT, ORDER BY에서 제거 |
| search_production_4label.jsp | 39, 70 | SELECT, ORDER BY에서 제거 |
| search_production_nonfixed.jsp | 45, 82 | SELECT, ORDER BY에서 제거 |
| search_barcode_info_temp_diff_prd.jsp | 43, 76 | SELECT, ORDER BY에서 제거 |
| search_homeplus_nonfixed.jsp | 45 | SELECT에서 제거 |
| search_shipment_homeplus.jsp | 45, 77 | SELECT, ORDER BY에서 제거 |
| (외 JSP 파일들) | - | out.println()에서 제거 |
| ProgressDlgShipSearch.java | temp[2] | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 37, 113, 168, 234, 280, 338, 386, 440, 443, 455, 479, 482, 494, 666, 707 | 스키마/INSERT/SELECT/ORDER BY에서 제거 |
| Shipments_Info.java | 8, 103-108 | 필드, getter/setter 제거 |
| DBInfo.java | 28 | 상수 제거 |

---

### BL_NO

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음 (스피너 선택 목록에만 표시)

#### 2. VIEW 내부 사용: O
- **VIEW DECODE 계산식에 사용**
- VW_PDA_WID_LIST:73-74 → `DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) BL_NO`
- VW_PDA_WID_LIST:213-214 → 동일 (UNION ALL 쿼리)
- IMPORT_ID_NO가 NULL이면 BL_NO 사용, 아니면 IMPORT_ID_NO 사용

#### 3. 안드로이드 소스 사용: O
- **작업 대상 검색 분기에 핵심적으로 사용**
- ShipmentActivity.java:772 → `temp_bl_no.equals(arSM.get(i).getBL_NO())` (BL번호 일치 상품 검색)
- ShipmentActivity.java:1318 → `BL_NO.equals(arSM.get(current_work_position).getBL_NO())` (BL 검증)
- ShipmentActivity.java:1544, 1564, 1569 → BL_NO 리스트 관리 및 작업 BL 설정
- ShipmentActivity.java:3193, 3199, 3215 → BL_NO 스피너 선택 관리
- **DBHandler.java:103 → WHERE 조건**: `AND BL_NO = '" + condition + "'`
- **DBHandler.java:371 → WHERE 조건**: `AND BL_NO = '" + bl_no + "'`

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **작업 대상 상품 검색의 핵심 기준**
- **스피너 BL 선택 시 해당 BL 상품으로 이동**
- **DB WHERE 조건으로 BL별 조회에 필수**
- **제거 시 BL별 상품 검색/선택 기능 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 56 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 100 | out.println()에서 제거 |
| (외 9개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[13] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 772, 1318, 1544, 1564, 1569, 3193, 3199, 3215 | **BL 검색/선택 로직 수정** (핵심) |
| DBHandler.java | 48, 103, 124, 179, 245, 291, 349, 371, 397, 677 | 스키마/INSERT/SELECT/WHERE에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 39 | 상수 제거 |

---

### ITEM_SPEC

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: X
- VW_PDA_WID_LIST:91, 232 → `WR.ITEM_SPEC` (SELECT만)
- WHERE, JOIN 조건에 사용 없음

#### 3. 안드로이드 소스 사용: O
- **라벨 인쇄에 사용**
- ShipmentActivity.java:1639 → `si.EMARTITEM + " / " + si.ITEM_SPEC` (상품명/냉장냉동 출력)
- ShipmentActivity.java:1641 → 동일
- ShipmentActivity.java:1643 → 로그 출력

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 인쇄 시 상품 스펙(냉장/냉동) 표시**
- **제거 시 라벨에 스펙 정보 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 62 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 102 | out.println()에서 제거 |
| (외 9개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 203 (temp[19]) | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1639, 1641, 1643 | **라벨 인쇄 로직 수정** (핵심) |
| DBHandler.java | 54, 130, 185, 251, 297, 355, 403, 683, 724 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 25, 239-244 | 필드, getter/setter 제거 |
| DBInfo.java | 45 | 상수 제거 |

---

### CT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **서브쿼리 WHERE 조건에 사용**
- VW_PDA_WID_LIST:93 → `WHERE BC.CODE = WR.CT_CODE AND BC.MASTER_CODE = 'HOMEPLUS_ORIGIN_CODE'`
- VW_PDA_WID_LIST:236 → 동일 (UNION ALL 쿼리)
- CT_NAME(원산지명) 조회를 위한 서브쿼리 조건

#### 3. 안드로이드 소스 사용: O
- **라벨 인쇄에 사용**
- ShipmentActivity.java:2609 → `si.getCT_CODE()` (원산지 코드 출력)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 인쇄 시 원산지 코드 표시**
- **VIEW 서브쿼리에서 CT_NAME 조회 조건**
- **제거 시 라벨에 원산지 코드 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | - | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | - | out.println()에서 제거 |
| (외 JSP 파일들) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 204 (temp[20]) | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 2609 | **라벨 인쇄 로직 수정** (핵심) |
| DBHandler.java | 55, 131, 186, 252, 298, 356, 404, 684, 725 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 26, 247-252 | 필드, getter/setter 제거 |
| DBInfo.java | 46 | 상수 제거 |

---

### GR_WAREHOUSE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: X
- VW_PDA_WID_LIST:109, 261 → `WR.GR_WAREHOUSE_CODE` (SELECT만)
- WHERE, JOIN 조건에 사용 없음

#### 3. 안드로이드 소스 사용: O
- **서버 요청 WHERE 조건에 핵심적으로 사용**
- ProgressDlgShipSearch.java:52 → `AND GR_WAREHOUSE_CODE = 'IN10273'` (삼일냉장)
- ProgressDlgShipSearch.java:54 → `AND GR_WAREHOUSE_CODE = 'IN60464'` (SWC)
- ProgressDlgShipSearch.java:56 → `AND GR_WAREHOUSE_CODE = '4001'` (이천1센터)
- ProgressDlgShipSearch.java:58 → `AND GR_WAREHOUSE_CODE = '4004'` (부산센터)
- ProgressDlgShipSearch.java:60 → `AND GR_WAREHOUSE_CODE = 'IN63279'` (탑로지스)
- 동일 패턴이 searchType별로 반복 (라인 72-120)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **창고별 출하 리스트 필터링의 핵심 조건**
- **제거 시 창고별 조회 기능 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | - | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | - | out.println()에서 제거 |
| ProgressDlgShipSearch.java | 52-120 | **창고별 WHERE 조건 수정** (핵심) |

---

### PACKER_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **JOIN 조건에 사용**
- VW_PDA_WID_LIST:128-129 → `ON BD.PACKER_CODE = BSI.PACKER_CODE`
- VW_PDA_WID_LIST:279-280 → `ON OD.PACKER_CODE = BSI.PACKER_CODE`
- B_SUPPLIER_ITEM 테이블과 연결

#### 3. 안드로이드 소스 사용: O
- **분기 조건에 핵심적으로 사용**
- ShipmentActivity.java:347 → `getPACKER_CODE().equals("30228")` (킬코이 제품 판단)
- ShipmentActivity.java:793 → 동일 (킬코이 + 미트센터 납품 분기)
- ShipmentActivity.java:1699 → 동일 (소비기한 변조 출력 분기)
- ProductionActivity.java:441 → WHERE 조건 `AND a.PACKER_CODE = '...'`

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **패커별 비즈니스 로직 분기의 핵심**
- **킬코이(30228) 제품 특수 처리에 필수**
- **제거 시 패커별 분기 처리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 65 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 103 | out.println()에서 제거 |
| (외 9개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[22] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 347, 793, 1699 | **패커별 분기 로직 수정** (핵심) |
| ProductionActivity.java | 441 | WHERE 조건 수정 |
| DBHandler.java | 57, 133, 188, 254, 300, 358, 406, 686, 727 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 28, 263-267 | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### PACKER_PRODUCT_CODE

#### 1. UI 노출: O
- **화면에 패커상품코드 표시**
- ShipmentActivity.java:1356, 1375, 1436 → `edit_product_code.setText(bi.getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:1574, 3172 → 동일
- ShipmentActivity.java:3796 → `detail_edit_ppcode.setText(si.getPACKER_PRODUCT_CODE())`
- DetailAdapter.java:180 → `holder.ppcode.setText(arSrc.get(pos).getPACKER_PRODUCT_CODE())`

#### 2. VIEW 내부 사용: O
- **JOIN 조건에 사용**
- VW_PDA_WID_LIST:129 → `AND BD.PACKER_PRODUCT_CODE = BSI.PACKER_PRODUCT_CODE`
- VW_PDA_WID_LIST:280 → `AND OD.PACKER_PRODUCT_CODE = BSI.PACKER_PRODUCT_CODE`
- B_SUPPLIER_ITEM, S_BARCODE_INFO 테이블과 연결

#### 3. 안드로이드 소스 사용: O
- **서버 전송에 사용**
- ShipmentActivity.java:3323, 3433 → `packet += list_send_info.get(i).getPACKER_PRODUCT_CODE()`
- **DB 조회 조건에 사용**
- ShipmentActivity.java:877, 3227, 3396, 3533 → `updatequeryShipment()` 파라미터
- ShipmentActivity.java:2953, 3135, 3938 → `selectqueryGoodsWet()`, `selectqueryListGoodsWetInfo()` 파라미터
- ProductionActivity.java:441 → WHERE 조건 `AND a.PACKER_PRODUCT_CODE = '...'`
- **계근 정보 객체에 저장**
- ShipmentActivity.java:1119 → `gi.setPACKER_PRODUCT_CODE()`

#### 4. DDL 사용: O
- **JSP INSERT 문에 사용**
- insert_goods_wet.jsp:54 → INSERT 컬럼
- insert_goods_wet_homeplus.jsp:54 → 동일
- insert_goods_wet_ono.jsp:44 → 동일
- insert_goods_wet_new.jsp:58 → 동일

#### 5. 비즈니스 영향: **있음 (핵심)**
- **패커 상품코드 식별자로 핵심적으로 사용**
- **UI 표시, 서버 전송, DB 조회, DDL 모두 사용**
- **제거 시 패커상품 식별 및 전송 기능 불가**

#### 6. 수정 위치
| 파일                         | 라인          | 수정 내용                       |
| -------------------------- | ----------- | --------------------------- |
| search_shipment.jsp 등      | -           | SELECT 문에서 컬럼 제거            |
| search_shipment.jsp 등      | -           | out.println()에서 제거          |
| insert_goods_wet.jsp 등     | 54          | **INSERT 컬럼 제거** (핵심)       |
| ProgressDlgShipSearch.java | temp[24]    | 파싱 제거, 인덱스 조정               |
| ShipmentActivity.java      | 다수          | **UI/서버전송/DB조회 로직 수정** (핵심) |
| ProductionActivity.java    | 441         | WHERE 조건 수정                 |
| DetailAdapter.java         | 180         | UI 표시 제거                    |
| DBHandler.java             | -           | 스키마/INSERT/SELECT에서 제거      |
| Shipments_Info.java        | 30, 279-284 | 필드, getter/setter 제거        |
| DBInfo.java                | 16          | 상수 제거                       |

---

### ITEM_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **서브쿼리 WHERE 조건에 사용**
- VW_PDA_WID_LIST:157 → `AND EB.ITEM_TYPE = 'W'` (원료육 타입만 조회)
- VW_PDA_WID_LIST:309 → 동일 (UNION ALL 쿼리)

#### 3. 안드로이드 소스 사용: O
- **상품 타입별 분기 로직의 핵심**
- ShipmentActivity.java:917 → `getITEM_TYPE().equals("W")` 또는 `"HW"` (원료육 - 바코드 계근)
- ShipmentActivity.java:970 → `getITEM_TYPE().equals("S")` (정량)
- ShipmentActivity.java:1024-1025 → `getITEM_TYPE().equals("J")` (제품 - 지정 중량 입력)
- ShipmentActivity.java:1035 → `getITEM_TYPE().equals("B")` (비정량)
- ShipmentActivity.java:1829, 2676 → 바코드 생성 분기
- ShipmentActivity.java:2589 → 라벨 인쇄 분기

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **상품 타입별 계근 로직 분기의 핵심**
- W: 원료육 바코드 계근
- J: 제품 지정 중량 입력
- B: 비정량
- S: 정량
- **제거 시 상품 타입별 분기 처리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 69 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 105 | out.println()에서 제거 |
| (외 JSP 파일들) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[26] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 917, 970, 1024, 1035, 1829, 2589, 2676 | **상품 타입별 분기 로직 수정** (핵심) |
| DBHandler.java | 61 등 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### MAJOR_CATEGORY

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **BARCODE_TYPE DECODE 계산에 사용**
- VW_PDA_WID_LIST:98 → `DECODE(BI.MAJOR_CATEGORY, '10', 'M9', EO.BARCODE_TYPE)`
- MAJOR_CATEGORY='10'이면 BARCODE_TYPE을 'M9'로 변환
- VW_PDA_WID_LIST:117 → `BI.MAJOR_CATEGORY AS MAJOR_CATEGORY` (SELECT)

#### 3. 안드로이드 소스 사용: X
- Activity에서 분기, 계산, 서버 전송에 사용 없음
- 로컬 DB 저장/조회 경유만 존재

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW DECODE에서 BARCODE_TYPE 결정에 사용**
- MAJOR_CATEGORY='10'일 때 'M9' 바코드 타입 적용
- **VIEW 구조상 필수**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | - | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | - | out.println()에서 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### CONTAINER_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: X
- VW_PDA_WID_LIST:118 → `BI.CONTAINER_TYPE AS CONTAINER_TYPE` (SELECT만)
- WHERE, JOIN, DECODE 조건에 사용 없음

#### 3. 안드로이드 소스 사용: X
- Activity에서 분기, 계산, 서버 전송에 사용 없음
- Shipments_Info에 필드 없음 (파싱/저장 안함)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력만 (search_shipment.jsp:82, 107)

#### 5. 비즈니스 영향: 없음
- 화면에 표시되지 않음
- 서버로 전송되지 않음
- 조건 분기/계산에 사용되지 않음
- **제거해도 비즈니스 기능에 영향 없음**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp | 82 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp | 107 | out.println()에서 제거 |
| ProgressDlgShipSearch.java | - | 파싱 인덱스 조정 (해당 컬럼 파싱 안함) |

---

### STORE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **DECODE 조건에 사용**
- VW_PDA_WID_LIST:116 → `DECODE(EO.STORECODE, '9820', BSI.EMART_PLANT_CODE, NULL)` (미트센터 분기)
- VW_PDA_WID_LIST:115 → `EO.STORECODE AS STORE_CODE` (SELECT)

#### 3. 안드로이드 소스 사용: O
- **미트센터 분기 조건에 핵심적으로 사용**
- ShipmentActivity.java → `getSTORE_CODE().equals("9820")` (미트센터 납품 판단)
- **바코드 생성 분기에 사용**
- 미트센터(9820) 납품 시 별도 바코드 로직 적용

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **미트센터(9820) 납품 여부 판단의 핵심**
- **바코드 생성 로직 분기에 필수**
- **제거 시 미트센터 납품 분기 처리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | - | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | - | out.println()에서 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | - | **미트센터 분기 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### EMARTITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **JOIN 조건에 사용**
- VW_PDA_WID_LIST:58 → `EO.ITEM_CODE AS EMARTITEM_CODE` (SELECT)
- 바코드 조회 서브쿼리에서 사용

#### 3. 안드로이드 소스 사용: O
- **바코드 생성의 핵심**
- ShipmentActivity.java:1877 → `si.getEMARTITEM_CODE().substring(0, 6)` (바코드 앞 6자리)
- ShipmentActivity.java → 바코드 문자열 생성에 필수

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성의 핵심 요소**
- 이마트 상품코드 앞 6자리가 바코드에 포함
- **제거 시 바코드 생성 기능 불가**

#### 6. 수정 위치
| 파일                         | 라인     | 수정 내용                  |
| -------------------------- | ------ | ---------------------- |
| search_shipment.jsp 등      | -      | SELECT 문에서 컬럼 제거       |
| search_shipment.jsp 등      | -      | out.println()에서 제거     |
| ProgressDlgShipSearch.java | -      | 파싱 제거, 인덱스 조정          |
| ShipmentActivity.java      | 1877 등 | **바코드 생성 로직 수정** (핵심)  |
| DBHandler.java             | -      | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java        | -      | 필드, getter/setter 제거   |

---

### ITEM_NAME

#### 1. UI 노출: O
- **화면에 상품명 표시**
- ShipmentListAdapter → 리스트 아이템에 상품명 표시
- ShipmentActivity → 상세 화면에 상품명 표시

#### 2. VIEW 내부 사용: O
- **DECODE 계산식으로 생성**
- VW_PDA_WID_LIST:53-56 → `DECODE(EO.ITEM_NAME, NULL, DE_ITEM(ID.ITEM_CODE), EO.ITEM_NAME) AS ITEM_NAME`
- EO.ITEM_NAME이 NULL이면 DE_ITEM 함수로 조회

#### 3. 안드로이드 소스 사용: O
- **UI 표시에 사용**
- ShipmentActivity → 상품명 setText()
- ShipmentListAdapter → 리스트 아이템 표시

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **상품명 UI 표시에 필수**
- **제거 시 상품 식별 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | - | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | - | out.println()에서 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | - | **UI 표시 로직 수정** (핵심) |
| ShipmentListAdapter.java | - | UI 표시 제거 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### STORE_IN_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: X
- VW_PDA_WID_LIST:108 → `STORE_IN_DATE` (SELECT만)
- WHERE, JOIN 조건에 사용 없음

#### 3. 안드로이드 소스 사용: O
- **라벨 인쇄에 사용**
- ShipmentActivity.java → 납품일자 라벨 출력
- 라벨에 STORE_IN_DATE 출력

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 인쇄 시 납품일자 표시**
- **제거 시 라벨에 납품일자 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | - | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | - | out.println()에서 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | - | **라벨 인쇄 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### BARCODE_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **DECODE 계산식으로 BARCODE_TYPE 값 생성**
- VW_PDA_WID_LIST:98 → `DECODE(CENTER_SCALE_USE_YN, 'Y', DECODE(BI.ITEM_TYPE, '10', DECODE(BI.MAJOR_CATEGORY, '10', 'M9', EO.BARCODE_TYPE), EO.BARCODE_TYPE), EO.BARCODE_TYPE)`
- CENTER_SCALE_USE_YN='Y'이고 ITEM_TYPE='10'이고 MAJOR_CATEGORY='10'이면 'M9'로 변환
- 그 외에는 EO.BARCODE_TYPE 원본 사용

#### 3. 안드로이드 소스 사용: O
- **바코드 타입별 분기의 핵심**
- ShipmentActivity.java:833 → `getBARCODE_TYPE().equals("M3") || getBARCODE_TYPE().equals("M4")` (상품 타입 분기)
- ShipmentActivity.java:1865 → `switch (si.getBARCODE_TYPE())` (바코드 생성 분기)
  - case "M0", "M1", "M3", "M4", "M8", "M9": 미트 바코드 타입별 처리
  - case "E0", "E1", "E2", "E3": 이마트 바코드 타입별 처리
- ShipmentActivity.java:2195-2240 → 바코드 타입별 분기 (M0/E0, M1/E1, M3/E3 등)
- ShipmentActivity.java:2601, 2631, 2665 → 라벨 인쇄 분기

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력으로만 사용

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성 로직의 핵심 분기 조건**
- 바코드 타입별로 완전히 다른 바코드 포맷 사용
  - M0/E0: 기본 미트/이마트 바코드
  - M1/E1, M3/E3: 다른 포맷 바코드
  - M9: 센터 계근용 특수 바코드
- **제거 시 바코드 생성 기능 전체 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 66 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 95 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[25] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 833, 1865, 2195-2240, 2601, 2631, 2665 | **바코드 타입별 분기 로직 수정** (핵심) |
| DBHandler.java | 60, 136, 191, 257, 303, 361, 409, 689, 730 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 31, 287-292 | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### PACKWEIGHT

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 직접 표시되지 않음

#### 2. VIEW 내부 사용: X
- VW_PDA_WID_LIST:100 → `EO.PACKWEIGHT` (SELECT만)
- WHERE, JOIN, DECODE 조건에 사용 없음

#### 3. 안드로이드 소스 사용: O
- **제품(J) 타입 계근 시 기본 중량으로 사용**
- ShipmentActivity.java:1026 → `item_weight = arSM.get(current_work_position).getPACKWEIGHT()` (제품 타입 지정 중량)
- **바코드 생성 시 중량 문자열로 사용**
- ShipmentActivity.java:1839 → `print_weight_str = si.getPACKWEIGHT()` (바코드 중량)
- ShipmentActivity.java:2685 → 동일 (바코드 중량)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력만

#### 5. 비즈니스 영향: **있음**
- **제품(J) 타입 계근 시 기본 중량 설정**
- **바코드 생성 시 중량 값으로 사용**
- **제거 시 제품 타입 계근/바코드 중량 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 70 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 105 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[27] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1026, 1839, 2685 | **제품 계근/바코드 중량 로직 수정** (핵심) |
| DBHandler.java | 62, 138, 193, 259, 305, 363, 411, 691, 732 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 33, 303-308 | 필드, getter/setter 제거 |
| DBInfo.java | 53 | 상수 제거 |

---

### EMARTLOGIS_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 직접 표시되지 않음

#### 2. VIEW 내부 사용: O
- **DECODE 계산식으로 NULL 처리**
- VW_PDA_WID_LIST:110 → `DECODE(EO.EMARTLOGIS_CODE, NULL, '0000000', EO.EMARTLOGIS_CODE) AS EMARTLOGIS_CODE`
- NULL이면 '0000000' 기본값 적용

#### 3. 안드로이드 소스 사용: O
- **바코드 생성의 핵심 (pBarcode2)**
- ShipmentActivity.java:1880-2050 → `si.getEMARTLOGIS_CODE().substring(0, 6)` (바코드 앞 6자리)
- 바코드 타입별로 EMARTLOGIS_CODE 앞 6자리 + 중량 + 패커코드 + 이력번호 조합
- **미트센터 분기 조건에 사용**
- ShipmentActivity.java:2349 → `si.getEMARTLOGIS_CODE().equals("0000000")` (물류코드 없음 판단)
- ShipmentActivity.java:2429 → `!si.getEMARTLOGIS_CODE().equals("0000000")` (물류코드 있음 판단)
- ShipmentActivity.java:2379, 2456 → 미트센터 바코드 생성
- **라벨 인쇄에 사용**
- ShipmentActivity.java:2551 → `pointCode = si.EMARTLOGIS_CODE.toString()` (지점코드)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력만

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성의 핵심 구성요소**
- 이마트 물류코드 앞 6자리가 pBarcode2에 포함
- **미트센터 납품 분기 조건**
- 물류코드 유무에 따라 미트센터 바코드 생성 로직 분기
- **제거 시 바코드 생성/미트센터 분기 기능 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 73 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 106 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[30] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1880-2050, 2349, 2379, 2429, 2456, 2551 | **바코드 생성/미트센터 분기 로직 수정** (핵심) |
| DBHandler.java | 366, 414, 694, 735 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### USE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 직접 표시되지 않음

#### 2. VIEW 내부 사용: X
- VW_PDA_WID_LIST:39 → `"USE_CODE"` (SELECT만)
- WHERE, JOIN, DECODE 조건에 사용 없음

#### 3. 안드로이드 소스 사용: O
- **바코드 생성에 사용**
- ShipmentActivity.java:2027 → `pBarcode2 = si.getEMARTITEM_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + si.getUSE_CODE()`
- ShipmentActivity.java:2028 → `pBarcodeStr2` 동일 패턴
- 특정 바코드 타입에서 용도코드가 바코드 구성에 포함

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력만

#### 5. 비즈니스 영향: **있음**
- **특정 바코드 타입에서 바코드 구성요소로 사용**
- 이마트상품코드 + 이력번호 + USE_CODE 조합으로 바코드 생성
- **제거 시 해당 바코드 타입 생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 77 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 106 | out.println()에서 제거 |
| (외 JSP 파일들) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 2027, 2028 | **바코드 생성 로직 수정** (핵심) |
| DBHandler.java | 70, 145, 201, 698, 739 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 46, 376-381 | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CENTERNAME

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 직접 사용 없음
- 스피너 선택 목록으로만 표시

#### 2. VIEW 내부 사용: O
- **DECODE 조건에 사용**
- VW_PDA_WID_LIST:82 → `DECODE(SUBSTR(EO.CENTERNAME, 1, 2), 'CJ', ...)` (CLIENTNAME 계산에 사용)
- 센터명 앞 2자리로 CJ 여부 판단

#### 3. 안드로이드 소스 사용: O
- **센터 유형 분기의 핵심**
- ShipmentActivity.java:367 → `getCENTERNAME().contains("TRD") || getCENTERNAME().contains("WET") || getCENTERNAME().contains("E/T")` (트레이더스/WET/E/T 센터 분기)
- ShipmentActivity.java:839 → 동일 분기 조건
- ShipmentActivity.java:1745 → 트레이더스 납품 분기
- **라벨 인쇄에 사용**
- ShipmentActivity.java:2088, 2093 → `WoosimCmd.getTTFcode(si.CENTERNAME)` (센터명 라벨 출력)
- **로컬 DB WHERE 조건**
- DBHandler.java:152, 370 → `WHERE CENTERNAME = '...'` (센터별 데이터 조회)
- DBHandler.java:629-642 → 센터명 리스트 조회 (스피너용)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **센터 유형별 비즈니스 분기의 핵심**
  - TRD/WET/E/T: 트레이더스 납품 분기
  - CJ: 출고업체명 DECODE 분기
- **라벨 인쇄 시 센터명 출력**
- **센터별 데이터 필터링 (스피너)**
- **제거 시 센터 유형 분기/라벨 인쇄 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 61 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 102 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[18] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 367, 839, 1745, 2086-2093 | **센터 분기/라벨 인쇄 로직 수정** (핵심) |
| DBHandler.java | 53, 129, 152, 184, 250, 296, 354, 370, 402, 620-642, 682, 723 | 스키마/INSERT/SELECT/WHERE에서 제거 |
| Shipments_Info.java | 24, 231-236 | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### WH_AREA

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 직접 표시되지 않음

#### 2. VIEW 내부 사용: X
- VW_PDA_WID_LIST:112 → `WH_AREA` (SELECT만)
- WHERE, JOIN, DECODE 조건에 사용 없음

#### 3. 안드로이드 소스 사용: O
- **라벨 인쇄에 사용 (창고 구역)**
- ShipmentActivity.java:2325 → `whArea = si.getWH_AREA()` (라벨 인쇄 시 창고구역)
- ShipmentActivity.java:2407 → 동일 (미트센터 바코드 라벨)
- ShipmentActivity.java:2484 → 동일
- ShipmentActivity.java:2869 → 동일

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력만

#### 5. 비즈니스 영향: **있음**
- **라벨 인쇄 시 창고 구역 정보 표시**
- 상품 보관 위치 정보로 라벨에 포함
- **제거 시 라벨에 창고 구역 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 75 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 106 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[32] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 2325, 2407, 2484, 2869 | **라벨 인쇄 로직 수정** (핵심) |
| DBHandler.java | 68, 143, 199, 696, 737 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 44, 360-365 | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CT_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 직접 표시되지 않음

#### 2. VIEW 내부 사용: O
- **서브쿼리로 원산지명 조회**
- VW_PDA_WID_LIST:93 → `(SELECT BC.CODE_NAME FROM B_COMMON_CODE BC WHERE BC.CODE = WR.CT_CODE AND BC.MASTER_CODE = 'HOMEPLUS_ORIGIN_CODE' AND BC.STATUS = 'Y')||'산' AS CT_NAME`
- CT_CODE를 기준으로 공통코드에서 원산지명을 조회하여 '산' 붙임

#### 3. 안드로이드 소스 사용: O
- **라벨 인쇄에 사용 (원산지명)**
- ShipmentActivity.java:2258 → `String ctName = si.getCT_NAME()` (원산지명 변수 설정)
- 라벨 인쇄 시 원산지명 출력

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력만

#### 5. 비즈니스 영향: **있음**
- **라벨 인쇄 시 원산지명 표시**
- 예: "호주산", "미국산" 등
- **제거 시 라벨에 원산지명 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 78 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 106 | out.println()에서 제거 |
| (외 JSP 파일들) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[35] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 2258 | **라벨 인쇄 로직 수정** (핵심) |
| DBHandler.java | 71, 146, 202, 699, 740 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 47, 384-389 | 필드, getter/setter 제거 |
| DBInfo.java | 120 | 상수 제거 |

---

### USE_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 직접 표시되지 않음

#### 2. VIEW 내부 사용: X
- VW_PDA_WID_LIST:113 → `EO.USE_NAME` (SELECT만)
- WHERE, JOIN, DECODE 조건에 사용 없음

#### 3. 안드로이드 소스 사용: O
- **라벨 인쇄에 사용 (용도명)**
- ShipmentActivity.java:2021 → `Log.d(TAG, "용도명 : " + si.getUSE_NAME())` (로그)
- ShipmentActivity.java:2031 → `pBarcodeStr3 = si.EMARTITEM + "," + si.getUSE_NAME()` (바코드 문자열)
- ShipmentActivity.java:2265 → `belowBarcodeString = si.EMARTITEM + "," + si.getUSE_NAME()` (바코드 하단 문자열)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력만

#### 5. 비즈니스 영향: **있음**
- **라벨 인쇄 시 용도명 표시**
- 바코드 하단에 "이마트상품명,용도명" 형식으로 출력
- **제거 시 라벨에 용도명 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 76 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 106 | out.println()에서 제거 |
| (외 JSP 파일들) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[33] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 2021, 2031, 2265 | **라벨 인쇄 로직 수정** (핵심) |
| DBHandler.java | 69, 144, 200, 697, 738 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 45, 368-373 | 필드, getter/setter 제거 |
| DBInfo.java | 118 | 상수 제거 |

---

### BRANDNAME

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **DE_COMMON 함수로 브랜드명 조회**
- VW_PDA_WID_LIST:77 → `DE_COMMON('BRAND', ID.BRAND_CODE) AS BRANDNAME`
- BRAND_CODE를 기준으로 공통코드에서 브랜드명 조회

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 사용 없음
- 로컬 DB 저장/조회 경유만 존재
- 분기, 계산, 서버 전송에 사용 없음

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력만

#### 5. 비즈니스 영향: 없음
- 화면에 표시되지 않음
- 서버로 전송되지 않음
- 조건 분기/계산에 사용되지 않음
- **제거해도 비즈니스 기능에 영향 없음**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | 58 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | 101 | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[15] | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 50, 126, 181, 247, 293, 351, 399, 679, 720 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 21, 207-212 | 필드, getter/setter 제거 |
| DBInfo.java | 41 | 상수 제거 |

---

### BARCODEGOODS

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **S_BARCODE_INFO 서브쿼리로 조회**
- VW_PDA_WID_LIST:101-107 → `(SELECT BARCODEGOODS FROM S_BARCODE_INFO A WHERE A.PACKER_CLIENT_CODE = BD.PACKER_CODE AND A.PACKER_PRODUCT_CODE = BD.PACKER_PRODUCT_CODE AND A.status = 'Y' AND ROWNUM < 2) BARCODEGOODS`
- 패커코드, 패커상품코드를 기준으로 바코드상품 코드 조회

#### 3. 안드로이드 소스 사용: O
- **바코드 검증 로직에서 핵심적으로 사용**
- ShipmentActivity.java:1335 → `String bg = bi.getBARCODEGOODS();`
- ShipmentActivity.java:1340-1341 → 로그 출력 (바코드 검증 디버깅)
- ShipmentActivity.java:1348-1349 → 임시 바코드와 비교 검증 `temp_bg.equals(bg)`
- ShipmentActivity.java:1419-1430 → 동일 (바코드 검증 로직)
- ShipmentActivity.java:3014 → `find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type)` (작업 정보 조회)
- **DB 로컬 저장/조회에 사용**
- DBHandler.java:63 → 로컬 DB 스키마 정의
- DBHandler.java:139, 260, 364, 692 → SELECT 문에서 조회
- DBHandler.java:194, 306, 412 → cursor에서 값 추출
- DBHandler.java:266 → `WHERE BARCODEGOODS = '...'` 조건으로 조회
- DBHandler.java:733 → INSERT 문에서 저장
- **바코드 정보 검색에서 사용**
- ProgressDlgBarcodeSearch.java:115 → `bi.setBARCODEGOODS(temp[6].toString())`
- ProgressDlgBarcodeSearch.java:142 → 로그 출력

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력만

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 검증 로직의 핵심 요소**
- 스캔한 바코드와 서버 BARCODEGOODS 일치 여부 확인
- **바코드 범위 검증에 사용** (BARCODEGOODS_FROM, BARCODEGOODS_TO와 함께)
- **제거 시 바코드 검증 기능 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp 등 | - | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp 등 | - | out.println()에서 제거 |
| (외 10개 JSP 파일) | - | 동일하게 SELECT/출력 제거 |
| ProgressDlgBarcodeSearch.java | 115, 142 | **바코드 정보 파싱/로그 제거** |
| ProgressDlgShipSearch.java | temp[?] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1335, 1340-1349, 1419-1430, 3014 | **바코드 검증 로직 수정** (핵심) |
| DBHandler.java | 63, 139, 194, 260, 266, 306, 364, 412, 692, 733 | 스키마/INSERT/SELECT/WHERE에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EMART_PLANT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 setText() 등에 사용 없음
- 화면에 표시되지 않음

#### 2. VIEW 내부 사용: O
- **DECODE로 STORE_CODE 조건부 조회**
- VW_PDA_WID_LIST:116 → `DECODE(EO.STORECODE, '9820', BSI.EMART_PLANT_CODE, NULL) AS EMART_PLANT_CODE`
- VW_PDA_WID_LIST:268 → 동일 (UNION ALL 쿼리)
- STORECODE가 '9820'(이마트)인 경우에만 B_SUPPLIER_ITEM 테이블에서 가공장 코드 조회

#### 3. 안드로이드 소스 사용: O
- **이마트 미트센터 바코드 분기 조건에 사용**
- ShipmentActivity.java:2349 → `si.getBARCODE_TYPE().equals("M0") && si.getSTORE_CODE().equals("9231") && si.getEMARTLOGIS_CODE().equals("0000000") && !si.getEMART_PLANT_CODE().equals("")`
- ShipmentActivity.java:2429 → `si.getBARCODE_TYPE().equals("M0") && si.getSTORE_CODE().equals("9231") && !si.getEMARTLOGIS_CODE().equals("0000000") && si.getEMART_PLANT_CODE().equals("")`
- **바코드 생성에 핵심적으로 사용**
- ShipmentActivity.java:2379 → `meatCenterBarcode = ... + si.getEMART_PLANT_CODE()` (바코드 문자열 구성)
- ShipmentActivity.java:2380 → `meatCenterBarcodeStr = ... + si.getEMART_PLANT_CODE()` (바코드 표시 문자열)
- **DB 로컬 저장/조회**
- DBHandler.java:73, 149, 204, 702, 743 → 스키마/INSERT/SELECT
- ProgressDlgShipSearch.java:223 → `si.setEMART_PLANT_CODE(temp[37].toString())`

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- SELECT/출력만

#### 5. 비즈니스 영향: **있음 (핵심)**
- **이마트 미트센터 라벨 타입 분기의 핵심**
- EMART_PLANT_CODE 유무에 따라 "ERP-미트센터출하코드" vs "미트센터출하코드" 라벨 구분
- **바코드 구성요소로 직접 사용** (이마트 가공장 코드)
- **제거 시 이마트 미트센터 바코드 분기/생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment.jsp | 80 | SELECT 문에서 컬럼 제거 |
| search_shipment.jsp | 107 | out.println()에서 제거 |
| ProgressDlgShipSearch.java | 223 | temp[37] 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 2349, 2429 | **분기 조건 수정** (핵심) |
| ShipmentActivity.java | 2379, 2380 | **바코드 생성 로직 수정** (핵심) |
| DBHandler.java | 73, 149, 204, 702, 743 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 49, 59-64 | 필드, getter/setter 제거 |
| DBInfo.java | 61 | 상수 제거 |

---

## 변경 이력

| 날짜         | 컬럼명                 | 내용                                                           | 작성자 |
| ---------- | ------------------- | ------------------------------------------------------------ | --- |
| 2025-12-09 | GI_H_ID             | 최초 분석 - 미사용 판정                                               | -   |
| 2025-12-09 | CLIENT_CODE         | 분석 - 미사용 판정 (함수 파라미터로만 전달, 실제 로직에서 미사용)                      | -   |
| 2025-12-09 | GI_REQ_DATE         | 분석 - **사용** 판정 (서버/앱 WHERE 조건에서 사용)                          | -   |
| 2025-12-09 | GI_D_ID             | 분석 - **사용** 판정 (핵심 식별자, 서버 전송/DB WHERE/DDL 사용)               | -   |
| 2025-12-09 | EOI_ID              | 분석 - 미사용 판정 (ORDER BY로만 사용, 비즈니스 로직 미사용)                     | -   |
| 2025-12-09 | ITEM_CODE           | 분석 - **사용** 판정 (서버 전송, WHERE 조건, DDL 사용)                     | -   |
| 2025-12-09 | GI_REQ_PKG          | 분석 - **사용** 판정 (UI 표시, 완료 판단 분기, 진행률 계산)                     | -   |
| 2025-12-09 | GI_REQ_QTY          | 분석 - **사용** 판정 (UI 표시, 총 중량 계산)                              | -   |
| 2025-12-09 | AMOUNT              | 분석 - 미사용 판정 (경유만, Activity 사용 없음)                            | -   |
| 2025-12-09 | GOODS_R_ID          | 분석 - 미사용 판정 (경유만, Activity 사용 없음)                            | -   |
| 2025-12-09 | BRAND_CODE          | 분석 - **사용** 판정 (서버 전송, WHERE 조건, DDL 사용)                     | -   |
| 2025-12-09 | GI_H_ID             | **수정** - VIEW JOIN 조건 사용 확인 → **사용** 판정                      | -   |
| 2025-12-09 | EOI_ID              | **수정** - VIEW WHERE/JOIN 조건 사용 확인 → **사용** 판정                | -   |
| 2025-12-09 | GI_REQ_PKG          | **수정** - VIEW WHERE 조건 사용 확인 (GI_REQ_PKG <> 0)               | -   |
| 2025-12-09 | GOODS_R_ID          | **수정** - VIEW JOIN 조건 사용 확인 → **사용** 판정                      | -   |
| 2025-12-09 | GR_REF_NO           | 분석 - 미사용 판정 (VIEW SELECT만, WHERE/JOIN/Activity 사용 없음)        | -   |
| 2025-12-09 | PACKING_QTY         | 분석 - VIEW SELECT 컬럼 아님 (WHERE 조건에만 사용, 앱으로 전달 안됨)            | -   |
| 2025-12-09 | IMPORT_ID_NO        | 분석 - **사용** 판정 (바코드 생성에 필수, VIEW BL_NO 계산에 사용)               | -   |
| 2025-12-09 | BL_NO               | 분석 - **사용** 판정 (작업 대상 검색 분기, DB WHERE 조건, VIEW DECODE 계산)    | -   |
| 2025-12-09 | ITEM_SPEC           | 분석 - **사용** 판정 (라벨 인쇄 시 상품 스펙/냉장냉동 표시)                       | -   |
| 2025-12-09 | CT_CODE             | 분석 - **사용** 판정 (라벨 인쇄 원산지 출력, VIEW 서브쿼리 조건)                  | -   |
| 2025-12-09 | BL_D_ID             | 분석 - VIEW SELECT 컬럼 아님 (JOIN 조건에만 사용, 앱으로 전달 안됨)             | -   |
| 2025-12-09 | BL_S_ID             | 분석 - VIEW SELECT 컬럼 아님 (JOIN 조건에만 사용, 앱으로 전달 안됨)             | -   |
| 2025-12-09 | GR_WAREHOUSE_CODE   | 분석 - **사용** 판정 (서버 요청 WHERE 조건, 창고별 필터링)                     | -   |
| 2025-12-09 | CONTRACT_TYPE       | 분석 - VIEW SELECT 컬럼 아님 (WHERE 조건에만 사용, 해외/국내 매입 구분)          | -   |
| 2025-12-09 | PACKER_CODE         | 분석 - **사용** 판정 (패커별 분기 조건, VIEW JOIN 조건)                     | -   |
| 2025-12-09 | PACKER_PRODUCT_CODE | 분석 - **사용** 판정 (UI 표시, 서버 전송, DB 조회, DDL INSERT)             | -   |
| 2025-12-09 | OFFER_D_ID          | 분석 - VIEW SELECT 컬럼 아님 (JOIN 조건에만 사용, 앱으로 전달 안됨)             | -   |
| 2025-12-09 | ITEM_TYPE           | 분석 - **사용** 판정 (상품 타입별 분기 핵심, VIEW 서브쿼리 조건)                  | -   |
| 2025-12-09 | MAJOR_CATEGORY      | 분석 - **사용** 판정 (VIEW DECODE 계산에서 BARCODE_TYPE 결정에 사용)        | -   |
| 2025-12-09 | CONTAINER_TYPE      | 분석 - 미사용 판정 (VIEW SELECT만, Activity 사용 없음)                   | -   |
| 2025-12-09 | STORE_NAME          | 분석 - VIEW SELECT 컬럼 아님 (서브쿼리 내부에서만 사용, 앱으로 전달 안됨)            | -   |
| 2025-12-09 | STORE_CODE          | 분석 - **사용** 판정 (미트센터 분기 조건, 바코드 생성, VIEW JOIN 조건)            | -   |
| 2025-12-09 | CENTER_CODE         | 분석 - VIEW SELECT 컬럼 아님 (서브쿼리 JOIN/DECODE 조건에만 사용, 앱으로 전달 안됨) | -   |
| 2025-12-09 | EMARTITEM_CODE      | 분석 - **사용** 판정 (바코드 생성 핵심, VIEW JOIN 조건)                     | -   |
| 2025-12-09 | ITEM_NAME           | 분석 - **사용** 판정 (UI 상품명 표시, VIEW DECODE 계산)                   | -   |
| 2025-12-09 | STORE_IN_DATE       | 분석 - **사용** 판정 (라벨 인쇄 납품일자 출력)                               | -   |
| 2025-12-09 | BARCODE_TYPE        | 분석 - **사용** 판정 (바코드 타입별 분기 핵심, VIEW DECODE 계산)               | -   |
| 2025-12-09 | PACKWEIGHT          | 분석 - **사용** 판정 (제품 타입 계근 기본중량, 바코드 중량 문자열)                   | -   |
| 2025-12-09 | EMARTLOGIS_CODE     | 분석 - **사용** 판정 (바코드 생성 핵심, 미트센터 분기 조건)                       | -   |
| 2025-12-09 | USE_CODE            | 분석 - **사용** 판정 (특정 바코드 타입에서 바코드 구성요소)                        | -   |
| 2025-12-09 | CENTERNAME          | 분석 - **사용** 판정 (센터 유형 분기 핵심, 라벨 인쇄, DB WHERE 조건)             | -   |
| 2025-12-09 | WH_AREA             | 분석 - **사용** 판정 (라벨 인쇄 시 창고 구역 정보 표시)                         | -   |
| 2025-12-09 | CT_NAME             | 분석 - **사용** 판정 (라벨 인쇄 시 원산지명 표시, VIEW 서브쿼리)                   | -   |
| 2025-12-09 | USE_NAME            | 분석 - **사용** 판정 (라벨 인쇄 시 용도명 표시, 바코드 하단 문자열)                   | -   |
| 2025-12-09 | CENTER_SCALE_USE_YN | 분석 - VIEW SELECT 컬럼 아님 (BARCODE_TYPE DECODE 조건에만 사용, 앱으로 전달 안됨) | -   |
| 2025-12-09 | BRANDNAME           | 분석 - 미사용 판정 (VIEW DE_COMMON 함수, Activity 사용 없음, 경유만)          | -   |
| 2025-12-09 | BARCODEGOODS        | 분석 - **사용** 판정 (바코드 검증 로직 핵심, VIEW 서브쿼리, DB WHERE 조건)         | -   |
| 2025-12-09 | EMART_PLANT_CODE    | 분석 - **사용** 판정 (이마트 미트센터 바코드 분기 및 생성 핵심, VIEW DECODE 조건부)     | -   |
