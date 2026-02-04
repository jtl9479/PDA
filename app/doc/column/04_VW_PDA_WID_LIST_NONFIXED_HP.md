# VW_PDA_WID_LIST_NONFIXED_HP 컬럼 사용 여부 분석

## VIEW 정보
- **VIEW 명**: VW_PDA_WID_LIST_NONFIXED_HP
- **스키마**: INNO
- **용도**: 홈플러스 비정량 제품 계근시 사용
- **관련 JSP**: search_homeplus_nonfixed.jsp
- **바코드 타입**: H5

---

## 컬럼 분석 결과

| 컬럼명                                         | UI 노출 | VIEW 내부 | 안드로이드 소스 | DDL 사용 | 비즈니스 영향 | 판정      |
| ------------------------------------------- | ----- | ------- | -------- | ------ | ------- | ------- |
| [GI_H_ID](#gi_h_id)                         | X     | **O**   | X        | X      | **있음**  | **사용**  |
| [GI_D_ID](#gi_d_id)                         | X     | X       | **O**    | **O**  | **있음**  | **사용**  |
| [EOI_ID](#eoi_id)                           | X     | **O**   | X        | X      | **있음**  | **사용**  |
| [ITEM_CODE](#item_code)                     | X     | **O**   | **O**    | **O**  | **있음**  | **사용**  |
| [ITEM_NAME](#item_name)                     | **O** | X       | X        | X      | **있음**  | **사용**  |
| [EMARTITEM_CODE](#emartitem_code)           | X     | **O**   | **O**    | X      | **있음**  | **사용**  |
| [EMARTITEM](#emartitem)                     | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [GI_REQ_PKG](#gi_req_pkg)                   | **O** | X       | **O**    | X      | **있음**  | **사용**  |
| [GI_REQ_QTY](#gi_req_qty)                   | **O** | X       | **O**    | X      | **있음**  | **사용**  |
| [AMOUNT](#amount)                           | X     | X       | X        | X      | 없음      | **미사용** |
| [GOODS_R_ID](#goods_r_id)                   | X     | **O**   | X        | X      | **있음**  | **사용**  |
| [GR_REF_NO](#gr_ref_no)                     | X     | X       | X        | X      | 없음      | **미사용** |
| [GI_REQ_DATE](#gi_req_date)                 | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [BL_NO](#bl_no)                             | **O** | X       | **O**    | X      | **있음**  | **사용**  |
| [BRAND_CODE](#brand_code)                   | X     | **O**   | **O**    | **O**  | **있음**  | **사용**  |
| [BRANDNAME](#brandname)                     | X     | X       | X        | X      | 없음      | **미사용** |
| [CLIENT_CODE](#client_code)                 | X     | **O**   | **O**    | X      | **있음**  | **사용**  |
| [CLIENTNAME](#clientname)                   | **O** | X       | **O**    | X      | **있음**  | **사용**  |
| [CENTERNAME](#centername)                   | **O** | X       | **O**    | X      | **있음**  | **사용**  |
| [ITEM_SPEC](#item_spec)                     | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [CT_CODE](#ct_code)                         | X     | **O**   | **O**    | X      | **있음**  | **사용**  |
| [PACKER_CODE](#packer_code)                 | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [IMPORT_ID_NO](#import_id_no)               | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [PACKERNAME](#packername)                   | X     | X       | X        | X      | 없음      | **미사용** |
| [PACKER_PRODUCT_CODE](#packer_product_code) | **O** | X       | **O**    | **O**  | **있음**  | **사용**  |
| [BARCODE_TYPE](#barcode_type)               | X     | **O**   | **O**    | X      | **있음**  | **사용**  |
| [ITEM_TYPE](#item_type)                     | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [PACKWEIGHT](#packweight)                   | X     | X       | X        | X      | 없음      | **미사용** |
| [BARCODEGOODS](#barcodegoods)               | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [STORE_IN_DATE](#store_in_date)             | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [GR_WAREHOUSE_CODE](#gr_warehouse_code)     | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [EMARTLOGIS_CODE](#emartlogis_code)         | X     | X       | X        | X      | 없음      | **미사용** |
| [EMARTLOGIS_NAME](#emartlogis_name)         | X     | X       | X        | X      | 없음      | **미사용** |
| [WH_AREA](#wh_area)                         | X     | X       | X        | X      | 없음      | **미사용** |
| [USE_NAME](#use_name)                       | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [USE_CODE](#use_code)                       | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [CT_NAME](#ct_name)                         | X     | X       | **O**    | X      | **있음**  | **사용**  |
| [STORE_NAME](#store_name)                   | X     | X       | X        | X      | 없음      | **미사용** |
| [STORE_CODE](#store_code)                   | X     | **O**   | **O**    | X      | **있음**  | **사용**  |

---

## 상세 분석

### GI_H_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED_HP:46 → `INNER JOIN W_GOODS_ID ID ON IH.GI_H_ID = ID.GI_H_ID`
- W_GOODS_IH(출고헤더)와 W_GOODS_ID(출고상세) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- 로컬 DB INSERT 경유만 존재
- DBHandler.java:705 → `si.getGI_H_ID()` (로컬 DB INSERT)
- 분기, 계산, 서버 전송 등 실제 사용 없음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- VIEW에서 출고헤더-출고상세 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 46 | **JOIN 조건 - 제거 불가** |
| search_homeplus_nonfixed.jsp | 43, 93 | SELECT/출력 제거 |
| DBHandler.java | 705 | 로컬 DB INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 26 | 상수 제거 |

---

### GI_D_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:6 → `ID.GI_D_ID`

#### 3. 안드로이드 소스 사용: O (핵심)
- **작업 위치 식별 핵심**
- ShipmentActivity.java:511 → `if (arSM.get(i).getGI_D_ID().toString().equals(gi_d_id.toString()))` (작업 위치 매칭)
- **로컬 DB 조회 조건**
- ShipmentActivity.java:877, 2953, 3135, 3227 → `selectqueryListGoodsWetInfo(mContext, arSM.get(i).getGI_D_ID(), ...)`
- **서버 전송 핵심**
- ShipmentActivity.java:3302-3304 → `qry_where = qry_where + "GI_D_ID = " + arSM.get(i).getGI_D_ID()` (서버 쿼리 조건)
- ShipmentActivity.java:3320 → `packet += list_send_info.get(i).getGI_D_ID() + "::"` (서버 전송 패킷)
- ShipmentActivity.java:3370 → `String completeStr = arSM.get(j).getGI_D_ID() + "::" + ...` (완료 전송)
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1116 → `gi.setGI_D_ID(arSM.get(current_work_position).getGI_D_ID())`
- DBHandler.java:1561, 1632, 1705 → Goodswet INSERT에 사용

#### 4. DDL 사용: O (핵심)
- **INSERT 문 사용**
- insert_goods_wet.jsp:51 → `+ ", GI_D_ID"` (W_GOODS_WET 테이블 INSERT)
- insert_goods_wet_homeplus.jsp:51 → 동일
- **UPDATE 문 WHERE 조건**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet_homeplus.jsp:109 → 동일

#### 5. 비즈니스 영향: **있음 (핵심)**
- **출고상세 식별자**: 모든 계근 작업의 기준 키
- **서버 조회/전송 필수**: WHERE 조건, 패킷 전송에 사용
- **DDL INSERT/UPDATE 핵심**: 계근 데이터 저장 시 필수
- **제거 시 계근 데이터 저장/조회/전송 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 6 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 44, 93 | SELECT/출력 제거 |
| ShipmentActivity.java | 511, 877, 1116, 2953, 3135, 3227, 3302-3304, 3320, 3370 | **핵심 로직 - 제거 불가** |
| insert_goods_wet_homeplus.jsp | 51, 109 | **DDL INSERT/UPDATE - 제거 불가** |
| DBHandler.java | 706, 1561, 1632, 1705 | **로컬 DB INSERT - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 13 | 상수 제거 |

---

### EOI_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **JOIN 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED_HP:47 → `INNER JOIN W_E_ORDER_ITEM EOI ON ID.EOI_ID = EOI.EOI_ID`
- W_GOODS_ID(출고상세)와 W_E_ORDER_ITEM(홈플러스 발주) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- 로컬 DB INSERT 경유만 존재
- 분기, 계산, 서버 전송 등 실제 사용 없음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- 출고상세와 홈플러스 발주 정보 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 47 | **JOIN 조건 - 제거 불가** |
| search_homeplus_nonfixed.jsp | 45, 93 | SELECT/출력 제거 |
| DBHandler.java | - | 로컬 DB INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### ITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O (핵심)
- **여러 컬럼 생성에 사용**
- VW_PDA_WID_LIST_NONFIXED_HP:8 → `ID.ITEM_CODE` (상품코드)
- VW_PDA_WID_LIST_NONFIXED_HP:31 → `ID.ITEM_CODE PACKER_PRODUCT_CODE` (패커상품코드로 재사용)
- VW_PDA_WID_LIST_NONFIXED_HP:35 → `ID.ITEM_CODE BARCODEGOODS` (바코드상품으로 재사용)
- **JOIN 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED_HP:50 → `INNER JOIN B_ITEM BI ON BI.ITEM_CODE = ID.ITEM_CODE`

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 스캔 매칭**
- ShipmentActivity.java → 바코드 스캔 시 ITEM_CODE로 상품 매칭
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1117 → `gi.setITEM_CODE(arSM.get(current_work_position).getITEM_CODE())`
- DBHandler.java:1562, 1633, 1706 → Goodswet INSERT에 사용

#### 4. DDL 사용: O (핵심)
- **INSERT 문 사용**
- insert_goods_wet_homeplus.jsp → W_GOODS_WET INSERT에 ITEM_CODE 포함
- **UPDATE 문 WHERE 조건**
- insert_goods_wet_homeplus.jsp:109 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW 내 다중 컬럼 생성**: PACKER_PRODUCT_CODE, BARCODEGOODS의 원천
- **VIEW JOIN 조건**: B_ITEM 테이블 연결
- **DDL INSERT/UPDATE 핵심**: 계근 데이터 저장 시 필수
- **제거 시 VIEW 구조 및 계근 데이터 저장 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 8, 31, 35, 50 | **다중 사용 및 JOIN - 제거 불가** |
| search_homeplus_nonfixed.jsp | 46, 93 | SELECT/출력 제거 |
| ShipmentActivity.java | 1117 | **Goodswet 저장 - 제거 불가** |
| insert_goods_wet_homeplus.jsp | 109 | **DDL UPDATE WHERE - 제거 불가** |
| DBHandler.java | 1562, 1633, 1706 | **로컬 DB INSERT - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### ITEM_NAME

#### 1. UI 노출: O
- **화면에 상품명 표시**
- DetailAdapter.java → 상세 화면에 상품명 표시
- ShipmentListAdapter.java → 리스트에 상품명 표시

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:10 → `EOI.ITEM_NAME ITEM_NAME` (발주서 상품명)

#### 3. 안드로이드 소스 사용: X
- UI 표시 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유 존재

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **UI 표시 필수**: 사용자가 상품을 식별하기 위한 핵심 정보
- **제거 시 사용자가 상품 식별 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 10 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 47, 94 | SELECT/출력 제거 |
| DetailAdapter.java | - | **UI 표시 - 제거 불가** |
| ShipmentListAdapter.java | - | **UI 표시 - 제거 불가** |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### EMARTITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **JOIN 조건의 원천**
- VW_PDA_WID_LIST_NONFIXED_HP:11 → `EOI.ITEM_CODE EMARTITEM_CODE`
- VW_PDA_WID_LIST_NONFIXED_HP:48 → `INNER JOIN B_EMART_BARCODE beb ON EOI.ITEM_CODE = BEB.EMARTITEM_CODE`
- EOI.ITEM_CODE가 B_EMART_BARCODE와의 JOIN 조건에 사용

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 생성에 사용**
- ShipmentActivity.java:2027 → `pBarcode2 = si.getEMARTITEM_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + si.getUSE_CODE()` (M9 바코드)
- ShipmentActivity.java:1880-2050 → 다양한 바코드 타입에서 상품코드 앞 6자리 사용

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW JOIN 조건의 원천**: B_EMART_BARCODE 연결
- **바코드 생성 핵심**: 상품코드 앞 6자리가 바코드 구성 요소
- **제거 시 VIEW JOIN 및 바코드 생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 11, 48 | **JOIN 원천 - 제거 불가** |
| search_homeplus_nonfixed.jsp | 48, 94 | SELECT/출력 제거 |
| ShipmentActivity.java | 1880-2050 | **바코드 생성 - 제거 불가** |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### EMARTITEM

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음 (ITEM_NAME과 동일 값)

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:13 → `EOI.ITEM_NAME EMARTITEM` (발주서 상품명, ITEM_NAME과 동일)

#### 3. 안드로이드 소스 사용: O
- **라벨 프린터 상품명 출력**
- ShipmentActivity.java:2371-2374 → `si.EMARTITEM` (미트센터 라벨 상품명)
- ShipmentActivity.java:2597-2602 → `si.EMARTITEM` (홈플러스 라벨 상품명)
- **바코드 문자열 조합**
- ShipmentActivity.java:2031 → `pBarcodeStr3 = si.EMARTITEM + "," + si.getUSE_NAME()` (M9)
- ShipmentActivity.java:2265 → `belowBarcodeString = si.EMARTITEM + "," + si.getUSE_NAME()` (M9 라벨)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 프린터 상품명 출력**: 여러 라벨 타입에서 상품명 표시
- **제거 시 라벨에 상품명 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 13 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 49, 94 | SELECT/출력 제거 |
| ShipmentActivity.java | 2031, 2265, 2371-2374, 2597-2602 | **라벨 출력 - 제거 불가** |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### GI_REQ_PKG

#### 1. UI 노출: O
- **화면에 출하요청수량 표시**
- DetailAdapter.java → 상세 화면에 요청수량 표시
- ShipmentListAdapter.java → 리스트에 요청수량 표시

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:14 → `ID.GI_REQ_PKG`

#### 3. 안드로이드 소스 사용: O
- **작업 완료 체크에 사용**
- ShipmentActivity.java → 요청수량과 작업수량 비교하여 완료 여부 판단
- **진행률 계산**
- 요청수량 대비 처리수량으로 진행률 표시

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **UI 표시 필수**: 요청수량 표시
- **작업 완료 체크**: 요청수량 달성 여부 판단
- **제거 시 작업 완료 판단 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 14 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 50, 95 | SELECT/출력 제거 |
| ShipmentActivity.java | - | **작업 완료 체크 - 제거 불가** |
| DetailAdapter.java | - | **UI 표시 - 제거 불가** |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### GI_REQ_QTY

#### 1. UI 노출: O
- **화면에 출하요청중량 표시**
- DetailAdapter.java → 상세 화면에 요청중량 표시

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:15 → `ID.GI_REQ_QTY`

#### 3. 안드로이드 소스 사용: O
- **중량 비교/검증에 사용**
- ShipmentActivity.java → 요청중량과 실제중량 비교

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **UI 표시**: 요청중량 표시
- **중량 검증**: 요청중량 대비 실제중량 비교
- **제거 시 중량 검증 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 15 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 51, 95 | SELECT/출력 제거 |
| ShipmentActivity.java | - | **중량 검증 - 제거 불가** |
| DetailAdapter.java | - | **UI 표시 - 제거 불가** |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### AMOUNT

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:16 → `ID.AMOUNT`

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유만 존재

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 16 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 52, 95 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GOODS_R_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O (핵심)
- **JOIN 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED_HP:49 → `INNER JOIN W_GOODS_R R ON R.GOODS_R_ID = ID.GOODS_R_ID`
- W_GOODS_ID(출고상세)와 W_GOODS_R(입고) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- 로컬 DB INSERT 경유만 존재
- 분기, 계산, 서버 전송 등 실제 사용 없음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- 출고상세와 입고 정보(W_GOODS_R) 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 17, 49 | **JOIN 조건 - 제거 불가** |
| search_homeplus_nonfixed.jsp | 53, 96 | SELECT/출력 제거 |
| DBHandler.java | - | 로컬 DB INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GR_REF_NO

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:18 → `R.GR_REF_NO` (창고입고번호)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유만 존재

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 18 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 54, 96 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GI_REQ_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:19 → `IH.GI_REQ_DATE` (출하요청일)

#### 3. 안드로이드 소스 사용: O (핵심)
- **서버 조회 WHERE 조건으로 사용**
- ProgressDlgShipSearch.java:48 → `data = " WHERE GI_REQ_DATE = '" + Common.selectDay + "'"`
- **로컬 DB 조회 조건**
- DBHandler.java → 출하요청일 기준 조회

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **서버 조회 WHERE 조건 핵심**: 출하요청일 기준으로 데이터 필터링
- **날짜별 작업 관리**: 특정 날짜의 출하 대상만 조회
- **제거 시 날짜별 조회 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 19 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 55, 96 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 48 | **서버 조회 WHERE - 제거 불가** |
| DBHandler.java | - | **조회 조건 - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### BL_NO

#### 1. UI 노출: O
- **화면에 BL번호 표시**
- DetailAdapter.java → 상세 화면에 BL번호(수입식별번호) 표시
- ShipmentListAdapter.java → 리스트에 BL번호 표시

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:20 → `ID.BL_NO`

#### 3. 안드로이드 소스 사용: O
- **로컬 DB 조회 조건**
- ShipmentActivity.java → BL_NO 기준 로컬 DB 조회
- DBHandler.java → 계근 이력 조회 시 BL_NO 조건 사용
- **Goodswet 저장**
- ShipmentActivity.java:1120 → `gi.setBL_NO(arSM.get(current_work_position).getBL_NO())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **UI 표시**: BL번호 표시로 원산지 추적
- **로컬 DB 조회 조건**: BL 단위 작업 내역 조회
- **제거 시 BL 단위 작업 추적 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 20 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 56, 97 | SELECT/출력 제거 |
| ShipmentActivity.java | 1120 | **Goodswet 저장 - 제거 불가** |
| DetailAdapter.java | - | **UI 표시 - 제거 불가** |
| DBHandler.java | - | **조회 조건 - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### BRAND_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **BRANDNAME 생성에 사용**
- VW_PDA_WID_LIST_NONFIXED_HP:22 → `INNO.DE_COMMON('BRAND', ID.BRAND_CODE) BRANDNAME`
- DE_COMMON 함수 호출 파라미터로 사용

#### 3. 안드로이드 소스 사용: O (핵심)
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1118 → `gi.setBRAND_CODE(arSM.get(current_work_position).getBRAND_CODE())`
- DBHandler.java:1563, 1634, 1707 → Goodswet INSERT에 사용

#### 4. DDL 사용: O (핵심)
- **INSERT 문 사용**
- insert_goods_wet_homeplus.jsp:52 → `+ ", BRAND_CODE"` (W_GOODS_WET 테이블 INSERT)
- **UPDATE 문 WHERE 조건**
- insert_goods_wet_homeplus.jsp:109 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW 내 BRANDNAME 생성**: DE_COMMON 함수의 필수 파라미터
- **DDL INSERT/UPDATE 핵심**: 계근 데이터 저장 시 필수
- **UPDATE WHERE 조건**: 기존 데이터 업데이트 식별에 필수
- **제거 시 계근 데이터 저장/수정 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 21, 22 | SELECT 및 DE_COMMON 파라미터 |
| search_homeplus_nonfixed.jsp | 57, 97 | SELECT/출력 제거 |
| ShipmentActivity.java | 1118 | **Goodswet 저장 - 제거 불가** |
| insert_goods_wet_homeplus.jsp | 52, 109 | **DDL INSERT/UPDATE - 제거 불가** |
| DBHandler.java | 1563, 1634, 1707 | **로컬 DB INSERT - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### BRANDNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (DE_COMMON 결과)
- VW_PDA_WID_LIST_NONFIXED_HP:22 → `INNO.DE_COMMON('BRAND', ID.BRAND_CODE) BRANDNAME`

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유만 존재

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **단순 경유만 - 제거 가능**
- **참고**: BRAND_CODE는 필수이나 BRANDNAME은 미사용

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 22 | SELECT 컬럼에서 제거 (DE_COMMON 호출 제거) |
| search_homeplus_nonfixed.jsp | 58, 97 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CLIENT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **SELECT 컬럼**
- VW_PDA_WID_LIST_NONFIXED_HP:23 → `IH.CLIENT_CODE`
- W_GOODS_IH(출고헤더) 테이블에서 출고업체코드 조회

#### 3. 안드로이드 소스 사용: O (핵심)
- **로컬 DB 조회 조건**
- ShipmentActivity.java:2953 → `row = DBHandler.selectqueryListGoodsWetInfo(mContext, arSM.get(i).getGI_D_ID(), arSM.get(i).getPACKER_PRODUCT_CODE(), arSM.get(i).getCLIENT_CODE())`
- ShipmentActivity.java:3135, 3938 → 동일 패턴
- **계근 데이터 조회 시 WHERE 조건으로 사용**

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음
- PACKER_CLIENT_CODE는 별도 컬럼 (바코드 정보에서 가져옴)

#### 5. 비즈니스 영향: **있음 (핵심)**
- **로컬 DB 조회 조건**: GI_D_ID, PACKER_PRODUCT_CODE와 함께 계근 데이터 식별
- **제거 시 계근 데이터 조회 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 23 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 59, 97 | SELECT/출력 제거 |
| ShipmentActivity.java | 2953, 3135, 3938 | **DB 조회 조건 - 제거 불가** |
| DBHandler.java | 51, 127, 182, 248, 294, 352, 400, 680 | **스키마/조회 조건 - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 42 | 상수 제거 |

---

### CLIENTNAME

#### 1. UI 노출: O
- **화면에 출고업체명 표시**
- ShipmentListAdapter.java:141 → `holder.position.setText(arSrc.get(pos).getCLIENTNAME())`
- 리스트에서 출고업체명 표시

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:24 → `EOI.STORE_NAME CLIENTNAME` (발주서의 점포명)

#### 3. 안드로이드 소스 사용: O (핵심)
- **라벨 프린터 분기 조건**
- ShipmentActivity.java:2055-2064 → CLIENTNAME 기준으로 pointName 분기
  - `if (si.CLIENTNAME.contains("이마트"))` → "이마트" 분리
  - `else if (si.CLIENTNAME.contains("신세계백화점"))` → "백화점" 분리
  - `else if (si.CLIENTNAME.contains("EVERY"))` → "EVERY" 분리
  - `else if (si.CLIENTNAME.contains("E/T"))` → "E/T" 분리
- **라벨 출력에 사용**
- ShipmentActivity.java:2553, 2771 → `pointName = si.CLIENTNAME.toString()`
- **목록 표시에 사용**
- ShipmentActivity.java:3020 → `list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 표시 필수**: 출고업체명 표시로 작업 대상 식별
- **라벨 프린터 분기 조건**: 업체명에 따라 라벨 포맷 결정
- **제거 시 라벨 출력 오류 및 UI 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 24 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 60, 98 | SELECT/출력 제거 |
| ShipmentListAdapter.java | 141 | **UI 표시 - 제거 불가** |
| ShipmentActivity.java | 2055-2064, 2553, 2771, 3020 | **라벨 분기/출력 - 제거 불가** |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |

---

### CENTERNAME

#### 1. UI 노출: O (간접)
- 로그 출력에서 센터명 표시
- ShipmentActivity.java:1684, 2517, 2642 → `Log.d(TAG, "센터명 : '" + si.CENTERNAME + "'...`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:25 → `DE_COMMON('HOMEPLUS_STORE_CODE',EOI.CENTER_CODE) CENTERNAME`

#### 3. 안드로이드 소스 사용: O (핵심)
- **로컬 DB 조회 WHERE 조건**
- DBHandler.java:152 → `WHERE CENTERNAME = '" + center_name + "'`
- DBHandler.java:370 → 동일
- **라벨 프린터 분기 조건**
- ShipmentActivity.java:367 → `if(arSM.get(current_work_position).getCENTERNAME().contains("TRD") || ...getCENTERNAME().contains("WET") || ...getCENTERNAME().contains("E/T")...)`
- ShipmentActivity.java:1745 → `if (si.getCENTERNAME().contains("E/T") || si.getCENTERNAME().contains("WET") || si.getCENTERNAME().contains("TRD"))`
- **라벨 출력**
- ShipmentActivity.java:2086-2093 → 센터명 길이에 따라 폰트 크기 조정 후 라벨 출력
  - `byteStream.write(WoosimCmd.getTTFcode(35, 35, si.CENTERNAME))` (7자 초과)
  - `byteStream.write(WoosimCmd.getTTFcode(40, 40, si.CENTERNAME))` (7자 이하)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **로컬 DB 조회 WHERE 조건**: 센터별 데이터 필터링
- **라벨 프린터 분기 조건**: TRD/WET/E/T 센터 구분으로 라벨 포맷 결정
- **라벨 출력**: 센터명 직접 인쇄
- **제거 시 센터별 조회/라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 25 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 61, 98 | SELECT/출력 제거 |
| ShipmentActivity.java | 367, 1745, 2086-2093 | **분기/라벨 출력 - 제거 불가** |
| DBHandler.java | 53, 129, 152, 184, 250, 296, 354, 370 | **스키마/WHERE 조건 - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 44 | 상수 제거 |

---

### ITEM_SPEC

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:26 → `R.ITEM_SPEC` (스펙 - 냉장/냉동 구분)

#### 3. 안드로이드 소스 사용: O
- **라벨 프린터 출력**
- ShipmentActivity.java:1639-1641 → 상품명과 함께 스펙 출력
  - `byteStream.write(WoosimCmd.getTTFcode(35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC))` (길면 35pt)
  - `byteStream.write(WoosimCmd.getTTFcode(40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC))` (짧으면 40pt)
- **로그 출력**
- ShipmentActivity.java:1643 → `Log.i(TAG, "상품명 / 냉장냉동 : " + si.EMARTITEM + " / " + si.ITEM_SPEC)`

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 출력**: 상품명과 함께 냉장/냉동 스펙 표시
- **제거 시 라벨에 스펙 정보 누락**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 26 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 62, 98 | SELECT/출력 제거 |
| ShipmentActivity.java | 1639-1643 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 54, 130, 185, 251, 297, 355, 403, 683, 724 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 25, 239-244 | 필드, getter/setter 제거 |
| DBInfo.java | 45 | 상수 제거 |

---

### CT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **CT_NAME 생성에 사용 (서브쿼리)**
- VW_PDA_WID_LIST_NONFIXED_HP:43 → `(SELECT BC.CT_NAME FROM B_COUNTRY bc WHERE BC.CT_CODE = R.CT_CODE) AS CT_NAME`
- 원산지 코드로 원산지명 조회

#### 3. 안드로이드 소스 사용: O
- **라벨 프린터 출력**
- ShipmentActivity.java:2609 → `byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(si.getCT_CODE())))`
- 홈플러스 라벨에 원산지 코드 출력

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW 내부 CT_NAME 생성**: 원산지명 조회에 필수
- **라벨 출력**: 원산지 코드 직접 인쇄
- **제거 시 원산지 정보 누락**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 27, 43 | SELECT 및 CT_NAME 서브쿼리 파라미터 |
| search_homeplus_nonfixed.jsp | 63, 99 | SELECT/출력 제거 |
| ShipmentActivity.java | 2609 | **라벨 출력 - 제거 불가** |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 26, 247-252 | 필드, getter/setter 제거 |
| DBInfo.java | 46 | 상수 제거 |

---

### PACKER_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 고정값으로 존재
- VW_PDA_WID_LIST_NONFIXED_HP:28 → `'IN67677' PACKER_CODE` (하이랜드이노베이션 고정)
- **참고**: 홈플러스 비정량은 패커가 항상 '하이랜드이노베이션'으로 고정

#### 3. 안드로이드 소스 사용: O (핵심)
- **분기 조건으로 사용**
- ShipmentActivity.java:347 → `if (arSM.get(current_work_position).getPACKER_CODE().equals("30228") && arSM.get(current_work_position).getSTORE_CODE().equals("9231"))`
- ShipmentActivity.java:793, 883, 1699 → 킬코이 제품(30228) + 미트센터(9231) 조합 체크
- **로그 출력**
- ShipmentActivity.java:344 → `Log.i(TAG, "=====================패커코드 체크==================" + arSM.get(current_work_position).getPACKER_CODE())`
- ShipmentActivity.java:1693 → `Log.d(TAG, "패커정보 : " + si.getPACKER_CODE())`
- **참고**: 홈플러스 비정량(H5)은 PACKER_CODE가 'IN67677' 고정이므로 위 분기는 해당 없음

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **분기 조건**: 특정 패커(킬코이) 제품 처리 분기에 사용
- **홈플러스 비정량에서는 고정값이므로 분기 미해당**
- 하지만 공통 구조이므로 유지 필요

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 28 | SELECT 컬럼에서 제거 (고정값) |
| search_homeplus_nonfixed.jsp | 65, 99 | SELECT/출력 제거 |
| ShipmentActivity.java | 344, 347, 793, 883, 1693, 1699 | **분기 조건 - 공통 구조상 유지** |
| DBHandler.java | 57, 133, 188, 254, 300, 358, 406, 686 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 28, 263-268 | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### IMPORT_ID_NO

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:29 → `R.IMPORT_ID_NO` (수입식별번호)

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 생성 핵심 요소**
- ShipmentActivity.java:1877-2050 → 다양한 바코드 타입에서 수입식별번호 사용
  - `pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO()`
  - M8, M9 등 바코드 구성에 필수
- **라벨 출력**
- ShipmentActivity.java:2612 → `byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(print_weight_double) + "/"+si.getIMPORT_ID_NO().substring(8, 12)))`
- ShipmentActivity.java:2866 → `"이력(묶음)번호 : " + si.getIMPORT_ID_NO()`
- **목록 표시**
- ShipmentActivity.java:3020 → `list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO())`

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성 필수**: 이마트/홈플러스 바코드 구성 요소
- **라벨 출력**: 이력번호 표시
- **제거 시 바코드 생성 및 라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 29 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 64, 99 | SELECT/출력 제거 |
| ShipmentActivity.java | 1877-2050, 2612, 2866, 3020 | **바코드 생성/라벨 출력 - 제거 불가** |
| DBHandler.java | 56, 132, 187, 253, 299, 357, 405, 685 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 47 | 상수 제거 |

---

### PACKERNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 고정값으로 존재
- VW_PDA_WID_LIST_NONFIXED_HP:30 → `'하이랜드이노베이션' PACKERNAME` (고정)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유만 존재
- ProgressDlgShipSearch.java:207 → 파싱만 수행

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **홈플러스 비정량에서는 고정값 '하이랜드이노베이션'**
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 30 | SELECT 컬럼에서 제거 (고정값) |
| search_homeplus_nonfixed.jsp | 66, 100 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 207 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 58, 134, 189, 255, 301, 359, 407, 687, 728 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 29, 271-276 | 필드, getter/setter 제거 |
| DBInfo.java | 49 | 상수 제거 |

---

### PACKER_PRODUCT_CODE

#### 1. UI 노출: O
- **화면에 패커상품코드 표시**
- DetailAdapter.java:180 → `holder.ppcode.setText(arSrc.get(pos).getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:1574, 3172 → `edit_product_code.setText(arSM.get(...).getPACKER_PRODUCT_CODE())`

#### 2. VIEW 내부 사용: X
- VIEW에서 ITEM_CODE 값을 재사용
- VW_PDA_WID_LIST_NONFIXED_HP:31 → `ID.ITEM_CODE PACKER_PRODUCT_CODE`
- **참고**: 홈플러스 비정량에서는 ITEM_CODE가 곧 PACKER_PRODUCT_CODE

#### 3. 안드로이드 소스 사용: O (핵심)
- **로컬 DB 조회 조건**
- ShipmentActivity.java:877 → `selectqueryListGoodsWetInfo(..., arSM.get(current_work_position).getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:2953, 3135, 3227, 3938 → 동일 패턴
- **Goodswet 저장**
- ShipmentActivity.java:1119 → `gi.setPACKER_PRODUCT_CODE(arSM.get(current_work_position).getPACKER_PRODUCT_CODE())`
- **바코드 스캔 매칭**
- ShipmentActivity.java:1342-1443 → PACKER_PRODUCT_CODE로 상품 검색
- **서버 전송**
- ShipmentActivity.java:3323 → `packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::"`

#### 4. DDL 사용: O (핵심)
- **INSERT 문 사용**
- insert_goods_wet_homeplus.jsp:54 → `+ ", PACKER_PRODUCT_CODE"` (W_GOODS_WET INSERT)

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 표시**: 상품코드 표시
- **로컬 DB 조회 조건**: 계근 데이터 식별
- **DDL INSERT**: 계근 데이터 저장
- **서버 전송**: 패킷 구성 요소
- **제거 시 계근 데이터 저장/조회/전송 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 31 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 67, 100 | SELECT/출력 제거 |
| DetailAdapter.java | 180 | **UI 표시 - 제거 불가** |
| ShipmentActivity.java | 877, 1119, 1342-1443, 1574, 2953, 3135, 3172, 3227, 3323, 3938 | **핵심 로직 - 제거 불가** |
| insert_goods_wet_homeplus.jsp | 54 | **DDL INSERT - 제거 불가** |
| DBHandler.java | - | **스키마/조회 조건 - 제거 불가** |
| Shipments_Info.java | 30, 279-284 | 필드, getter/setter 제거 |
| DBInfo.java | 16 | 상수 제거 |

---

### BARCODE_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **WHERE 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED_HP:53 → `AND BEB.BARCODE_TYPE = 'H5'`
- **홈플러스 비정량 제품만 필터링**
- **SELECT 컬럼**
- VW_PDA_WID_LIST_NONFIXED_HP:32 → `BEB.BARCODE_TYPE`

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 생성 분기 핵심**
- ShipmentActivity.java:1865 → `switch (si.getBARCODE_TYPE())`
- 바코드 타입(M0, M1, M3, M4, M8, M9, E0, E1, E2, E3, H5 등)에 따라 바코드 생성 로직 분기
- **라벨 출력 분기**
- ShipmentActivity.java:2098-2204 → BARCODE_TYPE별 라벨 포맷 결정
  - M3, M4: 특정 포맷
  - M9: 다른 포맷
  - M0, E0, E1, M8: 또 다른 포맷
- **계근 모드 분기**
- ShipmentActivity.java:833, 1735, 1783 → M3, M4 타입 체크

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW WHERE 조건**: H5 타입만 조회
- **바코드 생성 분기 핵심**: 타입별 바코드 구성 로직
- **라벨 출력 분기**: 타입별 라벨 포맷
- **제거 시 바코드/라벨 생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 32, 53 | SELECT 및 **WHERE 조건 - 제거 불가** |
| search_homeplus_nonfixed.jsp | 68, 100 | SELECT/출력 제거 |
| ShipmentActivity.java | 833, 1735, 1783, 1865, 2098-2204 | **바코드/라벨 분기 - 제거 불가** |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### ITEM_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:33 → `BEB.ITEM_TYPE`
- B_EMART_BARCODE 테이블에서 상품타입 조회

#### 3. 안드로이드 소스 사용: O (핵심)
- **중량 입력 방식 분기 핵심**
- ShipmentActivity.java:917 → `if (arSM.get(current_work_position).getITEM_TYPE().equals("W") || ...getITEM_TYPE().equals("HW"))`
  - **W**: 바코드 계근 (원료육)
  - **HW**: 하이랜드 원료육 (비정량)
  - **S**: 정량 제품
  - **J**: 지정 중량 입력
  - **B**: 비정량 제품
- **중량 처리 분기**
- ShipmentActivity.java:1829 → W/HW 타입은 소수점 처리, J 타입은 고정 중량
- ShipmentActivity.java:2589 → B 타입 별도 처리
- **로그 출력**
- ShipmentActivity.java:916, 1835, 1842, 2682, 2688 → ITEM_TYPE 로그

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **중량 입력 방식 결정**: W/HW(계근), J(고정), B(비정량), S(정량)
- **소수점 처리 여부 결정**: W/HW는 소수점 처리
- **제거 시 중량 입력 방식 결정 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 33 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 69, 101 | SELECT/출력 제거 |
| ShipmentActivity.java | 916-1045, 1797, 1829-1842, 2589, 2676-2688 | **중량 처리 분기 - 제거 불가** |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 52 | 상수 제거 |

---

### PACKWEIGHT

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 NULL 고정값으로 존재
- VW_PDA_WID_LIST_NONFIXED_HP:34 → `NULL AS PACKWEIGHT`
- **홈플러스 비정량에서는 항상 NULL**

#### 3. 안드로이드 소스 사용: X (조건부)
- **ITEM_TYPE이 'J'인 경우에만 사용**
- ShipmentActivity.java:1026 → `item_weight = arSM.get(current_work_position).getPACKWEIGHT()` (J 타입)
- ShipmentActivity.java:1838-1839, 2685 → J 타입 중량 설정
- **그러나 홈플러스 비정량(H5)에서는 PACKWEIGHT가 NULL이므로 미사용**
- 단순 파싱/저장만 수행

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음 (홈플러스 비정량 한정)**
- **VIEW에서 NULL 고정**: 홈플러스 비정량은 팩중량 개념 없음
- **ITEM_TYPE='J' 아니므로 분기 미해당**
- **단순 경유만 - 제거 가능**
- **참고**: 다른 VIEW(이마트 등)에서는 사용될 수 있음

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 34 | SELECT 컬럼에서 제거 (NULL 고정값) |
| search_homeplus_nonfixed.jsp | 70, 101 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 211 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 62, 138, 193, 259, 305, 363, 411, 691, 732 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 33, 303-308 | 필드, getter/setter 제거 |
| DBInfo.java | 53 | 상수 제거 |

---

### BARCODEGOODS

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 ITEM_CODE 값을 재사용
- VW_PDA_WID_LIST_NONFIXED_HP:35 → `ID.ITEM_CODE BARCODEGOODS`
- **참고**: 홈플러스 비정량에서는 ITEM_CODE가 곧 BARCODEGOODS

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 스캔 매칭**
- ShipmentActivity.java:1335-1349 → 바코드 스캔 시 BARCODEGOODS로 상품 매칭
  - `String bg = bi.getBARCODEGOODS()`
  - `temp_bg.equals(bg)` 비교로 상품 식별
- ShipmentActivity.java:1419-1430 → 동일 패턴
- **작업 위치 찾기**
- ShipmentActivity.java:3014 → `work_ppcode = find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type)`
- **로컬 DB 조회 WHERE 조건**
- DBHandler.java:266 → `WHERE BARCODEGOODS = '" + barcodegoods + "'`

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 스캔 매칭 핵심**: 스캔한 바코드로 상품 식별
- **로컬 DB 조회 조건**: BARCODEGOODS로 데이터 필터링
- **제거 시 바코드 스캔으로 상품 찾기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 35 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 71, 101 | SELECT/출력 제거 |
| ShipmentActivity.java | 1335-1349, 1419-1430, 3014 | **바코드 매칭 - 제거 불가** |
| DBHandler.java | 63, 139, 194, 260, 266, 306 | **스키마/WHERE 조건 - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### STORE_IN_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:36 → `EOI.STORE_IN_DATE` (납품일자)

#### 3. 안드로이드 소스 사용: O (핵심)
- **라벨 출력 핵심**
- ShipmentActivity.java:2283-2284 → 납품일자 포맷팅 후 라벨 출력
  - `String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일"`
- ShipmentActivity.java:2300-2301, 2310-2311, 2397-2398, 2474-2475, 2615-2616 → 동일 패턴
- **로그 출력**
- `Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE())`

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **라벨 출력 필수**: 납품일자를 라벨에 인쇄
- **법적 요구사항**: 식품 라벨에 납품일자 표시 필수
- **제거 시 라벨에 납품일자 누락**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 36 | SELECT 컬럼에서 제거 |
| search_homeplus_nonfixed.jsp | 72, 101 | SELECT/출력 제거 |
| ShipmentActivity.java | 2283-2284, 2300-2301, 2310-2311, 2397-2398, 2474-2475, 2615-2616 | **라벨 출력 - 제거 불가** |
| ProgressDlgShipSearch.java | 213 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 64, 140, 195 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 35, 75-77 | 필드, getter/setter 제거 |
| DBInfo.java | 57 | 상수 제거 |

---

### GR_WAREHOUSE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED_HP:37 → `R.GR_WAREHOUSE_CODE` (창고코드)
- **참고**: JSP에서 SELECT하지 않음 (search_homeplus_nonfixed.jsp에서 제외)

#### 3. 안드로이드 소스 사용: O (핵심)
- **서버 조회 WHERE 조건 핵심**
- ProgressDlgShipSearch.java:52-120 → 창고 구분에 따라 WHERE 조건 추가
  - `data += " AND GR_WAREHOUSE_CODE = 'IN10273'"` (창고1)
  - `data += " AND GR_WAREHOUSE_CODE = 'IN60464'"` (창고2)
  - `data += " AND GR_WAREHOUSE_CODE = '4001'"` (창고3)
  - `data += " AND GR_WAREHOUSE_CODE = '4004'"` (창고4)
  - `data += " AND GR_WAREHOUSE_CODE = 'IN63279'"` (창고5)
- **창고별 데이터 분리 조회에 필수**

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **서버 조회 WHERE 조건**: 창고별 데이터 필터링
- **JSP에서는 SELECT 안함**: VIEW에만 존재, 앱에서 WHERE 조건으로만 사용
- **제거 시 창고별 데이터 조회 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 37 | SELECT 컬럼 유지 (WHERE 조건용) |
| search_homeplus_nonfixed.jsp | - | **SELECT 안함** (기존 상태 유지) |
| ProgressDlgShipSearch.java | 52-120 | **서버 조회 WHERE - 제거 불가** |
| DBHandler.java | - | 스키마에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EMARTLOGIS_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 공백 고정값으로 존재
- VW_PDA_WID_LIST_NONFIXED_HP:38 → `' ' EMARTLOGIS_CODE`
- **홈플러스 비정량에서는 항상 공백**

#### 3. 안드로이드 소스 사용: X (조건부)
- **이마트 바코드 생성에 사용** (홈플러스 제외)
- ShipmentActivity.java:1880-2050 → `si.getEMARTLOGIS_CODE().substring(0, 6)` 바코드 생성
- **그러나 홈플러스 비정량(H5)에서는 공백이므로 사용 불가**
- 공백 값에 대해 substring 호출 시 오류 가능성
- **실제로 홈플러스용 라벨 로직에서는 EMARTITEM_CODE 사용**

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음 (홈플러스 비정량 한정)**
- **VIEW에서 공백 고정**: 홈플러스 비정량에서는 미사용
- **이마트 VIEW에서만 유효한 컬럼**
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 38 | SELECT 컬럼에서 제거 (공백 고정값) |
| search_homeplus_nonfixed.jsp | 73, 103 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 214 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EMARTLOGIS_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 공백 고정값으로 존재
- VW_PDA_WID_LIST_NONFIXED_HP:39 → `' ' EMARTLOGIS_NAME`
- **홈플러스 비정량에서는 항상 공백**

#### 3. 안드로이드 소스 사용: X (조건부)
- **이마트 라벨 출력에 사용** (홈플러스 제외)
- ShipmentActivity.java:2161-2166 → 주석 처리됨
  - `/*if (si.EMARTLOGIS_NAME.length() > 14) {...}*/`
- **그러나 홈플러스 비정량(H5)에서는 공백이므로 미사용**
- 단순 파싱/저장만 수행

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음 (홈플러스 비정량 한정)**
- **VIEW에서 공백 고정**: 홈플러스 비정량에서는 미사용
- **이마트 VIEW에서만 유효한 컬럼**
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 39 | SELECT 컬럼에서 제거 (공백 고정값) |
| search_homeplus_nonfixed.jsp | 74, 103 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 215 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 66, 142, 197, 263, 309, 367, 415, 695, 736 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 42, 71-73 | 필드, getter/setter 제거 |
| DBInfo.java | 59 | 상수 제거 |

---

### WH_AREA

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 공백 고정값으로 존재
- VW_PDA_WID_LIST_NONFIXED_HP:40 → `' ' WH_AREA`
- **홈플러스 비정량에서는 항상 공백**

#### 3. 안드로이드 소스 사용: X (조건부)
- **이마트 라벨 출력에 사용** (홈플러스 제외)
- ShipmentActivity.java:2325, 2407, 2484, 2869 → `whArea = si.getWH_AREA()`
- **그러나 홈플러스 비정량(H5)에서는 공백이므로 미사용**
- 단순 파싱/저장만 수행

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음 (홈플러스 비정량 한정)**
- **VIEW에서 공백 고정**: 홈플러스 비정량에서는 미사용
- **이마트 VIEW(VW_PDA_WID_LIST_NONFIXED)에서만 유효한 컬럼**
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 40 | SELECT 컬럼에서 제거 (공백 고정값) |
| search_homeplus_nonfixed.jsp | 75, 103 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 217, 225 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 68, 143, 199, 696, 737 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 44, 360-365 | 필드, getter/setter 제거 |
| DBInfo.java | 117 | 상수 제거 |

---

### USE_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 공백 고정값으로 존재
- VW_PDA_WID_LIST_NONFIXED_HP:40 → `' ' USE_NAME`
- **홈플러스 비정량에서는 항상 공백**

#### 3. 안드로이드 소스 사용: O (라벨 출력용)
- **라벨 출력 문자열에 사용**
- ShipmentActivity.java:2021 → `Log.d(TAG, "용도명 : " + si.getUSE_NAME())`
- ShipmentActivity.java:2031 → `pBarcodeStr3 = si.EMARTITEM +","+si.getUSE_NAME()` (라벨 하단 문자열)
- ShipmentActivity.java:2265 → `String belowBarcodeString = si.EMARTITEM +","+si.getUSE_NAME()` (라벨 출력)
- **로컬 DB 저장**
- ProgressDlgShipSearch.java:219, 226 → `si.setUSE_NAME(temp[33].toString())` (서버 응답 파싱)
- DBHandler.java:69, 144, 200, 697, 738 → 스키마/SELECT/INSERT에 사용

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (조건부)**
- **홈플러스 비정량(H5)에서는 공백 고정값**
- **라벨 출력에 사용**: 이마트 비정량에서 용도명 표시
- **공백이지만 라벨 구조상 필요** - 완전 제거 시 라벨 출력 오류 가능

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 40 | SELECT 컬럼 (공백 고정값) |
| search_homeplus_nonfixed.jsp | 76, 103 | SELECT/출력 |
| ShipmentActivity.java | 2021, 2031, 2265 | **라벨 출력 로직 - 제거 주의** |
| ProgressDlgShipSearch.java | 219, 226 | 파싱 |
| DBHandler.java | 69, 144, 200, 697, 738 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 45, 368-373 | 필드, getter/setter |
| DBInfo.java | 118 | 상수 |

---

### USE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 공백 고정값으로 존재
- VW_PDA_WID_LIST_NONFIXED_HP:41 → `' ' USE_CODE`
- **홈플러스 비정량에서는 항상 공백**

#### 3. 안드로이드 소스 사용: O (바코드 생성 핵심)
- **바코드 생성에 사용**
- ShipmentActivity.java:2027 → `pBarcode2 = si.getEMARTITEM_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + si.getUSE_CODE()` (바코드 데이터)
- ShipmentActivity.java:2028 → `pBarcodeStr2 = si.getEMARTITEM_CODE().substring(0, 6) + " " + si.getIMPORT_ID_NO() + " " + si.getUSE_CODE()` (바코드 문자열)
- **로컬 DB 저장**
- DBHandler.java:70, 145, 201, 698, 739 → 스키마/SELECT/INSERT에 사용

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (바코드 구성 요소)**
- **홈플러스 비정량(H5)에서는 공백 고정값**
- **바코드 생성에 사용**: 공백이지만 바코드 구조상 필요
- **제거 시 바코드 형식 오류 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 41 | SELECT 컬럼 (공백 고정값) |
| search_homeplus_nonfixed.jsp | 77, 103 | SELECT/출력 |
| ShipmentActivity.java | 2027-2028 | **바코드 생성 로직 - 제거 주의** |
| DBHandler.java | 70, 145, 201, 698, 739 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 46, 376-381 | 필드, getter/setter |
| DBInfo.java | 119 | 상수 |

---

### CT_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 서브쿼리로 원산지명 조회
- VW_PDA_WID_LIST_NONFIXED_HP:43 → `(SELECT BC.CT_NAME FROM B_COUNTRY bc WHERE BC.CT_CODE = R.CT_CODE) AS CT_NAME`
- B_COUNTRY 테이블에서 원산지명 조회

#### 3. 안드로이드 소스 사용: O (라벨 출력 핵심)
- **라벨 출력에 사용**
- ShipmentActivity.java:2258 → `String ctName = si.getCT_NAME()`
- ShipmentActivity.java:2259 → `byteStream.write(WoosimCmd.getTTFcode(25, 25, ctName))` (라벨에 원산지명 출력)
- **로컬 DB 저장**
- ProgressDlgShipSearch.java:221, 228 → `si.setCT_NAME(temp[35].toString())` (서버 응답 파싱)
- DBHandler.java:71, 146, 202, 699, 740 → 스키마/SELECT/INSERT에 사용

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (라벨 출력 핵심)**
- **원산지명 라벨 출력**: Woosim 프린터로 라벨에 출력
- **제거 시 원산지 표시 불가**
- **식품 라벨 필수 표시 항목**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 43 | SELECT 컬럼 (서브쿼리) |
| search_homeplus_nonfixed.jsp | 78, 103 | SELECT/출력 |
| ShipmentActivity.java | 2258-2259 | **라벨 출력 로직 - 제거 불가** |
| ProgressDlgShipSearch.java | 221, 228 | 파싱 |
| DBHandler.java | 71, 146, 202, 699, 740 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 47, 384-389 | 필드, getter/setter |
| DBInfo.java | 120 | 상수 |

---

### STORE_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로 존재
- VW_PDA_WID_LIST_NONFIXED_HP:44 → `EOI.STORE_NAME`
- W_E_ORDER_ITEM 테이블에서 매장명 조회

#### 3. 안드로이드 소스 사용: X
- **안드로이드 소스에서 STORE_NAME 필드 없음**
- Shipments_Info.java에 STORE_NAME 필드 미정의
- JSP SELECT 목록에서도 제외됨 (search_homeplus_nonfixed.jsp)

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에 컬럼은 존재하나 JSP/안드로이드에서 미사용**
- **완전 미사용 컬럼 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 44 | SELECT 컬럼에서 제거 |

---

### STORE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- VIEW에서 ORDER BY 조건에 사용
- VW_PDA_WID_LIST_NONFIXED_HP:45 → `EOI.STORE_CODE`
- VW_PDA_WID_LIST_NONFIXED_HP:55 → `ORDER BY BEB.EMRTITEM_NAME, EOI.STORE_CODE`
- W_E_ORDER_ITEM 테이블에서 매장코드 조회

#### 3. 안드로이드 소스 사용: O (핵심 분기 조건)
- **바코드/라벨 출력에 직접 사용**
- ShipmentActivity.java:1848-1849 → `sBarcode = si.getSTORE_CODE()` (바코드 데이터)
- ShipmentActivity.java:2105 → `storeNamePlusCode = pointName + "(" + si.getSTORE_CODE() + ")"` (라벨 문자열)
- ShipmentActivity.java:2552 → `storeCode = si.STORE_CODE.toString()` (라벨 출력)
- **핵심 분기 조건 (킬코이 미트센터)**
- ShipmentActivity.java:347 → `if (PACKER_CODE.equals("30228") && STORE_CODE.equals("9231"))` (킬코이 + 미트센터)
- ShipmentActivity.java:793 → 동일 조건으로 소비기한 검증
- ShipmentActivity.java:1699 → 소비기한 변조 출력 분기
- **이마트 바코드 타입 분기**
- ShipmentActivity.java:2349, 2429 → `si.getSTORE_CODE().equals("9231")` (M0 타입 분기)
- **로컬 DB 저장**
- ProgressDlgShipSearch.java:222, 229 → `si.setSTORE_CODE(temp[36].toString())` (서버 응답 파싱)
- DBHandler.java:72, 147, 203, 700, 741 → 스키마/SELECT/INSERT에 사용

#### 4. DDL 사용: X
- insert_goods_wet_homeplus.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드/라벨 출력 필수**: 매장코드 출력에 사용
- **핵심 분기 조건**: 킬코이(30228) + 미트센터(9231) 특수 처리
- **VIEW 정렬 조건**: 조회 결과 정렬에 사용
- **제거 시 특수 분기 로직 오류 발생**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED_HP (VIEW) | 45, 55 | SELECT 컬럼 및 ORDER BY |
| search_homeplus_nonfixed.jsp | 79, 103 | SELECT/출력 |
| ShipmentActivity.java | 345, 347, 793, 884, 1694, 1699, 1848-1849, 1858, 2105, 2349, 2429, 2552 | **핵심 분기/출력 로직 - 제거 불가** |
| ProgressDlgShipSearch.java | 222, 229 | 파싱 |
| DBHandler.java | 72, 147, 203, 700, 741 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 48, 392-397 | 필드, getter/setter |
| DBInfo.java | 121 | 상수 |