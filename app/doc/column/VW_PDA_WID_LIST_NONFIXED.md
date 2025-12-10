# VW_PDA_WID_LIST_NONFIXED 컬럼 사용 여부 분석

## VIEW 정보
- **VIEW 명**: VW_PDA_WID_LIST_NONFIXED
- **스키마**: INNO
- **용도**: 이마트 비정량 제품 계근시 사용
- **관련 JSP**: search_production_nonfixed.jsp, search_homeplus_nonfixed.jsp

---

## 컬럼 분석 결과

| 컬럼명 | UI 노출 | VIEW 내부 | 안드로이드 소스 | DDL 사용 | 비즈니스 영향 | 판정 |
| ------ | ------- | --------- | --------------- | -------- | ------------- | ---- |
| [GI_H_ID](#gi_h_id) | X | **O** | X | X | **있음** | **사용** |
| [GI_D_ID](#gi_d_id) | X | **O** | **O** | **O** | **있음** | **사용** |
| [EOI_ID](#eoi_id) | X | **O** | X | X | **있음** | **사용** |
| [ITEM_CODE](#item_code) | X | **O** | **O** | **O** | **있음** | **사용** |
| [ITEM_NAME](#item_name) | **O** | **O** | X | X | **있음** | **사용** |
| [EMARTITEM_CODE](#emartitem_code) | X | X | **O** | X | **있음** | **사용** |
| [EMARTITEM](#emartitem) | X | X | **O** | X | **있음** | **사용** |
| [GI_REQ_PKG](#gi_req_pkg) | **O** | **O** | **O** | X | **있음** | **사용** |
| [GI_REQ_QTY](#gi_req_qty) | **O** | X | **O** | X | **있음** | **사용** |
| [AMOUNT](#amount) | X | X | X | X | 없음 | **미사용** |
| [GOODS_R_ID](#goods_r_id) | X | **O** | X | X | **있음** | **사용** |
| [GR_REF_NO](#gr_ref_no) | X | X | X | X | 없음 | **미사용** |
| [GI_REQ_DATE](#gi_req_date) | X | **O** | **O** | X | **있음** | **사용** |
| [BL_NO](#bl_no) | **O** | **O** | **O** | X | **있음** | **사용** |
| [BRAND_CODE](#brand_code) | X | **O** | **O** | **O** | **있음** | **사용** |
| [BRANDNAME](#brandname) | X | X | X | X | 없음 | **미사용** |
| [CLIENT_CODE](#client_code) | X | **O** | **O** | X | **있음** | **사용** |
| [CLIENTNAME](#clientname) | **O** | **O** | **O** | X | **있음** | **사용** |
| [CENTERNAME](#centername) | **O** | **O** | **O** | X | **있음** | **사용** |
| [ITEM_SPEC](#item_spec) | **O** | X | X | X | **있음** | **사용** |
| [CT_CODE](#ct_code) | **O** | **O** | X | X | **있음** | **사용** |
| [PACKER_CODE](#packer_code) | X | X | **O** | X | **있음** | **사용** |
| [IMPORT_ID_NO](#import_id_no) | **O** | **O** | **O** | X | **있음** | **사용** |
| [PACKERNAME](#packername) | X | **O** | X | X | 없음 | **미사용** |
| [PACKER_PRODUCT_CODE](#packer_product_code) | **O** | **O** | **O** | X | **있음** | **사용** |
| [BARCODE_TYPE](#barcode_type) | X | **O** | **O** | X | **있음** | **사용** |
| [ITEM_TYPE](#item_type) | X | X | **O** | X | **있음** | **사용** |
| [PACKWEIGHT](#packweight) | X | X | **O** | X | **있음** | **사용** |
| [BARCODEGOODS](#barcodegoods) | X | **O** | **O** | X | **있음** | **사용** |
| [STORE_IN_DATE](#store_in_date) | **O** | X | X | X | **있음** | **사용** |
| [GR_WAREHOUSE_CODE](#gr_warehouse_code) | X | X | **O** | X | **있음** | **사용** |
| [EMARTLOGIS_CODE](#emartlogis_code) | X | **O** | **O** | X | **있음** | **사용** |
| [EMARTLOGIS_NAME](#emartlogis_name) | X | **O** | X | X | 없음 | **미사용** |
| [WH_AREA](#wh_area) | **O** | **O** | X | X | **있음** | **사용** |
| [USE_NAME](#use_name) | **O** | **O** | **O** | X | **있음** | **사용** |
| [USE_CODE](#use_code) | X | **O** | **O** | X | **있음** | **사용** |
| [CT_NAME](#ct_name) | **O** | **O** | X | X | **있음** | **사용** |
| [STORE_CODE](#store_code) | **O** | **O** | **O** | X | **있음** | **사용** |

---

## 상세 분석

### GI_H_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED:122 → `ON IH.GI_H_ID = ID.GI_H_ID`
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
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 122 | **JOIN 조건 - 제거 불가** |
| search_production_nonfixed.jsp | 43, 94 | SELECT/출력 제거 |
| DBHandler.java | 705 | 로컬 DB INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 26 | 상수 제거 |

---

### GI_D_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **주석 처리된 WHERE 조건에서 참조**
- VW_PDA_WID_LIST_NONFIXED:179 → `-- AND ID.GI_D_ID = 178505` (주석이지만 테스트용으로 사용)
- 출고상세 테이블(W_GOODS_ID)의 기본 키

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
- insert_goods_wet_ono.jsp:41 → 동일
- insert_goods_wet_new.jsp:55 → 동일
- **UPDATE 문 WHERE 조건**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet_homeplus.jsp:109 → 동일
- insert_goods_wet_ono.jsp:80 → 동일

#### 5. 비즈니스 영향: **있음 (핵심)**
- **출고상세 식별자**: 모든 계근 작업의 기준 키
- **서버 조회/전송 필수**: WHERE 조건, 패킷 전송에 사용
- **DDL INSERT/UPDATE 핵심**: 계근 데이터 저장 시 필수
- **제거 시 계근 데이터 저장/조회/전송 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | - | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 44, 94 | SELECT/출력 제거 |
| ShipmentActivity.java | 511, 877, 1116, 2953, 3135, 3227, 3302-3304, 3320, 3360, 3366, 3370, 3396 | **핵심 로직 - 제거 불가** |
| insert_goods_wet.jsp | 51, 105 | **DDL INSERT/UPDATE - 제거 불가** |
| DBHandler.java | 706, 1561, 1632, 1705 | **로컬 DB INSERT - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 13 | 상수 제거 |

---

### EOI_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **서브쿼리 JOIN 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED:174 → `ON ID.EOI_ID = EO.EOI_ID`
- W_GOODS_ID(출고상세)와 EO 서브쿼리(이마트 발주 정보) 연결 키
- **WHERE 조건에서 NULL 체크**
- VW_PDA_WID_LIST_NONFIXED:178 → `AND EO.EOI_ID IS NOT NULL`
- **JSP ORDER BY 조건**
- search_production_nonfixed.jsp:82 → `+ " ORDER BY EOI_ID ASC"`

#### 3. 안드로이드 소스 사용: X
- 로컬 DB INSERT 경유만 존재
- DBHandler.java:707 → `si.getEOI_ID()` (로컬 DB INSERT)
- 분기, 계산, 서버 전송 등 실제 사용 없음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건 필수**: 이마트 발주 정보 연결에 필수
- **VIEW WHERE 조건**: 유효한 발주 데이터만 필터링
- **JSP ORDER BY**: 결과 정렬에 사용
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 174, 178 | **JOIN/WHERE 조건 - 제거 불가** |
| search_production_nonfixed.jsp | 45, 82, 94 | **ORDER BY - 제거 불가** / SELECT/출력 제거 |
| DBHandler.java | 707 | 로컬 DB INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 28 | 상수 제거 |

---

## 변경 이력

| 날짜 | 컬럼명 | 내용 | 작성자 |
| ---- | ------ | ---- | ------ |
| 2025-12-09 | GI_H_ID | 분석 - **사용** 판정 (VIEW JOIN 조건으로 필수) | - |
| 2025-12-09 | GI_D_ID | 분석 - **사용** 판정 (핵심 식별자, 서버 전송/DB WHERE/DDL INSERT/UPDATE) | - |
| 2025-12-09 | EOI_ID | 분석 - **사용** 판정 (VIEW JOIN/WHERE 조건, JSP ORDER BY) | - |
| 2025-12-10 | ITEM_CODE | 분석 - **사용** 판정 (VIEW JOIN/DECODE 함수, DDL WHERE 조건, 서버 전송) | - |
| 2025-12-10 | ITEM_NAME | 분석 - **사용** 판정 (UI 화면 표시, VIEW DECODE 함수) | - |
| 2025-12-10 | EMARTITEM_CODE | 분석 - **사용** 판정 (바코드 생성 핵심, 모든 바코드 타입에 사용) | - |
| 2025-12-10 | EMARTITEM | 분석 - **사용** 판정 (라벨 프린터 상품명 출력 핵심) | - |
| 2025-12-10 | GI_REQ_PKG | 분석 - **사용** 판정 (VIEW WHERE 조건, UI 표시, 계근 완료 판단 핵심) | - |
| 2025-12-10 | GI_REQ_QTY | 분석 - **사용** 판정 (UI 표시, 총 계근요청중량 계산) | - |
| 2025-12-10 | AMOUNT | 분석 - **미사용** 판정 (단순 경유만 - 제거 가능) | - |
| 2025-12-10 | GOODS_R_ID | 분석 - **사용** 판정 (VIEW JOIN 조건으로 필수) | - |
| 2025-12-10 | GR_REF_NO | 분석 - **미사용** 판정 (단순 경유만 - 제거 가능) | - |
| 2025-12-10 | GI_REQ_DATE | 분석 - **사용** 판정 (VIEW WHERE 조건, 서버 조회 조건 핵심) | - |
| 2025-12-10 | BL_NO | 분석 - **사용** 판정 (VIEW DECODE 함수, UI 표시, BL별 조회/필터링 핵심) | - |
| 2025-12-10 | BRAND_CODE | 분석 - **사용** 판정 (VIEW 함수 인자, 서버 전송, DDL WHERE 조건) | - |
| 2025-12-10 | BRANDNAME | 분석 - **미사용** 판정 (단순 경유만 - 제거 가능) | - |
| 2025-12-10 | CLIENT_CODE | 분석 - **사용** 판정 (VIEW DECODE 함수 인자, 로컬 DB 조회 조건) | - |
| 2025-12-10 | CLIENTNAME | 분석 - **사용** 판정 (UI 화면 표시, 라벨 프린터 출력 분기) | - |
| 2025-12-10 | CENTERNAME | 분석 - **사용** 판정 (라벨 출력, 로컬 DB WHERE 조건, 소비기한 분기 핵심) | - |
| 2025-12-10 | ITEM_SPEC | 분석 - **사용** 판정 (라벨 프린터 상품명/스펙 출력) | - |
| 2025-12-10 | CT_CODE | 분석 - **사용** 판정 (VIEW 내부 CT_NAME 생성, 라벨 원산지 출력) | - |
| 2025-12-10 | PACKER_CODE | 분석 - **사용** 판정 (킬코이 미트센터 납품 분기 핵심) | - |
| 2025-12-10 | IMPORT_ID_NO | 분석 - **사용** 판정 (바코드 생성 핵심, VIEW BL_NO 생성, 라벨 출력) | - |
| 2025-12-10 | PACKERNAME | 분석 - **미사용** 판정 (단순 경유만 - 제거 가능) | - |
| 2025-12-10 | PACKER_PRODUCT_CODE | 분석 - **사용** 판정 (UI 표시, VIEW 생성, 로컬 DB 조회, 서버 전송, Goodswet 저장 핵심) | - |
| 2025-12-10 | BARCODE_TYPE | 분석 - **사용** 판정 (바코드 생성 switch 분기, 라벨 출력 분기 핵심) | - |
| 2025-12-10 | ITEM_TYPE | 분석 - **사용** 판정 (계근 방식 분기 핵심 - W/S/J/B/HW) | - |
| 2025-12-10 | PACKWEIGHT | 분석 - **사용** 판정 (J 타입 지정 중량, 라벨 중량 출력) | - |
| 2025-12-10 | BARCODEGOODS | 분석 - **사용** 판정 (바코드 스캔 매칭 핵심, VIEW에서 ITEM_CODE로 생성) | - |
| 2025-12-10 | STORE_IN_DATE | 분석 - **사용** 판정 (라벨 프린터 납품일자 출력 핵심) | - |

---

### ITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **JOIN 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED:126 → `ON ID.ITEM_CODE = BI.ITEM_CODE` (B_ITEM 테이블 연결)
- **DECODE 함수 인자로 사용**
- VW_PDA_WID_LIST_NONFIXED:52 → `DE_ITEM(ID.ITEM_CODE)` (ITEM_NAME 조회)
- VW_PDA_WID_LIST_NONFIXED:97 → `DE_ITEM(ID.ITEM_CODE)` (PACKERNAME 조회)
- **다른 컬럼으로 재사용**
- VW_PDA_WID_LIST_NONFIXED:101-102 → `ID.ITEM_CODE AS PACKER_PRODUCT_CODE`
- VW_PDA_WID_LIST_NONFIXED:109-110 → `ID.ITEM_CODE AS BARCODEGOODS`

#### 3. 안드로이드 소스 사용: O
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1127 → `gi.setITEM_CODE(arSM.get(current_work_position).getITEM_CODE())`
- **서버 전송 패킷에 포함**
- ShipmentActivity.java:3370 → `arSM.get(j).getITEM_CODE()` (완료 전송)

#### 4. DDL 사용: O
- **UPDATE 문 WHERE 조건**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet.jsp:113 → `pstmt.setString(6, splitData[10]); // ITEM_CODE`
- insert_goods_wet_homeplus.jsp:109, 117 → 동일
- insert_goods_wet_ono.jsp:80, 88 → 동일

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW JOIN 조건 필수**: B_ITEM 테이블 연결에 필수
- **VIEW DECODE 함수 필수**: 상품명, 패커명 조회에 사용
- **DDL WHERE 조건 필수**: 계근 데이터 UPDATE 시 필수
- **제거 시 VIEW 구조 변경 및 DDL 실패**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 52, 97, 101, 109, 126 | **JOIN/DECODE - 제거 불가** |
| search_production_nonfixed.jsp | 46, 94 | SELECT/출력 제거 |
| ShipmentActivity.java | 1127, 3370 | **서버 전송 - 제거 불가** |
| insert_goods_wet.jsp | 105, 113 | **DDL WHERE - 제거 불가** |
| DBHandler.java | 38, 114, 169, 708 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### ITEM_NAME

#### 1. UI 노출: O
- **화면에 상품명 표시**
- ShipmentActivity.java:1573 → `edit_product_name.setText(arSM.get(work_position).getITEM_NAME())` (상품명 표시)

#### 2. VIEW 내부 사용: O
- **DECODE 함수로 생성**
- VW_PDA_WID_LIST_NONFIXED:51-54 → `DECODE(EO.ITEM_NAME, NULL, DE_ITEM(ID.ITEM_CODE), EO.ITEM_NAME) AS ITEM_NAME`
- 이마트 상품명이 없으면 DE_ITEM 함수로 조회

#### 3. 안드로이드 소스 사용: X
- UI 표시 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유만 (DBHandler.java:709)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **UI 화면 표시 핵심**: 사용자가 작업 중인 상품명 확인
- **VIEW DECODE 함수로 생성**: VIEW 구조상 필수
- **제거 시 상품명 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 51-54 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 47, 95 | SELECT/출력 제거 |
| ShipmentActivity.java | 1573 | **UI 표시 - 제거 불가** |
| DBHandler.java | 39, 115, 170, 709 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EMARTITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:56 → `EO.ITEM_CODE AS EMARTITEM_CODE`

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 생성 핵심 (모든 바코드 타입에 사용)**
- ShipmentActivity.java:1620-1621 → `pBarcode = si.getEMARTITEM_CODE() + print_weight_str + "00" + now` (기본 바코드)
- ShipmentActivity.java:1877 → `pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + ...` (M0 바코드)
- ShipmentActivity.java:1893 → M1 바코드
- ShipmentActivity.java:1910 → M3 바코드
- ShipmentActivity.java:1926 → M4 바코드
- ShipmentActivity.java:1943 → E0 바코드
- ShipmentActivity.java:1959 → E1 바코드
- ShipmentActivity.java:1976 → E2 바코드 (`si.getEMARTITEM_CODE().toString() + si.getIMPORT_ID_NO()`)
- ShipmentActivity.java:1989 → E3 바코드 (`pBarcode = si.getEMARTITEM_CODE()`)
- ShipmentActivity.java:2005 → M8 바코드
- ShipmentActivity.java:2024-2028 → M9 바코드 (pBarcode, pBarcode2 모두 사용)
- ShipmentActivity.java:2046 → 추가 바코드 타입
- ShipmentActivity.java:2759 → 롯데 바코드 (`si.getEMARTITEM_CODE().substring(0, 6)`)
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1125 → `gi.setEMARTITEM_CODE(arSM.get(current_work_position).getEMARTITEM_CODE())`
- DBHandler.java:1570, 1641, 1714 → Goodswet INSERT에 사용

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 직접 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성의 핵심 구성요소**: 모든 바코드 타입(M0~M9, E0~E3)에서 사용
- **라벨 출력 필수**: 바코드 없이는 라벨 출력 불가
- **제거 시 바코드 생성 불가 → 라벨 출력 불가 → 계근 작업 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 56 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 48, 95 | SELECT/출력 제거 |
| ShipmentActivity.java | 1125, 1620-2759 (다수) | **바코드 생성 - 제거 불가** |
| DBHandler.java | 40, 116, 171, 710, 1570, 1641, 1714 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EMARTITEM

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 setText() 표시 없음
- 라벨 프린터 출력에만 사용

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:57 → `EO.ITEM_NAME AS EMARTITEM` (이마트 상품명)

#### 3. 안드로이드 소스 사용: O (핵심)
- **라벨 프린터 상품명 출력 핵심**
- ShipmentActivity.java:1638-1641 → 라벨 글자 크기 분기 (`si.EMARTITEM.length() > 14`)
- ShipmentActivity.java:1639 → `WoosimCmd.getTTFcode(35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC)` (작은 폰트)
- ShipmentActivity.java:1641 → `WoosimCmd.getTTFcode(40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC)` (큰 폰트)
- ShipmentActivity.java:2152-2155 → 동일 (다른 라벨 타입)
- ShipmentActivity.java:2371-2374 → 동일
- ShipmentActivity.java:2448-2451 → 동일
- **바코드 하단 문자열에 사용**
- ShipmentActivity.java:2031 → `pBarcodeStr3 = si.EMARTITEM + "," + si.getUSE_NAME()`
- ShipmentActivity.java:2265 → `String belowBarcodeString = si.EMARTITEM + "," + si.getUSE_NAME()`
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1126 → `gi.setEMARTITEM(arSM.get(current_work_position).getEMARTITEM())`
- **로그 출력**
- ShipmentActivity.java:1588, 1685, 2518 → 로그에 상품명 출력

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **라벨 프린터 상품명 출력 핵심**: 라벨에 이마트 상품명 출력
- **글자 크기 분기**: 상품명 길이에 따라 폰트 크기 조정
- **제거 시 라벨에 상품명 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 57 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 49, 95 | SELECT/출력 제거 |
| ShipmentActivity.java | 1126, 1638-2518 (다수) | **라벨 출력 - 제거 불가** |
| DBHandler.java | 41, 117, 172, 711 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GI_REQ_PKG

#### 1. UI 노출: O
- **화면에 출하요청수량/계근수량 표시**
- ShipmentListAdapter.java:142 → `holder.count.setText(arSrc.get(pos).getGI_REQ_PKG() + "/" + arSrc.get(pos).getPACKING_QTY())`
- ShipmentActivity.java:1207 → `edit_wet_count.setText(arSM.get(current_work_position).getGI_REQ_PKG() + " / " + ...)`
- ShipmentActivity.java:1570, 3797, 4018 → 동일

#### 2. VIEW 내부 사용: O
- **WHERE 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED:177 → `AND ID.GI_REQ_PKG <> 0` (출하요청수량이 0이 아닌 것만)

#### 3. 안드로이드 소스 사용: O (핵심)
- **계근 완료 판단 핵심**
- ShipmentActivity.java:862 → `if (arSM.get(current_work_position).getGI_REQ_PKG().equals(String.valueOf(arSM.get(current_work_position).getPACKING_QTY())))` (요청수량 = 계근수량이면 완료)
- ShipmentActivity.java:1104, 1244, 1478, 3043, 3247 → 동일 조건
- **센터 총 계근요청수량 계산**
- ShipmentActivity.java:3030 → `centerTotalCount += Integer.parseInt(arSM.get(i).getGI_REQ_PKG())`
- ShipmentActivity.java:3185 → 동일
- **전송 완료 판단**
- ShipmentActivity.java:3369 → `if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG()))` (전송 개수와 요청 개수 비교)
- ShipmentActivity.java:3501 → 동일

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW WHERE 조건 필수**: 출하요청이 있는 데이터만 조회
- **UI 표시 필수**: 사용자에게 요청수량/완료수량 표시
- **계근 완료 판단 핵심**: 요청수량과 계근수량 비교로 완료 여부 판단
- **제거 시 계근 완료 판단 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 59, 177 | **WHERE 조건 - 제거 불가** |
| search_production_nonfixed.jsp | 50, 96 | SELECT/출력 제거 |
| ShipmentListAdapter.java | 142 | **UI 표시 - 제거 불가** |
| ShipmentActivity.java | 772, 862, 1104, 1207, 1244, 1478, 1570, 3030, 3043, 3185, 3247, 3369, 3501, 3797, 4018 | **핵심 로직 - 제거 불가** |
| DBHandler.java | 42, 118, 173, 712 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GI_REQ_QTY

#### 1. UI 노출: O
- **화면에 출하요청중량/계근중량 표시**
- ShipmentListAdapter.java:146 → `holder.weight.setText(arSrc.get(pos).getGI_REQ_QTY() + "/" + arSrc.get(pos).getGI_QTY())`
- ShipmentActivity.java:1208 → `edit_wet_weight.setText(arSM.get(current_work_position).getGI_REQ_QTY() + " / " + ...)`
- ShipmentActivity.java:1571, 3798, 4019 → 동일

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:61 → `ID.GI_REQ_QTY` (출하요청중량)

#### 3. 안드로이드 소스 사용: O
- **센터 총 계근요청중량 계산**
- ShipmentActivity.java:3031 → `centerTotalWeight += Double.parseDouble(arSM.get(i).getGI_REQ_QTY())`
- ShipmentActivity.java:3186 → 동일

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **UI 표시 필수**: 사용자에게 요청중량/완료중량 표시
- **센터 총 계근요청중량 계산**: 센터별 총 요청중량 집계
- **제거 시 중량 표시/집계 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 61 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 51, 96 | SELECT/출력 제거 |
| ShipmentListAdapter.java | 146 | **UI 표시 - 제거 불가** |
| ShipmentActivity.java | 1208, 1571, 3031, 3186, 3798, 4019 | **중량 계산/표시 - 제거 불가** |
| DBHandler.java | 43, 119, 174, 713 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 14, 151 | 필드, getter/setter 제거 |
| DBInfo.java | 34 | 상수 제거 |

---

### AMOUNT

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:63 → `ID.AMOUNT` (출하상품금액, 단순 SELECT)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- DBHandler.java:714 → `si.getAMOUNT()` (로컬 DB INSERT만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 63 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 52, 96 | SELECT/출력에서 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 44, 120, 241, 345, 673, 714 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 159, 164 | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GOODS_R_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED:124 → `ON ID.GOODS_R_ID = WR.GOODS_R_ID`
- W_GOODS_ID(출고상세)와 W_GOODS_R(입고) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- 로컬 DB INSERT 경유만 존재
- DBHandler.java:715 → `si.getGOODS_R_ID()` (로컬 DB INSERT)
- 분기, 계산, 서버 전송 등 실제 사용 없음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- VIEW에서 출고상세-입고 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 65, 124 | **JOIN 조건 - 제거 불가** |
| search_production_nonfixed.jsp | 53, 97 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 194 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 45, 121, 176, 242, 288, 346, 394, 674, 715 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 16, 167-172 | 필드, getter/setter 제거 |
| DBInfo.java | 36 | 상수 제거 |

---

### GR_REF_NO

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:67 → `WR.GR_REF_NO` (창고입고번호, 단순 SELECT)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- ProgressDlgShipSearch.java:195 → `si.setGR_REF_NO(temp[11].toString())` (파싱만)
- DBHandler.java:716 → `si.getGR_REF_NO()` (로컬 DB INSERT만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 67 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 54, 97 | SELECT/출력에서 제거 |
| ProgressDlgShipSearch.java | 195 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 46, 122, 177, 243, 289, 347, 395, 675, 716 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 17, 175-180 | 필드, getter/setter 제거 |
| DBInfo.java | 37 | 상수 제거 |

---

### GI_REQ_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **WHERE 조건으로 사용**
- VW_PDA_WID_LIST_NONFIXED:181 → `AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` (오늘 이후 출하요청만)

#### 3. 안드로이드 소스 사용: O
- **서버 조회 WHERE 조건으로 사용**
- ProgressDlgShipSearch.java:48 → `String data = " WHERE GI_REQ_DATE = '" + Common.selectDay + "'"` (날짜별 조회)
- **로컬 DB 조회 조건**
- DBHandler.java:107 → `qry_condition = qry_condition + " AND GI_REQ_DATE = " + Common.selectDay`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW WHERE 조건 필수**: 오늘 이후 출하요청 데이터만 조회
- **서버/로컬 조회 조건 필수**: 날짜별 데이터 필터링
- **제거 시 날짜별 조회 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 69, 181 | **WHERE 조건 - 제거 불가** |
| search_production_nonfixed.jsp | 55, 97 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 48 | **서버 조회 조건 - 제거 불가** |
| DBHandler.java | 47, 107, 123, 178, 244, 290, 348, 396, 676, 717 | **로컬 DB 조건 - 제거 불가** |
| Shipments_Info.java | 18, 183-188 | 필드, getter/setter 제거 |
| DBInfo.java | 38 | 상수 제거 |

---

### BL_NO

#### 1. UI 노출: O
- **화면에 BL번호 표시**
- ShipmentListAdapter.java:149-152 → `holder.bl.setText(arSrc.get(pos).getBL_NO().substring(...))` (BL번호 뒤 4자리 표시)

#### 2. VIEW 내부 사용: O
- **DECODE 함수로 생성**
- VW_PDA_WID_LIST_NONFIXED:71-72 → `DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) BL_NO`
- 수입식별번호가 없으면 BL번호, 있으면 수입식별번호 사용

#### 3. 안드로이드 소스 사용: O (핵심)
- **BL별 필터링/조회 핵심**
- ShipmentActivity.java:772 → `if (temp_bl_no.equals(arSM.get(i).getBL_NO()) && ...)` (BL별 작업 상태 확인)
- ShipmentActivity.java:1318 → `if (BL_NO.equals(arSM.get(current_work_position).getBL_NO()))` (바코드 스캔 매칭)
- **BL 리스트 생성**
- ShipmentActivity.java:1564 → `list_bl.add(arSM.get(work_position).getBL_NO())` (BL 리스트에 추가)
- ShipmentActivity.java:3193-3199 → BL 리스트 생성/중복 체크
- ShipmentActivity.java:3215 → BL 스피너 선택 위치
- **로컬 DB 조회 조건**
- DBHandler.java:103 → `qry_condition = " AND BL_NO = '" + condition + "' "` (BL별 조회)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 표시 필수**: 사용자에게 BL번호 표시
- **VIEW DECODE 함수로 생성**: VIEW 구조상 필수
- **BL별 조회/필터링 핵심**: 작업 상태 확인, 바코드 매칭
- **제거 시 BL별 조회 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 71-72 | DECODE 함수에서 제거 |
| search_production_nonfixed.jsp | 56, 97 | SELECT/출력 제거 |
| ShipmentListAdapter.java | 149-152 | **UI 표시 - 제거 불가** |
| ShipmentActivity.java | 772, 1318, 1544, 1564, 1569, 3193-3215 | **BL 필터링 - 제거 불가** |
| DBHandler.java | 48, 103, 124, 179, 245, 291, 349, 397, 677, 718 | **로컬 DB 조건 - 제거 불가** |
| Shipments_Info.java | 19, 191-196 | 필드, getter/setter 제거 |
| DBInfo.java | 39 | 상수 제거 |

---

### BRAND_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **DE_COMMON 함수 인자로 사용**
- VW_PDA_WID_LIST_NONFIXED:75 → `DE_COMMON('BRAND', ID.BRAND_CODE) AS BRANDNAME` (브랜드명 조회)

#### 3. 안드로이드 소스 사용: O
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1128 → `gi.setBRAND_CODE(arSM.get(current_work_position).getBRAND_CODE())`
- **서버 전송 패킷에 포함**
- ShipmentActivity.java:3331 → `packet += list_send_info.get(i).getBRAND_CODE() + "::"`
- ShipmentActivity.java:3441 → 동일
- **완료 전송에 사용**
- ShipmentActivity.java:3370 → `String completeStr = ... + "::" + arSM.get(j).getBRAND_CODE() + "::"`
- ShipmentActivity.java:3502 → 동일

#### 4. DDL 사용: O
- **UPDATE 문 WHERE 조건**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet.jsp:114 → `pstmt.setString(7, splitData[11]); // BRAND_CODE`
- insert_goods_wet_homeplus.jsp:109, 118 → 동일
- insert_goods_wet_ono.jsp:80, 89 → 동일

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW 함수 인자 필수**: DE_COMMON()으로 브랜드명 조회
- **서버 전송 필수**: 계근 데이터 전송 시 포함
- **DDL WHERE 조건 필수**: 계근 데이터 UPDATE 시 필수
- **제거 시 서버 전송/DDL 실패**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 73, 75 | **함수 인자 - 제거 불가** |
| search_production_nonfixed.jsp | 57, 98 | SELECT/출력 제거 |
| ShipmentActivity.java | 1128, 3331, 3370, 3441, 3502 | **서버 전송 - 제거 불가** |
| insert_goods_wet.jsp | 105, 114 | **DDL WHERE - 제거 불가** |
| DBHandler.java | 49, 125, 180, 246, 292, 350, 398, 678, 719 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 20, 199-204 | 필드, getter/setter 제거 |
| DBInfo.java | 14 | 상수 제거 |

---

### BRANDNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:75 → `DE_COMMON('BRAND', ID.BRAND_CODE) AS BRANDNAME` (브랜드명, 단순 SELECT)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- DBHandler.java:720 → `si.getBRANDNAME()` (로컬 DB INSERT만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 75 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 58, 98 | SELECT/출력에서 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 50, 126, 181, 247, 293, 351, 399, 679, 720 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CLIENT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **DECODE 함수 인자로 사용**
- VW_PDA_WID_LIST_NONFIXED:77-86 → CLIENTNAME 생성에 사용
  ```sql
  DECODE (
     SUBSTR (EO.CENTERNAME, 1, 2),
     'CJ', DE_CLIENT2 (IH.CLIENT_CODE) || '(' || EO.STORECODE || ')',
     DE_CLIENT (IH.CLIENT_CODE))
  AS CLIENTNAME
  ```
- DE_CLIENT / DE_CLIENT2 함수 인자로 출고업체명 조회

#### 3. 안드로이드 소스 사용: O
- **로컬 DB 조회 조건으로 사용**
- ShipmentActivity.java:877 → `DBHandler.selectqueryListGoodsWetInfo(mContext, arSM.get(i).getGI_D_ID(), arSM.get(i).getCLIENT_CODE())`
- ShipmentActivity.java:2953 → 동일
- ShipmentActivity.java:3135 → 동일
- ShipmentActivity.java:3227 → 동일
- **Goodswet 조회 조건**
- DBHandler.java:1528 → `selectqueryGoodsWet(Context context, String GI_D_ID, String CLIENT_CODE)`
- DBHandler.java:1529 → `qry_where = qry_where + " AND CLIENT_CODE = '" + CLIENT_CODE + "'"`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW DECODE 함수 인자 필수**: CLIENTNAME 생성에 필수
- **로컬 DB 조회 조건 필수**: GoodsWet 조회 시 필터링
- **제거 시 VIEW CLIENTNAME 생성 불가 및 로컬 DB 조회 실패**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 77, 81, 85 | **DECODE 함수 인자 - 제거 불가** |
| search_production_nonfixed.jsp | 59, 98 | SELECT/출력 제거 |
| ShipmentActivity.java | 877, 2953, 3135, 3227 | **로컬 DB 조회 조건 - 제거 불가** |
| DBHandler.java | 51, 127, 182, 248, 294, 352, 400, 680, 721, 1528-1529 | **조회 조건 - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CLIENTNAME

#### 1. UI 노출: O
- **화면에 출고업체명 표시**
- ShipmentListAdapter.java:141 → `holder.position.setText(arSrc.get(pos).getCLIENTNAME())` (리스트에 업체명 표시)

#### 2. VIEW 내부 사용: O
- **DECODE 함수로 생성**
- VW_PDA_WID_LIST_NONFIXED:79-86 → 센터명에 따라 다른 형식으로 출력
  ```sql
  DECODE (
     SUBSTR (EO.CENTERNAME, 1, 2),
     'CJ', DE_CLIENT2 (IH.CLIENT_CODE) || '(' || EO.STORECODE || ')',
     DE_CLIENT (IH.CLIENT_CODE))
  AS CLIENTNAME
  ```

#### 3. 안드로이드 소스 사용: O (핵심)
- **라벨 프린터 출력 분기 핵심**
- ShipmentActivity.java:2055-2064 → 출고업체별 라벨 출력 분기
  ```java
  if (si.CLIENTNAME.contains("이마트")) { ... }
  else if (si.CLIENTNAME.contains("신세계백화점")) { ... }
  else if (si.CLIENTNAME.contains("EVERY") || si.CLIENTNAME.contains("E") || si.CLIENTNAME.contains("T")) { ... }
  ```
- **Position 리스트 표시**
- ShipmentActivity.java:3020 → `list_pos.add(arSM.get(i).getCLIENTNAME())`
- ShipmentActivity.java:3175 → 동일
- ShipmentActivity.java:3794 → 동일
- **로그 출력**
- ShipmentActivity.java:1586 → 로그에 업체명 출력

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 표시 필수**: 리스트에 출고업체명 표시
- **VIEW DECODE 함수로 생성**: VIEW 구조상 필수
- **라벨 출력 분기 핵심**: 출고업체별 다른 라벨 형식 출력
- **제거 시 UI 표시 불가 및 라벨 출력 분기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 79-86 | DECODE 함수에서 제거 |
| search_production_nonfixed.jsp | 60, 99 | SELECT/출력 제거 |
| ShipmentListAdapter.java | 141 | **UI 표시 - 제거 불가** |
| ShipmentActivity.java | 1586, 2055-2064, 3020, 3175, 3794 | **라벨 분기/Position - 제거 불가** |
| DBHandler.java | 52, 128, 183, 249, 295, 353, 401, 681, 722 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### CENTERNAME

#### 1. UI 노출: O
- **라벨 프린터에 센터명 출력**
- ShipmentActivity.java:2086-2093 → 센터명 길이에 따라 폰트 크기 분기
  ```java
  if (7 < si.CENTERNAME.length()) {
      byteStream.write(WoosimCmd.getTTFcode(35, 35, si.CENTERNAME));  // 작은 폰트
  } else {
      byteStream.write(WoosimCmd.getTTFcode(40, 40, si.CENTERNAME));  // 큰 폰트
  }
  ```

#### 2. VIEW 내부 사용: O
- **서브쿼리 JOIN 결과로 생성**
- VW_PDA_WID_LIST_NONFIXED:88 → `EO.CENTERNAME` (B_COMMON_CODE에서 조회)
- VW_PDA_WID_LIST_NONFIXED:80, 154-157 → EO 서브쿼리에서 센터코드로 센터명 조회
  ```sql
  INNER JOIN B_COMMON_CODE BCC
     ON BCC.MASTER_CODE = 'EMART_BRANCH_CODE'
        AND EOI.CENTER_CODE = BCC.CODE
  ```
- **DECODE 함수에서 센터명 앞 2자리 사용**
- VW_PDA_WID_LIST_NONFIXED:80 → `SUBSTR(EO.CENTERNAME, 1, 2)` (CLIENTNAME 생성 분기)

#### 3. 안드로이드 소스 사용: O (핵심)
- **로컬 DB WHERE 조건**
- DBHandler.java:152 → `WHERE CENTERNAME = '" + center_name + "'` (센터별 조회)
- DBHandler.java:370 → 동일
- **소비기한 입력 분기 핵심**
- ShipmentActivity.java:367 → `if(arSM.get(current_work_position).getCENTERNAME().contains("TRD") || ... .contains("WET") || ... .contains("E/T"))`
- ShipmentActivity.java:839 → 트레이더스/WET/E/T 센터 소비기한 검증 분기
- ShipmentActivity.java:1745 → 트레이더스 납품분 라벨 출력 분기
- **로그 출력**
- ShipmentActivity.java:1684, 2517, 2642 → 로그에 센터명 출력

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW 서브쿼리로 생성**: B_COMMON_CODE에서 센터코드로 센터명 조회
- **로컬 DB WHERE 조건 필수**: 센터별 데이터 필터링
- **소비기한 분기 핵심**: 트레이더스/WET/E/T 센터는 소비기한 입력 필수
- **라벨 출력 필수**: 센터명 라벨에 출력
- **제거 시 센터별 조회/소비기한 검증 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 80, 88 | **DECODE/서브쿼리 - 제거 불가** |
| search_production_nonfixed.jsp | 61, 99 | SELECT/출력 제거 |
| ShipmentActivity.java | 367, 839, 1684, 1745, 2086-2093, 2517, 2642 | **소비기한 분기/라벨 - 제거 불가** |
| DBHandler.java | 53, 129, 152, 184, 250, 296, 354, 370, 402, 682, 723 | **WHERE 조건 - 제거 불가** |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### ITEM_SPEC

#### 1. UI 노출: O
- **라벨 프린터에 상품명/스펙 출력**
- ShipmentActivity.java:1639 → `WoosimCmd.getTTFcode(35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC)` (작은 폰트)
- ShipmentActivity.java:1641 → `WoosimCmd.getTTFcode(40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC)` (큰 폰트)
- ShipmentActivity.java:1643 → 로그 출력 (`상품명 / 냉장냉동`)

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:90 → `WR.ITEM_SPEC` (스펙, 단순 SELECT)

#### 3. 안드로이드 소스 사용: X
- 라벨 출력 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유 (DBHandler.java:724)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 프린터 출력 필수**: 상품명과 함께 스펙(냉장/냉동) 정보 출력
- **제거 시 라벨에 스펙 정보 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 90 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 62, 99 | SELECT/출력 제거 |
| ShipmentActivity.java | 1639, 1641, 1643 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 54, 130, 185, 251, 297, 355, 403, 683, 724 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 25, 239-244 | 필드, getter/setter 제거 |
| DBInfo.java | 45 | 상수 제거 |

---

### CT_CODE

#### 1. UI 노출: O
- **라벨 프린터에 원산지 출력**
- ShipmentActivity.java:2609 → `WoosimCmd.getTTFcode(40, 40, String.valueOf(si.getCT_CODE()))` (원산지 코드 출력)

#### 2. VIEW 내부 사용: O
- **CT_NAME 생성에 사용**
- VW_PDA_WID_LIST_NONFIXED:118 → `(SELECT BC.CODE_NAME FROM B_COMMON_CODE BC WHERE BC.CODE = WR.CT_CODE AND BC.MASTER_CODE = 'HOMEPLUS_ORIGIN_CODE' AND BC.STATUS = 'Y')||'산' AS CT_NAME`
- W_GOODS_R 테이블의 CT_CODE로 B_COMMON_CODE에서 원산지명 조회

#### 3. 안드로이드 소스 사용: X
- 라벨 출력 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유 (DBHandler.java:725)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW CT_NAME 생성 필수**: CT_CODE로 원산지명(CT_NAME) 조회
- **라벨 출력 필수**: 원산지 코드 라벨에 출력
- **제거 시 VIEW CT_NAME 생성 불가 및 라벨 원산지 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 92, 118 | **CT_NAME 서브쿼리 - 제거 불가** |
| search_production_nonfixed.jsp | 63, 100 | SELECT/출력 제거 |
| ShipmentActivity.java | 2609 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 55, 131, 186, 252, 298, 356, 404, 684, 725 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 26, 247-252 | 필드, getter/setter 제거 |
| DBInfo.java | 46 | 상수 제거 |

---

### PACKER_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:94 → `'0000' AS PACKER_CODE` (비정량은 고정값 '0000')

#### 3. 안드로이드 소스 사용: O (핵심)
- **킬코이 미트센터 납품 분기 핵심**
- ShipmentActivity.java:347 → `if (arSM.get(current_work_position).getPACKER_CODE().equals("30228") && arSM.get(current_work_position).getSTORE_CODE().equals("9231"))`
- ShipmentActivity.java:793 → 동일 조건 (킬코이제품 + 이마트미트센터)
- ShipmentActivity.java:1699 → `if (si.getPACKER_CODE().equals("30228") && si.getSTORE_CODE().equals("9231"))` (소비기한 변조 출력)
- **로그 출력**
- ShipmentActivity.java:344, 883, 1693 → 패커코드 로그 출력

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **킬코이 미트센터 납품 분기 필수**: 패커코드 30228 + 스토어코드 9231 조합으로 특수 처리
- **소비기한 변조 출력**: 킬코이 미트센터 납품분은 makingdate로 소비기한 변조
- **제거 시 킬코이 미트센터 납품 분기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 94 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 65, 100 | SELECT/출력 제거 |
| ShipmentActivity.java | 344, 347, 793, 883, 1693, 1699 | **킬코이 분기 - 제거 불가** |
| DBHandler.java | 57, 133, 188, 254, 300, 358, 406, 686, 727 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 28, 263-268 | 필드, getter/setter 제거 |
| DBInfo.java | 48 | 상수 제거 |

---

### IMPORT_ID_NO

#### 1. UI 노출: O
- **라벨 프린터에 이력(묶음)번호 출력**
- ShipmentActivity.java:2612 → `si.getIMPORT_ID_NO().substring(8, 12)` (중량/식별번호 뒤 4자리)
- ShipmentActivity.java:2866 → `"이력(묶음)번호 : " + si.getIMPORT_ID_NO()` (라벨에 출력)
- **Position 리스트에 표시**
- ShipmentActivity.java:3020 → `list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO())`

#### 2. VIEW 내부 사용: O
- **BL_NO DECODE 함수에서 사용**
- VW_PDA_WID_LIST_NONFIXED:71-72 → `DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) BL_NO`
- 수입식별번호가 있으면 BL_NO 대신 수입식별번호 사용

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 생성 핵심 (모든 바코드 타입에 사용)**
- ShipmentActivity.java:1877 → M0 바코드: `... + si.getIMPORT_ID_NO()`
- ShipmentActivity.java:1910 → M3 바코드
- ShipmentActivity.java:1943 → M4 바코드
- ShipmentActivity.java:1976 → E2 바코드: `si.getEMARTITEM_CODE().toString() + si.getIMPORT_ID_NO()`
- ShipmentActivity.java:2005 → M8 바코드
- ShipmentActivity.java:2024-2028 → M9 바코드
- ShipmentActivity.java:2046 → 기타 바코드
- ShipmentActivity.java:2379-2380 → 미트센터 바코드
- ShipmentActivity.java:2764 → 롯데 바코드

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW BL_NO 생성에 사용**: DECODE 함수로 BL_NO 또는 수입식별번호 선택
- **바코드 생성 핵심**: 대부분의 바코드 타입에서 수입식별번호 포함
- **라벨 출력 필수**: 이력번호 라벨에 출력
- **제거 시 바코드 생성 불가 → 라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 71, 95 | **DECODE 함수 - 제거 불가** |
| search_production_nonfixed.jsp | 64, 100 | SELECT/출력 제거 |
| ShipmentActivity.java | 1874-2866 (다수) | **바코드/라벨 - 제거 불가** |
| DBHandler.java | 56, 132, 187, 253, 299, 357, 405, 685, 726 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 27, 255-260 | 필드, getter/setter 제거 |
| DBInfo.java | 47 | 상수 제거 |

---

### PACKERNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음

#### 2. VIEW 내부 사용: O
- **DECODE 함수로 생성**
- VW_PDA_WID_LIST_NONFIXED:96-99 → `DECODE(EO.ITEM_NAME, NULL, DE_ITEM(ID.ITEM_CODE), EO.ITEM_NAME) AS PACKERNAME`
- 비정량 VIEW에서는 이마트 상품명을 패커이름으로 사용 (패커 정보 없음)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- ProgressDlgShipSearch.java:207 → `si.setPACKERNAME(temp[23].toString())` (파싱만)
- DBHandler.java:728 → `si.getPACKERNAME()` (로컬 DB INSERT만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 96-99 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 66, 101 | SELECT/출력에서 제거 |
| ProgressDlgShipSearch.java | 207 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 58, 134, 189, 255, 301, 359, 407, 687, 728 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 29, 271-276 | 필드, getter/setter 제거 |
| DBInfo.java | 49 | 상수 제거 |

---

### PACKER_PRODUCT_CODE

#### 1. UI 노출: O
- **화면에 패커상품코드 표시**
- ShipmentActivity.java:1574 → `edit_product_code.setText(arSM.get(work_position).getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:3172 → `edit_product_code.setText(arSM.get(0).getPACKER_PRODUCT_CODE().toString())`
- ShipmentActivity.java:3796 → `detail_edit_ppcode.setText(si.getPACKER_PRODUCT_CODE())`
- **Adapter에서 표시**
- DetailAdapter.java:180 → `holder.ppcode.setText(arSrc.get(pos).getPACKER_PRODUCT_CODE())`

#### 2. VIEW 내부 사용: O
- **다른 컬럼으로 재사용**
- VW_PDA_WID_LIST_NONFIXED:101-102 → `ID.ITEM_CODE AS PACKER_PRODUCT_CODE` (비정량은 ITEM_CODE를 PACKER_PRODUCT_CODE로 사용)
- VW_PDA_WID_LIST_NONFIXED:109-110 → `ID.ITEM_CODE AS BARCODEGOODS` (동일)

#### 3. 안드로이드 소스 사용: O (핵심)
- **로컬 DB 조회 조건 핵심**
- ShipmentActivity.java:877 → `selectqueryListGoodsWetInfo(mContext, arSM.get(i).getGI_D_ID(), arSM.get(i).getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:2953, 3135, 3227 → 동일
- ShipmentActivity.java:3938 → `DBHandler.selectqueryGoodsWet(... si.getPACKER_PRODUCT_CODE(), ...)`
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1119 → `gi.setPACKER_PRODUCT_CODE(arSM.get(current_work_position).getPACKER_PRODUCT_CODE())`
- DBHandler.java:1564, 1635, 1708 → Goodswet INSERT에 사용
- **서버 전송 패킷에 포함**
- ShipmentActivity.java:3323 → `packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::"`
- ShipmentActivity.java:3433 → 동일
- **로컬 DB UPDATE 조건**
- ShipmentActivity.java:3396 → `DBHandler.updatequeryShipment(mContext, arSM.get(j).getGI_D_ID(), arSM.get(j).getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:3533 → 동일

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW에서 생성**: 비정량은 ITEM_CODE를 PACKER_PRODUCT_CODE로 사용
- **UI 표시 필수**: 패커상품코드 화면에 표시
- **로컬 DB 조회 핵심**: GoodsWet 조회 시 WHERE 조건
- **서버 전송 필수**: 계근 데이터 전송 시 포함
- **제거 시 로컬 DB 조회/서버 전송 실패**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 101-102, 109-110 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 67, 101 | SELECT/출력 제거 |
| ShipmentActivity.java | 877, 1119, 1574, 2953, 3135, 3172, 3227, 3323, 3396, 3433, 3533, 3796, 3938 | **핵심 로직 - 제거 불가** |
| DetailAdapter.java | 180 | **UI 표시 - 제거 불가** |
| DBHandler.java | 59, 135, 190, 256, 302, 360, 408, 688, 729, 1564, 1635, 1708 | **조회/INSERT - 제거 불가** |
| Shipments_Info.java | 30, 279-284 | 필드, getter/setter 제거 |
| DBInfo.java | 16 | 상수 제거 |

---

### BARCODE_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **서브쿼리 WHERE 조건**
- VW_PDA_WID_LIST_NONFIXED:162 → `AND EB.BARCODE_TYPE = 'M8'` (비정량은 M8 바코드만)
- **EO 서브쿼리에서 조회**
- VW_PDA_WID_LIST_NONFIXED:133 → `EB.BARCODE_TYPE` (B_EMART_BARCODE 테이블)

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 생성 switch 분기 핵심**
- ShipmentActivity.java:1865 → `switch (si.getBARCODE_TYPE())` (M0, M1, M3, M4, E0, E1, E2, E3, M8, M9 분기)
- ShipmentActivity.java:2732 → `switch (si.getBARCODE_TYPE())` (롯데용 L0 분기)
- **라벨 출력 분기**
- ShipmentActivity.java:833, 1735, 1783 → M3/M4 소비기한 분기
- ShipmentActivity.java:2098-2297 → 바코드 타입별 라벨 레이아웃 분기
- ShipmentActivity.java:2343 → P0 바코드 분기
- ShipmentActivity.java:2349, 2429 → M0 바코드 + 미트센터 분기
- ShipmentActivity.java:2823, 2836, 2841, 2851, 2881 → L0 (원앤원) 분기

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW WHERE 조건**: 비정량은 M8 바코드만 필터링
- **바코드 생성 switch 분기 핵심**: M0~M9, E0~E3, L0 등 바코드 타입별 생성 로직
- **라벨 출력 분기 핵심**: 바코드 타입별 라벨 레이아웃 결정
- **제거 시 바코드 생성/라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 133, 162 | **서브쿼리 WHERE - 제거 불가** |
| search_production_nonfixed.jsp | 68, 101 | SELECT/출력 제거 |
| ShipmentActivity.java | 833-2881 (다수) | **바코드/라벨 분기 - 제거 불가** |
| DBHandler.java | 60, 136, 191, 257, 303, 361, 409, 689, 730 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 31, 287-292 | 필드, getter/setter 제거 |
| DBInfo.java | 51 | 상수 제거 |

---

### ITEM_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 고정값으로 설정
- VW_PDA_WID_LIST_NONFIXED:107 → `'HW' AS ITEM_TYPE` (하이랜드원료육, PDA에서 FLOOR 처리 안 함)

#### 3. 안드로이드 소스 사용: O (핵심)
- **계근 방식 분기 핵심**
- ShipmentActivity.java:917 → `if (arSM.get(current_work_position).getITEM_TYPE().equals("W") || ... .equals("HW"))` (바코드 계근)
- ShipmentActivity.java:970 → `else if (... .getITEM_TYPE().equals("S"))` (S 타입)
- ShipmentActivity.java:1024 → `else if (... .getITEM_TYPE().equals("J"))` (지정 중량 입력)
- ShipmentActivity.java:1035 → `if (... .getITEM_TYPE().equals("B"))` (B 타입)
- **라벨 출력 계근 방식 분기**
- ShipmentActivity.java:1829 → `if (si.getITEM_TYPE().equals("W") || ... .equals("HW"))` (중량 계산)
- ShipmentActivity.java:1837 → `else if (si.getITEM_TYPE().equals("J"))` (지정 중량)
- ShipmentActivity.java:2589 → `if (si.getITEM_TYPE().equals("B"))` (B 타입)
- ShipmentActivity.java:2676 → `if (si.getITEM_TYPE().equals("W") || ... .equals("S"))` (롯데용)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **계근 방식 분기 핵심**: W/HW(바코드 계근), S, J(지정 중량), B 타입별 처리
- **VIEW에서 고정값**: 비정량은 'HW' (하이랜드원료육)
- **중량 계산 방식 결정**: FLOOR 처리 여부 등
- **제거 시 계근 방식 분기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 107 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 69, 102 | SELECT/출력 제거 |
| ShipmentActivity.java | 916-1035, 1797, 1829, 1837, 2589, 2676-2688 | **계근 분기 - 제거 불가** |
| DBHandler.java | 61, 137, 192, 258, 304, 362, 410, 690, 731 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 32, 295-300 | 필드, getter/setter 제거 |
| DBInfo.java | 52 | 상수 제거 |

---

### PACKWEIGHT

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:108 → `EO.PACKWEIGHT` (팩 중량, EO 서브쿼리에서 조회)

#### 3. 안드로이드 소스 사용: O
- **J 타입 지정 중량으로 사용**
- ShipmentActivity.java:1026 → `item_weight = arSM.get(current_work_position).getPACKWEIGHT()` (J 타입 중량)
- **라벨 출력 중량으로 사용**
- ShipmentActivity.java:1839 → `print_weight_str = si.getPACKWEIGHT()` (J 타입 라벨 중량)
- ShipmentActivity.java:2685 → `print_weight_str = si.getPACKWEIGHT()` (롯데용 J 타입)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **J 타입 지정 중량 핵심**: ITEM_TYPE이 'J'일 때 고정 중량값으로 사용
- **라벨 중량 출력**: J 타입 계근 시 라벨에 출력되는 중량값
- **제거 시 J 타입 계근 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 108 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 70, 102 | SELECT/출력 제거 |
| ShipmentActivity.java | 1026, 1838-1839, 2685 | **J 타입 중량 - 제거 불가** |
| DBHandler.java | 62, 138, 193, 259, 305, 363, 411, 691, 732 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 33, 303-308 | 필드, getter/setter 제거 |
| DBInfo.java | 53 | 상수 제거 |

---

### BARCODEGOODS

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **ITEM_CODE로 생성**
- VW_PDA_WID_LIST_NONFIXED:109-110 → `ID.ITEM_CODE AS BARCODEGOODS` (비정량은 ITEM_CODE 사용)

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 스캔 매칭에 사용**
- ShipmentActivity.java:1335-1349 → `String bg = bi.getBARCODEGOODS()` (바코드 정보에서 비교)
- ShipmentActivity.java:1419-1430 → 동일
- **작업 위치 찾기**
- ShipmentActivity.java:3014 → `work_ppcode = find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type)` (작업 정보 검색)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW에서 생성**: 비정량은 ITEM_CODE를 BARCODEGOODS로 사용
- **바코드 스캔 매칭 핵심**: 스캔한 바코드와 상품 매칭
- **작업 위치 검색**: 계근 작업 시작 시 해당 상품 찾기
- **제거 시 바코드 스캔 매칭 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 109-110 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 71, 102 | SELECT/출력 제거 |
| ShipmentActivity.java | 1335-1349, 1419-1430, 3014 | **바코드 매칭 - 제거 불가** |
| DBHandler.java | 63, 139, 194, 260, 306, 364, 412, 692, 733 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 34, 311-316 | 필드, getter/setter 제거 |
| DBInfo.java | 54 (주석처리됨) | 상수 제거 |

---

### STORE_IN_DATE

#### 1. UI 노출: O
- **라벨 프린터에 납품일자 출력**
- ShipmentActivity.java:2283-2284 → `si.getSTORE_IN_DATE().substring(0,4) + "년 "...` (M3 라벨)
- ShipmentActivity.java:2300-2301 → M4 라벨
- ShipmentActivity.java:2310-2311 → M9 라벨
- ShipmentActivity.java:2397-2398 → 미트센터 라벨
- ShipmentActivity.java:2474-2475 → 미트센터 라벨
- ShipmentActivity.java:2615-2616 → 홈플러스 라벨

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:111 → `STORE_IN_DATE` (EO 서브쿼리에서 조회)

#### 3. 안드로이드 소스 사용: X
- 라벨 출력 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유 (DBHandler.java:734)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 프린터 납품일자 출력 핵심**: 라벨에 "YYYY년 MM월 DD일" 형식으로 출력
- **여러 바코드 타입에서 사용**: M3, M4, M9, 미트센터, 홈플러스 등
- **제거 시 라벨에 납품일자 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 111 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 72, 103 | SELECT/출력 제거 |
| ShipmentActivity.java | 2283-2284, 2300-2301, 2310-2311, 2397-2398, 2474-2475, 2615-2616 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 64, 140, 195, 261, 307, 365, 413, 693, 734 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 35, 75, 77 | 필드, getter/setter 제거 |
| DBInfo.java | 57 | 상수 제거 |

---

### GR_WAREHOUSE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_LIST_NONFIXED:112 → `WR.GR_WAREHOUSE_CODE` (W_GOODS_R 테이블에서 조회)

#### 3. 안드로이드 소스 사용: O (핵심)
- **서버 조회 WHERE 조건으로 사용**
- ProgressDlgShipSearch.java:52 → `data += " AND GR_WAREHOUSE_CODE = 'IN10273'"` (삼일냉장)
- ProgressDlgShipSearch.java:54 → `data += " AND GR_WAREHOUSE_CODE = 'IN60464'"` (SWC)
- ProgressDlgShipSearch.java:56 → `data += " AND GR_WAREHOUSE_CODE = '4001'"` (이천1센터)
- ProgressDlgShipSearch.java:58 → `data += " AND GR_WAREHOUSE_CODE = '4004'"` (부산센터)
- ProgressDlgShipSearch.java:60 → `data += " AND GR_WAREHOUSE_CODE = 'IN63279'"` (탑로지스)
- **여러 searchType에서 동일하게 사용** (0, 2, 3, 6)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **서버 조회 WHERE 조건 핵심**: 창고별 데이터 필터링
- **창고 선택 기능**: 삼일냉장, SWC, 이천1센터, 부산센터, 탑로지스 구분
- **제거 시 창고별 조회 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 112 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 75, 103 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 52-60, 72-80, 89-97, 112-120 | **서버 조회 WHERE - 제거 불가** |
| DBHandler.java | 65, 141, 196, 262, 308, 366, 414, 694, 735 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | 58 | 상수 제거 |

---

### EMARTLOGIS_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **EO 서브쿼리에서 조회**
- VW_PDA_WID_LIST_NONFIXED:113 → `DECODE(EO.EMARTLOGIS_CODE,NULL,'0000000',EO.EMARTLOGIS_CODE) AS EMARTLOGIS_CODE`
- VW_PDA_WID_LIST_NONFIXED:137 → `EB.EMARTLOGIS_CODE` (서브쿼리 내부)

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 생성에 사용 (핵심)**
- ShipmentActivity.java:1880-2050 → 다수의 BARCODE_TYPE별 바코드 생성 시 사용
  - `pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO()`
- ShipmentActivity.java:2349, 2429 → 미트센터 바코드 분기 조건
  - `si.getEMARTLOGIS_CODE().equals("0000000")` (미트센터 여부 판단)
- **라벨 출력에 사용**
- ShipmentActivity.java:2551 → `pointCode = si.EMARTLOGIS_CODE.toString()` (지점코드 출력)
- ShipmentActivity.java:2592 → `getTTFcode(155, 155, pointCode.toString())` (지점코드 라벨)
- **롯데 전용 업체코드**
- ShipmentActivity.java:2654 → `String pCompCode_lotte = si.EMARTLOGIS_CODE` (롯데용 업체코드)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성 핵심**: 거의 모든 바코드 타입에서 EMARTLOGIS_CODE가 바코드 구성 요소
- **미트센터 분기 조건**: "0000000"인지 여부로 미트센터 라벨 출력 결정
- **롯데 업체코드**: 롯데 출하 시 업체코드로 사용
- **라벨 지점코드 출력**: 홈플러스 등 라벨에 지점코드 표시
- **제거 시 바코드 생성/라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 113, 137 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 73, 104 | SELECT/출력 제거 |
| ShipmentActivity.java | 1880-2050, 2349, 2429, 2551, 2592, 2654 | **바코드/라벨 생성 - 제거 불가** |
| DBHandler.java | 65, 141, 196, 262, 308, 366, 414, 694, 735 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 41, 67, 69 | 필드, getter/setter 제거 |
| DBInfo.java | 58 | 상수 제거 |

---

### EMARTLOGIS_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음
- 주석 처리된 라벨 출력 코드 존재 (미사용)
- ShipmentActivity.java:2161-2166 → 주석 처리됨 `/*if (si.EMARTLOGIS_NAME.length() > 14)...*/`

#### 2. VIEW 내부 사용: O
- **EO 서브쿼리에서 DECODE 함수로 생성**
- VW_PDA_WID_LIST_NONFIXED:114 → `DECODE(EO.EMARTLOGIS_NAME,NULL,'정보없음',EO.EMARTLOGIS_NAME) AS EMARTLOGIS_NAME`
- VW_PDA_WID_LIST_NONFIXED:138 → `(SELECT BE.EMRTITEM_NAME FROM B_EMART_BARCODE BE WHERE BE.EMARTITEM_CODE = EB.EMARTLOGIS_CODE) AS EMARTLOGIS_NAME`

#### 3. 안드로이드 소스 사용: X
- **주석 처리된 코드만 존재 (미사용)**
- ShipmentActivity.java:2161-2166 → 주석 처리된 라벨 출력 코드
- ShipmentActivity.java:2218 → 주석 처리된 로그 출력
- 로컬 DB INSERT 경유만 존재 (DBHandler.java:736)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- 주석 처리된 코드만 존재하여 실제 사용 없음
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 114, 138 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 74, 104 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 215 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 66, 142, 197, 263, 309, 367, 415, 695, 736 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 42, 71, 73 | 필드, getter/setter 제거 |
| DBInfo.java | 59 | 상수 제거 |

---

### WH_AREA

#### 1. UI 노출: O
- **라벨 프린터에 창고영역 출력**
- ShipmentActivity.java:2325-2332 → 이마트 라벨에 WH_AREA 출력
  ```java
  whArea = si.getWH_AREA();
  if(whArea != null || !whArea.equals("")){
      byteStream.write(WoosimCmd.PM_setPosition(430, 385));
      byteStream.write(WoosimCmd.getTTFcode(65, 65, whArea));
  }
  ```
- ShipmentActivity.java:2407-2414 → 미트센터+공장코드 라벨에 출력
- ShipmentActivity.java:2484-2491 → 미트센터 라벨에 출력
- ShipmentActivity.java:2869-2877 → 롯데 라벨에 출력

#### 2. VIEW 내부 사용: O
- **EO 서브쿼리에서 조회**
- VW_PDA_WID_LIST_NONFIXED:115 → `WH_AREA` (EO 서브쿼리에서 전달)
- VW_PDA_WID_LIST_NONFIXED:139 → `BCC2.REF_CODE2 WH_AREA` (B_COMMON_CODE에서 REF_CODE2 조회)

#### 3. 안드로이드 소스 사용: X
- 라벨 출력 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유 (DBHandler.java:737)
- 주석 처리된 setWH_AREA 코드 존재 (ShipmentActivity.java:2957)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 프린터 창고영역 출력**: 여러 바코드 타입 라벨에 창고 영역 표시
- **이마트, 미트센터, 롯데 라벨**: 다양한 라벨에서 사용
- **제거 시 라벨에 창고영역 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 115, 139 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 75, 104 | SELECT/출력 제거 |
| ShipmentActivity.java | 2325-2332, 2407-2414, 2484-2491, 2869-2877 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 68, 143, 199, 696, 737 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 44, 360-365 | 필드, getter/setter 제거 |
| DBInfo.java | 117 | 상수 제거 |

---

### USE_NAME

#### 1. UI 노출: O
- **라벨 프린터에 용도명 출력**
- ShipmentActivity.java:2031 → `pBarcodeStr3 = si.EMARTITEM +","+si.getUSE_NAME()` (제품명+용도)
- ShipmentActivity.java:2265 → `String belowBarcodeString = si.EMARTITEM +","+si.getUSE_NAME()` (M9 라벨)
- ShipmentActivity.java:2021 → `Log.d(TAG, "용도명 : " + si.getUSE_NAME())` (로그 출력)

#### 2. VIEW 내부 사용: O
- **EO 서브쿼리에서 서브쿼리로 조회**
- VW_PDA_WID_LIST_NONFIXED:116 → `EO.USE_NAME`
- VW_PDA_WID_LIST_NONFIXED:141-146 →
  ```sql
  (SELECT BC.CODE_NAME
     FROM B_COMMON_CODE BC
    WHERE BC.MASTER_CODE = 'EMART_RAWMEAT_USE_TYPE'
      AND BC.CODE = EB.USE_CODE
  ) AS USE_NAME
  ```

#### 3. 안드로이드 소스 사용: O
- **라벨 출력 시 문자열 조합**
- ShipmentActivity.java:2031 → M9 바코드 타입에서 제품명+용도명 조합
- ShipmentActivity.java:2265 → M9 라벨 하단에 제품명+용도명 출력

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **M9 라벨 출력 핵심**: 제품명과 용도명 조합하여 라벨에 표시
- **VIEW에서 서브쿼리로 생성**: USE_CODE를 기반으로 CODE_NAME 조회
- **제거 시 M9 라벨 용도명 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 116, 141-146 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 76, 104 | SELECT/출력 제거 |
| ShipmentActivity.java | 2021, 2031, 2265 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 69, 144, 200, 697, 738 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 45, 368-373 | 필드, getter/setter 제거 |
| DBInfo.java | 118 | 상수 제거 |

---

### USE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **EO 서브쿼리에서 조회**
- VW_PDA_WID_LIST_NONFIXED:117 → `EB.USE_CODE AS USE_CODE`
- VW_PDA_WID_LIST_NONFIXED:145 → `AND BC.CODE = EB.USE_CODE` (USE_NAME 생성 시 WHERE 조건)
- VW_PDA_WID_LIST_NONFIXED:147 → `EB.USE_CODE AS USE_CODE`

#### 3. 안드로이드 소스 사용: O (핵심)
- **M9 바코드 생성에 사용**
- ShipmentActivity.java:2027 → `pBarcode2 = si.getEMARTITEM_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + si.getUSE_CODE()`
- ShipmentActivity.java:2028 → `pBarcodeStr2 = si.getEMARTITEM_CODE().substring(0, 6) + " " + si.getIMPORT_ID_NO() + " " + si.getUSE_CODE()`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **M9 바코드 생성 핵심**: 바코드 구성 요소로 USE_CODE 포함
- **VIEW에서 USE_NAME 생성 조건**: USE_CODE를 기반으로 CODE_NAME 조회
- **제거 시 M9 바코드 생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 117, 145, 147 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 77, 104 | SELECT/출력 제거 |
| ShipmentActivity.java | 2027-2028 | **바코드 생성 - 제거 불가** |
| DBHandler.java | 70, 145, 201, 698, 739 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 46, 376-381 | 필드, getter/setter 제거 |
| DBInfo.java | 119 | 상수 제거 |

---

### CT_NAME

#### 1. UI 노출: O
- **라벨 프린터에 원산지명 출력**
- ShipmentActivity.java:2258-2259 → M9 라벨에 원산지명 출력
  ```java
  String ctName = si.getCT_NAME();
  byteStream.write(WoosimCmd.getTTFcode(25, 25, ctName));
  ```

#### 2. VIEW 내부 사용: O
- **서브쿼리로 원산지명 생성**
- VW_PDA_WID_LIST_NONFIXED:118 →
  ```sql
  (SELECT BC.CODE_NAME FROM B_COMMON_CODE BC
   WHERE BC.CODE = WR.CT_CODE
   AND BC.MASTER_CODE = 'HOMEPLUS_ORIGIN_CODE'
   AND BC.STATUS = 'Y')||'산' AS CT_NAME
  ```
- CT_CODE를 기반으로 CODE_NAME 조회 후 '산' 접미사 추가 (예: "호주산")

#### 3. 안드로이드 소스 사용: X
- 라벨 출력 외 분기, 계산, 서버 전송 등 사용 없음
- 로컬 DB INSERT 경유 (DBHandler.java:740)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **M9 라벨 원산지명 출력**: CT_CODE 기반으로 "호주산" 등 표시
- **VIEW에서 서브쿼리로 생성**: HOMEPLUS_ORIGIN_CODE 마스터 코드 참조
- **제거 시 M9 라벨 원산지명 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 118 | SELECT 컬럼에서 제거 |
| search_production_nonfixed.jsp | 78, 104 | SELECT/출력 제거 |
| ShipmentActivity.java | 2258-2259 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 71, 146, 202, 699, 740 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 47, 384-389 | 필드, getter/setter 제거 |
| DBInfo.java | 120 | 상수 제거 |

---

### STORE_CODE

#### 1. UI 노출: O
- **라벨 프린터에 점포코드 출력**
- ShipmentActivity.java:1848-1849 → 바코드 생성 시 STORE_CODE 사용
  ```java
  sBarcode = si.getSTORE_CODE();
  sBarcodeStr = si.getSTORE_CODE();
  ```
- ShipmentActivity.java:2105 → M9 라벨에 지점명+점포코드 출력
  ```java
  String storeNamePlusCode = pointName + "(" + si.getSTORE_CODE() +")";
  ```
- ShipmentActivity.java:2552 → 홈플러스 라벨에 storeCode 사용
- ShipmentActivity.java:2589-2590 → B 타입 라벨에 점포코드 출력
  ```java
  if (si.getITEM_TYPE().equals("B")) {
      byteStream.write(WoosimCmd.getTTFcode(155, 155, storeCode.toString()));  // 점포코드 출력(홈플러스 비정량)
  }
  ```

#### 2. VIEW 내부 사용: O
- **EO 서브쿼리에서 조회**
- VW_PDA_WID_LIST_NONFIXED:119 → `EO.STORECODE AS STORE_CODE`
- VW_PDA_WID_LIST_NONFIXED:130 → `EOI.STORE_CODE STORECODE`
- VW_PDA_WID_LIST_NONFIXED:140 → `DECODE(EOI.STORE_CODE, EOI.CENTER_CODE, '직납','센터납') deliverType` (직납/센터납 분기)
- VW_PDA_WID_LIST_NONFIXED:161 → `AND EOI.STORE_CODE = BCC2.CODE` (JOIN 조건)

#### 3. 안드로이드 소스 사용: O (핵심)
- **미트센터 분기 조건 핵심**
- ShipmentActivity.java:347 → `arSM.get(current_work_position).getSTORE_CODE().equals("9231")` (킬코이+미트센터)
- ShipmentActivity.java:793 → 동일 (미트센터 분기)
- ShipmentActivity.java:1699 → 동일 (소비기한 변조 분기)
- ShipmentActivity.java:2349 → `si.getSTORE_CODE().equals("9231")` (미트센터+공장코드 라벨)
- ShipmentActivity.java:2429 → 동일 (미트센터 라벨)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **미트센터 분기 핵심**: STORE_CODE "9231"로 이마트 미트센터 식별
- **VIEW JOIN 조건**: EO 서브쿼리 JOIN에 사용
- **라벨 출력**: 지점명+점포코드 형태로 표시
- **직납/센터납 분기**: STORE_CODE와 CENTER_CODE 비교로 구분
- **제거 시 미트센터 분기 및 라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_NONFIXED (VIEW) | 119, 130, 140, 161 | **JOIN 조건 및 분기 - 제거 불가** |
| search_production_nonfixed.jsp | 79, 104 | SELECT/출력 제거 |
| ShipmentActivity.java | 347, 793, 1699, 1848-1849, 2105, 2349, 2429, 2552, 2589-2590 | **미트센터 분기/라벨 - 제거 불가** |
| DBHandler.java | 72, 147, 203, 700, 741 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 48, 392-397 | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |
