# 홈플러스정량 계근데이터전송 JSP MSSQL 전환

**작성일**: 2026-04-30
**목적**: 홈플러스 정량(searchType=2) 및 롯데(searchType=6) 계근 완료 데이터를 INSERT하는 `insert_goods_wet_homeplus.jsp`를 Oracle `W_GOODS_WET` 테이블 + Oracle 시퀀스(`W_GOODS_WET_SEQ.NEXTVAL`) 기반에서 MSSQL `SM_출고계근` 테이블 + MSSQL 시퀀스(`NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ`) 기반으로 전환한다. 단건 처리 구조(`##` 없음) 및 Java 측 packet 구조는 변경하지 않는다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### insert_goods_wet_homeplus.jsp (전환 대상)

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_homeplus.jsp`

**사용 searchType**: 2(홈플러스 정량), 6(롯데)

```jsp
// DB 접속: getMSSQLConnection() — 이미 MSSQL 접속으로 전환됨
// INSERT 쿼리: Oracle W_GOODS_WET 테이블 + W_GOODS_WET_SEQ.NEXTVAL — MSSQL에 존재하지 않아 실행 불가
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
    + ", REG_TIME"
    + ", CHANNEL_CODE"
    + ", BOX_ORDER)"
    + " VALUES "
    + "(W_GOODS_WET_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

pstmt.setInt(1,    Integer.parseInt(splitData[0]));                       // GI_D_ID
pstmt.setDouble(2, (Double.parseDouble(splitData[1]) * 100) / 100.0);     // WEIGHT
pstmt.setString(3, splitData[2]);                                         // WEIGHT_UNIT
pstmt.setString(4, splitData[3]);                                         // PACKER_PRODUCT_CODE
pstmt.setString(5, splitData[4]);                                         // BARCODE
pstmt.setString(6, splitData[5]);                                         // PACKER_CLIENT_CODE
pstmt.setString(7, splitData[6]);                                         // MAKINGDATE
pstmt.setString(8, splitData[7]);                                         // BOXSERIAL
pstmt.setInt(9,    Integer.parseInt(splitData[8]));                       // BOX_CNT
pstmt.setString(10, splitData[9]);                                        // REG_ID
pstmt.setString(11, dateStr);                                             // REG_DATE (서버 자동)
pstmt.setString(12, timeStr);                                             // REG_TIME (서버 자동)
pstmt.setString(13, splitData[12]);                                       // CHANNEL_CODE ← splitData[12]
pstmt.setInt(14,   Integer.parseInt(splitData[13]));                      // BOX_ORDER ← splitData[13]
```

- 처리 방식: 단건 (`data.split("::")`, `##` 없음)
- 파라미터 14개 (PK 자동 생성 제외)
- INSERT 컬럼 15개 (PK 포함)

### 문제점

1. **MSSQL에 `W_GOODS_WET` 테이블 없음** — `getMSSQLConnection()`으로 MSSQL에 접속하지만 해당 테이블이 없어 실행 즉시 SQL 오류 발생
2. **Oracle 시퀀스 문법** — `W_GOODS_WET_SEQ.NEXTVAL`은 MSSQL에서 지원하지 않음. MSSQL은 `NEXT VALUE FOR SequenceName` 문법 사용
3. **컬럼명 불일치** — `SM_출고계근`은 한글 컬럼명 사용 (예: `출고상세SEQ`, `계근중량` 등)
4. **Oracle 전용 컬럼 2개 제거 필요** — `CHANNEL_CODE`, `BOX_ORDER`는 `SM_출고계근`에 없는 컬럼으로 제거 필요
5. **MSSQL 신규 컬럼 5개 추가 필요** — `출고LOTSEQ`(splitData[14]), `회사코드`(splitData[10]), `수정사원`(splitData[9]), `수정일자`(서버 자동), `수정시간`(서버 자동)은 Java가 이미 전송하고 있으나 현재 JSP에서 미사용
6. **`출고LOTSEQ` NOT NULL 제약** — splitData[14](GI_L_ID)가 빈 문자열이면 `Integer.parseInt()` 예외 발생 위험

---

## 2. 변경 구조

### 데이터 흐름

