# 8개 출하대상받기 Oracle VIEW WHERE 조건 비교 분석

## 개요

- **파일 경로**: `app/doc/view/VW_PDA_WID_*` (8개 VIEW DDL 파일)
- **패키지**: Oracle DB VIEW (HIGHLAND 스키마 / INNO 스키마)
- **총 라인 수**: VIEW별 50~300줄
- **타입**: Oracle VIEW DDL
- **작성일**: 2026-04-22

**목적**: 8개 출하대상받기 JSP가 참조하는 Oracle VIEW의 WHERE 조건만 얕게 추출하여 비교 매트릭스를 생성한다.  
차후 6개 미전환 JSP의 MSSQL 전환 시 WHERE 조건 참조 자료로 활용한다.

**분석 범위**: WHERE 조건 추출 + 단순 비교 매트릭스 (JOIN 구조·SELECT 컬럼·업무 의미 분석은 본 작업 범위 외)

### 분석 대상 8개 VIEW 일람

| # | searchType | VIEW 명 | 스키마 | 구조 | DDL 파일 |
|:-:|:---:|--------|:------:|:----:|----------|
| 1 | 0 (이마트) | `VW_PDA_WID_LIST` | HIGHLAND | UNION ALL 2블록 | `app/doc/view/VW_PDA_WID_LIST` |
| 2 | 1 (생산) | `VW_PDA_WID_PRO_LIST` | INNO | 단일 SELECT | `app/doc/view/VW_PDA_WID_PRO_LIST` |
| 3 | 2 (홈플러스) | `VW_PDA_WID_HOMEPLUS_LIST` | HIGHLAND | UNION ALL 2블록 | `app/doc/view/VW_PDA_WID_HOMEPLUS_LIST` |
| 4 | 3 (도매) | `VW_PDA_WID_WHOLESALE_LIST` | HIGHLAND | UNION ALL 2블록 | `app/doc/view/VW_PDA_WID_WHOLESALE_LIST` |
| 5 | 4 (비정량) | `VW_PDA_WID_LIST_NONFIXED` | INNO | 단일 SELECT | `app/doc/view/VW_PDA_WID_LIST_NONFIXED` |
| 6 | 5 (홈플러스 비정량) | `VW_PDA_WID_LIST_NONFIXED_HP` | INNO | 단일 SELECT | `app/doc/view/VW_PDA_WID_LIST_NONFIXED_HP` |
| 7 | 6 (롯데) | `VW_PDA_WID_LIST_LOTTE` | INNO | UNION 2블록 | `app/doc/view/VW_PDA_WID_LIST_LOTTE` |
| 8 | 7 (생산 라벨) | `VW_PDA_WID_PRO_4LABEL_LIST` | INNO | **DDL 파일 없음** | 누락 (JSP에서 VIEW명만 확인) |

---

## 1. 각 VIEW의 WHERE 조건 (개별)

---

### 1.1 VW_PDA_WID_LIST (이마트, searchType=0)

**용도**: 이마트 계근 사용  
**구조**: UNION ALL 2블록 (블록1=해외매입, 블록2=국내매입)

#### A. 메인 WHERE 절 — 블록 1 (해외매입 / I_BL_D 조인)

```sql
WHERE     1 = 1
      AND ID.PACKING_QTY = 0
      AND ID.GI_REQ_PKG <> 0
      AND WR.CONTRACT_TYPE <> '40'
      --AND IH.SEND_FLAG = 'Y'
      AND EO.EOI_ID IS NOT NULL
      AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')
```

| 조건 | 설명 |
|------|------|
| `ID.PACKING_QTY = 0` | 아직 포장(계근) 처리가 안 된 건만 |
| `ID.GI_REQ_PKG <> 0` | 출하요청수량이 있는 건만 |
| `WR.CONTRACT_TYPE <> '40'` | 국내매입 계약 타입(40) 제외 → 해외매입만 |
| `EO.EOI_ID IS NOT NULL` | 이마트 발주서(EOI)와 매칭된 건만 |
| `IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 출하요청일만 |
| ~~`IH.SEND_FLAG = 'Y'`~~ | 주석처리됨 (비활성) |

#### A. 메인 WHERE 절 — 블록 2 (국내매입 / I_OFFER_D 조인)

```sql
WHERE     1 = 1
       AND ID.PACKING_QTY = 0
       AND ID.GI_REQ_PKG <> 0
       AND EO.EOI_ID IS NOT NULL
       AND WR.CONTRACT_TYPE = '40'
       --AND IH.SEND_FLAG = 'Y'
       AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')
```

| 조건 | 설명 |
|------|------|
| `ID.PACKING_QTY = 0` | 아직 포장(계근) 처리가 안 된 건만 |
| `ID.GI_REQ_PKG <> 0` | 출하요청수량이 있는 건만 |
| `EO.EOI_ID IS NOT NULL` | 이마트 발주서(EOI)와 매칭된 건만 |
| `WR.CONTRACT_TYPE = '40'` | 국내매입 계약 타입(40)만 |
| `IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 출하요청일만 |
| ~~`IH.SEND_FLAG = 'Y'`~~ | 주석처리됨 (비활성) |

#### B. 서브쿼리 EO 내부 JOIN 조건 (실질 필터 효과)

이마트 VIEW의 **타입구분 필터**. 서브쿼리 EO의 `B_EMART_BARCODE EB` 조인 ON 절에 위치.

