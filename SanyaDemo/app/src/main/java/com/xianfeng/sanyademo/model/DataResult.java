package com.xianfeng.sanyademo.model;

import java.util.List;

/**
 * Created by xianfeng on 2016/10/21.
 */

//用于保存用户提交的开户信息
public class DataResult {
    //    private String areainfo = ""; //区域
//    private String priceinfo = ""; //价格
//    private String gastypeinfo = ""; //用气
//    private float gasValue = 0; //金额
//    private String cardNumberString = ""; //卡号
//    private String userNumber = ""; //用户号
    public AreaData areaData;
    public ChargeData chargeData;
    public GasData gasData;

    public String username; //用户姓名
    public String useraddress; //用户地址
    public String tablenumber; //表具编号
    public String gasAmount;   //换气表量

    public String cardNumberString; //卡号
    public String userNumber;   //用户号

    public String companyno = "008001"; //公司号

    private float gasValue;    //换算成金额
    public float getGasValue(){
        gasValue = Float.valueOf(gasAmount).floatValue(); //换算成金额
        return gasValue;
    }
    public void setGasValue(Float value){
        gasValue = value;
    }
}

