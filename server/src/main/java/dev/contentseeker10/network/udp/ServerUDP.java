package dev.contentseeker10.network.udp;

import dev.contentseeker10.network.context.ConnectionContext;
import dev.contentseeker10.network.context.RequestContext;
import dev.contentseeker10.network.context.UdpConnectionContext;
import dev.contentseeker10.server.ServerManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerUDP implements Runnable {

    private final DatagramSocket serverSocket;
    private final int port;

    public ServerUDP(DatagramSocket serverSocket, int port) {
        this.serverSocket = serverSocket;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[65635];
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);

                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());

                ConnectionContext context = new UdpConnectionContext(serverSocket, packet.getAddress(), packet.getPort());

                ServerManager.getInstance().getRawQueue().put(new RequestContext<>(data, context));
            }
        } catch (SocketException _) {

        } catch (IOException e) {
            System.err.println("[SERVER UDP] Error receiving packet: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        serverSocket.close();
    }
}
