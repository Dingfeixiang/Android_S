package com.xianfeng.sanyademo.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import com.xianfeng.sanyademo.model.*;

import java.io.File;
import java.sql.SQLException;

/**
 * Created by xianfeng on 2016/10/28.
 */


public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "sanyacard.db";
    //文件路径
    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/SanyaData";
    //.db路径
    public static final String DATABASE_PATH = FILE_PATH + "/" + DATABASE_NAME;

    private static Context context_;
    private static DatabaseHelper instance;
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        File file = new File(FILE_PATH);
        System.out.println("存储路径是：" + file.getPath());
        if (!file.exists()){
            createDir(file.getPath());
        }
        File f = new File(DATABASE_PATH);
        if (!f.exists()) {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
                    DATABASE_PATH, null);
            onCreate(db);
            db.close();
        }
        System.out.println(DATABASE_PATH);
    }

    public static synchronized DatabaseHelper getHelper(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null)
                    instance = new DatabaseHelper(context);
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, AccountData.class);
            TableUtils.createTable(connectionSource, AreaData.class);
            TableUtils.createTable(connectionSource, ChargeData.class);
            TableUtils.createTable(connectionSource, GasData.class);
            TableUtils.createTable(connectionSource, RecordData.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, AccountData.class,true);
            TableUtils.dropTable(connectionSource, AreaData.class,true);
            TableUtils.dropTable(connectionSource, ChargeData.class,true);
            TableUtils.dropTable(connectionSource, GasData.class,true);
            TableUtils.dropTable(connectionSource, RecordData.class,true);
            onCreate(database, connectionSource);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(){
        return SQLiteDatabase.openDatabase(DATABASE_PATH,null,SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase(){
        return SQLiteDatabase.openDatabase(DATABASE_PATH,null,SQLiteDatabase.OPEN_READONLY);
    }

    public static boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            System.out.println("创建目录" + destDirName + "失败，目标目录已经存在");
            return false;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            System.out.println("创建目录" + destDirName + "成功！");
            return true;
        } else {
            System.out.println("创建目录" + destDirName + "失败！");
            return false;
        }
    }
}
