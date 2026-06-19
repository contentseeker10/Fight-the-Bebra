package dev.contentseeker10.server.pipeline;

import dev.contentseeker10.network.context.RequestContext;

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

public class Sender implements Runnable {

    private final BlockingQueue<RequestContext<byte[]>> inputQueue;

    public Sender(BlockingQueue<RequestContext<byte[]>> inputQueue) {
        this.inputQueue = inputQueue;
    }

    public void sendMessage(RequestContext<byte[]> context, InetAddress target) {
        try {
            context.getConnection().sendResponse(context.getBody());
        } catch (Exception e) {
            System.err.println("[SERVER] Error sending response to client: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestContext<byte[]> rawContext = inputQueue.take();
                sendMessage(rawContext, null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
