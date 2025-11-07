# DBHelper.java - SQLite 데이터베이스 헬퍼

## 개요
DBHelper 클래스는 Highland E-mart PDA 애플리케이션의 SQLite 데이터베이스를 관리하는 헬퍼 클래스입니다. SQLiteOpenHelper를 상속받아 데이터베이스 생성, 업그레이드, 기본 쿼리 실행 기능을 제공합니다.

- **파일 경로**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\db\DBHelper.java`
- **패키지**: `com.rgbsolution.highland_emart.db`
- **총 라인 수**: 89줄
- **상속**: `SQLiteOpenHelper`

## 클래스 구조

### 주요 필드

```java
public static final String TAG = "DBHelper";
private DatabaseHelper mDbHelper;                    // 내부 헬퍼 클래스 인스턴스
private SQLiteDatabase mDb;                          // SQLite 데이터베이스 인스턴스
private static final String DATABASE_NAME = "HIGHLAND";      // 데이터베이스 이름
private static final int DATABASE_VERSION = 27;              // 데이터베이스 버전
private final Context mCtx;                          // 컨텍스트
```

#### 필드 설명
- **DATABASE_NAME**: 데이터베이스 파일명 (HIGHLAND)
- **DATABASE_VERSION**: 현재 데이터베이스 스키마 버전 (27)
  - 업그레이드가 필요할 경우 이 값을 증가시킴

## 내부 클래스: DatabaseHelper

### 1. 생성자 (28-30줄)

```java
DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
}
```

**설명**: SQLiteOpenHelper 생성자를 호출하여 데이터베이스 초기화

**매개변수**:
- `context`: 애플리케이션 컨텍스트
- `DATABASE_NAME`: 데이터베이스 이름
- `null`: CursorFactory (기본값 사용)
- `DATABASE_VERSION`: 데이터베이스 버전

### 2. onCreate 메서드 (32-37줄)

```java
@Override
public void onCreate(SQLiteDatabase db) {
    if (Common.D) {
        Log.d(TAG, "Table Upgrade ........ ");
    }
}
```

**설명**: 데이터베이스가 처음 생성될 때 호출되는 메서드

**동작**:
- 현재는 로그만 출력
- 실제 테이블 생성은 DBHandler의 createquery 메서드들에서 수행

**호출 시점**:
- 앱 최초 실행 시
- 데이터베이스 파일이 존재하지 않을 때

### 3. onUpgrade 메서드 (39-65줄)

```java
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.d(TAG, "Upgrading from version " + oldVersion + " to " + newVersion);

    Log.d(TAG, "업그레이드 하러 들어옴 : " + oldVersion + " to " + newVersion);
    try {
        db.beginTransaction();
        // 업그레이드 SQL 실행 (현재 주석처리됨)
        // db.execSQL("DROP TABLE TB_GOODS_WET");
        // db.execSQL("ALTER TABLE TB_SHIPMENT ADD COLUMN WH_AREA text");
        // db.execSQL("ALTER TABLE TB_GOODS_WET ADD COLUMN BOX_ORDER Integer DEFAULT 0");
        // db.execSQL("ALTER TABLE TB_SHIPMENT ADD COLUMN LAST_BOX_ORDER INTEGER");
        db.setTransactionSuccessful();
    } catch (IllegalStateException e) {
        Log.d(TAG, "onUpgrade exception -> " + e.getMessage().toString());
    } finally {
        db.endTransaction();
    }

    if (Common.D) {
        Log.d(TAG, "Table Upgrade Drop ");
    }
}
```

**설명**: 데이터베이스 버전이 업그레이드될 때 호출되는 메서드

**매개변수**:
- `db`: SQLiteDatabase 인스턴스
- `oldVersion`: 이전 데이터베이스 버전
- `newVersion`: 새로운 데이터베이스 버전

**동작**:
1. 버전 정보 로그 출력
2. 트랜잭션 시작
3. 스키마 변경 SQL 실행 (현재 주석처리)
4. 트랜잭션 커밋
5. 예외 발생 시 자동 롤백

**과거 업그레이드 이력** (주석으로 보존):
- `DROP TABLE TB_GOODS_WET`: 계근 테이블 삭제
- `ALTER TABLE TB_SHIPMENT ADD COLUMN WH_AREA text`: 창고구역 컬럼 추가
- `ALTER TABLE TB_GOODS_WET ADD COLUMN BOX_ORDER Integer`: 박스순서 컬럼 추가
- `ALTER TABLE TB_SHIPMENT ADD COLUMN LAST_BOX_ORDER INTEGER`: 마지막 박스순서 컬럼 추가

**호출 시점**:
- DATABASE_VERSION 값이 증가했을 때
- 기존 데이터베이스보다 높은 버전으로 업그레이드 시

## 공개 메서드

### 4. DBHelper 생성자 (68-70줄)

```java
public DBHelper(Context ctx) {
    this.mCtx = ctx;
}
```

**설명**: DBHelper 인스턴스 생성자

**매개변수**:
- `ctx`: 애플리케이션 컨텍스트

**반환**: DBHelper 인스턴스

### 5. open 메서드 (72-76줄)

```java
public DBHelper open() throws SQLException {
    mDbHelper = new DatabaseHelper(mCtx);
    mDb = mDbHelper.getWritableDatabase();
    return this;
}
```

**설명**: 데이터베이스 연결을 열고 쓰기 가능한 데이터베이스 인스턴스를 반환

**동작**:
1. DatabaseHelper 인스턴스 생성
2. 쓰기 가능한 데이터베이스 획득
3. 자기 자신을 반환 (메서드 체이닝 가능)

**예외**: SQLException - 데이터베이스 열기 실패 시

**반환**: DBHelper 인스턴스 (메서드 체이닝용)

**사용 예시**:
```java
DBHelper dbHelper = new DBHelper(context);
dbHelper.open();
```

### 6. close 메서드 (78-80줄)

```java
public void close() {
    mDbHelper.close();
}
```

**설명**: 데이터베이스 연결을 닫음

**동작**: DatabaseHelper의 close() 메서드 호출

**주의사항**:
- 사용 후 반드시 호출하여 리소스 해제
- try-finally 블록에서 호출 권장

### 7. selectSql 메서드 (82-84줄)

```java
public Cursor selectSql(String sql) {
    return mDb.rawQuery(sql, null);
}
```

**설명**: SELECT 쿼리를 실행하고 결과를 Cursor로 반환

**매개변수**:
- `sql`: 실행할 SQL SELECT 문

**반환**: Cursor - 쿼리 결과를 포함하는 커서

**사용 예시**:
```java
String sql = "SELECT * FROM TB_SHIPMENT WHERE ITEM_CODE = '12345'";
Cursor cursor = dbHelper.selectSql(sql);
while (cursor.moveToNext()) {
    // 데이터 처리
}
cursor.close();
```

### 8. executeSql 메서드 (86-88줄)

```java
public void executeSql(String sql) {
    mDb.execSQL(sql);
}
```

**설명**: INSERT, UPDATE, DELETE, CREATE, DROP 등의 SQL 문을 실행

**매개변수**:
- `sql`: 실행할 SQL 문

**반환**: 없음

**사용 예시**:
```java
String sql = "UPDATE TB_SHIPMENT SET SAVE_TYPE = 'Y' WHERE ITEM_CODE = '12345'";
dbHelper.executeSql(sql);
```

## 사용 패턴

### 기본 사용 패턴

```java
DBHelper dbHelper = new DBHelper(context);
dbHelper.open();
try {
    // 데이터베이스 작업 수행
    Cursor cursor = dbHelper.selectSql("SELECT * FROM TB_SHIPMENT");
    // 결과 처리
    cursor.close();
} catch (Exception e) {
    Log.e(TAG, "Database error: " + e.getMessage());
} finally {
    dbHelper.close();
}
```

### SELECT 쿼리 실행

```java
DBHelper mDbHelper = new DBHelper(context);
mDbHelper.open();

