package com.example.loqui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.adapter.ConversationAdapter;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.databinding.ActivityMessageRequestBinding;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageRequestActivity extends AppCompatActivity implements ConversationAdapter.Listener {

    private ActivityMessageRequestBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String userId;
    private List<ChatMessage> chatMessages;
    private ConversationAdapter conversationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
        setListeners();
        getRequestedMessages();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        userId = preferenceManager.getString(Keys.KEY_USER_ID);

        chatMessages = new ArrayList<>();
//        chatAdapter = new ChatAdapter(
//                chatMessages,
//                preferenceManager.getString(Keys.KEY_USER_ID),
//                Utils.getBitmapFromEncodedString(receivedUser.getImage())
//        );
        conversationAdapter = new ConversationAdapter(this, chatMessages, this);
        binding.rvMessageRequest.setAdapter(conversationAdapter);

        database.collection(Keys.KEY_COLLECTION_ROOM)
                .whereEqualTo(Keys.KEY_STATUS, RoomStatus.REQUESTED)
                .addSnapshotListener(listenRequestedChats);
    }

    private void setListeners() {
        binding.swAllow.setChecked(preferenceManager.getBoolean(Keys.KEY_SETTING_MESSAGE_REQUEST));

        binding.swAllow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.putBoolean(Keys.KEY_SETTING_MESSAGE_REQUEST, isChecked);
            binding.swAllow.setChecked(isChecked);

            if (isChecked) {
                getRequestedMessages();
            } else {
                binding.rvMessageRequest.setVisibility(View.GONE);
            }

            HashMap<String, Object> setting = new HashMap<>();
            setting.put(Keys.KEY_SETTING_MESSAGE_REQUEST, isChecked);
            setting.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

            database.collection(Keys.KEY_COLLECTION_SETTINGS)
                    .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            queryDocumentSnapshots.getDocuments().get(0).getReference().update(setting);
                            MyToast.showLongToast(this.getApplicationContext(), "Setting changed");
                        }
                    });

