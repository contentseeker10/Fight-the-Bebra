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

    private static final Map<ConnectionContext, User> connectionToUser = new ConcurrentHashMap<>();
    private static final Map<User, ConnectionContext> userToConnection = new ConcurrentHashMap<>();

    public void newSession(User user, ConnectionContext context) {
        if (user == null || context == null) {
            return;
        }
        if (connectionToUser.putIfAbsent(context, user) == null && userToConnection.putIfAbsent(user, context) == null)
            System.out.println("[SERVER] New session started for user: " + user.getUsername() + " (" + user.getId() + ")");
    }

    public ConnectionContext getSession(User user) {
        if (user == null) {
            return null;
        }
        return userToConnection.get(user);
    }

    public User getSessionUser(ConnectionContext context) {
        if (context == null) {
            return null;
        }
        return connectionToUser.get(context);
    }

    public void endSession(ConnectionContext context) {
        if (context == null) {
            return;
        }
        User user = getSessionUser(context);
        if (connectionToUser.remove(context) != null && userToConnection.remove(user) != null)
            System.out.println("[SERVER] Session ended for user: " + user.getUsername() + " (" + user.getId() + ")");
    }

}