String sqlStr = "SELECT ITEM_CODE, ITEM_NAME FROM TB_SHIPMENT WHERE BRAND_CODE = 'ABC'";
Cursor cursor = mDbHelper.selectSql(sqlStr);

while (cursor.moveToNext()) {
    String itemCode = cursor.getString(cursor.getColumnIndex("ITEM_CODE"));
    String itemName = cursor.getString(cursor.getColumnIndex("ITEM_NAME"));
    // 데이터 처리
}
cursor.close();
mDbHelper.close();
```

### INSERT/UPDATE/DELETE 실행

```java
DBHelper dbHelper = new DBHelper(context);
dbHelper.open();

try {
    String insertSql = "INSERT INTO TB_SHIPMENT (ITEM_CODE, ITEM_NAME) VALUES ('12345', 'Product A')";
    dbHelper.executeSql(insertSql);

    String updateSql = "UPDATE TB_SHIPMENT SET SAVE_TYPE = 'Y' WHERE ITEM_CODE = '12345'";
    dbHelper.executeSql(updateSql);
} catch (Exception e) {
    Log.e(TAG, "Error: " + e.getMessage());
} finally {
    dbHelper.close();
}
```

### 트랜잭션 처리

```java
DBHelper dbHelper = new DBHelper(context);
dbHelper.open();

try {
    dbHelper.executeSql("BEGIN TRANSACTION");
    dbHelper.executeSql("INSERT INTO TB_SHIPMENT ...");
    dbHelper.executeSql("UPDATE TB_GOODS_WET ...");
    dbHelper.executeSql("COMMIT");
} catch (Exception e) {
    dbHelper.executeSql("ROLLBACK");
    Log.e(TAG, "Transaction failed: " + e.getMessage());
} finally {
    dbHelper.close();
}
```

## 데이터베이스 버전 관리

### 버전 업그레이드 절차

1. **DATABASE_VERSION 증가**
   ```java
   private static final int DATABASE_VERSION = 28; // 27 -> 28로 증가
   ```

2. **onUpgrade 메서드에 변경사항 추가**
   ```java
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       try {
           db.beginTransaction();
           if (oldVersion < 28) {
               db.execSQL("ALTER TABLE TB_SHIPMENT ADD COLUMN NEW_COLUMN TEXT");
           }
           db.setTransactionSuccessful();
       } finally {
           db.endTransaction();
       }
   }
   ```

3. **앱 재설치 또는 업데이트 시 자동 적용**

## 관련 파일

- **DBInfo.java**: 테이블명 및 컬럼명 상수 정의
- **DBHandler.java**: 실제 CRUD 작업을 수행하는 클래스
- **Common.java**: 디버그 플래그 및 공통 유틸리티

## 주의사항

1. **open()과 close() 쌍으로 사용**
   - 항상 finally 블록에서 close() 호출

2. **Cursor 자원 관리**
   - selectSql() 후 반드시 cursor.close() 호출

3. **트랜잭션 사용**
   - 여러 쿼리를 실행할 때는 트랜잭션 사용 권장

4. **버전 관리**
   - 스키마 변경 시 DATABASE_VERSION 증가 필수

5. **예외 처리**
   - SQLException 및 일반 Exception 처리 필요

## 디버그 로그

- `Common.D` 플래그가 true일 때만 로그 출력
- 업그레이드 과정 추적 가능
- 문제 발생 시 로그 확인 권장