//            FirebaseHelper.findUser(database, preferenceManager.getString(Keys.KEY_USER_ID))
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                            DocumentReference reference = queryDocumentSnapshots.getDocuments().get(0).getReference();
//                            reference.update(Keys.KEY_SETTING_MESSAGE_REQUEST, isChecked);
//
//                        }
//                    });
        });
    }

    private void getRequestedMessages() {
        loading(true);
        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        List<String> rooms = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            rooms.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
                        }

                        database.collection(Keys.KEY_COLLECTION_ROOM)
                                .whereIn(Keys.KEY_ID, rooms)
                                .whereEqualTo(Keys.KEY_STATUS, RoomStatus.REQUESTED)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                        List<String> hiddenRoom = new ArrayList<>();
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                            hiddenRoom.add(documentSnapshot.getString(Keys.KEY_ID));
                                        }

                                        database.collection(Keys.KEY_COLLECTION_CHAT)
                                                .whereIn(Keys.KEY_ROOM_ID, hiddenRoom)
                                                .orderBy(Keys.KEY_CREATED_DATE, Query.Direction.DESCENDING)
                                                .limit(1)
                                                .get()
                                                .addOnSuccessListener(queryDocumentSnapshots2 -> {

                                                    if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
                                                        for (DocumentSnapshot snapshot : queryDocumentSnapshots2.getDocuments()) {
                                                            ChatMessage chatMessage = new ChatMessage();
                                                            chatMessage.setId(snapshot.getString(Keys.KEY_ID));
                                                            chatMessage.setRoomId(snapshot.getString(Keys.KEY_ROOM_ID));
                                                            chatMessage.setUserId(snapshot.getString(Keys.KEY_USER_ID));
                                                            chatMessage.setMessage(snapshot.getString(Keys.KEY_MESSAGE));
                                                            chatMessage.setType(snapshot.getString(Keys.KEY_TYPE));
                                                            chatMessage.setStatus(snapshot.getString(Keys.KEY_STATUS));
                                                            chatMessage.setCreatedDate(snapshot.get(Keys.KEY_CREATED_DATE).toString());
                                                            chatMessage.setModifiedDate(snapshot.get(Keys.KEY_MODIFIED_DATE).toString());
                                                            chatMessages.add(chatMessage);
                                                        }

                                                        conversationAdapter.notifyItemRangeInserted(0, chatMessages.size());
                                                    }
                                                    loading(false);
                                                })
                                                .addOnFailureListener(e -> {

                                                });
                                    } else {
                                        loading(false);
                                    }

                                });
                    } else {
                        loading(false);
                    }
                });

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private final EventListener<QuerySnapshot> listenConversation = (value, error) -> {
//        loading(true);
        if (error != null) {
//            loading(false);
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            chatMessages.clear();
            conversationAdapter.notifyItemRangeRemoved(0, count);

            Set<String> filter = new HashSet<>();

            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                String roomId = queryDocumentSnapshot.getString(Keys.KEY_ROOM_ID);

                if (!filter.contains(roomId)) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(queryDocumentSnapshot.getString(Keys.KEY_ID));
                    chatMessage.setUserId(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));
                    chatMessage.setRoomId(roomId);
                    chatMessage.setMessage(queryDocumentSnapshot.getString(Keys.KEY_MESSAGE));
                    chatMessage.setReplyId(queryDocumentSnapshot.getString(Keys.KEY_REPLY_ID));
                    chatMessage.setStatus(queryDocumentSnapshot.getString(Keys.KEY_STATUS));
                    chatMessage.setType(queryDocumentSnapshot.getString(Keys.KEY_TYPE));
                    chatMessage.setCreatedDate(queryDocumentSnapshot.getString(Keys.KEY_CREATED_DATE));
                    chatMessage.setModifiedDate(queryDocumentSnapshot.getString(Keys.KEY_MODIFIED_DATE));
                    chatMessages.add(chatMessage);

                    filter.add(roomId);
                }

            }
            conversationAdapter.notifyItemRangeRemoved(0, chatMessages.size());
            conversationAdapter.notifyItemRangeInserted(0, chatMessages.size());
        }
        loading(false);
    };

    private final EventListener<QuerySnapshot> listenRequestedChats = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            List<String> rooms = new ArrayList<>();

            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                    Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                            .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                            .whereEqualTo(Keys.KEY_ROOM_ID, queryDocumentSnapshot.get(Keys.KEY_ID))
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                    rooms.add(documentSnapshot.getString(Keys.KEY_ID));
                                }
                            });

                    database.collection(Keys.KEY_COLLECTION_CHAT)
                            .whereEqualTo(Keys.KEY_ROOM_ID, queryDocumentSnapshot.get(Keys.KEY_ID))
                            .orderBy(Keys.KEY_CREATED_DATE, Query.Direction.DESCENDING)
                            .addSnapshotListener(listenConversation);
                }
//                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
//                    QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
//
////                        if (!queryDocumentSnapshot.getString(Keys.KEY_USER_ID).equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
//                    String messageId = queryDocumentSnapshot.getString(Keys.KEY_ID);
//                    int index = findMessage(this.chatMessages, messageId);
//                    if (index != -1) {
//                        ChatMessage chatMessage = chatMessages.get(index);
//                        chatMessage.setStatus(queryDocumentSnapshot.getString(Keys.KEY_STATUS));
//                        chatMessages.set(index, chatMessage);
//                    }
//                    //}
//
//                }
            }

//            Collections.sort(chatMessages, (t1, t2) -> t1.getCreatedDate().compareTo(t2.getCreatedDate()));
//            if (count == 0) {
//                chatAdapter.notifyDataSetChanged();
//            } else {
//                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
//                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
//            }
//            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

//        if (conversationId == null) {
//            checkForConversation();
//        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        getRequestedMessages();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void sendDialogResult(ConversationAdapter.Result result, ChatMessage chatMessage) {

    }
}