package com.xianfeng.ormlitedemo;

import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.field.DatabaseField;

/**
 * Created by xianfeng on 16/4/29.
 */
@DatabaseTable(tableName = "tb_user")
//如果没有特别指出tableName = "tb_user"，那么默认情况将类名作为表名
//这里也可以使用注解@Entity，因为ORMLite既支持它自己的注解（@DatabaseTable和 @DatabaseField）也支持很多来自javax.persistence包中标准的注解。
//你可以使用来自javax.persistence包的更多的标准JPA注解。
public class User {
    //用户编号
    /**
     * id:这个字段是否为主键，默认为false
     * generatedId:字段是否自动增加。默认为false。
     * 注意：id和generatedId只能指明一个，否则会报错的
     */
    //可以用javax.persistence注解： @Id,@Column
    @DatabaseField(generatedId=true)
    private int userId;
    //用户名
    @DatabaseField
    private String userName;
    //密码
    @DatabaseField
    private String password;

    public User() {
        //必须提供无参构造函数，这样查询的时候可以返回查询出来的对象
    }
    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}