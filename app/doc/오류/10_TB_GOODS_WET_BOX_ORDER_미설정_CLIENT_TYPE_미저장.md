# TB_GOODS_WET BOX_ORDER 미설정 및 CLIENT_TYPE 미저장

## 발견일
2026-04-06

## 현상
- wet_data_insert()에서 Goodswets_Info 객체에 `setBOX_ORDER()` 호출하지 않음
- insertqueryGoodsWet()에서 BOX_ORDER 컬럼에 빈값("") 저장
- insertqueryGoodsWet()에서 CLIENT_TYPE 컬럼이 INSERT 목록에 없음 (SQLite에 NULL)
- **이마트(searchType=0)에서는 두 필드 모두 사용하지 않으므로 실제 영향 없음**

## 원래부터 있던 버그인가?

**YES - 원본(PDA-INNO(원본))에서도 동일한 구조**

```java
// 원본 ShipmentActivity.java wet_data_insert()
// setBOX_ORDER() 호출 없음 (이마트/생산/도매 경로)
// 원본 DBHandler.java insertqueryGoodsWet()
// CLIENT_TYPE 컬럼 미포함 (이마트/생산/도매용 INSERT)
```

## 원인

### 문제 1: BOX_ORDER set 안함

#### 코드 위치
- `BixolonShipmentActivity.java` : 1612~1628줄

#### 현재 문제 코드
```java
// BixolonShipmentActivity.java:1612~1628
Goodswets_Info gi = new Goodswets_Info();
gi.setGI_D_ID(...);
gi.setWEIGHT(...);
// ... 14개 필드 set ...
gi.setSAVE_TYPE("F");
gi.setDUPLICATE("F");
// ★ gi.setBOX_ORDER() 호출 없음
```

#### INSERT에서 사용
```java
// DBHandler.java:1402
+ Common.nullCheck(gi.getBOX_ORDER(), "") + "','"
// ★ getBOX_ORDER()는 빈값("") 반환 → 빈 문자열로 저장
```

### 문제 2: CLIENT_TYPE INSERT 미포함

#### 코드 위치
- `DBHandler.java` : 1354~1421줄

#### 현재 문제 코드
```java
// DBHandler.java:1364~1384 - insertqueryGoodsWet() INSERT 컬럼 목록
+ DBInfo.GI_D_ID + ", "
+ DBInfo.WEIGHT + ", "
// ... 중간 생략 ...
+ DBInfo.SAVE_TYPE + ", "
+ DBInfo.DUPLICATE
// ★ CLIENT_TYPE 컬럼 없음
```

#### 홈플러스/롯데에서는 정상
```java
// DBHandler.java:insertqueryGoodsWetHomeplus() - 홈플러스용
+ "06" + "','"  // CLIENT_TYPE = '06' 하드코딩

// DBHandler.java:insertqueryGoodsWetLotte() - 롯데용
+ "07" + "','"  // CLIENT_TYPE = '07' 하드코딩
```

## 상세 흐름

1. **이마트 계근** (searchType=0)
   - wet_data_insert() → BOX_ORDER set 안함
   - DBHandler.insertqueryGoodsWet() 호출
   - BOX_ORDER = "" (빈값), CLIENT_TYPE = NULL

2. **서버 전송 시**
   - selectquerySendGoodsWet() → BOX_ORDER = "", CLIENT_TYPE = ""
   - packet[12] = getCLIENT_TYPE() = "" (빈값)
   - packet[13] = getBOX_ORDER() = "" (빈값)
   - **insert_goods_wet.jsp에서 splitData[12], [13]은 사용하지 않으므로 영향 없음**

## 영향 범위

| searchType | BOX_ORDER | CLIENT_TYPE | 영향 |
|:----------:|:---------:|:-----------:|:----:|
| 0 (이마트) | 빈값 | NULL | **없음** (JSP 미사용) |
| 1 (생산) | 빈값 | NULL | **없음** |
| 2 (홈플러스) | maxBoxOrder | '06' | 정상 (별도 INSERT) |
| 3 (도매) | 빈값 | NULL | **없음** |
| 6 (롯데) | lotte_TryCount | '07' | 정상 (별도 INSERT) |

## 수정 방안

이마트/생산/도매 경로에서는 BOX_ORDER, CLIENT_TYPE을 사용하지 않으므로 수정 불필요.

> 원본과 동일한 구조이므로 **기존 기능 100% 동일 원칙**에 따라 현재는 수정하지 않는다.

## 상태
- [ ] 미수정 (원본과 동일, 이마트 기준 영향 없음)
