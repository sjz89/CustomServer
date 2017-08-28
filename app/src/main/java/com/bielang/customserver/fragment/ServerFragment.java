package com.bielang.customserver.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bielang.customserver.MessageService;
import com.bielang.customserver.MyApplication;
import com.bielang.customserver.R;
import com.bielang.customserver.activity.ChatActivity;
import com.bielang.customserver.adapter.MsgListAdapter;
import com.bielang.customserver.bean.ChatMessage;
import com.bielang.customserver.bean.CustomerInfo;
import com.bielang.customserver.bean.MsgList;
import com.bielang.customserver.util.HttpPost;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class ServerFragment extends Fragment {
    private TextView statement;
    private ImageView changeState;
    private MessageService.MsgBinder mBinder;
    private ArrayList<MsgList> mData;
    private MsgListAdapter mAdapter;
    private int mId;
    private int end_position;
    private Realm realm;
    private TextView customer_name;
    private TextView customer_sex;
    private TextView customer_area;
    private TextView customer_keyword;
    private AlertDialog showCustomerInfo;
    private static final int SERVICE_MSG = 0;
    private static final int WEBSOCKET_STATE = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_server,container,false);
        initView(view,inflater);
        return view;
    }
    private void initView(View view,LayoutInflater inflater){
        statement = view.findViewById(R.id.statement);
        changeState = view.findViewById(R.id.change_state);
        statement.setVisibility(View.VISIBLE);
        changeState.setVisibility(View.VISIBLE);
        statement.setText(R.string.state_online);
        View.OnClickListener StateChange = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (statement.getText().equals(getString(R.string.state_online))) {
                    statement.setText(R.string.state_hangUp);
                    mBinder.getService().disconnect();
                } else {
                    statement.setText(R.string.state_online);
                    mBinder.getService().connect();
                }
            }
        };
        statement.setOnClickListener(StateChange);
        changeState.setOnClickListener(StateChange);
        RecyclerView msg_list = view.findViewById(R.id.list_msg);
        msg_list.setLayoutManager(new LinearLayoutManager(getContext()));
        msg_list.setHasFixedSize(true);
        mData = new ArrayList<>();

        //数据库读取列表
        RealmResults<MsgList> msgLists = realm.where(MsgList.class).equalTo("csId", MyApplication.getInstance().getMyInfo().getId()).findAll();
        mData = (ArrayList<MsgList>) realm.copyFromRealm(msgLists);

        mAdapter = new MsgListAdapter(getContext(), mData, new MsgListAdapter.ItemTouchListener() {
            @Override
            public void onItemClick(View view, int position) {
                mId = mData.get(position).getId();
                mData.get(position).setNewMsgNumber(0);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("id", mId);
                intent.putExtra("title", mData.get(position).getName());
                intent.putExtra("header", mData.get(position).getHeader());
                startActivity(intent);
            }

            @Override
            public void onRightMenuDeleteClick(View view, int position) {
                realm.beginTransaction();
                RealmResults<MsgList> msgLists = realm.where(MsgList.class).equalTo("csId", MyApplication.getInstance().getMyInfo().getId()).equalTo("mId", mData.get(position).getId()).findAll();
                msgLists.deleteAllFromRealm();
                realm.commitTransaction();
                mData.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(position, mData.size());
                mAdapter.keepMsgData();
            }

            @Override
            public void onRightMenuEndChatClick(View view, int position) {
                if (!mData.get(position).getLastMsg().equals("已结束对话")) {
                    end_position = position;
                    new Thread(end_chat).start();
                    mData.get(position).setLastMsg("已结束对话");
                    ChatMessage end_msg = new ChatMessage(MyApplication.getInstance().getMyInfo().getId(),
                            mData.get(position).getId(), ChatMessage.MessageType_End, "已结束对话", new Date());
                    realm.beginTransaction();
                    RealmResults<ChatMessage> results = realm.where(ChatMessage.class).equalTo("csId", MyApplication.getInstance().getMyInfo().getId()).equalTo("mId", mData.get(position).getId())
                            .equalTo("mType", ChatMessage.MessageType_End).findAll();
                    results.deleteAllFromRealm();
                    realm.copyToRealm(end_msg);
                    realm.commitTransaction();
                    mAdapter.keepMsgData();
                    mAdapter.Refresh();
                } else
                    Toast.makeText(getActivity(), "该对话已结束", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {
                CustomerInfo customerInfo = realm.where(CustomerInfo.class).equalTo("id", mData.get(position).getId()).findFirst();
                customer_name.setText(customerInfo.getName());
                customer_sex.setText(customerInfo.getSex());
                String subStr[] = customerInfo.getArea().split(",");
                String text = "";
                for (String aSubStr : subStr) text += aSubStr;
                customer_area.setText(text);
                String keywordStr = customerInfo.getLastconversationkeyword();
                if (keywordStr != null)
                    customer_keyword.setText(keywordStr.substring(1, keywordStr.length() - 1));
                else
                    customer_keyword.setText("");
                showCustomerInfo.show();
            }
        });
        msg_list.setAdapter(mAdapter);

        mAdapter.Refresh();

        View Alertview = inflater.inflate(R.layout.dialog_customer_info, null);
        customer_name = Alertview.findViewById(R.id.customer_name);
        customer_sex = Alertview.findViewById(R.id.customer_sex);
        customer_area = Alertview.findViewById(R.id.customer_area);
        customer_keyword = Alertview.findViewById(R.id.customer_keyWord);
        showCustomerInfo = new AlertDialog.Builder(getContext(), R.style.dialog).create();
        showCustomerInfo.setView(Alertview);

    }
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MessageService.MsgBinder) service;
            mBinder.getService().addCallback(new MessageService.Callback() {
                @Override
                public void onDataChange(ChatMessage data) {
                    Message msg = new Message();
                    msg.obj = data;
                    msg.what = SERVICE_MSG;
                    handler.sendMessage(msg);
                }

                @Override
                public void isWebSocketConnected(boolean state) {
                    Message msg = new Message();
                    msg.obj = state;
                    msg.what = WEBSOCKET_STATE;
                    handler.sendMessage(msg);
                }
            });
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SERVICE_MSG:
                    ChatMessage data = (ChatMessage) msg.obj;
                    realm.beginTransaction();
                    realm.copyToRealm(data);
                    realm.commitTransaction();
                    boolean isNew = true;
                    CustomerInfo info = realm.where(CustomerInfo.class).equalTo("id", data.getId()).findFirst();
                    for (int i = 0; i < mData.size(); i++) {
                        if (mData.get(i).getId() == data.getId()) {
                            if (data.getContent().equals("客户接入")) {
                                mData.get(i).setName(info.getUsername());
                                mData.get(i).setLevel(info.getLevel());
                            }
                            mData.get(i).setNewMsgNumber(mData.get(i).getNewMsgNumber() + 1);
                            if (mBinder.getService().isApplicationBroughtToBackground(getContext()))
                                notification(mData.get(i).getId(), mData.get(i).getName(), data.getContent());
                            isNew = false;
                            break;
                        }
                    }
                    if (isNew) {
                        MsgList msgList = new MsgList(MyApplication.getInstance().getMyInfo().getId(), data.getId(), info.getUsername(), data.getContent(), data.getDate());
                        msgList.setNewMsgNumber(1);
                        if (info.getSex().equals("男"))
                            msgList.setHeader(R.drawable.pic_sul1);
                        else
                            msgList.setHeader(R.drawable.pic_sul3);
                        msgList.setLevel(info.getLevel());
                        if (mBinder.getService().isApplicationBroughtToBackground(getContext()))
                            notification(msgList.getId(), msgList.getName(), data.getContent());
                        mData.add(msgList);
                    }
                    mAdapter.keepMsgData();
                    mAdapter.Refresh();
                    break;
                case WEBSOCKET_STATE:
                    if (!(boolean) msg.obj && !statement.getText().equals("挂起"))
                        statement.setText("离线");
                    else if ((boolean) msg.obj)
                        statement.setText("在线");
                    break;
                default:
                    break;
            }
        }
    };

    private Runnable end_chat = new Runnable() {
        @Override
        public void run() {
            HttpPost.end_chat(mData.get(end_position).getId());
        }
    };
    private void notification(int id, String name, String content) {
        NotificationManager notifyManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        //获取PendingIntent
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("title", name);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建 Notification.Builder 对象
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_start);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(getContext().getApplicationContext())
                //点击通知后自动清除
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.login_logo2)
                .setLargeIcon(bitmap)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(name)
                .setContentText(content)
                .setContentIntent(mainPendingIntent)
                .setFullScreenIntent(pendingIntent, false);
        //发送通知
        notifyManager.notify(id, builder.build());
    }
}
