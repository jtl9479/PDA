# DBHandler.java - 데이터베이스 CRUD 핸들러

## 개요
DBHandler 클래스는 Highland E-mart PDA 애플리케이스의 데이터베이스 CRUD(Create, Read, Update, Delete) 작업을 수행하는 핸들러 클래스입니다. 모든 데이터베이스 테이블에 대한 생성, 조회, 삽입, 수정, 삭제 작업을 static 메서드로 제공합니다.

- **파일 경로**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\db\DBHandler.java`
- **패키지**: `com.rgbsolution.highland_emart.db`
- **총 라인 수**: 2101줄
- **주요 기능**: 데이터베이스 CRUD 작업

## 클래스 구조

### 주요 필드

```java
private static final String TAG = "DBHandler";
static SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
static SimpleDateFormat timeformat = new SimpleDateFormat("HHmmss");
```

### Import 클래스
- `Shipments_Info`: 출하 정보 데이터 모델
- `Barcodes_Info`: 바코드 정보 데이터 모델
- `Goodswets_Info`: 계근 정보 데이터 모델

## 메서드 분류

### 1. TB_SHIPMENT (출하대상) 관련 메서드

#### 1.1 createqueryShipment (25-86줄)

```java
public static void createqueryShipment(Context context)
```

**설명**: TB_SHIPMENT 테이블을 생성합니다.

**테이블 구조**:
- SHIPMENT_ID (INTEGER PRIMARY KEY AUTOINCREMENT)
- GI_H_ID, GI_D_ID, EOI_ID (TEXT NOT NULL)
- ITEM_CODE, ITEM_NAME (TEXT NOT NULL)
- EMARTITEM_CODE, EMARTITEM (TEXT)
- GI_REQ_PKG, GI_REQ_QTY, AMOUNT (TEXT NOT NULL)
- GOODS_R_ID, GR_REF_NO, GI_REQ_DATE (TEXT NOT NULL)
- BL_NO, BRAND_CODE, BRANDNAME (TEXT NOT NULL)
- CLIENT_CODE, CLIENTNAME, CENTERNAME (TEXT)
- ITEM_SPEC, CT_CODE, IMPORT_ID_NO (TEXT NOT NULL)
- PACKER_CODE, PACKERNAME, PACKER_PRODUCT_CODE (TEXT NOT NULL)
- BARCODE_TYPE, ITEM_TYPE (TEXT NOT NULL)
- PACKWEIGHT, BARCODEGOODS (TEXT)
- STORE_IN_DATE, EMARTLOGIS_CODE, EMARTLOGIS_NAME (TEXT)
- SAVE_TYPE (TEXT NOT NULL)
- WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE (TEXT)
- EMART_PLANT_CODE (TEXT)
- LAST_BOX_ORDER (INTEGER)

**사용 예시**:
```java
DBHandler.createqueryShipment(context);
```

#### 1.2 selectqueryShipment (89-220줄)

```java
public static ArrayList<Shipments_Info> selectqueryShipment(
    Context context,
    String center_name,
    String condition,
    boolean type
)
```

**설명**: 센터명과 조건으로 출하 정보를 조회합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `center_name`: 센터명
- `condition`: 검색 조건 (패커 상품코드 또는 BL번호)
- `type`: true=패커 상품코드, false=BL번호

**반환**: `ArrayList<Shipments_Info>` - 출하 정보 리스트


**SQL 쿼리**:
```sql
SELECT * FROM TB_SHIPMENT
WHERE CENTERNAME = ?
  AND (PACKER_PRODUCT_CODE in (?) OR BL_NO = ?)
  AND GI_REQ_DATE = ?
