package com.example.graduationproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;


    private FirebaseFirestore db;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_EMAIL = "saved_email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ROLE = "user_role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ImageView btnTogglePassword = findViewById(R.id.btnTogglePassword);
        View btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvRegister = findViewById(R.id.tvSignUp);
        ImageView btnBack = findViewById(R.id.btnBack);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_EMAIL, "");
        if (!savedEmail.isEmpty() && etEmail != null) {
            etEmail.setText(savedEmail);
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnTogglePassword != null) {
            btnTogglePassword.setOnClickListener(v -> {
                if (isPasswordVisible) {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                isPasswordVisible = !isPasswordVisible;
                etPassword.setSelection(etPassword.getText().length());
            });
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            });
        }

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, UserTypeActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> performLogin());
        }
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "يرجى إدخال البريد الإلكتروني وكلمة المرور", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (!user.isEmailVerified()) {
                                showVerificationNeededDialog(email);
                                return;
                            }
                            String userId = user.getUid();
                            saveUserDataLocally(email, userId);
                            fetchUserRole(userId);
                        }
                    } else {
                        Log.w("LoginActivity", "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "البريد الإلكتروني أو كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDataLocally(String email, String userId) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    private void fetchUserRole(String userId) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String role = document.getString("role");
                            navigateToDashboard(role != null ? role : "customer");
                        } else {
                            navigateToDashboard("customer");
                        }
                    } else {
                        Log.e("LoginActivity", "Error fetching user role", task.getException());
                        navigateToDashboard("customer");
                    }
                });
    }

    private void navigateToDashboard(String role) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_USER_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();

        Toast.makeText(LoginActivity.this, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show();
        
        Intent intent;
        if ("provider".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, ProviderDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MapExplorerActivity.class);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showVerificationNeededDialog(String email) {
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("تفعيل الحساب مطلوب")
                .setMessage("لم يتم تفعيل حسابك بعد.\n\nيرجى النقر على رابط التفعيل الذي تم إرساله إلى بريدك الإلكتروني: " + email + "\n\nبعد التفعيل، يمكنك تسجيل الدخول.")
                .setPositiveButton("إعادة إرسال الرابط", (dialog, which) -> resendVerificationEmail())
                .setNegativeButton("حسناً", (dialog, which) -> signOutAndFinish())
                .setCancelable(false)
                .show();
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "تم إرسال رابط التفعيل مجدداً", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "فشل إرسال الرابط، يرجى المحاولة لاحقاً", Toast.LENGTH_SHORT).show();
                        }
                        signOutAndFinish();
                    });
        } else {
            signOutAndFinish();
        }
    }

    private void signOutAndFinish() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
        finish();
    }
}
