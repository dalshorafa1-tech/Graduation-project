package com.example.graduationproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Splash screen: wait for 3 seconds then navigate based on app state
        new Handler(Looper.getMainLooper()).postDelayed(this::checkNavigationLogic, 3000);
    }

    private void checkNavigationLogic() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        Intent intent;
        if (isLoggedIn) {
            // 1. User is logged in -> Go to Map
            intent = new Intent(MainActivity.this, MapExplorerActivity.class);
        } else if (isFirstTime) {
            // 2. First time opening the app -> Go to Onboarding
            intent = new Intent(MainActivity.this, OnboardingActivity.class);
        } else {
            // 3. Not first time but not logged in -> Go to Login
            intent = new Intent(MainActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
