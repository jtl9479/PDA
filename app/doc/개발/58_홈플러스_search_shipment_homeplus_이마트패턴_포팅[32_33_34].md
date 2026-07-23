# search_shipment_homeplus.jsp — 이마트 검증 패턴 포팅 (년월조건 / SD 서브쿼리 / STORE_IN_DATE CONVERT)

**작성일**: 2026-07-23
**목적**: 홈플러스(searchType=2) 출하대상 조회 JSP(`search_shipment_homeplus.jsp`)에서 AI 정적검증(code-verifier)으로 발견된 위험 3건(오류 32·33·34)을 수정한다. 3건 모두 이마트(searchType=0) `search_shipment.jsp`에서 이미 검증·커밋 완료된 패턴이 홈플러스 JSP에는 미반영된 것으로, 이번 문서는 해당 패턴을 홈플러스에 그대로 포팅하는 작업만 다룬다. 원본(Oracle) 비교(original-comparator)는 이미 통과했으므로 앱(Java) 코드 변경은 없고 JSP만 수정한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### search_shipment_homeplus.jsp (실제 코드, 36~105줄)

```sql
String quertystring = "SELECT /* 홈플러스 출하대상 조회 */"
                + " D.SEQ AS GI_D_ID"
                + ", I.품목코드 AS ITEM_CODE"
                + ", I.품목명 AS ITEM_NAME"
                + ", HE.상품코드 AS EMARTITEM_CODE"
                + ", HE.상품명 AS EMARTITEM"
                + ", L.박스수량 AS GI_REQ_PKG"
                + ", L.중량 AS GI_REQ_QTY"
                + ", D.출고일자 AS GI_REQ_DATE"
                + ", COALESCE(NULLIF(V.BLNO, ''), V.이력번호) AS BL_NO"
                + ", '' AS BRAND_CODE"
                + ", HE.납품처코드 AS CLIENT_CODE"
                + ", HE.납품처명 AS CLIENTNAME"
                + ", B.상호 AS CENTERNAME"
                + ", I.규격 AS ITEM_SPEC"
                + ", I.원산지 AS CT_CODE"
                + ", V.이력번호 AS IMPORT_ID_NO"
                + ", I.패커코드 AS PACKER_CODE"
                + ", I.PPCODE AS PACKER_PRODUCT_CODE"
                + ", COALESCE(M1.바코드타입, M2.바코드타입) AS BARCODE_TYPE"
                + ", 'S' AS ITEM_TYPE"
                + ", COALESCE(NULLIF(V.평균중량,0), I.박스중량) AS PACKWEIGHT"
                + ", I.상품바코드 AS BARCODEGOODS"
                + ", SD.납기일자 AS STORE_IN_DATE"                              -- 59줄 (오류 34)
                + ", HE.납품처코드 AS EMARTLOGIS_CODE"
                + " FROM SM_출고상세 D"
                + " INNER JOIN SM_출고머리 H"
                + "   ON H.회사코드 = D.회사코드"
                + "  AND H.출고사업장 = D.출고사업장"
                + "  AND H.출고일자 = D.출고일자"
                + "  AND H.출고일련번호 = D.출고일련번호"
                + " JOIN CO_품목코드 I"
                + "   ON D.회사코드 = I.회사코드"
                + "  AND D.출고품목코드 = I.품목코드"
                + " JOIN SM_마트사발주홈플러스 HE"
                + "   ON D.마트사SEQ = HE.SEQ"
                + " JOIN CO_거래처MASTER B"
                + "   ON HE.회사코드 = B.회사코드"
                + "  AND HE.납품처코드 = B.마트사거래처코드"
                + "  AND B.마트사구분 = '4'"
                + " LEFT JOIN CO_거래처MASTER G"
                + "   ON G.회사코드 = D.회사코드"
                + "  AND G.거래처코드 = H.출고거래처"
                + " LEFT JOIN CO_매출처품목코드매핑 M1"
                + "   ON M1.회사코드 = D.회사코드"
                + "  AND M1.품목코드 = D.출고품목코드"
                + "  AND M1.거래처코드 = H.출고거래처"
                + " LEFT JOIN CO_거래처MASTER G2"
                + "   ON G2.회사코드 = D.회사코드"
                + "  AND G2.계층코드 = LEFT(G.계층코드, 5)"
                + "  AND G2.거래처코드 != H.출고거래처"
                + " LEFT JOIN CO_매출처품목코드매핑 M2"
                + "   ON M2.회사코드 = D.회사코드"
                + "  AND M2.품목코드 = D.출고품목코드"
                + "  AND M2.거래처코드 = G2.거래처코드"
                + " JOIN SM_출고LOT L"
                + "   ON L.출고상세SEQ = D.SEQ"
                + " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"                    -- 93~98줄 (오류 32)
                + "   ON V.회사코드 = D.회사코드"
                + "  AND V.사업장 = D.출고사업장"
                + "  AND V.창고코드 = D.창고코드"
                + "  AND V.품목코드 = D.출고품목코드"
                + "  AND V.LOTNO = L.LOTNO"
                // ★ AND V.년월 = LEFT(D.출고일자, 6) 조건 없음 (오류 32)
                + " LEFT JOIN SM_수주상세 SD"                                   -- 99~100줄 (오류 33)
                + "   ON SD.마트사SEQ = HE.SEQ"
                + " WHERE H.마트사구분 = '4'"
                + "   AND D.출고수량 > 0"
                + "   AND COALESCE(M1.타입구분, M2.타입구분) = 'W'"
                + qry_where
                + " ORDER BY HE.납품처코드 ASC, I.PPCODE ASC, I.품목명 ASC";
```

