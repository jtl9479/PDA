# insert_goods_wet_homeplus.jsp - 홈플러스 계근 데이터 전송 JSP MSSQL 전환 전 구조 분석

## 개요

홈플러스 정량(searchType=2) 및 롯데(searchType=6) 계근 완료 데이터를 서버 DB에 INSERT하는 JSP.
현재 **Oracle 테이블(`W_GOODS_WET`) + Oracle 시퀀스(`W_GOODS_WET_SEQ.NEXTVAL`)** 기반으로 동작 중이며,
MSSQL 전환 전 구조를 완전히 파악하여 `SM_출고계근` 테이블로의 전환 범위를 사전 정의하는 것이 목적이다.

이마트(insert_goods_wet.jsp)는 MSSQL 전환 완료, 공용 배치(insert_goods_wet_new.jsp)는 MSSQL 전환 완료 상태이며,
홈플러스(본 JSP)는 **아직 Oracle 기반** 상태로 남아있다.

- **파일 경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_homeplus.jsp`
- **원본 파일 경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\insert_goods_wet_homeplus.jsp`
- **패키지**: JSP (독립 스크립트)
- **총 라인 수**: 111줄
- **타입**: JSP (계근 데이터 전송 — INSERT 전용)
- **작성일**: 2026-04-30

---

## 1. 역할

- PDA 앱에서 계근 완료 후 서버 DB에 계근 결과를 INSERT
- 호출 대상: searchType=2(홈플러스 정량), searchType=6(롯데) — 단, 롯데는 ShipmentActivity에서 호출
- 처리 방식: **단건 처리** (`data.split("::")` → 1행 INSERT)
- `##` 배치 구분자 없음 — 이마트 단건 구조와 동일한 패턴
- 단, 홈플러스는 Java 측에서도 **단건 건별 HTTP 호출 방식** 사용 (배치 전송 아님)
- Oracle `W_GOODS_WET` 테이블에 INSERT (MSSQL 전환 전 상태)
- Oracle 시퀀스 `W_GOODS_WET_SEQ.NEXTVAL` 사용 (MSSQL 전환 전 상태)

---

## 2. 주요 상수/필드

| 상수/필드 | 타입 | 값 | 용도 |
|----------|------|-----|------|
| INSERT 대상 테이블 | String | `W_GOODS_WET` | Oracle 계근 테이블 (전환 전) |
| 시퀀스 | String | `W_GOODS_WET_SEQ.NEXTVAL` | Oracle 자동 PK 생성 (전환 전) |
| 컬럼 수 | int | 15개 (PK 포함) | GOODS_WET_ID(SEQ) + 14개 |
| ? 파라미터 수 | int | 14개 | PK 제외 |
| data 구분자 | String | `"::"` | 단건 컬럼 구분자 |
| 배치 구분자 | - | 없음 | 단건 처리 — `##` 없음 |
| DB 접속 방식 | String | `getMSSQLConnection()` | 현재 MSSQL 연결 (테이블은 Oracle 것 그대로) |
| 인코딩 | String | UTF-8 | `request.setCharacterEncoding("UTF-8")` |
| 입력 파라미터 | String | `data`, `dbid` | HTTP 요청 파라미터 |

> 주의: 현재 `getMSSQLConnection()`으로 MSSQL에 연결하면서 Oracle 전용 테이블(`W_GOODS_WET`)과 Oracle 시퀀스(`W_GOODS_WET_SEQ.NEXTVAL`)를 그대로 사용하고 있다.
> MSSQL에 해당 테이블/시퀀스가 없으면 **실행 시 SQL 오류**가 발생한다.

---

## 3. 주요 메서드

JSP는 단일 스크립트 블록이므로 메서드 단위 분리 없음. 주요 동작 블록으로 표기.

| 동작 블록 | 위치(줄) | 반환 | 용도 |
|----------|:--------:|------|------|
| DB 연결 | L22~31 | - | `getMSSQLConnection()` 호출 |
| 데이터 수신 및 split | L33 | - | `data.split("::")` → splitData[] |
| 날짜/시간 생성 | L40~46 | - | `SimpleDateFormat` — 서버 현재 시각 |
| INSERT 쿼리 조립 | L49~65 | - | `W_GOODS_WET` 테이블 15컬럼 INSERT |
| PreparedStatement 파라미터 세팅 | L69~82 | - | splitData[0]~[13] 14개 파라미터 |
| executeUpdate | L86 | - | INSERT 실행 (1건) |
| commit | L87 | - | INSERT 직후 즉시 commit |
| 응답 출력 | L100 | - | `out.println("s")` (성공) / `out.println("f")` (실패) |
| rollback | L106 | - | 예외 발생 시 롤백 |

