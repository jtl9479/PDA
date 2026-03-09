# PDA 프로젝트 → HL ERP 전환 공수 분석

## 1. 프로젝트 현황

### 1.1 시스템 구성

| 구분 | 기술 스택 | 설명 |
|------|----------|------|
| **서버 (Tomcat)** | JSP + JDBC + MSSQL | Apache Tomcat 8.5.29, 순수 JSP (프레임워크 없음) |
| **클라이언트 (Android)** | Java + SQLite | PDA 단말기 앱, 바코드 스캐너/라벨 프린터 연동 |
| **DB** | SQL Server (weberp_dev) | JDBC 직접 연결 (115.68.112.17:1433) |

### 1.2 코드 규모

| 구분 | 항목 | 수량 |
|------|------|------|
| **Tomcat (JSP)** | JSP 파일 | 26개 |
| | 코드 라인 | ~2,845줄 |
| | 직접 참조 테이블 | 7개 |
| | 사용 VIEW | 10개 |
| **Android (Java)** | Java 파일 | 31개 |
| | 코드 라인 | ~17,807줄 |
| | SQLite 테이블 | 4개 (77+ 컬럼) |
| | API 엔드포인트 | 20개 |
| | Activity | 8개 |
| **VIEW** | VIEW 수 | 10개 |
| | VIEW당 평균 컬럼 | 33~41개 |
| | 참조 원본 테이블 | 15+개 |
| **전체** | **총 코드 라인** | **~20,650줄** |

### 1.3 기능 구성

#### JSP 서버 기능 (26개)

| 분류 | 파일 | 수량 | 설명 |
|------|------|------|------|
| 입고(계근 저장) | insert_goods_wet*.jsp | 4 | 기본/홈플러스/신규/ON 채널별 계근 데이터 저장 |
| 바코드 조회 | search_barcode_info*.jsp | 4 | 정량/비정량/템포러리 바코드 정보 조회 |
| 출하 조회 | search_shipment*.jsp | 7 | 이마트/홈플러스/롯데/도매/ON 채널별 출하 목록 |
| 생산 조회 | search_production*.jsp | 4 | 생산투입/4라벨/무게계산/비정량 |
| 기타 조회 | search_goods_wet.jsp, search_homeplus_nonfixed*.jsp | 3 | 계근내역/홈플러스 비정량 |
| 업데이트 | update_shipment.jsp | 1 | 출하 상태 변경 (CHECK_YN) |
| 인증 | manager_login.jsp | 1 | 관리자 로그인 |
| 공통 | db_connection.jsp | 1 | DB 연결 공통함수 |
| 기타 | test.jsp, test.html | 2 | 테스트용 |

#### Android 앱 기능 (8개 Activity)

| Activity | 라인 | 기능 |
|----------|------|------|
| LoginActivity | 415 | 로그인, SQLite 초기화, 블루투스 프린터 설정 |
| MainActivity | 540 | 메인 메뉴 (8가지 작업 유형), 날짜/창고 선택, 서버 데이터 다운로드 |
| ShipmentActivity | 4,459 | 출하 계근 (바코드 스캔, 중량 입력, 라벨 인쇄, 서버 전송) |
| BixolonShipmentActivity | 3,392 | Bixolon 프린터용 출하 계근 |
| ProductionActivity | 706 | 생산 계근 계산기 (바코드/수기 중량 입력, 누적 계산) |
| SettingActivity | 201 | 프린터 ON/OFF 설정 |
| ExpiryEnterActivity | ~100 | 유통기한 입력 |
| DeviceListActivity | ~150 | 블루투스 프린터 기기 선택 |

#### 작업 유형 (searchType)

| searchType | 이름 | VIEW | 설명 |
|:----------:|------|------|------|
| 0 | 출하대상 (이마트) | VW_PDA_WID_LIST_ONO* | 이마트 정상 출하 |
| 1 | 생산대상 | VW_PDA_WID_PRO_LIST | 생산 투입 계근 |
| 2 | 홈플러스 | VW_PDA_WID_HOMEPLUS_LIST | 홈플러스 하이퍼 출하 |
| 3 | 도매업체 | VW_PDA_WID_WHOLESALE_LIST | 도매 거래처 출하 |
| 4 | 비정량 출하 | VW_PDA_WID_LIST_NONFIXED | 정량 규격 없는 상품 |
| 5 | 홈플러스 비정량 | VW_PDA_WID_LIST_NONFIXED_HP | 홈플러스 비정량 |
| 6 | 롯데 | VW_PDA_WID_LIST_LOTTE | 롯데마트 출하 |
| 7 | 생산(라벨) | VW_PDA_WID_PRO_4LABEL_LIST | 라벨 인쇄용 생산 |

