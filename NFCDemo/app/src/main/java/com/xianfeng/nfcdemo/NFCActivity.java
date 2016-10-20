package com.xianfeng.nfcdemo;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import com.xianfeng.services.CardHandler;
import com.xianfeng.assist.CardInfo;
import com.xianfeng.assist.ReadCardInfo;
import com.xianfeng.assist.WriteCardInfo;
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

    //界面相关
    private TextView tv0,tv1,tv2,tv3,tv4,tv5,tv6;
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
        initUI();
        initUserInfo();
        mCardRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCard();
            }
        });
        mCardWrite.setOnClickListener(new View.OnClickListener() {
            private String iii = "";

            @Override
            public void onClick(View v) {
                String buyText = mBuyAmount.getText().toString().trim();
                String cardNoText = mCardNo.getText().toString().trim();
                if (!cardNoText.equalsIgnoreCase("")) {
                    if (!buyText.equalsIgnoreCase("")) {
                        Integer write_value = new Integer(Integer.parseInt(buyText));
                        writeCard(write_value);
                    } else {
                        showAlertView("请输入充值气量");
                    }
                } else {
                    showAlertView("请先进行读卡操作");
                }
            }
        });
    }


    public static final String CARD_PARAM_KEY_TYPE = "TYPE";
    public static final String CARD_PARAM_KEY_WRITE_VALUE = "WRITE_VALUE";
    public void readCard(){
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(CARD_PARAM_KEY_TYPE, CardHandler.OPERATION_READ_TYPE);
        operateCard(param);
    }
    public void writeCard(Integer tobeWritten){
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(CARD_PARAM_KEY_TYPE, CardHandler.OPERATION_WRITE_TYPE);
        param.put(CARD_PARAM_KEY_WRITE_VALUE, tobeWritten);
        operateCard(param);
    }
    public void clearCard(){
        tv0.setText("请将卡片贴在手机背面");
        mBuyAmount.setText("");
        tv6.setText("充值气量");
        mCardAddr.setText("");
        tv1.setText("用户地址");
        mCardName.setText("");
        tv2.setText("用户姓名");
        mCardNo.setText("");
        tv3.setText("卡片编号");
        mCardAmount.setText("");
        tv4.setText("卡上气量");
        mCardPrice.setText("");
        tv5.setText("燃气单价");
        nfcManager_.reconvertStatus();
    }


    public void startSocket(String data, int type) {
        socketThread_ = new SocketThread(cardHandler_.hander, data, type);
        socketThread_.start();
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

    //UI相关
    public void initUI(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        tv0 = (TextView)findViewById(R.id.tv0);
        tv1 = (TextView)findViewById(R.id.tv1);
        tv2 = (TextView)findViewById(R.id.tv2);
        tv3 = (TextView)findViewById(R.id.tv3);
        tv4 = (TextView)findViewById(R.id.tv4);
        tv5 = (TextView)findViewById(R.id.tv5);
        tv6 = (TextView)findViewById(R.id.tv6);
        mCardAddr = (EditText)findViewById(R.id.addr_text);
        mCardName = (EditText) findViewById(R.id.edt_card_name);
        mCardNo = (EditText) findViewById(R.id.edt_card_no);
        mCardAmount = (EditText) findViewById(R.id.edt_card_amount);
        mCardPrice = (EditText) findViewById(R.id.edt_card_price);
        mBuyAmount = (EditText) findViewById(R.id.edt_card_buyamount);
        mCardRead = (Button) findViewById(R.id.btn_readcard);
        mCardWrite = (Button) findViewById(R.id.btn_writecard);
    }
    public void initUserInfo() {
        mCardAddr.setText("");
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
    public void updateUI(CardInfo info) {
        if (info instanceof ReadCardInfo) {
            ReadCardInfo rInfo = (ReadCardInfo) info;
            tv0.setText("输入气量应小于：" + rInfo.maxPurchase);
            tv1.setText("用户地址：");
            mCardAddr.setText(rInfo.userAddr);
            tv2.setText("用户姓名：");
            mCardName.setText(rInfo.username);
            tv3.setText("卡片编号：");
            mCardNo.setText(rInfo.userID);
            tv4.setText("卡上气量：");
            mCardAmount.setText(rInfo.gases);
            tv5.setText("燃气单价：");
            mCardPrice.setText(String.valueOf(rInfo.price));
            tv6.setText("充值气量：");
            mBuyAmount.setText("");
        } else if (info instanceof WriteCardInfo) {
            WriteCardInfo wInfo = (WriteCardInfo) info;
            tv0.setText("购气次数为：" + wInfo.purchaseCount);
            tv1.setText("交易公司：");
            mCardAddr.setText(wInfo.comSeq);
            tv2.setText("用户姓名：");
            mCardName.setText(wInfo.username);
            tv3.setText("卡片编号：");
            mCardNo.setText(wInfo.userID);
            tv4.setText("交易时间：");
            mCardAmount.setText(wInfo.transDate);
            tv5.setText("购气次数：");
            mCardPrice.setText(wInfo.purchaseCount);
            tv6.setText("充值气量：");
            mBuyAmount.setText("");
        }
    }
    //处理NFC触发
    @Override
    protected void onNewIntent(Intent intent) {
        //读取数据
        nfcManager_.readData(intent);
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

        if (data == null || data.equals("")){
            tv0.setText("请将卡片贴在手机背面");
            return;
        }

        String tip;
        if (nfcManager_.isInvalid()) {
            tip = "没有NFC硬件";
        } else if (!nfcManager_.isEnabled()) {
            tip = "NFC已禁用";
        } else {
            tip = "";
        }
        final StringBuilder s = new StringBuilder(getResources().getString(R.string.app_name));
        s.append("      ").append(tip);
        setTitle(s);
    }
}
