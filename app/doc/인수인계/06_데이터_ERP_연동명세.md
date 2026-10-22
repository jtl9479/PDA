# 06. 데이터·ERP 연동명세

> 인수인계 문서 06/09 · 작성일 2026-10-22
> 기준: 앱 커밋 `4bcfc6a` (코드 기준 `93cab85`) · JSP 저장소 커밋 `79c3480`
> 인덱스는 **JSP `out.println` 출력 순서**(0부터)다. JSP 출력, Java 파싱(`temp[]`), 로컬 DB CREATE 문을 소스에서 직접 뽑아 대조했다(9장).

---

## 1. 통신 방식

| 항목 | 내용 |
|------|------|
| 호출 | `HTTP POST <BASE_URL>/<JSP명>.jsp` (`common/HttpHelper.java`), 인코딩 UTF-8 |
| 로그인 파라미터 | `id`(사번), `pwd`(SHA-1 16진수), `companyCode`(20) |
| 그 외 파라미터 | `data` — 조회는 **WHERE 조건 문자열**(예: `WHERE D.회사코드 = '20' AND D.출고일자 = '20261022' AND D.창고코드 = '…'`), 전송은 계근 패킷(7장)<br>`dbid` — 원본 시절 DB 구분값(`inno`/`highland`). 현재 JSP는 받기만 하고 쓰지 않는다(모든 JSP가 `common/db_connection.jsp` 하나로 접속) |
| 응답 (조회) | 행 구분 `;;`, 칸 구분 `::`. 앱은 `split("::", -1)`로 끝의 빈 칸까지 받는다 |
| 응답 (전송) | `s` 성공 / `f` 실패 |
| 응답 (로그인) | 성공 시 사번, 실패 시 `fail` |

> 앱이 WHERE 조건을 만들어 보내고 JSP가 쿼리 뒤에 그대로 붙이는 구조(원본과 같음)라, 조회 조건을 바꿀 때는 **앱(`ProgressDlg*`)과 JSP를 함께** 봐야 한다. JSP 안에서 앱이 보낸 조건의 컬럼명을 바꿔 끼우는 경우도 있다(예: 비정량 바코드정보 `SBI.ITEM_CODE` → `SBI.품목코드`).

---

## 2. JSP 목록 (20개 + 공통 1개)

| JSP | 용도 | searchType | 호출 위치 | ERP 테이블 | 읽기/쓰기 |
|------|------|:-:|------|------|:-:|
| `manager_login.jsp` | 로그인 | — | `LoginActivity` | `CO_비밀번호` | 읽기 |
| `search_warehouse.jsp` | 창고 목록 | — | `LoginActivity` | `CO_창고관리` | 읽기 |
| `search_shipment.jsp` | 출하대상 | 0 | `ProgressDlgShipSearch` | 3장 | 읽기 |
| `search_production_nonfixed.jsp` | 출하대상 | 4 | 〃 | 3장 | 읽기 |
| `search_shipment_homeplus.jsp` | 출하대상 | 2 | 〃 | 3장 | 읽기 |
| `search_homeplus_nonfixed.jsp` | 출하대상 | 5 | 〃 | 3장 | 읽기 |
| `search_shipment_wholesale.jsp` | 출하대상 | 3 | 〃 | 3장 | 읽기 |
| `search_shipment_lotte.jsp` | 출하대상 | 6 | 〃 | 3장 | 읽기 |
| `search_production.jsp` | 출하대상(생산) | 1 | 〃 | `PD_생산작업지시`·`PD_생산작업지시소요량`·`PD_생산작업일보`·`PD_생산계근` 등 | 읽기 · **미사용** |
| `search_barcode_info.jsp` | 바코드정보 | 0·1·2·3·6·7 | `ProgressDlgBarcodeSearch` | `CO_품목코드` | 읽기 |
| `search_barcode_info_nonfixed.jsp` | 바코드정보 | 4 | 〃 | `CO_품목코드` | 읽기 |
| `search_homeplus_nonfixed2.jsp` | 바코드정보 | 5 | 〃 | `CO_품목코드` | 읽기 |
| `search_goods_wet.jsp` | 기존 계근분 | 1·7 외 | `ProgressDlgGoodsWetSearch` | `SM_출고계근` | 읽기 |
| `search_goods_wet_production.jsp` | 기존 계근분(생산) | 1·7 | 〃 | `PD_생산계근` | 읽기 · 미사용 |
| `insert_goods_wet.jsp` | 계근 전송 (건별) | 0 | `ShipmentSendTask` | `SM_출고계근` | **쓰기** |
| `insert_goods_wet_homeplus.jsp` | 계근 전송 (건별) | 2 | 〃 | `SM_출고계근` (+박스순번) | **쓰기** |
| `insert_goods_wet_lotte.jsp` | 계근 전송 (건별) | 6 | 〃 | `SM_출고계근` (+박스순번) | **쓰기** |
| `insert_goods_wet_new.jsp` | 계근 전송 (일괄) | 3·4·5 | 〃 | `SM_출고계근` | **쓰기** |
| `insert_goods_wet_production.jsp` | 계근 전송(생산) | 1·7 | 〃 | `PD_생산계근` | 쓰기 · 미사용 |
| `search_production_calc.jsp` | 생산 중량 계산 | — | `ProductionActivity` | `CO_품목코드` | 읽기 · 보류 |
| `common/db_connection.jsp` | DB 접속 (모든 JSP가 include) | — | — | — | — |

