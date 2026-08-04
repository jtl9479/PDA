# 생산(searchType=1) 계근대상받기 JSP MSSQL 전환

**작성일**: 2026-08-04
**목적**: `search_production.jsp`의 Oracle VIEW(`VW_PDA_WID_PRO_LIST`) 의존성을 제거하고 HL_ERP 생산 모듈(`PD_`) 기반 직접 JOIN 쿼리로 교체한다. 계근 결과 적재 테이블도 `SM_출고계근`(영업 출고)에서 `PD_생산계근`(신설)으로 분리하고, 계근 이력 조회 JSP도 함께 분리한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

### 추가 제약 조건 (이 가이드 한정)

- 본 작업 범위는 **생산(searchType=1)** 계근 흐름에 한정한다. 생산라벨(7)은 전송 분기만 함께 처리하고 조회 JSP(`search_production_4label.jsp`)는 제외한다.
- 생산계근계산(`search_production_calc.jsp` / `ProductionActivity`)은 독립 화면이므로 별도 가이드로 분리한다.
- 다른 searchType(0·2·3·4·5·6)의 동작에 영향을 주지 않는다.

---

## 1. 현재 구조

### 1.1 search_production.jsp (전환 전)

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_production.jsp`

```java
conn = getMSSQLConnection();          // 접속만 MSSQL 전환 완료

String qry_where = request.getParameter("data");
System.out.println("##search_production all parameter :" + qry_where);   // 로그만, 쿼리 미적용

String quertystring = "SELECT "
        + "GI_H_ID" + ", GI_D_ID" + ", EOI_ID" + …    // 32개
        + " FROM VW_PDA_WID_PRO_LIST"                  // Oracle VIEW
        + " ORDER BY EOI_ID ASC";
```

### 1.2 문제점

| # | 문제 | 영향 |
|:-:|------|------|
| 1 | `VW_PDA_WID_PRO_LIST`는 MSSQL에 존재하지 않음 | **조회 시 SQL 오류** |
| 2 | 출력 컬럼 32개 — 앱은 24개 표준 레이아웃 기대 | `temp[]` 인덱스 전체 밀림 |
| 3 | `qry_where` 미적용 | 회사코드·일자 필터 없이 전량 조회 |
| 4 | `GI_L_ID` 미전송 | 전송 시 `Integer.parseInt("")` 예외 |
| 5 | 계근 결과가 `SM_출고계근`(영업 출고)로 적재 | **키 공간 충돌** |

**2번 상세** — `ProgressDlgShipSearch.java:222`가 `temp[0]`을 `GI_D_ID`로 기대하나 JSP는 `GI_H_ID`를 보냄. `temp[5]`(`GI_REQ_PKG`)에 `EMARTITEM_CODE`가 들어가 `Double.parseDouble()` 실패 또는 무증상 데이터 오염.

**5번 상세** — `SM_출고계근.출고상세SEQ`/`출고LOTSEQ`는 `SM_출고상세`/`SM_출고LOT` 참조 키다. `PD_생산작업지시소요량.SEQ`를 넣으면 값이 겹쳐 ERP 조회 시 생산·출고 데이터가 뒤섞인다.

### 1.3 UI 노출 상태

| 버튼 | searchType | `activity_main.xml` | 노출 |
|---|:--:|---|:--:|
| 생산계근대상받기 (`btnproductionlist`) | 1 | `:261` | ✅ |
| 생산계근입력시작 (`btnProdWet`) | 1 | `:281` | ✅ |
| 생산대상받기(라벨) (`btnproductionlist4print`) | 7 | `:428` TableRow `visibility="gone"` | ❌ |
| 생산입력시작(라벨) (`btnProdWet4print`) | 7 | `:448` 〃 | ❌ |

searchType=1은 화면에 노출되어 있으므로 사용자가 누르면 즉시 오류를 만난다.

---

## 2. 변경 구조

### 데이터 흐름

```
P0101 생산작업지시 (ERP)
    ↓  PD_생산작업지시 + PD_생산작업지시소요량 INSERT
[생산계근대상받기]  btnproductionlist
    ↓  search_production.jsp  ★ Step 1~3
    ↓  GI_D_ID = Q.SEQ / GI_L_ID = H.SEQ, 25개 컬럼
ProgressDlgShipSearch  ★ Step 4
    ↓  temp[0]~temp[24] 파싱
TB_SHIPMENT (SQLite)
    ↓
ProgressDlgBarcodeSearch → ProgressDlgGoodsWetSearch  ★ Step 7
    ↓  search_goods_wet_production.jsp  → PD_생산계근 계근이력 복원
TB_GOODS_WET (SQLite)
    ↓
[생산계근입력시작]  btnProdWet
    ↓  BixolonShipmentActivity (프린터 비활성)
    ↓  바코드 스캔 → 중량 추출
