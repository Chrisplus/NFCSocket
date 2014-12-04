package com.chrisplus.nfcsocket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.chrisplus.nfcsocket.socketserver.HCEService;

/**
 * This class implements the NFC socket server;
 * 
 * @author Shiqi Jiang
 *
 */
public class NFCSocketServer {
	public static final String TAG = NFCSocketServer.class.getSimpleName();

	private static NFCSocketServer instance;

	private Context context;
	private Intent intent;
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

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

	public void listen() {
		Log.d(TAG, "bind to service");
		context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		ComponentName testName = context.startService(intent);

		if (testName != null) {
			Log.d(TAG, testName.getShortClassName());
		}

	}

	public void close() {
		Log.d(TAG, "unbind to service");
		Boolean res = context.stopService(intent);
		context.unbindService(serviceConnection);
		Log.d(TAG, "Stop Service " + res);
	}
}
