# JSP 호출 파라미터 가이드 (이마트 출하)

**작성일**: 2026-03-18
**대상**: searchType=0 (이마트 출하) 기준

---

## 1. 공통 전송 방식

### HTTP 전송

```java
// HttpHelper.java
HttpHelper.getInstance().sendDataDb(data, dbid, type, url);
```

| 파라미터 | 설명 | 값 |
|---------|------|-----|
| data | 쿼리 조건 또는 패킷 데이터 | JSP별 다름 |
| dbid | DB 접속 ID | "inno" |
| type | 요청 타입 | JSP별 다름 |
| url | JSP URL | Common.URL_XXX |

### HTTP POST 구조

```
POST {url}
Content-Type: application/x-www-form-urlencoded; charset=euc-kr

data={data}&dbid={dbid}
```

JSP에서 수신:
```java
String qry_where = request.getParameter("data");
String dbid = request.getParameter("dbid");
```

### 응답 형식

```
행1컬럼1::컬럼2::...::컬럼N;;행2컬럼1::컬럼2::...::컬럼N;;
```
- 컬럼 구분: `::`
- 행 구분: `;;`

---

## 2. search_shipment.jsp (출하대상 조회)

### 호출 위치
`ProgressDlgShipSearch.java:108~130`

### 호출 코드
```java
HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT);
```

### data 구성

```java
// 기본 조건
String data = " WHERE GI_REQ_DATE = '" + Common.selectDay + "'";

// 이마트(searchType=0) 창고별 조건 추가
if (Common.selectWarehouse.equals("삼일냉장")) {
    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
} else if (Common.selectWarehouse.equals("SWC")) {
    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
} else if (Common.selectWarehouse.equals("이천1센터")) {
    data += " AND GR_WAREHOUSE_CODE = '4001'";
} else if (Common.selectWarehouse.equals("부산센터")) {
    data += " AND GR_WAREHOUSE_CODE = '4004'";
} else if (Common.selectWarehouse.equals("탑로지스")) {
    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
}
```

### 실제 전송 예시 (삼일냉장, 2026-03-18)

```
data= WHERE GI_REQ_DATE = '20260318' AND GR_WAREHOUSE_CODE = 'IN10273'
```

### JSP에서 사용 (기존 Oracle)

```sql
SELECT ... FROM VW_PDA_WID_LIST
{data}
ORDER BY EOI_ID ASC
```

### 최종 실행 쿼리 (기존 Oracle)

```sql
SELECT ... FROM VW_PDA_WID_LIST
WHERE GI_REQ_DATE = '20260318' AND GR_WAREHOUSE_CODE = 'IN10273'
ORDER BY EOI_ID ASC
```

### VW_PDA_WID_LIST VIEW 내부 WHERE 조건

VIEW 자체에 아래 조건이 포함되어 있으므로, 앱에서 전달하는 WHERE 조건과 합산된다.

**해외매입 (UNION ALL 상단)**
```sql
WHERE 1 = 1
  AND ID.PACKING_QTY = 0                              -- 미계근 건만
  AND ID.GI_REQ_PKG <> 0                              -- 요청수량 있는 건만
  AND WR.CONTRACT_TYPE <> '40'                         -- 국내매입 제외 (해외만)
  AND EO.EOI_ID IS NOT NULL                            -- 이마트 주문 매칭된 건만
  AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')   -- 오늘 이후 출하일
```

**국내매입 (UNION ALL 하단)**
```sql
WHERE 1 = 1
  AND ID.PACKING_QTY = 0                              -- 미계근 건만
  AND ID.GI_REQ_PKG <> 0                              -- 요청수량 있는 건만
  AND WR.CONTRACT_TYPE = '40'                          -- 국내매입만
  AND EO.EOI_ID IS NOT NULL                            -- 이마트 주문 매칭된 건만
  AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')   -- 오늘 이후 출하일
```

**JOIN 내부 필터**
```sql
-- B_EMART_BARCODE 조인 조건 (원료육만)
AND EB.ITEM_TYPE = 'W'
```

