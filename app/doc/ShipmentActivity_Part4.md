# ShipmentActivity 분석 - Part 4: 서버 전송, 예외 처리 및 요약

> **파일 위치**: `app/src/main/java/com/rgbsolution/highland_emart/ShipmentActivity.java`
> **코드 라인**: 4158줄
> **작성일**: 2025-01-27

---

## 목차

- [Part 1: 개요 및 클래스 구조](ShipmentActivity_Part1.md)
- [Part 2: 바코드 스캔 처리](ShipmentActivity_Part2.md)
- [Part 3: 계근 데이터 처리 및 프린터 출력](ShipmentActivity_Part3.md)
- [Part 4: 서버 전송, 예외 처리 및 요약](ShipmentActivity_Part4.md) 현재 문서

---

## 1. ProgressDlgShipSelect AsyncTask (2915~3098줄)

### 1.1 개요

출하대상 리스트를 조회하는 AsyncTask입니다.

```java
class ProgressDlgShipSelect extends AsyncTask<Integer, String, Integer>
```

**생성자 매개변수**:
- `center_name`: 센터명
- `condition`: 검색 조건 (패커상품코드 또는 BL번호)
- `type`: true = 패커상품코드 스캔, false = BL 스캔

### 1.2 onPreExecute (2928~2943줄)

```java
@Override
protected void onPreExecute() {
    pDialog = new ProgressDialog(mContext);
    pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    pDialog.setTitle("출하대상 불러오는중...");
    pDialog.setMessage("잠시만 기다려 주세요..");
    pDialog.setCancelable(false);
    pDialog.show();

    // 센터 누적 초기화
    centerTotalCount = 0;
    centerTotalWeight = 0.0;
    centerWorkCount = 0;
    centerWorkWeight = 0.0;
    super.onPreExecute();
}
```

### 1.3 doInBackground (2946~2990줄)

#### 1.3.1 출하대상 조회 (2949~2958줄)

```java
// 계근지점 검색
arSM = DBHandler.selectqueryShipment(mContext, this.center_name, this.condition, this.type);

for (int i = 0; i < arSM.size(); i++) {
    String[] row = new String[2];
    row = DBHandler.selectqueryListGoodsWetInfo(mContext,
        arSM.get(i).getGI_D_ID(),
        arSM.get(i).getPACKER_PRODUCT_CODE(),
        arSM.get(i).getCLIENT_CODE());

    arSM.get(i).setGI_QTY(Double.parseDouble(row[0]));      // 중량
    arSM.get(i).setPACKING_QTY(Integer.parseInt(row[1]));   // 수량
    arSM.get(i).setSAVE_CNT(Integer.parseInt(row[2]));      // 계근 상품 전송 개수
}
```

#### 1.3.2 롯데 박스 순번 초기화 (2964~2981줄)

```java
// 롯데의 경우만 lotte_TryCount 사용
if(Common.searchType.equals("6")) {
    Shipments_Info si = arSM.get(0);
    lotte_TryCount = Integer.parseInt(si.LAST_BOX_ORDER) + 1;

    if (lotte_TryCount > 9999) {
        lotte_TryCount = 1;
    }

    Log.e(TAG, "***************************LAST_BOX_ORDER : " + si.getLAST_BOX_ORDER());

    // 현재 계근된 수량을 더함
    for (int i = 0; i < arSM.size(); i++) {
        lotte_TryCount += arSM.get(i).getPACKING_QTY();
    }

    if (lotte_TryCount > 9999) {
        lotte_TryCount = lotte_TryCount % 9999; // 9999 넘으면 순환
    }

    Log.d(TAG, "======================== lotte_TryCount =========================" + lotte_TryCount);
}
```

**롯데 박스 순번 관리**:
- `LAST_BOX_ORDER`: DB에서 조회한 마지막 박스 순번
- 마지막 순번 + 1부터 시작
- 현재 계근된 수량만큼 추가
- 9999 초과시 1로 순환

### 1.4 onPostExecute (3003~3097줄)

#### 1.4.1 출하대상 존재시 처리 (3009~3053줄)

```java
if (arSM.size() > 0) {       // 목록 존재

    // 패커상품 정보 설정
    if (!this.type)
        work_ppcode = find_work_info(arSM.get(0).getBARCODEGOODS().toString(), this.type);

    // 지점명 리스트 생성
    ArrayList<String> list_position = new ArrayList<String>();
    for (int i = 0; i < arSM.size(); i++) {
        list_position.add(arSM.get(i).getCLIENTNAME() + " / " + arSM.get(i).getIMPORT_ID_NO());
    }

    // Spinner 어댑터 설정
    ArrayAdapter<String> position_adapter = new ArrayAdapter<String>(
        ShipmentActivity.this,
        android.R.layout.simple_spinner_item,
        list_position
    );
    position_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp_point_name.setAdapter(position_adapter);
    select_flag = true;

    // 센터 누적 계산
    for (int i = 0; i < arSM.size(); i++) {
        centerTotalCount += Integer.parseInt(arSM.get(i).getGI_REQ_PKG());      // 센터 총 계근요청수량
        centerTotalWeight += Double.parseDouble(arSM.get(i).getGI_REQ_QTY());   // 센터 총 계근요청중량

        centerWorkCount += arSM.get(i).getPACKING_QTY();                        // 센터 총 계근수량
        centerWorkWeight += arSM.get(i).getGI_QTY();                            // 센터 총 계근중량
    }

    // UI 업데이트
    edit_center_tcount.setText(centerTotalCount + " / " + centerWorkCount);
    edit_center_tweight.setText(Math.round(centerTotalWeight * 100) / 100.0 + " / " + centerWorkWeight);

    position_adapter.notifyDataSetChanged();

    // 미완료 지점으로 자동 이동
    for (int i = 0; i < arSM.size(); i++) {
        if (!arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))) {
            sp_point_name.setSelection(i);
            break;
        }
    }

    set_scanFlag(false);  // BL 스캔 모드로 전환

    // 전체 계근 완료 체크
    if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {
        show_wetFinishDialog();
    }
}
```

#### 1.4.2 조회 결과 없을 시 (3054~3092줄)

```java
else {            // 결과 없음
    vibrator.vibrate(1000);
    Log.e(TAG, "###############################################");
    Log.e(TAG, "######### 출하대상 리스트 조회결과 없음 ###########");
    Log.e(TAG, "###############################################");

    // TB_BARCODE_INFO의 BarcodeGoods 데이터가 없을 경우
    if(work_item_barcodegoods == "" ) {
        Log.d(TAG, "test");
        Log.d(TAG, "alert_flag3: " + alert_flag);
        showAlertDialog("barcode", 0);
        alert_flag = true;
        edit_product_code.setText("");
    }

    Toast.makeText(getApplicationContext(), "조회결과가 없습니다.", Toast.LENGTH_SHORT).show();

    // scan_flag 초기화
    if (work_flag == 1) {
        set_scanFlag(true);
    } else if (work_flag == 0){
        set_scanFlag(false);
    } else if (work_flag == 2){
        set_scanFlag(true);
    }

    current_work_position = -1;
    sp_point_name.setAdapter(null);
    sp_bl_no.setAdapter(null);

    // UI 초기화
    centerTotalCount = 0;
    centerTotalWeight = 0.0;
    edit_center_tcount.setText("0 / 0");
    edit_center_tweight.setText("0 / 0");
    edit_wet_count.setText("0 / 0");
    edit_wet_weight.setText("0 / 0");
    edit_product_name.setText("");
    edit_product_code.setText("");
}
```

