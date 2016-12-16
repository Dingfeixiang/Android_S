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


public class NFCMiddleware {

    private NFCTag tag_;
    private stnfcndefhandler _mndefMessageHandler;

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


//    NFCTag newTag = NFCApplication.getApplication().getCurrentTag();
//    public NDEFSimplifiedMessage resolvTag(NFCTag newTag){
//
//        NDEFSimplifiedMessage newMsg = null;
//
//        final int SUCCESS_RES = 1;
//        int retRes = SUCCESS_RES;
//
//        Log.v(this.getClass().getName(), "updateSmartFragment entry ..");
//        //if (newTag.getM_ModelChanged() == 1) return;
//        // return new instance according to current tag - if not null
//        // - Check if NDEF data are present for current tag
//        int currentValideTLVBlockID = newTag.getCurrentValideTLVBlokID();
//        if (currentValideTLVBlockID == -1) {
//            // FBE newTag.decodeTag();
//            retRes = newTag.decodeTag();
//        }
//
//        if ( retRes!= SUCCESS_RES) {
//            Log.v(this.getClass().getName(), "updateSmartFragment decodeTag failled ..");
//        } else{
//            NDEFSimplifiedMessageHandler ndefSimpleMsgHandler
//                    = newTag.getNDEFSimplifiedHandler(currentValideTLVBlockID);
//            if (ndefSimpleMsgHandler != null) {
//                newMsg = newTag.getNDEFSimplifiedHandler(currentValideTLVBlockID).getNDEFSimplifiedMessage();
//            }
//        }
//
//        return newMsg;
//    }
//    void resolveMessage(NDEFSimplifiedMessage newMsg){
//        NDEFSimplifiedMessageType newMsgType = NDEFSimplifiedMessageType.NDEF_SIMPLE_MSG_TYPE_EMPTY;
//        if (newMsg != null) {
//            newMsgType = newMsg.getType();
//            NDEFSimplifiedMessageHandler.getStrFromMsgType(newMsgType);
//        }
//        switch (newMsgType) {
//            case NDEF_SIMPLE_MSG_TYPE_TEXT:
//                if (newMsg != null) {
//
//                }
//                break;
//            default:
//                // Nothing to do... for the moment
//                // Just hide current fragment, if not already hidden
//        }
//    }

}
