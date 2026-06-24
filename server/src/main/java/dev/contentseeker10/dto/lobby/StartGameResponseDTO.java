package dev.contentseeker10.dto.lobby;

public record StartGameResponseDTO(
    boolean success,
    String error,
    String udpToken,
    int udpPort
) {}
