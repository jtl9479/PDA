# 비정량(searchType=4) 바코드 정보 조회 JSP 컬럼 사용/미사용 분석

**작성일**: 2026-04-13
**대상**: search_barcode_info_nonfixed.jsp (24개 컬럼)
**분석 범위**: JSP 출력 → Java 파싱 → 로컬DB INSERT → 실제 사용처 추적

---

## 1. 분석 대상 파일

| 파일 | 경로 | 역할 |
|------|------|------|
| JSP 원본 | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\...\search_barcode_info_nonfixed.jsp` | Oracle 원본 |
| JSP 현재 | `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp` | MSSQL 전환 대상 |
| Java 파싱 | `ProgressDlgBarcodeSearch.java:107-133` | temp[0]~temp[23] 파싱 |
| 로컬DB | `DBHandler.java` — createqueryBarcodeInfo(), insertqueryBarcodeInfo() | TB_BARCODE_INFO |
| 모델 | `Barcodes_Info.java` | setter/getter |
| 사용처 | `BixolonShipmentActivity.java` — find_work_info() (L1760), 바코드 스캔 처리 | 실제 비즈니스 로직 |

---

## 2. 원본 vs 현재 JSP 비교

| 항목 | 원본 (Oracle) | 현재 (MSSQL 전환 대상) |
|------|:------------:|:-------------------:|
| SELECT 쿼리 | 동일 | 동일 |
| 테이블 | B_ITEM | B_ITEM |
| 컬럼 수 | 24개 | 24개 |
| DB 접속 | Oracle (oracle.jdbc.driver) | MSSQL (getMSSQLConnection) |
| 인코딩 | euc-kr | UTF-8 |

**SELECT 쿼리는 완전히 동일** — DB 접속 방식만 다름

---

## 3. 24개 컬럼 사용/미사용 전체 분석

| Index | SELECT 별칭 | 실제 값 | temp[n] | setter (L행) | TB_BARCODE_INFO INSERT | BSA get 사용 (L행) | 용도 분류 |
|:-----:|:----------:|--------|:-------:|-------------|:----:|-------------------|---------|
| 0 | PACKER_CLIENT_CODE | `'이마트용'` (고정) | temp[0] | setPACKER_CLIENT_CODE (L107) | O | getPACKER_CLIENT_CODE() L1559 | **사용: 계근 서버전송 패킷** |
| 1 | PACKER_PRODUCT_CODE | `ITEM_CODE` | temp[1] | setPACKER_PRODUCT_CODE (L108) | O | getPACKER_PRODUCT_CODE() L1788,1790 | **사용: 바코드 매칭 키값, 화면 표시** |
| 2 | PACKER_PRD_NAME | `ITEM_NAME_KR` | temp[2] | setPACKER_PRD_NAME (L109) | O | get 미호출 | 로컬DB저장용 |
| 3 | ITEMCODE | `ITEM_CODE` | temp[3] | setITEM_CODE (L110) | O | get 미호출 | 로컬DB저장용 |
| 4 | ITEM_NAME_KR | `ITEM_NAME_KR` | temp[4] | setITEM_NAME_KR (L111) | O | getITEM_NAME_KR() L1787,1805 | **사용: 바코드 매칭 후 화면 상품명 표시** |
| 5 | BRAND_CODE | `'0000'` (고정) | temp[5] | setBRAND_CODE (L112) | O | get 미호출 | 로컬DB저장용 |
| 6 | BARCODEGOODS | `ITEM_CODE` | temp[6] | setBARCODEGOODS (L113) | O | getBARCODEGOODS() L1768,1834 | **사용: 바코드 매칭 핵심 비교값** |
| 7 | BASEUNIT | `'KG'` (고정) | temp[7] | setBASEUNIT (L114) | O | getBASEUNIT() L1336,1383,1445,1556 | **사용: LB↔KG 환산 분기, 계근단위** |
| 8 | ZEROPOINT | `SBI.ZEROPOINT` | temp[8] | setZEROPOINT (L115) | O | getZEROPOINT() L1327,1380,1442 | **사용: 중량 소수점 자릿수 환산** |
| 9 | PACKER_PRD_CODE_FROM | `SBI.PACKER_PRD_CODE_FROM` | temp[9] | setPACKER_PRD_CODE_FROM (L116) | O | get 미호출 (Log 출력만) | **미사용(get미호출)** |
| 10 | PACKER_PRD_CODE_TO | `SBI.PACKER_PRD_CODE_TO` | temp[10] | setPACKER_PRD_CODE_TO (L117) | O | get 미호출 (Log 출력만) | **미사용(get미호출)** |
| 11 | BARCODEGOODS_FROM | `SBI.BARCODEGOODS_FROM` | temp[11] | setBARCODEGOODS_FROM (L118) | O | getBARCODEGOODS_FROM() L1769,1835 | **사용: 바코드 상품코드 추출 시작위치** |
| 12 | BARCODEGOODS_TO | `SBI.BARCODEGOODS_TO` | temp[12] | setBARCODEGOODS_TO (L119) | O | getBARCODEGOODS_TO() L1770,1836 | **사용: 바코드 상품코드 추출 끝위치** |
| 13 | WEIGHT_FROM | `SBI.WEIGHT_FROM` | temp[13] | setWEIGHT_FROM (L120) | O | getWEIGHT_FROM() L1313,1324 | **사용: 바코드 중량 추출 시작위치** |
| 14 | WEIGHT_TO | `SBI.WEIGHT_TO` | temp[14] | setWEIGHT_TO (L121) | O | getWEIGHT_TO() L1314,1324 | **사용: 바코드 중량 추출 끝위치** |
| 15 | MAKINGDATE_FROM | `SBI.MAKINGDATE_FROM` | temp[15] | setMAKINGDATE_FROM (L122) | O | getMAKINGDATE_FROM() L680,1354,1405,1467 | **사용: 제조일 추출, 소비기한 검증** |
| 16 | MAKINGDATE_TO | `SBI.MAKINGDATE_TO` | temp[16] | setMAKINGDATE_TO (L123) | O | getMAKINGDATE_TO() L681,1354,1405,1467 | **사용: 제조일 추출 끝위치** |
| 17 | BOXSERIAL_FROM | `SBI.BOXSERIAL_FROM` | temp[17] | setBOXSERIAL_FROM (L124) | O | getBOXSERIAL_FROM() L1360,1411,1473 | **사용: 박스시리얼 추출 시작위치** |
| 18 | BOXSERIAL_TO | `SBI.BOXSERIAL_TO` | temp[18] | setBOXSERIAL_TO (L125) | O | getBOXSERIAL_TO() L1360,1411,1473 | **사용: 박스시리얼 추출 끝위치** |
| 19 | STATUS | `SBI.STATUS` | temp[19] | setSTATUS (L126) | O | get 미호출 | 로컬DB저장용 |
| 20 | REG_ID | `SBI.REG_ID` | temp[20] | setREG_ID (L127) | O | get 미호출 | 로컬DB저장용 |
| 21 | REG_DATE | `SBI.REG_DATE` | temp[21] | setREG_DATE (L128) | **X (INSERT 누락)** | get 미호출 | **미사용(파싱O·INSERT제외·get미호출)** |
| 22 | REG_TIME | `SBI.REG_TIME` | temp[22] | setREG_TIME (L129) | **X (INSERT 누락)** | get 미호출 | **미사용(파싱O·INSERT제외·get미호출)** |
| 23 | memo | `'0000'` (고정) | temp[23] | setMEMO (L130) | **X (INSERT 누락)** | get 미호출 | **미사용(파싱O·INSERT제외·get미호출)** |
| - | SHELF_LIFE | JSP 미전송 | 미파싱 (L131-133) | 미호출 | O (빈값 '') | getSHELF_LIFE() L1232 — 비정량에서는 항상 빈값 | **비정량 미사용(JSP미전송)** |

---

## 4. 용도별 분류 요약

### 4.1 실제 사용 (14개)

| Index | 컬럼 | 용도 |
|:-----:|------|------|
| 0 | PACKER_CLIENT_CODE | 계근 서버전송 패킷 포함 |
| 1 | PACKER_PRODUCT_CODE | 바코드 매칭 키값, 화면 상품코드 표시 |
| 4 | ITEM_NAME_KR | 바코드 매칭 후 화면 상품명 표시 |
| 6 | BARCODEGOODS | 바코드 매칭 핵심 비교값 |
| 7 | BASEUNIT | LB↔KG 환산 분기, 계근단위 |
| 8 | ZEROPOINT | 중량 소수점 자릿수 환산 |
| 11 | BARCODEGOODS_FROM | 바코드에서 상품코드 추출 시작위치 |
| 12 | BARCODEGOODS_TO | 바코드에서 상품코드 추출 끝위치 |
| 13 | WEIGHT_FROM | 바코드에서 중량 추출 시작위치 |
| 14 | WEIGHT_TO | 바코드에서 중량 추출 끝위치 |
| 15 | MAKINGDATE_FROM | 제조일 추출, 소비기한 검증 |
| 16 | MAKINGDATE_TO | 제조일 추출 끝위치 |
| 17 | BOXSERIAL_FROM | 박스시리얼 추출 시작위치 |
| 18 | BOXSERIAL_TO | 박스시리얼 추출 끝위치 |

### 4.2 로컬DB저장용 (5개) — 파싱+INSERT하나 get 미호출

| Index | 컬럼 | 비고 |
|:-----:|------|------|
| 2 | PACKER_PRD_NAME | 패커 상품명 (비정량은 ITEM_NAME_KR과 동일값) |
| 3 | ITEMCODE → ITEM_CODE | 품목코드 (arSM에서 별도 조회) |
| 5 | BRAND_CODE | 고정값 '0000' |
| 19 | STATUS | 상태값 |
| 20 | REG_ID | 등록자 |

### 4.3 미사용 — get 미호출 (2개) — 파싱+INSERT하나 로직에 관여 안함

| Index | 컬럼 | 비고 |
|:-----:|------|------|
| 9 | PACKER_PRD_CODE_FROM | Log 출력만, 비즈니스 로직 미사용 |
| 10 | PACKER_PRD_CODE_TO | Log 출력만, 비즈니스 로직 미사용 |

### 4.4 미사용 — INSERT 제외 (3개) — 파싱하나 DB 저장도 안됨

| Index | 컬럼 | 비고 |
|:-----:|------|------|
| 21 | REG_DATE | CREATE에 있으나 INSERT 누락 (원본도 동일) |
| 22 | REG_TIME | CREATE에 있으나 INSERT 누락 (원본도 동일) |
| 23 | memo | CREATE에 있으나 INSERT 누락 (원본도 동일), 고정값 '0000' |

### 4.5 비정량 미사용 (1개)

| Index | 컬럼 | 비고 |
|:-----:|------|------|
| - | SHELF_LIFE | JSP 미전송, Java 미파싱 (L131-133), 빈값 INSERT |

---

## 5. INSERT 누락 컬럼 상세

`DBHandler.insertqueryBarcodeInfo()` (L1066-1112) INSERT 구문에서 3개 컬럼 누락:

| TB_BARCODE_INFO 컬럼 | CREATE | INSERT | Java 파싱 | 영향 |
|---------------------|:------:|:------:|:---------:|------|
| REG_DATE | O (TEXT) | **X** | O (temp[21]) | NULL 저장, 기능 영향 없음 |
| REG_TIME | O (TEXT) | **X** | O (temp[22]) | NULL 저장, 기능 영향 없음 |
| MEMO | O (TEXT) | **X** | O (temp[23]) | NULL 저장, 기능 영향 없음 |

**참고**: 이 3개 컬럼은 **원본에서도 동일하게 INSERT 누락**이므로 수정 대상이 아님 (제1원칙 준수)

---

## 6. 인덱스 정합성 검증 결과

| # | 검증 항목 | 결과 |
|:-:|----------|:----:|
| 1 | JSP out.println 24개 순서 ↔ Java temp[0]~temp[23] | **일치** |
| 2 | 원본 JSP ↔ 현재 JSP SELECT 쿼리 | **동일** |
| 3 | searchType=4 SHELF_LIFE 미파싱 처리 | **정상** |
| 4 | CREATE TABLE ↔ INSERT 일치 | **주의** (REG_DATE/TIME/MEMO 3개 누락, 원본도 동일) |

---

## 7. 관련 문서

- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` — 이마트 search_barcode_info.jsp 분석 포함
- `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md` — 비정량 전체 흐름 (1-1단계)
- `app/doc/소스분석/17_ProgressDlgBarcodeSearch.md` — 파싱 클래스 분석
- `app/doc/소스분석/43_이마트(0)_비정량(4)_Java파싱_공유분리_분석.md`
