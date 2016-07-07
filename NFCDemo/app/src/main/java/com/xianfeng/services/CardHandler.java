package com.xianfeng.services;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Arrays;
import java.util.Map;

import com.xianfeng.nfcdemo.NFCActivity;
import com.xianfeng.util.CodeFormat;
import com.xianfeng.util.Arith;
import com.xianfeng.assist.*;


/**
 * Created by xianfeng on 16/6/30.
 */
public class CardHandler{

    private final static String TAG = NFCActivity.TAG;

    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_WRITE_DONE = 3;

    public static final int MESSAGE_UI = 2;
    public static final int MESSAGE_ERROR = 4;
    public static final int MESSAGE_SOCKET = 5;

    public static final String HOST_IP = "115.236.0.2";
    public static final String HOST_PORT = "6543";

    public static final String OPERATION_READ_TYPE = "read_type";
    public static final String OPERATION_WRITE_TYPE = "write_type";

    private static final String SINGNAL_READDATA_PRE    = "LYGASGS150100010001"; //发送读卡数据
    private static final String SINGNAL_WRITEDATA_PRE   = "LYGASGS210100010001"; //发送写卡数据

    private static final String SINGNAL_CARD_RECEIVEDATA_SUCCESS = "9000";

    private static final String SINGNAL_SOCKET_RECEIVEDATA_SUCCESS = "0000";
    private static final int MESSAGE_RIGHT_LENGTH = 512;

    //操作返回类型标志
    private static final int ERROR_READ_EMPTY               = 5;
    private static final int ERROR_READ_FAILED              = 10;
    private static final int ERROR_WRITE_CHECKFAILED        = 20;
    private static final int ERROT_WRITEIN_FAILED           = 30;
    private static final int ERROT_WRITE_UPDATEKEY_FAILED   = 40;

    public static final int ERROT_SOCKET_CONNECT_FAILED    = 100;
    public static final int ERROT_SOCKET_SEND_EXCEPTION    = 200;
    public static final int ERROT_SOCKET_RECEIVE_EXCEPTION = 300;


    public CardHandler(NFCActivity activity,NfcManager manager) {
        activity_ = activity;
        nfcManager_ = manager;
    }

    private NfcManager nfcManager_ = null;
    private NFCActivity activity_ = null;
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
            String  type = (String) param_.get(NFCActivity.CARD_PARAM_KEY_TYPE);
            Integer write_value = (Integer)param_.get(NFCActivity.CARD_PARAM_KEY_WRITE_VALUE);
            String nfcGetted = nfcManager_.dataReaded();

            msg = new Message();

