package com.rgbsolution.highland_emart.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Barcodes_Info;

import java.util.ArrayList;

public class ProgressDlgBarcodeSearch extends AsyncTask<Integer, String, Integer> {

    private static final String TAG = "pDlgBarcodeSearch";

    private ProgressDialog pDialog;
    ArrayList<String[]> list_code_info;
    private ArrayList<Barcodes_Info> list_received_bi;
    private ArrayList<String[]> list_unknown_bi;
    private Context mContext;
    String receiveData = "";                            // 조회결과를 Received

    public ProgressDlgBarcodeSearch(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setTitle("바코드정보 조회중 입니다.");
        pDialog.setMessage("잠시만 기다려 주세요..");
        pDialog.setCancelable(false);
        pDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        try {

            if(Common.searchType.equals("4") || Common.searchType.equals("5") ){
                list_code_info = DBHandler.selectqueryCodeListForNonFixed(mContext);
            }else{
                list_code_info = DBHandler.selectqueryCodeList(mContext);
            }

            String data = " WHERE ";
            for (int i = 0; i < list_code_info.size(); i++) {

                if(Common.searchType.equals("4") || Common.searchType.equals("5")){
                    if (i == list_code_info.size() - 1) {
                        Log.d( TAG, "TEST DATA : " + list_code_info.get(i)[0].toString() );
                        data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "'";
                    } else {
                        data = data + "SBI.ITEM_CODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
                    }
                }else{
                    if (i == list_code_info.size() - 1) {
                        data = data + "SBI.PACKER_PRODUCT_CODE = '" + list_code_info.get(i)[0].toString() + "'";
                    } else {
                        data = data + "SBI.PACKER_PRODUCT_CODE = '" + list_code_info.get(i)[0].toString() + "' OR ";
                    }
                }
            }

            if(list_code_info.size() == 0){
                data = data + "1=0";
            }

            Log.i(TAG, "Barcode Code List : " + data);

            // 디비접속 설정
            if(Common.searchType.equals("0") || Common.searchType.equals("2") || Common.searchType.equals("3") || Common.searchType.equals("6")) { //이마트 혹은 홈플러스, 롯데
                //receiveData = HttpHelper.getInstance().sendData(data, "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO);
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO);
            }else if(Common.searchType.equals("1")){
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO);
            }else if(Common.searchType.equals("4")) {
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO_NONFIXED);
            }else if(Common.searchType.equals("5")) {
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_barcode_info", Common.URL_SEARCH_HOMEPLUS_NONFIXED2);
            }else if(Common.searchType.equals("7")){
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_barcode_info", Common.URL_SEARCH_BARCODE_INFO);
            }

            //결과값의 앞, 뒤에 공백 제거
            receiveData = receiveData.replace("\r\n", "");
            receiveData = receiveData.replace("\n", "");
            if (Common.D) {
                Log.d(TAG, "Barcode receiveData : " + receiveData.toString());
            }

            //결과값을 row별로 split
            String[] result = receiveData.split(";;");
            list_received_bi = new ArrayList<Barcodes_Info>();
            String[] temp;
            Barcodes_Info bi;
            publishProgress("max", Integer.toString(result.length));
            if (Common.D) {
                Log.d(TAG, "Barcode result's Count : " + result.length);
            }
            if (result.length > 0) {
                for (String s : result) {
                    //각 row의 데이터별로 split
                    temp = s.split("::");
                    bi = new Barcodes_Info();
                    bi.setPACKER_CLIENT_CODE(temp[0].toString());
                    bi.setPACKER_PRODUCT_CODE(temp[1].toString());
                    bi.setPACKER_PRD_NAME(temp[2].toString());
                    bi.setITEM_CODE(temp[3].toString());
                    bi.setITEM_NAME_KR(temp[4].toString());
                    bi.setBRAND_CODE(temp[5].toString());
                    bi.setBARCODEGOODS(temp[6].toString());
                    bi.setBASEUNIT(temp[7].toString());
                    bi.setZEROPOINT(temp[8].toString());
                    bi.setPACKER_PRD_CODE_FROM(temp[9].toString());
                    bi.setPACKER_PRD_CODE_TO(temp[10].toString());
                    bi.setBARCODEGOODS_FROM(temp[11].toString());
                    bi.setBARCODEGOODS_TO(temp[12].toString());
                    bi.setWEIGHT_FROM(temp[13].toString());
                    bi.setWEIGHT_TO(temp[14].toString());
                    bi.setMAKINGDATE_FROM(temp[15].toString());
                    bi.setMAKINGDATE_TO(temp[16].toString());
                    bi.setBOXSERIAL_FROM(temp[17].toString());
                    bi.setBOXSERIAL_TO(temp[18].toString());
                    bi.setSTATUS(temp[19].toString());
                    bi.setREG_ID(temp[20].toString());
                    bi.setREG_DATE(temp[21].toString());
                    bi.setREG_TIME(temp[22].toString());
                    bi.setMEMO(temp[23].toString());
                    if (!Common.searchType.equals("4") && !Common.searchType.equals("5")) {
                        bi.setSHELF_LIFE(temp[24].toString());
                    }

                    //바코드정보 내부 SQLite에 INSERT
                    DBHandler.insertqueryBarcodeInfo(mContext, bi);
                    list_received_bi.add(bi);
                    if (Common.D) {
                        Log.d(TAG, "bi : " + bi.getPACKER_CLIENT_CODE() + ", " + bi.getPACKER_PRODUCT_CODE() + ", " + bi.getPACKER_PRD_NAME() + ", " +
                                bi.getITEM_NAME_KR() + ", " + bi.getBRAND_CODE() + ", " + bi.getBARCODEGOODS() + ", " + bi.getBASEUNIT() + ", " +
                                bi.getZEROPOINT() + ", " + bi.getPACKER_PRD_CODE_FROM() + ", " + bi.getPACKER_PRD_CODE_TO() + ", " +
                                bi.getBARCODEGOODS_FROM() + ", " + bi.getBARCODEGOODS_TO() + ", " + bi.getWEIGHT_FROM() + ", " +
                                bi.getWEIGHT_TO() + ", " + bi.getSTATUS() + ", " + bi.getREG_ID() + ", " + bi.getREG_DATE() + ", " + bi.getREG_TIME() + ", " + bi.getMEMO() + ", " + bi.getSHELF_LIFE());

                    }

                    bi = null;
                    publishProgress("progress", Integer.toString(list_received_bi.size()), Integer.toString(list_received_bi.size()) + "번 데이터 저장중..");
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
        list_unknown_bi = new ArrayList<String[]>();
        for (String[] s : list_code_info) {
            int iCount = 0;
            String req_code = s[0];
            for (int i = 0; i < list_received_bi.size(); i++) {
                if (req_code.equals(list_received_bi.get(i).getPACKER_PRODUCT_CODE())) {
                    // 요청한 Packer_Product_Code를 Receive 성공
                    break;
                }
                iCount++;
            }
            if (iCount == list_received_bi.size()) {
                String[] temp = new String[2];
                temp[0] = req_code;
                temp[1] = s[1].toString();
                list_unknown_bi.add(temp);
            }
        }
        Log.i(TAG, "===== 받지 못한 BarcodeInfo's PACKER_PRODUCT_CODE =====");
        for (int i = 0; i < list_unknown_bi.size(); i++) {
            Log.e(TAG, String.valueOf((i + 1)) + ". " + list_unknown_bi.get(i)[0].toString() + " | " + list_unknown_bi.get(i)[1].toString());
        }
        Log.i(TAG, "===== 받지 못한 BarcodeInfo's PACKER_PRODUCT_CODE =====");

        new ProgressDlgGoodsWetSearch(mContext, list_unknown_bi).execute();
    }
}
