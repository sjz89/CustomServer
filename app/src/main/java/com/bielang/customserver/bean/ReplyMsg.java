package com.bielang.customserver.bean;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ReplyMsg extends RealmObject{
    @PrimaryKey
    private long id;
    private String msg;

    public ReplyMsg(){}
    public ReplyMsg(String msg) {
        id=new Date().getTime();
        this.msg = msg;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
