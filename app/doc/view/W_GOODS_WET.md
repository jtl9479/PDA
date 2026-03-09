# W_GOODS_WET 분석 문서

**분석일**: 2026-03-04
**분석 기준**: 실제 JSP (`app/doc/JSP/search_goods_wet.jsp`, `insert_goods_wet*.jsp`) + Java 소스 코드

---

## 1. 개요

계근(무게 측정) 데이터 테이블 분석 문서입니다.

| 항목     | 내용                                  |
| ------ | ----------------------------------- |
| 스키마    | HIGHLAND / INNO                     |
| 테이블명   | W_GOODS_WET (Oracle) / TB_GOODS_WET (SQLite) |
| 용도     | 바코드 스캔 후 계근 데이터 저장/조회/전송            |
| 총 컬럼 수 | **22개** (SQLite), 서버 다운로드 12개, 서버 업로드 10~14개 |

### 1.1 관련 파일

| 파일 | 역할 |
|------|------|
| `app/doc/JSP/search_goods_wet.jsp` | 서버 다운로드 JSP (기존 계근 데이터 조회) |
| `app/doc/JSP/insert_goods_wet.jsp` | 이마트 업로드 JSP (단건) |
| `app/doc/JSP/insert_goods_wet_new.jsp` | 생산/도매/비정량 업로드 JSP (배치) |
| `app/doc/JSP/insert_goods_wet_homeplus.jsp` | 홈플러스/롯데 업로드 JSP (단건, CLIENT_TYPE 포함) |
| `app/doc/JSP/insert_goods_wet_ono.jsp` | 레거시 업로드 JSP |
| `ProgressDlgGoodsWetSearch.java` | 서버 다운로드 파싱 |
| `BixolonShipmentActivity.java` | 핵심 비즈니스 로직 (계근 입력/전송) |
| `LabelPrintHelper.java` | 라벨 출력 (간접 사용) |
| `DBHandler.java` | 로컬 DB CRUD (14개 메서드) |
| `Goodswets_Info.java` | 데이터 모델 (DTO) |

### 1.2 데이터 생명주기

```
[다운로드]                              [로컬 생성]
search_goods_wet.jsp                  바코드 스캔 → wet_data_insert()
  ↓ (12컬럼)                             ↓ (19~20컬럼)
ProgressDlgGoodsWetSearch             insertqueryGoodsWet*()
  ↓ SAVE_TYPE="Y"                       ↓ SAVE_TYPE="F"
TB_GOODS_WET (SQLite)                 TB_GOODS_WET (SQLite)
                                        ↓
                                      insert_goods_wet*.jsp (서버 업로드)
                                        ↓
                                      SAVE_TYPE → "Y" (전송완료)
```

---

## 2. SQLite 테이블 스키마

### 2.1 CREATE TABLE 정의

**소스**: `DBHandler.java:1052-1092` (`createqueryGoodsWet()`)

```sql
CREATE TABLE TB_GOODS_WET (
  GOODS_WET_ID        INTEGER PRIMARY KEY AUTOINCREMENT,
  GI_D_ID             TEXT NOT NULL,
  WEIGHT              TEXT NOT NULL,
  WEIGHT_UNIT         TEXT NOT NULL,
  PACKER_PRODUCT_CODE TEXT NOT NULL,
  BARCODE             TEXT,
  PACKER_CLIENT_CODE  TEXT NOT NULL,
  MAKINGDATE          TEXT,
  BOXSERIAL           TEXT,
  BOX_CNT             INTEGER NOT NULL,
  EMARTITEM_CODE      TEXT,
  EMARTITEM           TEXT,
  ITEM_CODE           TEXT,
  BRAND_CODE          TEXT,
  REG_ID              TEXT,
  REG_DATE            TEXT,
  REG_TIME            TEXT NOT NULL,
  SAVE_TYPE           TEXT NOT NULL,
  MEMO                TEXT,
  DUPLICATE           TEXT,
  CLIENT_TYPE         TEXT,
  BOX_ORDER           Integer DEFAULT 0
);
```

**총 22개 컬럼**

---

## 3. 컬럼 목록 (22개)

### 3.1 전체 컬럼 상세

|  #  | 컬럼명                 | SQLite 타입       | NOT NULL | 기본값  | 설명                        |
| :-: | ------------------- | --------------- | :------: | :--: | ------------------------- |
|  1  | GOODS_WET_ID        | INTEGER PK AUTO |    ✓     | AUTO | 로컬 PK (앱 내부 전용)           |
|  2  | GI_D_ID             | TEXT            |    ✓     |  -   | 출하상세ID (TB_SHIPMENT 연결 키) |
|  3  | WEIGHT              | TEXT            |    ✓     |  -   | 중량 (소수점 2자리)              |
|  4  | WEIGHT_UNIT         | TEXT            |    ✓     |  -   | 중량 단위 (LB/KG)             |
|  5  | PACKER_PRODUCT_CODE | TEXT            |    ✓     |  -   | 패커 상품코드                   |
|  6  | BARCODE             | TEXT            |    -     |  -   | 스캔한 바코드 원본                |
|  7  | PACKER_CLIENT_CODE  | TEXT            |    ✓     |  -   | 패커 거래처코드                  |
|  8  | MAKINGDATE          | TEXT            |    -     |  -   | 제조일 (바코드에서 추출)            |
|  9  | BOXSERIAL           | TEXT            |    -     |  -   | 박스번호 (바코드에서 추출)           |
| 10  | BOX_CNT             | INTEGER         |    ✓     |  -   | 계근 순서번호 (PACKING_QTY+1)   |
| 11  | EMARTITEM_CODE      | TEXT            |    -     |  -   | 이마트 상품코드                  |
| 12  | EMARTITEM           | TEXT            |    -     |  -   | 이마트 상품명                   |
| 13  | ITEM_CODE           | TEXT            |    -     |  -   | 상품코드                      |
| 14  | BRAND_CODE          | TEXT            |    -     |  -   | 브랜드코드                     |
| 15  | REG_ID              | TEXT            |    -     |  -   | 등록자 ID                    |
| 16  | REG_DATE            | TEXT            |    -     |  -   | 등록 날짜 (yyyyMMdd)          |
| 17  | REG_TIME            | TEXT            |    ✓     |  -   | 등록 시간 (HHmmss)            |
| 18  | SAVE_TYPE           | TEXT            |    ✓     |  -   | 전송 상태 (F=미전송, Y=전송완료)     |
| 19  | MEMO                | TEXT            |    -     |  -   | 메모                        |
| 20  | DUPLICATE           | TEXT            |    -     |  -   | 중복스캔 여부                   |
| 21  | CLIENT_TYPE         | TEXT            |    -     |  -   | 채널코드 (06=홈플러스, 07=롯데)     |
| 22  | BOX_ORDER           | Integer         |    -     |  0   | 박스 순번                     |

