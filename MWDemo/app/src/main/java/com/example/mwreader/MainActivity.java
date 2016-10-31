package com.example.mwreader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.example.mwreader.R;
import com.mwcard.MedzoneBlueTooth;
import com.mwcard.ReaderAndroidBlueTooth;
import com.mwcard.ReaderAndroidCom;
//import com.mwcard.MedzoneBlueTooth;
import com.mwcard.ReaderAndroidUsb;
import com.mwcard.RootCmd;
import com.mwcard.Reader;
import com.mwreader.bluetooth.ClsUtils;
import com.mwreader.bluetooth.SearchActivity;

import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony.Sms.Conversations;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity {

	private static final String TAG = null;
	private View view1, view2, view3;
	private ViewPager viewPager;
	private PagerTitleStrip pagerTitleStrip;
	private PagerTabStrip pagerTabStrip;
	private List<View> viewList;
	private List<String> titleList;
	private ArrayAdapter<String> adapter;
	// private Button weibo_button;
	private Button btn_operate;// 进入卡片操作
	private Button btn_openport;// open port
	private Button btn_closeport;// close port
	// private Button btn_readeprom;// read eeprom
	// private Button btn_writeeprom;// write eeprom
	// private Button btn_cardstate;// read cardstate
	private Button btn_beep; // beep
	// 接触CPU卡
	private Button btn_icpoweron;
	private Button btn_iccommand;
	private Button btn_icpoweroff;

	private RadioGroup radiogroup;
	private RadioButton radio1, radio2, radio3;
	private Intent intent;
	private int dev = 0;
	private String[] portstr;
	private int openflag = 0;
	public static Reader myReader;// =new ReaderAndroidUsb();
	public static ReaderAndroidUsb readerAndroidUsb; // 安卓usb打开方式跟串口方式不一样
	private UsbDeviceConnection connection = null;

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context paramAnonymousContext,
				Intent paramAnonymousIntent) {
			if ("com.android.example.USB_PERMISSION"
					.equals(paramAnonymousIntent.getAction()))
				try {
					UsbDevice i = ((UsbDevice) paramAnonymousIntent
							.getParcelableExtra("device"));
					if (paramAnonymousIntent.getBooleanExtra("permission",
							false))
						;
					return;
				} finally {
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
			Log.i(TAG,
					device.getDeviceName() + " "
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // 文本输入框默认不获得焦点
		initView();

		// initUsbDevice();

		SerialPortFinder mSerialPortFinder = new SerialPortFinder();
		// portstr=mSerialPortFinder.getAllDevicesPath();

		// InitItem();
	}

	private void initView() {
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagertab);
		pagerTabStrip.setTabIndicatorColor(getResources()
				.getColor(R.color.gold));
		pagerTabStrip.setDrawFullUnderline(false);
		pagerTabStrip
				.setBackgroundColor(getResources().getColor(R.color.azure));
		pagerTabStrip.setTextSpacing(50);
		view1 = findViewById(R.layout.layout1);
		view2 = findViewById(R.layout.layout2);
		view3 = findViewById(R.layout.layout3);
		LayoutInflater lf = getLayoutInflater().from(this);
		view1 = lf.inflate(R.layout.layout1, null);
		// view2 = lf.inflate(R.layout.layout2, null);
		// view3 = lf.inflate(R.layout.layout3, null);

		viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
		viewList.add(view1);
		// viewList.add(view2);
		// viewList.add(view3);

		titleList = new ArrayList<String>();// 每个页面的Title数据
		titleList.add("R6");
		// titleList.add("X5");
		// titleList.add("OTHERS");

		PagerAdapter pagerAdapter = new PagerAdapter() {

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {

				return arg0 == arg1;
			}

			@Override
			public int getCount() {

				return viewList.size();
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				container.removeView(viewList.get(position));

			}

			@Override
			public int getItemPosition(Object object) {

				return super.getItemPosition(object);
			}

			@Override
			public CharSequence getPageTitle(int position) {

				return titleList.get(position);
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(viewList.get(position));

				btn_operate = (Button) findViewById(R.id.btncardop);
				btn_operate.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						intent = new Intent(MainActivity.this, Card.class);
						startActivity(intent);
					}
				});
				btn_operate.setEnabled(false);

				radio1 = (RadioButton) findViewById(R.id.radioButton1);
				radio2 = (RadioButton) findViewById(R.id.radioButton2);
				radio3 = (RadioButton) findViewById(R.id.radioButton3);
				// radio2.setEnabled(false);

				// 创建按钮对象
				btn_openport = (Button) findViewById(R.id.btnopenport);
				btn_closeport = (Button) findViewById(R.id.btncloseport);
				// btn_cardstate = (Button)findViewById(R.id.btncardstate);
				btn_beep = (Button) findViewById(R.id.btnbeep);
				// btn_readeprom = (Button)findViewById(R.id.btnreadeeprom);
				// btn_writeeprom= (Button)findViewById(R.id.btnwriteeeprom);
				// CPU 卡
				// btn_icpoweron = (Button)findViewById(R.id.btncpureset);
				// 设置监听
				btn_openport.setOnClickListener(new ButtonClickListener());
				btn_closeport.setOnClickListener(new ButtonClickListener());
				// btn_cardstate.setOnClickListener(new ButtonClickListener());
				btn_beep.setOnClickListener(new ButtonClickListener());
				// btn_readeprom.setOnClickListener(new ButtonClickListener());
				// btn_writeeprom.setOnClickListener(new ButtonClickListener());
				// btn_icpoweron.setOnClickListener(new ButtonClickListener());
				return viewList.get(position);
			}

		};
		viewPager.setAdapter(pagerAdapter);
	}

	/* 以下是设备操作的触发 */
	class ButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {

			case R.id.btnopenport:
				try {
					try {
						ClsUtils.removeBond(SearchActivity.remoteDevice);
						// myReader.mBTHSocket.close();
						myReader.closeReader();
					} catch (Exception ex) {
					}

					if (radio1.isChecked()) // 蓝牙
					{

						Intent intent = new Intent(MainActivity.this,
								SearchActivity.class);
						startActivityForResult(intent, 1);
						// myReader=new MedzoneBlueTooth();
						// st=myReader.openReader();

					} 
					else if (radio2.isChecked()) {	// U口
						if (initUsbDevice() != 0) {
							ETversion.setText("打开设备失败");
							return;
						}
						// myReader=new Reader();
						// myReader.openReader("USB1","");

						ETversion.setText(myReader.getHardwareVer());
						ETsnr.setText(myReader.getSerialNumber());
						Tip.setText("设备连接成功.");
						btn_operate.setEnabled(true);
					}
						/*
						 * if (radio2.isChecked()) //串口 { myReader=new Reader();
						 * String str=""; openflag=0; for (int
						 * i=0;i<portstr.length;i++) { try {
						 * str=str+portstr[i]+",";
						 * st=myReader.openReader(portstr[i],"115200"); } catch
						 * (Exception ex) { st=-1; }
						 * 
						 * if (st>=0) { openflag=1; break; } }
						 * 
						 * if (openflag==0) { Tip.setText("设备连接失败."+str);
						 * return; } }
						 */
						// myReader.openReader("/dev/s3c2410_serial3",
						// "115200",0);
					else{
						myReader = new ReaderAndroidCom();
						int st = myReader.openReader("/dev/ttyS1", "9600");
						if (st >= 0)
						{
							st = myReader.beep(2, 2, 2);
							Tip.setText("设备连接成功.");
							btn_operate.setEnabled(true);
						}
						else
						{
							ETversion.setText("打开设备失败");
						}
					}
				} catch (Exception ex) {
					// btn_operate.setEnabled(false);
					Tip.setText(ex.getMessage());
				}

				break;
			case R.id.btncloseport:
				try {
					ClsUtils.removeBond(SearchActivity.remoteDevice);
					// myReader.mBTHSocket.close();
					myReader.closeReader();
					ETversion.setText("");
					Tip.setText("设备关闭成功.");
				} catch (Exception ex) {
					Tip.setText(ex.getMessage());
				}
				break;
			case R.id.btnbeep:
				try {
					String data = "00112233445566778899001122334455667788990011223344556677889900112233445566778899001122334455667788990011223344556677889900112233445566778899001122334455667788990011223344556677889900112233445566778899001122334455667788990011223344556677889900112233445566778899";
//					data =myReader.readConfig(0, 70);
					
					myReader.writeConfig(0, data);
//					myReader.beep(2, 20, 20);
					ETsnr.setText("");
					Thread.sleep(2000);
//					ETsnr.setText(myReader.getSerialNumber());
				} catch (Exception ex) {
					Tip.setText(ex.getMessage());
				}
				break;
			}
		}

		private final EditText ETsnr = (EditText) findViewById(R.id.ETsnr);// 写EPROM数据
		// private final EditText
		// ETadrress=(EditText)findViewById(R.id.ETadrress);//EPROM 地址
		// private final EditText
		// ETlength=(EditText)findViewById(R.id.ETlength);//EPROM 地址
		private final EditText Tip = (EditText) findViewById(R.id.layout1_Tip); // 提示信息框
		private final EditText ETversion = (EditText) findViewById(R.id.ETversion);// 版本信息框
		// private final EditText
		// ETreaddata=(EditText)findViewById(R.id.ETreaddata);//EEROM信息框

		private String str_in;
		public int st = 0;
		public String lenstr;
		public String version;
	}

	/**
	 * 把字符串去空格后转换成byte数组。如"37 5a"转成[0x37][0x5A]
	 * 
	 * @param s
	 * @return
	 */
	public static int stringToInt(String intstr) {
		if (intstr.isEmpty()) {
			return 0;
		}
		Integer integer;
		integer = Integer.valueOf(intstr);
		return integer.intValue();
	}

	// 蓝牙调用连接成功后，回调到这个接口
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { // resultCode为回传的标记，我在B中回传的是RESULT_OK
		case RESULT_OK:
			Bundle b = data.getExtras(); // data为B中回传的Intent
			String str = b.getString("connected");// str即为回传的值
			if (str.equals("connectedok")) {
				EditText ETsnr = (EditText) findViewById(R.id.ETsnr);// 写EPROM数据
				EditText Tip = (EditText) findViewById(R.id.layout1_Tip); // 提示信息框
				final EditText ETversion = (EditText) findViewById(R.id.ETversion);// 版本信息框

				try {
					ETversion.setText(myReader.getHardwareVer());
					// ETsnr.setText(myReader.getSerialNumber());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Tip.setText(e.getMessage());
					return;
				}
				btn_operate.setEnabled(true);
				Tip.setText("设备连接成功.");
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	/*
	 * private void InitItem() { String[] m_arr = {"CPU卡","SAM1","SAM2","SAM3"};
	 * Spinner spinner; spinner = (Spinner) findViewById(R.id.spinner1);
	 * spinner.setPrompt("请选择颜色" ); //将可选内容与ArrayAdapter连接起来 adapter = new
	 * ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,m_arr);
	 * //设置下拉列表的风格
	 * adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item
	 * ); //将adapter 添加到spinner中 spinner.setAdapter(adapter); //添加事件Spinner事件监听
	 * spinner.setOnItemSelectedListener(new SpinnerSelectedListener()); //设置默认值
	 * spinner.setVisibility(View.VISIBLE); } class SpinnerSelectedListener
	 * implements OnItemSelectedListener{
	 * 
	 * public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long
	 * arg3) { //view.setText("你的血型是："+m[arg2]);
	 * Toast.makeText(MainActivity.this, "yes", Toast.LENGTH_SHORT).show(); }
	 * 
	 * public void onNothingSelected(AdapterView<?> arg0) { } }
	 */

}
