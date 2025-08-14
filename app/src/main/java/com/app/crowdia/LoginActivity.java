package com.app.crowdia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import classes.Auth;
import classes.User;

import java.util.ArrayList;
import java.util.Locale;

public class LoginActivity extends BaseActivity {
    EditText txtLogin, txtPasswd;
    Button buttonLogin, buttonSignup;
    private SharedPreferences preferences;

    @Override
    protected void attachBaseContext(Context newBase) {
        // сохраненные настройки языка
        SharedPreferences prefs = newBase.getSharedPreferences("app", Context.MODE_PRIVATE);
        String languageCode = prefs.getString("language", "ru");
        
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);
        
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar("", false);
        
        setContentView(R.layout.activity_login);

        preferences = getSharedPreferences("app", MODE_PRIVATE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("Crowdia").child("Users");

        txtLogin = findViewById(R.id.loginEmail);
        txtPasswd = findViewById(R.id.loginPasswd);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignup = findViewById(R.id.buttonSignup);

        buttonSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });


        txtPasswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    buttonLogin.setEnabled(!s.toString().trim().isEmpty());
                } else {
                    buttonLogin.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && txtPasswd.getText().length() > 0) {
                    buttonLogin.setEnabled(!s.toString().trim().isEmpty());
                } else {
                    buttonLogin.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonLogin.setOnClickListener(v -> {
            buttonLogin.setEnabled(false);

            String email = txtLogin.getText().toString();
            String passwd = txtPasswd.getText().toString();

            Query query = usersRef.orderByChild("email").equalTo(email);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        User u = data.getValue(User.class);
                        if (!BCrypt.checkpw(passwd, u.getPassword())) {
                            Toast.makeText(LoginActivity.this, R.string.invalid_credentials, Toast.LENGTH_LONG).show();
                        } else {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("userkey", u.getKey());
                            editor.apply();

                            Auth.signedInUser = u;
                            
                            // добавление текущего времени в список входов
                            if (u.getLogins() == null) {
                                u.setLogins(new ArrayList<>());
                            }
                            u.getLogins().add(System.currentTimeMillis());
                            
                            // сохранение обновленного списка входов в Firebase
                            usersRef.child(u.getKey()).child("logins").setValue(u.getLogins());

                            Toast.makeText(LoginActivity.this, "Вход", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                            startActivity(intent);
                            finish();
                        }
                        return;
                    }
                    Toast.makeText(LoginActivity.this, R.string.invalid_credentials, Toast.LENGTH_LONG).show();
                    buttonLogin.setEnabled(true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}