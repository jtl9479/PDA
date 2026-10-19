# PDA 미사용 소스 정리

**작성일**: 2026-10-19
**목적**: PDA 앱 전체 점검(2026-10-19)에서 확인한 미사용 클래스·메서드·필드·import·리소스를 삭제한다. 동작은 바뀌지 않는다 (호출·참조가 없는 코드만 삭제).

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### 점검 방법

- 정적 분석 스크립트: Java 66개 파일의 클래스·메서드·필드 선언별 참조 수(주석 제외), res/Manifest 리소스 참조(`R.xxx.name`, `@xxx/name`), import 사용 여부
- 후보를 직접 확인하고 원본(`D:\PDA\PDA-INNO(원본)`)과 대조

### 문제점

- 원본부터 미사용이거나 전환 중 쓰지 않게 된 소스가 남아 있어 소스 파악을 어렵게 한다 (프로젝트 목적 4. 소스 정리).

### 정리 제외 (유지)

| 대상 | 이유 |
|---|---|
| `ShipmentActivity.java` | 유지 결정 (진입 불가 구버전) |
| `print/BluetoothPrintService.java`, `scanner/Constants.java` | `ShipmentActivity`가 사용 |
| `ProductionActivity.java` | 메인 버튼으로 진입 가능 (생산 미사용은 업무 결정) |
| `common/TestDataHelper.java` | 테스트 도구 (`TEST_MODE=false`) |
| `BixolonSocketPrinter.printTestLabel()` | `BixolonShipmentActivity.java:477`에서 사용 |
| items getter/setter 11개 | 단순 접근자, 정리 불필요 |

---

## 2. 변경 구조

### 데이터 흐름

```
호출/참조 0인 소스
    ↓ 삭제
빌드 성공 + 실기기 주요 화면 동작 동일
```

---

## 3. 수정 대상 파일

| # | 파일 | 수정 내용 |
|:-:|------|----------|
| 1 | `scanner/ScannerActivity.java` | 파일 삭제 (HoneywellScannerActivity로 대체된 이전 기반 클래스, 원본은 부모 클래스였음) |
| 2 | `common/ProgressDlgNewBarcodeInfo.java` | 파일 삭제 (원본부터 미사용) |
| 3 | `common/Base64.java` | 파일 삭제 (원본부터 미사용) |
| 4 | `LoginActivity.java` | 빅솔론 테스트 출력 체인 삭제: `printTest`, `showPrinterSelectDialog`, `sendBixolonTestPrint`, `mHandler`, `mBixolonHandler`, 필드 `mBluetoothAdapter`·`mPrintService`·`mBixolonPrinter` (printTest 호출 0 → 체인 전체 미사용) |
| 5 | `db/DBHandler.java` | `selectqueryShipmentOnly`, `selectqueryShipmentBL`, `updatequeryBarcodeInfo`, `selectquerySearchBarcodeInfo` 삭제 (원본부터 미사용) |
| 6 | `print/BixolonSocketPrinter.java` | `sendCommand`, `getConnectedDeviceName`, `printBitmap` 삭제 |
| 7 | `print/LabelPrintHelper.java` | `slcsText` 삭제 |
| 8 | `common/Common.java` | `URL_VERSION`, `URL_UPDATE_BARCODE_INFO` 삭제 |
| 9 | import | 미사용 import 삭제: LoginActivity, ProductionActivity(`ArrayList`), ProgressDlgShipSearch(`HashMap`), BixolonSocketPrinter(`BitmapFactory` 외 삭제 메서드 전용), ShipmentMode(`Log`, `Common`, `DBHandler`) |
| 10 | res | `layout/popup_activity.xml`, `drawable/highland_back.jpg`·`printer.png`, `mipmap-*/ic_launcher.png`(5), `raw/chk.wav`·`s.mp3`, color `colorSelect`·`titletextcolor`, style `WindowTitleBackground`, array `emart_center_list`, string 36개 |

---

## 4. 수정 상세

