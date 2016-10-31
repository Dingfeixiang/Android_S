package com.xianfeng.sanyademo.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.xianfeng.sanyademo.model.AreaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.xianfeng.sanyademo.*;
import com.xianfeng.sanyademo.model.ChargeData;
import com.xianfeng.sanyademo.model.GasData;
import com.xianfeng.sanyademo.model.RecordData;
import com.xianfeng.sanyademo.sql.DataDao;

import org.json.JSONArray;
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

    public static final int TYPE_DB_RECORD = 10;
    public static final int TYPE_DB_AREA = 11;
    public static final int TYPE_DB_CHARGE = 12;
    public static final int TYPE_DB_GAS = 13;
    public static final int TYPE_DB_ACCOUNT = 14;

//    public static final int MESSAGE_ERROR_SOAP = 6;//SOAP请求错误

    //数据类型
    private static final int DATATYPE_USERDATA = 99;
    private static final int DATATYPE_ACCOUNTDATA = 100;

    private static final int DATATYPE_AREADATA = 101;
    private static final int DATATYPE_CHARGEDATA = 102;
    private static final int DATATYPE_GASDATA = 103;

    //数据存储
    public ArrayList<AreaData> areaDatas = new ArrayList<>();
    public ArrayList<ChargeData>  chargeDatas= new ArrayList<>();
    public ArrayList<GasData> gasDatas = new ArrayList<>();

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
//        private Dao<AreaData,Integer> areaDao;

        public sendCommand(Map<String, Object> param) {
            sendCommand.this.param_ = param;
        }

        //子线程中不可以操作UI，使用Handler进行消息传递
        @Override
        public void run() {
            //获取参数
            int type = (int) param_.get(TYPE);
            Object object = param_.get(INFO);
            msg = new Message();

            if(type == MESSAGE_DB){//数据库操作
                msg.what = MESSAGE_DB;
                try{
                    if(object instanceof RecordData){
                        RecordData recordData = (RecordData)object;
                        DataDao dao = new DataDao(detialActivity);
                        int result = dao.getRecordDao().create(recordData);
                        System.out.println("添加制卡记录" + (result == 1?"成功":"失败") + "!");
                    }

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
            try{
                hander.sendMessage(msg);
            }catch (Exception ex){
                System.out.println("");
            }

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
                    try{

                    }catch (Exception e){

                    }
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
                        String stringIwant = jsonObject.getString("result");
                        JSONObject valueIwant = new JSONObject(stringIwant);
                        boolean establishResult = valueIwant.getBoolean("result");
                        String cardNumber = valueIwant.getString("cardno");
                        String userNumber = valueIwant.getString("systemno");
                        String resultcode = valueIwant.getString("resultcode");
                        //开户结果回调
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
                        areaDatas = parseBasedataJSONObject(jsonObject,DATATYPE_AREADATA);
                        chargeDatas = parseBasedataJSONObject(jsonObject,DATATYPE_CHARGEDATA);
                        gasDatas = parseBasedataJSONObject(jsonObject,DATATYPE_GASDATA);
                        //返回数据
                        detialActivity.downloadBasedataResult(areaDatas,chargeDatas,gasDatas);
                        System.out.println("解析结束");
                    }catch(Exception ex){
                        System.out.println("基础信息解析错误!");
                    }

                    break;


                default:
                    break;

            }
        }
    };

    private ArrayList parseBasedataJSONObject(JSONObject jsonObject,int datatype){

        ArrayList  datalist = new ArrayList();

        try{
            String stringIwant = jsonObject.getString("result");
            JSONObject valueIwant = new JSONObject(stringIwant);

            JSONArray arrayIwant = new JSONArray();
            if (datatype == DATATYPE_AREADATA){
                arrayIwant = valueIwant.getJSONArray("area");
            }else if (datatype == DATATYPE_CHARGEDATA){
                arrayIwant = valueIwant.getJSONArray("price");
            }else if (datatype == DATATYPE_GASDATA){
                arrayIwant = valueIwant.getJSONArray("usergastype");
            }
            for (int i=0; i<arrayIwant.length();i++){

                if (datatype == DATATYPE_AREADATA){

                    JSONObject objectIwant = arrayIwant.getJSONObject(i);
                    String areaid = objectIwant.getString("areaid");
                    String areaname = objectIwant.getString("areaname");

                    AreaData areaData = new AreaData();
                    areaData.setUserId(i);
                    areaData.setAreaid(areaid);
                    areaData.setAreaname(areaname);
                    datalist.add(areaData);

                }else if (datatype == DATATYPE_CHARGEDATA){

                    JSONObject objectIwant = arrayIwant.getJSONObject(i);
                    String priceno = objectIwant.getString("priceno");
                    String pricestartdate = objectIwant.getString("pricestartdate");
                    String pricecycle = objectIwant.getString("pricecycle");
                    String cyclestartdate = objectIwant.getString("cyclestartdate");
                    String clearflag = objectIwant.getString("clearflag");
                    String laddprice1 = objectIwant.getString("laddprice1");
                    String laddprice2 = objectIwant.getString("laddprice2");
                    String laddprice3 = objectIwant.getString("laddprice3");
                    String laddvalue1 = objectIwant.getString("laddvalue1");
                    String laddvalue2 = objectIwant.getString("laddvalue2");
                    String pricename = objectIwant.getString("pricename");
                    String pricever = objectIwant.getString("pricever");

                    ChargeData chargeData = new ChargeData();
                    chargeData.setUserId(i);
                    chargeData.setPricename(pricename);
                    chargeData.setPricever(pricever);
                    chargeData.setPriceno(priceno);
                    chargeData.setPricestartdate(pricestartdate);
                    chargeData.setPricecycle(pricecycle);
                    chargeData.setCyclestartdate(cyclestartdate);
                    chargeData.setClearflag(clearflag);
                    chargeData.setLaddprice1(laddprice1);
                    chargeData.setLaddprice2(laddprice2);
                    chargeData.setLaddprice3(laddprice3);
                    chargeData.setLaddvalue1(laddvalue1);
                    chargeData.setLaddvalue2(laddvalue2);
                    datalist.add(chargeData);

                }else if (datatype == DATATYPE_GASDATA){

                    JSONObject objectIwant = arrayIwant.getJSONObject(i);
                    String usergastype = objectIwant.getString("usergastype");
                    String usergastypename = objectIwant.getString("usergastypename");

                    GasData gasData = new GasData();
                    gasData.setUserId(i);
                    gasData.setUsergastype(usergastype);
                    gasData.setUsergastypename(usergastypename);
                    datalist.add(gasData);
                }
            }

        }catch (Exception ex){
            System.out.println("基础数据解析过程错误！");
        }
        return datalist;
    }
}
