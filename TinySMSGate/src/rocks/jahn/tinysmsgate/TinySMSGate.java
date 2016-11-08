package com.ks.tinysmsgate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by shadk on 08.11.2016.
 */

public class TinySMSGate extends Activity {
    public static final String tag = "SMSGate";

    private SharedPreferences preferences;
    private SMSGateService serverService = null;
    private boolean serviceBound = false;
    private TinySMSGate mainContext = this;

    @Override
    protected void onStart() {
        super.onStart();

        startService(new Intent(this, SMSGateService.class));

        Intent serverIntent = new Intent(this, SMSGateService.class);
        bindService(serverIntent, serverConnection, BIND_AUTO_CREATE);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_smsgate_interface);

        Button btnReceiverToggle = (Button) findViewById(R.id.btnReceiverToggle);
        btnReceiverToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(serviceBound) {
                    if(serverService.isAlive()) {
                        serverService.stopServer();
                    } else {
                        serverService.startServer();
                    }
                    updateStatuses();
                }
            }

        });

        Button btnForwarderToggle = (Button) findViewById(R.id.btnForwarderToggle);

        btnForwarderToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean setting = preferences.getBoolean("chkForwardSMS", false);
                preferences.edit().putBoolean("chkForwardSMS", !setting).apply();
                updateStatuses();
            }

        });

        Button btnHelp = (Button) findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gist.github.com/need12648430/205c8288693ead748fed"));
                startActivity(browserIntent);
            }

        });

        Button btnPreferences = (Button) findViewById(R.id.btnPreferences);
        btnPreferences.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent preferences = new Intent("rocks.jahn.smsgate.SMSGatePreferences");
                startActivity(preferences);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.smsgate_interface, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(serviceBound) {
            unbindService(serverConnection);
            serviceBound = false;
        }
    }

    public void updateStatuses() {
        String ip = Utils.getIPAddress(true);
        String page = preferences.getString("txtPage", "POST");

        TextView txtIP = (TextView) findViewById(R.id.txtIP);
        txtIP.setText("IP: " + ip);

        Button btnReceiverToggle = (Button) findViewById(R.id.btnReceiverToggle);
        Button btnForwarderToggle = (Button) findViewById(R.id.btnForwarderToggle);
        Button btnPreferences = (Button) findViewById(R.id.btnPreferences);
        TextView txtReceiving = (TextView) findViewById(R.id.txtReceiving);
        TextView txtForwarding = (TextView) findViewById(R.id.txtForwarding);
        TextView txtPort = (TextView) findViewById(R.id.txtPort);
        TextView txtPage = (TextView) findViewById(R.id.txtPage);

        if (preferences.getBoolean("chkForwardSMS", false)) {
            txtForwarding.setText("Forwarder On");
            txtForwarding.setTextColor(0xFF00CC00);
            btnForwarderToggle.setText("Stop Forwarder");
        } else {
            txtForwarding.setText("Forwarder Off");
            txtForwarding.setTextColor(0xFFFF0000);
            btnForwarderToggle.setText("Start Forwarder");
        }

        if(serverService.isAlive()) {
            txtReceiving.setText("Receiver On");
            btnReceiverToggle.setText("Stop Receiver");
            btnPreferences.setEnabled(false);
            txtReceiving.setTextColor(0xFF00CC00);
            int port = serverService.getPort();
            txtPort.setText("Port: " + port);
            txtPage.setText("Page: " + page);
        } else {
            txtReceiving.setText("Receiver Off");
            btnReceiverToggle.setText("Start Receiver");
            btnPreferences.setEnabled(true);
            txtReceiving.setTextColor(0xFFFF0000);
            txtPort.setText("Port: None");
            txtPage.setText("Page: " + page);
        }
    }

    private ServiceConnection serverConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            SMSGateService.SMSGateServiceBinder serviceBinder = (SMSGateService.SMSGateServiceBinder) binder;
            serverService = serviceBinder.getServerInstance();
            serverService
                    .setPreferences(
                            preferences
                    );
            serverService.setSmsManager(SmsManager.getDefault());
            serverService.setContext(mainContext);
            serviceBound = true;

            updateStatuses();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serverService = null;
            serviceBound = false;
        }

    };
}
