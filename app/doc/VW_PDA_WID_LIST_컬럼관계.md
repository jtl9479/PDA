# VW_PDA_WID_LIST 컬럼 관계 가이드

**작성일**: 2026-01-14
**소스 기준**: VW_PDA_WID_LIST VIEW SQL

---

## 1. 주요 컬럼 출처 테이블

### 1.1 테이블 별칭 정리

| 별칭 | 테이블명 | 설명 |
|------|----------|------|
| IH | W_GOODS_IH | 출고헤더 |
| ID | W_GOODS_ID | 출고상세 |
| WR | W_GOODS_R | 입고 |
| EOI | W_EMART_ORDER_ITEM | 이마트주문아이템 |
| EB | B_EMART_BARCODE | 이마트바코드 |
| BCC | B_COMMON_CODE | 공통코드 (센터) |
| BCC2 | B_COMMON_CODE | 공통코드 (점포) |
| BSI | B_SUPPLIER_ITEM | 공급사품목 |
| BI | B_ITEM | 품목마스터 |
| BD | I_BL_D | BL상세 |
| OD | I_OFFER_D | 오퍼상세 |

### 1.2 EO 서브쿼리

EO는 별도 테이블이 아닌 **서브쿼리 별칭**입니다.

```sql
INNER JOIN (SELECT EOI.EOI_ID,
                   EOI.STORE_NAME STORENAME,
                   EOI.STORE_CODE STORECODE,
                   BCC.CODE_NAME CENTERNAME,
                   ...
            FROM W_EMART_ORDER_ITEM EOI
                 INNER JOIN B_EMART_BARCODE EB ...
                 INNER JOIN B_COMMON_CODE BCC ...
           ) EO
```

---

## 2. CLIENTNAME / STORE_CODE / CENTERNAME 관계

### 2.1 컬럼 정의 (VIEW SQL 근거)

| 컬럼 | VIEW 표현 | 원본 테이블 | VIEW 주석 |
|------|-----------|-------------|-----------|
| CLIENTNAME | DE_CLIENT(IH.CLIENT_CODE) | W_GOODS_IH | "출고업체명" (Line 88) |
| STORE_CODE | EO.STORECODE | W_EMART_ORDER_ITEM | 점포코드 |
| CENTERNAME | BCC.CODE_NAME | W_EMART_ORDER_ITEM | "센터명" (Line 90) |

### 2.2 CLIENTNAME 상세 (VIEW Line 79-88)

```sql
IH.CLIENT_CODE                              -- 출고업체코드 (Line 79)

DECODE (
   SUBSTR (EO.CENTERNAME, 1, 2),
   'CJ',    DE_CLIENT2 (IH.CLIENT_CODE) || '(' || EO.STORECODE || ')',
           DE_CLIENT (IH.CLIENT_CODE))
AS CLIENTNAME                               -- 출고업체명 (Line 88)

FROM W_GOODS_IH IH                          -- (Line 119, 271)
```

**중요**: CLIENTNAME은 **I_BL_D(BL상세)나 I_OFFER_D(오퍼상세)에서 오지 않습니다.**

### 2.3 데이터 출처 비교

```
W_GOODS_IH (출고헤더)
└── CLIENT_CODE → DE_CLIENT() → CLIENTNAME (출고업체명)

W_EMART_ORDER_ITEM (이마트주문아이템)
├── CENTER_CODE → CENTERNAME (센터명)
└── STORE_CODE → STORE_CODE (점포코드)
```

### 2.4 직납/센터납 판별 (VIEW Line 145)

```sql
DECODE(EOI.STORE_CODE, EOI.CENTER_CODE, '직납', '센터납') deliverType
```

| 조건 | deliverType | 의미 |
|------|-------------|------|
| STORE_CODE = CENTER_CODE | 직납 | 센터가 곧 납품처 |
| STORE_CODE ≠ CENTER_CODE | 센터납 | 센터 경유 후 납품 |

### 2.5 3컬럼 관계 예제

#### 예제 1: 직납
```
CLIENTNAME:  "하이랜드무역"     ← 출고업체 (하이랜드가 출고)
STORE_CODE:  "1234"            ← 납품처 점포
CENTERNAME:  "용인센터"         ← 센터명 (= 납품처)

→ 하이랜드무역이 용인센터(1234)로 직접 납품
```

