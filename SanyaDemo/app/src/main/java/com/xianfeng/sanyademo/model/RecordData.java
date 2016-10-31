package com.xianfeng.sanyademo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by xianfeng on 2016/10/19.
 */

@DatabaseTable(tableName = "tb_logrecord")

public class RecordData {

    @DatabaseField(generatedId=true)
    private int userId;

    @DatabaseField
    private String log;

    @DatabaseField
    private double gases;
    @DatabaseField
    private double gasfee;
    @DatabaseField
    private double price1;
    @DatabaseField
    private double price2;
    @DatabaseField
    private double price3;
    @DatabaseField
    private int laddgas1;
    @DatabaseField
    private int laddgas2;
    @DatabaseField
    private String pricedate;
    @DatabaseField
    private int pricetype;
    @DatabaseField
    private int pricever;
    @DatabaseField
    private int pricecycle;
    @DatabaseField
    private int clearflag;
    @DatabaseField
    private String cycledate;
    @DatabaseField
    private double newprice1;
    @DatabaseField
    private double newprice2;
    @DatabaseField
    private double newprice3;
    @DatabaseField
    private int newladdgas1;
    @DatabaseField
    private int newladdgas2;
    @DatabaseField
    private String newpricedate;
    @DatabaseField
    private int newpricetype;
    @DatabaseField
    private int newpricever;
    @DatabaseField
    private int newpricecycle;
    @DatabaseField
    private int newclearflag;
    @DatabaseField
    private String newcycledate;
    @DatabaseField
    private String meterno;
    @DatabaseField
    private String companyno;
    @DatabaseField
    private String cardno;

    public RecordData(){}

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getLog() {
        return log;
    }
    public void setLog(String log) {
        this.log = log;
    }

    public String getCardno() {
        return cardno;
    }

    public void setCardno(String cardno) {
        this.cardno = cardno;
    }

    public String getCompanyno() {
        return companyno;
    }

    public void setCompanyno(String companyno) {
        this.companyno = companyno;
    }

    public String getMeterno() {
        return meterno;
    }

    public void setMeterno(String meterno) {
        this.meterno = meterno;
    }

    public String getNewcycledate() {
        return newcycledate;
    }

    public void setNewcycledate(String newcycledate) {
        this.newcycledate = newcycledate;
    }

    public int getNewclearflag() {
        return newclearflag;
    }

    public void setNewclearflag(int newclearflag) {
        this.newclearflag = newclearflag;
    }

    public int getNewpricecycle() {
        return newpricecycle;
    }

    public void setNewpricecycle(int newpricecycle) {
        this.newpricecycle = newpricecycle;
    }

    public int getNewpricever() {
        return newpricever;
    }

    public void setNewpricever(int newpricever) {
        this.newpricever = newpricever;
    }

    public int getNewpricetype() {
        return newpricetype;
    }

    public void setNewpricetype(int newpricetype) {
        this.newpricetype = newpricetype;
    }

    public String getNewpricedate() {
        return newpricedate;
    }

    public void setNewpricedate(String newpricedate) {
        this.newpricedate = newpricedate;
    }

    public int getNewladdgas2() {
        return newladdgas2;
    }

    public void setNewladdgas2(int newladdgas2) {
        this.newladdgas2 = newladdgas2;
    }

    public int getNewladdgas1() {
        return newladdgas1;
    }

    public void setNewladdgas1(int newladdgas1) {
        this.newladdgas1 = newladdgas1;
    }

    public double getNewprice3() {
        return newprice3;
    }

    public void setNewprice3(double newprice3) {
        this.newprice3 = newprice3;
    }

    public double getNewprice2() {
        return newprice2;
    }

    public void setNewprice2(double newprice2) {
        this.newprice2 = newprice2;
    }

    public double getNewprice1() {
        return newprice1;
    }

    public void setNewprice1(double newprice1) {
        this.newprice1 = newprice1;
    }

    public String getCycledate() {
        return cycledate;
    }

    public void setCycledate(String cycledate) {
        this.cycledate = cycledate;
    }

    public int getClearflag() {
        return clearflag;
    }

    public void setClearflag(int clearflag) {
        this.clearflag = clearflag;
    }

    public int getPricecycle() {
        return pricecycle;
    }

    public void setPricecycle(int pricecycle) {
        this.pricecycle = pricecycle;
    }

    public int getPricever() {
        return pricever;
    }

    public void setPricever(int pricever) {
        this.pricever = pricever;
    }

    public int getPricetype() {
        return pricetype;
    }

    public void setPricetype(int pricetype) {
        this.pricetype = pricetype;
    }

    public String getPricedate() {
        return pricedate;
    }

    public void setPricedate(String pricedate) {
        this.pricedate = pricedate;
    }

    public int getLaddgas2() {
        return laddgas2;
    }

    public void setLaddgas2(int laddgas2) {
        this.laddgas2 = laddgas2;
    }

    public int getLaddgas1() {
        return laddgas1;
    }

    public void setLaddgas1(int laddgas1) {
        this.laddgas1 = laddgas1;
    }

    public double getPrice3() {
        return price3;
    }

    public void setPrice3(double price3) {
        this.price3 = price3;
    }

    public double getPrice2() {
        return price2;
    }

    public void setPrice2(double price2) {
        this.price2 = price2;
    }

    public double getPrice1() {
        return price1;
    }

    public void setPrice1(double price1) {
        this.price1 = price1;
    }

    public double getGasfee() {
        return gasfee;
    }

    public void setGasfee(double gasfee) {
        this.gasfee = gasfee;
    }

    public double getGases() {
        return gases;
    }

    public void setGases(double gases) {
        this.gases = gases;
    }
}
