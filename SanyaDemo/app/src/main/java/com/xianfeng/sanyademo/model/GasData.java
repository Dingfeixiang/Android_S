package com.xianfeng.sanyademo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by xianfeng on 2016/10/19.
 */

@DatabaseTable(tableName = "tb_gasdata")

public class GasData extends User {

    @DatabaseField(generatedId=true)
    private int userId;
    //用气类型
    @DatabaseField
    private String usergastype; //用气类型编号
    @DatabaseField
    private String usergastypename; //用气类型名称

    public GasData(){}


    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getUsergastype() {
        return usergastype;
    }
    public void setUsergastype(String usergastype) {
        this.usergastype = usergastype;
    }
    public String getUsergastypename() {
        return usergastypename;
    }
    public void setUsergastypename(String usergastypename) {
        this.usergastypename = usergastypename;
    }
}
