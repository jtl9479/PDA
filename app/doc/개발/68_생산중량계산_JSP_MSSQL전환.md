# 생산 중량계산 `search_production_calc.jsp` MSSQL 전환

**작성일**: 2026-09-27
**목적**: `search_production_calc.jsp`의 Oracle 테이블(`B_SUPPLIER_ITEM`, `S_BARCODE_INFO`) 의존성을 제거하고 `CO_품목코드` 기반으로 교체한다. PDA JSP 18개 중 **유일하게 미전환으로 남은 파일**이며, 전환 완료된 `search_barcode_info.jsp` 패턴을 그대로 적용한다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

### 추가 제약 조건 (이 가이드 한정)

- 본 작업 범위는 **`search_production_calc.jsp` 1개 파일**에 한정한다.
- 출력 4개 컬럼의 **수와 순서를 변경하지 않는다.** `ProductionActivity:415~418`이 `tempArray[0]~[3]`으로 파싱한다.
- 앱 소스(`ProductionActivity.java` 등)는 **수정하지 않는다.** JSP 단독 수정으로 완결한다.
- 구분자는 `::` 이며 행 종료자 `;;` 를 **붙이지 않는다** (원본과 동일).

---

## 1. 현재 구조

### 1.1 search_production_calc.jsp (전환 전)

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_production_calc.jsp`

```java
conn = getMSSQLConnection();          // 접속만 MSSQL 전환 완료

String qry_where = request.getParameter("data");   // 치환 없이 그대로 사용

String quertystring = "SELECT "
        + " C.WEIGHT_FROM"
        + ",C.WEIGHT_TO"
        + ",C.ZEROPOINT"
        + ",C.BASEUNIT"
        + " FROM B_SUPPLIER_ITEM a"        // Oracle 테이블
        + ",S_BARCODE_INFO c"              // Oracle 테이블
        + " WHERE    a.SUPPLIER_ITEM_ID = c.SUPPLIER_ITEM_ID "
        + " AND A.STATUS = 'Y'"
        + " AND ROWNUM = 1"                // Oracle 전용 구문
        + qry_where;

out.println(rs.getString("WEIGHT_FROM") + "::" + rs.getString("WEIGHT_TO")
          + "::" + rs.getString("ZEROPOINT") + "::" + rs.getString("BASEUNIT"));
```

**쿼리는 Oracle 원본과 바이트 단위로 동일하다.** 커넥션(`OracleDriver` → `getMSSQLConnection()`), 인코딩(`euc-kr` → `UTF-8`), 로거(`logger.info` → `System.out.println`)만 전환되었다.

### 1.2 호출 경로

`ProductionActivity.java:670~673`
```java
String packet = "AND a.PACKER_CODE = '"+packerCode+"'"
              + " AND a.PACKER_PRODUCT_CODE = '"+packerProductCode+"'";
result = HttpHelper.getInstance().sendDataDb(packet, "inno",
             "search_production_calc", Common.URL_WET_PRODUCTION_CALC);