```
변경 전:
[Java: BixolonShipmentActivity]
  searchType=2/6 → 단건 packet 조립 ("::" 구분자, ## 없음, 15개 필드)
    ↓ HTTP POST
[JSP: insert_goods_wet_homeplus.jsp]
  getMSSQLConnection() → INSERT INTO W_GOODS_WET (Oracle 테이블 — MSSQL에 없음 → 실행 오류)
  W_GOODS_WET_SEQ.NEXTVAL (Oracle 시퀀스 — MSSQL에 없음 → 실행 오류)
    ↓ 14개 파라미터 세팅 (splitData[10],[11],[14] 미사용)
    ↓ executeUpdate → commit
    ↓ out.println("s") or "f"

변경 후:
[Java: BixolonShipmentActivity]
  searchType=2/6 → 단건 packet 조립 ("::" 구분자, ## 없음, 15개 필드) ← 변경 없음
    ↓ HTTP POST
[JSP: insert_goods_wet_homeplus.jsp]
  getMSSQLConnection() → INSERT INTO SM_출고계근 (MSSQL 테이블)
  NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ (MSSQL 시퀀스)
    ↓ 17개 파라미터 세팅 (splitData[10],[14] 신규 활용, splitData[12],[13] 제거)
    ↓ executeUpdate → commit
    ↓ out.println("s") or "f"
```

### 변경 전/후 INSERT 컬럼 비교

| 항목 | 변경 전 (Oracle W_GOODS_WET) | 변경 후 (MSSQL SM_출고계근) |
|------|:---------------------------:|:-------------------------:|
| 테이블명 | `W_GOODS_WET` | `SM_출고계근` |
| PK 시퀀스 | `W_GOODS_WET_SEQ.NEXTVAL` | `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ` |
| PK 컬럼 | `GOODS_WET_ID` | `SEQ` |
| GI_D_ID | `GI_D_ID` | `출고상세SEQ` |
| (없음) | - | `출고LOTSEQ` (splitData[14] 신규 활용) |
| WEIGHT | `WEIGHT` | `계근중량` |
| WEIGHT_UNIT | `WEIGHT_UNIT` | `계근중량단위` |
| PACKER_PRODUCT_CODE | `PACKER_PRODUCT_CODE` | `PPCODE` |
| BARCODE | `BARCODE` | `계근바코드` |
| PACKER_CLIENT_CODE | `PACKER_CLIENT_CODE` | `패커코드` |
| MAKINGDATE | `MAKINGDATE` | `제조일자` |
| BOXSERIAL | `BOXSERIAL` | `박스시리얼` |
| BOX_CNT | `BOX_CNT` | `계근순번` |
| REG_ID | `REG_ID` | `등록사원` |
| REG_DATE | `REG_DATE` | `등록일자` |
| REG_TIME | `REG_TIME` | `등록시간` |
| (없음) | - | `회사코드` (splitData[10] 신규 활용) |
| (없음) | - | `수정사원` (splitData[9] 재사용) |
| (없음) | - | `수정일자` (dateStr 재사용) |
| (없음) | - | `수정시간` (timeStr 재사용) |
| CHANNEL_CODE | `CHANNEL_CODE` | **제거** (MSSQL에 없음) |
| BOX_ORDER | `BOX_ORDER` | **제거** (MSSQL에 없음) |
| 총 컬럼 수 | 15개 | 18개 |
| 총 파라미터 수 | 14개 | 17개 |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **insert_goods_wet_homeplus.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\` | Oracle `W_GOODS_WET` → MSSQL `SM_출고계근` INSERT 쿼리 교체 + Oracle 시퀀스 → MSSQL 시퀀스 교체 |

**Java 측 수정 없음**: `BixolonShipmentActivity.java`의 packet 조립 코드(15개 필드, `::` 구분자)는 이미 `splitData[14]`(GI_L_ID), `splitData[10]`(회사코드)를 포함하고 있으므로 Java 측 수정 불필요.

---

## 4. 수정 상세

### 4.1 insert_goods_wet_homeplus.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_homeplus.jsp`

**변경 전 (Oracle INSERT 쿼리 + 파라미터 세팅, L49~82):**

