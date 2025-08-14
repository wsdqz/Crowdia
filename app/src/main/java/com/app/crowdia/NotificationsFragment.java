package com.app.crowdia;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import classes.Auth;
import classes.Notification;
import classes.NotificationService;

public class NotificationsFragment extends Fragment implements NotificationAdapter.NotificationListener {
    
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout emptyStateContainer;
    private DatabaseReference notificationsRef;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        
        notificationsRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("Notifications");
        
        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);
        
        swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        
        loadNotifications();
        
        return view;
    }
    
    private void loadNotifications() {
        if (Auth.signedInUser == null) {
            showEmptyState();
            return;
        }
        
        showLoading();
        
        // уведомления для текущего пользователя
        Query query = notificationsRef.orderByChild("userId").equalTo(Auth.signedInUser.getKey());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Notification> notifications = new ArrayList<>();
                
                for (DataSnapshot data : snapshot.getChildren()) {
                    Notification notification = data.getValue(Notification.class);
                    if (notification != null) {
                        notifications.add(notification);
                    }
                }
                
                // cортируем уведомления по времени (от новых к старым)
                Collections.sort(notifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

                adapter.setNotifications(notifications);
                
                // если нет уведомлений
                if (notifications.isEmpty()) {
                    showEmptyState();
                } else {
                    showContent();
                }
                
                // останавливаем анимацию обновления
                swipeRefreshLayout.setRefreshing(false);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showEmptyState();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);
    }
    
    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onNotificationClick(Notification notification, int position) {
        // отмечаем уведомление как прочитанное
        if (!notification.isRead()) {
            notification.setRead(true);
            NotificationService.getInstance().markAsRead(notification.getId());
            adapter.updateNotification(notification, position);
        }
        
        // открываем соответствующий экран от уведомления
        if (notification.getProjectId() != null) {
            Intent intent = new Intent(getContext(), ProjectDetailsActivity.class);
            intent.putExtra("projectKey", notification.getProjectId());
            
            // передаем ID комментария
            if (notification.getCommentId() != null) {
                intent.putExtra("commentId", notification.getCommentId());
            }
            
            startActivity(intent);
        }
    }

    // удаление уведомления
    @Override
    public void onDeleteClick(Notification notification, int position) {
        // из Firebase
        NotificationService.getInstance().deleteNotification(notification.getId());
        
        // из списка
        adapter.removeNotification(position);

        if (adapter.getItemCount() == 0) {
            showEmptyState();
        }
    }
} 