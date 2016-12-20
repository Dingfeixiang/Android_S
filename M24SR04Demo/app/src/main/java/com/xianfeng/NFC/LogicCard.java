package com.xianfeng.NFC;

import java.sql.Struct;

/**
 * Created by xianfeng on 2016/12/15.
 */

//先锋卡
public class LogicCard{

    public static final int MAX_LENGTH = 256;

    public static final byte[] FRAME_START_SIGN = new byte[]{0x68};
    public static final byte[] FRAME_END_SIGN = new byte[]{0x16};
    /*
        *卡片功能
     */
    //读表命令
    public static final int COMMAND_READ_FUNCCODE = 0X01;
    public static final int COMMAND_READ_LEHGTH = 0X00;
    public static final byte[] COMMAND_READ_CONTENT = new byte[]{0x00};

    //阀门控制命令
    public static final int COMMAND_VALVECONTROL_FUNCCODE = 0X02;
    public static final int COMMAND_VALVECONTROL_LENGTH = 0X01;
    public static final byte[] COMMAND_VALVECONTROL_TURNON = new byte[]{0x55};
    public static final byte[] COMMAND_VALVECONTROL_TURNOFF = new byte[]{(byte) 0xAA};

    //充值命令
    public static final int COMMAND_RECHARGE_FUNCCODE = 0X03;
    public static final int COMMAND_VRECHARGE_LENGTH = 0X04;


    //控制码
    public byte[] controlCode(){return null;}

    //功能码
    public byte[] functionCode(){return null;}

    //校验码
    public byte[] verifyCode(){return null;}

}
