package com.xianfeng.ormlitedemo;

/**
 * Created by xianfeng on 16/5/3.
 */

//import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import android.test.AndroidTestCase;
//import android.util.Log;
import android.os.Environment;

public class OrmliteTest extends AndroidTestCase
{
    public DatabaseHelper helper = null;

    public DatabaseHelper getHelper(){
        helper = DatabaseHelper.getHelper(getContext());
        return helper;
    }

    public void testSqlile()
    {
        //获取数据路径
        String DATABASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        System.out.println("path:"+DATABASE_PATH);

        User user1 = this.getHelper().findUserById(1);
        System.out.println("user is:"+
                user1.getUserId()+user1.getUserName()+user1.getPassword());
//        try
//        {
//            User user1 = this.queryUserWithId(1);
//            System.out.println(user1.getUserId()+user1.getUserName()+user1.getPassword());
//
//        }catch (IOException io){
//            io.printStackTrace();
//        }
    }

    public void testShowUsers()
    {
        try
        {
            List<User> users = this.getHelper().getUserDao().queryForAll();
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                System.out.println("userID:"+user.getUserId());
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    public void testAddUser()
    {

        User u1 = new User("zhy", "2B青年");
        DatabaseHelper helper = this.getHelper();
        try
        {
            helper.getUserDao().create(u1);

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void testDeleteUser()
    {
        DatabaseHelper helper = DatabaseHelper.getHelper(getContext());
        try
        {
            helper.getUserDao().deleteById(2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void testUpdateUser()
    {
        DatabaseHelper helper = DatabaseHelper.getHelper(getContext());
        try
        {
            User u1 = new User("zhy-android", "2B青年");
            u1.setUserId(3);
            helper.getUserDao().update(u1);

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}