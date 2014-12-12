package com.chrisplus.nfcsocket;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class HCEService extends CustomHostApduService {

	public static final String TAG = HCEService.class.getSimpleName();
	private Messenger serverMessenger;

	@Override
	public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
		if (serverMessenger != null) {
			if (isSelectAidApdu(commandApdu)) {
				sendMessage(NfcServerSocket.MSG_SERVER_SELECT_MESSAGE,
						commandApdu);
			} else {
				sendMessage(NfcServerSocket.MSG_SERVER_NORMAL_MESSAGE,
						commandApdu);
			}
		}

		return null;
	}

	@Override
	public void onDeactivated(int reason) {

	}

	@Override
	public void onRefreshListener(Messenger sMessenger) {
		serverMessenger = sMessenger;
	}

	private boolean isSelectAidApdu(byte[] apdu) {
		return apdu.length >= 2 && apdu[0] == (byte) 0
				&& apdu[1] == (byte) 0xa4;
	}

	private void sendMessage(int what, byte[] data) {
		Message msg = Message.obtain(null, what);
		Bundle bundle = new Bundle();
		bundle.putByteArray(NfcServerSocket.DATA_KEY, data);
		msg.setData(bundle);
		msg.replyTo = mMessenger;
		try {
			serverMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
