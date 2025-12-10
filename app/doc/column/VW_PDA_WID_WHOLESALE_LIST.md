# VW_PDA_WID_WHOLESALE_LIST 컬럼 사용 여부 분석

## VIEW 정보
- **VIEW 명**: VW_PDA_WID_WHOLESALE_LIST
- **스키마**: HIGHLAND
- **용도**: 도매 계근시 사용
- **관련 JSP**: search_shipment_wholesale.jsp
- **바코드 타입**: NA (고정값)
- **ITEM_TYPE**: S (고정값 - 소수점 2자리 계근)
- **특징**: UNION ALL로 해외매입/국내매입 두 경우 결합

---

## 컬럼 분석 결과

| 컬럼명 | UI 노출 | VIEW 내부 | 안드로이드 소스 | DDL 사용 | 비즈니스 영향 | 판정 |
| --- | --- | --- | --- | --- | --- | --- |
| [GI_H_ID](#gi_h_id) | X | **O** | X | X | **있음** | **사용** |
| [GI_D_ID](#gi_d_id) | X | X | **O** | **O** | **있음** | **사용** |
| [EOI_ID](#eoi_id) | X | X | **O** | X | **있음** | **사용** |
| [ITEM_CODE](#item_code) | X | **O** | **O** | **O** | **있음** | **사용** |
| [ITEM_NAME](#item_name) | **O** | X | **O** | X | **있음** | **사용** |
| [EMARTITEM_CODE](#emartitem_code) | X | X | **O** | X | **있음** | **사용** |
| [EMARTITEM](#emartitem) | **O** | X | **O** | X | **있음** | **사용** |
| [GI_REQ_PKG](#gi_req_pkg) | **O** | X | **O** | X | **있음** | **사용** |
| [GI_REQ_QTY](#gi_req_qty) | **O** | X | **O** | X | **있음** | **사용** |
| [AMOUNT](#amount) | X | X | X | X | 없음 | **미사용** |
| [GOODS_R_ID](#goods_r_id) | X | X | X | X | 없음 | **미사용** |
| [GR_REF_NO](#gr_ref_no) | X | X | X | X | 없음 | **미사용** |
| [GI_REQ_DATE](#gi_req_date) | X | **O** | X | X | **있음** | **사용** |
| [BL_NO](#bl_no) | **O** | X | **O** | X | **있음** | **사용** |
| [BRAND_CODE](#brand_code) | X | X | **O** | **O** | **있음** | **사용** |
| [BRANDNAME](#brandname) | X | X | X | X | 없음 | **미사용** |
| [CLIENT_CODE](#client_code) | X | X | **O** | X | **있음** | **사용** |
| [CLIENTNAME](#clientname) | **O** | X | **O** | X | **있음** | **사용** |
| [CENTERNAME](#centername) | **O** | X | **O** | X | **있음** | **사용** |
| [ITEM_SPEC](#item_spec) | **O** | X | **O** | X | **있음** | **사용** |
| [CT_CODE](#ct_code) | **O** | X | **O** | X | **있음** | **사용** |
| [PACKER_CODE](#packer_code) | X | X | **O** | X | **있음** | **사용** |
| [IMPORT_ID_NO](#import_id_no) | **O** | X | **O** | X | **있음** | **사용** |
| [PACKERNAME](#packername) | X | X | X | X | 없음 | **미사용** |
| [PACKER_PRODUCT_CODE](#packer_product_code) | **O** | X | **O** | X | **있음** | **사용** |
| [BARCODE_TYPE](#barcode_type) | X | X | **O** | X | **있음** | **사용** |
| [ITEM_TYPE](#item_type) | X | X | **O** | X | **있음** | **사용** |
| [PACKWEIGHT](#packweight) | X | X | **O** | X | **있음** | **사용** |
| [BARCODEGOODS](#barcodegoods) | X | X | **O** | X | **있음** | **사용** |
| [STORE_IN_DATE](#store_in_date) | **O** | X | **O** | X | **있음** | **사용** |
| [GR_WAREHOUSE_CODE](#gr_warehouse_code) | X | **O** | X | X | **있음** | **사용** |
| [EMARTLOGIS_CODE](#emartlogis_code) | X | X | **O** | X | **있음** | **사용** |
| [EMARTLOGIS_NAME](#emartlogis_name) | X | X | X | X | 없음 | **미사용** |
| [WH_AREA](#wh_area) | X | X | **O** | X | **있음** | **사용** |

---

## 상세 분석

### GI_H_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_WHOLESALE_LIST:99 → `ON IH.GI_H_ID = ID.GI_H_ID` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:195 → `ON IH.GI_H_ID = ID.GI_H_ID` (국내매입)
- W_GOODS_IH(출고헤더)와 W_GOODS_ID(출고상세) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- 로컬 DB INSERT 경유만 존재
- ShipmentActivity.java에서 getGI_H_ID() 호출 없음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- VIEW에서 출고헤더-출고상세 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 99, 195 | **JOIN 조건 - 제거 불가** |
| search_shipment_wholesale.jsp | 37, 85 | SELECT/출력 제거 |
| DBHandler.java | 35, 111, 166, 705 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 6, 87-92 | 필드, getter/setter 제거 |
| DBInfo.java | 26 | 상수 제거 |

---

### GI_D_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:40 → `ID.GI_D_ID` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:121 → `ID.GI_D_ID` (국내매입)

#### 3. 안드로이드 소스 사용: O (핵심)
- **작업 위치 식별 핵심**
- ShipmentActivity.java:511 → `if (arSM.get(i).getGI_D_ID().toString().equals(gi_d_id.toString()))` (작업 위치 매칭)
- **로컬 DB 조회 조건**
- ShipmentActivity.java:877, 2953, 3135, 3227 → `selectqueryListGoodsWetInfo(mContext, arSM.get(i).getGI_D_ID(), ...)`
- **서버 전송 핵심**
- ShipmentActivity.java:3302-3304 → `qry_where = qry_where + "GI_D_ID = " + arSM.get(i).getGI_D_ID()` (서버 쿼리 조건)
- ShipmentActivity.java:3320, 3430 → `packet += list_send_info.get(i).getGI_D_ID() + "::"` (서버 전송 패킷)
- ShipmentActivity.java:3370, 3502 → `String completeStr = arSM.get(j).getGI_D_ID() + "::" + ...` (완료 전송)
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1116 → `gi.setGI_D_ID(arSM.get(current_work_position).getGI_D_ID())`

#### 4. DDL 사용: O (핵심)
- **INSERT 문 사용**
- insert_goods_wet.jsp:51 → `+ ", GI_D_ID"` (W_GOODS_WET 테이블 INSERT)
- **UPDATE 문 WHERE 조건**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`

#### 5. 비즈니스 영향: **있음 (핵심)**
- **출고상세 식별자**: 모든 계근 작업의 기준 키
- **서버 조회/전송 필수**: WHERE 조건, 패킷 전송에 사용
- **DDL INSERT/UPDATE 핵심**: 계근 데이터 저장 시 필수
- **제거 시 계근 데이터 저장/조회/전송 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 40, 121 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 38, 85 | SELECT/출력 |
| ShipmentActivity.java | 511, 877, 1116, 2953, 3135, 3227, 3302-3304, 3320, 3370, 3430, 3502 | **핵심 로직 - 제거 불가** |
| insert_goods_wet.jsp | 51, 105 | **DDL INSERT/UPDATE - 제거 불가** |
| DBHandler.java | 36, 112, 167, 706 | **로컬 DB - 제거 불가** |
| Shipments_Info.java | 7, 95-100 | 필드, getter/setter |
| DBInfo.java | 27 | 상수 |

---

### EOI_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_WHOLESALE_LIST:42 → `'NA' AS EOI_ID` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:123 → `'NA' AS EOI_ID` (국내매입)
- **도매 출하에서는 이마트 발주번호 없음 → 고정값 'NA'**

#### 3. 안드로이드 소스 사용: O (ORDER BY)
- **로컬 DB ORDER BY 조건**
- DBHandler.java:443, 482 → `ORDER BY EOI_ID ASC` (조회 정렬)
- **JSP ORDER BY 조건**
- search_shipment_wholesale.jsp:72 → `ORDER BY EOI_ID ASC`
- **로컬 DB 저장**
- ProgressDlgShipSearch.java:159 → `si.setEOI_ID(temp[2].toString())` (서버 응답 파싱)
- DBHandler.java:707 → 로컬 DB INSERT

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (구조적)**
- **VIEW에서 고정값 'NA'**: 도매 특성상 이마트 발주번호 없음
- **로컬 DB/JSP ORDER BY 조건**: 조회 정렬에 사용
- **다른 VIEW와 컬럼 구조 통일 필요**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 42, 123 | SELECT 컬럼 (고정값 'NA') |
| search_shipment_wholesale.jsp | 39, 72, 85 | SELECT/ORDER BY/출력 |
| DBHandler.java | 37, 113, 168, 440, 443, 479, 482, 666, 707 | 스키마/SELECT/ORDER BY/INSERT |
| Shipments_Info.java | 8, 103-108 | 필드, getter/setter |
| DBInfo.java | 28 | 상수 |

---

### ITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_WHOLESALE_LIST:105 → `ON WR.ITEM_CODE = BI.ITEM_CODE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:201 → `ON WR.ITEM_CODE = BI.ITEM_CODE` (국내매입)
- W_GOODS_R(입고)와 B_ITEM(상품마스터) 테이블 연결 키

#### 3. 안드로이드 소스 사용: O (핵심)
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1127 → `gi.setITEM_CODE(arSM.get(current_work_position).getITEM_CODE())`
- **서버 전송 패킷**
- ShipmentActivity.java:3330, 3440 → `packet += list_send_info.get(i).getITEM_CODE() + "::"`
- ShipmentActivity.java:3370, 3502 → `completeStr = GI_D_ID + "::" + ITEM_CODE + "::" + BRAND_CODE + ...`

#### 4. DDL 사용: O (핵심)
- **UPDATE 문 WHERE 조건**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet.jsp:113 → `pstmt.setString(6, splitData[10]); // ITEM_CODE`

#### 5. 비즈니스 영향: **있음 (핵심)**
- **VIEW JOIN 조건**: 상품 정보 조회 필수
- **서버 전송 필수**: 완료 패킷 전송에 사용
- **DDL UPDATE WHERE 조건**: 계근 데이터 저장 시 필수
- **제거 시 계근 데이터 저장/전송 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 44, 105, 125, 201 | SELECT 컬럼 및 **JOIN 조건 - 제거 불가** |
| search_shipment_wholesale.jsp | 40, 85 | SELECT/출력 |
| ShipmentActivity.java | 1127, 3330, 3370, 3440, 3502 | **핵심 로직 - 제거 불가** |
| insert_goods_wet.jsp | 105, 113 | **DDL UPDATE WHERE - 제거 불가** |
| DBHandler.java | 39, 115, 170, 708 | **로컬 DB - 제거 불가** |
| Shipments_Info.java | 9, 111-116 | 필드, getter/setter |
| DBInfo.java | 29 | 상수 |

---

### ITEM_NAME

#### 1. UI 노출: O
- **화면에 상품명 표시**
- ShipmentActivity.java:1573 → `edit_product_name.setText(arSM.get(work_position).getITEM_NAME())`
- ShipmentActivity.java:3171 → `edit_product_name.setText(arSM.get(0).getITEM_NAME().toString())`
- ShipmentActivity.java:3795 → `detail_edit_ppname.setText(si.getITEM_NAME())`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:46 → `DE_ITEM(ID.ITEM_CODE) AS ITEM_NAME` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:127 → `DE_ITEM(ID.ITEM_CODE)` (국내매입)
- 사용자 정의 함수로 상품코드에서 상품명 조회

#### 3. 안드로이드 소스 사용: O (UI 표시)
- **상품명 화면 표시**: edit_product_name, detail_edit_ppname 등
- **로컬 DB 저장**
- ProgressDlgShipSearch.java → 서버 응답 파싱
- DBHandler.java → 로컬 DB INSERT

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (UI 필수)**
- **화면 표시 필수**: 상품명은 작업자가 반드시 확인해야 함
- **제거 시 상품 식별 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 46, 127 | SELECT 컬럼 (DE_ITEM 함수) |
| search_shipment_wholesale.jsp | 41, 86 | SELECT/출력 |
| ShipmentActivity.java | 1573, 3171, 3795 | **UI 표시 - 제거 불가** |
| DBHandler.java | 40, 116, 171, 709 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 10, 119-124 | 필드, getter/setter |
| DBInfo.java | 30 | 상수 |

---

### EMARTITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_WHOLESALE_LIST:48 → `'NA' AS EMARTITEM_CODE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:129 → `'NA' AS EMARTITEM_CODE` (국내매입)
- **도매 출하에서는 이마트 상품코드 없음 → 고정값 'NA'**

#### 3. 안드로이드 소스 사용: O (바코드 생성 핵심)
- **바코드 생성에 사용**
- ShipmentActivity.java:1620-1621 → P0 바코드: `pBarcode = si.getEMARTITEM_CODE() + print_weight_str + "00" + now`
- ShipmentActivity.java:1877-1878, 1893-1894, 1910-1911, 1926-1927, 1943-1944 → M8/M0/M9 바코드
- **다양한 바코드 타입별 생성 로직에 사용**
- **Goodswet 저장**
- ShipmentActivity.java:1125 → `gi.setEMARTITEM_CODE(arSM.get(current_work_position).getEMARTITEM_CODE())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (바코드 핵심)**
- **바코드 생성 필수**: 모든 바코드 타입에서 상품코드로 사용
- **도매에서는 'NA' 고정**: 다른 VIEW와 컬럼 구조 통일
- **제거 시 바코드 생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 48, 129 | SELECT 컬럼 (고정값 'NA') |
| search_shipment_wholesale.jsp | 42, 86 | SELECT/출력 |
| ShipmentActivity.java | 1125, 1588, 1620-1621, 1871-1989 등 다수 | **바코드 생성 - 제거 불가** |
| DBHandler.java | 41, 117, 172, 710 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 11, 127-132 | 필드, getter/setter |
| DBInfo.java | 31 | 상수 |

---

### EMARTITEM

#### 1. UI 노출: O
- **라벨 인쇄 시 상품명으로 표시**
- ShipmentActivity.java:1638-1641 → `byteStream.write(WoosimCmd.getTTFcode(40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC))`
- ShipmentActivity.java:1643 → `Log.i(TAG, "상품명 / 냉장냉동 : " + si.EMARTITEM + " / " + si.ITEM_SPEC)`
- 라벨에 이마트 상품명 + 스펙(냉장/냉동) 출력

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_WHOLESALE_LIST:49 → `'NA' AS EMARTITEM` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:130 → `'NA' AS EMARTITEM` (국내매입)
- **도매 출하에서는 이마트 상품명 없음 → 고정값 'NA'**

#### 3. 안드로이드 소스 사용: O (라벨 인쇄)
- **라벨 인쇄 상품명**
- ShipmentActivity.java:1638-1643 → 라벨에 상품명 출력
- **로그 출력**
- ShipmentActivity.java:1588, 1685 → `Log.d(TAG, "상품명 : '" + si.EMARTITEM + "'")`
- **Goodswet 저장**
- ShipmentActivity.java:1126 → `gi.setEMARTITEM(arSM.get(current_work_position).getEMARTITEM())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (라벨 인쇄)**
- **라벨 상품명 표시**: 바코드 라벨에 상품명 인쇄
- **도매에서는 'NA' 고정**: 다른 VIEW와 컬럼 구조 통일
- **제거 시 라벨 상품명 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 49, 130 | SELECT 컬럼 (고정값 'NA') |
| search_shipment_wholesale.jsp | 43, 86 | SELECT/출력 |
| ShipmentActivity.java | 1126, 1588, 1638-1643, 1685 | **라벨 인쇄 - 제거 불가** |
| DBHandler.java | 42, 118, 173, 711 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 12, 135-140 | 필드, getter/setter |
| DBInfo.java | 32 | 상수 |

---

### GI_REQ_PKG

#### 1. UI 노출: O
- **요청수량/계근수량 표시**
- ShipmentActivity.java:1207 → `edit_wet_count.setText(arSM.get(current_work_position).getGI_REQ_PKG() + " / " + arSM.get(current_work_position).getPACKING_QTY())`
- ShipmentActivity.java:1570 → 동일 패턴 UI 표시
- ShipmentActivity.java:3797 → `detail_edit_count.setText(si.getGI_REQ_PKG() + " / " + si.getPACKING_QTY())`
- ShipmentActivity.java:4018 → 상세화면 표시
- **리스트 어댑터 표시**
- ShipmentListAdapter.java:142 → `holder.count.setText(arSrc.get(pos).getGI_REQ_PKG() + "/" + arSrc.get(pos).getPACKING_QTY())`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:51 → `ID.GI_REQ_PKG` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:132 → `ID.GI_REQ_PKG` (국내매입)

#### 3. 안드로이드 소스 사용: O (핵심)
- **계근 완료 여부 판정 (핵심)**
- ShipmentActivity.java:772 → `!arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))` (계근 미완료 체크)
- ShipmentActivity.java:862, 1104, 1244, 3247 → 동일 패턴 완료 판정
- ShipmentActivity.java:3043, 3043 → 계근 미완료 건 체크
- **센터별 총 계근요청수량 계산**
- ShipmentActivity.java:3030, 3185 → `centerTotalCount += Integer.parseInt(arSM.get(i).getGI_REQ_PKG())`
- **전송 완료 체크**
- ShipmentActivity.java:3369, 3501 → `arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())` (전송 개수와 요청 개수 비교)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **계근 완료 판정 핵심**: 요청수량과 실제 계근수량 비교로 작업 완료 여부 결정
- **UI 필수 표시**: 작업자에게 남은 작업량 표시
- **센터별 집계**: 총 요청수량 계산에 사용
- **제거 시 계근 완료 판정 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 51, 132 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 44, 86 | SELECT/출력 |
| ShipmentActivity.java | 772, 862, 1104, 1207, 1244, 1478, 1570, 3030, 3043, 3185, 3247, 3369, 3501, 3797, 4018 | **완료 판정 핵심 - 제거 불가** |
| ShipmentListAdapter.java | 142 | **UI 표시 - 제거 불가** |
| DBHandler.java | 43, 119, 174, 712 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 13, 143-148 | 필드, getter/setter |
| DBInfo.java | 33 | 상수 |

---

### GI_REQ_QTY

#### 1. UI 노출: O
- **요청중량/계근중량 표시**
- ShipmentActivity.java:1208 → `edit_wet_weight.setText(arSM.get(current_work_position).getGI_REQ_QTY() + " / " + arSM.get(current_work_position).getGI_QTY())`
- ShipmentActivity.java:1571 → 동일 패턴 UI 표시
- ShipmentActivity.java:3798 → `detail_edit_weight.setText(si.getGI_REQ_QTY() + " / " + si.getGI_QTY())`
- **리스트 어댑터 표시**
- ShipmentListAdapter.java:146 → `holder.weight.setText(arSrc.get(pos).getGI_REQ_QTY() + "/" + arSrc.get(pos).getGI_QTY())`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:53 → `ID.GI_REQ_QTY` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:134 → `ID.GI_REQ_QTY` (국내매입)

#### 3. 안드로이드 소스 사용: O (핵심)
- **센터별 총 계근요청중량 계산**
- ShipmentActivity.java:3031 → `centerTotalWeight += Double.parseDouble(arSM.get(i).getGI_REQ_QTY())`
- ShipmentActivity.java:3186 → 동일 패턴
- **UI 표시**
- 요청중량 / 실제계근중량 형태로 작업자에게 표시

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 필수 표시**: 요청중량과 실제중량 비교 표시
- **센터별 집계**: 총 요청중량 계산에 사용
- **중량 기반 작업 현황 파악에 필수**
- **제거 시 중량 현황 표시 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 53, 134 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 45, 86 | SELECT/출력 |
| ShipmentActivity.java | 1208, 1571, 3031, 3186, 3798 | **UI/집계 핵심 - 제거 불가** |
| ShipmentListAdapter.java | 146 | **UI 표시 - 제거 불가** |
| DBHandler.java | 44, 120, 175, 713 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 14, 151-156 | 필드, getter/setter |
| DBInfo.java | 34 | 상수 |

---

### AMOUNT

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:55 → `ID.AMOUNT` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:136 → `ID.AMOUNT` (국내매입)
- 출하상품금액

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity.java에서 getAMOUNT() 호출 없음
- 로컬 DB INSERT 경유만 존재 (DBHandler.java:175)
- 실제 비즈니스 로직에서 사용하지 않음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- 금액 정보는 PDA 계근 작업에 불필요
- VIEW에서만 조회하고 실제 사용하지 않음

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 55, 136 | SELECT 컬럼 제거 |
| search_shipment_wholesale.jsp | 46, 87 | SELECT/출력 제거 |
| DBHandler.java | 44, 120, 175, 241, 287, 345, 393, 714 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 15, 159-164 | 필드, getter/setter 제거 |
| DBInfo.java | 35 | 상수 제거 |

---

### GOODS_R_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:57 → `WR.GOODS_R_ID` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:138 → `WR.GOODS_R_ID` (국내매입)
- 입고번호

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity.java에서 getGOODS_R_ID() 호출 없음
- 로컬 DB INSERT 경유만 존재 (DBHandler.java:176)
- 실제 비즈니스 로직에서 사용하지 않음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- 입고번호는 PDA 계근 작업에 불필요
- VIEW에서만 조회하고 실제 사용하지 않음

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 57, 138 | SELECT 컬럼 제거 |
| search_shipment_wholesale.jsp | 47, 87 | SELECT/출력 제거 |
| DBHandler.java | 45, 121, 176, 242, 288, 346, 394, 715 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 16, 167-172 | 필드, getter/setter 제거 |
| DBInfo.java | 36 | 상수 제거 |

---

### GR_REF_NO

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:59 → `WR.GR_REF_NO` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:140 → `WR.GR_REF_NO` (국내매입)
- 창고입고번호

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity.java에서 getGR_REF_NO() 호출 없음
- 로컬 DB INSERT 경유만 존재 (DBHandler.java:177)
- 실제 비즈니스 로직에서 사용하지 않음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- 창고입고번호는 PDA 계근 작업에 불필요
- VIEW에서만 조회하고 실제 사용하지 않음

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 59, 140 | SELECT 컬럼 제거 |
| search_shipment_wholesale.jsp | 48, 87 | SELECT/출력 제거 |
| DBHandler.java | 46, 122, 177, 243, 289, 347, 395, 716 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 17, 175-180 | 필드, getter/setter 제거 |
| DBInfo.java | 37 | 상수 제거 |

---

### GI_REQ_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O (WHERE 조건)
- **VIEW WHERE 조건으로 사용**
- VW_PDA_WID_WHOLESALE_LIST:116 → `AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:212 → `AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` (국내매입)
- **당일 이후 출하요청건만 조회하는 필터 조건**

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity.java에서 getGI_REQ_DATE() 호출 없음
- 로컬 DB INSERT 경유만 존재
- 실제 비즈니스 로직에서 사용하지 않음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (VIEW 필터)**
- **VIEW WHERE 조건 필수**: 당일 이후 출하요청건만 조회
- SELECT 컬럼 자체는 제거 가능하나 WHERE 조건은 유지 필요
- **WHERE 조건 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 61, 116, 142, 212 | SELECT 컬럼 제거 가능, **WHERE 조건 유지** |
| search_shipment_wholesale.jsp | 49, 87 | SELECT/출력 제거 |
| DBHandler.java | 47, 123, 178, 717 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 18, 183-188 | 필드, getter/setter 제거 |
| DBInfo.java | 38 | 상수 제거 |

---

### BL_NO

#### 1. UI 노출: O
- **리스트 어댑터에서 BL번호 뒤 4자리 표시**
- ShipmentListAdapter.java:152 → `holder.bl.setText(arSrc.get(pos).getBL_NO().substring(arSrc.get(pos).getBL_NO().length() - 4, arSrc.get(pos).getBL_NO().length()))`
- ShipmentListAdapter.java:149 → 빈값 체크 `if(arSrc.get(pos).getBL_NO().equals(""))`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:63-64 → `DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) BL_NO` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:144-145 → 동일 패턴 (국내매입)
- 수입식별번호 우선, 없으면 BL번호 사용

#### 3. 안드로이드 소스 사용: O (핵심)
- **BL번호 기준 작업 그룹핑**
- ShipmentActivity.java:772 → `if (temp_bl_no.equals(arSM.get(i).getBL_NO()) && ...)` (동일 BL번호 건 체크)
- ShipmentActivity.java:1317-1318 → 바코드 스캔 시 BL번호 매칭 확인
- ShipmentActivity.java:1564, 1569 → BL번호 리스트 관리
- **스피너 BL번호 선택**
- ShipmentActivity.java:3193-3199 → BL번호 리스트 구성
- ShipmentActivity.java:3215 → 스피너에서 현재 BL번호 선택

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **BL번호 기준 작업 그룹핑**: 동일 BL건 묶어서 작업
- **UI 표시 필수**: 작업자에게 BL번호 식별 정보 제공
- **제거 시 BL기준 작업 관리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 63-64, 144-145 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 50, 87 | SELECT/출력 |
| ShipmentActivity.java | 772, 1317-1318, 1544, 1564, 1569, 3193-3199, 3215 | **BL기준 작업관리 - 제거 불가** |
| ShipmentListAdapter.java | 149, 152 | **UI 표시 - 제거 불가** |
| DBHandler.java | 48, 124, 179, 718 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 19, 191-196 | 필드, getter/setter |
| DBInfo.java | 39 | 상수 |

---

### BRAND_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:65 → `ID.BRAND_CODE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:146 → `ID.BRAND_CODE` (국내매입)

#### 3. 안드로이드 소스 사용: O (핵심)
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1128 → `gi.setBRAND_CODE(arSM.get(current_work_position).getBRAND_CODE())`
- **서버 전송 패킷**
- ShipmentActivity.java:3331, 3441 → `packet += list_send_info.get(i).getBRAND_CODE() + "::"`
- ShipmentActivity.java:3370, 3502 → `completeStr = GI_D_ID + "::" + ITEM_CODE + "::" + BRAND_CODE + "::" + REG_ID`

#### 4. DDL 사용: O (핵심)
- **UPDATE 문 WHERE 조건**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet.jsp:114 → `pstmt.setString(7, splitData[11]); // BRAND_CODE`

#### 5. 비즈니스 영향: **있음 (핵심)**
- **DDL UPDATE WHERE 조건**: 계근 데이터 저장 시 필수
- **서버 전송 필수**: 완료 패킷 전송에 사용
- **제거 시 계근 데이터 저장/전송 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 65, 146 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 51, 87 | SELECT/출력 |
| ShipmentActivity.java | 1128, 3331, 3370, 3441, 3502 | **서버 전송 핵심 - 제거 불가** |
| insert_goods_wet.jsp | 105, 114 | **DDL UPDATE WHERE - 제거 불가** |
| DBHandler.java | 49, 125, 180, 719 | **로컬 DB - 제거 불가** |
| Shipments_Info.java | 20, 199-204 | 필드, getter/setter |
| DBInfo.java | 40 | 상수 |

---

### BRANDNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:67 → `DE_COMMON('BRAND', ID.BRAND_CODE) AS BRANDNAME` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:148 → 동일 패턴 (국내매입)
- 공통코드 함수로 브랜드명 조회

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity.java에서 getBRANDNAME() 호출 없음
- 로컬 DB INSERT 경유만 존재
- 실제 비즈니스 로직에서 사용하지 않음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- 브랜드명은 PDA 계근 작업에서 표시하지 않음
- BRAND_CODE만 사용하고 명칭은 불필요

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 67, 148 | SELECT 컬럼 제거 |
| search_shipment_wholesale.jsp | 52, 88 | SELECT/출력 제거 |
| DBHandler.java | 50, 126, 181, 720 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 21, 207-212 | 필드, getter/setter 제거 |
| DBInfo.java | 41 | 상수 제거 |

---

### CLIENT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:69 → `IH.CLIENT_CODE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:150 → `IH.CLIENT_CODE` (국내매입)
- 출고업체코드

#### 3. 안드로이드 소스 사용: O (로컬 DB 조회 조건)
- **로컬 DB 조회 조건으로 사용**
- ShipmentActivity.java:2953 → `DBHandler.selectqueryListGoodsWetInfo(mContext, arSM.get(i).getGI_D_ID(), arSM.get(i).getPACKER_PRODUCT_CODE(), arSM.get(i).getCLIENT_CODE())`
- ShipmentActivity.java:3135 → 동일 패턴
- ShipmentActivity.java:3938 → 상세화면에서 조회
- **Goodswet 테이블 조회 시 조건으로 사용**

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **로컬 DB 조회 조건**: Goodswet 데이터 조회 시 필수
- **제거 시 로컬 DB 조회 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 69, 150 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 53, 88 | SELECT/출력 |
| ShipmentActivity.java | 2953, 3135, 3938 | **로컬 DB 조회 - 제거 불가** |
| DBHandler.java | 51, 127, 182, 721 | **로컬 DB - 제거 불가** |
| Shipments_Info.java | 22, 215-220 | 필드, getter/setter |
| DBInfo.java | 42 | 상수 |

---

### CLIENTNAME

#### 1. UI 노출: O
- **리스트 어댑터에서 출고업체명 표시**
- ShipmentListAdapter.java:141 → `holder.position.setText(arSrc.get(pos).getCLIENTNAME())`
- **스피너 리스트에서 표시**
- ShipmentActivity.java:3020 → `list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO())`
- ShipmentActivity.java:3175 → `list_position.add(arSM.get(i).getCLIENTNAME())`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:71 → `DE_CLIENT(IH.CLIENT_CODE) AS CLIENTNAME` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:152-153 → 동일 패턴 (국내매입)
- 클라이언트 함수로 업체명 조회

#### 3. 안드로이드 소스 사용: O (핵심)
- **라벨 인쇄 시 매장명 파싱**
- ShipmentActivity.java:2055-2064 → CLIENTNAME에서 매장명 추출 (이마트, 신세계백화점, EVERY, E/T 등 파싱)
- ShipmentActivity.java:2107, 2123 → 길이 체크 후 라벨 출력
- **로그 출력**
- ShipmentActivity.java:1684, 2517, 2642 → `Log.d(TAG, "출고업체명 : '" + si.CLIENTNAME + "'")`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 표시 필수**: 작업자에게 출고 대상 업체 표시
- **라벨 인쇄 매장명 파싱**: 바코드 라벨에 매장명 인쇄
- **제거 시 업체 식별 및 라벨 인쇄 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 71, 152-153 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 54, 88 | SELECT/출력 |
| ShipmentActivity.java | 1684, 2055-2064, 2107, 2123, 2517, 2553, 2642, 2771, 3019-3020, 3175 | **UI/라벨 핵심 - 제거 불가** |
| ShipmentListAdapter.java | 141 | **UI 표시 - 제거 불가** |
| DBHandler.java | 52, 128, 183, 722 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 23, 223-228 | 필드, getter/setter |
| DBInfo.java | 43 | 상수 |

---

### CENTERNAME

#### 1. UI 노출: O
- **라벨 인쇄 시 센터명 출력**
- ShipmentActivity.java:2088, 2093 → `byteStream.write(WoosimCmd.getTTFcode(40, 40, si.CENTERNAME))` (센터명 출력)
- **로그 출력**
- ShipmentActivity.java:1684, 2517, 2642 → `Log.d(TAG, "센터명 : '" + si.CENTERNAME + "'")`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:73 → `DE_CLIENT(IH.CLIENT_CODE) AS CENTERNAME` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:155 → 동일 패턴 (국내매입)
- **도매 VIEW에서는 CLIENTNAME과 동일한 값 사용**

#### 3. 안드로이드 소스 사용: O (핵심)
- **바코드 타입 분기 조건**
- ShipmentActivity.java:367 → `if(arSM.get(current_work_position).getCENTERNAME().contains("TRD") || ...contains("WET") || ...contains("E/T"))`
- ShipmentActivity.java:839 → 센터명 기준 분기 처리
- **라벨 인쇄 분기**
- ShipmentActivity.java:1745 → `if (si.getCENTERNAME().contains("E/T") || ...contains("WET") || ...contains("TRD"))` (트레이더스 납품분)
- ShipmentActivity.java:2086 → 길이 체크 후 라벨 출력

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 타입 분기 조건 핵심**: TRD, WET, E/T 등 센터 유형별 처리 분기
- **라벨 인쇄 필수**: 센터명 라벨 출력
- **제거 시 센터별 분기 처리 및 라벨 인쇄 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 73, 155 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 55, 88 | SELECT/출력 |
| ShipmentActivity.java | 367, 791, 839, 1684, 1745, 2086-2093, 2517, 2642 | **분기조건/라벨 핵심 - 제거 불가** |
| DBHandler.java | 53, 129, 184, 723 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 24, 231-236 | 필드, getter/setter |
| DBInfo.java | 44 | 상수 |

---

### ITEM_SPEC

#### 1. UI 노출: O
- **라벨 인쇄 시 상품명과 함께 스펙 출력**
- ShipmentActivity.java:1639 → `byteStream.write(WoosimCmd.getTTFcode(35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC))`
- ShipmentActivity.java:1641 → 동일 패턴 (폰트 크기 다름)
- **로그 출력**
- ShipmentActivity.java:1643 → `Log.i(TAG, "상품명 / 냉장냉동 : " + si.EMARTITEM + " / " + si.ITEM_SPEC)`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:76 → `WR.ITEM_SPEC` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:160 → `WR.ITEM_SPEC` (국내매입)
- 상품 스펙 (냉장/냉동 구분 등)

#### 3. 안드로이드 소스 사용: O (라벨 인쇄)
- **라벨 인쇄 시 스펙 출력**
- 상품명 뒤에 " / 냉장" 또는 " / 냉동" 형태로 출력
- 작업자에게 상품 보관 조건 정보 제공

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (라벨 인쇄)**
- **라벨 스펙 표시 필수**: 냉장/냉동 구분 정보
- **제거 시 보관 조건 정보 누락**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 76, 160 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 56, 88 | SELECT/출력 |
| ShipmentActivity.java | 1639, 1641, 1643 | **라벨 인쇄 - 제거 불가** |
| DBHandler.java | 54, 130, 185, 724 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 25, 239-244 | 필드, getter/setter |
| DBInfo.java | 45 | 상수 |

---

### CT_CODE

#### 1. UI 노출: O
- **라벨 인쇄 시 원산지 코드 출력**
- ShipmentActivity.java:2609 → `byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(si.getCT_CODE())))`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:77 → `WR.CT_CODE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:162 → `WR.CT_CODE` (국내매입)
- 원산지 코드

#### 3. 안드로이드 소스 사용: O (라벨 인쇄)
- **라벨 인쇄 시 원산지 출력**
- ShipmentActivity.java:2609 → 라벨에 원산지 코드 인쇄
- ShipmentActivity.java:2798 → 원앤원 분기에서 주석 처리됨 (현재 미사용)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (라벨 인쇄)**
- **라벨 원산지 표시**: 상품 원산지 정보 인쇄
- **제거 시 원산지 정보 누락**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 77, 162 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 57, 88 | SELECT/출력 |
| ShipmentActivity.java | 2609, 2798 | **라벨 인쇄 - 제거 불가** |
| DBHandler.java | 55, 131, 186, 725 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 26, 247-252 | 필드, getter/setter |
| DBInfo.java | 46 | 상수 |

---

### PACKER_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:78 → `BD.PACKER_CODE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:164 → `OD.PACKER_CODE` (국내매입)
- 패커(가공업체) 코드

#### 3. 안드로이드 소스 사용: O (분기 조건)
- **특정 패커 분기 조건**
- ShipmentActivity.java:347 → `if (arSM.get(current_work_position).getPACKER_CODE().equals("30228") && ...getSTORE_CODE().equals("9231"))` (킬코이 + 미트센터)
- ShipmentActivity.java:793 → 동일 패턴
- ShipmentActivity.java:1699 → `if (si.getPACKER_CODE().equals("30228") && si.getSTORE_CODE().equals("9231"))` (소비기한 변조 출력)
- **로그 출력**
- ShipmentActivity.java:344, 883, 1693 → 패커코드 체크 로그

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (분기 조건)**
- **특정 패커별 분기 처리**: 킬코이(30228) + 미트센터(9231) 조합 시 특수 처리
- **제거 시 패커별 특수 처리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 78, 164 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 58, 89 | SELECT/출력 |
| ShipmentActivity.java | 344, 347, 793, 883, 1693, 1699 | **분기 조건 - 제거 불가** |
| DBHandler.java | 56, 132, 187, 726 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 27, 255-260 | 필드, getter/setter |
| DBInfo.java | 47 | 상수 |

---

### IMPORT_ID_NO

#### 1. UI 노출: O
- **스피너 리스트에서 표시**
- ShipmentActivity.java:3020 → `list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO())`
- **라벨 인쇄 시 이력번호 출력**
- ShipmentActivity.java:2866 → `byteStream.write(WoosimCmd.getTTFcode(30, 30, "이력(묶음)번호 : " + si.getIMPORT_ID_NO()))`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:79 → `WR.IMPORT_ID_NO` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:165 → `WR.IMPORT_ID_NO` (국내매입)
- 수입식별번호

#### 3. 안드로이드 소스 사용: O (핵심 - 바코드 생성)
- **바코드 생성 핵심 구성요소**
- ShipmentActivity.java:1877-1878 → M8 바코드: `pBarcode = EMARTITEM_CODE + weight + compCode + IMPORT_ID_NO`
- ShipmentActivity.java:1910-1911 → M0 바코드
- ShipmentActivity.java:1943-1944 → M9 바코드
- ShipmentActivity.java:1976-1977, 2005-2006, 2024-2028, 2046-2050 → 다양한 바코드 타입에서 사용
- ShipmentActivity.java:2379-2380, 2456 → 미트센터 바코드
- **바코드2 구성**
- ShipmentActivity.java:2764-2765 → `pBarcode2 = si.getIMPORT_ID_NO()`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드 생성 필수**: 대부분의 바코드 타입에서 수입식별번호 포함
- **라벨 이력번호 표시**: 이력 추적 정보
- **제거 시 바코드 생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 79, 165 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 59, 89 | SELECT/출력 |
| ShipmentActivity.java | 1874-2050 다수, 2379, 2456, 2742, 2764-2765, 2866, 3020 | **바코드 생성 핵심 - 제거 불가** |
| DBHandler.java | 57, 133, 188, 727 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 28, 263-268 | 필드, getter/setter |
| DBInfo.java | 48 | 상수 |

---

### PACKERNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:80 → `DE_CLIENT(BD.PACKER_CODE) AS PACKERNAME` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:166 → `DE_CLIENT(OD.PACKER_CODE) AS PACKERNAME` (국내매입)
- 패커명 (클라이언트 함수로 조회)

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity.java에서 getPACKERNAME() 호출 없음
- 로컬 DB INSERT 경유만 존재
- 실제 비즈니스 로직에서 사용하지 않음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- 패커명은 PDA 계근 작업에서 표시하지 않음
- PACKER_CODE만 사용하고 명칭은 불필요

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 80, 166 | SELECT 컬럼 제거 |
| search_shipment_wholesale.jsp | 60, 89 | SELECT/출력 제거 |
| DBHandler.java | 58, 134, 189, 728 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 29, 271-276 | 필드, getter/setter 제거 |
| DBInfo.java | 49 | 상수 제거 |

---

### PACKER_PRODUCT_CODE

#### 1. UI 노출: O
- **상품코드 화면 표시**
- ShipmentActivity.java:1574 → `edit_product_code.setText(arSM.get(work_position).getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:3172 → `edit_product_code.setText(arSM.get(0).getPACKER_PRODUCT_CODE().toString())`
- ShipmentActivity.java:3796 → `detail_edit_ppcode.setText(si.getPACKER_PRODUCT_CODE())`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:81 → `BD.PACKER_PRODUCT_CODE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:168-170 → `DECODE(OD.PACKER_PRODUCT_CODE, NULL, ID.ITEM_CODE, OD.PACKER_PRODUCT_CODE)` (국내매입)
- 패커 상품코드

#### 3. 안드로이드 소스 사용: O (핵심)
- **로컬 DB 조회 조건**
- ShipmentActivity.java:877 → `selectqueryGoodsWet(mContext, GI_D_ID, PACKER_PRODUCT_CODE)`
- ShipmentActivity.java:2953, 3135 → `selectqueryListGoodsWetInfo(mContext, GI_D_ID, PACKER_PRODUCT_CODE, CLIENT_CODE)`
- ShipmentActivity.java:3227, 3938 → 동일 패턴
- **Goodswet 저장**
- ShipmentActivity.java:1119 → `gi.setPACKER_PRODUCT_CODE(arSM.get(current_work_position).getPACKER_PRODUCT_CODE())`
- **서버 전송 패킷**
- ShipmentActivity.java:3323, 3433 → `packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::"`
- **DB 업데이트**
- ShipmentActivity.java:3396, 3533 → `DBHandler.updatequeryShipment(mContext, GI_D_ID, PACKER_PRODUCT_CODE)`
- **바코드 스캔 매칭**
- ShipmentActivity.java:1342-1443 → 바코드 스캔 시 PACKER_PRODUCT_CODE로 상품 매칭

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **로컬 DB 조회/저장 필수**: Goodswet 테이블 조회 조건
- **서버 전송 필수**: 완료 패킷 전송에 사용
- **바코드 스캔 매칭**: 상품 식별 핵심
- **제거 시 상품 식별/저장/전송 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 81, 168-170 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 61, 89 | SELECT/출력 |
| ShipmentActivity.java | 877, 1119, 1342-1443, 1574, 2953, 3019, 3135, 3172, 3227, 3323, 3396, 3433, 3533, 3796, 3938 | **핵심 로직 - 제거 불가** |
| DBHandler.java | 59, 135, 190, 729 | **로컬 DB - 제거 불가** |
| Shipments_Info.java | 30, 279-284 | 필드, getter/setter |
| DBInfo.java | 50 | 상수 |

---

### BARCODE_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_WHOLESALE_LIST:82 → `'NA' AS BARCODE_TYPE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:173 → `'NA' AS BARCODE_TYPE` (국내매입)
- **도매 출하에서는 바코드 타입 'NA' 고정**

#### 3. 안드로이드 소스 사용: O (핵심 - 라벨 인쇄 분기)
- **라벨 인쇄 타입 분기 (핵심)**
- ShipmentActivity.java:1865 → `switch (si.getBARCODE_TYPE())` (바코드 타입별 인쇄 분기)
- ShipmentActivity.java:833, 1735, 1783 → M3/M4 체크
- ShipmentActivity.java:2098-2297 → M0, M1, M3, M4, M8, M9, E0, E1, E2, E3 등 다양한 바코드 타입 분기
- ShipmentActivity.java:2343 → P0 체크
- ShipmentActivity.java:2349, 2429 → M0 + 미트센터 조합 체크

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **라벨 인쇄 타입 결정 핵심**: 바코드 타입에 따라 라벨 형식 완전히 다름
- **도매에서는 'NA' 고정**: 다른 VIEW와 컬럼 구조 통일
- **제거 시 라벨 인쇄 분기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 82, 173 | SELECT 컬럼 (고정값 'NA') |
| search_shipment_wholesale.jsp | 62, 89 | SELECT/출력 |
| ShipmentActivity.java | 833, 1735, 1783, 1865, 2098-2429 다수 | **라벨 타입 분기 핵심 - 제거 불가** |
| DBHandler.java | 60, 136, 191, 730 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 31, 287-292 | 필드, getter/setter |
| DBInfo.java | 51 | 상수 |

---

### ITEM_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_WHOLESALE_LIST:83 → `'S' AS ITEM_TYPE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:175 → `'S' AS ITEM_TYPE` (국내매입)
- **도매 출하에서는 ITEM_TYPE 'S' 고정** (소수점 2자리 계근)

#### 3. 안드로이드 소스 사용: O (핵심 - 계근 방식 분기)
- **계근 방식 분기 (핵심)**
- ShipmentActivity.java:917 → `if (getITEM_TYPE().equals("W") || getITEM_TYPE().equals("HW"))` (바코드 계근)
- ShipmentActivity.java:970 → `else if (getITEM_TYPE().equals("S"))` (저울 계근, 소수점 2자리)
- ShipmentActivity.java:1024 → `else if (getITEM_TYPE().equals("J"))` (지정 중량 입력)
- ShipmentActivity.java:1035 → `if (getITEM_TYPE().equals("B"))` (박스 계근)
- **라벨 인쇄 분기**
- ShipmentActivity.java:1829 → `if (si.getITEM_TYPE().equals("W") || si.getITEM_TYPE().equals("HW"))`
- ShipmentActivity.java:1837 → W 타입 로그
- ShipmentActivity.java:2589 → `if (si.getITEM_TYPE().equals("B"))`
- ShipmentActivity.java:2676 → 롯데용 분기

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **계근 방식 결정 핵심**: W(바코드), S(저울), J(지정중량), B(박스) 등 계근 방식 분기
- **도매에서는 'S' 고정**: 저울 계근 (소수점 2자리)
- **제거 시 계근 방식 분기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 83, 175 | SELECT 컬럼 (고정값 'S') |
| search_shipment_wholesale.jsp | 63, 90 | SELECT/출력 |
| ShipmentActivity.java | 916-1045, 1797, 1829-1842, 2589, 2676-2688 | **계근 방식 분기 핵심 - 제거 불가** |
| DBHandler.java | 61, 137, 192, 731 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 32, 295-300 | 필드, getter/setter |
| DBInfo.java | 52 | 상수 |

---

### PACKWEIGHT

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_WHOLESALE_LIST:84 → `'NA' AS PACKWEIGHT` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:176 → `'NA' AS PACKWEIGHT` (국내매입)
- **도매 출하에서는 PACKWEIGHT 'NA' 고정**

#### 3. 안드로이드 소스 사용: O (J타입 중량)
- **ITEM_TYPE J일 때 지정 중량으로 사용**
- ShipmentActivity.java:1026 → `item_weight = arSM.get(current_work_position).getPACKWEIGHT()` (J타입 중량)
- **라벨 인쇄 시 중량으로 사용**
- ShipmentActivity.java:1839 → `print_weight_str = si.getPACKWEIGHT()` (J타입 라벨)
- ShipmentActivity.java:2685 → 롯데용 라벨

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (J타입 필수)**
- **ITEM_TYPE J일 때 지정 중량**: 저울 계근 없이 고정 중량 사용
- **도매에서는 'NA' 고정**: S타입(저울계근) 사용
- **제거 시 J타입 중량 처리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 84, 176 | SELECT 컬럼 (고정값 'NA') |
| search_shipment_wholesale.jsp | 64, 90 | SELECT/출력 |
| ShipmentActivity.java | 1026, 1838-1839, 2685 | **J타입 중량 - 제거 불가** |
| DBHandler.java | 62, 138, 193, 732 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 33, 303-308 | 필드, getter/setter |
| DBInfo.java | 53 | 상수 |

---

### BARCODEGOODS

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 서브쿼리로 조회
- VW_PDA_WID_WHOLESALE_LIST:85-91 → `SELECT BARCODEGOODS FROM S_BARCODE_INFO WHERE ...` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:177-187 → `DECODE(..., (SELECT BARCODEGOODS FROM S_BARCODE_INFO ...))` (국내매입)
- S_BARCODE_INFO 테이블에서 바코드 상품 조회

#### 3. 안드로이드 소스 사용: O (바코드 스캔 매칭)
- **바코드 스캔 시 상품 매칭**
- ShipmentActivity.java:1335-1349 → 바코드 스캔 시 BARCODEGOODS로 상품 검색
- ShipmentActivity.java:1419-1430 → 동일 패턴
- **작업 상품 검색**
- ShipmentActivity.java:3014 → `work_ppcode = find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type)`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (바코드 매칭)**
- **바코드 스캔 상품 매칭**: 스캔된 바코드와 상품 연결
- **제거 시 바코드 스캔 매칭 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 85-91, 177-187 | SELECT 서브쿼리 |
| search_shipment_wholesale.jsp | 65, 90 | SELECT/출력 |
| ShipmentActivity.java | 1335-1349, 1419-1430, 3014 | **바코드 매칭 - 제거 불가** |
| DBHandler.java | 63, 139, 194, 733 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 34, 311-316 | 필드, getter/setter |
| DBInfo.java | 54 | 상수 |

---

### STORE_IN_DATE

#### 1. UI 노출: O
- **라벨 인쇄 시 납품일자 출력**
- ShipmentActivity.java:2284 → `String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + ... + "월 " + ... + "일"`
- ShipmentActivity.java:2301, 2311, 2398, 2475, 2616 → 동일 패턴

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_WHOLESALE_LIST:92 → `IH.GI_REQ_DATE AS STORE_IN_DATE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:188 → `IH.GI_REQ_DATE AS STORE_IN_DATE` (국내매입)
- 출하요청일을 납품일자로 사용

#### 3. 안드로이드 소스 사용: O (라벨 인쇄)
- **라벨 인쇄 시 납품일자 출력**
- ShipmentActivity.java:2283-2284 → M9 바코드 타입 납품일자
- ShipmentActivity.java:2300-2301, 2310-2311 → M9 하단 코드 납품일자
- ShipmentActivity.java:2397-2398, 2474-2475 → 미트센터 납품일자
- ShipmentActivity.java:2615-2616 → 원앤원 납품일자
- "YYYY년 MM월 DD일" 형식으로 변환하여 라벨에 인쇄

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (라벨 인쇄)**
- **라벨 납품일자 표시 필수**: 납품 예정일 정보
- **제거 시 납품일자 정보 누락**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 92, 188 | SELECT 컬럼 |
| search_shipment_wholesale.jsp | 66, 90 | SELECT/출력 |
| ShipmentActivity.java | 2283-2284, 2300-2301, 2310-2311, 2397-2398, 2474-2475, 2615-2616 | **라벨 인쇄 - 제거 불가** |
| DBHandler.java | 64, 140, 195, 734 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 35, 319-324 | 필드, getter/setter |
| DBInfo.java | 55 | 상수 |

---

### GR_WAREHOUSE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O (WHERE 조건)
- **VIEW WHERE 조건으로 사용**
- VW_PDA_WID_WHOLESALE_LIST:115 → `AND WR.GR_WAREHOUSE_CODE = '4001'` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:211 → `AND WR.GR_WAREHOUSE_CODE = '4001'` (국내매입)
- **도매 출하는 4001 창고만 조회**

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity.java에서 getGR_WAREHOUSE_CODE() 호출 없음
- ProgressDlgShipSearch.java에서 파싱만 존재
- 실제 비즈니스 로직에서 사용하지 않음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (VIEW 필터)**
- **VIEW WHERE 조건 필수**: 4001 창고만 조회
- **SELECT 컬럼은 불필요**: JSP에서 SELECT하지 않음
- **WHERE 조건 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 93, 115, 189, 211 | SELECT 컬럼 제거 가능, **WHERE 조건 유지** |
| search_shipment_wholesale.jsp | - | SELECT 없음 (33개 컬럼 중 제외) |

---

### EMARTLOGIS_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_WHOLESALE_LIST:94 → `'0000000' AS EMARTLOGIS_CODE` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:190 → `'0000000' AS EMARTLOGIS_CODE` (국내매입)
- **도매 출하에서는 이마트 물류코드 '0000000' 고정**

#### 3. 안드로이드 소스 사용: O (핵심 - 바코드2 생성)
- **바코드2 생성 핵심 구성요소**
- ShipmentActivity.java:1880-1881, 1896-1897, 1913-1914, 1929-1930, 1946-1947, 1962-1963 → `pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + ...`
- ShipmentActivity.java:1979-1980 → `pBarcode2 = si.getEMARTLOGIS_CODE().toString() + IMPORT_ID_NO`
- ShipmentActivity.java:1992-1993 → `pBarcode2 = si.getEMARTLOGIS_CODE()`
- ShipmentActivity.java:2008-2009, 2049-2050 → 동일 패턴
- **미트센터 바코드**
- ShipmentActivity.java:2379-2380, 2456-2457 → 미트센터 바코드 생성
- **분기 조건**
- ShipmentActivity.java:2349, 2429 → `si.getEMARTLOGIS_CODE().equals("0000000")` (미트센터 분기)
- **업체코드로 사용**
- ShipmentActivity.java:2551 → `pointCode = si.EMARTLOGIS_CODE.toString()`
- ShipmentActivity.java:2654 → `pCompCode_lotte = si.EMARTLOGIS_CODE` (롯데 업체코드)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **바코드2 생성 필수**: 이마트 물류코드 기반 바코드 생성
- **도매에서는 '0000000' 고정**: 다른 VIEW와 컬럼 구조 통일
- **제거 시 바코드2 생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 94, 190 | SELECT 컬럼 (고정값 '0000000') |
| search_shipment_wholesale.jsp | 67, 91 | SELECT/출력 |
| ShipmentActivity.java | 1880-2050 다수, 2136-2139, 2349, 2379-2380, 2429, 2456-2457, 2551, 2654 | **바코드2 생성 핵심 - 제거 불가** |
| DBHandler.java | 65, 141, 196, 735 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 36, 327-332 | 필드, getter/setter |
| DBInfo.java | 56 | 상수 |

---

### EMARTLOGIS_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_WHOLESALE_LIST:95 → `'정보없음' AS EMARTLOGIS_NAME` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:191 → `'정보없음' AS EMARTLOGIS_NAME` (국내매입)
- **도매 출하에서는 이마트 물류명 '정보없음' 고정**

#### 3. 안드로이드 소스 사용: X
- ShipmentActivity.java:2161-2166 → **주석 처리됨** (현재 미사용)
- ShipmentActivity.java:2218 → 로그도 주석 처리
- 실제 비즈니스 로직에서 사용하지 않음

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- 이마트 물류명은 현재 주석 처리되어 미사용
- EMARTLOGIS_CODE만 사용하고 명칭은 불필요

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 95, 191 | SELECT 컬럼 제거 |
| search_shipment_wholesale.jsp | 68, 91 | SELECT/출력 제거 |
| DBHandler.java | 66, 142, 197, 736 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 37, 335-340 | 필드, getter/setter 제거 |
| DBInfo.java | 57 | 상수 제거 |

---

### WH_AREA

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_WHOLESALE_LIST:96 → `'' AS WH_AREA` (해외매입)
- VW_PDA_WID_WHOLESALE_LIST:192 → `'' AS WH_AREA` (국내매입)
- **도매 출하에서는 창고 영역 빈값 고정**

#### 3. 안드로이드 소스 사용: O (라벨 인쇄)
- **라벨 인쇄 시 창고 영역 출력**
- ShipmentActivity.java:2325 → `whArea = si.getWH_AREA()`
- ShipmentActivity.java:2407, 2484 → 미트센터 라벨에서 사용
- ShipmentActivity.java:2869 → 원앤원 라벨에서 사용
- ShipmentActivity.java:2957 → 주석 처리된 테스트 코드

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (라벨 인쇄)**
- **라벨 창고 영역 표시**: 창고 위치 정보
- **도매에서는 빈값 고정**: 다른 VIEW와 컬럼 구조 통일
- **제거 시 라벨 창고 영역 정보 누락**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_WHOLESALE_LIST (VIEW) | 96, 192 | SELECT 컬럼 (고정값 '') |
| search_shipment_wholesale.jsp | 69, 91 | SELECT/출력 |
| ShipmentActivity.java | 2325, 2407, 2484, 2869, 2957 | **라벨 인쇄 - 제거 불가** |
| DBHandler.java | 67, 143, 198, 737 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 38, 343-348 | 필드, getter/setter |
| DBInfo.java | 58 | 상수 |

