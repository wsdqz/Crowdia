package com.app.crowdia;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import classes.Project;

public class HomeFragment extends Fragment {
    private LinearLayout topProjectsContainer, endingProjectsContainer, recentProjectsContainer;
    private ArrayList<Project> allProjects = new ArrayList<>();
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        
        topProjectsContainer = rootView.findViewById(R.id.topProjectsContainer);
        endingProjectsContainer = rootView.findViewById(R.id.endingProjectsContainer);
        recentProjectsContainer = rootView.findViewById(R.id.recentProjectsContainer);
        
        setupShowAllButtons();
        loadProjects();
        
        return rootView;
    }

    private void loadProjects() {
        // загрузка проектов из Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference projectRef = database.getReference("Crowdia").child("Projects");

        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    allProjects.clear();
                    int count = 0;
                    
                    for (DataSnapshot data : snapshot.getChildren()) {
                        try {
                            Project p = data.getValue(Project.class);
                            if (p != null) {
                                p.setKey(data.getKey());
                                allProjects.add(p);
                                count++;
                            }
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), R.string.project_processing_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    updateSections();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.critical_loading_error, Toast.LENGTH_SHORT).show();

                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateSections() {
        if (getActivity() == null) return;
        
        long now = System.currentTimeMillis();
        
        // топ проекты (по currentAmount)
        ArrayList<Project> top = new ArrayList<>(allProjects);
        Collections.sort(top, (a, b) -> Double.compare(b.getCurrentAmount(), a.getCurrentAmount()));
        if (top.size() > 5) top = new ArrayList<>(top.subList(0, 5));
        fillHorizontalSection(topProjectsContainer, top);

        // истекающие проекты (по deadline)
        ArrayList<Project> ending = new ArrayList<>(allProjects);
        
        // сначала фильтруем только будущие проекты
        ending.removeIf(p -> p.getDeadline() <= 0 || p.getDeadline() < now);
        
        // сортируем по возрастанию дедлайна
        Collections.sort(ending, Comparator.comparingLong(Project::getDeadline));
        
        // ограничение 5 проектами
        if (ending.size() > 5) ending = new ArrayList<>(ending.subList(0, 5));
        fillHorizontalSection(endingProjectsContainer, ending);

        // недавние проекты (по дате создания)
        ArrayList<Project> recent = new ArrayList<>(allProjects);
        Collections.sort(recent, (a, b) -> {
            if (a.getCreatedAt() > 0 && b.getCreatedAt() > 0) {
                return Long.compare(b.getCreatedAt(), a.getCreatedAt());
            }
            // если нет времени создания, используем deadline как резервный вариант
            return Long.compare(b.getDeadline(), a.getDeadline());
        });
        fillRecentProjectsSection(recentProjectsContainer, recent);
    }

    private void fillHorizontalSection(LinearLayout container, ArrayList<Project> projects) {
        if (getActivity() == null) return;
        
        container.removeAllViews();
        for (Project project : projects) {
            View card = new ProjectCardAdapter(getActivity(), new ArrayList<>(Collections.singletonList(project)), this::openProjectDetails).getView(0, null, container);
            container.addView(card);
        }
    }

    private void fillRecentProjectsSection(LinearLayout container, ArrayList<Project> projects) {
        if (getActivity() == null) return;
        
        container.removeAllViews();
        RecentProjectAdapter adapter = new RecentProjectAdapter(getActivity(), projects, this::openProjectDetails);
        
        for (int i = 0; i < projects.size(); i++) {
            View card = adapter.getView(i, null, container);
            container.addView(card);
        }
    }

    private void openProjectDetails(Project project) {
        Intent intent = new Intent(getActivity(), ProjectDetailsActivity.class);
        intent.putExtra("projectKey", project.getKey());
        startActivity(intent);
    }
    
    private void setupShowAllButtons() {
        TextView btnShowAllRecent = rootView.findViewById(R.id.btnShowAllRecent);

        btnShowAllRecent.setOnClickListener(v -> {
            // переключение на SearchFragment
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.loadSearchFragment("newest");
            }
        });
    }
} 