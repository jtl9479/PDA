# ShipmentActivity.java 리팩토링 계획

---

## ⚠️ 최우선 원칙

### 기존 동작과 100% 동일하게 동작해야 한다

이것이 리팩토링의 **제일 중요한 원칙**이다.

- 모든 변경은 **기능 변화 없이** 코드 구조만 개선해야 한다
- 리팩토링 후 **동일한 입력에 동일한 출력**이 보장되어야 한다
- 의심스러운 변경은 **하지 않는다**

---

## 개요
- **파일 위치**: `app/src/main/java/com/rgbsolution/highland_emart/ShipmentActivity.java`
- **현재 코드 라인**: 4,479줄
- **작성일**: 2026-01-07
- **소스 확인**: 전체 소스 코드 직접 확인 완료

---

## 현재 코드 구조 (실제 라인 기준)

| 영역 | 라인 범위 | 줄 수 | 설명 |
|-----|----------|------|------|
| import 및 클래스 선언 | 1~100 | 100 | 패키지, import 문 |
| 멤버 변수 | 101~330 | 230 | 약 230개 이상의 변수 선언 |
| onCreate() | 331~525 | 195 | 생명주기, 초기화 |
| 버튼 리스너 | 526~760 | 235 | inputBtnListener, sendBtnListener 등 |
| mHandler, setMessage() | 761~908 | 148 | PM80 스캐너 메시지 처리 |
| **setBarcodeMsg()** | 909~1485 | **576** | **핵심 메서드 - 바코드 스캔 처리** |
| wet_data_insert() | 1486~1660 | 175 | 계근 데이터 DB 저장 |
| find_* 메서드 | 1661~1795 | 135 | 패커상품/BL 검색 |
| Spinner 리스너 | 1796~1968 | 173 | 센터/지점/작업유형 선택 |
| **setPrinting()** | 1969~2746 | **777** | **이마트 라벨 출력** |
| setHomeplusPrinting() | 2747~2874 | 127 | 홈플러스 라벨 출력 |
| setPrintingLotte() | 2875~3157 | 282 | 롯데 라벨 출력 |
| sendData() | 3158~3167 | 10 | 프린터 데이터 전송 |
| ProgressDlgShipSelect | 3168~3373 | 206 | 출하대상 조회 AsyncTask |
| ProgressDlgShipSelectBL | 3374~3556 | 183 | BL번호 기반 조회 |
| **ProgressDlgShipmentSend** | 3557~3896 | **339** | **서버 전송 AsyncTask** |
| ProgressDlgPrintConnect | 3897~3952 | 56 | 프린터 연결 |
| onActivityResult | 3953~4041 | 89 | Activity 결과 처리 |
| ProgressDlgDiscon | 4042~4080 | 39 | 프린터 연결 해제 |
| show_wetDetailDialog() | 4081~4268 | 188 | 계근 상세 내역 다이얼로그 |
| 기타 Dialog | 4269~4438 | 170 | 삭제/전송완료/계근완료 다이얼로그 |
| showAlertDialog() | 4439~4478 | 40 | 에러 알림 다이얼로그 |

---

## searchType 사용 현황 (실제 소스 확인)

| 값 | 의미 | 사용 위치 (주요) |
|----|------|-----------------|
| "0" | 이마트 출하 | 2016줄, 3624줄, 3651줄, 3688줄 등 |
| "1" | 생산 계근 (이노이천) | 3655줄, 3727줄, 3773줄 등 |
| "2" | 홈플러스 출하 | 3653줄, 3688줄, 3771줄 등 |
| "3" | 도매 출하 | 3651줄, 3727줄, 3776줄 등 |
| "4" | 도매 비정량 | 3727줄, 3773줄, 3819줄 등 |
| "5" | 홈플러스 비정량 | 3727줄, 3773줄, 3819줄 등 |
| "6" | 롯데 출하 | 2922줄, 3240줄, 3653줄 등 |
| "7" | 생산 계근 (라벨) | 3657줄, 3727줄, 3823줄 등 |

**총 사용 횟수**: 약 50회 이상

---

## 매직 넘버 현황 (실제 소스 확인)

