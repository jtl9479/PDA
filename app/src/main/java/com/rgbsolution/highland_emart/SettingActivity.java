package com.rgbsolution.highland_emart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.db.DBHandler;

public class SettingActivity extends AppCompatActivity {

    private final String TAG = "SettingActivity";
    private Button btn_on;
    private Button btn_off;
    private int clkCount = 0;

    SharedPreferences spfBluetooth;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        btn_on = (Button) findViewById(R.id.btn_on);
        btn_off = (Button) findViewById(R.id.btn_off);

        spfBluetooth = getSharedPreferences("spfBluetooth", Activity.MODE_PRIVATE);
        editor = spfBluetooth.edit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (Common.printer_setting) {
            btn_on.setBackgroundResource(R.drawable.setting_on_green);
            btn_off.setBackgroundResource(R.drawable.setting_off_gray);
        } else {
            btn_on.setBackgroundResource(R.drawable.setting_on_gray);
            btn_off.setBackgroundResource(R.drawable.setting_off_green);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_on:
                Log.d(TAG, "==== ON Click ====");
                Common.printer_setting = true;
                btn_on.setBackgroundResource(R.drawable.setting_on_green);
                btn_off.setBackgroundResource(R.drawable.setting_off_gray);
                break;
            case R.id.btn_off:
                Log.d(TAG, "==== OFF Click ====");
                Common.printer_setting = false;
                btn_on.setBackgroundResource(R.drawable.setting_on_gray);
                btn_off.setBackgroundResource(R.drawable.setting_off_green);
                break;
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
        }
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " onDestroy");
        if (Common.printer_setting) {
            editor.putBoolean("printer_setting", true);
            editor.commit();
        } else {
            editor.putBoolean("printer_setting", false);
            editor.putString("printer_address", "");
            editor.commit();
            Common.printer_address = "";
        }
    }
}
