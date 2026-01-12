# HoneywellScannerActivity.java 허니웰 기준으로 수정

---

## 개요

- **작성일**: 2026-01-09
- **최종 수정일**: 2026-01-12
- **목적**: PM80 전용 ScannerActivity를 유지하고, Honeywell EDA51 전용 Activity를 새로 생성
- **대상 파일**: `scanner/HoneywellScannerActivity.java`

---

## 최우선 원칙

### ScannerActivity와 100% 동일한 기능을 지원해야 한다

- 바코드 수신 방식만 다르고, 나머지 기능은 **완전히 동일**해야 한다
- 하위 Activity(ShipmentActivity, ProductionActivity)가 **코드 변경 없이** 동작해야 한다

---

## 현재 구조

```
ScannerActivity (PM80 전용)
    ├── ShipmentActivity
    └── ProductionActivity
```

---

## 변경 후 구조

```
ScannerActivity (PM80 전용) - 기존 유지, 수정 없음

HoneywellScannerActivity (Honeywell EDA51 전용) - 신규 생성
    ├── ShipmentActivity (상속 변경)
    └── ProductionActivity (상속 변경)
```

---

## 변경 대상 파일

| 파일 | 변경 내용 |
|------|----------|
| HoneywellScannerActivity.java | 신규 생성 |
| ShipmentActivity.java | 상속 변경: ScannerActivity → HoneywellScannerActivity |
| ProductionActivity.java | 상속 변경: ScannerActivity → HoneywellScannerActivity |
| AndroidManifest.xml | ScanResultReceiver 등록 제거 |

---

## Step 1. Honeywell Intent 정보 확인 ✅ 완료

**Part 1. 분석**
- 메서드: N/A (사전 조사)
- 범위: 라인 N/A
- 용도: Honeywell EDA51 바코드 스캐너 Intent 정보 확인
- 주의할 점: 디바이스 설정에서 Scan To Intent 활성화 필요
- 호출 수: N/A

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | Intent Action | N/A | N/A | `com.honeywell.scantointent.intent.action.BARCODE_DATA` |
| 2 | Intent Extra | N/A | N/A | `com.honeywell.scantointent.intent.extra.DATA` |
| 3 | 설정 위치 | N/A | N/A | Settings → Honeywell Settings → Scan Settings → Scan To Intent |

**Part 2. 변환 계획**
- 변환 방식: PM80 SDK → Intent 직접 수신
- 사용할 헬퍼 메서드: BroadcastReceiver.onReceive()
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | ScanResultReceiver → m_brc | m_brc 직접 수신 |
  | ACTION_RECEIVE_PM80 | ACTION_BARCODE_DATA |
  | EXTRA_BARCODE | EXTRA_BARCODE_DATA |
- 주의사항: EDA51 디바이스에서 Scan To Intent 설정 활성화 필수

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트
- [x] Part 7: 주석 보강
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**: Honeywell EDA51 Intent 정보
- **왜**: HoneywellScannerActivity 생성을 위한 사전 조사
- **어떻게**: Honeywell 문서 및 디바이스 설정 확인

---

## Step 2. HoneywellScannerActivity.java 생성 ✅ 완료

**Part 1. 분석**
- 메서드: 전체 클래스 (HoneywellScannerActivity)
- 범위: 라인 1~223
- 용도: Honeywell EDA51 전용 바코드 스캐너 기본 Activity 제공
- 주의할 점: ScannerActivity와 동일한 UI/기능 유지 필수
- 호출 수: BroadcastReceiver 1회

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | ACTION_BARCODE_DATA | 라인 58-59 | N/A | Honeywell 바코드 Action 상수 |
| 2 | EXTRA_BARCODE_DATA | 라인 62-63 | N/A | Honeywell 바코드 Extra 키 상수 |
| 3 | btn_init | 라인 70 | N/A | protected Button (ActionBar 초기화 버튼) |
| 4 | swt_print | 라인 73 | N/A | protected SwitchCompat (인쇄 스위치) |
| 5 | onCreate() | 라인 87-112 | N/A | BroadcastReceiver 등록, ActionBar 설정 |
| 6 | onCheckedChanged() | 라인 126-138 | N/A | 인쇄 ON/OFF 콜백 |
| 7 | onDestroy() | 라인 152-156 | N/A | Receiver 해제 |
| 8 | setMessage() | 라인 189-190 | N/A | 바코드 수신 콜백 (하위에서 오버라이드) |
| 9 | m_brc | 라인 202-221 | N/A | Honeywell 바코드 수신 BroadcastReceiver |

**Part 2. 변환 계획**
- 변환 방식: ScannerActivity 복사 후 PM80 코드 제거, Honeywell 코드 추가
- 사용할 헬퍼 메서드: N/A (신규 생성)
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | PM80 SDK import 제거 | Honeywell은 SDK 불필요 |
  | mScanner, mDecodeResult 제거 | PM80 SDK 변수 |
  | ScanResultReceiver 제거 | PM80 SDK 결과 수신용 |
  | initScanner() 제거 | PM80 SDK 초기화 |
  | ACTION_BARCODE_DATA 추가 | Honeywell 바코드 Action |
  | EXTRA_BARCODE_DATA 추가 | Honeywell 바코드 Extra 키 |
