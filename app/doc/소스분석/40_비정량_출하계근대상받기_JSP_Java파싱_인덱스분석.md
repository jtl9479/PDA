# 비정량(searchType=4) 출하계근대상받기 - JSP out.println과 Java temp[] 파싱 인덱스 대조 분석

## 개요

비정량(searchType=4) 출하계근대상 조회 시 JSP가 출력하는 컬럼과 Java가 파싱하는 인덱스를 대조 분석한다.

- **JSP 파일 경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\search_production_nonfixed.jsp`
- **Java 파싱 파일 경로**: `app/src/main/java/com/rgbsolution/highland_emart/common/ProgressDlgShipSearch.java`
- **VIEW**: `VW_PDA_WID_LIST_NONFIXED`
- **타입**: JSP + Java 파싱 인덱스 대조 분석
- **작성일**: 2026-04-07

---

## 1. 역할

- 비정량(searchType=4) 출하계근대상 조회 시 서버(JSP)가 출력하는 데이터와 앱(Java)이 파싱하는 인덱스의 일치 여부를 분석
- 원본 JSP/Java와 현재 JSP/Java의 컬럼 구조 차이를 비교
- 인덱스 불일치로 인한 데이터 매핑 오류 가능성 식별

---

## 2. 주요 상수/필드

| 상수/필드 | 타입 | 값 | 용도 |
|----------|------|-----|------|
| Common.searchType | String | "4" | 비정량 출고 구분 |
| Common.URL_SEARCH_PRODUCTION_NONFIXED | String | BASE_URL + "/search_production_nonfixed.jsp" | 비정량 JSP URL |
| 구분자 | String | "::" | 컬럼 간 구분자 |
| 행 구분자 | String | ";;" | 행 간 구분자 |

---

## 3. 주요 메서드

| 메서드 | 위치(줄) | 반환 | 용도 |
|--------|:--------:|------|------|
| doInBackground() | ProgressDlgShipSearch.java:66줄 | Void | 출하대상 조회 및 파싱 메인 로직 |

---

## 4. 호출 관계

### 4.1 이 파일이 호출하는 대상

| 호출 대상 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| HttpHelper | sendDataDb() | 153줄 | 비정량 JSP 호출 |
| Shipments_Info | set*() | 211~272줄 | 파싱 결과 객체에 설정 |

### 4.2 이 파일을 호출하는 곳

| 호출 위치 | 메서드 | 위치(줄) | 용도 |
|----------|--------|:--------:|------|
| ShipSearchActivity 등 | execute() | - | 비정량 출하대상 조회 실행 |

---

## 5. 데이터 흐름

```
[PDA 앱] searchType="4" → HttpHelper.sendDataDb()
    ↓
[JSP] search_production_nonfixed.jsp
    ↓ SELECT FROM VW_PDA_WID_LIST_NONFIXED
    ↓ out.println (37개 컬럼, "::" 구분)
    ↓
[PDA 앱] receiveData 수신
    ↓ split(";;") → row 분리
    ↓ split("::", -1) → column 분리 → temp[]
    ↓
