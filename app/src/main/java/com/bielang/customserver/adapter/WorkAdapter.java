package com.bielang.customserver.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bielang.customserver.R;
import com.bielang.customserver.bean.WorkList;

import java.util.ArrayList;

/**
 * 工单Adapter
 * Created by Daylight on 2017/7/21.
 */

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.MyViewHolder> implements View.OnClickListener{
    private ArrayList<WorkList> mData;
    private OnItemClickListener mOnItemClickListener = null;
    public WorkAdapter(ArrayList<WorkList> mData) {
        this.mData = mData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_work,parent,false);
        view.setOnClickListener(this);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
        viewHolder.itemView.setTag(position);
        viewHolder.name.setText(String.valueOf("客户ID:"+mData.get(position).getCustomerId()));
        switch (mData.get(position).getStatus()){
            case "notfinish":
                viewHolder.state.setText("未完成");
                break;
            case "waitconfirm":
                viewHolder.state.setText("待确认");
                break;
            case "cancel":
                viewHolder.state.setText("已取消");
                break;
            case "finish":
                viewHolder.state.setText("已完成");
                break;
        }
        viewHolder.number.setText(String.valueOf("订单号："+mData.get(position).getId()));
        if (mData.get(position).getTime()!=null){
            String time=mData.get(position).getTime();
            viewHolder.time.setText(time.substring(0,time.length()-5));
        }
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    @Override
    public void onClick(View view){
        if (mOnItemClickListener!=null){
            mOnItemClickListener.onItemClick(view,(int)view.getTag());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView state;
        TextView number;
        TextView time;
        MyViewHolder(View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.work_name);
            state=itemView.findViewById(R.id.work_state);
            number=itemView.findViewById(R.id.work_number);
            time=itemView.findViewById(R.id.work_time);
        }
    }
}
