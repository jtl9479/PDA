package com.rgbsolution.highland_emart.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rgbsolution.highland_emart.R;
import com.rgbsolution.highland_emart.common.Common;

import java.util.ArrayList;

public class UnknownAdapter extends BaseAdapter {

    private static final String TAG = "UnknownAdapter";

    private Context mContext;
    private int layout;
    public ArrayList<String[]> arSrc;
    LayoutInflater Inflater;

    public UnknownAdapter(Context _context, int _layout, ArrayList<String[]> _arSrc) {
        mContext = _context;
        layout = _layout;
        arSrc = _arSrc;
        Inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        if (Common.D) {
            Log.d(TAG, ":: sAdapter remove ::");
        }
        arSrc.remove(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final int pos = position;
        final ViewHolder holder;
        final ViewGroup mParent = parent;

        if (convertView == null) {
            if (Common.D) {
                Log.d(TAG, "convertView Inflate");
                Log.d(TAG, "position : " + pos);
            }
            convertView = Inflater.inflate(layout, parent, false);
            holder = new ViewHolder();
            holder.ppcode = (TextView) convertView.findViewById(R.id.ppcode);
            holder.ppname = (TextView) convertView.findViewById(R.id.ppname);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if ((pos % 2) != 0) {
            convertView.setBackgroundColor(Color.parseColor("#BBDEFB"));
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }

        holder.ppcode.setText(arSrc.get(pos)[0]);
        holder.ppname.setText(arSrc.get(pos)[1]);

        return convertView;
    }

    static class ViewHolder {
        TextView ppcode;
        TextView ppname;
    }

}
