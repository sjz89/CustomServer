package com.bielang.customserver.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bielang.customserver.adapter.WorkAdapter;
import com.bielang.customserver.bean.CustomerInfo;
import com.bielang.customserver.bean.LeaveMessage;
import com.bielang.customserver.MessageService;
import com.bielang.customserver.MyApplication;
import com.bielang.customserver.R;
import com.bielang.customserver.bean.ChatMessage;
import com.bielang.customserver.bean.MsgList;
import com.bielang.customserver.adapter.LeaveMsgAdapter;
import com.bielang.customserver.adapter.MsgListAdapter;
import com.bielang.customserver.adapter.ViewPagerAdapter;
import com.bielang.customserver.bean.ReplyMsg;
import com.bielang.customserver.bean.WorkList;
import com.bielang.customserver.emotion.EmotionMainFragment;
import com.bielang.customserver.util.BottomNavigationViewHelper;
import com.bielang.customserver.util.CacheDataManager;
import com.bielang.customserver.util.DateUtil;
import com.bielang.customserver.util.HttpPost;
import com.bielang.customserver.util.JsonParser;
import com.bielang.customserver.util.SharePreferencesUtil;
import com.bielang.customserver.view.NoScrollViewPager;
import com.bumptech.glide.Glide;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
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
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import static com.bielang.customserver.util.GetStatusBarHeight.getStatusBarHeight;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private int mId;
    private boolean isManager;
    private LinearLayout service_layout;
    private LinearLayout msg_layout;
    private LinearLayout work_layout;
    private LinearLayout setting_layout;
    private ArrayList<MsgList> mData;
    private MsgListAdapter mAdapter;
    private TextView title;
    private TabLayout tabLayout;
    private TextView statement;
    private ImageView changeState;
    private MessageService.MsgBinder mBinder;
    private RecyclerView msg_list;
    private EditText InputBox;
    private FrameLayout input_layout;
    private EmotionMainFragment emotionMainFragment;
    private FragmentManager fm;
    private BottomNavigationView navigation;
    private int mLastHeight;
    private int reply_position;
    private int end_position;
    private LeaveMsgAdapter leave_msg_adapter;
    private ArrayList<LeaveMessage> leave_messages;
    private ImageView user_head;
    private TextView user_name;
    private ArrayList<WorkList> doingList;
    private ArrayList<WorkList> doneList;
    private WorkAdapter doingAdapter;
    private WorkAdapter doneAdapter;
    private Realm realm;
    private TextView customer_name;
    private TextView customer_level;
    private TextView customer_sex;
    private TextView customer_area;
    private TextView customer_keyword;
    private AlertDialog showCustomerInfo;
    private Date last_fresh_time;
    private ImageView search_btn;
    private RelativeLayout search_bar;
    private RelativeLayout title_title;
    private ImageView switch_button;
    private RecyclerView search_list;
    private WorkAdapter search_adapter;
    private ArrayList<WorkList> search_datas;
    private NoScrollViewPager viewPager;
    private EditText search_box;

    private static final int SERVICE_MSG = 0;
    private static final int LEAVE_MSG = 1;
    private static final int WEBSOCKET_STATE = 2;
    private static final int ORDER_MSG = 3;

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

    @Override
    protected void onResume() {
        super.onResume();
        if (!isManager) {
            mId = getIntent().getIntExtra("id", -1);
            if (mId != -1) {
                for (int i = 0; i < mData.size(); i++) {
                    if (mData.get(i).getId() == mId) {
                        mData.get(i).setNewMsgNumber(0);
                    }
                }
            }
            mId = -1;
            mAdapter.keepMsgData();
            mAdapter.Refresh();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIat = SpeechRecognizer.createRecognizer(MainActivity.this, initListener);
        mIatDialog = new RecognizerDialog(MainActivity.this, initListener);

        realm = Realm.getDefaultInstance();
        isManager = SharePreferencesUtil.getValue(this, "isManager", false);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ViewGroup mContentView = (ViewGroup) this.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        toolbar.setPadding(0, getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        msg_layout = (LinearLayout) findViewById(R.id.message_list);
        work_layout = (LinearLayout) findViewById(R.id.work_list);
        setting_layout = (LinearLayout) findViewById(R.id.setting_list);

        title = (TextView) findViewById(R.id.title_mainActivity);
        title_title=(RelativeLayout)findViewById(R.id.title_title);

        initMessageLayout();
        initWorkLayout();
        initSettingLayout();

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        switch_button = (ImageView) findViewById(R.id.switch_to_manager);
        if (isManager) {
            switch_button.setVisibility(View.VISIBLE);
            switch_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ManagerActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
            Menu menu = navigation.getMenu();
            menu.removeItem(R.id.navigation_service);
            navigation.setSelectedItemId(R.id.navigation_message);
        } else {
            Intent bindIntent = new Intent(this, MessageService.class);
            bindService(bindIntent, connection, BIND_AUTO_CREATE);
            service_layout = (LinearLayout) findViewById(R.id.service_list);
            initServiceLayout();
            navigation.setSelectedItemId(R.id.navigation_service);
        }


//        new Thread(getLeaveMsg).start();
        if (DateUtil.getDay() - DateUtil.getDay(new Date(SharePreferencesUtil.getValue(this, "update_hot_words_time", 0))) != 0)
            new Thread(update_hot_words).start();
    }

    //消息界面
    private void initServiceLayout() {
        statement = (TextView) findViewById(R.id.statement);
        changeState = (ImageView) findViewById(R.id.change_state);
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
        RecyclerView msg_list = (RecyclerView) findViewById(R.id.list_msg);
        msg_list.setLayoutManager(new LinearLayoutManager(this));
        msg_list.setHasFixedSize(true);
        mData = new ArrayList<>();

        //数据库读取列表
        RealmResults<MsgList> msgLists = realm.where(MsgList.class).equalTo("csId",MyApplication.getInstance().getMyInfo().getId()).findAll();
        mData = (ArrayList<MsgList>) realm.copyFromRealm(msgLists);

        mAdapter = new MsgListAdapter(this, mData, new MsgListAdapter.ItemTouchListener() {
            @Override
            public void onItemClick(View view, int position) {
                mId = mData.get(position).getId();
                mData.get(position).setNewMsgNumber(0);
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("id", mId);
                intent.putExtra("title", mData.get(position).getName());
                intent.putExtra("header", mData.get(position).getHeader());
                startActivity(intent);
            }

            @Override
            public void onRightMenuDeleteClick(View view, int position) {
                realm.beginTransaction();
                RealmResults<MsgList> msgLists = realm.where(MsgList.class).equalTo("csId",MyApplication.getInstance().getMyInfo().getId()).equalTo("mId", mData.get(position).getId()).findAll();
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
                    RealmResults<ChatMessage> results = realm.where(ChatMessage.class).equalTo("csId",MyApplication.getInstance().getMyInfo().getId()).equalTo("mId", mData.get(position).getId())
                            .equalTo("mType", ChatMessage.MessageType_End).findAll();
                    results.deleteAllFromRealm();
                    realm.copyToRealm(end_msg);
                    realm.commitTransaction();
                    mAdapter.keepMsgData();
                    mAdapter.Refresh();
                } else
                    Toast.makeText(MainActivity.this, "该对话已结束", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {
                CustomerInfo customerInfo = realm.where(CustomerInfo.class).equalTo("id", mData.get(position).getId()).findFirst();
                customer_name.setText(customerInfo.getName());
                customer_level.setText(String.valueOf((int) customerInfo.getLevel()));
                customer_sex.setText(customerInfo.getSex());
                String subStr[] =customerInfo.getArea().split(",");
                String text="";
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

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View Alertview = inflater.inflate(R.layout.dialog_customer_info, null);
        customer_name = Alertview.findViewById(R.id.customer_name);
        customer_level = Alertview.findViewById(R.id.customer_level);
        customer_sex = Alertview.findViewById(R.id.customer_sex);
        customer_area = Alertview.findViewById(R.id.customer_area);
        customer_keyword = Alertview.findViewById(R.id.customer_keyWord);
        showCustomerInfo = new AlertDialog.Builder(this, R.style.dialog).create();
        showCustomerInfo.setView(Alertview);

    }

    //留言界面
    private void initMessageLayout() {
        final LinearLayout ll_root = (LinearLayout) findViewById(R.id.container);
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
        input_layout = (FrameLayout) findViewById(R.id.editor_reply);
        msg_list = (RecyclerView) findViewById(R.id.msg_listView);
        msg_list.setLayoutManager(new LinearLayoutManager(this));
        msg_list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                input_layout.setVisibility(View.GONE);
                navigation.setVisibility(View.VISIBLE);
                return false;
            }
        });
        initEmotionMainFragment();
        leave_messages = new ArrayList<>();

        RealmResults<LeaveMessage> realmResults = realm.where(LeaveMessage.class).equalTo("csId",MyApplication.getInstance().getMyInfo().getId()).findAll();
        leave_messages = (ArrayList<LeaveMessage>) realm.copyFromRealm(realmResults);

        leave_msg_adapter = new LeaveMsgAdapter(this, leave_messages);
        msg_list.setAdapter(leave_msg_adapter);
        leave_msg_adapter.setOnItemClickListener(new LeaveMsgAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                navigation.setVisibility(View.GONE);
                input_layout.setVisibility(View.VISIBLE);
                InputBox.requestFocus();
                InputMethodManager imm = (InputMethodManager) InputBox.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(InputBox, 0);
                reply_position = position;
                msg_list.scrollToPosition(position);
            }
        });
        if (leave_messages.size() == 0) {
            leave_messages.add(new LeaveMessage(MyApplication.getInstance().getMyInfo().getId(),8888, "别浪", "看到请回复", new Date(), new RealmList<ReplyMsg>()));
            leave_messages.add(new LeaveMessage(MyApplication.getInstance().getMyInfo().getId(),9999, "小浪", "啦啦啦啦啦", new Date(), new RealmList<ReplyMsg>()));
            leave_msg_adapter.Refresh();
        }
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
        emotionMainFragment.bindToContentView(msg_list);
        fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.editor_reply, emotionMainFragment, "tag");
        transaction.addToBackStack(null);
        //提交修改
        transaction.commit();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onStart() {
        super.onStart();
        TextView BtnSend = fm.findFragmentByTag("tag").getView().findViewById(R.id.BtnSend);
        InputBox = fm.findFragmentByTag("tag").getView().findViewById(R.id.InputBox);
        InputBox.setFocusable(true);
        InputBox.setFocusableInTouchMode(true);
        ImageView voice_button = fm.findFragmentByTag("tag").getView().findViewById(R.id.voice_button);
        voice_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlowerCollector.onEvent(MainActivity.this, "iat_recognize");
                mIatResults.clear();
                // 设置参数
                setParam();
                mIatDialog.setListener(mRecognizerDialogListener);
                mIatDialog.show();
            }
        });
        BtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!InputBox.getText().toString().equals("")) {
                    leave_messages.get(reply_position).addReply_msg("你回复" + leave_messages.get(reply_position).getName()
                            + "：" + InputBox.getText().toString());
                    leave_msg_adapter.Refresh();
                    InputBox.setText("");
                    msg_list.scrollToPosition(reply_position);
                }
            }
        });
    }

    private void initWorkLayout() {
        LayoutInflater inflater = getLayoutInflater();
        viewPager = (NoScrollViewPager) findViewById(R.id.view_pager);
        viewPager.setScroll(true);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        List<View> viewList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();
        titleList.add("正在进行");
        titleList.add("已完结的");
        View tab01 = inflater.inflate(R.layout.viewpage_doing, null);
        View tab02 = inflater.inflate(R.layout.viewpage_done, null);
        viewList.add(tab01);
        viewList.add(tab02);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(viewList, titleList);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        RecyclerView work_doing_list = tab01.findViewById(R.id.work_doing_list);
        work_doing_list.setLayoutManager(new LinearLayoutManager(this));
        doingList = new ArrayList<>();
        RealmResults<WorkList> doingResults;
        if (!isManager)
            doingResults = realm.where(WorkList.class).equalTo("customerserviceId",MyApplication.getInstance().getMyInfo().getId())
                    .equalTo("status", "notfinish").or().equalTo("status", "waitconfirm").findAllSorted("id", Sort.DESCENDING);
        else
            doingResults=realm.where(WorkList.class).equalTo("company_id",MyApplication.getInstance().getMyInfo().getCompanyId())
                    .equalTo("status", "notfinish").or().equalTo("status", "waitconfirm").findAllSorted("id", Sort.DESCENDING);
        for (int i = 0; i < doingResults.size(); i++) {
            doingList.add(i, realm.copyFromRealm(doingResults.get(i)));
        }

        doingAdapter = new WorkAdapter(doingList);
        work_doing_list.setAdapter(doingAdapter);
        doingAdapter.setOnItemClickListener(new WorkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this, OrderDetailActivity.class);
                intent.putExtra("Id", doingList.get(position).getId());
                startActivity(intent);
            }
        });

        RecyclerView work_done_list = tab02.findViewById(R.id.work_done_list);
        work_done_list.setLayoutManager(new LinearLayoutManager(this));
        doneList = new ArrayList<>();
        RealmResults<WorkList> doneResults;
        if (!isManager)
            doneResults= realm.where(WorkList.class).equalTo("customerserviceId",MyApplication.getInstance().getMyInfo().getId())
                    .equalTo("status", "finish").or().equalTo("status", "cancel").findAllSorted("id", Sort.DESCENDING);
        else
            doneResults= realm.where(WorkList.class).equalTo("company_id",MyApplication.getInstance().getMyInfo().getCompanyId())
                    .equalTo("status", "finish").or().equalTo("status", "cancel").findAllSorted("id", Sort.DESCENDING);
        for (int i = 0; i < doneResults.size(); i++) {
            doneList.add(i, realm.copyFromRealm(doneResults.get(i)));
        }

        doneAdapter = new WorkAdapter(doneList);
        work_done_list.setAdapter(doneAdapter);
        doneAdapter.setOnItemClickListener(new WorkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this, OrderDetailActivity.class);
                intent.putExtra("Id", doingList.get(position).getId());
                startActivity(intent);
            }
        });

        search_list=(RecyclerView)findViewById(R.id.search_list);
        search_list.setLayoutManager(new LinearLayoutManager(this));
        search_datas=new ArrayList<>();
        search_adapter=new WorkAdapter(search_datas);
        search_list.setAdapter(search_adapter);
        search_adapter.setOnItemClickListener(new WorkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this, OrderDetailActivity.class);
                intent.putExtra("Id", search_datas.get(position).getId());
                startActivity(intent);
            }
        });
        search_list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });

        search_box=(EditText)findViewById(R.id.search_box);
        search_box.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                search_datas.clear();
                search_adapter.notifyDataSetChanged();
                if (editable.length()!=0) {
                    String text = editable.toString();
                    RealmResults<WorkList> results = realm.where(WorkList.class).equalTo("id", Integer.parseInt(text))
                            .or().equalTo("customerId", Integer.parseInt(text))
                            .or().equalTo("customerserviceId", Integer.parseInt(text))
                            .or().equalTo("product_id", Integer.parseInt(text)).findAll();
                    for (int i = 0; i < results.size(); i++) {
                        search_datas.add(realm.copyFromRealm(results.get(i)));
                        search_adapter.notifyItemChanged(i);
                    }
                }
            }
        });

        search_bar=(RelativeLayout)findViewById(R.id.search_bar);
        search_btn=(ImageView)findViewById(R.id.search_button);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setVisibility(View.GONE);
                search_list.setVisibility(View.VISIBLE);
                switch_button.setVisibility(View.GONE);
                title_title.setVisibility(View.GONE);
                search_btn.setVisibility(View.GONE);
                search_bar.setVisibility(View.VISIBLE);
                navigation.setVisibility(View.GONE);
                search_box.requestFocus();
                InputMethodManager imm = (InputMethodManager) search_box.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(search_box, 0);
            }
        });

    }

    //设置界面
    private void initSettingLayout() {
        user_head = (ImageView) findViewById(R.id.setting_head);
        //头像
        Glide.with(this).load(MyApplication.getInstance().getMyInfo().getHeader()).error(R.drawable.pic_sul2).into(user_head);
        TextView user_account = (TextView) findViewById(R.id.setting_type);
        user_account.setText(String.valueOf("ID:" + MyApplication.getInstance().getMyInfo().getId()));
        user_name = (TextView) findViewById(R.id.setting_name);
        //用户名
        user_name.setText(MyApplication.getInstance().getMyInfo().getUsername());

        LinearLayout info = (LinearLayout) findViewById(R.id.setting_info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                startActivityForResult(intent, 1000);
            }
        });

        TextView button_clean = (TextView) findViewById(R.id.setting_clean);
        button_clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String CacheSize = "已清理" + CacheDataManager.getTotalCacheSize(view.getContext()) + "缓存文件";
                    Toast.makeText(view.getContext(), CacheSize, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                CacheDataManager.clearAllCache(view.getContext());
            }
        });
        TextView button_quit = (TextView) findViewById(R.id.quit);
        button_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View e) {
                //退出登录
                if (!isManager)
                    mBinder.getService().disconnect();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                sendBroadcast(new Intent("finish_manager_activity"));
                MainActivity.this.finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data.getBooleanExtra("hasChanged", false)) {
            Glide.with(this).load(MyApplication.getInstance().getMyInfo().getHeader()).error(R.drawable.pic_sul2).into(user_head);
            user_name.setText(MyApplication.getInstance().getMyInfo().getUsername());
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_service:
                if (service_layout.getVisibility() != View.VISIBLE) {
                    search_btn.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    statement.setVisibility(View.VISIBLE);
                    changeState.setVisibility(View.VISIBLE);
                    title.setText(R.string.title_service);
                    service_layout.setVisibility(View.VISIBLE);
                    msg_layout.setVisibility(View.GONE);
                    work_layout.setVisibility(View.GONE);
                    setting_layout.setVisibility(View.GONE);
                }
                return true;
            case R.id.navigation_message:
                if (msg_layout.getVisibility() != View.VISIBLE) {
                    search_btn.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    if (!isManager) {
                        statement.setVisibility(View.GONE);
                        changeState.setVisibility(View.GONE);
                        service_layout.setVisibility(View.GONE);
                    }
                    title.setText(R.string.title_message);
                    msg_layout.setVisibility(View.VISIBLE);
                    work_layout.setVisibility(View.GONE);
                    setting_layout.setVisibility(View.GONE);
                }
                return true;
            case R.id.navigation_work:
                if (work_layout.getVisibility() != View.VISIBLE) {
                    new Thread(getOrderMsg).start();
                    if (!isManager) {
                        statement.setVisibility(View.GONE);
                        changeState.setVisibility(View.GONE);
                        service_layout.setVisibility(View.GONE);
                    }
                    search_btn.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    title.setText(R.string.title_work);
                    msg_layout.setVisibility(View.GONE);
                    work_layout.setVisibility(View.VISIBLE);
                    setting_layout.setVisibility(View.GONE);
                }
                return true;
            case R.id.navigation_setting:
                if (setting_layout.getVisibility() != View.VISIBLE) {
                    search_btn.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    if (!isManager) {
                        statement.setVisibility(View.GONE);
                        changeState.setVisibility(View.GONE);
                        service_layout.setVisibility(View.GONE);
                    }
                    title.setText(R.string.title_setting);
                    msg_layout.setVisibility(View.GONE);
                    work_layout.setVisibility(View.GONE);
                    setting_layout.setVisibility(View.VISIBLE);
                }
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!emotionMainFragment.isInterceptBackPress()) {
            if (input_layout.getVisibility() == View.VISIBLE) {
                input_layout.setVisibility(View.GONE);
                navigation.setVisibility(View.VISIBLE);
            }else if (search_bar.getVisibility()==View.VISIBLE){
                search_box.setText("");
                search_datas.clear();
                viewPager.setVisibility(View.VISIBLE);
                search_list.setVisibility(View.GONE);
                search_btn.setVisibility(View.VISIBLE);
                search_bar.setVisibility(View.GONE);
                title_title.setVisibility(View.VISIBLE);
                navigation.setVisibility(View.VISIBLE);
                if (isManager)
                    switch_button.setVisibility(View.VISIBLE);
            }else {
                moveTaskToBack(true);
            }
        }
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
                            if (data.getContent().equals("客户接入"))
                                mData.get(i).setName(info.getUsername());
                            mData.get(i).setNewMsgNumber(mData.get(i).getNewMsgNumber() + 1);
                            if (mBinder.getService().isApplicationBroughtToBackground(MainActivity.this))
                                notification(mData.get(i).getId(), mData.get(i).getName(), data.getContent());
                            isNew = false;
                            break;
                        }
                    }
                    if (isNew) {
                        MsgList msgList = new MsgList(MyApplication.getInstance().getMyInfo().getId(),data.getId(), info.getUsername(), data.getContent(), data.getDate());
                        msgList.setNewMsgNumber(1);
                        if (mBinder.getService().isApplicationBroughtToBackground(MainActivity.this))
                            notification(msgList.getId(), msgList.getName(), data.getContent());
                        mData.add(msgList);
                    }
                    mAdapter.keepMsgData();
                    mAdapter.Refresh();
                    break;
                case LEAVE_MSG:

                    break;
                case ORDER_MSG:
                    RealmResults<WorkList> doingResults;
                    if (!isManager)
                        doingResults = realm.where(WorkList.class).equalTo("customerserviceId",MyApplication.getInstance().getMyInfo().getId())
                                .equalTo("status", "notfinish").or().equalTo("status", "waitconfirm").findAllSorted("id", Sort.DESCENDING);
                    else
                        doingResults=realm.where(WorkList.class).equalTo("company_id",MyApplication.getInstance().getMyInfo().getCompanyId())
                                .equalTo("status", "notfinish").or().equalTo("status", "waitconfirm").findAllSorted("id", Sort.DESCENDING);
                    doingList.clear();
                    for (int i = 0; i < doingResults.size(); i++) {
                        doingList.add(i, realm.copyFromRealm(doingResults.get(i)));
                        doingAdapter.notifyItemChanged(i);
                    }
                    RealmResults<WorkList> doneResults;
                    if (!isManager)
                        doneResults= realm.where(WorkList.class).equalTo("customerserviceId",MyApplication.getInstance().getMyInfo().getId())
                                .equalTo("status", "finish").or().equalTo("status", "cancel").findAllSorted("id", Sort.DESCENDING);
                    else
                        doneResults= realm.where(WorkList.class).equalTo("company_id",MyApplication.getInstance().getMyInfo().getCompanyId())
                                .equalTo("status", "finish").or().equalTo("status", "cancel").findAllSorted("id", Sort.DESCENDING);
                    doneList.clear();
                    for (int i = 0; i < doneResults.size(); i++) {
                        doneList.add(i, realm.copyFromRealm(doneResults.get(i)));
                        doneAdapter.notifyItemChanged(i);
                    }
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
    //    private Runnable getLeaveMsg=new Runnable() {
