# BARCODEGOODS 컬럼 사용 현황

**작성일**: 2026-02-03
**컬럼 의미**: 바코드상품코드
**파싱 위치**: temp[21]

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
| 바코드검증 | ● |
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
| **바코드 매칭** | 스캔한 바코드와 출하대상 상품 매칭 |
| **상품코드 추출** | 바코드에서 상품코드 위치 지정 |
| **작업 대상 검색** | 바코드로 작업 대상 찾기 |
| **DB 저장** | TB_SHIPMENT, TB_BARCODE_INFO 테이블에 저장 |

---

## 2. 사용 위치

| 파일 | 용도 |
|------|------|
| Shipments_Info.java | 출하대상 DTO |
| Barcodes_Info.java | 바코드 정보 DTO |
| DBInfo.java | DB 컬럼 상수 정의 |
| DBHandler.java | TB_SHIPMENT, TB_BARCODE_INFO 테이블 CRUD |
| BixolonShipmentActivity.java | 바코드 매칭 검증 |
| ShipmentActivity.java | 바코드 매칭 검증 |
| ProgressDlgShipSearch.java | 서버 응답 파싱 |
| ProgressDlgBarcodeSearch.java | 바코드 정보 파싱 |

---

## 3. 저장 테이블

| 테이블 | 컬럼 | 용도 |
|--------|------|------|
| TB_SHIPMENT | BARCODEGOODS | 출하대상 바코드상품코드 |
| TB_BARCODE_INFO | BARCODEGOODS | 바코드 정보 상품코드 |
| TB_BARCODE_INFO | BARCODEGOODS_FROM | 상품코드 시작 위치 |
| TB_BARCODE_INFO | BARCODEGOODS_TO | 상품코드 끝 위치 |

---

## 4. 관련 컬럼 (TB_BARCODE_INFO)

| 컬럼명 | 의미 | 예시 |
|--------|------|------|
| **BARCODEGOODS** | 바코드 상품코드 | "880123" |
| **BARCODEGOODS_FROM** | 상품코드 시작 위치 | "0" |
| **BARCODEGOODS_TO** | 상품코드 끝 위치 | "6" |

---

## 5. 사용 코드

### 5.1 서버 응답 파싱

**파일**: ProgressDlgShipSearch.java (Line 289)

```java
si.setBARCODEGOODS(temp[21].toString());     // 바코드상품코드
```

### 5.2 바코드 매칭 검증

**파일**: BixolonShipmentActivity.java (Line 1704~1718)

```java
String bg = bi.getBARCODEGOODS();           // 바코드 상품코드
String bg_from = bi.getBARCODEGOODS_FROM(); // 시작 위치
String bg_to = bi.getBARCODEGOODS_TO();     // 끝 위치

Log.i(TAG, "BARCODEGOODS \t\tFROM : " + bg_from + "\t TO : " + bg_to);
Log.i(TAG, "BARCODEGOODS : \t\t" + bg);

// 스캔한 바코드에서 상품코드 추출
String temp_bg = req.substring(Integer.parseInt(bg_from), Integer.parseInt(bg_to));

Log.i(TAG, "TEMP BARCODEGOODS : \t" + temp_bg);
Log.i(TAG, "TEMP BARCODEGOODS eq : \t" + temp_bg.equals(bg));  // 매칭 확인
```

### 5.3 작업 대상 검색

**파일**: BixolonShipmentActivity.java (Line 3389)

```java
// 바코드상품코드로 작업 대상 찾기
work_ppcode = find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type);
```

### 5.4 DB 조회 조건

**파일**: DBHandler.java (Line 233)

```java
// 바코드상품코드로 출하대상 조회
+ " WHERE BARCODEGOODS = '" + barcodegoods + "' "
```

---

## 6. 바코드 매칭 흐름

```
1. 바코드 스캔
        ↓
2. TB_BARCODE_INFO에서 BARCODEGOODS_FROM, BARCODEGOODS_TO 조회
        ↓
3. 스캔한 바코드에서 상품코드 추출
   barcode.substring(FROM, TO)
        ↓
4. BARCODEGOODS와 비교
        ↓
5. 일치하면 해당 출하대상으로 계근 진행
```

---

## 7. 바코드 구조 예시

```
바코드: 8801234567890123456789
        ↑     ↑
      FROM   TO
       (0)   (6)

추출: 880123 → BARCODEGOODS와 비교
```

| 항목 | 값 |
|------|-----|
| 스캔 바코드 | 8801234567890123456789 |
| BARCODEGOODS_FROM | 0 |
| BARCODEGOODS_TO | 6 |
| 추출 결과 | 880123 |
| BARCODEGOODS | 880123 |
| 매칭 결과 | ✅ 일치 |

---

## 8. 결론

**상태**: ✅ 필수 (삭제 불가)

BARCODEGOODS는 **바코드 매칭의 핵심 컬럼**으로:
- 스캔한 바코드에서 **상품코드 추출 후 매칭**
- `BARCODEGOODS_FROM` ~ `BARCODEGOODS_TO` 위치에서 추출
- 추출한 값과 **BARCODEGOODS 비교**하여 출하대상 확인
- TB_SHIPMENT, TB_BARCODE_INFO 양쪽에서 사용

---

**최종 수정일**: 2026-02-03
