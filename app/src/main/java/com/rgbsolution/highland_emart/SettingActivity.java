package com.rgbsolution.highland_emart;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.print.BixolonSocketPrinter;

import java.util.Set;

/**
 * SettingActivity - 설정 화면
 *
 * 프린터 사용 여부를 설정하는 화면입니다.
 *
 * ■ 주요 기능:
 *   1. 프린터 ON/OFF 설정
 *   2. 설정값 SharedPreferences 저장
 *   3. 숨김 기능: 이미지 7회 클릭 시 전체 계근내역 삭제
 *   4. 테스트 출력 (개발42 추가) — 빅솔론 프린터 연결 + 테스트 라벨 1장 출력
 *
 * ■ SharedPreferences 키:
 *   - "printer_setting": 프린터 사용 여부 (boolean)
 *   - "printer_address": 프린터 주소 (String)
 *
 * @see Common#printer_setting 프린터 설정 전역 변수
 * @see Common#printer_address 프린터 주소 전역 변수
 */
public class SettingActivity extends AppCompatActivity {

    private final String TAG = "SettingActivity";

    /** 프린터 ON 버튼 */
    private Button btn_on;

    /** 프린터 OFF 버튼 */
    private Button btn_off;

    /** 테스트 출력 버튼 */
    private Button btn_test_print;

    /** 이미지 클릭 카운트 (숨김 기능용, 7회 이상 클릭 시 계근내역 삭제 다이얼로그 표시) */
    private int clkCount = 0;

    /** 프린터 설정 저장용 SharedPreferences */
    SharedPreferences spfBluetooth;

    /** SharedPreferences Editor */
    SharedPreferences.Editor editor;

    /** 블루투스 어댑터 */
    private BluetoothAdapter mBluetoothAdapter = null;

    /** 빅솔론 소켓 프린터 (테스트 출력 전용) */
    private BixolonSocketPrinter mBixolonPrinter = null;

    /** 연결 진행 다이얼로그 */
    private ProgressDialog cDialog = null;

    /**
     * 연결 시도 중 플래그 (개발42 보완)
     * - connect()는 disconnect() → setState(STATE_NONE) → setState(STATE_CONNECTING) 순으로 호출됨
     * - disconnect 단계의 STATE_NONE이 핸들러로 전달되어 "연결 실패" Toast가 잘못 발생하는 문제 방지
     * - STATE_CONNECTING 수신 후의 STATE_NONE만 진짜 실패로 인식
     */
    private boolean wasConnecting = false;

