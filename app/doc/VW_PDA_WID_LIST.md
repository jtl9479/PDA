# VW_PDA_WID_LIST 분석 문서

**분석일**: 2026-01-13
**분석 기준**: 실제 VIEW SQL (`app/doc/view/VW_PDA_WID_LIST`) + Java 소스 코드

---

## 1. 개요

이마트 출하 계근 리스트 조회 VIEW 분석 문서입니다.

| 항목     | 내용                        |
| ------ | ------------------------- |
| 스키마    | HIGHLAND                  |
| VIEW명  | VW_PDA_WID_LIST           |
| 용도     | 이마트 계근 (searchType = "0") |
| 구조     | UNION ALL (해외매입 + 국내매입)   |
| 총 컬럼 수 | **41개**                   |

### 1.1 관련 파일

| 파일 | 역할 |
|------|------|
| `app/doc/view/VW_PDA_WID_LIST` | Oracle VIEW SQL 정의 |
| `ProgressDlgShipSearch.java` | 서버 데이터 파싱 |
| `BixolonShipmentActivity.java` | 계근 입력 화면 |
| `DBHandler.java` | 로컬 DB 저장 |
| `Shipments_Info.java` | 데이터 모델 (DTO) |

---

## 2. JOIN 테이블 구조

### 2.1 메인 테이블

| Alias | 테이블명 | 역할 |
|-------|----------|------|
| IH | W_GOODS_IH | 출고 헤더 |
| ID | W_GOODS_ID | 출고 상세 |
| WR | W_GOODS_R | 입고 |
| BD | I_BL_D | BL 상세 (해외매입) |
| OD | I_OFFER_D | 오퍼 상세 (국내매입) |
| BSI | B_SUPPLIER_ITEM | 공급사 품목 |
| BI | B_ITEM | 품목 마스터 |

### 2.2 EO 서브쿼리 (이마트 주문 정보)

| Alias | 테이블명 | 역할 |
|-------|----------|------|
| EOI | W_EMART_ORDER_ITEM | 이마트 주문 아이템 |
| EB | B_EMART_BARCODE | 이마트 바코드 |
| BCC | B_COMMON_CODE | 센터코드 → 센터명 |
| BCC2 | B_COMMON_CODE | 점포코드 → WH_AREA |
| X | B_COMMON_CODE | 저울바코드 사용센터 |

### 2.3 사용 함수

| 함수 | 용도 | 입력값 |
|------|------|--------|
| DE_ITEM() | 상품코드 → 상품명 조회 | ITEM_CODE |
| DE_COMMON() | 공통코드 → 브랜드명 조회 | 'BRAND', BRAND_CODE |
| DE_CLIENT() | 거래처코드 → 거래처명 조회 | CLIENT_CODE / PACKER_CODE |
| DE_CLIENT2() | 거래처명 조회 (CJ센터용) | CLIENT_CODE |

### 2.4 JOIN 관계도

```
W_GOODS_IH (IH)
    │
    ├─── INNER JOIN W_GOODS_ID (ID) ON IH.GI_H_ID = ID.GI_H_ID
    │         │
    │         ├─── INNER JOIN W_GOODS_R (WR) ON ID.GOODS_R_ID = WR.GOODS_R_ID
    │         │         │
    │         │         ├─── [해외매입] INNER JOIN I_BL_D (BD)
    │         │         │         ON BD.BL_D_ID = WR.BL_D_ID
    │         │         │         AND BD.BL_S_ID = WR.BL_S_ID
    │         │         │
    │         │         └─── [국내매입] INNER JOIN I_OFFER_D (OD)
    │         │                   ON OD.OFFER_D_ID = WR.BL_D_ID
    │         │
    │         ├─── INNER JOIN B_ITEM (BI) ON ID.ITEM_CODE = BI.ITEM_CODE
    │         │
    │         └─── INNER JOIN EO (서브쿼리) ON ID.EOI_ID = EO.EOI_ID
    │
    └─── LEFT OUTER JOIN B_SUPPLIER_ITEM (BSI)
              ON PACKER_CODE + PACKER_PRODUCT_CODE
```

---

## 3. VIEW 컬럼 목록 (41개)

### 3.0 용도 분류 기준

#### 3.0.1 서버전송
**정의**: 계근 완료 후 서버로 전송되는 HTTP 패킷에 포함
**목적지**: `insert_goods_wet.jsp` → `W_GOODS_WET` 테이블 INSERT

```java
packet += si.get컬럼명() + "::";   // ✓ 서버전송
completeStr = ... + si.get컬럼명(); // ✓ 서버전송
```
- **제외**: 로컬 DB 저장만 되는 경우

#### 3.0.2 화면표시
**정의**: 사용자가 화면에서 직접 볼 수 있는 UI 요소에 표시

```java
editText.setText(si.get컬럼명());           // ✓ Activity UI
holder.textView.setText(si.get컬럼명());    // ✓ ListView/Adapter
```
- **제외**: Log.d(), Log.i() 등 디버그 출력

#### 3.0.3 바코드생성
**정의**: 라벨에 인쇄되는 바코드 문자열 생성에 사용

```java
pBarcode = si.get컬럼명().substring(0, 6) + ...;  // ✓
sBarcode = si.get컬럼명();                         // ✓
meatCenterBarcode = ... + si.get컬럼명();          // ✓
```

#### 3.0.4 라벨출력
**정의**: 프린터로 출력되는 라벨의 텍스트 영역에 인쇄

```java
slcsText(..., si.get컬럼명());              // ✓ Bixolon
WoosimCmd.getTTFcode(..., si.get컬럼명());  // ✓ Woosim
변수 = si.컬럼명; → slcsText(..., 변수);     // ✓ 변수 경유도 포함
```

#### 3.0.5 로직분기
**정의**: if/switch 조건문에서 값 비교로 프로그램 흐름이 변경

```java
if (si.get컬럼명().equals("값"))         // ✓
if (si.get컬럼명().contains("값"))       // ✓
switch (si.get컬럼명())                  // ✓
```
- **제외**: 단순 null 체크 `if (si.get컬럼명() != null)`
- **제외**: 길이 체크 `if (si.컬럼명.length() > 14)` (표시 방식 결정일 뿐)

#### 3.0.6 기타
**정의**: 위 5개에 해당하지 않지만 비즈니스 로직에서 사용

| 용도 | 판단 기준 |
|------|----------|
| 조회조건 | JSP WHERE절에 사용 |
| 중복스캔방지 | list_bl.add() 등 중복 체크 |
| DB조회조건 | selectquery 파라미터 |
| 아이템검색 | find_work_info() 파라미터 |

#### 3.0.7 미사용
**정의**: 로컬 SQLite DB에 저장만 되고 비즈니스 로직에서 사용 안됨

```java
// DBHandler.java에서만 발견됨
si.set컬럼명(temp[n]);  // 파싱 저장
si.get컬럼명()          // DB INSERT만
```

#### 3.0.8 앱미전달
**정의**: VIEW에 정의되어 있으나 JSP에서 앱으로 전송하지 않음

- 예: GR_WAREHOUSE_CODE, MAJOR_CATEGORY, CONTAINER_TYPE

#### 3.0.9 다중 용도 처리
**원칙**: 해당하는 모든 용도를 기재

```
예: STORE_CODE → 바코드생성 ✓, 라벨출력 ✓, 로직분기 ✓
```

---

### 3.1 VIEW 정의 순서 (CREATE VIEW 문)