### 3.2 용도별 컬럼 분류

#### 3.2.0 분류 요약표

| 용도 | 컬럼 수 | 비고 |
|------|:------:|------|
| 서버다운로드 | **12개** | search_goods_wet.jsp에서 수신 |
| 서버전송 | **14개** | insert_goods_wet*.jsp로 송신 |
| 화면표시 | **5개** | DetailAdapter에서 표시 |
| 라벨출력 | **3개** | LabelPrintHelper로 전달 (간접) |
| 로직분기 | **7개** | 비즈니스 로직 조건문 사용 |
| 미사용 | **3개** | 스키마에만 존재 |

#### 3.2.1 서버다운로드 (12개)

search_goods_wet.jsp SELECT 컬럼 → ProgressDlgGoodsWetSearch.java 파싱

| temp[] | 컬럼명                 | 설명       |
| :----: | ------------------- | -------- |
|   0    | GI_D_ID             | 출하상세ID   |
|   1    | WEIGHT              | 중량       |
|   2    | WEIGHT_UNIT         | 중량 단위    |
|   3    | PACKER_PRODUCT_CODE | 패커 상품코드  |
|   4    | BARCODE             | 바코드      |
|   5    | PACKER_CLIENT_CODE  | 패커 거래처코드 |
|   6    | BOX_CNT             | 계근 순서번호  |
|   7    | REG_ID              | 등록자 ID   |
|   8    | REG_DATE            | 등록 날짜    |
|   9    | REG_TIME            | 등록 시간    |
|   10   | MAKINGDATE          | 제조일      |
|   11   | BOXSERIAL           | 박스번호     |

**참고**: BOX_ORDER(temp[12])는 ProgressDlgGoodsWetSearch.java:113에서 접근하나, JSP가 12개 컬럼만 반환하므로 ArrayIndexOutOfBounds 가능성 있음 (잠재적 버그)

#### 3.2.2 서버전송 (10~14개)

업로드 패킷 구조 (`BixolonShipmentActivity.java:2542-2555`)

| 패킷순서 | 컬럼명 | 이마트 | 홈플러스/롯데 | 비고 |
|:--------:|--------|:-----:|:----------:|------|
| 0 | GI_D_ID | ✓ | ✓ | |
| 1 | WEIGHT | ✓ | ✓ | |
| 2 | WEIGHT_UNIT | ✓ | ✓ | |
| 3 | PACKER_PRODUCT_CODE | ✓ | ✓ | |
| 4 | BARCODE | ✓ | ✓ | |
| 5 | PACKER_CLIENT_CODE | ✓ | ✓ | |
| 6 | MAKINGDATE | ✓ | ✓ | |
| 7 | BOXSERIAL | ✓ | ✓ | |
| 8 | BOX_CNT | ✓ | ✓ | |
| 9 | REG_ID | ✓ | ✓ | |
| 10 | ITEM_CODE | ✓* | ✓* | *JSP에서 주석처리된 UPDATE에서만 사용 |
| 11 | BRAND_CODE | ✓* | ✓* | *JSP에서 주석처리된 UPDATE에서만 사용 |
| 12 | CLIENT_TYPE | - | ✓ | 홈플러스/롯데만 (CHANNEL_CODE) |
| 13 | BOX_ORDER | - | ✓ | 홈플러스/롯데만 |

**JSP별 INSERT 컬럼 수**:
- `insert_goods_wet.jsp` (이마트): 13개 (PK + 10컬럼 + REG_DATE/REG_TIME 서버생성)
- `insert_goods_wet_homeplus.jsp` (홈플러스/롯데): 15개 (+CHANNEL_CODE, BOX_ORDER)
- `insert_goods_wet_new.jsp` (생산/도매/비정량): 13개 (배치 모드)

#### 3.2.3 화면표시 (5개)

DetailAdapter에서 계근 상세 목록 표시

| 컬럼명 | 표시 내용 |
|--------|----------|
| WEIGHT | 중량 |
| MAKINGDATE | 제조일 |
| BOXSERIAL | 박스번호 |
| PACKER_PRODUCT_CODE | 패커 상품코드 |
| BOX_CNT | 순서번호 (행 번호) |

