package com.example.parentalcontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationService extends Service implements LocationListener {
    
    private static final String TAG = "LocationService";
    private static final long UPDATE_INTERVAL = 60 * 60 * 1000;
    
    private LocationManager locationManager;
    private Handler handler;
    private Runnable locationRunnable;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        NotificationChannel channel = new NotificationChannel(
            "location_channel", "تتبع الموقع", NotificationManager.IMPORTANCE_LOW
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        
        Notification notification = new Notification.Builder(this, "location_channel")
            .setContentTitle("تتبع الموقع")
            .setContentText("جاري إرسال الموقع كل ساعة...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build();
        
        startForeground(3, notification);
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        
        // إرسال رسالة بدء تتبع الموقع
        TelegramBot.sendMessage("📍 <b>بدء تتبع الموقع</b>\n\n" +
                               "⏰ الوقت: " + new SimpleDateFormat("yyyy-MM-dd hh:mm a", new Locale("ar")).format(new Date()) + "\n" +
                               "🔄 سيتم الإرسال كل ساعة");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        return START_STICKY;
    }
    
    private void startLocationUpdates() {
        requestSingleLocation();
        
        locationRunnable = new Runnable() {
            @Override
            public void run() {
                requestSingleLocation();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
        handler.postDelayed(locationRunnable, UPDATE_INTERVAL);
    }
    
    private void requestSingleLocation() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
            } else {
                TelegramBot.sendMessage("⚠️ تنبيه: GPS مقفول على جهاز الابن!");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied: " + e.getMessage());
        }
    }
    
    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();
        
        String message = String.format(
            "📍 <b>موقع الابن</b>\n\n" +
            "🌍 خط العرض: <code>%.6f</code>\n" +
            "🌍 خط الطول: <code>%.6f</code>\n" +
            "🎯 الدقة: %.1f متر\n" +
            "⏰ الوقت: %s",
            latitude, longitude, accuracy,
            new SimpleDateFormat("yyyy-MM-dd hh:mm a", new Locale("ar")).format(new Date())
        );
        
        TelegramBot.sendMessage(message);
        TelegramBot.sendLocation(latitude, longitude);
    }
    
    @Override
    public void onProviderEnabled(String provider) {}
    
    @Override
    public void onProviderDisabled(String provider) {}
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        if (handler != null && locationRunnable != null) {
            handler.removeCallbacks(locationRunnable);
        }
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        TelegramBot.sendMessage("🛑 <b>تم إيقاف تتبع الموقع!</b>");
        super.onDestroy();
    }
}