ORDER BY SAVE_TYPE ASC, SHIPMENT_ID ASC, CLIENTNAME ASC
```

**사용 예시**:
```java
ArrayList<Shipments_Info> list = DBHandler.selectqueryShipment(
    context,
    "센터A",
    "PROD001",
    true
);
```

#### 1.3 selectqueryShipmentOnly (221-324줄)

```java
public static ArrayList<Shipments_Info> selectqueryShipmentOnly(
    Context context,
    String barcodegoods
)
```

**설명**: 바코드 상품코드로 출하 정보를 조회합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `barcodegoods`: 바코드 상품코드

**반환**: `ArrayList<Shipments_Info>` - 출하 정보 리스트

#### 1.4 selectqueryShipmentBL (325-430줄)

```java
public static ArrayList<Shipments_Info> selectqueryShipmentBL(
    Context context,
    String center_name,
    String bl_no
)
```

**설명**: BL번호로 출하 정보를 조회합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `center_name`: 센터명
- `bl_no`: BL번호

**반환**: `ArrayList<Shipments_Info>` - 출하 정보 리스트

#### 1.5 selectqueryAllShipment (431-469줄)

```java
public static ArrayList<Shipments_Info> selectqueryAllShipment(Context context)
```

**설명**: 모든 출하 정보를 조회합니다.

**반환**: `ArrayList<Shipments_Info>` - 전체 출하 정보 리스트

#### 1.6 selectqueryAllProduction (470-508줄)

```java
public static ArrayList<Shipments_Info> selectqueryAllProduction(Context context)
```

**설명**: 모든 생산 정보를 조회합니다.

**반환**: `ArrayList<Shipments_Info>` - 전체 생산 정보 리스트

#### 1.7 selectqueryCodeList (509-546줄)

```java
public static ArrayList<String[]> selectqueryCodeList(Context context)
```

**설명**: 고정 거래처의 코드 리스트를 조회합니다.

**반환**: `ArrayList<String[]>` - [패커코드, 패커상품코드, 패커상품명] 배열의 리스트

**SQL 쿼리**:
```sql
SELECT DISTINCT PACKER_CODE, PACKER_PRODUCT_CODE, ITEM_NAME
FROM TB_SHIPMENT
WHERE CLIENT_TYPE != '1' AND SAVE_TYPE = 'N'
ORDER BY PACKER_CODE, PACKER_PRODUCT_CODE
```

#### 1.8 selectqueryCodeListForNonFixed (547-584줄)

```java
public static ArrayList<String[]> selectqueryCodeListForNonFixed(Context context)
```

**설명**: 비고정 거래처의 코드 리스트를 조회합니다.

**반환**: `ArrayList<String[]>` - [패커코드, 패커상품코드, 패커상품명] 배열의 리스트

#### 1.9 selectqueryGIDIDList (585-620줄)

```java
public static ArrayList<String> selectqueryGIDIDList(Context context)
```

**설명**: 고유한 GI_D_ID 리스트를 조회합니다.

**반환**: `ArrayList<String>` - GI_D_ID 리스트

**SQL 쿼리**:
```sql
SELECT DISTINCT GI_D_ID FROM TB_SHIPMENT
WHERE GI_REQ_DATE = ?
ORDER BY GI_D_ID
```

#### 1.10 selectqueryCenterList (621-656줄)

```java
public static ArrayList<String> selectqueryCenterList(Context context)
```

**설명**: 고유한 센터명 리스트를 조회합니다.

**반환**: `ArrayList<String>` - 센터명 리스트

**SQL 쿼리**:
```sql
SELECT DISTINCT CENTERNAME FROM TB_SHIPMENT
WHERE GI_REQ_DATE = ?
ORDER BY CENTERNAME
```

#### 1.11 insertqueryShipment (657-765줄)

```java
public static boolean insertqueryShipment(
    Context context,
    Shipments_Info si,
    String saveType
)
```

**설명**: 출하 정보를 삽입합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `si`: Shipments_Info 객체
- `saveType`: 저장 타입 ('Y' 또는 'N')

**반환**: `boolean` - 성공 여부

**SQL 쿼리**:
```sql
INSERT INTO TB_SHIPMENT (GI_H_ID, GI_D_ID, EOI_ID, ...)
VALUES (?, ?, ?, ...)
```

**사용 예시**:
```java
Shipments_Info si = new Shipments_Info();
si.setGI_H_ID("GH001");
si.setGI_D_ID("GD001");
// ... 기타 필드 설정
boolean result = DBHandler.insertqueryShipment(context, si, "N");
```

#### 1.12 deletequeryShipment (766-786줄)

```java
public static void deletequeryShipment(Context context)
```

**설명**: TB_SHIPMENT 테이블의 모든 데이터를 삭제하고 자동증가 카운터를 초기화합니다.

**SQL 쿼리**:
```sql
DELETE FROM TB_SHIPMENT;
DELETE FROM sqlite_sequence WHERE name = 'TB_SHIPMENT';
```

**사용 예시**:
```java
DBHandler.deletequeryShipment(context);
```

#### 1.13 updatequeryShipment (789-812줄)

```java
public static void updatequeryShipment(
    Context context,
    String gi_d_id,
    String packer_product_code
)
```

**설명**: 출하 정보의 저장 타입을 'Y'로 업데이트합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `gi_d_id`: 출고상세번호
- `packer_product_code`: 패커 상품코드

**SQL 쿼리**:
```sql
UPDATE TB_SHIPMENT
SET SAVE_TYPE = 'Y'
WHERE GI_D_ID = ? AND PACKER_PRODUCT_CODE = ?
```

**사용 예시**:
```java
DBHandler.updatequeryShipment(context, "GD001", "PROD001");
```

#### 1.14 refreshShipmentList (1916-1953줄)

```java
public static boolean refreshShipmentList(
    Context context,
    ArrayList<String> list_delete,
    ArrayList<Shipments_Info> list_insert
)
```

**설명**: 출하 리스트를 갱신합니다 (삭제 후 삽입).

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `list_delete`: 삭제할 SHIPMENT_ID 리스트
- `list_insert`: 삽입할 Shipments_Info 리스트

**반환**: `boolean` - 성공 여부

**동작**:
1. list_delete의 ID들을 삭제
2. list_insert의 데이터들을 삽입

---

### 2. TB_BARCODE_INFO (바코드정보) 관련 메서드

#### 2.1 createqueryBarcodeInfo (815-859줄)

```java
public static void createqueryBarcodeInfo(Context context)
```

**설명**: TB_BARCODE_INFO 테이블을 생성합니다.

**테이블 구조**:
- BARCODE_INFO_ID (INTEGER PRIMARY KEY AUTOINCREMENT)
- PACKER_CLIENT_CODE, PACKER_PRODUCT_CODE, PACKER_PRD_NAME (TEXT NOT NULL)
- ITEM_CODE (TEXT NOT NULL)
- ITEM_NAME_KR, BRAND_CODE, BARCODEGOODS (TEXT)
- BASEUNIT, ZEROPOINT (TEXT NOT NULL)
- PACKER_PRD_CODE_FROM, PACKER_PRD_CODE_TO (TEXT)
- BARCODEGOODS_FROM, BARCODEGOODS_TO (TEXT NOT NULL)
- WEIGHT_FROM, WEIGHT_TO (TEXT NOT NULL)
- MAKINGDATE_FROM, MAKINGDATE_TO (TEXT)
- BOXSERIAL_FROM, BOXSERIAL_TO (TEXT)
- STATUS, REG_ID (TEXT)
- REG_DATE, REG_TIME, MEMO (TEXT)
- SHELF_LIFE (TEXT)

**사용 예시**:
```java
DBHandler.createqueryBarcodeInfo(context);
```

#### 2.2 selectqueryBarcodeInfo (862-976줄)

```java
public static ArrayList<Barcodes_Info> selectqueryBarcodeInfo(Context context)
```

**설명**: 모든 바코드 정보를 조회합니다.

**반환**: `ArrayList<Barcodes_Info>` - 바코드 정보 리스트

**SQL 쿼리**:
```sql
SELECT BARCODE_INFO_ID, PACKER_CLIENT_CODE, PACKER_PRODUCT_CODE,
       PACKER_PRD_NAME, ITEM_CODE, ITEM_NAME_KR, BRAND_CODE,
       BARCODEGOODS, BASEUNIT, ZEROPOINT,
       PACKER_PRD_CODE_FROM, PACKER_PRD_CODE_TO,
       BARCODEGOODS_FROM, BARCODEGOODS_TO,
       WEIGHT_FROM, WEIGHT_TO,
       MAKINGDATE_FROM, MAKINGDATE_TO,
       BOXSERIAL_FROM, BOXSERIAL_TO,
       STATUS, REG_ID, REG_DATE, REG_TIME, MEMO, SHELF_LIFE
