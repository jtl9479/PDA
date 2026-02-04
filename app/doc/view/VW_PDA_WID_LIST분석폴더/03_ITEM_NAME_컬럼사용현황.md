# ITEM_NAME 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 상품명 (품목명)
**파싱 위치**: temp[2]

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
| **화면 표시** | 계근 화면에서 현재 작업 중인 상품명 표시 |
| **상세 화면 표시** | 출하대상 상세 정보 팝업에 표시 |
| **DB 저장** | TB_SHIPMENT 테이블에 저장 |

---

## 2. 관련 컬럼 비교

| 컬럼명 | 파싱 위치 | 의미 | 용도 |
|--------|-----------|------|------|
| **ITEM_NAME** | temp[2] | 상품명 | 화면 표시, DB 저장 |
| **ITEM_NAME_KR** | - | 한글 상품명 | 바코드 정보 테이블 |

---

## 3. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| Barcodes_Info.java | 바코드 정보 DTO (ITEM_NAME_KR) |
| DBHandler.java | TB_SHIPMENT, TB_BARCODE_INFO 테이블 CRUD |
| BixolonShipmentActivity.java | 화면 표시 |
| ShipmentActivity.java | 화면 표시 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |

---

## 4. 저장 테이블

| 테이블             | 컬럼           | 용도            |
| --------------- | ------------ | ------------- |
| TB_SHIPMENT     | ITEM_NAME    | 출하대상 상품명 저장   |
| TB_BARCODE_INFO | ITEM_NAME_KR | 바코드 정보 한글 상품명 |

---

## 5. 사용 코드

### 5.1 계근 화면 상품명 표시

**파일**: BixolonShipmentActivity.java (Line 1924)

```java
// 작업 시작 시 상품명 표시
edit_product_name.setText(arSM.get(work_position).getITEM_NAME());
```

### 5.2 출하대상 선택 후 상품명 표시

**파일**: BixolonShipmentActivity.java (Line 3555)

```java
// 출하대상 목록에서 선택 후 상품명 표시
edit_product_name.setText(arSM.get(0).getITEM_NAME().toString());
```

### 5.3 상세 화면 상품명 표시

**파일**: BixolonShipmentActivity.java (Line 4215)

```java
// 출하대상 상세 팝업에서 상품명 표시
detail_edit_ppname.setText(si.getITEM_NAME());
```

### 5.4 바코드 스캔 후 상품명 표시 (ITEM_NAME_KR)

**파일**: BixolonShipmentActivity.java (Line 1723)

```java
// 바코드 스캔 후 바코드 정보의 한글 상품명 표시
edit_product_name.setText(bi.getITEM_NAME_KR());
```

---

## 6. 화면 표시 필드

| 화면 필드 | 변수명 | 용도 |
|-----------|--------|------|
| 상품명 | edit_product_name | 계근 화면 상품명 표시 |
| 상세 상품명 | detail_edit_ppname | 상세 팝업 상품명 표시 |

---

## 7. 결론

**상태**: ✅ 필수 (삭제 불가)

ITEM_NAME은 **화면 표시** 전용 컬럼으로, 사용자에게 현재 작업 중인 상품 정보를 표시하는 데 사용됩니다.

---

**최종 수정일**: 2026-02-03
