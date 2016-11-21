package com.xianfeng.NFC;

/**
 * Created by xianfeng on 2016/11/17.
 */

public class NFCCallback {

    public enum TagStatus{
        TAG_EMPTY,
        TAG_USED,
    }

    public interface TagCallBack{
        void currentTagStatus(TagStatus status);
    }

    public interface ReadCallBack{
        void readFinish(String something);
    }
}
