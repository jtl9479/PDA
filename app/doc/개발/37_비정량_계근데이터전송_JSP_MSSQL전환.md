# 비정량(searchType=4 포함 공용 배치) 계근데이터 전송 JSP MSSQL 전환

**작성일**: 2026-04-22
**목적**: 공용 배치 계근전송 JSP(`insert_goods_wet_new.jsp`)를 Oracle `W_GOODS_WET` 테이블 기반 INSERT에서 MSSQL `SM_출고계근` 테이블 기반 INSERT로 전환한다. Oracle 시퀀스(`W_GOODS_WET_SEQ.NEXTVAL`)는 MSSQL 시퀀스(`NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ`)로 전환. 배치 처리(`##` 행 구분자 + for 루프) 구조는 그대로 유지. Java 측(BixolonShipmentActivity/ShipmentActivity)은 수정하지 않는다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### insert_goods_wet_new.jsp (공용 배치 JSP, 전환 대상)

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_new.jsp`

**사용 searchType**: 1(생산), 3(도매), 4(비정량), 5(홈플러스비정량), 7(생산라벨) — 5개 공용

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
    + ", REG_TIME)"
    + " VALUES "
    + "(W_GOODS_WET_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?)";
```

- 13개 필드 INSERT (GOODS_WET_ID PK + 12개 파라미터)
- 배치 처리: `data.split("##")` → for 루프 → 각 행 `split("::")` → 파라미터 세팅 → executeUpdate
- commit: 루프 완료 후 1회

### 문제점

1. **MSSQL에 `W_GOODS_WET` 테이블 없음** — DB 접속은 MSSQL이지만 테이블이 없어 실행 시 오류
2. **Oracle 시퀀스 문법** — `W_GOODS_WET_SEQ.NEXTVAL`은 MSSQL에서 지원하지 않음. MSSQL은 `NEXT VALUE FOR SequenceName` 문법
3. **컬럼명이 영문 Oracle 스타일** — `SM_출고계근`은 한글 컬럼명 사용
4. **필드 수 부족 (13 vs SM_출고계근 요구 필드)** — Java는 이미 15개 필드(splitData[0]~[14])를 전송하고 있으나 현재 JSP는 10개(splitData[0]~[9])만 사용. SM_출고계근에 회사코드/출고LOTSEQ 등 필수 컬럼이 있어 이를 활용해야 함
5. **참고**: 이마트 `insert_goods_wet.jsp`는 이미 같은 MSSQL 전환을 완료했으며, `SM_출고계근`에 18개 필드를 INSERT 중 (개발32 선례). 본 JSP는 **이마트 MSSQL 버전의 INSERT 쿼리 구조를 그대로 재사용**하되 배치 for 루프를 유지하는 형태로 전환

---

## 2. 변경 구조

### 데이터 흐름

```
[Java: BixolonShipmentActivity / ShipmentActivity]
  searchType=1/3/4/5/7 → for 루프로 15개 필드 packet 조립 ("::" 구분자, "##" 행 구분)
    ↓ HTTP POST (data 파라미터)
[JSP: insert_goods_wet_new.jsp]
  변경 전: INSERT INTO W_GOODS_WET (13 컬럼) + W_GOODS_WET_SEQ.NEXTVAL (Oracle 전용, MSSQL 실행 불가)
  변경 후: INSERT INTO SM_출고계근 (18 컬럼) + NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ (MSSQL 시퀀스)
    ↓ 배치 for 루프 내부에서 N건 INSERT
    ↓ 루프 완료 후 commit 1회
    ↓
[Java]
  응답 "s" 수신 → SAVE_TYPE='F' → 'Y' UPDATE (TB_GOODS_WET)
```

### 변경 전/후 테이블 비교

| 항목 | 변경 전 (Oracle W_GOODS_WET) | 변경 후 (MSSQL SM_출고계근) |
|------|------------------------|---------------------------|
| 테이블명 | `W_GOODS_WET` | `SM_출고계근` |
| PK 시퀀스 | `W_GOODS_WET_SEQ.NEXTVAL` | `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ` |
| 컬럼 수 | 13개 | 18개 |
| 컬럼명 | 영문 (Oracle) | 한글 (MSSQL) |
| 배치 처리 | for 루프 유지 | for 루프 유지 (변경 없음) |
| `##` 구분자 | 유지 | 유지 (변경 없음) |

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **insert_goods_wet_new.jsp** | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\` | INSERT 쿼리 W_GOODS_WET → SM_출고계근, 시퀀스 전환, 18개 필드로 확장, 파라미터 세팅 순서 변경 |

**수정 불필요 파일**:
- `BixolonShipmentActivity.java` / `ShipmentActivity.java` — Java는 이미 15개 필드(splitData[0]~[14])를 packet으로 전송 중, 수정 불요
- `DBHandler.java` — TB_GOODS_WET 로컬 DB 구조 변경 없음
- `Goodswets_Info.java` — 모델 변경 없음

---

## 4. 수정 상세

### 4.1 insert_goods_wet_new.jsp

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_new.jsp`

**변경 전 (L53~67):**

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
PreparedStatement pstmt = conn.prepareStatement(qry);