#### 3.2.4 라벨출력 (3개, 간접 전달)

LabelPrintHelper에 직접 Goodswets_Info를 전달하지 않고, BixolonShipmentActivity가 값을 추출하여 파라미터로 전달

| 컬럼명 | 전달 방식 | 사용처 |
|--------|----------|--------|
| WEIGHT | `weight_double` 파라미터 | 라벨 중량 표시 |
| MAKINGDATE | `making_date` 파라미터 | 라벨 제조일 표시 |
| BOX_ORDER | `box_order` 파라미터 | 롯데 라벨 박스순번 |

#### 3.2.5 로직분기 (7개)

| 컬럼명 | 사용처 | 로직 |
|--------|--------|------|
| GI_D_ID | WHERE 조건 | 출하건별 조회, 전송 루프 매칭 |
| WEIGHT | 합계 계산 | 라벨 합계 출력, UI 합계 표시 |
| PACKER_PRODUCT_CODE | WHERE 조건 | 상품별 계근 목록 조회 |
| BARCODE | WHERE 조건 | 중복 체크, 삭제, 전송 후 UPDATE |
| BOX_CNT | WHERE 조건 | 삭제 후 재번호 부여, 전송 후 UPDATE |
| SAVE_TYPE | 분기 조건 | "F"=전송 대상, "Y"=전송완료 (삭제불가) |
| CLIENT_TYPE | WHERE 조건 | selectMaxBoxOrder에서 "06" 조건 |

#### 3.2.6 미사용 컬럼 (3개)

| 컬럼명 | 상태 | 비고 |
|--------|------|------|
| MEMO | 스키마에만 존재 | 앱에서 읽기/쓰기 없음 |
| DUPLICATE | 항상 "F" 고정 | 설정만 하고 조건 분기 없음 |
| EMARTITEM_CODE / EMARTITEM | SQLite 저장만 | 서버 전송 안됨, 화면 표시 안됨 |

### 3.3 용도별 체크표

|  #  | 컬럼명                 | 다운로드 | 서버전송 | 화면표시 | 라벨출력 | 로직분기 | 미사용 | 비고               |
| :-: | ------------------- | :--: | :--: | :--: | :--: | :--: | :-: | ---------------- |
|  1  | GOODS_WET_ID        |      |      |      |      |      |  ✓  | 로컬 PK 전용         |
|  2  | GI_D_ID             |  ✓   |  ✓   |      |      |  ✓   |     | TB_SHIPMENT 연결 키 |
|  3  | WEIGHT              |  ✓   |  ✓   |  ✓   |  ✓   |  ✓   |     | 핵심 데이터           |
|  4  | WEIGHT_UNIT         |  ✓   |  ✓   |      |      |      |     | LB/KG 구분         |
|  5  | PACKER_PRODUCT_CODE |  ✓   |  ✓   |  ✓   |      |  ✓   |     | 상품 매칭 키          |
|  6  | BARCODE             |  ✓   |  ✓   |      |      |  ✓   |     | 중복체크/삭제/UPDATE   |
|  7  | PACKER_CLIENT_CODE  |  ✓   |  ✓   |      |      |      |     |                  |
|  8  | MAKINGDATE          |  ✓   |  ✓   |  ✓   |  ✓   |      |     | 바코드에서 추출         |
|  9  | BOXSERIAL           |  ✓   |  ✓   |  ✓   |      |      |     | 바코드에서 추출         |
| 10  | BOX_CNT             |  ✓   |  ✓   |  ✓   |      |  ✓   |     | 순서번호             |
| 11  | EMARTITEM_CODE      |      |      |      |      |      |  ✓  | 저장만, 미사용         |
| 12  | EMARTITEM           |      |      |      |      |      |  ✓  | 저장만, 미사용         |
| 13  | ITEM_CODE           |      |  ✓*  |      |      |      |     | *서버에서 주석처리       |
| 14  | BRAND_CODE          |      |  ✓*  |      |      |      |     | *서버에서 주석처리       |
| 15  | REG_ID              |  ✓   |  ✓   |      |      |      |     |                  |
| 16  | REG_DATE            |  ✓   |      |      |      |  ✓   |     | 서버 자체 생성         |
| 17  | REG_TIME            |  ✓   |      |      |      |      |     | 서버 자체 생성         |
| 18  | SAVE_TYPE           |      |      |      |      |  ✓   |     | 로컬 플래그 전용        |
| 19  | MEMO                |      |      |      |      |      |  ✓  | 완전 미사용           |
| 20  | DUPLICATE           |      |      |      |      |      |  ✓  | 항상 "F" 고정        |
| 21  | CLIENT_TYPE         |      | ✓**  |      |      |  ✓   |     | **홈플러스/롯데만       |
| 22  | BOX_ORDER           | ✓*** | ✓**  |      |  ✓   |      |     | **홈플러스/롯데만       |

- `✓*` = 패킷에 포함되나 서버 JSP에서 주석처리된 코드에서만 참조
- `✓**` = 홈플러스/롯데 전용 (insert_goods_wet_homeplus.jsp)
- `✓***` = 다운로드 시 temp[12] 접근하나 JSP가 12컬럼만 반환 (잠재적 버그)

---

## 4. 다운로드 흐름 상세

### 4.1 트리거 시점

바코드 정보 다운로드 완료 후 자동 실행

```
ProgressDlgShipSearch (출하 다운로드)
  ↓ onPostExecute()
ProgressDlgBarcodeSearch (바코드 정보 다운로드)
  ↓ onPostExecute()
ProgressDlgGoodsWetSearch (계근 데이터 다운로드)  ← 여기
```

