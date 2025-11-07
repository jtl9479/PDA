# Highland EMART PDA 시스템 문서

## 프로젝트 개요

Highland EMART PDA 시스템은 물류 창고에서 사용되는 Android 기반 휴대용 데이터 터미널(PDA) 애플리케이션입니다. 이 시스템은 출하 계근(무게 측정), 바코드 스캐닝, 블루투스 프린터 출력 등의 기능을 제공합니다.

### 주요 기능

- **출하 계근 관리**: 이마트, 홈플러스, 롯데, 생산, 도매 등 다양한 출하 타입 지원
- **바코드 스캐닝**: PM80 하드웨어 스캐너를 통한 바코드 인식 및 파싱
- **무게 측정**: LB → KG 자동 변환, 실시간 무게 데이터 처리
- **블루투스 프린터**: Woosim WSP-R240 열전사 프린터 연동
- **로컬 데이터베이스**: SQLite 기반 오프라인 데이터 관리
- **서버 연동**: HTTP/SOAP 기반 웹 서비스 통신

### 기술 스택

- **플랫폼**: Android
- **언어**: Java
- **데이터베이스**: SQLite (HIGHLAND DB v27)
- **하드웨어**: PM80 바코드 스캐너, Woosim WSP-R240 프린터
- **통신**: HTTP, Bluetooth SPP

---

## 문서 구조

전체 26개의 Java 클래스에 대한 분석 문서가 카테고리별로 정리되어 있습니다.

### 📱 Activities (화면 및 사용자 인터페이스)

| 문서 | 클래스명 | 라인 수 | 주요 기능 |
|------|----------|---------|-----------|
| [ShipmentActivity Part 1](./ShipmentActivity_Part1.md) | ShipmentActivity | 1-620 | 출하 계근 메인 화면 - 개요, 클래스 구조, 생명주기 |
| [ShipmentActivity Part 2](./ShipmentActivity_Part2.md) | ShipmentActivity | 621-1100 | 바코드 스캔 처리 로직, ITEM_TYPE 분기 |
| [ShipmentActivity Part 3](./ShipmentActivity_Part3.md) | ShipmentActivity | 1101-1700 | 데이터 삽입 및 프린터 출력 메서드 |
| [ShipmentActivity Part 4](./ShipmentActivity_Part4.md) | ShipmentActivity | 1701-4158 | 서버 전송, 예외 처리, UI 리스너 |
| [LoginActivity](./LoginActivity.md) | LoginActivity | 242 | 사용자 로그인, SHA-256 암호화, 점포 선택 |
| [MainActivity](./MainActivity.md) | MainActivity | 663 | 메인 메뉴, 8가지 작업 타입 선택 |
| [ProductionActivity](./ProductionActivity.md) | ProductionActivity | 466 | 생산 계근 처리, 무게 계산 |
| [SettingActivity](./SettingActivity.md) | SettingActivity | 136 | 프린터 설정, 숨김 삭제 기능 |
| [ExpiryEnterActivity](./ExpiryEnterActivity.md) | ExpiryEnterActivity | 114 | 유통기한 입력 다이얼로그 |
| [ScannerActivity](./ScannerActivity.md) | ScannerActivity | 207 | PM80 스캐너 기본 클래스 |
| [DeviceListActivity](./DeviceListActivity.md) | DeviceListActivity | 179 | 블루투스 장치 선택 |

**총 7개 Activity 클래스** (ShipmentActivity는 4개 파트로 분할)

---

### 🗄️ Database (데이터베이스)

| 문서 | 클래스명 | 라인 수 | 주요 기능 |
|------|----------|---------|-----------|
| [DBInfo](./DBInfo.md) | DBInfo | 126 | 데이터베이스 스키마 상수 정의 (6개 테이블) |
| [DBHelper](./DBHelper.md) | DBHelper | 89 | SQLiteOpenHelper 구현, DB 생성/업그레이드 |
| [DBHandler](./DBHandler.md) | DBHandler | 2101 | CRUD 메서드, 35개 데이터 처리 메서드 |

**테이블 구조**:
- `TB_SHIPMENT`: 출하 정보 (14개 메서드)
- `TB_BARCODE_INFO`: 바코드 파싱 규칙 (7개 메서드)
- `TB_GOODS_WET`: 계근 데이터 (14개 메서드)
- `TB_SHOP_CODE`: 점포 코드
- `TB_PROD_CODE`: 제품 코드
- `TB_PROD_CODE_WET`: 무게 제품 코드

