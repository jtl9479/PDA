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
import com.rgbsolution.highland_emart.print.BluetoothPrintService;
import com.rgbsolution.highland_emart.print.BixolonSocketPrinter;
import com.rgbsolution.highland_emart.print.DeviceListActivity;

import java.security.MessageDigest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import com.sgis.labelengine.*;

import java.security.MessageDigest;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";                        // Log's TAG
    private ProgressDialog pDialog = null;

    // UI references.
    private EditText editID;                                        // ID 입력창
    private EditText editPWD;                                        // PWD 입력창
    private String user_id = "";
    private String user_pwd = "";

    // 블루투스 프린터 관련
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothPrintService mPrintService = null;

    // 빅솔론 프린터 (소켓 통신 방식)
    private BixolonSocketPrinter mBixolonPrinter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // SQLite Init
        DBHandler.createqueryShipment(getApplicationContext());                // 출하대상 		Table Create
        DBHandler.createqueryBarcodeInfo(getApplicationContext());            // 바코드정보 		Table Create
        DBHandler.createqueryGoodsWet(getApplicationContext());                // 계근내역		Table Create
        DBHandler.createqueryGoodsWetProductionCalc(getApplicationContext());

        // 모바일프린터의 정보를 저장할 SharedPreferences
        SharedPreferences spfBluetooth = getSharedPreferences("spfBluetooth", MODE_PRIVATE);

        // 모바일프린터 정보 저장값 read
        Common.printer_setting = spfBluetooth.getBoolean("printer_setting", true);
        Common.printer_address = spfBluetooth.getString("printer_address", "");

        if (!Common.printer_setting) {
            Common.print_bool = false;
        } else {
            Common.print_bool = true;
        }

        editID = (EditText) findViewById(R.id.editID);
        editPWD = (EditText) findViewById(R.id.editPWD);

        // 기본값 설정
        editID.setText("12345678");
        editPWD.setText("5411");

        // 창고 목록 서버 조회
        new WarehouseSearchTask().execute();

    }

    // 창고 목록 조회 AsyncTask
    class WarehouseSearchTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                String data = " WHERE 회사코드 = '" + Common.selectCompanyCode + "'";
                return HttpHelper.getInstance().sendDataDb(data, "inno",
                    "search_warehouse", Common.URL_SEARCH_WAREHOUSE);
            } catch (Exception e) {
                Log.e(TAG, "창고 목록 조회 실패: " + e.getMessage());
                return "";
            }
        }

        @Override
        protected void onPostExecute(String receiveData) {
            String normalized = (receiveData == null) ? "" : receiveData.replace("\r\n", "").replace("\n", "");

            Common.warehouseNames.clear();
            Common.warehouseCodes.clear();

            if (!normalized.isEmpty()) {
                String[] rows = normalized.split(";;");
                for (String row : rows) {
                    if (row.isEmpty()) continue;
                    String[] cols = row.split("::", -1);
                    if (cols.length >= 3) {
                        Common.warehouseCodes.add(cols[1].trim());  // 창고코드
                        Common.warehouseNames.add(cols[2].trim());  // 창고명
                    }
                }
            }

            // 조회 0건: 원인(연결 실패 vs 실제 0건)에 따라 안내 문구 분리, Spinner는 빈 상태 유지
            if (Common.warehouseNames.isEmpty()) {
                if (normalized.trim().isEmpty()) {
                    // 정상 응답이나 실제 창고 데이터 0건 — 기존 문구 유지
                    new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("창고 목록 없음")
                        .setMessage("등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요.")
                        .setPositiveButton("확인", null)
                        .show();
                } else {
                    // 응답은 왔으나 비어있지 않음 = 통신/서버/DB 연결 오류 메시지가 담겨 있음
                    Log.e(TAG, "창고 목록 조회 오류 응답: " + normalized);
                    new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("창고 목록 조회 실패")
                        .setMessage("서버에 연결할 수 없습니다.\n서버 상태와 네트워크를 확인 후 다시 시도하세요.")
                        .setPositiveButton("확인", null)
                        .show();
                }
                return;  // Spinner 구성 생략 — selectWarehouse/Code는 "" 상태 유지
            }

            // Spinner 구성
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                LoginActivity.this,
                android.R.layout.simple_spinner_item,
                Common.warehouseNames
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            Spinner spin = (Spinner) findViewById(R.id.storeSpinner);
            spin.setAdapter(adapter);

            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView parent, View v, int position, long id) {
                    Common.selectWarehouse = Common.warehouseNames.get(position);
                    Common.selectWarehouseCode = Common.warehouseCodes.get(position);
                    Log.i(TAG, "창고 선택: " + Common.selectWarehouse
                        + " (" + Common.selectWarehouseCode + ")");
                }
                public void onNothingSelected(AdapterView parent) {}
            });
        }
    }

    //	Button 클릭 이벤트
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:

                // 창고 목록 비어있음 검증 (0건 조회 시 로그인 차단)
                if (Common.warehouseNames.isEmpty()) {
                    Toast.makeText(LoginActivity.this,
                        "등록된 PDA 사용 창고가 없습니다.\nERP 창고관리(C0114)에서 PDA여부를 설정하세요.",
                        Toast.LENGTH_LONG).show();
                    break;
                }
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

                // SHA-1 해시 암호화
                MessageDigest digest = MessageDigest.getInstance("SHA1");
                String password = user_pwd.toString();
                byte[] encodedHash = digest.digest(password.getBytes("UTF-8"));
                StringBuilder sb = new StringBuilder();

                for (byte b : encodedHash) {
                    sb.append(String.format("%02x", b));
                }

                String encPassword = sb.toString();

                if (Common.D) {
                    Log.d(TAG, "Common.URL_LOGIN : " + Common.URL_LOGIN);
                    Log.d(TAG, "SHA1 : " + encPassword);
                    Log.d(TAG, "::: ID = '" + Common.REG_ID + "', PWD = '" + password + "' :::");
                }

                receiveData = HttpHelper.getInstance().loginData(Common.REG_ID, encPassword, Common.selectCompanyCode, Common.URL_LOGIN);
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

    //빅솔론 프린트 출력 테스트 (소켓 통신 방식)
    private void printTest() {
        try {
            // 블루투스 어댑터 초기화
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 블루투스 활성화 확인
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "블루투스를 켜주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 빅솔론 프린터 초기화 (소켓 통신 방식)
            if (mBixolonPrinter == null) {
                mBixolonPrinter = new BixolonSocketPrinter(this, mBixolonHandler);
            }

            // 프린터 선택 다이얼로그 표시
            showPrinterSelectDialog();

        } catch (Exception e) {
            Log.e(TAG, "printTest Error: " + e.getMessage());
            Toast.makeText(this, "오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 프린터 선택 다이얼로그 표시
    private void showPrinterSelectDialog() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            final BluetoothDevice[] deviceArray = pairedDevices.toArray(new BluetoothDevice[0]);
            String[] deviceNames = new String[deviceArray.length];

            for (int i = 0; i < deviceArray.length; i++) {
                deviceNames[i] = deviceArray[i].getName() + "\n" + deviceArray[i].getAddress();
            }

            new AlertDialog.Builder(this)
                .setTitle("프린터 선택")
                .setItems(deviceNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BluetoothDevice selectedDevice = deviceArray[which];
                        String address = selectedDevice.getAddress();
                        Log.d(TAG, "선택된 프린터: " + selectedDevice.getName() + " - " + address);

                        // 선택한 프린터 주소 저장
                        SharedPreferences spfBixolon = getSharedPreferences("spfBixolon", MODE_PRIVATE);
                        SharedPreferences.Editor editor = spfBixolon.edit();
                        editor.putString("bixolon_address", address);
                        editor.apply();

                        mBixolonPrinter.connect(selectedDevice);
                        Toast.makeText(LoginActivity.this, "프린터 연결 중...", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
        } else {
            Toast.makeText(this, "페어링된 블루투스 장치가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 블루투스 통신을 위한 Handler (기존 호환용)
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: // MESSAGE_DEVICE_NAME
                    break;
                case 2: // MESSAGE_TOAST
                    break;
                case 3: // MESSAGE_READ
                    break;
            }
        }
    };

    // 빅솔론 프린터 Handler (소켓 통신 방식)
    private final Handler mBixolonHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BixolonSocketPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonSocketPrinter.STATE_CONNECTED:
                            Log.d(TAG, "빅솔론 프린터 연결됨");
                            Toast.makeText(LoginActivity.this, "프린터 연결 성공!", Toast.LENGTH_SHORT).show();
                            // 연결 성공 후 테스트 출력
                            sendBixolonTestPrint();
                            break;
                        case BixolonSocketPrinter.STATE_CONNECTING:
                            Log.d(TAG, "빅솔론 프린터 연결 중...");
                            break;
                        case BixolonSocketPrinter.STATE_NONE:
                            Log.d(TAG, "빅솔론 프린터 연결 해제됨");
                            break;
                    }
                    break;
                case BixolonSocketPrinter.MESSAGE_PRINT_COMPLETE:
                    Log.d(TAG, "인쇄 완료");
                    Toast.makeText(LoginActivity.this, "인쇄 완료!", Toast.LENGTH_SHORT).show();
                    break;
                case BixolonSocketPrinter.MESSAGE_TOAST:
                    String toastMsg = (String) msg.obj;
                    Toast.makeText(LoginActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // 빅솔론 프린터로 테스트 출력 (소켓 통신 방식)
    private void sendBixolonTestPrint() {
        try {
            if (mBixolonPrinter == null) {
                Toast.makeText(this, "프린터가 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!mBixolonPrinter.isConnected()) {
                Toast.makeText(this, "프린터가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 소켓 통신으로 테스트 라벨 출력
            //mBixolonPrinter.printTestLabel();
            mBixolonPrinter.printTestLabel();

            Log.d(TAG, "테스트 출력 명령 전송 완료");

        } catch (Exception e) {
            Log.e(TAG, "sendBixolonTestPrint Error: " + e.getMessage());
            Toast.makeText(this, "출력 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
