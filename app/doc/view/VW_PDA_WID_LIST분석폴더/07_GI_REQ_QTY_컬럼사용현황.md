# GI_REQ_QTY 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 출하요청중량 (요청 중량 kg)
**파싱 위치**: temp[6]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | ● |
| 바코드생성 | |
| 라벨출력 | |
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
| **화면 표시** | "요청중량 / 완료중량" 형태로 표시 |
| **센터 총 요청중량 계산** | 센터별 전체 요청중량 합계 산출 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 화면 표시 |
| ShipmentActivity.java | 화면 표시 |
| ShipmentListAdapter.java | 목록 화면 표시 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 요청중량 저장 |

---

## 4. 사용 코드

### 4.1 화면 표시 (요청중량/완료중량)

**파일**: BixolonShipmentActivity.java (Line 1582)

```java
// "요청중량 / 완료중량" 형태로 표시
edit_wet_weight.setText(arSM.get(current_work_position).getGI_REQ_QTY() + " / " + arSM.get(current_work_position).getGI_QTY());
```

### 4.2 작업 시작 시 화면 표시

**파일**: BixolonShipmentActivity.java (Line 1922)

```java
// 작업 시작 시 중량 표시
edit_wet_weight.setText(arSM.get(work_position).getGI_REQ_QTY() + " / " + arSM.get(work_position).getGI_QTY());
```

### 4.3 센터 총 요청중량 계산

**파일**: BixolonShipmentActivity.java (Line 3406)

```java
// 센터별 전체 요청중량 합계
centerTotalWeight += Double.parseDouble(arSM.get(i).getGI_REQ_QTY());
```

### 4.4 상세 화면 표시

**파일**: BixolonShipmentActivity.java (Line 4218)

```java
// 출하대상 상세 팝업에서 중량 표시
detail_edit_weight.setText(si.getGI_REQ_QTY() + " / " + si.getGI_QTY());
```

### 4.5 목록 화면 표시

**파일**: ShipmentListAdapter.java (Line 146)

```java
// 출하대상 목록에서 "요청중량/완료중량" 표시
holder.weight.setText(arSrc.get(pos).getGI_REQ_QTY() + "/" + arSrc.get(pos).getGI_QTY());
```

---

## 5. 관련 컬럼

| 컬럼명 | 의미 | 타입 |
|--------|------|------|
| **GI_REQ_QTY** | 출하요청중량 (요청) | String (kg) |
| **GI_QTY** | 계근중량 (완료) | double (kg) |

---

## 6. 화면 표시 필드

| 화면 필드 | 변수명 | 표시 형태 |
|-----------|--------|-----------|
| 계근 화면 중량 | edit_wet_weight | "요청중량 / 완료중량" |
| 상세 화면 중량 | detail_edit_weight | "요청중량 / 완료중량" |
| 목록 화면 중량 | holder.weight | "요청중량/완료중량" |

---

## 7. GI_REQ_PKG vs GI_REQ_QTY 비교

| 컬럼명 | 의미 | 단위 | 완료 비교 대상 |
|--------|------|------|----------------|
| **GI_REQ_PKG** | 출하요청수량 | 박스 | PACKING_QTY |
| **GI_REQ_QTY** | 출하요청중량 | kg | GI_QTY |

---

## 8. 결론

**상태**: ✅ 필수 (삭제 불가)

GI_REQ_QTY는 **화면 표시 전용 컬럼**으로:
- GI_QTY와 함께 **"요청/완료" 형태**로 중량 표시
- 센터별 **총 요청중량 합계** 계산
- 작업 완료 판단에는 사용되지 않음 (GI_REQ_PKG 사용)

---

**최종 수정일**: 2026-02-03
