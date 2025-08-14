package classes;

public class Donate {
    private String key;
    private String userId;
    private String projectId;
    private String projectTitle;
    private double amount;
    private long date;

    public Donate() {
    }

    public Donate(String userId, String projectId, String projectTitle, double amount) {
        this.userId = userId;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.amount = amount;
        this.date = System.currentTimeMillis();
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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
