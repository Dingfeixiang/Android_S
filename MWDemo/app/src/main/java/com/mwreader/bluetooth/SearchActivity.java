package com.mwreader.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import com.example.mwreader.MainActivity;
//import com.mwcard.MedzoneBlueTooth;
import com.mwcard.Reader;
import com.mwcard.ReaderAndroidBlueTooth;

import android.R;
import android.R.array;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 娣诲姞鍜屼慨鏀筸ember
 * 
 * @author kevin
 * 
 */
public class SearchActivity extends BaseActivity implements
		OnItemClickListener {
	private ListView list = null;
	private BAdapter bleAdapter = null;
	private ArrayList<BluetoothDevice> arraySource = null;
	private BluetoothAdapter adapter;
	
	protected UUID btDeviceUUID = UUID
				.fromString("00001101-0000-1000-8000-00805F9B34FB");
	protected BluetoothSocket mBTHSocket = null;
	protected BluetoothServerSocket mBThServer = null;
	protected InputStream mmInStream = null;
	protected OutputStream mmOutStream = null;
    public static BluetoothDevice remoteDevice=null;
    
	private boolean connectFlag=false;
	private int connetTime =0;
	
    // 创建一个接收ACTION_FOUND广播的BroadcastReceiver 
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { 
        public void onReceive(Context context, Intent intent) { 
            String action = intent.getAction(); 
            
            // 发现设备 
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { 
                // 从Intent中获取设备对象 
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); 
                // 将设备名称和地址放入array adapter，以便在ListView中显示 
                arraySource.add(device);
                bleAdapter.notifyDataSetChanged();
                //showMessage(device.getName() + "\n" + device.getAddress());
                
            }else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){    
            	arraySource.clear();
            	initNavigation("正在扫描蓝牙设备...");
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){ 
            	initNavigation("蓝牙设备扫描完成。");
            }else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){ 
            	if (connectFlag)
            	{
	            	initNavigation("蓝牙设备连接成功。");
	        		Toast toast=Toast.makeText(getApplicationContext(), "蓝牙设备连接成功。", Toast.LENGTH_SHORT); 
	        		toast.show();
	        		
	                Intent intent1=new Intent();  
	                intent1.putExtra("connected", "connectedok");  
	                SearchActivity.this.setResult(RESULT_OK, intent1);  
	                finish(); 
            	}
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {  
                // 状态改变的广播  
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);    
                 int   connectState = device.getBondState();  
                    switch (connectState) {  
                        case BluetoothDevice.BOND_NONE:  
                            break;  
                        case BluetoothDevice.BOND_BONDING:  
                        	initNavigation("正在配对..。");
                            break;  
                        case BluetoothDevice.BOND_BONDED:  
                        	int i=0;
                            //try {  
                                // 连接  
                              // connect(device);  
                           // } catch (IOException e) {  
                          //      e.printStackTrace();  
                          //  }  
                            break;  
                    }  
            } else if (intent.getAction().equals(
                		"android.bluetooth.device.action.PAIRING_REQUEST"))
            {
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                try
                {
	                ClsUtils.setPin(btDevice, "000000"); // 手机和蓝牙采集器配对
	                ClsUtils.createBond(btDevice);
	                ClsUtils.cancelPairingUserInput(btDevice); //某些手机在此处会抛出异常，若果有需要处理的可在catch中处理
                }
                catch (Exception e)
                {
	                // TODO Auto-generated catch block
                	e.printStackTrace();
                }
            } 
        }
    };
    
	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(Res.layout.activity_search);
		this.initView();
		
	  	adapter = BluetoothAdapter.getDefaultAdapter();
	  	if (!adapter.isEnabled())
	  		adapter.enable();
        // 注册BroadcastReceiver 
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        //String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST"; 
        //filter.addAction(ACTION_PAIRING_REQUEST);

        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED); 
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); 
        this.registerReceiver(mReceiver, filter); // 不要忘了之后解除绑定
        
        // If we're already discovering, stop it
        if (adapter.isDiscovering()) {
        	adapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        adapter.startDiscovery();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//app.manager.startScanBluetoothDevice();
		//app.manager.isEdnabled(this);

		dialog = new ProgressDialog(this);
		dialog.setMessage("aaa");
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//arraySource.clear();
		//bleAdapter.notifyDataSetChanged();
		//app.manager.stopScanBluetoothDevice();
	}

	@SuppressWarnings("static-access")
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(Res.anim.activity_in_left,
				Res.anim.activity_out_bottom);
	}

	@SuppressWarnings("static-access")
	protected void initView() {
		initNavigation("蓝牙设备");
		list = (ListView) this.findViewById(Res.id.list);

		list.setOnItemClickListener(this);

		arraySource = new ArrayList<BluetoothDevice>();
		bleAdapter = new BAdapter(this, arraySource);

		list.setAdapter(bleAdapter);

	}

	@Override
	protected void initNavigation(String title) {
		// TODO Auto-generated method stub
		super.initNavigation(title);
		navigateView.setRightHideBtn(false);
		navigateView.rightBtn.setText("刷新");
		navigateView.rightBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		        // If we're already discovering, stop it
		        if (adapter.isDiscovering()) {
		        	adapter.cancelDiscovery();
		        }
		        
		        // Request discover from BluetoothAdapter
		        adapter.startDiscovery();
			}
		});
		navigateView.setEnable(false);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		super.onActivityResult(requestCode, resultCode, data);
		//app.manager.onRequestResult(requestCode, resultCode, data);
	}
	
	/**
	 * 连接选定的蓝牙设备，在连接之前，先进行判断有无配对，如果没有配对，则进行蓝牙的配对。
	 * @param selectDevice
	 * @return
	 */
	protected boolean connectDevice(BluetoothDevice device,String strPsw) {    
        try {    
            adapter.cancelDiscovery(); //取消搜索蓝牙设备
    		if (!adapter.isEnabled()) //判断蓝牙是否打开
    		{
    			adapter.enable();
    		}
    		
    		/*if (device.getBondState() != BluetoothDevice.BOND_BONDED)
    		{
    			try
    			{
    				Log.d("mylog", "NOT BOND_BONDED");
    				ClsUtils.setPin(device, strPsw); // 手机和蓝牙采集器配对
    				ClsUtils.createBond( device);
    				remoteDevice = device; // 配对完毕就把这个设备对象传给全局的remoteDevice
    			}
    			catch (Exception e)
    			{
    				// TODO Auto-generated catch block

    				Log.d("mylog", "setPiN failed!");
    				e.printStackTrace();
    				
    				connetTime++;
    			} //

    		}
    		else
    		{
    			Log.d("mylog", "HAS BOND_BONDED");
    			try
    			{
    				ClsUtils.setPin(device, strPsw); // 手机和蓝牙采集器配对
    				ClsUtils.createBond(device);
    				remoteDevice = device; // 如果绑定成功，就直接把这个设备对象传给全局的remoteDevice
    			}
    			catch (Exception e)
    			{
    				// TODO Auto-generated catch block
    				Log.d("mylog", "setPiN failed!");
    				e.printStackTrace();
    				
    				connetTime++;
    			}
    		}*/
            
    		// 连接建立之前的先配对    
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {    
                Method creMethod = BluetoothDevice.class    
                        .getMethod("createBond");    
                Log.e("TAG", "开始配对");    
                creMethod.invoke(device);    
            } else {    
            }    
        } catch (Exception e) {    
            // TODO: handle exception    
            //DisplayMessage("无法配对！"); 
            e.printStackTrace();   
    		Toast toast=Toast.makeText(getApplicationContext(),"配对出错，错误为:"+ e.getMessage(), Toast.LENGTH_LONG); 
    		toast.show();
    		
    		connetTime++;
    		return false;
        }    
   
        try {    
        	mBTHSocket.connect();    
            //DisplayMessage("连接成功!");   
            connectFlag = true;  
            
            return true;
        } catch (IOException e) {     
            //DisplayMessage("连接失败！");  
            connetTime++;  
            connectFlag = false;  
        } finally {  
        }    
        
        return false;
    }  

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
		BluetoothDevice selectDevice=arraySource.get(arg2);
		
        try{
        
        	//initNavigation("正在连接所选择的蓝牙设备...");

        	adapter.cancelDiscovery();
        	
        	int sdk = Integer.parseInt(Build.VERSION.SDK);
        	if (sdk >= 10) {
        		mBTHSocket = selectDevice
        					.createInsecureRfcommSocketToServiceRecord(btDeviceUUID);
        	} else {
        		mBTHSocket = selectDevice
        				.createRfcommSocketToServiceRecord(btDeviceUUID);
        	}
        	
			//mBThServer = adapter
			//		.listenUsingRfcommWithServiceRecord(
			//				"myServerSocket", btDeviceUUID);
			connectFlag=false;
			connetTime=0;
	        while (!connectFlag && connetTime <= 10) {                  
	            if (connectDevice(selectDevice,"000000")) break;
	        } 
			
	        if (connectFlag)
	        {
	        	ReaderAndroidBlueTooth mwab = new ReaderAndroidBlueTooth();
	        	mwab.setmBTHSocket(mBTHSocket);
	        	//Reader mw = new com.mwcard.ReaderAndroidBlueTooth();
				//Reader mw = new MedzoneBlueTooth();
				//mw.mBTHSocket=mBTHSocket;
				
				try {
					mwab.openReader();
					mwab.beep(2, 10, 10);
					
					MainActivity.myReader=mwab;
				} catch (Exception e) {
					// TODO Auto-generated catch block
		    		Toast toast=Toast.makeText(getApplicationContext(), "打开读写器出错，错误为:"+e.getMessage(), Toast.LENGTH_LONG); 
		    		toast.show();
		    		
					e.printStackTrace();
				}
	        }
	        else
	        {
	    		Toast toast=Toast.makeText(getApplicationContext(), "蓝牙设备连接出错，请重试!", Toast.LENGTH_LONG); 
	    		toast.show();
	        }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			connectFlag=false;
    		Toast toast=Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG); 
    		toast.show();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		dialog.dismiss();
	}

	private ProgressDialog dialog = null;

	/*@Override
	public void onReceive(Context context, Intent intent, String macData,
			String uuid) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		this.connectedOrDis(action);
		if (RFStarBLEService.ACTION_GATT_CONNECTED.equals(action)) {
			Log.d(App.TAG, "111111111 杩炴帴瀹屾垚");
			dialog.show();
		} else if (RFStarBLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
			Log.d(App.TAG, "111111111 杩炴帴鏂紑");
			dialog.hide();
		} else if (RFStarBLEService.ACTION_GATT_SERVICES_DISCOVERED
				.equals(action)) {
			dialog.hide();
			this.finish();
		}
	}*/
}