| Index | 컬럼명                 | 데이터 출처                                                   | 설명              | 용도                           | 여부  |
| :---: | ------------------- | -------------------------------------------------------- | --------------- | ---------------------------- | --- |
|   0   | GI_H_ID             | IH.GI_H_ID                                               | 출고헤더ID          | 미사용(로컬DB저장)                  | O   |
|   1   | GI_D_ID             | ID.GI_D_ID                                               | 출고상세ID (PK)     | 서버전송(PK)                     | O   |
|   2   | EOI_ID              | ID.EOI_ID                                                | 이마트 주문ID        | 미사용(로컬DB저장)                  | O   |
|   3   | ITEM_CODE           | ID.ITEM_CODE                                             | 상품코드            | 서버전송                         | O   |
|   4   | ITEM_NAME           | DECODE(EO.ITEM_NAME, NULL, DE_ITEM())                    | 상품명             | 화면표시                         | O   |
|   5   | EMARTITEM_CODE      | EO.ITEM_CODE                                             | 이마트 상품코드        | 바코드생성                        | O   |
|   6   | EMARTITEM           | EO.ITEM_NAME                                             | 이마트 상품명         | 라벨출력                         | O   |
|   7   | GI_REQ_PKG          | ID.GI_REQ_PKG                                            | 출하요청수량          | 화면표시, 로직분기                   | O   |
|   8   | GI_REQ_QTY          | ID.GI_REQ_QTY                                            | 출하요청중량          | 화면표시(진행률)                    | O   |
|   9   | AMOUNT              | ID.AMOUNT                                                | 출하상품금액          | 미사용                          | O   |
|  10   | GOODS_R_ID          | WR.GOODS_R_ID                                            | 입고ID            | 미사용                          | O   |
|  11   | GR_REF_NO           | WR.GR_REF_NO                                             | 창고입고번호          | 미사용                          | X   |
|  12   | GI_REQ_DATE         | GI_REQ_DATE                                              | 출하요청일           | 미사용(WHERE는 Common.selectDay) | O   |
|  13   | BL_NO               | DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) | BL번호            | 화면표시, 중복스캔방지                 | O   |
|  14   | BRAND_CODE          | ID.BRAND_CODE                                            | 브랜드코드           | 서버전송 (JSP 미사용)               | O   |
|  15   | BRANDNAME           | DE_COMMON('BRAND', ID.BRAND_CODE)                        | 브랜드명            | 미사용                          |     |
|  16   | CLIENT_CODE         | IH.CLIENT_CODE                                           | 출고업체코드          | 로직분기(DB조회조건)                 | O   |
|  17   | CLIENTNAME          | DECODE(SUBSTR(EO.CENTERNAME,1,2),'CJ',...)               | 출고업체명           | 화면표시, 라벨출력                   | O   |
|  18   | CENTERNAME          | EO.CENTERNAME                                            | 센터명             | 로직분기, 라벨출력                   |     |
|  19   | ITEM_SPEC           | WR.ITEM_SPEC                                             | 상품규격            | 라벨출력                         |     |
|  20   | CT_CODE             | WR.CT_CODE                                               | 원산지코드           | 라벨출력                         |     |
|  21   | PACKER_CODE         | BD.PACKER_CODE / OD.PACKER_CODE                          | 패커코드            | 로직분기(킬코이30228)               |     |
|  22   | IMPORT_ID_NO        | WR.IMPORT_ID_NO                                          | 수입식별번호          | 바코드생성, 라벨출력                  |     |
|  23   | PACKERNAME          | DE_CLIENT(BD.PACKER_CODE)                                | 패커명             | 미사용                          |     |
|  24   | PACKER_PRODUCT_CODE | BD.PACKER_PRODUCT_CODE                                   | 패커상품코드          | 화면표시, 서버전송                   |     |
|  25   | BARCODE_TYPE        | DECODE(CENTER_SCALE_USE_YN,...)                          | 바코드타입           | 로직분기(M0/M3/M4/M9)            |     |
|  26   | ITEM_TYPE           | EO.ITEM_TYPE                                             | 아이템타입 (W/S/J/B) | 로직분기(계근방식)                   |     |
|  27   | PACKWEIGHT          | EO.PACKWEIGHT                                            | 포장중량            | 제품(J)계근                      |     |
|  28   | BARCODEGOODS        | (SELECT FROM S_BARCODE_INFO)                             | 바코드상품코드         | 아이템검색                        |     |
|  29   | STORE_IN_DATE       | STORE_IN_DATE                                            | 입고일자            | 라벨출력(납품일자)                   |     |
|  30   | GR_WAREHOUSE_CODE   | WR.GR_WAREHOUSE_CODE                                     | 창고코드            | WHERE조건(앱미전달)                |     |
|  31   | EMARTLOGIS_CODE     | DECODE(EO.EMARTLOGIS_CODE,NULL,'0000000',...)            | 이마트물류코드         | 바코드생성, 라벨출력, 로직분기            |     |
|  32   | EMARTLOGIS_NAME     | DECODE(EO.EMARTLOGIS_NAME,NULL,'정보없음',...)               | 이마트물류명          | 미사용                          |     |
|  33   | WH_AREA             | WH_AREA                                                  | 창고구역            | 라벨출력                         |     |
|  34   | USE_NAME            | EO.USE_NAME                                              | 용도명             | 라벨출력                         |     |
|  35   | USE_CODE            | EO.USE_CODE                                              | 용도코드            | 바코드생성                        |     |
|  36   | CT_NAME             | (SELECT FROM B_COMMON_CODE)\|\|'산'                       | 원산지명            | 라벨출력                         |     |
|  37   | STORE_CODE          | EO.STORECODE                                             | 점포코드            | 로직분기, 바코드생성, 라벨출력            |     |
|  38   | EMART_PLANT_CODE    | DECODE(EO.STORECODE,'9820',BSI.EMART_PLANT_CODE,NULL)    | 이마트공장코드         | 바코드생성, 로직분기                  |     |
|  39   | MAJOR_CATEGORY      | BI.MAJOR_CATEGORY                                        | 대분류(축종)         | 앱미전달                         |     |
|  40   | CONTAINER_TYPE      | BI.CONTAINER_TYPE                                        | 컨테이너타입(냉장/냉동)   | 앱미전달                         |     |

### 3.2 용도별 컬럼 분류

#### 3.2.0 용도별 체크표 (전체 41개)

| Index | 컬럼명                 | 서버전송 | 화면표시 | 바코드 | 라벨  | 로직분기 | 기타  | 미사용 | 앱미전달 | 비고                               |     |     |
| :---: | ------------------- | :--: | :--: | :-: | :-: | :--: | :-: | :-: | :--: | -------------------------------- | --- | --- |
|   0   | GI_H_ID             |      |      |     |     |      |     |  ✓  |      | 로컬DB저장만                          |     |     |
|   1   | GI_D_ID             |  ✓   |      |     |     |      |     |     |      | PK                               |     |     |
|   2   | EOI_ID              |      |      |     |     |      |     |  ✓  |      | 로컬DB저장만                          |     |     |
|   3   | ITEM_CODE           |  ✓   |      |     |     |      |     |     |      | 상품코드                             |     |     |
|   4   | ITEM_NAME           |      |  ✓   |     |     |      |     |     |      | 상품명                              |     |     |
|   5   | EMARTITEM_CODE      |      |      |  ✓  |     |      |     |     |      | 앞6자리                             |     |     |
|   6   | EMARTITEM           |      |      |     |  ✓  |      |     |     |      | 이마트상품명                           |     |     |
|   7   | GI_REQ_PKG          |      |  ✓   |     |     |  ✓   |     |     |      | 진행률+완료체크                         |     |     |
|   8   | GI_REQ_QTY          |      |  ✓   |     |     |      |     |     |      | 진행률(중량)                          |     |     |
|   9   | AMOUNT              |      |      |     |     |      |     |  ✓  |      | 로컬DB저장만                          |     |     |
|  10   | GOODS_R_ID          |      |      |     |     |      |     |  ✓  |      | 로컬DB저장만                          |     |     |
|  11   | GR_REF_NO           |      |      |     |     |      |     |  ✓  |      | 로컬DB저장만                          |     |     |
|  12   | GI_REQ_DATE         |      |      |     |     |      |     |  ✓  |      | 로컬DB저장만(WHERE는 Common.selectDay) |     |     |
|  13   | BL_NO               |      |  ✓   |     |     |      |  ✓  |     |      | 리스트+중복방지                         |     |     |
|  14   | BRAND_CODE          |  ✓   |      |     |     |      |     |     |      | 서버전송됨                            |     |     |
|  15   | BRANDNAME           |      |      |     |     |      |     |  ✓  |      | 로컬DB저장만                          |     |     |
|  16   | CLIENT_CODE         |      |      |     |     |  ✓   |     |     |      | DB조회조건                           |     |     |
|  17   | CLIENTNAME          |      |  ✓   |     |  ✓  |      |     |     |      | 점포명                              |     |     |
|  18   | CENTERNAME          |      |      |     |  ✓  |  ✓   |     |     |      | TRD/WET/E/T                      |     |     |
|  19   | ITEM_SPEC           |      |      |     |  ✓  |      |     |     |      | 라벨출력                             |     |     |
|  20   | CT_CODE             |      |      |     |  ✓  |      |     |     |      | 라벨인쇄                             |     |     |
|  21   | PACKER_CODE         |      |      |     |     |  ✓   |     |     |      | 킬코이30228                         |     |     |
|  22   | IMPORT_ID_NO        |      |      |  ✓  |  ✓  |      |     |     |      | 수입식별번호                           |     |     |
|  23   | PACKERNAME          |      |      |     |     |      |     |  ✓  |      | 로컬DB저장만                          |     |     |
|  24   | PACKER_PRODUCT_CODE |  ✓   |  ✓   |     |     |      |     |     |      | 패커상품코드                           |     |     |
|  25   | BARCODE_TYPE        |      |      |     |     |  ✓   |     |     |      | M0/M3/M4/M9                      |     |     |
|  26   | ITEM_TYPE           |      |      |     |     |  ✓   |     |     |      | W/J/B/S                          |     |     |
|  27   | PACKWEIGHT          |      |      |     |  ✓  |      |  ✓  |     |      | 제품(J)계근                          |     |     |
|  28   | BARCODEGOODS        |      |      |     |     |      |  ✓  |     |      | 아이템검색                            |     |     |
|  29   | STORE_IN_DATE       |      |      |     |  ✓  |      |     |     |      | 라벨납품일자                           |     |     |
|  30   | GR_WAREHOUSE_CODE   |      |      |     |     |      |     |     |  ✓   | WHERE조건                          |     |     |
|  31   | EMARTLOGIS_CODE     |      |      |  ✓  |  ✓  |  ✓   |     |     |      | 물류+지점+분기                         |     |     |
|  32   | EMARTLOGIS_NAME     |      |      |     |     |      |     |  ✓  |      | 로컬DB저장만                          |     |     |
|  33   | WH_AREA             |      |      |     |  ✓  |      |     |     |      | 창고구역                             |     |     |
|  34   | USE_NAME            |      |      |     |  ✓  |      |     |     |      | 용도명                              |     |     |
|  35   | USE_CODE            |      |      |  ✓  |     |      |     |     |      | 용도코드                             |     |     |
|  36   | CT_NAME             |      |      |     |  ✓  |      |     |     |      | 원산지명                             |     |     |
|  37   | STORE_CODE          |      |      |  ✓  |  ✓  |  ✓   |     |     |      | 미트센터9231                         |     |     |
|  38   | EMART_PLANT_CODE    |      |      |  ✓  |     |  ✓   |     |     |      | 가공장코드                            |     |     |
|  39   | MAJOR_CATEGORY      |      |      |     |     |      |     |     |  ✓   | 축종                               |     |     |
|  40   | CONTAINER_TYPE      |      |      |     |     |      |     |     |  ✓   | 냉장/냉동                            |     |     |

**용도별 컬럼 수:**

| 용도 | 컬럼 수 |
|------|:------:|
| 서버전송 | 4 |
| 화면표시 | 6 |
| 바코드생성 | 6 |
| 라벨출력 | 12 |
| 로직분기 | 9 |
| 기타 | 3 |
| 미사용 | 9 |
| 앱미전달 | 3 |

#### 3.2.1 계근결과 서버전송용 (4개)

계근 완료 후 서버에 업로드되는 VIEW 컬럼 (실제 패킷에 포함)

**데이터 흐름:**
```
PDA 앱 (BixolonShipmentActivity)
    ↓ HTTP POST (packet 문자열)
JSP 서버 (insert_goods_wet.jsp)
    ↓ INSERT
Oracle DB (W_GOODS_WET 테이블)
```

