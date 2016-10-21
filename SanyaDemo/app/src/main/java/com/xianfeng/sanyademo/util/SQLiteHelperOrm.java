package com.xianfeng.sanyademo.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import com.xianfeng.sanyademo.model.*;

/**
 * Created by xianfeng on 2016/10/11.
 */

//本地管理,ormlite
public class SQLiteHelperOrm extends OrmLiteSqliteOpenHelper{

    // 数据库名称
    private static final String DATABASE_NAME = "sanyamember.db";
    // 数据库version
    private static final int DATABASE_VERSION = 1;

    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/SanyaData";
    public static final String DATABASE_PATH = FILE_PATH + "/" + DATABASE_NAME;
    private static final File file = new File(FILE_PATH);


    //数据库操作单例
    private Context context = null;
    public SQLiteHelperOrm(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;


    }

    //创建数据目录
    public void setDataPath(String path){
        createDir(path);
    }
//    public static synchronized SQLiteHelperOrm getInstance(Context context) {
//        if (instance == null)
//        {
//            synchronized (SQLiteHelperOrm.class)
//            {
//                if (instance == null)
//                    instance = new SQLiteHelperOrm(context);
//            }
//        }
//        return instance;
//    }

    //Dao数据管理,不明问题,可能跟重写有关,即getDao的名字(不是这里出问题)
//    private Map<String, Dao> daos = new HashMap<String, Dao>();
//    public synchronized Dao getDao(Class clazz) throws SQLException
//    {
//        Dao dao = null;
//        String className = clazz.getSimpleName();
//        if (daos.containsKey(className))
//        {
//            dao = daos.get(className);
//        }
//        if (dao == null)
//        {
//            dao = super.getDao(clazz.getClass());
//            daos.put(className, dao);
//        }
//        return dao;
//    }

    private Dao<User, Integer>  userDao;
    private Dao<AccountData, Integer> accountDao;
    private Dao<AreaData,Integer> areaDao;
    private Dao<ChargeData, Integer> chargeDao;
    private Dao<GasData, Integer> gasDao;
    private Dao<RecordData, Integer> recordDao;


    public Dao<User, Integer> getUserDao() throws SQLException {
        if (userDao == null) {
            userDao = getDao(User.class);
        }
        return userDao;
    }

    public Dao<AccountData, Integer> getAccountDao() throws SQLException {
        if (accountDao == null) {
            accountDao = getDao(AccountData.class);
        }
        return accountDao;
    }

    public Dao<AreaData, Integer> getAreaDao() throws SQLException {
        if (areaDao == null) {
            areaDao = getDao(AreaData.class);
        }
        return areaDao;
    }

    public Dao<ChargeData, Integer> getChargeDao() throws SQLException {
        if (chargeDao == null) {
            chargeDao = getDao(ChargeData.class);
        }
        return chargeDao;
    }

    public Dao<GasData, Integer> getGasDao() throws SQLException {
        if (gasDao == null) {
            gasDao = getDao(GasData.class);
        }
        return gasDao;

    }

    public Dao<RecordData, Integer> getRecordDao() throws SQLException {
        if (recordDao == null) {
            recordDao = getDao(RecordData.class);
        }
        return recordDao;
    }


    //打开数据库,没有则创建
//    public void openDatabase(){
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_PATH,null);
//        if(!isOpen()){
//            this.onCreate(db);
//        }
//    }


    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {

        try {
            Log.i(SQLiteHelperOrm.class.getName(), "onCreate");
            this.getWritableDatabase();

            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, AccountData.class);
            TableUtils.createTable(connectionSource, AreaData.class);
            TableUtils.createTable(connectionSource, ChargeData.class);
            TableUtils.createTable(connectionSource, GasData.class);
            TableUtils.createTable(connectionSource, RecordData.class);

        } catch (SQLException e) {
            Log.e(SQLiteHelperOrm.class.getName(), "Can't create database", e);
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
            Log.i(SQLiteHelperOrm.class.getName(), "onUpgrade");
            //删掉旧版本的数据
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, AccountData.class, true);
            TableUtils.dropTable(connectionSource, AreaData.class,true);
            TableUtils.dropTable(connectionSource, ChargeData.class,true);
            TableUtils.dropTable(connectionSource, GasData.class,true);
            TableUtils.dropTable(connectionSource, RecordData.class,true);
            //创建一个新的版本
            onCreate(sqliteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(SQLiteHelperOrm.class.getName(), "Can't drop databases", e);
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
        userDao = null;
        accountDao = null;
        areaDao = null;
        chargeDao = null;
        gasDao = null;
        recordDao = null;

//        for (String key : daos.keySet())
//        {
//            Dao dao = daos.get(key);
//            dao = null;
//        }
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

