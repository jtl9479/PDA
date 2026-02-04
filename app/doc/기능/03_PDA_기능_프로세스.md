# Highland E-Mart PDA 기능 및 프로세스

**작성일**: 2026-01-09

---

## 1. 앱 구조 개요

```
LoginActivity (로그인)
    ↓
MainActivity (메인 메뉴)
    ├── ShipmentActivity (계근 입력)
    │       └── ExpiryEnterActivity (유통기한 입력)
    ├── ProductionActivity (생산 계근 계산)
    └── SettingActivity (설정)
```

---

## 2. Activity별 기능

| Activity                     | 파일 위치                                   | 역할                                  |
| ---------------------------- | --------------------------------------- | ----------------------------------- |
| **LoginActivity**            | `LoginActivity.java`                    | 로그인, DB 테이블 생성, 프린터 설정 로드, 센터 선택    |
| **MainActivity**             | `MainActivity.java`                     | 메인 메뉴, 날짜 선택, 출하대상 다운로드, 계근입력 시작    |
| **ShipmentActivity**         | `ShipmentActivity.java`                 | 바코드 스캔, 계근 데이터 입력, 라벨 인쇄, 서버 전송     |
| **ProductionActivity**       | `ProductionActivity.java`               | 생산 계근 계산 (바코드/수기 입력)                |
| **ExpiryEnterActivity**      | `ExpiryEnterActivity.java`              | 유통기한, 중량 수기 입력                      |
| **SettingActivity**          | `SettingActivity.java`                  | 프린터 ON/OFF, 계근내역 삭제                 |
| **DeviceListActivity**       | `print/DeviceListActivity.java`         | 블루투스 프린터 선택                         |
| **ScannerActivity**          | `scanner/ScannerActivity.java`          | PM80 바코드 스캐너 기본 Activity            |
| **HoneywellScannerActivity** | `scanner/HoneywellScannerActivity.java` | Honeywell EDA51 바코드 스캐너 기본 Activity |

---

## 3. 작업 유형 (searchType)

| searchType | 작업 유형 | 다운로드 버튼 | 계근입력 버튼 |
|------------|----------|--------------|--------------|
| **0** | 이마트 출하 | 출하대상받기 | 이마트 계근입력시작 |
| **1** | 생산 | 생산계근대상받기 | 생산계근입력시작 |
| **2** | 홈플러스 하이퍼 | 홈플러스출하대상받기 | 홈플러스 계근입력시작 |
| **3** | 도매업체 | 도매업체출하대상받기 | 도매업체 계근입력시작 |
| **4** | 비정량 출하 | 비정량출하대상받기 | 비정량 계근입력시작 |
| **5** | 홈플러스 비정량 | 홈플러스비정량출하대상받기 | 홈플러스비정량 계근입력시작 |
| **6** | 롯데 | 롯데출하대상받기 | 롯데 계근입력시작 |
| **7** | 생산(라벨) | 생산대상받기(라벨) | 생산입력시작(라벨) |

---

## 4. 주요 프로세스 흐름

### 4.1 로그인 프로세스

```
1. 앱 시작 → LoginActivity
2. SQLite 테이블 생성
   - TB_SHIPMENT (출하대상)
   - TB_BARCODE_INFO (바코드정보)
   - TB_GOODS_WET (계근내역)
   - TB_GOODS_WET_PRODUCTION_CALC (생산계근계산)
3. 프린터 설정 로드 (SharedPreferences)
4. ID/PWD 입력 + 센터 선택
   - 부산2
   - 이천1센터
   - 삼일냉장
   - SWC
   - 탑로지스
5. 서버 로그인 검증 (ProgressDlgLogin)
6. 성공 → MainActivity 이동
```

### 4.2 출하대상 다운로드 프로세스

```
1. MainActivity에서 날짜 선택 (btnDay)
2. 출하대상받기 버튼 클릭 (searchType 설정)
3. ProgressDlgShipSearch 실행 → 서버 HTTP 통신
4. JSON 응답 파싱 → Shipments_Info 객체 생성
5. DB에 저장 (TB_SHIPMENT)
6. 완료 Toast 표시
```

