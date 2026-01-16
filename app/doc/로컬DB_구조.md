# 로컬 SQLite DB 구조

**작성일**: 2026-01-15
**소스 기준**: DBHandler.java, DBInfo.java, DBHelper.java

---

## 1. DB 정보

| 항목 | 값 |
|------|-----|
| DB명 | HIGHLAND |
| 버전 | 27 |
| 파일 | DBHelper.java:20-22 |

---

## 2. 테이블 정의 현황

### 2.1 DBInfo.java 정의 (6개)

| 테이블명 | 상수명 | CREATE 위치 | 상태 |
|----------|--------|-------------|------|
| TB_SHIPMENT | TABLE_NAME_SHIPMENT | DBHandler.java:32-75 | 사용 |
| TB_BARCODE_INFO | TABLE_NAME_BARCODE_INFO | DBHandler.java:816-844 | 사용 |
| TB_GOODS_WET | TABLE_NAME_GOODS_WET | DBHandler.java:1169-1193 | 사용 |
| TB_GOODS_WET_PRODUCTION_CALC | TABLE_NAME_GOODS_WET_PRODUCTION_CALC | DBHandler.java:1938-1941 | 사용 |
| TB_PRODUCTION | ~~TABLE_NAME_PRODUCTION~~ | - | **삭제됨** |
| TB_COMPLETE_ITEM | ~~TABLE_NAME_COMPLETE_ITEM~~ | - | **삭제됨** |

### 2.2 실제 사용 테이블 (4개)

| 테이블 | 컬럼 수 | 용도 | 데이터 방향 |
|--------|--------|------|------------|
| TB_SHIPMENT | 41 | 출하대상 저장 | 서버 → 앱 |
| TB_BARCODE_INFO | 26 | 바코드 파싱 정보 | 서버 → 앱 |
| TB_GOODS_WET | 22 | 계근 결과 저장 | 앱 → 서버 |
| TB_GOODS_WET_PRODUCTION_CALC | 1 | 생산 계근 바코드 임시저장 | 로컬 전용 |

### 2.3 테이블 생성 위치

LoginActivity.java:65-68에서 앱 시작 시 4개 테이블 생성:

```java
DBHandler.createqueryShipment(getApplicationContext());           // Line 65
DBHandler.createqueryBarcodeInfo(getApplicationContext());        // Line 66
DBHandler.createqueryGoodsWet(getApplicationContext());           // Line 67
DBHandler.createqueryGoodsWetProductionCalc(getApplicationContext()); // Line 68
```

---

## 3. TB_SHIPMENT (출하대상)

**역할**: 서버에서 조회한 출하대상 데이터 저장
**생성**: DBHandler.java:25-86 (createqueryShipment)
**컬럼 수**: 41개
**데이터 방향**: 서버 → 앱 (다운로드 전용, 서버 전송 없음)

### 3.1 컬럼 목록

| #   | 컬럼명                 | 타입      | NOT NULL | 라인  | 사용여부 | 비고                       |
| --- | ------------------- | ------- | :------: | --- | :--: | ------------------------ |
| 1   | SHIPMENT_ID         | INTEGER | PK, AUTO | 34  |  N   | getter 호출 없음             |
| 2   | GI_H_ID             | TEXT    |    O     | 35  |  DB  | 저장만                      |
| 3   | GI_D_ID             | TEXT    |    O     | 36  |  Y   | 출하대상 매칭, 계근 저장/조회, 서버 전송 |
| 4   | EOI_ID              | TEXT    |    O     | 37  |  DB  | 저장만                      |
| 5   | ITEM_CODE           | TEXT    |    O     | 38  |  Y   | TB_GOODS_WET 저장, 서버 전송   |
| 6   | ITEM_NAME           | TEXT    |    O     | 39  |  Y   | 화면 표시 (상품명)              |
| 7   | EMARTITEM_CODE      | TEXT    |    -     | 40  |  Y   | 바코드 생성 (라벨 인쇄)           |
| 8   | EMARTITEM           | TEXT    |    -     | 41  |  Y   | TB_GOODS_WET 저장          |
| 9   | GI_REQ_PKG          | TEXT    |    O     | 42  |  Y   | 화면 표시 (요청수량), 완료 체크      |
| 10  | GI_REQ_QTY          | TEXT    |    O     | 43  |  Y   | 화면 표시 (요청중량)             |
| 11  | AMOUNT              | TEXT    |    O     | 44  |  DB  | 저장만                      |
| 12  | GOODS_R_ID          | TEXT    |    O     | 45  |  DB  | 저장만                      |
| 13  | GR_REF_NO           | TEXT    |    O     | 46  |  DB  | 저장만                      |
| 14  | GI_REQ_DATE         | TEXT    |    O     | 47  |  Y   | DB 조회 WHERE 조건           |
| 15  | BL_NO               | TEXT    |    O     | 48  |  Y   | BL번호 선택/표시, 계근완료 체크      |
| 16  | BRAND_CODE          | TEXT    |    O     | 49  |  Y   | TB_GOODS_WET 저장, 서버 전송   |
| 17  | BRANDNAME           | TEXT    |    O     | 50  |  DB  | 저장만                      |
| 18  | CLIENT_CODE         | TEXT    |    O     | 51  |  DB  | 저장만 (WHERE 미사용)          |
| 19  | CLIENTNAME          | TEXT    |    O     | 52  |  Y   | 화면 표시 (출하업체명), 리스트       |
| 20  | CENTERNAME          | TEXT    |    -     | 53  |  Y   | 센터 구분 (트레이더스/이마트 등)      |
| 21  | ITEM_SPEC           | TEXT    |    O     | 54  |  Y   | 라벨 인쇄 (상품명/냉장냉동)         |
| 22  | CT_CODE             | TEXT    |    O     | 55  |  Y   | 라벨 인쇄 (원산지)              |
| 23  | IMPORT_ID_NO        | TEXT    |    O     | 56  |  Y   | 바코드 생성, 라벨 인쇄 (수입식별번호)   |
| 24  | PACKER_CODE         | TEXT    |    O     | 57  |  Y   | 킬코이 제품 판별 (라벨 분기)        |
| 25  | PACKERNAME          | TEXT    |    O     | 58  |  DB  | 저장만                      |
| 26  | PACKER_PRODUCT_CODE | TEXT    |    O     | 59  |  Y   | 바코드 매칭, 계근 조회, 서버 전송     |
| 27  | BARCODE_TYPE        | TEXT    |    O     | 60  |  Y   | 바코드 유형 판별                |
| 28  | ITEM_TYPE           | TEXT    |    O     | 61  |  Y   | 상품 타입 판별                 |
| 29  | PACKWEIGHT          | TEXT    |    -     | 62  |  Y   | 팩 중량                     |
| 30  | BARCODEGOODS        | TEXT    |    -     | 63  |  Y   | 바코드 매칭                   |
| 31  | STORE_IN_DATE       | TEXT    |    -     | 64  |  Y   | 납품일자                     |
| 32  | EMARTLOGIS_CODE     | TEXT    |    -     | 65  |  Y   | 바코드 생성 (납품코드)            |
| 33  | EMARTLOGIS_NAME     | TEXT    |    -     | 66  |  DB  | 저장만                      |
| 34  | SAVE_TYPE           | TEXT    |    O     | 67  |  Y   | 전송 여부 판별, 리스트 표시         |
| 35  | WH_AREA             | TEXT    |    -     | 68  |  Y   | 창고 구역                    |
| 36  | USE_NAME            | TEXT    |    -     | 69  |  Y   | 용도명                      |
| 37  | USE_CODE            | TEXT    |    -     | 70  |  Y   | 바코드 생성 (이중바코드)           |
| 38  | CT_NAME             | TEXT    |    -     | 71  |  Y   | 원산지명                     |
| 39  | STORE_CODE          | TEXT    |    -     | 72  |  Y   | 미트센터 판별                  |
| 40  | EMART_PLANT_CODE    | TEXT    |    -     | 73  |  Y   | 바코드 생성 (가공장코드)           |
| 41  | LAST_BOX_ORDER      | INTEGER |    -     | 74  |  Y   | 롯데 박스순서 (Log 출력)         |

