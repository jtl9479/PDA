# ShipmentActivity 분석 - Part 3: 계근 데이터 처리 및 프린터 출력

## 목차
- [Part 1: 초기화 및 UI 구성](ShipmentActivity_Part1.md)
- [Part 2: 바코드 스캔 및 데이터 처리](ShipmentActivity_Part2.md)
- **Part 3: 계근 데이터 처리 및 프린터 출력** (현재 문서)
- [Part 4: 다이얼로그 및 보조 기능](ShipmentActivity_Part4.md)

---

## 1. wet_data_insert() - 계근 데이터 저장 및 출력 (1102~1252줄)

### 1.1 메서드 개요
계근 중량을 입력받아 DB에 저장하고 프린터 출력을 수행하는 핵심 메서드입니다.

```java
public void wet_data_insert(String weight_str, double weight_double, String making_date, String box_serial)
```

**매개변수:**
- `weight_str`: 중량 문자열
- `weight_double`: 중량 실수값
- `making_date`: 제조일자
- `box_serial`: 박스 일련번호

### 1.2 계근 완료 체크 (1104~1113줄)

```java
if (arSM.get(current_work_position).getGI_REQ_PKG().equals(String.valueOf(arSM.get(current_work_position).getPACKING_QTY()))) {
    if ((arSM.size() - 1) == current_work_position) {
        show_wetFinishDialog();
    } else {
        Toast.makeText(getApplicationContext(), "계근이 끝난 지점입니다.\n다음 지점을 작업해주세요.", Toast.LENGTH_SHORT).show();
        vibrator.vibrate(300);
    }
    return;
}
```

**로직:**
- 요청수량과 계근수량이 같으면 계근 완료
- 마지막 작업지점이면 완료 다이얼로그 표시
- 그 외는 다음 지점 작업 안내

### 1.3 Goodswets_Info 객체 생성 (1115~1131줄)

```java
Goodswets_Info gi = new Goodswets_Info();
gi.setGI_D_ID(arSM.get(current_work_position).getGI_D_ID());
gi.setWEIGHT(weight_str);
gi.setWEIGHT_UNIT(work_item_bi_info.getBASEUNIT());
gi.setPACKER_PRODUCT_CODE(arSM.get(current_work_position).getPACKER_PRODUCT_CODE());
gi.setBARCODE(work_item_fullbarcode);
gi.setPACKER_CLIENT_CODE(work_item_bi_info.getPACKER_CLIENT_CODE());
gi.setMAKINGDATE(making_date);
gi.setBOXSERIAL(box_serial);
gi.setBOX_CNT(String.valueOf((arSM.get(current_work_position).getPACKING_QTY() + 1)));
gi.setEMARTITEM_CODE(arSM.get(current_work_position).getEMARTITEM_CODE());
gi.setEMARTITEM(arSM.get(current_work_position).getEMARTITEM());
gi.setITEM_CODE(arSM.get(current_work_position).getITEM_CODE());
gi.setBRAND_CODE(arSM.get(current_work_position).getBRAND_CODE());
gi.setREG_ID(Common.REG_ID);
gi.setSAVE_TYPE("F");
gi.setDUPLICATE("F");
```

### 1.4 출하유형별 DB 저장 (1133~1151줄)

```java
String lotteBoxOrder = ""; // 롯데 전용 박스 순번을 담을 변수

if(Common.searchType.equals("2")) { //홈플러스
    int maxBoxOrder = DBHandler.selectMaxBoxOrder(this);
    Log.e(TAG, "=======================MAX BOX ORDER ###=========================" + maxBoxOrder);
    DBHandler.insertqueryGoodsWetHomeplus(this, gi, maxBoxOrder);
} else if (Common.searchType.equals("6")) { //롯데
    // 1. 현재 lotte_TryCount 값을 이 계근 건의 박스 순번으로 확정
    lotteBoxOrder = String.valueOf(lotte_TryCount);
    // 2. 확정된 번호를 사용하여 DB에 저장
    DBHandler.insertqueryGoodsWetLotte(this, gi, lotte_TryCount);
    // 3. DB 저장이 끝난 직후, 다음 계근을 위해 카운터 즉시 증가
    lotte_TryCount++;
    if (lotte_TryCount > 9999) {
        lotte_TryCount = 1;
    }
} else { //이마트
    DBHandler.insertqueryGoodsWet(this, gi);
}
```

**출하유형별 처리:**
- **홈플러스(2)**: 최대 박스 순번 조회 후 저장
- **롯데(6)**: lotte_TryCount 사용하여 4자리 박스 순번 관리
- **이마트(0)**: 일반 저장

### 1.5 중량 변환 처리 (1153~1167줄)

```java
String temp_weight = "";

if(Common.searchType.equals("0")) { //이마트
    weight_double = Math.floor(weight_double * 10);
    weight_double = weight_double / 10.0;
    temp_weight = String.format("%.1f", weight_double);
}else{ //생산 혹은 홈플러스
    temp_weight = Double.toString(weight_double); //생산일 경우 그대로 입력
}

weight_double = Double.parseDouble(temp_weight);
```

**중량 처리:**
- **이마트**: 소수점 첫째자리에서 내림 (floor)
- **홈플러스/생산**: 원본 값 그대로 사용

### 1.6 출하대상 누적 업데이트 (1169~1184줄)

```java
arSM.get(current_work_position).setPACKING_QTY(arSM.get(current_work_position).getPACKING_QTY() + 1);           // 계근수량

if(Common.searchType.equals("0")) {
    arSM.get(current_work_position).setGI_QTY(Math.round((arSM.get(current_work_position).getGI_QTY() + weight_double) * 10.0) / 10.0);    // 계근중량
}else{
    double v1 = arSM.get(current_work_position).getGI_QTY();
    double v2 = weight_double;
    double v3 = v1+v2;
    double v4 = Math.round(v3*1000)/1000.0;
    arSM.get(current_work_position).setGI_QTY(v4);    // 계근중량 변경 후
    Log.e(TAG, "=========================chk prod 계근중량=========================" + v4);
}
```

**중량 누적 처리:**
- **이마트**: 소수점 둘째자리에서 반올림
- **생산**: 소수점 넷째자리에서 반올림

### 1.7 센터 누적 중량 업데이트 (1186~1205줄)

```java
centerWorkCount++;
centerWorkWeight += weight_double;

Log.e(TAG, "=========================센터중량 변환전=========================" + centerWorkWeight);

if(Common.searchType.equals("0")) { //출하일 경우에만 round 처리
    centerWorkWeight = Math.round(centerWorkWeight * 100.0) / 100.0;
}else{ //생산일 경우
    centerWorkWeight = Math.round(centerWorkWeight*1000)/1000.0; //생산일 경우 소수점 넷째자리에서 반올림
}

Log.e(TAG, "=========================센터중량 변환후=========================" + centerWorkWeight);

edit_center_tcount.setText(centerTotalCount + " / " + centerWorkCount);

if(Common.searchType.equals("0")) { //출하일때
    edit_center_tweight.setText(Math.round(centerTotalWeight * 10) / 10.0 + " / " + centerWorkWeight);
}else{ //생산일때
    edit_center_tweight.setText(centerTotalWeight + " / " + centerWorkWeight);
}

edit_wet_count.setText(arSM.get(current_work_position).getGI_REQ_PKG() + " / " + arSM.get(current_work_position).getPACKING_QTY());
edit_wet_weight.setText(arSM.get(current_work_position).getGI_REQ_QTY() + " / " + arSM.get(current_work_position).getGI_QTY());
```

### 1.8 프린터 출력 호출 (1223~1240줄)

