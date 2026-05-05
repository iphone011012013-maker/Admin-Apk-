package com.example.parentalcontrol;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private Button btnRequestUsage, btnRequestLocation, btnGenerateReport;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);

        if (!prefs.getBoolean("setup_complete", false)) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        btnRequestUsage = findViewById(R.id.btnRequestUsage);
        btnRequestLocation = findViewById(R.id.btnRequestLocation);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);

        String supervisor = prefs.getString("supervisor_name", "غير معروف");
        tvStatus.setText("المشرف: " + supervisor);

        btnRequestUsage.setOnClickListener(v -> {
            if (!hasUsageStatsPermission()) {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                Toast.makeText(this, "فعل صلاحية الوصول لبيانات الاستخدام", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "الصلاحية موجودة", Toast.LENGTH_SHORT).show();
            }
        });

        btnRequestLocation.setOnClickListener(v -> {
            requestPermissions(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, 100);
        });

        btnGenerateReport.setOnClickListener(v -> {
            Toast.makeText(this, "جاري إعداد التقرير...", Toast.LENGTH_SHORT).show();
            TelegramBot.sendMessage("📊 طلب تقرير من المشرف: " + supervisor);
        });

        startService(new Intent(this, AppMonitorService.class));
        startService(new Intent(this, LocationService.class));
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}