**사용여부 범례**: Y=사용, DB=저장만(읽기 없음), N=미사용

#### 3.1.1 컬럼별 상세 사용처

**1. SHIPMENT_ID** (INTEGER, PK) - 미사용
- getSHIPMENT_ID() 호출 없음
- PK 자동 생성 컬럼이나 실제 조회에 사용되지 않음

**2. GI_H_ID** (TEXT) - 저장만
- DBHandler.java:664 - TB_SHIPMENT INSERT 시 저장
- 출고헤더ID이나 앱에서 읽어서 사용하는 곳 없음 (서버 전송 packet에 미포함)

**3. GI_D_ID** (TEXT)
- BixolonShipmentActivity.java:827 - 출하대상 매칭 (바코드 스캔 시 gi_d_id 비교)
- BixolonShipmentActivity.java:1207, 1211 - 중복 체크 조건
- BixolonShipmentActivity.java:1491 - TB_GOODS_WET INSERT 시 값 복사
- BixolonShipmentActivity.java:3328, 3519 - selectqueryListGoodsWetInfo() 조회 조건
- BixolonShipmentActivity.java:3711, 3713 - 서버 전송 WHERE 조건 생성
- BixolonShipmentActivity.java:3729, 3835 - 서버 전송 packet에 포함
- BixolonShipmentActivity.java:3768, 3897 - updatequeryGoodsWet() 조건
- BixolonShipmentActivity.java:3778, 3906 - 완료 문자열 생성 (completeStr)
- BixolonShipmentActivity.java:3801, 3934 - updatequeryShipment() 호출
- BixolonShipmentActivity.java:4377, 4417 - selectqueryGoodsWet(), deletequerySelectGoodsWet() 조건
- ShipmentActivity.java - 동일 패턴
- ProgressDlgShipSearch.java:345, 352, 365 - 출하대상 동기화 비교

**4. EOI_ID** (TEXT) - 저장만
- DBHandler.java:665 - TB_SHIPMENT INSERT 시 저장
- 이마트 출하번호(발주번호)이나 앱에서 읽어서 사용하는 곳 없음 (서버 전송 packet에 미포함)

**5. ITEM_CODE** (TEXT)
- BixolonShipmentActivity.java:1502 - TB_GOODS_WET INSERT 시 값 복사
- BixolonShipmentActivity.java:3739, 3845 - 서버 전송 packet에 포함
- BixolonShipmentActivity.java:3778, 3906 - 완료 문자열 생성 (completeStr)
- ShipmentActivity.java - 동일 패턴

**6. ITEM_NAME** (TEXT)
- BixolonShipmentActivity.java:1924 - 화면 표시 (edit_product_name.setText)
- BixolonShipmentActivity.java:3555 - 화면 표시
- BixolonShipmentActivity.java:4215 - 상세 화면 표시 (detail_edit_ppname)
- ShipmentActivity.java:1829, 3470, 4130 - 동일 패턴

**7. EMARTITEM_CODE** (TEXT)
- BixolonShipmentActivity.java:1500 - TB_GOODS_WET INSERT 시 값 복사
- BixolonShipmentActivity.java:1983-1984 - 바코드 생성 (생산 라벨)
- BixolonShipmentActivity.java:2222-2396 - 바코드 생성 (라벨 타입별)
  - M0 원료육: substring(0,6) + 중량 + 회사코드 + 수입식별번호
  - E2 냉동: substring(0,6) + 중량 + 회사코드 + 수입식별번호
  - E3 냉장: substring(0,6) + 중량 + 회사코드
  - M7 이중바코드: substring(0,6) + 중량 + 회사코드 + 수입식별번호
  - M8 비정량: 상품코드 전체 + 수입식별번호
- BixolonShipmentActivity.java:2979-2998 - 롯데 라벨 바코드 생성
- ShipmentActivity.java - 동일 패턴

**8. EMARTITEM** (TEXT)
- BixolonShipmentActivity.java:1501 - TB_GOODS_WET INSERT 시 값 복사 (이마트 상품명)
- ShipmentActivity.java:1406 - 동일

**9. GI_REQ_PKG** (TEXT)
- BixolonShipmentActivity.java:1148 - BL번호별 계근 완료 체크
- BixolonShipmentActivity.java:1199, 1480 - 요청수량 == 계근수량 완료 체크
- BixolonShipmentActivity.java:1581, 1921 - 화면 표시 (edit_wet_count: "요청/완료")
- BixolonShipmentActivity.java:1618 - 수량 초과 체크
- BixolonShipmentActivity.java:1829, 3418 - 계근 미완료 항목 필터
- BixolonShipmentActivity.java:3405, 3569 - 센터 총 요청수량 합산
- BixolonShipmentActivity.java:3631 - 계근 완료 여부 체크
- BixolonShipmentActivity.java:3777, 3905 - 전송완료 체크 (SAVE_CNT == GI_REQ_PKG)
- BixolonShipmentActivity.java:4217, 4453 - 상세 화면 표시
- ShipmentListAdapter.java:142 - 리스트 아이템 표시

**10. GI_REQ_QTY** (TEXT)
- BixolonShipmentActivity.java:1582, 1922 - 화면 표시 (edit_wet_weight: "요청/완료")
- BixolonShipmentActivity.java:3406, 3570 - 센터 총 요청중량 합산
- BixolonShipmentActivity.java:4218, 4454 - 상세 화면 표시
- ShipmentListAdapter.java:146 - 리스트 아이템 표시

