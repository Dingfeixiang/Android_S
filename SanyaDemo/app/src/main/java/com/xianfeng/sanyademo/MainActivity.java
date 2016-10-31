package com.xianfeng.sanyademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.xianfeng.sanyademo.util.DataProcesser;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import com.xianfeng.sanyademo.view.*;


public class MainActivity extends AppCompatActivity {

//    public static final String EXTRAS_NAME = "NAME";
//    public static final String EXTRAS_PASSWORD = "PASSWORD";
    DataProcesser processer = DataProcesser.getInstance();

    //UI
    private Button loginBtn;
    private EditText nameET,passwordET;
    private static CustomProgressDialog cpd_Dialog = null;

    //data
    String companyString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        processer.mainActivity = this;

        //
        nameET = (EditText)findViewById(R.id.accountEdittext);
        nameET.setText("admins");
        passwordET = (EditText)findViewById(R.id.pwdEdittext);
        passwordET.setText("000000");
        companyString = "008001";

        if (cpd_Dialog == null) {
            cpd_Dialog = CustomProgressDialog.createDialog(this);
        }


        loginBtn = (Button)findViewById(R.id.login);
        loginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (true)
                {
                    //需要数据库
                    loginDetial();
                }else {
                    //登录
                    String name = nameET.getText().toString().trim();
                    String pass = passwordET.getText().toString().trim();
                    if (!name.equalsIgnoreCase("")) {
                        if (!pass.equalsIgnoreCase("")){
                            if (!cpd_Dialog.isShowing()) {
                                cpd_Dialog.show();
                            }
                            loginRequest();
                        }else {
                            alertMessage("请输入密码!");
                        }
                    } else {
                        alertMessage("请输入用户名!");
                    }
                }
            }
        });

    }

    //下载数据
    void loginRequest(){
        Map<String, Object> param = new HashMap();
        param.put(processer.TYPE,processer.MESSAGE_LOGIN);

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("username",nameET.getText().toString().trim());
            jsonObject.put("password",passwordET.getText().toString().trim());
            jsonObject.put("companycode",companyString);
        }catch (Exception ex){
            System.out.println("登录数据组装出错!");
        }
        param.put(processer.INFO,jsonObject);
        processer.excuteCommandOnBackground(param);
    }

    //进入详情
    void loginDetial(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, DetialActivity.class);
        startActivity(intent);
    }


    //服务器请求后处理
    public void loginResultDispose(boolean isSussess){
        if (cpd_Dialog.isShowing()) {
            cpd_Dialog.dismiss();
        }
        if (isSussess){
            loginDetial();
        }else {
            clearUI();
            alertMessage("登录失败!");
        }
    }

    void alertMessage(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_SHORT).show();
    }

    void clearUI(){

    }
}
