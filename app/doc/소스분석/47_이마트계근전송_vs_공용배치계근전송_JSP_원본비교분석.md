# 이마트(insert_goods_wet.jsp) vs 공용 배치(insert_goods_wet_new.jsp) JSP 원본 비교 분석

## 개요

**원본 Oracle JSP 2종의 1:1 비교**. 이마트 단건 계근전송 JSP와 5개 searchType 공용 배치 계근전송 JSP를 **원본 프로젝트(`apache-tomcat-7.0.78_PDA_IN(원본)`) 기준으로** 비교하여 두 JSP가 처음부터 어떻게 다르게 설계되었는지(순수 업무 로직 차이)를 파악한다.

MSSQL 전환 아티팩트를 배제하고 **단건/배치의 본질적 차이**만 추출하는 것이 본 문서의 목적이다. 개발37(`insert_goods_wet_new.jsp` MSSQL 전환)의 사전자료로 활용한다.

- **대상 파일 A (이마트 단건, 원본 Oracle)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\insert_goods_wet.jsp`
- **대상 파일 B (공용 배치, 원본 Oracle)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\insert_goods_wet_new.jsp`
- **Java 호출 위치**: `BixolonShipmentActivity.java:2705~2715`, `ShipmentActivity.java:3664~3791`
- **대상 DB 테이블**: `W_GOODS_WET` (Oracle, 공통)
- **비교 시점**: **원본(MSSQL 전환 전)** 기준
- **타입**: JSP 원본 비교 분석
- **작성일**: 2026-04-22

---

## 1. 역할

- 두 JSP 모두 **PDA에서 계근 완료 후 서버 DB(W_GOODS_WET)에 계근 결과를 INSERT**하는 역할
- 호출자(`BixolonShipmentActivity` / `ShipmentActivity`)가 `searchType`에 따라 URL만 다르게 호출
- 저장 대상 테이블과 SELECT 컬럼은 완전히 동일 (`W_GOODS_WET` 13개 필드)
- **역할·데이터 구조는 100% 동일**하며, 차이는 오직 **처리 방식(단건 vs 배치)과 구분자**뿐

---

## 2. 주요 상수/필드 (원본 기준)

| 항목 | 이마트 원본 (A) | 공용 배치 원본 (B) | 차이 유형 |
|------|----------------|----------------|:--------:|
| DB 접속 방식 | Oracle `DriverManager.getConnection(url, dbid, "DBPassword")` | Oracle `DriverManager.getConnection(url, dbid, "DBpassword")` | 비밀번호 대소문자 표기 차이 (동일 의미) |
| 드라이버 | `oracle.jdbc.driver.OracleDriver` | `oracle.jdbc.driver.OracleDriver` | 동일 |
| 요청 인코딩 | `euc-kr` | `euc-kr` | 동일 |
| 입력 파라미터 | `data`, `dbid` | `data`, `dbid` | 동일 |
| **데이터 구분자** | `"::"` (컬럼 구분만, 행 구분 없음) | `"##"` (행 구분) + `"::"` (컬럼 구분) | **핵심 차이** |
| **처리 방식** | **단건** (`data.split("::")`) | **배치** (`data.split("##")` → for 루프 → `split("::")`) | **핵심 차이** |
| 대상 테이블 | `W_GOODS_WET` | `W_GOODS_WET` | 동일 |
| 시퀀스 | `W_GOODS_WET_SEQ.NEXTVAL` | `W_GOODS_WET_SEQ.NEXTVAL` | 동일 |
| INSERT 컬럼 수 | **13개** (GOODS_WET_ID + 12개) | **13개** (GOODS_WET_ID + 12개) | 동일 |
| INSERT 컬럼 구성 | GOODS_WET_ID, GI_D_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE, BARCODE, PACKER_CLIENT_CODE, MAKINGDATE, BOXSERIAL, BOX_CNT, REG_ID, REG_DATE, REG_TIME | 동일 | **완전 동일** |
| 파라미터 세팅 | `pstmt.setXxx(1)`~`(12)` 12회 | `pstmt.setXxx(1)`~`(12)` 12회 (루프 내) | 동일 |
| commit 시점 | INSERT 직후 | 루프 완료 후 (전체 배치 동시 commit) | 로직적 차이 |
| 예외 처리 | try-catch, rollback | try-catch, rollback | 동일 |
| 응답 | `"s"` 성공, `"f"` 실패 | `"s"` 성공, `"f"` 실패 | 동일 |