---

### 📦 Items/Models (데이터 모델)

| 문서 | 클래스명 | 필드 수 | 주요 용도 |
|------|----------|---------|-----------|
| [Shipments_Info](./Shipments_Info.md) | Shipments_Info | 54 | 출하 정보 데이터 모델 |
| [Barcodes_Info](./Barcodes_Info.md) | Barcodes_Info | 30 | 바코드 파싱 규칙 모델 |
| [Goodswets_Info](./Goodswets_Info.md) | Goodswets_Info | 25 | 계근 데이터 모델 |

**특징**:
- Parcelable 인터페이스 구현 (Intent 간 데이터 전달)
- Getter/Setter 메서드 제공
- 서버 응답 XML 파싱 데이터 저장

---

### 🔄 Adapters (RecyclerView 어댑터)

| 문서 | 클래스명 | 라인 수 | 주요 기능 |
|------|----------|---------|-----------|
| [DetailAdapter](./DetailAdapter.md) | DetailAdapter | 200 | 계근 상세 목록, 다중 선택, 재출력 기능 |
| [ShipmentListAdapter](./ShipmentListAdapter.md) | ShipmentListAdapter | 172 | 출하 목록, 단일 선택 |
| [UnknownAdapter](./UnknownAdapter.md) | UnknownAdapter | 93 | 미등록 제품 코드/명 목록 |

**공통 패턴**:
- BaseAdapter 상속
- ViewHolder 패턴 (성능 최적화)
- 커스텀 레이아웃 인플레이션

---

### 🔧 Common/Utility (공통 유틸리티)

| 문서 | 클래스명 | 라인 수 | 주요 기능 |
|------|----------|---------|-----------|
| [Common](./Common.md) | Common | 81 | 전역 상수, URL 정의, 공통 변수 |
| [HttpHelper](./HttpHelper.md) | HttpHelper | 130 | HTTP POST 통신, EUC-KR 인코딩 |
| [Base64](./Base64.md) | Base64 | 124 | Base64 인코딩/디코딩 유틸리티 |
| [ProgressDlgShipSearch](./ProgressDlgShipSearch.md) | ProgressDlgShipSearch | 322 | 출하 데이터 다운로드 AsyncTask |
| [ProgressDlgGoodsWetSearch](./ProgressDlgGoodsWetSearch.md) | ProgressDlgGoodsWetSearch | 188 | 계근 데이터 조회 AsyncTask |
| [ProgressDlgBarcodeSearch](./ProgressDlgBarcodeSearch.md) | ProgressDlgBarcodeSearch | 201 | 바코드 정보 조회 AsyncTask |
| [ProgressDlgNewBarcodeInfo](./ProgressDlgNewBarcodeInfo.md) | ProgressDlgNewBarcodeInfo | 108 | 신규 바코드 등록 AsyncTask |

**AsyncTask 패턴**:
- `doInBackground()`: HTTP 통신 및 XML 파싱
- `onPostExecute()`: UI 업데이트 및 DB 저장
- 프로그레스 다이얼로그 표시

---

### 🖨️ Print/Scanner (하드웨어 통신)

| 문서 | 클래스명 | 라인 수 | 주요 기능 |
|------|----------|---------|-----------|
| [BluetoothPrintService](./BluetoothPrintService.md) | BluetoothPrintService | 316 | 블루투스 프린터 SPP 통신 서비스 |
| [Constants](./Constants.md) | Constants | 47 | PM80 스캐너 Intent 액션 상수 |

**프린터 통신 흐름**:
```
BluetoothDevice 선택 → connect() → ConnectThread → ConnectedThread → write() → 프린터 출력
```

**스캐너 통신 흐름**:
```
BroadcastReceiver 등록 → INTENT_ACTION_SCAN → 바코드 데이터 수신 → setBarcodeMsg() 처리
```

---

## 주요 데이터 흐름

### 1. 출하 계근 프로세스

