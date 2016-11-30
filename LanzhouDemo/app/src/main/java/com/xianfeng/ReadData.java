package com.xianfeng;

import android.content.Intent;
import android.renderscript.Double2;

/**
 * Created by xianfeng on 2016/11/25.
 */

public class ReadData {

    String userno; //用户号
    String corpno; //公司号
    String meterno; //
    Integer buycount; //购气次数
    Integer cardgases; //用户气量
    Double gasfee; //
    String buygasdate; //购气时间

    Integer errorcode;

    public Integer getErrorcode() {
        return errorcode;
    }

    public String getBuygasdate() {

        return buygasdate;
    }

    public Double getGasfee() {

        return gasfee;
    }

    public Integer getCardgases() {

        return cardgases;
    }

    public Integer getBuycount() {

        return buycount;
    }

    public String getMeterno() {
        return meterno;
    }

    public String getCorpno() {

        return corpno;
    }

    public String getUserno() {

        return userno;
    }
}