```sql
-- 블록1 L135, 블록2 L272 동일 위치
INNER JOIN B_EMART_BARCODE EB
    ON EB.EMARTITEM_CODE = EOI.ITEM_CODE
   AND EB.ITEM_TYPE = 'W'              -- ★ 이마트 정량 핵심 필터: 원료육만
```

| 조건 | 설명 | 위치 |
|------|------|:----:|
| **`EB.ITEM_TYPE = 'W'`** | **타입구분 W(원료육) 만 조회** ← 이마트 정량 VIEW 식별 핵심 | JOIN ON (블록1·2 모두) |

**중요**: SQL 위치는 JOIN ON 절이지만 **필터링 효과는 WHERE와 동일**. 비정량(4)의 `IN ('J', 'B')`와 대조되는 핵심 차이점.

#### 참고: SELECT 절 ITEM_TYPE 주석 (DDL L79)

```sql
EO.ITEM_TYPE,                     -- 원료육 :  W, 제품 : J, 비정량 : B
```

→ DDL 주석이 **W/J/B의 의미를 직접 명시**. 4.5장에서 종합 정리.

---

### 1.2 VW_PDA_WID_PRO_LIST (생산, searchType=1)

**용도**: 생산투입 계근시 사용  
**구조**: 단일 SELECT (UNION 없음)

```sql
WHERE A.GI_H_ID = B.GI_H_ID
  AND B.GOODS_R_ID = R.GOODS_R_ID
  AND R.BL_D_ID = D.OFFER_D_ID
  AND B.ITEM_CODE = I.ITEM_CODE
  AND A.GI_TYPE = 'M1'
  AND A.SEND_FLAG = 'N'
  AND R.CONTRACT_TYPE = '40'
  AND B.STATUS = '10'
  AND B.PACKING_QTY = 0
  AND B.GI_REQ_PKG <> 0
  AND B.PROC_DATE IS NULL
  AND NVL(B.PROC_PUT_FLAG,'N') = 'N'
  AND A.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')
```

| 조건 | 설명 |
|------|------|
| `A.GI_H_ID = B.GI_H_ID` 외 3개 | 테이블 간 JOIN 조건 (WHERE 절에 기술) |
| `A.GI_TYPE = 'M1'` | 생산투입 출고 타입만 |
| `A.SEND_FLAG = 'N'` | 미전송 건만 (SEND_FLAG 활성 적용) |
| `R.CONTRACT_TYPE = '40'` | 국내매입 계약 타입(40)만 |
| `B.STATUS = '10'` | 출고상세 상태 10(대기)인 건만 |
| `B.PACKING_QTY = 0` | 아직 포장(계근) 처리가 안 된 건만 |
| `B.GI_REQ_PKG <> 0` | 출하요청수량이 있는 건만 |
| `B.PROC_DATE IS NULL` | 처리일자 미지정 건만 |
| `NVL(B.PROC_PUT_FLAG,'N') = 'N'` | 투입처리 플래그가 N인 건만 |
| `A.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 출하요청일만 |

---

### 1.3 VW_PDA_WID_HOMEPLUS_LIST (홈플러스, searchType=2)

**용도**: 홈플러스 계근시 사용  
**구조**: UNION ALL 2블록 (블록1=해외매입, 블록2=국내매입)

#### 블록 1 (해외매입 / I_BL_D 조인)

```sql
WHERE     1 = 1
      AND ID.PACKING_QTY = 0
      AND ID.GI_REQ_PKG <> 0
      AND WR.CONTRACT_TYPE <> '40'
      --AND IH.SEND_FLAG = 'Y'
      AND EO.EOI_ID IS NOT NULL
      AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')
```

| 조건 | 설명 |
|------|------|
| `ID.PACKING_QTY = 0` | 아직 포장(계근) 처리가 안 된 건만 |
| `ID.GI_REQ_PKG <> 0` | 출하요청수량이 있는 건만 |
| `WR.CONTRACT_TYPE <> '40'` | 해외매입만 (국내매입 제외) |
| `EO.EOI_ID IS NOT NULL` | 홈플러스 발주서(EOI)와 매칭된 건만 |
| `IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 출하요청일만 |
| ~~`IH.SEND_FLAG = 'Y'`~~ | 주석처리됨 (비활성) |

#### 블록 2 (국내매입 / I_OFFER_D 조인)

```sql
WHERE     1 = 1
      AND ID.PACKING_QTY = 0
      AND ID.GI_REQ_PKG <> 0
      AND EO.EOI_ID IS NOT NULL
      AND WR.CONTRACT_TYPE = '40'
      --AND IH.SEND_FLAG = 'Y'
       AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')
```

| 조건 | 설명 |
|------|------|
| `ID.PACKING_QTY = 0` | 아직 포장(계근) 처리가 안 된 건만 |
| `ID.GI_REQ_PKG <> 0` | 출하요청수량이 있는 건만 |
| `EO.EOI_ID IS NOT NULL` | 홈플러스 발주서(EOI)와 매칭된 건만 |
| `WR.CONTRACT_TYPE = '40'` | 국내매입 계약 타입(40)만 |
| `IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 출하요청일만 |
| ~~`IH.SEND_FLAG = 'Y'`~~ | 주석처리됨 (비활성) |

#### B. 서브쿼리 EO 내부 JOIN 조건 (실질 필터 효과)

홈플러스 정량 VIEW의 **타입구분 필터**. 서브쿼리 EO의 `B_EMART_BARCODE EB` 조인 ON 절에 위치.

```sql
-- 블록1 L108, 블록2 L208 동일 위치
INNER JOIN B_EMART_BARCODE EB
    ON EB.EMARTITEM_CODE = EOI.ITEM_CODE
   AND EB.ITEM_TYPE = 'W'              -- ★ 홈플러스 정량 핵심 필터: 원료육만
