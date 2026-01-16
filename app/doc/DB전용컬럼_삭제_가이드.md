# DB 전용 컬럼 삭제 가이드

**작성일**: 2026-01-16
**검증 범위**: JSP 24개, Java 32개, VIEW 문서

---

## 1. 삭제 대상 컬럼 (8개)

| # | VIEW idx | temp[] idx | 컬럼명 | 설명 |
|:-:|:--------:|:----------:|--------|------|
| 1 | 0 | temp[0] | GI_H_ID | 출고헤더ID |
| 2 | 2 | temp[2] | EOI_ID | 이마트 주문ID |
| 3 | 9 | temp[9] | AMOUNT | 출하상품금액 |
| 4 | 10 | temp[10] | GOODS_R_ID | 입고번호 |
| 5 | 11 | temp[11] | GR_REF_NO | 창고입고번호 |
| 6 | 15 | temp[15] | BRANDNAME | 브랜드명 |
| 7 | 23 | temp[23] | PACKERNAME | 패커명 |
| 8 | 31 | temp[31] | EMARTLOGIS_NAME | 이마트물류명 |

---

## 2. 데이터 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. 서버 VIEW (7개)                         ← ✅ 수정 대상      │
│    38개 컬럼 → 30개 컬럼 (8개 삭제)                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. JSP (17개 파일)                         ← ✅ 수정 대상      │
│    rs.getString() 8개 컬럼 삭제                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. ProgressDlgShipSearch.java              ← ✅ 수정 대상      │
│    8개 setter 삭제 + temp[] 인덱스 전체 재조정                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. Shipments_Info.java                     ← ✅ 수정 대상      │
│    8개 필드/getter/setter 삭제                                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. DBHandler.java                          ← ✅ 수정 대상      │
│    CREATE TABLE / INSERT / SELECT                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6. TB_SHIPMENT (로컬 SQLite)                                    │
│    41개 컬럼 → 33개 컬럼                                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 삭제 범위

### 3.1 수정 대상 (서버)

| 파일 | 수정 내용 |
|------|-----------|
| VIEW 7개 | 8개 컬럼 SELECT 삭제 |
| JSP 17개 | rs.getString() 8개 삭제 |

### 3.2 수정 대상 (앱)

| 파일 | 수정 내용 |
|------|-----------|
| DBHandler.java | CREATE/INSERT/SELECT에서 8개 컬럼 제거 |
| DBHelper.java | DB 버전 증가, onUpgrade 추가 |
| Shipments_Info.java | 8개 필드 + getter + setter 삭제 |
| ProgressDlgShipSearch.java | 8개 setter 삭제 + temp[] 인덱스 재조정 |
| TestDataHelper.java | 8개 setter 제거 |
| DBInfo.java | 8개 상수 삭제 |
| ShipmentActivity.java | 주석 정리 (선택) |
| BixolonShipmentActivity.java | 주석 정리 (선택) |

---

## 4. 수정 순서

