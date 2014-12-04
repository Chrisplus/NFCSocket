package com.chrisplus.nfcsocket;

import android.content.Context;

/**
 * 
 * This is the helper class of NFCSocket.
 * 
 * @author Shiqi Jiang
 *
 */
public class NFCSocketHelper {

	public static NFCSocketServer instanceNFCSocketServer(Context context) {
		return NFCSocketServer.getInstance(context);
	}

	public static NFCSocketClient instanceNFCSocketClient() {
		return null;
	}
}
