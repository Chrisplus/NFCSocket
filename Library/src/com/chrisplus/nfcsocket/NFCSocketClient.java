package com.chrisplus.nfcsocket;

import java.io.IOException;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

/**
 * This class is Socket Client.
 * 
 * @author Shiqi Jiang
 *
 */
public class NFCSocketClient implements ReaderCallback {

	public static final String TAG = NFCSocketClient.class.getSimpleName();

	private NfcAdapter nfcAdapter;
	private Tag currentTag;
	private IsoDep isoDep;

	public NFCSocketClient(Activity activity) {
		nfcAdapter = NfcAdapter.getDefaultAdapter(activity
				.getApplicationContext());

		if (nfcAdapter != null) {
			nfcAdapter.enableReaderMode(activity, this, Utils.NFC_MODE_FLAGS,
					null);
		}

	}

	@Override
	public void onTagDiscovered(Tag tag) {
		Log.d(TAG, "discover a tag");
		currentTag = tag;
		isoDep = IsoDep.get(tag);
	}

	public void close(Activity activity) {
		if (nfcAdapter != null) {
			nfcAdapter.disableReaderMode(activity);
		}

		currentTag = null;
		isoDep = null;
	}

	public Boolean isAlive() {
		if (nfcAdapter != null) {
			return nfcAdapter.isEnabled();
		} else {
			return false;
		}
	}

	public void connect() {
		if (currentTag != null && isoDep != null) {
			try {
				isoDep.connect();
				isoDep.transceive(Utils.createSelectAidApdu());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public byte[] send(byte[] message) {
		if (currentTag == null) {
			return null;
		}

		if (isoDep == null) {
			return null;
		}

		try {
			isoDep.connect();

			if (isoDep.isConnected()) {
				byte[] response = isoDep.transceive(message);
				return response;
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
