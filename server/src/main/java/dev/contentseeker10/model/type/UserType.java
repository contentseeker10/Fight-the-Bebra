package dev.contentseeker10.model.type;

public enum UserType {
    NONE("NONE"),
    GUEST("GUEST"),
    ADMIN("ADMIN");

    private final String type;

    UserType(String type) { this.type = type; }

    @Override
    public String toString() {
        return super.toString();
    }
}