```

| 조건 | 설명 | 위치 |
|------|------|:----:|
| **`EB.ITEM_TYPE = 'W'`** | **타입구분 W(원료육) 만 조회** ← 홈플러스 정량 VIEW 식별 핵심 | JOIN ON (블록1·2 모두) |

#### 참고: SELECT 절 ITEM_TYPE 주석 (DDL L71-72)

```sql
--EO.ITEM_TYPE,
'S' AS ITEM_TYPE, --PDA에서 소수점 2자리 계근 가능한 경우는 ITEM_TYPE S아님 J일 경우임
                 -- J는 제품의 의미로 사용하고 있기때문에 S로 하드코딩
```

→ DDL 주석이 **"J는 제품의 의미"**임을 명시 (이마트 L79와 일관).

---

### 1.4 VW_PDA_WID_WHOLESALE_LIST (도매, searchType=3)

**용도**: 도매 계근시 사용  
**구조**: UNION ALL 2블록 (블록1=해외매입, 블록2=국내매입)

#### 블록 1 (해외매입 / I_BL_D 조인)

```sql
WHERE     1 = 1
      AND ID.PACKING_QTY = 0
      AND ID.GI_REQ_PKG <> 0
      AND WR.CONTRACT_TYPE <> '40'
      AND ID.CHECK_YN = 'Y'
      AND ID.EOI_ID IS NULL
      AND BI.STATUS = 'Y'
      AND BI.ITEM_TYPE = '10'
      AND IH.SEND_FLAG <> 'N'
      AND WR.GR_WAREHOUSE_CODE = '4001'
      AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')
```

| 조건 | 설명 |
|------|------|
| `ID.PACKING_QTY = 0` | 아직 포장(계근) 처리가 안 된 건만 |
| `ID.GI_REQ_PKG <> 0` | 출하요청수량이 있는 건만 |
| `WR.CONTRACT_TYPE <> '40'` | 해외매입만 |
| `ID.CHECK_YN = 'Y'` | 확인(승인)된 건만 |
| `ID.EOI_ID IS NULL` | 이마트/홈플러스 발주서 미연결 건만 (도매용) |
| `BI.STATUS = 'Y'` | 활성 아이템만 |
| `BI.ITEM_TYPE = '10'` | 아이템 타입 10인 건만 |
| `IH.SEND_FLAG <> 'N'` | 전송 대기 상태가 아닌 건만 |
| `WR.GR_WAREHOUSE_CODE = '4001'` | 창고코드 4001(도매 창고)만 |
| `IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 출하요청일만 |

#### 블록 2 (국내매입 / I_OFFER_D 조인)

```sql
WHERE     1 = 1
      AND ID.PACKING_QTY = 0
      AND ID.GI_REQ_PKG <> 0
      AND WR.CONTRACT_TYPE = '40'
      AND ID.CHECK_YN = 'Y'
      AND ID.EOI_ID IS NULL
      AND BI.STATUS = 'Y'
      AND BI.ITEM_TYPE = '10'
      AND IH.SEND_FLAG <> 'N'
      AND WR.GR_WAREHOUSE_CODE = '4001'
      AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')
```

| 조건 | 설명 |
|------|------|
| `ID.PACKING_QTY = 0` | 아직 포장(계근) 처리가 안 된 건만 |
| `ID.GI_REQ_PKG <> 0` | 출하요청수량이 있는 건만 |
| `WR.CONTRACT_TYPE = '40'` | 국내매입만 |
| `ID.CHECK_YN = 'Y'` | 확인(승인)된 건만 |
| `ID.EOI_ID IS NULL` | 도매 전용 (발주서 미연결) |
| `BI.STATUS = 'Y'` | 활성 아이템만 |
| `BI.ITEM_TYPE = '10'` | 아이템 타입 10인 건만 |
| `IH.SEND_FLAG <> 'N'` | 전송 대기 상태가 아닌 건만 |
| `WR.GR_WAREHOUSE_CODE = '4001'` | 창고코드 4001만 |
| `IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 출하요청일만 |

---

### 1.5 VW_PDA_WID_LIST_NONFIXED (이마트 비정량, searchType=4)

**용도**: 이마트 비정량 제품 계근시 사용  
**구조**: 단일 SELECT (UNION 없음)

#### A. 메인 WHERE 절

```sql
WHERE     1 = 1
      AND ID.PACKING_QTY = 0
      AND ID.GI_REQ_PKG <> 0
      AND EO.EOI_ID IS NOT NULL
      AND WR.CONTRACT_TYPE = '40'
      --AND IH.SEND_FLAG = 'Y'
       AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')
