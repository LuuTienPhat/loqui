package com.example.loqui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ItemContainerRecentConversionBinding;
import com.example.loqui.listeners.ConversationListener;
import com.example.loqui.utils.Utils;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversationListener conversationListener;

    public RecentConversationAdapter(List<ChatMessage> chatMessages, ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    class ConversationViewHolder extends RecyclerView.ViewHolder {

        ItemContainerRecentConversionBinding binding;

        public ConversationViewHolder(@NonNull ItemContainerRecentConversionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage) {
            binding.imageProfile.setImageBitmap(Utils.getBitmapFromEncodedString(chatMessage.getConversationImage()));
            binding.tvTextName.setText(chatMessage.getConversationName());
            binding.tvRecentMessage.setText(chatMessage.getMessage());
            binding.getRoot().setOnClickListener(view -> {
                User user = new User();
                user.setId(chatMessage.getConversationId());
                user.setLastName(chatMessage.getConversationName());
//                user.setName(chatMessage.getConversationName());
                user.setImage(chatMessage.getConversationImage());
                conversationListener.onConversationListener(user);
            });

        }
    }
}
