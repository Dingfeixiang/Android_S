package com.xianfeng.sanyademo.util;

/**
 * Created by xianfeng on 2016/10/11.
 */

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapSerializationEnvelope;

import java.util.Map;

//下载管理,ksoap2
public class Downloader {

    public static final int MESSAGE_UI = 1;
    public static final int MESSAGE_ERROR = 2;
    public static final int MESSAGE_SOAP = 3;

    public static final String SERVICE_NS = ""; //WebService的命名空间
    public static final String SERVICE_URL = "http://192.168.4.90:90/xfWeb.asmx";//WebService提供服务的URL


    //数据库操作单例
    private static Downloader instance;

    /**
     * 单例获取该Helper
     */
    public static synchronized Downloader getHelper() {
        if (instance == null)
        {
            synchronized (Downloader.class)
            {
                if (instance == null)
                    instance = new Downloader();
            }
        }
        return instance;
    }

    // 创建HttpTransportSE传输对象
    HttpTransportSE httptransport = new HttpTransportSE(SERVICE_URL);

    // 使用SOAP1.1协议创建Envelop对象
    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

    private String soapMethod(String methodName){
        return "";
    }


    //这里是子线程
    public class sendCommand implements Runnable {

        private Map<String, Object> param_ = null;
        private Message msg = null;

        public sendCommand(Map<String, Object> param) {
            sendCommand.this.param_ = param;
        }

        //子线程中不可以操作UI，使用Handler进行消息传递
        @Override
        public void run() {
            //获取参数
            String  type = (String) param_.get("");


            msg = new Message();
            hander.sendMessage(msg);
        }
    }

    //回调处理
    @SuppressLint("HandlerLeak")
    public final Handler hander = new Handler() {

        String iii = "";

        @Override
        public void handleMessage(Message msg) {

            //主线程
            switch (msg.what) {
                case MESSAGE_UI:
                    byte[] data = (byte[]) msg.obj;

                    try {

                    } catch (Exception e) {

                    }

                    break;
                case MESSAGE_ERROR:

                default:
                    break;
            }
        }
    };

}

