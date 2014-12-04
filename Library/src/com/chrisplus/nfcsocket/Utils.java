package com.chrisplus.nfcsocket;

/**
 * This class records the default constants.
 * 
 * @author Shiqi Jiang
 *
 */
public class Utils {

	public static final byte[] CLA_INS_P1_P2 = { 0x00, (byte) 0xA4, 0x04, 0x00 };
	public static final byte[] AID_ANDROID = { (byte) 0xF0, 0x01, 0x02, 0x03,
			0x04, 0x05, 0x06 };

	public static byte[] createSelectAidApdu(byte[] aid) {
		byte[] result = new byte[6 + aid.length];
		System.arraycopy(CLA_INS_P1_P2, 0, result, 0, CLA_INS_P1_P2.length);
		result[4] = (byte) aid.length;
		System.arraycopy(aid, 0, result, 5, aid.length);
		result[result.length - 1] = 0;
		return result;
	}
	
}
