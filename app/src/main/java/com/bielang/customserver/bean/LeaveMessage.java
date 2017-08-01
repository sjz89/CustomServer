package com.bielang.customserver.bean;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 留言
 * Created by Daylight on 2017/7/14.
 */

public class LeaveMessage extends RealmObject{
    @PrimaryKey
    private int mId;
    private int csId;
    private String mName;
    private String mMessage;
    private Date mDate;
    private RealmList<ReplyMsg> reply_msg;
    public LeaveMessage(){}
    public LeaveMessage(int csId,int id, String name, String message, Date date,RealmList<ReplyMsg> reply_msg){
        this.csId=csId;
        this.mId=id;
        this.mName=name;
        this.mMessage=message;
        this.mDate=date;
        this.reply_msg=reply_msg;
    }

    public int getCsId() {
        return csId;
    }

    public void setCsId(int csId) {
        this.csId = csId;
    }

    public RealmList<ReplyMsg> getReply_msg() {
        return reply_msg;
    }

    public void setReply_msg(RealmList<ReplyMsg> reply_msg) {
        this.reply_msg = reply_msg;
    }

    public void addReply_msg(String msg){
        this.reply_msg.add(new ReplyMsg(msg));
    }

    public void setId(int id) {
        this.mId = id;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getMessage() {
        return mMessage;
    }

    public Date getDate() {
        return mDate;
    }
}
