package com.chrisplus.nfcsocketexample;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chrisplus.nfcsocket.NFCClientSocket;
import com.chrisplus.nfcsocket.NFCSocketHelper;
import com.chrisplus.nfcsocket.NFCSocketServer;

public class MainActivity extends Activity implements
		NFCSocketServer.NFCSocketServerListener {

	private Button startServer;
	private Button stopServer;
	private NFCSocketServer socketServer;
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
				NFCClientSocket.getInstance(getApplicationContext())
						.unregister(MainActivity.this);
				socketServer = NFCSocketHelper.instanceNFCSocketServer(context);
				socketServer.listen(MainActivity.this);

			}

		});

		stopServer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (socketServer != null) {
					socketServer.close();
				}
			}

		});

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String message = "Hello, world";
				console.append("Send: " + message + "\n");
				String response = NFCClientSocket.getInstance(
						getApplicationContext()).send(message.getBytes());

				if (response != "") {
					console.append("Receive: " + response + "\n");
				} else {
					console.append("Receive: " + response + "\n");
				}

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
	public byte[] onAccept(byte[] message) {

		if (message != null) {
			try {
				console.append("Receive: " + new String(message, "UTF-8")
						+ "\n");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			console.append("Receive: " + message + "\n");
		}

		return "Got it".getBytes();
	}

	@Override
	protected void onResume() {
		NFCClientSocket.getInstance(getApplicationContext()).register(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		NFCClientSocket.getInstance(getApplicationContext()).unregister(this);
		super.onPause();
	}

}
