package com.xianfeng;


/**
 * Created by xianfeng on 2016/11/24.
 */

public class RW_Card {

    public int writeOrders(String dataStr,WriteData data){
        int errorcode = -1;

        return errorcode;
    }

    public ReadData readCard(String dataStr){
        ReadData data = new ReadData();

        byte[] hbuf = new byte[257];
        //一代表卡片类型
        byte[] cardtype = new byte[2];
        //卡片标识
        byte[] cardflag = new byte[2];
        //更新标识
        byte[] meterflag = new byte[2];
        //Y300D标识 扩频表标识
        byte[] safeflag = new byte[2];


        try{
            if (dataStr.length() > 512) return null;

            hbuf = CodeFormat.hexStr2ByteArr(dataStr);
            safeflag = memcpy(safeflag,0,hbuf,0x12,1);
            cardtype = memcpy(cardtype,0,hbuf,0x21,1);

            cardflag = memcpy(cardflag,0,hbuf,0x32,1);
            meterflag = memcpy(meterflag,0,hbuf,0x50,1);

            if (cardtype[0] != 0x50) return null;

            if (safeflag[0] == 0x01){

                //赋值公司代码
                data.corpno = corporationID(hbuf,0x27);

                //卡号
                data.userno = cardNumber(hbuf,0x22);

                //表号
                StringBuffer meterno = new StringBuffer();
                meterno.append(dataStr.substring(0xC1*2,2));
                meterno.append(dataStr.substring(0xC3*2,8));
                data.meterno = meterno.toString();

                //购气次数
                data.buycount = hbuf[0x36] * (0xFF + 1) + hbuf[0x37];

                //气量
                StringBuffer cardgases = new StringBuffer();
                cardgases.append(dataStr.substring(0x33*2,2));
                cardgases.append(dataStr.substring(0x9A*2,6));
                data.cardgases = Double.valueOf(cardgases.toString());

                //购气时间
                data.buygasdate = purchaseDate(hbuf,dataStr);
                data.errorcode = 0;
            } else {

                //赋值公司代码
                data.corpno = corporationID(hbuf,0x27);

                //赋值表号(10 位)
                data.meterno = "";

                //判断是否为300表
                if (((cardflag[0] == 0x50) || (cardflag[0] == 0x51))
                        && ((meterflag[0] == 0x94) || (meterflag[0] == 0x83))){
                    //卡号
                    data.userno = cardNumber(hbuf,0x22);

                    //气量
                    StringBuffer cardgases = new StringBuffer();
                    cardgases.append(dataStr.substring(0x9A*2,6));
                    data.cardgases = Double.valueOf(cardgases.toString());

                    data.gasfee = 0.0;

                    //次数
                    data.buycount = 0;

                    //购气时间
                    data.buygasdate = purchaseDate(hbuf,dataStr);

                    data.errorcode = 0;
                }
                //判断是否为优化表
                else if(((cardflag[0] == 0x52) || (cardflag[0] == 0x53))
                        && ((meterflag[0] == 0x21) || (meterflag[0] == 0x94))){

                    //卡号
                    data.userno = cardNumber(hbuf,0x22);

                    int cardgases = hbuf[0x2A] * 0x10000 + hbuf[0x2B] * 0x100 + hbuf[0x2C];
                    data.cardgases = Double.valueOf((double)cardgases);

                    data.gasfee = 0.00;

                    data.buycount = 0;

                    //购气时间
                    data.buygasdate = purchaseDate(hbuf,dataStr);

                    data.errorcode = 0;
                }else {
                    data.errorcode = -3;
                }
            }

        }catch (Exception ex){
            data.errorcode = -1;
            System.out.println("读卡发生错误！");
        }
        return data;
    }

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    //是否为先锋卡
    public int innoverCard(String dataStr,byte[] companyStr){

        int errorcode = -1;

        byte[] hbuf = new byte[257];
        byte[] logicCard = new byte[3];

        try{
            //卡片数据asc转16进制
            if (dataStr.length() > 512) return -1;
            hbuf = CodeFormat.hexStr2ByteArr(dataStr);

            //逻辑加密卡表用户卡标识
            memcpy(hbuf,0x21,logicCard,0,1);
            if (hbuf[0x27] != 0x50) return -1;

            //公司代码
            String corpID = corporationID(hbuf,0x27);

            if (CodeFormat.byteArr2HexStr(companyStr).contains(corpID)){
                errorcode = 0;
            }else {
                errorcode = -1;
            }

        }catch (Exception ex){
            System.out.println("there is something wrong in innoverCard");
        }

        return errorcode;
    }

