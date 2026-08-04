# 생산(searchType=1) 계근대상 컬럼 매핑 — Oracle → MSSQL(HL_ERP)

**작성일**: 2026-08-04
**최종 갱신**: 2026-08-04 (전환 완료 반영)
**대상 JSP**: `search_production.jsp`
**전환 전**: `VW_PDA_WID_PRO_LIST` (Oracle, 32개 출력)
**전환 후**: `PD_생산작업지시` + `PD_생산작업지시소요량` 기반 직접 JOIN (**25개 출력**)

---

## 1. 테이블 별칭

| 별칭 | 테이블 | 역할 | Oracle 대응 |
|:--:|------|------|-----------|
| `Q` | `PD_생산작업지시소요량` | 투입 자재 상세 (기준 테이블) | `W_GOODS_ID` |
| `H` | `PD_생산작업지시` | 작업지시 머리 | `W_GOODS_IH` (`GI_TYPE='M1'`) |
| `I` | `CO_품목코드` | 품목 마스터 | `B_ITEM` + `I_OFFER_D` |
| `V` | `월품목별재고화일_LOT별_VIEW` | LOT별 재고·이력 | `W_GOODS_R` (일부) |
| `G` | `CO_거래처MASTER` | 거래처 | `B_CLIENT` |
| `W` | `PD_생산계근` (신설) | 계근 실적 (`NOT EXISTS` 용) | `W_GOODS_WET` |

**제거된 Oracle 테이블**

| Oracle | 제거 사유 |
|--------|---------|
| `W_GOODS_R` | `BRAND_CODE`→`''`, `ITEM_SPEC`/`CT_CODE`→`''`, `이력번호`→`Q.이력번호` |
| `I_OFFER_D` | `PACKER_CODE`/`PACKER_PRODUCT_CODE`→`CO_품목코드.패커코드`/`.PPCODE` |
| `S_BARCODE_INFO` | `BARCODEGOODS` 서브쿼리 → `CO_품목코드.상품바코드` |

---

## 2. 25개 출력 컬럼 매핑 (확정)

**Index = `out.println` 출력 순서 = `ProgressDlgShipSearch.java` `temp[n]`**

| Index | 출력 별칭 | Oracle 산출식 | **MSSQL 산출식** | 생산 용도 |
|:-----:|---------|-------------|----------------|---------|
| 0 | `GI_D_ID` | `B.GI_D_ID` | `Q.SEQ` | 계근 조회 키 |
| 1 | `ITEM_CODE` | `B.ITEM_CODE` | `Q.자재코드` | 품목 식별 |
| 2 | `ITEM_NAME` | `I.ITEM_NAME_KR` | `I.품목명` | 화면 상품명 |
| 3 | `EMARTITEM_CODE` | `B.ITEM_CODE` | `''` | 로컬DB저장용 |
| 4 | `EMARTITEM` | `I.ITEM_NAME_KR` | `''` | 로컬DB저장용 |
| 5 | `GI_REQ_PKG` | `B.GI_REQ_PKG` | `CEILING(ROUND(Q.실소요량,2) / NULLIF(COALESCE(NULLIF(V.평균중량,0), I.박스중량),0))` | 완료 판정 |
| 6 | `GI_REQ_QTY` | `B.GI_REQ_QTY` | `Q.실소요량` | 요청 중량 |
| 7 | `GI_REQ_DATE` | `A.GI_REQ_DATE` | `''` | 로컬DB저장용 |
| 8 | `BL_NO` | `B.BL_NO` | `COALESCE(NULLIF(V.BLNO,''), NULLIF(Q.이력번호,''), '')` | 목록·BL 스피너 |
| 9 | `BRAND_CODE` | `R.BRAND_CODE` | `''` | 로컬DB저장용 |
| 10 | `CLIENT_CODE` | `A.CLIENT_CODE` | `''` | 로컬DB저장용 |
| 11 | `CLIENTNAME` | `B_CLIENT` 서브쿼리 | `ISNULL(G.상호, '')` | 작업지점 스피너·목록 |
| 12 | `CENTERNAME` | `'하이랜드푸드'` 고정 | `'하이랜드푸드'` 고정 | 계근 분기 |
| 13 | `ITEM_SPEC` | `R.ITEM_SPEC` | `''` | 로컬DB저장용 |
| 14 | `CT_CODE` | `R.CT_CODE` | `''` | 로컬DB저장용 |
| 15 | `IMPORT_ID_NO` | `DECODE(B.IMPORT_ID_NO,NULL,'0000',…)` | `COALESCE(NULLIF(Q.이력번호,''), '0000')` | 작업지점 스피너 |
| 16 | `PACKER_CODE` | `D.PACKER_CODE` | `I.패커코드` | 바코드 규칙 조회 |
| 17 | `PACKER_PRODUCT_CODE` | `DECODE(D.PACKER_PRODUCT_CODE,NULL,'0000',…)` | `COALESCE(NULLIF(I.PPCODE,''), '0000')` | 바코드 매칭·계근키 |
| 18 | `BARCODE_TYPE` | `'P0'` 고정 | `''` | 로컬DB저장용 |
| 19 | `ITEM_TYPE` | `DECODE(I.ITEM_TYPE,10,'S','J')` | `CASE WHEN I.비정량여부 = 1 THEN 'S' ELSE 'J' END` | **계근 방식 결정** |
| 20 | `PACKWEIGHT` | `I.PACK_WEIGHT` | `COALESCE(NULLIF(V.평균중량,0), I.박스중량)` | 중량 계산 |
| 21 | `BARCODEGOODS` | `DECODE(…,'0000000', S_BARCODE_INFO 서브쿼리)` | `COALESCE(NULLIF(I.상품바코드,''), '0000000')` | 바코드 매칭 |
| 22 | `STORE_IN_DATE` | `A.GI_DATE` | `''` | 로컬DB저장용 |
| 23 | `EMARTLOGIS_CODE` | `'0000000'` 고정 | `''` | 로컬DB저장용 |
| **24** | **`GI_L_ID`** | **없음 (신규)** | **`H.SEQ`** | **전송 키(지시SEQ)** |

