# VW_PDA_WID_LIST_LOTTE 컬럼 사용 여부 분석

## VIEW 정보
- **VIEW 명**: VW_PDA_WID_LIST_LOTTE
- **스키마**: INNO
- **용도**: 롯데마트 계근 리스트
- **searchType**: 2 (추정)

---

## 컬럼 분석 결과

| 컬럼명                               | UI 노출 | VIEW 내부 | 안드로이드 소스 | DDL 사용 | 비즈니스 영향 | 판정      |     |
| --------------------------------- | ----- | ------- | -------- | ------ | ------- | ------- | --- |
| [GI_H_ID](#gi_h_id)               | X     | **O**   | X        | X      | **있음**  | **사용**  |     |
| [GI_D_ID](#gi_d_id)               | X     | **O**   | **O**    | **O**  | **있음**  | **사용**  |     |
| [EOI_ID](#eoi_id)                 | X     | **O**   | X        | X      | **있음**  | **사용**  |     |
| [ITEM_CODE](#item_code)           | X     | **O**   | **O**    | **O**  | **있음**  | **사용**  |     |
| [ITEM_NAME](#item_name)           | **O** | X       | X        | X      | **있음**  | **사용**  |     |
| [EMARTITEM_CODE](#emartitem_code) | X     | **O**   | **O**    | X      | **있음**  | **사용**  |     |
| [EMARTITEM](#emartitem)           | X     | X       | **O**    | X      | **있음**  | **사용**  |     |
| [GI_REQ_PKG](#gi_req_pkg)         | **O** | **O**   | **O**    | X      | **있음**  | **사용**  |     |
| [GI_REQ_QTY](#gi_req_qty)         | **O** | X       | **O**    | X      | **있음**  | **사용**  |     |
| [AMOUNT](#amount)                 | X     | X       | X        | X      | 없음      | **미사용** |     |
| [GOODS_R_ID](#goods_r_id)         | X     | **O**   | X        | X      | **있음**  | **사용**  |     |
| [GR_REF_NO](#gr_ref_no)           | X     | X       | X        | X      | 없음      | **미사용** |     |
| [GI_REQ_DATE](#gi_req_date)       | X     | **O**   | **O**    | X      | **있음**  | **사용**  |     |
| [BL_NO](#bl_no)                   | **O** | X       | **O**    | X      | **있음**  | **사용**  |     |
| [BRAND_CODE](#brand_code)         | X     | **O**   | **O**    | **O**  | **있음**  | **사용**  |     |
| [BRANDNAME](#brandname)           | X     | X       | X        | X      | 없음      | **미사용** |     |
| [CLIENT_CODE](#client_code)       | X     | **O**   | **O**    | X      | **있음**  | **사용**  |     |
| [CLIENTNAME](#clientname)         | **O** | X       | **O**    | X      | **있음**  | **사용**  |     |
| [CENTERNAME](#centername)         | X     | X       | **O**    | X      | **있음**  | **사용**  |     |
| [ITEM_SPEC](#item_spec)           | X     | X       | **O**    | X      | **있음**  | **사용**  |     |
| [CT_CODE](#ct_code)               | X     | X       | **O**    | X      | **있음**  | **사용**  |     |
| [PACKER_CODE](#packer_code)       | X     | **O**   | **O**    | X      | **있음**  | **사용**  |     |
| [IMPORT_ID_NO](#import_id_no)     | X     | **O**   | **O**    | X      | **있음**  | **사용**  |     |
| [PACKERNAME](#packername)         | X     | X       | X        | X      | 없음      | **미사용** |     |
| [PACKER_PRODUCT_CODE](#packer_product_code) | X | **O** | **O** | **O** | **있음** | **사용** |     |
| [BARCODE_TYPE](#barcode_type)     | X     | **O**   | **O**    | X      | **있음**  | **사용** |     |
| [ITEM_TYPE](#item_type)           | X     | **O**   | **O**    | X      | **있음**  | **사용** |     |
| [PACKWEIGHT](#packweight)         | X     | X       | **O**    | X      | **있음**  | **사용** |     |
| [BARCODEGOODS](#barcodegoods)     | X     | X       | **O**    | X      | **있음**  | **사용** |     |
| [STORE_IN_DATE](#store_in_date)   | X     | X       | **O**    | X      | **있음**  | **사용** |     |
| [GR_WAREHOUSE_CODE](#gr_warehouse_code) | X | X | **O** | X | **있음** | **사용** |     |
| [EMARTLOGIS_CODE](#emartlogis_code) | X | X | **O** | X | **있음** | **사용** |     |
| [EMARTLOGIS_NAME](#emartlogis_name) | X | X | X | X | 없음 | **미사용** |     |
| [WH_AREA](#wh_area)               | X     | X       | **O**    | X      | **있음**  | **사용** |     |
| [LAST_BOX_ORDER](#last_box_order) | X     | X       | **O**    | X      | **있음**  | **사용** |     |


---

## 상세 분석

### GI_H_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:107 → `ON IH.GI_H_ID = ID.GI_H_ID` (해외 매입 쿼리)
- VW_PDA_WID_LIST_LOTTE:195 → `ON IH.GI_H_ID = ID.GI_H_ID` (국내 매입 쿼리)
- W_GOODS_IH(출고헤더)와 W_GOODS_ID(출고상세) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음
- 로컬 DB 저장/조회 경유만 존재

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- VIEW에서 출고헤더-출고상세 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 107, 195 | **JOIN 조건 - 제거 불가** |
| search_shipment_lotte.jsp | 38, 86 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GI_D_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **LAST_BOX_ORDER 서브쿼리 WHERE 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:99 → `WHERE W.GI_D_ID IN (SELECT D.GI_D_ID FROM W_GOODS_ID D, W_MART_ORDER_ITEM L WHERE D.EOI_ID = L.EOI_ID)`
- VW_PDA_WID_LIST_LOTTE:187 → 동일 (UNION 쿼리)
- 박스 순번 계산에 필수

#### 3. 안드로이드 소스 사용: O
- **서버 전송에 핵심적으로 사용**
- ShipmentActivity.java:3320, 3430 → `packet += list_send_info.get(i).getGI_D_ID()` (서버 전송 패킷)
- ShipmentActivity.java:3370, 3502 → `completeStr = arSM.get(j).getGI_D_ID() + "::"` (완료 문자열)
- **DB 조회 WHERE 조건에 사용**
- ShipmentActivity.java:3302-3304 → `qry_where = "GI_D_ID = " + arSM.get(i).getGI_D_ID()`
- **완료 판단 및 업데이트**
- ShipmentActivity.java:3360, 3493 → `updatequeryGoodsWet(mContext, list_send_info.get(i).getGI_D_ID(), ...)`
- ShipmentActivity.java:3366, 3498 → GI_D_ID 비교로 완료 처리

#### 4. DDL 사용: O
- **INSERT 및 UPDATE에 핵심 사용**
- insert_goods_wet.jsp:51 → `GI_D_ID` INSERT 컬럼
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ?` UPDATE 조건

#### 5. 비즈니스 영향: **있음 (핵심)**
- **출고상세 식별자로 핵심**
- 서버 전송, 완료 처리, DB 조회 모두에 필수
- **제거 시 전체 계근 프로세스 불가**

#### 6. 수정 위치
| 파일                           | 라인             | 수정 내용                     |
| ---------------------------- | -------------- | ------------------------- |
| VW_PDA_WID_LIST_LOTTE (VIEW) | 99, 187        | **서브쿼리 조건 - 제거 불가**       |
| search_shipment_lotte.jsp    | 39, 86         | SELECT/출력 제거              |
| insert_goods_wet.jsp         | 51, 105        | **INSERT/UPDATE - 제거 불가** |
| ShipmentActivity.java        | 3302-3502 (다수) | **서버 전송, 완료 처리 - 제거 불가**  |
| ProgressDlgShipSearch.java   | -              | 파싱 제거, 인덱스 조정             |
| DBHandler.java               | -              | 스키마/INSERT/SELECT에서 제거    |
| Shipments_Info.java          | -              | 필드, getter/setter 제거      |
| DBInfo.java                  | -              | 상수 제거                     |

---

### EOI_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:115 → `ON wmoi.EOI_ID = id.EOI_ID` (발주서 JOIN)
- VW_PDA_WID_LIST_LOTTE:203 → 동일 (UNION 쿼리)
- **서브쿼리 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:99 → `WHERE D.EOI_ID = L.EOI_ID` (LAST_BOX_ORDER 서브쿼리)
- VW_PDA_WID_LIST_LOTTE:187 → 동일 (UNION 쿼리)
- W_GOODS_ID와 W_MART_ORDER_ITEM 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity에서 분기, 계산, 서버 전송 사용 없음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **JSP ORDER BY 정렬 기준**
- search_shipment_lotte.jsp:74 → `ORDER BY EOI_ID ASC`
- **VIEW JOIN 및 서브쿼리 조건으로 필수**
- **제거 시 정렬 순서 변경 및 VIEW 구조 변경 필요**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 99, 115, 187, 203 | **JOIN/서브쿼리 조건 - 제거 불가** |
| search_shipment_lotte.jsp | 40, 74, 86 | SELECT/ORDER BY/출력 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### ITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음
- ITEM_NAME은 표시됨 (ShipmentActivity.java:1573)

#### 2. VIEW 내부 사용: O
- **DE_ITEM 함수 인자로 사용**
- VW_PDA_WID_LIST_LOTTE:47 → `DE_ITEM(ID.ITEM_CODE) AS ITEM_NAME` (해외 매입 쿼리)
- VW_PDA_WID_LIST_LOTTE:135 → `DE_ITEM(ID.ITEM_CODE) AS ITEM_NAME` (국내 매입 쿼리)
- ITEM_NAME(상품명) 조회를 위한 함수 인자로 필수 사용

#### 3. 안드로이드 소스 사용: O
- **서버 전송에 사용**
- ShipmentActivity.java:3330 → `packet += list_send_info.get(i).getITEM_CODE() + "::"` (서버 전송 패킷)
- ShipmentActivity.java:3440 → `packet += list_send_info.get(i).getITEM_CODE() + "::"` (서버 전송 패킷)
- **완료 문자열에 사용**
- ShipmentActivity.java:3370 → `completeStr = arSM.get(j).getGI_D_ID() + "::" + arSM.get(j).getITEM_CODE() + "::"` (완료 처리)
- ShipmentActivity.java:3502 → 동일 (완료 처리)
- **Goodswets_Info 저장에 사용**
- ShipmentActivity.java:1127 → `gi.setITEM_CODE(arSM.get(current_work_position).getITEM_CODE())` (로컬 DB 저장)

#### 4. DDL 사용: O
- **UPDATE WHERE 조건으로 사용**
- update_shipment.jsp:68 → `WHERE GI_D_ID=? AND ITEM_CODE=? AND BRAND_CODE=?`
- update_shipment.jsp:82 → `pstmt.setString(5, splitData[1])` - ITEM_CODE 바인딩
- **서버 DB의 W_GOODS_ID 테이블 UPDATE 조건 핵심 컬럼**

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW 함수 인자로 필수**: DE_ITEM() 함수로 상품명 조회에 사용
- **서버 전송 필수**: 서버로 전송되는 패킷에 포함
- **완료 처리 필수**: 출하 완료 처리 시 서버에 전송
- **DDL WHERE 조건 필수**: W_GOODS_ID UPDATE 시 복합키로 사용
- **제거 시 전체 계근/출하 프로세스 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 47, 135 | **DE_ITEM() 함수 인자 - 제거 불가** |
| search_shipment_lotte.jsp | 41, 86 | SELECT/출력 제거 |
| update_shipment.jsp | 68, 82 | **UPDATE WHERE 조건 - 제거 불가** |
| ShipmentActivity.java | 1127, 3330, 3370, 3440, 3502 | **서버 전송/완료 처리 - 제거 불가** |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 38, 114, 169, 235, 281, 339, 387, 667, 708 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 9, 111-116 | 필드, getter/setter 제거 |
| Goodswets_Info.java | 17, 125-130 | 필드, getter/setter 제거 |
| DBInfo.java | 29 | 상수 제거 |

---

### ITEM_NAME

#### 1. UI 노출: O
- **화면에 상품명으로 표시**
- ShipmentActivity.java:1573 → `edit_product_name.setText(arSM.get(work_position).getITEM_NAME())` (작업 화면)
- ShipmentActivity.java:3171 → `edit_product_name.setText(arSM.get(0).getITEM_NAME().toString())` (목록 선택 후)
- ShipmentActivity.java:3795 → `detail_edit_ppname.setText(si.getITEM_NAME())` (상세 다이얼로그)

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:47, 135 → `DE_ITEM(ID.ITEM_CODE) AS ITEM_NAME` (단순 조회)
- JSP에서 SELECT/출력만 (search_shipment_lotte.jsp:42, 87)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장/UI 표시 외 분기, 계산, 서버 전송 등 사용 없음
- 라인 3019 주석 처리됨: `/*list_position.add(...getITEM_NAME())*/`

#### 4. DDL 사용: X
- INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **화면 UI에 상품명 표시 필수**
- 사용자가 어떤 상품을 계근하는지 확인하는 핵심 정보
- **제거 시 사용자가 상품 식별 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 8, 47, 135 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 42, 87 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 1573, 3171, 3795 | **setText() UI 표시 - 대체 필요** |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 10, 119-124 | 필드, getter/setter 제거 |
| DBInfo.java | 30 | 상수 제거 |

---

### EMARTITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:117 → `ON beb.EMARTITEM_CODE = wmoi.MART_ITEMCODE AND beb.BARCODE_TYPE LIKE 'L%'` (해외 매입 쿼리)
- VW_PDA_WID_LIST_LOTTE:205 → 동일 (국내 매입 쿼리)
- B_EMART_BARCODE 테이블과 W_MART_ORDER_ITEM 테이블 연결 키

#### 3. 안드로이드 소스 사용: O
- **바코드 생성에 핵심적으로 사용**
- ShipmentActivity.java:1620-1621 → `pBarcode = si.getEMARTITEM_CODE() + print_weight_str + "00" + now` (바코드 생성)
- ShipmentActivity.java:1877-1878 → `pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO()` (M0 바코드)
- ShipmentActivity.java:1893-1894 → M1 바코드
- ShipmentActivity.java:1910-1911 → M3 바코드
- ShipmentActivity.java:1926-1927 → M4 바코드
- ShipmentActivity.java:1943-1944 → E0 바코드
- **Goodswets_Info 저장에 사용**
- ShipmentActivity.java:1125 → `gi.setEMARTITEM_CODE(arSM.get(current_work_position).getEMARTITEM_CODE())`

#### 4. DDL 사용: X
- INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW JOIN 조건 필수**: B_EMART_BARCODE 테이블 연결에 필수
- **바코드 생성 필수**: 계근 후 라벨에 출력되는 바코드 생성에 핵심
- **제거 시 바코드 생성 불가 → 라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 117, 205 | **JOIN 조건 - 제거 불가** |
| search_shipment_lotte.jsp | 43, 87 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 1125, 1620-1944 (다수) | **바코드 생성 - 제거 불가** |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 11, 127-132 | 필드, getter/setter 제거 |
| Goodswets_Info.java | 15, 109-114 | 필드, getter/setter 제거 |
| DBInfo.java | 31 | 상수 제거 |

---

### EMARTITEM

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음 (라벨 프린터 출력만)

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:49, 137 → `wmoi.MART_ITEMNAME AS EMARTITEM` (단순 조회)
- JSP에서 SELECT/출력만 (search_shipment_lotte.jsp:44, 87)

#### 3. 안드로이드 소스 사용: O
- **라벨 프린터 출력에 핵심 사용**
- ShipmentActivity.java:1638-1641 → 라벨에 상품명 출력 (`WoosimCmd.getTTFcode(si.EMARTITEM + " / " + si.ITEM_SPEC)`)
- ShipmentActivity.java:2031, 2265 → 바코드 하단 문자열 (`si.EMARTITEM + "," + si.getUSE_NAME()`)
- ShipmentActivity.java:2152-2155, 2371-2374, 2448-2451, 2597 → 다양한 라벨 포맷에서 상품명 출력
- **글자 길이 분기에 사용**
- ShipmentActivity.java:1638, 2152, 2371, 2448 → `if (si.EMARTITEM.length() > 14)` 글자 크기 조절

#### 4. DDL 사용: X
- INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **라벨 프린터 출력 필수**: 계근 후 라벨에 이마트 상품명 출력
- 글자 길이에 따라 폰트 크기 자동 조절
- **제거 시 라벨에 상품명 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 10, 49, 137 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 44, 87 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 1126, 1588, 1638-2597 (다수) | **라벨 프린터 출력 - 제거 불가** |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 12, 135-140 | 필드, getter/setter 제거 |
| Goodswets_Info.java | 16, 117-122 | 필드, getter/setter 제거 |
| DBInfo.java | 32 | 상수 제거 |

---

### GI_REQ_PKG

#### 1. UI 노출: O
- **화면에 출하요청수량 표시**
- ShipmentActivity.java:1207 → `edit_wet_count.setText(arSM.get(current_work_position).getGI_REQ_PKG() + " / " + ...)`
- ShipmentActivity.java:1570 → 동일
- ShipmentActivity.java:3797 → `detail_edit_count.setText(si.getGI_REQ_PKG() + " / " + si.getPACKING_QTY())`
- ShipmentActivity.java:4018 → 동일
- ShipmentListAdapter.java:142 → `holder.count.setText(arSrc.get(pos).getGI_REQ_PKG() + "/" + ...)`

#### 2. VIEW 내부 사용: O
- **VIEW WHERE 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:119 → `AND ID.GI_REQ_PKG <> 0` (해외 매입 쿼리)
- VW_PDA_WID_LIST_LOTTE:208 → `AND ID.GI_REQ_PKG <> 0` (국내 매입 쿼리)
- 출하요청수량이 0이 아닌 건만 조회

#### 3. 안드로이드 소스 사용: O
- **계근 완료 판단에 핵심 사용**
- ShipmentActivity.java:772 → `!arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))` (계근 미완료 검색)
- ShipmentActivity.java:862, 1104, 3247 → 요청수량 == 계근수량 비교 (계근 완료 판단)
- ShipmentActivity.java:1244 → `Integer.parseInt(arSM.get(...).getGI_REQ_PKG()) <= arSM.get(...).getPACKING_QTY()` (계근 완료 조건)
- ShipmentActivity.java:1478, 3043 → 계근 미완료 지점 검색
- **총 계근요청수량 계산**
- ShipmentActivity.java:3030, 3185 → `centerTotalCount += Integer.parseInt(arSM.get(i).getGI_REQ_PKG())`
- **서버 전송 완료 판단**
- ShipmentActivity.java:3369, 3501 → `arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())` (전송 개수 == 요청 개수)

#### 4. DDL 사용: X
- INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW WHERE 조건 필수**: 출하요청수량이 0이 아닌 건만 조회
- **계근 완료 판단 핵심**: 요청수량과 계근수량 비교로 완료 여부 판단
- **총 계근요청수량 계산 필수**: 센터별 총 계근요청수량 합산
- **서버 전송 완료 판단 필수**: 전송 개수와 요청 개수 비교
- **제거 시 계근 완료 판단 불가 → 전체 프로세스 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 119, 208 | **WHERE 조건 - 제거 불가** |
| search_shipment_lotte.jsp | 45, 88 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 772, 862, 1104, 1207, 1244, 1478, 1570, 3030, 3043, 3185, 3247, 3369, 3501, 3797, 4018 | **계근 완료 판단/UI 표시 - 제거 불가** |
| ShipmentListAdapter.java | 142 | **리스트 UI 표시 - 제거 불가** |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 13, 143-148 | 필드, getter/setter 제거 |
| DBInfo.java | 33 | 상수 제거 |

---

### GI_REQ_QTY

#### 1. UI 노출: O
- **화면에 출하요청중량 표시**
- ShipmentActivity.java:1208 → `edit_wet_weight.setText(arSM.get(current_work_position).getGI_REQ_QTY() + " / " + ...)`
- ShipmentActivity.java:1571 → 동일
- ShipmentActivity.java:3798 → `detail_edit_weight.setText(si.getGI_REQ_QTY() + " / " + si.getGI_QTY())`
- ShipmentActivity.java:4019 → 동일
- ShipmentListAdapter.java:146 → `holder.weight.setText(arSrc.get(pos).getGI_REQ_QTY() + "/" + ...)`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:52, 140 → `ID.GI_REQ_QTY` (단순 조회)
- JSP에서 SELECT/출력만 (search_shipment_lotte.jsp:46, 88)

#### 3. 안드로이드 소스 사용: O
- **총 계근요청중량 계산에 사용**
- ShipmentActivity.java:3031 → `centerTotalWeight += Double.parseDouble(arSM.get(i).getGI_REQ_QTY())` (센터 총 계근요청중량)
- ShipmentActivity.java:3186 → 동일

#### 4. DDL 사용: X
- INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **UI 표시 필수**: `요청중량 / 계근중량` 형태로 화면에 표시
- **총 계근요청중량 계산 필수**: 센터별 총 계근요청중량 합산
- **제거 시 화면에서 요청중량 확인 불가**

#### 6. 수정 위치
| 파일                           | 라인                                 | 수정 내용                  |
| ---------------------------- | ---------------------------------- | ---------------------- |
| VW_PDA_WID_LIST_LOTTE (VIEW) | 12, 52, 140                        | SELECT 컬럼에서 제거         |
| search_shipment_lotte.jsp    | 46, 88                             | SELECT/출력에서 제거         |
| ShipmentActivity.java        | 1208, 1571, 3031, 3186, 3798, 4019 | **UI 표시/계산 - 대체 필요**   |
| ShipmentListAdapter.java     | 146                                | **리스트 UI 표시 - 대체 필요**  |
| ProgressDlgShipSearch.java   | -                                  | 파싱 제거, 인덱스 조정          |
| DBHandler.java               | -                                  | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java          | 14, 151-156                        | 필드, getter/setter 제거   |
| DBInfo.java                  | 34                                 | 상수 제거                  |

---

### AMOUNT

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음
- setText() 등으로 AMOUNT 표시하는 코드 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:54, 142 → `ID.AMOUNT` (단순 조회)
- JSP에서 SELECT/출력만 (search_shipment_lotte.jsp:47, 88)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- DBHandler.java:714 → `si.getAMOUNT()` (로컬 DB INSERT만)
- getAMOUNT()가 if 조건, 계산, 서버 전송에 사용되지 않음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT 문에 사용 없음
- update_shipment.jsp UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **제거해도 비즈니스 기능에 영향 없음**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 54, 142 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 47, 88 | SELECT/출력에서 제거 |
| ProgressDlgShipSearch.java | - | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 714 | INSERT 문에서 컬럼 제거 |
| DBHandler.java | - | 스키마/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### GOODS_R_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음
- setText() 등으로 GOODS_R_ID 표시하는 코드 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:109 → `ON ID.GOODS_R_ID = WR.GOODS_R_ID` (해외 매입 쿼리)
- VW_PDA_WID_LIST_LOTTE:197 → `ON ID.GOODS_R_ID = WR.GOODS_R_ID` (국내 매입 쿼리)
- W_GOODS_ID(출고상세)와 W_GOODS_R(입고) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- DBHandler.java:715 → `si.getGOODS_R_ID()` (로컬 DB INSERT만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT 문에 사용 없음
- update_shipment.jsp UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- W_GOODS_ID(출고상세)와 W_GOODS_R(입고) 테이블 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일                           | 라인                                         | 수정 내용                  |
| ---------------------------- | ------------------------------------------ | ---------------------- |
| VW_PDA_WID_LIST_LOTTE (VIEW) | 109, 197                                   | **JOIN 조건 - 제거 불가**    |
| search_shipment_lotte.jsp    | 48, 89                                     | SELECT/출력 제거           |
| ProgressDlgShipSearch.java   | 194                                        | 파싱 제거, 인덱스 조정          |
| DBHandler.java               | 45, 121, 176, 242, 288, 346, 394, 674, 715 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java          | 16, 167-172                                | 필드, getter/setter 제거   |
| DBInfo.java                  | 36                                         | 상수 제거                  |

---

### GR_REF_NO

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 표시 없음
- setText() 등으로 GR_REF_NO 표시하는 코드 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:58, 146 → `WR.GR_REF_NO` (창고입고번호, 단순 SELECT)
- JSP에서 SELECT/출력만 (search_shipment_lotte.jsp:49, 89)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- DBHandler.java:716 → `si.getGR_REF_NO()` (로컬 DB INSERT만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT 문에 사용 없음
- update_shipment.jsp UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **제거해도 비즈니스 기능에 영향 없음**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 58, 146 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 49, 89 | SELECT/출력에서 제거 |
| ProgressDlgShipSearch.java | 195 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 46, 122, 177, 243, 289, 347, 395, 675, 716 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 17, 175-180 | 필드, getter/setter 제거 |
| DBInfo.java | 37 | 상수 제거 |

---

### GI_REQ_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW WHERE 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:122 → `AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` (해외 매입)
- VW_PDA_WID_LIST_LOTTE:211 → `AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE, 'YYYYMMDD')` (국내 매입)
- 오늘 날짜 이후의 출하요청일만 조회하는 핵심 필터

#### 3. 안드로이드 소스 사용: O
- **JSP 호출 시 WHERE 조건으로 사용**
- ProgressDlgShipSearch.java:48 → `WHERE GI_REQ_DATE = '` + Common.selectDay (서버 조회 조건)
- DBHandler.java:107 → `AND GI_REQ_DATE = ` + Common.selectDay (로컬 DB 조회 조건)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW WHERE 조건 필수**: 오늘 이후 출하요청일만 조회
- **서버/로컬 조회 조건**: 선택된 날짜 기준 데이터 필터링
- **제거 시 날짜별 데이터 조회 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 122, 211 | **WHERE 조건 - 제거 불가** |
| search_shipment_lotte.jsp | 50, 89 | SELECT/출력에서 제거 |
| ProgressDlgShipSearch.java | 48, 196 | **서버 조회 조건 - 제거 불가** |
| DBHandler.java | 47, 107, 123, 178, 244, 290, 348, 396, 676, 717 | **조회 조건 - 제거 불가** |
| Shipments_Info.java | 18, 183-188 | 필드, getter/setter 제거 |
| DBInfo.java | 38 | 상수 제거 |

---

### BL_NO

#### 1. UI 노출: O
- **리스트 화면에 BL번호 끝 4자리 표시**
- ShipmentListAdapter.java:152 → `holder.bl.setText(arSrc.get(pos).getBL_NO().substring(...))` (BL번호 끝 4자리)

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:62-63, 150-151 → `DECODE(WR.BL_NO, NULL, WR.IMPORT_ID_NO, WR.BL_NO)` (단순 조회)

#### 3. 안드로이드 소스 사용: O
- **BL번호 기반 조회/필터링에 핵심 사용**
- DBHandler.java:103 → `AND BL_NO = '` + condition (로컬 DB 조회 조건)
- DBHandler.java:371 → `AND BL_NO = '` + bl_no (BL번호로 데이터 조회)
- **BL번호 스피너/리스트 구성에 사용**
- ShipmentActivity.java:772 → `temp_bl_no.equals(arSM.get(i).getBL_NO())` (BL별 미완료 검색)
- ShipmentActivity.java:1318 → `BL_NO.equals(arSM.get(current_work_position).getBL_NO())` (바코드 스캔 검증)
- ShipmentActivity.java:1564, 1569 → `list_bl.add(arSM.get(work_position).getBL_NO())` (BL 리스트 구성)
- ShipmentActivity.java:3193-3215 → BL 스피너 선택 처리

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 표시 필수**: 리스트에서 BL번호 끝 4자리 표시
- **BL별 조회/필터 핵심**: BL번호로 데이터 그룹핑 및 조회
- **바코드 스캔 검증**: 스캔한 바코드와 현재 BL 매칭 확인
- **제거 시 BL별 작업 관리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 62-63, 150-151 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 51, 89 | SELECT/출력에서 제거 |
| ShipmentListAdapter.java | 149, 152 | **UI 표시 - 제거 불가** |
| ShipmentActivity.java | 772, 1318, 1544, 1564, 1569, 3193-3215 | **BL 관리 로직 - 제거 불가** |
| DBHandler.java | 48, 103, 124, 179, 245, 291, 349, 371, 397, 677, 718 | **조회 조건 - 제거 불가** |
| Shipments_Info.java | 19, 191-196 | 필드, getter/setter 제거 |
| DBInfo.java | 39 | 상수 제거 |

---

### BRAND_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **DE_COMMON 함수 인자로 사용**
- VW_PDA_WID_LIST_LOTTE:66 → `DE_COMMON('BRAND', ID.BRAND_CODE) AS BRANDNAME` (해외 매입)
- VW_PDA_WID_LIST_LOTTE:154 → 동일 (국내 매입)
- 브랜드명(BRANDNAME) 조회를 위한 함수 인자로 필수

#### 3. 안드로이드 소스 사용: O
- **서버 전송에 핵심 사용**
- ShipmentActivity.java:3331 → `packet += list_send_info.get(i).getBRAND_CODE() + "::"` (서버 전송)
- ShipmentActivity.java:3441 → 동일 (서버 전송)
- **완료 처리에 핵심 사용**
- ShipmentActivity.java:3370 → `completeStr = ... + arSM.get(j).getBRAND_CODE() + "::"` (완료 문자열)
- ShipmentActivity.java:3502 → 동일 (완료 문자열)
- **Goodswets_Info 저장에 사용**
- ShipmentActivity.java:1128 → `gi.setBRAND_CODE(arSM.get(current_work_position).getBRAND_CODE())`

#### 4. DDL 사용: O
- **INSERT/UPDATE WHERE 조건으로 사용**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet.jsp:114 → `pstmt.setString(7, splitData[11])` - BRAND_CODE 바인딩
- update_shipment.jsp:68 → `WHERE GI_D_ID=? AND ITEM_CODE=? AND BRAND_CODE=?`

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW 함수 인자 필수**: DE_COMMON()으로 브랜드명 조회
- **서버 전송 필수**: 서버로 전송되는 패킷에 포함
- **완료 처리 필수**: 출하 완료 시 서버에 전송
- **DDL WHERE 조건 필수**: INSERT/UPDATE 시 복합키로 사용
- **제거 시 전체 계근/출하 프로세스 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 66, 154 | **함수 인자 - 제거 불가** |
| search_shipment_lotte.jsp | 52, 90 | SELECT/출력에서 제거 |
| insert_goods_wet.jsp | 105, 114 | **UPDATE WHERE 조건 - 제거 불가** |
| update_shipment.jsp | 68 | **UPDATE WHERE 조건 - 제거 불가** |
| ShipmentActivity.java | 1128, 3331, 3370, 3441, 3502 | **서버 전송/완료 처리 - 제거 불가** |
| DBHandler.java | 719, 1124, 1573, 1644, 1717 | Goodswets INSERT에서 제거 |
| Shipments_Info.java | 20, 199-204 | 필드, getter/setter 제거 |
| Goodswets_Info.java | 18, 133-138 | 필드, getter/setter 제거 |
| Barcodes_Info.java | 6, 40-45 | 필드, getter/setter 제거 |
| DBInfo.java | 40 | 상수 제거 |

---

### BRANDNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:66, 154 → `DE_COMMON('BRAND', ID.BRAND_CODE) AS BRANDNAME` (단순 SELECT)
- JSP에서 SELECT/출력만 (search_shipment_lotte.jsp:53, 90)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- DBHandler.java:720 → `si.getBRANDNAME()` (로컬 DB INSERT만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **제거해도 비즈니스 기능에 영향 없음**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 66, 154 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 53, 90 | SELECT/출력에서 제거 |
| ProgressDlgShipSearch.java | 199 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 50, 126, 181, 247, 293, 351, 399, 679, 720 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 21, 207-212 | 필드, getter/setter 제거 |
| DBInfo.java | 41 | 상수 제거 |

---

### CLIENT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:113 → `ON Bcc.CLIENT_CODE = IH.CLIENT_CODE AND BCC.STATUS = 'Y'` (해외 매입)
- VW_PDA_WID_LIST_LOTTE:201 → 동일 (국내 매입)
- B_CLIENT_CHARGE 테이블과 W_GOODS_IH 테이블 연결 키
- **DE_CLIENT 함수 인자로 사용**
- VW_PDA_WID_LIST_LOTTE:70 → `DE_CLIENT(IH.CLIENT_CODE) AS CLIENTNAME` (해외 매입)
- VW_PDA_WID_LIST_LOTTE:158 → 동일 (국내 매입)

#### 3. 안드로이드 소스 사용: O
- **Goodswet 조회 조건으로 사용**
- ShipmentActivity.java:2953 → `DBHandler.selectqueryListGoodsWetInfo(mContext, ..., arSM.get(i).getCLIENT_CODE())`
- ShipmentActivity.java:3135 → 동일
- ShipmentActivity.java:3938 → `DBHandler.selectqueryGoodsWet(..., si.getCLIENT_CODE())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW JOIN 조건 필수**: B_CLIENT_CHARGE 테이블 연결에 필수
- **VIEW 함수 인자 필수**: DE_CLIENT()로 출고업체명 조회
- **Goodswet 조회 조건**: 계근 정보 조회 시 사용
- **제거 시 VIEW 구조 변경 및 조회 로직 변경 필요**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 70, 113, 158, 201 | **JOIN/함수 인자 - 제거 불가** |
| search_shipment_lotte.jsp | 54, 90 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 2953, 3135, 3938 | **조회 조건 - 제거 불가** |
| DBHandler.java | 51, 127, 182, 248, 294, 352, 400, 680, 721 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 22, 215-220 | 필드, getter/setter 제거 |
| DBInfo.java | 42 | 상수 제거 |

---

### CLIENTNAME

#### 1. UI 노출: O
- **리스트 화면에 출고업체명 표시**
- ShipmentListAdapter.java:141 → `holder.position.setText(arSrc.get(pos).getCLIENTNAME())`
- **상세 다이얼로그에 출고업체명 표시**
- ShipmentActivity.java:3794 → `detail_edit_position_name.setText(si.getCLIENTNAME())`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:71, 159 → `DE_CLIENT(IH.CLIENT_CODE) AS CLIENTNAME` (단순 조회)

#### 3. 안드로이드 소스 사용: O
- **라벨 프린터 출력에 사용**
- ShipmentActivity.java:2055-2064 → 출고업체명 분기 처리 (이마트, 신세계백화점, EVERY, E/T)
- ShipmentActivity.java:2107, 2123 → 출고업체명 길이 체크 (라벨 출력)
- ShipmentActivity.java:2553, 2771 → `pointName = si.CLIENTNAME.toString()` (라벨 출력)
- **위치 리스트 구성에 사용**
- ShipmentActivity.java:3020 → `list_position.add(arSM.get(i).getCLIENTNAME() + " / " + ...)` (위치 리스트)
- ShipmentActivity.java:3175 → `list_position.add(arSM.get(i).getCLIENTNAME())` (위치 리스트)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 표시 필수**: 리스트에서 출고업체명 표시
- **라벨 프린터 출력 핵심**: 라벨에 출고업체명 출력, 업체명 분기 처리
- **위치 리스트 구성**: 출고업체별 작업 관리
- **제거 시 출고업체 식별 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 71, 159 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 55, 91 | SELECT/출력에서 제거 |
| ShipmentListAdapter.java | 141 | **UI 표시 - 제거 불가** |
| ShipmentActivity.java | 1684, 2055-2123, 2517, 2553, 2642, 2771, 2803, 3019-3020, 3175, 3794 | **라벨 출력/리스트 - 제거 불가** |
| DBHandler.java | 52, 128, 183, 249, 295, 353, 401, 681, 722 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 23, 223-228 | 필드, getter/setter 제거 |
| DBInfo.java | 43 | 상수 제거 |

---

### CENTERNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 setText() 표시 없음
- 라벨 프린터 출력에만 사용

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:73, 161 → `wmoi.CENTER_NAME AS CENTERNAME` (단순 SELECT)

#### 3. 안드로이드 소스 사용: O
- **라벨 출력 분기에 핵심 사용**
- ShipmentActivity.java:367 → `getCENTERNAME().contains("TRD") || ... "WET" || ... "E/T"` (센터명 분기)
- ShipmentActivity.java:839 → 센터 선택 시 로깅
- ShipmentActivity.java:1745 → `si.getCENTERNAME().contains("E/T") || "WET" || "TRD"` (트레이더스 납품 분기)
- **라벨 프린터 출력**
- ShipmentActivity.java:2086-2093 → `WoosimCmd.getTTFcode(..., si.CENTERNAME)` (센터명 출력)
- **로컬 DB 조회 조건**
- DBHandler.java:152 → `WHERE CENTERNAME = '` + center_name (센터별 조회)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **라벨 출력 분기 핵심**: 센터명에 따라 라벨 출력 로직 분기 (TRD, WET, E/T 등)
- **라벨 프린터 출력**: 라벨에 센터명 출력
- **센터별 조회 조건**: 로컬 DB에서 센터별 데이터 조회
- **제거 시 센터별 작업 분기 및 라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 73, 161 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 56, 91 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 367, 791, 839, 1684, 1745, 2086-2093, 2517, 2642 | **라벨 출력/분기 - 제거 불가** |
| DBHandler.java | 53, 129, 152, 184, 250, 296, 354, 402, 682, 723 | **조회 조건 - 제거 불가** |
| Shipments_Info.java | 24, 231-236 | 필드, getter/setter 제거 |
| DBInfo.java | 44 | 상수 제거 |

---

### ITEM_SPEC

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 setText() 표시 없음
- 라벨 프린터 출력에만 사용

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:74, 162 → `WR.ITEM_SPEC` (스펙, 단순 SELECT)

#### 3. 안드로이드 소스 사용: O
- **라벨 프린터 출력에 핵심 사용**
- ShipmentActivity.java:1639-1641 → `WoosimCmd.getTTFcode(..., si.EMARTITEM + " / " + si.ITEM_SPEC)` (상품명/스펙 출력)
- ShipmentActivity.java:1643 → 로그 출력 `si.EMARTITEM + " / " + si.ITEM_SPEC`

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 프린터 출력 필수**: 라벨에 상품명과 함께 스펙(냉장/냉동 등) 출력
- **제거 시 라벨에 스펙 정보 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 74, 162 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 57, 91 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 1639-1643 | **라벨 프린터 출력 - 제거 불가** |
| ProgressDlgShipSearch.java | 203 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 54, 130, 185, 251, 297, 355, 403, 683, 724 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 25, 239-244 | 필드, getter/setter 제거 |
| DBInfo.java | 45 | 상수 제거 |

---

### CT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 setText() 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:75, 163 → `id.CT_CODE AS CT_CODE` (원산지, 단순 SELECT)

#### 3. 안드로이드 소스 사용: O
- **라벨 프린터 출력에 사용**
- ShipmentActivity.java:2609 → `WoosimCmd.getTTFcode(..., si.getCT_CODE())` (원산지 출력)
- ShipmentActivity.java:2798 → 주석 처리됨 (원앤원 지점자리에 원산지 표시 안함)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 프린터 출력**: 라벨에 원산지 코드 출력
- **제거 시 라벨에 원산지 정보 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 75, 163 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 58, 92 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 2609 | **라벨 프린터 출력 - 제거 불가** |
| DBHandler.java | 55, 131, 186, 252, 298, 356, 404, 684, 725 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 26, 247-252 | 필드, getter/setter 제거 |
| DBInfo.java | 46 | 상수 제거 |

---

## 변경 이력

| 날짜       | 컬럼명  | 내용                                         | 작성자 |
| ---------- | ------- | -------------------------------------------- | ------ |
| 2025-12-09 | GI_H_ID | 분석 - **사용** 판정 (VIEW JOIN 조건으로 필수) | -      |
| 2025-12-09 | GI_D_ID | 분석 - **사용** 판정 (핵심 식별자, 서버 전송/DB WHERE/DDL INSERT/UPDATE) | -      |
| 2025-12-09 | EOI_ID | 분석 - **사용** 판정 (JSP ORDER BY, VIEW JOIN/서브쿼리 조건) | -      |
| 2025-12-09 | ITEM_CODE | 분석 - **사용** 판정 (VIEW 함수 인자, 서버 전송, DDL WHERE 조건) | -      |
| 2025-12-09 | ITEM_NAME | 분석 - **사용** 판정 (UI 화면 표시 - 상품명) | -      |
| 2025-12-09 | EMARTITEM_CODE | 분석 - **사용** 판정 (VIEW JOIN 조건, 바코드 생성 핵심) | -      |
| 2025-12-09 | EMARTITEM | 분석 - **사용** 판정 (라벨 프린터 상품명 출력 핵심) | -      |
| 2025-12-09 | GI_REQ_PKG | 분석 - **사용** 판정 (VIEW WHERE 조건, UI 표시, 계근 완료 판단 핵심) | -      |
| 2025-12-09 | GI_REQ_QTY | 분석 - **사용** 판정 (UI 표시 - 출하요청중량, 총 계근요청중량 계산) | -      |
| 2025-12-09 | AMOUNT | 분석 - **미사용** 판정 (단순 경유만 - 제거 가능) | -      |
| 2025-12-09 | GOODS_R_ID | 분석 - **사용** 판정 (VIEW JOIN 조건으로 필수) | -      |
| 2025-12-09 | GR_REF_NO | 분석 - **미사용** 판정 (단순 경유만 - 제거 가능) | -      |
| 2025-12-09 | GI_REQ_DATE | 분석 - **사용** 판정 (VIEW WHERE 조건, 날짜별 조회 핵심) | -      |
| 2025-12-09 | BL_NO | 분석 - **사용** 판정 (UI 표시, BL별 조회/필터링 핵심) | -      |
| 2025-12-09 | BRAND_CODE | 분석 - **사용** 판정 (VIEW 함수 인자, 서버 전송, DDL WHERE 조건) | -      |
| 2025-12-09 | BRANDNAME | 분석 - **미사용** 판정 (단순 경유만 - 제거 가능) | -      |
| 2025-12-09 | CLIENT_CODE | 분석 - **사용** 판정 (VIEW JOIN/함수 인자, Goodswet 조회 조건) | -      |
| 2025-12-09 | CLIENTNAME | 분석 - **사용** 판정 (UI 표시, 라벨 프린터 출력, 위치 리스트 핵심) | -      |
| 2025-12-09 | CENTERNAME | 분석 - **사용** 판정 (라벨 출력 분기, 센터명 출력, 센터별 조회) | -      |
| 2025-12-09 | ITEM_SPEC | 분석 - **사용** 판정 (라벨 프린터에 스펙 정보 출력) | -      |
| 2025-12-09 | CT_CODE | 분석 - **사용** 판정 (라벨 프린터에 원산지 출력) | -      |
| 2025-12-09 | PACKER_CODE | 분석 - **사용** 판정 (VIEW 서브쿼리/함수 인자, 킬코이 제품 분기 핵심) | -      |
| 2025-12-09 | IMPORT_ID_NO | 분석 - **사용** 판정 (VIEW DECODE 대체값, 바코드 생성 핵심) | -      |
| 2025-12-09 | PACKERNAME | 분석 - **미사용** 판정 (단순 경유만 - 제거 가능) | -      |
| 2025-12-09 | PACKER_PRODUCT_CODE | 분석 - **사용** 판정 (VIEW 서브쿼리, 바코드 매칭, DDL INSERT 핵심) | -      |
| 2025-12-09 | BARCODE_TYPE | 분석 - **사용** 판정 (VIEW JOIN 조건, 바코드/라벨 분기 핵심) | -      |
| 2025-12-09 | ITEM_TYPE | 분석 - **사용** 판정 (VIEW WHERE 조건, 계근 방식 분기 핵심) | -      |
| 2025-12-09 | PACKWEIGHT | 분석 - **사용** 판정 (지정중량 계근, 바코드 중량 문자열 생성) | -      |
| 2025-12-09 | BARCODEGOODS | 분석 - **사용** 판정 (바코드 스캔 매칭, 작업 상품코드 검색 핵심) | -      |
| 2025-12-09 | STORE_IN_DATE | 분석 - **사용** 판정 (라벨 프린터 납품일자 출력 핵심) | -      |
| 2025-12-09 | GR_WAREHOUSE_CODE | 분석 - **사용** 판정 (서버 조회 창고 필터링 핵심) | -      |
| 2025-12-09 | EMARTLOGIS_CODE | 분석 - **사용** 판정 (물류 바코드 pBarcode2 생성 핵심) | -      |
| 2025-12-09 | EMARTLOGIS_NAME | 분석 - **미사용** 판정 (상수값 '정보없음', 코드 주석처리됨 - 제거 가능) | -      |
| 2025-12-09 | WH_AREA | 분석 - **사용** 판정 (라벨 프린터 창고 구역 출력) | -      |
| 2025-12-09 | LAST_BOX_ORDER | 분석 - **사용** 판정 (롯데 박스 순번 초기값 계산 핵심) | -      |

---

### PACKER_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **서브쿼리 WHERE 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:85 → `WHERE A.PACKER_CLIENT_CODE = BD.PACKER_CODE` (바코드 조회)
- VW_PDA_WID_LIST_LOTTE:173 → 동일 (국내 매입 쿼리)
- **DE_CLIENT 함수 인자로 사용**
- VW_PDA_WID_LIST_LOTTE:78 → `DE_CLIENT(BD.PACKER_CODE) AS PACKERNAME` (해외 매입)
- VW_PDA_WID_LIST_LOTTE:166 → `DE_CLIENT(OD.PACKER_CODE) AS PACKERNAME` (국내 매입)

#### 3. 안드로이드 소스 사용: O
- **킬코이 제품 분기에 핵심 사용**
- ShipmentActivity.java:347 → `if (getPACKER_CODE().equals("30228") && getSTORE_CODE().equals("9231"))` (킬코이 + 미트센터)
- ShipmentActivity.java:793 → 동일 분기
- ShipmentActivity.java:1699 → 동일 분기 (소비기한 변조 출력)
- **로그 출력에 사용**
- ShipmentActivity.java:344, 883, 1693 → `Log.d/Log.e(...getPACKER_CODE())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW 서브쿼리 조건 필수**: 바코드 조회에 사용
- **VIEW 함수 인자 필수**: DE_CLIENT()로 패커명 조회
- **킬코이 제품 분기 핵심**: 패커코드 "30228"로 킬코이 제품 특수 처리
- **제거 시 킬코이 제품 라벨 출력 로직 변경 및 VIEW 구조 변경 필요**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 78, 85, 166, 173 | **서브쿼리/함수 인자 - 제거 불가** |
| search_shipment_lotte.jsp | 60, 92 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 344, 347, 793, 883, 1693, 1699 | **킬코이 분기 - 제거 불가** |
| DBHandler.java | 57, 133, 188, 254, 300, 358, 406 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 28, 263-268 | 필드, getter/setter 제거 |
| DBInfo.java | 48 | 상수 제거 |

---

### IMPORT_ID_NO

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **DECODE 함수에서 사용**
- VW_PDA_WID_LIST_LOTTE:62 → `DECODE(WR.BL_NO, NULL, WR.IMPORT_ID_NO, WR.BL_NO) BL_NO` (해외 매입)
- VW_PDA_WID_LIST_LOTTE:150 → 동일 (국내 매입)
- BL_NO가 NULL일 때 수입식별번호를 대신 사용

#### 3. 안드로이드 소스 사용: O
- **바코드 생성에 핵심 사용**
- ShipmentActivity.java:1877-1878 → `pBarcode = ... + si.getIMPORT_ID_NO()` (M0 바코드)
- ShipmentActivity.java:1880-1881 → `pBarcode2 = ... + si.getIMPORT_ID_NO()` (물류 바코드)
- ShipmentActivity.java:1910-1914 → M3 바코드
- ShipmentActivity.java:1943-1947 → M4 바코드
- ShipmentActivity.java:1976-1977 → E0 바코드
- **로그 출력**
- ShipmentActivity.java:1874, 1907, 1940, 1972 → `Log.d(...수입식별번호 : " + si.getIMPORT_ID_NO())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW DECODE 함수에서 사용**: BL_NO가 NULL일 때 대체값
- **바코드 생성 핵심**: M0, M3, M4, E0 바코드 생성 시 필수 구성요소
- **제거 시 바코드 생성 불가 → 라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 62, 77, 150, 165 | **DECODE 함수 - 제거 불가** |
| search_shipment_lotte.jsp | 59, 92 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 1874-1977 (다수) | **바코드 생성 - 제거 불가** |
| ProgressDlgShipSearch.java | 205 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### PACKERNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:78 → `DE_CLIENT(BD.PACKER_CODE) AS PACKERNAME` (단순 SELECT)
- VW_PDA_WID_LIST_LOTTE:166 → 동일 (국내 매입)

#### 3. 안드로이드 소스 사용: X
- 단순 파싱/저장 외 분기, 계산, 서버 전송 등 사용 없음
- ProgressDlgShipSearch.java:207 → `si.setPACKERNAME(temp[23].toString())` (파싱만)
- DBHandler.java:728 → `si.getPACKERNAME()` (로컬 DB INSERT만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **VIEW에서 조회 → JSP 출력 → 앱 파싱 → 로컬 DB 저장**만 수행
- 화면 표시, 분기 조건, 계산, 서버 전송 어디에도 사용되지 않음
- **제거해도 비즈니스 기능에 영향 없음**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 78, 166 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 61, 93 | SELECT/출력에서 제거 |
| ProgressDlgShipSearch.java | 207 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 58, 134, 189, 255, 301, 359, 407, 687, 728 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 29, 271-276 | 필드, getter/setter 제거 |
| DBInfo.java | 49 | 상수 제거 |

---

### PACKER_PRODUCT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **서브쿼리 WHERE 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:86 → `AND A.PACKER_PRODUCT_CODE = BD.PACKER_PRODUCT_CODE` (바코드 조회)
- VW_PDA_WID_LIST_LOTTE:174 → 동일 (국내 매입)
- **LAST_BOX_ORDER 서브쿼리에서 사용**
- VW_PDA_WID_LIST_LOTTE:101 → `AND W.PACKER_PRODUCT_CODE = BD.PACKER_PRODUCT_CODE` (해외 매입)
- VW_PDA_WID_LIST_LOTTE:189 → 동일 (국내 매입)

#### 3. 안드로이드 소스 사용: O
- **바코드 스캔 후 상품 매칭에 핵심 사용**
- ShipmentActivity.java:1356-1379 → `edit_product_code.setText(bi.getPACKER_PRODUCT_CODE())` (UI 표시)
- ShipmentActivity.java:1358-1379 → `pp_code = bi.getPACKER_PRODUCT_CODE()` (패커상품코드 조건 생성)
- **DB 조회에 사용**
- ShipmentActivity.java:877 → `selectqueryListGoodsWetInfo(..., arSM.get(current_work_position).getPACKER_PRODUCT_CODE())`
- **Goodswets_Info 저장에 사용**
- ShipmentActivity.java:1119 → `gi.setPACKER_PRODUCT_CODE(...getPACKER_PRODUCT_CODE())`

#### 4. DDL 사용: O
- **INSERT 문에 사용**
- insert_goods_wet.jsp:54 → `PACKER_PRODUCT_CODE` INSERT 컬럼

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW 서브쿼리 조건 필수**: 바코드 조회 및 박스순번 계산에 사용
- **바코드 스캔 매칭 핵심**: 스캔한 바코드로 상품 매칭
- **DDL INSERT 필수**: W_GOODS_WET 테이블에 저장
- **제거 시 바코드 스캔 매칭 및 VIEW 구조 변경 필요**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 86, 101, 174, 189 | **서브쿼리 조건 - 제거 불가** |
| search_shipment_lotte.jsp | 62, 93 | SELECT/출력에서 제거 |
| insert_goods_wet.jsp | 54 | **INSERT 컬럼 - 제거 불가** |
| ShipmentActivity.java | 877, 1119, 1356-1440 (다수) | **바코드 매칭 - 제거 불가** |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 30, 279-284 | 필드, getter/setter 제거 |
| DBInfo.java | 50 | 상수 제거 |

---

### BARCODE_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_LIST_LOTTE:117 → `AND beb.BARCODE_TYPE LIKE 'L%'` (해외 매입)
- VW_PDA_WID_LIST_LOTTE:205 → 동일 (국내 매입)
- 롯데마트용 바코드 타입('L%')만 필터링

#### 3. 안드로이드 소스 사용: O
- **바코드 생성 분기에 핵심 사용**
- ShipmentActivity.java:833 → `if(getBARCODE_TYPE().equals("M3") || getBARCODE_TYPE().equals("M4"))` (센터 분기)
- ShipmentActivity.java:1735, 1783 → 동일 분기
- ShipmentActivity.java:1865 → `switch (si.getBARCODE_TYPE())` (바코드 타입별 처리)
- **라벨 출력 분기에 핵심 사용**
- ShipmentActivity.java:2098-2204 → 바코드 타입별 라벨 출력 분기 (M0, M1, M3, M4, M8, M9, E0, E1, E2, E3)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW JOIN 조건 필수**: 롯데마트용 바코드 타입 필터링
- **바코드 생성/라벨 출력 분기 핵심**: 바코드 타입에 따라 완전히 다른 바코드 및 라벨 생성
- **제거 시 바코드 생성 및 라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 117, 205 | **JOIN 조건 - 제거 불가** |
| search_shipment_lotte.jsp | 63, 93 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 833, 1735, 1783, 1865, 2098-2204 (다수) | **바코드/라벨 분기 - 제거 불가** |
| ProgressDlgShipSearch.java | 209 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 31, 287-292 | 필드, getter/setter 제거 |
| DBInfo.java | 51 | 상수 제거 |

---

### ITEM_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW WHERE 조건으로 사용** (국내 매입 쿼리만)
- VW_PDA_WID_LIST_LOTTE:213 → `AND beb.ITEM_TYPE = 'W'` (원료육만 해당)
- 해외 매입은 'S' 상수값, 국내 매입은 'W' 조건 필터

#### 3. 안드로이드 소스 사용: O
- **계근 방식 분기에 핵심 사용**
- ShipmentActivity.java:917 → `if (getITEM_TYPE().equals("W") || getITEM_TYPE().equals("HW"))` (바코드 계근)
- ShipmentActivity.java:970 → `else if (getITEM_TYPE().equals("S"))` (S 타입 처리)
- ShipmentActivity.java:1024 → `else if (getITEM_TYPE().equals("J"))` (지정 중량)
- ShipmentActivity.java:1035 → `if (getITEM_TYPE().equals("B"))` (B 타입 처리)
- **라벨 출력 분기에 사용**
- ShipmentActivity.java:1829 → `if (si.getITEM_TYPE().equals("W") || si.getITEM_TYPE().equals("HW"))` (바코드 생성)
- ShipmentActivity.java:2589 → `if (si.getITEM_TYPE().equals("B"))` (B 타입 라벨)
- ShipmentActivity.java:2676 → `if (si.getITEM_TYPE().equals("W") || si.getITEM_TYPE().equals("S"))` (롯데용)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW WHERE 조건**: 국내 매입 원료육 필터링
- **계근 방식 분기 핵심**: 상품 타입(W, HW, S, J, B)에 따라 완전히 다른 계근 프로세스
- **제거 시 계근 방식 판단 불가 → 전체 프로세스 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 81, 169, 213 | **WHERE 조건 - 제거 불가** |
| search_shipment_lotte.jsp | 64, 94 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 917, 970, 1024, 1035, 1829, 2589, 2676 (다수) | **계근 분기 - 제거 불가** |
| ProgressDlgShipSearch.java | 210 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 32, 295-300 | 필드, getter/setter 제거 |
| DBInfo.java | 52 | 상수 제거 |

---

### PACKWEIGHT

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:82, 170 → `NULL AS PACKWEIGHT` (상수값 NULL)

#### 3. 안드로이드 소스 사용: O
- **ITEM_TYPE 'J' 지정중량 계근에 사용**
- ShipmentActivity.java:1026 → `item_weight = arSM.get(current_work_position).getPACKWEIGHT()` (지정 중량 값)
- **바코드 중량 문자열 생성에 사용**
- ShipmentActivity.java:1839 → `print_weight_str = si.getPACKWEIGHT()` (W/HW 타입)
- ShipmentActivity.java:2685 → `print_weight_str = si.getPACKWEIGHT()` (롯데용)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **지정중량(J타입) 계근에 필수**: 미리 정해진 중량값 사용
- **바코드 중량 문자열 생성**: 일부 케이스에서 사용
- 단, VIEW에서는 현재 NULL 상수값만 반환
- **제거 시 지정중량 타입 처리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 82, 170 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 65, 94 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 1026, 1839, 2685 | **중량 처리 - 제거 불가** |
| ProgressDlgShipSearch.java | 211 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 62, 138, 193, 259, 305, 363, 411, 691, 732 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 33, 303-308 | 필드, getter/setter 제거 |
| DBInfo.java | 53 | 상수 제거 |

---

### BARCODEGOODS

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (서브쿼리로 조회)
- VW_PDA_WID_LIST_LOTTE:83-89 → `(SELECT a.BARCODEGOODS FROM S_BARCODE_INFO A WHERE ...)` (서브쿼리)
- VW_PDA_WID_LIST_LOTTE:171-177 → 동일 (국내 매입)

#### 3. 안드로이드 소스 사용: O
- **바코드 스캔 매칭에 핵심 사용**
- ShipmentActivity.java:1335 → `String bg = bi.getBARCODEGOODS()` (바코드 상품코드)
- ShipmentActivity.java:1340-1349 → 바코드 매칭 로그 및 비교
- ShipmentActivity.java:1419-1430 → 동일
- **작업 상품코드 검색에 사용**
- ShipmentActivity.java:3014 → `work_ppcode = find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type)`

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 스캔 매칭 핵심**: 스캔한 바코드와 상품 바코드 비교
- **작업 상품코드 검색**: 바코드로 작업할 상품 찾기
- **제거 시 바코드 스캔 매칭 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 83-89, 171-177 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 66, 94 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 1335, 1340-1349, 1419-1430, 3014 | **바코드 매칭 - 제거 불가** |
| ProgressDlgShipSearch.java | 212 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 34, 311-316 | 필드, getter/setter 제거 |
| DBInfo.java | 54 (주석처리됨) | 상수 제거 |

---

### STORE_IN_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 setText() 표시 없음
- 라벨 프린터 출력에만 사용

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:90 → `WR.FROM_EXPIRY_DATE AS STORE_IN_DATE` (해외 매입)
- VW_PDA_WID_LIST_LOTTE:178 → 동일 (국내 매입)

#### 3. 안드로이드 소스 사용: O
- **라벨 프린터 납품일자 출력에 핵심 사용**
- ShipmentActivity.java:2283-2284 → `tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + ...` (납품일자 포맷)
- ShipmentActivity.java:2300-2301 → 동일
- ShipmentActivity.java:2310-2311 → 동일
- ShipmentActivity.java:2397-2398 → 동일
- ShipmentActivity.java:2474-2475 → 동일
- ShipmentActivity.java:2615 → 로그 출력

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **라벨 프린터 납품일자 출력 핵심**: "YYYY년 MM월 DD일" 포맷으로 라벨에 출력
- 다양한 라벨 타입에서 납품일자 출력에 사용
- **제거 시 라벨에 납품일자 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 90, 178 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 67, 94 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 2283-2615 (다수) | **라벨 납품일자 - 제거 불가** |
| ProgressDlgShipSearch.java | 213 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 64, 140, 195, 261, 307, 365, 413, 693, 734 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 35, 319-324 | 필드, getter/setter 제거 |
| DBInfo.java | 55 | 상수 제거 |

---

### GR_WAREHOUSE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (WHERE, JOIN, 계산 등에 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:91, 179 → `WR.GR_WAREHOUSE_CODE` (단순 SELECT)

#### 3. 안드로이드 소스 사용: O
- **서버 조회 WHERE 조건에 핵심 사용**
- ProgressDlgShipSearch.java:52 → `data += " AND GR_WAREHOUSE_CODE = 'IN10273'"` (창고1)
- ProgressDlgShipSearch.java:54 → `data += " AND GR_WAREHOUSE_CODE = 'IN60464'"` (창고2)
- ProgressDlgShipSearch.java:56 → `data += " AND GR_WAREHOUSE_CODE = '4001'"` (창고3)
- ProgressDlgShipSearch.java:58 → `data += " AND GR_WAREHOUSE_CODE = '4004'"` (창고4)
- ProgressDlgShipSearch.java:60 → `data += " AND GR_WAREHOUSE_CODE = 'IN63279'"` (창고5)
- searchType에 따라 다른 창고코드로 데이터 필터링

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **서버 조회 창고 필터링 핵심**: searchType에 따라 특정 창고 데이터만 조회
- **제거 시 창고별 데이터 필터링 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 91, 179 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | - | (JSP에서 SELECT 안 함) |
| ProgressDlgShipSearch.java | 52-120 (다수) | **창고 필터 조건 - 제거 불가** |
| DBHandler.java | - | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | - | 필드, getter/setter 제거 |
| DBInfo.java | - | 상수 제거 |

---

### EMARTLOGIS_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 서브쿼리로 조회 (WHERE, JOIN, 계산에 직접 사용 안 함)
- VW_PDA_WID_LIST_LOTTE:92 → `(SELECT NVL(REF_CODE2,'회사코드없음') FROM B_COMMON_CODE ... WHERE bcc.code = wmoi.CENTER_CODE) AS EMARTLOGIS_CODE`
- VW_PDA_WID_LIST_LOTTE:180 → 동일 (국내 매입)
- 롯데 스토어 코드 조회 서브쿼리

#### 3. 안드로이드 소스 사용: O
- **물류 바코드(pBarcode2) 생성에 핵심 사용**
- ShipmentActivity.java:1880-1881 → `pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + ...` (M0 바코드)
- ShipmentActivity.java:1896-1897 → M1 바코드
- ShipmentActivity.java:1913-1914 → M3 바코드
- ShipmentActivity.java:1929-1930 → M4 바코드
- ShipmentActivity.java:1946-1947 → E0 바코드
- ShipmentActivity.java:1962-1963 → E1 바코드
- ShipmentActivity.java:1979-1980 → E2 바코드
- ShipmentActivity.java:1992-1993 → E3 바코드
- ShipmentActivity.java:2008-2009 → M8 바코드
- ShipmentActivity.java:2049-2050 → M9 바코드

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **물류 바코드 생성 핵심**: pBarcode2(물류용 바코드)의 핵심 구성요소
- 롯데 센터 코드를 바코드에 포함
- **제거 시 물류 바코드 생성 불가 → 라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 92, 180 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 68, 95 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 1880-2050 (다수) | **물류 바코드 생성 - 제거 불가** |
| ProgressDlgShipSearch.java | 214 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 65, 141, 196, 262, 308, 366, 414, 694, 735 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 41, 67-69 | 필드, getter/setter 제거 |
| DBInfo.java | 58 | 상수 제거 |

---

### EMARTLOGIS_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (상수값)
- VW_PDA_WID_LIST_LOTTE:93, 181 → `'정보없음' AS EMARTLOGIS_NAME` (상수값 '정보없음')

#### 3. 안드로이드 소스 사용: X
- **주석 처리됨 - 실제 사용 안 함**
- ShipmentActivity.java:2161-2166 → 주석 처리됨 `/*if (si.EMARTLOGIS_NAME.length() > 14) {...}*/`
- ShipmentActivity.java:2218 → 주석 처리됨 `//Log.i(TAG, "===============EMARTLOGIS_NAME============"...)`
- 단순 파싱/저장만 (ProgressDlgShipSearch:215, DBHandler:736)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **없음**
- VIEW에서 상수값 '정보없음'만 반환
- 안드로이드에서 사용 코드가 모두 주석 처리됨
- **단순 경유만 - 제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 93, 181 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 69, 95 | SELECT/출력에서 제거 |
| ProgressDlgShipSearch.java | 215 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 66, 142, 197, 263, 309, 367, 415, 695, 736 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 42, 71-73 | 필드, getter/setter 제거 |
| DBInfo.java | 59 | 상수 제거 |

---

### WH_AREA

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 setText() 표시 없음
- 라벨 프린터 출력에만 사용

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재 (빈 문자열 상수)
- VW_PDA_WID_LIST_LOTTE:94, 182 → `'' AS WH_AREA` (빈 문자열 상수)
- 창고 구역 정보 (현재 빈 값)

#### 3. 안드로이드 소스 사용: O
- **라벨 프린터 창고 구역 출력에 사용**
- ShipmentActivity.java:2243 → `whArea = si.getWH_AREA()` (창고 구역 값 추출)
- ShipmentActivity.java:2246-2248 → `if(whArea != null || !whArea.equals("")){ byteStream.write(WoosimCmd.getTTFcode(65, 65, whArea)); }` (라벨 출력)
- ShipmentActivity.java:2261 → 동일 (단위팩 라벨)
- ShipmentActivity.java:2275-2277 → 동일
- ShipmentActivity.java:2383-2385 → 동일 (다른 라벨 타입)

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **라벨 프린터 창고 구역 출력**: 라벨에 창고 구역(WH_AREA) 출력
- VIEW에서 현재 빈 문자열이지만, 향후 데이터 입력 시 라벨에 출력됨
- **구조상 제거 시 향후 창고 구역 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 94, 182 | SELECT 컬럼에서 제거 |
| search_shipment_lotte.jsp | 70, 95 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 2243-2385 (다수) | **라벨 출력 - 제거 시 창고 구역 출력 불가** |
| ProgressDlgShipSearch.java | 216 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 67, 143, 198, 264, 310, 368, 416, 696, 737 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 43, 75-77 | 필드, getter/setter 제거 |
| DBInfo.java | 60 | 상수 제거 |

---

### LAST_BOX_ORDER

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 복잡한 서브쿼리로 계산되어 SELECT됨
- VW_PDA_WID_LIST_LOTTE:95-104, 183-192 → 박스 순번 연속성 검사 서브쿼리
- `NVL((SELECT curr_order FROM (...) WHERE (t.curr_order = t.next_order + 1) OR (t.curr_order = 1 AND t.next_order = 9999) FETCH FIRST 1 ROW ONLY), 0) AS LAST_BOX_ORDER`
- 최근 데이터부터 박스 순번을 검사하여 정상적인 마지막 박스순번 추출

#### 3. 안드로이드 소스 사용: O
- **롯데 박스 순번 초기값 계산에 핵심 사용**
- ShipmentActivity.java:2653-2654 → `lotte_TryCount = Integer.parseInt(si.LAST_BOX_ORDER) + 1;` (박스 순번 +1)
- ShipmentActivity.java:2656 → `lotteBoxOrder = String.valueOf(lotte_TryCount);` (문자열 변환)
- ShipmentActivity.java:2660-2663 → 9999 순환 처리 (`if(lotte_TryCount > 9999)`)
- ShipmentActivity.java:2668-2670 → 로그 출력 `Log.e(TAG, "======LAST_BOX_ORDER=====" + si.LAST_BOX_ORDER)`

#### 4. DDL 사용: X
- insert_goods_wet.jsp, update_shipment.jsp에 사용 없음
- BOX_ORDER는 insert_goods_wet.jsp에서 INSERT되지만, LAST_BOX_ORDER는 VIEW에서 계산된 값

#### 5. 비즈니스 영향: **있음 (핵심)**
- **롯데 박스 순번 계산 핵심**: 새 박스 출력 시 이전 순번 + 1로 시작
- VIEW에서 연속성 검증된 마지막 박스 순번을 제공
- 1-9999 순환 로직의 시작점
- **제거 시 박스 순번 연속성 보장 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_LIST_LOTTE (VIEW) | 95-104, 183-192 | SELECT 컬럼(서브쿼리)에서 제거 |
| search_shipment_lotte.jsp | 71, 95 | SELECT/출력에서 제거 |
| ShipmentActivity.java | 2653-2670 | **박스 순번 계산 - 제거 불가** |
| ProgressDlgShipSearch.java | 217 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 68, 144, 199, 265, 311, 369, 417, 697, 738 | 스키마/INSERT/SELECT에서 제거 |
| Shipments_Info.java | 44, 79-81 | 필드, getter/setter 제거 |
| DBInfo.java | 61 | 상수 제거 |
