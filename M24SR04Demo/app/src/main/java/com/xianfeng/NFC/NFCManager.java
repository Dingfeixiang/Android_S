package com.xianfeng.NFC;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Parcelable;

import java.io.IOException;
import java.nio.charset.Charset;

import com.st.NFC.NFCApplication;
import com.st.NFC.NFCTag;

import com.xianfeng.Util.*;

public class NFCManager {

    // 外部保存变量
    private Activity activity_;

    //NFC
    // Constants for NFC feature state
    // - state is detected at class creation
    // - STATE_NFC_UNKNOWN is the initial state
    // - STATE_NFC_NOT_AVAILABLE is a final state (no NFC chip in current device)
    // - STATE_NFC_NOT_ENABLED is a transient state: NFC activation can be detected in onResume method (if user switched to paremeters menu and came back to the application)
    // - STATE_NFC_ENABLED is a transient state: NFC can be deactivated by end user, then need to be detected
    public enum NfcState {
        STATE_NFC_UNKNOWN,
        STATE_NFC_NOT_AVAILABLE,
        STATE_NFC_NOT_ENABLED,
        STATE_NFC_ENABLED
    }
    private NfcAdapter nfcAdapter_;
    private PendingIntent pendingIntent_;
    public static String[][] TECHLISTS; //NFC技术列表
    public static IntentFilter[] FILTERS; //过滤器
    static {
        try {
            // 创建一个处理NFC标签技术的数组
            TECHLISTS = new String[][] { { IsoDep.class.getName() }, { NfcA.class.getName() } };
            // 创建Intent过滤器
            FILTERS = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED, "*/*") };
        } catch (Exception ignored) {
        }
    }

    //初始化
    public void initAdapter(Activity activity) {

        System.out.println("初始化NFC");
        //初始化nfc适配器
        nfcAdapter_ = NfcAdapter.getDefaultAdapter(activity);
        //初始化卡片信息
        // 创建一个PendingIntent对象，以便Android系统能够在扫描到NFC标签时，用它来封装NFC标签的详细信息
        pendingIntent_ = PendingIntent.getActivity(activity, 0,
                new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        //保留外部变量
        activity_ = activity;
    }


    private NfcState nfcState_ = NfcState.STATE_NFC_UNKNOWN;
    // Check for available NFC Adapter
    public NfcState checkForAvailableNFC() {
        PackageManager pm = activity_.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            // NFC not available
            nfcState_ = NfcState.STATE_NFC_NOT_AVAILABLE;
        } else {
            if (!nfcAdapter_.isEnabled()) {
                // NFC not enabled
                nfcState_ = NfcState.STATE_NFC_NOT_ENABLED;
            } else {
                nfcState_ = NfcState.STATE_NFC_ENABLED;
            }
        }
        return nfcState_;
    }

    /*
    *  public
    * */
    //nfcAdapter指针
    public boolean isValid(){
        if (nfcAdapter_ != null && nfcAdapter_.isEnabled())
            return true;
        else
            return false;
    }

    // 在主线程中调用enableForegroundDispatch()方法，一旦NFC标签接触到手机，这个方法就会被激活
    public void enableForegroundDispatch(Activity activity){
        if (nfcAdapter_ != null) {
            // Route the NFC events to the next activity (Tag Info ?)
            nfcAdapter_.enableForegroundDispatch(activity, pendingIntent_,
                    null /*nfcFiltersArray*/, null /*nfcTechLists*/);
//            nfcAdapter_.enableForegroundDispatch(activity, pendingIntent_, FILTERS, TECHLISTS);
        }
    }

    //调用disableForegroundDispatch()方法当Activity挂起时禁用前台调用。
    public void disableForegroundDispatch(Activity activity){
        if (nfcAdapter_ != null)
            nfcAdapter_.disableForegroundDispatch(activity);
    }


    //////////////////////////////////////////////////////////////////////////////////

    public NFCTag getNFCTag(Intent intent){
        NFCTag tmpTag = null;
        String action = intent.getAction();
        if ((NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
                || (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))
                || (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action))){
            Tag rawTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (rawMsgs != null) {
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                tmpTag = new NFCTag(rawTag, msgs);
            } else {
                tmpTag = new NFCTag(rawTag);
            }
            NFCApplication.getApplication().setCurrentTag(tmpTag);
        }
        return tmpTag;
    }

    //从Intent中读卡
    public void readData(Intent intent,NFCCallback.ReadCallBack callback){
        System.out.println ("从intent中获取标签信息！");

        //从这里获取intent 并组装成ST NFCTag
        NFCTag nfcTag = getNFCTag(intent);
        NFCMiddleware middleware = new NFCMiddleware(nfcTag);
//        NFCApplication currentApp = NFCApplication.getApplication();
//        currentApp.setCurrentTag(nfcTag);
        if (callback != null)
            callback.readFinish(middleware.readTag(nfcTag));
    }

    //写卡操作
    public void writeData(String data,NFCCallback.TagCallBack tagCallBack){
        NFCApplication currentApp = NFCApplication.getApplication();
        NFCTag currentTag = currentApp.getCurrentTag();
        if ((currentTag == null) || (!currentTag.pingTag())) {
            if(tagCallBack != null)
                tagCallBack.currentTagStatus(NFCCallback.TagStatus.TAG_EMPTY);
            return;
        }
        NFCMiddleware middleware = new NFCMiddleware(currentApp.getCurrentTag());
        middleware.writeTag(data);

        if(tagCallBack != null)
            tagCallBack.currentTagStatus(NFCCallback.TagStatus.TAG_USED);
    }





    //关键处理函数，处理扫描到的NdefMessage
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

        /*
    * Writes an NdefMessage to a NFC tag
    */
    public static boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

}