FROM TB_BARCODE_INFO
```

**사용 예시**:
```java
ArrayList<Barcodes_Info> list = DBHandler.selectqueryBarcodeInfo(context);
for (Barcodes_Info bi : list) {
    Log.d(TAG, "Barcode: " + bi.getBARCODEGOODS());
}
```

#### 2.3 selectqueryBarcodeGoodsInfo (977-1091줄)

```java
public static ArrayList<Barcodes_Info> selectqueryBarcodeGoodsInfo(Context context)
```

**설명**: 바코드 상품 정보를 조회합니다 (selectqueryBarcodeInfo와 동일).

**반환**: `ArrayList<Barcodes_Info>` - 바코드 정보 리스트

#### 2.4 insertqueryBarcodeInfo (1092-1163줄)

```java
public static boolean insertqueryBarcodeInfo(
    Context context,
    Barcodes_Info bi
)
```

**설명**: 바코드 정보를 삽입합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `bi`: Barcodes_Info 객체

**반환**: `boolean` - 성공 여부

**SQL 쿼리**:
```sql
INSERT INTO TB_BARCODE_INFO (
    PACKER_CLIENT_CODE, PACKER_PRODUCT_CODE, PACKER_PRD_NAME,
    ITEM_CODE, ITEM_NAME_KR, BRAND_CODE, BARCODEGOODS,
    BASEUNIT, ZEROPOINT,
    PACKER_PRD_CODE_FROM, PACKER_PRD_CODE_TO,
    BARCODEGOODS_FROM, BARCODEGOODS_TO,
    WEIGHT_FROM, WEIGHT_TO,
    MAKINGDATE_FROM, MAKINGDATE_TO,
    BOXSERIAL_FROM, BOXSERIAL_TO,
    STATUS, REG_ID, REG_DATE, REG_TIME, MEMO, SHELF_LIFE
) VALUES (?, ?, ?, ...)
```

**사용 예시**:
```java
Barcodes_Info bi = new Barcodes_Info();
bi.setPACKER_CLIENT_CODE("PC001");
bi.setPACKER_PRODUCT_CODE("PROD001");
// ... 기타 필드 설정
boolean result = DBHandler.insertqueryBarcodeInfo(context, bi);
```

#### 2.5 updatequeryBarcodeInfo (1164-1203줄)

```java
public static void updatequeryBarcodeInfo(
    Context context,
    HashMap<String, String> hTemp
)
```

**설명**: 바코드 정보를 업데이트합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `hTemp`: 업데이트할 필드와 값을 담은 HashMap

**SQL 쿼리**:
```sql
UPDATE TB_BARCODE_INFO
SET PACKER_PRODUCT_CODE = ?, PACKER_PRD_NAME = ?,
    ITEM_CODE = ?, ITEM_NAME_KR = ?,
    BRAND_CODE = ?, BARCODEGOODS = ?,
    BASEUNIT = ?, ZEROPOINT = ?,
    PACKER_PRD_CODE_FROM = ?, PACKER_PRD_CODE_TO = ?,
    BARCODEGOODS_FROM = ?, BARCODEGOODS_TO = ?,
    WEIGHT_FROM = ?, WEIGHT_TO = ?,
    MAKINGDATE_FROM = ?, MAKINGDATE_TO = ?,
    BOXSERIAL_FROM = ?, BOXSERIAL_TO = ?,
    STATUS = ?, REG_ID = ?, REG_DATE = ?, REG_TIME = ?,
    MEMO = ?, SHELF_LIFE = ?
