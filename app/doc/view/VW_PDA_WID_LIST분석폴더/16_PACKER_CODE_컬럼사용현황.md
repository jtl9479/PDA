# PACKER_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 패커코드
**파싱 위치**: temp[16]

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
| **킬코이 판별** | PACKER_CODE = "30228" 이면 킬코이 제품 |
| **소비기한 입력 조건** | 킬코이 + 미트센터 조합 시 소비기한 필수 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 킬코이 판별, 소비기한 로직 |
| ShipmentActivity.java | 킬코이 판별, 소비기한 로직 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 패커코드 저장 |

---

## 4. 킬코이 상수 정의

**파일**: BixolonShipmentActivity.java (Line 175)

```java
private static final String KILKOY_PACKER_CODE = "30228";  // 킬코이 패커코드
```

---

## 5. 사용 코드

### 5.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 284)

```java
si.setPACKER_CODE(temp[16].toString());      // 패커코드
```

### 5.2 킬코이 판별 - 소비기한 입력 화면

**파일**: BixolonShipmentActivity.java (Line 622~626)

```java
Log.i(TAG, "=====================패커코드 체크==================" + arSM.get(current_work_position).getPACKER_CODE());

// 킬코이(30228) + 미트센터(9231) 조합 시 소비기한 입력 화면 이동
if (arSM.get(current_work_position).getPACKER_CODE().equals(KILKOY_PACKER_CODE)
    && arSM.get(current_work_position).getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)) {
    // 소비기한 입력 화면 이동
}
```

### 5.3 킬코이 미트센터 계근 제한

**파일**: BixolonShipmentActivity.java (Line 1169~1171)

```java
// 킬코이 제품 + 미트센터 납품 시 소비기한 필수
if (arSM.get(current_work_position).getPACKER_CODE().equals(KILKOY_PACKER_CODE)
    && arSM.get(current_work_position).getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)) {
    Toast.makeText(getApplicationContext(),
        "미트센터 납품 - KILKOY 상품의 경우 소비기한정보가 필수로 입력되어야 합니다.\n" +
        "현 상품의 계근을 진행할 수 없습니다. 관리자에게 문의하세요.",
        Toast.LENGTH_LONG).show();
    return;
}
```

### 5.4 킬코이 라벨 출력 - 소비기한 계산

**파일**: BixolonShipmentActivity.java (Line 2079)

```java
// 킬코이 + 미트센터 납품분: 제조일에서 소비기한 계산
if (si.getPACKER_CODE().equals(KILKOY_PACKER_CODE)
    && si.getSTORE_CODE().equals(MEAT_CENTER_STORE_CODE)) {
    // makingdate 이용해 소비기한 변환 출력
}
```

---

## 6. 킬코이 처리 조건

| 조건 | PACKER_CODE | STORE_CODE | 처리 |
|------|:-----------:|:----------:|------|
| 킬코이 + 미트센터 | 30228 | 9231 | 소비기한 필수 입력 |
| 킬코이 + 일반 | 30228 | 그 외 | 일반 처리 |
| 일반 패커 | 그 외 | - | 일반 처리 |

---

## 7. 관련 상수

| 상수명 | 값 | 의미 |
|--------|-----|------|
| `KILKOY_PACKER_CODE` | "30228" | 킬코이 패커코드 |
| `MEAT_CENTER_STORE_CODE` | "9231" | 이마트 미트센터 코드 |

---

## 8. 킬코이 미트센터 처리 흐름

```
1. 출하대상 조회
       ↓
2. PACKER_CODE == "30228" (킬코이)?
       ↓ Yes
3. STORE_CODE == "9231" (미트센터)?
       ↓ Yes
4. 소비기한 입력 필수
       ↓
5. 소비기한 미입력 시 → 계근 불가 (Toast 표시)
       ↓
6. 소비기한 입력 시 → 제조일 기반 소비기한 계산 → 라벨 출력
```

---

## 9. 결론

**상태**: ✅ 필수 (삭제 불가)

PACKER_CODE는 **킬코이 제품 판별의 핵심 컬럼**으로:
- **PACKER_CODE = "30228"** 이면 킬코이 제품
- 킬코이 + 미트센터(9231) 조합 시 **소비기한 입력 필수**
- 소비기한 미입력 시 **계근 불가** 처리
- 라벨 출력 시 **제조일 기반 소비기한 계산**

---

**최종 수정일**: 2026-02-03
