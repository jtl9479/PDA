# 홈플러스정량 계근데이터전송 JSP MSSQL 전환

**작성일**: 2026-04-30
**목적**: 홈플러스 정량(searchType=2) 및 롯데(searchType=6) 계근 완료 데이터 전송 시 호출하는 JSP를 `insert_goods_wet_homeplus.jsp`(Oracle 쿼리, MSSQL 동작 불가)에서 이미 MSSQL 전환 완료된 `insert_goods_wet.jsp`로 변경한다. packet 구조가 동일하므로 `BixolonShipmentActivity.java`의 URL 2곳만 변경하면 된다.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### insert_goods_wet_homeplus.jsp (현재 — 동작 불가)

**경로**: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_homeplus.jsp`

```jsp
// DB 접속: getMSSQLConnection() — 이미 MSSQL 접속
// INSERT 쿼리: Oracle W_GOODS_WET + W_GOODS_WET_SEQ.NEXTVAL — MSSQL에 없음 → 실행 오류
INSERT INTO W_GOODS_WET(GOODS_WET_ID, ...) VALUES (W_GOODS_WET_SEQ.NEXTVAL, ?, ...)
```

### BixolonShipmentActivity.java (현재 — URL 분기)

```java
// 구 로직 L2649~2652
if(searchType == EMART || searchType == WHOLESALE) {
    result = HttpHelper.sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);
} else if(searchType == HOMEPLUS || searchType == LOTTE) {
    result = HttpHelper.sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS); // ← 변경 대상
}

// 신 로직 L2741~2742
} else if(searchType == HOMEPLUS) {
    result = HttpHelper.sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS); // ← 변경 대상
}
```

### packet 구조 (공통 — searchType 무관)

L2625~2639에서 searchType 구분 없이 동일한 15개 필드를 `::` 구분자로 조립:

| splitData[] | 필드 |
|:-----------:|------|
| [0] | GI_D_ID |
| [1] | WEIGHT |
| [2] | WEIGHT_UNIT |
| [3] | PACKER_PRODUCT_CODE |
| [4] | BARCODE |
| [5] | PACKER_CLIENT_CODE |
| [6] | MAKINGDATE |
| [7] | BOXSERIAL |
| [8] | BOX_CNT |
| [9] | REG_ID |
| [10] | Common.selectCompanyCode (회사코드) |
| [11] | BRAND_CODE |
| [12] | CLIENT_TYPE |
| [13] | BOX_ORDER |
| [14] | GI_L_ID |

### 문제점

1. **`insert_goods_wet_homeplus.jsp` 동작 불가** — MSSQL 접속 후 Oracle 전용 `W_GOODS_WET` 테이블 참조 → 실행 즉시 SQL 오류
2. **이마트(`insert_goods_wet.jsp`)는 이미 MSSQL 전환 완료** — SM_출고계근 INSERT, NEXT VALUE FOR SM_DLIVY_WEIGH_SEQ, 18컬럼, 17파라미터
3. **packet 구조 동일** — 이마트와 홈플러스/롯데 모두 동일한 15개 필드 조립 → URL만 변경하면 재사용 가능

---

## 2. 변경 구조

### 데이터 흐름

```
[변경 전]
BixolonShipmentActivity (searchType=2/6)
    ↓ URL_INSERT_GOODS_WET_HOMEPLUS
insert_goods_wet_homeplus.jsp (Oracle W_GOODS_WET — 동작 불가)

[변경 후]
BixolonShipmentActivity (searchType=2/6)
    ↓ URL_INSERT_GOODS_WET  ← URL만 변경
insert_goods_wet.jsp (MSSQL SM_출고계근 — 이미 완료)
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **BixolonShipmentActivity.java** | L2652, L2742 | `URL_INSERT_GOODS_WET_HOMEPLUS` → `URL_INSERT_GOODS_WET` 2곳 변경 |

**JSP 수정 없음**: `insert_goods_wet.jsp`는 이미 MSSQL 전환 완료 상태이며, packet 구조가 동일하므로 별도 수정 불필요.

---

## 4. 수정 상세

### 4.1 BixolonShipmentActivity.java

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`

**변경 전:**

```java
// L2651~2652 (구 로직)
} else if(Common.searchType.equals(SEARCH_TYPE_HOMEPLUS) || Common.searchType.equals(SEARCH_TYPE_LOTTE)) {
    result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS);

// L2741~2742 (신 로직)
} else if(Common.searchType.equals(SEARCH_TYPE_HOMEPLUS)) {
    result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS);
```

**변경 후:**

```java
// L2651~2652 (구 로직)
} else if(Common.searchType.equals(SEARCH_TYPE_HOMEPLUS) || Common.searchType.equals(SEARCH_TYPE_LOTTE)) {
    result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);

// L2741~2742 (신 로직)
} else if(Common.searchType.equals(SEARCH_TYPE_HOMEPLUS)) {
    result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);
