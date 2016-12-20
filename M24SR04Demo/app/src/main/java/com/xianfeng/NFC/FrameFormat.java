package com.xianfeng.NFC;

/**
 * Created by xianfeng on 2016/12/16.
 */
//帧结构
public class FrameFormat {

    public static final int FRAME_MAX_LENGTH = 120;

    public FrameFormat(){

    }

    public FrameFormat(String dataHex, byte dataAreaLength_c, byte dataAreaLength_d){
        new FrameFormat(converTobyte(dataHex),dataAreaLength_c,dataAreaLength_d);
    }
    //参数依次为：数据，命令区数据域长度（最大108 0x6C），数据区数据长度（）
    public FrameFormat(byte[] data, byte dataAreaLength_c, byte dataAreaLength_d){

        commandStart_c = copyByte(data,0x00,1);
        controlCode_c = copyByte(data,0x01,1);
        commandLength_c = copyByte(data,0x02,1);
        tableAddress_c = copyByte(data,0x03,5);
        functionCode_c = copyByte(data,0x08,1);
        this.dataAreaLength_c = dataAreaLength_c;
        dataArea_c = copyByte(data,0x09,dataAreaLength_c);
        verifyCode_c = copyByte(data,0x09+dataAreaLength_c,2);
        commandEnd_c = copyByte(data,0x0B+dataAreaLength_c,1);

        dataStart_d = copyByte(data,0x78,1);
        controlCode_d = copyByte(data,0x79,1);
        dataLength_d = copyByte(data,0x7A,1);
        tableAddress_d = copyByte(data,0x7B,5);
        functionCode_d = copyByte(data,0x80,1);
        this.dataAreaLength_d = dataAreaLength_d;
        dataArea_d = copyByte(data,0x81,dataAreaLength_d);
        verifyCode_d = copyByte(data,0x81+dataAreaLength_d,2);
        dataEnd_d = copyByte(data,0x83+dataAreaLength_d,1);

    }

    //command
    byte[] commandStart_c   = new byte[1];          //命令起始符 0
    byte[] controlCode_c    = new byte[1];          //控制代码 1
    byte[] commandLength_c  = new byte[1];          //命令长度 2
    byte[] tableAddress_c   = new byte[5];          //表具地址 3
    byte[] functionCode_c   = new byte[1];          //功能代码 8
    byte   dataAreaLength_c = 0;                    //数据域长度
    byte[] dataArea_c;                              //数据域 9
    byte[] verifyCode_c     = new byte[2];          //CRC校验值 9+n
    byte[] commandEnd_c     = new byte[1];          //命令结束符 11+n

    //data
    byte[] dataStart_d      = new byte[1];          //命令起始符 120
    byte[] controlCode_d    = new byte[1];          //控制代码 121
    byte[] dataLength_d     = new byte[1];          //命令长度 122
    byte[] tableAddress_d   = new byte[5];          //表具地址 123
    byte[] functionCode_d   = new byte[1];          //功能代码 128
    byte   dataAreaLength_d = 0;                    //数据域长度
    byte[] dataArea_d;                              //数据域 129
    byte[] verifyCode_d     = new byte[2];          //CRC校验值，低字节在前 129+m
    byte[] dataEnd_d        = new byte[1];          //命令结束符 131+m



    //获取当前数据
    public byte[] getWholeData(){

        byte[] wholeData = new byte[12 + dataAreaLength_c + 12 + dataAreaLength_d];

        System.arraycopy(commandStart_c,0,wholeData,0x00,1);
        System.arraycopy(controlCode_c,0,wholeData,0x01,1);
        System.arraycopy(commandLength_c,0,wholeData,0x02,1);
        System.arraycopy(tableAddress_c,0,wholeData,0x03,5);
        System.arraycopy(functionCode_c,0,wholeData,0x08,1);
        System.arraycopy(dataArea_c,0,wholeData,0x09,dataAreaLength_c);
        System.arraycopy(verifyCode_c,0,wholeData,0x09+dataAreaLength_c,2);
        System.arraycopy(commandEnd_c,0,wholeData,0x0B+dataAreaLength_c,1);

        System.arraycopy(dataStart_d,0,wholeData,0x78,1);
        System.arraycopy(controlCode_d,0,wholeData,0x79,1);
        System.arraycopy(dataLength_d,0,wholeData,0x7A,1);
        System.arraycopy(tableAddress_d,0,wholeData,0x7B,1);
        System.arraycopy(functionCode_d,0,wholeData,0x80,1);
        System.arraycopy(dataArea_d,0,wholeData,0x81,dataAreaLength_d);
        System.arraycopy(verifyCode_d,0,wholeData,0x81+dataAreaLength_d,2);
        System.arraycopy(dataEnd_d,0,wholeData,0x83+dataAreaLength_d,1);

        return wholeData;
    }

    byte[] copyByte(byte[] data,int start,int length){
        byte[] byteIwant = new byte[length];
        System.arraycopy(data,start,byteIwant,0,length);
        return byteIwant;
    }
    byte[] converTobyte(String dataHex){
        byte[] bytes = dataHex.getBytes();
        return bytes;
    }
}