| Index | 컬럼명                 | 설명          | 목적                                  |
| :---: | ------------------- | ----------- | ----------------------------------- |
|   1   | GI_D_ID             | 출고상세ID (PK) | W_GOODS_WET.GI_D_ID → W_GOODS_ID 연결 |
|   3   | ITEM_CODE           | 상품코드        | 패킷 포함 (JSP에서 미사용)                   |
|  14   | BRAND_CODE          | 브랜드코드       | 패킷 포함 (JSP에서 미사용)                   |
|  24   | PACKER_PRODUCT_CODE | 패커상품코드      | W_GOODS_WET.PACKER_PRODUCT_CODE 저장  |

**참고**: 서버 전송 패킷에는 VIEW 컬럼 외에도 WEIGHT, BARCODE, BOXSERIAL 등 계근 시 생성되는 데이터가 포함됨

#### 3.2.2 화면표시용 (6개)

PDA 화면에 표시되는 데이터

| Index | 컬럼명                 | 표시 내용           |     |
| :---: | ------------------- | --------------- | --- |
|   4   | ITEM_NAME           | 상품명             |     |
|   7   | GI_REQ_PKG          | 출하요청수량 (진행률)    |     |
|   8   | GI_REQ_QTY          | 출하요청중량 (진행률)    |     |
|  13   | BL_NO               | BL번호 (리스트 뒤4자리) |     |
|  17   | CLIENTNAME          | 출고업체명 (점포명)     |     |
|  24   | PACKER_PRODUCT_CODE | 패커 상품코드         |     |

#### 3.2.3 바코드생성용 (6개)

바코드 구성에 사용되는 데이터

| Index | 컬럼명              | 바코드 내 위치/용도 |
| :---: | ---------------- | ----------- |
|   5   | EMARTITEM_CODE   | 앞 6자리       |
|  22   | IMPORT_ID_NO     | 수입식별번호/이력번호 |
|  31   | EMARTLOGIS_CODE  | 물류 상품코드     |
|  35   | USE_CODE         | 원료육 용도코드    |
|  37   | STORE_CODE       | 점포코드        |
|  38   | EMART_PLANT_CODE | 미트센터 가공장코드  |

#### 3.2.4 라벨출력용 (12개)

라벨 인쇄 시 사용되는 데이터

| Index | 컬럼명             | 라벨 내 표시                            |
| :---: | --------------- | ---------------------------------- |
|   6   | EMARTITEM       | 이마트 상품명                            |
|  17   | CLIENTNAME      | 점포명 (pointName)                    |
|  18   | CENTERNAME      | 센터명                                |
|  19   | ITEM_SPEC       | 상품규격 (EMARTITEM + "/" + ITEM_SPEC) |
|  20   | CT_CODE         | 원산지코드                              |
|  22   | IMPORT_ID_NO    | 수입식별번호                             |
|  29   | STORE_IN_DATE   | 납품일자                               |
|  31   | EMARTLOGIS_CODE | 지점코드 (정량)                          |
|  33   | WH_AREA         | 창고구역                               |
|  34   | USE_NAME        | 용도명                                |
|  36   | CT_NAME         | 원산지명                               |
|  37   | STORE_CODE      | 점포코드 (비정량)                         |

#### 3.2.5 로직분기용 (9개)

계근/출력 방식 결정에 사용되는 데이터

| Index | 컬럼명              | 분기 조건              | 설명                                   |
| :---: | ---------------- | ------------------ | ------------------------------------ |
|   7   | GI_REQ_PKG       | equals 비교          | 요청수량==완료수량 → 계근완료 여부 판별              |
|  16   | CLIENT_CODE      | DB 조회 조건           | selectqueryGoodsWet 조회 파라미터          |
|  18   | CENTERNAME       | TRD/WET/E/T        | 센터 유형 판별                             |
|  21   | PACKER_CODE      | 30228              | 킬코이 제품 판별                            |
|  25   | BARCODE_TYPE     | M0/M3/M4/M9        | 바코드 유형별 처리                           |
|  26   | ITEM_TYPE        | W/J/B/S/HW         | W:원료육, J:제품, B:비정량, S:세트, HW:홈플러스원료육 |
|  31   | EMARTLOGIS_CODE  | LOGIS_CODE_DEFAULT | 미트센터 납품 시 물류코드 유무 분기                 |
|  37   | STORE_CODE       | 9231               | 이마트 미트센터 판별                          |
|  38   | EMART_PLANT_CODE | 공백 여부              | 미트센터 납품 시 가공장코드 유무 분기                |

#### 3.2.6 기타 (4개)

| Index | 컬럼명          | 용도                     |
| :---: | ------------ | ---------------------- |
|  12   | GI_REQ_DATE  | 조회조건 (출하요청일)           |
|  13   | BL_NO        | 중복스캔방지                 |
|  27   | PACKWEIGHT   | 제품(J) 계근 시 포장중량        |
|  28   | BARCODEGOODS | 아이템검색 (바코드 스캔 시 상품 매칭) |

#### 3.2.7 미사용 컬럼 (8개)

로컬 DB에 저장만 되고 실제 사용되지 않는 컬럼

| Index | 컬럼명             | 설명      | 비고               |
| :---: | --------------- | ------- | ---------------- |
|   0   | GI_H_ID         | 출고헤더ID  | 서버 수신 → 로컬DB 저장만 |
|   2   | EOI_ID          | 이마트주문ID | 서버 수신 → 로컬DB 저장만 |
|   9   | AMOUNT          | 출하상품금액  | 서버 수신 → 로컬DB 저장만 |
|  10   | GOODS_R_ID      | 입고ID    | 서버 수신 → 로컬DB 저장만 |
|  11   | GR_REF_NO       | 창고입고번호  | 서버 수신 → 로컬DB 저장만 |
|  15   | BRANDNAME       | 브랜드명    | 서버 수신 → 로컬DB 저장만 |
|  23   | PACKERNAME      | 패커명     | 서버 수신 → 로컬DB 저장만 |
|  32   | EMARTLOGIS_NAME | 이마트물류명  | 주석에만 언급, 실제 미사용  |

#### 3.2.8 앱 미전달 컬럼 (3개)

| Index | 컬럼명               | 설명            | 미전달 사유         |
| :---: | ----------------- | ------------- | -------------- |
|  30   | GR_WAREHOUSE_CODE | 창고코드          | WHERE 조건으로만 사용 |
|  39   | MAJOR_CATEGORY    | 대분류(축종)       | JSP에서 제외       |
|  40   | CONTAINER_TYPE    | 컨테이너타입(냉장/냉동) | JSP에서 제외       |

### 3.3 테이블별 컬럼 분류

#### 3.3.1 IH (W_GOODS_IH) - 출고헤더

| Index | 컬럼명         | 설명     |
| :---: | ----------- | ------ |
|   0   | GI_H_ID     | 출고헤더ID |
|  12   | GI_REQ_DATE | 출하요청일  |
|  16   | CLIENT_CODE | 출고업체코드 |

#### 3.3.2 ID (W_GOODS_ID) - 출고상세

| Index | 컬럼명        | 설명          |
| :---: | ---------- | ----------- |
|   1   | GI_D_ID    | 출고상세ID (PK) |
|   2   | EOI_ID     | 이마트 주문ID    |
|   3   | ITEM_CODE  | 상품코드        |
|   7   | GI_REQ_PKG | 출하요청수량      |
|   8   | GI_REQ_QTY | 출하요청중량      |
|   9   | AMOUNT     | 출하상품금액      |
|  14   | BRAND_CODE | 브랜드코드       |

#### 3.3.3 WR (W_GOODS_R) - 입고

| Index | 컬럼명 | 설명 |
|:-----:|--------|------|
| 10 | GOODS_R_ID | 입고ID |
| 11 | GR_REF_NO | 창고입고번호 |
| 13 | BL_NO | BL번호 (DECODE: IMPORT_ID_NO 또는 BL_NO) |
| 19 | ITEM_SPEC | 상품규격 |
| 20 | CT_CODE | 원산지코드 |
| 22 | IMPORT_ID_NO | 수입식별번호 |
| 30 | GR_WAREHOUSE_CODE | 창고코드 (앱미전달) |

#### 3.3.4 BD/OD (I_BL_D / I_OFFER_D) - BL상세/오퍼상세

| Index | 컬럼명 | 출처 | 설명 |
|:-----:|--------|------|------|
| 21 | PACKER_CODE | BD.PACKER_CODE / OD.PACKER_CODE | 패커코드 |
| 24 | PACKER_PRODUCT_CODE | BD.PACKER_PRODUCT_CODE / OD.PACKER_PRODUCT_CODE | 패커상품코드 |

**참고**: 해외매입은 BD(I_BL_D), 국내매입은 OD(I_OFFER_D) 사용

#### 3.3.5 EOI (W_EMART_ORDER_ITEM) - 이마트주문아이템

| Index | 컬럼명 | 설명 |
|:-----:|--------|------|
| 5 | EMARTITEM_CODE | 이마트 상품코드 |
| 6 | EMARTITEM | 이마트 상품명 |
| 29 | STORE_IN_DATE | 입고일자 |
| 37 | STORE_CODE | 점포코드 |

#### 3.3.6 EB (B_EMART_BARCODE) - 이마트바코드

| Index | 컬럼명 | 설명 |
|:-----:|--------|------|
| 25 | BARCODE_TYPE | 바코드타입 (DECODE 처리) |
| 26 | ITEM_TYPE | 아이템타입 (W/J/B/S) |
| 27 | PACKWEIGHT | 포장중량 |
| 31 | EMARTLOGIS_CODE | 이마트물류코드 |
| 32 | EMARTLOGIS_NAME | 이마트물류명 (서브쿼리) |
| 35 | USE_CODE | 용도코드 |

#### 3.3.7 BCC (B_COMMON_CODE) - 공통코드

