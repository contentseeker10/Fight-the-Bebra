package dev.contentseeker10.services;

import dev.contentseeker10.dto.UserDTO;
import dev.contentseeker10.dto.lobby.CreateLobbyResponseDTO;
import dev.contentseeker10.dto.lobby.JoinLobbyResponseDTO;
import dev.contentseeker10.dto.lobby.LeaveLobbyResponseDTO;
import dev.contentseeker10.model.Lobby;
import dev.contentseeker10.model.User;
import dev.contentseeker10.model.type.LobbyType;
import dev.contentseeker10.model.type.UserType;
import java.security.SecureRandom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyService {

    private static final LobbyService INSTANCE = new LobbyService();
    private LobbyService() {}
    public static LobbyService getInstance() { return INSTANCE; }

    private final Map<String, Lobby> activeLobbies = new ConcurrentHashMap<>();

    public CreateLobbyResponseDTO createLobby(User creator) {
        if (creator == null) {
            return new CreateLobbyResponseDTO(false, "Bad Request", "");
        }
        creator.setType(UserType.ADMIN);
        String code;
        Lobby lobby;
        do {
            code = generateCode();
            lobby = new Lobby(code, creator);
        } while (activeLobbies.putIfAbsent(code, lobby) != null);
        return new CreateLobbyResponseDTO(true, "", code);
    }

    public JoinLobbyResponseDTO joinLobby(String lobbyCode, User user) {
        Lobby lobby = activeLobbies.get(lobbyCode);
        if (lobby == null) {
            return new JoinLobbyResponseDTO(false, "Lobby not found", null);
        }
        synchronized (lobby) {
            if (lobby.getGuest() != null) {
                return new JoinLobbyResponseDTO(false, "Room is full", null);
            }
            if (lobby.getType() != LobbyType.WAITING) {
                return new JoinLobbyResponseDTO(false, "Game already started", null);
            }
            user.setType(UserType.GUEST);
            lobby.setGuest(user);
            return new JoinLobbyResponseDTO(true, "", new UserDTO(lobby.getAdmin()));
        }
    }

    public LeaveLobbyResponseDTO leaveLobby(String lobbyCode, User user) {
        Lobby lobby = activeLobbies.get(lobbyCode);
        if (lobby == null || lobby.getAdmin() == null) {
            return new LeaveLobbyResponseDTO(false, "Bad Request");
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
                return new LeaveLobbyResponseDTO(false, "Bad Request");
            }
            return new LeaveLobbyResponseDTO(true, "");
        }
    }

    public void forceLeaveLobby(User user) {
        if (user == null) {
            return;
        }
        for (Map.Entry<String, Lobby> entry : activeLobbies.entrySet()) {
            Lobby lobby = entry.getValue();
            synchronized (lobby) {
                if (lobby.getAdmin() != null && lobby.getAdmin().equals(user)) {
                    leaveLobby(entry.getKey(), lobby.getAdmin());
                } else if (lobby.getGuest() != null && lobby.getGuest().equals(user)) {
                    leaveLobby(entry.getKey(), lobby.getGuest());
                }
            }
        }
    }

    public User getLobbyAdmin(String lobbyCode) {
        // Would be nice to have a validation of lobby code here, but I'll skip it
        return activeLobbies.get(lobbyCode).getAdmin();
    }

    public User getLobbyGuest(String lobbyCode) {
        // Would be nice to have a validation of lobby code here, but I'll skip it
        return activeLobbies.get(lobbyCode).getGuest();
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generateCode() {
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

}
