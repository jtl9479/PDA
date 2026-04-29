# S_BARCODE_INFO와 B_ITEM의 동명 컬럼 중복 설계 — 의도성 여부


---

## 1. 질문 요지 (한 줄)

원본 Oracle DB에서 **`S_BARCODE_INFO`와 `B_ITEM`이 동일한 이름의 바코드 파싱 규칙 컬럼**(BARCODEGOODS_FROM/TO, WEIGHT_FROM/TO, MAKINGDATE_FROM/TO, BOXSERIAL_FROM/TO, ZEROPOINT, PACKER_PRD_CODE_FROM/TO, STATUS, REG_ID/DATE/TIME 등)을 **중복 보유**하는 것이 의도된 설계입니까, 아니면 레거시 확장의 결과입니까?

---

## 2. 발견 경위

- 이마트 원본 JSP는 `FROM S_BARCODE_INFO SBI` 기반으로 바코드 파싱 규칙 컬럼 조회
- 비정량 원본 JSP는 `FROM B_ITEM sbi` 기반이지만 **동일한 컬럼명**(`SBI.ZEROPOINT`, `SBI.BARCODEGOODS_FROM` 등) 참조

---

## 3. 기술적 사실 (확정)

| 항목  | 내용                                                                                                                                                                                                                         | 근거 위치                                                                                      |
| --- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| 1   | 이마트 원본 JSP는 S_BARCODE_INFO를 메인 테이블로 사용 (3-way JOIN)                                                                                                                                                                        | `apache-tomcat-7.0.78_PDA_IN(원본)/webapps/ROOT/inno/search_barcode_info.jsp:41~70`          |
| 2   | 비정량 원본 JSP는 B_ITEM 단독 사용 (JOIN 없음)                                                                                                                                                                                         | `apache-tomcat-7.0.78_PDA_IN(원본)/webapps/ROOT/inno/search_barcode_info_nonfixed.jsp:40~66` |
| 3   | 비정량 JSP는 `SBI.ZEROPOINT`, `SBI.BARCODEGOODS_FROM/TO`, `SBI.WEIGHT_FROM/TO`, `SBI.MAKINGDATE_FROM/TO`, `SBI.BOXSERIAL_FROM/TO`, `SBI.PACKER_PRD_CODE_FROM/TO`, `SBI.STATUS`, `SBI.REG_ID/DATE/TIME` 참조 (SBI = B_ITEM alias) | 비정량 JSP L48~62                                                                             |
| 5   | 따라서 원본 Oracle DB에서 두 테이블은 **동명 컬럼을 중복 보유**하는 상태로 운영됨                                                                                                                                                                       |                                                                                            |


---

## 4. 비즈니스 의문점

- 바코드 파싱 규칙(구간 위치)을 **같은 이름**으로 두 테이블에 중복 저장한 이유가 무엇입니까?
- 만약 같은 품목코드에 대해 `S_BARCODE_INFO.WEIGHT_FROM`과 `B_ITEM.WEIGHT_FROM`의 **실제 값이 다를 수 있습니까**?
- B_ITEM의 바코드 파싱 컬럼은 **원래 있던 설계**입니까, **비정량 라인 추가 시 확장**된 것입니까?

---

## 5. 확인 요청 사항

### 🔑 핵심 질문

**현재 HL ERP의 `C0705`(품목코드관리) 화면에서 바코드 파싱 규칙(바코드상품코드시작/끝, 중량시작/끝, 제조일자시작/끝, 박스시리얼시작/끝, 소수점, 생산품상태 등)을 관리해도 됩니까?**

- 즉 기존 원본 Oracle에서 `S_BARCODE_INFO`에 입력하던 바코드 규칙 정보를 **MSSQL 전환 후에는 C0705 화면 1곳에서만 관리해도 업무상 문제가 없습니까**?

---