package com.rgbsolution.highland_emart.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rgbsolution.highland_emart.R;
import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Goodswets_Info;

import java.util.ArrayList;

public class DetailAdapter extends BaseAdapter {

    private static final String TAG = "DetailAdapter";

    public static final int MESSAGE_REPRINT = 5;

    private Context mContext;
    private int layout;
    public ArrayList<Goodswets_Info> arSrc;
    private Handler mHandler;

    public ArrayList<Boolean> cbStatus = new ArrayList<Boolean>();
    LayoutInflater Inflater;

    public DetailAdapter(Context _context, int _layout, ArrayList<Goodswets_Info> _arSrc, Handler _handler) {
        try {
            mContext = _context;
            layout = _layout;
            arSrc = _arSrc;
            Inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mHandler = _handler;

            if (Common.D) {
                Log.d(TAG, "arSrc 의 갯수 : " + arSrc.size());
            }
            for (int i = 0; i < arSrc.size(); i++) {
                cbStatus.add(false);
            }
        } catch (Exception ex) {
            Log.e(TAG, "========= detailAdapter init fail =========");
            Log.e(TAG, ex.getMessage().toString());
        }
    }

    @Override
    public int getCount() {
        return arSrc.size();
    }

    @Override
    public Object getItem(int position) {
        return arSrc.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void remove(int position) {
        try {
            cbStatus.remove(arSrc.size() - position);
            arSrc.remove(arSrc.size() - position);

            for (int i = 0; i < arSrc.size(); i++) {
                if (Integer.parseInt(arSrc.get(i).getBOX_CNT()) > position) {
                    arSrc.get(i).setBOX_CNT(String.valueOf((Integer.parseInt(arSrc.get(i).getBOX_CNT()) - 1)));
                }
            }

            if (Common.D) {
                Log.e(TAG, "============");
                Log.e(TAG, "Call remove");
                Log.e(TAG, "============");
                Log.d(TAG, "현재 Item갯수 : " + arSrc.size() + "개");
            }
        } catch (Exception ex) {
            Log.e(TAG, "======== remove Exception ========");
            Log.e(TAG, ex.getMessage().toString());
        }
    }

    //	화면에 보여지는 행을 그려주기위한 getView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final ViewHolder holder;
        final ViewGroup mParent = parent;
        final View mConvertView = convertView;

//		행 재사용을 위해 viewHolder 사용
        if (convertView == null) {
            //Log.d(TAG, "convertView Inflate");
            //Log.d(TAG, "position : " + pos);
            convertView = Inflater.inflate(layout, parent, false);
            holder = new ViewHolder();
            holder.no = (TextView) convertView.findViewById(R.id.no);
            holder.boxserial = (TextView) convertView.findViewById(R.id.boxserial);
            holder.ppcode = (TextView) convertView.findViewById(R.id.ppcode);
            holder.weight = (TextView) convertView.findViewById(R.id.weight);
            holder.makingdate = (TextView) convertView.findViewById(R.id.makingdate);
            holder.print = (Button) convertView.findViewById(R.id.reprint);
            convertView.setTag(holder);
        } else {
            //Log.d(TAG, "convertView not null, pos : " + pos);
            holder = (ViewHolder) convertView.getTag();
        }

//		row 선택 시 체크박스 변경
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Common.D) {
                    Log.d(TAG, pos + "번째 convertView Click !");
                }

                if (arSrc.get(pos).getSAVE_TYPE().equals("Y")) {
                    Toast.makeText(mContext, "전송이 완료된 상품은\n삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cbStatus.get(pos))
                    cbStatus.set(pos, false);
                else
                    cbStatus.set(pos, true);
                notifyDataSetChanged();
            }
        });

        if (!Common.printer_setting || !Common.print_bool) {
            holder.print.setEnabled(false);
        }

        holder.print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("WEIGHT", arSrc.get(pos).getWEIGHT());
                bundle.putString("MAKINGDATE", arSrc.get(pos).getMAKINGDATE());
                bundle.putString("BOX_ORDER", arSrc.get(pos).getBOX_ORDER());
                msg.setData(bundle);
                msg.what = MESSAGE_REPRINT;
                mHandler.sendMessage(msg);
            }
        });

        if ((pos % 2) != 0) {
            convertView.setBackgroundColor(Color.parseColor("#BBDEFB"));
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }


//		계근완료 여부 확인 및 색상 변경
        if (arSrc.get(pos).getSAVE_TYPE().equals("Y")) {
            convertView.setBackgroundColor(Color.parseColor("#FFF9C4"));
        }

        if (cbStatus.get(pos)) {
            convertView.setBackgroundColor(Color.parseColor("#80CBC4"));
        }

//		row별 데이터 출력
        holder.no.setText(arSrc.get(pos).getBOX_CNT());
        holder.boxserial.setText(arSrc.get(pos).getBOXSERIAL());
        holder.ppcode.setText(arSrc.get(pos).getPACKER_PRODUCT_CODE());
        holder.weight.setText(arSrc.get(pos).getWEIGHT());
        holder.makingdate.setText(arSrc.get(pos).getMAKINGDATE());
        holder.print.setText("출력");

        return convertView;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView no;
        TextView boxserial;
        TextView ppcode;
        TextView weight;
        TextView makingdate;
        Button print;
    }
}
