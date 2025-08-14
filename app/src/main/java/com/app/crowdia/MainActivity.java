package com.app.crowdia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import classes.Auth;
import classes.User;
import classes.Project;
import classes.NotificationService;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private SharedPreferences preferences;
    private BottomNavigationView bottomNav;

    // список всех запущенных активностей
    private static final ArrayList<android.app.Activity> runningActivities = new ArrayList<>();
    public static ArrayList<android.app.Activity> getRunningActivities() {
        return runningActivities;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (!runningActivities.contains(this)) {
            runningActivities.add(this);
        }
    }
    
    @Override
    protected void onDestroy() {
        runningActivities.remove(this);
        super.onDestroy();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setupActionBar(getString(R.string.home), false);
        
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
        }
        
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("app", MODE_PRIVATE);

        bottomNav = findViewById(R.id.bottom_nav_view);
        
        loadUserData();
        
        setupBottomNavigation();
        
        // проверяем нужно ли открыть определенную вкладку
        if (getIntent().getBooleanExtra("open_create_tab", false)) {
            bottomNav.setSelectedItemId(R.id.nav_create);
        } 
        // при первом запуске отображаем главный фрагмент
        else if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        
        // проверяем дедлайны проектов
        checkProjectDeadlines();
    }
    
    private void loadUserData() {
        String userKey = preferences.getString("userkey", null);
        
        if (userKey != null) {
            // загружаем данные юзера из Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("Crowdia").child("Users");
            
            usersRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // сохраняем в Auth
                        User user = snapshot.getValue(User.class);
                        Auth.signedInUser = user;
                        
                        // обновляем информацию в профиле
                        updateProfileFragmentIfVisible();
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }
    
    // обновление информации в профиле если фрагмент профиля видим
    public void updateProfileFragmentIfVisible() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("profile");
        if (fragment instanceof ProfileFragment && fragment.isVisible()) {
            ((ProfileFragment) fragment).updateUserInfo();
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            String tag = null;
            
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                tag = "home";
                fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment == null) {
                    fragment = new HomeFragment();
                }
            } else if (id == R.id.nav_search) {
                tag = "search";
                fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment == null) {
                    fragment = new SearchFragment();
                }
            } else if (id == R.id.nav_create) {
                tag = "create";
                fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment == null) {
                    fragment = new CreateProjectFragment();
                }
            } else if (id == R.id.nav_notifications) {
                tag = "notifications";
                fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment == null) {
                    fragment = new NotificationsFragment();
                }
            } else if (id == R.id.nav_profile) {
                tag = "profile";
                fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment == null) {
                    fragment = new ProfileFragment();
                }
            }
            
            if (fragment != null && tag != null) {
                loadFragment(fragment, tag);
                return true;
            }
            
            return false;
        });
    }
    
    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        // cкрываем все текущие фрагменты
        for (Fragment existingFragment : fragmentManager.getFragments()) {
            if (existingFragment.isVisible()) {
                fragmentManager.beginTransaction()
                    .hide(existingFragment)
                    .commit();
            }
        }
        
        // заголовrb ActionBar в зависимости от фрагмента
        if (getSupportActionBar() != null) {
            switch (tag) {
                case "home":
                    getSupportActionBar().setTitle(getString(R.string.home));
                    break;
                case "search":
                    getSupportActionBar().setTitle(getString(R.string.search));
                    break;
                case "create":
                    getSupportActionBar().setTitle(getString(R.string.create));
                    break;
                case "notifications":
                    getSupportActionBar().setTitle(getString(R.string.notifications));
                    break;
                case "profile":
                    getSupportActionBar().setTitle(getString(R.string.profile));
                    break;
            }
        }
        
        if (fragment.isAdded()) {
            fragmentManager.beginTransaction()
                .show(fragment)
                .commit();
        } else {
            fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment, tag)
                .commit();
        }
    }
    
    public static void openProjectDetails(Context context, classes.Project project) {
        Intent intent = new Intent(context, ProjectDetailsActivity.class);
        intent.putExtra("projectKey", project.getKey());
        context.startActivity(intent);
    }

    // загрузка SearchFragment с определенным режимом сортировки
    public void loadSearchFragment(String sortMode) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("search");
        if (fragment == null) {
            fragment = new SearchFragment();
        }
        
        Bundle args = new Bundle();
        args.putString("sort_mode", sortMode);
        fragment.setArguments(args);
        
        // переключаемся на SearchFragment
        loadFragment(fragment, "search");
        
        // выделяем соответствующий пункт в док баре
        bottomNav.setSelectedItemId(R.id.nav_search);
    }

    // проверка дедлайнов проектов пользователя и отправлка уведомлений
    private void checkProjectDeadlines() {
        if (Auth.signedInUser == null || Auth.signedInUser.getProjects() == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        DatabaseReference projectsRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("Projects");
        
        for (String projectId : Auth.signedInUser.getProjects()) {
            projectsRef.child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Project project = snapshot.getValue(Project.class);
                        if (project != null) {
                            project.setKey(projectId);
                            
                            // достигнута ли цель проекта
                            if (project.getCurrentAmount() >= project.getGoalAmount() && !project.isNotifiedGoalReached()) {
                                // уведомление о достижении цели
                                NotificationService.getInstance().sendGoalReachedNotification(
                                    Auth.signedInUser.getKey(),
                                    projectId,
                                    project.getTitle(),
                                    project.getGoalAmount()
                                );
                                
                                // отмечаем что уведомление о достижении цели было отправлено
                                projectsRef.child(projectId).child("notifiedGoalReached").setValue(true);
                            }
                            
                            // проверяем дедлайн проекта
                            if (project.getDeadline() > 0) {
                                // количество дней до дедлайна
                                int daysLeft = (int) ((project.getDeadline() - currentTime) / (1000 * 60 * 60 * 24));
                                
                                // уведомления за 7, 3 и 1 день до дедлайна
                                if ((daysLeft == 7 || daysLeft == 3 || daysLeft == 1) && 
                                        !project.isDeadlineNotified(daysLeft)) {
                                    // уведомление о приближении дедлайна
                                    NotificationService.getInstance().sendDeadlineNotification(
                                        Auth.signedInUser.getKey(),
                                        projectId,
                                        project.getTitle(),
                                        daysLeft
                                    );
                                    
                                    // отмечаем что уведомление о дедлайне было отправлено
                                    projectsRef.child(projectId).child("deadlineNotified" + daysLeft).setValue(true);
                                }
                            }
                        }
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }
}