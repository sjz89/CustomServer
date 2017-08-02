package com.bielang.customserver.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bielang.customserver.MessageService;
import com.bielang.customserver.MyApplication;
import com.bielang.customserver.adapter.QuickReplyAdapter;
import com.bielang.customserver.bean.CustomerInfo;
import com.bielang.customserver.bean.SendMessage;
import com.bielang.customserver.adapter.ChatAdapter;
import com.bielang.customserver.bean.ChatMessage;
import com.bielang.customserver.util.DateUtil;
import com.bielang.customserver.R;
import com.bielang.customserver.emotion.EmotionMainFragment;
import com.bielang.customserver.util.HttpPost;
import com.bielang.customserver.util.JsonParser;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.sunflower.FlowerCollector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.bielang.customserver.util.GetStatusBarHeight.getStatusBarHeight;

/**
 * 聊天界面
 * Created by Daylight on 2017/7/5.
 */

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection, MessageService.Callback {
    private Gson gson;
    private int mId;
    private RecyclerView mListView;
    private EditText InputBox;
    private ArrayList<ChatMessage> mData;
    private ChatAdapter mAdapter;
    private int mLastHeight;
    private LinearLayout ll_root;
    private EmotionMainFragment emotionMainFragment;
    private FragmentManager fm;
    private InputMethodManager imm;
    private MessageService.MsgBinder msgBinder;
    private boolean isWebSocketConnected = true;
    private Realm realm;
    private TextView goodsId;
    private TextView msgNum;
    private int newMsg;
    private LinearLayout order_edit;
    private LinearLayout ad;
    private LinearLayout quick_reply;
    private ListView quick_reply_list;
    private ArrayList<String> msgList;

    private SpeechRecognizer mIat;
    private RecognizerDialog mIatDialog;
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private InitListener initListener = new InitListener() {
        @Override
        public void onInit(int i) {
        }
    };
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        public void onError(SpeechError error) {
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent bindIntent = new Intent(this, MessageService.class);
        bindService(bindIntent, this, BIND_AUTO_CREATE);
        realm = Realm.getDefaultInstance();

        mIat = SpeechRecognizer.createRecognizer(ChatActivity.this, initListener);
        mIatDialog = new RecognizerDialog(ChatActivity.this, initListener);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ViewGroup mContentView = (ViewGroup) this.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }

        gson = new GsonBuilder().create();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        toolbar.setPadding(0, getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mId = getIntent().getIntExtra("id", -1);

        TextView title = (TextView) findViewById(R.id.title_chat);
        title.setText(getIntent().getStringExtra("title"));
        LinearLayout back_button = (LinearLayout) findViewById(R.id.chat_back);
        back_button.setOnClickListener(this);
        ImageView info_button = (ImageView) findViewById(R.id.chat_info);
        info_button.setOnClickListener(this);
        ll_root = (LinearLayout) findViewById(R.id.ll_root);
        final View decorView = this.getWindow().getDecorView();//获取window的视图
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        decorView.getWindowVisibleDisplayFrame(r);
                        //window视图添加控件此方法可能会被多次调用,防止重复调用
                        if (mLastHeight != r.bottom) {
                            mLastHeight = r.bottom;
                            ViewGroup.LayoutParams params = ll_root.getLayoutParams();
                            params.height = r.bottom - ll_root.getTop();
                            ll_root.setLayoutParams(params);
                        }
                    }
                });

        mListView = (RecyclerView) findViewById(R.id.MainList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mListView.setLayoutManager(layoutManager);
        mData = new ArrayList<>();
        RealmResults<ChatMessage> messages = realm.where(ChatMessage.class).equalTo("csId", MyApplication.getInstance().getMyInfo().getId()).equalTo("mId", mId).findAllSorted("mDate");
        mData = (ArrayList<ChatMessage>) realm.copyFromRealm(messages);

        //创建接收器时传入头像id
        mAdapter = new ChatAdapter(this, mData, getIntent().getIntExtra("header",R.drawable.pic_sul1));
        mAdapter.setOnItemClickListener(new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showAlertDialog("商品详情",mData.get(position).getContent(),false);
            }

            @Override
            public void onTextClick(View view, int position) {
                showPopWindows(view,position);
            }

        });

        mListView.setAdapter(mAdapter);
        mListView.scrollToPosition(mData.size() - 1);
        mAdapter.notifyDataSetChanged();

        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });

        ll_root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = ll_root.getRootView().getHeight() - ll_root.getHeight();
                if (heightDiff > 300) {
                    mListView.scrollToPosition(mAdapter.getItemCount() - 1);
                }
            }
        });
        initEmotionMainFragment();
        msgNum=(TextView)findViewById(R.id.msg_num);
        msgNum.setVisibility(View.INVISIBLE);
        newMsg=0;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(mId);


    }

    private void showAlertDialog(String title, final String jsonData, boolean hasButton){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View Alertview = inflater.inflate(R.layout.dialog_goods, null);
        goodsId = Alertview.findViewById(R.id.goods_id);
        TextView goodsCategoryId = Alertview.findViewById(R.id.goods_category_id);
        TextView goodsName = Alertview.findViewById(R.id.goods_name);
        TextView goodsPrice = Alertview.findViewById(R.id.goods_price);
        TextView goodsRemark = Alertview.findViewById(R.id.goods_remark);
        TextView goodsStatus = Alertview.findViewById(R.id.goods_status);
        ImageView goodsPic = Alertview.findViewById(R.id.goods_pic);
        AlertDialog goods = new AlertDialog.Builder(ChatActivity.this).create();
        goods.setTitle(title);
        goods.setView(Alertview);
        if (hasButton){
            goods.setButton(DialogInterface.BUTTON_POSITIVE, "推荐", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ChatMessage good = new ChatMessage(MyApplication.getInstance().getMyInfo().getId(),
                            mId, ChatMessage.MessageType_Goods, jsonData, new Date());
                    SendMessage newMessage = new SendMessage(MyApplication.getInstance().getMyInfo().getId(),
                            mId, 1210, goodsId.getText().toString());
                    String message = gson.toJson(newMessage);
                    msgBinder.getService().sendMsg(message);
                    mData.add(good);
                    realm.beginTransaction();
                    realm.copyToRealm(good);
                    realm.commitTransaction();
                    mAdapter.notifyItemChanged(mData.size() - 1);
                    mListView.smoothScrollToPosition(mData.size() - 1);
                }
            });
            goods.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            goodsId.setText(String.valueOf(jsonObject.getInt("id")));
            goodsCategoryId.setText(String.valueOf(jsonObject.getInt("category_id")));
            goodsName.setText(jsonObject.getString("name"));
            goodsPrice.setText(String.valueOf(jsonObject.getInt("price")));
            goodsRemark.setText(jsonObject.getString("remark"));
            goodsStatus.setText(jsonObject.getString("status"));
            Glide.with(ChatActivity.this).load(HttpPost.url + jsonObject.getString("pic"))
                    .placeholder(R.drawable.icon_placeholder).error(R.drawable.icon_failure).into(goodsPic);
            goods.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void showKnowledgeDialog(String jsonStr){
        try {
            JSONArray jsonArray=new JSONArray(jsonStr);
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View knowledgeView = inflater.inflate(R.layout.dialog_knowledge, null);
            TextView knowledge_title=knowledgeView.findViewById(R.id.knowledge_title);
            TextView knowledge_category=knowledgeView.findViewById(R.id.knowledge_category);
            TextView knowledge_content=knowledgeView.findViewById(R.id.knowledge_content);
            AlertDialog showKnowledge=new AlertDialog.Builder(ChatActivity.this).create();
            showKnowledge.setView(knowledgeView);
            showKnowledge.setTitle("搜索结果");
            String title=jsonArray.getJSONObject(0).getString("title");
            String category=jsonArray.getJSONObject(0).getString("category");
            String content=jsonArray.getJSONObject(0).getString("content");
            knowledge_title.setText(Html.fromHtml(title));
            knowledge_category.setText(Html.fromHtml(category));
            knowledge_content.setText(Html.fromHtml("\u3000\u3000"+content));
            showKnowledge.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDataChange(ChatMessage data) {
        Message msg = new Message();
        msg.obj = data;
        msg.what = 1;
        handler.sendMessage(msg);
    }

    @Override
    public void isWebSocketConnected(boolean state) {
        Message msg = new Message();
        msg.obj = state;
        msg.what = 0;
        handler.sendMessage(msg);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        msgBinder = (MessageService.MsgBinder) iBinder;
        msgBinder.getService().addCallback(this);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    isWebSocketConnected = (boolean) msg.obj;
                    break;
                case 1:
                    ChatMessage data = (ChatMessage) msg.obj;
                    if (data.getId() == mId) {
                        mData.add(data);
                        mAdapter.notifyItemChanged(mData.size() - 1);
                        mListView.smoothScrollToPosition(mData.size() - 1);
                    }else{
                        CustomerInfo info = realm.where(CustomerInfo.class).equalTo("id", data.getId()).findFirst();
                        if (msgNum.getVisibility()!=View.VISIBLE)
                            msgNum.setVisibility(View.VISIBLE);
                        if (newMsg<99)
                            msgNum.setText(String.valueOf(++newMsg));
                        notification(data.getId(),info.getName(),data.getContent());
                    }
                    break;
                case 2:
                    String jsonStr = msg.getData().getString("info");
                    if (jsonStr != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            String jsonData = jsonObject.getString("productJSON");
                            showAlertDialog("商品推荐",jsonData,true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(ChatActivity.this, "无推荐商品", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    showKnowledgeDialog(msg.getData().getString("jsonStr"));
                    break;
                case 4:
                    Toast.makeText(ChatActivity.this,"无搜索结果",Toast.LENGTH_SHORT).show();
                case 5:
                    String json=(String)msg.obj;
                    try {
                        JSONObject jsonObject=new JSONObject(json);
                        int total=jsonObject.getInt("total");
                        JSONArray jsonArray=jsonObject.getJSONArray("rows");
                        for (int i=0;i<total;i++){
                            msgList.add(jsonArray.getJSONObject(i).getString("content"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                default:
                    break;
            }
        }
    };

    private Runnable get_goods_info = new Runnable() {
        @Override
        public void run() {
            String jsonStr = HttpPost.get_goods_info(mId);
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("info", jsonStr);
            msg.setData(data);
            msg.what = 2;
            handler.sendMessage(msg);
        }
    };

    private Runnable get_quick_msg=new Runnable() {
        @Override
        public void run() {
            String jsonStr=HttpPost.get_quick_reply_msg();
            if (jsonStr!=null){
                Message msg=new Message();
                msg.obj=jsonStr;
                msg.what=5;
                handler.sendMessage(msg);
            }
        }
    };

    private class search_knowledge implements Runnable {
        String content;
        int firstResult;
        int maxResult;
        search_knowledge(String content,int first,int max){
            this.content=content;
            this.firstResult=first;
            this.maxResult=max;
        }
        @Override
        public void run() {
            String jsonStr=HttpPost.search_knowledge_base(content,firstResult,maxResult);
            if (jsonStr!=null){
                try {
                    JSONObject jsonObject=new JSONObject(jsonStr);
                    int total=jsonObject.getInt("total");
                    if (total!=0){
                        Message msg=new Message();
                        Bundle data=new Bundle();
                        data.putString("jsonStr",jsonObject.getString("rows"));
                        msg.setData(data);
                        msg.what=3;
                        handler.sendMessage(msg);
                    }else {
                        Message msg = new Message();
                        msg.what=4;
                        handler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Message msg = new Message();
                msg.what=4;
                handler.sendMessage(msg);
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    public void initEmotionMainFragment() {
        //构建传递参数
        Bundle bundle = new Bundle();
        //绑定主内容编辑框
        bundle.putBoolean(EmotionMainFragment.BIND_TO_EDITTEXT, true);
        //隐藏控件
        bundle.putBoolean(EmotionMainFragment.HIDE_BAR_EDITTEXT_AND_BTN, false);
        //替换fragment
        //创建修改实例
        emotionMainFragment = EmotionMainFragment.newInstance(EmotionMainFragment.class, bundle);
        emotionMainFragment.bindToContentView(mListView);
        fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fl_emotionview_main, emotionMainFragment, "fragment_tag");
        transaction.addToBackStack(null);
        //提交修改
        transaction.commit();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onStart() {
        super.onStart();
        TextView BtnSend = fm.findFragmentByTag("fragment_tag").getView().findViewById(R.id.BtnSend);
        InputBox = fm.findFragmentByTag("fragment_tag").getView().findViewById(R.id.InputBox);
        ImageView work_button = fm.findFragmentByTag("fragment_tag").getView().findViewById(R.id.work_button);
        ImageView voice_button = fm.findFragmentByTag("fragment_tag").getView().findViewById(R.id.voice_button);
        voice_button.setOnClickListener(this);
        work_button.setVisibility(View.VISIBLE);
        BtnSend.setOnClickListener(this);
        order_edit=emotionMainFragment.getView(0);
        order_edit.setOnClickListener(this);
        ad=emotionMainFragment.getView(1);
        ad.setOnClickListener(this);
        quick_reply=emotionMainFragment.getView(2);
        quick_reply.setOnClickListener(this);
        quick_reply_list=emotionMainFragment.getList();
        msgList=new ArrayList<>();
        new Thread(get_quick_msg).start();
        QuickReplyAdapter adapter=new QuickReplyAdapter(this,msgList);
        quick_reply_list.setAdapter(adapter);
        quick_reply_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InputBox.setText(msgList.get(i));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        realm.close();
        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }

    }

    @Override
    public void onBackPressed() {
        /*
         * 判断是否拦截返回键操作
         */
        if (!emotionMainFragment.isInterceptBackPress()) {
            unbindService(this);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("id", mId);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.BtnSend:
                if (mData.size() != 0 && mData.get(mData.size() - 1).getType() == ChatMessage.MessageType_End)
                    Toast.makeText(ChatActivity.this, "对话已结束", Toast.LENGTH_SHORT).show();
                else if (!InputBox.getText().toString().equals("") && isWebSocketConnected && !msgBinder.getService().isHangUp()) {
                    ChatMessage mMessage = new ChatMessage(MyApplication.getInstance().getMyInfo().getId(), mId, ChatMessage.MessageType_To,
                            InputBox.getText().toString(), DateUtil.getDate());
                    realm.beginTransaction();
                    realm.copyToRealm(mMessage);
                    realm.commitTransaction();
                    mData.add(mMessage);
                    mAdapter.notifyItemChanged(mData.size() - 1);
                    int messageType = 1200;
                    SendMessage newMessage = new SendMessage(MyApplication.getInstance().getMyInfo().getId(),
                            mId, messageType, mMessage.getContent());
                    String message = gson.toJson(newMessage);
                    msgBinder.getService().sendMsg(message);
                    mListView.smoothScrollToPosition(mData.size() - 1);
                    InputBox.setText("");
                } else if (!isWebSocketConnected && !msgBinder.getService().isHangUp())
                    Toast.makeText(ChatActivity.this, "连接已断开，请检查网络", Toast.LENGTH_LONG).show();
                else if (msgBinder.getService().isHangUp())
                    Toast.makeText(ChatActivity.this, "你已挂起，请切换状态", Toast.LENGTH_LONG).show();
                else if (InputBox.getText().toString().equals(""))
                    Toast.makeText(ChatActivity.this, "Say Something!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.order_edit:
                Intent startOrder = new Intent(ChatActivity.this, OrderActivity.class);
                startOrder.putExtra("id", mId);
                startActivityForResult(startOrder, 400);
                break;
            case R.id.ad:
                new Thread(get_goods_info).start();
                break;
            case R.id.quick_reply:
                order_edit.setVisibility(View.GONE);
                ad.setVisibility(View.GONE);
                quick_reply.setVisibility(View.GONE);
                quick_reply_list.setVisibility(View.VISIBLE);
                break;
            case R.id.voice_button:
                FlowerCollector.onEvent(ChatActivity.this, "iat_recognize");
                mIatResults.clear();
                // 设置参数
                setParam();
                mIatDialog.setListener(mRecognizerDialogListener);
                mIatDialog.show();
                break;
            case R.id.chat_back:
                unbindService(this);
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("id", mId);
                startActivity(intent);
                finish();
                break;
            case R.id.chat_info:
                Intent mIntent = new Intent(this, CustomerInfoActivity.class);
                mIntent.putExtra("id", mId);
                startActivity(mIntent);
                break;
        }
    }

    private void notification(int id, String name, String content) {
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //获取PendingIntent
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("title", name);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建 Notification.Builder 对象
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_start);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                //点击通知后自动清除
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setSmallIcon(R.drawable.login_logo2)
                .setLargeIcon(bitmap)
                .setWhen(System.currentTimeMillis())
                .setTicker(name+"："+content)
                .setContentTitle(name)
                .setContentText(content)
                .setContentIntent(mainPendingIntent);
        //发送通知
        notifyManager.notify(id, builder.build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 400:
                ChatMessage order = new ChatMessage(MyApplication.getInstance().getMyInfo().getId(), mId, ChatMessage.MessageType_Hint, "已发送订单", new Date());
                mData.add(order);
                realm.beginTransaction();
                realm.copyToRealm(order);
                realm.commitTransaction();
                mAdapter.notifyItemChanged(mData.size() - 1);
                mListView.smoothScrollToPosition(mData.size() - 1);
                break;
            case 401:
                break;
            default:
                break;
        }
    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuilder resultBuffer = new StringBuilder();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        InputBox.setText(resultBuffer.toString());
        InputBox.setSelection(InputBox.length());
    }

    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.CLOUD_GRAMMAR, null);
        mIat.setParameter(SpeechConstant.SUBJECT, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }
    private void showPopWindows(View v, final int position) {
        View mPopView = LayoutInflater.from(this).inflate(R.layout.popup, null);
        final PopupWindow mPopWindow = new PopupWindow(mPopView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = mPopView.getMeasuredWidth();
        int popupHeight = mPopView.getMeasuredHeight();
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        mPopWindow.showAtLocation(v, Gravity.NO_GRAVITY,
                (location[0] + v.getWidth() / 2) - popupWidth / 2, location[1]
                        - popupHeight);
        mPopWindow.update();
        mPopView.findViewById(R.id.popup_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new search_knowledge(mData.get(position).getContent(),0,1)).start();
                mPopWindow.dismiss();
            }
        });
        mPopView.findViewById(R.id.popup_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData myClip = ClipData.newPlainText("text", mData.get(position).getContent());
                cm.setPrimaryClip(myClip);
                mPopWindow.dismiss();
            }
        });
    }
}