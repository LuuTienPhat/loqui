package com.example.loqui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ItemContainerUserAvatarBinding;
import com.example.loqui.listeners.UserListener;
import com.example.loqui.utils.Utils;

import java.util.List;

public class UserAvatarAdapter extends RecyclerView.Adapter<UserAvatarAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;

    public UserAvatarAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    public UserAvatarAdapter(List<User> users) {
        this.users = users;
        userListener = null;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserAvatarBinding itemContainerUserAvatarBinding = ItemContainerUserAvatarBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserAvatarBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserAvatarBinding binding;

        public UserViewHolder(@NonNull ItemContainerUserAvatarBinding item) {
            super(item.getRoot());
            binding = item;
        }

        void setUserData(User user) {
            Bitmap avatar = Utils.getBitmapFromEncodedString(user.getImage());
            if (avatar != null) {
                binding.ivAvatar.setImageBitmap(avatar);
            }
            binding.btnRemove.setOnClickListener(v -> {
                userListener.onUserRemoved(user);
            });
        }
    }
}
