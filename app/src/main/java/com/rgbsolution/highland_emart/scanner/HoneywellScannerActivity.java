package com.rgbsolution.highland_emart.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

/**
 * 바코드 스캐너 기능을 제공하는 기본 Activity (Honeywell EDA51 전용)
 *
 * PDA 디바이스의 바코드 스캐너 결과를 수신하는 기능을 제공한다.
 * 계근 관련 Activity들(ShipmentActivity, ProductionActivity 등)이 이 클래스를 상속받아
 * 바코드 스캔 기능을 사용한다.
 *
 * [현재 지원 디바이스]
 * - Honeywell EDA51 (Intent 방식)
 *
 * [바코드 수신 흐름]
 * 1. PDA 스캐너 버튼 → Honeywell 시스템이 바코드 디코딩
 * 2. Intent 브로드캐스트 발송 (ACTION_BARCODE_DATA)
 * 3. m_brc에서 직접 수신
 * 4. setMessage() 호출 → 하위 Activity에서 오버라이드하여 처리
 *
 * [ActionBar 구성]
 * - 뒤로가기 버튼
 * - 초기화 버튼 (btn_init)
 * - 인쇄 ON/OFF 스위치 (swt_print)
 *
 * [DataWedge 설정 필요]
 * 설정 → Honeywell Settings → Scan Settings → Internal Scanner
 *   → Default profile → Data Processing Settings → Scan To Intent (활성화)
 *
 */
public class HoneywellScannerActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "HoneywellScannerActivity";

    // ========================================================================================
    // 상수 (Honeywell EDA51)
    // ========================================================================================

    /** Honeywell EDA51 바코드 스캔 Action */
    private static final String ACTION_BARCODE_DATA =
            "com.honeywell.scantointent.intent.action.BARCODE_DATA";

    /** Honeywell EDA51 바코드 데이터 Extra 키 */
    private static final String EXTRA_BARCODE_DATA =
            "com.honeywell.scantointent.intent.extra.DATA";

    // ========================================================================================
    // UI 컴포넌트 (ActionBar)
    // ========================================================================================

    /** 초기화 버튼 */
    protected Button btn_init;

    /** 인쇄 ON/OFF 스위치 */
    protected SwitchCompat swt_print;

    // ========================================================================================
    // Activity 생명주기
    // ========================================================================================

    /**
     * Activity 생성 시 호출
     *
     * BroadcastReceiver 등록, ActionBar 설정을 수행한다.
     *
     * @param savedInstanceState 저장된 상태 (사용하지 않음)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // Honeywell 바코드 수신을 위한 Receiver 등록
        IntentFilter filter = new IntentFilter(ACTION_BARCODE_DATA);
        this.registerReceiver(m_brc, filter);

        // ActionBar 설정
        ActionBar actionBar = getSupportActionBar();
        View mCustomView = LayoutInflater.from(this).inflate(R.layout.layout_actionbar, null);
        actionBar.setCustomView(mCustomView);

        // ActionBar 옵션 설정
        actionBar.setDisplayHomeAsUpEnabled(true);   // 뒤로가기 버튼 표시
        actionBar.setHomeButtonEnabled(true);        // 홈 버튼 활성화
        actionBar.setDisplayShowTitleEnabled(false); // 기본 타이틀 숨김
        actionBar.setDisplayShowCustomEnabled(true); // 커스텀 뷰 표시

        // ActionBar UI 컴포넌트 초기화
        btn_init = (Button) mCustomView.findViewById(R.id.btn_init);
        swt_print = (SwitchCompat) mCustomView.findViewById(R.id.swt_print);

        // 인쇄 스위치 초기값 설정
        swt_print.setChecked(Common.print_bool);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * 인쇄 스위치 상태 변경 콜백
     *
     * @param buttonView 변경된 스위치
     * @param isChecked 체크 상태
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.swt_print:
                if (!isChecked) {
                    Toast.makeText(getApplicationContext(), "인쇄 : OFF", Toast.LENGTH_SHORT).show();
                    Common.print_bool = false;
                } else {
                    Toast.makeText(getApplicationContext(), "인쇄 : ON", Toast.LENGTH_SHORT).show();
                    Common.print_bool = true;
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 스위치 리스너 재등록
        swt_print.setOnCheckedChangeListener(this);
    }

    /**
     * Activity 종료 시 호출
     *
     * BroadcastReceiver 등록 해제를 수행한다.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(m_brc);
    }

    /**
     * ActionBar 메뉴 아이템 선택 처리
     *
     * 뒤로가기 버튼 클릭 시 Activity를 종료한다.
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

    // ========================================================================================
    // 바코드 수신 콜백
    // ========================================================================================

    /**
     * 바코드 수신 콜백 메서드 (하위 Activity에서 오버라이드)
     *
     * 스캐너에서 바코드가 수신되면 이 메서드가 호출된다.
     * 하위 Activity(ShipmentActivity, ProductionActivity 등)에서 이 메서드를
     * 오버라이드하여 바코드 처리 로직을 구현한다.
     *
     * @param msg 스캔된 바코드 문자열
     */
    protected void setMessage(String msg) {
    }

    // ========================================================================================
    // Honeywell 바코드 수신 (BroadcastReceiver)
    // ========================================================================================

    /**
     * Honeywell 바코드 수신 Receiver
     *
     * Honeywell EDA51에서 바코드 스캔 시 발송되는 Intent를 수신하여 setMessage()를 호출한다.
     * onCreate()에서 ACTION_BARCODE_DATA Action으로 등록된다.
     */
    BroadcastReceiver m_brc = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Common.D) {
                Log.v(TAG, "Context : " + context + "Intent : " + intent);
            }

            String action = intent.getAction();

            // Honeywell 바코드 수신
            if (ACTION_BARCODE_DATA.equals(action)) {
                if (Common.D) {
                    Log.e(TAG, "===== Receiver action From Honeywell =====");
                }
                // 바코드 데이터 추출 및 콜백 호출
                String receive_data = intent.getStringExtra(EXTRA_BARCODE_DATA);
                setMessage(receive_data);
            }
        }
    };
}
