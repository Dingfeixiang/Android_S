package com.xianfeng.sanyademo.util;

/**
 * Created by xianfeng on 2016/10/12.
 */

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

//WM
import com.mwcard.Reader;
import com.mwcard.ReaderAndroidCom;
import com.mwcard.ReaderAndroidUsb;
import com.mwreader.bluetooth.ClsUtils;
import com.mwreader.bluetooth.SearchActivity;


//import com.mwcard.ReaderAndroidCom;
//import com.mwreader.bluetooth.ClsUtils;
//import com.mwreader.bluetooth.SearchActivity;

//明华读卡器管理

import java.util.HashMap;
import java.util.Iterator;

public class MWManager {

    private static final String TAG = "MWManager";

    //操作单例
    private static MWManager instance;

    public static synchronized MWManager getHelper() {
        if (instance == null)
        {
            synchronized (MWManager.class)
            {
                if (instance == null)
                    instance = new MWManager();
            }
        }
        return instance;
    }

    //WM相关
    public static Reader myReader;   // new ReaderAndroidUsb();
    public static ReaderAndroidUsb readerAndroidUsb; // 安卓usb打开方式跟串口方式不一样

    //当前处理的Acitivity
    public Activity disposedActivity = null;




    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public int fold = 1;
        public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent) {

            if ("com.android.example.USB_PERMISSION".equals(paramAnonymousIntent.getAction())){

                try {

                    UsbDevice i = ((UsbDevice) paramAnonymousIntent.getParcelableExtra("device"));
                    if (paramAnonymousIntent.getBooleanExtra("permission", false))
                        ;

                    return;
                } finally {

                }
            }
        }
    };

    /**
     * @return 0 表示初始化成功，其他值表示失败
     */
    private int initUsbDevice() {

        assert disposedActivity!=null;

        int st = 1;
        ContextWrapper mContext = null;
        final String ACTION_USB_PERMISSION = "com.example.hellojni.USB_PERMISSION";

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(disposedActivity, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        disposedActivity.registerReceiver(this.mUsbReceiver, filter);

        UsbManager manager = (UsbManager) disposedActivity.getSystemService("usb");
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            Log.i(TAG, device.getDeviceName() + " "
                    + Integer.toHexString(device.getVendorId()) + " "
                    + Integer.toHexString(device.getProductId()));
            if (!ReaderAndroidUsb.isSupported(device)) {
                continue;
            }
            manager.requestPermission(device, mPermissionIntent);

            readerAndroidUsb = new ReaderAndroidUsb(manager);

            try {
                st = readerAndroidUsb.openReader(device);
                if (st >= 0) {
                    myReader = readerAndroidUsb;
                    st = 0;
                    break;
                }
            } catch (Exception e) {
                // TODO: handle exception
                Log.e(TAG, "openDevice failed");
            }
        }
        return st;
    }

//    public void scanDevices(){
//        Intent intent = new Intent();
//        intent.setClass(new MainActivity.this, SearchActivity.class);
//        disposedActivity.startActivityForResult(intent, 1);
//    }
    public boolean initManager(){
        int judge = initUsbDevice();
        if (judge == 0){
            return true;
        }else {
            return false;
        }
    }


    public void closeDevice(){
        try {
            ClsUtils.removeBond(SearchActivity.remoteDevice);
            myReader.closeReader();
        }catch (Exception ex){
            System.out.println("断开设备错误");
        }
    }

    public boolean openDevice(){
        boolean result = false;
        try {
            myReader = new ReaderAndroidCom();
            int st = myReader.openReader("/dev/ttyS1", "9600");
            if (st >= 0)
            {
                st = myReader.beep(2, 2, 2);
                result = true;
            }
            else
            {
                result = false;
            }
        }catch (Exception ex){

        }
        return result;
    }

}
