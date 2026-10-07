# 롯데 라벨 바코드 맨 앞 회사코드 미출력 (매핑 물류코드 공백)

## 발견일
2026-10-06

(수정리스트 `app/doc/개발/70_테스트후_수정리스트.xlsx` A2 "롯데 라벨 회사코드 미노출", 2026-10-06 확인)

## 에러 발생 시나리오

```
1. 롯데(searchType=6, 바코드타입 L0) 출하대상 받기 (P00082 LOT_진장점 등, 품목 1110100681)
2. 출하 계근 후 라벨 인쇄
3. 바코드 조립: 회사코드 + 제조일(6) + 중량(4) + 상품코드(6) + 박스순번(4)
4. 회사코드 자리가 빈 값 → 바코드 앞부분이 비어 출력
   (실측 바코드 26092906338809790013 = 제조일6 + 중량4 + 상품코드6 + 박스순번4, 회사코드 없음)
```

---

## 현상
- 롯데 라벨 바코드/바코드 문자열 맨 앞 회사코드가 출력되지 않음
- 실측 바코드 `26092906338809790013` (20자리) — 정상이라면 회사코드가 앞에 붙어야 함
- **원본은 값이 없으면 '회사코드없음'을 출력하나, 현재는 빈 값으로 아무 표시도 없음**

## 원래부터 있던 버그인가?

**NO (부분) - 앱 코드는 원본과 동일하나, 회사코드 조회 소스가 Oracle 뷰(센터코드 기준 공통코드) → MSSQL JSP(물류코드 매핑)로 전환되면서 값이 비게 됨**

```sql
-- 원본 Oracle 뷰 app/doc/view/VW_PDA_WID_LIST_LOTTE
(SELECT NVL(REF_CODE2,'회사코드없음') FROM B_COMMON_CODE bcc
  WHERE bcc.MASTER_CODE LIKE 'LOTTE_STORE_CODE' AND bcc.code = wmoi.CENTER_CODE) AS EMARTLOGIS_CODE
-- 센터코드 기준, 없으면 '회사코드없음'
```

```sql
-- 현재 JSP search_shipment_lotte.jsp:61
COALESCE(M1.물류코드, M2.물류코드) AS EMARTLOGIS_CODE
-- M1 = 출고거래처 매핑, M2 = 상위계층(LEFT(계층코드,5)) 거래처 매핑
```

전환 결정: `app/doc/개발/49_롯데_출하대상받기_JSP_MSSQL전환.md`에서 이마트 패턴과 동일하게 물류코드로 매핑.

## 원인

### 문제 1 (주요): 매핑 레코드의 물류코드가 빈 값 (데이터 문제)

#### 코드 위치
- `search_shipment_lotte.jsp` : 61줄 (SELECT), 151줄 (index 23 전송)
- `LabelPrintHelper.java` : 1107줄 (`pCompCode_lotte`), 1209/1211줄 (바코드/바코드문자열 조립)

#### 현재 문제 코드
```java
// LabelPrintHelper.java:1107
String pCompCode_lotte = si.EMARTLOGIS_CODE; // 롯데전용 업체코드 뷰에서 EMARTLOGIS_CODE로 받아옴
// LabelPrintHelper.java:1209
pBarcode = pCompCode_lotte+making_date+print_weight_str.substring(print_weight_str.length()-4, print_weight_str.length())+si.getEMARTITEM_CODE().substring(0, 6) +boxserial_cnt;
// ★ EMARTLOGIS_CODE가 빈 값이면 회사코드 자리가 비어 출력 (예외 없이 조용히 누락)
```

#### 발생 시나리오
운영 DB(weberp_hl) 조회 결과:
- 롯데 출고 7건(P00082 LOT_진장점 등, 품목 1110100681) 모두 M1(출고거래처) 매핑 없음
- M2(상위 A00565) 매핑 1건 존재 — SEQ 42, 타입구분 W, 바코드타입 L0, **물류코드 빈 값**
- 따라서 `COALESCE(M1.물류코드, M2.물류코드)` = 빈 값 → EMARTLOGIS_CODE 빈 값
- 회사 20 매핑 중 물류코드가 입력된 거래처는 A00561(6건), A01852(1건)뿐
- SM_마트사발주롯데마트에는 업체코드 컬럼이 없어 대체 소스 없음

### 문제 2 (보조): 값 없을 때 기본값('회사코드없음') 미이식

