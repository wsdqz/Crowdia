package com.app.crowdia;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import classes.Auth;
import classes.Comment;
import classes.Project;
import classes.Donate;
import classes.NotificationService;

public class ProjectDetailsActivity extends BaseActivity implements CommentAdapter.CommentInteractionListener {
    
    private String projectKey;
    private Project currentProject;

    private ViewPager2 imageViewPager;
    private TextView projectTitle, projectCategory, projectDeadline;
    private TextView projectCurrentAmount, projectGoalAmount, projectBackersCount;
    private TextView projectDescription, noCommentsText;
    private ProgressBar projectProgressBar;
    private Button supportButton, progressJournalButton;
    private EditText commentInput;
    private ImageButton sendCommentButton;
    private RecyclerView commentsRecyclerView;
    
    private ProjectImageAdapter imageAdapter;
    private CommentAdapter commentAdapter;
    
    private DatabaseReference projectRef;
    private DatabaseReference commentsRef;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);
        
        projectKey = getIntent().getStringExtra("projectKey");
        if (projectKey == null) {
            Toast.makeText(this, R.string.project_loading_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupActionBar(getString(R.string.project_details), true);
        
        initUI();
        
        setupAdapters();
        
        loadProjectData();
        
        setupEventListeners();
    }
    
    private void initUI() {
        imageViewPager = findViewById(R.id.imageViewPager);
        projectTitle = findViewById(R.id.projectTitle);
        projectCategory = findViewById(R.id.projectCategory);
        projectDeadline = findViewById(R.id.projectDeadline);
        projectCurrentAmount = findViewById(R.id.projectCurrentAmount);
        projectGoalAmount = findViewById(R.id.projectGoalAmount);
        projectBackersCount = findViewById(R.id.projectBackersCount);
        projectProgressBar = findViewById(R.id.projectProgressBar);
        projectDescription = findViewById(R.id.projectDescription);
        supportButton = findViewById(R.id.supportButton);
        progressJournalButton = findViewById(R.id.progressJournalButton);
        commentInput = findViewById(R.id.commentInput);
        sendCommentButton = findViewById(R.id.sendCommentButton);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        noCommentsText = findViewById(R.id.noCommentsText);
    }
    
    private void setupAdapters() {
        // настройка адаптера для изображений
        imageAdapter = new ProjectImageAdapter(this);
        imageViewPager.setAdapter(imageAdapter);
        
        // настройка адаптера для вопросов
        commentAdapter = new CommentAdapter(this, "", Auth.signedInUser.getKey(), this);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);
    }
    
    private void loadProjectData() {
        projectRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("Projects").child(projectKey);
        commentsRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("ProjectComments").child(projectKey);
        
        // загрузка данных проекта
        projectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentProject = snapshot.getValue(Project.class);

                    if (currentProject != null) {
                        currentProject.setKey(projectKey);
                        
                        updateUI();
                        
                        // обновляем ID владельца проекта в адаптере комментариев
                        commentAdapter = new CommentAdapter(
                                ProjectDetailsActivity.this,
                                currentProject.getCreatorId(),
                                Auth.signedInUser.getKey(),
                                ProjectDetailsActivity.this
                        );
                        commentsRecyclerView.setAdapter(commentAdapter);
                        
                        loadComments();
                    } else {
                        Toast.makeText(ProjectDetailsActivity.this, R.string.project_not_found, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProjectDetailsActivity.this, R.string.project_not_found, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProjectDetailsActivity.this, getString(R.string.project_loading_error) + ": " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Comment> comments = new ArrayList<>();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        comment.setKey(commentSnapshot.getKey());
                        comments.add(comment);
                    }
                }
                
                commentAdapter.setComments(comments);
                
                // сообщение если нет комментариев
                if (comments.isEmpty()) {
                    noCommentsText.setVisibility(View.VISIBLE);
                } else {
                    noCommentsText.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProjectDetailsActivity.this, getString(R.string.comments_loading_error, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI() {
        if (currentProject == null) return;
        
        setTitle(currentProject.getTitle() != null ? currentProject.getTitle() : getString(R.string.project_details));
        
        projectTitle.setText(currentProject.getTitle() != null ? currentProject.getTitle() : "");
        
        // устанавливаем категорию
        if (currentProject.getCategory() != null && !currentProject.getCategory().isEmpty()) {
            projectCategory.setText(currentProject.getCategory());
            projectCategory.setVisibility(View.VISIBLE);
        } else {
            projectCategory.setVisibility(View.GONE);
        }
        
        // форматирование даты дедлайна
        if (currentProject.getDeadline() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String deadlineStr = String.format(getString(R.string.deadline_format), dateFormat.format(new Date(currentProject.getDeadline())));
            projectDeadline.setText(deadlineStr);
            projectDeadline.setVisibility(View.VISIBLE);
        } else {
            projectDeadline.setVisibility(View.GONE);
        }
        
        // форматирование сумм
        projectCurrentAmount.setText(String.format(Locale.getDefault(), "%,.0f ₸", currentProject.getCurrentAmount()));
        projectGoalAmount.setText(String.format(getString(R.string.of_amount), String.format(Locale.getDefault(), "%,.0f ₸", currentProject.getGoalAmount())));
        
        // прогресс-бар
        projectProgressBar.setProgress(currentProject.getProgressPercentage());
        
        // количество спонсоров
        int backersCount = currentProject.getBackersCount();
        String backersText = backersCount + " " + 
                getResources().getQuantityString(R.plurals.backers_count, backersCount);
        projectBackersCount.setText(backersText);
        
        // описание
        if (currentProject.getDescription() != null && !currentProject.getDescription().isEmpty()) {
            projectDescription.setText(currentProject.getDescription());
            projectDescription.setVisibility(View.VISIBLE);
        } else {
            projectDescription.setText("");
        }
        
        // загружаем изображения
        ArrayList<String> allImages = currentProject.getAllImages();
        if (allImages != null && !allImages.isEmpty()) {
            imageAdapter.setImages(allImages);
            imageViewPager.setVisibility(View.VISIBLE);
        } else {
            imageViewPager.setVisibility(View.GONE);
        }
        
        updateSupportButton();
    }
    
    private void updateSupportButton() {
        if (currentProject.isBackedBy(Auth.signedInUser.getKey())) {
            supportButton.setText(R.string.support_project_again);
            supportButton.setEnabled(true);
        } else {
            supportButton.setText(R.string.support_project);
            supportButton.setEnabled(true);
        }
    }
    
    private void setupEventListeners() {
        sendCommentButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!TextUtils.isEmpty(commentText)) {
                addComment(commentText);
            } else {
                Toast.makeText(this, R.string.enter_question_text, Toast.LENGTH_SHORT).show();
            }
        });
        
        supportButton.setOnClickListener(v -> {
            showSupportDialog();
        });
        
        progressJournalButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnnouncementsActivity.class);
            intent.putExtra("projectKey", projectKey);
            startActivity(intent);
        });
    }
    
    private void addComment(String commentText) {
        if (commentText.isEmpty()) return;
        
        Comment comment = new Comment(
            Auth.signedInUser.getKey(),
            Auth.signedInUser.getUsername(),
            commentText,
            System.currentTimeMillis()
        );
        
        DatabaseReference newCommentRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("ProjectComments").child(projectKey).push();
        comment.setKey(newCommentRef.getKey());
        
        newCommentRef.setValue(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                commentInput.setText("");
                noCommentsText.setVisibility(View.GONE);
                
                // уведомление владельцу проекта о новом комментарии
                if (!Auth.signedInUser.getKey().equals(currentProject.getCreatorId())) {
                    NotificationService.getInstance().sendCommentNotification(
                        currentProject.getCreatorId(),
                        Auth.signedInUser.getKey(),
                        Auth.signedInUser.getUsername(),
                        projectKey,
                        currentProject.getTitle(),
                        comment.getKey(),
                        commentText.length() > 50 ? commentText.substring(0, 50) + "..." : commentText
                    );
                }
            } else {
                Toast.makeText(this, R.string.comment_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showSupportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.support_project);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_support_project, null);
        builder.setView(dialogView);
        
        TextView balanceText = dialogView.findViewById(R.id.balanceText);
        EditText amountInput = dialogView.findViewById(R.id.amountInput);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button supportButton = dialogView.findViewById(R.id.supportButton);
        
        // текущий баланс пользователя
        String balanceFormatted = String.format(getString(R.string.your_balance), 
                String.format(Locale.getDefault(), "%,.0f ₸", Auth.signedInUser.getBalance()));
        balanceText.setText(balanceFormatted);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        supportButton.setOnClickListener(v -> {
            String amountStr = amountInput.getText().toString().trim();
            if (!TextUtils.isEmpty(amountStr)) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(this, R.string.amount_must_be_positive, Toast.LENGTH_SHORT).show();
                    } else {
                        // собрал ли проект уже нужную сумму
                        if (currentProject.isFullyFunded()) {
                            showFullyFundedDialog(amount);
                        } else {
                            // достаточно ли средств
                            if (Auth.signedInUser.getBalance() < amount) {
                                // сообщение о недостатке средств
                                Toast.makeText(this, R.string.insufficient_funds, Toast.LENGTH_SHORT).show();
                            } else {
                                // если средств хватает поддерживаем проект
                                supportProject(amount);
                                dialog.dismiss();
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, R.string.enter_valid_amount, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.enter_amount, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showFullyFundedDialog(double amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.project_fully_funded_title);
        builder.setMessage(R.string.project_fully_funded_message);
        
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            supportProject(amount);
        });
        
        builder.setNegativeButton(R.string.no, (dialog, which) -> {
            dialog.dismiss();
        });
        
        builder.show();
    }
    
    private void supportProject(double amount) {
        if (amount <= 0) {
            Toast.makeText(this, R.string.amount_must_be_positive, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // достаточно ли средств
        if (Auth.signedInUser.getBalance() < amount) {
            Toast.makeText(this, R.string.insufficient_funds, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // списываем средства с баланса
        double newUserBalance = Auth.signedInUser.getBalance() - amount;
        Auth.signedInUser.setBalance(newUserBalance);
        
        // обновляем баланс юзера в Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Crowdia")
                .child("Users").child(Auth.signedInUser.getKey());
        userRef.child("balance").setValue(newUserBalance);
        
        // обновляем текущую сумму проекта
        double newAmount = currentProject.getCurrentAmount() + amount;
        projectRef.child("currentAmount").setValue(newAmount);
        
        // обновляем доступную сумму для вывода
        double newAvailableAmount = currentProject.getAvailableAmount() + amount;
        projectRef.child("availableAmount").setValue(newAvailableAmount);
        
        // добавляем пользователя как спонсора
        projectRef.child("backers").child(Auth.signedInUser.getKey()).setValue(true);
        
        Donate donate = new Donate(
            Auth.signedInUser.getKey(),
            projectKey,
            currentProject.getTitle(),
            amount
        );
        
        // сохраняем пожертвование в Firebase
        DatabaseReference donatesRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("Donates");
        DatabaseReference newDonateRef = donatesRef.push();
        donate.setKey(newDonateRef.getKey());
        newDonateRef.setValue(donate);
        
        // добавляем запись о пожертвовании в список пожертвований пользователя
        ArrayList<String> donates = Auth.signedInUser.getDonates();
        if (donates == null) {
            donates = new ArrayList<>();
        }
        if (!donates.contains(projectKey)) {
            donates.add(projectKey);
            userRef.child("donates").setValue(donates);
            Auth.signedInUser.setDonates(donates);
        }
        
        // уведомление владельцу проекта о пожертвовании
        NotificationService.getInstance().sendDonationNotification(
            currentProject.getCreatorId(),
            Auth.signedInUser.getKey(),
            Auth.signedInUser.getUsername(),
            projectKey,
            currentProject.getTitle(),
            amount
        );
        
        // достигнута ли цель проекта после этого пожертвования
        if (!currentProject.isFullyFunded() && newAmount >= currentProject.getGoalAmount()) {
            // уведомление о достижении цели проекта
            NotificationService.getInstance().sendGoalReachedNotification(
                currentProject.getCreatorId(),
                projectKey,
                currentProject.getTitle(),
                currentProject.getGoalAmount()
            );
        }
        
        Toast.makeText(this, String.format(getString(R.string.support_success), amount + " ₸"), Toast.LENGTH_SHORT).show();
        
        updateProfileFragmentBalance();
    }
    
    // обновляет отображение баланса если он открыт
    private void updateProfileFragmentBalance() {
        try {
            for (android.app.Activity activity : MainActivity.getRunningActivities()) {
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).updateProfileFragmentIfVisible();
                    break;
                }
            }
        } catch (Exception e) {
        }
    }
    
    @Override
    public void onReplyClick(Comment comment, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comment_reply, null);
        com.google.android.material.textfield.TextInputEditText input = dialogView.findViewById(R.id.replyInput);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button sendButton = dialogView.findViewById(R.id.sendButton);
        
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        sendButton.setOnClickListener(v -> {
            String replyText = input.getText().toString().trim();
            if (!TextUtils.isEmpty(replyText)) {
                Comment.Reply reply = new Comment.Reply(replyText, System.currentTimeMillis());
                commentsRef.child(comment.getKey()).child("reply").setValue(reply)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProjectDetailsActivity.this, R.string.reply_sent, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            
                            // уведомление автору комментария о полученном ответе
                            if (!comment.getUserId().equals(Auth.signedInUser.getKey())) {
                                NotificationService.getInstance().sendReplyNotification(
                                    comment.getUserId(),
                                    Auth.signedInUser.getKey(),
                                    Auth.signedInUser.getUsername(),
                                    projectKey,
                                    currentProject.getTitle(),
                                    comment.getKey()
                                );
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ProjectDetailsActivity.this, R.string.reply_error, Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, R.string.enter_reply_text, Toast.LENGTH_SHORT).show();
                // фокус на поле ввода
                input.requestFocus();
            }
        });
    }
    
    @Override
    public void onLikeClick(Comment comment, int position) {
        // проекра на самолайк
        if (comment.getUserId().equals(Auth.signedInUser.getKey())) {
            Toast.makeText(this, R.string.cannot_like_own, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // был ли лайк до изменения
        boolean wasLiked = comment.isLikedBy(Auth.signedInUser.getKey());
        
        // обновляем лайк в Firebase
        comment.toggleLike(Auth.signedInUser.getKey());
        commentsRef.child(comment.getKey()).child("likes").setValue(comment.getLikes())
                .addOnSuccessListener(aVoid -> {
                    commentAdapter.updateComment(comment, position);
                    
                    // отправляем уведомление только если лайк был добавлен (а не убран)
                    if (!wasLiked && comment.isLikedBy(Auth.signedInUser.getKey())) {
                        NotificationService.getInstance().sendLikeNotification(
                            comment.getUserId(),
                            Auth.signedInUser.getKey(),
                            Auth.signedInUser.getUsername(),
                            projectKey,
                            currentProject.getTitle(),
                            comment.getKey()
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProjectDetailsActivity.this, R.string.like_error, Toast.LENGTH_SHORT).show();
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