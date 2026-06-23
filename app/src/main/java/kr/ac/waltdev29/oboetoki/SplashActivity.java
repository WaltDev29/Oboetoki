package kr.ac.waltdev29.oboetoki;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import kr.ac.waltdev29.oboetoki.databinding.ActivitySplashBinding;
import kr.ac.waltdev29.oboetoki.util.PreferenceManager;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginStatus, 2000);
    }

    private void checkLoginStatus() {
        String token = preferenceManager.getToken();
        Intent intent;
        
        if (token == null || token.isEmpty()) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