**11. AMOUNT** (TEXT) - 저장만
- DBHandler.java:672 - TB_SHIPMENT INSERT 시 저장
- 출하상품금액이나 앱에서 읽어서 사용하는 곳 없음

**12. GOODS_R_ID** (TEXT) - 저장만
- DBHandler.java:673 - TB_SHIPMENT INSERT 시 저장
- 입고번호이나 앱에서 읽어서 사용하는 곳 없음 (서버 전송 packet에 미포함)

**13. GR_REF_NO** (TEXT) - 저장만
- DBHandler.java:674 - TB_SHIPMENT INSERT 시 저장
- 창고입고번호이나 앱에서 읽어서 사용하는 곳 없음

**14. GI_REQ_DATE** (TEXT)
- DBHandler.java:106 - 로컬 DB 조회 시 WHERE 조건으로 사용
- DBHandler.java:675 - TB_SHIPMENT INSERT 시 저장
- 출하요청일 기준 데이터 필터링

**15. BL_NO** (TEXT)
- BixolonShipmentActivity.java:1148 - BL번호 매칭 (temp_bl_no.equals)
- BixolonShipmentActivity.java:1687 - 현재 작업 BL번호 비교
- BixolonShipmentActivity.java:1895 - BL번호 빈값 체크
- BixolonShipmentActivity.java:1915, 1920 - BL번호 리스트 추가, 현재 작업 BL 설정
- BixolonShipmentActivity.java:3577, 3583, 3599 - BL번호 목록 생성, 스피너 선택
- ShipmentListAdapter.java:149, 152 - 리스트에 BL번호 뒤 4자리 표시

**16. BRAND_CODE** (TEXT)
- BixolonShipmentActivity.java:1503 - TB_GOODS_WET INSERT 시 값 복사
- BixolonShipmentActivity.java:3740, 3846 - 서버 전송 packet에 포함
- BixolonShipmentActivity.java:3778, 3906 - 완료 문자열 생성 (completeStr)
- ProgressDlgBarcodeSearch.java:142 - 바코드 정보 로그

**17. BRANDNAME** (TEXT) - 저장만
- DBHandler.java:678 - TB_SHIPMENT INSERT 시 저장
- 브랜드명이나 앱에서 읽어서 사용하는 곳 없음

**18. CLIENT_CODE** (TEXT) - 저장만
- BixolonShipmentActivity.java:3328, 3519 - selectqueryListGoodsWetInfo() 파라미터 전달
- BixolonShipmentActivity.java:4377 - selectqueryGoodsWet() 파라미터 전달
- **주의**: 파라미터로 전달되지만 WHERE 조건에 미사용 (DBHandler.java:1196, 1328)

**19. CLIENTNAME** (TEXT)
- BixolonShipmentActivity.java:3395, 3559 - 출하대상 선택 리스트 표시 (업체명)
- BixolonShipmentActivity.java:4214 - 상세 화면 표시 (detail_edit_position_name)
- ShipmentListAdapter.java:141 - 리스트 아이템 표시 (holder.position)

**20. CENTERNAME** (TEXT)
- BixolonShipmentActivity.java:645, 1167, 1177 - 센터 구분 체크 (트레이더스/웨트/이마트)
- BixolonShipmentActivity.java:2118 - 라벨 인쇄 분기 (트레이더스 납품분)
- CENTER_NAME_TRD, CENTER_NAME_WET, CENTER_NAME_ET 상수와 비교

**21. ITEM_SPEC** (TEXT)
- BixolonShipmentActivity.java:2002-2006 - 라벨 인쇄 (si.EMARTITEM + " / " + si.ITEM_SPEC)
- ShipmentActivity.java:1907-1911 - 라벨 인쇄 (상품명/냉장냉동 표시)
- DBHandler.java:682 - TB_SHIPMENT INSERT 시 저장

**22. CT_CODE** (TEXT)
- BixolonShipmentActivity.java:2827 - 라벨 인쇄 (원산지 텍스트 출력)
- ShipmentActivity.java:2861 - 라벨 인쇄 (우심 프린터)

**23. IMPORT_ID_NO** (TEXT)
- BixolonShipmentActivity.java:2225-2399 - 바코드 생성에 수입식별번호 포함
  - M0, E2, M7, M8 타입 바코드에 수입식별번호 포함
  - 이중바코드 생성 시 pBarcode2에 포함
- BixolonShipmentActivity.java:2638-2690 - 미트센터 바코드 생성
- BixolonShipmentActivity.java:2831 - 라벨 인쇄 (중량/수입식별번호 뒤 4자리)
- BixolonShipmentActivity.java:3001-3002 - 롯데 라벨 이력번호 바코드
- BixolonShipmentActivity.java:3102 - 라벨 인쇄 텍스트 ("이력(묶음)번호 : ")
- BixolonShipmentActivity.java:3395 - 출하대상 선택 리스트 (업체명/수입식별번호)

**24. PACKER_CODE** (TEXT)
- BixolonShipmentActivity.java:622, 625 - 킬코이 제품 판별 (KILKOY_PACKER_CODE)
- BixolonShipmentActivity.java:1169, 2079 - 킬코이 + 미트센터 조건 체크
- 킬코이 제품은 소비기한 변조 출력 처리

**25. PACKERNAME** (TEXT) - 저장만
- DBHandler.java:686 - TB_SHIPMENT INSERT 시 저장
- 패커명이나 앱에서 읽어서 사용하는 곳 없음

**26. PACKER_PRODUCT_CODE** (TEXT)
- BixolonShipmentActivity.java:1211 - 중복 체크 조건 (duplicatequeryGoodsWet)
- BixolonShipmentActivity.java:3328, 3519, 3611 - 계근 조회/중복 체크 조건
- BixolonShipmentActivity.java:3801, 3934 - updatequeryShipment() 조건
- BixolonShipmentActivity.java:4377 - selectqueryGoodsWet() 조회 조건
- DetailAdapter.java - 상세 표시

**27. BARCODE_TYPE** (TEXT)
- BixolonShipmentActivity.java - 바코드 유형별 분기 처리
- 라벨 타입 (E2, E3, M0, M6, M7, M8 등) 결정에 사용

**28. ITEM_TYPE** (TEXT)
- BixolonShipmentActivity.java - 상품 타입별 분기 처리

**29. PACKWEIGHT** (TEXT)
- BixolonShipmentActivity.java - 팩 중량 사용

**30. BARCODEGOODS** (TEXT)
- BixolonShipmentActivity.java - 바코드 상품코드 매칭
- ProgressDlgBarcodeSearch.java:142 - 바코드 정보 로그

**31. STORE_IN_DATE** (TEXT)
- BixolonShipmentActivity.java - 납품일자 사용

