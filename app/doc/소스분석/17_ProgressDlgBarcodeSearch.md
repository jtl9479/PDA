# ProgressDlgBarcodeSearch.java

## 개요

`ProgressDlgBarcodeSearch`는 출하대상 상품들의 바코드 정보를 서버에서 조회하여 로컬 SQLite 데이터베이스에 저장하는 AsyncTask 클래스입니다. 출하 유형에 따라 다른 URL을 사용하며, 조회 후 바코드 정보가 없는 상품 목록을 추출하여 다음 단계로 전달합니다.

- **파일 경로**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\common\ProgressDlgBarcodeSearch.java`
- **패키지**: `com.rgbsolution.highland_emart.common`
- **총 라인 수**: 201줄
- **주요 기능**: 바코드 정보 조회 및 미등록 상품 추출

## 클래스 구조

```java
public class ProgressDlgBarcodeSearch extends AsyncTask<Integer, String, Integer>
```

### 주요 멤버 변수

```java
private static final String TAG = "pDlgBarcodeSearch";
private ProgressDialog pDialog;                            // 진행 상태 다이얼로그
ArrayList<String[]> list_code_info;                       // PDA의 상품 코드 목록
private ArrayList<Barcodes_Info> list_received_bi;        // 서버에서 받은 바코드 정보
private ArrayList<String[]> list_unknown_bi;              // 바코드 정보가 없는 상품 목록
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
    pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    pDialog.setTitle("바코드정보 조회중 입니다.");
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

백그라운드 스레드에서 실행되며, 서버에서 바코드 정보를 조회하고 로컬 DB에 저장합니다.

#### 주요 처리 단계

1. **PDA의 상품 코드 목록 조회**

```java
if(Common.searchType.equals("4") || Common.searchType.equals("5") ){
    // 비정량계근출고: ITEM_CODE 기준
    list_code_info = DBHandler.selectqueryCodeListForNonFixed(mContext);
}else{
    // 일반: PACKER_PRODUCT_CODE 기준
    list_code_info = DBHandler.selectqueryCodeList(mContext);
}
```

**list_code_info 구조**:
```java
String[] item = new String[2];
item[0] = CODE;        // ITEM_CODE 또는 PACKER_PRODUCT_CODE
item[1] = ITEM_NAME;   // 상품명
```

2. **WHERE 절 조건 구성**

```java
String data = " WHERE ";
for (int i = 0; i < list_code_info.size(); i++) {
    if(Common.searchType.equals("4") || Common.searchType.equals("5")){
        // 비정량계근출고
        if (i == list_code_info.size() - 1) {
            data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "'";
        } else {
            data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
        }
    }else{
        // 일반
        if (i == list_code_info.size() - 1) {
            data = data + "SBI.PACKER_PRODUCT_CODE = '" + list_code_info.get(i)[0].toString() + "'";
        } else {
            data = data + "SBI.PACKER_PRODUCT_CODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
        }
    }
}

if(list_code_info.size() == 0){
    data = data + "1=0";  // 목록이 없을 경우 조회되지 않도록
}
```

**일반 조회 예시**:
```sql
WHERE SBI.PACKER_PRODUCT_CODE = 'PACK001' OR SBI.PACKER_PRODUCT_CODE = 'PACK002' OR SBI.PACKER_PRODUCT_CODE = 'PACK003'
```

**비정량계근출고 조회 예시**:
```sql
WHERE SBI.ITEM_CODE = 'ITEM001' OR SBI.ITEM_CODE = 'ITEM002' OR SBI.ITEM_CODE = 'ITEM003'
```

3. **출하 유형별 서버 통신**

```java
// 출하 유형에 따라 다른 URL 사용
if(Common.searchType.equals("0") || Common.searchType.equals("2") ||
   Common.searchType.equals("3") || Common.searchType.equals("6")) {
    // 이마트, 홈플러스, 일반도매, 롯데
    receiveData = HttpHelper.getInstance().sendDataDb(
        data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO
    );
} else if(Common.searchType.equals("1")){
    // 생산대상
    receiveData = HttpHelper.getInstance().sendDataDb(
        data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO
    );
} else if(Common.searchType.equals("4")) {
    // 비정량계근출고
    receiveData = HttpHelper.getInstance().sendDataDb(
        data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO_NONFIXED
    );
} else if(Common.searchType.equals("5")) {
    // 홈플러스 비정량계근출고
    receiveData = HttpHelper.getInstance().sendDataDb(
        data, "inno", "search_barcode_info", Common.URL_SEARCH_HOMEPLUS_NONFIXED2
    );
} else if(Common.searchType.equals("7")){
    // 생산대상(라벨)
    receiveData = HttpHelper.getInstance().sendDataDb(
        data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO
    );
}
```

