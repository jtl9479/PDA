# VW_PDA_WID_HOMEPLUS_LIST 컬럼 사용 여부 분석

## VIEW 정보
- **VIEW 명**: VW_PDA_WID_HOMEPLUS_LIST
- **용도**: 홈플러스 계근 리스트
- **searchType**: 1

---

## 컬럼 분석 결과

| 컬럼명                               | UI 노출 | VIEW 내부 | 안드로이드 소스 | DDL 사용 | 비즈니스 영향 | 판정     |
| --------------------------------- | ----- | ------- | -------- | ------ | ------- | ------ |
| [GI_H_ID](#gi_h_id)               | X     | **O**   | X        | X      | **있음**  | **사용** |
| [GI_D_ID](#gi_d_id)               | X     | X       | **O**    | **O**  | **있음**  | **사용** |
| [EOI_ID](#eoi_id)                 | X     | **O**   | X        | X      | **있음**  | **사용** |
| [ITEM_CODE](#item_code)           | X     | **O**   | **O**    | **O**  | **있음**  | **사용** |
| [ITEM_NAME](#item_name)           | **O** | **O**   | **O**    | X      | **있음**  | **사용** |
| [EMARTITEM_CODE](#emartitem_code) | X     | **O**   | **O**    | X      | **있음**  | **사용** |
| [EMARTITEM](#emartitem)           | **O** | X       | **O**    | X      | **있음**  | **사용** |
| [GI_REQ_PKG](#gi_req_pkg)         | **O** | **O**   | **O**    | X      | **있음**  | **사용** |
| [GI_REQ_QTY](#gi_req_qty)         | **O** | X       | **O**    | X      | **있음**  | **사용** |
| [AMOUNT](#amount)                 | X     | X       | X        | X      | 없음      | 미사용    |
| [GOODS_R_ID](#goods_r_id)         | X     | **O**   | X        | X      | **있음**  | **사용** |
| [GR_REF_NO](#gr_ref_no)           | X     | X       | X        | X      | 없음      | 미사용    |
| [GI_REQ_DATE](#gi_req_date)       | X     | **O**   | X        | X      | **있음**  | **사용** |
| [BL_NO](#bl_no)                   | **O** | **O**   | **O**    | X      | **있음**  | **사용** |
| [BRAND_CODE](#brand_code)         | X     | **O**   | **O**    | **O**  | **있음**  | **사용** |
| [BRANDNAME](#brandname)           | X     | X       | X        | X      | 없음      | 미사용    |
| [CLIENT_CODE](#client_code)       | X     | **O**   | **O**    | X      | **있음**  | **사용** |
| [CLIENTNAME](#clientname)         | **O** | X       | **O**    | X      | **있음**  | **사용** |
| [CENTERNAME](#centername)         | **O** | **O**   | **O**    | X      | **있음**  | **사용** |
| [ITEM_SPEC](#item_spec)           | **O** | X       | **O**    | X      | **있음**  | **사용** |
| [CT_CODE](#ct_code)               | **O** | X       | **O**    | X      | **있음**  | **사용** |
| [PACKER_CODE](#packer_code)       | X     | **O**   | **O**    | X      | **있음**  | **사용** |
| [IMPORT_ID_NO](#import_id_no)     | **O** | **O**   | **O**    | X      | **있음**  | **사용** |
| [PACKERNAME](#packername)         | X     | X       | X        | X      | 없음      | 미사용    |
| [PACKER_PRODUCT_CODE](#packer_product_code) | **O** | **O** | **O** | **O** | **있음** | **사용** |
| [BARCODE_TYPE](#barcode_type)     | X     | X       | **O**    | X      | **있음**  | **사용** |
| [ITEM_TYPE](#item_type)           | X     | **O**   | **O**    | X      | **있음**  | **사용** |
| [PACKWEIGHT](#packweight)         | X     | X       | **O**    | X      | **있음**  | **사용** |
| [BARCODEGOODS](#barcodegoods)     | X     | X       | **O**    | X      | **있음**  | **사용** |
| [STORE_IN_DATE](#store_in_date)   | **O** | X       | **O**    | X      | **있음**  | **사용** |
| [GR_WAREHOUSE_CODE](#gr_warehouse_code) | X | X | X | X | 없음 | 미사용 |
| [EMARTLOGIS_CODE](#emartlogis_code) | X | **O** | **O** | X | **있음** | **사용** |
| [EMARTLOGIS_NAME](#emartlogis_name) | X | X | X | X | 없음 | 미사용 |
| [HOMPLUS_STORE_CODE](#homplus_store_code) | X | **O** | X | X | **있음** | **사용** |


---

## 상세 분석

### GI_H_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_HOMEPLUS_LIST:108 → `ON IH.GI_H_ID = ID.GI_H_ID`
- VW_PDA_WID_HOMEPLUS_LIST:223 → `ON IH.GI_H_ID = ID.GI_H_ID` (UNION ALL 쿼리)
- W_GOODS_IH(출고헤더)와 W_GOODS_ID(출고상세) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음
- 로컬 DB 저장/조회 경유만 존재

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- VIEW에서 출고헤더-출고상세 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일                              | 라인                                         | 수정 내용                  |
| ------------------------------- | ------------------------------------------ | ---------------------- |
| VW_PDA_WID_HOMEPLUS_LIST (VIEW) | 108, 223                                   | **JOIN 조건 - 제거 불가**    |
| search_shipment_homeplus.jsp    | 43                                         | SELECT 문에서 컬럼 제거       |
| search_shipment_homeplus.jsp    | 89                                         | out.println()에서 제거     |
| ProgressDlgShipSearch.java      | 157                                        | temp[0] 파싱 제거, 인덱스 조정  |
| DBHandler.java                  | 35, 111, 166, 232, 278, 336, 384, 664, 705 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java             | 6, 87-92                                   | 필드, getter/setter 제거   |
| DBInfo.java                     | 26                                         | 상수 제거                  |

---

### GI_D_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: O
- **서버 전송에 핵심적으로 사용**
- ShipmentActivity.java:3320, 3430 → `packet += list_send_info.get(i).getGI_D_ID()` (서버 전송 패킷 구성)
- ShipmentActivity.java:3370, 3502 → `completeStr = arSM.get(j).getGI_D_ID() + "::"` (완료 문자열)
- **DB 조회 WHERE 조건에 사용**
- ShipmentActivity.java:3302-3304 → `qry_where = "GI_D_ID = " + arSM.get(i).getGI_D_ID()`
- ShipmentActivity.java:877, 3227, 3396 → `updatequeryShipment()` 파라미터
- ShipmentActivity.java:2953, 3135 → `selectqueryListGoodsWetInfo()` 파라미터
- ShipmentActivity.java:3360, 3493 → `updatequeryGoodsWet()` 파라미터
- **작업 식별/비교에 사용**
- ShipmentActivity.java:511 → `arSM.get(i).getGI_D_ID().equals(gi_d_id)` (작업 대상 식별)
- ShipmentActivity.java:3366, 3498 → `arSM.get(j).getGI_D_ID().equals(list_send_info.get(i).getGI_D_ID())`
- ShipmentActivity.java:1116 → `gi.setGI_D_ID()` (계근 정보 설정)

#### 4. DDL 사용: O
- **JSP INSERT/UPDATE 문에 사용**
- insert_goods_wet_homeplus.jsp:51 → INSERT 컬럼
- insert_goods_wet_homeplus.jsp:109 → `WHERE GI_D_ID = ?` (UPDATE 조건)

#### 5. 비즈니스 영향: **있음 (핵심)**
- **출고 상세 행 식별자로 핵심적으로 사용**
- **서버 전송, DB 조회/수정, DDL 모두 사용**
- **제거 시 출고 데이터 식별/전송/수정 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 44, 89 | SELECT/출력 제거 |
| insert_goods_wet_homeplus.jsp | 51, 109 | **INSERT/WHERE 제거** (핵심) |
| ProgressDlgShipSearch.java | temp[1] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 다수 | **서버전송/DB조회/작업식별 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EOI_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_HOMEPLUS_LIST:135 → `ON ID.EOI_ID = EO.EOI_ID`
- VW_PDA_WID_HOMEPLUS_LIST:250 → `ON ID.EOI_ID = EO.EOI_ID` (UNION ALL 쿼리)
- **VIEW WHERE 조건으로 사용**
- VW_PDA_WID_HOMEPLUS_LIST:141 → `AND EO.EOI_ID IS NOT NULL`
- VW_PDA_WID_HOMEPLUS_LIST:254 → `AND EO.EOI_ID IS NOT NULL`
- W_GOODS_ID(출고상세)와 W_E_ORDER_ITEM(이마트 주문) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음
- 로컬 DB 저장/조회 경유만 존재

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- **JSP ORDER BY 정렬에 사용**
- search_shipment_homeplus.jsp:77 → `ORDER BY ... EOI_ID ASC`

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN/WHERE 조건으로 필수**
- 출고상세와 이마트 주문 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_HOMEPLUS_LIST (VIEW) | 135, 141, 250, 254 | **JOIN/WHERE 조건 - 제거 불가** |
| search_shipment_homeplus.jsp | 45, 77, 89 | SELECT/ORDER BY/출력 제거 |
| ProgressDlgShipSearch.java | temp[2] | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### ITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **ITEM_NAME 계산에 사용**
- VW_PDA_WID_HOMEPLUS_LIST:47 → `DE_ITEM(ID.ITEM_CODE)` (상품명 조회 함수 파라미터)
- VW_PDA_WID_HOMEPLUS_LIST:152 → `DE_ITEM(ID.ITEM_CODE)` (UNION ALL 쿼리)
- **PACKER_PRODUCT_CODE/BARCODEGOODS DECODE 조건에 사용**
- VW_PDA_WID_HOMEPLUS_LIST:197, 207 → `DECODE(OD.PACKER_PRODUCT_CODE, NULL, ID.ITEM_CODE, ...)`

#### 3. 안드로이드 소스 사용: O
- **서버 전송에 사용**
- ShipmentActivity.java:3330, 3440 → `packet += list_send_info.get(i).getITEM_CODE()` (서버 전송 패킷)
- ShipmentActivity.java:3370, 3502 → `completeStr = ... + arSM.get(j).getITEM_CODE()` (완료 문자열)
- **계근 정보 설정에 사용**
- ShipmentActivity.java:1127 → `gi.setITEM_CODE(arSM.get(current_work_position).getITEM_CODE())`

#### 4. DDL 사용: O
- **JSP UPDATE WHERE 조건에 사용**
- insert_goods_wet_homeplus.jsp:109 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet_homeplus.jsp:117 → `pstmt.setString(6, splitData[10])` (ITEM_CODE 파라미터)

#### 5. 비즈니스 영향: **있음 (핵심)**
- **상품 식별자로 핵심적으로 사용**
- **서버 전송, DDL WHERE 조건, VIEW 함수 파라미터로 사용**
- **제거 시 상품 식별/전송/수정 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 46, 89 | SELECT/출력 제거 |
| insert_goods_wet_homeplus.jsp | 109, 117 | **WHERE 조건 제거** (핵심) |
| ProgressDlgShipSearch.java | temp[3] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1127, 3330, 3370, 3440, 3502 | **서버전송/계근정보 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### ITEM_NAME


#### 1. UI 노출: O
- **화면에 상품명 표시**
- ShipmentActivity.java:1573 → `edit_product_name.setText(arSM.get(work_position).getITEM_NAME())`
- ShipmentActivity.java:3171 → `edit_product_name.setText(arSM.get(0).getITEM_NAME().toString())`
- ShipmentActivity.java:3795 → `detail_edit_ppname.setText(si.getITEM_NAME())`

#### 2. VIEW 내부 사용: O
- **DECODE로 상품명 계산**
- VW_PDA_WID_HOMEPLUS_LIST:46-49 → `DECODE(EO.ITEM_NAME, NULL, DE_ITEM(ID.ITEM_CODE), EO.ITEM_NAME) AS ITEM_NAME`
- EO.ITEM_NAME이 NULL이면 DE_ITEM 함수로 상품명 조회

#### 3. 안드로이드 소스 사용: O
- **UI 표시에 사용** (위 참조)
- **리스트 표시에 사용**
- ShipmentActivity.java:3019 → 리스트 항목 구성에 사용 (주석처리됨)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음
- **JSP ORDER BY 정렬에 사용**
- search_shipment_homeplus.jsp:77 → `ORDER BY ... ITEM_NAME ASC`

#### 5. 비즈니스 영향: **있음 (핵심)**
- **사용자에게 상품명을 보여주는 핵심 컬럼**
- **제거 시 상품명 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 47, 77, 90 | SELECT/ORDER BY/출력 제거 |
| ProgressDlgShipSearch.java | temp[4] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1573, 3171, 3795 | **UI 표시 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GI_REQ_QTY

#### 1. UI 노출: O
- **화면에 출하요청중량 표시**
- ShipmentActivity.java:1208 → `edit_wet_weight.setText(arSM.get(current_work_position).getGI_REQ_QTY() + " / " + ... .getGI_QTY())`
- ShipmentActivity.java:1571 → 동일
- ShipmentActivity.java:3798 → `detail_edit_weight.setText(si.getGI_REQ_QTY() + " / " + si.getGI_QTY())`
- ShipmentActivity.java:4019 → 동일

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: O
- **UI 표시에 사용** (위 참조)
- **총 중량 계산에 사용**
- ShipmentActivity.java:3031 → `centerTotalWeight += Double.parseDouble(arSM.get(i).getGI_REQ_QTY())` (센터 총 요청중량)
- ShipmentActivity.java:3186 → 동일

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **사용자에게 요청 중량 정보 표시**
- **센터별 총 요청중량 계산에 사용**
- **제거 시 중량 정보 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 51, 91 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[8] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1208, 1571, 3031, 3186, 3798, 4019 | **UI표시/중량계산 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### AMOUNT

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- `ID.AMOUNT` (출하상품금액)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음
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
| search_shipment_homeplus.jsp | 52, 91 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[9] | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GOODS_R_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_HOMEPLUS_LIST:110 → `ON ID.GOODS_R_ID = WR.GOODS_R_ID`
- VW_PDA_WID_HOMEPLUS_LIST:225 → `ON ID.GOODS_R_ID = WR.GOODS_R_ID` (UNION ALL 쿼리)
- W_GOODS_ID(출고상세)와 W_GOODS_R(입고) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음
- 로컬 DB 저장/조회 경유만 존재

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- VIEW에서 출고상세-입고 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_HOMEPLUS_LIST (VIEW) | 110, 225 | **JOIN 조건 - 제거 불가** |
| search_shipment_homeplus.jsp | 53, 92 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[10] | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GR_REF_NO

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- `WR.GR_REF_NO` (창고입고번호)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음
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
| search_shipment_homeplus.jsp | 54, 92 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[11] | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GI_REQ_PKG

#### 1. UI 노출: O
- **화면에 출하요청수량 표시**
- ShipmentActivity.java:1207 → `edit_wet_count.setText(arSM.get(current_work_position).getGI_REQ_PKG() + " / " + ... .getPACKING_QTY())`
- ShipmentActivity.java:1570 → 동일
- ShipmentActivity.java:3797 → `detail_edit_count.setText(si.getGI_REQ_PKG() + " / " + si.getPACKING_QTY())`
- ShipmentActivity.java:4018 → 동일

#### 2. VIEW 내부 사용: O
- **VIEW WHERE 조건에 사용**
- VW_PDA_WID_HOMEPLUS_LIST:138 → `AND ID.GI_REQ_PKG <> 0`
- VW_PDA_WID_HOMEPLUS_LIST:253 → `AND ID.GI_REQ_PKG <> 0` (UNION ALL 쿼리)
- 출하요청수량이 0이 아닌 건만 조회

#### 3. 안드로이드 소스 사용: O
- **완료 판단 분기에 핵심적으로 사용**
- ShipmentActivity.java:772 → `!arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))` (미완료 건 필터)
- ShipmentActivity.java:862, 1104, 3247 → `getGI_REQ_PKG().equals(String.valueOf(...getPACKING_QTY()))` (완료 여부 판단)
- ShipmentActivity.java:1244 → `Integer.parseInt(...getGI_REQ_PKG()) <= ...getPACKING_QTY()` (완료 판단)
- ShipmentActivity.java:3369, 3501 → `getSAVE_CNT() == Integer.parseInt(...getGI_REQ_PKG())` (전송 완료 판단)
- **진행률 계산에 사용**
- ShipmentActivity.java:3030, 3185 → `centerTotalCount += Integer.parseInt(arSM.get(i).getGI_REQ_PKG())` (센터 총 요청수량)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **계근 완료 여부 판단의 핵심**
- 요청수량과 실제 계근수량 비교로 완료 상태 결정
- **제거 시 완료 판단 및 진행률 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_HOMEPLUS_LIST (VIEW) | 138, 253 | **WHERE 조건 수정** |
| search_shipment_homeplus.jsp | 50, 91 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[7] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 772, 862, 1104, 1207, 1244, 1478, 1570, 3030, 3043, 3185, 3247, 3369, 3501, 3797, 4018 | **완료판단/UI표시 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EMARTITEM

#### 1. UI 노출: O
- **라벨 인쇄 시 상품명 출력**
- ShipmentActivity.java:1638-1641 → `byteStream.write(WoosimCmd.getTTFcode(..., si.EMARTITEM + " / " + si.ITEM_SPEC))`
- ShipmentActivity.java:2152-2155 → `byteStream.write(WoosimCmd.getTTFcode(..., si.EMARTITEM))`
- ShipmentActivity.java:2371-2374, 2448-2451 → 동일 (미트센터 라벨)
- **바코드 하단 문자열에 사용**
- ShipmentActivity.java:2031 → `pBarcodeStr3 = si.EMARTITEM + "," + si.getUSE_NAME()`
- ShipmentActivity.java:2265 → `belowBarcodeString = si.EMARTITEM + "," + si.getUSE_NAME()`

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- `EO.ITEM_NAME AS EMARTITEM` (단순 별칭)

#### 3. 안드로이드 소스 사용: O
- **라벨 인쇄에 핵심적으로 사용** (위 참조)
- **계근 정보 설정에 사용**
- ShipmentActivity.java:1126 → `gi.setEMARTITEM(arSM.get(current_work_position).getEMARTITEM())`
- **로그 출력에 사용**
- ShipmentActivity.java:1588, 1643, 1685, 2158, 2518 → 상품명 로그

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **라벨 인쇄 시 상품명 표시의 핵심**
- 14자 초과 시 폰트 크기 조정 로직 존재
- **제거 시 라벨에 상품명 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 49, 90 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[6] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1126, 1638-1641, 2031, 2152-2155, 2265, 2371-2374, 2448-2451 | **라벨 인쇄 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EMARTITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW 서브쿼리 JOIN 조건에 사용**
- VW_PDA_WID_HOMEPLUS_LIST:128-129 → `ON EB.EMARTITEM_CODE = EOI.ITEM_CODE`
- VW_PDA_WID_HOMEPLUS_LIST:243-244 → 동일 (UNION ALL 쿼리)
- B_EMART_BARCODE와 W_E_ORDER_ITEM 테이블 연결

#### 3. 안드로이드 소스 사용: O
- **바코드 생성에 핵심적으로 사용**
- ShipmentActivity.java:1620-1621 → `pBarcode = si.getEMARTITEM_CODE() + print_weight_str + "00" + now`
- ShipmentActivity.java:1877-1878 → `pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO()`
- ShipmentActivity.java:1893-1894, 1910-1911, 1926-1927, 1943-1944 → 동일 패턴 (바코드 타입별 분기)
- **계근 정보 설정에 사용**
- ShipmentActivity.java:1125 → `gi.setEMARTITEM_CODE(arSM.get(current_work_position).getEMARTITEM_CODE())`
- **로그 출력에 사용**
- ShipmentActivity.java:1588, 1871, 1888, 1904, 1921, 1937 → 상품코드 로그

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성의 핵심 구성요소**
- 바코드 문자열 앞 6자리가 EMARTITEM_CODE에서 추출
- **제거 시 바코드 생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 48, 90 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[5] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1125, 1620-1621, 1877-1944 등 | **바코드 생성 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### STORE_IN_DATE

#### 1. UI 노출: O
- **라벨에 납품일자 인쇄**
- ShipmentActivity.java:2284 → `tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + ...substring(4,6) + "월 " + ...substring(6,8) + "일"`
- "YYYYMMDD" → "YYYY년 MM월 DD일" 형식으로 변환하여 라벨에 인쇄

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- VW_PDA_WID_HOMEPLUS_LIST:101 → `STORE_IN_DATE`
- VW_PDA_WID_HOMEPLUS_LIST:122 → 서브쿼리에서 `EOI.STORE_IN_DATE`
- VW_PDA_WID_HOMEPLUS_LIST:216 → `STORE_IN_DATE` (UNION ALL 쿼리)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: O
- **라벨 인쇄 시 납품일자 표시**
- ShipmentActivity.java:2283-2284 → 납품일자 포맷팅 및 인쇄
- ShipmentActivity.java:2300-2301 → 동일
- ShipmentActivity.java:2310-2311 → 동일
- ShipmentActivity.java:2397-2398 → 미트센터 라벨
- ShipmentActivity.java:2474-2475 → 미트센터 라벨
- ShipmentActivity.java:2615-2616 → 기타 라벨

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 인쇄 시 납품일자 표시**
- YYYYMMDD 형식을 "YYYY년 MM월 DD일" 형식으로 변환
- **제거 시 라벨에 납품일자 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 72, 97 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[29] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 2283-2284, 2300-2301, 2310-2311, 2397-2398, 2474-2475, 2615-2616 | **납품일자 라벨인쇄 로직 수정** |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### BARCODEGOODS

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용 (서브쿼리 결과)
- VW_PDA_WID_HOMEPLUS_LIST:94-100 → 서브쿼리로 S_BARCODE_INFO에서 BARCODEGOODS 조회
  ```sql
  (SELECT BARCODEGOODS FROM S_BARCODE_INFO A
   WHERE A.PACKER_CLIENT_CODE = BD.PACKER_CODE
   AND A.PACKER_PRODUCT_CODE = BD.PACKER_PRODUCT_CODE
   AND A.status = 'Y' AND ROWNUM < 2) BARCODEGOODS
  ```
- VW_PDA_WID_HOMEPLUS_LIST:208-214 → 동일 (UNION ALL 쿼리)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: O
- **바코드 검증/매칭에 핵심적으로 사용**
- ShipmentActivity.java:1335 → `String bg = bi.getBARCODEGOODS()` (바코드 상품 코드)
- ShipmentActivity.java:1336-1337 → `getBARCODEGOODS_FROM()`, `getBARCODEGOODS_TO()` (바코드 범위)
- ShipmentActivity.java:1348-1349 → `temp_bg.equals(bg)` (스캔한 바코드와 매칭)
- ShipmentActivity.java:1419-1430 → 동일 패턴
- **작업 상품 검색에 사용**
- ShipmentActivity.java:3014 → `find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type)`

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 스캔 검증의 핵심**
- 스캔한 바코드가 BARCODEGOODS와 일치하는지 확인
- BARCODEGOODS_FROM ~ BARCODEGOODS_TO 범위로 바코드 부분 추출
- **작업 대상 상품 검색에 사용**
- **제거 시 바코드 검증 및 상품 매칭 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 71, 97 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[28] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1335-1349, 1419-1430, 3014 | **바코드검증/상품검색 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### PACKWEIGHT

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- VW_PDA_WID_HOMEPLUS_LIST:93 → `EO.PACKWEIGHT`
- VW_PDA_WID_HOMEPLUS_LIST:120 → 서브쿼리에서 `EB.PACKWEIGHT`
- VW_PDA_WID_HOMEPLUS_LIST:204 → `EO.PACKWEIGHT` (UNION ALL 쿼리)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: O
- **ITEM_TYPE J(지정중량)일 때 중량 값으로 사용**
- ShipmentActivity.java:1026 → `item_weight = arSM.get(current_work_position).getPACKWEIGHT()` (J타입 지정 중량)
- **바코드 중량 문자열 생성에 사용**
- ShipmentActivity.java:1839 → `print_weight_str = si.getPACKWEIGHT()` (J타입 바코드 중량)
- ShipmentActivity.java:2685 → 동일 (롯데용)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **ITEM_TYPE J(지정중량) 처리의 핵심**
- J타입은 바코드에서 중량을 추출하지 않고 PACKWEIGHT 값 사용
- **바코드 중량 문자열 생성에 사용**
- **제거 시 J타입 계근 처리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 70, 97 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[27] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1026, 1839, 2685 | **J타입 지정중량 로직 수정** |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### ITEM_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건에 사용**
- VW_PDA_WID_HOMEPLUS_LIST:130 → `AND EB.ITEM_TYPE = 'W'` (B_EMART_BARCODE 테이블 JOIN 조건)
- VW_PDA_WID_HOMEPLUS_LIST:245 → 동일 (UNION ALL 쿼리)
- **하드코딩으로 'S' 값 반환**
- VW_PDA_WID_HOMEPLUS_LIST:92 → `'S' AS ITEM_TYPE` (주석: "PDA에서 소수점 2자리 계근 가능한 경우는 ITEM_TYPE S아님 J일 경우임")
- VW_PDA_WID_HOMEPLUS_LIST:236 → 동일 (UNION ALL 쿼리)

#### 3. 안드로이드 소스 사용: O
- **계근 방식 분기에 핵심적으로 사용**
- ShipmentActivity.java:917 → `getITEM_TYPE().equals("W") || getITEM_TYPE().equals("HW")` (바코드 계근)
- ShipmentActivity.java:970 → `getITEM_TYPE().equals("S")` (소수점 계근)
- ShipmentActivity.java:1024-1025 → `getITEM_TYPE().equals("J")` (지정된 중량 입력)
- ShipmentActivity.java:1035 → `getITEM_TYPE().equals("B")` (박스 타입)
- **바코드 생성 분기에 사용**
- ShipmentActivity.java:1829, 2676 → W/HW/S 타입별 바코드 생성
- ShipmentActivity.java:1837, 2684 → J 타입 바코드 생성
- ShipmentActivity.java:2589 → B 타입 처리

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **계근 방식 분기의 핵심**
  - W/HW: 바코드 계근 (중량 바코드에서 추출)
  - S: 소수점 2자리 계근
  - J: 지정된 중량 입력 (바코드에서 중량/제조일/박스시리얼 X)
  - B: 박스 타입
- **VIEW JOIN 조건으로 필수**
- **제거 시 계근 방식 분기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_HOMEPLUS_LIST (VIEW) | 130, 245 | **JOIN 조건 - 제거 불가** |
| search_shipment_homeplus.jsp | 69, 97 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[26] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 916-1045, 1797-1842, 2589, 2676-2688 | **계근방식/바코드생성 분기 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### BARCODE_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- VW_PDA_WID_HOMEPLUS_LIST:90 → `EO.BARCODE_TYPE`
- VW_PDA_WID_HOMEPLUS_LIST:118 → 서브쿼리에서 `EB.BARCODE_TYPE`
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: O
- **바코드 생성 switch 분기에 핵심적으로 사용**
- ShipmentActivity.java:1865 → `switch (si.getBARCODE_TYPE())` (바코드 타입별 생성 로직 분기)
- **미트센터 바코드 분기에 사용**
- ShipmentActivity.java:833 → `getBARCODE_TYPE().equals("M3") || getBARCODE_TYPE().equals("M4")` (미트센터 바코드)
- ShipmentActivity.java:1735, 1783 → 동일
- **라벨 인쇄 분기에 사용**
- ShipmentActivity.java:2098-2204 → 바코드 타입별 라벨 인쇄 로직 분기
  - M0, E0, E1, M8: 기본 바코드
  - M1: 특수 바코드
  - E2, E3: 이마트 바코드
  - M3, M4: 미트센터 바코드
  - M9: 특수 처리

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성 로직 분기의 핵심**
- 바코드 타입(M0/M1/M3/M4/M8/M9/E0/E1/E2/E3)에 따라 완전히 다른 바코드 생성
- **라벨 인쇄 형식 결정에 핵심**
- **제거 시 바코드 생성 로직 분기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 68, 96 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[25] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 833, 1735, 1783, 1865, 2098-2204 | **바코드생성/라벨인쇄 분기 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### PACKER_PRODUCT_CODE

#### 1. UI 노출: O
- **화면에 패커상품코드 표시**
- ShipmentActivity.java:1356, 1375, 1436 → `edit_product_code.setText(bi.getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:3172 → `edit_product_code.setText(arSM.get(0).getPACKER_PRODUCT_CODE().toString())`
- ShipmentActivity.java:3796 → `detail_edit_ppcode.setText(si.getPACKER_PRODUCT_CODE())`

#### 2. VIEW 내부 사용: O
- **BARCODEGOODS 서브쿼리 조건**
- VW_PDA_WID_HOMEPLUS_LIST:97 → `AND A.PACKER_PRODUCT_CODE = BD.PACKER_PRODUCT_CODE`
- VW_PDA_WID_HOMEPLUS_LIST:212 → 동일 (UNION ALL 쿼리)
- **DECODE로 NULL 처리**
- VW_PDA_WID_HOMEPLUS_LIST:196-198 → `DECODE(OD.PACKER_PRODUCT_CODE, NULL, ID.ITEM_CODE, OD.PACKER_PRODUCT_CODE)`

#### 3. 안드로이드 소스 사용: O
- **UI 표시에 사용** (위 참조)
- **서버 전송에 사용**
- ShipmentActivity.java:3323, 3433 → `packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::"`
- **DB 조회 파라미터로 사용**
- ShipmentActivity.java:877, 3227 → `updatequeryShipment(mContext, getGI_D_ID(), getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:2953, 3135 → `selectqueryListGoodsWetInfo(mContext, getGI_D_ID(), getPACKER_PRODUCT_CODE(), getCLIENT_CODE())`
- ShipmentActivity.java:3396, 3533 → `updatequeryShipment(mContext, getGI_D_ID(), getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:3938 → `selectqueryGoodsWet(this, getGI_D_ID(), getPACKER_PRODUCT_CODE(), getCLIENT_CODE())`
- **계근 정보 설정에 사용**
- ShipmentActivity.java:1119 → `gi.setPACKER_PRODUCT_CODE(arSM.get(current_work_position).getPACKER_PRODUCT_CODE())`
- **바코드 검색 조건에 사용**
- ShipmentActivity.java:1342, 1423 → PACKER_PRODUCT_CODE로 상품 검색

#### 4. DDL 사용: O
- **JSP INSERT 문에 사용**
- insert_goods_wet_homeplus.jsp:54 → `+ ", PACKER_PRODUCT_CODE"` (INSERT 컬럼)
- **JSP ORDER BY에 사용**
- search_shipment_homeplus.jsp:77 → `ORDER BY HOMPLUS_STORE_CODE ASC, PACKER_PRODUCT_CODE ASC, ...`

#### 5. 비즈니스 영향: **있음 (핵심)**
- **패커 상품 식별의 핵심**
- 서버 전송, DB 조회/수정, DDL INSERT 모두 사용
- **바코드 검색 및 계근 데이터 식별에 필수**
- **제거 시 패커 상품 식별/조회/전송 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 67, 77, 96 | SELECT/ORDER BY/출력 제거 |
| insert_goods_wet_homeplus.jsp | 54 | **INSERT 컬럼 제거** (핵심) |
| ProgressDlgShipSearch.java | temp[24] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 877, 1119, 1342-1440, 2953, 3135, 3172, 3227, 3323, 3396, 3433, 3533, 3796, 3938 | **서버전송/DB조회/UI표시 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### PACKERNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- `DE_CLIENT(BD.PACKER_CODE) AS PACKERNAME` (패커이름 계산 결과)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음
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
| search_shipment_homeplus.jsp | 66, 96 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[23] | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### IMPORT_ID_NO

#### 1. UI 노출: O
- **리스트 항목에 수입이력번호 표시**
- ShipmentActivity.java:3020 → `list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO())`
- **라벨에 이력번호 인쇄**
- ShipmentActivity.java:2612 → `si.getIMPORT_ID_NO().substring(8, 12)` (이력번호 일부)
- ShipmentActivity.java:2866 → `"이력(묶음)번호 : " + si.getIMPORT_ID_NO()` (전체 이력번호)

#### 2. VIEW 내부 사용: O
- **BL_NO 계산 DECODE 조건에 사용**
- VW_PDA_WID_HOMEPLUS_LIST:66 → `DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) BL_NO`
- VW_PDA_WID_HOMEPLUS_LIST:169 → 동일 (UNION ALL 쿼리)
- IMPORT_ID_NO가 있으면 BL_NO 대신 사용 (이력번호 우선)
- **NVL로 NULL 처리**
- VW_PDA_WID_HOMEPLUS_LIST:87 → `NVL(WR.IMPORT_ID_NO,'            ') IMPORT_ID_NO` (12자리 공백)

#### 3. 안드로이드 소스 사용: O
- **바코드 생성에 핵심적으로 사용**
- ShipmentActivity.java:1877-2050 → 다양한 바코드 타입별로 IMPORT_ID_NO 포함
  - `pBarcode = EMARTITEM_CODE + print_weight_str + pCompCode + IMPORT_ID_NO`
  - `pBarcode2 = EMARTLOGIS_CODE + print_weight_str + meatCenterCode + IMPORT_ID_NO`
- ShipmentActivity.java:2379, 2456 → 미트센터 바코드에 사용
- ShipmentActivity.java:2764-2765 → `pBarcode2 = si.getIMPORT_ID_NO()` (바코드 자체로 사용)
- **리스트 표시에 사용** (위 참조)
- **라벨 인쇄에 사용** (위 참조)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성의 핵심 구성요소**
- 수입이력번호가 바코드 문자열의 마지막 부분에 포함
- **VIEW에서 BL_NO 계산에 사용** - 이력번호 우선 로직
- **제거 시 바코드 생성 및 이력 추적 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 64, 95 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[22] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1874-2050, 2379, 2456, 2612, 2742-2765, 2866, 3020 | **바코드생성/라벨인쇄/리스트 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### PACKER_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **PACKERNAME 계산 함수 파라미터**
- VW_PDA_WID_HOMEPLUS_LIST:88 → `DE_CLIENT(BD.PACKER_CODE) AS PACKERNAME` (패커이름 조회)
- VW_PDA_WID_HOMEPLUS_LIST:194 → `DE_CLIENT(OD.PACKER_CODE) AS PACKERNAME` (UNION ALL 쿼리)
- **BARCODEGOODS 서브쿼리 조건**
- VW_PDA_WID_HOMEPLUS_LIST:96 → `WHERE A.PACKER_CLIENT_CODE = BD.PACKER_CODE` (바코드 정보 조회)
- VW_PDA_WID_HOMEPLUS_LIST:210 → 동일 (UNION ALL 쿼리)

#### 3. 안드로이드 소스 사용: O
- **특정 패커(킬코이) 분기 조건에 핵심적으로 사용**
- ShipmentActivity.java:347 → `getPACKER_CODE().equals("30228") && getSTORE_CODE().equals("9231")` (킬코이 + 미트센터)
- ShipmentActivity.java:793 → 동일 (킬코이 제품 + 이마트 미트센터 납품 시 특수 처리)
- ShipmentActivity.java:1699 → 동일 (소비기한 변조 출력)
- **로그 출력에 사용**
- ShipmentActivity.java:344, 883, 1693 → 패커코드 로그

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **특정 패커(킬코이-30228) 특수 처리의 핵심**
- 킬코이 + 미트센터 납품 시 소비기한 변조 출력 등 특수 로직 분기
- **VIEW에서 PACKERNAME 계산, BARCODEGOODS 조회에 사용**
- **제거 시 패커별 특수 처리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 65, 95 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[21] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 344, 347, 793, 883, 1693, 1699 | **킬코이 패커 분기 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CT_CODE

#### 1. UI 노출: O
- **라벨에 원산지 인쇄**
- ShipmentActivity.java:2609 → `byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(si.getCT_CODE())))`

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- VW_PDA_WID_HOMEPLUS_LIST:85 → `(SELECT CT.CT_NAME FROM B_COUNTRY CT WHERE CT.CT_CODE = WR.CT_CODE) AS CT_CODE`
- 서브쿼리로 국가코드에서 국가명(원산지명) 조회
- VW_PDA_WID_HOMEPLUS_LIST:190 → `WR.CT_CODE` (UNION ALL 쿼리에서는 직접 컬럼 사용)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: O
- **라벨 인쇄에 사용** (위 참조)
- ShipmentActivity.java:2798 → 주석 처리됨 (원앤원 지점자리에 원산지 - 현재 표시 안함)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 인쇄 시 원산지 표시**
- VIEW에서 국가코드→국가명 변환 서브쿼리 사용
- **제거 시 라벨에 원산지 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 63, 95 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[20] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 2609 | **라벨 인쇄 로직 수정** |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### ITEM_SPEC

#### 1. UI 노출: O
- **라벨에 상품명과 함께 스펙 인쇄**
- ShipmentActivity.java:1639 → `byteStream.write(WoosimCmd.getTTFcode(35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC))` (14자 초과 시)
- ShipmentActivity.java:1641 → `byteStream.write(WoosimCmd.getTTFcode(40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC))` (14자 이하 시)

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- `WR.ITEM_SPEC` (스펙 - 냉장/냉동 등)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: O
- **라벨 인쇄에 사용** (위 참조)
- "상품명 / 스펙" 형태로 라벨에 인쇄
- ShipmentActivity.java:1643 → `Log.i(TAG, "상품명 / 냉장냉동 : " + si.EMARTITEM + " / " + si.ITEM_SPEC)` (로그)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 인쇄 시 상품 스펙 표시**
- 상품명과 함께 냉장/냉동 등 스펙 정보 표시
- **제거 시 라벨에 스펙 정보 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 62, 94 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[19] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1639, 1641, 1643 | **라벨 인쇄 로직 수정** |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CENTERNAME

#### 1. UI 노출: O
- **라벨에 센터명 인쇄**
- ShipmentActivity.java:2088 → `byteStream.write(WoosimCmd.getTTFcode(35, 35, si.CENTERNAME))` (7자 초과 시)
- ShipmentActivity.java:2093 → `byteStream.write(WoosimCmd.getTTFcode(40, 40, si.CENTERNAME))` (7자 이하 시)

#### 2. VIEW 내부 사용: O
- **CLIENTNAME 계산 DECODE 조건에 사용**
- VW_PDA_WID_HOMEPLUS_LIST:75 → `SUBSTR(EO.CENTERNAME, 1, 2)` (센터명 앞 2자로 분기)
- 센터명 앞 2자가 'CJ'인 경우 DE_CLIENT2 함수 호출, 그 외는 STORENAME 사용
- VW_PDA_WID_HOMEPLUS_LIST:83 → `EO.CENTERNAME` (센터명 SELECT)
- VW_PDA_WID_HOMEPLUS_LIST:121 → `BCC.CODE_NAME CENTERNAME` (서브쿼리에서 센터명 조회)

#### 3. 안드로이드 소스 사용: O
- **바코드 타입 분기 조건에 핵심적으로 사용**
- ShipmentActivity.java:367 → `getCENTERNAME().contains("TRD") || getCENTERNAME().contains("WET") || getCENTERNAME().contains("E/T")` (트레이더스/WET/E/T 센터 분기)
- ShipmentActivity.java:839 → 센터명 기반 바코드 처리 분기 (긴 라인)
- ShipmentActivity.java:1745 → `si.getCENTERNAME().contains("E/T") || si.getCENTERNAME().contains("WET") || si.getCENTERNAME().contains("TRD")` (트레이더스 납품분 처리)
- **라벨 인쇄에 사용** (위 참조)
- ShipmentActivity.java:2086 → `si.CENTERNAME.length()` (글자 수에 따른 폰트 크기 조정)
- **로그 출력에 사용**
- ShipmentActivity.java:791, 1684, 2517, 2642 → `si.CENTERNAME` (센터명 로그)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 타입 분기의 핵심** - TRD/WET/E/T 센터 여부로 바코드 처리 로직 분기
- **라벨 인쇄 시 센터명 표시**
- **VIEW에서 CLIENTNAME 계산에 사용**
- **제거 시 센터별 바코드 처리 및 라벨 인쇄 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 61, 94 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[18] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 367, 791, 839, 1684, 1745, 2086-2093, 2517, 2642 | **바코드분기/라벨인쇄 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CLIENTNAME

#### 1. UI 노출: O
- **리스트 항목에 출고업체명 표시**
- ShipmentActivity.java:3020 → `list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO())`
- ShipmentActivity.java:3175 → `list_position.add(arSM.get(i).getCLIENTNAME())`

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- DECODE로 계산된 결과값: `DECODE(SUBSTR(EO.CENTERNAME,1,2), 'CJ', DE_CLIENT2(IH.CLIENT_CODE)||'('||EO.STORECODE||')', EO.STORENAME) AS CLIENTNAME`
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: O
- **리스트 표시에 사용** (위 참조)
- **라벨 인쇄 시 매장명 파싱에 핵심적으로 사용**
- ShipmentActivity.java:2055-2064 → 업체명에서 매장명 추출 분기
  - `si.CLIENTNAME.contains("이마트")` → "이마트"로 split
  - `si.CLIENTNAME.contains("신세계백화점")` → "백화점"으로 split
  - `si.CLIENTNAME.contains("EVERY")` → "EVERY"로 split
  - `si.CLIENTNAME.contains("E/T")` → "E/T"로 split
  - 그 외 → `pointName = si.CLIENTNAME.toString()`
- ShipmentActivity.java:2553, 2771 → `pointName = si.CLIENTNAME.toString()` (라벨 인쇄 매장명)
- ShipmentActivity.java:2107, 2123 → `si.CLIENTNAME.toString().length()` (글자 수에 따른 폰트 크기 조정)
- **로그 출력에 사용**
- ShipmentActivity.java:1684, 2517, 2642 → `si.CLIENTNAME` (출고업체명 로그)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **라벨 인쇄 시 매장명 표시의 핵심**
- 업체명에서 매장명 추출 로직이 라벨 인쇄에 필수
- **리스트 항목 표시에 사용**
- **제거 시 매장명 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 60, 94 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[17] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1684, 2055-2064, 2107, 2123, 2517, 2553, 2642, 2771, 3020, 3175 | **리스트표시/라벨인쇄 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CLIENT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **CLIENTNAME 계산 함수 파라미터**
- VW_PDA_WID_HOMEPLUS_LIST:72 → `IH.CLIENT_CODE` (출고업체코드)
- VW_PDA_WID_HOMEPLUS_LIST:76 → `DE_CLIENT2(IH.CLIENT_CODE)` (CLIENTNAME 계산)
- DECODE로 센터명 앞 2자가 'CJ'인 경우 DE_CLIENT2 함수로 업체명 조회

#### 3. 안드로이드 소스 사용: O
- **DB 조회 파라미터로 사용**
- ShipmentActivity.java:2953 → `DBHandler.selectqueryListGoodsWetInfo(mContext, ...getGI_D_ID(), ...getPACKER_PRODUCT_CODE(), ...getCLIENT_CODE())`
- ShipmentActivity.java:3135 → 동일
- ShipmentActivity.java:3938 → `DBHandler.selectqueryGoodsWet(ShipmentActivity.this, si.getGI_D_ID(), si.getPACKER_PRODUCT_CODE(), si.getCLIENT_CODE())`
- 계근 정보 조회 시 CLIENT_CODE를 파라미터로 전달

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **DB 조회 파라미터로 필수**
- 계근 정보 조회 시 GI_D_ID + PACKER_PRODUCT_CODE + CLIENT_CODE로 데이터 식별
- **VIEW에서 CLIENTNAME 계산에 사용**
- **제거 시 계근 정보 조회 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 59, 93 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[16] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 2953, 3135, 3938 | **DB 조회 파라미터 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### BRANDNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- `DE_COMMON('BRAND', ID.BRAND_CODE) AS BRANDNAME` (브랜드명 계산 결과)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음
- 로컬 DB 저장/조회 경유만 존재

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: 없음
- 화면에 표시되지 않음
- 서버로 전송되지 않음
- 조건 분기/계산에 사용되지 않음
- **제거해도 비즈니스 기능에 영향 없음**

#### 6. 수정 위치
| 파일                           | 라인       | 수정 내용                  |
| ---------------------------- | -------- | ---------------------- |
| search_shipment_homeplus.jsp | 58, 93   | SELECT/출력 제거           |
| ProgressDlgShipSearch.java   | temp[15] | 파싱 제거, 인덱스 조정          |
| DBHandler.java               | -        | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java          | -        | 필드, getter/setter 제거   |
| DBInfo.java                  | -        | 상수 제거                  |


---

### BRAND_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **BRANDNAME 계산 함수 파라미터**
- VW_PDA_WID_HOMEPLUS_LIST:70 → `DE_COMMON('BRAND', ID.BRAND_CODE) AS BRANDNAME`
- VW_PDA_WID_HOMEPLUS_LIST:173 → 동일 (UNION ALL 쿼리)
- DE_COMMON 함수로 브랜드코드에서 브랜드명 조회

#### 3. 안드로이드 소스 사용: O
- **서버 전송에 사용**
- ShipmentActivity.java:3331 → `packet += list_send_info.get(i).getBRAND_CODE() + "::"` (서버 전송 패킷)
- ShipmentActivity.java:3441 → 동일
- **완료 문자열에 사용**
- ShipmentActivity.java:3370 → `completeStr = ...getGI_D_ID() + "::" + ...getITEM_CODE() + "::" + ...getBRAND_CODE() + "::" + Common.REG_ID`
- ShipmentActivity.java:3502 → 동일
- **계근 정보 설정에 사용**
- ShipmentActivity.java:1128 → `gi.setBRAND_CODE(arSM.get(current_work_position).getBRAND_CODE())`

#### 4. DDL 사용: O
- **JSP UPDATE WHERE 조건에 사용**
- insert_goods_wet_homeplus.jsp:109 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet_homeplus.jsp:118 → `pstmt.setString(7, splitData[11])` (BRAND_CODE 파라미터)

#### 5. 비즈니스 영향: **있음 (핵심)**
- **복합 키의 일부로 핵심적으로 사용**
- GI_D_ID + ITEM_CODE + BRAND_CODE로 출고 상세 행 식별
- **서버 전송, DDL WHERE 조건, VIEW 함수 파라미터로 사용**
- **제거 시 데이터 식별/전송/수정 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 57, 93 | SELECT/출력 제거 |
| insert_goods_wet_homeplus.jsp | 109, 118 | **WHERE 조건 제거** (핵심) |
| ProgressDlgShipSearch.java | temp[14] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1128, 3331, 3370, 3441, 3502 | **서버전송/완료문자열 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### BL_NO

#### 1. UI 노출: O
- **스피너(드롭다운)에 BL번호 표시**
- ShipmentActivity.java:109 → `private Spinner sp_bl_no;` (BL번호 스피너 선언)
- ShipmentActivity.java:1564 → `list_bl.add(arSM.get(work_position).getBL_NO())` (스피너 항목 추가)
- ShipmentActivity.java:1567 → `sp_bl_no.setAdapter(bl_adapter)` (어댑터 설정)

#### 2. VIEW 내부 사용: O
- **DECODE로 BL번호/이력번호 계산**
- VW_PDA_WID_HOMEPLUS_LIST:66-67 → `DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) BL_NO`
- VW_PDA_WID_HOMEPLUS_LIST:169-170 → 동일 (UNION ALL 쿼리)
- IMPORT_ID_NO(수입이력번호)가 NULL이면 BL_NO, 아니면 IMPORT_ID_NO 사용
- 주석: "BL_NO 컬럼에 이력번호를 넣어준다"

#### 3. 안드로이드 소스 사용: O
- **바코드 스캔 검증에 사용**
- ShipmentActivity.java:1317-1318 → `if (BL_NO.equals(arSM.get(current_work_position).getBL_NO()))` (바코드 일치 검증)
- **작업 대상 필터링에 사용**
- ShipmentActivity.java:772 → `if (temp_bl_no.equals(arSM.get(i).getBL_NO()) && ...)` (BL번호 같은 상품 검색)
- ShipmentActivity.java:778, 1569 → `work_bl_no = ...getBL_NO()` (현재 작업 BL번호 설정)
- **BL번호 유효성 검사에 사용**
- ShipmentActivity.java:1544 → `if(arSM.get(current_work_position).getBL_NO() == "")` (빈 BL번호 경고)
- **DB 조회 조건에 사용**
- ShipmentActivity.java:3131 → `DBHandler.selectqueryShipment(mContext, this.center_name, this.bl_no, false)` (BL번호 기준 조회)

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 스캔 검증의 핵심** - 스캔한 바코드와 BL번호 일치 확인
- **작업 대상 식별의 핵심** - BL번호로 작업할 상품 필터링
- **VIEW에서 이력번호 우선 로직** - IMPORT_ID_NO 우선, 없으면 BL_NO
- **제거 시 바코드 검증 및 작업 대상 식별 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 56, 92 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[13] | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 109, 188, 614, 762-784, 1317-1318, 1544, 1564-1569, 3080, 3103-3131 | **바코드검증/작업필터 로직 수정** (핵심) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GI_REQ_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW WHERE 조건으로 사용**
- VW_PDA_WID_HOMEPLUS_LIST:142 → `AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')`
- VW_PDA_WID_HOMEPLUS_LIST:257 → `AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` (UNION ALL 쿼리)
- 오늘 날짜 이후의 출하요청 건만 조회하는 필터 조건

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음
- 로컬 DB 저장/조회 경유만 존재

#### 4. DDL 사용: X
- JSP INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW WHERE 조건으로 필수**
- 오늘 날짜 이후 출하요청 건만 필터링하는 핵심 조건
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_HOMEPLUS_LIST (VIEW) | 142, 257 | **WHERE 조건 - 제거 불가** |
| search_shipment_homeplus.jsp | 55, 92 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | temp[12] | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EMARTLOGIS_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **EMARTLOGIS_NAME 조회를 위한 서브쿼리에 사용**
- VW_PDA_WID_HOMEPLUS_LIST:123 → `EB.EMARTLOGIS_CODE` (서브쿼리 내 SELECT)
- VW_PDA_WID_HOMEPLUS_LIST:124 → `WHERE BE.EMARTITEM_CODE = EB.EMARTLOGIS_CODE` (서브쿼리 조건)
- VW_PDA_WID_HOMEPLUS_LIST:239-240 → 동일 (UNION ALL 쿼리)
- VW_PDA_WID_HOMEPLUS_LIST:103, 218 → `EO.STORECODE AS EMARTLOGIS_CODE` (SELECT 별칭)

#### 3. 안드로이드 소스 사용: O
- **바코드 생성에 핵심적으로 사용**
- ShipmentActivity.java:1880-1881 → `si.getEMARTLOGIS_CODE().substring(0, 6)` (바코드 앞 6자리)
- ShipmentActivity.java:1896-1897 → 동일
- ShipmentActivity.java:1913-1914 → 동일
- ShipmentActivity.java:1929-1930 → 동일
- ShipmentActivity.java:1946-1947 → 동일
- ShipmentActivity.java:1962-1963 → 동일
- ShipmentActivity.java:1979-1980 → `si.getEMARTLOGIS_CODE().toString()` (전체 코드)
- ShipmentActivity.java:1992-1993 → `si.getEMARTLOGIS_CODE()` (전체 코드)
- ShipmentActivity.java:2008-2009 → 동일
- ShipmentActivity.java:2049-2050 → 동일
- **바코드 생성 시 pBarcode2, pBarcodeStr2 구성에 필수**

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성의 핵심 구성요소**
- 바코드 앞 6자리 또는 전체 코드로 사용
- BARCODE_TYPE에 따라 다른 형식으로 바코드 구성
- **제거 시 바코드 생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_HOMEPLUS_LIST (VIEW) | 123-124, 239-240 | **서브쿼리 조건 - EMARTLOGIS_NAME 조회 불가** |
| search_shipment_homeplus.jsp | 73, 98 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 1880-2050 (다수) | **바코드 생성 로직 전면 수정 필요** |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### HOMPLUS_STORE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **JSP ORDER BY 조건으로 사용**
- search_shipment_homeplus.jsp:77 → `ORDER BY HOMPLUS_STORE_CODE ASC, PACKER_PRODUCT_CODE ASC, ITEM_NAME ASC, EOI_ID ASC`
- VW_PDA_WID_HOMEPLUS_LIST:105 → `EO.STORECODE AS HOMPLUS_STORE_CODE` (해외 매입 쿼리)
- VW_PDA_WID_HOMEPLUS_LIST:220 → `EO.STORECODE AS HOMPLUS_STORE_CODE` (국내 매입 쿼리)
- **정렬 순서의 첫 번째 기준**

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **JSP ORDER BY 첫 번째 정렬 기준**
- 홈플러스 매장 코드별로 데이터 정렬
- **제거 시 정렬 순서 변경**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 77 | **ORDER BY에서 제거 시 정렬 순서 변경** |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EMARTLOGIS_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용 (DECODE로 NULL 처리)
- VW_PDA_WID_HOMEPLUS_LIST:104 → `DECODE(EO.EMARTLOGIS_NAME,NULL,'정보없음',EO.EMARTLOGIS_NAME) AS EMARTLOGIS_NAME`
- VW_PDA_WID_HOMEPLUS_LIST:124 → 서브쿼리에서 `EMRTITEM_NAME`을 `EMARTLOGIS_NAME`으로 별칭
- VW_PDA_WID_HOMEPLUS_LIST:219, 240 → 동일 (UNION ALL 쿼리)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: X
- **모두 주석 처리되어 실제 사용 없음**
- ShipmentActivity.java:2161-2167 → 주석 처리된 코드 (`/*...*/`)
  ```java
  /*if (si.EMARTLOGIS_NAME.length() > 14) {
      byteStream.write(WoosimCmd.getTTFcode(35, 35, si.EMARTLOGIS_NAME));
  } else {
      byteStream.write(WoosimCmd.getTTFcode(40, 40, si.EMARTLOGIS_NAME));
  }*/
  ```
- ShipmentActivity.java:2218 → 주석 처리된 로그

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: 없음
- 원래 라벨 인쇄 시 사용 예정이었으나 주석 처리됨
- Activity에서 어떠한 분기, 계산, 서버 전송에도 사용되지 않음
- **MSSQL 마이그레이션 시 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | 74, 98 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| ShipmentActivity.java | 2161-2167, 2218 | 주석 코드 정리 (선택) |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GR_WAREHOUSE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: X
- SELECT 컬럼으로만 사용
- VW_PDA_WID_HOMEPLUS_LIST:34 → SELECT 절에 `GR_WAREHOUSE_CODE`
- VW_PDA_WID_HOMEPLUS_LIST:102 → `WR.GR_WAREHOUSE_CODE` (해외 매입 쿼리)
- VW_PDA_WID_HOMEPLUS_LIST:217 → `WR.GR_WAREHOUSE_CODE` (국내 매입 쿼리)
- JOIN/WHERE 조건에서 사용 없음

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음
- 로컬 DB 저장/조회 경유만 존재 (가능성)

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: 없음
- 창고 코드 정보를 경유만 함
- Activity에서 어떠한 분기, 계산, 서버 전송에도 사용되지 않음
- **MSSQL 마이그레이션 시 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| search_shipment_homeplus.jsp | - | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

## 변경 이력

| 날짜       | 컬럼명  | 내용                                         | 작성자 |
| ---------- | ------- | -------------------------------------------- | ------ |
| 2025-12-09 | GI_H_ID | 분석 - **사용** 판정 (VIEW JOIN 조건으로 필수) | -      |
| 2025-12-09 | GI_D_ID | 분석 - **사용** 판정 (핵심 식별자, 서버 전송/DB WHERE/DDL INSERT) | -      |
| 2025-12-09 | EOI_ID  | 분석 - **사용** 판정 (VIEW JOIN/WHERE 조건, JSP ORDER BY) | -      |
| 2025-12-09 | ITEM_CODE | 분석 - **사용** 판정 (서버 전송, DDL WHERE, VIEW 함수 파라미터) | -      |
| 2025-12-09 | ITEM_NAME | 분석 - **사용** 판정 (UI 표시, VIEW DECODE 계산, JSP ORDER BY) | -      |
| 2025-12-09 | EMARTITEM_CODE | 분석 - **사용** 판정 (바코드 생성 핵심, VIEW JOIN 조건) | -      |
| 2025-12-09 | EMARTITEM | 분석 - **사용** 판정 (라벨 인쇄 상품명 핵심) | -      |
| 2025-12-09 | GI_REQ_PKG | 분석 - **사용** 판정 (UI 표시, 완료 판단, VIEW WHERE 조건) | -      |
| 2025-12-09 | GI_REQ_QTY | 분석 - **사용** 판정 (UI 표시, 총 중량 계산) | -      |
| 2025-12-09 | AMOUNT | 분석 - 미사용 판정 (경유만, Activity 사용 없음) | -      |
| 2025-12-09 | GOODS_R_ID | 분석 - **사용** 판정 (VIEW JOIN 조건으로 필수) | -      |
| 2025-12-09 | GR_REF_NO | 분석 - 미사용 판정 (경유만, Activity 사용 없음) | -      |
| 2025-12-09 | GI_REQ_DATE | 분석 - **사용** 판정 (VIEW WHERE 조건으로 필수) | -      |
| 2025-12-09 | BL_NO | 분석 - **사용** 판정 (UI 스피너 표시, 바코드 검증, VIEW DECODE 계산) | -      |
| 2025-12-09 | BRAND_CODE | 분석 - **사용** 판정 (서버 전송, DDL WHERE, VIEW 함수 파라미터) | -      |
| 2025-12-09 | BRANDNAME | 분석 - 미사용 판정 (경유만, Activity 사용 없음) | -      |
| 2025-12-09 | CLIENT_CODE | 분석 - **사용** 판정 (DB 조회 파라미터, VIEW 함수 파라미터) | -      |
| 2025-12-09 | CLIENTNAME | 분석 - **사용** 판정 (UI 리스트 표시, 라벨 인쇄 매장명) | -      |
| 2025-12-09 | CENTERNAME | 분석 - **사용** 판정 (라벨 인쇄, 바코드 타입 분기, VIEW CLIENTNAME 계산) | -      |
| 2025-12-09 | ITEM_SPEC | 분석 - **사용** 판정 (라벨 인쇄 - 상품명과 함께 스펙 표시) | -      |
| 2025-12-09 | CT_CODE | 분석 - **사용** 판정 (라벨 인쇄 - 원산지 표시) | -      |
| 2025-12-09 | PACKER_CODE | 분석 - **사용** 판정 (킬코이 패커 분기, VIEW 함수/서브쿼리 파라미터) | -      |
| 2025-12-09 | IMPORT_ID_NO | 분석 - **사용** 판정 (바코드 생성 핵심, 리스트 표시, VIEW BL_NO DECODE) | -      |
| 2025-12-09 | PACKERNAME | 분석 - 미사용 판정 (경유만, Activity 사용 없음) | -      |
| 2025-12-09 | PACKER_PRODUCT_CODE | 분석 - **사용** 판정 (UI 표시, 서버전송, DB조회, DDL INSERT, VIEW서브쿼리/ORDER BY) | -      |
| 2025-12-09 | BARCODE_TYPE | 분석 - **사용** 판정 (바코드 생성 switch 분기 핵심 - M0/M1/M3/M4/M8/M9/E0/E1/E2/E3 등) | -      |
| 2025-12-09 | ITEM_TYPE | 분석 - **사용** 판정 (계근 방식 분기 핵심 - W/HW/S/J/B, VIEW JOIN 조건) | -      |
| 2025-12-09 | PACKWEIGHT | 분석 - **사용** 판정 (J타입 지정중량, 바코드 중량 문자열 생성) | -      |
| 2025-12-09 | BARCODEGOODS | 분석 - **사용** 판정 (바코드 검증/매칭, 작업 상품 검색) | -      |
| 2025-12-09 | STORE_IN_DATE | 분석 - **사용** 판정 (라벨 인쇄 납품일자 표시) | -      |
| 2025-12-09 | GR_WAREHOUSE_CODE | 분석 - 미사용 판정 (경유만, JSP/Activity 사용 없음) | -      |
| 2025-12-09 | EMARTLOGIS_CODE | 분석 - **사용** 판정 (바코드 생성 핵심 - 앞 6자리 또는 전체 코드, VIEW 서브쿼리) | -      |
| 2025-12-09 | EMARTLOGIS_NAME | 분석 - 미사용 판정 (Activity 주석 처리, 경유만) | -      |
| 2025-12-09 | HOMPLUS_STORE_CODE | 분석 - **사용** 판정 (JSP ORDER BY 첫 번째 정렬 기준) | -      |
