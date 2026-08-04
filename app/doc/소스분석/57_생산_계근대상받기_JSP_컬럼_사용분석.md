# 생산(searchType=1) 계근대상받기 JSP 컬럼 사용/미사용 분석

**작성일**: 2026-08-04
**대상**: `search_production.jsp` (32개 컬럼)
**분석 범위**: JSP 출력 → Java 파싱 → 로컬DB INSERT → 실제 사용처 추적
**목적**: MSSQL 전환 시 유지/제거/추가할 컬럼 확정

---

## 1. 분석 대상 파일

| 파일 | 경로 | 역할 |
|------|------|------|
| JSP 원본 | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_production.jsp` | Oracle 원본 |
| JSP 현재 | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_production.jsp` | MSSQL 전환 대상 |
| Java 호출 | `ProgressDlgShipSearch.java:129` | `Common.URL_SEARCH_PRODUCTION` |
| Java 파싱 (현재) | `ProgressDlgShipSearch.java:211~261` | `temp[0]~temp[23]` |
| Java 파싱 (원본) | `PDA-INNO(원본)/.../ProgressDlgShipSearch.java:157~215` | `temp[0]~temp[31]` |
| 로컬DB | `DBHandler.java` — `createqueryShipment()`(L25), `insertqueryShipment()`(L737~770) | `TB_SHIPMENT` |
| 모델 | `Shipments_Info.java` | setter/getter |
| 사용처 | `BixolonShipmentActivity.java`, `LabelPrintHelper.java`, `ShipmentListAdapter.java`, `DetailAdapter.java` | 계근·라벨·목록 |

---

## 2. 원본 vs 현재 JSP 비교

`diff` 실측 결과.

| 항목 | 원본 (Oracle) | 현재 | 전환 |
|------|:------------:|:----:|:----:|
| DB 접속 | `Class.forName("oracle.jdbc.driver.OracleDriver")` + `DriverManager.getConnection(jdbc:oracle:thin:@...)` | `getMSSQLConnection()` (`common/db_connection.jsp` include) | ✅ 완료 |
| 인코딩 | `euc-kr` | `UTF-8` | ✅ 완료 |
| 로깅 | `logger.info(...)` | `System.out.println(...)` | ✅ 완료 |
| **SELECT 쿼리** | **`FROM VW_PDA_WID_PRO_LIST`** | **동일** | ❌ **미전환** |
| **컬럼 수/순서** | **32개** | **동일** | ❌ **미전환** |
| **`ORDER BY`** | **`EOI_ID ASC`** | **동일** | ❌ **미전환** |

> **접속 계층만 MSSQL로 전환되고 쿼리는 Oracle VIEW 그대로**입니다. MSSQL에 `VW_PDA_WID_PRO_LIST`가 없으므로 실행 시 SQL 오류가 발생합니다.
> `search_production_calc.jsp`, `search_production_4label.jsp`도 동일 상태입니다.

---

## 3. 32개 컬럼 사용/미사용 전체 분석

**Index = JSP `out.println` 출력 순서** (`search_production.jsp:79~90`, SELECT 순서와 동일)

`temp[n](현재)`는 현재 앱의 24개 표준 레이아웃 기준 **기대 인덱스**이며, JSP가 실제로 보내는 위치와 다릅니다(§4 참조).

