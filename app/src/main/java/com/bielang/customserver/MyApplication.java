package com.bielang.customserver;

import android.app.Application;

import com.bielang.customserver.bean.UserInfo;
import com.facebook.stetho.Stetho;
import com.iflytek.cloud.SpeechUtility;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * 全局变量
 * Created by Daylight on 2017/7/12.
 */

public class MyApplication extends Application {
    private static MyApplication myApp;
    private static UserInfo myInfo;
    public static MyApplication getInstance() {
        return myApp;
    }

    @Override
    public void onCreate() {
        SpeechUtility.createUtility(this, "appid=597443f4");
        super.onCreate();
        myApp = this;
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("changlian.realm").build();
        Realm.setDefaultConfiguration(config);
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());
    }
    private void keepInfo(UserInfo mInfo){
        Realm realm=Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(mInfo);
        realm.commitTransaction();
        realm.close();
    }
    public void setMyInfo(int id,String type,String username,String name,String sex){
        myInfo=new UserInfo(id,type,username,name,sex);
        keepInfo(myInfo);
    }
    public void setMyInfo(UserInfo info){
        myInfo=info;
        keepInfo(myInfo);
    }

    public UserInfo getMyInfo(){
        return myInfo;
    }

    public void setHeader(String Url){
        myInfo.setHeader(Url);
        keepInfo(myInfo);
    }

}