| 값 | 의미 | 사용 위치 |
|----|------|----------|
| `9999` | 롯데 박스 순번 최대값 | 3245줄, 3252줄, 3253줄 |
| `"610933"` | 회사 코드 | 2059줄 |
| `"(주)하이랜드이노베이션"` | 회사명 | 2060줄, 2778줄, 2906줄 |
| `"059015"` | 미트센터 코드 | 2590줄, 2668줄 |
| `"9231"` | 미트센터 지점코드 | 2583줄, 2663줄 |
| `"0000000"` | 로지스코드 기본값 | 2583줄, 2663줄 |

---

## 리팩토링 체크리스트

### Step 1. searchType 상수 정의 ✅ 완료 (2026-01-07)
- [x] `SEARCH_TYPE_EMART = "0"` (이마트 출하)
- [x] `SEARCH_TYPE_PRODUCTION = "1"` (생산 계근)
- [x] `SEARCH_TYPE_HOMEPLUS = "2"` (홈플러스 출하)
- [x] `SEARCH_TYPE_WHOLESALE = "3"` (도매 출하)
- [x] `SEARCH_TYPE_NONFIXED = "4"` (도매 비정량)
- [x] `SEARCH_TYPE_HOMEPLUS_NONFIXED = "5"` (홈플러스 비정량)
- [x] `SEARCH_TYPE_LOTTE = "6"` (롯데 출하)
- [x] `SEARCH_TYPE_PRODUCTION_LABEL = "7"` (생산 라벨)

**변경 내역**: 157~165줄에 상수 정의, 47개 사용처 모두 상수로 변경, SHIPMENT → EMART로 명칭 변경

---

### Step 2. 매직 넘버 상수화 ✅ 완료 (2026-01-07)
- [x] `COMPANY_CODE = "610933"` - 회사 코드
- [x] `COMPANY_NAME = "(주)하이랜드이노베이션"` - 회사명
- [x] `MEAT_CENTER_CODE = "059015"` - 미트센터 업체코드
- [x] `MEAT_CENTER_STORE_CODE = "9231"` - 미트센터 지점코드
- [x] `KILKOY_PACKER_CODE = "30228"` - 킬코이 패커코드
- [x] `LOGIS_CODE_DEFAULT = "0000000"` - 로지스코드 기본값
- [x] `LOTTE_BOX_ORDER_MAX = 9999` - 롯데 박스 순번 최대값

**변경 내역**: 167~178줄에 7개 상수 정의, 모든 사용처 상수로 변경

---

### Step 3. 문자열 비교 수정 ❌ 보류

현재 소스에서 `Common.searchType.equals("0")` 형태로 **이미 올바르게** `.equals()` 사용 중.

**수정 대상 항목** (기존 동작 변경으로 보류):
- [ ] `whArea != null || !whArea.equals("")` (4곳) - 수정 시 빈 문자열 처리 동작 변경됨
- [ ] `getBL_NO() == ""` (1808줄) - 수정 시 alert 동작 변경됨
- [ ] `work_item_barcodegoods == ""` (3358줄) - 수정 시 alert 동작 변경됨
- [ ] `packet ==""` (3786줄) - 수정 시 전송 동작 변경됨

**보류 사유**: 기존 동작과 100% 동일해야 하는 원칙에 따라 보류. 버그 수정이지만 운영 중인 동작을 변경하게 됨.

---

### Step 4. Lambda 표현식 변환 ✅ 완료 (2026-01-07)

**View.OnClickListener 변환 (3곳)**:
- [x] `detail_btn_back.setOnClickListener(v -> {...})`
- [x] `detail_btn_delete.setOnClickListener(v -> {...})`
- [x] `detail_btn_sum.setOnClickListener(v -> {...})`

**DialogInterface.OnClickListener 변환 (9곳)**:
- [x] sendBtnListener - setPositiveButton, setNegativeButton
- [x] setBarcodeMsg() 내 다이얼로그 - setPositiveButton, setNegativeButton
- [x] deleteQuestionDialog - setPositiveButton
- [x] show_sendFinishDialog - setPositiveButton
- [x] show_wetNextDialog - setPositiveButton
- [x] show_wetFinishDialog - setPositiveButton
- [x] showAlertDialog - setNeutralButton

**변경 내역**: 총 12개 익명 클래스 → Lambda 변환, 약 36줄 감소

---

### Step 5. 코드 정리 ✅ 완료 (2026-01-07)

#### 5-1. 완료 항목
- [x] `finish_flag` 변수 삭제 - 미사용 변수 (값 설정만 하고 읽지 않음)
  - 변수 선언 삭제 (336줄)
  - 값 설정 코드 삭제 (4345줄, 4404줄)

