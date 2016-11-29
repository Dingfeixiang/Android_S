package com.xianfeng;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xianfeng on 2016/11/24.
 */

public class RW_Card {

    public int writeOrders(String dataStr,WriteData data){
        int errorcode = -1;
        byte[] hbuf = new byte[257];
        String data512 = dataStr.substring(0,512);

        //取卡片标识
        String cardtype = data512.substring(0x21*2,0x21*2+2);//一代表卡片类型
        String cardflag = data512.substring(0x32*2,0x32*2+2);//卡片标识
        String meterflag = data512.substring(0x50*2,0x50*2+2);//更新标识
        String safeflag = data512.substring(0x12*2,0x12*2+2);//Y300D标识 扩频表标识
        String isflag = data512.substring(0x13*2,0x13*2+2);//扩频表表具返写标识

        //写卡数据

        //useful
        int initflag;
        int cardgas,cardgas_y;
        int w_meterflag;


        try{
            hbuf = CodeFormat.hexStr2ByteArr(data512);
            byte[] btCardtype = CodeFormat.hexStr2ByteArr(cardtype);
            byte[] btCardFlag = CodeFormat.hexStr2ByteArr(cardflag);
            byte[] btMeterflag = CodeFormat.hexStr2ByteArr(meterflag);
            byte[] btSafeflag = CodeFormat.hexStr2ByteArr(safeflag);

            if (btCardtype[0] != 0x50) return -1;

            if (btSafeflag[0] == 0x01){

                //购气次数
                String cardBuyCount = "";
                cardBuyCount = CodeFormat.addZeroString
                        (CodeFormat.Integer2HexStr(data.buycount),2);
                byte[] btCardBuyCount = CodeFormat.hexStr2ByteArr(cardBuyCount);

                //设置时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String tmpDate = sdf.format(data.date);
                byte[] btDate = CodeFormat.hexStr2ByteArr(tmpDate);
                //判断是否带初始化标识
                if ((hbuf[0x99] & 0x40) == 0){
                    hbuf[0x99] = btDate[3];
                }else {
                    hbuf[0x99] = (byte) (btDate[3] | 0xC0);
                }

                //设置购气量
                String cardGases= CodeFormat.addZeroString(String.valueOf(data.gases),4);
                byte[] btCardGas = CodeFormat.hexStr2ByteArr(cardGases);

                //设置检验码
                int Sum = 0;
                Sum = (Sum ^ btCardGas[0] ^ btCardGas[1] ^ btCardGas[2] ^ btCardGas[3]
                        ^ hbuf[0x35] ^ btCardBuyCount[0] ^ btCardBuyCount[1]) % 0xFF;

                //组合卡面数据
                hbuf[0x21] = 0x50;								//卡类别代码
                hbuf[0x33] = btCardGas[0];						//卡上气量最高字节
                hbuf[0x36] = btCardBuyCount[0];
                hbuf[0x37] = btCardBuyCount[1];				    //购气次数
                hbuf[0x38] = (byte) Sum;						//关键数据校验码
                hbuf[0x96] = btDate[0];
                hbuf[0x97] = btDate[1];
                hbuf[0x98] = btDate[2];
                hbuf[0x99] = btDate[3];							//购气时间
                hbuf[0x9A] = btCardGas[1];
                hbuf[0x9B] = btCardGas[2];
                hbuf[0x9C] = btCardGas[3];					    //卡上气量低字节

                int i;
                for (i = 0x51; i <= 0x80; i++)
                {
                    //清空月累用气量
                    hbuf[i] = (byte)0xFF;
                }
                for (i = 0x9E; i <= 0xB3; i++)
                {
                    //清空表工作状态字、月指针、累用、累购、累用最高位、累购最高位
                    hbuf[i] = (byte)0xFF;
                }

                //卡片密码
                byte[] cardNo = new byte[9];
                memcpy(cardNo,0,hbuf,0xC8,4);
                byte[] oldpwd = new byte[9];

                data.password = calcCardPwd(cardNo);
                data.pwstr = calcCardPwd(cardNo);
                data.outbuf = hbuf;

                errorcode = 0;
                return errorcode;

            }
            else {
                //赋值密码
                byte[] tempcard_no = new byte[5];
                byte[] tempcard_no_new = new byte[7];

                byte[] tmpcardno = new byte[11];
                byte[] tmpcardno_new = new byte[7];
                byte[] sdate = new byte[5];

                if (((btCardFlag[0] == 0x50) || (btCardFlag[0] == 0x51)) &&
                        ((btMeterflag[0] == (byte) 0x94) || (btMeterflag[0] == (byte)0x83))){
                    //判断是否为300表
                    tempcard_no = memcpy(tempcard_no,0,hbuf,0xC8,4);
                    //中中中
//                    byte[] oldpwd = new byte[4];
                    data.password = calcCardPwd(tempcard_no);
                }
                else if (((btCardFlag[0] == 0x52) || (btCardFlag[0] == 0x53)) &&
                        ((btMeterflag[0] == (byte)0x21))) {
                    tempcard_no_new = memcpy(tempcard_no_new,0,hbuf,0x22,6);
                    data.password = calcCardPwd_New(tempcard_no_new);
                }
                else if (((btCardFlag[0] == 0x52) || (btCardFlag[0] == 0x53)) &&
                        ((btMeterflag[0] == (byte)0x94))){
                    tempcard_no = memcpy(tempcard_no,0,hbuf,0xC8,4);
                    data.password = calcCardPwd(tempcard_no);
                }
                else {
                    errorcode = -3;
                    return errorcode;
                }

                //判断是否带初始化标识{
                if ((hbuf[0x99] & 0x40) == 0){
                    initflag = 0;
                }else{
                    initflag = 1;
                }

                //Y300区卡上气量
                String tmpBuyGasNum = data512.substring(0x9A*2,0x9A*2+6);
                cardgas = Integer.valueOf(tmpBuyGasNum);

                //优化区气量
                cardgas_y = hbuf[10] * 65536 + hbuf[11] * 256 + hbuf[12];

                //设置写卡数据
                ////优化区数据////////////////////////////////////////////////////////
                //年月日
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String tmpDate = sdf.format(data.date);
                byte[] btDate = CodeFormat.hexStr2ByteArr(tmpDate);

                //设置老卡区日期，新卡区表具标识，验证密码，新密码
                sdate = memcpy(sdate,0,btDate,0,4);
                if ((btMeterflag[0] == (byte)0x94) &&
                        ((btCardFlag[0] == 0x50) || (btCardFlag[0] == 0x51))){
                    if (initflag == 1){
                        sdate[3] = (byte) (sdate[3] | 0xC0);
                        w_meterflag = 0x94;
                    }else {
                        w_meterflag = 0x83;
                    }

                    //校验卡片密码
                    tmpcardno = memcpy(tmpcardno,0,hbuf,0xC8,4);
                    data.pwstr = calcCardPwd(tmpcardno);

                }else if((btMeterflag[0] == (byte)0x83) &&
                        ((btCardFlag[0] == 0x50) || (btCardFlag[0] == 0x51))){
                    w_meterflag = 0x83;

                    //校验卡片密码
                    tmpcardno = memcpy(tmpcardno,0,hbuf,0xC8,4);
                    data.pwstr = calcCardPwd(tmpcardno);

                }else if(((btMeterflag[0] == (byte)0x21) || (btMeterflag[0] == (byte)0x94)) &&
                        ((btCardFlag[0] == 0x52) || (btCardFlag[0] == 0x53))){
                    w_meterflag = 0x21;

                    tmpcardno_new = memcpy(tmpcardno_new,0,hbuf,0x22,6);
                    data.pwstr = calcCardPwd_New(tmpcardno_new);

                }
                else {
                    errorcode = -3;
                    return errorcode;
                }

                hbuf[0x21] = 0x50;
                //[2]~[9]不变
                //优化区气量
                hbuf[0x2A] = (byte)(data.gases / 65536);
                hbuf[0x2B] = (byte)((data.gases % 65536) / 256);
                hbuf[0x2C] = (byte)(data.gases % 256);

                //优化区日期yymmdd[13]~[15]
                hbuf[0x2D] = btDate[0];
                hbuf[0x2E] = btDate[1];
                hbuf[0x2F] = btDate[2];

                hbuf[0x30] = 0;
                hbuf[0x31] = 0;

                //(21H-32H)校验和HEX码
                hbuf[0x20] = 0;
                int i;
                for (i = 0x21; i <= 0x32; i++) {
                    hbuf[0x20] = (byte) ((hbuf[0x20] + hbuf[i]) % 256);
                }
                hbuf[0x20] = (byte) ((hbuf[0x20] + 3) % 256);
                for (i = 0x33; i <= 0x4A; i++) {
                    hbuf[i] = 0;
                }

                //卡上气量备份
                hbuf[0x4B] = hbuf[0x2A];
                hbuf[0x4C] = hbuf[0x2B];
                hbuf[0x4D] = hbuf[0x2C];

                hbuf[0x4E] = 0;
                for (i = 0x4B; i <= 0x4D; i++) {
                    hbuf[0x4E] = (byte) ((hbuf[0x4E] + hbuf[i]) % 256);
                }
                hbuf[0x4E] = (byte)((hbuf[0x4E] + 3) % 256);

                hbuf[0x4F] = 1;
                hbuf[0x50] = (byte)w_meterflag;

                //////////////////////////////////////////////y300区数据
                //计算气量 cardgas
                String gasesStr = String.valueOf(data.gases);
                byte[] buf1 = CodeFormat.hexStr2ByteArr(CodeFormat.addZeroString(gasesStr,3));
                //赋值日期
                for (i=0;i<4;i++){
                    hbuf[0x96+i] = sdate[i];
                }
                //赋值气量
                for (i=0;i<3;i++){
                    hbuf[0x9A+i] = buf1[i];
                }
                //清返写区
                for (i=0x9D;i <= 0xC7;i++){
                    hbuf[i] = (byte) 0xFF;
                }

                data.outbuf = hbuf;
                errorcode = 0;
                return errorcode;
            }
        }catch (Exception ex){
            System.out.println("写卡" + ex.getMessage());
        }

        return errorcode;
    }

