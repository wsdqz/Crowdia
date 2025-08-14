package com.app.crowdia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProjectImageAdapter extends RecyclerView.Adapter<ProjectImageAdapter.ImageViewHolder> {
    
    private ArrayList<String> imagesBase64;
    private Context context;
    
    public ProjectImageAdapter(Context context) {
        this.context = context;
        this.imagesBase64 = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_project_image, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageBase64 = imagesBase64.get(position);
        
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                // В случае ошибки оставляем дефолтное изображение
                holder.imageView.setImageResource(R.drawable.default_avatar);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.default_avatar);
        }
        
        // текст индикатора текущего изображения
        holder.imageCounterText.setText((position + 1) + "/" + getItemCount());
    }
    
    @Override
    public int getItemCount() {
        return imagesBase64.size();
    }
    
    public void setImages(ArrayList<String> images) {
        this.imagesBase64.clear();
        if (images != null) {
            this.imagesBase64.addAll(images);
        }
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView imageCounterText;
        
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.projectImageView);
            imageCounterText = itemView.findViewById(R.id.imageCounterText);
        }
    }
} 