---

## 2. ProgressDlgShipmentSend AsyncTask (3276~3595줄)

### 2.1 개요

계근 데이터를 서버로 전송하는 AsyncTask입니다.

```java
class ProgressDlgShipmentSend extends AsyncTask<Void, String, String>
```

### 2.2 onPreExecute (3284~3293줄)

```java
@Override
protected void onPreExecute() {
    super.onPreExecute();
    pDialog = new ProgressDialog(mContext);
    pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    pDialog.setTitle("전송 중 입니다.");
    pDialog.setMessage("잠시만 기다려 주세요..");
    pDialog.setCancelable(false);
    pDialog.show();
}
```

### 2.3 doInBackground - 처리 흐름

#### 2.3.1 전송 대상 조회 (3299~3310줄)

```java
String qry_where = "";
for (int i = 0; i < arSM.size(); i++) {
    if (i == (arSM.size() - 1)) {
        qry_where = qry_where + "GI_D_ID = " + arSM.get(i).getGI_D_ID();
    } else {
        qry_where = qry_where + "GI_D_ID = " + arSM.get(i).getGI_D_ID() + " OR ";
    }
}
Log.v(TAG, "전송상품 검색 where : " + qry_where);

list_send_info = DBHandler.selectquerySendGoodsWet(mContext, qry_where);
publishProgress("max", Integer.toString(list_send_info.size()));
```

#### 2.3.2 이마트/홈플러스/롯데 전송 방식 (3315~3421줄) - 개별 건

```java
if(Common.searchType.equals("0") || Common.searchType.equals("2") || Common.searchType.equals("6")) {
    // 이마트 혹은 홈플러스, 롯데 출고일때..구로직

    for (int i = 0; i < list_send_info.size(); i++) {
        if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {  // 미전송 건만
            iCount++;

            // 패킷 생성
            String packet = "";
            packet += list_send_info.get(i).getGI_D_ID() + "::";
            packet += list_send_info.get(i).getWEIGHT() + "::";
            packet += list_send_info.get(i).getWEIGHT_UNIT() + "::";
            packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::";
            packet += list_send_info.get(i).getBARCODE() + "::";
            packet += list_send_info.get(i).getPACKER_CLIENT_CODE() + "::";
            packet += list_send_info.get(i).getMAKINGDATE() + "::";
            packet += list_send_info.get(i).getBOXSERIAL() + "::";
            packet += list_send_info.get(i).getBOX_CNT() + "::";
            packet += list_send_info.get(i).getREG_ID() + "::";
            packet += list_send_info.get(i).getITEM_CODE() + "::";
            packet += list_send_info.get(i).getBRAND_CODE() + "::";
            packet += list_send_info.get(i).getCLIENT_TYPE() + "::";
            packet += list_send_info.get(i).getBOX_ORDER();

            // 출하 유형별 URL 분기
            if(Common.searchType.equals("0") || Common.searchType.equals("3")) {
                result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);
            } else if(Common.searchType.equals("2") || Common.searchType.equals("6")) {
                result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_HOMEPLUS);
            } else if(Common.searchType.equals("1")) {
                result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);
            } else if(Common.searchType.equals("7")) {
                result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET);
            }

            // 결과 처리
            result = result.replace("\r\n", "");
            result = result.replace("\n", "");

            if (result.equals("s")) {
                // PDA SQLite update
                boolean bool = DBHandler.updatequeryGoodsWet(mContext,
                    list_send_info.get(i).getGI_D_ID(),
                    list_send_info.get(i).getBARCODE(),
                    list_send_info.get(i).getBOX_CNT());

                if (bool) {
                    publishProgress("progress", Integer.toString(iCount), Integer.toString(iCount) + "번 데이터 전송성공..");

                    // 출하대상 업데이트
                    for (int j = 0; j < arSM.size(); j++) {
                        if (arSM.get(j).getGI_D_ID().equals(list_send_info.get(i).getGI_D_ID())) {
                            arSM.get(j).setSAVE_CNT(arSM.get(j).getSAVE_CNT() + 1);

                            // 전송 개수와 요청 개수 비교
                            if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {
                                String completeStr = arSM.get(j).getGI_D_ID() + "::"
                                    + arSM.get(j).getITEM_CODE() + "::"
                                    + arSM.get(j).getBRAND_CODE() + "::"
                                    + Common.REG_ID;

                                // 출하대상 Table의 계근여부 Update
                                if(Common.searchType.equals("0") || Common.searchType.equals("2") || Common.searchType.equals("6")) {
                                    receiveData = HttpHelper.getInstance().sendDataDb(completeStr, "inno", "complete_shipment", Common.URL_UPDATE_SHIPMENT);
                                } else if(Common.searchType.equals("1") || Common.searchType.equals("4") || Common.searchType.equals("5") || Common.searchType.equals("7")) {
                                    receiveData = HttpHelper.getInstance().sendDataDb(completeStr, "inno", "complete_shipment", Common.URL_UPDATE_SHIPMENT);
                                }

                                receiveData = receiveData.replace("\r\n", "");
                                receiveData = receiveData.replace("\n", "");

                                if (receiveData.equals("s")) {
                                    Log.v(TAG, "출하대상 리스트 update 완료");
                                    arSM.get(j).setSAVE_TYPE("Y");
                                    DBHandler.updatequeryShipment(mContext, arSM.get(j).getGI_D_ID(), arSM.get(j).getPACKER_PRODUCT_CODE());

                                    jChk++;

                                    if (jChk == arSM.size()) {
                                        return "ss";  // 모든 출하대상 전송 완료
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (result.equals("f")) {
                return result;  // 전송 실패
            }
        }
    }
}
```

**개별 건 전송 특징**:
- SAVE_TYPE="F" 인 건만 전송
- 건별로 HTTP 요청 전송
- 전송 성공시 즉시 PDA DB 업데이트 (SAVE_TYPE="T")
- 출하대상별 전송 개수 카운트
- 전송 개수 = 요청 개수일 때 complete_shipment API 호출

#### 2.3.3 생산/도매 전송 방식 (3422~3558줄) - 일괄 전송 (##구분)

