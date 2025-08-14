package com.app.crowdia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // получаем сохраненные настройки языка
        SharedPreferences prefs = newBase.getSharedPreferences("app", Context.MODE_PRIVATE);
        String languageCode = prefs.getString("language", "ru");
        
        // создаем конфигурацию с выбранным языком
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // применяем сохраненную тему
        SharedPreferences preferences = getSharedPreferences("app", MODE_PRIVATE);
        int themeMode = preferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        
        // скрываем экшн бар
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
//
        // настраиваем статус бар в зависимости от темы
        setupStatusBar();

        // прогресс бар
        ProgressBar pb = findViewById(R.id.progressBar);
        String user = preferences.getString("userkey", null);
        Handler handler = new Handler();
        handler.postDelayed(() -> pb.setProgress(25, true), 500);
        handler.postDelayed(() -> pb.setProgress(50, true), 1000);
        handler.postDelayed(() -> pb.setProgress(75, true), 1500);
        handler.postDelayed(() -> pb.setProgress(100, true), 2000);


        handler.postDelayed(() -> {
            if (user == null) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                intent.putExtra("userkey", user);
                startActivity(intent);
            }
            finish();
        }, 2100);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void setupStatusBar() {
        Window window = getWindow();
        
        // проверяем включена ли темная тема
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        
        if (isDarkTheme) {
            // темная тема
            window.setStatusBarColor(getResources().getColor(R.color.colorStatusBarDark, null));
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(false);
        } else {
            // светлая тема
            window.setStatusBarColor(getResources().getColor(R.color.white, null));
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setAppearanceLightStatusBars(true);
        }
    }
}