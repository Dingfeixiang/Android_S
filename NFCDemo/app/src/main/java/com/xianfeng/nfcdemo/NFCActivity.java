package com.xianfeng.nfcdemo;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.xianfeng.services.NfcManager;


public class NFCActivity extends AppCompatActivity {

    private static final String TAG = "NFCActivity";

    //界面控件
    TextView infoView;
    //NFC管理
    NfcManager nfcManager_ = new NfcManager();
    private NfcAdapter nfcAdapter_;
    private PendingIntent pendingIntent;

    //双击退出控制项
    private long exitTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        System.out.println("NFC开发");
        infoView = (TextView)findViewById(R.id.promt);

        //初始化nfc
        nfcManager_.initAdapter(this);
        onNewIntent (getIntent ());
    }

    //处理NFC触发
    @Override
    protected void onNewIntent(Intent intent) {
        //读取数据
        nfcManager_.readDataFromIntent(intent);
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

    //双击退出
    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "再次按返回键退出", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }


    //刷新状态
    public void refreshStatus(String data) {
        System.out.println("刷新页面");
        String tip;
        if (nfcManager_.isInvalid()) {
            tip = "没有NFC硬件";
        } else if (!nfcManager_.isEnabled()) {
            tip = "NFC已禁用";
        } else {
            setTitle(getResources().getString(R.string.app_name));
            infoView.setText(data);
            return;
        }
        final StringBuilder s = new StringBuilder(getResources().getString(R.string.app_name));
        s.append("  --  ").append(tip);
        setTitle(s);
    }


    //显示提示信息
    public void displayToast(String str) {
        Toast toast=Toast.makeText(this, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP,0,220);
        toast.show();
    }
}