TB_GOODS_WET (SQLite)
    ↓  [전송]
insert_goods_wet_production.jsp  ★ Step 5
    ↓  splitData[0]→소요량SEQ, splitData[14]→지시SEQ
PD_생산계근 (MSSQL)  ★ Step 0
```

### 테이블 대응

| Oracle | HL_ERP | 근거 |
|---|---|---|
| `W_GOODS_IH` (`GI_TYPE='M1'`) | `PD_생산작업지시` | `SM_출고머리.출고구분` 실사용값 `1`/`2`/`3`에 생산투입 없음 |
| `W_GOODS_ID` | `PD_생산작업지시소요량` | `P0101Service.java:213` 이 `불출여부='N'` 세팅 |
| `B_ITEM` | `CO_품목코드` | 기확립 |
| `W_GOODS_R` | **제거** → `월품목별재고화일_LOT별_VIEW` + `CO_품목코드` | 이마트 전환 선례 |
| `I_OFFER_D` | **제거** → `CO_품목코드.패커코드`/`.PPCODE` | 이마트 전환 선례 |
| `B_CLIENT` | `CO_거래처MASTER` | |
| `W_GOODS_WET` | **`PD_생산계근`(신설)** | §5.1 |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **PrdctnWeighEntity.java** | `HL_ERP/src/main/java/com/sgis/domain/pd/prodwork/entity/` | 신규 — `PD_생산계근` |
| 2 | **PrdctnWeighRepository.java** | `HL_ERP/src/main/java/com/sgis/web/pd/prodwork/repository/` | 신규 |
| 3 | **search_production.jsp** | `Tomcat/webapps/ROOT/inno/` | 쿼리 전면 교체, 32→25개 |
| 4 | **insert_goods_wet_production.jsp** | 〃 | 신규 — `PD_생산계근` INSERT |
| 5 | **Common.java** | `common/Common.java:32` | URL 상수 추가 |
| 6 | **BixolonShipmentActivity.java** | `:2755` | 생산(1)·생산라벨(7) 전송 분기 분리 |
| 7 | **ProgressDlgShipSearch.java** | `:289` | searchType=1 파싱 분기 추가 |
| 8 | **search_goods_wet_production.jsp** | `Tomcat/webapps/ROOT/inno/` | 신규 — `PD_생산계근` 계근이력 조회 |
| 9 | **ProgressDlgGoodsWetSearch.java** | `:75~81` | 생산(1)·생산라벨(7) 조회 JSP 분기 |

---

## 4. 수정 상세

### 4.1 search_production.jsp — SELECT 25개 컬럼

**Index = `out.println` 출력 순서 = `ProgressDlgShipSearch` `temp[n]`**

| Idx | 컬럼 | Oracle | **MSSQL** | 용도(생산 기준) |
|:--:|---|---|---|---|
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
| 12 | `CENTERNAME` | `'하이랜드푸드'` | `'하이랜드푸드'` | 계근 분기 |
| 13 | `ITEM_SPEC` | `R.ITEM_SPEC` | `''` | 로컬DB저장용 |
| 14 | `CT_CODE` | `R.CT_CODE` | `''` | 로컬DB저장용 |
| 15 | `IMPORT_ID_NO` | `DECODE(B.IMPORT_ID_NO,NULL,'0000',…)` | `COALESCE(NULLIF(Q.이력번호,''), '0000')` | 작업지점 스피너 |
| 16 | `PACKER_CODE` | `D.PACKER_CODE` | `I.패커코드` | 바코드 규칙 조회 |
| 17 | `PACKER_PRODUCT_CODE` | `DECODE(D.PACKER_PRODUCT_CODE,NULL,'0000',…)` | `COALESCE(NULLIF(I.PPCODE,''), '0000')` | 바코드 매칭·계근키 |
| 18 | `BARCODE_TYPE` | `'P0'` | `''` | 로컬DB저장용 |
| 19 | `ITEM_TYPE` | `DECODE(I.ITEM_TYPE,10,'S','J')` | `CASE WHEN I.비정량여부 = 1 THEN 'S' ELSE 'J' END` | **계근 방식 결정** |
| 20 | `PACKWEIGHT` | `I.PACK_WEIGHT` | `COALESCE(NULLIF(V.평균중량,0), I.박스중량)` | 중량 계산 |
| 21 | `BARCODEGOODS` | `S_BARCODE_INFO` 서브쿼리 | `COALESCE(NULLIF(I.상품바코드,''), '0000000')` | 바코드 매칭 |
| 22 | `STORE_IN_DATE` | `A.GI_DATE` | `''` | 로컬DB저장용 |
| 23 | `EMARTLOGIS_CODE` | `'0000000'` | `''` | 로컬DB저장용 |
| **24** | **`GI_L_ID`** | **없음** | **`H.SEQ`** | **전송 키(지시SEQ)** |

**제거된 8개** — `개발/09_DB전용컬럼_삭제_가이드.md` (로컬DB v28)로 앱에서 이미 삭제

`GI_H_ID`, `EOI_ID`, `AMOUNT`, `GOODS_R_ID`, `GR_REF_NO`, `BRANDNAME`, `PACKERNAME`, `EMARTLOGIS_NAME`

**`''` 처리 10개** — 생산 로직 사용처 0건, `DBHandler` INSERT에만 존재

`EMARTITEM_CODE`, `EMARTITEM`, `GI_REQ_DATE`, `BRAND_CODE`, `CLIENT_CODE`, `ITEM_SPEC`, `CT_CODE`, `BARCODE_TYPE`, `STORE_IN_DATE`, `EMARTLOGIS_CODE`

> `BARCODE_TYPE`·`STORE_IN_DATE`·`EMARTLOGIS_CODE`·`CT_CODE`는 `LabelPrintHelper` 전용인데, 생산은 라벨을 출력하지 않아(`BixolonShipmentActivity.java:519~521`, `:557`, `:563`) 도달하지 않는다.
> `CENTERNAME`은 `''` 불가 — `BixolonShipmentActivity.java:1280`의 `substring(0, 3)`에서 `StringIndexOutOfBoundsException` 발생.

### 4.2 search_production.jsp — FROM / WHERE

**변경 전:**
```sql
FROM VW_PDA_WID_PRO_LIST
ORDER BY EOI_ID ASC
```

**변경 후:**
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

**WHERE 조건 대응**

| Oracle | MSSQL | 근거 |
|---|---|---|
| `A.GI_TYPE = 'M1'` | 소멸 | `PD_` 테이블 자체가 생산 |
| `A.SEND_FLAG = 'N'` | 소멸 | 대응 컬럼 없음. 이마트·홈플·비정량 VIEW도 주석 처리(`--AND IH.SEND_FLAG = 'Y'`) |
| `R.CONTRACT_TYPE = '40'` | 소멸 | 입고 테이블 제거. 이마트 전환분에도 미존재 |
| `B.STATUS = '10'` | 소멸 | 대응 컬럼 없음 |
| `B.PACKING_QTY = 0` | **`NOT EXISTS (PD_생산계근)`** | `개발/46` NOT EXISTS 패턴 |
| `B.GI_REQ_PKG <> 0` | `Q.실소요량 <> 0` | |
| (원본 없음) | **`I.대분류 = '2'`** | **신규 조건** — §5.4 참조 |
| `B.PROC_DATE IS NULL` | 소멸 | 대응 컬럼 없음 |
| `NVL(B.PROC_PUT_FLAG,'N')='N'` | **소멸** | ERP에 `불출여부` 컬럼은 있으나 WHERE 필터 사용처가 전체 0건. 사용자 제외 결정 |
| `A.GI_REQ_DATE >= SYSDATE` | `qry_where`의 `H.지시일자 =` | 앱이 선택 일자 전달. 타 searchType과 동일 |

> **`V.년월` 조건 필수** — 누락 시 오류25(이마트)·오류32(홈플러스)와 동일한 출하대상 행중복 발생.

### 4.3 search_production.jsp — qry_where 별칭 치환

**변경 전:** 파라미터를 받아 로그만 출력

**변경 후:**
```java
String qry_where = request.getParameter("data");

