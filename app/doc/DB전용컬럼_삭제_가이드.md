# DB 전용 컬럼 삭제 가이드

**작성일**: 2026-01-16
**검증 범위**: JSP 24개, Java 32개, VIEW 문서

---

## 1. 삭제 대상 컬럼 (8개)

| # | VIEW idx | temp[] idx | 컬럼명 | 설명 |
|:-:|:--------:|:----------:|--------|------|
| 1 | 0 | temp[0] | GI_H_ID | 출고헤더ID |
| 2 | 2 | temp[2] | EOI_ID | 이마트 주문ID |
| 3 | 9 | temp[9] | AMOUNT | 출하상품금액 |
| 4 | 10 | temp[10] | GOODS_R_ID | 입고번호 |
| 5 | 11 | temp[11] | GR_REF_NO | 창고입고번호 |
| 6 | 15 | temp[15] | BRANDNAME | 브랜드명 |
| 7 | 23 | temp[23] | PACKERNAME | 패커명 |
| 8 | 31 | temp[31] | EMARTLOGIS_NAME | 이마트물류명 |

---

## 2. 데이터 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. 서버 VIEW (VW_PDA_WID_LIST 등)                               │
│    8개 컬럼 포함 SELECT                                         │
│    ※ 수정 불가 (서버 DB)                                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. JSP (12개 파일)                                              │
│    rs.getString("GI_H_ID") + "::" + ... 형태로 앱 전송          │
│    ※ 수정 불가 (서버 배포 필요)                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. ProgressDlgShipSearch.java                                   │
│    si.setGI_H_ID(temp[0].toString());                           │
│    ※ 수정 불가 (temp[] 인덱스 순서 유지 필수)                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. Shipments_Info.java                                          │
│    필드/getter/setter                                           │
│    ※ 수정 불가 (파싱에서 setter 호출)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. DBHandler.java                           ← ✅ 수정 대상     │
│    CREATE TABLE / INSERT / SELECT                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6. TB_SHIPMENT (로컬 SQLite)                                    │
│    41개 컬럼 → 33개 컬럼                                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 삭제 범위

### 3.1 수정 불가 (유지 필수)

| 파일 | 이유 |
|------|------|
| 서버 VIEW | 앱에서 수정 불가 |
| JSP 12개 | 서버 배포 필요, temp[] 순서 결정 |
| ProgressDlgShipSearch.java | temp[] 인덱스 순서 유지 필수 |
| Shipments_Info.java | setter가 파싱에서 호출됨 |

### 3.2 수정 대상

| 파일 | 수정 내용 |
|------|-----------|
| DBHandler.java | CREATE/INSERT/SELECT에서 8개 컬럼 제거 |
| DBHelper.java | DB 버전 증가, onUpgrade 추가 |
| TestDataHelper.java | 8개 setter 제거 |
| DBInfo.java | 8개 상수 삭제 (선택) |

---

## 4. 수정 순서