| Index | 컬럼명                   | VIEW 산출식                                      | temp[n] (원본) | temp[n] (현재 기대) | setter (현재 L행)                  | TB_SHIPMENT | 실사용 (get 호출 수) | 용도 분류                                 |
| :---: | --------------------- | --------------------------------------------- | :----------: | :-------------: | ------------------------------- | :---------: | :------------: | ------------------------------------- |
|   0   | `GI_H_ID`             | DDL 미선언                                       |   temp[0]    |        —        | 없음                              |      X      |       0        | **미사용(v28 삭제)**                       |
|   1   | `GI_D_ID`             | `B.GI_D_ID`                                   |   temp[1]    |     temp[0]     | `setGI_D_ID` (L211)             |      O      |       52       | **사용: 계근 조회 PK**                      |
|   2   | `EOI_ID`              | DDL 미선언                                       |   temp[2]    |        —        | 없음                              |      X      |       0        | **미사용(v28 삭제)** — 단 JSP `ORDER BY` 용  |
|   3   | `ITEM_CODE`           | `B.ITEM_CODE`                                 |   temp[3]    |     temp[1]     | `setITEM_CODE` (L212)           |      O      |       14       | **사용: 품목 식별**                         |
|   4   | `ITEM_NAME`           | `I.ITEM_NAME_KR`                              |   temp[4]    |     temp[2]     | `setITEM_NAME` (L213)           |      O      |       10       | **사용: 화면 상품명**                        |
|   5   | `EMARTITEM_CODE`      | `B.ITEM_CODE`                                 |   temp[5]    |     temp[3]     | `setEMARTITEM_CODE` (L214)      |      O      |     **87**     | **사용: 바코드 매칭 핵심**                     |
|   6   | `EMARTITEM`           | `I.ITEM_NAME_KR`                              |   temp[6]    |     temp[4]     | `setEMARTITEM` (L215)           |      O      |       7        | **사용: 계근 데이터 표시**                     |
|   7   | `GI_REQ_PKG`          | `B.GI_REQ_PKG`                                |   temp[7]    |     temp[5]     | `setGI_REQ_PKG` (L216)          |      O      |       33       | **사용: 요청박스수, 완료판정**                   |
|   8   | `GI_REQ_QTY`          | `B.GI_REQ_QTY`                                |   temp[8]    |     temp[6]     | `setGI_REQ_QTY` (L244)          |      O      |       15       | **사용: 요청중량, 소수점 보정**                  |
|   9   | `AMOUNT`              | DDL 미선언                                       |   temp[9]    |        —        | 없음                              |      X      |       0        | **미사용(v28 삭제)**                       |
|  10   | `GOODS_R_ID`          | DDL 미선언                                       |   temp[10]   |        —        | 없음                              |      X      |       0        | **미사용(v28 삭제)**                       |
|  11   | `GR_REF_NO`           | DDL 미선언                                       |   temp[11]   |        —        | 없음                              |      X      |       0        | **미사용(v28 삭제)**                       |
|  12   | `GI_REQ_DATE`         | `A.GI_REQ_DATE`                               |   temp[12]   |     temp[7]     | `setGI_REQ_DATE` (L245)         |      O      |     **1**      | **로컬DB저장용** (`DBHandler.java:745` 유일) |
|  13   | `BL_NO`               | `B.BL_NO`                                     |   temp[13]   |     temp[8]     | `setBL_NO` (L246)               |      O      |       19       | **사용: 목록 표시**                         |
|  14   | `BRAND_CODE`          | `R.BRAND_CODE`                                |   temp[14]   |     temp[9]     | `setBRAND_CODE` (L247)          |      O      |       16       | **사용: 계근 전송 패킷**                      |
|  15   | `BRANDNAME`           | DDL 미선언                                       |   temp[15]   |        —        | 없음                              |      X      |       0        | **미사용(v28 삭제)**                       |
|  16   | `CLIENT_CODE`         | `A.CLIENT_CODE`                               |   temp[16]   |    temp[10]     | `setCLIENT_CODE` (L248)         |      O      |       7        | **사용: 계근 조회 키(4키 중 1)**               |
|  17   | `CLIENTNAME`          | `B_CLIENT` 서브쿼리                               |   temp[17]   |    temp[11]     | `setCLIENTNAME` (L249)          |      O      |       10       | **사용: 목록 거래처명**                       |
|  18   | `CENTERNAME`          | `'하이랜드푸드'` (고정)                               |   temp[18]   |    temp[12]     | `setCENTERNAME` (L250)          |      O      |       9        | **사용: 계근 분기 조건**                      |
|  19   | `ITEM_SPEC`           | `R.ITEM_SPEC`                                 |   temp[19]   |    temp[13]     | `setITEM_SPEC` (L251)           |      O      |     **1**      | **로컬DB저장용** (`DBHandler.java:751` 유일) |
|  20   | `CT_CODE`             | `R.CT_CODE`                                   |   temp[20]   |    temp[14]     | `setCT_CODE` (L252)             |      O      |       4        | **사용: 라벨 원산지 인쇄**                     |
|  21   | `IMPORT_ID_NO`        | `DECODE(B.IMPORT_ID_NO,NULL,'0000',…)`        |   temp[21]   |    temp[15]     | `setIMPORT_ID_NO` (L253)        |      O      |     **85**     | **사용: 이력번호, 라벨 인쇄**                   |
|  22   | `PACKER_CODE`         | `D.PACKER_CODE`                               |   temp[22]   |    temp[16]     | `setPACKER_CODE` (L254)         |      O      |       11       | **사용: 바코드 파싱 규칙 조회**                  |
|  23   | `PACKERNAME`          | DDL 미선언                                       |   temp[23]   |        —        | 없음                              |      X      |       0        | **미사용(v28 삭제)**                       |
|  24   | `PACKER_PRODUCT_CODE` | `DECODE(D.PACKER_PRODUCT_CODE,NULL,'0000',…)` |   temp[24]   |    temp[17]     | `setPACKER_PRODUCT_CODE` (L255) |      O      |     **61**     | **사용: 바코드 매칭 + 계근 조회 키**              |
|  25   | `BARCODE_TYPE`        | `'P0'` (고정)                                   |   temp[25]   |    temp[18]     | `setBARCODE_TYPE` (L256)        |      O      |     **75**     | **사용: 라벨 레이아웃 분기 (`case "P0"`)**      |
|  26   | `ITEM_TYPE`           | `DECODE(I.ITEM_TYPE,10,'S','J')`              |   temp[26]   |    temp[19]     | `setITEM_TYPE` (L257)           |      O      |       21       | **사용: 라벨/계근 분기**                      |
|  27   | `PACKWEIGHT`          | `I.PACK_WEIGHT`                               |   temp[27]   |    temp[20]     | `setPACKWEIGHT` (L258)          |      O      |       7        | **사용: 중량 계산·인쇄**                      |
|  28   | `BARCODEGOODS`        | `DECODE(…,'0000000', S_BARCODE_INFO 서브쿼리)`    |   temp[28]   |    temp[21]     | `setBARCODEGOODS` (L259)        |      O      |       11       | **사용: 바코드 생성**                        |
|  29   | `STORE_IN_DATE`       | `A.GI_DATE`                                   |   temp[29]   |    temp[22]     | `setSTORE_IN_DATE` (L260)       |      O      |       23       | **사용: 라벨 납품일자**                       |
|  30   | `EMARTLOGIS_CODE`     | `'0000000'` (고정)                              |   temp[30]   |    temp[23]     | `setEMARTLOGIS_CODE` (L261)     |      O      |       51       | **사용: 라벨 물류코드**                       |
|  31   | `EMARTLOGIS_NAME`     | DDL 미선언                                       |   temp[31]   |        —        | 없음                              |      X      |       0        | **미사용(v28 삭제)**                       |
| **-** | **`GI_L_ID`**         | **JSP 미전송**                                   |      —       | **temp[24] 필요** | `setGI_L_ID` **미호출**            |   O (빈값)    |       35       | **누락: 추가 필요**                         |
|   -   | `GR_WAREHOUSE_CODE`   | `R.GR_WAREHOUSE_CODE`                         |   JSP 미조회    |        —        | 없음                              |      X      |       0        | **미사용(JSP미전달)** — DDL에만 존재            |

