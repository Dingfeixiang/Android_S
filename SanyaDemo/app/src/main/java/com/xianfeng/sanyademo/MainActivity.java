package com.xianfeng.sanyademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Intent;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = null;
//    public static final String EXTRAS_NAME = "NAME";
//    public static final String EXTRAS_PASSWORD = "PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginBtn = (Button)findViewById(R.id.login);

        loginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //这里就可以判断账号密码是否正确了，这里让大家自己试验动手一下谢谢如果账号密码是admin 123456就成功
                //否则就提示登陆失败，大家试一试吧，我这里直接跳转了，没做验证

                //这个是直接跳转到MainActivity
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, DetialActivity.class);
                startActivity(intent);

            }
        });
    }
}
