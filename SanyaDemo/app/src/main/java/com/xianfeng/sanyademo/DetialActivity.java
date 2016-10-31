package com.xianfeng.sanyademo;

import android.content.Intent;
import android.graphics.Color;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


import com.mwreader.bluetooth.SearchActivity;
import com.xianfeng.sanyademo.sql.*;
import com.xianfeng.sanyademo.model.*;
import com.xianfeng.sanyademo.util.*;
import com.xianfeng.sanyademo.view.*;

import org.json.JSONObject;


public class DetialActivity extends AppCompatActivity{

    private static final String TAG = "DetialActivity";

    //布局
    Spinner     aspinner,pspinner,gspinner;
    EditText    usernameET,addressET,numberET,gasAmountET;
    TextView    moneyView,cardView;
    Button      submit,facture;
    CustomProgressDialog cpd_Dialog = null;

    //管理器
    MWManager   mwManger = MWManager.getHelper();
    DataProcesser   processer = DataProcesser.getInstance();

    //数据
    private List<AreaData> spinnerlist_area = new ArrayList<>();
    private List<ChargeData> spinnerlist_charge = new ArrayList();;
    private List<GasData> spinnerlist_gas = new ArrayList();

    //开户数据
    private DataResult  dataResult = new DataResult();
    private boolean setupAccountEnableFlag = false;

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
            alertMessage("表具编号不能为空!");
            return;
        }
        if (numberET.getText().length() > 10){
            alertMessage("表具编号长度不能超过10!");
            return;
        }
        if (dataResult.areaData.getAreaname().equals("")){
            alertMessage("请选择区域信息!");
            return;
        }
        if (dataResult.chargeData.getPricename().equals("")){
            alertMessage("请选择价格信息!");
            return;
        }
        if (dataResult.gasData.getUsergastypename().equals("")){
            alertMessage("请选择用气类型!");
            return;
        }

        //用户姓名
        String username = usernameET.getText().toString().trim();
        dataResult.username = username;
        //用户地址
        String address = addressET.getText().toString().trim();
        dataResult.useraddress = address;
        //表具编号
        String tablenumber = numberET.getText().toString().trim();
        dataResult.tablenumber = tablenumber;
        //换表气量
        String gasnumber = gasAmountET.getText().toString().trim();
        dataResult.gasAmount = gasnumber.toString();

        //换算成表金额
        String moneyViewStr = moneyView.getText().toString().trim();
        String valueStr = moneyViewStr.substring(0,moneyViewStr.length()-1);
        dataResult.setGasValue(Float.valueOf(valueStr).floatValue()); //换算后的

        //请求开户
        if (!cpd_Dialog.isShowing()) {
            cpd_Dialog.show();
        }

        setupAccountEnableFlag = false;
        establishAccount(dataResult);
    }
    //请求开户
    void establishAccount(DataResult dataResult){

        Map<String, Object> param = new HashMap();
        param.put(processer.TYPE,processer.MESSAGE_ACCOUNT);

        String username = dataResult.username;
        String address = dataResult.useraddress;
        String money = String.valueOf(dataResult.getGasValue());
        String metercode = dataResult.tablenumber;

        String areaid = dataResult.areaData.getAreaid();
        String priceno = dataResult.chargeData.getPriceno();
        String usergastype = dataResult.gasData.getUsergastype();
        JSONObject jsonObject = new JSONObject();
        try{
            //拼接请求json
            jsonObject.put("username",username);
            jsonObject.put("areaid",areaid);
            jsonObject.put("address",address);
            jsonObject.put("priceno",priceno);
            jsonObject.put("usergastype",usergastype);
            jsonObject.put("money",money);
            jsonObject.put("metercode",metercode);

        }catch (Exception ex){
            System.out.println("开户数据组装出错!");
        }
        param.put(processer.INFO,jsonObject);
        processer.excuteCommandOnBackground(param);
    }
    //开户结果
    public void establishAccountResult(String resultcode,String cardNum,String userNum){
        cpd_Dialog.dismiss();
        if (resultcode.equals("000")){
            //成功
            alertMessage("开户成功");
            setupAccountEnableFlag = true;
            dataResult.cardNumberString = cardNum;
            dataResult.userNumber = userNum;
            //保存开户信息
            storeAccountInfoToSQL(dataResult);
        }else if (resultcode.equals("999")){
            alertMessage("开户重复！");
        }else if (resultcode.equals("555")){
            alertMessage("开户发生错误！");
            dataResult.cardNumberString = "";
            dataResult.userNumber = "";
        }else {
            alertMessage("开户失败！");
            dataResult.cardNumberString = "";
            dataResult.userNumber = "";
        }
        //UI
        cardView.setText(dataResult.cardNumberString);
    }
    //保存开户信息到数据库
    void storeAccountInfoToSQL(DataResult dataResult){
        RecordData recordData = transferDataResult(dataResult);
        DataDao dao = new DataDao(this);
        try{
            dao.getRecordDao().create(recordData);
            System.out.println("保存开户信息");
        }catch (Exception ex){
            System.out.println("开户信息保存失败！");
        }
    }
    //将界面提交的用户信息转换为数据库信息
    RecordData transferDataResult(DataResult dataResult){
        RecordData recordData = new RecordData();
        double gases = Double.valueOf(dataResult.gasAmount); recordData.setGases(gases);
        double gasfee = Double.valueOf(String.valueOf(dataResult.getGasValue())); recordData.setGasfee(gasfee);
        double price1 = Double.valueOf(dataResult.chargeData.getLaddprice1()); recordData.setPrice1(price1);
        double price2 = Double.valueOf(dataResult.chargeData.getLaddprice2()); recordData.setPrice2(price2);
        double price3 = Double.valueOf(dataResult.chargeData.getLaddprice3()); recordData.setPrice3(price3);
        int laddgas1 = Integer.valueOf(dataResult.chargeData.getLaddvalue1()); recordData.setLaddgas1(laddgas1);
        int laddgas2 = Integer.valueOf(dataResult.chargeData.getLaddvalue2()); recordData.setLaddgas2(laddgas2);
        String pricedate = dataResult.chargeData.getPricestartdate(); recordData.setPricedate(pricedate);//1
        int pricetype = Integer.valueOf(dataResult.gasData.getUsergastype()); recordData.setPricetype(pricetype);
        int pricever = Integer.valueOf(dataResult.chargeData.getPricever()) ; recordData.setPricever(pricever); //1
        int pricecycle = Integer.valueOf(dataResult.chargeData.getPricecycle()); recordData.setPricecycle(pricecycle); //1
        int clearflag = Integer.valueOf(dataResult.chargeData.getClearflag()); recordData.setClearflag(clearflag); //1
        String cycledate = dataResult.chargeData.getCyclestartdate(); recordData.setCycledate(cycledate);//1

        double newprice1 = price1; recordData.setNewprice1(newprice1);
        double newprice2 = price2; recordData.setNewprice2(newprice2);
        double newprice3 = price3; recordData.setNewprice3(newprice3);
        int newladdgas1 = laddgas1; recordData.setLaddgas1(newladdgas1);
        int newladdgas2 = laddgas2; recordData.setNewladdgas2(newladdgas2);
        String newpricedate = pricedate; recordData.setNewpricedate(newpricedate);
        int newpricetype = pricetype; recordData.setNewpricetype(newpricetype);
        int newpricever = pricever; recordData.setNewpricever(newpricever);
        int newpricecycle = pricecycle; recordData.setNewpricecycle(newpricecycle);
        int newclearflag = clearflag; recordData.setNewclearflag(newclearflag);
        String newcycledate = cycledate; recordData.setNewcycledate(newcycledate);

        String meterno = dataResult.tablenumber; recordData.setMeterno(meterno); //表具编号
        String cardno = dataResult.cardNumberString; recordData.setCardno(cardno);
        String companyno = dataResult.companyno; recordData.setCompanyno(companyno);
        //中中
        return recordData;
    }


    /****************************************************/
    //右边按钮
    void writeButtonAction(){
        if (setupAccountEnableFlag){
            //写卡
        }else {
            alertMessage("开户成功才可以制卡！");
        }
    }
    //保存单个数据到数据库
    //数据库操作结果回传
    public void dbOperationCallback(boolean isSuccess,int dbtype){
        if (dbtype == processer.TYPE_DB_RECORD){
            if (isSuccess){
                System.out.println("写卡记录存储成功！");
            }else {
                System.out.println("写卡记录存储失败！");
            }
        }

    }


    /****************************************************/

    //下载基础数据
    void downloadData(){
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(processer.INFO,null);
        param.put(processer.TYPE,processer.MESSAGE_BASEDATA);
        processer.excuteCommandOnBackground(param);
    }
    //基础数据返回
    public void downloadBasedataResult(ArrayList<AreaData> areaDatas,
                                ArrayList<ChargeData> chargeDatas,
                                ArrayList<GasData> gasDatas){
        //保存列表
        spinnerlist_area = areaDatas;
        spinnerlist_charge = chargeDatas;
        spinnerlist_gas = gasDatas;
        //保存列表第一个数据
        dataResult.areaData = areaDatas.get(0);
        dataResult.chargeData = chargeDatas.get(0);
        dataResult.gasData = gasDatas.get(0);

        //设置区域信息
        List<String> alist = new ArrayList<>();
        for (int i=0;i<areaDatas.size();i++){
            AreaData dataIwant = areaDatas.get(i);
            String value = dataIwant.getAreaname();
            alist.add(value);
        }
        //设置价格信息
        List<String> clist = new ArrayList<>();
        for (int i=0;i<chargeDatas.size();i++){
            ChargeData dataIwant = chargeDatas.get(i);
            String value = dataIwant.getPricename() + dataIwant.getPriceno();
            clist.add(value);
        }
        //设置用气类型
        List<String> glist = new ArrayList<>();
        for (int i=0;i<gasDatas.size();i++) {
            GasData dataIwant = gasDatas.get(i);
            String value = dataIwant.getUsergastypename();
            glist.add(value);
        }

        //更新UI
        updateUI(dataResult);
        setupSpinnerValues(aspinner,alist);
        setupSpinnerValues(pspinner,clist);
        setupSpinnerValues(gspinner,glist);
        cpd_Dialog.dismiss();
        cardView.setText("---");

        //存储到数据库
        File dbfile = new File(DatabaseHelper.DATABASE_PATH);
        if (!dbfile.exists()){
            storeBaseData(areaDatas,chargeDatas,gasDatas);
        }
    }


    void storeBaseData(ArrayList<AreaData> areaDatas,
                       ArrayList<ChargeData> chargeDatas,
                       ArrayList<GasData> gasDatas){
        DataDao dataDao = new DataDao(this);
        try{
            dataDao.getAreaDao().callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (AreaData data : areaDatas) {
                        dataDao.getAreaDao().create(data);
                    }
                    return null;
                }
            });
            dataDao.getChargeDao().callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (ChargeData data : chargeDatas) {
                        dataDao.getChargeDao().create(data);
                    }
                    return null;
                }
            });
            dataDao.getGasDao().callBatchTasks(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (GasData data : gasDatas) {
                        dataDao.getGasDao().create(data);
                    }
                    return null;
                }
            });
            System.out.println("存储基础信息");
        }catch (Exception sqlex){
            System.out.println("存储基础信息错误!");
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
        List<String> none = new ArrayList<>();
        none.add("");
        setupSpinnerValues(aspinner,none);
        setupSpinnerValues(pspinner,none);
        setupSpinnerValues(gspinner,none);

        usernameET.setText("");
        addressET.setText("");
        numberET.setText("");
        gasAmountET.setText("");
        moneyView.setText("" + "元");
        cardView.setText("");
    }
    /********      交互       **********/


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
            TextView tv = (TextView) arg1;
            tv.setTextColor(Color.BLACK);
            if (arg1 == aspinner){
                AreaData areaData = spinnerlist_area.get(arg2);
                dataResult.areaData = areaData;
            }else if(arg1 == pspinner){
                ChargeData chargeData = spinnerlist_charge.get(arg2);
                dataResult.chargeData = chargeData;
            }else if(arg1 == gspinner){
                GasData gasData = spinnerlist_gas.get(arg2);
                dataResult.gasData = gasData;
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

            String gasnumber = gasAmountET.getText().toString().trim();
            dataResult.gasAmount = gasnumber.toString();

            //设置换算金额
            moneyView.setText(dataResult.getGasValue() + "元");
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
            Log.d("TAG","beforeTextChanged--------------->");
        }
    };


    /********      UI与初始化相关       **********/
    public void alertMessage(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_SHORT).show();
    }
    //更新UI
    public void updateUI(DataResult result){
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
        cardView = (TextView) findViewById(R.id.ctview);

        submit = (Button) findViewById(R.id.read);
        facture = (Button) findViewById(R.id.write);
        submit.setClickable(true);
        facture.setClickable(true);

        submit.setOnClickListener(new ButtonClickListener());
        facture.setOnClickListener(new ButtonClickListener());

        //设置换表气量动作
        gasAmountET.addTextChangedListener(textWatcher);

        if (cpd_Dialog == null) {
            cpd_Dialog = CustomProgressDialog.createDialog(this);
        }
    }
    //设置spinner数据
    void setupSpinnerValues(Spinner spinner, List<String> values){
        if (values.size() == 0){
            List<String> strings = new ArrayList<>();
            strings.add("");
            values = strings;
        }
        //将可选内容与ArrayAdapter连接起来
//        String[]
        ArrayAdapter<String> adapter = new ArrayAdapter(this.getApplicationContext(),
                android.R.layout.simple_spinner_item,values);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //将adapter 添加到spinner中
        spinner.setAdapter(adapter);
//
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
                if (!cpd_Dialog.isShowing()) {
                    cpd_Dialog.show();
                }
                downloadData();
                break;

            case R.id.connect:
                connectDevice();
                break;

            case R.id.clear:
                clearData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("返回");
    }
}

