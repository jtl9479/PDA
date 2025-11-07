# ProgressDlgShipSearch.java

## 개요

`ProgressDlgShipSearch`는 출하대상 목록을 서버에서 조회하여 로컬 SQLite 데이터베이스에 저장하는 AsyncTask 클래스입니다. 다양한 출하 유형(이마트, 홈플러스, 롯데, 일반도매, 생산대상 등)을 지원하며, 서버에서 받은 데이터를 파싱하여 PDA의 기존 데이터와 비교한 후 변경사항을 반영합니다.

- **파일 경로**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\common\ProgressDlgShipSearch.java`
- **패키지**: `com.rgbsolution.highland_emart.common`
- **총 라인 수**: 322줄
- **주요 기능**: 출하대상 조회 및 로컬 DB 동기화

## 클래스 구조

```java
public class ProgressDlgShipSearch extends AsyncTask<Integer, String, Integer>
```

### 주요 멤버 변수

```java
private static final String TAG = "ProgressDlgShipSearch";
ProgressDialog pDialog;                                    // 진행 상태 다이얼로그
ArrayList<Shipments_Info> list_si_info;                   // 서버에서 받은 출하정보 목록
private Context mContext;                                  // Context
String receiveData = "";                                   // 조회결과 데이터
```

## AsyncTask 메서드

### 1. onPreExecute()

백그라운드 작업 시작 전 UI 스레드에서 실행되며, 진행 다이얼로그를 표시합니다.

```java
@Override
protected void onPreExecute() {
    pDialog = new ProgressDialog(mContext);
    pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    pDialog.setTitle("대상 조회중 입니다.");
    pDialog.setMessage("잠시만 기다려 주세요..");
    pDialog.setCancelable(false);
    pDialog.show();
    super.onPreExecute();
}
```

**특징**:
- SPINNER 스타일의 프로그레스 다이얼로그 사용
- 취소 불가능하게 설정

### 2. doInBackground(Integer... params)

백그라운드 스레드에서 실행되며, 서버에서 출하대상 데이터를 조회하고 로컬 DB와 동기화합니다.

#### 주요 처리 단계

1. **PDA 기존 목록 조회**
```java
ArrayList<Shipments_Info> list_si_pda = DBHandler.selectqueryAllShipment(mContext);
Log.d(TAG, "============== PDA List's size : " + list_si_pda.size() + "================");
```

2. **조회 조건 구성**
```java
String data = " WHERE GI_REQ_DATE = '" + Common.selectDay + "'";
```

3. **출하 유형별 서버 통신**

출하 유형은 `Common.searchType`으로 구분됩니다:

| searchType | 설명 | URL | 창고 코드 필터 |
|-----------|------|-----|--------------|
| "0" | 이마트 출하대상 | URL_SEARCH_SHIPMENT | O |
| "1" | 생산대상 | URL_SEARCH_PRODUCTION | X |
| "2" | 홈플러스 | URL_SEARCH_SHIPMENT_HOMEPLUS | O |
| "3" | 일반도매업체 | URL_SEARCH_SHIPMENT_WHOLESALE | O |
| "4" | 비정량계근출고 | URL_SEARCH_PRODUCTION_NONFIXED | X |
| "5" | 홈플러스 비정량계근출고 | URL_SEARCH_HOMEPLUS_NONFIXED | X |
| "6" | 롯데 출하대상 | URL_SEARCH_SHIPMENT_LOTTE | O |
| "7" | 생산대상(라벨) | URL_SEARCH_PRODUCTION_4LABEL | X |

**창고 코드 매핑**:
```java
if(Common.selectWarehouse.equals("삼일냉장")){
    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
}else if(Common.selectWarehouse.equals("SWC")){
    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
}else if(Common.selectWarehouse.equals("이천1센터")){
    data += " AND GR_WAREHOUSE_CODE = '4001'";
}else if(Common.selectWarehouse.equals("부산센터")){
    data += " AND GR_WAREHOUSE_CODE = '4004'";
}else if(Common.selectWarehouse.equals("탑로지스")){
    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
}
```

4. **서버 데이터 파싱**

```java
// 결과값 정리
receiveData = receiveData.replace("\r\n", "");
receiveData = receiveData.replace("\n", "");

// row별로 split (구분자: ;;)
String[] result = receiveData.split(";;");
list_si_info = new ArrayList<Shipments_Info>();

