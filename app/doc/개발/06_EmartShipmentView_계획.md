# 이마트 출하 VIEW 분석 개발 문서

> 개발목표 항목 7: 이마트 출하 VIEW 분석

---

## 개요

PDA 출하 시스템에서 사용하는 VIEW 7종에 대한 구조 분석 및 컬럼 사용 현황 정리

---

## Step 1. VW_PDA_WID_LIST (이마트) VIEW 구조 분석

**Part 1. 분석**
- 메서드: VIEW (SQL)
- 범위: VW_PDA_WID_LIST 전체
- 용도: 이마트 출하 계근 작업용 데이터 조회
- 주의할 점: HIGHLAND 스키마 사용, 해외매입/국내매입 UNION 구조
- 호출 수: 테이블 11개, 함수 5개

| # | 항목 | 스키마 | 컬럼 수 | searchType |
|---|------|--------|---------|------------|
| 1 | VW_PDA_WID_LIST | HIGHLAND | 41 | 0 |

**사용 테이블**:
| 테이블 | 별칭 | JOIN 유형 | 용도 |
|--------|------|----------|------|
| W_GOODS_IH | IH | FROM | 출고 Header |
| W_GOODS_ID | ID | INNER JOIN | 출고 상세 |
| W_GOODS_R | WR | INNER JOIN | 입고 정보 |
| I_BL_D | BD | INNER JOIN | BL 상세 (해외매입) |
| I_OFFER_D | OD | INNER JOIN | 오퍼 상세 (국내매입) |
| B_SUPPLIER_ITEM | BSI | LEFT OUTER JOIN | 공급업체 상품 |
| B_ITEM | BI | INNER JOIN | 상품 마스터 |
| W_EMART_ORDER_ITEM | EOI | INNER JOIN (서브쿼리) | 이마트 발주 |
| B_EMART_BARCODE | EB | INNER JOIN (서브쿼리) | 이마트 바코드 |
| B_COMMON_CODE | BCC | INNER/LEFT JOIN | 공통코드 |
| S_BARCODE_INFO | A | 서브쿼리 | 바코드 정보 |

**사용 함수**:
| 함수명 | 용도 |
|--------|------|
| DE_ITEM() | 상품코드 → 상품명 |
| DE_COMMON() | 공통코드 → 코드명 |
| DE_CLIENT() | 거래처코드 → 거래처명 |
| DE_CLIENT2() | 거래처코드 → 거래처명2 |
| DECODE() | 조건부 값 변환 |

**Part 2. 변환 계획**
- 변환 방식: 해당 없음 (분석 작업)
- 사용할 헬퍼 메서드: 해당 없음
- 명령어 매핑: 해당 없음
- 주의사항: 분석 결과를 문서로 정리

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

---

## Step 2. VIEW 7종 비교 분석

**Part 1. 분석**
- 메서드: VIEW 7종 전체
- 범위: 전체 VIEW
- 용도: searchType별 출하 유형 데이터 조회
- 주의할 점: 스키마(HIGHLAND/INNO) 및 컬럼 수 차이
- 호출 수: VIEW 7개

| # | VIEW명 | 용도 | searchType | 스키마 | 총 컬럼 | 사용 | 미사용 |
|---|--------|------|------------|--------|---------|------|--------|
| 1 | VW_PDA_WID_LIST | 이마트 계근 | 0 | HIGHLAND | 41 | 37 | 4 |
| 2 | VW_PDA_WID_PRO_LIST | 생산투입 계근 | 1 | INNO | 33 | 28 | 5 |
| 3 | VW_PDA_WID_HOMEPLUS_LIST | 홈플러스 계근 | 2 | HIGHLAND | 34 | 28 | 6 |
| 4 | VW_PDA_WID_WHOLESALE_LIST | 도매 계근 | 3 | HIGHLAND | 34 | 28 | 6 |
| 5 | VW_PDA_WID_LIST_NONFIXED | 이마트 비정량 | 4 | INNO | 38 | 33 | 5 |
| 6 | VW_PDA_WID_LIST_NONFIXED_HP | 홈플러스 비정량 | 5 | INNO | 39 | 28 | 11 |
| 7 | VW_PDA_WID_LIST_LOTTE | 롯데마트 계근 | 6 | INNO | 35 | 30 | 5 |

**Part 2. 변환 계획**
- 변환 방식: 해당 없음 (분석 작업)
- 사용할 헬퍼 메서드: 해당 없음
- 명령어 매핑: 해당 없음
- 주의사항: VIEW별 차이점 문서화

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

