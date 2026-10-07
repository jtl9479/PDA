# 비정량 일괄전송 로컬 Y 처리 루프 조기 종료(return "ss") 제거

**작성일**: 2026-10-07
**목적**: 일괄전송 분기(searchType 1·3·4·5·7)의 로컬 Y 처리 루프에서 마지막 출하대상 완료 시점에 즉시 `return "ss"` 하여 남은 SAVE_TYPE F 행이 Y 로 갱신되지 않는 문제(수정리스트 B1)와, 그로 인한 재전송 시 서버 SM_출고계근 계근순번 중복 INSERT(B2)를 방지한다. `return "ss"` 를 루프 종료 후로 이동한다 (안1, 최소 변경).

> **사용자 결정(2026-10-07)**: 오류 41 수정 방안 중 **안1 채택**. 안2(`DBHandler.selectqueryListGoodsWetInfo` SEND_Y count 식)는 적용하지 않는다.
> **원본 동일 결함에 대한 사용자 승인 예외**: 원본 `ShipmentActivity` 일괄전송 분기도 동일한 조기 return 구조이므로 "원본 100% 동일" 원칙의 예외이다. 서버 중복 적재 방지를 위해 사용자가 승인한 변경이며, 반환값 의미와 서버 전송 packet 은 변경하지 않는다. 차이는 "남은 F 행이 Y 로 갱신된다"는 점뿐이다.
> **대상 제외**: 정량 분기(이마트·홈플러스·롯데), `ShipmentActivity.java`(구버전, 진입 불가), `DBHandler.java`.

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

추가 제약(본 문서): 람다 등 새 문법 도입 금지. 지정된 `return "ss"` 블록 이동 외 변경 금지(들여쓰기 외 형태 유지).

---

## 1. 현재 구조

### BixolonShipmentActivity.ProgressDlgShipmentSend.doInBackground — 일괄전송 분기

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`
(분기 진입 2864행: `searchType` 1·3·4·5·7, F 행 packet 조립 2869~2895, 전송 2905~2924, 로컬 Y 루프 2934~2966, 분기 종료 2967, `return result;` 2968)

```java
// 2934~2966 (현재)
for (int i = 0; i < list_send_info.size(); i++) { //계근데이터 루프돌면서
    if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
        if (result.equals("s")) {
            boolean bool = DBHandler.updatequeryGoodsWet(mContext, ...); //PDA 계근테이블 SAVE TYPE Y로 업데이트
            if (bool) {
                publishProgress(...);
                for (int j = 0; j < arSM.size(); j++) {
                    if (GI_D_ID 일치 && GI_L_ID 일치) {
                        arSM.get(j).setSAVE_CNT(arSM.get(j).getSAVE_CNT() + 1);
                        if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {
                                arSM.get(j).setSAVE_TYPE("Y");
                                DBHandler.updatequeryShipment(...);
                                jChk++;

                                if (jChk == arSM.size()) {                                   // 2953
                                    Log.d(TAG, "arSM.size() when return: " + arSM.size());   // 2954
                                    Log.d(TAG, "jChk number when return: " + jChk);          // 2955
                                    return "ss";                                             // 2956  ★ 조기 종료
                                }                                                            // 2957
                        }
                    }
                }
            }
        } else if (result.equals("f")) {
            return result;
        }
    }
}
}   // 2967 분기 종료
return result;   // 2968
```

- 서버 전송은 F 행 전체를 한 packet(`##` 구분)으로 한 번에 보낸다. 응답 "s" 이면 서버에는 전 행이 INSERT 된다.
- 로컬은 F 행을 한 건씩 Y 로 갱신하다가, 모든 출하대상이 완료되는 시점(`jChk == arSM.size()`)에 즉시 `return "ss"` 하므로 이후 F 행은 Y 로 갱신되지 않는다.

### 문제점

- 서버에는 올라갔으나 로컬 일부 행이 F 로 잔류 → 상세 화면 F/Y 상태 불일치 (B1)
- 재전송 시 잔류 F 행이 다시 packet 에 포함 → 서버 SM_출고계근에 동일 계근순번 중복 INSERT (B2, 오류 41 SEQ 258·259)
- 원본(`ShipmentActivity` 3422~3545 일괄전송 분기)도 동일 구조 → 원본 결함

---

## 2. 변경 구조

### 데이터 흐름

