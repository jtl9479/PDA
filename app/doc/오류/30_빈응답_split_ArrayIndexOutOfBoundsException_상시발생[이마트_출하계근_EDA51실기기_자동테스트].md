# 서버 빈 응답 파싱 시 ArrayIndexOutOfBoundsException 상시 발생 (데이터 0건 조회마다 재현)

## 발견일
2026-07-15

## 에러 발생 시나리오

```
1. EDA51 실기기에서 BixolonShipmentActivity 진입 전, 조회일자를 20260715(데이터 없는 날짜)로 설정
2. 이마트 출하대상받기 실행 → search_shipment.jsp 호출
3. 서버가 정상 응답하나 조회 결과 0건이므로 receiveData가 빈 문자열("")로 반환됨
4. ProgressDlgShipSearch.onPostExecute()에서 receiveData.split(";;") → 길이 1 배열(["" ]) 생성
5. "".split("::", -1) → 다시 길이 1 배열 생성
6. temp[1] 접근 시 ArrayIndexOutOfBoundsException 발생 (catch로 방어되어 크래시는 없음)
7. 연쇄로 ProgressDlgBarcodeSearch도 동일 패턴으로 예외 발생
```

---

## 현상
- 데이터 0건 조회(정상 케이스)마다 매번 ArrayIndexOutOfBoundsException 예외가 발생함(로그에만 기록, 크래시 없음)
- logcat 실측:
  ```
  D/ProgressDlgShipSearch: ============== 출하리스트 조회조건 :  AND D.회사코드 = '20' AND D.출고일자 = '20260715' AND D.창고코드 = 'W301'
  D/ProgressDlgShipSearch: receiveData :
  D/ProgressDlgShipSearch: result's Count : 1
  E/ProgressDlgShipSearch: e : java.lang.ArrayIndexOutOfBoundsException: length=1; index=1
  D/pDlgBarcodeSearch: Barcode receiveData :
  E/pDlgBarcodeSearch: e : java.lang.ArrayIndexOutOfBoundsException: length=1; index=1
  ```
- 서버 정상 응답(데이터 0건)과 서버 미연결/통신 실패가 화면상 동일하게 "바코드정보가 없는 상품목록"류의 빈 다이얼로그로 표시되어 사용자가 두 상황을 구분할 수 없음
- **핵심 현상: 빈 응답(정상 0건)에 대한 가드가 없어 매번 예외 경로를 타며, 우연히 catch로 방어되어 은폐되고 있을 뿐 코드상 결함임**

## 원래부터 있던 버그인가?

**NO — 오류 05번(JSP 마지막컬럼 빈값 파싱오류) 수정 과정에서 `split("::", -1)` 옵션은 적용되었으나, "완전히 빈 응답(0건)" 자체에 대한 가드는 여전히 누락된 상태**

```java
// ProgressDlgShipSearch.java:207 (05번 수정 적용된 현재 코드)
temp = s.split("::", -1);
// ★ trailing empty string은 보존되지만, receiveData 자체가 ""인 경우
//   result.length == 1 (빈 문자열 1개)이 되어 temp.length도 1 → temp[1] 이상 접근 시 예외
```
현재 코드는 05번에서 지적된 "마지막 컬럼 빈값" 문제(원본에도 있던 구조)와는 달리, `receiveData.isEmpty()`(전체 응답이 완전히 빈 경우) 자체에 대한 방어 로직이 신규 코드에도 여전히 없다. `if (result.length > 0)`이라는 가드가 존재하지만, 빈 문자열을 split해도 length는 1(빈 문자열 1개)이 되므로 이 가드는 실질적으로 아무 역할을 하지 못한다.

## 원인

### 문제 1 (주요): receiveData가 빈 문자열일 때 split 결과가 "빈 배열"이 아닌 "빈 문자열 1개짜리 배열"이 되는 Java 특성 미방어

#### 코드 위치
- `ProgressDlgShipSearch.java` : 194~220줄
- `ProgressDlgBarcodeSearch.java` : 98~117줄

#### 현재 문제 코드
```java
// ProgressDlgShipSearch.java:194, 204-220
String[] result = receiveData.split(";;");
// ★ receiveData == "" 이면 result = [""] (length=1, 빈 문자열 1개) — length==0이 아님
...
if (result.length > 0) {          // ★ 항상 true (빈 응답도 length=1)
    for (String s : result) {
        temp = s.split("::", -1);  // s == "" → temp = [""] (length=1)
        si.setGI_D_ID(temp[0].toString());     // OK (temp[0] = "")
        si.setITEM_CODE(temp[1].toString());   // ★ ArrayIndexOutOfBoundsException (length=1, index=1)
        ...
```

```java
// ProgressDlgBarcodeSearch.java:98, 106-109
String[] result = receiveData.split(";;");
if (result.length > 0) {          // ★ 동일하게 항상 true
    for (String s : result) {
        temp = s.split("::", -1);
        bi.setPACKER_CLIENT_CODE(temp[0].toString());  // OK
        bi.setPACKER_PRODUCT_CODE(temp[1].toString()); // ★ ArrayIndexOutOfBoundsException
```

