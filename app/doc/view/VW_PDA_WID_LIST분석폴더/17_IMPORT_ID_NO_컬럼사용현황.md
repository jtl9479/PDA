# IMPORT_ID_NO 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 수입식별번호 (이력번호)
**파싱 위치**: temp[15]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | ● |
| 바코드생성 | ● |
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
| **바코드 생성** | 바코드 구성 요소 (12자리) |
| **라벨 출력** | 뒤 4자리만 표시 |
| **스피너 표시** | 지점명 + 수입식별번호 형태로 표시 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 바코드 생성, 라벨 출력, 스피너 |
| ShipmentActivity.java | 바코드 생성, 라벨 출력, 스피너 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 수입식별번호 저장 |

---

## 4. 바코드 타입별 사용

| 타입 | 바코드 구성 | IMPORT_ID_NO 사용 |
|------|-----------|:-----------------:|
| **M0** | 상품코드6 + 중량6 + 회사코드6 + 수입식별번호12 | ✅ 전체 12자리 |
| **M1** | 상품코드6 + 중량6 + 회사코드6 | ❌ 미사용 |
| **M3** | 납품코드 + 중량6 + 회사코드6 + 수입식별번호12 | ✅ 전체 12자리 |
| **M4** | 납품코드 + 중량6 + 회사코드6 | ❌ 미사용 |
| **M8** | 상품코드6 + 중량6 + 회사코드6 + 수입식별번호12 | ✅ 전체 12자리 |
| **M9** | 상품코드6 + 수입식별번호12 + 용도코드 | ✅ 전체 12자리 |
| **E0** | 상품코드6 + 중량6 + 회사코드6 + 수입식별번호12 | ✅ 전체 12자리 |
| **E1** | 상품코드6 + 중량6 + 회사코드6 + 111111111111 | ❌ 고정값 사용 |
| **E2** | 상품코드13 + 수입식별번호12 | ✅ 전체 12자리 |
| **E3** | 상품코드13 | ❌ 미사용 |

---

## 5. 사용 코드

### 5.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 283)

```java
si.setIMPORT_ID_NO(temp[15].toString());    // 수입식별번호
```

### 5.2 바코드 생성 - M0 타입

**파일**: BixolonShipmentActivity.java (Line 2228~2232)

```java
// M0: 상품코드6 + 중량6 + 회사코드6 + 수입식별번호12
pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
```

### 5.3 바코드 생성 - E2 타입

**파일**: BixolonShipmentActivity.java (Line 2325~2329)

```java
// E2: 상품코드13 + 수입식별번호12
pBarcode = si.getEMARTITEM_CODE().toString() + si.getIMPORT_ID_NO();
pBarcodeStr = si.getEMARTITEM_CODE().toString() + " " + si.getIMPORT_ID_NO();
```

### 5.4 라벨 출력 - 뒤 4자리만 표시

**파일**: BixolonShipmentActivity.java (Line 2831)

```java
// 중량/수입식별번호 뒤 4자리 형태로 출력
slcsCmd.append(slcsText(380, 361, 40, 40,
    String.valueOf(print_weight_double) + "/" + si.getIMPORT_ID_NO().substring(8, 12)));
// 예: "13.5/1234"
```

### 5.5 스피너 표시 - 지점명과 함께

**파일**: BixolonShipmentActivity.java (Line 3395)

```java
// 스피너에 "지점명 / 수입식별번호" 형태로 표시
list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO());
// 예: "이마트 강남점 / 123456789012"
```

---

## 6. 바코드 구성 예시

### M0 타입 (27자리)
```
상품코드  중량    회사  수입식별번호
123456 + 001350 + 01 + 123456789012
```

### E2 타입 (25자리)
```
상품코드13자리   수입식별번호
1234567890123 + 123456789012
```

---

## 7. 라벨 출력 위치

라벨 내 출력 형식: **"중량/수입식별번호 뒤4자리"**

```
┌─────────────────────────────────────┐
│  센터명                             │
│  지점명                             │
│                                     │
│  BOX                                │
│  원산지    13.5/1234  ← 중량/뒤4자리 │
│  2026년 01월 15일                   │
│  업체명                             │
└─────────────────────────────────────┘
```

---

## 8. 수입식별번호 구조 (12자리)

```
XXXX XXXX XXXX
 │      │    │
 │      │    └─ 일련번호 (4자리) ← 라벨에 표시
 │      └────── 중간번호 (4자리)
 └───────────── 앞번호 (4자리)
```

---

## 9. 결론

**상태**: ✅ 필수 (삭제 불가)

IMPORT_ID_NO는 **바코드 생성의 핵심 컬럼**으로:
- **바코드 생성** 시 12자리 전체 사용 (M0, M3, M8, M9, E0, E2 등)
- **라벨 출력** 시 뒤 4자리만 표시 (`substring(8, 12)`)
- **스피너** 에서 지점명과 함께 표시하여 출하대상 구분
- VIEW 원천: **PM_자재입출고명세서.이력번호**

---

**최종 수정일**: 2026-02-03
