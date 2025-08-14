package com.app.crowdia;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.mindrot.jbcrypt.BCrypt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import classes.Auth;

public class AccountActivity extends BaseActivity {
    
    private EditText usernameInput, emailInput;
    private Button saveProfileButton, changePasswordButton, viewLoginsButton;
    private DatabaseReference usersRef;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        
        setupActionBar(getString(R.string.account), true);
        
        usersRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("Users");
        
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        viewLoginsButton = findViewById(R.id.viewLoginsButton);
        
        if (Auth.signedInUser != null) {
            usernameInput.setText(Auth.signedInUser.getUsername());
            emailInput.setText(Auth.signedInUser.getEmail());
        }
        
        saveProfileButton.setOnClickListener(v -> saveProfileChanges());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        viewLoginsButton.setOnClickListener(v -> showLoginsHistory());
    }
    
    private void saveProfileChanges() {
        String newUsername = usernameInput.getText().toString().trim();
        String newEmail = emailInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(newUsername)) {
            Toast.makeText(this, getString(R.string.enter_username), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(newEmail)) {
            Toast.makeText(this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // проверяем изменился ли email
        boolean emailChanged = !newEmail.equals(Auth.signedInUser.getEmail());
        
        if (emailChanged) {
            // если email изменился запрашиваем пароль
            showPasswordConfirmationDialog(newUsername, newEmail);
        } else {
            // если email не изменился просто обновляем имя пользователя
            updateProfile(newUsername, newEmail);
        }
    }
    
    private void updateProfile(String newUsername, String newEmail) {
        // обновляем данные пользователя
        Auth.signedInUser.setUsername(newUsername);
        Auth.signedInUser.setEmail(newEmail);
        
        // обновляем данные в Firebase
        usersRef.child(Auth.signedInUser.getKey()).child("username").setValue(newUsername);
        usersRef.child(Auth.signedInUser.getKey()).child("email").setValue(newEmail);
        
        Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
    }
    
    private void showPasswordConfirmationDialog(String newUsername, String newEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.password_confirmation));
        builder.setMessage(getString(R.string.password_confirmation_message));
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_password_confirm, null);
        builder.setView(dialogView);
        
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        
        builder.setPositiveButton(getString(R.string.confirm), null);
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String password = passwordInput.getText().toString().trim();
            
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // проверяем пароль
            if (BCrypt.checkpw(password, Auth.signedInUser.getPassword())) {
                updateProfile(newUsername, newEmail);
                dialog.dismiss();
            } else {
                Toast.makeText(this, getString(R.string.invalid_password), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.change_password_title));
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);
        
        EditText currentPasswordInput = dialogView.findViewById(R.id.currentPasswordInput);
        EditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        EditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);
        
        builder.setPositiveButton(getString(R.string.change_password), null);
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPassword = currentPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            
            if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, getString(R.string.fill_all_password_fields), Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, getString(R.string.passwords_dont_match), Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (newPassword.length() < 6) {
                Toast.makeText(this, getString(R.string.password_min_length), Toast.LENGTH_SHORT).show();
                return;
            }
            
            // проверяем текущий пароль
            if (BCrypt.checkpw(currentPassword, Auth.signedInUser.getPassword())) {
                // хеши нового пароля
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                
                Auth.signedInUser.setPassword(hashedPassword);
                
                usersRef.child(Auth.signedInUser.getKey()).child("password").setValue(hashedPassword);
                
                Toast.makeText(this, getString(R.string.password_changed), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, getString(R.string.invalid_password), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoginsHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.logins_history_title));
        
        if (Auth.signedInUser.getLogins() == null || Auth.signedInUser.getLogins().isEmpty()) {
            builder.setMessage(getString(R.string.logins_history_empty));
        } else {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_logins_history, null);
            builder.setView(dialogView);
            
            RecyclerView loginsRecyclerView = dialogView.findViewById(R.id.loginsRecyclerView);
            loginsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            // список времени входов (от новых к старым)
            ArrayList<Long> loginTimes = new ArrayList<>(Auth.signedInUser.getLogins());
            Collections.sort(loginTimes, Collections.reverseOrder());
            
            // адаптер для списка входов
            LoginsAdapter adapter = new LoginsAdapter(loginTimes);
            loginsRecyclerView.setAdapter(adapter);
        }
        
        builder.setPositiveButton(getString(R.string.close), (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    
    // адаптер для списка входов
    private class LoginsAdapter extends RecyclerView.Adapter<LoginsAdapter.LoginViewHolder> {
        
        private ArrayList<Long> loginTimes;
        private SimpleDateFormat dateFormat;
        
        public LoginsAdapter(ArrayList<Long> loginTimes) {
            this.loginTimes = loginTimes;
            this.dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        }
        
        @NonNull
        @Override
        public LoginViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_login, parent, false);
            return new LoginViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull LoginViewHolder holder, int position) {
            long loginTime = loginTimes.get(position);
            String formattedDate = dateFormat.format(new Date(loginTime));
            holder.loginTimeText.setText(formattedDate);
        }
        
        @Override
        public int getItemCount() {
            return loginTimes.size();
        }
        
        class LoginViewHolder extends RecyclerView.ViewHolder {
            TextView loginTimeText;
            
            public LoginViewHolder(@NonNull View itemView) {
                super(itemView);
                loginTimeText = itemView.findViewById(R.id.loginHistoryText);
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 