# LabelPrintHelper 소스 정리 (로직 영향 없음)

**작성일**: 2026-09-23
**목적**: `LabelPrintHelper.java`(1,491줄)를 `BixolonShipmentActivity` 와 **동일한 절차**로 정리한다. 모든 Step 이 **로직 변경 0건**이며, 실행 코드 동일성을 기계적으로 검증한다.

> **선행 작업**: `app/doc/개발/66_BixolonShipmentActivity_소스정리.md` (Step 1~15 완료, 3,737 → 3,077줄)
> 이 문서는 66 에서 확립한 정리 기준·검증 방법을 그대로 적용한다.

---

## 1. 배경

소스 정리 대상을 **파일이 큰 순서대로 하나씩** 진행하기로 했다(사용자 결정).

| 순 | 파일 | 줄수 | 비고 |
|:--:|------|---:|------|
| — | `ShipmentActivity.java` | 4,460 | **제외** — 진입 불가하나 유지 방침 |
| — | `BixolonShipmentActivity.java` | 3,077 | 개발66 에서 완료 |
| — | `db/DBHandler.java` | 2,082 | SQL 문자열 위주라 성격이 다름, 후순위 |
| **1** | **`print/LabelPrintHelper.java`** | **1,491** | **이 문서** |
| 2 | `ProductionActivity.java` | 699 | 이후 |
| 3 | `MainActivity.java` | 617 | 이후 |

`DBHandler`(2,082줄)가 더 크지만 **SQL 문자열이 대부분**이라 "정리"보다 컬럼 정합성 검증이 필요한 파일이다. 성격이 달라 후순위로 둔다.

---

## 2. 현황 측정

### 2.1 구성

| 항목 | 값 |
|------|---:|
| 전체 | **1,491줄** |
| Javadoc | **207줄 (13.9%)** |
| 메서드 | 15개 |
| 내부 인터페이스 | `PrinterCallback` 1개 |
| 상수 | 27개 (`TAG` 포함) |
| 주석 처리된 코드 | 0줄 |
| 미사용 import | 0개 |
| 미사용 필드 | 0개 |
| 죽은 메서드 | 0개 |

**`BixolonShipmentActivity` 와 달리 죽은 코드가 없다.** 정리 여지는 Javadoc 과 상수 중복에 몰려 있다.

### 2.2 메서드 크기

| 메서드 | 줄수 | 역할 |
|--------|---:|------|
| **`setPrinting`** | **612** | 이마트·비정량 라벨 — `BARCODE_TYPE` 11종 분기 |
| **`setPrintingLotte`** | **264** | 롯데 라벨 — 박스 순번 포함 |
| `setHomeplusPrinting` | 111 | 홈플러스 라벨 |
| `setPrinting_prod` | 90 | 생산 라벨 (미사용 기능) |
| `slcsBitmapText` | 61 | 비트맵 텍스트 렌더링 |
| `loadCustomFont` | 11 | 휴먼울림체 로드 |
| `slcsText`·`slcsBarcode`·`slcsLine`·`slcsBox` | 각 4~5 | SLCS 명령 조립 |
| `slcsInit`·`slcsLabelSize`·`slcsPrint`·`slcsFeedToMark` | 각 3 | 동일 |

상위 2개(`setPrinting` 612 + `setPrintingLotte` 264)가 **876줄로 파일의 59%** 다.

### 2.3 Javadoc 분포

| 줄수 | 위치 | 대상 |
|---:|---:|------|
| **35** | 317 | `setPrinting` |
| **25** | 1201 | `setPrintingLotte` |
| **20** | 1069 | `setHomeplusPrinting` |
| **20** | 22 | 클래스 Javadoc |
| 13 | 965 | `setPrinting_prod` |
| 11 | 143 | `slcsText` |
| 10 | 241 | `slcsBitmapText` |
| 10 | 190 | `slcsBox` |
| 10 | 175 | `slcsLine` |
| 9 | 160 | `slcsBarcode` |
| 7 | 215·132·121·96 | `slcsFeedToMark`·`slcsLabelSize`·`slcsInit`·`PrinterCallback` |

`slcs*` 계열은 **본문이 3~5줄인데 Javadoc 이 7~11줄**이다. 개발66 Step 13 에서 같은 상황을 1줄로 줄인 전례가 있다.

### 2.4 상수 27개 — `Common` 중복 12개

| 상수 | 사용 | `Common` 에 동일 상수 |
|------|---:|:---:|
| `SEARCH_TYPE_EMART`·`SEARCH_TYPE_LOTTE` | 1·1 | ⭕ |
| `MEAT_CENTER_STORE_CODE`·`KILKOY_PACKER_CODE` | 3·1 | ⭕ |
| `ITEM_TYPE_W`·`HW`·`S`·`J`·`B` | 2·1·1·2·2 | ⭕ |
| `CENTER_NAME_TRD`·`WET`·`ET` | 1·1·3 | ⭕ |
| `COMPANY_CODE`·`COMPANY_NAME` | 1·4 | ❌ |
| `MEAT_CENTER_CODE`·`LOGIS_CODE_DEFAULT` | 2·2 | ❌ |
| `BARCODE_TYPE_M0`~`P0` (11개) | 1~9 | ❌ |

