package dev.contentseeker10.dto.lobby;

import dev.contentseeker10.dto.UserDTO;

public record JoinLobbyResponseDTO(
    boolean success,
    String error,
    UserDTO admin,
    int recordScore
) {}
