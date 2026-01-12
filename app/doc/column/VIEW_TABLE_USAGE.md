# VIEW별 사용 테이블 및 함수 상세

---

## 1. VW_PDA_WID_LIST (이마트) - HIGHLAND 스키마

### 테이블
| 테이블                | 별칭               | JOIN 유형           | 용도                         |     |
| ------------------ | ---------------- | ----------------- | -------------------------- | --- |
| W_GOODS_IH         | IH               | FROM              | 출고 Header                  |     |
| W_GOODS_ID         | ID               | INNER JOIN        | 출고 상세                      |     |
| W_GOODS_R          | WR               | INNER JOIN        | 입고 정보                      |     |
| I_BL_D             | BD               | INNER JOIN        | BL 상세 (해외매입 UNION 첫번째)     |     |
| I_OFFER_D          | OD               | INNER JOIN        | 오퍼 상세 (국내매입 UNION 두번째)     |     |
| B_SUPPLIER_ITEM    | BSI              | LEFT OUTER JOIN   | 공급업체 상품 (EMART_PLANT_CODE) |     |
| B_ITEM             | BI               | INNER JOIN        | 상품 마스터                     |     |
| W_EMART_ORDER_ITEM | EOI              | INNER JOIN (서브쿼리) | 이마트 발주                     |     |
| B_EMART_BARCODE    | EB               | INNER JOIN (서브쿼리) | 이마트 바코드                    |     |
| B_COMMON_CODE      | BCC, BCC2, BC, X | INNER/LEFT JOIN   | 공통코드                       |     |
| S_BARCODE_INFO     | A                | 서브쿼리 (SELECT)     | 바코드 정보                     |     |

### 함수
| 함수명 | 용도 |
|--------|------|
| DE_ITEM() | 상품코드 → 상품명 |
| DE_COMMON() | 공통코드 → 코드명 |
| DE_CLIENT() | 거래처코드 → 거래처명 |
| DE_CLIENT2() | 거래처코드 → 거래처명2 |
| DECODE() | 조건부 값 변환 |

---

## 2. VW_PDA_WID_PRO_LIST (생산) - INNO 스키마

### 테이블
| 테이블 | 별칭 | JOIN 유형 | 용도 |
|--------|------|----------|------|
| W_GOODS_IH | A | FROM | 출고 Header |
| W_GOODS_ID | B | WHERE 조인 | 출고 상세 |
| W_GOODS_R | R | WHERE 조인 | 입고 정보 |
| I_OFFER_D | D | WHERE 조인 | 오퍼 상세 |
| B_ITEM | I | WHERE 조인 | 상품 마스터 |
| B_COMMON_CODE | - | 서브쿼리 (SELECT) | 브랜드명 조회 |
| B_CLIENT | - | 서브쿼리 (SELECT) | 거래처명 조회 |
| S_BARCODE_INFO | S | 서브쿼리 (SELECT) | 바코드 정보 |

### 함수
| 함수명 | 용도 |
|--------|------|
| DE_CLIENT() | 거래처코드 → 거래처명 |
| DECODE() | 조건부 값 변환 |
| NVL() | NULL 처리 |

### 하드코딩 값
- EOI_ID = '1111'
- CENTERNAME = '하이랜드푸드'
- BARCODE_TYPE = 'P0'
- EMARTLOGIS_CODE = '0000000'
- EMARTLOGIS_NAME = '정보없음'

---

## 3. VW_PDA_WID_HOMEPLUS_LIST (홈플러스) - HIGHLAND 스키마

### 테이블
| 테이블 | 별칭 | JOIN 유형 | 용도 |
|--------|------|----------|------|
| W_GOODS_IH | IH | FROM | 출고 Header |
| W_GOODS_ID | ID | INNER JOIN | 출고 상세 |
| W_GOODS_R | WR | INNER JOIN | 입고 정보 |
| I_BL_D | BD | INNER JOIN | BL 상세 (해외매입) |
| I_OFFER_D | OD | INNER JOIN | 오퍼 상세 (국내매입) |
| W_E_ORDER_ITEM | EOI | INNER JOIN (서브쿼리) | 홈플러스 발주 |
| B_EMART_BARCODE | EB | INNER JOIN (서브쿼리) | 바코드 마스터 |
| B_COMMON_CODE | BCC | INNER JOIN (서브쿼리) | 공통코드 |
| B_COUNTRY | CT | 서브쿼리 (SELECT) | 원산지명 |
| S_BARCODE_INFO | A | 서브쿼리 (SELECT) | 바코드 정보 |

