# ProgressDlgNewBarcodeInfo.java

## 개요

`ProgressDlgNewBarcodeInfo`는 신규 바코드 정보를 서버에 등록하고 로컬 SQLite 데이터베이스에 저장하는 AsyncTask 클래스입니다. 사용자가 입력한 바코드 정보를 서버로 전송하고 성공 여부를 확인한 후, 성공 시 로컬 DB에도 저장합니다.

- **파일 경로**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\common\ProgressDlgNewBarcodeInfo.java`
- **패키지**: `com.rgbsolution.highland_emart.common`
- **총 라인 수**: 108줄
- **주요 기능**: 신규 바코드 정보 등록

## 클래스 구조

```java
public class ProgressDlgNewBarcodeInfo extends AsyncTask<String, Void, String>
```

**특징**:
- `String` 파라미터 입력 (바코드 정보 문자열)
- 진행률 업데이트 없음 (`Void`)
- `String` 결과 반환 (서버 응답)

### 주요 멤버 변수

```java
private static final String TAG = "pDlgNewBarcodeInfo";
ProgressDialog pDialog;                        // 진행 상태 다이얼로그
private Context mContext;                      // Context
private AlertDialog infoDialog;                // 바코드 입력 다이얼로그
String addData = "";                           // 등록할 바코드 정보
String receiveData = "";                       // 서버 응답 데이터
```

## AsyncTask 메서드

### 1. onPreExecute()

백그라운드 작업 시작 전 UI 스레드에서 실행되며, 진행 다이얼로그를 표시합니다.

```java
@Override
protected void onPreExecute() {
    pDialog = new ProgressDialog(mContext);
    pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    pDialog.setTitle("등록중 입니다.");
    pDialog.setMessage("잠시만 기다려 주세요..");
    pDialog.setCancelable(false);
    pDialog.show();
    super.onPreExecute();
}
```

**특징**:
- SPINNER 스타일의 프로그레스 다이얼로그 사용
- 취소 불가능하게 설정
- 간단한 로딩 표시

### 2. doInBackground(String... params)

백그라운드 스레드에서 실행되며, 서버에 신규 바코드 정보를 등록합니다.

```java
@Override
protected String doInBackground(String... params) {
    try {
        if (Common.D) {
            Log.d(TAG, "바코드 등록 전송정보 : " + params[0]);
        }

        addData = params[0];

        if (Common.D) {
            Log.d(TAG, "등록 시작");
        }

        // 서버에 바코드 정보 등록
        receiveData = HttpHelper.getInstance().sendData(
            params[0],
            "barcode_new_insert",
            Common.URL_INSERT_BARCODE_INFO
        );

    } catch (Exception e) {
        if (Common.D) {
            e.printStackTrace();
            Log.e(TAG, "e : " + e.toString());
        }
    }
    return receiveData;
}
```

**파라미터**:
- `params[0]`: 바코드 정보 문자열 (구분자: `::`)

**데이터 형식**:
```
BRAND_CODE::PACKER_PRODUCT_CODE::PACKER_PRD_NAME::ITEM_CODE::PACKER_CLIENT_CODE::BARCODEGOODS::BASEUNIT::ZEROPOINT::PACKER_PRD_CODE_FROM::PACKER_PRD_CODE_TO::BARCODEGOODS_FROM::BARCODEGOODS_TO::WEIGHT_FROM::WEIGHT_TO::MAKINGDATE_FROM::MAKINGDATE_TO::BOXSERIAL_FROM::BOXSERIAL_TO::STATUS
```

**반환값**: 서버 응답 문자열 (`"s"` 또는 `"f"`)

### 3. onPostExecute(String _result)

백그라운드 작업 완료 후 UI 스레드에서 실행되며, 서버 응답에 따라 결과를 처리합니다.

```java
@Override
protected void onPostExecute(String _result) {
    // 결과값의 앞, 뒤에 공백 제거
    String result = _result.replace("\r\n", "");
    result = result.replace("\n", "");
    pDialog.dismiss();

    // s : 성공, f : 실패
    if (result.toString().equals("s")) {
        // 성공 시 로컬 DB에 저장
        String[] split_data = addData.split("::");

        Barcodes_Info bi = new Barcodes_Info();
        HashMap<String, String> temp = new HashMap<String, String>();

        temp.put("PACKER_CLIENT_CODE", split_data[4].toString());
        temp.put("BRAND_CODE", split_data[0].toString());
        temp.put("PACKER_PRODUCT_CODE", split_data[1].toString());
        temp.put("PACKER_PRD_NAME", split_data[2].toString());
        temp.put("ITEM_CODE", split_data[3].toString());
        temp.put("BARCODEGOODS", split_data[5].toString());
        temp.put("BASEUNIT", split_data[6].toString());
        temp.put("ZEROPOINT", split_data[7].toString());
        temp.put("PACKER_PRD_CODE_FROM", split_data[8].toString());
        temp.put("PACKER_PRD_CODE_TO", split_data[9].toString());
        temp.put("BARCODEGOODS_FROM", split_data[10].toString());
        temp.put("BARCODEGOODS_TO", split_data[11].toString());
        temp.put("WEIGHT_FROM", split_data[12].toString());
        temp.put("WEIGHT_TO", split_data[13].toString());
        temp.put("MAKINGDATE_FROM", split_data[14].toString());
        temp.put("MAKINGDATE_TO", split_data[15].toString());
        temp.put("BOXSERIAL_FROM", split_data[16].toString());
        temp.put("BOXSERIAL_TO", split_data[17].toString());
        temp.put("STATUS", split_data[18].toString());
        temp.put("REG_ID", Common.REG_ID);

        // 로컬 DB에 저장
        DBHandler.insertqueryBarcodeInfo(mContext, bi);

        Toast.makeText(mContext, "등록 완료", Toast.LENGTH_SHORT).show();
        infoDialog.dismiss();

    } else {
        // 실패 시
        Toast.makeText(mContext, "등록 실패, " + result, Toast.LENGTH_SHORT).show();
        if (Common.D) {
            Log.d(TAG, "등록 실패:: " + result);
        }
    }
}
```

**처리 흐름**:
1. 응답 문자열 정리 (공백 제거)
2. 진행 다이얼로그 닫기
3. 성공("s") 시:
   - 바코드 정보 파싱
   - HashMap 생성
   - 로컬 DB 저장
   - 성공 토스트 메시지
   - 입력 다이얼로그 닫기
4. 실패("f" 또는 기타) 시:
   - 실패 토스트 메시지
   - 에러 로그 출력

## 생성자

```java
public ProgressDlgNewBarcodeInfo(Context context, AlertDialog alertDialog) {
    mContext = context;
    infoDialog = alertDialog;
}
```

**파라미터**:
- `context`: Context (Activity 또는 Fragment)
- `alertDialog`: 바코드 정보 입력 다이얼로그 (등록 성공 시 닫기 위함)

## 서버 통신

### 요청 형식

```java
receiveData = HttpHelper.getInstance().sendData(
    params[0],                          // 바코드 정보 문자열
    "barcode_new_insert",               // 액션
    Common.URL_INSERT_BARCODE_INFO      // URL
);
```

### 요청 데이터 구조

바코드 정보는 `::` 구분자로 연결된 문자열입니다:

```
BRAND_CODE::PACKER_PRODUCT_CODE::PACKER_PRD_NAME::ITEM_CODE::PACKER_CLIENT_CODE::BARCODEGOODS::BASEUNIT::ZEROPOINT::PACKER_PRD_CODE_FROM::PACKER_PRD_CODE_TO::BARCODEGOODS_FROM::BARCODEGOODS_TO::WEIGHT_FROM::WEIGHT_TO::MAKINGDATE_FROM::MAKINGDATE_TO::BOXSERIAL_FROM::BOXSERIAL_TO::STATUS
```

**필드 구성 (19개)**:

| 인덱스 | 필드명 | 설명 |
|-------|--------|------|
| 0 | BRAND_CODE | 브랜드 코드 |
| 1 | PACKER_PRODUCT_CODE | 포장업체 상품 코드 |
| 2 | PACKER_PRD_NAME | 포장업체 상품명 |
| 3 | ITEM_CODE | 상품 코드 |
| 4 | PACKER_CLIENT_CODE | 포장업체 거래처 코드 |
| 5 | BARCODEGOODS | 바코드 상품 |
| 6 | BASEUNIT | 기본 단위 |
| 7 | ZEROPOINT | 영점 |
| 8 | PACKER_PRD_CODE_FROM | 포장업체 상품 코드 시작 |
| 9 | PACKER_PRD_CODE_TO | 포장업체 상품 코드 종료 |
| 10 | BARCODEGOODS_FROM | 바코드 시작 |
| 11 | BARCODEGOODS_TO | 바코드 종료 |
| 12 | WEIGHT_FROM | 무게 시작 |
| 13 | WEIGHT_TO | 무게 종료 |
| 14 | MAKINGDATE_FROM | 제조일자 시작 |
| 15 | MAKINGDATE_TO | 제조일자 종료 |
| 16 | BOXSERIAL_FROM | 박스 시리얼 시작 |
| 17 | BOXSERIAL_TO | 박스 시리얼 종료 |
| 18 | STATUS | 상태 |

**요청 예시**:
```
BRAND001::PACK001::사과 1kg::ITEM001::CLIENT001::8801234567890::KG::0::PACK001001::PACK001999::8801234567890::8801234567999::10.0::20.0::20231001::20231201::BOX001::BOX999::A
```

### 응답 형식

서버는 간단한 문자열로 응답합니다:

- `"s"`: 성공 (success)
- `"f"`: 실패 (failure)
- 기타: 에러 메시지

## 사용 방법

### 기본 사용

```java
// 1. 바코드 정보 문자열 구성
String barcodeData = "BRAND001::PACK001::사과 1kg::ITEM001::CLIENT001::" +
                     "8801234567890::KG::0::PACK001001::PACK001999::" +
                     "8801234567890::8801234567999::10.0::20.0::" +
                     "20231001::20231201::BOX001::BOX999::A";

