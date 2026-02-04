# ProgressDlgGoodsWetSearch.java

## 개요

`ProgressDlgGoodsWetSearch`는 계근(計斤, 무게 측정) 데이터를 서버에서 조회하여 로컬 SQLite 데이터베이스에 저장하는 AsyncTask 클래스입니다. 출하대상 목록에 포함된 상품들의 실제 계근 정보를 가져오며, 바코드 정보가 없는 상품 목록을 다이얼로그로 표시합니다.

- **파일 경로**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\common\ProgressDlgGoodsWetSearch.java`
- **패키지**: `com.rgbsolution.highland_emart.common`
- **총 라인 수**: 188줄
- **주요 기능**: 계근 데이터 조회 및 미등록 바코드 알림

## 클래스 구조

```java
public class ProgressDlgGoodsWetSearch extends AsyncTask<Integer, String, Integer>
```

### 주요 멤버 변수

```java
private static final String TAG = "pDlgGoodsWetSearch";
private ProgressDialog pDialog;                            // 진행 상태 다이얼로그
private ArrayList<Goodswets_Info> arGI;                   // 계근정보 목록
private LayoutInflater Inflater;                          // 레이아웃 인플레이터
private View dlog_unknownLayout;                          // 미등록 바코드 다이얼로그 레이아웃
private AlertDialog unKnownDialog;                        // 미등록 바코드 다이얼로그
private UnknownAdapter unknownAdapter;                    // 미등록 상품 어댑터
private ListView unknownList;                             // 미등록 상품 리스트뷰
private ArrayList<String[]> list_unknown;                 // 미등록 바코드 목록
private Context mContext;                                 // Context
String receiveData = "";                                  // 조회결과 데이터
```

## AsyncTask 메서드

### 1. onPreExecute()

백그라운드 작업 시작 전 UI 스레드에서 실행되며, 진행 다이얼로그를 표시합니다.

```java
@Override
protected void onPreExecute() {
    pDialog = new ProgressDialog(mContext);
    pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    pDialog.setTitle("계근정보 조회중 입니다.");
    pDialog.setMessage("잠시만 기다려 주세요..");
    pDialog.setCancelable(false);
    pDialog.show();
    super.onPreExecute();
}
```

**특징**:
- HORIZONTAL 스타일의 프로그레스 바 사용
- 취소 불가능하게 설정
- 진행률 표시 가능

### 2. doInBackground(Integer... params)

백그라운드 스레드에서 실행되며, 서버에서 계근 데이터를 조회하고 로컬 DB에 저장합니다.

#### 주요 처리 단계

1. **PDA의 GI_D_ID 목록 조회**

```java
ArrayList<String> list_id_info = DBHandler.selectqueryGIDIDList(mContext);
```

PDA에 저장된 출하대상의 GI_D_ID 목록을 조회합니다.

2. **WHERE 절 조건 구성**

```java
String data = " WHERE ";
for (int i = 0; i < list_id_info.size(); i++) {
    if (i == list_id_info.size() - 1) {
        data = data + "GI_D_ID = '" + list_id_info.get(i).toString() + "'";
    } else {
        data = data + "GI_D_ID = '" + list_id_info.get(i).toString() + "' OR ";
    }
}

if(list_id_info.size() == 0){
    data = data + "1=0";  // 목록이 없을 경우 조회되지 않도록
}
```

**예시 결과**:
```sql
WHERE GI_D_ID = '12345' OR GI_D_ID = '12346' OR GI_D_ID = '12347'
```

3. **서버 통신**

```java
receiveData = HttpHelper.getInstance().sendDataDb(
    data,
    "inno",
    "search_goods_wet",
    Common.URL_SEARCH_GOODS_WET
);
```

4. **데이터 파싱**

```java
// 결과값 정리
receiveData = receiveData.replace("\r\n", "");
receiveData = receiveData.replace("\n", "");

// row별로 split (구분자: ;;)
String[] result = receiveData.split(";;");
arGI = new ArrayList<Goodswets_Info>();