```java
else if(Common.searchType.equals("1") || Common.searchType.equals("3") || Common.searchType.equals("4") || Common.searchType.equals("5") || Common.searchType.equals("7")) {
    // 생산계근 혹은 도매계근일때

    String packet = "";

    // 전문 전송용 for문 (##구분자로 연결)
    for (int i = 0; i < list_send_info.size(); i++) {
        if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
            iCount++;
            packet += list_send_info.get(i).getGI_D_ID() + "::";
            packet += list_send_info.get(i).getWEIGHT() + "::";
            packet += list_send_info.get(i).getWEIGHT_UNIT() + "::";
            packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::";
            packet += list_send_info.get(i).getBARCODE() + "::";
            packet += list_send_info.get(i).getPACKER_CLIENT_CODE() + "::";
            packet += list_send_info.get(i).getMAKINGDATE() + "::";
            packet += list_send_info.get(i).getBOXSERIAL() + "::";
            packet += list_send_info.get(i).getBOX_CNT() + "::";
            packet += list_send_info.get(i).getREG_ID() + "::";
            packet += list_send_info.get(i).getITEM_CODE() + "::";
            packet += list_send_info.get(i).getBRAND_CODE() + "::";
            packet += list_send_info.get(i).getCLIENT_TYPE() + "::";
            packet += list_send_info.get(i).getBOX_ORDER() + "##";  // ## 구분자
        }
    }

    boolean sendOrNot = true;
    if(packet == "") {
        sendOrNot = false;
    }

    if(sendOrNot) {
        // 일괄 전송
        if(Common.searchType.equals("1") || Common.searchType.equals("4") || Common.searchType.equals("5") || Common.searchType.equals("7")) {
            result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_NEW);
        } else if(Common.searchType.equals("3")) {
            result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_INSERT_GOODS_WET_NEW);
        }
    } else {
        result = "af";  // already finish
    }

    result = result.replace("\r\n", "");
    result = result.replace("\n", "");

    // 결과 처리 (전체 루프)
    for (int i = 0; i < list_send_info.size(); i++) {
        if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
            if (result.equals("s")) {
                boolean bool = DBHandler.updatequeryGoodsWet(mContext,
                    list_send_info.get(i).getGI_D_ID(),
                    list_send_info.get(i).getBARCODE(),
                    list_send_info.get(i).getBOX_CNT());

                if (bool) {
                    publishProgress("progress", Integer.toString(iCount), Integer.toString(iCount) + "번 데이터 전송성공..");

                    for (int j = 0; j < arSM.size(); j++) {
                        if (arSM.get(j).getGI_D_ID().equals(list_send_info.get(i).getGI_D_ID())) {
                            arSM.get(j).setSAVE_CNT(arSM.get(j).getSAVE_CNT() + 1);

                            if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {
                                String completeStr = arSM.get(j).getGI_D_ID() + "::"
                                    + arSM.get(j).getITEM_CODE() + "::"
                                    + arSM.get(j).getBRAND_CODE() + "::"
                                    + Common.REG_ID;

                                // 출하대상 Table의 계근여부 Update
                                if (Common.searchType.equals("0") || Common.searchType.equals("2")) {
                                    receiveData = HttpHelper.getInstance().sendDataDb(completeStr, "inno", "complete_shipment", Common.URL_UPDATE_SHIPMENT);
                                } else if (Common.searchType.equals("1")) {
                                    receiveData = HttpHelper.getInstance().sendDataDb(completeStr, "inno", "complete_shipment", Common.URL_UPDATE_SHIPMENT);
                                } else if (Common.searchType.equals("3") || Common.searchType.equals("4") || Common.searchType.equals("5")) {
                                    // 도매계근은 아래 URL을 호출하지 않는다.
                                    // GI_D_ID별 CHECK_YN으로 대상을 구분하는데 아래 URL이 CHECK_YN을 N으로 꺾어버리기 때문에
                                    // 박스 일부 재계근이 불가능해짐
                                    receiveData = "s";
                                } else if (Common.searchType.equals("7")) {
                                    receiveData = HttpHelper.getInstance().sendDataDb(completeStr, "inno", "complete_shipment", Common.URL_UPDATE_SHIPMENT);
                                }

                                receiveData = receiveData.replace("\r\n", "");
                                receiveData = receiveData.replace("\n", "");

                                if (receiveData.equals("s")) {
                                    Log.v(TAG, "출하대상 리스트 update 완료");
                                    arSM.get(j).setSAVE_TYPE("Y");
                                    DBHandler.updatequeryShipment(mContext, arSM.get(j).getGI_D_ID(), arSM.get(j).getPACKER_PRODUCT_CODE());

                                    jChk++;

                                    if (jChk == arSM.size()) {
                                        return "ss";
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (result.equals("f")) {
                return result;
            }
        }
    }
}
return result;
```

**일괄 전송 특징**:
- SAVE_TYPE="F" 인 모든 건을 `##`로 연결
- 한 번의 HTTP 요청으로 전송
- 전송 성공시 모든 건의 PDA DB 업데이트
- 도매계근(3, 4, 5)은 complete_shipment API 미호출

### 2.4 complete_shipment API 호출 조건

| searchType | 출하 유형 | complete_shipment 호출 |
|-----------|----------|----------------------|
| **0** | 이마트 출하 | ✅ 호출 |
| **1** | 생산 계근 (이노이천) | ✅ 호출 |
| **2** | 홈플러스 출하 | ✅ 호출 |
| **3** | 도매 출하 | ❌ **미호출** |
| **4** | 도매 비정량 | ❌ **미호출** |
| **5** | 홈플러스 비정량 | ❌ **미호출** |
| **6** | 롯데 출하 | ✅ 호출 |
| **7** | 생산 계근 | ✅ 호출 |

**도매계근 complete_shipment API 미호출 이유 (3518~3521줄 주석)**:
```java
// 도매계근은 아래 URL을 호출하지 않는다.
// GI_D_ID별 CHECK_YN으로 대상을 구분하는데
// 아래 URL이 CHECK_YN을 N으로 꺾어버리기 때문에
// 박스 일부 재계근이 불가능해짐
receiveData = "s";
```

### 2.5 onPostExecute (3573~3594줄)

```java
@Override
protected void onPostExecute(String _result) {
    Log.v(TAG, "전송결과 : " + _result);
    pDialog.dismiss();

    if (_result.equals("s")) {
        Toast.makeText(getApplicationContext(), "결과 s, 전송 성공.", Toast.LENGTH_SHORT).show();
        sListAdapter.notifyDataSetChanged();
    } else if (_result.equals("ss")) {
        Toast.makeText(getApplicationContext(), "결과 ss, 전송성공.", Toast.LENGTH_SHORT).show();
        sListAdapter.notifyDataSetChanged();
        show_sendFinishDialog();  // 전송 완료 다이얼로그
    } else if (_result.equals("update_fail")) {
        Toast.makeText(getApplicationContext(), "출하대상 완료 작업실패", Toast.LENGTH_SHORT).show();
        vibrator.vibrate(500);
    } else if(_result.equals("f")) {
        Toast.makeText(getApplicationContext(), "네트워크 에러로 인한 전송간 오류 발생, 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
        vibrator.vibrate(500);
    } else if(_result.equals("af")) {
        Toast.makeText(getApplicationContext(), "이미 모두 전송되었거나 전송할 건이 없습니다.", Toast.LENGTH_SHORT).show();
        vibrator.vibrate(500);
    }
    super.onPostExecute(_result);
}
```

**결과 코드**:
- `"s"`: 일부 전송 성공
- `"ss"`: 모든 출하대상 전송 완료
- `"update_fail"`: 출하대상 완료 실패
- `"f"`: 네트워크 에러
- `"af"`: already finish (전송할 건 없음)

---

## 3. 예외 처리

### 3.1 중복 바코드 체크 (2곳)

