package com.kiddo.remotescreen.model;

public record LoginResponse(String token, long expiresIn, String fullName) {}
