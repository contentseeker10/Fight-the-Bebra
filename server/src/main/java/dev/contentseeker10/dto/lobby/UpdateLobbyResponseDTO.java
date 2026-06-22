package dev.contentseeker10.dto.lobby;

import dev.contentseeker10.dto.UserDTO;

public record UpdateLobbyResponseDTO(boolean success, String error, UserDTO[] users) {
}
