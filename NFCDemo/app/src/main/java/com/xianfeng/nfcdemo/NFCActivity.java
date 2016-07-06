package com.xianfeng.nfcdemo;

import android.content.Intent;
import android.os.Bundle;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import com.xianfeng.assist.CardHandler;
import com.xianfeng.assist.CardHandler.sendCommand;
import com.xianfeng.assist.CardInfo;
import com.xianfeng.services.NfcManager;
import com.xianfeng.services.SocketThread;
import com.xianfeng.util.CustomProgressDialog;
import com.xianfeng.util.ThreadPoolUtils;

public class NFCActivity extends AppCompatActivity {

    public static final String TAG = "NFCActivity";

    //NFC管理
    NfcManager  nfcManager_  = new NfcManager();
    CardHandler cardHandler_ = new CardHandler(this,nfcManager_);

    //socket相关
    private SocketThread socketThread_;

    //双击退出控制项
    private long exitTime = 0;

    //界面控件
    private TextView infoView;
    private EditText mCardAddr,mCardName, mCardNo, mCardAmount, mCardPrice, mBuyAmount;
    private Button mCardRead, mCardWrite;
    private static CustomProgressDialog cpd_Dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        System.out.println("NFC开发");

        //初始化nfc
        nfcManager_.initAdapter(this);
        onNewIntent (getIntent ());

        //UI
        infoView = (TextView)findViewById(R.id.promt);
        mCardAddr = (EditText)findViewById(R.id.addr_text);
        mCardName = (EditText) findViewById(R.id.edt_card_name);
        mCardNo = (EditText) findViewById(R.id.edt_card_no);
        mCardAmount = (EditText) findViewById(R.id.edt_card_amount);
        mCardPrice = (EditText) findViewById(R.id.edt_card_price);
        mBuyAmount = (EditText) findViewById(R.id.edt_card_buyamount);

        mCardRead = (Button) findViewById(R.id.btn_readcard);
        mCardRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("TYPE", CardHandler.OPERATION_READ_TYPE);
                operateCard(param);
            }
        });

        mCardWrite = (Button) findViewById(R.id.btn_writecard);
        mCardWrite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("TYPE", CardHandler.OPERATION_WRITE_TYPE);

                if (!mCardNo.getText().toString().trim().equalsIgnoreCase("")) {
                    if (!mBuyAmount.getText().toString().trim().equalsIgnoreCase("")) {
                        operateCard(param);
                    } else {
                        Toast.makeText(NFCActivity.this, "请输入充值气量", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NFCActivity.this, "请先进行读卡操作", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void operateCard(Map<String, Object> param) {
        if (cpd_Dialog == null) {
            cpd_Dialog = CustomProgressDialog.createDialog(NFCActivity.this);
        }

        if (!cpd_Dialog.isShowing()) {
            cpd_Dialog.show();
        }
        //内部类的用法
        ThreadPoolUtils.execute(cardHandler_.new sendCommand(param));
    }


    public void startSocket(String data, int type) {
        socketThread_ = new SocketThread(cardHandler_.hander, data, type);
        socketThread_.start();
    }

    //UI相关
    public void initUserInfo() {
        mCardName.setText("");
        mCardNo.setText("");
        mCardAmount.setText("");
        mCardPrice.setText("");
        mBuyAmount.setText("");
    }
    public void dialogDimiss(){
        if (cpd_Dialog != null && cpd_Dialog.isShowing()) {
            cpd_Dialog.dismiss();
            cpd_Dialog = null;
        }
    }
    public void showAlertView(String presentation){
        Toast.makeText(getApplicationContext(), presentation, Toast.LENGTH_SHORT).show();
    }
    public void displayToast(String str) {
        Toast toast=Toast.makeText(this, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP,0,220);
        toast.show();
    }
    public void updateUI(CardInfo info){
        mCardAddr.setText(info.userAddr);
        mCardName.setText(info.username);
        mCardNo.setText(info.userID);
        mCardAmount.setText(info.gases);
        mCardPrice.setText(info.price);
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
}