| Index | 컬럼명 | Alias | 설명 |
|:-----:|--------|:-----:|------|
| 18 | CENTERNAME | BCC | 센터명 (CODE_NAME) |
| 33 | WH_AREA | BCC2 | 창고구역 (REF_CODE2) |
| 34 | USE_NAME | - | 용도명 (서브쿼리) |

#### 3.3.8 BI (B_ITEM) - 품목마스터

| Index | 컬럼명 | 설명 |
|:-----:|--------|------|
| 39 | MAJOR_CATEGORY | 대분류/축종 (앱미전달) |
| 40 | CONTAINER_TYPE | 컨테이너타입 (앱미전달) |

**참고**: BARCODE_TYPE 결정 시 BI.ITEM_TYPE, BI.MAJOR_CATEGORY도 사용됨

#### 3.3.9 BSI (B_SUPPLIER_ITEM) - 공급사품목

| Index | 컬럼명 | 설명 |
|:-----:|--------|------|
| 38 | EMART_PLANT_CODE | 이마트공장코드 (STORE_CODE='9820'일 때만) |

#### 3.3.10 함수/서브쿼리 파생 컬럼

| Index | 컬럼명 | 생성 방식 | 설명 |
|:-----:|--------|-----------|------|
| 4 | ITEM_NAME | DECODE + DE_ITEM() | 상품명 (EO 없으면 함수 호출) |
| 15 | BRANDNAME | DE_COMMON('BRAND', BRAND_CODE) | 브랜드명 |
| 17 | CLIENTNAME | DECODE + DE_CLIENT() / DE_CLIENT2() | 출고업체명 (CJ센터 분기) |
| 23 | PACKERNAME | DE_CLIENT(PACKER_CODE) | 패커명 |
| 28 | BARCODEGOODS | S_BARCODE_INFO 서브쿼리 | 바코드상품코드 |
| 36 | CT_NAME | B_COMMON_CODE 서브쿼리 + '산' | 원산지명 |

### 3.4 테이블별 컬럼 수 요약

| 테이블 | 컬럼 수 | 비고 |
|--------|:------:|------|
| IH (W_GOODS_IH) | 3 | 출고헤더 |
| ID (W_GOODS_ID) | 7 | 출고상세 |
| WR (W_GOODS_R) | 7 | 입고 |
| BD/OD (I_BL_D/I_OFFER_D) | 2 | BL/오퍼상세 |
| EOI (W_EMART_ORDER_ITEM) | 4 | 이마트주문아이템 |
| EB (B_EMART_BARCODE) | 6 | 이마트바코드 |
| BCC (B_COMMON_CODE) | 3 | 공통코드 |
| BI (B_ITEM) | 2 | 품목마스터 (앱미전달) |
| BSI (B_SUPPLIER_ITEM) | 1 | 공급사품목 |
| 함수/서브쿼리 파생 | 6 | DE_ITEM, DE_COMMON 등 |
| **합계** | **41** | |

---

## 4. VIEW → JSP → Java 컬럼 순서 변환

### 4.1 중요: VIEW 순서와 Java 파싱 순서가 다름!

JSP(서버)가 VIEW 결과를 앱에 전송할 때 **컬럼 순서를 재배열**합니다.

**주요 차이점:**

| 구분       | VIEW 순서           | JSP 전송 순서 (= Java temp[]) |
| -------- | ----------------- | ------------------------- |
| index 21 | PACKER_CODE       | IMPORT_ID_NO              |
| index 22 | IMPORT_ID_NO      | PACKER_CODE               |
| index 30 | GR_WAREHOUSE_CODE | EMARTLOGIS_CODE           |
| index 31 | EMARTLOGIS_CODE   | EMARTLOGIS_NAME           |
| index 32 | EMARTLOGIS_NAME   | WH_AREA                   |
| ...      | ...               | ...                       |


**변환 규칙:**
1. **index 21-22 순서 교체**: PACKER_CODE ↔ IMPORT_ID_NO
2. **GR_WAREHOUSE_CODE 제외**: VIEW index 30이 앱에 전달되지 않음 (WHERE 조건으로만 사용)
3. **index 30 이후 당겨짐**: VIEW 31번부터 컬럼이 한 칸씩 앞으로 이동
4. **MAJOR_CATEGORY, CONTAINER_TYPE 제외**: VIEW index 39, 40이 앱에 전달되지 않음

---

## 5. Java 파싱 매핑 (ProgressDlgShipSearch.java)

### 5.1 기본 컬럼 (temp[0]~temp[31]) - 모든 searchType 공통

| temp[] | Java 필드 | VIEW 컬럼 | 사용 목적 |
|:------:|-----------|-----------|-----------|
| 0 | setGI_H_ID() | GI_H_ID | 출고 헤더 ID |
| 1 | setGI_D_ID() | GI_D_ID | 출고 상세 ID (PK) |
| 2 | setEOI_ID() | EOI_ID | 이마트 발주 ID |
| 3 | setITEM_CODE() | ITEM_CODE | 상품코드 |
| 4 | setITEM_NAME() | ITEM_NAME | 상품명, 화면 표시 |
| 5 | setEMARTITEM_CODE() | EMARTITEM_CODE | 이마트 상품코드, 바코드 생성 |
| 6 | setEMARTITEM() | EMARTITEM | 이마트 상품명, 라벨 출력 |
| 7 | setGI_REQ_PKG() | GI_REQ_PKG | 출하요청수량, 진행률 표시 |
| 8 | setGI_REQ_QTY() | GI_REQ_QTY | 출하요청중량, 진행률 표시 |
| 9 | setAMOUNT() | AMOUNT | 금액 |
| 10 | setGOODS_R_ID() | GOODS_R_ID | 입고번호 |
| 11 | setGR_REF_NO() | GR_REF_NO | 창고입고번호 |
| 12 | setGI_REQ_DATE() | GI_REQ_DATE | 출하요청일 |
| 13 | setBL_NO() | BL_NO | BL번호, 중복 스캔 방지 |
| 14 | setBRAND_CODE() | BRAND_CODE | 브랜드코드, 서버 전송 |
| 15 | setBRANDNAME() | BRANDNAME | 브랜드명 |
| 16 | setCLIENT_CODE() | CLIENT_CODE | 출고업체코드 |
| 17 | setCLIENTNAME() | CLIENTNAME | 출고업체명, 리스트 표시 |
| 18 | setCENTERNAME() | CENTERNAME | 센터명, TRD/WET/E/T 판별 |
| 19 | setITEM_SPEC() | ITEM_SPEC | 상품 스펙 |
| 20 | setCT_CODE() | CT_CODE | 원산지코드 |
| 21 | setIMPORT_ID_NO() | IMPORT_ID_NO | 수입식별번호, 바코드/라벨 |
| 22 | setPACKER_CODE() | PACKER_CODE | 패커코드, 킬코이(30228) 판별 |
| 23 | setPACKERNAME() | PACKERNAME | 패커명 |
| 24 | setPACKER_PRODUCT_CODE() | PACKER_PRODUCT_CODE | 패커 상품코드, 화면 표시 |
| 25 | setBARCODE_TYPE() | BARCODE_TYPE | 바코드 타입, M0/M3/M4 분기 |
| 26 | setITEM_TYPE() | ITEM_TYPE | 상품유형 (W/J/B/S/HW), 계근 분기 |
| 27 | setPACKWEIGHT() | PACKWEIGHT | 팩 중량, 제품(J) 계근 |
| 28 | setBARCODEGOODS() | BARCODEGOODS | 바코드 상품코드, 아이템 검색 |
| 29 | setSTORE_IN_DATE() | STORE_IN_DATE | 입고일자 |
| 30 | setEMARTLOGIS_CODE() | EMARTLOGIS_CODE | 이마트 물류코드, 바코드 생성 |
| 31 | setEMARTLOGIS_NAME() | EMARTLOGIS_NAME | 이마트 물류명 |

### 5.2 추가 컬럼 - searchType별 분기

**searchType "0", "4" (이마트, 비정량)**: temp[32]~temp[37]

| temp[] | Java 필드 | 용도 |
|:------:|-----------|------|
| 32 | setWH_AREA() | 창고구역, 라벨 출력 |
| 33 | setUSE_NAME() | 용도명, 라벨 출력 |
| 34 | setUSE_CODE() | 용도코드, 바코드 생성 |
| 35 | setCT_NAME() | 원산지명, 라벨 출력 |
| 36 | setSTORE_CODE() | 점포코드, 미트센터(9231) 판별 |
| 37 | setEMART_PLANT_CODE() | 가공장코드, 미트센터 바코드 |

**searchType "5" (홈플러스 비정량)**: temp[32]~temp[36]

**searchType "6" (롯데)**: temp[32]~temp[33]

| temp[] | Java 필드 | 용도 |
|:------:|-----------|------|
| 32 | setWH_AREA() | 창고구역 |
| 33 | setLAST_BOX_ORDER() | 마지막 박스순번 (1~9999) |

---

## 6. WHERE 조건

### 6.1 VIEW 내부 WHERE (고정)

| 조건 | 설명 |
|------|------|
| `ID.PACKING_QTY = 0` | 미계근 건만 조회 |
| `ID.GI_REQ_PKG <> 0` | 요청수량 있는 건만 |
| `EO.EOI_ID IS NOT NULL` | 이마트 주문 있는 건만 |
| `IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 요청일 |

### 6.2 UNION ALL 구분 조건

| 쿼리 | 조건 | 설명 |
|------|------|------|
| 1번 (해외매입) | `WR.CONTRACT_TYPE <> '40'` | BL 기준 입고 |
| 2번 (국내매입) | `WR.CONTRACT_TYPE = '40'` | 오퍼 기준 입고 |

### 6.3 앱에서 추가되는 WHERE

```java
// 기본 조건
String data = " WHERE GI_REQ_DATE = '" + Common.selectDay + "'";

