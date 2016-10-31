package com.xianfeng.sanyademo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * Created by xianfeng on 2016/10/19.
 */

@DatabaseTable(tableName = "tb_areadata")

public class AreaData {

    @DatabaseField(generatedId=true)
    private int userId;
    //区域信息
    @DatabaseField
    private String areaid; //区域编号
    @DatabaseField
    private String areaname; //区域名称

    public AreaData() {
        //必须提供无参构造函数，这样查询的时候可以返回查询出来的对象
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAreaid() {
        return areaid;
    }

    public void setAreaid(String areaid) {
        this.areaid = areaid;
    }

    public String getAreaname() {
        return areaname;
    }

    public void setAreaname(String areaname) {
        this.areaname = areaname;
    }

}