```

`ProductionActivity.java:404~418` — **동기 수신 후 파싱**
```java
String temp = task.execute(test).get();   // .get() 으로 doInBackground 반환값 직접 수신
if(temp!=null) {
    String[] tempArray = temp.split("::", -1);
    weightFrom = tempArray[0];   // 중량 시작 위치
    weightTo   = tempArray[1];   // 중량 끝 위치
    zeroPoint  = tempArray[2];   // 소수점 자릿수
    baseUnit   = tempArray[3];   // 단위
    successFlag = true;
}
```

> `onPostExecute`는 로그 출력·다이얼로그 종료만 한다. 결과 소비는 `.get()` 경로다.

### 1.3 문제점

| # | 문제 | 영향 |
|:-:|------|------|
| 1 | `B_SUPPLIER_ITEM`·`S_BARCODE_INFO`는 MSSQL에 부재 | **HTTP 500 — 조회 전면 실패** |
| 2 | `ROWNUM = 1` 은 Oracle 전용 구문 | MSSQL 문법 오류 |
| 3 | `qry_where`의 `a.PACKER_CODE`·`a.PACKER_PRODUCT_CODE` 미치환 | `CO_품목코드` 전환 후 컬럼명 불일치 |
| 4 | `A.STATUS = 'Y'` 대응 컬럼 부재 | `CO_품목코드`에 `STATUS` 없음 |

**실측 (2026-09-27)** — 앱이 보내는 WHERE 그대로 호출

```
POST search_production_calc.jsp
data = AND a.PACKER_CODE = '30228' AND a.PACKER_PRODUCT_CODE = '20001'
→ HTTP 500  (line 47 = executeQuery)
```

### 1.4 현재 상태 — 생산 중량계산 전면 불가

| 단계 | JSP | 상태 |
|------|-----|:--:|
| **바코드 중량정보 조회** | **`search_production_calc.jsp`** | ❌ **본 작업 대상** |
| 계근 저장 | 없음 (로컬 SQLite `TB_GOODS_WET_PRODUCTION_CALC`) | — |
| 서버 전송 | 없음 | — |

조회가 실패하면 `weightFrom`이 설정되지 않아 `substring(weightFrom-1, weightTo)`(536행)가 동작하지 못하고 **중량 추출 자체가 불가능**하다. 화면은 *"입력하신 PACKER CODE / PPCODE 에 해당하는 바코드 정보가 없습니다."* 토스트만 출력한다.

---

## 2. 변경 구조

### 데이터 흐름

```
[MainActivity] 생산계근계산시작 (btnProdWetCalc)
    ↓ Intent → ProductionActivity            MainActivity:356
[PACKER코드 · PP코드 입력] → [바코드정보 수신]
    ↓ getBarcodeInfo()                       ProductionActivity:395
    ↓ SelectWeightFromTo.doInBackground      :658
    ↓ AND a.PACKER_CODE = '…' AND a.PACKER_PRODUCT_CODE = '…'
search_production_calc.jsp   ★ 본 작업
    ↓ CO_품목코드, 4개 컬럼
    ↓ WEIGHT_FROM::WEIGHT_TO::ZEROPOINT::BASEUNIT
weightFrom / weightTo / zeroPoint / baseUnit  :415~418
    ↓
[바코드 스캔] → substring(weightFrom-1, weightTo) → 중량 추출  :536
    ↓