for (int i = 0; i < splitDataTotal.length; i++) {
    String[] splitData = splitDataTotal[i].split("::");

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

    pstmt.executeUpdate();
    pstmt.clearParameters();
}
conn.commit();
```

**변경 후:**

```jsp
// SM_출고계근 MSSQL 전환: 이마트 insert_goods_wet.jsp(MSSQL 전환 완료)의 INSERT 구조를 재사용하되 배치 for 루프 유지
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

for (int i = 0; i < splitDataTotal.length; i++) {
    String[] splitData = splitDataTotal[i].split("::");

    pstmt.setInt(1, Integer.parseInt(splitData[0]));                         // 출고상세SEQ (GI_D_ID)
    pstmt.setInt(2, Integer.parseInt(splitData[14]));                        // 출고LOTSEQ (GI_L_ID)
    pstmt.setDouble(3, (Double.parseDouble(splitData[1]) * 100) / 100.0);    // 계근중량 (WEIGHT)
    pstmt.setString(4, splitData[2]);                                        // 계근중량단위 (WEIGHT_UNIT)
    pstmt.setString(5, splitData[3]);                                        // ppCode (PACKER_PRODUCT_CODE)
    pstmt.setString(6, splitData[4]);                                        // 계근바코드 (BARCODE)
    pstmt.setString(7, splitData[5]);                                        // 패커코드 (PACKER_CLIENT_CODE)
    pstmt.setString(8, splitData[6]);                                        // 제조일자 (MAKINGDATE)
    pstmt.setString(9, splitData[7]);                                        // 박스시리얼 (BOXSERIAL)
    pstmt.setInt(10, Integer.parseInt(splitData[8]));                        // 계근순번 (BOX_CNT)
    pstmt.setString(11, splitData[9]);                                       // 등록사원 (REG_ID)
    pstmt.setString(12, dateStr);                                            // 등록일자 (서버 자동)
    pstmt.setString(13, timeStr);                                            // 등록시간 (서버 자동)
    pstmt.setString(14, splitData[10]);                                      // 회사코드 (Common.selectCompanyCode)
    pstmt.setString(15, splitData[9]);                                       // 수정사원 (REG_ID와 동일)
    pstmt.setString(16, dateStr);                                            // 수정일자 (서버 자동)
    pstmt.setString(17, timeStr);                                            // 수정시간 (서버 자동)

    pstmt.executeUpdate();
    pstmt.clearParameters();
}
conn.commit();
```

**검증**: 배치 for 루프(`##` 구분자 + `clearParameters()`) 유지, 파라미터 17개(PK 자동 생성 제외) 세팅, 이마트 `insert_goods_wet.jsp` MSSQL 버전의 INSERT 구조와 한 글자 단위로 동일

---

## 5. 사이드이펙트

### BixolonShipmentActivity.java / ShipmentActivity.java (수정 없음, 영향 분석)

두 Activity는 이미 15개 필드를 packet으로 조립 중 (소스분석47 4.2장 참조):

```java
packet += GI_D_ID + "::";                  // splitData[0]
packet += WEIGHT + "::";                   // splitData[1]
packet += WEIGHT_UNIT + "::";              // splitData[2]
packet += PACKER_PRODUCT_CODE + "::";      // splitData[3]
packet += BARCODE + "::";                  // splitData[4]
packet += PACKER_CLIENT_CODE + "::";       // splitData[5]
packet += MAKINGDATE + "::";               // splitData[6]
packet += BOXSERIAL + "::";                // splitData[7]
packet += BOX_CNT + "::";                  // splitData[8]
packet += REG_ID + "::";                   // splitData[9]
packet += Common.selectCompanyCode + "::"; // splitData[10] ← 회사코드 (이미 전송 중)
packet += BRAND_CODE + "::";               // splitData[11]
packet += CLIENT_TYPE + "::";              // splitData[12]
packet += BOX_ORDER + "::" + GI_L_ID + "##"; // splitData[13], [14] ← GI_L_ID (이미 전송 중)
```

**핵심 사실**: Java는 이미 전환 후 JSP가 요구하는 15개 필드를 **정확히 전송하고 있음**. 즉 Java 측 수정 불필요.

### insert_goods_wet_new.jsp 호출 분기

- `BixolonShipmentActivity.java:2711` — searchType 1, 4, 5, 7
- `BixolonShipmentActivity.java:2715` — searchType 3 (도매)
- `ShipmentActivity.java:3787/3791` — 동일 분기

→ **5개 searchType 공용** 사용 중, 본 JSP 전환은 5개 searchType 모두에 영향

---

## 6. 데이터 저장 구조

### 컬럼 매핑: Oracle W_GOODS_WET → MSSQL SM_출고계근

