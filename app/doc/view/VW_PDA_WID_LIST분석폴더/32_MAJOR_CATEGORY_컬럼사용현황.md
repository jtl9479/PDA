# MAJOR_CATEGORY 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 대분류
**파싱 위치**: - (앱에 전달 안 됨)

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | |
| 라벨출력 | |
| 로직분기 | |
| DB저장 | |
| 바코드검증 | |
| 조회조건 | |
| 앱미전달 | ● |

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
| **VIEW 내부 로직** | BARCODE_TYPE='M9' 결정 조건 |

---

## 2. 특성

| 항목 | 내용 |
|------|------|
| **앱 전달** | ❌ 전달 안 됨 |
| **로컬 DB 저장** | ❌ 저장 안 함 |
| **DTO 필드** | ❌ 없음 |
| **용도** | VIEW 내부 BARCODE_TYPE 결정 조건만 |

---

## 3. 사용 위치

| 파일 | 용도 |
|------|------|
| VW_PDA_WID_LIST (VIEW SQL) | BARCODE_TYPE='M9' 결정 조건 |

---

## 4. VIEW SQL에서의 역할

### 4.1 BARCODE_TYPE 결정 로직

**파일**: VW_PDA_WID_LIST (Line 78)

```sql
DECODE(
  CENTER_SCALE_USE_YN, 'Y',                    -- 센터저울사용여부 = 'Y'
  DECODE(
    BI.ITEM_TYPE, '10',                        -- 품목타입 = '10'
    DECODE(
      BI.MAJOR_CATEGORY, '10', 'M9',           -- ★ 대분류 = '10' → 'M9'
      EO.BARCODE_TYPE                          -- 아니면 기존 바코드타입
    ),
    EO.BARCODE_TYPE                            -- 품목타입 ≠ '10' → 기존
  ),
  EO.BARCODE_TYPE                              -- 센터저울 미사용 → 기존
) AS BARCODE_TYPE
```

### 4.2 SELECT 컬럼 (참조용)

**파일**: VW_PDA_WID_LIST (Line 96)

```sql
BI.MAJOR_CATEGORY AS MAJOR_CATEGORY
-- B_ITEM 테이블에서 가져옴 (SELECT에 포함되지만 앱에서 미사용)
```

---

## 5. BARCODE_TYPE='M9' 결정 조건

| 조건 | 값 | 설명 |
|------|:--:|------|
| CENTER_SCALE_USE_YN | 'Y' | 센터저울 사용 |
| BI.ITEM_TYPE | '10' | 품목타입 10 |
| **BI.MAJOR_CATEGORY** | **'10'** | **대분류 10** |

### 조건 충족 시

```
CENTER_SCALE_USE_YN = 'Y'
        AND
BI.ITEM_TYPE = '10'
        AND
BI.MAJOR_CATEGORY = '10'
        ↓
BARCODE_TYPE = 'M9'
```

---

## 6. 흐름도

```
┌─────────────────────────────────────┐
│     CENTER_SCALE_USE_YN = 'Y'?      │
└─────────────────┬───────────────────┘
                  │
        ┌────Yes──┴──No────┐
        │                  │
        ▼                  ▼
┌───────────────┐    EO.BARCODE_TYPE
│ ITEM_TYPE='10'?│
└───────┬───────┘
        │
  ┌─Yes─┴─No─┐
  │          │
  ▼          ▼
┌────────────┐  EO.BARCODE_TYPE
│MAJOR_      │
│CATEGORY    │
│='10'?      │
└─────┬──────┘
      │
┌─Yes─┴─No─┐
│          │
▼          ▼
'M9'    EO.BARCODE_TYPE
```

---

## 7. VIEW 원천

| 항목 | 내용 |
|------|------|
| **VIEW 컬럼** | BI.MAJOR_CATEGORY |
| **원천 테이블** | B_ITEM (별칭 BI) |
| **원천 컬럼** | MAJOR_CATEGORY |
| **역할** | 품목 대분류 코드 |

---

## 8. M9 바코드 타입 의미

| 바코드 타입 | 용도 |
|:-----------:|------|
| **M9** | 이마트 우육 센터납 (저울 스캔용) |

### M9 라벨 특징

- "[저울 스캔용]" 표시
- 두 번째 바코드에 USE_CODE 포함
- 원산지명(CT_NAME), 용도명(USE_NAME) 출력

---

## 9. 앱에서 미사용 이유

MAJOR_CATEGORY는 **VIEW 내부 로직**에서만 사용:

1. VIEW가 BARCODE_TYPE 값을 **미리 계산**
2. 앱은 계산된 **BARCODE_TYPE**만 수신 (temp[18])
3. MAJOR_CATEGORY 원본값은 앱에 **불필요**

```
서버 VIEW                          앱
┌──────────────┐                ┌──────────────┐
│ MAJOR_       │                │              │
│ CATEGORY=10  │──계산──▶       │ BARCODE_TYPE │
│ + 기타조건   │    'M9'        │   = 'M9'     │
└──────────────┘                └──────────────┘
   (내부용)                        (수신)
```

---

## 10. 결론

**상태**: ⚠️ 내부용 (앱 미전달)

MAJOR_CATEGORY는 **VIEW 내부 BARCODE_TYPE 결정용 컬럼**으로:
- **앱에 전달되지 않음** (파싱 위치 없음)
- **로컬 DB에 저장하지 않음**
- VIEW 내부에서 **BARCODE_TYPE='M9'** 결정 조건
- 조건: `CENTER_SCALE_USE_YN='Y' + ITEM_TYPE='10' + MAJOR_CATEGORY='10'`

---

**최종 수정일**: 2026-02-03
