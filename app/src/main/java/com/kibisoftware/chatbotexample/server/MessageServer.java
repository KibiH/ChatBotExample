package com.kibisoftware.chatbotexample.server;


import android.util.Log;

import com.kibisoftware.chatbotexample.chatmodel.ChatMessage;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

// this is a dummy class to represent a server
// since it is representing a remote server all calls initially block with a Thread.sleep(2000)
// it's currently a very simple state machine - it receives messages in JSON format and replies in
// JSON format
public class MessageServer {

    public static final String RESPONSE_NONE = "none";
    public static final String RESPONSE_TEXT = "text";
    public static final String RESPONSE_NUMBER = "number";
    public static final String RESPONSE_SELECT = "select";

    private static String botName = "Kibi";

    private ServerCallback callback;

    public void processMessage(String incomingMessage, ServerCallback callback) {
        this.callback = callback;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            //ignore
        }

        ChatMessage incoming = fromJson(incomingMessage);
        if (incoming == null) {
            callback.Reply(errorString());
            return;
        }

        dealWithMessage(incoming);
    }

    private ChatMessage fromJson(String messageJson) {
        ChatMessage ret = null;

        JSONParser parser = new JSONParser();
        try {
            JSONObject json = (JSONObject)parser.parse(messageJson);
            String message = (String)json.get("message");
            long step = (long) json.get("step");

            ret = new ChatMessage(message, ChatMessage.Type.RECEIVED,
                    (int)step, null, null);
        } catch (ParseException pe) {
            Log.e("JSONException", "Failed to parse json: " + pe.getMessage(), pe);
        }
        return ret;
    }

    private String toJson(ChatMessage outgoing) {
        String jsonStr = null;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", outgoing.getMessage());
        jsonObject.put("step", outgoing.getStep());
        jsonObject.put("responseType", outgoing.getResponseType());
        switch (outgoing.getResponseType()) {
            case RESPONSE_NONE:
                break;
            case RESPONSE_TEXT:
                break;
            case RESPONSE_NUMBER:
                break;
            case RESPONSE_SELECT:
                JSONArray list = new JSONArray();
                for (int  i = 0; i < outgoing.getResponses().length; i++) {
                    list.add(outgoing.getResponses()[i]);
                }
                jsonObject.put("responses", list);
                break;
        }
            jsonStr = jsonObject.toString();

        return jsonStr;
    }

    private String errorString() {
        ChatMessage errorMessage = new ChatMessage("An error occured",
                ChatMessage.Type.RECEIVED, Step.ERROR,
                RESPONSE_NONE, null);
        return toJson(errorMessage);
    }

    private void dealWithMessage(ChatMessage incoming) {
        respondToStep(incoming.getStep(), incoming.getMessage());
    }

    private void respondToStep(long step, String incomingMessage) {
        // we have the state mechine here:
        // currently five steps
        String replyStr;
        ChatMessage outGoingChat;
        Step myStep = Step.valueOf(step);
        switch (myStep) {
            case STEPONE:
                // this is actually the initialization from the client, no message to read
                // so we simply send the initial hallo messages
                outGoingChat = new ChatMessage("Hello, I am " + botName,
                        ChatMessage.Type.RECEIVED, Step.STEPTWO,
                        RESPONSE_NONE, null);
                replyStr = toJson(outGoingChat);
                callback.Reply(replyStr);
                outGoingChat = new ChatMessage("What is your name?",
                        ChatMessage.Type.RECEIVED, Step.STEPTWO,
                        RESPONSE_TEXT, null);
                replyStr = toJson(outGoingChat);
                callback.Reply(replyStr);
                break;
            case STEPTWO:
                // we should receive a string with the user's name
                outGoingChat = new ChatMessage("Nice to meet you " + incomingMessage + " :)",
                        ChatMessage.Type.RECEIVED, Step.STEPTHREE,
                        RESPONSE_NONE, null);
                replyStr = toJson(outGoingChat);
                callback.Reply(replyStr);
                outGoingChat = new ChatMessage("What is your phone number?",
                        ChatMessage.Type.RECEIVED, Step.STEPTHREE,
                        RESPONSE_NUMBER, null);
                replyStr = toJson(outGoingChat);
                callback.Reply(replyStr);
                break;
            case STEPTHREE:
                // ask about terms of service
                String choices[] = {"NO", "YES"};
                outGoingChat = new ChatMessage("Do you agree to our terms of service?",
                        ChatMessage.Type.RECEIVED, Step.STEPFOUR,
                        RESPONSE_SELECT, choices);
                replyStr = toJson(outGoingChat);
                callback.Reply(replyStr);
                break;
            case STEPFOUR:
                // should have response either NO or YES
                if (incomingMessage.equals("NO")) {
                    respondToStep(Step.STEPFIVE.getValue(), "EXIT");
                } else if (incomingMessage.equals("YES")) {
                    // ask about terms of service
                    String restart[] = {"RESTART", "EXIT"};
                    outGoingChat = new ChatMessage("Thanks!",
                            ChatMessage.Type.RECEIVED, Step.STEPFIVE,
                            RESPONSE_NONE, null);
                    replyStr = toJson(outGoingChat);
                    callback.Reply(replyStr);
                    outGoingChat = new ChatMessage("This is the last step!",
                            ChatMessage.Type.RECEIVED, Step.STEPFIVE,
                            RESPONSE_NONE, null);
                    replyStr = toJson(outGoingChat);
                    callback.Reply(replyStr);
                    outGoingChat = new ChatMessage("What do you want to do now?",
                            ChatMessage.Type.RECEIVED, Step.STEPFIVE,
                            RESPONSE_SELECT, restart);
                    replyStr = toJson(outGoingChat);
                    callback.Reply(replyStr);
                } else {
                    replyStr = errorString();
                    callback.Reply(replyStr);
                }
                break;
            case STEPFIVE:
                // check if we should restart
                if (incomingMessage.equals("RESTART")) {
                    respondToStep(Step.STEPONE.getValue(), "");
                } else {
                    outGoingChat = new ChatMessage("Bye Bye!",
                            ChatMessage.Type.RECEIVED, Step.ERROR,
                            RESPONSE_NONE, null);
                    replyStr = toJson(outGoingChat);
                    callback.Reply(replyStr);
                }
                break;
            default:
                // error case
                callback.Reply(errorString());
        }
    }

    public enum Step {
        STEPONE(1),
        STEPTWO(2),
        STEPTHREE(3),
        STEPFOUR(4),
        STEPFIVE(5),
        ERROR(6);

        private long value;
        private static Map map = new HashMap<>();

        Step(long i) {
            this.value = i;
        }

        static {
            for (Step pageType : Step.values()) {
                map.put(pageType.value, pageType);
            }
        }

        public static Step valueOf(long pageType) {
            return (Step) map.get(pageType);
        }

        public long getValue() {
            return value;
        }
    };


}
