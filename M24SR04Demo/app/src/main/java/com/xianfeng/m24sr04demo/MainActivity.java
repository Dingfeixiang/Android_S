package com.xianfeng.m24sr04demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.st.NFC.NFCApplication;
import com.st.NFC.NFCTag;
import com.xianfeng.NFC.*;

public class MainActivity extends AppCompatActivity {

    //控件
    TextView showView;
    Button  testButton;
    ProgressDialog dialog = null;

    //
    NFCManager nfcManager_ = new NFCManager();

    //读写标记
    boolean _readingFlag = false;
    boolean _writingFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showView = (TextView)findViewById(R.id.showview);
        testButton = (Button)findViewById(R.id.button);
        dialog = new ProgressDialog(MainActivity.this);

        // Animation in case of TAP tag request
        ImageView nfcWavesImg = (ImageView) findViewById(R.id.NfcWavesImgId);
        nfcWavesImg.setBackgroundResource(R.drawable.nfc_waves_anim);

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                nfcManager_.writeData("1234AAA", new NFCCallback.TagCallBack() {
                    @Override
                    public void currentTagStatus(NFCCallback.TagStatus status) {
                        if (status == NFCCallback.TagStatus.TAG_EMPTY){
                            loadingForWrite();
                        }
                    }
                });
            }
        });
        nfcManager_.initAdapter(this);

        _readingFlag = true;
        _writingFlag = false;
//        onNewIntent(getIntent());
    }

    void loadingForWrite(){
        // And write NDEF message to current tag
        dialog.setMessage(getString(R.string.nfc_act_tag_wait_for_Tapping));
        dialog.show();
    }


    //程序恢复
    @Override
    protected void onResume() {
        super.onResume();
        nfcManager_.enableForegroundDispatch(this);
    }

    //程序暂停
    @Override
    protected void onPause() {
        super.onPause();
        nfcManager_.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equalsIgnoreCase("android.intent.action.MAIN")) {
            return;
        }else {
            NFCTag nfcTag = nfcManager_.getNFCTag(intent);
            NFCApplication.getApplication().setCurrentTag(nfcTag);

            if (_readingFlag){
                nfcManager_.readData(intent, new NFCCallback.ReadCallBack() {
                    @Override
                    public void readFinish(String something) {
                        Toast.makeText(getApplicationContext(),something, Toast.LENGTH_LONG).show();
                    }
                });
            }

            if (_writingFlag){
                nfcManager_.writeData("1234AA", new NFCCallback.TagCallBack() {
                    @Override
                    public void currentTagStatus(NFCCallback.TagStatus status) {
                        if (status == NFCCallback.TagStatus.TAG_EMPTY){
                            Toast.makeText(getApplicationContext(),"写入失败！", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

        }


//        nfcManager_.writeData("1234AA",null);
    }

    public void updateUI(String info){
        showView.setText(info);
    }
}