```
변경 전
[전송] F 행 3건 → packet 1개 → 서버 INSERT 3건 (result="s")
    ↓
[로컬 Y 루프] 행1 Y → 행2 Y → (jChk == arSM.size()) return "ss"
    ↓
[행3 F 잔류] → 재전송 시 packet 에 행3 포함 → 서버 중복 INSERT

변경 후
[전송] F 행 3건 → packet 1개 → 서버 INSERT 3건 (result="s")
    ↓
[로컬 Y 루프] 행1 Y → 행2 Y → 행3 Y (루프 끝까지 수행, 조기 return 없음)
    ↓
[루프 종료 후] if (jChk == arSM.size()) return "ss"; else return result;
    ↓
[재전송] F 행 없음 → packet=="" → "af" ("이미 모두 전송되었거나 전송할 건이 없습니다.")
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **BixolonShipmentActivity.java** | 일괄전송 분기 로컬 Y 루프 2953~2957행 | 루프 안 `if (jChk == arSM.size()) { Log 2줄; return "ss"; }` 블록 삭제 |
| 2 | **BixolonShipmentActivity.java** | 같은 분기, 바깥 for 종료(2966행) 직후 | `if (jChk == arSM.size()) { return "ss"; }` 추가 (이후 기존 `return result;` 유지) |
| - | BixolonShipmentActivity.java 정량 분기 | 2785~2863행 | **변경 금지** (건별 전송, 조기 return 시 미전송 건만 남아 중복 없음) |
| - | DBHandler.java `selectqueryListGoodsWetInfo` | SEND_Y count 식 | **변경 금지** (안2 미적용) |
| - | ShipmentActivity.java | - | 수정 금지 (구버전, 진입 불가) |

---

## 4. 수정 상세

### 4.1 BixolonShipmentActivity.java (Step 1)

**경로**: `app/src/main/java/com/rgbsolution/highland_emart/BixolonShipmentActivity.java`

**변경 전:**

```java
                                            if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {  // 전송 개수와 출하요청 개수가 같으면
                                                    Log.v(TAG, "출하대상 계근 완료");
                                                    arSM.get(j).setSAVE_TYPE("Y");          // 전부 전송했다면 전송여부 Y로 변경
                                                    DBHandler.updatequeryShipment(mContext, arSM.get(j).getGI_D_ID(), arSM.get(j).getPACKER_PRODUCT_CODE(), arSM.get(j).getGI_L_ID());

                                                    jChk++;

                                                    if (jChk == arSM.size()) {
                                                        Log.d(TAG, "arSM.size() when return: " + arSM.size());
                                                        Log.d(TAG, "jChk number when return: " + jChk);
                                                        return "ss";
                                                    }
                                            }
                                        }
                                    }
                                }
                            } else if (result.equals("f")) {
                                return result;
                            }//result "s이면" 끝
                        }
                    }
                }
                return result;
```

**변경 후:**

```java
                                            if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {  // 전송 개수와 출하요청 개수가 같으면
                                                    Log.v(TAG, "출하대상 계근 완료");
                                                    arSM.get(j).setSAVE_TYPE("Y");          // 전부 전송했다면 전송여부 Y로 변경
                                                    DBHandler.updatequeryShipment(mContext, arSM.get(j).getGI_D_ID(), arSM.get(j).getPACKER_PRODUCT_CODE(), arSM.get(j).getGI_L_ID());

                                                    jChk++;
                                            }
                                        }
                                    }
                                }
                            } else if (result.equals("f")) {
                                return result;
                            }//result "s이면" 끝
                        }
                    }
                    if (jChk == arSM.size()) {
                        return "ss";
                    }
                }
                return result;
