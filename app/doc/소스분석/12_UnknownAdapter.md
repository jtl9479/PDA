# UnknownAdapter

## 개요

`UnknownAdapter`는 상품 코드와 상품명을 간단하게 표시하는 BaseAdapter 구현체입니다. 총 93줄의 경량 코드로 구성되어 있으며, ViewHolder 패턴을 사용합니다.

**파일 경로**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\adapter\UnknownAdapter.java`

### 주요 기능
- 상품 코드와 상품명의 단순 목록 표시
- 교차 배경색으로 가독성 향상
- 최소한의 기능만 제공하는 경량 어댑터

### 데이터 모델
- **아이템 타입**: `ArrayList<String[]>` (2차원 배열)
  - `String[0]`: 상품 코드 (ppcode)
  - `String[1]`: 상품명 (ppname)

---

## 용도 분석

### 추정 용도
파일명 "UnknownAdapter"와 코드 구조를 분석한 결과, 다음과 같은 용도로 사용되는 것으로 추정됩니다:

1. **미식별 상품 목록**:
   - 바코드 스캔 시 시스템에 등록되지 않은 상품 표시
   - 포장 코드가 매칭되지 않는 상품 리스트

2. **검색/조회 결과 표시**:
   - 상품 코드 검색 결과 간단 표시
   - 상품명 검색 결과 표시

3. **간단한 선택 목록**:
   - 상품 선택을 위한 단순 목록
   - 팝업이나 다이얼로그에서 사용

### 특징
- **선택 기능 없음**: CheckBox나 Radio 기능 미제공
- **추가 정보 없음**: 상품 코드와 이름만 표시
- **상호작용 최소화**: 클릭 이벤트 핸들러 없음

---

## 클래스 구조

### 필드
```java
private Context mContext;                        // 컨텍스트
private int layout;                              // 레이아웃 리소스 ID
public ArrayList<String[]> arSrc;                // 데이터 소스 (2차원 배열)
LayoutInflater Inflater;                         // 레이아웃 인플레이터
```

---

## 생성자

```java
public UnknownAdapter(Context _context, int _layout, ArrayList<String[]> _arSrc)
```

**파라미터**:
- `_context`: 애플리케이션 컨텍스트
- `_layout`: 아이템 레이아웃 리소스 ID
- `_arSrc`: 상품 정보 데이터 리스트 (String[] 형태)

**동작**:
- LayoutInflater 초기화
- 체크박스 상태 관리 없음 (다른 어댑터와 차이점)

**특징**:
- Handler 파라미터 없음
- 초기화 시 예외 처리 없음
- 체크박스 상태 리스트 미사용

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
- 특정 위치의 String[] 배열 반환

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
- 지정된 위치의 아이템 삭제
- 디버그 로그 출력 (":: sAdapter remove ::")

**특징**:
- 단순 삭제만 수행
- BOX_CNT 재정렬 없음
- 체크박스 상태 삭제 로직 없음

### UI 렌더링 메서드

#### getView(int position, View convertView, ViewGroup parent)
```java
@Override
public View getView(int position, View convertView, ViewGroup parent)
```

**ViewHolder 패턴 구현**:
```java
if (convertView == null) {
    if (Common.D) {
        Log.d(TAG, "convertView Inflate");
        Log.d(TAG, "position : " + pos);
    }
    convertView = Inflater.inflate(layout, parent, false);
    holder = new ViewHolder();
    holder.ppcode = (TextView) convertView.findViewById(R.id.ppcode);
    holder.ppname = (TextView) convertView.findViewById(R.id.ppname);
    convertView.setTag(holder);
} else {
    holder = (ViewHolder) convertView.getTag();
}
```

**배경색 처리**:
```java
if ((pos % 2) != 0) {
    convertView.setBackgroundColor(Color.parseColor("#BBDEFB"));  // 홀수: 연한 파랑
} else {
    convertView.setBackgroundColor(Color.WHITE);                   // 짝수: 흰색
}
```

**데이터 바인딩**:
```java
holder.ppcode.setText(arSrc.get(pos)[0]);    // 상품 코드
holder.ppname.setText(arSrc.get(pos)[1]);    // 상품명
```

**특징**:
- 클릭 이벤트 없음
- 상태 변경 없음 (선택, 전송 완료 등)
- 단순 데이터 표시만 수행

---

## ViewHolder 구조

```java
static class ViewHolder {
    TextView ppcode;     // 상품 코드 (Packer Product Code)
    TextView ppname;     // 상품명 (Product Name)
}
```

---

## 사용 방법

### 1. 데이터 준비
```java
ArrayList<String[]> unknownProducts = new ArrayList<>();

