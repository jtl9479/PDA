package com.rgbsolution.highland_emart.shipment.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.print.BixolonSocketPrinter;

/**
 * 블루투스 프린터 연결 해제 AsyncTask (개발/77 Step 4, BixolonShipmentActivity.ProgressDlgDiscon 이동)
 * Activity 종료 시 프린터 연결을 해제한다. onDestroy()에서 호출된다.
 */
public class PrintDisconnectTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    public interface Host {
        void setConnectDialog(ProgressDialog dialog);    // cDialog = dialog
        BixolonSocketPrinter getPrinter();               // mBixolonPrinter
        void releasePrinter();                           // mBixolonPrinter = null (백그라운드 스레드)
    }

    private Context mContextDiscon;
    private Host host;

    public PrintDisconnectTask(Context context, Host host) {
        mContextDiscon = context;
        this.host = host;
    }

    @Override
    protected void onPreExecute() {
        ProgressDialog cDialog = new ProgressDialog(mContextDiscon);
        host.setConnectDialog(cDialog);
        cDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        cDialog.setTitle("프린터를 해제중 입니다.");
        cDialog.setMessage("잠시만 기다려 주세요..");
        cDialog.setCancelable(false);
        cDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (host.getPrinter() != null) host.getPrinter().disconnect();
            host.releasePrinter();
        } catch (Exception e) {
            if (Common.D) {
                Log.e(TAG, "e : " + e.toString());
            }
        }
        return null;
    }
}
