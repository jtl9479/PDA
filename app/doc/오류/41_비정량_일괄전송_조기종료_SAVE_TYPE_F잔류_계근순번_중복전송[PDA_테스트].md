# 비정량 일괄전송 조기 종료로 SAVE_TYPE F 잔류 → 재전송 시 계근순번 중복 INSERT

## 발견일
2026-10-07 (서버 데이터 근거는 2026-10-01 테스트 전송분)

## 에러 발생 시나리오

```
1. 이마트 비정량(searchType=4) 출하대상 다운로드 (출고상세SEQ 508 / LOT 586 / 품목 2120300988 / 출고수량 3)
2. 3건 계근 (스캔 2건 + 수기 1건) → 로컬 SAVE_TYPE = F 3건
3. 전송 → 3건이 한 packet 으로 서버에 일괄 INSERT (SM_출고계근 SEQ 255~257)
4. 로컬 Y 처리 루프에서 출하대상 전송개수 == 출하요청수량 → 즉시 return "ss" → 3건 중 일부 F 잔류
5. 다시 전송 → F 잔류 행이 다시 packet 에 포함 → SEQ 258, 259 로 동일 계근순번 중복 INSERT
6. PDA 상세 화면에 No 1,2,2,3,3 으로 표시
```

---

## 현상
- 서버 SM_출고계근(운영 DB weberp_hl)에 동일 출고상세/LOT 의 계근순번이 중복 저장됨

| SEQ | 등록시간 | 계근순번 | 중량 | 바코드 |
|-----|---------|:-------:|-----:|--------|
| 255 | 112611 | 1 | 11.8 | 01180202609282120300988 |
| 256 | 112611 | 2 | 12.5 | 01254202609282120300988 |
| 257 | 112611 | 3 | 24 | (수기, 빈값) |
| 258 | 112629 | 2 | 12.5 | 01254202609282120300988 (중복) |
| 259 | 112629 | 3 | 24 | (빈값) (중복) |

- PDA 상세 화면(2026-10-06 EDA51)에서도 No 1,2,2,3,3 으로 표시
- **서버에는 이미 올라갔는데 PDA 는 F(미전송) 로 표시 (수정리스트 B1: F/Y 상태 불일치), 재전송 시 중복 적재 (B2: 계근 순번 중복)**
- 비교: 홈플러스 비정량(마트사구분 4) 출고상세 499 / LOT 577 SEQ 260~262 (순번 1,2,3) 은 중복 없음
- 별건 참고: 롯데 출고상세 510 / LOT 588 계근순번 2 가 2건 (6.05, 6.02, 중량 상이) → 본 건과 원인 상이 추정 (PACKING_QTY 기반 BOX_CNT 미갱신), 별도 확인 필요

## 원래부터 있던 버그인가?

**YES (원본 동일) - 원본 ShipmentActivity 의 일괄전송 분기와 selectqueryListGoodsWetInfo count 식이 동일**

```java
// 원본 D:\PDA\PDA-INNO(원본)\...\ShipmentActivity.java:3422~3545 일괄전송 분기 - 동일한 조기 return "ss"
// 원본 DBHandler.selectqueryListGoodsWetInfo(약 1424행) - 동일한 count(SAVE_TYPE='Y') 식
// 원본은 3/4/5 에서 URL_UPDATE_SHIPMENT 호출 대신 receiveData="s" 처리(3518행)로 동일하게 Y 처리 경로 진입
```

## 원인

### 문제 1 (주요): 일괄전송 분기의 조기 return "ss" 로 나머지 행이 F 로 잔류

#### 코드 위치
- `BixolonShipmentActivity.java:2864` : 일괄전송 분기 진입 (searchType 1, 3, 4, 5, 7)
- `BixolonShipmentActivity.java:2869~2895` : F 행 전체를 한 packet(`##` 구분)으로 조립
- `BixolonShipmentActivity.java:2916`, `:2920` : URL_INSERT_GOODS_WET_NEW 일괄 전송
- `BixolonShipmentActivity.java:2934~2966` : 로컬 Y 처리 루프 (조기 return 포함)
- `DBHandler.java:1729` : `updatequeryGoodsWet` (한 건씩 SAVE_TYPE Y 갱신)

#### 현재 문제 코드
```java
// BixolonShipmentActivity.java:2934~2957
for (int i = 0; i < list_send_info.size(); i++) {
    if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
        if (result.equals("s")) {
            boolean bool = DBHandler.updatequeryGoodsWet(...);          // 한 건 Y 처리
            if (bool) {
                for (int j = 0; j < arSM.size(); j++) {
                    if (...GI_D_ID 일치 && GI_L_ID 일치) {
                        arSM.get(j).setSAVE_CNT(arSM.get(j).getSAVE_CNT() + 1);
                        if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {
                            ...
                            jChk++;
                            if (jChk == arSM.size()) {
                                return "ss";   // ★ 남은 F 행을 Y 로 바꾸지 않고 종료
                            }
                        }
                    }
                }
            }
        }
    }
}
```

