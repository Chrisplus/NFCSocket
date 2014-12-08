package com.chrisplus.nfcsocket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.chrisplus.nfcsocket.socketserver.HCEService;
import com.chrisplus.nfcsocket.socketserver.HCEService.HCEServiceListener;

/**
 * This class implements the NFC socket server;
 * 
 * @author Shiqi Jiang
 *
 */
public class NFCSocketServer implements HCEServiceListener {
	public static final String TAG = NFCSocketServer.class.getSimpleName();

	private static NFCSocketServer instance;

	private Context context;
	private Intent intent;
	private NFCSocketServerListener listener;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service != null) {
				((HCEService.HCEBinder) service).getService().setListener(
						NFCSocketServer.this);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	public static NFCSocketServer getInstance(Context context) {
		if (instance == null) {
			instance = new NFCSocketServer(context);
		}
		return instance;
	}

	private NFCSocketServer(Context ctx) {
		context = ctx;
		intent = new Intent(context, HCEService.class);
	}

	public void setListener(NFCSocketServerListener lst) {
		if (lst != null) {
			listener = lst;
		}
	}

	public void listen() {
		Log.d(TAG, "bind to service");
		context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		context.startService(intent);
	}

	public void close() {
		Log.d(TAG, "unbind to service");
		Boolean res = context.stopService(intent);
		context.unbindService(serviceConnection);
		Log.d(TAG, "Stop Service " + res);
	}

	public interface NFCSocketServerListener {

		public void onAccept(byte[] message);

	}

	@Override
	public void onCreateService() {
		Log.d(TAG, "listen: on create");

	}

	@Override
	public void onRebindService(HCEService service) {
		Log.d(TAG, "listen: on rebind");

	}

	@Override
	public void onUnBindService(Intent intent) {
		Log.d(TAG, "listen: on unbind");

	}

	@Override
	public void onStartService() {
		Log.d(TAG, "listen: on start");

	}

	@Override
	public void onDestroyService() {
		Log.d(TAG, "listen: on destroy");

	}

	@Override
	public byte[] onAcceptSelectAidMessage(byte[] selectAidMessage) {
		Log.d(TAG, "listen: on accept aid message");
		return null;
	}

	@Override
	public byte[] onAcceptMessage(byte[] message) {
		Log.d(TAG, "listen: on accept message");
		return null;
	}

	@Override
	public void onLost() {
		Log.d(TAG, "listen: on lost");

	}
}
