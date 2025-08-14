package com.app.crowdia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import classes.Project;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminProjectsAdapter extends RecyclerView.Adapter<AdminProjectsAdapter.ProjectViewHolder> {
    
    private ArrayList<Project> projects;
    private Context context;
    private OnProjectActionListener listener;
    
    public interface OnProjectActionListener {
        void onProjectClick(Project project);
        void onDeleteClick(Project project);
    }
    
    public AdminProjectsAdapter(Context context, OnProjectActionListener listener) {
        this.context = context;
        this.projects = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.project_card_admin, parent, false);
        return new ProjectViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        
        holder.projectTitle.setText(project.getTitle());
        
        loadAuthorInfo(project.getCreatorId(), holder);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String deadlineStr = context.getString(R.string.deadline_format, dateFormat.format(new Date(project.getDeadline())));
        holder.projectDeadline.setText(deadlineStr);
        
        double goalAmount = project.getGoalAmount();
        double currentAmount = project.getCurrentAmount();
        int progressPercent = (int) ((currentAmount / goalAmount) * 100);
        
        String progressText = context.getString(R.string.project_progress_format, currentAmount, goalAmount);
        holder.projectProgress.setText(progressText);
        holder.projectProgressPercent.setText(context.getString(R.string.project_progress_percent, progressPercent));
        holder.projectProgressBar.setProgress(progressPercent);
        
        String status = progressPercent >= 100 ? context.getString(R.string.project_status_completed) : context.getString(R.string.project_status_active);
        String statusText = context.getString(R.string.project_status, status);
        holder.projectStatusText.setText(statusText);
        
        holder.deleteProjectButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(project);
            }
        });
        
        // устанавливаем изображение проекта
        if (project.getCoverImage() != null && !project.getCoverImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(project.getCoverImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.projectImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                // в случае ошибки оставляем дефолтное изображение
                holder.projectImage.setImageResource(R.drawable.default_avatar);
            }
        } else {
            holder.projectImage.setImageResource(R.drawable.default_avatar);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProjectClick(project);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return projects.size();
    }
    
    // обновление списка проектов
    public void updateProjects(ArrayList<Project> newProjects) {
        this.projects.clear();
        this.projects.addAll(newProjects);
        notifyDataSetChanged();
    }
    
    private void loadAuthorInfo(String authorId, ProjectViewHolder holder) {
        if (authorId == null || authorId.isEmpty()) {
            holder.projectAuthor.setText(context.getString(R.string.project_author, context.getString(R.string.unknown)));
            return;
        }
        
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Crowdia").child("Users").child(authorId);
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    if (username != null && !username.isEmpty()) {
                        holder.projectAuthor.setText(context.getString(R.string.project_author, username));
                    } else {
                        holder.projectAuthor.setText(context.getString(R.string.project_author, "ID " + authorId));
                    }
                } else {
                    holder.projectAuthor.setText(context.getString(R.string.project_author, "ID " + authorId));
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.projectAuthor.setText(context.getString(R.string.project_author, "ID " + authorId));
            }
        });
    }
    
    // ViewHolder для проекта
    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        ImageView projectImage;
        TextView projectTitle;
        TextView projectAuthor;
        TextView projectDeadline;
        TextView projectProgress;
        TextView projectProgressPercent;
        ProgressBar projectProgressBar;
        TextView projectStatusText;
        Button deleteProjectButton;
        
        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectImage = itemView.findViewById(R.id.projectImage);
            projectTitle = itemView.findViewById(R.id.projectTitle);
            projectAuthor = itemView.findViewById(R.id.projectAuthor);
            projectDeadline = itemView.findViewById(R.id.projectDeadline);
            projectProgress = itemView.findViewById(R.id.projectProgress);
            projectProgressPercent = itemView.findViewById(R.id.projectProgressPercent);
            projectProgressBar = itemView.findViewById(R.id.projectProgressBar);
            projectStatusText = itemView.findViewById(R.id.projectStatusText);
            deleteProjectButton = itemView.findViewById(R.id.deleteProjectButton);
        }
    }
}
