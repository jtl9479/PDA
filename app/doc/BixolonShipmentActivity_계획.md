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

### 단계별 작업 계획 (체크리스트) - 방법 B

> **방법 B**: 사용 코드 먼저 변환 → 마지막에 import 삭제

#### Step 1. mWoosim 변수 및 관련 코드 제거 ✅ 완료
- [x] `private WoosimService mWoosim = null;` 삭제 (라인 249) → 주석으로 대체
- [x] `mWoosim = new WoosimService(mHandler);` 삭제 (라인 487, 848) → 주석으로 대체
- [x] `mWoosim.processRcvData(...)` 삭제 (라인 843) → 주석으로 대체
- [x] `WoosimService.MESSAGE_PRINTER` 관련 코드 삭제 (라인 873) → 주석으로 대체
- [x] `WoosimService.MSR` 관련 코드 삭제 → 상위 블록과 함께 제거

#### Step 2. SLCS 헬퍼 메서드 생성
- [ ] 텍스트 출력 메서드 생성 (WoosimCmd.getTTFcode 대체)
- [ ] 바코드 생성 메서드 생성 (WoosimBarcode.createBarcode 대체)
- [ ] 선/박스 그리기 메서드 생성 (WoosimImage 대체)
- [ ] 라벨 초기화 메서드 생성 (WoosimCmd.initPrinter 대체)
- [ ] 인쇄 실행 메서드 생성 (WoosimCmd.PM_printData 대체)

#### Step 3. 인쇄 블록 1 변환 (이마트 기본 라벨)
- [ ] 라인 1938~ 분석
- [ ] Woosim 명령어 → SLCS 변환
- [ ] 컴파일 확인

#### Step 4. 인쇄 블록 2 변환 (이마트 확장 라벨 E0~E3)
- [ ] 라인 2385~ 분석
- [ ] Woosim 명령어 → SLCS 변환
- [ ] 컴파일 확인

#### Step 5. 인쇄 블록 3 변환 (홈플러스 라벨)
- [ ] 라인 2863~ 분석
- [ ] Woosim 명령어 → SLCS 변환
- [ ] 컴파일 확인

#### Step 6. 인쇄 블록 4 변환 (롯데 라벨)
- [ ] 라인 3096~ 분석
- [ ] Woosim 명령어 → SLCS 변환
- [ ] 컴파일 확인

#### Step 7. 인쇄 블록 5 변환 (기타 라벨)
- [ ] 라인 4240~ 분석
- [ ] Woosim 명령어 → SLCS 변환
- [ ] 컴파일 확인

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
