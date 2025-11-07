# DetailAdapter

## 개요

`DetailAdapter`는 계근 상세 내역을 표시하는 BaseAdapter 구현체입니다. 총 200줄의 코드로 구성되어 있으며, ViewHolder 패턴을 사용하여 리스트 뷰의 성능을 최적화합니다.

**파일 경로**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\adapter\DetailAdapter.java`

### 주요 기능
- 계근 상세 내역 리스트 표시
- 박스별 정보 출력 (순번, 박스시리얼, 포장코드, 중량, 제조일)
- 항목별 선택 상태 관리 (CheckBox 상태)
- 라벨 재출력 기능
- 전송 완료 여부에 따른 UI 상태 변경

### 데이터 모델
- **아이템 타입**: `Goodswets_Info` (계근 정보)
- **체크박스 상태**: `ArrayList<Boolean> cbStatus`

---

## 클래스 구조

### 필드
```java
private Context mContext;                        // 컨텍스트
private int layout;                              // 레이아웃 리소스 ID
public ArrayList<Goodswets_Info> arSrc;          // 데이터 소스
private Handler mHandler;                        // 메시지 핸들러
public ArrayList<Boolean> cbStatus;              // 체크박스 상태 리스트
LayoutInflater Inflater;                         // 레이아웃 인플레이터
```

### 상수
```java
public static final int MESSAGE_REPRINT = 5;     // 재출력 메시지 코드
```

---

## 생성자

```java
public DetailAdapter(Context _context, int _layout,
                    ArrayList<Goodswets_Info> _arSrc, Handler _handler)
```

**파라미터**:
- `_context`: 애플리케이션 컨텍스트
- `_layout`: 아이템 레이아웃 리소스 ID
- `_arSrc`: 계근 정보 데이터 리스트
- `_handler`: 재출력 이벤트를 처리할 핸들러

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
1. 역순으로 아이템 삭제 (`arSrc.size() - position`)
2. 체크박스 상태도 함께 삭제
3. 삭제된 위치 이후의 모든 아이템의 `BOX_CNT` 값 1씩 감소
4. 디버그 로그 출력

**특징**:
- 역순 삭제 방식 사용
- BOX_CNT 재정렬 로직 포함

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
    holder.boxserial = (TextView) convertView.findViewById(R.id.boxserial);
    holder.ppcode = (TextView) convertView.findViewById(R.id.ppcode);
    holder.weight = (TextView) convertView.findViewById(R.id.weight);
    holder.makingdate = (TextView) convertView.findViewById(R.id.makingdate);
    holder.print = (Button) convertView.findViewById(R.id.reprint);
    convertView.setTag(holder);
} else {
    holder = (ViewHolder) convertView.getTag();
}
```

**배경색 처리**:
1. 기본 교차 색상 (짝수: 흰색, 홀수: 연한 파랑 `#BBDEFB`)
2. 전송 완료 항목: 노란색 (`#FFF9C4`)
3. 선택된 항목: 청록색 (`#80CBC4`)

**이벤트 리스너**:

1. **아이템 클릭 리스너**:
```java
convertView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // 전송 완료된 상품은 삭제 불가
        if (arSrc.get(pos).getSAVE_TYPE().equals("Y")) {
            Toast.makeText(mContext, "전송이 완료된 상품은\n삭제할 수 없습니다.",
                          Toast.LENGTH_SHORT).show();
            return;
        }
        // 체크박스 상태 토글
        cbStatus.set(pos, !cbStatus.get(pos));
        notifyDataSetChanged();
    }
});
```

2. **재출력 버튼 클릭 리스너**:
```java
holder.print.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("WEIGHT", arSrc.get(pos).getWEIGHT());
        bundle.putString("MAKINGDATE", arSrc.get(pos).getMAKINGDATE());
        bundle.putString("BOX_ORDER", arSrc.get(pos).getBOX_ORDER());
        msg.setData(bundle);
        msg.what = MESSAGE_REPRINT;
        mHandler.sendMessage(msg);
    }
});
```

