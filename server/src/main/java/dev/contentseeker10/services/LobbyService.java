package dev.contentseeker10.services;

import dev.contentseeker10.model.Lobby;
import dev.contentseeker10.model.User;
import dev.contentseeker10.model.type.LobbyType;
import dev.contentseeker10.model.type.UserType;
import net.bytebuddy.utility.RandomString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyService {

    private static final LobbyService INSTANCE = new LobbyService();
    private LobbyService() {}
    public static LobbyService getInstance() { return INSTANCE; }

    private final Map<String, Lobby> activeLobbies = new ConcurrentHashMap<>();

    // TODO: Make DTO for Lobby Response data and switch return type to it instead of String

    public String createLobby(User creator) {
        creator.setType(UserType.ADMIN);
        String code;
        Lobby lobby;
        do {
            code = generateCode();
            lobby = new Lobby(code, creator);
        } while (activeLobbies.putIfAbsent(code, lobby) != null);
        return code;
    }

    public String enterLobby(String lobbyCode, User user) {
        Lobby lobby = activeLobbies.get(lobbyCode);
        if (lobby == null) {
            return "Lobby not found";
        }
        synchronized (lobby) {
            if (lobby.getGuest() != null) {
                return "Room is full";
            }
            if (lobby.getType() != LobbyType.WAITING) {
                return "Game already started";
            }
            user.setType(UserType.GUEST);
            lobby.setGuest(user);
            return "Success";
        }
    }

    public String leaveLobby(String lobbyCode, User user) {
        Lobby lobby = activeLobbies.get(lobbyCode);
        if (lobby == null || lobby.getAdmin() == null) {
            return "Bad Request";
        }
        synchronized (lobby) {
            UserType userType = user.getType();
            if (userType == UserType.ADMIN) {
                lobby.getAdmin().setType(UserType.NONE);
                if (lobby.getGuest() != null) {
                    lobby.getGuest().setType(UserType.ADMIN);
                    lobby.setAdmin(lobby.getGuest());
                    lobby.setGuest(null);
                } else {
                    lobby.setAdmin(null);
                    activeLobbies.remove(lobbyCode);
                }
            } else if (userType == UserType.GUEST) {
                lobby.getGuest().setType(UserType.NONE);
                lobby.setGuest(null);
            } else {
                return "Bad Request";
            }
            return "Success";
        }
    }

    private static String generateCode() {
        return RandomString.make(5).toUpperCase();
    }

}
