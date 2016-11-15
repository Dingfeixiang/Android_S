package com.mwreader.bluetooth;

/************************************ ������Ժ��� * **************/
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
public class ClsUtils
{

	/**
	 * ���豸��� �ο�Դ�룺platform/packages/apps/Settings.git
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	static public boolean createBond(BluetoothDevice btDevice)
			throws Exception
	{
		Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	/**
	 * ���豸������ �ο�Դ�룺platform/packages/apps/Settings.git
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	static public boolean removeBond(BluetoothDevice btDevice)
			throws Exception
	{
		Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
		Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	static public boolean setPin(BluetoothDevice btDevice,
			String str) throws Exception
	{
		try
		{
			Method removeBondMethod = BluetoothDevice.class.getDeclaredMethod("setPin",
					new Class[]
					{byte[].class});
			Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice,
					new Object[]
					{str.getBytes()});
			Log.e("returnValue", "" + returnValue);
		}
		catch (SecurityException e)
		{
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}

	// ȡ���û�����
	static public boolean cancelPairingUserInput(BluetoothDevice device)	throws Exception
	{
		Method createBondMethod = BluetoothDevice.class.getMethod("cancelPairingUserInput");
		// cancelBondProcess()
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

	// ȡ�����
	static public boolean cancelBondProcess(BluetoothDevice device)	throws Exception
	{
		Method createBondMethod = BluetoothDevice.class.getMethod("cancelBondProcess");
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

	/**
	 * 
	 * @param clsShow
	 */
	static public void printAllInform()
	{
		try
		{
			// ȡ�����з���
			Method[] hideMethod = BluetoothDevice.class.getMethods();
			int i = 0;
			for (; i < hideMethod.length; i++)
			{
				Log.e("method name", hideMethod[i].getName() + ";and the i is:"
						+ i);
			}
			// ȡ�����г���
			Field[] allFields = BluetoothDevice.class.getFields();
			for (i = 0; i < allFields.length; i++)
			{
				Log.e("Field name", allFields[i].getName());
			}
		}
		catch (SecurityException e)
		{
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}