| # | MSSQL 컬럼 (SM_출고계근) | 타입 | 파라미터 인덱스 | Java splitData[] | Oracle W_GOODS_WET 대응 | 비고 |
|:-:|:--------------------:|:----:|:-----------:|:-------------:|:----------------------:|------|
| PK | SEQ | INT | 자동 (`NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ`) | - | `GOODS_WET_ID` | 서버 자동 |
| 1 | 출고상세SEQ | INT | setInt(1) | splitData[0] | `GI_D_ID` | 동일 |
| 2 | 출고LOTSEQ | INT | setInt(2) | splitData[14] | (없음) | **MSSQL 추가** |
| 3 | 계근중량 | DOUBLE | setDouble(3) | splitData[1] | `WEIGHT` | `(Double * 100) / 100.0` |
| 4 | 계근중량단위 | STRING | setString(4) | splitData[2] | `WEIGHT_UNIT` | 동일 |
| 5 | ppCode | STRING | setString(5) | splitData[3] | `PACKER_PRODUCT_CODE` | 컬럼명 변경 |
| 6 | 계근바코드 | STRING | setString(6) | splitData[4] | `BARCODE` | 동일 의미 |
| 7 | 패커코드 | STRING | setString(7) | splitData[5] | `PACKER_CLIENT_CODE` | 컬럼명 변경 |
| 8 | 제조일자 | STRING | setString(8) | splitData[6] | `MAKINGDATE` | 동일 의미 |
| 9 | 박스시리얼 | STRING | setString(9) | splitData[7] | `BOXSERIAL` | 동일 의미 |
| 10 | 계근순번 | INT | setInt(10) | splitData[8] | `BOX_CNT` | 컬럼명 변경 |
| 11 | 등록사원 | STRING | setString(11) | splitData[9] | `REG_ID` | 동일 의미 |
| 12 | 등록일자 | STRING | setString(12) | `dateStr` | `REG_DATE` | 서버 자동 생성 |
| 13 | 등록시간 | STRING | setString(13) | `timeStr` | `REG_TIME` | 서버 자동 생성 |
| 14 | 회사코드 | VARCHAR(2) | setString(14) | splitData[10] | (없음) | **MSSQL 추가**, `Common.selectCompanyCode` 값 |
| 15 | 수정사원 | STRING | setString(15) | splitData[9] | (없음) | **MSSQL 추가**, REG_ID와 동일값 |
| 16 | 수정일자 | STRING | setString(16) | `dateStr` | (없음) | **MSSQL 추가**, 서버 자동 |
| 17 | 수정시간 | STRING | setString(17) | `timeStr` | (없음) | **MSSQL 추가**, 서버 자동 |

### MSSQL에 추가되는 5개 컬럼

| 컬럼 | 값 출처 | 이유 |
|------|--------|------|
| 출고LOTSEQ | splitData[14] (GI_L_ID) | SM_출고계근 스키마 요구, Java 이미 전송 중 |
| 회사코드 | splitData[10] (Common.selectCompanyCode) | 멀티회사 환경 필수, VARCHAR(2) 제약, Java 이미 전송 중 (개발32 선행 이슈 해결) |
| 수정사원 | splitData[9] (REG_ID와 동일) | SM_출고계근 audit 컬럼, 등록사원과 동일값 |
| 수정일자 | `dateStr` (서버 자동) | SM_출고계근 audit 컬럼 |
| 수정시간 | `timeStr` (서버 자동) | SM_출고계근 audit 컬럼 |

### Java 미사용 splitData (유지 예정)

Java는 15개를 전송하지만 JSP는 11개(`[0]~[10]` + `[14]`)만 사용:

| 미사용 splitData | 의미 | 전환 후 처리 |
|:-------------:|------|------------|
| splitData[11] | BRAND_CODE | 변경 없음 (원본 JSP도 미사용) |
| splitData[12] | CLIENT_TYPE | 변경 없음 (원본 JSP도 미사용) |
| splitData[13] | BOX_ORDER | 변경 없음 (원본 JSP도 미사용) |

→ Java 전송 구조는 수정하지 않으므로 이 3개 필드는 packet에 포함되어 전송되지만 JSP에서 무시됨 (원본 동작 유지)

### 인덱스 매핑 (이마트 insert_goods_wet.jsp와 대조)

- 이마트 JSP: 단건 처리, 파라미터 세팅 L72~88 (17회)
- 본 JSP(전환 후): **for 루프 내** 파라미터 세팅, 각 반복마다 17회 + `clearParameters()` (배치)

→ 파라미터 개수·순서·타입은 이마트와 완전 동일, 루프 구조만 차이

---

## 7. 호출 시점

```
[PDA 앱 계근 완료 → 전송 버튼 클릭]
    ↓
ProgressDlgShipmentSend.doInBackground() 시작
    ↓
if (searchType == 1/3/4/5/7) {   ← 5개 searchType 공용 분기 (BixolonShipmentActivity.java:2663)
    ↓
    for (list_send_info) {
        if (SAVE_TYPE == "F") {
            packet += ...15개 필드... + "##";  ← 배치 packet 조립 (2668~2694)
        }
    }
    ↓
    HTTP POST URL_INSERT_GOODS_WET_NEW  ← 1회 호출 (2711 or 2715)
        ↓
    insert_goods_wet_new.jsp 실행
        ├─ data.split("##") → splitDataTotal[0]~[N-1]
        ├─ for (N회) {
        │     split("::") → splitData[0]~[14]
        │     INSERT INTO SM_출고계근 (17 파라미터)
        │     executeUpdate
        │     clearParameters
        │ }
        └─ commit (루프 완료 후 1회)
    ↓
    응답 "s" 수신
        ↓
    DBHandler.updatequeryGoodsWet → SAVE_TYPE 'F' → 'Y' UPDATE
}
    ↓
AsyncTask 완료 → ProgressDialog 종료
```

