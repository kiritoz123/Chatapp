package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.window.SplashScreen;

import com.example.chatapplication.databinding.ActivitySplashBinding;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        new Handler().postDelayed(() -> {
            if (preferenceManager.getBoolean(Constants.KEY_IS_SIGN_IN)) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            }
            finish();
        }, 1000);

    }
}