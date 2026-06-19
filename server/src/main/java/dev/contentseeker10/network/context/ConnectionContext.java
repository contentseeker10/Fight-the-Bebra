package dev.contentseeker10.network.context;

public interface ConnectionContext {
    void sendResponse(byte[] data) throws Exception;
}
