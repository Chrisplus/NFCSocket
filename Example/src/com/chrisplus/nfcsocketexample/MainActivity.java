package com.chrisplus.nfcsocketexample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chrisplus.nfcsocket.NfcClientSocket;
import com.chrisplus.nfcsocket.NfcServerSocket;

public class MainActivity extends Activity {

	private Button startServer;
	private Button stopServer;
	private Button send;
	private TextView console;
	private EditText message;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		context = getApplicationContext();
		startServer = (Button) findViewById(R.id.startserver);
		stopServer = (Button) findViewById(R.id.stopserver);

		send = (Button) findViewById(R.id.send);
		console = (TextView) findViewById(R.id.text);
		message = (EditText) findViewById(R.id.message);

		startServer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				NfcClientSocket.getInstance(getApplicationContext())
						.unregister(clientListener);
				NfcServerSocket.getInstance(getApplicationContext())
						.setListener(serverListener);
				NfcServerSocket.getInstance(getApplicationContext()).listen();

			}

		});

		stopServer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				NfcClientSocket.getInstance(getApplicationContext()).register(
						clientListener);
				NfcServerSocket.getInstance(getApplicationContext()).close();
			}

		});

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!NfcClientSocket.getInstance(getApplicationContext())
						.isConnected()) {
					int i = NfcClientSocket
							.getInstance(getApplicationContext()).connect();
					Log.d("BTR", i + "");

					if (i == NfcClientSocket.CONNECT_SUCCESS) {
						byte[] response = NfcClientSocket.getInstance(
								getApplicationContext()).send(
								"Hello".getBytes());
						showLog(response);
					}
				} else {
					byte[] response = NfcClientSocket.getInstance(
							getApplicationContext()).send("Hello".getBytes());
					showLog(response);
				}

				// NfcClientSocket.getInstance(getApplicationContext()).close();
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		NfcClientSocket.getInstance(getApplicationContext()).register(
				clientListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		NfcClientSocket.getInstance(getApplicationContext()).unregister(
				clientListener);
	}

	private NfcServerSocket.NfcServerSocketListener serverListener = new NfcServerSocket.NfcServerSocketListener() {

		@Override
		public byte[] onSelectMessage(byte[] message) {
			Log.d("BTR", "selectMessage");
			return "welcome".getBytes();
		}

		@Override
		public byte[] onMessage(byte[] message) {
			Log.d("BTR", "normalMessage");
			return "I know".getBytes();
		}

	};

	private NfcClientSocket.NfcClientSocketListener clientListener = new NfcClientSocket.NfcClientSocketListener() {

		@Override
		public void onDiscoveryTag() {
			Log.d("BTR", "tag!");
		}

		@Override
		public Activity getCurrentActivity() {
			return MainActivity.this;
		}
	};

	private void showLog(byte[] res) {
		if (res != null) {
			Log.d("BTR", new String(res));
		}

	}

}