- 앱 `Common.java`에는 있지만 서버에 JSP가 없는 주소: `search_production_4label.jsp`(생산 라벨, 삭제), `update_shipment.jsp`(호출 제거, 개발/33), `insert_barcode_info.jsp`(호출 없음).

---

## 3. 출하대상 응답 (`ProgressDlgShipSearch` → `TB_SHIPMENT`)

### 3.1 ERP 테이블 (조인 별칭)

| 별칭 | 테이블 | 이마트 0·4 | 홈플러스 2·5 | 도매 3 | 롯데 6 |
|------|------|:-:|:-:|:-:|:-:|
| H | `SM_출고머리` | O | O | O | O |
| D | `SM_출고상세` | O | O | O | O |
| L | `SM_출고LOT` | O | O | O | O |
| I | `CO_품목코드` | O | O | O | O |
| G / B / BC / G2 | `CO_거래처MASTER` (출고거래처·센터·최상위거래처) | G·B·BC·G2 | G·B·G2 | G | G·G2 |
| M1 / M2 | `CO_매출처품목코드매핑` (하위·최상위 거래처) | O | O | — | O |
| ME / HE / LE | `SM_마트사발주이마트` / `SM_마트사발주홈플러스` / `SM_마트사발주롯데마트` | ME | HE | — | LE |
| SD | `SM_수주상세` (납기일자) | 4만 | 5만 | — | O |
| C / C1 | `CO_각종소분류코드` (용도명·원산지명) | O | C1 | — | — |
| W | `SM_출고계근` (마지막 박스순번) | — | — | — | O |

공통 조건: `D.회사코드`, `D.출고일자 = 작업일자`, `H.거래명세발행여부 = 'N'`, 마트사구분·타입구분(05 문서 1장), 창고(0·2·3·6).

### 3.2 공통 인덱스 0~23

같은 값이면 "공통"으로 적었다. Java 필드와 로컬 컬럼 이름은 응답 컬럼명과 같다(`Shipments_Info.set<이름>`, `TB_SHIPMENT.<이름>`).