---

## 8. 개발 플랜

### Step 1: Oracle W_GOODS_WET → MSSQL SM_출고계근 INSERT 쿼리 전환

**Part 1. 분석**

- 메서드: JSP qry 문자열 구성 (L53~68)
- 범위: `insert_goods_wet_new.jsp` 전체 INSERT 블록
- 용도: W_GOODS_WET 기반 13개 필드 INSERT를 SM_출고계근 기반 18개 필드 INSERT로 전환
- 주의할 점:
  - 배치 for 루프 (L71~102) 및 `clearParameters()` 호출 유지 필수
  - 파라미터 개수 13 → 17로 증가 (PK 자동 제외)
  - splitData[10] (회사코드), splitData[14] (GI_L_ID) 추가 활용
  - 수정사원/일자/시간 컬럼 추가 (수정사원 = 등록사원 동일값)
  - 이마트 `insert_goods_wet.jsp` MSSQL 버전의 INSERT 구조 재사용

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 테이블명 | L53 | `W_GOODS_WET` → `SM_출고계근` |
| 2 | PK 컬럼명 | L53 | `GOODS_WET_ID` → `SEQ` |
| 3 | 시퀀스 | L67 | `W_GOODS_WET_SEQ.NEXTVAL` → `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ` |
| 4 | 컬럼명 한글 전환 | L54~65 | GI_D_ID→출고상세SEQ, WEIGHT→계근중량, WEIGHT_UNIT→계근중량단위, PACKER_PRODUCT_CODE→ppCode, BARCODE→계근바코드, PACKER_CLIENT_CODE→패커코드, MAKINGDATE→제조일자, BOXSERIAL→박스시리얼, BOX_CNT→계근순번, REG_ID→등록사원, REG_DATE→등록일자, REG_TIME→등록시간 |
| 5 | 출고LOTSEQ 컬럼 추가 | L54 이후 | `출고LOTSEQ` 추가 (MSSQL만 존재) |
| 6 | 회사코드 컬럼 추가 | L65 이후 | `회사코드` 추가 |
| 7 | 수정사원/일자/시간 컬럼 추가 | L65 이후 | `수정사원`, `수정일자`, `수정시간` 추가 |
| 8 | VALUES 자리 표시자 추가 | L67 | `?` 12개 → 17개 |
| 9 | 파라미터 세팅 추가 | L77~88 | `setInt(2, splitData[14])`, `setString(14, splitData[10])`, `setString(15, splitData[9])`, `setString(16, dateStr)`, `setString(17, timeStr)` |
| 10 | 파라미터 인덱스 재배열 | L77~88 | 기존 1~12 → 새 순서에 맞춰 1~17 |

**Part 2. 변환 계획**

- 변환 방식: 이마트 `insert_goods_wet.jsp`(MSSQL 전환 완료)의 INSERT 쿼리와 파라미터 세팅 블록을 **그대로 재사용**, 배치 for 루프 구조만 유지
- 배치 루프 내 파라미터 세팅은 건별로 `setXxx` → `executeUpdate` → `clearParameters` 순서 유지
- commit은 루프 완료 후 1회 (기존 동일)
- Java 측 packet 조립 구조는 변경 불필요 (이미 15개 필드 전송 중)
- 주의사항: 파라미터 개수 불일치 시 SQLException 발생하므로 `VALUES (...)`의 `?` 개수와 `setXxx` 호출 개수(17개) 정확히 일치해야 함

**체크리스트**
- [x] Part 1: Oracle vs MSSQL 컬럼·시퀀스 매핑 분석 완료 (소스분석47 + 사전 시뮬레이션)
- [x] Part 2: 이마트 JSP 구조 재사용 방식 확인 (이마트 insert_goods_wet.jsp L49~88과 동일 쿼리)
- [x] Part 3: JSP INSERT 쿼리 전환 + 파라미터 세팅 17개 구현 (2026-04-22)
- [ ] Part 4: JSP Tomcat 재시작 후 문법 오류 없음 확인 (Step 3 이관 — 실기기 테스트 시 수행)
- [ ] Part 5: 단위테스트 (MSSQL에서 INSERT 직접 실행) (Step 3 이관 — 실기기 테스트 시 수행)
- [x] Part 6: 회귀테스트 없음 (JSP 단독, Java 영향 없음) — Step 3에서 5개 searchType + 이마트 회귀