```
┌─────────────────────────────────────────────────────────────────┐
│                        수정 순서                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Step 1. DBHandler.java - CREATE TABLE 수정                     │
│      │   createqueryShipment() - 8개 컬럼 정의 삭제             │
│      ▼                                                          │
│  Step 2. DBHandler.java - SELECT 쿼리 수정 (4개 함수)           │
│      │   2-1. selectqueryShipment()                             │
│      │   2-2. selectqueryShipmentOnly()                         │
│      │   2-3. selectqueryShipmentBL()                           │
│      │   2-4. selectqueryAllShipment()                          │
│      ▼                                                          │
│  Step 3. DBHandler.java - INSERT 쿼리 수정                      │
│      │   insertqueryShipment() - 8개 컬럼명 + VALUES 삭제       │
│      ▼                                                          │
│  Step 4. DBHelper.java - DB 마이그레이션                        │
│      │   DB_VERSION 증가, onUpgrade() 추가                      │
│      ▼                                                          │
│  Step 5. TestDataHelper.java - 테스트 데이터 수정               │
│      │   8개 setter 호출 삭제                                   │
│      ▼                                                          │
│  Step 6. DBInfo.java - 상수 삭제 (선택)                         │
│      ▼                                                          │
│  Step 7. 컴파일 및 테스트                                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

| 순서 | 이유 |
|------|------|
| CREATE TABLE 먼저 | 테이블 구조가 기준 |
| SELECT 다음 | 존재하지 않는 컬럼 SELECT 시 런타임 에러 |
| INSERT 다음 | 존재하지 않는 컬럼 INSERT 시 런타임 에러 |
| DB 마이그레이션 | 코드 수정 완료 후 DB 버전 증가 |

### 4.1 진행 체크리스트

```
[ ] Step 1. CREATE TABLE 수정 완료
[ ] Step 2-1. selectqueryShipment 수정 완료
[ ] Step 2-2. selectqueryShipmentOnly 수정 완료
[ ] Step 2-3. selectqueryShipmentBL 수정 완료
[ ] Step 2-4. selectqueryAllShipment 수정 완료
[ ] Step 3. INSERT 쿼리 수정 완료
[ ] Step 4. DB 마이그레이션 완료
[ ] Step 5. TestDataHelper 수정 완료
[ ] Step 6. DBInfo 상수 삭제 완료 (선택)
[ ] Step 7. 컴파일 및 테스트 완료
```

---

## 5. Step별 수정 상세

### Step 1. CREATE TABLE (라인 32-75)

**함수**: `createqueryShipment()`

| 라인 | 삭제 코드 |
|:----:|-----------|
| 35 | `+ DBInfo.GI_H_ID + " TEXT NOT NULL, "` |
| 37 | `+ DBInfo.EOI_ID + " TEXT NOT NULL, "` |
| 44 | `+ DBInfo.AMOUNT + " TEXT NOT NULL, "` |
| 45 | `+ DBInfo.GOODS_R_ID + " TEXT NOT NULL, "` |
| 46 | `+ DBInfo.GR_REF_NO + " TEXT NOT NULL, "` |
| 50 | `+ DBInfo.BRANDNAME + " TEXT NOT NULL, "` |
| 58 | `+ DBInfo.PACKERNAME + " TEXT NOT NULL, "` |
| 66 | `+ DBInfo.EMARTLOGIS_NAME + " TEXT, "` |

---

### Step 2-1. selectqueryShipment (라인 108-196)

**SELECT 컬럼:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 110 | `+ DBInfo.GI_H_ID + ", "` |
| 112 | `+ DBInfo.EOI_ID + ", "` |
| 119 | `+ DBInfo.AMOUNT + ", "` |
| 120 | `+ DBInfo.GOODS_R_ID + ", "` |
| 121 | `+ DBInfo.GR_REF_NO + ", "` |
| 125 | `+ DBInfo.BRANDNAME + ", "` |
| 133 | `+ DBInfo.PACKERNAME + ", "` |
| 141 | `+ DBInfo.EMARTLOGIS_NAME + ", "` |

**cursor setter:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 165 | `si.setGI_H_ID(...)` |
| 167 | `si.setEOI_ID(...)` |
| 174 | `si.setAMOUNT(...)` |
| 175 | `si.setGOODS_R_ID(...)` |
| 176 | `si.setGR_REF_NO(...)` |
| 180 | `si.setBRANDNAME(...)` |
| 188 | `si.setPACKERNAME(...)` |
| 196 | `si.setEMARTLOGIS_NAME(...)` |

---

### Step 2-2. selectqueryShipmentOnly (라인 229-308)

**SELECT 컬럼:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 231 | `+ DBInfo.GI_H_ID + ", "` |
| 233 | `+ DBInfo.EOI_ID + ", "` |
| 240 | `+ DBInfo.AMOUNT + ", "` |
| 241 | `+ DBInfo.GOODS_R_ID + ", "` |
| 242 | `+ DBInfo.GR_REF_NO + ", "` |
| 246 | `+ DBInfo.BRANDNAME + ", "` |
| 254 | `+ DBInfo.PACKERNAME + ", "` |
| 262 | `+ DBInfo.EMARTLOGIS_NAME + ", "` |

**cursor setter:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 277 | `si.setGI_H_ID(...)` |
| 279 | `si.setEOI_ID(...)` |
| 286 | `si.setAMOUNT(...)` |
| 287 | `si.setGOODS_R_ID(...)` |
| 288 | `si.setGR_REF_NO(...)` |
| 292 | `si.setBRANDNAME(...)` |
| 300 | `si.setPACKERNAME(...)` |
| 308 | `si.setEMARTLOGIS_NAME(...)` |

---

### Step 2-3. selectqueryShipmentBL (라인 333-413)

**SELECT 컬럼:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 335 | `+ DBInfo.GI_H_ID + ", "` |
| 337 | `+ DBInfo.EOI_ID + ", "` |
| 344 | `+ DBInfo.AMOUNT + ", "` |
| 345 | `+ DBInfo.GOODS_R_ID + ", "` |
| 346 | `+ DBInfo.GR_REF_NO + ", "` |
| 350 | `+ DBInfo.BRANDNAME + ", "` |
| 358 | `+ DBInfo.PACKERNAME + ", "` |
| 366 | `+ DBInfo.EMARTLOGIS_NAME + ", "` |

**cursor setter:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 382 | `si.setGI_H_ID(...)` |
| 384 | `si.setEOI_ID(...)` |
| 391 | `si.setAMOUNT(...)` |
| 392 | `si.setGOODS_R_ID(...)` |
| 393 | `si.setGR_REF_NO(...)` |
| 397 | `si.setBRANDNAME(...)` |
| 405 | `si.setPACKERNAME(...)` |
| 413 | `si.setEMARTLOGIS_NAME(...)` |

---

### Step 2-4. selectqueryAllShipment (라인 436-453)

**수정 전:**
```java
String sqlStr = "SELECT "
        + DBInfo.GI_D_ID + ", "
        + DBInfo.EOI_ID
        + " FROM " + DBInfo.TABLE_NAME_SHIPMENT
        + " ORDER BY EOI_ID ASC";