**출하 유형별 URL 매핑**:

| searchType | 출하 유형 | URL | 비고 |
|-----------|---------|-----|------|
| "0" | 이마트 | URL_SEARCH_BARCODE_INFO | - |
| "1" | 생산대상 | URL_SEARCH_BARCODE_INFO | - |
| "2" | 홈플러스 | URL_SEARCH_BARCODE_INFO | - |
| "3" | 일반도매 | URL_SEARCH_BARCODE_INFO | - |
| "4" | 비정량계근출고 | URL_SEARCH_BARCODE_INFO_NONFIXED | ITEM_CODE 사용 |
| "5" | 홈플러스 비정량 | URL_SEARCH_HOMEPLUS_NONFIXED2 | ITEM_CODE 사용 |
| "6" | 롯데 | URL_SEARCH_BARCODE_INFO | - |
| "7" | 생산대상(라벨) | URL_SEARCH_BARCODE_INFO | - |

4. **데이터 파싱**

```java
// 결과값 정리
receiveData = receiveData.replace("\r\n", "");
receiveData = receiveData.replace("\n", "");

// row별로 split (구분자: ;;)
String[] result = receiveData.split(";;");
list_received_bi = new ArrayList<Barcodes_Info>();

// 최대값 설정 (프로그레스 바)
publishProgress("max", Integer.toString(result.length));

if (result.length > 0) {
    for (String s : result) {
        // 각 row의 데이터별로 split (구분자: ::)
        temp = s.split("::");
        bi = new Barcodes_Info();
        bi.setPACKER_CLIENT_CODE(temp[0].toString());
        bi.setPACKER_PRODUCT_CODE(temp[1].toString());
        bi.setPACKER_PRD_NAME(temp[2].toString());
        bi.setITEM_CODE(temp[3].toString());
        bi.setITEM_NAME_KR(temp[4].toString());
        bi.setBRAND_CODE(temp[5].toString());
        bi.setBARCODEGOODS(temp[6].toString());
        bi.setBASEUNIT(temp[7].toString());
        bi.setZEROPOINT(temp[8].toString());
        bi.setPACKER_PRD_CODE_FROM(temp[9].toString());
        bi.setPACKER_PRD_CODE_TO(temp[10].toString());
        bi.setBARCODEGOODS_FROM(temp[11].toString());
        bi.setBARCODEGOODS_TO(temp[12].toString());
        bi.setWEIGHT_FROM(temp[13].toString());
        bi.setWEIGHT_TO(temp[14].toString());
        bi.setMAKINGDATE_FROM(temp[15].toString());
        bi.setMAKINGDATE_TO(temp[16].toString());
        bi.setBOXSERIAL_FROM(temp[17].toString());
        bi.setBOXSERIAL_TO(temp[18].toString());
        bi.setSTATUS(temp[19].toString());
        bi.setREG_ID(temp[20].toString());
        bi.setREG_DATE(temp[21].toString());
        bi.setREG_TIME(temp[22].toString());
        bi.setMEMO(temp[23].toString());

        // 비정량계근출고가 아닌 경우에만 유통기한 설정
        if (!Common.searchType.equals("4") && !Common.searchType.equals("5")) {
            bi.setSHELF_LIFE(temp[24].toString());
        }

        // 바코드정보 내부 SQLite에 INSERT
        DBHandler.insertqueryBarcodeInfo(mContext, bi);
        list_received_bi.add(bi);

        bi = null;

        // 진행률 업데이트
        publishProgress("progress",
            Integer.toString(list_received_bi.size()),
            Integer.toString(list_received_bi.size()) + "번 데이터 저장중.."
        );
    }
}
```

