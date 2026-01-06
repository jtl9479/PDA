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

/**
 * SettingActivity - 설정 화면
 *
 * 프린터 사용 여부를 설정하는 화면입니다.
 *
 * ■ 주요 기능:
 *   1. 프린터 ON/OFF 설정
 *   2. 설정값 SharedPreferences 저장
 *   3. 숨김 기능: 이미지 7회 클릭 시 전체 계근내역 삭제
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

    /** 이미지 클릭 카운트 (숨김 기능용, 7회 이상 클릭 시 계근내역 삭제 다이얼로그 표시) */
    private int clkCount = 0;

    /** 프린터 설정 저장용 SharedPreferences */
    SharedPreferences spfBluetooth;

    /** SharedPreferences Editor */
    SharedPreferences.Editor editor;

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
        }
    }

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
     * - 프린터 설정값을 SharedPreferences에 저장
     * - OFF인 경우 프린터 주소도 초기화
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, TAG + " onDestroy");

        // 프린터 설정값 저장
        if (Common.printer_setting) {
            // ON: 프린터 사용
            editor.putBoolean("printer_setting", true);
            editor.commit();
        } else {
            // OFF: 프린터 미사용, 주소도 초기화
            editor.putBoolean("printer_setting", false);
            editor.putString("printer_address", "");
            editor.commit();
            Common.printer_address = "";
        }
    }
}