WHERE PACKER_CLIENT_CODE = ?
```

**사용 예시**:
```java
HashMap<String, String> hTemp = new HashMap<>();
hTemp.put("PACKER_CLIENT_CODE", "PC001");
hTemp.put("PACKER_PRODUCT_CODE", "PROD001");
// ... 기타 필드 설정
DBHandler.updatequeryBarcodeInfo(context, hTemp);
```

#### 2.6 deletequeryBarcodeInfo (1204-1225줄)

```java
public static void deletequeryBarcodeInfo(Context context)
```

**설명**: TB_BARCODE_INFO 테이블의 모든 데이터를 삭제하고 자동증가 카운터를 초기화합니다.

**SQL 쿼리**:
```sql
DELETE FROM TB_BARCODE_INFO;
DELETE FROM sqlite_sequence WHERE name = 'TB_BARCODE_INFO';
```

**사용 예시**:
```java
DBHandler.deletequeryBarcodeInfo(context);
```

#### 2.7 selectquerySearchBarcodeInfo (1857-1915줄)

```java
public static ArrayList<HashMap<String, String>> selectquerySearchBarcodeInfo(
    Context context,
    String packer_product_code,
    String brand_code
)
```

**설명**: 패커 상품코드와 브랜드코드로 바코드 정보를 검색합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `packer_product_code`: 패커 상품코드
- `brand_code`: 브랜드코드

**반환**: `ArrayList<HashMap<String, String>>` - 바코드 정보 맵 리스트

**SQL 쿼리**:
```sql
SELECT * FROM TB_BARCODE_INFO
WHERE PACKER_PRODUCT_CODE = ? AND BRAND_CODE = ?
```

---

### 3. TB_GOODS_WET (계근작업) 관련 메서드

#### 3.1 createqueryGoodsWet (1226-1266줄)

```java
public static void createqueryGoodsWet(Context context)
```

**설명**: TB_GOODS_WET 테이블을 생성합니다.

**테이블 구조**:
- GOODS_WET_ID (INTEGER PRIMARY KEY AUTOINCREMENT)
- GI_D_ID, WEIGHT, WEIGHT_UNIT (TEXT NOT NULL)
- PACKER_PRODUCT_CODE (TEXT NOT NULL)
- BARCODE (TEXT)
- PACKER_CLIENT_CODE (TEXT NOT NULL)
- MAKINGDATE, BOXSERIAL (TEXT)
- BOX_CNT (INTEGER NOT NULL)
- EMARTITEM_CODE, EMARTITEM, ITEM_CODE, BRAND_CODE (TEXT)
- REG_ID, REG_DATE (TEXT)
- REG_TIME, SAVE_TYPE (TEXT NOT NULL)
- MEMO, DUPLICATE, CLIENT_TYPE (TEXT)
- BOX_ORDER (Integer DEFAULT 0)

**사용 예시**:
```java
DBHandler.createqueryGoodsWet(context);
```

#### 3.2 selectqueryGoodsWet (1269-1338줄)

```java
public static ArrayList<Goodswets_Info> selectqueryGoodsWet(
    Context context,
    String gi_d_id,
    String packer_product_code,
    String client_code
)
```

**설명**: 계근 상품 작업내역을 조회합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `gi_d_id`: 출고상세번호
- `packer_product_code`: 패커 상품코드
- `client_code`: 거래처코드

**반환**: `ArrayList<Goodswets_Info>` - 계근 정보 리스트

**SQL 쿼리**:
```sql
SELECT GOODS_WET_ID, GI_D_ID, WEIGHT, WEIGHT_UNIT,
       PACKER_PRODUCT_CODE, BARCODE, PACKER_CLIENT_CODE,
       BOXSERIAL, BOX_CNT, EMARTITEM_CODE, EMARTITEM,
       REG_ID, SAVE_TYPE, MAKINGDATE, BOX_ORDER, DUPLICATE
