package com.example.caffix;

public class User {
    private String userId;
    private String provider;
    private String registrationDate;
    private String lastLoginDate;
    private String email;

    // 생성자
    public User(String userId, String provider, String registrationDate, String lastLoginDate, String email) {
        this.userId = userId;
        this.provider = provider;
        this.registrationDate = registrationDate;
        this.lastLoginDate = lastLoginDate;
        this.email = email;
    }

    // Getter 및 Setter 메서드
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
