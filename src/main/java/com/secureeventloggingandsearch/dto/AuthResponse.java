package com.secureeventloggingandsearch.dto;

public class AuthResponse {

    private String token;
    private String type = "Bearer";

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() { return token; }

    public String getType() { return type; }
}
