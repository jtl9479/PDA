# BixolonShipmentActivity 신규 생성 계획

---

## 최우선 원칙

### ShipmentActivity와 100% 동일한 기능을 지원해야 한다

- 프린터 방식만 다르고, 나머지 기능은 **완전히 동일**해야 한다
- Woosim → Bixolon 프린터 코드만 변경

---

## 개요

- **작성일**: 2026-01-09
- **목적**: ShipmentActivity(Woosim 전용)를 유지하고, Bixolon 전용 Activity를 새로 생성

---

## 현재 구조

```
ShipmentActivity (Woosim 프린터 전용)
    - WoosimCmd, WoosimService, WoosimBarcode 사용
    - BluetoothPrintService로 통신
```

---

## 변경 후 구조

```
ShipmentActivity (Woosim 전용) - 기존 유지, 수정 없음

BixolonShipmentActivity (Bixolon 전용) - 신규 생성
    - BixolonSocketPrinter 사용
    - SLCS 명령어로 라벨 인쇄
```

---

## 장단점

### 장점

| 항목 | 설명 |
|------|------|
| 기존 코드 보존 | Woosim 코드를 건드리지 않음 |
| 롤백 용이 | 문제 발생 시 기존 Activity로 복구 |
| 양쪽 지원 가능 | 필요시 Woosim, Bixolon 모두 지원 가능 |

### 단점

| 항목 | 설명 |
|------|------|
| 코드 중복 | 4,400줄 코드가 거의 중복됨 |
| 유지보수 | 버그 수정 시 두 파일 모두 수정 필요 |

---

## 변경 대상 파일

| 파일 | 변경 내용 |
|------|----------|
| BixolonShipmentActivity.java | 신규 생성 |
| AndroidManifest.xml | BixolonShipmentActivity 등록 |
| MainActivity.java | BixolonShipmentActivity 호출로 변경 |

---

## BixolonShipmentActivity 설계

### 제거 항목 (Woosim 전용)

| 항목 | 이유 |
|------|------|
| Woosim import | Bixolon은 Woosim SDK 불필요 |
| WoosimService mWoosim | Woosim 서비스 |
| WoosimCmd.* | Woosim 명령어 (245회 사용) |
| WoosimBarcode.* | Woosim 바코드 |
| WoosimImage.* | Woosim 이미지 |

### 추가 항목 (Bixolon 전용)

| 항목 | 설명 |
|------|------|
| BixolonSocketPrinter import | Bixolon 프린터 서비스 |
| BixolonSocketPrinter mBixolonPrinter | Bixolon 프린터 인스턴스 |
| SLCS 명령어 | Bixolon 라벨 명령어 |

### 100% 동일하게 유지해야 하는 항목

| 항목 | 설명 |
|------|------|
| 바코드 스캔 | HoneywellScannerActivity 상속 |
| 출하대상 조회 | DB 조회 로직 동일 |
| 계근 데이터 저장 | wet_data_insert() 동일 |
| 서버 전송 | ProgressDlgShipmentSend 동일 |
| UI/레이아웃 | activity_scanner 동일 |

---

## 프린터 명령어 변환

### Woosim → Bixolon SLCS

| Woosim | Bixolon SLCS | 설명 |
|--------|-------------|------|
| WoosimCmd.PM_setArea() | SS (Set Size) | 라벨 크기 설정 |
| WoosimCmd.PM_setBarcode() | BD (Barcode Draw) | 바코드 생성 |
| WoosimCmd.PM_setTextStyle() | ST (Set Text) | 텍스트 스타일 |
| WoosimCmd.PM_setText() | T (Text) | 텍스트 출력 |
| WoosimCmd.PM_printLabel() | P (Print) | 라벨 인쇄 |

---

## Bixolon SLCS 명령어 예시