---

## 3. 주요 결정 근거

### 3.1 `GI_D_ID` = `Q.SEQ` / `GI_L_ID` = `H.SEQ`

이마트는 **출고상세 1 : 출고LOT N** 구조라 두 키가 자연히 분리된다.

```sql
D.SEQ AS GI_D_ID     -- SM_출고상세
L.SEQ AS GI_L_ID     -- SM_출고LOT
```

생산은 `PD_생산작업지시소요량` **1행에 `LOTNO` 1개**라 LOT 펼침이 없다. `Q.SEQ`가 `@Id` + `GenerationType.SEQUENCE` PK이므로 그 자체로 유일하다.

전송 시 `insert_goods_wet_production.jsp`가 `splitData[0]→소요량SEQ`, `splitData[14]→지시SEQ`로 받으므로 두 키 모두 의미를 갖는다.

> 로컬 앱 동작만 보면 `GI_L_ID` 없이도 `Q.SEQ`가 유일해 문제없으나, 전송 JSP가 `Integer.parseInt(splitData[14])`를 수행하므로 빈값이면 전송이 실패한다.

### 3.2 `GI_REQ_PKG` / `GI_REQ_QTY`

`PD_생산작업지시소요량`에는 박스수 컬럼이 없다. ERP P0101 화면이 계산식으로 산출한다.

`P0101_SQL.xml:607~608`
```sql
SELECT NULLIF(COALESCE(NULLIF(S.평균중량, 0), I.박스중량), 0) AS 박스중량
     , CEILING(ROUND(PDR.실소요량, 2)
             / NULLIF(COALESCE(NULLIF(S.평균중량, 0), I.박스중량), 0)) AS 박스수량
```

이 식을 그대로 채용한다. `CEILING`이므로 정수가 되어 `ProgressDlgShipSearch:227`의 `(int) Double.parseDouble()` 및 `BixolonShipmentActivity:2309`의 `Integer.parseInt()`가 안전하다.

**`소요량`이 아닌 `실소요량`을 쓰는 근거** — `P0101_SQL.xml:231~232`
```sql
, MAX(PDR.소요량)     AS reqreQy      -- LOT 분할돼도 같은 값 반복 → MAX
, SUM(PDR.실소요량)   AS rlReqreQy    -- 행별 실제 배분량 → SUM
```

`P0101Service.java:222~226`도 재고 검증에 `실소요량`을 사용한다.

### 3.3 `ITEM_TYPE` = `CASE WHEN I.비정량여부 = 1 THEN 'S' ELSE 'J' END`

**값 체계** (`문의사항/01:15` 사용자 확인, `VW_PDA_WID_LIST:79` 주석)

| 값 | 의미 |
|:--:|---|
| `W` | 원료육 |
| `J` | 가공육(제품) |
| `B` | 비정량 |
| `S` | 저울 계근 |

