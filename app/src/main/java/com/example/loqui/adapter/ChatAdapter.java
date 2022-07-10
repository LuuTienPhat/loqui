package com.example.loqui.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.databinding.ItemContainerReceivedMessageBinding;
import com.example.loqui.databinding.ItemContainerSentMessageBinding;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final String senderId;
    private final Bitmap receivedProfileImage;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, Bitmap receivedProfileImage) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.receivedProfileImage = receivedProfileImage;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Object returnedViewHolder = null;
        if (viewType == VIEW_TYPE_SENT) {
            returnedViewHolder = new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(
                                    parent.getContext()),
                            parent,
                            false));
        } else {
            returnedViewHolder = new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(
                                    parent.getContext()),
                            parent,
                            false));
        }
        return (RecyclerView.ViewHolder) returnedViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receivedProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 0;
        if (chatMessages.get(position).getSenderId().equals(senderId)) {
            viewType = VIEW_TYPE_SENT;
        } else {
            viewType = VIEW_TYPE_RECEIVED;
        }
        return viewType;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMessageBinding binding;

        public SentMessageViewHolder(ItemContainerSentMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage) {
            binding.tvMessage.setText(chatMessage.getMessage());
            binding.tvDateTime.setText(chatMessage.getDateTime());
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage, Bitmap receivedProfileImage) {
            binding.tvMessage.setText(chatMessage.getMessage());
            binding.tvDateTime.setText(chatMessage.getDateTime());
            binding.imageProfile.setImageBitmap(receivedProfileImage);
        }


    }
}
