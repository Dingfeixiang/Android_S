package com.xianfeng.sanyademo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by xianfeng on 2016/10/19.
 */

@DatabaseTable(tableName = "tb_chargedata")
public class ChargeData {

    @DatabaseField(generatedId=true)
    private int userId;
    //价格
    @DatabaseField
    private String priceno; //价格编码
    @DatabaseField
    private String pricename; //价格名称
    @DatabaseField
    private String pricestartdate; //价格启用时间
    @DatabaseField
    private String pricever; //价格体系版本号
    @DatabaseField
    private String pricecycle; //价格周期;单位月
    @DatabaseField
    private String cyclestartdate; //周期启用时间
    @DatabaseField
    private String clearflag; //累用量清除标识.0为不清除.1为清除
    @DatabaseField
    private String laddprice1; //阶梯价格1
    @DatabaseField
    private String laddvalue1; //阶梯限量1
    @DatabaseField
    private String laddprice2; //阶梯价格2
    @DatabaseField
    private String laddvalue2; //阶梯限量2
    @DatabaseField
    private String laddprice3; //阶梯价格3

    public ChargeData(){}

    public String getPricename() {
        return pricename;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setPricename(String pricename) {
        this.pricename = pricename;
    }

    public String getPriceno() {
        return priceno;
    }

    public void setPriceno(String priceno) {
        this.priceno = priceno;
    }

    public String getPricestartdate() {
        return pricestartdate;
    }

    public void setPricestartdate(String pricestartdate) {
        this.pricestartdate = pricestartdate;
    }

    public String getPricever() {
        return pricever;
    }

    public void setPricever(String pricever) {
        this.pricever = pricever;
    }

    public String getPricecycle() {
        return pricecycle;
    }

    public void setPricecycle(String pricecycle) {
        this.pricecycle = pricecycle;
    }

    public String getCyclestartdate() {
        return cyclestartdate;
    }

    public void setCyclestartdate(String cyclestartdate) {
        this.cyclestartdate = cyclestartdate;
    }

    public String getClearflag() {
        return clearflag;
    }

    public void setClearflag(String clearflag) {
        this.clearflag = clearflag;
    }

    public String getLaddprice1() {
        return laddprice1;
    }

    public void setLaddprice1(String laddprice1) {
        this.laddprice1 = laddprice1;
    }

    public String getLaddvalue1() {
        return laddvalue1;
    }

    public void setLaddvalue1(String laddvalue1) {
        this.laddvalue1 = laddvalue1;
    }

    public String getLaddprice2() {
        return laddprice2;
    }

    public void setLaddprice2(String laddprice2) {
        this.laddprice2 = laddprice2;
    }

    public String getLaddvalue2() {
        return laddvalue2;
    }

    public void setLaddvalue2(String laddvalue2) {
        this.laddvalue2 = laddvalue2;
    }

    public String getLaddprice3() {
        return laddprice3;
    }

    public void setLaddprice3(String laddprice3) {
        this.laddprice3 = laddprice3;
    }
}
