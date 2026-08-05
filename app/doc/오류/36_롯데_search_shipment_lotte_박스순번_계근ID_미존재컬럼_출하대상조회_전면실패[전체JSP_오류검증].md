# 롯데 search_shipment_lotte.jsp 박스순번·계근ID 미존재 컬럼 참조로 출하대상 조회 전면 실패

**발견일**: 2026-08-05
**발견 경로**: 전체 searchType JSP 오류 검증 (JSP 직접 HTTP 호출)
**대상**: searchType = 6 (롯데)
**심각도**: **높음** — 롯데 출하대상받기 기능 전체 불가

---

## 1. 증상

롯데 출하대상받기 호출 시 JSP 가 **HTTP 500** 을 반환한다.

```
SQLServerException: 열 이름 '박스순번'이(가) 잘못되었습니다.
```

앱에서는 `ProgressDlgShipSearch` 가 빈 응답을 받아 출하대상이 0건이 되며,
롯데 계근 자체가 시작되지 않는다.

### 재현

```
POST http://183.101.157.205:4040/inno/search_shipment_lotte.jsp
data =  AND D.회사코드 = '20' AND D.출고일자 = '20260714' AND D.창고코드 = 'W301'
→ HTTP 500 (0.10s)
```

조회일자·창고코드와 무관하게 항상 실패한다. 데이터 유무 문제가 아니라 **컬럼 미존재로 인한 구문 오류**다.

---

## 2. 원인

`search_shipment_lotte.jsp` L63~67 의 `LAST_BOX_ORDER` 서브쿼리가
`SM_출고계근` 에 없는 컬럼 2개를 참조한다.

```sql
, (SELECT TOP 1 W.박스순번
     FROM SM_출고계근 W
    WHERE W.출고상세SEQ = D.SEQ
      AND W.박스순번 IS NOT NULL
    ORDER BY W.계근ID DESC) AS LAST_BOX_ORDER
```

### `SM_출고계근` 실제 컬럼 (18개)

```
SEQ(bigint) | 회사코드 | 등록일자 | 등록사원 | 등록시간 | 수정일자 | 수정사원 | 수정시간
박스시리얼 | 출고상세SEQ(bigint) | 제조일자 | 패커코드 | PPCODE | 계근바코드
계근순번(int) | 계근중량단위 | 계근중량(float) | 출고LOTSEQ(bigint)
```

| JSP 참조 | 존재 | 비고 |
|---|:--:|---|
| `W.박스순번` | ❌ | ERP 전체 테이블에 `박스순번` 컬럼 자체가 없음 |
| `W.계근ID` | ❌ | PK 는 `SEQ` |
| `W.출고상세SEQ` | ✅ | |

ERP 내 `박스%` 컬럼 34개를 조회했으나 `박스순번` 은 없고, 유사한 것은 `박스시리얼`·`박스수량`·`박스입수` 등 성격이 다른 컬럼뿐이다.

---

## 3. 원본 동작

### Oracle VIEW `VW_PDA_WID_LIST_LOTTE` L73~83

```sql
NVL((SELECT curr_order
     FROM ( SELECT NVL(W.BOX_ORDER, 0) AS curr_order,
                   LEAD(W.BOX_ORDER) OVER (ORDER BY W.GOODS_WET_ID DESC) AS next_order
            FROM W_GOODS_WET W
            WHERE W.GI_D_ID IN (SELECT D.GI_D_ID FROM W_GOODS_ID D, W_MART_ORDER_ITEM L WHERE D.EOI_ID = L.EOI_ID)
              AND W.BOX_ORDER IS NOT NULL
              AND W.PACKER_PRODUCT_CODE = BD.PACKER_PRODUCT_CODE ) t
     WHERE (t.curr_order = t.next_order + 1) OR (t.curr_order = 1 AND t.next_order = 9999)
     FETCH FIRST 1 ROW ONLY)
   , 0) AS LAST_BOX_ORDER
```

원본은 단순히 "마지막 값"을 가져오는 것이 **아니다**.

| 요소 | 의미 |
|---|---|
| `W_GOODS_WET.BOX_ORDER` | 계근 데이터의 박스 순번 (1~9999) |
| `LEAD(...) OVER (ORDER BY GOODS_WET_ID DESC)` | 최근 → 과거 순으로 다음 행의 순번을 붙임 |
| `curr_order = next_order + 1` | 두 순번이 **연속**인지 검사 |
| `curr_order = 1 AND next_order = 9999` | **9999 → 1 순환** 지점 검사 |
| `FETCH FIRST 1 ROW ONLY` | 연속성이 확인된 첫 행 = 정상적인 마지막 순번 |
| `NVL(…, 0)` | 없으면 0 |
| 필터 | `PACKER_PRODUCT_CODE` 일치 건만 |

