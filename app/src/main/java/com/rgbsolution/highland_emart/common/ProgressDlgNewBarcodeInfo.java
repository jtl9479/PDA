package com.rgbsolution.highland_emart.common;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Barcodes_Info;

import java.util.HashMap;

public class ProgressDlgNewBarcodeInfo extends AsyncTask<String, Void, String> {

    private static final String TAG = "pDlgNewBarcodeInfo";

    ProgressDialog pDialog;
    private Context mContext;
    private AlertDialog infoDialog;

    String addData = "";
    String receiveData = "";


    public ProgressDlgNewBarcodeInfo(Context context, AlertDialog alertDialog) {
        mContext = context;
        infoDialog = alertDialog;
    }

    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setTitle("등록중 입니다.");
        pDialog.setMessage("잠시만 기다려 주세요..");
        pDialog.setCancelable(false);
        pDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            if (Common.D) {
                Log.d(TAG, "바코드 등록 전송정보 : " + params[0]);
            }
            addData = params[0];
            //Thread.sleep(2000);
            if (Common.D) {
                Log.d(TAG, "등록 시작");
            }
            receiveData = HttpHelper.getInstance().sendData(params[0], "barcode_new_insert", Common.URL_INSERT_BARCODE_INFO);

        } catch (Exception e) {
            if (Common.D) {
                e.printStackTrace();
                Log.e(TAG, "e : " + e.toString());
            }
        }
        return receiveData;
    }

    @Override
    protected void onPostExecute(String _result) {
        //결과값의 앞, 뒤에 공백 제거
        String result = _result.replace("\r\n", "");
        result = result.replace("\n", "");
        pDialog.dismiss();

        //s : 성공, f : 실패
        if (result.toString().equals("s")) {

            String[] split_data = addData.split("::");

            Barcodes_Info bi = new Barcodes_Info();
            HashMap<String, String> temp = new HashMap<String, String>();
            temp.put("PACKER_CLIENT_CODE", split_data[4].toString());
            temp.put("BRAND_CODE", split_data[0].toString());
            temp.put("PACKER_PRODUCT_CODE", split_data[1].toString());
            temp.put("PACKER_PRD_NAME", split_data[2].toString());
            temp.put("ITEM_CODE", split_data[3].toString());
            temp.put("BARCODEGOODS", split_data[5].toString());
            temp.put("BASEUNIT", split_data[6].toString());
            temp.put("ZEROPOINT", split_data[7].toString());
            temp.put("PACKER_PRD_CODE_FROM", split_data[8].toString());
            temp.put("PACKER_PRD_CODE_TO", split_data[9].toString());
            temp.put("BARCODEGOODS_FROM", split_data[10].toString());
            temp.put("BARCODEGOODS_TO", split_data[11].toString());
            temp.put("WEIGHT_FROM", split_data[12].toString());
            temp.put("WEIGHT_TO", split_data[13].toString());
            temp.put("MAKINGDATE_FROM", split_data[14].toString());
            temp.put("MAKINGDATE_TO", split_data[15].toString());
            temp.put("BOXSERIAL_FROM", split_data[16].toString());
            temp.put("BOXSERIAL_TO", split_data[17].toString());
            temp.put("STATUS", split_data[18].toString());
            temp.put("REG_ID", Common.REG_ID);
            DBHandler.insertqueryBarcodeInfo(mContext, bi);
            Toast.makeText(mContext, "등록 완료", Toast.LENGTH_SHORT).show();
            infoDialog.dismiss();
        } else {
            Toast.makeText(mContext, "등록 실패, " + result, Toast.LENGTH_SHORT).show();
            if (Common.D) {
                Log.d(TAG, "등록 실패:: " + result);
            }
        }
    }
}