package com.android.ble.util;

import java.util.Locale;

public class ByteUtil {
	public static byte[] cancat(byte[] a, byte[] b) {
		int alen = a.length;
		int blen = b.length;
		byte[] result = new byte[alen + blen];
		System.arraycopy(a, 0, result, 0, alen);
		System.arraycopy(b, 0, result, alen, blen);
		return result;
	}

	public static int getInt(byte[] bb, int index) {
		return (bb[(index + 0)] & 0xFF) << 24 | (bb[(index + 1)] & 0xFF) << 16 | (bb[(index + 2)] & 0xFF) << 8
				| (bb[(index + 3)] & 0xFF) << 0;
	}

	public static short getShort(byte[] b, int index) {
		return (short) (b[index] << 8 | b[(index + 1)] & 0xFF);
	}

	public static byte[] intToByte(int number) {
		int temp = number;
		byte[] b = new byte[4];
		for (int i = 0; i < b.length; ++i) {
			b[i] = Integer.valueOf(temp & 0xFF).byteValue();
			temp >>= 8;
		}
		return b;
	}

	public static byte[] shortToByte(short number) {
		int temp = number;
		byte[] b = new byte[2];
		for (int i = b.length - 1; i >= 0; --i) {
			b[i] = Integer.valueOf(temp & 0xFF).byteValue();
			temp >>= 8;
		}
		return b;
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if ((src == null) || (src.length <= 0)) {
			return null;
		}
		for (int i = 0; i < src.length; ++i) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString().toUpperCase(Locale.US);
	}

	public static byte[] hexStringToBytes(String hexString) {
		if ((hexString == null) || (hexString.equals(""))) {
			return null;
		}
		hexString = hexString.toUpperCase(Locale.US);
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; ++i) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[(pos + 1)]));
		}
		return d;
	}

	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";

		for (int n = 0; n < b.length; ++n) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase(Locale.US);
	}

	public static String byteArr2HexStr(byte[] arrB) {
		int iLen = arrB.length;

		StringBuffer sb = new StringBuffer(iLen * 2);
		for (int i = 0; i < iLen; ++i) {
			int intTmp = arrB[i];

			while (intTmp < 0) {
				intTmp += 256;
			}

			if (intTmp < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString().toUpperCase(Locale.US);
	}
}
