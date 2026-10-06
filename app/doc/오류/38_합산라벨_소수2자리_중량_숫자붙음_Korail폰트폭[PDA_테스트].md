# 합산 라벨 소수 2자리 중량 숫자 붙음 (Korail 폰트 폭)

## 발견일
2026-10-02

## 에러 발생 시나리오

```
1. 출하 계근 화면에서 소수 2자리 중량(예: 12.34)으로 계근
2. 계근 내역 팝업 → 합계(detail_btn_sum) 버튼 클릭
3. 합산 라벨 인쇄 시 개별 중량 6열 × 6행 배치
4. 소수 2자리 값이 옆 칸 숫자와 여백 없이 붙어 출력됨
```

---

## 현상
- 합산 라벨의 개별 중량 중 5글자 값(`12.34`)이 다음 칸 숫자와 맞닿아 구분이 안 됨
- 4글자 값(`12.3`)은 정상
- **열 간격 100도트 = 5글자 폭 100도트 → 여백 0**

## 원래부터 있던 버그인가?

**NO - 배치 좌표는 원본과 동일하나, 글자 렌더링이 Woosim 프린터 TTF → Korail.ttf 비트맵으로 바뀌면서 글자 폭이 달라져 발생**

```java
// 원본 ShipmentActivity.java:3881~3885
p_hight = 10+(i/6*50)-(i/36*300);
p_weight = 100 * (i%6);
byteStream.write(WoosimCmd.getTTFcode(40, 40, list_gi_info.get(i).getWEIGHT()));
```

## 원인

### 문제 1 (주요): 40 크기 Korail 폰트 5글자 폭이 열 간격(100도트)과 같음

#### 코드 위치
- `BixolonShipmentActivity.java` : 2237~2242줄

#### 현재 문제 코드
```java
p_hight = 10 + (i / 6 * 50) - (i / 36 * 300);
p_weight = 100 * (i % 6);                     // ★ 열 간격 100도트
sumText.write(labelPrintHelper.bitmapText(p_weight, p_hight, 40, list_gi_info.get(i).getWEIGHT()));  // ★ 40 크기
```

#### 발생 시나리오
Korail.ttf 실측 폭 (size 40, PIL 측정)

| 값 | 폭 | 열 간격 | 여백 |
|:---:|:---:|:---:|:---:|
| `12.3` | 78 | 100 | 22 |
| `12.34` | 100 | 100 | **0** |
| `123.45` | 122 | 100 | **-22** |

`slcsBitmapText`는 `setFakeBoldText(true)`도 적용하므로 실제 출력은 측정값보다 약간 더 넓다.

## 상세 흐름

1. **합계 버튼 클릭** (`detail_btn_sum`)
   - `list_gi_info` 각 항목의 중량을 `bitmapText(x, y, 40, weight)`로 비트맵 생성
2. **좌표 배치** (`p_weight = 100 * (i % 6)`)
   - 열 시작 x = 0, 100, 200, 300, 400, 500
3. **문제 발생** (중량 문자열 5글자 이상)
   - **경로 A**: 4글자 → 여백 22도트, 정상
   - **경로 B**: 5글자 → 여백 0, 숫자 붙음

## 영향 범위
- 계근 내역 합산 라벨의 개별 중량 표기 (총 중량 줄은 별도 좌표로 영향 없음)
- `BixolonShipmentActivity.java` (ShipmentActivity.java는 구버전 진입불가 파일로 제외)

## 수정 방안

### 수정 1: 개별 중량 폰트 크기 40 → 35

```java
sumText.write(labelPrintHelper.bitmapText(p_weight, p_hight, 35, list_gi_info.get(i).getWEIGHT()));
```

size 35 실측: `12.34` = 91도트(여백 9), `123.4` = 91도트, `123.45` = 111도트(100kg 이상·소수 2자리는 여전히 초과)

> 배치(6열 × 6행, 36건 단위)와 총 중량 줄은 원본 그대로 유지한다. 열 간격 확대는 6열이 라벨 폭(576)을 넘어 한 장당 건수가 바뀌므로 채택하지 않음.

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/개발/71_합산라벨_개별중량_폰트축소[합산라벨_소수2자리_중량_숫자붙음].md`
- `app/doc/개발/70_테스트후_수정리스트.xlsx` (A1)
