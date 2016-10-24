package com.xianfeng.sanyademo.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.j256.ormlite.dao.Dao;
import com.xianfeng.sanyademo.model.AreaData;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import com.xianfeng.sanyademo.*;

import org.json.JSONObject;


/**
 * Created by xianfeng on 2016/10/21.
 */

public class DataProcesser{

    private static final String TAG = "DataProcesser";

    //与其他类交互键名
    public static final String TYPE = "TYPE"; //交互类型
    public static final String INFO = "INFO"; //交互数据

    //交互消息类型(对应TYPE)
    public static final int MESSAGE_DB = 1;
    public static final int MESSAGE_ERROR_DB = 2;

    public static final int MESSAGE_LOGIN = 3; //登录
    public static final int MESSAGE_BASEDATA = 4; //基础数据
    public static final int MESSAGE_ACCOUNT = 5; //开户
//    public static final int MESSAGE_ERROR_SOAP = 6;//SOAP请求错误


    //数据库操作单例
    private static DataProcesser instance;
    public static synchronized DataProcesser getInstance() {
        if (instance == null)
        {
            synchronized (DataProcesser.class)
            {
                if (instance == null)
                    instance = new DataProcesser();
            }
        }
        return instance;
    }

    private Downloader downloader = Downloader.getHelper();
    public MainActivity mainActivity = null;
    public DetialActivity detialActivity = null;

    //执行命令
    public void excuteCommandOnBackground(Map<String, Object> param){
        ThreadPoolUtils.execute(new sendCommand(param));
    }

    //这里是子线程
    public class sendCommand implements Runnable {

        private Map<String, Object> param_ = null;

        private Message msg = null;
        private Dao<AreaData,Integer> areaDao;

        public sendCommand(Map<String, Object> param) {
            sendCommand.this.param_ = param;
        }

        //子线程中不可以操作UI，使用Handler进行消息传递
        @Override
        public void run() {
            //获取参数
            int type = (int) param_.get(TYPE);
            msg = new Message();

            if(type == MESSAGE_DB){//数据库操作
                try{
                    msg.what = MESSAGE_DB;
                    AreaData area = new AreaData();
                    area.setUserId(100);
                    area.setAreaname("这是测试");
                    area.setAreaid("123");
                    areaDao = detialActivity.db.getAreaDao();
                    areaDao.create(area);
                    System.out.println("添加测试");
                }catch (Exception ex){
                    System.out.println("添加错误");
                    msg.what = MESSAGE_ERROR_DB;
                }

            }else {//网络请求操作
                String method = "";

                if(type == MESSAGE_BASEDATA){ //拉取基础数据
                    msg.what = MESSAGE_BASEDATA;
                    method = downloader.getAppDownloadDB;
                }
                else if(type == MESSAGE_LOGIN) { //登录
                    msg.what = MESSAGE_LOGIN;
                    method = downloader.getAppLogin;

                }else if (type == MESSAGE_ACCOUNT){ //开户
                    msg.what = MESSAGE_ACCOUNT;
                    method = downloader.getAppAccount;
                }

                try{
                    JSONObject josnstr = (JSONObject) param_.get(INFO);
                    downloader.soapRequest(method, josnstr, new Downloader.Callbackable() {
                        @Override
                        public void soapResponse(JSONObject jsonObject) {
                            System.out.print(jsonObject);
                            msg.obj = jsonObject;
                        }
                    });
                }catch (Exception ex){
                    System.out.println("数据请求错误!");
                    msg.obj = null;
                }

            }

            //回调
            hander.sendMessage(msg);
        }
    }

    //回调处理
    @SuppressLint("HandlerLeak")
    public final Handler hander = new Handler() {

        String standup = "";

        @Override
        public void handleMessage(Message msg) {

            //主线程
            switch (msg.what) {

                case MESSAGE_DB:

                    break;
                case MESSAGE_ERROR_DB:

                    break;


                //登录结果
                case MESSAGE_LOGIN:
                    try{
                        JSONObject jsonObject = (JSONObject) msg.obj;
                        boolean loginResult;
                        loginResult = jsonObject.getBoolean("result");
                        mainActivity.loginResultDispose(loginResult);

                    }catch (Exception ex){
                        System.out.println("登录回传解析错误!");
                        mainActivity.loginResultDispose(false);
                    }
                    break;

                //开户结果
                case MESSAGE_ACCOUNT:
                    try{
                        JSONObject jsonObject = (JSONObject) msg.obj;
                        boolean establishResult = jsonObject.getBoolean("result");
                        String cardNumber = jsonObject.getString("cardno");
                        String userNumber = jsonObject.getString("systemno");
                        detialActivity.establishAccountResult(establishResult,cardNumber,userNumber);

                    }catch (Exception ex){
                        System.out.println("登录回传解析错误!");
                        detialActivity.establishAccountResult(false,"","");
                    }
                    break;

                //基础数据拉取结果
                case MESSAGE_BASEDATA:
                    try{
                        JSONObject jsonObject = (JSONObject) msg.obj;
                        //解析数据


                    }catch(Exception ex){
                        System.out.println("基础信息解析错误!");
                    }

                    break;


                default:
                    break;

            }
        }
    };

    private List<Array> parseBasedataJSONObject(JSONObject jsonObject){

        return null;
    }
}
