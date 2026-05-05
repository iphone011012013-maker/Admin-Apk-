package com.example.parentalcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("ParentalControl", Context.MODE_PRIVATE);
            if (prefs.getBoolean("setup_complete", false)) {
                
                // إرسال رسالة إعادة تشغيل
                TelegramBot.sendMessage("🔄 <b>تم إعادة تشغيل الجهاز</b>\n\n" +
                                       "📱 الجهاز: " + android.os.Build.MODEL + "\n" +
                                       "⏰ الوقت: " + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()) + "\n" +
                                       "✅ جاري تشغيل الخدمات...");
                
                context.startService(new Intent(context, AppMonitorService.class));
                context.startService(new Intent(context, LocationService.class));
            }
        }
    }
}