로컬 SQLite TB_GOODS_WET_PRODUCTION_CALC (중복 판정용, onDestroy 시 삭제)
```

> 이 화면은 **서버에 아무것도 쓰지 않는다.** 서버 통신은 위 조회 1건뿐이다.

### 테이블 대응

| Oracle | HL_ERP | 근거 |
|--------|--------|------|
| `B_SUPPLIER_ITEM` + `S_BARCODE_INFO` | `CO_품목코드` | `search_barcode_info.jsp` 전환 선례 (동일 4개 값을 `CO_품목코드`에서 조회) |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **search_production_calc.jsp** | `Tomcat/webapps/ROOT/inno/` | 2테이블 조인 → `CO_품목코드` 단일, `ROWNUM` → `TOP 1`, `STATUS` 조건 삭제, `qry_where` 치환 |

**앱 소스 수정 없음.**

---

## 4. 수정 상세

### 4.1 컬럼 매핑 (4개)

`search_barcode_info.jsp`(전환완료)가 동일한 4개 값을 `CO_품목코드`에서 조회하고 있으므로 그 매핑을 그대로 사용한다.

| Idx | 별칭 | Oracle | **MSSQL (본 작업)** | 전례 |
|:--:|------|--------|---------------------|------|
| 0 | `WEIGHT_FROM` | `C.WEIGHT_FROM` | `SBI.중량시작` | `search_barcode_info.jsp:48` |
| 1 | `WEIGHT_TO` | `C.WEIGHT_TO` | `SBI.중량끝` | `search_barcode_info.jsp:49` |
| 2 | `ZEROPOINT` | `C.ZEROPOINT` | `SBI.소수점` | `search_barcode_info.jsp:43` |
| 3 | `BASEUNIT` | `C.BASEUNIT` | `SBI.기준단위` | `search_barcode_info.jsp:42` |

**컬럼 실재 검증 (2026-09-27, `search_barcode_info.jsp`에 WHERE 주입하여 실측)**

| 컬럼 | 결과 |
|------|------|
| `SBI.중량시작` | ✅ 조건 동작 — 9건 |
| `SBI.중량끝` | ✅ 조건 동작 |
| `SBI.기준단위` | ✅ 조건 동작 — 9건 |
| `SBI.소수점` | ✅ 조회 동작 |
| `SBI.패커코드` | ✅ 조건 동작 — 489건 |
| `SBI.ppCode` | ✅ 조건 동작 — 8,177건 |

### 4.2 FROM 절 · 조인 제거

**변경 전:**
```sql
FROM B_SUPPLIER_ITEM a, S_BARCODE_INFO c
WHERE a.SUPPLIER_ITEM_ID = c.SUPPLIER_ITEM_ID
```

**변경 후:**
```sql
FROM CO_품목코드 SBI
WHERE 1=1
```

`CO_품목코드` 단일 테이블에 4개 값이 모두 존재하므로 **조인이 소멸**한다. 별칭은 `search_barcode_info.jsp`와 동일하게 `SBI`로 통일한다.

`qry_where`가 `AND …` 로 시작하므로 결합 지점으로 `WHERE 1=1` 을 둔다. (원본은 조인 조건이 `WHERE` 역할을 했다.)

### 4.3 `ROWNUM = 1` → `SELECT TOP 1`

**변경 전:**
```sql
AND ROWNUM = 1
```

**변경 후:**
```sql
SELECT TOP 1 …
```

원본에 `ORDER BY`가 없으므로 Oracle `ROWNUM = 1`과 MSSQL `TOP 1` 모두 **비결정적으로 1건**을 반환한다. 동작 동일.

### 4.4 `A.STATUS = 'Y'` 삭제

**변경 전:**
```sql
AND A.STATUS = 'Y'
```

**변경 후:** 삭제

**근거 — `search_barcode_info.jsp` 전환 선례**

원본에서 `STATUS = 'Y'`를 사용하는 JSP는 `search_barcode_info.jsp`와 `search_production_calc.jsp` **2개뿐**이며, 전자는 전환 시 조건을 **전부 삭제**했다.

| 항목 | 원본 (Oracle) | 전환 (MSSQL) |
|------|---------------|--------------|
| 테이블 | `S_BARCODE_INFO` + `B_ITEM` + `B_SUPPLIER_ITEM` 3개 JOIN | `CO_품목코드` 단일 |
| `BI.STATUS = 'Y'` | 있음 (`:67`) | **삭제** |
| `SBI.STATUS = 'Y'` | 있음 (`:68`) | **삭제** |
| `BSI.STATUS = 'Y'` | 있음 (`:68`) | **삭제** |
| 출력 `STATUS` | 실제 컬럼 | `'' AS STATUS` |

**`생산품상태`는 대체재가 아니다.** `search_barcode_info_nonfixed.jsp:57`·`search_homeplus_nonfixed2.jsp:62`가 `SBI.생산품상태 AS STATUS`로 매핑하지만 **출력 전용**이며 WHERE 조건으로 쓰지 않는다. ERP 소스 기준 값이 `'3'` 계열이라 `'Y'`와 도메인이 다르다.

```
bin/main/mapper/co/bizbasic/C0102_SQL.xml:221   AND C.생산품상태 = '3'
bin/main/mapper/pd/prodwork/P0101_SQL.xml:215   AND I.생산품상태 = '3'
```

### 4.5 qry_where 컬럼명 치환

**변경 전:** 치환 없음

**변경 후:**
```java
// 앱(ProductionActivity:670)이 a.PACKER_CODE / a.PACKER_PRODUCT_CODE 로 전송
// CO_품목코드의 실제 컬럼명은 패커코드 / ppCode 이므로 치환
qry_where = qry_where.replace("a.PACKER_PRODUCT_CODE", "SBI.ppCode");
qry_where = qry_where.replace("a.PACKER_CODE",         "SBI.패커코드");
```

`search_barcode_info_nonfixed.jsp:37`·`search_homeplus_nonfixed2.jsp:42`·`search_production.jsp:39`와 동일한 처리 패턴이다.

앱이 보내는 조건
```
AND a.PACKER_CODE = '30228' AND a.PACKER_PRODUCT_CODE = '20001'
```

치환 결과
```sql
AND SBI.패커코드 = '30228' AND SBI.ppCode = '20001'
```

> **치환 순서 주의** — `a.PACKER_PRODUCT_CODE`를 먼저 치환한다. 역순으로 하면 `a.PACKER_CODE` 치환이 `a.PACKER_PRODUCT_CODE`에 영향을 주지 않으므로 실제로는 어느 순서든 안전하나(`a.PACKER_PRODUCT_CODE`는 `a.PACKER_CODE`를 부분문자열로 포함하지 않음), 가독성을 위해 긴 것부터 치환한다.

### 4.6 회사코드 — 추가하지 않음

**원본에 회사코드 조건이 없고, 앱도 전송하지 않으므로 추가하지 않는다.**

| 판단 근거 | 내용 |
|----------|------|
| 전례 | 전환된 JSP 중 **회사코드를 하드코딩한 파일은 0건**. 모두 앱에서 수신한다 |
| 앱 | `ProductionActivity:670`은 회사코드를 전송하지 않는다. 추가하려면 앱 수정이 필요한데 본 작업 범위 밖이다 |
| 원본 동일성 | 원본에 없던 조건을 추가하면 결과 집합이 달라질 수 있다 |
| 데이터 영향 | `CO_품목코드` 전체 8,219건 중 회사코드 20이 8,177건. 나머지 42건은 과거 테스트분이며 정합성 이슈 무시 가능 |

> 향후 회사코드 필터가 필요해지면 **앱에서 조건을 추가 전송**하는 방식으로 처리한다 (`search_barcode_info.jsp` 패턴).

---

## 5. 사이드이펙트

### 5.1 다른 searchType 영향

**없음.** `search_production_calc.jsp`는 `ProductionActivity` 전용이며, 이 화면은 `searchType`을 설정하지 않는 독립 화면이다. `Common.URL_WET_PRODUCTION_CALC`를 참조하는 곳은 `ProductionActivity:673` 1곳뿐이다.

### 5.2 앱 소스 영향

**없음.** 출력 4개 컬럼의 수와 순서를 유지하므로 `ProductionActivity:415~418`의 `tempArray[0]~[3]` 파싱이 그대로 동작한다. `::` 구분자, `;;` 미사용도 원본과 동일하다.

### 5.3 0건 반환 시 동작

조건에 맞는 행이 없으면 `out.println`이 실행되지 않아 응답이 빈 문자열이 된다. 앱에서는 `temp.split("::", -1)` 결과 길이가 1이 되어 `tempArray[1]` 접근 시 `ArrayIndexOutOfBoundsException`이 발생하고, `catch`(428행)에서 잡혀 `successFlag = false` → *"입력하신 PACKER CODE / PPCODE 에 해당하는 바코드 정보가 없습니다."* 토스트가 출력된다.

**원본과 동일한 동작**이므로 별도 처리하지 않는다.

### 5.4 원본 대비 동작 변경 1건

| 항목 | 원본 | 전환 후 | 근거 |
|------|------|---------|------|
| `A.STATUS = 'Y'` 필터 | 유효 상태 품목만 | **필터 없음** | `CO_품목코드`에 대응 컬럼 부재. `search_barcode_info.jsp` 전환분과 동일 처리 (§4.4) |

---

## 6. 선결 조건 — 데이터

**현재 `CO_품목코드`에 중량 정보가 거의 없다.** (2026-09-27 실측)

| 항목 | 건수 |
|------|-----:|
| 회사코드 20 · ppCode 보유 | 8,177 |
| **중량시작 + 중량끝 보유** | **9** |
| 그중 **패커코드 보유** | **0** |

```
패커=[] pp=1110100321 from=8  to=12  zp=2  unit=PK
패커=[] pp=1110100681 from=1  to=5   zp=2  unit=CT
패커=[] pp=2120300988 from=1  to=5   zp=2  unit=KG
… (9건 전부 패커코드 공백)
```

앱 조건이 **AND**(`패커코드 = '…' AND ppCode = '…'`)이므로, 패커코드가 공백인 행은 조건을 만족할 수 없어 **JSP를 전환해도 결과는 0건**이 된다.

> **ERP `CO_품목코드`에 패커코드·ppCode 입력 예정** (사용자 확인, 2026-09-27).
> 중량 정보를 보유한 위 9건에도 패커코드가 채워져야 테스트가 가능하다.

JSP 전환(Step 1)은 데이터와 무관하게 선행 가능하며, 단위테스트(Step 2)는 데이터 입력 이후 진행한다.

---

## 7. 호출 시점

```
[MainActivity]
    └── 생산계근계산시작 (btnProdWetCalc) 클릭        MainActivity:355
            ↓ new Intent(this, ProductionActivity.class)
        ProductionActivity
            ↓ PACKER코드 · PP코드 입력
            ↓ [바코드정보 수신] 클릭
        getBarcodeInfo()                              :395
            ↓ SelectWeightFromTo.execute().get()      :404~408
            ↓ ★ search_production_calc.jsp
            ↓ weightFrom / weightTo / zeroPoint / baseUnit
        [바코드 스캔] → 중량 추출 → 박스개수·총중량 누적
            ↓ insertGoodsWetProductionCalc (로컬)
        [뒤로가기] → onDestroy → deleteGoodsWetProductionCalc