if (qry_where != null) {
	qry_where = qry_where.replace("D.회사코드", "Q.회사코드")
						 .replace("D.출고일자", "H.지시일자");
} else {
	qry_where = "";
}
```

앱이 보내는 값 (`ProgressDlgShipSearch.java:107~108`)
```
 AND D.회사코드 = '20' AND D.출고일자 = '20260804'
```

`D.` 는 `SM_출고상세` 기준 별칭이라 생산 쿼리에 그대로 쓸 수 없다. `개발/35`(비정량)의 `ITEM_CODE → 품목코드` replace 선례를 따른다.

**검증**: Tomcat 로그 `##search_production query :` 에서 치환 결과 확인

### 4.4 insert_goods_wet_production.jsp (신규)

`insert_goods_wet_new.jsp`를 원본으로 하고 적재 테이블·참조 키만 교체한다.

```sql
INSERT INTO PD_생산계근(SEQ, 소요량SEQ, 지시SEQ, 계근중량, 계근중량단위, PPCODE,
                        계근바코드, 패커코드, 제조일자, 박스시리얼, 계근순번,
                        등록사원, 등록일자, 등록시간, 회사코드, 수정사원, 수정일자, 수정시간)
VALUES (NEXT VALUE FOR PD_PRDCTN_WEIGH_SEQ,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
```

**패킷 매핑** (`BixolonShipmentActivity.java:2716~2730`, 행 `##` / 컬럼 `::`)