```jsp
//SQL 
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
    + ", REG_TIME"
    + ", CHANNEL_CODE"
    + ", BOX_ORDER)"
    + " VALUES "
    + "(W_GOODS_WET_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
PreparedStatement pstmt = conn.prepareStatement(qry);    


pstmt.setInt(1, Integer.parseInt(splitData[0]));
pstmt.setDouble(2, (Double.parseDouble(splitData[1]) * 100) / 100.0);
pstmt.setString(3, splitData[2]);
pstmt.setString(4, splitData[3]);
pstmt.setString(5, splitData[4]);
pstmt.setString(6, splitData[5]);
pstmt.setString(7, splitData[6]);
pstmt.setString(8, splitData[7]);
pstmt.setInt(9, Integer.parseInt(splitData[8]));
pstmt.setString(10, splitData[9]);
pstmt.setString(11, dateStr);
pstmt.setString(12, timeStr);
pstmt.setString(13, splitData[12]);
pstmt.setInt(14, Integer.parseInt(splitData[13]));
```

**변경 후 (MSSQL INSERT 쿼리 + 파라미터 세팅):**

```jsp
//SQL
String qry = "INSERT INTO SM_출고계근(SEQ"
    + ", 출고상세SEQ"
    + ", 출고LOTSEQ"
    + ", 계근중량"
    + ", 계근중량단위"
    + ", ppCode"
    + ", 계근바코드"
    + ", 패커코드"
    + ", 제조일자"
    + ", 박스시리얼"
    + ", 계근순번"
    + ", 등록사원"
    + ", 등록일자"
    + ", 등록시간"
    + ", 회사코드"
    + ", 수정사원"
    + ", 수정일자"
    + ", 수정시간)"
    + " VALUES "
    + "(NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
PreparedStatement pstmt = conn.prepareStatement(qry);

pstmt.setInt(1, Integer.parseInt(splitData[0]));                         // 출고상세SEQ (GI_D_ID)
pstmt.setInt(2, Integer.parseInt(splitData[14]));                        // 출고LOTSEQ (GI_L_ID)
pstmt.setDouble(3, (Double.parseDouble(splitData[1]) * 100) / 100.0);   // 계근중량
pstmt.setString(4, splitData[2]);                                        // 계근중량단위
pstmt.setString(5, splitData[3]);                                        // ppCode
pstmt.setString(6, splitData[4]);                                        // 계근바코드
pstmt.setString(7, splitData[5]);                                        // 패커코드
pstmt.setString(8, splitData[6]);                                        // 제조일자
pstmt.setString(9, splitData[7]);                                        // 박스시리얼
pstmt.setInt(10, Integer.parseInt(splitData[8]));                        // 계근순번
pstmt.setString(11, splitData[9]);                                       // 등록사원
pstmt.setString(12, dateStr);                                            // 등록일자
pstmt.setString(13, timeStr);                                            // 등록시간
pstmt.setString(14, splitData[10]);                                      // 회사코드
pstmt.setString(15, splitData[9]);                                       // 수정사원
pstmt.setString(16, dateStr);                                            // 수정일자
pstmt.setString(17, timeStr);                                            // 수정시간
```

**검증**: 이마트 MSSQL 전환 완료 JSP(`insert_goods_wet.jsp`)와 INSERT 쿼리 구조(18개 컬럼, 17개 파라미터) 및 파라미터 세팅 순서가 동일한지 비교 확인

---

## 5. 사이드이펙트

### BixolonShipmentActivity.java (호출 측 — 변경 없음)

```java
// Common.java L31
public static final String URL_INSERT_GOODS_WET_HOMEPLUS = BASE_URL + "/insert_goods_wet_homeplus.jsp";

// BixolonShipmentActivity.java L2651~2652 (구 로직 분기, searchType=2/6)
// BixolonShipmentActivity.java L2741~2742 (신 로직 배치 분기, searchType=2)
HttpHelper.sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS);
```

- Java 측 packet 구조(15개 필드, `::` 구분자)는 이미 `splitData[14]`(GI_L_ID), `splitData[10]`(회사코드)를 포함하고 있어 **Java 측 코드 변경 불필요**
- JSP URL, HTTP 파라미터 이름(`data`, `dbid`), 응답 형식(`"s"` / `"f"`)은 변경 없음 → **Java 측 응답 처리 코드 영향 없음**
- **searchType=5(홈플러스 비정량)**: 본 JSP를 사용하지 않음(실제 코드에서 `URL_INSERT_GOODS_WET_NEW` 호출) → **영향 없음**
- **searchType=6(롯데)**: 동일 JSP 사용 → 전환 후 롯데 계근 전송도 함께 동작 검증 필요

