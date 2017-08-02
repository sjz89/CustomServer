package com.bielang.customserver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bielang.customserver.R;

import java.util.ArrayList;


public class QuickReplyAdapter extends BaseAdapter {
    private ArrayList<String> mData;
    private Context mContext;
    public QuickReplyAdapter(Context context,ArrayList<String> data){
        this.mContext=context;
        this.mData=data;
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MyViewHolder viewHolder;
        if (view==null){
            view= LayoutInflater.from(mContext).inflate(R.layout.item_quick_reply,null);
            viewHolder=new MyViewHolder(view);
            view.setTag(viewHolder);
        }else
            viewHolder=(MyViewHolder)view.getTag();
        viewHolder.text.setText(mData.get(i));
        return view;
    }
    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        private MyViewHolder(View itemView) {
            super(itemView);
            text=itemView.findViewById(R.id.quick_reply_msg);
        }
    }
}