**계근 동작 차이** (`BixolonShipmentActivity`)

| 값 | 행 | 중량 출처 | 제조일자·박스시리얼 |
|:--:|:--:|---|:--:|
| `S` | `:1406` | 바코드 실측 추출 + LB환산 + 소수점 2자리 | 바코드에서 추출 |
| `J` | `:1457` | `PACKWEIGHT` 고정값 | **없음** |

**이마트 경로 사용 불가** — 이마트는 `COALESCE(M1.타입구분, M2.타입구분)`(`CO_매출처품목코드매핑`)을 쓰나, 이 테이블은 `거래처코드`가 키에 포함된 **매출처별 매핑**이다. 생산은 사내 투입이라 해당 행이 존재할 이유가 없다.

**Oracle은 `B_ITEM.ITEM_TYPE`을 직접 사용** — `VW_PDA_WID_PRO_LIST:27`, `:42`, `:46`

`B_ITEM.ITEM_TYPE = 10`의 정확한 의미는 문서·소스 어디에도 없다(`developer_questions.md`에 질의로만 존재). **사용자 결정으로 `CO_품목코드.비정량여부 = 1`에 대응시킨다.**

### 3.4 `IMPORT_ID_NO` = `Q.이력번호`

`PD_생산작업지시소요량.이력번호`는 작업지시 생성 시 재고 VIEW에서 가져와 저장된다.

`P0101_SQL.xml:479` (`getP0104ReqreQyList`)
```sql
, V.이력번호      AS histNo        -- 월품목별재고화일_LOT별_VIEW 에서 조회
, V.LOTNO        AS lotNo
```

이후 `P0101_SQL.xml:238`(`MAX(PDR.이력번호)`), `:581`(WMS 자재이동)에서 소요량 테이블을 직접 읽으므로 값이 실재한다.

`procDrctMvmn()`이 WMS 이동 후 갱신하는 것은 **`창고코드`뿐**이며 `이력번호`는 건드리지 않는다.

### 3.5 `CENTERNAME` = `'하이랜드푸드'` (고정 유지)

Oracle 원본(`VW_PDA_WID_PRO_LIST:18`)이 하드코딩이다. 생산은 외부 납품처가 없어 자사명을 고정한다.

**`''`로 바꾸면 안 된다** — `BixolonShipmentActivity.java:1280`
```java
getCENTERNAME().substring(0, 3).equals(CENTER_NAME_ET)
```
길이 3 미만이면 `StringIndexOutOfBoundsException` 발생.

### 3.6 로컬DB 저장 전용 10개 → `''`

생산 로직 사용처가 0건이고 `DBHandler` INSERT에만 존재하는 컬럼은 `''`로 조회한다(사용자 결정).

| 컬럼 | 유일 사용처 | 비고 |
|---|---|---|
| `EMARTITEM_CODE` | `DBHandler:741, 1548, 1621, 1696` | `TB_GOODS_WET`까지 복사되나 미사용 |
| `EMARTITEM` | `DBHandler:742, 1549, 1622, 1697` | 〃 |
| `GI_REQ_DATE` | `DBHandler:745` | |
| `BRAND_CODE` | `DBHandler:747` | 이마트도 `''` |
| `CLIENT_CODE` | `DBHandler:748` | 조회 키 미사용 (§3.7) |
| `ITEM_SPEC` | `DBHandler:751` | |
| `CT_CODE` | `DBHandler:752` | `LabelPrintHelper` 전용 |
| `BARCODE_TYPE` | `DBHandler:756` | 〃 |
| `STORE_IN_DATE` | `DBHandler:760` | 〃 |
| `EMARTLOGIS_CODE` | `DBHandler:761` | 〃 |

생산은 라벨을 출력하지 않으므로(`BixolonShipmentActivity:519~521`, `:557`, `:563`) `LabelPrintHelper` 참조는 도달하지 않는다.

### 3.7 `CLIENT_CODE`는 계근 조회 키가 아니다

`selectqueryListGoodsWetInfo(context, gi_d_id, pp_code, client_code, gi_l_id)`는 파라미터를 4개 받지만 WHERE는 3개만 쓴다.

`DBHandler.java:1402~1405`
```sql
WHERE GI_D_ID = '…' AND GI_L_ID = '…' AND PACKER_PRODUCT_CODE = '…'
```

`selectqueryGoodsWet`(`:1266~1269`)도 동일하다. `TB_GOODS_WET`에 `CLIENT_CODE` 컬럼 자체가 없다.

### 3.8 `CLIENTNAME`은 유지 (화면 표시)

