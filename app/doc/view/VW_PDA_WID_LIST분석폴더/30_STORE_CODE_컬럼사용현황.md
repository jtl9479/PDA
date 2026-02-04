# STORE_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 점포코드
**파싱 위치**: temp[28]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | ● |
| 라벨출력 | ● |
| 로직분기 | ● |
| DB저장 | ● |
| 바코드검증 | |
| 조회조건 | |
| 앱미전달 | |

> **구분 기준**
> - **서버전송**: 서버에 데이터 전송 시 패킷에 포함
> - **화면표시**: 앱 화면(Activity)에 텍스트로 표시
> - **바코드생성**: 바코드 문자열 생성에 사용
> - **라벨출력**: 라벨 인쇄 시 텍스트/값으로 출력
> - **로직분기**: if/switch 등 조건 분기에 사용
> - **DB저장**: 로컬 SQLite(TB_SHIPMENT)에 저장
> - **바코드검증**: 스캔 바코드와 매칭 검증
> - **조회조건**: 서버/로컬 DB 쿼리 WHERE 조건
> - **앱미전달**: 서버 VIEW에서만 사용, 앱에 전달 안 됨

---

## 1. 용도

| 용도 | 설명 |
|------|------|
| **미트센터 판별** | STORE_CODE == "9231" → 미트센터 납품분 |
| **라벨 출력 (M9)** | "지점명(점포코드)" 형식으로 표시 |
| **라벨 출력 (비정량)** | 점포코드 직접 표시 (큰 폰트) |
| **홈플러스 바코드** | sBarcode에 점포코드 사용 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 미트센터 판별, 라벨 출력 |
| ShipmentActivity.java | 미트센터 판별, 라벨 출력 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 점포코드 저장 |

---

## 4. 상수 정의

**파일**: BixolonShipmentActivity.java (Line 174)

```java
private static final String MEAT_CENTER_STORE_CODE = "9231";  // 미트센터 지점코드
```

---

## 5. 사용 코드

### 5.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 300, 309)

```java
si.setSTORE_CODE(temp[28].toString());       // 점포코드
```

### 5.2 미트센터 판별

**파일**: BixolonShipmentActivity.java (Line 626, 1169, 2079, 2618, 2671)

```java
// 미트센터 납품분 판별
if (arSM.get(current_work_position).getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)) {
    // STORE_CODE == "9231" → 미트센터
}

// 킬코이 + 미트센터 조합
if (si.getPACKER_CODE().equals(KILKOY_PACKER_CODE)
    && si.getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)) {
    // 킬코이 제품이면서 미트센터 납품분
    // 소비기한 변조 처리
}

// 미트센터 추가 라벨 출력 조건
if (si.getBARCODE_TYPE().equals(BARCODE_TYPE_M0)
    && si.getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)
    && si.getEMARTLOGIS_CODE().equals(LOGIS_CODE_DEFAULT)
    && !si.getEMART_PLANT_CODE().equals("")) {
    // 미트센터 + 공장코드 라벨 출력
}
```

### 5.3 M9 라벨 출력 - 지점명+점포코드

**파일**: BixolonShipmentActivity.java (Line 2451~2461)

```java
// M9 타입: "지점명(점포코드)" 형식
String storeNamePlusCode = pointName + "(" + si.getSTORE_CODE() + ")";

if (11 < si.CLIENTNAME.toString().length()) {
    slcsCmd.append(slcsText(10, 270, 35, 35, storeNamePlusCode.toString()));  // 긴 이름: 폰트 35
} else {
    slcsCmd.append(slcsText(10, 270, 40, 40, storeNamePlusCode.toString()));  // 짧은 이름: 폰트 40
}
```

### 5.4 비정량 라벨 출력 - 점포코드

**파일**: BixolonShipmentActivity.java (Line 2783, 2805~2812)

```java
storeCode = si.STORE_CODE.toString();

// ITEM_TYPE_B(비정량)이면 storeCode, 아니면 pointCode(EMARTLOGIS_CODE) 출력
if (si.getITEM_TYPE().equals(ITEM_TYPE_B)) {
    slcsCmd.append(slcsText(170, 135, 155, 155, storeCode.toString()));  // ★ 비정량: 점포코드
} else {
    slcsCmd.append(slcsText(170, 135, 155, 155, pointCode.toString()));  // 정량: 지점코드
}
```

