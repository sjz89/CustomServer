package com.bielang.customserver.bean;

public class SendMessage {
    private int customerservice_id;
    private int customer_id;
    private int msgtype_id;
    private String content;
    public SendMessage(int cs_id,int c_id,int type_id,String content){
        this.customerservice_id=cs_id;
        this.customer_id=c_id;
        this.msgtype_id=type_id;
        this.content=content;
    }

    public void setCustomerservice_id(int customerservice_id) {
        this.customerservice_id = customerservice_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public void setMsgtype_id(int msgtype_id) {
        this.msgtype_id = msgtype_id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCustomerservice_id() {
        return customerservice_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public int getMsgtype_id() {
        return msgtype_id;
    }

    public String getContent() {
        return content;
    }
}