### 1.4 로컬 SQLite 테이블 (Android)

| 테이블 | 컬럼 수 | 용도 |
|--------|---------|------|
| TB_SHIPMENT | 33 | 출하대상 (서버에서 다운로드) |
| TB_BARCODE_INFO | 26 | 바코드 파싱 규칙 |
| TB_GOODS_WET | 29 | 계근 내역 (서버 전송 대상) |
| TB_GOODS_WET_PRODUCTION_CALC | 1 | 생산 계산기 바코드 |

---

## 2. INNO → HL ERP 테이블 매핑

### 2.1 매핑 종합

| # | INNO 테이블 | HL ERP 대응 테이블 | Entity 클래스 | 매핑 | 난이도 |
|---|-----------|----------------|-------------|------|--------|
| 1 | W_GOODS_IH | **SM_출고머리** | DlivyHeadEntity | 1:1 완전 | 낮음 |
| 2 | W_GOODS_ID | **SM_출고상세** | DlivyDetailEntity | 1:1 완전 | 낮음 |
| 3 | W_GOODS_WET | **SM_출고LOT** | DlivyLotEntity | 구조 차이 | **높음** |
| 4 | W_GOODS_R | **PM_매입계산서머리/상세** | PuchasBillHeadEntity | 1:1 완전 | 중간 |
| 5 | I_BL_D | **TD_CREDIT발행상세** | CreditDetailEntity | 부분 호환 | **높음** |
| 6 | I_OFFER_D | **TD_OFFER상세** | OfferDetailEntity | 1:1 완전 | 중간 |
| 7 | B_ITEM | **CO_품목코드** | ItemCodeEntity | 1:1 완전 | 낮음 |
| 8 | B_CLIENT | **CO_거래처MASTER** | BcncMasterEntity | 1:1 완전 | 낮음 |
| 9 | B_SUPPLIER_ITEM | **CO_거래처품목매핑** | PuchasOfficItemCodeMappingEntity | 부분 호환 | 중간 |
| 10 | B_EMART_BARCODE | **CO_품목코드 (바코드 컬럼)** | ItemCodeEntity | 참조 | 중간 |
| 11 | B_COMMON_CODE | **CO_각종소분류코드** | CmmnCodeDetailEntity | 1:1 완전 | 낮음 |
| 12 | S_BARCODE_INFO | **LB_라벨양식상세** | LabelFormDetailEntity | 1:1 완전 | 중간 |
| 13 | W_EMART_ORDER_ITEM | **SM_마트사발주이마트** | MartCmnpyOrderEmartEntity | 1:1 완전 | 낮음 |
| 14 | W_E_ORDER_ITEM | **SM_마트사발주홈플러스** | MartCmnpyOrderHmplsEntity | 1:1 완전 | 낮음 |
| 15 | W_MART_ORDER_ITEM | **SM_마트사발주롯데** | MartCmnpyOrderLotteEntity | 1:1 완전 | 낮음 |
| 16 | B_COUNTRY | **CO_국가마스터** | — | 부분 호환 | 낮음 |
| 17 | CO_비밀번호 | **CO_비밀번호 (또는 GR_개인기본)** | — | 참조 | 낮음 |
| 18 | B_CLIENT_CHARGE | **CO_거래처MASTER (담당자 컬럼)** | BcncMasterEntity | 참조 | 낮음 |

**매핑 통계**: 1:1 완전 11개 (61%), 부분 호환 4개 (22%), 참조 3개 (17%)

### 2.2 핵심 매핑 난제

#### 난제 1: W_GOODS_WET → SM_출고LOT (계근 → LOT 체계)

