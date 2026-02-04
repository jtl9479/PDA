# VIEW 조회 이후 프로세스

**작성일**: 2026-02-04
**목적**: 서버 VIEW 조회부터 앱 UI 표시까지의 전체 데이터 흐름 정의

---

## 1. 전체 흐름도

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            전체 데이터 흐름                                   │
└─────────────────────────────────────────────────────────────────────────────┘

  [서버]                      [앱 - 백그라운드]                [앱 - UI]
     │                             │                            │
     │  ① VIEW 쿼리 요청           │                            │
     │◄────────────────────────────│                            │
     │   (searchType별 분기)       │                            │
     │                             │                            │
     │  ② HTTP 응답                │                            │
     │────────────────────────────►│                            │
     │   (;; 행구분, :: 컬럼구분)   │                            │
     │                             │                            │
     │                      ③ 응답 파싱                         │
     │                      temp[] 배열                         │
     │                             │                            │
     │                      ④ DTO 매핑                          │
     │                      Shipments_Info                      │
     │                             │                            │
     │                      ⑤ SQLite 저장                       │
     │                      TB_SHIPMENT                         │
     │                             │                            │
     │                      ⑥ ArrayList 로드     ──────────────►│ arSM
     │                                                          │
     │                                                  ⑦ UI 표시/처리
     │                                                  - 화면표시
     │                                                  - 라벨출력
     │                                                  - 바코드생성
     │                                                  - 서버전송
```

---

## 2. 단계별 상세

### 2.1 ① VIEW 쿼리 요청

**파일**: ProgressDlgShipSearch.java (Line 94-207)

```java
// searchType에 따라 다른 VIEW 쿼리 호출
switch (searchType) {
    case 0:  // 이마트
        // VW_PDA_WID_LIST 쿼리
        break;
    case 1:  // 생산계근
        // VW_PDA_WID_LIST (생산) 쿼리
        break;
    case 2:  // 홈플러스
        // VW_PDA_WID_LIST 쿼리
        break;
    case 3:  // 도매
        // VW_PDA_WID_LIST 쿼리
        break;
    case 4:  // 비정량
        // VW_PDA_WID_LIST_NONFIXED 쿼리
        break;
    case 5:  // 홈플러스 비정량
        // VW_PDA_WID_LIST_NONFIXED_HP 쿼리
        break;
    case 6:  // 롯데
        // VW_PDA_WID_LIST_LOTTE 쿼리
        break;
    case 7:  // 생산라벨
        // VW_PDA_WID_LIST 쿼리
        break;
}
```

#### searchType 정의

| searchType | 거래처 | VIEW |
|:----------:|--------|------|
| 0 | 이마트 | VW_PDA_WID_LIST |
| 1 | 생산계근 | VW_PDA_WID_LIST |
| 2 | 홈플러스 | VW_PDA_WID_LIST |
| 3 | 도매 | VW_PDA_WID_LIST |
| 4 | 비정량 | VW_PDA_WID_LIST_NONFIXED |
| 5 | 홈플러스 비정량 | VW_PDA_WID_LIST_NONFIXED_HP |
| 6 | 롯데 | VW_PDA_WID_LIST_LOTTE |
| 7 | 생산라벨 | VW_PDA_WID_LIST |

---

### 2.2 ② HTTP 응답

**파일**: ProgressDlgShipSearch.java (Line 212-225)

```java
// 서버 응답 수신
String response = httpClient.execute(...);

// 응답 구분자
// - 행 구분: ";;"
// - 컬럼 구분: "::"
```

#### 응답 형식 예시

```
값1::값2::값3::...::값30;;값1::값2::값3::...::값30;;...
```

---

### 2.3 ③ 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 225-239)

```java
// 행 분리
String[] rows = response.split(";;");

for (String row : rows) {
    // 컬럼 분리
    String[] temp = row.split("::");

    // temp[0] ~ temp[29] 사용
}
```

---

### 2.4 ④ DTO 매핑

**파일**: ProgressDlgShipSearch.java (Line 240-291)

```java
Shipments_Info si = new Shipments_Info();

