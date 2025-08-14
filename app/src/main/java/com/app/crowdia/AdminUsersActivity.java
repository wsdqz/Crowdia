package com.app.crowdia;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import classes.Auth;
import classes.User;

public class AdminUsersActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private AdminUsersAdapter adapter;
    private LinearLayout emptyView;
    private TextView usersCountText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // проверка на админа
        if (!Auth.signedInUser.isAdmin()) {
            Toast.makeText(this, getString(R.string.access_denied), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupActionBar(getString(R.string.manage_users_title), true);
        
        setContentView(R.layout.activity_admin_users);
        
        recyclerView = findViewById(R.id.adminUsersRecyclerView);
        emptyView = findViewById(R.id.emptyUsersView);
        usersCountText = findViewById(R.id.usersCountText);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUsersAdapter(this, new AdminUsersAdapter.OnUserActionListener() {
            @Override
            public void onUserClick(User user) {
            }

            @Override
            public void onActionClick(User user) {
                showUserManagementOptions(user);
            }
        });
        recyclerView.setAdapter(adapter);
        
        loadAllUsers();
    }
    
    private void loadAllUsers() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("Crowdia").child("Users");
        
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> allUsers = new ArrayList<>();
                
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setKey(userSnapshot.getKey());
                            // не добавляем текущего пользователя в список
                            if (!user.getKey().equals(Auth.signedInUser.getKey())) {
                                allUsers.add(user);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(AdminUsersActivity.this, "Ошибка при обработке пользователя", Toast.LENGTH_SHORT).show();
                    }
                }
                
                // обновляем счетчик пользователей
                usersCountText.setText(getString(R.string.users_count, allUsers.size()));
                
                // обновляем адаптер
                adapter.updateUsers(allUsers);
                
                // показываем список или сообщение об отсутствии пользователей
                if (allUsers.isEmpty()) {
                    showEmptyView();
                } else {
                    showUsersList();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminUsersActivity.this, getString(R.string.loading_projects_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showUserManagementOptions(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.user_management));
        
        // создаем опции управления
        String[] options;
        if (user.isAdmin()) {
            options = new String[]{getString(R.string.change_balance), getString(R.string.revoke_admin)};
        } else {
            options = new String[]{getString(R.string.change_balance), getString(R.string.make_admin)};
        }
        
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // изменить баланс
                showChangeBalanceDialog(user);
            } else if (which == 1) {
                // изменить статус администратора
                updateUserAdminStatus(user, !user.isAdmin());
            }
        });
        
        builder.show();
    }
    
    private void showChangeBalanceDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.change_user_balance));
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_funds, null);
        builder.setView(dialogView);
        
        EditText amountInput = dialogView.findViewById(R.id.amountInput);
        amountInput.setText(String.valueOf((int)user.getBalance()));
        
        builder.setPositiveButton(getString(R.string.save), null);
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            if (!TextUtils.isEmpty(amountStr)) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount < 0) {
                        Toast.makeText(this, getString(R.string.balance_negative_error), Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserBalance(user, amount);
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, getString(R.string.enter_valid_amount), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.enter_amount_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUserBalance(User user, double newBalance) {
        // обновляем баланс пользователя в Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Crowdia").child("Users").child(user.getKey());
        
        userRef.child("balance").setValue(newBalance)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.user_balance_updated, user.getUsername()), Toast.LENGTH_SHORT).show();
                    // обновляем локальный объект пользователя
                    user.setBalance(newBalance);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateUserAdminStatus(User user, boolean isAdmin) {
        // обновляем статус администратора пользователя в Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Crowdia").child("Users").child(user.getKey());
        
        userRef.child("admin").setValue(isAdmin)
                .addOnSuccessListener(aVoid -> {
                    String message = isAdmin ? 
                            getString(R.string.user_now_admin, user.getUsername()) :
                            getString(R.string.user_admin_revoked, user.getUsername());
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    // обновляем локальный объект пользователя
                    user.setAdmin(isAdmin);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
    
    private void showUsersList() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }
}