---

## 3. 주요 메서드 (원본 기준)

두 JSP 모두 단일 스크립트 블록. 주요 동작 블록을 나란히 표기한다.

| 동작 | 이마트 원본 (A) 줄 | 공용 배치 원본 (B) 줄 | 용도 |
|------|:---------------:|:------------------:|------|
| 파라미터 수신 | L20~22 | L20~23 | `data`, `dbid` 수신 |
| Oracle 드라이버 로드 | L25 | L26 | `Class.forName(driver)` |
| DB 접속 | L26 | L27 | `DriverManager.getConnection()` |
| 데이터 split | L34 `split("::")` | L36 `split("##")` + L73 `split("::")` | **핵심 차이** |
| 날짜/시간 생성 | L41~47 | L46~52 | `SimpleDateFormat` |
| INSERT 쿼리 조립 | L50~64 | L54~68 | 동일 쿼리 |
| PreparedStatement | L65 | L69 | 동일 |
| 파라미터 세팅 | L68~79 (12회, 1회) | L77~88 (12회, **루프 내**) | **핵심 차이 — 배치** |
| 루프 제어 | 없음 | L71~102 `for (i=0; i < splitDataTotal.length; i++)` | **핵심 차이** |
| `executeUpdate` | L97 (1회) | L92 (루프 내 N회) | 배치 실행 |
| `clearParameters` | 없음 (단건이므로 불요) | L93 (루프 내, 재사용 위함) | 배치 전용 |
| commit | L125 (INSERT 1건 후) | L104 (루프 완료 후) | 배치 commit |
| 리소스 해제 | L134~137 | L106~109 | 동일 |
| 응답 | L138 `out.println("s")` | L111 `out.println("s")` | 동일 |

---

## 4. 호출 관계

### 4.1 Java 호출 분기 (searchType)

| searchType | 호출 URL 상수 | 원본 JSP | Java 분기 위치 |
|:---------:|---------------|---------|:----:|
| 0 (이마트) | `URL_INSERT_GOODS_WET` | **A** (단건) | `BixolonShipmentActivity.java:2705~2706` |
| 1 (생산) | `URL_INSERT_GOODS_WET_NEW` | **B** (배치) | `BixolonShipmentActivity.java:2709~2711` |
| 2 (홈플러스) | `URL_INSERT_GOODS_WET_HOMEPLUS` | (다른 JSP) | `BixolonShipmentActivity.java:2707~2708` |
| 3 (도매) | `URL_INSERT_GOODS_WET_NEW` | **B** (배치) | `BixolonShipmentActivity.java:2712~2715` |
| 4 (비정량) | `URL_INSERT_GOODS_WET_NEW` | **B** (배치) | `BixolonShipmentActivity.java:2709~2711` |
| 5 (홈플러스 비정량) | `URL_INSERT_GOODS_WET_NEW` | **B** (배치) | `BixolonShipmentActivity.java:2709~2711` |
| 6 (롯데) | `URL_INSERT_GOODS_WET_HOMEPLUS` | (다른 JSP) | `ShipmentActivity.java` |
| 7 (생산 라벨) | `URL_INSERT_GOODS_WET_NEW` | **B** (배치) | `BixolonShipmentActivity.java:2709~2711` |

### 4.2 Java 측 packet 조립 구조 (B 호출 경로만)

