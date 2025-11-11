package com.clipers.clipers.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserDTO user;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, UserDTO user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}
