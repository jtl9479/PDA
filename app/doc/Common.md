# Common.java

## 개요

`Common.java`는 Highland E-Mart PDA 애플리케이션의 전역 설정 및 유틸리티를 제공하는 클래스입니다. 서버 통신을 위한 URL 상수, 애플리케이션 전역 변수, 그리고 데이터 검증을 위한 유틸리티 메서드를 포함합니다.

**파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\common\Common.java`

**라인 수**: 81줄

---

## 주요 구성 요소

### 1. 디버그 플래그

```java
public static boolean D = true;  // Debug ? true : false
```

애플리케이션의 디버그 모드를 제어하는 전역 플래그입니다.

---

### 2. URL 상수 (21개)

서버와의 통신을 위한 API 엔드포인트 URL을 정의합니다. 현재는 빈 문자열로 초기화되어 있으며, 런타임에 설정됩니다.

#### 기본 URL
- `URL_VERSION` - 버전 확인
- `URL_LOGIN` - 관리자 로그인

#### 출하 관련 URL
- `URL_SEARCH_SHIPMENT` - 일반 출하 조회
- `URL_SEARCH_SHIPMENT_HOMEPLUS` - 홈플러스 출하 조회
- `URL_SEARCH_SHIPMENT_WHOLESALE` - 도매 출하 조회
- `URL_SEARCH_SHIPMENT_LOTTE` - 롯데 출하 조회
- `URL_UPDATE_SHIPMENT` - 출하 정보 수정

#### 생산 관련 URL
- `URL_SEARCH_PRODUCTION` - 생산 정보 조회
- `URL_SEARCH_PRODUCTION_4LABEL` - 4라벨 생산 정보 조회
- `URL_SEARCH_PRODUCTION_NONFIXED` - 비정량 생산 정보 조회
- `URL_WET_PRODUCTION_CALC` - 습중량 생산 계산

#### 바코드 관련 URL
- `URL_SEARCH_BARCODE_INFO` - 바코드 정보 조회
- `URL_INSERT_BARCODE_INFO` - 바코드 정보 추가
- `URL_UPDATE_BARCODE_INFO` - 바코드 정보 수정
- `URL_SEARCH_BARCODE_INFO_NONFIXED` - 비정량 바코드 정보 조회

#### 습중량(Wet Weight) 관련 URL
- `URL_SEARCH_GOODS_WET` - 습중량 상품 조회
- `URL_INSERT_GOODS_WET` - 습중량 상품 추가
- `URL_INSERT_GOODS_WET_NEW` - 신규 습중량 상품 추가
- `URL_INSERT_GOODS_WET_HOMEPLUS` - 홈플러스 습중량 상품 추가

#### 홈플러스 비정량 URL
- `URL_SEARCH_HOMEPLUS_NONFIXED` - 홈플러스 비정량 조회 1
- `URL_SEARCH_HOMEPLUS_NONFIXED2` - 홈플러스 비정량 조회 2

---

### 3. 전역 변수

#### 사용자 정보
```java
public static String REG_ID = "";          // 사용자 ID
public static String USER_TYPE = "";        // 사용자 권한
```

#### 검색 및 필터 관련
```java
public static boolean search_bool = false;  // 검색 상태
public static String selectDay = "";        // 계근 선택 날짜
public static String selectWarehouse = "";  // 창고
public static String searchType = "0";      // 계근대상 종류 (기본값: "0")
```

#### 프린터 설정
```java
public static String printer_address = "";   // 모바일프린터 MAC주소
public static boolean printer_setting = true; // 모바일프린터 사용여부
public static boolean print_bool = true;     // 인쇄 여부
```

#### 센터 정보
```java
public static ArrayList<String> list_center_info;  // 센터 정보 목록
```

---

### 4. 유틸리티 메서드

#### `nullCheck(String value, String defaultValue)`

데이터의 null 여부를 체크하고 기본값을 반환합니다.

**파라미터**:
- `value` - 검사할 문자열 값
- `defaultValue` - null일 경우 반환할 기본값

**반환값**:
- `value`가 null, 빈 문자열(""), 또는 "null" 문자열인 경우 `defaultValue` 반환
- 그 외의 경우 `value` 반환

**예외 처리**:
- Exception 발생 시 로그 출력 후 `defaultValue` 반환

---

## 사용 예시

### 1. URL 설정

```java
// 애플리케이션 초기화 시 URL 설정
Common.URL_LOGIN = "http://183.111.165.158:8080/highland/real/manager_login.jsp";
Common.URL_SEARCH_SHIPMENT = "http://183.111.165.158:8080/highland/real/search_shipment.jsp";
```

### 2. 사용자 정보 저장

```java
// 로그인 성공 후 사용자 정보 저장
Common.REG_ID = "user001";
Common.USER_TYPE = "ADMIN";
```

### 3. null 체크 사용

```java
// 안전한 문자열 처리
String userName = Common.nullCheck(userInput, "Unknown");
String warehouse = Common.nullCheck(warehouseCode, "DEFAULT");
```

### 4. 검색 조건 설정

```java
// 검색 필터 설정
Common.selectDay = "2025-10-31";
Common.selectWarehouse = "WH001";
Common.searchType = "1";  // 검색 타입 변경
Common.search_bool = true;
```

### 5. 프린터 설정

```java
// 블루투스 프린터 설정
Common.printer_address = "00:11:22:33:44:55";
Common.printer_setting = true;
Common.print_bool = true;
```

---

## 주의사항

1. **URL 초기화**: 모든 URL 상수가 빈 문자열로 초기화되어 있으므로, 애플리케이션 시작 시 반드시 설정해야 합니다.

2. **전역 상태 관리**: static 변수로 선언되어 있어 애플리케이션 전체에서 공유되므로, 멀티스레드 환경에서 주의가 필요합니다.

3. **nullCheck 메서드**: "null" 문자열도 null로 처리하므로, 실제로 "null"이라는 문자열이 필요한 경우 주의해야 합니다.

4. **디버그 모드**: `Common.D` 플래그를 통해 로그 출력을 제어할 수 있습니다.

---

## 코드 구조

```
Common.java
├── 디버그 플래그 (D)
├── URL 상수 (21개)
│   ├── 기본 URL (VERSION, LOGIN)
│   ├── 출하 관련 (SHIPMENT)
│   ├── 생산 관련 (PRODUCTION)
│   ├── 바코드 관련 (BARCODE_INFO)
│   ├── 습중량 관련 (GOODS_WET)
│   └── 홈플러스 비정량 (HOMEPLUS_NONFIXED)
├── 전역 변수
│   ├── 사용자 정보 (REG_ID, USER_TYPE)
│   ├── 검색 설정 (search_bool, selectDay, selectWarehouse, searchType)
│   ├── 프린터 설정 (printer_address, printer_setting, print_bool)
│   └── 센터 정보 (list_center_info)
└── 유틸리티 메서드
    └── nullCheck(String, String)
```