| `splitData` | PDA 필드 | `PD_생산계근` |
|:--:|---|---|
| `[0]` | `GI_D_ID` | **`소요량SEQ`** (= `Q.SEQ`) |
| `[1]` | `WEIGHT` | `계근중량` |
| `[2]` | `WEIGHT_UNIT` | `계근중량단위` |
| `[3]` | `PACKER_PRODUCT_CODE` | `PPCODE` |
| `[4]` | `BARCODE` | `계근바코드` |
| `[5]` | `PACKER_CLIENT_CODE` | `패커코드` |
| `[6]` | `MAKINGDATE` | `제조일자` |
| `[7]` | `BOXSERIAL` | `박스시리얼` |
| `[8]` | `BOX_CNT` | `계근순번` |
| `[9]` | `REG_ID` | `등록사원`·`수정사원` |
| `[10]` | 회사코드 | `회사코드` |
| `[11]~[13]` | `BRAND_CODE`·`CLIENT_TYPE`·`BOX_ORDER` | 미사용 |
| `[14]` | `GI_L_ID` | **`지시SEQ`** (= `H.SEQ`) |

**계근중량 `(값 * 100) / 100.0`** — 원본 식 유지. 앱(`BixolonShipmentActivity.java:660` `Math.floor(w*10)`)에서 이미 버림 처리되므로 JSP는 무연산 통과. `오류/31` 확정 판정(버림이 정상 사양) 준수.

### 4.5 Common.java

**변경 후:**
```java
public static final String URL_INSERT_GOODS_WET_PRODUCTION = BASE_URL + "/insert_goods_wet_production.jsp";
```

### 4.6 BixolonShipmentActivity.java:2755

**변경 전:**
```java
}else if(Common.searchType.equals(SEARCH_TYPE_PRODUCTION) || Common.searchType.equals(SEARCH_TYPE_NONFIXED)|| Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)|| Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)){
    result = …sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_NEW);
}
```

**변경 후:**
```java
}else if(Common.searchType.equals(SEARCH_TYPE_PRODUCTION) || Common.searchType.equals(SEARCH_TYPE_PRODUCTION_LABEL)){   // 생산(1), 생산라벨(7) : PD_생산계근 적재
    Log.i(TAG, "===================send packet 확인==================" + packet);
    result = …sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_PRODUCTION);
}else if(Common.searchType.equals(SEARCH_TYPE_NONFIXED)|| Common.searchType.equals(SEARCH_TYPE_HOMEPLUS_NONFIXED)){
    Log.i(TAG, "===================send packet 확인==================" + packet);
    result = …sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_NEW);
}
```

**검증**: 비정량(4)·홈플비정량(5)·도매(3)는 기존 `URL_INSERT_GOODS_WET_NEW` 유지 → 회귀 없음

### 4.7 ProgressDlgShipSearch.java:289

**변경 후:**
```java
// 생산(1): 추가 1개 필드 (GI_L_ID = 생산작업지시SEQ)
} else if(Common.searchType.equals("1")) {
    si.setGI_L_ID(temp[24].toString());          // 생산작업지시SEQ
}
```

홈플러스(2)와 동일 패턴. 기존 분기(0·4·2·5·6) 미변경.

---

### 4.8 search_goods_wet_production.jsp (신규)

계근 이력을 서버에서 PDA로 복원하는 조회다. `ProgressDlgBarcodeSearch:201` 완료 후 `ProgressDlgGoodsWetSearch`가 호출하며, 앱 재설치·기기 교체 시 기존 계근분을 되살린다.

`search_goods_wet.jsp`와 **출력 14개 컬럼 순서는 동일**하게 유지하고(`ProgressDlgGoodsWetSearch`가 `temp[0]~temp[13]` 파싱) 조회 대상만 교체한다.

| 항목 | `search_goods_wet.jsp` | **생산용** |
|---|---|---|
| 테이블 | `SM_출고계근` | **`PD_생산계근`** |
| `GI_D_ID` | `출고상세SEQ` | **`소요량SEQ`** |
| `GI_L_ID` | `출고LOTSEQ` | **`지시SEQ`** |
| `PACKER_PRODUCT_CODE` | `ppCode` | `PPCODE` |
| 나머지 10개 | 동일 | 동일 |

**WHERE 컬럼명 치환**

앱이 `ProgressDlgGoodsWetSearch.java:62~66`에서 `SM_출고계근` 기준 컬럼명으로 조건을 만든다.

```java
data = data + "출고상세SEQ = '" + list_id_info.get(i) + "' OR ";
```

JSP에서 치환한다.

```java
qry_where = qry_where.replace("출고상세SEQ", "소요량SEQ");
```

### 4.9 ProgressDlgGoodsWetSearch.java:75~81

**변경 전:**
```java
receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_goods_wet", Common.URL_SEARCH_GOODS_WET);
```