#### 예제 2: 센터납
```
CLIENTNAME:  "하이랜드무역"     ← 출고업체
STORE_CODE:  "5678"            ← 최종 점포 (이마트 강남점)
CENTERNAME:  "용인센터"         ← 경유 센터

→ 하이랜드무역 → 용인센터 → 이마트 강남점(5678)
```

#### 예제 3: 미트센터
```
CLIENTNAME:  "하이랜드무역"
STORE_CODE:  "9231"            ← 미트센터 전용 코드
CENTERNAME:  "이마트미트센터"

→ 하이랜드무역이 미트센터로 납품 (특수 바코드 처리)
```

#### 컬럼 의미 요약

| 컬럼 | 의미 | 질문 |
|------|------|------|
| CLIENTNAME | 출고업체명 | "누가 보내는가?" |
| STORE_CODE | 점포코드 | "어디로 가는가?" |
| CENTERNAME | 센터명 | "어느 센터 경유/담당?" |

### 2.7 VIEW 구조 참고

VIEW는 UNION ALL로 2개 파트 구성:

| 파트 | 설명 | JOIN 테이블 |
|------|------|-------------|
| 1 (Line 45-186) | 해외 매입 (BL 입고) | W_GOODS_IH + I_BL_D |
| 2 (Line 189-337) | 국내 매입 (오퍼 입고) | W_GOODS_IH + I_OFFER_D |

**양쪽 파트 모두 CLIENTNAME은 W_GOODS_IH.CLIENT_CODE에서 가져옴**

---

## 3. 상품코드 관련 컬럼

### 3.1 EMARTITEM_CODE vs EMARTLOGIS_CODE

| 컬럼 | 원본 테이블 | 용도 | 바코드 |
|------|-------------|------|--------|
| EMARTITEM_CODE | W_EMART_ORDER_ITEM.ITEM_CODE | 주문 상품코드 | pBarcode |
| EMARTLOGIS_CODE | B_EMART_BARCODE.EMARTLOGIS_CODE | 물류 상품코드 | pBarcode2 |

### 3.2 바코드 생성 시 사용

```java
// pBarcode: 주문 상품코드 기반 (점포 스캔용)
pBarcode = EMARTITEM_CODE.substring(0,6) + 중량 + 회사코드 + ...

// pBarcode2: 물류 상품코드 기반 (물류센터용)
pBarcode2 = EMARTLOGIS_CODE.substring(0,6) + 중량 + 회사코드 + ...
```

### 3.3 라벨 출력

| 바코드 타입 | pBarcode | pBarcode2 |
|------------|:--------:|:---------:|
| M0, M1, M8, E0~E3, P0 | ✅ 출력 | 생성만 |
| **M3, M4, M9, L0** | ✅ 출력 | ✅ 출력 |

---

## 4. 패커/공급사 관련 컬럼

### 4.1 PACKER_CODE vs PACKER_PRODUCT_CODE

| 컬럼 | 원본 테이블 | 설명 |
|------|-------------|------|
| PACKER_CODE | I_BL_D / I_OFFER_D | 패커(공급사) 코드 |
| PACKER_PRODUCT_CODE | I_BL_D / I_OFFER_D | 패커 상품코드 |
| PACKERNAME | DE_CLIENT(PACKER_CODE) | 패커명 (함수 변환) |

### 4.2 서버 전송

```java
// insert_goods_wet.jsp로 전송되는 데이터
packet = GI_D_ID + "::" + WEIGHT + "::" + WEIGHT_UNIT + "::"
       + PACKER_PRODUCT_CODE + "::" + BARCODE + "::" + ...
```

---

## 5. 미트센터 관련 컬럼

### 5.1 미트센터 판별

| 컬럼 | 값 | 의미 |
|------|-----|------|
| STORE_CODE | "9231" | 이마트 미트센터 |
| EMARTLOGIS_CODE | "0000000" | 물류코드 기본값 |
| EMART_PLANT_CODE | 값 있음/없음 | 가공장코드 유무 |

### 5.2 미트센터 바코드 분기

```java
// Line 2618: PLANT_CODE 있는 경우
if (BARCODE_TYPE == "M0"
    && STORE_CODE == "9231"
    && EMARTLOGIS_CODE == "0000000"
    && EMART_PLANT_CODE != "") {
    // meatCenterBarcode에 EMART_PLANT_CODE 포함
}

// Line 2671: PLANT_CODE 없는 경우
if (BARCODE_TYPE == "M0"
    && STORE_CODE == "9231"
    && EMARTLOGIS_CODE != "0000000"
    && EMART_PLANT_CODE == "") {
    // meatCenterBarcode에 EMART_PLANT_CODE 미포함
}
```

