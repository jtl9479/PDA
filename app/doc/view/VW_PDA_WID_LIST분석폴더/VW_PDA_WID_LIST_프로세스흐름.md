# VW_PDA_WID_LIST 조회 및 이후 프로세스 흐름

**작성일**: 2026-02-03

---

## 1. 전체 프로세스 개요

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PDA 출하 계근 프로세스                               │
└─────────────────────────────────────────────────────────────────────────────────┘

[1] MainActivity          [2] ProgressDlgShipSearch    [3] ProgressDlgBarcodeSearch
    "대상받기" 클릭  ─────▶  서버 VIEW 조회  ─────────▶  바코드 정보 조회
         │                      │                            │
         │                      ▼                            ▼
         │               VW_PDA_WID_LIST              VW_BARCODE_INFO
         │                      │                            │
         │                      ▼                            ▼
         │               TB_SHIPMENT 저장            TB_BARCODE_INFO 저장
         │                      │                            │
         └──────────────────────┴────────────────────────────┘
                                        │
[4] MainActivity                        │
    "계근입력시작" 클릭  ◀──────────────┘
         │
         ▼
[5] BixolonShipmentActivity
    - 출하대상 표시
    - 바코드 스캔
    - 계근 입력
    - 라벨 출력
         │
         ▼
[6] 서버 전송
    계근 결과 업로드
```

---

## 2. 단계별 상세 설명

### 2.1 [Step 1] MainActivity - 대상받기

**파일**: `MainActivity.java`

```java
// 버튼 클릭 시 downloadShipmentList() 호출
case R.id.btnGetlist:  // 이마트 대상받기
    downloadShipmentList("0", "이마트대상받기");
    break;
```

#### 대상받기 타입 (searchType)

| searchType | 버튼 | VIEW |
|:----------:|------|------|
| 0 | 이마트 대상받기 | VW_PDA_WID_LIST |
| 1 | 생산 대상받기 | VW_PDA_WID_LIST |
| 2 | 홈플러스 대상받기 | VW_PDA_WID_HOMEPLUS_LIST |
| 3 | 도매업체 대상받기 | VW_PDA_WID_WHOLESALE_LIST |
| 4 | 비정량 대상받기 | VW_PDA_WID_LIST_NONFIXED |
| 5 | 홈플러스 비정량 | VW_PDA_WID_LIST_NONFIXED_HP |
| 6 | 롯데 대상받기 | VW_PDA_WID_LIST_LOTTE |
| 7 | 생산(라벨) 대상받기 | VW_PDA_WID_LIST |

#### downloadShipmentList() 처리

```java
private void downloadShipmentList(String searchType, String logMessage) {
    // 1. searchType 설정
    Common.searchType = searchType;

    // 2. 기존 데이터 삭제
    DBHandler.deletequeryShipment(getApplicationContext());
    DBHandler.deletequeryBarcodeInfo(getApplicationContext());
    DBHandler.deletequeryGoodsWet(getApplicationContext());

    // 3. 서버 조회 시작 (비동기)
    new ProgressDlgShipSearch(this).execute();
}
```

---

### 2.2 [Step 2] ProgressDlgShipSearch - VIEW 조회

**파일**: `ProgressDlgShipSearch.java`

#### 서버 요청 흐름

```
┌────────────────────────────────────────────────────────────┐
│                    ProgressDlgShipSearch                    │
├────────────────────────────────────────────────────────────┤
│ 1. 조회 조건 생성                                           │
│    WHERE GI_REQ_DATE = '20260203'                          │
│    AND GR_WAREHOUSE_CODE = 'IN10273'  (창고 조건)           │
├────────────────────────────────────────────────────────────┤
│ 2. 서버 API 호출                                            │
│    URL: search_shipment.jsp                                │
│    VIEW: VW_PDA_WID_LIST                                   │
├────────────────────────────────────────────────────────────┤
│ 3. 응답 데이터 수신                                         │
│    형식: "컬럼1::컬럼2::컬럼3;;컬럼1::컬럼2::컬럼3;;"       │
├────────────────────────────────────────────────────────────┤
│ 4. 데이터 파싱 → Shipments_Info 객체                        │
├────────────────────────────────────────────────────────────┤
│ 5. PDA DB 동기화 (삭제/추가)                                │
│    TB_SHIPMENT 테이블에 저장                                │
├────────────────────────────────────────────────────────────┤
│ 6. 완료 → ProgressDlgBarcodeSearch 자동 실행               │
└────────────────────────────────────────────────────────────┘
```

#### 서버 응답 파싱 (temp[] 배열)

```java
// 기본 필드 (0~23)
si.setGI_D_ID(temp[0]);           // 출하상세ID (PK)
si.setITEM_CODE(temp[1]);         // 품목코드
si.setITEM_NAME(temp[2]);         // 품목명
si.setEMARTITEM_CODE(temp[3]);    // 이마트품목코드
si.setEMARTITEM(temp[4]);         // 이마트품목명
si.setGI_REQ_PKG(temp[5]);        // 요청수량
si.setGI_REQ_QTY(temp[6]);        // 요청중량
si.setGI_REQ_DATE(temp[7]);       // 요청일자
si.setBL_NO(temp[8]);             // BL번호
si.setBRAND_CODE(temp[9]);        // 브랜드코드
si.setCLIENT_CODE(temp[10]);      // 거래처코드
si.setCLIENTNAME(temp[11]);       // 거래처명
si.setCENTERNAME(temp[12]);       // 센터명
si.setITEM_SPEC(temp[13]);        // 품목규격
si.setCT_CODE(temp[14]);          // CT코드
si.setIMPORT_ID_NO(temp[15]);     // 수입식별번호
si.setPACKER_CODE(temp[16]);      // 패커코드
si.setPACKER_PRODUCT_CODE(temp[17]); // 패커상품코드
si.setBARCODE_TYPE(temp[18]);     // 바코드타입
si.setITEM_TYPE(temp[19]);        // 아이템타입
si.setPACKWEIGHT(temp[20]);       // 포장중량
si.setBARCODEGOODS(temp[21]);     // 바코드상품코드
si.setSTORE_IN_DATE(temp[22]);    // 입고일자
si.setEMARTLOGIS_CODE(temp[23]);  // 물류코드

