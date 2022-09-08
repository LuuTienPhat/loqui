package com.example.loqui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ItemContainerUserCheckBoxBinding;
import com.example.loqui.listeners.UserListener;
import com.example.loqui.utils.Utils;

import java.util.List;

public class UsersCheckboxAdapter extends RecyclerView.Adapter<UsersCheckboxAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;

    public UsersCheckboxAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    public UsersCheckboxAdapter(List<User> users) {
        this.users = users;
        userListener = null;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserCheckBoxBinding itemUserCheckboxBinding = ItemContainerUserCheckBoxBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemUserCheckboxBinding);
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

        ItemContainerUserCheckBoxBinding binding;

        public UserViewHolder(@NonNull ItemContainerUserCheckBoxBinding item) {
            super(item.getRoot());
            binding = item;
        }

        void setUserData(User user) {
            //binding.tvTextName.setText(user.getName());
            binding.tvTextName.setText(user.getFullName());
            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(user.getImage()));
            binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                userListener.onUserChecked(user, isChecked);
            });
            binding.getRoot().setOnClickListener(view -> {
                userListener.onUserChecked(user, binding.checkbox.isChecked());
            });
        }
    }
}