| 위치 | 화면 |
|---|---|
| `BixolonShipmentActivity:2299` | 작업지점 스피너 (`CLIENTNAME / IMPORT_ID_NO`) |
| `BixolonShipmentActivity:2467` | 작업지점 스피너 (BL 경로) |
| `BixolonShipmentActivity:3067` | 계근상세 팝업 지점명 |
| `ShipmentListAdapter:141` | 계근 목록 지점 컬럼 |

`H.거래처코드`는 P0101 화면에서 거래처 팝업으로 선택하는 항목이며, 없으면 `PD_생산계획.거래처코드`로 폴백된다(`P0101_SQL.xml:89`). 미입력 시 `LEFT JOIN` 미매칭 → `ISNULL(G.상호,'')` → `''`. 기능 영향은 없고 화면 지점명이 공백으로 보인다.

---

## 4. WHERE 조건 매핑 (확정)

### 4.1 Oracle 원본

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
  AND A.GI_REQ_DATE >= TO_CHAR(SYSDATE,'YYYYMMDD');
```

### 4.2 MSSQL 최종

```sql
  FROM PD_생산작업지시소요량 Q
 INNER JOIN PD_생산작업지시 H
         ON H.SEQ = Q.HSEQ
  JOIN CO_품목코드 I
         ON I.회사코드 = Q.회사코드
        AND I.품목코드 = Q.자재코드
  LEFT JOIN 월품목별재고화일_LOT별_VIEW V
         ON V.회사코드 = Q.회사코드
        AND V.사업장   = Q.사업장
        AND V.창고코드 = Q.창고코드
        AND V.품목코드 = Q.자재코드
        AND V.LOTNO   = Q.LOTNO
        AND V.년월     = LEFT(H.지시일자, 6)      -- ★ 필수
  LEFT JOIN CO_거래처MASTER G
         ON G.회사코드   = Q.회사코드
        AND G.거래처코드 = H.거래처코드
 WHERE Q.실소요량 <> 0
   AND I.대분류 = '2'                            -- 상품(투입 원료)만
   AND NOT EXISTS (
           SELECT 1 FROM PD_생산계근 W
            WHERE W.소요량SEQ = Q.SEQ
              AND W.회사코드  = Q.회사코드
       )
   + qry_where
 ORDER BY GI_D_ID ASC
```

### 4.3 조건별 대응

| Oracle 조건 | MSSQL | 근거 |
|---|---|---|
| `A.GI_TYPE = 'M1'` | 소멸 | `PD_` 테이블 자체가 생산. `SM_출고머리.출고구분`에 생산투입 값 없음 |
| `NVL(B.PROC_PUT_FLAG,'N')='N'` | **소멸** | ERP `불출여부` 컬럼은 존재하나 WHERE 필터 사용처 전체 0건. 사용자 제외 결정 |
| `B.PROC_DATE IS NULL` | 소멸 | 대응 컬럼 없음 |
| `A.SEND_FLAG = 'N'` | 소멸 | 의미 미확인. 이마트·홈플·비정량 VIEW도 주석 처리 |
| `R.CONTRACT_TYPE = '40'` | 소멸 | 입고 테이블 제거. 이마트 전환분에도 미존재 |
| `B.STATUS = '10'` | 소멸 | 대응 컬럼 없음 |
| `B.PACKING_QTY = 0` | **`NOT EXISTS (PD_생산계근)`** | `개발/46` NOT EXISTS 패턴 |
| `B.GI_REQ_PKG <> 0` | `Q.실소요량 <> 0` | |
| (원본 없음) | **`I.대분류 = '2'`** | **신규** — 계근 대상은 상품(생산 투입 원료)만. `1`(제품)은 생산 결과물이라 투입 대상이 아님 |
| `A.GI_REQ_DATE >= SYSDATE` | `qry_where`의 `H.지시일자 =` | 타 searchType과 동일하게 `=` 유지 |

> **`V.년월` 필수** — 누락 시 오류25(이마트)·오류32(홈플러스)와 동일한 출하대상 행중복 발생.

### 4.4 `qry_where` 별칭 치환

```java
qry_where = qry_where.replace("D.회사코드", "Q.회사코드")
                     .replace("D.출고일자", "H.지시일자");
