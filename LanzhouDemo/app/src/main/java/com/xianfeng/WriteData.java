package com.xianfeng;

import java.util.Date;
import java.util.Map;

/**
 * Created by xianfeng on 2016/11/25.
 */

public class WriteData {

    Date date;
    Integer buycount;
    Integer gases;
    Map userData;

    String password;
    String pwstr;

    byte[] outbuf;

    public WriteData(){}

    public WriteData(Date date,Integer buycount,Integer gases){
        this.date = date;
        this.buycount = buycount;
        this.gases = gases;
    }

    public Map getUserData() {
        return userData;
    }

    public void setUserData(Map userData) {
        this.userData = userData;
    }

    public byte[] getOutbuf() {
        return outbuf;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public void setGases(Integer gases) {
        this.gases = gases;
    }

    public void setBuycount(Integer buycount) {

        this.buycount = buycount;
    }



}
