package com.xianfeng.Util;


/**
 * Created by xianfeng on 2016/11/14.
 */

///session
//卡片逻辑
public class RFInteraction {

    //RESPONSE CODE
    static final String RAPDU_CODE_COMPLETED = "9000";
    static final String RAPDU_CODE_WRONGLENGTH = "6700";
    static final String RAPDU_CODE_SECURITY_STATUS_NOT_SATISFIED = "6982";
    static final String RAPDU_CODE_GPO_NOT_CONFIGURED = "6A80"; //The GPO is not configured as an interrupt mode
    static final String RAPDU_CODE_NOT_FOUND = "6A82"; //FILE OR APPLICATION
    static final String RAPDU_CODE_P1OR2_INCORRECT = "6A86";
    static final String RAPDU_CODE_NOT_SUPPORTED = "6E00";

    //ExtendedReadBinary command
    String ExtendedReadBinaryCommand(String offset){
        return "";
    }

    //EnablePermanentState/DisablePermanentState command
    static final String ENABLE_CODE_READONLY = "A2280001";
    static final String DISABLE_CODE_READONLY = "A2260001";
    static final String ENABLE_CODE_WRITEONLY = "A2280002";
    static final String DISABLE_CODE_WRITEONLY = "A2260002";

    //SendInterrupt command
    static final String INTERRUPT_COMMAND = "A2D6001E00";

    //





    //访问命令
    String readCommondAttach(String data){
        String comondIwant = "";

        return comondIwant;
    }

    String writeCommandAttach(String data){
        String comondIwant = "";

        return comondIwant;
    }


    //读写
    String readBlock(int start,int length){

        return "";
    }

    void writeBlock(int start,int length,String data){

    }
}











