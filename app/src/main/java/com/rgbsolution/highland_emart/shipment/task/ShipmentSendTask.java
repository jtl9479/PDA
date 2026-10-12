package com.rgbsolution.highland_emart.shipment.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.common.HttpHelper;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.shipment.mode.ShipmentMode;

import java.util.ArrayList;

/**
 * 계근 데이터 서버 전송 AsyncTask (개발/77 Step 5, BixolonShipmentActivity.ProgressDlgShipmentSend 이동)
 * 출하 유형별로 호출 URL 이 다르다(mode.getSendUrl). 패킷 구분자는 {@code ::} 다.
 * doInBackground 본문 그대로. 진행 다이얼로그(pDialog)·결과 화면 처리는 Activity 필드를 쓰므로 Host 로 위임한다.
 * @see HttpHelper#sendDataDb(String, String, String, String)
 */
public class ShipmentSendTask extends AsyncTask<Void, String, String> {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    public interface Host {
        void setProgressDialog(ProgressDialog dialog);           // pDialog = dialog
        ArrayList<Shipments_Info> getShipments();                // arSM (같은 객체 — SAVE_CNT·SAVE_TYPE 수정 반영)
        void setSendList(ArrayList<Goodswets_Info> list);       // list_send_info = list (백그라운드)
        void onSendPostExecute(String _result);                  // 기존 onPostExecute 본문
    }

    private Context mContext;
    private ShipmentMode mode;
    private Host host;
    String receiveData = "";