```

---

## 5. 사이드이펙트

- **searchType=0(이마트)**: 이미 `URL_INSERT_GOODS_WET` 사용 중 → 영향 없음
- **searchType=6(롯데)**: 구 로직 L2652에서 홈플러스와 같이 처리 → 동일하게 변경됨
- **searchType=4/5(비정량)**: `URL_INSERT_GOODS_WET_NEW` 사용 → 영향 없음
- **insert_goods_wet_homeplus.jsp**: 더 이상 호출되지 않으나 파일 삭제는 하지 않음

---

## 6. 호출 시점

```
[BixolonShipmentActivity — 계근 완료 후 전송 버튼 클릭]
    ↓
[구 로직 분기 — L2620~2696]
searchType=2 또는 searchType=6
    ↓ 건별 packet 조립 (15개 필드, :: 구분자)
    ↓ HttpHelper.sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET)
    ↓ insert_goods_wet.jsp 호출 (단건 처리)
    ↓ 응답 "s" → TB_GOODS_WET.SAVE_TYPE F→Y UPDATE

[신 로직 배치 분기 — L2741~2742]
searchType=2
    ↓ HttpHelper.sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET)
    ↓ insert_goods_wet.jsp 호출
```

---

## 7. 개발 플랜

### Step 1: BixolonShipmentActivity.java URL 변경

**Part 1. 분석**
- 대상: `BixolonShipmentActivity.java` L2652, L2742
- 범위: `URL_INSERT_GOODS_WET_HOMEPLUS` → `URL_INSERT_GOODS_WET` 2곳

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 구 로직 URL | L2652 | HOMEPLUS/LOTTE 분기 URL 변경 |
| 2 | 신 로직 URL | L2742 | HOMEPLUS 분기 URL 변경 |

**Part 2. 변환 계획**
- 변환 방식: L2652, L2742 두 곳의 `URL_INSERT_GOODS_WET_HOMEPLUS` → `URL_INSERT_GOODS_WET` 로 교체
- 주의사항: 다른 searchType 분기(이마트, 생산, 비정량, 도매)는 건드리지 않음

**체크리스트**
- [x] Part 1: L2652 현재 코드 확인 완료
- [x] Part 1: L2742 현재 코드 확인 완료
- [x] Part 3: L2652 URL 변경 완료
- [x] Part 3: L2742 URL 변경 완료
- [ ] Part 4: 컴파일 오류 없음 확인
- [x] Part 6: 변경 내용 작성

**Part 6. 변경 내용**:
- **무엇을**: `BixolonShipmentActivity.java` L2652, L2742의 `URL_INSERT_GOODS_WET_HOMEPLUS` → `URL_INSERT_GOODS_WET` 2곳 변경
- **왜**: `insert_goods_wet_homeplus.jsp`는 MSSQL 접속 후 Oracle 전용 `W_GOODS_WET` 테이블 참조로 실행 불가 상태이며, `insert_goods_wet.jsp`(이마트용)가 이미 MSSQL 전환 완료(SM_출고계근) 상태이고 packet 구조가 동일(15개 필드, :: 구분자)하므로 URL만 변경으로 재사용 가능
- **어떻게**: 구 로직(L2652) HOMEPLUS||LOTTE 분기, 신 로직(L2742) HOMEPLUS 분기 각 1곳씩 URL 상수 교체

---

### Step 2: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | searchType=2(홈플러스 정량) 계근 전송 실행 → 응답 "s" 수신 | □ |
| 2 | SM_출고계근 테이블에 행 INSERT 확인 | □ |
| 3 | INSERT된 행의 출고상세SEQ, 출고LOTSEQ, 계근중량, 회사코드 값 검증 | □ |
| 4 | searchType=6(롯데) 계근 전송 정상 동작 확인 | □ |
| 5 | searchType=0(이마트) 계근 전송 사이드이펙트 없음 확인 | □ |

---

### 개발 순서 요약

```
Step 1: BixolonShipmentActivity.java URL 2곳 변경
    ↓
Step 2: 통합 테스트
```

---

## 8. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | BixolonShipmentActivity.java URL 변경 | ✅ 완료 |
| 2 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/소스분석/54_홈플러스_계근데이터전송_JSP_MSSQL전환전_구조분석.md` — 계근 전송 JSP 구조 분석
- `app/doc/소스분석/56_홈플러스정량_출하_전체흐름분석.md` — 전체 흐름 분석
- JSP 재사용: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet.jsp`
- JSP 기존: `D:\PDA\apache-tomcat-8.5.29\apache-tomcat-8.5.29\webapps\ROOT\inno\insert_goods_wet_homeplus.jsp`
- Java: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`

---

**문서 버전**: 2.0 (방향 변경: JSP 수정 → Java URL 변경으로 전환)
