package com.xianfeng.sanyademo.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.CloseableIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import com.xianfeng.sanyademo.model.*;

/**
 * Created by xianfeng on 2016/10/11.
 */

//本地管理,ormlite
public class DataManager extends OrmLiteSqliteOpenHelper{

    // 数据库名称
    private static final String DATABASE_NAME = "sanyamember.db";
    // 数据库version
    private static final int DATABASE_VERSION = 1;

    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/SanyaData";
    public static final String DATABASE_PATH = FILE_PATH + "/" + DATABASE_NAME;
    private static final File file = new File(FILE_PATH);


    //数据库操作单例
    private static DataManager instance;
    private DataManager(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }
    /**
     * 单例获取该Helper
     */
    public static synchronized DataManager getHelper(Context context) {
        if (instance == null)
        {
            synchronized (DataManager.class)
            {
                if (instance == null)
                    instance = new DataManager(context);
            }
        }
        return instance;
    }

    //Dao数据管理
    private Map<String, Dao> daos = new HashMap<String, Dao>();
    @Override
    public synchronized Dao getDao(Class clazz) throws SQLException {
        try{
            Dao dao = null;
            String className = clazz.getSimpleName();
            if (daos.containsKey(className))
            {
                dao = daos.get(className);
            }
            if (dao == null)
            {
                dao = super.getDao(clazz);
                daos.put(className, dao);
            }
            return dao;
        }catch (SQLException ex){

        }
        return null;
    }

    public void openDatabase(){
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_PATH,null);
        if (!instance.isOpen()){
            instance.onCreate(db);
        }
    }


    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {

        try {
            Log.i(DataManager.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, User.class);

        } catch (SQLException e) {
            Log.e(DataManager.class.getName(), "Can't create database", e);
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
            Log.i(DataManager.class.getName(), "onUpgrade");
            //删掉旧版本的数据
            TableUtils.dropTable(connectionSource, User.class, true);
            //创建一个新的版本
            onCreate(sqliteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(DataManager.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }



    @Override
    public synchronized SQLiteDatabase getWritableDatabase(){
        file.setWritable(true);
        return SQLiteDatabase.openDatabase(DATABASE_PATH,null,SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase(){
        file.setReadable(true);
        return SQLiteDatabase.openDatabase(DATABASE_PATH,null,SQLiteDatabase.OPEN_READONLY);
    }


    /**
     * 释放资源
     */
    @Override
    public void close() {

        super.close();

        for (String key : daos.keySet())
        {
            Dao dao = daos.get(key);
            dao = null;
        }
    }
}