**Part 6. 변경 내용** (완료):
- **무엇을**: `insert_goods_wet_new.jsp` INSERT 쿼리를 Oracle `W_GOODS_WET` 기반(13개 필드, `W_GOODS_WET_SEQ.NEXTVAL`) → MSSQL `SM_출고계근` 기반(18개 필드, `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ`)로 전환. 배치 for 루프 + `##` 구분자 구조 그대로 유지. Java 측 수정 없음.
- **왜**: DB 접속은 이미 MSSQL로 전환됐으나 INSERT 쿼리가 Oracle 전용 W_GOODS_WET 테이블과 Oracle 시퀀스 문법을 사용하여 실행 시 오류 발생 상태. MSSQL SM_출고계근 테이블의 한글 컬럼명에 맞춰 매핑하고 NOT NULL 제약을 만족시키기 위해 회사코드·출고LOTSEQ·수정사원/일자/시간 필드 추가 필요. Java는 이미 15개 필드(splitData[0]~[14])를 packet으로 전송 중이므로 JSP 측만 수정하면 됨. 이마트 `insert_goods_wet.jsp`가 이미 동일 패턴으로 MSSQL 전환을 완료한 선례가 있어 쿼리 구조를 그대로 재사용.
- **어떻게**:
  1. 테이블명: `W_GOODS_WET` → `SM_출고계근`
  2. 시퀀스: `W_GOODS_WET_SEQ.NEXTVAL` → `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ`
  3. PK 컬럼명: `GOODS_WET_ID` → `SEQ`
  4. 컬럼명 한글 전환 11건 (GI_D_ID→출고상세SEQ, WEIGHT→계근중량, WEIGHT_UNIT→계근중량단위, PACKER_PRODUCT_CODE→ppCode, BARCODE→계근바코드, PACKER_CLIENT_CODE→패커코드, MAKINGDATE→제조일자, BOXSERIAL→박스시리얼, BOX_CNT→계근순번, REG_ID→등록사원, REG_DATE→등록일자, REG_TIME→등록시간)
  5. MSSQL 필수 컬럼 5개 추가: 출고LOTSEQ(splitData[14])·회사코드(splitData[10])·수정사원(splitData[9] 동일값)·수정일자(dateStr)·수정시간(timeStr)
  6. VALUES `?` 개수 12 → 17로 증가
  7. for 루프 내 파라미터 세팅 12회 → 17회로 확장 (splitData 매핑 순서: 0, 14, 1, 2, 3, 4, 5, 6, 7, 8, 9, dateStr, timeStr, 10, 9, dateStr, timeStr)
  8. 배치 for 루프 + `##` 구분자 + `clearParameters()` + commit 루프 외부 1회 구조 모두 유지
  9. out.println / catch / rollback 블록 수정 없음
- **검증**: ⑤ code-verifier(7항목 PASS) + ⑥ original-comparator(14항목 전체 원본 동일/허용차이) 모두 COMMIT OK 판정

---

### Step 2: 배치 처리 루프 + `##` 행 구분자 유지 검증

**Part 1. 분석**

- 메서드: JSP try 블록 내 배치 처리 (L33~119)
- 범위: `data.split("##")`, for 루프, `clearParameters()`, commit 순서
- 용도: Step 1에서 INSERT 쿼리를 변경한 후에도 배치 처리 구조가 원본과 동일한지 정적 검증
- 주의할 점:
  - `data.split("##")` 유지 (L36)
  - for 루프 `splitDataTotal.length` 반복 조건 유지 (L71)
  - 루프 내 `splitData = splitDataTotal[i].split("::")` 유지 (L73)
  - 루프 내 파라미터 세팅 후 `pstmt.executeUpdate()` + `pstmt.clearParameters()` 유지
  - `conn.commit()`은 루프 **외부**, 루프 완료 후 1회만 실행 유지

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | data split | L36 | `splitDataTotal = data.split("##")` 유지 |
| 2 | for 루프 | L71 | `for (int i = 0; i < splitDataTotal.length; i++)` 유지 |
| 3 | 행 split | L73 | `splitData = splitDataTotal[i].split("::")` 유지 |
| 4 | executeUpdate 위치 | 루프 내 | 각 행마다 1회 실행 유지 |
| 5 | clearParameters 위치 | 루프 내 | 각 실행 후 호출 유지 |
| 6 | commit 위치 | 루프 외 | 배치 완료 후 1회만 실행 유지 |
| 7 | Java packet 구조 | (수정 없음) | `::` + `##` 조립 유지 확인 |

**Part 2. 변환 계획**

- 변환 방식: 구조 변경 없음, 정적 검증만 수행
- 주의사항: Step 1에서 파라미터 개수·순서만 변경되므로 루프 구조에는 영향 없음

**체크리스트**
- [x] Part 1: 배치 for 루프 구조 유지 확인 (수정 후 JSP L75: `for (int i = 0; i < splitDataTotal.length; i++)` 그대로)
- [x] Part 2: `##` 구분자 처리 유지 확인 (L35: `data.split("##")` 그대로, L77: 루프 내 `splitDataTotal[i].split("::")` 유지)
- [x] Part 3: `clearParameters()` 호출 순서 유지 확인 (L102: `pstmt.executeUpdate()` 직후 `pstmt.clearParameters()` 호출)
- [x] Part 4: commit 1회 (루프 외) 유지 확인 (L113: 루프 L111 종료 후 `conn.commit()` 1회만 실행)
- [x] Part 5: Java packet 조립 `::` + `##` 구조 확인 (BixolonShipmentActivity.java L2668~2694에서 15개 필드 조립, L2684에서 `+ "##"` 행 종결자 확인)
- [x] Part 6: 회귀테스트 없음 (JSP 단독) — Step 3에서 5개 searchType + 이마트 회귀 테스트로 수행

