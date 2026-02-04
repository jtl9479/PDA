# GI_REQ_DATE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 출하요청일 (요청일자)
**파싱 위치**: temp[7]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | |
| 라벨출력 | |
| 로직분기 | ● |
| DB저장 | ● |
| 바코드검증 | |
| 조회조건 | ● |
| 앱미전달 | |

> **구분 기준**
> - **서버전송**: 서버에 데이터 전송 시 패킷에 포함
> - **화면표시**: 앱 화면(Activity)에 텍스트로 표시
> - **바코드생성**: 바코드 문자열 생성에 사용
> - **라벨출력**: 라벨 인쇄 시 텍스트/값으로 출력
> - **로직분기**: if/switch 등 조건 분기에 사용
> - **DB저장**: 로컬 SQLite(TB_SHIPMENT)에 저장
> - **바코드검증**: 스캔 바코드와 매칭 검증
> - **조회조건**: 서버/로컬 DB 쿼리 WHERE 조건
> - **앱미전달**: 서버 VIEW에서만 사용, 앱에 전달 안 됨

---

## 1. 용도

| 용도 | 설명 |
|------|------|
| **서버 조회 조건** | 출하대상 조회 시 날짜 필터링 |
| **로컬 DB 조회 조건** | TB_SHIPMENT 테이블 조회 시 날짜 필터링 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD, 조회 조건 |
| ProgressDlgShipSearch.java | 서버 조회 조건, 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 요청일자 저장 |

---

## 4. 사용 코드

### 4.1 서버 조회 조건

**파일**: ProgressDlgShipSearch.java (Line 108)

```java
// 서버 조회 시 날짜 조건
String data = " WHERE GI_REQ_DATE = '" + Common.selectDay + "'";
```

### 4.2 로컬 DB 조회 조건

**파일**: DBHandler.java (Line 98)

```java
// 로컬 DB 조회 시 날짜 조건
qry_condition = qry_condition + " AND GI_REQ_DATE = '" + Common.selectDay + "'";
```

### 4.3 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 275)

```java
// 서버 응답에서 요청일자 파싱
si.setGI_REQ_DATE(temp[7].toString());
```

---

## 5. 조회 조건 변수

| 변수명 | 위치 | 설명 |
|--------|------|------|
| **Common.selectDay** | Common.java | 사용자가 선택한 조회 날짜 |

---

## 6. 조회 흐름

```
1. 사용자가 날짜 선택 → Common.selectDay 설정
2. 서버 조회: WHERE GI_REQ_DATE = 'Common.selectDay'
3. 응답 파싱: si.setGI_REQ_DATE(temp[7])
4. 로컬 DB 저장
5. 로컬 DB 조회: WHERE GI_REQ_DATE = 'Common.selectDay'
```

---

## 7. 결론

**상태**: ✅ 필수 (삭제 불가)

GI_REQ_DATE는 **조회 조건의 핵심 컬럼**으로:
- 서버에서 **날짜별 출하대상 조회** 시 필터링
- 로컬 DB에서 **날짜별 출하대상 조회** 시 필터링
- Common.selectDay와 비교하여 해당 날짜 데이터만 조회

---

**최종 수정일**: 2026-02-03
