# CENTERNAME 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 센터명
**파싱 위치**: temp[12]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | |
| 바코드생성 | |
| 라벨출력 | ● |
| 로직분기 | ● |
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
| **센터유형 판별** | TRD, WET, ET 등 센터 유형 식별 |
| **유통기한 입력 조건** | 특정 센터일 때 유통기한 입력 필수 |
| **라벨 출력** | 라벨에 센터명 인쇄 |
| **DB 조회 조건** | CENTERNAME으로 출하대상 필터링 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD, 조회 조건 |
| BixolonShipmentActivity.java | 센터유형 판별, 라벨 출력 |
| ShipmentActivity.java | 센터유형 판별, 라벨 출력 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 센터명 저장 |

---

## 4. 센터유형 상수

| 상수명 | 값 | 의미 |
|--------|-----|------|
| CENTER_NAME_TRD | "TRD" | 트레이더스 냉장 |
| CENTER_NAME_WET | "WET" | WET 센터 |
| CENTER_NAME_ET | "E/T" 또는 유사 | 이마트 트레이더스 |

---

## 5. 사용 코드

### 5.1 센터유형 판별 - 유통기한 입력 조건

**파일**: BixolonShipmentActivity.java (Line 645)

```java
// TRD, WET, ET 센터이거나 롯데일 경우 유통기한 입력 필요
if (arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_TRD) ||
    arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_WET) ||
    arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_ET) ||
    Common.searchType.equals(SEARCH_TYPE_LOTTE)) {
    // 유통기한 입력 다이얼로그 표시
}
```

### 5.2 센터유형 판별 - 바코드 스캔 처리

**파일**: BixolonShipmentActivity.java (Line 1177)

```java
// 센터유형에 따라 바코드 스캔 처리 분기
if (arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_TRD) ||
    arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_WET) ||
    arSM.get(current_work_position).getCENTERNAME().contains(CENTER_NAME_ET)) {
    // 트레이더스/WET 센터 처리
}
```

### 5.3 라벨 출력 - 유통기한 입력 여부

**파일**: BixolonShipmentActivity.java (Line 2118)

```java
// ET, WET, TRD 센터일 경우 유통기한 입력
if (si.getCENTERNAME().contains(CENTER_NAME_ET) ||
    si.getCENTERNAME().contains(CENTER_NAME_WET) ||
    si.getCENTERNAME().contains(CENTER_NAME_TRD)) {
    // 유통기한 입력 필요
}
```

### 5.4 라벨 출력 - 센터명 인쇄

**파일**: BixolonShipmentActivity.java (Line 2434~2439)

```java
// 센터명 길이에 따라 폰트 크기 조정
if (7 < si.CENTERNAME.length()) {
    slcsCmd.append(slcsText(10, 12, 35, 35, si.CENTERNAME));  // 7자 초과: 크기 35
} else {
    slcsCmd.append(slcsText(10, 10, 40, 40, si.CENTERNAME));  // 7자 이하: 크기 40
}
```

### 5.5 DB 조회 조건

**파일**: DBHandler.java (Line 135)

```java
// 센터명으로 출하대상 필터링
+ " WHERE CENTERNAME = '" + center_name + "' "
```

### 5.6 센터 목록 조회

**파일**: DBHandler.java (Line 529~532)

```java
// 센터명 목록 조회 (중복 제거)
SELECT DISTINCT CENTERNAME
FROM TB_SHIPMENT
ORDER BY CENTERNAME ASC
```

---

## 6. 센터유형별 처리

| 센터유형 | 유통기한 입력 | 특수 처리 |
|----------|:------------:|----------|
| TRD (트레이더스 냉장) | ✅ 필수 | 유통기한 다이얼로그 |
| WET | ✅ 필수 | 유통기한 다이얼로그 |
| E/T (이마트 트레이더스) | ✅ 필수 | 유통기한 다이얼로그 |
| 일반 센터 | ❌ 불필요 | 기본 처리 |

---

## 7. 폰트 크기 조정

| 센터명 길이 | 폰트 크기 |
|------------|----------|
| 7자 초과 | 35 |
| 7자 이하 | 40 |

---

## 8. 결론

**상태**: ✅ 필수 (삭제 불가)

CENTERNAME은 **센터유형 판별의 핵심 컬럼**으로:
- **TRD/WET/ET** 포함 여부로 센터유형 식별
- 센터유형에 따라 **유통기한 입력 필수 여부** 결정
- **라벨 출력** 시 센터명 인쇄
- **DB 조회 조건**으로 센터별 출하대상 필터링

---

**최종 수정일**: 2026-02-03
