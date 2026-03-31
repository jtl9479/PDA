# search_warehouse 한글 깨짐 (인코딩 불일치)

## 발견일
2026-03-31

## 현상
- 서버 로그에서 `INNO DB 연결 성공`은 정상 출력
- `##search_warehouse data : WHERE 회사코드 = '20'`가 `WHERE ȸ���ڵ� = '20'`로 깨져서 출력
- **JSP에 하드코딩된 한글은 정상, 앱에서 전달받은 파라미터만 깨짐**

## 원래부터 있던 버그인가?

**YES - 기존 JSP(search_shipment.jsp 등)도 동일한 구조이나, 앱에서 전달하는 data 파라미터에 한글이 포함되지 않아 발견되지 않았음. search_warehouse.jsp에서 회사코드를 파라미터로 전달하면서 최초 발견.**

## 원인

### 문제 1 (주요): 앱과 서버 간 인코딩 불일치

#### 코드 위치
- `HttpHelper.java` : 122줄
- 각 JSP 파일 : `request.setCharacterEncoding("UTF-8")`

#### 현재 문제 코드
```java
// HttpHelper.java:122
result = new UrlEncodedFormEntity(nameValue, "euc-kr");
// ★ 앱에서 EUC-KR로 인코딩하여 전송
```

```jsp
// search_warehouse.jsp:14
request.setCharacterEncoding("UTF-8");
// ★ 서버에서 UTF-8로 디코딩하여 수신
```

#### 발생 시나리오
1. 앱에서 `WHERE 회사코드 = '20'` 문자열을 EUC-KR로 인코딩하여 HTTP POST 전송
2. JSP에서 `request.setCharacterEncoding("UTF-8")`로 UTF-8 디코딩 시도
3. EUC-KR 바이트를 UTF-8로 해석하면서 한글 깨짐 발생

## 상세 흐름

1. **LoginActivity** (앱)
   - `data = " WHERE 회사코드 = '20'"` 문자열 생성
   - `HttpHelper.sendDataDb(data, ...)` 호출

2. **HttpHelper.makeHttpPostDb** (앱)
   - `UrlEncodedFormEntity(nameValue, "euc-kr")` → EUC-KR로 인코딩

3. **search_warehouse.jsp** (서버)
   - `request.setCharacterEncoding("UTF-8")` → UTF-8로 디코딩 시도
   - **EUC-KR ≠ UTF-8 → 한글 깨짐**

## 영향 범위
- `HttpHelper.java`의 `makeEntity()` 메서드를 사용하는 **모든 JSP 호출**에 영향
- 기존에는 data 파라미터에 한글이 없어 문제가 드러나지 않았으나, 한글 컬럼명을 사용하는 새 쿼리에서 발생
- 해당 파일:
  - `HttpHelper.java` : 122줄 (makeEntity)
  - `search_warehouse.jsp` : data 파라미터 수신
  - `search_shipment.jsp` : data 파라미터 수신 (향후 한글 WHERE 조건 사용 시 동일 문제 발생)
  - `search_barcode_info.jsp` : 동일
  - `search_goods_wet.jsp` : 동일

## 수정 방안

### 방법 A (권장): 앱에서 UTF-8로 변경

```java
// HttpHelper.java:122
// 변경 전
result = new UrlEncodedFormEntity(nameValue, "euc-kr");
// 변경 후
result = new UrlEncodedFormEntity(nameValue, "UTF-8");
```

> 주의: 기존 동작 중인 모든 JSP 호출에 영향. 단, JSP 쪽은 이미 UTF-8로 수신하고 있으므로 오히려 정상화됨.

### 방법 B (임시): JSP에서 EUC-KR로 수신

```jsp
// 변경 전
request.setCharacterEncoding("UTF-8");
// 변경 후
request.setCharacterEncoding("euc-kr");
```

> 비권장: JSP마다 개별 수정 필요하며, 표준 인코딩(UTF-8) 방향에 역행.

## 상태
- [ ] 미수정
