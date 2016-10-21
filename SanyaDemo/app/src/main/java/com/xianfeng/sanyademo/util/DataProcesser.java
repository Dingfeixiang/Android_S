package com.xianfeng.sanyademo.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.j256.ormlite.dao.Dao;
import com.xianfeng.sanyademo.model.AreaData;

import java.util.Map;
import com.xianfeng.sanyademo.*;


/**
 * Created by xianfeng on 2016/10/21.
 */

public class DataProcesser{

    private static final String TAG = "DataProcesser";

    //与其他类交互键名
    public static final String TYPE = "TYPE"; //交互类型
    public static final String INFO = "INFO"; //交互数据

    //交互消息类型(对应TYPE)
    public static final int MESSAGE_UI = 1;
    public static final int MESSAGE_SOAP = 2;
    public static final int MESSAGE_ERROR_SOAP = 3;
    public static final int MESSAGE_DB = 4;
    public static final int MESSAGE_ERROR_DB = 5;
    public static final int MESSAGE_LOGIN = 6;
    public static final int MESSAGE_LOGIN_RETURN = 7;


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
            if(type == MESSAGE_DB){
                //保存数据
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
            }else if(type == MESSAGE_SOAP){
                //下载数据
                try{
                    msg.what = MESSAGE_SOAP;

                }catch (Exception ex){
                    msg.what = MESSAGE_ERROR_SOAP;
                }

            }else if(type == MESSAGE_LOGIN){
                msg.what = MESSAGE_LOGIN;
                String jsonStr = "";
                downloader.loginRequest(jsonStr, new Downloader.Callbackable() {
                    @Override
                    public void loginResult(boolean isSuccess) {
                        if (isSuccess){
                            msg.arg1 = 1;
                        }else {
                            msg.arg1 = 0;
                        }
                    }
                });
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

                case MESSAGE_UI:

                    break;
                case MESSAGE_DB:

                    break;
                case MESSAGE_ERROR_DB:

                    break;

                case MESSAGE_SOAP:
                    break;

                case MESSAGE_ERROR_SOAP:

                    break;

                //登录结果
                case MESSAGE_LOGIN:
                    if (msg.arg1 == 0){
                        mainActivity.loginResultDispose(false);
                    }else {
                        mainActivity.loginResultDispose(true);
                    }
                    break;

                default:
                    break;
            }
        }
    };
}
