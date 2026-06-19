package dev.contentseeker10.dto;

public record AuthResponseDTO(boolean success, String error, UserDTO user) {
}