            if (type == OPERATION_READ_TYPE) {
                // 读卡
                if (nfcGetted == null){
                    Log.i(TAG, "没有获取到数据");
                    msg.what = MESSAGE_ERROR;
                    msg.arg1 = ERROR_READ_EMPTY;
                } else if (nfcGetted != null && nfcGetted.length() == MESSAGE_RIGHT_LENGTH) {
                    //读卡
                    msg.what = MESSAGE_READ;
                } else {
                    Log.i(TAG, "卡片数据返回错误");

                    msg.what = MESSAGE_ERROR;
                    msg.arg1 = ERROR_READ_FAILED;
                }
                hander.sendMessage(msg);
            } else if (type == OPERATION_WRITE_TYPE) {
                // 写卡
                Log.i(TAG, "开始回传气量和卡片信息");
                getWriteData(write_value.intValue());
                Log.i(TAG, "回传气量和卡片信息成功");
            }
        }

        // 回传购气量
        private void getWriteData(int writeData) {
            msg = new Message();
            msg.what = MESSAGE_WRITE;
            msg.arg2 = writeData;
            hander.sendMessage(msg);
        }
    }



    //数据处理
    @SuppressLint("HandlerLeak")
    public final Handler hander = new Handler() {

        String iii = "";

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_READ:
                    //开启socket
                    String sendRead = dataSendToServerForRead(nfcManager_.dataReaded());
                    activity_.startSocket(sendRead, MESSAGE_READ);
                    break;
                case MESSAGE_WRITE:
                    String sendWrite = dataSendToServerForWrite(nfcManager_.dataReaded(),msg.arg2);
                    activity_.startSocket(sendWrite, MESSAGE_WRITE);
                    break;
                case MESSAGE_WRITE_DONE:
                    activity_.dialogDimiss();
//                    activity_.initUserInfo();
                    activity_.showAlertView("写卡成功");
                    break;
                case MESSAGE_UI:
                    byte[] data = (byte[]) msg.obj;

                    try {
                        Log.i(TAG, "**********数据解析开始**********");
                        // 返回码
                        String retCode = CodeFormat
                                .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 5, 9)), 4).trim();
                        Log.i(TAG, "服务器返回码: " + retCode);

                        if (!retCode.equals(SINGNAL_SOCKET_RECEIVEDATA_SUCCESS)){
                            String errorCode = getErrorStr(retCode);
                            activity_.showAlertView(errorCode);
                        }else {
                            if (msg.arg1 == MESSAGE_READ){
                                CardInfo iWant = parseDataForRead(data);
                                activity_.updateUI(iWant);
                            }else if(msg.arg1 == MESSAGE_WRITE){
                                CardInfo iWant = parseDataForWrite(data);
                                activity_.updateUI(iWant);
                                nfcManager_.writeCard((WriteCardInfo) iWant);
                            }
                        }
                        Log.i(TAG, "**********数据解析结束**********");
                        activity_.dialogDimiss();
                    } catch (Exception e) {
                        Log.i(TAG, "UI:数据解析异常");
                        activity_.showAlertView("数据解析异常");
                        activity_.dialogDimiss();
                        nfcManager_.reconvertStatus();
                    }

                    break;
                case MESSAGE_ERROR:
                    activity_.dialogDimiss();

                    if (msg.arg1 == ERROR_READ_FAILED) {
                        activity_.showAlertView("卡片读取失败");
                    } else if (msg.arg1 == ERROR_WRITE_CHECKFAILED) {
                        activity_.showAlertView("核对卡片密钥错误");
                    } else if (msg.arg1 == ERROT_WRITEIN_FAILED) {
                        activity_.initUserInfo();
                        activity_.showAlertView("卡片数据写入失败");
                    } else if (msg.arg1 == ERROT_WRITE_UPDATEKEY_FAILED) {
                        activity_.initUserInfo();
                        activity_.showAlertView("卡片数据写入成功,密钥更新失败");
                    } else if (msg.arg1 == ERROR_READ_EMPTY){
                        activity_.showAlertView("没有获取到数据，请重新贴卡！");
                    }
                    nfcManager_.reconvertStatus();

                    break;
                case MESSAGE_SOCKET:
                    activity_.dialogDimiss();
                    if (msg.arg1 == ERROT_SOCKET_CONNECT_FAILED) {
                        activity_.showAlertView("SOCKET连接失败");
                    } else if (msg.arg1 == ERROT_SOCKET_SEND_EXCEPTION) {
                        activity_.showAlertView("数据发送异常");
                    } else if (msg.arg1 == ERROT_SOCKET_RECEIVE_EXCEPTION) {
                        activity_.showAlertView("数据接收异常");
                    }
                    nfcManager_.reconvertStatus();
                    break;
                default:
                    break;
            }
        }
    };



    //读卡时向服务器发送的数据
    public String dataSendToServerForRead(String originData){
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(SINGNAL_READDATA_PRE);
        sBuilder.append(originData);
//        sBuilder.append(originData.substring(0,originData.length()-4));//截取掉后面的四位标志位
        return sBuilder.toString();
    }

    //读取时解析服务器返回的数据
    public CardInfo parseDataForRead(byte[] data) {
        Log.i(TAG, "**********数据解析开始**********");
        ReadCardInfo iWant = new ReadCardInfo();

        try {
            // 返回码
            String retCode = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 5, 9)), 4).trim();
            Log.i(TAG, "返回码: " + retCode);
            iWant.retCode = retCode;

            if (retCode.equals(SINGNAL_SOCKET_RECEIVEDATA_SUCCESS)) {
                // 卡类型
                String cardType = CodeFormat
                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 9, 11)), 2)
                        .trim();
                Log.i(TAG, "卡类型: " + cardType);
                iWant.cardType = cardType;

                // 卡面气量
                String gases = CodeFormat
                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 11, 15)), 4)
                        .trim();
                Log.i(TAG, "卡面气量: " + gases);
                iWant.gases = gases;

                // 用户号
                String userID = CodeFormat
                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 15, 31)), 16)
                        .trim();
                Log.i(TAG, "用户号: " + userID);
                iWant.userID = userID;

                // 用户名称
                String userName = CodeFormat
                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 31, 95)), 64)
                        .trim();
                Log.i(TAG, "用户名称: " + userName);
                iWant.username = userName;

                // 用户地址
                String userAddr = CodeFormat
                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 95, 159)), 64)
                        .trim();
                Log.i(TAG, "用户地址: " + userAddr);
                iWant.userAddr = userAddr;

                // 用户（用气）性质
                String userDesc = CodeFormat
                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 159, 191)), 32)
                        .trim();
                Log.i(TAG, "用户（用气）性质: " + userDesc);
                iWant.userDesc = userDesc;

                // 用户状态
                String userSta = CodeFormat
                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 191, 195)), 4)
                        .trim();
                Log.i(TAG, "用户状态: " + userSta);
                iWant.userSta = userSta;

                // 购气单价
                String price = CodeFormat
                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 195, 201)), 6)
                        .trim();
                double money = Arith.div(Integer.parseInt(price), 100.0);
                Log.i(TAG, "购气单价: " + money);
                iWant.price = money;

                // 最大可购气量
                String maxPurchase = CodeFormat
                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 201, 213)), 12)
                        .trim();
                Log.i(TAG, "最大可购气量: " + maxPurchase);
                iWant.maxPurchase = maxPurchase;

                // 最小可购气量
                String minPurchase = CodeFormat
                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 213, 225)), 12)
                        .trim();
                Log.i(TAG, "最小可购气量: " + minPurchase);
                iWant.minPurchase = minPurchase;
            }

        }
        catch (Exception e) {
            Log.i(TAG, "数据解析异常");
        }

        return iWant;
    };


    //写卡时向服务器发送的数据
    public String dataSendToServerForWrite(String originData,int toBeWritten){
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(SINGNAL_WRITEDATA_PRE);
        //右对齐，共四位，左补0，十进制
        String writeStr = String.format("% 4d",toBeWritten);
        if (writeStr.length() > 4)
            writeStr = "   0";
        sBuilder.append(writeStr);
        sBuilder.append(originData);
        return sBuilder.toString();
    }

    //写卡时解析服务器返回数据
    public CardInfo parseDataForWrite(byte[] data) {

        WriteCardInfo iWant = new WriteCardInfo();

        try {
            // 交易获申请日期
            String transDate = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 9, 17)), 8)
                    .trim();
            Log.i(TAG, "交易获申请日期: " + transDate);
            iWant.transDate = transDate;

            // 交易获申请时间
            String transTime = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 17, 21)), 4)
                    .trim();
            Log.i(TAG, "交易获申请时间: " + transTime);
            iWant.transTime = transTime;

            // 公司顺序号
            String ComSeq = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 21, 29)), 8)
                    .trim();
            Log.i(TAG, "公司顺序号: " + ComSeq);
            iWant.comSeq = ComSeq;

            // 卡类型
            String cardType = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 29, 31)), 2)
                    .trim();
            Log.i(TAG, "卡类型: " + cardType);
            iWant.cardType = cardType;

            // 用户号
            String userID = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 31, 47)), 16)
                    .trim();
            Log.i(TAG, "用户号: " + userID);
            iWant.userID = userID;

            // 用户名称
            String userName = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 47, 111)), 64)
                    .trim();
            Log.i(TAG, "用户名称: " + userName);
            iWant.username = userName;

            // 用户（用气）性质
            String userDesc = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 111, 113)), 2)
                    .trim();
            Log.i(TAG, "用户（用气）性质: " + userDesc);
            iWant.userDesc = userDesc;

            // 购气金额
            String amount = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 113, 121)), 8)
                    .trim();
            double money = Arith.div(Integer.parseInt(amount), 100.0);
            Log.i(TAG, "购气金额: " + money);
            iWant.amount = money;

            // 购气次数
            String purchaseCount = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 121, 127)), 6)
                    .trim();
            Log.i(TAG, "购气次数: " + purchaseCount);
            iWant.purchaseCount = purchaseCount;

            // 密码长度
            String pwLength = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 127, 129)), 2)
                    .trim();
            Log.i(TAG, "密码长度: " + pwLength);
            iWant.pwLength = pwLength;

            // 写保护密码
            String verifyPw = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 129, 145)), 16)
                    .trim();
            Log.i(TAG, "写保护密码: " + verifyPw);
            iWant.verifyPw = verifyPw;

            // 新密码
            String newPw = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 145, 161)), 16)
                    .trim();
            Log.i(TAG, "新密码: " + newPw);
            iWant.pwNew = newPw;

            // 写卡起始地址
            String offset = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 161, 165)), 4)
                    .trim();
            Log.i(TAG, "写卡起始地址: " + offset);
            iWant.offset = offset;

            // 写卡长度
            String wrLength = CodeFormat
                    .hexStr2IntStr(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 165, 167)))
                    .trim();
            Log.i(TAG, "写卡长度ַ: " + wrLength);
            iWant.wrLength = wrLength;

            // 写卡数据
            String dataBuf = CodeFormat
                    .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 167, 679)), 512)
                    .trim();
            Log.i(TAG, "写卡数据: " + dataBuf);
            iWant.dataBuf = dataBuf;

        }
        catch (Exception e) {
            Log.i(TAG, "数据解析异常");
        }

        return iWant;
    }

    //错误代码
    private String getErrorStr(String retCode) {
        String mErr = "";

        if (retCode.equalsIgnoreCase("0001")) {
            mErr = "交易日期不匹配";
        } else if (retCode.equalsIgnoreCase("0002")) {
            mErr = "验证码错或数据被人为修改";
        } else if (retCode.equalsIgnoreCase("0003")) {
            mErr = "数据包格式不符合定义";
        } else if (retCode.equalsIgnoreCase("0004")) {
            mErr = "非法交易";
        } else if (retCode.equalsIgnoreCase("0005")) {
            mErr = "暂不支持本交易";
        } else if (retCode.equalsIgnoreCase("0007")) {
            mErr = "系统忙，请稍候再试";
        } else if (retCode.equalsIgnoreCase("0009")) {
            mErr = "数据库忙，请稍候再试";
        } else if (retCode.equalsIgnoreCase("0010")) {
            mErr = "数据更新失败";
        } else if (retCode.equalsIgnoreCase("1015")) {
            mErr = "客户资料不存在";
        } else if (retCode.equalsIgnoreCase("1016")) {
            mErr = "客户资料保密，不允许查询";
        } else if (retCode.equalsIgnoreCase("1019")) {
            mErr = "无所查购气记录";
        } else if (retCode.equalsIgnoreCase("1020")) {
            mErr = "客户不存在，无法查询购气记录";
        } else if (retCode.equalsIgnoreCase("2020")) {
            mErr = "写卡失败或卡面校验失败";
        } else if (retCode.equalsIgnoreCase("2021")) {
            mErr = "非法购气量";
        } else if (retCode.equalsIgnoreCase("2022")) {
            mErr = "无客户资料，不允许进行购气充值";
        } else if (retCode.equalsIgnoreCase("2023")) {
            mErr = "已达到购气限制值";
        } else if (retCode.equalsIgnoreCase("2024")) {
            mErr = "卡面信息与数据库不符";
        } else if (retCode.equalsIgnoreCase("2025")) {
            mErr = "客户状态异常，不允许进行购气充值(重复购气)";
        } else if (retCode.equalsIgnoreCase("2026")) {
            mErr = "无效卡";
        } else if (retCode.equalsIgnoreCase("2027")) {
            mErr = "已透支";
        } else if (retCode.equalsIgnoreCase("2028")) {
            mErr = "已冻结";
        } else if (retCode.equalsIgnoreCase("2030")) {
            mErr = "交易撤销失败";
        } else if (retCode.equalsIgnoreCase("2031")) {
            mErr = "交易不可撤销";
        } else if (retCode.equalsIgnoreCase("2035")) {
            mErr = "交易冲正失败";
        } else if (retCode.equalsIgnoreCase("2036")) {
            mErr = "交易不可冲正";
        } else if (retCode.equalsIgnoreCase("2037")) {
            mErr = "错误的冲正气量";
        } else if (retCode.equalsIgnoreCase("3035")) {
            mErr = "无发票信息";
        } else if (retCode.equalsIgnoreCase("3036")) {
            mErr = "发票不允许补打";
        } else if (retCode.equalsIgnoreCase("4005")) {
            mErr = "对账金额不符";
        } else if (retCode.equalsIgnoreCase("4006")) {
            mErr = "对账笔数不符";
        } else if (retCode.equalsIgnoreCase("4007")) {
            mErr = "对账金额、笔数均不符";
        } else if (retCode.equalsIgnoreCase("4008")) {
            mErr = "无匹配对账流水";
        } else if (retCode.equalsIgnoreCase("4009")) {
            mErr = "银行方成功，公司方未成功";
        } else if (retCode.equalsIgnoreCase("4010")) {
            mErr = "公司方成功，银行方未成功";
        } else if (retCode.equalsIgnoreCase("4011")) {
            mErr = "对账流水日期超限";
        }

        return mErr;
    };
}
