package com.ks.tinysmsgate;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by shadk on 08.11.2016.
 */

public class SMSForwarder extends AsyncTask<Void, Void, String> {
    Context context;
    String from;
    String message;
    String to;
    String method;
    String smsCenter;
    String sim;

    public SMSForwarder(Context context, String from, String message, String to, String method, String smsCenter, String sim) {
        this.context = context;
        this.from = from;
        this.message = message;
        this.to = to;
        this.method = method;
        this.smsCenter = smsCenter;
        this.sim = sim;
    }

    @Override
    protected String doInBackground(Void... params) {
        return sendMessage(method, to, from, message, smsCenter, sim);
    }

    @Override
    protected void onPostExecute(String res) {
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
    }

    public String sendMessage(String method, String to, String from, String message, String smsCenter, String sim) {
        try {
            message = URLEncoder.encode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.v(TinySMSGate.tag, "Gross.");
        }
        String fullData = "phone=" + from + "&message=" + message + "&smscenter=" + smsCenter + "&sim=" + sim;
        if(method.equals("POST")) {
            try {
                URL url = new URL(to);
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("User-agent", "TinySMSGate");
                    connection.setDoOutput(true);
                    DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                    output.writeBytes(fullData);
                    output.flush();
                    output.close();

                    int responseCode = connection.getResponseCode();
                    switch(responseCode) {
                        case 200:
                            return "Tiny SMS Gate forwarded a text.";
                        default:
                            return "Tiny SMS Gate forwarded a text, but it may have failed. Code " + responseCode;
                    }
                } catch (IOException e) {
                    return "Tiny SMS Gate tried to forward an SMS, but could not connect.";
                }
            } catch (MalformedURLException e) {
                return "Tiny SMS Gate tried to forward an SMS, but the URL is malformed.";
            }
        } else {
            try {
                URL url = new URL(to + "?" + fullData);
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-agent", "Tiny SMS Gate");
                    int responseCode = connection.getResponseCode();
                    switch(responseCode) {
                        case 200:
                            return "Tiny SMS Gate forwarded a text.";
                        default:
                            return "Tiny SMS Gate forwarded a text, but it may have failed. Code " + responseCode;
                    }
                } catch (IOException e) {
                    return "Tiny SMS Gate tried to forward an SMS, but could not connect.";
                }
            } catch (MalformedURLException e) {
                return "Tiny SMS Gate tried to forward an SMS, but the URL was malformed.";
            }
        }
    }
}
