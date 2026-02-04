# VW_PDA_WID_PRO_LIST 컬럼 사용 여부 분석

## VIEW 정보
- **VIEW 명**: VW_PDA_WID_PRO_LIST
- **스키마**: INNO
- **용도**: 생산투입 계근시 사용
- **관련 JSP**: search_production.jsp
- **바코드 타입**: P0 (고정값)
- **GI_TYPE**: M1 (생산투입)

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
| [EMARTITEM](#emartitem) | X | X | **O** | X | **있음** | **사용** |
| [GI_REQ_PKG](#gi_req_pkg) | **O** | X | **O** | X | **있음** | **사용** |
| [GI_REQ_QTY](#gi_req_qty) | **O** | X | **O** | X | **있음** | **사용** |
| [AMOUNT](#amount) | X | X | X | X | 없음 | **미사용** |
| [GOODS_R_ID](#goods_r_id) | X | **O** | X | X | **있음** | **사용** |
| [GR_REF_NO](#gr_ref_no) | X | X | X | X | 없음 | **미사용** |
| [GI_REQ_DATE](#gi_req_date) | X | X | **O** | X | **있음** | **사용** |
| [BL_NO](#bl_no) | **O** | X | **O** | X | **있음** | **사용** |
| [BRAND_CODE](#brand_code) | X | X | **O** | **O** | **있음** | **사용** |
| [BRANDNAME](#brandname) | X | X | X | X | 없음 | **미사용** |
| [CLIENT_CODE](#client_code) | X | X | **O** | X | **있음** | **사용** |
| [CLIENTNAME](#clientname) | **O** | X | **O** | X | **있음** | **사용** |
| [CENTERNAME](#centername) | X | X | **O** | X | **있음** | **사용** |
| [ITEM_SPEC](#item_spec) | X | X | **O** | X | **있음** | **사용** |
| [CT_CODE](#ct_code) | X | X | **O** | X | **있음** | **사용** |
| [IMPORT_ID_NO](#import_id_no) | X | X | **O** | X | **있음** | **사용** |
| [PACKER_CODE](#packer_code) | X | X | **O** | X | **있음** | **사용** |
| [PACKERNAME](#packername) | X | X | X | X | 없음 | **미사용** |
| [PACKER_PRODUCT_CODE](#packer_product_code) | **O** | X | **O** | X | **있음** | **사용** |
| [BARCODE_TYPE](#barcode_type) | X | X | **O** | X | **있음** | **사용** |
| [ITEM_TYPE](#item_type) | X | X | **O** | X | **있음** | **사용** |
| [PACKWEIGHT](#packweight) | X | X | **O** | X | **있음** | **사용** |
| [BARCODEGOODS](#barcodegoods) | X | X | **O** | X | **있음** | **사용** |
| [STORE_IN_DATE](#store_in_date) | X | X | **O** | X | **있음** | **사용** |
| [GR_WAREHOUSE_CODE](#gr_warehouse_code) | X | X | **O** | X | **있음** | **사용** |
| [EMARTLOGIS_CODE](#emartlogis_code) | X | X | **O** | X | **있음** | **사용** |
| [EMARTLOGIS_NAME](#emartlogis_name) | X | X | X | X | 없음 | **미사용** |

---

## 상세 분석

### GI_H_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_PRO_LIST:54 → `WHERE A.GI_H_ID = B.GI_H_ID`
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
| VW_PDA_WID_PRO_LIST (VIEW) | 54 | **JOIN 조건 - 제거 불가** |
| search_production.jsp | 42, 87 | SELECT/출력 제거 |
| DBHandler.java | 35, 111, 166, 705 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 6, 87-92 | 필드, getter/setter 제거 |
| DBInfo.java | 26 | 상수 제거 |

---

### GI_D_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:5 → `B.GI_D_ID AS GI_D_ID`

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
| VW_PDA_WID_PRO_LIST (VIEW) | 5 | SELECT 컬럼에서 제거 |
| search_production.jsp | 43, 87 | SELECT/출력 제거 |
| ShipmentActivity.java | 511, 877, 1116, 2953, 3135, 3227, 3302-3304, 3320, 3370 | **핵심 로직 - 제거 불가** |
| insert_goods_wet.jsp | 51, 105 | **DDL INSERT/UPDATE - 제거 불가** |
| DBHandler.java | 36, 112, 167, 706 | **로컬 DB - 제거 불가** |
| Shipments_Info.java | 7, 95-100 | 필드, getter/setter |
| DBInfo.java | 27 | 상수 |

---

### EOI_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 고정값으로 존재
- VW_PDA_WID_PRO_LIST:6 → `'1111' AS EOI_ID`
- **생산투입에서는 발주번호 없음 → 고정값 '1111'**

#### 3. 안드로이드 소스 사용: O (ORDER BY)
- **로컬 DB ORDER BY 조건**
- DBHandler.java:443, 482 → `ORDER BY EOI_ID ASC` (조회 정렬)
- **로컬 DB 저장**
- ProgressDlgShipSearch.java:159 → `si.setEOI_ID(temp[2].toString())` (서버 응답 파싱)
- DBHandler.java:707 → 로컬 DB INSERT

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (구조적)**
- **VIEW에서 고정값 '1111'**: 생산투입 특성상 발주번호 없음
- **로컬 DB ORDER BY 조건**: 조회 정렬에 사용
- **다른 VIEW와 컬럼 구조 통일 필요**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 6 | SELECT 컬럼 (고정값 '1111') |
| search_production.jsp | 44, 75, 87 | SELECT/ORDER BY/출력 |
| DBHandler.java | 37, 113, 168, 440, 443, 479, 482, 666, 707 | 스키마/SELECT/ORDER BY/INSERT |
| Shipments_Info.java | 8, 103-108 | 필드, getter/setter |
| DBInfo.java | 28 | 상수 |

---

### ITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_PRO_LIST:57 → `AND B.ITEM_CODE = I.ITEM_CODE`
- W_GOODS_ID(출고상세)와 B_ITEM(상품마스터) 테이블 연결 키

#### 3. 안드로이드 소스 사용: O (핵심)
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1127 → `gi.setITEM_CODE(arSM.get(current_work_position).getITEM_CODE())`
- **서버 전송 패킷**
- ShipmentActivity.java:3330, 3440 → `packet += list_send_info.get(i).getITEM_CODE() + "::"`
- ShipmentActivity.java:3370, 3502 → `completeStr = GI_D_ID + "::" + ITEM_CODE + "::" + BRAND_CODE + ...`
- **로컬 DB INSERT**
- DBHandler.java:708, 1572, 1643, 1716 → Goodswet INSERT에 사용

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
| VW_PDA_WID_PRO_LIST (VIEW) | 7, 57 | SELECT 컬럼 및 **JOIN 조건 - 제거 불가** |
| search_production.jsp | 45, 87 | SELECT/출력 |
| ShipmentActivity.java | 1127, 3330, 3370, 3440, 3502 | **핵심 로직 - 제거 불가** |
| insert_goods_wet.jsp | 105, 113 | **DDL UPDATE WHERE - 제거 불가** |
| DBHandler.java | 39, 115, 170, 708, 1572, 1643, 1716 | **로컬 DB - 제거 불가** |
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
- VW_PDA_WID_PRO_LIST:8 → `I.ITEM_NAME_KR AS ITEM_NAME`
- B_ITEM 테이블에서 한글 상품명 조회

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
| VW_PDA_WID_PRO_LIST (VIEW) | 8 | SELECT 컬럼 |
| search_production.jsp | 46, 88 | SELECT/출력 |
| ShipmentActivity.java | 1573, 3171, 3795 | **UI 표시 - 제거 불가** |
| DBHandler.java | 40, 116, 171, 709 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 10, 119-124 | 필드, getter/setter |
| DBInfo.java | 30 | 상수 |

---

### EMARTITEM_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:9 → `B.ITEM_CODE AS EMARTITEM_CODE`
- **생산투입에서는 ITEM_CODE와 동일값**

#### 3. 안드로이드 소스 사용: O (바코드 생성 핵심)
- **바코드 생성에 사용**
- ShipmentActivity.java:1620-1621 → `pBarcode = si.getEMARTITEM_CODE() + print_weight_str + "00" + now` (P0 바코드)
- ShipmentActivity.java:1877-1878 → `pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + ...`
- **다양한 바코드 타입별 생성 로직에 사용**
- M8, M9, H5, L1 등 모든 바코드 타입에서 상품코드로 사용
- **Goodswet 저장**
- ShipmentActivity.java:1125 → `gi.setEMARTITEM_CODE(arSM.get(current_work_position).getEMARTITEM_CODE())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (바코드 핵심)**
- **바코드 생성 필수**: 모든 바코드 타입에서 상품코드로 사용
- **제거 시 바코드 생성 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 9 | SELECT 컬럼 |
| search_production.jsp | 47, 88 | SELECT/출력 |
| ShipmentActivity.java | 1125, 1588, 1620-1621, 1871-1989 등 다수 | **바코드 생성 - 제거 불가** |
| DBHandler.java | 41, 117, 172, 710 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 11, 127-132 | 필드, getter/setter |
| DBInfo.java | 31 | 상수 |

---

### EMARTITEM

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음 (라벨 출력용)

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:10 → `I.ITEM_NAME_KR AS EMARTITEM`
- **생산투입에서는 ITEM_NAME과 동일값**

#### 3. 안드로이드 소스 사용: O (라벨 출력 핵심)
- **라벨 출력에 상품명 표시**
- ShipmentActivity.java:1638-1641 → `byteStream.write(WoosimCmd.getTTFcode(..., si.EMARTITEM + " / " + si.ITEM_SPEC))`
- ShipmentActivity.java:2152-2155 → `byteStream.write(WoosimCmd.getTTFcode(..., si.EMARTITEM))`
- **라벨 문자열 조합**
- ShipmentActivity.java:2031 → `pBarcodeStr3 = si.EMARTITEM + "," + si.getUSE_NAME()`
- ShipmentActivity.java:2265 → `belowBarcodeString = si.EMARTITEM + "," + si.getUSE_NAME()`
- **Goodswet 저장**
- ShipmentActivity.java:1126 → `gi.setEMARTITEM(arSM.get(current_work_position).getEMARTITEM())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (라벨 출력 핵심)**
- **라벨 출력 필수**: Woosim 프린터로 상품명 출력
- **제거 시 라벨에 상품명 미표시**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 10 | SELECT 컬럼 |
| search_production.jsp | 48, 88 | SELECT/출력 |
| ShipmentActivity.java | 1126, 1588, 1638-1643, 2031, 2152-2158, 2265, 2371-2374, 2448-2451 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 42, 118, 173, 711 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 12, 135-140 | 필드, getter/setter |
| DBInfo.java | 32 | 상수 |

---

### GI_REQ_PKG

#### 1. UI 노출: O
- **화면에 요청수량/완료수량 표시**
- ShipmentActivity.java:1207 → `edit_wet_count.setText(getGI_REQ_PKG() + " / " + getPACKING_QTY())`
- ShipmentActivity.java:1570, 3797, 4018 → 동일 형태로 표시
- **목록 어댑터에 표시**
- ShipmentListAdapter.java:142 → `holder.count.setText(getGI_REQ_PKG() + "/" + getPACKING_QTY())`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:11 → `B.GI_REQ_PKG AS GI_REQ_PKG`

#### 3. 안드로이드 소스 사용: O (핵심 분기 조건)
- **작업 완료 여부 판단**
- ShipmentActivity.java:862 → `if (getGI_REQ_PKG().equals(String.valueOf(getPACKING_QTY())))` (완료 여부)
- ShipmentActivity.java:1104, 1244, 1478, 3043, 3247 → 동일 조건 다수
- **전송 완료 판단**
- ShipmentActivity.java:3369, 3501 → `if (getSAVE_CNT() == Integer.parseInt(getGI_REQ_PKG()))`
- **센터 총 수량 계산**
- ShipmentActivity.java:3030, 3185 → `centerTotalCount += Integer.parseInt(getGI_REQ_PKG())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 표시 필수**: 작업자가 요청/완료 수량 확인
- **작업 완료 판단 핵심**: 요청수량 = 완료수량 비교
- **제거 시 작업 진행률 확인 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 11 | SELECT 컬럼 |
| search_production.jsp | 49, 89 | SELECT/출력 |
| ShipmentActivity.java | 772, 862, 1104, 1207, 1244, 1478, 1570, 3030, 3043, 3185, 3247, 3369, 3501, 3797, 4018 | **핵심 분기 - 제거 불가** |
| ShipmentListAdapter.java | 142 | **목록 표시 - 제거 불가** |
| DBHandler.java | 42, 118, 173, 712 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 13, 143-148 | 필드, getter/setter |
| DBInfo.java | 33 | 상수 |

---

### GI_REQ_QTY

#### 1. UI 노출: O
- **화면에 요청중량/완료중량 표시**
- ShipmentActivity.java:1208 → `edit_wet_weight.setText(getGI_REQ_QTY() + " / " + getGI_QTY())`
- ShipmentActivity.java:1571, 3798, 4019 → 동일 형태로 표시

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:12 → `B.GI_REQ_QTY AS GI_REQ_QTY`

#### 3. 안드로이드 소스 사용: O (중량 계산)
- **센터 총 중량 계산**
- ShipmentActivity.java:3031, 3186 → `centerTotalWeight += Double.parseDouble(getGI_REQ_QTY())`
- **로컬 DB 저장**
- DBHandler.java:713 → 로컬 DB INSERT
- ProgressDlgShipSearch.java:192 → 서버 응답 파싱

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (UI/계산 필수)**
- **UI 표시 필수**: 작업자가 요청/완료 중량 확인
- **센터별 총 중량 계산**: 요약 정보 표시에 사용
- **제거 시 중량 정보 확인 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 12 | SELECT 컬럼 |
| search_production.jsp | 50, 89 | SELECT/출력 |
| ShipmentActivity.java | 1208, 1571, 3031, 3186, 3798, 4019 | **UI/계산 - 제거 불가** |
| DBHandler.java | 43, 119, 174, 672, 713 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 192 | 파싱 |
| Shipments_Info.java | 14, 151-156 | 필드, getter/setter |
| DBInfo.java | 34 | 상수 |

---

### AMOUNT

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:13 → `R.AMOUNT AS AMOUNT`
- W_GOODS_R 테이블에서 금액 조회

#### 3. 안드로이드 소스 사용: X
- **ShipmentActivity.java에서 getAMOUNT() 호출 없음**
- 로컬 DB 저장만 수행 (단순 경유)
- ProgressDlgShipSearch.java:193 → `si.setAMOUNT(temp[9].toString())` (파싱만)
- DBHandler.java:714 → 로컬 DB INSERT (저장만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **단순 경유만 - 실제 사용 없음**
- **제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 13 | SELECT 컬럼에서 제거 |
| search_production.jsp | 51, 89 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 193 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 44, 120, 175, 673, 714 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 15, 159-164 | 필드, getter/setter 제거 |
| DBInfo.java | 35 | 상수 제거 |

---

### GOODS_R_ID

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: O
- **VIEW JOIN 조건으로 사용**
- VW_PDA_WID_PRO_LIST:55 → `AND B.GOODS_R_ID = R.GOODS_R_ID`
- W_GOODS_ID(출고상세)와 W_GOODS_R(입고) 테이블 연결 키

#### 3. 안드로이드 소스 사용: X
- **ShipmentActivity.java에서 getGOODS_R_ID() 호출 없음**
- 로컬 DB 저장만 수행 (단순 경유)
- ProgressDlgShipSearch.java:194 → `si.setGOODS_R_ID(temp[10].toString())` (파싱만)
- DBHandler.java:715 → 로컬 DB INSERT (저장만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음**
- **VIEW JOIN 조건으로 필수**
- VIEW에서 출고상세-입고 연결에 필수
- **VIEW 구조상 제거 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 14, 55 | SELECT 컬럼 및 **JOIN 조건 - 제거 불가** |
| search_production.jsp | 52, 90 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 194 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 45, 121, 176, 674, 715 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 16, 167-172 | 필드, getter/setter 제거 |
| DBInfo.java | 36 | 상수 제거 |

---

### GR_REF_NO

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:15 → `R.GI_D_ID AS GR_REF_NO`
- **생산투입에서는 W_GOODS_R.GI_D_ID를 창고입고번호로 사용**

#### 3. 안드로이드 소스 사용: X
- **ShipmentActivity.java에서 getGR_REF_NO() 호출 없음**
- 로컬 DB 저장만 수행 (단순 경유)
- ProgressDlgShipSearch.java:195 → `si.setGR_REF_NO(temp[11].toString())` (파싱만)
- DBHandler.java:716 → 로컬 DB INSERT (저장만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **단순 경유만 - 실제 사용 없음**
- **제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 15 | SELECT 컬럼에서 제거 |
| search_production.jsp | 53, 90 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 195 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 46, 122, 177, 675, 716 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 17, 175-180 | 필드, getter/setter 제거 |
| DBInfo.java | 37 | 상수 제거 |

---

### GI_REQ_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:16 → `A.GI_REQ_DATE AS GI_REQ_DATE`
- W_GOODS_IH 테이블에서 출하요청일 조회

#### 3. 안드로이드 소스 사용: O (조회 조건)
- **서버 조회 WHERE 조건**
- ProgressDlgShipSearch.java:48 → `WHERE GI_REQ_DATE = '" + Common.selectDay + "'`
- **로컬 DB 조회 조건**
- DBHandler.java:107 → `AND GI_REQ_DATE = " + Common.selectDay`
- **로컬 DB 저장**
- ProgressDlgShipSearch.java:196 → `si.setGI_REQ_DATE(temp[12].toString())` (파싱)
- DBHandler.java:717 → 로컬 DB INSERT

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (조회 조건)**
- **서버/로컬 조회 WHERE 조건**: 날짜별 데이터 필터링
- **제거 시 날짜별 조회 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 16 | SELECT 컬럼 |
| search_production.jsp | 54, 90 | SELECT/출력 |
| ProgressDlgShipSearch.java | 48, 196 | **WHERE 조건 - 제거 불가** |
| DBHandler.java | 47, 107, 123, 178, 676, 717 | **조회 조건 - 제거 불가** |
| Shipments_Info.java | 18, 183-188 | 필드, getter/setter |
| DBInfo.java | 38 | 상수 |

---

### BL_NO

#### 1. UI 노출: O
- **목록 어댑터에 BL번호 표시**
- ShipmentListAdapter.java:149-152 → `holder.bl.setText(getBL_NO().substring(...))`

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:17 → `B.BL_NO AS BL_NO`
- W_GOODS_ID 테이블에서 BL번호 조회

#### 3. 안드로이드 소스 사용: O (핵심 분기)
- **바코드 스캔 매칭**
- ShipmentActivity.java:1318 → `if (BL_NO.equals(arSM.get(current_work_position).getBL_NO()))`
- **작업 위치 검색**
- ShipmentActivity.java:772 → `if (temp_bl_no.equals(arSM.get(i).getBL_NO()) && ...)`
- **BL 목록 관리**
- ShipmentActivity.java:1564, 1569 → `list_bl.add(getBL_NO()); work_bl_no = getBL_NO()`
- ShipmentActivity.java:3193, 3199, 3215 → BL 스피너 선택 처리
- **로컬 DB 조회 조건**
- DBHandler.java:103 → `AND BL_NO = '" + condition + "'`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 표시 필수**: BL번호 목록 표시
- **바코드 스캔 매칭**: 작업 대상 식별
- **BL별 작업 그룹화**: 동일 BL 묶음 처리
- **제거 시 BL별 작업 관리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 17 | SELECT 컬럼 |
| search_production.jsp | 55, 90 | SELECT/출력 |
| ShipmentActivity.java | 772, 1317-1318, 1544, 1564, 1569, 3193, 3199, 3215 | **핵심 분기 - 제거 불가** |
| ShipmentListAdapter.java | 149-152 | **목록 표시 - 제거 불가** |
| DBHandler.java | 48, 103, 124, 179, 677, 718 | **조회 조건 - 제거 불가** |
| ProgressDlgShipSearch.java | 197 | 파싱 |
| Shipments_Info.java | 19, 191-196 | 필드, getter/setter |
| DBInfo.java | 39 | 상수 |

---

### BRAND_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:18 → `R.BRAND_CODE AS BRAND_CODE`
- W_GOODS_R 테이블에서 브랜드코드 조회

#### 3. 안드로이드 소스 사용: O (핵심)
- **Goodswet 저장 시 사용**
- ShipmentActivity.java:1128 → `gi.setBRAND_CODE(arSM.get(current_work_position).getBRAND_CODE())`
- **서버 전송 패킷**
- ShipmentActivity.java:3331, 3441 → `packet += list_send_info.get(i).getBRAND_CODE() + "::"`
- ShipmentActivity.java:3370, 3502 → `completeStr = GI_D_ID + "::" + ITEM_CODE + "::" + BRAND_CODE + ...`

#### 4. DDL 사용: O (핵심)
- **UPDATE 문 WHERE 조건**
- insert_goods_wet.jsp:105 → `WHERE GI_D_ID = ? AND ITEM_CODE = ? AND BRAND_CODE = ?`
- insert_goods_wet.jsp:114 → `pstmt.setString(7, splitData[11]); // BRAND_CODE`

#### 5. 비즈니스 영향: **있음 (핵심)**
- **서버 전송 필수**: 완료 패킷 전송에 사용
- **DDL UPDATE WHERE 조건**: 계근 데이터 저장 시 필수
- **제거 시 계근 데이터 저장/전송 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 18 | SELECT 컬럼 |
| search_production.jsp | 56, 91 | SELECT/출력 |
| ShipmentActivity.java | 1128, 3331, 3370, 3441, 3502 | **핵심 로직 - 제거 불가** |
| insert_goods_wet.jsp | 105, 114 | **DDL UPDATE WHERE - 제거 불가** |
| DBHandler.java | 49, 125, 180, 678, 719 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 20, 199-204 | 필드, getter/setter |
| DBInfo.java | 14 | 상수 (공통) |

---

### BRANDNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:19-22 → 서브쿼리로 브랜드명 조회
```sql
(SELECT CODE_NAME FROM B_COMMON_CODE
 WHERE MASTER_CODE = 'BRAND' AND CODE = R.BRAND_CODE) AS BRANDNAME
```

#### 3. 안드로이드 소스 사용: X
- **ShipmentActivity.java에서 getBRANDNAME() 호출 없음**
- 로컬 DB 저장만 수행 (단순 경유)
- ProgressDlgShipSearch.java:199 → `si.setBRANDNAME(temp[15].toString())` (파싱만)
- DBHandler.java:720 → 로컬 DB INSERT (저장만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **단순 경유만 - 실제 사용 없음**
- **제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 19-22 | SELECT 컬럼에서 제거 (서브쿼리) |
| search_production.jsp | 57, 91 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 199 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 50, 126, 181, 679, 720 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 21, 207-212 | 필드, getter/setter 제거 |
| DBInfo.java | 41 | 상수 제거 |

---

### CLIENT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:23 → `A.CLIENT_CODE AS CLIENT_CODE`
- W_GOODS_IH 테이블에서 출고업체코드 조회

#### 3. 안드로이드 소스 사용: O (조회 조건)
- **로컬 DB 조회 조건**
- ShipmentActivity.java:2953 → `selectqueryListGoodsWetInfo(mContext, getGI_D_ID(), getPACKER_PRODUCT_CODE(), getCLIENT_CODE())`
- ShipmentActivity.java:3135, 3938 → 동일 패턴 사용
- **로컬 DB 저장**
- DBHandler.java:721 → 로컬 DB INSERT

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (조회 조건)**
- **로컬 DB 조회 조건**: Goodswet 정보 조회 시 필터 조건
- **제거 시 계근 데이터 조회 오류 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 23 | SELECT 컬럼 |
| search_production.jsp | 58, 91 | SELECT/출력 |
| ShipmentActivity.java | 2953, 3135, 3938 | **조회 조건 - 제거 불가** |
| DBHandler.java | 51, 127, 182, 680, 721 | 스키마/SELECT/INSERT |
| Shipments_Info.java | 22, 215-220 | 필드, getter/setter |
| DBInfo.java | 42 | 상수 |

---

### CLIENTNAME

#### 1. UI 노출: O
- **목록 어댑터에 출고업체명 표시**
- ShipmentListAdapter.java:141 → `holder.position.setText(arSrc.get(pos).getCLIENTNAME())`
- **상세 화면에 표시**
- ShipmentActivity.java:3794 → `detail_edit_position_name.setText(si.getCLIENTNAME())`

#### 2. VIEW 내부 사용: X
- VIEW에서 서브쿼리로 조회
- VW_PDA_WID_PRO_LIST:24-26 →
```sql
(SELECT CLIENT_FNAME FROM B_CLIENT WHERE CLIENT_CODE = A.CLIENT_CODE) AS CLIENTNAME
```

#### 3. 안드로이드 소스 사용: O (UI/분기)
- **목록 구성**
- ShipmentActivity.java:3020 → `list_position.add(getCLIENTNAME() + " / " + getIMPORT_ID_NO())`
- ShipmentActivity.java:3175 → `list_position.add(getCLIENTNAME())`
- **라벨 분기 조건**
- ShipmentActivity.java:2055-2061 → `if (si.CLIENTNAME.contains("이마트"))` 등 업체명 기반 분기
- **로컬 DB ORDER BY 조건**
- DBHandler.java:154, 267, 372 → `ORDER BY ... CLIENTNAME ASC`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (UI/분기 핵심)**
- **UI 표시 필수**: 목록, 상세 화면에 업체명 표시
- **라벨 출력 분기**: 업체명에 따라 라벨 형식 결정
- **로컬 DB 정렬 조건**: 조회 결과 정렬에 사용
- **제거 시 업체 식별 및 라벨 분기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 24-26 | SELECT 컬럼 (서브쿼리) |
| search_production.jsp | 59, 92 | SELECT/출력 |
| ShipmentActivity.java | 1684, 2055-2061, 3020, 3175, 3794 | **UI/분기 - 제거 불가** |
| ShipmentListAdapter.java | 141 | **목록 표시 - 제거 불가** |
| DBHandler.java | 52, 128, 154, 183, 267, 372, 681, 722 | **ORDER BY - 제거 불가** |
| Shipments_Info.java | 23, 223-228 | 필드, getter/setter |
| DBInfo.java | 43 | 상수 |

---

### CENTERNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음 (라벨 출력용)

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_PRO_LIST:27 → `'하이랜드푸드' AS CENTERNAME`
- **생산투입에서는 센터명이 '하이랜드푸드' 고정**

#### 3. 안드로이드 소스 사용: O (분기 조건/라벨 출력)
- **센터 분기 조건**
- ShipmentActivity.java:367 → `getCENTERNAME().contains("TRD") || getCENTERNAME().contains("WET") || getCENTERNAME().contains("E/T")`
- ShipmentActivity.java:1745 → 트레이더스 납품 분기 조건
- **라벨 출력**
- ShipmentActivity.java:2086-2093 → `byteStream.write(WoosimCmd.getTTFcode(..., si.CENTERNAME))` (라벨에 센터명 출력)
- **로컬 DB 조회 조건**
- DBHandler.java:152 → `WHERE CENTERNAME = '...'`
- **로그 출력**
- ShipmentActivity.java:1684, 2517, 2642 → `Log.d(TAG, "센터명 : '" + si.CENTERNAME + "'")`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (분기/라벨 핵심)**
- **센터 분기 조건**: 트레이더스, WET 등 납품처별 분기 로직
- **라벨 출력**: Woosim 프린터로 센터명 출력
- **로컬 DB 조회 조건**: 센터별 데이터 필터링
- **제거 시 센터별 분기 및 라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 27 | SELECT 컬럼 (고정값 '하이랜드푸드') |
| search_production.jsp | 60, 92 | SELECT/출력 |
| ShipmentActivity.java | 367, 791, 839, 1684, 1745, 1750, 2086-2093, 2517, 2642 | **분기/라벨 - 제거 불가** |
| DBHandler.java | 53, 129, 152, 184, 682, 723 | **조회 조건 - 제거 불가** |
| Shipments_Info.java | 24, 231-236 | 필드, getter/setter |
| DBInfo.java | 44 | 상수 |

---

### ITEM_SPEC

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음 (라벨 출력용)

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:28 → `R.ITEM_SPEC AS ITEM_SPEC`
- W_GOODS_R 테이블에서 스펙(냉장/냉동) 조회

#### 3. 안드로이드 소스 사용: O (라벨 출력)
- **라벨 출력에 상품명/스펙 표시**
- ShipmentActivity.java:1639 → `byteStream.write(WoosimCmd.getTTFcode(35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC))`
- ShipmentActivity.java:1641 → `byteStream.write(WoosimCmd.getTTFcode(40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC))`
- ShipmentActivity.java:1643 → `Log.i(TAG, "상품명 / 냉장냉동 : " + si.EMARTITEM + " / " + si.ITEM_SPEC)`
- **로컬 DB 저장/조회**
- DBHandler.java:185, 297, 403 → `si.setITEM_SPEC(...)` (로컬 DB 조회)
- DBHandler.java:724 → 로컬 DB INSERT
- ProgressDlgShipSearch.java:203 → `si.setITEM_SPEC(temp[19].toString())` (서버 응답 파싱)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (라벨 출력)**
- **라벨 출력 필수**: 상품명과 함께 냉장/냉동 구분 표시
- **제거 시 라벨에 스펙 미표시**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 28 | SELECT 컬럼 |
| search_production.jsp | 61, 92 | SELECT/출력 |
| ShipmentActivity.java | 1639-1643 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 54, 130, 185, 251, 297, 355, 403, 683, 724 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 203 | 파싱 |
| Shipments_Info.java | 25, 239-244 | 필드, getter/setter |
| DBInfo.java | 45 | 상수 |

---

### CT_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음 (라벨 출력용)

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:29 → `R.CT_CODE AS CT_CODE`
- W_GOODS_R 테이블에서 원산지 코드 조회

#### 3. 안드로이드 소스 사용: O (라벨 출력)
- **라벨에 원산지 출력**
- ShipmentActivity.java:2609 → `byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(si.getCT_CODE())))`
- **로컬 DB 저장/조회**
- DBHandler.java:186, 298, 404 → `si.setCT_CODE(...)` (로컬 DB 조회)
- DBHandler.java:725 → 로컬 DB INSERT
- ProgressDlgShipSearch.java:204 → `si.setCT_CODE(temp[20].toString())` (서버 응답 파싱)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (라벨 출력)**
- **라벨 출력 필수**: 원산지 정보 표시
- **제거 시 라벨에 원산지 미표시**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 29 | SELECT 컬럼 |
| search_production.jsp | 62, 93 | SELECT/출력 |
| ShipmentActivity.java | 2609 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 55, 131, 186, 252, 298, 356, 404, 684, 725 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 204 | 파싱 |
| Shipments_Info.java | 26, 247-252 | 필드, getter/setter |
| DBInfo.java | 46 | 상수 |

---

### IMPORT_ID_NO

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음 (바코드/라벨 출력용)

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:30-31 →
```sql
DECODE(B.IMPORT_ID_NO, NULL, '0000', B.IMPORT_ID_NO) AS IMPORT_ID_NO
```
- W_GOODS_ID 테이블에서 수입식별번호 조회, NULL이면 '0000' 기본값

#### 3. 안드로이드 소스 사용: O (바코드 생성 핵심)
- **바코드 생성에 수입식별번호 포함**
- ShipmentActivity.java:1877-1881 → M8 바코드: `pBarcode = EMARTITEM_CODE + weight + compCode + IMPORT_ID_NO`
- ShipmentActivity.java:1910-1914 → M9 바코드
- ShipmentActivity.java:1943-1947, 1976-1980, 2005-2009, 2024-2028, 2046-2050 → 다양한 바코드 타입
- **미트센터 바코드**
- ShipmentActivity.java:2379-2380 → `meatCenterBarcode = ... + IMPORT_ID_NO + EMART_PLANT_CODE`
- ShipmentActivity.java:2456-2457 → 미트센터 바코드
- **라벨 출력**
- ShipmentActivity.java:2612 → `print_weight + "/" + IMPORT_ID_NO.substring(8, 12)` (중량/식별번호 끝4자리)
- ShipmentActivity.java:2764-2765 → 바코드 문자열로 사용
- ShipmentActivity.java:2866 → `"이력(묶음)번호 : " + IMPORT_ID_NO` (라벨 출력)
- **목록 구성**
- ShipmentActivity.java:3020 → `list_position.add(CLIENTNAME + " / " + IMPORT_ID_NO)`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (바코드 핵심)**
- **바코드 생성 필수**: 모든 바코드 타입에서 수입식별번호 포함
- **라벨 출력**: 이력번호 표시
- **목록 구분**: 업체명 + 식별번호로 작업 목록 구성
- **제거 시 바코드 생성 및 이력 추적 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 30-31 | SELECT 컬럼 (DECODE 함수) |
| search_production.jsp | 63, 93 | SELECT/출력 |
| ShipmentActivity.java | 1874-2050, 2379-2457, 2612, 2764-2866, 3020 | **바코드/라벨 - 제거 불가** |
| DBHandler.java | 56, 132, 187, 253, 299, 357, 405, 685, 726 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 205 | 파싱 |
| Shipments_Info.java | 27, 255-260 | 필드, getter/setter |
| DBInfo.java | 47 | 상수 |

---

### PACKER_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:32 → `D.PACKER_CODE AS PACKER_CODE`
- I_OFFER_D 테이블에서 패커 코드 조회

#### 3. 안드로이드 소스 사용: O (핵심 분기 조건)
- **킬코이 제품 분기 조건**
- ShipmentActivity.java:347 → `if (getPACKER_CODE().equals("30228") && getSTORE_CODE().equals("9231"))` (킬코이+미트센터)
- ShipmentActivity.java:793 → 동일 분기 조건 (바코드 스캔 시)
- ShipmentActivity.java:1699 → 동일 분기 조건 (라벨 출력 시)
- **로그 출력**
- ShipmentActivity.java:344, 883, 1693 → `Log.i(TAG, "패커코드 : " + getPACKER_CODE())`
- **생산투입 조회 조건**
- ProductionActivity.java:441 → `packet = "AND a.PACKER_CODE = '" + packerCode + "'"`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (분기 조건 핵심)**
- **패커별 분기 로직**: 킬코이(30228) 등 특정 패커 제품 특별 처리
- **미트센터 납품 분기**: 패커코드 + 스토어코드 조합으로 분기
- **생산투입 조회**: 바코드 스캔 시 패커코드로 조회
- **제거 시 패커별 특별 처리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 32 | SELECT 컬럼 |
| search_production.jsp | 64, 94 | SELECT/출력 |
| ShipmentActivity.java | 344, 347, 793, 883, 1693, 1699 | **분기 조건 - 제거 불가** |
| ProductionActivity.java | 441 | **조회 조건 - 제거 불가** |
| DBHandler.java | 57, 133, 188, 254, 300, 358, 406, 686, 727 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 206 | 파싱 |
| Shipments_Info.java | 28, 263-268 | 필드, getter/setter |
| DBInfo.java | 48 | 상수 |

---

### PACKERNAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:33 → `DE_CLIENT(D.PACKER_CODE) AS PACKERNAME`
- 사용자 정의 함수로 패커 코드에서 패커 이름 조회

#### 3. 안드로이드 소스 사용: X
- **ShipmentActivity.java에서 getPACKERNAME() 호출 없음**
- 로컬 DB 저장만 수행 (단순 경유)
- ProgressDlgShipSearch.java:207 → `si.setPACKERNAME(temp[23].toString())` (파싱만)
- DBHandler.java:728 → 로컬 DB INSERT (저장만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **단순 경유만 - 실제 사용 없음**
- **제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 33 | SELECT 컬럼에서 제거 (DE_CLIENT 함수 호출) |
| search_production.jsp | 65, 94 | SELECT/출력 제거 |
| ProgressDlgShipSearch.java | 207 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 58, 134, 189, 255, 301, 359, 407, 687, 728 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 29, 271-276 | 필드, getter/setter 제거 |
| DBInfo.java | 49 | 상수 제거 |

---

### PACKER_PRODUCT_CODE

#### 1. UI 노출: O
- **상세 화면에 패커 상품코드 표시**
- ShipmentActivity.java:3796 → `detail_edit_ppcode.setText(si.getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:3172 → `edit_product_code.setText(arSM.get(0).getPACKER_PRODUCT_CODE())`
- ShipmentActivity.java:1356, 1375, 1436 → 바코드 스캔 후 상품코드 표시

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:34-35 →
```sql
DECODE(D.PACKER_PRODUCT_CODE, NULL, '0000', D.PACKER_PRODUCT_CODE) AS PACKER_PRODUCT_CODE
```
- I_OFFER_D 테이블에서 패커 상품코드 조회, NULL이면 '0000' 기본값

#### 3. 안드로이드 소스 사용: O (핵심)
- **로컬 DB 조회 조건**
- ShipmentActivity.java:877 → `selectqueryListGoodsWetInfo(mContext, GI_D_ID, PACKER_PRODUCT_CODE)`
- ShipmentActivity.java:2953, 3135, 3227, 3938 → 동일 패턴
- **Goodswet 저장**
- ShipmentActivity.java:1119 → `gi.setPACKER_PRODUCT_CODE(arSM.get(current_work_position).getPACKER_PRODUCT_CODE())`
- **서버 전송 패킷**
- ShipmentActivity.java:3323, 3433 → `packet += PACKER_PRODUCT_CODE + "::"`
- **DB UPDATE**
- ShipmentActivity.java:3396, 3533 → `DBHandler.updatequeryShipment(mContext, GI_D_ID, PACKER_PRODUCT_CODE)`
- **생산투입 조회**
- ProductionActivity.java:441 → `"AND a.PACKER_PRODUCT_CODE = '" + packerProductCode + "'"`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심)**
- **UI 표시**: 상품코드 화면 표시
- **로컬 DB 조회 조건**: 계근 데이터 조회 시 필수
- **서버 전송 패킷**: 완료 데이터 전송에 포함
- **생산투입 조회**: 바코드 스캔 시 상품 조회
- **제거 시 상품별 작업 관리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 34-35 | SELECT 컬럼 (DECODE 함수) |
| search_production.jsp | 66, 95 | SELECT/출력 |
| ShipmentActivity.java | 874, 877, 1119, 1342-1440, 2953, 3019, 3135, 3172, 3227, 3323, 3396, 3433, 3533, 3796, 3938 | **핵심 로직 - 제거 불가** |
| ProductionActivity.java | 441 | **조회 조건 - 제거 불가** |
| DBHandler.java | 59, 135, 190, 256, 302, 360, 408, 688, 729 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 208 | 파싱 |
| Shipments_Info.java | 30, 279-284 | 필드, getter/setter |
| DBInfo.java | 50 | 상수 |

---

### BARCODE_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_PRO_LIST:36 → `'P0' AS BARCODE_TYPE`
- **생산투입은 바코드 타입 'P0' 고정**

#### 3. 안드로이드 소스 사용: O (핵심 분기 조건)
- **바코드 타입별 분기 (switch 문)**
- ShipmentActivity.java:1865 → `switch (si.getBARCODE_TYPE())` (바코드 생성 분기)
- ShipmentActivity.java:2732 → `switch (si.getBARCODE_TYPE())` (라벨 출력 분기)
- **바코드 타입별 조건 분기**
- ShipmentActivity.java:833, 1735, 1783 → M3, M4 타입 분기
- ShipmentActivity.java:2098-2239 → M0, M1, M3, M4, M8, M9, E0-E3 등 다양한 타입 분기
- ShipmentActivity.java:2343 → `if(!si.getBARCODE_TYPE().equals("P0"))` (P0 타입 특별 처리)
- ShipmentActivity.java:2349, 2429 → M0 + 미트센터 조합 분기
- **로그 출력**
- ShipmentActivity.java:2730-2731 → `Log.d(TAG, "바코드 타입 : " + getBARCODE_TYPE())`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심 분기)**
- **바코드 생성 분기 핵심**: 타입별로 바코드 형식이 완전히 다름
- **라벨 출력 분기**: 타입별로 라벨 레이아웃 다름
- **P0 타입 특별 처리**: 생산투입 전용 처리 로직
- **제거 시 바코드/라벨 출력 분기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 36 | SELECT 컬럼 (고정값 'P0') |
| search_production.jsp | 67, 95 | SELECT/출력 |
| ShipmentActivity.java | 831-833, 1734-1735, 1782-1783, 1857, 1865, 2098-2349, 2429, 2730-2732, 2800 | **분기 조건 - 제거 불가** |
| DBHandler.java | 60, 136, 191, 257, 303, 361, 409, 689, 730 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 209 | 파싱 |
| Shipments_Info.java | 31, 287-292 | 필드, getter/setter |
| DBInfo.java | 51 | 상수 |

---

### ITEM_TYPE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:37 → `DECODE(I.ITEM_TYPE, 10, 'S', 'J') AS ITEM_TYPE`
- B_ITEM 테이블의 ITEM_TYPE 값에 따라 'S' 또는 'J'로 변환
- **10이면 'S'(정량), 그 외 'J'(비정량)**

#### 3. 안드로이드 소스 사용: O (핵심 분기 조건)
- **중량 입력 방식 분기**
- ShipmentActivity.java:917 → `if (getITEM_TYPE().equals("W") || getITEM_TYPE().equals("HW"))` (바코드 계근)
- ShipmentActivity.java:970 → `else if (getITEM_TYPE().equals("S"))` (정량)
- ShipmentActivity.java:1024-1035 → `else if (getITEM_TYPE().equals("J"))` / `if (getITEM_TYPE().equals("B"))` (비정량/박스)
- **라벨 출력 분기**
- ShipmentActivity.java:1829, 1837 → W/HW 타입: 바코드에서 중량 추출
- ShipmentActivity.java:2589 → B 타입: 박스 라벨 출력
- ShipmentActivity.java:2676-2684 → W/S/J 타입별 중량 처리
- **로그 출력**
- ShipmentActivity.java:916, 1835, 1842, 2682, 2688 → `Log.d(TAG, "ITEM_TYPE : " + ...)`

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (핵심 분기)**
- **중량 입력 방식 핵심**: 타입별로 중량 입력/추출 방식 다름
  - W/HW: 바코드에서 중량 추출
  - S: 정량 (고정 중량)
  - J: 비정량 (직접 입력)
  - B: 박스 단위
- **라벨 출력 분기**: 타입별로 라벨 내용 다름
- **제거 시 중량 처리 및 라벨 출력 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 37 | SELECT 컬럼 (DECODE 함수) |
| search_production.jsp | 68, 95 | SELECT/출력 |
| ShipmentActivity.java | 916-1045, 1797, 1829-1842, 2589, 2676-2688 | **분기 조건 - 제거 불가** |
| DBHandler.java | 61, 137, 192, 258, 304, 362, 410, 690, 731 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 210 | 파싱 |
| Shipments_Info.java | 32, 295-300 | 필드, getter/setter |
| DBInfo.java | 52 | 상수 |

---

### PACKWEIGHT

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:38 → `I.PACK_WEIGHT AS PACKWEIGHT`
- B_ITEM 테이블에서 팩 중량 조회

#### 3. 안드로이드 소스 사용: O (중량 처리)
- **비정량(J) 타입 중량 설정**
- ShipmentActivity.java:1026 → `item_weight = arSM.get(current_work_position).getPACKWEIGHT()` (J타입 기본 중량)
- **바코드 중량 문자열 설정**
- ShipmentActivity.java:1839 → `print_weight_str = si.getPACKWEIGHT()` (J타입 라벨 출력)
- ShipmentActivity.java:2685 → `print_weight_str = si.getPACKWEIGHT()` (롯데용 라벨)
- **로컬 DB 저장/조회**
- DBHandler.java:193, 305, 411 → `si.setPACKWEIGHT(...)` (로컬 DB 조회)
- DBHandler.java:732 → 로컬 DB INSERT
- ProgressDlgShipSearch.java:211 → `si.setPACKWEIGHT(temp[27].toString())` (서버 응답 파싱)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (중량 처리)**
- **비정량 타입 기본 중량**: J타입 상품의 기본 중량값
- **바코드/라벨 출력**: 중량 문자열로 사용
- **제거 시 비정량 상품 중량 처리 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 38 | SELECT 컬럼 |
| search_production.jsp | 69, 96 | SELECT/출력 |
| ShipmentActivity.java | 1026, 1838-1839, 2685 | **중량 처리 - 제거 불가** |
| DBHandler.java | 62, 138, 193, 259, 305, 363, 411, 691, 732 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 211 | 파싱 |
| Shipments_Info.java | 33, 303-308 | 필드, getter/setter |
| DBInfo.java | 53 | 상수 |

---

### BARCODEGOODS

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 서브쿼리로 조회
- VW_PDA_WID_PRO_LIST:39-44 →
```sql
DECODE(D.PACKER_PRODUCT_CODE, NULL, '0000000',
  (SELECT BARCODEGOODS FROM S_BARCODE_INFO S
   WHERE S.PACKER_CLIENT_CODE = D.PACKER_CODE
     AND S.PACKER_PRODUCT_CODE = D.PACKER_PRODUCT_CODE
     AND S.STATUS = 'Y')) AS BARCODEGOODS
```
- S_BARCODE_INFO 테이블에서 바코드 상품코드 조회

#### 3. 안드로이드 소스 사용: O (바코드 스캔)
- **바코드 스캔 시 작업 검색**
- ShipmentActivity.java:3014 → `work_ppcode = find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type)`
- **바코드 정보 조회** (Barcodes_Info 클래스에서)
- ShipmentActivity.java:1335-1337 → `bg = bi.getBARCODEGOODS(); bg_from = bi.getBARCODEGOODS_FROM(); bg_to = bi.getBARCODEGOODS_TO()`
- ShipmentActivity.java:1419-1421 → 동일 패턴
- **로컬 DB 조회 조건**
- DBHandler.java:266 → `WHERE BARCODEGOODS = '...'`
- **로컬 DB 저장/조회**
- DBHandler.java:194 → `si.setBARCODEGOODS(...)` (로컬 DB 조회)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (바코드 스캔 핵심)**
- **바코드 스캔 매칭**: 스캔한 바코드로 작업 대상 검색
- **로컬 DB 조회 조건**: 바코드 상품코드로 조회
- **제거 시 바코드 스캔 기반 작업 매칭 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 39-44 | SELECT 컬럼 (서브쿼리) |
| search_production.jsp | 70, 96 | SELECT/출력 |
| ShipmentActivity.java | 1335-1337, 1419-1421, 3014 | **바코드 스캔 - 제거 불가** |
| DBHandler.java | 63, 139, 194, 260, 266, 306, 364, 412, 692, 733 | **조회 조건 - 제거 불가** |
| ProgressDlgShipSearch.java | 212 | 파싱 |
| Shipments_Info.java | 34, 311-316 | 필드, getter/setter |
| DBInfo.java | 73 | 상수 |

---

### STORE_IN_DATE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음 (라벨 출력용)

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:45 → `A.GI_DATE AS STORE_IN_DATE`
- W_GOODS_IH 테이블에서 출고일자 조회 (납품일자로 사용)

#### 3. 안드로이드 소스 사용: O (라벨 출력 핵심)
- **라벨에 납품일자 출력**
- ShipmentActivity.java:2283-2284 →
```java
Log.i(TAG, "납품일자 : " + si.getSTORE_IN_DATE());
String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " +
                  si.getSTORE_IN_DATE().substring(4,6) + "월 " +
                  si.getSTORE_IN_DATE().substring(6,8) + "일";
```
- ShipmentActivity.java:2300-2301, 2310-2311, 2397-2398 → 동일 패턴 (다양한 라벨 타입)
- ShipmentActivity.java:2615-2617 → 원앤원 라벨 납품일자 출력
- **로컬 DB 저장/조회**
- DBHandler.java:195, 307, 413 → `si.setSTORE_IN_DATE(...)` (로컬 DB 조회)
- DBHandler.java:734 → 로컬 DB INSERT

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (라벨 출력 핵심)**
- **라벨 출력 필수**: 납품일자는 라벨 필수 정보
- **날짜 형식 변환**: YYYYMMDD → YYYY년 MM월 DD일
- **제거 시 라벨에 납품일자 미표시**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 45 | SELECT 컬럼 |
| search_production.jsp | 71, 96 | SELECT/출력 |
| ShipmentActivity.java | 2283-2284, 2300-2301, 2310-2311, 2397-2398, 2615-2617 | **라벨 출력 - 제거 불가** |
| DBHandler.java | 64, 140, 195, 261, 307, 365, 413, 693, 734 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 213 | 파싱 |
| Shipments_Info.java | 35, 75-77 | 필드, getter/setter |
| DBInfo.java | 55 | 상수 |

---

### GR_WAREHOUSE_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 SELECT 컬럼으로만 존재
- VW_PDA_WID_PRO_LIST:46 → `R.GR_WAREHOUSE_CODE AS GR_WAREHOUSE_CODE`
- W_GOODS_R 테이블에서 창고 코드 조회

#### 3. 안드로이드 소스 사용: O (서버 조회 조건)
- **서버 조회 WHERE 조건** (창고별 필터링)
- ProgressDlgShipSearch.java:52 → `data += " AND GR_WAREHOUSE_CODE = 'IN10273'"` (창고1)
- ProgressDlgShipSearch.java:54 → `data += " AND GR_WAREHOUSE_CODE = 'IN60464'"` (창고2)
- ProgressDlgShipSearch.java:56 → `data += " AND GR_WAREHOUSE_CODE = '4001'"` (창고3)
- ProgressDlgShipSearch.java:58 → `data += " AND GR_WAREHOUSE_CODE = '4004'"` (창고4)
- ProgressDlgShipSearch.java:60 → `data += " AND GR_WAREHOUSE_CODE = 'IN63279'"` (창고5)
- **동일 패턴 반복**: 72-80, 89-97, 112-120 라인

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (서버 조회 조건)**
- **창고별 데이터 필터링**: 선택한 창고의 데이터만 조회
- **안드로이드 로컬 DB에는 저장하지 않음** (서버 조회 시에만 사용)
- **제거 시 창고별 필터링 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 46 | SELECT 컬럼 |
| search_production.jsp | 72, 97 | SELECT/출력 |
| ProgressDlgShipSearch.java | 52-60, 72-80, 89-97, 112-120 | **서버 조회 조건 - 제거 불가** |

---

### EMARTLOGIS_CODE

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_PRO_LIST:47 → `'0000000' AS EMARTLOGIS_CODE`
- **생산투입에서는 이마트물류코드 '0000000' 고정**

#### 3. 안드로이드 소스 사용: O (바코드 생성 핵심)
- **바코드2 생성에 사용** (이마트물류코드 기반 바코드)
- ShipmentActivity.java:1880-1881 → M8 바코드: `pBarcode2 = EMARTLOGIS_CODE.substring(0,6) + weight + compCode + IMPORT_ID_NO`
- ShipmentActivity.java:1896-1897 → M0 바코드
- ShipmentActivity.java:1913-1914, 1929, 1946-1947, 1979-1980, 2008-2009 → 다양한 바코드 타입
- **미트센터 분기 조건**
- ShipmentActivity.java:2349 → `if (BARCODE_TYPE.equals("M0") && STORE_CODE.equals("9231") && EMARTLOGIS_CODE.equals("0000000") && !EMART_PLANT_CODE.equals(""))`
- ShipmentActivity.java:2429 → `if (BARCODE_TYPE.equals("M0") && STORE_CODE.equals("9231") && !EMARTLOGIS_CODE.equals("0000000") && EMART_PLANT_CODE.equals(""))`
- **미트센터 바코드 생성**
- ShipmentActivity.java:2379-2380 → `meatCenterBarcode = EMARTLOGIS_CODE.substring(0,6) + ...`
- ShipmentActivity.java:2456-2457 → 동일 패턴

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **있음 (바코드 생성 핵심)**
- **바코드2 생성 필수**: 이마트물류코드 기반 두 번째 바코드
- **미트센터 분기 조건**: 이마트물류코드 유무로 분기
- **생산투입에서는 '0000000' 고정**: 다른 VIEW와 컬럼 구조 통일
- **제거 시 바코드2 생성 및 미트센터 분기 불가**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 47 | SELECT 컬럼 (고정값 '0000000') |
| search_production.jsp | 73, 97 | SELECT/출력 |
| ShipmentActivity.java | 1880-2009, 2349, 2379-2380, 2429, 2456-2457 | **바코드 생성/분기 - 제거 불가** |
| DBHandler.java | 65, 141, 196, 262, 308, 366, 414, 694, 735 | 스키마/SELECT/INSERT |
| ProgressDlgShipSearch.java | 214 | 파싱 |
| Shipments_Info.java | 41, 67-69 | 필드, getter/setter |
| DBInfo.java | 58 | 상수 |

---

### EMARTLOGIS_NAME

#### 1. UI 노출: X
- Activity, Adapter에서 화면에 직접 표시 없음

#### 2. VIEW 내부 사용: X
- VIEW에서 **고정값**으로 존재
- VW_PDA_WID_PRO_LIST:48 → `'정보없음' AS EMARTLOGIS_NAME`
- **생산투입에서는 이마트물류명 '정보없음' 고정**

#### 3. 안드로이드 소스 사용: X
- **ShipmentActivity.java에서 getEMARTLOGIS_NAME() 호출 없음**
- ShipmentActivity.java:2161-2166 → 주석 처리됨 (사용 안 함)
- 로컬 DB 저장만 수행 (단순 경유)
- ProgressDlgShipSearch.java:215 → `si.setEMARTLOGIS_NAME(temp[31].toString())` (파싱만)
- DBHandler.java:736 → 로컬 DB INSERT (저장만)

#### 4. DDL 사용: X
- insert_goods_wet.jsp INSERT/UPDATE 문에 사용 없음

#### 5. 비즈니스 영향: **없음**
- **주석 처리됨 - 실제 사용 안 함**
- **단순 경유만 - 실제 사용 없음**
- **제거 가능**

#### 6. 수정 위치
| 파일 | 라인 | 수정 내용 |
|------|------|-----------|
| VW_PDA_WID_PRO_LIST (VIEW) | 48 | SELECT 컬럼에서 제거 (고정값 '정보없음') |
| search_production.jsp | 74, 98 | SELECT/출력 제거 |
| ShipmentActivity.java | 2161-2166, 2218 | 주석 처리된 코드 정리 |
| ProgressDlgShipSearch.java | 215 | 파싱 제거, 인덱스 조정 |
| DBHandler.java | 66, 142, 197, 263, 309, 367, 415, 695, 736 | 스키마/SELECT/INSERT에서 제거 |
| Shipments_Info.java | 42, 71-73 | 필드, getter/setter 제거 |
| DBInfo.java | 59 | 상수 제거 |