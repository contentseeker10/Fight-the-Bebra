package dev.contentseeker10.services;

import dev.contentseeker10.model.User;
import dev.contentseeker10.network.context.ConnectionContext;
import dev.contentseeker10.network.context.TcpConnectionContext;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionService {

    private static final SessionService INSTANCE = new SessionService();
    private SessionService() {}
    public static SessionService getInstance() { return INSTANCE; }

    private static final Map<Integer, ConnectionContext> activeSessions = new ConcurrentHashMap<>();

    public void newSession(Integer userId, ConnectionContext context) {
        activeSessions.putIfAbsent(userId, context);
        System.out.println("[SERVER] New session started for userId: " + userId);
    }

    public ConnectionContext getSession(Integer userId) {
        if (userId == null) {
            return null;
        }
        return activeSessions.get(userId);
    }

    public void endSession(Integer userId) {
        if (userId == null) {
            return;
        }
        activeSessions.remove(userId);
        System.out.println("[SERVER] Session ended for userId: " + userId);
    }

    public Integer getUserIdByConnection(ConnectionContext context) {
        if (context instanceof TcpConnectionContext tcpContext) {
            Socket socket = tcpContext.getSocket();
            for (Map.Entry<Integer, ConnectionContext> entry : activeSessions.entrySet()) {
                if (entry.getValue() instanceof TcpConnectionContext activeTcp) {
                    if (activeTcp.getSocket() == socket) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

}
