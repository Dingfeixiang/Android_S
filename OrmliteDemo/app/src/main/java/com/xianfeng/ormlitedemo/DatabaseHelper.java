package com.xianfeng.ormlitedemo;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * Created by xianfeng on 16/4/29.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper{
    // 数据库名称
    private static final String DATABASE_NAME = "helloAndroid.db";
    // 数据库version
    private static final int DATABASE_VERSION = 1;

    /**
     * 包含两个泛型:
     * 第一个泛型表DAO操作的类
     * 第二个表示操作类的主键类型
     */
    private Dao<User, Integer> userDao = null;

    private RuntimeExceptionDao<User, Integer> simpleRuntimeDao = null;
    public RuntimeExceptionDao<User, Integer> getSimpleDataDao() {
        if (simpleRuntimeDao == null) {
            simpleRuntimeDao = getRuntimeExceptionDao(User.class);
        }
        Log.i("test", "simpleRuntimeDao ======= "+simpleRuntimeDao);
        return simpleRuntimeDao;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Dao<User, Integer> getUserDao() throws SQLException {
        if (userDao == null) {
            userDao = getDao(User.class);
        }
        return userDao;
    }

    //数据库操作单例
    private static DatabaseHelper instance;
    /**
     * 单例获取该Helper
     *
     * @param context
     * @return
     */
    public static synchronized DatabaseHelper getHelper(Context context)
    {
        if (instance == null)
        {
            synchronized (DatabaseHelper.class)
            {
                if (instance == null)
                    instance = new DatabaseHelper(context);
            }
        }
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, User.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }

    }
    /**
     * 这个方法在你的应用升级以及它有一个更高的版本号时调用。所以需要你调整各种数据来适应新的版本
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVersion,
                          int newVersion) {
        Log.i("test", "更新....");
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            //删掉旧版本的数据
            TableUtils.dropTable(connectionSource, User.class, true);
            //创建一个新的版本
            onCreate(sqliteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 插入一条数据
     */
    public void insert(User user){
        RuntimeExceptionDao<User, Integer> dao = getSimpleDataDao();
        //通过实体对象创建在数据库中创建一条数据，成功返回1，说明插入了一条数据
        Log.i("test", "dao = " + dao+"  user= "+user);
        int returnValue = dao.create(user);
        Log.i("test", "插入数据后返回值："+returnValue);
    }
    /**
     * 查询所有的用户信息
     * @return
     */
    public List<User> findAllUser(){
        RuntimeExceptionDao<User, Integer> dao = getSimpleDataDao();
        return dao.queryForAll();
    }

    /*
     * 查询一条用户信息
     */
    public User findUserById(Integer id){
        RuntimeExceptionDao<User, Integer> dao = getSimpleDataDao();
        return dao.queryForId(id);
    }


    /**
     * 删除第一条用户信息
     */
    public void deleteById(){
        RuntimeExceptionDao<User, Integer> dao = getSimpleDataDao();
        List<User> list = dao.queryForAll();
        //删除成功返回1（删除了一条数据）
        if(list.size()>0){
            int returnValue = dao.deleteById(list.get(0).getUserId());
            Log.i("test", "删除一条数据后返回值:"+returnValue);
        }

    }
    /**
     * 批量删除用户信息
     */
    public void deleteByIds(){
        RuntimeExceptionDao<User, Integer> dao = getSimpleDataDao();
        List<User> list = dao.queryForAll();
        List<Integer> ids = new ArrayList<Integer>();
        if(list.size()>0){
            for(User u:list){
                ids.add(u.getUserId());
            }
            //返回删除的记录数
            int returnValue = dao.deleteIds(ids);
            Log.i("test", "批量删除后返回值:"+returnValue);
        }

    }


    /**
     * 释放资源
     */
    @Override
    public void close()
    {
        super.close();
        userDao = null;
    }

}
