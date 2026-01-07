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
- **현재 코드 라인**: 약 4,478줄
- **작성일**: 2026-01-07

---

## 현재 코드 분석 요약

### 파일 구조
| 영역 | 라인 범위 | 설명 |
|-----|----------|------|
| 멤버 변수 | 1~330 | 330개 이상의 변수 선언 |
| 생명주기 | 331~525 | onCreate, onResume, onDestroy 등 |
| 버튼 리스너 | 525~760 | inputBtnListener, sendBtnListener, selectBtnListener 등 |
| setMessage | 892~908 | PM80 스캐너 진입점 |
| setBarcodeMsg | 959~1485 | **핵심 메서드** (약 526줄) |
| wet_data_insert | 1487~1660 | 계근 데이터 저장 |
| find_* 메서드 | 1663~1795 | 패커상품 검색 |
| Spinner 리스너 | 1845~1968 | 센터/지점/작업유형 선택 |
| setPrinting | 2114~2960 | 이마트 라벨 출력 (약 846줄) |
| setHomeplusPrinting | 2966~3088 | 홈플러스 라벨 출력 |
| setPrintingLotte | 3113~3375 | 롯데 라벨 출력 |
| ProgressDlgShipSelect | 3409~3592 | 출하대상 조회 AsyncTask |
| ProgressDlgShipmentSend | 3804~4123 | 서버 전송 AsyncTask |
| Dialog 메서드 | 4121~4705 | 각종 다이얼로그 |

### 주요 문제점
1. **매직 넘버**: searchType "0"~"7", 롯데 박스 순번 9999, 업체코드 610933 등
2. **거대 메서드**: setBarcodeMsg (526줄), setPrinting (846줄)
3. **코드 중복**: 전송 로직, 소비기한 계산 로직
4. **하드코딩**: 업체코드 "30228", 지점코드 "9231" 등
5. **문자열 비교**: `==` 대신 `.equals()` 사용 필요한 곳 존재

---

## 리팩토링 체크리스트

### Step 1. searchType 상수 정의
- [ ] SEARCH_TYPE_SHIPMENT = "0" (이마트 출하)
- [ ] SEARCH_TYPE_PRODUCTION_INNO = "1" (생산 계근 이노이천)
- [ ] SEARCH_TYPE_HOMEPLUS = "2" (홈플러스 출하)
- [ ] SEARCH_TYPE_WHOLESALE = "3" (도매 출하)
- [ ] SEARCH_TYPE_NONFIXED = "4" (도매 비정량)
- [ ] SEARCH_TYPE_HOMEPLUS_NONFIXED = "5" (홈플러스 비정량)
- [ ] SEARCH_TYPE_LOTTE = "6" (롯데 출하)
- [ ] SEARCH_TYPE_PRODUCTION = "7" (생산 계근)

**비고**: MainActivity에서 정의한 상수와 동일하게 Common.java에 정의하거나, 각 Activity에서 import하여 사용

---

### Step 2. 매직 넘버 상수화
- [ ] `LOTTE_BOX_ORDER_MAX = 9999` (롯데 박스 순번 최대값)
- [ ] `COMPANY_CODE = "610933"` (회사 코드)
- [ ] `COMPANY_NAME = "(주)하이랜드이노베이션"` (회사명)
- [ ] `MEAT_CENTER_CODE = "059015"` (미트센터 코드)
- [ ] `MEAT_CENTER_STORE_CODE = "9231"` (미트센터 지점코드)
- [ ] `KILKOY_PACKER_CODE = "30228"` (킬코이 패커코드)

---

### Step 3. setBarcodeMsg() 메서드 분리
현재 526줄의 거대 메서드를 의미 단위로 분리

#### 3-1. 패커상품 스캔 처리 분리
- [ ] `handlePackerProductScan(String msg)` 메서드 추출 (967~1094줄)
  - 최초 스캔 처리
  - 동일 상품 재스캔 처리
  - 다른 상품 스캔 처리

#### 3-2. BL 스캔 처리 분리
- [ ] `handleBLScan(String msg)` 메서드 추출 (1099~1434줄)
  - BL번호로 position 찾기
  - 소비기한 검증
  - 계근 완료 체크
  - 중복 바코드 체크

#### 3-3. 중량 추출 로직 분리
- [ ] `extractWeight(int itemType)` 메서드 추출 (1255~1425줄)
  - ITEM_TYPE별 중량 추출 로직
  - LB → KG 변환
  - 제조일/박스시리얼 추출