### 5.3 EMART_PLANT_CODE 상세

#### 5.3.1 컬럼 정의

| 항목 | 내용 |
|------|------|
| 컬럼명 | EMART_PLANT_CODE |
| 한글명 | 이마트 가공장코드 |
| 원본 테이블 | B_SUPPLIER_ITEM (BSI) |
| 데이터 타입 | VARCHAR |
| 용도 | 미트센터 납품 시 바코드에 가공장코드 포함 여부 결정 |

#### 5.3.2 VIEW SQL 조회 로직

```sql
-- VIEW Line 116, 268 (UNION ALL 양쪽 파트 동일)
DECODE(EO.STORECODE, '9820', BSI.EMART_PLANT_CODE, NULL) AS EMART_PLANT_CODE

-- JOIN 조건 - 파트1 해외매입 (Line 127-129)
LEFT OUTER JOIN B_SUPPLIER_ITEM BSI
    ON BD.PACKER_CODE = BSI.PACKER_CODE
   AND BD.PACKER_PRODUCT_CODE = BSI.PACKER_PRODUCT_CODE

-- JOIN 조건 - 파트2 국내매입 (Line 278-280)
LEFT OUTER JOIN B_SUPPLIER_ITEM BSI
    ON OD.PACKER_CODE = BSI.PACKER_CODE
   AND OD.PACKER_PRODUCT_CODE = BSI.PACKER_PRODUCT_CODE
```

| 조건                  | 결과                      | 설명               |
| ------------------- | ----------------------- | ---------------- |
| STORE_CODE = '9820' | BSI.EMART_PLANT_CODE 반환 | 미트센터 납품 시만 조회    |
| 그 외                 | NULL                    | 일반 납품은 가공장코드 불필요 |

**참고**: VIEW에서는 '9820'으로 조건 분기하지만, Java에서는 '9231'로 미트센터를 판별합니다.

#### 5.3.3 Java 사용 위치

| 파일 | 라인 | 용도 |
|------|------|------|
| BixolonShipmentActivity.java | 2618 | 가공장코드 있는 경우 분기 |
| BixolonShipmentActivity.java | 2638-2639 | 바코드에 가공장코드 포함 |
| BixolonShipmentActivity.java | 2671 | 가공장코드 없는 경우 분기 |
| DBInfo.java | 61 | 컬럼명 상수 정의 |
| Shipments_Info.java | 49 | DTO 필드 정의 |
| ProgressDlgShipSearch.java | 309 | 서버 응답 파싱 |

#### 5.3.4 미트센터 바코드 생성 분기

**Case 1: 가공장코드 있음 (Line 2618)**
```java
if (si.getBARCODE_TYPE().equals("M0")
    && si.getSTORE_CODE().equals("9231")           // 미트센터
    && si.getEMARTLOGIS_CODE().equals("0000000")   // 물류코드 기본값
    && !si.getEMART_PLANT_CODE().equals("")) {     // 가공장코드 있음 ★

    // 바코드 구성: 물류코드(6) + 중량(6) + 회사코드(2) + 수입식별번호(12) + 가공장코드
    meatCenterBarcode = EMARTLOGIS_CODE.substring(0,6)
                      + print_weight_str
                      + meatCenterCode
                      + IMPORT_ID_NO
                      + EMART_PLANT_CODE;  // ★ 가공장코드 포함
}
```

**Case 2: 가공장코드 없음 (Line 2671)**
```java
if (si.getBARCODE_TYPE().equals("M0")
    && si.getSTORE_CODE().equals("9231")           // 미트센터
    && !si.getEMARTLOGIS_CODE().equals("0000000")  // 물류코드 있음
    && si.getEMART_PLANT_CODE().equals("")) {      // 가공장코드 없음 ★

    // 바코드 구성: 물류코드(6) + 중량(6) + 회사코드(2) + 수입식별번호(12)
    meatCenterBarcode = EMARTLOGIS_CODE.substring(0,6)
                      + print_weight_str
                      + meatCenterCode
                      + IMPORT_ID_NO;  // 가공장코드 미포함
}
```

#### 5.3.5 바코드 구성 비교

