package com.chrisplus.nfcsocket.socketserver;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

/**
 * This class is Host card Emulator
 * 
 * @author Shiqi Jiang
 *
 */
public class HCEService extends HostApduService {

	@Override
	public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
		return null;
	}

	@Override
	public void onDeactivated(int reason) {

	}

	private boolean selectAidApdu(byte[] apdu) {
		return apdu.length >= 2 && apdu[0] == (byte) 0
				&& apdu[1] == (byte) 0xa4;
	}


}
