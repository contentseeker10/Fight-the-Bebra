package dev.contentseeker10.server.pipeline;

import dev.contentseeker10.message.CommandType;
import dev.contentseeker10.message.Message;
import dev.contentseeker10.message.Payload;
import dev.contentseeker10.network.context.RequestContext;
import dev.contentseeker10.services.AuthorizationService;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {

    private static final AuthorizationService authorizationService = AuthorizationService.getInstance();



    private final BlockingQueue<RequestContext<Message>> inputQueue;
    private final BlockingQueue<RequestContext<Message>> outputQueue;



    public Processor(BlockingQueue<RequestContext<Message>> inputQueue, BlockingQueue<RequestContext<Message>> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    public void process(RequestContext<Message> requestContext) {

    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestContext<Message> requestContext = inputQueue.take();
                process(requestContext);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