---

### Step 4. 소비기한 검증 로직 분리
- [ ] `validateShelfLife(int position)` 메서드 추출
  - 킬코이 + 미트센터 검증 (1131~1138줄)
  - 트레이더스/수입육 검증 (1177~1186줄)

---

### Step 5. setPrinting() 메서드 분리
현재 846줄의 거대 메서드를 분리

#### 5-1. 소비기한 계산 분리
- [ ] `calculateExpiryDate(String makingDate)` 메서드 추출 (2129~2222줄)
  - 미트센터 특수 처리
  - 트레이더스 센터 처리

#### 5-2. 바코드 생성 분리
- [ ] `generateBarcode(String barcodeType, Shipments_Info si, String weightStr)` 메서드 추출 (2297~2484줄)
  - M0~M9, E0~E3 바코드 형식별 생성

#### 5-3. Woosim 프린터 명령 분리
- [ ] `printEmartLabel(ByteArrayOutputStream stream, Shipments_Info si)` 메서드 추출 (2510~2778줄)

---

### Step 6. 서버 전송 로직 공통화
ProgressDlgShipmentSend 내 중복 로직 정리

- [ ] 개별 건 전송 / 일괄 전송 로직 공통 메서드 추출
- [ ] complete_shipment API 호출 조건 상수화

---

### Step 7. 문자열 비교 수정
- [ ] `searchType == "0"` → `"0".equals(searchType)` 또는 `SEARCH_TYPE_SHIPMENT.equals(searchType)`
- [ ] 기타 `==` 문자열 비교 → `.equals()` 변경

---

### Step 8. Lambda 표현식 변환
- [ ] DialogInterface.OnClickListener → Lambda
- [ ] View.OnClickListener → Lambda (선택적)
- [ ] Spinner.OnItemSelectedListener (Lambda 변환 불가능 - 2개 메서드)

---

### Step 9. 코드 정리
- [ ] 미사용 변수 삭제
- [ ] 불필요한 주석 정리 (Cleanup 문서 참조 - 이미 155줄 삭제됨)
- [ ] `if (true)` 조건문 검토 (1034줄 - find_BL(msg) 복원 또는 제거)
- [ ] `finish_flag` 변수 검토 (값 설정만 되고 사용 안됨)

---

## 리팩토링 우선순위

### 높음 (즉시 적용 가능)
1. **Step 1**: searchType 상수 정의 - 가독성 향상, 오타 방지
2. **Step 2**: 매직 넘버 상수화 - 유지보수성 향상
3. **Step 7**: 문자열 비교 수정 - 버그 예방

### 중간 (신중한 적용 필요)
4. **Step 8**: Lambda 표현식 변환 - 코드 간결화
5. **Step 4**: 소비기한 검증 분리 - 재사용성 향상
6. **Step 9**: 코드 정리 - 불필요 코드 제거

### 낮음 (대규모 변경)
7. **Step 3**: setBarcodeMsg 분리 - 526줄 메서드 분할
8. **Step 5**: setPrinting 분리 - 846줄 메서드 분할
9. **Step 6**: 전송 로직 공통화 - AsyncTask 구조 변경

---

## 주의사항

### 동작 보장
- 모든 8가지 출하 유형(searchType 0~7)이 정상 동작해야 함
- 프린터 출력 결과가 기존과 100% 동일해야 함
- 서버 전송 패킷 형식이 변경되면 안됨

### 테스트 필수 항목
1. 바코드 스캔 → 출하대상 조회
2. 중량 추출 (ITEM_TYPE W, S, J, B)
3. 계근 데이터 DB 저장
4. 프린터 출력 (이마트, 홈플러스, 롯데, 생산)
5. 서버 전송 (개별 건, 일괄 전송)
6. 롯데 박스 순번 순환 (1~9999)
7. 비정량 중복 바코드 허용
8. 미트센터 추가 라벨 출력

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
| - | 리팩토링 계획 문서 작성 | 2026-01-07 | 현재 문서 |
| - | 주석 처리 코드 삭제 (1차) | 2026-01-06 | Cleanup 문서 참조 |
| - | 주석 처리 코드 삭제 (2차) | 2026-01-06 | 약 155줄 삭제 |

---

**최종 수정일**: 2026-01-07