```java
if (Common.print_bool) {
    if (Common.searchType.equals("2") || Common.searchType.equals("5")) {
        Log.d(TAG, "===========홈플 출력 시작 ================");
        setHomeplusPrinting(weight_double, arSM.get(current_work_position), false);
    }else if(Common.searchType.equals("0")){
        Log.d(TAG, "===========이마트 출력 시작 ================");
        setPrinting(weight_double, arSM.get(current_work_position), false, making_date);
    }else if(Common.searchType.equals("4")){
        Log.d(TAG, "===========이마트(비정량) 출력 시작 ================");
        setPrinting(weight_double, arSM.get(current_work_position), false, making_date);
    }else if(Common.searchType.equals("6")){
        Log.d(TAG, "===========롯데 출력 시작 ================");
        setPrintingLotte(weight_double, arSM.get(current_work_position), false, making_date, lotteBoxOrder);
    }else if(Common.searchType.equals("7")){
        Log.d(TAG, "===========생산 출력 시작 ================");
        setPrinting_prod(weight_double, arSM.get(current_work_position), false);
    }
}
```

**출하유형별 출력 메서드:**
- **홈플러스(2,5)**: setHomeplusPrinting()
- **이마트(0,4)**: setPrinting()
- **롯데(6)**: setPrintingLotte()
- **생산(7)**: setPrinting_prod()

### 1.9 계근 완료 체크 (1244~1251줄)

```java
if (Integer.parseInt(arSM.get(current_work_position).getGI_REQ_PKG()) <= arSM.get(current_work_position).getPACKING_QTY()) {
    // 요청수량과 계근수량이 같을 때 (계근이 끝났을 때)
    if ((centerTotalCount > 0) && (centerTotalCount == centerWorkCount)) {       // 총 계근 완료
        show_wetFinishDialog();
    } else {
        //show_wetNextDialog();
    }
}
```

---

## 2. setPrinting() - 이마트 라벨 출력 (1682~2513줄)

### 2.1 메서드 시그니처

```java
public String setPrinting(double weight_double, Shipments_Info si, boolean reprint, String making_date)
```

**매개변수:**
- `weight_double`: 중량
- `si`: 출하 정보 객체
- `reprint`: 재출력 여부
- `making_date`: 제조일자

### 2.2 소비기한 계산 로직 (1697~1790줄)

#### 2.2.1 미트센터 특수 처리 (1699~1742줄)

```java
if (si.getPACKER_CODE().equals("30228") && si.getSTORE_CODE().equals("9231")) { //킬코이 , 미트센터 납품분

    String rawExp = "20"+making_date;
    Log.e(TAG, "rawExp chk : " + rawExp);

    int shelfLiftToInt = Integer.parseInt(work_item_bi_info.getSHELF_LIFE()) - 1; //1일을 뺀다.

    SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd");
    Calendar cal = Calendar.getInstance();
    Date dt = null;
    try {
        dt = dtFormat.parse(rawExp);
    } catch (ParseException e) {
        e.printStackTrace();
    }
    cal.setTime(dt);
    //쉘프라이프를 더한다.
    cal.add(Calendar.DATE,  shelfLiftToInt);
    String expireDateCalc = dtFormat.format(cal.getTime());

    //dash 처리 위해서 잘라서 다시 붙임
    String YYYY = expireDateCalc.substring(0,4);
    String MM = expireDateCalc.substring(4,6);
    String DD = expireDateCalc.substring(6);
    String YYYYMMDD = YYYY+"-"+MM+"-"+DD;

    if(arSM.get(current_work_position).getBARCODE_TYPE().equals("M3") ||
       arSM.get(current_work_position).getBARCODE_TYPE().equals("M4")){
        expiryDayConvert = "소비기한: "+YYYYMMDD;
    }else{
        expiryDayConvert = "/소비기한 : "+YYYYMMDD;
    }
}
```

**처리 과정:**
1. 제조일자에 "20" 접두어 추가 (ex: "170730" → "20170730")
2. SHELF_LIFE에서 1일을 뺀 값 계산
3. 제조일자에 SHELF_LIFE 추가하여 소비기한 계산
4. YYYY-MM-DD 형식으로 변환

#### 2.2.2 트레이더스 센터 처리 (1744~1789줄)

```java
if(Common.searchType.equals("0")){ //이마트 수입육 계근일때만
    if (si.getCENTERNAME().contains("E/T")  ||  si.getCENTERNAME().contains("WET")  ||  si.getCENTERNAME().contains("TRD")) {

        String rawExp = "20"+making_date;
        int shelfLiftToInt = Integer.parseInt(work_item_bi_info.getSHELF_LIFE()) - 1;

        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        Date dt = null;
        try {
            dt = dtFormat.parse(rawExp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal.setTime(dt);
        cal.add(Calendar.DATE,  shelfLiftToInt);
        String expireDateCalc = dtFormat.format(cal.getTime());

        String YYYY = expireDateCalc.substring(0,4);
        String MM = expireDateCalc.substring(4,6);
        String DD = expireDateCalc.substring(6);
        String YYYYMMDD = YYYY+"-"+MM+"-"+DD;

        if(arSM.get(current_work_position).getBARCODE_TYPE().equals("M3") ||
           arSM.get(current_work_position).getBARCODE_TYPE().equals("M4")){
            expiryDayConvert = "소비기한: "+YYYYMMDD;
        }else{
            expiryDayConvert = "/소비기한: "+YYYYMMDD;
        }
    }
}
```

### 2.3 업체코드 설정 (1792~1804줄)

```java
String pCompCode = "610933";
String pCompName = "(주)하이랜드이노베이션";
```

**참고:**
- 모든 건이 하이랜드이노베이션으로 출고됨

### 2.4 중량 변환 (1816~1863줄)

```java
// 소수점 한자리 이후 절사
String print_weight_str = "";
Double print_weight_double = 0.0;
String weight_ = String.valueOf(weight_double);
String weight_str = String.valueOf(weight_double);
String[] weight_sp = weight_str.split("\\.");
String print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 1);
weight_double = Double.parseDouble(print_weight);

// W, J 구분
if (si.getITEM_TYPE().equals("W") || si.getITEM_TYPE().equals("HW") ) {
    print_weight_double = weight_double;
    print_weight_str = String.valueOf(print_weight_double);
} else if (si.getITEM_TYPE().equals("J")) {
    print_weight_str = si.getPACKWEIGHT();
    print_weight_double = Double.parseDouble(print_weight_str);
}

// .을 지워서 숫자만으로 표시
String temp = print_weight_str.replace(".", "");
int iLen = temp.length();

for (int i = 0; i < 6 - iLen; i++) {
    temp = "0" + temp;            // ex) 198 -> 000198
}

print_weight_str = temp;
```

**처리 과정:**
1. 소수점 첫째자리까지만 유지
2. W/HW 타입: 실제 중량 사용
3. J 타입: 고정 중량(PACKWEIGHT) 사용
4. 6자리 형식으로 변환 (앞자리 0 채움)

### 2.5 바코드 형식별 바코드 생성 (1865~2052줄)

#### 2.5.1 M0 형식 (1866~1882줄)

```java
case "M0":
    // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
    break;
```

**형식:** `상품코드6자리` + `중량6자리` + `회사코드` + `수입식별번호12자리`

**예시:** `123456` + `000198` + `610933` + `123456789012`

#### 2.5.2 M1 형식 (1883~1898줄)

```java
case "M1":
    // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드
    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode;
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;
    break;
```

**형식:** `상품코드6자리` + `중량6자리` + `회사코드`

#### 2.5.3 M3 형식 (1899~1915줄)

```java
case "M3":
    // 납품코드 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
    break;
```