---

## 6. 데이터 저장 구조

### splitData[] 인덱스별 매핑 (Java packet → JSP → SM_출고계근)

| splitData[] | Java 필드명 | 변경 전 Oracle 컬럼 | 변경 후 MSSQL 컬럼 | 파라미터# (후) | 타입 |
|:-----------:|-----------|:-----------------:|:-----------------:|:------------:|------|
| [0] | GI_D_ID | GI_D_ID | 출고상세SEQ | 1 | INT |
| [1] | WEIGHT | WEIGHT | 계근중량 | 3 | DOUBLE |
| [2] | WEIGHT_UNIT | WEIGHT_UNIT | 계근중량단위 | 4 | String |
| [3] | PACKER_PRODUCT_CODE | PACKER_PRODUCT_CODE | ppCode | 5 | String |
| [4] | BARCODE | BARCODE | 계근바코드 | 6 | String |
| [5] | PACKER_CLIENT_CODE | PACKER_CLIENT_CODE | 패커코드 | 7 | String |
| [6] | MAKINGDATE | MAKINGDATE | 제조일자 | 8 | String |
| [7] | BOXSERIAL | BOXSERIAL | 박스시리얼 | 9 | String |
| [8] | BOX_CNT | BOX_CNT | 계근순번 | 10 | INT |
| [9] | REG_ID | REG_ID | 등록사원 / 수정사원 | 11 / 15 | String |
| [10] | Common.selectCompanyCode | **미사용** | 회사코드 | 14 | String (신규 활용) |
| [11] | BRAND_CODE | **미사용** | **미사용** | - | - |
| [12] | CLIENT_TYPE | CHANNEL_CODE | **제거** | - | - |
| [13] | BOX_ORDER | BOX_ORDER | **제거** | - | - |
| [14] | GI_L_ID | **미사용** | 출고LOTSEQ | 2 | INT (신규 활용) |
| (서버) | dateStr | REG_DATE | 등록일자 / 수정일자 | 12 / 16 | String |
| (서버) | timeStr | REG_TIME | 등록시간 / 수정시간 | 13 / 17 | String |

### SM_출고계근 최종 INSERT 컬럼 구조 (18개)

```
SM_출고계근 컬럼:
  SEQ           ← NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ (자동)
  출고상세SEQ   ← splitData[0]  (파라미터 1)
  출고LOTSEQ    ← splitData[14] (파라미터 2)
  계근중량      ← splitData[1]  (파라미터 3)
  계근중량단위  ← splitData[2]  (파라미터 4)
  ppCode        ← splitData[3]  (파라미터 5)
  계근바코드    ← splitData[4]  (파라미터 6)
  패커코드      ← splitData[5]  (파라미터 7)
  제조일자      ← splitData[6]  (파라미터 8)
  박스시리얼    ← splitData[7]  (파라미터 9)
  계근순번      ← splitData[8]  (파라미터 10)
  등록사원      ← splitData[9]  (파라미터 11)
  등록일자      ← dateStr       (파라미터 12, 서버 자동)
  등록시간      ← timeStr       (파라미터 13, 서버 자동)
  회사코드      ← splitData[10] (파라미터 14)
  수정사원      ← splitData[9]  (파라미터 15, 등록사원과 동일값)
  수정일자      ← dateStr       (파라미터 16, 서버 자동)
  수정시간      ← timeStr       (파라미터 17, 서버 자동)
```

---

## 7. 호출 시점