| 항목 | INNO (W_GOODS_WET) | HL ERP (SM_출고LOT) |
|------|-------------------|-------------------|
| 개념 | 계근 1건 = 1레코드 | LOT 1건 = 1레코드 |
| 키 구조 | GI_D_ID (출고상세 FK) | 출고머리SEQ + 출고상세SEQ + 출고LOTSEQ |
| 중량 | WEIGHT (단일 필드) | 중량 + 평균중량 (이중 관리) |
| 추가 필드 | — | LOTNO, 소비기한, 확정여부, 확정일자 |
| 관계 | W_GOODS_ID:W_GOODS_WET = 1:N | SM_출고상세:SM_출고LOT = 1:N |

> LOT 채번 규칙과 확정 프로세스를 PDA에서 어떻게 처리할지 설계 필요

#### 난제 2: I_BL_D → TD_CREDIT발행상세 (BL → CREDIT 구조)

| 항목 | INNO (I_BL_D) | HL ERP (TD_CREDIT발행상세) |
|------|--------------|------------------------|
| 개념 | BL(선하증권) 기반 | 수입신용장(CREDIT) 기반 |
| 키 | BL_D_ID | 년도 + 구분 + 번호 + 행번호 |
| 패커 정보 | PACKER_CODE 직접 포함 | 별도 참조 |

> VIEW의 해외매입 파트(UNION ALL 파트1)에서 JOIN 구조 재설계 필요

#### 난제 3: DB 함수 미존재

INNO VIEW에서 사용하는 DB 함수가 HL ERP에 없을 수 있음:

| INNO 함수 | 용도 | HL ERP 대응 |
|----------|------|-----------|
| DE_ITEM(품목코드) | 품목명 조회 | CO_품목코드 JOIN으로 대체 |
| DE_CLIENT(거래처코드) | 거래처명 조회 | CO_거래처MASTER JOIN으로 대체 |
| DE_COMMON(대분류, 소분류) | 공통코드명 조회 | CO_각종소분류코드 JOIN으로 대체 |

> 함수를 JOIN으로 대체하거나, HL ERP DB에 동일 함수를 생성해야 함

### 2.3 모듈별 매핑 상세

#### SM (출고) 모듈

```
INNO                          HL ERP
W_GOODS_IH (출고 헤더)    →   SM_출고머리
  ├─ CLIENT_CODE              ├─ 출고거래처
  ├─ GI_REQ_DATE              ├─ 출고일자
  └─ GI_H_ID (PK)             └─ SEQ (PK, auto)

W_GOODS_ID (출고 상세)    →   SM_출고상세
  ├─ GI_D_ID (PK)             ├─ SEQ (PK, auto)
  ├─ ITEM_CODE                ├─ 출고품목코드
  ├─ GI_REQ_PKG               ├─ 출고박스수량
  ├─ GI_REQ_QTY               ├─ 출고중량
  └─ PACKING_QTY              └─ (계근 상태)

W_GOODS_WET (계근)       →   SM_출고LOT
  ├─ GI_D_ID (FK)             ├─ 출고상세SEQ (FK)
  ├─ WEIGHT                   ├─ 중량
  ├─ BOX_CNT                  ├─ 박스수량
  ├─ MAKINGDATE               ├─ (제조일 → 소비기한)
  └─ BARCODE                  └─ LOTNO
```

#### 마트사 발주

```
W_EMART_ORDER_ITEM        →   SM_마트사발주이마트
W_E_ORDER_ITEM             →   SM_마트사발주홈플러스
W_MART_ORDER_ITEM          →   SM_마트사발주롯데
```

#### CO (기준정보) 모듈

```
B_ITEM                     →   CO_품목코드
  ├─ ITEM_CODE                 ├─ 품목코드
  ├─ ITEM_NAME (via DE_ITEM)   ├─ 품목명
  ├─ MAJOR_CATEGORY            ├─ 대분류
  └─ CONTAINER_TYPE            └─ (용기타입)

B_CLIENT                   →   CO_거래처MASTER
  ├─ CLIENT_CODE               ├─ 거래처코드
  └─ CLIENT_NAME (via DE_CLIENT) └─ 상호

B_COMMON_CODE              →   CO_각종소분류코드
  ├─ CODE_TYPE                 ├─ 대분류코드
  ├─ CODE_VALUE                ├─ 소분류코드
  └─ CODE_NAME (via DE_COMMON) └─ 코드명
```

---

