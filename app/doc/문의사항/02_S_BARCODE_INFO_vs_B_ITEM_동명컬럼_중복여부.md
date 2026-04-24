# S_BARCODE_INFO와 B_ITEM의 동명 컬럼 중복 설계 — 의도성 여부


---

## 1. 질문 요지 (한 줄)

원본 Oracle DB에서 **`S_BARCODE_INFO`와 `B_ITEM`이 동일한 이름의 바코드 파싱 규칙 컬럼**(BARCODEGOODS_FROM/TO, WEIGHT_FROM/TO, MAKINGDATE_FROM/TO, BOXSERIAL_FROM/TO, ZEROPOINT, PACKER_PRD_CODE_FROM/TO, STATUS, REG_ID/DATE/TIME 등)을 **중복 보유**하는 것이 의도된 설계인가, 아니면 레거시 확장의 결과인가?

---

## 2. 발견 경위

- 이마트 원본 JSP는 `FROM S_BARCODE_INFO SBI` 기반으로 바코드 파싱 규칙 컬럼 조회
- 비정량 원본 JSP는 `FROM B_ITEM sbi` 기반이지만 **동일한 컬럼명**(`SBI.ZEROPOINT`, `SBI.BARCODEGOODS_FROM` 등) 참조
- 두 테이블이 같은 컬럼명을 갖지 않으면 비정량 JSP가 Oracle에서 실행될 수 없음

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

- 바코드 파싱 규칙(구간 위치)을 **같은 이름**으로 두 테이블에 중복 저장한 이유가 무엇인가
- 만약 같은 품목코드에 대해 `S_BARCODE_INFO.WEIGHT_FROM`과 `B_ITEM.WEIGHT_FROM`의 **실제 값이 다를 수 있는가**?
    - 다르다면: 정량·비정량이 서로 다른 바코드 포맷을 사용한다는 의미
    - 같다면: 데이터 일관성 유지가 어떻게 보장되었는가 (트리거, 프로시저, 배치 등)?
- B_ITEM의 바코드 파싱 컬럼은 **원래 있던 설계**인가, **비정량 라인 추가 시 확장**된 것인가?

---

## 5. 확인 요청 사항

### 🔑 핵심 질문

**현재 HL ERP의 `C0705`(품목코드관리) 화면에서 바코드 파싱 규칙(바코드상품코드시작/끝, 중량시작/끝, 제조일자시작/끝, 박스시리얼시작/끝, 소수점, 생산품상태 등)을 관리해도 되는가?**

- 즉 기존 원본 Oracle에서 `S_BARCODE_INFO`에 입력하던 바코드 규칙 정보를 **MSSQL 전환 후에는 C0705 화면 1곳에서만 관리하면 업무상 문제 없는가**?
- 별도의 바코드 규칙 관리 화면이 필요한가, 아니면 `CO_품목코드` 통합으로 충분한가?

### 참고 세부 질문

1. `S_BARCODE_INFO.WEIGHT_FROM`과 `B_ITEM.WEIGHT_FROM`은 같은 품목에 대해 같은 값을 가지는가, 다른 값을 가지는가? (샘플 데이터 확인 요청)
2. 위 동명 컬럼들의 데이터 관리 주체·갱신 시점·책임 부서가 각각 어디인가?
3. B_ITEM의 바코드 파싱 컬럼은 B_ITEM 설계 당시부터 있었는가, 후속 확장으로 추가되었는가?
4. MSSQL 전환 후 CO_품목코드가 S_BARCODE_INFO와 B_ITEM의 바코드 컬럼을 어떻게 통합했는가 (어느 쪽 값을 기준으로 했는가)?
5. `PACKER_PRD_CODE_FROM/TO`, `REG_ID/DATE/TIME` 컬럼이 CO_품목코드에 누락된 것은 의도된 제거인가, 전환 과정의 누락인가?

---

## 7. 조치 방향 (답변 전까지)

- **답변 수신 전**: 
    - 개발36 Step 1~3은 **원본과 동일 동작 원칙**에 따라 그대로 진행 (CO_품목코드의 기존 컬럼 매핑 사용, 없는 컬럼은 빈값 처리)
    - 소스분석46 문서에 "S_BARCODE_INFO와 B_ITEM 동명 컬럼 중복 존재"를 주의사항으로 명시 (현재 미명시 상태 — 보강 필요)
- **답변 수신 후 시나리오별 조치**:
    - 해석 A 확정 시: 현재 CO_품목코드가 두 테이블 중 어느 쪽 값을 보존했는지 확인 필요, 잘못된 쪽은 보정 개발 필요
    - 해석 B 확정 시: 현재 전환 유효, 추가 조치 없음
    - 해석 C 확정 시: 현재 전환 유효, 문서에 이력 기록만 남김
    - 해석 D 확정 시: 비즈니스 분리 여부 재검토, 전환 검증 강화

---

## 8. 답변 (수신 시 작성)

| 항목 | 내용 |
|------|------|
| 답변 일자 | |
| 답변자 | |
| 결론 | |
| 후속 조치 | |

---

## 9. 관련 문서

- `app/doc/소스분석/46_이마트바코드정보조회_vs_비정량바코드정보조회_JSP_원본비교분석.md` — 원본 비교 문서 (본 문의 발견 근거)
- `app/doc/view/S_BARCODE_INFO.md` — S_BARCODE_INFO 컬럼 분석
- `app/doc/개발/36_비정량_바코드정보조회_JSP_MSSQL전환.md` — 비정량 JSP 전환 가이드 (CO_품목코드 매핑 포함)
- `app/doc/PDA_HL_ERP_전환_공수분석.md` — B_ITEM→CO_품목코드 매핑 명시
- `app/doc/일정/2026-04-22.md` — 최초 발견 일자 기록
- JSP 원본 이마트: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_barcode_info.jsp`
- JSP 원본 비정량: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\apache-tomcat-7.0.78_PDA_IN\webapps\ROOT\inno\search_barcode_info_nonfixed.jsp`
- ERP Entity: `D:\HL_ERP\workspace\SGIS_HL_WEBERP\src\main\java\com\sgis\domain\co\bizbasic\entity\ItemCodeEntity.java`
