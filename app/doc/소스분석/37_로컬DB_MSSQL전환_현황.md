# 로컬DB → MSSQL 전환 현황

**작성일**: 2026-04-06
**소스 기준**: DBHandler.java, DBInfo.java, insert_goods_wet.jsp, search_goods_wet.jsp, search_shipment.jsp

---

## 1. 전체 전환 현황 요약

| 로컬 테이블 (SQLite) | 컬럼 수 | 역할 | MSSQL 매핑 대상 | 전환 상태 |
|:-------------------:|:------:|------|:-------------:|:--------:|
| TB_SHIPMENT | 41개 | 출하대상 (서버→앱) | SM_출고상세/SM_출고머리/SM_수주머리/SM_수주상세/SM_마트사발주이마트 | **미착수** |
| TB_BARCODE_INFO | 26개 | 바코드 파싱 규칙 (서버→앱) | LB_라벨양식상세 / CO_품목코드 | **미착수** |
| TB_GOODS_WET | 22개 | 계근 결과 (앱→서버) | SM_출고계근 | **미착수** |
| TB_GOODS_WET_PRODUCTION_CALC | 1개 | 생산 바코드 임시저장 (로컬전용) | - | **미착수** |

**JSP 서버 코드**: Oracle→MSSQL 연결 전환 완료, SM_출고계근 INSERT/SELECT 이미 동작 중

---

## 2. TB_GOODS_WET ↔ SM_출고계근 매핑

### 2.1 데이터 흐름

```
[바코드 스캔]
    ↓
TB_GOODS_WET INSERT (로컬 SQLite, 즉시, SAVE_TYPE='F')
    ↓ 오프라인 버퍼 역할
[전송 버튼 클릭]
    ↓
TB_GOODS_WET에서 SAVE_TYPE='F' SELECT → packet 조립 (::구분자)
    ↓
insert_goods_wet.jsp 호출 → SM_출고계근 INSERT (MSSQL)
    ↓
전송 성공 시 TB_GOODS_WET SAVE_TYPE='T' UPDATE
```

### 2.2 컬럼 매핑

#### SM_출고계근 INSERT 컬럼 (insert_goods_wet.jsp:49-67)

| # | SM_출고계근 컬럼 | 타입 | TB_GOODS_WET 대응 | PDA packet (splitData) | 비고 |
|:-:|:-------------:|:----:|:-----------------:|:---------------------:|------|
| PK | SEQ | INT | GOODS_WET_ID | 자동생성 (`NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ`) | 서버 자동 |
| 1 | 출고상세SEQ | INT | GI_D_ID | splitData[0] | |
| 2 | 계근중량 | DOUBLE | WEIGHT | splitData[1] | `(Double * 100) / 100.0` |
| 3 | 계근중량단위 | STRING | WEIGHT_UNIT | splitData[2] | |
| 4 | ppCode | STRING | PACKER_PRODUCT_CODE | splitData[3] | |
| 5 | 계근바코드 | STRING | BARCODE | splitData[4] | |
| 6 | 패커코드 | STRING | PACKER_CLIENT_CODE | splitData[5] | |
| 7 | 제조일자 | STRING | MAKINGDATE | splitData[6] | |
| 8 | 박스시리얼 | STRING | BOXSERIAL | splitData[7] | |
| 9 | 계근순번 | INT | BOX_CNT | splitData[8] | |
| 10 | 등록사원 | STRING | REG_ID | splitData[9] | |
| 11 | 등록일자 | STRING | REG_DATE | 서버 자동생성 (dateStr) | |
| 12 | 등록시간 | STRING | REG_TIME | 서버 자동생성 (timeStr) | |
| 13 | 회사코드 | STRING | (없음) | splitData[10] | PDA 로컬에 없는 컬럼 |
| 14 | 수정사원 | STRING | (없음) | splitData[9] (=등록사원) | 서버에서 등록사원과 동일값 |
| 15 | 수정일자 | STRING | (없음) | 서버 자동생성 (dateStr) | |
| 16 | 수정시간 | STRING | (없음) | 서버 자동생성 (timeStr) | |

#### SM_출고계근 SELECT 컬럼 (search_goods_wet.jsp:50-63)

