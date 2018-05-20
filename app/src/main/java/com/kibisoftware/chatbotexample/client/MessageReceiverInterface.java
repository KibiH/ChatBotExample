package com.kibisoftware.chatbotexample.client;

import com.kibisoftware.chatbotexample.chatmodel.ChatMessage;

public interface MessageReceiverInterface {
    void MessageReceived(ChatMessage message);
}