[파싱] temp[0]~temp[30] → Shipments_Info 객체
```

---

## 6. 핵심 코드 및 인덱스 대조

### 6.1 현재 JSP out.println 인덱스 (search_production_nonfixed.jsp)

JSP는 37개 컬럼을 출력한다 (search_production_nonfixed.jsp:88~97줄).

### 6.2 현재 Java temp[] 파싱 인덱스 (ProgressDlgShipSearch.java)

Java는 searchType "0"과 "4"를 동일한 파싱 로직으로 처리한다 (211~272줄).
이마트 JSP(search_shipment.jsp)는 31개 컬럼(index 0~30)을 출력하며, Java 파싱은 이 구조에 맞춰져 있다.

### 6.3 핵심 문제: 비정량 JSP는 37개 컬럼, Java는 31개 인덱스 기준 파싱

**비정량 JSP는 이마트 JSP와 다른 컬럼 구조를 가지고 있으나, Java 파싱은 이마트 기준(31개)으로 되어 있다.**

### 6.4 JSP out.println 인덱스 vs Java temp[] 파싱 인덱스 대조표

| JSP Index | JSP 컬럼명 | Java temp[] | Java 세터 | 매핑 일치 | 비고 |
|:---------:|-----------|:-----------:|----------|:---------:|------|
| 0 | GI_H_ID | temp[0] | setGI_D_ID() | **X** | JSP=GI_H_ID인데 Java는 GI_D_ID로 파싱 |
| 1 | GI_D_ID | temp[1] | setITEM_CODE() | **X** | JSP=GI_D_ID인데 Java는 ITEM_CODE로 파싱 |
| 2 | EOI_ID | temp[2] | setITEM_NAME() | **X** | JSP=EOI_ID인데 Java는 ITEM_NAME으로 파싱 |
| 3 | ITEM_CODE | temp[3] | setEMARTITEM_CODE() | **X** | 1칸 밀림 |
| 4 | ITEM_NAME | temp[4] | setEMARTITEM() | **X** | 1칸 밀림 |
| 5 | EMARTITEM_CODE | temp[5] | setGI_REQ_PKG() | **X** | 1칸 밀림 |
| 6 | EMARTITEM | temp[6] | setGI_REQ_QTY() | **X** | 1칸 밀림 (소수점 처리 대상) |
| 7 | GI_REQ_PKG | temp[7] | setGI_REQ_DATE() | **X** | |
| 8 | GI_REQ_QTY | temp[8] | setBL_NO() | **X** | |
| 9 | AMOUNT | temp[9] | setBRAND_CODE() | **X** | |
| 10 | GOODS_R_ID | temp[10] | setCLIENT_CODE() | **X** | |
| 11 | GR_REF_NO | temp[11] | setCLIENTNAME() | **X** | |
| 12 | GI_REQ_DATE | temp[12] | setCENTERNAME() | **X** | |
| 13 | BL_NO | temp[13] | setITEM_SPEC() | **X** | |
| 14 | BRAND_CODE | temp[14] | setCT_CODE() | **X** | |
| 15 | BRANDNAME | temp[15] | setIMPORT_ID_NO() | **X** | |
| 16 | CLIENT_CODE | temp[16] | setPACKER_CODE() | **X** | |
| 17 | CLIENTNAME | temp[17] | setPACKER_PRODUCT_CODE() | **X** | |
| 18 | CENTERNAME | temp[18] | setBARCODE_TYPE() | **X** | |
| 19 | ITEM_SPEC | temp[19] | setITEM_TYPE() | **X** | |
| 20 | CT_CODE | temp[20] | setPACKWEIGHT() | **X** | |
| 21 | IMPORT_ID_NO | temp[21] | setBARCODEGOODS() | **X** | |
| 22 | PACKER_CODE | temp[22] | setSTORE_IN_DATE() | **X** | |
| 23 | PACKERNAME | temp[23] | setEMARTLOGIS_CODE() | **X** | |
| 24 | PACKER_PRODUCT_CODE | temp[24] | setWH_AREA() | **X** | |
| 25 | BARCODE_TYPE | temp[25] | setUSE_NAME() | **X** | |
| 26 | ITEM_TYPE | temp[26] | setUSE_CODE() | **X** | |
| 27 | PACKWEIGHT | temp[27] | setCT_NAME() | **X** | |
| 28 | BARCODEGOODS | temp[28] | setSTORE_CODE() | **X** | |
| 29 | STORE_IN_DATE | temp[29] | setEMART_PLANT_CODE() | **X** | |
| 30 | EMARTLOGIS_CODE | temp[30] | setGI_L_ID() | **X** | |
| 31 | EMARTLOGIS_NAME | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 32 | WH_AREA | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 33 | USE_NAME | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 34 | USE_CODE | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 35 | CT_NAME | - | (파싱 없음) | - | Java에서 파싱하지 않음 |
| 36 | STORE_CODE | - | (파싱 없음) | - | Java에서 파싱하지 않음 |

### 6.5 이마트 JSP(search_shipment.jsp) out.println 인덱스 vs Java temp[] 파싱 (정상 매핑)

참고: 이마트 JSP는 Java 파싱과 1:1로 일치한다.

| JSP Index | JSP 컬럼명 | Java temp[] | Java 세터 | 매핑 일치 |
|:---------:|-----------|:-----------:|----------|:---------:|
| 0 | GI_D_ID | temp[0] | setGI_D_ID() | O |
| 1 | ITEM_CODE | temp[1] | setITEM_CODE() | O |
| 2 | ITEM_NAME | temp[2] | setITEM_NAME() | O |
| 3 | EMARTITEM_CODE | temp[3] | setEMARTITEM_CODE() | O |
| 4 | EMARTITEM | temp[4] | setEMARTITEM() | O |
| 5 | GI_REQ_PKG | temp[5] | setGI_REQ_PKG() | O |
| 6 | GI_REQ_QTY | temp[6] | setGI_REQ_QTY() | O |
| 7 | GI_REQ_DATE | temp[7] | setGI_REQ_DATE() | O |
| 8 | BL_NO | temp[8] | setBL_NO() | O |
| 9 | BRAND_CODE | temp[9] | setBRAND_CODE() | O |
| 10 | CLIENT_CODE | temp[10] | setCLIENT_CODE() | O |
| 11 | CLIENTNAME | temp[11] | setCLIENTNAME() | O |
| 12 | CENTERNAME | temp[12] | setCENTERNAME() | O |
| 13 | ITEM_SPEC | temp[13] | setITEM_SPEC() | O |
| 14 | CT_CODE | temp[14] | setCT_CODE() | O |
| 15 | IMPORT_ID_NO | temp[15] | setIMPORT_ID_NO() | O |
| 16 | PACKER_CODE | temp[16] | setPACKER_CODE() | O |
| 17 | PACKER_PRODUCT_CODE | temp[17] | setPACKER_PRODUCT_CODE() | O |
| 18 | BARCODE_TYPE | temp[18] | setBARCODE_TYPE() | O |
| 19 | ITEM_TYPE | temp[19] | setITEM_TYPE() | O |
| 20 | PACKWEIGHT | temp[20] | setPACKWEIGHT() | O |
| 21 | BARCODEGOODS | temp[21] | setBARCODEGOODS() | O |
| 22 | STORE_IN_DATE | temp[22] | setSTORE_IN_DATE() | O |
| 23 | EMARTLOGIS_CODE | temp[23] | setEMARTLOGIS_CODE() | O |
| 24 | WH_AREA | temp[24] | setWH_AREA() | O |
| 25 | USE_NAME | temp[25] | setUSE_NAME() | O |
| 26 | USE_CODE | temp[26] | setUSE_CODE() | O |
| 27 | CT_NAME | temp[27] | setCT_NAME() | O |
| 28 | STORE_CODE | temp[28] | setSTORE_CODE() | O |
| 29 | EMART_PLANT_CODE | temp[29] | setEMART_PLANT_CODE() | O |
| 30 | GI_L_ID | temp[30] | setGI_L_ID() | O |

---

## 7. 원본 비교

### 7.1 원본 JSP (search_production_nonfixed.jsp)

원본 JSP 경로: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_production_nonfixed.jsp`