`BixolonShipmentActivity.java` L2668~2694 for 루프에서 **15개 필드를 `::` 로 구분 + `##` 로 행 종결**:

```
splitData[0]  = GI_D_ID
splitData[1]  = WEIGHT
splitData[2]  = WEIGHT_UNIT
splitData[3]  = PACKER_PRODUCT_CODE
splitData[4]  = BARCODE
splitData[5]  = PACKER_CLIENT_CODE
splitData[6]  = MAKINGDATE
splitData[7]  = BOXSERIAL
splitData[8]  = BOX_CNT
splitData[9]  = REG_ID
splitData[10] = Common.selectCompanyCode  ← 회사코드
splitData[11] = BRAND_CODE
splitData[12] = CLIENT_TYPE
splitData[13] = BOX_ORDER
splitData[14] = GI_L_ID                    ← 출고LOT SEQ
```

**중요**: Java는 **15개 필드를 이미 전송**하지만, 원본 공용 배치 JSP(B)는 `splitData[0]~[9]` **10개만 사용**하고 `splitData[10]~[14]` **5개는 무시**. 이는 MSSQL 전환 시 중요한 사전 정보.

### 4.3 Java 측 packet 조립 구조 (A 호출 경로)

`BixolonShipmentActivity.java` 이마트 분기(L2705~2706)는 다른 메서드(`sendData` vs `sendDataDb`)를 사용하며, packet은 단일 행 `::` 구분만 사용 (`##` 없음). 실제 분석 대상 범위는 이마트의 ShipmentActivity 측 packet 조립 코드이며, 본 문서는 B의 분기에만 집중.

---

## 5. 데이터 흐름 (원본 기준)

```
[PDA 앱] 계근 완료 → 전송 버튼 → ProgressDlgShipmentSend
    ↓ TB_GOODS_WET 로컬 DB에서 SAVE_TYPE='F' SELECT
    ↓
    ├─ searchType=0 (이마트) → packet 조립 (::, 단일 행)
    │     ↓ HTTP POST (data 파라미터)
    │     ↓ [이마트 원본 JSP] (A)
    │         ├─ split("::") → splitData[0]~[12]
    │         ├─ pstmt.setXxx 12회
    │         ├─ executeUpdate (1회)
    │         └─ commit
    │
    └─ searchType=1,3,4,5,7 (공용 배치) → packet 조립 (::, ## 행구분, 15필드)
          ↓ HTTP POST (data 파라미터)
          ↓ [공용 배치 원본 JSP] (B)
              ├─ split("##") → splitDataTotal[0]~[N-1] (N개 행)
              ├─ for (i=0; i<N; i++) {
              │     split("::") → splitData[0]~[14]
              │     pstmt.setXxx 12회 (splitData[0]~[9]만, [10]~[14] 미사용)
              │     executeUpdate (루프 내 N회)
              │     clearParameters
              │ }
              └─ commit (N건 일괄)
    │
[PDA 앱] 응답 "s" 수신 시 TB_GOODS_WET SAVE_TYPE='F' → 'Y' UPDATE
```

---

## 6. 핵심 코드 — 원본 SQL 전문

### 6.1 이마트 원본 (A) INSERT 쿼리 — L50~64

```jsp
String qry = "INSERT INTO W_GOODS_WET(GOODS_WET_ID"
    + ", GI_D_ID"
    + ", WEIGHT"
    + ", WEIGHT_UNIT"
    + ", PACKER_PRODUCT_CODE"
    + ", BARCODE"
    + ", PACKER_CLIENT_CODE"
    + ", MAKINGDATE"
    + ", BOXSERIAL"
    + ", BOX_CNT"
    + ", REG_ID"
    + ", REG_DATE"
    + ", REG_TIME)"
    + " VALUES "
    + "(W_GOODS_WET_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?)";
```

### 6.2 공용 배치 원본 (B) INSERT 쿼리 — L54~68

