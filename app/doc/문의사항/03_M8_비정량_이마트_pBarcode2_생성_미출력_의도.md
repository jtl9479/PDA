# 이마트 비정량(M8) pBarcode2 생성은 되나 라벨 미출력 — 설계 의도 확인 필요

## 기본 정보

| 항목 | 내용 |
|------|------|
| **순번** | 03 |
| **발견 일자** | 2026-04-24 |
| **상태** | ⏳ 대기 (답변 필요) |
| **우선순위** | 중간 |
| **확인 대상** | 이마트 출하/물류 담당자 / 라벨 규격 담당자 / 원 개발자 |
| **관련 JSP/소스** | `LabelPrintHelper.java:671~687`, `LabelPrintHelper.java:811~843` |

---

## 1. 질문 요지 (한 줄)

이마트 비정량(M8) 라벨 출력 시 **pBarcode2(물류코드 기반 바코드)가 코드상 생성은 되지만 실제 라벨에는 인쇄되지 않는다**. 이것이 의도된 현재 운영 방식인가, 레거시 누락인가, 향후 출력 요구사항의 미구현 상태인가?

---

## 2. 발견 경위

- 2026-04-24 비정량 테스트 데이터 작성 준비 중 이마트 비정량(M8) 바코드 형식 분석
- `app/doc/기능/07_바코드_형식_가이드.md`에서 M8의 pBarcode/pBarcode2 두 개 바코드 형식 확인
- 실제 `LabelPrintHelper.java` 출력 로직 분석 결과, M8은 pBarcode만 라벨에 인쇄되고 pBarcode2는 변수 생성만 됨을 확인
- 동일 switch-case 내 M3/M4/M9는 pBarcode2까지 라벨에 인쇄되는데 M8만 예외

---

## 3. 기술적 사실 (확정)

| 항목 | 내용 | 근거 위치 |
|------|------|----------|
| 1 | M8 case에서 pBarcode/pBarcode2 두 변수 모두 생성 | `LabelPrintHelper.java:682~685` |
| 2 | pBarcode는 라벨 바코드로 인쇄됨 | `LabelPrintHelper.java:811` — `slcsBarcode(..., pBarcode)` 무조건 호출 |
| 3 | pBarcode2는 M3/M4/M9에서만 인쇄됨 | `LabelPrintHelper.java:832~843` |
| 4 | M8은 pBarcode2 인쇄 분기에 포함되지 않음 | 동일 위치 if/else 분기 |
| 5 | pBarcode2 로그에는 기록됨 | `LabelPrintHelper.java:791` — `Log.i(... pBarcode2 ...)` |
| 6 | M8의 pBarcodeStr2 필드 순서가 pBarcode2와 불일치 | 아래 4번 섹션 상세 |

### 3.1 pBarcode / pBarcode2 실제 코드 (M8)

```java
case "M8":
    // 이마트 비정량 납품분
    pBarcode  = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode ;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
    break;
```

### 3.2 타 바코드 타입 비교

| 타입 | pBarcode 인쇄 | pBarcode2 인쇄 |
|:----:|:-----------:|:-------------:|
| M0 | ✅ | ❌ (생성만, M8과 동일 패턴) |
| M1 | ✅ | ❌ |
| **M3** | ✅ | ✅ **라벨에 2개 인쇄** |
| **M4** | ✅ | ✅ **라벨에 2개 인쇄** |
| **M8** | ✅ | ❌ **생성만, 인쇄 안 됨** |
| **M9** | ✅ | ✅ **라벨에 2개 인쇄** |
| E0~E3 | ✅ | ❌ |

→ M8이 M0/M1/E0~E3와 같은 "1개 인쇄" 그룹에 속함. M3/M4/M9와는 다름.

---

## 4. 비즈니스 의문점

### 4.1 pBarcode2 생성의 목적

- 인쇄 안 할 것이면 왜 생성하는가? (로그용만은 비효율)
- M3/M4/M9와 같은 "2바코드 인쇄" 타입에 M8이 원래 포함되었다가 **누락된 것은 아닌가**?
- 이마트 비정량 물류센터에서 **물류코드 기반 바코드 인쇄가 원래 요구됐지만 현재는 하지 않는** 운영 변경이 있었나?

### 4.2 pBarcodeStr2 순서 불일치 (추가 발견)

```java
pBarcode2    = EMARTLOGIS_CODE(6) + IMPORT_ID_NO(12) + 중량(6) + 회사(2)       ← 바코드 데이터
pBarcodeStr2 = EMARTLOGIS_CODE(6) + 중량(6) + 회사(2) + IMPORT_ID_NO(12)       ← 텍스트 표현
                                    ↑ 순서가 pBarcode2와 다름!
```