**특징:** PC매입용 형식

#### 2.5.4 M4 형식 (1916~1931줄)

```java
case "M4":
    // 납품코드 + 중량 6자리 + 회사코드
    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode;
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode;
    break;
```

#### 2.5.5 E0 형식 (1932~1948줄)

```java
case "E0":
    // 에브리데이 상품코드 형식 1
    // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
    break;
```

#### 2.5.6 E1 형식 (1949~1964줄)

```java
case "E1":
    // 에브리데이 상품코드 형식 2
    // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 111111111111
    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + "111111111111";
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " 111111111111";

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + pCompCode + "111111111111";
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " 111111111111";
    break;
```

**특징:** 수입식별번호 대신 "111111111111" 고정값 사용

#### 2.5.7 E2 형식 (1965~1981줄)

```java
case "E2":
    // 에브리데이 상품코드 형식 3
    // 상품코드 13자리 + 수입식별번호(12자리)  = 25
    pBarcode = si.getEMARTITEM_CODE().toString() + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().toString() + " " + si.getIMPORT_ID_NO();

    pBarcode2 = si.getEMARTLOGIS_CODE().toString() + si.getIMPORT_ID_NO();
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().toString() + " " + si.getIMPORT_ID_NO();
    break;
```

**특징:** 중량 정보가 바코드에 포함되지 않음

#### 2.5.8 E3 형식 (1982~1994줄)

```java
case "E3":
    // 에브리데이 상품코드 형식 4
    // 상품코드 13자리
    pBarcode = si.getEMARTITEM_CODE();
    pBarcodeStr = si.getEMARTITEM_CODE();

    pBarcode2 = si.getEMARTLOGIS_CODE();
    pBarcodeStr2 = si.getEMARTLOGIS_CODE();
    break;
```

**특징:** 상품코드만 사용

#### 2.5.9 M9 형식 (2012~2033줄)

```java
case "M9":
    // 이마트 우육 센터납
    // 상품코드 앞자리 6 자리 + 중량 6자리 + 회사코드 + 수입식별번호(12자리)
    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    pBarcode2 = si.getEMARTITEM_CODE().substring(0, 6) +  si.getIMPORT_ID_NO() +  si.getUSE_CODE();
    pBarcodeStr2 = si.getEMARTITEM_CODE().substring(0, 6) + " " + si.getIMPORT_ID_NO() + " " + si.getUSE_CODE();

    //제품명 + 용도
    pBarcodeStr3 = si.EMARTITEM +","+si.getUSE_NAME();

    break;
```

**특징:**
- 우육 센터납용
- 두 번째 바코드에 USE_CODE 포함
- 용도명 정보 추가

#### 2.5.10 M8 형식 (2035~2051줄)

```java
case "M8":
    // 이마트 비정량 납품분
    pBarcode = si.getEMARTITEM_CODE().substring(0, 6) + print_weight_str + pCompCode + si.getIMPORT_ID_NO();
    pBarcodeStr = si.getEMARTITEM_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();

    pBarcode2 = si.getEMARTLOGIS_CODE().substring(0, 6) + si.getIMPORT_ID_NO() + print_weight_str + pCompCode ;
    pBarcodeStr2 = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + pCompCode + " " + si.getIMPORT_ID_NO();
    break;
```

### 2.6 Woosim 프린터 명령 (2078~2346줄)

#### 2.6.1 프린터 초기화 및 설정 (2080~2084줄)

```java
ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
try {
    byteStream.write(WoosimCmd.initPrinter());                                // 프린터 설정 초기화
    byteStream.write(WoosimCmd.setPageMode());
    byteStream.write(WoosimCmd.selectTTF("HYWULM.TTF"));
    byteStream.write(WoosimCmd.setTextStyle(true, false, false, 1, 1));
```

**Woosim 명령:**
- `initPrinter()`: 프린터 초기화
- `setPageMode()`: 페이지 모드 설정
- `selectTTF("HYWULM.TTF")`: 한글 폰트 선택
- `setTextStyle(bold, italic, underline, width, height)`: 텍스트 스타일

#### 2.6.2 센터명 출력 (2086~2096줄)

```java
if (7 < si.CENTERNAME.length()) {
    byteStream.write(WoosimCmd.PM_setPosition(10, 12));
    byteStream.write(WoosimCmd.getTTFcode(35, 35, si.CENTERNAME));
} else {
    byteStream.write(WoosimCmd.PM_setPosition(10, 10));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, si.CENTERNAME));
}
```

**로직:**
- 센터명 길이가 7자 초과: 35pt 크기
- 7자 이하: 40pt 크기

#### 2.6.3 지점명/업체명 출력 (2098~2134줄)

```java
if (si.getBARCODE_TYPE().equals("M3") || si.getBARCODE_TYPE().equals("M4")) {
    // PC매입: 업체명 출력 생략
} else if(si.getBARCODE_TYPE().equals("M9")){
    String vendorName = "[(주)하이랜드이노베이션]";
    byteStream.write(WoosimCmd.PM_setPosition(330, 13));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, vendorName));

    String storeNamePlusCode = pointName + "(" +  si.getSTORE_CODE() +")";

    if (11 < si.CLIENTNAME.toString().length()) {
        byteStream.write(WoosimCmd.PM_setPosition(10, 270));
        byteStream.write(WoosimCmd.getTTFcode(35, 35, storeNamePlusCode.toString()));
    } else {
        byteStream.write(WoosimCmd.PM_setPosition(10, 270));
        byteStream.write(WoosimCmd.getTTFcode(40, 40, storeNamePlusCode.toString()));
    }

    String usePurpose = "[저울 스캔용]";
    byteStream.write(WoosimCmd.PM_setPosition(400, 270));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, usePurpose));
}else {
    if (11 < si.CLIENTNAME.toString().length()) {
        byteStream.write(WoosimCmd.PM_setPosition(10, 60));
        byteStream.write(WoosimCmd.getTTFcode(35, 35, pointName.toString()));
    } else {
        byteStream.write(WoosimCmd.PM_setPosition(10, 60));
        byteStream.write(WoosimCmd.getTTFcode(40, 40, pointName.toString()));
    }
}
```

#### 2.6.4 상품명 출력 (2142~2156줄)

```java
if (si.getBARCODE_TYPE().equals("M3") || si.getBARCODE_TYPE().equals("M4")) {
    byteStream.write(WoosimCmd.PM_setPosition(15, 65));
}else if(si.getBARCODE_TYPE().equals("M9")){
    byteStream.write(WoosimCmd.PM_setPosition(15, 70));
}else {
    byteStream.write(WoosimCmd.PM_setPosition(80, 120));
}

if (si.EMARTITEM.length() > 14) {
    byteStream.write(WoosimCmd.getTTFcode(35, 35, si.EMARTITEM));
} else {
    byteStream.write(WoosimCmd.getTTFcode(40, 40, si.EMARTITEM));
}
```

#### 2.6.5 지점코드 바코드 출력 (2168~2184줄)

```java
byte[] STORECODE128 = WoosimBarcode.createBarcode(WoosimBarcode.CODE128, 2, 60, false, sBarcode.getBytes());

if (!si.getBARCODE_TYPE().equals("M9")){
    byteStream.write(WoosimCmd.PM_setPosition(420, 20));
    byteStream.write(STORECODE128);
}

if (!si.getBARCODE_TYPE().equals("M9")){
    byteStream.write(WoosimCmd.PM_setPosition(450, 80));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, sBarcodeStr));
}
```

**WoosimBarcode.createBarcode 매개변수:**
- 바코드 타입: CODE128
- 모듈 너비: 2
- 바코드 높이: 60
- HRI 출력: false (바코드 하단 숫자 출력 안함)
- 데이터: 바이트 배열

