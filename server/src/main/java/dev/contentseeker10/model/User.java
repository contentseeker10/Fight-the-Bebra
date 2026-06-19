package dev.contentseeker10.model;

public class User {

    private final int id;

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