| # | SELECT 컬럼 | 별칭 (AS) | TB_GOODS_WET 대응 |
|:-:|:---------:|:--------:|:----------------:|
| 1 | 출고상세SEQ | GI_D_ID | GI_D_ID |
| 2 | 계근중량 | WEIGHT | WEIGHT |
| 3 | 계근중량단위 | WEIGHT_UNIT | WEIGHT_UNIT |
| 4 | ppCode | PACKER_PRODUCT_CODE | PACKER_PRODUCT_CODE |
| 5 | 계근바코드 | BARCODE | BARCODE |
| 6 | 패커코드 | PACKER_CLIENT_CODE | PACKER_CLIENT_CODE |
| 7 | 계근순번 | BOX_CNT | BOX_CNT |
| 8 | 등록사원 | REG_ID | REG_ID |
| 9 | 등록일자 | REG_DATE | REG_DATE |
| 10 | 등록시간 | REG_TIME | REG_TIME |
| 11 | 제조일자 | MAKINGDATE | MAKINGDATE |
| 12 | 박스시리얼 | BOXSERIAL | BOXSERIAL |
| 13 | '' | BOX_ORDER | BOX_ORDER (빈값) |

### 2.3 TB_GOODS_WET에만 존재하는 컬럼 (SM_출고계근에 미전송)

| 컬럼 | 용도 | SM_출고계근 대응 |
|------|------|:-------------:|
| EMARTITEM_CODE | 이마트상품코드 | **없음** |
| EMARTITEM | 이마트상품명 | **없음** |
| ITEM_CODE | 상품코드 | **없음** (packet에는 포함) |
| BRAND_CODE | 브랜드코드 | **없음** (packet에는 포함) |
| SAVE_TYPE | 전송여부 (F/T) | **없음** (로컬 전용) |
| MEMO | 메모 | **없음** |
| DUPLICATE | 중복스캔 | **없음** |
| CLIENT_TYPE | 채널코드 (06=홈플러스, 07=롯데) | **없음** (packet에는 포함) |
| BOX_ORDER | 박스순번 | **없음** (packet에는 포함) |

### 2.4 SM_출고계근에만 존재하는 컬럼 (PDA 로컬에 없음)

| 컬럼 | 값 | 비고 |
|------|-----|------|
| 회사코드 | splitData[10] | PDA에서 전송하지만 로컬 DB에 미저장 |
| 수정사원 | 등록사원과 동일 | 서버에서 자동 설정 |
| 수정일자 | 서버 자동생성 | 서버에서 자동 설정 |
| 수정시간 | 서버 자동생성 | 서버에서 자동 설정 |

---

## 3. PDA 서버 전송 패킷 구조

### 3.1 packet 조립 (BixolonShipmentActivity.java:2650-2664)

```java
packet += GI_D_ID + "::";              // splitData[0]  → 출고상세SEQ
packet += WEIGHT + "::";               // splitData[1]  → 계근중량
packet += WEIGHT_UNIT + "::";          // splitData[2]  → 계근중량단위
packet += PACKER_PRODUCT_CODE + "::";  // splitData[3]  → ppCode
packet += BARCODE + "::";              // splitData[4]  → 계근바코드
packet += PACKER_CLIENT_CODE + "::";   // splitData[5]  → 패커코드
packet += MAKINGDATE + "::";           // splitData[6]  → 제조일자
packet += BOXSERIAL + "::";            // splitData[7]  → 박스시리얼
packet += BOX_CNT + "::";             // splitData[8]  → 계근순번
packet += REG_ID + "::";              // splitData[9]  → 등록사원, 수정사원
packet += ITEM_CODE + "::";           // splitData[10] → 회사코드 (★주의: JSP에서 회사코드로 사용)
packet += BRAND_CODE + "::";          // splitData[11] → JSP 미사용
packet += CLIENT_TYPE + "::";         // splitData[12] → JSP 미사용
packet += BOX_ORDER;                   // splitData[13] → JSP 미사용
```

### 3.2 JSP에서 사용하는 splitData 인덱스 (insert_goods_wet.jsp:71-86)

