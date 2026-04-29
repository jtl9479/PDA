# 비정량 이마트 EMARTLOGIS_CODE 빈 문자열 라벨 출력 예외

## 발견일
2026-04-27

## 에러 발생 시나리오

```
1. BixolonShipmentActivity에서 searchType=4 (이마트 비정량) 선택
2. 출하대상 조회 → search_production_nonfixed.jsp 호출 → TB_SHIPMENT 저장
3. 이마트 비정량 품목 바코드 스캔 (BARCODE_TYPE=M8, ITEM_TYPE=HW)
4. 계근 완료 후 라벨 출력 단계 진입
5. LabelPrintHelper.printLabel() → M8 case 실행
6. si.getEMARTLOGIS_CODE()가 빈 문자열("") 반환
7. pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) ... → StringIndexOutOfBoundsException 발생
8. 라벨 출력 없이 예외 처리 → 사용자 화면에 라벨 미출력
```

---

## 현상
- 이마트 비정량 품목 계근 완료 후 라벨이 출력되지 않음
- 사용자 PDA 화면에 아무 반응이 없음 (출력 성공/실패 메시지 없음)
- logcat에서 아래 패턴 확인:

```
04-29 11:59:29.224 D BixolonShipmentActivity: ===========이마트(비정량) 출력 시작 ================
04-29 11:59:29.225 D LabelPrintHelper: ITEM_TYPE : W
04-29 11:59:29.225 E LabelPrintHelper: ::::::::: M8 ::::::::
04-29 11:59:29.225 D LabelPrintHelper: 상품코드 full : 2492480000011, 6 : 249248
04-29 11:59:29.225 D LabelPrintHelper: 회사코드 : 610933
04-29 11:59:29.225 D LabelPrintHelper: 수입식별번호 : 202604230002
04-29 11:59:29.226 E BixolonShipmentActivity: setBarcodeMsg's BL 스캔 Exception -> length=0; index=6
```

- **핵심 현상**: `length=0; index=6` = 길이 0인 문자열에 `substring(0, 6)` 호출 → `StringIndexOutOfBoundsException`

## 원래부터 있던 버그인가?

**NO — MSSQL 전환 과정에서 발생한 신규 버그**

Oracle 원본 VIEW `VW_PDA_WID_LIST` L90에는 NULL 방어 처리가 있었다:

```sql
-- Oracle 원본 (VW_PDA_WID_LIST L90 — 이마트 정량)
DECODE(EO.EMARTLOGIS_CODE, NULL, '0000000', EO.EMARTLOGIS_CODE) AS EMARTLOGIS_CODE
```

MSSQL 전환 시 이 처리가 `COALESCE(M1.물류코드, M2.물류코드)` 형태로 대체되면서:
1. NULL → `'0000000'` default 처리 누락
2. 빈 문자열(`''`) 방어 처리 미포함

결과적으로 ERP `CO_매출처품목코드매핑.물류코드`에 비정량 품목의 값이 빈 문자열이면 PDA에 그대로 전달되어 예외 발생.

이마트 정량(searchType=0)은 ERP 데이터에 우연히 `'0000000'`이 들어있어 현재 회피 중이나 근본 원인은 동일.

## 원인

### 문제 1 (주요): JSP에서 EMARTLOGIS_CODE 빈 문자열 미방어

#### 코드 위치
- `search_production_nonfixed.jsp` : 60줄 (이마트 비정량, searchType=4)
- `search_shipment.jsp` : 60줄 (이마트 정량, searchType=0 — 잠재 위험)

#### 현재 문제 코드
```sql
-- search_production_nonfixed.jsp:60
", COALESCE(M1.물류코드, M2.물류코드) AS EMARTLOGIS_CODE"

-- search_shipment.jsp:60
", COALESCE(M1.물류코드, M2.물류코드) AS EMARTLOGIS_CODE"
-- ★ COALESCE는 NULL만 처리, 빈 문자열('')은 그대로 통과
-- ★ Oracle 원본의 DECODE(~, NULL, '0000000', ~) 처리 누락
```

