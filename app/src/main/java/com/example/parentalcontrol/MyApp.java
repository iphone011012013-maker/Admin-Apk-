package com.example.parentalcontrol;

import android.app.Application;

public class MyApp extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        TelegramBot.init(this);
        
        // إرسال رسالة تشغيل للبوت
        TelegramBot.sendMessage("✅ <b>تم تشغيل النظام الرقابي الأبوي</b>\n\n" +
                               "📱 الجهاز: " + android.os.Build.MODEL + "\n" +
                               "🏭 الشركة: " + android.os.Build.MANUFACTURER + "\n" +
                               "⏰ الوقت: " + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
    }
}
