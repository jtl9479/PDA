# ShipmentListAdapter

## 개요

`ShipmentListAdapter`는 출하대상 목록을 표시하는 BaseAdapter 구현체입니다. 총 172줄의 코드로 구성되어 있으며, ViewHolder 패턴을 사용하여 리스트의 효율적인 렌더링을 제공합니다.

**파일 경로**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\adapter\ShipmentListAdapter.java`

### 주요 기능
- 출하대상 목록 표시
- 단일 선택 모드 (Radio 방식)
- 출하 정보 표시 (업체명, 수량, 중량, BL번호)
- 작업 중 항목 강조 표시
- 전송 완료 여부 시각화

### 데이터 모델
- **아이템 타입**: `Shipments_Info` (출하 정보)
- **체크박스 상태**: `ArrayList<Boolean> cbStatus`

---

## 클래스 구조

### 필드
```java
private Context mContext;                        // 컨텍스트
private int layout;                              // 레이아웃 리소스 ID
public ArrayList<Shipments_Info> arSrc;          // 데이터 소스
public ArrayList<Boolean> cbStatus;              // 체크박스 상태 리스트
LayoutInflater Inflater;                         // 레이아웃 인플레이터
```

---

## 생성자

```java
public ShipmentListAdapter(Context _context, int _layout,
                          ArrayList<Shipments_Info> _arSrc, Handler _handler)
```

**파라미터**:
- `_context`: 애플리케이션 컨텍스트
- `_layout`: 아이템 레이아웃 리소스 ID
- `_arSrc`: 출하 정보 데이터 리스트
- `_handler`: 핸들러 (현재 미사용이지만 파라미터로 전달)

**동작**:
- 모든 아이템의 체크박스 상태를 초기화 (false)
- LayoutInflater 초기화
- 디버그 모드일 경우 데이터 개수 로깅

---

## 주요 메서드

### BaseAdapter 필수 메서드

#### getCount()
```java
@Override
public int getCount()
```
- 데이터 소스의 아이템 개수 반환

#### getItem(int position)
```java
@Override
public Object getItem(int position)
```
- 특정 위치의 아이템 반환

#### getItemId(int position)
```java
@Override
public long getItemId(int position)
```
- 아이템 ID 반환 (position 값 사용)

### 데이터 관리 메서드

#### remove(int position)
```java
public void remove(int position)
```
**동작**:
1. 지정된 위치의 아이템 삭제
2. 해당 체크박스 상태도 함께 삭제
3. 디버그 로그 출력

**특징**:
- DetailAdapter와 달리 순차적 삭제 방식 사용
- BOX_CNT 재정렬 로직 없음

### UI 렌더링 메서드

#### getView(int position, View convertView, ViewGroup parent)
```java
@Override
public View getView(int position, View convertView, ViewGroup parent)
```

**ViewHolder 패턴 구현**:
```java
if (convertView == null) {
    convertView = Inflater.inflate(layout, parent, false);
    holder = new ViewHolder();
    holder.no = (TextView) convertView.findViewById(R.id.no);
    holder.position = (TextView) convertView.findViewById(R.id.position);
    holder.count = (TextView) convertView.findViewById(R.id.count);
    holder.weight = (TextView) convertView.findViewById(R.id.weight);
    holder.bl = (TextView) convertView.findViewById(R.id.bl);
    convertView.setTag(holder);
} else {
    holder = (ViewHolder) convertView.getTag();
}
```

**배경색 처리 우선순위**:
1. 기본 교차 색상 (짝수: 흰색, 홀수: 연한 파랑 `#BBDEFB`)
2. 작업 중 항목: 주황색 (`#FFA726`) - `WORK_FLAG == 1`
3. 전송 완료 항목: 노란색 (`#FFF9C4`) - `SAVE_TYPE == "Y"`
4. 선택된 항목: 청록색 (`#80CBC4`)

**아이템 클릭 리스너 (단일 선택 모드)**:
```java
convertView.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
        if (cbStatus.get(pos)) {
            // 이미 선택된 항목 클릭 시 선택 해제
            cbStatus.set(pos, false);
            notifyDataSetChanged();
        } else {
            // 다른 항목 선택 시 모든 선택 해제 후 해당 항목만 선택
            for (int i = 0; i < cbStatus.size(); i++) {
                cbStatus.set(i, false);
            }
            cbStatus.set(pos, true);
            notifyDataSetChanged();
        }
    }
});
```

**데이터 바인딩**:
```java
try {
    // 순번 (1부터 시작)
    holder.no.setText(String.valueOf(pos + 1));

    // 업체명
    holder.position.setText(arSrc.get(pos).getCLIENTNAME());

    // 수량 정보 (요청수량/포장수량)
    holder.count.setText(arSrc.get(pos).getGI_REQ_PKG() + "/" +
                        arSrc.get(pos).getPACKING_QTY());

    // 중량 정보 (검색 타입에 따라 다름)
    if (Common.searchType.equals("3")) {
        holder.weight.setText("" + arSrc.get(pos).getGI_QTY());
    } else {
        holder.weight.setText(arSrc.get(pos).getGI_REQ_QTY() + "/" +
                            arSrc.get(pos).getGI_QTY());
    }

    // BL 번호 (마지막 4자리만 표시)
    if (arSrc.get(pos).getBL_NO().equals("")) {
        holder.bl.setText("");
    } else {
        holder.bl.setText(arSrc.get(pos).getBL_NO()
            .substring(arSrc.get(pos).getBL_NO().length() - 4,
                      arSrc.get(pos).getBL_NO().length()));
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

---

## ViewHolder 구조

```java
static class ViewHolder {
    TextView no;         // 순번
    TextView position;   // 업체명 (거래처명)
    TextView count;      // 수량 (요청수량/포장수량)
    TextView weight;     // 중량 (요청중량/실제중량)
    TextView bl;         // BL 번호 (마지막 4자리)
}
```

---

## 사용 방법

### 1. Adapter 생성
```java
ArrayList<Shipments_Info> shipmentList = new ArrayList<>();
Handler handler = new Handler();  // 현재는 미사용