//        @Override
//        public void run() {
//            while(Thread.currentThread().isAlive()) {
//              发送post请求获取留言信息
//              Message msg = new Message();
//              Bundle data = new Bundle();
//              data.putString("leave_msg",);
//              msg.setData(data);
//              msg.what=LEAVE_MSG;
//              handler.sendMessage(msg);
//                try {
//                    //一分钟同步一次数据
//                    Thread.sleep(60000);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    };
    private Runnable getOrderMsg = new Runnable() {
        @Override
        public void run() {
            if (last_fresh_time==null||new Date().getTime()-last_fresh_time.getTime()>60000) {
                last_fresh_time=new Date();
                if (!isManager)
                    HttpPost.get_total_order(MyApplication.getInstance().getMyInfo().getId());
                else
                    HttpPost.get_company_order();
                Message msg = new Message();
                msg.what = ORDER_MSG;
                handler.sendMessage(msg);
            }
        }
    };
    private Runnable end_chat = new Runnable() {
        @Override
        public void run() {
            HttpPost.end_chat(mData.get(end_position).getId());
        }
    };
    private Runnable update_hot_words = new Runnable() {
        @Override
        public void run() {
            String words = HttpPost.update_hot_words();
            if (words != null) {
                SharePreferencesUtil.putValue(MainActivity.this, "update_hot_words_time", new Date().getTime());
                try {
                    JSONArray jsonArray = new JSONArray(words);
                    JSONArray array = new JSONArray();
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                        array.put(jsonObject.getString("name"));
                    }
                    JSONObject hot_word = new JSONObject();
                    hot_word.put("name", "客户热词");
                    hot_word.put("words", array);
                    JSONArray hot_words = new JSONArray();
                    hot_words.put(hot_word);
                    JSONObject update = new JSONObject();
                    update.put("userword", hot_words);
                    mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
                    mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
                    mIat.updateLexicon("userword", update.toString(), mLexiconListener);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void notification(int id, String name, String content) {
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //获取PendingIntent
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("title", name);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建 Notification.Builder 对象
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_start);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
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

    private LexiconListener mLexiconListener = new LexiconListener() {
        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError error) {
        }
    };

    @Override
    protected void onDestroy() {
        if (!isManager) {
            unbindService(connection);
            stopService(new Intent(this, MessageService.class));
        }
        realm.close();
        super.onDestroy();
        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }
}