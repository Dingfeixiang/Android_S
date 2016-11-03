package com.xianfeng.sanyademo.util;

import com.xianfeng.sanyademo.model.RecordData;

import cardInterface.CardInfo;

/**
 * Created by xianfeng on 2016/10/25.
 */

//import cardInterface.CardInfo;

//卡片处理逻辑
public class CardHandler {

    private static final String CARD_CHECKPASS4442_COMMAND = "0103000003"; //发送读卡命令
    //发送写卡命令 写卡开始位置是20，长度是224
    private static final String CARD_WRITE4442_COMMAND_FOR_ONCE   = "01020020e0";
    private static final String CARD_CHANGPASS4442_COMMAND = "0105000003"; //密码较验

    private static final String SINGAL_RECEIVEDATA_SUCCESS = "9000";    //请求成功
    private static final String SINGAL_TIMEOUT = "6F06";    //超时

    MWManager  mwManger = MWManager.getHelper();
    public CardHandler(){}

    public boolean isConnected(){
        return (mwManger.myReader == null)?false:true;
    }

    public int beep(int beepTimes, int interval, int time){
        int result = 0;
        try{
            result = mwManger.myReader.beep(beepTimes,interval,time);
        }catch (Exception ex){
            result = 0;
        }
        return result;
    }

    public String readCard(){
        String data = "";
        try{
            data = mwManger.myReader.reader4442(32,224);
        }catch (Exception ex){
            data = "";
        }
        return data;
    }

    public boolean checkVerify(String verify){
        boolean checkRecult = false;
        try{
            mwManger.myReader.verifyPassword4442(verify);
            checkRecult = true;
        }catch (Exception ex){
            checkRecult = false;
        }
        return checkRecult;
    }

    public void changeVerify(String verifynew){
        try{
            mwManger.myReader.changePassword4442(verifynew);
        }catch (Exception ex){

        }
    }

    //向卡中写数据
    public boolean writeCard(String data){
        boolean writeResult = false;
        try{
            mwManger.myReader.beep(1,1,1);

            String subString = data.substring(64,512);
            mwManger.myReader.write4442(32,subString);

            mwManger.myReader.write4442(18,"01");
            writeResult = true;
            System.out.println("写卡操作");
        }catch (Exception ex){
            writeResult = false;
            System.out.println("写卡错误！");
        }
        return writeResult;
    }

    //获取写卡数据
    public String[] getWriteData(RecordData recordData){
        if (recordData.getCardno().length() < 10){
            return null;
        }
        String[] writeData = null;
        CardInfo cardInfo = new CardInfo();
        String cardno = recordData.getCardno();
        String reviseCardno = cardno.substring(cardno.length()-10,cardno.length());
        writeData = cardInfo.writeOrders(recordData.getGases(),recordData.getGasfee(),
                recordData.getPrice1(), recordData.getPrice2(),recordData.getPrice3(),
                recordData.getLaddgas1(),recordData.getLaddgas2(), recordData.getPricedate(),
                recordData.getPricetype(),recordData.getPricever(),recordData.getPricecycle(),
                recordData.getClearflag(),recordData.getCycledate(),recordData.getNewprice1(),
                recordData.getNewprice2(), recordData.getNewprice3(),recordData.getNewladdgas1(),
                recordData.getNewladdgas2(),recordData.getNewpricedate(), recordData.getNewpricetype(),
                recordData.getNewpricever(),recordData.getNewpricecycle(),recordData.getNewclearflag(),
                recordData.getNewcycledate(),recordData.getMeterno(),recordData.getCompanyno(),
                reviseCardno);
        return writeData;
    }
}