```
┌─────────────────────────────────────────────────────────────────┐
│                        수정 순서                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ═══════════════════ 서버 수정 ═══════════════════              │
│                                                                 │
│  Step S-1. VIEW 수정 (7개)                                      │
│      │     8개 컬럼 SELECT 삭제 (38개 → 30개)                   │
│      ▼                                                          │
│  Step S-2. JSP 수정 (17개 파일)                                 │
│      │     rs.getString() 8개 컬럼 삭제                         │
│      ▼                                                          │
│                                                                 │
│  ═══════════════════ 앱 수정 ════════════════════               │
│                                                                 │
│  Step 1. DBHandler.java - CREATE TABLE 수정                     │
│      │   createqueryShipment() - 8개 컬럼 정의 삭제             │
│      ▼                                                          │
│  Step 2. DBHandler.java - SELECT 쿼리 수정 (4개 함수)           │
│      │   2-1. selectqueryShipment()                             │
│      │   2-2. selectqueryShipmentOnly()                         │
│      │   2-3. selectqueryShipmentBL()                           │
│      │   2-4. selectqueryAllShipment()                          │
│      ▼                                                          │
│  Step 3. DBHandler.java - INSERT 쿼리 수정                      │
│      │   insertqueryShipment() - 8개 컬럼명 + VALUES 삭제       │
│      ▼                                                          │
│  Step 4. DBHelper.java - DB 마이그레이션                        │
│      │   DB_VERSION 증가, onUpgrade() 추가                      │
│      ▼                                                          │
│  Step 5. Shipments_Info.java - 필드/getter/setter 삭제          │
│      │   8개 필드, 8개 getter, 8개 setter 삭제                  │
│      ▼                                                          │
│  Step 6. ProgressDlgShipSearch.java - temp[] 인덱스 재조정      │
│      │   8개 setter 삭제 + 30개 setter 인덱스 변경              │
│      ▼                                                          │
│  Step 7. TestDataHelper.java - 테스트 데이터 수정               │
│      │   8개 setter 호출 삭제                                   │
│      ▼                                                          │
│  Step 8. DBInfo.java - 상수 삭제                                │
│      ▼                                                          │
│  Step 9. 주석 정리 (선택)                                       │
│      │   ShipmentActivity, BixolonShipmentActivity              │
│      ▼                                                          │
│  Step 10. 컴파일 및 테스트                                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

| 순서 | 이유 |
|------|------|
| VIEW 먼저 | 데이터 흐름의 시작점, 컬럼 수 변경 |
| JSP 다음 | VIEW 변경에 맞춰 rs.getString() 삭제 |
| CREATE TABLE | 테이블 구조가 앱 기준 |
| SELECT 다음 | 존재하지 않는 컬럼 SELECT 시 런타임 에러 |
| INSERT 다음 | 존재하지 않는 컬럼 INSERT 시 런타임 에러 |
| Shipments_Info 다음 | getter 삭제 후 INSERT VALUES 컴파일 에러 방지 |
| ProgressDlgShipSearch 다음 | setter 삭제 후 호출 코드 컴파일 에러 방지 |
| DB 마이그레이션 | 코드 수정 완료 후 DB 버전 증가 |

### 4.1 진행 체크리스트

#### 서버 수정

| Step | 파일 | 작업 | 완료 |
| :--: | --- | --- | :-: |
| S-1 | VIEW 7개 | 8개 컬럼 SELECT 삭제 | ✅ |
| S-2 | JSP 17개 | rs.getString() 8개 컬럼 삭제 | ✅ |

#### 앱 수정

| Step | 파일                                                  | 작업                         | 완료  |
| :--: | --------------------------------------------------- | -------------------------- | :-: |
|  1   | DBHandler.java                                      | CREATE TABLE 8개 컬럼 삭제      |  ✅  |
| 2-1  | DBHandler.java                                      | selectqueryShipment 수정     |  ✅  |
| 2-2  | DBHandler.java                                      | selectqueryShipmentOnly 수정 |  ✅  |
| 2-3  | DBHandler.java                                      | selectqueryShipmentBL 수정   |  ✅  |
| 2-4  | DBHandler.java                                      | selectqueryAllShipment 수정  |  ✅  |
|  3   | DBHandler.java                                      | INSERT 쿼리 수정               |  ✅  |
|  4   | DBHelper.java                                       | DB 마이그레이션                  |  ✅  |
|  5   | Shipments_Info.java                                 | 필드/getter/setter 삭제        |     |
|  6   | ProgressDlgShipSearch.java                          | temp[] 인덱스 재조정             |     |
|  7   | TestDataHelper.java                                 | setter 호출 삭제               |     |
|  8   | DBInfo.java                                         | 상수 삭제                      |     |
|  9   | ShipmentActivity.java, BixolonShipmentActivity.java | 주석 정리 (선택)                 |     |
|  10  | -                                                   | 컴파일 및 테스트                  |     |

---

## 5. Step별 수정 상세

### Step S-1. VIEW 수정 (서버)

**대상 VIEW (7개):**

| # | VIEW명 | 용도 |
|:-:|--------|------|
| 1 | VW_PDA_WID_LIST | 이마트 |
| 2 | VW_PDA_WID_PRO_LIST | 생산 |
| 3 | VW_PDA_WID_HOMEPLUS_LIST | 홈플러스 |
| 4 | VW_PDA_WID_WHOLESALE_LIST | 도매 |
| 5 | VW_PDA_WID_LIST_NONFIXED | 이마트 비정량 |
| 6 | VW_PDA_WID_LIST_NONFIXED_HP | 홈플러스 비정량 |
| 7 | VW_PDA_WID_LIST_LOTTE | 롯데 |

**삭제할 컬럼 (8개):**

| # | 컬럼명 | 현재 VIEW idx | 설명 |
|:-:|--------|:-------------:|------|
| 1 | GI_H_ID | 0 | 출고헤더ID |
| 2 | EOI_ID | 2 | 이마트 주문ID |
| 3 | AMOUNT | 9 | 출하상품금액 |
| 4 | GOODS_R_ID | 10 | 입고번호 |
| 5 | GR_REF_NO | 11 | 창고입고번호 |
| 6 | BRANDNAME | 15 | 브랜드명 |
| 7 | PACKERNAME | 23 | 패커명 |
| 8 | EMARTLOGIS_NAME | 31 | 이마트물류명 |

**수정 후**: 38개 컬럼 → 30개 컬럼

---

### Step S-2. JSP 수정 (서버)

**대상 파일 (17개):**

| # | 파일명 |
|:-:|--------|
| 1 | search_shipment.jsp |
| 2 | search_shipment_homeplus.jsp |
| 3 | search_shipment_lotte.jsp |
| 4 | search_shipment_ono.jsp |
| 5 | search_shipment_ono_temp.jsp |
| 6 | search_shipment_ono_temp_diff_prd.jsp |
| 7 | search_shipment_wholesale.jsp |
| 8 | search_homeplus_nonfixed.jsp |
| 9 | search_homeplus_nonfixed2.jsp |
| 10 | search_production.jsp |
| 11 | search_production_4label.jsp |
| 12 | search_production_nonfixed.jsp |
| 13 | search_barcode_info.jsp |
| 14 | search_barcode_info_temp.jsp |
| 15 | search_barcode_info_temp_diff_prd.jsp |
| 16 | search_barcode_info_nonfixed.jsp |
| 17 | search_goods_wet.jsp |

**삭제할 코드**:
```java
// 각 JSP에서 아래 8개 rs.getString() 삭제
rs.getString("GI_H_ID")
rs.getString("EOI_ID")
rs.getString("AMOUNT")
rs.getString("GOODS_R_ID")
rs.getString("GR_REF_NO")
rs.getString("BRANDNAME")
rs.getString("PACKERNAME")
rs.getString("EMARTLOGIS_NAME")
```

---

### Step 1. CREATE TABLE [DBHandler.java] (라인 32-75)

**파일**: `DBHandler.java`
**함수**: `createqueryShipment()`



| 라인  | 삭제 코드                                      |
| :-: | ------------------------------------------ |
| 35  | `+ DBInfo.GI_H_ID + " TEXT NOT NULL, "`    |
| 37  | `+ DBInfo.EOI_ID + " TEXT NOT NULL, "`     |
| 44  | `+ DBInfo.AMOUNT + " TEXT NOT NULL, "`     |
| 45  | `+ DBInfo.GOODS_R_ID + " TEXT NOT NULL, "` |
| 46  | `+ DBInfo.GR_REF_NO + " TEXT NOT NULL, "`  |
| 50  | `+ DBInfo.BRANDNAME + " TEXT NOT NULL, "`  |
| 58  | `+ DBInfo.PACKERNAME + " TEXT NOT NULL, "` |
| 66  | `+ DBInfo.EMARTLOGIS_NAME + " TEXT, "`     |

---

### Step 2-1. selectqueryShipment [DBHandler.java] (라인 108-196)

**파일**: `DBHandler.java`
**SELECT 컬럼:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 110 | `+ DBInfo.GI_H_ID + ", "` |
| 112 | `+ DBInfo.EOI_ID + ", "` |
| 119 | `+ DBInfo.AMOUNT + ", "` |
| 120 | `+ DBInfo.GOODS_R_ID + ", "` |
| 121 | `+ DBInfo.GR_REF_NO + ", "` |
| 125 | `+ DBInfo.BRANDNAME + ", "` |
| 133 | `+ DBInfo.PACKERNAME + ", "` |
| 141 | `+ DBInfo.EMARTLOGIS_NAME + ", "` |

**cursor setter:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 165 | `si.setGI_H_ID(...)` |
| 167 | `si.setEOI_ID(...)` |
| 174 | `si.setAMOUNT(...)` |
| 175 | `si.setGOODS_R_ID(...)` |
| 176 | `si.setGR_REF_NO(...)` |
| 180 | `si.setBRANDNAME(...)` |
| 188 | `si.setPACKERNAME(...)` |
| 196 | `si.setEMARTLOGIS_NAME(...)` |

---

### Step 2-2. selectqueryShipmentOnly [DBHandler.java] (라인 229-308)

**파일**: `DBHandler.java`
**SELECT 컬럼:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 231 | `+ DBInfo.GI_H_ID + ", "` |
| 233 | `+ DBInfo.EOI_ID + ", "` |
| 240 | `+ DBInfo.AMOUNT + ", "` |
| 241 | `+ DBInfo.GOODS_R_ID + ", "` |
| 242 | `+ DBInfo.GR_REF_NO + ", "` |
| 246 | `+ DBInfo.BRANDNAME + ", "` |
| 254 | `+ DBInfo.PACKERNAME + ", "` |
| 262 | `+ DBInfo.EMARTLOGIS_NAME + ", "` |

**cursor setter:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 277 | `si.setGI_H_ID(...)` |
| 279 | `si.setEOI_ID(...)` |
| 286 | `si.setAMOUNT(...)` |
| 287 | `si.setGOODS_R_ID(...)` |
| 288 | `si.setGR_REF_NO(...)` |
| 292 | `si.setBRANDNAME(...)` |
| 300 | `si.setPACKERNAME(...)` |
| 308 | `si.setEMARTLOGIS_NAME(...)` |

---

### Step 2-3. selectqueryShipmentBL [DBHandler.java] (라인 333-413)

**파일**: `DBHandler.java`

**SELECT 컬럼:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 335 | `+ DBInfo.GI_H_ID + ", "` |
| 337 | `+ DBInfo.EOI_ID + ", "` |
| 344 | `+ DBInfo.AMOUNT + ", "` |
| 345 | `+ DBInfo.GOODS_R_ID + ", "` |
| 346 | `+ DBInfo.GR_REF_NO + ", "` |
| 350 | `+ DBInfo.BRANDNAME + ", "` |
| 358 | `+ DBInfo.PACKERNAME + ", "` |
| 366 | `+ DBInfo.EMARTLOGIS_NAME + ", "` |

**cursor setter:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 382 | `si.setGI_H_ID(...)` |
| 384 | `si.setEOI_ID(...)` |
| 391 | `si.setAMOUNT(...)` |
| 392 | `si.setGOODS_R_ID(...)` |
| 393 | `si.setGR_REF_NO(...)` |
| 397 | `si.setBRANDNAME(...)` |
| 405 | `si.setPACKERNAME(...)` |
| 413 | `si.setEMARTLOGIS_NAME(...)` |

---

### Step 2-4. selectqueryAllShipment [DBHandler.java] (라인 436-453)

**파일**: `DBHandler.java`
**수정 전:**
```java
String sqlStr = "SELECT "
        + DBInfo.GI_D_ID + ", "
        + DBInfo.EOI_ID
        + " FROM " + DBInfo.TABLE_NAME_SHIPMENT
        + " ORDER BY EOI_ID ASC";