즉 **박스 순번의 연속성을 검증해 신뢰할 수 있는 마지막 순번을 뽑는 로직**이다.
VIEW 주석에도 "최근 데이터부터 박스 순번을 순차적으로 검사하여 정상적인 마지막 박스순번을 추출" 로 명시되어 있다.

### 앱 사용처

| 파일 | 위치 | 내용 |
|---|---|---|
| `ProgressDlgShipSearch.java` | :305 | `si.setLAST_BOX_ORDER(temp[25])` — 롯데(6) 전용 파싱 |
| `BixolonShipmentActivity.java` | :277 | `private int lotte_TryCount` |
| `BixolonShipmentActivity.java` | :1923~1930 | 계근 시 `lotteBoxOrder = lotte_TryCount` 확정 후 증가, `> 9999` 면 1 로 순환 |
| 원본 `ShipmentActivity.java` | :2653~2654 | `lotte_TryCount = Integer.parseInt(si.LAST_BOX_ORDER) + 1;` |

`LAST_BOX_ORDER + 1` 이 이번 계근 세션의 시작 박스 순번이 된다. 롯데 라벨의 박스 일련번호로 출력되는 값이다.

관련 분석: `app/doc/column/02_VW_PDA_WID_LIST_LOTTE.md` §LAST_BOX_ORDER — "제거 시 박스 순번 연속성 보장 불가", 비즈니스 영향 **있음(핵심)** 판정.

---

## 4. 현재 JSP 와 원본의 차이

| 항목 | 원본 Oracle | 현재 MSSQL | 판정 |
|---|---|---|:--:|
| 소스 컬럼 | `W_GOODS_WET.BOX_ORDER` | `SM_출고계근.박스순번` | ❌ 미존재 |
| 정렬 키 | `GOODS_WET_ID DESC` | `계근ID DESC` | ❌ 미존재 |
| 매칭 조건 | `PACKER_PRODUCT_CODE` 일치 | `출고상세SEQ = D.SEQ` | ⚠ 범위 상이 |
| 연속성 검사 | `LEAD` + 연속/순환 판정 | **없음** (`TOP 1` 만) | ⚠ 로직 누락 |
| 기본값 | `NVL(…, 0)` | 없음 (NULL 가능) | ⚠ |

컬럼 미존재가 1차 원인이고, **연속성 검증 로직 자체가 전환되지 않은 것**이 2차 문제다.
컬럼명만 바꿔도 원본과 동일하게 동작하지 않는다.

---

## 5. 추가 확인 — 롯데 전송 경로 부재

`LAST_BOX_ORDER` 는 계근 데이터의 박스 순번을 되읽는 값이다. 그런데 **박스 순번을 서버에 보내는 경로 자체가 없다.**

### 5.1 앱은 패킷에 담아 보낸다

`BixolonShipmentActivity.java:3034`
```java
packet += list_send_info.get(i).getBOX_ORDER() + "::" + list_send_info.get(i).getGI_L_ID() + "##";
```

### 5.2 그러나 롯데 전송 분기가 없다

`BixolonShipmentActivity.java:3056~3070`

| searchType | 전송 URL |
|:--:|---|
| 0 이마트 | `URL_INSERT_GOODS_WET` |
| 2 홈플러스 | `URL_INSERT_GOODS_WET` |
| 1 생산 / 7 생산라벨 | `URL_INSERT_GOODS_WET_PRODUCTION` |
| 4 비정량 / 5 홈플러스비정량 | `URL_INSERT_GOODS_WET_NEW` |
| 3 도매 | `URL_INSERT_GOODS_WET_NEW` |
| **6 롯데** | **분기 없음 → 전송 미실행** |

`if / else if` 체인에 `SEARCH_TYPE_LOTTE` 분기가 없어, 롯데는 어떤 JSP 도 호출되지 않고 `result` 가 미할당 상태로 넘어간다.

### 5.3 서버 측에도 저장 컬럼이 없다

`insert_goods_wet.jsp:59`·`insert_goods_wet_new.jsp:63` 은 `계근순번`(= `BOX_CNT`, `splitData[8]`) 만 INSERT 하며, `BOX_ORDER`(`splitData[13]`) 를 받는 컬럼이 없다. `SM_출고계근` 에도 대응 컬럼이 없다.

### 5.4 종합