- 삭제만 수행한다. 남는 코드의 내용·순서는 바꾸지 않는다.
- 메서드 삭제 시 바로 위 메서드 주석도 함께 삭제한다.

**검증**: `gradlew assembleDebug` 성공, 삭제 이름 재검색 0건, EDA51 설치 후 로그인 → 메인 → 출하대상받기 → 계근 화면 → 라벨 출력 확인

---

## 5. 사이드이펙트

- 호출·참조가 없는 코드만 삭제하므로 동작 변화 없음.
- 정적 분석으로 잡을 수 없는 리플렉션·문자열 호출은 없음 (`Class.forName`, `getIdentifier` 사용 없음 확인 후 진행).

---

## 6. 데이터 저장 구조

- 해당 없음 (로컬DB 구조·JSP 변경 없음).

---

## 7. 호출 시점

- 해당 없음 (삭제 대상은 호출되지 않음).

---

## 8. 개발 플랜

### Step 1: Java 미사용 소스 삭제 (3장 #1~#9)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트 (삭제 이름 재검색 0건)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**: 파일 3개 삭제(ScannerActivity, ProgressDlgNewBarcodeInfo, Base64), LoginActivity 테스트 출력 체인(메서드 3·핸들러 2·필드 3), DBHandler 메서드 4, BixolonSocketPrinter 메서드 3, LabelPrintHelper `slcsText`, Common URL 상수 2 삭제. 미사용 import 정리(LoginActivity 11, ProductionActivity 1, ProgressDlgShipSearch 1, DBHandler 1, BixolonSocketPrinter 3, ShipmentMode 3)
- **왜**: 호출·참조 0 (원본부터 미사용 또는 전환 중 미사용)
- **어떻게**: 선언 단위 삭제(바로 위 주석 포함), `gradlew assembleDebug` 성공, 삭제 이름 재검색 0건 (2026-10-19). Part 6 회귀테스트는 Step 3에서 수행

---

### Step 2: 미사용 리소스 삭제 (3장 #10)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인
- [x] Part 5: 단위테스트 (리소스 참조 재검색 0건)
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**: 파일 10개(layout `popup_activity`, drawable `highland_back.jpg`·`printer.png`, raw `chk.wav`·`s.mp3`, mipmap `ic_launcher.png` 5), `values/emart_center_list.xml` 파일, string 36·color 3(`colorSelect`, `titletextcolor`, `titlebackgroundcolor`)·style 1(`WindowTitleBackground`) 삭제
- **왜**: `R.xxx.name`·`@xxx/name` 참조 0. `titlebackgroundcolor`는 `WindowTitleBackground` 삭제 후 참조 0이 되어 함께 삭제
- **어떻게**: 리소스 참조 스캔 → 삭제 → 재스캔 0건, `gradlew assembleDebug` 성공 (2026-10-19). Part 6 회귀테스트는 Step 3에서 수행

---

### Step 3: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | `gradlew assembleDebug` 성공 | □ |
| 2 | EDA51 로그인 → 메인 | □ |
| 3 | 출하대상받기 → 계근 화면 진입 | □ |
| 4 | 스캔 계근 + 라벨 출력 | □ |

---

### 개발 순서 요약

```
Step 1: Java 미사용 소스 삭제
    ↓
Step 2: 미사용 리소스 삭제
    ↓
Step 3: 통합 테스트
```

---

## 9. 테스트 시나리오

### 시나리오 1: 빌드·기본 동작

```
1. gradlew assembleDebug
2. EDA51 설치, 로그인
3. 이마트 출하대상받기 → 계근입력시작 → 스캔 1건 + 라벨 출력
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 빌드 오류 | 정적 분석 누락 참조 | 해당 항목 복원 후 문서 갱신 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | Java 미사용 소스 삭제 | ✅ 완료 |
| 2 | 미사용 리소스 삭제 | ✅ 완료 |
| 3 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/개발/77_BixolonShipmentActivity_역할별_클래스_분리.md` (이전 정리 작업)

---

**문서 버전**: 1.0
