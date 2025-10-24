package com.rgbsolution.highland_emart.common;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.rgbsolution.highland_emart.R;
import com.rgbsolution.highland_emart.adapter.UnknownAdapter;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Goodswets_Info;

import java.util.ArrayList;
import java.util.HashMap;

public class ProgressDlgGoodsWetSearch extends AsyncTask<Integer, String, Integer> {

    private static final String TAG = "pDlgGoodsWetSearch";

    private ProgressDialog pDialog;
    private ArrayList<Goodswets_Info> arGI;

    private LayoutInflater Inflater;
    private View dlog_unknownLayout;
    private AlertDialog unKnownDialog;

    private UnknownAdapter unknownAdapter;
    private ListView unknownList;
    private ArrayList<String[]> list_unknown;

    private Context mContext;
    String receiveData = "";                            // 조회결과를 Received

    public ProgressDlgGoodsWetSearch(Context context, ArrayList<String[]> list_unknown) {
        mContext = context;
        this.list_unknown = list_unknown;
    }

    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setTitle("계근정보 조회중 입니다.");
        pDialog.setMessage("잠시만 기다려 주세요..");
        pDialog.setCancelable(false);
        pDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        try {

            ArrayList<String> list_id_info = DBHandler.selectqueryGIDIDList(mContext);

            String data = " WHERE ";
            for (int i = 0; i < list_id_info.size(); i++) {
                if (i == list_id_info.size() - 1) {
                    data = data + "GI_D_ID = '" + list_id_info.get(i).toString() + "'";
                } else {
                    data = data + "GI_D_ID = '" + list_id_info.get(i).toString() + "' OR ";
                }
            }

            if(list_id_info.size() == 0){
                data = data + "1=0";
            }

            Log.i(TAG, "Wet GI_D_ID List : " + data);

            // 디비접속 설정
            receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_goods_wet", Common.URL_SEARCH_GOODS_WET);

            //결과값의 앞, 뒤에 공백 제거
            receiveData = receiveData.replace("\r\n", "");
            receiveData = receiveData.replace("\n", "");
            if (Common.D) {
                Log.d(TAG, "Wet receiveData : " + receiveData.toString());
            }
            //결과값을 row별로 split
            String[] result = receiveData.split(";;");
            arGI = new ArrayList<Goodswets_Info>();
            String[] temp;

            publishProgress("max", Integer.toString(result.length));
            if (Common.D) {
                Log.d(TAG, "Wet result's Count : " + result.length);
            }

            if (receiveData.toString() != "" && result.length > 0) {
                HashMap<String, String> hTemp;
                Goodswets_Info gi;
                for (String s : result) {
                    //각 row의 데이터별로 split
                    temp = s.split("::");
                    gi = new Goodswets_Info();
                    gi.setGI_D_ID(temp[0].toString());
                    gi.setWEIGHT(temp[1].toString());
                    gi.setWEIGHT_UNIT(temp[2].toString());
                    gi.setPACKER_PRODUCT_CODE(temp[3].toString());
                    gi.setBARCODE(temp[4].toString());
                    gi.setPACKER_CLIENT_CODE(temp[5].toString());
                    gi.setBOX_CNT(temp[6].toString());
                    gi.setREG_ID(temp[7].toString());
                    gi.setREG_DATE(temp[8].toString());
                    gi.setREG_TIME(temp[9].toString());
                    gi.setMAKINGDATE(temp[10].toString());
                    gi.setBOXSERIAL(temp[11].toString());
                    gi.setBOX_ORDER(temp[12].toString());
                    gi.setSAVE_TYPE("Y");
                    //계근정보 내부 SQLite에 INSERT
                    DBHandler.insertqueryGoodsWet(mContext, gi);
                    arGI.add(gi);
                    publishProgress("progress", Integer.toString(arGI.size()), Integer.toString(arGI.size()) + "번 데이터 저장중..");
                }
            }
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
        pDialog.dismiss();
        if (mContext.toString().split("[@]")[0].equals("com.highland.LoginActivity")) {
            if (Common.D) {
                Log.d(TAG, "Context : LoginActivity");
                Log.d(TAG, "search_bool : " + Common.search_bool);
            }
            /*
            if(Common.search_bool) {
				DBHandler.deletequeryBarcodeInfo(mContext);			// 바코드정보 삭제
				new ProgressDlgBarcodeSearch(mContext).execute();
			} else {
				((Activity)mContext).finish();
				Intent i = new Intent(mContext, MainActivity.class);
				mContext.startActivity(i);
			}
			*/
        }
        show_unknownDialog(list_unknown);
        //finish();
        //Intent i = new Intent(LoginActivity.this, MainActivity.class);
        //startActivity(i);														// 메인Activity로 화면 전환
    }

    public void show_unknownDialog(ArrayList<String[]> list_unknown) {
        try {
            Inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            dlog_unknownLayout = Inflater.inflate(R.layout.dialog_unknown, null);

            //선택된 출하대상의 정보 초기화
            final AlertDialog.Builder dlog = new AlertDialog.Builder(mContext, R.style.AppCompatDialogStyle)
                    .setCancelable(true);
            dlog.setTitle("바코드정보가 없는 상품목록");
            dlog.setView(dlog_unknownLayout);
            unKnownDialog = dlog.create();
            unKnownDialog.show();

            unknownAdapter = new UnknownAdapter(mContext, R.layout.list_unknown, list_unknown);

            unknownList = (ListView) dlog_unknownLayout.findViewById(R.id.dlog_unknown);
            unknownList.setAdapter(unknownAdapter);
            unknownAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            if (Common.D) {
                Log.d(TAG, "Error 1001 : " + e.getMessage().toString());
            }
        }
    }
}