**32. EMARTLOGIS_CODE** (TEXT)
- BixolonShipmentActivity.java:2231-2398 - 바코드 생성 (납품코드)
  - pBarcode2 생성 시 EMARTLOGIS_CODE.substring(0,6) 사용
- BixolonShipmentActivity.java:2638, 2689 - 미트센터 바코드 생성

**33. EMARTLOGIS_NAME** (TEXT) - 저장만
- DBHandler.java:694 - TB_SHIPMENT INSERT 시 저장
- 납품명이나 앱에서 읽어서 사용하는 곳 없음

**34. SAVE_TYPE** (TEXT)
- BixolonShipmentActivity.java - 전송 여부 판별
- ShipmentListAdapter.java - 리스트 표시 시 전송 상태 확인
- DetailAdapter.java - 상세 표시

**35. WH_AREA** (TEXT)
- BixolonShipmentActivity.java - 창고 구역 사용

**36. USE_NAME** (TEXT)
- BixolonShipmentActivity.java - 용도명 사용

**37. USE_CODE** (TEXT)
- BixolonShipmentActivity.java:2376-2377 - 이중바코드 생성 (pBarcode2)
- ShipmentActivity.java:2290-2291 - 동일 패턴

**38. CT_NAME** (TEXT)
- BixolonShipmentActivity.java - 원산지명 사용

**39. STORE_CODE** (TEXT)
- BixolonShipmentActivity.java:1169 - 미트센터 판별 (MEAT_CENTER_STORE_CODE)
- BixolonShipmentActivity.java:2079 - 킬코이/미트센터 조건 체크

**40. EMART_PLANT_CODE** (TEXT)
- BixolonShipmentActivity.java:2638-2639 - 미트센터 바코드 생성에 가공장코드 포함
- ShipmentActivity.java:2628-2629 - 동일 패턴

**41. LAST_BOX_ORDER** (INTEGER)
- BixolonShipmentActivity.java:3348 - Log.e() 출력용
- ShipmentActivity.java:3263 - Log.e() 출력용
- 롯데 마지막 박스 순서, Log 출력에만 사용

### 3.2 VIEW idx ↔ 로컬DB # 인덱스 오프셋

VIEW와 로컬DB 컬럼 순서가 다름. 로컬 전용 컬럼이 삽입되어 오프셋 발생.

#### 로컬 전용 컬럼 (VIEW 미수신)

| 로컬DB # | 컬럼명 | 삽입 위치 |
|:--------:|--------|----------|
| 1 | SHIPMENT_ID | 맨 앞 (PK) |
| 34 | SAVE_TYPE | 중간 (EMARTLOGIS_NAME 다음) |
| 41 | LAST_BOX_ORDER | 맨 뒤 (롯데만 VIEW 수신) |

#### 오프셋 계산

| 구간 | VIEW idx | 로컬DB # | 오프셋 |
|------|:--------:|:--------:|:------:|
| 전반부 | 0 ~ 31 | 2 ~ 33 | +2 |
| 후반부 | 32 ~ 37 | 35 ~ 40 | +3 |

#### 오프셋 발생 지점 (DBHandler.java:66-68)

```
#33 EMARTLOGIS_NAME (VIEW idx 31)
#34 SAVE_TYPE       ← 로컬 전용 삽입
#35 WH_AREA         (VIEW idx 32, 오프셋 +3)
```

### 3.3 데이터 원본

| 원본 | VIEW 조회 |
|------|-----------|
| 이마트 | VW_PDA_WID_LIST |
| 홈플러스 | VW_PDA_WID_HOMEPLUS_LIST |
| 도매 | VW_PDA_WID_WHOLESALE_LIST |
| 비정량 | VW_PDA_WID_LIST_NONFIXED |
| 홈플러스 비정량 | VW_PDA_WID_LIST_NONFIXED_HP |
| 롯데 | VW_PDA_WID_LIST_LOTTE |

### 3.4 주요 CRUD 메서드

| 메서드                              | 동작                         | 라인        |
| -------------------------------- | -------------------------- | --------- |
| createqueryShipment()            | CREATE TABLE               | 25-86     |
| insertqueryShipment()            | INSERT                     | 654-760   |
| selectqueryShipment()            | SELECT (조건 조회)             | 89-217    |
| selectqueryShipmentOnly()        | SELECT (바코드 조회)            | 220-322   |
| selectqueryShipmentBL()          | SELECT (BL 조회)             | 324-426   |
| selectqueryAllShipment()         | SELECT ALL                 | 429-465   |
| selectqueryCodeList()            | SELECT DISTINCT 코드 목록      | 507-542   |
| selectqueryCodeListForNonFixed() | SELECT (비고정 코드 목록)         | 544-579   |
| selectqueryGIDIDList()           | SELECT DISTINCT GI_D_ID    | 582-615   |
| selectqueryCenterList()          | SELECT DISTINCT CENTERNAME | 618-651   |
| updatequeryShipment()            | UPDATE                     | 786-809   |
| deletequeryShipment()            | DELETE ALL                 | 763-783   |
| refreshShipmentList()            | DELETE + INSERT (갱신)       | 1854-1893 |

### 3.5 CRUD 호출 시점

| 동작     | 시점                           | 위치                                      |
| ------ | ---------------------------- | --------------------------------------- |
| INSERT | 출하대상받기 버튼 클릭                 | ProgressDlgShipSearch.java              |
| INSERT | refreshShipmentList()        | DBHandler.java:1878                     |
| SELECT | 바코드 스캔 시 출하대상 매칭             | BixolonShipmentActivity.java:3324       |
| SELECT | BL번호 조회                      | BixolonShipmentActivity.java:3515       |
| UPDATE | 서버 전송 성공 후 LAST_BOX_ORDER 갱신 | BixolonShipmentActivity.java:3801, 3934 |
| DELETE | 출하대상받기 재실행 시 전체 삭제           | MainActivity.java:206, 468              |

---

## 4. TB_BARCODE_INFO (바코드 정보)

**역할**: 바코드 파싱 규칙 저장 (S_BARCODE_INFO 서버 테이블에서 조회)
**생성**: DBHandler.java:812-856 (createqueryBarcodeInfo)
**컬럼 수**: 26개
**데이터 방향**: 서버 → 앱 (다운로드 전용, 서버 전송 없음)

### 4.1 컬럼 목록

