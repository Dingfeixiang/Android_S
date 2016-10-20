package com.xianfeng.sanyademo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.util.Map;

import com.j256.ormlite.dao.Dao;
import java.sql.SQLException;
import com.mwreader.bluetooth.SearchActivity;
import com.xianfeng.sanyademo.model.*;
import com.xianfeng.sanyademo.util.*;

public class DetialActivity extends AppCompatActivity {

    private static final String TAG = "DetialActivity";

    //与其他类交互键名
    public static final String KEY = "KEY";

    //布局
    Spinner     aspinner,pspinner,gspinner;
    EditText    username,address,number,gasAmount;
    TextView    moneyView;
    Button      submit,facture;

    //管理器
    MWManager   mwManger = MWManager.getHelper();
    DataManager dataManager = DataManager.getHelper(this);
    Downloader  downloader = Downloader.getHelper();

    //数据
    private String[]    aValues;
    private String[]    pValues;
    private String[]    gValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detial);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //初始化布局
        initView();

        //初始化数据等
        initModule();

    }


    //交互逻辑 button
    class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.read:
                submitButtonAction();
                    break;
                case R.id.write:
                writeButtonAction();
                    break;
            }
        }
    }

    //交互逻辑 spinner
    class SpinnerSelectedListener implements OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }


/********      交互       **********/

    //左边按钮
    void submitButtonAction(){

        try{
            AreaData area = new AreaData();
            area.setAreaname("这是测试");
            dataManager.getDao(AreaData.class).create(area);
        }catch (Exception ex){

        }
//        new UserDao(this).add(area);
    }

    //右边按钮
    void writeButtonAction(){

    }

    //下载数据
    void downloadData(){
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(KEY,"");
        ThreadPoolUtils.execute(downloader.new sendCommand(param));
    }

    //将表数据存储到数据库
    void storeToSql(List<User> list){
        if(list.size()>0){
            for(User u:list){
                try{

                    System.out.println("添加");
                }catch (Exception ex) {
                    System.out.println("添加失败!");
                }
            }
        }
    }

    //连接读卡设备
    void connectDevice(){
        try {
            mwManger.closeDevice();
            //浏览蓝牙设备
            Intent intent = new Intent(DetialActivity.this,
                    SearchActivity.class);
            startActivityForResult(intent, 1);
        }catch (Exception ex) {
            System.out.print("浏览蓝牙设备有问题!");
        }
    }

    //清除数据
    void clearData(){
        String[] none = new String[0];
        setupSpinnerValues(aspinner,none);
        setupSpinnerValues(pspinner,none);
        setupSpinnerValues(gspinner,none);
        username.setText("");
        address.setText("");
        number.setText("");
        gasAmount.setText("");
        moneyView.setText("");
    }

    /********      交互       **********/



/********      初始化相关       **********/

    void initModule(){
        //创建数据目录
        createDir(dataManager.FILE_PATH);
        //打开数据库
        dataManager.openDatabase();
        //这个是什么作用?
//        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    }

    public static boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            System.out.println("创建目录" + destDirName + "失败，目标目录已经存在");
            return false;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            System.out.println("创建目录" + destDirName + "成功！");
            return true;
        } else {
            System.out.println("创建目录" + destDirName + "失败！");
            return false;
        }
    }

    //布局相关
    void initView(){
        aspinner = (Spinner) findViewById(R.id.adegree);
        pspinner = (Spinner) findViewById(R.id.pdegree);
        gspinner = (Spinner) findViewById(R.id.gdegree);

        username = (EditText) findViewById(R.id.accountEdittext);
        address = (EditText) findViewById(R.id.pwdEdittext);//可以换行
        number = (EditText) findViewById(R.id.ntext);//限制10个长度
        gasAmount = (EditText) findViewById(R.id.atext);

        moneyView = (TextView) findViewById(R.id.mvalue);

        submit = (Button) findViewById(R.id.read);
        facture = (Button) findViewById(R.id.write);

        submit.setOnClickListener(new ButtonClickListener());
        facture.setOnClickListener(new ButtonClickListener());
    }

    //设置spinner数据
    void setupSpinnerValues(Spinner spinner, String[] values){

        //将可选内容与ArrayAdapter连接起来
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values);

        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //将adapter 添加到spinner中
        spinner.setAdapter(adapter);

        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());

        //设置默认值
        spinner.setVisibility(View.VISIBLE);
    }

    /********      初始化相关       **********/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.clear).setVisible(true);
        menu.findItem(R.id.connect).setVisible(true);
        menu.findItem(R.id.download).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.download:
                downloadData();
                break;

            case R.id.connect:
                connectDevice();
                break;

            case R.id.clear:
                clearData();
                break;
        }
        return true;
    }

}

