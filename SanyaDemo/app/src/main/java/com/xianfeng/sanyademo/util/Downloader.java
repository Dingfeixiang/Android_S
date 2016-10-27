package com.xianfeng.sanyademo.util;

/**
 * Created by xianfeng on 2016/10/11.
 */

import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.*;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

//下载管理,ksoap2
public class Downloader {

    //请求回调接口
    public interface Callbackable {
        void soapResponse(JSONObject jsonObject);
    }

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

    private static final String SERVICE_NAMESPACE =  "http://tempuri.org/";
    private static final String SERVICE_URL = "http://192.168.4.90:90/xfWeb.asmx"; //WebService的命名空间

    //方法调用名称
    public static final String getAppDownloadDB = "AppDownloadBD";
    public static final String getAppLogin = "AppLogin";
    public static final String getAppAccount = "AppAccount";

    // 创建HttpTransportSE传输对象
    HttpTransportSE httptransport = null;
    // 使用SOAP1.1协议创建Envelop对象
    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

    //请求与回调
    public void soapRequest(String method, JSONObject jsonObject, Callbackable callbackable) throws IOException, XmlPullParserException {
        SoapObject request = null;
        request = new SoapObject(SERVICE_NAMESPACE, method);

        if (jsonObject != null){
            String paramjson = jsonObject.toString();
            request.addProperty("paramjson",paramjson);
        }

        envelope.bodyOut = request;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        try{
            httptransport = new HttpTransportSE(SERVICE_URL);
            httptransport.call(SERVICE_NAMESPACE + method,envelope);

            Object responseJson = null;
            String responseStr = "";
            // 设置不返回null
            if (envelope.getResponse() != null) {
                responseJson = envelope.getResponse();
                responseStr = responseJson.toString();
            } else {
                responseStr = "";
            }
            System.out.println(responseStr);
            JSONObject returnJson = new JSONObject();
            returnJson.put("result",responseStr);
            callbackable.soapResponse(returnJson);

        }catch (Exception ex){
            System.out.print(ex.getMessage());
            callbackable.soapResponse(null);
        }
    }

//    public static void stopCall() throws ErrorMessage {
//        if (transport != null) {
//            try {
//                transport.getServiceConnection().disconnect();
//            } catch (IOException e) {
//                throw new ErrorMessage(e.getMessage());
//            }
//        } else {
//            throw new ErrorMessage("取消失败");
//        }
//    }

}