    public String calcCardPwd_New(byte[] card_no){
        String pwd = "";
        byte[] ret = new byte[4];
        byte[] customID = new byte[7];

        customID = memcpy(customID,0,card_no,0,6);
        ret[0] = (byte) (customID[0] + customID[3] ^ 0x76);
        ret[1] = (byte) (customID[1] + customID[4] ^ 0x77);
        ret[2] = (byte) (customID[2] + customID[5] ^ 0x80);

//        coveredID = hexToChar(customID,3);
        try{
            pwd = CodeFormat.byteArr2HexStr(ret).substring(0,6);
        }catch (Exception ex){
            pwd = "";
            System.out.println("计算密码时出现错误");
        }
        return pwd;
    }

    public String calcCardPwd(byte[] card_no){
        String pwd = "";
        byte[] ret = new byte[4];
        byte[] customID = new byte[5];
        byte[] coveredID;

        customID = memcpy(customID,0,card_no,0,4);
        for (int i=0;i <= 3; i++){
            ret[0] = (byte) (ret[0] + customID[i]);
        }
        ret[1] = (byte)(customID[2] + 0x1B);
        ret[2] = customID[3];

//        coveredID = hexToChar(customID,3);
        try{
            pwd = CodeFormat.byteArr2HexStr(ret).substring(0,6);
        }catch (Exception ex){
            pwd = "";
            System.out.println("计算密码时出现错误");
        }
        return pwd;
    }