| 구성요소 | Case 1 (가공장코드 有) | Case 2 (가공장코드 無) |
|----------|:--------------------:|:--------------------:|
| EMARTLOGIS_CODE (6자리) | ✅ | ✅ |
| 중량 (6자리) | ✅ | ✅ |
| 회사코드 (2자리) | ✅ | ✅ |
| 수입식별번호 (12자리) | ✅ | ✅ |
| **EMART_PLANT_CODE** | ✅ | ❌ |
| **총 길이** | **26 + α** | **26** |

#### 5.3.6 라벨 출력 예시

**Case 1: 가공장코드 포함 라벨**
```
┌─────────────────────────────────────┐
│ ERP-미트센터출하코드                   │
│ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │
│ 000000 001250 99 123456789012 610933 │
│ (물류)  (중량) (회사) (수입식별번호) (가공장) │
└─────────────────────────────────────┘
```

**Case 2: 가공장코드 미포함 라벨**
```
┌─────────────────────────────────────┐
│ 미트센터출하코드                       │
│ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │
│ 123456 001250 99 123456789012        │
│ (물류)  (중량) (회사) (수입식별번호)      │
└─────────────────────────────────────┘
```

#### 5.3.7 데이터 흐름

```
┌─────────────────┐
│ B_SUPPLIER_ITEM │ ← 원본 테이블 (가공장코드 저장)
│ EMART_PLANT_CODE│
└────────┬────────┘
         │ JOIN ON ITEM_CODE, SUPPLIER_CODE
         ▼
┌─────────────────┐
│ VW_PDA_WID_LIST │ ← VIEW (STORE_CODE='9820'일 때만 조회)
│ EMART_PLANT_CODE│
└────────┬────────┘
         │ search_shipment.jsp
         ▼
┌─────────────────┐
│ PDA 앱 (Java)   │ ← Shipments_Info DTO에 저장
│ EMART_PLANT_CODE│
└────────┬────────┘
         │ setPrinting() → 미트센터 분기
         ▼
┌─────────────────┐
│ 라벨 바코드      │ ← 가공장코드 유무에 따라 바코드 길이 결정
│ meatCenterBarcode│
└─────────────────┘
```

#### 5.3.8 관련 상수 (BixolonShipmentActivity.java)

```java
// Line 173-176
private static final String MEAT_CENTER_CODE = "059015";         // 미트센터 업체코드
private static final String MEAT_CENTER_STORE_CODE = "9231";     // 미트센터 지점코드
private static final String KILKOY_PACKER_CODE = "30228";        // 킬코이 패커코드
private static final String LOGIS_CODE_DEFAULT = "0000000";      // 물류코드 기본값
```

---

## 6. 센터 유형 판별 (CENTERNAME)

### 6.1 센터 유형 상수

```java
CENTER_NAME_TRD = "TRD"   // 트레이더스
CENTER_NAME_WET = "WET"   // WET 센터
CENTER_NAME_ET  = "E/T"   // E/T 센터
```

### 6.2 수입육 센터 판별

```java
if (CENTERNAME.contains("TRD")
    || CENTERNAME.contains("WET")
    || CENTERNAME.contains("E/T")) {
    // 수입육 센터 → 소비기한 입력 필요
}
```

---

## 7. 컬럼별 원본 테이블 요약