```

| 조건 | 설명 |
|------|------|
| `ID.PACKING_QTY = 0` | 아직 포장(계근) 처리가 안 된 건만 |
| `ID.GI_REQ_PKG <> 0` | 출하요청수량이 있는 건만 |
| `EO.EOI_ID IS NOT NULL` | 이마트 발주서(EOI)와 매칭된 건만 |
| `WR.CONTRACT_TYPE = '40'` | 국내매입 계약 타입(40)만 |
| `IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 출하요청일만 |
| ~~`IH.SEND_FLAG = 'Y'`~~ | 주석처리됨 (비활성) |

#### B. 서브쿼리 EO 내부 JOIN 조건 (실질 필터 효과)

비정량 VIEW에서 **가장 중요한 필터**. 서브쿼리 EO의 `B_EMART_BARCODE EB` 조인 ON 절에 위치.

```sql
-- 서브쿼리 EO 내부 (L127 인근)
INNER JOIN B_EMART_BARCODE EB
    ON EB.EMARTITEM_CODE = EOI.ITEM_CODE
   AND EB.ITEM_TYPE IN ('J', 'B')      -- ★ 비정량 핵심 필터
   ...
INNER JOIN B_COMMON_CODE BCC2
    ON BCC2.MASTER_CODE = 'EMART_BRANCH_CODE'
   AND EOI.STORE_CODE = BCC2.CODE
   AND EB.BARCODE_TYPE = 'M8'           -- ★ 비정량 바코드 타입
```

| 조건 | 설명 | 위치 |
|------|------|:----:|
| **`EB.ITEM_TYPE IN ('J', 'B')`** | **타입구분 J(가공육)/B(비정량) 만 조회** ← 비정량 VIEW 식별 핵심 | JOIN ON |
| **`EB.BARCODE_TYPE = 'M8'`** | **이마트 비정량 바코드 M8만 조회** | JOIN ON |

**중요**: 위 2개 조건은 SQL 위치는 JOIN ON 절이지만 **필터링 효과는 WHERE와 동일**. 비정량 VIEW가 다른 VIEW와 구분되는 **가장 본질적 조건**. 본 비교 분석 매트릭스(2장)에서도 별도 행으로 표기.

#### 참고: 문의사항 Q01과 연계

`IN ('J', 'B')` 조건의 'J'(가공육) 포함 적정성은 **문의사항 Q01**으로 별도 등록되어 답변 대기 중.
- 문서: `app/doc/문의사항/01_비정량_출하대상_타입구분_J코드_포함여부.md`

---

### 1.6 VW_PDA_WID_LIST_NONFIXED_HP (홈플러스 비정량, searchType=5)

**용도**: 홈플러스 비정량 제품 계근 사용  
**구조**: 단일 SELECT (UNION 없음)

```sql
WHERE 1=1
AND ID.STATUS = '10'
AND  BEB.BARCODE_TYPE = 'H5'
AND  R.GR_DATE  > '20240101'
ORDER BY BEB.EMRTITEM_NAME , EOI.STORE_CODE
```

| 조건 | 설명 |
|------|------|
| `ID.STATUS = '10'` | 출고상세 상태 10(대기)인 건만 |
| `BEB.BARCODE_TYPE = 'H5'` | 홈플러스 비정량 바코드 타입(H5)만 |
| `R.GR_DATE > '20240101'` | 2024-01-01 이후 입고된 건만 (날짜 하드코딩) |

**특이사항**:  
- 다른 VIEW에 있는 `PACKING_QTY = 0`, `GI_REQ_PKG <> 0`, `GI_REQ_DATE >= SYSDATE` 조건이 없음  
- `R.GR_DATE > '20240101'` 날짜 리터럴 하드코딩  
- ORDER BY 절이 VIEW 내에 포함됨

---

### 1.7 VW_PDA_WID_LIST_LOTTE (롯데, searchType=6)

**용도**: 롯데마트 계근시 사용  
**구조**: UNION 2블록 (UNION ALL이 아닌 UNION → 중복 제거)

#### 블록 1 (해외매입 / I_BL_D 조인)

```sql
WHERE     1 = 1
      AND ID.GI_REQ_PKG <> 0
      AND WR.CONTRACT_TYPE <> '40'
      AND IH.SEND_FLAG <> 'N'
      AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')
      AND bcc.USER_ID  = 'LOTTE'
      AND ID.STATUS = '10'
```

| 조건 | 설명 |
|------|------|
| `ID.GI_REQ_PKG <> 0` | 출하요청수량이 있는 건만 |
| `WR.CONTRACT_TYPE <> '40'` | 해외매입만 |
| `IH.SEND_FLAG <> 'N'` | 전송 대기 상태가 아닌 건만 |
| `IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 출하요청일만 |
| `bcc.USER_ID = 'LOTTE'` | 롯데 거래처 담당자 코드로 필터 |
| `ID.STATUS = '10'` | 출고상세 상태 10인 건만 |
| `ID.PACKING_QTY = 0` | **블록1에는 해당 조건 라인 자체가 없음** (주석도 없음) |

**JOIN 조건 내 필터**:  
- `BCC.STATUS = 'Y'` (B_CLIENT_CHARGE 조인 시)  
- `beb.BARCODE_TYPE LIKE 'L%'` (B_EMART_BARCODE 조인 시)

#### 블록 2 (국내매입 / I_OFFER_D 조인)

```sql
WHERE     1 = 1
      AND ID.GI_REQ_PKG <> 0
      AND WR.CONTRACT_TYPE = '40'
      AND IH.SEND_FLAG <> 'N'
      AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE, 'YYYYMMDD')
      AND bcc.USER_ID  = 'LOTTE'
      AND beb.ITEM_TYPE = 'W'
      AND ID.STATUS = '10'
      --   AND ID.PACKING_QTY = 0