    //把一个十六进制数拼成两个字符, len 转换后数字BYTE
    byte[] hexToChar(byte[] from,int len){
        byte[] btout = new byte[0];


        return btout;
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
                meterno.append(dataStr.substring(0xC1*2,0xC1*2+2));
                meterno.append(dataStr.substring(0xC3*2,0xC3*2+8));
                data.meterno = meterno.toString();

                //购气次数
                data.buycount = hbuf[0x36] * (0xFF + 1) + hbuf[0x37];

                //气量
                StringBuffer cardgases = new StringBuffer();
                cardgases.append(dataStr.substring(0x33*2,0x33*2+2));
                cardgases.append(dataStr.substring(0x9A*2,0x9A*2+6));
                data.cardgases = Integer.valueOf(cardgases.toString());

                //购气时间
                data.buygasdate = purchaseDate(hbuf,dataStr);
                data.errorcode = 0;
            }
            else {

                //赋值公司代码
                data.corpno = corporationID(hbuf,0x27);

                //赋值表号(10 位)
                data.meterno = "";

                //判断是否为300表
                //((cardflag[0] == 0x50) || (cardflag[0] == 0x51)) &&
                if (((meterflag[0] == (byte)0x94) || (meterflag[0] == (byte)0x83))){
                    //卡号
                    data.userno = cardNumber(hbuf,0x22);

                    //气量
                    StringBuffer cardgases = new StringBuffer();
                    cardgases.append(dataStr.substring(0x9A*2,0x9A*2+6));
                    data.cardgases = Integer.valueOf(cardgases.toString());

                    data.gasfee = 0.0;

                    //次数
                    data.buycount = 0;

                    //购气时间
                    data.buygasdate = purchaseDate(hbuf,dataStr);

                    data.errorcode = 0;
                }
                //判断是否为优化表
                else if(((cardflag[0] == 0x52) || (cardflag[0] == 0x53))
                        && ((meterflag[0] == 0x21) || (meterflag[0] == (byte)0x94))){

                    //卡号
                    data.userno = cardNumber(hbuf,0x22);

                    int cardgases = hbuf[0x2A] * 0x10000 + hbuf[0x2B] * 0x100 + hbuf[0x2C];
                    data.cardgases = cardgases;

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
            System.out.println("读卡发生错误:"+ex.getMessage());
        }
        return data;
    }

