package com.taskportal.dto.auth;

public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String fullName;

    public AuthResponse() {}

    public AuthResponse(String token, String tokenType, Long userId, String email, String fullName) {
        this.token = token;
        this.tokenType = tokenType;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
    }

    public static AuthResponse of(String token, Long userId, String email, String fullName) {
        return new AuthResponse(token, "Bearer", userId, email, fullName);
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
