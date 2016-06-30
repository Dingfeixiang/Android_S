/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ble.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.ble.R;
import com.android.ble.service.BluetoothLeService;
import com.android.ble.socket.SocketThread;
import com.android.ble.user.util.Arith;
import com.android.ble.user.util.CodeFormat;
import com.android.ble.user.util.ThreadPoolUtils;
import com.android.ble.util.CommonData;
import com.android.ble.util.HyBleApduControl;
import com.android.ble.util.SampleGattAttributes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLEService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_UI = 2;
    public static final int MESSAGE_WRITEDATA = 3;
    public static final int MESSAGE_ERROR = 4;
    public static final int MESSAGE_SOCKET = 5;

    private static final int CARD_4442 = 8;
    private static final String CARD_READ_4442 = "010100";
    private static final String CARD_WRITE_4442 = "010200";
    private static final String CARD_CHECKPASS_4442 = "01030000";
    private static final String CARD_CHANGEPASS_4442 = "01050000";

    private EditText mCardName, mCardNo, mCardAmount, mCardPrice, mBuyAmount;
    private Button mCardRead, mCardWrite;

    private String mDeviceName, mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private SocketThread mSocketThread;

    private boolean mConnected = false;
    private boolean mState = false;
    private boolean mDataState = false;
    private int mCount = 0;
    private int MaxCount = 50;
    private String mData;
    private String mOldKey;
    private String mNewKey;

    private static CustomProgressDialog cpd_Dialog;

    private List<String> mList = new ArrayList<String>();

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initBluetoothParam()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            Log.e("mDeviceAddress", "" + mDeviceAddress);
            mBluetoothLeService.connect(mDeviceAddress);

            CommonData.mBluetoothLeService = mBluetoothLeService;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    //无法合上？
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            Bundle extras = intent.getExtras();
            HyBleApduControl mHYBLEAC = new HyBleApduControl();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

            } else if (BluetoothLeService.ACTION_GATT_FOLLOWWRITE.equals(action)) {
                Log.i(TAG, CommonData.mydata);
                mState = true;
                mList.add(CommonData.mydata);
            } else if (BluetoothLeService.ACTION_GATT_ERRORWRITE.equals(action)) {
                int errSerial = extras.getInt(BluetoothLeService.EXTRA_DATA);
                mHYBLEAC.ErrorRecovery(errSerial);
            } else if (BluetoothLeService.ACTION_GATT_PACKGEWRITE.equals(action)) {
                mHYBLEAC.Packgewrite();
            } else if (BluetoothLeService.ACTION_GATT_DIDWRITE.equals(action)) {
                boolean iswrite = extras.getBoolean(BluetoothLeService.EXTRA_DATA);
                mHYBLEAC.OnWritePackgeFollow(iswrite);
            }else {

            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (R.string.connected == resourceId) {
                    mCardRead.setEnabled(true);
                    mCardWrite.setEnabled(true);
                } else if (R.string.disconnected == resourceId) {
                    initUserInfo();

                    mCardRead.setEnabled(false);
                    mCardWrite.setEnabled(false);
                }
            }
        });
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            Log.e("service.uuid", uuid);

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                Log.i("UUID: ", uuid);
                if (SampleGattAttributes.HEART_WRITE.equals(uuid)) {
                    Log.i("UUID连接成功", "找到可写设备");
                    CommonData.characteristic = gattCharacteristic;
                }
                if (SampleGattAttributes.HEART_READ.equals(uuid)) {
                    Log.i("UUID连接成功", "找到可读设备");
                    mConnected = true;
                    setNotify(gattCharacteristic);
                    invalidateOptionsMenu();
                }
            }
        }
    }

    private void setNotify(BluetoothGattCharacteristic characteristic) {
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user
            // interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(characteristic);
        }

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = characteristic;
            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        mCardName = (EditText) findViewById(R.id.edt_card_name);
        mCardNo = (EditText) findViewById(R.id.edt_card_no);
        mCardAmount = (EditText) findViewById(R.id.edt_card_amount);
        mCardPrice = (EditText) findViewById(R.id.edt_card_price);
        mBuyAmount = (EditText) findViewById(R.id.edt_card_buyamount);

        mCardRead = (Button) findViewById(R.id.btn_readcard);
        mCardRead.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("TYPE", 0);

                operateCard(m);
            }
        });

        mCardWrite = (Button) findViewById(R.id.btn_writecard);
        mCardWrite.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("TYPE", 1);

                if (!mCardNo.getText().toString().trim().equalsIgnoreCase("")) {
                    if (!mBuyAmount.getText().toString().trim().equalsIgnoreCase("")) {
                        operateCard(m);
                    } else {
                        Toast.makeText(DeviceControlActivity.this, "请输入充值气量", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DeviceControlActivity.this, "请先进行读卡操作", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(DeviceControlActivity.this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void operateCard(Map<String, Object> param) {
        if (cpd_Dialog == null) {
            cpd_Dialog = CustomProgressDialog.createDialog(DeviceControlActivity.this);
        }

        if (!cpd_Dialog.isShowing()) {
            cpd_Dialog.show();
        }

        ThreadPoolUtils.execute(new sendCommand(param));
    }

    private class sendCommand implements Runnable {
        private Map<String, Object> param = null;
        private StringBuilder sBuilder = null;
        private Message msg = null;

        public sendCommand(Map<String, Object> param) {
            this.param = param;
        }

        @Override
        public void run() {
            int t = (Integer) param.get("TYPE");

            if (t == 0) {
                // 读卡
                readCard(CARD_READ_4442);
            } else if (t == 1) {
                // 写卡
                writeCard(CARD_WRITE_4442);
            }
        }

        private void readCard(String mHeader) {
            mList.clear();

            sBuilder = new StringBuilder();
            sBuilder.append(mHeader);
            // 偏移量
            sBuilder.append("00");
            // 长度
            sBuilder.append(CodeFormat.Integer2HexStr(128));
            operateCard_4442(sBuilder.toString());

            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mCount > MaxCount) {
                    mCount = 0;
                    break;
                } else {
                    if (mState) {
                        mCount = 0;
                        break;
                    }
                }

                mCount++;
            }

            mState = false;

            msg = new Message();
            Log.i(TAG, String.valueOf(mList.size()));
            if (mList.size() == 1) {
                if (mList.get(0).substring(mList.get(0).length() - 4, mList.get(0).length()).equalsIgnoreCase("9000")) {
                    sBuilder = new StringBuilder();
                    sBuilder.append(mHeader);
                    // 偏移量
                    sBuilder.append("80");
                    // 长度
                    sBuilder.append(CodeFormat.Integer2HexStr(128));
                    operateCard_4442(sBuilder.toString());

                    while (true) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (mCount > MaxCount) {
                            mCount = 0;
                            break;
                        } else {
                            if (mState) {
                                mCount = 0;
                                break;
                            }
                        }

                        mCount++;
                    }

                    mState = false;

                    if (mList.size() == 2) {
                        msg = new Message();
                        if (mList.get(1).substring(mList.get(1).length() - 4, mList.get(1).length())
                                .equalsIgnoreCase("9000")) {
                            msg.what = MESSAGE_READ;
                        } else {
                            Log.i(TAG, "卡片数据返回错误");

                            msg.what = MESSAGE_ERROR;
                            msg.arg1 = 1;
                        }
                    } else {
                        Log.i(TAG, "卡片无数据返回");

                        msg.what = MESSAGE_ERROR;
                        msg.arg1 = 0;
                    }
                } else {
                    msg.what = MESSAGE_ERROR;
                    msg.arg1 = 1;
                }
            } else {
                Log.i(TAG, "卡片无数据返回");

                msg.what = MESSAGE_ERROR;
                msg.arg1 = 0;
            }

            mHandler.sendMessage(msg);
        }

        private void writeCard(String mHeader) {
            mList.clear();

            Log.i(TAG, "开始回传气量和卡片信息");

            // 回传购气量
            getWriteData();

            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mCount > MaxCount) {
                    mCount = 0;
                    break;
                } else {
                    if (mDataState) {
                        mCount = 0;
                        break;
                    }
                }

                mCount++;
            }

            Log.i(TAG, "回传气量和卡片信息成功");

            mDataState = false;

            Log.i(TAG, "开始核对卡片密码");

            // 核对卡片密码
            sBuilder = new StringBuilder();
            sBuilder.append(CARD_CHECKPASS_4442);
            // 密码字节数
            sBuilder.append("03");
            // 密码
            sBuilder.append(mOldKey);

            operateCard_4442(sBuilder.toString());

            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mCount > MaxCount) {
                    mCount = 0;
                    break;
                } else {
                    if (mState) {
                        mCount = 0;
                        break;
                    }
                }

                mCount++;
            }

            mState = false;

            msg = new Message();
            if (!mList.isEmpty()) {
                if (mList.get(0).equalsIgnoreCase("9000")) {
                    Log.i(TAG, "核对卡片密码成功");

                    mList.clear();

                    Log.i(TAG, "开始写入卡片数据");

                    sBuilder = new StringBuilder();
                    sBuilder.append(CARD_WRITE_4442);
                    // 偏移量
                    sBuilder.append("20");
                    // 长度
                    sBuilder.append(CodeFormat.Integer2HexStr(224));
                    // 卡片数据
                    Log.i(TAG, "卡片数据: " + mData.substring(64, 512));
                    sBuilder.append(mData.substring(64, 512));

                    operateCard_4442(sBuilder.toString());

                    while (true) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (mCount > MaxCount) {
                            mCount = 0;
                            break;
                        } else {
                            if (mState) {
                                mCount = 0;
                                break;
                            }
                        }

                        mCount++;
                    }

                    mState = false;

                    if (!mList.isEmpty()) {
                        if (mList.get(0).equalsIgnoreCase("9000")) {
                            if (!mOldKey.equalsIgnoreCase(mNewKey)) {
                                // 更新密钥
                                mList.clear();

                                Log.i(TAG, "开始更新卡片密钥");

                                sBuilder = new StringBuilder();
                                sBuilder.append(CARD_CHANGEPASS_4442);
                                // 密码字节数
                                sBuilder.append("03");
                                // 密码
                                sBuilder.append(mNewKey);

                                operateCard_4442(sBuilder.toString());

                                while (true) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (mCount > MaxCount) {
                                        mCount = 0;
                                        break;
                                    } else {
                                        if (mState) {
                                            mCount = 0;
                                            break;
                                        }
                                    }

                                    mCount++;
                                }

                                mState = false;

                                if (!mList.isEmpty()) {
                                    if (mList.get(0).equalsIgnoreCase("9000")) {
                                        Log.i(TAG, "写卡成功,卡片密钥更新成功");

                                        mList.clear();

                                        // 写卡成功
                                        msg.what = MESSAGE_WRITE;
                                    } else {
                                        Log.i(TAG, "写卡成功,密钥更新失败");

                                        mList.clear();

                                        // 写卡成功,密钥更新失败
                                        msg.what = MESSAGE_ERROR;
                                        msg.arg1 = 4;
                                    }
                                } else {
                                    Log.i(TAG, "写卡成功,密钥更新失败");

                                    mList.clear();

                                    // 写卡成功,密钥更新失败
                                    msg.what = MESSAGE_ERROR;
                                    msg.arg1 = 4;
                                }
                            } else {
                                Log.i(TAG, "卡片数据写入成功");

                                mList.clear();

                                // 写卡成功
                                msg.what = MESSAGE_WRITE;
                            }
                        } else {
                            Log.i(TAG, "卡片数据写入失败");

                            mList.clear();

                            msg.what = MESSAGE_ERROR;
                            msg.arg1 = 3;
                        }
                    } else {
                        Log.i(TAG, "卡片无数据返回");

                        mList.clear();

                        msg.what = MESSAGE_ERROR;
                        msg.arg1 = 0;
                    }
                } else {
                    Log.i(TAG, "核对卡片密码失败");

                    mList.clear();

                    msg.what = MESSAGE_ERROR;
                    msg.arg1 = 2;
                }
            } else {
                Log.i(TAG, "卡片无数据返回");

                mList.clear();

                msg.what = MESSAGE_ERROR;
                msg.arg1 = 0;
            }

            mHandler.sendMessage(msg);
        }

        // 回传购气量
        private void getWriteData() {
            msg = new Message();
            msg.what = MESSAGE_WRITEDATA;
            mHandler.sendMessage(msg);
        }

        // 卡片操作
        private void operateCard_4442(String mData) {
            HyBleApduControl mBLEAC = new HyBleApduControl();
            mBLEAC.setcardrequest(mData, CARD_4442);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        StringBuilder sBuilder = null;

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_READ:
                    mData = mList.get(0).substring(0, mList.get(0).length() - 4)
                            + mList.get(1).substring(0, mList.get(1).length() - 4);

                    sBuilder = new StringBuilder();
                    sBuilder.append("LYGASGS150100010001");
                    sBuilder.append(mList.get(0).substring(0, mList.get(0).length() - 4));
                    sBuilder.append(mList.get(1).substring(0, mList.get(1).length() - 4));

                    startSocket(sBuilder.toString(), MESSAGE_READ);

                    break;
                case MESSAGE_WRITE:
                    if (cpd_Dialog != null && cpd_Dialog.isShowing()) {
                        cpd_Dialog.dismiss();
                        cpd_Dialog = null;
                    }

                    initUserInfo();

                    Toast.makeText(DeviceControlActivity.this, "写卡成功", Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_UI:
                    byte[] data = (byte[]) msg.obj;

                    try {
                        Log.i(TAG, "**********数据解析开始**********");
                        // 返回码
                        String retCode = CodeFormat
                                .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 5, 9)), 4).trim();
                        Log.i(TAG, "返回码: " + retCode);

                        if (retCode.equalsIgnoreCase("0000")) {
                            if (msg.arg1 == 0) {
                                if (cpd_Dialog != null && cpd_Dialog.isShowing()) {
                                    cpd_Dialog.dismiss();
                                    cpd_Dialog = null;
                                }

                                // 卡类型
                                String cardType = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 9, 11)), 2)
                                        .trim();
                                Log.i(TAG, "卡类型: " + cardType);

                                // 卡面气量
                                String gases = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 11, 15)), 4)
                                        .trim();
                                mCardAmount.setText(gases);
                                Log.i(TAG, "卡面气量: " + gases);

                                // 用户号
                                String userID = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 15, 31)), 16)
                                        .trim();
                                mCardNo.setText(userID);
                                Log.i(TAG, "用户号: " + userID);

                                // 用户名称
                                String userName = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 31, 95)), 64)
                                        .trim();
                                mCardName.setText(userName);
                                Log.i(TAG, "用户名称: " + userName);

                                // 用户地址
                                String userAddr = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 95, 159)), 64)
                                        .trim();
                                Log.i(TAG, "用户地址: " + userAddr);

                                // 用户（用气）性质
                                String userDesc = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 159, 191)), 32)
                                        .trim();
                                Log.i(TAG, "用户（用气）性质: " + userDesc);

                                // 用户状态
                                String userSta = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 191, 195)), 4)
                                        .trim();
                                Log.i(TAG, "用户状态: " + userSta);

                                // 购气单价
                                String price = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 195, 201)), 6)
                                        .trim();
                                double p = Arith.div(Integer.parseInt(price), 100.0);
                                mCardPrice.setText(String.valueOf(p));
                                Log.i(TAG, "购气单价: " + p);

                                // 最大可购气量
                                String maxPurchase = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 201, 213)), 12)
                                        .trim();
                                Log.i(TAG, "最大可购气量: " + maxPurchase);

                                // 最小可购气量
                                String minPurchase = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 213, 225)), 12)
                                        .trim();
                                Log.i(TAG, "最小可购气量: " + minPurchase);

                                Toast.makeText(DeviceControlActivity.this, "读卡成功", Toast.LENGTH_SHORT).show();
                            } else if (msg.arg1 == 3) {
                                // 交易获申请日期
                                String transDate = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 9, 17)), 8)
                                        .trim();
                                Log.i(TAG, "交易获申请日期: " + transDate);
                                // 交易获申请时间
                                String transTime = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 17, 21)), 4)
                                        .trim();
                                Log.i(TAG, "交易获申请时间: " + transTime);
                                // 公司顺序号
                                String ComSeq = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 21, 29)), 8)
                                        .trim();
                                Log.i(TAG, "公司顺序号: " + ComSeq);
                                // 卡类型
                                String cardType = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 29, 31)), 2)
                                        .trim();
                                Log.i(TAG, "卡类型: " + cardType);
                                // 用户号
                                String userID = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 31, 47)), 16)
                                        .trim();
                                Log.i(TAG, "用户号: " + userID);
                                // 用户名称
                                String userName = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 47, 111)), 64)
                                        .trim();
                                mCardName.setText(userName);
                                Log.i(TAG, "用户名称: " + userName);
                                // 用户（用气）性质
                                String userDesc = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 111, 113)), 2)
                                        .trim();
                                Log.i(TAG, "用户（用气）性质: " + userDesc);
                                // 购气金额
                                String amount = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 113, 121)), 8)
                                        .trim();
                                double money = Arith.div(Integer.parseInt(amount), 100.0);
                                Log.i(TAG, "购气金额: " + money);
                                // 购气次数
                                String purchaseCount = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 121, 127)), 6)
                                        .trim();
                                Log.i(TAG, "购气次数: " + purchaseCount);
                                // 密码长度
                                String pwLength = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 127, 129)), 2)
                                        .trim();
                                Log.i(TAG, "密码长度: " + pwLength);
                                // 写保护密码
                                String verifyPw = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 129, 145)), 16)
                                        .trim();
                                mOldKey = verifyPw;
                                Log.i(TAG, "写保护密码: " + verifyPw);
                                // 新密码
                                String newPw = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 145, 161)), 16)
                                        .trim();
                                mNewKey = newPw;
                                Log.i(TAG, "新密码: " + newPw);
                                // 写卡起始地址
                                String offset = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 161, 165)), 4)
                                        .trim();
                                Log.i(TAG, "写卡起始地址: " + offset);
                                // 写卡长度
                                String wrLength = CodeFormat
                                        .hexStr2IntStr(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 165, 167)))
                                        .trim();
                                Log.i(TAG, "写卡长度ַ: " + wrLength);
                                // 写卡数据
                                String dataBuf = CodeFormat
                                        .hexToStringGBK(CodeFormat.byteArr2HexStr(Arrays.copyOfRange(data, 167, 679)), 512)
                                        .trim();
                                Log.i(TAG, "写卡数据: " + dataBuf);

                                // 更新写卡数据状态
                                mDataState = true;
                                // 写卡数据赋值给全局变量
                                mData = dataBuf;
                            }
                        } else {
                            if (cpd_Dialog != null && cpd_Dialog.isShowing()) {
                                cpd_Dialog.dismiss();
                                cpd_Dialog = null;
                            }

                            String tip = getErrorStr(retCode);
                            Log.i(TAG, "**错误信息: " + tip + "**");
                            Toast.makeText(DeviceControlActivity.this, "操作失败,错误信息:" + tip, Toast.LENGTH_SHORT).show();
                        }
                        Log.i(TAG, "**********数据解析结束**********");
                    } catch (Exception e) {
                        Log.i(TAG, "数据解析异常");
                        Toast.makeText(DeviceControlActivity.this, "数据解析异常", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case MESSAGE_WRITEDATA:
                    try {
                        String amount = mBuyAmount.getText().toString().trim();

                        sBuilder = new StringBuilder();
                        sBuilder.append("LYGASGS210100010001");
                        sBuilder.append(CodeFormat.getSpaceString(4 - amount.length()) + amount);
                        sBuilder.append(mData);

                        startSocket(sBuilder.toString(), MESSAGE_WRITEDATA);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case MESSAGE_ERROR:
                    if (cpd_Dialog != null && cpd_Dialog.isShowing()) {
                        cpd_Dialog.dismiss();
                        cpd_Dialog = null;
                    }

                    if (msg.arg1 == 0) {
                        Toast.makeText(DeviceControlActivity.this, "卡片无数据返回", Toast.LENGTH_SHORT).show();
                    } else if (msg.arg1 == 1) {
                        Toast.makeText(DeviceControlActivity.this, "卡片数据返回错误", Toast.LENGTH_SHORT).show();
                    } else if (msg.arg1 == 2) {
                        Toast.makeText(DeviceControlActivity.this, "核对卡片密钥错误", Toast.LENGTH_SHORT).show();
                    } else if (msg.arg1 == 3) {
                        initUserInfo();

                        Toast.makeText(DeviceControlActivity.this, "卡片数据写入失败", Toast.LENGTH_SHORT).show();
                    } else if (msg.arg1 == 4) {
                        initUserInfo();

                        Toast.makeText(DeviceControlActivity.this, "卡片数据写入成功,密钥更新失败", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case MESSAGE_SOCKET:
                    if (cpd_Dialog != null && cpd_Dialog.isShowing()) {
                        cpd_Dialog.dismiss();
                        cpd_Dialog = null;
                    }

                    if (msg.arg1 == 0) {
                        Toast.makeText(DeviceControlActivity.this, "SOCKET连接失败", Toast.LENGTH_SHORT).show();
                    } else if (msg.arg1 == 1) {
                        Toast.makeText(DeviceControlActivity.this, "数据发送异常", Toast.LENGTH_SHORT).show();
                    } else if (msg.arg1 == 2) {
                        Toast.makeText(DeviceControlActivity.this, "数据接收异常", Toast.LENGTH_SHORT).show();
                    }

                    break;
                default:
                    break;
            }
        }

    };

    /**
     * 启动SOCKET
     *
     * @param data ����
     */
    private void startSocket(String data, int type) {
        mSocketThread = new SocketThread(mHandler, data, type);
        mSocketThread.start();
    }

    /**
     * UI���
     */
    private void initUserInfo() {
        mCardName.setText("");
        mCardNo.setText("");
        mCardAmount.setText("");
        mCardPrice.setText("");
        mBuyAmount.setText("");
    }

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;

        if (cpd_Dialog != null && cpd_Dialog.isShowing()) {
            cpd_Dialog.dismiss();
            cpd_Dialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_FOLLOWWRITE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_PACKGEWRITE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_ERRORWRITE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_REWRITE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DIDWRITE);
        return intentFilter;
    }
}
