package com.mwreader.bluetooth;

import java.io.DataOutputStream;
import java.io.OutputStream;

public final class RootCmd {
	
	//执行linux命令并输出结果
	protected static int execRootCmdSlient(String paramString)
	{
		try{			
			Process localProcess = Runtime.getRuntime().exec("su");
			Object localObject = localProcess.getOutputStream();
			DataOutputStream localDataOutputStream = new DataOutputStream(
					(OutputStream) localObject);
			String str = String.valueOf(paramString);
			localObject = str + "\n";
			localDataOutputStream.writeBytes((String) localObject);
			localDataOutputStream.flush();
			localDataOutputStream.writeBytes("exit\n");
			localDataOutputStream.flush();
			localProcess.waitFor();
			localObject = localProcess.exitValue();
			return -1;			
		}catch (Exception localException){
			localException.printStackTrace();
		}
		return 0;		
	}
	
}