| JSP index | splitData | SM_출고계근 컬럼 | 비고 |
|:---------:|:---------:|:-------------:|------|
| pstmt.setInt(1, ...) | splitData[0] | 출고상세SEQ | |
| pstmt.setDouble(2, ...) | splitData[1] | 계근중량 | `(Double * 100) / 100.0` |
| pstmt.setString(3, ...) | splitData[2] | 계근중량단위 | |
| pstmt.setString(4, ...) | splitData[3] | ppCode | |
| pstmt.setString(5, ...) | splitData[4] | 계근바코드 | |
| pstmt.setString(6, ...) | splitData[5] | 패커코드 | |
| pstmt.setString(7, ...) | splitData[6] | 제조일자 | |
| pstmt.setString(8, ...) | splitData[7] | 박스시리얼 | |
| pstmt.setInt(9, ...) | splitData[8] | 계근순번 | |
| pstmt.setString(10, ...) | splitData[9] | 등록사원 | |
| pstmt.setString(11, ...) | - | 등록일자 | 서버 자동 (dateStr) |
| pstmt.setString(12, ...) | - | 등록시간 | 서버 자동 (timeStr) |
| pstmt.setString(13, ...) | splitData[10] | 회사코드 | **PDA의 ITEM_CODE를 회사코드로 사용** |
| pstmt.setString(14, ...) | splitData[9] | 수정사원 | 등록사원과 동일 |
| pstmt.setString(15, ...) | - | 수정일자 | 서버 자동 (dateStr) |
| pstmt.setString(16, ...) | - | 수정시간 | 서버 자동 (timeStr) |

### 3.3 searchType별 전송 URL

| searchType | 대상 | JSP URL | 상수 |
|:----------:|------|---------|------|
| 0 (이마트) | insert_goods_wet.jsp | `Common.URL_INSERT_GOODS_WET` | inno |
| 1 (생산) | insert_goods_wet.jsp | `Common.URL_INSERT_GOODS_WET` | inno |
| 2 (홈플러스) | insert_goods_wet_homeplus.jsp | `Common.URL_INSERT_GOODS_WET_HOMEPLUS` | inno |
| 3 (도매) | insert_goods_wet.jsp | `Common.URL_INSERT_GOODS_WET` | inno |
| 6 (롯데) | insert_goods_wet_homeplus.jsp | `Common.URL_INSERT_GOODS_WET_HOMEPLUS` | inno |
| 7 (생산라벨) | insert_goods_wet.jsp | `Common.URL_INSERT_GOODS_WET` | inno |

---

## 4. TB_GOODS_WET 관련 DBHandler 메서드 전체 목록

### 4.1 CREATE

| 메서드 | 위치(줄) | 테이블 | 비고 |
|--------|:--------:|--------|------|
| `createqueryGoodsWet()` | 1052 | TB_GOODS_WET | 22개 컬럼 정의 |

### 4.2 SELECT (4개)

| 메서드 | 위치(줄) | 용도 | WHERE 조건 |
|--------|:--------:|------|-----------|
| `selectqueryGoodsWet()` | 1095 | 지점별 계근 데이터 조회 | GI_D_ID, PACKER_PRODUCT_CODE, PACKER_CLIENT_CODE |
| `selectquerySendGoodsWet()` | 1165 | 서버 전송용 전체 조회 | 외부 qry_where (GI_D_ID OR ...) |
| `selectqueryListGoodsWetInfo()` | 1239 | 계근 리스트 정보 (수량/중량) | GI_D_ID, PACKER_PRODUCT_CODE, PACKER_CLIENT_CODE |
| `selectMaxBoxOrder()` | 1778 | 홈플러스 MAX BOX_ORDER | 조건 없음 |

### 4.3 INSERT (3개)

| 메서드 | 위치(줄) | 대상 | 특이사항 |
|--------|:--------:|------|---------|
| `insertqueryGoodsWet()` | 1354 | 이마트/도매/생산 | 기본 INSERT (19개 컬럼) |
| `insertqueryGoodsWetHomeplus()` | 1424 | 홈플러스 | CLIENT_TYPE='06', maxBoxOrder |
| `insertqueryGoodsWetLotte()` | 1497 | 롯데 | CLIENT_TYPE='07', lotte_TryCount |

### 4.4 UPDATE (1개)

