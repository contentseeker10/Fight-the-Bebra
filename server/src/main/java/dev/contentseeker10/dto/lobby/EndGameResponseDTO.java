package dev.contentseeker10.dto.lobby;

import dev.contentseeker10.dto.UserDTO;

public record EndGameResponseDTO(
    boolean success,
    String error,
    int recordScore,
    UserDTO admin,
    UserDTO guest
) {}
