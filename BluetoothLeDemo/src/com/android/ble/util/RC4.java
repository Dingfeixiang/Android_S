package com.android.ble.util;

import android.util.Log;

public class RC4 {
	public static String decry_RC4(byte[] data, String key) {
		if ((data == null) || (key == null)) {
			return null;
		}
		return asString(RC4Base(data, key));
	}

	public static String decry_RC4(String data, String key) {
		if ((data == null) || (key == null)) {
			return null;
		}
		return new String(RC4Base(HexString2Bytes(data), key));
	}

	public static byte[] encry_RC4_byte(String data, String key) {
		if ((data == null) || (key == null)) {
			return null;
		}
		byte[] b_data = data.getBytes();

		return RC4Base(b_data, key);
	}

	public static String encry_RC4_string(String data, String key) {
		if ((data == null) || (key == null)) {
			return null;
		}
		return toHexString(asString(encry_RC4_byte(data, key)));
	}

	private static String asString(byte[] buf) {
		StringBuffer strbuf = new StringBuffer(buf.length);
		for (int i = 0; i < buf.length; ++i) {
			strbuf.append((char) buf[i]);
		}
		return strbuf.toString();
	}

	private static byte[] initKey(String aKey) {
		byte[] b_key = ByteUtil.hexStringToBytes(aKey);
		byte[] state = new byte[256];

		for (int i = 0; i < 256; ++i) {
			state[i] = (byte) i;
		}
		int index1 = 0;
		int index2 = 0;
		if ((b_key == null) || (b_key.length == 0)) {
			return null;
		}
		for (int i = 0; i < 256; ++i) {
			index2 = (b_key[index1] & 0xFF) + (state[i] & 0xFF) + index2 & 0xFF;
			byte tmp = state[i];
			state[i] = state[index2];
			state[index2] = tmp;
			index1 = (index1 + 1) % b_key.length;
		}
		return state;
	}

	private static String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); ++i) {
			int ch = s.charAt(i);
			String s4 = Integer.toHexString(ch & 0xFF);
			if (s4.length() == 1) {
				s4 = '0' + s4;
			}
			str = str + s4;
		}
		return str;
	}

	private static byte[] HexString2Bytes(String src) {
		int size = src.length();
		byte[] ret = new byte[size / 2];
		byte[] tmp = src.getBytes();
		for (int i = 0; i < size / 2; ++i) {
			ret[i] = uniteBytes(tmp[(i * 2)], tmp[(i * 2 + 1)]);
		}
		return ret;
	}

	private static byte uniteBytes(byte src0, byte src1) {
		char _b0 = (char) Byte.decode("0x" + new String(new byte[] { src0 })).byteValue();
		_b0 = (char) (_b0 << '\004');
		char _b1 = (char) Byte.decode("0x" + new String(new byte[] { src1 })).byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	public static byte[] RC4Base(byte[] input, String mKkey) {
		byte[] key = initKey(mKkey);

		CommonData.RC4_x = 0;
		CommonData.RC4_y = 0;

		byte[] result = new byte[input.length];

		for (int i = 0; i < input.length; ++i) {
			CommonData.RC4_x = CommonData.RC4_x + 1 & 0xFF;
			CommonData.RC4_y = (key[CommonData.RC4_x] & 0xFF) + CommonData.RC4_y & 0xFF;
			byte tmp = key[CommonData.RC4_x];
			key[CommonData.RC4_x] = key[CommonData.RC4_y];
			key[CommonData.RC4_y] = tmp;
			int xorIndex = (key[CommonData.RC4_x] & 0xFF) + (key[CommonData.RC4_y] & 0xFF) & 0xFF;
			result[i] = (byte) (input[i] ^ key[xorIndex]);
		}
		return result;
	}

	public static byte[] rc4_crypt(byte[] Data, String mdkey) {
		int i = 0;
		int j = 0;
		int t = 0;
		int k = 0;

		byte[] key = ByteUtil.hexStringToBytes(mdkey);

		byte[] s = rc4_init(key, key.length);

		Log.e("S-BOX", ByteUtil.bytesToHexString(s));

		for (k = 0; k < key.length; ++k) {
			i = (i + 1) % 256;
			j = (j + s[i]) % 256;
			byte tmp = s[i];
			s[i] = s[j];
			s[j] = tmp;
			t = (s[i] + s[j]) % 256;
			int tmp100_98 = k;
			Data[tmp100_98] = (byte) (Data[tmp100_98] ^ s[t]);
		}

		return Data;
	}

	static byte[] rc4_init(byte[] key, int Len) {
		byte[] s = new byte[256];

		int i = 0;
		int j = 0;

		byte[] k = new byte[256];

		byte tmp = 0;

		for (i = 0; i < 256; ++i) {
			s[i] = (byte) i;
			k[i] = key[(i % Len)];
			Log.e("len", String.valueOf(k[i]));
		}

		for (i = 0; i < 256; ++i) {
			j = (j + s[i] + k[i]) % 256;
			tmp = s[i];
			s[i] = s[j];
			s[j] = tmp;
		}
		return s;
	}
}