**변경 후:**
```java
// 생산(1), 생산라벨(7) : 계근 이력이 PD_생산계근에 적재되므로 전용 JSP 사용
if(Common.searchType.equals("1") || Common.searchType.equals("7")) {
    receiveData = …sendDataDb(data, "inno", "search_goods_wet", Common.URL_SEARCH_GOODS_WET_PRODUCTION);
} else {
    receiveData = …sendDataDb(data, "inno", "search_goods_wet", Common.URL_SEARCH_GOODS_WET);
}
```

`Common.java:30`에 상수 추가
```java
public static final String URL_SEARCH_GOODS_WET_PRODUCTION = BASE_URL + "/search_goods_wet_production.jsp";
```

**검증**: 0·2·3·4·5·6은 기존 `URL_SEARCH_GOODS_WET` 유지 → 회귀 없음

---

## 5. 사이드이펙트

### 5.1 PD_생산계근 신설 (ERP 스키마 변경)

`SM_출고계근`을 재사용하지 않은 이유

| # | 사유 |
|:-:|---|
| 1 | `출고상세SEQ`/`출고LOTSEQ`는 `SM_출고상세`/`SM_출고LOT` 참조 키 → `PD_` SEQ 투입 시 **키 공간 충돌** |
| 2 | `PD_생산자재사용일보`는 생산실적 등록(PDA 계근 **이후**) 시점 생성 → 계근 시점에 SEQ 확보 불가 |
| 3 | `DlivyWeighEntity`가 `com.sgis.domain.sm.release` 패키지 — 영업 출고 모듈 |

`SM_출고계근`과 컬럼 구성은 동일하고 참조 키만 교체했다.

| `SM_출고계근` | `PD_생산계근` |
|---|---|
| `출고상세SEQ` | `소요량SEQ` (`PD_생산작업지시소요량.SEQ`) |
| `출고LOTSEQ` | `지시SEQ` (`PD_생산작업지시.SEQ`) |
| 나머지 9개 + `BaseEntity` 공통 7개 | 동일 |

`application.yml:93~94` `hbm2ddl.auto: update` — ERP 재기동 시 테이블 자동 생성.

### 5.2 생산라벨(7) 전송 경로 변경

`URL_INSERT_GOODS_WET_NEW` → `URL_INSERT_GOODS_WET_PRODUCTION`

searchType=7은 현재 UI가 `visibility="gone"`이라 실행되지 않으므로 즉시 영향은 없다. 7번 개발 시 조회 JSP(`search_production_4label.jsp`)도 `GI_D_ID`/`GI_L_ID`를 `Q.SEQ`/`H.SEQ`로 맞춰야 한다.

### 5.4 대분류 필터 신규 추가

```sql
AND I.대분류 = '2'
```

| 대분류 | 의미 | 생산에서의 역할 |
|:--:|---|---|
| `1` | 제품 | 생산 **결과물** — `PD_생산작업지시.품목코드` |
| **`2`** | **상품** | **생산 투입 원료** — `PD_생산작업지시소요량.자재코드` |

계근 대상은 생산에 투입하는 상품이다. 조건이 없으면 원자재·부자재·반제품이 함께 조회된다.

> Oracle 원본(`VW_PDA_WID_PRO_LIST`)에는 없던 조건이다. 원본은 `R.CONTRACT_TYPE = '40'`·`B.STATUS = '10'` 등 다른 축으로 대상을 좁혔으나 ERP에 대응 컬럼이 없어 소멸했고, 그 결과 조회 범위가 넓어진 것을 `대분류`로 보정한다.

---

### 5.3 원본 대비 동작 변경 3건

| # | 항목 | 원본 | 전환 후 | 근거 |
|:-:|---|---|---|---|
| 1 | 조회 일자 범위 | `GI_REQ_DATE >= 오늘` | `지시일자 = 선택일자` | 타 searchType 전환분과 동일 |
| 2 | `GI_L_ID` 전송 | 없음 | `H.SEQ` | `insert_goods_wet_production.jsp`가 `지시SEQ`로 사용. `개발/31` 전 searchType 확대 방침 |
| 3 | 로컬DB 저장 전용 10개 컬럼 | 실제 값 | `''` | 생산 로직 사용처 0건 (사용자 결정) |

---

## 6. 데이터 저장 구조

### 인덱스 매핑

```
JSP out.println[0]  = GI_D_ID              ↔  temp[0]  → si.setGI_D_ID              (:222)
JSP out.println[1]  = ITEM_CODE            ↔  temp[1]  → si.setITEM_CODE            (:223)
…
JSP out.println[23] = EMARTLOGIS_CODE      ↔  temp[23] → si.setEMARTLOGIS_CODE      (:272)
JSP out.println[24] = GI_L_ID              ↔  temp[24] → si.setGI_L_ID              (:291)
```

### 전송 패킷 매핑

