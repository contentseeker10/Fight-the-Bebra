package dev.contentseeker10.services;

import dev.contentseeker10.model.Lobby;
import dev.contentseeker10.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyService {

    private static final LobbyService INSTANCE = new LobbyService();
    private LobbyService() {}
    public static LobbyService getInstance() { return INSTANCE; }

    private final Map<String, Lobby> activeLobbies = new ConcurrentHashMap<>();

    public void createLobby(User creator) {

    }

    public void enterLobby(User user) {

    }

    public void leaveLobby(User user) {

    }

    private static String generateCode() {

    }

}