## 3. VIEW 전환 상세

### 3.1 전환 전략

VIEW 출력 컬럼 alias를 기존과 동일하게 유지하여, VIEW를 참조하는 JSP와 Android 앱의 수정을 최소화한다.

```
[전략: VIEW alias 유지]

INNO VIEW (기존)                    HL ERP VIEW (전환 후)
SELECT ID.GI_D_ID,          →     SELECT 출고상세.SEQ AS GI_D_ID,
       BI.ITEM_NAME,        →            품목.품목명 AS ITEM_NAME,
       IH.CLIENT_CODE       →            출고머리.출고거래처 AS CLIENT_CODE
FROM W_GOODS_ID ID           →     FROM SM_출고상세 출고상세
JOIN B_ITEM BI ...           →     JOIN CO_품목코드 품목 ...
JOIN W_GOODS_IH IH ...       →     JOIN SM_출고머리 출고머리 ...

→ JSP/앱 측 코드 변경 최소화
```

### 3.2 VIEW별 전환 공수

| VIEW | 컬럼 | JOIN 수 | 특수 사항 | M/D |
|------|:----:|:-------:|----------|:---:|
| VW_PDA_WID_LIST | 41 | 10+ | UNION ALL(해외/국내), 서브쿼리, DB함수 3개 | 5 |
| VW_PDA_WID_LIST_ONO | ~38 | 8+ | LIST 변형, ON 전용 조건 | 3 |
| VW_PDA_WID_LIST_ONO_TEMP | ~38 | 8+ | ONO 임시 조회 변형 | 2 |
| VW_PDA_WID_LIST_ONO_DIFF_PRD | ~38 | 8+ | ONO 다른상품 변형 | 2 |
| VW_PDA_WID_PRO_LIST | 33 | 6+ | 생산 전용, 고정값 다수 | 3 |
| VW_PDA_WID_PRO_4LABEL_LIST | ~33 | 6+ | PRO 라벨 변형 | 2 |
| VW_PDA_WID_HOMEPLUS_LIST | 34 | 8+ | 홈플러스 전용 발주 테이블 | 3 |
| VW_PDA_WID_WHOLESALE_LIST | 34 | 6+ | 도매 전용, EOI_ID IS NULL 조건 | 2 |
| VW_PDA_WID_LIST_NONFIXED | 38 | 8+ | 비정량 전용 조건 | 3 |
| VW_PDA_WID_LIST_NONFIXED_HP | 39 | 8+ | 홈플러스 비정량 | 3 |
| VW_PDA_WID_LIST_LOTTE | 35 | 8+ | 롯데 전용, LAST_BOX_ORDER 서브쿼리 | 3 |
| **DB 함수 재생성** | — | — | DE_ITEM(), DE_CLIENT(), DE_COMMON() | 2 |
| | | | **VIEW 소계** | **33** |

### 3.3 VIEW 참조 테이블 관계도

```
[VW_PDA_WID_LIST 기준 — HL ERP 전환 후]

SM_출고머리
    │
    ├─ INNER JOIN SM_출고상세
    │      │
    │      ├─ INNER JOIN PM_매입계산서상세 (입고)
    │      │      │
    │      │      ├─ INNER JOIN TD_CREDIT발행상세  [해외매입]
    │      │      │
    │      │      └─ INNER JOIN TD_OFFER상세       [국내매입]
    │      │
    │      ├─ INNER JOIN CO_품목코드
    │      │
    │      └─ INNER JOIN SM_마트사발주이마트 (서브쿼리)
    │             ├─ INNER JOIN CO_품목코드 (바코드)
    │             ├─ INNER JOIN CO_각종소분류코드 (센터명)
    │             └─ INNER JOIN CO_각종소분류코드 (창고구역)
    │
    └─ LEFT JOIN CO_거래처품목매핑 (가공장코드)
```

---

## 4. JSP 수정 상세

### 4.1 직접 테이블 접근 JSP (수정 필수)