```

(삽입 위치: 로컬 Y 처리 바깥 `for (int i ...)` 닫는 `}` 직후, 분기(`else if`) 닫는 `}` 직전. 즉 일괄전송 분기 안에서만 판정한다. 정량 분기·분기 밖 경로에서는 `jChk` 가 별도 지역 변수(2783행)이므로 영향 없음 — 구현 시 일괄전송 분기에서 `jChk` 선언·초기화 범위(2783행 선언이 분기 바깥 공통인지)를 확인한다.)

**검증**: 컴파일 후 grep 으로 일괄전송 분기에 `return "ss"` 가 루프 밖 1곳만 있는지, 정량 분기(2853행)는 그대로인지 확인.

---

## 5. 사이드이펙트

### 5.1 반환값 동치성

- `jChk` 는 출하대상(arSM 1건)별로 `SAVE_CNT == GI_REQ_PKG` 도달 시 1회만 증가한다. 도달 후 같은 출하대상의 SAVE_CNT 는 계속 증가하여 `==` 재진입이 없으므로 jChk 는 arSM 건수를 초과하지 않는다.
- 따라서 "모든 출하대상 완료 시 `jChk == arSM.size()`" 는 루프 중간에서 판정하든 종료 후 판정하든 성립 조건이 동일하다. `return "ss"` 반환 조건 불변.
- 그 외: 완료 미달 시 `return result;` ("s"), 서버 응답 "f" 는 루프 안 `return result` 즉시 반환 유지, packet 없음은 "af" 유지.

### 5.2 onPostExecute (2982~3003행) 분기

```java
if (_result.equals("s"))   → Toast "결과 s, 전송 성공." + notifyDataSetChanged
else if ("ss")             → Toast "결과 ss, 전송성공." + notifyDataSetChanged + show_sendFinishDialog()
else if ("update_fail")    → ...
else if ("f")              → 네트워크 에러 Toast + 진동
else if ("af")             → "이미 모두 전송되었거나 전송할 건이 없습니다." + 진동
```

- 반환 문자열 집합과 의미가 변경되지 않으므로 onPostExecute 분기 결과는 동일하다. 변경점은 ss 반환 시 로컬 SQLite 의 F→Y 갱신 대상이 "전부"가 된다는 것뿐이다(그 후 `notifyDataSetChanged` 로 화면 반영).
- 로컬 갱신 행수가 늘어 `updatequeryGoodsWet` 호출 횟수·`publishProgress` 호출 횟수가 증가한다(진행 표시 개수 증가, 기능 영향 없음).

### 5.3 기타 영향

- `jChk == arSM.size()` 직전에 발생하던 조기 종료로 건너뛰던 나머지 F 행의 `arSM.get(j).setSAVE_CNT(+1)` 가 추가 실행된다. 이미 완료된 출하대상은 `SAVE_CNT > GI_REQ_PKG` 가 되며 `==` 조건 재진입이 없어 SAVE_TYPE/로컬 출하 갱신에는 영향 없음. 이후 화면 전송개수 표시(SAVE_CNT 사용부)에서 요청 수량 초과 값이 표시될 가능성이 있는지 Step 1 에서 SAVE_CNT 사용처를 확인한다.
- `jChk` 는 해당 doInBackground 지역 변수이며 외부 참조 없음(확인 대상).
- 정량(이마트·홈플러스·롯데) 분기: 코드 변경 없음 → 영향 없음.
- 안2 미적용이므로 SEND_Y 초기값 부풀림(오류 41 문제 2)은 잔존한다. 이로 인해 `SAVE_CNT` 초기값이 큰 경우 `==` 조건 도달 시점이 달라질 수 있으나 본 변경의 판정 로직은 기존과 동일하다.

---

## 6. 데이터 저장 구조

### 변수 매핑

| 변수 | 타입 | 용도 | 예시 |
|------|------|------|------|
| `list_send_info` | List<GoodsWetInfo> | 로컬 계근데이터(SAVE_TYPE F/Y) | 3건 F |
| `arSM` | 출하대상 목록 | 출하대상 1건 = GI_D_ID(+GI_L_ID) 1행 | 1건 (GI_REQ_PKG=3) |
| `SAVE_CNT` | int | 출하대상별 전송 개수 | 초기 row[2](SEND_Y), 루프에서 +1 |
| `jChk` | int | 완료된 출하대상 건수 | 1 |
| `result` | String | 서버 응답/반환값 | "s", "ss", "f", "af" |

### 로컬/서버 매핑 (테스트 데이터)

```
로컬 TB_GOODS_WET (GI_D_ID=출고상세SEQ, GI_L_ID=LOT) SAVE_TYPE  ↔  서버 SM_출고계근
F → (전송 후) Y                                                 INSERT 3건 (계근순번 1,2,3)
```

---

## 7. 호출 시점

```
[비정량 계근 화면 (BixolonShipmentActivity)]
    ├── 계근(스캔/수기) → 로컬 TB_GOODS_WET SAVE_TYPE=F
    ├── 전송 버튼
    │       ↓ ProgressDlgShipmentSend.doInBackground (비동기)
    │   [일괄전송 분기] F 행 packet 조립 → URL_INSERT_GOODS_WET_NEW
    │       ↓ result "s"
    │   ★ 로컬 Y 처리 루프 (본 변경 대상) → return "ss" 또는 result
    │       ↓
    │   onPostExecute: "ss" → 전송완료 다이얼로그 / "s" → Toast / "af" → 이미 전송
    └── 재전송 → F 없음 → "af"