원본 JSP도 현재 JSP와 동일하게 37개 컬럼을 출력한다 (GI_H_ID, GI_D_ID, EOI_ID, ... STORE_CODE).

| 항목 | 원본 JSP | 현재 JSP | 동일 |
|------|---------|---------|:----:|
| VIEW | VW_PDA_WID_LIST_NONFIXED | VW_PDA_WID_LIST_NONFIXED | O |
| 컬럼 수 | 37개 | 37개 | O |
| out.println 순서 | GI_H_ID부터 STORE_CODE까지 | GI_H_ID부터 STORE_CODE까지 | O |
| DB 연결 | Oracle | MSSQL | X |
| 인코딩 | euc-kr | UTF-8 | X |

### 7.2 원본 Java 파싱 (ProgressDlgShipSearch.java)

원본 Java 경로: `D:\PDA\PDA-INNO(원본)\app\src\main\java\com\rgbsolution\highland_emart\common\ProgressDlgShipSearch.java`

| 항목 | 원본 Java | 현재 Java | 동일 |
|------|----------|----------|:----:|
| temp[0] | setGI_H_ID() | setGI_D_ID() | **X** |
| temp[1] | setGI_D_ID() | setITEM_CODE() | **X** |
| temp[2] | setEOI_ID() | setITEM_NAME() | **X** |
| temp[3] | setITEM_CODE() | setEMARTITEM_CODE() | **X** |
| 파싱 범위 | temp[0]~temp[37] | temp[0]~temp[30] | **X** |
| searchType 4 분기 | temp[32]~temp[37] | temp[24]~temp[30] | **X** |
| split 방식 | split("::") | split("::", -1) | X (기능 동일) |

### 7.3 원본 Java 파싱 인덱스 (정상 매핑)

원본 Java는 37개 컬럼 모두 파싱한다 (원본 ProgressDlgShipSearch.java:157~223줄).