FROM TB_GOODS_WET
WHERE GI_D_ID = ? AND PACKER_PRODUCT_CODE = ?
ORDER BY BOX_CNT DESC
```

**사용 예시**:
```java
ArrayList<Goodswets_Info> list = DBHandler.selectqueryGoodsWet(
    context,
    "GD001",
    "PROD001",
    "PC001"
);
```

#### 3.3 selectquerySendGoodsWet (1339-1412줄)

```java
public static ArrayList<Goodswets_Info> selectquerySendGoodsWet(
    Context context,
    String qry_where
)
```

**설명**: 전송할 계근 정보를 조회합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `qry_where`: WHERE 조건절

**반환**: `ArrayList<Goodswets_Info>` - 계근 정보 리스트

**SQL 쿼리**:
```sql
SELECT * FROM TB_GOODS_WET WHERE [qry_where]
```

#### 3.4 selectqueryListGoodsWetInfo (1413-1457줄)

```java
public static String[] selectqueryListGoodsWetInfo(
    Context context,
    String gi_d_id,
    String pp_code,
    String client_code
)
```

**설명**: 계근 정보의 요약 통계를 조회합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `gi_d_id`: 출고상세번호
- `pp_code`: 패커 상품코드
- `client_code`: 거래처코드

**반환**: `String[]` - [총 수량, 총 중량, 중량단위]

**SQL 쿼리**:
```sql
SELECT COUNT(1), SUM(WEIGHT), WEIGHT_UNIT
FROM TB_GOODS_WET
WHERE GI_D_ID = ? AND PACKER_PRODUCT_CODE = ?
```

**사용 예시**:
```java
String[] info = DBHandler.selectqueryListGoodsWetInfo(
    context,
    "GD001",
    "PROD001",
    "PC001"
);
int count = Integer.parseInt(info[0]);
double totalWeight = Double.parseDouble(info[1]);
String unit = info[2];
```

#### 3.5 duplicatequeryGoodsWet_check (1458-1491줄)

```java
public static boolean duplicatequeryGoodsWet_check(
    Context context,
    String barcode
)
```

**설명**: 바코드 중복 여부를 확인합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `barcode`: 확인할 바코드

**반환**: `boolean` - 중복이면 true, 아니면 false

**SQL 쿼리**:
```sql
SELECT COUNT(1) FROM TB_GOODS_WET WHERE BARCODE = ?
```

**사용 예시**:
```java
boolean isDuplicate = DBHandler.duplicatequeryGoodsWet_check(context, "BC12345");
if (isDuplicate) {
    Log.d(TAG, "중복 바코드입니다.");
}
```

#### 3.6 duplicatequeryGoodsWet (1492-1528줄)

```java
public static boolean duplicatequeryGoodsWet(
    Context context,
    String barcode,
    String gi_d_id,
    String pp_code
)
```

**설명**: 특정 출하건의 바코드 중복 여부를 확인합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `barcode`: 확인할 바코드
- `gi_d_id`: 출고상세번호
- `pp_code`: 패커 상품코드

**반환**: `boolean` - 중복이면 true, 아니면 false

**SQL 쿼리**:
```sql
SELECT COUNT(1) FROM TB_GOODS_WET
WHERE BARCODE = ?
  AND GI_D_ID = ?
  AND PACKER_PRODUCT_CODE = ?