```

---

## 8. 개발 플랜

### Step 1: JSP MSSQL 전환

**Part 1. 분석**
- 파일: `search_production_calc.jsp`
- 범위: SELECT 4개 컬럼 + FROM/조인 + `ROWNUM` + `STATUS` 조건 + `qry_where` 치환
- 용도: `B_SUPPLIER_ITEM`·`S_BARCODE_INFO` → `CO_품목코드` 전환
- 주의할 점: 출력 컬럼 수·순서 불변, `::` 구분자 유지, `;;` 미사용 유지

**Part 2. 변환 계획**
- 변환 방식: `search_barcode_info.jsp` 패턴 적용 (동일 4개 값을 `CO_품목코드`에서 조회)
- 주의사항: `ROWNUM = 1` → `TOP 1`(§4.3), `STATUS = 'Y'` 삭제(§4.4), 회사코드 미추가(§4.6)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 쿼리 실행 확인 (HTTP 200, 4개 컬럼, 구분자 일치)
- [ ] Part 5: 단위테스트 (실기기 — ERP 데이터 입력 후)
- [ ] Part 6: 회귀테스트 (대상 없음 — §5.1)

**Part 4. 쿼리 실행 확인 결과** (2026-09-27, 앱과 동일한 POST 재현)

| # | 요청 `data` | 결과 |
|:-:|------------|------|
| ① | `AND a.PACKER_CODE = '30228' AND a.PACKER_PRODUCT_CODE = '20001'` | **HTTP 200**, 0건 (해당 패커코드 미존재 — 정상) |
| ② | `AND a.PACKER_PRODUCT_CODE = '2120300988'` | **HTTP 200**, `1::5::2::KG` — 4개 컬럼·구분자 일치 |
| ③ | (조건 없음) | **HTTP 200**, `::::0::` — `TOP 1` 동작 확인 |

전환 전 ①은 **HTTP 500** (line 47 `executeQuery`)이었다.

②의 응답 `1::5::2::KG` 는 `ProductionActivity:415~418` 파싱과 일치한다.

```
tempArray[0] = "1"   → weightFrom  (중량 시작 위치)
tempArray[1] = "5"   → weightTo    (중량 끝 위치)
tempArray[2] = "2"   → zeroPoint   (소수점 자릿수)
tempArray[3] = "KG"  → baseUnit    (단위)
```

**Part 6. 변경 내용**
- **무엇을**: `FROM B_SUPPLIER_ITEM a, S_BARCODE_INFO c` → `FROM CO_품목코드 SBI`, 컬럼 4개 한글 전환(`AS` 별칭 유지), `ROWNUM = 1` → `SELECT TOP 1`, `A.STATUS = 'Y'` 삭제, 조인 조건 → `WHERE 1=1`, `qry_where` 치환 2건 추가
- **왜**: `B_SUPPLIER_ITEM`·`S_BARCODE_INFO`가 MSSQL에 부재하여 조회가 HTTP 500으로 전면 실패, 생산 중량계산 화면에서 중량 추출 자체가 불가능
- **어떻게**: `search_barcode_info.jsp` 패턴 적용(동일 4개 값을 `CO_품목코드`에서 조회). 출력 4개 컬럼 수·순서·구분자 유지하여 앱 무수정. `qry_where` 치환은 `search_barcode_info_nonfixed.jsp:37`·`search_production.jsp:39` 패턴 적용

---

### Step 2: 통합 테스트

> **선결**: ERP `CO_품목코드`에 패커코드·ppCode 입력 완료 (§6)

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | Tomcat 로그에 SQLException 없음 | □ |
| 2 | `##serch_shipment query: ` 치환 결과 확인 (`SBI.패커코드`·`SBI.ppCode`) | □ |
| 3 | 생산계근계산시작 진입 → 화면 정상 표시 | □ |
| 4 | PACKER코드·PP코드 입력 → 바코드정보 수신 성공 토스트 | □ |
| 5 | 입력 필드 비활성화 확인 (`setUseableEditText(false)`) | □ |
| 6 | 바코드 스캔 → 중량 추출 정상 (`substring` 범위) | □ |
| 7 | 박스개수 +1, 총중량 누적, 박스당중량 표시 | □ |
| 8 | 존재하지 않는 코드 → 실패 토스트 (§5.3) | □ |
| 9 | 리셋 → 박스개수·총중량 0 복귀 | □ |
| 10 | 뒤로가기 → 재진입 시 데이터 초기화 (`onDestroy`) | □ |

