package com.kibisoftware.chatbotexample;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.kibisoftware.chatbotexample.chatmodel.ChatMessage;
import com.kibisoftware.chatbotexample.chatui.ChatAdapter;
import com.kibisoftware.chatbotexample.chatui.ConversationDivider;
import com.kibisoftware.chatbotexample.client.MessageClient;
import com.kibisoftware.chatbotexample.client.MessageReceiverInterface;
import com.kibisoftware.chatbotexample.server.MessageServer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MessageReceiverInterface {
    private RecyclerView chatView;
    private ChatAdapter chatAdapter;
    private RecyclerView.LayoutManager chatLayoutManager;

    private MessageServer server;
    private MessageClient client;

    private ImageView sendButton;
    private EditText inputBox;
    private Button button1;
    private Button button2;
    private long messageStep = MessageServer.Step.STEPONE.getValue();

    // don't want to implement different resources for portrait and
    // landscape, also don't want to look ugly when we rotate
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        chatView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatView = findViewById(R.id.chat_list);

        chatView.setHasFixedSize(true);

        chatLayoutManager = new LinearLayoutManager(this);
        chatView.setLayoutManager(chatLayoutManager);

        chatAdapter = new ChatAdapter(this);
        chatView.setAdapter(chatAdapter);

        ConversationDivider separator = new ConversationDivider(ContextCompat.getDrawable(this,R.drawable.conversation_separator), this);
        chatView.addItemDecoration(separator);

        inputBox = findViewById(R.id.inputBox);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // need to disable the send button for now
                sendButton.setEnabled(false);
                String messageText = inputBox.getText().toString();
                if (messageText.length() == 0) {
                    // ignore
                    return;
                }
                // empty out the text box
                inputBox.setText("");
                ChatMessage newMessage = new ChatMessage(messageText,
                        ChatMessage.Type.SENT, messageStep, null, null);
                sendMessage(newMessage);
            }
        });

        button1 = findViewById(R.id.choiceButton1);
        button2 = findViewById(R.id.choiceButton2);

        server = new MessageServer();
        client = new MessageClient(server, this);

        // just start up
        ChatMessage init = new ChatMessage("Initialization",
                ChatMessage.Type.SENT, messageStep, null, null);
        client.sendMessage(init);
    }

    @Override
    public void MessageReceived(final ChatMessage message) {
        //show it on screen
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatAdapter.addMessage(message);
                chatView.scrollToPosition(chatAdapter.getItemCount() - 1);

                messageStep = message.getStep();

                //check the response type
                switch (message.getResponseType()) {
                    case MessageServer.RESPONSE_TEXT:
                        //make visible and enable text and button, and set input type to a-z
                        setTextInput();
                        break;
                    case MessageServer.RESPONSE_NUMBER:
                        setNumberInput();
                        break;
                    case MessageServer.RESPONSE_SELECT:
                        setSelectInput(message.getResponses());
                        break;
                    case MessageServer.RESPONSE_NONE:
                        setNoInput();
                }
            }
        });
    }

    public long getItemStep(int position) {
        return chatAdapter.getItemStep(position);
    }

    private void setTextInput() {
        inputBox.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.VISIBLE);
        inputBox.setEnabled(true);
        sendButton.setEnabled(true);
        inputBox.setText("");
        ArrayList<InputFilter> curInputFilters = new ArrayList<InputFilter>();
        curInputFilters.add(0, new AlphabeticInputFilter());
        InputFilter[] newInputFilters = curInputFilters.toArray(new InputFilter[curInputFilters.size()]);
        inputBox.setFilters(newInputFilters);
        inputBox.setInputType(InputType.TYPE_CLASS_TEXT);
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
    }

    private void setNumberInput() {
        inputBox.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.VISIBLE);
        inputBox.setText("");
        inputBox.setEnabled(true);
        sendButton.setEnabled(true);
        ArrayList<InputFilter> curInputFilters = new ArrayList<InputFilter>();
        curInputFilters.add(0, new NumericInputFilter());
        InputFilter[] newInputFilters = curInputFilters.toArray(new InputFilter[curInputFilters.size()]);
        inputBox.setFilters(newInputFilters);
        inputBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
    }

    private void setSelectInput(String responses[]) {
        inputBox.setVisibility(View.GONE);
        sendButton.setVisibility(View.GONE);
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
        //prevent double taps
        button1.setEnabled(true);
        button2.setEnabled(true);

        // in a case with possibilities of more buttons we would do this in a loop
        button1.setText(responses[0]);
        button2.setText(responses[1]);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //prevent double taps
                button1.setEnabled(false);
                button2.setEnabled(false);
                ChatMessage newMessage = new ChatMessage(button1.getText().toString(),
                        ChatMessage.Type.SENT, messageStep, null, null);
                sendMessage(newMessage);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //prevent double taps
                button1.setEnabled(false);
                button2.setEnabled(false);
                ChatMessage newMessage = new ChatMessage(button2.getText().toString(),
                        ChatMessage.Type.SENT, messageStep, null, null);
                sendMessage(newMessage);
            }
        });

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setNoInput() {
        inputBox.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.VISIBLE);
        inputBox.setEnabled(false);
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void sendMessage(ChatMessage newMessage) {
        chatAdapter.addMessage(newMessage);
        client.sendMessage(newMessage);
        chatView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    public static class AlphabeticInputFilter implements InputFilter {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            // Only keep characters that are alphabetic
            StringBuilder builder = new StringBuilder();
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                /// allow spaces too
                if (Character.isLetter(c) || c == ' ') {
                    builder.append(c);
                }
            }

            // If all characters are valid, return null, otherwise only return the filtered characters
            boolean allCharactersValid = (builder.length() == end - start);
            return allCharactersValid ? null : builder.toString();
        }
    }

    public static class NumericInputFilter implements InputFilter {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            // Only keep characters that are alphabetic
            StringBuilder builder = new StringBuilder();
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (Character.isDigit(c)) {
                    builder.append(c);
                }
            }

            // If all characters are valid, return null, otherwise only return the filtered characters
            boolean allCharactersValid = (builder.length() == end - start);
            return allCharactersValid ? null : builder.toString();
        }
    }

}