| 컬럼 | 원본 테이블 | VIEW 주석/비고 |
|------|-------------|----------------|
| GI_H_ID | W_GOODS_IH | 출고헤더ID |
| GI_D_ID | W_GOODS_ID | 출고상세ID |
| GI_REQ_PKG | W_GOODS_ID | "출하요청수량" (Line 61) |
| GI_REQ_DATE | W_GOODS_IH | "출하요청일" (Line 71), WHERE 조회조건 (Line 184) |
| CLIENT_CODE | W_GOODS_IH | "출고업체코드" (Line 79) |
| CLIENTNAME | W_GOODS_IH.CLIENT_CODE에서 결정,<br>DE_CLIENT()/DE_CLIENT2() 함수로 업체명 변환 | "출고업체명" (Line 88) |
| CENTERNAME | W_EMART_ORDER_ITEM.CENTER_CODE에서 결정,<br>B_COMMON_CODE는 코드→이름 변환용 참조 테이블 (CODE_NAME) | "센터명" (Line 90) |
| STORE_CODE | W_EMART_ORDER_ITEM | 점포코드 |
| EMARTITEM_CODE | W_EMART_ORDER_ITEM.ITEM_CODE | 이마트 상품코드 |
| EMARTITEM | W_EMART_ORDER_ITEM.ITEM_NAME | "이마트상품명" (Line 59) |
| EMARTLOGIS_CODE | B_EMART_BARCODE | 물류 상품코드 |
| BARCODE_TYPE | B_EMART_BARCODE | 바코드 타입 |
| PACKER_CODE | I_BL_D / I_OFFER_D | "패커" (Line 94) |
| PACKER_PRODUCT_CODE | I_BL_D / I_OFFER_D | "패커 상품코드" (Line 97) |
| IMPORT_ID_NO | W_GOODS_R | "수입식별번호" (Line 95) |
| ITEM_SPEC | W_GOODS_R | "스펙" (Line 91) |
| CT_CODE | W_GOODS_R | "원산지" (Line 92) |
| STORE_IN_DATE | W_EMART_ORDER_ITEM | 납품일자 |
| USE_CODE | B_EMART_BARCODE | 용도코드 |
| EMART_PLANT_CODE | B_SUPPLIER_ITEM | 가공장코드 (STORE_CODE='9820'일 때만) |
| WH_AREA | W_EMART_ORDER_ITEM.STORE_CODE에서 결정,<br>B_COMMON_CODE는 코드→창고구역 변환용 참조 테이블 (REF_CODE2) | 창고구역 |
| USE_NAME | B_EMART_BARCODE.USE_CODE에서 결정,<br>B_COMMON_CODE는 코드→용도명 변환용 참조 테이블 (CODE_NAME) | 용도명 |
| CT_NAME | W_GOODS_R.CT_CODE에서 결정,<br>B_COMMON_CODE는 코드→원산지명 변환용 참조 테이블 (CODE_NAME + '산') | 원산지명 |

---

## 8. WH_AREA (창고구역)

### 8.1 데이터 조회 방식

```sql
-- Line 162-165
INNER JOIN B_COMMON_CODE BCC2
   ON BCC2.MASTER_CODE = 'EMART_STORE_CODE'
  AND EOI.STORE_CODE = BCC2.CODE

-- Line 144
BCC2.REF_CODE2 WH_AREA
```

### 8.2 조회 흐름

1. W_EMART_ORDER_ITEM.STORE_CODE 값으로
2. B_COMMON_CODE 테이블 조인 (MASTER_CODE = 'EMART_STORE_CODE')
3. B_COMMON_CODE.REF_CODE2 값을 WH_AREA로 반환

### 8.3 원본 테이블

| 항목 | 테이블 |
|------|--------|
| 키 (STORE_CODE) | W_EMART_ORDER_ITEM |
| 값 (REF_CODE2) | B_COMMON_CODE |

**결론**: 키 데이터가 W_EMART_ORDER_ITEM에서 결정됨

---

## 9. BL_NO vs IMPORT_ID_NO 비교

### 9.1 기본 정보 비교

| 항목 | BL_NO | IMPORT_ID_NO |
|------|-------|--------------|
| 한글명 | BL번호 (선하증권번호) | 수입식별번호 (이력번호) |
| 원본 테이블 | W_GOODS_R | W_GOODS_R |
| VIEW 주석 | "BL번호" (Line 74) | "수입식별번호" (Line 95) |
| 데이터 단위 | 선적/컨테이너 단위 (큰 단위) | 로트/개별 상품 단위 (작은 단위) |
| 발급 시점 | 선박 운송 시 | 검역/통관 시 |

### 9.2 VIEW SQL 조회 로직

```sql
-- BL_NO 조회 (Line 73-74)
DECODE(WR.IMPORT_ID_NO, NULL, WR.BL_NO, WR.IMPORT_ID_NO) BL_NO

-- IMPORT_ID_NO 조회 (Line 95)
WR.IMPORT_ID_NO    -- 수입식별번호
```

**BL_NO 조회 우선순위:**
| 조건 | VIEW의 BL_NO 컬럼 값 |
|------|---------------------|
| IMPORT_ID_NO 있음 | IMPORT_ID_NO 반환 |
| IMPORT_ID_NO 없음 (NULL) | WR.BL_NO 반환 |

