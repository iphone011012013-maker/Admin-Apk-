package com.example.parentalcontrol;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "تم تفعيل صلاحيات المشرف", Toast.LENGTH_SHORT).show();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        TelegramBot.sendMessage("⚠️ تنبيه: ابنك حاول إلغاء صلاحيات المشرف!");
        return "تحذير: إلغاء صلاحيات المشرف سيتم إبلاغ الأب فوراً!";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        TelegramBot.sendMessage("🚨 تنبيه خطير: تم إلغاء صلاحيات المشرف!");
    }
}