---

## 4. 인덱스 불일치 상세

### 4.1 원본 대비 현재 앱의 레이아웃 변경

`개발/09_DB전용컬럼_삭제_가이드.md` 작업(로컬DB **v28**)으로 8개 컬럼이 삭제되었습니다.

`DBHelper.java:55`
```java
// v28: 8개 컬럼 삭제 (GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME)
```

| | 원본 | 현재 |
|---|:--:|:--:|
| 공통 컬럼 수 | 32 | **24** |
| `temp[0]` | `GI_H_ID` | `GI_D_ID` |
| `temp[1]` | `GI_D_ID` | `ITEM_CODE` |
| `temp[23]` | `PACKERNAME` | `EMARTLOGIS_CODE` |

`ProgressDlgShipSearch.java:194~211`의 파싱은 **searchType 분기 없이 전 유형 공통**입니다.

```java
String[] result = receiveData.split(";;");
for (String s : result) {
    temp = s.split("::", -1);
    si.setGI_D_ID(temp[0].toString());   // 전 searchType 공통
```

이마트(0)·홈플러스(2)·도매(3)·비정량(4)·홈비정(5)·롯데(6) JSP는 24개로 전환 완료되었으나, **`search_production.jsp`만 32개 원본 레이아웃에 남아 있어 전 인덱스가 어긋납니다.**

