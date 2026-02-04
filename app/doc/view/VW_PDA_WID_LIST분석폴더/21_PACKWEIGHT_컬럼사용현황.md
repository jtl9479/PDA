# PACKWEIGHT 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 포장중량 (팩 중량)
**파싱 위치**: temp[20]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | |
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
| **ITEM_TYPE=J 지정중량** | 제품 타입일 때 고정 중량값으로 사용 |
| **계근 중량** | 바코드 추출 대신 이 값을 중량으로 사용 |
| **라벨 출력** | 라벨에 중량으로 인쇄 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 지정 중량 계근, 라벨 출력 |
| ShipmentActivity.java | 지정 중량 계근, 라벨 출력 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 포장중량 저장 |

---

## 4. ITEM_TYPE과의 관계

| ITEM_TYPE | 중량 소스 | PACKWEIGHT 사용 |
|:---------:|----------|:---------------:|
| **W** | 바코드에서 추출 | ❌ |
| **HW** | 바코드에서 추출 | ❌ |
| **S** | 저울에서 입력 | ❌ |
| **J** | **PACKWEIGHT 값** | ✅ **사용** |
| **B** | 저울/수동 입력 | ❌ |

---

## 5. 사용 코드

### 5.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 288)

```java
si.setPACKWEIGHT(temp[20].toString());       // 포장중량
```

### 5.2 계근 시 중량 설정 - ITEM_TYPE=J

**파일**: BixolonShipmentActivity.java (Line 1354~1356)

```java
else if (arSM.get(current_work_position).getITEM_TYPE().equals(ITEM_TYPE_J)) {
    // 이마트 ITEM_TYPE J (지정된 중량 입력) | 바코드에서 중량, 제조일, 박스시리얼 X
    item_weight = arSM.get(current_work_position).getPACKWEIGHT();  // ★ PACKWEIGHT 사용
}
```

### 5.3 라벨 출력 - 중량 설정

**파일**: BixolonShipmentActivity.java (Line 2191~2192)

```java
else if (si.getITEM_TYPE().equals(ITEM_TYPE_J)) {
    print_weight_str = si.getPACKWEIGHT();  // ★ PACKWEIGHT를 출력 중량으로
    Log.d(TAG, "ITEM_TYPE : J");
}
```

### 5.4 롯데용 라벨 출력

**파일**: BixolonShipmentActivity.java (Line 2924~2925)

```java
else if (si.getITEM_TYPE().equals(ITEM_TYPE_J)) {
    print_weight_str = si.getPACKWEIGHT();  // 롯데용도 동일
    Log.d(TAG, "ITEM_TYPE : J");
}
```

---

## 6. 중량 처리 흐름

### ITEM_TYPE = W/HW (바코드 계근)
```
바코드 스캔 → 바코드에서 중량 추출 → item_weight
```

### ITEM_TYPE = J (지정 중량) ★
```
바코드 스캔 → PACKWEIGHT 값 사용 → item_weight
                    ↓
              바코드에서 중량 추출 안 함
```

### ITEM_TYPE = S (저울 계근)
```
저울에서 중량 입력 → item_weight
```

---

## 7. 라벨 출력 시 중량 흐름

```
ITEM_TYPE 확인
      ↓
┌─────────────────────────────────────┐
│                                     │
▼                                     ▼
W/HW/S                                J
바코드/저울 중량                    PACKWEIGHT
      │                               │
      └───────────┬───────────────────┘
                  ↓
           print_weight_str
                  ↓
             라벨에 인쇄
```

---

## 8. 예시

| 상품 | ITEM_TYPE | PACKWEIGHT | 실제 계근 중량 |
|------|:---------:|:----------:|:-------------:|
| 원료육 A | W | 10.0 | 바코드에서 추출 (12.5kg) |
| 제품 B | J | 10.0 | **10.0kg** (PACKWEIGHT 사용) |
| 세트 C | S | 10.0 | 저울 입력 (15.3kg) |

---

## 9. 결론

**상태**: ✅ 필수 (삭제 불가)

PACKWEIGHT는 **ITEM_TYPE=J 전용 지정중량 컬럼**으로:
- **ITEM_TYPE = "J"** (제품)일 때만 사용
- 바코드에서 중량을 추출하지 않고 **고정값** 사용
- 계근 시 `item_weight`로 설정
- 라벨 출력 시 `print_weight_str`로 사용

---

**최종 수정일**: 2026-02-03
