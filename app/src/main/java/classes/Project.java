package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Project {
    private String key;
    private String title;
    private String description;
    private String creatorId;
    private double goalAmount; // сумма которую надо собрать
    private double currentAmount; // собранная сумма
    private double availableAmount; // сумма доступная для вывода
    private String category;
    private long deadline;
    private long createdAt;
    private Map<String, Boolean> backers; // кеи спонсоров и их статус
    private String coverImage;
    private ArrayList<String> additionalImages;
    private boolean notifiedGoalReached;
    private boolean deadlineNotified7;
    private boolean deadlineNotified3;
    private boolean deadlineNotified1;
    private ArrayList<String> announcements; // кеи объявлений проекта

    public Project() {
        backers = new HashMap<>();
        additionalImages = new ArrayList<>();
        announcements = new ArrayList<>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public double getGoalAmount() {
        return goalAmount;
    }

    public void setGoalAmount(double goalAmount) {
        this.goalAmount = goalAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public double getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(double availableAmount) {
        this.availableAmount = availableAmount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Boolean> getBackers() {
        return backers;
    }

    public void setBackers(Map<String, Boolean> backers) {
        this.backers = backers;
    }
    
    public int getBackersCount() {
        return backers == null ? 0 : backers.size();
    }
    
    public void addBacker(String userId) {
        if (backers == null) {
            backers = new HashMap<>();
        }
        backers.put(userId, true);
    }
    
    public boolean isBackedBy(String userId) {
        return backers != null && backers.containsKey(userId);
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    
    public ArrayList<String> getAdditionalImages() {
        return additionalImages;
    }
    
    public void setAdditionalImages(ArrayList<String> additionalImages) {
        this.additionalImages = additionalImages;
    }
    
    public void addAdditionalImage(String imageBase64) {
        if (additionalImages == null) {
            additionalImages = new ArrayList<>();
        }
        additionalImages.add(imageBase64);
    }
    
    public int getAdditionalImagesCount() {
        return additionalImages == null ? 0 : additionalImages.size();
    }
    
    // получение всех изображений проекта
    public ArrayList<String> getAllImages() {
        ArrayList<String> allImages = new ArrayList<>();
        
        if (coverImage != null && !coverImage.isEmpty()) {
            allImages.add(coverImage);
        }
        
        if (additionalImages != null && !additionalImages.isEmpty()) {
            allImages.addAll(additionalImages);
        }
        
        return allImages;
    }
    
    // получение процента выполнения цели
    public int getProgressPercentage() {
        if (goalAmount <= 0) {
            return 0;
        }
        return (int) ((currentAmount / goalAmount) * 100);
    }
    
    // проверка собрал ли проект полную сумму
    public boolean isFullyFunded() {
        return currentAmount >= goalAmount;
    }
    
    // проверяет было ли отправлено уведомление о достижении цели проекта
    public boolean isNotifiedGoalReached() {
        return notifiedGoalReached;
    }
    
    // проверяет было ли отправлено уведомление о дедлайне за указанное количество дней
    public boolean isDeadlineNotified(int days) {
        switch (days) {
            case 7:
                return deadlineNotified7;
            case 3:
                return deadlineNotified3;
            case 1:
                return deadlineNotified1;
            default:
                return false;
        }
    }

    public void setNotifiedGoalReached(boolean notifiedGoalReached) {
        this.notifiedGoalReached = notifiedGoalReached;
    }
    
    public boolean isDeadlineNotified7() {
        return deadlineNotified7;
    }
    
    public void setDeadlineNotified7(boolean deadlineNotified7) {
        this.deadlineNotified7 = deadlineNotified7;
    }
    
    public boolean isDeadlineNotified3() {
        return deadlineNotified3;
    }
    
    public void setDeadlineNotified3(boolean deadlineNotified3) {
        this.deadlineNotified3 = deadlineNotified3;
    }
    
    public boolean isDeadlineNotified1() {
        return deadlineNotified1;
    }
    
    public void setDeadlineNotified1(boolean deadlineNotified1) {
        this.deadlineNotified1 = deadlineNotified1;
    }
    
    public ArrayList<String> getAnnouncements() {
        return announcements;
    }
    
    public void setAnnouncements(ArrayList<String> announcements) {
        this.announcements = announcements;
    }
    
    public void addAnnouncement(String announcementKey) {
        if (announcements == null) {
            announcements = new ArrayList<>();
        }
        announcements.add(announcementKey);
    }
    
    public boolean hasAnnouncements() {
        return announcements != null && !announcements.isEmpty();
    }
}