**중요:**
- VIEW의 BL_NO 컬럼은 실제로 **"IMPORT_ID_NO 또는 BL_NO"** 값을 담고 있음
- 원본 WR.BL_NO는 IMPORT_ID_NO가 없을 때만 대체값으로 사용됨
- Java에서는 VIEW의 BL_NO 값을 그대로 받아서 중복 스캔 방지 등에 사용
- IMPORT_ID_NO는 별도 컬럼(Line 95)으로 조회하여 바코드 생성에 사용

### 9.3 Java 사용 위치 비교

#### BL_NO 사용 위치

| 파일                           | 라인         | 용도            |
| ---------------------------- | ---------- | ------------- |
| BixolonShipmentActivity.java | 1148       | 중복 스캔 방지      |
| BixolonShipmentActivity.java | 1687       | 바코드 스캔 검증     |
| BixolonShipmentActivity.java | 1915, 1920 | BL 목록 관리      |
| BixolonShipmentActivity.java | 3577-3599  | BL 스피너 선택     |
| ShipmentListAdapter.java     | 149-152    | 화면 표시 (뒤 4자리) |
| DBHandler.java               | 102        | 조회 조건         |

#### IMPORT_ID_NO 사용 위치

| 파일 | 라인 | 용도 |
|------|------|------|
| BixolonShipmentActivity.java | 2228-2232 | M0 바코드 생성 |
| BixolonShipmentActivity.java | 2261-2265 | M3 바코드 생성 |
| BixolonShipmentActivity.java | 2294-2298 | M4 바코드 생성 |
| BixolonShipmentActivity.java | 2325-2329 | E2 바코드 생성 |
| BixolonShipmentActivity.java | 2354-2358 | P0 바코드 생성 |
| BixolonShipmentActivity.java | 2373-2377 | M9 바코드 생성 |
| BixolonShipmentActivity.java | 2395-2399 | M8 바코드 생성 |
| BixolonShipmentActivity.java | 2638-2690 | 미트센터 바코드 생성 |
| BixolonShipmentActivity.java | 2831 | 라벨 중량/이력번호 표시 |
| BixolonShipmentActivity.java | 3001-3002 | 롯데 pBarcode2 생성 |
| BixolonShipmentActivity.java | 3102 | 라벨 "이력(묶음)번호" 출력 |

### 9.4 용도 비교

| 구분 | BL_NO | IMPORT_ID_NO |
|------|:-----:|:------------:|
| **바코드 생성** | ❌ | ✅ |
| **라벨 출력** | ❌ | ✅ |
| **중복 스캔 방지** | ✅ | ❌ |
| **BL 목록 관리** | ✅ | ❌ |
| **화면 표시** | ✅ (뒤 4자리) | ✅ (이력번호) |
| **외부 노출** | ❌ (앱 내부용) | ✅ (라벨 인쇄) |

### 9.5 바코드 구성 시 IMPORT_ID_NO 위치

```
pBarcode = 상품코드(6) + 중량(6) + 회사코드(2) + IMPORT_ID_NO(12)
           ├─────────────────────────────────┤   └──────────────┘
                      14자리                        12자리
```

| 바코드 타입 | IMPORT_ID_NO 포함 | 비고 |
|------------|:-----------------:|------|
| M0 | ✅ | 기본형 |
| M1 | ❌ | 수입식별번호 없음 |
| M3 | ✅ | PC매입 |
| M4 | ❌ | 원산지명 포함 |
| M8 | ✅ | 비정량 (순서 다름) |
| M9 | ✅ | 우육 센터납 |
| E0 | ✅ | 에브리데이 기본 |
| E1 | ❌ | 고정값 "111111111111" 사용 |
| E2 | ✅ | 간소화 |
| E3 | ❌ | 상품코드만 |
| P0 | ✅ | 생산투입 |
| L0 | ✅ | 롯데 (pBarcode2에 단독) |

### 9.6 중복 가능성 및 실제 로직 분석

#### 9.6.1 중복 가능성

| 컬럼 | 단위 | 중복 가능성 |
|------|------|------------|
| BL_NO (원본) | 선적 단위 (가장 큼) | **매우 높음** |
| IMPORT_ID_NO | 로트 단위 (중간) | **있음** |
| GI_D_ID | 출고상세 단위 (가장 작음) | **없음** |

**참고:** VIEW의 BL_NO 컬럼에는 IMPORT_ID_NO가 우선 들어가므로, 실제로는 IMPORT_ID_NO 중복 가능성이 적용됨