// 최대값 설정 (프로그레스 바)
publishProgress("max", Integer.toString(result.length));

if (receiveData.toString() != "" && result.length > 0) {
    for (String s : result) {
        // 각 row의 데이터별로 split (구분자: ::)
        temp = s.split("::");
        gi = new Goodswets_Info();
        gi.setGI_D_ID(temp[0].toString());
        gi.setWEIGHT(temp[1].toString());
        gi.setWEIGHT_UNIT(temp[2].toString());
        gi.setPACKER_PRODUCT_CODE(temp[3].toString());
        gi.setBARCODE(temp[4].toString());
        gi.setPACKER_CLIENT_CODE(temp[5].toString());
        gi.setBOX_CNT(temp[6].toString());
        gi.setREG_ID(temp[7].toString());
        gi.setREG_DATE(temp[8].toString());
        gi.setREG_TIME(temp[9].toString());
        gi.setMAKINGDATE(temp[10].toString());
        gi.setBOXSERIAL(temp[11].toString());
        gi.setBOX_ORDER(temp[12].toString());
        gi.setSAVE_TYPE("Y");

        // 계근정보 내부 SQLite에 INSERT
        DBHandler.insertqueryGoodsWet(mContext, gi);
        arGI.add(gi);

        // 진행률 업데이트
        publishProgress("progress",
            Integer.toString(arGI.size()),
            Integer.toString(arGI.size()) + "번 데이터 저장중.."
        );
    }
}
```

**데이터 필드 (13개)**:
- `GI_D_ID` (0): 출하 상세 ID
- `WEIGHT` (1): 무게
- `WEIGHT_UNIT` (2): 무게 단위
- `PACKER_PRODUCT_CODE` (3): 포장업체 상품 코드
- `BARCODE` (4): 바코드
- `PACKER_CLIENT_CODE` (5): 포장업체 거래처 코드
- `BOX_CNT` (6): 박스 개수
- `REG_ID` (7): 등록자 ID
- `REG_DATE` (8): 등록 날짜
- `REG_TIME` (9): 등록 시간
- `MAKINGDATE` (10): 제조일자
- `BOXSERIAL` (11): 박스 시리얼
- `BOX_ORDER` (12): 박스 순서

**반환값**: `0` (성공)

### 3. onProgressUpdate(String... progress)

진행 상태 업데이트를 처리합니다.

```java
@Override
protected void onProgressUpdate(String... progress) {
    if (progress[0].equals("progress")) {
        pDialog.setProgress(Integer.parseInt(progress[1]));
        pDialog.setMessage(progress[2]);
    } else if (progress[0].equals("max")) {
        pDialog.setMax(Integer.parseInt(progress[1]));
    }
}
```

**업데이트 방식**:
- `"max"`: 프로그레스 바의 최대값 설정
- `"progress"`: 현재 진행률과 메시지 업데이트

### 4. onPostExecute(Integer result)

백그라운드 작업 완료 후 UI 스레드에서 실행되며, 바코드 정보가 없는 상품 목록을 다이얼로그로 표시합니다.

```java
@Override
protected void onPostExecute(Integer result) {
    pDialog.dismiss();

    if (mContext.toString().split("[@]")[0].equals("com.highland.LoginActivity")) {
        if (Common.D) {
            Log.d(TAG, "Context : LoginActivity");
            Log.d(TAG, "search_bool : " + Common.search_bool);
        }
    }

    show_unknownDialog(list_unknown);
}
```

**특징**:
- 진행 다이얼로그 닫기
- Context 검증
- 미등록 바코드 다이얼로그 표시

## 미등록 바코드 다이얼로그

### show_unknownDialog(ArrayList<String[]> list_unknown)

바코드 정보가 없는 상품 목록을 다이얼로그로 표시하는 메서드입니다.

```java
public void show_unknownDialog(ArrayList<String[]> list_unknown) {
    try {
        Inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dlog_unknownLayout = Inflater.inflate(R.layout.dialog_unknown, null);

        // 다이얼로그 빌더 설정
        final AlertDialog.Builder dlog = new AlertDialog.Builder(mContext, R.style.AppCompatDialogStyle)
                .setCancelable(true);
        dlog.setTitle("바코드정보가 없는 상품목록");
        dlog.setView(dlog_unknownLayout);
        unKnownDialog = dlog.create();
        unKnownDialog.show();

        // 어댑터 및 리스트뷰 설정
        unknownAdapter = new UnknownAdapter(mContext, R.layout.list_unknown, list_unknown);
        unknownList = (ListView) dlog_unknownLayout.findViewById(R.id.dlog_unknown);
        unknownList.setAdapter(unknownAdapter);
        unknownAdapter.notifyDataSetChanged();

    } catch (Exception e) {
        if (Common.D) {
            Log.d(TAG, "Error 1001 : " + e.getMessage().toString());
        }
    }
}
```

**다이얼로그 구성**:
- 제목: "바코드정보가 없는 상품목록"
- 레이아웃: `R.layout.dialog_unknown`
- 리스트 아이템: `R.layout.list_unknown`
- 취소 가능

**list_unknown 데이터 구조**:
```java
String[] item = new String[2];
item[0] = PACKER_PRODUCT_CODE;  // 포장업체 상품 코드
item[1] = ITEM_NAME;             // 상품명
```

## 서버 통신

### 요청 형식

```java
receiveData = HttpHelper.getInstance().sendDataDb(
    data,                          // WHERE 절
    "inno",                        // 데이터베이스명
    "search_goods_wet",            // 액션
    Common.URL_SEARCH_GOODS_WET    // URL
);
```

**요청 예시**:
```sql
WHERE GI_D_ID = '12345' OR GI_D_ID = '12346' OR GI_D_ID = '12347'
```

### 응답 형식

```
GI_D_ID::WEIGHT::WEIGHT_UNIT::PACKER_PRODUCT_CODE::BARCODE::PACKER_CLIENT_CODE::BOX_CNT::REG_ID::REG_DATE::REG_TIME::MAKINGDATE::BOXSERIAL::BOX_ORDER;;
GI_D_ID::WEIGHT::WEIGHT_UNIT::PACKER_PRODUCT_CODE::BARCODE::PACKER_CLIENT_CODE::BOX_CNT::REG_ID::REG_DATE::REG_TIME::MAKINGDATE::BOXSERIAL::BOX_ORDER;;
...
```

- **Row 구분자**: `;;`
- **Column 구분자**: `::`
- **필드 개수**: 13개

**응답 예시**:
```
12345::15.5::KG::PACK001::8801234567890::CLIENT001::10::USER01::20231031::143000::20231030::BOX001::1;;
12346::18.2::KG::PACK002::8801234567891::CLIENT002::12::USER01::20231031::143100::20231030::BOX002::2;;
```

## 사용 방법

### 기본 사용

```java
// 1. 미등록 바코드 목록 준비 (ProgressDlgBarcodeSearch에서 전달받음)
ArrayList<String[]> list_unknown = new ArrayList<>();

