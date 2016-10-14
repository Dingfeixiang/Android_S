package com.xianfeng.sanyademo.util;

/**
 * Created by xianfeng on 2016/10/11.
 */

import java.net.*;
import android.util.Log;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapSerializationEnvelope;

//下载管理,ksoap2
public class Downloader {

    public static final String SERVICE_NS = ""; //WebService的命名空间
    public static final String SERVICE_URL = "";//WebService提供服务的URL

    // 创建HttpTransportSE传输对象
    HttpTransportSE httptransport = new HttpTransportSE(SERVICE_URL);

    // 使用SOAP1.1协议创建Envelop对象
    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);


    private String soapMethod(String methodName){
        return "";
    }


}