| 메서드 | 위치(줄) | 용도 | 변경 컬럼 |
|--------|:--------:|------|----------|
| `updatequeryGoodsWet()` | 1572 | 전송 완료 표시 | SAVE_TYPE='F'→'T' |

### 4.5 DELETE (3개)

| 메서드 | 위치(줄) | 용도 | 조건 |
|--------|:--------:|------|------|
| `deletequerySelectGoodsWet()` | 1601 | 개별 삭제 + BOX_CNT 갱신 | GI_D_ID, BARCODE, BOX_CNT |
| `deletequeryGoodsWet()` | 1637 | 전송완료 건 삭제 | SAVE_TYPE='T' |
| `deletequeryAllGoodsWet()` | 1660 | 전체 삭제 | 무조건 |

### 4.6 중복 체크 (2개)

| 메서드 | 위치(줄) | 용도 | WHERE 조건 |
|--------|:--------:|------|-----------|
| `duplicatequeryGoodsWet_check()` | 1283 | 단순 중복 체크 | BARCODE |
| `duplicatequeryGoodsWet()` | 1317 | 상세 중복 체크 | BARCODE, GI_D_ID, PACKER_PRODUCT_CODE |

---

## 5. TB_GOODS_WET 호출 위치 (BixolonShipmentActivity.java)

| 호출 메서드 | 위치(줄) | 호출되는 시점 |
|-----------|:--------:|------------|
| `insertqueryGoodsWet()` | 1647 | 바코드 2차 스캔(BL번호) → wet_data_insert() |
| `insertqueryGoodsWetHomeplus()` | 1635 | 홈플러스 계근 시 |
| `insertqueryGoodsWetLotte()` | 1640 | 롯데 계근 시 |
| `duplicatequeryGoodsWet_check()` | 1202 | 1차 스캔(상품바코드) 시 중복 체크 |
| `duplicatequeryGoodsWet()` | 1332 | 2차 스캔(BL번호) 시 중복 체크 |
| `selectquerySendGoodsWet()` | 2640 | 전송 버튼 → ProgressDlgShipmentSend |
| `updatequeryGoodsWet()` | 2690 | 서버 전송 성공 시 SAVE_TYPE 업데이트 |
| `selectqueryGoodsWet()` | - | 지점 상세 조회 시 |
| `selectqueryListGoodsWetInfo()` | - | 리스트 정보 조회 시 |
| `deletequerySelectGoodsWet()` | - | 개별 계근 삭제 시 |
| `deletequeryGoodsWet()` | - | 전송완료 건 삭제 시 |
| `deletequeryAllGoodsWet()` | - | 전체 초기화 시 |

---

## 6. 전환 시 고려사항

### 6.1 로컬 TB_GOODS_WET은 변경 불필요할 수 있음

TB_GOODS_WET은 **로컬 SQLite 오프라인 버퍼**이므로, MSSQL 테이블 구조와 1:1로 맞출 필요가 없다. 서버 전송 시 packet 조립 단계에서 SM_출고계근 구조에 맞게 변환하면 된다.

**현재 이미 동작하는 구조:**
- PDA 로컬: TB_GOODS_WET (22개 컬럼, SQLite)
- 서버 JSP: SM_출고계근 (17개 컬럼, MSSQL)
- 변환: packet 조립 (BixolonShipmentActivity:2650-2664) → JSP에서 splitData 파싱

### 6.2 로컬 테이블을 변경하는 경우 영향 범위

| 영향 파일 | 수정 대상 | 수정 수 |
|----------|----------|:------:|
| DBInfo.java | 테이블명/컬럼명 상수 | 22개 |
| DBHandler.java | CREATE/SELECT/INSERT/UPDATE/DELETE 쿼리 | 13개 메서드 |
| BixolonShipmentActivity.java | wet_data_insert(), 전송 packet 조립 | 2곳 |
| ShipmentActivity.java | 동일 구조 (원본) | 2곳 |
| Goodswets_Info.java | 모델 클래스 getter/setter | 22개 필드 |
| DetailAdapter.java | 리스트 표시 | 1곳 |

### 6.3 packet splitData 인덱스 불일치 주의

