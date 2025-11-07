# Highland EMART PDA 프로젝트 문서

## 목차
1. [프로젝트 개요](#프로젝트-개요)
2. [전체 프로젝트 구조](#전체-프로젝트-구조)
3. [데이터베이스 스키마](#데이터베이스-스키마)
4. [비즈니스 로직](#비즈니스-로직)
5. [주요 기능](#주요-기능)
6. [프린터 정보](#프린터-정보)

---

## 프로젝트 개요

**프로젝트명**: Highland EMART PDA
**패키지명**: `com.rgbsolution.highland_emart`
**플랫폼**: Android
**목적**: 물류 및 재고 관리를 위한 PDA(휴대용 단말기) 애플리케이션

### 주요 특징
- PDA 기기 특화 (바코드 스캔, 블루투스 프린터 연동)
- 서버 통신 (HTTP/SOAP 기반 웹 서비스)
- 로컬 SQLite 데이터베이스 (오프라인 지원)
- 다중 창고 지원 (5개 센터)
- 다양한 출하 유형 (이마트, 홈플러스, 롯데, 도매 등)

---

## 전체 프로젝트 구조

### 1. 디렉토리 구조

```
D:\PDA\PDA-INNO/
├── app/                                # 주 애플리케이션 모듈
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/                   # Java 소스코드
│   │   │   │   └── com/rgbsolution/highland_emart/
│   │   │   │       ├── (루트)          # 주요 Activity
│   │   │   │       ├── scanner/        # 바코드 스캐너
│   │   │   │       ├── print/          # 블루투스 프린터
│   │   │   │       ├── db/             # 데이터베이스
│   │   │   │       ├── common/         # 공통 유틸리티
│   │   │   │       ├── adapter/        # RecyclerView 어댑터
│   │   │   │       └── items/          # 데이터 모델
│   │   │   ├── res/                    # 리소스 파일
│   │   │   └── AndroidManifest.xml     # 앱 설정
│   ├── libs/                           # 외부 라이브러리
│   │   ├── WoosimLib240.jar           # Woosim 프린터
│   │   ├── ksoap2-3.0.0.jar           # SOAP 통신
│   │   └── device.sdk.jar             # 바코드 스캐너
│   └── build.gradle
├── gradle/
└── settings.gradle
```

### 2. 패키지 구조

#### 루트 패키지 (주요 Activity)
- `LoginActivity.java` - 로그인 화면
- `MainActivity.java` - 메인 메뉴
- `ShipmentActivity.java` - 출하 계근 작업 (핵심)
- `ProductionActivity.java` - 생산 계근 계산
- `SettingActivity.java` - 프린터 설정

#### scanner/ (바코드 스캐너)
- `ScannerActivity.java` - 스캐너 기본 기능
- `Constants.java` - 스캐너 상수

#### print/ (블루투스 프린터)
- `BluetoothPrintService.java` - 블루투스 프린터 서비스
- `DeviceListActivity.java` - 디바이스 선택

#### db/ (데이터베이스)
- `DBInfo.java` - 테이블 스키마 정의
- `DBHelper.java` - SQLite 핸들러
- `DBHandler.java` - CRUD 메서드

#### common/ (공통 유틸리티)
- `Common.java` - 전역 상수
- `HttpHelper.java` - HTTP 통신
- `ProgressDlgShipSearch.java` - 출하 대상 조회
- `ProgressDlgGoodsWetSearch.java` - 계근 데이터 조회

---

## 데이터베이스 스키마

### DB 정보
- **DB 이름**: HIGHLAND
- **DB 버전**: 27
- **DB 엔진**: SQLite

### 주요 테이블

#### TB_SHIPMENT (출하 대상)
서버에서 다운로드한 출하 대상 정보를 저장합니다.

**주요 컬럼**:
- SHIPMENT_ID: 로컬 ID (자동증가)
- GI_H_ID: 출고번호 (헤더)
- GI_D_ID: 출고번호 (상세)
- ITEM_CODE: 상품코드
- ITEM_NAME: 상품명
- EMARTITEM_CODE: 이마트 상품코드
- PACKER_PRODUCT_CODE: 패커 상품코드
- GI_REQ_QTY: 출하요청중량 (KG)
- GI_QTY: 계근 중량 (누적)
- PACKING_QTY: 계근 수량 (박스 수)
- SAVE_TYPE: 저장 여부 (Y/N)

#### TB_BARCODE_INFO (바코드 정보)
바코드 파싱 규칙을 저장합니다.

**주요 컬럼**:
- PACKER_PRODUCT_CODE: 패커 상품코드
- WEIGHT_FROM/TO: 중량 시작/끝 위치
- MAKINGDATE_FROM/TO: 제조일 시작/끝 위치
- BOXSERIAL_FROM/TO: 박스번호 시작/끝 위치
- ZEROPOINT: 소수점 자리수
- BASEUNIT: 단위 (LB/KG)

**바코드 파싱 예시**:
```
바코드: 12345678901234567890
- WEIGHT_FROM=11, TO=15     → "12345" → 123.45 (ZEROPOINT=2)
- MAKINGDATE_FROM=16, TO=21 → "250115" (2025-01-15)
- BOXSERIAL_FROM=22, TO=25  → "0001"
```

#### TB_GOODS_WET (계근 기록)
실제 계근(무게측정) 데이터를 저장합니다.

**주요 컬럼**:
- GOODS_WET_ID: 로컬 ID
- GI_D_ID: 출고번호 (TB_SHIPMENT 연결)
- WEIGHT: 중량 (소수점 2자리)
- BARCODE: 스캔한 바코드 원본
- MAKINGDATE: 제조일
- BOXSERIAL: 박스번호
- BOX_CNT: 계근 순서번호
- SAVE_TYPE: 전송 여부 (Y/N)

---

## 비즈니스 로직

### 1. 바코드 스캔 처리

#### 처리 흐름
```
PM80 하드웨어 스캐너
    ↓
ScanManager.initScanner()
    ↓
BroadcastReceiver로 스캔 결과 수신
    ↓
ShipmentActivity.setBarcodeMsg()
    ↓
바코드 정보 추출 및 처리
```

#### 바코드 정보 추출

**중량 추출 로직** (ShipmentActivity.java 라인 905-1030):
```java
// 1. 바코드에서 중량 문자열 추출
item_weight = barcode.substring(
    Integer.parseInt(weight_from) - 1,
    Integer.parseInt(weight_to)
);  // 예: "12345"

// 2. 소수점 적용
double item_pow = Math.pow(10, Integer.parseInt(zeroPoint));
item_weight_double = Double.parseDouble(item_weight) / item_pow;
// 예: 12345 / 100 = 123.45

// 3. LB → KG 변환
if ("LB".equals(baseUnit)) {
    item_weight_double = item_weight_double * 0.453592;
}
```

**제조일 추출**:
```java
item_making_date = barcode.substring(
    Integer.parseInt(makingDate_from) - 1,
    Integer.parseInt(makingDate_to)
);  // 예: "250115"
```

**박스번호 추출**:
```java
item_box_serial = barcode.substring(
    Integer.parseInt(boxSerial_from) - 1,
    Integer.parseInt(boxSerial_to)
);  // 예: "0001"
```

### 2. 출하 관리

#### 출하 대상 다운로드
1. 서버에서 출하 대상 조회 (날짜, 창고코드 기준)
2. 응답 데이터 파싱 (구분자: `;;`와 `::`)
3. 로컬 DB에 저장

#### 계근 처리 (wet_data_insert 메서드)
1. 출하 대상 선택
2. 바코드 스캔 또는 수기 입력
3. 중량 추출/입력
4. Goodswets_Info 객체 생성
5. 로컬 DB에 저장 (TB_GOODS_WET)
6. 누적 중량 및 박스 수 업데이트
7. 라벨 프린트 (선택)
8. 서버 전송

#### 서버 전송
1. 미전송 데이터 조회 (SAVE_TYPE='N')
2. HTTP POST로 서버에 전송
3. 전송 성공 시 SAVE_TYPE='Y'로 업데이트

### 3. 서버 통신

#### HTTP 통신 구조 (HttpHelper.java)
- **방식**: POST
- **Content-Type**: application/x-www-form-urlencoded
- **인코딩**: EUC-KR
- **파라미터**: data (쿼리문), dbid (DB ID)

#### 주요 API 엔드포인트
- URL_SEARCH_SHIPMENT: 출하 대상 조회
- URL_SEARCH_BARCODE_INFO: 바코드 정보 조회
- URL_INSERT_GOODS_WET: 계근 데이터 저장
- URL_UPDATE_SHIPMENT: 출하 완료 처리

---

## 주요 기능

### 1. 로그인 (LoginActivity)
- 창고 선택 (5개 센터: 부산, 이천1, 이천2, 삼일냉장, 영광냉장)
- 사용자 ID/PW 입력
- 서버 인증

### 2. 메인 메뉴 (MainActivity)
**작업 유형 선택** (6가지):
1. 이마트 출하 대상 다운로드
2. 이마트 생산 대상 다운로드
3. 홈플러스 출하
4. 롯데 출하
5. 도매 출하
6. 생산 계근 계산

**기타 기능**:
- 작업 날짜 선택 (DatePicker)
- 프린터 설정 이동

### 3. 출하 계근 (ShipmentActivity)
**핵심 기능**:
1. 출하 대상 목록 조회
2. 출하 대상 선택
3. 바코드 스캔 (PM80 스캐너)
4. 중량 자동 추출 또는 수기 입력
5. 계근 데이터 저장
6. 라벨 프린트 (Woosim 프린터)
7. 서버 전송

**주요 메서드**:
- `setBarcodeMsg()` - 바코드 처리 (라인 742-1100)
- `wet_data_insert()` - 계근 데이터 저장 (라인 1102-1252)
- `find_work_info()` - 바코드 검증 (라인 1327-1410)
- `setPrinting()` - 이마트 라벨 출력 (라인 1682+)
- `setHomeplusPrinting()` - 홈플러스 라벨 출력
- `setPrintingLotte()` - 롯데 라벨 출력
- `setPrinting_prod()` - 생산 라벨 출력 (라인 1585-1680)

### 4. 생산 계근 계산 (ProductionActivity)
1. 패커 코드 + PP 코드 입력
2. 바코드 스캔 또는 수기 입력
3. 중량 자동 계산
4. 박스 개수 누적
5. 총 중량 누적

### 5. 프린터 설정 (SettingActivity)
1. 블루투스 프린터 목록 조회
2. 프린터 선택 및 연결
3. 연결 테스트
4. 설정 저장 (SharedPreferences)

---

## 프린터 정보

### 사용 프린터
**Woosim WSP-R240** 모바일 블루투스 프린터 ✅

**특징**:
- 블루투스 SPP 연결
- 감열식 라벨 프린터
- 한글 폰트 지원 (HYWULM.TTF)
- 라벨 크기: 576 × 460 픽셀 (약 72mm 폭)

### 프린터 라이브러리
**파일**: `app/libs/WoosimLib240.jar`

**주요 클래스**:
- `WoosimService` - Woosim 프린터 서비스
- `WoosimCmd` - Woosim 명령어
- `WoosimBarcode` - 바코드 생성

**참고**: Zebra, Brother, SATO 등 다른 프린터는 지원하지 않습니다.

### 프린팅 명령어 예시

**생산 라벨 출력 (setPrinting_prod 메서드)**:
```java
ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

// 프린터 초기화
byteStream.write(WoosimCmd.initPrinter());
byteStream.write(WoosimCmd.setPageMode());
byteStream.write(WoosimCmd.selectTTF("HYWULM.TTF"));  // 한글 폰트

// 상품명 출력
byteStream.write(WoosimCmd.PM_setPosition(50, 120));
byteStream.write(WoosimCmd.getTTFcode(40, 40, 상품명));

// 바코드 출력 (CODE128)
byte[] barcode = WoosimBarcode.createBarcode(
    WoosimBarcode.CODE128, 2, 60, false, 바코드데이터
);
byteStream.write(WoosimCmd.PM_setPosition(50, 190));
byteStream.write(barcode);

// 중량 출력
byteStream.write(WoosimCmd.PM_setPosition(50, 340));
byteStream.write(WoosimCmd.getTTFcode(40, 40, "중량: " + weight + " KG"));

// 출력 실행
byteStream.write(WoosimCmd.PM_setArea(0, 0, 576, 460));
byteStream.write(WoosimCmd.PM_printData());
byteStream.write(WoosimCmd.PM_setStdMode());

// 블루투스로 전송
sendData(byteStream.toByteArray());
sendData(WoosimCmd.feedToMark());  // 용지 피드
```

### 라벨 종류

| 라벨 유형 | 메서드 | 출력 정보 |
|---------|--------|---------|
| **이마트 출하** | `setPrinting()` | 상품명, 바코드(M0/M1/M3/M4/E0/E1), 중량, 브랜드명, 출고업체, 원산지, 유통기한 |
| **홈플러스 출하** | `setHomeplusPrinting()` | 상품명, 바코드, 중량, 납품코드, 납품명 |
| **롯데 출하** | `setPrintingLotte()` | 상품명, 바코드, 중량, 박스순서(BOX_ORDER) |
| **생산 계근** | `setPrinting_prod()` | 상품명, 바코드(상품코드+중량+타임스탬프), 중량 |

### 바코드 형식

#### 이마트 바코드 (M0 타입)
```
상품코드(6) + 중량(6) + 회사코드(2) + 수입식별번호(12)
예: 123456 + 001234 + 01 + 202501150001
```

#### 생산 바코드
```
상품코드 + 중량(6) + "00" + 타임스탬프(14)
예: ITEM001 + 012345 + 00 + 25011512345678
```

### 블루투스 연결

**BluetoothPrintService.java**:
- UUID: `00001101-0000-1000-8000-00805F9B34FB` (SPP)
- RFCOMM 소켓 사용
- 자동 재연결 지원

**연결 흐름**:
1. SettingActivity에서 프린터 선택
2. BluetoothAdapter로 페어링된 장치 목록 조회
3. BluetoothPrintService로 연결
4. WoosimService로 응답 처리
5. 연결 상태를 SharedPreferences에 저장

---

## 개발 환경

### 빌드 도구
- Gradle 4.10.1
- Android Gradle Plugin 3.3.1

### SDK
- compileSdkVersion: 25 (바코드 리더기 호환성)
- minSdkVersion: 14
- targetSdkVersion: 미설정

### 외부 라이브러리

| 라이브러리 | 파일 | 용도 |
|----------|------|------|
| **Woosim** | WoosimLib240.jar | 프린터 제어 |
| **KSOAP2** | ksoap2-3.0.0.jar | SOAP 웹 서비스 통신 |
| **Device SDK** | device.sdk.jar | PM80 바코드 스캐너 |
| **AppCompat** | appcompat-v7:25+ | Android 호환성 |

### 하드웨어 요구사항
- **PDA 기기**: PM80 또는 호환 모델
- **바코드 스캐너**: PM80 내장 스캐너
- **블루투스 프린터**: Woosim WSP-R240

---

## 주요 클래스 참조

### Activity
| 클래스 | 파일 위치 | 주요 라인 |
|--------|----------|----------|
| LoginActivity | LoginActivity.java | 전체 |
| MainActivity | MainActivity.java | 전체 |
| ShipmentActivity | ShipmentActivity.java | 742-1100 (바코드)<br>1102-1252 (계근)<br>1585-1680 (프린트) |
| ProductionActivity | ProductionActivity.java | 299-392 |
| SettingActivity | SettingActivity.java | 전체 |

### Database
| 클래스 | 파일 위치 | 주요 라인 |
|--------|----------|----------|
| DBInfo | db/DBInfo.java | 전체 (컬럼 상수) |
| DBHelper | db/DBHelper.java | 전체 (DB 핸들러) |
| DBHandler | db/DBHandler.java | CRUD 메서드 |

### Communication
| 클래스 | 파일 위치 | 역할 |
|--------|----------|------|
| HttpHelper | common/HttpHelper.java | HTTP 통신 |
| ProgressDlgShipSearch | common/ProgressDlgShipSearch.java | 출하 대상 조회 |

### Scanner & Print
| 클래스 | 파일 위치 | 역할 |
|--------|----------|------|
| ScannerActivity | scanner/ScannerActivity.java | 바코드 스캔 |
| BluetoothPrintService | print/BluetoothPrintService.java | 블루투스 통신 |
| DeviceListActivity | print/DeviceListActivity.java | 프린터 선택 |

---

---

## ShipmentActivity 상세 분석

출하/생산 계근(무게 측정) 작업의 핵심 Activity에 대한 상세 분석은 별도 문서로 분리되었습니다.

📁 **분석 문서**:
- [ShipmentActivity Part 1 - 개요 및 클래스 구조](ShipmentActivity_Part1.md)
- [ShipmentActivity Part 2 - 바코드 스캔 처리](ShipmentActivity_Part2.md)
- [ShipmentActivity Part 3 - 계근 데이터 처리 및 프린터 출력](ShipmentActivity_Part3.md)
- [ShipmentActivity Part 4 - 서버 전송 및 예외 처리](ShipmentActivity_Part4.md)

---

**문서 작성일**: 2025-01-27
**문서 버전**: 1.1
**앱 버전**: 1.00 (versionCode: 4)