| 원본 temp[] | 원본 세터 | 원본 JSP 컬럼 | 매핑 |
|:-----------:|----------|-------------|:----:|
| temp[0] | setGI_H_ID() | GI_H_ID | O |
| temp[1] | setGI_D_ID() | GI_D_ID | O |
| temp[2] | setEOI_ID() | EOI_ID | O |
| temp[3] | setITEM_CODE() | ITEM_CODE | O |
| temp[4] | setITEM_NAME() | ITEM_NAME | O |
| temp[5] | setEMARTITEM_CODE() | EMARTITEM_CODE | O |
| temp[6] | setEMARTITEM() | EMARTITEM | O |
| temp[7] | setGI_REQ_PKG() | GI_REQ_PKG | O |
| temp[8] | setGI_REQ_QTY() | GI_REQ_QTY | O |
| temp[9] | setAMOUNT() | AMOUNT | O |
| temp[10] | setGOODS_R_ID() | GOODS_R_ID | O |
| temp[11] | setGR_REF_NO() | GR_REF_NO | O |
| temp[12] | setGI_REQ_DATE() | GI_REQ_DATE | O |
| temp[13] | setBL_NO() | BL_NO | O |
| temp[14] | setBRAND_CODE() | BRAND_CODE | O |
| temp[15] | setBRANDNAME() | BRANDNAME | O |
| temp[16] | setCLIENT_CODE() | CLIENT_CODE | O |
| temp[17] | setCLIENTNAME() | CLIENTNAME | O |
| temp[18] | setCENTERNAME() | CENTERNAME | O |
| temp[19] | setITEM_SPEC() | ITEM_SPEC | O |
| temp[20] | setCT_CODE() | CT_CODE | O |
| temp[21] | setIMPORT_ID_NO() | IMPORT_ID_NO | O |
| temp[22] | setPACKER_CODE() | PACKER_CODE | O |
| temp[23] | setPACKERNAME() | PACKERNAME | O |
| temp[24] | setPACKER_PRODUCT_CODE() | PACKER_PRODUCT_CODE | O |
| temp[25] | setBARCODE_TYPE() | BARCODE_TYPE | O |
| temp[26] | setITEM_TYPE() | ITEM_TYPE | O |
| temp[27] | setPACKWEIGHT() | PACKWEIGHT | O |
| temp[28] | setBARCODEGOODS() | BARCODEGOODS | O |
| temp[29] | setSTORE_IN_DATE() | STORE_IN_DATE | O |
| temp[30] | setEMARTLOGIS_CODE() | EMARTLOGIS_CODE | O |
| temp[31] | setEMARTLOGIS_NAME() | EMARTLOGIS_NAME | O |
| temp[32] | setWH_AREA() | WH_AREA | O |
| temp[33] | setUSE_NAME() | USE_NAME | O |
| temp[34] | setUSE_CODE() | USE_CODE | O |
| temp[35] | setCT_NAME() | CT_NAME | O |
| temp[36] | setSTORE_CODE() | STORE_CODE | O |
| temp[37] | setEMART_PLANT_CODE() | - | - (원본 JSP에 없지만 Java에서 파싱) |

---

## 8. 주의사항

### 8.1 핵심 인덱스 불일치 문제

- **현재 비정량 JSP는 37개 컬럼을 출력하지만, 현재 Java는 이마트 기준 31개 인덱스로 파싱한다**
- 비정량 JSP는 index 0에 `GI_H_ID`, index 1에 `GI_D_ID`, index 2에 `EOI_ID`를 출력
- 그러나 현재 Java는 temp[0]을 `GI_D_ID`로 파싱 (이마트 JSP 기준)
- **결과: 모든 컬럼이 밀려서 잘못된 데이터가 매핑됨**

### 8.2 근본 원인

현재 Java 파싱은 이마트 JSP(search_shipment.jsp) 기준으로 작성됨:
- 이마트 JSP: 31개 컬럼 (GI_D_ID부터 시작, GI_H_ID/EOI_ID/AMOUNT/GOODS_R_ID/GR_REF_NO/BRANDNAME/PACKERNAME/EMARTLOGIS_NAME 없음)
- 비정량 JSP: 37개 컬럼 (GI_H_ID부터 시작, 원본 VIEW 구조 그대로)

### 8.3 원본과의 차이 원인

원본에서는 모든 searchType이 **동일한 37개 컬럼 파싱 로직**을 공유했다 (temp[0]~temp[37]).
현재 프로젝트에서는 이마트 JSP를 31개 컬럼으로 재작성하면서 Java 파싱도 31개 기준으로 변경했으나,
**비정량 JSP는 원본 VIEW(37개) 구조를 그대로 유지하고 있어 불일치가 발생**한다.

### 8.4 비정량 JSP 37개 컬럼 사용/미사용 분류

