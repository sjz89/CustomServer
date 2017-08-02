package com.bielang.customserver.bean;

import com.bielang.customserver.util.DateUtil;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


/**
 *消息列表item数据对象
 * Created by Daylight on 2017/7/6.
 */

public class MsgList extends RealmObject{
    @PrimaryKey
    private int mId;
    private int csId;
    private String mName;
    private String mLastMsg;
    private Date mLastTime;
    private double level;
    private int mNewMsgNumber;
    private int mHeaderUrl;
    public MsgList(){}
    public MsgList(int csId,int id, String name, String lastMsg, Date lastTime){
        this.csId=csId;
        this.mId=id;
        this.mName=name;
        this.mLastMsg=lastMsg;
        this.mLastTime=lastTime;
        this.mNewMsgNumber=0;
    }

    public int getCsId() {
        return csId;
    }

    public void setCsId(int csId) {
        this.csId = csId;
    }

    public void setId(int id){
        this.mId=id;
    }
    public void setName(String name){
        this.mName=name;
    }
    public void setLastMsg(String lastMsg){
        this.mLastMsg=lastMsg;
    }
    public void setLastTime(Date lastTime){
        this.mLastTime=lastTime;
    }
    public void setNewMsgNumber(int newMsgNumber){
        this.mNewMsgNumber=newMsgNumber;
    }
    public void setHeader(int headerUrl){
        this.mHeaderUrl=headerUrl;
    }
    public int getId(){
        return this.mId;
    }
    public String getName(){
        return this.mName;
    }
    public String getLastMsg(){
        return this.mLastMsg;
    }
    public Date getDate(){
        return this.mLastTime;
    }
    public String getLastTime(){
        return DateUtil.AutoTransFormat(this.mLastTime);
    }
    public int getNewMsgNumber(){
        return this.mNewMsgNumber;
    }
    public int getHeader(){
        return this.mHeaderUrl;
    }
    public double getLevel() {
        return level;
    }
    public void setLevel(double level) {
        this.level = level;
    }

}
