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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import classes.Announcement;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {
    
    private ArrayList<Announcement> announcements;
    private Context context;
    private String currentUserId;
    private AnnouncementInteractionListener listener;
    
    public AnnouncementAdapter(Context context, String currentUserId, AnnouncementInteractionListener listener) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.announcements = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        Announcement announcement = announcements.get(position);
        
        holder.authorName.setText(announcement.getAuthorName());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(announcement.getTimestamp()));
        holder.announcementDate.setText(formattedDate);
        
        holder.announcementText.setText(announcement.getText());
        
        holder.likesCount.setText(String.valueOf(announcement.getLikesCount()));
        
        // устанавливаем изображение если оно есть
        if (announcement.hasImage()) {
            holder.announcementImage.setVisibility(View.VISIBLE);
            try {
                byte[] decodedString = Base64.decode(announcement.getImageUrl(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.announcementImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.announcementImage.setVisibility(View.GONE);
                e.printStackTrace();
            }
        } else {
            holder.announcementImage.setVisibility(View.GONE);
        }
        
        // устанавливаем состояние кнопки лайка
        updateLikeButton(holder, announcement);
        
        holder.likeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClick(announcement, holder.getAdapterPosition());
            }
        });
    }
    
    private void updateLikeButton(AnnouncementViewHolder holder, Announcement announcement) {
        if (announcement.isLikedBy(currentUserId)) {
            holder.likeButton.setImageResource(R.drawable.ic_like_filled);
        } else {
            holder.likeButton.setImageResource(R.drawable.ic_like);
        }
    }
    
    @Override
    public int getItemCount() {
        return announcements.size();
    }
    
    public void setAnnouncements(ArrayList<Announcement> announcements) {
        this.announcements = announcements;
        notifyDataSetChanged();
    }
    
    public void updateAnnouncement(Announcement announcement, int position) {
        if (position >= 0 && position < announcements.size()) {
            announcements.set(position, announcement);
            notifyItemChanged(position);
        }
    }
    
    public static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView authorName, announcementDate, announcementText, likesCount;
        ImageView announcementImage;
        ImageButton likeButton;
        
        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.authorName);
            announcementDate = itemView.findViewById(R.id.announcementDate);
            announcementText = itemView.findViewById(R.id.announcementText);
            announcementImage = itemView.findViewById(R.id.announcementImage);
            likeButton = itemView.findViewById(R.id.likeButton);
            likesCount = itemView.findViewById(R.id.likesCount);
        }
    }
    
    public interface AnnouncementInteractionListener {
        void onLikeClick(Announcement announcement, int position);
    }
} 