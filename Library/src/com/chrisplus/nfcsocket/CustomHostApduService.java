package com.chrisplus.nfcsocket;

import android.app.Service;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * This class is modified from {@link HostApduService}
 * 
 * <li>remove final from onBind method. <li>add refresh message and its
 * corresponding handle function. <li>add callback onRefreshListener
 * 
 * @author Shiqi Jiang
 *
 */
public abstract class CustomHostApduService extends Service {
	/**
	 * The {@link Intent} action that must be declared as handled by the
	 * service.
	 */
	public static final String SERVICE_INTERFACE = "android.nfc.cardemulation.action.HOST_APDU_SERVICE";

	/**
	 * The name of the meta-data element that contains more information about
	 * this service.
	 */
	public static final String SERVICE_META_DATA = "android.nfc.cardemulation.my_host_apdu_service";

	/**
	 * Reason for {@link #onDeactivated(int)}. Indicates deactivation was due to
	 * the NFC link being lost.
	 */
	public static final int DEACTIVATION_LINK_LOSS = 0;

	/**
	 * Reason for {@link #onDeactivated(int)}.
	 *
	 * <p>
	 * Indicates deactivation was due to a different AID being selected (which
	 * implicitly deselects the AID currently active on the logical channel).
	 *
	 * <p>
	 * Note that this next AID may still be resolved to this service, in which
	 * case {@link #processCommandApdu(byte[], Bundle)} will be called again.
	 */
	public static final int DEACTIVATION_DESELECTED = 1;

	static final String TAG = "ApduService";

	/**
	 * MSG_COMMAND_APDU is sent by NfcService when a 7816-4 command APDU has
	 * been received.
	 *
	 */
	public static final int MSG_COMMAND_APDU = 0;

	/**
	 * MSG_RESPONSE_APDU is sent to NfcService to send a response APDU back to
	 * the remote device.
	 *
	 */
	public static final int MSG_RESPONSE_APDU = 1;

	/**
	 * MSG_DEACTIVATED is sent by NfcService when the current session is
	 * finished; either because another AID was selected that resolved to
	 * another service, or because the NFC link was deactivated.
	 *
	 */
	public static final int MSG_DEACTIVATED = 2;

	/**
	 * MSG_DEACTIVATED is the unhandled message
	 */
	public static final int MSG_UNHANDLED = 3;

	/**
	 * MSG_REFRESH_SERVER is sent by NfcServerSocket when user start listening.
	 * The messenger of NfcServerSocket will be set in replyto attribute.
	 */
	public static final int MSG_REFRESH_SERVER = 4;

	/**
	 * KEY_DATA is the key of response data in bundle.
	 */
	public static final String KEY_DATA = "data";

	/**
	 * Messenger interface to NfcService for sending responses. Only accessed on
	 * main thread by the message handler.
	 *
	 */
	Messenger mNfcService = null;

	final Messenger mMessenger = new Messenger(new MsgHandler());

	final class MsgHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_COMMAND_APDU:
				Bundle dataBundle = msg.getData();
				if (dataBundle == null) {
					return;
				}
				if (mNfcService == null)
					mNfcService = msg.replyTo;

