# GI_REQ_PKG 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 출하요청수량 (요청 박스 수)
**파싱 위치**: temp[5]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | |
| 화면표시 | ● |
| 바코드생성 | |
| 라벨출력 | |
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

| 용도               | 설명                                        |
| ---------------- | ----------------------------------------- |
| **작업 완료 판단**     | GI_REQ_PKG == PACKING_QTY 비교로 계근 완료 여부 확인 |
| **화면 표시**        | "요청수량 / 완료수량" 형태로 표시                      |
| **센터 총 요청수량 계산** | 센터별 전체 요청수량 합계 산출                         |
| **전송 완료 판단**     | SAVE_CNT == GI_REQ_PKG 비교로 서버 전송 완료 확인    |
| **DB 저장**        | TB_SHIPMENT 테이블에 저장                       |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD |
| BixolonShipmentActivity.java | 계근 완료 판단, 화면 표시 |
| ShipmentActivity.java | 계근 완료 판단, 화면 표시 |
| ShipmentListAdapter.java | 목록 화면 표시 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 요청수량 저장 |

---

## 4. 사용 코드

### 4.1 작업 완료 판단

**파일**: BixolonShipmentActivity.java (Line 1199)

```java
// 요청수량 == 계근수량이면 작업 완료
if (arSM.get(current_work_position).getGI_REQ_PKG().equals(String.valueOf(arSM.get(current_work_position).getPACKING_QTY()))) {
    // 완료 처리
}
```

### 4.2 미완료 작업 확인

**파일**: BixolonShipmentActivity.java (Line 1148)

```java
// BL번호가 같고 계근이 완료되지 않은 건 확인
if (temp_bl_no.equals(arSM.get(i).getBL_NO()) && !arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))) {
    bCheck = true;
}
```

### 4.3 화면 표시 (요청/완료)

**파일**: BixolonShipmentActivity.java (Line 1581)

```java
// "요청수량 / 완료수량" 형태로 표시
edit_wet_count.setText(arSM.get(current_work_position).getGI_REQ_PKG() + " / " + arSM.get(current_work_position).getPACKING_QTY());
```

### 4.4 센터 총 요청수량 계산

**파일**: BixolonShipmentActivity.java (Line 3405)

```java
// 센터별 전체 요청수량 합계
centerTotalCount += Integer.parseInt(arSM.get(i).getGI_REQ_PKG());
```

### 4.5 전송 완료 판단

**파일**: BixolonShipmentActivity.java (Line 3777)

```java
// 전송 개수와 요청 개수 비교
if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {
    // 전송 완료 처리
}
```

### 4.6 목록 화면 표시

**파일**: ShipmentListAdapter.java (Line 142)

```java
// 출하대상 목록에서 "요청/완료" 표시
holder.count.setText(arSrc.get(pos).getGI_REQ_PKG() + "/" + arSrc.get(pos).getPACKING_QTY());
```

### 4.7 초과 계근 방지

**파일**: BixolonShipmentActivity.java (Line 1618)

```java
// 요청수량보다 계근수량이 크거나 같으면 추가 계근 방지
if (Integer.parseInt(arSM.get(current_work_position).getGI_REQ_PKG()) <= arSM.get(current_work_position).getPACKING_QTY()) {
    // 경고 메시지
}
```

---

## 5. 관련 컬럼

| 컬럼명 | 의미 | 비교 대상 |
|--------|------|-----------|
| **GI_REQ_PKG** | 출하요청수량 (요청 박스) | 기준값 |
| **PACKING_QTY** | 계근수량 (완료 박스) | 작업 완료 판단 |
| **SAVE_CNT** | 전송 개수 | 전송 완료 판단 |

---

## 6. 비교 로직 요약

| 비교 | 조건 | 의미 |
|------|------|------|
| GI_REQ_PKG == PACKING_QTY | 참 | 계근 완료 |
| GI_REQ_PKG != PACKING_QTY | 참 | 계근 미완료 |
| GI_REQ_PKG <= PACKING_QTY | 참 | 초과 계근 방지 |
| SAVE_CNT == GI_REQ_PKG | 참 | 전송 완료 |

---

## 7. 결론

**상태**: ✅ 필수 (삭제 불가)

GI_REQ_PKG는 **작업 완료 판단의 핵심 컬럼**으로:
- PACKING_QTY와 비교하여 **계근 완료 여부** 판단
- SAVE_CNT와 비교하여 **전송 완료 여부** 판단
- 센터별 **총 요청수량 합계** 계산
- 화면에 **"요청/완료"** 형태로 표시

---

**최종 수정일**: 2026-02-03
