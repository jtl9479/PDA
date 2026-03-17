# M0 라벨 인쇄 - Woosim → Bixolon SLCS 이관 가이드

**작성일**: 2026-03-17
**대상**: M0 바코드 타입 (이마트 원료육)

---

## 1. 개요

기존 Woosim 프린터 SDK 기반 라벨 인쇄를 Bixolon 프린터 SLCS 명령어 기반으로 이관하는 과정에서 발생하는 차이점과 수정 방법을 정리한다.

| 항목 | 원본 (Woosim) | 현재 (Bixolon) |
|------|-------------|---------------|
| 소스 파일 | ShipmentActivityOrg.java | LabelPrintHelper.java |
| 프린터 SDK | WoosimCmd / WoosimBarcode | Bixolon SLCS 명령어 |
| 바코드 생성 | SDK가 비트맵 생성 → 프린터 전송 | 프린터 펌웨어가 직접 렌더링 |
| 텍스트 출력 | `WoosimCmd.getTTFcode()` | SLCS `T` 명령 |
| 좌표 설정 | `WoosimCmd.PM_setPosition(x, y)` + 출력 명령 | 출력 명령에 좌표 포함 |

---

## 2. 프린터 명령 구조 차이

### 2.1 텍스트 출력

**Woosim (2단계: 위치 설정 → 출력)**
```java
byteStream.write(WoosimCmd.PM_setPosition(10, 12));           // 위치 설정
byteStream.write(WoosimCmd.getTTFcode(35, 35, "센터명"));      // 텍스트 출력
```

**Bixolon SLCS (1단계: 위치+출력 통합)**
```java
slcsCmd.append(slcsText(10, 12, 35, 35, "센터명"));
// → "T10,12,0,1,1,35,35,0,0,0,'센터명'\r\n"
```

차이점: Woosim은 위치와 출력이 분리되어 있으나, SLCS는 하나의 명령에 좌표가 포함된다. **좌표값은 동일하게 사용 가능하다.**

### 2.2 바코드 출력 (핵심 차이)

**Woosim**
```java
byte[] CODE128 = WoosimBarcode.createBarcode(
    WoosimBarcode.CODE128,  // 바코드 타입
    2,                       // narrow (모듈 폭)
    60,                      // height
    false,                   // HRI 표시 안함
    pBarcode.getBytes()      // 데이터
);
byteStream.write(WoosimCmd.PM_setPosition(80, 170));  // 위치 설정
byteStream.write(CODE128);                             // 바코드 출력
```

**Bixolon SLCS**
```java
slcsCmd.append(slcsBarcode(80, 170, 60, pBarcode));
// → "BD80,170,CODE128,2,4,60,0,0,0,'280001000056610933123456789012'\r\n"
//                        ↑ ↑
//                   narrow wide
```

| 파라미터 | Woosim | Bixolon SLCS | 비고 |
|---------|--------|-------------|------|
| narrow | 2 | 2 | 동일 |
| wide | SDK 내부 자동 처리 | **4 (수동 지정)** | **차이 발생 원인** |
| height | 60 | 60 | 동일 |

### 2.3 바코드 폭 문제 (M0 핵심 이슈)

M0 바코드 데이터: `280001000056610933123456789012` (30자리)

**Woosim**
- SDK 내부에서 narrow=2 기준으로 wide를 자동 계산
- SDK가 비트맵을 생성하여 프린터에 이미지로 전송
- 라벨 폭에 맞게 렌더링됨

**Bixolon SLCS (현재 설정: narrow=2, wide=4)**
```
CODE128은 바(bar)와 스페이스(space)가 narrow/wide 두 종류로 구성된다.
narrow=2, wide=4 설정에서는 최소 단위가 2도트이므로 바코드 전체 폭이 크게 증가한다.

30자리 데이터 + 시작/정지 코드의 예상 폭:
- narrow=2, wide=4 → 약 700~750픽셀 (라벨 576px 초과)
- narrow=1, wide=2 → 약 350~380픽셀 (라벨 576px 이내)

시작 X좌표: 80
라벨 폭: 576
사용 가능 폭: 576 - 80 = 496픽셀

→ narrow=2, wide=4에서는 라벨 초과 → 바코드 미출력 또는 잘림
→ narrow=1, wide=2에서는 라벨 이내 → 정상 출력
```

---

## 3. M0 라벨 레이아웃 비교

### 3.1 라벨 기본 정보

| 항목 | 값 |
|------|-----|
| 라벨 폭 | 576픽셀 |
| 라벨 높이 | 460픽셀 |

### 3.2 M0 출력 요소 (위→아래 순서)

