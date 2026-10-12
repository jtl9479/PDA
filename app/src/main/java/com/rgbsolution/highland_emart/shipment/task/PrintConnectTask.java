package com.rgbsolution.highland_emart.shipment.task;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.print.BixolonSocketPrinter;

/**
 * 블루투스 프린터 연결 AsyncTask (개발/77 Step 4, BixolonShipmentActivity.ProgressDlgPrintConnect 이동)
 * 저장된 주소({@code Common.printer_address})로 자동 연결하고, 성공 시 mHandler 로 알린다.
 * 다이얼로그·어댑터·프린터는 원래처럼 Activity 필드를 사용하도록 Host 로 주고받는다.
 */
public class PrintConnectTask extends AsyncTask<Integer, String, Integer> {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    public interface Host {
        void setConnectDialog(ProgressDialog dialog);    // cDialog = dialog
        BluetoothAdapter getBluetoothAdapter();          // mBluetoothAdapter
        BixolonSocketPrinter getPrinter();               // mBixolonPrinter
    }

    private Context mContext;
    private Host host;

    public PrintConnectTask(Context context, Host host) {
        mContext = context;
        this.host = host;
    }

    @Override
    protected void onPreExecute() {
        ProgressDialog cDialog = new ProgressDialog(mContext);
        host.setConnectDialog(cDialog);
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
            //선택된 장비 연결 시도
            BluetoothDevice device = host.getBluetoothAdapter().getRemoteDevice(Common.printer_address);
            if (Common.D) {
                Log.e(TAG, "print_address :: " + Common.printer_address);
            }
            host.getPrinter().connect(device);
        } catch (Exception e) {
            if (Common.D) {
                Log.e(TAG, "e : " + e.toString());
            }
        }

        return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {
    }
}
