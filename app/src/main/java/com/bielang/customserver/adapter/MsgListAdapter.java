package com.bielang.customserver.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aitsuki.swipe.SwipeItemLayout;
import com.bielang.customserver.MyApplication;
import com.bielang.customserver.R;
import com.bielang.customserver.bean.ChatMessage;
import com.bielang.customserver.emotion.SpanStringUtils;
import com.bielang.customserver.bean.MsgList;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.bielang.customserver.DataName.EMOTION_CLASSIC_TYPE;

/**
 *
 * Created by Daylight on 2017/7/6.
 */

public class MsgListAdapter extends RecyclerView.Adapter<MsgListAdapter.MyViewHolder>{
    private Context mContext;
    private ArrayList<MsgList> mData;
    private ItemTouchListener mItemTouchListener;
    private Realm realm;
    public MsgListAdapter(Context context,ArrayList<MsgList> data,ItemTouchListener itemTouchListener)
    {
        this.mContext=context;
        this.mData=data;
        this.mItemTouchListener = itemTouchListener;
        realm=Realm.getDefaultInstance();
    }
    public void keepMsgData(){
        realm.beginTransaction();
        for (int i=0;i<mData.size();i++){
            realm.copyToRealmOrUpdate(mData.get(i));
        }
        realm.commitTransaction();
    }
    public void Refresh()
    {
        for (int i = 0; i < mData.size(); i++) {
            RealmResults<ChatMessage> cm = realm.where(ChatMessage.class)
                    .equalTo("csId", MyApplication.getInstance().getMyInfo().getId())
                    .equalTo("mId", mData.get(i).getId()).findAll();
            if (cm.size()!=0) {
                if (cm.get(cm.size()-1).getType()==ChatMessage.MessageType_Goods)
                    mData.get(i).setLastMsg("已推荐商品");
                else
                    mData.get(i).setLastMsg(cm.get(cm.size()-1).getContent());
                mData.get(i).setLastTime(cm.get(cm.size()-1).getDate());
            }
        }
        Collections.sort(mData, new Comparator<MsgList>() {
            @Override
            public int compare(MsgList msgList, MsgList t1) {
                if (msgList.getDate().before(t1.getDate()))
                    return 1;
                else if (msgList.getDate().after(t1.getDate()))
                    return -1;
                return 0;
            }
        });
        this.notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msglist,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.itemView.setTag(position);
        if (mItemTouchListener!=null) {
            holder.msgItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemTouchListener.onItemClick(holder.itemView, position);
                }
            });
            holder.msgItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mItemTouchListener.onLongClick(holder.itemView,position);
                    return false;
                }
            });
            if (holder.rightMenuDelete!=null){
                holder.rightMenuDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mItemTouchListener.onRightMenuDeleteClick(holder.itemView,position);
                        holder.mSwipeItemLayout.close();
                    }
                });
            }
            if (holder.rightMenuEndChat!=null){
                holder.rightMenuEndChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mItemTouchListener.onRightMenuEndChatClick(holder.itemView,position);
                        holder.mSwipeItemLayout.close();
                    }
                });
            }
        }
        Glide.with(mContext).load(mData.get(position).getHeader()).error(R.drawable.pic_sul1).into(holder.Head);
        holder.Name.setText(mData.get(position).getName());
        holder.LastMsg.setText(SpanStringUtils.getEmotionContent(EMOTION_CLASSIC_TYPE,
                mContext, holder.LastMsg, mData.get(position).getLastMsg()));
        holder.LastTime.setText(mData.get(position).getLastTime());
        if (mData.get(position).getNewMsgNumber()!=0) {
            holder.MsgNumber.setVisibility(View.VISIBLE);
            holder.MsgNumber.setText(String.valueOf(mData.get(position).getNewMsgNumber()));
        }else
            holder.MsgNumber.setVisibility(View.INVISIBLE);
        RealmResults<ChatMessage> results = realm.where(ChatMessage.class).equalTo("csId",MyApplication.getInstance().getMyInfo().getId())
                .equalTo("mId", mData.get(position).getId())
                .equalTo("mType", ChatMessage.MessageType_End).findAll();
        if (results.size() == 0)
            holder.label.setVisibility(View.VISIBLE);
        else
            holder.label.setVisibility(View.GONE);
    }

    @Override
    public long getItemId(int Index)
    {
        return Index;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public interface ItemTouchListener {
        void onItemClick(View view , int position);

        void onRightMenuDeleteClick(View view , int position);

        void onRightMenuEndChatClick(View view,int position);

        void onLongClick(View view,int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView Head;
        private TextView Name;
        private TextView LastMsg;
        private TextView LastTime;
        private TextView MsgNumber;
        private SwipeItemLayout mSwipeItemLayout;
        private TextView rightMenuDelete;
        private TextView rightMenuEndChat;
        private LinearLayout msgItem;
        private TextView label;
        MyViewHolder(View view){
            super(view);
            Head=view.findViewById(R.id.msgList_headImage);
            Name=view.findViewById(R.id.msgList_name);
            LastMsg=view.findViewById(R.id.msgList_msg);
            LastTime=view.findViewById(R.id.msgList_time);
            MsgNumber = view.findViewById(R.id.msgList_number);
            mSwipeItemLayout = view.findViewById(R.id.swipe_layout);
            rightMenuDelete=view.findViewById(R.id.right_menu_delete);
            rightMenuEndChat=view.findViewById(R.id.right_menu_end_chat);
            msgItem=view.findViewById(R.id.msgList_item);
            label=view.findViewById(R.id.msgList_label);
        }
    }
}
