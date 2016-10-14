package com.xianfeng.sanyademo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import java.util.HashMap;
import java.util.Iterator;


//WM
import com.mwcard.Reader;
import com.mwcard.ReaderAndroidUsb;
import com.mwcard.ReaderAndroidCom;
import com.mwreader.bluetooth.ClsUtils;
import com.mwreader.bluetooth.SearchActivity;



public class DetialActivity extends AppCompatActivity {

    private static final String TAG = null;
    //布局
    EditText    username,address,number,gasAmount;
    TextView    moneyView;
    Button      submit,facture;

    //WM相关
    public static Reader myReader;   // =new ReaderAndroidUsb();
    public static ReaderAndroidUsb readerAndroidUsb; // 安卓usb打开方式跟串口方式不一样



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detial);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //初始化布局
        initView();

        


    }


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
    public int initUsbDevice() {
        int st = 1;
        ContextWrapper mContext = null;
        final String ACTION_USB_PERMISSION = "com.example.hellojni.USB_PERMISSION";
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(this.mUsbReceiver, filter);

        UsbManager manager = (UsbManager) getSystemService("usb");
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

    //交互逻辑
    class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.read:

                    break;
                case R.id.write:

                    break;
            }
        }
    }

    //布局相关
    void initView(){

        username = (EditText) findViewById(R.id.accountEdittext);
        address = (EditText) findViewById(R.id.pwdEdittext);//可以换行
        number = (EditText) findViewById(R.id.ntext);//限制10个长度
        gasAmount = (EditText) findViewById(R.id.atext);

        moneyView = (TextView) findViewById(R.id.mvalue);

        submit = (Button) findViewById(R.id.read);
        facture = (Button) findViewById(R.id.write);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.clear).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:

                break;
        }
        return true;
    }

}