**Part 6. 변경 내용** (완료):
- **무엇을**: Step 1에서 INSERT 쿼리를 SM_출고계근 기반 18컬럼/17파라미터로 변경한 후에도 `insert_goods_wet_new.jsp`의 배치 처리 구조(`##` 구분자, for 루프, `clearParameters()`, commit 루프 외 1회, catch rollback)가 원본과 동일하게 유지되는지 정적 구조 검증.
- **왜**: Step 1의 쿼리 변경이 배치 처리 흐름에 영향을 주지 않았음을 공식 확인해야 함. 5개 searchType(1/3/4/5/7)이 공용으로 사용하는 JSP이므로 배치 구조 손상 시 모든 searchType에서 장애 발생 가능. Java 측 packet 조립도 수정 없음 검증 필요.
- **어떻게**:
  1. 수정 후 JSP L33~127 전체를 재판독하여 배치 구조 7개 체크포인트 확인
     - `data.split("##")` — L35 유지
     - `for (int i = 0; i < splitDataTotal.length; i++)` — L75 유지
     - `splitDataTotal[i].split("::")` — L77 유지
     - `pstmt.executeUpdate()` 루프 내 — L101 유지
     - `pstmt.clearParameters()` 루프 내 — L102 유지
     - `conn.commit()` 루프 외 1회 — L113 유지
     - `conn.rollback()` catch 블록 — L125 유지
  2. Java 측 `BixolonShipmentActivity.java` L2668~2694 재판독
     - for 루프로 15개 필드 packet 조립 유지
     - `::` 컬럼 구분자 + `##` 행 종결자 조합 유지
     - Java 파일 git status로 수정 없음 확인
  3. 구조 변경 없음 확인 → 본 단계는 순수 정적 검증, 실제 코드 수정 0건
- **검증**: Step 1 수행 시 ⑤ code-verifier와 ⑥ original-comparator가 이미 배치 구조 유지를 교차 검증했으며, 본 Step 2는 이를 명시적으로 문서화. 컴파일/단위테스트/회귀테스트는 Step 3 통합 테스트로 이관

---

### Step 3: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 비정량(searchType=4) 계근 후 전송 시 `insert_goods_wet_new.jsp` 호출 확인 | □ |
| 2 | Tomcat 로그에 INSERT 쿼리 출력 확인 (`##insert_goods_wet query start, query :`) | □ |
| 3 | 5건 이상 배치 계근 후 전송 시 for 루프 N회 실행 확인 (로그 반복 출력) | □ |
| 4 | 응답 "s" 수신 시 TB_GOODS_WET SAVE_TYPE='F' → 'Y' UPDATE 확인 | □ |
| 5 | SM_출고계근 테이블에 정상 INSERT 확인 (SELECT로 데이터 존재 확인) | □ |
| 6 | `SEQ` 컬럼이 `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ` 값으로 정상 채번 확인 | □ |
| 7 | `출고상세SEQ` = splitData[0] (GI_D_ID) 값 일치 확인 | □ |
| 8 | `출고LOTSEQ` = splitData[14] (GI_L_ID) 값 일치 확인 | □ |
| 9 | `회사코드` = splitData[10] (Common.selectCompanyCode = "20") 값 일치 확인 | □ |
| 10 | `수정사원` = splitData[9] (REG_ID와 동일값) 확인 | □ |
| 11 | `등록일자`/`등록시간`/`수정일자`/`수정시간` 서버 자동 생성 확인 | □ |
| 12 | 배치 중 1건 실패 시 전체 rollback 동작 확인 (의도적 오류 데이터 전송) | □ |
| 13 | 생산(searchType=1) 계근 후 전송 회귀 테스트 | □ |
| 14 | 도매(searchType=3) 계근 후 전송 회귀 테스트 | □ |
| 15 | 홈플러스비정량(searchType=5) 계근 후 전송 회귀 테스트 | □ |
| 16 | 생산라벨(searchType=7) 계근 후 전송 회귀 테스트 | □ |
| 17 | 이마트(searchType=0) 계근 후 전송 회귀 테스트 (본 작업 무영향 확인) | □ |
| 18 | 비정량 E2E 통합 테스트 (출하조회→바코드조회→계근→전송→ERP 확인) | □ |

---

### 개발 순서 요약

```
Step 1: W_GOODS_WET → SM_출고계근 INSERT 쿼리 전환 (JSP 수정)
    ↓
Step 2: 배치 처리 루프 + ## 구분자 유지 검증
    ↓
Step 3: 통합 테스트 (5개 searchType 회귀 + 이마트 회귀)
```

---

## 9. 테스트 시나리오

### 시나리오 1: 비정량 5건 배치 전송

