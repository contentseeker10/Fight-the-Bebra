package dev.contentseeker10.model;

import dev.contentseeker10.model.type.LobbyType;

public class Lobby {

    private final String code;

    private User admin;
    private User guest;

    private LobbyType type = LobbyType.WAITING;

    public Lobby(String code, User admin) {
        this.code = code;
        this.admin = admin;
    }

    public String getCode() {
        return code;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public User getGuest() {
        return guest;
    }

    public void setGuest(User guest) {
        this.guest = guest;
    }

    public LobbyType getType() {
        return type;
    }

    public void setType(LobbyType type) {
        this.type = type;
    }

}