```
[BixolonShipmentActivity — 계근 완료 후 전송 버튼 클릭]
    ↓
[ProgressDlgShipmentSend.doInBackground()]
    ↓ TB_GOODS_WET에서 SAVE_TYPE='F' 레코드 SELECT (DBHandler.selectquerySendGoodsWet())
    ↓
    [구 로직 분기 — L2620~2696]
    searchType=2 또는 searchType=6
        ↓ 건별 packet 조립 (15개 필드, :: 구분자)
        ↓ ★ HttpHelper.sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS)
        ↓ insert_goods_wet_homeplus.jsp 호출 (단건 처리)
        ↓ 응답 "s" → TB_GOODS_WET.SAVE_TYPE = 'F' → 'Y' UPDATE
    ↓
    [신 로직 배치 분기 — L2741~2742]
    searchType=2
        ↓ ★ HttpHelper.sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS)
        ↓ insert_goods_wet_homeplus.jsp 호출 (단건 처리)
    ↓
[JSP: insert_goods_wet_homeplus.jsp]
    ↓ getMSSQLConnection()
    ↓ splitData = data.split("::")  ← 15개 필드
    ↓ INSERT INTO SM_출고계근 (18컬럼, 17파라미터)
    ↓ NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ
    ↓ executeUpdate() → conn.commit()
    ↓ out.println("s") 또는 out.println("f")
    ↓
[BixolonShipmentActivity]
    "s" 수신 → SAVE_TYPE F→Y UPDATE
```

---

## 8. 개발 플랜

### Step 1: SM_출고계근 테이블 컬럼 구조 확인

**Part 1. 분석**
- 대상 파일: `DlivyWeighEntity.java`, `insert_goods_wet.jsp` (이마트 MSSQL 전환 완료본)
- 범위: `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\java\com\sgis\domain\sm\release\entity\DlivyWeighEntity.java`
- 용도: SM_출고계근 테이블 컬럼명, 타입, NOT NULL 제약 조건을 이마트 완료 JSP와 교차 검증하여 전환 목표 컬럼 구조 확정
- 주의할 점: `출고LOTSEQ`는 `NOT NULL DEFAULT 0` 제약. splitData[14](GI_L_ID)가 빈 문자열인 경우 `Integer.parseInt()` 예외 발생 가능 — 예외 처리 또는 기본값 처리 방식 결정 필요

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | DlivyWeighEntity 컬럼 | DlivyWeighEntity.java | SEQ, 출고상세SEQ, 출고LOTSEQ, 계근순번, 계근중량, 계근중량단위, 계근바코드, PPCODE, 패커코드, 제조일자, 박스시리얼 + BaseEntity(등록사원, 등록일자, 등록시간, 회사코드, 수정사원, 수정일자, 수정시간) |
| 2 | 이마트 MSSQL 완료 JSP INSERT 구조 | insert_goods_wet.jsp | 18컬럼, 17파라미터, NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ |
| 3 | Oracle 원본 INSERT 구조 | insert_goods_wet_homeplus.jsp | 15컬럼(PK포함), 14파라미터, W_GOODS_WET_SEQ.NEXTVAL |

**Part 2. 변환 계획**
- 변환 방식: DlivyWeighEntity + 이마트 완료 JSP를 교차 검증하여 `SM_출고계근` 18컬럼 INSERT 구조 확정
- 주의사항: `출고LOTSEQ`(NOT NULL) — splitData[14] 빈 문자열 시 `Integer.parseInt("")` 예외 발생. 이마트 완료 JSP에서 동일 처리 방식 확인하여 동일하게 적용

**체크리스트**
- [ ] Part 1: DlivyWeighEntity.java 컬럼 목록 확인 완료
- [ ] Part 1: 이마트 완료 JSP(insert_goods_wet.jsp) INSERT 구조 확인 완료
- [ ] Part 2: 18컬럼 INSERT 구조 확정 (소스분석 54 §8.3 매핑표와 일치 검증)
- [ ] Part 2: splitData[14] 빈 문자열 처리 방식 확정
- [ ] Part 6: 변경 내용 작성

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 2: Oracle INSERT 쿼리 → MSSQL SM_출고계근 INSERT 쿼리로 교체

**Part 1. 분석**
- 대상 파일: `insert_goods_wet_homeplus.jsp`
- 범위: L49~82 (INSERT 쿼리 조립 + PreparedStatement 파라미터 세팅)
- 용도: Oracle `W_GOODS_WET` 15컬럼 INSERT를 MSSQL `SM_출고계근` 18컬럼 INSERT로 교체. CHANNEL_CODE, BOX_ORDER 제거. 출고LOTSEQ, 회사코드, 수정사원/일자/시간 신규 추가.
- 주의할 점: 파라미터 순서가 이마트 완료 JSP(`insert_goods_wet.jsp`)와 완전히 동일해야 함. `CHANNEL_CODE`(splitData[12])와 `BOX_ORDER`(splitData[13]) 파라미터 세팅 코드 삭제 필요.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 변경 전 쿼리 | L49~65 | W_GOODS_WET 15컬럼, W_GOODS_WET_SEQ.NEXTVAL, 14파라미터 |
| 2 | 변경 후 쿼리 | L49~68 | SM_출고계근 18컬럼, NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ, 17파라미터 |
| 3 | 파라미터 세팅 변경 | L69~82 → L70~88 | splitData[12],[13] 제거 / splitData[14],[10] 신규 추가 |