### 함수
| 함수명 | 용도 |
|--------|------|
| DE_ITEM() | 상품코드 → 상품명 |
| DE_COMMON() | 공통코드 → 코드명 |
| DE_CLIENT() | 거래처코드 → 거래처명 |
| DE_CLIENT2() | 거래처코드 → 거래처명2 |
| DECODE() | 조건부 값 변환 |
| NVL() | NULL 처리 |

### 하드코딩 값
- ITEM_TYPE = 'S' (소수점 2자리 계근용)

---

## 4. VW_PDA_WID_WHOLESALE_LIST (도매) - HIGHLAND 스키마

### 테이블
| 테이블 | 별칭 | JOIN 유형 | 용도 |
|--------|------|----------|------|
| W_GOODS_IH | IH | FROM | 출고 Header |
| W_GOODS_ID | ID | INNER JOIN | 출고 상세 |
| W_GOODS_R | WR | INNER JOIN | 입고 정보 |
| I_BL_D | BD | INNER JOIN | BL 상세 (해외매입) |
| I_OFFER_D | OD | INNER JOIN | 오퍼 상세 (국내매입) |
| B_ITEM | BI | INNER JOIN | 상품 마스터 |
| S_BARCODE_INFO | A | 서브쿼리 (SELECT) | 바코드 정보 |

### 함수
| 함수명 | 용도 |
|--------|------|
| DE_ITEM() | 상품코드 → 상품명 |
| DE_COMMON() | 공통코드 → 코드명 |
| DE_CLIENT() | 거래처코드 → 거래처명 |
| DECODE() | 조건부 값 변환 |

### 하드코딩 값
- EOI_ID = 'NA'
- EMARTITEM_CODE = 'NA'
- EMARTITEM = 'NA'
- BARCODE_TYPE = 'NA'
- ITEM_TYPE = 'S'
- PACKWEIGHT = 'NA'
- EMARTLOGIS_CODE = '0000000'
- EMARTLOGIS_NAME = '정보없음'
- WH_AREA = ''

---

## 5. VW_PDA_WID_LIST_NONFIXED (이마트 비정량) - INNO 스키마

### 테이블
| 테이블 | 별칭 | JOIN 유형 | 용도 |
|--------|------|----------|------|
| W_GOODS_IH | IH | FROM | 출고 Header |
| W_GOODS_ID | ID | INNER JOIN | 출고 상세 |
| W_GOODS_R | WR | INNER JOIN | 입고 정보 |
| B_ITEM | BI | INNER JOIN | 상품 마스터 |
| W_EMART_ORDER_ITEM | EOI | INNER JOIN (서브쿼리) | 이마트 발주 |
| B_EMART_BARCODE | EB | INNER JOIN (서브쿼리) | 이마트 바코드 |
| B_COMMON_CODE | BCC, BCC2, BC, X | INNER/LEFT JOIN | 공통코드 |

### 함수
| 함수명 | 용도 |
|--------|------|
| DE_ITEM() | 상품코드 → 상품명 |
| DE_CLIENT() | 거래처코드 → 거래처명 |
| DE_CLIENT2() | 거래처코드 → 거래처명2 |
| DECODE() | 조건부 값 변환 |

### 하드코딩 값
- PACKER_CODE = '0000'
- ITEM_TYPE = 'HW' (하이랜드원료육)

### 특이사항
- I_BL_D, I_OFFER_D 사용 안함 (패커 정보 없음)
- BARCODE_TYPE = 'M8' 조건 고정

---

## 6. VW_PDA_WID_LIST_NONFIXED_HP (홈플러스 비정량) - INNO 스키마

### 테이블
| 테이블 | 별칭 | JOIN 유형 | 용도 |
|--------|------|----------|------|
| W_GOODS_IH | IH | FROM | 출고 Header |
| W_GOODS_ID | ID | INNER JOIN | 출고 상세 |
| W_GOODS_R | R | INNER JOIN | 입고 정보 |
| W_E_ORDER_ITEM | EOI | INNER JOIN | 홈플러스 발주 |
| B_EMART_BARCODE | BEB | INNER JOIN | 바코드 마스터 |
| B_ITEM | BI | INNER JOIN | 상품 마스터 |
| B_COUNTRY | BC | 서브쿼리 (SELECT) | 원산지명 |

### 함수
| 함수명 | 용도 |
|--------|------|
| DE_COMMON() | 공통코드 → 코드명 |

### 하드코딩 값
- PACKER_CODE = 'IN67677'
- PACKERNAME = '하이랜드이노베이션'
- PACKWEIGHT = NULL
- EMARTLOGIS_CODE = ' '
- EMARTLOGIS_NAME = ' '
- WH_AREA = ' '
- USE_NAME = ' '
- USE_CODE = ' '

