package com.rgbsolution.highland_emart.shipment.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.rgbsolution.highland_emart.R;
import com.rgbsolution.highland_emart.adapter.DetailAdapter;
import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.db.DBHandler;
import com.rgbsolution.highland_emart.items.Goodswets_Info;
import com.rgbsolution.highland_emart.items.Shipments_Info;
import com.rgbsolution.highland_emart.print.LabelPrintHelper;
import com.rgbsolution.highland_emart.print.label.SumLabel;

import java.util.ArrayList;

/**
 * 계근 상세 팝업 (목록·삭제·합산) (개발/77 Step 7)
 * BixolonShipmentActivity.show_wetDetailDialog / deleteQuestionDialog / refresh_delete 를 그대로 이동.
 * 팝업 전용 필드(detail_*, detailAdapter, list_gi_info)는 이 클래스가 갖고, Activity 상태는 Host 로 주고받는다.
 */
public class WetDetailDialog {

    private static final String TAG = "BixolonShipmentActivity";   // 기존 로그 태그 유지

    public interface Host {
        void setDialogFlag(boolean flag);           // dialog_flag
        void onDetailBack();                        // 뒤로가기: 체크 해제·입력창 비움·work_flag 별 재조회
        void vibrate(long ms);                      // vibrator.vibrate
        void disableSendButton();                   // btn_send 비활성 + 배경
        Shipments_Info getSelectedShipment();       // arSM.get(select_position)
        Handler getHandler();                       // mHandler (DetailAdapter 재출력 등)
    }

    private Context mContext;
    private Host host;
    private LabelPrintHelper labelPrintHelper;
    private LabelPrintHelper.PrinterCallback printerCallback;

    /*
        계근 상세내역 팝업 필드
     */
    private EditText detail_edit_count;

    private EditText detail_edit_weight;

    private DetailAdapter detailAdapter;

    private Button detail_btn_back;

    private ArrayList<Goodswets_Info> list_gi_info;

    public WetDetailDialog(Context context, Host host, LabelPrintHelper labelPrintHelper, LabelPrintHelper.PrinterCallback printerCallback) {
        mContext = context;
        this.host = host;
        this.labelPrintHelper = labelPrintHelper;
        this.printerCallback = printerCallback;
    }

    public void refresh_delete(String delete_weight) {
        Log.d(TAG, "삭제되는 계근대상의 중량 : " + delete_weight);
        host.getSelectedShipment().setPACKING_QTY(host.getSelectedShipment().getPACKING_QTY() - 1);
        double temp = host.getSelectedShipment().getGI_QTY() - Double.parseDouble(delete_weight);
        temp = Math.round(temp * 100) / 100.0;
        host.getSelectedShipment().setGI_QTY(temp);

        detail_edit_count.setText(host.getSelectedShipment().getGI_REQ_PKG() + " / " + host.getSelectedShipment().getPACKING_QTY());
        detail_edit_weight.setText(host.getSelectedShipment().getGI_REQ_QTY() + " / " + host.getSelectedShipment().getGI_QTY());
    }

