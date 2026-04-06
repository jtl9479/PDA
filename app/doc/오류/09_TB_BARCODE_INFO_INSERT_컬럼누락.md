# TB_BARCODE_INFO INSERT 시 REG_DATE, REG_TIME, MEMO 컬럼 누락

## 발견일
2026-04-06

## 현상
- search_barcode_info.jsp에서 REG_DATE, REG_TIME, MEMO 값을 응답으로 전달
- ProgressDlgBarcodeSearch에서 temp[21]~temp[23]으로 파싱하여 Barcodes_Info 객체에 set
- **DBHandler.insertqueryBarcodeInfo()에서 해당 3개 컬럼을 INSERT하지 않음**
- TB_BARCODE_INFO 테이블에 REG_DATE, REG_TIME, MEMO 값이 저장되지 않음

## 원래부터 있던 버그인가?

**YES - 원본(PDA-INNO(원본))에서도 동일하게 누락되어 있음**

```java
// 원본 DBHandler.java:insertqueryBarcodeInfo()
// INSERT 컬럼 목록에 REG_DATE, REG_TIME, MEMO 없음
// 현재 프로젝트도 동일
```

## 원인

### 문제 1 (주요): insertqueryBarcodeInfo()에서 3개 컬럼 미포함

#### 코드 위치
- `DBHandler.java` : 918~988줄

#### 현재 문제 코드
```java
// DBHandler.java:924~970 - INSERT 컬럼 목록 (22개)
String sqlStr = "INSERT INTO " + DBInfo.TABLE_NAME_BARCODE_INFO + " ("
    + DBInfo.PACKER_CLIENT_CODE + ", "
    + DBInfo.BRAND_CODE + ", "
    + DBInfo.PACKER_PRODUCT_CODE + ", "
    // ... 중간 생략 ...
    + DBInfo.STATUS + ", "
    + DBInfo.REG_ID + ", "
    // ★ REG_DATE 없음
    // ★ REG_TIME 없음
    + DBInfo.SHELF_LIFE
    // ★ MEMO 없음
    + ") VALUES(...)";
```

#### CREATE TABLE에는 존재
```java
// DBHandler.java:699~731 - CREATE TABLE (25개 데이터 컬럼)
+ DBInfo.REG_ID + " TEXT, "
+ DBInfo.REG_DATE + " TEXT, "      // ★ 테이블에는 정의됨
+ DBInfo.REG_TIME + " TEXT, "      // ★ 테이블에는 정의됨
+ DBInfo.MEMO + " TEXT, "          // ★ 테이블에는 정의됨
+ DBInfo.SHELF_LIFE + " TEXT "
```

#### 파싱에서는 set 함
```java
// ProgressDlgBarcodeSearch.java:129~131
bi.setREG_DATE(temp[21]);    // ★ set은 하지만 DB에 저장 안됨
bi.setREG_TIME(temp[22]);    // ★ set은 하지만 DB에 저장 안됨
bi.setMEMO(temp[23]);        // ★ set은 하지만 DB에 저장 안됨
```

## 상세 흐름

1. **search_barcode_info.jsp** 응답
   - temp[20]=REG_ID, temp[21]=REG_DATE, temp[22]=REG_TIME, temp[23]=MEMO 전달

2. **ProgressDlgBarcodeSearch** 파싱
   - Barcodes_Info 객체에 REG_DATE, REG_TIME, MEMO set

3. **DBHandler.insertqueryBarcodeInfo()** INSERT
   - INSERT 컬럼 목록에 REG_DATE, REG_TIME, MEMO **미포함**
   - TB_BARCODE_INFO에 해당 값 **저장되지 않음** (NULL)

## 영향 범위
- `DBHandler.java` : insertqueryBarcodeInfo() (918~988줄)
- TB_BARCODE_INFO 테이블의 REG_DATE, REG_TIME, MEMO 컬럼이 항상 NULL
- 해당 3개 컬럼을 조회하는 코드는 없으므로 **실제 기능 동작에 영향 없음**

## 수정 방안

### 수정: INSERT 컬럼에 REG_DATE, REG_TIME, MEMO 추가

```java
// DBHandler.java:insertqueryBarcodeInfo()
+ DBInfo.REG_ID + ", "
+ DBInfo.REG_DATE + ", "      // 추가
+ DBInfo.REG_TIME + ", "      // 추가
+ DBInfo.MEMO + ", "          // 추가
+ DBInfo.SHELF_LIFE
```

> 원본과 동일한 버그이므로 **기존 기능 100% 동일 원칙**에 따라 현재는 수정하지 않는다.

## 상태
- [ ] 미수정 (원본과 동일, 변경 불필요)
