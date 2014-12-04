package com.chrisplus.nfcsocketexample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.chrisplus.nfcsocket.NFCSocketHelper;
import com.chrisplus.nfcsocket.NFCSocketServer;

public class MainActivity extends Activity {

	private Button startServer;
	private Button stopServer;
	private NFCSocketServer socketServer;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = getApplicationContext();
		startServer = (Button) findViewById(R.id.startserver);
		stopServer = (Button) findViewById(R.id.stopserver);

		startServer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				socketServer = NFCSocketHelper.instanceNFCSocketServer(context);
				socketServer.listen();
			}

		});

		stopServer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(socketServer != null){
					socketServer.close();
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
}
