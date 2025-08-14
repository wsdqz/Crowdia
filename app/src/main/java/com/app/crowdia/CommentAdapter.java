package com.app.crowdia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import classes.Comment;
import classes.User;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    
    private List<Comment> comments;
    private Context context;
    private String projectOwnerId;
    private String currentUserId;
    private CommentInteractionListener listener;
    
    public interface CommentInteractionListener {
        void onReplyClick(Comment comment, int position);
        void onLikeClick(Comment comment, int position);
    }
    
    public CommentAdapter(Context context, String projectOwnerId, String currentUserId, CommentInteractionListener listener) {
        this.context = context;
        this.comments = new ArrayList<>();
        this.projectOwnerId = projectOwnerId;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        
        loadUserData(comment.getUserId(), holder.userName, holder.userAvatar);

        // текст
        holder.commentText.setText(comment.getText());
        
        // дата
        holder.commentDate.setText(DateUtils.getRelativeTimeSpanString(
                comment.getTimestamp(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ));
        
        // бейдж автора если от владельца проекта
        if (comment.getUserId().equals(projectOwnerId)) {
            holder.authorBadge.setVisibility(View.VISIBLE);
        } else {
            holder.authorBadge.setVisibility(View.GONE);
        }
        
        // лайки
        holder.likeCount.setText(String.valueOf(comment.getLikesCount()));
        
        // цвет иконки лайка
        if (comment.isLikedBy(currentUserId)) {
            holder.likeIcon.setImageResource(R.drawable.ic_like_filled);
        } else {
            holder.likeIcon.setImageResource(R.drawable.ic_like);
        }
        
        // ответ (если он есть)
        if (comment.hasReply()) {
            holder.replyContainer.setVisibility(View.VISIBLE);
            holder.replyText.setText(comment.getReply().getText());
            holder.replyDate.setText(DateUtils.getRelativeTimeSpanString(
                    comment.getReply().getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            ));
            
            // всегда показываем имя автора проекта в ответе
            holder.replyAuthorName.setText(context.getString(R.string.project_author_label));
        } else {
            holder.replyContainer.setVisibility(View.GONE);
        }
        
        // кнопка ответа (видна только автору и только если нет ответа)
        if (currentUserId.equals(projectOwnerId) && !comment.hasReply()) {
            holder.replyButton.setVisibility(View.VISIBLE);
        } else {
            holder.replyButton.setVisibility(View.GONE);
        }
        
        holder.replyButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReplyClick(comment, holder.getAdapterPosition());
            }
        });
        
        holder.likeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClick(comment, holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return comments.size();
    }
    
    public void setComments(List<Comment> comments) {
        this.comments.clear();
        if (comments != null) {
            this.comments.addAll(comments);
        }
        notifyDataSetChanged();
    }

    public void updateComment(Comment comment, int position) {
        this.comments.set(position, comment);
        notifyItemChanged(position);
    }
    
    private void loadUserData(String userId, TextView userNameView, ImageView userAvatarView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userNameView.setText(user.getUsername());
                        
                        // загрузка аватара
                        String avatarBase64 = user.getAvatar();
                        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(avatarBase64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                userAvatarView.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                userAvatarView.setImageResource(R.drawable.default_avatar);
                            }
                        } else {
                            userAvatarView.setImageResource(R.drawable.default_avatar);
                        }
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userNameView.setText("Пользователь");
                userAvatarView.setImageResource(R.drawable.default_avatar);
            }
        });
    }
    
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userName, commentDate, commentText, likeCount, replyButton;
        TextView authorBadge, replyAuthorName, replyDate, replyText;
        ImageView userAvatar, likeIcon;
        LinearLayout likeButton, replyContainer;
        
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            
            userName = itemView.findViewById(R.id.commentUserName);
            commentDate = itemView.findViewById(R.id.commentDate);
            commentText = itemView.findViewById(R.id.commentText);
            userAvatar = itemView.findViewById(R.id.commentUserAvatar);
            likeCount = itemView.findViewById(R.id.likeCount);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            likeButton = itemView.findViewById(R.id.likeButton);
            replyButton = itemView.findViewById(R.id.replyButton);
            authorBadge = itemView.findViewById(R.id.authorBadge);
            
            replyContainer = itemView.findViewById(R.id.replyContainer);
            replyAuthorName = itemView.findViewById(R.id.replyAuthorName);
            replyDate = itemView.findViewById(R.id.replyDate);
            replyText = itemView.findViewById(R.id.replyText);
        }
    }
} 