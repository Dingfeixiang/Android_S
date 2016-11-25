package com.xianfeng;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class CodeFormat {

	/**
	 * 将byte数组转换为表示16进制值的字符串
	 * 
	 * @param arr
	 *            需要转换的byte数组
	 * @return 转换后的字符串
	 * @throws Exception
	 *             本方法不处理任何异常，所有异常全部抛出
	 */
	public static String byteArr2HexStr(byte[] arr) throws Exception {
		int iLen = arr.length;
		// 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
		StringBuffer sb = new StringBuffer(iLen * 2);
		for (int i = 0; i < iLen; i++) {
			int intTmp = arr[i];
			// 把负数转换为正数
			while (intTmp < 0) {
				intTmp = intTmp + 256;
			}
			// 小于0F的数需要在前面补0
			if (intTmp < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}

	/**
	 * 将表示16进制值的字符串转换为byte数组
	 * 
	 * @param strIn
	 *            需要转换的字符串
	 * @return 转换后的byte数组
	 * @throws Exception
	 *             本方法不处理任何异常，所有异常全部抛出
	 */
	public static byte[] hexStr2ByteArr(String strIn) throws Exception {
		byte[] arr = strIn.getBytes();
		int iLen = arr.length;

		// 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arr, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}

	/**
	 * 将表示10进制值的字符串转换为byte数组
	 * 
	 * @param strIn
	 *            需要转换的字符串
	 * @return 转换后的byte数组
	 * @throws Exception
	 *             本方法不处理任何异常，所有异常全部抛出
	 */
	public static byte[] str2ByteArr(String strIn) throws Exception {
		byte[] arr = strIn.getBytes();
		int iLen = arr.length;

		// 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arr, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 10);
		}
		return arrOut;
	}

	/**
	 * 16进制字符串转2进制字符串
	 * 
	 * @param hexString
	 * @return
	 */
	public static String hexStr2BinaryStr(String hexString) {
		if (hexString == null || hexString.length() % 2 != 0)
			return null;
		String bString = "", tmp;
		for (int i = 0; i < hexString.length(); i++) {
			tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
			bString += tmp.substring(tmp.length() - 4);
		}
		return bString;
	}

	/**
	 * 2进制字符串转16进制字符串
	 * 
	 * @param bString
	 * @return
	 */
	public static String binaryStr2hexStr(String bString) {
		if (bString == null || bString.equals("") || bString.length() % 8 != 0)
			return null;
		StringBuffer tmp = new StringBuffer();
		int iTmp = 0;
		for (int i = 0; i < bString.length(); i += 4) {
			iTmp = 0;
			for (int j = 0; j < 4; j++) {
				iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
			}
			tmp.append(Integer.toHexString(iTmp));
		}
		return tmp.toString();
	}

	/**
	 * 16进制高低位互换
	 * 
	 * @param hexString
	 * @return
	 */
	public static String hexHigh2Low(String hexString) {
		int length = hexString.length() / 2;
		StringBuffer sb = new StringBuffer();
		for (int i = length; i > 0; i--) {
			String str = hexString.substring(2 * i - 2, 2 * i);
			sb.append(str);
		}
		return sb.toString();
	}

	/**
	 * 2进制高低位互换
	 * 
	 * @param binaryStr
	 * @return
	 */
	public static String binaryHigh2Low(String binaryString) {
		StringBuffer sb = new StringBuffer();
		for (int i = binaryString.length(); i > 0; i--) {
			String str = binaryString.substring(i - 1, i);
			sb.append(str);
		}
		return sb.toString();
	}

	/**
	 * 日期字符串转16进制字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String dateStr2HexStr(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length() / 2; i++) {
			String hexStr = Integer.toHexString(Integer.parseInt(str.substring(2 * i, 2 * i + 2)));
			if (hexStr.length() == 1) {
				hexStr = "0" + hexStr;

			}
			sb.append(hexStr);
		}
		return sb.toString();
	}

	/**
	 * 整型转16进制字符串
	 * 
	 * @param num
	 * @return
	 */
	public static String Integer2HexStr(int num) {
		String hexStr = Integer.toHexString(num);
		if (hexStr.length() == 1)
			hexStr = "0" + hexStr;
		return hexStr;
	}
	
	/**
	 * 16进制字符串转日期字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String hexStr2DateStr(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length() / 2; i++) {
			String dateStr = Integer.toString(hexStr2Int(str.substring(2 * i, 2 * i + 2)));
			if (dateStr.length() == 1) {
				dateStr = "0" + dateStr;
			}
			sb.append(dateStr);
		}
		return sb.toString();
	}

	/**
	 * Byte -> Hex
	 * 
	 * @param bytes
	 * @return
	 */
	public static String byte2Hex(byte[] bytes, int count) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < count; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * 16进制字符串转int
	 * 
	 * @param str
	 * @return
	 */
	public static int hexStr2Int(String str) {
		int a = Integer.parseInt(str, 16);
		return a;
	}

	/**
	 * 16进制字符串转int字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String hexStr2IntStr(String str) {
		int a = Integer.parseInt(str, 16);
		return String.valueOf(a);
	}

	/**
	 * 拼F字符串
	 * 
	 * @param len
	 *            长度
	 * @return
	 */
	public static String getFString(int len) {
		String temp = "";
		for (int i = 0; i < len * 2; i++) {
			temp += "F";
		}
		return temp;
	}

	/**
	 * 拼0字符串
	 * 
	 * @param len
	 *            长度
	 * @return
	 */
	public static String getZeroString(int len) {
		String temp = "";
		for (int i = 0; i < len * 2; i++) {
			temp += "0";
		}
		return temp;
	}

	/**
	 * 字符串补零(如长度符合要求，则返回原字串)
	 * 
	 * @param hexStr
	 *            16进制字符串
	 * @param len
	 *            目标长度
	 * @return
	 */
	public static String addZeroString(String hexStr, int len) {
		String temp = "";
		if (hexStr.length() != 2 * len) {
			for (int i = 0; i < 2 * len - hexStr.length(); i++) {
				temp += "0";
			}
			temp = temp + hexStr;

			return temp;
		} else {
			return hexStr;
		}
	}

	/**
	 * 字符串后面补F(如长度符合要求，则返回原字串)
	 * 
	 * @param hexStr
	 *            16进制字符串
	 * @param len
	 *            目标长度
	 * @return
	 */
	public static String formatString(String hexStr, int len) {
		String temp = "";
		if (hexStr.length() != len) {
			for (int i = 0; i < len - hexStr.length(); i++) {
				temp += "F";
				;
			}
			temp = hexStr + temp;

			return temp;
		} else {
			return hexStr;
		}
	}

	/**
	 * 字符串后面补0(如长度符合要求，则返回原字串)
	 * 
	 * @param hexStr
	 *            16进制字符串
	 * @param len
	 *            目标长度
	 * @return
	 */
	public static String formatZeroString(String hexStr, int len) {
		String temp = "";
		if (hexStr.length() != len) {
			for (int i = 0; i < len - hexStr.length(); i++) {
				temp += "0";
				;
			}
			temp = hexStr + temp;

			return temp;
		} else {
			return hexStr;
		}
	}

	/**
	 * char 转 byte
	 * 
	 * @param chars
	 * @return
	 */
	public static byte[] getBytes(char[] chars) {
		Charset cs = Charset.forName("UTF-8");
		CharBuffer cb = CharBuffer.allocate(chars.length);
		cb.put(chars);
		cb.flip();
		ByteBuffer bb = cs.encode(cb);

		return bb.array();
	}

	/**
	 * ASCII转HEX
	 * 
	 * @param str
	 * @return
	 */
	public static String convertStringToHex(String str) {

		char[] chars = str.toCharArray();

		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			hex.append(Integer.toHexString((int) chars[i]));
		}

		return hex.toString();
	}

	/**
	 * HEX转ASCII
	 * 
	 * @param hex
	 * @return
	 */
	public static String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		// StringBuilder temp = new StringBuilder();

		for (int i = 0; i < hex.length() - 1; i += 2) {

			String output = hex.substring(i, (i + 2));

			if (!output.equalsIgnoreCase("FF")) {

				int decimal = Integer.parseInt(output, 16);

				sb.append((char) decimal);

				// temp.append(decimal);
			}
		}

		return sb.toString();
	}

	/**
	 * HEX转汉字国标码
	 * 
	 * @param s
	 * @return
	 */
	public static String hexToStringGBK(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}
		try {
			s = new String(baKeyword, "GBK");
		} catch (Exception e1) {
			e1.printStackTrace();
			return "";
		}
		return s;
	}

	/**
	 * 汉字国标码转HEX
	 * 
	 * @param s
	 * @return
	 */
	public static String gbkToStringHex(String s) {
		String str = "";
		try {
			byte[] b = s.getBytes("GBK");

			for (int i = 0; i < b.length; i++) {
				Integer I = new Integer(b[i]);
				@SuppressWarnings("static-access")
				String strTmp = I.toHexString(b[i]);
				if (strTmp.length() > 2)
					strTmp = strTmp.substring(strTmp.length() - 2);
				str = str + strTmp;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return str;
	}

}
