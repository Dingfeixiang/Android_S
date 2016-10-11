package com.xianfeng.nfcdemo;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.nio.charset.Charset;
import java.util.Map;


/*
    继承并实现接口CreateNdefMessageCallback 方法createNdefMessage
 */
public class NFCActivity extends AppCompatActivity
        implements CreateNdefMessageCallback {
    NfcAdapter mAdapter;
    TextView infoView;
    SendCommand sendConmand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        System.out.println("NFC开发");
        infoView = (TextView)findViewById(R.id.promt);

        //检测是否有NFC适配器
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Register callback
        //注册回调函数
        mAdapter.setNdefPushMessageCallback(this, this);
//        mAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                infoView.setText("请在系统设置中先启用NFC功能！");
                return;
            }

            infoView.setText("NFC功能已开启！");
            Log.d (TAG,"NFC已开启："+getIntent ().getAction ());
        }else {
            if (!mAdapter.isEnabled()) {

            }
            infoView.setText("设备不支持NFC！");
            return;
        }

        //得到是否检测到ACTION_NDEF_DISCOVERED触发
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    //重载Activity类方法处理当新Intent到来事件
    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }


//    @Override
//    public void onPause(){
//        super.onPause();
//        if (mAdapter != null) {
//            //隐式启动
//            mAdapter.disableForegroundDispatch(this);
//            mAdapter.disableForegroundNdefPush(this);
//        }
//    }

    /*
         NFC相关
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = "NFC";
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
                        "application/com.xianfeng.nfcdemo", text.getBytes())
                });
        return msg;
    }

    //Creates a custom MIME type encapsulated in an NDEF record
    public NdefRecord createMime(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        System.out.println("接收到通知：mimeType:" + mimeType);
        return mimeRecord;
    }


    private static final String TAG = "NFCActivity";

    //关键处理函数，处理扫描到的NdefMessage
    void processIntent(Intent intent) {

        //获取到Intent的Action，注意多打Log
        Log.d(TAG, "handleIntent: " + intent.getAction());
        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Log.d(TAG, "handleIntent: NDEF");
        }else if (intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)){
            Log.d(TAG, "handleIntent: TECH");
        }else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)){
            Log.d(TAG, "handleIntent: TAG");
        }else {
            Log.d(TAG, "handleIntent: no valid action");
        }

        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        infoView.setText(new String(msg.getRecords()[0].getPayload()));

    }


    /*
     *卡片处理
     */
    private static final String CARD_READ_NFC = "";
    private static final String CARD_WRITE_NFC = "";

    private class SendCommand implements Runnable {

        private Map<String, Object> param = null;
        private StringBuilder sBuilder = null;
        private Message msg = null;

        public SendCommand(Map<String, Object> param) {
            this.param = param;
        }

        @Override
        public void run() {
            int t = (Integer) param.get("TYPE");

            if (t == 0) {
                // 读卡
                readCard(CARD_READ_NFC);
            } else if (t == 1) {
                // 写卡
                writeCard(CARD_WRITE_NFC);
            }
        }

        private void readCard(String mHeader) {

        }

        private void writeCard(String mHeader) {

        }
        //卡片操作

    }

}
