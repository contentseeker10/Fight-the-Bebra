package dev.contentseeker10.model;

import dev.contentseeker10.model.type.UserType;

public class User {

    private final int id;

    private UserType type = UserType.NONE;

    private String username;

    private volatile int recordScore;
    private volatile int deathCount;

    public User(int id, String username, int recordScore, int deathCount) {
        this.id = id;
        this.username = username;
        this.recordScore = recordScore;
        this.deathCount = deathCount;
    }

    public int getId() {
        return id;
    }

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRecordScore() {
        return recordScore;
    }

    public void setRecordScore(int recordScore) {
        this.recordScore = recordScore;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = deathCount;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", recordScore=" + recordScore +
                ", deathCount=" + deathCount +
                '}';
    }
}