// 창고별 조건 (이마트)
if(Common.selectWarehouse.equals("삼일냉장")){
    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
}else if(Common.selectWarehouse.equals("SWC")){
    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
}else if(Common.selectWarehouse.equals("이천1센터")){
    data += " AND GR_WAREHOUSE_CODE = '4001'";
}else if(Common.selectWarehouse.equals("부산센터")){
    data += " AND GR_WAREHOUSE_CODE = '4004'";
}else if(Common.selectWarehouse.equals("탑로지스")){
    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
}
```

---

## 7. 특수 컬럼 로직

### 7.1 BARCODE_TYPE 결정 로직

```sql
DECODE(CENTER_SCALE_USE_YN, 'Y',
    DECODE(BI.ITEM_TYPE, '10',
        DECODE(BI.MAJOR_CATEGORY, '10', 'M9', EO.BARCODE_TYPE),
        EO.BARCODE_TYPE),
    EO.BARCODE_TYPE)
```

| 조건 | 결과 |
|------|------|
| CENTER_SCALE_USE_YN = 'Y' AND ITEM_TYPE = '10' AND MAJOR_CATEGORY = '10' | 'M9' |
| 그 외 | EO.BARCODE_TYPE (B_EMART_BARCODE 테이블 값) |

### 7.2 CLIENTNAME 결정 로직

```sql
DECODE (
    SUBSTR (EO.CENTERNAME, 1, 2),
    'CJ',    DE_CLIENT2(IH.CLIENT_CODE) || '(' || EO.STORECODE || ')',
    DE_CLIENT(IH.CLIENT_CODE)
)
```

| 조건 | 결과 |
|------|------|
| 센터명이 'CJ'로 시작 | 업체명2(점포코드) 형식 |
| 그 외 | 업체명 |

### 7.3 EMART_PLANT_CODE 결정 로직

```sql
DECODE(EO.STORECODE, '9820', BSI.EMART_PLANT_CODE, NULL)
```

- 점포코드가 '9820'일 때만 공장코드 사용

### 7.4 킬코이 제품 특수 처리

- **조건**: PACKER_CODE = '30228' AND STORE_CODE = '9231'
- **내용**: 킬코이 제품이면서 이마트 미트센터 납품 시 소비기한 별도 처리

### 7.5 센터 유형별 처리

- **TRD/WET/E/T 센터**: CENTERNAME에 해당 문자열 포함 시 별도 처리
- **롯데 (searchType "6")**: 별도 라벨 출력 로직

---

## 8. 데이터 흐름

```
[서버 VIEW]
    ↓ HTTP 요청 (ProgressDlgShipSearch)
    ↓ 응답 형식: row1;;row2;;row3 (행 구분: ;;)
    ↓ 컬럼 형식: col1::col2::col3 (열 구분: ::)
[파싱]
    ↓ Shipments_Info 객체 생성
[로컬 DB 저장]
    ↓ DBHandler.insertqueryShipment()
[화면 표시]
    ↓ BixolonShipmentActivity에서 조회/표시
[계근 처리]
    ↓ 바코드 스캔 → 중량 입력 → 라벨 출력
[서버 전송]
    ↓ 계근 완료 데이터 전송
```

---

## 9. searchType별 사용 컬럼 차이

| searchType | 용도 | temp[] 범위 | 특이 컬럼 |
|:----------:|------|:-----------:|-----------|
| "0" | 이마트 출하대상 | 0~37 (38개) | WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE |
| "1" | 생산대상 | 0~31 (32개) | - |
| "2" | 홈플러스 하이퍼 | 0~31 (32개) | - |
| "3" | 도매업체 | 0~31 (32개) | - |
| "4" | 비정량 출하 | 0~37 (38개) | WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE |
| "5" | 홈플러스 비정량 | 0~36 (37개) | WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE |
| "6" | 롯데 | 0~33 (34개) | WH_AREA, LAST_BOX_ORDER |
| "7" | 생산(라벨) | 0~31 (32개) | - |

---

## 10. 컬럼별 상세 사용처 (41개)

각 컬럼이 앱에서 어떻게 사용되는지 상세 정리입니다.

### 10.1 출고 식별자 그룹 (Index 0~2)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 0 | GI_H_ID | 미사용 | 출고헤더 FK. 로컬DB 저장만 됨. 서버 전송 패킷에 미포함 |
| 1 | GI_D_ID | 서버전송, 로컬DB | 출고상세 PK. 계근건 고유 식별자로 로컬DB 저장 및 서버 업로드 시 사용 |
| 2 | EOI_ID | 미사용 | 이마트 주문 FK. 로컬DB 저장만 됨. 서버 전송 패킷에 미포함 |

### 10.2 상품 정보 그룹 (Index 3~6)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 3 | ITEM_CODE | 서버전송 | 하이랜드 상품코드. 패킷에 포함되나 JSP에서 미사용 (W_GOODS_WET에 저장 안됨) |
| 4 | ITEM_NAME | 화면표시 | 하이랜드 상품명. 계근 리스트 화면에서 상품명으로 표시 |
| 5 | EMARTITEM_CODE | 바코드생성 | 이마트 상품코드. 바코드 생성 시 앞 6자리로 사용 (substring(0,6)) |
| 6 | EMARTITEM | 라벨출력 | 이마트 상품명. 라벨에 인쇄되는 상품명 |

### 10.3 수량/금액 그룹 (Index 7~9)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 7 | GI_REQ_PKG | 화면표시, 로직분기 | 출하요청수량. 진행률 표시용 (완료수량/요청수량) |
| 8 | GI_REQ_QTY | 화면표시 | 출하요청중량. 진행률 표시용 (완료중량/요청중량) |
| 9 | AMOUNT | 미사용 | 출하상품금액. 서버에서 수신하여 로컬DB에 저장만 함. 실제 사용 안함 |

### 10.4 입고 정보 그룹 (Index 10~13)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 10 | GOODS_R_ID | 미사용 | 입고ID. 서버에서 수신하여 로컬DB에 저장만 함. 실제 사용 안함 |
| 11 | GR_REF_NO | 미사용 | 창고입고번호. 서버에서 수신하여 로컬DB에 저장만 함. 실제 사용 안함 |
| 12 | GI_REQ_DATE | 미사용 | 출하요청일. WHERE 조건은 Common.selectDay 사용, VIEW 값은 로컬DB 저장만 |
| 13 | BL_NO | 화면표시, 중복방지 | BL번호/수입식별번호. 동일 BL 중복 스캔 방지 체크용 |

### 10.5 브랜드/거래처 그룹 (Index 14~17)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 14 | BRAND_CODE | 서버전송 | 브랜드코드. 서버 전송용 |
| 15 | BRANDNAME | 미사용 | 브랜드명. 로컬DB 저장만, 실제 사용 안함 |
| 16 | CLIENT_CODE | 로직분기 | 출고업체코드. selectqueryGoodsWet() DB 조회 파라미터 |
| 17 | CLIENTNAME | 화면표시, 라벨출력 | 출고업체명/점포명. 계근 리스트에서 납품처로 표시, 라벨에 인쇄 |

### 10.6 센터/원산지 그룹 (Index 18~20)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 18 | CENTERNAME | 라벨출력, 로직분기 | 센터명. 라벨에 인쇄, "TRD"/"WET"/"E"/"T" 포함 여부로 센터 유형 판별 |
| 19 | ITEM_SPEC | 라벨출력 | 상품규격. 라벨에 상품명과 함께 인쇄 |
| 20 | CT_CODE | 라벨출력 | 원산지코드. 라벨에 인쇄 (BixolonShipmentActivity:2827) |

### 10.7 패커 정보 그룹 (Index 21~24)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 21 | PACKER_CODE | 로직분기 | 패커코드. "30228"(킬코이) 판별하여 소비기한 특수 처리 |
| 22 | IMPORT_ID_NO | 바코드생성, 라벨출력 | 수입식별번호. M3 바코드 타입 시 바코드에 포함, 라벨에도 인쇄 |
| 23 | PACKERNAME | 미사용 | 패커명. 로컬DB 저장만, 실제 사용 안함 |
| 24 | PACKER_PRODUCT_CODE | 화면표시, 서버전송 | 패커상품코드. 화면에서 상품 식별용으로 표시 및 서버 전송 |

### 10.8 바코드/계근 분기 그룹 (Index 25~28)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 25 | BARCODE_TYPE | 로직분기 | 바코드타입. M0/M3/M4/M9별로 다른 바코드 생성 로직 분기 |
| 26 | ITEM_TYPE | 로직분기 | 아이템타입. W(원료육)/J(제품)/B(비정량)/S(세트)별 계근 방식 분기 |
| 27 | PACKWEIGHT | 제품계근 | 포장중량. ITEM_TYPE="J"(제품)일 때 고정중량으로 사용 |
| 28 | BARCODEGOODS | 아이템검색 | 바코드상품코드. 바코드 스캔 시 상품 매칭 검색용 |

### 10.9 입고일/물류코드 그룹 (Index 29~32)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 29 | STORE_IN_DATE | 라벨출력 | 입고/납품일자. 라벨에 "YYYY년 MM월 DD일" 형식으로 출력 |
| 30 | GR_WAREHOUSE_CODE | WHERE조건 | 창고코드. 앱에 전달 안됨. WHERE 조건(삼일냉장/SWC 등)으로만 사용 |
| 31 | EMARTLOGIS_CODE | 바코드생성, 라벨출력, 로직분기 | 이마트물류코드. 바코드 생성, 라벨에 지점코드로 인쇄, 로직 분기 |
| 32 | EMARTLOGIS_NAME | 미사용 | 이마트물류명. 로컬DB 저장만, 실제 사용 안함 |

### 10.10 라벨 출력 그룹 (Index 33~36)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 33 | WH_AREA | 라벨출력 | 창고구역. 라벨에 배송구역 정보로 인쇄 |
| 34 | USE_NAME | 라벨출력 | 용도명. 원료육 라벨에 용도(원료/가공/소분 등) 인쇄 |
| 35 | USE_CODE | 바코드생성 | 용도코드. 원료육 바코드 생성 시 용도코드 포함 |
| 36 | CT_NAME | 라벨출력 | 원산지명. 라벨에 "호주산", "미국산" 등 인쇄 |

### 10.11 점포/공장 그룹 (Index 37~38)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 37 | STORE_CODE | 로직분기, 바코드생성, 라벨출력 | 점포코드. "9231"(이마트미트센터) 판별, 바코드 생성, 라벨에 인쇄 |
| 38 | EMART_PLANT_CODE | 바코드생성, 로직분기 | 이마트공장코드. 바코드 생성, STORE_CODE="9820" 조건 분기 |

### 10.12 앱 미전달 그룹 (Index 39~40)

| Index | 컬럼명 | 사용처 | 상세 설명 |
|:-----:|--------|--------|-----------|
| 39 | MAJOR_CATEGORY | VIEW내부 | 대분류(축종). BARCODE_TYPE 결정 시 사용 (앱 미전달) |
| 40 | CONTAINER_TYPE | JSP조건 | 컨테이너타입(냉장/냉동). JSP에서 필터링용 (앱 미전달) |

### 10.13 코드 사용 예시

**화면표시 (BixolonShipmentActivity.java)**
```java
// 계근 리스트에 표시
tv_itemname.setText(info.getITEM_NAME());      // Index 4
tv_clientname.setText(info.getCLIENTNAME());   // Index 17
tv_packer.setText(info.getPACKER_PRODUCT_CODE()); // Index 24
```

**바코드생성 (BixolonShipmentActivity.java)**
```java
// M0 바코드: EMARTITEM_CODE 앞 6자리 사용
String itemCode = info.getEMARTITEM_CODE().substring(0, 6);  // Index 5
String logisCode = info.getEMARTLOGIS_CODE();                // Index 31
```

**로직분기 (BixolonShipmentActivity.java)**
```java
// ITEM_TYPE별 계근 방식 분기
if (info.getITEM_TYPE().equals("W")) {
    // 원료육: 실제 중량 입력
} else if (info.getITEM_TYPE().equals("J")) {
    // 제품: PACKWEIGHT 고정중량 사용
    weight = info.getPACKWEIGHT();  // Index 27
}