si.setEOI_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EOI_ID")), ""));
```

**수정 후:**
```java
String sqlStr = "SELECT "
        + DBInfo.GI_D_ID
        + " FROM " + DBInfo.TABLE_NAME_SHIPMENT
        + " ORDER BY GI_D_ID ASC";

// EOI_ID setter 삭제
```

---

### Step 3. INSERT 쿼리 [DBHandler.java] (라인 621-703)

**파일**: `DBHandler.java`
**함수**: `insertqueryShipment()`

**INSERT 컬럼명:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 622 | `+ DBInfo.GI_H_ID + ", "` |
| 624 | `+ DBInfo.EOI_ID + ", "` |
| 631 | `+ DBInfo.AMOUNT + ", "` |
| 632 | `+ DBInfo.GOODS_R_ID + ", "` |
| 633 | `+ DBInfo.GR_REF_NO + ", "` |
| 637 | `+ DBInfo.BRANDNAME + ", "` |
| 645 | `+ DBInfo.PACKERNAME + ", "` |
| 653 | `+ DBInfo.EMARTLOGIS_NAME + ", "` |

**INSERT VALUES:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 663 | `+ Common.nullCheck(si.getGI_H_ID(), "") + "','"` |
| 665 | `+ Common.nullCheck(si.getEOI_ID(), "") + "','"` |
| 672 | `+ Common.nullCheck(si.getAMOUNT(), "") + "','"` |
| 673 | `+ Common.nullCheck(si.getGOODS_R_ID(), "") + "','"` |
| 674 | `+ Common.nullCheck(si.getGR_REF_NO(), "") + "','"` |
| 678 | `+ Common.nullCheck(si.getBRANDNAME(), "") + "','"` |
| 686 | `+ Common.nullCheck(si.getPACKERNAME(), "") + "','"` |
| 694 | `+ Common.nullCheck(si.getEMARTLOGIS_NAME(), "") + "','"` |

---

### Step 4. DB 마이그레이션 [DBHelper.java]

**파일**: `DBHelper.java` (라인 22, 40-65)

```java
// 1. DB 버전 증가 (라인 22)
private static final int DATABASE_VERSION = 28;  // 기존 27 → 28

