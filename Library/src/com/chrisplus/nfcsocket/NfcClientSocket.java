package com.chrisplus.nfcsocket;

import java.io.IOException;
import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

public class NfcClientSocket implements ReaderCallback {

	public static final String TAG = NfcClientSocket.class.getSimpleName();
	public static final int NFC_MODE_FLAGS = NfcAdapter.FLAG_READER_NFC_A
			| NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

	/* Status Code of Connecting */
	public static final int CONNECT_SUCCESS = 100;
	public static final int CONNECT_FAIL_NO_TAG = -101;
	public static final int CONNECT_FAIL_NO_TRANCEIVER = -102;
	public static final int CONNECT_FAIL_NO_RESPONSE = -103;
	public static final int CONNECT_FAIL_IO_ERROR = -104;
	public static final int CONNECT_FAIL_WRONG_INFO = -105;
	public static final int CONNECT_FAIL_UNKNOWN = -199;

	private static NfcClientSocket instance;

	private NfcAdapter nfcAdapter;
	private Tag currentTag;
	private IsoDep isoDep;
	private HashSet<NfcClientSocketListener> listenerSet;

	public static NfcClientSocket getInstance(Context context) {
		if (instance == null) {
			instance = new NfcClientSocket(context);
		}

		return instance;
	}

	private NfcClientSocket(Context context) {
		nfcAdapter = NfcAdapter.getDefaultAdapter(context);
		listenerSet = new HashSet<NfcClientSocketListener>();
		currentTag = null;
		isoDep = null;
	}

	public synchronized void register(NfcClientSocketListener ls) {
		if (ls != null) {
			if (!listenerSet.contains(ls)) {
				listenerSet.add(ls);
				enableNfcReaderMode(ls.getCurrentActivity());
			}
		}

	}

	public synchronized void unregister(NfcClientSocketListener ls) {
		if (ls != null) {
			if (!listenerSet.contains(ls)) {
				listenerSet.remove(ls);
				disableNfcReaderMode(ls.getCurrentActivity());
			}
		}
	}

	public int getClientNum() {
		if (listenerSet != null) {
			return listenerSet.size();
		} else {
			return -1;
		}
	}

	public void setTimeout(int millisecond) {

	}

	public int connect() {

		if (currentTag == null) {
			return CONNECT_FAIL_NO_TAG;
		}

		if (isoDep == null) {
			return CONNECT_FAIL_NO_TRANCEIVER;
		}

		try {
			isoDep.connect();

			byte[] response = isoDep.transceive(Utils.createSelectAidApdu());

			if (checkConnectResponse(response)) {
				return CONNECT_SUCCESS;
			} else {
				return CONNECT_FAIL_WRONG_INFO;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return CONNECT_FAIL_IO_ERROR;
		}

	}

	public byte[] send(byte[] message) {
		if (currentTag == null) {
			return null;
		}

		if (isoDep == null) {
			isoDep = IsoDep.get(currentTag);
		}

		if (isoDep.isConnected()) {
			try {
				byte[] response = isoDep.transceive(message);
				return response;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public void close() {
		currentTag = null;
		if (isoDep != null) {
			try {
				isoDep.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			isoDep = null;
		}
	}

	@Override
	public void onTagDiscovered(Tag tag) {
		currentTag = tag;
		isoDep = IsoDep.get(currentTag);

		if (listenerSet != null) {
			for (NfcClientSocketListener listener : listenerSet) {
				if (listener != null) {
					listener.onDiscoveryTag();
				}
			}
		}
	}

	public interface NfcClientSocketListener {
		public Activity getCurrentActivity();

		public void onDiscoveryTag();

	}

	private void enableNfcReaderMode(Activity activity) {
		if (nfcAdapter != null) {
			nfcAdapter.enableReaderMode(activity, this, NFC_MODE_FLAGS, null);
		}
	}

	private void disableNfcReaderMode(Activity activity) {
		if (nfcAdapter != null) {
			nfcAdapter.disableReaderMode(activity);
		}
	}

	private boolean checkConnectResponse(byte[] data) {
		return true;
	}

}