si.setEOI_ID(Common.nullCheck(cursor.getString(cursor.getColumnIndex("EOI_ID")), ""));
```

**수정 후:**
```java
String sqlStr = "SELECT "
        + DBInfo.GI_D_ID
        + " FROM " + DBInfo.TABLE_NAME_SHIPMENT
        + " ORDER BY GI_D_ID ASC";

// EOI_ID setter 삭제
```

---

### Step 3. INSERT 쿼리 (라인 621-703)

**함수**: `insertqueryShipment()`

**INSERT 컬럼명:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 622 | `+ DBInfo.GI_H_ID + ", "` |
| 624 | `+ DBInfo.EOI_ID + ", "` |
| 631 | `+ DBInfo.AMOUNT + ", "` |
| 632 | `+ DBInfo.GOODS_R_ID + ", "` |
| 633 | `+ DBInfo.GR_REF_NO + ", "` |
| 637 | `+ DBInfo.BRANDNAME + ", "` |
| 645 | `+ DBInfo.PACKERNAME + ", "` |
| 653 | `+ DBInfo.EMARTLOGIS_NAME + ", "` |

**INSERT VALUES:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 663 | `+ Common.nullCheck(si.getGI_H_ID(), "") + "','"` |
| 665 | `+ Common.nullCheck(si.getEOI_ID(), "") + "','"` |
| 672 | `+ Common.nullCheck(si.getAMOUNT(), "") + "','"` |
| 673 | `+ Common.nullCheck(si.getGOODS_R_ID(), "") + "','"` |
| 674 | `+ Common.nullCheck(si.getGR_REF_NO(), "") + "','"` |
| 678 | `+ Common.nullCheck(si.getBRANDNAME(), "") + "','"` |
| 686 | `+ Common.nullCheck(si.getPACKERNAME(), "") + "','"` |
| 694 | `+ Common.nullCheck(si.getEMARTLOGIS_NAME(), "") + "','"` |

---

### Step 4. DB 마이그레이션

**파일**: `DBHelper.java` (라인 22, 40-65)

```java
// 1. DB 버전 증가 (라인 22)
private static final int DATABASE_VERSION = 28;  // 기존 27 → 28

