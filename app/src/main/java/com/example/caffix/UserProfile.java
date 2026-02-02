package com.example.caffix;
public class UserProfile {
    private String name;
    private String email;
    private String profilePictureUrl; // 프로필 사진 URL 필드

    public UserProfile(String name, String email) {
        this.name = name;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
