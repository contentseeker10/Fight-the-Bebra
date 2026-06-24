package dev.contentseeker10.model;

import dev.contentseeker10.network.context.UdpConnectionContext;

public class GameSession {
    private final String lobbyCode;
    private final int adminId;
    private final int guestId;

    private final String adminToken;
    private final String guestToken;

    private volatile UdpConnectionContext adminUdp;
    private volatile UdpConnectionContext guestUdp;

    public GameSession(String lobbyCode, int adminId, int guestId, String adminToken, String guestToken) {
        this.lobbyCode = lobbyCode;
        this.adminId = adminId;
        this.guestId = guestId;
        this.adminToken = adminToken;
        this.guestToken = guestToken;
    }

    public String getLobbyCode() {
        return lobbyCode;
    }

    public int getAdminId() {
        return adminId;
    }

    public int getGuestId() {
        return guestId;
    }

    public String getAdminToken() {
        return adminToken;
    }

    public String getGuestToken() {
        return guestToken;
    }

    public UdpConnectionContext getAdminUdp() {
        return adminUdp;
    }

    public void setAdminUdp(UdpConnectionContext adminUdp) {
        this.adminUdp = adminUdp;
    }

    public UdpConnectionContext getGuestUdp() {
        return guestUdp;
    }

    public void setGuestUdp(UdpConnectionContext guestUdp) {
        this.guestUdp = guestUdp;
    }
}