출력(24개 컬럼, 인덱스 0~23):

```java
out.println(
    rs.getString("GI_D_ID") + "::" +            // 0
    rs.getString("ITEM_CODE") + "::" +           // 1
    rs.getString("ITEM_NAME") + "::" +           // 2
    rs.getString("EMARTITEM_CODE") + "::" +      // 3
    rs.getString("EMARTITEM") + "::" +           // 4
    rs.getString("GI_REQ_PKG") + "::" +          // 5
    rs.getString("GI_REQ_QTY") + "::" +          // 6
    rs.getString("GI_REQ_DATE") + "::" +         // 7
    rs.getString("BL_NO") + "::" +               // 8
    rs.getString("BRAND_CODE") + "::" +          // 9
    rs.getString("CLIENT_CODE") + "::" +         // 10
    rs.getString("CLIENTNAME") + "::" +          // 11
    rs.getString("CENTERNAME") + "::" +          // 12
    rs.getString("ITEM_SPEC") + "::" +           // 13
    rs.getString("CT_CODE") + "::" +             // 14
    rs.getString("IMPORT_ID_NO") + "::" +        // 15
    rs.getString("PACKER_CODE") + "::" +         // 16
    rs.getString("PACKER_PRODUCT_CODE") + "::" + // 17
    rs.getString("BARCODE_TYPE") + "::" +        // 18
    rs.getString("ITEM_TYPE") + "::" +           // 19
    rs.getString("PACKWEIGHT") + "::" +          // 20
    rs.getString("BARCODEGOODS") + "::" +        // 21
    rs.getString("STORE_IN_DATE") + "::" +       // 22
    rs.getString("EMARTLOGIS_CODE") + ";;"       // 23
    );
```

### 이마트(참고, search_shipment.jsp — 이미 수정 완료된 형태)

```sql
+ " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"
+ "   ON V.회사코드 = D.회사코드"
+ "  AND V.사업장 = D.출고사업장"
+ "  AND V.창고코드 = D.창고코드"
+ "  AND V.품목코드 = D.출고품목코드"
+ "  AND V.LOTNO = L.LOTNO"
+ "  AND V.년월 = LEFT(D.출고일자, 6)"     -- ★ 이마트는 이미 반영 (커밋 dce6722)
```

이마트 `search_shipment.jsp`는 `SM_수주상세`를 애초에 JOIN하지 않고 `H.출고일자 AS STORE_IN_DATE`(59줄, `SM_출고머리`의 VARCHAR 계열 컬럼)를 직접 사용하므로 오류 33·34에 해당하는 구조 자체가 없다. 따라서 Step 2·3은 "이마트 코드를 그대로 복사"하는 방식이 아니라, 오류 문서(33·34)에 제시된 수정안을 홈플러스 구조에 맞게 적용한다.

### 문제점

- **오류 32**: `월품목별재고화일_LOT별_VIEW V` LEFT JOIN에 년월 조건이 없어 동일 LOT이 재고 이력 월 수만큼 중복 매칭 → 출하대상 행 N배 중복
- **오류 33**: `SM_수주상세 SD` LEFT JOIN이 `SD.마트사SEQ = HE.SEQ` 단일 조건만 사용해 1:N 관계를 고려하지 않음 → 동일 마트사SEQ에 SD N건이면 출하 건 N배 중복
- **오류 34**: `SD.납기일자`(MSSQL datetime)를 CONVERT 없이 `STORE_IN_DATE`로 SELECT → `rs.getString()`이 `"2026-05-04 00:00:00.0"` 형식 반환 → `LabelPrintHelper.setHomeplusPrinting()`(1177줄)의 `substring(0,4)/(4,6)/(6,8)` YYYYMMDD 전제 파싱이 어긋나 라벨 날짜 오인쇄

---

## 2. 변경 구조

### 데이터 흐름 (변경 전)

