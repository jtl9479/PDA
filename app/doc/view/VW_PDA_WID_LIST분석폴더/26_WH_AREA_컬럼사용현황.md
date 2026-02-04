# WH_AREA 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 창고구역
**파싱 위치**: temp[24]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | |
| 라벨출력 | ● |
| 로직분기 | |
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
| **라벨 출력** | 라벨 우측 하단에 창고구역 코드 인쇄 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 라벨 출력 (창고구역) |
| ShipmentActivity.java | 라벨 출력 (창고구역) |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 창고구역 저장 |

---

## 4. 사용 코드

### 4.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 296, 305, 313)

```java
si.setWH_AREA(temp[24].toString());          // 창고구역
```

### 4.2 라벨 출력 - 이마트 일반

**파일**: BixolonShipmentActivity.java (Line 2598~2603)

```java
// WH_AREA 출력
whArea = si.getWH_AREA();
Log.e(TAG, "::::::::: whArea check44 ::::::::" + whArea);
if(whArea != null || !whArea.equals("")){
    slcsCmd.append(slcsText(430, 385, 65, 65, whArea));  // ★ 위치: (430, 385), 폰트: 65x65
}
```

### 4.3 라벨 출력 - 롯데(L0)

**파일**: BixolonShipmentActivity.java (Line 3111~3118)

```java
// [11] WH_AREA 출력 (x=385, y=305, 폰트크기 65x65) - 창고구역 코드
whArea = si.getWH_AREA();
Log.e(TAG, "::::::::: whArea check44 ::::::::" + whArea);

if (whArea != null && !whArea.equals("")) {
    slcsCmd.append(slcsText(385, 305, 65, 65, whArea));  // ★ 위치: (385, 305), 폰트: 65x65
}
```

### 4.4 라벨 출력 - 미트센터

**파일**: BixolonShipmentActivity.java (Line 2656~2660, 2707~2711)

```java
whArea = si.getWH_AREA();
if(whArea != null || !whArea.equals("")){
    slcsMeat.append(slcsText(430, 385, 65, 65, whArea));
}
```

---

## 5. 라벨 출력 위치

| 라벨 타입 | X 좌표 | Y 좌표 | 폰트 크기 |
|:--------:|:------:|:------:|:---------:|
| 이마트 일반 | 430 | 385 | 65x65 |
| 미트센터 | 430 | 385 | 65x65 |
| 롯데(L0) | 385 | 305 | 65x65 |

---

## 6. 라벨 레이아웃 예시

### 이마트 라벨
```
┌─────────────────────────────────────┐
│  센터명                             │
│  지점명                             │
│                                     │
│  ||||||||||||||||||||  (바코드)     │
│                                     │
│  중      량 : 13.5 KG               │
│  납품일자 : 2026년 01월 15일        │
│  업체코드 : 01                ┌────┐│
│  업 체 명 : 하이랜드          │A-1 ││  ← ★ WH_AREA
│                               └────┘│
└─────────────────────────────────────┘
```

### 롯데 라벨 (L0)
```
┌─────────────────────────────────────┐
│  상품명                             │
│  ||||||||||||||||||||  (바코드1)    │
│  ════════════════════════           │
│  ||||||||||||||||||||  (바코드2)    │
│  중      량 : 13.5 KG    ┌────┐     │
│  납품처 : 롯데            │A-1 │     │  ← ★ WH_AREA
│  제조일자 : 2026.01.15   └────┘     │
│  ════════════════════════           │
└─────────────────────────────────────┘
```

---

## 7. VIEW 원천

| 항목 | 내용 |
|------|------|
| **VIEW 컬럼** | BCC2.REF_CODE2 WH_AREA |
| **원천 테이블** | B_COMMON_CODE (별칭 BCC2) |
| **원천 컬럼** | REF_CODE2 |
| **역할** | 점포코드(STORE_CODE)를 창고구역으로 매핑 |

### VIEW SQL 발췌

```sql
-- VW_PDA_WID_LIST (Line 122)
SELECT ...
       BCC2.REF_CODE2 WH_AREA,  -- B_COMMON_CODE.REF_CODE2 → 창고구역
       ...
FROM W_EMART_ORDER_ITEM EOI
     INNER JOIN B_COMMON_CODE BCC2
        ON BCC2.CODE = EOI.STORE_CODE  -- 점포코드로 조인
```

---

## 8. VIEW별 WH_AREA 값

| VIEW | WH_AREA 값 |
|------|-----------|
| VW_PDA_WID_LIST | B_COMMON_CODE.REF_CODE2 (실제 창고구역) |
| VW_PDA_WID_LIST_LOTTE | '' (빈 문자열) |
| VW_PDA_WID_WHOLESALE_LIST | '' (빈 문자열) |
| VW_PDA_WID_LIST_NONFIXED | B_COMMON_CODE.REF_CODE2 |
| VW_PDA_WID_LIST_NONFIXED_HP | ' ' (공백 문자) |

---

## 9. 데이터 예시

| 예시 값 | 의미 |
|--------|------|
| A-1 | 창고 A, 구역 1 |
| A-01 | 창고 A, 구역 01 |
| LT_A01 | 롯데 창고 A, 구역 01 |
| NF_A01 | 비정량 창고 A, 구역 01 |

---

## 10. 결론

**상태**: ✅ 필수 (삭제 불가)

WH_AREA는 **라벨 출력의 창고구역 표시 컬럼**으로:
- 라벨 **우측 하단**에 큰 폰트(65x65)로 출력
- **물류 작업자**가 창고 내 위치 파악에 사용
- B_COMMON_CODE 테이블의 **REF_CODE2**에서 매핑
- 롯데, 도매 VIEW는 빈 값 제공

---

**최종 수정일**: 2026-02-03
