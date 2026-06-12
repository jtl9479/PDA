# TB_BARCODE_INFO INSERT REG_DATE/REG_TIME/MEMO 누락

## 발견일
2026-05-06

## 에러 발생 시나리오

```
1. 앱에서 바코드 정보 조회 요청 (searchType 무관, 모든 타입)
2. search_barcode_info.jsp → temp[21]=REG_DATE, temp[22]=REG_TIME, temp[23]=MEMO 전송
3. ProgressDlgBarcodeSearch.java에서 bi.setREG_DATE/setREG_TIME/setMEMO 호출
4. DBHandler.insertqueryBarcodeInfo() 실행 → INSERT SQL에 3개 컬럼 없음
5. 결과: REG_DATE, REG_TIME, MEMO가 TB_BARCODE_INFO에 NULL로 저장됨
```

---

## 현상
- TB_BARCODE_INFO 테이블 CREATE 시 REG_DATE, REG_TIME, MEMO 컬럼이 선언되어 있음
- JSP(search_barcode_info.jsp)에서 해당 컬럼 값을 temp[21], temp[22], temp[23]으로 전송
- ProgressDlgBarcodeSearch.java에서 bi.setREG_DATE/setREG_TIME/setMEMO 호출까지 정상 수행
- **DBHandler.insertqueryBarcodeInfo()의 INSERT SQL 컬럼 목록에서 3개 컬럼이 빠져 있어 값이 저장되지 않음**
- NOT NULL 제약이 없어 SQL 오류는 발생하지 않고 조용히 NULL 저장됨 (무증상 데이터 손실)

## 원래부터 있던 버그인가?

**YES - 원본(PDA-INNO(원본)) DBHandler.java에서도 동일하게 누락되어 있음**

원본 INSERT 컬럼 목록 (L1099~1121):
```java
// D:\PDA\PDA-INNO(원본) DBHandler.java:1119~1121
+ DBInfo.STATUS + ", "
+ DBInfo.REG_ID + ", "
+ DBInfo.SHELF_LIFE      // ← REG_DATE, REG_TIME, MEMO 없음
+ ") VALUES('"
```

현재(INNO) INSERT 컬럼 목록 (L1087~1089):
```java
// DBHandler.java:1087~1089
+ DBInfo.STATUS + ", "
+ DBInfo.REG_ID + ", "
+ DBInfo.SHELF_LIFE      // ← REG_DATE, REG_TIME, MEMO 없음
+ ") VALUES('"
```

원본과 현재 모두 동일하게 누락. CREATE TABLE에는 포함되어 있으나 INSERT에서만 빠진 상태.

## 원인

### 문제 1 (주요): insertqueryBarcodeInfo() INSERT SQL 컬럼 누락

#### 코드 위치
- `DBHandler.java` : 1066~1112줄 (insertqueryBarcodeInfo)

#### 현재 문제 코드
```java
// DBHandler.java:1066~1112
String sqlStr = "INSERT INTO "
        + DBInfo.TABLE_NAME_BARCODE_INFO + " ("
        + DBInfo.PACKER_CLIENT_CODE + ", "
        + DBInfo.BRAND_CODE + ", "
        + DBInfo.PACKER_PRODUCT_CODE + ", "
        + DBInfo.PACKER_PRD_NAME + ", "
        + DBInfo.ITEM_CODE + ", "
        + DBInfo.ITEM_NAME_KR + ", "
        + DBInfo.BARCODEGOODS + ", "
        + DBInfo.BASEUNIT + ", "
        + DBInfo.ZEROPOINT + ", "
        + DBInfo.PACKER_PRD_CODE_FROM + ", "
        + DBInfo.PACKER_PRD_CODE_TO + ", "
        + DBInfo.BARCODEGOODS_FROM + ", "
        + DBInfo.BARCODEGOODS_TO + ", "
        + DBInfo.WEIGHT_FROM + ", "
        + DBInfo.WEIGHT_TO + ", "
        + DBInfo.MAKINGDATE_FROM + ", "
        + DBInfo.MAKINGDATE_TO + ", "
        + DBInfo.BOXSERIAL_FROM + ", "
        + DBInfo.BOXSERIAL_TO + ", "
        + DBInfo.STATUS + ", "
        + DBInfo.REG_ID + ", "
        // ★ REG_DATE 누락
        // ★ REG_TIME 누락
        // ★ MEMO 누락
        + DBInfo.SHELF_LIFE
        + ") VALUES('"
        ...
        + Common.nullCheck(bi.getREG_ID(), "") + "','"
        // ★ bi.getREG_DATE() 미포함
        // ★ bi.getREG_TIME() 미포함
        // ★ bi.getMEMO() 미포함
        + Common.nullCheck(bi.getSHELF_LIFE(), "") + "')";
```

#### 발생 시나리오
CREATE TABLE에는 25개 컬럼이 선언되어 있으나 INSERT SQL에는 REG_DATE/REG_TIME/MEMO 3개가 빠진 22개만 포함. NOT NULL 제약이 없으므로 SQLite에서 예외 없이 NULL로 저장됨.

### 문제 2 (보조): CREATE TABLE과 INSERT SQL 컬럼 수 불일치

#### 코드 위치
- `DBHandler.java` : 841~873줄 (createqueryBarcodeInfo)

