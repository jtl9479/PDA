package com.rgbsolution.highland_emart.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;
import java.util.HashMap;

public class ProgressDlgShipSearch extends AsyncTask<Integer, String, Integer> {

    private static final String TAG = "ProgressDlgShipSearch";
    ProgressDialog pDialog;
    ArrayList<Shipments_Info> list_si_info;
    private Context mContext;
    String receiveData = "";                            // 조회결과를 Received

    public ProgressDlgShipSearch(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setTitle("대상 조회중 입니다.");
        pDialog.setMessage("잠시만 기다려 주세요..");
        pDialog.setCancelable(false);
        pDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        try {
            Log.d(TAG, "대상 조회 시작");
            if (Common.D) {
        }

            // PDA가 가지고있는 목록 GI_D_ID / EOI_ID 리스트
            ArrayList<Shipments_Info> list_si_pda = DBHandler.selectqueryAllShipment(mContext);
            Log.d(TAG, "============== PDA List's size : " + list_si_pda.size() + "================");

            String data = " WHERE GI_REQ_DATE = '" + Common.selectDay + "'"; // test 시 날짜 String으로 직접 세팅

            if(Common.searchType.equals("0")){   // 출하대상 리스트
                if(Common.selectWarehouse.equals("삼일냉장")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
                }else if(Common.selectWarehouse.equals("SWC")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
                }else if(Common.selectWarehouse.equals("이천1센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4001'";
                }else if(Common.selectWarehouse.equals("부산센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4004'";
                }else if(Common.selectWarehouse.equals("탑로지스")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
                }

                // 디비접속 설정
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT);
                //receiveData = HttpHelper.getInstance().sendData(data, "search_shipment", Common.URL_SEARCH_SHIPMENT);
                Log.d(TAG, "============== 출하리스트 조회조건 : " + data + "================");
            }else if(Common.searchType.equals("1")){ // 생산대상 리스트
                receiveData = HttpHelper.getInstance().sendDataDb(data, "Inno", "search_shipment", Common.URL_SEARCH_PRODUCTION);
                Log.d(TAG, "============== 생산리스트 조회조건 : " + data + "================");
            }else if(Common.searchType.equals("2")){
                if(Common.selectWarehouse.equals("삼일냉장")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
                }else if(Common.selectWarehouse.equals("SWC")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
                }else if(Common.selectWarehouse.equals("이천1센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4001'";
                }else if(Common.selectWarehouse.equals("부산센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4004'";
                }else if(Common.selectWarehouse.equals("탑로지스")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
                }
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT_HOMEPLUS);
                //receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_WET_PRODUCTION_CALC);
                //result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_WET_PRODUCTION_CALC);
                //receiveData = HttpHelper.getInstance().sendData(data, "search_shipment", Common.URL_SEARCH_SHIPMENT);
                Log.d(TAG, "============== 출하리스트 조회조건 홈플러스로 들어옴 : " + data + "================");
            }else if(Common.searchType.equals("3")){ //일반도매업체
                if(Common.selectWarehouse.equals("삼일냉장")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
                }else if(Common.selectWarehouse.equals("SWC")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
                }else if(Common.selectWarehouse.equals("이천1센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4001'";
                }else if(Common.selectWarehouse.equals("부산센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4004'";
                }else if(Common.selectWarehouse.equals("탑로지스")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
                }
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_SHIPMENT_WHOLESALE);
                //receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_WET_PRODUCTION_CALC);
                //result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", Common.URL_WET_PRODUCTION_CALC);
                //receiveData = HttpHelper.getInstance().sendData(data, "search_shipment", Common.URL_SEARCH_SHIPMENT);
                Log.d(TAG, "============== 출하리스트 조회조건 홈플러스로 들어옴 : " + data + "================");
            }else if(Common.searchType.equals("4")){ //비정량계근출고
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_PRODUCTION_NONFIXED);
                Log.d(TAG, "============== 출하리스트 조회조건 비정량출고로 들어옴 : " + data + "================");
            }else if(Common.searchType.equals("5")){ // 홈플러스 비정량계근출고
                receiveData = HttpHelper.getInstance().sendDataDb(data, "inno", "search_shipment", Common.URL_SEARCH_HOMEPLUS_NONFIXED);
                Log.d(TAG, "============== 출하리스트 조회조건 홈플러스 비정량출고로 들어옴 : " + data + "================");
            }else if(Common.searchType.equals("6")){ // 롯데출하대상 리스트
                if(Common.selectWarehouse.equals("삼일냉장")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN10273'";
                }else if(Common.selectWarehouse.equals("SWC")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN60464'";
                }else if(Common.selectWarehouse.equals("이천센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4001'";
                }else if(Common.selectWarehouse.equals("부산센터")){
                    data += " AND GR_WAREHOUSE_CODE = '4004'";
                }else if(Common.selectWarehouse.equals("탑로지스")){
                    data += " AND GR_WAREHOUSE_CODE = 'IN63279'";
                }
                Log.d(TAG, "==============  Common.URL_SEARCH_SHIPMENT_LOTTE : " + Common.URL_SEARCH_SHIPMENT_LOTTE + "================");
                receiveData = HttpHelper.getInstance().sendDataDb(data, "highland", "search_shipment", Common.URL_SEARCH_SHIPMENT_LOTTE);
                //receiveData = HttpHelper.getInstance().sendDataDb(data, "highland", "search_shipment", Common.URL_WET_PRODUCTION_CALC);
                //result = HttpHelper.getInstance().sendDataDb(packet, "highland", "goodswet_insert", Common.URL_WET_PRODUCTION_CALC);
                //receiveData = HttpHelper.getInstance().sendData(data, "search_shipment", Common.URL_SEARCH_SHIPMENT);
                Log.d(TAG, "============== 출하리스트 조회조건 롯데 들어옴 : " + data + "================");
            }else if(Common.searchType.equals("7")){ // 생산대상리스트(라벨)
                receiveData = HttpHelper.getInstance().sendDataDb(data, "Inno", "search_shipment", Common.URL_SEARCH_PRODUCTION_4LABEL);
                Log.d(TAG, "============== 생산대상리스트(라벨) 조회 : " + data + "================");
            }

            Log.d(TAG, "============== Common.searchType : " + Common.searchType + "================");

            //결과값의 앞, 뒤에 공백 제거
            receiveData = receiveData.replace("\r\n", "");
            receiveData = receiveData.replace("\n", "");
            if (Common.D) {
                Log.d(TAG, "receiveData : " + receiveData.toString());
            }

            //결과값을 row별로 split
            String[] result = receiveData.split(";;");
            list_si_info = new ArrayList<Shipments_Info>();
            String[] temp;
            HashMap<String, String> hTemp;
            Shipments_Info si;
            if (Common.D) {
                Log.d(TAG, "result's Count : " + result.length);
            }

            if (result.length > 0) {
                for (String s : result) {
                    //각 row의 데이터별로 split
                    temp = s.split("::");
                    si = new Shipments_Info();
                    si.setGI_H_ID(temp[0].toString());
                    si.setGI_D_ID(temp[1].toString());
                    si.setEOI_ID(temp[2].toString());
                    si.setITEM_CODE(temp[3].toString());
                    si.setITEM_NAME(temp[4].toString());
                    si.setEMARTITEM_CODE(temp[5].toString());
                    si.setEMARTITEM(temp[6].toString());
                    si.setGI_REQ_PKG(temp[7].toString());

                    String[] split_qty = temp[8].split("[.]");

                    if (split_qty.length > 1) {
                        if (split_qty[1].length() > 3) {
                            double double_qty = Double.parseDouble(temp[8].toString());            // Int -> double 로 수정
                            //double double_qty = Integer.parseInt(temp[7].toString());

                            if (Common.D) {
                                Log.d(TAG, "string_qty : " + temp[8].toString());
                                Log.d(TAG, "double_qty : " + double_qty);
                            }
                            double_qty = Math.floor(double_qty * 10);
                            double result_qty = double_qty / 10.0;

                            temp[8] = String.valueOf(result_qty);
                            if (Common.D) {
                                Log.d(TAG, "result_qty : " + result_qty);
                                Log.d(TAG, "result_string_qty : " + temp[8].toString());
                            }
                        } else {
                            if (Common.D) {
                                Log.d(TAG, "3자리이하_qty : " + temp[8].toString());
                            }
                        }
                    }

                    si.setGI_REQ_QTY(temp[8].toString());
                    si.setAMOUNT(temp[9].toString());
                    si.setGOODS_R_ID(temp[10].toString());
                    si.setGR_REF_NO(temp[11].toString());
                    si.setGI_REQ_DATE(temp[12].toString());
                    si.setBL_NO(temp[13].toString());
                    si.setBRAND_CODE(temp[14].toString());
                    si.setBRANDNAME(temp[15].toString());
                    si.setCLIENT_CODE(temp[16].toString());
                    si.setCLIENTNAME(temp[17].toString());
                    si.setCENTERNAME(temp[18].toString());
                    si.setITEM_SPEC(temp[19].toString());
                    si.setCT_CODE(temp[20].toString());
                    si.setIMPORT_ID_NO(temp[21].toString());
                    si.setPACKER_CODE(temp[22].toString());
                    si.setPACKERNAME(temp[23].toString());
                    si.setPACKER_PRODUCT_CODE(temp[24].toString());
                    si.setBARCODE_TYPE(temp[25].toString());
                    si.setITEM_TYPE(temp[26].toString());
                    si.setPACKWEIGHT(temp[27].toString());
                    si.setBARCODEGOODS(temp[28].toString());
                    si.setSTORE_IN_DATE(temp[29].toString());
                    si.setEMARTLOGIS_CODE(temp[30].toString());
                    si.setEMARTLOGIS_NAME(temp[31].toString());
                    if(Common.searchType.equals("0") || Common.searchType.equals("4")) { //이마트 출하계근일때만
                        si.setWH_AREA(temp[32].toString());
                        //배포 전까지 하단 4줄 주석처리, 2021-05-11
                        si.setUSE_NAME(temp[33].toString());
                        si.setUSE_CODE(temp[34].toString());
                        si.setCT_NAME(temp[35].toString());
                        si.setSTORE_CODE(temp[36].toString());
                        si.setEMART_PLANT_CODE(temp[37].toString());
                    } else if(Common.searchType.equals("5")) {
                        si.setWH_AREA(temp[32].toString());
                        si.setUSE_NAME(temp[33].toString());
                        si.setUSE_CODE(temp[34].toString());
                        si.setCT_NAME(temp[35].toString());
                        si.setSTORE_CODE(temp[36].toString());
                    } else if(Common.searchType.equals("6")) {
                        si.setWH_AREA(temp[32].toString());
                        si.setLAST_BOX_ORDER(temp[33].toString());
                    }
                    si.setSAVE_TYPE("F");
                    //출하대상 내부 SQLite에 INSERT
                    //DBHandler.insertqueryShipment(mContext, si, "F");
                    list_si_info.add(si);
                }
            }
            // ↑ G3 출하리스트 다운로드 끝

            // ↓ PDA G3 리스트 비교 시작
            ArrayList<String> list_delete = new ArrayList<String>();
            ArrayList<Shipments_Info> list_insert = new ArrayList<Shipments_Info>();


            // 삭제 리스트 검색
            for (int i = 0; i < list_si_pda.size(); i++) {   // pda 리스트
                int check = 0;
                for (int j = 0; j < list_si_info.size(); j++) {   // down 리스트
                    if (list_si_pda.get(i).getGI_D_ID().equals(list_si_info.get(j).getGI_D_ID())) {
                        check++;
                        break;
                    }
                }
                if (check == 0) {
                    list_delete.add(list_si_pda.get(i).getGI_D_ID());
                    Log.e(TAG, "==============================================");
                    Log.e(TAG, "================ 삭제 ItemInfo ================");
                    Log.e(TAG, "GI_D_ID : " + list_si_pda.get(i).getGI_D_ID());
                    Log.e(TAG, "==============================================");
                }
            }

            // 추가 리스트 검색
            for (int i = 0; i < list_si_info.size(); i++) {
                int check = 0;
                for (int j = 0; j < list_si_pda.size(); j++) {
                    if (list_si_info.get(i).getGI_D_ID().equals(list_si_pda.get(j).getGI_D_ID())) {
                        check++;
                        break;
                    }
                }

                if (check == 0) {
                    list_insert.add(list_si_info.get(i));
                    Log.e(TAG, "==============================================");
                    Log.e(TAG, "================ 추가 ItemInfo ================");
                    Log.e(TAG, "GI_D_ID : " + list_si_info.get(i).getGI_D_ID());
                    Log.e(TAG, "==============================================");
                }
            }

            boolean refresh_result = DBHandler.refreshShipmentList(mContext, list_delete, list_insert);
            if (refresh_result) {
                // refresh 성공
                Log.v(TAG, "Shipment Refresh 성공");
            } else {
                // refresh 실패
                Log.v(TAG, "Shipment Refresh 실패");
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
        new ProgressDlgBarcodeSearch(mContext).execute();
        //new ProgressDlgGoodsWetSearch(mContext).execute();
        //((Activity)mContext).finish();
        //Intent i = new Intent(mContext, MainActivity.class);
        //mContext.startActivity(i);
    }
}