| JSP | 현재 테이블 | 전환 후 테이블 | 작업 | M/D |
|-----|-----------|-------------|------|:---:|
| insert_goods_wet.jsp | INSERT W_GOODS_WET | INSERT SM_출고LOT | 컬럼 매핑 + LOT 채번 | 2 |
| insert_goods_wet_homeplus.jsp | INSERT W_GOODS_WET | INSERT SM_출고LOT | 홈플러스 채널 추가 필드 | 2 |
| insert_goods_wet_new.jsp | INSERT W_GOODS_WET (배치) | INSERT SM_출고LOT (배치) | 대량 INSERT 변환 | 2 |
| insert_goods_wet_ono.jsp | INSERT W_GOODS_WET + UPDATE W_GOODS_ID | INSERT SM_출고LOT + UPDATE SM_출고상세 | 2개 테이블 동시 변환 | 2 |
| search_barcode_info.jsp | SELECT S_BARCODE_INFO + B_ITEM + B_SUPPLIER_ITEM | LB_라벨양식상세 + CO_품목코드 + CO_거래처품목매핑 | 3-way JOIN 재작성 | 1.5 |
| search_barcode_info_nonfixed.jsp | SELECT B_ITEM | CO_품목코드 | 단순 컬럼 변경 | 1 |
| search_barcode_info_temp.jsp | SELECT S_BARCODE_INFO + B_ITEM | LB_라벨양식상세 + CO_품목코드 | JOIN 재작성 | 1 |
| search_barcode_info_temp_diff_prd.jsp | SELECT S_BARCODE_INFO + B_ITEM | 동일 | 변형 | 0.5 |
| search_goods_wet.jsp | SELECT W_GOODS_WET | SM_출고LOT | 컬럼 매핑 | 1 |
| search_production_calc.jsp | SELECT B_SUPPLIER_ITEM + S_BARCODE_INFO | CO_거래처품목매핑 + LB_라벨양식상세 | JOIN 재작성 | 1 |
| search_homeplus_nonfixed2.jsp | SELECT B_ITEM | CO_품목코드 | 단순 변경 | 0.5 |
| update_shipment.jsp | UPDATE W_GOODS_ID | UPDATE SM_출고상세 | 컬럼 매핑 | 1 |
| manager_login.jsp | SELECT CO_비밀번호 | CO_비밀번호 또는 GR_개인기본 | 인증 로직 검토 | 0.5 |
| db_connection.jsp | JDBC 설정 | JDBC 설정 (동일 DB) | 최소 변경 | 0.5 |
| | | | **직접 테이블 소계** | **16.5** |

### 4.2 VIEW 기반 JSP (최소 수정)

VIEW alias 유지 전략 적용 시, 대부분 WHERE 조건만 변경:

| JSP | 사용 VIEW | 변경 사항 | M/D |
|-----|----------|----------|:---:|
| search_shipment.jsp | VW_PDA_WID_LIST_ONO_DIFF_PRD | WHERE 창고코드 매핑 확인 | 0.3 |
| search_shipment_ono.jsp | VW_PDA_WID_LIST_ONO | 동일 | 0.3 |
| search_shipment_ono_temp.jsp | VW_PDA_WID_LIST_ONO_TEMP | 동일 | 0.2 |
| search_shipment_ono_temp_diff_prd.jsp | VW_PDA_WID_LIST_ONO_TEMP | 동일 | 0.2 |
| search_shipment_homeplus.jsp | VW_PDA_WID_HOMEPLUS_LIST | 창고코드 매핑 | 0.3 |
| search_shipment_lotte.jsp | VW_PDA_WID_LIST_LOTTE | 창고코드 매핑 | 0.3 |
| search_shipment_wholesale.jsp | VW_PDA_WID_WHOLESALE_LIST | 창고코드 매핑 | 0.3 |
| search_production.jsp | VW_PDA_WID_PRO_LIST | 창고코드 매핑 | 0.2 |
| search_production_4label.jsp | VW_PDA_WID_PRO_4LABEL_LIST | 동일 | 0.2 |
| search_production_nonfixed.jsp | VW_PDA_WID_LIST_NONFIXED | 동일 | 0.2 |
| search_homeplus_nonfixed.jsp | VW_PDA_WID_LIST_NONFIXED_HP | 동일 | 0.2 |
| | | **VIEW 기반 소계** | **2.7** |

---

## 5. Android 앱 수정 상세

### 5.1 수정 대상

