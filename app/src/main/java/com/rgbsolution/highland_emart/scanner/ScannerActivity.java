package com.rgbsolution.highland_emart.scanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.rgbsolution.highland_emart.R;
import com.rgbsolution.highland_emart.common.Common;

import device.common.DecodeResult;
import device.common.ScanConst;
import device.sdk.ScanManager;

public class ScannerActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "ScannerActivity";
    private static final String RECEIVE_PM80 = "ACTION_RECEIVE_PM80";
    public static ScanManager mScanner = null;
    private static DecodeResult mDecodeResult = null;

    protected Button btn_init;
    protected SwitchCompat swt_print;

    //	스캐너의 스캔 결과를 받아오는 BroadCastReceiver
    public static class ScanResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Common.D) {
                Log.v(TAG, "Scan Receive !");
                Log.v(TAG, "=============== ResultReceiver's    Context = " + context + " ====");
            }
            if (mScanner != null) {
                mScanner.aDecodeGetResult(mDecodeResult);
                String barcode = mDecodeResult.toString();
                if (Common.D) {
                    Log.v(TAG, "mScanner is not NULL, barcode is " + barcode.toString());
                }
                if (!barcode.equals("READ_FAIL")) {
//					읽어들인 바코드값을 전달
                    Intent i = new Intent();
                    i.putExtra("BARCODE", barcode);
                    i.setAction(RECEIVE_PM80);

                    context.sendBroadcast(i);
                }
            }
        }
    }

    //	PDA 스캐너 초기화
    private void initScanner() {
        Log.v(TAG, "PM80 Scanner Init !");
        if (mScanner == null) {
            if (Common.D) {
                Log.v(TAG, "PM80 ScanManager 초기화");
            }
            mScanner = new ScanManager();
        }
        if (mDecodeResult == null) {
            if (Common.D) {
                Log.v(TAG, "PM80 DecodeResult 초기화");
            }
            mDecodeResult = new DecodeResult();
        }
        if (mScanner != null) {
            mScanner.aDecodeAPIInit();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            mScanner.aDecodeSetDecodeEnable(1);
            mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG);

			/*if (mScanner.aDecodeGetTriggerMode() == ScanConst.TriggerMode.DCD_TRIGGER_MODE_AUTO) {
				mAutoScanOption.setChecked(true);
			} else {
				mAutoScanOption.setChecked(false);
			}

			if (mScanner.aDecodeGetBeepEnable() == 1) {
				mBeepOption.setChecked(true);
			} else {
				mBeepOption.setChecked(false);
			}*/
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        initScanner();
        IntentFilter filter = new IntentFilter(RECEIVE_PM80);
        this.registerReceiver(m_brc, filter);                        // ScanResultReceiver에서 전달하는 값을 받기위한 Receiver 등록

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        View mCustomView = LayoutInflater.from(this).inflate(R.layout.layout_actionbar, null);
        actionBar.setCustomView(mCustomView);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        btn_init = (Button) mCustomView.findViewById(R.id.btn_init);
        swt_print = (SwitchCompat) mCustomView.findViewById(R.id.swt_print);
        if (Common.print_bool) {
            swt_print.setChecked(true);
        } else {
            swt_print.setChecked(false);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences spfBluetooth = getSharedPreferences("spfBluetooth", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = spfBluetooth.edit();
        switch (buttonView.getId()) {
            case R.id.swt_print:
                if (!isChecked) {
                    Toast.makeText(getApplicationContext(), "인쇄 : OFF", Toast.LENGTH_SHORT).show();
                    Common.print_bool = false;
                } else if (isChecked) {
                    Toast.makeText(getApplicationContext(), "인쇄 : ON", Toast.LENGTH_SHORT).show();
                    Common.print_bool = true;
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        swt_print.setOnCheckedChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScanner = null;
        unregisterReceiver(m_brc);                                    // Receiver 해제
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setMessage(String msg) {
    }

    //	ScanResultReceiver에서 전달하는 값을 받기위한 Receiver
    BroadcastReceiver m_brc = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Common.D) {
                Log.v(TAG, "Context : " + context + "Intent : " + intent);
            }
            String action = intent.getAction();

            if (RECEIVE_PM80.equals(action)) {
                if (Common.D) {
                    Log.e(TAG, "===== Receiver action From PM80 =====");
                }
                String receive_data = intent.getStringExtra("BARCODE");
                setMessage(receive_data);
            }
        }
    };
}