				byte[] apdu = dataBundle.getByteArray(KEY_DATA);
				if (apdu != null) {
					byte[] responseApdu = processCommandApdu(apdu, null);
					if (responseApdu != null) {
						if (mNfcService == null) {
							Log.e(TAG,
									"Response not sent; service was deactivated.");
							return;
						}
						Message responseMsg = Message.obtain(null,
								MSG_RESPONSE_APDU);
						Bundle responseBundle = new Bundle();
						responseBundle.putByteArray(KEY_DATA, responseApdu);
						responseMsg.setData(responseBundle);
						responseMsg.replyTo = mMessenger;
						try {
							mNfcService.send(responseMsg);
						} catch (RemoteException e) {
							Log.e("TAG",
									"Response not sent; RemoteException calling into "
											+ "NfcService.");
						}
					}
				} else {
					Log.e(TAG, "Received MSG_COMMAND_APDU without data.");
				}
				break;
			case MSG_RESPONSE_APDU:
				if (mNfcService == null) {
					Log.e(TAG, "Response not sent; service was deactivated.");
					return;
				}
				try {
					msg.replyTo = mMessenger;
					mNfcService.send(msg);
				} catch (RemoteException e) {
					Log.e(TAG, "RemoteException calling into NfcService.");
				}
				break;
			case MSG_DEACTIVATED:
				// Make sure we won't call into NfcService again
				mNfcService = null;
				onDeactivated(msg.arg1);
				break;
			case MSG_UNHANDLED:
				if (mNfcService == null) {
					Log.e(TAG,
							"notifyUnhandled not sent; service was deactivated.");
					return;
				}
				try {
					msg.replyTo = mMessenger;
					mNfcService.send(msg);
				} catch (RemoteException e) {
					Log.e(TAG, "RemoteException calling into NfcService.");
				}
				break;
			/*
			 * handle upper server messenger refresh. null will be set if server
			 * will be close.
			 */
			case MSG_REFRESH_SERVER:
				onRefreshListener(msg.replyTo);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	/**
	 * Sends a response APDU back to the remote device.
	 *
	 * <p>
	 * Note: this method may be called from any thread and will not block.
	 * 
	 * @param responseApdu
	 *            A byte-array containing the reponse APDU.
	 */
	public final void sendResponseApdu(byte[] responseApdu) {
		Message responseMsg = Message.obtain(null, MSG_RESPONSE_APDU);
		Bundle dataBundle = new Bundle();
		dataBundle.putByteArray(KEY_DATA, responseApdu);
		responseMsg.setData(dataBundle);
		try {
			mMessenger.send(responseMsg);
		} catch (RemoteException e) {
			Log.e("TAG", "Local messenger has died.");
		}
	}

	/**
	 * Calling this method allows the service to tell the OS that it won't be
	 * able to complete this transaction - for example, because it requires data
	 * connectivity that is not present at that moment.
	 *
	 * The OS may use this indication to give the user a list of alternative
	 * applications that can handle the last AID that was selected. If the user
	 * would select an application from the list, that action by itself will not
	 * cause the default to be changed; the selected application will be invoked
	 * for the next tap only.
	 *
	 * If there are no other applications that can handle this transaction, the
	 * OS will show an error dialog indicating your service could not complete
	 * the transaction.
	 *
	 * <p>
	 * Note: this method may be called anywhere between the first
	 * {@link #processCommandApdu(byte[], Bundle)} call and a
	 * {@link #onDeactivated(int)} call.
	 */
	public final void notifyUnhandled() {
		Message unhandledMsg = Message.obtain(null, MSG_UNHANDLED);
		try {
			mMessenger.send(unhandledMsg);
		} catch (RemoteException e) {
			Log.e("TAG", "Local messenger has died.");
		}
	}

	/**
	 * <p>
	 * This method will be called when a command APDU has been received from a
	 * remote device. A response APDU can be provided directly by returning a
	 * byte-array in this method. Note that in general response APDUs must be
	 * sent as quickly as possible, given the fact that the user is likely
	 * holding his device over an NFC reader when this method is called.
	 *
	 * <p class="note">
	 * If there are multiple services that have registered for the same AIDs in
	 * their meta-data entry, you will only get called if the user has
	 * explicitly selected your service, either as a default or just for the
	 * next tap.
	 *
	 * <p class="note">
	 * This method is running on the main thread of your application. If you
	 * cannot return a response APDU immediately, return null and use the
	 * {@link #sendResponseApdu(byte[])} method later.
	 *
	 * @param commandApdu
	 *            The APDU that was received from the remote device
	 * @param extras
	 *            A bundle containing extra data. May be null.
	 * @return a byte-array containing the response APDU, or null if no response
	 *         APDU can be sent at this point.
	 */
	public abstract byte[] processCommandApdu(byte[] commandApdu, Bundle extras);

	/**
	 * This method will be called in two possible scenarios: <li>The NFC link
	 * has been deactivated or lost <li>A different AID has been selected and
	 * was resolved to a different service component
	 * 
	 * @param reason
	 *            Either {@link #DEACTIVATION_LINK_LOSS} or
	 *            {@link #DEACTIVATION_DESELECTED}
	 */
	public abstract void onDeactivated(int reason);

	/**
	 * This method will be called in two scenarios: <li>When
	 * {@link NfcServerSocket} bind with {@link HCEService}, its local messenger
	 * will be sent. <li> {@link NfcServerSocket} unbind with {@link HCEService},
	 * null will be sent.
	 * 
	 * @param serverMessenger
	 *            Either local messenger of {@link NfcServerSocket} or null
	 */
	public abstract void onRefreshListener(Messenger serverMessenger);
}