// 2. onUpgrade 메소드 (라인 40-65)
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 28) {
        db.execSQL("DROP TABLE IF EXISTS " + DBInfo.TABLE_NAME_SHIPMENT);
    }
}
```

---

### Step 5. 필드/getter/setter 삭제 [Shipments_Info.java]

**파일**: `Shipments_Info.java`
**삭제할 필드:**

| 컬럼명 | 필드 | getter | setter |
|--------|------|--------|--------|
| GI_H_ID | `private String GI_H_ID;` | `getGI_H_ID()` | `setGI_H_ID()` |
| EOI_ID | `private String EOI_ID;` | `getEOI_ID()` | `setEOI_ID()` |
| AMOUNT | `private String AMOUNT;` | `getAMOUNT()` | `setAMOUNT()` |
| GOODS_R_ID | `private String GOODS_R_ID;` | `getGOODS_R_ID()` | `setGOODS_R_ID()` |
| GR_REF_NO | `private String GR_REF_NO;` | `getGR_REF_NO()` | `setGR_REF_NO()` |
| BRANDNAME | `private String BRANDNAME;` | `getBRANDNAME()` | `setBRANDNAME()` |
| PACKERNAME | `private String PACKERNAME;` | `getPACKERNAME()` | `setPACKERNAME()` |
| EMARTLOGIS_NAME | `private String EMARTLOGIS_NAME;` | `getEMARTLOGIS_NAME()` | `setEMARTLOGIS_NAME()` |

---

### Step 6. temp[] 인덱스 재조정 [ProgressDlgShipSearch.java]

**파일**: `ProgressDlgShipSearch.java`
**⚠️ 중요: VIEW 수정으로 인해 temp[] 인덱스 전체 재조정 필요**

#### 6.1 삭제할 setter (8개)

| 현재 인덱스 | 삭제 코드 |
|:-----------:|-----------|
| temp[0] | `si.setGI_H_ID(temp[0].toString());` |
| temp[2] | `si.setEOI_ID(temp[2].toString());` |
| temp[9] | `si.setAMOUNT(temp[9].toString());` |
| temp[10] | `si.setGOODS_R_ID(temp[10].toString());` |
| temp[11] | `si.setGR_REF_NO(temp[11].toString());` |
| temp[15] | `si.setBRANDNAME(temp[15].toString());` |
| temp[23] | `si.setPACKERNAME(temp[23].toString());` |
| temp[31] | `si.setEMARTLOGIS_NAME(temp[31].toString());` |

#### 6.2 인덱스 재조정 매핑표 (38개 → 30개)

| 현재 idx | 컬럼명 | 삭제 | 새 idx |
|:--------:|--------|:----:|:------:|
| 0 | GI_H_ID | ❌ | - |
| 1 | GI_D_ID | | **0** |
| 2 | EOI_ID | ❌ | - |
| 3 | ITEM_CODE | | **1** |
| 4 | ITEM_NAME | | **2** |
| 5 | EMARTITEM_CODE | | **3** |
| 6 | EMARTITEM | | **4** |
| 7 | GI_REQ_PKG | | **5** |
| 8 | GI_REQ_QTY | | **6** |
| 9 | AMOUNT | ❌ | - |
| 10 | GOODS_R_ID | ❌ | - |
| 11 | GR_REF_NO | ❌ | - |
| 12 | GI_REQ_DATE | | **7** |
| 13 | BL_NO | | **8** |
| 14 | BRAND_CODE | | **9** |
| 15 | BRANDNAME | ❌ | - |
| 16 | CLIENT_CODE | | **10** |
| 17 | CLIENTNAME | | **11** |
| 18 | CENTERNAME | | **12** |
| 19 | ITEM_SPEC | | **13** |
| 20 | CT_CODE | | **14** |
| 21 | IMPORT_ID_NO | | **15** |
| 22 | PACKER_CODE | | **16** |
| 23 | PACKERNAME | ❌ | - |
| 24 | PACKER_PRODUCT_CODE | | **17** |
| 25 | BARCODE_TYPE | | **18** |
| 26 | ITEM_TYPE | | **19** |
| 27 | PACKWEIGHT | | **20** |
| 28 | BARCODEGOODS | | **21** |
| 29 | STORE_IN_DATE | | **22** |
| 30 | EMARTLOGIS_CODE | | **23** |
| 31 | EMARTLOGIS_NAME | ❌ | - |
| 32 | WH_AREA | | **24** |
| 33 | USE_NAME / LAST_BOX_ORDER | | **25** |
| 34 | USE_CODE | | **26** |
| 35 | CT_NAME | | **27** |
| 36 | STORE_CODE | | **28** |
| 37 | EMART_PLANT_CODE | | **29** |

#### 6.3 수정 전/후 비교

**수정 전:**
```java
si.setGI_H_ID(temp[0].toString());           // 삭제
si.setGI_D_ID(temp[1].toString());           // temp[0]으로 변경
si.setEOI_ID(temp[2].toString());            // 삭제
si.setITEM_CODE(temp[3].toString());         // temp[1]로 변경
si.setITEM_NAME(temp[4].toString());         // temp[2]로 변경
...
si.setWH_AREA(temp[32].toString());          // temp[24]로 변경
si.setUSE_NAME(temp[33].toString());         // temp[25]로 변경
```

**수정 후:**
```java
si.setGI_D_ID(temp[0].toString());           // 1 → 0
si.setITEM_CODE(temp[1].toString());         // 3 → 1
si.setITEM_NAME(temp[2].toString());         // 4 → 2
si.setEMARTITEM_CODE(temp[3].toString());    // 5 → 3
si.setEMARTITEM(temp[4].toString());         // 6 → 4
si.setGI_REQ_PKG(temp[5].toString());        // 7 → 5
si.setGI_REQ_QTY(temp[6].toString());        // 8 → 6
si.setGI_REQ_DATE(temp[7].toString());       // 12 → 7
si.setBL_NO(temp[8].toString());             // 13 → 8
si.setBRAND_CODE(temp[9].toString());        // 14 → 9
si.setCLIENT_CODE(temp[10].toString());      // 16 → 10
si.setCLIENTNAME(temp[11].toString());       // 17 → 11
si.setCENTERNAME(temp[12].toString());       // 18 → 12
si.setITEM_SPEC(temp[13].toString());        // 19 → 13
si.setCT_CODE(temp[14].toString());          // 20 → 14
si.setIMPORT_ID_NO(temp[15].toString());     // 21 → 15
si.setPACKER_CODE(temp[16].toString());      // 22 → 16
si.setPACKER_PRODUCT_CODE(temp[17].toString()); // 24 → 17
si.setBARCODE_TYPE(temp[18].toString());     // 25 → 18
si.setITEM_TYPE(temp[19].toString());        // 26 → 19
si.setPACKWEIGHT(temp[20].toString());       // 27 → 20
si.setBARCODEGOODS(temp[21].toString());     // 28 → 21
si.setSTORE_IN_DATE(temp[22].toString());    // 29 → 22
si.setEMARTLOGIS_CODE(temp[23].toString());  // 30 → 23
si.setWH_AREA(temp[24].toString());          // 32 → 24
si.setUSE_NAME(temp[25].toString());         // 33 → 25
si.setUSE_CODE(temp[26].toString());         // 34 → 26
si.setCT_NAME(temp[27].toString());          // 35 → 27
si.setSTORE_CODE(temp[28].toString());       // 36 → 28
si.setEMART_PLANT_CODE(temp[29].toString()); // 37 → 29
```

---

### Step 7. 테스트 데이터 수정 [TestDataHelper.java]

**파일**: `TestDataHelper.java`
**삭제할 setter:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 54 | WHERE GI_H_ID 조건 → WHERE GI_D_ID 조건으로 변경 |
| 83, 115, 141, 170, 194, 223 | `si.setGI_H_ID(...)` |
| 85, 117, 143, 172, 196, 225 | `si.setEOI_ID(...)` |
| 252 | `si.setAMOUNT(...)` |
| 255 | `si.setBRANDNAME(...)` |
| 258 | `si.setPACKERNAME(...)` |
| 261 | `si.setGOODS_R_ID(...)` |
| 262 | `si.setGR_REF_NO(...)` |
| 269 | `si.setEMARTLOGIS_NAME(...)` |

---

### Step 8. 상수 삭제 [DBInfo.java]

**파일**: `DBInfo.java`

| 라인 | 삭제 코드 |
|:----:|-----------|
| 24 | `public static final String GI_H_ID = "GI_H_ID";` |
| 26 | `public static final String EOI_ID = "EOI_ID";` |
| 33 | `public static final String AMOUNT = "AMOUNT";` |
| 34 | `public static final String GOODS_R_ID = "GOODS_R_ID";` |
| 35 | `public static final String GR_REF_NO = "GR_REF_NO";` |
| 39 | `public static final String BRANDNAME = "BRANDNAME";` |
| 47 | `public static final String PACKERNAME = "PACKERNAME";` |
| 57 | `public static final String EMARTLOGIS_NAME = "EMARTLOGIS_NAME";` |

---

### Step 9. 주석 정리 (선택) [ShipmentActivity.java, BixolonShipmentActivity.java]

**파일**: `ShipmentActivity.java`, `BixolonShipmentActivity.java`
**코드 동작에 영향 없음 - 문서화 목적으로 정리**

#### 9.1 ShipmentActivity.java

| 라인 | 수정 내용 |
|:----:|-----------|
| 1963 | Javadoc 주석에서 `EMARTLOGIS_NAME` 언급 삭제/수정 |
| 2468 | 주석 처리된 Log 코드 삭제 (`// Log.i(...EMARTLOGIS_NAME...)`) |

