package com.xianfeng.m24sr04demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.st.NFC.NFCApplication;
import com.st.NFC.NFCTag;
import com.xianfeng.Module.CleanableEditText;
import com.xianfeng.NFC.*;

public class MainActivity extends AppCompatActivity {

    //控件
    TextView showView;
    Button  testButton1;
    EditText editText;
    Switch aSwitch;
    ProgressDialog dialog = null;

    //
    NFCManager nfcManager_ = new NFCManager();

    //读写标记
    boolean _readingFlag = false;
    boolean _writingFlag = false;

    //
    String writedata_ = "default string";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showView = (TextView)findViewById(R.id.showview);
        testButton1 = (Button)findViewById(R.id.button);
        aSwitch = (Switch)findViewById(R.id.switch1);
        editText = (CleanableEditText)findViewById(R.id.edittext);
        dialog = new ProgressDialog(MainActivity.this);

        // Animation in case of TAP tag request
        ImageView nfcWavesImg = (ImageView) findViewById(R.id.NfcWavesImgId);
        nfcWavesImg.setBackgroundResource(R.drawable.nfc_waves_anim);


//        testButton1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View view) {
//
//                String string = editText.getText().toString();
//                System.out.println(string);
//                if (!string.equals(""))
//                    writedata_ = editText.getText().toString();
//
//                nfcManager_.writeData(null,writedata_, new NFCCallback.TagCallBack() {
//                    @Override
//                    public void currentTagStatus(NFCCallback.Status status) {
//                        if (status == NFCCallback.Status.TAG_EMPTY){
//                            loadingForWrite();
//                        }
//                    }
//                });
//            }
//        });

        _readingFlag = true;
        _writingFlag = false;
        testButton1.setClickable(false);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    _readingFlag = true;
                    _writingFlag = !_readingFlag;
                } else {
                    _writingFlag = true;
                    _readingFlag = !_writingFlag;
                }
                testButton1.setClickable(_writingFlag);
            }
        });

        nfcManager_.initAdapter(this);

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

            if (_readingFlag){

                NFCTag nfcTag = nfcManager_.getNFCTag(intent);
                NFCApplication.getApplication().setCurrentTag(nfcTag);

                nfcManager_.readData(intent, new NFCCallback.ReadCallBack() {
                    @Override
                    public void readFinish(String something) {
                        Toast.makeText(getApplicationContext(),something, Toast.LENGTH_LONG).show();
                        showView.setText(something);
                    }
                });
            }

            if (_writingFlag){
                NFCTag nfcTag = nfcManager_.getNFCTag(intent);
                NFCApplication.getApplication().setCurrentTag(nfcTag);

                String string = editText.getText().toString();
                System.out.println(string);
                if (!string.equals(""))
                    writedata_ = editText.getText().toString();


                nfcManager_.writeData(intent,writedata_, new NFCCallback.TagCallBack() {
                    @Override
                    public void currentTagStatus(NFCCallback.Status status) {
                        if (status == NFCCallback.Status.TAG_EMPTY){
                            Toast.makeText(getApplicationContext(),"Tag为空！", Toast.LENGTH_LONG).show();
                        }else if(status == NFCCallback.Status.TAG_WRITE_SUCCESS){
                            Toast.makeText(getApplicationContext(),"写入成功！", Toast.LENGTH_LONG).show();
                        }else if(status == NFCCallback.Status.TAG_WRITE_FAILED){
                            Toast.makeText(getApplicationContext(),"写入失败！", Toast.LENGTH_LONG).show();
                        }
                        dialog.dismiss();
                    }
                });
            }

        }
    }

    public void updateUI(String info){
        showView.setText(info);
    }
}
