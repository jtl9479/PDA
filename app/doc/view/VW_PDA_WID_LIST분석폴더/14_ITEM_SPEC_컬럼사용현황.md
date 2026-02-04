# ITEM_SPEC 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 스펙 (품목규격)
**파싱 위치**: temp[13]

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
| **라벨 출력** | 상품명과 함께 "상품명 / 규격" 형태로 라벨에 인쇄 |
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
| TB_SHIPMENT | 출하대상 품목규격 저장 |

---

## 4. 사용 코드

### 4.1 라벨 출력 - 상품명과 규격 결합

**파일**: BixolonShipmentActivity.java (Line 1999~2006)

```java
//------------------------상품명 / 냉장냉동------------------------
// 상품명이 일정 길이 이상 넘어갈 경우 글자 크기 조절
if (si.EMARTITEM.length() > 14) {
    slcsCmd.append(slcsText(50, 120, 35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC));
} else {
    slcsCmd.append(slcsText(50, 120, 40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC));
}
Log.i(TAG, "write------------------------------------>상품명 / 냉장냉동 : " + si.EMARTITEM + " / " + si.ITEM_SPEC);
```

### 4.2 라벨 출력 - Woosim 프린터

**파일**: ShipmentActivity.java (Line 1907~1911)

```java
// Woosim 프린터용 라벨 출력
if (si.EMARTITEM.length() > 14) {
    byteStream.write(WoosimCmd.getTTFcode(35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC));
} else {
    byteStream.write(WoosimCmd.getTTFcode(40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC));
}
Log.i(TAG, "write------------------------------------>상품명 / 냉장냉동 : " + si.EMARTITEM + " / " + si.ITEM_SPEC);
```

### 4.3 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 281)

```java
si.setITEM_SPEC(temp[13].toString());        // 품목규격
```

### 4.4 DB INSERT

**파일**: DBHandler.java (Line 610)

```java
// TB_SHIPMENT 테이블에 저장
+ Common.nullCheck(si.getITEM_SPEC(), "") + "','"
```

---

## 5. 라벨 출력 형식

| 항목 | 출력 형식 | 예시 |
|------|----------|------|
| 라벨 1행 | 상품명 / 규격 | "삼겹살 / 냉장" |

### 글자 크기 조정

| 조건 | 폰트 크기 |
|------|----------|
| 상품명 14자 초과 | 35 |
| 상품명 14자 이하 | 40 |

---

## 6. 라벨 출력 위치

라벨 내 출력 위치: **(50, 120)** - 라벨 상단 영역

```
┌─────────────────────────────────────┐
│                                     │
│  상품명 / 규격  ← ITEM_SPEC 출력    │
│                                     │
│  ||||||||||||||||||||  (바코드)     │
│  1234567890123456                   │
│                                     │
│  센터명                 중량        │
│  업체명(지점명)         일자        │
│                                     │
└─────────────────────────────────────┘
```

---

## 7. 결론

**상태**: ✅ 필수 (삭제 불가)

ITEM_SPEC은 **라벨 출력의 필수 컬럼**으로:
- 상품명(EMARTITEM)과 함께 **"상품명 / 규격"** 형태로 라벨에 인쇄
- 주로 **냉장/냉동** 등 보관 조건 표시
- 상품명 길이에 따라 **폰트 크기 자동 조정** (14자 기준)

---

**최종 수정일**: 2026-02-03
