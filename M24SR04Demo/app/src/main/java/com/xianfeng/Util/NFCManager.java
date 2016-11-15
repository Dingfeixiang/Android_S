package com.xianfeng.Util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.Parcelable;


/**
 * Created by xianfeng on 2016/11/14.
 */

public class NFCManager {
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
    private IsoDep isodep_; //ISO14443-4 NFC操作

    // 外部界面
    private Activity activity_;
    //初始化
    public void initAdapter(Activity activity) {

        System.out.println("初始化NFC");
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

    public void enableForegroundDispatch(Activity activity){
        if (nfcAdapter_ != null) {
            nfcAdapter_.enableForegroundDispatch(activity, pendingIntent_, FILTERS, TECHLISTS);
        }
    }
    public void disableForegroundDispatch(Activity activity){
        if (nfcAdapter_ != null)
            nfcAdapter_.disableForegroundDispatch(activity);
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
//                readData();
            }
        }else {
//            activity_.refreshStatus("");
        }
    }
}