### 4.3 계근 입력 프로세스 (ShipmentActivity)

```
1. 계근입력시작 버튼 클릭
2. searchType 검증 + 리스트 존재 확인
3. ShipmentActivity 진입
4. 센터/업체/BL번호 선택 (스피너)
5. 바코드 스캔 (Honeywell EDA51)
   ├── 바코드 파싱 → 상품코드, 중량, 제조일자 추출
   └── DB 조회 → 출하대상 매칭
6. 계근 데이터 입력
   ├── 자동: 바코드에서 중량 추출
   └── 수동: ExpiryEnterActivity에서 직접 입력
7. 라벨 인쇄 (블루투스 프린터)
8. 계근 데이터 저장 (TB_GOODS_WET)
9. 서버 전송 (ProgressDlgShipmentSend)
```

### 4.4 생산 계근 계산 프로세스 (ProductionActivity)

```
1. 생산계근계산시작 버튼 클릭
2. ProductionActivity 진입
3. 작업 방식 선택 (바코드/수기)
4. 바코드 스캔 또는 수기 입력
5. 계근 계산 실행
6. 결과 저장/전송
```

---

## 5. 데이터 흐름

### 5.1 SQLite 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 리스트 |
| TB_PRODUCTION | 생산대상 리스트 |
| TB_BARCODE_INFO | 바코드 정보 |
| TB_GOODS_WET | 계근 내역 |
| TB_GOODS_WET_PRODUCTION_CALC | 생산 계근 계산 |
| TB_COMPLETE_ITEM | 완료 항목 |

### 5.2 서버 통신 (HTTP)

| AsyncTask 클래스 | 용도 |
|-----------------|------|
| ProgressDlgLogin | 로그인 인증 |
| ProgressDlgShipSearch | 출하/생산 대상 다운로드 |
| ProgressDlgShipSelect | 센터별 출하대상 조회 |
| ProgressDlgShipSelectBL | BL번호별 출하대상 조회 |
| ProgressDlgShipmentSend | 계근 데이터 서버 전송 |
| ProgressDlgBarcodeSearch | 바코드 정보 조회 |
| ProgressDlgNewBarcodeInfo | 신규 바코드 정보 등록 |
| ProgressDlgGoodsWetSearch | 계근 내역 조회 |

---

## 6. 바코드 스캔

### 6.1 지원 디바이스

| 디바이스 | Activity | 수신 방식 |
|----------|----------|-----------|
| Point Mobile PM80 | ScannerActivity | SDK + BroadcastReceiver (AndroidManifest 등록) |
| Honeywell EDA51 | HoneywellScannerActivity | Intent 직접 수신 (동적 등록) |

### 6.2 Honeywell EDA51 Intent 정보

```java
Action: "com.honeywell.scantointent.intent.action.BARCODE_DATA"
Extra:  "com.honeywell.scantointent.intent.extra.DATA"
```

### 6.3 바코드 처리 흐름

```
바코드 스캔
    ↓
BroadcastReceiver (m_brc)
    ↓
setMessage(barcode) 호출
    ↓
하위 Activity에서 오버라이드하여 처리
    ↓
바코드 파싱 (상품코드, 중량, 제조일자 추출)
    ↓
출하대상 매칭 → 계근 데이터 생성
```

### 6.4 상속 구조

```
AppCompatActivity
    └── HoneywellScannerActivity (바코드 수신 기능)
            ├── ShipmentActivity (계근 입력)
            └── ProductionActivity (생산 계근 계산)
```

---

## 7. 라벨 인쇄

### 7.1 프린터 유형

| 클래스 | 프린터 | 통신 방식 |
|--------|--------|-----------|
| BluetoothPrintService | 일반 블루투스 프린터 | Bluetooth SPP |
| BixolonSocketPrinter | 빅솔론 프린터 | Socket 통신 |

### 7.2 인쇄 프로세스

```
1. DeviceListActivity에서 프린터 선택
2. 블루투스 페어링/연결
3. 계근 완료 시 라벨 데이터 생성
4. 프린터로 전송
```

### 7.3 인쇄 ON/OFF 제어

