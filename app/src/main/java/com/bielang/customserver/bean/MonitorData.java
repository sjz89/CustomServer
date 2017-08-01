package com.bielang.customserver.bean;

public class MonitorData {
    private int mId;
    private int mNumber;
    private boolean isOnline;
    public MonitorData(int id,int number,boolean isOnline){
        this.mId=id;
        this.mNumber=number;
        this.isOnline=isOnline;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getId() {

        return mId;
    }

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        this.mNumber = number;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
