package com.app.crowdia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends BaseActivity {

    private SharedPreferences preferences;
    private TextView currentThemeText;
    private TextView currentLanguageText;
    private TextView appVersionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("app", MODE_PRIVATE);

        setupActionBar(getString(R.string.settings), true);

        currentThemeText = findViewById(R.id.currentThemeText);
        currentLanguageText = findViewById(R.id.currentLanguageText);
        appVersionText = findViewById(R.id.appVersionText);
        LinearLayout themeSettingLayout = findViewById(R.id.themeSettingLayout);
        LinearLayout languageSettingLayout = findViewById(R.id.languageSettingLayout);
        
        loadSettings();

        // обработчик нажатия на настройку темы
        themeSettingLayout.setOnClickListener(v -> showThemeSelectionDialog());

        // обработчик нажатия на настройку языка
        languageSettingLayout.setOnClickListener(v -> showLanguageSelectionDialog());
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSettings() {
        // загрузка настроек темы
        int themeMode = preferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        switch (themeMode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                currentThemeText.setText(R.string.theme_dark);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                currentThemeText.setText(R.string.theme_light);
                break;
            default:
                currentThemeText.setText(R.string.theme_system);
                break;
        }

        // загрузка настроек языка
        String language = preferences.getString("language", "ru"); // русский по дефолту
        switch (language) {
            case "kk":
                currentLanguageText.setText(R.string.language_kazakh);
                break;
            default:
                currentLanguageText.setText(R.string.language_russian);
                break;
        }
    }

    private void showThemeSelectionDialog() {
        final String[] themes = {getString(R.string.theme_system), getString(R.string.theme_light), getString(R.string.theme_dark)};
        int currentTheme = preferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        int selectedIndex = 0;
        
        switch (currentTheme) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                selectedIndex = 2;
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                selectedIndex = 1;
                break;
            default:
                selectedIndex = 0;
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_theme)
                .setSingleChoiceItems(themes, selectedIndex, (dialog, which) -> {
                    int themeMode;
                    switch (which) {
                        case 1:
                            themeMode = AppCompatDelegate.MODE_NIGHT_NO;
                            currentThemeText.setText(R.string.theme_light);
                            break;
                        case 2:
                            themeMode = AppCompatDelegate.MODE_NIGHT_YES;
                            currentThemeText.setText(R.string.theme_dark);
                            break;
                        default:
                            themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                            currentThemeText.setText(R.string.theme_system);
                            break;
                    }

                    // сохраняем выбранную тему
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("theme_mode", themeMode);
                    editor.apply();

                    // применяем тему
                    AppCompatDelegate.setDefaultNightMode(themeMode);

                    dialog.dismiss();
                    Toast.makeText(SettingsActivity.this, R.string.theme_changed, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showLanguageSelectionDialog() {
        final String[] languages = {getString(R.string.language_russian), getString(R.string.language_kazakh)};
        String currentLanguage = preferences.getString("language", "ru");
        int selectedIndex = 0;
        
        switch (currentLanguage) {
            case "kk":
                selectedIndex = 1;
                break;
            default:
                selectedIndex = 0;
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_language)
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String languageCode;

                    switch (which) {
                        case 1:
                            languageCode = "kk";
                            currentLanguageText.setText(R.string.language_kazakh);
                            break;
                        default:
                            languageCode = "ru";
                            currentLanguageText.setText(R.string.language_russian);
                            break;
                    }

                    // сохраняем выбранный язык
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("language", languageCode);
                    editor.apply();

                    dialog.dismiss();
                    Toast.makeText(SettingsActivity.this, R.string.language_changed, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
} 