| VIEW 내부 조건 | 의미 | 앱에서 대체 |
|---------------|------|-----------|
| PACKING_QTY = 0 | 미계근 건 | 서버 DB 미업데이트로 항상 0 |
| GI_REQ_PKG <> 0 | 요청수량 존재 | 새 쿼리: `D.출고수량 > 0` |
| CONTRACT_TYPE | 해외/국내 구분 UNION | 새 쿼리: 구분 없음 |
| EOI_ID IS NOT NULL | 이마트 주문 매칭 | 새 쿼리: `H.마트사구분 = '1'` |
| GI_REQ_DATE >= SYSDATE | 오늘 이후 출하 | 새 쿼리: `D.출고일자 > '20260316'` |
| EB.ITEM_TYPE = 'W' | 원료육 바코드 | 새 쿼리: `I.원료육여부 = '1'` |

### 새 쿼리 WHERE 조건 (현재 MSSQL)

```sql
WHERE D.회사코드 = '20'
  AND D.출고일자 > '20260316'
  AND D.창고코드 = '1'
  AND H.마트사구분 = '1'
  AND D.출고수량 > 0
  AND I.원료육여부 = '1'
```

### 응답 컬럼 (32개, :: 구분)

| Index | 컬럼 | 설명 |
|:-----:|------|------|
| 0 | GI_D_ID | 출고상세ID |
| 1 | ITEM_CODE | 품목코드 |
| 2 | ITEM_NAME | 품목명 |
| 3 | EMARTITEM_CODE | 이마트상품코드 |
| 4 | EMARTITEM | 이마트상품명 |
| 5 | GI_REQ_PKG | 요청수량 |
| 6 | GI_REQ_QTY | 요청중량 |
| 7 | GI_REQ_DATE | 요청일자 |
| 8 | BL_NO | BL번호 |
| 9 | BRAND_CODE | 브랜드코드 |
| 10 | CLIENT_CODE | 거래처코드 |
| 11 | CLIENTNAME | 거래처명 |
| 12 | CENTERNAME | 센터명 |
| 13 | ITEM_SPEC | 품목규격 |
| 14 | CT_CODE | 원산지코드 |
| 15 | IMPORT_ID_NO | 수입식별번호 |
| 16 | PACKER_CODE | 패커코드 |
| 17 | PACKER_PRODUCT_CODE | 패커상품코드 |
| 18 | BARCODE_TYPE | 바코드타입 |
| 19 | ITEM_TYPE | 아이템타입 |
| 20 | PACKWEIGHT | 포장중량 |
| 21 | BARCODEGOODS | 바코드상품코드 |
| 22 | STORE_IN_DATE | 입고일자 |
| 23 | EMARTLOGIS_CODE | 물류코드 |
| 24~31 | searchType별 추가 필드 | 이마트: WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE, MAJOR_CATEGORY, CONTAINER_TYPE |

---

## 3. search_barcode_info.jsp (바코드 규칙 조회)

### 호출 위치
`ProgressDlgBarcodeSearch.java:49~77`

### 호출 코드
```java
HttpHelper.getInstance().sendDataDb(data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO);
```

### data 구성

```java
// TB_SHIPMENT에서 PACKER_PRODUCT_CODE 목록 추출
ArrayList<String[]> list_code_info = DBHandler.selectqueryCodeList(mContext);

String data = " WHERE ";
for (int i = 0; i < list_code_info.size(); i++) {
    if (i == list_code_info.size() - 1) {
        data += "SBI.PACKER_PRODUCT_CODE = '" + list_code_info.get(i)[0] + "'";
    } else {
        data += "SBI.PACKER_PRODUCT_CODE = '" + list_code_info.get(i)[0] + "' OR ";
    }
}
// 목록이 비어있으면
if (list_code_info.size() == 0) {
    data += "1=0";
}
```

### 실제 전송 예시

```
data= WHERE SBI.PACKER_PRODUCT_CODE = '20001' OR SBI.PACKER_PRODUCT_CODE = '30005'
```

### JSP에서 사용

```sql
SELECT ... FROM S_BARCODE_INFO SBI
INNER JOIN B_ITEM BI ON ...
INNER JOIN B_SUPPLIER_ITEM BSI ON ...
{data}
ORDER BY PACKER_PRODUCT_CODE ASC
```

### 최종 실행 쿼리

```sql
SELECT ... FROM S_BARCODE_INFO SBI
INNER JOIN B_ITEM BI ON SBI.ITEMCODE = BI.ITEM_CODE AND BI.STATUS = 'Y'
INNER JOIN B_SUPPLIER_ITEM BSI ON SBI.PACKER_CLIENT_CODE = BSI.PACKER_CODE
  AND SBI.PACKER_PRODUCT_CODE = BSI.PACKER_PRODUCT_CODE
  AND SBI.STATUS = 'Y' AND BSI.STATUS = 'Y'
WHERE SBI.PACKER_PRODUCT_CODE = '20001' OR SBI.PACKER_PRODUCT_CODE = '30005'
ORDER BY PACKER_PRODUCT_CODE ASC
```