```
packet[0]  = GI_D_ID   → splitData[0]  → PD_생산계근.소요량SEQ
packet[14] = GI_L_ID   → splitData[14] → PD_생산계근.지시SEQ
```

---

## 7. 호출 시점

```
[MainActivity]
    ├── btnproductionlist 클릭          MainActivity.java:268
    │       ↓  downloadShipmentList(SEARCH_TYPE_PRODUCTION, "생산대상받기")
    │       ├── Common.searchType = "1"                     :467
    │       ├── TB_SHIPMENT / TB_BARCODE_INFO / TB_GOODS_WET 전체 삭제   :470, 485, 486
    │       └── new ProgressDlgShipSearch(this).execute()   :489
    │               ↓  doInBackground (백그라운드)
    │               ├── WHERE 생성 (회사코드 + 출고일자)      :107~108
    │               ├── ★ search_production.jsp 호출         :129
    │               ├── split(";;") → split("::", -1)        :204, 217
    │               ├── temp[0]~temp[24] 파싱                :222~291
    │               └── refreshShipmentList() → TB_SHIPMENT  :364
    │
    └── btnProdWet 클릭                  MainActivity.java:312
            ↓  startWeighing(SEARCH_TYPE_PRODUCTION, …)      :430
            ↓  BixolonShipmentActivity (activity_shipment.xml)
            ├── 프린터 강제 비활성                            :519~521
            ├── 바코드 스캔 → ProgressDlgShipSelect            :1087
            ├── ITEM_TYPE 분기 → 중량 추출                    :1353~1465
            ├── TB_GOODS_WET INSERT                          :1594~1610
            └── [전송] → ★ insert_goods_wet_production.jsp    :2757
                    ↓
                PD_생산계근
```

---

## 8. 개발 플랜

### Step 0: PD_생산계근 테이블 신설

**Part 1. 분석**
- 대상: HL_ERP 프로젝트 (`D:\HL_ERP\workspace\SGIS_HL_WEBERP`)
- 용도: PDA 생산계근 결과 적재 테이블
- 주의: `hbm2ddl.auto: update` 설정으로 재기동 시 운영 DB에 실제 생성됨

**Part 2. 변환 계획**
- `DlivyWeighEntity`(`SM_출고계근`) 구조 복제 후 참조 키 2개 교체
- 시퀀스 `PD_PRDCTN_WEIGH_SEQ` 생성

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: `PrdctnWeighEntity.java` 작성
- [x] Part 4: `PrdctnWeighRepository.java` 작성
- [x] Part 5: 시퀀스 생성
- [x] Part 6: 테이블 생성

**Part 6. 변경 내용**
- **무엇을**: `PD_생산계근` 테이블 + 엔티티 + Repository 신설
- **왜**: `SM_출고계근`은 `SM_출고상세`/`SM_출고LOT` 참조 키를 가져 `PD_` SEQ 투입 시 키 공간 충돌
- **어떻게**: `SM_출고계근` 컬럼 구성 유지, `출고상세SEQ`→`소요량SEQ` / `출고LOTSEQ`→`지시SEQ` 교체

---

### Step 1: SELECT 쿼리 MSSQL 전환

**Part 1. 분석**
- 파일: `search_production.jsp:36~70`
- 용도: Oracle VIEW → 4테이블 직접 JOIN
- 주의할 점: `V.년월` 조건 필수 (오류25·32 재발 방지)

**Part 2. 변환 계획**
- 변환 방식: `PD_생산작업지시소요량` 기준 + `PD_생산작업지시`/`CO_품목코드`/`월품목별재고화일_LOT별_VIEW`/`CO_거래처MASTER` JOIN
- 주의사항: `NULLIF(…, 0)`은 0으로 나누기 방어용으로 유지

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [ ] Part 4: 쿼리 실행 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용**
- **무엇을**: `FROM VW_PDA_WID_PRO_LIST` → 4테이블 JOIN
- **왜**: MSSQL에 해당 VIEW 부재
- **어떻게**: §4.2 참조

---

### Step 2: 출력 컬럼 32개 → 25개 재구성

**Part 1. 분석**
- 파일: `search_production.jsp:81~90`(변경 전)
- 용도: `ProgressDlgShipSearch` `temp[0]~temp[24]` 정합
- 주의할 점: **Index 기준은 `out.println` 순서** (SELECT 순서 아님)

**Part 2. 변환 계획**
- 제거 8개 (v28 삭제분), `''` 처리 10개, `GI_L_ID` 신규 추가

**체크리스트**
- [x] Part 1~3 완료
- [ ] Part 4: 인덱스 정합 실측 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용**
- **무엇을**: 32개 → 25개 (24 표준 + `GI_L_ID`)
- **왜**: 앱이 `개발/09`로 24개 표준 레이아웃으로 전환됨
- **어떻게**: §4.1 참조

