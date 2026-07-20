package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        CardView btnSave = findViewById(R.id.btnSavePassword);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String newPwd = etNewPassword.getText().toString().trim();
                String confirmPwd = etConfirmPassword.getText().toString().trim();

                if (newPwd.isEmpty() || confirmPwd.isEmpty()) {
                    Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPwd.length() < 6) {
                    Toast.makeText(this, "كلمة المرور يجب أن تكون 6 أحرف على الأقل", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPwd.equals(confirmPwd)) {
                    Toast.makeText(this, "كلمات المرور غير متطابقة", Toast.LENGTH_SHORT).show();
                    return;
                }

                // تنفيذ عملية التغيير في Supabase
                performPasswordReset(newPwd);
            });
        }
    }

    private void performPasswordReset(String newPassword) {
        // هنا يتم استدعاء API تحديث كلمة المرور
        // كمثال سنعتبر العملية نجحت ونعود لشاشة تسجيل الدخول
        Toast.makeText(this, "تم تغيير كلمة المرور بنجاح", Toast.LENGTH_LONG).show();
        
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