```

앱이 보내는 값(`ProgressDlgShipSearch.java:107~108`)은 `SM_출고상세` 기준 별칭이므로 치환이 필요하다. `개발/35`(비정량)의 `ITEM_CODE → 품목코드` replace 선례를 따른다.

---

## 5. 제거되는 컬럼 (8개)

`개발/09_DB전용컬럼_삭제_가이드.md` 작업(로컬DB **v28**)으로 앱에서 이미 삭제되었다.

`GI_H_ID`, `EOI_ID`, `AMOUNT`, `GOODS_R_ID`, `GR_REF_NO`, `BRANDNAME`, `PACKERNAME`, `EMARTLOGIS_NAME`

`DBHelper.java:55`
```java
// v28: 8개 컬럼 삭제 (GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME)
```

원본 앱(`PDA-INNO(원본)/.../ProgressDlgShipSearch.java:157~215`)은 32개를 모두 파싱했다.

---

## 6. VIEW DDL 문서 정합성 주의

`app/doc/view/VW_PDA_WID_PRO_LIST`에 보관된 DDL은 **25개 컬럼**만 선언하나, 실제 JSP는 32개를 조회했다.

| 구분 | 컬럼 | 개수 |
|------|------|:--:|
| DDL 미선언인데 JSP 조회 | `GI_H_ID`, `EOI_ID`, `AMOUNT`, `GOODS_R_ID`, `GR_REF_NO`, `BRANDNAME`, `PACKERNAME`, `EMARTLOGIS_NAME` | 8 |
| DDL 선언인데 JSP 미조회 | `GR_WAREHOUSE_CODE` | 1 |

`ORDER BY EOI_ID ASC`의 `EOI_ID`도 DDL에 없다. **보관 DDL은 운영 VIEW보다 이전 버전이므로 전환 기준으로 삼으면 안 된다.**

---

## 7. 실행 검증 필요 항목

정적 분석으로 확인 불가한 항목이다. Tomcat 로그(`##search_production query :` / `##search_production SQLException :`)로 확인한다.

| # | 항목 | 실패 시 대응 |
|:-:|---|---|
| 1 | `Q.회사코드` 컬럼 존재 (`BaseEntity` 상속분, ERP 매퍼 사용 사례 미확인) | `H.회사코드`로 일괄 교체 |
| 2 | `V.BLNO` 컬럼 존재 (이마트 JSP 사용 사실로만 확인) | `BL_NO`를 `COALESCE(NULLIF(Q.이력번호,''),'')`로 축소 |
| 3 | `I.비정량여부` 값 분포 | `ITEM_TYPE` `S`/`J` 판정 재검토 |

> `NULL` 유입 위험은 없다. 기저 컬럼이 모두 `nullable = false`이고, `LEFT JOIN` 미매칭분은 `COALESCE`/`ISNULL` 폴백으로 처리된다. `CO_품목코드.박스중량`도 `nullable = false` + 업무상 필수 입력이므로 `NULLIF(…, 0)`이 `NULL`을 만들지 않는다.

---

## 8. 관련 문서

- `app/doc/개발/59_생산_계근대상받기_JSP_MSSQL전환.md` — 개발 가이드 (Step 설계·체크리스트)
- `app/doc/소스분석/57_생산_계근대상받기_JSP_컬럼_사용분석.md` — 32개 컬럼 사용처 전수 추적
- `app/doc/column/05_VW_PDA_WID_PRO_LIST.md` — Oracle VIEW 33개 컬럼 사용여부
- `app/doc/column/10_VW_PDA_WID_LIST.md` — 이마트 컬럼 정의 (전환 선례)
- `app/doc/view/VW_PDA_WID_PRO_LIST` — Oracle DDL (25컬럼 구버전, §6 참조)
- `app/doc/개발/09_DB전용컬럼_삭제_가이드.md` — 8개 컬럼 삭제 근거
- `app/doc/개발/31_GI_L_ID_추가[GI_D_ID_비유니크_전체_영향범위].md` — `GI_L_ID` 도입 배경
- `app/doc/개발/46_정량_비정량_이마트_미계근조건_NOT_EXISTS_추가.md` — `NOT EXISTS` 패턴 선례
- `app/doc/개발/35_비정량_출하계근대상_JSP_MSSQL전환.md` — `qry_where` replace 선례
- `app/doc/오류/25_출하대상_조회_행중복_월품목별재고화일LOT별VIEW_년월조건누락.md` — `V.년월` 근거
- `app/doc/문의사항/01_비정량_출하대상_타입구분_J코드_포함여부.md` — `W`/`J`/`B` 값 체계 확인
- `app/doc/소스분석/48_8개_출하대상받기_VIEW_WHERE조건_비교.md` — §1.2 생산 WHERE 조건