// 상품 추가 (String[0]: 상품코드, String[1]: 상품명)
unknownProducts.add(new String[]{"P001", "사과"});
unknownProducts.add(new String[]{"P002", "배"});
unknownProducts.add(new String[]{"P003", "감"});
```

### 2. Adapter 생성
```java
UnknownAdapter adapter = new UnknownAdapter(
    context,
    R.layout.item_unknown_layout,  // 아이템 레이아웃
    unknownProducts
);
```

### 3. ListView에 설정
```java
ListView listView = findViewById(R.id.listView);
listView.setAdapter(adapter);
```

### 4. 아이템 클릭 처리 (외부에서 구현)
```java
listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String[] selectedItem = adapter.arSrc.get(position);
        String productCode = selectedItem[0];
        String productName = selectedItem[1];

        // 선택된 상품 처리
        Toast.makeText(context,
            "선택: " + productCode + " - " + productName,
            Toast.LENGTH_SHORT).show();
    }
});
```

### 5. 아이템 추가
```java
adapter.arSrc.add(new String[]{"P004", "귤"});
adapter.notifyDataSetChanged();
```

### 6. 아이템 삭제
```java
adapter.remove(position);
adapter.notifyDataSetChanged();
```

---

## 색상 코드

| 상태 | 색상 코드 | 설명 |
|------|----------|------|
| 짝수 행 | `#FFFFFF` | 흰색 |
| 홀수 행 | `#BBDEFB` | 연한 파랑 |

---

## 레이아웃 예시

### item_unknown_layout.xml (추정)
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="8dp">

    <TextView
        android:id="@+id/ppcode"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:textSize="16sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/ppname"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="2"
        android:textSize="14sp" />

</LinearLayout>
```

---

## 특징 및 주의사항

### 특징
1. **경량 구조**:
   - 최소한의 기능만 제공
   - 코드 라인 수가 93줄로 가장 짧음

2. **단순 데이터 표시**:
   - 상품 코드와 이름만 표시
   - 추가적인 상태 관리 없음

3. **이벤트 처리 없음**:
   - Adapter 내부에서 클릭 이벤트 처리하지 않음
   - ListView의 OnItemClickListener로 외부에서 처리 필요

4. **ViewHolder 패턴**:
   - 성능 최적화를 위해 ViewHolder 패턴 적용
   - 디버그 모드에서만 inflate 로그 출력

### 주의사항
1. **데이터 구조**:
   - String[] 배열 사용 시 인덱스 0, 1만 사용
   - 배열 크기가 2 미만이면 예외 발생 가능

2. **예외 처리 없음**:
   - 다른 어댑터와 달리 try-catch 미사용
   - 잘못된 데이터 입력 시 크래시 가능성

3. **이벤트 처리**:
   - Adapter 자체에 클릭 이벤트 없음
   - ListView의 OnItemClickListener 필수 구현

4. **선택 기능 없음**:
   - CheckBox나 Radio 기능 미제공
   - 선택 상태 표시 필요 시 수정 필요

### 다른 Adapter와의 비교

| 항목 | DetailAdapter | ShipmentListAdapter | UnknownAdapter |
|------|---------------|---------------------|----------------|
| 줄 수 | 200줄 | 172줄 | 93줄 |
| 데이터 타입 | Goodswets_Info | Shipments_Info | String[] |
| 선택 기능 | 다중 선택 | 단일 선택 | 없음 |
| 버튼 | 재출력 버튼 | 없음 | 없음 |
| 클릭 이벤트 | O (내부) | O (내부) | X (외부) |
| Handler | O (사용) | O (미사용) | X |
| 예외 처리 | O | O | X |
| 상태 표시 | 전송완료 | 작업중, 전송완료 | 없음 |
| 용도 | 계근 상세 | 출하 목록 | 간단한 목록 |

---

## 개선 제안

### 1. 예외 처리 추가
```java
@Override
public View getView(int position, View convertView, ViewGroup parent) {
    try {
        // 기존 코드
        holder.ppcode.setText(arSrc.get(pos)[0]);
        holder.ppname.setText(arSrc.get(pos)[1]);
    } catch (ArrayIndexOutOfBoundsException e) {
        Log.e(TAG, "Invalid data format at position: " + pos);
        holder.ppcode.setText("");
        holder.ppname.setText("");
    }
    return convertView;
}
```

### 2. 클릭 이벤트 인터페이스 추가 (선택사항)
```java
public interface OnItemClickListener {
    void onItemClick(String productCode, String productName, int position);
}

private OnItemClickListener mListener;

public void setOnItemClickListener(OnItemClickListener listener) {
    this.mListener = listener;
}

// getView 내부에서
convertView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onItemClick(arSrc.get(pos)[0], arSrc.get(pos)[1], pos);
        }
    }
});
```

### 3. 데이터 모델 객체 사용 (권장)
```java
// String[] 대신 Product 클래스 사용
public class Product {
    private String code;
    private String name;

    // getter, setter, constructor
}

public ArrayList<Product> arSrc;
```

---

## 관련 클래스
- `Common`: 전역 설정 및 상수 관리
  - `D`: 디버그 모드 플래그
- 별도의 데이터 모델 클래스 미사용 (String[] 직접 사용)

---

## 결론

`UnknownAdapter`는 이름과 달리 상품 코드와 상품명을 간단하게 표시하는 **범용 목록 어댑터**입니다. 복잡한 기능 없이 단순 데이터 표시에 특화되어 있으며, 다음과 같은 경우에 적합합니다:

- 미등록 상품 목록 표시
- 상품 검색 결과 표시
- 간단한 선택 목록 (다이얼로그 등)
- 프로토타입 또는 임시 목록

추가 기능이 필요한 경우 `DetailAdapter`나 `ShipmentListAdapter`를 참고하여 확장할 수 있습니다.