| #   | 컬럼명                  | 타입      | NOT NULL | 라인  | 사용여부 | 비고  |
| --- | -------------------- | ------- | :------: | --- | :--: | --- |
| 1   | BARCODE_INFO_ID      | INTEGER | PK, AUTO | 818 | N    | getter 호출 없음 |
| 2   | PACKER_CLIENT_CODE   | TEXT    |    O     | 819 | DB   | INSERT/로그만 |
| 3   | PACKER_PRODUCT_CODE  | TEXT    |    O     | 820 | Y    | 바코드 조회 결과 표시 |
| 4   | PACKER_PRD_NAME      | TEXT    |    O     | 821 | DB   | INSERT/로그만 |
| 5   | ITEM_CODE            | TEXT    |    O     | 822 | DB   | INSERT만 |
| 6   | ITEM_NAME_KR         | TEXT    |    -     | 823 | Y    | 화면 표시 (한글 상품명) |
| 7   | BRAND_CODE           | TEXT    |    O     | 824 | DB   | INSERT/로그만 |
| 8   | BARCODEGOODS         | TEXT    |    O     | 825 | Y    | 바코드 매칭 |
| 9   | BASEUNIT             | TEXT    |    O     | 826 | DB   | INSERT/로그만 |
| 10  | ZEROPOINT            | TEXT    |    O     | 827 | DB   | INSERT/로그만 |
| 11  | PACKER_PRD_CODE_FROM | TEXT    |    -     | 828 | DB   | INSERT/로그만 |
| 12  | PACKER_PRD_CODE_TO   | TEXT    |    -     | 829 | DB   | INSERT/로그만 |
| 13  | BARCODEGOODS_FROM    | TEXT    |    O     | 830 | Y    | 바코드 파싱 (상품코드 시작) |
| 14  | BARCODEGOODS_TO      | TEXT    |    O     | 831 | Y    | 바코드 파싱 (상품코드 끝) |
| 15  | WEIGHT_FROM          | TEXT    |    O     | 832 | DB   | INSERT/로그만 |
| 16  | WEIGHT_TO            | TEXT    |    O     | 833 | DB   | INSERT/로그만 |
| 17  | MAKINGDATE_FROM      | TEXT    |    -     | 834 | DB   | INSERT만 |
| 18  | MAKINGDATE_TO        | TEXT    |    -     | 835 | DB   | INSERT만 |
| 19  | BOXSERIAL_FROM       | TEXT    |    -     | 836 | DB   | INSERT만 |
| 20  | BOXSERIAL_TO         | TEXT    |    -     | 837 | DB   | INSERT만 |
| 21  | STATUS               | TEXT    |    -     | 838 | DB   | INSERT/로그만 |
| 22  | REG_ID               | TEXT    |    O     | 839 | DB   | INSERT/로그만 |
| 23  | REG_DATE             | TEXT    |    -     | 840 | DB   | 로그만 |
| 24  | REG_TIME             | TEXT    |    -     | 841 | DB   | 로그만 |
| 25  | MEMO                 | TEXT    |    -     | 842 | DB   | 로그만 |
| 26  | SHELF_LIFE           | TEXT    |    -     | 843 | DB   | INSERT/로그만 |

**사용여부 범례**: Y=사용, DB=저장만(읽기 없음 또는 로그만), N=미사용

#### 4.1.1 컬럼별 상세 사용처

**1. BARCODE_INFO_ID** (INTEGER, PK) - 미사용
- getBARCODE_INFO_ID() 호출 없음
- PK 자동 생성 컬럼이나 실제 조회에 사용되지 않음

**2. PACKER_CLIENT_CODE** (TEXT)
- DBHandler.java:1023 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:141 - 바코드 정보 수신 시 로그 출력용
- 실제 비즈니스 로직에서 읽어서 사용하는 곳 없음

**3. PACKER_PRODUCT_CODE** (TEXT)
- BixolonShipmentActivity.java - 바코드 조회 결과 화면 표시
- ShipmentActivity.java - 동일 패턴
- DBHandler.java:1024 - TB_BARCODE_INFO INSERT 시 저장
- 바코드 스캔 시 매칭된 상품정보 표시에 사용

**4. PACKER_PRD_NAME** (TEXT)
- DBHandler.java:1026 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:141 - 바코드 정보 수신 시 로그 출력용
- 실제 비즈니스 로직에서 읽어서 사용하는 곳 없음

**5. ITEM_CODE** (TEXT)
- DBHandler.java:1027 - TB_BARCODE_INFO INSERT 시 저장
- 실제 비즈니스 로직에서 읽어서 사용하는 곳 없음

**6. ITEM_NAME_KR** (TEXT)
- BixolonShipmentActivity.java - 바코드 조회 결과 화면 표시 (한글 상품명)
- ShipmentActivity.java - 동일 패턴
- DBHandler.java:1028 - TB_BARCODE_INFO INSERT 시 저장
- 바코드 스캔 시 매칭된 상품의 한글명 표시에 사용

**7. BRAND_CODE** (TEXT)
- DBHandler.java:1024 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:142 - 바코드 정보 수신 시 로그 출력용
- 실제 비즈니스 로직에서 읽어서 사용하는 곳 없음

**8. BARCODEGOODS** (TEXT)
- BixolonShipmentActivity.java - 바코드 매칭 시 상품코드 비교
- ShipmentActivity.java - 동일 패턴
- DBHandler.java:1029 - TB_BARCODE_INFO INSERT 시 저장
- 스캔한 바코드에서 추출한 상품코드와 비교하여 출하대상 매칭

**9. BASEUNIT** (TEXT)
- DBHandler.java:1030 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:142 - 바코드 정보 수신 시 로그 출력용
- 중량 단위 (LB, KG) 정보이나 앱에서 직접 사용하지 않음

**10. ZEROPOINT** (TEXT)
- DBHandler.java:1031 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:143 - 바코드 정보 수신 시 로그 출력용
- 소수점 자릿수 정보이나 앱에서 직접 사용하지 않음

**11. PACKER_PRD_CODE_FROM** (TEXT)
- DBHandler.java:1032 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:143 - 바코드 정보 수신 시 로그 출력용
- 패커 상품코드 추출 시작 위치이나 앱에서 직접 사용하지 않음

**12. PACKER_PRD_CODE_TO** (TEXT)
- DBHandler.java:1033 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:143 - 바코드 정보 수신 시 로그 출력용
- 패커 상품코드 추출 끝 위치이나 앱에서 직접 사용하지 않음

**13. BARCODEGOODS_FROM** (TEXT)
- BixolonShipmentActivity.java - 바코드 파싱 시 상품코드 추출 시작 위치
- ShipmentActivity.java - 동일 패턴
- DBHandler.java:1034 - TB_BARCODE_INFO INSERT 시 저장
- 바코드 문자열에서 상품코드 substring 시작 인덱스

**14. BARCODEGOODS_TO** (TEXT)
- BixolonShipmentActivity.java - 바코드 파싱 시 상품코드 추출 끝 위치
- ShipmentActivity.java - 동일 패턴
- DBHandler.java:1035 - TB_BARCODE_INFO INSERT 시 저장
- 바코드 문자열에서 상품코드 substring 끝 인덱스

**15. WEIGHT_FROM** (TEXT)
- DBHandler.java:1036 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:144 - 바코드 정보 수신 시 로그 출력용
- 중량 추출 시작 위치이나 앱에서 직접 사용하지 않음