si.setGI_REQ_DATE(temp[0].toString());       // 출하요청일자
si.setBL_NO(temp[1].toString());             // BL번호
si.setREQ_SEQ(temp[2].toString());           // 요청순번
si.setCLIENT_CODE(temp[3].toString());       // 거래처코드
si.setCLIENTNAME(temp[4].toString());        // 거래처명
si.setGI_REQ_PKG(temp[5].toString());        // 출하요청수량
si.setITEM_CODE(temp[6].toString());         // 품목코드
si.setEMARTITEM(temp[7].toString());         // 품종명
si.setBARCODEGOODS(temp[8].toString());      // 바코드상품코드
si.setNET_WT(temp[9].toString());            // 순중량
si.setEXP_PERIOD(temp[10].toString());       // 소비기한
si.setSALE_CUST_CODE(temp[11].toString());   // 판매처코드
si.setPRODUCT_DATE(temp[12].toString());     // 생산일자
si.setITEM_TYPE(temp[13].toString());        // 품목타입
si.setCT_CODE(temp[14].toString());          // 원산지코드
si.setUSE_CODE(temp[15].toString());         // 용도코드
si.setGRADE(temp[16].toString());            // 등급
si.setGENDER(temp[17].toString());           // 성별
si.setBARCODE_TYPE(temp[18].toString());     // 바코드타입
si.setCOUNTRY_ORG_NO(temp[19].toString());   // 이력번호
si.setIMPORT_ID_NO(temp[20].toString());     // 수입식별번호
si.setEMART_TRACE_NO(temp[21].toString());   // 이마트이력번호
si.setPACKER_CODE(temp[22].toString());      // 업체코드
si.setEMARTLOGIS_CODE(temp[23].toString());  // 지점코드
si.setEXP_DATE(temp[24].toString());         // 소비기한일자
si.setCENTERNAME(temp[25].toString());       // 센터명
si.setUSE_NAME(temp[26].toString());         // 용도명
si.setCT_NAME(temp[27].toString());          // 원산지명
si.setSTORE_CODE(temp[28].toString());       // 점포코드
si.setEMART_PLANT_CODE(temp[29].toString()); // 가공장코드
```

#### temp[] 인덱스 → 컬럼 매핑 전체

| temp[] | 컬럼명 | 의미 |
|:------:|--------|------|
| temp[0] | GI_REQ_DATE | 출하요청일자 |
| temp[1] | BL_NO | BL번호 |
| temp[2] | REQ_SEQ | 요청순번 |
| temp[3] | CLIENT_CODE | 거래처코드 |
| temp[4] | CLIENTNAME | 거래처명 |
| temp[5] | GI_REQ_PKG | 출하요청수량 |
| temp[6] | ITEM_CODE | 품목코드 |
| temp[7] | EMARTITEM | 품종명 |
| temp[8] | BARCODEGOODS | 바코드상품코드 |
| temp[9] | NET_WT | 순중량 |
| temp[10] | EXP_PERIOD | 소비기한 |
| temp[11] | SALE_CUST_CODE | 판매처코드 |
| temp[12] | PRODUCT_DATE | 생산일자 |
| temp[13] | ITEM_TYPE | 품목타입 |
| temp[14] | CT_CODE | 원산지코드 |
| temp[15] | USE_CODE | 용도코드 |
| temp[16] | GRADE | 등급 |
| temp[17] | GENDER | 성별 |
| temp[18] | BARCODE_TYPE | 바코드타입 |
| temp[19] | COUNTRY_ORG_NO | 이력번호 |
| temp[20] | IMPORT_ID_NO | 수입식별번호 |
| temp[21] | EMART_TRACE_NO | 이마트이력번호 |
| temp[22] | PACKER_CODE | 업체코드 |
| temp[23] | EMARTLOGIS_CODE | 지점코드 |
| temp[24] | EXP_DATE | 소비기한일자 |
| temp[25] | CENTERNAME | 센터명 |
| temp[26] | USE_NAME | 용도명 |
| temp[27] | CT_NAME | 원산지명 |
| temp[28] | STORE_CODE | 점포코드 |
| temp[29] | EMART_PLANT_CODE | 가공장코드 |

---

### 2.5 ⑤ SQLite 저장

**파일**: DBHandler.java (Line 557-643)

```java
public void insertqueryShipment(Shipments_Info si) {
    ContentValues values = new ContentValues();

    values.put("GI_REQ_DATE", si.getGI_REQ_DATE());
    values.put("BL_NO", si.getBL_NO());
    values.put("REQ_SEQ", si.getREQ_SEQ());
    // ... 30개 컬럼 저장

    db.insert("TB_SHIPMENT", null, values);
}
```

#### TB_SHIPMENT 테이블 스키마

**파일**: DBHandler.java (Line 32-67)

```sql
CREATE TABLE TB_SHIPMENT (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    GI_REQ_DATE TEXT,
    BL_NO TEXT,
    REQ_SEQ TEXT,
    CLIENT_CODE TEXT,
    CLIENTNAME TEXT,
    GI_REQ_PKG TEXT,
    ITEM_CODE TEXT,
    EMARTITEM TEXT,
    BARCODEGOODS TEXT,
    NET_WT TEXT,
    EXP_PERIOD TEXT,
    SALE_CUST_CODE TEXT,
    PRODUCT_DATE TEXT,
    ITEM_TYPE TEXT,
    CT_CODE TEXT,
    USE_CODE TEXT,
    GRADE TEXT,
    GENDER TEXT,
    BARCODE_TYPE TEXT,
    COUNTRY_ORG_NO TEXT,
    IMPORT_ID_NO TEXT,
    EMART_TRACE_NO TEXT,
    PACKER_CODE TEXT,
    EMARTLOGIS_CODE TEXT,
    EXP_DATE TEXT,
    CENTERNAME TEXT,
    USE_NAME TEXT,
    CT_NAME TEXT,
    STORE_CODE TEXT,
    EMART_PLANT_CODE TEXT,
    PACKING_QTY INTEGER DEFAULT 0,
    SAVE_CNT INTEGER DEFAULT 0
);
```

---

### 2.6 ⑥ ArrayList 로드

**파일**: DBHandler.java (Line 81-368)

```java
public ArrayList<Shipments_Info> selectqueryShipment(String where) {
    ArrayList<Shipments_Info> arSM = new ArrayList<>();

    Cursor cursor = db.query("TB_SHIPMENT", null, where, null, null, null, null);

    while (cursor.moveToNext()) {
        Shipments_Info si = new Shipments_Info();

        si.setGI_REQ_DATE(cursor.getString(cursor.getColumnIndex("GI_REQ_DATE")));
        // ... 모든 컬럼 로드

        arSM.add(si);
    }

    return arSM;
}
```

---

### 2.7 ⑦ UI 표시/처리

**파일**: BixolonShipmentActivity.java

```java
// arSM: 로컬 DB에서 로드된 출하대상 목록
ArrayList<Shipments_Info> arSM;