    public void show(Shipments_Info si, LayoutInflater Inflater) {
        try {
            host.setDialogFlag(true);
            View detail_layout = Inflater.inflate(R.layout.dialog_detailshipment, null);

            EditText detail_edit_position_name = (EditText) detail_layout.findViewById(R.id.detail_edit_position);
            EditText detail_edit_ppname = (EditText) detail_layout.findViewById(R.id.detail_edit_ppname);
            EditText detail_edit_ppcode = (EditText) detail_layout.findViewById(R.id.detail_edit_ppcode);
            detail_edit_count = (EditText) detail_layout.findViewById(R.id.detail_edit_count);
            detail_edit_weight = (EditText) detail_layout.findViewById(R.id.detail_edit_weight);

            detail_btn_back = (Button) detail_layout.findViewById(R.id.detail_btn_back);
            final Button detail_btn_delete = (Button) detail_layout.findViewById(R.id.detail_btn_select);
            final Button detail_btn_sum = (Button) detail_layout.findViewById(R.id.detail_btn_sum);

            detail_edit_position_name.setText(si.getCLIENTNAME());
            detail_edit_ppname.setText(si.getITEM_NAME());
            detail_edit_ppcode.setText(si.getPACKER_PRODUCT_CODE());
            detail_edit_count.setText(si.getGI_REQ_PKG() + " / " + si.getPACKING_QTY());
            detail_edit_weight.setText(si.getGI_REQ_QTY() + " / " + si.getGI_QTY());

            final AlertDialog.Builder dlog = new AlertDialog.Builder(mContext, R.style.AppCompatDialogStyle)
                    .setCancelable(false);
            dlog.setView(detail_layout);
            final AlertDialog detail_dialog = dlog.create();
            detail_dialog.show();

            detail_btn_back.setOnClickListener(v -> {
                detail_dialog.dismiss();
                host.setDialogFlag(false);
                host.onDetailBack();
            });

            detail_btn_delete.setOnClickListener(v -> {
                // 선택된 Items 삭제
                try {
                    if (list_gi_info.size() == 0) {
                        Toast.makeText(mContext.getApplicationContext(), "삭제할 항목이 없습니다.", Toast.LENGTH_SHORT).show();
                        host.vibrate(1000);
                    } else if (list_gi_info.size() > 0) {
                        ArrayList<Goodswets_Info> list_delete = new ArrayList<Goodswets_Info>();
                        for (int i = 0; i < detailAdapter.cbStatus.size(); i++) {
                            if (detailAdapter.cbStatus.get(i))
                                list_delete.add(list_gi_info.get(i));
                        }
                        if (list_delete.size() > 0) {
                            deleteQuestionDialog(host.getSelectedShipment(), list_delete);
                        } else {
                            Toast.makeText(mContext.getApplicationContext(), "삭제할 항목을 선택하세요.", Toast.LENGTH_SHORT).show();
                            host.vibrate(1000);
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "==== detail_btn_delete Exception ====");
                    Log.e(TAG, ex.getMessage().toString());
                }
            });

            detail_btn_sum.setOnClickListener(v -> {
                // 전체 리스트 합
                try {
                    if (list_gi_info.size() == 0) {
                        Toast.makeText(mContext.getApplicationContext(), "합산할 항목이 없습니다.", Toast.LENGTH_SHORT).show();
                        host.vibrate(1000);
                    } else if (list_gi_info.size() > 0) {
                        // 파렛트당 박스 수 입력 (개발 79, 수정 리스트 C2)
                        // 기본값 36 = 원본 36건(6열 x 6행) 단위와 동일 출력
                        final EditText edit_pallet_cnt = new EditText(mContext);
                        edit_pallet_cnt.setInputType(InputType.TYPE_CLASS_NUMBER);
                        edit_pallet_cnt.setText("36");
                        edit_pallet_cnt.setSelectAllOnFocus(true);

                        new AlertDialog.Builder(mContext, R.style.AppCompatDialogStyle)
                                .setIcon(R.drawable.highland)
                                .setTitle("합산 라벨")
                                .setMessage("파렛트당 박스 수를 입력하세요.")
                                .setView(edit_pallet_cnt)
                                .setPositiveButton("출력", (dialog, which) -> {
                                    int pallet_box_cnt = 0;
                                    try {
                                        pallet_box_cnt = Integer.parseInt(edit_pallet_cnt.getText().toString().trim());
                                    } catch (NumberFormatException e) {
                                        pallet_box_cnt = 0;
                                    }
                                    if (pallet_box_cnt <= 0) {
                                        Toast.makeText(mContext.getApplicationContext(), "파렛트당 박스 수를 입력하세요.", Toast.LENGTH_SHORT).show();
                                        host.vibrate(1000);
                                        return;
                                    }
                                    SumLabel.print(labelPrintHelper, list_gi_info, pallet_box_cnt, printerCallback);   // 개발/77 Step 1
                                }).setNegativeButton("취소", null)
                                .show();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "==== detail_btn_sum Exception ====");
                    Log.e(TAG, ex.getMessage().toString());
                }
            });

            list_gi_info = DBHandler.selectqueryGoodsWet(mContext, si.getGI_D_ID(), si.getPACKER_PRODUCT_CODE(), si.getCLIENT_CODE(), si.getGI_L_ID());
            detailAdapter = new DetailAdapter(mContext, R.layout.list_detailshipment, list_gi_info, host.getHandler());

            ListView detail_list = (ListView) detail_layout.findViewById(R.id.detail_list);
            detail_list.setAdapter(detailAdapter);
            detailAdapter.notifyDataSetChanged();
        } catch (Exception ex) {
            Log.e(TAG, "======== show_wetDetailDialog Exception ========");
            Log.e(TAG, ex.getMessage().toString());
        }
    }

    //	계근상품 삭제 Dialog
    public void deleteQuestionDialog(final Shipments_Info si, final ArrayList<Goodswets_Info> list_delete) {
        String alertTitle = "계근상품 삭제";
        String buttonMessage = "정말 삭제하시겠습니까?";
        String buttonYes = "삭제";
        String buttonNo = "취소";

        new AlertDialog.Builder(mContext, R.style.AppCompatDialogStyle)
                .setIcon(R.drawable.highland)
                .setTitle(alertTitle)
                .setMessage(buttonMessage)
                .setCancelable(false)
                .setPositiveButton(buttonYes, (dialog, which) -> {
                    try {
                        if (Common.D) {
                            Log.d(TAG, "삭제할 계근상품 수 : " + list_delete.size());
                        }
                        for (int i = list_delete.size()-1; i >= 0; i--) {
                            // List 삭제 & SQLite 삭제
                            Log.i(TAG, "삭제 i count : " + i);
                            Log.i(TAG, "삭제 row position : " + list_delete.get(i).getBOX_CNT());
                            String delete_box = list_delete.get(i).getBOX_CNT();

                            DBHandler.deletequerySelectGoodsWet(mContext.getApplicationContext(),
                                    list_delete.get(i).getGI_D_ID(), list_delete.get(i).getBARCODE(), Integer.parseInt(delete_box), list_delete.get(i).getGI_L_ID());
                            refresh_delete(list_delete.get(i).getWEIGHT());

                        /*    int removeindex = Integer.parseInt(delete_box)-1;
                            detailAdapter.remove(removeindex);*/
                        }
                        /*detailAdapter.notifyDataSetChanged();*/

                        host.vibrate(500);
                        host.disableSendButton();

                        detail_btn_back.performClick();

                        if (Common.D) {
                            Log.d(TAG, "계근 선택항목 삭제 성공 !");
                        }
                        Toast.makeText(mContext.getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();

                    } catch (Exception ex) {
                        if (Common.D) {
                            Log.d(TAG, "계근 선택항목 삭제 실패 -> " + ex.getMessage().toString());
                        }
                        Toast.makeText(mContext.getApplicationContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(buttonNo, null)
                .show();
    }
}