**미사용 상수는 0개다.** 개발66 Step 2 에서 `BixolonShipmentActivity` 의 같은 상수 15개를 지웠는데, 그것들은 이 파일로 기능이 옮겨간 뒤 남은 껍데기였다. 여기 있는 것은 **전부 실제로 쓰인다.**

---

## 3. 작업 원칙

- 문서에 명시된 step 만 진행하고, 다음 step 은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선·리팩토링을 임의로 수행하지 않는다
- **기존 기능과 100% 동일하게 동작해야 한다**

### 추가 제약 (이 문서 한정)

1. **라벨 출력물이 바뀌면 안 된다.** SLCS 명령 문자열은 한 글자도 달라지지 않아야 한다. 매 Step 마다 **문자열 리터럴 전수 비교**를 수행한다.
2. **`setPrinting`(612줄)·`setPrintingLotte`(264줄) 본문은 이 문서에서 건드리지 않는다.** 분기 구조 변경은 실기기 검증이 필요하므로 범위 밖이다.
3. **로그 문자열을 바꾸지 않는다.** 개발66 에서 기계 치환이 `Log` 문자열 내부까지 오염시켜 원복한 이력이 있다(개발66 §10 재발 방지).
4. **생산 라벨(`setPrinting_prod`)은 미사용이지만 삭제하지 않는다.** `searchType=7` 은 UI 가 막혀 있을 뿐 코드는 유지 방침이다([[project-production-unused]]).

---

## 4. Step 계획

### Step 1 — Javadoc 단순화 (207 → 약 80줄)

**기준** (개발66 §15.1·§16.1 과 동일)

| 유지 | 삭제 |
|------|------|
| 한 줄 요약 | "처리 흐름" 단계 나열 — 코드에 이미 있음 |
| 코드로 안 드러나는 이유·주의 | 분기 목록 — 코드가 더 정확함 |
| 의미가 불명확한 `@param` | 파라미터명이 이미 말하는 `@param` |

**대상**

| 대상 | 현재 | 목표 | 비고 |
|------|---:|---:|------|
| `setPrinting` | 35 | ~8 | `BARCODE_TYPE` 분기 목록 제거, `@param` 유지 |
| `setPrintingLotte` | 25 | ~7 | 박스 순번 주의사항은 유지 |
| `setHomeplusPrinting` | 20 | ~6 | |
| 클래스 Javadoc | 20 | ~7 | |
| `setPrinting_prod` | 13 | ~5 | 미사용 기능 표기 유지 |
| `slcsText`·`slcsBarcode`·`slcsLine`·`slcsBox` | 40 | ~4 | **본문보다 긴 Javadoc** → 각 1줄 |
| `slcsInit`·`slcsLabelSize`·`slcsPrint`·`slcsFeedToMark` | 28 | ~4 | 각 1줄 |
| `slcsBitmapText` | 10 | ~5 | 비트맵 렌더링 주의사항 유지 |
| `PrinterCallback` | 7 | ~4 | |

**주의**: `slcsBarcode` 본문의 `// B1 x,y,barcode_type,...` 같은 **SLCS 명령 형식 주석은 유지**한다. 프로토콜 설명이라 코드만 봐서는 알 수 없다.

**검증**: 문자열 리터럴 전수 비교 → 동일 / 실행 코드 줄 집합 동일 / 빌드

---

### Step 2 — `Common` 중복 상수 12개 정리

`Common` 에 같은 이름·같은 값의 상수가 이미 있다(개발66 Step 7 에서 이관).

| 상수 | 이 파일 사용 |
|------|---:|
| `SEARCH_TYPE_EMART`·`SEARCH_TYPE_LOTTE` | 2 |
| `MEAT_CENTER_STORE_CODE`·`KILKOY_PACKER_CODE` | 4 |
| `ITEM_TYPE_W`·`HW`·`S`·`J`·`B` | 8 |
| `CENTER_NAME_TRD`·`WET`·`ET` | 5 |
| **합계** | **19곳** |

**작업**: 자체 선언 12개 삭제 → 참조 19곳을 `Common.<상수>` 로 치환.

**치환 시 주의** (개발66 Step 7 에서 확인된 사항)
- 정규식에 `(?<![\w."])` 경계를 두어 **문자열 리터럴 내부를 치환하지 않는다**
- 치환 후 **`Common.Common.` 중복 접두** 검사

**검증**: 리터럴 전수 비교(값 12개만 감소해야 함) / `Common.Common.` 0건 / 빌드

---

### Step 3 — 남은 상수 15개 판단

`Common` 에 없는 15개다.