```

| 조건 | 설명 |
|------|------|
| `ID.GI_REQ_PKG <> 0` | 출하요청수량이 있는 건만 |
| `WR.CONTRACT_TYPE = '40'` | 국내매입만 |
| `IH.SEND_FLAG <> 'N'` | 전송 대기 상태가 아닌 건만 |
| `IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')` | 오늘 이후 출하요청일만 |
| `bcc.USER_ID = 'LOTTE'` | 롯데 거래처 담당자 코드로 필터 |
| `beb.ITEM_TYPE = 'W'` | 원료육만 (블록2 추가 조건) |
| `ID.STATUS = '10'` | 출고상세 상태 10인 건만 |
| ~~`ID.PACKING_QTY = 0`~~ | **블록2에만 주석 표시 존재** (`--AND ID.PACKING_QTY = 0`, 비활성) |

**JOIN 조건 내 필터**:  
- `BCC.STATUS = 'Y'` (B_CLIENT_CHARGE 조인 시)  
- `beb.BARCODE_TYPE LIKE 'L%'` (B_EMART_BARCODE 조인 시)

---

### 1.8 VW_PDA_WID_PRO_4LABEL_LIST (생산 라벨, searchType=7)

**용도**: 생산 4라벨 출력시 사용 (search_production_4label.jsp 참조)  
**구조**: DDL 파일 없음 — 분석 불가

VIEW 명은 JSP `search_production_4label.jsp` 69줄에서 `FROM VW_PDA_WID_PRO_4LABEL_LIST` 확인.  
DDL 파일이 `app/doc/view/` 폴더에 없어 WHERE 조건 추출 불가.

---

## 2. 공통 조건 vs 차이 조건 매트릭스

가로축: 8개 VIEW (이마트/생산/홈플/도매/비정량/홈비정/롯데/생산라벨)  
세로축: 각 조건  
값: O=있음, X=없음, △=주석처리(비활성), ?=DDL없음

> 참고: UNION ALL/UNION 구조 VIEW는 각 블록에 동일 조건이 있는 경우 O로 표시

| 조건 | 이마트(0) | 생산(1) | 홈플(2) | 도매(3) | 비정량(4) | 홈비정(5) | 롯데(6) | 생산라벨(7) |
|------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| `PACKING_QTY = 0` | O | O | O | O | O | X | X | ? |
| `GI_REQ_PKG <> 0` | O | O | O | O | O | X | O | ? |
| `GI_REQ_DATE >= SYSDATE` | O | O | O | O | O | X | O | ? |
| `CONTRACT_TYPE <> '40'` (블록1) | O | X | O | O | X | X | O | ? |
| `CONTRACT_TYPE = '40'` (블록2) | O | O | O | O | O | X | O | ? |
| `EOI_ID IS NOT NULL` | O | X | O | X | O | X | X | ? |
| `EOI_ID IS NULL` | X | X | X | O | X | X | X | ? |
| `SEND_FLAG = 'N'` | X | O | X | X | X | X | X | ? |
| `SEND_FLAG <> 'N'` (활성) | △주석 | X | △주석 | O | △주석 | X | O | ? |
| `STATUS = '10'` | X | O | X | X | X | O | O | ? |
| `CHECK_YN = 'Y'` | X | X | X | O | X | X | X | ? |
| `BI.STATUS = 'Y'` | X | X | X | O | X | X | X | ? |
| `BI.ITEM_TYPE = '10'` | X | X | X | O | X | X | X | ? |
| `GR_WAREHOUSE_CODE = '4001'` | X | X | X | O | X | X | X | ? |
| `PROC_DATE IS NULL` | X | O | X | X | X | X | X | ? |
| `PROC_PUT_FLAG = 'N'` | X | O | X | X | X | X | X | ? |
| `GI_TYPE = 'M1'` | X | O | X | X | X | X | X | ? |
| `GR_DATE > '20240101'` | X | X | X | X | X | O | X | ? |
| `BARCODE_TYPE = 'H5'` | X | X | X | X | X | O | X | ? |
| `bcc.USER_ID = 'LOTTE'` | X | X | X | X | X | X | O | ? |
| `beb.ITEM_TYPE = 'W'` (블록2) | X | X | X | X | X | X | O | ? |

### 2.1 JOIN ON 절 필터 매트릭스 (실질 필터 효과 동일)

WHERE 절은 아니지만 JOIN ON 조건에서 데이터를 필터링하는 항목. **VIEW 식별·구분의 핵심 조건들이 여기 위치**.

| JOIN 필터 조건 | 이마트(0) | 생산(1) | 홈플(2) | 도매(3) | **비정량(4)** | 홈비정(5) | 롯데(6) | 생산라벨(7) |
|------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **`EB.ITEM_TYPE = 'W'`** (원료육) | O | X | O | **X** ※1 | X | X | X | ? |
| **`EB.ITEM_TYPE IN ('J', 'B')`** (가공육+비정량) ★ | X | X | X | X | **O** | X | X | ? |
| **`EB.BARCODE_TYPE = 'M8'`** (이마트 비정량) ★ | X | X | X | X | **O** | X | X | ? |
| `BEB.ITEM_TYPE = 'W'` (홈비정) | X | X | X | X | X | **X** ※2 | X | ? |
| `beb.BARCODE_TYPE LIKE 'L%'` (롯데) | X | X | X | X | X | X | O | ? |
| `BCC.STATUS = 'Y'` (롯데 거래처 활성) | X | X | X | X | X | X | O | ? |

