package com.chrisplus.nfcsocket;

import android.content.Intent;
import android.nfc.cardemulation.MyHostApduService;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * This class is Host card Emulator
 * 
 * @author Shiqi Jiang
 *
 */
public class HCEService extends MyHostApduService {

	public static final String TAG = HCEService.class.getSimpleName();
	private HCEServiceListener listener;
	private HCEBinder hceBinder = new HCEBinder();

	@Override
	public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
		if (selectAidApdu(commandApdu)) {
			/* receive select command */
			if (listener != null) {
				return listener.onAcceptSelectAidMessage(commandApdu);
			}
		} else {
			if (listener != null) {
				return listener.onAcceptMessage(commandApdu);
			}
		}

		return null;
	}

	@Override
	public void onDeactivated(int reason) {

		/*
		 * Two reasons
		 * 
		 * HCEService.DEACTIVATION_DESELECTED;
		 * HCEService.DEACTIVATION_LINK_LOSS;
		 */

		if (listener != null) {
			listener.onLost();
		}
	}

	public void setListener(HCEServiceListener lst) {
		Log.d(TAG, "set listener");

		if (lst != null) {
			listener = lst;
		}
	}

	private boolean selectAidApdu(byte[] apdu) {
		return apdu.length >= 2 && apdu[0] == (byte) 0
				&& apdu[1] == (byte) 0xa4;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (listener != null) {
			listener.onStartService();
		}

		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		if (listener != null) {
			listener.onCreateService();
		}
		super.onCreate();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (listener != null) {
			listener.onUnBindService(intent);
		}
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		if (listener != null) {
			listener.onRebindService(this);
		}
		super.onRebind(intent);
	}

	@Override
	public void onDestroy() {
		if (listener != null) {
			listener.onDestroyService();
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return hceBinder;
	}

	/**
	 * 
	 * This interface is used to update information to upper
	 * {@link NFCSocketServer}
	 * 
	 * @author Shiqi Jiang
	 *
	 */
	public interface HCEServiceListener {

		public void onCreateService();

		public void onRebindService(HCEService service);

		public void onUnBindService(Intent intent);

		public void onStartService();

		public void onDestroyService();

		public byte[] onAcceptSelectAidMessage(byte[] selectAidMessage);

		public byte[] onAcceptMessage(byte[] message);

		public void onLost();

	}

	public class HCEBinder extends Binder {

		public HCEService getService() {
			return HCEService.this;
		}
	}

}
