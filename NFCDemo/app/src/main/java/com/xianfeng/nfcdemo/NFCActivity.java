package com.xianfeng.nfcdemo;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;

import android.widget.Toast;
import java.nio.charset.Charset;

//继承并实现接口CreateNdefMessageCallback方法createNdefMessage
public class NFCActivity extends Activity implements CreateNdefMessageCallback {
    NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        //检测是否有NFC适配器
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        //注册回调函数
        mNfcAdapter.setNdefPushMessageCallback(this, this);

        System.out.println("NFC开发");
    }


    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = ("Beam me up, Android!\n\n" +
                "Beam Time: " + System.currentTimeMillis());
        //回调函数，构造NdefMessage格式
        NdefMessage msg = new NdefMessage(
                new NdefRecord[]{
                        createMime("app/com.xianfeng.nfcdemo", text.getBytes())
                });
        return msg;
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     */
    public NdefRecord createMime(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        System.out.println("接收到通知：mimeType:" + mimeType);
        return mimeRecord;
    }



    //重载Activity类方法处理当新Intent到来事件
    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        //得到是否检测到ACTION_NDEF_DISCOVERED触发
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    /**
     * 从intent事件中解析 NDEF 消息
     */
    //关键处理函数，处理扫描到的NdefMessage
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        System.out.println(new String(msg.getRecords()[0].getPayload()));
    }


}
