# 회사코드 packet 수정 (ITEM_CODE → Common.selectCompanyCode)

**작성일**: 2026-04-07
**목적**: 서버 전송 packet[10]에 ITEM_CODE 대신 실제 회사코드(Common.selectCompanyCode) 전송
**관련 오류**: `app/doc/오류/19_SM출고계근_회사코드_ITEM_CODE_잘림오류.md`

---

## AI 제약 조건

- 기존 WHERE 조건, 로직을 임의로 제거/추가/변경하지 않는다
- 문서에 명시된 step만 진행하고, 다음 step은 지시를 기다린다
- step 완료 후 체크리스트 + 진행 현황을 반드시 업데이트한다
- 문서에 없는 개선/리팩토링을 임의로 수행하지 않는다
- 기존 기능과 100% 동일하게 동작해야 한다

---

## 1. 현재 구조

### BixolonShipmentActivity.java packet 조립

```java
// 2601줄
packet += list_send_info.get(i).getITEM_CODE() + "::";   // splitData[10] = ITEM_CODE("1110100694")
```

### insert_goods_wet.jsp

```java
// 85줄
pstmt.setString(14, splitData[10]);   // SM_출고계근.회사코드에 ITEM_CODE 저장
```

### SM_출고계근.회사코드

```java
// BaseEntity.java:42
@Column(name = "회사코드", columnDefinition = "VARCHAR(2)")
```

### 문제점

- ITEM_CODE("1110100694", 10자리) → VARCHAR(2) → **잘림 에러**
- SM_출고계근에 품목코드 컬럼 없음 (불필요)
- 회사코드에 실제 회사코드("20")가 들어가야 함

---

## 2. 변경 구조

```
[변경 전]
packet[10] = getITEM_CODE() = "1110100694" → 회사코드(VARCHAR(2)) → 에러

[변경 후]
packet[10] = Common.selectCompanyCode = "20" → 회사코드(VARCHAR(2)) → 정상
```

---

## 3. 수정 대상 파일

| # | 파일 | 위치 | 수정 내용 |
|:-:|------|------|----------|
| 1 | **BixolonShipmentActivity.java** | packet 조립 2601줄 | getITEM_CODE() → Common.selectCompanyCode |
| 2 | **BixolonShipmentActivity.java** | packet 조립 2707줄 (생산/도매) | 동일 |

---

## 4. 수정 상세

### 4.1 BixolonShipmentActivity.java

**변경 전 (이마트/홈플러스/롯데 구로직, 2601줄):**
```java
packet += list_send_info.get(i).getITEM_CODE() + "::";
```

**변경 후:**
```java
packet += Common.selectCompanyCode + "::";STEP1 
```

**변경 전 (생산/도매/비정량 신로직, 2707줄):**
```java
packet += list_send_info.get(i).getITEM_CODE() + "::";
```

**변경 후:**
```java
packet += Common.selectCompanyCode + "::";
```

---

## 5. 사이드이펙트

- packet[10] 이후 인덱스 변경 없음 (값만 교체)
- insert_goods_wet.jsp 수정 불필요 (splitData[10]을 그대로 회사코드로 사용)
- packet[11]=BRAND_CODE, [12]=CLIENT_TYPE, [13]=BOX_ORDER, [14]=GI_L_ID 영향 없음
- ShipmentActivity도 동일 패턴이지만 미사용 파일

---

## 6. 개발 플랜

### Step 1: packet[10] 회사코드 수정

**체크리스트**
- [x] BixolonShipmentActivity 이마트/홈플러스/롯데 packet 수정 (2602줄)
- [x] BixolonShipmentActivity 생산/도매/비정량 packet 수정 (2710줄)
- [x] 컴파일 확인 (BUILD SUCCESSFUL)
- [ ] 전송 테스트 → SM_출고계근.회사코드 = "20" 정상 저장 확인

**Part 6. 변경 내용**:
- **무엇을**: packet[10]에 getITEM_CODE() → Common.selectCompanyCode 변경 (2곳)
- **왜**: ITEM_CODE(10자리)가 SM_출고계근.회사코드(VARCHAR(2))에 저장 시 잘림 에러
- **어떻게**: 2602줄, 2710줄에서 `Common.selectCompanyCode`("20") 전송

---

## 7. 진행 현황

| Step | 작업 | 상태 |
|------|------|------|
| 1 | packet[10] 회사코드 수정 | ✅ 완료 (실기기 테스트 대기) |

---

## 관련 문서

- `app/doc/오류/19_SM출고계근_회사코드_ITEM_CODE_잘림오류.md`
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md`

---

**문서 버전**: 1.0
