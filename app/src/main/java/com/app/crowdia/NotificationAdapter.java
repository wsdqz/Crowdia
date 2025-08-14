package com.app.crowdia;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import classes.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    
    private Context context;
    private ArrayList<Notification> notifications;
    private NotificationListener listener;
    
    public interface NotificationListener {
        void onNotificationClick(Notification notification, int position);
        void onDeleteClick(Notification notification, int position);
    }
    
    public NotificationAdapter(Context context, NotificationListener listener) {
        this.context = context;
        this.notifications = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        
        // заголовок и сообщение
        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());
        
        // время
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                notification.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        holder.time.setText(timeAgo);
        
        // показываем/скрываем индикатор непрочитанного уведомления
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);
        
        // устанавливаем иконку
        setNotificationIcon(holder.icon, notification.getType());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification, holder.getAdapterPosition());
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(notification, holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return notifications.size();
    }
    
    public void setNotifications(ArrayList<Notification> notifications) {
        this.notifications.clear();
        if (notifications != null) {
            this.notifications.addAll(notifications);
        }
        notifyDataSetChanged();
    }

    public void removeNotification(int position) {
        if (position >= 0 && position < notifications.size()) {
            notifications.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public void updateNotification(Notification notification, int position) {
        if (position >= 0 && position < notifications.size()) {
            notifications.set(position, notification);
            notifyItemChanged(position);
        }
    }
    
    private void setNotificationIcon(ImageView imageView, String notificationType) {
        imageView.setImageResource(R.drawable.ic_notifications);
    }
    
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;
        ImageView icon, deleteButton;
        View unreadIndicator;
        
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notificationTitle);
            message = itemView.findViewById(R.id.notificationMessage);
            time = itemView.findViewById(R.id.notificationTime);
            icon = itemView.findViewById(R.id.notificationIcon);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }
    }
} 