    public ShipmentSendTask(Context context, ShipmentMode mode, Host host) {
        mContext = context;
        this.mode = mode;
        this.host = host;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ProgressDialog pDialog = new ProgressDialog(mContext);
        host.setProgressDialog(pDialog);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setTitle("전송 중 입니다.");
        pDialog.setMessage("잠시만 기다려 주세요..");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        String result = "";
        String qry_where = "";
        ArrayList<Shipments_Info> arSM = host.getShipments();
        try {
            for (int i = 0; i < arSM.size(); i++) {
                if (i == (arSM.size() - 1)) {
                    qry_where = qry_where + "GI_D_ID = " + arSM.get(i).getGI_D_ID();
                } else {
                    qry_where = qry_where + "GI_D_ID = " + arSM.get(i).getGI_D_ID() + " OR ";
                }
            }
            Log.v(TAG, "전송상품 검색 where : " + qry_where);

            ArrayList<Goodswets_Info> list_send_info = DBHandler.selectquerySendGoodsWet(mContext, qry_where);        // 목록 조회 성공
            host.setSendList(list_send_info);   // Activity 필드 list_send_info 대입 (원래 위치)
            publishProgress("max", Integer.toString(list_send_info.size()));

            int iCount = 0;
            int jChk = 0;

            if(mode.getSendType() == ShipmentMode.SendType.PER_ITEM){ //건별 전송 : 이마트, 홈플러스, 롯데 (개발/76)
                for (int i = 0; i < list_send_info.size(); i++) { //SAVE_TYPE 과 상관 없이 계근 데이터 모두 루프
                    if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
                        iCount++;
                        String packet = "";
                        packet += list_send_info.get(i).getGI_D_ID() + "::";
                        packet += list_send_info.get(i).getWEIGHT() + "::";
                        packet += list_send_info.get(i).getWEIGHT_UNIT() + "::";
                        packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::";
                        packet += list_send_info.get(i).getBARCODE() + "::";
                        packet += list_send_info.get(i).getPACKER_CLIENT_CODE() + "::";
                        packet += list_send_info.get(i).getMAKINGDATE() + "::";
                        packet += list_send_info.get(i).getBOXSERIAL() + "::";
                        packet += list_send_info.get(i).getBOX_CNT() + "::";
                        packet += list_send_info.get(i).getREG_ID() + "::";
                        packet += Common.selectCompanyCode + "::";
                        packet += list_send_info.get(i).getBRAND_CODE() + "::";
                        packet += list_send_info.get(i).getCLIENT_TYPE() + "::";
                        packet += list_send_info.get(i).getBOX_ORDER() + "::";
                        packet += list_send_info.get(i).getGI_L_ID() + "::";
                        // 라벨 출력 정보 [15]~[25] (개발/80)
                        packet += list_send_info.get(i).getBARCODE_TYPE() + "::";
                        packet += list_send_info.get(i).getLABEL_BARCODE1() + "::";
                        packet += list_send_info.get(i).getLABEL_BARCODE2() + "::";
                        packet += list_send_info.get(i).getPRINT_WEIGHT() + "::";
                        packet += list_send_info.get(i).getEXPIRY_DATE() + "::";
                        packet += list_send_info.get(i).getEXPIRY_TEXT() + "::";
                        packet += list_send_info.get(i).getPRINT_IMPORT_ID_NO() + "::";
                        packet += list_send_info.get(i).getPRINT_STORE_CODE() + "::";
                        packet += list_send_info.get(i).getPRINT_ITEM_NAME() + "::";
                        packet += list_send_info.get(i).getPRINT_POINT_NAME() + "::";
                        packet += list_send_info.get(i).getPRINT_CENTER_NAME();


                        if (Common.D) {
                            Log.d(TAG, "Send Packet : '" + packet + "'");
                        }

                        Log.i(TAG, "=====================Common.searchType==================" + Common.searchType);

                        // 디비접속 설정 — 마트별 전송 URL (이마트 insert_goods_wet / 홈플러스 _homeplus / 롯데 _lotte)
                        result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", mode.getSendUrl());

                        //결과값의 앞, 뒤에 공백 제거
                        result = result.replace("\r\n", "");
                        result = result.replace("\n", "");
                        Log.d(TAG, "i number : " + i);
                        Log.v(TAG, "전송결과 : " + result);
                        //s : 성공, f : 실패
                        if (result.equals("s")) {
                            boolean bool = DBHandler.updatequeryGoodsWet(mContext, list_send_info.get(i).getGI_D_ID(), list_send_info.get(i).getBARCODE(), list_send_info.get(i).getBOX_CNT(), list_send_info.get(i).getGI_L_ID());
                            Log.d(TAG, "boolean " + bool);
                            if (bool) {          // 전송 & PDA SQLite update 성공
                                publishProgress("progress", Integer.toString(iCount), Integer.toString(iCount) + "번 데이터 전송성공..");

                                for (int j = 0; j < arSM.size(); j++) {
                                    if (arSM.get(j).getGI_D_ID().equals(list_send_info.get(i).getGI_D_ID())
                                            && arSM.get(j).getGI_L_ID().equals(list_send_info.get(i).getGI_L_ID())) {
                                        arSM.get(j).setSAVE_CNT(arSM.get(j).getSAVE_CNT() + 1);

                                        if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {            // 전송 개수와 요청 개수 비교
                                                Log.v(TAG, "출하대상 계근 완료");
                                                arSM.get(j).setSAVE_TYPE("Y");          // 전부 전송했다면 전송여부 Y로 변경
                                                DBHandler.updatequeryShipment(mContext, arSM.get(j).getGI_D_ID(), arSM.get(j).getPACKER_PRODUCT_CODE(), arSM.get(j).getGI_L_ID());

                                                jChk++;

                                                if (jChk == arSM.size()) {
                                                    Log.d(TAG, "arSM.size() when return: " + arSM.size());
                                                    Log.d(TAG, "jChk number when return: " + jChk);
                                                    return "ss";
                                                }
                                        }
                                    }
                                }
                            }
                        } else if (result.equals("f")) {
                            return result;
                        }
                    }
                }
            }else if(mode.getSendType() == ShipmentMode.SendType.BATCH){ //일괄 전송 : 이마트 비정량, 홈플러스 비정량, 도매, 생산, 생산라벨 (개발/76)
                Log.i(TAG, "=====================여기 들어오는지 확인==================");
                Log.i(TAG, "=====================사이즈 확인=================="+list_send_info.size());
                String packet = "";
                //전문 전송용 for문
                for (int i = 0; i < list_send_info.size(); i++) {
                    if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
                        iCount++;
                        packet += list_send_info.get(i).getGI_D_ID() + "::";
                        packet += list_send_info.get(i).getWEIGHT() + "::";
                        packet += list_send_info.get(i).getWEIGHT_UNIT() + "::";
                        packet += list_send_info.get(i).getPACKER_PRODUCT_CODE() + "::";
                        packet += list_send_info.get(i).getBARCODE() + "::";
                        packet += list_send_info.get(i).getPACKER_CLIENT_CODE() + "::";
                        packet += list_send_info.get(i).getMAKINGDATE() + "::";
                        packet += list_send_info.get(i).getBOXSERIAL() + "::";
                        packet += list_send_info.get(i).getBOX_CNT() + "::";
                        packet += list_send_info.get(i).getREG_ID() + "::";
                        packet += Common.selectCompanyCode + "::";
                        packet += list_send_info.get(i).getBRAND_CODE() + "::";
                        packet += list_send_info.get(i).getCLIENT_TYPE() + "::";
                        packet += list_send_info.get(i).getBOX_ORDER() + "::" + list_send_info.get(i).getGI_L_ID() + "::";
                        // 라벨 출력 정보 [15]~[25] (개발/80)
                        packet += list_send_info.get(i).getBARCODE_TYPE() + "::";
                        packet += list_send_info.get(i).getLABEL_BARCODE1() + "::";
                        packet += list_send_info.get(i).getLABEL_BARCODE2() + "::";
                        packet += list_send_info.get(i).getPRINT_WEIGHT() + "::";
                        packet += list_send_info.get(i).getEXPIRY_DATE() + "::";
                        packet += list_send_info.get(i).getEXPIRY_TEXT() + "::";
                        packet += list_send_info.get(i).getPRINT_IMPORT_ID_NO() + "::";
                        packet += list_send_info.get(i).getPRINT_STORE_CODE() + "::";
                        packet += list_send_info.get(i).getPRINT_ITEM_NAME() + "::";
                        packet += list_send_info.get(i).getPRINT_POINT_NAME() + "::";
                        packet += list_send_info.get(i).getPRINT_CENTER_NAME() + "##";

                        if (Common.D) {
                            Log.d(TAG, "Send Packet : '" + packet + "'");
                        }

                        Log.i(TAG, "=====================여기 들어오는지 확인==================");

                        Log.i(TAG, "=====================Common.searchType==================" + Common.searchType);
                    }// "F이면" 끝
                }//for문 끝
                //새 로직 여기다가 넣어야 될 듯
                Log.i(TAG, "===================send packet 확인==================" + packet);
                boolean sendOrNot = true;

                if(packet ==""){

                    sendOrNot = false;
                }

                if(sendOrNot){
                    //전문전송.. 마트별 전송 URL (생산·생산라벨 _production / 이마트 비정량·홈플러스 비정량·도매 _new)
                    Log.i(TAG, "===================send packet 확인==================" + packet);
                    result = HttpHelper.getInstance().sendDataDb(packet, "inno", "goodswet_insert", mode.getSendUrl());
                }else{
                    result = "af"; //already finish
                }

                //결과값의 앞, 뒤에 공백 제거
                result = result.replace("\r\n", "");
                result = result.replace("\n", "");
                Log.v(TAG, "전송결과 : " + result); // s : success

                //s : 성공, f : 실패
                //list_send_info : PDA 계근데이터
                //arSM : 출하대상
                for (int i = 0; i < list_send_info.size(); i++) { //계근데이터 루프돌면서
                    if (list_send_info.get(i).getSAVE_TYPE().equals("F")) {
                        if (result.equals("s")) {
                            boolean bool = DBHandler.updatequeryGoodsWet(mContext, list_send_info.get(i).getGI_D_ID(), list_send_info.get(i).getBARCODE(), list_send_info.get(i).getBOX_CNT(), list_send_info.get(i).getGI_L_ID()); //PDA 계근테이블 SAVE TYPE Y로 업데이트
                            Log.d(TAG, "boolean " + bool);
                            if (bool) {          // 전송 & PDA SQLite update 성공
                                publishProgress("progress", Integer.toString(iCount), Integer.toString(iCount) + "번 데이터 전송성공..");
                                for (int j = 0; j < arSM.size(); j++) { //출하대상루프(GI_D_ID별 1 ROW)
                                    if (arSM.get(j).getGI_D_ID().equals(list_send_info.get(i).getGI_D_ID())
                                            && arSM.get(j).getGI_L_ID().equals(list_send_info.get(i).getGI_L_ID())) { //출하대상 루프의 GI_D_ID와 계근데이터의 GI_D_ID가 같으면
                                        arSM.get(j).setSAVE_CNT(arSM.get(j).getSAVE_CNT() + 1); //출하대상 데이터에 SAVE_CNT(저장갯수) 데이터 저장

                                        if (arSM.get(j).getSAVE_CNT() == Integer.parseInt(arSM.get(j).getGI_REQ_PKG())) {  // 전송 개수와 출하요청 개수가 같으면
                                                Log.v(TAG, "출하대상 계근 완료");
                                                arSM.get(j).setSAVE_TYPE("Y");          // 전부 전송했다면 전송여부 Y로 변경
                                                DBHandler.updatequeryShipment(mContext, arSM.get(j).getGI_D_ID(), arSM.get(j).getPACKER_PRODUCT_CODE(), arSM.get(j).getGI_L_ID());

                                                jChk++;
                                                // 원본: 여기서 jChk == arSM.size() 이면 즉시 return "ss"
                                                // 일괄전송은 서버에 이미 전부 적재되므로 남은 계근 행도 Y 처리해야 재전송 중복이 없어 루프 종료 후 판정 (개발/74)
                                        }
                                    }
                                }
                            }
                        } else if (result.equals("f")) {
                            return result;
                        }//result "s이면" 끝
                    }
                }

                // jChk > 0 : 출하대상이 없을 때(0 == 0) 기존처럼 result 를 반환하도록 유지
                if (jChk > 0 && jChk == arSM.size()) {
                    Log.d(TAG, "arSM.size() when return: " + arSM.size());
                    Log.d(TAG, "jChk number when return: " + jChk);
                    return "ss";
                }
            }
            return result;
        } catch (Exception ex) {
            Log.e(TAG, "======== ProgressDlgShipmentSend doInBackgounrd Exception ========");
            Log.e(TAG, ex.toString());
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String _result) {
        host.onSendPostExecute(_result);
        super.onPostExecute(_result);
    }
}