// BARCODE_TYPE별 바코드 생성 분기
switch (info.getBARCODE_TYPE()) {  // Index 25
    case "M0": // 기본
    case "M3": // 수입식별번호 포함
    case "M4": // 원산지명 포함
    case "M9": // 납품일자
}

// 킬코이 특수 처리
if (info.getPACKER_CODE().equals("30228") &&
    info.getSTORE_CODE().equals("9231")) {
    // 소비기한 별도 처리
}
```

**서버전송 (UploadWeighingResult)**
```java
// 서버 전송 시 필수 필드
params.put("GI_H_ID", info.getGI_H_ID());       // Index 0
params.put("GI_D_ID", info.getGI_D_ID());       // Index 1
params.put("EOI_ID", info.getEOI_ID());         // Index 2
params.put("ITEM_CODE", info.getITEM_CODE());   // Index 3
// ... 15개 컬럼 전송
```

---

## 11. 41개 컬럼 상세 설명 (언제/어디서/어떻게)

각 컬럼이 **언제**(When), **어디서**(Where), **어떻게**(How) 사용되는지 상세 설명입니다.

### 11.1 Index 0~10

| Index | 컬럼명 | 언제 | 어디서 | 어떻게 |
|:-----:|--------|------|--------|--------|
| 0 | GI_H_ID | 출하대상 수신 시 | ProgressDlgShipSearch.java, DBHandler.java | 출고헤더ID (W_GOODS_IH). 서버 수신 → 로컬DB 저장만. 패킷 미포함 |
| 1 | GI_D_ID | 계근 완료 후 서버 전송 시 | BixolonShipmentActivity.java:3729 | `packet += si.getGI_D_ID()` → W_GOODS_WET.GI_D_ID 저장 |
| 2 | EOI_ID | 출하대상 수신 시 | ProgressDlgShipSearch.java, DBHandler.java | `temp[2]`→`setEOI_ID()`→SQLite 저장. 패킷 미포함 |
| 3 | ITEM_CODE | 계근 완료 후 서버 전송 시 | BixolonShipmentActivity.java:3739 | `packet += si.getITEM_CODE()` 전송. JSP에서 미사용 |
| 4 | ITEM_NAME | 계근 리스트 화면 표시 시 | BixolonShipmentActivity.java:1924, 4215 | `edit_product_name.setText(si.getITEM_NAME())` |
| 5 | EMARTITEM_CODE | 바코드 생성 시 | BixolonShipmentActivity.java:2228 | `si.getEMARTITEM_CODE().substring(0,6)` → 바코드 앞 6자리 |
| 6 | EMARTITEM | 라벨 인쇄 시 | BixolonShipmentActivity.java:2002 | `slcsText(..., si.EMARTITEM + "/" + si.ITEM_SPEC)` |
| 7 | GI_REQ_PKG | 화면 표시 및 완료 체크 시 | BixolonShipmentActivity.java:1581, 1199 | 진행률: `getGI_REQ_PKG() + "/" + getPACKING_QTY()` |
| 8 | GI_REQ_QTY | 화면 진행률 표시 시 | BixolonShipmentActivity.java | GI_REQ_PKG와 함께 진행률 표시 |
| 9 | AMOUNT | 사용 안함 | DBHandler.java | SQLite 저장만. 비즈니스 로직에서 미사용 |
| 10 | GOODS_R_ID | 사용 안함 | DBHandler.java | SQLite 저장만. 비즈니스 로직에서 미사용 |

### 11.2 Index 11~20

| Index | 컬럼명         | 언제              | 어디서                                     | 어떻게                                                     |
| :---: | ----------- | --------------- | --------------------------------------- | ------------------------------------------------------- |
|  11   | GR_REF_NO   | 사용 안함           | DBHandler.java                          | SQLite 저장만. 비즈니스 로직에서 미사용                               |
|  12   | GI_REQ_DATE | 출하대상 조회 시       | ProgressDlgShipSearch.java              | `WHERE GI_REQ_DATE = 'yyyyMMdd'` 조건으로 사용                |
|  13   | BL_NO       | 바코드 스캔 시 중복 방지  | BixolonShipmentActivity.java:3577       | `list_bl.get(j).equals(arSM.get(i).getBL_NO())` 중복 체크   |
|  14   | BRAND_CODE  | 계근 완료 후 서버 전송 시 | BixolonShipmentActivity.java:3740       | `packet += si.getBRAND_CODE()` 전송. JSP에서 미사용            |
|  15   | BRANDNAME   | 사용 안함           | DBHandler.java                          | SQLite 저장만. 비즈니스 로직에서 미사용                               |
|  16   | CLIENT_CODE | DB 조회 조건으로 사용 시 | BixolonShipmentActivity.java            | `selectqueryGoodsWet(CLIENT_CODE, ...)` 파라미터            |
|  17   | CLIENTNAME  | 화면 표시 및 라벨 출력 시 | BixolonShipmentActivity.java:2413, 2784 | `pointName = si.CLIENTNAME`, `slcsText(..., pointName)` |
|  18   | CENTERNAME  | 센터 유형 분기 판별 시   | BixolonShipmentActivity.java:645, 2118  | `if(CENTERNAME.contains("TRD"))` 등 센터 유형 판별             |
|  19   | ITEM_SPEC   | 라벨 출력 시         | BixolonShipmentActivity.java:2002       | `slcsText(..., si.EMARTITEM + "/" + si.ITEM_SPEC)`      |
|  20   | CT_CODE     | 라벨 출력 시         | BixolonShipmentActivity.java:2827       | `slcsText(..., si.getCT_CODE())` 원산지코드 출력               |

### 11.3 Index 21~30

| Index | 컬럼명 | 언제 | 어디서 | 어떻게 |
|:-----:|--------|------|--------|--------|
| 21 | PACKER_CODE | 킬코이 제품 판별 시 | BixolonShipmentActivity.java:1169, 2079 | `if(PACKER_CODE.equals("30228"))` 소비기한 특수처리 |
| 22 | IMPORT_ID_NO | M3 바코드 생성 시 | BixolonShipmentActivity.java:2228 | `pBarcode = ... + si.getIMPORT_ID_NO()` |
| 23 | PACKERNAME | 사용 안함 | DBHandler.java | SQLite 저장만. 비즈니스 로직에서 미사용 |
| 24 | PACKER_PRODUCT_CODE | 서버 전송 시 | BixolonShipmentActivity.java:3732 | `packet += si.getPACKER_PRODUCT_CODE()` |
| 25 | BARCODE_TYPE | 바코드 생성 로직 분기 시 | BixolonShipmentActivity.java:2216 | `switch(si.getBARCODE_TYPE()) { case "M0": ... }` |
| 26 | ITEM_TYPE | 계근 방식 분기 시 | BixolonShipmentActivity.java:1250, 2183 | `if(ITEM_TYPE.equals("W"))` 원료육/`"J"` 제품 |
| 27 | PACKWEIGHT | 제품(J) 계근 시 | BixolonShipmentActivity.java:1356, 2192 | `print_weight_str = si.getPACKWEIGHT()` |
| 28 | BARCODEGOODS | 바코드 스캔으로 아이템 검색 시 | BixolonShipmentActivity.java:3389 | `find_work_info(arSM.get(0).getBARCODEGOODS())` |
| 29 | STORE_IN_DATE | 라벨 납품일자 출력 시 | BixolonShipmentActivity.java:2564, 2572 | `si.getSTORE_IN_DATE().substring(...)` 형식화 출력 |
| 30 | GR_WAREHOUSE_CODE | VIEW WHERE 조건 (앱 미전달) | VW_PDA_WID_LIST SQL | `AND GR_WAREHOUSE_CODE = 'IN10273'` 창고 필터 |

### 11.4 Index 31~40

| Index | 컬럼명 | 언제 | 어디서 | 어떻게 |
|:-----:|--------|------|--------|--------|
| 31 | EMARTLOGIS_CODE | 바코드 생성 및 로직 분기 시 | BixolonShipmentActivity.java:2231, 2618 | `pBarcode2 = si.getEMARTLOGIS_CODE().substring(0,6)` |
| 32 | EMARTLOGIS_NAME | 사용 안함 | DBHandler.java | SQLite 저장만. 실제 미사용 |
| 33 | WH_AREA | 라벨 창고구역 출력 시 | BixolonShipmentActivity.java:2599, 2656 | `whArea = si.getWH_AREA()` |
| 34 | USE_NAME | 라벨 용도명 출력 시 | BixolonShipmentActivity.java:2370, 2380 | `pBarcodeStr3 = si.EMARTITEM + "," + si.getUSE_NAME()` |
| 35 | USE_CODE | 원료육 바코드 생성 시 | BixolonShipmentActivity.java:2376 | `pBarcode2 = ... + si.getUSE_CODE()` |
| 36 | CT_NAME | 라벨 원산지명 출력 시 | BixolonShipmentActivity.java:2553 | `String ctName = si.getCT_NAME()` |
| 37 | STORE_CODE | 미트센터 분기 및 바코드 생성 시 | BixolonShipmentActivity.java:1169, 2201 | `if(STORE_CODE.equals("9231"))`, `sBarcode = si.getSTORE_CODE()` |
| 38 | EMART_PLANT_CODE | 미트센터 바코드 생성 시 | BixolonShipmentActivity.java:2618, 2638 | `meatCenterBarcode = ... + si.getEMART_PLANT_CODE()` |
| 39 | MAJOR_CATEGORY | VIEW 내부 BARCODE_TYPE 결정 시 (앱 미전달) | VW_PDA_WID_LIST SQL | `DECODE(MAJOR_CATEGORY, '10', 'M9', ...)` 축종 판별 |
| 40 | CONTAINER_TYPE | JSP 필터링 조건 (앱 미전달) | VW_PDA_WID_LIST SQL | 냉장/냉동 구분용. 앱에 전달되지 않음 |

### 11.5 컬럼 사용 패턴 요약

| 패턴 | 해당 컬럼 Index | 설명 |
|------|:-------------:|------|
| 서버 전송 | 1, 3, 14, 24 | `packet += si.get컬럼()` → JSP 전송 |
| 화면 표시 | 4, 7, 8, 13, 17, 24 | `setText(si.get컬럼())` → UI 표시 |
| 바코드 생성 | 5, 22, 31, 35, 37, 38 | 바코드 문자열 조합에 사용 |
| 라벨 출력 | 6, 17, 18, 19, 20, 22, 29, 31, 33, 34, 36, 37 | `slcsText()` 라벨 인쇄 |
| 로직 분기 | 7, 16, 18, 21, 25, 26, 31, 37, 38 | `if/switch` 조건 판별 |
| 아이템 검색 | 28 | `find_work_info()` 상품 매칭 |
| 중복 방지 | 13 | `list_bl.contains()` 중복 체크 |
| 조회 조건 | 12, 30 | WHERE 절 파라미터 |
| 미사용 | 0, 2, 9, 10, 11, 15, 23, 32 | SQLite 저장만, 로직 미사용 |
| 앱 미전달 | 30, 39, 40 | VIEW에만 존재, 앱에 전송 안됨 |

### 11.6 BARCODEGOODS 서브쿼리 상세 분석

#### 11.6.1 서브쿼리 정의 (VW_PDA_WID_LIST Line 101-107)

```sql
(SELECT BARCODEGOODS
   FROM S_BARCODE_INFO A
  WHERE A.PACKER_CLIENT_CODE = BD.PACKER_CODE
    AND A.PACKER_PRODUCT_CODE = BD.PACKER_PRODUCT_CODE
    AND A.status = 'Y'
    AND ROWNUM < 2) BARCODEGOODS