### 응답 컬럼 (25개, :: 구분)

| Index | 컬럼 | 설명 |
|:-----:|------|------|
| 0 | PACKER_CLIENT_CODE | 패커거래처코드 |
| 1 | PACKER_PRODUCT_CODE | 패커상품코드 |
| 2 | PACKER_PRD_NAME | 패커상품명 |
| 3 | ITEMCODE | 아이템코드 |
| 4 | ITEM_NAME_KR | 한글상품명 |
| 5 | BRAND_CODE | 브랜드코드 |
| 6 | BARCODEGOODS | 바코드상품코드 |
| 7 | BASEUNIT | 기준단위(KG/LB) |
| 8 | ZEROPOINT | 소수점자리수 |
| 9 | PACKER_PRD_CODE_FROM | 패커코드시작위치 |
| 10 | PACKER_PRD_CODE_TO | 패커코드끝위치 |
| 11 | BARCODEGOODS_FROM | 바코드상품코드시작 |
| 12 | BARCODEGOODS_TO | 바코드상품코드끝 |
| 13 | WEIGHT_FROM | 중량시작위치 |
| 14 | WEIGHT_TO | 중량끝위치 |
| 15 | MAKINGDATE_FROM | 제조일시작위치 |
| 16 | MAKINGDATE_TO | 제조일끝위치 |
| 17 | BOXSERIAL_FROM | 박스시리얼시작 |
| 18 | BOXSERIAL_TO | 박스시리얼끝 |
| 19 | STATUS | 상태 |
| 20 | REG_ID | 등록자 |
| 21 | REG_DATE | 등록일 |
| 22 | REG_TIME | 등록시간 |
| 23 | MEMO | 메모 |
| 24 | SHELF_LIFE | 유통기한(일수) |

---

## 4. search_goods_wet.jsp (기존 계근 데이터 조회)

### 호출 위치
`ProgressDlgGoodsWetSearch.java:58~76`

### 호출 코드
```java
HttpHelper.getInstance().sendDataDb(data, "inno", "search_goods_wet", Common.URL_SEARCH_GOODS_WET);
```

### data 구성

```java
// TB_SHIPMENT에서 GI_D_ID 목록 추출
ArrayList<String> list_id_info = DBHandler.selectqueryGIDIDList(mContext);

String data = " WHERE ";
for (int i = 0; i < list_id_info.size(); i++) {
    if (i == list_id_info.size() - 1) {
        data += "GI_D_ID = '" + list_id_info.get(i) + "'";
    } else {
        data += "GI_D_ID = '" + list_id_info.get(i) + "' OR ";
    }
}
// 목록이 비어있으면
if (list_id_info.size() == 0) {
    data += "1=0";
}
```

### 실제 전송 예시

```
data= WHERE GI_D_ID = '12345' OR GI_D_ID = '12346' OR GI_D_ID = '12347'
```

### JSP에서 사용

```sql
SELECT GI_D_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE,
       BARCODE, PACKER_CLIENT_CODE, BOX_CNT, REG_ID,
       REG_DATE, REG_TIME, MAKINGDATE, BOXSERIAL
FROM W_GOODS_WET
{data}
ORDER BY GI_D_ID ASC
```

### 최종 실행 쿼리

```sql
SELECT GI_D_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE,
       BARCODE, PACKER_CLIENT_CODE, BOX_CNT, REG_ID,
       REG_DATE, REG_TIME, MAKINGDATE, BOXSERIAL
FROM W_GOODS_WET
WHERE GI_D_ID = '12345' OR GI_D_ID = '12346' OR GI_D_ID = '12347'
ORDER BY GI_D_ID ASC
```

### 응답 컬럼 (12개, :: 구분)

| Index | 컬럼 | 설명 |
|:-----:|------|------|
| 0 | GI_D_ID | 출고상세ID |
| 1 | WEIGHT | 중량 |
| 2 | WEIGHT_UNIT | 중량단위 |
| 3 | PACKER_PRODUCT_CODE | 패커상품코드 |
| 4 | BARCODE | 바코드 |
| 5 | PACKER_CLIENT_CODE | 패커거래처코드 |
| 6 | BOX_CNT | 계근순번 |
| 7 | REG_ID | 등록자 |
| 8 | REG_DATE | 등록일 |
| 9 | REG_TIME | 등록시간 |
| 10 | MAKINGDATE | 제조일 |
| 11 | BOXSERIAL | 박스시리얼 |

