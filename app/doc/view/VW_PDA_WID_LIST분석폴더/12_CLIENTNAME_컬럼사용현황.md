# CLIENTNAME 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 출고업체명 (지점명)
**파싱 위치**: temp[11]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | ● |
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
| **화면 표시** | 목록, 스피너, 상세 화면에 지점명 표시 |
| **라벨 출력** | 지점명 추출하여 라벨에 인쇄 |
| **DB 정렬** | ORDER BY CLIENTNAME으로 정렬 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD, 정렬 |
| BixolonShipmentActivity.java | 화면 표시, 라벨 출력 |
| ShipmentActivity.java | 화면 표시, 라벨 출력 |
| ShipmentListAdapter.java | 목록 화면 표시 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 출고업체명 저장 |

---

## 4. 사용 코드

### 4.1 목록 화면 표시

**파일**: ShipmentListAdapter.java (Line 141)

```java
// 출하대상 목록에서 지점명 표시
holder.position.setText(arSrc.get(pos).getCLIENTNAME());
```

### 4.2 스피너 목록 생성

**파일**: BixolonShipmentActivity.java (Line 3559)

```java
// 지점 선택 스피너 목록에 추가
list_position.add(arSM.get(i).getCLIENTNAME());
```

### 4.3 스피너 목록 (수입식별번호 포함)

**파일**: BixolonShipmentActivity.java (Line 3395)

```java
// 지점명 + 수입식별번호 형태로 스피너 표시
list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO());
```

### 4.4 상세 화면 표시

**파일**: BixolonShipmentActivity.java (Line 4214)

```java
// 출하대상 상세 팝업에서 지점명 표시
detail_edit_position_name.setText(si.getCLIENTNAME());
```

### 4.5 라벨 출력 - 지점명 추출

**파일**: BixolonShipmentActivity.java (Line 2404~2413)

```java
// 업체명에서 지점명만 추출
if (si.CLIENTNAME.contains("이마트")) {
    split_name = si.CLIENTNAME.split("이마트");        // "이마트"로 분리
} else if (si.CLIENTNAME.contains("신세계백화점")) {
    split_name = si.CLIENTNAME.split("백화점");        // "백화점"으로 분리
} else if (si.CLIENTNAME.contains("EVERY")) {
    split_name = si.CLIENTNAME.split("EVERY");         // "EVERY"로 분리
} else if (si.CLIENTNAME.contains(CENTER_NAME_ET)) {
    split_name = si.CLIENTNAME.split(CENTER_NAME_ET);  // 이마트 트레이더스
} else {
    pointName = si.CLIENTNAME.toString();              // 그대로 사용
}
```

### 4.6 라벨 출력 - 글자 수 조정

**파일**: BixolonShipmentActivity.java (Line 2453)

```java
// 지점명 11자 초과 시 폰트 크기 조정
if (11 < si.CLIENTNAME.toString().length()) {
    // 작은 폰트 사용
}
```

### 4.7 DB 정렬

**파일**: DBHandler.java (Line 234)

```java
// 출하대상 조회 시 지점명으로 정렬
+ " ORDER BY SAVE_TYPE ASC, CLIENTNAME ASC";
```

---

## 5. 지점명 분리 패턴

| 업체명 포함 문자 | 분리 기준 | 예시 |
|-----------------|----------|------|
| "이마트" | "이마트" | "이마트 강남점" → "강남점" |
| "신세계백화점" | "백화점" | "신세계백화점 본점" → "본점" |
| "EVERY" | "EVERY" | "EVERY DAY 잠실점" → "DAY 잠실점" |
| CENTER_NAME_ET | 이마트 트레이더스 | 트레이더스 지점 |
| 그 외 | 전체 사용 | 그대로 표시 |

---

## 6. 화면 표시 필드

| 화면 필드 | 변수명 | 용도 |
|-----------|--------|------|
| 목록 지점명 | holder.position | 목록 화면 |
| 스피너 | sp_position | 지점 선택 |
| 상세 지점명 | detail_edit_position_name | 상세 팝업 |

---

## 7. 결론

**상태**: ✅ 필수 (삭제 불가)

CLIENTNAME은 **화면 표시의 핵심 컬럼**으로:
- **목록, 스피너, 상세 화면**에 지점명 표시
- **라벨 출력** 시 지점명 추출 (이마트, 백화점 등으로 분리)
- 글자 수에 따라 **폰트 크기 자동 조정** (11자 기준)
- **ORDER BY** 정렬 기준

---

**최종 수정일**: 2026-02-03
