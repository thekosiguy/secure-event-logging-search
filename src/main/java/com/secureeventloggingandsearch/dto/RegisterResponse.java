package com.secureeventloggingandsearch.dto;

public class RegisterResponse {

    private String username;
    private String role;

    public RegisterResponse(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() { return username; }

    public String getRole() { return role; }
}