**프린터 설정 처리**:
```java
if (!Common.printer_setting || !Common.print_bool) {
    holder.print.setEnabled(false);
}
```

**데이터 바인딩**:
```java
holder.no.setText(arSrc.get(pos).getBOX_CNT());                    // 순번
holder.boxserial.setText(arSrc.get(pos).getBOXSERIAL());          // 박스시리얼
holder.ppcode.setText(arSrc.get(pos).getPACKER_PRODUCT_CODE());   // 포장코드
holder.weight.setText(arSrc.get(pos).getWEIGHT());                // 중량
holder.makingdate.setText(arSrc.get(pos).getMAKINGDATE());        // 제조일
holder.print.setText("출력");                                       // 출력 버튼
```

---

## ViewHolder 구조

```java
static class ViewHolder {
    TextView no;            // 순번
    TextView boxserial;     // 박스 시리얼 번호
    TextView ppcode;        // 포장 상품 코드
    TextView weight;        // 중량
    TextView makingdate;    // 제조일
    Button print;           // 재출력 버튼
}
```

---

## 사용 방법

### 1. Adapter 생성
```java
ArrayList<Goodswets_Info> dataList = new ArrayList<>();
Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
        if (msg.what == DetailAdapter.MESSAGE_REPRINT) {
            Bundle bundle = msg.getData();
            String weight = bundle.getString("WEIGHT");
            String makingDate = bundle.getString("MAKINGDATE");
            String boxOrder = bundle.getString("BOX_ORDER");
            // 재출력 로직 실행
        }
    }
};

DetailAdapter adapter = new DetailAdapter(
    context,
    R.layout.item_detail_layout,  // 아이템 레이아웃
    dataList,
    handler
);
```

### 2. ListView에 설정
```java
ListView listView = findViewById(R.id.listView);
listView.setAdapter(adapter);
```

### 3. 아이템 삭제
```java
// 선택된 아이템 삭제
for (int i = adapter.cbStatus.size() - 1; i >= 0; i--) {
    if (adapter.cbStatus.get(i)) {
        adapter.remove(i);
    }
}
adapter.notifyDataSetChanged();
```

### 4. 재출력 메시지 처리
```java
@Override
public void handleMessage(Message msg) {
    if (msg.what == DetailAdapter.MESSAGE_REPRINT) {
        Bundle data = msg.getData();
        // 라벨 재출력 로직
        printLabel(
            data.getString("WEIGHT"),
            data.getString("MAKINGDATE"),
            data.getString("BOX_ORDER")
        );
    }
}
```

---

## 색상 코드

| 상태 | 색상 코드 | 설명 |
|------|----------|------|
| 짝수 행 | `#FFFFFF` | 흰색 |
| 홀수 행 | `#BBDEFB` | 연한 파랑 |
| 전송 완료 | `#FFF9C4` | 노란색 |
| 선택됨 | `#80CBC4` | 청록색 |

---

## 특징 및 주의사항

### 특징
1. **ViewHolder 패턴**: 뷰 재사용으로 스크롤 성능 최적화
2. **상태 기반 UI**: 전송 여부, 선택 상태에 따른 배경색 변경
3. **재출력 기능**: Handler를 통한 비동기 메시지 처리
4. **교차 색상**: 가독성 향상을 위한 행별 색상 구분

### 주의사항
1. **전송 완료 항목**: `SAVE_TYPE == "Y"` 인 항목은 삭제 불가
2. **프린터 설정**: `Common.printer_setting`과 `Common.print_bool` 확인 필요
3. **역순 삭제**: `remove()` 메서드가 역순으로 삭제하므로 반복문 사용 시 주의
4. **BOX_CNT 재정렬**: 삭제 시 자동으로 순번 재조정

### 예외 처리
- 생성자와 `remove()` 메서드에서 Exception 처리
- 오류 발생 시 로그 출력

---

## 관련 클래스
- `Goodswets_Info`: 계근 상세 정보 모델
- `Common`: 전역 설정 및 상수 관리
- Handler를 통한 Activity와의 통신
