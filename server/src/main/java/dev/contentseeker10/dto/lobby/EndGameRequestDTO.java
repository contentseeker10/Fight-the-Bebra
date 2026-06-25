package dev.contentseeker10.dto.lobby;

public record EndGameRequestDTO(
    String lobbyCode,
    int adminScore,
    int guestScore
) {}