| Idx | 응답 컬럼 | 이마트 (0) | 이마트 비정량 (4) | 홈플러스 (2) | 홈플러스 비정량 (5) | 도매 (3) | 롯데 (6) |
|:-:|------|------|------|------|------|------|------|
| 0 | GI_D_ID | 공통 `D.SEQ` (출고상세 SEQ) | | | | | |
| 1 | ITEM_CODE | 공통 `I.품목코드` | | | | | |
| 2 | ITEM_NAME | `I.품목명` | `I.품목명` | `I.품목명` | `HE.상품명` | `I.품목명` | `I.품목명` |
| 3 | EMARTITEM_CODE | `ME.상품코드` | `ME.상품코드` | `HE.상품코드` | `HE.상품코드` | `'NA'` | `LE.상품코드` |
| 4 | EMARTITEM | `ME.상품명` | `ME.상품명` | `HE.상품명` | `HE.상품명` | `'NA'` | `LE.상품명` |
| 5 | GI_REQ_PKG | 공통 `L.박스수량` — 앱이 정수로 변환(`2.0` → `2`) | | | | | |
| 6 | GI_REQ_QTY | 공통 `L.중량` — 소수 4자리 이상이면 앱이 1자리로 버림 | | | | | |
| 7 | GI_REQ_DATE | 공통 `D.출고일자` | | | | | |
| 8 | BL_NO | 공통 `L.이력번호` | | | | | |
| 9 | BRAND_CODE | 공통 `''` | | | | | |
| 10 | CLIENT_CODE | 공통 `H.출고거래처` | | | | | |
| 11 | CLIENTNAME | CJ센터면 `G.약칭(점포코드)`, 아니면 `G.상호` | 이마트와 같음 | CJ면 `G.약칭(배송처코드)`, 아니면 `HE.배송처` | 홈플러스와 같음 | `G.상호` | `G.상호` |
| 12 | CENTERNAME | `BC.상호` | `BC.상호` | `B.상호` | `B.상호` | `G.상호` | `LE.센터명` |
| 13 | ITEM_SPEC | 공통 `I.규격` | | | | | |
| 14 | CT_CODE | `I.원산지` | `I.원산지` | 원산지 코드명(`C1.명칭`), 없으면 `I.원산지` | `I.원산지` | `I.원산지` | `I.원산지` |
| 15 | IMPORT_ID_NO | 공통 `L.이력번호` | | | | | |
| 16 | PACKER_CODE | `I.패커코드` | `'0000'` | `I.패커코드` | `I.패커코드` | `I.패커코드` | `I.패커코드` |
| 17 | PACKER_PRODUCT_CODE | `I.PPCODE` | `I.품목코드` | `I.PPCODE` | `I.품목코드` | `I.PPCODE` | `I.PPCODE` |
| 18 | BARCODE_TYPE | 매핑 바코드타입(M1→M2) | 〃 | 〃 | 〃 | `'NA'` | 〃 |
| 19 | ITEM_TYPE | 매핑 타입구분 | **`'HW'`** | **`'S'`** | 매핑 타입구분 | **`'S'`** | **`'S'`** |
| 20 | PACKWEIGHT | LOT 평균중량(0이면 `I.박스중량`) | 〃 | 〃 | `NULL` | `'NA'` | `NULL` |
| 21 | BARCODEGOODS | `I.상품바코드` | `I.품목코드` | `I.상품바코드` | `I.품목코드` | `I.상품바코드` | `I.상품바코드` |
| 22 | STORE_IN_DATE | `H.출고일자` | `SD.납기일자` | `H.출고일자` | `SD.납기일자` | `D.출고일자` | `SD.납기일자` |
| 23 | EMARTLOGIS_CODE | 매핑 물류코드, 비면 `'0000000'` | 〃 | `HE.납품처코드` | `''` | `'0000000'` | 매핑 물류코드 |

> - 19 ITEM_TYPE은 **조회 조건의 타입구분과 다르다.** 조회는 `타입구분 = 'W'`로 걸러도 응답은 마트별 고정값(`HW`·`S`)을 보낸다. 앱은 이 값으로 바코드 중량 추출 방식을 고른다(05 문서 2.6).
> - 22 STORE_IN_DATE는 마트마다 원천이 다르다(04 문서 M4).
> - "매핑"은 `CO_매출처품목코드매핑`을 하위 거래처(M1) → 최상위 거래처(M2) 순으로 찾은 값이다.

### 3.3 마트별 인덱스 24~

| Idx | 이마트·이마트 비정량 (0·4) | 홈플러스 (2) | 홈플러스 비정량 (5) | 도매 (3) | 롯데 (6) |
|:-:|------|------|------|------|------|
| 24 | WH_AREA `B.창고구역` | **GI_L_ID** `L.SEQ` | WH_AREA `''` | **GI_L_ID** `L.SEQ` | WH_AREA `''` |
| 25 | USE_NAME `C.명칭` | | USE_NAME `''` | | LAST_BOX_ORDER — 품목 기준 `SM_출고계근.박스순번`의 연속 구간 마지막 값, 없으면 0 |
| 26 | USE_CODE `I.제품용도` | | USE_CODE `''` | | **GI_L_ID** `L.SEQ` |
| 27 | CT_NAME `C1.명칭` | | CT_NAME `C1.명칭` | | |
| 28 | STORE_CODE `ME.점포코드` | | STORE_CODE `HE.납품처코드` | | |
| 29 | EMART_PLANT_CODE `''` | | **GI_L_ID** `L.SEQ` | | |
| 30 | **GI_L_ID** `L.SEQ` | | | | |
| 칸 수 | 31 | 25 | 30 | 25 | 27 |

- `TB_SHIPMENT`에는 응답 외에 `SHIPMENT_ID`(자동 증가), `SAVE_TYPE`(전송 여부)가 있다.

