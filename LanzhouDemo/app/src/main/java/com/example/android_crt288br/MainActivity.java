package com.example.android_crt288br;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import Crt288brDrv.Crt288br;

import com.xianfeng.InnoverCard;
import com.xianfeng.ReadData;
import com.xianfeng.WriteData;


public class MainActivity extends Activity {

	Crt288br mCrt288br = new Crt288br();
	Button btnOpen, btnOpenUsb, btnClose, btnInit,btnGetCardStatus;
	Button btnCardProcess, btnSimProcess;	
	EditText editsPort, editBaudRate;
	//先锋
	Button readBtn,writeBtn;
	EditText editWriten;
	//数据
	String dataString_;
	String companyStr_;


	String g_log = "";
	protected BroadcastReceiver mUsbReceiver;
	private final static String TAG = "CRT288bru";
	
	private static Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getApplicationContext();
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();	
		}
	}
	
	protected void onStart(){
		super.onStart();
		
		btnOpen  = (Button)this.findViewById(R.id.buttonOpenDev);
		btnOpenUsb = (Button)this.findViewById(R.id.buttonOpenUsbDev);
		btnClose  = (Button)this.findViewById(R.id.buttonCloseDev);
		btnInit  = (Button)this.findViewById(R.id.buttonInitDev);
		btnGetCardStatus  = (Button)this.findViewById(R.id.buttonGetCardStatus);
		btnCardProcess  = (Button)this.findViewById(R.id.button1);
		btnSimProcess  = (Button)this.findViewById(R.id.button2);

		readBtn = (Button)this.findViewById(R.id.buttonread);
		writeBtn = (Button)this.findViewById(R.id.buttonwrite);
		editWriten = (EditText)this.findViewById(R.id.editText3);

		readBtn.setOnClickListener(xfOnClickListener);
		writeBtn.setOnClickListener(xfOnClickListener);
		
		btnOpen.setOnClickListener(myOnClickListener); 
		btnOpenUsb.setOnClickListener(myOnClickListener); 
		btnClose.setOnClickListener(myOnClickListener); 
		btnInit.setOnClickListener(myOnClickListener); 
		btnGetCardStatus.setOnClickListener(myOnClickListener); 
		btnCardProcess.setOnClickListener(myOnClickListener); 
		btnSimProcess.setOnClickListener(myOnClickListener); 
		
		editBaudRate = (EditText)this.findViewById(R.id.editText2);
		editsPort = (EditText)this.findViewById(R.id.editText1);

		dataString_ = getResources().getString(R.string.carddata);
		companyStr_ = getResources().getString(R.string.company);
				
	}

	private  OnClickListener xfOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			InnoverCard innoverCard = new InnoverCard(dataString_);

			if (view == readBtn){
				if (innoverCard.myCard(companyStr_)){

					ReadData readData = innoverCard.readCard();

					String userno = readData.getUserno(); //用户号
					String corpo = readData.getCorpno(); //公司号
					String meterno = readData.getMeterno(); //表具编号
					Integer buycount = readData.getBuycount(); //购气次数
					Integer cardgases = readData.getCardgases(); //用户气量
					String buygasdate = readData.getBuygasdate(); //购气时间
					Integer errorcode = readData.getErrorcode();//错误代码

					System.out.println(userno + "--" + corpo + "--" + meterno + "--"
							+ String.valueOf(buycount) + "--" + String.valueOf(cardgases)
							+ "--" + buygasdate + "--" + String.valueOf(errorcode));
				}
			}else if(view == writeBtn){


				Calendar c = Calendar.getInstance();
				//参数分别为，购气时间，购气次数，购气量
				WriteData writeData = new WriteData(c.getTime(),5,123);
				writeData.setDate(c.getTime());
				writeData.setBuycount(1);
				writeData.setGases(122);

				//处理数据，结果为处理后的数据，可直接写入卡片
				String res = innoverCard.writeCard(writeData);
				System.out.println(res);

				InnoverCard card = new InnoverCard(res);
				ReadData readData = card.readCard();
				System.out.print(readData.getUserno());


			}
		}
	};
	
	private OnClickListener myOnClickListener = new OnClickListener() {
		String ddd = "";

		 public void onClick(View v) {
			 String strlog = "";
			 int iRet = 0;
			 
			 if(v == btnOpen)
	    	 {
	    	        char[] portinfo = new char[56]; 
	    	        
	    	        String strData = editsPort.getText().toString();
	    	        String strData2 = editBaudRate.getText().toString();
	    	        int  iBaudRate = Integer.valueOf(strData2).intValue();
	    	       
	    	        portinfo = strData.toCharArray();
	    	        iRet = mCrt288br.OpenDevice(portinfo, iBaudRate);
	    	        if(iRet != 0){
	    				strlog = "打开串口失败 ！";		
	    			}
	    			else{
	    				strlog = "打开串口成功 ！";			
	    			}
	    			
	    			
	    	}
			 if(v == btnOpenUsb)
	    	 { 
				 
				// get FileDescriptor by Android USB Host API
				 UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
				 final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
				 HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
				 Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
				  
				 PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, 
				                                                              new Intent(ACTION_USB_PERMISSION), 0);
				 IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
				mContext.registerReceiver(mUsbReceiver, filter);
				  
				 int fd = -1;
				 while(deviceIterator.hasNext()){
				     UsbDevice device = deviceIterator.next();
				     Log.i(TAG, device.getDeviceName() + " " + Integer.toHexString(device.getVendorId()) + 
				                " " + Integer.toHexString(device.getProductId()));
				  
				     manager.requestPermission(device, mPermissionIntent);
				     UsbDeviceConnection connection = manager.openDevice(device);
				     if(connection != null){
				        fd = connection.getFileDescriptor();
				        iRet = mCrt288br.OpenUsbDevice(fd);
		    	        if(iRet == 0){
		    	        	strlog = "打开USB成功 ！";	
		    	        	break;
		    			}
		    	        else{
		    	        	strlog = "打开USB失败 ！";	
		    	        }
				     } else
				        Log.e(TAG, "UsbManager openDevice failed");
				     break;
				 }
				 
	    	
	    			
	    	}
			else if(v == btnClose)
			{
				iRet = mCrt288br.CloseDevice();
   	        if(iRet != 0){
   				strlog = "关闭串口失败 ！";		
   			}
   			else{
   				strlog = "关闭串口成功 ！";			
   			}
			}
			else if(v == btnInit)
			{
				iRet = mCrt288br.InitDev();
				if(iRet != 0){
					strlog = "设备初始化失败 ！";		
				}
				else{
					strlog = "设备初始化成功 ！";			
				}
		   }
		   else if(v == btnGetCardStatus)
		   {
			   // 返回卡片状态: 1 无卡， 3 卡在读卡器内， 9 设备不在线，卡状态未知。
				iRet = mCrt288br.GetCardStatus();
				switch(iRet){
				case 1:
					strlog = "无卡";
					break;
				case 3:
					strlog = "卡在读卡器内";
					break;
				case 9:
					strlog = "设备不在线，卡状态未知";
					break;
				default:
					strlog = "状态未知";
					break;			
				}
		   }
		   else if(v == btnCardProcess) 
		   {
			    String strTestString = "";
				iRet = mCrt288br.GetICType();
				switch(iRet){
				case 0:
					strlog = " S50卡";
					break;
				case 1:
					strlog = "S70卡";
					break;
				case 2:
					strlog = "UL卡";
					break;
				case 4:
					strlog = "TYPEA CPU卡";
					strlog += "\r\n";
					strTestString = TestNonContactCard(0); 
					strlog += strTestString;
					break;
				case 5:
					strlog = "TYPEB CPU卡";	
					strlog += "\r\n";
					strTestString = TestNonContactCard(1);
					strlog += strTestString;
					break;
				case 9:
					strlog = "非接触式射频卡未知类型";
					break;
				case 10:
					strlog = "T=0 接触式CPU卡";
					strlog += "\r\n";
					strTestString = TestContactCard(1);
					strlog += strTestString;
					break;
				case 11:
					strlog = "T=1 接触式CPU卡";
					strlog += "\r\n";
					strTestString = TestContactCard(2);
					strlog += strTestString;
					break;
				case 20:
					strlog = "24C01卡";
					strlog += "\r\n";
					strTestString = Test24ICxCard(iRet);
					strlog += strTestString;
					break;
				case 21:
					strlog = "24C02卡";
					strlog += "\r\n";
					strTestString = Test24ICxCard(iRet);
					strlog += strTestString;
					break;
				case 22:
					strlog = "24C04卡";
					strlog += "\r\n";
					strTestString = Test24ICxCard(iRet);
					strlog += strTestString;
					break;
				case 23:
					strlog = "24C08卡";
					strlog += "\r\n";
					strTestString = Test24ICxCard(iRet);
					strlog += strTestString;
					break;
				case 24:
					strlog = "24C16卡";
					strlog += "\r\n";
					strTestString = Test24ICxCard(iRet);
					strlog += strTestString;
					break;
				case 25:
					strlog = "24C32卡";
					strlog += "\r\n";
					strTestString = Test24ICxCard(iRet);
					strlog += strTestString;
					break;
				case 26:
					strlog = "24C64卡";
					strlog += "\r\n";
					strTestString = Test24ICxCard(iRet);
					strlog += strTestString;
					break;
				case 30:
					strlog = "SL4442卡";
					strlog += "\r\n";
					strTestString = TestSLE4442();
					strlog += strTestString;
					break;
				case 31:
					strlog = "SL4428卡";
					strlog += "\r\n";
					strTestString = TestSLE4428();
					strlog += strTestString;
					break;
				case 98:
					strlog = "无卡";
					break;
				case 99:
					strlog = "卡类型未知";
					break;
				default:
					strlog = "获取卡类型失败";
					break;
					
				}
				//iRet = mCrt288br.CardPowerOperation(0); //下电
					
		   }
		   else if(v == btnSimProcess)
		   {	   
			   strlog = TestSimCard();
		   }
	
		Toast.makeText(MainActivity.this, strlog, 1).show();
			 
		 }
	 };
	
	 private String TestContactCard(int iProtocol)
	 {
		 String strlog = "";
		 int iRet = 0;
		 
		 int  iMode = 1; //复位模式。 0 EMV协议冷复位， 1 ISO7816协议冷复位， 2 热复位。
		 int[] iOutAtrLen = new int[2];
		 byte[] byOutAtr = new byte[128];
		 iRet = mCrt288br.ContactCPUReset(iMode, iOutAtrLen, byOutAtr);
         if(iRet != 0){
		    strlog = "接触式CPU卡上电失败！";		
	     }
	     else{
		    String strOutUid = "";
	    for(int i=0;i<iOutAtrLen[0];i++)
	    {
		    strOutUid += String.format("%02x ", byOutAtr[i]);
	    }
		strlog = "接触式CPU卡上电成功 ！Atr: ";
		strlog += strOutUid;
		strlog += "\r\n";
		
		//int iProtocol = 1;   //协议模式。 1 T=0， 2 T=1。
		String strApdu = "0084000004";
		int[] iRecvLen = new int[2];
		byte[] byRecv = new byte[512];
		iRet = mCrt288br.ContactCPUSendAPDU(iProtocol, strApdu.length(), strApdu.getBytes(), iRecvLen, byRecv);
		if(iRet != 0)
		{
			strlog += "接触式CPU卡发送APDU失败！";
		}
		else
		{				
			String strRecv = "";
		    for(int i=0;i<iRecvLen[0];i++)
		    {
			    strRecv += String.format("%02x ", byRecv[i]);
		    }
		    strlog += "接触式CPU卡发送APDU成功 ！Recv: ";
		    strlog += strRecv;
		}
		
	   }
         
         return strlog;
	 }
	 
	 
	 private String TestNonContactCard(int iType)
	 {
		 String strlog = "";
		 int iRet = 0;
		 
		 //int  iType = 0; //电源操作模式。 0 TYPEA 上电， 1 TYPEB 上电， 2下电。
		 int[] iOutAtrLen = new int[2];
		 byte[] byOutAtr = new byte[128];
		 iRet = mCrt288br.NonContactCPUPower(iType, iOutAtrLen, byOutAtr);
         if(iRet != 0){
		    strlog = "非接触式CPU卡上电失败！";		
	     }
	     else{
		    String strOutUid = "";
	    for(int i=0;i<iOutAtrLen[0];i++)
	    {
		    strOutUid += String.format("%02x ", byOutAtr[i]);
	    }
		strlog = "非接触式CPU卡上电成功 ！Atr: ";
		strlog += strOutUid;
		strlog += "\r\n";
		
		String strApdu = "0084000008";
		int[] iRecvLen = new int[2];
		byte[] byRecv = new byte[512];
		iRet = mCrt288br.NonContactCPUSendAPDU(strApdu.length(), strApdu.getBytes(), iRecvLen, byRecv);
		if(iRet != 0)
		{
			strlog += "非接触式CPU卡发送APDU失败！";
		}
		else
		{				
			String strRecv = "";
		    for(int i=0;i<iRecvLen[0];i++)
		    {
			    strRecv += String.format("%02x ", byRecv[i]);
		    }
		    strlog += "非接触式CPU卡发送APDU成功 ！Recv: ";
		    strlog += strRecv;
		}
		
	   }
         mCrt288br.NonContactCPUPower(2, iOutAtrLen, byOutAtr); //下电
        
        return strlog;
	 }
	 
	 private String Test24ICxCard(int i24CxxType)
	 {
		 String strlog = "";
		 int iRet = 0;
		 
		 int iMode = 2;  //操作模式   1 读， 2 不带校验写， 3 带校验写
		 //int i24CxxType=20; //24Cxx卡类型。 20 24C01卡， 21 24C02卡，22 24C04卡， 23 24C08卡， 24 24C16卡， 25 24C32卡， 26 24C64卡
		 int wStartAddr = 0x05; //起始位置(24C01: 0x00-0x07F, 24C02: 0x00-0xFF, 24C04: 0x00-0x01FF, 24C08: 0x00-0x03FF, 24C16: 0x00-0x07FF,
		 int[] uDataLength = new int[2];
		 String strWriteDataString = "1234567890";
		 uDataLength[0] = strWriteDataString.length();
		 iRet = mCrt288br.I24CxxProcess(iMode, i24CxxType, wStartAddr, uDataLength, strWriteDataString.getBytes());
         if(iRet != 0){
		     strlog = "24ICx卡不带校验写失败！";		
	     }
	     else{
	    	 strlog = "24ICx卡不带校验写成功 ！";
		     strlog += "\r\n";
	
		    iMode = 1;
		    byte[] byReadData = new byte[512];
		    iRet = mCrt288br.I24CxxProcess(iMode, i24CxxType, wStartAddr, uDataLength, byReadData);
		    if(iRet != 0)
		    {
		    	strlog += "24ICx卡读数据失败！";
		    }
		    else
		    {				
		    	String strRecv = "";
		    	for(int i=0;i<uDataLength[0];i++)
		    	{
		    		strRecv += String.format("%02x ", byReadData[i]);
		    	}
		    	strlog += "24ICx卡读数据成功 ！Data: ";
		    	strlog += strRecv;
		    }
	   }
         
         return strlog;
	 }

	 private String TestSimCard()
	 {
		 String strlog = "";
		 int iRet = 0;
		 
		 int iMode = 0; //iMode--复位模式。 0 EMV协议冷复位， 1 ISO7816协议冷复位。
		 int iPm = 2; //复位工作电压。 1 3V工作电压， 2 5V工作电压。
		 int iSimNums = 1; //SIM卡座号。 1 SIM1卡座， 2 SIM2卡座， 3 SIM3卡座。
		 int[] iOutAtrLen = new int[2];
		 byte[] byOutAtr = new byte[128];
		 iRet = mCrt288br.SIMReset(iMode, iPm, iSimNums, iOutAtrLen, byOutAtr);
         if(iRet != 0){
		    strlog = "SIM卡复位失败！";		
	     }
	     else{
		    String strOutUid = "";
	    for(int i=0;i<iOutAtrLen[0];i++)
	    {
		    strOutUid += String.format("%02x ", byOutAtr[i]);
	    }
		strlog = "SIM卡复位成功 ！Atr: ";
		strlog += strOutUid;
		strlog += "\r\n";
		
		String strApdu = "0084000004";
		int[] iRecvLen = new int[2];
		byte[] byRecv = new byte[512];
		iRet = mCrt288br.SIMSendAPDU(1, iSimNums, strApdu.length(), strApdu.getBytes(), iRecvLen, byRecv);
		if(iRet != 0)
		{
			strlog += "SIM卡发送APDU失败！";
		}
		else
		{				
			String strRecv = "";
		    for(int i=0;i<iRecvLen[0];i++)
		    {
			    strRecv += String.format("%02x ", byRecv[i]);
		    }
		    strlog += "SIM卡发送APDU成功 ！Recv: ";
		    strlog += strRecv;
		}
		
	   }
         
         return strlog;
	 }
	 
	 private String TestSLE4442()
	 {
		 String strlog = "";
		 int iRet = 0;
		 
		 int[] iOutAtrLen = new int[2];
		 byte[] byOutAtr = new byte[128];
		 iRet = mCrt288br.SLE4442Power(iOutAtrLen, byOutAtr);
         if(iRet != 0){
		    strlog = "SLE4442卡上电失败！";		
	     }
	     else{
		    String strOutUid = "";
		    for(int i=0;i<iOutAtrLen[0];i++)
		    {
		    	strOutUid += String.format("%02x ", byOutAtr[i]);
		    }
		    strlog = "SLE4442上电成功 ！Atr: ";
		    strlog += strOutUid;
		    strlog += "\r\n";
		
		    int iMode = 1;   //操作模式  1 校验密码， 2 修改密码
		    String strKey = "FFFFFF";

		    iRet = mCrt288br.SLE4442CheckPasswd(iMode, strKey.length(), strKey.getBytes());
		    if(iRet != 0)
		    {
		    	strlog += "SLE4442卡校验密码失败！";
		    }
			else
			{				
			    strlog += "SLE4442卡校验密码成功 ！";
			    strlog += "\r\n";
			    
			    int iMode1 = 2;  //操作模式 1 读数据， 2 写数据
			    int iRegion = 1; //操作区域 1 主存储区， 2 保护位， 3 安全区(写数据不支持)
			    int wStartAddr = 0x05; //起始位置 0x00-0xFF
				int[] uDataLength = new int[2];
				String strWriteData = "1234567890";
				uDataLength[0] = strWriteData.length();
			    iRet = mCrt288br.SLE4442Process(iMode1, iRegion, wStartAddr, uDataLength, strWriteData.getBytes());
			    if (iRet != 0) {
			    	strlog += "SLE4442卡写主存储区数据失败！";
				}
			    else {
			    	strlog += "SLE4442卡写主存储区数据成功！";
				}
			    
			    strlog += "\r\n";
			    
			    iMode1 = 1;  //操作模式 1 读数据， 2 写数据
			    byte[] byReadData = new byte[512];
			    iRet = mCrt288br.SLE4442Process(iMode1, iRegion, wStartAddr, uDataLength, byReadData);
			    if (iRet != 0) {
			    	strlog += "SLE4442卡读主存储区数据失败！";
				}
			    else {
			    	String strRecv = "";
			    	for(int i=0;i<uDataLength[0];i++)
			    	{
			    		strRecv += String.format("%02x ", byReadData[i]);
			    	}
			    	strlog += "SLE4442卡读主存储区数据成功 ！Data: ";
			    	strlog += strRecv;
				}
			}
		
	   }
         
         return strlog;
	 }
	 
	 private String TestSLE4428()
	 {
		 
		 String strlog = "";
		 int iRet = 0;
		
		 int[] iOutAtrLen = new int[2];
		 byte[] byOutAtr = new byte[128];
		 
		 iRet = mCrt288br.SLE4428Power(iOutAtrLen, byOutAtr);
         if(iRet != 0){
		    strlog = "SLE4428卡上电失败！";		
	     }
	     else{
		    String strOutUid = "";
		    for(int i=0;i<iOutAtrLen[0];i++)
		    {
		    	strOutUid += String.format("%02x ", byOutAtr[i]);
		    }
		    strlog = "SLE4428上电成功 ！Atr: ";
		    strlog += strOutUid;
		    strlog += "\r\n";
		

		    String strKey = "FFFF";
		    iRet = mCrt288br.SLE4428CheckPasswd(strKey.length(), strKey.getBytes());
		    if(iRet != 0)
		    {
		    	strlog += "SLE4428卡校验密码失败！";
		    }
			else
			{				
			    strlog += "SLE4428卡校验密码成功 ！";
			    strlog += "\r\n";
			    
			    int iMode1 = 2;  //操作模式 1 读数据， 2 写数据
			    int iRegion = 1; //操作区域 1 主存储区， 2 保护位，
			    int wStartAddr = 0x26; //起始位置0x0000-0x03FF
				int[] uDataLength = new int[2];
				String strWriteData = "1234567890";
				uDataLength[0] = strWriteData.length();
			    iRet = mCrt288br.SLE4428Process(iMode1, iRegion, wStartAddr, uDataLength, strWriteData.getBytes());
			    if (iRet != 0) {
			    	strlog += "SLE4428卡写主存储区数据失败！";
				}
			    else {
			    	strlog += "SLE4428卡写主存储区数据成功！";
				}
			    
			    strlog += "\r\n";
			    
			    iMode1 = 1;  //操作模式 1 读数据， 2 写数据
			    byte[] byReadData = new byte[512];
			    iRet = mCrt288br.SLE4428Process(iMode1, iRegion, wStartAddr, uDataLength, byReadData);
			    if (iRet != 0) {
			    	strlog += "SLE4428卡读主存储区数据失败！";
				}
			    else {
			    	String strRecv = "";
			    	for(int i=0;i<uDataLength[0];i++)
			    	{
			    		strRecv += String.format("%02x ", byReadData[i]);
			    	}
			    	strlog += "SLE4428卡读主存储区数据成功 ！Data: ";
			    	strlog += strRecv;
				}
			}
		
	   }
         
         return strlog;
	 }

	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
}
