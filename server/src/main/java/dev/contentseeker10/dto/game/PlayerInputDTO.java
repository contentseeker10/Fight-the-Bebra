package dev.contentseeker10.dto.game;

public record PlayerInputDTO(
    float x,
    float y,
    int hp,
    boolean isAttacking
) {}
