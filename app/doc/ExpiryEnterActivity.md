# ExpiryEnterActivity 문서

## 1. 개요

**파일**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\ExpiryEnterActivity.java`
**총 라인 수**: 114줄
**목적**: 소비기한(제조일자) 입력을 담당하는 Activity

이 Activity는 ShipmentActivity로부터 무게와 제조일자 범위 정보를 받아 화면에 표시하고, 사용자가 실제 제조일자를 입력하면 유효성을 검증한 후 결과를 반환하는 역할을 수행합니다.

## 2. 클래스 구조

### 2.1 클래스 선언
```java
public class ExpiryEnterActivity extends AppCompatActivity
```

### 2.2 멤버 변수

| 변수명 | 타입 | 용도 |
|--------|------|------|
| `enteredWeight` | EditText | 무게 표시 필드 (읽기 전용) |
| `makingDateFrom` | EditText | 제조일자 시작 범위 (읽기 전용) |
| `makingDateTo` | EditText | 제조일자 종료 범위 (읽기 전용) |
| `vibrator` | Vibrator | 입력 오류 시 진동 피드백 |
| `TAG` | String | 로그 태그 ("ExpiryEnterActivity") |

### 2.3 레이아웃 파일
- `R.layout.expiry_enter`

## 3. 주요 메서드 분석

### 3.1 onCreate() (27-113줄)

**Intent로부터 데이터 수신** (38-54줄)
```java
Intent intentB = getIntent();
String weightReceivedStr = intentB.getStringExtra("weightStrKey");
Double weightReceivedDbl = intentB.getDoubleExtra("weightDblKey", 0);
String makingFromReceived = intentB.getStringExtra("makingFromKey");
String makingToReceived = intentB.getStringExtra("makingToKey");
```

**수신 데이터**:
- `weightStrKey`: 무게 (문자열)
- `weightDblKey`: 무게 (Double)
- `makingFromKey`: 제조일자 시작 범위
- `makingToKey`: 제조일자 종료 범위

**UI 초기화** (56-69줄)
- 수신한 데이터를 EditText에 표시
- 무게와 날짜 범위 필드를 읽기 전용으로 설정 (`setClickable(false)`, `setFocusable(false)`)

### 3.2 날짜 유효성 검증 로직 (84-101줄)

```java
String checkDate = "20" + enteredMakingDateSend;
SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
dateFormat.setLenient(false);
dateFormat.parse(checkDate);
```

**검증 프로세스**:
1. 입력된 날짜 앞에 "20" 접두사 추가 (예: "250131" → "20250131")
2. `yyyyMMdd` 형식으로 파싱 시도
3. `setLenient(false)`로 엄격한 날짜 검증 수행
4. 유효하지 않으면 `ParseException` 발생

**검증 실패 시**:
- Toast 메시지 표시: "날짜를 알맞은 형식으로 입력하세요."
- 1초간 진동
- 포커스를 날짜 입력 필드로 이동

## 4. UI 리스너

### 4.1 저장 버튼 (71-103줄)

**동작**:
1. 입력된 제조일자를 가져옴 (`enteredMakingDate`)
2. 날짜 형식 유효성 검증
3. 검증 성공 시:
   - Intent에 데이터 설정:
     - `enteredWeightSend`: 무게 (문자열)
     - `enteredWeightDblSend`: 무게 (Double)
     - `enteredMakingDateSend`: 입력된 제조일자
   - `RESULT_OK`와 함께 Activity 종료

### 4.2 닫기 버튼 (106-111줄)

**동작**:
- Activity 종료 (`finish()`)
- 데이터 전달 없이 종료

## 5. 코드 위치 참조

### 5.1 주요 코드 블록

| 기능 | 라인 번호 |
|------|----------|
| 클래스 선언 | 16 |
| 멤버 변수 선언 | 18-24 |
| onCreate 시작 | 27 |
| Intent 데이터 수신 | 38-54 |
| UI 초기화 | 56-69 |
| 저장 버튼 리스너 | 71-103 |
| 날짜 검증 로직 | 84-101 |
| 닫기 버튼 리스너 | 106-111 |

### 5.2 주요 로그 메시지

```java
Log.i(TAG, "메인에서 받아온 값 체크 weightReceivedStr: " + weightReceivedStr);
Log.i(TAG, "메인에서 받아온 값 체크 weightReceivedDbl: " + weightReceivedDbl);
Log.i(TAG, "메인에서 받아온 값 체크 makingFromReceived: " + makingFromReceived);
Log.i(TAG, "메인에서 받아온 값 체크 makingToReceived: " + makingToReceived);
Log.i(TAG, "체크데이트 확인: " + checkDate);
```

### 5.3 데이터 흐름

```
ShipmentActivity
    → [Intent] weightStr, weightDbl, makingFrom, makingTo
    → ExpiryEnterActivity
    → [사용자 입력] 제조일자
    → [검증] SimpleDateFormat
    → [Intent] weightStr, weightDbl, enteredMakingDate
    → ShipmentActivity
```

## 6. 특이사항

1. **날짜 입력 형식**: YYMMDD 형식으로 입력받아 "20" 접두사를 추가하여 YYYYMMDD로 변환 (2000년대만 지원)
2. **읽기 전용 필드**: 무게와 날짜 범위는 수정 불가
3. **Vibrator 피드백**: 잘못된 날짜 입력 시 1초간 진동으로 사용자에게 피드백 제공
4. **무게 데이터 중복**: String과 Double 두 가지 형식으로 무게를 전달
