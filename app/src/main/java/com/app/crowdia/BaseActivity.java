package com.app.crowdia;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {
    
    @Override
    protected void attachBaseContext(Context newBase) {
        // получаем сохраненные настройки языка
        SharedPreferences preferences = newBase.getSharedPreferences("app", Context.MODE_PRIVATE);
        String languageCode = preferences.getString("language", "ru");
        
        // создаем конфигурацию с выбранным языком
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }
        
        setupStatusBar();
    }
    
    // цвет и иконки статус бара в зависимости от текущей темы
    protected void setupStatusBar() {
        Window window = getWindow();
        
        // проверка включена ли темная тема
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

    protected void setupActionBar(String title, boolean showBackButton) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showBackButton);
        }

    }
} 
