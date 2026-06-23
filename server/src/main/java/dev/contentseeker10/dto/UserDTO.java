package dev.contentseeker10.dto;

import dev.contentseeker10.model.User;
import dev.contentseeker10.model.type.UserType;

public record UserDTO(Integer id, UserType type, String username, Integer recordScore, Integer deathCount) {
    public UserDTO(User user) {
        this(user.getId(), user.getType(), user.getUsername(), user.getRecordScore(), user.getDeathCount());
    }
}
