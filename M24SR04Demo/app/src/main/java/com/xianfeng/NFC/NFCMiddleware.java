package com.xianfeng.NFC;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;

import com.st.NDEF.NDEFSimplifiedMessage;
import com.st.NDEF.NDEFSimplifiedMessageHandler;
import com.st.NDEF.NDEFSimplifiedMessageType;
import com.st.NDEF.NDEFTextMessage;
import com.st.NDEF.stndefwritestatus;
import com.st.NDEF.stnfcndefhandler;
import com.st.NFC.NFCApplication;
import com.st.NFC.NFCTag;
import com.xianfeng.Util.TextRecord;


/**
 * Created by xianfeng on 2016/11/15.
 * 中间件用于连接ST公司的库与本项目数据
 */

//该类为中间件，用于连接ST lib与先锋NFC库
//RFInteraction

public class NFCMiddleware {

    private NFCTag tag_;

    NFCMiddleware(NFCTag tag){
        tag_ = tag;
    }

    //获取tag数据
    public String readTag(NFCTag tag){
        String dataString = "";

        NdefMessage[] tagMsgs = tag.getNdefMessages();
        NdefMessage msgIwant;

        if (tagMsgs == null) return "";
        if (tagMsgs.length > 0){
            msgIwant = tagMsgs[tagMsgs.length -1];
            System.out.println(msgIwant.toString());

            if (msgIwant != null){
                NdefRecord[] records = msgIwant.getRecords();
                if (records.length > 0){

                    dataString = TextRecord.parse(records[records.length-1]).getText();

                    System.out.println(dataString);
                }else {
                    System.out.println("ndefrecord count is zero");
                }
            }else {
                System.out.println("get ndefmessage error");
            }

        }else {
            System.out.println("ndefmessage count is zero");
        }
        //            NDEFSimplifiedMessage tagSimpleMsg =
//            tmpTag.getNDEFSimplifiedHandler(tmpTag.getCurrentValideTLVBlokID()).getNDEFSimplifiedMessage();
//            NdefRecord[] records = tagSimpleMsg.getNDEFMessage().getRecords();
//            System.out.println(tagSimpleMsg.getNDEFMessage().toString());

        return dataString;
    }

    //组装数据
    private NDEFSimplifiedMessage msgToWrite(String msgData){
        NDEFTextMessage textMessage = new NDEFTextMessage();
        textMessage.setText(msgData);
        return textMessage;
    }

    //写入tag
    public boolean writeTag(NFCTag nfcTag,String data){
        //获取msg
        NDEFSimplifiedMessage msgToWrite = msgToWrite(data);
        stndefwritestatus status = nfcTag.writeNDEFMessage(msgToWrite);
        if (status == stndefwritestatus.WRITE_STATUS_OK){
            return true;
        }else {
            return false;
        }
    }

    //读写
    public boolean writeBlock(int start,int length,byte[] data){
        boolean writeRes = false;
        if (tag_ == null) return false;



        return writeRes;
    }

    public byte[] readBlock(int start,int length){
        byte[] var = new byte[4];

        return var;
    }

}