#### 9.6.2 실제 소스 로직

**BixolonShipmentActivity.java (Line 1146-1162):**
```java
String temp_bl_no = sp_bl_no.getItemAtPosition(sp_bl_no.getSelectedItemPosition()).toString();
for (int i = 0; i < arSM.size(); i++) {
    if (temp_bl_no.equals(arSM.get(i).getBL_NO())
        && !arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))) {
        current_work_position = i;
        work_bl_no = temp_bl_no;
        break;  // 첫 번째 매칭 후 종료
    } else {
        work_bl_no = "";
        current_work_position = -1;
    }
}
```

**ShipmentActivity.java (Line 1051-1067):** 동일한 로직

#### 9.6.3 로직 분석

| 조건 | 설명 |
|------|------|
| `temp_bl_no.equals(getBL_NO())` | 스피너 선택 BL_NO와 일치 |
| `GI_REQ_PKG ≠ PACKING_QTY` | 요청수량 ≠ 포장수량 (미완료) |
| `break` | 첫 번째 매칭 후 종료 |

#### 9.6.4 문제점

| 문제 | 설명 |
|------|------|
| GI_D_ID 미사용 | 출고상세ID로 구분하지 않음 |
| 첫 번째만 선택 | 동일 BL_NO 중 첫 번째 미완료 건만 선택 |
| 중복 출고 건 | 동일 IMPORT_ID_NO로 여러 출고 시 구분 불가 |

**예시:**
- 동일 IMPORT_ID_NO의 원료 100kg 입고
- 상품 A로 50kg 출고 (GI_D_ID: 001)
- 상품 B로 50kg 출고 (GI_D_ID: 002)
- 스피너에서 해당 BL_NO 선택 시 → **항상 첫 번째(001)만 선택됨**

#### 9.6.5 개선 방안 (참고)

정확한 작업 선택을 위해서는 GI_D_ID를 추가 조건으로 사용해야 함:
```java
if (temp_bl_no.equals(arSM.get(i).getBL_NO())
    && temp_gi_d_id.equals(arSM.get(i).getGI_D_ID())  // GI_D_ID 추가
    && !arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))) {
```

### 9.7 요약

| 구분 | BL_NO | IMPORT_ID_NO |
|------|-------|--------------|
| **주 용도** | 앱 내부 관리 | 바코드/라벨 출력 |
| **식별 단위** | 선적 단위 | 로트 단위 |
| **중복 가능성** | 높음 | 낮음 |
| **외부 노출** | 없음 | 라벨에 인쇄 |
| **이력 추적** | 불가 | 가능 |

---

## 10. PACKWEIGHT (포장중량)

### 10.1 기본 정보

| 항목 | 내용 |
|------|------|
| 컬럼명 | PACKWEIGHT |
| 한글명 | 포장중량 (팩 중량) |
| 데이터 타입 | VARCHAR (TEXT) |
| 용도 | ITEM_TYPE="J"(제품)일 때 지정 중량으로 사용 |

### 10.2 VIEW별 원본 테이블

| VIEW | 원본 | 조회 방식 |
|------|------|----------|
| VW_PDA_WID_LIST (이마트) | B_EMART_BARCODE | `EB.PACKWEIGHT` |
| VW_PDA_WID_HOMEPLUS_LIST (홈플러스) | B_EMART_BARCODE | `EB.PACKWEIGHT` |
| VW_PDA_WID_LIST_NONFIXED (이마트 비정량) | B_EMART_BARCODE | `EB.PACKWEIGHT` |
| VW_PDA_WID_LIST_LOTTE (롯데) | - | `NULL AS PACKWEIGHT` |
| VW_PDA_WID_LIST_NONFIXED_HP (홈플러스 비정량) | - | `NULL AS PACKWEIGHT` |
| VW_PDA_WID_WHOLESALE_LIST (도매) | - | `'NA' AS PACKWEIGHT` |
| VW_PDA_WID_PRO_LIST (생산투입) | B_ITEM (I) | `I.PACK_WEIGHT` |

**결론:** 마트사별로 원본 테이블이 다름
- 이마트/홈플러스: **B_EMART_BARCODE**
- 롯데/도매/홈플러스 비정량: **NULL 또는 'NA'** (미사용)
- 생산투입: **B_ITEM**

### 10.3 Java 사용 위치

#### 10.3.1 상수/필드 정의

