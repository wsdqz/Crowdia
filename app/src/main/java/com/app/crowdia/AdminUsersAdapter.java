package com.app.crowdia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

import classes.User;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder> {
    
    private ArrayList<User> users;
    private Context context;
    private OnUserActionListener listener;
    
    public interface OnUserActionListener {
        void onUserClick(User user);
        void onActionClick(User user);
    }
    
    public AdminUsersAdapter(Context context, OnUserActionListener listener) {
        this.context = context;
        this.users = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        
        holder.userName.setText(user.getUsername());
        
        holder.userEmail.setText(user.getEmail());
        
        // устанавливаем баланс пользователя
        String balanceText = String.format(Locale.getDefault(), "Баланс: %.0f ₸", user.getBalance());
        holder.userBalance.setText(balanceText);
        
        // показываем или скрываем бейдж админа
        if (user.isAdmin()) {
            holder.adminBadge.setVisibility(View.VISIBLE);
        } else {
            holder.adminBadge.setVisibility(View.GONE);
        }
        
        holder.userActionsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActionClick(user);
            }
        });
        
        // устанавливаем аватар пользователя
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(user.getAvatar(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.userAvatar.setImageBitmap(bitmap);
            } catch (Exception e) {
                // в случае ошибки оставляем дефолтное изображение
                holder.userAvatar.setImageResource(R.drawable.default_avatar);
            }
        } else {
            holder.userAvatar.setImageResource(R.drawable.default_avatar);
        }
        
        // устанавливаем обработчик нажатия на карточку пользователя
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    // обновление списка пользователей
    public void updateUsers(ArrayList<User> newUsers) {
        this.users.clear();
        this.users.addAll(newUsers);
        notifyDataSetChanged();
    }
    
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar;
        TextView userName;
        TextView userEmail;
        TextView userBalance;
        TextView adminBadge;
        ImageButton userActionsButton;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userBalance = itemView.findViewById(R.id.userBalance);
            adminBadge = itemView.findViewById(R.id.adminBadge);
            userActionsButton = itemView.findViewById(R.id.userActionsButton);
        }
    }
} 