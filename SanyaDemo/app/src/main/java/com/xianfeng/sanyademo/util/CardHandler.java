package com.xianfeng.sanyademo.util;

/**
 * Created by xianfeng on 2016/10/25.
 */

//import cardInterface.CardInfo;

//卡片处理逻辑
public class CardHandler {

    private static final String CARD_CHECKPASS4442_COMMAND = "0103000003"; //发送读卡命令
    //发送写卡命令 写卡开始位置是20，长度是224
    private static final String CARD_WRITE4442_COMMAND_FOR_ONCE   = "01020020e0";
    private static final String CARD_CHANGPASS4442_COMMAND = "0105000003"; //密码较验

    private static final String SINGAL_RECEIVEDATA_SUCCESS = "9000";    //请求成功
    private static final String SINGAL_TIMEOUT = "6F06";    //超时

    public CardHandler(){}

    public boolean writeCard(Byte[] data){
        return false;
    }

    public Byte[] readCard(){
        return null;
    }



}
