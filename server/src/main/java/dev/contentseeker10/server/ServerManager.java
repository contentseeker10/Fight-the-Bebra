package dev.contentseeker10.server;

import dev.contentseeker10.message.Message;
import dev.contentseeker10.network.context.ConnectionContext;
import dev.contentseeker10.network.context.RequestContext;
import dev.contentseeker10.network.tcp.ServerTCP;
import dev.contentseeker10.server.pipeline.Decryptor;
import dev.contentseeker10.server.pipeline.Encryptor;
import dev.contentseeker10.server.pipeline.Processor;
import dev.contentseeker10.server.pipeline.Sender;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerManager {

    private static final ServerManager INSTANCE = new ServerManager();
    private ServerManager() {}
    public static ServerManager getInstance() { return INSTANCE; }

    private static final int TCP_PORT = 9090;
    private ServerTCP tcpServer;
    private Thread tcpServerThread;

    private final BlockingQueue<RequestContext<byte[]>> rawQueue = new LinkedBlockingQueue<>(1000);
    private final BlockingQueue<RequestContext<Message>> decodedQueue = new LinkedBlockingQueue<>(1000);
    private final BlockingQueue<RequestContext<Message>> responseQueue = new LinkedBlockingQueue<>(1000);
    private final BlockingQueue<RequestContext<byte[]>> sendQueue = new LinkedBlockingQueue<>(1000);

    private final List<Thread> runningThreads = new ArrayList<>();

    public void start(int decryptors, int processors, int encryptors, int senders) {
        System.out.println("[SERVER] Starting server...");

        try {
            System.out.println("[SERVER] Starting TCP server..");
            ServerSocket serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("[SERVER] TCP Server: " + serverSocket);
            tcpServer = new ServerTCP(serverSocket);
            tcpServerThread = new Thread(tcpServer, "TCP-Server-Acceptor");
            tcpServerThread.start();
        } catch (IOException e) {
            System.err.println("Unable to start TCP server: " + e.getMessage());
        }
        
        startDecryptors(decryptors);
        startProcessors(processors);
        startEncryptors(encryptors);
        startSenders(senders);

        System.out.println("[SERVER] Server started successfully.");
    }

    private void startDecryptors(int threads) {
        for (int i = 0; i < threads; i++) {
            System.out.println("[SERVER] Starting Decryptor #" + i + "...");
            Thread t = new Thread(new Decryptor(rawQueue, decodedQueue), "Decryptor-Thread-" + i);
            t.start();
            runningThreads.add(t);
        }
    }

    private void startProcessors(int threads) {
        for (int i = 0; i < threads; i++) {
            System.out.println("[SERVER] Starting Processor #" + i + "...");
            Thread t = new Thread(new Processor(decodedQueue, responseQueue), "Processor-Thread-" + i);
            t.start();
            runningThreads.add(t);
        }
    }

    private void startEncryptors(int threads) {
        for (int i = 0; i < threads; i++) {
            System.out.println("[SERVER] Starting Encryptor #" + i + "...");
            Thread t = new Thread(new Encryptor(responseQueue, sendQueue), "Encryptor-Thread-" + i);
            t.start();
            runningThreads.add(t);
        }
    }

    private void startSenders(int threads) {
        for (int i = 0; i < threads; i++) {
            System.out.println("[SERVER] Starting Sender #" + i + "...");
            Thread t = new Thread(new Sender(sendQueue), "Sender-Thread-" + i);
            t.start();
            runningThreads.add(t);
        }
    }

    public void stop() {
        System.out.println("[SERVER] Stopping server...");

        stopTasks();
        stopTcpServer();

        System.out.println("[SERVER] Server is stopped.");
    }

    private void stopTasks() {
        for (Thread t : runningThreads) {
            t.interrupt();
        }
        for (Thread t : runningThreads) {
            try {
                System.out.println("[SERVER] Stopping task " + t.getName() + "...");
                t.join(1000);
            } catch (InterruptedException e) {
                System.err.println("[SERVER] Thread join interrupted.");
                Thread.currentThread().interrupt();
            }
        }
        runningThreads.clear();
    }

    private void stopTcpServer() {
        if (tcpServer != null) {
            System.out.println("[SERVER] Stopping TCP server...");
            tcpServer.stop();
        }
        if (tcpServerThread != null) {
            tcpServerThread.interrupt();
            try {
                tcpServerThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public BlockingQueue<RequestContext<byte[]>> getRawQueue() {
        return rawQueue;
    }

}