---

## 4. 바코드정보 응답 (`ProgressDlgBarcodeSearch` → `TB_BARCODE_INFO`)

모두 `CO_품목코드`(별칭 SBI)에서 읽는다. 공통 JSP는 25칸, 비정량 JSP 두 개는 24칸이다.

| Idx | 응답 컬럼 | `search_barcode_info.jsp` (0·2·3·6) | 비정량 4·5 (`…_nonfixed.jsp`, `search_homeplus_nonfixed2.jsp`) | 용도 |
|:-:|------|------|------|------|
| 0 | PACKER_CLIENT_CODE | `패커코드` | `'이마트용'` / `'홈플러스용'` | |
| 1 | PACKER_PRODUCT_CODE | `ppCode` | `품목코드` | 스캔 품목 매칭 키 |
| 2 | PACKER_PRD_NAME | `''` | `품목명` | |
| 3 | ITEM_CODE | `''` | `품목코드` | |
| 4 | ITEM_NAME_KR | `품목명` | `품목명` | 화면 품목명 |
| 5 | BRAND_CODE | `''` | `'0000'` | |
| 6 | BARCODEGOODS | `상품바코드` | `품목코드` | 상품코드 작업 매칭 |
| 7 | BASEUNIT | `기준단위` | `'KG'` | `LB`면 KG 환산 |
| 8 | ZEROPOINT | `소수점` | `소수점` | 중량 소수 자릿수 |
| 9·10 | PACKER_PRD_CODE_FROM/TO | `''` | `''` | |
| 11·12 | BARCODEGOODS_FROM/TO | `바코드상품코드시작/끝` | 같음 | 바코드 안 상품코드 위치 |
| 13·14 | WEIGHT_FROM/TO | `중량시작/끝` | 같음 | 바코드 안 중량 위치 |
| 15·16 | MAKINGDATE_FROM/TO | `제조일자시작/끝` | 같음 | 바코드 안 제조일자 위치 (소비기한 검사에도 사용) |
| 17·18 | BOXSERIAL_FROM/TO | `박스시리얼시작/끝` | 같음 | 바코드 안 박스시리얼 위치 |
| 19 | STATUS | `''` | `생산품상태` | |
| 20~22 | REG_ID / REG_DATE / REG_TIME | `''` | `''` | |
| 23 | MEMO | `''` | `'0000'` | |
| 24 | SHELF_LIFE | 적용단위가 1이면 적용값×365, 2면 ×30, 그 외 적용값 (일수) | **없음 — 4·5는 파싱 안 함** | 소비기한 일수 |

조회 조건: 앱이 출하대상의 패커상품코드 목록으로 `(SBI.PPCODE = … OR …) AND SBI.회사코드 = '20'`을 만들어 보낸다. 공통 JSP는 여기에 `ppCode != ''`(빈 행 제외)를 더 붙인다. 비정량 JSP는 `ITEM_CODE`를 `품목코드`로 바꿔 끼운다.

---

## 5. 기존 계근분 응답 (`ProgressDlgGoodsWetSearch` → `TB_GOODS_WET`, `SAVE_TYPE='Y'`)

`search_goods_wet.jsp` — `SM_출고계근`, 14칸.

| Idx | 응답 컬럼 | ERP 컬럼 | Idx | 응답 컬럼 | ERP 컬럼 |
|:-:|------|------|:-:|------|------|
| 0 | GI_D_ID | `출고상세SEQ` | 7 | REG_ID | `등록사원` |
| 1 | WEIGHT | `계근중량` | 8 | REG_DATE | `등록일자` |
| 2 | WEIGHT_UNIT | `계근중량단위` | 9 | REG_TIME | `등록시간` |
| 3 | PACKER_PRODUCT_CODE | `ppCode` | 10 | MAKINGDATE | `제조일자` |
| 4 | BARCODE | `계근바코드` | 11 | BOXSERIAL | `박스시리얼` |
| 5 | PACKER_CLIENT_CODE | `패커코드` | 12 | BOX_ORDER | `''` |
| 6 | BOX_CNT | `계근순번` | 13 | GI_L_ID | `출고LOTSEQ` |

---

## 6. 로그인·창고