    /**
     * 액티비티 생성 시 호출
     * - UI 초기화
     * - SharedPreferences 초기화
     * - 현재 프린터 설정에 따라 버튼 상태 표시
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // UI 초기화
        btn_on = (Button) findViewById(R.id.btn_on);
        btn_off = (Button) findViewById(R.id.btn_off);
        btn_test_print = (Button) findViewById(R.id.btn_test_print);

        // 블루투스 어댑터 초기화
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // SharedPreferences 초기화
        spfBluetooth = getSharedPreferences("spfBluetooth", Activity.MODE_PRIVATE);
        editor = spfBluetooth.edit();

        // 액션바 뒤로가기 버튼 활성화
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // 현재 프린터 설정에 따라 버튼 배경색 설정
        if (Common.printer_setting) {
            btn_on.setBackgroundResource(R.drawable.setting_on_green);
            btn_off.setBackgroundResource(R.drawable.setting_off_gray);
        } else {
            btn_on.setBackgroundResource(R.drawable.setting_on_gray);
            btn_off.setBackgroundResource(R.drawable.setting_off_green);
        }
    }

    /**
     * 버튼 클릭 이벤트 처리
     *
     * @param v 클릭된 View
     *
     * ■ 처리 버튼:
     *   - btn_on: 프린터 사용 ON
     *   - btn_off: 프린터 사용 OFF
     *   - imageView: 숨김 기능 (7회 클릭 시 전체 계근내역 삭제)
     *   - btn_test_print: 테스트 출력 (개발42)
     */
    public void onClick(View v) {
        switch (v.getId()) {
            // 프린터 ON 버튼
            case R.id.btn_on:
                Log.d(TAG, "==== ON Click ====");
                Common.printer_setting = true;
                btn_on.setBackgroundResource(R.drawable.setting_on_green);
                btn_off.setBackgroundResource(R.drawable.setting_off_gray);
                break;

            // 프린터 OFF 버튼
            case R.id.btn_off:
                Log.d(TAG, "==== OFF Click ====");
                Common.printer_setting = false;
                btn_on.setBackgroundResource(R.drawable.setting_on_gray);
                btn_off.setBackgroundResource(R.drawable.setting_off_green);
                break;

            // 숨김 기능: 이미지 7회 클릭 시 전체 계근내역 삭제 다이얼로그 표시
            case R.id.imageView:
                clkCount = clkCount + 1;

                if(clkCount > 6){
                    String alertTitle = getResources().getString(R.string.app_name);
                    String buttonNo = getString(R.string.exit_no);

                    new AlertDialog.Builder(SettingActivity.this, R.style.AppCompatDialogStyle)
                            .setIcon(R.drawable.highland)
                            .setTitle(alertTitle)
                            .setMessage("전체계근내역을 삭제 하시겠습니까?")
                            .setCancelable(false)
                            .setPositiveButton("삭제",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DBHandler.deletequeryAllGoodsWet(getApplicationContext());
                                            Toast.makeText(getApplicationContext(), "전체계근내역이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    }).setNegativeButton(buttonNo, null).show();
                    clkCount = 0;
                }
                break;

            // 테스트 출력 버튼 (개발42)
            case R.id.btn_test_print:
                Log.d(TAG, "==== 테스트 출력 Click ====");

                // 블루투스 어댑터 미지원 체크
                if (mBluetoothAdapter == null) {
                    Toast.makeText(this, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
                    break;
                }

                // 블루투스 활성화 체크
                if (!mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "블루투스를 켜주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }

                // BixolonSocketPrinter 초기화
                if (mBixolonPrinter == null) {
                    mBixolonPrinter = new BixolonSocketPrinter(SettingActivity.this, mBixolonHandler);
                }

                // 저장된 프린터 주소 확인
                if (Common.printer_address == null || Common.printer_address.equals("")) {
                    // 저장 주소 없음 → 페어링 장치 목록 다이얼로그
                    showPrinterSelectDialog();
                } else {
                    // 저장 주소 있음 → 바로 연결 시도
                    new ProgressDlgPrintConnect(SettingActivity.this).execute();
                }
                break;
        }
    }

    /**
     * 페어링된 블루투스 장치 목록을 다이얼로그로 표시하여 프린터 선택
     * - 선택된 장치의 MAC 주소를 Common.printer_address 및 SharedPreferences에 저장
     * - 저장 후 ProgressDlgPrintConnect 실행하여 연결 시도
     */
    private void showPrinterSelectDialog() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices == null || pairedDevices.size() == 0) {
            Toast.makeText(this, "페어링된 블루투스 장치가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        final BluetoothDevice[] deviceArray = pairedDevices.toArray(new BluetoothDevice[0]);
        String[] deviceNames = new String[deviceArray.length];

        for (int i = 0; i < deviceArray.length; i++) {
            deviceNames[i] = deviceArray[i].getName() + "\n" + deviceArray[i].getAddress();
        }

        new AlertDialog.Builder(SettingActivity.this, R.style.AppCompatDialogStyle)
                .setTitle("프린터 선택")
                .setItems(deviceNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BluetoothDevice selectedDevice = deviceArray[which];
                        String address = selectedDevice.getAddress();
                        Log.d(TAG, "선택된 프린터: " + selectedDevice.getName() + " - " + address);

                        // 선택한 프린터 주소 저장
                        Common.printer_address = address;
                        editor.putString("printer_address", Common.printer_address);
                        editor.commit();

                        // 연결 시도
                        new ProgressDlgPrintConnect(SettingActivity.this).execute();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    /**
     * 블루투스 프린터 연결 AsyncTask
     * - Common.printer_address에 저장된 주소로 BixolonSocketPrinter 연결 시도
     * - 연결 결과는 mBixolonHandler가 수신 (STATE_CONNECTED → printTestLabel 호출)
     */
    class ProgressDlgPrintConnect extends AsyncTask<Integer, String, Integer> {
        private android.content.Context mContext;

        public ProgressDlgPrintConnect(android.content.Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            cDialog = new ProgressDialog(mContext);
            cDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            cDialog.setTitle("프린터 연결중...");
            cDialog.setMessage("잠시만 기다려 주세요..");
            cDialog.setCancelable(false);
            cDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(Common.printer_address);
                Log.d(TAG, "연결 시도 주소: " + Common.printer_address);
                mBixolonPrinter.connect(device);
            } catch (Exception e) {
                Log.e(TAG, "ProgressDlgPrintConnect error: " + e.toString());
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
        }
    }

    /**
     * 빅솔론 프린터 상태 변경 Handler
     * - STATE_CONNECTED: 연결 성공 → printTestLabel() 호출
     * - STATE_NONE: 연결 실패 → 다이얼로그 닫기 + Toast
     */
    private final Handler mBixolonHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BixolonSocketPrinter.MESSAGE_STATE_CHANGE:
                    int newState = msg.arg1;
                    Log.d(TAG, "Bixolon state -> " + newState);

                    switch (newState) {
                        case BixolonSocketPrinter.STATE_CONNECTING:
                            // 연결 시도 시작 — 진짜 실패 판정 플래그 ON
                            wasConnecting = true;
                            break;

                        case BixolonSocketPrinter.STATE_CONNECTED:
                            // 연결 성공 → 다이얼로그 닫기 + 테스트 라벨 출력
                            wasConnecting = false;
                            if (cDialog != null && cDialog.isShowing()) {
                                cDialog.dismiss();
                            }
                            Toast.makeText(getApplicationContext(),
                                    "프린터 연결 성공. 테스트 출력 중...", Toast.LENGTH_SHORT).show();
                            if (mBixolonPrinter != null) {
                                mBixolonPrinter.printTestLabel();
                            }
                            break;

                        case BixolonSocketPrinter.STATE_NONE:
                            // STATE_CONNECTING 이후 발생한 STATE_NONE만 진짜 실패로 처리
                            // (connect() 내부 disconnect() 호출 시 발생하는 가짜 STATE_NONE 무시)
                            if (wasConnecting) {
                                wasConnecting = false;
                                if (cDialog != null && cDialog.isShowing()) {
                                    cDialog.dismiss();
                                }
                                Toast.makeText(getApplicationContext(),
                                        "프린터 연결 실패", Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                    break;

                case BixolonSocketPrinter.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;

                case BixolonSocketPrinter.MESSAGE_PRINT_COMPLETE:
                    Log.d(TAG, "테스트 출력 완료");
                    break;
            }
        }
    };

    /**
     * 액션바 메뉴 아이템 선택 처리
     * - 뒤로가기 버튼 클릭 시 액티비티 종료
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
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

    /**
     * 액티비티 종료 시 호출
     * - 테스트 출력용 프린터 연결 해제 (개발42)
     * - 프린터 설정값을 SharedPreferences에 저장
     * - OFF인 경우 프린터 주소도 초기화
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " onDestroy");

        // 테스트 출력용 프린터 연결 해제 (개발42)
        if (mBixolonPrinter != null) {
            mBixolonPrinter.disconnect();
            mBixolonPrinter = null;
        }

        // 프린터 설정값 저장
        if (Common.printer_setting) {
            // ON: 프린터 사용
            editor.putBoolean("printer_setting", true);
            editor.commit();
        } else {
            // OFF: 프린터 미사용, 주소 및 Bixolon 초기화 플래그도 초기화
            editor.putBoolean("printer_setting", false);
            editor.putString("printer_address", "");
            editor.putBoolean("bixolon_initialized", false);  // Bixolon 초기화 플래그 리셋
            editor.commit();
            Common.printer_address = "";
        }
    }
}