```jsp
String qry = "INSERT INTO W_GOODS_WET(GOODS_WET_ID"
    + ", GI_D_ID"
    + ", WEIGHT"
    + ", WEIGHT_UNIT"
    + ", PACKER_PRODUCT_CODE"
    + ", BARCODE"
    + ", PACKER_CLIENT_CODE"
    + ", MAKINGDATE"
    + ", BOXSERIAL"
    + ", BOX_CNT"
    + ", REG_ID"
    + ", REG_DATE"
    + ", REG_TIME)"
    + " VALUES "
    + "(W_GOODS_WET_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?)";
```

### 6.3 두 쿼리의 동일성

**쿼리 문자열이 한 글자 단위로 완전히 동일** (변수명, 공백, 순서 포함). 따라서 DB 레벨의 동작은 100% 같음. 차이는 JSP 외부(데이터 수신/처리) 레벨에서만 발생.

### 6.4 컬럼 매핑 (A, B 공통)

| # | Oracle 컬럼 | Java splitData[] 인덱스 | 비고 |
|:-:|-----------|:-------------------:|------|
| PK | GOODS_WET_ID | 자동생성 (`W_GOODS_WET_SEQ.NEXTVAL`) | 시퀀스 |
| 1 | GI_D_ID | splitData[0] | 출고상세 ID |
| 2 | WEIGHT | splitData[1] | 계근중량 (`(Double * 100) / 100.0`) |
| 3 | WEIGHT_UNIT | splitData[2] | 중량 단위 |
| 4 | PACKER_PRODUCT_CODE | splitData[3] | 패커상품코드 |
| 5 | BARCODE | splitData[4] | 계근바코드 |
| 6 | PACKER_CLIENT_CODE | splitData[5] | 패커업체코드 |
| 7 | MAKINGDATE | splitData[6] | 제조일자 |
| 8 | BOXSERIAL | splitData[7] | 박스시리얼 |
| 9 | BOX_CNT | splitData[8] | 계근순번 (박스 카운트) |
| 10 | REG_ID | splitData[9] | 등록사원 |
| 11 | REG_DATE | `dateStr` (서버 생성) | `yyyyMMdd` |
| 12 | REG_TIME | `timeStr` (서버 생성) | `HHmmss` |

### 6.5 B에서 미사용되는 splitData

B는 Java가 전송한 15개 중 **10개만 사용**. 나머지 5개(splitData[10]~[14])는 **수신은 하지만 JSP에서 활용하지 않음**:

| splitData[] | Java에서 의미 | 원본 B의 사용 여부 |
|:---------:|-------------|:----------------:|
| [10] | `Common.selectCompanyCode` (회사코드) | ❌ 미사용 |
| [11] | `BRAND_CODE` | ❌ 미사용 |
| [12] | `CLIENT_TYPE` | ❌ 미사용 |
| [13] | `BOX_ORDER` | ❌ 미사용 |
| [14] | `GI_L_ID` (출고LOT SEQ) | ❌ 미사용 |

→ **이 5개 필드는 MSSQL 전환 시 활용 가능한 여유 자원** (개발37에서 결정 필요)

---

## 7. 비즈니스 로직 차이 분석 (원본 기준)

### 7.1 설계 철학의 근본적 차이

| 설계 요소 | 이마트 단건 (A) | 공용 배치 (B) |
|---------|--------------|------------|
| **전송 트리거** | 계근 1건 완료 즉시 전송 | 여러 건 모아 일괄 전송 (전송 버튼) |
| **네트워크 부담** | 건별 HTTP 호출 (고빈도) | 일괄 HTTP 호출 (저빈도) |
| **트랜잭션 범위** | 건별 독립 | 일괄 (commit 1회) |
| **실패 시 영향** | 1건만 실패/롤백 | 전체 일괄 롤백 |
| **구분자 설계** | `::` (컬럼)만 필요 | `::` + `##` (행) 2종 필요 |
| **for 루프** | 없음 | JSP 내부 루프 |

