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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = null;
//    public static final String EXTRAS_NAME = "NAME";
//    public static final String EXTRAS_PASSWORD = "PASSWORD";
    DataProcesser processer = DataProcesser.getInstance();

    //UI
    Button loginBtn;
    EditText nameET,passwordET;

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

        loginBtn = (Button)findViewById(R.id.login);
        loginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!true)
                {
                    loginDetial();
                }else {
                    String name = nameET.getText().toString().trim();
                    String pass = passwordET.getText().toString().trim();
                    if (!name.equalsIgnoreCase("")) {
                        if (!pass.equalsIgnoreCase("")){
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
        }catch (Exception ex){
            System.out.println("登录数据组装出错!");
        }
        param.put(processer.INFO,jsonObject);

//        Map<String, String> values = new HashMap();
//        values.put("name",nameET.getText().toString().trim());
//        values.put("password",passwordET.getText().toString().trim());
//        param.put(processer.INFO,values);

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