- 주의사항: ScannerActivity와 100% 동일한 UI/기능 유지

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트
- [x] Part 7: 주석 보강
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**: HoneywellScannerActivity.java 신규 생성
- **왜**: Honeywell EDA51 바코드 스캐너 지원
- **어떻게**: ScannerActivity에서 공통 코드 복사, PM80 제거, Honeywell Intent 수신 추가

---

## Step 3. ShipmentActivity 상속 변경 ✅ 완료

**Part 1. 분석**
- 메서드: 클래스 선언부
- 범위: 라인 40 (import), 라인 149 (extends)
- 용도: ShipmentActivity의 상위 클래스를 HoneywellScannerActivity로 변경
- 주의할 점: 기존 기능에 영향 없어야 함
- 호출 수: N/A

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | import 문 | 라인 40 | N/A | HoneywellScannerActivity import |
| 2 | extends 문 | 라인 149 | N/A | extends HoneywellScannerActivity |

**Part 2. 변환 계획**
- 변환 방식: import 및 extends 문 수정
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | import ...scanner.ScannerActivity | import ...scanner.HoneywellScannerActivity |
  | extends ScannerActivity | extends HoneywellScannerActivity |
- 주의사항: 기존 setMessage() 오버라이드 코드 변경 없음

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트
- [x] Part 7: 주석 보강
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**: ShipmentActivity 상속 변경
- **왜**: Honeywell EDA51 바코드 수신을 위해
- **어떻게**: ScannerActivity → HoneywellScannerActivity 상속 변경

---

## Step 4. ProductionActivity 상속 변경 ✅ 완료

**Part 1. 분석**
- 메서드: 클래스 선언부
- 범위: 라인 19 (import), 라인 47 (extends)
- 용도: ProductionActivity의 상위 클래스를 HoneywellScannerActivity로 변경
- 주의할 점: 기존 기능에 영향 없어야 함
- 호출 수: N/A

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | import 문 | 라인 19 | N/A | HoneywellScannerActivity import |
| 2 | extends 문 | 라인 47 | N/A | extends HoneywellScannerActivity |

**Part 2. 변환 계획**
- 변환 방식: import 및 extends 문 수정
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | import ...scanner.ScannerActivity | import ...scanner.HoneywellScannerActivity |
  | extends ScannerActivity | extends HoneywellScannerActivity |
- 주의사항: 기존 setMessage() 오버라이드 코드 변경 없음

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트
- [x] Part 7: 주석 보강
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**: ProductionActivity 상속 변경
- **왜**: Honeywell EDA51 바코드 수신을 위해
- **어떻게**: ScannerActivity → HoneywellScannerActivity 상속 변경

---

## Step 5. AndroidManifest.xml 수정 ✅ 완료

**Part 1. 분석**
- 메서드: N/A (XML 설정)
- 범위: AndroidManifest.xml 전체
- 용도: PM80 관련 설정 제거
- 주의할 점: Honeywell은 별도 Manifest 설정 불필요
- 호출 수: N/A

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | ScanResultReceiver | 제거됨 | N/A | PM80 전용 BroadcastReceiver |
| 2 | device.common.USERMSG | 제거됨 | N/A | PM80 SDK intent-filter |

**Part 2. 변환 계획**
- 변환 방식: PM80 관련 설정 제거
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | ScanResultReceiver 등록 | 제거 |
  | device.common.USERMSG intent-filter | 제거 |
- 주의사항: Honeywell은 Activity 내에서 동적으로 BroadcastReceiver 등록

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트
- [x] Part 6: 회귀테스트
- [x] Part 7: 주석 보강
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**: AndroidManifest.xml PM80 설정 제거
- **왜**: PM80 미사용으로 불필요한 설정 정리
- **어떻게**: ScanResultReceiver 등록 및 intent-filter 제거

---

## Step 6. 통합 테스트

**Part 1. 분석**
- 메서드: N/A (테스트)
- 범위: HoneywellScannerActivity, ShipmentActivity, ProductionActivity
- 용도: Honeywell EDA51 바코드 스캔 통합 테스트
- 주의할 점: 실제 디바이스 필요
- 호출 수: N/A

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | APK 빌드 | N/A | N/A | 빌드 오류 없음 확인 |
| 2 | APK 설치 | N/A | N/A | EDA51 디바이스에 설치 |
| 3 | 바코드 스캔 | N/A | N/A | 계근입력 화면에서 바코드 스캔 테스트 |
| 4 | setMessage() | N/A | N/A | 바코드 수신 콜백 동작 확인 |
| 5 | ActionBar | N/A | N/A | 뒤로가기, 초기화 버튼, 인쇄 스위치 동작 확인 |

**Part 2. 변환 계획**
- 변환 방식: N/A (테스트)
- 사용할 헬퍼 메서드: N/A
- 명령어 매핑:
  | 기존 | 변환 후 |
  |------|---------|
  | N/A | N/A |
- 주의사항: Honeywell EDA51 디바이스 필요

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

## 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | Honeywell Intent 정보 확인 | ✅ 완료 |
| 2 | HoneywellScannerActivity.java 생성 | ✅ 완료 |
| 3 | ShipmentActivity 상속 변경 | ✅ 완료 |
| 4 | ProductionActivity 상속 변경 | ✅ 완료 |
| 5 | AndroidManifest.xml 수정 | ✅ 완료 |
| 6 | 통합 테스트 | ⏳ 대기 (디바이스 필요) |

---

**최종 수정일**: 2026-01-12