#### 9.2 BixolonShipmentActivity.java

| 라인 | 수정 내용 |
|:----:|-----------|
| 2049 | Javadoc 주석에서 `EMARTLOGIS_NAME` 언급 삭제/수정 |

---

## 6. 테스트

### 6.1 컴파일 테스트

```
[ ] Android Studio → Build → Rebuild Project
[ ] 컴파일 에러 없음
[ ] APK 생성 성공
```

### 6.2 기능 테스트

| # | 시나리오 | 예상 결과 |
|:-:|----------|-----------|
| 1 | 신규 설치 | 33컬럼 테이블 생성 |
| 2 | 업그레이드 | 테이블 재생성 |
| 3 | 출하대상 조회 | 정상 파싱, 저장 |
| 4 | 계근 | 정상 동작 |
| 5 | 서버 전송 | 정상 전송 |

---

## 7. 체크리스트

```
=== 서버 수정 ===

Step S-1. VIEW 수정 [VIEW 7개]
    [x] VW_PDA_WID_LIST
    [x] VW_PDA_WID_PRO_LIST
    [x] VW_PDA_WID_HOMEPLUS_LIST
    [x] VW_PDA_WID_WHOLESALE_LIST
    [x] VW_PDA_WID_LIST_NONFIXED
    [x] VW_PDA_WID_LIST_NONFIXED_HP
    [x] VW_PDA_WID_LIST_LOTTE

Step S-2. JSP 수정 [JSP 17개 파일]
    [x] search_shipment.jsp
    [x] search_shipment_homeplus.jsp
    [x] search_shipment_lotte.jsp
    [x] search_shipment_ono.jsp
    [x] search_shipment_ono_temp.jsp
    [x] search_shipment_ono_temp_diff_prd.jsp
    [x] search_shipment_wholesale.jsp
    [x] search_homeplus_nonfixed.jsp
    [x] search_homeplus_nonfixed2.jsp (주석만 있어 수정 불필요)
    [x] search_production.jsp
    [x] search_production_4label.jsp
    [x] search_production_nonfixed.jsp
    [x] search_barcode_info.jsp (주석만 있어 수정 불필요)
    [x] search_barcode_info_temp.jsp (주석만 있어 수정 불필요)
    [x] search_barcode_info_temp_diff_prd.jsp
    [x] search_barcode_info_nonfixed.jsp (주석만 있어 수정 불필요)
    [x] search_goods_wet.jsp (주석만 있어 수정 불필요)

=== 앱 수정 ===

Step 1. CREATE TABLE [DBHandler.java]
    [x] createqueryShipment - 8개 컬럼 삭제

Step 2. SELECT 쿼리 [DBHandler.java]
    [x] selectqueryShipment - SELECT 8개 + cursor 8개
    [x] selectqueryShipmentOnly - SELECT 8개 + cursor 8개
    [x] selectqueryShipmentBL - SELECT 8개 + cursor 8개
    [x] selectqueryAllShipment - EOI_ID + ORDER BY

Step 3. INSERT 쿼리 [DBHandler.java]
    [x] insertqueryShipment - 컬럼명 8개 + VALUES 8개

Step 4. DB 마이그레이션 [DBHelper.java]
    [x] DATABASE_VERSION 27 → 28 증가 (라인 22)
    [x] onUpgrade() 수정 (라인 40-65)

Step 5. 필드/getter/setter 삭제 [Shipments_Info.java]
    [ ] 8개 필드 삭제
    [ ] 8개 getter 삭제
    [ ] 8개 setter 삭제

Step 6. temp[] 인덱스 재조정 [ProgressDlgShipSearch.java]
    [ ] 8개 setter 호출 삭제
    [ ] 30개 setter temp[] 인덱스 재조정

Step 7. 테스트 데이터 [TestDataHelper.java]
    [ ] WHERE 조건 변경 + setter 제거

Step 8. 상수 삭제 [DBInfo.java]
    [ ] 8개 상수 삭제

Step 9. 주석 정리 (선택) [ShipmentActivity.java, BixolonShipmentActivity.java]
    [ ] ShipmentActivity.java - Javadoc 주석 + 주석 처리된 Log 삭제
    [ ] BixolonShipmentActivity.java - Javadoc 주석 수정

Step 10. 테스트
    [ ] 컴파일 성공
    [ ] 신규 설치 테스트
    [ ] 업그레이드 테스트
    [ ] 기능 테스트
```

---

## 8. 수정 결과

| 항목 | 수정 전 | 수정 후 |
|------|:-------:|:-------:|
| VIEW 컬럼 | 38개 | 30개 |
| TB_SHIPMENT 컬럼 | 41개 | 33개 |
| temp[] 인덱스 | 38개 | 30개 |
| 수정 파일 (앱) | - | 8개 |
| 수정 파일 (서버) | - | VIEW 7개 + JSP 17개 |

---

## 9. 핵심 요약

```
1. 서버 VIEW 7개 + JSP 17개 수정 (8개 컬럼 삭제)
2. DBHandler CREATE/INSERT/SELECT 수정
3. Shipments_Info 8개 필드/getter/setter 삭제
4. ProgressDlgShipSearch 8개 setter 삭제 + temp[] 인덱스 전체 재조정
5. TestDataHelper setter 호출 삭제
6. DBInfo 8개 상수 삭제
7. ShipmentActivity, BixolonShipmentActivity 주석 정리 (선택)
```

---

**문서 작성일**: 2026-01-16
**관련 문서**: VIEW_로컬DB_컬럼비교.md, 로컬DB_구조.md
