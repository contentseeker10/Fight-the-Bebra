package dev.contentseeker10.dto;

import dev.contentseeker10.model.User;

public record UserDTO(Integer id, String username, Integer recordScore, Integer deathCount) {
    public UserDTO(User user) {
        this(user.getId(), user.getUsername(), user.getRecordScore(), user.getDeathCount());
    }
}