**데이터 필드 (24~25개)**:
- `PACKER_CLIENT_CODE` (0): 포장업체 거래처 코드
- `PACKER_PRODUCT_CODE` (1): 포장업체 상품 코드
- `PACKER_PRD_NAME` (2): 포장업체 상품명
- `ITEM_CODE` (3): 상품 코드
- `ITEM_NAME_KR` (4): 상품명 (한글)
- `BRAND_CODE` (5): 브랜드 코드
- `BARCODEGOODS` (6): 바코드 상품
- `BASEUNIT` (7): 기본 단위
- `ZEROPOINT` (8): 영점
- `PACKER_PRD_CODE_FROM` (9): 포장업체 상품 코드 시작
- `PACKER_PRD_CODE_TO` (10): 포장업체 상품 코드 종료
- `BARCODEGOODS_FROM` (11): 바코드 시작
- `BARCODEGOODS_TO` (12): 바코드 종료
- `WEIGHT_FROM` (13): 무게 시작
- `WEIGHT_TO` (14): 무게 종료
- `MAKINGDATE_FROM` (15): 제조일자 시작
- `MAKINGDATE_TO` (16): 제조일자 종료
- `BOXSERIAL_FROM` (17): 박스 시리얼 시작
- `BOXSERIAL_TO` (18): 박스 시리얼 종료
- `STATUS` (19): 상태
- `REG_ID` (20): 등록자 ID
- `REG_DATE` (21): 등록 날짜
- `REG_TIME` (22): 등록 시간
- `MEMO` (23): 메모
- `SHELF_LIFE` (24): 유통기한 (비정량계근출고 제외)

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

백그라운드 작업 완료 후 UI 스레드에서 실행되며, 바코드 정보가 없는 상품 목록을 추출하고 다음 단계로 진행합니다.

```java
@Override
protected void onPostExecute(Integer result) {
    pDialog.dismiss();

    // 바코드 정보가 없는 상품 목록 추출
    list_unknown_bi = new ArrayList<String[]>();
    for (String[] s : list_code_info) {
        int iCount = 0;
        String req_code = s[0];  // 요청한 코드

        // 받은 바코드 정보에서 해당 코드가 있는지 확인
        for (int i = 0; i < list_received_bi.size(); i++) {
            if (req_code.equals(list_received_bi.get(i).getPACKER_PRODUCT_CODE())) {
                // 바코드 정보 존재
                break;
            }
            iCount++;
        }

        // 바코드 정보가 없는 경우
        if (iCount == list_received_bi.size()) {
            String[] temp = new String[2];
            temp[0] = req_code;           // 코드
            temp[1] = s[1].toString();    // 상품명
            list_unknown_bi.add(temp);
        }
    }

    // 로그 출력
    Log.i(TAG, "===== 받지 못한 BarcodeInfo's PACKER_PRODUCT_CODE =====");
    for (int i = 0; i < list_unknown_bi.size(); i++) {
        Log.e(TAG, String.valueOf((i + 1)) + ". " +
            list_unknown_bi.get(i)[0].toString() + " | " +
            list_unknown_bi.get(i)[1].toString());
    }
    Log.i(TAG, "===== 받지 못한 BarcodeInfo's PACKER_PRODUCT_CODE =====");

    // 다음 단계: 계근정보 조회
    new ProgressDlgGoodsWetSearch(mContext, list_unknown_bi).execute();
}
```

**처리 흐름**:
1. 진행 다이얼로그 닫기
2. 요청한 코드와 받은 바코드 정보 비교
3. 바코드 정보가 없는 상품 목록 추출
4. 미등록 상품 목록 로그 출력
5. 계근정보 조회 시작 (ProgressDlgGoodsWetSearch)

## 서버 통신

### 요청 형식

```java
receiveData = HttpHelper.getInstance().sendDataDb(
    data,                                  // WHERE 절
    "inno",                                // 데이터베이스명
    "search_barcode_info",                 // 액션
    Common.URL_SEARCH_BARCODE_INFO         // URL (출하 유형별 상이)
);
```

**일반 조회 요청 예시**:
```sql
WHERE SBI.PACKER_PRODUCT_CODE = 'PACK001' OR SBI.PACKER_PRODUCT_CODE = 'PACK002'
```

**비정량계근출고 요청 예시**:
```sql
WHERE SBI.ITEM_CODE = 'ITEM001' OR SBI.ITEM_CODE = 'ITEM002'
```

### 응답 형식

```
PACKER_CLIENT_CODE::PACKER_PRODUCT_CODE::PACKER_PRD_NAME::ITEM_CODE::ITEM_NAME_KR::BRAND_CODE::BARCODEGOODS::BASEUNIT::ZEROPOINT::PACKER_PRD_CODE_FROM::PACKER_PRD_CODE_TO::BARCODEGOODS_FROM::BARCODEGOODS_TO::WEIGHT_FROM::WEIGHT_TO::MAKINGDATE_FROM::MAKINGDATE_TO::BOXSERIAL_FROM::BOXSERIAL_TO::STATUS::REG_ID::REG_DATE::REG_TIME::MEMO::SHELF_LIFE;;
...
```

- **Row 구분자**: `;;`
- **Column 구분자**: `::`
- **필드 개수**: 24개 (일반) 또는 25개 (비정량계근출고 제외)

