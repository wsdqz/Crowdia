package com.app.crowdia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MenuItem;
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

public class SignupActivity extends BaseActivity {
    EditText txtLogin, txtEmail, txtPasswd;
    Button buttonRegister, buttonBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setupActionBar("", true);
        
        setContentView(R.layout.activity_signup);

        txtLogin = findViewById(R.id.signupName);
        txtEmail = findViewById(R.id.signupEmail);
        txtPasswd = findViewById(R.id.signupPasswd);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonBackToLogin = findViewById(R.id.buttonBackToLogin);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference crowdia = database.getReference("Crowdia");
        DatabaseReference usersRef = crowdia.child("Users");

        txtPasswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateRegisterButtonState();
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
                updateRegisterButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && txtPasswd.getText().length() > 0 && txtLogin.getText().length() > 0) {
                    boolean isValidEmail = isValidEmail(s.toString());
                    if (isValidEmail) {
                        buttonRegister.setEnabled(true);
                        txtEmail.setError(null);
                    } else {
                        buttonRegister.setEnabled(false);
                    }
                } else {
                    buttonRegister.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        buttonRegister.setOnClickListener(v -> {
            buttonRegister.setEnabled(false);

            String email = txtEmail.getText().toString();
            String login = txtLogin.getText().toString();
            String passwd = txtPasswd.getText().toString();

            Query query = usersRef.orderByChild("username").equalTo(login);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        txtLogin.setError(getString(R.string.user_already_exists));
                        buttonRegister.setEnabled(true);
                    } else {
                        User u = new User();

                        u.setUsername(login);
                        u.setEmail(email);
                        u.setPassword(BCrypt.hashpw(passwd, BCrypt.gensalt()));
                        u.setAdmin(false);

                        DatabaseReference push = usersRef.push();
                        u.setKey(push.getKey());
                        push.setValue(u);

                        Toast.makeText(SignupActivity.this, R.string.login_success, Toast.LENGTH_LONG).show();

                        SharedPreferences preferences = getSharedPreferences("app", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("userkey", u.getKey());
                        editor.apply();

                        Auth.signedInUser = u;
                        if (u.getLogins() == null) {
                            u.setLogins(new ArrayList<>());
                        }
                        u.getLogins().add(System.currentTimeMillis());
                        
                        // cохраняем обновленный список входов в Firebase
                        usersRef.child(u.getKey()).child("logins").setValue(u.getLogins());

                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);

                        finish();
                        startActivity(intent);
                    }
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
    
    // проверка корректности email
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    // состояние кнопки регистрации в зависимости от заполненных полей
    private void updateRegisterButtonState() {
        String email = txtEmail.getText().toString();
        boolean isValid = txtLogin.getText().length() > 0 && 
                          txtPasswd.getText().length() > 0 && 
                          email.length() > 0 && 
                          isValidEmail(email);
                          
        buttonRegister.setEnabled(isValid);
        
        if (email.length() > 0 && !isValidEmail(email)) {
            txtEmail.setError(getString(R.string.invalid_email));
        } else {
            txtEmail.setError(null);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // переход на экран входа при нажатии на кнопку назад
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}