```
1. MainActivity → searchType=4 (비정량) 선택
2. 출하대상 조회 → 바코드정보 조회 자동 실행 (개발36 완료)
3. 5개 상품 연속 계근 (TB_GOODS_WET에 SAVE_TYPE='F' 5건 저장)
4. 전송 버튼 클릭
5. BixolonShipmentActivity: packet 조립 ("건1##건2##건3##건4##건5")
6. HTTP POST → insert_goods_wet_new.jsp 호출 (1회)
7. JSP 내부: split("##") → 5행, for 루프 5회 실행
8. SM_출고계근에 5건 INSERT (SEQ 5개 자동 채번)
9. commit 1회
10. 응답 "s"
11. Java: TB_GOODS_WET 5건 SAVE_TYPE 'F' → 'Y' UPDATE
```

### 시나리오 2: 회귀 — 이마트 단건 전송

```
1. MainActivity → searchType=0 (이마트) 선택
2. 바코드 스캔 → 1건 계근 → 전송 버튼
3. BixolonShipmentActivity: 단건 packet 조립 (## 없음)
4. HTTP POST → insert_goods_wet.jsp (변경 없음) 호출
5. SM_출고계근 INSERT 1건
6. 본 작업(개발37)이 이마트 동작에 영향 없음 확인
```

### 시나리오 3: 배치 실패 시 롤백

```
1. 5건 배치 전송 중 3번째 건에서 제약 조건 위반 (예: 필수 컬럼 NULL)
2. JSP catch 블록 진입 → conn.rollback()
3. SM_출고계근에 1~5건 모두 롤백 (0건)
4. 응답 "f"
5. Java: SAVE_TYPE 'F' 유지 (업데이트 안 됨) → 재시도 대상 유지
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 테이블 없음 오류: `W_GOODS_WET` | MSSQL에 해당 테이블 없음 | Step 1에서 `SM_출고계근`으로 전환 |
| 2 | 시퀀스 문법 오류: `W_GOODS_WET_SEQ.NEXTVAL` | MSSQL에서 Oracle 시퀀스 문법 미지원 | `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ` 사용 |
| 3 | 회사코드 NOT NULL 제약 위반 | 원본 13개 필드에는 회사코드 없음 | splitData[10] 값 INSERT (Java 이미 전송 중) |
| 4 | 회사코드 VARCHAR(2) 길이 초과 | ITEM_CODE(10자리)를 회사코드에 넣으면 잘림 | 개발32 선행 수정 이미 완료: splitData[10]은 `Common.selectCompanyCode`("20") |
| 5 | 출고LOTSEQ NOT NULL 제약 위반 | 원본 13개 필드에는 GI_L_ID 없음 | splitData[14] 값 INSERT (Java 이미 전송 중) |
| 6 | 파라미터 개수 불일치 (SQLException) | `setXxx` 호출과 `?` 개수 불일치 | Step 1에서 정확히 17개 세팅 (PK 자동) |
| 7 | 배치 중간 실패 시 전체 롤백 | `conn.commit()`이 루프 외부에 있음 | 원본 동작 유지 (재시도 기반 복구) |
| 8 | `SM_DLIVY_WEIGH_SEQ` 시퀀스 미존재 | 시퀀스가 MSSQL에 없을 경우 | 이마트 `insert_goods_wet.jsp`가 이미 사용 중이므로 존재 확정 |

---

## 11. 사전 시뮬레이션 결과 (⑤ code-verifier)

**실행 일자**: 2026-04-22
**검증 단계**: 코드 수정 **전** 사전 시뮬레이션 (실제 JSP 파일 미수정)
**최종 판정**: ✅ **GO** (착수 가능)

### 11.1 검증 항목별 결과

| # | 검증 항목 | 결과 | 핵심 발견 |
|:-:|----------|:----:|----------|
| 1 | SM_출고계근 스키마 18컬럼 대조 | ✅ PASS | ERP `DlivyWeighEntity` + `BaseEntity`에 18개 컬럼 전부 존재, 모두 NOT NULL |
| 2 | 이마트 JSP와 쿼리 동일성 | ✅ PASS | 테이블명·컬럼명·순서·시퀀스·`?` 개수 완전 일치 |
| 3 | Java packet vs JSP splitData 파싱 | ✅ PASS | 15개 필드 중 11개(0~10, 14) 사용, 3개(11, 12, 13) 원본처럼 무시 |
| 4 | 파라미터 개수 정합성 | ✅ PASS | `?` 17개 = setXxx 17회 |
| 5 | 배치 처리 구조 유지 | ✅ PASS | `##` split, for 루프, clearParameters, commit 위치 모두 유지 |
| 6 | 회사코드 VARCHAR(2) 제약 | ✅ PASS | 개발32 완료로 `selectCompanyCode`("20", 2자리) 사용 확정 |
| 7 | 5개 searchType 공용 호출 | ✅ PASS | searchType 1/3/4/5/7 모두 URL_INSERT_GOODS_WET_NEW로 라우팅 확인 |
| 8 | SM_DLIVY_WEIGH_SEQ 시퀀스 존재 | ✅ PASS | ERP Entity에서 시퀀스 정의 직접 확인 + 이마트 JSP 운영 중 |
| 9 | 전체 흐름 시뮬레이션 (7 Phase) | ✅ PASS | 계근 → packet 조립 → HTTP → split → 배치 INSERT → commit → UPDATE 전 단계 정상 |
| 10 | 예상 문제점 8건 재평가 | 6건 해결 / 2건 실기기 대기 | Problem 7(배치 rollback)은 실기기 검증 필요 |

