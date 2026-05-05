package com.example.parentalcontrol;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SetupActivity extends AppCompatActivity {
    
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    private DevicePolicyManager dpm;
    private ComponentName adminComponent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = getSharedPreferences("ParentalControl", MODE_PRIVATE);
        if (prefs.getBoolean("setup_complete", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_setup);
        
        EditText etSupervisorName = findViewById(R.id.etSupervisorName);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnEnableAdmin = findViewById(R.id.btnEnableAdmin);
        
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, AdminReceiver.class);
        
        btnEnableAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
                "مطلوب لتفعيل الرقابة الأبوية ومنع إلغاء التثبيت");
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
        });
        
        btnSave.setOnClickListener(v -> {
            String supervisorName = etSupervisorName.getText().toString().trim();
            if (supervisorName.isEmpty()) {
                Toast.makeText(this, "اكتب اسم المشرف", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!dpm.isAdminActive(adminComponent)) {
                Toast.makeText(this, "لازم تفعل صلاحيات المشرف الأول", Toast.LENGTH_SHORT).show();
                return;
            }
            
            prefs.edit()
                .putString("supervisor_name", supervisorName)
                .putBoolean("setup_complete", true)
                .apply();
            
            TelegramBot.init(this);
            
            startService(new Intent(this, AppMonitorService.class));
            startService(new Intent(this, LocationService.class));
            
            Toast.makeText(this, "تم الإعداد بنجاح!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "تم تفعيل صلاحيات المشرف", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "رفضت التفعيل", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