```

#### 3.7 insertqueryGoodsWet (1529-1598줄)

```java
public static boolean insertqueryGoodsWet(
    Context context,
    Goodswets_Info gi
)
```

**설명**: 계근 정보를 삽입합니다 (기본).

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `gi`: Goodswets_Info 객체

**반환**: `boolean` - 성공 여부

**SQL 쿼리**:
```sql
INSERT INTO TB_GOODS_WET (
    GI_D_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE,
    BARCODE, PACKER_CLIENT_CODE, MAKINGDATE, BOXSERIAL,
    BOX_CNT, EMARTITEM_CODE, EMARTITEM, ITEM_CODE,
    BRAND_CODE, REG_ID, REG_DATE, REG_TIME,
    SAVE_TYPE, MEMO, DUPLICATE, CLIENT_TYPE
) VALUES (?, ?, ?, ...)
```

**사용 예시**:
```java
Goodswets_Info gi = new Goodswets_Info();
gi.setGI_D_ID("GD001");
gi.setWEIGHT("10.5");
gi.setWEIGHT_UNIT("KG");
// ... 기타 필드 설정
boolean result = DBHandler.insertqueryGoodsWet(context, gi);
```

#### 3.8 insertqueryGoodsWetHomeplus (1599-1671줄)

```java
public static boolean insertqueryGoodsWetHomeplus(
    Context context,
    Goodswets_Info gi,
    int maxBoxOrder
)
```

**설명**: 홈플러스용 계근 정보를 삽입합니다 (BOX_ORDER 포함).

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `gi`: Goodswets_Info 객체
- `maxBoxOrder`: 최대 박스 순서번호

**반환**: `boolean` - 성공 여부

**사용 예시**:
```java
int maxBoxOrder = DBHandler.selectMaxBoxOrder(context);
boolean result = DBHandler.insertqueryGoodsWetHomeplus(context, gi, maxBoxOrder);
```

#### 3.9 insertqueryGoodsWetLotte (1672-1746줄)

```java
public static boolean insertqueryGoodsWetLotte(
    Context context,
    Goodswets_Info gi,
    int lotte_TryCount
)
```

**설명**: 롯데용 계근 정보를 삽입합니다 (시도 횟수 포함).

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `gi`: Goodswets_Info 객체
- `lotte_TryCount`: 롯데 시도 횟수

**반환**: `boolean` - 성공 여부

#### 3.10 updatequeryGoodsWet (1747-1775줄)

```java
public static boolean updatequeryGoodsWet(
    Context context,
    String gi_d_id,
    String barcode,
    String box_cnt
)
```

**설명**: 계근 정보의 저장 타입을 'Y'로 업데이트합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `gi_d_id`: 출고상세번호
- `barcode`: 바코드
- `box_cnt`: 박스 카운트

**SQL 쿼리**:
```sql
UPDATE TB_GOODS_WET
SET SAVE_TYPE = 'Y'
WHERE GI_D_ID = ? AND BARCODE = ? AND BOX_CNT = ?
```

**사용 예시**:
```java
boolean result = DBHandler.updatequeryGoodsWet(context, "GD001", "BC12345", "1");
```

#### 3.11 deletequerySelectGoodsWet (1776-1811줄)

```java
public static void deletequerySelectGoodsWet(
    Context context,
    String gi_d_id,
    String barcode,
    int box_cnt
)
```

**설명**: 특정 계근 정보를 삭제합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `gi_d_id`: 출고상세번호
- `barcode`: 바코드
- `box_cnt`: 박스 카운트

**SQL 쿼리**:
```sql
DELETE FROM TB_GOODS_WET
WHERE GI_D_ID = ? AND BARCODE = ? AND BOX_CNT = ?
```

**사용 예시**:
```java
DBHandler.deletequerySelectGoodsWet(context, "GD001", "BC12345", 1);
```

#### 3.12 deletequeryGoodsWet (1812-1834줄)

```java
public static void deletequeryGoodsWet(Context context)
```

**설명**: TB_GOODS_WET 테이블에서 저장된 데이터만 삭제합니다.

**SQL 쿼리**:
```sql
DELETE FROM TB_GOODS_WET WHERE SAVE_TYPE = 'Y'
```

**사용 예시**:
```java
DBHandler.deletequeryGoodsWet(context);
```

#### 3.13 deletequeryAllGoodsWet (1835-1856줄)

```java
public static void deletequeryAllGoodsWet(Context context)
```

**설명**: TB_GOODS_WET 테이블의 모든 데이터를 삭제하고 자동증가 카운터를 초기화합니다.

**SQL 쿼리**:
```sql
DELETE FROM TB_GOODS_WET;
DELETE FROM sqlite_sequence WHERE name = 'TB_GOODS_WET';
```

**사용 예시**:
```java
DBHandler.deletequeryAllGoodsWet(context);
```

#### 3.14 selectMaxBoxOrder (1954-1997줄)

```java
public static int selectMaxBoxOrder(Context context)
```

**설명**: 최대 박스 순서번호를 조회합니다.

**반환**: `int` - 최대 BOX_ORDER 값 (없으면 0)

**SQL 쿼리**:
```sql
SELECT MAX(BOX_ORDER) FROM TB_GOODS_WET
```

**사용 예시**:
```java
int maxBoxOrder = DBHandler.selectMaxBoxOrder(context);
Log.d(TAG, "Max Box Order: " + maxBoxOrder);
```

---

### 4. TB_GOODS_WET_PRODUCTION_CALC (생산계근계산) 관련 메서드

#### 4.1 createqueryGoodsWetProductionCalc (1998-2017줄)

```java
public static void createqueryGoodsWetProductionCalc(Context context)
```

**설명**: TB_GOODS_WET_PRODUCTION_CALC 테이블을 생성합니다.

**테이블 구조**:
- BARCODE (TEXT) - 바코드만 저장하는 단순 테이블

**사용 예시**:
```java
DBHandler.createqueryGoodsWetProductionCalc(context);
```

#### 4.2 insertGoodsWetProductionCalc (2020-2047줄)

```java
public static boolean insertGoodsWetProductionCalc(
    Context context,
    String msg
)
```

**설명**: 생산계근 계산 데이터를 삽입합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `msg`: 바코드 메시지

**반환**: `boolean` - 성공 여부

**SQL 쿼리**:
```sql
INSERT INTO TB_GOODS_WET_PRODUCTION_CALC (BARCODE) VALUES (?)
```

**사용 예시**:
```java
boolean result = DBHandler.insertGoodsWetProductionCalc(context, "BC12345");
```

#### 4.3 selectGoodsWetProductionCalc (2050-2080줄)

```java
public static String selectGoodsWetProductionCalc(
    Context context,
    String msg
)
```

**설명**: 생산계근 바코드의 중복 개수를 조회합니다.

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `msg`: 바코드 메시지

**반환**: `String` - 중복 개수

**SQL 쿼리**:
```sql
SELECT COUNT(1) FROM TB_GOODS_WET_PRODUCTION_CALC WHERE BARCODE = ?
```

**사용 예시**:
```java
String count = DBHandler.selectGoodsWetProductionCalc(context, "BC12345");
int duplicateCount = Integer.parseInt(count);
```

#### 4.4 deleteGoodsWetProductionCalc (2082-2099줄)

```java
public static void deleteGoodsWetProductionCalc(Context context)
```

**설명**: TB_GOODS_WET_PRODUCTION_CALC 테이블의 모든 데이터를 삭제합니다.

**SQL 쿼리**:
```sql
DELETE FROM TB_GOODS_WET_PRODUCTION_CALC
```

**사용 예시**:
```java
DBHandler.deleteGoodsWetProductionCalc(context);
```

---

## 주요 사용 패턴

### 1. 테이블 초기화

```java
// 앱 시작 시 모든 테이블 생성
DBHandler.createqueryShipment(context);
DBHandler.createqueryBarcodeInfo(context);
DBHandler.createqueryGoodsWet(context);
DBHandler.createqueryGoodsWetProductionCalc(context);
```

### 2. 출하 정보 조회 및 처리

```java
// 센터와 패커 상품코드로 출하 정보 조회
ArrayList<Shipments_Info> shipmentList =
    DBHandler.selectqueryShipment(context, "센터A", "PROD001", true);