```

| 항목 | 값 |
|------|------|
| 원본 테이블 | **S_BARCODE_INFO** |
| 조인 조건 1 | PACKER_CLIENT_CODE = BD.PACKER_CODE |
| 조인 조건 2 | PACKER_PRODUCT_CODE = BD.PACKER_PRODUCT_CODE |
| 필터 | status = 'Y' |
| 제한 | ROWNUM < 2 (첫 번째 행만) |

#### 11.6.2 VIEW별 BARCODEGOODS 정의

| VIEW | 라인 | 정의 방식 |
|------|:----:|----------|
| VW_PDA_WID_LIST | 101-107 | S_BARCODE_INFO 서브쿼리 |
| VW_PDA_WID_LIST_LOTTE | 83-89 | S_BARCODE_INFO 서브쿼리 |
| VW_PDA_WID_HOMEPLUS_LIST | 94-100 | S_BARCODE_INFO 서브쿼리 |
| VW_PDA_WID_WHOLESALE_LIST | 85-91 | S_BARCODE_INFO 서브쿼리 |
| VW_PDA_WID_LIST_NONFIXED | 110 | 직접 정의 |
| VW_PDA_WID_LIST_NONFIXED_HP | 35 | ID.ITEM_CODE 직접 사용 |

#### 11.6.3 사용 시점

| 사용 시점 | 메서드/위치 | 설명 |
|----------|------------|------|
| **바코드 스캔** | find_work_info() Line 1696 | 스캔 바코드에서 상품코드 추출 → DB 매칭 |
| **BL 검색** | onPostExecute() Line 3389 | type=false 시 첫 번째 항목 BARCODEGOODS로 검색 |
| **출하대상 수신** | VW_PDA_WID_LIST 서브쿼리 | 서버에서 BARCODEGOODS 조회하여 전달 |
| **로컬 DB 저장** | DBHandler.insertShipmentDB() | 출하 정보에 BARCODEGOODS 저장 |

#### 11.6.4 selectqueryBarcodeInfo 쿼리 (DBHandler.java Line 859-942)

바코드 스캔 시 상품 매칭에 사용되는 로컬 DB 조회 쿼리.

**테이블**: TABLE_NAME_BARCODE_INFO (SQLite 로컬 DB)

**쿼리**:
```sql
SELECT BARCODE_INFO_ID, PACKER_CLIENT_CODE, BRAND_CODE, PACKER_PRODUCT_CODE,
       PACKER_PRD_NAME, ITEM_CODE, ITEM_NAME_KR, BARCODEGOODS, BASEUNIT,
       ZEROPOINT, PACKER_PRD_CODE_FROM, PACKER_PRD_CODE_TO, BARCODEGOODS_FROM,
       BARCODEGOODS_TO, WEIGHT_FROM, WEIGHT_TO, MAKINGDATE_FROM, MAKINGDATE_TO,
       BOXSERIAL_FROM, BOXSERIAL_TO, STATUS, REG_ID, REG_DATE, REG_TIME, MEMO, SHELF_LIFE
FROM TABLE_NAME_BARCODE_INFO
WHERE BARCODEGOODS_TO != ''
```

**주요 컬럼**:

| 컬럼 | 용도 |
|------|------|
| BARCODEGOODS | 바코드 상품코드 (매칭 대상) |
| BARCODEGOODS_FROM | 스캔 바코드에서 상품코드 추출 시작 위치 |
| BARCODEGOODS_TO | 스캔 바코드에서 상품코드 추출 끝 위치 |
| PACKER_PRODUCT_CODE | 패커 상품코드 (매칭 성공 시 표시) |
| ITEM_NAME_KR | 상품명 (매칭 성공 시 표시) |
| WEIGHT_FROM / WEIGHT_TO | 바코드에서 중량 추출 위치 |
| MAKINGDATE_FROM / MAKINGDATE_TO | 바코드에서 제조일자 추출 위치 |
| BOXSERIAL_FROM / BOXSERIAL_TO | 바코드에서 박스번호 추출 위치 |

**데이터 흐름**:
```
[서버] S_BARCODE_INFO 테이블
          ↓ (앱 시작 시 동기화)
[로컬 SQLite] TABLE_NAME_BARCODE_INFO
          ↓ (selectqueryBarcodeInfo 호출)
[메모리] ArrayList<Barcodes_Info>
          ↓ (바코드 스캔 시 매칭)
[화면] 상품명, 패커상품코드 표시
```

#### 11.6.5 바코드 스캔 → 상품 매칭 로직

```java
// BixolonShipmentActivity.java Line 1700-1732
ArrayList<Barcodes_Info> list_barcode_info = DBHandler.selectqueryBarcodeInfo(this);

for (Barcodes_Info bi : list_barcode_info) {
    String bg = bi.getBARCODEGOODS();           // DB 바코드 상품코드
    String bg_from = bi.getBARCODEGOODS_FROM(); // 추출 시작 위치
    String bg_to = bi.getBARCODEGOODS_TO();     // 추출 끝 위치

    // 스캔 바코드에서 상품코드 추출
    temp_bg = req.substring(bg_from - 1, bg_to);

    // 매칭 확인
    if (temp_bg.equals(bg)) {
        work_item_bi_info = bi;
        edit_product_name.setText(bi.getITEM_NAME_KR());
        edit_product_code.setText(bi.getPACKER_PRODUCT_CODE());
    }
}
```

**매칭 성공/실패 처리 (Line 1720-1737)**:

```java
if (temp_bg.equals(bg)) {                       // 매칭 성공
    Log.i(TAG, "barcodegoods find success");
    work_item_bi_info = bi;                     // 바코드 정보 객체 저장
    edit_product_name.setText(bi.getITEM_NAME_KR());    // 상품명 표시
    edit_product_code.setText(bi.getPACKER_PRODUCT_CODE()); // 패커코드 표시
    pp_code = bi.getPACKER_PRODUCT_CODE();      // 패커코드 변수 저장
    work_item_barcodegoods = bg;                // 바코드상품코드 저장
    count++;                                    // 매칭 카운트 증가
} else {                                        // 매칭 실패
    edit_product_name.setText("");              // 상품명 필드 초기화
    edit_product_code.setText("");              // 패커코드 필드 초기화
    work_item_barcodegoods = "";                // 바코드상품코드 초기화
}
```

| 항목 | 매칭 성공 | 매칭 실패 |
|------|----------|----------|
| work_item_bi_info | Barcodes_Info 객체 저장 | 변경 없음 |
| edit_product_name | 상품명(ITEM_NAME_KR) 표시 | 빈 문자열 |
| edit_product_code | 패커상품코드 표시 | 빈 문자열 |
| pp_code | PACKER_PRODUCT_CODE 저장 | 변경 없음 |
| work_item_barcodegoods | 바코드상품코드 저장 | 빈 문자열 |
| **결과** | **계근 작업 진행 가능** | **계근 작업 불가** |

**매칭 실패 시 후속 처리**:
- `pp_code`가 빈 문자열이면 출하 대상 목록에서 해당 상품을 찾을 수 없음
- 계근 입력 시 상품 정보가 비어있어 진행 불가
- 사용자는 수동으로 상품을 선택해야 함

#### 11.6.6 매칭 프로세스 흐름

```
[바코드 스캔: 01088012345678900125260113001]
                    ↓
