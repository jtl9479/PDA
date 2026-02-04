# GI_D_ID 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 출고상세ID (Primary Key)
**파싱 위치**: temp[0]

---

## 0. 사용 영역

| 구분 | 사용 |
|------|:----:|
| 서버전송 | ● |
| 화면표시 | |
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
| **PK (기본키)** | 출하대상 데이터 고유 식별 |
| **계근 데이터 연결** | TB_GOODS_WET 테이블과 출하대상 연결 (FK 역할) |
| **작업 행 선택** | 목록에서 현재 작업 대상 찾기 |
| **서버 전송** | 계근 완료 데이터 전송 시 식별자로 사용 |
| **데이터 동기화** | PDA 로컬 DB ↔ 서버 데이터 비교 기준 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| Goodswets_Info.java | 계근 데이터 DTO |
| DBHandler.java | TB_SHIPMENT, TB_GOODS_WET 테이블 CRUD |
| BixolonShipmentActivity.java | 계근 작업 처리 |
| ShipmentActivity.java | 계근 작업 처리 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |
| ProgressDlgGoodsWetSearch.java | 계근 데이터 조회 조건 |

---

## 3. 저장 테이블

| 테이블 | 역할 |
|--------|------|
| TB_SHIPMENT | 출하대상 저장 (GI_D_ID가 PK) |
| TB_GOODS_WET | 계근 데이터 저장 (GI_D_ID로 출하대상 연결) |

---

## 4. 사용 코드

### 4.1 PK로서 데이터 식별

**파일**: BixolonShipmentActivity.java (Line 827)

```java
// 출하대상 목록에서 특정 행 찾기
if (arSM.get(i).getGI_D_ID().toString().equals(gi_d_id.toString())) {
    current_work_position = i;
}
```

### 4.2 DB 조회 조건

**파일**: DBHandler.java (Line 1123)

```java
// TB_GOODS_WET 테이블에서 계근 데이터 조회
"WHERE GI_D_ID = '" + gi_d_id + "' AND PACKER_PRODUCT_CODE = '" + packer_product_code + "'"
```

### 4.3 서버 전송 패킷 구성

**파일**: BixolonShipmentActivity.java (Line 3729)

```java
// 계근 데이터 전송 포맷
packet += list_send_info.get(i).getGI_D_ID() + "::";
// 결과: GI_D_ID::WEIGHT::WEIGHT_UNIT::PACKER_PRODUCT_CODE::BARCODE::...
```

### 4.4 작업 완료 처리

**파일**: BixolonShipmentActivity.java (Line 3778)

```java
// 출하 완료 시 서버 전송
String completeStr = arSM.get(j).getGI_D_ID() + "::" + arSM.get(j).getITEM_CODE() + "::" + arSM.get(j).getBRAND_CODE() + "::" + Common.REG_ID;
```

### 4.5 데이터 동기화

**파일**: ProgressDlgShipSearch.java (Line 337)

```java
// PDA 로컬 DB와 서버 데이터 비교 시 기준
if (list_si_pda.get(i).getGI_D_ID().equals(list_si_info.get(j).getGI_D_ID())) {
    bCheck = true;
}
```

### 4.6 계근 데이터 조회

**파일**: BixolonShipmentActivity.java (Line 3328)

```java
// 출하대상별 계근 데이터 조회
row = DBHandler.selectqueryListGoodsWetInfo(mContext, arSM.get(i).getGI_D_ID(), arSM.get(i).getPACKER_PRODUCT_CODE(), arSM.get(i).getCLIENT_CODE());
```

---

## 5. 결론

**상태**: ✅ 필수 (삭제 불가)

GI_D_ID는 출고상세 식별자로서 **핵심 PK 컬럼**입니다.

---

**최종 수정일**: 2026-02-03
