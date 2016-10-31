package com.xianfeng;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.xianfeng.ormlitedemo.DatabaseHelper;
import com.xianfeng.ormlitedemo.R;
import com.xianfeng.ormlitedemo.User;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db = DatabaseHelper.getHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                try{
                    User user = new User();
                    user.setUserName("test");
//                    db.getDao(User.class).create(user);
                    db.insert(user);
                    System.out.println("添加测试");
                }catch (Exception ex){
                    System.out.println("添加失败");
                }
            }
        });
    }

}