#### 2.6.6 메인 바코드 출력 (2186~2242줄)

```java
byte[] CODE128 = WoosimBarcode.createBarcode(WoosimBarcode.CODE128, 2, 60, false, pBarcode.getBytes());

// 바코드 타입에 따른 위치 설정
if (si.getBARCODE_TYPE().equals("M0") || si.getBARCODE_TYPE().equals("E0")
        || si.getBARCODE_TYPE().equals("E1") || si.getBARCODE_TYPE().equals("M8")) {
    byteStream.write(WoosimCmd.PM_setPosition(80, 170));
} else if (si.getBARCODE_TYPE().equals("M1")) {
    byteStream.write(WoosimCmd.PM_setPosition(145, 170));
} else if (si.getBARCODE_TYPE().equals("E2")) {
    byteStream.write(WoosimCmd.PM_setPosition(90, 170));
} else if (si.getBARCODE_TYPE().equals("E3")) {
    byteStream.write(WoosimCmd.PM_setPosition(160, 170));
} else if (si.getBARCODE_TYPE().equals("M3")) {
    byteStream.write(WoosimCmd.PM_setPosition(70, 115));
} else if (si.getBARCODE_TYPE().equals("M4")) {
    byteStream.write(WoosimCmd.PM_setPosition(145, 115));
}else if(si.getBARCODE_TYPE().equals("M9")){
    byteStream.write(WoosimCmd.PM_setPosition(90, 125));
}

byteStream.write(CODE128);

// 바코드 번호(숫자) 출력
if (si.getBARCODE_TYPE().equals("M0") || si.getBARCODE_TYPE().equals("E0") ||
    si.getBARCODE_TYPE().equals("E1") || si.getBARCODE_TYPE().equals("M8")) {
    byteStream.write(WoosimCmd.PM_setPosition(75, 240));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcodeStr));
} if (si.getBARCODE_TYPE().equals("M1")) {
    byteStream.write(WoosimCmd.PM_setPosition(147, 240));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcodeStr));
} else if (si.getBARCODE_TYPE().equals("E2")) {
    byteStream.write(WoosimCmd.PM_setPosition(100, 240));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcodeStr));
} else if (si.getBARCODE_TYPE().equals("E3")) {
    byteStream.write(WoosimCmd.PM_setPosition(190, 240));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcodeStr));
} else if (si.getBARCODE_TYPE().equals("M3")) {
    byteStream.write(WoosimCmd.PM_setPosition(25, 175));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcodeStr + "  PC매입"));
} else if (si.getBARCODE_TYPE().equals("M4")) {
    byteStream.write(WoosimCmd.PM_setPosition(117, 175));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcodeStr + "  PC매입"));
}else if (si.getBARCODE_TYPE().equals("M9")) {
    byteStream.write(WoosimCmd.PM_setPosition(90, 192));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcodeStr ));
}
```

#### 2.6.7 M9 하단 바코드 및 정보 출력 (2252~2267줄)

```java
if(si.getBARCODE_TYPE().equals("M9")){
    byteStream.write(WoosimCmd.PM_setPosition(125, 325));
    byteStream.write(LOGISCODE128);

    byteStream.write(WoosimCmd.PM_setPosition(450, 330));
    String ctName = si.getCT_NAME();
    byteStream.write(WoosimCmd.getTTFcode(25, 25, ctName ));

    byteStream.write(WoosimCmd.PM_setPosition(125, 390));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcodeStr2 ));

    byteStream.write(WoosimCmd.PM_setPosition(80, 420));
    String belowBarcodeString = si.EMARTITEM +","+si.getUSE_NAME();
    byteStream.write(WoosimCmd.getTTFcode(25, 25, belowBarcodeString ));
}
```

#### 2.6.8 중량 및 업체 정보 출력 (2277~2322줄)

```java
if (si.getBARCODE_TYPE().equals("M3") || si.getBARCODE_TYPE().equals("M4")) {
    byteStream.write(WoosimCmd.PM_setPosition(15, 300));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, "중     량 : "));
    byteStream.write(WoosimCmd.PM_setPosition(175, 300));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(print_weight_double) + " KG"));
    byteStream.write(WoosimCmd.PM_setPosition(15, 348));

    String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";

    byteStream.write(WoosimCmd.getTTFcode(30, 30, "납품일자 : " + tempDate));
    byteStream.write(WoosimCmd.PM_setPosition(15, 388));

    if (reprint) {
        pCompName = pCompName + "  *";
    }
    byteStream.write(WoosimCmd.getTTFcode(30, 30, "업        체 : " + pCompCode + "   " + pCompName));
    byteStream.write(WoosimCmd.PM_setPosition(15, 428));
    byteStream.write(WoosimCmd.getTTFcode(30, 30, expiryDayConvert));

}else if(si.getBARCODE_TYPE().equals("M9")) {

    byteStream.write(WoosimCmd.PM_setPosition(90, 220));
    String tempDate = si.getSTORE_IN_DATE().substring(0, 4) + "년 " + si.getSTORE_IN_DATE().substring(4, 6) + "월 " + si.getSTORE_IN_DATE().substring(6, 8) + "일";
    byteStream.write(WoosimCmd.getTTFcode(30, 30, "납품일 : " + tempDate));

} else {
    byteStream.write(WoosimCmd.PM_setPosition(15, 280));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, "중      량 : "));
    byteStream.write(WoosimCmd.PM_setPosition(175, 280));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(print_weight_double) + " KG"));
    byteStream.write(WoosimCmd.PM_setPosition(15, 328));

    String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";

    byteStream.write(WoosimCmd.getTTFcode(30, 30, "납품일자 : " + tempDate));
    byteStream.write(WoosimCmd.PM_setPosition(15, 368));

    if (reprint) {
        pCompName = pCompName + "  *";
    }
    byteStream.write(WoosimCmd.getTTFcode(30, 30, "업체코드 : " + pCompCode + expiryDayConvert));
    byteStream.write(WoosimCmd.PM_setPosition(15, 408));
    byteStream.write(WoosimCmd.getTTFcode(30, 30, "업 체 명 : " + pCompName));
}
```

#### 2.6.9 보관구역 표시 (2324~2333줄)

```java
whArea = si.getWH_AREA();

if(whArea != null || !whArea.equals("")){
    byteStream.write(WoosimCmd.PM_setPosition(430, 385));
    byteStream.write(WoosimCmd.getTTFcode(65, 65, whArea));
}
```

#### 2.6.10 출력 완료 및 전송 (2335~2346줄)

```java
if(si.getBARCODE_TYPE().equals("M9")) {
    byteStream.write(WoosimImage.drawLine(0, 260, 560, 260, 5));
}

byteStream.write(WoosimCmd.PM_setArea(0, 0, 576, 460));
byteStream.write(WoosimCmd.PM_printData());
byteStream.write(WoosimCmd.PM_setStdMode());

if( !si.getBARCODE_TYPE().equals("P0") ) {
    sendData(byteStream.toByteArray());
    sendData(WoosimCmd.feedToMark());
}
```

**Woosim 명령:**
- `PM_setArea(x, y, width, height)`: 출력 영역 설정
- `PM_printData()`: 페이지 모드 데이터 출력
- `PM_setStdMode()`: 표준 모드로 전환
- `feedToMark()`: 라벨 자르기 위치까지 용지 이송

### 2.7 미트센터 추가 라벨 출력 (2348~2504줄)

#### 2.7.1 ERP-미트센터출하코드 (2348~2426줄)