// 이마트(0), 비정량(4) 추가 필드 (24~29)
si.setWH_AREA(temp[24]);          // 창고구역
si.setUSE_NAME(temp[25]);         // 용도명
si.setUSE_CODE(temp[26]);         // 용도코드
si.setCT_NAME(temp[27]);          // CT명
si.setSTORE_CODE(temp[28]);       // 점포코드
si.setEMART_PLANT_CODE(temp[29]); // 이마트공장코드
```

#### DB 동기화 로직

```
┌─────────────────────────────────────────────────────────┐
│                   DB 동기화 (Sync)                       │
├─────────────────────────────────────────────────────────┤
│ [삭제 대상 검색]                                         │
│   PDA에 있고, 서버에 없음 → 삭제 (출하 취소된 항목)       │
│                                                         │
│ [추가 대상 검색]                                         │
│   서버에 있고, PDA에 없음 → 추가 (신규 출하대상)          │
│                                                         │
│ [동기화 실행]                                            │
│   DBHandler.refreshShipmentList(삭제목록, 추가목록)      │
└─────────────────────────────────────────────────────────┘
```

---

### 2.3 [Step 3] ProgressDlgBarcodeSearch - 바코드 정보 조회

**파일**: `ProgressDlgBarcodeSearch.java`

```
┌────────────────────────────────────────────────────────────┐
│                  ProgressDlgBarcodeSearch                   │
├────────────────────────────────────────────────────────────┤
│ 1. 출하대상에서 PACKER_PRODUCT_CODE 목록 추출              │
├────────────────────────────────────────────────────────────┤
│ 2. 바코드 정보 조회                                        │
│    WHERE PACKER_PRODUCT_CODE IN ('코드1', '코드2', ...)   │
├────────────────────────────────────────────────────────────┤
│ 3. 응답 데이터 파싱 → Barcodes_Info 객체                   │
│    - WEIGHT_FROM, WEIGHT_TO (중량 추출 위치)               │
│    - MAKINGDATE_FROM, MAKINGDATE_TO (제조일 추출 위치)     │
│    - BARCODEGOODS_FROM, BARCODEGOODS_TO (상품코드 위치)    │
├────────────────────────────────────────────────────────────┤
│ 4. TB_BARCODE_INFO 테이블에 저장                           │
├────────────────────────────────────────────────────────────┤
│ 5. 완료 → 사용자 대기 상태                                 │
└────────────────────────────────────────────────────────────┘
```

---

### 2.4 [Step 4] MainActivity - 계근입력시작

**파일**: `MainActivity.java`

```java
// "계근입력시작" 버튼 클릭
case R.id.btnWet:  // 이마트 계근입력시작
    startWeighing("0", "출하 리스트를 받아주세요.", "리스트가 없습니다.");
    break;