| 상수 | 성격 | 판단 |
|------|------|------|
| `BARCODE_TYPE_M0`~`P0` (11) | **라벨 레이아웃 분기 전용** | 이 파일에 유지 — 라벨 출력에서만 쓰임 |
| `COMPANY_CODE`·`COMPANY_NAME` | 회사 정보 | **`Common` 이관 검토** — `ShipmentActivity` 에도 중복 |
| `MEAT_CENTER_CODE`·`LOGIS_CODE_DEFAULT` | 업무 코드 | `Common` 이관 검토 |

`BARCODE_TYPE_*` 는 개발66 §10 에서 정한 배치 기준상 **"특정 영역 전용 로직"** 에 해당해 이 파일이 맞다. 나머지 4개는 별건으로 판단한다.

**이 Step 은 판단만 하고, 이관은 사용자 승인 후 진행한다.**

---

### Step 4 — 멤버 재배치

개발66 Step 14 와 동일한 방식으로 종류별 섹션을 만든다.

```
1 상수 - 회사/업무 코드
2 상수 - BARCODE_TYPE
3 필드 - 폰트
4 인터페이스 - PrinterCallback
5 SLCS 명령 조립 (slcs* 8개 + slcsBitmapText)
6 폰트 로드
7 라벨 인쇄 - setPrinting / setPrinting_prod / setHomeplusPrinting / setPrintingLotte
```

**검증** (개발66 §17.4 의 4가지)
1. 실행 코드 줄 집합 다중집합 비교 → 동일
2. 문자열 리터럴 전수 비교 → 동일
3. 멤버 선언 집합 → 동일
4. **필드 초기화 상호 의존 검사** → 0건이어야 순서 무관

---

### Step 5 — 지역변수 강등 검토

멤버변수 중 한 메서드에서만 쓰이는 것을 찾는다.

현재 필드는 `TAG`·상수 26개·`customFont` 뿐이라 **후보가 거의 없을 것으로 예상**되나, 개발66 Step 15 절차대로 확인한다.

**판정 기준** (개발66 §18.2)
- 람다·익명 클래스에서 읽는데 **대입이 그 정의보다 뒤**면 제외 (실행 순서가 바뀜)
- 다른 메서드에서도 쓰면 제외

---

## 5. 범위 밖 (별건)

| 항목 | 사유 |
|------|------|
| `setPrinting`(612줄) 분기 분해 | 실행 코드 재작성 — 실기기 검증 필요 |
| `BixolonShipmentActivity` 합계 라벨 SLCS 이관(~108줄) | 실행 코드 이동 — 별도 문서 |
| `slcsBarcode` 본문 차이(`BD` vs `B1`) | 개발66 §15.2 에서 확인. Activity 쪽은 삭제 완료, 이 파일 것이 정본 |
| 로그 정리 | 실기기 테스트 판정 기준 |

---

## 6. 위험과 대응

| 위험 | 대응 |
|------|------|
| **라벨 출력물이 달라짐** | 매 Step 문자열 리터럴 전수 비교. 1개라도 다르면 중단 |
| 기계 치환이 로그 문자열 오염 | 정규식 경계 `(?<![\w."])` + 리터럴 비교로 이중 확인 |
| 블록 주석 `/* */` 오삭제 | 개발66 Step 15 에서 겪은 실패. **선언 줄만 삭제하고 주석은 건드리지 않는다** |
| 작업 중 파일 손상 | Step 착수 전 스크래치패드에 백업본 복사 |

---

## 7. 체크리스트

| Step | 작업 | 상태 |
|------|------|:----:|
| 1 | Javadoc 단순화 (207 → 약 80줄) | ⏳ 대기 |
| 2 | `Common` 중복 상수 12개 정리 (참조 19곳) | ⏳ 대기 |
| 3 | 남은 상수 15개 판단 | ⏳ 대기 |
| 4 | 멤버 재배치 | ⏳ 대기 |
| 5 | 지역변수 강등 검토 | ⏳ 대기 |

---

## 8. 예상 결과

| 항목 | 현재 | 예상 |
|------|---:|---:|
| 전체 | 1,491줄 | **약 1,340줄** |
| Javadoc | 207줄 | 약 80줄 |
| 자체 상수 | 27개 | 15개 |
| 실행 코드 | 변화 없음 | **동일** |

줄 감소는 약 150줄(10%)로 크지 않다. 목적은 **중복 제거와 배치 정리**이며, 개발66 과 마찬가지로 실행 코드는 한 줄도 바뀌지 않는다.

---

## 관련 문서

- `app/doc/개발/66_BixolonShipmentActivity_소스정리.md` — 정리 기준·검증 방법의 원본. Javadoc 기준(§15.1), 상수 배치 기준(§14.6), 재배치 검증 4가지(§17.4), 지역변수 판정(§18.2)
- `app/doc/일정/2026-09-22.md` — 개발66 진행 기록 및 판단 근거
- `app/doc/라벨/` — 라벨 레이아웃·규격
- `app/doc/기능/11_바코드스캔후_전체프로세스.md`