### 4.2 다운로드 로직

**소스**: `ProgressDlgGoodsWetSearch.java:55-127`

1. **GI_D_ID 목록 추출** (line 58)
   ```java
   String qry = DBHandler.selectqueryGIDIDList(mContext);
   ```
   로컬 TB_SHIPMENT에서 GI_D_ID 목록을 가져옴

2. **WHERE 절 동적 생성** (lines 60-71)
   ```
   WHERE GI_D_ID = 'xxx' OR GI_D_ID = 'yyy' ...
   ```

3. **서버 호출** (line 76)
   ```java
   HttpHelper.getInstance().sendDataDb(data, "inno", "search_goods_wet", Common.URL_SEARCH_GOODS_WET)
   ```

4. **응답 파싱** (lines 85-116)
   - 행 구분: `;;`
   - 열 구분: `::`
   - SAVE_TYPE = "Y" (기전송 상태로 설정)

### 4.3 temp[] 매핑

**소스**: `ProgressDlgGoodsWetSearch.java:101-113`

```java
gi.setGI_D_ID(temp[0]);              // 출하상세ID
gi.setWEIGHT(temp[1]);              // 중량
gi.setWEIGHT_UNIT(temp[2]);         // 중량 단위
gi.setPACKER_PRODUCT_CODE(temp[3]); // 패커 상품코드
gi.setBARCODE(temp[4]);             // 바코드
gi.setPACKER_CLIENT_CODE(temp[5]);  // 패커 거래처코드
gi.setBOX_CNT(temp[6]);            // 계근 순서번호
gi.setREG_ID(temp[7]);             // 등록자 ID
gi.setREG_DATE(temp[8]);           // 등록 날짜
gi.setREG_TIME(temp[9]);           // 등록 시간
gi.setMAKINGDATE(temp[10]);        // 제조일
gi.setBOXSERIAL(temp[11]);         // 박스번호
gi.setBOX_ORDER(temp[12]);         // 박스 순번 ← 잠재적 버그
gi.setSAVE_TYPE("Y");              // 기전송 상태
```

### 4.4 JSP SELECT 쿼리

**소스**: `search_goods_wet.jsp:56-68`

```sql
SELECT GI_D_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE,
       BARCODE, PACKER_CLIENT_CODE, BOX_CNT, REG_ID,
       REG_DATE, REG_TIME, MAKINGDATE, BOXSERIAL
FROM W_GOODS_WET
WHERE {동적 조건}
ORDER BY GI_D_ID ASC
```

**12개 컬럼 반환** (BOX_ORDER 미포함)

---

## 5. 로컬 생성 흐름 상세

### 5.1 wet_data_insert() 메서드

**소스**: `BixolonShipmentActivity.java:1494-1640`

바코드 스캔 후 계근 데이터를 생성하는 핵심 메서드

#### 5.1.1 Goodswets_Info 객체 생성 (lines 1506-1522)

```java
Goodswets_Info gi = new Goodswets_Info();
gi.setGI_D_ID(si.getGI_D_ID());                    // 출하상세ID (Shipments_Info에서)
gi.setWEIGHT(weight_str);                           // 중량 (바코드에서 추출)
gi.setWEIGHT_UNIT(bi.getBASEUNIT());               // 중량 단위 (Barcodes_Info에서)
gi.setPACKER_PRODUCT_CODE(si.getPACKER_PRODUCT_CODE()); // 패커 상품코드
gi.setBARCODE(work_item_fullbarcode);               // 스캔 바코드 원본
gi.setPACKER_CLIENT_CODE(bi.getPACKER_CLIENT_CODE()); // 패커 거래처코드
gi.setMAKINGDATE(making_date);                      // 제조일 (바코드에서 추출)
gi.setBOXSERIAL(box_serial);                        // 박스번호 (바코드에서 추출)
gi.setBOX_CNT(String.valueOf(PACKING_QTY + 1));     // 순서번호 (자동 증가)
gi.setEMARTITEM_CODE(si.getEMARTITEM_CODE());      // 이마트 상품코드
gi.setEMARTITEM(si.getEMARTITEM());                // 이마트 상품명
gi.setITEM_CODE(si.getITEM_CODE());                // 상품코드
gi.setBRAND_CODE(si.getBRAND_CODE());              // 브랜드코드
gi.setREG_ID(Common.REG_ID);                       // 등록자 ID (로그인 사용자)
gi.setSAVE_TYPE("F");                              // 미전송 상태
gi.setDUPLICATE("F");                              // 중복 아님
```

#### 5.1.2 searchType별 INSERT 분기 (lines 1526-1542)

| searchType | INSERT 메서드 | CLIENT_TYPE | BOX_ORDER |
|:----------:|--------------|:-----------:|-----------|
| 0 (이마트) | insertqueryGoodsWet() | 없음 | 0 (기본값) |
| 1 (생산) | insertqueryGoodsWet() | 없음 | 0 |
| 2 (홈플러스) | insertqueryGoodsWetHomeplus() | "06" | maxBoxOrder (일별 자동증가) |
| 3 (도매) | insertqueryGoodsWet() | 없음 | 0 |
| 4 (비정량) | insertqueryGoodsWet() | 없음 | 0 |
| 5 (홈플비정량) | insertqueryGoodsWet() | 없음 | 0 |
| 6 (롯데) | insertqueryGoodsWetLotte() | "07" | lotte_TryCount (1~MAX 순환) |
| 7 (생산라벨) | insertqueryGoodsWet() | 없음 | 0 |