#### 3.1.1 첫 번째 체크: 패커상품 스캔 직후 (651줄)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet_check(getApplicationContext(), msg);
```

**쿼리**:
```sql
SELECT COUNT(*) FROM TB_GOODS_WET WHERE BARCODE = ?
```

#### 3.1.2 두 번째 체크: BL 스캔 직후 (876~895줄)

```java
boolean dup = DBHandler.duplicatequeryGoodsWet(getApplicationContext(),
    work_item_fullbarcode,
    arSM.get(current_work_position).getGI_D_ID(),
    arSM.get(current_work_position).getPACKER_PRODUCT_CODE());

// 비정량은 바코드 같은게 얼마든지 나올 수 있기 때문에 중복확인 제외
if(Common.searchType.equals("4") || Common.searchType.equals("5")) {
    dup = false;
}

if (dup) {
    Toast.makeText(getApplicationContext(),
        "이미 스캔한 바코드입니다.\n다른 바코드를 스캔하세요.",
        Toast.LENGTH_SHORT).show();
    vibrator.vibrate(1000);
    work_item_fullbarcode = "";
    work_item_barcodegoods = "";
    return;
}
```

**쿼리**:
```sql
SELECT COUNT(*) FROM TB_GOODS_WET
WHERE BARCODE = ?
  AND GI_D_ID = ?
  AND PACKER_PRODUCT_CODE = ?