| JSP Index | 컬럼명 | Java 파싱 | 로컬DB | 이마트 포함 | 판정 |
|:---------:|--------|:---------:|:------:|:----------:|------|
| 0 | GI_H_ID | X | X | X | **미사용** |
| 1 | GI_D_ID | O | O | O (이마트 0) | 사용 |
| 2 | EOI_ID | X | X | X | **미사용** |
| 3 | ITEM_CODE | O | O | O (이마트 1) | 사용 |
| 4 | ITEM_NAME | O | O | O (이마트 2) | 사용 |
| 5 | EMARTITEM_CODE | O | O | O (이마트 3) | 사용 |
| 6 | EMARTITEM | O | O | O (이마트 4) | 사용 |
| 7 | GI_REQ_PKG | O | O | O (이마트 5) | 사용 |
| 8 | GI_REQ_QTY | O | O | O (이마트 6) | 사용 |
| 9 | AMOUNT | X | X | X | **미사용** |
| 10 | GOODS_R_ID | X | X | X | **미사용** |
| 11 | GR_REF_NO | X | X | X | **미사용** |
| 12 | GI_REQ_DATE | O | O | O (이마트 7) | 사용 |
| 13 | BL_NO | O | O | O (이마트 8) | 사용 |
| 14 | BRAND_CODE | O | O | O (이마트 9) | 사용 |
| 15 | BRANDNAME | X | X | X | **미사용** |
| 16 | CLIENT_CODE | O | O | O (이마트 10) | 사용 |
| 17 | CLIENTNAME | O | O | O (이마트 11) | 사용 |
| 18 | CENTERNAME | O | O | O (이마트 12) | 사용 |
| 19 | ITEM_SPEC | O | O | O (이마트 13) | 사용 |
| 20 | CT_CODE | O | O | O (이마트 14) | 사용 |
| 21 | IMPORT_ID_NO | O | O | O (이마트 15) | 사용 |
| 22 | PACKER_CODE | O | O | O (이마트 16) | 사용 |
| 23 | PACKERNAME | X | X | X | **미사용** |
| 24 | PACKER_PRODUCT_CODE | O | O | O (이마트 17) | 사용 |
| 25 | BARCODE_TYPE | O | O | O (이마트 18) | 사용 |
| 26 | ITEM_TYPE | O | O | O (이마트 19) | 사용 |
| 27 | PACKWEIGHT | O | O | O (이마트 20) | 사용 |
| 28 | BARCODEGOODS | O | O | O (이마트 21) | 사용 |
| 29 | STORE_IN_DATE | O | O | O (이마트 22) | 사용 |
| 30 | EMARTLOGIS_CODE | O | O | O (이마트 23) | 사용 |
| 31 | EMARTLOGIS_NAME | X | X | X | **미사용** |
| 32 | WH_AREA | O | O | O (이마트 24) | 사용 |
| 33 | USE_NAME | O | O | O (이마트 25) | 사용 |
| 34 | USE_CODE | O | O | O (이마트 26) | 사용 |
| 35 | CT_NAME | O | O | O (이마트 27) | 사용 |
| 36 | STORE_CODE | O | O | O (이마트 28) | 사용 |
| - | EMART_PLANT_CODE | O | O | O (이마트 29) | **누락 (추가 필요)** |
| - | GI_L_ID | O | O | O (이마트 30) | **누락 (추가 필요)** |

**사용 컬럼 28개 / 미사용 컬럼 8개 / 누락 컬럼 2개**

미사용 8개 (삭제 대상): GI_H_ID, EOI_ID, AMOUNT, GOODS_R_ID, GR_REF_NO, BRANDNAME, PACKERNAME, EMARTLOGIS_NAME
누락 2개 (추가 대상): EMART_PLANT_CODE, GI_L_ID
전환 결과: 37 - 8 + 2 = **31개** (이마트 JSP와 동일)

### 8.5 비정량 고유 컬럼

비정량 사용 28개 컬럼은 **전부 이마트 31개에 포함**되어 있다. **비정량에서만 사용하는 고유 컬럼은 없다.**

이마트(31개) ⊃ 비정량 사용(28개) 관계이며, 차이는 이마트에만 있는 EMART_PLANT_CODE + GI_L_ID 2개뿐.

### 8.6 해결 방향 (2가지)

**방법 A: 비정량 JSP를 이마트 JSP와 동일한 31개 컬럼 구조로 수정**
- search_production_nonfixed.jsp의 out.println을 search_shipment.jsp와 동일한 31개 컬럼으로 변경
- Java 파싱 수정 불필요
- VIEW 대신 직접 JOIN 쿼리 사용 (이마트 JSP처럼)

**방법 B: Java 파싱에 비정량 전용 분기 추가**
- searchType "4"일 때 37개 컬럼 파싱 로직 추가
- JSP 수정 불필요

---

## 9. 관련 문서

- `app/doc/소스분석/36_이마트출하_원본비교분석.md`
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`
- `app/doc/오류/01_계근완료후_동일바코드_라벨재출력_버그.md`
- `app/doc/오류/02_WH_AREA_null체크_OR연산자_버그.md`
