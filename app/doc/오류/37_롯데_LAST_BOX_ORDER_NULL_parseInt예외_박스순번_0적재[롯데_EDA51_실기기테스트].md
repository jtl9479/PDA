# 롯데 `LAST_BOX_ORDER` NULL → `parseInt` 예외 → 박스순번 0 적재

**발견일**: 2026-09-28
**발견 경로**: EDA51 실기기 테스트 (롯데 searchType=6, 계근일 2026-08-05)
**심각도**: 🔴 높음 — 롯데 박스순번이 **영원히 0**으로 적재되어 라벨 시퀀스가 동작하지 않음
**상태**: 미조치

---

## 1. 증상

롯데 첫 계근 시 `TB_GOODS_WET.BOX_ORDER` 가 `1` 이 아닌 **`0`** 으로 적재된다.

```
DBHandler: insertqueryGoodsWetLotte -> INSERT INTO TB_GOODS_WET
  (GI_D_ID, GI_L_ID, WEIGHT, WEIGHT_UNIT, PACKER_PRODUCT_CODE, BARCODE,
   PACKER_CLIENT_CODE, MAKINGDATE, BOXSERIAL, BOX_CNT, EMARTITEM_CODE,
   EMARTITEM, ITEM_CODE, BRAND_CODE, REG_ID, REG_DATE, REG_TIME,
   SAVE_TYPE, DUPLICATE, CLIENT_TYPE, BOX_ORDER)
  VALUES('270','','6.0','CT','1110100681','00600202608051110100681','',
         '20260805','',1,'8809790366394','돼지고기캠핑모둠팩(삼겹살/목심/항정살)',
         '1110100681','','12345678','20260928','104102','F','F','07','0')
                                                                      ↑ 박스순번 0
```

로컬DB 확인

```
TB_GOODS_WET 1행
  GI_D_ID=270 WEIGHT=6.0 CT BOX_CNT=1 BOX_ORDER=0 MAKINGDATE=20260805 SAVE_TYPE=F
```

**앱은 죽지 않는다.** 예외가 상위 `catch` 에 삼켜져 조용히 실패한다.

---

## 2. 원인

### 2.1 예외 발생 지점

`BixolonShipmentActivity.java:2603~2606`

```java
// 롯데의 경우만 lotte_TryCount 사용, 초기화 후 현재 찍힌 수량 더해서 전역변수로 만들기.
if(Common.searchType.equals(Common.SEARCH_TYPE_LOTTE)) {

    Shipments_Info si = arSM.get(0);
    lotte_TryCount = Integer.parseInt(si.LAST_BOX_ORDER) + 1;   // ★ LAST_BOX_ORDER = "" → 예외
    ...
}
```

`si.LAST_BOX_ORDER` 가 **빈 문자열**이라 `Integer.parseInt("")` 가 `NumberFormatException` 을 던진다.

logcat

```
BixolonShipmentActivity: result's Count : 3
BixolonShipmentActivity: e : java.lang.NumberFormatException: For input string: ""
```

### 2.2 예외가 삼켜지는 구조

`BixolonShipmentActivity.java:2620~2625`

```java
} catch (Exception e) {
    if (Common.D) {
        Log.e(TAG, "e : " + e.toString());
    }
}
```

`doInBackground` 전체를 감싸는 `try/catch` 라서 **채번 블록 전체가 건너뛰어진다.**
→ `lotte_TryCount` 가 필드 초기값 `0` 에 머문다.
→ 이후 계근마다 `insertqueryGoodsWetLotte(this, gi, lotte_TryCount)` 에 `0` 이 전달된다.

### 2.3 빈 문자열이 되는 경로

| 단계 | 값 |
|------|-----|
| MSSQL 서브쿼리 (계근 이력 없음) | `NULL` |
| JSP `rs.getString("LAST_BOX_ORDER")` | `null` (Java null) → `out.println` 시 문자열 `"null"` |
| 앱 수신 `receiveData` | `...::1110100681::20260805::::::null` |
| `ProgressDlgShipSearch:305` `si.setLAST_BOX_ORDER(temp[25])` | `"null"` |
| `DBHandler` `Common.nullCheck(..., "")` | **`""`** |
| `Integer.parseInt("")` | **예외** |

---

## 3. 원본 대비 — 앱 코드는 동일, JSP 전환 누락

**앱 코드는 원본과 글자 단위로 같다.**

`PDA-INNO(원본)/ShipmentActivity.java:2969`

```java
lotte_TryCount = Integer.parseInt(si.LAST_BOX_ORDER) + 1;
```

즉 앱 버그가 아니라 **MSSQL 전환 시 NULL 처리 누락**이다.

| | 원본 (Oracle) | 현재 (MSSQL) |
|---|---|---|
| 출처 | `VW_PDA_WID_LIST_LOTTE` 뷰의 `LAST_BOX_ORDER` 컬럼 | `search_shipment_lotte.jsp` 내 서브쿼리 |
| 계근 이력 없을 때 | 뷰에서 `0` 반환 (앱이 `parseInt` 하는 전제) | **`NULL`** |

