# VIEW 컬럼 비교 분석 (doc/column 문서 기반)

## VIEW 목록

| VIEW명 | 용도 | searchType | 스키마 | 총 컬럼 | 사용 | 미사용 |
|--------|------|------------|--------|---------|------|--------|
| VW_PDA_WID_LIST | 이마트 계근 | 0 | HIGHLAND | 41 | 37 | 4 |
| VW_PDA_WID_PRO_LIST | 생산투입 계근 | 1 | INNO | 33 | 28 | 5 |
| VW_PDA_WID_HOMEPLUS_LIST | 홈플러스 계근 | 2 | HIGHLAND | 34 | 28 | 6 |
| VW_PDA_WID_WHOLESALE_LIST | 도매 계근 | 3 | HIGHLAND | 34 | 28 | 6 |
| VW_PDA_WID_LIST_NONFIXED | 이마트 비정량 | 4 | INNO | 38 | 33 | 5 |
| VW_PDA_WID_LIST_NONFIXED_HP | 홈플러스 비정량 | 5 | INNO | 39 | 28 | 11 |
| VW_PDA_WID_LIST_LOTTE | 롯데마트 계근 | 6 | INNO | 35 | 30 | 5 |

---

## 1. 공통 미사용 컬럼 (모든 VIEW에서 미사용)

| 컬럼명 | 설명 | 비고 |
|--------|------|------|
| **AMOUNT** | 출하상품금액 | 7개 VIEW 전체 미사용 |
| **GR_REF_NO** | 창고입고번호 | 7개 VIEW 전체 미사용 |
| **BRANDNAME** | 브랜드명 | 7개 VIEW 전체 미사용 |
| **PACKERNAME** | 패커이름 | 7개 VIEW 전체 미사용 |

---

## 2. 일부 VIEW에서 미사용 컬럼

| 컬럼명 | LIST | PRO | HP | WHOLE | NONFIX | NONFIX_HP | LOTTE |
|--------|:----:|:---:|:--:|:-----:|:------:|:---------:|:-----:|
| AMOUNT | X | X | X | X | X | X | X |
| GR_REF_NO | X | X | X | X | X | X | X |
| BRANDNAME | X | X | X | X | X | X | X |
| PACKERNAME | X | X | X | X | X | X | X |
| EMARTLOGIS_NAME | - | X | X | X | X | X | X |
| GR_WAREHOUSE_CODE | O | O | X | O | O | O | O |
| CLIENT_CODE | X | O | O | O | O | O | O |
| CONTAINER_TYPE | X | - | - | - | - | - | - |
| GOODS_R_ID | O | O | O | X | O | O | O |
| PACKWEIGHT | O | O | O | O | O | X | O |
| WH_AREA | O | - | - | O | O | X | O |
| STORE_NAME | - | - | - | - | - | X | - |
| EMARTLOGIS_CODE | O | O | O | O | O | X | O |

**범례**: O=사용, X=미사용, -=해당 컬럼 없음

---

## 3. VIEW별 미사용 컬럼 상세

### VW_PDA_WID_LIST (이마트) - 4개 미사용
| 컬럼명 | 판정 |
|--------|------|
| AMOUNT | 미사용 |
| GR_REF_NO | 미사용 |
| BRANDNAME | 미사용 |
| CONTAINER_TYPE | 미사용 |

### VW_PDA_WID_PRO_LIST (생산) - 5개 미사용
| 컬럼명 | 판정 |
|--------|------|
| AMOUNT | 미사용 |
| GR_REF_NO | 미사용 |
| BRANDNAME | 미사용 |
| PACKERNAME | 미사용 |
| EMARTLOGIS_NAME | 미사용 |

### VW_PDA_WID_HOMEPLUS_LIST (홈플러스) - 6개 미사용
| 컬럼명 | 판정 |
|--------|------|
| AMOUNT | 미사용 |
| GR_REF_NO | 미사용 |
| BRANDNAME | 미사용 |
| PACKERNAME | 미사용 |
| GR_WAREHOUSE_CODE | 미사용 |
| EMARTLOGIS_NAME | 미사용 |

### VW_PDA_WID_WHOLESALE_LIST (도매) - 6개 미사용
| 컬럼명 | 판정 |
|--------|------|
| AMOUNT | 미사용 |
| GOODS_R_ID | 미사용 |
| GR_REF_NO | 미사용 |
| BRANDNAME | 미사용 |
| PACKERNAME | 미사용 |
| EMARTLOGIS_NAME | 미사용 |

### VW_PDA_WID_LIST_NONFIXED (이마트 비정량) - 5개 미사용
| 컬럼명 | 판정 |
|--------|------|
| AMOUNT | 미사용 |
| GR_REF_NO | 미사용 |
| BRANDNAME | 미사용 |
| PACKERNAME | 미사용 |
| EMARTLOGIS_NAME | 미사용 |

### VW_PDA_WID_LIST_NONFIXED_HP (홈플러스 비정량) - 11개 미사용
| 컬럼명 | 판정 |
|--------|------|
| AMOUNT | 미사용 |
| GR_REF_NO | 미사용 |
| BRANDNAME | 미사용 |
| PACKERNAME | 미사용 |
| PACKWEIGHT | 미사용 |
| EMARTLOGIS_CODE | 미사용 |
| EMARTLOGIS_NAME | 미사용 |
| WH_AREA | 미사용 |
| STORE_NAME | 미사용 |