#### 발생 시나리오
1. 조회 조건에 맞는 데이터가 0건이면 JSP가 빈 문자열을 응답
2. `"".split(";;")`은 Java 사양상 `[""]`(길이 1)을 반환 — `length == 0`이 되지 않음
3. `if (result.length > 0)` 가드를 통과하여 for문 진입
4. `s = ""`, `s.split("::", -1)`도 동일하게 `[""]`(길이 1) 반환
5. `temp[1]` 이상 인덱스 접근 시 `ArrayIndexOutOfBoundsException`
6. try-catch로 예외가 흡수되어 크래시는 나지 않지만, `list_si_info`/`list_received_bi`에 아무것도 추가되지 않은 채(우연히 결과적으로는 맞는 동작) 종료 — **의도된 방어가 아닌 예외로 인한 우연한 정상 종료**

## 상세 흐름

1. **정상 케이스(데이터 있음)** (조건: receiveData에 실제 row 존재)
   - `result.length >= 1`, 각 `s`가 실제 컬럼 데이터를 담고 있음
   - `temp[N]` 접근이 정상적으로 이루어짐 → 정상 파싱

2. **문제 케이스(데이터 0건, receiveData == "")** (조건: 조회 결과 0건)
   - `result = [""]`(length=1) → for문에서 `s = ""`
   - `temp = "".split("::", -1) = [""]`(length=1)
   - `temp[1]` 접근 → **ArrayIndexOutOfBoundsException**
   - catch(Exception e)에서 로그만 남기고 종료 → `list_si_info`가 빈 상태로 유지되어 겉보기엔 "정상적으로 0건 처리"된 것처럼 보임

3. **연쇄 영향** (ProgressDlgBarcodeSearch)
   - `ProgressDlgShipSearch` 완료 후 자동 실행되는 `ProgressDlgBarcodeSearch`도 동일 구조로 예외 발생
   - `list_received_bi`도 빈 상태 유지

4. **사용자 화면**
   - 데이터 0건(정상)과 서버 통신 실패(비정상)가 모두 "바코드정보가 없는 상품목록" 등 동일한 빈 결과 화면으로 표시되어 구분 불가

## 영향 범위
- `ProgressDlgShipSearch.java` (194~220줄) — 전체 searchType(0~7)의 출하대상 조회 공통 로직
- `ProgressDlgBarcodeSearch.java` (98~117줄) — 전체 searchType 바코드정보 조회 공통 로직
- `ProgressDlgGoodsWetSearch.java` — 동일한 `split(";;")` → `split("::", -1)` 패턴을 사용하는 경우 동일 문제 가능성 (확인 필요)
- 조회 결과가 0건인 모든 조회 조건(예: 데이터 없는 조회일자, 미출고 창고 등)에서 항상 발생 — 재현율 100%

## 수정 방안

### 수정 1: receiveData 빈 문자열 가드 추가 (권장)

```java
// ProgressDlgShipSearch.java:194 수정 방향
list_si_info = new ArrayList<Shipments_Info>();
if (!receiveData.isEmpty()) {
    String[] result = receiveData.split(";;");
    String[] temp;
    Shipments_Info si;
    if (Common.D) {
        Log.d(TAG, "result's Count : " + result.length);
    }
    for (String s : result) {
        if (s.isEmpty()) continue;   // 빈 row 스킵
        temp = s.split("::", -1);
        si = new Shipments_Info();
        ...
    }
}
```

```java
// ProgressDlgBarcodeSearch.java:98 수정 방향
list_received_bi = new ArrayList<Barcodes_Info>();
if (!receiveData.isEmpty()) {
    String[] result = receiveData.split(";;");
    ...
    for (String s : result) {
        if (s.isEmpty()) continue;
        temp = s.split("::", -1);
        ...
    }
}
```

> 수정 시 주의사항: `result.length > 0` 가드를 `!receiveData.isEmpty()` 또는 `if (s.isEmpty()) continue;`로 교체해야 실제로 빈 응답을 걸러낼 수 있다. `result.length > 0` 조건은 이번 사례처럼 항상 참이 되어 무의미하다. 오류 05번(마지막 컬럼 빈값)과는 별개 방어이므로 함께 적용해도 서로 충돌하지 않는다.

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/오류/05_JSP_마지막컬럼_빈값_파싱오류.md` — split trailing empty string 관련 유사·선행 오류(마지막 컬럼 값이 빈 경우). 본 오류는 "응답 전체가 빈 경우"로 원인이 구분됨
- `app/doc/테스트/07_이마트_출하계근_EDA51실기기_자동테스트.md` — 본 오류 발견 테스트
- `app/doc/참고자료/오류패턴_분석.md` — 패턴 G: 인코딩/파싱 (데이터 전송/파싱 구조 문제)
- `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`
- `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgBarcodeSearch.java`
- `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgGoodsWetSearch.java` (동일 패턴 여부 확인 필요)
