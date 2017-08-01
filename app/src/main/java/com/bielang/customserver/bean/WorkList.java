package com.bielang.customserver.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 工单列表
 * Created by Daylight on 2017/7/21.
 */

public class WorkList extends RealmObject{
    @PrimaryKey
    private int id;
    private int customerId;
    private int customerserviceId;
    private int company_id;
    private String demandaddr;
    private String demandotherrequest;
    private String demandphone;
    private String demandtime;
    private int product_id;
    private String status;
    private String time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCustomerserviceId() {
        return customerserviceId;
    }

    public void setCustomerserviceId(int customerserviceId) {
        this.customerserviceId = customerserviceId;
    }

    public int getCompany_id() {
        return company_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }

    public String getDemandaddr() {
        return demandaddr;
    }

    public void setDemandaddr(String demandaddr) {
        this.demandaddr = demandaddr;
    }

    public String getDemandotherrequest() {
        return demandotherrequest;
    }

    public void setDemandotherrequest(String demandotherrequest) {
        this.demandotherrequest = demandotherrequest;
    }

    public String getDemandphone() {
        return demandphone;
    }

    public void setDemandphone(String demandphone) {
        this.demandphone = demandphone;
    }

    public String getDemandtime() {
        return demandtime;
    }

    public void setDemandtime(String demandtime) {
        this.demandtime = demandtime;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