[BARCODEGOODS_FROM=4, BARCODEGOODS_TO=16]
                    ↓
[추출: substring(3, 16) → "8801234567890"]
                    ↓
[DB BARCODEGOODS와 비교]
                    ↓
[일치 → 상품 정보 표시]
```

#### 11.6.7 관련 테이블 (S_BARCODE_INFO)

| 컬럼 | 설명 |
|------|------|
| BARCODEGOODS | 바코드 상품코드 (13자리) |
| BARCODEGOODS_FROM | 추출 시작 위치 |
| BARCODEGOODS_TO | 추출 끝 위치 |
| PACKER_CLIENT_CODE | 패커 코드 (조인 조건) |
| PACKER_PRODUCT_CODE | 패커 상품코드 (조인 조건) |
| STATUS | 활성 상태 ('Y') |

---

## 12. 미사용 컬럼 (12개)

### 12.1 로컬DB 저장만 (9개)

서버에서 수신하여 로컬 SQLite에 저장되지만, 비즈니스 로직에서 사용되지 않음.

| Index | 컬럼명 | 원본 테이블 | 비고 |
|:-----:|--------|-------------|------|
| 0 | GI_H_ID | W_GOODS_IH (출고헤더) | getter 미호출, 패킷 미포함 |
| 2 | EOI_ID | W_GOODS_ID (출고상세) | getter 미호출, 패킷 미포함 |
| 9 | AMOUNT | W_GOODS_ID (출고상세) | getter 미호출 |
| 10 | GOODS_R_ID | W_GOODS_R (입고) | getter 미호출 |
| 11 | GR_REF_NO | W_GOODS_R (입고) | getter 미호출 |
| 12 | GI_REQ_DATE | W_GOODS_IH (출고헤더) | WHERE 조건은 Common.selectDay 사용 |
| 15 | BRANDNAME | 서브쿼리 DE_COMMON() | getter 미호출 |
| 23 | PACKERNAME | 서브쿼리 DE_CLIENT() | getter 미호출 |
| 32 | EMARTLOGIS_NAME | B_EMART_BARCODE | getter 미호출 (주석 로그만 존재) |

### 12.2 앱 미전달 (3개)

VIEW에서 조회되지만 앱에 전달되지 않음.

| Index | 컬럼명 | 용도 |
|:-----:|--------|------|
| 30 | GR_WAREHOUSE_CODE | WHERE 조건으로만 사용 (Common.selectWarehouse) |
| 39 | MAJOR_CATEGORY | VIEW 내부 BARCODE_TYPE 결정용 |
| 40 | CONTAINER_TYPE | JSP 필터링용 |

### 12.3 실제 사용 컬럼 (29개)

| Index | 컬럼명 | 용도 |
|:-----:|--------|------|
| 1 | GI_D_ID | 서버전송 (PK, W_GOODS_WET 연결) |
| 3 | ITEM_CODE | 서버전송 (packet[10]) |
| 4 | ITEM_NAME | 화면표시 (상품명) |
| 5 | EMARTITEM_CODE | 바코드생성 (앞 6자리) |
| 6 | EMARTITEM | 라벨출력 (이마트상품명) |
| 7 | GI_REQ_PKG | 화면표시, 로직분기 (진행률, 완료체크) |
| 8 | GI_REQ_QTY | 화면표시 (진행률) |
| 13 | BL_NO | 화면표시, 중복스캔방지 |
| 14 | BRAND_CODE | 서버전송 (packet[11]) |
| 16 | CLIENT_CODE | 로직분기 (DB조회조건) |
| 17 | CLIENTNAME | 화면표시, 라벨출력 (점포명) |
| 18 | CENTERNAME | 라벨출력, 로직분기 (TRD/WET/E/T) |
| 19 | ITEM_SPEC | 라벨출력 (상품규격) |
| 20 | CT_CODE | 라벨출력 (원산지코드) |
| 21 | PACKER_CODE | 로직분기 (킬코이 30228) |
| 22 | IMPORT_ID_NO | 바코드생성, 라벨출력 (수입식별번호) |
| 24 | PACKER_PRODUCT_CODE | 화면표시, 서버전송 |
| 25 | BARCODE_TYPE | 로직분기 (M0/M3/M4/M9 등) |
| 26 | ITEM_TYPE | 로직분기 (W/S/J/B 계근방식) |
| 27 | PACKWEIGHT | 제품(J) 계근 (고정중량) |
| 28 | BARCODEGOODS | 아이템검색 (바코드 스캔 매칭) |
| 29 | STORE_IN_DATE | 라벨출력 (납품일자) |
| 31 | EMARTLOGIS_CODE | 바코드생성, 라벨출력, 로직분기 |
| 33 | WH_AREA | 라벨출력 (창고구역) |
| 34 | USE_NAME | 라벨출력 (용도명) |
| 35 | USE_CODE | 바코드생성 (용도코드) |
| 36 | CT_NAME | 라벨출력 (원산지명) |
| 37 | STORE_CODE | 로직분기, 바코드생성, 라벨출력 (점포코드) |
| 38 | EMART_PLANT_CODE | 바코드생성, 로직분기 (이마트공장코드) |

### 12.4 요약

| 분류 | 개수 |
|------|:----:|
| 로컬DB 저장만 | 9 |
| 앱 미전달 | 3 |
| **총 미사용** | **12** |
| **실제 사용** | **29** |

---

## 13. 요약

| 항목 | 내용 |
|------|------|
| 총 컬럼 수 | 41개 |
| 실제 사용 컬럼 | 29개 |
| 미사용 컬럼 | 12개 |
| 앱 전달 컬럼 | 38개 |
| 앱 미전달 컬럼 | 3개 |
| JOIN 테이블 | 7개 + EO 서브쿼리 |
| UNION ALL | 2개 (해외매입 + 국내매입) |

---

## 14. 변경 이력

| 날짜 | 내용 |
|------|------|
| 2025-12-05 | 최초 작성 |
| 2026-01-13 | 실제 VIEW SQL 기준 재분석, 문서 통합 |
| 2026-01-13 | VIEW↔JSP 컬럼 순서 변환 섹션 추가 (섹션 4), 오류 수정 |
| 2026-01-13 | 섹션 3.1 컬럼 용도 추가 |
| 2026-01-13 | 섹션 3.2 용도별 컬럼 분류 추가, 기존 섹션 6 삭제 |
| 2026-01-13 | 섹션 3.3~3.4 테이블별 컬럼 분류 추가 |
| 2026-01-13 | 섹션 3.2 용도별 분류를 통합 체크표로 변경 |
| 2026-01-13 | 섹션 10 컬럼별 상세 사용처 추가 (41개 컬럼 그룹별 정리) |
| 2026-01-13 | AMOUNT, GOODS_R_ID, GR_REF_NO 용도 수정: 서버전송→미사용 (별도 분류) |
| 2026-01-14 | 서버전송 컬럼 재검증 - 실제 패킷 포함 기준으로 분류 수정 |
| 2026-01-14 | GI_H_ID, EOI_ID → 미사용 (패킷 미포함), STORE_IN_DATE → 라벨출력, EMARTLOGIS_NAME → 로직분기 |
| 2026-01-14 | EMARTITEM_CODE, EMARTITEM 서버전송 제거 (바코드생성, 라벨출력만 유지) |
| 2026-01-14 | CLIENTNAME 라벨출력 추가 (pointName으로 라벨에 점포명 출력) |
| 2026-01-14 | 섹션 3.0 용도 분류 기준 추가 (서버전송/화면표시/바코드생성/라벨출력/로직분기/기타/미사용/앱미전달) |
| 2026-01-14 | 기준 기반 재검증: GI_REQ_PKG 로직분기 추가, BL_NO 화면표시 추가, EMARTLOGIS_CODE 로직분기 추가 |
| 2026-01-14 | 섹션 3.2.0 체크표에 앱미전달 컬럼 추가 (8개 분류 기준 일치화) |
| 2026-01-14 | 서버전송 데이터 흐름 추가: PDA → insert_goods_wet.jsp → W_GOODS_WET |
| 2026-01-14 | 섹션 11 추가: 41개 컬럼 상세 설명 (언제/어디서/어떻게) |
| 2026-01-14 | 섹션 11 라인번호 검증 및 수정 (실제 소스 기준) |
| 2026-01-14 | 섹션 3.3.5 EO 서브쿼리를 원본 테이블별로 분리 (EOI, EB, BCC) |
| 2026-01-15 | 섹션 11.6 BARCODEGOODS 서브쿼리 상세 분석 추가 (S_BARCODE_INFO, 사용시점, 매칭로직) |
| 2026-01-15 | 섹션 11.6.5 매칭 성공/실패 처리 로직 상세 추가 (work_item_bi_info, pp_code, 후속 처리) |
| 2026-01-15 | 섹션 12 미사용 컬럼 추가 (13개: 로컬DB저장만 8개, 앱미전달 3개, JSP미사용 2개) |
| 2026-01-15 | 섹션 12 재검증 - 미사용 12개, 사용 29개로 수정 (GI_REQ_DATE→미사용, ITEM_CODE/BRAND_CODE→사용) |
