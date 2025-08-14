package com.app.crowdia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import classes.Project;

public class RecentProjectAdapter extends ArrayAdapter<Project> {
    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    private OnProjectClickListener listener;

    public RecentProjectAdapter(@NonNull Context context, ArrayList<Project> projects, OnProjectClickListener listener) {
        super(context, R.layout.project_card_full_width, projects);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Project project = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.project_card_full_width, parent, false);
        }
        
        ImageView image = convertView.findViewById(R.id.projectImage);
        TextView title = convertView.findViewById(R.id.projectTitle);
        TextView amount = convertView.findViewById(R.id.projectAmount);
        TextView deadline = convertView.findViewById(R.id.projectDeadline);
        TextView fullyFundedBadge = convertView.findViewById(R.id.fullyFundedBadge);

        // загружаем обложку если она есть
        if (project.getCoverImage() != null && !project.getCoverImage().isEmpty()) {
            try {
                
                byte[] decodedString = Base64.decode(project.getCoverImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                
                if (bitmap != null) {
                    image.setImageBitmap(bitmap);
                } else {
                    image.setImageResource(R.drawable.ic_launcher_background);
                }
            } catch (IllegalArgumentException e) {
                // ошибка декодирования Base64
                image.setImageResource(R.drawable.ic_launcher_background);
            } catch (Exception e) {
                image.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            image.setImageResource(R.drawable.ic_launcher_background);
        }
        
        title.setText(project.getTitle());
        
        // строковые ресурсы с форматированием
        String amountText = String.format(getContext().getString(R.string.collected), (int)project.getCurrentAmount() + "₸");
        amount.setText(amountText);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String deadlineText = String.format(getContext().getString(R.string.deadline), sdf.format(new Date(project.getDeadline())));
        deadline.setText(deadlineText);
        
        // показываем бейдж если проект собрал полную сумму
        if (project.isFullyFunded()) {
            fullyFundedBadge.setVisibility(View.VISIBLE);
        } else {
            fullyFundedBadge.setVisibility(View.GONE);
        }
        
        convertView.setOnClickListener(v -> {
            if (listener != null) listener.onProjectClick(project);
        });
        
        return convertView;
    }
} 