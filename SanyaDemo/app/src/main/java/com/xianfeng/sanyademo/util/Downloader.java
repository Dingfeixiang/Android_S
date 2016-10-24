package com.xianfeng.sanyademo.util;

/**
 * Created by xianfeng on 2016/10/11.
 */

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.telecom.Call;

import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapSerializationEnvelope;

import java.util.Map;

//下载管理,ksoap2
public class Downloader {

    public interface Callbackable {
        void loginResult(boolean isSuccess);
//        void basedataResult();
    }

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


    public void loginRequest(String jsonString,Callbackable callbackable){

        //登录接口回调后调用
        if (callbackable != null)
            callbackable.loginResult(true);
    }

    public void achieveBasedataRequest(String jsonString,Callbackable callbackable){

        //

    }


}

