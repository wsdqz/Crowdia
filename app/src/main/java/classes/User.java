package classes;

import java.util.ArrayList;

public class User {
    public User() {
        projects = new ArrayList<>();
        donates = new ArrayList<>();
        logins = new ArrayList<>();
        admin = false;
    }
    private String key;
    private String username;
    private String email;
    private String password;
    private double balance;
    private String avatar;
    private ArrayList<String> projects;
    private ArrayList<String> donates;
    private ArrayList<Long> logins;
    private boolean admin;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public ArrayList<String> getProjects() {
        return projects;
    }

    public void setProjects(ArrayList<String> projects) {
        this.projects = projects;
    }

    public ArrayList<String> getDonates() {
        return donates;
    }

    public void setDonates(ArrayList<String> donates) {
        this.donates = donates;
    }

    public ArrayList<Long> getLogins() {
        return logins;
    }

    public void setLogins(ArrayList<Long> logins) {
        this.logins = logins;
    }
    
    public boolean isAdmin() {
        return admin;
    }
    
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}