    //byte 与 int 的相互转换
    public static byte intToByte(int x) {
        return (byte) x;
    }

    //时间
    public String purchaseDate(byte[] hbuf,String dataStr){
        StringBuffer buygasdate = new StringBuffer();
        try{
            if ((hbuf[0x99] & 0x40) == 0){
                buygasdate.append(dataStr.substring(0x96*2,8));
            }else {
                //日期格式为 6 + 2
                buygasdate.append(dataStr.substring(0x96*2,6));
                byte[] tempdate = new byte[2];
                tempdate = memcpy(tempdate,0,hbuf,0x99,1);
                int sfdate = tempdate[0] ^ 0xC0;
                tempdate = itoa(intToByte(sfdate),16);
                tempdate = zeroFill(tempdate,2);
                //加两位不知道是什么的码
                buygasdate.append(CodeFormat.byteArr2HexStr(tempdate));
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return buygasdate.toString();
    }

    //提取卡号
    public String cardNumber(byte[] hbuf,int start){
        String cardno = "";
        for (int i=0;i <= 4;i++){
            byte tmp = hbuf[i + start];
            String temp = CodeFormat.addZeroString(String.valueOf(tmp),1);
            cardno += temp;
        }
        return cardno;
//        //卡号
//        for (int i = 0;i <= 4;i++){
//            byte tmp = hbuf[i + 0x22];
//            byte[] tempCardnppart = itoa(tmp,10);
//            tempCardnppart = zeroFill(tempCardnppart,2);
//            cardno = memcpy(cardno,i*2,tempCardnppart,0,2);
//        }
//        data.userno = CodeFormat.byteArr2HexStr(cardno);
    }

    //提取公司代码
    public String corporationID(byte[] hbuf,int start){
        String corpID = "";
        for (int i = 0;i <= 2;i++){
            byte tmp = hbuf[i + start];
            String temp = CodeFormat.addZeroString(String.valueOf(tmp),1);
            corpID += temp;
        }
        return corpID;
//        //赋值公司代码
//        for (int i = 0;i <= 2;i++){
//            byte tmp = hbuf[i + 0x27];
//            //转换为十进制数
//            byte[] tempCorpID = itoa(tmp,10);
//            //数组补0
//            tempCorpID = zeroFill(tempCorpID,2);
//            corpID = memcpy(corpID,i*2,tempCorpID,0,2);
//        }
//        data.corpno = CodeFormat.byteArr2HexStr(corpID);
    }

    //System提供了一个静态方法arraycopy(),我们可以使用它来实现数组之间的复制
    public byte[] memcpy(byte[] des,int i,byte[] original,int j,int length){
        System.arraycopy(des,i,original,j,length);
        return des;
    }

    //数字不足两位补0（公司代码和编号需要）
    public byte[] zeroFill(byte[] cropID,int length){
        //按Number数组长度把Ptr数组前面补零
        byte[] cropIDIwant = new byte[512];



        return cropIDIwant;
    }

    //十六进制转换为 ? 进制数
    public byte[] itoa(byte bdata,int radix){
        if (radix == 10){

        }else if(radix == 16){

        }
        return new byte[0];
    }

    //函数用于判断字符串str2是否是str1的子串
    public int strstr(String source, String target) {
        if (source == null || target == null) {
            return -1;
        }

        for (int i = 0; i < source.length() - target.length() + 1; i++) {
            int j = 0;
            for (j = 0; j < target.length(); j++) {
                if (source.charAt(i + j) != target.charAt(j)) {
                    break;
                }
            }
            // finished loop, target found
            if (j == target.length()) {
                return i;
            }
        }
        return -1;
    }

}
