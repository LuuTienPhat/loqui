package com.example.loqui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ItemContainerUserBinding;
import com.example.loqui.listeners.UserListener;
import com.example.loqui.utils.Utils;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

//    public UsersAdapter(List<User> users) {
//        this.users = users;
//        userListener = null;
//    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding binding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(binding);
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

        ItemContainerUserBinding binding;

        public UserViewHolder(@NonNull ItemContainerUserBinding item) {
            super(item.getRoot());
            binding = item;
        }

        void setUserData(User user) {
            binding.tvTextName.setText(user.getFullName());
            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(user.getImage()));
            binding.getRoot().setOnClickListener(view -> userListener.onUserClicked(user));
        }
    }

//    private Bitmap getUserImage(String encodedImage) {
//        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//    }
}