---

## Step 3. 미사용 컬럼 분석

**Part 1. 분석**
- 메서드: 전체 VIEW 컬럼
- 범위: 미사용 컬럼
- 용도: 제거 대상 컬럼 식별
- 주의할 점: 모든 VIEW 공통 미사용 vs 일부 미사용 구분
- 호출 수: 컬럼 분석

**전체 VIEW 공통 미사용 컬럼 (4개)**:
| # | 컬럼명 | 설명 | 비고 |
|---|--------|------|------|
| 1 | AMOUNT | 출하상품금액 | 7개 VIEW 전체 미사용 |
| 2 | GR_REF_NO | 창고입고번호 | 7개 VIEW 전체 미사용 |
| 3 | BRANDNAME | 브랜드명 | 7개 VIEW 전체 미사용 |
| 4 | PACKERNAME | 패커이름 | 7개 VIEW 전체 미사용 |

**VW_PDA_WID_LIST (이마트) 미사용 컬럼 (4개)**:
| # | 컬럼명 | 판정 |
|---|--------|------|
| 1 | AMOUNT | 미사용 |
| 2 | GR_REF_NO | 미사용 |
| 3 | BRANDNAME | 미사용 |
| 4 | CONTAINER_TYPE | 미사용 |

**Part 2. 변환 계획**
- 변환 방식: 해당 없음 (분석 작업)
- 사용할 헬퍼 메서드: 해당 없음
- 명령어 매핑: 해당 없음
- 주의사항: 미사용 컬럼 제거 시 영향도 분석 필요

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

---

## Step 4. 공통 사용 컬럼 분석

**Part 1. 분석**
- 메서드: 전체 VIEW 컬럼
- 범위: 공통 사용 컬럼
- 용도: 핵심 데이터 컬럼 식별
- 주의할 점: 모든 VIEW에서 필수로 사용되는 컬럼
- 호출 수: 컬럼 분석

**모든 VIEW 공통 사용 컬럼**:
| # | 컬럼명 | 용도 |
|---|--------|------|
| 1 | GI_H_ID | 출고 Header ID (JOIN 조건) |
| 2 | GI_D_ID | 출고 상세 ID (핵심 키) |
| 3 | EOI_ID | 발주번호 |
| 4 | ITEM_CODE | 상품코드 |
| 5 | ITEM_NAME | 상품명 |
| 6 | EMARTITEM_CODE | 마트 상품코드 |
| 7 | EMARTITEM | 마트 상품명 |
| 8 | GI_REQ_PKG | 요청수량 |
| 9 | GI_REQ_QTY | 요청중량 |
| 10 | GI_REQ_DATE | 요청일자 |
| 11 | BL_NO | BL번호 |
| 12 | BRAND_CODE | 브랜드코드 |
| 13 | CLIENTNAME | 출고업체명 |
| 14 | CENTERNAME | 센터명 |
| 15 | ITEM_SPEC | 스펙 |
| 16 | CT_CODE | 원산지 |
| 17 | PACKER_CODE | 패커코드 |
| 18 | IMPORT_ID_NO | 수입식별번호 |
| 19 | PACKER_PRODUCT_CODE | 패커 상품코드 |
| 20 | BARCODE_TYPE | 바코드유형 |
| 21 | ITEM_TYPE | 상품타입 |
| 22 | BARCODEGOODS | 바코드 상품코드 |
| 23 | STORE_IN_DATE | 납품일자 |
| 24 | EMARTLOGIS_CODE | 물류코드 |

**Part 2. 변환 계획**
- 변환 방식: 해당 없음 (분석 작업)
- 사용할 헬퍼 메서드: 해당 없음
- 명령어 매핑: 해당 없음
- 주의사항: 공통 컬럼은 모든 VIEW에서 유지 필수

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

---

## Step 5. searchType별 처리 차이 분석

**Part 1. 분석**
- 메서드: ShipmentActivity.java
- 범위: searchType 분기 로직
- 용도: searchType별 비즈니스 로직 차이 식별
- 주의할 점: 중량 처리, 소비기한, 프린터, DB INSERT 분기
- 호출 수: 분기 로직 다수

