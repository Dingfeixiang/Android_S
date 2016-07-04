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

import com.broadstar.nfccardsdk.LogicCard;
import com.broadstar.nfccardsdk.NfcReader;
import com.broadstar.nfccardsdk.exception.APDUException;
import com.broadstar.nfccardsdk.exception.ReaderException;

/**
 * Created by xianfeng on 16/6/30.
 */


public class NfcManager{

//    private static NfcManager instance = new NfcManager();
//    private NfcManager (){}
//    public static NfcManager getInstance() { return instance; }

    //NFC
    private NfcAdapter nfcAdapter_;
    private PendingIntent pendingIntent;
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
        pendingIntent = PendingIntent.getActivity(activity, 0,
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
            nfcAdapter_.enableForegroundDispatch(activity, pendingIntent, FILTERS, TECHLISTS);
        }
    }

    //
    public void disableForegroundDispatch(NFCActivity activity){
        if (nfcAdapter_ != null)
            nfcAdapter_.disableForegroundDispatch(activity);
    }


    /*
    *   从Intent中读取数据
    * */
    public void readDataFromIntent(Intent intent){

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
            activity_.refreshStatus(iWant);

        } catch (ReaderException e) {
            e.printStackTrace();
            activity_.displayToast("连接错误");
        } catch (APDUException e) {
            e.printStackTrace();
            activity_.displayToast("读取失败");
        }
    }

    private String dealData(byte[] data) {
        System.out.println ("处理数据");
        String stringData = CodeFormat.bytesToHexString(data);
        String result = stringData.substring(0, 32);
        for(int i=1; i<16; i++) {
            result = result + "\n" + stringData.substring(i * 32, (i + 1) * 32);
        }
        return result;
    }

    /**
     * 读取逻辑卡主存储区全部数据
     * @return 数据
     * @throws ReaderException
     * @throws APDUException
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