**16. WEIGHT_TO** (TEXT)
- DBHandler.java:1037 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:145 - 바코드 정보 수신 시 로그 출력용
- 중량 추출 끝 위치이나 앱에서 직접 사용하지 않음

**17. MAKINGDATE_FROM** (TEXT)
- DBHandler.java:1038 - TB_BARCODE_INFO INSERT 시 저장
- 제조일 추출 시작 위치이나 앱에서 직접 사용하지 않음

**18. MAKINGDATE_TO** (TEXT)
- DBHandler.java:1039 - TB_BARCODE_INFO INSERT 시 저장
- 제조일 추출 끝 위치이나 앱에서 직접 사용하지 않음

**19. BOXSERIAL_FROM** (TEXT)
- DBHandler.java:1040 - TB_BARCODE_INFO INSERT 시 저장
- 박스번호 추출 시작 위치이나 앱에서 직접 사용하지 않음

**20. BOXSERIAL_TO** (TEXT)
- DBHandler.java:1041 - TB_BARCODE_INFO INSERT 시 저장
- 박스번호 추출 끝 위치이나 앱에서 직접 사용하지 않음

**21. STATUS** (TEXT)
- DBHandler.java:1042 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:145 - 바코드 정보 수신 시 로그 출력용
- 사용여부(Y/N) 정보이나 앱에서 필터링에 사용하지 않음

**22. REG_ID** (TEXT)
- DBHandler.java:1043 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:145 - 바코드 정보 수신 시 로그 출력용
- 등록자 ID이나 앱에서 직접 사용하지 않음

**23. REG_DATE** (TEXT)
- ProgressDlgBarcodeSearch.java:145 - 바코드 정보 수신 시 로그 출력용
- DBHandler INSERT에서는 컬럼 누락 (DB 저장 안함)
- 등록일자이나 앱에서 직접 사용하지 않음

**24. REG_TIME** (TEXT)
- ProgressDlgBarcodeSearch.java:145 - 바코드 정보 수신 시 로그 출력용
- DBHandler INSERT에서는 컬럼 누락 (DB 저장 안함)
- 등록시간이나 앱에서 직접 사용하지 않음

**25. MEMO** (TEXT)
- ProgressDlgBarcodeSearch.java:145 - 바코드 정보 수신 시 로그 출력용
- DBHandler INSERT에서는 컬럼 누락 (DB 저장 안함)
- 메모이나 앱에서 직접 사용하지 않음

**26. SHELF_LIFE** (TEXT)
- DBHandler.java:1044 - TB_BARCODE_INFO INSERT 시 저장
- ProgressDlgBarcodeSearch.java:145 - 바코드 정보 수신 시 로그 출력용
- 유통기한 정보이나 앱에서 직접 사용하지 않음

### 4.2 바코드 파싱 컬럼 설명

| 컬럼 | 용도 | 예시 |
|------|------|------|
| BARCODEGOODS_FROM/TO | 상품코드 추출 위치 | 4~16 → substring(3, 16) |
| WEIGHT_FROM/TO | 중량 추출 위치 | 17~20 → substring(16, 20) |
| MAKINGDATE_FROM/TO | 제조일 추출 위치 | 21~26 → substring(20, 26) |
| BOXSERIAL_FROM/TO | 박스번호 추출 위치 | 27~29 → substring(26, 29) |

### 4.3 주요 CRUD 메서드

| 메서드 | 동작 | 라인 |
|--------|------|------|
| createqueryBarcodeInfo() | CREATE TABLE | 812-856 |
| insertqueryBarcodeInfo() | INSERT | 1031-1100 |
| selectqueryBarcodeInfo() | SELECT (조건 조회) | 859-942 |
| selectqueryBarcodeGoodsInfo() | SELECT (상품 조회) | 945-1028 |
| updatequeryBarcodeInfo() | UPDATE | 1103-1140 |
| deletequeryBarcodeInfo() | DELETE ALL | 1143-1162 |
| selectquerySearchBarcodeInfo() | SELECT (상품코드+브랜드 조회) | 1795-1851 |

### 4.4 CRUD 호출 시점

| 동작     | 시점                  | 위치                                 |
| ------ | ------------------- | ---------------------------------- |
| INSERT | 바코드정보받기 버튼 클릭       | ProgressDlgBarcodeSearch.java:138  |
| INSERT | 신규 바코드 수동 등록        | ProgressDlgNewBarcodeInfo.java:99  |
| SELECT | 바코드 스캔 시 파싱 규칙 조회   | BixolonShipmentActivity.java:1700  |
| UPDATE | **사용 안함** (정의만 존재)  | -                                  |
| DELETE | 바코드정보받기 재실행 시 전체 삭제 | MainActivity.java:483              |
| DELETE | 계근 데이터 동기화 시 함께 삭제  | ProgressDlgGoodsWetSearch.java:150 |

---

## 5. TB_GOODS_WET (계근 결과)

**역할**: 계근 입력 결과 저장 (서버 전송 전 로컬 저장)
**생성**: DBHandler.java:1165-1205 (createqueryGoodsWet)
**컬럼 수**: 22개
**데이터 방향**: 앱 → 서버 (업로드, insert_goods_wet.jsp로 전송)

### 5.1 컬럼 목록

| # | 컬럼명 | 타입 | NOT NULL | 라인 |
|---|--------|------|:--------:|------|
| 1 | GOODS_WET_ID | INTEGER | PK, AUTO | 1171 |
| 2 | GI_D_ID | TEXT | O | 1172 |
| 3 | WEIGHT | TEXT | O | 1173 |
| 4 | WEIGHT_UNIT | TEXT | O | 1174 |
| 5 | PACKER_PRODUCT_CODE | TEXT | O | 1175 |
| 6 | BARCODE | TEXT | - | 1176 |
| 7 | PACKER_CLIENT_CODE | TEXT | O | 1177 |
| 8 | MAKINGDATE | TEXT | - | 1178 |
| 9 | BOXSERIAL | TEXT | - | 1179 |
| 10 | BOX_CNT | INTEGER | O | 1180 |
| 11 | EMARTITEM_CODE | TEXT | - | 1181 |
| 12 | EMARTITEM | TEXT | - | 1182 |
| 13 | ITEM_CODE | TEXT | - | 1183 |
| 14 | BRAND_CODE | TEXT | - | 1184 |
| 15 | REG_ID | TEXT | - | 1185 |
| 16 | REG_DATE | TEXT | - | 1186 |
| 17 | REG_TIME | TEXT | O | 1187 |
| 18 | SAVE_TYPE | TEXT | O | 1188 |
| 19 | MEMO | TEXT | - | 1189 |
| 20 | DUPLICATE | TEXT | - | 1190 |
| 21 | CLIENT_TYPE | TEXT | - | 1191 |
| 22 | BOX_ORDER | INTEGER | - | 1192 |

