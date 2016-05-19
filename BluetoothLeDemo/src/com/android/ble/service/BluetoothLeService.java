package com.android.ble.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.android.ble.util.ByteUtil;
import com.android.ble.util.CommonData;
import com.android.ble.util.SampleGattAttributes;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BluetoothLeService extends Service {
	private static final String TAG = BluetoothLeService.class.getSimpleName();
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;

	public static final String ACTION_GATT_CONNECTED = "com.android.ble.ACTION_GATT_CONNECTED";
	public static final String ACTION_GATT_DISCONNECTED = "com.android.ble.ACTION_GATT_DISCONNECTED";
	public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.android.ble.ACTION_GATT_SERVICES_DISCOVERED";
	public static final String ACTION_DATA_AVAILABLE = "com.android.ble.ACTION_DATA_AVAILABLE";
	public static final String ACTION_GATT_DIDWRITE = "com.android.ble.ACTION_GATT_DIDWRITE";
	public static final String ACTION_GATT_REWRITE = "com.android.ble.ACTION_GATT_REWRITE";
	public static final String ACTION_GATT_ERRORWRITE = "com.android.ble.ACTION_GATT_ERRORWRITE";
	public static final String ACTION_GATT_FOLLOWWRITE = "com.android.ble.ACTION_GATT_FOLLOWWRITE";

	public static final String ACTION_GATT_PACKGEWRITE = "com.android.ble.ACTION_GATT_PACKGEWRITE";
	public static final String ACTION_GATT_PACKGEMD5 = "com.android.ble.ACTION_GATT_PACKGEMD5";
	public static final String ACTION_GATT_PACKGEMD5RE2 = "com.android.ble.ACTION_GATT_PACKGEMD5RE2";
	public static final String ACTION_GATT_UPDATA = "com.android.ble.ACTION_GATT_UPDATA";
	public static final String EXTRA_DATA = "com.android.ble.EXTRA_DATA";
	public static final UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == 2) {
				String intentAction = "com.android.ble.ACTION_GATT_CONNECTED";
				BluetoothLeService.this.broadcastUpdate(intentAction);
				Log.i(BluetoothLeService.TAG, "Connected to GATT server.");
				Log.i(BluetoothLeService.TAG, "Attempting to start service discovery:"
						+ BluetoothLeService.this.mBluetoothGatt.discoverServices());
			} else if (newState == 0) {
				String intentAction = "com.android.ble.ACTION_GATT_DISCONNECTED";
				Log.i(BluetoothLeService.TAG, "Disconnected from GATT server.");
				BluetoothLeService.this.broadcastUpdate(intentAction);
			} else {
				Log.e("", "链接失败");
			}
		}

		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			super.onReliableWriteCompleted(gatt, status);
		}

		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == 0)
				BluetoothLeService.this.broadcastUpdate("com.android.ble.ACTION_GATT_SERVICES_DISCOVERED");
			else
				Log.w(BluetoothLeService.TAG, "onServicesDiscovered received: " + status);
		}

		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			System.out.println("onDescriptorWriteonDescriptorWrite = " + status + ", descriptor ="
					+ descriptor.getUuid().toString());
		}

		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			if (characteristic.getValue() != null) {
				String tempvale = ByteUtil.bytesToHexString(characteristic.getValue());
				Log.e("收到的数据:  ", tempvale);
				dataprocessing(characteristic.getValue());
			}

			Log.e("", "--------onCharacteristicChanged-----");
		}

		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			System.out.println("rssi = " + rssi);
		}

		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			boolean writeok;
			if (status == 0) {
				writeok = true;
				Log.e("", "--------write success----- status:" + status);
			} else {
				writeok = false;
				Log.e("", "--------write faile----- status:" + status);
			}
			BluetoothLeService.this.broadcastUpdate("com.android.ble.ACTION_GATT_DIDWRITE", writeok);
		}
	};

	private final IBinder mBinder = new LocalBinder();

	public void dataprocessing(byte[] data) {
		String datatemp = ByteUtil.byteArr2HexStr(data);
		if ((data.length >= 2) && (datatemp.equals("101500"))) {
			Log.e("1015", "接收到1015报文");

			CommonData.check15page = true;
			if (CommonData.check15Timer != null) {
				CommonData.check15Timer.cancel();
				CommonData.check15Timer = null;
			}
			CommonData.maxnow = 0;
			CommonData.lastcountfill = 0;
			CommonData.outtimercountwait15 = 0;

			CommonData.BledataRevArray.clear();

			if (CommonData.checkreciveTimer != null) {
				CommonData.checkreciveTimer.cancel();
				CommonData.checkreciveTimer = null;
			}
			CommonData.checkreciveTimer = new Timer();
			CommonData.checkreciveTimer.schedule(new TimerTask() {
				public void run() {
					BluetoothLeService.this.broadcastUpdate("com.android.ble.ACTION_GATT_REWRITE");
				}
			}, 3000L);

			return;
		}
		if ((datatemp.substring(0, 4).equals("1011")) || (datatemp.substring(0, 4).equals("1012"))
				|| (datatemp.substring(0, 4).equals("1014"))) {
			Log.e("1011", "接收到正确回复报文11");

			CommonData.check15page = true;
			if (CommonData.check15Timer != null) {
				CommonData.check15Timer.cancel();
				CommonData.check15Timer = null;
			}
			CommonData.maxnow = 0;
			CommonData.lastcountfill = 0;
			CommonData.outtimercountwait15 = 0;

			CommonData.BledataRevArray.clear();
		}

		if (CommonData.check15page) {
			if (CommonData.checkreciveTimer != null) {
				CommonData.checkreciveTimer.cancel();
				CommonData.checkreciveTimer = null;
				CommonData.outtimercountrecive = 0;
			}

			if (CommonData.checkpackgeTimer != null) {
				CommonData.checkpackgeTimer.cancel();
				CommonData.checkpackgeTimer = null;
				CommonData.outtimercountwaitpackge = 0;
			}

			int max = ((data[0] >> 4) + 16) % 16;
			Log.e("max", String.valueOf(max));
			int current = ((data[0] & 0xF) + 16) % 16;

			if ((CommonData.lostpackage) && (current != CommonData.lastcountfill + 1)) {
				return;
			}
			CommonData.lostpackage = false;

			if ((CommonData.BledataRevArray.size() == 0) && (current != 0)) {
				broadcastUpdate("com.android.ble.ACTION_GATT_ERRORWRITE", 0);
				return;
			}

			if ((CommonData.BledataRevArray.size() == 0) && (current == 0)) {
				CommonData.maxnow = max;
				CommonData.lastcountfill = current;
			} else {
				if (current <= CommonData.lastcountfill) {
					Log.e("", "重复包丢弃");
					return;
				}

				if (current - CommonData.lastcountfill > 1) {
					Log.e("", "第" + (CommonData.lastcountfill + 2) + "包丢失");
					broadcastUpdate("com.android.ble.ACTION_GATT_ERRORWRITE", CommonData.lastcountfill + 1);
					CommonData.lostpackage = true;
					return;
				}

				if ((CommonData.BledataRevArray.size() != 0) && (CommonData.maxnow != max)) {
					Log.e("", "最后一个包丢失");
					return;
				}
				CommonData.lastcountfill = current;
			}

			Log.e("current 和   maxnow", current + "      " + (CommonData.maxnow - 1));
			if (current == CommonData.maxnow - 1) {
				CommonData.BledataRevArray.add(data);

				for (int i = 0; i < CommonData.BledataRevArray.size(); ++i) {
					if (i == 0) {
						if (CommonData.requsetype == 1) {
							CommonData.mydata = ByteUtil.bytesToHexString((byte[]) CommonData.BledataRevArray.get(i))
									.substring(12);
							Log.e("截取数据1", CommonData.mydata);
						} else if (CommonData.requsetype == 2 || CommonData.requsetype == 8) {
							CommonData.mydata = ByteUtil.bytesToHexString((byte[]) CommonData.BledataRevArray.get(i))
									.substring(12);
							Log.e("截取数据2", CommonData.mydata);
						} else if (CommonData.requsetype == 4) {
							CommonData.mydata = ByteUtil.bytesToHexString((byte[]) CommonData.BledataRevArray.get(i))
									.substring(8);
							Log.e("截取数据3", CommonData.mydata);
						}
					} else {
						CommonData.mydata += ByteUtil.bytesToHexString((byte[]) CommonData.BledataRevArray.get(i))
								.substring(2);
					}
				}

				Log.e("完整数据：", CommonData.mydata);
				CommonData.outtimercountwaitpackge = 0;

				broadcastUpdate("com.android.ble.ACTION_GATT_FOLLOWWRITE");

				CommonData.BledataRevArray.clear();
				CommonData.BledataAr.clear();
				CommonData.check15page = false;
				if (CommonData.checkreciveTimer != null) {
					CommonData.checkreciveTimer.cancel();
					CommonData.checkreciveTimer = null;
				}

				return;
			}

			CommonData.BledataRevArray.add(data);
			CommonData.checkpackgeTimer = new Timer();
			CommonData.checkpackgeTimer.schedule(new TimerTask() {
				public void run() {
					BluetoothLeService.this.broadcastUpdate("com.android.ble.ACTION_GATT_PACKGEWRITE");
				}
			}, 200L);
		}
	}

	private void broadcastUpdate(String action) {
		Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(String action, boolean Ok) {
		Intent intent = new Intent(action);
		intent.putExtra("com.android.ble.EXTRA_DATA", Ok);
		Log.e("", "发送广播带上成功与否标志");
		sendBroadcast(intent);
	}

	private void broadcastUpdate(String action, int type) {
		Intent intent = new Intent(action);
		intent.putExtra("com.android.ble.EXTRA_DATA", type);
		sendBroadcast(intent);
	}

	// private void broadcastUpdate(String action, BluetoothGattCharacteristic
	// characteristic) {
	// Intent intent = new Intent(action);
	//
	// if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
	// String value = characteristic.getStringValue(0);
	// intent.putExtra("com.android.ble.EXTRA_DATA", value);
	// } else {
	// byte[] data = characteristic.getValue();
	// if ((data != null) && (data.length > 0)) {
	// StringBuilder stringBuilder = new StringBuilder(data.length);
	// for (byte byteChar : data)
	// stringBuilder.append(String.format("%02X ", new Object[] {
	// Byte.valueOf(byteChar) }));
	// intent.putExtra("com.android.ble.EXTRA_DATA",
	// new String(data) + "\n" + stringBuilder.toString());
	// }
	// }
	// sendBroadcast(intent);
	// }

	@SuppressLint("ShowToast")
	public boolean initBluetoothParam() {
		if (this.mBluetoothManager == null) {
			this.mBluetoothManager = ((BluetoothManager) getSystemService("bluetooth"));
			if (this.mBluetoothManager == null) {
				Toast.makeText(this, "Bluetooth初始化失败", 0).show();
				return false;
			}
		}
		this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
		if (this.mBluetoothAdapter == null) {
			Toast.makeText(this, "不能获得BluetoothAdapter", 0).show();
			return false;
		}
		return true;
	}

	public boolean connect(String address) {
		if ((this.mBluetoothAdapter == null) || (address == null)) {
			return false;
		}
		if ((this.mBluetoothDeviceAddress != null) && (address.equals(this.mBluetoothDeviceAddress))
				&& (this.mBluetoothGatt != null)) {
			Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
			if (this.mBluetoothGatt.connect()) {
				return true;
			}
			return false;
		}

		BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			Log.v("device of null", "device of null");
			return false;
		}

		this.mBluetoothGatt = device.connectGatt(this, false, this.mGattCallback);
		return true;
	}

	public void disconnect() {
		if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null)) {
			return;
		}
		this.mBluetoothGatt.disconnect();
	}

	public void close() {
		if (this.mBluetoothGatt == null) {
			return;
		}
		this.mBluetoothGatt.close();
		this.mBluetoothGatt = null;
	}

	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null)) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		this.mBluetoothGatt.readCharacteristic(characteristic);
	}

	public void writeCharacteristic(BluetoothGattCharacteristic mWriteCaracteristic, byte[] data) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null || mWriteCaracteristic == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mWriteCaracteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
		mWriteCaracteristic.setValue(data);
		Log.e("write :", "" + ByteUtil.bytesToHexString(data));
		this.mBluetoothGatt.writeCharacteristic(mWriteCaracteristic);
	}

	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
		if ((this.mBluetoothAdapter == null) || (this.mBluetoothGatt == null)) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		if (SampleGattAttributes.HEART_READ.equals(characteristic.getUuid().toString())) {
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	public IBinder onBind(Intent intent) {
		return this.mBinder;
	}

	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	public List<BluetoothGattService> getServices() {
		if (this.mBluetoothGatt == null)
			return null;
		return this.mBluetoothGatt.getServices();
	}

	public BluetoothGattService getservice(UUID uuid) {
		BluetoothGattService service = null;
		if (this.mBluetoothGatt != null) {
			service = this.mBluetoothGatt.getService(uuid);
		}
		return service;
	}

	public class LocalBinder extends Binder {
		public LocalBinder() {
		}

		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}
}
