package com.bielang.customserver.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 客服信息
 * Created by Daylight on 2017/7/12.
 */

public class UserInfo extends RealmObject{
    @PrimaryKey
    private int id;
    private int companyid;
    private String status;
    private String username;
    private String name;
    private String sex;
    private String phone;
    private String registtime;
    private String mHeaderUrl;
    public UserInfo(){}
    public UserInfo(int id,String status,String username,String name,String sex){
        this.id=id;
        this.status=status;
        this.username=username;
        this.name=name;
        this.sex=sex;
    }
    public void setId(int id){
        this.id=id;
    }
    public int getId(){
        return this.id;
    }
    public int getCompanyId() {
        return companyid;
    }
    public void setCompanyId(int companyId) {
        this.companyid = companyId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setType(String status){
        this.status=status;
    }
    public String getType(){
        return this.status;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return this.name;
    }
    public void setSex(String sex){
        this.sex=sex;
    }
    public String getSex(){
        return this.sex;
    }

    public String getRegisttime() {
        return registtime;
    }

    public void setRegisttime(String registtime) {
        this.registtime = registtime;
    }

    public void setHeader(String headerUrl){
        this.mHeaderUrl=headerUrl;
    }
    public String getHeader(){
        return this.mHeaderUrl;
    }
}
