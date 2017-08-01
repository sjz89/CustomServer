package com.bielang.customserver.bean;

import java.util.Date;

import io.realm.RealmObject;

/**
 *聊天界面item数据对象
 * Created by Daylight on 2017/7/6.
 */
public class ChatMessage extends RealmObject{
    public static final int MessageType_From=1;
    public static final int MessageType_To=2;
    public static final int MessageType_Hint=3;
    public static final int MessageType_End=4;
    public static final int MessageType_Goods=5;
    private int mId;
    private int csId;
    private int mType;
    private String mContent;
    private Date mDate;
    public ChatMessage(){}
    public ChatMessage(int csId,int id,int Type,String Content,Date Date){
        this.csId=csId;
        this.mId=id;
        this.mType=Type;
        this.mContent=Content;
        this.mDate=Date;
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
    public int getId(){
        return this.mId;
    }
    public void setType(int type){
        this.mType=type;
    }
    public int getType() {
        return mType;
    }
    public String getContent() {
        return mContent;
    }
    public void setContent(String mContent) {
        this.mContent = mContent;
    }
    public Date getDate(){
        return mDate;
    }
    public void setDate(Date date){
        this.mDate=date;
    }
}