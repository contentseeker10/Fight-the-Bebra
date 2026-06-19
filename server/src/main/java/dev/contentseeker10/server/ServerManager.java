package dev.contentseeker10.server;

public class ServerManager {

    private static final ServerManager INSTANCE = new ServerManager();
    private ServerManager() {}
    public static ServerManager getInstance() { return INSTANCE; }



}
