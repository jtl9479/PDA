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
import com.rgbsolution.highland_emart.scanner.HoneywellScannerActivity;

import com.rgbsolution.highland_emart.db.DBHandler;
import java.util.ArrayList;

/**
 * 생산 계근 계산기 Activity
 *
 * 바코드 스캔 또는 수기 입력을 통해 박스별 중량을 누적 계산하는 화면.
 * 프린터 출력 기능은 비활성화되어 있으며, 순수하게 중량 계산만 수행한다.
 *
 * [주요 기능]
 * - 바코드 스캔: 바코드에서 중량 정보를 추출하여 자동 계산
 * - 수기 입력: 바코드 스캔이 불가능한 경우 중량을 직접 입력
 * - 박스 개수 누적: 스캔/입력된 박스의 총 개수 계산
 * - 총 중량 누적: 모든 박스의 중량을 합산
 * - 중복 체크: 동일 바코드의 중복 스캔 방지 (패커코드 31341 제외)
 * - 단위 변환: LB(파운드) → KG 자동 환산
 *
 * [사용 흐름]
 * 1. PACKER_CODE, PP_CODE 입력
 * 2. [바코드 정보 수신] 버튼으로 중량 위치 정보 조회
 * 3. 바코드 스캔 또는 수기 입력으로 중량 누적
 * 4. 필요시 [리셋] 버튼으로 초기화
 *
 * @see ScannerActivity 바코드 스캐너 기능 상속
 * @see SelectWeightFromTo 바코드 중량 정보 조회 AsyncTask
 */
public class ProductionActivity extends HoneywellScannerActivity {
    private final String TAG = "ProductionActivity";

    // ========================================================================================
    // UI 컴포넌트
    // ========================================================================================
    /** 로딩 다이얼로그 */
    private ProgressDialog pDialog = null;

    /** 작업 방식 선택 스피너 (0: 바코드, 1: 수기) */
    private Spinner sp_work;

    /** 바코드 스캔값 또는 수기 중량 입력 필드 */
    private EditText edit_barcode;

    /** PP(Packer Product) 코드 입력 필드 */
    private EditText edit_pp_code;

    /** 총 박스 개수 표시 필드 */
    private EditText total_box_count;

    /** 총 중량 표시 필드 */
    private EditText total_weight;

    /** 마지막 스캔/입력된 박스의 중량 표시 필드 */
    private EditText weight_of_box;

    /** 패커 코드 입력 필드 */
    private EditText edit_packer_code;

    /** 계근 입력 버튼 (수기 모드에서 사용) */
    private Button btn_input;

    /** 바코드 정보 수신 버튼 */
    private Button btn_input_barcode_info;

    /** 리셋 버튼 */
    private Button btn_reset;

    // ========================================================================================
    // 상태 플래그
    // ========================================================================================
    /**
     * 작업 방식 플래그
     * - 1: 바코드 스캔 모드
     * - 0: 수기 입력 모드
     */
    private int work_flag = 1;

    /**
     * 바코드 정보 수신 완료 여부
     * true이면 바코드 스캔 가능, false이면 먼저 [바코드 정보 수신] 필요
     */
    private boolean communicationOrNot = false;

    // ========================================================================================
    // 바코드 중량 정보 (서버에서 조회)
    // ========================================================================================
    /** 바코드 내 중량 시작 위치 (1-based) */
    private String weightFrom;

    /** 바코드 내 중량 끝 위치 (1-based) */
    private String weightTo;

    /** 소수점 자릿수 (10^zeroPoint로 나눔) */
    private String zeroPoint;

    /** 중량 단위 (KG 또는 LB) */
    private String baseUnit;