```
[PDA 홈플러스 출하대상 받기 요청]
    ↓
[search_shipment_homeplus.jsp]
    ↓
[SM_출고상세 D ⋈ SM_출고LOT L ⋈ SM_마트사발주홈플러스 HE]
    ↓
[LEFT JOIN 월품목별재고화일_LOT별_VIEW V (년월조건 없음)]  → LOT × 재고월수 만큼 행 팽창 (오류32)
    ↓
[LEFT JOIN SM_수주상세 SD ON SD.마트사SEQ=HE.SEQ (1:N)]     → SD 매칭건수 만큼 추가 팽창 (오류33)
    ↓
[SD.납기일자 → STORE_IN_DATE (CONVERT 없음)]                → datetime 원문 문자열 그대로 전송 (오류34)
    ↓
[PDA 수신 → TB_SHIPMENT 중복 행 INSERT, 라벨 날짜 오인쇄 가능]
```

### 데이터 흐름 (변경 후)

```
[PDA 홈플러스 출하대상 받기 요청]
    ↓
[search_shipment_homeplus.jsp]
    ↓
[SM_출고상세 D ⋈ SM_출고LOT L ⋈ SM_마트사발주홈플러스 HE]
    ↓
[LEFT JOIN 월품목별재고화일_LOT별_VIEW V + AND V.년월 = LEFT(D.출고일자, 6)]  → 출고월 1행만 JOIN (Step1)
    ↓
[OUTER APPLY (SELECT TOP 1 납기일자 FROM SM_수주상세 WHERE 마트사SEQ=HE.SEQ ORDER BY SEQ DESC) SD]
    → HE.SEQ당 SD 1행만 매칭 (Step2)
    ↓
[CONVERT(VARCHAR(8), SD.납기일자, 112) AS STORE_IN_DATE]   → YYYYMMDD 8자리 문자열 (Step3)
    ↓
[PDA 수신 → TB_SHIPMENT 중복 없이 INSERT, 라벨 날짜 정상 인쇄]
```

### 변경 전/후 비교

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| V 뷰 JOIN 조건 수 | 5개 | 6개 (`AND V.년월 = LEFT(D.출고일자, 6)` 추가) |
| SD 결합 방식 | `LEFT JOIN SM_수주상세 SD ON SD.마트사SEQ=HE.SEQ` (1:N 팽창 가능) | `OUTER APPLY (SELECT TOP 1 ... ORDER BY SEQ DESC) SD` (HE.SEQ당 1행 고정) |
| STORE_IN_DATE 형식 | datetime 원문 문자열 (`"2026-05-04 00:00:00.0"`) | `CONVERT(VARCHAR(8), SD.납기일자, 112)` → `"20260504"` |
| SELECT 컬럼 개수 | 24개 (인덱스 0~23) | 24개 (인덱스 0~23) — **변경 없음** |
| ProgressDlgShipSearch temp[] 파싱 인덱스 | 0~23 (searchType=2는 24~ 분기 없음) | 동일 — **변경 없음** |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_shipment_homeplus.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp` : 59줄 | `SD.납기일자 AS STORE_IN_DATE` → `CONVERT(VARCHAR(8), SD.납기일자, 112) AS STORE_IN_DATE` (오류34) |
| 2 | **search_shipment_homeplus.jsp** | 동일 파일 : 93~98줄 | V 뷰 LEFT JOIN에 `AND V.년월 = LEFT(D.출고일자, 6)` 1개 조건 추가 (오류32) |
| 3 | **search_shipment_homeplus.jsp** | 동일 파일 : 99~100줄 | `LEFT JOIN SM_수주상세 SD ON SD.마트사SEQ = HE.SEQ` → `OUTER APPLY (SELECT TOP 1 납기일자 FROM SM_수주상세 WHERE 마트사SEQ = HE.SEQ ORDER BY SEQ DESC) SD`로 전환 (오류33) |

---

## 4. 수정 상세

### 4.1 search_shipment_homeplus.jsp — V 뷰 년월 조건 추가 (오류 32)

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp` (93~98줄)

**변경 전:**

```sql
+ " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"
+ "   ON V.회사코드 = D.회사코드"
+ "  AND V.사업장 = D.출고사업장"
+ "  AND V.창고코드 = D.창고코드"
+ "  AND V.품목코드 = D.출고품목코드"
+ "  AND V.LOTNO = L.LOTNO"
```

**변경 후:**

```sql
+ " LEFT JOIN 월품목별재고화일_LOT별_VIEW V"
+ "   ON V.회사코드 = D.회사코드"
+ "  AND V.사업장 = D.출고사업장"
+ "  AND V.창고코드 = D.창고코드"
+ "  AND V.품목코드 = D.출고품목코드"
+ "  AND V.LOTNO = L.LOTNO"
+ "  AND V.년월 = LEFT(D.출고일자, 6)"     -- ★ 추가 (이마트 dce6722 패턴 포팅)
```

**검증**: 이마트 커밋 `dce6722`에서 이력번호 905002407409 기준 4행→2행 실측 완료된 것과 동일 패턴. 홈플러스도 동일 VIEW·동일 JOIN 키를 사용하므로 동일 원리로 적용. MSSQL weberp_dev에서 홈플러스 대상 실측 재확인 필요.

---

### 4.2 search_shipment_homeplus.jsp — SM_수주상세 LEFT JOIN → OUTER APPLY 전환 (오류 33)