```

#### startWeighing() 처리

```java
private void startWeighing(String searchType, ...) {
    // 1. searchType 검증 (대상받기 타입과 일치해야 함)
    if (!Common.searchType.equals(searchType)) {
        Toast.makeText(..., wrongTypeMessage, ...);
        return;
    }

    // 2. DB에서 출하대상 리스트 조회
    ArrayList<Shipments_Info> list = DBHandler.selectqueryAllShipment(...);

    // 3. 리스트 있으면 계근 화면으로 이동
    if (list.size() > 0) {
        Intent i = new Intent(this, BixolonShipmentActivity.class);
        startActivity(i);
    } else {
        Toast.makeText(..., emptyListMessage, ...);
    }
}
```

---

### 2.5 [Step 5] BixolonShipmentActivity - 계근 입력

**파일**: `BixolonShipmentActivity.java`

```
┌────────────────────────────────────────────────────────────┐
│                   BixolonShipmentActivity                   │
│                      (계근 입력 화면)                        │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ [출하대상 정보]                                       │  │
│  │ 상품명: 한우 등심                                     │  │
│  │ 지점명: 이마트 강남점                                 │  │
│  │ 요청수량: 10박스 / 완료: 5박스                        │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ [바코드 스캔]                                         │  │
│  │ ▢▢▢▢▢▢▢▢▢▢▢▢▢▢▢▢▢▢▢▢▢▢▢                                │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ [계근 처리 흐름]                                      │  │
│  │                                                      │  │
│  │  1. 바코드 스캔                                      │  │
│  │         ↓                                            │  │
│  │  2. ITEM_TYPE별 중량 추출                            │  │
│  │     - W/HW: 바코드에서 중량 추출                      │  │
│  │     - S: 저울에서 입력                               │  │
│  │     - J: PACKWEIGHT 사용                             │  │
│  │     - B: 저울/수동 입력                              │  │
│  │         ↓                                            │  │
│  │  3. BARCODE_TYPE별 바코드 생성                        │  │
│  │     - M0~M9, E0~E3, L0, P0                          │  │
│  │         ↓                                            │  │
│  │  4. 라벨 출력 (Bixolon 프린터)                        │  │
│  │         ↓                                            │  │
│  │  5. TB_GOODS_WET 테이블에 저장                        │  │
│  │         ↓                                            │  │
│  │  6. 서버 전송 (전송버튼 또는 자동)                    │  │
│  │                                                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

#### 바코드 스캔 → 중량 추출 → 라벨 출력 흐름

```
바코드 스캔
    │
    ▼
바코드 매칭 (BARCODEGOODS 비교)
    │
    ▼
출하대상 확인
    │
    ▼
┌───────────────────────────────────────────────────┐
│              ITEM_TYPE별 중량 추출                 │
├─────────────┬─────────────────────────────────────┤
│    W/HW     │ 바코드에서 추출                      │
│             │ barcode.substring(FROM, TO)         │
├─────────────┼─────────────────────────────────────┤
│      S      │ 저울에서 입력                        │
├─────────────┼─────────────────────────────────────┤
│      J      │ PACKWEIGHT 값 사용                   │
├─────────────┼─────────────────────────────────────┤
│      B      │ 저울/수동 입력                       │
└─────────────┴─────────────────────────────────────┘
    │
    ▼
┌───────────────────────────────────────────────────┐
│           BARCODE_TYPE별 바코드 생성               │
├─────────────┬─────────────────────────────────────┤
│   M0~M4     │ 상품코드(6)+중량+회사코드+수입식별   │
├─────────────┼─────────────────────────────────────┤
│   M8, M9    │ 특수 형식 (비정량, 우육)             │
├─────────────┼─────────────────────────────────────┤
│   E0~E3     │ 지점코드+수입식별                    │
├─────────────┼─────────────────────────────────────┤
│     L0      │ 롯데 전용 형식                       │
└─────────────┴─────────────────────────────────────┘
    │
    ▼
라벨 출력 (SLCS 명령어)
    │
    ▼
TB_GOODS_WET 저장
    │
    ▼
서버 전송 대기
```