    //是否为先锋卡
    public int innoverCard(String dataStr,byte[] companyStr){

        int errorcode = -1;

        byte[] hbuf = new byte[257];
        try{
            //卡片数据asc转16进制
            if (dataStr.length() > 512) return -1;
            hbuf = CodeFormat.hexStr2ByteArr(dataStr);

            //逻辑加密卡表用户卡标识
            if (hbuf[0x21] != 0x50) return -1;

            //公司代码
            String corpID = corporationID(hbuf,0x27);

            if (CodeFormat.byteArr2HexStr(companyStr)
                    .substring(0,6).contains(corpID)){
                errorcode = 0;
            }else {
                errorcode = -1;
            }

        }catch (Exception ex){
            System.out.println("there is something wrong in innoverCard");
        }

        return errorcode;
    }


    //时间
    public String purchaseDate(byte[] hbuf,String dataStr){
        StringBuffer buygasdate = new StringBuffer();
        try{
            if ((hbuf[0x99] & 0x40) == 0){
                buygasdate.append(dataStr.substring(0x96*2,0x96*2+8));
            }else {
                //日期格式为 6 + 2
                buygasdate.append(dataStr.substring(0x96*2,0x96*2+6));

                //加两位不知道是什么的码
                byte[] tempdate = new byte[2];
                byte add = (byte) (hbuf[0x99] ^ 0xC0);
                tempdate[0] = add;
                buygasdate.append(CodeFormat.byteArr2HexStr(tempdate).substring(0,2));
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
    }

    //System提供了一个静态方法arraycopy(),我们可以使用它来实现数组之间的复制
    public byte[] memcpy(byte[] des,int i,byte[] original,int j,int length){
        for (int k=0;k < length;k++){
            des[k+i] = original[k+j];
        }
//        System.arraycopy(des,i,original,j,length);
        return des;
    }

    //数字不足两位补0（公司代码和编号需要）

    //十六进制转换为 ? 进制数\

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
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
