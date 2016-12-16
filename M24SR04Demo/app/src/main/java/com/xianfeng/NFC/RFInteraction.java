package com.xianfeng.NFC;

import com.st.nfc4.Type4TagOperationM24SR;
/**
 * Created by xianfeng on 2016/11/14.
 */

///session
//RF芯片逻辑
public class RFInteraction {

    //这个工具类有猫腻
    protected Type4TagOperationM24SR m_Type4TagOperationM24SR;

    //RESPONSE CODE
    static final String RAPDU_CODE_COMPLETED = "9000";
    static final String RAPDU_CODE_WRONGLENGTH = "6700";
    static final String RAPDU_CODE_SECURITY_STATUS_NOT_SATISFIED = "6982";
    static final String RAPDU_CODE_GPO_NOT_CONFIGURED = "6A80"; //The GPO is not configured as an interrupt mode
    static final String RAPDU_CODE_NOT_FOUND = "6A82"; //FILE OR APPLICATION
    static final String RAPDU_CODE_P1OR2_INCORRECT = "6A86";
    static final String RAPDU_CODE_NOT_SUPPORTED = "6E00";

//    //ExtendedReadBinary command
//    String ExtendedReadBinaryCommand(String offset){
//        return "";
//    }

    //EnablePermanentState/DisablePermanentState command
    static final String ENABLE_CODE_READONLY = "A2280001";
    static final String DISABLE_CODE_READONLY = "A2260001";
    static final String ENABLE_CODE_WRITEONLY = "A2280002";
    static final String DISABLE_CODE_WRITEONLY = "A2260002";

    //SendInterrupt command
    static final String INTERRUPT_COMMAND = "A2D6001E00";

    //instruction code
    static final int SYSTEMSELECT = 0XA4;
    static final int READBINARY = 0XB0;
    static final int UPDATEBINARY = 0XD6;


    //读写
    public void writeBlock(int start,int length,byte[] data){

    }

    public byte[] readBlock(int start,int length){
        byte[] var = new byte[4];

        return var;
    }
}