---

## 4. 호출 관계

### 4.1 이 파일이 호출하는 대상

| 호출 대상 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| db_connection.jsp (include) | getMSSQLConnection() | L10, L23 | DB 연결 |
| Oracle W_GOODS_WET | INSERT | L49~65 | 계근 데이터 저장 |
| Oracle W_GOODS_WET_SEQ | NEXTVAL | L65 | PK 자동생성 |

### 4.2 이 파일을 호출하는 곳

| 호출 위치 | 메서드/분기 | 위치(줄) | searchType |
|----------|-----------|:--------:|:----------:|
| BixolonShipmentActivity.java | ProgressDlgShipmentSend.doInBackground() L2652 | L2651~2652 | 2 (홈플러스), 6 (롯데) — 구 로직 분기 |
| BixolonShipmentActivity.java | ProgressDlgShipmentSend.doInBackground() L2742 | L2741~2742 | 2 (홈플러스) — 신 로직 배치 분기 |
| Common.java | URL_INSERT_GOODS_WET_HOMEPLUS | L31 | URL 상수 정의 |

**URL 상수 (Common.java:31줄)**:
```java
public static final String URL_INSERT_GOODS_WET_HOMEPLUS = BASE_URL + "/insert_goods_wet_homeplus.jsp";
```

---

## 5. 데이터 흐름

```
[PDA 앱 - BixolonShipmentActivity.java]
    ↓ list_send_info = DBHandler.selectquerySendGoodsWet() (TB_GOODS_WET에서 SAVE_TYPE='F' 조회)
    ↓
    [구 로직 분기 L2620~2696] searchType=2 → 1건씩 packet 조립
        packet =  GI_D_ID::WEIGHT::WEIGHT_UNIT::PACKER_PRODUCT_CODE::BARCODE::
                  PACKER_CLIENT_CODE::MAKINGDATE::BOXSERIAL::BOX_CNT::REG_ID::
                  Common.selectCompanyCode::BRAND_CODE::CLIENT_TYPE::BOX_ORDER::GI_L_ID
        (15필드, :: 구분, ## 없음, 단건 HTTP POST)
    ↓ HttpHelper.sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS)
    ↓
[JSP: insert_goods_wet_homeplus.jsp]
    ↓ getMSSQLConnection() — MSSQL 연결
    ↓ splitData = data.split("::") — 단건 파싱
    ↓ INSERT INTO W_GOODS_WET (Oracle 테이블)
    ↓ W_GOODS_WET_SEQ.NEXTVAL (Oracle 시퀀스)
    ↓ 14개 파라미터 세팅 (splitData[0]~[9], [12], [13] + dateStr + timeStr)
    ↓ executeUpdate (1건) → commit
    ↓ out.println("s") 또는 out.println("f")
    ↓
[PDA 앱] "s" 수신 → TB_GOODS_WET SAVE_TYPE F→Y 업데이트
```

---

## 6. 핵심 코드

### 6.1 현재 JSP INSERT 쿼리 (Oracle 기반, L49~65)

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
    + ", REG_TIME"
    + ", CHANNEL_CODE"
    + ", BOX_ORDER)"
    + " VALUES "
    + "(W_GOODS_WET_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