---

## 5. insert_goods_wet.jsp (계근 데이터 서버 전송)

### 호출 위치
`ShipmentActivity.java:3639~3663`

### 호출 코드
```java
HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);
```

### data 구성 (패킷)

```java
String packet = "";
packet += list_send_info.get(i).getGI_D_ID() + "::";           // [0]
packet += list_send_info.get(i).getWEIGHT() + "::";            // [1]
packet += list_send_info.get(i).getWEIGHT_UNIT() + "::";       // [2]
packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::"; // [3]
packet += list_send_info.get(i).getBARCODE() + "::";            // [4]
packet += list_send_info.get(i).getPACKER_CLIENT_CODE() + "::"; // [5]
packet += list_send_info.get(i).getMAKINGDATE() + "::";         // [6]
packet += list_send_info.get(i).getBOXSERIAL() + "::";          // [7]
packet += list_send_info.get(i).getBOX_CNT() + "::";            // [8]
packet += list_send_info.get(i).getREG_ID() + "::";             // [9]
packet += list_send_info.get(i).getITEM_CODE() + "::";          // [10]
packet += list_send_info.get(i).getBRAND_CODE() + "::";         // [11]
packet += list_send_info.get(i).getCLIENT_TYPE() + "::";        // [12]
packet += list_send_info.get(i).getBOX_ORDER() + "::";          // [13]
packet += list_send_info.get(i).getCOMPANY_CODE();              // [14]
```

### 실제 전송 예시

```
data=12345::5.67::KG::20001::002000100567260310B002::TEST_CLIENT::260310::B002::1::admin::ITEM001::BR01::::0::20
```

### JSP에서 사용

```java
String[] splitData = data.split("::");
// splitData[0]  = GI_D_ID         → 출고상세SEQ
// splitData[1]  = WEIGHT          → 계근중량
// splitData[2]  = WEIGHT_UNIT     → 계근중량단위
// splitData[3]  = PACKER_PRODUCT_CODE → ppCode
// splitData[4]  = BARCODE         → 계근바코드
// splitData[5]  = PACKER_CLIENT_CODE → 패커코드
// splitData[6]  = MAKINGDATE      → 제조일자
// splitData[7]  = BOXSERIAL       → 박스시리얼
// splitData[8]  = BOX_CNT         → 계근순번
// splitData[9]  = REG_ID          → 등록사원
// splitData[10] = ITEM_CODE       → (미사용, 회사코드 대체)
// splitData[11] = BRAND_CODE      → (미사용)
// splitData[12] = CLIENT_TYPE     → (미사용)
// splitData[13] = BOX_ORDER       → (미사용)
// splitData[14] = COMPANY_CODE    → 회사코드
```

### JSP INSERT 쿼리

```sql
INSERT INTO SM_출고계근 (SEQ, 출고상세SEQ, 계근중량, 계근중량단위, ppCode,
    계근바코드, 패커코드, 제조일자, 박스시리얼, 계근순번,
    등록사원, 등록일자, 등록시간, 회사코드, 수정사원, 수정일자, 수정시간)
VALUES (NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ,
    splitData[0], splitData[1], splitData[2], splitData[3],
    splitData[4], splitData[5], splitData[6], splitData[7], splitData[8],
    splitData[9], 서버시각, 서버시각, splitData[14], splitData[9], 서버시각, 서버시각)
```

### 응답

```
s  → 성공
f  → 실패 (에러메시지 포함)
```

---

## 6. 호출 순서

```
① search_shipment.jsp       → TB_SHIPMENT 로컬 저장
        ↓
② search_barcode_info.jsp   → TB_BARCODE_INFO 로컬 저장
        ↓
③ search_goods_wet.jsp      → TB_GOODS_WET 로컬 저장 (기존 계근 동기화)
        ↓
④ 바코드 스캔 → 계근 → 로컬 저장
        ↓
⑤ insert_goods_wet.jsp      → 서버 SM_출고계근 INSERT
```

①②③은 "출하대상받기" 시 자동 순차 실행됩니다.
⑤는 계근 데이터 전송 시 건별로 호출됩니다.

---

**문서 버전**: 1.0
