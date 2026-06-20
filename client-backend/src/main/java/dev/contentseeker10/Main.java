package dev.contentseeker10;

import dev.contentseeker10.network.tcp.ClientTCP;
import dev.contentseeker10.network.tcp.bridge.BridgeTCP;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    static void main() {
        try {
            ServerSocket bridgeSocket = new ServerSocket(9000);
            BridgeTCP bridgeTCP = new BridgeTCP(bridgeSocket);
            new Thread(bridgeTCP, "Bridge-Thread").start();
            System.out.println("[BRIDGE] TCP Bridge started on port 9000");
        } catch (IOException e) {
            System.err.println("[BRIDGE] Failed to start TCP Bridge: " + e.getMessage());
        }
    }
}
