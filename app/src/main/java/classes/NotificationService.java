package classes;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationService {
    
    private static NotificationService instance;
    private DatabaseReference notificationsRef;
    
    private NotificationService() {
        notificationsRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("Notifications");
    }
    
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    // уведомление о пожертвовании
    public void sendDonationNotification(String projectOwnerId, String fromUserId, String fromUsername, 
                                         String projectId, String projectTitle, double amount) {
        String title = "Новое пожертвование";
        String message = fromUsername + " поддержал ваш проект \"" + projectTitle + "\" на сумму " + (int)amount + " ₸";
        
        Notification notification = new Notification(projectOwnerId, title, message, Notification.TYPE_DONATION, projectId);
        notification.setFromUserId(fromUserId);
        
        saveNotification(notification);
    }
    
    // уведомление о новом комментарии к проекту
    public void sendCommentNotification(String projectOwnerId, String fromUserId, String fromUsername, 
                                        String projectId, String projectTitle, String commentId, String commentText) {
        String title = "Новый комментарий";
        String message = fromUsername + " оставил комментарий к вашему проекту \"" + projectTitle + "\"";
        
        Notification notification = new Notification(projectOwnerId, title, message, Notification.TYPE_COMMENT, projectId);
        notification.setFromUserId(fromUserId);
        notification.setCommentId(commentId);
        
        saveNotification(notification);
    }
    
    // уведомление об ответе на комментарий
    public void sendReplyNotification(String userId, String fromUserId, String fromUsername, 
                                      String projectId, String projectTitle, String commentId) {
        String title = "Ответ на комментарий";
        String message = fromUsername + " ответил на ваш комментарий в проекте \"" + projectTitle + "\"";
        
        Notification notification = new Notification(userId, title, message, Notification.TYPE_REPLY, projectId);
        notification.setFromUserId(fromUserId);
        notification.setCommentId(commentId);
        
        saveNotification(notification);
    }
    
    // уведомление о лайке комментария
    public void sendLikeNotification(String userId, String fromUserId, String fromUsername, 
                                     String projectId, String projectTitle, String commentId) {
        String title = "Новый лайк";
        String message = fromUsername + " оценил ваш комментарий в проекте \"" + projectTitle + "\"";
        
        Notification notification = new Notification(userId, title, message, Notification.TYPE_LIKE, projectId);
        notification.setFromUserId(fromUserId);
        notification.setCommentId(commentId);
        
        saveNotification(notification);
    }
    
    // уведомление о достижении цели проекта
    public void sendGoalReachedNotification(String projectOwnerId, String projectId, String projectTitle, double goalAmount) {
        String title = "Цель достигнута!";
        String message = "Ваш проект \"" + projectTitle + "\" собрал необходимую сумму " + (int)goalAmount + " ₸";
        
        Notification notification = new Notification(projectOwnerId, title, message, Notification.TYPE_GOAL_REACHED, projectId);
        
        saveNotification(notification);
    }
    
    // уведомление о приближении дедлайна проекта
    public void sendDeadlineNotification(String projectOwnerId, String projectId, String projectTitle, int daysLeft) {
        String title = "Приближается дедлайн";
        String message = "До окончания сбора средств для проекта \"" + projectTitle + "\" осталось " + daysLeft + " дней";
        
        Notification notification = new Notification(projectOwnerId, title, message, Notification.TYPE_DEADLINE, projectId);
        
        saveNotification(notification);
    }
    
    // cохранение уведомление в Firebase
    private void saveNotification(Notification notification) {
        DatabaseReference newNotificationRef = notificationsRef.push();
        notification.setId(newNotificationRef.getKey());
        newNotificationRef.setValue(notification);
    }
    
    //  отметка уведомления прочитанным
    public void markAsRead(String notificationId) {
        notificationsRef.child(notificationId).child("read").setValue(true);
    }
    
    // удаление уведомления
    public void deleteNotification(String notificationId) {
        notificationsRef.child(notificationId).removeValue();
    }
} 