```
LoginActivity (로그인)
  ↓
MainActivity (작업 타입 선택)
  ↓
ShipmentActivity (출하 정보 조회)
  ↓
ProgressDlgShipSearch (서버에서 출하 데이터 다운로드)
  ↓
DBHandler.insertShipmentInfo() (로컬 DB 저장)
  ↓
바코드 스캔 → setBarcodeMsg() (무게 추출, LB→KG 변환)
  ↓
wet_data_insert() (계근 데이터 DB 저장)
  ↓
setPrinting() (프린터 출력)
  ↓
ProgressDlgShipmentSend (서버 전송)
```

### 2. 바코드 처리 로직

```
PM80 스캐너 (하드웨어)
  ↓
BroadcastReceiver (INTENT_ACTION_SCAN)
  ↓
setBarcodeMsg(String msg)
  ↓
ITEM_TYPE 분기 (W/S/J/B/HW)
  ↓
무게 추출 (substring 파싱)
  ↓
단위 변환 (LB → KG: × 0.453592)
  ↓
Goodswets_Info 객체 생성
  ↓
DB 저장 및 UI 업데이트
```

### 3. 프린터 출력 데이터

**이마트 바코드 포맷 (M0-M9, E0-E3)**:
- M0: 표준 바코드 (21자리)
- M3: 유통기한 포함 (24자리)
- E0: 수출용 바코드 (29자리)

**출력 내용**:
- 제품명, 제품코드
- 실중량 (KG)
- 포장일자, 유통기한
- 점포코드, 출하번호

---

## 개발 환경 설정

### 1. Android Studio 프로젝트 열기
```bash
File → Open → D:\PDA\PDA-INNO
```

### 2. Gradle 빌드
```bash
./gradlew clean build
```

### 3. PM80 스캐너 권한 설정
`AndroidManifest.xml`에 다음 권한 추가:
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.INTERNET" />
```

### 4. 서버 URL 설정
`Common.java` 파일에서 서버 주소 수정:
```java
public static String URL_IP = "http://your-server-ip:port";
```

---

## 코드 스타일 가이드

### 네이밍 규칙
- **Activity**: PascalCase (예: `ShipmentActivity`)
- **변수**: snake_case (예: `item_weight`, `searchType`)
- **상수**: UPPER_SNAKE_CASE (예: `STATE_CONNECTED`, `URL_IP`)
- **메서드**: camelCase (예: `setBarcodeMsg`, `wet_data_insert`)

### 주요 코딩 패턴
- AsyncTask: 백그라운드 HTTP 통신
- BroadcastReceiver: 하드웨어 이벤트 수신
- SQLite CRUD: DBHandler 클래스 통한 데이터 처리
- Parcelable: Intent 간 객체 전달

---

## 문제 해결 (Troubleshooting)

### 1. 프린터 연결 실패
- **증상**: "프린터가 연결되지 않았습니다" 토스트 메시지
- **해결**:
  1. SettingActivity에서 프린터 설정 확인
  2. 블루투스 페어링 재설정
  3. `BluetoothPrintService.STATE_CONNECTED` 상태 확인

### 2. 바코드 파싱 오류
- **증상**: 바코드 스캔 후 "바코드정보 등록이 필요합니다" 메시지
- **해결**:
  1. `TB_BARCODE_INFO` 테이블에 해당 `ITEM_TYPE` 등록 확인
  2. `ProgressDlgBarcodeSearch`로 서버에서 바코드 정보 다운로드
  3. 바코드 길이 및 파싱 규칙 검증

### 3. 서버 통신 타임아웃
- **증상**: "서버 연결에 실패하였습니다" 다이얼로그
- **해결**:
  1. 네트워크 연결 상태 확인
  2. `Common.URL_IP` 서버 주소 검증
  3. 방화벽 및 포트 오픈 확인

---

## 버전 관리

### 현재 버전
- **App Version**: 확인 필요 (build.gradle 참조)
- **DB Version**: 27 (DBHelper.java:15)
- **Min SDK**: 확인 필요
- **Target SDK**: 확인 필요

### Git 브랜치 전략
```
main: 프로덕션 안정 버전
(현재 커밋: 25372a6 - "no message")
```

---

## 라이선스 및 연락처

이 문서는 Highland EMART PDA 시스템의 코드 분석을 위해 작성되었습니다.

---

## 문서 업데이트 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|-----------|
| 2025-10-31 | 1.0 | 전체 26개 클래스 분석 문서 초안 작성 |

---

**문서 작성 완료**: 30개 문서 파일 (26개 Java 클래스 + doc.md + README.md)
