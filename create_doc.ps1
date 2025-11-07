$utf8WithBom = New-Object System.Text.UTF8Encoding $true
$content = @"
# Highland EMART PDA 프로젝트 문서

## 목차  
1. [프로젝트 개요](#프로젝트-개요)
2. [전체 프로젝트 구조](#전체-프로젝트-구조)
3. [데이터베이스 스키마](#데이터베이스-스키마)
4. [비즈니스 로직](#비즈니스-로직)
5. [주요 기능](#주요-기능)

---

## 프로젝트 개요

**프로젝트명**: Highland EMART PDA
**패키지명**: ``com.rgbsolution.highland_emart``
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

``````
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
│   ├── build.gradle                    # 앱 빌드 설정
│   └── proguard-rules.pro
├── gradle/
├── build.gradle
└── settings.gradle
``````

### 2. 패키지 구조 및 역할

#### 루트 패키지 (주요 Activity)
- ``LoginActivity.java`` - 로그인 화면 및 사용자 인증
- ``MainActivity.java`` - 메인 메뉴 (작업 유형 선택)
- ``ShipmentActivity.java`` - 출하 계근 작업 (핵심 비즈니스 로직)
- ``ProductionActivity.java`` - 생산 계근 계산
- ``ExpiryEnterActivity.java`` - 유통기한 입력
- ``SettingActivity.java`` - 프린터 설정

#### scanner/ (바코드 스캐너)
- ``ScannerActivity.java`` - 스캐너 기본 기능
- ``Constants.java`` - 스캐너 상수 정의

#### print/ (블루투스 프린터)
- ``BluetoothPrintService.java`` - 블루투스 프린터 서비스
- ``DeviceListActivity.java`` - 블루투스 디바이스 선택

#### db/ (데이터베이스)
- ``DBInfo.java`` - DB 테이블 스키마 정의
- ``DBHelper.java`` - SQLite 데이터베이스 핸들러
- ``DBHandler.java`` - DB CRUD 메서드 모음

---

## 데이터베이스 스키마

### 주요 테이블

#### TB_SHIPMENT (출하 대상)
출하 대상 정보를 저장하는 메인 테이블입니다.

**주요 컬럼**:
- SHIPMENT_ID: 로컬 ID (자동증가)
- GI_H_ID: 출고번호 (헤더)
- GI_D_ID: 출고번호 (상세)
- ITEM_CODE: 상품코드
- ITEM_NAME: 상품명
- PACKER_PRODUCT_CODE: 패커 상품코드
- GI_REQ_QTY: 출하요청중량 (KG)
- GI_QTY: 계근 중량 (누적)
- PACKING_QTY: 계근 수량 (누적 박스 수)
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
``````
PM80 하드웨어 스캐너
    ↓
ScanManager.initScanner()
    ↓
BroadcastReceiver로 스캔 결과 수신
    ↓
ShipmentActivity.setBarcodeMsg()
    ↓
바코드 정보 추출 및 처리
``````

#### 바코드에서 정보 추출
1. **중량 추출**
   - 바코드에서 WEIGHT_FROM ~ WEIGHT_TO 위치의 문자열 추출
   - ZEROPOINT 적용 (예: 12345 → 123.45)
   - LB인 경우 KG로 변환 (× 0.453592)

2. **제조일 추출**
   - MAKINGDATE_FROM ~ MAKINGDATE_TO 위치에서 추출

3. **박스번호 추출**
   - BOXSERIAL_FROM ~ BOXSERIAL_TO 위치에서 추출

### 2. 출하 관리

#### 출하 대상 다운로드
- 서버에서 출하 대상 조회 (날짜, 창고코드 기준)
- 응답 데이터 파싱 (구분자: ;;와 ::)
- 로컬 DB에 저장

#### 계근 처리
1. 출하 대상 선택
2. 바코드 스캔 또는 수기 입력
3. 중량 추출/입력
4. Goodswets_Info 객체 생성
5. 로컬 DB에 저장
6. 누적 중량 및 박스 수 업데이트

#### 라벨 프린트
- 이마트, 홈플러스, 롯데 각각 다른 포맷
- 블루투스 프린터로 출력

#### 서버 전송
- 미전송 데이터 조회 (SAVE_TYPE='N')
- HTTP POST로 서버에 전송
- 전송 성공 시 SAVE_TYPE='Y'로 업데이트

### 3. 서버 통신

#### HTTP 통신 구조
- POST 방식
- Content-Type: application/x-www-form-urlencoded
- 인코딩: EUC-KR
- 파라미터: data (쿼리문), dbid (DB ID)

#### 주요 API 엔드포인트
- URL_SEARCH_SHIPMENT: 출하 대상 조회
- URL_SEARCH_BARCODE_INFO: 바코드 정보 조회
- URL_INSERT_GOODS_WET: 계근 데이터 저장
- URL_UPDATE_SHIPMENT: 출하 완료 처리

---

## 주요 기능

### 1. 로그인 (LoginActivity)
- 창고 선택 (5개 센터)
- 사용자 ID/PW 입력
- 서버 인증

### 2. 메인 메뉴 (MainActivity)
- 작업 유형 선택 (이마트 출하/생산, 홈플러스, 롯데, 도매, 생산계근)
- 작업 날짜 선택
- 프린터 설정

### 3. 출하 계근 (ShipmentActivity)
- 출하 대상 목록 조회
- 바코드 스캔 및 정보 추출
- 중량 자동 추출 또는 수기 입력
- 계근 데이터 저장
- 라벨 프린트
- 서버 전송

### 4. 생산 계근 계산 (ProductionActivity)
- 패커 코드 + PP 코드 입력
- 바코드 스캔 또는 수기 입력
- 중량 자동 계산
- 박스 개수 및 총 중량 누적

### 5. 프린터 설정 (SettingActivity)
- 블루투스 프린터 목록 조회
- 프린터 선택 및 연결
- 설정 저장

---

## 주요 클래스 참조

### Activity
- ``LoginActivity.java`` - 로그인
- ``MainActivity.java`` - 메인 메뉴
- ``ShipmentActivity.java`` - 출하 계근 (핵심)
- ``ProductionActivity.java`` - 생산 계근
- ``SettingActivity.java`` - 프린터 설정

### Database
- ``DBInfo.java`` - 스키마 정의
- ``DBHelper.java`` - DB 핸들러
- ``DBHandler.java`` - CRUD 메서드

### Communication
- ``HttpHelper.java`` - HTTP 통신
- ``ProgressDlgShipSearch.java`` - 출하 대상 조회
- ``ProgressDlgGoodsWetSearch.java`` - 계근 데이터 조회

### Scanner & Print
- ``ScannerActivity.java`` - 바코드 스캔
- ``BluetoothPrintService.java`` - 프린터 서비스

---

## 개발 환경

- **플랫폼**: Android
- **SDK**: compileSdkVersion 25, minSdkVersion 14
- **빌드 도구**: Gradle 4.10.1, Android Gradle Plugin 3.3.1
- **외부 라이브러리**:
  - ksoap2-3.0.0.jar (SOAP 통신)
  - WoosimLib240.jar (프린터)
  - device.sdk.jar (바코드 스캐너)

---

**문서 작성일**: 2025-01-27
**문서 버전**: 1.0
**앱 버전**: 1.00 (versionCode: 4)
"@

[System.IO.File]::WriteAllLines("D:\PDA\PDA-INNO\doc.md", $content, $utf8WithBom)
Write-Host "doc.md 파일이 UTF-8 BOM 인코딩으로 생성되었습니다."
