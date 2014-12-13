package com.chrisplus.nfcsocket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * This class is used to create and operate NFC server.
 * 
 * @author Shiqi Jiang
 *
 */
public class NfcServerSocket {

	public static final String TAG = NfcServerSocket.class.getSimpleName();
	public static final int MSG_SERVER_SELECT_MESSAGE = 1;
	public static final int MSG_SERVER_NORMAL_MESSAGE = 2;
	public static final int MSG_SERVER_DEACTIVE = 3;
	public static final String DATA_KEY = "key";

	private static NfcServerSocket instance;
	private NfcServerSocketListener listener;
	private Context context;
	private Messenger coreNfcMessenger;
	private final Messenger localMessenger = new Messenger(new MsgHandler());
	private Intent intent;
	private final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			log("hce service connected & send localMessenger");
			coreNfcMessenger = new Messenger(service);
			Message msg = Message.obtain(null, HCEService.MSG_REFRESH_SERVER);
			msg.replyTo = localMessenger;
			try {
				coreNfcMessenger.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			log("hce service disconnected");
			if (coreNfcMessenger != null) {
				log("send null messenger");
				Message msg = Message.obtain(null,
						HCEService.MSG_REFRESH_SERVER);
				msg.replyTo = null;
				try {
					coreNfcMessenger.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}

		}
	};

	/**
	 * Get an instance of NfcServerSocket
	 * 
	 * @param context
	 *            The application context.
	 * @return The instance of NfcServerSocket
	 */
	public static NfcServerSocket getInstance(Context context) {
		if (instance == null) {
			instance = new NfcServerSocket(context);
		}
		return instance;
	}

	private NfcServerSocket(Context ctx) {
		context = ctx;
		intent = new Intent(context, HCEService.class);
	}

	/**
	 * Start the Nfc service and listening the coming reader message.
	 */
	public void listen() {
		if (listener != null) {
			log("start listen");
			context.bindService(intent, serviceConnection,
					Context.BIND_AUTO_CREATE);
			context.startService(intent);
		}
	}

	/**
	 * Stop the Nfc service.
	 */
	public void close() {

		context.unbindService(serviceConnection);
		if (context.stopService(intent)) {
			log("stop nfc service");
		}
	}

	/**
	 * Set a listener which can get callback when the Nfc server gets messages.
	 * 
	 * @param lst
	 *            A {NfcServerSocketListener} used to get the callback when
	 *            messages come.
	 */
	public void setListener(NfcServerSocketListener lst) {
		if (lst != null) {
			log("set listener");
			listener = lst;
		}
	}

	/**
	 * The interface used to get callback when message comes.
	 * 
	 * @author Shiqi Jiang
	 *
	 */
	public interface NfcServerSocketListener {
		/**
		 * The callback when the select command is received. The connection will
		 * NOT be created if null is returned.
		 * 
		 * @param message
		 *            The select message containing AID generally.
		 * @return You should not return null if you confirm to create a
		 *         connection with client side.
		 */
		public byte[] onSelectMessage(byte[] message);

		public byte[] onMessage(byte[] message);

	}

	private class MsgHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SERVER_SELECT_MESSAGE:
				if (listener != null) {
					byte[] response = listener.onSelectMessage(msg.getData()
							.getByteArray(DATA_KEY));
					sendResponse(response);
				}
				break;
			case MSG_SERVER_NORMAL_MESSAGE:
				if (listener != null) {
					byte[] response = listener.onMessage(msg.getData()
							.getByteArray(DATA_KEY));
					sendResponse(response);
				}
				break;
			default:
				super.handleMessage(msg);
			}

		}
	}

	private void sendResponse(byte[] response) {
		if (coreNfcMessenger != null) {
			Message message = Message
					.obtain(null, HCEService.MSG_RESPONSE_APDU);
			Bundle dataBundle = new Bundle();
			dataBundle.putByteArray(HCEService.KEY_DATA, response);
			message.setData(dataBundle);
			try {
				coreNfcMessenger.send(message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}

	private void log(String logMessage) {
		Log.d(TAG, logMessage);
	}

}