// 2. 입력 다이얼로그 참조 (등록 성공 시 닫기 위함)
AlertDialog inputDialog = ...; // 바코드 입력 다이얼로그

// 3. AsyncTask 실행
new ProgressDlgNewBarcodeInfo(context, inputDialog).execute(barcodeData);
```

### 실제 사용 예제

일반적으로 바코드 정보 입력 다이얼로그의 "등록" 버튼 클릭 시 호출됩니다:

```java
// 바코드 정보 입력 다이얼로그에서
btnRegister.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // 입력 필드에서 값 가져오기
        String brandCode = edtBrandCode.getText().toString();
        String packerProductCode = edtPackerProductCode.getText().toString();
        String packerPrdName = edtPackerPrdName.getText().toString();
        String itemCode = edtItemCode.getText().toString();
        String packerClientCode = edtPackerClientCode.getText().toString();
        String barcodeGoods = edtBarcodeGoods.getText().toString();
        String baseUnit = edtBaseUnit.getText().toString();
        String zeroPoint = edtZeroPoint.getText().toString();
        String packerPrdCodeFrom = edtPackerPrdCodeFrom.getText().toString();
        String packerPrdCodeTo = edtPackerPrdCodeTo.getText().toString();
        String barcodeGoodsFrom = edtBarcodeGoodsFrom.getText().toString();
        String barcodeGoodsTo = edtBarcodeGoodsTo.getText().toString();
        String weightFrom = edtWeightFrom.getText().toString();
        String weightTo = edtWeightTo.getText().toString();
        String makingDateFrom = edtMakingDateFrom.getText().toString();
        String makingDateTo = edtMakingDateTo.getText().toString();
        String boxSerialFrom = edtBoxSerialFrom.getText().toString();
        String boxSerialTo = edtBoxSerialTo.getText().toString();
        String status = edtStatus.getText().toString();

        // 바코드 정보 문자열 구성
        String barcodeData = brandCode + "::" + packerProductCode + "::" +
                            packerPrdName + "::" + itemCode + "::" +
                            packerClientCode + "::" + barcodeGoods + "::" +
                            baseUnit + "::" + zeroPoint + "::" +
                            packerPrdCodeFrom + "::" + packerPrdCodeTo + "::" +
                            barcodeGoodsFrom + "::" + barcodeGoodsTo + "::" +
                            weightFrom + "::" + weightTo + "::" +
                            makingDateFrom + "::" + makingDateTo + "::" +
                            boxSerialFrom + "::" + boxSerialTo + "::" + status;

        // 등록 시작
        new ProgressDlgNewBarcodeInfo(context, infoDialog).execute(barcodeData);
    }
});
```

### 유효성 검사 추가 예제

```java
btnRegister.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // 필수 입력 필드 검사
        if (TextUtils.isEmpty(edtBrandCode.getText())) {
            Toast.makeText(context, "브랜드 코드를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(edtPackerProductCode.getText())) {
            Toast.makeText(context, "포장업체 상품 코드를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(edtItemCode.getText())) {
            Toast.makeText(context, "상품 코드를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // 바코드 정보 문자열 구성
        String barcodeData = buildBarcodeData();

        // 등록 시작
        new ProgressDlgNewBarcodeInfo(context, infoDialog).execute(barcodeData);
    }

    private String buildBarcodeData() {
        return edtBrandCode.getText().toString() + "::" +
               edtPackerProductCode.getText().toString() + "::" +
               edtPackerPrdName.getText().toString() + "::" +
               edtItemCode.getText().toString() + "::" +
               edtPackerClientCode.getText().toString() + "::" +
               edtBarcodeGoods.getText().toString() + "::" +
               edtBaseUnit.getText().toString() + "::" +
               edtZeroPoint.getText().toString() + "::" +
               edtPackerPrdCodeFrom.getText().toString() + "::" +
               edtPackerPrdCodeTo.getText().toString() + "::" +
               edtBarcodeGoodsFrom.getText().toString() + "::" +
               edtBarcodeGoodsTo.getText().toString() + "::" +
               edtWeightFrom.getText().toString() + "::" +
               edtWeightTo.getText().toString() + "::" +
               edtMakingDateFrom.getText().toString() + "::" +
               edtMakingDateTo.getText().toString() + "::" +
               edtBoxSerialFrom.getText().toString() + "::" +
               edtBoxSerialTo.getText().toString() + "::" +
               edtStatus.getText().toString();
    }
});
```

## 데이터 흐름

```
1. onPreExecute()
   └─ 진행 다이얼로그 표시
   ↓
2. doInBackground(String... params)
   ├─ 바코드 정보 문자열 받기
   ├─ 서버로 전송
   └─ 응답 받기
   ↓
3. onPostExecute(String result)
   ├─ 응답 처리
   ├─ 성공("s")
   │  ├─ 바코드 정보 파싱
   │  ├─ HashMap 생성
   │  ├─ 로컬 DB 저장
   │  ├─ 성공 토스트
   │  └─ 입력 다이얼로그 닫기
   └─ 실패("f" 또는 기타)
      ├─ 실패 토스트
      └─ 에러 로그
```

## 주요 특징

1. **간단한 등록**: 단일 문자열로 모든 바코드 정보 전송
2. **진행 표시**: SPINNER 스타일의 간단한 로딩 표시
3. **성공/실패 처리**: 서버 응답에 따른 명확한 분기 처리
4. **로컬 DB 동기화**: 서버 등록 성공 시 로컬 DB에도 자동 저장
5. **사용자 피드백**: Toast 메시지로 결과 알림
6. **다이얼로그 관리**: 등록 성공 시 입력 다이얼로그 자동 닫기

## 관련 클래스

- `Barcodes_Info`: 바코드 정보 데이터 모델
- `DBHandler`: SQLite 데이터베이스 핸들러
- `HttpHelper`: HTTP 통신 헬퍼
- `Common`: 공통 상수 및 변수

## 주의사항

1. **Context 필수**: 생성자에 Context 전달 필수
2. **다이얼로그 참조 필수**: 생성자에 AlertDialog 전달 필수
3. **데이터 형식 준수**: 19개 필드를 `::` 구분자로 연결한 문자열 필수
4. **필드 순서 중요**: split_data 인덱스에 맞춰 정확한 순서로 데이터 구성
5. **UI 스레드에서 실행**: Activity나 Fragment의 Context에서 실행해야 함
6. **네트워크 권한**: 인터넷 권한 필요
7. **취소 불가**: 다이얼로그가 취소 불가능하므로 백그라운드 처리 완료까지 대기 필요
8. **입력 검증**: 서버로 전송하기 전에 필수 필드 검증 권장

## HashMap 사용 주의사항

`onPostExecute`에서 HashMap을 생성하지만 실제로 사용하지 않는 것으로 보입니다. 이 부분은 코드 리팩토링 시 제거하거나 Barcodes_Info 객체에 직접 설정하는 것이 더 효율적입니다:

```java
// 현재 코드 (HashMap 미사용)
HashMap<String, String> temp = new HashMap<String, String>();
temp.put("PACKER_CLIENT_CODE", split_data[4].toString());
// ...

// 개선 가능한 코드
bi.setPACKER_CLIENT_CODE(split_data[4].toString());
bi.setBRAND_CODE(split_data[0].toString());
bi.setPACKER_PRODUCT_CODE(split_data[1].toString());
// ...
```

## 에러 처리

```java
try {
    // 서버 통신
} catch (Exception e) {
    if (Common.D) {
        e.printStackTrace();
        Log.e(TAG, "e : " + e.toString());
    }
}
```

모든 예외는 로그로 출력되며, 앱이 중단되지 않도록 처리됩니다.

## 디버그 모드

`Common.D` 플래그를 통해 디버그 로그를 제어할 수 있습니다:

```java
if (Common.D) {
    Log.d(TAG, "바코드 등록 전송정보 : " + params[0]);
    Log.d(TAG, "등록 시작");
    Log.d(TAG, "등록 실패:: " + result);
}
```

## URL 상수

```java
Common.URL_INSERT_BARCODE_INFO  // 바코드 정보 등록 URL
```

## REG_ID

로컬 DB 저장 시 등록자 ID는 `Common.REG_ID`를 사용합니다:

```java
temp.put("REG_ID", Common.REG_ID);
```

이 값은 앱 로그인 시 설정되어야 합니다.
