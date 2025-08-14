package classes;

import java.util.HashMap;
import java.util.Map;

public class Comment {
    private String key;
    private String userId;
    private String username;
    private String text;
    private long timestamp;
    private Map<String, Boolean> likes;
    private Reply reply;
    
    public Comment() {
        likes = new HashMap<>();
    }
    
    public Comment(String userId, String text, long timestamp) {
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
        this.likes = new HashMap<>();
    }
    
    public Comment(String userId, String username, String text, long timestamp) {
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
        this.likes = new HashMap<>();
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
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
    
    public Reply getReply() {
        return reply;
    }
    
    public void setReply(Reply reply) {
        this.reply = reply;
    }
    
    public boolean hasReply() {
        return reply != null;
    }
    
    // класс для ответов на вопросы
    public static class Reply {
        private String text;
        private long timestamp;
        
        public Reply() {
        }
        
        public Reply(String text, long timestamp) {
            this.text = text;
            this.timestamp = timestamp;
        }
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
} 