// 2. onUpgrade 메소드 (라인 40-65)
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 28) {
        db.execSQL("DROP TABLE IF EXISTS " + DBInfo.TABLE_NAME_SHIPMENT);
    }
}
```

---

### Step 5. TestDataHelper.java

**삭제할 setter:**

| 라인 | 삭제 코드 |
|:----:|-----------|
| 54 | WHERE GI_H_ID 조건 → 다른 조건으로 변경 |
| 83, 115, 141, 170, 194, 223 | `si.setGI_H_ID(...)` |
| 85, 117, 143, 172, 196, 225 | `si.setEOI_ID(...)` |
| 252 | `si.setAMOUNT(...)` |
| 255 | `si.setBRANDNAME(...)` |
| 258 | `si.setPACKERNAME(...)` |
| 261 | `si.setGOODS_R_ID(...)` |
| 262 | `si.setGR_REF_NO(...)` |
| 269 | `si.setEMARTLOGIS_NAME(...)` |

---

### Step 6. DBInfo.java (선택)

| 라인 | 삭제 코드 |
|:----:|-----------|
| 24 | `public static final String GI_H_ID = "GI_H_ID";` |
| 26 | `public static final String EOI_ID = "EOI_ID";` |
| 33 | `public static final String AMOUNT = "AMOUNT";` |
| 34 | `public static final String GOODS_R_ID = "GOODS_R_ID";` |
| 35 | `public static final String GR_REF_NO = "GR_REF_NO";` |
| 39 | `public static final String BRANDNAME = "BRANDNAME";` |
| 47 | `public static final String PACKERNAME = "PACKERNAME";` |
| 57 | `public static final String EMARTLOGIS_NAME = "EMARTLOGIS_NAME";` |

**참고**: 상수는 삭제하지 않아도 됨 (미사용 상수로 유지 가능)

---

## 6. 테스트

### 6.1 컴파일 테스트

```
[ ] Android Studio → Build → Rebuild Project
[ ] 컴파일 에러 없음
[ ] APK 생성 성공
```

### 6.2 기능 테스트

| # | 시나리오 | 예상 결과 |
|:-:|----------|-----------|
| 1 | 신규 설치 | 33컬럼 테이블 생성 |
| 2 | 업그레이드 | 테이블 재생성 |
| 3 | 출하대상 조회 | 정상 파싱, 저장 |
| 4 | 계근 | 정상 동작 |
| 5 | 서버 전송 | 정상 전송 |

---

## 7. 체크리스트

```
Step 1. CREATE TABLE
    [ ] createqueryShipment - 8개 컬럼 삭제

Step 2. SELECT 쿼리
    [ ] selectqueryShipment - SELECT 8개 + cursor 8개
    [ ] selectqueryShipmentOnly - SELECT 8개 + cursor 8개
    [ ] selectqueryShipmentBL - SELECT 8개 + cursor 8개
    [ ] selectqueryAllShipment - EOI_ID + ORDER BY

Step 3. INSERT 쿼리
    [ ] insertqueryShipment - 컬럼명 8개 + VALUES 8개

Step 4. DB 마이그레이션
    [ ] DBHelper.java - DATABASE_VERSION 27 → 28 증가 (라인 22)
    [ ] DBHelper.java - onUpgrade() 수정 (라인 40-65)

Step 5. 테스트 데이터
    [ ] TestDataHelper.java - setter 제거

Step 6. 상수 삭제 (선택)
    [ ] DBInfo.java - 8개 상수 삭제

Step 7. 테스트
    [ ] 컴파일 성공
    [ ] 신규 설치 테스트
    [ ] 업그레이드 테스트
    [ ] 기능 테스트
```

---

## 8. 수정 결과

| 항목 | 수정 전 | 수정 후 |
|------|:-------:|:-------:|
| TB_SHIPMENT 컬럼 | 41개 | 33개 |
| 수정 파일 | - | 4개 |
| 수정 함수 | - | 6개 |
| 삭제 라인 | - | ~80 라인 |

---

## 9. 핵심 요약

```
1. JSP/VIEW 수정 없이 앱만 수정
2. ProgressDlgShipSearch temp[] 파싱 유지 (삭제 불가)
3. Shipments_Info 필드/getter/setter 유지 (삭제 불가)
4. DBHandler CREATE/INSERT/SELECT만 수정
5. DB 저장만 생략, 파싱은 계속 수행
```

---

**문서 작성일**: 2026-01-16
**관련 문서**: VIEW_로컬DB_컬럼비교.md, 로컬DB_구조.md
