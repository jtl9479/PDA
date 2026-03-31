# WH_AREA null 체크 OR 연산자 버그

## 발견일
2026-03-10

## 현상
- WH_AREA 값이 `null`로 내려오면 **NullPointerException**으로 앱 크래시 발생
- WH_AREA 값이 `""`(빈 문자열)이면 빈 값이 라벨에 출력됨

## 원인

### 문제 코드
```java
if(whArea != null || !whArea.equals("")) {
```

`||`(OR) 연산자를 사용하여 조건이 잘못됨:
- `whArea = null` → `false || null.equals("")` → **NullPointerException** 발생
- `whArea = ""` → `true || (평가안함)` → true → 빈 문자열 출력

### 발생 위치 (총 12곳)

#### LabelPrintHelper.java (4곳)
| 줄 | 위치 |
|----|------|
| 789 | setPrinting() - 기본 라벨 |
| 848 | setPrinting() - 미트센터 +공장코드 라벨 |
| 901 | setPrinting() - 미트센터 라벨 |
| 1421 | setPrintingLotte() - 롯데 라벨 |

#### ShipmentActivity.java (4곳)
| 줄 | 위치 |
|----|------|
| 2575 | setPrinting() - 기본 라벨 |
| 2656 | setPrinting() - 미트센터 +공장코드 라벨 |
| 2733 | setPrinting() - 미트센터 라벨 |
| 3139 | setPrintingLotte() - 롯데 라벨 |

#### ShipmentActivityOrg.java (4곳)
| 줄 | 위치 |
|----|------|
| 2329 | setPrinting() - 기본 라벨 |
| 2411 | setPrinting() - 미트센터 +공장코드 라벨 |
| 2488 | setPrinting() - 미트센터 라벨 |
| 2873 | setPrintingLotte() - 롯데 라벨 |

## 원래부터 있던 버그인가?

**YES** - 원본(ShipmentActivityOrg.java)에도 동일한 코드 존재

## 수정 방안

`||`를 `&&`로 변경:

```java
// 수정 전
if(whArea != null || !whArea.equals("")) {

// 수정 후
if(whArea != null && !whArea.equals("")) {
```

| whArea 값 | 수정 전 | 수정 후 |
|-----------|--------|--------|
| `null` | NullPointerException | false → 출력 안 함 |
| `""` | true → 빈 값 출력 | false → 출력 안 함 |
| `"A1"` | true → 정상 출력 | true → 정상 출력 |

> 12곳 전부 동일하게 수정 필요

## 상태
- [ ] 미수정
