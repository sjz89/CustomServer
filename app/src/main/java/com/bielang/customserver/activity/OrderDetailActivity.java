package com.bielang.customserver.activity;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bielang.customserver.R;
import com.bielang.customserver.bean.WorkList;
import com.bielang.customserver.util.HttpPost;
import com.bielang.customserver.util.SharePreferencesUtil;

import io.realm.Realm;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView commit_order;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_order_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        int mId=getIntent().getIntExtra("Id",0);
        Realm realm=Realm.getDefaultInstance();
        final WorkList order_data=realm.where(WorkList.class).equalTo("id",mId).findFirst();

        TextView id=(TextView)findViewById(R.id.order_detail_id);
        TextView customerId=(TextView)findViewById(R.id.order_detail_customerID);
        TextView customerServiceId=(TextView)findViewById(R.id.order_detail_customerServiceID);
        TextView productId=(TextView)findViewById(R.id.order_detail_product_id);
        final TextView status=(TextView)findViewById(R.id.order_detail_status);
        TextView address=(TextView)findViewById(R.id.order_detail_address);
        TextView end_time=(TextView)findViewById(R.id.order_detail_end_time);
        TextView tel=(TextView)findViewById(R.id.order_detail_tel);
        TextView remarks=(TextView)findViewById(R.id.order_detail_remarks);

        id.setText(String.valueOf(order_data.getId()));
        customerId.setText(String.valueOf(order_data.getCustomerId()));
        customerServiceId.setText(String.valueOf(order_data.getCustomerserviceId()));
        productId.setText(String.valueOf(order_data.getProduct_id()));
        switch (order_data.getStatus()) {
            case "notfinish":
                status.setText("未完成");
                break;
            case "cancel":
                status.setText("已取消");
                break;
            case "waitconfirm":
                status.setText("待确认");
                break;
            case "finish":
                status.setText("已完成");
                break;
        }
        address.setText(order_data.getDemandaddr());
        end_time.setText(order_data.getDemandtime().substring(0,order_data.getDemandtime().length()-5));
        tel.setText(order_data.getDemandphone());
        remarks.setText(order_data.getDemandotherrequest());

        ImageButton back=(ImageButton)findViewById(R.id.order_detail_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.dialog_remarks,null);
        EditText editor=view.findViewById(R.id.editor_remarks);
        editor.setText(remarks.getText());
        editor.setFocusableInTouchMode(false);
        editor.setFocusable(false);

        final AlertDialog seeRemarks=new AlertDialog.Builder(OrderDetailActivity.this).create();
        seeRemarks.setTitle("客户备注");
        seeRemarks.setView(view);
        RelativeLayout see_remarks=(RelativeLayout)findViewById(R.id.order_detail_see_remarks);
        see_remarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seeRemarks.show();
            }
        });
        if (order_data.getStatus().equals("notfinish")&&SharePreferencesUtil.getValue(this,"isManager",false)) {
            commit_order = (TextView) findViewById(R.id.order_commit_button);
            commit_order.setVisibility(View.VISIBLE);
            commit_order.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(commitOrder).start();
                    status.setText("已完成");
                    commit_order.setVisibility(View.GONE);
                }
            });
        }
    }
    private Runnable commitOrder=new Runnable() {
        @Override
        public void run() {
            HttpPost.commit_order(getIntent().getIntExtra("Id",0));
            Realm realm=Realm.getDefaultInstance();
            realm.beginTransaction();
            WorkList order=realm.where(WorkList.class).equalTo("id",getIntent().getIntExtra("Id",0)).findFirst();
            order.setStatus("finish");
            realm.copyToRealmOrUpdate(order);
            realm.commitTransaction();
            realm.close();
            sendBroadcast(new Intent("refresh_order_list"));
        }
    };
}