### 5.2 서버 전송 필드 (insert_goods_wet.jsp)

TB_GOODS_WET에서 서버로 전송되는 필드:

| 전송 필드 | TB_GOODS_WET 컬럼 |
|-----------|-------------------|
| goods_r_id | TB_SHIPMENT.GOODS_R_ID |
| eoi_id | TB_SHIPMENT.EOI_ID |
| gi_h_id | TB_SHIPMENT.GI_H_ID |
| gi_d_id | GI_D_ID |
| weight | WEIGHT |
| weight_unit | WEIGHT_UNIT |
| packer_code | TB_SHIPMENT.PACKER_CODE |
| packer_product_code | PACKER_PRODUCT_CODE |
| barcode | BARCODE |
| packer_client_code | PACKER_CLIENT_CODE |
| makingdate | MAKINGDATE |
| boxserial | BOXSERIAL |
| item_code | ITEM_CODE |
| brand_code | BRAND_CODE |

### 5.3 주요 CRUD 메서드

| 메서드 | 동작 | 라인 |
|--------|------|------|
| createqueryGoodsWet() | CREATE TABLE | 1165-1205 |
| insertqueryGoodsWet() | INSERT | 1467-1535 |
| insertqueryGoodsWetHomeplus() | INSERT (홈플러스) | 1537-1608 |
| insertqueryGoodsWetLotte() | INSERT (롯데) | 1610-1681 |
| selectqueryGoodsWet() | SELECT (조건 조회) | 1208-1275 |
| selectquerySendGoodsWet() | SELECT (전송용) | 1278-1350 |
| updatequeryGoodsWet() | UPDATE | 1685-1711 |
| deletequerySelectGoodsWet() | DELETE (조건 삭제) | 1714-1747 |
| deletequeryGoodsWet() | DELETE (전송완료분) | 1750-1770 |
| deletequeryAllGoodsWet() | DELETE ALL | 1773-1792 |
| selectqueryListGoodsWetInfo() | SELECT (GI_D_ID별 정보) | 1352-1393 |
| duplicatequeryGoodsWet_check() | SELECT (바코드 중복 체크) | 1396-1427 |
| duplicatequeryGoodsWet() | SELECT (바코드+GI_D_ID+PP_CODE 중복 체크) | 1430-1464 |
| selectMaxBoxOrder() | SELECT MAX(BOX_ORDER) | 1891-1931 |

### 5.4 CRUD 호출 시점

| 동작 | 시점 | 위치 |
|------|------|------|
| INSERT | 계근 완료 (일반: 이마트, 도매, 비정량) | BixolonShipmentActivity.java:1525 |
| INSERT | 계근 완료 (홈플러스, searchType 2) | BixolonShipmentActivity.java:1513 |
| INSERT | 계근 완료 (롯데, searchType 6) | BixolonShipmentActivity.java:1518 |
| INSERT | 서버에서 미전송 계근 데이터 동기화 | ProgressDlgGoodsWetSearch.java:116 |
| SELECT | 바코드 스캔 전 중복 체크 | BixolonShipmentActivity.java:1080, 1210 |
| SELECT | 특정 품목 계근 내역 조회 | BixolonShipmentActivity.java:4377 |
| SELECT | 전송용 데이터 조회 (SAVE_TYPE='N') | DBHandler.java:1278 |
| UPDATE | 서버 전송 성공 후 SAVE_TYPE='Y' 변경 | BixolonShipmentActivity.java:3768, 3897 |
| DELETE | 서버 전송 완료 후 SAVE_TYPE='Y' 삭제 | DBHandler.java:1750 |
| DELETE | 사용자가 개별 계근 삭제 | BixolonShipmentActivity.java:4416 |
| DELETE | 설정에서 전체 삭제 | SettingActivity.java:133 |
| DELETE | 바코드정보받기 시 함께 삭제 | MainActivity.java:484 |

---

## 6. TB_GOODS_WET_PRODUCTION_CALC (생산계근 바코드)

**역할**: 생산 계근 시 바코드 임시 저장 (중복 체크용)
**생성**: DBHandler.java:1934-1953 (createqueryGoodsWetProductionCalc)
**컬럼 수**: 1개
**데이터 방향**: 로컬 전용 (서버 통신 없음)

### 6.1 컬럼 목록

| # | 컬럼명 | 타입 | NOT NULL | 라인 |
|---|--------|------|:--------:|------|
| 1 | BARCODE | TEXT | - | 1940 |

### 6.2 주요 CRUD 메서드

| 메서드 | 동작 | 라인 |
|--------|------|------|
| createqueryGoodsWetProductionCalc() | CREATE TABLE | 1934-1953 |
| insertGoodsWetProductionCalc() | INSERT | 1956-1983 |
| selectGoodsWetProductionCalc() | SELECT (중복 체크) | 1986-2014 |
| deleteGoodsWetProductionCalc() | DELETE ALL | 2016-2033 |

### 6.3 CRUD 호출 시점

| 동작 | 시점 | 위치 |
|------|------|------|
| INSERT | 생산 계근 바코드 스캔 (중복 아닐 때만) | ProductionActivity.java:496 |
| SELECT | 바코드 스캔 시 중복 체크 | ProductionActivity.java:492 |
| UPDATE | **없음** (해당 테이블은 UPDATE 미사용) | - |
| DELETE | 생산 화면 진입 시 초기화 | ProductionActivity.java:171 |
| DELETE | 생산 작업 완료 후 | ProductionActivity.java:283 |
| DELETE | 화면 종료 시 (onDestroy) | ProductionActivity.java:623 |

---

## 7. 삭제된 테이블 (미사용)

아래 테이블들은 DBInfo.java에 상수 정의만 있었고, 실제로 사용되지 않아 **2026-01-15에 소스에서 삭제됨**.

### 7.1 TB_PRODUCTION

| 항목 | 내용 |
|------|------|
| 상수명 | TABLE_NAME_PRODUCTION |
| 원래 위치 | DBInfo.java:6 |
| 삭제 사유 | CREATE TABLE 없음, 관련 메서드 데드코드 |
| 관련 삭제 코드 | selectqueryAllProduction() (DBHandler.java:467-504) |

**삭제된 코드 내용**:
```java
// DBInfo.java에서 삭제됨
public static final String TABLE_NAME_PRODUCTION = "TB_PRODUCTION";

// DBHandler.java에서 삭제됨 (데드코드)
public static ArrayList<String[]> selectqueryAllProduction(Context context) {
    // TB_PRODUCTION 테이블이 존재하지 않아 항상 실패하는 코드였음
}
```

### 7.2 TB_COMPLETE_ITEM

