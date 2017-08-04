package com.bielang.customserver.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bielang.customserver.MessageService;
import com.bielang.customserver.MyApplication;
import com.bielang.customserver.R;
import com.bielang.customserver.util.HttpPost;
import com.bielang.customserver.util.SharePreferencesUtil;
import com.bumptech.glide.Glide;

import static com.bielang.customserver.DataName.ACCOUNT;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,View.OnFocusChangeListener{
    private EditText et_name, et_pass;
    private Button bt_username_clear;
    private Button bt_pwd_clear;
    private CheckBox remember_pw,auto_login;
    private boolean isManager;
    private String testId="123456";
    private String testPassword="123456";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        SharePreferencesUtil.putValue(this,"first_start",false);
        ImageView logo=(ImageView)findViewById(R.id.logo);
        Glide.with(this).load(R.drawable.login_logo).asBitmap().dontAnimate().into(logo);

        et_name = (EditText) findViewById(R.id.username);
        et_pass = (EditText) findViewById(R.id.password);
        TextView bt_login=(TextView) findViewById(R.id.login);
        TextView bt_login_error=(TextView) findViewById(R.id.login_error);
        bt_username_clear = (Button)findViewById(R.id.bt_username_clear);
        bt_pwd_clear = (Button)findViewById(R.id.bt_pwd_clear);
        remember_pw=(CheckBox) findViewById(R.id.remember_pw);
        auto_login=(CheckBox) findViewById(R.id.auto_login);

        bt_login.setOnClickListener(this);
        bt_login_error.setOnClickListener(this);
        bt_username_clear.setOnClickListener(this);
        bt_pwd_clear.setOnClickListener(this);
        auto_login.setOnClickListener(this);
        remember_pw.setOnClickListener(this);

        et_name.setOnFocusChangeListener(this);
        et_pass.setOnFocusChangeListener(this);

        et_name.setText(SharePreferencesUtil.getValue(this,ACCOUNT,""));
        et_pass.setText(SharePreferencesUtil.getValue(this,"password",""));
        auto_login.setChecked(SharePreferencesUtil.getValue(this,"auto_login",false));
        remember_pw.setChecked(SharePreferencesUtil.getValue(this,"remember",false));

        et_name.addTextChangedListener(new EditChangeListener(bt_username_clear));
        et_pass.addTextChangedListener(new EditChangeListener(bt_pwd_clear));

        isManager=SharePreferencesUtil.getValue(this,"isManager",false);
        RadioGroup roal_choose=(RadioGroup)findViewById(R.id.radio_group);
        RadioButton radio_service=(RadioButton)findViewById(R.id.radio_service);
        RadioButton radio_manager=(RadioButton)findViewById(R.id.radio_manager);
        if (!isManager)
            radio_service.setChecked(true);
        else
            radio_manager.setChecked(true);
        SharePreferencesUtil.putValue(LoginActivity.this,"isManager",isManager);
        roal_choose.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.radio_service:
                        isManager=false;
                        break;
                    case R.id.radio_manager:
                        isManager=true;
                        break;
                    default:
                        isManager = false;
                        break;
                }
                SharePreferencesUtil.putValue(LoginActivity.this,"isManager",isManager);
            }
        });
    }

    private void login(String val){
        if (val!=null&&val.equals("success")) {
            if (remember_pw.isChecked()) {
                SharePreferencesUtil.putValue(this,"remember",true);
                SharePreferencesUtil.putValue(this,"password",et_pass.getText().toString());
            }
            else {
                SharePreferencesUtil.putValue(this,"remember",false);
                SharePreferencesUtil.removeValue(this,"password");
            }
            SharePreferencesUtil.putValue(this,"auto_login",auto_login.isChecked());
            SharedPreferences.Editor sp = this.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
            sp.putString(ACCOUNT, et_name.getText().toString());
            sp.apply();
            Intent intent;
            if (!isManager){
                intent=new Intent(this,MainActivity.class);
                startService(new Intent(LoginActivity.this, MessageService.class));
            }else
                intent=new Intent(this,ManagerActivity.class);
            startActivity(intent);
            this.finish();
        }else if (val!=null&&val.equals("false")){
            SharePreferencesUtil.putValue(this,"auto_login",false);
            SharePreferencesUtil.putValue(this,"remember",false);
            Toast.makeText(this,"密码错误",Toast.LENGTH_SHORT).show();
        }else if (val!=null&&val.equals("noexist")){
            SharePreferencesUtil.putValue(this,"auto_login",false);
            SharePreferencesUtil.putValue(this,"remember",false);
            Toast.makeText(this,"账号不存在",Toast.LENGTH_SHORT).show();
        }
    }
    public void onFocusChange(View v, boolean hasFocus){
        switch(v.getId()){
            case R.id.username:
                bt_pwd_clear.setVisibility(View.INVISIBLE);
                if (et_name.getText().length()!=0)
                    bt_username_clear.setVisibility(View.VISIBLE);
                break;
            case R.id.password:
                bt_username_clear.setVisibility(View.INVISIBLE);
                if (et_pass.getText().length()!=0)
                    bt_pwd_clear.setVisibility(View.VISIBLE);
                break;
        }
    }
    public void onClick(View arg0){
        switch (arg0.getId()){
            case R.id.login:
                if (et_name.getText().toString().equals(testId)&&et_pass.getText().toString().equals(testPassword)){
                    SharedPreferences.Editor sp = this.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                    sp.putString(ACCOUNT, et_name.getText().toString());
                    sp.apply();
                    MyApplication.getInstance().setMyInfo(Integer.parseInt(et_name.getText().toString()),"管理员",
                            "小浪","别浪","男");
                    Intent intent;
                    if (!isManager) {
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                        startService(new Intent(LoginActivity.this, MessageService.class));
                    }else
                        intent=new Intent(LoginActivity.this,ManagerActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }else
                    new Thread(networkTask).start();
                break;
            case R.id.bt_username_clear:
                et_name.setText("");
                break;
            case R.id.bt_pwd_clear:
                et_pass.setText("");
                break;
            case R.id.login_error:
                et_pass.clearFocus();
                et_name.clearFocus();
                new AlertDialog.Builder(this).setTitle(R.string.forget_password)
                        .setMessage("请联系管理员找回密码").setPositiveButton("知道了",null).show();
                break;
            case R.id.auto_login:
                et_pass.clearFocus();
                et_name.clearFocus();
                if (auto_login.isChecked())
                    remember_pw.setChecked(true);
                break;
            case R.id.remember_pw:
                et_pass.clearFocus();
                et_name.clearFocus();
                if(!remember_pw.isChecked())
                    auto_login.setChecked(false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        finishAndRemoveTask();
        super.onDestroy();
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Bundle data=msg.getData();
                    String val=data.getString("value");
                    login(val);
                    break;
            }
        }
    };
    private Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            String loginBack;
            if (!isManager)
                loginBack=HttpPost.login_service(Integer.parseInt(et_name.getText().toString()),et_pass.getText().toString());
            else
                loginBack=HttpPost.login_manager(Integer.parseInt(et_name.getText().toString()),et_pass.getText().toString());
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", loginBack);
            msg.setData(data);
            msg.what=0;
            handler.sendMessage(msg);
        }
    };
    private class EditChangeListener implements TextWatcher{
        private Button clearBtn;
        EditChangeListener(Button clearBtn){
            this.clearBtn=clearBtn;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.length()==0)
                clearBtn.setVisibility(View.INVISIBLE);
            else
                clearBtn.setVisibility(View.VISIBLE);
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.length()==0)
                clearBtn.setVisibility(View.INVISIBLE);
            else
                clearBtn.setVisibility(View.VISIBLE);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length()==0)
                clearBtn.setVisibility(View.INVISIBLE);
            else
                clearBtn.setVisibility(View.VISIBLE);
        }
    }
}
