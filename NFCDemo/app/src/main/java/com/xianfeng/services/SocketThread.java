package com.android.ble.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.xianfeng.util.CodeFormat;
import com.xianfeng.assist.NFC4442Message;


public class SocketThread extends Thread {
    // Debugging
    private static final String TAG = "SocketThread";

    private Handler mHandler = null;
    private String mData = null;
    private int mType;

    private Socket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;

    public SocketThread(Handler handler, String data, int type) {
        mHandler = handler;
        mData = data;
        mType = type;
    }

    /**
     * Connect Socket
     *
     * @throws Exception
     */
    public void connect(String HOST_IP,String HOST_PORT) throws Exception {
        mSocket = new Socket();
        SocketAddress endpoint = new InetSocketAddress(HOST_IP, Integer.parseInt(HOST_PORT));
        mSocket.connect(endpoint, 10000);

        mInStream = mSocket.getInputStream();
        mOutStream = mSocket.getOutputStream();
    }

    /**
     * Connect Socket
     *
     * @throws Exception
     */
    public void connect() throws Exception {
        mSocket = new Socket();
        SocketAddress endpoint = new InetSocketAddress(NFC4442Message.HOST_IP,
        Integer.parseInt(NFC4442Message.HOST_PORT));
        mSocket.connect(endpoint, 10000);

        mInStream = mSocket.getInputStream();
        mOutStream = mSocket.getOutputStream();
    }


    public void run() {
        Log.i(TAG, "Socket_Start");

        try {
            connect();
        } catch (Exception e) {
            Log.e(TAG, "Socket_Failed", e);

            Message msg = new Message();
            msg.what = NFC4442Message.MESSAGE_SOCKET;
            msg.arg1 = 0;
            mHandler.sendMessage(msg);

            return;
        }

        Log.i(TAG, "Socket_Success");

        try {
            write(mData.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "Send data exception", e);

            Message msg = new Message();
            msg.what = NFC4442Message.MESSAGE_UI;
            msg.arg1 = 1;
            mHandler.sendMessage(msg);

            return;
        }

        try {
            mSocket.shutdownOutput();

            int length = 0;
            byte[] bytes = new byte[1024];

            while ((length = mInStream.read(bytes)) != -1) {
                Log.i(TAG, "Server Respond: " + CodeFormat.hexToStringGBK(CodeFormat.byteArr2HexStr(bytes), length)
                        + ", Data Length: " + String.valueOf(length));

                Message msg = new Message();
                msg.what = NFC4442Message.MESSAGE_UI;
                msg.arg1 = mType;
                msg.obj = bytes;
                mHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            Log.e(TAG, "Receive data exception", e);

            Message msg = new Message();
            msg.what = NFC4442Message.MESSAGE_UI;
            msg.arg1 = 2;
            mHandler.sendMessage(msg);
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     * @throws Exception
     */
    private void write(byte[] buffer) throws Exception {
        Log.i(TAG, "Client Send: " + CodeFormat.hexToStringGBK(CodeFormat.byteArr2HexStr(buffer), buffer.length));

        mOutStream.write(buffer);
        mOutStream.flush();
    }

    /**
     * Close the connected Socket.
     */
    public void cancel() {
        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Socket_Close_Exception", e);
        }
    }
}