※1 도매(3) 정정: B_EMART_BARCODE 조인 자체가 없음 (B_ITEM만 조인)  
※2 홈비정(5) 정정: 해당 VIEW에 `BEB.ITEM_TYPE='W'` 조건 없음 (단순 EMARTITEM_CODE 조인만)  
★ 표시: 비정량 VIEW만의 핵심 식별 조건 (다른 VIEW와 절대적으로 구분되는 항목)

**참고 — JOIN 매트릭스에서 제거된 조건**:
- `BEB.BARCODE_TYPE = 'H5'` 행: 본 조건은 **WHERE 절에 위치** (홈비정 DDL L45). 메인 매트릭스(2장)의 별도 행으로 이미 표기되어 있어 본 매트릭스에서 제거 (중복/오분류 방지)
- `BI.STATUS = 'Y'` 행: 도매(3)의 경우 **WHERE 절** 조건 (도매 DDL L91)이며, 이마트(0)/홈플(2)에는 해당 조건 자체가 없음. 본 매트릭스에서 제거 (메인 매트릭스에 도매 O로 정상 기재됨)
- `S_BARCODE_INFO STATUS = 'Y'` 행: BARCODEGOODS 스칼라 서브쿼리 내부 조건이며 VIEW 식별 필터로 작용하지 않으므로 제거

---

## 3. 마트사 구분 코드 정리

VIEW DDL에서 직접 확인 가능한 코드만 정리.

| 코드 구분 | 값 | 근거 |
|----------|-----|------|
| 바코드 타입 - 이마트 비정량 | `M8` | VW_PDA_WID_LIST_NONFIXED 서브쿼리 내 `EB.BARCODE_TYPE = 'M8'` |
| 바코드 타입 - 홈플러스 비정량 | `H5` | VW_PDA_WID_LIST_NONFIXED_HP WHERE `BEB.BARCODE_TYPE = 'H5'` |
| 바코드 타입 - 롯데 | `L%` (L로 시작) | VW_PDA_WID_LIST_LOTTE JOIN 조건 `beb.BARCODE_TYPE LIKE 'L%'` |
| 창고 코드 - 도매 | `4001` | VW_PDA_WID_WHOLESALE_LIST `WR.GR_WAREHOUSE_CODE = '4001'` |
| 거래처 사용자ID - 롯데 | `LOTTE` | VW_PDA_WID_LIST_LOTTE `bcc.USER_ID = 'LOTTE'` |
| CONTRACT_TYPE - 해외매입 | `<> '40'` | 이마트/홈플/도매/롯데 블록1 공통 |
| CONTRACT_TYPE - 국내매입 | `= '40'` | 이마트/생산/홈플/도매/비정량/롯데 블록2 공통 |
| GI_TYPE - 생산투입 | `M1` | VW_PDA_WID_PRO_LIST `A.GI_TYPE = 'M1'` |
| ITEM_TYPE - 원료육 | `W` | VW_PDA_WID_LIST_LOTTE 블록2 `beb.ITEM_TYPE = 'W'` |
| B_EMART_BARCODE JOIN - 이마트/홈플 | `EB.ITEM_TYPE = 'W'` | VW_PDA_WID_LIST·HOMEPLUS JOIN 조건 |
| B_EMART_BARCODE JOIN - 비정량 | `EB.ITEM_TYPE IN ('J','B')` | VW_PDA_WID_LIST_NONFIXED JOIN 조건 |

---

## 4. 핵심 발견 (얕은 수준)

### 4.1 모든 VIEW 공통 조건

아래 3개 조건은 분석 가능한 7개 VIEW 중 5개 이상에서 공통으로 나타남:

- `GI_REQ_PKG <> 0`: 7개 VIEW 중 6개 (홈플러스 비정량 제외)
- `GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')`: 7개 중 6개 (홈플러스 비정량 제외)
- `PACKING_QTY = 0`: 7개 중 5개 (홈플러스 비정량·롯데 제외)

### 4.2 UNION 구조 분포

| 구조 | VIEW |
|------|------|
| UNION ALL 2블록 | 이마트(0), 홈플러스(2), 도매(3) |
| UNION 2블록 (중복제거) | 롯데(6) |
| 단일 SELECT | 생산(1), 비정량(4), 홈플러스 비정량(5) |
| DDL 없음 | 생산 라벨(7) |

### 4.3 특이 구조 VIEW

- **홈플러스 비정량(5)**: `PACKING_QTY`, `GI_REQ_PKG`, `GI_REQ_DATE` 조건 없음. 날짜 리터럴(`'20240101'`) 하드코딩. ORDER BY 절이 VIEW 내에 포함됨.
- **생산(1)**: 유일하게 SEND_FLAG 조건이 활성(`= 'N'`)이며 PROC_DATE, PROC_PUT_FLAG, GI_TYPE 같은 생산 고유 조건을 가짐.
- **도매(3)**: 유일하게 `CHECK_YN = 'Y'`, `BI.STATUS = 'Y'`, `BI.ITEM_TYPE = '10'`, `GR_WAREHOUSE_CODE = '4001'` 조건 보유. `EOI_ID IS NULL`로 마트 발주서와 무관한 건만 조회.
- **롯데(6)**: UNION ALL이 아닌 UNION (중복 제거 O). `bcc.USER_ID = 'LOTTE'` 하드코딩으로 롯데 구분.

