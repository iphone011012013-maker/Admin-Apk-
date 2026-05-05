package com.example.parentalcontrol;

import android.app.Application;
import java.text.DateFormat;
import java.util.Date;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TelegramBot.init(this);

        TelegramBot.sendMessage("✅ <b>تم تشغيل النظام الرقابي الأبوي</b>

" +
                               "📱 الجهاز: " + android.os.Build.MODEL + "
" +
                               "🏭 الشركة: " + android.os.Build.MANUFACTURER + "
" +
                               "⏰ الوقت: " + DateFormat.getDateTimeInstance().format(new Date()));
    }
}