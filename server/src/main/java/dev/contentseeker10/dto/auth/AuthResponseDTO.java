package dev.contentseeker10.dto.auth;

import dev.contentseeker10.dto.UserDTO;

public record AuthResponseDTO(boolean success, String error, UserDTO user) {
}