### 4.2 실제 오작동 양상

`ProgressDlgShipSearch.java:216`
```java
si.setGI_REQ_PKG(String.valueOf((int) Double.parseDouble(temp[5])));
```

`temp[5]`에는 JSP index 5 = `EMARTITEM_CODE`(= 품목코드)가 들어갑니다.

| 품목코드 형태 | 결과 |
|---|---|
| 영문·기호 포함 | `NumberFormatException` → 조회 실패 |
| **숫자만** | **예외 없음 → 요청박스수 자리에 품목코드가 저장되는 무증상 데이터 오염** |

후자가 더 위험합니다. `GI_REQ_PKG`는 계근 완료 판정(`SAVE_CNT == GI_REQ_PKG`)에 쓰이므로 잘못된 값이 들어가면 완료 처리가 영구히 되지 않습니다.

### 4.3 `GI_L_ID` 누락

`ProgressDlgShipSearch.java:262~290`의 searchType별 추가 필드 분기에 **`1`(생산)이 없습니다.**

| searchType | 추가 필드 | `GI_L_ID` |
|:--:|:--:|:--:|
| 0, 4 | 6개 (`temp[24]`~`[30]`) | `temp[30]` ✅ |
| 2 | 1개 | `temp[24]` ✅ |
| 5 | 5개 | ❌ |
| 6 | 2개 | ❌ |
| **1** | **분기 없음** | **❌** |

`Shipments_Info.java:7`
```java
public String GI_L_ID = "";   // 출고LOT번호(출고LOTSEQ 값)
```

초기값이 `""`이고 `insertqueryShipment()`가 `Common.nullCheck(si.getGI_L_ID(), "")`로 감싸므로 **`TB_SHIPMENT.GI_L_ID TEXT NOT NULL` 제약 위반은 발생하지 않습니다.** 대신 모든 행의 `GI_L_ID`가 빈 문자열이 되어 **LOT 구분이 사라집니다** — 오류16(`GI_D_ID` 비유니크 → 계근수량 중복표시)과 동일한 증상입니다.

이는 미완료 항목으로 이미 기록되어 있습니다.

`개발/31_GI_L_ID_추가[GI_D_ID_비유니크_전체_영향범위].md:142, :150`
```
**다른 searchType (현재 작업 범위 외, 별도 진행):**
- search_production.jsp (searchType=1), search_production_4label.jsp (searchType=7)

- [ ] (별도) 다른 searchType JSP GI_L_ID 추가 (이마트 완료 후 진행)
```

---

## 5. 용도별 분류 요약

### 5.1 실사용 (22개)