#### 코드 위치
- `search_shipment_lotte.jsp` : 61줄

#### 문제 코드
```sql
COALESCE(M1.물류코드, M2.물류코드) AS EMARTLOGIS_CODE
-- ★ 원본의 NVL(REF_CODE2,'회사코드없음') 기본값 없음. 빈 값/NULL이 그대로 앱으로 전달됨
-- (21번 오류와 같은 계열: 빈 문자열 미방어)
```

### 문제 3 (보조): 컬럼 용도 불일치

- 원본은 센터별 롯데 회사코드(공통코드 LOTTE_STORE_CODE.REF_CODE2)를 사용
- 현재는 거래처-품목 매핑의 `물류코드`(이마트에서는 물류센터 납품코드)를 롯데 회사코드로 재사용 → 용도가 다름

## 상세 흐름

1. **출하대상 받기** (searchType=6)
   - `search_shipment_lotte.jsp`가 M1/M2 매핑에서 물류코드 조회
   - M1 없음, M2(A00565, SEQ 42) 물류코드 빈 값 → EMARTLOGIS_CODE = ""

2. **앱 파싱** (index 23)
   - `si.EMARTLOGIS_CODE` = "" 저장

3. **라벨 출력 `setPrintingLotte`** (바코드타입 L0)
   - `pCompCode_lotte = ""`
   - **경로 A (현재)**: 빈 값 → 바코드 `제조일6+중량4+상품코드6+박스순번4` (20자리), 회사코드 누락
   - **경로 B (원본)**: 값 없으면 '회사코드없음' 출력 → 누락 여부가 눈에 보임

## 영향 범위
- 롯데 L0 라벨 바코드 및 바코드 문자열 (회사코드 자리)
- 롯데 출하대상 중 M1 매핑이 없고 상위(M2) 매핑 물류코드도 비어 있는 모든 거래처/품목
- 파일: `search_shipment_lotte.jsp`, `LabelPrintHelper.java` (setPrintingLotte), ERP `CO_매출처품목코드매핑` 데이터

## 수정 방안 (미적용, 사용자 결정 대기)

### 수정 1: 현 구조 유지 + 데이터 조치
- ERP `CO_매출처품목코드매핑` A00565 / 1110100681 (SEQ 42) 물류코드에 롯데 회사코드 입력
- 실제 코드값 필요. 단 컬럼명이 물류코드(이마트에선 물류센터 납품코드)라 용도 불일치

### 수정 2: 원본 방식 복원
- 센터별 롯데 회사코드 공통코드를 신설하고 JSP를 수정해 센터코드 기준으로 조회

```sql
-- 예시 (공통코드 구조 확정 후)
COALESCE(NULLIF(<센터별 롯데 회사코드>, ''), '회사코드없음') AS EMARTLOGIS_CODE
```

### 수정 3 (선택): JSP 빈 값 기본값
```sql
COALESCE(NULLIF(COALESCE(M1.물류코드, M2.물류코드), ''), '회사코드없음') AS EMARTLOGIS_CODE
```

> 수정 3만 적용하면 원본처럼 누락이 '회사코드없음'으로 드러날 뿐 실제 회사코드가 출력되지는 않는다. 근본 해결은 수정 1 또는 2. 앱 코드는 원본과 동일하므로 수정 불필요.

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/개발/49_롯데_출하대상받기_JSP_MSSQL전환.md` — 물류코드 매핑 전환 결정
- `app/doc/개발/70_테스트후_수정리스트.xlsx` — A2 롯데 라벨 회사코드 미노출
- `app/doc/view/VW_PDA_WID_LIST_LOTTE` — 원본 Oracle 뷰
- `app/doc/column/02_VW_PDA_WID_LIST_LOTTE.md` — 컬럼 정의
- `app/doc/view/VW_PDA_WID_LIST분석폴더/25_EMARTLOGIS_CODE_컬럼사용현황.md`
- `app/doc/테스트/증빙/2026-10-06_바코드타입정리_EDA51/` — 05_L0_롯데0929_계근출력.png, 08_L0_롯데0929_제조일6자리_수정후.png
- `app/doc/오류/21_*` (EMARTLOGIS_CODE 빈 문자열, 같은 계열)
- 오류 패턴: **패턴 C: 컬럼명/구조 변경** (`app/doc/참고자료/오류패턴_분석.md`)