for (String s : result) {
    // 각 row의 데이터별로 split (구분자: ::)
    temp = s.split("::");
    si = new Shipments_Info();
    si.setGI_H_ID(temp[0].toString());
    si.setGI_D_ID(temp[1].toString());
    si.setEOI_ID(temp[2].toString());
    si.setITEM_CODE(temp[3].toString());
    si.setITEM_NAME(temp[4].toString());
    si.setEMARTITEM_CODE(temp[5].toString());
    si.setEMARTITEM(temp[6].toString());
    si.setGI_REQ_PKG(temp[7].toString());
    // ... (총 38개 필드)
    list_si_info.add(si);
}
```

**데이터 필드 (38개)**:
- GI_H_ID, GI_D_ID, EOI_ID (0-2)
- ITEM_CODE, ITEM_NAME, EMARTITEM_CODE, EMARTITEM (3-6)
- GI_REQ_PKG, GI_REQ_QTY, AMOUNT (7-9)
- GOODS_R_ID, GR_REF_NO, GI_REQ_DATE, BL_NO (10-13)
- BRAND_CODE, BRANDNAME, CLIENT_CODE, CLIENTNAME, CENTERNAME (14-18)
- ITEM_SPEC, CT_CODE, IMPORT_ID_NO (19-21)
- PACKER_CODE, PACKERNAME, PACKER_PRODUCT_CODE (22-24)
- BARCODE_TYPE, ITEM_TYPE, PACKWEIGHT, BARCODEGOODS (25-28)
- STORE_IN_DATE, EMARTLOGIS_CODE, EMARTLOGIS_NAME (29-31)
- WH_AREA, USE_NAME, USE_CODE, CT_NAME, STORE_CODE, EMART_PLANT_CODE, LAST_BOX_ORDER (32-37)

5. **수량 정밀도 조정**

소수점 3자리 초과 시 소수점 1자리로 반올림:
```java
String[] split_qty = temp[8].split("[.]");
if (split_qty.length > 1) {
    if (split_qty[1].length() > 3) {
        double double_qty = Double.parseDouble(temp[8].toString());
        double_qty = Math.floor(double_qty * 10);
        double result_qty = double_qty / 10.0;
        temp[8] = String.valueOf(result_qty);
    }
}
si.setGI_REQ_QTY(temp[8].toString());
```

6. **출하 유형별 추가 필드 처리**

```java
if(Common.searchType.equals("0") || Common.searchType.equals("4")) { //이마트 출하계근일때만
    si.setWH_AREA(temp[32].toString());
    si.setUSE_NAME(temp[33].toString());
    si.setUSE_CODE(temp[34].toString());
    si.setCT_NAME(temp[35].toString());
    si.setSTORE_CODE(temp[36].toString());
    si.setEMART_PLANT_CODE(temp[37].toString());
} else if(Common.searchType.equals("5")) {
    si.setWH_AREA(temp[32].toString());
    si.setUSE_NAME(temp[33].toString());
    si.setUSE_CODE(temp[34].toString());
    si.setCT_NAME(temp[35].toString());
    si.setSTORE_CODE(temp[36].toString());
} else if(Common.searchType.equals("6")) {
    si.setWH_AREA(temp[32].toString());
    si.setLAST_BOX_ORDER(temp[33].toString());
}
```

7. **PDA-서버 데이터 비교 및 동기화**

```java
ArrayList<String> list_delete = new ArrayList<String>();
ArrayList<Shipments_Info> list_insert = new ArrayList<Shipments_Info>();

// 삭제 리스트 검색 (PDA에는 있지만 서버에는 없는 데이터)
for (int i = 0; i < list_si_pda.size(); i++) {
    int check = 0;
    for (int j = 0; j < list_si_info.size(); j++) {
        if (list_si_pda.get(i).getGI_D_ID().equals(list_si_info.get(j).getGI_D_ID())) {
            check++;
            break;
        }
    }
    if (check == 0) {
        list_delete.add(list_si_pda.get(i).getGI_D_ID());
    }
}

// 추가 리스트 검색 (서버에는 있지만 PDA에는 없는 데이터)
for (int i = 0; i < list_si_info.size(); i++) {
    int check = 0;
    for (int j = 0; j < list_si_pda.size(); j++) {
        if (list_si_info.get(i).getGI_D_ID().equals(list_si_pda.get(j).getGI_D_ID())) {
            check++;
            break;
        }
    }
    if (check == 0) {
        list_insert.add(list_si_info.get(i));
    }
}

