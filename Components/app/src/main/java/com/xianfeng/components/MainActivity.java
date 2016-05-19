package com.xianfeng.components;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.content.DialogInterface.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //文本
        final TextView myTextView = (TextView) findViewById(R.id.myTextView);
        myTextView.setText("我是文本，别烦我！");

        //按钮
        final Button myButton = (Button) findViewById(R.id.myButton);
        assert myButton != null;
        myButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                System.out.println("点我点我！");
                myTextView.setText(myButton.getText());
                myTextView.setBackgroundColor(0x0000);
            }
        });
        Button my_button = (Button) findViewById(R.id.my_button);
        my_button.setAlpha((float) 0.5);

        //输入框
        final EditText myEditText = (EditText)findViewById(R.id.myEditText);
        myEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // 响应用户的回车键动作，将EditText中值显示到TextView中
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&(keyCode == KeyEvent.KEYCODE_ENTER)) {
                    myTextView.setText(myEditText.getText());
                    return true;
                }
                    return false;
                }
            });

    }

    //图片按钮的点击方法
    public void my_buttonClicked(View view){
        System.out.println("我是带图片的按钮啊！");
        TextView myTextView = (TextView) findViewById(R.id.myTextView);
        myTextView.setText("我是文本，变回来！");

    }


}
