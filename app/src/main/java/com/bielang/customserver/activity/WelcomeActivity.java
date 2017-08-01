package com.bielang.customserver.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bielang.customserver.MessageService;
import com.bielang.customserver.R;
import com.bielang.customserver.util.HttpPost;
import com.bielang.customserver.util.SharePreferencesUtil;
import com.bumptech.glide.Glide;

import static com.bielang.customserver.DataName.ACCOUNT;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);

        ImageView load_bg=(ImageView) findViewById(R.id.load_bg);
        Glide.with(this).load(R.mipmap.load).asBitmap().dontAnimate().into(load_bg);

        final boolean isAutoLogin= SharePreferencesUtil.getValue(this,"auto_login",false);
        final boolean isFirstStart=SharePreferencesUtil.getValue(this,"first_start",true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFirstStart){
                    Intent intent=new Intent(WelcomeActivity.this, GuideActivity.class);
                    startActivity(intent);
                    WelcomeActivity.this.finish();
                }else {
                    if (isAutoLogin) {
                        new Thread(networkTask).start();
                    } else {
                        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                        startActivity(intent);
                        WelcomeActivity.this.finish();
                    }
                }
            }
        },1000*2);
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data=msg.getData();
            String val=data.getString("value");
            if (val!=null&&val.equals("success")){
                Intent intent;
                if (!SharePreferencesUtil.getValue(WelcomeActivity.this,"isManager",false)) {
                    intent=new Intent(WelcomeActivity.this,MainActivity.class);
                    startService(new Intent(WelcomeActivity.this, MessageService.class));
                }else
                    intent=new Intent(WelcomeActivity.this,ManagerActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }else{
                Toast.makeText(WelcomeActivity.this,"登陆失败",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        }
    };
    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            boolean isManager=SharePreferencesUtil.getValue(WelcomeActivity.this,"isManager",false);
            String loginBack;
            if (!isManager)
                loginBack=HttpPost.login_service(Integer.parseInt(SharePreferencesUtil.getValue(WelcomeActivity.this,ACCOUNT,"")),
                        SharePreferencesUtil.getValue(WelcomeActivity.this,"password",""));
            else
                loginBack=HttpPost.login_manager(Integer.parseInt(SharePreferencesUtil.getValue(WelcomeActivity.this,ACCOUNT,"")),
                        SharePreferencesUtil.getValue(WelcomeActivity.this,"password",""));
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", loginBack);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };
}