```

**비정량 예외 (searchType="4", "5")**:
- 비정량 상품은 박스마다 중량이 다르지만 바코드는 동일할 수 있음
- 중복 체크 생략

### 3.2 소비기한 필수 검증

#### 3.2.1 킬코이 + 미트센터 (793~800줄)

```java
if (arSM.get(current_work_position).getPACKER_CODE().equals("30228")
    && arSM.get(current_work_position).getSTORE_CODE().equals("9231")) {

    if (work_item_bi_info.getSHELF_LIFE().equals("")
        || work_item_bi_info.getMAKINGDATE_FROM().equals("")
        || work_item_bi_info.getMAKINGDATE_TO().equals("")) {

        Toast.makeText(getApplicationContext(),
            "미트센터 납품 - KILKOY 상품의 경우 소비기한정보가 필수로 입력되어야 합니다.\n" +
            "현 상품의 계근을 진행할 수 없습니다. 관리자에게 문의하세요.",
            Toast.LENGTH_LONG).show();
        vibrator.vibrate(1000);
        work_ppcode = "";
        scan_flag = true;
        return;
    }
}
```

#### 3.2.2 트레이더스/수입육 (839~848줄)

```java
if (arSM.get(current_work_position).getCENTERNAME().equals("용인TRD")
    || arSM.get(current_work_position).getCENTERNAME().equals("대구TRD")
    || arSM.get(current_work_position).getCENTERNAME().contains("TRD")
    || arSM.get(current_work_position).getCENTERNAME().contains("E/T")
    || arSM.get(current_work_position).getCENTERNAME().contains("WET")) {

    if (Common.searchType.equals("0")) {
        if (work_item_bi_info.getSHELF_LIFE().equals("")
            || work_item_bi_info.getMAKINGDATE_FROM().equals("")
            || work_item_bi_info.getMAKINGDATE_TO().equals("")) {

            Toast.makeText(getApplicationContext(),
                "트레이더스 납품 상품의 경우 소비기한정보가 필수로 입력되어야 합니다.\n" +
                "현 상품의 계근을 진행할 수 없습니다. 관리자에게 문의하세요.",
                Toast.LENGTH_LONG).show();
            vibrator.vibrate(1000);
            work_ppcode = "";
            scan_flag = true;
            return;
        }
    }
}
```

**검증 조건**:
- SHELF_LIFE (유통기한)
- MAKINGDATE_FROM (제조일 시작 위치)
- MAKINGDATE_TO (제조일 종료 위치)

3개 필드 중 하나라도 빈 값이면 계근 불가

### 3.3 계근 완료 체크 (862~870줄)

```java
if (arSM.get(current_work_position).getGI_REQ_PKG()
    .equals(String.valueOf(arSM.get(current_work_position).getPACKING_QTY()))) {

    // 이미 계근 완료됨
    if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {
        show_wetFinishDialog();  // 총 계근 완료
    } else {
        // 다음 지점 작업 필요
    }
    return;
}
```

**조건**:
- `GI_REQ_PKG` (요청 수량) == `PACKING_QTY` (계근 수량)

### 3.4 스캔 오류 알림 (showAlertDialog) (4121~4156줄)

```java
public void showAlertDialog(String s, int i) {
    try {
        Inflater = (LayoutInflater) ShipmentActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(ShipmentActivity.this, R.style.AppCompatDialogStyle);
        vibrator.vibrate(500);
        builder.setIcon(R.drawable.highland);
        builder.setTitle("스캔 오류");

        if(!alert_flag) {
            if (s.equals("weight")) {
                builder.setMessage("중량위치정보가 없습니다.\n다른 바코드를 스캔해주세요.");
            } else if (s.equals("barcode")) {
                builder.setMessage("바코드 정보(조회결과)가 없습니다.\n다른 바코드를 스캔해주세요");
            } else if (s.equals("bl")) {
                builder.setMessage(i + "번 상품의 bl정보가 없습니다.");
            }

            builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    alert_flag = false;
                    alert.dismiss();
                }
            });

            alert = builder.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
            alert_flag = true;
        } else if(alert_flag) {
            return;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

**오류 타입**:
- `"weight"`: 중량위치정보 없음
- `"barcode"`: 바코드 정보 없음
- `"bl"`: BL 정보 없음

---

## 4. 주요 Dialog

### 4.1 show_wetDetailDialog (3779~3948줄) - 계근 상세

```java
private void show_wetDetailDialog(Shipments_Info si, Barcodes_Info bi, int position)
```

**표시 정보**:
- 지점명 (CLIENTNAME)
- 패커 상품명 (ITEM_NAME)
- 패커 상품코드 (PACKER_PRODUCT_CODE)
- 계근 수량 (GI_REQ_PKG / PACKING_QTY)
- 계근 중량 (GI_REQ_QTY / GI_QTY)

**버튼**:
1. **뒤로 (detail_btn_back)**: 상세 다이얼로그 닫기 및 출하대상 재조회
2. **선택 삭제 (detail_btn_delete)**: 선택된 계근 데이터 삭제
3. **합산 (detail_btn_sum)**: 전체 리스트 중량 합산 라벨 출력

#### 4.1.1 합산 라벨 출력 (3860~3936줄)

```java
detail_btn_sum.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (list_gi_info.size() == 0) {
            Toast.makeText(getApplicationContext(), "합산할 항목이 없습니다.", Toast.LENGTH_SHORT).show();
            vibrator.vibrate(1000);
        } else if (list_gi_info.size() > 0) {
            try {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byteStream.write(WoosimCmd.initPrinter());
                byteStream.write(WoosimCmd.setPageMode());
                byteStream.write(WoosimCmd.selectTTF("HYWULM.TTF"));
                byteStream.write(WoosimCmd.setTextStyle(true, false, false, 1, 1));

                double weight_sum = 0;
                int p_weight = 0;
                int p_hight = 0;

                for (int i = 0; i < list_gi_info.size(); i++) {
                    p_hight = 10 + (i/6*50) - (i/36*300);
                    p_weight = 100 * (i%6);

                    byteStream.write(WoosimCmd.PM_setPosition(p_weight, p_hight));
                    byteStream.write(WoosimCmd.getTTFcode(40, 40, list_gi_info.get(i).getWEIGHT()));

                    weight_sum += Double.parseDouble(list_gi_info.get(i).getWEIGHT());

                    // 36건마다 페이지 구분 출력
                    if((i+1) % 36 == 0) {
                        weight_sum = Math.floor(weight_sum * 100) / 100.0;
                        String temp_weight = String.format("%.1f", weight_sum);
                        weight_sum = Double.parseDouble(temp_weight);

                        byteStream.write(WoosimCmd.PM_setPosition(100, 350));
                        byteStream.write(WoosimCmd.getTTFcode(60, 60, ((i+1)/36) + "번 총 중량 : " + Double.toString(weight_sum)));
                        byteStream.write(WoosimCmd.PM_setArea(0, 0, 576, 460));

                        sendData(byteStream.toByteArray());
                        sendData(WoosimCmd.feedToMark());

                        // 다음 페이지 준비
                        byteStream.reset();
                        byteStream.write(WoosimCmd.initPrinter());
                        byteStream.write(WoosimCmd.setPageMode());
                        byteStream.write(WoosimCmd.selectTTF("HYWULM.TTF"));
                        byteStream.write(WoosimCmd.setTextStyle(true, false, false, 1, 1));
                        weight_sum = 0;
                    } else if((i+1) == list_gi_info.size()) {
                        // 마지막 페이지
                        weight_sum = Math.floor(weight_sum * 100) / 100.0;
                        String temp_weight = String.format("%.1f", weight_sum);
                        weight_sum = Double.parseDouble(temp_weight);

                        byteStream.write(WoosimCmd.PM_setPosition(100, 350));
                        byteStream.write(WoosimCmd.getTTFcode(60, 60, (((i+1)/36)+1) + "번 총 중량 : " + Double.toString(weight_sum)));
                        byteStream.write(WoosimCmd.PM_setArea(0, 0, 576, 460));

                        sendData(byteStream.toByteArray());
                        sendData(WoosimCmd.feedToMark());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
});
```

**합산 라벨 형식**:
- 6열 x 6행 = 36건씩 한 페이지
- 각 셀에 중량 표시
- 페이지 하단에 합계 출력
- 36건 초과시 여러 페이지 출력

### 4.2 deleteQuestionDialog (3955~4009줄) - 삭제 확인

```java
public void deleteQuestionDialog(final Shipments_Info si, final ArrayList<Goodswets_Info> list_delete)
```

**동작**:
1. 선택된 계근 데이터 삭제 확인
2. 삭제 시 DB 및 메모리에서 제거
3. 출하대상 누적 정보 재계산

```java
new AlertDialog.Builder(ShipmentActivity.this, R.style.AppCompatDialogStyle)
    .setIcon(R.drawable.highland)
    .setTitle("계근상품 삭제")
    .setMessage("정말 삭제하시겠습니까?")
    .setCancelable(false)
    .setPositiveButton("삭제",
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = list_delete.size()-1; i >= 0; i--) {
                    String delete_box = list_delete.get(i).getBOX_CNT();

                    DBHandler.deletequerySelectGoodsWet(getApplicationContext(),
                        list_delete.get(i).getGI_D_ID(),
                        list_delete.get(i).getBARCODE(),
                        Integer.parseInt(delete_box));

                    refresh_delete(list_delete.get(i).getWEIGHT());
                }

                vibrator.vibrate(500);
                btn_send.setEnabled(false);
                btn_send.setBackgroundResource(R.drawable.disable_round_button);
                detail_btn_back.performClick();

                Toast.makeText(getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("취소", null)
    .show();
```

#### 4.2.1 refresh_delete (4011~4020줄)

```java
public void refresh_delete(String delete_weight) {
    Log.d(TAG, "삭제되는 계근대상의 중량 : " + delete_weight);

    arSM.get(select_position).setPACKING_QTY(arSM.get(select_position).getPACKING_QTY() - 1);

    double temp = arSM.get(select_position).getGI_QTY() - Double.parseDouble(delete_weight);
    temp = Math.round(temp * 100) / 100.0;
    arSM.get(select_position).setGI_QTY(temp);

    detail_edit_count.setText(arSM.get(select_position).getGI_REQ_PKG() + " / " + arSM.get(select_position).getPACKING_QTY());
    detail_edit_weight.setText(arSM.get(select_position).getGI_REQ_QTY() + " / " + arSM.get(select_position).getGI_QTY());
}
```

### 4.3 show_wetFinishDialog (4091~4118줄) - 계근 완료

```java
private void show_wetFinishDialog()
```

**표시 시점**:
- 센터 총 계근수량 == 센터 총 계근요청수량

```java
new AlertDialog.Builder(ShipmentActivity.this, R.style.AppCompatDialogStyle)
    .setIcon(R.drawable.highland)
    .setTitle(R.string.shipment_wet_finish)
    .setMessage(R.string.shipment_wet_finish_msg)
    .setCancelable(false)
    .setPositiveButton("확인",
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish_flag = true;
                btn_send.setEnabled(true);  // 전송 버튼 활성화
                btn_send.setBackgroundResource(R.drawable.round_button);
                dialog_flag = false;

                if (work_flag == 1) {
                    scanFlag_init();
                } else if (work_flag == 0){
                    set_scanFlag(false);
                } else if (work_flag == 2){
                    scanFlag_init();
                }

                edit_barcode.setText("");
            }
        }).show();
```

### 4.4 show_sendFinishDialog (4023~4050줄) - 전송 완료

```java
private void show_sendFinishDialog()
```

**표시 시점**:
- 모든 출하대상 전송 완료 (result="ss")

```java
new AlertDialog.Builder(ShipmentActivity.this, R.style.AppCompatDialogStyle)
    .setIcon(R.drawable.highland)
    .setTitle(R.string.shipment_wet_send_finish)
    .setMessage(R.string.shipment_wet_send_finish_msg)
    .setCancelable(false)
    .setPositiveButton("확인",
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish_flag = true;
                btn_send.setEnabled(false);  // 전송 버튼 비활성화
                btn_send.setBackgroundResource(R.drawable.disable_round_button);
                dialog_flag = false;

                if (work_flag == 1) {
                    scanFlag_init();
                } else if (work_flag == 0){
                    set_scanFlag(false);
                } else if (work_flag == 2){
                    scanFlag_init();
                }

                edit_barcode.setText("");
                work_item_fullbarcode = "";
                work_item_barcodegoods = "";
            }
        }).show();
```

---

## 5. UI 리스너

### 5.1 inputBtnListener (290~399줄) - 입력 버튼

```java
private View.OnClickListener inputBtnListener = new View.OnClickListener()
```

**동작**:

#### 5.1.1 바코드 스캔 모드 (work_flag=1)

```java
if (work_flag == 1) {
    setBarcodeMsg(edit_barcode.getText().toString());
}
```

#### 5.1.2 수기 입력 모드 (work_flag=0)

```java
else if(work_flag == 0) {
    if (edit_barcode.getText().toString().equals("")) {
        Toast.makeText(getApplicationContext(), "중량을 입력하세요.", Toast.LENGTH_SHORT).show();
        return;
    } else if (current_work_position == -1) {
        Toast.makeText(getApplicationContext(), "작업 중인 지점이 없습니다.", Toast.LENGTH_SHORT).show();
        return;
    } else if (work_ppcode.equals("")) {
        Toast.makeText(getApplicationContext(), "상품패커코드가 없습니다.", Toast.LENGTH_SHORT).show();
        return;
    } else {
        work_item_fullbarcode = "";
        String weight_str = edit_barcode.getText().toString();
        double weight_double = Double.parseDouble(weight_str);

        String temp_weight = "";

        // 이마트 출하대상일 경우
        if(Common.searchType.equals("0")) {
            weight_double = Math.floor(weight_double * 10);
            weight_double = weight_double / 10.0;
            temp_weight = String.format("%.1f", weight_double);
        } else {
            // 생산계근 or 홈플러스 추가계근일 경우
            temp_weight = Double.toString(weight_double);
        }

        weight_double = Double.parseDouble(temp_weight);
        weight_str = String.valueOf(weight_double);

        // 킬코이 + 미트센터 또는 트레이더스/수입육/롯데일 경우
        if (arSM.get(current_work_position).getPACKER_CODE().equals("30228")
            && arSM.get(current_work_position).getSTORE_CODE().equals("9231")) {

            // ExpiryEnterActivity로 이동 (소비기한 입력)
            Intent IntentA = new Intent(ShipmentActivity.this, ExpiryEnterActivity.class);
            IntentA.putExtra("weightStrKey", weight_str);
            IntentA.putExtra("weightDblKey", weight_double);
            IntentA.putExtra("makingFromKey", work_item_bi_info.getMAKINGDATE_FROM());
            IntentA.putExtra("makingToKey", work_item_bi_info.getMAKINGDATE_TO());
            startActivityForResult(IntentA, GET_DATA_REQUEST);

        } else if(arSM.get(current_work_position).getCENTERNAME().contains("TRD")
            || arSM.get(current_work_position).getCENTERNAME().contains("WET")
            || arSM.get(current_work_position).getCENTERNAME().contains("E/T")
            || Common.searchType.equals("6")) {

            // 수입육 계근, 롯데계근일 때 수기입력시 소비기한 창 띄움
            if(Common.searchType.equals("0") || Common.searchType.equals("6")) {
                Intent IntentA = new Intent(ShipmentActivity.this, ExpiryEnterActivity.class);
                // ... (위와 동일)
                startActivityForResult(IntentA, GET_DATA_REQUEST);
            } else {
                wet_data_insert(weight_str, weight_double, "", "");
            }
        } else {
            wet_data_insert(weight_str, weight_double, "", "");
        }

        edit_barcode.setText("");
    }
}
```

#### 5.1.3 상품코드 모드 (work_flag=2)

```java
else if(work_flag == 2) {
    setBarcodeMsg(edit_barcode.getText().toString());
}
```

### 5.2 sendBtnListener (409~447줄) - 전송 버튼

```java
private View.OnClickListener sendBtnListener = new View.OnClickListener()
```

**동작**:

```java
@Override
public void onClick(View v) {
    Log.i(TAG, "Send 버튼 클릭");

    dialog_flag = true;
    new AlertDialog.Builder(ShipmentActivity.this, R.style.AppCompatDialogStyle)
        .setIcon(R.drawable.highland)
        .setTitle(R.string.shipment_wet_send)
        .setMessage(R.string.shipment_wet_send_msg)
        .setCancelable(false)
        .setPositiveButton(R.string.shipment_wet_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog_flag = false;
                new ProgressDlgShipmentSend(ShipmentActivity.this).execute();
            }
        }).setNegativeButton(R.string.shipment_wet_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog_flag = false;
            }
        }).show();
}
```

### 5.3 selectBtnListener (449~469줄) - 선택 버튼

```java
private View.OnClickListener selectBtnListener = new View.OnClickListener()
```

**동작**:

```java
@Override
public void onClick(View v) {
    Log.i(TAG, "Select 버튼 클릭");

    setSelect_Position(-1);

    // 선택된 항목 찾기
    for (int i = 0; i < sListAdapter.cbStatus.size(); i++) {
        if (sListAdapter.cbStatus.get(i)) {
            setSelect_Position(i);
            break;
        }
    }

    if (getSelect_Position() != -1) {
        show_wetDetailDialog(arSM.get(getSelect_Position()), work_item_bi_info, getSelect_Position());
        set_scanFlag(true);
        work_ppcode = "";
    } else {
        Toast.makeText(getApplicationContext(), "지점을 선택해주세요.", Toast.LENGTH_SHORT).show();
        vibrator.vibrate(1000);
    }
}
```

### 5.4 workSelectedListener (1460~1502줄) - 작업 유형 Spinner

```java
private Spinner.OnItemSelectedListener workSelectedListener = new Spinner.OnItemSelectedListener()
```

**동작**:

```java
@Override
public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    Toast.makeText(getApplicationContext(), "현재작업 : " + sp_center_name.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
    edit_barcode.setText("");

    switch (arg2) {
        case 0:        // 바코드
            work_flag = 1;
            scan_flag = true;
            break;

        case 1:        // 수기
            work_flag = 0;
            work_item_fullbarcode = "";
            set_scanFlag(false);

            if (arSM.size() > 0) {
                current_work_position = -1;

                // 계근이 완료되지 않은 지점 찾기
                for (int i = 0; i < arSM.size(); i++) {
                    if (!arSM.get(i).getGI_REQ_PKG().equals(String.valueOf(arSM.get(i).getPACKING_QTY()))) {
                        current_work_position = i;
                        sp_point_name.setSelection(current_work_position);
                        break;
                    }
                }

                if (current_work_position == -1)
                    show_wetFinishDialog();
            }
            break;

        case 2:     // 상품코드
            work_flag = 2;
            scan_flag = true;
            break;
    }
}
```

**작업 유형**:
- 0: 바코드 스캔 (work_flag=1, scan_flag=true)
- 1: 수기 입력 (work_flag=0, scan_flag=false)
- 2: 상품코드 (work_flag=2, scan_flag=true)

### 5.5 emartCenterSelectedListener (1505~1532줄) - 센터명 Spinner

```java
private Spinner.OnItemSelectedListener emartCenterSelectedListener = new Spinner.OnItemSelectedListener()
```

**동작**:

```java
@Override
public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    Toast.makeText(getApplicationContext(), "센터명 : " + sp_center_name.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();

    if (!work_ppcode.equals("")) {
        // 바코드 작업이냐 수기 작업이냐 구분해서 scan_flag값 주기
        if (work_flag == 1) {
            set_scanFlag(true);
            work_ppcode = "";
        } else if(work_flag == 0){
            set_scanFlag(false);
        } else if(work_flag == 2){
            set_scanFlag(true);
            work_ppcode = "";
        }
    }
}
```

### 5.6 emartPointSelectedListener (1534~1559줄) - 지점명 Spinner

```java
private Spinner.OnItemSelectedListener emartPointSelectedListener = new Spinner.OnItemSelectedListener()
```

**동작**:

```java
@Override
public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    Toast.makeText(getApplicationContext(), "작업지점 : " + sp_point_name.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();

    current_work_position = sp_point_name.getSelectedItemPosition();
    sp_point_name.setSelection(current_work_position);

    calc_info(current_work_position);

    // BL 정보 체크
    if(arSM.get(current_work_position).getBL_NO() == ""){
        showAlertDialog("bl", current_work_position+1);
        alert_flag = true;
    } else {
        alert_flag = false;
    }
}
```

#### 5.6.1 calc_info (1561~1583줄)

```java
private void calc_info(int work_position) {
    // BL 번호 Spinner 설정
    ArrayList<String> list_bl = new ArrayList<String>();
    list_bl.add(arSM.get(work_position).getBL_NO());

    ArrayAdapter<String> bl_adapter = new ArrayAdapter<String>(
        ShipmentActivity.this,
        android.R.layout.simple_spinner_item,
        list_bl
    );
    bl_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp_bl_no.setAdapter(bl_adapter);

    work_bl_no = arSM.get(work_position).getBL_NO();

    // 계근 정보 표시
    edit_wet_count.setText(arSM.get(work_position).getGI_REQ_PKG() + " / " + arSM.get(work_position).getPACKING_QTY());
    edit_wet_weight.setText(arSM.get(work_position).getGI_REQ_QTY() + " / " + arSM.get(work_position).getGI_QTY());

    // 상품 정보 표시
    edit_product_name.setText(arSM.get(work_position).getITEM_NAME());
    edit_product_code.setText(arSM.get(work_position).getPACKER_PRODUCT_CODE());

    if (!select_flag) {
        scanFlag_init();
    }
}
```

---

## 6. 출하 유형별 차이점 비교표 (완전판)

### 6.1 전송 방식

| searchType | 출하 유형 | 전송 방식 | 패킷 구분 | URL | complete API |
|-----------|----------|----------|----------|-----|-------------|
| **0** | 이마트 출하 | 개별 건 | - | URL_INSERT_GOODS_WET | ✅ 호출 |
| **1** | 생산 계근 (이노이천) | 일괄 전송 | ## | URL_INSERT_GOODS_WET_NEW | ✅ 호출 |
| **2** | 홈플러스 출하 | 개별 건 | - | URL_INSERT_GOODS_WET_HOMEPLUS | ✅ 호출 |
| **3** | 도매 출하 | 일괄 전송 | ## | URL_INSERT_GOODS_WET_NEW | ❌ **미호출** |
| **4** | 도매 비정량 | 일괄 전송 | ## | URL_INSERT_GOODS_WET_NEW | ❌ **미호출** |
| **5** | 홈플러스 비정량 | 일괄 전송 | ## | URL_INSERT_GOODS_WET_NEW | ❌ **미호출** |
| **6** | 롯데 출하 | 개별 건 | - | URL_INSERT_GOODS_WET_HOMEPLUS | ✅ 호출 |
| **7** | 생산 계근 | 일괄 전송 | ## | URL_INSERT_GOODS_WET_NEW | ✅ 호출 |

### 6.2 중량 처리

| searchType | 출하 유형 | 소수점 처리 | 중량 변환 로직 |
|-----------|----------|-----------|--------------|
| **0** | 이마트 출하 | 1자리 절사 | Math.floor(중량 * 10) / 10.0 |
| **1** | 생산 계근 (이노이천) | 2자리 강제 | DecimalFormat("###.00") |
| **2** | 홈플러스 출하 | 2자리 유지 | 원본 값 |
| **3** | 도매 출하 | 2자리 유지 | 원본 값 |
| **4** | 도매 비정량 | 2자리 유지 | 원본 값 |
| **5** | 홈플러스 비정량 | 2자리 유지 | 원본 값 |
| **6** | 롯데 출하 | 2자리 유지 | 원본 값 + 4자리 변환 |
| **7** | 생산 계근 | 2자리 강제 | DecimalFormat("###.00") |

### 6.3 중복 바코드 체크

| searchType | 출하 유형 | 중복 체크 | 비고 |
|-----------|----------|----------|------|
| **0** | 이마트 출하 | ✅ 체크 | - |
| **1** | 생산 계근 (이노이천) | ✅ 체크 | - |
| **2** | 홈플러스 출하 | ✅ 체크 | - |
| **3** | 도매 출하 | ✅ 체크 | - |
| **4** | 도매 비정량 | ❌ **체크 안함** | 비정량 예외 |
| **5** | 홈플러스 비정량 | ❌ **체크 안함** | 비정량 예외 |
| **6** | 롯데 출하 | ✅ 체크 | - |
| **7** | 생산 계근 | ✅ 체크 | - |

### 6.4 박스 순번 관리

| searchType | 출하 유형 | 박스 순번 방식 | 저장 필드 |
|-----------|----------|--------------|----------|
| **0** | 이마트 출하 | PACKING_QTY + 1 | BOX_CNT |
| **1** | 생산 계근 (이노이천) | PACKING_QTY + 1 | BOX_CNT |
| **2** | 홈플러스 출하 | MAX(BOX_ORDER) + 1 | BOX_ORDER |
| **3** | 도매 출하 | PACKING_QTY + 1 | BOX_CNT |
| **4** | 도매 비정량 | PACKING_QTY + 1 | BOX_CNT |
| **5** | 홈플러스 비정량 | MAX(BOX_ORDER) + 1 | BOX_ORDER |
| **6** | 롯데 출하 | lotte_TryCount (1~9999 순환) | BOXSERIAL (4자리) |
| **7** | 생산 계근 | PACKING_QTY + 1 | BOX_CNT |

### 6.5 프린터 출력

| searchType | 출하 유형 | 프린터 | 출력 메서드 | 특징 |
|-----------|----------|--------|-----------|------|
| **0** | 이마트 출하 | ✅ 필수 | setPrinting() | 10가지 바코드 형식 |
| **1** | 생산 계근 (이노이천) | ❌ **비활성화** | - | 프린터 OFF |
| **2** | 홈플러스 출하 | ✅ 필수 | setHomeplusPrinting() | 바코드 없음 |
| **3** | 도매 출하 | ✅ 필수 | setPrinting() | - |
| **4** | 도매 비정량 | ✅ 필수 | setPrinting() | - |
| **5** | 홈플러스 비정량 | ✅ 필수 | setHomeplusPrinting() | - |
| **6** | 롯데 출하 | ✅ 필수 | setPrintingLotte() | 2개 바코드 |
| **7** | 생산 계근 | ✅ 필수 | setPrinting_prod() | 타임스탬프 바코드 |

### 6.6 소비기한 검증

| searchType | 출하 유형 | 소비기한 필수 조건 |
|-----------|----------|------------------|
| **0** | 이마트 출하 | 킬코이+미트센터, 트레이더스 |
| **1** | 생산 계근 (이노이천) | - |
| **2** | 홈플러스 출하 | - |
| **3** | 도매 출하 | - |
| **4** | 도매 비정량 | - |
| **5** | 홈플러스 비정량 | - |
| **6** | 롯데 출하 | - |
| **7** | 생산 계근 | - |

---

## 7. 특이사항 및 주의점

### 7.1 롯데 박스 순번 관리

**초기화 로직 (2964~2981줄)**:

```java
if(Common.searchType.equals("6")) {
    Shipments_Info si = arSM.get(0);
    lotte_TryCount = Integer.parseInt(si.LAST_BOX_ORDER) + 1;

    if (lotte_TryCount > 9999) {
        lotte_TryCount = 1;
    }

    // 현재 계근된 수량을 더함
    for (int i = 0; i < arSM.size(); i++) {
        lotte_TryCount += arSM.get(i).getPACKING_QTY();
    }

    if (lotte_TryCount > 9999) {
        lotte_TryCount = lotte_TryCount % 9999;
    }
}
```

**증가 로직 (1139~1151줄)**:

```java
if (Common.searchType.equals("6")) {
    lotteBoxOrder = String.valueOf(lotte_TryCount);
    DBHandler.insertqueryGoodsWetLotte(this, gi, lotte_TryCount);

    // DB 저장이 끝난 직후, 다음 계근을 위해 카운터 즉시 증가
    lotte_TryCount++;

    if (lotte_TryCount > 9999) {
        lotte_TryCount = 1;
    }
}
```

**특징**:
- 1~9999 범위에서 순환
- DB의 LAST_BOX_ORDER에서 마지막 번호 조회
- 현재 계근된 수량 합산
- 계근 즉시 카운터 증가

### 7.2 이노이천 생산 계근 프린터 비활성화

**onCreate (220~225줄)**:

```java
if(Common.searchType.equals("1")) {
    Log.i(TAG, "===================PRINTER DISABLE==================");
    swt_print.setChecked(false);   // 인쇄 안함으로 세팅
    swt_print.setClickable(false); // 스위치 불가능하도록 변경
    Common.print_bool = false;     // 이마트 스티커 출력 로직 타지 않도록 false처리
}
```

**이유**:
- 이노이천 생산 계근은 라벨 출력 없음
- 프린터 스위치 비활성화
- 블루투스 연결 안함

### 7.3 미트센터 추가 라벨

**조건 (2348~2426줄, 2428~2504줄)**:
- 바코드 타입: M0
- 지점코드: 9231 (미트센터)

**라벨 종류**:
1. **ERP-미트센터출하코드**: 물류코드=0000000, 공장코드 있음
2. **미트센터출하코드**: 물류코드≠0000000, 공장코드 없음

### 7.4 도매계근 complete_shipment API 미호출

**이유 (3518~3521줄 주석)**:
```
도매계근은 아래 URL을 호출하지 않는다.
GI_D_ID별 CHECK_YN으로 대상을 구분하는데
아래 URL이 CHECK_YN을 N으로 꺾어버리기 때문에
박스 일부 재계근이 불가능해짐
```

**영향**:
- searchType="3", "4", "5"
- complete_shipment API 호출하지 않음
- receiveData = "s" 직접 할당

---

## 8. 개선 가능 영역

### 8.1 코드 중복

**전송 로직 (3315~3558줄)**:
- 개별 건 전송 (이마트/홈플러스/롯데)
- 일괄 전송 (생산/도매)

두 로직이 거의 동일하지만 분기 처리되어 있음. 공통 메서드로 리팩토링 가능.

### 8.2 Magic Number

**롯데 박스 순번**:
```java
if (lotte_TryCount > 9999) {
    lotte_TryCount = 1;
}
```

9999를 상수로 정의 권장:
```java
private static final int LOTTE_BOX_ORDER_MAX = 9999;
```

### 8.3 하드코딩된 조건

**킬코이 + 미트센터 (793줄)**:
```java
if (arSM.get(current_work_position).getPACKER_CODE().equals("30228")
    && arSM.get(current_work_position).getSTORE_CODE().equals("9231"))
```

업체코드/지점코드를 DB 설정으로 관리 권장.

### 8.4 예외 처리 부족

**네트워크 에러**:
```java
if (result.equals("f")) {
    return result;
}
```

네트워크 오류 발생시 사용자에게 재시도 옵션 제공 필요.

---

## 9. 전체 요약

### 9.1 ShipmentActivity 핵심 기능

1. **바코드 스캔**: PM80 스캐너 연동, 패커상품 코드 추출
2. **중량 추출**: ITEM_TYPE별 중량 파싱 및 LB→KG 변환
3. **계근 데이터 저장**: SQLite DB 저장 및 누적 관리
4. **프린터 출력**: Woosim 블루투스 프린터, 출하 유형별 라벨
5. **서버 전송**: HTTP POST, 개별/일괄 전송

### 9.2 출하 유형 (8가지)

| searchType | 출하 유형 | 전송 방식 | 프린터 | 특징 |
|-----------|----------|----------|--------|------|
| 0 | 이마트 출하 | 개별 건 | ✅ | 10가지 바코드 형식 |
| 1 | 생산 계근 (이노이천) | 일괄 (##) | ❌ | 프린터 비활성화 |
| 2 | 홈플러스 출하 | 개별 건 | ✅ | BOX_ORDER 관리 |
| 3 | 도매 출하 | 일괄 (##) | ✅ | complete API 미호출 |
| 4 | 도매 비정량 | 일괄 (##) | ✅ | 중복 허용 |
| 5 | 홈플러스 비정량 | 일괄 (##) | ✅ | 중복 허용 |
| 6 | 롯데 출하 | 개별 건 | ✅ | lotte_TryCount (1~9999) |
| 7 | 생산 계근 | 일괄 (##) | ✅ | 타임스탬프 바코드 |

### 9.3 주요 처리 흐름

```
1. 패커상품 스캔
   └─ find_PackerProduct()
      └─ find_work_info()
         └─ 바코드 정보 조회

2. 출하대상 조회
   └─ ProgressDlgShipSelect
      └─ DBHandler.selectqueryShipment()
         └─ arSM 리스트 생성

3. BL 스캔
   └─ setBarcodeMsg() (scan_flag=false)
      └─ 중량 추출
         └─ ITEM_TYPE별 처리 (W, S, J, B)

4. 계근 데이터 저장
   └─ wet_data_insert()
      └─ DB 저장 (출하 유형별 분기)
      └─ 프린터 출력 (출하 유형별 메서드)

5. 서버 전송
   └─ ProgressDlgShipmentSend
      ├─ 개별 건 전송 (이마트/홈플러스/롯데)
      └─ 일괄 전송 (생산/도매)
```

### 9.4 특수 처리 사항

1. **롯데 박스 순번**: 1~9999 순환, LAST_BOX_ORDER 기반
2. **이노이천 생산**: 프린터 완전 비활성화
3. **미트센터**: 추가 라벨 2종 출력
4. **도매계근**: complete_shipment API 미호출
5. **비정량**: 중복 바코드 허용
6. **소비기한**: 킬코이/트레이더스 필수 검증

### 9.5 코드 품질

**장점**:
- 8가지 출하 유형 지원
- 상세한 예외 처리
- 다양한 Dialog로 사용자 안내

**개선 필요**:
- 코드 중복 (전송 로직)
- Magic Number (9999)
- 하드코딩된 조건 (업체/지점 코드)
- 네트워크 에러 재시도 로직 부족

---

## 10. 관련 파일

- [Part 1: 개요 및 클래스 구조](ShipmentActivity_Part1.md)
- [Part 2: 바코드 스캔 처리](ShipmentActivity_Part2.md)
- [Part 3: 계근 데이터 처리 및 프린터 출력](ShipmentActivity_Part3.md)

---

**작성일**: 2025-01-27
**Part**: 4/4
**이전**: [← ShipmentActivity Part 3](ShipmentActivity_Part3.md)