| 분류 | 컬럼 | 대표 사용처 |
|------|------|-----------|
| **바코드 매칭** | `EMARTITEM_CODE`(87), `PACKER_PRODUCT_CODE`(61), `BARCODEGOODS`(11), `PACKER_CODE`(11) | `BixolonShipmentActivity.java` |
| **라벨 출력** | `BARCODE_TYPE`(75), `IMPORT_ID_NO`(85), `EMARTLOGIS_CODE`(51), `STORE_IN_DATE`(23), `ITEM_TYPE`(21), `CT_CODE`(4), `PACKWEIGHT`(7) | `LabelPrintHelper.java` |
| **계근 조회 키** | `GI_D_ID`(52), `CLIENT_CODE`(7) | `DBHandler.selectqueryGoodsWet()` |
| **목록 표시** | `GI_REQ_PKG`(33), `GI_REQ_QTY`(15), `BL_NO`(19), `CLIENTNAME`(10), `ITEM_NAME`(10), `ITEM_CODE`(14), `EMARTITEM`(7) | `ShipmentListAdapter.java`, `DetailAdapter.java` |
| **분기 조건** | `CENTERNAME`(9), `BRAND_CODE`(16) | `BixolonShipmentActivity.java:702` |

### 5.2 로컬DB 저장 전용 (2개)

| 컬럼 | 유일 소비처 | 비고 |
|------|-----------|------|
| `GI_REQ_DATE` | `DBHandler.java:745` | INSERT 후 읽는 코드 없음 |
| `ITEM_SPEC` | `DBHandler.java:751` | INSERT 후 읽는 코드 없음 |

`createqueryShipment()`가 둘 다 `TEXT NOT NULL`로 선언하므로 전환 후에도 값 공급이 필요합니다(빈 문자열 허용).

### 5.3 미사용 (8개) — 전환 시 제거

`GI_H_ID`, `EOI_ID`, `AMOUNT`, `GOODS_R_ID`, `GR_REF_NO`, `BRANDNAME`, `PACKERNAME`, `EMARTLOGIS_NAME`

전 소스 검색 결과 `DBHelper.java:55` **주석 1건 외 참조 없음**. `Shipments_Info`에 필드·setter·getter 모두 부재.

### 5.4 누락 (1개) — 전환 시 추가

`GI_L_ID` — `PD_생산작업지시소요량.SEQ` 또는 `LOTNO`로 공급 가능

---

## 6. 전환 목표 레이아웃

**32개 → 24개 + `GI_L_ID` = 25개** (홈플러스(2)와 동일 패턴)

| Index | 컬럼 |
|:--:|---|
| 0~23 | `GI_D_ID`, `ITEM_CODE`, `ITEM_NAME`, `EMARTITEM_CODE`, `EMARTITEM`, `GI_REQ_PKG`, `GI_REQ_QTY`, `GI_REQ_DATE`, `BL_NO`, `BRAND_CODE`, `CLIENT_CODE`, `CLIENTNAME`, `CENTERNAME`, `ITEM_SPEC`, `CT_CODE`, `IMPORT_ID_NO`, `PACKER_CODE`, `PACKER_PRODUCT_CODE`, `BARCODE_TYPE`, `ITEM_TYPE`, `PACKWEIGHT`, `BARCODEGOODS`, `STORE_IN_DATE`, `EMARTLOGIS_CODE` |
| **24** | **`GI_L_ID`** (신규) |

동반 수정: `ProgressDlgShipSearch.java:262~290`에 `searchType=1` 분기 추가

```java
} else if(Common.searchType.equals("1")) {
    si.setGI_L_ID(temp[24].toString());   // 출고LOTSEQ
}
```

> **원본 대비 동작 변경 주의**: 원본 앱은 생산에 추가 필드를 받지 않았습니다(`PDA-INNO(원본)/.../ProgressDlgShipSearch.java:217~223`은 이마트 계열에만 `temp[32]~[37]` 적용). `GI_L_ID` 추가는 개발31이 오류16 해결을 위해 전 searchType 확대를 전제한 설계이며 이마트·홈플러스에 적용 완료된 상태이므로, 생산만 제외하면 일관성이 깨집니다. 개발 문서에 판단 근거를 명시하고 진행합니다.

---

## 7. VIEW DDL 문서 정합성 문제

`app/doc/view/VW_PDA_WID_PRO_LIST`에 보관된 DDL은 **25개 컬럼**만 선언합니다.