| 항목 | 내용 |
|------|------|
| 상수명 | TABLE_NAME_COMPLETE_ITEM |
| 원래 위치 | DBInfo.java:10 |
| 삭제 사유 | 상수 정의만 존재, CREATE TABLE 없음, 사용 코드 없음 |
| 관련 삭제 코드 | 없음 (정의만 있었음) |

**삭제된 코드 내용**:
```java
// DBInfo.java에서 삭제됨
public static final String TABLE_NAME_COMPLETE_ITEM = "TB_COMPLETE_ITEM";
```

---

## 8. 테이블 관계도

```
┌─────────────────────────────────────────────────────────┐
│                    서버 데이터 흐름                       │
└─────────────────────────────────────────────────────────┘
              │                              │
              ▼                              ▼
    ┌─────────────────┐           ┌───────────────────────┐
    │ VW_PDA_WID_LIST │           │    S_BARCODE_INFO     │
    │ (출하대상 VIEW)  │           │  (바코드 파싱 정보)   │
    └────────┬────────┘           └───────────┬───────────┘
             │                                │
             ▼                                ▼
    ┌─────────────────┐           ┌───────────────────────┐
    │  TB_SHIPMENT    │◄──────────│   TB_BARCODE_INFO     │
    │ (출하대상 저장)  │  바코드    │  (파싱 규칙 저장)     │
    │   41 컬럼       │  매칭     │     26 컬럼           │
    └────────┬────────┘           └───────────────────────┘
             │
             │ 계근 입력
             ▼
    ┌─────────────────┐
    │  TB_GOODS_WET   │─────────► 서버 전송
    │  (계근 결과)    │           (insert_goods_wet.jsp)
    │   22 컬럼       │
    └─────────────────┘
```

---

## 9. 데이터 흐름

### 9.1 출하대상 받기

```
1. MainActivity → "출하대상받기" 버튼 클릭
2. ProgressDlgShipSearch → 서버 VIEW 조회
3. DBHandler.insertqueryShipment() → TB_SHIPMENT에 저장 (Line 654)
```

### 9.2 바코드 정보 받기

```
1. MainActivity → "바코드정보받기" 버튼 클릭
2. ProgressBarcodeinfoSearch → S_BARCODE_INFO 조회
3. DBHandler.insertqueryBarcodeInfo() → TB_BARCODE_INFO에 저장 (Line 1031)
```

### 9.3 계근 입력

```
1. BixolonShipmentActivity → 바코드 스캔
2. TB_BARCODE_INFO에서 파싱 규칙 조회 (selectqueryBarcodeInfo, Line 859)
3. TB_SHIPMENT에서 매칭 데이터 조회 (selectqueryShipment, Line 89)
4. 계근값 입력 후 DBHandler.insertqueryGoodsWet() → TB_GOODS_WET에 저장 (Line 1467)
5. "작업내역전송" → insert_goods_wet.jsp로 서버 전송
```

---

## 10. 관련 파일

| 파일 | 역할 |
|------|------|
| DBInfo.java | 테이블명, 컬럼명 상수 정의 |
| DBHandler.java | CRUD 메서드 구현 |
| DBHelper.java | SQLiteOpenHelper 래퍼 |
| LoginActivity.java | 테이블 생성 호출 (Line 65-68) |
| BixolonShipmentActivity.java | TB_GOODS_WET 데이터 생성 |
| ProgressDlgShipSearch.java | TB_SHIPMENT 데이터 생성 |
| ProgressBarcodeinfoSearch.java | TB_BARCODE_INFO 데이터 생성 |

---

## 11. DEFAULT 값

| 테이블 | 컬럼 | DEFAULT | 라인 |
|--------|------|---------|------|
| TB_GOODS_WET | BOX_ORDER | 0 | 1192 |

**참고**: 다른 컬럼들은 DEFAULT 값이 없음. NULL 허용 컬럼은 빈 문자열 저장 가능.

---

## 12. 중복 체크 로직

### 12.1 duplicatequeryGoodsWet_check()

**용도**: 바코드 단순 중복 체크 (생산 계근)
**위치**: DBHandler.java:1396-1427

```
바코드 스캔
    │
    ▼
SELECT COUNT(*) FROM TB_GOODS_WET WHERE BARCODE = ?
    │
    ├─ count > 0 → 중복 (true 반환)
    │
    └─ count = 0 → 중복 아님 (false 반환)
```

### 12.2 duplicatequeryGoodsWet()

**용도**: 바코드 + GI_D_ID + PACKER_PRODUCT_CODE 복합 중복 체크 (출하 계근)
**위치**: DBHandler.java:1430-1464

```
바코드 스캔
    │
    ▼
SELECT COUNT(*) FROM TB_GOODS_WET
WHERE BARCODE = ?
  AND GI_D_ID = ?
  AND PACKER_PRODUCT_CODE = ?
    │
    ├─ count > 0 → 중복 (true 반환)
    │
    └─ count = 0 → 중복 아님 (false 반환)
```

**차이점**:
| 메서드 | 조건 | 사용 화면 |
|--------|------|----------|
| duplicatequeryGoodsWet_check | BARCODE만 | 생산 계근 (searchType 1, 7) |
| duplicatequeryGoodsWet | BARCODE + GI_D_ID + PP_CODE | 출하 계근 (그 외) |

---

## 13. searchType별 VIEW 매핑

| searchType | 기능 | 서버 VIEW |
|:----------:|------|-----------|
| 0 | 이마트 출하 | VW_PDA_WID_LIST |
| 1 | 생산 계근 | VW_PDA_WID_PRODUCTION_LIST |
| 2 | 홈플러스 출하 | VW_PDA_WID_HOMEPLUS_LIST |
| 3 | 도매업체 출하 | VW_PDA_WID_WHOLESALE_LIST |
| 4 | 비정량 출하 | VW_PDA_WID_LIST_NONFIXED |
| 5 | 홈플러스 비정량 | VW_PDA_WID_LIST_NONFIXED_HP |
| 6 | 롯데 출하 | VW_PDA_WID_LIST_LOTTE |
| 7 | 생산 라벨 | VW_PDA_WID_PRODUCTION_LIST |

---

## 14. INDEX 정보

**현재 상태**: 별도 INDEX 생성 없음

모든 테이블은 PRIMARY KEY (AUTOINCREMENT)만 사용:
- TB_SHIPMENT: SHIPMENT_ID
- TB_BARCODE_INFO: BARCODE_INFO_ID
- TB_GOODS_WET: GOODS_WET_ID

**검색 성능 고려 사항**:
- TB_SHIPMENT.GI_D_ID - 자주 조회되나 INDEX 없음
- TB_GOODS_WET.BARCODE - 중복 체크에 사용되나 INDEX 없음
- 데이터량이 적어 INDEX 미적용 상태

---

**최종 수정일**: 2026-01-16
