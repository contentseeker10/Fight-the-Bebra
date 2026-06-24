package dev.contentseeker10.network.context;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpConnectionContext implements ConnectionContext {

    private final DatagramSocket serverSocket;

    private final InetAddress clientAddress;
    private final int clientPort;

    public UdpConnectionContext(DatagramSocket serverSocket, InetAddress clientAddress, int clientPort) {
        this.serverSocket = serverSocket;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }

    @Override
    public void sendResponse(byte[] data) throws Exception {
        DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
        serverSocket.send(packet);
    }

    public DatagramSocket getServerSocket() {
        return serverSocket;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UdpConnectionContext that = (UdpConnectionContext) o;

        if (clientPort != that.clientPort) return false;
        return clientAddress != null ? clientAddress.equals(that.clientAddress) : that.clientAddress == null;
    }

    @Override
    public int hashCode() {
        int result = clientAddress != null ? clientAddress.hashCode() : 0;
        result = 31 * result + clientPort;
        return result;
    }
}