**경로**: 동일 파일 (99~100줄)

**변경 전:**

```sql
+ " LEFT JOIN SM_수주상세 SD"
+ "   ON SD.마트사SEQ = HE.SEQ"
```

**변경 후:**

```sql
+ " OUTER APPLY (SELECT TOP 1 납기일자 FROM SM_수주상세"
+ "               WHERE 마트사SEQ = HE.SEQ ORDER BY SEQ DESC) SD"
```

**주의**:
- 별칭 `SD`는 그대로 유지하여 4.3(59줄 `SD.납기일자`)이 수정 없이 계속 참조되도록 한다.
- `ORDER BY SEQ DESC`는 오류 문서 33의 예시안이며, "최신 SEQ가 대표행"이라는 가정을 임의로 확정한 것이다. **SM_수주상세.SEQ가 실제 유니크 PK/시퀀스인지, 최신순=최신 SEQ가 업무적으로 맞는 대표값인지는 코드 반영 전 ERP 담당자 확인 또는 실측(HL_ERP 리포지토리/DB) 필요** — 이 문서는 확정이 아닌 계획안이다.
- `OUTER APPLY`는 SQL Server 전용 문법이며 FROM절에서 `HE`(SM_마트사발주홈플러스, 70~71줄)가 먼저 정의된 이후 위치(99번째 줄)에 있어야 `HE.SEQ`를 상관 참조할 수 있다 — 현재 위치(V JOIN 다음)는 이미 HE 이후이므로 위치 이동 불필요.
- SELECT절/WHERE절 전체에서 `SD`를 참조하는 곳이 59줄(`SD.납기일자 AS STORE_IN_DATE`) 외에 없는지 재확인 완료(JSP 전체 Read, 다른 참조 없음).

**검증**: SM_수주상세.마트사SEQ 유니크 여부, SEQ 컬럼 존재 여부를 코드 반영 전 실측(HL_ERP 리포지토리/DB SELECT)으로 확인.

---

### 4.3 search_shipment_homeplus.jsp — STORE_IN_DATE datetime CONVERT 적용 (오류 34)

**경로**: 동일 파일 (59줄)

**변경 전:**

```sql
+ ", SD.납기일자 AS STORE_IN_DATE"
```

**변경 후:**

```sql
+ ", CONVERT(VARCHAR(8), SD.납기일자, 112) AS STORE_IN_DATE"
```

**주의**: 4.2에서 SD가 `OUTER APPLY` 서브쿼리로 바뀌어도 별칭 `SD`와 컬럼명 `납기일자`는 동일하게 유지되므로 이 라인 자체의 수식(CONVERT 래핑)은 4.2 적용 여부와 무관하게 동일하게 작성한다. 단, **4.2가 먼저 반영되어야** 이 라인의 `SD.납기일자`가 "HE.SEQ당 1건으로 고정된 값"이 된다 — 4.3만 단독 적용하면 CONVERT는 정상 동작하지만 4.2(오류33)의 1:N 팽창 문제는 그대로 남는다. 따라서 Step 2 → Step 3 순서로 진행하고, Step 3 완료 후 반드시 최종 SELECT절에서 `SD.납기일자` 참조가 4.2의 OUTER APPLY 결과를 정확히 가리키는지 재확인한다.

**검증**: `SD.납기일자`가 NULL인 경우 CONVERT 결과도 NULL → LabelPrintHelper의 substring NULL 방어 여부는 별도 확인 필요(오류 34 문서의 "관련 문서" 22번 오류 7 참조, 이번 step 범위 아님).

---

## 5. 사이드이펙트

### 5.1 ProgressDlgShipSearch.java — temp[] 파싱 인덱스 (변경 없음)

```java
// ProgressDlgShipSearch.java:258~261
si.setPACKWEIGHT(temp[20].toString());       // 포장중량
si.setBARCODEGOODS(temp[21].toString());     // 바코드상품코드
si.setSTORE_IN_DATE(temp[22].toString());    // 입고일자
si.setEMARTLOGIS_CODE(temp[23].toString());  // 이마트물류코드
```

- Step 1~3은 SELECT 컬럼 개수·순서를 바꾸지 않는다(24개, 인덱스 0~23 유지). `Common.searchType.equals("2")`는 24번 이후 분기(265~286줄)에 해당하지 않으므로 홈플러스는 애초에 인덱스 0~23만 사용 — Step 적용 후에도 파싱 코드 변경 불필요.
- **검증 필수 항목**: Step 완료 후 실제 조회 결과의 `::` 구분 컬럼 수가 24개(세미콜론 앞 `;;`까지)인지 반드시 재확인한다.

### 5.2 LabelPrintHelper.java — setHomeplusPrinting() (Step 3만 해당)

```java
// LabelPrintHelper.java:1176~1178
Log.i(TAG, "=====================납품일자==================" + si.getSTORE_IN_DATE());
String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
```