### 7.2 왜 이마트만 단건 처리인가

- 이마트는 **출하대상 하나씩 바코드 스캔 + 즉시 계근 + 즉시 라벨 출력 + 즉시 전송** 흐름
- 중간에 통신 장애가 나도 이미 전송된 건까지는 안전하게 기록되어야 함
- 현장에서 "한 건 전송"의 확인이 명확해야 함

### 7.3 왜 5개 searchType은 공용 배치인가

- 생산/도매/비정량/홈플러스비정량/생산라벨은 **여러 건을 한꺼번에 계근 후 일괄 전송**하는 워크플로우
- 현장 효율 우선 (네트워크 호출 횟수 감소)
- 전송 실패 시 재시도 단순 (전체 배치 재전송)

### 7.4 기술적 동일성 (DB 레벨)

- 대상 테이블·컬럼 구조 100% 동일
- 시퀀스 동일
- 파라미터 타입 동일 (Int, Double, String)
- 서버 자동 생성 필드(REG_DATE/REG_TIME) 동일

즉 **"같은 DB 인프라 위에서 처리 패턴만 달리한 쌍둥이 JSP"**

---

## 8. 원본 대비 현재(MSSQL 전환) 상태 요약 (참고)

본 문서는 **원본 비교가 주목적**이지만, 현재 전환 작업의 진척을 이해하기 위한 상태표를 포함한다.

| JSP | 원본 (Oracle) | 현재 | 전환 상태 |
|-----|-------------|------|:--------:|
| 이마트 (A) `insert_goods_wet.jsp` | W_GOODS_WET + 13개 필드 + 단건 | SM_출고계근 + **18개 필드** + 단건 | ✅ 완료 |
| 공용 배치 (B) `insert_goods_wet_new.jsp` | W_GOODS_WET + 13개 필드 + 배치 | W_GOODS_WET + 13개 필드 + 배치 (DB 접속만 MSSQL) | ❌ 미전환 (개발37) |

### 8.1 이마트 MSSQL 전환 시 구조 변화 (참고)

원본 이마트(A)는 **13개 필드 INSERT**였으나 MSSQL 전환 시 **18개로 확장**됨:

| 추가된 필드 | 매핑 값 | 출처 |
|-----------|--------|------|
| 출고LOTSEQ | `splitData[14]` (GI_L_ID) | PDA 앱 추가 전송 |
| 회사코드 | `splitData[10]` (Common.selectCompanyCode) | PDA 앱 추가 전송 |
| 수정사원 | `splitData[9]` (REG_ID와 동일값) | 서버 자동 |
| 수정일자 | `dateStr` (서버 자동 생성) | 서버 자동 |
| 수정시간 | `timeStr` (서버 자동 생성) | 서버 자동 |

또한 Oracle 시퀀스는 MSSQL 시퀀스로 교체:
- `W_GOODS_WET_SEQ.NEXTVAL` → `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ`

### 8.2 공용 배치(B) MSSQL 전환 시 쟁점

본 비교 분석에서 도출된 **핵심 의사결정 포인트** (개발37에서 결정 필요):

| 쟁점 | 옵션 1: 13개 필드 유지 | 옵션 2: 18개 필드 확장 (이마트 패턴 일치) |
|------|--------------------|------------------------------------|
| **회사코드** | NULL INSERT (또는 기본값) | `splitData[10]` 사용 |
| **출고LOTSEQ** | NULL INSERT | `splitData[14]` 사용 |
| **수정사원/일자/시간** | NULL | 서버 자동 생성 |
| **CLAUDE.md 제1원칙** | 원본(13개) 동일 동작 유지 ✅ | 원본과 필드 수 다름 ⚠️ (하지만 이마트도 확장됨) |
| **데이터 정합성** | 회사코드 누락으로 멀티회사 환경에서 문제 가능 | 이마트와 동일한 풍부한 레코드 |
| **Java 수정 필요 여부** | 불필요 (splitData 10~14 무시) | 불필요 (Java는 이미 15개 전송) |

