# ITEM_TYPE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 아이템타입 (계근 방식)
**파싱 위치**: temp[19]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | |
| 라벨출력 | |
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
| **계근 방식 분기** | 바코드 계근, 저울 계근, 지정 중량 등 |
| **중량 추출 방식** | 바코드에서 추출 vs 저울에서 입력 vs 고정값 |
| **라벨 출력 분기** | 비정량(B)일 때 STORE_CODE 사용 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 계근 방식 분기 |
| ShipmentActivity.java | 계근 방식 분기 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 아이템타입 저장 |

---

## 4. 아이템 타입 상수 정의

**파일**: BixolonShipmentActivity.java (Line 195~199)

```java
private static final String ITEM_TYPE_W = "W";    // 바코드 계근 (원료육)
private static final String ITEM_TYPE_HW = "HW";  // 바코드 계근 확장
private static final String ITEM_TYPE_S = "S";    // 저울 계근 (세트)
private static final String ITEM_TYPE_J = "J";    // 지정 중량 (제품)
private static final String ITEM_TYPE_B = "B";    // 홈플러스 비정량
```

---

## 5. 아이템 타입별 계근 방식

| 타입 | 명칭 | 중량 추출 | 제조일 | 박스번호 |
|:---:|------|----------|:------:|:--------:|
| **W** | 원료육 (바코드 계근) | 바코드에서 추출 | ✅ 추출 | ✅ 추출 |
| **HW** | 바코드 계근 확장 | 바코드에서 추출 | ✅ 추출 | ✅ 추출 |
| **S** | 세트 (저울 계근) | 저울에서 입력 | ❌ | ❌ |
| **J** | 제품 (지정 중량) | 고정값 (PACKWEIGHT) | ❌ | ❌ |
| **B** | 비정량 | 저울/수동 입력 | ❌ | ❌ |

---

## 6. 사용 코드

### 6.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 287)

```java
si.setITEM_TYPE(temp[19].toString());        // 아이템타입 (W, S, J, B 등)
```

### 6.2 계근 방식 분기 - 바코드 계근 (W, HW)

**파일**: BixolonShipmentActivity.java (Line 1250~1260)

```java
if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_W)
    || arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_HW)) {
    // 바코드에서 중량, 제조일, 박스시리얼 추출
    // 이마트 ITEM_TYPE W (바코드 계근)
}
```

### 6.3 계근 방식 분기 - 저울 계근 (S)

**파일**: BixolonShipmentActivity.java (Line 1303~1313)

```java
else if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_S)) {
    // 저울에서 중량 입력
    // 제조일, 박스시리얼 없음
}
```

### 6.4 계근 방식 분기 - 지정 중량 (J)

**파일**: BixolonShipmentActivity.java (Line 1354~1355)

```java
else if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_J)) {
    // 이마트 ITEM_TYPE J (지정된 중량 입력)
    // 바코드에서 중량, 제조일, 박스시리얼 X
    // PACKWEIGHT 값 사용
}
```

### 6.5 계근 방식 분기 - 비정량 (B)

**파일**: BixolonShipmentActivity.java (Line 1365~1375)

```java
if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_B)) {
    // 홈플러스 비정량
    // 저울/수동 중량 입력
}
```

### 6.6 라벨 출력 - 비정량 분기

**파일**: BixolonShipmentActivity.java (Line 2807~2811)

```java
// ITEM_TYPE_B(비정량)이면 storeCode, 아니면 pointCode 출력
if (si.getITEM_TYPE().equals(ITEM_TYPE_B)) {
    slcsCmd.append(slcsText(170, 135, 155, 155, storeCode.toString()));  // 비정량: 점포코드(STORE_CODE)
} else {
    slcsCmd.append(slcsText(170, 135, 155, 155, pointCode.toString()));  // 정량: 지점코드(EMARTLOGIS_CODE)
}
```

### 6.7 중량 계산 분기

**파일**: BixolonShipmentActivity.java (Line 2183~2195)

```java
if (si.getITEM_TYPE().equals(ITEM_TYPE_W) || si.getITEM_TYPE().equals(ITEM_TYPE_HW)) {
    // 바코드에서 중량 추출
    // 바코드 정보의 WEIGHT_FROM ~ WEIGHT_TO 위치에서 추출
    Log.d(TAG, "ITEM_TYPE : W");
} else if (si.getITEM_TYPE().equals(ITEM_TYPE_J)) {
    // 지정 중량 사용 (PACKWEIGHT)
    Log.d(TAG, "ITEM_TYPE : J");
}
```

---

## 7. 계근 흐름도

```
바코드 스캔
     ↓
ITEM_TYPE 확인
     ↓
┌────────────────────────────────────────────────────┐
│                                                    │
▼                    ▼                    ▼          ▼
W/HW                 S                    J          B
바코드 계근          저울 계근            지정 중량   비정량
   │                  │                    │          │
   ▼                  ▼                    ▼          ▼
바코드에서          저울에서             PACKWEIGHT  저울/수동
중량 추출           중량 입력            값 사용     입력
제조일 추출            -                    -          -
박스번호 추출          -                    -          -
```

---

## 8. 타입별 라벨 코드 표시

| 타입 | 라벨에 표시되는 코드 |
|:---:|---------------------|
| **B** (비정량) | STORE_CODE (점포코드) |
| **그 외** | EMARTLOGIS_CODE (지점코드) |

---

## 9. 결론

**상태**: ✅ 필수 (삭제 불가)

ITEM_TYPE은 **계근 방식 결정의 핵심 컬럼**으로:
- **W/HW**: 바코드에서 중량, 제조일, 박스번호 **자동 추출**
- **S**: 저울에서 중량 **수동 입력**
- **J**: PACKWEIGHT 값으로 **지정 중량** 사용
- **B**: 비정량 처리, 라벨에 **STORE_CODE** 표시

---

**최종 수정일**: 2026-02-03
