# 공통 컬럼의 searchType별 사용 차이 분석

> ShipmentActivity.java 분석 기반, searchType별 공통 컬럼 처리 로직 차이

---

## searchType 값 정리

| searchType | 용도 | VIEW |
|------------|------|------|
| 0 | 이마트 출하 | VW_PDA_WID_LIST |
| 1 | 생산투입 계근 | VW_PDA_WID_PRO_LIST |
| 2 | 홈플러스 출하 | VW_PDA_WID_HOMEPLUS_LIST |
| 3 | 도매 출하 | VW_PDA_WID_WHOLESALE_LIST |
| 4 | 이마트 비정량 | VW_PDA_WID_LIST_NONFIXED |
| 5 | 홈플러스 비정량 | VW_PDA_WID_LIST_NONFIXED_HP |
| 6 | 롯데마트 | VW_PDA_WID_LIST_LOTTE |
| 7 | 생산 라벨출력 | (생산 VIEW 사용) |

---

## 1. 중량 처리 차이 (GI_REQ_QTY, 계근 중량)

### 소수점 자릿수 처리

| searchType | 처리 방식 | 코드 위치 |
|------------|----------|-----------|
| **0 (이마트)** | 소수점 1자리로 버림 (floor) | Line 560-566 |
| **6 (롯데)** | 소수점 2자리까지 유지 | Line 3137-3143 |
| **그 외** | 원본 그대로 유지 | Line 567-569 |

```java
// 이마트 (searchType=0)
weight_double = Math.floor(weight_double * 10) / 10.0;
temp_weight = String.format("%.1f", weight_double);

// 그 외 (생산/홈플러스 등)
temp_weight = Double.toString(weight_double);
```

### 계근 중량 합산

| searchType | 반올림 방식 |
|------------|-------------|
| **0 (이마트)** | `Math.round(x * 10.0) / 10.0` (소수점 1자리) |
| **그 외** | `Math.round(x * 1000) / 1000.0` (소수점 3자리) |

### LB→KG 환산

| searchType | 환산 후 처리 |
|------------|-------------|
| **0 (이마트)** | `Math.floor(x * item_pow) / item_pow` |
| **그 외** | `Math.floor(x * 100) / 100` (소수점 2자리) |

---

## 2. CENTERNAME 사용 차이

### 소비기한 입력 필요 조건

CENTERNAME에 "TRD", "WET", "E/T" 포함 시 소비기한 입력이 필요하나, **searchType에 따라 처리가 다름**:

| searchType | 소비기한 처리 |
|------------|--------------|
| **0 (이마트)** | ExpiryEnterActivity로 소비기한 수동 입력 |
| **6 (롯데)** | ExpiryEnterActivity로 소비기한 수동 입력 |
| **그 외** | 소비기한 입력 없이 wet_data_insert() 직접 호출 |

```java
// Line 603-627
if(arSM.get(current_work_position).getCENTERNAME().contains("TRD") ||
   arSM.get(current_work_position).getCENTERNAME().contains("WET") ||
   arSM.get(current_work_position).getCENTERNAME().contains("E/T") ||
   Common.searchType.equals("6")){

    if(Common.searchType.equals("0") || Common.searchType.equals("6")){
        // 소비기한 입력 창 띄움
        startActivityForResult(IntentA, GET_DATA_REQUEST);
    }else{
        // 소비기한 없이 바로 계근
        wet_data_insert(weight_str, weight_double, "", "");
    }
}
```

### 트레이더스 납품 검증 (Line 1177-1186)

| searchType | 검증 여부 |
|------------|----------|
| **0 (이마트)** | SHELF_LIFE, MAKINGDATE_FROM/TO 필수 검증 |
| **그 외** | 검증 생략 |

---

## 3. 바코드 중복 체크 차이

| searchType | 중복 체크 |
|------------|----------|
| **4 (이마트 비정량)** | 중복 허용 (dup = false 강제) |
| **5 (홈플러스 비정량)** | 중복 허용 (dup = false 강제) |
| **그 외** | 중복 체크 수행 |

```java
// Line 1217-1219
if (Common.searchType.equals("4") || Common.searchType.equals("5")) {
    dup = false;  // 비정량은 바코드 중복 허용
}
```

---

## 4. 프린터 관련 차이

### 프린터 활성화 여부

| searchType | 프린터 |
|------------|--------|
| **1 (생산)** | 비활성화 (블루투스 연결 안 함) |
| **그 외** | 활성화 |

```java
// Line 432, 454, 460
if(Common.searchType.equals("1")){
    // 프린터 버튼 비활성화
    btn_print.setEnabled(false);
}
```

### 라벨 출력 메서드 분기

| searchType | 출력 메서드 |
|------------|------------|
| **0 (이마트)** | setPrinting() |
| **2 (홈플러스)** | setHomeplusPrinting() |
| **4 (이마트 비정량)** | setPrinting() |
| **5 (홈플러스 비정량)** | setHomeplusPrinting() |
| **6 (롯데)** | setPrintingLotte() |
| **7 (생산 라벨)** | setPrinting_prod() |

