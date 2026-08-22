package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Splash screen: wait for 2 seconds then navigate to the Map Explorer
        // reduced delay slightly for better UX, directing immediately to Map to show services
        new Handler(Looper.getMainLooper()).postDelayed(this::checkNavigationLogic, 2000);
    }

    private void checkNavigationLogic() {
        // Direct the user immediately to MapExplorerActivity so they see services on the map at first launch
        Intent intent = new Intent(MainActivity.this, MapExplorerActivity.class);
        startActivity(intent);
        finish();
    }
}
