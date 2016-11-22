package com.xianfeng.NFC;

/**
 * Created by xianfeng on 2016/11/17.
 */

public class NFCCallback {

    public enum Status{
        TAG_EMPTY,
        TAG_WRITE_SUCCESS,
        TAG_WRITE_FAILED,
    }

    public interface TagCallBack{
        void currentTagStatus(Status status);
    }

    public interface ReadCallBack{
        void readFinish(String something);
    }
}