ShipmentListAdapter adapter = new ShipmentListAdapter(
    context,
    R.layout.item_shipment_layout,  // 아이템 레이아웃
    shipmentList,
    handler
);
```

### 2. ListView에 설정
```java
ListView listView = findViewById(R.id.listView);
listView.setAdapter(adapter);
```

### 3. 선택된 아이템 가져오기
```java
// 단일 선택 모드이므로 선택된 항목은 최대 1개
int selectedPosition = -1;
for (int i = 0; i < adapter.cbStatus.size(); i++) {
    if (adapter.cbStatus.get(i)) {
        selectedPosition = i;
        break;
    }
}

if (selectedPosition != -1) {
    Shipments_Info selectedItem = adapter.arSrc.get(selectedPosition);
    // 선택된 아이템 처리
}
```

### 4. 아이템 삭제
```java
// 특정 위치의 아이템 삭제
adapter.remove(position);
adapter.notifyDataSetChanged();
```

### 5. 선택 초기화
```java
// 모든 선택 해제
for (int i = 0; i < adapter.cbStatus.size(); i++) {
    adapter.cbStatus.set(i, false);
}
adapter.notifyDataSetChanged();
```

---

## 색상 코드

| 상태 | 색상 코드 | 설명 | 우선순위 |
|------|----------|------|---------|
| 짝수 행 | `#FFFFFF` | 흰색 | 1 (기본) |
| 홀수 행 | `#BBDEFB` | 연한 파랑 | 1 (기본) |
| 작업 중 | `#FFA726` | 주황색 | 2 |
| 전송 완료 | `#FFF9C4` | 노란색 | 3 |
| 선택됨 | `#80CBC4` | 청록색 | 4 (최우선) |

---

## 데이터 표시 형식

### 수량 정보 (count)
```
요청수량/포장수량
예: "10/12"
```

### 중량 정보 (weight)
- **검색 타입 "3"**: 실제 중량만 표시
  ```
  예: "500"
  ```
- **기타 검색 타입**: 요청중량/실제중량 표시
  ```
  예: "480/500"
  ```

### BL 번호 (bl)
- 전체 BL 번호 중 마지막 4자리만 표시
  ```
  원본: "ABCD1234567890"
  표시: "7890"
  ```

---

## 특징 및 주의사항

### 특징
1. **단일 선택 모드**:
   - Radio Button 방식으로 동작
   - 한 번에 하나의 항목만 선택 가능
   - 선택된 항목 다시 클릭 시 선택 해제

2. **작업 상태 표시**:
   - `WORK_FLAG == 1`: 현재 작업 중인 항목을 주황색으로 강조

3. **검색 타입별 중량 표시**:
   - `Common.searchType`에 따라 중량 정보 표시 형식 변경

4. **BL 번호 간소화**:
   - 긴 BL 번호의 마지막 4자리만 표시하여 가독성 향상

### 주의사항
1. **선택 모드**:
   - 단일 선택만 가능 (복수 선택 불가)
   - 다중 선택이 필요한 경우 로직 수정 필요

2. **검색 타입 확인**:
   - `Common.searchType` 값에 따라 중량 표시 형식이 달라짐
   - 검색 타입 설정 필수

3. **예외 처리**:
   - 데이터 바인딩 시 try-catch로 예외 처리
   - BL 번호 substring 시 길이 체크 필요

4. **Handler 미사용**:
   - 생성자에서 Handler를 받지만 현재 코드에서는 사용하지 않음

### DetailAdapter와의 차이점

| 항목 | DetailAdapter | ShipmentListAdapter |
|------|---------------|---------------------|
| 선택 모드 | 다중 선택 (CheckBox) | 단일 선택 (Radio) |
| 삭제 방식 | 역순 삭제 | 순차 삭제 |
| 재출력 기능 | O (Button) | X |
| 작업 상태 표시 | X | O (WORK_FLAG) |
| BOX_CNT 재정렬 | O | X |
| Handler 사용 | O (재출력) | X (미사용) |

---

## 관련 클래스
- `Shipments_Info`: 출하 정보 모델
  - `CLIENTNAME`: 업체명
  - `GI_REQ_PKG`: 요청 수량
  - `PACKING_QTY`: 포장 수량
  - `GI_REQ_QTY`: 요청 중량
  - `GI_QTY`: 실제 중량
  - `BL_NO`: BL 번호
  - `WORK_FLAG`: 작업 중 플래그 (0/1)
  - `SAVE_TYPE`: 전송 완료 여부 ("Y"/"N")
- `Common`: 전역 설정 및 상수 관리
  - `searchType`: 검색 타입 ("3" 등)
  - `D`: 디버그 모드 플래그
