package com.app.crowdia;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import java.util.List;

import classes.Auth;
import classes.Project;

public class AdminProjectsActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private AdminProjectsAdapter adapter;
    private LinearLayout emptyView;
    private TextView projectsCountText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // проверяем является ли юзер админом
        if (!Auth.signedInUser.isAdmin()) {
            Toast.makeText(this, getString(R.string.access_denied), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupActionBar(getString(R.string.manage_projects_title), true);
        
        setContentView(R.layout.activity_admin_projects);

        recyclerView = findViewById(R.id.adminProjectsRecyclerView);
        emptyView = findViewById(R.id.emptyProjectsView);
        projectsCountText = findViewById(R.id.projectsCountText);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminProjectsAdapter(this, new AdminProjectsAdapter.OnProjectActionListener() {
            @Override
            public void onProjectClick(Project project) {
                // открывает детальную информацию о проекте
                Intent intent = new Intent(AdminProjectsActivity.this, ProjectDetailsActivity.class);
                intent.putExtra("projectKey", project.getKey());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Project project) {
                showDeleteConfirmationDialog(project);
            }
        });
        recyclerView.setAdapter(adapter);
        
        loadAllProjects();
    }
    
    private void loadAllProjects() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference projectsRef = database.getReference("Crowdia").child("Projects");
        
        projectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Project> allProjects = new ArrayList<>();
                
                for (DataSnapshot projectSnapshot : snapshot.getChildren()) {
                    try {
                        Project project = projectSnapshot.getValue(Project.class);
                        if (project != null) {
                            // устанавливаем ключ проекта
                            project.setKey(projectSnapshot.getKey());
                            allProjects.add(project);
                        }
                    } catch (Exception e) {
                        Toast.makeText(AdminProjectsActivity.this, "Ошибка при обработке проекта", Toast.LENGTH_SHORT).show();

                    }
                }
                
                // обновляем счетчик проектов
                projectsCountText.setText(getString(R.string.projects_count, allProjects.size()));
                
                // обновляем адаптер
                adapter.updateProjects(allProjects);
                
                // показываем список или сообщение об отсутствии проектов
                if (allProjects.isEmpty()) {
                    showEmptyView();
                } else {
                    showProjectsList();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminProjectsActivity.this, getString(R.string.loading_projects_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showDeleteConfirmationDialog(Project project) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_project_title));
        builder.setMessage(getString(R.string.delete_project_message, project.getTitle()));
        
        builder.setPositiveButton(getString(R.string.delete_project), (dialog, which) -> {
            deleteProject(project);
        });
        
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void deleteProject(Project project) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        
        // удаляем проект из базы данных
        DatabaseReference projectRef = database.getReference("Crowdia").child("Projects").child(project.getKey());
        projectRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.project_deleted), Toast.LENGTH_SHORT).show();
                    
                    // удаляем проект из списка проектов автора
                    if (project.getCreatorId() != null && !project.getCreatorId().isEmpty()) {
                        removeProjectFromAuthor(project.getCreatorId(), project.getKey());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.project_delete_error), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void removeProjectFromAuthor(String authorId, String projectKey) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Crowdia").child("Users").child(authorId);
        
        userRef.child("projects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<String> userProjects = new ArrayList<>();
                    for (DataSnapshot projectSnapshot : snapshot.getChildren()) {
                        String projectId = projectSnapshot.getValue(String.class);
                        if (!projectKey.equals(projectId)) {
                            userProjects.add(projectId);
                        }
                    }
                    userRef.child("projects").setValue(userProjects);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminProjectsActivity.this, "Ошибка при обновлении списка проектов пользователя", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showEmptyView() {
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
    
    private void showProjectsList() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }
} 