#### 문제 코드
```java
// DBHandler.java:867~872 - CREATE TABLE에는 존재
+ DBInfo.STATUS + " TEXT , "
+ DBInfo.REG_ID + " TEXT NOT NULL, "
+ DBInfo.REG_DATE + " TEXT , "    // ★ CREATE에는 있음
+ DBInfo.REG_TIME + " TEXT , "    // ★ CREATE에는 있음
+ DBInfo.MEMO + " TEXT , "        // ★ CREATE에는 있음
+ DBInfo.SHELF_LIFE + " TEXT"
```

CREATE TABLE: 25개 데이터 컬럼 (AUTOINCREMENT PK 제외)
INSERT SQL: 22개 컬럼 → 3개 불일치

## 상세 흐름

1. **JSP 조회** (search_barcode_info.jsp)
   - SELECT 결과 25번째 컬럼까지 out.println으로 출력
   - temp[21] = REG_DATE(빈 문자열 `''`), temp[22] = REG_TIME(빈 문자열 `''`), temp[23] = MEMO(빈 문자열 `''`)
   - temp[24] = SHELF_LIFE

2. **파싱** (ProgressDlgBarcodeSearch.java:130~134)
   - `bi.setREG_DATE(temp[21].toString())` → Barcodes_Info 객체에 정상 저장
   - `bi.setREG_TIME(temp[22].toString())` → 정상
   - `bi.setMEMO(temp[23].toString())` → 정상
   - `bi.setSHELF_LIFE(temp[24].toString())` → 정상

3. **DB INSERT** (DBHandler.insertqueryBarcodeInfo:1066~1112)
   - INSERT SQL 컬럼 목록: 22개 (REG_DATE, REG_TIME, MEMO 미포함)
   - VALUES 목록: 22개
   - **문제 발생 경로**: bi에는 REG_DATE/REG_TIME/MEMO 값이 존재하나 SQL에서 제외됨
   - SQLite: NOT NULL 아니므로 NULL로 저장, 예외 없음

4. **읽기 조회** (selectqueryBarcodeInfo, selectqueryBarcodeInfoSearchType)
   - SELECT에는 REG_DATE, REG_TIME, MEMO 포함 (L916~920, L1002~1006)
   - cursor.getString()으로 읽기: NULL → `""` 반환
   - 현재 앱 로직에서 REG_DATE, REG_TIME, MEMO를 사용하는 코드 없음 → 동작 영향 없음

## 영향 범위

| 컬럼 | 저장 상태 | 앱 내 사용처 | 동작 영향 |
|------|-----------|------------|:--------:|
| REG_DATE | NULL (빈값) | 없음 | **없음** |
| REG_TIME | NULL (빈값) | 없음 | **없음** |
| MEMO | NULL (빈값) | 없음 | **없음** |

- 영향 파일: `app/src/main/java/com/rgbsolution/highland_emart/db/DBHandler.java`
- 영향 함수: `insertqueryBarcodeInfo()`
- 모든 searchType(0~7) 공통 영향 — 단일 INSERT 함수를 공유
- NOT NULL 제약 없음 → SQL 예외 발생하지 않음
- JSP에서도 `'' AS REG_DATE`, `'' AS REG_TIME`, `'' AS MEMO` 로 빈 문자열 전송 → 실 데이터 없음
- **현재 앱에서 이 3개 컬럼을 조회하거나 표시하는 로직이 없으므로 동작상 영향 없음**

## 수정 방안

### 수정 1: INSERT 컬럼 목록에 REG_DATE, REG_TIME, MEMO 추가

`DBHandler.java:1087~1112` `insertqueryBarcodeInfo()` 수정

```java
// 수정 전
+ DBInfo.STATUS + ", "
+ DBInfo.REG_ID + ", "
+ DBInfo.SHELF_LIFE
+ ") VALUES('"
...
+ Common.nullCheck(bi.getREG_ID(), "") + "','"
+ Common.nullCheck(bi.getSHELF_LIFE(), "") + "')";

// 수정 후
+ DBInfo.STATUS + ", "
+ DBInfo.REG_ID + ", "
+ DBInfo.REG_DATE + ", "
+ DBInfo.REG_TIME + ", "
+ DBInfo.MEMO + ", "
+ DBInfo.SHELF_LIFE
+ ") VALUES('"
...
+ Common.nullCheck(bi.getREG_ID(), "") + "','"
+ Common.nullCheck(bi.getREG_DATE(), "") + "','"
+ Common.nullCheck(bi.getREG_TIME(), "") + "','"
+ Common.nullCheck(bi.getMEMO(), "") + "','"
+ Common.nullCheck(bi.getSHELF_LIFE(), "") + "')";
```

> 주의: 원본(PDA-INNO(원본))도 동일하게 누락되어 있으나, CREATE TABLE과의 정합성 및 데이터 완전성을 위해 수정한다. JSP는 현재 빈 문자열(`''`)을 전송하므로 수정 후에도 빈 문자열로 저장되며 동작 변화 없음.

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/오류/09_소스정합성체크_컬럼누락_미설정.md` — 동일 오류 최초 기록 (간략 버전)
- `app/doc/소스분석/37_로컬DB_MSSQL전환_현황.md` — 로컬DB 전환 현황
- 패턴 C: 컬럼명/구조 변경 (INSERT 컬럼 목록과 CREATE TABLE 불일치)