- `Common.print_bool`: 인쇄 활성화 여부
- `Common.printer_setting`: 프린터 설정 여부
- `Common.printer_address`: 프린터 블루투스 주소

---

## 8. 설정 기능 (SettingActivity)

| 기능 | 설명 |
|------|------|
| 프린터 ON | 인쇄 기능 활성화 (`Common.printer_setting = true`) |
| 프린터 OFF | 인쇄 기능 비활성화 (`Common.printer_setting = false`) |
| 전체계근내역 삭제 | TB_GOODS_WET 테이블 초기화 (5회 클릭 필요) |

---

## 9. 주요 클래스 목록

### 9.1 Activity

| 클래스 | 파일 경로 |
|--------|-----------|
| LoginActivity | `com/rgbsolution/highland_emart/LoginActivity.java` |
| MainActivity | `com/rgbsolution/highland_emart/MainActivity.java` |
| ShipmentActivity | `com/rgbsolution/highland_emart/ShipmentActivity.java` |
| ProductionActivity | `com/rgbsolution/highland_emart/ProductionActivity.java` |
| ExpiryEnterActivity | `com/rgbsolution/highland_emart/ExpiryEnterActivity.java` |
| SettingActivity | `com/rgbsolution/highland_emart/SettingActivity.java` |
| DeviceListActivity | `com/rgbsolution/highland_emart/print/DeviceListActivity.java` |
| ScannerActivity | `com/rgbsolution/highland_emart/scanner/ScannerActivity.java` |
| HoneywellScannerActivity | `com/rgbsolution/highland_emart/scanner/HoneywellScannerActivity.java` |

### 9.2 데이터베이스

| 클래스 | 역할 |
|--------|------|
| DBHandler | DB CRUD 연산 |
| DBHelper | SQLite 연결 관리 |
| DBInfo | 테이블/컬럼 상수 정의 |

### 9.3 데이터 모델

| 클래스 | 역할 |
|--------|------|
| Shipments_Info | 출하대상 정보 |
| Goodswets_Info | 계근 내역 정보 |
| Barcodes_Info | 바코드 정보 |

### 9.4 어댑터

| 클래스 | 역할 |
|--------|------|
| ShipmentListAdapter | 출하대상 리스트 어댑터 |
| DetailAdapter | 상세 정보 어댑터 |
| UnknownAdapter | 미확인 항목 어댑터 |

### 9.5 통신/유틸리티

| 클래스 | 역할 |
|--------|------|
| HttpHelper | HTTP 통신 헬퍼 |
| Common | 전역 변수/상수 |
| Base64 | Base64 인코딩/디코딩 |

### 9.6 프린터

| 클래스 | 역할 |
|--------|------|
| BluetoothPrintService | 블루투스 프린터 서비스 |
| BixolonSocketPrinter | 빅솔론 프린터 (소켓) |

---

## 10. 화면 흐름 다이어그램

```
┌─────────────────┐
│  LoginActivity  │
│   (로그인)       │
└────────┬────────┘
         │ 로그인 성공
         ▼
┌─────────────────┐
│  MainActivity   │
│   (메인 메뉴)    │
└────────┬────────┘
         │
    ┌────┴────┬──────────────┬─────────────┐
    │         │              │             │
    ▼         ▼              ▼             ▼
┌────────┐ ┌────────┐ ┌────────────┐ ┌────────────┐
│다운로드 │ │계근입력 │ │생산계근계산│ │   설정     │
│(서버)   │ │시작    │ │  시작     │ │           │
└────────┘ └───┬────┘ └─────┬──────┘ └────────────┘
               │            │
               ▼            ▼
        ┌────────────┐ ┌────────────────┐
        │Shipment    │ │Production      │
        │Activity    │ │Activity        │
        │(계근 입력)  │ │(생산계근계산)  │
        └─────┬──────┘ └────────────────┘
              │
    ┌─────────┼─────────┐
    │         │         │
    ▼         ▼         ▼
┌────────┐ ┌────────┐ ┌────────┐
│바코드   │ │유통기한│ │라벨    │
│스캔    │ │입력    │ │인쇄    │
└────────┘ └────────┘ └────────┘
```

---

**최종 수정일**: 2026-01-09
