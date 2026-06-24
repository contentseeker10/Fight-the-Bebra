package dev.contentseeker10.services;

import dev.contentseeker10.model.GameSession;
import dev.contentseeker10.network.context.UdpConnectionContext;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameService {

    private static final GameService INSTANCE = new GameService();
    private GameService() {}
    public static GameService getInstance() { return INSTANCE; }

    private final Map<String, GameSession> sessionsByLobby = new ConcurrentHashMap<>();
    private final Map<Integer, GameSession> sessionsByUser = new ConcurrentHashMap<>();
    private final Map<UdpConnectionContext, Integer> udpToUser = new ConcurrentHashMap<>();

    public GameSession createSession(String lobbyCode, int adminId, int guestId) {
        String adminToken = UUID.randomUUID().toString().substring(0, 8);
        String guestToken = UUID.randomUUID().toString().substring(0, 8);

        GameSession session = new GameSession(lobbyCode, adminId, guestId, adminToken, guestToken);
        
        sessionsByLobby.put(lobbyCode, session);
        sessionsByUser.put(adminId, session);
        sessionsByUser.put(guestId, session);
        
        return session;
    }

    public boolean registerUdpAddress(int userId, String token, UdpConnectionContext context) {
        GameSession session = sessionsByUser.get(userId);
        if (session == null) {
            return false;
        }

        if (userId == session.getAdminId() && token.equals(session.getAdminToken())) {
            session.setAdminUdp(context);
            udpToUser.put(context, userId);
            System.out.println("[GAME SERVICE] Registered UDP address for Admin (User: " + userId + ")");
            return true;
        } else if (userId == session.getGuestId() && token.equals(session.getGuestToken())) {
            session.setGuestUdp(context);
            udpToUser.put(context, userId);
            System.out.println("[GAME SERVICE] Registered UDP address for Guest (User: " + userId + ")");
            return true;
        }

        return false;
    }

    public Integer getUserIdByUdpContext(UdpConnectionContext context) {
        return udpToUser.get(context);
    }

    public UdpConnectionContext getTeammateUdpContext(int senderId) {
        GameSession session = sessionsByUser.get(senderId);
        if (session == null) {
            return null;
        }
        return (senderId == session.getAdminId()) ? session.getGuestUdp() : session.getAdminUdp();
    }

    public void cleanSession(int userId) {
        GameSession session = sessionsByUser.remove(userId);
        if (session != null) {
            sessionsByLobby.remove(session.getLobbyCode());
            int teammateId = (session.getAdminId() == userId) ? session.getGuestId() : session.getAdminId();
            sessionsByUser.remove(teammateId);
            
            if (session.getAdminUdp() != null) udpToUser.remove(session.getAdminUdp());
            if (session.getGuestUdp() != null) udpToUser.remove(session.getGuestUdp());
            
            System.out.println("[GAME SERVICE] Cleaned game session for lobby: " + session.getLobbyCode());
        }
    }
}