### VW_PDA_WID_LIST_LOTTE (롯데) - 5개 미사용
| 컬럼명 | 판정 |
|--------|------|
| AMOUNT | 미사용 |
| GR_REF_NO | 미사용 |
| BRANDNAME | 미사용 |
| PACKERNAME | 미사용 |
| EMARTLOGIS_NAME | 미사용 |

---

## 4. 특정 VIEW 전용 컬럼

### VW_PDA_WID_LIST (이마트) 전용
| 컬럼명 | 사용여부 | 비고 |
|--------|----------|------|
| EMART_PLANT_CODE | **사용** | 이마트 공장코드 |
| MAJOR_CATEGORY | **사용** | 주요 카테고리 |
| CONTAINER_TYPE | 미사용 | 용기 타입 |

### VW_PDA_WID_LIST_LOTTE (롯데) 전용
| 컬럼명 | 사용여부 | 비고 |
|--------|----------|------|
| LAST_BOX_ORDER | **사용** | 마지막 박스 순번 |

### VW_PDA_WID_HOMEPLUS_LIST (홈플러스) 전용
| 컬럼명 | 사용여부 | 비고 |
|--------|----------|------|
| HOMPLUS_STORE_CODE | **사용** | 홈플러스 점포코드 |

### VW_PDA_WID_LIST_NONFIXED_HP (홈플러스 비정량) 전용
| 컬럼명 | 사용여부 | 비고 |
|--------|----------|------|
| STORE_NAME | 미사용 | 점포명 |

---

## 5. 사용 여부 불일치 컬럼 (VIEW마다 다름)

| 컬럼명 | 사용 VIEW | 미사용 VIEW | 검토 필요 |
|--------|-----------|-------------|-----------|
| GR_WAREHOUSE_CODE | LIST, PRO, WHOLE, NONFIX, NONFIX_HP, LOTTE | HP | HP에서 제거 검토 |
| CLIENT_CODE | PRO, HP, WHOLE, NONFIX, NONFIX_HP, LOTTE | LIST | LIST에서 미사용 이유 확인 |
| GOODS_R_ID | LIST, PRO, HP, NONFIX, NONFIX_HP, LOTTE | WHOLE | WHOLE에서 미사용 이유 확인 |
| EMARTLOGIS_NAME | - | 전체 | 전체 제거 검토 |
| PACKWEIGHT | LIST, PRO, HP, WHOLE, NONFIX, LOTTE | NONFIX_HP | NONFIX_HP에서 미사용 이유 확인 |

---

## 6. 제거 권장 컬럼 (전체 VIEW 공통)

다음 컬럼들은 **모든 VIEW에서 미사용**으로 확인되어 제거 검토 대상입니다:

| 컬럼명 | 제거 영향도 | 권장 |
|--------|-------------|------|
| **AMOUNT** | 낮음 | 제거 권장 |
| **GR_REF_NO** | 낮음 | 제거 권장 |
| **BRANDNAME** | 낮음 | 제거 권장 |
| **PACKERNAME** | 낮음 | 제거 권장 |
| **EMARTLOGIS_NAME** | 낮음 (6/7 VIEW) | 제거 권장 |

### 제거 시 수정 위치
1. **VIEW DDL**: 각 VIEW에서 SELECT 컬럼 제거
2. **JSP**: search_*.jsp 파일에서 SELECT/출력 제거
3. **Android**:
   - Shipments_Info.java: 필드, getter/setter 제거
   - DBInfo.java: 상수 제거
   - DBHandler.java: INSERT/SELECT 문 수정
   - ProgressDlgShipSearch.java: 파싱 인덱스 조정

---

## 7. 공통 사용 컬럼 (모든 VIEW에서 사용)

| 컬럼명 | 용도 |
|--------|------|
| GI_H_ID | 출고 Header ID (JOIN 조건) |
| GI_D_ID | 출고 상세 ID (핵심 키) |
| EOI_ID | 발주번호 |
| ITEM_CODE | 상품코드 |
| ITEM_NAME | 상품명 |
| EMARTITEM_CODE | 마트 상품코드 |
| EMARTITEM | 마트 상품명 |
| GI_REQ_PKG | 요청수량 |
| GI_REQ_QTY | 요청중량 |
| GI_REQ_DATE | 요청일자 |
| BL_NO | BL번호 |
| BRAND_CODE | 브랜드코드 |
| CLIENTNAME | 출고업체명 |
| CENTERNAME | 센터명 |
| ITEM_SPEC | 스펙 |
| CT_CODE | 원산지 |
| PACKER_CODE | 패커코드 |
| IMPORT_ID_NO | 수입식별번호 |
| PACKER_PRODUCT_CODE | 패커 상품코드 |
| BARCODE_TYPE | 바코드유형 |
| ITEM_TYPE | 상품타입 |
| BARCODEGOODS | 바코드 상품코드 |
| STORE_IN_DATE | 납품일자 |
| EMARTLOGIS_CODE | 물류코드 |

---

*작성일: 2024년*
*기반 문서: doc/column/*.md*