#### 발생 시나리오
ERP `CO_매출처품목코드매핑.물류코드`에 비정량 품목의 물류코드가 빈 문자열(`''`)로 등록된 경우,
`COALESCE(M1.물류코드, M2.물류코드)`는 빈 문자열을 NULL이 아닌 유효값으로 판단해 그대로 반환.
PDA가 수신한 `EMARTLOGIS_CODE = ""` 상태로 `TB_SHIPMENT`에 저장됨.

---

### 문제 2 (보조): LabelPrintHelper에서 EMARTLOGIS_CODE 빈 값 무방어 substring 호출

#### 코드 위치
- `LabelPrintHelper.java` : 518줄 (M0 case)
- `LabelPrintHelper.java` : 685줄 (M8 case — 실제 예외 발생)

#### 문제 코드
```java
// LabelPrintHelper.java:685 (M8 case)
pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode;
// ★ EMARTLOGIS_CODE가 빈 문자열("")이면 substring(0, 6) → StringIndexOutOfBoundsException

// LabelPrintHelper.java:518 (M0 case — 동일 패턴, 잠재 위험)
pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
// ★ 동일 패턴, 빈 문자열 유입 시 동일 예외 발생
```

## 상세 흐름

1. **출하대상 조회** (searchType=4)
   - `search_production_nonfixed.jsp` 호출
   - `COALESCE(M1.물류코드, M2.물류코드) AS EMARTLOGIS_CODE` 쿼리 실행
   - ERP 데이터에 비정량 품목 물류코드가 `''`인 경우 그대로 반환
   - 결과: `EMARTLOGIS_CODE = ""`가 PDA에 전달됨

2. **TB_SHIPMENT 저장** (ProgressDlgBarcodeSearch)
   - 수신 데이터 파싱 후 `TB_SHIPMENT`에 INSERT
   - `EMARTLOGIS_CODE = ""` 값이 로컬DB에 저장됨

3. **바코드 스캔** (BixolonShipmentActivity)
   - 품목 바코드 스캔 → `BARCODE_TYPE=M8`, `ITEM_TYPE=HW` 매칭
   - 라벨 출력 요청: `LabelPrintHelper.printLabel()` 호출

4. **라벨 출력 시도** (LabelPrintHelper)
   - `ITEM_TYPE = W` 분기 진입 (logcat 확인)
   - `BARCODE_TYPE = M8` case 진입
   - logcat: `::::::::: M8 ::::::::` 출력

5. **문제 발생 단계** (LabelPrintHelper:685)
   - `si.getEMARTLOGIS_CODE()` → `""` 반환
   - **경로 A (정량, 정상)**: `EMARTLOGIS_CODE = "0000000"` (7자리) → `substring(0, 6)` = `"000000"` → 정상 동작
   - **경로 B (비정량, 예외)**: `EMARTLOGIS_CODE = ""` (0자리) → `substring(0, 6)` → `StringIndexOutOfBoundsException`
   - Exception이 `setBarcodeMsg`로 전파 → logcat: `Exception -> length=0; index=6`
   - 라벨 출력 중단, 사용자 화면에 미출력

## 영향 범위

| 구분 | 파일 | 위치 | 발생 여부 |
|------|------|------|:---------:|
| 이마트 비정량 (searchType=4) | `search_production_nonfixed.jsp` | 60줄 SELECT | 즉시 발생 |
| 이마트 비정량 (searchType=4) | `LabelPrintHelper.java` | 685줄 M8 case | 즉시 발생 |
| 이마트 정량 (searchType=0) | `search_shipment.jsp` | 60줄 SELECT | 잠재 위험 (현재 ERP 데이터에 `'0000000'` 우연히 존재) |
| 이마트 정량 (searchType=0) | `LabelPrintHelper.java` | 518줄 M0 case | 잠재 위험 |

- ERP `CO_매출처품목코드매핑.물류코드` 컬럼에 빈 문자열 또는 NULL이 들어있는 모든 품목에서 재발 가능
- searchType=4 비정량 전체 출력 불가 상태

## 수정 방안

### 수정 1: JSP에서 NULLIF + ISNULL 조합으로 NULL과 빈 문자열 모두 방어 (권장)