- Step 3 적용 전: `STORE_IN_DATE`가 `"2026-05-04 00:00:00.0"` 형태 → `substring(0,4)="2026"`, `substring(4,6)="-0"`, `substring(6,8)="5-"` → 라벨에 `"2026년 -0월 5-일"`처럼 오인쇄되거나 최악의 경우 문자열 길이 부족으로 `StringIndexOutOfBoundsException` 발생 가능.
- Step 3 적용 후: `STORE_IN_DATE`가 `"20260504"` 8자리 → 정상적으로 `"2026년 05월 04일"` 형태로 파싱됨.
- 호출부: `BixolonShipmentActivity.java:936, 1701`, `ShipmentActivity.java:860, 1505` — Java 코드 수정 없음, JSP 응답 형식만 정상화.
- `setHomeplusPrinting()`은 searchType 2(홈플러스), 5(홈플러스비정량) 공통 사용 함수이나, 이번 문서는 **searchType=2(search_shipment_homeplus.jsp)만** 대상으로 한다. searchType=5(`search_homeplus_nonfixed.jsp`)의 동일 패턴(오류22 목록 기재)은 본 문서 범위 밖이며 별도 개발 문서로 다룬다.

### 5.3 IMPORT_ID_NO(V.이력번호) — Step 1과 무관 (참고)

```java
// LabelPrintHelper.java:1172
slcsCmd.append(slcsText(380, 361, 40, 40, String.valueOf(print_weight_double) + "/"+si.getIMPORT_ID_NO().substring(8, 12)));
```

- `IMPORT_ID_NO`(V.이력번호)도 substring을 사용하나 Step 1(년월조건 추가)은 값 자체의 포맷을 바꾸지 않고 중복 행 수만 정상화하므로 이 라인은 영향받지 않는다. NULL 발생 가능성(엣지케이스, 아래 5.4)만 확인 대상.

### 5.4 엣지케이스 — LEFT JOIN NULL 반환

| 상황 | 결과 | 영향 |
|------|------|------|
| 출고월에 V 뷰 재고행 없음 | `BL_NO`/`IMPORT_ID_NO` = NULL, `PACKWEIGHT` = COALESCE로 `I.박스중량` 대체 | 라벨 IMPORT_ID_NO substring(8,12) 시 NullPointerException 가능 — 기존에도 동일 리스크였으므로 이번 수정으로 신규 발생하는 것은 아님 |
| HE.SEQ에 SM_수주상세 행 자체가 없음 | OUTER APPLY 결과 NULL → `STORE_IN_DATE` = NULL → CONVERT(NULL,...) = NULL | `si.getSTORE_IN_DATE().substring(...)` 호출 시 NPE 가능 — 오류34 문서 "관련 문서" 22번 오류7과 연계, 이번 step 범위에서는 문서화만 하고 방어 코드 추가는 별도 판단 필요 |

---

## 6. 데이터 저장 구조

### 변수 매핑

| 변수 | 타입 | 용도 | 예시(변경 전) | 예시(변경 후) |
|------|------|------|--------------|--------------|
| `si.STORE_IN_DATE` | String | 홈플러스 라벨 납품일자 출력(YYYY년 MM월 DD일) | `"2026-05-04 00:00:00.0"` | `"20260504"` |
| `si.BL_NO` | String | 계근 전송 식별자 (V.BLNO/V.이력번호) | 중복 N행에 동일 값 반복 | 1행 |
| `si.IMPORT_ID_NO` | String | 이력번호 표시 (V.이력번호) | 중복 N행에 동일 값 반복 | 1행 |

### 인덱스 매핑 (변경 없음, 검증용)

```
temp[0]  = GI_D_ID            temp[12] = CENTERNAME
temp[1]  = ITEM_CODE          temp[13] = ITEM_SPEC
temp[2]  = ITEM_NAME          temp[14] = CT_CODE
temp[3]  = EMARTITEM_CODE     temp[15] = IMPORT_ID_NO
temp[4]  = EMARTITEM          temp[16] = PACKER_CODE
temp[5]  = GI_REQ_PKG         temp[17] = PACKER_PRODUCT_CODE
temp[6]  = GI_REQ_QTY         temp[18] = BARCODE_TYPE
temp[7]  = GI_REQ_DATE        temp[19] = ITEM_TYPE
temp[8]  = BL_NO              temp[20] = PACKWEIGHT
temp[9]  = BRAND_CODE         temp[21] = BARCODEGOODS
temp[10] = CLIENT_CODE        temp[22] = STORE_IN_DATE   ← Step3 형식만 변경(YYYYMMDD)
temp[11] = CLIENTNAME         temp[23] = EMARTLOGIS_CODE
```

---

## 7. 호출 시점