| 파일 | 라인 | 내용 |
|------|------|------|
| DBInfo.java | 53 | `PACKWEIGHT = "PACKWEIGHT"` (컬럼명 상수) |
| Shipments_Info.java | 33 | `public String PACKWEIGHT = ""` (DTO 필드) |
| Shipments_Info.java | 303-308 | getter/setter |

#### 10.3.2 계근 입력 시 (ITEM_TYPE="J")

| 파일 | 라인 | 코드 |
|------|------|------|
| BixolonShipmentActivity.java | 1356 | `item_weight = getPACKWEIGHT()` |
| ShipmentActivity.java | 1261 | `item_weight = getPACKWEIGHT()` |

#### 10.3.3 라벨 출력 시 (ITEM_TYPE="J")

| 파일 | 라인 | 코드 |
|------|------|------|
| BixolonShipmentActivity.java | 2192 | `print_weight_str = si.getPACKWEIGHT()` |
| BixolonShipmentActivity.java | 2925 | `print_weight_str = si.getPACKWEIGHT()` (롯데) |
| ShipmentActivity.java | 2106 | `print_weight_str = si.getPACKWEIGHT()` |
| ShipmentActivity.java | 2958 | `print_weight_str = si.getPACKWEIGHT()` (롯데) |

#### 10.3.4 서버 응답 파싱

| 파일 | 라인 | 코드 |
|------|------|------|
| ProgressDlgShipSearch.java | 295 | `si.setPACKWEIGHT(temp[27].toString())` |

### 10.4 ITEM_TYPE별 중량 처리 비교

| ITEM_TYPE | 설명 | 중량 처리 | PACKWEIGHT 사용 |
|-----------|------|----------|:---------------:|
| W | 원료육 | 바코드에서 추출 | ❌ |
| HW | 홈플러스 원료육 | 바코드에서 추출 | ❌ |
| S | 세트 | 바코드에서 추출 | ❌ |
| **J** | **제품** | **지정 중량** | **✅** |
| B | 비정량 | 바코드에서 추출 | ❌ |

### 10.5 마트사별 PACKWEIGHT 사용 여부

| 마트사 | searchType | PACKWEIGHT | 비고 |
|--------|:----------:|:----------:|------|
| 이마트 | 0 | ✅ | B_EMART_BARCODE |
| 홈플러스 | 2 | ✅ | B_EMART_BARCODE |
| 도매 | 3 | ❌ | 'NA' 고정값 |
| 이마트 비정량 | 4 | ✅ | B_EMART_BARCODE |
| 홈플러스 비정량 | 5 | ❌ | NULL |
| 롯데 | 6 | ❌ | NULL |
| 생산투입 | 7 | ✅ | B_ITEM.PACK_WEIGHT |

### 10.6 실제 코드 예시

**계근 입력 시 (BixolonShipmentActivity.java Line 1354-1361):**
```java
} else if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_J)) {
    // 이마트 ITEM_TYPE J (지정된 중량 입력) | 바코드에서 중량, 제조일, 박스시리얼 X
    item_weight = arSM.get(current_work_position).getPACKWEIGHT();
    Log.i(TAG, "Type J | 지정된 중량값 : " + item_weight);
    item_weight_double = Double.parseDouble(item_weight);
}
```

**라벨 출력 시 (BixolonShipmentActivity.java Line 2191-2196):**
```java
} else if (si.getITEM_TYPE().equals(ITEM_TYPE_J)) {
    print_weight_str = si.getPACKWEIGHT();
    print_weight_double = Double.parseDouble(print_weight_str);
}
```

### 10.7 요약

| 구분 | 내용 |
|------|------|
| **원본 테이블** | B_EMART_BARCODE (이마트/홈플러스), B_ITEM (생산투입) |
| **사용 조건** | ITEM_TYPE = "J" (제품) |
| **용도** | 계근 없이 지정된 포장중량 사용 |
| **미사용 마트** | 롯데, 도매, 홈플러스 비정량 (NULL/'NA') |

---

## 11. 관련 문서

| 문서 | 설명 |
|------|------|
| VW_PDA_WID_LIST.md | VIEW 컬럼 상세 문서 |
| 바코드_형식_가이드.md | 바코드 타입별 형식 |
| 테스트데이터_마트사별.md | 마트사별 테스트 바코드 |

---

**최종 수정일**: 2026-01-15 (PACKWEIGHT 섹션 추가)
