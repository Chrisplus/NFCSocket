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
                        String tmp = getMessage();
                        appendMessage(false, tmp);
                        byte[] response = NfcClientSocket.getInstance(
                                getApplicationContext()).send(
                                tmp.getBytes());
                        showLog(response);
                        appendMessage(true, new String(response));
                    }
                } else {
                    String tmp = getMessage();
                    appendMessage(false, tmp);
                    byte[] response = NfcClientSocket.getInstance(
                            getApplicationContext()).send(tmp.getBytes());
                    showLog(response);
                    appendMessage(true, new String(response));
                }

                // NfcClientSocket.getInstance(getApplicationContext()).close();
            }

        });
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

    private NfcServerSocket.NfcServerSocketListener serverListener
            = new NfcServerSocket.NfcServerSocketListener() {

        @Override
        public byte[] onSelectMessage(byte[] message) {
            Log.d("BTR", "selectMessage");
            appendMessage(true, new String(message));
            appendMessage(false, "welcome");
            return "welcome".getBytes();
        }

        @Override
        public byte[] onMessage(byte[] message) {
            Log.d("BTR", "normalMessage");
            appendMessage(true, new String(message));
            appendMessage(false, "I know");
            return "I know".getBytes();
        }

    };

    private NfcClientSocket.NfcClientSocketListener clientListener
            = new NfcClientSocket.NfcClientSocketListener() {

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

    private void appendMessage(boolean isReceive, String message) {
        if (console != null) {
            String tmp = "";
            if (isReceive) {
                tmp += "Receive: ";
            } else {
                tmp += "Send: ";
            }

            tmp = tmp + message + "\n";
            console.append(tmp);
        }
    }

    private String getMessage() {
        if (message != null && message.getText().length() > 0) {
            return message.getText().toString();
        } else {
            return "Hello";
        }
    }

}