#### 5.1.3 중량 반올림 로직 (lines 1548-1556)

| searchType | 반올림 방식 | 예시 |
|:----------:|-----------|------|
| 이마트 (0) | Floor 소수점 1자리 | 12.36 → 12.3 |
| 기타 (1,2,3,4,5,6,7) | 원본 값 그대로 | 12.36 → 12.36 |

#### 5.1.4 라벨 출력 분기 (lines 1613-1630)

| searchType | 메서드 | 비고 |
|:----------:|--------|------|
| 0 (이마트) | setPrinting() | |
| 2 (홈플러스) | setHomeplusPrinting() | |
| 4 (비정량) | setPrinting() | |
| 5 (홈플비정량) | setHomeplusPrinting() | |
| 6 (롯데) | setPrintingLotte() | BOX_ORDER 사용 |
| 7 (생산라벨) | setPrinting_prod() | |

---

## 6. 서버 업로드 흐름

### 6.1 업로드 패킷 구조

**소스**: `BixolonShipmentActivity.java:2542-2555`

```
GI_D_ID :: WEIGHT :: WEIGHT_UNIT :: PACKER_PRODUCT_CODE :: BARCODE ::
PACKER_CLIENT_CODE :: MAKINGDATE :: BOXSERIAL :: BOX_CNT :: REG_ID ::
ITEM_CODE :: BRAND_CODE :: CLIENT_TYPE :: BOX_ORDER
```

14개 필드, `::` 구분

### 6.2 searchType별 업로드 URL

| searchType | JSP | 전송방식 | INSERT 컬럼 수 |
|:----------:|-----|:-------:|:-------------:|
| 0 (이마트) | insert_goods_wet.jsp | 단건 | 13 |
| 1 (생산) | insert_goods_wet_new.jsp | 배치 (`##`) | 13 |
| 2 (홈플러스) | insert_goods_wet_homeplus.jsp | 단건 | 15 (+CHANNEL_CODE, BOX_ORDER) |
| 3 (도매) | insert_goods_wet_new.jsp | 배치 | 13 |
| 4 (비정량) | insert_goods_wet_new.jsp | 배치 | 13 |
| 5 (홈플비정량) | insert_goods_wet_new.jsp | 배치 | 13 |
| 6 (롯데) | insert_goods_wet_homeplus.jsp | 단건 | 15 (+CHANNEL_CODE, BOX_ORDER) |
| 7 (생산라벨) | insert_goods_wet_new.jsp | 배치 | 13 |

### 6.3 전송 대상 필터

```java
// SAVE_TYPE이 "F"인 레코드만 전송
if (arGI.get(i).getSAVE_TYPE().equals("F")) { ... }
```

### 6.4 전송 완료 후 처리

```java
// 전송 성공 시 SAVE_TYPE을 "Y"로 갱신
DBHandler.updatequeryGoodsWet(context, GI_D_ID, BARCODE, BOX_CNT);
// → UPDATE TB_GOODS_WET SET SAVE_TYPE = 'Y'
//   WHERE GI_D_ID = ? AND BARCODE = ? AND BOX_CNT = ?
```

### 6.5 JSP별 Oracle INSERT 컬럼

#### insert_goods_wet.jsp (이마트 표준)
```sql
INSERT INTO W_GOODS_WET (
  GOODS_WET_ID,        -- W_GOODS_WET_SEQ.NEXTVAL
  GI_D_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE,
  BARCODE, PACKER_CLIENT_CODE, MAKINGDATE, BOXSERIAL,
  BOX_CNT, REG_ID, REG_DATE, REG_TIME
)
```
REG_DATE/REG_TIME: **서버에서 자체 생성** (SYSDATE 기반)

#### insert_goods_wet_homeplus.jsp (홈플러스/롯데)
```sql
INSERT INTO W_GOODS_WET (
  -- 위 13개 + 추가:
  CHANNEL_CODE,        -- splitData[12] = CLIENT_TYPE ("06"/"07")
  BOX_ORDER            -- splitData[13]
)
```

---

## 7. DBHandler 메서드 목록

### 7.1 메서드 요약 (14개)

| # | 메서드명 | 라인 | 유형 | 용도 |
|:-:|---------|:----:|:----:|------|
| 1 | createqueryGoodsWet() | 1052-1092 | CREATE | 테이블 생성 (22컬럼) |
| 2 | selectqueryGoodsWet() | 1095-1162 | SELECT | 계근 상세 조회 (상세 다이얼로그) |
| 3 | selectquerySendGoodsWet() | 1165-1237 | SELECT | 전송용 계근 목록 조회 |
| 4 | selectqueryListGoodsWetInfo() | 1239-1280 | SELECT | 요약 통계 (합계/개수) |
| 5 | duplicatequeryGoodsWet_check() | 1283-1314 | SELECT | 전체 바코드 중복 체크 |
| 6 | duplicatequeryGoodsWet() | 1317-1351 | SELECT | 범위 내 바코드 중복 체크 |
| 7 | insertqueryGoodsWet() | 1354-1422 | INSERT | 표준 삽입 (이마트/생산/도매) |
| 8 | insertqueryGoodsWetHomeplus() | 1424-1495 | INSERT | 홈플러스 삽입 (+CLIENT_TYPE="06") |
| 9 | insertqueryGoodsWetLotte() | 1497-1568 | INSERT | 롯데 삽입 (+CLIENT_TYPE="07") |
| 10 | updatequeryGoodsWet() | 1572-1598 | UPDATE | 전송완료 표시 (SAVE_TYPE→"Y") |
| 11 | deletequerySelectGoodsWet() | 1601-1634 | DELETE | 개별 미전송 삭제 + BOX_CNT 재번호 |
| 12 | deletequeryGoodsWet() | 1637-1657 | DELETE | 전송완료 전체 삭제 (SAVE_TYPE="Y") |
| 13 | deletequeryAllGoodsWet() | 1660-1679 | DELETE | 전체 삭제 |
| 14 | selectMaxBoxOrder() | 1778-1818 | SELECT | 홈플러스 BOX_ORDER 최대값+1 |

