package com.rgbsolution.highland_emart.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rgbsolution.highland_emart.R;
import com.rgbsolution.highland_emart.common.Common;
import com.rgbsolution.highland_emart.items.Shipments_Info;

import java.util.ArrayList;


public class ShipmentListAdapter extends BaseAdapter {

    private static final String TAG = "ShipmentListAdapter";
    private Context mContext;
    private int layout;
    public ArrayList<Shipments_Info> arSrc;

    public ArrayList<Boolean> cbStatus = new ArrayList<Boolean>();
    LayoutInflater Inflater;

    public ShipmentListAdapter(Context _context, int _layout, ArrayList<Shipments_Info> _arSrc, Handler _handler) {
        mContext = _context;
        layout = _layout;
        arSrc = _arSrc;
        Inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (Common.D) {
            Log.d(TAG, "arSrc 의 갯수 : " + arSrc.size());
        }
        for (int i = 0; i < arSrc.size(); i++) {
            cbStatus.add(false);
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
        arSrc.remove(position);
        cbStatus.remove(position);
        if (Common.D) {
            Log.e(TAG, "============");
            Log.e(TAG, "Call remove");
            Log.e(TAG, "============");
            Log.d(TAG, "현재 Item갯수 : " + arSrc.size() + "개");
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
            holder.position = (TextView) convertView.findViewById(R.id.position);
            holder.count = (TextView) convertView.findViewById(R.id.count);
            holder.weight = (TextView) convertView.findViewById(R.id.weight);
            holder.bl = (TextView) convertView.findViewById(R.id.bl);
            convertView.setTag(holder);
        } else {
            //Log.d(TAG, "convertView not null, pos : " + pos);
            holder = (ViewHolder) convertView.getTag();
        }

        if ((pos % 2) != 0) {
            convertView.setBackgroundColor(Color.parseColor("#BBDEFB"));
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }

        if (arSrc.get(pos).isWORK_FLAG() == 1) {
            convertView.setBackgroundColor(Color.parseColor("#FFA726"));
        }

//		row 선택 시 체크박스 변경
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Common.D) {
                    Log.d(TAG, pos + "번째 convertView Click !");
                }

                if (cbStatus.get(pos)) {
                    cbStatus.set(pos, false);
                    notifyDataSetChanged();
                } else {

                    for (int i = 0; i < cbStatus.size(); i++) {
                        cbStatus.set(i, false);
                    }
                    cbStatus.set(pos, true);
                    notifyDataSetChanged();
                }
            }
        });

//		계근완료 여부 확인 및 색상 변경
        if (arSrc.get(pos).getSAVE_TYPE().equals("Y")) {
            convertView.setBackgroundColor(Color.parseColor("#FFF9C4"));
        }

        if (cbStatus.get(pos)) {
            convertView.setBackgroundColor(Color.parseColor("#80CBC4"));
        }

        try{
            //		row별 데이터 출력
            holder.no.setText(String.valueOf(pos + 1));
            holder.position.setText(arSrc.get(pos).getCLIENTNAME());
            holder.count.setText(arSrc.get(pos).getGI_REQ_PKG() + "/" + arSrc.get(pos).getPACKING_QTY());
            if(Common.searchType.equals("3")){
                holder.weight.setText(""+ arSrc.get(pos).getGI_QTY());
            }else{
                holder.weight.setText(arSrc.get(pos).getGI_REQ_QTY() + "/" + arSrc.get(pos).getGI_QTY());
            }

            if(arSrc.get(pos).getBL_NO().equals("")){
                holder.bl.setText("");
            }else{
                holder.bl.setText(arSrc.get(pos).getBL_NO().substring(arSrc.get(pos).getBL_NO().length() - 4, arSrc.get(pos).getBL_NO().length()));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return convertView;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView no;
        TextView position;
        TextView count;
        TextView weight;
        TextView bl;
    }
}