---

### 개발 순서 요약

```
Step 1: JSP MSSQL 전환
    ↓  (ERP 패커코드·ppCode 입력 대기)
Step 2: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 생산 중량계산 정상 흐름

```
1. PDA 로그인 → 메인화면
2. [생산계근계산시작] 클릭 → ProductionActivity 진입
3. PACKER코드 · PP코드 입력
4. [바코드정보 수신] 클릭
5. Tomcat 로그 확인
     ##serch_shipment query: SELECT TOP 1 SBI.중량시작 AS WEIGHT_FROM …
       FROM CO_품목코드 SBI WHERE 1=1 AND SBI.패커코드 = '…' AND SBI.ppCode = '…'
6. 토스트 "바코드 중량 정보 세팅됨, 계근을 시작하세요." 확인
7. 바코드 스캔 → 중량 추출 → 박스개수·총중량 누적 확인
8. [리셋] → 초기화 확인
```

### 시나리오 2: 조회 실패 (0건)

```
1. 등록되지 않은 PACKER코드 · PP코드 입력
2. [바코드정보 수신] 클릭
3. 토스트 "입력하신 PACKER CODE / PPCODE 에 해당하는 바코드 정보가 없습니다." 확인
4. 입력 필드가 계속 활성 상태인지 확인 (successFlag = false)
```

---

## 10. 관련 문서

| 폴더 | 문서 | 관계 |
|------|------|------|
| 개발/ | `61_홈플러스비정량_바코드정보조회_JSP_MSSQL전환.md` | JSP MSSQL 전환 패턴 선례 |
| 개발/ | `63_롯데_박스순번_파이프라인_복구[36].md` | `qry_where` 치환·JSP 신설 선례 |
| 오류/ | `36_롯데_search_shipment_lotte_박스순번_계근ID_미존재컬럼...md` | "`search_production_calc.jsp`는 MSSQL 미전환 상태로 별도 관리" 기록 — **본 문서로 해소** |
| 테스트/ | `PDA테스트문서.xlsx` — `9.생산중량계산` 시트 | TC P-01~P-17 |

---

## 11. 진행 현황

| Step | 내용 | 상태 |
|:----:|------|:----:|
| 1 | JSP MSSQL 전환 | ✅ 완료 (2026-09-27) |
| 2 | 통합 테스트 | ⏳ 대기 (ERP 패커코드·ppCode 입력 선결 — §6) |

### Step 1 완료 기록 (2026-09-27)

`search_production_calc.jsp` MSSQL 전환 완료. PDA JSP 18개 **전부 전환 완료**되었다.

| 항목 | 전환 전 | 전환 후 |
|------|---------|---------|
| 앱 포맷 호출 | **HTTP 500** (line 47) | **HTTP 200** |
| 테이블 | `B_SUPPLIER_ITEM` + `S_BARCODE_INFO` | `CO_품목코드` |
| 1건 제한 | `ROWNUM = 1` | `SELECT TOP 1` |
| 상태 필터 | `A.STATUS = 'Y'` | 없음 (§4.4) |
| 앱 수정 | — | **없음** |

**미해소 — Step 2 진행 조건**: `CO_품목코드`의 중량 정보 보유 9건에 패커코드가 전부 공백이다(§6). ERP 입력 완료 후 실기기 테스트가 가능하다.
