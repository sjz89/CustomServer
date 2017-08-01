package com.bielang.customserver.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bielang.customserver.R;
import com.bielang.customserver.adapter.MonitorListAdapter;
import com.bielang.customserver.bean.MonitorData;
import com.bielang.customserver.util.BottomNavigationViewHelper;
import com.bielang.customserver.util.HttpPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.BubbleChartData;
import lecho.lib.hellocharts.model.BubbleValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.BubbleChartView;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class ManagerActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    private ScrollView home_layout;
    private LinearLayout monitor_layout;
    private ScrollView data_layout;
    private BubbleChartView bubbleChart;
    private ColumnChartView columnChart;
    private TextView title;
    private TextView complete;
    private TextView degree;
    private TextView newCustomer;
    private TextView danger;
    private ArrayList<MonitorData> monitorDatas;
    private MonitorListAdapter monitorAdapter;
    private static final int HOME_DATA=0;
    private static final int RFM_DATA=1;
    private static final int HOT_WORD=2;
    private static final int MONITOR_DATA=3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_manager);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        title = (TextView) findViewById(R.id.title_managerActivity);
        ImageView switch_button = (ImageView) findViewById(R.id.switch_to_service);
        switch_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ManagerActivity.this,MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        home_layout=(ScrollView)findViewById(R.id.home_list);
        data_layout=(ScrollView)findViewById(R.id.data_list);
        monitor_layout=(LinearLayout)findViewById(R.id.monitor_list);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.manager_navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        navigation.setSelectedItemId(R.id.navigation_home);


        initHomeLayout();
        initDataLayout();
        initMonitorLayout();

        new Thread(getManagerData).start();
    }
    @SuppressWarnings("deprecation")
    private void initHomeLayout(){
        complete=(TextView)findViewById(R.id.monitor_complete);
        degree=(TextView)findViewById(R.id.monitor_degree);
        newCustomer=(TextView)findViewById(R.id.monitor_newCustomer);
        danger=(TextView)findViewById(R.id.monitor_danger);

        LineChartView lineChart=(LineChartView)findViewById(R.id.data_chart);
        int colors[]={R.color.blue,R.color.orange,R.color.green_400};
        List<Line> lines=new ArrayList<>();
        String dataX[]={"0:00","2:00","4:00","6:00","8:00","10:00",
                "12:00","14:00","16:00","18:00","20:00","22:00","24:00"};
        int dataY[]={0,1,4,5,7,8,9,13,19,17,12,7,3};
        for (int n=0;n<3;n++) {
            List<PointValue> values = new ArrayList<>();
            for (int j = 0; j < dataX.length; j++) {
                values.add(new PointValue(j,dataY[j]+n+new Random().nextInt(5)));
            }
            Line line = new Line();               //根据值来创建一条线
            line.setValues(values);
            line.setColor(getResources().getColor(colors[n]));        //设置线的颜色
            line.setShape(ValueShape.CIRCLE);                 //设置点的形状
            line.setHasLines(true);               //设置是否显示线
            line.setHasPoints(true);             //设置是否显示节点
            line.setCubic(true);                     //设置线是否立体或其他效果
            line.setFilled(false);                   //设置是否填充线下方区域
            line.setHasLabels(true);       //设置是否显示节点标签
            //设置节点点击的效果
            line.setHasLabelsOnlyForSelected(true);
            lines.add(line);
        }
        LineChartData lineData=new LineChartData();
        lineData.setLines(lines);
        lineData.setBaseValue(Float.NEGATIVE_INFINITY);
        Axis axisX = new Axis();                    //X轴
        Axis axisY = new Axis().setHasLines(true);  //Y轴
        List<AxisValue> axisValues=new ArrayList<>();
        for (int i=0;i<dataX.length;i++)
            axisValues.add(new AxisValue(i).setLabel(dataX[i]));
        axisX.setValues(axisValues);
        axisX.setTextColor(Color.GRAY);             //X轴灰色
        axisY.setTextColor(Color.GRAY);             //Y轴灰色
        lineData.setAxisXBottom(axisX);            //设置X轴位置 下方
        lineData.setAxisYLeft(axisY);              //设置Y轴位置 左边
        lineChart.setLineChartData(lineData);
        lineChart.setZoomEnabled(true);
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right= dataX.length;
        v.bottom= -1;
        v.top= 25;
        lineChart.setMaximumViewport(v);
        lineChart.setCurrentViewport(v);
    }
    //数据查看界面
    @SuppressWarnings("deprecation")
    private void initDataLayout(){
        bubbleChart=(BubbleChartView)findViewById(R.id.data_bubble_chart);
        bubbleChart.setZoomEnabled(false);
        columnChart=(ColumnChartView)findViewById(R.id.data_column_chart);
        columnChart.setZoomEnabled(false);
    }
    //监控界面
    private void initMonitorLayout(){
        ListView monitor_list=(ListView)findViewById(R.id.monitor_listView);
        monitorDatas=new ArrayList<>();
        monitorAdapter=new MonitorListAdapter(this,monitorDatas);
        monitor_list.setAdapter(monitorAdapter);
        monitorAdapter.Refresh();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HOME_DATA:
                    complete.setText(String.valueOf(msg.getData().getInt("conversation")));
                    degree.setText(String.valueOf(msg.getData().getInt("degree")+"%"));
                    newCustomer.setText(String.valueOf(msg.getData().getInt("newcustomer")));
                    danger.setText(String.valueOf(msg.getData().getInt("danger")));
                    break;
                case RFM_DATA:
                    BubbleChartData bubbleData=(BubbleChartData)msg.obj;
                    bubbleData.setHasLabelsOnlyForSelected(true);
                    Axis axisX = new Axis().setHasLines(true);
                    Axis axisY = new Axis().setHasLines(true);
                    axisX.setName("客户最近购买金额");
                    axisY.setName("客户购买次数");
                    bubbleData.setAxisXBottom(axisX);
                    bubbleData.setAxisYLeft(axisY);
                    bubbleChart.setBubbleChartData(bubbleData);
                    break;
                case MONITOR_DATA:
                    monitorAdapter.notifyDataSetChanged();
                    break;
                case HOT_WORD:
                    ColumnChartData columnData=(ColumnChartData)msg.obj;
                    columnChart.setColumnChartData(columnData);
                    break;
            }
        }
    };
    private Runnable getManagerData = new Runnable() {
        @Override
        public void run() {
            String jsonData=HttpPost.getMonitorData();
            if (jsonData!=null) {
                try {
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    String jsStr = jsonObject.getString("date");
                    jsonObject = new JSONObject(jsStr);
                    data.putInt("conversation",jsonObject.getInt("conversation"));
                    data.putInt("degree",jsonObject.getInt("degree"));
                    data.putInt("newcustomer",jsonObject.getInt("newcustomer"));
                    data.putInt("danger",jsonObject.getInt("danger"));
                    msg.setData(data);
                    msg.what = HOME_DATA;
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable getMonitorData=new Runnable() {
        @Override
        public void run() {
            String jsonData=HttpPost.getCustomerServiceState();
            if (jsonData!=null){
                try {
                    Message msg = new Message();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray jsonArray=new JSONArray(jsonObject.getString("rows"));
                    monitorDatas.clear();
                    for (int i=0;i<jsonArray.length();i++){
                        monitorDatas.add(new MonitorData(jsonArray.getJSONObject(i).getInt("id"),
                                jsonArray.getJSONObject(i).getInt("haveservice"),jsonArray.getJSONObject(i).getBoolean("isonline")));
                    }
                    msg.what = MONITOR_DATA;
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable getHotWord=new Runnable() {
        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            String jsonData=HttpPost.update_hot_words();
            if (jsonData!=null){
                try {
                    JSONArray jsonArray=new JSONArray(jsonData);
                    int numSubColumns=1;
                    List<Column> columns=new ArrayList<>();
                    List<SubcolumnValue> values;
                    List<AxisValue> axisValues=new ArrayList<>();
                    for (int i=0;i<jsonArray.length();i++){
                        values=new ArrayList<>();
                        for (int j=0;j<numSubColumns;j++){
                            values.add(new SubcolumnValue(jsonArray.getJSONObject(i).getInt("value"),
                                    getResources().getColor(R.color.blue)));
                            axisValues.add(new AxisValue(i).setLabel(jsonArray.getJSONObject(i).getString("name")));
                        }
                        Column column=new Column(values);
                        column.setHasLabelsOnlyForSelected(true);
                        columns.add(column);
                    }
                    ColumnChartData columnData=new ColumnChartData(columns);
                    columnData.setAxisXBottom(new Axis(axisValues).setTextColor(R.color.black));
                    columnData.setAxisYLeft(new Axis().setHasLines(true));
                    Message msg=new Message();
                    msg.obj=columnData;
                    msg.what=HOT_WORD;
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private Runnable getRFMData=new Runnable() {
        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            String jsonData=HttpPost.get_rfm_data();
            if (jsonData!=null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    jsonArray=jsonArray.getJSONArray(0);
                    List<BubbleValue> bubbleValues=new ArrayList<>();
                    for (int i=0;i<jsonArray.length();i++){
                        float x=(float)jsonArray.getJSONObject(i).getInt("buysum");
                        float y=(float)jsonArray.getJSONObject(i).getInt("buytimes");
                        float z=jsonArray.getJSONObject(i).getLong("lastbuytime");
                        BubbleValue value=new BubbleValue(x,y,z);
                        value.setShape(ValueShape.CIRCLE);
                        value.setColor(getResources().getColor(R.color.red_400));
                        bubbleValues.add(value);
                    }
                    BubbleChartData bubbleData=new BubbleChartData(bubbleValues);
                    bubbleData.setBubbleScale((float)0.4);
                    Message msg=new Message();
                    msg.obj=bubbleData;
                    msg.what=RFM_DATA;
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (home_layout.getVisibility()!=View.VISIBLE) {
                    new Thread(getManagerData).start();
                    title.setText(R.string.title_home);
                    home_layout.setVisibility(View.VISIBLE);
                    data_layout.setVisibility(View.GONE);
                    monitor_layout.setVisibility(View.GONE);
                }
                return true;
            case R.id.navigation_data:
                if (data_layout.getVisibility()!=View.VISIBLE) {
                    new Thread(getRFMData).start();
                    new Thread(getHotWord).start();
                    title.setText(R.string.title_data);
                    home_layout.setVisibility(View.GONE);
                    data_layout.setVisibility(View.VISIBLE);
                    monitor_layout.setVisibility(View.GONE);
                }
                return true;
            case R.id.navigation_monitor:
                if (monitor_layout.getVisibility()!=View.VISIBLE) {
                    new Thread(getMonitorData).start();
                    title.setText(R.string.title_monitor);
                    home_layout.setVisibility(View.GONE);
                    data_layout.setVisibility(View.GONE);
                    monitor_layout.setVisibility(View.VISIBLE);
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("finish_manager_activity");
        registerReceiver(broadcastReceiver,intentFilter);
    }

    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
}
