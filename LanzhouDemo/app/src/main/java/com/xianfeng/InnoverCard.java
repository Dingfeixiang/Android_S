package com.xianfeng;

import java.util.Map;

/**
 * Created by xianfeng on 2016/11/28.
 */


public class InnoverCard {

    private RW_Card _rw_card;
    private String _dataStr = "";

    public void setDataStr(String dataStr){
        _dataStr = dataStr;
    }

    public InnoverCard(){
        _rw_card = new RW_Card();
    }

    public InnoverCard(String dataStr){
        _rw_card = new RW_Card();
        _dataStr = dataStr;
    }

    public String writeCard(WriteData data){
        String res = "";
        try{
            if (_rw_card.writeOrders(_dataStr,data) == 0){
                res = CodeFormat.byteArr2HexStr(data.getOutbuf());
            }else {
                res = "";
            }
        }catch (Exception ex){
            res = "";
            System.out.println("写卡数据"+ex.getMessage());
        }
        return res;
    }

    public ReadData readCard(){
        ReadData readData;
        readData = _rw_card.readCard(_dataStr);
        return readData;
    }

    public boolean myCard(String companyStr){
        boolean mine = false;
        try{
            byte[] btcomstr = CodeFormat.hexStr2ByteArr(companyStr);
            int res = _rw_card.innoverCard(_dataStr,btcomstr);
            if (res == 0){
                mine = true;
            }else {
                mine = false;
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            mine = false;
        }
        return mine;
    }
}