```
앱 : lotte_TryCount 계산 → 라벨 인쇄 → 로컬 TB_GOODS_WET.BOX_ORDER 저장  ✅
     ↓ 전송
전송 : 롯데 분기 없음                                                    ❌
서버 : BOX_ORDER 수신·저장 컬럼 없음                                      ❌
     ↓ 재조회
JSP : SM_출고계근.박스순번 조회 → 컬럼 미존재로 500                        ❌
```

**롯데 박스 순번 파이프라인이 서버 구간 전체에서 미구현 상태**다. JSP 컬럼명만 고쳐도 저장된 값이 없어 항상 0 이 되고, 앱은 매번 1 부터 시작한다.

---

## 6. 미확정 사항

수정 전 확인이 필요하다.

| # | 항목 | 내용 |
|:-:|---|---|
| 1 | **`SM_출고계근` 박스순번 컬럼 신설 여부** | ERP 테이블 변경이 필요하다. 생산의 `PD_생산계근` 신설과 같은 절차 |
| 2 | 대체 컬럼 후보 | `계근순번(int)` 은 **대상 내 박스 일련번호**(1,2,3…)이고 롯데 `BOX_ORDER` 는 **1~9999 전역 순환**이라 의미가 다름. 전용 필요 |
| 3 | 전송 분기 추가 | `BixolonShipmentActivity` 에 `SEARCH_TYPE_LOTTE` 분기 및 전용 INSERT JSP 필요 |
| 4 | 연속성 로직 이식 | MSSQL `LEAD` 지원되므로 이식 가능. 컬럼 확정이 선행 |
| 5 | 매칭 범위 | 원본은 `PACKER_PRODUCT_CODE` 기준(전역), 현재는 `출고상세SEQ` 기준(대상별). 원본 의미상 전역이 맞음 |

**임의 치환 금지** — `박스순번` → `계근순번` 단순 치환 시 라벨 박스 일련번호가 원본과 달라진다.
롯데 라벨 규격·운영 정책 확인 후 결정해야 한다.

---

## 7. 영향 범위

| 대상 | 영향 |
|---|---|
| 롯데(6) 출하대상받기 | **전면 불가** — HTTP 500 |
| 롯데 계근·라벨 | 대상이 없어 진입 불가 |
| 타 searchType | **없음** — 각자 JSP 사용, 전수 검증에서 정상 확인 |

### 전체 JSP 검증 결과 (2026-08-05)

| searchType | JSP | 결과 |
|:--:|---|:--:|
| 0 이마트 | `search_shipment.jsp` | ✅ 2행 / 31컬럼 |
| 1 생산 | `search_production.jsp` | ✅ |
| 2 홈플러스 | `search_shipment_homeplus.jsp` | ✅ |
| 3 도매 | `search_shipment_wholesale.jsp` | ✅ |
| 4 비정량 | `search_production_nonfixed.jsp` | ✅ |
| 5 홈플러스비정량 | `search_homeplus_nonfixed.jsp` | ✅ |
| **6 롯데** | `search_shipment_lotte.jsp` | ❌ **본 오류** |
| — | `search_barcode_info.jsp` | ✅ 25컬럼 |
| — | `search_barcode_info_nonfixed.jsp` | ✅ 24컬럼 |
| — | `search_goods_wet.jsp` | ✅ |
| — | `search_goods_wet_production.jsp` | ✅ |
| — | `search_warehouse.jsp` | ✅ |

`search_production_calc.jsp` 는 MSSQL 미전환 상태로 별도 관리(검증 제외).

---

## 8. 오류 패턴 분류

**미존재 컬럼 참조** — 오류 32·33·35(홈플러스)와 동일 계열.
Oracle VIEW 를 MSSQL 직접 쿼리로 전환하면서 원본 컬럼명을 ERP 테이블에 매핑하지 않고 임의 한글명으로 적은 사례다.

`app/doc/참고자료/오류패턴_분석.md` 갱신 대상.

---

## 9. 관련 문서

- `app/doc/column/02_VW_PDA_WID_LIST_LOTTE.md` — `LAST_BOX_ORDER` 컬럼 분석 (§1468~1510)
- `app/doc/view/VW_PDA_WID_LIST_LOTTE` — Oracle 원본 VIEW 정의 (L73~83)
- `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-7.0.78_PDA_IN(원본)\webapps\ROOT\inno\search_shipment_lotte.jsp` — 원본 JSP (L71·L95)
- `app/doc/참고자료/오류패턴_분석.md` — 오류 패턴 분류

---

**문서 버전**: 1.0
