package dev.contentseeker10.server.pipeline;

import dev.contentseeker10.message.Message;
import dev.contentseeker10.network.coders.Encoder;
import dev.contentseeker10.network.context.RequestContext;

import java.util.concurrent.BlockingQueue;

public class Encryptor implements Runnable {

    private final BlockingQueue<RequestContext<Message>> inputQueue;
    private final BlockingQueue<RequestContext<byte[]>> outputQueue;

    public Encryptor(BlockingQueue<RequestContext<Message>> inputQueue, BlockingQueue<RequestContext<byte[]>> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    public void encrypt(RequestContext<Message> responseContext) throws InterruptedException {
        byte[] encodedMessage = Encoder.encode(responseContext.getBody());
        outputQueue.put(new RequestContext<>(encodedMessage, responseContext.getConnection()));
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestContext<Message> responseContext = inputQueue.take();
                encrypt(responseContext);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
