package com.bielang.customserver;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.bielang.customserver.bean.ChatMessage;
import com.bielang.customserver.util.HttpPost;
import com.bielang.customserver.util.SharePreferencesUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bielang.customserver.DataName.ACCOUNT;

public class MessageService extends Service {
    private WebSocketClient webSocketClient;
    private ChatMessage chatMessage=null;
    private List<Callback> list;
    private boolean isHangUp;
    @Override
    public void onCreate() {
        super.onCreate();
        list=new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connect();
        new Thread(checkConnect).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        webSocketClient.close();
        super.onDestroy();
    }
    @SuppressWarnings("deprecation")
    public void connect(){
        isHangUp=false;
        try {
            webSocketClient=new WebSocketClient(new URI("ws://192.168.155.1/channel/websocket/"
                    +SharePreferencesUtil.getValue(this,ACCOUNT,"")),new Draft_17()) {
                @Override
                public void onOpen(ServerHandshake serverHandshakeData) {
                    new Thread(connectWebSocket).start();
                    for (int i=0;i<list.size();i++){
                        list.get(i).isWebSocketConnected(true);
                    }
                    JSONObject initWebSocket=new JSONObject();
                    try {
                        initWebSocket.put("customerservice_id",MyApplication.getInstance().getMyInfo().getId());
                        initWebSocket.put("msgtype_id",1300);
                        sendMsg(initWebSocket.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMessage(final String message) {
                    try {
                        JSONObject jsonString=new JSONObject(message);
                        switch (jsonString.getInt("msgtype_id")){
                            case 3104:
                                HttpPost.get_customer_info(jsonString.getInt("customer_id"));
                                chatMessage=new ChatMessage(MyApplication.getInstance().getMyInfo().getId(),
                                        jsonString.getInt("customer_id"), ChatMessage.MessageType_Hint,"客户接入",new Date());
                                for (int i=0;i<list.size();i++)
                                    list.get(i).onDataChange(chatMessage);
                                if (!isApplicationBroughtToBackground(getApplicationContext()))
                                    ringtoneNotify();
                                break;
                            case 2100:
                                chatMessage = new ChatMessage(MyApplication.getInstance().getMyInfo().getId(),
                                        jsonString.getInt("customer_id"), ChatMessage.MessageType_From, jsonString.getString("content"), new Date());
                                for (int i = 0; i < list.size(); i++)
                                    list.get(i).onDataChange(chatMessage);
                                if (!isApplicationBroughtToBackground(getApplicationContext()))
                                    ringtoneNotify();
                                break;
                            case 2108:
                                if (jsonString.getString("content").equals("confirm"))
                                    chatMessage=new ChatMessage(MyApplication.getInstance().getMyInfo().getId(),
                                            jsonString.getInt("customer_id"), ChatMessage.MessageType_Hint,"对方已确认订单",new Date());
                                else if (jsonString.getString("content").equals("cancel"))
                                    chatMessage=new ChatMessage(MyApplication.getInstance().getMyInfo().getId(),
                                            jsonString.getInt("customer_id"), ChatMessage.MessageType_Hint,"对方已取消订单",new Date());
                                for (int i = 0; i < list.size(); i++)
                                    list.get(i).onDataChange(chatMessage);
                                if (!isApplicationBroughtToBackground(getApplicationContext()))
                                    ringtoneNotify();
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    for (int i=0;i<list.size();i++){
                        list.get(i).isWebSocketConnected(false);
                    }
                }
                @Override
                public void onError(Exception e) {
                    for (int i=0;i<list.size();i++){
                        list.get(i).isWebSocketConnected(false);
                    }
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        if (webSocketClient.isOpen()) {
            isHangUp=true;
            webSocketClient.close();
            new Thread(disconnectWebSocket).start();
        }
    }

    public boolean isHangUp(){
        return isHangUp;
    }
    @SuppressWarnings("deprecation")
    public boolean isApplicationBroughtToBackground(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && !tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        KeyguardManager keyguardManager=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    private void ringtoneNotify(){
        RingtoneManager manager= new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone defaultRingtone = RingtoneManager.getRingtone(this, defaultUri);
        defaultRingtone.play();
    }
    public void sendMsg(String msg){
        if (webSocketClient.isOpen())
           webSocketClient.send(msg);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();
    }

    public class MsgBinder extends Binder{
        public MessageService getService(){
            return MessageService.this;
        }
    }
    public void addCallback(Callback callback) {
        list.add(callback);
    }

    public interface Callback{
        void onDataChange(ChatMessage data);
        void isWebSocketConnected(boolean state);
    }
    private Runnable checkConnect=new Runnable() {
        @Override
        public void run() {
            while (webSocketClient!=null) {
                for (int i = 0; i < list.size(); i++)
                    list.get(i).isWebSocketConnected(webSocketClient.isOpen());
                if (webSocketClient.isClosed()&&!isHangUp)
                    connect();
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private Runnable connectWebSocket=new Runnable() {
        @Override
        public void run() {
            HttpPost.connectWebSocket(Integer.parseInt(SharePreferencesUtil.getValue(MyApplication.getInstance(),ACCOUNT,"")));
        }
    };
    private Runnable disconnectWebSocket=new Runnable() {
        @Override
        public void run() {
            HttpPost.disconnectWebSocket(Integer.parseInt(SharePreferencesUtil.getValue(MyApplication.getInstance(),ACCOUNT,"")));
        }
    };

}