// 2. AsyncTask 실행
new ProgressDlgGoodsWetSearch(context, list_unknown).execute();
```

### 실제 사용 예제

일반적으로 `ProgressDlgBarcodeSearch`의 `onPostExecute`에서 호출됩니다:

```java
// ProgressDlgBarcodeSearch.java의 onPostExecute()
@Override
protected void onPostExecute(Integer result) {
    pDialog.dismiss();

    // 바코드 정보가 없는 상품 목록 생성
    list_unknown_bi = new ArrayList<String[]>();
    for (String[] s : list_code_info) {
        int iCount = 0;
        String req_code = s[0];
        for (int i = 0; i < list_received_bi.size(); i++) {
            if (req_code.equals(list_received_bi.get(i).getPACKER_PRODUCT_CODE())) {
                break;
            }
            iCount++;
        }
        if (iCount == list_received_bi.size()) {
            String[] temp = new String[2];
            temp[0] = req_code;           // PACKER_PRODUCT_CODE
            temp[1] = s[1].toString();    // ITEM_NAME
            list_unknown_bi.add(temp);
        }
    }

    // 계근정보 조회 시작
    new ProgressDlgGoodsWetSearch(mContext, list_unknown_bi).execute();
}
```

### 단독 사용 예제

```java
// 미등록 바코드 목록 생성
ArrayList<String[]> unknownList = new ArrayList<>();
String[] item1 = {"PACK001", "사과 1kg"};
String[] item2 = {"PACK002", "배 1.5kg"};
unknownList.add(item1);
unknownList.add(item2);

