# BL_NO 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: BL번호 (선하증권번호)
**파싱 위치**: temp[8]

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

| 용도 | 설명 |
|------|------|
| **스피너 선택** | BL번호 목록을 스피너에 표시하여 작업 그룹 선택 |
| **중복 방지** | 같은 BL번호 내 미완료 작업 확인 |
| **바코드 스캔 검증** | 스캔한 바코드와 BL번호 일치 확인 |
| **DB 조회 조건** | BL_NO로 출하대상 필터링 |
| **목록 화면 표시** | 뒤 4자리만 표시 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| DBHandler.java | TB_SHIPMENT 테이블 CRUD, 조회 조건 |
| BixolonShipmentActivity.java | 스피너, 중복 방지, 바코드 검증 |
| ShipmentActivity.java | 스피너, 중복 방지, 바코드 검증 |
| ShipmentListAdapter.java | 목록 화면 표시 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 3. 저장 테이블

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 BL번호 저장 |

---

## 4. 사용 코드

### 4.1 BL번호 스피너 목록 생성

**파일**: BixolonShipmentActivity.java (Line 3577~3583)

```java
// 중복 제거하여 BL번호 목록 생성
for (int j = 0; j < list_bl.size(); j++) {
    if (list_bl.get(j).toString().equals(arSM.get(i).getBL_NO())) {
        bCheck = true;
        break;
    }
}
if (!bCheck) {
    list_bl.add(arSM.get(i).getBL_NO());  // 중복 아니면 추가
}
```

### 4.2 스피너 선택 시 현재 작업 BL번호 설정

**파일**: BixolonShipmentActivity.java (Line 3599)

```java
// 스피너에서 현재 작업 BL번호와 일치하는 항목 선택
if (sp_bl_no.getItemAtPosition(i).toString().equals(arSM.get(current_work_position).getBL_NO())) {
    sp_bl_no.setSelection(i);
}
```

### 4.3 같은 BL번호 내 미완료 작업 확인 (중복 방지)

**파일**: BixolonShipmentActivity.java (Line 1148)

```java
// BL번호가 같고 계근 미완료인 건이 있는지 확인
if (temp_bl_no.equals(arSM.get(i).getBL_NO()) && !arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))) {
    bCheck = true;
}
```

### 4.4 바코드 스캔 검증

**파일**: BixolonShipmentActivity.java (Line 1686~1687)

```java
// 스캔한 바코드와 현재 작업 BL번호 비교
String BL_NO = barcode;
if (BL_NO.equals(arSM.get(current_work_position).getBL_NO())) {
    // 일치 시 처리
}
```

### 4.5 DB 조회 조건

**파일**: DBHandler.java (Line 94)

```java
// BL번호로 출하대상 필터링
qry_condition = " AND BL_NO = '" + condition + "' ";
```

### 4.6 목록 화면 표시 (뒤 4자리)

**파일**: ShipmentListAdapter.java (Line 149~152)

```java
// BL번호 뒤 4자리만 표시
if(arSrc.get(pos).getBL_NO().equals("")){
    holder.bl.setText("");
} else {
    holder.bl.setText(arSrc.get(pos).getBL_NO().substring(arSrc.get(pos).getBL_NO().length() - 4, arSrc.get(pos).getBL_NO().length()));
}
```

---

## 5. 스피너 관련 변수

| 변수명 | 용도 |
|--------|------|
| sp_bl_no | BL번호 스피너 UI |
| list_bl | BL번호 목록 (ArrayList) |
| work_bl_no | 현재 작업 중인 BL번호 |

---

## 6. BL번호 사용 흐름

```
1. 서버에서 출하대상 조회
2. BL번호 목록 생성 (중복 제거)
3. 스피너에 BL번호 목록 표시
4. 사용자가 BL번호 선택 → 해당 출하대상 필터링
5. 바코드 스캔 시 BL번호 검증
6. 같은 BL번호 내 미완료 작업 확인
```

---

## 7. 결론

**상태**: ✅ 필수 (삭제 불가)

BL_NO는 **작업 그룹 관리의 핵심 컬럼**으로:
- **스피너**에서 BL번호별 작업 선택
- 같은 BL번호 내 **미완료 작업 확인** (중복 방지)
- **바코드 스캔 검증**
- 목록에서 **뒤 4자리**만 표시

---

**최종 수정일**: 2026-02-03