    // ========================================================================================
    // Activity 생명주기
    // ========================================================================================
    /**
     * Activity 생성 시 호출
     *
     * UI 컴포넌트 초기화, 리스너 설정, 임시 테이블 초기화를 수행한다.
     * 프린터 스위치는 비활성화하여 라벨 출력을 방지한다.
     *
     * @param savedInstanceState 저장된 상태 (사용하지 않음)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "=====================Common.searchType Check==================" + Common.searchType);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production_calc);

        // 입력 필드 초기화
        edit_pp_code = (EditText) findViewById(R.id.edit_pp_code);
        edit_packer_code = (EditText) findViewById(R.id.edit_packer_code);
        edit_barcode = (EditText) findViewById(R.id.edit_barcode);

        // 결과 표시 필드 초기화
        total_box_count = (EditText) findViewById(R.id.total_box_count);
        total_weight = (EditText) findViewById(R.id.total_weight);
        weight_of_box = (EditText) findViewById(R.id.weight_of_box);

        // 계근 입력 버튼 초기화 및 리스너 연결
        btn_input = (Button) findViewById(R.id.btn_input);
        btn_input.setOnClickListener(inputBtnListener);

        // 바코드 정보 수신 버튼 초기화 및 리스너 연결
        btn_input_barcode_info = (Button) findViewById(R.id.btn_input_get_barcode_info);
        btn_input_barcode_info.setOnClickListener(inputBarcodeBtnListener);

        // 리셋 버튼 초기화 및 리스너 연결
        btn_reset = (Button) findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(resetBtnListener);

        // 결과 필드 초기값 설정
        total_box_count.setText("0");
        total_weight.setText("0");
        weight_of_box.setText("0");

        // 작업 방식 스피너 초기화 (바코드/수기 선택)
        sp_work = (Spinner) findViewById(R.id.sp_work);
        sp_work.setOnItemSelectedListener(workSelectedListener);

        // 프린터 스위치 비활성화 (생산 계근에서는 라벨 출력 안함)
        swt_print.setChecked(false);
        swt_print.setClickable(false);

        // 임시 테이블 초기화 (이전 데이터 삭제)
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

    // ========================================================================================
    // 유틸리티 메서드
    // ========================================================================================

    /**
     * 소프트 키보드 숨김
     */
    private void hideKeyboard() {
        InputMethodManager btn_input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        btn_input.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    // ========================================================================================
    // 버튼 리스너
    // ========================================================================================

    /**
     * 계근 입력 버튼 리스너
     *
     * 수기 입력 모드(work_flag == 0)에서만 동작한다.
     * 바코드 모드에서는 바코드 스캔 시 자동으로 처리된다.
     */
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

    /**
     * 바코드 정보 수신 버튼 리스너
     *
     * PACKER_CODE와 PP_CODE를 기반으로 서버에서 바코드 중량 정보를 조회한다.
     * 조회 성공 시 바코드 스캔이 가능해진다.
     */
    private View.OnClickListener inputBarcodeBtnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.i(TAG, "바코드 정보 수신버튼 클릭");
            if (edit_packer_code.getText().toString().equals("") || edit_pp_code.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "PACKER 혹은 PP코드를 입력 후 다운로드를 바코드정보 수신을 진행하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            getBarcodeInfo();
            hideKeyboard();
        }
    };

