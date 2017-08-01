package com.bielang.customserver.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bielang.customserver.MyApplication;
import com.bielang.customserver.R;
import com.bielang.customserver.util.HttpPost;
import com.bielang.customserver.util.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.bielang.customserver.util.GetStatusBarHeight.getStatusBarHeight;


public class OrderActivity extends AppCompatActivity implements TimePickerDialog.TimePickerDialogInterface{
    private TimePickerDialog timePickerDialog;
    private TextView end_date;
    private String data;
    private EditText editor;
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ViewGroup mContentView = (ViewGroup) this.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_order);
        toolbar.setPadding(0, getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        TextView send_button=(TextView)findViewById(R.id.order_send_button);
        final TextView customer_id=(TextView)findViewById(R.id.order_customerID);
        final TextView customerService_id=(TextView)findViewById(R.id.order_customerServiceID);
        final EditText id=(EditText)findViewById(R.id.order_id);
        final EditText address=(EditText)findViewById(R.id.order_address);
        end_date=(TextView)findViewById(R.id.order_end_time);
        RelativeLayout choose_date=(RelativeLayout) findViewById(R.id.order_choose_date);
        final EditText phone=(EditText)findViewById(R.id.order_tel);
        final TextView remarks=(TextView)findViewById(R.id.order_remarks);

        LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.dialog_remarks,null);
        editor=view.findViewById(R.id.editor_remarks);
        editor.setText(remarks.getText());

        final AlertDialog addRemarks=new AlertDialog.Builder(OrderActivity.this).create();
        addRemarks.setTitle("客户备注");
        addRemarks.setView(view);
        addRemarks.setButton(DialogInterface.BUTTON_POSITIVE, "保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                remarks.setText(editor.getText());
            }
        });
        addRemarks.setButton(DialogInterface.BUTTON_NEGATIVE, "退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        RelativeLayout add_remarks=(RelativeLayout)findViewById(R.id.order_add_remarks);
        add_remarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRemarks.show();
            }
        });

        customer_id.setText(String.valueOf(getIntent().getIntExtra("id",0)));
        customerService_id.setText(String.valueOf(MyApplication.getInstance().getMyInfo().getId()));
        choose_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerDialog=new TimePickerDialog(OrderActivity.this);
                timePickerDialog.showDatePickerDialog();
            }
        });
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!id.getText().toString().equals("")&&!address.getText().toString().equals("")
                        &&!end_date.getText().toString().equals("")&&!phone.getText().toString().equals("")) {
                    String pat1 = "yyyy-MM-dd";
                    String pat2 = "MM/dd/yyyy";
                    SimpleDateFormat sdf1 = new SimpleDateFormat(pat1, Locale.getDefault());
                    SimpleDateFormat sdf2 = new SimpleDateFormat(pat2, Locale.getDefault());
                    Date date = null;
                    try {
                        date = sdf1.parse(end_date.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    data = "customerId=" + customer_id.getText() + "&customerserviceId=" + customerService_id.getText()
                            + "&product_id=" + id.getText().toString() + "&company_id=" + MyApplication.getInstance().getMyInfo().getCompanyId()
                            + "&demandaddr=" + address.getText().toString() + "&demandtime=" + sdf2.format(date) + "&demandphone="
                            + phone.getText().toString() + "&demandotherrequest=" + remarks.getText().toString();
                    new Thread(sendOrder).start();
                    Intent intent = new Intent(OrderActivity.this, ChatActivity.class);
                    OrderActivity.this.setResult(400, intent);
                    OrderActivity.this.finish();
                }else
                    Toast.makeText(OrderActivity.this,"请将信息填写完整",Toast.LENGTH_LONG).show();
            }
        });
        ImageButton back=(ImageButton)findViewById(R.id.order_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog isExit = new AlertDialog.Builder(OrderActivity.this).create();
                isExit.setTitle("系统提示");
                isExit.setMessage("确认放弃订单并退出？");
                isExit.setButton(DialogInterface.BUTTON_POSITIVE,"确定", listener);
                isExit.setButton(DialogInterface.BUTTON_NEGATIVE,"取消", listener);
                isExit.show();
            }
        });
    }

    @Override
    public void positiveListener() {
        String date=String.valueOf(timePickerDialog.getYear()+"-"+timePickerDialog.getMonth()+"-"+timePickerDialog.getDay());
        end_date.setText(date);
    }

    @Override
    public void negativeListener() {

    }
    private Runnable sendOrder=new Runnable() {
        @Override
        public void run() {
            HttpPost.send_order(data);
        }
    };

    @Override
    public void onBackPressed() {
        AlertDialog isExit = new AlertDialog.Builder(this).create();
        isExit.setTitle("系统提示");
        isExit.setMessage("确认放弃订单并退出？");
        isExit.setButton(DialogInterface.BUTTON_POSITIVE,"确定", listener);
        isExit.setButton(DialogInterface.BUTTON_NEGATIVE,"取消", listener);
        isExit.show();
    }
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    Intent intent=new Intent(OrderActivity.this,ChatActivity.class);
                    setResult(401,intent);
                    OrderActivity.this.finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };
}
