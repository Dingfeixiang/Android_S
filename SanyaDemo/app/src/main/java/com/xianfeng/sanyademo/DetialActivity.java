package com.xianfeng.sanyademo;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import com.mwreader.bluetooth.SearchActivity;
import com.xianfeng.sanyademo.util.*;

public class DetialActivity extends AppCompatActivity {

    private static final String TAG = "DetialActivity";
    //布局
    EditText    username,address,number,gasAmount;
    TextView    moneyView;
    Button      submit,facture;

    MWManager mwManger = MWManager.getHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detial);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //初始化布局
        initView();

        //初始化读写卡器
        initModule();


    }



    //交互逻辑
    class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.read:

                    break;
                case R.id.write:

                    break;
            }
        }
    }

    void initModule(){
        //这个是什么作用?
//        SerialPortFinder mSerialPortFinder = new SerialPortFinder();

    }


    //布局相关
    void initView(){

        username = (EditText) findViewById(R.id.accountEdittext);
        address = (EditText) findViewById(R.id.pwdEdittext);//可以换行
        number = (EditText) findViewById(R.id.ntext);//限制10个长度
        gasAmount = (EditText) findViewById(R.id.atext);

        moneyView = (TextView) findViewById(R.id.mvalue);

        submit = (Button) findViewById(R.id.read);
        facture = (Button) findViewById(R.id.write);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.clear).setVisible(true);
        menu.findItem(R.id.connect).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:

                break;
            case R.id.connect:

                try {
                    mwManger.closeDevice();
                    //浏览蓝牙设备
                    Intent intent = new Intent(DetialActivity.this,
                            SearchActivity.class);
                    startActivityForResult(intent, 1);
                }catch (Exception ex) {
                    System.out.print("这里有问题!");
                }

                break;
        }
        return true;
    }

}

