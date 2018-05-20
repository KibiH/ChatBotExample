package com.kibisoftware.chatbotexample.chatui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kibisoftware.chatbotexample.R;
import com.kibisoftware.chatbotexample.chatmodel.ChatMessage;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private ArrayList<ChatMessage> chatMessages;
    private Context context;
    private LayoutInflater inflater;

    public ChatAdapter(Context context) {
        this.chatMessages = new ArrayList<>();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyDataSetChanged();
    }

    public void clearMessages() {
        this.chatMessages.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getType().ordinal();
    }

    public long getItemStep(int position) {
        if (position > chatMessages.size() - 1 || position < 0) {
            return 0;
        }
        return chatMessages.get(position).getStep();
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        // create a new view
        if (viewType == ChatMessage.Type.SENT.ordinal()) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_layout, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.received_layout, parent, false);
        }
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.messageView.setText(chatMessages.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView messageView;
        public ViewHolder(View v)   {
            super(v);
            messageView = v.findViewById(R.id.message_text_view);
        }
    }
}