    /**
     * 리셋 버튼 리스너
     *
     * 모든 계근 데이터를 초기화한다.
     * 박스 개수, 총 중량, 스캔된 바코드 기록(DB)을 모두 삭제한다.
     */
    private View.OnClickListener resetBtnListener = new View.OnClickListener() {
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

    /**
     * 작업 방식 스피너 리스너
     *
     * 바코드 스캔 모드와 수기 입력 모드를 전환한다.
     * - 0 (바코드): work_flag = 1
     * - 1 (수기): work_flag = 0, 바코드 입력 필드 초기화
     */
    private Spinner.OnItemSelectedListener workSelectedListener = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            try {
                switch (arg2) {
                    case 0:        // 바코드
                        work_flag = 1;
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

    // ========================================================================================
    // 바코드 스캔 처리
    // ========================================================================================

    /**
     * 바코드 스캔 콜백 메서드 (ScannerActivity에서 호출)
     *
     * 바코드 스캐너에서 값이 입력되면 이 메서드가 호출된다.
     * 바코드 정보 수신 완료 여부와 필수 입력값을 검증한 후 처리를 진행한다.
     *
     * @param msg 스캔된 바코드 문자열
     */
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

    /**
     * 바코드 메시지 처리
     *
     * 스캔된 바코드를 화면에 표시하고 중량 계산을 수행한다.
     * 스피너를 바코드 모드로 전환한다.
     *
     * @param msg 스캔된 바코드 문자열
     */
    public void setBarcodeMsg(final String msg) {

        sp_work.setSelection(0);

        try {
            edit_barcode.setText(msg);
            calcAndEnterWeight("scan", msg);
        } catch (Exception ex) {
            Log.e(TAG, "setBarcodeMsg Exception -> " + ex.getMessage().toString());
        }
    }

    // ========================================================================================
    // 서버 통신
    // ========================================================================================

    /**
     * 바코드 중량 정보 조회
     *
     * 서버에서 PACKER_CODE와 PP_CODE에 해당하는 바코드 중량 정보를 조회한다.
     * 조회 성공 시 weightFrom, weightTo, zeroPoint, baseUnit 값이 설정된다.
     *
     * [조회 결과 (:: 구분자)]
     * - weightFrom: 바코드 내 중량 시작 위치
     * - weightTo: 바코드 내 중량 끝 위치
     * - zeroPoint: 소수점 자릿수
     * - baseUnit: 단위 (KG/LB)
     *
     * @see SelectWeightFromTo AsyncTask로 서버 통신
     */
    protected void getBarcodeInfo(){

        boolean successFlag = false;

        // 이미 바코드 정보를 수신한 경우 중복 조회 방지
        if(!communicationOrNot){
            Log.d(TAG, "getting barcode info start");
            try{
                // AsyncTask로 서버에서 바코드 중량 정보 조회
                SelectWeightFromTo task = new SelectWeightFromTo(ProductionActivity.this);
                String[] test = new String[2];
                test[0] = edit_packer_code.getText().toString();
                test[1] = edit_pp_code.getText().toString();
                String temp = task.execute(test).get();  // 동기 방식으로 결과 대기

                if(temp!=null) {
                    // 응답 파싱 (:: 구분자로 분리)
                    String[] tempArray = temp.split("::");

                    // 바코드 중량 위치 정보 저장
                    weightFrom = tempArray[0].toString();  // 중량 시작 위치
                    weightTo = tempArray[1].toString();    // 중량 끝 위치
                    zeroPoint = tempArray[2].toString();   // 소수점 자릿수
                    baseUnit = tempArray[3].toString();    // 단위 (KG/LB)

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
                // 바코드 정보 수신 완료 플래그 설정
                communicationOrNot = true;
                // 입력 필드 비활성화 (중량 정보 고정)
                setUseableEditText(edit_pp_code, false);
                setUseableEditText(edit_packer_code, false);

                Toast.makeText(getApplicationContext(), "바코드 중량 정보 세팅됨, 계근을 시작하세요.", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(getApplicationContext(), "입력하신 PACKER CODE / PPCODE 에 해당하는 바코드 정보가 없습니다. 입력하신 정보를 다시 확인해 주세요.", Toast.LENGTH_SHORT).show();
            }

        }

    }

    // ========================================================================================
    // 핵심 계산 로직
    // ========================================================================================

    /**
     * 중량 계산 및 누적 처리 (핵심 메서드)
     *
     * 바코드 스캔 또는 수기 입력된 중량을 계산하여 총 중량에 누적한다.
     *
     * [처리 흐름]
     * 1. 바코드 중복 체크 (DB 조회)
     * 2. 박스 개수 증가
     * 3. 중량 계산:
     *    - 수기(manual): 입력값 그대로 사용
     *    - 스캔(scan): 바코드에서 중량 추출 → 단위 변환
     * 4. 총 중량 누적
     * 5. UI 업데이트
     *
     * [중량 추출 방식 (스캔 모드)]
     * 바코드: "...XXXX..."
     *             ^    ^
     *        weightFrom weightTo
     *
     * 중량 = XXXX / 10^zeroPoint
     * LB인 경우: 중량 = 중량 * 0.453592
     *
     * @param mode 입력 모드 ("manual": 수기, "scan": 바코드 스캔)
     * @param barcode 스캔된 바코드 문자열 (수기 모드에서는 빈 문자열)
     */
    protected void calcAndEnterWeight(String mode, String barcode){

        Log.d(TAG, "calcAndEnterWeight start");

        // 바코드 중복 체크 (DB에서 해당 바코드 존재 여부 조회)
        String barcodeCount = DBHandler.selectGoodsWetProductionCalc(getApplicationContext(), barcode);

        if(barcodeCount.equals("0")){
            // 신규 바코드인 경우 DB에 저장
            DBHandler.insertGoodsWetProductionCalc(getApplicationContext(), barcode);
        }else{
            // 중복 바코드인 경우 (단, 패커코드 31341은 중복 허용)
            if(!edit_packer_code.getText().toString().equals("31341")){
                Toast.makeText(getApplicationContext(), "바코드를 중복으로 스캔하셨습니다. 다른 바코드를 스캔해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 현재 박스 개수 및 총 중량 조회
        Integer startBoxCnt = Integer.parseInt(total_box_count.getText().toString());
        Float totalWeight = Float.parseFloat(total_weight.getText().toString());

        String totalWeightFormated = "";
        String item_weight_str = "";

        // 박스 개수 1 증가
        startBoxCnt = startBoxCnt + 1;
        total_box_count.setText(startBoxCnt.toString());

        // [수기 입력 모드] 입력값을 그대로 중량으로 사용
        if(mode.equals("manual")) {

            totalWeight = totalWeight + Float.parseFloat(edit_barcode.getText().toString());
            totalWeightFormated = String.format("%.2f", totalWeight);

        // [바코드 스캔 모드] 바코드에서 중량 추출
        }else if(mode.equals("scan")){

            Log.d(TAG, "weightfrom,to:" + weightFrom+":" + weightTo+ ":");

            String item_weight = "";            // 바코드에서 추출한 중량 문자열
            Double item_weight_double = 0.0;    // 계산된 중량 (Double)
            String work_item_fullbarcode = barcode;
            double item_pow = 0;                // 소수점 변환용 (10^zeroPoint)

            // 서버에서 받아온 값 trim 처리 (공백 제거)
            String weightFromVar = weightFrom.trim();
            String weightToVar = weightTo.trim();
            String zeroPointVar = zeroPoint.trim();
            String baseUnitVar = baseUnit.trim();

            Log.d(TAG, "work_item_fullbarcode " + work_item_fullbarcode);
            Log.d(TAG, "weightFrom TEST " + weightFromVar);
            Log.d(TAG, "weightTo TEST " + weightToVar);

            // 바코드에서 중량 부분 추출 (weightFrom ~ weightTo)
            item_weight = work_item_fullbarcode.substring(
                    Integer.parseInt(weightFromVar) - 1, Integer.parseInt(weightToVar)
            );

            Log.i(TAG, "Type W | 절사한 중량값 : " + item_weight);

            // 소수점 적용 (예: zeroPoint=2이면 100으로 나눔)
            item_pow = Math.pow(10, Integer.parseInt(zeroPointVar));
            item_weight_double = Double.parseDouble(item_weight) / item_pow;

            // LB -> KG 단위 변환 (1 LB = 0.453592 KG)
            if ("LB".equals(baseUnitVar)) {
                double temp_weight_double = item_weight_double * 0.453592;

                Log.i(TAG, "temp_weight_double 계산 전 : " + temp_weight_double);
                Log.i(TAG, "math.floor: " + Math.floor(temp_weight_double * item_pow));

                // 소수점 둘째자리까지 버림 처리
                item_weight_double = Math.floor(temp_weight_double * 100) / 100;
                item_weight_str = String.valueOf(item_weight_double);

                Log.i(TAG, "LB->KG | 환산 중량 item_pow : " + item_pow);
                Log.i(TAG, "LB->KG | 환산 중량 Double값 : " + item_weight_double);
                Log.i(TAG, "LB->KG | 환산 중량 String값 : " + item_weight_str);
            }

            // 최종 중량 소수점 둘째자리 포맷팅
            String temp_weight = String.format("%.2f", item_weight_double);

            item_weight_double = Double.parseDouble(temp_weight);
            item_weight_str = String.valueOf(item_weight_double);

            Log.i(TAG, "Type W | ZeroPoint 적용 중량 Double값 : " + item_weight_double);
            Log.i(TAG, "Type W | ZeroPoint 적용 중량 String값 : " + item_weight_str);

            // 총 중량에 누적
            totalWeight = totalWeight + Float.parseFloat(item_weight_str);
            totalWeightFormated = String.format("%.2f", totalWeight);

        }

        // UI 업데이트: 총 중량 표시
        total_weight.setText(totalWeightFormated);

        // UI 업데이트: 현재 박스 중량 표시 및 토스트 메시지
        if(mode.equals("manual")){
            weight_of_box.setText(edit_barcode.getText().toString());
            Toast.makeText(getApplicationContext(), "중량 정보 "+edit_barcode.getText().toString()+" kg 입력됨", Toast.LENGTH_SHORT).show();
        }else{
            weight_of_box.setText(item_weight_str);
            Toast.makeText(getApplicationContext(), "중량 정보 "+item_weight_str+" kg 입력됨", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * EditText 활성화/비활성화 설정
     *
     * 바코드 정보 수신 후 PACKER_CODE와 PP_CODE 입력 필드를
     * 비활성화하여 수정을 방지한다.
     *
     * @param et 대상 EditText
     * @param useable true: 활성화, false: 비활성화
     */
    private void setUseableEditText(EditText et, boolean useable) {
        et.setClickable(useable);
        et.setEnabled(useable);
        et.setFocusable(useable);
        et.setFocusableInTouchMode(useable);
    }

    /**
     * Activity 종료 시 호출
     *
     * 임시 테이블(GOODS_WET_PRODUCTION_CALC)의 데이터를 삭제하여
     * 다음 사용 시 깨끗한 상태로 시작할 수 있도록 한다.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " onDestroy, table data delete");
        DBHandler.deleteGoodsWetProductionCalc(getApplicationContext());
    }

    // ========================================================================================
    // AsyncTask
    // ========================================================================================

    /**
     * 바코드 중량 정보 조회 AsyncTask
     *
     * 서버에서 PACKER_CODE와 PP_CODE에 해당하는 바코드 중량 위치 정보를 조회한다.
     *
     * [요청 파라미터]
     * - values[0]: PACKER_CODE
     * - values[1]: PACKER_PRODUCT_CODE (PP_CODE)
     *
     * [응답 형식 (:: 구분자)]
     * weightFrom::weightTo::zeroPoint::baseUnit
     *
     * @see Common#URL_WET_PRODUCTION_CALC 서버 URL
     */
    class SelectWeightFromTo extends AsyncTask<String, String, String> {

        private Context mContext;

        public SelectWeightFromTo(Context context) {
            mContext = context;
        }

        // 백그라운드 작업 시작 전 로딩 다이얼로그 표시
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

        // 백그라운드에서 서버 통신 수행
        @Override
        protected String doInBackground(String ... values) {

            String result = "";

            try {
                // 파라미터에서 패커코드, PP코드 추출
                String packerCode = values[0];
                String packerProductCode = values[1];

                Log.d(TAG, "============ packerCode ============== : " + packerCode);
                Log.d(TAG, "============ packerProductCode ========: " + packerProductCode);

                // WHERE 조건절 생성
                String packet = "AND a.PACKER_CODE = '"+packerCode+"'"+ " AND a.PACKER_PRODUCT_CODE = '"+packerProductCode+"'";
                // 서버에 요청하여 바코드 중량 정보 조회
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

        // 백그라운드 작업 완료 후 로딩 다이얼로그 닫기
        @Override
        protected void onPostExecute(String _result) {
            Log.v(TAG, "전송결과 : " + _result);
            pDialog.dismiss();
            super.onPostExecute(_result);
        }
    }
}

