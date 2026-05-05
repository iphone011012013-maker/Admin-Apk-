package com.example.parentalcontrol;

import android.content.Context;
import android.util.Log;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelegramBot {

    private static final String BOT_TOKEN = "8519648833:AAHeg8gNX7P1UZabWKcqeFJv0NAggRzS3Qs";
    private static final String ADMIN_ID = "1431886140";
    private static final String API_URL = "https://api.telegram.org/bot" + BOT_TOKEN;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static void sendMessage(String message) {
        executor.execute(() -> {
            try {
                String urlString = API_URL + "/sendMessage";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String params = "chat_id=" + ADMIN_ID + 
                               "&text=" + URLEncoder.encode(message, "UTF-8") +
                               "&parse_mode=HTML";

                OutputStream os = conn.getOutputStream();
                os.write(params.getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    Log.e("TelegramBot", "Error: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("TelegramBot", "Send failed: " + e.getMessage());
            }
        });
    }

    public static void sendLocation(double latitude, double longitude) {
        executor.execute(() -> {
            try {
                String urlString = API_URL + "/sendLocation";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String params = "chat_id=" + ADMIN_ID + 
                               "&latitude=" + latitude +
                               "&longitude=" + longitude;

                OutputStream os = conn.getOutputStream();
                os.write(params.getBytes("UTF-8"));
                os.close();

                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                Log.e("TelegramBot", "Location send failed: " + e.getMessage());
            }
        });
    }
}