### 4.4 VIEW별 식별 핵심 조건 (가장 본질적인 필터)

각 VIEW가 다른 VIEW와 구분되는 가장 본질적 조건. WHERE 절일 수도 있고 JOIN ON 절일 수도 있음.

| searchType | 명칭 | **핵심 식별 조건** |
|:---:|------|------|
| 0 | 이마트 정량 | `EB.ITEM_TYPE = 'W'` (JOIN, 블록1·2 모두) + UNION ALL 해외/국내 |
| 1 | 생산 | `BI.ITEM_TYPE = '10'` (SELECT 변환) + `GI_TYPE = 'M1'` + `PROC_PUT_FLAG = 'N'` |
| 2 | 홈플러스 정량 | `EB.ITEM_TYPE = 'W'` (JOIN, 블록1·2 모두) + UNION ALL 해외/국내 |
| 3 | 도매 | `EOI_ID IS NULL` + `CHECK_YN = 'Y'` + `GR_WAREHOUSE_CODE = '4001'` + `BI.STATUS = 'Y'` (WHERE) |
| **4** | **이마트 비정량** | **`EB.ITEM_TYPE IN ('J', 'B')` (JOIN) + `EB.BARCODE_TYPE = 'M8'` (JOIN)** ★ |
| 5 | 홈플러스 비정량 | `BEB.BARCODE_TYPE = 'H5'` (WHERE) + `R.GR_DATE > '20240101'` (WHERE, 날짜 하드코딩) |
| 6 | 롯데 | `bcc.USER_ID = 'LOTTE'` + `beb.BARCODE_TYPE LIKE 'L%'` (JOIN) + `beb.ITEM_TYPE = 'W'` (블록2 WHERE) |
| 7 | 생산 라벨 | DDL 없어 분석 불가 |

★ **이마트 비정량(searchType=4)의 본질**:
- `IN ('J', 'B')`: 타입구분 - DDL L79 주석에 "원료육: W, 제품: J, 비정량: B"로 명시
- `'M8'`: 이마트 비정량 전용 바코드 타입
- 두 조건 모두 **JOIN ON 절에 있지만 WHERE 절과 동일한 필터 효과**를 가지므로 WHERE 분석 시 반드시 함께 검토 필요

---

### 4.5 DDL SELECT 절 ITEM_TYPE 주석 종합 (Q01 단서)

**문의사항 Q01 (`IN ('J', 'B')`의 'J' 의미)**의 답변에 결정적 1차 자료. 3개 VIEW DDL이 SELECT 절 주석에 ITEM_TYPE 코드 의미를 직접 명시하고 있음.

| VIEW | 라인 | DDL 주석 인용 | 핵심 내용 |
|------|:----:|------------|---------|
| 이마트(0) | L79 | `EO.ITEM_TYPE,                     -- 원료육 :  W, 제품 : J, 비정량 : B` | **W=원료육 / J=제품 / B=비정량** 직접 명시 |
| 홈플러스(2) | L71-72 | `'S' AS ITEM_TYPE, --PDA에서 소수점 2자리 계근 가능한 경우는 ITEM_TYPE S아님 J일 경우임 J는 제품의 의미로 사용하고 있기때문에 S로 하드코딩` | **J = 제품의 의미** 재확인 |
| 도매(3) | L63 | (홈플러스(2)와 동일 주석) | 동일 |

**3개 DDL 주석 일관성**: J = "제품"으로 일관 명시. 사용자가 Q01 작성 시 표기한 "J = 가공육"은 동일한 카테고리의 다른 표현(가공육 ⊂ 제품)으로 추정.

**시사점 (해석 금지·자료 제시만)**:
- DDL 주석은 **공식 정의에 가까운 1차 자료**
- Q01 답변 시 본 자료를 함께 제출하면 답변자가 "J=제품" 정의를 확인 후 가공육 포함 여부를 판정 가능
- 단, DDL 주석 작성 시점이 언제인지 불명 → 현재 운영 정의와 일치하는지는 별도 확인 필요 (해석은 본 문서 범위 외)

---

## 5. 역할

- Oracle VIEW DDL에서 WHERE 조건만 추출·정리
- 8개 searchType별 출하대상 필터링 조건 비교 매트릭스 제공
- 미전환 JSP의 MSSQL 전환 시 WHERE 조건 참조 자료

---

## 6. 주요 상수/필드

해당 없음 (VIEW DDL 분석 문서)

---

## 7. 주요 메서드

해당 없음 (VIEW DDL 분석 문서)

---

## 8. 호출 관계

### 8.1 이 VIEW들을 참조하는 JSP