### 특이사항
- I_BL_D, I_OFFER_D 사용 안함
- BARCODE_TYPE = 'H5' 조건 고정
- S_BARCODE_INFO 사용 안함

---

## 7. VW_PDA_WID_LIST_LOTTE (롯데) - INNO 스키마

### 테이블
| 테이블 | 별칭 | JOIN 유형 | 용도 |
|--------|------|----------|------|
| W_GOODS_IH | IH | FROM | 출고 Header |
| W_GOODS_ID | ID | INNER JOIN | 출고 상세 |
| W_GOODS_R | WR | INNER JOIN | 입고 정보 |
| I_BL_D | BD | INNER JOIN | BL 상세 (해외매입) |
| I_OFFER_D | OD | INNER JOIN | 오퍼 상세 (국내매입) |
| B_CLIENT_CHARGE | BCC | INNER JOIN | 거래처 담당 (USER_ID='LOTTE') |
| W_MART_ORDER_ITEM | WMOI | INNER JOIN | 롯데마트 발주 |
| B_EMART_BARCODE | BEB | INNER JOIN | 바코드 마스터 |
| B_COMMON_CODE | BCC | 서브쿼리 (SELECT) | 공통코드 (회사코드) |
| W_GOODS_WET | W | 서브쿼리 (SELECT) | 계근 데이터 (LAST_BOX_ORDER) |
| S_BARCODE_INFO | A | 서브쿼리 (SELECT) | 바코드 정보 |

### 함수
| 함수명 | 용도 |
|--------|------|
| DE_ITEM() | 상품코드 → 상품명 |
| DE_COMMON() | 공통코드 → 코드명 |
| DE_CLIENT() | 거래처코드 → 거래처명 |
| DECODE() | 조건부 값 변환 |
| NVL() | NULL 처리 |
| LEAD() OVER | 윈도우 함수 (다음 행 값) |

### 하드코딩 값
- ITEM_TYPE = 'S'
- PACKWEIGHT = NULL
- EMARTLOGIS_NAME = '정보없음'
- WH_AREA = ''

### 특이사항
- BARCODE_TYPE LIKE 'L%' 조건
- B_CLIENT_CHARGE.USER_ID = 'LOTTE' 필터
- LAST_BOX_ORDER 계산을 위해 W_GOODS_WET 사용

---

## 테이블 사용 요약 매트릭스

| 테이블 | LIST | PRO | HP | WHOLE | NONFIX | NONFIX_HP | LOTTE |
|--------|:----:|:---:|:--:|:-----:|:------:|:---------:|:-----:|
| **W_GOODS_IH** | O | O | O | O | O | O | O |
| **W_GOODS_ID** | O | O | O | O | O | O | O |
| **W_GOODS_R** | O | O | O | O | O | O | O |
| **I_BL_D** | O | - | O | O | - | - | O |
| **I_OFFER_D** | O | O | O | O | - | - | O |
| **B_ITEM** | O | O | - | O | O | O | - |
| **B_SUPPLIER_ITEM** | O | - | - | - | - | - | - |
| **W_EMART_ORDER_ITEM** | O | - | - | - | O | - | - |
| **W_E_ORDER_ITEM** | - | - | O | - | - | O | - |
| **W_MART_ORDER_ITEM** | - | - | - | - | - | - | O |
| **B_EMART_BARCODE** | O | - | O | - | O | O | O |
| **B_COMMON_CODE** | O | O | O | - | O | - | O |
| **B_CLIENT** | - | O | - | - | - | - | - |
| **B_CLIENT_CHARGE** | - | - | - | - | - | - | O |
| **B_COUNTRY** | - | - | O | - | - | O | - |
| **S_BARCODE_INFO** | O | O | O | O | - | - | O |
| **W_GOODS_WET** | - | - | - | - | - | - | O |

---

## 함수 사용 요약 매트릭스

| 함수 | LIST | PRO | HP | WHOLE | NONFIX | NONFIX_HP | LOTTE |
|------|:----:|:---:|:--:|:-----:|:------:|:---------:|:-----:|
| **DE_ITEM()** | O | - | O | O | O | - | O |
| **DE_COMMON()** | O | - | O | O | - | O | O |
| **DE_CLIENT()** | O | O | O | O | O | - | O |
| **DE_CLIENT2()** | O | - | O | - | O | - | - |
| **DECODE()** | O | O | O | O | O | - | O |
| **NVL()** | - | O | O | - | - | - | O |
| **LEAD() OVER** | - | - | - | - | - | - | O |

---

*작성일: 2024년*