---

## 5. DB INSERT 분기

| searchType | INSERT 메서드 | 특이사항 |
|------------|--------------|----------|
| **2 (홈플러스)** | insertqueryGoodsWetHomeplus() | BOX_ORDER 사용 |
| **6 (롯데)** | insertqueryGoodsWetLotte() | lotte_TryCount로 박스번호 관리 |
| **그 외** | insertqueryGoodsWet() | 기본 INSERT |

```java
// Line 1520-1536
if(Common.searchType.equals("2")) {
    DBHandler.insertqueryGoodsWetHomeplus(this, gi, maxBoxOrder);
} else if (Common.searchType.equals("6")) {
    DBHandler.insertqueryGoodsWetLotte(this, gi, lotte_TryCount);
    lotte_TryCount++;  // 다음 계근을 위해 증가
} else {
    DBHandler.insertqueryGoodsWet(this, gi);
}
```

---

## 6. 서버 전송 JSP 분기

| searchType | 호출 JSP |
|------------|----------|
| **0 (이마트)** | insert_goods_wet.jsp |
| **2 (홈플러스)** | insert_goods_wet_homeplus.jsp |
| **3 (도매)** | insert_goods_wet.jsp (HIGHLAND 스키마) |
| **6 (롯데)** | insert_goods_wet_ono.jsp |
| **1, 4, 5, 7 (생산/비정량)** | insert_goods_wet_new.jsp (INNO 스키마) |

```java
// Line 3870-3879
if(Common.searchType.equals("0")||Common.searchType.equals("3")) {
    sUrl = Common.SERVER_WET_INSERT;  // HIGHLAND 스키마
}else if(Common.searchType.equals("2")||Common.searchType.equals("6")){
    sUrl = Common.SERVER_WET_INSERT_HOMEPLUS;  // 홈플러스/롯데
}else if(Common.searchType.equals("1")){
    sUrl = Common.SERVER_WET_INSERT_NEW;  // 생산 (INNO)
}else if(Common.searchType.equals("7")){
    sUrl = Common.SERVER_WET_INSERT_NEW;  // 생산라벨 (INNO)
}
```

---

## 7. UI 레이아웃 차이

| searchType | 레이아웃 |
|------------|---------|
| **3 (도매)** | 도매 전용 레이아웃 (일부 필드 숨김) |
| **그 외** | 기본 레이아웃 |

```java
// Line 358
if(Common.searchType.equals("3")){
    // 도매용 레이아웃 설정
}
```

---

## 8. 재출력(Reprint) 처리

| searchType | 재출력 지원 |
|------------|------------|
| **2, 5 (홈플러스)** | setHomeplusPrinting() |
| **6 (롯데)** | setPrintingLotte() (BOX_ORDER 필요) |
| **7 (생산 라벨)** | setPrinting_prod() |
| **그 외** | setPrinting() |

---

## 9. 공통 컬럼별 searchType 차이 요약

| 컬럼명 | 차이 있음 | 차이 내용 |
|--------|:--------:|----------|
| **GI_REQ_QTY** | O | 이마트는 소수점 1자리, 그 외는 원본 유지 |
| **CENTERNAME** | O | TRD/WET/E/T 조건에서 이마트/롯데만 소비기한 입력 |
| **ITEM_TYPE** | X | 모든 searchType에서 동일하게 W/S/J/HW/B 분기 |
| **BARCODE_TYPE** | X | 모든 searchType에서 동일하게 바코드 형식 결정 |
| **GI_D_ID** | X | 모든 searchType에서 동일하게 키로 사용 |
| **PACKER_CODE** | X | 모든 searchType에서 동일하게 사용 |
| **EMARTITEM_CODE** | O | 롯데는 바코드에 포함, 이마트/홈플러스는 라벨에만 표시 |
| **PACKWEIGHT** | X | ITEM_TYPE=J일 때만 사용, searchType 무관 |
| **BL_NO** | X | 모든 searchType에서 동일하게 바코드 매칭에 사용 |
| **IMPORT_ID_NO** | O | 롯데 L0 바코드에서만 사용 |

---

## 10. 결론

### 공통 컬럼 중 searchType별로 처리가 다른 컬럼

1. **중량 관련**: GI_REQ_QTY 계산 시 소수점 자릿수 처리
2. **센터 관련**: CENTERNAME의 TRD/WET/E/T 조건이 이마트/롯데에서만 소비기한 입력 트리거
3. **바코드 관련**: EMARTITEM_CODE, IMPORT_ID_NO가 롯데 바코드에서 다르게 사용

### 공통 컬럼 중 모든 searchType에서 동일하게 사용되는 컬럼

- GI_D_ID, GI_H_ID, EOI_ID, ITEM_CODE, ITEM_NAME
- BARCODE_TYPE, ITEM_TYPE, PACKER_CODE, PACKER_PRODUCT_CODE
- BL_NO, BRAND_CODE, CLIENT_CODE, PACKWEIGHT

---

*작성일: 2024년*
*분석 대상: ShipmentActivity.java*