### 7.2 주요 메서드 상세

#### selectqueryGoodsWet() -- 상세 조회 (16컬럼 읽기)
```
WHERE GI_D_ID = ? AND PACKER_PRODUCT_CODE = ?
ORDER BY BOX_CNT DESC
```
**읽는 컬럼**: GOODS_WET_ID, GI_D_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE, BARCODE, PACKER_CLIENT_CODE, BOXSERIAL, BOX_CNT, EMARTITEM_CODE, EMARTITEM, REG_ID, SAVE_TYPE, MAKINGDATE, BOX_ORDER, DUPLICATE

#### selectquerySendGoodsWet() -- 전송용 조회 (19컬럼 읽기)
```
WHERE {GI_D_ID 조건}
ORDER BY GI_D_ID ASC, BOX_CNT ASC
```
**읽는 컬럼**: 위 16개 + ITEM_CODE, BRAND_CODE, CLIENT_TYPE

#### insertqueryGoodsWet() -- 표준 삽입 (19컬럼 쓰기)
```
INSERT (GI_D_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE, BARCODE,
        PACKER_CLIENT_CODE, MAKINGDATE, BOXSERIAL, BOX_CNT,
        EMARTITEM_CODE, EMARTITEM, ITEM_CODE, BRAND_CODE,
        REG_ID, REG_DATE, REG_TIME, BOX_ORDER, SAVE_TYPE, DUPLICATE)
```
REG_DATE/REG_TIME: **로컬에서 자동 생성** (SimpleDateFormat)

#### insertqueryGoodsWetHomeplus() -- 홈플러스 삽입 (20컬럼 쓰기)
```
INSERT 위 19개 + CLIENT_TYPE ("06" 하드코딩)
```
BOX_ORDER: `maxBoxOrder` 파라미터 (selectMaxBoxOrder()로 조회)

#### insertqueryGoodsWetLotte() -- 롯데 삽입 (20컬럼 쓰기)
```
INSERT 위 19개 + CLIENT_TYPE ("07" 하드코딩)
```
BOX_ORDER: `lotte_TryCount` 파라미터 (1~MAX 순환)

#### deletequerySelectGoodsWet() -- 개별 삭제 + 재번호
```sql
-- 1) 미전송 레코드 삭제
DELETE FROM TB_GOODS_WET
WHERE BARCODE = ? AND GI_D_ID = ? AND BOX_CNT = ? AND SAVE_TYPE = 'F'

-- 2) 이후 순번 재정렬
UPDATE TB_GOODS_WET SET BOX_CNT = BOX_CNT - 1
WHERE BOX_CNT > ?
```

---

## 8. SAVE_TYPE 상태 관리

### 8.1 상태값

| 값 | 의미 | 설정 시점 |
|:--:|------|----------|
| F | 미전송 | wet_data_insert() 로컬 생성 시 |
| Y | 전송완료 | 서버 업로드 성공 후 / 서버 다운로드 시 |

### 8.2 상태별 동작 제한

