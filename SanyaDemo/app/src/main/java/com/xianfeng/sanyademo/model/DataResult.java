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
    public AreaData areaData = null;
    public ChargeData chargeData = null;
    public GasData gasData = null;

    public String username = ""; //用户姓名
    public String useraddress = ""; //用户地址
    public String tablenumber = ""; //表具编号

    public String cardNumberString = ""; //卡号
    public String userNumber = "";   //用户号

    public String companyno = "008001"; //公司号

    private String gasAmount = "0";   //换气表量
    public String getGasAmount(){
        return gasAmount;
    }
    public void setGasAmount(String amount){
        gasAmount = amount;
    }

    private float gasValue = 0;    //换算成金额
    public float getGasMoney(){
        return gasValue;
//        if (gasAmount.length() > 0){
//            try{
//                gasValue = Float.valueOf(gasAmount).floatValue(); //换算成金额
//            }catch (Exception ex){
//                gasValue = 0;
//            }
//            return gasValue;
//        }else {
//            return 0;
//        }
    }
    public void setGasMoney(Float value){
        gasValue = value;
    }
}