```
SS,0,200,400,300        // 라벨 크기 설정 (가로, 세로)
SD,20                    // 인쇄 농도
T,50,50,3,1,1,0,0,N,"테스트"  // 텍스트
BD,50,100,CODE128,4,2,100,0,0,"1234567890"  // 바코드
P1                       // 1장 인쇄
```

---

## 3번 작업: Woosim → Bixolon 코드 변환 상세 계획

**작성일**: 2026-01-12

---

### 현재 상태 분석

| 항목 | 현재 상태 | 비고 |
|------|----------|------|
| sendData() | ✅ BixolonSocketPrinter 사용 | 이미 변환됨 (라인 3216~3225) |
| byteStream 생성 | ❌ Woosim 명령어 사용 | 변환 필요 |
| Woosim import | 4개 | 제거 필요 |
| WoosimCmd 호출 | 241회 | SLCS 변환 필요 |
| WoosimBarcode 호출 | 10회 | SLCS 변환 필요 |
| WoosimImage 호출 | 4회 | SLCS 변환 필요 |
| mWoosim 변수 | 사용 중 | 제거 필요 |

---

### 인쇄 블록 (5개)

| # | 시작 라인 | 용도 |
|---|----------|------|
| 1 | 1938 | 이마트 기본 라벨 |
| 2 | 2385 | 이마트 확장 라벨 (E0~E3) |
| 3 | 2863 | 홈플러스 라벨 |
| 4 | 3096 | 롯데 라벨 |
| 5 | 4240 | 기타 라벨 |

---

### 인쇄 블록 분석 템플릿

각 인쇄 블록(Step 3~7) 분석 시 아래 형식을 사용한다.

```
**Step N-1. 분석**
- 메서드: [메서드명]
- 범위: 라인 [시작]~[끝]
- Woosim 호출: WoosimCmd [N]회, WoosimBarcode [N]회, WoosimImage [N]회

| # | 출력 항목 | 위치 | 크기 | 내용 |
|---|----------|------|------|------|
| 1 | [항목명] | (x, y) | [w]x[h] | [출력 내용] |
| ... | ... | ... | ... | ... |

- [ ] Step N-1: 분석 완료 확인
- [ ] Step N-2: Woosim 명령어 → SLCS 변환
- [ ] Step N-3: 컴파일 확인
- [ ] Step N-4: 단위테스트
- [ ] Step N-5: 회귀테스트 (Step 1~N-1)
- [ ] Step N-6: 주석 보강

**변경 내용** (변환 완료 후 작성):
- **무엇을**: [변환 대상]
- **왜**: [변환 이유]
- **어떻게**: [변환 방법]
```

---

### 단계별 작업 계획 (체크리스트) - 방법 B

> **방법 B**: 사용 코드 먼저 변환 → 마지막에 import 삭제

#### Step 1. mWoosim 변수 및 관련 코드 제거 ✅ 완료
- [x] `private WoosimService mWoosim = null;` 삭제 (라인 249) → 주석으로 대체
- [x] `mWoosim = new WoosimService(mHandler);` 삭제 (라인 487, 848) → 주석으로 대체
- [x] `mWoosim.processRcvData(...)` 삭제 (라인 843) → 주석으로 대체
- [x] `WoosimService.MESSAGE_PRINTER` 관련 코드 삭제 (라인 873) → 주석으로 대체
- [x] `WoosimService.MSR` 관련 코드 삭제 → 상위 블록과 함께 제거

#### Step 2. SLCS 헬퍼 메서드 생성 ✅ 완료
- [x] 텍스트 출력 메서드 생성 → `slcsText(x, y, w, h, text)` (라인 3246)
- [x] 바코드 생성 메서드 생성 → `slcsBarcode(x, y, h, data)` (라인 3261)
- [x] 선/박스 그리기 메서드 생성 → `slcsLine()`, `slcsBox()` (라인 3277, 3292)
- [x] 라벨 초기화 메서드 생성 → `slcsInit()` (라인 3220)
- [x] 인쇄 실행 메서드 생성 → `slcsPrint(copies)` (라인 3303)
- [x] 라벨 크기 설정 메서드 추가 → `slcsLabelSize(w, h)` (라인 3231)