| JSP | 응답 |
|------|------|
| `manager_login.jsp` | `CO_비밀번호`에서 회사코드 + 사번으로 비밀번호를 읽어 16진수로 바꾼 뒤 앱이 보낸 SHA-1 값과 비교 → 성공 시 사번, 실패 시 `fail` |
| `search_warehouse.jsp` | 0 `회사코드`, 1 `창고코드`, 2 `창고명` (`CO_창고관리`, `PDA여부 = '1'`). 앱은 1·2만 쓰고 0은 무시(회사코드는 항상 20) |

---

## 7. 계근 전송 패킷 (`ShipmentSendTask` → `SM_출고계근`)

칸 구분 `::`. 일괄 전송(`insert_goods_wet_new.jsp`)은 박스 여러 개를 `##`으로 이어 한 번에 보낸다.

| Idx | 앱 값 (`TB_GOODS_WET`) | `SM_출고계근` 컬럼 | 비고 |
|:-:|------|------|------|
| 0 | GI_D_ID | `출고상세SEQ` | |
| 1 | WEIGHT | `계근중량` | |
| 2 | WEIGHT_UNIT | `계근중량단위` | |
| 3 | PACKER_PRODUCT_CODE | `ppCode` | |
| 4 | BARCODE | `계근바코드` | 수기 입력이면 빈 값 |
| 5 | PACKER_CLIENT_CODE | `패커코드` | |
| 6 | MAKINGDATE | `제조일자` | |
| 7 | BOXSERIAL | `박스시리얼` | |
| 8 | BOX_CNT | `계근순번` | |
| 9 | REG_ID | `등록사원`, `수정사원` | |
| 10 | 회사코드 (`Common.selectCompanyCode`) | `회사코드` | |
| 11 | BRAND_CODE | — | 보내지만 JSP 미사용 |
| 12 | CLIENT_TYPE | — | 보내지만 JSP 미사용 |
| 13 | BOX_ORDER | `박스순번` | 홈플러스·롯데 JSP만 저장 |
| 14 | GI_L_ID | `출고LOTSEQ` | |
| 15 | BARCODE_TYPE | `바코드타입` | 15~25: 처음 출력한 라벨 값 (개발/80). 길이 초과 시 JSP가 자름, 없으면 `''` |
| 16 | LABEL_BARCODE1 | `라벨바코드1` | |
| 17 | LABEL_BARCODE2 | `라벨바코드2` | |
| 18 | PRINT_WEIGHT | `출력중량` | |
| 19 | EXPIRY_DATE | `소비기한` | |
| 20 | EXPIRY_TEXT | `소비기한문구` | |
| 21 | PRINT_IMPORT_ID_NO | `출력이력번호` | |
| 22 | PRINT_STORE_CODE | `점포코드` | |
| 23 | PRINT_ITEM_NAME | `출력상품명` | |
| 24 | PRINT_POINT_NAME | `출력지점명` | |
| 25 | PRINT_CENTER_NAME | `출력센터명` | |

- JSP가 채우는 값: `SEQ`(순번 `SM_DLIVY_WEIGH_SEQ`), `등록일자`·`등록시간`·`수정일자`·`수정시간`(서버 시각).
- 생산(1·7)은 `insert_goods_wet_production.jsp` → `PD_생산계근` (미사용).

---

## 8. PDA 로컬 DB (SQLite `HIGHLAND`, 버전 28)