---

### Step 3: qry_where 적용 + 별칭 치환

**체크리스트**
- [x] Part 1~3 완료
- [ ] Part 4: 치환 결과 로그 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용**
- **무엇을**: `qry_where` 쿼리 적용 + `D.회사코드`/`D.출고일자` → `Q.회사코드`/`H.지시일자`
- **왜**: 기존에는 파라미터를 받고도 쿼리에 붙이지 않아 전량 조회
- **어떻게**: §4.3 참조

---

### Step 4: 앱 파싱 분기 추가

**체크리스트**
- [x] Part 1~3 완료
- [x] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용**
- **무엇을**: `ProgressDlgShipSearch.java:289` searchType=1 분기 추가
- **왜**: `GI_L_ID`가 `""`이면 전송 시 `Integer.parseInt("")` 예외
- **어떻게**: §4.7 참조

---

### Step 5: 계근 전송 JSP 신규 + 전송 분기 분리

**체크리스트**
- [x] Part 1~3 완료
- [x] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트 (3·4·5 영향 없음 확인)

**Part 6. 변경 내용**
- **무엇을**: `insert_goods_wet_production.jsp` 신규 + `Common.java`/`BixolonShipmentActivity.java` 수정
- **왜**: 생산 계근 결과를 `SM_출고계근`(영업)이 아닌 `PD_생산계근`에 적재
- **어떻게**: §4.4~4.6 참조

---

### Step 7: 계근이력 조회 JSP 분리

**Part 1. 분석**
- 파일: `search_goods_wet.jsp`(공용) / `ProgressDlgGoodsWetSearch.java:76`
- 용도: 서버 계근 이력을 PDA로 복원. 앱 재설치·기기 교체 대응
- 주의할 점: 출력 14개 컬럼 순서 고정 (`temp[0]~temp[13]`)

**Part 2. 변환 계획**
- `search_goods_wet_production.jsp` 신규 — `PD_생산계근` 조회
- 앱에서 searchType 1·7 분기

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트 (0·2·3·4·5·6 영향 없음 확인)

**Part 6. 변경 내용**
- **무엇을**: `search_goods_wet_production.jsp` 신규 + `Common.java`/`ProgressDlgGoodsWetSearch.java` 분기
- **왜**: 생산 계근 이력은 `PD_생산계근`에 적재되는데 공용 JSP가 `SM_출고계근`을 조회해 0건이 되거나 타 전표 데이터를 가져올 위험
- **어떻게**: §4.8~4.9 참조

---

### Step 6: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | P0101에서 작업지시 생성 → `불출여부='N'` 데이터 확보 | □ |
| 2 | `Q.회사코드` 컬럼 존재 확인 (SQL 오류 없음) | □ |
| 3 | `V.BLNO` 컬럼 존재 확인 | □ |
| 4 | 생산계근대상받기 → 25개 컬럼 `::` 수신 (adb logcat) | □ |
| 5 | `TB_SHIPMENT` INSERT 정상, `GI_L_ID` 채워짐 | □ |
| 6 | `ITEM_TYPE` 값 확인 (`S`/`J`) 및 계근 방식 일치 | □ |
| 7 | `GI_REQ_PKG` 정수 확인, 완료 판정 동작 | □ |
| 8 | 바코드 스캔 → 중량 추출 → `TB_GOODS_WET` INSERT | □ |
| 9 | 라벨 미출력 확인 (프린터 연결 요구 없음) | □ |
| 10 | 전송 → `PD_생산계근` INSERT, `소요량SEQ`/`지시SEQ` 값 확인 | □ |
| 11 | 재조회 시 계근 완료 건 제외 (`NOT EXISTS`) | □ |
| 12 | 계근 후 앱 데이터 삭제 → 재조회 시 계근이력 복원 확인 (search_goods_wet_production) | □ |
| 13 | 이마트(0)·비정량(4)·도매(3) 회귀 테스트 (조회·전송·계근이력 3경로) | □ |

---

### 개발 순서 요약

```
Step 0: PD_생산계근 테이블 신설          ✅
    ↓
Step 1: SELECT 쿼리 MSSQL 전환          ✅
    ↓
Step 2: 출력 컬럼 32 → 25 재구성         ✅
    ↓
Step 3: qry_where 적용 + 별칭 치환       ✅
    ↓
Step 4: 앱 파싱 분기 추가                ✅
    ↓
Step 5: 계근 전송 JSP 신규 + 분기 분리    ✅
    ↓
Step 7: 계근이력 조회 JSP 분리            ✅
    ↓
Step 6: 통합 테스트                      ⏳
```

---

## 9. 테스트 시나리오

### 시나리오 1: 생산 계근 정상 흐름