// 조회된 출하 정보 처리
for (Shipments_Info si : shipmentList) {
    Log.d(TAG, "상품명: " + si.getITEM_NAME());
    Log.d(TAG, "요청수량: " + si.getGI_REQ_PKG());
}
```

### 3. 계근 작업 처리

```java
// 바코드 스캔 후 계근 정보 저장
Goodswets_Info gi = new Goodswets_Info();
gi.setGI_D_ID("GD001");
gi.setWEIGHT("10.5");
gi.setWEIGHT_UNIT("KG");
gi.setPACKER_PRODUCT_CODE("PROD001");
gi.setBARCODE("BC12345");
gi.setSAVE_TYPE("N");

// 중복 체크
boolean isDuplicate = DBHandler.duplicatequeryGoodsWet(
    context,
    gi.getBARCODE(),
    gi.getGI_D_ID(),
    gi.getPACKER_PRODUCT_CODE()
);

if (!isDuplicate) {
    // 계근 정보 저장
    boolean result = DBHandler.insertqueryGoodsWet(context, gi);
    if (result) {
        Log.d(TAG, "계근 정보 저장 성공");
    }
} else {
    Log.d(TAG, "중복 바코드입니다.");
}
```

### 4. 계근 통계 조회

```java
// 계근 작업 통계 조회
String[] info = DBHandler.selectqueryListGoodsWetInfo(
    context,
    "GD001",
    "PROD001",
    "PC001"
);

int totalCount = Integer.parseInt(info[0]);
double totalWeight = Double.parseDouble(info[1]);
String unit = info[2];