```
[PDA 홈플러스 출하대상 받기 버튼 클릭] (BixolonShipmentActivity, searchType=2)
    ↓
[ProgressDlgShipSearch.doInBackground()]
    ↓
[HttpURLConnection → search_shipment_homeplus.jsp 호출]
    파라미터: data = " AND D.회사코드='SGI' AND D.출고일자='YYYYMMDD' AND D.창고코드='...' ..."
    ↓
[search_shipment_homeplus.jsp 실행]
    ├── LEFT JOIN 월품목별재고화일_LOT별_VIEW V + 년월조건(Step1) → 행 중복 제거
    ├── OUTER APPLY SM_수주상세 SD(Step2) → HE.SEQ당 1행 고정
    └── CONVERT(VARCHAR(8), SD.납기일자, 112) AS STORE_IN_DATE(Step3) → YYYYMMDD 8자리
    ↓
[결과 :: / ;; 구분자로 PDA 전송, 24개 컬럼(인덱스 0~23)]
    ↓
[ProgressDlgShipSearch.onPostExecute()]
    → TB_SHIPMENT INSERT (중복 없음)
    → 화면 출하대상 목록 표시
    ↓
[계근 완료 → BixolonShipmentActivity → LabelPrintHelper.setHomeplusPrinting()]
    → si.getSTORE_IN_DATE().substring(0,4)/(4,6)/(6,8) 정상 파싱(YYYYMMDD 전제 충족)
    → 라벨 납품일자 정상 인쇄
```

---

## 8. 개발 플랜

### Step 1: V 뷰 년월 조건 추가 (오류 32)

**Part 1. 분석**
- 대상 파일: `search_shipment_homeplus.jsp`
- 범위: `LEFT JOIN 월품목별재고화일_LOT별_VIEW V` 절 (93~98줄)
- 용도: 년월 미조건으로 인한 출하대상 행 중복 제거 (이마트 dce6722 패턴 포팅)
- 주의할 점: LEFT JOIN 구조 유지(INNER JOIN 변경 금지), 기존 5개 조건 모두 보존, WHERE절로 조건 이동 금지

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | V 뷰 JOIN 조건 | 93~98줄 | 기존 5개 조건에 `AND V.년월 = LEFT(D.출고일자, 6)` 추가 |
| 2 | BL_NO/IMPORT_ID_NO/PACKWEIGHT 도출 방식 | 45, 52, 57줄 | 변경 없음(COALESCE 구조 그대로 유지) |

**Part 2. 변환 계획**
- 변환 방식: V 뷰 LEFT JOIN 블록에 6번째 조건 1행 삽입 (이마트 106줄과 동일 표현)
- 주의사항: 다른 JOIN(SD, M1/M2 등) 조건과 절대 혼동하지 않는다. 이 step은 93~98줄만 수정한다.

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인 (JSP — Tomcat 재배포 후 런타임 실행 확인)
- [ ] Part 5: 단위테스트 (MSSQL weberp_dev에서 동일 LOT 다월 재고행 케이스 실측: N행 → 1행 확인)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 2: SM_수주상세 LEFT JOIN → OUTER APPLY 서브쿼리 전환 (오류 33)

**Part 1. 분석**
- 대상 파일: `search_shipment_homeplus.jsp`
- 범위: `LEFT JOIN SM_수주상세 SD ON SD.마트사SEQ = HE.SEQ` (99~100줄)
- 용도: SD 1:N 관계로 인한 출하 건 중복(Cartesian product) 제거
- 주의할 점: 이마트에는 이 JOIN 구조 자체가 없어 참조할 기존 검증 코드가 없음 — 오류 문서 33의 예시안을 기반으로 하되 `ORDER BY SEQ DESC`(대표 행 선정 기준)는 **ERP 담당자 확인 또는 실측 필요**(임의 확정 금지). 별칭 `SD`는 4.3(59줄)과의 호환을 위해 유지.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | SD JOIN 구조 | 99~100줄 | `LEFT JOIN SM_수주상세 SD ON SD.마트사SEQ=HE.SEQ` → `OUTER APPLY (SELECT TOP 1 납기일자 FROM SM_수주상세 WHERE 마트사SEQ=HE.SEQ ORDER BY SEQ DESC) SD` |
| 2 | SD 참조 위치 전수 확인 | JSP 전체 | 59줄(`SD.납기일자`) 외 SD 참조 없음(Read로 확인 완료) — 신규 참조 발생 시 서브쿼리 SELECT 목록에 컬럼 추가 필요 |
| 3 | ORDER BY 기준 확정 | (외부) | SM_수주상세.SEQ 유니크/시퀀스 여부, "최신 SEQ=대표값" 업무 타당성 사전 확인 |

**Part 2. 변환 계획**
- 변환 방식: LEFT JOIN 절을 OUTER APPLY 상관 서브쿼리로 치환, 별칭 SD 유지
- 주의사항: OUTER APPLY는 FROM절에서 `HE`(70~71줄) 이후 위치해야 `HE.SEQ` 상관 참조가 유효 — 현재 위치(99줄)는 이미 조건 충족. 서브쿼리 내부 컬럼은 현재 SELECT절에서 사용하는 `납기일자` 1개만 포함(불필요한 컬럼 추가 금지).

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인 (ORDER BY 기준 확정 포함)
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인 (JSP — Tomcat 재배포 후 런타임 실행 확인)
- [ ] Part 5: 단위테스트 (동일 HE.SEQ에 SM_수주상세 N건 존재하는 케이스 실측: N행 → 1행 확인)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 3: STORE_IN_DATE datetime CONVERT 적용 (오류 34)

