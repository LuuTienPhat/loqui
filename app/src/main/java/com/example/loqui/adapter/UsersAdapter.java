package com.example.loqui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.constants.FriendStatus;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ItemContainerUserBinding;
import com.example.loqui.listeners.UserListener;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;
    private Boolean isOpen;
    private PreferenceManager preferenceManager;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
        this.isOpen = false;
        this.preferenceManager = null;
    }

    public UsersAdapter(List<User> users, UserListener userListener, PreferenceManager preferenceManager, Boolean isOpen) {
        this.users = users;
        this.userListener = userListener;
        this.isOpen = isOpen;
        this.preferenceManager = preferenceManager;
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

            if (isOpen) {
                binding.tvTextDescription.setVisibility(View.VISIBLE);
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(Keys.KEY_COLLECTION_FRIEND)
                        .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                        .whereEqualTo(Keys.KEY_FRIEND_ID, user.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                                String friendStatus = documentSnapshot.getString(Keys.KEY_STATUS);
                                if (friendStatus.equals(FriendStatus.FRIEND)) {
                                    binding.tvTextDescription.setText("Friend");
                                }
                                if (friendStatus.equals(FriendStatus.NEW)) {
                                    binding.tvTextDescription.setText("You sent a friend request");
                                }
                            }
                        });

                database.collection(Keys.KEY_COLLECTION_FRIEND)
                        .whereEqualTo(Keys.KEY_USER_ID, user.getId())
                        .whereEqualTo(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                                String friendStatus = documentSnapshot.getString(Keys.KEY_STATUS);
                                if (friendStatus.equals(FriendStatus.NEW)) {
                                    binding.tvTextDescription.setText("They sent you a friend request");
                                }
                            }
                        });
            }
        }
    }

//    private Bitmap getUserImage(String encodedImage) {
//        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//    }
}
