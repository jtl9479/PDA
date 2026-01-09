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

import device.common.DecodeResult;
import device.common.ScanConst;
import device.sdk.ScanManager;

/**
 * 바코드 스캐너 기능을 제공하는 기본 Activity
 *
 * PDA 디바이스의 바코드 스캐너를 초기화하고 스캔 결과를 수신하는 기능을 제공한다.
 * 계근 관련 Activity들(ShipmentActivity, ProductionActivity 등)이 이 클래스를 상속받아
 * 바코드 스캔 기능을 사용한다.
 *
 * [현재 지원 디바이스]
 * - Point Mobile PM80 (device.sdk.ScanManager 사용)
 *
 * [바코드 수신 흐름]
 * 1. PDA 스캐너 버튼 → PM80 SDK가 바코드 디코딩
 * 2. ScanResultReceiver가 결과 수신 (AndroidManifest에 등록)
 * 3. 내부 브로드캐스트로 m_brc에 전달
 * 4. setMessage() 호출 → 하위 Activity에서 오버라이드하여 처리
 *
 * [ActionBar 구성]
 * - 뒤로가기 버튼
 * - 초기화 버튼 (btn_init)
 * - 인쇄 ON/OFF 스위치 (swt_print)
 *
 */
public class ScannerActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "ScannerActivity";

    // ========================================================================================
    // 상수
    // ========================================================================================

    /** 내부 브로드캐스트 Action (ScanResultReceiver → m_brc) */
    private static final String RECEIVE_PM80 = "ACTION_RECEIVE_PM80";

    // ========================================================================================
    // PM80 스캐너 SDK (Point Mobile)
    // ========================================================================================

    /** PM80 스캐너 매니저 (싱글톤) */
    public static ScanManager mScanner = null;

    /** PM80 바코드 디코딩 결과 저장 객체 */
    private static DecodeResult mDecodeResult = null;

    // ========================================================================================
    // UI 컴포넌트 (ActionBar)
    // ========================================================================================

    /** 초기화 버튼 */
    protected Button btn_init;

    /** 인쇄 ON/OFF 스위치 */
    protected SwitchCompat swt_print;

    // ========================================================================================
    // PM80 스캐너 결과 수신 (BroadcastReceiver)
    // ========================================================================================

    /**
     * PM80 스캐너 결과 수신 BroadcastReceiver
     *
     * AndroidManifest.xml에 등록되어 PM80 SDK로부터 스캔 결과를 수신한다.
     * 수신된 바코드를 내부 브로드캐스트(RECEIVE_PM80)로 재전송하여 m_brc에서 처리한다.
     *
     * [수신 Action]
     * - device.common.USERMSG (AndroidManifest에 설정)
     */
    public static class ScanResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Common.D) {
                Log.v(TAG, "Scan Receive !");
                Log.v(TAG, "=============== ResultReceiver's    Context = " + context + " ====");
            }
            if (mScanner != null) {
                // PM80 SDK에서 디코딩 결과 조회
                mScanner.aDecodeGetResult(mDecodeResult);
                String barcode = mDecodeResult.toString();
                if (Common.D) {
                    Log.v(TAG, "mScanner is not NULL, barcode is " + barcode.toString());
                }
                // 스캔 성공 시 내부 브로드캐스트로 전달
                if (!barcode.equals("READ_FAIL")) {
                    Intent i = new Intent();
                    i.putExtra("BARCODE", barcode);
                    i.setAction(RECEIVE_PM80);
                    context.sendBroadcast(i);
                }
            }
        }
    }

    // ========================================================================================
    // PM80 스캐너 초기화
    // ========================================================================================

    /**
     * PM80 스캐너 초기화
     *
     * PM80 SDK의 ScanManager와 DecodeResult를 초기화하고 스캐너를 활성화한다.
     * Activity 생성 시 onCreate()에서 호출된다.
     */
    private void initScanner() {
        Log.v(TAG, "PM80 Scanner Init !");

        // ScanManager 초기화 (싱글톤)
        if (mScanner == null) {
            if (Common.D) {
                Log.v(TAG, "PM80 ScanManager 초기화");
            }
            mScanner = new ScanManager();
        }

        // DecodeResult 초기화
        if (mDecodeResult == null) {
            if (Common.D) {
                Log.v(TAG, "PM80 DecodeResult 초기화");
            }
            mDecodeResult = new DecodeResult();
        }

        // 스캐너 API 초기화 및 설정
        if (mScanner != null) {
            mScanner.aDecodeAPIInit();
            try {
                Thread.sleep(500);  // SDK 초기화 대기
            } catch (InterruptedException e) {
            }
            mScanner.aDecodeSetDecodeEnable(1);  // 스캐너 활성화
            mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG);  // 결과 타입 설정
        }
    }

    // ========================================================================================
    // Activity 생명주기
    // ========================================================================================

    /**
     * Activity 생성 시 호출
     *
     * 스캐너 초기화, BroadcastReceiver 등록, ActionBar 설정을 수행한다.
     *
     * @param savedInstanceState 저장된 상태 (사용하지 않음)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // PM80 스캐너 초기화
        initScanner();

        // 내부 브로드캐스트 수신을 위한 Receiver 등록
        IntentFilter filter = new IntentFilter(RECEIVE_PM80);
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
        // 스위치 리스너 재등록
        swt_print.setOnCheckedChangeListener(this);
    }

    /**
     * Activity 종료 시 호출
     *
     * 스캐너 참조 해제 및 BroadcastReceiver 등록 해제를 수행한다.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mScanner = null;
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
    // 내부 브로드캐스트 수신 (BroadcastReceiver)
    // ========================================================================================

    /**
     * 내부 브로드캐스트 수신 Receiver
     *
     * ScanResultReceiver에서 전달된 바코드를 수신하여 setMessage()를 호출한다.
     * onCreate()에서 RECEIVE_PM80 Action으로 등록된다.
     */
    BroadcastReceiver m_brc = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Common.D) {
                Log.v(TAG, "Context : " + context + "Intent : " + intent);
            }
            String action = intent.getAction();

            // PM80 내부 브로드캐스트 수신
            if (RECEIVE_PM80.equals(action)) {
                if (Common.D) {
                    Log.e(TAG, "===== Receiver action From PM80 =====");
                }
                // 바코드 데이터 추출 및 콜백 호출
                String receive_data = intent.getStringExtra("BARCODE");
                setMessage(receive_data);
            }
        }
    };
}
