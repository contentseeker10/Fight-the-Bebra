package dev.contentseeker10.network;

import dev.contentseeker10.message.Message;

public interface ResponseListener {
    void onResponse(Message message);
}
