package com.bielang.customserver.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bielang.customserver.bean.LeaveMessage;
import com.bielang.customserver.R;
import com.bielang.customserver.bean.ReplyMsg;
import com.bielang.customserver.emotion.SpanStringUtils;
import com.bielang.customserver.util.DateUtil;
import com.bielang.customserver.view.NestFullListView;
import com.bielang.customserver.view.NestFullViewHolder;

import java.util.ArrayList;

import io.realm.Realm;

import static com.bielang.customserver.DataName.EMOTION_CLASSIC_TYPE;

public class LeaveMsgAdapter extends RecyclerView.Adapter<LeaveMsgAdapter.MyViewHolder> implements View.OnClickListener,View.OnLongClickListener{
    private ArrayList<LeaveMessage> mData;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener = null;
    private OnItemLongListener mOnItemLong = null;
    public LeaveMsgAdapter(Context context,ArrayList<LeaveMessage> data){
        this.mContext=context;
        this.mData=data;
    }

    public void Refresh(){
        Realm realm=Realm.getDefaultInstance();
        realm.beginTransaction();
        for (LeaveMessage data:mData){
            realm.copyToRealmOrUpdate(data);
        }
        realm.commitTransaction();
        realm.close();
        this.notifyDataSetChanged();
    }

    @Override
    public LeaveMsgAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leave_message,parent,false);
        view.setOnLongClickListener(this);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.itemView.setTag(position);
        holder.leave_msg_name.setText(mData.get(position).getName());
        holder.leave_msg_text.setText(mData.get(position).getMessage());
        holder.leave_msg_time.setText(DateUtil.getTime(mData.get(position).getDate(),4));
        holder.reply_msgList.setAdapter(new NestFullListViewAdapter<ReplyMsg>(R.layout.item_reply_msg,mData.get(position).getReply_msg()) {
            @Override
            public void onBind(int pos, ReplyMsg s, NestFullViewHolder mHolder) {
                mHolder.setText(R.id.reply_msg, SpanStringUtils.getEmotionContent(EMOTION_CLASSIC_TYPE,
                        mContext,(TextView)mHolder.getView(R.id.reply_msg),s.getMsg()));
            }
        });
        if (mOnItemClickListener!=null){
            holder.leave_msg_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(holder.leave_msg_reply,position);
                }
            });
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

    public void setOnItemLongClickListener(OnItemLongListener listener){
        this.mOnItemLong =  listener;
    }

    @Override
    public boolean onLongClick(View view) {
        if(mOnItemLong != null){
            mOnItemLong.onItemLongClick(view,(int)view.getTag());
        }
        return true;
    }

    public interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public interface OnItemLongListener{
        void onItemLongClick(View view,int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView leave_msg_header;
        TextView leave_msg_name;
        TextView leave_msg_text;
        TextView leave_msg_time;
        ImageView leave_msg_reply;
        NestFullListView reply_msgList;
        MyViewHolder(View itemView) {
            super(itemView);
            leave_msg_name=itemView.findViewById(R.id.leave_msg_name);
            leave_msg_header=itemView.findViewById(R.id.leave_msg_header);
            leave_msg_text=itemView.findViewById(R.id.leave_msg_text);
            leave_msg_time=itemView.findViewById(R.id.leave_msg_time);
            leave_msg_reply=itemView.findViewById(R.id.leave_msg_reply);
            reply_msgList=itemView.findViewById(R.id.reply_msgList);
        }
    }
}