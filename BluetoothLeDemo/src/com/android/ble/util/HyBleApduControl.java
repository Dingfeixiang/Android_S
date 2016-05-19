package com.android.ble.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class HyBleApduControl {
    String MD5_result1 = "";
    String MD5_result2 = "";

    String MD5_1 = "0600";
    String MD5 = "8cc239eef3b66e3382c30f0747d436cb3d222f71913e1a9a990151dc52e4357d3132333435363738";
    String requset1 = "0007A0A40000027F10";
    String requset2 = "0007a0a40000026f3c";
    String requset3 = "0005a0c000000f";
    String requset4 = "0005a0b20104b0";
    String requset5 = "00b5a0dc0104b000001FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

    private int no1015 = 0;
    private int norecive = 0;
    private int nopackge = 0;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println("action = " + action);
            if ("com.android.ble.ACTION_GATT_CONNECTED".equals(action)) {
                System.out.println("action = " + action);
            } else {
                if (("com.android.ble.ACTION_GATT_DISCONNECTED".equals(action))
                        || ("com.android.ble.ACTION_GATT_SERVICES_DISCOVERED".equals(action))) {
                    return;
                }

                if ("com.android.ble.ACTION_DATA_AVAILABLE".equals(action)) {
                    Bundle extras = intent.getExtras();
                    String data = extras.getString("com.android.ble.EXTRA_DATA");

                    int i = extras.getByte("com.android.ble.EXTRA_DATA");
                } else if ("com.android.ble.ACTION_GATT_DIDWRITE".equals(action)) {
                    Bundle boolextras = intent.getExtras();

                    boolean iswrite = boolextras.getBoolean("com.android.ble.EXTRA_DATA");
                    // Log.e("发送了数据广播：", iswrite);
                    if (iswrite) {
                        HyBleApduControl.this.sendfollow(1);
                        CommonData.cardrequesetcount = 0;
                    } else if (CommonData.cardrequesetcount++ < 3) {
                        HyBleApduControl.this.sendfollow(0);
                    }

                } else if ("com.android.ble.ACTION_GATT_REWRITE".equals(action)) {
                    Log.e("recive超时", "等待接收数据超时");
                } else if ("com.android.ble.ACTION_GATT_ERRORWRITE".equals(action)) {
                    Bundle Errorextras = intent.getExtras();
                    int errSerial = Errorextras.getInt("com.android.ble.EXTRA_DATA");
                    HyBleApduControl.this.ErrorRecovery(errSerial);
                } else {
                    if ("com.android.ble.ACTION_GATT_FOLLOWWRITE".equals(action)) {
                        return;
                    }

                    if ("com.android.ble.ACTION_GATT_PACKGEWRITE".equals(action)) {
                        if (CommonData.outtimercountwaitpackge++ < 5) {
                            Log.e("lostpackge", "等待第" + (CommonData.lastcountfill + 2) + "超时");
                            HyBleApduControl.this.ErrorRecovery(CommonData.lastcountfill + 1);
                        } else {
                            CommonData.outtimercountwaitpackge = 0;
                            Log.e("lostpackge", "等待第" + (CommonData.lastcountfill + 2) + "超时,且发送5次纠错包");
                            CommonData.requsetnow = null;
                            HyBleApduControl.this.ResetAttribut();
                        }
                    } else if ("com.android.ble.ACTION_GATT_PACKGEMD5".equals(action)) {
                        String MD5_temp_result1 = HyBleApduControl.this.MD5 + CommonData.mydata;
                        Log.e("MD5result1", MD5_temp_result1);

                        HyBleApduControl.this.MD5_result1 = HyBleApduControl.this.MD5(MD5_temp_result1);
                        Log.e("加密之后：", HyBleApduControl.this.MD5_result1);

                        byte[] byt = new byte[8];
                        Random random = new Random();
                        random.nextBytes(byt);
                        String randnum = ByteUtil.bytesToHexString(byt);
                        Log.e("随机数", randnum);
                        HyBleApduControl.this.MD5_result1 += randnum;
                        HyBleApduControl.this.setcardrequest("0710" + HyBleApduControl.this.MD5_result1, 1);
                    } else if ("com.android.ble.ACTION_GATT_PACKGEMD5RE2".equals(action)) {
                        String MD5_temp_result2 = HyBleApduControl.this.MD5 + HyBleApduControl.this.MD5_result1;
                        Log.e("result2", MD5_temp_result2);

                        HyBleApduControl.this.MD5_result2 = HyBleApduControl.this.MD5(MD5_temp_result2);
                        if (HyBleApduControl.this.MD5_result2.equals(CommonData.mydata.substring(2))) {
                            CommonData.RC4_x = 0;
                            CommonData.RC4_y = 0;
                            Log.e("加密之后MD5_2：", HyBleApduControl.this.MD5_result2);
                            String MD5_temp_result3 = HyBleApduControl.this.MD5 + HyBleApduControl.this.MD5_result2;
                            Log.e("md5_3加密前：", MD5_temp_result3);
                            CommonData.MD5_result3 = HyBleApduControl.this.MD5(MD5_temp_result3);
                            Log.e("MD5_3加密后：", CommonData.MD5_result3);
                            CommonData.MD5_identification = 4;
                        }
                    } else {
                        "com.android.ble.ACTION_GATT_UPDATA".equals(action);
                    }
                }
            }
        }
    };

    public String MD5(String MD5_result) {
        String MD5_temp_result = "";

        byte[] md5 = new byte[350];
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md5 = md.digest(ByteUtil.hexStringToBytes(MD5_result));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        MD5_temp_result = ByteUtil.bytesToHexString(md5);

        return MD5_temp_result;
    }

    public void ResetAttribut() {
        CommonData.check15Timer = null;
        CommonData.checkpackgeTimer = null;
        CommonData.checkreciveTimer = null;

        CommonData.outtimercountrecive = 0;
        CommonData.outtimercountwait15 = 0;
        CommonData.outtimercountwaitpackge = 0;

        CommonData.lastcountfill = 0;
        CommonData.maxnow = 0;
        CommonData.check15page = false;
        CommonData.lostpackage = false;
        CommonData.BledataAr.clear();
        CommonData.BledataRevArray.clear();
    }

    // private static IntentFilter makeGattUpdateIntentFilter() {
    // IntentFilter intentFilter = new IntentFilter();
    // intentFilter.addAction("com.android.ble.ACTION_GATT_CONNECTED");
    // intentFilter.addAction("com.android.ble.ACTION_GATT_DISCONNECTED");
    // intentFilter.addAction("com.android.ble.ACTION_GATT_SERVICES_DISCOVERED");
    // intentFilter.addAction("com.android.ble.ACTION_DATA_AVAILABLE");
    // intentFilter.addAction("com.android.ble.ACTION_GATT_DIDWRITE");
    // intentFilter.addAction("com.android.ble.ACTION_GATT_REWRITE");
    // intentFilter.addAction("com.android.ble.ACTION_GATT_ERRORWRITE");
    // intentFilter.addAction("com.android.ble.ACTION_GATT_FOLLOWWRITE");
    // intentFilter.addAction("com.android.ble.ACTION_GATT_PACKGEWRITE");
    // intentFilter.addAction("com.android.ble.ACTION_GATT_PACKGEMD5");
    // intentFilter.addAction("com.android.ble.ACTION_GATT_PACKGEMD5RE2");
    // intentFilter.addAction("com.android.ble.ACTION_GATT_UPDATA");
    // return intentFilter;
    // }

    public void sendsmalldata(ArrayList<byte[]> data, int nowserial) {
        byte[] mydata = data.get(nowserial);

        CommonData.mBluetoothLeService.writeCharacteristic(CommonData.characteristic, mydata);
    }

    public void ErrorRecovery(int ErrorSerial) {
        byte[] temp = new byte[20];
        temp[0] = 16;
        temp[1] = 19;
        temp[2] = 0;
        temp[3] = 1;
        temp[4] = (byte) ErrorSerial;
        Log.e("", "发送纠错包：" + ByteUtil.bytesToHexString(temp));
        CommonData.mBluetoothLeService.writeCharacteristic(CommonData.characteristic, temp);
    }

    public void Packgewrite() {
        if (CommonData.outtimercountwaitpackge++ < 3) {
            nopackge++;
            Log.e("lostpackge", "等待第" + (CommonData.lastcountfill + 2) + "超时");
            ErrorRecovery(CommonData.lastcountfill + 1);
        } else {
            CommonData.outtimercountwaitpackge = 0;
            Log.e("lostpackge", "等待第" + (CommonData.lastcountfill + 2) + "超时,且发送3次纠错包");
            CommonData.requsetnow = null;
            ResetAttribut();
        }
    }

    public void OnWritePackgeFollow(boolean iswrite) {
        if (iswrite) {
            sendfollow(1);
            CommonData.cardrequesetcount = 0;

        } else {
            if (CommonData.cardrequesetcount++ < 3) {
                sendfollow(0);
            }
        }
    }

    public void sendfollow(int type) {
        if (type == 0) {
            sendsmalldata(CommonData.BledataAr, CommonData.serial);
        } else {
            CommonData.serial += 1;
            if (CommonData.serial < CommonData.pagecount) {
                Log.e("进入发送小包", "发送第：" + (CommonData.serial + 1));

                sendsmalldata(CommonData.BledataAr, CommonData.serial);
            } else {
                Log.e("", "发送结束");

                CommonData.isendsend = true;
                if (!CommonData.check15page) {
                    CommonData.check15Timer = new Timer();
                    CommonData.check15Timer.schedule(new TimerTask() {
                        public void run() {
                            if (CommonData.outtimercountwait15++ < 3) {
                                Log.e("", "没有收到15响应报文！");
                                HyBleApduControl.this.setcardrequest(CommonData.requsetnow, CommonData.requsetype);
                            } else {
                                CommonData.outtimercountwait15 = 0;
                                Log.e("", "没有收到15响应报文，兵超出发送最大次数限制！");
                                HyBleApduControl.this.ResetAttribut();
                                CommonData.requsetnow = null;
                            }
                        }
                    }, 3000L);
                }
            }
        }
    }

    public void setcardrequest(String value, int type) {
        int length = 0;

        CommonData.requsetnow = value;

        byte[] temp = new byte[350];

        if (CommonData.isendsend) {
            CommonData.check15page = false;
            CommonData.serial = 0;
            CommonData.pagecount = 0;
            CommonData.isendsend = false;
            CommonData.BledataAr.clear();
        }

        int oneortow = 0;
        oneortow = value.length();

        Log.e("输入字符串 长度value： ", String.valueOf(oneortow));

        if ((value == null) || (oneortow % 2 != 0)) {
            return;
        }

        CommonData.requsetype = type;
        byte[] valuedata;
        if (CommonData.MD5_identification < 3) {
            valuedata = ByteUtil.hexStringToBytes(value);
        } else {
            String data = "0123456789abcdef0123456789abcdef";
            byte[] minwen = new byte[256];
            for (int i = 0; i < 256; ++i) {
                minwen[i] = (byte) i;
            }
            valuedata = RC4.RC4Base(minwen, data);
            valuedata = RC4.RC4Base(ByteUtil.hexStringToBytes(value), CommonData.MD5_result3);
            Log.e("加密之后：", ByteUtil.bytesToHexString(valuedata));
        }

        length = valuedata.length;

        Log.e("有效数据段：", value + "长度：" + length);

        if (type == 2 || type == 8) {
            if (type == 2) {
                temp[0] = 0x02;
            } else {
                temp[0] = 0x08;
            }

            temp[1] = (byte) ((length + 2) / 256);
            temp[2] = (byte) ((length + 2) % 256);

            temp[3] = (byte) (length / 256);
            temp[4] = (byte) (length % 256);

            for (int i = 0; i < length; i++) {
                temp[i + 5] = valuedata[i];
            }
            SendData(temp, length + 5);
        } else if (type == 1 || type == 3) {

            temp[1] = (byte) (length / 256);
            temp[2] = (byte) (length % 256);

            for (int i = 0; i < length; i++) {
                temp[i + 3] = valuedata[i];
            }
            SendData(temp, length + 3);
        }
    }

    public void SendData(byte[] pbtye, int length) {
        byte[] temp;
        if (length % 19 == 0) {
            CommonData.pagecount = length / 19;
            temp = new byte[20];
        } else {
            CommonData.pagecount = length / 19 + 1;
            temp = new byte[length % 19 + 1];
        }
        int follow = 0;
        int lastcount = 0;

        Log.e("pagecount", String.valueOf(CommonData.pagecount));

        for (int n = 0; n < CommonData.pagecount; ++n) {
            byte[] twinty = new byte[20];
            twinty[0] = (byte) ((CommonData.pagecount << 4) + n);
            if (n + 1 == CommonData.pagecount) {
                if (length % 19 == 0) {
                    for (int m = 1; m <= 19; ++m) {
                        twinty[m] = pbtye[follow];
                        ++follow;
                        ++lastcount;
                    }
                } else {
                    for (int m = 1; m <= length % 19; ++m) {
                        twinty[m] = pbtye[follow];
                        ++follow;
                        ++lastcount;
                    }
                }

                for (int j = 0; j < lastcount + 1; ++j) {
                    temp[j] = twinty[j];
                }

                CommonData.BledataAr.add(temp);
            } else {
                for (int i = 1; i <= 19; ++i) {
                    twinty[i] = pbtye[follow];
                    ++follow;
                }
                CommonData.BledataAr.add(twinty);
            }
            Log.e("小包数据", ByteUtil.byteArr2HexStr((byte[]) CommonData.BledataAr.get(n)));
        }

        sendsmalldata(CommonData.BledataAr, CommonData.serial);
    }

}
