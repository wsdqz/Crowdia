package classes;

import java.util.HashMap;
import java.util.Map;

public class Announcement {
    private String key;
    private String projectId;
    private String authorId;
    private String authorName;
    private String text;
    private String imageUrl; // null если нет изображения
    private long timestamp;
    private Map<String, Boolean> likes;

    public Announcement() {
        likes = new HashMap<>();
    }
    
    public Announcement(String projectId, String authorId, String authorName, String text, String imageUrl) {
        this.projectId = projectId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
        this.likes = new HashMap<>();
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Boolean> getLikes() {
        return likes;
    }
    
    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }
    
    public int getLikesCount() {
        return likes != null ? likes.size() : 0;
    }
    
    public boolean isLikedBy(String userId) {
        return likes != null && likes.containsKey(userId) && likes.get(userId);
    }
    
    public void toggleLike(String userId) {
        if (likes == null) {
            likes = new HashMap<>();
        }
        
        if (likes.containsKey(userId) && likes.get(userId)) {
            likes.remove(userId);
        } else {
            likes.put(userId, true);
        }
    }
    
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }
} 