```sql
-- search_production_nonfixed.jsp:60 수정
", ISNULL(NULLIF(COALESCE(M1.물류코드, M2.물류코드), ''), '0000000') AS EMARTLOGIS_CODE"

-- search_shipment.jsp:60 수정 (정량도 동일 적용)
", ISNULL(NULLIF(COALESCE(M1.물류코드, M2.물류코드), ''), '0000000') AS EMARTLOGIS_CODE"
```

- `NULLIF(값, '')`: 빈 문자열을 NULL로 변환
- `ISNULL(..., '0000000')`: NULL이면 `'0000000'` default 적용
- Oracle 원본 `DECODE(~, NULL, '0000000', ~)` 동작과 동일

---

### 수정 2: LabelPrintHelper 내부 방어 처리 추가 (보완책)

```java
// LabelPrintHelper.java:685 M8 case 수정
String logisCode = si.getEMARTLOGIS_CODE();
if (logisCode == null || logisCode.length() < 6) {
    logisCode = "0000000"; // Oracle 원본 DECODE 동작과 동일
}
pBarcode2 = logisCode.substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode;
pBarcodeStr2 = logisCode.substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

// LabelPrintHelper.java:518 M0 case 동일 패턴 적용
String logisCode = si.getEMARTLOGIS_CODE();
if (logisCode == null || logisCode.length() < 6) {
    logisCode = "0000000";
}
pBarcode2 = logisCode.substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
pBarcodeStr2 = logisCode.substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
```

---

### 수정 3: ERP 데이터 직접 입력 (별도 ERP 작업 필요)

- ERP `CO_매출처품목코드매핑` 테이블에서 비정량 품목의 `물류코드` 컬럼에 정확한 값 입력
- JSP/앱 코드 수정 없이 해결 가능하나, 데이터 관리 의존적 방법

> 권장 순서: **수정 1(JSP) + 수정 2(Java)** 병행 — JSP에서 원천 차단, Java에서 이중 방어. Oracle 원본 DDL의 default 처리 의도를 완전히 복원.

---

## 최종 결정 — 코드 수정 없이 ERP 데이터 입력으로 해결 (2026-04-29)

### 결정 사유

물류코드 미입력은 **ERP 운영자의 휴먼 에러**로 판단. 코드에서 default 처리하면 다음과 같은 부작용 발생:

- 운영자가 물류코드 누락 사실을 인지하지 못함 (조용히 `'0000000'` default로 처리됨)
- 물류 시스템 연계 시 기준 데이터 부정확
- 향후 신규 품목 등록 시에도 동일 휴먼 에러 반복 가능

→ **코드에서 default 처리 ❌, ERP 입력 단계에서 필수 검증 ✅** 방향으로 결정.

### 해결 조치

| 조치 | 내용 | 처리 시점 |
|---|---|---|
| ERP 데이터 입력 | `CO_매출처품목코드매핑.물류코드` 컬럼에 비정량 품목(`2110100001`, `2110100002`)의 물류코드 직접 입력 | 2026-04-29 |
| JSP 수정 | **하지 않음** (의도적) | - |
| Java 수정 | **하지 않음** (의도적) | - |

### 운영 정책

- ERP `CO_매출처품목코드매핑` 등록 시 **물류코드 필수 입력** 정책 적용
- 향후 동일 예외 발생 시 → 휴먼 에러로 처리 (ERP 데이터 확인·입력)

## 상태
- [x] **해결됨 (ERP 물류코드 입력, 2026-04-29) — 코드 수정 없음**

## 관련 문서
- `app/doc/문의사항/03_M8_비정량_이마트_pBarcode2_생성_미출력_의도.md` — M8 case pBarcode2 처리 의도 문의
- `app/doc/소스분석/49_출하대상받기_4유형_조회조건_종합정리.md` — 이마트 정량 MSSQL WHERE 절 (1.6절)
- `app/doc/view/VW_PDA_WID_LIST` — 이마트 정량 Oracle 원본 DDL (L90: DECODE NULL→'0000000')
- `app/doc/참고자료/오류패턴_분석.md` — **패턴 C: 컬럼명/구조 변경** (Oracle DECODE→COALESCE 전환 시 default 처리 누락)
