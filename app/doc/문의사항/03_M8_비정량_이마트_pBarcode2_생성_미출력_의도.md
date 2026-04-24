# 이마트 비정량(M8) pBarcode2 생성은 되나 라벨 미출력 — 설계 의도 확인 필요

## 기본 정보

## 1. 질문 요지 (한 줄)

이마트 비정량(M8) 라벨 출력 시 **pBarcode2(물류코드 기반 바코드)가 코드상 생성은 되지만 실제 라벨에는 인쇄되지 않는다**. 지
워도 상관이 없는가?

---

## 2. 발견 경위

- 실제 `LabelPrintHelper.java` 출력 로직 분석 결과, M8은 pBarcode만 라벨에 인쇄되고 pBarcode2는 변수 생성만 됨을 확인
- 동일 switch-case 내 M3/M4/M9는 pBarcode2까지 라벨에 인쇄되는데 M8만 예외

---

## 3. 기술적 사실 (확정)

| 항목  | 내용                                      | 근거 위치                                                             |
| --- | --------------------------------------- | ----------------------------------------------------------------- |
| 1   | M8 case에서 pBarcode/pBarcode2 두 변수 모두 생성 | `LabelPrintHelper.java:682~685`                                   |
| 2   | pBarcode는 라벨 바코드로 인쇄됨                   | `LabelPrintHelper.java:811` — `slcsBarcode(..., pBarcode)` 무조건 호출 |
| 3   | pBarcode2는 M3/M4/M9에서만 인쇄됨              | `LabelPrintHelper.java:832~843`                                   |
| 4   | M8은 pBarcode2 인쇄 분기에 포함되지 않음            | 동일 위치 if/else 분기                                                  |
| 5   | pBarcode2 로그에는 기록됨                      | `LabelPrintHelper.java:791` — `Log.i(... pBarcode2 ...)`          |


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

### 4.3 이마트 비정량 라벨의 실제 현장 운영

- 이마트 비정량 매장에서 **실제로 바코드가 1개만 찍힌 라벨을 수령하는가**?

---

## 6. 확인 요청 사항

### 🔑 핵심 질문

**이마트 비정량(M8) 라벨에서 `pBarcode2`(물류코드 기반 바코드)를 코드에서 완전히 제거해도 되는가?**

- 현재 코드상 `pBarcode2`는 **생성만 되고 라벨에 인쇄되지 않음**
- 즉 **pBarcode2 생성 코드를 삭제해도 실운영에 영향이 없는가**?
- 아니면 **향후 pBarcode2 인쇄가 필요하므로 남겨두어야 하는가**?

### 참고 세부 질문

1. 이마트 비정량 매장에서 **실제로 바코드가 1개만 찍힌 라벨을 수령하는가**?
2. pBarcode2 (물류코드 기반) 인쇄 기능이 **과거에 사용됐다가 제거**된 것인가, 아니면 **원래부터 미사용**인가?
3. 만약 제거 결정 시 `pBarcode2`, `pBarcodeStr2` 변수 및 관련 로그 출력 코드도 함께 삭제해도 되는가?

---
