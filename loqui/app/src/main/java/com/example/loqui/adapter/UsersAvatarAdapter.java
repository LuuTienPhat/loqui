package com.example.loqui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ItemContainerUserAvatarBinding;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;

import java.util.List;

public class UsersAvatarAdapter extends RecyclerView.Adapter<UsersAvatarAdapter.UserViewHolder> {

    private final List<User> users;
    private final UsersAvatarAdapter.Listener listener;

    public enum Result {
        CLICKED, REMOVE
    }

    public interface Listener {
        void sendDialogResult(UsersAvatarAdapter.Result result, User user);
    }

    public UsersAvatarAdapter(List<User> users, UsersAvatarAdapter.Listener listener) {
        this.users = users;
        this.listener = listener;
    }

//    public UsersAdapter(List<User> users) {
//        this.users = users;
//        userListener = null;
//    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserAvatarBinding binding = ItemContainerUserAvatarBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(binding, parent);
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

        private ViewGroup parent;
        private PreferenceManager preferenceManager = null;
        private ItemContainerUserAvatarBinding binding;

        public UserViewHolder(@NonNull ItemContainerUserAvatarBinding item, ViewGroup parent) {
            super(item.getRoot());
            this.binding = item;
            this.parent = parent;
            this.preferenceManager = new PreferenceManager(parent.getContext().getApplicationContext());
        }

        void setUserData(User user) {
            if (user.getId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(preferenceManager.getString(Keys.KEY_AVATAR)));
                String fullName = preferenceManager.getString(Keys.KEY_LASTNAME) + " " + preferenceManager.getString(Keys.KEY_FIRSTNAME);
                binding.tvName.setText(fullName);
                binding.btnRemove.setVisibility(View.GONE);
//                binding.badge.setVisibility(View.VISIBLE);
            } else {
                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(user.getImage()));
//                if (user.getAvailability().equals(UserAvailability.UNAVAILABLE)) {
//                    binding.badge.setVisibility(View.GONE);
//                } else {
//                    binding.badge.setVisibility(View.VISIBLE);
//                }
                binding.tvName.setText(user.getFullName());

                binding.btnRemove.setOnClickListener(v -> {
                    listener.sendDialogResult(Result.REMOVE, user);
                });

            }

            binding.ivAvatar.setOnClickListener(v -> {
                listener.sendDialogResult(Result.CLICKED, user);
            });

//            binding.tvTextName.setText(user.getFullName());
//            binding.ivAvatar.setImageBitmap(getUserImage(user.getImage()));
//            binding.getRoot().setOnClickListener(view -> userListener.onUserClicked(user));
        }
    }
}
