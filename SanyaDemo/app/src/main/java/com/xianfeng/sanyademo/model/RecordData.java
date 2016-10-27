package com.xianfeng.sanyademo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by xianfeng on 2016/10/19.
 */

@DatabaseTable(tableName = "tb_logrecord")

public class RecordData extends User {

    @DatabaseField(generatedId=true)
    private int userId;

    @DatabaseField
    private String log;

    public RecordData(){}

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getLog() {
        return log;
    }
    public void setLog(String log) {
        this.log = log;
    }
}
