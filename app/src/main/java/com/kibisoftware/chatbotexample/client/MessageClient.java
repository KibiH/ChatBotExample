package com.kibisoftware.chatbotexample.client;

import android.os.AsyncTask;
import android.util.Log;

import com.kibisoftware.chatbotexample.chatmodel.ChatMessage;
import com.kibisoftware.chatbotexample.server.MessageServer;
import com.kibisoftware.chatbotexample.server.ServerCallback;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.ref.WeakReference;
import java.util.Iterator;

public class MessageClient implements ServerCallback {
    private MessageServer server;
    private MessageReceiverInterface receiver;

    public MessageClient(MessageServer server, MessageReceiverInterface receiver) {
        this.receiver = receiver;
        this.server = server;
    }

    public void sendMessage(ChatMessage message) {
        String jsonStr = null;
        String messageStr = message.getMessage();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", messageStr);
        jsonObject.put("step", message.getStep());
        jsonStr = jsonObject.toString();

        if (jsonStr == null) {
            // something went wrong, so bail
            return;
        }


        new MessageTask(server, this).execute(jsonStr);

    }

    @Override
    public void Reply(String replyJson) {
        // this string needs to be sent back to the receiver
        ChatMessage message = fromJson(replyJson);
        receiver.MessageReceived(message);
    }

    private ChatMessage fromJson(String incomingJson) {
        ChatMessage ret = null;

        JSONParser parser = new JSONParser();
        try {
            JSONObject json = (JSONObject)parser.parse(incomingJson);
            String message = (String)json.get("message");
            long step = (long) json.get("step");
            String responseType = (String) json.get("responseType");
            if (responseType == null) {
                responseType = MessageServer.RESPONSE_NONE;
            }
            String responseArr[] = null;
            switch (responseType) {
                case MessageServer.RESPONSE_NONE:
                case MessageServer.RESPONSE_TEXT:
                case MessageServer.RESPONSE_NUMBER:
                    break;
                case MessageServer.RESPONSE_SELECT:

                    JSONArray responses = (JSONArray) json.get("responses");
                    responseArr =  new String[responses.size()];
                    Iterator<String> iterator = responses.iterator();
                    int i = 0;
                    while (iterator.hasNext()) {
                        responseArr[i++] = iterator.next();
                    }
            }

            ret = new ChatMessage(message, ChatMessage.Type.RECEIVED,
                    step, responseType, responseArr);
        } catch (ParseException pe) {
            Log.e("JSONException", "Failed to parse json: " + pe.getMessage(), pe);
        }
        return ret;

    }

    private static class MessageTask extends AsyncTask<String, Void, Void> {
        private WeakReference<MessageServer> serverReference;
        private WeakReference<MessageClient> clientReference;

        // only retain a weak reference to the activity
        MessageTask(MessageServer server, MessageClient client) {
            serverReference = new WeakReference<>(server);
            clientReference = new WeakReference<>(client);
        }

        @Override
        protected Void doInBackground(String... strings) {
            // only actually ever dealing with one message at a time, so we don't cycle through the
            // strings, just take the first (only) one
            MessageServer server = serverReference.get();
            if (server == null) {
                return null;
            }
            MessageClient client = clientReference.get();
            if (client != null) {
                server.processMessage(strings[0], client);
            }
            return null;
        }

    }
}
