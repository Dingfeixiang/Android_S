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
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


//import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.mwreader.bluetooth.SearchActivity;
import com.xianfeng.sanyademo.model.*;
import com.xianfeng.sanyademo.util.*;

import org.json.JSONObject;


public class DetialActivity extends AppCompatActivity{

    private static final String TAG = "DetialActivity";

    //布局
    Spinner     aspinner,pspinner,gspinner;
    EditText    usernameET,addressET,numberET,gasAmountET;
    TextView    moneyView,cardView;
    Button      submit,facture;

    //管理器
    MWManager   mwManger = MWManager.getHelper();
    DataProcesser   processer = DataProcesser.getInstance();
    public SQLiteHelperOrm db = new SQLiteHelperOrm(this);

    //数据
    private String[]    aValues;
    private String[]    pValues;
    private String[]    gValues;

    private String areainfo = "";
    private String priceinfo = "";
    private String gastypeinfo = "";
    private float gasValue = 0;
    private String cardNumberString = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detial);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //初始化布局
        initView();
        //初始化数据等
        initModule();

//        CardInfo.writeOrders()
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
            if (arg1 == aspinner){
                String cardNumber = aValues[arg2];
                areainfo = cardNumber;
            }else if(arg1 == pspinner){
                String cardNumber = pValues[arg2];
                priceinfo = cardNumber;
            }else if(arg1 == gspinner){
                String cardNumber = gValues[arg2];
                gastypeinfo = cardNumber;
            }
            //设置显示当前选择的项
            arg0.setVisibility(View.VISIBLE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }

    //EditText交互
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            Log.d("TAG","onTextChanged--------------->");
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            Log.d("TAG","afterTextChanged--------------->");
            //设置换算金额
            gasAmountET.setText(String.valueOf(gasValue) + "元");
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
            Log.d("TAG","beforeTextChanged--------------->");
        }
    };


    /********      交互       **********/

    //左边按钮
    void submitButtonAction(){
        if (usernameET.getText().toString().trim().equals("")) {
            alertMessage("用户不能为空!");
            return;
        }
        if (addressET.getText().toString().trim().equals("")){
            alertMessage("地址不能为空!");
            return;
        }
        if (numberET.getText().toString().trim().equals("")){
            alertMessage("用户编号不能为空!");
            return;
        }
        if (areainfo.equals("")){
            alertMessage("请选择区域信息!");
            return;
        }
        if (priceinfo.equals("")){
            alertMessage("请选择价格信息!");
            return;
        }
        if (gastypeinfo.equals("")){
            alertMessage("请选择用气类型!");
            return;
        }

        String moneyViewStr = moneyView.getText().toString().trim();
        String valueStr = moneyViewStr.substring(0,moneyViewStr.length()-1);
        gasValue = Float.valueOf(valueStr).floatValue();
        establishAccount();
    }
    //请求开户
    void establishAccount(){
        Map<String, Object> param = new HashMap();
        param.put(processer.TYPE,processer.MESSAGE_ACCOUNT);
        JSONObject jsonObject = new JSONObject();
        try{
            String username = usernameET.getText().toString().trim();
            String address = addressET.getText().toString().trim();
            String money = String.valueOf(gasValue);
            String areaid = areainfo;
            String priceno = priceinfo;
            String usergastype = gastypeinfo;

            //拼接请求json
            jsonObject.put("username",username);
            jsonObject.put("areaid",areaid);
            jsonObject.put("address",address);
            jsonObject.put("priceno",priceno);
            jsonObject.put("usergastype",usergastype);
            jsonObject.put("money",money);

        }catch (Exception ex){
            System.out.println("开户数据组装出错!");
        }
        param.put(processer.INFO,jsonObject);
        processer.excuteCommandOnBackground(param);
    }
    //开户结果
    public void establishAccountResult(boolean result,String cardNum,String userNum){
        if (result == true){
            alertMessage("开户成功!");
        }else {
            alertMessage("开户失败!");
        }
    }


    //右边按钮
    void writeButtonAction(){
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(processer.TYPE,processer.MESSAGE_DB);
        ThreadPoolUtils.execute(processer.new sendCommand(param));
    }
    //请求制卡
    //制卡结果



    //下载数据
    void downloadData(){
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(processer.INFO,null);
        param.put(processer.TYPE,processer.MESSAGE_BASEDATA);


        processer.excuteCommandOnBackground(param);
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
    public void clearData(){
        String[] none = new String[0];
        setupSpinnerValues(aspinner,none);
        setupSpinnerValues(pspinner,none);
        setupSpinnerValues(gspinner,none);
        usernameET.setText("");
        addressET.setText("");
        numberET.setText("");
        gasAmountET.setText("");
        moneyView.setText("");
    }

    /********      交互       **********/



    /********      UI与初始化相关       **********/

    //更新UI
    public void updateUI(DataResult result){

        setupSpinnerValues(aspinner,result.areas);
        setupSpinnerValues(pspinner,result.prices);
        setupSpinnerValues(gspinner,result.gastypes);
//        username.setText("");
//        address.setText("");
//        number.setText("");
//        gasAmount.setText("");
//        moneyView.setText("");
    }

    //初始化数据等
    void initModule(){
        //
        processer.detialActivity = this;
        //打开数据库
        db.setDataPath(SQLiteHelperOrm.FILE_PATH);

        //这个是什么作用?
//        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    }

    //布局相关
    void initView(){
        aspinner = (Spinner) findViewById(R.id.adegree);
        pspinner = (Spinner) findViewById(R.id.pdegree);
        gspinner = (Spinner) findViewById(R.id.gdegree);

        usernameET = (EditText) findViewById(R.id.accountEdittext);
        addressET = (EditText) findViewById(R.id.pwdEdittext);//可以换行
        numberET = (EditText) findViewById(R.id.ntext);//限制10个长度
        gasAmountET = (EditText) findViewById(R.id.atext);

        moneyView = (TextView) findViewById(R.id.mvalue);
//        cardView = (TextView) findViewById(R.id.cardvalue);

        submit = (Button) findViewById(R.id.read);
        facture = (Button) findViewById(R.id.write);

        submit.setOnClickListener(new ButtonClickListener());
        facture.setOnClickListener(new ButtonClickListener());
        gasAmountET.addTextChangedListener(textWatcher);
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

    void alertMessage(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_SHORT).show();
    }

}