#### 5-2. 보류 항목 (Step 3과 동일)
- [ ] `whArea != null || !whArea.equals("")` - 논리 오류 (기존 동작 변경됨)

**변경 내역**: 미사용 변수 `finish_flag` 삭제 (3줄 감소)

---

## 리팩토링 우선순위

### 높음 (즉시 적용 가능, 안전)
1. **Step 1**: searchType 상수 정의 - 가독성 향상, 오타 방지
2. **Step 2**: 매직 넘버 상수화 - 유지보수성 향상
3. **Step 3**: 문자열 비교 수정 (`==` → `.equals()`) - 버그 예방

### 중간 (신중한 적용)
4. **Step 4**: Lambda 표현식 변환 - 코드 간결화
5. **Step 5**: 미사용 변수/논리 오류 수정

### 낮음 (대규모 변경, 현재 범위 제외)
- setBarcodeMsg() 메서드 분리 (576줄)
- setPrinting() 메서드 분리 (777줄)
- 서버 전송 로직 공통화

**대규모 변경은 현재 리팩토링 범위에서 제외**.

---

## 테스트 체크리스트

리팩토링 후 반드시 확인해야 할 항목:

### 기능 테스트
- [ ] 이마트 출하 (searchType "0") - 바코드 스캔, 라벨 출력, 서버 전송
- [ ] 생산 계근 (searchType "1") - 바코드 스캔, 라벨 출력, 서버 전송
- [ ] 홈플러스 출하 (searchType "2") - 바코드 스캔, 라벨 출력, 서버 전송
- [ ] 도매 출하 (searchType "3") - 바코드 스캔, 라벨 출력, 서버 전송
- [ ] 도매 비정량 (searchType "4") - 바코드 스캔, 라벨 출력, 서버 전송
- [ ] 홈플러스 비정량 (searchType "5") - 바코드 스캔, 라벨 출력, 서버 전송
- [ ] 롯데 출하 (searchType "6") - 바코드 스캔, 라벨 출력, 서버 전송, 박스 순번
- [ ] 생산 라벨 (searchType "7") - 바코드 스캔, 라벨 출력, 서버 전송

### 프린터 출력 테스트
- [ ] 이마트 라벨 (M0~M9, E0~E3, P0)
- [ ] 홈플러스 라벨 (H5)
- [ ] 롯데 라벨 (L0)
- [ ] 미트센터 추가 라벨 (9231 지점)

### 서버 전송 테스트
- [ ] 개별 건 전송 (이마트, 홈플러스, 롯데)
- [ ] 일괄 전송 (생산, 도매)
- [ ] complete_shipment API 호출

---

## 관련 문서

- [ShipmentActivity_Part1.md](ShipmentActivity_Part1.md) - 개요 및 클래스 구조
- [ShipmentActivity_Part2.md](ShipmentActivity_Part2.md) - 바코드 스캔 처리
- [ShipmentActivity_Part3.md](ShipmentActivity_Part3.md) - 계근 데이터 처리 및 프린터 출력
- [ShipmentActivity_Part4.md](ShipmentActivity_Part4.md) - 서버 전송 및 예외 처리
- [ShipmentActivity_Cleanup.md](ShipmentActivity_Cleanup.md) - 정리 기록

---

## 완료 기록

| 단계 | 작업 내용 | 완료 일자 | 비고 |
|-----|----------|----------|------|
| Step 1 | searchType 상수 정의 | 2026-01-07 | 8개 상수, 47개 사용처 변경 |
| Step 2 | 매직 넘버 상수화 | 2026-01-07 | 7개 상수 정의, 모든 사용처 변경 |
| Step 3 | 문자열 비교 수정 | - | **보류** (기존 동작 변경됨) |
| Step 4 | Lambda 표현식 변환 | 2026-01-07 | 12개 익명 클래스 변환, 약 36줄 감소 |
| Step 5 | 코드 정리 | 2026-01-07 | 미사용 변수 finish_flag 삭제 |
| - | 리팩토링 계획 문서 작성 | 2026-01-07 | 전체 소스 확인 후 재작성 |
| - | 주석 처리 코드 삭제 (Cleanup) | 2026-01-06 | 약 155줄 삭제 완료 |

---

**최종 수정일**: 2026-01-07
