package dev.contentseeker10.dto.game;

public record PlayerStateDTO(
    int userId,
    float x,
    float y,
    int hp,
    boolean isAttacking
) {}
