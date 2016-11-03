package com.xianfeng.sanyademo.sql;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.xianfeng.sanyademo.model.*;

import java.sql.SQLException;

/**
 * Created by xianfeng on 2016/10/28.
 */

public class DataDao {

    private Dao dao;
    private DatabaseHelper helper;

    public DataDao(Context context) {
        try {
            helper = DatabaseHelper.getHelper(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Dao<User, Integer>  userDao;
    private Dao<AccountData, Integer> accountDao;
    private Dao<AreaData,Integer> areaDao;
    private Dao<ChargeData, Integer> chargeDao;
    private Dao<GasData, Integer> gasDao;
    private Dao<RecordData, Integer> recordDao;

    public Dao getDao(Class clazz) throws SQLException {
        if (dao == null) {
            dao = helper.getDao(clazz.getClass());
        }
        return dao;
    }

    public Dao<User, Integer> getUserDao() throws SQLException {
        if (userDao == null) {
            userDao = helper.getDao(User.class);
        }
        return userDao;
    }
    public Dao<AccountData, Integer> getAccountDao() throws SQLException {
        if (accountDao == null) {
            accountDao = helper.getDao(AccountData.class);
        }
        return accountDao;
    }
    public Dao<AreaData, Integer> getAreaDao() throws SQLException {
        if (areaDao == null) {
            areaDao = helper.getDao(AreaData.class);
        }
        return areaDao;
    }
    public Dao<ChargeData, Integer> getChargeDao() throws SQLException {
        if (chargeDao == null) {
            chargeDao = helper.getDao(ChargeData.class);
        }
        return chargeDao;
    }
    public Dao<GasData, Integer> getGasDao() throws SQLException {
        if (gasDao == null) {
            gasDao = helper.getDao(GasData.class);
        }
        return gasDao;

    }
    public Dao<RecordData, Integer> getRecordDao() throws SQLException {
        if (recordDao == null) {
            recordDao = helper.getDao(RecordData.class);
        }
        return recordDao;
    }

    //通过区域编号查询区域

    //通过

}
