# USE_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 용도코드
**파싱 위치**: temp[26]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | ● |
| 라벨출력 | |
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
| **M9 바코드 생성** | 두 번째 바코드(pBarcode2)에 포함 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | M9 바코드 생성 |
| ShipmentActivity.java | M9 바코드 생성 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 용도코드 저장 |

---

## 4. 사용 코드

### 4.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 298, 307)

```java
si.setUSE_CODE(temp[26].toString());         // 용도코드
```

### 4.2 M9 바코드 생성

**파일**: BixolonShipmentActivity.java (Line 2376~2377)

```java
case "M9":
    // 이마트 우육 센터납

    // 바코드1: 상품코드(6) + 중량(6) + 회사코드 + 수입식별번호(12)
    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    // ★ 바코드2: 상품코드(6) + 수입식별번호(12) + 용도코드
    pBarcode2 = si.getEMARTITEM_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + si.getUSE_CODE();
    pBarcodeStr2 = si.getEMARTITEM_CODE().substring(0, 6) + " " + si.getIMPORT_ID_NO() + " " + si.getUSE_CODE();

    break;
```

---

## 5. USE_CODE vs USE_NAME

| 컬럼 | 의미 | 파싱 위치 | 용도 |
|------|------|:--------:|------|
| **USE_CODE** | 용도코드 | temp[26] | **바코드 생성** (M9) |
| USE_NAME | 용도명 | temp[25] | 라벨 텍스트 출력 (M9) |

---

## 6. M9 바코드2 구조

```
┌──────────────────────────────────────────────────┐
│  상품코드(6)  │  수입식별번호(12)  │  용도코드   │
│    880123    │   123456789012    │     01     │
└──────────────────────────────────────────────────┘
         ↑              ↑               ↑
   EMARTITEM_CODE   IMPORT_ID_NO    USE_CODE
    substring(0,6)
```

### 예시

| 항목 | 값 |
|------|-----|
| EMARTITEM_CODE | 8801234567890 |
| 상품코드 6자리 | 880123 |
| IMPORT_ID_NO | 123456789012 |
| USE_CODE | 01 |
| **pBarcode2** | **88012312345678901201** |

---

## 7. VIEW 원천

| 항목 | 내용 |
|------|------|
| **VIEW 컬럼** | EB.USE_CODE AS USE_CODE |
| **원천 테이블** | B_EMART_BARCODE (별칭 EB) |
| **원천 컬럼** | USE_CODE |
| **역할** | 제품 용도 구분 코드 |

### VIEW SQL 발췌

```sql
-- VW_PDA_WID_LIST (Line 130)
EB.USE_CODE AS USE_CODE
-- B_EMART_BARCODE.USE_CODE에서 직접 가져옴
```

---

## 8. 적용 바코드 타입

| 바코드 타입 | USE_CODE 사용 |
|:-----------:|:-------------:|
| M0 | ❌ |
| M1 | ❌ |
| M3 | ❌ |
| M4 | ❌ |
| M8 | ❌ |
| **M9** | ✅ **사용** (바코드2) |
| E0~E3 | ❌ |
| L0 | ❌ |

---

## 9. 데이터 예시

| USE_CODE | USE_NAME (참고) | 설명 |
|:--------:|----------------|------|
| 01 | 일반 | 일반 용도 |
| 02 | 구이용 | 구이 전용 |
| 03 | 국거리용 | 국물 요리용 |
| NF | 비정량용 | 비정량 제품 |
| HPF | 홈플러스용 | 홈플러스 전용 |

---

## 10. 라벨 레이아웃 (M9)

```
┌─────────────────────────────────────┐
│  센터명                             │
│  지점명                             │
│                                     │
│  |||||||||||||||||||| (바코드1)     │
│  880123 001350 01 123456789012      │
│                                     │
│  중      량 : 13.5 KG               │
│  납품일 : 2026년 01월 15일          │
│  ════════════════════════           │
│  |||||||||||||||||||| (바코드2)  국산│
│  880123 123456789012 01  ← ★ USE_CODE│
│  육우,일반                          │
└─────────────────────────────────────┘
```

---

## 11. 결론

**상태**: ✅ 필수 (삭제 불가)

USE_CODE는 **M9 바코드 타입 전용 바코드 생성 컬럼**으로:
- **M9 타입**의 두 번째 바코드(pBarcode2)에 포함
- 바코드 구성: `상품코드(6) + 수입식별번호(12) + USE_CODE`
- B_EMART_BARCODE 테이블에서 직접 가져옴
- USE_NAME(용도명)과 쌍으로 사용 (코드/명칭)

---

**최종 수정일**: 2026-02-03
