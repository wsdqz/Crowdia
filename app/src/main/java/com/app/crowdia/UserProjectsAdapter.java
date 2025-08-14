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
import java.util.List;
import java.util.Locale;

import classes.Project;

public class UserProjectsAdapter extends RecyclerView.Adapter<UserProjectsAdapter.ProjectViewHolder> {
    
    private List<Project> projects;
    private Context context;
    private OnProjectClickListener listener;
    
    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }
    
    public UserProjectsAdapter(Context context, OnProjectClickListener listener) {
        this.context = context;
        this.projects = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.project_card_user, parent, false);
        return new ProjectViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        
        // название проекта
        holder.projectTitle.setText(project.getTitle());
        
        // дедлайн
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String deadlineStr = context.getString(R.string.deadline_format, dateFormat.format(new Date(project.getDeadline())));
        holder.projectDeadline.setText(deadlineStr);
        
        // прогресс сбора средств
        double goalAmount = project.getGoalAmount();
        double currentAmount = project.getCurrentAmount();
        double availableAmount = project.getAvailableAmount();
        int progressPercent = (int) ((currentAmount / goalAmount) * 100);
        
        String progressText = context.getString(R.string.project_progress_format, currentAmount, goalAmount);
        holder.projectProgress.setText(progressText);
        holder.projectProgressPercent.setText(context.getString(R.string.project_progress_percent, progressPercent));
        holder.projectProgressBar.setProgress(progressPercent);
        
        // доступная сумму
        String availableText = context.getString(R.string.available_amount, availableAmount);
        holder.availableAmountText.setText(availableText);
        
        holder.withdrawButton.setOnClickListener(v -> {
            if (context instanceof UserProjectsActivity) {
                ((UserProjectsActivity) context).showWithdrawFundsDialog(project);
            }
        });
        
        // изображение проекта
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
    public void updateProjects(List<Project> newProjects) {
        this.projects.clear();
        this.projects.addAll(newProjects);
        notifyDataSetChanged();
    }
    
    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        ImageView projectImage;
        TextView projectTitle;
        TextView projectDeadline;
        TextView projectProgress;
        TextView projectProgressPercent;
        ProgressBar projectProgressBar;
        TextView availableAmountText;
        Button withdrawButton;
        
        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectImage = itemView.findViewById(R.id.projectImage);
            projectTitle = itemView.findViewById(R.id.projectTitle);
            projectDeadline = itemView.findViewById(R.id.projectDeadline);
            projectProgress = itemView.findViewById(R.id.projectProgress);
            projectProgressPercent = itemView.findViewById(R.id.projectProgressPercent);
            projectProgressBar = itemView.findViewById(R.id.projectProgressBar);
            availableAmountText = itemView.findViewById(R.id.availableAmountText);
            withdrawButton = itemView.findViewById(R.id.withdrawButton);
        }
    }
}