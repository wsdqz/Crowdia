package com.app.crowdia;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import classes.Announcement;
import classes.Auth;
import classes.Project;

public class AnnouncementsActivity extends BaseActivity implements AnnouncementAdapter.AnnouncementInteractionListener {
    
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_CAMERA = 102;
    
    private String projectKey;
    private Project project;
    private boolean isProjectOwner = false;
    
    private RecyclerView announcementsRecyclerView;
    private TextView noAnnouncementsText;
    private FloatingActionButton addAnnouncementButton;
    
    private AnnouncementAdapter announcementAdapter;
    
    // Firebase
    private DatabaseReference projectRef;
    private DatabaseReference announcementsRef;
    
    // для диалога создания объявления
    private String encodedImage = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);
        
        // получаем ключ проекта из Intent
        projectKey = getIntent().getStringExtra("projectKey");
        if (projectKey == null) {
            Toast.makeText(this, getString(R.string.project_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupActionBar(getString(R.string.announcements_title), true);
        
        announcementsRecyclerView = findViewById(R.id.announcementsRecyclerView);
        noAnnouncementsText = findViewById(R.id.noAnnouncementsText);
        addAnnouncementButton = findViewById(R.id.addAnnouncementButton);
        
        // сообщение об отсутствии объявлений по умолчанию
        noAnnouncementsText.setVisibility(View.VISIBLE);
        announcementsRecyclerView.setVisibility(View.GONE);
        
        announcementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementAdapter = new AnnouncementAdapter(this, Auth.signedInUser.getKey(), this);
        announcementsRecyclerView.setAdapter(announcementAdapter);
        
        loadProjectInfo();
        
        loadAnnouncements();
        
        // настройка обработчиков событий
        setupEventListeners();
    }
    
    private void loadProjectInfo() {
        projectRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("Projects").child(projectKey);
        announcementsRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("ProjectAnnouncements").child(projectKey);
        
        // загрузка данных проекта
        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    project = snapshot.getValue(Project.class);
                    if (project != null) {
                        project.setKey(projectKey);
                        
                        // является ли текущий пользователь создателем проекта
                        isProjectOwner = project.getCreatorId().equals(Auth.signedInUser.getKey());
                        
                        // Показываем кнопку добавления объявления только создателю проекта
                        addAnnouncementButton.setVisibility(isProjectOwner ? View.VISIBLE : View.GONE);
                        
                        // Загружаем объявления
                        loadAnnouncements();
                    }
                } else {
                    Toast.makeText(AnnouncementsActivity.this, R.string.project_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AnnouncementsActivity.this, "Ошибка загрузки проекта: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void loadAnnouncements() {
        announcementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Announcement> announcements = new ArrayList<>();
                for (DataSnapshot announcementSnapshot : snapshot.getChildren()) {
                    Announcement announcement = announcementSnapshot.getValue(Announcement.class);
                    if (announcement != null) {
                        announcement.setKey(announcementSnapshot.getKey());
                        announcements.add(announcement);
                    }
                }
                
                // сортируем объявления по времени создания (новые сверху)
                Collections.sort(announcements, (a1, a2) -> Long.compare(a2.getTimestamp(), a1.getTimestamp()));
                
                announcementAdapter.setAnnouncements(announcements);
                
                // показываем сообщение, если нет объявлений
                if (announcements.isEmpty()) {
                    noAnnouncementsText.setVisibility(View.VISIBLE);
                    announcementsRecyclerView.setVisibility(View.GONE);
                } else {
                    noAnnouncementsText.setVisibility(View.GONE);
                    announcementsRecyclerView.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AnnouncementsActivity.this, R.string.announcements_loading_error, Toast.LENGTH_SHORT).show();
                
                // при ошибке загрузки тоже показываем сообщение об отсутствии объявлений
                noAnnouncementsText.setVisibility(View.VISIBLE);
                announcementsRecyclerView.setVisibility(View.GONE);
            }
        });
    }
    
    private void setupEventListeners() {
        addAnnouncementButton.setOnClickListener(v -> {
            showCreateAnnouncementDialog();
        });
    }
    
    private void showCreateAnnouncementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_announcement, null);
        builder.setView(dialogView);
        
        EditText announcementInput = dialogView.findViewById(R.id.announcementInput);
        Button addPhotoButton = dialogView.findViewById(R.id.addPhotoButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button postButton = dialogView.findViewById(R.id.postButton);
        FrameLayout imageContainer = dialogView.findViewById(R.id.imageContainer);
        ImageView announcementImage = dialogView.findViewById(R.id.announcementImage);
        ImageButton removeImageButton = dialogView.findViewById(R.id.removeImageButton);
        
        // сбрасываем закодированное изображение
        encodedImage = null;
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // добавления фото
        addPhotoButton.setOnClickListener(v -> {
            showImageSourceDialog();
        });
        
        // удаления фото
        removeImageButton.setOnClickListener(v -> {
            encodedImage = null;
            imageContainer.setVisibility(View.GONE);
        });
        
        // кнопка отмены
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });
        
        // кнопка публикации
        postButton.setOnClickListener(v -> {
            String announcementText = announcementInput.getText().toString().trim();
            if (TextUtils.isEmpty(announcementText)) {
                Toast.makeText(this, R.string.enter_announcement_text, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // публикация объявление
            postAnnouncement(announcementText, encodedImage);
            dialog.dismiss();
        });
    }
    
    private void showImageSourceDialog() {
        String[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery)};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_image));
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // камера
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_CAMERA);
                } else {
                    Toast.makeText(this, R.string.camera_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                // галерея
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhotoIntent, REQUEST_GALLERY);
            }
        });
        
        builder.show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            
            if (requestCode == REQUEST_CAMERA && data != null) {
                // получаем изображение с камеры
                Bundle extras = data.getExtras();
                if (extras != null) {
                    bitmap = (Bitmap) extras.get("data");
                }
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                // получаем изображение из галереи
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        bitmap = BitmapFactory.decodeStream(imageStream);
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this, R.string.gallery_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            if (bitmap != null) {
                // масштабируем изображение для уменьшения размера
                bitmap = scaleBitmap(bitmap, 800);
                
                // кодируем изображение в Base64
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                
                AlertDialog dialog = (AlertDialog) getWindow().getDecorView().getRootView().getTag();
                if (dialog != null && dialog.isShowing()) {
                    FrameLayout imageContainer = dialog.findViewById(R.id.imageContainer);
                    ImageView announcementImage = dialog.findViewById(R.id.announcementImage);
                    
                    if (imageContainer != null && announcementImage != null) {
                        announcementImage.setImageBitmap(bitmap);
                        imageContainer.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }
    
    private Bitmap scaleBitmap(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = originalWidth;
        int newHeight = originalHeight;
        
        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            if (originalWidth > originalHeight) {
                newWidth = maxDimension;
                newHeight = (int) (originalHeight * ((float) maxDimension / originalWidth));
            } else {
                newHeight = maxDimension;
                newWidth = (int) (originalWidth * ((float) maxDimension / originalHeight));
            }
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    private void postAnnouncement(String text, String imageUrl) {
        // создаем новое объявление
        Announcement announcement = new Announcement(
            projectKey,
            Auth.signedInUser.getKey(),
            Auth.signedInUser.getUsername(),
            text,
            imageUrl
        );
        
        // сохраняем объявление в Firebase
        DatabaseReference newAnnouncementRef = announcementsRef.push();
        announcement.setKey(newAnnouncementRef.getKey());
        
        newAnnouncementRef.setValue(announcement).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // добавляем ключ объявления в список объявлений проекта
                if (project != null) {
                    project.addAnnouncement(announcement.getKey());
                    projectRef.child("announcements").setValue(project.getAnnouncements());
                }
                
                Toast.makeText(this, R.string.announcement_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.announcement_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onLikeClick(Announcement announcement, int position) {
        // проверяем не ставит ли юзер лайк своему объявлению
        if (announcement.getAuthorId().equals(Auth.signedInUser.getKey())) {
            Toast.makeText(this, R.string.cannot_like_own, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // обновляем лайк в объявлении
        announcement.toggleLike(Auth.signedInUser.getKey());
        
        // обновляем лайк в Firebase
        announcementsRef.child(announcement.getKey()).child("likes").setValue(announcement.getLikes())
                .addOnSuccessListener(aVoid -> {
                    announcementAdapter.updateAnnouncement(announcement, position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.like_error, Toast.LENGTH_SHORT).show();
                });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}