### 11.2 검증 중 도출된 주의사항

| # | 항목 | 내용 |
|:-:|------|------|
| 1 | `ppCode` 대소문자 | ERP Entity는 `PPCODE` 정의, 쿼리는 `ppCode`. MSSQL 기본 대소문자 비구분 + 이마트 JSP가 동일하게 `ppCode`로 운영 중이라 문제없음 |
| 2 | `ShipmentActivity.java`는 범위 외 | 개발32에서 "미사용 파일"로 명시됨. BixolonShipmentActivity만 실행되므로 영향 없음 |
| 3 | 예상 문제점 #7 (배치 rollback) | 실기기 테스트에서 의도적 오류 주입(데이터 제약 위반 등)으로 검증 필요. 원본부터 의도된 트랜잭션 동작이므로 버그 아님 |

### 11.3 10장 예상 문제점 재평가 결과

| 예상 문제점 # | 시뮬레이션 결과 | 비고 |
|:-:|:-:|------|
| 1. W_GOODS_WET 테이블 없음 | ✅ 해결 확정 | Step 1 적용 시 `SM_출고계근`으로 전환 |
| 2. Oracle 시퀀스 문법 | ✅ 해결 확정 | `NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ` 사용 |
| 3. 회사코드 NOT NULL | ✅ 해결 확정 | splitData[10] = selectCompanyCode 사용 |
| 4. 회사코드 VARCHAR(2) 길이 | ✅ 해결 확정 | 개발32 선행 수정 완료 |
| 5. 출고LOTSEQ NOT NULL | ✅ 해결 확정 | splitData[14] = GI_L_ID 사용 |
| 6. 파라미터 개수 불일치 | ✅ 해결 확정 | 17개 정합성 검증됨 |
| 7. 배치 중간 실패 전체 rollback | ⚠️ 실기기 테스트 대기 | 원본 동작 유지, 데이터 제약 위반 시나리오로 검증 예정 |
| 8. SM_DLIVY_WEIGH_SEQ 미존재 | ✅ 해결 확정 | 이마트 JSP 운영 중 + Entity 정의 확인 |

### 11.4 결론

- 코드 정합성 레벨(스키마·파싱·쿼리 문법·파라미터 매핑)은 **전 항목 통과**
- 트랜잭션 동작(rollback)은 실기기/실데이터 의존 → Step 3 통합 테스트에서 검증
- **Step 1 착수 가능**. 수정 범위는 JSP L53~88 (INSERT 쿼리 블록 + 파라미터 세팅 블록)으로 한정

---

## 12. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 사전 시뮬레이션 | ⑤ code-verifier 정합성 검증 | ✅ PASS (2026-04-22) |
| 1 | W_GOODS_WET → SM_출고계근 INSERT 쿼리 전환 | ✅ 완료 (2026-04-22, ⑤⑥ 사후 검증 PASS) |
| 2 | 배치 처리 루프 + `##` 구분자 유지 검증 | ✅ 완료 (2026-04-22, 정적 구조 검증 7항목 PASS, 컴파일/테스트는 Step 3 이관) |
| 3 | 통합 테스트 (5개 searchType 회귀 + 이마트 회귀) | ⏳ 대기 |

---

## 관련 문서

- `app/doc/소스분석/47_이마트계근전송_vs_공용배치계근전송_JSP_원본비교분석.md` — 이마트 단건 vs 공용 배치 원본 Oracle 기준 1:1 비교 (사전자료)
- `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md` — 비정량 전체 흐름 (3단계 전송 부분)
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` — 이마트 전체 흐름 (참고, insert_goods_wet.jsp 포함)
- `app/doc/소스분석/37_로컬DB_MSSQL전환_현황.md` — TB_GOODS_WET ↔ SM_출고계근 매핑
- `app/doc/개발/32_회사코드_packet수정[SM출고계근_회사코드_ITEM_CODE_잘림오류].md` — 회사코드 packet 선행 수정
- `app/doc/오류/19_SM출고계근_회사코드_ITEM_CODE_잘림오류.md` — 회사코드 관련 선행 오류
- `app/doc/개발/31_GI_L_ID_추가[GI_D_ID_비유니크_전체_영향범위].md` — GI_L_ID 선행 수정
- `app/doc/개발/35_비정량_출하계근대상_JSP_MSSQL전환.md` — 비정량 1번 JSP 전환 (참고)
- `app/doc/개발/36_비정량_바코드정보조회_JSP_MSSQL전환.md` — 비정량 2번 JSP 전환 (동일 패턴 참고)
- `app/doc/view/W_GOODS_WET.md` — W_GOODS_WET 테이블 분석 (원본)
- JSP 이마트 참고 (MSSQL 전환 완료): `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet.jsp`
- JSP 전환 대상 (현재): `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_new.jsp`
- Java 호출: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java:2663~2716`

---

**문서 버전**: 1.0
