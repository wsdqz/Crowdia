package com.app.crowdia;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import java.util.Locale;

import classes.Auth;
import classes.Project;
import classes.User;

public class UserProjectsActivity extends BaseActivity implements UserProjectsAdapter.OnProjectClickListener {
    
    private RecyclerView recyclerView;
    private UserProjectsAdapter adapter;
    private LinearLayout emptyView;
    private TextView projectsCountText;
    private Button createProjectButton;
    private TextView noProjectsText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setupActionBar(getString(R.string.my_projects_title), true);
        
        setContentView(R.layout.activity_user_projects);
        
        recyclerView = findViewById(R.id.userProjectsRecyclerView);
        emptyView = findViewById(R.id.emptyProjectsView);
        projectsCountText = findViewById(R.id.projectsCountText);
        createProjectButton = findViewById(R.id.createProjectButton);
        noProjectsText = findViewById(R.id.noProjectsText);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserProjectsAdapter(this, this);
        recyclerView.setAdapter(adapter);
        
        createProjectButton.setOnClickListener(v -> {
            // открываем вкладку создания проекта
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("open_create_tab", true);
            startActivity(intent);
            finish();
        });
        
        loadUserProjects();
    }
    
    private void loadUserProjects() {
        if (Auth.signedInUser == null) {
            Toast.makeText(this, getString(R.string.error_user_not_authorized), Toast.LENGTH_SHORT).show();
            showEmptyView();
            return;
        }
        
        // получаем ID проектов юзера
        ArrayList<String> projectIds = Auth.signedInUser.getProjects();
        

        if (projectIds == null || projectIds.isEmpty()) {
            // если у юзера нет проектов
            showEmptyView();
            return;
        }
        

        // счетчик проектов
        projectsCountText.setText(getString(R.string.projects_count, projectIds.size()));
        
        // загружаем данные каждого проекта из Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ArrayList<Project> userProjects = new ArrayList<>();
        
        try {
            for (String projectId : projectIds) {
                if (projectId == null || projectId.isEmpty()) {
                    continue;
                }
                
                DatabaseReference projectRef = database.getReference("Crowdia").child("Projects").child(projectId);
                
                projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            if (snapshot.exists()) {
                                Project project = snapshot.getValue(Project.class);
                                if (project != null) {
                                    project.setKey(projectId);
                                    userProjects.add(project);

                                    // обновляем адаптер после загрузки всех проектов
                                    if (userProjects.size() == projectIds.size()) {
                                        adapter.updateProjects(userProjects);
                                        showProjectsList();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(UserProjectsActivity.this, getString(R.string.error_processing_project), Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.loading_projects_error), Toast.LENGTH_SHORT).show();
            showEmptyView();
        }
    }
    
    // диалог для вывода средств из проекта
    public void showWithdrawFundsDialog(Project project) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_withdraw_funds, null);
        
        TextView availableFundsText = dialogView.findViewById(R.id.availableFundsText);
        EditText withdrawAmountInput = dialogView.findViewById(R.id.withdrawAmountInput);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button withdrawButton = dialogView.findViewById(R.id.withdrawButton);
        
        // доступная сумму
        availableFundsText.setText(String.format(Locale.getDefault(), 
                getString(R.string.available_funds), project.getAvailableAmount()));
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        withdrawButton.setOnClickListener(v -> {
            String amountStr = withdrawAmountInput.getText().toString().trim();
            if (!TextUtils.isEmpty(amountStr)) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(this, getString(R.string.amount_greater_than_zero), Toast.LENGTH_SHORT).show();
                    } else if (amount > project.getAvailableAmount()) {
                        Toast.makeText(this, getString(R.string.insufficient_available_funds), Toast.LENGTH_SHORT).show();
                    } else {
                        // вывод средств
                        withdrawFunds(project, amount);
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, getString(R.string.enter_valid_amount), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.enter_withdraw_amount_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // вывод средств из проекта на баланс
    private void withdrawFunds(Project project, double amount) {
        // доступная сумма в проекте
        double newAvailableAmount = project.getAvailableAmount() - amount;
        project.setAvailableAmount(newAvailableAmount);
        
        // баланс пользователя
        User user = Auth.signedInUser;
        double currentBalance = user.getBalance();
        double newBalance = currentBalance + amount;
        user.setBalance(newBalance);
        
        // сохраняем в Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        
        // обновляем проект
        DatabaseReference projectRef = database.getReference("Crowdia").child("Projects").child(project.getKey());
        projectRef.child("availableAmount").setValue(newAvailableAmount);
        
        // обновляем пользователя
        DatabaseReference userRef = database.getReference("Crowdia").child("Users").child(user.getKey());
        userRef.child("balance").setValue(newBalance)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, getString(R.string.funds_withdrawn_success), Toast.LENGTH_SHORT).show();
                
                adapter.notifyDataSetChanged();
                
                // обновляем баланс если открыт профиль
                updateProfileFragmentBalance();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, getString(R.string.error_updating_balance), Toast.LENGTH_SHORT).show();
            });
    }
    
    // Обновляет отображение баланса в профиле если он открыт
    private void updateProfileFragmentBalance() {
        MainActivity mainActivity = getMainActivityInstance();
        if (mainActivity != null) {
            mainActivity.updateProfileFragmentIfVisible();
        }
    }

    private MainActivity getMainActivityInstance() {
        try {
            for (android.app.Activity activity : getRunningActivities()) {
                if (activity instanceof MainActivity) {
                    return (MainActivity) activity;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_finding_main_activity), Toast.LENGTH_SHORT).show();
        }
        return null;
    }
    
    // список запущенных активити
    private ArrayList<android.app.Activity> getRunningActivities() {
        try {
            return MainActivity.getRunningActivities();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private void showEmptyView() {
        noProjectsText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        projectsCountText.setText(getString(R.string.projects_count, 0));
    }
    
    private void showProjectsList() {
        noProjectsText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }
    
    @Override
    public void onProjectClick(Project project) {
        // детальная информация о проекте
        Intent intent = new Intent(this, ProjectDetailsActivity.class);
        intent.putExtra("projectKey", project.getKey());
        startActivity(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 