// 계근정보 조회 시작
ProgressDlgGoodsWetSearch task = new ProgressDlgGoodsWetSearch(this, unknownList);
task.execute();
```

## 데이터 흐름

```
1. onPreExecute()
   ↓
2. doInBackground()
   ├─ PDA의 GI_D_ID 목록 조회
   ├─ WHERE 절 조건 구성
   ├─ 서버 통신
   ├─ 데이터 파싱
   ├─ 로컬 DB 저장
   └─ 진행률 업데이트
   ↓
3. onPostExecute()
   └─ show_unknownDialog()
       └─ 미등록 바코드 다이얼로그 표시
```

## 주요 특징

1. **진행률 표시**: HORIZONTAL 스타일로 실시간 진행률 표시
2. **미등록 바코드 알림**: 바코드 정보가 없는 상품을 다이얼로그로 표시
3. **실시간 저장**: 데이터를 받으면서 동시에 로컬 DB에 저장
4. **커스텀 어댑터**: UnknownAdapter를 통한 커스텀 리스트 표시
5. **상세 로깅**: 각 단계별 상세한 로그 출력

## 관련 클래스

- `Goodswets_Info`: 계근정보 데이터 모델
- `DBHandler`: SQLite 데이터베이스 핸들러
- `HttpHelper`: HTTP 통신 헬퍼
- `Common`: 공통 상수 및 변수
- `UnknownAdapter`: 미등록 상품 리스트 어댑터
- `ProgressDlgBarcodeSearch`: 이전 단계 AsyncTask

## 레이아웃 리소스

### dialog_unknown.xml
미등록 바코드 다이얼로그의 레이아웃 파일

```xml
<!-- 예상 구조 -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout>
    <ListView
        android:id="@+id/dlog_unknown"
        ... />
</LinearLayout>
```

### list_unknown.xml
미등록 바코드 리스트 아이템의 레이아웃 파일

```xml
<!-- 예상 구조 -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout>
    <TextView
        android:id="@+id/txt_packer_product_code"
        ... />
    <TextView
        android:id="@+id/txt_item_name"
        ... />
</LinearLayout>
```

## 주의사항

1. **Context 필수**: 생성자에 Context 전달 필수
2. **미등록 목록 필수**: 생성자에 list_unknown 전달 필수
3. **사전 데이터 필요**: PDA에 출하대상 데이터가 있어야 함 (GI_D_ID)
4. **UI 스레드에서 실행**: Activity나 Fragment의 Context에서 실행해야 함
5. **네트워크 권한**: 인터넷 권한 필요
6. **메모리 고려**: 대량 데이터 처리 시 메모리 사용량 고려
7. **다이얼로그 취소**: 미등록 바코드 다이얼로그는 취소 가능
8. **순차 실행**: 일반적으로 ProgressDlgBarcodeSearch 이후 실행됨

## 에러 처리

```java
try {
    // 데이터 처리
} catch (Exception e) {
    if (Common.D) {
        Log.e(TAG, "e : " + e.toString());
    }
}
```

모든 예외는 로그로 출력되며, 앱이 중단되지 않도록 처리됩니다.

## 디버그 모드

`Common.D` 플래그를 통해 디버그 로그를 제어할 수 있습니다:

```java
if (Common.D) {
    Log.d(TAG, "Wet receiveData : " + receiveData.toString());
    Log.d(TAG, "Wet result's Count : " + result.length);
}
```
