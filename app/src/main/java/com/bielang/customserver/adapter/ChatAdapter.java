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

import com.bielang.customserver.bean.ChatMessage;
import com.bielang.customserver.MyApplication;
import com.bielang.customserver.util.DateUtil;
import com.bielang.customserver.util.HttpPost;
import com.bielang.customserver.view.BubbleTextView;
import com.bielang.customserver.R;
import com.bielang.customserver.emotion.SpanStringUtils;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.bielang.customserver.DataName.EMOTION_CLASSIC_TYPE;


public class ChatAdapter extends RecyclerView.Adapter implements View.OnClickListener{

    private Context mContext;
    private ArrayList<ChatMessage> mData;
    private int Header_From;
    private OnItemClickListener mOnItemClickListener = null;

    public ChatAdapter(Context context, ArrayList<ChatMessage> data, int header_From) {
        this.mContext = context;
        this.mData = data;
        this.Header_From = header_From;
    }
    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == ChatMessage.MessageType_From) {
            view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_get_msg, parent, false);
            return new FromViewHolder(view);
        } else if (viewType == ChatMessage.MessageType_To) {
            view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_send_msg, parent, false);
            return new ToViewHolder(view);
        } else if (viewType==ChatMessage.MessageType_Goods){
            view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goods,parent,false);
            return new GoodsViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_hint, parent, false);
            return new HintViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.itemView.setTag(position);
        if (holder instanceof HintViewHolder) {
            if (mData.get(position).getContent().equals("客户接入"))
                ((HintViewHolder) holder).Content_hint.setVisibility(View.GONE);
            else if (position!=0&&mData.get(position).getType()==ChatMessage.MessageType_End)
                ((HintViewHolder) holder).Content_hint.setText("——历史记录——");
            else
                ((HintViewHolder) holder).Content_hint.setText(mData.get(position).getContent());
        } else if (holder instanceof FromViewHolder) {
            if (position==0)
                ((FromViewHolder)holder).get_time.setText(DateUtil.AutoTransFormat(mData.get(position).getDate()));
            else {
                if (mData.get(position-1).getType()==ChatMessage.MessageType_Hint
                        ||mData.get(position).getDate().getTime() - mData.get(position - 1).getDate().getTime() >= 30000)
                    ((FromViewHolder) holder).get_time.setText(DateUtil.AutoTransFormat(mData.get(position).getDate()));
                else
                    ((FromViewHolder) holder).get_time.setVisibility(View.GONE);
            }
            Glide.with(MyApplication.getInstance()).load(Header_From).into(((FromViewHolder) holder).Head_from);
            ((FromViewHolder) holder).Content_from.setText( SpanStringUtils.getEmotionContent
                    (EMOTION_CLASSIC_TYPE,mContext,((FromViewHolder) holder).Content_from,mData.get(position).getContent()));
            if (mOnItemClickListener!=null){
                ((FromViewHolder) holder).Content_from.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        mOnItemClickListener.onTextClick(((FromViewHolder)holder).Content_from,position);
                        return false;
                    }
                });
            }
        } else if (holder instanceof ToViewHolder) {
            if (position==0)
                ((ToViewHolder)holder).send_time.setText(DateUtil.AutoTransFormat(mData.get(position).getDate()));
            else {
                if (mData.get(position-1).getType()==ChatMessage.MessageType_Hint
                        ||mData.get(position).getDate().getTime() - mData.get(position - 1).getDate().getTime() >= 30000)
                    ((ToViewHolder) holder).send_time.setText(DateUtil.AutoTransFormat(mData.get(position).getDate()));
                else
                    ((ToViewHolder) holder).send_time.setVisibility(View.GONE);
            }
            Glide.with(MyApplication.getInstance()).load(MyApplication.getInstance().getMyInfo().getHeader())
                    .error(R.drawable.pic_sul2).into(((ToViewHolder) holder).Head_to);
            ((ToViewHolder) holder).Content_to.setText(SpanStringUtils.getEmotionContent
                    (EMOTION_CLASSIC_TYPE,mContext,((ToViewHolder) holder).Content_to,mData.get(position).getContent()));
            if (mOnItemClickListener!=null){
                ((ToViewHolder) holder).Content_to.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        mOnItemClickListener.onTextClick(((ToViewHolder)holder).Content_to,position);
                        return false;
                    }
                });
            }
        } else if (holder instanceof GoodsViewHolder){
            if (position==0)
                ((GoodsViewHolder)holder).send_time.setText(DateUtil.AutoTransFormat(mData.get(position).getDate()));
            else {
                if (mData.get(position-1).getType()==ChatMessage.MessageType_Hint
                        ||mData.get(position).getDate().getTime() - mData.get(position - 1).getDate().getTime() >= 30000)
                    ((GoodsViewHolder) holder).send_time.setText(DateUtil.AutoTransFormat(mData.get(position).getDate()));
                else
                    ((GoodsViewHolder) holder).send_time.setVisibility(View.GONE);
            }
            Glide.with(MyApplication.getInstance()).load(MyApplication.getInstance().getMyInfo().getHeader())
                    .error(R.drawable.pic_sul2).into(((GoodsViewHolder) holder).Head_to);
            try {
                JSONObject jsonObject=new JSONObject(mData.get(position).getContent());
                ((GoodsViewHolder) holder).Goods_id.setText(String.valueOf("商品号："+jsonObject.getString("id")));
                ((GoodsViewHolder) holder).Goods_price.setText(String.valueOf("商品价格："+jsonObject.getString("price")));
                Glide.with(MyApplication.getInstance()).load(HttpPost.url + jsonObject.getString("pic"))
                        .placeholder(R.drawable.icon_placeholder).error(R.drawable.icon_failure).into(((GoodsViewHolder) holder).Goods_pic);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mOnItemClickListener!=null)
                ((GoodsViewHolder) holder).Btn_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemClickListener.onItemClick(((GoodsViewHolder) holder).Btn_detail,position);
                    }
                });
        }
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
        void onTextClick(View view,int position);
    }


    private class HintViewHolder extends RecyclerView.ViewHolder {
        TextView Content_hint;

        HintViewHolder(View view) {
            super(view);
            Content_hint = view.findViewById(R.id.hint);
        }
    }

    private class FromViewHolder extends RecyclerView.ViewHolder{
        TextView get_time;
        BubbleTextView Content_from;
        ImageView Head_from;

        FromViewHolder(View view) {
            super(view);
            get_time=view.findViewById(R.id.get_time);
            Content_from = view.findViewById(R.id.From_Content);
            Head_from = view.findViewById(R.id.Header_From);
        }
    }

    private class ToViewHolder extends RecyclerView.ViewHolder{
        TextView send_time;
        BubbleTextView Content_to;
        ImageView Head_to;

        ToViewHolder(View view) {
            super(view);
            send_time=view.findViewById(R.id.send_time);
            Content_to =view.findViewById(R.id.To_Content);
            Head_to = view.findViewById(R.id.Header_To);
        }
    }
    private class GoodsViewHolder extends RecyclerView.ViewHolder{
        TextView send_time;
        ImageView Head_to;
        ImageView Goods_pic;
        TextView Goods_id;
        TextView Goods_price;
        LinearLayout Btn_detail;
        GoodsViewHolder(View itemView) {
            super(itemView);
            send_time=itemView.findViewById(R.id.goods_send_time);
            Head_to=itemView.findViewById(R.id.goods_Header_To);
            Goods_pic=itemView.findViewById(R.id.pic);
            Goods_id=itemView.findViewById(R.id.idNum);
            Goods_price=itemView.findViewById(R.id.price);
            Btn_detail=itemView.findViewById(R.id.see_detail);
        }
    }
}
