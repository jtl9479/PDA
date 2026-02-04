# USE_NAME 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 용도명
**파싱 위치**: temp[25]

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
| **라벨 출력** | M9 타입 라벨에 "상품명,용도명" 형식으로 출력 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | M9 라벨 출력 (상품명+용도명) |
| ShipmentActivity.java | M9 라벨 출력 (상품명+용도명) |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 용도명 저장 |

---

## 4. 사용 코드

### 4.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 297, 306)

```java
si.setUSE_NAME(temp[25].toString());         // 용도명
```

### 4.2 M9 바코드 생성 - 용도명 로그

**파일**: BixolonShipmentActivity.java (Line 2370)

```java
case "M9":
    // 이마트 비정량 납품분
    Log.d(TAG, "용도명 : " + si.getUSE_NAME());

    // 바코드 문자열 3 (제품명 + 용도)
    pBarcodeStr3 = si.EMARTITEM + "," + si.getUSE_NAME();  // ★ 용도명 포함
    break;
```

### 4.3 M9 라벨 출력 - 상품명+용도명

**파일**: BixolonShipmentActivity.java (Line 2551~2558)

```java
} else if(si.getBARCODE_TYPE().equals(BARCODE_TYPE_M9)){
    slcsCmd.append(slcsBarcode(125, 325, 60, pBarcode2));
    String ctName = si.getCT_NAME();
    slcsCmd.append(slcsText(450, 330, 25, 25, ctName));
    slcsCmd.append(slcsText(125, 390, 25, 25, pBarcodeStr2));

    // ★ 상품명 + 용도명 출력
    String belowBarcodeString = si.EMARTITEM + "," + si.getUSE_NAME();
    slcsCmd.append(slcsText(80, 420, 25, 25, belowBarcodeString));
}
```

---

## 5. USE_NAME vs USE_CODE

| 컬럼 | 의미 | 파싱 위치 | 용도 |
|------|------|:--------:|------|
| **USE_CODE** | 용도코드 | temp[26] | 바코드 생성 (M9) |
| **USE_NAME** | 용도명 | temp[25] | **라벨 출력** (M9) |

---

## 6. 라벨 출력 (M9 타입만)

### 출력 위치

| 항목 | X 좌표 | Y 좌표 | 폰트 크기 |
|------|:------:|:------:|:---------:|
| 상품명+용도명 | 80 | 420 | 25x25 |

### 라벨 레이아웃 (M9)

```
┌─────────────────────────────────────┐
│  센터명                             │
│  지점명                             │
│                                     │
│  |||||||||||||||||||| (바코드1)     │
│  바코드문자열1                      │
│                                     │
│  중      량 : 13.5 KG               │
│  납품일 : 2026년 01월 15일          │
│  ════════════════════════           │
│  |||||||||||||||||||| (바코드2)  국산│
│  바코드문자열2                      │
│  육우,일반  ← ★ 상품명+용도명       │
└─────────────────────────────────────┘
```

---

## 7. VIEW 원천

| 항목 | 내용 |
|------|------|
| **VIEW 컬럼** | USE_NAME |
| **원천 테이블** | B_COMMON_CODE |
| **MASTER_CODE** | 'EMART_RAWMEAT_USE_TYPE' |
| **역할** | USE_CODE → 용도명 변환 |

### VIEW SQL 발췌

```sql
-- VW_PDA_WID_LIST (Line 125~129)
(
  SELECT BC.CODE_NAME
    FROM B_COMMON_CODE BC
   WHERE BC.MASTER_CODE = 'EMART_RAWMEAT_USE_TYPE'  -- 이마트 원료육 용도 타입
     AND BC.CODE = EB.USE_CODE                      -- USE_CODE로 조회
) AS USE_NAME
```

---

## 8. VIEW별 USE_NAME 값

| VIEW | USE_NAME 값 |
|------|-----------|
| VW_PDA_WID_LIST | B_COMMON_CODE.CODE_NAME |
| VW_PDA_WID_LIST_NONFIXED | B_COMMON_CODE.CODE_NAME |
| VW_PDA_WID_LIST_NONFIXED_HP | ' ' (공백 문자) |
| VW_PDA_WID_LIST_LOTTE | 컬럼 없음 |
| VW_PDA_WID_WHOLESALE_LIST | 컬럼 없음 |

---

## 9. 데이터 예시

| USE_CODE | USE_NAME (예상) |
|:--------:|-----------------|
| 01 | 일반 |
| 02 | 구이용 |
| 03 | 국거리용 |
| 04 | 장조림용 |

---

## 10. 적용 바코드 타입

| 바코드 타입 | USE_NAME 사용 |
|:-----------:|:-------------:|
| M0 | ❌ |
| M1 | ❌ |
| M3 | ❌ |
| M4 | ❌ |
| M8 | ❌ |
| **M9** | ✅ **사용** |
| E0~E3 | ❌ |
| L0 | ❌ |

---

## 11. 결론

**상태**: ✅ 필수 (삭제 불가)

USE_NAME은 **M9 바코드 타입 전용 라벨 출력 컬럼**으로:
- **M9 타입**에서만 라벨에 출력
- "상품명,용도명" 형식으로 바코드 하단에 표시
- B_COMMON_CODE 테이블에서 **USE_CODE → 용도명** 변환
- USE_CODE와 함께 사용 (코드/명칭 쌍)

---

**최종 수정일**: 2026-02-03
