package com.xianfeng.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.tech.NfcA;
import android.os.Parcelable;
import android.util.Log;

import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.Tag;

import com.xianfeng.nfcdemo.NFCActivity;
import com.xianfeng.util.CodeFormat;
import com.xianfeng.assist.WriteCardInfo;

import com.broadstar.nfccardsdk.*;
import com.broadstar.nfccardsdk.exception.*;

/**
 * Created by xianfeng on 16/6/30.
 */


public class NfcManager{

//    private static NfcManager instance = new NfcManager();
//    private NfcManager (){}
//    public static NfcManager getInstance() { return instance; }

    //NFC
    private NfcAdapter nfcAdapter_;
    private PendingIntent pendingIntent_;
    public static String[][] TECHLISTS; //NFC技术列表
    public static IntentFilter[] FILTERS; //过滤器

    static {
        try {
            TECHLISTS = new String[][] { { IsoDep.class.getName() }, { NfcA.class.getName() } };

            FILTERS = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED, "*/*") };
        } catch (Exception ignored) {
        }
    }

    // 卡片操作
    private LogicCard card_;
    private NfcReader reader_;
    private IsoDep isodep_; //ISO14443-4 NFC操作

    // 外部界面
    private NFCActivity activity_;

    //初始化
    public void initAdapter(NFCActivity activity){

        System.out.println ("初始化NFC");
        //初始化nfc适配器
        nfcAdapter_ = NfcAdapter.getDefaultAdapter(activity);
        //初始化卡片信息
        pendingIntent_ = PendingIntent.getActivity(activity, 0,
                            new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        //保留外部变量
        activity_ = activity;
    }

    /*
    *  public
    * */
    //nfcAdapter指针
    public boolean isInvalid(){
        if (nfcAdapter_ == null)
            return true;
        else
            return false;
    }

    //nfc是否禁用
    public boolean isEnabled(){
        if (nfcAdapter_.isEnabled())
            return true;
        else
            return false;
    }

    public void enableForegroundDispatch(NFCActivity activity){
        if (nfcAdapter_ != null) {
            nfcAdapter_.enableForegroundDispatch(activity, pendingIntent_, FILTERS, TECHLISTS);
        }
    }
    public void disableForegroundDispatch(NFCActivity activity){
        if (nfcAdapter_ != null)
            nfcAdapter_.disableForegroundDispatch(activity);
    }


    /*
    *   卡片读写
    * */
    private String intentData_ = null;
    public String dataReaded(){return intentData_;}


    private String lastIntentData_ = "";
    public void reconvertStatus(){
        lastIntentData_ = "";
    }
    //从Intent中读卡
    public void readData(Intent intent){

        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
            return;
        }

        System.out.println ("从intent中获取标签信息！");
        //从intent中获取标签信息
        Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (p != null) {
            Tag tag = (Tag) p;
            isodep_ = IsoDep.get(tag);
            if (isodep_ != null){
                if (reader_ == null) {
                    reader_ = new NfcReader(isodep_);
                }
                else
                    reader_.setIsoDep(isodep_);
                try {
                    reader_.reset();
                } catch (ReaderException e) {
                    e.printStackTrace();
                    return;
                }
                if (card_ == null)
                    card_ = new LogicCard(reader_);
                readData();
            }
        }else {
            activity_.refreshStatus("");
        }
    }
    private void readData() {

        System.out.println ("读取数据");
        if (isodep_ == null) {
            activity_.displayToast("请重新贴卡");
            return;
        }
        try {
            byte[] data = readAllBlock();
            Log.d("数据长度", "" + data.length);

            //获取数据字符串
            String iWant = dealData(data);
            System.out.println("获取到的字符串为："+iWant);
            if (!lastIntentData_.equals(iWant)){
                //记录卡片读取的原始信息
                intentData_ = iWant;
                activity_.displayToast("已获取到卡片数据");
                activity_.refreshStatus("请保持卡片贴合状态"); //测试顺利
                activity_.readCard();
                lastIntentData_ = iWant;
            }else {
                System.out.println("卡片为同一张卡片");
                return;
            }

        } catch (ReaderException e) {
            e.printStackTrace();
            activity_.displayToast("连接错误");
            reconvertStatus();
        } catch (APDUException e) {
            e.printStackTrace();
            activity_.displayToast("读取失败");
            reconvertStatus();
        }
    }
    private String dealData(byte[] data) {
        System.out.println ("处理数据");
        String iWant = CodeFormat.bytesToHexString(data);
        return iWant;
    }

    //使用服务器解析出的数据写卡
    public void writeCard(WriteCardInfo writeInfo){
        if (checkPassword(writeInfo.verifyPw)) {
            writeCard(writeInfo.offset, writeInfo.dataBuf);
        }else {
            activity_.displayToast("请检查检验密码是否正确");
        }
    }

    private boolean checkPassword(String password){
        if (isodep_ == null)
            return false;
        String iWant = password.replaceAll(" ", "");
        if(iWant.length() != 6) {
            activity_.displayToast("密码长度错误");
            return false;
        }
        try {
            card_.checkPW(CodeFormat.hexStringToBytes(iWant));
//            activity_.displayToast("密码校验成功");
            return true;
        } catch (ReaderException e) {
            e.printStackTrace();
            activity_.displayToast("密码校验失败: 连接错误");
            return false;
        } catch (APDUException e) {
            e.printStackTrace();
            String sw = e.getResponse();
            if (sw.substring(0,2).equals("63")) {
                activity_.displayToast("密码校验失败: 剩余次数" + sw.charAt(3));
            } else {
                activity_.displayToast("密码校验失败: " + e.getMessage());
            }
            return false;
        }
    }
    private void writeCard(String startAddress,String writeData){
        if (isodep_ == null) return;
        String address = startAddress.substring(2,startAddress.length());
        int add;
        if (address.length() != 0) {
            add = Integer.parseInt(address, 16);
            if (!(add <= 255 && add >= 0)) {
                activity_.displayToast("地址输入错误");
                return;
            }
        } else {
            activity_.displayToast("地址输入错误");
            return;
        }
        String data = writeData.replaceAll(" ", "").substring(64,512);
        if (data.length() == 0 || data.length() % 2 != 0) {
            activity_.displayToast("数据长度错误");
            return;
        }
        if (data.length() / 2 + add > LogicCard.MAX_LENGTH) {
            activity_.displayToast("地址或数据长度错误");
            return;
        }
        try {
            card_.writeBlock(add, 224, CodeFormat.hexStringToBytes(data));
            activity_.displayToast("数据写入成功");
        } catch (ReaderException | APDUException e) {
            e.printStackTrace();
            activity_.displayToast("数据写入失败: " + e.getMessage());
        }
    }

    /**
     * 读取逻辑卡主存储区全部数据
     * @return 数据
     */
    public synchronized byte[] readAllBlock() throws ReaderException, APDUException {
        //分两次读取，每次读取一半
        byte[] result = new byte[LogicCard.MAX_LENGTH];
        byte[] result1 = card_.readBlock(0, LogicCard.MAX_LENGTH / 2);
        byte[] result2 = card_.readBlock(LogicCard.MAX_LENGTH / 2, LogicCard.MAX_LENGTH / 2);
        System.arraycopy(result1, 0, result, 0, result1.length);
        System.arraycopy(result2, 0, result, LogicCard.MAX_LENGTH / 2, result2.length);
        return result;
    }


}
