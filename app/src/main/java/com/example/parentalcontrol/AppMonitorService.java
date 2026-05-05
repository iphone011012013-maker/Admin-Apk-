package com.example.parentalcontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppMonitorService extends Service {

    private Handler handler;
    private Runnable monitorRunnable;
    private String lastPackage = "";
    private long lastEnterTime = 0;
    private Map<String, Long> appUsage = new HashMap<>();
    private Map<String, Integer> appOpenCount = new HashMap<>();
    private boolean firstRun = true;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());

        NotificationChannel channel = new NotificationChannel(
            "monitor_channel", "مراقبة البرامج", NotificationManager.IMPORTANCE_LOW
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "monitor_channel")
            .setContentTitle("الرقابة الأبوية")
            .setContentText("جاري مراقبة استخدام البرامج...")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build();

        startForeground(2, notification);

        if (firstRun) {
            TelegramBot.sendMessage("📡 <b>بدء مراقبة البرامج</b>

" +
                                   "⏰ الوقت: " + new SimpleDateFormat("yyyy-MM-dd hh:mm a", new Locale("ar")).format(new Date()));
            firstRun = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startMonitoring();
        return START_STICKY;
    }

    private void startMonitoring() {
        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                checkCurrentApp();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(monitorRunnable);
    }

    private void checkCurrentApp() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();

        UsageEvents events = usm.queryEvents(now - 10000, now);
        UsageEvents.Event event = new UsageEvents.Event();

        String currentPackage = "";
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                currentPackage = event.getPackageName();
            }
        }

        if (!currentPackage.isEmpty() && !currentPackage.equals(lastPackage)) {
            if (!lastPackage.isEmpty() && lastEnterTime > 0) {
                long duration = now - lastEnterTime;
                String appName = getAppName(lastPackage);

                appUsage.put(lastPackage, appUsage.getOrDefault(lastPackage, 0L) + duration);

                String timeFormat = new SimpleDateFormat("hh:mm a", new Locale("ar")).format(new Date(lastEnterTime));
                String durationStr = formatDuration(duration);

                String message = String.format(
                    "📱 <b>تقرير استخدام</b>

" +
                    "👤 الابن خرج من: <b>%s</b>
" +
                    "⏰ دخل الساعة: <b>%s</b>
" +
                    "⏱️ قعد: <b>%s</b>",
                    appName, timeFormat, durationStr
                );

                TelegramBot.sendMessage(message);
            }

            lastPackage = currentPackage;
            lastEnterTime = now;
            appOpenCount.put(currentPackage, appOpenCount.getOrDefault(currentPackage, 0) + 1);

            String appName = getAppName(currentPackage);
            String timeFormat = new SimpleDateFormat("hh:mm a", new Locale("ar")).format(new Date(now));

            String message = String.format(
                "🟢 <b>دخول جديد</b>

" +
                "👤 الابن دخل على: <b>%s</b>
" +
                "⏰ الساعة: <b>%s</b>",
                appName, timeFormat
            );

            TelegramBot.sendMessage(message);
        }
    }

    private String getAppName(String packageName) {
        try {
            return getPackageManager().getApplicationLabel(
                getPackageManager().getApplicationInfo(packageName, 0)
            ).toString();
        } catch (Exception e) {
            return packageName;
        }
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%d ساعة و %d دقيقة و %d ثانية", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%d دقيقة و %d ثانية", minutes, seconds % 60);
        } else {
            return String.format("%d ثانية", seconds);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (handler != null && monitorRunnable != null) {
            handler.removeCallbacks(monitorRunnable);
        }
        TelegramBot.sendMessage("🛑 <b>تم إيقاف مراقبة البرامج!</b>");
        super.onDestroy();
    }
}