```java
if (si.getBARCODE_TYPE().equals("M0") && si.getSTORE_CODE().equals("9231") &&
    si.getEMARTLOGIS_CODE().equals("0000000") && !si.getEMART_PLANT_CODE().equals("")) {

    String meatCenterTitle = "ERP-미트센터출하코드";
    String meatCenterCode = "059015";

    // 새 페이지 시작
    byteStream.reset();

    byteStream.write(WoosimCmd.initPrinter());
    byteStream.write(WoosimCmd.setPageMode());
    byteStream.write(WoosimCmd.selectTTF("HYWULM.TTF"));
    byteStream.write(WoosimCmd.setTextStyle(true, false, false, 1, 1));

    byteStream.write(WoosimCmd.PM_setPosition(120, 35));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, meatCenterTitle));

    byteStream.write(WoosimCmd.PM_setPosition(115, 120));
    if (si.EMARTITEM.length() > 14) {
        byteStream.write(WoosimCmd.getTTFcode(35, 35, si.EMARTITEM));
    } else {
        byteStream.write(WoosimCmd.getTTFcode(40, 40, si.EMARTITEM));
    }

    byteStream.write(WoosimCmd.PM_setPosition(35, 170));

    meatCenterBarcode = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + meatCenterCode + si.getIMPORT_ID_NO() + si.getEMART_PLANT_CODE();
    meatCenterBarcodeStr = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + meatCenterCode + " " + si.getIMPORT_ID_NO() + " " + si.getEMART_PLANT_CODE();

    byte[] MEATCENTERBARCODE128 = WoosimBarcode.createBarcode(WoosimBarcode.CODE128, 2, 60, false, meatCenterBarcode.getBytes());

    byteStream.write(MEATCENTERBARCODE128);

    byteStream.write(WoosimCmd.PM_setPosition(40, 240));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, meatCenterBarcodeStr));

    // ... 중량, 납품일자, 업체코드, 업체명 출력 ...

    byteStream.write(WoosimCmd.PM_setArea(0, 0, 576, 460));
    byteStream.write(WoosimCmd.PM_printData());
    byteStream.write(WoosimCmd.PM_setStdMode());

    sendData(byteStream.toByteArray());
    sendData(WoosimCmd.feedToMark());
}
```

**조건:**
- 바코드 타입: M0
- 지점코드: 9231 (미트센터)
- 물류코드: 0000000
- 공장코드: 있음

**바코드 형식:** `물류코드6자리` + `중량6자리` + `미트센터코드(059015)` + `수입식별번호` + `공장코드`

#### 2.7.2 미트센터출하코드 (2428~2504줄)

```java
if (si.getBARCODE_TYPE().equals("M0") && si.getSTORE_CODE().equals("9231") &&
    !si.getEMARTLOGIS_CODE().equals("0000000") && si.getEMART_PLANT_CODE().equals("")) {

    String meatCenterTitle = "미트센터출하코드";
    String meatCenterCode = "059015";

    // ... 위와 유사한 출력 로직 ...

    meatCenterBarcode = si.getEMARTLOGIS_CODE().substring(0, 6) + print_weight_str + meatCenterCode + si.getIMPORT_ID_NO();
    meatCenterBarcodeStr = si.getEMARTLOGIS_CODE().substring(0, 6) + " " + print_weight_str + " " + meatCenterCode + " " + si.getIMPORT_ID_NO();

    // ... 출력 처리 ...
}
```

**조건:**
- 바코드 타입: M0
- 지점코드: 9231 (미트센터)
- 물류코드: 0000000이 아님
- 공장코드: 없음

**바코드 형식:** `물류코드6자리` + `중량6자리` + `미트센터코드(059015)` + `수입식별번호`

---

## 3. setHomeplusPrinting() - 홈플러스 라벨 출력 (2515~2637줄)

### 3.1 메서드 시그니처

```java
public String setHomeplusPrinting(double weight_double, Shipments_Info si, boolean reprint)
```

### 3.2 중량 변환 (2528~2549줄)

```java
String print_weight_str = "";
Double print_weight_double = 0.0;
String weight_ = String.valueOf(weight_double);
String weight_str = String.valueOf(weight_double);
String[] weight_sp = weight_str.split("\\.");
String print_weight = "";

if(weight_sp[1].length() > 1){
    print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 2);
}else if(weight_sp[1].length() == 1){
    print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 1);
}else{
    print_weight = weight_sp[0];
}

weight_double = Double.parseDouble(print_weight);
print_weight_double = weight_double;

pointCode = si.EMARTLOGIS_CODE.toString();
storeCode = si.STORE_CODE.toString();
pointName = si.CLIENTNAME.toString();
```

**특징:**
- 소수점 둘째자리까지 유지 (이마트와 다름)
- 지점코드: EMARTLOGIS_CODE 사용

### 3.3 프린터 출력 (2556~2627줄)

```java
ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
try {
    byteStream.write(WoosimCmd.initPrinter());
    byteStream.write(WoosimCmd.setPageMode());
    byteStream.write(WoosimCmd.selectTTF("HYWULM.TTF"));
    byteStream.write(WoosimCmd.setTextStyle(true, false, false, 1, 1));
    byteStream.write(WoosimCmd.PM_setArea(0, 0, 510, 590));
    byteStream.write(WoosimCmd.PM_setDirection(1));  // 방향 회전

    // 지점명
    if(pointName.length() >6) {
        byteStream.write(WoosimCmd.PM_setPosition(30, 170));
        byteStream.write(WoosimCmd.getTTFcode(70, 70, pointName.toString()));
    }else{
        byteStream.write(WoosimCmd.PM_setPosition(30, 170));
        byteStream.write(WoosimCmd.getTTFcode(100, 100, pointName.toString()));
    }

    // 지점코드/점포코드
    byteStream.write(WoosimCmd.PM_setPosition(135, 170));
    if (si.getITEM_TYPE().equals("B")) {
        byteStream.write(WoosimCmd.getTTFcode(155, 155, storeCode.toString()));  // 홈플러스 비정량
    } else {
        byteStream.write(WoosimCmd.getTTFcode(155, 155, pointCode.toString()));  // 지점코드
    }

    // 상품명
    if (si.EMARTITEM.length() > 17) {
        byteStream.write(WoosimCmd.PM_setPosition(287, 170));
        byteStream.write(WoosimCmd.getTTFcode(25, 25, si.EMARTITEM));
    } else {
        byteStream.write(WoosimCmd.PM_setPosition(283, 170));
        byteStream.write(WoosimCmd.getTTFcode(30, 30, si.EMARTITEM));
    }

    // BOX 표시
    byteStream.write(WoosimCmd.PM_setPosition(322, 170));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, "BOX"));

    // CT_CODE
    byteStream.write(WoosimCmd.PM_setPosition(361, 170));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(si.getCT_CODE())));

    // 중량 / 수입식별번호 뒷 4자리
    byteStream.write(WoosimCmd.PM_setPosition(361, 380));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(print_weight_double) + "/"+si.getIMPORT_ID_NO().substring(8, 12)));

    // 납품일자
    byteStream.write(WoosimCmd.PM_setPosition(402, 170));
    String tempDate = si.getSTORE_IN_DATE().substring(0,4) + "년 " + si.getSTORE_IN_DATE().substring(4,6) + "월 " + si.getSTORE_IN_DATE().substring(6,8) + "일";
    byteStream.write(WoosimCmd.getTTFcode(40, 40, tempDate));

    // 업체명
    byteStream.write(WoosimCmd.PM_setPosition(441, 170));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, pCompName));

    byteStream.write(WoosimCmd.PM_printData());
    byteStream.write(WoosimCmd.PM_setStdMode());

    sendData(byteStream.toByteArray());
    sendData(WoosimCmd.feedToMark());
}
```