// DB 동기화
boolean refresh_result = DBHandler.refreshShipmentList(mContext, list_delete, list_insert);
```

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

### 4. onPostExecute(Integer result)

백그라운드 작업 완료 후 UI 스레드에서 실행되며, 다음 단계로 바코드 정보 조회를 시작합니다.

```java
@Override
protected void onPostExecute(Integer result) {
    pDialog.dismiss();
    new ProgressDlgBarcodeSearch(mContext).execute();
}
```

**특징**:
- 출하대상 조회 완료 후 자동으로 바코드 정보 조회 시작
- 체인 방식으로 데이터 동기화 진행

## 서버 통신

### 요청 형식

```java
receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT);
```

**파라미터**:
- `data`: WHERE 절 조건문
- `dbName`: "inno" 또는 "highland" (롯데의 경우)
- `action`: "search_shipment"
- `url`: 출하 유형별 URL

### 응답 형식

```
데이터1::데이터2::...::데이터38;;
데이터1::데이터2::...::데이터38;;
...
```

- **Row 구분자**: `;;`
- **Column 구분자**: `::`
- **필드 개수**: 32~38개 (출하 유형에 따라 다름)

### URL 목록

```java
Common.URL_SEARCH_SHIPMENT              // 이마트 출하대상
Common.URL_SEARCH_PRODUCTION            // 생산대상
Common.URL_SEARCH_SHIPMENT_HOMEPLUS     // 홈플러스
Common.URL_SEARCH_SHIPMENT_WHOLESALE    // 일반도매업체
Common.URL_SEARCH_PRODUCTION_NONFIXED   // 비정량계근출고
Common.URL_SEARCH_HOMEPLUS_NONFIXED     // 홈플러스 비정량
Common.URL_SEARCH_SHIPMENT_LOTTE        // 롯데
Common.URL_SEARCH_PRODUCTION_4LABEL     // 생산대상(라벨)
```

## 사용 방법

### 기본 사용

```java
// 1. 출하 유형 설정
Common.searchType = "0";  // 이마트 출하대상

// 2. 날짜 및 창고 설정
Common.selectDay = "20231031";
Common.selectWarehouse = "삼일냉장";

// 3. AsyncTask 실행
new ProgressDlgShipSearch(context).execute();
```

### 출하 유형별 예제

**1. 이마트 출하대상 조회**
```java
Common.searchType = "0";
Common.selectDay = "20231031";
Common.selectWarehouse = "SWC";
new ProgressDlgShipSearch(context).execute();
```

**2. 생산대상 조회**
```java
Common.searchType = "1";
Common.selectDay = "20231031";
new ProgressDlgShipSearch(context).execute();
```

**3. 홈플러스 출하대상 조회**
```java
Common.searchType = "2";
Common.selectDay = "20231031";
Common.selectWarehouse = "이천1센터";
new ProgressDlgShipSearch(context).execute();
```

**4. 롯데 출하대상 조회**
```java
Common.searchType = "6";
Common.selectDay = "20231031";
Common.selectWarehouse = "부산센터";
new ProgressDlgShipSearch(context).execute();
```

**5. 비정량계근출고 조회**
```java
Common.searchType = "4";
Common.selectDay = "20231031";
new ProgressDlgShipSearch(context).execute();
```

## 데이터 흐름

```
1. onPreExecute()
   ↓
2. doInBackground()
   ├─ PDA 기존 목록 조회
   ├─ 조회 조건 구성
   ├─ 서버 통신 (출하 유형별)
   ├─ 데이터 파싱
   ├─ PDA-서버 데이터 비교
   └─ DB 동기화 (삭제/추가)
   ↓
3. onPostExecute()
   └─ ProgressDlgBarcodeSearch 실행
```

## 주요 특징

1. **다중 출하 유형 지원**: 8가지 출하 유형 지원
2. **창고별 필터링**: 5개 창고 코드 지원
3. **데이터 동기화**: 서버와 PDA의 데이터를 비교하여 변경사항만 반영
4. **수량 정밀도 조정**: 소수점 3자리 초과 시 자동 반올림
5. **체인 방식 처리**: 완료 후 자동으로 바코드 정보 조회 시작
6. **상세 로깅**: 각 단계별 상세한 로그 출력

## 관련 클래스

- `Shipments_Info`: 출하정보 데이터 모델
- `DBHandler`: SQLite 데이터베이스 핸들러
- `HttpHelper`: HTTP 통신 헬퍼
- `Common`: 공통 상수 및 변수
- `ProgressDlgBarcodeSearch`: 다음 단계 AsyncTask

## 주의사항

1. **Context 필수**: 생성자에 Context 전달 필수
2. **사전 설정 필요**: `Common.searchType`, `Common.selectDay`, `Common.selectWarehouse` 설정 필요
3. **UI 스레드에서 실행**: Activity나 Fragment의 Context에서 실행해야 함
4. **네트워크 권한**: 인터넷 권한 필요
5. **메모리 고려**: 대량 데이터 처리 시 메모리 사용량 고려
6. **취소 불가**: 다이얼로그가 취소 불가능하므로 백그라운드 처리 완료까지 대기 필요
