package com.example.parentalcontrol;

import android.app.Application;

public class MyApp extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        TelegramBot.init(this);
    }
}