PDA에서 보내는 splitData[10]은 **ITEM_CODE**이지만, JSP에서는 **회사코드**로 사용한다.

```
PDA: packet += ITEM_CODE + "::";           // splitData[10]
JSP: pstmt.setString(13, splitData[10]);   // 회사코드 컬럼에 저장
```

splitData[11]=BRAND_CODE, splitData[12]=CLIENT_TYPE, splitData[13]=BOX_ORDER는 **JSP에서 사용하지 않는다.**

---

## 7. ERP Entity 기반 MSSQL 테이블 정의 (HL_ERP 프로젝트)

**소스 경로**: `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\java\com\sgis\domain\sm\release\entity\`

### 7.1 공통 컬럼 (BaseEntity)

**파일**: `com.sgis.domain.cmmn.base.entity.BaseEntity`

모든 SM_ 테이블이 상속하는 공통 컬럼:

| 컬럼명 | 타입 | 기본값 | Java 필드 | 비고 |
|--------|------|--------|----------|------|
| 회사코드 | VARCHAR(2) | '' | cmpnyCode | INSERT 시 자동설정 |
| 등록사원 | VARCHAR(8) | '' | insEmpno | INSERT 시 자동설정 |
| 등록일자 | VARCHAR(8) | '00000000' | insDate | INSERT 시 자동설정 |
| 등록시간 | VARCHAR(6) | '000000' | insTime | INSERT 시 자동설정 |
| 수정사원 | VARCHAR(8) | '' | updEmpno | UPDATE 시 자동설정 |
| 수정일자 | VARCHAR(8) | '00000000' | updDate | UPDATE 시 자동설정 |
| 수정시간 | VARCHAR(6) | '000000' | updTime | UPDATE 시 자동설정 |

---

### 7.2 SM_출고계근 (DlivyWeighEntity)

**파일**: `DlivyWeighEntity.java`
**시퀀스**: `SM_DLIVY_WEIGH_SEQ`

| # | 컬럼명 | 타입 | 기본값 | NOT NULL | Java 필드 | PDA 대응 (TB_GOODS_WET) |
|:-:|--------|------|--------|:--------:|----------|:---------------------:|
| PK | SEQ | BIGINT | 자동증가 | O | seq | GOODS_WET_ID |
| 1 | 출고상세SEQ | BIGINT | 0 | O | dlivyDetailSeq | GI_D_ID |
| 2 | 계근순번 | INT | 0 | O | weighSn | BOX_CNT |
| 3 | 계근중량 | FLOAT | 0 | O | weighWt | WEIGHT |
| 4 | 계근중량단위 | VARCHAR(10) | '' | O | weighUnit | WEIGHT_UNIT |
| 5 | 계근바코드 | VARCHAR(50) | '' | O | weighBrcd | BARCODE |
| 6 | PPCODE | VARCHAR(20) | '' | O | ppCode | PACKER_PRODUCT_CODE |
| 7 | 패커코드 | VARCHAR(6) | '' | O | packerCode | PACKER_CLIENT_CODE |
| 8 | 제조일자 | VARCHAR(8) | '00000000' | O | mnfcturDe | MAKINGDATE |
| 9 | 박스시리얼 | VARCHAR(30) | '' | O | boxSerial | BOXSERIAL |
| + | 회사코드 | VARCHAR(2) | '' | O | (BaseEntity) | (없음) |
| + | 등록사원 | VARCHAR(8) | '' | O | (BaseEntity) | REG_ID |
| + | 등록일자 | VARCHAR(8) | '00000000' | O | (BaseEntity) | REG_DATE |
| + | 등록시간 | VARCHAR(6) | '000000' | O | (BaseEntity) | REG_TIME |
| + | 수정사원 | VARCHAR(8) | '' | O | (BaseEntity) | (없음) |
| + | 수정일자 | VARCHAR(8) | '00000000' | O | (BaseEntity) | (없음) |
| + | 수정시간 | VARCHAR(6) | '000000' | O | (BaseEntity) | (없음) |

**총 컬럼**: 고유 9개 + BaseEntity 공통 7개 = **16개**

---

### 7.3 SM_출고머리 (DlivyHeadEntity)

**파일**: `DlivyHeadEntity.java`
**시퀀스**: `SM_DIL_HEAD_SEQ`

| # | 컬럼명 | 타입 | Java 필드 | 설명 |
|:-:|--------|------|----------|------|
| PK | SEQ | BIGINT | seq | |
| 1 | 출고사업장 | VARCHAR(2) | dlivyBPlc | |
| 2 | 출고일자 | VARCHAR(8) | dlivyDe | |
| 3 | 출고일련번호 | SMALLINT | dlivySN | |
| 4 | 출고부서 | VARCHAR(14) | dlivyDept | |
| 5 | 출고거래처 | VARCHAR(6) | dlivyBcnc | |
| 6 | 운송방법 | VARCHAR(2) | trnsprtMth | |
| 7 | 출고구분 | VARCHAR(1) | dlivySe | |
| 8 | 공급가액 | FLOAT | splPc | |
| 9 | 부가세 | FLOAT | vat | |
| 10 | 총금액 | FLOAT | totAm | |
| 11 | 출고검사완료 | VARCHAR(1) | dlivyInspctCompt | |
| 12 | 거래명세발행여부 | VARCHAR(1) | delngDtlsIsuAt | |
| 13 | 세금_작성여부 | VARCHAR(1) | taxWritngAt | |
| 14 | 출고담당자 | VARCHAR(8) | dlivyCharger | |
| 15 | 계산서사업장 | VARCHAR(2) | billBPlc | |
| 16 | 계산서발행일자 | VARCHAR(8) | billIsuDe | |
| 17 | 계산서번호 | SMALLINT | billNum | |
| 18 | 거래명세회수여부 | VARCHAR(1) | delngDtlsRtrvlAt | |
| 19 | 차량코드 | VARCHAR(7) | vhcleCode | |
| 20 | 직배송업체 | VARCHAR(6) | directDlvyEntrps | |
| 21 | 명세서발행일자 | VARCHAR(8) | dtStmnIsuDe | |
| 22 | 영업담당자 | VARCHAR(8) | salesChager | |
| 23 | 마트사구분 | VARCHAR(1) | martCmnpySe | |
| + | BaseEntity 공통 7개 | | | |

**총 컬럼**: 고유 23개 + 공통 7개 = **30개**

---

### 7.4 SM_출고상세 (DlivyDetailEntity)

**파일**: `DlivyDetailEntity.java`
**시퀀스**: `SM_DIL_DET_SEQ`

| # | 컬럼명 | 타입 | Java 필드 | 설명 |
|:-:|--------|------|----------|------|
| PK | SEQ | BIGINT | seq | |
| 1 | 출고사업장 | VARCHAR(2) | dlivyBPlc | |
| 2 | 출고일자 | VARCHAR(8) | dlivyDe | |
| 3 | 출고일련번호 | SMALLINT | dlivySN | |
| 4 | 행번호 | SMALLINT | rowNum | |
| 5 | 거래처코드 | VARCHAR(10) | bcncCode | |
| 6 | 출고품목코드 | VARCHAR(10) | dlivyItemCode | |
| 7 | 출고박스수량 | FLOAT | dlivyBoxQy | |
| 8 | 출고수량 | FLOAT | dlivyQy | |
| 9 | 출고중량 | FLOAT | dlivyWt | |
| 10 | 출고단가 | FLOAT | dlivyUntPc | |
| 11 | 출고금액 | FLOAT | dlivyAm | |
| 12 | 부가세금액 | FLOAT | vatAm | |
| 13 | 제조사업장 | VARCHAR(2) | mnfcturBPlc | |
| 14 | 제조일자 | VARCHAR(8) | mnfcturDe | |
| 15 | 제조번호 | SMALLINT | mnfcturNum | |
| 16 | 수주사업장 | VARCHAR(2) | rcvOrdbizPlc | |
| 17 | 수주일자 | VARCHAR(8) | rcvOrdDe | |
| 18 | 수주일련번호 | SMALLINT | rcvOrdSN | |
| 19 | 순번 | SMALLINT | sn | |
| 20 | 수주수량 | FLOAT | rcvOrdQy | |
| 21 | 수주중량 | FLOAT | rcvOrdWt | |
| 22 | 성적서발행사업장 | VARCHAR(2) | gradCrtfctPblicteBPlc | |
| 23 | 성적서발행일자 | VARCHAR(8) | gradCrtfctPblicteDe | |
| 24 | 성적서발행번호 | SMALLINT | gradCrtfctPblicteNum | |
| 25 | 시험성적서발행 | VARCHAR(1) | testReprtPblicte | |
| 26 | 출고확인 | VARCHAR(1) | dlivyCnfirm | |
| 27 | 세금계산서등록 | VARCHAR(1) | elTaxBilIns | |
| 28 | 계산서사업장 | VARCHAR(2) | billBPlc | |
| 29 | 계산서발행일자 | VARCHAR(8) | billIsuDe | |
| 30 | 계산서발행번호 | SMALLINT | billIsuNum | |
| 31 | 계산서행번호 | SMALLINT | billRowNum | |
| 32 | 비고 | VARCHAR(2000) | rm | |
| 33 | 변경전단가 | FLOAT | bfChgUntPc | |
| 34 | 창고코드 | VARCHAR(4) | wrhsCode | |
| 35 | 이력번호 | VARCHAR(15) | histNo | |
| 36 | 이력번호개수 | SMALLINT | histNoCnt | |
| 37 | 계근여부 | VARCHAR(1) | weighYn | 기본값 'N' |
| 38 | 이체출고여부 | VARCHAR(1) | transfrDlivyYn | 기본값 'N' |
| + | BaseEntity 공통 7개 | | | |

**총 컬럼**: 고유 38개 + 공통 7개 = **45개**

---

### 7.5 SM_출고LOT (DlivyLotEntity)

**파일**: `DlivyLotEntity.java`
**시퀀스**: `SM_DLIVY_LOT_SEQ`

| # | 컬럼명 | 타입 | Java 필드 | 설명 |
|:-:|--------|------|----------|------|
| PK | SEQ | BIGINT | seq | |
| 1 | 출고머리SEQ | BIGINT | dlivyHeadSeq | |
| 2 | 출고상세SEQ | BIGINT | dlivyDetailSeq | |
| 3 | 출고LOTSEQ | BIGINT | dlivyLotSeq | |
| 4 | 사업장 | VARCHAR(2) | bizPlc | |
| 5 | 창고코드 | VARCHAR(4) | wrhsCode | |
| 6 | LOTNO | VARCHAR(50) | lotNo | |
| 7 | 박스수량 | FLOAT | boxQy | |
| 8 | 수량 | FLOAT | qy | |
| 9 | 중량 | FLOAT | wt | |
| 10 | 평균중량 | FLOAT | avgWt | |
| 11 | 제조일자 | VARCHAR(8) | mnfcturDe | |
| 12 | 소비기한 | VARCHAR(8) | expDe | |
| 13 | 이력번호 | VARCHAR(15) | histNo | |
| 14 | 피스여부 | VARCHAR(1) | pieceYn | 기본값 'N' |
| 15 | WMS관리번호 | VARCHAR(100) | wmsManageNo | |
| 16 | 확정여부 | VARCHAR(1) | cfmYn | 기본값 'N' |
| 17 | 확정일자 | VARCHAR(8) | cfmDe | |
| 18 | 확정시간 | VARCHAR(6) | cfmTime | |
| + | BaseEntity 공통 7개 | | | |

**총 컬럼**: 고유 18개 + 공통 7개 = **25개**

---

## 8. PDA 로컬 테이블 ↔ MSSQL 테이블 관계도

```
PDA 로컬 (SQLite)                    MSSQL (ERP)
─────────────────                    ──────────────────
TB_SHIPMENT (41컬럼)          ←──   SM_출고머리 (30컬럼)
  서버→앱 다운로드                     SM_출고상세 (45컬럼)
                                      SM_수주머리
                                      SM_수주상세
                                      SM_마트사발주이마트

TB_BARCODE_INFO (26컬럼)      ←──   (별도 확인 필요)

TB_GOODS_WET (22컬럼)         ──→   SM_출고계근 (16컬럼)
  앱→서버 전송                         insert_goods_wet.jsp 경유

                               ──→   SM_출고LOT (25컬럼)
                                      (현재 PDA에서 미사용)
```

---

**문서 버전**: 1.1