```

### 6.2 현재 JSP 파라미터 세팅 (L69~82)

```jsp
pstmt.setInt(1,    Integer.parseInt(splitData[0]));                          // GI_D_ID
pstmt.setDouble(2, (Double.parseDouble(splitData[1]) * 100) / 100.0);        // WEIGHT
pstmt.setString(3, splitData[2]);                                            // WEIGHT_UNIT
pstmt.setString(4, splitData[3]);                                            // PACKER_PRODUCT_CODE
pstmt.setString(5, splitData[4]);                                            // BARCODE
pstmt.setString(6, splitData[5]);                                            // PACKER_CLIENT_CODE
pstmt.setString(7, splitData[6]);                                            // MAKINGDATE
pstmt.setString(8, splitData[7]);                                            // BOXSERIAL
pstmt.setInt(9,    Integer.parseInt(splitData[8]));                          // BOX_CNT
pstmt.setString(10, splitData[9]);                                           // REG_ID
pstmt.setString(11, dateStr);                                                // REG_DATE (서버 자동)
pstmt.setString(12, timeStr);                                                // REG_TIME (서버 자동)
pstmt.setString(13, splitData[12]);                                          // CHANNEL_CODE ← splitData[12] !
pstmt.setInt(14,   Integer.parseInt(splitData[13]));                         // BOX_ORDER ← splitData[13]
```

### 6.3 BixolonShipmentActivity.java packet 조립 (구 로직 분기, L2624~2639)

```java
String packet = "";
packet += list_send_info.get(i).getGI_D_ID() + "::";          // splitData[0] = GI_D_ID
packet += list_send_info.get(i).getWEIGHT() + "::";            // splitData[1] = WEIGHT
packet += list_send_info.get(i).getWEIGHT_UNIT() + "::";       // splitData[2] = WEIGHT_UNIT
packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::";// splitData[3] = PACKER_PRODUCT_CODE
packet += list_send_info.get(i).getBARCODE() + "::";            // splitData[4] = BARCODE
packet += list_send_info.get(i).getPACKER_CLIENT_CODE() + "::"; // splitData[5] = PACKER_CLIENT_CODE
packet += list_send_info.get(i).getMAKINGDATE() + "::";         // splitData[6] = MAKINGDATE
packet += list_send_info.get(i).getBOXSERIAL() + "::";          // splitData[7] = BOXSERIAL
packet += list_send_info.get(i).getBOX_CNT() + "::";            // splitData[8] = BOX_CNT
packet += list_send_info.get(i).getREG_ID() + "::";             // splitData[9] = REG_ID
packet += Common.selectCompanyCode + "::";                       // splitData[10] = 회사코드 (JSP 미사용)
packet += list_send_info.get(i).getBRAND_CODE() + "::";          // splitData[11] = BRAND_CODE (JSP 미사용)
packet += list_send_info.get(i).getCLIENT_TYPE() + "::";         // splitData[12] = CLIENT_TYPE → JSP: CHANNEL_CODE
packet += list_send_info.get(i).getBOX_ORDER() + "::";           // splitData[13] = BOX_ORDER → JSP: BOX_ORDER
packet += list_send_info.get(i).getGI_L_ID();                   // splitData[14] = GI_L_ID (JSP 미사용)
```

---

## 7. splitData[] 인덱스별 컬럼 매핑 상세표

### 7.1 Java packet → JSP splitData[] → Oracle W_GOODS_WET 매핑

| splitData[] | Java 필드명 | JSP 파라미터 # | Oracle 컬럼명 | 타입 | JSP 사용 여부 |
|:-----------:|-----------|:-------------:|-------------|------|:------------:|
| [0] | GI_D_ID | 1 | GI_D_ID | INT | O |
| [1] | WEIGHT | 2 | WEIGHT | DOUBLE | O |
| [2] | WEIGHT_UNIT | 3 | WEIGHT_UNIT | String | O |
| [3] | PACKER_PRODUCT_CODE | 4 | PACKER_PRODUCT_CODE | String | O |
| [4] | BARCODE | 5 | BARCODE | String | O |
| [5] | PACKER_CLIENT_CODE | 6 | PACKER_CLIENT_CODE | String | O |
| [6] | MAKINGDATE | 7 | MAKINGDATE | String | O |
| [7] | BOXSERIAL | 8 | BOXSERIAL | String | O |
| [8] | BOX_CNT | 9 | BOX_CNT | INT | O |
| [9] | REG_ID | 10 | REG_ID | String | O |
| (서버) | dateStr | 11 | REG_DATE | String | 서버 자동 |
| (서버) | timeStr | 12 | REG_TIME | String | 서버 자동 |
| [10] | Common.selectCompanyCode | - | **미사용** | - | **X** |
| [11] | BRAND_CODE | - | **미사용** | - | **X** |
| [12] | CLIENT_TYPE | 13 | CHANNEL_CODE | String | O (명칭 불일치 주의) |
| [13] | BOX_ORDER | 14 | BOX_ORDER | INT | O |
| [14] | GI_L_ID | - | **미사용** | - | **X** |

**핵심 발견**: Java가 15개 필드를 전송하지만 JSP는 12개만 실제 INSERT.
- `splitData[10]`(회사코드), `splitData[11]`(BRAND_CODE), `splitData[14]`(GI_L_ID): **JSP에서 완전 미사용**
- `splitData[12]`(CLIENT_TYPE)가 Oracle 컬럼명 `CHANNEL_CODE`에 매핑됨 (필드명 불일치)

---

## 8. Oracle W_GOODS_WET vs MSSQL SM_출고계근 컬럼 매핑표

### 8.1 Oracle W_GOODS_WET (홈플러스 JSP 현재 사용)

홈플러스 JSP가 INSERT하는 15개 컬럼 (PK 포함):

| Oracle 컬럼명 | 타입 | NOT NULL | 설명 |
|-------------|------|:--------:|------|
| GOODS_WET_ID | NUMBER | O | PK (W_GOODS_WET_SEQ.NEXTVAL) |
| GI_D_ID | NUMBER | O | 출고상세 ID |
| WEIGHT | NUMBER | - | 계근중량 |
| WEIGHT_UNIT | VARCHAR2 | - | 계근중량단위 |
| PACKER_PRODUCT_CODE | VARCHAR2 | - | 패커상품코드 |
| BARCODE | VARCHAR2 | - | 계근바코드 |
| PACKER_CLIENT_CODE | VARCHAR2 | - | 패커코드 |
| MAKINGDATE | VARCHAR2 | - | 제조일자 |
| BOXSERIAL | VARCHAR2 | - | 박스시리얼 |
| BOX_CNT | NUMBER | - | 계근순번 |
| REG_ID | VARCHAR2 | - | 등록사원 |
| REG_DATE | VARCHAR2 | - | 등록일자 (서버 자동) |
| REG_TIME | VARCHAR2 | - | 등록시간 (서버 자동) |
| CHANNEL_CODE | VARCHAR2 | - | 채널코드 (Java CLIENT_TYPE) |
| BOX_ORDER | NUMBER | - | 박스 순번 |

### 8.2 MSSQL SM_출고계근 (전환 목표 테이블, DlivyWeighEntity.java 기준)

| MSSQL 컬럼명 | Java 필드명 | 타입 | NOT NULL | columnDefinition |
|-----------|-----------|------|:--------:|-----------------|
| SEQ | seq | BIGINT | O | PK (NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ) |
| 출고상세SEQ | dlivyDetailSeq | BIGINT | O | DEFAULT 0 |
| 출고LOTSEQ | dlivyLotSeq | BIGINT | O | DEFAULT 0 |
| 계근순번 | weighSn | INT | O | DEFAULT 0 |
| 계근중량 | weighWt | FLOAT | O | DEFAULT 0 |
| 계근중량단위 | weighUnit | VARCHAR(10) | O | DEFAULT '' |
| 계근바코드 | weighBrcd | VARCHAR(50) | O | DEFAULT '' |
| PPCODE | ppCode | VARCHAR(20) | O | DEFAULT '' |
| 패커코드 | packerCode | VARCHAR(6) | O | DEFAULT '' |
| 제조일자 | mnfcturDe | VARCHAR(8) | O | DEFAULT '00000000' |
| 박스시리얼 | boxSerial | VARCHAR(30) | O | DEFAULT '' |
| 등록사원 | (BaseEntity) | - | - | BaseEntity 상속 |
| 등록일자 | (BaseEntity) | - | - | BaseEntity 상속 |
| 등록시간 | (BaseEntity) | - | - | BaseEntity 상속 |
| 회사코드 | (BaseEntity) | - | - | BaseEntity 상속 |
| 수정사원 | (BaseEntity) | - | - | BaseEntity 상속 |
| 수정일자 | (BaseEntity) | - | - | BaseEntity 상속 |
| 수정시간 | (BaseEntity) | - | - | BaseEntity 상속 |

### 8.3 Oracle W_GOODS_WET → MSSQL SM_출고계근 컬럼 매핑

| Oracle 컬럼 | MSSQL 컬럼 | splitData[] | 비고 |
|------------|-----------|:-----------:|------|
| GOODS_WET_ID | SEQ | 자동 | W_GOODS_WET_SEQ.NEXTVAL → NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ |
| GI_D_ID | 출고상세SEQ | [0] | 타입 NUMBER→BIGINT |
| WEIGHT | 계근중량 | [1] | 타입 NUMBER→FLOAT |
| WEIGHT_UNIT | 계근중량단위 | [2] | |
| PACKER_PRODUCT_CODE | PPCODE | [3] | 컬럼명 변경 (ppCode로 단축) |
| BARCODE | 계근바코드 | [4] | 컬럼명 변경 |
| PACKER_CLIENT_CODE | 패커코드 | [5] | 컬럼명 변경 |
| MAKINGDATE | 제조일자 | [6] | 컬럼명 변경 |
| BOXSERIAL | 박스시리얼 | [7] | 컬럼명 변경 |
| BOX_CNT | 계근순번 | [8] | 컬럼명 변경 (용도 동일) |
| REG_ID | 등록사원 | [9] | 컬럼명 변경 (BaseEntity) |
| REG_DATE | 등록일자 | 서버 자동 | 컬럼명 변경 (BaseEntity) |
| REG_TIME | 등록시간 | 서버 자동 | 컬럼명 변경 (BaseEntity) |
| CHANNEL_CODE | **대응 없음** | [12] | MSSQL 테이블에 CHANNEL_CODE 컬럼 없음 — 제거 필요 |
| BOX_ORDER | **대응 없음** | [13] | MSSQL 테이블에 BOX_ORDER 컬럼 없음 — 제거 또는 대안 필요 |
| **없음** | 출고LOTSEQ | [14] | Oracle에 없음 → splitData[14](GI_L_ID) 활용 가능 |
| **없음** | 회사코드 | [10] | Oracle에 없음 → splitData[10](Common.selectCompanyCode) 활용 가능 |
| **없음** | 수정사원 | [9] | Oracle에 없음 → splitData[9](REG_ID와 동일값) 사용 |
| **없음** | 수정일자 | 서버 자동 | Oracle에 없음 |
| **없음** | 수정시간 | 서버 자동 | Oracle에 없음 |

---

## 9. 원본 비교 (Oracle 원본 vs 현재 JSP)

| 항목 | 원본 (Oracle JSP) | 현재 JSP | 동일 |
|------|-----------------|---------|:----:|
| DB 연결 방식 | `DriverManager.getConnection(url, dbid, "DBpassword")` (Oracle 직접) | `getMSSQLConnection()` | X |
| 인코딩 | `euc-kr` | UTF-8 | X |
| 대상 테이블 | `W_GOODS_WET` | `W_GOODS_WET` | O |
| 시퀀스 | `W_GOODS_WET_SEQ.NEXTVAL` | `W_GOODS_WET_SEQ.NEXTVAL` | O |
| INSERT 컬럼 수 | 15개 (PK 포함) | 15개 (PK 포함) | O |
| INSERT 컬럼 구성 | GOODS_WET_ID, GI_D_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE, BARCODE, PACKER_CLIENT_CODE, MAKINGDATE, BOXSERIAL, BOX_CNT, REG_ID, REG_DATE, REG_TIME, CHANNEL_CODE, BOX_ORDER | 동일 | O |
| 파라미터 수 | 14개 | 14개 | O |
| 처리 방식 | 단건 (split("::"), ## 없음) | 단건 (split("::"), ## 없음) | O |
| commit 시점 | INSERT 직후 | INSERT 직후 | O |
| 응답 | "s"/"f" | "s"/"f" | O |
| Logger 방식 | `logger.info()` (log4j) | `System.out.println()` | X |

**결론**: 현재 JSP는 DB 연결만 MSSQL로 변경, 인코딩 변경, 로거 방식 변경된 상태이며
테이블·시퀀스·쿼리 구조는 Oracle 원본과 완전히 동일하다.

---

## 10. 3종 JSP 구조 비교 (홈플러스 vs 이마트 vs 공용 배치)

| 항목 | 홈플러스 (본 문서) | 이마트 (MSSQL 완료) | 공용 배치 (MSSQL 완료) |
|------|:----------------:|:------------------:|:-------------------:|
| 파일명 | insert_goods_wet_homeplus.jsp | insert_goods_wet.jsp | insert_goods_wet_new.jsp |
| 처리 방식 | **단건** (:: 구분만) | **단건** (:: 구분만) | **배치** (## 행구분 + ::) |
| 대상 테이블 | W_GOODS_WET (Oracle) | SM_출고계근 (MSSQL) | SM_출고계근 (MSSQL) |
| 시퀀스 | W_GOODS_WET_SEQ.NEXTVAL (Oracle) | NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ | NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ |
| INSERT 컬럼 수 | 15개 (PK 포함) | 18개 (PK 포함) | 18개 (PK 포함) |
| 출고LOTSEQ 포함 | **X** (splitData[14] 미사용) | O (splitData[14]=GI_L_ID) | O (splitData[14]=GI_L_ID) |
| 회사코드 포함 | **X** (splitData[10] 미사용) | O (splitData[10]) | O (splitData[10]) |
| 수정사원/일자/시간 | **X** | O (서버 자동) | O (서버 자동) |
| CHANNEL_CODE/BOX_ORDER | O (Oracle 전용) | **X** (MSSQL 없음) | **X** (MSSQL 없음) |
| for 루프 | 없음 | 없음 | O (## 단위 루프) |
| clearParameters | 없음 | 없음 | O (루프 내) |
| MSSQL 전환 상태 | **미전환** | 완료 | 완료 |
| searchType | 2 (홈플러스), 6 (롯데) | 0 (이마트) | 1, 3, 4, 5, 7 |

---

## 11. MSSQL 전환 시 변경 범위 요약

### 11.1 반드시 변경해야 하는 항목

| 변경 항목 | 변경 전 (Oracle) | 변경 후 (MSSQL) | 참조 |
|---------|:---------------:|:---------------:|------|
| 테이블명 | `W_GOODS_WET` | `SM_출고계근` | DlivyWeighEntity.java |
| 시퀀스 | `W_GOODS_WET_SEQ.NEXTVAL` | `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ` | DlivyWeighEntity.java |
| 컬럼명: GI_D_ID | `GI_D_ID` | `출고상세SEQ` | |
| 컬럼명: WEIGHT | `WEIGHT` | `계근중량` | |
| 컬럼명: WEIGHT_UNIT | `WEIGHT_UNIT` | `계근중량단위` | |
| 컬럼명: PACKER_PRODUCT_CODE | `PACKER_PRODUCT_CODE` | `PPCODE` | |
| 컬럼명: BARCODE | `BARCODE` | `계근바코드` | |
| 컬럼명: PACKER_CLIENT_CODE | `PACKER_CLIENT_CODE` | `패커코드` | |
| 컬럼명: MAKINGDATE | `MAKINGDATE` | `제조일자` | |
| 컬럼명: BOXSERIAL | `BOXSERIAL` | `박스시리얼` | |
| 컬럼명: BOX_CNT | `BOX_CNT` | `계근순번` | |
| 컬럼명: REG_ID/DATE/TIME | `REG_ID`, `REG_DATE`, `REG_TIME` | `등록사원`, `등록일자`, `등록시간` | BaseEntity |

### 11.2 추가해야 하는 항목 (MSSQL 테이블에 있으나 현재 JSP 미포함)

| 추가 컬럼 | MSSQL 컬럼명 | 값 출처 | 비고 |
|---------|-----------|--------|------|
| 출고LOTSEQ | 출고LOTSEQ | splitData[14] (GI_L_ID) | NOT NULL 제약 — 반드시 추가 |
| 회사코드 | 회사코드 | splitData[10] (Common.selectCompanyCode) | BaseEntity, 멀티회사 환경 필수 |
| 수정사원 | 수정사원 | splitData[9] (REG_ID와 동일값) | BaseEntity |
| 수정일자 | 수정일자 | dateStr (서버 자동) | BaseEntity |
| 수정시간 | 수정시간 | timeStr (서버 자동) | BaseEntity |

### 11.3 제거해야 하는 항목 (Oracle에만 있고 MSSQL에 없음)

| 제거 컬럼 | Oracle 컬럼명 | splitData[] | 이유 |
|---------|-----------|:-----------:|------|
| CHANNEL_CODE | CHANNEL_CODE | [12] | MSSQL SM_출고계근에 해당 컬럼 없음 |
| BOX_ORDER | BOX_ORDER | [13] | MSSQL SM_출고계근에 해당 컬럼 없음 |

### 11.4 MSSQL 전환 후 예상 INSERT 구조 (이마트 패턴 적용 시 — 18개 컬럼)

```sql
INSERT INTO SM_출고계근(SEQ, 출고상세SEQ, 출고LOTSEQ, 계근중량, 계근중량단위,
    PPCODE, 계근바코드, 패커코드, 제조일자, 박스시리얼, 계근순번,
    등록사원, 등록일자, 등록시간, 회사코드, 수정사원, 수정일자, 수정시간)