**searchType 값 정리**:
| # | searchType | 용도 | VIEW |
|---|------------|------|------|
| 1 | 0 | 이마트 출하 | VW_PDA_WID_LIST |
| 2 | 1 | 생산투입 계근 | VW_PDA_WID_PRO_LIST |
| 3 | 2 | 홈플러스 출하 | VW_PDA_WID_HOMEPLUS_LIST |
| 4 | 3 | 도매 출하 | VW_PDA_WID_WHOLESALE_LIST |
| 5 | 4 | 이마트 비정량 | VW_PDA_WID_LIST_NONFIXED |
| 6 | 5 | 홈플러스 비정량 | VW_PDA_WID_LIST_NONFIXED_HP |
| 7 | 6 | 롯데마트 | VW_PDA_WID_LIST_LOTTE |
| 8 | 7 | 생산 라벨출력 | (생산 VIEW 사용) |

**주요 처리 차이**:
| # | 처리 항목 | 이마트(0) | 그 외 |
|---|----------|-----------|-------|
| 1 | 중량 소수점 | 1자리 버림 | 원본 유지 |
| 2 | 소비기한 입력 | 필요 (TRD/WET/E/T) | 생략 |
| 3 | 라벨 출력 | setPrinting() | 각 전용 메서드 |
| 4 | DB INSERT | insertqueryGoodsWet() | 각 전용 메서드 |
| 5 | 서버 전송 | insert_goods_wet.jsp | 각 전용 JSP |

**Part 2. 변환 계획**
- 변환 방식: 해당 없음 (분석 작업)
- 사용할 헬퍼 메서드: 해당 없음
- 명령어 매핑: 해당 없음
- 주의사항: 분기 로직 변경 시 전체 테스트 필요

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

---

## Step 6. 테이블/함수 의존성 분석

**Part 1. 분석**
- 메서드: VIEW 7종 전체
- 범위: 테이블 및 함수 사용 현황
- 용도: 의존성 파악 및 영향도 분석
- 주의할 점: VIEW별 테이블 사용 차이
- 호출 수: 테이블 16개, 함수 7개

**테이블 사용 매트릭스**:
| 테이블 | LIST | PRO | HP | WHOLE | NONFIX | NONFIX_HP | LOTTE |
|--------|:----:|:---:|:--:|:-----:|:------:|:---------:|:-----:|
| W_GOODS_IH | O | O | O | O | O | O | O |
| W_GOODS_ID | O | O | O | O | O | O | O |
| W_GOODS_R | O | O | O | O | O | O | O |
| I_BL_D | O | - | O | O | - | - | O |
| I_OFFER_D | O | O | O | O | - | - | O |
| B_ITEM | O | O | - | O | O | O | - |
| B_SUPPLIER_ITEM | O | - | - | - | - | - | - |
| W_EMART_ORDER_ITEM | O | - | - | - | O | - | - |
| W_E_ORDER_ITEM | - | - | O | - | - | O | - |
| W_MART_ORDER_ITEM | - | - | - | - | - | - | O |
| B_EMART_BARCODE | O | - | O | - | O | O | O |
| B_COMMON_CODE | O | O | O | - | O | - | O |
| B_CLIENT | - | O | - | - | - | - | - |
| B_CLIENT_CHARGE | - | - | - | - | - | - | O |
| B_COUNTRY | - | - | O | - | - | O | - |
| S_BARCODE_INFO | O | O | O | O | - | - | O |
| W_GOODS_WET | - | - | - | - | - | - | O |

**함수 사용 매트릭스**:
| 함수 | LIST | PRO | HP | WHOLE | NONFIX | NONFIX_HP | LOTTE |
|------|:----:|:---:|:--:|:-----:|:------:|:---------:|:-----:|
| DE_ITEM() | O | - | O | O | O | - | O |
| DE_COMMON() | O | - | O | O | - | O | O |
| DE_CLIENT() | O | O | O | O | O | - | O |
| DE_CLIENT2() | O | - | O | - | O | - | - |
| DECODE() | O | O | O | O | O | - | O |
| NVL() | - | O | O | - | - | - | O |
| LEAD() OVER | - | - | - | - | - | - | O |

**Part 2. 변환 계획**
- 변환 방식: 해당 없음 (분석 작업)
- 사용할 헬퍼 메서드: 해당 없음
- 명령어 매핑: 해당 없음
- 주의사항: 테이블/함수 변경 시 영향받는 VIEW 파악 필요

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

---

## Step 7. VIEW 전용 컬럼 분석

**Part 1. 분석**
- 메서드: VIEW별 전용 컬럼
- 범위: 특정 VIEW에만 존재하는 컬럼
- 용도: VIEW별 특수 기능 식별
- 주의할 점: 전용 컬럼은 해당 VIEW에서만 사용
- 호출 수: 전용 컬럼 다수