### 5.5 홈플러스 바코드 생성

**파일**: BixolonShipmentActivity.java (Line 2201~2202)

```java
sBarcode = si.getSTORE_CODE();     // 홈플러스용 바코드
sBarcodeStr = si.getSTORE_CODE();  // 홈플러스용 바코드 문자열
```

---

## 6. STORE_CODE vs EMARTLOGIS_CODE

| 컬럼 | 의미 | 파싱 위치 | 용도 |
|------|------|:--------:|------|
| **STORE_CODE** | 점포코드 | temp[28] | 미트센터 판별, 비정량 라벨 |
| EMARTLOGIS_CODE | 지점코드 | temp[23] | 바코드 생성, 정량 라벨 |

### ITEM_TYPE에 따른 라벨 출력

| ITEM_TYPE | 라벨에 출력되는 코드 |
|:---------:|---------------------|
| **B** (비정량) | STORE_CODE (점포코드) |
| 그 외 | EMARTLOGIS_CODE (지점코드) |

---

## 7. 라벨 출력 위치

### M9 라벨

| 항목 | X 좌표 | Y 좌표 | 폰트 크기 | 예시 |
|------|:------:|:------:|:---------:|------|
| 지점명+점포코드 | 10 | 270 | 35~40 | "이마트 강남점(1234)" |

### 비정량 라벨

| 항목 | X 좌표 | Y 좌표 | 폰트 크기 |
|------|:------:|:------:|:---------:|
| 점포코드 | 170 | 135 | 155x155 |

---

## 8. 라벨 레이아웃 예시

### M9 라벨
```
┌─────────────────────────────────────┐
│  [하이랜드]                         │
│  |||||||||||||||||||| (바코드1)     │
│                                     │
│  이마트 강남점(1234)  [저울 스캔용] │  ← ★ 지점명(STORE_CODE)
│  |||||||||||||||||||| (바코드2)  국산│
│  바코드문자열                       │
│  육우,일반                          │
└─────────────────────────────────────┘
```

### 비정량 라벨 (ITEM_TYPE_B)
```
┌─────────────────────────────────────┐
│  지점명                             │
│  ┌──────┐                           │
│  │ 1234 │  ← ★ STORE_CODE (큰 폰트) │
│  └──────┘                           │
│  상품명                             │
│  |||||||||||||||||||| (바코드)      │
│  중량 / 납품일자                    │
└─────────────────────────────────────┘
```

---

## 9. VIEW 원천

| 항목 | 내용 |
|------|------|
| **VIEW 컬럼** | EO.STORECODE AS STORE_CODE |
| **원천 테이블** | W_EMART_ORDER_ITEM (별칭 EOI) |
| **원천 컬럼** | STORE_CODE |
| **역할** | 납품 점포 식별 |

### VIEW SQL 발췌

```sql
-- VW_PDA_WID_LIST (Line 94, 113)
EO.STORECODE AS STORE_CODE
-- W_EMART_ORDER_ITEM.STORE_CODE에서 가져옴

-- 직납/센터납 판별에도 사용
DECODE(EOI.STORE_CODE, EOI.CENTER_CODE, '직납', '센터납') deliverType
```

---

## 10. 특수 점포코드

| 점포코드 | 의미 | 처리 |
|:--------:|------|------|
| **9231** | 미트센터 | 특수 라벨 출력, 킬코이 소비기한 처리 |

---

## 11. 결론

**상태**: ✅ 필수 (삭제 불가)

STORE_CODE는 **납품 점포 식별의 핵심 컬럼**으로:
- **미트센터 판별**: STORE_CODE == "9231"
- **M9 라벨**: "지점명(점포코드)" 형식으로 출력
- **비정량(ITEM_TYPE_B) 라벨**: 점포코드 큰 폰트로 출력
- **홈플러스**: 바코드(sBarcode)에 사용
- EMARTLOGIS_CODE와 함께 납품처 정보 구성

---

**최종 수정일**: 2026-02-03
