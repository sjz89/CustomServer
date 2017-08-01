package com.bielang.customserver.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 客户信息
 * Created by Daylight on 2017/7/26.
 */

public class CustomerInfo extends RealmObject{
    @PrimaryKey
    private int id;
    private String username;
    private String name;
    private String phone;
    private String mailbox;
    private String sex;
    private double level;
    private String registtime;

    private String ipaddr;
    private String area;
    private String logintime;
    private String referer;
    private String browserinfo;
    private String hostname;
    private String systeminfo;
    private String macaddr;
    private String region;
    private String referkeyword;

    private String lastconversationtime;
    private String lastconversationfirstkeyword;
    private String lastconversationkeyword;



    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getMailbox() {
        return mailbox;
    }
    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public double getLevel() {
        return level;
    }
    public void setLevel(double level) {
        this.level = level;
    }
    public String getRegisttime() {
        return registtime;
    }
    public void setRegisttime(String registtime) {
        this.registtime = registtime;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getIpaddr() {
        return ipaddr;
    }
    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }
    public String getArea() {
        return area;
    }
    public void setArea(String area) {
        this.area = area;
    }
    public String getLogintime() {
        return logintime;
    }
    public void setLogintime(String logintime) {
        this.logintime = logintime;
    }
    public String getReferer() {
        return referer;
    }
    public void setReferer(String referer) {
        this.referer = referer;
    }
    public String getBrowserinfo() {
        return browserinfo;
    }
    public void setBrowserinfo(String browserinfo) {
        this.browserinfo = browserinfo;
    }
    public String getHostname() {
        return hostname;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public String getSysteminfo() {
        return systeminfo;
    }
    public void setSysteminfo(String systeminfo) {
        this.systeminfo = systeminfo;
    }
    public String getMacaddr() {
        return macaddr;
    }
    public void setMacaddr(String macaddr) {
        this.macaddr = macaddr;
    }
    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }
    public String getReferkeyword() {
        return referkeyword;
    }
    public void setReferkeyword(String referkeyword) {
        this.referkeyword = referkeyword;
    }
    public String getLastconversationtime() {
        return lastconversationtime;
    }
    public void setLastconversationtime(String lastconversationtime) {
        this.lastconversationtime = lastconversationtime;
    }

    public String getLastconversationfirstkeyword() {
        return lastconversationfirstkeyword;
    }
    public void setLastconversationfirstkeyword(String lastconversationfirstkeyword) {
        this.lastconversationfirstkeyword = lastconversationfirstkeyword;
    }
    public String getLastconversationkeyword() {
        return lastconversationkeyword;
    }
    public void setLastconversationkeyword(String lastconversationkeyword) {
        this.lastconversationkeyword = lastconversationkeyword;
    }


}