| 파일 | 라인 | 수정 범위 | M/D |
|------|------|----------|:---:|
| DBHandler.java | 1,922 | SQLite 테이블 스키마 (컬럼 추가/변경), INSERT/SELECT 쿼리 | 3 |
| Shipments_Info.java | 354 | DTO 필드명 변경 (VIEW alias 유지 시 최소) | 0.5 |
| Barcodes_Info.java | 239 | DTO 필드명 변경 | 0.5 |
| Goodswets_Info.java | 205 | DTO 필드명 변경, LOT 관련 필드 추가 | 1 |
| ProgressDlgShipSearch.java | 426 | 서버 응답 파싱 (컬럼 순서/개수 변경 시) | 1 |
| ProgressDlgBarcodeSearch.java | 201 | 바코드 응답 파싱 | 0.5 |
| ShipmentActivity.java | 4,459 | INSERT 전송 필드 변경, LOT 관련 필드 추가 | 2 |
| BixolonShipmentActivity.java | 3,392 | ShipmentActivity와 동일 변경 | 1.5 |
| HttpHelper.java | — | 전송 파라미터 변경 (INSERT 관련) | 0.5 |
| Common.java | — | 상수 변경 (창고코드 매핑 등) | 0.5 |
| | | **Android 소계** | **11** |

### 5.2 수정 전략

VIEW alias 유지 시 Android 영향도:

| 영역 | VIEW alias 유지 | alias 변경 |
|------|:---------------:|:---------:|
| 서버 응답 파싱 | 변경 없음 | 전면 수정 |
| SQLite 스키마 | 최소 변경 | 전면 수정 |
| DTO 클래스 | 최소 변경 | 전면 수정 |
| INSERT 전송 | **수정 필요** (테이블 컬럼 변경) | 수정 필요 |
| 비즈니스 로직 | LOT 관련만 추가 | LOT 관련만 추가 |

---

## 6. 공수 산정 종합

### 6.1 작업별 공수 (중급 개발자 1명)

| # | 작업 | M/D | 상세 |
|---|------|:---:|------|
| 1 | 분석/매핑 설계 | 7 | INNO 18개 테이블 → HL ERP 컬럼 매핑 정의서 작성, HL ERP 구조 학습 |
| 2 | VIEW 10개 재작성 + DB함수 | 28 | 내부 JOIN/WHERE 전면 재작성, 출력 alias 유지, DB 함수 3개 재생성 |
| 3 | JSP 수정 (직접 테이블) | 15 | INSERT 4개, UPDATE 1개, SELECT 6개, LOGIN 1개, 공통 1개 |
| 4 | JSP 수정 (VIEW 기반) | 3 | WHERE 조건 변경 (창고코드 매핑 등) |
| 5 | Android 앱 수정 | 10 | DBHandler 스키마, INSERT 필드, DTO, 파싱 로직 |
| 6 | LOT 체계 연동 | 5 | W_GOODS_WET → SM_출고LOT 구조 차이 해소, LOT 채번/분배 로직 |
| 7 | 단위 테스트 | 10 | 8가지 searchType × CRUD 기능별 검증 |
| 8 | 통합 테스트 | 8 | 바코드 스캔 → 조회 → 계근 → 서버 전송 → 라벨 인쇄 E2E |
| 9 | 안정화/버그 수정 | 5 | 현장 테스트, 프린터 연동, 마트사별 검증 |
| | **총계** | **91** | |

### 6.2 공수 분포

```
[공수 분포 — 작업 유형별]

  VIEW 재작성      31% ████████████████
  JSP 수정         20% ██████████
  테스트/안정화      25% █████████████
  Android 수정     11% ██████
  분석/설계          8% ████
  LOT 체계 연동      5% ███
```

```
[공수 분포 — 개발 vs 비개발]

  개발 (VIEW+JSP+Android+LOT)  67% ██████████████████████████████████
  테스트/안정화                  25% █████████████
  분석/설계                      8% ████
```

### 6.3 시나리오별 일정

| 시나리오 | 생산성 | 보정 공수 | 기간 (1명) | 설명 |
|---------|:------:|:--------:|:---------:|------|
| **낙관적** | 100% | 91 M/D | **약 4개월** | HL ERP 숙지, 매핑 명확, 이슈 없음 |
| **현실적** | 80% | 114 M/D | **약 5개월** | HL ERP 학습 시간 포함, 중간 이슈 발생 |
| **보수적** | 70% | 130 M/D | **약 6개월** | LOT 구조 재설계, 현업 협의 필요 |