| 구분 | 컬럼 | 개수 |
|------|------|:--:|
| DDL 미선언인데 JSP가 조회 | `GI_H_ID`, `EOI_ID`, `AMOUNT`, `GOODS_R_ID`, `GR_REF_NO`, `BRANDNAME`, `PACKERNAME`, `EMARTLOGIS_NAME` | 8 |
| DDL 선언인데 JSP 미조회 | `GR_WAREHOUSE_CODE` | 1 |

`ORDER BY EOI_ID ASC`의 `EOI_ID`도 DDL에 없습니다. 원본 JSP가 운영에서 정상 동작했으므로, **운영 Oracle VIEW는 문서 DDL보다 최소 8개 컬럼이 더 많은 상위 버전**입니다.

→ **보관 DDL을 전환 기준으로 삼으면 안 됩니다.** `column/05_VW_PDA_WID_PRO_LIST.md`(33개 기준)가 JSP와 일치하는 최신본입니다.

`개발/00_개발진행현황.md:64~65`의 "SM_출고머리/상세 기반 VIEW" 기재도 오류이며, `PD_생산작업지시` / `PD_생산작업지시소요량` 기반으로 정정이 필요합니다(§8).

---

## 8. ERP 테이블 대응 (참고)

| Oracle | ERP | 확정 |
|--------|-----|:---:|
| `W_GOODS_IH A` (`GI_TYPE='M1'`) | `PD_생산작업지시` | ✅ |
| `W_GOODS_ID B` | `PD_생산작업지시소요량` | ✅ |
| `B_ITEM I` | `CO_품목코드` | ✅ |
| `W_GOODS_R R` | 제거 → `월품목별재고화일_LOT별_VIEW` + `CO_품목코드` | ✅ |
| `I_OFFER_D D` | 제거 → `CO_품목코드.패커코드` / `.PPCODE` | ✅ |
| `B_CLIENT` | `CO_거래처MASTER` | ✅ |
| `NVL(B.PROC_PUT_FLAG,'N')='N'` | `PD_생산작업지시소요량.불출여부='N'` | ✅ |
| `DECODE(I.ITEM_TYPE,10,'S','J')` | `CO_품목코드.품목구분`(?) | ⏸ **미확정** |
| `A.SEND_FLAG='N'` | `PD_생산작업지시.지시여부`(?) | ⏸ **미확정** |
| `A.CLIENT_CODE` | `PD_생산작업지시.거래처코드`(?) | ⏸ **미확정** |

**근거**: `SM_출고머리.출고구분` 실사용값은 `1`(출고)/`2`(반품)/`3`(거래명세)뿐이며 생산투입 값이 없음. `P0101Service.java:213`이 작업지시 시 `불출여부='N'`을 강제 세팅.

미확정 3건은 실 DB 조회로 확인이 필요하며, 특히 `ITEM_TYPE`은 라벨 레이아웃 분기(21곳)에 직결되어 오판 시 잘못된 라벨이 인쇄됩니다.

---

## 9. 관련 문서

- `app/doc/개발/00_개발진행현황.md` — §4.0 searchType별 전환 현황 (§7 정정 필요)
- `app/doc/개발/09_DB전용컬럼_삭제_가이드.md` — 8개 컬럼 삭제(로컬DB v28) 근거
- `app/doc/개발/31_GI_L_ID_추가[GI_D_ID_비유니크_전체_영향범위].md` — `GI_L_ID` 미완료 항목
- `app/doc/오류/16_GI_D_ID_비유니크_전체_영향범위.md` — `GI_L_ID` 필요 사유
- `app/doc/view/VW_PDA_WID_PRO_LIST` — Oracle DDL (구버전, §7 참조)
- `app/doc/column/05_VW_PDA_WID_PRO_LIST.md` — 33개 컬럼 사용여부 분석
- `app/doc/소스분석/45_비정량_바코드정보조회_JSP_컬럼_사용분석.md` — 동일 형식 선행 분석
- `app/doc/소스분석/48_8개_출하대상받기_VIEW_WHERE조건_비교.md` — §1.2 생산(1)
- `app/doc/소스분석/25_ProductionActivity.md` — 생산계근계산 화면
- `app/doc/일정/2026-08-04.md` — 본 분석 수행 일자
