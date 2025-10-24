package com.rgbsolution.highland_emart;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.common.HttpHelper;
import com.rgbsolution.highland_emart.db.DBHandler;

import org.kobjects.base64.Base64;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";                        // Log's TAG

    private ProgressDialog pDialog = null;

    // UI references.
    private EditText editID;                                        // ID 입력창
    private EditText editPWD;                                        // PWD 입력창

    private String user_id = "";
    private String user_pwd = "";

    static final String[] store = {"부산센터","이천1센터","삼일냉장","SWC","탑로지스"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // SQLite Init
        DBHandler.createqueryShipment(getApplicationContext());                // 출하대상 		Table Create
        DBHandler.createqueryBarcodeInfo(getApplicationContext());            // 바코드정보 		Table Create
        DBHandler.createqueryGoodsWet(getApplicationContext());                // 계근내역		Table Create
        DBHandler.createqueryGoodsWetProductionCalc(getApplicationContext());

        Common.URL_VERSION = getString(R.string.URL_VERSION);
        Common.URL_LOGIN = getString(R.string.URL_LOGIN);
        Common.URL_SEARCH_SHIPMENT = getString(R.string.URL_SEARCH_SHIPMENT);
        Common.URL_SEARCH_SHIPMENT_HOMEPLUS = getString(R.string.URL_SEARCH_SHIPMENT_HOMEPLUS);
        Common.URL_SEARCH_SHIPMENT_LOTTE = getString(R.string.URL_SEARCH_SHIPMENT_LOTTE);
        Common.URL_SEARCH_SHIPMENT_WHOLESALE = getString(R.string.URL_SEARCH_SHIPMENT_WHOLESALE);
        Common.URL_SEARCH_PRODUCTION = getString(R.string.URL_SEARCH_PRODUCTION);
        Common.URL_SEARCH_PRODUCTION_4LABEL = getString(R.string.URL_SEARCH_PRODUCTION_4LABEL);
        Common.URL_SEARCH_BARCODE_INFO = getString(R.string.URL_SEARCH_BARCODE_INFO);
        Common.URL_SEARCH_GOODS_WET = getString(R.string.URL_SEARCH_GOODS_WET);
        Common.URL_INSERT_GOODS_WET = getString(R.string.URL_INSERT_GOODS_WET);
        Common.URL_INSERT_GOODS_WET_NEW = getString(R.string.URL_INSERT_GOODS_WET_NEW);
        Common.URL_INSERT_GOODS_WET_HOMEPLUS = getString(R.string.URL_INSERT_GOODS_WET_HOMEPLUS);
        Common.URL_INSERT_BARCODE_INFO = getString(R.string.URL_INSERT_BARCODE_INFO);
        Common.URL_UPDATE_BARCODE_INFO = getString(R.string.URL_UPDATE_BARCODE_INFO);
        Common.URL_UPDATE_SHIPMENT = getString(R.string.URL_UPDATE_SHIPMENT);
        Common.URL_WET_PRODUCTION_CALC = getString(R.string.URL_WET_PRODUCTION_CALC);
        Common.URL_SEARCH_PRODUCTION_NONFIXED = getString(R.string.URL_SEARCH_PRODUCTION_NONFIXED);
        Common.URL_SEARCH_BARCODE_INFO_NONFIXED = getString(R.string.URL_SEARCH_BARCODE_INFO_NONFIXED);
        Common.URL_SEARCH_HOMEPLUS_NONFIXED = getString(R.string.URL_SEARCH_HOMEPLUS_NONFIXED);
        Common.URL_SEARCH_HOMEPLUS_NONFIXED2 = getString(R.string.URL_SEARCH_HOMEPLUS_NONFIXED2);

        // 모바일프린터의 정보를 저장할 SharedPreferences
        SharedPreferences spfBluetooth = getSharedPreferences("spfBluetooth", MODE_PRIVATE);

        // 모바일프린터 정보 저장값 read
        Common.printer_setting = spfBluetooth.getBoolean("printer_setting", true);
        Common.printer_address = spfBluetooth.getString("printer_address", "");

        if (!Common.printer_setting)
            Common.print_bool = false;
        else
            Common.print_bool = true;

        editID = (EditText) findViewById(R.id.editID);
        editPWD = (EditText) findViewById(R.id.editPWD);

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,store);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spin = (Spinner) findViewById(R.id.storeSpinner);
        spin.setAdapter(adapter);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            TextView storetv = (TextView) findViewById(R.id.TextView01);

            public void onItemSelected(AdapterView parent, View v, int position, long id){
                storetv.setText(store[position]);
                Common.selectWarehouse = storetv.getText().toString();
                Log.i(TAG, TAG + "=====================Common.selectWarehouse======================" + Common.selectWarehouse);
            }
            public void onNothingSelected(AdapterView parent){
                storetv.setText("");
            }
        });

    }

    //	Button 클릭 이벤트
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:

                if (("").equals(editID.getText().toString())) {
                    editID.setError("아이디를 입력하세요.");
                    break;
                }
                if (("").equals(editPWD.getText().toString())) {
                    editPWD.setError("비밀번호를 입력하세요.");
                    break;
                }

                user_id = editID.getText().toString();
                user_pwd = editPWD.getText().toString();

                new ProgressDlgLogin(LoginActivity.this).execute();
                break;
            case R.id.btnClose:
                exitDialog();
                break;
        }
    }

    @Override
    protected void onResume() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " onDestroy");
    }

    // 입력받은 ID, PASSWORD정보를 통해 로그인하기위한 AsyncTask
    class ProgressDlgLogin extends AsyncTask<Integer, String, Integer> {
        private Context mContext;
        String receiveData = "";                        // 조회결과를 Received

        public ProgressDlgLogin(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(mContext);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setTitle("로그인중 입니다.");
            pDialog.setMessage("잠시만 기다려 주세요..");
            pDialog.setCancelable(false);
            pDialog.show();

            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                Common.REG_ID = user_id.toString();
                String pwdBase = Base64.encode(user_pwd.toString().getBytes());
                MessageDigest sh = MessageDigest.getInstance("SHA-256");
                sh.update(pwdBase.getBytes());
                byte byteData[] = sh.digest();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < byteData.length; i++) {
                    sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
                }
                if (Common.D) {
                    Log.d(TAG, "SHA256 : " + sb.toString());
                    Log.d(TAG, "::: ID = '" + Common.REG_ID.toString() + "', PWD = '" + user_pwd.toString() + "' AND '" + pwdBase + "' :::");
                }
                receiveData = HttpHelper.getInstance().loginData(Common.REG_ID, pwdBase, "login", Common.URL_LOGIN);
            } catch (Exception e) {
                if (Common.D) {
                    e.printStackTrace();
                    Log.e(TAG, "e : " + e.toString());
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // 결과값의 앞, 뒤에 공백 제거
            receiveData = receiveData.replace("\r\n", "");
            receiveData = receiveData.replace("\n", "");
            if (Common.D) {
                Log.d(TAG, "receiveData : '" + receiveData + "'");
            }
            pDialog.dismiss();

            if (receiveData.toString().equals("fail")) {
                editID.setError("아이디와 비밀번호를 확인해주세요.");
                Toast.makeText(mContext, "로그인 실패", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "로그인 성공", Toast.LENGTH_SHORT).show();
                Common.USER_TYPE = receiveData;
                // 테스트 시 제외
                //new ProgressDlgShipSearch(LoginActivity.this).execute();		// 로그인 성공 시 출하대상 조회
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitDialog();
        }

        return super.onKeyDown(keyCode, event);
    }

    //	프로그램 종료 Dialog
    public void exitDialog() {
        String alertTitle = getResources().getString(R.string.app_name);
        String buttonMessage = getString(R.string.exit_message);
        String buttonYes = getString(R.string.exit_yes);
        String buttonNo = getString(R.string.exit_no);

        new AlertDialog.Builder(LoginActivity.this, R.style.AppCompatDialogStyle)
                .setIcon(R.drawable.highland)
                .setTitle(alertTitle)
                .setMessage(buttonMessage)
                .setCancelable(false)
                .setPositiveButton(buttonYes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setNegativeButton(buttonNo, null).show();
    }
}