Log.d(TAG, "총 수량: " + totalCount);
Log.d(TAG, "총 중량: " + totalWeight + " " + unit);
```

### 5. 데이터 동기화

```java
// 계근 완료 후 저장 타입 업데이트
DBHandler.updatequeryGoodsWet(context, "GD001", "BC12345", "1");
DBHandler.updatequeryShipment(context, "GD001", "PROD001");

// 저장된 데이터 삭제 (서버 전송 완료 후)
DBHandler.deletequeryGoodsWet(context);
```

### 6. 바코드 정보 조회

```java
// 바코드 정보 조회
ArrayList<Barcodes_Info> barcodeList =
    DBHandler.selectqueryBarcodeInfo(context);

// 특정 바코드 정보 검색
ArrayList<HashMap<String, String>> searchResult =
    DBHandler.selectquerySearchBarcodeInfo(context, "PROD001", "BR001");
```

### 7. 데이터 초기화

```java
// 테이블 데이터 전체 삭제
DBHandler.deletequeryShipment(context);
DBHandler.deletequeryBarcodeInfo(context);
DBHandler.deletequeryAllGoodsWet(context);
DBHandler.deleteGoodsWetProductionCalc(context);
```

## 트랜잭션 처리

### 예시: 여러 작업을 트랜잭션으로 묶기

```java
DBHelper dbHelper = new DBHelper(context);
dbHelper.open();

try {
    dbHelper.executeSql("BEGIN TRANSACTION");

    // 여러 작업 수행
    for (Shipments_Info si : shipmentList) {
        DBHandler.insertqueryShipment(context, si, "N");
    }

    dbHelper.executeSql("COMMIT");
    Log.d(TAG, "트랜잭션 성공");
} catch (Exception e) {
    dbHelper.executeSql("ROLLBACK");
    Log.e(TAG, "트랜잭션 실패: " + e.getMessage());
} finally {
    dbHelper.close();
}
```

## 에러 처리

### 일반적인 에러 처리 패턴

```java
public static boolean insertqueryShipment(Context context, Shipments_Info si, String saveType) {
    DBHelper dbHelper = new DBHelper(context);
    dbHelper.open();
    boolean result;

    try {
        String sqlStr = "INSERT INTO ...";
        dbHelper.executeSql(sqlStr);
        result = true;
    } catch (Exception e) {
        e.printStackTrace();
        if (Common.D) {
            Log.v(TAG, "insertqueryShipment exception -> " + e.getMessage());
        }
        result = false;
    } finally {
        dbHelper.close();
    }

    return result;
}
```

## 성능 최적화 팁

1. **대량 데이터 처리 시 트랜잭션 사용**
   ```java
   dbHelper.executeSql("BEGIN TRANSACTION");
   // 많은 INSERT/UPDATE 작업
   dbHelper.executeSql("COMMIT");
   ```

2. **필요한 컬럼만 SELECT**
   ```java
   // 나쁜 예
   SELECT * FROM TB_SHIPMENT

   // 좋은 예
   SELECT ITEM_CODE, ITEM_NAME FROM TB_SHIPMENT
   ```

3. **인덱스 활용**
   - 자주 검색되는 컬럼에 인덱스 생성
   - GI_D_ID, PACKER_PRODUCT_CODE 등

4. **Cursor 자원 관리**
   ```java
   Cursor cursor = mDbHelper.selectSql(sqlStr);
   try {
       while (cursor.moveToNext()) {
           // 데이터 처리
       }
   } finally {
       cursor.close();
   }
   ```

## 관련 파일

- **DBInfo.java**: 테이블명 및 컬럼명 상수 정의
- **DBHelper.java**: 데이터베이스 생성 및 관리
- **Shipments_Info.java**: 출하 정보 데이터 모델
- **Barcodes_Info.java**: 바코드 정보 데이터 모델
- **Goodswets_Info.java**: 계근 정보 데이터 모델
- **Common.java**: 공통 유틸리티 (nullCheck, 디버그 플래그 등)

## 주의사항

1. **리소스 관리**
   - DBHelper는 반드시 close() 호출
   - Cursor는 사용 후 반드시 close()

2. **예외 처리**
   - 모든 데이터베이스 작업에서 예외 처리 필수
   - try-catch-finally 패턴 사용

3. **NULL 체크**
   - Common.nullCheck() 사용하여 NULL 안전성 확보

4. **날짜/시간 형식**
   - dateformat: "yyyyMMdd"
   - timeformat: "HHmmss"

5. **디버그 로그**
   - Common.D 플래그로 제어
   - 프로덕션에서는 false로 설정

6. **트랜잭션**
   - 여러 작업은 트랜잭션으로 묶어 처리
   - 실패 시 ROLLBACK으로 일관성 유지

7. **성능**
   - 대량 데이터는 배치 처리
   - 불필요한 SELECT * 지양