```

---

## 8. 개발 플랜

### Step 1: 코드 수정 + 컴파일

**Part 1. 분석**
- 메서드: `BixolonShipmentActivity.ProgressDlgShipmentSend.doInBackground` (일괄전송 분기)
- 범위: `BixolonShipmentActivity.java` 2864~2968행 (수정은 2953~2957행 삭제 + 2966행 직후 추가)
- 용도: 로컬 F 행 전부 Y 갱신 후 "ss" 판정
- 주의할 점: 정량 분기(2848~2853행 동일 패턴)는 변경 금지. 일괄전송 분기와 구분하여 수정한다.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 조기 return 블록 | 2953~2957 | `if (jChk == arSM.size()) { Log 2줄; return "ss"; }` 삭제 |
| 2 | 루프 후 판정 | 2966 직후 | `if (jChk == arSM.size()) { return "ss"; }` 추가 |
| 3 | jChk 선언 | 2783 | 일괄전송 분기에서도 접근 가능한 범위인지 확인 |
| 4 | SAVE_CNT 사용처 | 파일 전체 | 요청 수량 초과 값 표시 여부 확인 |

**Part 2. 변환 계획**
- 변환 방식: 블록 삭제 후 루프 종료 직후 동일 조건 판정 1건 추가. 로그 2줄은 삭제 블록에 포함.
- 주의사항: 람다 등 새 문법 금지, 정량 분기·DBHandler 변경 금지, 반환값 의미 불변.

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 변환 수행
- [ ] Part 4: 컴파일 확인
- [ ] Part 5: 단위테스트
- [ ] Part 6: 회귀테스트

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 2: EDA51 실기기 테스트

**Part 1. 분석**
- 메서드: 위 doInBackground 일괄전송 분기 + 비정량 계근 UI
- 범위: EDA51 실기기 (비정량 searchType=4, 정량 이마트 회귀)
- 용도: F 잔류 제거, 재전송 시 "af", 서버 중복 없음 확인
- 주의할 점: 테스트 전 서버 SM_출고계근 상태 확인(사용자가 전체 삭제 예정). 이전 중복 행(SEQ 258·259)이 남아 있으면 판정이 흐려진다.

| # | 항목 | 위치 | 내용 |
|---|------|------|------|
| 1 | 테스트 전 서버 상태 | SM_출고계근(weberp_hl) | 대상 출고상세/LOT 기존 행 유무·삭제 여부 확인 |
| 2 | 비정량 3박스 전송 | 비정량 계근 화면 | 로컬 3건 모두 SAVE_TYPE Y |
| 3 | 재전송 | 전송 버튼 | "af" Toast |
| 4 | 정량 회귀 | 이마트 정량 | 전송 동작 변경 없음 |

**Part 2. 변환 계획**
- 변환 방식: 코드 변경 없음(테스트). 테스트 데이터 `app/doc/테스트/` 시나리오 기준.
- 주의사항: 실패 시 `app/doc/오류/` 문서화 후 개발 문서로 되돌림.

**체크리스트**
- [ ] Part 1: 분석 완료 확인
- [ ] Part 2: 변환 계획 확인
- [ ] Part 3: 테스트 수행
- [ ] Part 4: 서버/로컬 데이터 대조
- [ ] Part 5: 증빙(스크린샷) 저장
- [ ] Part 6: 회귀테스트(정량 이마트)

**Part 6. 변경 내용** (완료 후 작성):
- **무엇을**:
- **왜**:
- **어떻게**:

---

### Step 3: 통합 테스트

| # | 테스트 | 확인 |
|:-:|--------|------|
| 1 | 이마트 비정량(4) 3박스 전송 → 로컬 3건 Y, 서버 3건 (순번 1,2,3) | □ |
| 2 | 이마트 비정량 재전송 → "af", 서버 행 수 불변 | □ |
| 3 | 홈플러스 비정량(5) 일괄전송 → 로컬 전부 Y, 서버 중복 없음 | □ |
| 4 | 도매(3) 일괄전송 → 로컬 전부 Y, 서버 중복 없음 | □ |
| 5 | 일부 출하대상만 완료된 경우 반환 "s" (ss 다이얼로그 미표시) | □ |
| 6 | 전송 실패("f") 시 기존과 동일 Toast, 로컬 F 유지 | □ |
| 7 | 정량(이마트·홈플러스·롯데) 전송 회귀 이상 없음 | □ |
| 8 | 전송 후 상세 화면 No 중복 표시 없음 | □ |

---

### 개발 순서 요약

```
Step 1: 코드 수정 + 컴파일
    ↓