**Part 2. 변환 계획**
- 변환 방식: `insert_goods_wet.jsp`(이마트 완료본) INSERT 쿼리 + 파라미터 세팅 블록을 그대로 복사하여 적용. 파일명/Logger 변수명만 홈플러스 JSP에 맞게 유지.
- 주의사항:
  - 컬럼 순서가 이마트 JSP와 동일해야 함 (SEQ, 출고상세SEQ, 출고LOTSEQ, 계근중량, 계근중량단위, ppCode, 계근바코드, 패커코드, 제조일자, 박스시리얼, 계근순번, 등록사원, 등록일자, 등록시간, 회사코드, 수정사원, 수정일자, 수정시간)
  - `splitData[14]`(출고LOTSEQ) 빈 문자열 처리: Step 1에서 확정한 방식 적용
  - `split("::")`는 단건 구조 그대로 유지 (`##` 추가하지 않음)
  - commit 위치(executeUpdate 직후 1회) 변경 없음

**체크리스트**
- [ ] Part 1: 변경 전 쿼리 (L49~82) 정확히 파악 완료
- [ ] Part 2: INSERT 쿼리 18컬럼으로 교체 완료
- [ ] Part 2: 파라미터 세팅 17개로 교체 완료 (CHANNEL_CODE, BOX_ORDER 제거 / 출고LOTSEQ, 회사코드, 수정사원/일자/시간 추가)
- [ ] Part 4: JSP 문법 오류 없음 확인 (Tomcat 재시작 후 500 오류 없음)
- [ ] Part 5: 이마트 완료 JSP와 INSERT 구조 동일성 비교 확인
- [ ] Part 6: 변경 내용 작성

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 3: Oracle 시퀀스 → MSSQL 시퀀스로 교체 (Step 2와 통합 진행)

> Step 2와 동일 블록(INSERT 쿼리 문자열)에 포함되어 있으므로 Step 2 진행 시 함께 처리됨.
> 독립 Step으로 분리하여 별도 체크리스트를 통해 명시적으로 검증.

**Part 1. 분석**
- 대상 파일: `insert_goods_wet_homeplus.jsp`
- 범위: L65 (`W_GOODS_WET_SEQ.NEXTVAL` 사용 위치)
- 용도: Oracle 시퀀스 문법을 MSSQL 시퀀스 문법으로 교체
- 주의할 점: MSSQL은 `NEXT VALUE FOR 시퀀스명` 문법 사용. `INSERT INTO 테이블(SEQ, ...) VALUES (NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ, ?, ?, ...)` 형태로 VALUES 절에 직접 기재.

| # | 항목 | 변경 전 | 변경 후 |
|---|------|---------|---------|
| 1 | 시퀀스 문법 | `W_GOODS_WET_SEQ.NEXTVAL` | `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ` |
| 2 | PK 컬럼명 | `GOODS_WET_ID` | `SEQ` |
| 3 | VALUES 첫 번째 값 | `W_GOODS_WET_SEQ.NEXTVAL` | `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ` |

**Part 2. 변환 계획**
- 변환 방식: Step 2의 INSERT 쿼리 교체 시 자동으로 반영됨. 별도 코드 변경 없이 Step 2 결과 검증만 수행.
- 주의사항: `SM_DLIVY_WEIGH_SEQ` 시퀀스가 MSSQL DB에 실제로 존재하는지 이마트 완료 JSP 동작으로 검증됨 — 별도 확인 불필요.