원본 JSP(`apache-tomcat-7.0.78_PDA_IN(원본)/inno/search_shipment_lotte.jsp:71`)는 뷰 컬럼을 그대로 `SELECT` 만 한다. NULL 방어는 **뷰 안에** 있었고, 전환하면서 서브쿼리로 풀어쓸 때 그 방어가 사라졌다.

---

## 4. 영향 범위

| 대상 | 영향 |
|------|------|
| 롯데(6) 박스순번 | **전량 0 적재.** 1~9999 순환 채번이 전혀 동작하지 않음 |
| 롯데 라벨 바코드 | 박스순번이 라벨 시퀀스에 반영되지 않음 |
| `insert_goods_wet_lotte.jsp`(개발63 신설) | 정상 동작하나 **받는 값이 0** 이라 `SM_출고계근.박스순번` 에 0 적재 |
| `LAST_BOX_ORDER` 재조회 | 0 이 적재되므로 다음 계근도 `0+1=1` 로 시작 → **순번이 계속 1 부근에 머묾** |
| 다른 마트사(0·2·3·4·5) | **영향 없음.** `searchType == 6` 분기 내부 |

> 개발63(롯데 박스순번 파이프라인 복구)이 **이 건 때문에 완결되지 않는다.**

---

## 5. 조치안

### 권장 — JSP 1곳 수정, 앱 무수정

`search_shipment_lotte.jsp:63~67`

**변경 전**
```sql
+ ", (SELECT TOP 1 W.박스순번"
+ "    FROM SM_출고계근 W"
+ "    WHERE W.출고상세SEQ = D.SEQ"
+ "      AND W.박스순번 IS NOT NULL"
+ "    ORDER BY W.SEQ DESC) AS LAST_BOX_ORDER"
```

**변경 후**
```sql
+ ", COALESCE((SELECT TOP 1 W.박스순번"
+ "            FROM SM_출고계근 W"
+ "            WHERE W.출고상세SEQ = D.SEQ"
+ "              AND W.박스순번 IS NOT NULL"
+ "            ORDER BY W.SEQ DESC), 0) AS LAST_BOX_ORDER"
```

**근거**
- 원본 Oracle 뷰가 `0` 을 반환하던 동작을 복원하는 것이므로 **"기존 기능 100% 동일"에 부합**
- 앱 코드를 건드리지 않음 (기존 조건 변경 금지 원칙 유지)
- 전환된 다른 JSP들이 이미 `COALESCE` 를 쓰고 있어 패턴 일치
  (`search_shipment.jsp:46` `COALESCE(NULLIF(V.BLNO, ''), V.이력번호)` 등)

**결과**: `parseInt("0") + 1 = 1` → 첫 계근 박스순번 **1** 로 정상 채번

### 비권장 — 앱 방어 코드 추가

`Common.nullCheck(si.LAST_BOX_ORDER, "0")` 로 바꾸는 방법도 있으나, 원본에 없는 코드를 앱에 추가하는 것이라 **원본 동일성 원칙에 어긋난다.** JSP 수정으로 해결 가능하므로 채택하지 않는다.

---

## 6. 재현 절차

```
1. PDA 로그인 (부산센터 / 12345678)
2. 날짜설정 → 2026년 8월 5일
3. [롯데 출하대상받기]  → 5건 수신
4. [(롯데출하) 계근입력시작]
5. 바코드 스캔 1차 : 00600202608051110100681   (상품 식별)
6. 바코드 스캔 2차 : 00600202608051110100681   (중량 추출 + 계근)
7. logcat 확인
     e : java.lang.NumberFormatException: For input string: ""
     insertqueryGoodsWetLotte -> ... ,'0')
8. 로컬DB 확인
     SELECT BOX_ORDER FROM TB_GOODS_WET   → 0
```

**전제**: 해당 `출고상세SEQ` 에 박스순번이 적재된 계근 이력이 없을 것 (첫 계근)

---

## 7. 증빙

`app/doc/테스트/증빙/2026-09-28_롯데_EDA51/`

| 파일 | 내용 |
|------|------|
| `s06_lotte_recv.png` | 롯데 출하대상 5건 수신 |
| `s07_lotte_weigh.png` | 계근 화면 진입 |
| `s08_scan1.png` | 1차 스캔 — 상품 식별 |
| `s09_scan2.png` | 2차 스캔 — 계근 반영 (18/1, 108.0/6.0) |
| `HIGHLAND_조회직후.db` | `TB_SHIPMENT` 5행, `LAST_BOX_ORDER=''` |
| `HIGHLAND_계근후.db` | `TB_GOODS_WET` 1행, `BOX_ORDER=0` |

---

## 8. 관련 문서

| 폴더 | 문서 | 관계 |
|------|------|------|
| 개발/ | `63_롯데_박스순번_파이프라인_복구[36].md` | **Step 4 완결을 막는 건** |
| 오류/ | `36_롯데_search_shipment_lotte_박스순번_계근ID_미존재컬럼_출하대상조회_전면실패[전체JSP_오류검증].md` | 같은 JSP의 선행 오류 |
| 테스트/ | `PDA테스트문서.xlsx` — `5.계근-롯데` 시트 | `LT-` TC |
| 테스트/ | `증빙/2026-09-28_롯데_EDA51/` | 본 건 증빙 |
