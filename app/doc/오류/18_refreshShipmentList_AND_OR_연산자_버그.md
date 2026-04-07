# refreshShipmentList() AND/OR 연산자 버그

## 발견일
2026-04-07

## 현상
- 출하대상받기(ProgressDlgShipSearch) 실행 시, 서버와 PDA 데이터를 비교하여 차이분을 삭제/추가하는 동기화(refreshShipmentList)에서 발생
- 삭제 대상이 2건 이상이면 **아무것도 삭제되지 않음**
- 삭제 대상 1건일 때만 정상 동작
- 단, 출하대상받기 시 `deletequeryShipment()`로 TB_SHIPMENT 전체 삭제 후 재INSERT가 선행되므로 **실질적 영향은 낮음**

## 원래부터 있던 버그인가?

**YES - 원본에도 동일한 코드 존재**

## 원인

### 문제: 삭제 대상 여러 건을 AND로 연결 (OR이어야 함)

#### 코드 위치
- `DBHandler.java` : refreshShipmentList() 1878~1883줄

#### 현재 문제 코드
```java
for (int i = 0; i < list_delete.size(); i++) {
    if (i == list_delete.size() - 1) {
        delete_where += " GI_D_ID = '" + list_delete.get(i) + "'";
    } else {
        delete_where += "GI_D_ID = '" + list_delete.get(i) + "' AND ";
        //                                                     ★ AND 잘못됨
    }
}
```

#### 발생 시나리오

**삭제 대상 1건 (정상):**
```sql
DELETE FROM TB_SHIPMENT WHERE GI_D_ID = '1330'
-- → 정상 삭제
```

**삭제 대상 2건 (버그):**
```sql
DELETE FROM TB_SHIPMENT WHERE GI_D_ID = '1330' AND GI_D_ID = '1440'
-- → GI_D_ID가 동시에 1330이면서 1440일 수 없음 → 항상 false → 삭제 안됨
```

**정상 코드:**
```sql
DELETE FROM TB_SHIPMENT WHERE GI_D_ID = '1330' OR GI_D_ID = '1440'
```

## 영향 범위
- `DBHandler.java` : refreshShipmentList() 1878~1883줄
- 출하대상받기 재실행 시 동기화 삭제 로직
- 삭제 대상 2건 이상일 때만 발생

## GI_L_ID 추가 개발과의 관계

개발31 Step 5에서 `list_delete` 자료구조를 GI_D_ID + GI_L_ID 조합으로 변경할 때, DELETE WHERE도 함께 수정함.
이때 AND/OR 버그도 **반드시 함께 수정**해야 함.

**수정 후 예상 코드:**
```sql
DELETE FROM TB_SHIPMENT 
WHERE (GI_D_ID = '1330' AND GI_L_ID = '5001') 
   OR (GI_D_ID = '1330' AND GI_L_ID = '5003')
```

## 상태
- [ ] 미수정 (개발31 Step 5에서 함께 수정 예정)

## 관련 문서
- `app/doc/개발/31_GI_L_ID_추가[GI_D_ID_비유니크_전체_영향범위].md` - Step 5
- `app/doc/오류/02_WH_AREA_null체크_OR연산자_버그.md` - 유사 패턴
