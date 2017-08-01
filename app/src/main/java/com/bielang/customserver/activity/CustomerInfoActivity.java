package com.bielang.customserver.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bielang.customserver.R;
import com.bielang.customserver.bean.CustomerInfo;

import io.realm.Realm;


public class CustomerInfoActivity extends AppCompatActivity {
    private String Area=null,IPAddr=null,RegistTime=null,LoginTime=null,LastTime=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_info);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_customer_info);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView id=(TextView)findViewById(R.id.customerID);
        TextView level=(TextView)findViewById(R.id.customerLevel);
        TextView name=(TextView)findViewById(R.id.customerName);
        TextView nicName=(TextView)findViewById(R.id.customerNicName);
        TextView sex=(TextView)findViewById(R.id.customerSex);
        TextView phone=(TextView)findViewById(R.id.customerPhone);
        TextView mail=(TextView)findViewById(R.id.customerMail);
        final RelativeLayout moreInfo=(RelativeLayout) findViewById(R.id.moreInfo);
        final TextView ip=(TextView)findViewById(R.id.customerIP);
        final TextView area=(TextView)findViewById(R.id.customerArea);
        final TextView loginTime=(TextView)findViewById(R.id.customerloginTime);
        final TextView registTime=(TextView)findViewById(R.id.customerRegistTime);
        final TextView lastTIme=(TextView)findViewById(R.id.customerlastConversationTime);


        String Level=null,Name=null,NicName=null,Sex=null,Phone=null,Mail=null;

        Realm realm=Realm.getDefaultInstance();
        CustomerInfo customerInfo=realm.where(CustomerInfo.class).equalTo("id",getIntent().getIntExtra("id",0)).findFirst();
        if (customerInfo!=null){
            Level=String.valueOf(customerInfo.getLevel());
            Name=customerInfo.getName();
            NicName=customerInfo.getUsername();
            Sex=customerInfo.getSex();
            Phone=customerInfo.getPhone();
            Mail=customerInfo.getMailbox();
            Area=customerInfo.getArea();
            IPAddr=customerInfo.getIpaddr();
            RegistTime=customerInfo.getRegisttime();
            LoginTime=customerInfo.getLogintime();
            LastTime=customerInfo.getLastconversationtime();
        }

        id.setText(String.valueOf(getIntent().getIntExtra("id",0)));
        if (Level!=null)
            level.setText(Level);
        if (Name!=null)
            name.setText(Name);
        if (NicName!=null)
            nicName.setText(NicName);
        if (Sex!=null)
            sex.setText(Sex);
        if (Phone!=null)
            phone.setText(Phone);
        if (Mail!=null)
            mail.setText(Mail);
        moreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Area!=null) {
                    String subStr[] =Area.split(",");
                    String text="";
                    for (String aSubStr : subStr) text += aSubStr;
                    area.setText(text);
                }
                if (IPAddr!=null)
                    ip.setText(IPAddr);
                if (RegistTime!=null)
                    registTime.setText(RegistTime);
                if (LoginTime!=null)
                    loginTime.setText(LoginTime);
                if (LastTime!=null)
                    lastTIme.setText(LastTime);
                moreInfo.setVisibility(View.GONE);
                RelativeLayout ip_info=(RelativeLayout)findViewById(R.id.customer_info_ip);
                ip_info.setVisibility(View.VISIBLE);
                RelativeLayout area_info=(RelativeLayout)findViewById(R.id.customer_info_area);
                area_info.setVisibility(View.VISIBLE);
                RelativeLayout registtime_info=(RelativeLayout)findViewById(R.id.customer_info_registTime);
                registtime_info.setVisibility(View.VISIBLE);
                RelativeLayout logintime_info=(RelativeLayout)findViewById(R.id.customer_info_loginTime);
                logintime_info.setVisibility(View.VISIBLE);
                RelativeLayout lasttime_info=(RelativeLayout)findViewById(R.id.customer_info_lastTime);
                lasttime_info.setVisibility(View.VISIBLE);
            }
        });

        ImageButton back=(ImageButton) findViewById(R.id.customer_info_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomerInfoActivity.this.finish();
            }
        });
    }
}