- 일반적으로 pBarcodeStr은 pBarcode를 공백 구분해서 보여주는 용도
- M8의 pBarcodeStr2는 pBarcode2와 필드 순서가 달라 **의도된 차이인지 복사/붙여넣기 실수인지 불명**
- 현재 인쇄 안 되므로 실질 영향 없지만, 향후 pBarcode2 출력 요구 시 결함 노출 가능

### 4.3 이마트 비정량 라벨의 실제 현장 운영

- 이마트 비정량 매장에서 **실제로 바코드가 1개만 찍힌 라벨을 수령하는가**?
- 물류센터에서는 별도 바코드가 필요하지 않은가?
- M3/M4가 2개 인쇄되는 이유와 비교했을 때 M8이 1개만 인쇄되는 이유?

---

## 5. 가능한 해석

| 해석 | 내용 | 가능성 |
|:----:|------|:----:|
| A | 의도된 설계: 이마트 비정량은 물류센터에서 바코드 인쇄 불필요, 주문용만 필요 | 중간 |
| B | 레거시 누락: 원래 M3/M4/M9처럼 2개 인쇄가 요구됐으나 M8 분기만 코드에서 빠짐 | **높음** (switch-case 패턴 일관성 저하) |
| C | 운영 변경 이력: 과거에는 2개 인쇄 → 현재는 1개로 축소 → 코드만 남음 | 중간 |
| D | 향후 요구사항 대비: pBarcode2 생성 로직을 미리 구현, 실사용은 미정 | 낮음 |
| E | 복사/붙여넣기 후 수정 누락: M3/M4/M9의 switch-case를 복사한 후 인쇄 분기만 제거, 변수 생성은 제거 못 함 | 낮음 |

---

## 6. 확인 요청 사항

1. **이마트 비정량(M8) 라벨 표준 규격**에 몇 개의 바코드가 필요한가? 현재 운영(1개)이 정확한가?
2. **pBarcode2 (물류코드 기반)가 원래 라벨에 인쇄되어야 하는데 누락된 것은 아닌가**?
3. 만약 인쇄가 필요하다면 라벨 위치, 크기, 텍스트 표시 방식은?
4. **pBarcodeStr2의 필드 순서 불일치**가 의도인가, 버그인가?
5. 원본 Oracle 시절 M8 라벨 출력 로직과 현재가 동일한가?

---

## 7. 조치 방향 (답변 전까지)

- **답변 수신 전**: 현재 코드 유지 (pBarcode만 인쇄, pBarcode2 생성만)
- **Step 3 실기기 테스트 시**: 실제 M8 라벨 출력 결과 사진 확보하여 현장 운영 상태 기록
- **답변 수신 후 시나리오별 조치**:
    - 해석 A 확정 시: 본 문의 종결, pBarcode2 생성 코드는 로그 용도로 유지
    - 해석 B 확정 시: 개발 가이드 신규 작성 (개발NN) → M8에 pBarcode2 인쇄 분기 추가 + pBarcodeStr2 순서 정정 + 라벨 위치/크기 정의
    - 해석 C 확정 시: pBarcode2 생성 코드 제거(코드 정리) 또는 그대로 유지
    - 해석 D 확정 시: 향후 요구사항 구체화 시점에 재검토 (현재 미결)

---

## 8. 답변 (수신 시 작성)

| 항목 | 내용 |
|------|------|
| 답변 일자 | |
| 답변자 | |
| 결론 | |
| 후속 조치 | |

---

## 9. 관련 문서

- `app/doc/기능/07_바코드_형식_가이드.md` — 바코드 타입별 형식 정리 (M8 섹션 포함)
- `app/doc/문의사항/01_비정량_출하대상_타입구분_J코드_포함여부.md` — 관련 문의(비정량 업무 규칙)
- `app/doc/문의사항/02_S_BARCODE_INFO_vs_B_ITEM_동명컬럼_중복여부.md` — 관련 문의(비정량 스키마)
- Java 코드: `app/src/main/java/com/rgbsolution/highland_emart/print/LabelPrintHelper.java`
  - L69: `BARCODE_TYPE_M8` 상수
  - L671~687: M8 switch-case
  - L791: pBarcode2 로그 출력
  - L811: pBarcode 라벨 인쇄 (무조건)
  - L832~843: pBarcode2 인쇄 분기 (M3/M4/M9만)
- 관련 Activity: `BixolonShipmentActivity.java` L181~192 (BARCODE_TYPE 상수)
- 원본 참조: `D:\PDA\PDA-INNO(원본)\app\src\main\java\...\LabelPrintHelper.java` — 원본 시절 M8 라벨 출력 로직 확인 필요
