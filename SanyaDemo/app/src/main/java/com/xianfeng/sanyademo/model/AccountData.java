package com.xianfeng.sanyademo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by xianfeng on 2016/10/19.
 */
@DatabaseTable(tableName = "tb_accountinfo")
public class AccountData extends User {

    @DatabaseField(generatedId=true)
    private int userId;
    //开户
    @DatabaseField
    private String accountName; //用户姓名
    @DatabaseField
    private String address; //用户地址

    @DatabaseField
    private String areaid; //区域编号*(带*号表需要判断)
    @DatabaseField
    private String priceno; //价格编码*
    @DatabaseField
    private String usergastype; //用户用气类型*
    @DatabaseField
    private String metercode; //表号*

    @DatabaseField
    private Float money;   //开卡初始量(气量转换成金额)


    //开户结果
    @DatabaseField
    private boolean establishResult; //开户结果
    @DatabaseField
    private String cardno; //卡号（失败为""）
    @DatabaseField
    private String systemno; //用户号（失败为""）

    public AccountData() {}

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMetercode() {
        return metercode;
    }

    public void setMetercode(String metercode) {
        this.metercode = metercode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAreaid() {
        return areaid;
    }

    public void setAreaid(String areaid) {
        this.areaid = areaid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPriceno() {
        return priceno;
    }

    public void setPriceno(String priceno) {
        this.priceno = priceno;
    }

    public String getUsergastype() {
        return usergastype;
    }

    public void setUsergastype(String usergastype) {
        this.usergastype = usergastype;
    }

    public Float getMoney() {
        return money;
    }

    public void setMoney(Float money) {
        this.money = money;
    }

    public boolean isEstablishResult() {
        return establishResult;
    }

    public void setEstablishResult(boolean establishResult) {
        this.establishResult = establishResult;
    }

    public String getCardno() {
        return cardno;
    }

    public void setCardno(String cardno) {
        this.cardno = cardno;
    }

    public String getSystemno() {
        return systemno;
    }

    public void setSystemno(String systemno) {
        this.systemno = systemno;
    }
}