**특징:**
- 출력 영역: 510 x 590 (이마트: 576 x 460)
- 방향 회전: PM_setDirection(1) 사용
- 바코드 없이 텍스트만 출력
- 수입식별번호 뒷 4자리 표시

---

## 4. setPrintingLotte() - 롯데 라벨 출력 (2640~2902줄)

### 4.1 메서드 시그니처

```java
public String setPrintingLotte(double weight_double, Shipments_Info si, boolean reprint, String making_date, String box_order)
```

**매개변수:**
- `box_order`: 롯데 전용 박스 순번 (4자리)

### 4.2 중량 4자리 변환 로직 (2656~2729줄)

```java
String[] weight_sp = weight_str.split("\\.");
String print_weight = "";

if(Common.searchType.equals("6")){
    String chk = weight_sp[1];
    if(chk.length() >=2){
        print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 2);
    }else{
        print_weight = weight_sp[0] + "." + weight_sp[1].substring(0, 1);
    }
}

String temp = print_weight_str.replace(".", "");
int iLen = temp.length();

if(iLen == 4){ //17.36의 경우
    for (int i = 0; i < 6 - iLen; i++) {
        temp = "0" + temp;
    }
}else if(iLen == 3){ //17.7의 경우
    for (int i = 0; i < 5 - iLen; i++) {
        temp = "0" + temp;
    }
    if(print_weight_double >= 10) { // 10이상 인경우 뒤에 0붙임 ex 001770 : 17.7kg
        temp = temp + "0";
    } else { // 10미만인 경우 앞에 0 붙임 ex) 000177 : 1.77kg
        temp = "0"+ temp;
    }
}else if(iLen == 2){ //17의 경우
    for (int i = 0; i < 4 - iLen; i++) {
        temp = "0" + temp;
    }
    if(print_weight_double >= 10) {  //10이상인 경우 ex 001700 : 17kg
        temp = temp + "00";
    } else if(print_weight_double < 10 && print_weight_double > 1) { // ex) 000170 : 1.7kg
        temp = "0" + temp + "0";
    }
}

print_weight_str = temp;
```

**중량 변환 예시:**

| 실제 중량 | 소수점 분리 | 길이 | 변환 로직 | 최종 결과 |
|----------|------------|------|----------|----------|
| 17.36 kg | 1736 | 4 | 앞에 00 추가 | 001736 |
| 17.7 kg | 177 | 3 | 앞에 00, 뒤에 0 추가 | 001770 |
| 1.77 kg | 177 | 3 | 앞에 000 추가 | 000177 |
| 17 kg | 17 | 2 | 앞에 00, 뒤에 00 추가 | 001700 |
| 1.7 kg | 17 | 2 | 앞에 000, 뒤에 0 추가 | 000170 |

### 4.3 L0 바코드 형식 (2732~2767줄)

```java
switch (si.getBARCODE_TYPE()) {
    case "L0":
        // 박스 순번 4자리 형식 생성
        String boxserial_cnt = "";

        if (box_order != null && !box_order.isEmpty()) {
            boxserial_cnt = String.format("%04d", Integer.parseInt(box_order));
        } else {
            Log.e(TAG, "setPrintingLotte: box_order가 null 또는 empty입니다.");
            return "";
        }

        // 바코드: 회사코드 + 제조일자 + 중량4자리 + 마트제품코드6자리 + 박스번호4자리
        pBarcode = pCompCode_lotte + making_date + print_weight_str.substring(print_weight_str.length()-4, print_weight_str.length()) + si.getEMARTITEM_CODE().substring(0, 6) + boxserial_cnt;
        pBarcodeStr = pCompCode_lotte + making_date + print_weight_str.substring(print_weight_str.length()-4, print_weight_str.length()) + si.getEMARTITEM_CODE().substring(0, 6) + boxserial_cnt;

        pBarcode2 = si.getIMPORT_ID_NO();  // 이력번호
        pBarcodeStr2 = si.getIMPORT_ID_NO();

        break;
}
```

**바코드 구성:**
- 회사코드 (EMARTLOGIS_CODE)
- 제조일자 (6자리, YYMMDD)
- 중량 뒤 4자리
- 상품코드 앞 6자리
- 박스번호 4자리

**예시:**
```
회사코드: 123456
제조일자: 211225
중량: 001736 (뒤 4자리: 1736)
상품코드: 789012
박스번호: 0001

바코드: 123456 211225 1736 789012 0001
```

### 4.4 프린터 출력 (2784~2892줄)

```java
ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
try {
    byteStream.write(WoosimCmd.initPrinter());
    byteStream.write(WoosimCmd.setPageMode());
    byteStream.write(WoosimCmd.selectTTF("HYWULM.TTF"));
    byteStream.write(WoosimCmd.setTextStyle(true, false, false, 1, 1));

    // 상품명
    byteStream.write(WoosimCmd.PM_setPosition(10, 12));
    byteStream.write(WoosimCmd.getTTFcode(35, 35, si.EMARTITEM));

    // 메인 바코드 (중량 바코드)
    byte[] CODE128 = WoosimBarcode.createBarcode(WoosimBarcode.CODE128, 2, 60, false, pBarcode.getBytes());

    if(si.getBARCODE_TYPE().equals("L0")){
        byteStream.write(WoosimCmd.PM_setPosition(100, 80));
    }

    byteStream.write(CODE128);

    // 바코드 번호(숫자)
    if (si.getBARCODE_TYPE().equals("L0")){
        byteStream.write(WoosimCmd.PM_setPosition(114, 139));
        byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcodeStr));
    }

    // 이력번호 바코드
    byte[] CODE128_2 = WoosimBarcode.createBarcode(WoosimBarcode.CODE128, 2, 60, false, pBarcode2.getBytes());

    if (si.getBARCODE_TYPE().equals("L0")) {
        byteStream.write(WoosimCmd.PM_setPosition(150, 350));
        byteStream.write(CODE128_2);

        byteStream.write(WoosimCmd.PM_setPosition(155, 410));
        byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcode2));
    }

    // 중량, 납품처, 제조일자, 이력번호
    if (si.getBARCODE_TYPE().equals("L0")){
        byteStream.write(WoosimCmd.PM_setPosition(15, 180));
        byteStream.write(WoosimCmd.getTTFcode(40, 40, "중      량 : "));
        byteStream.write(WoosimCmd.PM_setPosition(175, 180));
        byteStream.write(WoosimCmd.getTTFcode(40, 40, String.valueOf(print_weight_double) + " KG"));

        byteStream.write(WoosimCmd.PM_setPosition(15, 228));
        byteStream.write(WoosimCmd.getTTFcode(30, 30, "납품처 : " + pCompName));

        byteStream.write(WoosimCmd.PM_setPosition(15, 268));
        String tempDate = "20"+ making_date.substring(0,2) + "년 " + making_date.substring(2,4) + "월 " + making_date.substring(4,6) + "일";
        byteStream.write(WoosimCmd.getTTFcode(30, 30, "제조일자 : " + tempDate));

        byteStream.write(WoosimCmd.PM_setPosition(15, 313));
        byteStream.write(WoosimCmd.getTTFcode(30, 30, "이력(묶음)번호 : " + si.getIMPORT_ID_NO()));
    }

    // 보관구역
    whArea = si.getWH_AREA();
    if(whArea != null || !whArea.equals("")){
        byteStream.write(WoosimCmd.PM_setPosition(385, 305));
        byteStream.write(WoosimCmd.getTTFcode(65, 65, whArea));
    }

    // 테두리 및 구분선
    byteStream.write(WoosimImage.drawBox(0, 0, 560, 440, 3));

    if(si.getBARCODE_TYPE().equals("L0")) {
        byteStream.write(WoosimImage.drawLine(0, 60, 560, 60, 3));
        byteStream.write(WoosimImage.drawLine(0, 180, 560, 180, 3));
        byteStream.write(WoosimImage.drawLine(0, 345, 560, 345, 3));
    }

    byteStream.write(WoosimCmd.PM_setArea(0, 0, 576, 460));
    byteStream.write(WoosimCmd.PM_printData());
    byteStream.write(WoosimCmd.PM_setStdMode());

    sendData(byteStream.toByteArray());
    sendData(WoosimCmd.feedToMark());
}
```

