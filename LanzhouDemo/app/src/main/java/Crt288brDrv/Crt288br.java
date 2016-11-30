package Crt288brDrv;
import android.R.integer;
import android.util.Log;

public class Crt288br {
	public native int OpenDevice(char sPort[], int iBaudRate);
	public native int OpenUsbDevice(int fd);
	public native int CloseDevice();
	public native int ExeCommand(int iSendDataLen, byte bySendData[], int iRecvDataLen[], byte byRecvData[], byte byStCode[]);
	public native int GetDllVersion(char VersMes[]);
	public native int InitDev();
	public native int GetCardStatus();
	public native int GetICType();
	public native int CardPowerOperation(int iType);
	public native int ContactCPUReset(int iMode, int iAtr[], byte byAtr[]);
	public native int ContactCPUSendAPDU(int iProtocol, int iSendLen, byte bySendData[], int iRecvLen[], byte byRecvData[]);
	public native int SIMReset(int iMode,int iPm, int iSimNums, int iAtr[], byte byAtr[]);
	public native int SIMSendAPDU(int iProtocol, int iSimNums, int iSendLen, byte bySendData[], int iRecvLen[], byte byRecvData[]);
	public native int I24CxxSetCardType(int i24CxxType);
	public native int I24CxxProcess(int iMode ,int i24CxxType, int wStartAddr, int uDataLength[], byte lpData[]);
	public native int NonContactCPUPower(int iType, int iAtr[], byte byAtr[]);
	public native int NonContactCPUSendAPDU(int iSendLen, byte bySendData[], int iRecvLen[], byte byRecvData[]);
	public native int NonContactCPUGetCardSN(char szSN[]);
	public native int SLE4442Power(int iAtr[], byte byAtr[]);
	public native int SLE4442CheckPasswd(int iMode, int uDataLength, byte lpData[]);
	public native int SLE4442Process(int iMode, int iRegion ,int wStartAddr, int uDataLength[], byte lpData[]);
	public native int SLE4428Power(int iAtr[], byte byAtr[]);
	public native int SLE4428CheckPasswd(int uDataLength, byte lpData[]);
	public native int SLE4428ChangePasswd(int iNewKeyLen, byte byNewKeyData[], int iOldKeyLen, byte byOldKeyData[]);
	public native int SLE4428Process(int iMode, int iRegion ,int wStartAddr, int uDataLength[], byte lpData[]);
	
	static
	{
        try {  

        	System.loadLibrary("crt288bru_drv");	
        }  

        catch (UnsatisfiedLinkError ule) {  
        	Log.e("crt288br", "Load crt288bru_drv Error!");
        }  
	}

}