**체크리스트**
- [ ] Part 1: 변경 후 쿼리에 `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ` 포함 확인
- [ ] Part 1: `W_GOODS_WET_SEQ.NEXTVAL` 문자열이 JSP에 더 이상 없음 확인
- [ ] Part 1: `GOODS_WET_ID` 컬럼명이 `SEQ`로 변경됨 확인
- [ ] Part 5: 이마트 완료 JSP와 시퀀스 문법 동일성 확인
- [ ] Part 6: 변경 내용 작성

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 4: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | Tomcat 재시작 후 `insert_goods_wet_homeplus.jsp` 500 오류 없음 | □ |
| 2 | searchType=2(홈플러스 정량) — PDA에서 계근 완료 후 서버 전송 실행 | □ |
| 3 | 응답 "s" 수신 확인 (TB_GOODS_WET SAVE_TYPE F→Y 업데이트 확인) | □ |
| 4 | MSSQL `SM_출고계근` 테이블에 행 INSERT 확인 (SEQ 자동 생성 확인) | □ |
| 5 | INSERT된 행의 `출고상세SEQ`, `출고LOTSEQ`, `계근중량`, `회사코드` 값 검증 | □ |
| 6 | INSERT된 행의 `등록사원`, `등록일자`, `등록시간` 값 검증 (서버 시각 자동 생성) | □ |
| 7 | INSERT된 행의 `수정사원`, `수정일자`, `수정시간` 값 검증 (등록사원/일자/시간과 동일) | □ |
| 8 | searchType=6(롯데) — 동일 JSP를 사용하는 롯데 계근 전송 동작 확인 | □ |
| 9 | 이마트 계근 전송(searchType=0, insert_goods_wet.jsp) 정상 동작 여부 확인 (영향 없음 검증) | □ |
| 10 | 공용 배치 계근 전송(searchType=4/5, insert_goods_wet_new.jsp) 정상 동작 여부 확인 (영향 없음 검증) | □ |

---

### 개발 순서 요약

```
Step 1: SM_출고계근 테이블 컬럼 구조 확인
    ↓ DlivyWeighEntity + 이마트 완료 JSP 교차 검증 → 18컬럼 구조 확정
Step 2: Oracle INSERT 쿼리 → MSSQL SM_출고계근 INSERT 쿼리로 교체
    ↓ W_GOODS_WET 15컬럼 → SM_출고계근 18컬럼 교체 + 파라미터 세팅 교체
Step 3: Oracle 시퀀스 → MSSQL 시퀀스로 교체
    ↓ W_GOODS_WET_SEQ.NEXTVAL → NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ (Step 2와 통합)
Step 4: 통합 테스트
    ↓ searchType=2/6 계근 전송 동작 확인 + SM_출고계근 INSERT 확인
```

---

## 9. 테스트 시나리오

### 시나리오 1: 홈플러스 정량(searchType=2) 계근 전송 정상 동작 확인

```
1. PDA 앱 실행 → 로그인 → searchType=2(홈플러스 정량) 선택
2. 출하대상 받기 실행 → 홈플러스 정량 출하 대상 목록 수신 확인
3. 바코드 스캔 → 계근 실행 → TB_GOODS_WET에 SAVE_TYPE='F' 레코드 생성 확인
4. 계근 데이터 전송(서버 전송) 버튼 클릭
5. insert_goods_wet_homeplus.jsp 호출 확인 (Tomcat 로그)
6. 응답 "s" 수신 → TB_GOODS_WET SAVE_TYPE 'F'→'Y' 업데이트 확인
7. MSSQL SM_출고계근 테이블에 행 INSERT 확인
   - SEQ: NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ 자동 생성
   - 출고상세SEQ: splitData[0] (GI_D_ID 값)
   - 출고LOTSEQ: splitData[14] (GI_L_ID 값)
   - 계근중량: splitData[1]
   - 회사코드: splitData[10] (Common.selectCompanyCode)
   - 등록일자/시간: 서버 현재 시각 (yyyyMMdd / HHmmss)
```

### 시나리오 2: 롯데(searchType=6) 계근 전송 동작 확인

```
1. PDA 앱 실행 → 로그인 → searchType=6(롯데) 선택
2. 출하대상 받기 실행 → 롯데 출하 대상 목록 수신 확인
3. 계근 실행 → TB_GOODS_WET에 SAVE_TYPE='F' 레코드 생성 확인
4. 계근 데이터 전송(서버 전송) 버튼 클릭
5. insert_goods_wet_homeplus.jsp 호출 확인 (Tomcat 로그)
6. 응답 "s" 수신 → SAVE_TYPE 'F'→'Y' 업데이트 확인
7. MSSQL SM_출고계근 테이블에 행 INSERT 확인
```