**특징:**
- 2개의 바코드 (중량 바코드, 이력번호 바코드)
- 테두리 박스 및 3개의 수평선
- 제조일자 표시 (납품일자가 아님)

---

## 5. setPrinting_prod() - 생산 라벨 출력 (1585~1680줄)

### 5.1 메서드 시그니처

```java
public String setPrinting_prod(double weight_double, Shipments_Info si, boolean reprint)
```

### 5.2 중량 변환 (1594~1613줄)

```java
String weight_str = String.valueOf(weight_double);
String print_weight_str = String.valueOf(weight_double);

// 소수점 둘째자리까지 채우기
String weight_00 = "";
DecimalFormat decimalFormat = new DecimalFormat("###.00");
weight_00 = decimalFormat.format(weight_double);

// .을 지워서 숫자만으로 표시
String temp = weight_00.replace(".", "");
int iLen = temp.length();

for (int i = 0; i < 6 - iLen; i++) {
    temp = "0" + temp;            // ex) 198 -> 000198
}
print_weight_str = temp;
```

**특징:**
- 소수점 둘째자리까지 강제 표시 (DecimalFormat)
- 6자리 형식으로 변환

### 5.3 바코드 생성 (1615~1623줄)

```java
// 바코드 형식 : 상품코드 + 중량 6자리 + 00 + 연월일시분초
SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssSS");
Date time = new Date();
String now = dateFormat.format(time);

pBarcode = si.getEMARTITEM_CODE() + print_weight_str + "00" + now;
pBarcodeStr = si.getEMARTITEM_CODE() + print_weight_str + "00" + now;
```

**바코드 구성:**
- 상품코드 전체
- 중량 6자리
- 고정값 "00"
- 연월일시분초 (14자리, yyMMddHHmmssSS)

**예시:**
```
상품코드: 1234567890123
중량: 001980
고정값: 00
타임스탬프: 21122515304512

바코드: 1234567890123 001980 00 21122515304512
```

### 5.4 프린터 출력 (1626~1670줄)

```java
ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
try {
    byteStream.write(WoosimCmd.initPrinter());
    byteStream.write(WoosimCmd.setPageMode());
    byteStream.write(WoosimCmd.selectTTF("HYWULM.TTF"));
    byteStream.write(WoosimCmd.setTextStyle(true, false, false, 1, 1));

    // 상품명 / 냉장냉동
    byteStream.write(WoosimCmd.PM_setPosition(50, 120));
    if (si.EMARTITEM.length() > 14) {
        byteStream.write(WoosimCmd.getTTFcode(35, 35, si.EMARTITEM + " / " + si.ITEM_SPEC));
    } else {
        byteStream.write(WoosimCmd.getTTFcode(40, 40, si.EMARTITEM + " / " + si.ITEM_SPEC));
    }

    // 바코드
    byte[] CODE128 = WoosimBarcode.createBarcode(WoosimBarcode.CODE128, 2, 60, false, pBarcode.getBytes());
    byteStream.write(WoosimCmd.PM_setPosition(50, 190));
    byteStream.write(CODE128);

    // 바코드 번호
    byteStream.write(WoosimCmd.PM_setPosition(45, 260));
    byteStream.write(WoosimCmd.getTTFcode(25, 25, pBarcodeStr));

    // 중량
    byteStream.write(WoosimCmd.PM_setPosition(50, 340));
    byteStream.write(WoosimCmd.getTTFcode(40, 40, "중      량   :   " + weight_str + " KG"));

    byteStream.write(WoosimCmd.PM_setArea(0, 0, 576, 460));
    byteStream.write(WoosimCmd.PM_printData());
    byteStream.write(WoosimCmd.PM_setStdMode());

    sendData(byteStream.toByteArray());
    sendData(WoosimCmd.feedToMark());
}
```

**특징:**
- 간단한 레이아웃 (상품명/냉장냉동, 바코드, 중량)
- ITEM_SPEC (냉장/냉동) 정보 표시
- 타임스탬프 기반 고유 바코드

---

## 6. 라벨 형식 비교표

### 6.1 출하유형별 라벨 형식

| 출하유형 | searchType | 메서드 | 중량 소수점 | 바코드 포함 |
|---------|-----------|--------|------------|-----------|
| 이마트 | 0 | setPrinting() | 1자리 절사 | O |
| 홈플러스 | 2, 5 | setHomeplusPrinting() | 2자리 유지 | X |
| 롯데 | 6 | setPrintingLotte() | 2자리 유지 | O (2개) |
| 생산 | 7 | setPrinting_prod() | 2자리 강제 | O |

### 6.2 이마트 바코드 형식 비교

| 타입 | 용도 | 바코드 구성 | 길이 |
|-----|------|-----------|------|
| M0 | 일반 | 상품코드6 + 중량6 + 회사코드 + 수입식별번호12 | 30 |
| M1 | 일반 | 상품코드6 + 중량6 + 회사코드 | 18 |
| M3 | PC매입 | 납품코드6 + 중량6 + 회사코드 + 수입식별번호12 | 30 |
| M4 | PC매입 | 납품코드6 + 중량6 + 회사코드 | 18 |
| M8 | 비정량 | 상품코드6 + 중량6 + 회사코드 + 수입식별번호12 | 30 |
| M9 | 우육센터 | 상품코드6 + 중량6 + 회사코드 + 수입식별번호12 | 30 |
| E0 | 에브리데이 | 상품코드6 + 중량6 + 회사코드 + 수입식별번호12 | 30 |
| E1 | 에브리데이 | 상품코드6 + 중량6 + 회사코드 + "111111111111" | 30 |
| E2 | 에브리데이 | 상품코드13 + 수입식별번호12 | 25 |
| E3 | 에브리데이 | 상품코드13 | 13 |

### 6.3 박스 순번 관리 비교

| 출하유형 | 박스 순번 관리 방식 | 형식 | 저장 위치 |
|---------|------------------|------|----------|
| 이마트 | PACKING_QTY + 1 | 정수 | BOX_CNT |
| 홈플러스 | MAX(BOX_ORDER) + 1 | 정수 | maxBoxOrder |
| 롯데 | lotte_TryCount | 4자리 (0001~9999) | BOXSERIAL |
| 생산 | PACKING_QTY + 1 | 정수 | BOX_CNT |

---

## 7. Woosim 프린터 명령어 리스트

### 7.1 초기화 및 모드 설정

| 명령어 | 설명 | 사용 예시 |
|-------|------|---------|
| `initPrinter()` | 프린터 초기화 | 출력 시작 시 |
| `setPageMode()` | 페이지 모드 설정 | 라벨 출력용 |
| `PM_setStdMode()` | 표준 모드로 전환 | 출력 완료 후 |

### 7.2 폰트 및 텍스트

| 명령어 | 설명 | 매개변수 |
|-------|------|---------|
| `selectTTF(fontName)` | 트루타입 폰트 선택 | "HYWULM.TTF" (한글) |
| `setTextStyle(bold, italic, underline, width, height)` | 텍스트 스타일 | true/false, 배율 |
| `getTTFcode(width, height, text)` | 텍스트 출력 | 폰트 크기, 문자열 |

