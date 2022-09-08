package com.example.loqui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.data.model.Recipient;
import com.example.loqui.databinding.ItemContainerUserNickNameBinding;
import com.example.loqui.listeners.UserNicknameListener;

import java.util.List;

public class UsersNickNameAdapter extends RecyclerView.Adapter<UsersNickNameAdapter.UserNicknameViewHolder> {

    private final List<Recipient> recipients;
    private final UserNicknameListener userNicknameListener;

    public UsersNickNameAdapter(List<Recipient> recipients, UserNicknameListener userNicknameListener) {
        this.recipients = recipients;
        this.userNicknameListener = userNicknameListener;
    }

    @NonNull
    @Override
    public UserNicknameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserNickNameBinding binding = ItemContainerUserNickNameBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserNicknameViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserNicknameViewHolder holder, int position) {
        holder.setUserData(recipients.get(position));
    }

    @Override
    public int getItemCount() {
        return recipients.size();
    }

    class UserNicknameViewHolder extends RecyclerView.ViewHolder {

        private ItemContainerUserNickNameBinding binding;

        public UserNicknameViewHolder(@NonNull ItemContainerUserNickNameBinding item) {
            super(item.getRoot());
            binding = item;
        }

        void setUserData(Recipient recipient) {
            //binding.tvName.setText(recipient.getUser().getName());
            binding.tvName.setText(recipient.getUser().getFullName());
            binding.imageProfile.setImageBitmap(getUserImage(recipient.getUser().getImage()));
            binding.tvNickname.setText(recipient.getNickname());
            binding.getRoot().setOnClickListener(view -> userNicknameListener.onUserNicknameClicked(recipient));
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
