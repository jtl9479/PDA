# SM_출고계근 회사코드에 ITEM_CODE 저장 시 잘림 오류

## 발견일
2026-04-07

## 에러 발생 시나리오

```
1. 바코드 스캔 → 계근 완료 → 전송 버튼 클릭
2. packet 조립: splitData[10] = ITEM_CODE("1110100694", 10자리)
3. insert_goods_wet.jsp: pstmt.setString(14, splitData[10]) → SM_출고계근.회사코드에 INSERT
4. SM_출고계근.회사코드 = VARCHAR(2) → 10자리 데이터 저장 불가
5. SQLServerException: 문자열이나 이진 데이터는 잘립니다.
```

---

## 현상
- 전송 버튼 클릭 시 서버 전송 실패
- Tomcat 로그: `com.microsoft.sqlserver.jdbc.SQLServerException: 문자열이나 이진 데이터는 잘립니다.`
- SM_출고계근.회사코드(VARCHAR(2))에 ITEM_CODE(10자리)가 들어감

## 원래부터 있던 버그인가?

**YES - 원본에서도 splitData[10]에 ITEM_CODE를 회사코드로 전송하는 구조**

단, 원본 Oracle에서는 회사코드 컬럼 크기가 달랐거나 다른 테이블에 INSERT했을 가능성이 있어 에러가 안 났을 수 있음. MSSQL 전환 후 SM_출고계근.회사코드가 VARCHAR(2)이므로 에러 발생.

## 원인

### 문제: packet[10]에 ITEM_CODE가 회사코드로 전송됨

#### PDA packet 조립 (BixolonShipmentActivity.java:2601)
```java
packet += list_send_info.get(i).getITEM_CODE() + "::";   // splitData[10]
```

#### JSP INSERT (insert_goods_wet.jsp:85)
```java
pstmt.setString(14, splitData[10]);   // SM_출고계근.회사코드에 저장
```

#### SM_출고계근.회사코드 타입 (BaseEntity.java:42)
```java
@Column(name = "회사코드", columnDefinition = "VARCHAR(2) DEFAULT ''")
```

#### 불일치
| 항목 | 값 | 크기 |
|------|-----|:----:|
| splitData[10] (ITEM_CODE) | "1110100694" | 10자리 |
| SM_출고계근.회사코드 | VARCHAR(2) | 2자리 |
| → **초과** | | |

## 영향 범위
- insert_goods_wet.jsp: pstmt.setString(14, splitData[10])
- BixolonShipmentActivity.java: packet 조립 2601줄
- ShipmentActivity.java: packet 조립 (동일 패턴)
- SM_출고계근 INSERT 전부 실패

## 수정 방안

### 방안: packet[10]에 실제 회사코드(Common.selectCompanyCode) 전송

**PDA packet 조립 변경:**
```java
// 변경 전
packet += list_send_info.get(i).getITEM_CODE() + "::";   // splitData[10] = ITEM_CODE

// 변경 후
packet += Common.selectCompanyCode + "::";                // splitData[10] = 회사코드("20")
```

- SM_출고계근.회사코드 = "20" (2자리) → VARCHAR(2)에 정상 저장
- ITEM_CODE는 SM_출고계근에 불필요 (SM_출고상세에서 관리)

## 상태
- [ ] 미수정

## 관련 문서
- `app/doc/소스분석/39_이마트출하_조회부터_계근전송_전체흐름.md` - 섹션 9.1 (splitData[10] 회사코드 문제)