```
1. ERP P0101에서 생산작업지시 등록 (자재 1건 이상)
2. PDA 로그인 → 회사/날짜 선택
3. [생산계근대상받기] 클릭 → 대상 목록 다운로드
4. [생산계근입력시작] 클릭 → 계근 화면 진입 (프린터 연결 요구 없음 확인)
5. 센터명 스피너에 '하이랜드푸드' 1개만 표시 확인
6. 바코드 스캔 → 상품명/PP코드 표시, 중량 추출
7. 계근수량/중량 누적 확인
8. [전송] 클릭 → "s" 응답
9. ERP에서 PD_생산계근 조회 → 소요량SEQ/지시SEQ 값 확인
10. [생산계근대상받기] 재실행 → 계근 완료 건이 목록에서 제외되는지 확인
```

### 시나리오 2: 회귀 테스트

```
1. 이마트(0) 출하대상받기 → 계근 → 전송 (SM_출고계근 적재 확인)
2. 비정량(4) 동일 흐름 (insert_goods_wet_new.jsp 사용 확인)
3. 도매(3) 동일 흐름
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|:-:|--------|------|----------|
| 1 | `Q.회사코드` 컬럼 부재 SQL 오류 | `PD_생산작업지시소요량`의 `회사코드`는 `BaseEntity` 상속분이며 ERP 매퍼 사용 사례 미확인 | `H.회사코드`로 일괄 교체 (JOIN 3곳 + `NOT EXISTS` 1곳 + 치환 대상) |
| 2 | `V.BLNO` 컬럼 부재 | 이마트 JSP 사용 사실로만 확인 | `BL_NO`를 `COALESCE(NULLIF(Q.이력번호,''), '')`로 축소 |
| 3 | `ITEM_TYPE` 판정 오류 | `I.비정량여부` 값 분포 미확인 | 실데이터 확인 후 `S`/`J` 기준 조정 |
| 4 | 작업지점 스피너 공백 | `H.거래처코드` 미입력 시 `CLIENTNAME=''` | 기능 영향 없음. 필요 시 `H.지시번호` 등 대체 검토 |
| 5 | 조회 0건 | 선택 일자에 작업지시 없음 | P0101에서 해당 일자 지시 생성 |
| 6 | `GI_REQ_PKG` 0 | `I.박스중량 = 0` | 품목코드에서 박스중량 필수 등록 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 0 | PD_생산계근 테이블 신설 | ✅ 완료 |
| 1 | SELECT 쿼리 MSSQL 전환 | ✅ 완료 |
| 2 | 출력 컬럼 32 → 25 재구성 | ✅ 완료 |
| 3 | qry_where 적용 + 별칭 치환 | ✅ 완료 |
| 4 | 앱 파싱 분기 추가 | ✅ 완료 |
| 5 | 계근 전송 JSP 신규 + 분기 분리 | ✅ 완료 |
| 7 | 계근이력 조회 JSP 분리 | ✅ 완료 |
| 6 | 통합 테스트 | ⏳ 대기 |

---

## 12. 관련 문서

- `app/doc/소스분석/57_생산_계근대상받기_JSP_컬럼_사용분석.md` — 32개 컬럼 사용처 전수 추적
- `app/doc/column/11_생산_계근대상_컬럼매핑_Oracle_MSSQL.md` — Oracle↔ERP 컬럼 매핑
- `app/doc/column/05_VW_PDA_WID_PRO_LIST.md` — Oracle VIEW 컬럼 사용여부
- `app/doc/view/VW_PDA_WID_PRO_LIST` — Oracle DDL (25컬럼 구버전, 기준 사용 금지)
- `app/doc/개발/00_개발진행현황.md` — §4.0 searchType별 전환 현황
- `app/doc/개발/09_DB전용컬럼_삭제_가이드.md` — 8개 컬럼 삭제(v28) 근거
- `app/doc/개발/31_GI_L_ID_추가[GI_D_ID_비유니크_전체_영향범위].md` — `GI_L_ID` 도입 배경
- `app/doc/개발/46_정량_비정량_이마트_미계근조건_NOT_EXISTS_추가.md` — `NOT EXISTS` 패턴 선례
- `app/doc/개발/35_비정량_출하계근대상_JSP_MSSQL전환.md` — `qry_where` replace 선례
- `app/doc/오류/25_출하대상_조회_행중복_월품목별재고화일LOT별VIEW_년월조건누락.md` — `V.년월` 근거
- `app/doc/오류/31_이마트_소숫점_반올림_주석_버림동작_불일치[...].md` — 계근중량 버림 사양
- `app/doc/소스분석/48_8개_출하대상받기_VIEW_WHERE조건_비교.md` — §1.2 생산 WHERE 조건
- `app/doc/소스분석/25_ProductionActivity.md` — 생산계근계산 화면 (범위 밖)
- `app/doc/일정/2026-08-04.md` — 작업 일자

---

**문서 버전**: 1.0