#### 발생 시나리오
서버에는 packet 의 모든 행이 INSERT 되었으나, 로컬 루프는 `SAVE_CNT+1 == GI_REQ_PKG` 이고 `jChk == arSM.size()` 가 되는 시점에 즉시 종료한다. 이후 행은 Y 로 갱신되지 않아 F 로 남고, 다음 전송 때 packet 에 다시 포함되어 서버에 중복 INSERT 된다.

### 문제 2 (보조): SEND_Y 카운트가 전체 행 수를 셈 → 전송개수 초기값 부풀림

#### 코드 위치
- `DBHandler.java:1398` : `count(SAVE_TYPE='Y') as SEND_Y`
- `BixolonShipmentActivity.java:2604` : `arSM.get(i).setSAVE_CNT(Integer.parseInt(row[2]))`

#### 문제 코드
```java
// DBHandler.java:1398
+ " count(" + DBInfo.SAVE_TYPE + "='Y') as SEND_Y,"
// ★ SQLite count(식) 은 0/1 모두 non-NULL 이라 전체 행 수를 셈 → 미전송 F 포함

// BixolonShipmentActivity.java:2604
arSM.get(i).setSAVE_CNT(Integer.parseInt(row[2]));      // 계근 상품 전송 개수
// ★ 전송개수 초기값이 실제보다 큼 → 로컬 루프에서 조기 종료 조건이 앞당겨짐
```

## 상세 흐름

1. **계근지점 조회** (`BixolonShipmentActivity.java:2597~2605`)
   - `selectqueryListGoodsWetInfo` 의 SEND_Y 가 전체 행 수(F 포함)로 반환
   - SAVE_CNT 초기값이 부풀려짐

2. **전송 packet 조립** (searchType 1, 3, 4, 5, 7, `:2864~2895`)
   - SAVE_TYPE F 행 전체를 한 packet 으로 합침 → URL_INSERT_GOODS_WET_NEW 로 일괄 전송
   - 서버 응답 "s" 이면 서버에는 전 행 INSERT 완료

3. **로컬 Y 처리 루프** (`:2934~2966`)
   - **경로 A (정상 기대)**: 모든 F 행이 Y 로 갱신 → 재전송 시 packet 없음 ("af")
   - **경로 B (문제)**: SAVE_CNT+1 == GI_REQ_PKG 이고 jChk == arSM.size() 가 되는 행에서 `return "ss"` → 이후 F 행 잔류 (B1)

4. **재전송**
   - F 잔류 행이 다시 packet 에 포함 → 서버에 동일 계근순번 중복 INSERT (B2)

5. **정량 경로 (이마트/홈플러스/롯데, `:2785~2863`) 는 해당 없음**
   - 건별 전송 후 즉시 Y 처리라 조기 종료돼도 미전송 건만 남아 중복 없음

## 영향 범위
- 일괄전송 경로 searchType 1(생산, 미사용), 3(도매), 4(비정량), 5(홈플러스 비정량), 7(생산라벨)
- 확인된 사례: 이마트 비정량 (SEQ 255~259). 도매·홈플러스 비정량은 동일 코드 경로라 동일 조건에서 재현 가능
- 파일: `BixolonShipmentActivity.java`, `DBHandler.java` (구버전 `ShipmentActivity.java:3680~3856` 도 동일 구조, 진입 불가 파일)
- 데이터: 운영 `SM_출고계근` 중복 행 (SEQ 258, 259). 사용자 예정: 테이블 데이터 전체 삭제로 정리

## 수정 방안

원본 동일 결함이므로 수정 여부는 사용자 결정 필요 (미적용).

### 수정 1 (안1, 영향 최소): 일괄전송 분기에서 조기 return 제거

```java
// BixolonShipmentActivity.java:2953~2957 부근
// 성공 시 조기 return 하지 않고 F 행 전부 Y 처리 후 루프 종료 뒤 jChk 로 판정
if (jChk == arSM.size()) { /* return "ss" 제거, 루프 종료 후 아래에서 처리 */ }
...
// for 루프 종료 후
if (result.equals("s") && jChk == arSM.size()) {
    return "ss";
}
return result;
```

### 수정 2 (안2): SEND_Y 카운트 수정

```java
// DBHandler.java:1398
+ " sum(CASE WHEN " + DBInfo.SAVE_TYPE + "='Y' THEN 1 ELSE 0 END) as SEND_Y,"
```

> 안2 는 정량 경로의 SAVE_CNT 판정에도 영향을 주므로 영향분석 후 적용. 안1 만 적용 시 영향 최소.
> 수정 시 원본 동작과의 차이(조기 return 제거)에 대한 사용자 승인 필요.

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/개발/70_테스트후_수정리스트.xlsx` (B1 비정량 전송 후 F/Y 상태 불일치, B2 비정량 계근 순번 중복)
- `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md`
- `app/doc/개발/37_비정량_계근데이터전송_JSP_MSSQL전환.md`
- `app/doc/테스트/증빙/2026-10-06_바코드타입정리_EDA51/04_M9_재출력_상세.png`
- `app/doc/참고자료/오류패턴_분석.md` (패턴 F: UI/로직 버그, 원본 동일)