### 7.3 위치 및 영역

| 명령어 | 설명 | 매개변수 |
|-------|------|---------|
| `PM_setPosition(x, y)` | 커서 위치 설정 | x, y 좌표 (픽셀) |
| `PM_setArea(x, y, width, height)` | 출력 영역 설정 | 영역 크기 |
| `PM_setDirection(direction)` | 출력 방향 설정 | 0~3 (회전 각도) |

### 7.4 바코드

| 명령어 | 설명 | 매개변수 |
|-------|------|---------|
| `WoosimBarcode.createBarcode(type, moduleWidth, height, hri, data)` | 바코드 생성 | CODE128, 모듈 너비, 높이, HRI 출력 여부, 데이터 |

**바코드 타입:**
- `WoosimBarcode.CODE128`: CODE128 바코드

### 7.5 그래픽

| 명령어 | 설명 | 매개변수 |
|-------|------|---------|
| `WoosimImage.drawBox(x, y, width, height, thickness)` | 박스 그리기 | 위치, 크기, 선 굵기 |
| `WoosimImage.drawLine(x1, y1, x2, y2, thickness)` | 선 그리기 | 시작점, 끝점, 선 굵기 |

### 7.6 출력 및 용지 제어

| 명령어 | 설명 | 사용 위치 |
|-------|------|---------|
| `PM_printData()` | 페이지 모드 데이터 출력 | 출력 직전 |
| `feedToMark()` | 라벨 자르기 위치까지 이송 | 출력 직후 |
| `cutPaper(mode)` | 용지 자르기 | 옵션 (미사용) |

### 7.7 좌표 시스템

**페이지 모드 좌표:**
- 원점: 왼쪽 상단 (0, 0)
- X축: 오른쪽 방향 (가로)
- Y축: 아래쪽 방향 (세로)
- 단위: 픽셀

**일반적인 라벨 크기:**
- 이마트/롯데/생산: 576 x 460
- 홈플러스: 510 x 590 (회전)

---

## 8. 주요 처리 흐름도

### 8.1 계근 데이터 처리 흐름

```
wet_data_insert()
  │
  ├─ 1. 계근 완료 체크
  │   └─ 요청수량 == 계근수량 → return
  │
  ├─ 2. Goodswets_Info 객체 생성
  │
  ├─ 3. 출하유형별 DB 저장
  │   ├─ 홈플러스(2): insertqueryGoodsWetHomeplus()
  │   ├─ 롯데(6): insertqueryGoodsWetLotte() + lotte_TryCount++
  │   └─ 이마트(0): insertqueryGoodsWet()
  │
  ├─ 4. 중량 변환
  │   ├─ 이마트: floor(중량 * 10) / 10
  │   └─ 생산/홈플러스: 원본 값
  │
  ├─ 5. 출하대상 누적 업데이트
  │   ├─ PACKING_QTY++
  │   └─ GI_QTY += weight_double
  │
  ├─ 6. 센터 누적 업데이트
  │   ├─ centerWorkCount++
  │   └─ centerWorkWeight += weight_double
  │
  ├─ 7. UI 업데이트
  │
  ├─ 8. 프린터 출력 (print_bool == true)
  │   ├─ 홈플러스(2,5): setHomeplusPrinting()
  │   ├─ 이마트(0,4): setPrinting()
  │   ├─ 롯데(6): setPrintingLotte()
  │   └─ 생산(7): setPrinting_prod()
  │
  └─ 9. 전체 계근 완료 체크
      └─ centerTotalCount == centerWorkCount → show_wetFinishDialog()
```

### 8.2 이마트 라벨 출력 흐름

```
setPrinting()
  │
  ├─ 1. 소비기한 계산
  │   ├─ 미트센터(30228, 9231)
  │   └─ 트레이더스(E/T, WET, TRD)
  │
  ├─ 2. 중량 변환
  │   ├─ 소수점 1자리까지 절사
  │   ├─ W/HW: 실제 중량
  │   ├─ J: 고정 중량(PACKWEIGHT)
  │   └─ 6자리 형식 변환
  │
  ├─ 3. 바코드 생성 (바코드 타입별)
  │   ├─ M0, M1, M3, M4, M8, M9
  │   └─ E0, E1, E2, E3
  │
  ├─ 4. 프린터 출력
  │   ├─ 초기화 및 설정
  │   ├─ 센터명
  │   ├─ 지점명/업체명
  │   ├─ 상품명
  │   ├─ 지점코드 바코드
  │   ├─ 메인 바코드
  │   ├─ 중량 및 업체 정보
  │   └─ 보관구역
  │
  └─ 5. 미트센터 추가 라벨 (조건부)
      ├─ ERP-미트센터출하코드
      └─ 미트센터출하코드
```

### 8.3 롯데 라벨 출력 흐름

```
setPrintingLotte()
  │
  ├─ 1. 중량 4자리 변환
  │   ├─ 소수점 2자리까지 유지
  │   └─ 길이별 처리 (2, 3, 4자리)
  │
  ├─ 2. L0 바코드 생성
  │   ├─ box_order → 4자리 형식 (0001~9999)
  │   └─ 회사코드 + 제조일자 + 중량4자리 + 상품코드6 + 박스번호4
  │
  ├─ 3. 프린터 출력
  │   ├─ 상품명
  │   ├─ 중량 바코드
  │   ├─ 이력번호 바코드
  │   ├─ 중량, 납품처, 제조일자
  │   ├─ 보관구역
  │   └─ 테두리 및 구분선
  │
  └─ 4. 출력 완료
```

---

## 9. 코드 분석 요약

### 9.1 주요 기능

1. **계근 데이터 관리**: Goodswets_Info 객체 생성 및 DB 저장
2. **중량 변환**: 출하유형별 소수점 처리 (절사/반올림)
3. **누적 계산**: 출하대상별 및 센터 전체 누적 관리
4. **프린터 출력**: 출하유형 및 바코드 타입별 라벨 생성
5. **소비기한 계산**: 제조일자 + SHELF_LIFE - 1일

### 9.2 핵심 로직

**중량 처리:**
- 이마트: floor(중량 * 10) / 10 (소수점 1자리 내림)
- 홈플러스/롯데: 소수점 2자리 유지
- 생산: DecimalFormat("###.00") (소수점 2자리 강제)

**박스 순번:**
- 이마트/생산: PACKING_QTY + 1
- 홈플러스: MAX(BOX_ORDER) + 1
- 롯데: lotte_TryCount (1~9999 순환)

**바코드 생성:**
- M0~M9, E0~E3: 10가지 형식
- L0: 롯데 전용 형식
- 생산: 타임스탬프 기반 고유 바코드

### 9.3 Woosim 프린터 특징

**페이지 모드:**
- 좌표 기반 절대 위치 지정
- 출력 전 메모리에 레이아웃 구성
- PM_printData()로 일괄 출력

**한글 지원:**
- HYWULM.TTF 트루타입 폰트 사용
- getTTFcode()로 한글 출력

**바코드:**
- CODE128 형식 사용
- WoosimBarcode.createBarcode()로 생성

---

## 10. 관련 파일

- **ShipmentActivity_Part1.md**: 초기화 및 UI 구성
- **ShipmentActivity_Part2.md**: 바코드 스캔 및 데이터 처리
- **ShipmentActivity_Part4.md**: 다이얼로그 및 보조 기능

---

*이 문서는 ShipmentActivity.java의 1102~2902줄 (계근 데이터 처리 및 프린터 출력 부분)을 분석한 것입니다.*