**응답 예시**:
```
CLIENT001::PACK001::사과 1kg::ITEM001::사과::BRAND001::8801234567890::KG::0::PACK001001::PACK001999::8801234567890::8801234567999::10.0::20.0::20231001::20231201::BOX001::BOX999::A::USER01::20231031::143000::테스트::30;;
```

## 사용 방법

### 기본 사용

```java
// 1. 출하 유형 설정
Common.searchType = "0";  // 이마트

// 2. AsyncTask 실행
new ProgressDlgBarcodeSearch(context).execute();
```

### 출하 유형별 예제

**1. 이마트 출하대상**
```java
Common.searchType = "0";
new ProgressDlgBarcodeSearch(context).execute();
```

**2. 생산대상**
```java
Common.searchType = "1";
new ProgressDlgBarcodeSearch(context).execute();
```

**3. 비정량계근출고**
```java
Common.searchType = "4";
new ProgressDlgBarcodeSearch(context).execute();
```

**4. 홈플러스 비정량계근출고**
```java
Common.searchType = "5";
new ProgressDlgBarcodeSearch(context).execute();
```

### 실제 사용 예제

일반적으로 `ProgressDlgShipSearch`의 `onPostExecute`에서 호출됩니다:

```java
// ProgressDlgShipSearch.java의 onPostExecute()
@Override
protected void onPostExecute(Integer result) {
    pDialog.dismiss();
    // 바코드 정보 조회 시작
    new ProgressDlgBarcodeSearch(mContext).execute();
}
```

## 데이터 흐름

```
1. onPreExecute()
   ↓
2. doInBackground()
   ├─ PDA의 상품 코드 목록 조회
   ├─ WHERE 절 조건 구성 (출하 유형별)
   ├─ 서버 통신 (출하 유형별 URL)
   ├─ 데이터 파싱 (24~25개 필드)
   ├─ 로컬 DB 저장
   └─ 진행률 업데이트
   ↓
3. onPostExecute()
   ├─ 미등록 바코드 목록 추출
   ├─ 로그 출력
   └─ ProgressDlgGoodsWetSearch 실행
```

## 주요 특징

1. **출하 유형별 처리**: 8가지 출하 유형에 따라 다른 URL 사용
2. **코드 기준 변경**: 비정량계근출고는 ITEM_CODE, 나머지는 PACKER_PRODUCT_CODE 사용
3. **진행률 표시**: HORIZONTAL 스타일로 실시간 진행률 표시
4. **미등록 상품 추출**: 바코드 정보가 없는 상품 목록 자동 추출
5. **유통기한 처리**: 비정량계근출고는 유통기한 필드 없음
6. **체인 방식 처리**: 완료 후 자동으로 계근정보 조회 시작
7. **상세 로깅**: 미등록 상품 목록 상세 로그 출력

## 관련 클래스

- `Barcodes_Info`: 바코드 정보 데이터 모델
- `DBHandler`: SQLite 데이터베이스 핸들러
- `HttpHelper`: HTTP 통신 헬퍼
- `Common`: 공통 상수 및 변수
- `ProgressDlgShipSearch`: 이전 단계 AsyncTask
- `ProgressDlgGoodsWetSearch`: 다음 단계 AsyncTask

## 주의사항

1. **Context 필수**: 생성자에 Context 전달 필수
2. **사전 데이터 필요**: PDA에 출하대상 데이터가 있어야 함
3. **출하 유형 설정**: `Common.searchType` 설정 필요
4. **UI 스레드에서 실행**: Activity나 Fragment의 Context에서 실행해야 함
5. **네트워크 권한**: 인터넷 권한 필요
6. **메모리 고려**: 대량 데이터 처리 시 메모리 사용량 고려
7. **취소 불가**: 다이얼로그가 취소 불가능하므로 백그라운드 처리 완료까지 대기 필요
8. **순차 실행**: 일반적으로 ProgressDlgShipSearch 이후 실행됨

## 디버그 모드

`Common.D` 플래그를 통해 디버그 로그를 제어할 수 있습니다:

```java
if (Common.D) {
    Log.d(TAG, "Barcode receiveData : " + receiveData.toString());
    Log.d(TAG, "Barcode result's Count : " + result.length);
    Log.d(TAG, "bi : " + bi.getPACKER_CLIENT_CODE() + ", " + ...);
}
```

## URL 상수

```java
Common.URL_SEARCH_BARCODE_INFO              // 일반 바코드 정보
Common.URL_SEARCH_BARCODE_INFO_NONFIXED     // 비정량계근출고 바코드 정보
Common.URL_SEARCH_HOMEPLUS_NONFIXED2        // 홈플러스 비정량 바코드 정보
```