> **[예외] Step 2 단위테스트 불가**
> - **오류 원인**: SLCS 헬퍼 메서드만 생성됨. 아직 인쇄 블록(Step 3~7)에서 호출되지 않음
> - **현재 상태**: Woosim 코드가 남아있어 컴파일 가능하나, SLCS 메서드는 미사용 상태
> - **해결 시점**: Step 3~7 완료 후 인쇄 블록에서 SLCS 메서드 호출 시 테스트 가능

#### Step 3. 인쇄 블록 1 변환 (이마트 기본 라벨) ✅ 완료
- [x] 라인 1930~1970 분석
- [x] Woosim 명령어 → SLCS 변환 (StringBuilder + slcs 헬퍼 메서드 사용)
- [x] 컴파일 확인: BUILD SUCCESSFUL
- [x] 단위테스트: SLCS 메서드 호출 확인 (라인 1935, 1943, 1945, 1950, 1954, 1958, 1962)
- [x] 회귀테스트 Step 1: mWoosim 주석 처리 유지 확인
- [x] 회귀테스트 Step 2: SLCS 헬퍼 메서드 7개 존재 확인

**변경 내용**:
- **무엇을**: ByteArrayOutputStream + Woosim 명령어 → StringBuilder + SLCS 명령어
- **왜**: Bixolon 프린터는 SLCS 명령어 사용
- **어떻게**: slcsInit(), slcsLabelSize(), slcsText(), slcsBarcode(), slcsPrint() 호출

#### Step 4. 인쇄 블록 2 변환 (이마트 확장 라벨 E0~E3) ✅ 완료
- [x] 라인 2368~2694 분석 (메인 라벨 + 미트센터+공장코드 + 미트센터)
- [x] Woosim 명령어 → SLCS 변환 (3개 서브블록 모두 변환)
- [x] 컴파일 확인: BUILD SUCCESSFUL
- [x] 단위테스트: SLCS 메서드 약 80회 호출 확인
- [x] 회귀테스트 Step 1~3: 모두 통과

**변경 내용**:
- **무엇을**: 메인 라벨, 미트센터+공장코드, 미트센터 3개 서브블록 Woosim → SLCS 변환
- **왜**: Bixolon 프린터 SLCS 명령어 사용
- **어떻게**: 각 서브블록에서 StringBuilder 사용, slcsInit/LabelSize/Text/Barcode/Line/Print 호출

#### Step 5. 인쇄 블록 3 변환 (홈플러스 라벨) ✅ 완료
- [x] 라인 2727~2795 분석 (setHomeplusPrinting 메서드 내)
- [x] Woosim 명령어 → SLCS 변환 (StringBuilder + slcs 헬퍼 메서드)
- [x] 컴파일 확인: BUILD SUCCESSFUL
- [x] 단위테스트: SLCS 메서드 14회 호출 확인
- [x] 회귀테스트:
  - Step 1: mWoosim 주석 처리 유지 (라인 249, 487, 848)
  - Step 2: SLCS 헬퍼 메서드 7개 존재 (라인 3079~3162)
  - Step 3: 인쇄 블록 1 존재 (라인 1930)
  - Step 4: 인쇄 블록 2 존재 (라인 2368)
- [x] 주석 보강: 원본 Woosim 대응, 좌표/크기 의미, 출력 항목별 번호 부여

**변경 내용**:
- **무엇을**: setHomeplusPrinting() 내 Woosim → SLCS 변환
- **왜**: Bixolon 프린터 SLCS 명령어 사용
- **어떻게**: slcsInit/LabelSize/Text/Print 호출 (지점명, 점포코드, 상품명, BOX, CT코드, 중량, 납품일자, 업체명)

#### Step 6. 인쇄 블록 4 변환 (롯데 라벨)

