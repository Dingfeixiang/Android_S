package com.xianfeng.m24sr04demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.xianfeng.NFC.*;

public class MainActivity extends AppCompatActivity {

    TextView showView;
    NFCManager nfcManager_ = new NFCManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showView = (TextView)findViewById(R.id.showview);
        nfcManager_.initAdapter(this);
        onNewIntent(getIntent());

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

        nfcManager_.readData(intent);

    }

    public void updateUI(String info){
        showView.setText(info);

    }
}
