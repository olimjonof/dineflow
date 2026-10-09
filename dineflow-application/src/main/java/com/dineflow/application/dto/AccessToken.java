package com.dineflow.application.dto;

public record AccessToken(String token, long expiresInSeconds) {
}