**Part 1. 분석**
- 메서드: setPrintingLotte()
- 범위: 라인 2960~3077
- 호출 수: WoosimCmd 약 20회, WoosimBarcode 2회, WoosimImage 4회

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | 상품명 | (10, 12) | 35x35 | si.EMARTITEM |
| 2 | 바코드1 (중량) | (100, 80) | h=60 | pBarcode (CODE128) |
| 3 | 바코드1 숫자 | (114, 139) | 25x25 | pBarcodeStr |
| 4 | 바코드2 (이력번호) | (150, 350) | h=60 | pBarcode2 (CODE128) |
| 5 | 이력번호 숫자 | (155, 410) | 25x25 | pBarcode2 |
| 6 | 중량 라벨 | (15, 180) | 40x40 | "중      량 : " |
| 7 | 중량 값 | (175, 180) | 40x40 | print_weight_double + " KG" |
| 8 | 납품처 | (15, 228) | 30x30 | pCompName |
| 9 | 제조일자 | (15, 268) | 30x30 | tempDate |
| 10 | 이력(묶음)번호 | (15, 313) | 30x30 | si.getIMPORT_ID_NO() |
| 11 | WH_AREA | (385, 305) | 65x65 | whArea |
| 12 | 겉 테두리 | (0,0)-(560,440) | 두께3 | drawBox |
| 13 | 가로선1 | y=60 | 두께3 | drawLine |
| 14 | 가로선2 | y=180 | 두께3 | drawLine |
| 15 | 가로선3 | y=345 | 두께3 | drawLine |

**Part 2. 변환 계획**
- 변환 방식: ByteArrayOutputStream + Woosim → StringBuilder + SLCS
- 사용할 헬퍼 메서드: slcsInit, slcsLabelSize, slcsText, slcsBarcode, slcsBox, slcsLine, slcsPrint
- 명령어 매핑:
  | 기존 (Woosim) | 변환 후 (SLCS) |
  |---------------|----------------|
  | WoosimCmd.initPrinter() | slcsInit() |
  | WoosimCmd.PM_setArea() | slcsLabelSize() |
  | WoosimCmd.PM_setPosition() + getTTFcode() | slcsText() |
  | WoosimBarcode.createBarcode() | slcsBarcode() |
  | WoosimImage.drawBox() | slcsBox() |
  | WoosimImage.drawLine() | slcsLine() |
  | WoosimCmd.PM_printData() | slcsPrint() |
- 주의사항:
  - L0 바코드 타입만 처리 (롯데 전용)
  - 박스/선 그리기 포함 (이마트/홈플러스와 다름)
  - 바코드 2개 출력 (중량바코드, 이력번호바코드)

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 (BUILD SUCCESSFUL)
- [x] Part 5: 단위테스트 (SLCS 메서드 약 20회 호출: slcsInit 1, slcsLabelSize 1, slcsText 11, slcsBarcode 2, slcsLine 3, slcsBox 1, slcsPrint 1)
- [x] Part 6: 회귀테스트 (Step 1~5 모두 통과)
  - Step 1: mWoosim 주석 처리 유지 (라인 249, 487, 843, 848)
  - Step 2: SLCS 헬퍼 메서드 7개 존재 (라인 3093~3176)
  - Step 3: 인쇄 블록 1 존재 (라인 1930)
  - Step 4: 인쇄 블록 2 존재 (라인 2368)
  - Step 5: 인쇄 블록 3 존재 (라인 2727)
