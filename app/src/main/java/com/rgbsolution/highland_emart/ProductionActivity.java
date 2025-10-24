package com.rgbsolution.highland_emart;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.common.HttpHelper;
import com.rgbsolution.highland_emart.scanner.ScannerActivity;

import com.rgbsolution.highland_emart.db.DBHandler;
import java.util.ArrayList;

public class ProductionActivity extends ScannerActivity {

    private final String TAG = "ProductionActivity";

    private ProgressDialog pDialog = null;
    private Spinner sp_work;
    private EditText edit_barcode;  // 스캔한 바코드 정보
    private EditText edit_pp_code;

    private EditText total_box_count;
    private EditText total_weight;
    private EditText weight_of_box;

    private EditText edit_packer_code;
    private Button btn_input;
    private Button btn_input_barcode_info;
    private Button btn_reset;

    private int work_flag = 1;       //  1 바코드스캔 , 2 수기입력

    private String weightFrom;
    private String weightTo;
    private String zeroPoint;
    private String baseUnit;

    private boolean communicationOrNot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "=====================Common.searchType Check==================" + Common.searchType);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production_calc);

        edit_pp_code = (EditText) findViewById(R.id.edit_pp_code);  //ppcode 입력창
        edit_packer_code = (EditText) findViewById(R.id.edit_packer_code); //packer code 입력창
        edit_barcode = (EditText) findViewById(R.id.edit_barcode); //바코드/수기 입력창

        total_box_count = (EditText) findViewById(R.id.total_box_count); //ppcode 입력창
        total_weight = (EditText) findViewById(R.id.total_weight);    //packer code 입력창
        weight_of_box = (EditText) findViewById(R.id.weight_of_box); //바코드/수기 입력창

        btn_input = (Button) findViewById(R.id.btn_input);
        btn_input.setOnClickListener(inputBtnListener);

        btn_input_barcode_info = (Button) findViewById(R.id.btn_input_get_barcode_info);
        btn_input_barcode_info.setOnClickListener(inputBarcodeBtnListener);

        btn_reset = (Button) findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(resetBtnListener);

        total_box_count.setText("0");
        total_weight.setText("0");
        weight_of_box.setText("0");

        sp_work = (Spinner) findViewById(R.id.sp_work);
        sp_work.setOnItemSelectedListener(workSelectedListener);

        swt_print.setChecked(false); //인쇄 안함으로 세팅
        swt_print.setClickable(false); //스위치 불가능하도록 변경

        /*edit_pp_code.setText("22872");
        edit_packer_code.setText("30360");*/
        //혹시 모르니 프로그램 들어갈 때 한번 더 지워줌
        DBHandler.deleteGoodsWetProductionCalc(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, TAG + " onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, TAG + " onStart");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, TAG + " onPause");
    }

    private void hideKeyboard() {
        InputMethodManager btn_input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        btn_input.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private View.OnClickListener inputBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Log.i(TAG, "계근 입력버튼 클릭");
            hideKeyboard();

                if (work_flag == 1) {     // 바코드 스캔 작업

                } else if (work_flag == 0) { //중량 수기입력작업
                    if (edit_barcode.getText().toString().equals("")) {      // 중량값이 입력되지 않았을 때
                        Toast.makeText(getApplicationContext(), "중량을 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (edit_packer_code.getText().toString().equals("") || edit_pp_code.getText().toString().equals("")) { //패커코드 혹은 pp코드를 입력하지 않고 중량을 입력하려 할 때
                        Toast.makeText(getApplicationContext(), "PACKER 혹은 PP코드를 입력 후 계근을 진행하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    calcAndEnterWeight("manual","");
                }
        }
    };

    private View.OnClickListener inputBarcodeBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.i(TAG, "바코드 정보 수신버튼 클릭");
            if (edit_packer_code.getText().toString().equals("") || edit_pp_code.getText().toString().equals("")) { //패커코드 혹은 pp코드를 입력하시 않고 바코드정보 다운을 진행할 때
                Toast.makeText(getApplicationContext(), "PACKER 혹은 PP코드를 입력 후 다운로드를 바코드정보 수신을 진행하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            getBarcodeInfo(); //바코드 정보 수신
            hideKeyboard();
        }
    };

    private View.OnClickListener resetBtnListener = new View.OnClickListener() { //리셋버튼 리스너
        @Override
        public void onClick(View v) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(ProductionActivity.this);

                dialog.setTitle("리셋 여부 확인")
                .setMessage("계근 정보를 리셋 하시겠습니까?(계근한 바코드 정보도 같이 리셋됩니다.)")
                .setIcon(android.R.drawable.ic_menu_save)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 확인시 처리 로직
                        total_box_count.setText("0");
                        total_weight.setText("0");
                        weight_of_box.setText("0");
                        edit_barcode.setText("");
                        sp_work.setSelection(0);
                        DBHandler.deleteGoodsWetProductionCalc(getApplicationContext());
                        Toast.makeText(ProductionActivity.this, "리셋을 완료했습니다.", Toast.LENGTH_SHORT).show();
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 취소시 처리 로직
                        Toast.makeText(ProductionActivity.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }})
                .show();
        }
    };

    private Spinner.OnItemSelectedListener workSelectedListener = new Spinner.OnItemSelectedListener() { //SPINNER 리스너
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            try {
                switch (arg2) {
                    case 0:        // 바코드
                        work_flag = 1;
                        //scan_flag = true;
                        Log.i(TAG, "바코드 클릭");
                        break;
                    case 1:        // 수기
                        work_flag = 0;
                        Log.i(TAG, "수기 클릭");
                        edit_barcode.setText("");
                        break;
                }
            } catch (Exception ex) {
                Log.e(TAG, "======== workSelectedListener Exception ========");
                Log.e(TAG, ex.getMessage().toString());
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    //바코드 스캔시 이 메서드를 무조건 탐
    protected void setMessage(String msg) {
        if (Common.D) {
            Log.d(TAG, "바코드스캐너 입력값 : " + msg);
        }

        if(!communicationOrNot){ //바코드 정보를 수신하지 않고 바코드 스캔을 진행할 경우
            Toast.makeText(getApplicationContext(), "바코드 정보를 수신한 후에 바코드 스캔을 진행해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edit_packer_code.getText().toString().equals("") || edit_pp_code.getText().toString().equals("")) { //패커코드나 PP코드를 입력하지 않고 계근 진행할 경우
            Toast.makeText(getApplicationContext(), "PACKER 혹은 PP코드를 입력 후 계근을 진행하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (msg != null) {
            if (work_flag == 1) { //바코드 스캐너로 입력
                setBarcodeMsg(msg);
            } else if (work_flag == 0) { //수기로 입력
                setBarcodeMsg(msg); //수기에 놓고도 바코드 스캔할 수 있음
            } else if (work_flag == 2) {
                setBarcodeMsg(msg);
            }
        }
    }

    //바코드 스캔의 결과
    public void setBarcodeMsg(final String msg) {

        sp_work.setSelection(0); //스피너를 바코드로 일단 돌린다.

        try {
            edit_barcode.setText(msg);
            calcAndEnterWeight("scan", msg);
        } catch (Exception ex) {
            Log.e(TAG, "setBarcodeMsg Exception -> " + ex.getMessage().toString());
        }
    }

    protected void getBarcodeInfo(){

        boolean successFlag = false;

        if(!communicationOrNot){
            Log.d(TAG, "getting barcode info start");
            try{
                SelectWeightFromTo task = new SelectWeightFromTo(ProductionActivity.this);
                String[] test = new String[2];
                test[0] = edit_packer_code.getText().toString();
                test[1] = edit_pp_code.getText().toString();
                String temp = task.execute(test).get();

                if(temp!=null) {
                    String[] tempArray = temp.split("::");

                    weightFrom = tempArray[0].toString();
                    weightTo = tempArray[1].toString();
                    zeroPoint = tempArray[2].toString();
                    baseUnit = tempArray[3].toString();

                    Log.d(TAG, "======== error0 weightFrom ======== : " + weightFrom);
                    Log.d(TAG, "======== error0 weightTo ======== : " + weightTo);
                    Log.d(TAG, "======== error0 zeroPoint ======== : " + zeroPoint);
                    Log.d(TAG, "======== error0 baseUnit ======== : " + baseUnit);

                    successFlag = true;
                }

            } catch (Exception ex) {
                Log.e(TAG, "======== Production Activity Exception ========");
                Log.e(TAG, ex.toString());
            }

            if(successFlag){
                communicationOrNot = true; //한번 바코드정보 통신 후에는 더이상 안하게
                setUseableEditText( edit_pp_code,false); //바코드 정보 받아온 후에는 수정 불가능하게 변경
                setUseableEditText( edit_packer_code,false); //바코드 정보 받아온 후에는 수정 불가능하게 변경

                Toast.makeText(getApplicationContext(), "바코드 중량 정보 세팅됨, 계근을 시작하세요.", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(getApplicationContext(), "입력하신 PACKER CODE / PPCODE 에 해당하는 바코드 정보가 없습니다. 입력하신 정보를 다시 확인해 주세요.", Toast.LENGTH_SHORT).show();
            }

        }

    }

    protected void calcAndEnterWeight(String mode, String barcode){

        Log.d(TAG, "calcAndEnterWeight start");

        String barcodeCount = DBHandler.selectGoodsWetProductionCalc(getApplicationContext(), barcode);

        if(barcodeCount.equals("0")){
            DBHandler.insertGoodsWetProductionCalc(getApplicationContext(), barcode);
        }else{
            if(!edit_packer_code.getText().toString().equals("31341")){
                Toast.makeText(getApplicationContext(), "바코드를 중복으로 스캔하셨습니다. 다른 바코드를 스캔해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Integer startBoxCnt = Integer.parseInt(total_box_count.getText().toString());
        Float totalWeight = Float.parseFloat(total_weight.getText().toString());  //토탈박스중량

        String totalWeightFormated = "";
        String item_weight_str = "";
        startBoxCnt = startBoxCnt + 1;

        total_box_count.setText(startBoxCnt.toString());

        if(mode.equals("manual")) {

            totalWeight = totalWeight + Float.parseFloat(edit_barcode.getText().toString());
            totalWeightFormated = String.format("%.2f", totalWeight);

        }else if(mode.equals("scan")){

            Log.d(TAG, "weightfrom,to:" + weightFrom+":" + weightTo+ ":");
            String item_weight = "";            // 상품 최초 중량 절사값(XXXX)
            Double item_weight_double = 0.0;    // 상품 Double 중량값
                   // 상품 String 중량값
            String work_item_fullbarcode = barcode;
            double item_pow = 0;              // 상품 zeroPoint에 대한 pow

            String weightFromVar = weightFrom.trim(); //클래스변수는 parseInt 할때 에러나서 멤버변수에 넣어줘야함, 어떠한 이유인지는 모르겠으나 db에서 받으온 파라미터값에 공백이 있어 parseInt시 에러가 나서 trim처리
            String weightToVar = weightTo.trim();
            String zeroPointVar = zeroPoint.trim();
            String baseUnitVar = baseUnit.trim();

            Log.d(TAG, "work_item_fullbarcode " + work_item_fullbarcode);
            Log.d(TAG, "weightFrom TEST " + weightFromVar);
            Log.d(TAG, "weightTo TEST " + weightToVar);

            item_weight = work_item_fullbarcode.substring(
                    Integer.parseInt(weightFromVar) - 1, Integer.parseInt(weightToVar)
            );

            Log.i(TAG, "Type W | 절사한 중량값 : " + item_weight);

            item_pow = Math.pow(10, Integer.parseInt(zeroPointVar))  ;
            item_weight_double = Double.parseDouble(item_weight) / item_pow;

            if ("LB".equals(baseUnitVar)) {
                // LB(파운드)라면 KG으로 환산 LB * 0.453592 = KG
                double temp_weight_double = item_weight_double * 0.453592;

                Log.i(TAG, "temp_weight_double 계산 전 : " + temp_weight_double);
                Log.i(TAG, "math.floor: " + Math.floor(temp_weight_double * item_pow));

                item_weight_double = Math.floor(temp_weight_double * 100) / 100; //lb 변환 후 소수점 두자리까지 처리하도록 변경
                item_weight_str = String.valueOf(item_weight_double);

                Log.i(TAG, "LB->KG | 환산 중량 item_pow : " + item_pow);
                Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + item_weight_double);
                Log.i(TAG, "LB->KG | 환산 중량 String값 : " + item_weight_str);
            }

            String temp_weight = String.format("%.2f", item_weight_double);

            item_weight_double = Double.parseDouble(temp_weight);
            item_weight_str = String.valueOf(item_weight_double);

            Log.i(TAG, "Type W | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
            Log.i(TAG, "Type W | ZeroPoint 적용 중량 String값 : " + item_weight_str);

            totalWeight = totalWeight + Float.parseFloat(item_weight_str);
            totalWeightFormated = String.format("%.2f", totalWeight);

        }

        total_weight.setText(totalWeightFormated);

        if(mode.equals("manual")){
            weight_of_box.setText(edit_barcode.getText().toString()) ;
            Toast.makeText(getApplicationContext(), "중량 정보 "+edit_barcode.getText().toString()+" kg 입력됨", Toast.LENGTH_SHORT).show();
        }else{
            weight_of_box.setText(item_weight_str) ;
            Toast.makeText(getApplicationContext(), "중량 정보 "+item_weight_str+" kg 입력됨", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUseableEditText(EditText et, boolean useable) {
        et.setClickable(useable);
        et.setEnabled(useable);
        et.setFocusable(useable);
        et.setFocusableInTouchMode(useable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " onDestroy, table data delete");
        DBHandler.deleteGoodsWetProductionCalc(getApplicationContext());
    }

    class SelectWeightFromTo extends AsyncTask<String, String, String> {

        private Context mContext;
        String receiveData = "";

        public SelectWeightFromTo(Context context) {
            mContext = context;
        }

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

        @Override
        protected String doInBackground(String ... values) {

            String result = "";

            try {

                String packerCode = values[0];
                String packerProductCode = values[1];

                Log.d(TAG, "============ packerCode ============== : " + packerCode);
                Log.d(TAG, "============ packerProductCode ========: " + packerProductCode);

                String packet = "AND a.PACKER_CODE = '"+packerCode+"'"+ " AND a.PACKER_PRODUCT_CODE = '"+packerProductCode+"'";
                result = HttpHelper.getInstance().sendDataDb(packet, "inno", "search_production_calc", Common.URL_WET_PRODUCTION_CALC);

            } catch (Exception ex) {
                Log.e(TAG, "======== selectWeightFromTo doInBackgounrd Exception ========");
                Log.e(TAG, ex.toString());
                return null;
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String _result) {
            Log.v(TAG, "전송결과 : " + _result);
            pDialog.dismiss();
            super.onPostExecute(_result);
        }
    }
}