| VIEW | JSP (원본 경로 기준) |
|------|---------------------|
| VW_PDA_WID_LIST | `webapps/ROOT/inno/search_shipment.jsp` |
| VW_PDA_WID_PRO_LIST | `webapps/ROOT/inno/search_production.jsp` |
| VW_PDA_WID_HOMEPLUS_LIST | `webapps/ROOT/inno/search_shipment_homeplus.jsp` |
| VW_PDA_WID_WHOLESALE_LIST | `webapps/ROOT/inno/search_shipment_wholesale.jsp` |
| VW_PDA_WID_LIST_NONFIXED | `webapps/ROOT/inno/search_shipment_ono.jsp` |
| VW_PDA_WID_LIST_NONFIXED_HP | `webapps/ROOT/inno/search_homeplus_nonfixed.jsp` 또는 `search_homeplus_nonfixed2.jsp` |
| VW_PDA_WID_LIST_LOTTE | `webapps/ROOT/inno/search_shipment_lotte.jsp` |
| VW_PDA_WID_PRO_4LABEL_LIST | `webapps/ROOT/inno/search_production_4label.jsp` |

JSP 경로 기준: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\`

---

## 9. 데이터 흐름

```
[Oracle DB]
    VW_PDA_WID_* (8개 VIEW)
         ↓ SELECT
[JSP] search_shipment.jsp 등
         ↓ HTTP 응답 ("::" 구분자)
[Android App] ProgressDlgShipSearch.java
         ↓ 파싱 (temp[] 배열)
[로컬 DB] TB_SHIPMENT 테이블
```

---

## 10. 핵심 코드

```sql
-- 이마트 기준 WHERE 조건 (블록1 해외매입)
WHERE     1 = 1
      AND ID.PACKING_QTY = 0            -- 미계근
      AND ID.GI_REQ_PKG <> 0           -- 요청수량 있음
      AND WR.CONTRACT_TYPE <> '40'     -- 해외매입
      AND EO.EOI_ID IS NOT NULL        -- 발주서 연결
      AND IH.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD')  -- 오늘 이후

-- 홈플러스 비정량 (다른 VIEW와 가장 다른 패턴)
WHERE 1=1
AND ID.STATUS = '10'
AND  BEB.BARCODE_TYPE = 'H5'
AND  R.GR_DATE  > '20240101'           -- 날짜 하드코딩
```

---

## 11. 원본 비교

| 항목 | 원본 (Oracle) | 현재 (MSSQL 전환 현황) | 전환됨 |
|------|:-------------:|:----------------------:|:------:|
| VW_PDA_WID_LIST (이마트) | Oracle VIEW | MSSQL 전환 완료 | O |
| VW_PDA_WID_LIST_NONFIXED (비정량) | Oracle VIEW | MSSQL 전환 완료 | O |
| VW_PDA_WID_PRO_LIST (생산) | Oracle VIEW | 미전환 | X |
| VW_PDA_WID_HOMEPLUS_LIST (홈플) | Oracle VIEW | 미전환 | X |
| VW_PDA_WID_WHOLESALE_LIST (도매) | Oracle VIEW | 미전환 | X |
| VW_PDA_WID_LIST_NONFIXED_HP (홈플비정) | Oracle VIEW | 미전환 | X |
| VW_PDA_WID_LIST_LOTTE (롯데) | Oracle VIEW | 미전환 | X |
| VW_PDA_WID_PRO_4LABEL_LIST (생산라벨) | Oracle VIEW | 미전환 / DDL 없음 | X |

---

## 12. 주의사항

- `VW_PDA_WID_PRO_4LABEL_LIST` DDL 파일이 `app/doc/view/` 폴더에 없음. JSP에서 VIEW명만 확인됨. MSSQL 전환 시 Oracle DB에서 DDL을 직접 추출해야 함.
- `VW_PDA_WID_LIST_NONFIXED_HP` (홈플러스 비정량)는 `R.GR_DATE > '20240101'` 날짜 리터럴이 하드코딩되어 있음. MSSQL 전환 시 이 조건 처리 방식 확인 필요.
- `VW_PDA_WID_LIST_LOTTE`는 UNION ALL이 아닌 UNION (중복 제거). MSSQL에서는 동일하게 UNION 사용 필요.
- `SEND_FLAG` 조건이 VIEW별로 다름: 생산(=`'N'`), 도매/롯데(`<> 'N'`), 이마트/홈플(주석 처리됨). MSSQL 전환 시 각 VIEW 별도 확인 필요.
- 본 문서는 WHERE 조건만 얕게 추출한 것이며 JOIN 구조·SELECT 컬럼 분석은 포함하지 않음.

---

## 13. 관련 문서

- `app/doc/view/VW_PDA_WID_LIST` — 이마트 VIEW DDL
- `app/doc/view/VW_PDA_WID_PRO_LIST` — 생산 VIEW DDL
- `app/doc/view/VW_PDA_WID_HOMEPLUS_LIST` — 홈플러스 VIEW DDL
- `app/doc/view/VW_PDA_WID_WHOLESALE_LIST` — 도매 VIEW DDL
- `app/doc/view/VW_PDA_WID_LIST_NONFIXED` — 이마트 비정량 VIEW DDL
- `app/doc/view/VW_PDA_WID_LIST_NONFIXED_HP` — 홈플러스 비정량 VIEW DDL
- `app/doc/view/VW_PDA_WID_LIST_LOTTE` — 롯데 VIEW DDL
- `app/doc/소스분석/37_로컬DB_MSSQL전환_현황.md` — MSSQL 전환 현황 분석
- `app/doc/소스분석/41_이마트VIEW_vs_비정량VIEW_비교분석.md` — 이마트/비정량 VIEW 비교
- `app/doc/소스분석/42_이마트Oracle_MSSQL_JSP_비정량Oracle_3자비교분석.md` — Oracle/MSSQL 3자 비교