| 테이블 | 용도 | 컬럼 |
|------|------|------|
| `TB_SHIPMENT` | 출하대상 | SHIPMENT_ID(PK), GI_D_ID, GI_L_ID, ITEM_CODE, ITEM_NAME, EMARTITEM_CODE, EMARTITEM, GI_REQ_PKG, GI_REQ_QTY, GI_REQ_DATE, BL_NO, BRAND_CODE, CLIENT_CODE, CLIENTNAME, CENTERNAME, ITEM_SPEC, CT_CODE, IMPORT_ID_NO, PACKER_CODE, PACKER_PRODUCT_CODE, BARCODE_TYPE, ITEM_TYPE, PACKWEIGHT, BARCODEGOODS, STORE_IN_DATE, EMARTLOGIS_CODE, SAVE_TYPE, WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE, LAST_BOX_ORDER (34) |
| `TB_BARCODE_INFO` | 바코드 규칙 | BARCODE_INFO_ID(PK), PACKER_CLIENT_CODE, PACKER_PRODUCT_CODE, PACKER_PRD_NAME, ITEM_CODE, ITEM_NAME_KR, BRAND_CODE, BARCODEGOODS, BASEUNIT, ZEROPOINT, PACKER_PRD_CODE_FROM, PACKER_PRD_CODE_TO, BARCODEGOODS_FROM, BARCODEGOODS_TO, WEIGHT_FROM, WEIGHT_TO, MAKINGDATE_FROM, MAKINGDATE_TO, BOXSERIAL_FROM, BOXSERIAL_TO, STATUS, REG_ID, REG_DATE, REG_TIME, MEMO, SHELF_LIFE (26) |
| `TB_GOODS_WET` | 계근 내역 | GOODS_WET_ID(PK), GI_D_ID, GI_L_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE, BARCODE, PACKER_CLIENT_CODE, MAKINGDATE, BOXSERIAL, BOX_CNT, EMARTITEM_CODE, EMARTITEM, ITEM_CODE, BRAND_CODE, REG_ID, REG_DATE, REG_TIME, **SAVE_TYPE**('F' 미전송 / 'Y' 전송·서버분), MEMO, DUPLICATE, CLIENT_TYPE, BOX_ORDER, 라벨 출력정보 11개(BARCODE_TYPE, LABEL_BARCODE1, LABEL_BARCODE2, PRINT_WEIGHT, EXPIRY_DATE, EXPIRY_TEXT, PRINT_IMPORT_ID_NO, PRINT_STORE_CODE, PRINT_ITEM_NAME, PRINT_POINT_NAME, PRINT_CENTER_NAME) (34) |
| `TB_GOODS_WET_PRODUCTION_CALC` | 생산 중량 계산 | BARCODE 등 (보류 기능) |

- 테이블은 로그인 때 없으면 만든다(`DBHandler.createquery*`). 라벨 출력정보 11개는 예전에 만든 기기에서도 로그인 때 자동으로 추가된다.
- `DBHelper.onUpgrade`의 테이블 삭제 코드는 주석 처리돼 있다. 컬럼 구조를 크게 바꾸면 **앱을 지우고 다시 설치**하는 것을 전제로 한다(미전송 계근 주의).
- 기본 키 외 고유 조건은 없다. 출하대상 행은 `GI_D_ID + GI_L_ID`로 구분한다(MSSQL은 출고상세 1건에 LOT가 여럿).

---

## 9. 작성 시 검증 기록 (2026-10-22)

| 검증 | 방법 | 결과 |
|------|------|------|
| JSP 출력 순서 | 출하대상 6개는 `rs.getString("…") + "::" // N` 주석, 바코드·계근분은 SELECT 순서(`getColumnName(n)`)로 추출 | 3·4·5장 반영 |
| Java 파싱 | `ProgressDlgShipSearch`·`ProgressDlgBarcodeSearch`·`ProgressDlgGoodsWetSearch`의 `set…(temp[N])` 추출 후 대조 | 인덱스 일치. 바코드 24번은 4·5에서 파싱 제외(`temp.length` 대신 searchType으로 분기) |
| 미파싱 칸 | 창고 응답 0번(회사코드), 전송 패킷 11·12번은 받는 쪽에서 쓰지 않음 | 표에 표시 |
| 로컬 DB | `DBHandler.createquery*` + `DBInfo` 상수 + 컬럼 자동 추가 코드 확인 | 8장 반영 |

> 원본(Oracle VIEW) 기준의 컬럼 문서는 `app/doc/column/`, `app/doc/view/`에 있다. VIEW를 직접 조인으로 바꾼 뒤의 실제 구조는 이 문서가 기준이다.

---

## 관련 문서
- `app/doc/인수인계/01_시스템_개요.md`
- `app/doc/인수인계/04_원본대비_변경점_및_미결사항.md`
- `app/doc/인수인계/05_마트별_업무흐름.md`
- `app/doc/column/PDA컬럼정의_SGI.xlsx`
- `app/doc/column/08_COMMON_COLUMN_USAGE_BY_SEARCHTYPE.md`
- `app/doc/column/09_VIEW_TABLE_USAGE.md`
- `app/doc/view/03_VIEW_로컬DB_컬럼비교.md`
- `app/doc/소스분석/35_로컬DB_구조.md`
- `app/doc/소스분석/49_출하대상받기_4유형_조회조건_종합정리.md`
- `app/doc/개발/14_JSP_호출_파라미터_가이드.md`
- `app/doc/개발/31_GI_L_ID_추가[GI_D_ID_비유니크_전체_영향범위].md`
- `app/doc/개발/80_출고계근_라벨출력정보_11컬럼_저장.md`
