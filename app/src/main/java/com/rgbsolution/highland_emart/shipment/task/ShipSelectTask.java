package com.rgbsolution.highland_emart.shipment.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.shipment.mode.ShipmentMode;

import java.util.ArrayList;

/**
 * 출하 대상 조회 AsyncTask (개발/77 Step 6, BixolonShipmentActivity.ProgressDlgShipSelect 이동)
 * 로컬 DB 에서 선택 센터의 출하 대상을 읽어 Activity 의 arSM 에 넣는다.
 * 화면 반영(리스트·스피너·센터 집계)은 Activity 메서드(onShipSelectPostExecute)에 그대로 남긴다.
 */
public class ShipSelectTask extends AsyncTask<Integer, String, Integer> {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    public interface Host {
        void setProgressDialog(ProgressDialog dialog);           // pDialog = dialog
        void resetCenterTotals();                                // centerTotal/Work Count·Weight = 0
        void setShipments(ArrayList<Shipments_Info> list);       // arSM = list (백그라운드, 조회 직후)
        void onSelectPostExecute(boolean type);                  // 기존 onPostExecute 본문
    }

    private Context mContext;
    private String center_name;
    private String condition;
    private boolean type;
    private ShipmentMode mode;
    private Host host;
    private ProgressDialog pDialog;

    public ShipSelectTask(Context context, String center_name, String condition, boolean type, ShipmentMode mode, Host host) {
        mContext = context;
        this.center_name = center_name;
        this.condition = condition;
        this.type = type;           // true ? 패커상품코드 스캔 : BL스캔
        this.mode = mode;
        this.host = host;
    }

    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);
        host.setProgressDialog(pDialog);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setTitle("출하대상 불러오는중...");
        pDialog.setMessage("잠시만 기다려 주세요..");
        pDialog.setCancelable(false);
        pDialog.show();

        host.resetCenterTotals();
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        try {
            //계근지점 검색 하는 쿼리
            ArrayList<Shipments_Info> arSM = DBHandler.selectqueryShipment(mContext, this.center_name, this.condition, this.type);
            host.setShipments(arSM);

            for (int i = 0; i < arSM.size(); i++) {
                String[] row = new String[2];
                row = DBHandler.selectqueryListGoodsWetInfo(mContext, arSM.get(i).getGI_D_ID(), arSM.get(i).getPACKER_PRODUCT_CODE(), arSM.get(i).getCLIENT_CODE(), arSM.get(i).getGI_L_ID());
                arSM.get(i).setGI_QTY(Double.parseDouble(row[0]));      // 중량
                arSM.get(i).setPACKING_QTY(Integer.parseInt(row[1]));   // 수량
                arSM.get(i).setSAVE_CNT(Integer.parseInt(row[2]));      // 계근 상품 전송 개수
            }

            if (Common.D) {
                Log.d(TAG, "result's Count : " + arSM.size());
            }

            // 마트별 목록 로드 후 처리 (롯데 박스순번 초기화, 개발/76)
            mode.onShipmentListLoaded(arSM);

        } catch (Exception e) {
            if (Common.D) {
                Log.e(TAG, "e : " + e.toString());
            }
        }

        return 0;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        if (progress[0].equals("progress")) {
            pDialog.setProgress(Integer.parseInt(progress[1]));
            pDialog.setMessage(progress[2]);
        } else if (progress[0].equals("max")) {
            pDialog.setMax(Integer.parseInt(progress[1]));
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        host.onSelectPostExecute(this.type);
    }
}
