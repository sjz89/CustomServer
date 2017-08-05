package com.bielang.customserver.util;


import com.bielang.customserver.MyApplication;
import com.bielang.customserver.bean.CustomerInfo;
import com.bielang.customserver.bean.UserInfo;
import com.bielang.customserver.bean.WorkList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

/**
 * post请求
 * Created by Daylight on 2017/7/14.
 */

public class HttpPost {
    public static final String url="http://192.168.155.1/channel/";
    private static String getPost(String path,String data){
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Charset","UTF-8");

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.connect();
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.write(data.getBytes());
            outputStream.flush();
            outputStream.close();
            //获得结果码
            int responseCode = connection.getResponseCode();
            if(responseCode ==200){
                //请求成功
                InputStream is = connection.getInputStream();
                return IOUtils.toString(is);
            }else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String login_service(int userId,String password){
        String path = url+"customerservice_loginValidate.action";
        String data="loginid="+userId+"&loginpassword="+password;
        String value=getPost(path,data);
        if (value!=null&&value.equals("success"))
            get_service_info(userId);
        return value;
    }
    public static String login_manager(int userId,String password){
        String path = url + "company_loginValidate.action";
        String data="loginid="+userId+"&loginpassword="+password;
        String value=getPost(path,data);
        if (value!=null&&value.equals("success")) {
            UserInfo userInfo=new UserInfo(userId,"管理员","小浪","别浪","男");
            userInfo.setCompanyId(userId);
            userInfo.setPhone("13568478625");
            userInfo.setRegisttime("2017-7-20 20.00.00");
            MyApplication.getInstance().setMyInfo(userInfo);
        }
        return value;
    }
    private static void get_service_info(int id){
        String path = url+"customerserviceInfo_getCustomerserviceinfo.action";
        String data="id="+id;
        String jsonStr=getPost(path,data);
        if (jsonStr!=null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonStr);
                String userInfo = jsonObject.getString("customerserviceinfo");
                Gson gson = new GsonBuilder().create();
                UserInfo mInfo = gson.fromJson(userInfo, UserInfo.class);
                MyApplication.getInstance().setMyInfo(mInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public static void change_user_info(){
        String path=url+"customerservice_editService.action";
        UserInfo myInfo=MyApplication.getInstance().getMyInfo();
        String data="csId="+myInfo.getId()+"&csUsername="+myInfo.getUsername()+
                "&csName="+myInfo.getName()+"&csPhone="+myInfo.getPhone()+"&csSex="+myInfo.getSex();
        getPost(path,data);
    }
    public static void get_customer_info(int customer_id){
        String path = url+"customerInfo_getCustomerinfo.action";
        String data="id="+customer_id;
        String jsonStr=getPost(path,data);
        try {
            JSONObject object=new JSONObject(jsonStr);
            Gson gson=new GsonBuilder().create();
            CustomerInfo customerInfo=gson.fromJson(object.getString("customerinfo"),CustomerInfo.class);
            Realm realm=Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(customerInfo);
            realm.commitTransaction();
            realm.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void end_chat(int customer_id){
        String path = url+"conversation_conversationEnd.action";
        String data="customer_id="+customer_id+"&customerservice_id="+
                MyApplication.getInstance().getMyInfo().getId()+"&msgtype_id"+1307;
        getPost(path,data);
    }
    public static void send_order(String data){
        String path=url+"order_confirm.action";
        getPost(path,data);
    }
    private static String get_orderCount(int customerService_id){
        String path=url+"order_getTotalCount.action";
        String data="customerserviceId="+customerService_id;
        return getPost(path,data);
    }
    public static void get_total_order(int customerService_id){
        String path=url+"order_getTotalOrder.action";
        String jsonData=get_orderCount(customerService_id);
        if (jsonData!=null) {
            try {
                JSONObject object = new JSONObject(jsonData);
                int total = object.getInt("total");
                String data = "limit=" + total + "&offset=" + 0 + "&customerserviceId=" + customerService_id;
                String order= getPost(path, data);
                JSONObject jsonObject;
                if (order != null) {
                    jsonObject = new JSONObject(order);
                    String jsonArray = jsonObject.getString("rows");
                    JSONArray array = new JSONArray(jsonArray);
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    Gson gson = new GsonBuilder().create();
                    for (int i = 0; i < total; i++) {
                        realm.copyToRealmOrUpdate(gson.fromJson(array.getString(i), WorkList.class));
                    }
                    realm.commitTransaction();
                    realm.close();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public static void commit_order(int id){
        String path=url+"order_finishByIds.action";
        String data="ids="+id;
        getPost(path,data);
    }
    public static void get_company_order(){
        String path=url+"order_getCompanyOrder.action";
        String data="limit=0&offset=0&company_id="+MyApplication.getInstance().getMyInfo().getCompanyId();
        String number=getPost(path,data);
        if (number!=null) {
            try {
                JSONObject jsonObject = new JSONObject(number);
                int total = jsonObject.getInt("total");
                data = "limit=" + total + "&offset=0&company_id=" + MyApplication.getInstance().getMyInfo().getCompanyId();
                String jsonData = getPost(path, data);
                jsonObject = new JSONObject(jsonData);
                JSONArray jsonArray = new JSONArray(jsonObject.getString("rows"));
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                Gson gson = new GsonBuilder().create();
                for (int i = 0; i < total; i++) {
                    realm.copyToRealmOrUpdate(gson.fromJson(jsonArray.getString(i), WorkList.class));
                }
                realm.commitTransaction();
                realm.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public static String update_hot_words(){
        String path=url+"conversation_getHotWord.action";
        String data="companyid="+MyApplication.getInstance().getMyInfo().getCompanyId();
        return getPost(path,data);
    }
    private static String get_goods_id(int customer_id){
        String path=url+"collaborativeFiltering_getRecommendProductByService.action";
        String data="customer_id="+customer_id+"&msgtype_id="+1310;
        return getPost(path,data);
    }
    public static String get_goods_info(int customer_id){
        String path=url+"product_getProduct.action";
        String jsonStr=get_goods_id(customer_id);
        if (jsonStr!=null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonStr);
                int goods_id = Integer.parseInt(jsonObject.getString("content"));
                String data = "id=" + goods_id;
                return getPost(path, data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static String get_rfm_data(){
        String path=url+"customerrfm_rfmScatter.action";
        String data="companyid="+1122;
        return getPost(path,data);
    }
    public static void connectWebSocket(int id){
        String path=url+"customerservice_serviceConnectWebSocket.action";
        String data="model.csId="+id;
        getPost(path,data);
    }
    public static void disconnectWebSocket(int id){
        String path=url+"customerservice_serviceCutWebSocket.action";
        String data="model.csId="+id;
        getPost(path,data);
    }
    public static String getMonitorData(){
        String path=url+"monitor_getCompanyMonitor.action";
        String data="cpId="+MyApplication.getInstance().getMyInfo().getCompanyId();
        return getPost(path,data);
    }
    public static String getCustomerServiceState(){
        String path=url+"monitor_getCustomerState.action";
        String data="limit=50&offset=0&cpId="+MyApplication.getInstance().getMyInfo().getCompanyId();
        return getPost(path,data);
    }
    public static String search_knowledge_base(String content,int first_result,int max_result){
        String path=url+"knowledgebase_searchByCompany.action";
        String data="queryString="+content+"&firstResult="+first_result+"&maxResult="+max_result+
                "&company_id="+MyApplication.getInstance().getMyInfo().getCompanyId();
        return getPost(path,data);
    }
    public static String get_quick_reply_msg(){
        String path=url+"quickmessage_pageQuery.action";
        String data="limit=20&offset=0&serviceid="+MyApplication.getInstance().getMyInfo().getId();
        return getPost(path,data);
    }

}
