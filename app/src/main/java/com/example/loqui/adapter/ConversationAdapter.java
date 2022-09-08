package com.example.loqui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loqui.constants.FriendStatus;
import com.example.loqui.constants.RoomType;
import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ItemContainerConversationBinding;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.UserViewHolder> {

    private final List<ChatMessage> chatMessages;
    private Activity activity;
    private final PreferenceManager preferenceManager;
    private Fragment fragment;
    private ConversationAdapter.Listener listener;

    public enum Result {
        LONG, SHORT
    }

    public interface Listener {
        void sendDialogResult(ConversationAdapter.Result result, ChatMessage chatMessage);
    }

    public ConversationAdapter(Activity activity, List<ChatMessage> chatMessages, ConversationAdapter.Listener listener) {
        this.activity = activity;
        this.chatMessages = chatMessages;
        this.preferenceManager = new PreferenceManager(activity.getApplicationContext());
        this.listener = listener;
    }

    public ConversationAdapter(Fragment fragment, List<ChatMessage> chatMessages) {
        this.fragment = fragment;
        this.chatMessages = chatMessages;
        this.preferenceManager = new PreferenceManager(fragment.requireContext().getApplicationContext());
        this.listener = (Listener) fragment;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerConversationBinding binding = ItemContainerConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private ItemContainerConversationBinding binding;
//        private PreferenceManager preferenceManager;

        public UserViewHolder(@NonNull ItemContainerConversationBinding item) {
            super(item.getRoot());
            binding = item;
        }

        private List<String> filter(HashMap<String, List<String>> data) {
            List<String> rooms = new ArrayList<>();

            for (String i : data.keySet()) {
                List<String> values = data.get(i);
                if (values.size() > 2) {
                    rooms.add(i);
                }
            }

            return rooms;
        }

        void setData(ChatMessage chatMessage) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
//            HashMap<String, List<String>> roomUsers = new HashMap<>();
//
//            database.collection(Keys.KEY_COLLECTION_RECIPIENT)
//                    .whereEqualTo(Keys.KEY_ROOM_ID, chatMessage.getRoomId())
//                    .get()
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        roomUsers.put(chatMessage.getRoomId(), new ArrayList<>());
//                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                            String roomId = documentSnapshot.getString(Keys.KEY_ROOM_ID);
//                            String userId = documentSnapshot.getString(Keys.KEY_USER_ID);
//                            if (!userId.equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
//                                List<String> u = roomUsers.get(roomId);
//                                u.add(userId);
//                                roomUsers.put(roomId, u);
//                            }
//                        }
//
//                        List<String> participants = roomUsers.get(chatMessage.getRoomId());
//
//                        if (participants.size() == 1) {
//                            String senderId = roomUsers.get(chatMessage.getRoomId()).get(0);
//                            if (senderId.equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
//                                String name = preferenceManager.getString(Keys.KEY_LASTNAME) + " " + preferenceManager.getString(Keys.KEY_FIRSTNAME);
//                                binding.tvName.setText(name);
//                                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(preferenceManager.getString(Keys.KEY_AVATAR)));
//                                String content = preferenceManager.getString(Keys.KEY_FIRSTNAME) + ": " + chatMessage.getMessage();
//                                binding.tvMessage.setText(content);
//                            } else {
//                                FirebaseHelper.findUser(database, senderId)
//                                        .addOnSuccessListener(queryDocumentSnapshots1 -> {
//                                            if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
//                                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots1.getDocuments().get(0);
//                                                String name = documentSnapshot.getString(Keys.KEY_LASTNAME) + " " + documentSnapshot.getString(Keys.KEY_FIRSTNAME);
//                                                binding.tvName.setText(name);
//                                                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(documentSnapshot.getString(Keys.KEY_AVATAR)));
//                                                String content = documentSnapshot.getString(Keys.KEY_FIRSTNAME) + ": " + chatMessage.getMessage();
//                                                binding.tvMessage.setText(content);
//                                            }
//                                        });
//                            }
//
//                        } else {
//                            database.collection(Keys.KEY_COLLECTION_ROOM).document(chatMessage.getRoomId()).get()
//                                    .addOnSuccessListener(documentSnapshot -> {
//                                        binding.tvName.setText(documentSnapshot.getString(Keys.KEY_NAME));
//                                        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(documentSnapshot.getString(Keys.KEY_AVATAR)));
//
//                                        if (chatMessage.getUserId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
//                                            String content = preferenceManager.getString(Keys.KEY_FIRSTNAME) + ": " + chatMessage.getMessage();
//                                            binding.tvMessage.setText(content);
//                                        } else {
//                                            FirebaseHelper.findUser(database, chatMessage.getUserId())
//                                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
//                                                        if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
//                                                            DocumentSnapshot snapshot = queryDocumentSnapshots1.getDocuments().get(0);
//                                                            String content = snapshot.getString(Keys.KEY_FIRSTNAME) + ": " + chatMessage.getMessage();
//                                                            binding.tvMessage.setText(content);
//                                                        }
//                                                    });
//                                        }
//                                    });
//                        }
//
//                    });

            // get conversation info
            database.collection(Keys.KEY_COLLECTION_ROOM)
                    .document(chatMessage.getRoomId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.getString(Keys.KEY_TYPE).equals(RoomType.GROUP)) {
                            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(documentSnapshot.getString(Keys.KEY_AVATAR)));
                            binding.tvName.setText(documentSnapshot.getString(Keys.KEY_NAME));

                            if (!chatMessage.getUserId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) { //nếu người nhắn khoing phải chính mình
                                FirebaseHelper.findUser(database, chatMessage.getUserId())
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                                DocumentSnapshot documentSnapshot1 = queryDocumentSnapshots.getDocuments().get(0);

                                                String content = documentSnapshot1.getString(Keys.KEY_FIRSTNAME) + ": " + chatMessage.getMessage();
                                                binding.tvMessage.setText(content);
                                            }
                                        });
                            }

                        } else {
                            database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                                    .whereEqualTo(Keys.KEY_ROOM_ID, chatMessage.getRoomId())
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                            String friendId = null;

                                            for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots.getDocuments()) {
                                                if (!documentSnapshot1.getString(Keys.KEY_USER_ID).equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                                                    friendId = documentSnapshot1.getString(Keys.KEY_USER_ID);
                                                }
                                            }

                                            String finalFriendId = friendId;

                                            //Kiểm tra xem người đó có chặn mình hay không
                                            database.collection(Keys.KEY_COLLECTION_FRIEND)
                                                    .whereEqualTo(Keys.KEY_USER_ID, friendId)
                                                    .whereEqualTo(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                                                    .get()
                                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                                        if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                                            DocumentSnapshot documentSnapshot1 = queryDocumentSnapshots1.getDocuments().get(0);
                                                            if (documentSnapshot1.getString(Keys.KEY_STATUS).equals(FriendStatus.BLOCKED)) { // nếu chặn
                                                                //binding.getRoot().setVisibility(View.GONE);
                                                                this.itemView.setVisibility(View.GONE);
                                                                ViewGroup.LayoutParams params = this.itemView.getLayoutParams();
                                                                params.height = 0;
                                                                params.width = 0;
                                                                this.itemView.setLayoutParams(params);
                                                            }

                                                            // nếu không chặn
                                                            else {

                                                                FirebaseHelper.findUser(database, finalFriendId)
                                                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                                            if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
                                                                                DocumentSnapshot documentSnapshot2 = queryDocumentSnapshots2.getDocuments().get(0);
                                                                                User user = new User();
                                                                                user.setLastName(documentSnapshot2.getString(Keys.KEY_LASTNAME));
                                                                                user.setFirstName(documentSnapshot2.getString(Keys.KEY_FIRSTNAME));

                                                                                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(documentSnapshot2.getString(Keys.KEY_AVATAR)));
                                                                                binding.tvName.setText(user.getFullName());

                                                                                if (!chatMessage.getUserId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) { //nếu người nhắn khoing phải chính mình
                                                                                    String content = documentSnapshot2.getString(Keys.KEY_FIRSTNAME) + ": " + chatMessage.getMessage();
                                                                                    binding.tvMessage.setText(content);
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        } else {
                                                            FirebaseHelper.findUser(database, finalFriendId)
                                                                    .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                                        if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
                                                                            DocumentSnapshot documentSnapshot2 = queryDocumentSnapshots2.getDocuments().get(0);
                                                                            User user = new User();
                                                                            user.setLastName(documentSnapshot2.getString(Keys.KEY_LASTNAME));
                                                                            user.setFirstName(documentSnapshot2.getString(Keys.KEY_FIRSTNAME));

                                                                            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(documentSnapshot2.getString(Keys.KEY_AVATAR)));
                                                                            binding.tvName.setText(user.getFullName());

                                                                            if (!chatMessage.getUserId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) { //nếu người nhắn khoing phải chính mình
                                                                                String content = documentSnapshot2.getString(Keys.KEY_FIRSTNAME) + ": " + chatMessage.getMessage();
                                                                                binding.tvMessage.setText(content);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });


                                        }
                                    });

                        }

                        if (chatMessage.getUserId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) { // nếu người chat là chính mình
                            String content = preferenceManager.getString(Keys.KEY_FIRSTNAME) + ": " + chatMessage.getMessage();
                            binding.tvMessage.setText(content);
                        }
                    });

            if (Utils.getShortenedReadableDate(Utils.currentTimeMillis()).equals(Utils.getShortenedReadableDate(chatMessage.getCreatedDate()))) {
                binding.tvDateTime.setText(Utils.getShortenedReadableTime(chatMessage.getCreatedDate()));
            } else {
                binding.tvDateTime.setText(Utils.getShortenedReadableDate(chatMessage.getCreatedDate()));
            }

            binding.getRoot().setOnClickListener(v -> {
                listener.sendDialogResult(Result.SHORT, chatMessage);
            });

            binding.getRoot().setOnLongClickListener(v -> {
                listener.sendDialogResult(Result.LONG, chatMessage);
                return true;
            });

        }
    }
}