| 순서 | 요소 | Woosim 좌표 | Bixolon 좌표 | 일치 |
|:---:|------|-----------|------------|:---:|
| 1 | 센터명 | (10, 12) 또는 (10, 10) | (10, 12) 또는 (10, 10) | O |
| 2 | 지점명 | (10, 60) | (10, 60) | O |
| 3 | 상품명 | (80, 120) | (80, 120) | O |
| 4 | 우측 상단 바코드 (sBarcode) | (420, 20) | (420, 20) | O |
| 5 | 우측 상단 바코드 번호 | (450, 80) | (450, 80) | O |
| 6 | 메인 바코드 (pBarcode) | (80, 170) | (80, 170) | O |
| 7 | 메인 바코드 번호 | (75, 240) | (75, 240) | O |
| 8 | 중량 | (15, 280) / (175, 280) | (15, 280) / (175, 280) | O |
| 9 | 납품일자 | (15, 328) | (15, 328) | O |
| 10 | 업체코드 | (15, 368) | (15, 368) | O |
| 11 | 업체명 | (15, 408) | (15, 408) | O |
| 12 | WH_AREA | (430, 385) | (430, 385) | O |

**좌표는 모두 동일하다.** 텍스트 출력 위치는 문제 없음.

---

## 4. 수정 방법

### 4.1 바코드 폭 수정 (필수)

**파일**: `LabelPrintHelper.java:159`

```java
// 현재 (바코드 폭 초과로 미출력)
private String slcsBarcode(int x, int y, int height, String data) {
    return "BD" + x + "," + y + ",CODE128,2,4," + height + ",0,0,0,'" + data + "'\r\n";
}
```

**수정안 1: narrow=1, wide=2 (권장)**
```java
private String slcsBarcode(int x, int y, int height, String data) {
    return "BD" + x + "," + y + ",CODE128,1,2," + height + ",0,0,0,'" + data + "'\r\n";
}
```
```
예상 바코드 폭: 약 350~380픽셀
시작 X=80 → 끝 약 430~460픽셀 → 라벨 576 이내 ✓
여유 공간 충분, 30자리 M0 바코드에 안전
```

**수정안 2: narrow=1, wide=3**
```java
private String slcsBarcode(int x, int y, int height, String data) {
    return "BD" + x + "," + y + ",CODE128,1,3," + height + ",0,0,0,'" + data + "'\r\n";
}
```
```
예상 바코드 폭: 약 400~430픽셀 → 라벨 576 이내 ✓
수정안1보다 바코드가 두꺼워 가독성 향상되나 여유 공간 감소
```

### 4.2 바코드 타입별 폭 참고

| 바코드 타입 | 데이터 길이 | narrow=2,wide=4 | narrow=1,wide=2 |
|:---------:|:---------:|:------------------:|:------------------:|
| M0 | 30자리 | 약 700~750px (초과) | 약 350~380px (정상) |
| M1 | 18자리 | 약 430~460px (정상) | 약 215~230px (정상) |
| M3 | 30자리 | 약 700~750px (초과) | 약 350~380px (정상) |
| M4 | 18자리 | 약 430~460px (정상) | 약 215~230px (정상) |

M0, M3처럼 30자리 바코드는 narrow=2에서 반드시 초과한다.

### 4.3 수정 후 검증 방법

1. M0 테스트 데이터로 바코드 스캔 (`002000100567260310B002`)
2. 라벨 출력 확인
3. 출력된 바코드를 바코드 스캐너로 스캔하여 데이터 일치 확인
4. M0원본.jpg와 레이아웃 비교

---

## 5. SLCS 바코드 명령 레퍼런스

### BD 명령 형식
```
BD x, y, barcode_type, narrow, wide, height, rotation, HRI, quiet_zone, 'data'
```

| 파라미터 | 설명 | M0 권장값 |
|---------|------|:--------:|
| x | X 좌표 | 80 |
| y | Y 좌표 | 170 |
| barcode_type | CODE128 | CODE128 |
| narrow | 좁은 바 폭 (도트) | **1** |
| wide | 넓은 바 폭 (도트) | **2** |
| height | 바코드 높이 (도트) | 60 |
| rotation | 회전 (0=0도) | 0 |
| HRI | 바코드 하단 문자 (0=없음) | 0 |
| quiet_zone | 여백 (0=없음) | 0 |
| data | 바코드 데이터 | pBarcode |

---

## 6. 주의사항

1. **slcsBarcode는 공통 메서드**이므로 narrow/wide 변경 시 모든 바코드 타입에 영향을 준다. 바코드 타입별로 다른 설정이 필요하면 파라미터를 추가해야 한다.
2. **Woosim SDK는 비트맵 전송**, Bixolon SLCS는 **프린터 펌웨어 렌더링**이므로 바코드 외형(바 두께, 여백)이 완전히 동일하지는 않다. 기능적 동일성(스캔 가능, 데이터 일치)을 기준으로 검증해야 한다.
3. M0원본.jpg는 Woosim 프린터 출력물이므로 Bixolon 출력과 **외형 차이가 있을 수 있다.**

---

**문서 버전**: 1.0