**VW_PDA_WID_LIST (이마트) 전용**:
| 컬럼명 | 사용여부 | 비고 |
|--------|----------|------|
| EMART_PLANT_CODE | 사용 | 이마트 공장코드 |
| MAJOR_CATEGORY | 사용 | 주요 카테고리 |
| CONTAINER_TYPE | 미사용 | 용기 타입 |

**VW_PDA_WID_LIST_LOTTE (롯데) 전용**:
| 컬럼명 | 사용여부 | 비고 |
|--------|----------|------|
| LAST_BOX_ORDER | 사용 | 마지막 박스 순번 |

**VW_PDA_WID_HOMEPLUS_LIST (홈플러스) 전용**:
| 컬럼명 | 사용여부 | 비고 |
|--------|----------|------|
| HOMPLUS_STORE_CODE | 사용 | 홈플러스 점포코드 |

**Part 2. 변환 계획**
- 변환 방식: 해당 없음 (분석 작업)
- 사용할 헬퍼 메서드: 해당 없음
- 명령어 매핑: 해당 없음
- 주의사항: 전용 컬럼 변경 시 해당 VIEW만 영향

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

---

## Step 8. 분석 결과 종합

**Part 1. 분석**
- 메서드: 전체 분석 종합
- 범위: VIEW 7종 전체
- 용도: 분석 결과 요약 및 권고사항 도출
- 주의할 점: 미사용 컬럼 제거 시 영향도 검토
- 호출 수: 종합 분석

**제거 권장 컬럼 (전체 VIEW 공통)**:
| 컬럼명 | 제거 영향도 | 권장 |
|--------|-------------|------|
| AMOUNT | 낮음 | 제거 권장 |
| GR_REF_NO | 낮음 | 제거 권장 |
| BRANDNAME | 낮음 | 제거 권장 |
| PACKERNAME | 낮음 | 제거 권장 |
| EMARTLOGIS_NAME | 낮음 (6/7 VIEW) | 제거 권장 |

**제거 시 수정 위치**:
1. VIEW DDL: 각 VIEW에서 SELECT 컬럼 제거
2. JSP: search_*.jsp 파일에서 SELECT/출력 제거
3. Android:
   - Shipments_Info.java: 필드, getter/setter 제거
   - DBInfo.java: 상수 제거
   - DBHandler.java: INSERT/SELECT 문 수정
   - ProgressDlgShipSearch.java: 파싱 인덱스 조정

**Part 2. 변환 계획**
- 변환 방식: 해당 없음 (분석 작업)
- 사용할 헬퍼 메서드: 해당 없음
- 명령어 매핑: 해당 없음
- 주의사항: 제거 작업은 별도 개발 항목으로 진행

**체크리스트**
- [x] Part 1: 분석 완료 확인
- [x] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트
- [ ] Part 7: 주석 보강
- [ ] Part 8: 변경 내용 작성

**Part 8. 변경 내용** (완료 후 작성):
- **무엇을**: 이마트 출하 VIEW 7종 구조 및 컬럼 사용 현황
- **왜**: PDA 시스템 유지보수 및 미사용 컬럼 정리를 위한 기초 분석
- **어떻게**: VIEW DDL, ShipmentActivity.java 소스코드 분석

---

## 관련 문서

- VIEW_COLUMN_COMPARISON.md - VIEW별 컬럼 비교 분석
- VIEW_TABLE_USAGE.md - VIEW별 테이블 및 함수 사용 상세
- COMMON_COLUMN_USAGE_BY_SEARCHTYPE.md - searchType별 컬럼 처리 차이
- VW_PDA_WID_LIST.md - 이마트 VIEW 상세
- VW_PDA_WID_PRO_LIST.md - 생산 VIEW 상세
- VW_PDA_WID_HOMEPLUS_LIST.md - 홈플러스 VIEW 상세
- VW_PDA_WID_WHOLESALE_LIST.md - 도매 VIEW 상세
- VW_PDA_WID_LIST_NONFIXED.md - 이마트 비정량 VIEW 상세
- VW_PDA_WID_LIST_NONFIXED_HP.md - 홈플러스 비정량 VIEW 상세
- VW_PDA_WID_LIST_LOTTE.md - 롯데 VIEW 상세

---

*작성일: 2025-01-12*
*개발목표 항목: 7. 이마트 출하 VIEW 분석*
