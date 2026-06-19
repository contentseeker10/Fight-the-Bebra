package dev.contentseeker10.server.pipeline;

import dev.contentseeker10.message.Message;
import dev.contentseeker10.network.coders.Decoder;
import dev.contentseeker10.network.context.RequestContext;

import java.util.concurrent.BlockingQueue;

public class Decryptor implements Runnable {

    private final BlockingQueue<RequestContext<byte[]>> inputQueue;
    private final BlockingQueue<RequestContext<Message>> outputQueue;

    public Decryptor(BlockingQueue<RequestContext<byte[]>> inputQueue, BlockingQueue<RequestContext<Message>> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    public void decrypt(RequestContext<byte[]> rawContext) throws InterruptedException {
        Message decodedMessage = Decoder.decode(rawContext.getBody());
        outputQueue.put(new RequestContext<>(decodedMessage, rawContext.getConnection()));
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestContext<byte[]> rawContext = inputQueue.take();
                decrypt(rawContext);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
