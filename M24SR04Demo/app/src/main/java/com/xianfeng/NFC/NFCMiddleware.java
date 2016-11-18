package com.xianfeng.NFC;

import android.content.Intent;
import android.util.Log;

import com.st.NDEF.NDEFSimplifiedMessage;
import com.st.NDEF.NDEFSimplifiedMessageHandler;
import com.st.NDEF.NDEFSimplifiedMessageType;
import com.st.NDEF.stnfcndefhandler;
import com.st.NFC.NFCApplication;
import com.st.NFC.NFCTag;

/**
 * Created by xianfeng on 2016/11/15.
 */

//中间件
public class NFCMiddleware {

    private Intent intent_;
    private stnfcndefhandler _mndefMessageHandler;

    NFCMiddleware(Intent intent){
        intent_ = intent;
    }

    private void manageSmartNdefArrayData(Intent intent) {
        byte[] barray = intent.getByteArrayExtra("ndefbyteArray");
        String ndefclassfactory = intent.getStringExtra("ndefclass");
        if (barray ==null || ndefclassfactory == null) {
            _mndefMessageHandler = null;
            // issue with data to write
        } else {
            _mndefMessageHandler = new stnfcndefhandler(barray,(short)0);
        }
    }

    NFCTag newTag = NFCApplication.getApplication().getCurrentTag();

    public NDEFSimplifiedMessage resolvTag(NFCTag newTag){

        NDEFSimplifiedMessage newMsg = null;

        final int SUCCESS_RES = 1;
        int retRes = SUCCESS_RES;

        Log.v(this.getClass().getName(), "updateSmartFragment entry ..");
        //if (newTag.getM_ModelChanged() == 1) return;
        // return new instance according to current tag - if not null
        // - Check if NDEF data are present for current tag
        int currentValideTLVBlockID = newTag.getCurrentValideTLVBlokID();
        if (currentValideTLVBlockID == -1) {
            // FBE newTag.decodeTag();
            retRes = newTag.decodeTag();
        }

        if ( retRes!= SUCCESS_RES) {
            Log.v(this.getClass().getName(), "updateSmartFragment decodeTag failled ..");
        } else{
            NDEFSimplifiedMessageHandler ndefSimpleMsgHandler
                    = newTag.getNDEFSimplifiedHandler(currentValideTLVBlockID);
            if (ndefSimpleMsgHandler != null) {
                newMsg = newTag.getNDEFSimplifiedHandler(currentValideTLVBlockID).getNDEFSimplifiedMessage();
            }
        }

        return newMsg;
    }



    void resolveMessage(NDEFSimplifiedMessage newMsg){
        NDEFSimplifiedMessageType newMsgType = NDEFSimplifiedMessageType.NDEF_SIMPLE_MSG_TYPE_EMPTY;
        if (newMsg != null) {
            newMsgType = newMsg.getType();
            NDEFSimplifiedMessageHandler.getStrFromMsgType(newMsgType);
        }
        switch (newMsgType) {
            case NDEF_SIMPLE_MSG_TYPE_TEXT:
                if (newMsg != null) {

                }
                break;
            default:
                // Nothing to do... for the moment
                // Just hide current fragment, if not already hidden
        }
    }

    public byte[] readTag(){
        return new byte[0];
    }

    public void writeTag(){
        NDEFSimplifiedMessage msgToWrite;
    }

}








/*
* // 获取随机数的APDU命令
    private static final byte[] GET_RANDOM = {0x00, (byte)0x84, 0x00, 0x00, 0x08};
    // 声明ISO-DEP协议的Tag操作实例
    private final IsoDep tag;

    public NFCMiddleware(IsoDep tag) throws IOException {
        // 初始化ISO-DEP协议的Tag操作类实例
        this.tag = tag;
        tag.setTimeout(5000);
        tag.connect();
    }

    /**
     * 向Tag发送获取随机数的APDU并返回Tag响应
     * @return 十六进制随机数字符串
     * @throws IOException
    //     * @throws APDUError

    // 获取随机数的APDU命令
    public String send() throws IOException {
        // 发送APDU命令
        byte[] resp = tag.transceive(GET_RANDOM);
        String strResp =  new String(DigitalTrans.byte2hex(resp));
        Log.d("REQ ", new String(DigitalTrans.byte2hex(GET_RANDOM)));
        Log.d("RESP", new String(DigitalTrans.byte2hex(resp)));
        // 获取NFC Tag返回的状态值
        int status = ((0xff & resp[resp.length - 2]) << 8) | (0xff & resp[resp.length - 1]);
        if (status != 0x9000) {
    //            throw new APDUError(status);
            System.out.println("ST 发送失败");
        }
        return strResp;
    }
*/