- [x] Part 7: 주석 보강 (15개 출력 항목 번호, 좌표, 원본 Woosim 대응 주석 추가)
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용**:
- **무엇을**: setPrintingLotte() 메서드 내 Woosim 코드 (라인 2960~3077)
- **왜**: Bixolon 프린터는 SLCS 명령어를 사용하므로 Woosim → SLCS 변환 필요
- **어떻게**: ByteArrayOutputStream + WoosimCmd/WoosimBarcode/WoosimImage → StringBuilder + slcsInit/slcsLabelSize/slcsText/slcsBarcode/slcsLine/slcsBox/slcsPrint/slcsFeedToMark 헬퍼 메서드 호출로 변환. L0 바코드 타입(롯데 전용) 조건문 내 15개 출력 항목 (상품명, 바코드2개, 중량, 납품처, 제조일자, 이력번호, WH_AREA, 테두리 박스, 가로선 3개) 모두 SLCS로 변환 완료.
- **추가 수정**: slcsFeedToMark() 헬퍼 메서드 추가 (WoosimCmd.feedToMark() 대응, SLCS T 명령어)

#### Step 7. 인쇄 블록 5 변환 (합계 라벨)

**Part 1. 분석**
- 메서드: show_wetDetailDialog() 내부 detail_btn_sum.setOnClickListener()
- 범위: 라인 4223~4275
- 용도: 계근 내역 상세 다이얼로그에서 "합계" 버튼 클릭 시 중량 합산 라벨 인쇄
- 주의할 점:
  - 36개 항목 단위로 라벨 1장 인쇄 (6열 x 6행 = 36개)
  - 동적 좌표 계산 (p_weight = 100 * (i%6), p_hight = 10+(i/6*50)-(i/36*300))
  - 루프 내에서 여러 장 인쇄 가능
  - byteStream.reset() 사용하여 새 라벨 시작
- 호출 수: WoosimCmd 약 14회 (initPrinter 2, setPageMode 2, selectTTF 2, setTextStyle 2, PM_setPosition 3, getTTFcode 3, PM_setArea 2, feedToMark 2)

| # | 항목 | 위치 | 크기 | 내용 |
|---|------|------|------|------|
| 1 | 개별 중량 | 동적(p_weight, p_hight) | 40x40 | list_gi_info.get(i).getWEIGHT() |
| 2 | 페이지별 총 중량 | (100, 350) | 60x60 | "N번 총 중량 : XX.X" |

**Part 2. 변환 계획**
- 변환 방식: ByteArrayOutputStream + Woosim → StringBuilder + SLCS
- 사용할 헬퍼 메서드: slcsInit, slcsLabelSize, slcsText, slcsPrint, slcsFeedToMark
- 명령어 매핑:
  | 기존 (Woosim) | 변환 후 (SLCS) |
  |---------------|----------------|
  | WoosimCmd.initPrinter() | slcsInit() |
  | WoosimCmd.PM_setArea() | slcsLabelSize() |
  | WoosimCmd.PM_setPosition() + getTTFcode() | slcsText() |
  | WoosimCmd.PM_printData() | slcsPrint() |
  | WoosimCmd.feedToMark() | slcsFeedToMark() |
- 주의사항:
  - 루프 내 동적 좌표 계산 로직 유지
  - 36개 단위 또는 마지막 항목에서 라벨 인쇄
  - StringBuilder.setLength(0) 또는 새 StringBuilder로 reset 대체

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [x] Part 3: 변환 수행
- [x] Part 4: 컴파일 확인 (BUILD SUCCESSFUL)
- [x] Part 5: 단위테스트 (SLCS 메서드 호출: slcsInit 3, slcsLabelSize 3, slcsText 루프내, slcsPrint 2, slcsFeedToMark 2)
- [x] Part 6: 회귀테스트 (Step 1~6 모두 통과)
  - Step 1: mWoosim 주석 처리 유지 (라인 249, 487, 848)
  - Step 2: SLCS 헬퍼 메서드 8개 존재 (라인 3097~3191)
  - Step 3: 인쇄 블록 1 존재 (라인 1930)
  - Step 4: 인쇄 블록 2 존재 (라인 2368)
  - Step 5: 인쇄 블록 3 존재 (라인 2727)
  - Step 6: 인쇄 블록 4 존재 (라인 2960)
