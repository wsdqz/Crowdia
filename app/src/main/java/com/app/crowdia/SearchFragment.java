package com.app.crowdia;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.HashSet;
import java.util.Set;

import classes.Project;

public class SearchFragment extends Fragment {
    private LinearLayout allProjectsContainer;
    private LinearLayout categoriesContainer;
    private EditText searchInput;
    private ImageButton clearSearchButton;
    private TextView sortByNewest, sortByPopular, sortByEnding;
    private ArrayList<Project> allProjects = new ArrayList<>();
    private ArrayList<Project> filteredProjects = new ArrayList<>();
    private String currentSearchQuery = "";
    private String selectedCategory; // по дефолту показываем все категории
    private enum SortMode { NEWEST, POPULAR, ENDING }
    private SortMode currentSortMode = SortMode.NEWEST;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        
        allProjectsContainer = rootView.findViewById(R.id.allProjectsContainer);
        categoriesContainer = rootView.findViewById(R.id.categoriesContainer);
        searchInput = rootView.findViewById(R.id.searchInput);
        clearSearchButton = rootView.findViewById(R.id.clearSearchButton);
        sortByNewest = rootView.findViewById(R.id.sortByNewest);
        sortByPopular = rootView.findViewById(R.id.sortByPopular);
        sortByEnding = rootView.findViewById(R.id.sortByEnding);
        
        selectedCategory = getString(R.string.all_categories);
        
        // проверяем передан ли режим сортировки
        Bundle args = getArguments();
        if (args != null && args.containsKey("sort_mode")) {
            String sortMode = args.getString("sort_mode");
            switch (sortMode) {
                case "popular":
                    setActiveSortMode(SortMode.POPULAR);
                    break;
                case "ending":
                    setActiveSortMode(SortMode.ENDING);
                    break;
                case "newest":
                default:
                    setActiveSortMode(SortMode.NEWEST);
                    break;
            }
        }
        
        loadProjects();
        setupSearch();
        setupSorting();
        
        return rootView;
    }

    private void loadProjects() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference projectsRef = database.getReference("Crowdia").child("Projects");
        
        projectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allProjects.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Project project = snapshot.getValue(Project.class);
                    if (project != null) {
                        allProjects.add(project);
                    }
                }
                
                setupCategoryFilter();
                filterAndSortProjects();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    
    private void setupCategoryFilter() {
        // очищаем контейнер категорий
        categoriesContainer.removeAllViews();
        
        // получаем уникальные категории из всех проектов
        Set<String> categories = new HashSet<>();
        for (Project project : allProjects) {
            if (project.getCategory() != null && !project.getCategory().isEmpty()) {
                categories.add(project.getCategory());
            }
        }
        
        // "Все"
        String allCategoriesText = getString(R.string.all_categories);
        addCategoryButton(allCategoriesText, selectedCategory.equals(allCategoriesText));
        
        // остальные категории
        for (String category : categories) {
            addCategoryButton(category, selectedCategory.equals(category));
        }
    }
    
    private void addCategoryButton(String category, boolean isSelected) {
        TextView categoryButton = new TextView(getContext());
        categoryButton.setText(category);
        categoryButton.setPadding(24, 12, 24, 12);
        categoryButton.setTextSize(14);
        
        if (isSelected) {
            categoryButton.setBackgroundResource(R.drawable.category_button_selected);
            categoryButton.setTextColor(getResources().getColor(R.color.white));
        } else {
            categoryButton.setBackgroundResource(R.drawable.category_button);
            categoryButton.setTextColor(getResources().getColor(R.color.colorText));
        }
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 16, 0);
        categoryButton.setLayoutParams(params);
        
        categoryButton.setOnClickListener(v -> {
            selectedCategory = category;
            setupCategoryFilter();
            filterAndSortProjects();
        });
        
        categoriesContainer.addView(categoryButton);
    }

    private void setupSearch() {
        // настраиваем кнопку очистки
        clearSearchButton.setOnClickListener(v -> {
            searchInput.setText("");
            clearSearchButton.setVisibility(View.GONE);
        });
        
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filterAndSortProjects();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSorting() {
        sortByNewest.setOnClickListener(v -> {
            setActiveSortMode(SortMode.NEWEST);
        });
        
        sortByPopular.setOnClickListener(v -> {
            setActiveSortMode(SortMode.POPULAR);
        });
        
        sortByEnding.setOnClickListener(v -> {
            setActiveSortMode(SortMode.ENDING);
        });
    }

    private void setActiveSortMode(SortMode mode) {
        currentSortMode = mode;
        
        sortByNewest.setTextColor(getResources().getColor(R.color.colorTextSecondary));
        sortByPopular.setTextColor(getResources().getColor(R.color.colorTextSecondary));
        sortByEnding.setTextColor(getResources().getColor(R.color.colorTextSecondary));
        
        // выделение активного режим
        switch (mode) {
            case NEWEST:
                sortByNewest.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case POPULAR:
                sortByPopular.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case ENDING:
                sortByEnding.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
        }
        
        filterAndSortProjects();
    }

    private void filterAndSortProjects() {
        filteredProjects = new ArrayList<>();
        
        // фильт по названию и категории
        for (Project project : allProjects) {
            // соответствие категории
            boolean categoryMatch = selectedCategory.equals(getString(R.string.all_categories)) ||
                    (project.getCategory() != null && project.getCategory().equals(selectedCategory));
            
            // соответствие поисковому запросу (название)
            boolean searchMatch = currentSearchQuery.isEmpty() || 
                    (project.getTitle() != null && 
                     project.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()));
            
            // добавляем проект
            if (categoryMatch && searchMatch) {
                filteredProjects.add(project);
            }
        }
        
        // сортируем согласно выбранному режиму
        switch (currentSortMode) {
            case NEWEST:
                Collections.sort(filteredProjects, (a, b) -> {
                    // сортировка по времени создания (от новых к старым)
                    if (a.getCreatedAt() > 0 && b.getCreatedAt() > 0) {
                        return Long.compare(b.getCreatedAt(), a.getCreatedAt());
                    }
                    // если нет времени создания используем deadline
                    return Long.compare(b.getDeadline(), a.getDeadline());
                });
                break;
            case POPULAR:
                Collections.sort(filteredProjects, (a, b) -> Double.compare(b.getCurrentAmount(), a.getCurrentAmount()));
                break;
            case ENDING:
                long now = System.currentTimeMillis();
                Collections.sort(filteredProjects, Comparator.comparingLong(Project::getDeadline));
                filteredProjects.removeIf(p -> p.getDeadline() < now); // удаляем истекшие проекты
                break;
        }
        
        displayProjects();
    }

    private void displayProjects() {
        if (allProjectsContainer == null || !isAdded()) return;
        
        allProjectsContainer.removeAllViews();
        
        // сообщение если ничего не найдено
        if (filteredProjects.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText(getString(R.string.no_projects_found));
            emptyView.setTextSize(16);
            emptyView.setPadding(0, 50, 0, 0);
            emptyView.setGravity(android.view.Gravity.CENTER);
            allProjectsContainer.addView(emptyView);
            return;
        }
        
        RecentProjectAdapter adapter = new RecentProjectAdapter(getContext(), filteredProjects, this::openProjectDetails);
        
        for (int i = 0; i < filteredProjects.size(); i++) {
            View card = adapter.getView(i, null, allProjectsContainer);
            allProjectsContainer.addView(card);
        }
    }

    private void openProjectDetails(Project project) {
        // открываем детали проекта
        MainActivity.openProjectDetails(getContext(), project);
    }
} 