→ **이 결정은 개발37 가이드 작성 시 확정 필요**. 이마트가 이미 18개로 확장된 선례가 있고, Java가 이미 15개를 전송하므로 **옵션 2가 현실적으로 권장**되나, 원본 기능 보존 원칙과 충돌 여부는 업무 담당자 확인 필요 (문의사항 등록 검토).

### 8.3 배치 처리 유지 필수

- `##` 행 구분자는 JSP/Java 양측에서 유지 필수
- for 루프 구조 유지 필수
- `clearParameters()` 루프 내 호출 유지

---

## 9. 주의사항

- 본 문서의 모든 비교는 **원본 Oracle JSP(`apache-tomcat-7.0.78_PDA_IN(원본)`) 기준**이다.
- **원본 시점의 두 JSP는 INSERT 쿼리가 완전히 동일**했으며, 차이는 오직 단건/배치 처리 방식뿐
- MSSQL 전환 후 이마트(A)는 18개 필드로 확장되었으나, 공용 배치(B)는 아직 원본 상태 (개발37 전환 대상)
- **B 전환 시 쟁점**: 18개 필드로 확장할지 13개로 유지할지 — 개발37 가이드 작성 시 결정 필요
- **Java 측 수정 불필요**: Java(`BixolonShipmentActivity` / `ShipmentActivity`)는 이미 15개 필드를 `::` + `##` 구조로 전송 중이므로, JSP 측만 전환하면 됨
- **5개 searchType 영향**: 개발37 작업은 생산(1)/도매(3)/비정량(4)/홈플러스비정량(5)/생산라벨(7) **5개 searchType 모두 회귀 테스트** 필요
- 원본 B가 `splitData[10]~[14]` 5개 필드를 무시한 것은 **설계 누락이 아니라 의도된 최소 필수 필드 사용** (Oracle W_GOODS_WET 스키마 자체가 13컬럼만 지원)
- **이마트 insert_goods_wet.jsp의 MSSQL 전환 경로**가 공용 배치 전환 시 직접 참조 대상

---

## 10. 관련 문서

- `app/doc/개발/37_비정량_계근데이터전송_JSP_MSSQL전환.md` — 공용 배치 JSP MSSQL 전환 개발 가이드 (작성 예정)
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` — 이마트 전체 흐름 (insert_goods_wet.jsp 포함)
- `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md` — 비정량 전체 흐름 (insert_goods_wet_new.jsp 포함)
- `app/doc/소스분석/37_로컬DB_MSSQL전환_현황.md` — TB_GOODS_WET ↔ SM_출고계근 매핑 (이마트 MSSQL 버전 기준)
- `app/doc/소스분석/46_이마트바코드정보조회_vs_비정량바코드정보조회_JSP_원본비교분석.md` — 유사 패턴의 JSP 원본 비교 (선행 사례)
- `app/doc/개발/32_회사코드_packet수정[SM출고계근_회사코드_ITEM_CODE_잘림오류].md` — 회사코드 packet 관련 선행 이슈
- `app/doc/오류/19_SM출고계근_회사코드_ITEM_CODE_잘림오류.md` — SM_출고계근 관련 오류 사례
- `app/doc/view/W_GOODS_WET.md` — W_GOODS_WET 테이블 분석
- **JSP 원본 이마트**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\insert_goods_wet.jsp`
- **JSP 원본 공용 배치**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\insert_goods_wet_new.jsp`
- **JSP 현재 이마트 (MSSQL 전환 완료)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet.jsp`
- **JSP 현재 공용 배치 (MSSQL 미전환)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_new.jsp`
- **Java 호출**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java:2705~2715`
- **ERP Entity**: `D:\HL_ERP\workspace\SGIS_HL_WEBERP\...` (SM_출고계근 관련 엔티티)