- [x] Part 7: 주석 보강 (용도, 출력 항목 번호, 좌표, 원본 Woosim 대응 주석 추가)
- [x] Part 8: 변경 내용 작성

**Part 8. 변경 내용**:
- **무엇을**: show_wetDetailDialog() 내부 detail_btn_sum.setOnClickListener()의 Woosim 코드 (라인 4223~4275)
- **왜**: Bixolon 프린터는 SLCS 명령어를 사용하므로 Woosim → SLCS 변환 필요
- **어떻게**: ByteArrayOutputStream + WoosimCmd → StringBuilder + slcsInit/slcsLabelSize/slcsText/slcsPrint/slcsFeedToMark 헬퍼 메서드 호출로 변환. 36개 항목 단위 루프 내 동적 좌표 계산 로직 유지, byteStream.reset() → slcsCmd.setLength(0)로 대체.

#### Step 8. Woosim import 제거
- [ ] `import com.woosim.printer.WoosimBarcode;` 삭제
- [ ] `import com.woosim.printer.WoosimCmd;` 삭제
- [ ] `import com.woosim.printer.WoosimImage;` 삭제
- [ ] `import com.woosim.printer.WoosimService;` 삭제

#### Step 9. 빌드 및 통합 테스트
- [ ] 전체 빌드 성공 확인
- [ ] PDA 기기 설치
- [ ] 프린터 연결 테스트
- [ ] 라벨 인쇄 테스트 (이마트)
- [ ] 라벨 인쇄 테스트 (홈플러스)
- [ ] 라벨 인쇄 테스트 (롯데)

---

### Woosim → SLCS 명령어 매핑 (상세)

| Woosim | SLCS | 설명 |
|--------|------|------|
| `WoosimCmd.initPrinter()` | `CB\r\n` | 버퍼 클리어 |
| `WoosimCmd.setPageMode()` | - | 불필요 (SLCS는 기본 페이지 모드) |
| `WoosimCmd.selectTTF()` | `CS13,0\r\n` | 문자셋 설정 (한글) |
| `WoosimCmd.PM_setPosition(x, y)` | SLCS 좌표 사용 | V 명령어에 좌표 포함 |
| `WoosimCmd.getTTFcode(w, h, text)` | `V x,y,K,w,h,0,N,B,N,0,L,0,'text'\r\n` | 텍스트 출력 |
| `WoosimCmd.setTextStyle()` | - | V 명령어에 포함 |
| `WoosimCmd.PM_setArea(x, y, w, h)` | `SW w\r\nSL h\r\n` | 라벨 크기 설정 |
| `WoosimCmd.PM_printData()` | `P1\r\n` | 인쇄 실행 |
| `WoosimCmd.PM_setStdMode()` | - | 불필요 |
| `WoosimCmd.feedToMark()` | - | 불필요 또는 별도 처리 |
| `WoosimBarcode.createBarcode(CODE128, ...)` | `BD x,y,CODE128,4,2,h,0,0,'data'\r\n` | 바코드 생성 |
| `WoosimImage.drawLine(x1, y1, x2, y2, w)` | `SL x1,y1,x2,y2,w\r\n` | 선 그리기 |
| `WoosimImage.drawBox(x, y, w, h, t)` | `SR x,y,w,h,t\r\n` | 박스 그리기 |

---

### 제거 대상 import (4개)

```java
import com.woosim.printer.WoosimBarcode;  // 라인 39
import com.woosim.printer.WoosimCmd;      // 라인 40
import com.woosim.printer.WoosimImage;    // 라인 41
import com.woosim.printer.WoosimService;  // 라인 42
```

---

### 제거 대상 변수/코드

```java
private WoosimService mWoosim = null;     // 라인 249
mWoosim = new WoosimService(mHandler);    // 라인 489, 850
mWoosim.processRcvData(...);              // 라인 845
WoosimService.MESSAGE_PRINTER             // 라인 875
WoosimService.MSR                         // 라인 877
```

---

**최종 수정일**: 2026-01-12