| 동작 | SAVE_TYPE="F" | SAVE_TYPE="Y" |
|------|:------------:|:------------:|
| 서버 전송 | ✓ | - |
| 삭제 | ✓ | - |
| 화면 선택 (체크박스) | ✓ | - |
| 화면 표시 배경색 | 흰색/연파란(#BBDEFB) 교대 | **노란색** |
| 재출력 | ✓ | ✓ |

---

## 9. Goodswets_Info DTO 클래스

**소스**: `Goodswets_Info.java` (22개 필드)

| # | 필드명 | 기본값 | 설명 |
|:-:|--------|:-----:|------|
| 1 | GOODS_WET_ID | "" | Seq (PK) |
| 2 | GI_D_ID | "" | 출고번호 (출고상세번호 ID) |
| 3 | WEIGHT | "" | 중량, 소수점 2자리 |
| 4 | WEIGHT_UNIT | "" | 중량 단위 (LB, KG) |
| 5 | PACKER_PRODUCT_CODE | "" | 패커 상품코드 |
| 6 | BARCODE | "" | 스캔한 바코드 |
| 7 | PACKER_CLIENT_CODE | "" | 패커 거래처코드 |
| 8 | MAKINGDATE | "" | 제조일 |
| 9 | BOXSERIAL | "" | 박스번호 |
| 10 | BOX_CNT | "" | 계근 순서번호 |
| 11 | EMARTITEM_CODE | "" | 이마트 상품코드 |
| 12 | EMARTITEM | "" | 이마트 상품명 |
| 13 | ITEM_CODE | "" | 상품코드 |
| 14 | BRAND_CODE | "" | 브랜드코드 |
| 15 | REG_ID | "" | 등록자 ID |
| 16 | REG_DATE | "" | 등록 날짜 |
| 17 | REG_TIME | "" | 등록 시간 |
| 18 | SAVE_TYPE | "" | 전송 여부 |
| 19 | MEMO | "" | 메모 |
| 20 | DUPLICATE | "" | 중복스캔 |
| 21 | CLIENT_TYPE | "" | 채널코드 |
| 22 | BOX_ORDER | "" | 박스 순번 |

**특징**:
- Parcelable **미구현** (일반 POJO)
- 모든 필드 `public String` + getter/setter
- 기본 생성자만 존재

---

## 10. 연관 테이블 관계

### 10.1 데이터 출처 관계도

```
TB_SHIPMENT (VW_PDA_WID_LIST에서 다운로드)
    │
    ├── GI_D_ID ────────────→ TB_GOODS_WET.GI_D_ID (FK 역할)
    ├── PACKER_PRODUCT_CODE ─→ TB_GOODS_WET.PACKER_PRODUCT_CODE
    ├── EMARTITEM_CODE ──────→ TB_GOODS_WET.EMARTITEM_CODE
    ├── EMARTITEM ───────────→ TB_GOODS_WET.EMARTITEM
    ├── ITEM_CODE ───────────→ TB_GOODS_WET.ITEM_CODE
    └── BRAND_CODE ──────────→ TB_GOODS_WET.BRAND_CODE

TB_BARCODE_INFO (S_BARCODE_INFO에서 다운로드)
    │
    ├── PACKER_CLIENT_CODE ──→ TB_GOODS_WET.PACKER_CLIENT_CODE
    ├── BASEUNIT ────────────→ TB_GOODS_WET.WEIGHT_UNIT
    └── (바코드 파싱 규칙) ────→ TB_GOODS_WET.WEIGHT, MAKINGDATE, BOXSERIAL
```

### 10.2 컬럼별 데이터 출처 상세

|  #  | 컬럼                  | 출처                       | 코드 위치                                                | 설명                                                                                                  | 비고  |
| :-: | ------------------- | ------------------------ | ---------------------------------------------------- | --------------------------------------------------------------------------------------------------- | --- |
|  1  | GOODS_WET_ID        | 자동생성                     | SQLite AUTOINCREMENT                                 | PK, 로컬 자동 채번                                                                                        |     |
|  2  | GI_D_ID             | 출하대상 (TB_SHIPMENT)       | BSA:1507 `arSM.get(i).getGI_D_ID()`                  | 출고상세 ID (어떤 상품의 계근인지 연결)                                                                            |     |
|  3  | WEIGHT              | 바코드 추출 or 수기 입력          | BSA:1508 `gi.setWEIGHT(weight_str)`                  | 박스 1개 실측 중량 (kg). 바코드: substring(WEIGHT_FROM, WEIGHT_TO)로 추출. 수기: 사용자 직접 입력                         |     |
|  4  | WEIGHT_UNIT         | 바코드 정보 (TB_BARCODE_INFO) | BSA:1509 `work_item_bi_info.getBASEUNIT()`           | 중량 단위 (KG, LB 등)                                                                                    |     |
|  5  | PACKER_PRODUCT_CODE | 출하대상 (TB_SHIPMENT)       | BSA:1510 `arSM.get(i).getPACKER_PRODUCT_CODE()`      | 패커 상품코드                                                                                             |     |
|  6  | BARCODE             | 스캔한 바코드 원문               | BSA:1511 `work_item_fullbarcode`                     | 스캔한 바코드 전체 문자열. 수기 입력 시 빈값                                                                          |     |
|  7  | PACKER_CLIENT_CODE  | 바코드 정보 (TB_BARCODE_INFO) | BSA:1512 `work_item_bi_info.getPACKER_CLIENT_CODE()` | 패커(가공장) 거래처 코드                                                                                      |     |
|  8  | MAKINGDATE          | 바코드 추출 or 팝업 입력          | BSA:1513 `making_date`                               | 제조일자. 바코드: substring(MAKINGDATE_FROM, MAKINGDATE_TO). 킬코이/수입육: ExpiryEnterActivity 팝업 입력. 해당 없으면 빈값 |     |
|  9  | BOXSERIAL           | 바코드 추출                   | BSA:1514 `box_serial`                                | 박스 시리얼번호. 바코드: substring(BOXSERIAL_FROM, BOXSERIAL_TO). 수기 입력 시 빈값                                  |     |
| 10  | BOX_CNT             | 자동계산                     | BSA:1515 `PACKING_QTY + 1`                           | 계근 순번 (1, 2, 3, ..., 30). 현재까지 진행한 수 + 1                                                            |     |
| 11  | EMARTITEM_CODE      | 출하대상 (TB_SHIPMENT)       | BSA:1516 `arSM.get(i).getEMARTITEM_CODE()`           | 이마트 상품코드                                                                                            |     |
| 12  | EMARTITEM           | 출하대상 (TB_SHIPMENT)       | BSA:1517 `arSM.get(i).getEMARTITEM()`                | 이마트 상품명                                                                                             |     |
| 13  | ITEM_CODE           | 출하대상 (TB_SHIPMENT)       | BSA:1518 `arSM.get(i).getITEM_CODE()`                | 하이랜드 상품코드                                                                                           |     |
| 14  | BRAND_CODE          | 출하대상 (TB_SHIPMENT)       | BSA:1519 `arSM.get(i).getBRAND_CODE()`               | 브랜드 코드                                                                                              |     |
| 15  | REG_ID              | 시스템 (로그인 정보)             | BSA:1520 `Common.REG_ID`                             | 로그인한 사용자 ID                                                                                         |     |
| 16  | REG_DATE            | 시스템 (현재 시각)              | DBHandler INSERT 시 자동                                | 등록 날짜 (YYYYMMDD)                                                                                    |     |
| 17  | REG_TIME            | 시스템 (현재 시각)              | DBHandler INSERT 시 자동                                | 등록 시간 (HHmmss)                                                                                      |     |
| 18  | SAVE_TYPE           | 고정값                      | BSA:1521 `"F"`                                       | 전송 상태. "F"=미전송 → 서버 전송 성공 후 "Y"로 UPDATE                                                             | 미사용 |
| 19  | MEMO                | 미사용                      | -                                                    | 사용되지 않음                                                                                             | 미사용 |
| 20  | DUPLICATE           | 고정값                      | BSA:1522 `"F"`                                       | 중복 스캔 여부 플래그                                                                                        |     |
| 21  | CLIENT_TYPE         | INSERT 메소드 내부            | DBHandler:1467 등                                     | 마트사 구분코드 (06=홈플러스 등). 홈플러스 BOX_ORDER 계산 시 WHERE 조건으로 사용                                             |     |
| 22  | BOX_ORDER           | 마트사별 분기                  | DBHandler:1529(홈플), 1534(롯데)                         | 홈플러스: MAX(BOX_ORDER)+1 자동채번. 롯데: lotte_TryCount 글로벌 순번. 이마트: 0 (미사용)                                |     |

> **BSA** = BixolonShipmentActivity.java, **DBHandler** = DBHandler.java

#### 출처별 분류 요약

| 출처 | 컬럼 수 | 컬럼 목록 |
|------|:------:|----------|
| 출하대상 (TB_SHIPMENT) | 6 | GI_D_ID, PACKER_PRODUCT_CODE, EMARTITEM_CODE, EMARTITEM, ITEM_CODE, BRAND_CODE |
| 바코드 정보 (TB_BARCODE_INFO) | 2 | WEIGHT_UNIT, PACKER_CLIENT_CODE |
| 바코드 스캔/수기 입력 | 4 | WEIGHT, BARCODE, MAKINGDATE, BOXSERIAL |
| 자동생성/시스템 | 7 | GOODS_WET_ID, BOX_CNT, REG_ID, REG_DATE, REG_TIME, SAVE_TYPE, DUPLICATE |
| 마트사별 분기 | 2 | CLIENT_TYPE, BOX_ORDER |
| 미사용 | 1 | MEMO |

### 10.3 데이터 흐름 요약

```
[스캔 바코드]
     │
     ├─ 바코드 파싱 규칙 (TB_BARCODE_INFO) ──→ WEIGHT, MAKINGDATE, BOXSERIAL 추출
     │
     ├─ 출하 정보 (TB_SHIPMENT) ──→ GI_D_ID, ITEM_CODE, BRAND_CODE 등 매칭
     │
     └─ 시스템 ──→ REG_ID (로그인), REG_DATE/REG_TIME (현재시각), BOX_CNT (자동증가)
          │
          ▼
   TB_GOODS_WET (계근 레코드 생성)
          │
          ▼
   서버 전송 (insert_goods_wet*.jsp)
```

---

## 11. 발견된 이슈

### 11.1 BOX_ORDER 다운로드 매핑 불일치

| 항목 | 내용 |
|------|------|
| **위치** | `ProgressDlgGoodsWetSearch.java:113` |
| **증상** | `gi.setBOX_ORDER(temp[12])` 접근 |
| **원인** | `search_goods_wet.jsp`는 12개 컬럼만 반환 (index 0~11) |
| **결과** | temp[12]는 ArrayIndexOutOfBoundsException 발생 가능 |
| **영향** | try/catch로 감싸져 있어 BOX_ORDER는 기본값 0으로 유지될 가능성 |
| **심각도** | 낮음 (다운로드된 레코드의 BOX_ORDER는 이후 로직에서 사용 안됨) |

### 11.2 MEMO/DUPLICATE 미사용

| 컬럼 | 상태 |
|------|------|
| MEMO | CREATE TABLE에 존재하나 앱에서 읽기/쓰기 없음 |
| DUPLICATE | 항상 "F"로 설정되나 조건 분기에서 사용 안됨 |

### 11.3 EMARTITEM_CODE / EMARTITEM 실효성

로컬 SQLite에 저장되지만 서버로 전송되지 않고 화면에도 표시되지 않음. 삭제 후 실질적 영향 없음.

### 11.4 REG_DATE/REG_TIME 이중 관리

- **로컬**: SQLite INSERT 시 기기 시각으로 생성
- **서버**: JSP INSERT 시 SYSDATE로 별도 생성
- **다운로드**: 서버의 REG_DATE/REG_TIME을 다시 로컬에 저장
- 동일 레코드의 로컬 시각과 서버 시각이 다를 수 있음

---

## 12. 관련 보조 테이블

### TB_GOODS_WET_PRODUCTION_CALC

생산 계근에서 바코드 중복 확인용 보조 테이블

| 메서드 | 라인 | 용도 |
|--------|:----:|------|
| createqueryGoodsWetProductionCalc() | 1821 | CREATE TABLE (BARCODE 컬럼만) |
| insertGoodsWetProductionCalc() | 1843 | BARCODE INSERT |
| selectGoodsWetProductionCalc() | 1873 | 중복 바코드 EXISTS 체크 |
| deleteGoodsWetProductionCalc() | 1903 | 전체 삭제 |

---

## 문서 업데이트 이력

| 날짜 | 버전 | 변경 내용 |
|------|:----:|----------|
| 2026-03-04 | 1.0 | 초안 작성 (소스 코드 기반 전수 분석) |