> **권장**: 고객/경영진에게는 **5~6개월**로 보고

---

## 7. 핵심 리스크

| 리스크 | 영향도 | 추가 공수 | 설명 |
|--------|:------:|:--------:|------|
| **LOT 체계 차이** | 높음 | +5~15 M/D | INNO 계근(W_GOODS_WET) → HL ERP LOT(SM_출고LOT) 구조 상이. LOT 채번 규칙, 확정 프로세스 추가 필요 |
| **DB 함수 미존재** | 중간 | +2~5 M/D | DE_ITEM(), DE_CLIENT(), DE_COMMON() 등 INNO 전용 함수를 HL ERP DB에 재생성 또는 JOIN으로 대체 |
| **BL → CREDIT 구조** | 중간 | +3~5 M/D | 해외매입 VIEW에서 I_BL_D → TD_CREDIT발행상세 매핑 시 JOIN 구조 재설계 |
| **바코드 타입 매핑** | 낮음 | +2~3 M/D | B_EMART_BARCODE → CO_품목코드 바코드 컬럼 매핑, BARCODE_TYPE 결정 로직 변경 |
| **창고코드 체계 차이** | 낮음 | +1~2 M/D | INNO 창고코드(IN10273 등) → HL ERP 창고코드 매핑 테이블 필요 |
| **라벨 인쇄 호환성** | 낮음 | +2~3 M/D | 바코드 타입(M0~P0, L0 등)별 라벨 양식 HL ERP 데이터 기반 재검증 |

---

## 8. 전환 단계별 일정 (1명, 5개월)

| 단계 | 기간 | 주요 산출물 |
|------|------|-----------|
| 1. 분석/매핑 설계 | 2주 | INNO→HL ERP 컬럼 매핑 정의서, 변경 영향도 분석서 |
| 2. VIEW 재작성 | 6주 | VIEW 10개 + DB 함수 3개, SQL 검증 |
| 3. JSP 수정 | 3주 | 직접 테이블 JSP 14개 + VIEW 기반 JSP 11개 |
| 4. Android 앱 수정 | 3주 | DBHandler, DTO, Activity, 파싱 로직 |
| 5. LOT 체계 연동 | 1주 | LOT 채번, 확정 프로세스 연동 |
| 6. 단위 테스트 | 2주 | searchType 8가지 × CRUD 기능별 검증 |
| 7. 통합 테스트 | 2주 | E2E (스캔→조회→계근→전송→인쇄) |
| 8. 안정화 | 1주 | 현장 테스트, 버그 수정 |

```
Week:  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20
       ├──┤                                                         분석/매핑 설계
          ├──────────────┤                                          VIEW 재작성
                   ├────────┤                                       JSP 수정
                         ├────────┤                                 Android 수정
                               ├──┤                                 LOT 연동
                                  ├────┤                            단위 테스트
                                     ├────┤                         통합 테스트
                                           ├──┤                    안정화
```

---

## 9. 결론

| 항목 | 수치 |
|------|------|
| **총 코드 규모** | ~20,650줄 (JSP 2,845 + Android 17,807) |
| **전환 대상** | VIEW 10개 + JSP 26개 + Android Java 31개 |
| **테이블 매핑** | INNO 18개 → HL ERP 13개 테이블 |
| **매핑 호환율** | 61% 완전, 22% 부분, 17% 참조 |
| **총 공수** | **91 M/D (중급 개발자 1명)** |
| **현실적 기간** | **약 5개월** |
| **최대 공수 영역** | VIEW 재작성 (28 M/D, 31%) |
| **핵심 난제** | LOT 체계 전환, DB 함수 재생성, BL→CREDIT 구조 |
| **전환 전략** | VIEW alias 유지 → JSP/앱 변경 최소화 |

---

> **작성일**: 2026년 2월 20일
> **분석 대상**: D:\PDA (Tomcat + PDA-INNO Android)
> **전환 대상**: D:\HL_ERP\workspace\SGIS_HL_WEBERP (HL ERP)
> **분석 방법**: 소스코드 정적 분석 + 테이블 매핑 + VIEW 구조 분석