VALUES (NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
```

→ 파라미터 17개: splitData[0], [14], [1], [2], [3], [4], [5], [6], [7], [8], [9], dateStr, timeStr, [10], [9], dateStr, timeStr

### 11.5 searchType=5(홈플러스 비정량) 영향 범위

- 문서 상단 주석(L2568~2569)에는 searchType=4,5를 `insert_goods_wet_homeplus.jsp`로 표기하고 있으나,
  실제 코드(L2743)에서 searchType=5는 `URL_INSERT_GOODS_WET_NEW`(공용 배치)를 호출한다.
- 따라서 본 JSP(insert_goods_wet_homeplus.jsp) 전환 시 **searchType=5에는 영향 없음**.
- 영향 범위: searchType=2(홈플러스 정량), searchType=6(롯데)

---

## 12. 주의사항

- 현재 JSP는 `getMSSQLConnection()`으로 MSSQL에 연결하면서 `W_GOODS_WET`(Oracle 테이블) + `W_GOODS_WET_SEQ.NEXTVAL`(Oracle 시퀀스)을 그대로 사용하고 있다. MSSQL DB에 해당 테이블/시퀀스가 없으면 **실행 즉시 SQL 오류** 발생.
- `CHANNEL_CODE`는 Java `CLIENT_TYPE` 필드값을 받는 컬럼이다. 컬럼명과 Java 필드명이 다른 점에 주의 (역할: 채널/클라이언트 유형 구분).
- `BOX_ORDER` 컬럼이 Oracle에는 있고 MSSQL SM_출고계근에는 없다. MSSQL 전환 시 제거 처리 필요.
- Java는 이미 **15개 필드를 `::` 구분**으로 전송한다. 단건 방식이므로 `##` 없음. JSP 전환 시 Java 코드 수정 불필요.
- `splitData[10]`(회사코드)과 `splitData[14]`(GI_L_ID=출고LOTSEQ)는 현재 JSP에서 미사용이지만 MSSQL 전환 후 필수 활용 대상이다.
- `출고LOTSEQ`는 MSSQL SM_출고계근에서 `NOT NULL DEFAULT 0` 제약 조건. `splitData[14]`(GI_L_ID)가 빈 문자열이면 `Integer.parseInt()` 예외 발생 → 전환 시 예외 처리 또는 기본값 처리 고려 필요.
- 단건 처리 구조(`##` 없음)는 MSSQL 전환 후에도 그대로 유지한다.
- searchType=6(롯데)도 동일 JSP를 사용하므로, 전환 시 롯데 계근 전송도 함께 검증 필요.
- 이마트 MSSQL 전환 후 INSERT 구조(18개 컬럼)를 참조하여 동일한 구조로 전환하는 것이 권장된다.

---

## 13. 관련 문서

- `app/doc/소스분석/47_이마트계근전송_vs_공용배치계근전송_JSP_원본비교분석.md` — Oracle 원본 2종 비교 분석 (단건 vs 배치 설계 철학)
- `app/doc/소스분석/52_홈플러스정량_출하계근대상_JSP_Java파싱_인덱스분석.md` — 홈플러스 정량 출하대상 조회 JSP/Java 인덱스 분석 (동일 searchType=2 사전 분석)
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` — 이마트 MSSQL 전환 완료 패턴 참조
- `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md` — 공용 배치 MSSQL 전환 완료 패턴 참조
- `app/doc/소스분석/37_로컬DB_MSSQL전환_현황.md` — TB_GOODS_WET ↔ SM_출고계근 매핑 (이마트 MSSQL 기준)
- **JSP 현재 (홈플러스 Oracle 미전환)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_homeplus.jsp`
- **JSP 원본**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\insert_goods_wet_homeplus.jsp`
- **MSSQL 전환 참조 (이마트 완료)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet.jsp`
- **MSSQL 전환 참조 (공용 배치 완료)**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_new.jsp`
- **Java 호출**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java:2652, 2742`
- **ERP Entity (MSSQL 목표 테이블)**: `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\java\com\sgis\domain\sm\release\entity\DlivyWeighEntity.java`
- **URL 상수**: `app/src/main/java/com/rgbsolution/highland_emart/common/Common.java:31`