**Part 1. 분석**
- 대상 파일: `search_shipment_homeplus.jsp`
- 범위: `SD.납기일자 AS STORE_IN_DATE` (59줄)
- 용도: datetime 원문 문자열을 YYYYMMDD 8자리로 변환하여 `LabelPrintHelper.setHomeplusPrinting()`의 substring 파싱 전제 충족
- 주의할 점: **Step 2가 먼저 반영되어야** `SD.납기일자`가 HE.SEQ당 1건으로 고정된 값을 가리킴. Step 2 없이 Step 3만 단독 적용 시 CONVERT 자체는 정상 동작하지만 오류33(행 중복)은 해결되지 않음 — 두 step 순서를 반드시 준수

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | STORE_IN_DATE SELECT 표현식 | 59줄 | `SD.납기일자 AS STORE_IN_DATE` → `CONVERT(VARCHAR(8), SD.납기일자, 112) AS STORE_IN_DATE` |
| 2 | Step2와의 상호작용 | 59줄 + 99~100줄 | Step2에서 SD가 OUTER APPLY로 바뀌어도 별칭/컬럼명 동일 → 59줄 표현식은 Step2 여부와 무관하게 동일 코드로 작성 가능, 단 적용 순서는 Step2 → Step3 |
| 3 | NULL 케이스 | - | `SD.납기일자` NULL 시 CONVERT 결과도 NULL — LabelPrintHelper substring NULL 방어 여부는 별도 확인(본 step 범위 아님, 문서화만) |

**Part 2. 변환 계획**
- 변환 방식: SELECT절 59줄 표현식을 CONVERT(VARCHAR(8), ..., 112)로 래핑
- 주의사항: 다른 컬럼(EMARTLOGIS_CODE 등)과 줄 순서 혼동 금지, 컬럼 별칭(`AS STORE_IN_DATE`) 유지

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인 (JSP — Tomcat 재배포 후 런타임 실행 확인)
- [ ] Part 5: 단위테스트 (조회 결과 STORE_IN_DATE가 8자리 숫자 문자열인지 확인, 라벨 인쇄 시 substring 정상 동작 확인)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 4: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 홈플러스 출하대상 받기 — 복수 LOT/재고월 품목 조회 시 행 중복 없음 확인 (Step1) | [ ] |
| 2 | 동일 마트사SEQ(HE.SEQ)에 SM_수주상세 N건 케이스 — 출하 건 중복 없음 확인 (Step2) | [ ] |
| 3 | STORE_IN_DATE가 8자리 YYYYMMDD 문자열로 수신되는지 확인 (Step3) | [ ] |
| 4 | 계근 완료 후 홈플러스 라벨 "납품일자" 정상 인쇄 확인 (`YYYY년 MM월 DD일` 형식) | [ ] |
| 5 | 응답 컬럼 개수 24개(인덱스 0~23) 불변 확인 — `::` 구분 개수 카운트 | [ ] |
| 6 | ProgressDlgShipSearch 파싱 오류(ArrayIndexOutOfBoundsException 등) 미발생 확인 | [ ] |
| 7 | BL_NO/IMPORT_ID_NO/PACKWEIGHT 정상 출력 확인(Step1 영향) | [ ] |
| 8 | code-verifier 재실행 — 오류32/33/34 위험 해소 확인 | [ ] |
| 9 | original-comparator 재실행 — 비허용 차이 없음(회사코드/하드코딩 제외) 재확인 | [ ] |
| 10 | 홈플러스 출하계근 전체 테스트 시나리오 정상 통과 확인 | [ ] |

---

### 개발 순서 요약

```
Step 1: V 뷰 년월 조건 추가 (오류32)
    ↓
Step 2: SM_수주상세 LEFT JOIN → OUTER APPLY 전환 (오류33)
    ↓
Step 3: STORE_IN_DATE datetime CONVERT 적용 (오류34, Step2 선행 필수)
    ↓
Step 4: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 홈플러스 출하대상 조회 — 행 중복 없음 (Step1)

```
1. PDA 홈플러스 출하 화면 진입 (searchType=2)
2. 출고일자·창고 조건 설정 (동일 LOT이 2개월 이상 재고 이력을 가진 품목 포함)
3. "출하대상 받기" 버튼 클릭
4. 로컬DB TB_SHIPMENT 데이터 확인 — 동일 GI_D_ID 기준 중복 행 없는지 확인
5. 화면 표시 출하대상 목록에서 중복 행 없음 확인
```

### 시나리오 2: SM_수주상세 다중행 케이스 — 출하 건 중복 없음 (Step2)

```
1. 동일 마트사SEQ(HE.SEQ)에 SM_수주상세가 2건 이상 존재하는 테스트 데이터 준비
2. 홈플러스 출하대상 받기 실행
3. TB_SHIPMENT에서 해당 GI_D_ID가 1행만 존재하는지 확인
4. STORE_IN_DATE 값이 OUTER APPLY 대표 행(ORDER BY SEQ DESC) 기준과 일치하는지 확인
```

### 시나리오 3: STORE_IN_DATE 포맷 및 라벨 인쇄 (Step3)

```
1. 홈플러스 출하대상 받기 후 TB_SHIPMENT의 STORE_IN_DATE 값 확인
   - 8자리 숫자(YYYYMMDD) 형식인지 확인 (예: "20260504")
