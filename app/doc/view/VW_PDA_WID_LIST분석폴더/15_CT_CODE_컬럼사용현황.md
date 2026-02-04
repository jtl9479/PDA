# CT_CODE 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 원산지코드
**파싱 위치**: temp[14]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | |
| 라벨출력 | ● |
| 로직분기 | |
| DB저장 | ● |
| 바코드검증 | |
| 조회조건 | |
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
| **라벨 출력** | 원산지코드 라벨에 인쇄 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 라벨 출력 |
| ShipmentActivity.java | 라벨 출력 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 원산지코드 저장 |

---

## 4. 사용 코드

### 4.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 282)

```java
si.setCT_CODE(temp[14].toString());          // CT코드
```

### 4.2 라벨 출력 - Bixolon 프린터

**파일**: BixolonShipmentActivity.java (Line 2826~2827)

```java
// [5] CT코드 - 위치(361, 170), 크기 40
slcsCmd.append(slcsText(170, 361, 40, 40, String.valueOf(si.getCT_CODE())));
```

### 4.3 라벨 출력 - Woosim 프린터

**파일**: ShipmentActivity.java (Line 2857)

```java
byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(si.getCT_CODE())));
```

### 4.4 DB INSERT

**파일**: DBHandler.java (Line 611)

```java
// TB_SHIPMENT 테이블에 저장
+ Common.nullCheck(si.getCT_CODE(), "") + "','"
```

### 4.5 DTO 필드 정의

**파일**: Shipments_Info.java (Line 20)

```java
public String CT_CODE = "";    // 원산지
```

---

## 5. 원산지코드 예시

| 코드 | 의미 |
|------|------|
| KR | 국내산 (Korea) |
| US | 미국산 |
| AU | 호주산 |
| ... | 기타 국가 |

---

## 6. 라벨 출력 위치

라벨 내 출력 위치: **(170, 361)** - BOX 텍스트 아래

```
┌─────────────────────────────────────┐
│                                     │
│  상품명 / 규격                      │
│                                     │
│  ||||||||||||||||||||  (바코드)     │
│  1234567890123456                   │
│                                     │
│  BOX                                │
│  KR    ← CT_CODE (원산지)           │
│  2026년 01월 15일                   │
│                                     │
└─────────────────────────────────────┘
```

---

## 7. 결론

**상태**: ✅ 필수 (삭제 불가)

CT_CODE는 **원산지 표시 컬럼**으로:
- **라벨 출력** 시 원산지코드 인쇄 (KR, US 등)
- VIEW 원천: **CO_품목코드.원산지**
- TB_SHIPMENT 테이블에 저장

---

**최종 수정일**: 2026-02-03