### 시나리오 3: 이마트/비정량 계근 전송 영향 없음 확인

```
1. searchType=0(이마트) 계근 전송 실행
   → insert_goods_wet.jsp 호출 (홈플러스 JSP와 별개) → 정상 동작 확인
2. searchType=4(비정량) 계근 전송 실행
   → insert_goods_wet_new.jsp 호출 (홈플러스 JSP와 별개) → 정상 동작 확인
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | `splitData[14]` 빈 문자열로 `Integer.parseInt("")` 예외 발생 | TB_GOODS_WET의 GI_L_ID가 빈 문자열인 경우 | `splitData[14]`가 비어 있으면 `0`으로 대체: `Integer.parseInt(splitData[14].isEmpty() ? "0" : splitData[14])`. 이마트 완료 JSP의 처리 방식 우선 확인 |
| 2 | `SM_출고계근` 테이블 없음 오류 | MSSQL DB에 테이블이 생성되지 않은 경우 | 이마트 완료 JSP(insert_goods_wet.jsp)가 정상 동작 중이므로 테이블 존재 확인됨. 동일 테이블 사용으로 문제 없음 |
| 3 | `SM_DLIVY_WEIGH_SEQ` 시퀀스 없음 오류 | MSSQL DB에 시퀀스가 생성되지 않은 경우 | 이마트 완료 JSP(insert_goods_wet.jsp)가 이미 동일 시퀀스 사용 중. 정상 동작 중이므로 시퀀스 존재 확인됨 |
| 4 | 패커코드(VARCHAR(6)) 길이 초과 | Java에서 전송하는 PACKER_CLIENT_CODE 값이 6자 초과인 경우 | 이마트 전환 시 동일 컬럼 사용 중이며 문제 없었음. 현재 데이터 기준 6자 이내로 판단 |
| 5 | 롯데(searchType=6) 계근 전송 오류 | splitData 구조가 searchType=2와 다른 경우 | 소스분석 54 §4.2에서 searchType=6도 동일 packet 구조(15필드) 확인됨. 동일 변환 적용 가능 |
| 6 | CHANNEL_CODE / BOX_ORDER 데이터 유실 | Oracle 전용 컬럼 제거로 기존 데이터 미저장 | Oracle W_GOODS_WET 전환 완료 후 해당 컬럼은 MSSQL에 대응 컬럼이 없어 유실됨. 이마트 전환 시 동일하게 처리됨 — 정상 처리 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | SM_출고계근 테이블 컬럼 구조 확인 | ⏳ 대기 |
| 2 | Oracle INSERT 쿼리 → MSSQL SM_출고계근 INSERT 쿼리로 교체 | ⏳ 대기 |
| 3 | Oracle 시퀀스 → MSSQL 시퀀스로 교체 | ⏳ 대기 |
| 4 | 통합 테스트 | ⏳ 대기 |

---

**문서 버전**: 1.0

---

## 관련 문서

- `app/doc/소스분석/54_홈플러스_계근데이터전송_JSP_MSSQL전환전_구조분석.md` — 본 개발의 사전 구조 분석 (splitData 매핑, Oracle/MSSQL 컬럼 비교, 변경 범위 요약)
- `app/doc/소스분석/56_홈플러스정량_출하_전체흐름분석.md` — 홈플러스 정량 전체 흐름 §5: 3단계 서버 전송 위치 정의
- `app/doc/개발/37_비정량_계근데이터전송_JSP_MSSQL전환.md` — 공용 배치 계근 전송 MSSQL 전환 완료 사례 (배치 구조 비교 참조)
- **JSP 전환 대상**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_homeplus.jsp`
- **JSP 참조 (이마트 MSSQL 전환 완료)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet.jsp`
- **JSP 원본**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\insert_goods_wet_homeplus.jsp`
- **ERP Entity (MSSQL 목표 테이블)**: `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\java\com\sgis\domain\sm\release\entity\DlivyWeighEntity.java`
- **Java 호출 위치**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java:2652, 2742`
- **URL 상수**: `app/src/main/java/com/rgbsolution/highland_emart/common/Common.java:31`
