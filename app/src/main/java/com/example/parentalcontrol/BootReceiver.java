package com.example.parentalcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.text.DateFormat;
import java.util.Date;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("ParentalControl", Context.MODE_PRIVATE);
            if (prefs.getBoolean("setup_complete", false)) {

                TelegramBot.sendMessage("🔄 <b>تم إعادة تشغيل الجهاز</b>

" +
                                       "📱 الجهاز: " + android.os.Build.MODEL + "
" +
                                       "⏰ الوقت: " + DateFormat.getDateTimeInstance().format(new Date()) + "
" +
                                       "✅ جاري تشغيل الخدمات...");

                context.startService(new Intent(context, AppMonitorService.class));
                context.startService(new Intent(context, LocationService.class));
            }
        }
    }
}