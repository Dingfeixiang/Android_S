package com.android.ble.util;

import java.util.ArrayList;
import java.util.Timer;

import com.android.ble.service.BluetoothLeService;

import android.bluetooth.BluetoothGattCharacteristic;

public class CommonData {
	public static BluetoothLeService mBluetoothLeService;
	public static BluetoothGattCharacteristic characteristic = null;

	public static String requsetnow = "";
	public static String mydata = "";
	public static String RC4_Data = "";

	public static int requsetype = 0;
	public static int lastcountfill = 0;
	public static int maxnow = 0;
	public static int MD5_identification = 0;

	public static boolean isendsend = true;
	public static boolean check15page = false;
	public static boolean lostpackage = false;

	public static int serial = 0;
	public static int pagecount = 0;
	public static int requesetcount = 0;
	public static int cardrequesetcount = 0;

	public static int outtimercountwait15 = 0;
	public static int outtimercountrecive = 0;
	public static int outtimercountwaitpackge = 0;

	public static int RC4_x = 0;
	public static int RC4_y = 0;

	public static ArrayList<byte[]> BledataAr = new ArrayList<byte[]>();
	public static ArrayList<byte[]> BledataRevArray = new ArrayList<byte[]>();

	public static Timer check15Timer = null;
	public static Timer checkreciveTimer = null;
	public static Timer checkpackgeTimer = null;

	public static String MD5_result3 = "";

	public static boolean isrespone = true;
}
