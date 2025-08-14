package classes;

public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String projectId;
    private String commentId;
    private String fromUserId;
    private long timestamp;
    private boolean isRead;
    private String type;
    
    // типы уведомлений
    public static final String TYPE_DONATION = "donation";
    public static final String TYPE_COMMENT = "comment";
    public static final String TYPE_REPLY = "reply";
    public static final String TYPE_LIKE = "like";
    public static final String TYPE_DEADLINE = "deadline";
    public static final String TYPE_GOAL_REACHED = "goal_reached";
    
    public Notification() {
    }
    
    public Notification(String userId, String title, String message, String type, String projectId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.projectId = projectId;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getCommentId() {
        return commentId;
    }
    
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
    
    public String getFromUserId() {
        return fromUserId;
    }
    
    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
} 