Step 2: EDA51 실기기 테스트
    ↓
Step 3: 통합 테스트
```

---

## 9. 테스트 시나리오

**테스트 데이터**: 이마트 비정량, 출고일 2026-09-28, 품목 2120300988(여주WET). 바코드 형식 중량5/제조일8/품목10, 예 `01402202609282120300988`.

### 시나리오 0: 사전 확인

```
1. 서버 SM_출고계근 대상 출고상세/LOT 의 기존 행 확인 (전체 삭제 완료 여부, SEQ 258·259 중복 행 잔존 여부)
2. PDA 앱 로컬 TB_GOODS_WET 초기화(앱 데이터 정리 또는 신규 다운로드)
```

### 시나리오 1: 비정량 3박스 전송 후 F 잔류 없음

```
1. 이마트 비정량(searchType=4) 출하대상 다운로드 (출고수량 3)
2. 3박스 계근 (스캔 2건 + 수기 1건) → 로컬 SAVE_TYPE F 3건
3. 전송 → "결과 ss, 전송성공." 및 전송완료 다이얼로그
4. 상세 화면에서 3건 모두 전송(Y) 표시, 로컬 TB_GOODS_WET SAVE_TYPE Y 3건 확인
5. 서버 SM_출고계근 3건(계근순번 1,2,3) 확인
```

### 시나리오 2: 재전송 시 "af" 및 서버 중복 없음

```
1. 시나리오 1 직후 전송 버튼 다시 누름
2. "이미 모두 전송되었거나 전송할 건이 없습니다." Toast + 진동 (af)
3. 서버 SM_출고계근 행 수가 3건 그대로인지 확인 (중복 INSERT 없음)
```

### 시나리오 3: 정량 회귀 (이마트)

```
1. 이마트 정량 출하대상 계근 → 전송
2. 건별 전송·Y 처리·ss 다이얼로그가 변경 전과 동일하게 동작
```

### 시나리오 4: 홈플러스 비정량·도매 일괄전송

```
1. 홈플러스 비정량(5) / 도매(3) 계근 → 전송
2. 로컬 전부 Y, 재전송 af, 서버 중복 없음
```

---

## 10. 예상 문제점 및 해결 방안

| # | 문제점 | 원인 | 해결 방안 |
|---|--------|------|----------|
| 1 | 전송 후에도 "s" 로 끝나 완료 다이얼로그 미표시 | `jChk != arSM.size()` (SEND_Y 부풀림으로 SAVE_CNT 초기값이 커서 `==` 미도달 가능) | 원본과 동일한 판정이므로 변경 전과 같음. 안2 미적용 상태 유지, 필요 시 별도 건으로 처리 |
| 2 | SAVE_CNT 가 요청 수량을 초과해 화면 표시 이상 | 조기 return 제거로 나머지 F 행의 SAVE_CNT +1 추가 실행 | Step 1 에서 SAVE_CNT 사용처 확인, 이상 시 사용자 보고 후 재검토 |
| 3 | 기존 서버 중복 행(SEQ 258·259)으로 서버 확인 혼선 | 이전 테스트 잔존 데이터 | 테스트 전 사용자가 SM_출고계근 삭제 후 시작 |
| 4 | 정량 분기까지 같이 수정 | 동일 패턴(2848~2853행) 오인 | 정량 분기 변경 금지, diff 로 일괄전송 분기만 변경되었는지 확인 |
| 5 | 서버 응답 "f" 시 부분 갱신 | 루프 중 `result.equals("f")` 즉시 return 유지 | 동작 불변(원본 동일), 시나리오 6 으로 확인 |

---

## 11. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | 코드 수정 + 컴파일 | ⏳ 대기 |
| 2 | EDA51 실기기 테스트 | ⏳ 대기 |
| 3 | 통합 테스트 | ⏳ 대기 |

---

## 관련 문서

- `app/doc/오류/41_비정량_일괄전송_조기종료_SAVE_TYPE_F잔류_계근순번_중복전송[PDA_테스트].md`
- `app/doc/개발/70_테스트후_수정리스트.xlsx` (B1 비정량 전송 후 F/Y 상태 불일치, B2 비정량 계근 순번 중복)
- `app/doc/소스분석/44_비정량출하_조회부터_계근전송_전체흐름.md`
- `app/doc/개발/37_비정량_계근데이터전송_JSP_MSSQL전환.md`

---

**문서 버전**: 1.0