2. 계근 완료 후 홈플러스 라벨 출력
3. 라벨 "납품일자" 항목이 "YYYY년 MM월 DD일" 형식으로 정상 인쇄되는지 확인
4. SD.납기일자가 NULL인 케이스(SM_수주상세 미존재)에서 예외 발생 여부 확인(방어 코드 없으므로 발생 가능성 문서화)
```

### 시나리오 4: 통합 — 홈플러스 출하계근 전체 흐름

```
1. 출하대상 받기 → 계근 → 전송 → 라벨 인쇄까지 전체 흐름 수행
2. 각 단계에서 오류32/33/34 재발 여부 확인
3. code-verifier, original-comparator 재실행하여 정합성 재검증
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 출고월에 V 뷰 재고행 없는 LOT 발생 | V 뷰 해당 년월 행 미생성 | LEFT JOIN이므로 NULL 반환, BL_NO/IMPORT_ID_NO=NULL, PACKWEIGHT=I.박스중량 대체(기존 COALESCE 로직 유지). IMPORT_ID_NO.substring(8,12) 호출 시 NPE 가능성은 기존과 동일 리스크이므로 이번 수정 범위 아님 |
| 2 | SM_수주상세 ORDER BY 기준(SEQ DESC)이 업무 요건과 불일치할 가능성 | 오류 문서 33의 예시안을 임의 확정 없이 그대로 계획에 반영 | 코드 반영 전 ERP 담당자 확인 또는 HL_ERP 리포지토리/DB 실측으로 SEQ 컬럼 의미·유니크성 확인 후 확정 |
| 3 | SM_수주상세에 해당 HE.SEQ 매칭 행 자체가 없음 | 수주상세 미등록 케이스 | OUTER APPLY 결과 NULL → STORE_IN_DATE=NULL → substring 호출 시 NPE 가능. 방어 코드 추가 여부는 별도 오류 문서(22번 오류7) 연계 판단, 이번 step 범위 아님 |
| 4 | Step2·Step3 적용 순서 오류(Step3만 먼저 적용) | 두 step이 같은 SD 별칭을 공유 | 반드시 Step2 → Step3 순서로 진행, Step3 착수 전 Step2 체크리스트 완료 확인 |
| 5 | SELECT 컬럼 개수/순서 변경으로 인한 파싱 오류 | 수정 중 실수로 컬럼 추가/삭제/순서 변경 | 각 step 완료 후 응답 `::` 구분 개수가 24개(인덱스 0~23) 그대로인지 반드시 확인 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | V 뷰 년월 조건 추가 (오류32) | ⏳ 대기 |
| 2 | SM_수주상세 LEFT JOIN → OUTER APPLY 전환 (오류33) | ⏳ 대기 |
| 3 | STORE_IN_DATE datetime CONVERT 적용 (오류34) | ⏳ 대기 |
| 4 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- 오류: `app/doc/오류/32_홈플러스_월품목별재고_V뷰_년월조건_누락_출하대상_행중복[홈플러스_출하계근_AI정적검증].md`
- 오류: `app/doc/오류/33_홈플러스_SM_수주상세_LEFT_JOIN_다중행_팽창_출하건_중복[홈플러스_출하계근_AI정적검증].md`
- 오류: `app/doc/오류/34_홈플러스_STORE_IN_DATE_datetime_CONVERT_미적용_라벨_날짜_오인쇄[홈플러스_출하계근_AI정적검증].md`
- 참고: `app/doc/오류/22_잠재오류_18건_목록.md` (오류1: STORE_IN_DATE CONVERT 누락, 오류2: SM_수주상세 LEFT JOIN 팽창 최초 목록화)
- 참고: `app/doc/오류/25_출하대상_조회_행중복_월품목별재고화일LOT별VIEW_년월조건누락.md` (이마트 동일 원인, 수정 완료)
- 이마트 참조 개발 문서: `app/doc/개발/54_search_shipment_V뷰_년월조건_추가[출하대상_행중복].md` (커밋 `dce6722`)
- 이마트 JSP(참조): `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment.jsp`
- 수정 대상 JSP: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_shipment_homeplus.jsp`
- 참고자료: `app/doc/참고자료/오류패턴_분석.md` (패턴A 11번, 패턴C 13·14번, searchType=2 추가 체크 5·6번)

---

**문서 버전**: 1.0