---

### 2.6 [Step 6] 서버 전송

```
┌────────────────────────────────────────────────────────────┐
│                        서버 전송                            │
├────────────────────────────────────────────────────────────┤
│ [전송 데이터]                                               │
│ - GI_D_ID (출하상세ID)                                     │
│ - ITEM_CODE (품목코드)                                     │
│ - PACKER_PRODUCT_CODE (패커상품코드)                       │
│ - 계근 정보 (중량, 박스번호, 제조일 등)                     │
├────────────────────────────────────────────────────────────┤
│ [전송 방식]                                                 │
│ - 자동 전송: 계근 완료 시                                   │
│ - 수동 전송: 전송 버튼 클릭 시                              │
├────────────────────────────────────────────────────────────┤
│ [전송 완료 처리]                                            │
│ - SAVE_TYPE = "T" (Transmitted)                            │
│ - TB_GOODS_WET 업데이트                                    │
└────────────────────────────────────────────────────────────┘
```

---

## 3. 데이터 흐름도

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   서버 DB    │      │  PDA 로컬 DB │      │   프린터    │
│  (Oracle)   │      │  (SQLite)   │      │  (Bixolon)  │
└─────────────┘      └─────────────┘      └─────────────┘
       │                    │                    │
       │  VW_PDA_WID_LIST   │                    │
       │ ──────────────────▶│                    │
       │                    │                    │
       │                    │  TB_SHIPMENT       │
       │                    │  (출하대상 저장)    │
       │                    │                    │
       │  VW_BARCODE_INFO   │                    │
       │ ──────────────────▶│                    │
       │                    │                    │
       │                    │  TB_BARCODE_INFO   │
       │                    │  (바코드정보 저장)  │
       │                    │                    │
       │                    │        계근        │
       │                    │ ─────────────────▶ │
       │                    │                    │
       │                    │  TB_GOODS_WET      │ 라벨 출력
       │                    │  (계근내역 저장)    │ ─────────▶
       │                    │                    │
       │    계근결과 전송    │                    │
       │ ◀──────────────────│                    │
       │                    │                    │
```

---

## 4. 관련 테이블/클래스

### 4.1 서버 VIEW

| VIEW | 용도 |
|------|------|
| VW_PDA_WID_LIST | 이마트 출하대상 |
| VW_PDA_WID_HOMEPLUS_LIST | 홈플러스 출하대상 |
| VW_PDA_WID_WHOLESALE_LIST | 도매업체 출하대상 |
| VW_PDA_WID_LIST_NONFIXED | 비정량 출하대상 |
| VW_PDA_WID_LIST_NONFIXED_HP | 홈플러스 비정량 |
| VW_PDA_WID_LIST_LOTTE | 롯데 출하대상 |

### 4.2 PDA 로컬 테이블 (SQLite)

| 테이블 | 용도 |
|--------|------|
| TB_SHIPMENT | 출하대상 정보 |
| TB_BARCODE_INFO | 바코드 정보 |
| TB_GOODS_WET | 계근 내역 |

### 4.3 주요 클래스

| 클래스 | 역할 |
|--------|------|
| MainActivity | 메인 화면, 버튼 처리 |
| ProgressDlgShipSearch | 출하대상 조회 (비동기) |
| ProgressDlgBarcodeSearch | 바코드 정보 조회 (비동기) |
| BixolonShipmentActivity | 계근 입력, 라벨 출력 |
| Shipments_Info | 출하대상 DTO |
| Barcodes_Info | 바코드 정보 DTO |
| DBHandler | DB CRUD 처리 |

---

## 5. 결론

VW_PDA_WID_LIST 조회 이후 프로세스:

1. **대상받기** → VIEW 조회 → TB_SHIPMENT 저장
2. **바코드정보 조회** → TB_BARCODE_INFO 저장
3. **계근입력시작** → BixolonShipmentActivity 이동
4. **바코드 스캔** → 중량 추출 → 바코드 생성 → 라벨 출력
5. **계근 완료** → TB_GOODS_WET 저장 → 서버 전송

---

**최종 수정일**: 2026-02-03