// 현재 작업 중인 항목
int current_work_position;

// 데이터 접근 예시
Shipments_Info si = arSM.get(current_work_position);

// 화면표시
textView.setText(si.getCLIENTNAME());

// 라벨출력
printLabel(si);

// 바코드생성
String barcode = generateBarcode(si);

// 서버전송
sendToServer(si);
```

---

## 3. 주요 클래스

### 3.1 ProgressDlgShipSearch (AsyncTask)

| 메서드 | 역할 |
|--------|------|
| doInBackground() | 서버 통신, 응답 파싱, DB 저장 |
| onPostExecute() | UI 갱신 |

### 3.2 DBHandler

| 메서드 | 역할 |
|--------|------|
| insertqueryShipment() | 출하대상 INSERT |
| selectqueryShipment() | 출하대상 SELECT |
| updatequeryShipment() | 출하대상 UPDATE |
| deletequeryShipment() | 출하대상 DELETE |
| refreshShipmentList() | 전체 목록 갱신 |

### 3.3 Shipments_Info (DTO)

| 필드 수 | 역할 |
|:-------:|------|
| 42개 | 출하대상 데이터 보관 |

---

## 4. 데이터 동기화

### 4.1 서버 → 로컬 DB

**파일**: ProgressDlgShipSearch.java (Line 326-376)

```java
// 기존 데이터 삭제 후 INSERT
dbHandler.deletequeryShipment(where);

for (Shipments_Info si : list) {
    dbHandler.insertqueryShipment(si);
}
```

### 4.2 로컬 DB → 서버

**파일**: BixolonShipmentActivity.java

```java
// 계근 완료 시 서버 전송
if (si.getGI_REQ_PKG().equals(String.valueOf(si.getPACKING_QTY()))) {
    sendToServer(si);
    si.setSAVE_CNT(si.getSAVE_CNT() + 1);
    dbHandler.updatequeryShipment(si);
}
```

---

## 5. 사용 영역별 데이터 흐름

| 사용 영역 | 단계 | 설명 |
|----------|:----:|------|
| 서버전송 | ⑦ | 계근 완료 후 서버로 전송 |
| 화면표시 | ⑦ | Activity UI에 텍스트 표시 |
| 바코드생성 | ⑦ | 라벨 인쇄 시 바코드 문자열 생성 |
| 라벨출력 | ⑦ | 프린터로 라벨 인쇄 |
| 로직분기 | ⑦ | if/switch 조건 분기 |
| DB저장 | ⑤ | TB_SHIPMENT 테이블에 저장 |
| 바코드검증 | ⑦ | 스캔 바코드와 매칭 검증 |
| 조회조건 | ① | 서버 쿼리 WHERE 조건 |
| 앱미전달 | - | VIEW 내부에서만 사용 |

---

## 6. 관련 파일 목록

| 파일 | 역할 |
|------|------|
| ProgressDlgShipSearch.java | 서버 통신, 파싱, DB 저장 |
| DBHandler.java | SQLite CRUD |
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 상수 정의 |
| BixolonShipmentActivity.java | 빅솔론 프린터 Activity |
| ShipmentActivity.java | 우심 프린터 Activity |
| ShipmentListAdapter.java | 목록 어댑터 |

---

**최종 수정일**: 2026-02-04
