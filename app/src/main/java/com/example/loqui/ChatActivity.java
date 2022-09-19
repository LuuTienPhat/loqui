package com.example.loqui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.loqui.adapter.ChatAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.FriendStatus;
import com.example.loqui.constants.ImageExtension;
import com.example.loqui.constants.MessageStatus;
import com.example.loqui.constants.MessageType;
import com.example.loqui.constants.NotificationType;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.constants.RoomType;
import com.example.loqui.constants.UserAvailability;
import com.example.loqui.data.model.Attachment;
import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.data.model.Room;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityChatBinding;
import com.example.loqui.dialog.ChatOptionDialog;
import com.example.loqui.dialog.ExtraChatDialog;
import com.example.loqui.firebase.MessagingService;
import com.example.loqui.utils.CopyToClipBoard;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.SoftKeyboard;
import com.example.loqui.utils.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class ChatActivity extends BaseActivity implements ExtraChatDialog.Listener, ChatOptionDialog.Listener, ChatAdapter.Listener {

    private ActivityChatBinding binding;
    //    private User receivedUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    //    private String conversationId = null;
//    private Boolean isReceivedAvailable = false;
    private String roomId = null;
    private List<User> receivers;
    private ChatMessage replyMessage;
    private User me;
    private Room room;

    private boolean initFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        loadReceiverDetails();
        init();
        setListeners();
        onTask();

        me = new User();
        me.setId(preferenceManager.getString(Keys.KEY_USER_ID));
        me.setFirstName(preferenceManager.getString(Keys.KEY_FIRSTNAME));
        me.setLastName(preferenceManager.getString(Keys.KEY_LASTNAME));
        me.setToken(preferenceManager.getString(Keys.KEY_FCM_TOKEN));

        room = new Room();

//        getRoom(roomId);
    }

    private void onTask() {
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        //GET ROOM DETAIL FIRST
        database.collection(Keys.KEY_COLLECTION_ROOM)
                .document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot1 -> {
                    this.room.setId(documentSnapshot1.getString(Keys.KEY_ID));
                    this.room.setName(documentSnapshot1.getString(Keys.KEY_NAME));
                    this.room.setAvatar(documentSnapshot1.getString(Keys.KEY_AVATAR));
                    this.room.setType(documentSnapshot1.getString(Keys.KEY_TYPE));
                    this.room.setStatus(documentSnapshot1.getString(Keys.KEY_STATUS));
                    this.room.setAdminId(documentSnapshot1.getString(Keys.KEY_ADMIN_ID));
                    this.room.setCreatedDate(documentSnapshot1.getString(Keys.KEY_CREATED_DATE));
                    this.room.setModifiedDate(documentSnapshot1.getString(Keys.KEY_MODIFIED_DATE));

//                    if (this.receivers.size() > 1) {
//                        binding.tvName.setText(this.room.getName());
//                        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(documentSnapshot.getString(Keys.KEY_AVATAR)));
//                    }

                    if (this.room.getType().equals(RoomType.GROUP)) {
                        binding.tvName.setText(this.room.getName());
                        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(documentSnapshot1.getString(Keys.KEY_AVATAR)));
                    }

                    if (this.room.getType().equals(RoomType.TWO)) { // Phòng 2 người
                        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                                .whereEqualTo(Keys.KEY_ROOM_ID, roomId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<String> users = new ArrayList<>();

                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                        String id = documentSnapshot.getString(Keys.KEY_USER_ID);
                                        if (!id.equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                                            users.add(id);
                                        }
                                    }

                                    database.collection(Keys.KEY_COLLECTION_USERS)
                                            .whereIn(Keys.KEY_ID, users)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                                List<Task<QuerySnapshot>> taskList = new ArrayList<>();
                                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1) {
                                                    User user = new User();
                                                    user.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                    user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                                    user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                                                    user.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
                                                    user.setPhone(documentSnapshot.getString(Keys.KEY_PHONE));
                                                    user.setEmail(documentSnapshot.getString(Keys.KEY_EMAIL));
                                                    user.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
                                                    user.setAvailability(documentSnapshot.getString(Keys.KEY_AVAILABILITY));
                                                    receivers.add(user);
                                                }

                                                Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_FRIEND)
                                                        .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                                                        .whereEqualTo(Keys.KEY_FRIEND_ID, receivers.get(0).getId())
                                                        .get()
                                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                            if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
                                                                String status = queryDocumentSnapshots2.getDocuments().get(0).getString(Keys.KEY_STATUS);
                                                                if (status.equals(FriendStatus.BLOCKED)) {
                                                                    changeBlockUI(true);
                                                                } else {
                                                                    changeBlockUI(false);
                                                                }
                                                            }
                                                        });

                                                //taskList.add(t1);

                                                Tasks.whenAllComplete(t1)
                                                        .addOnSuccessListener(tasks1 -> {
                                                            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(receivers.get(0).getImage()));
                                                            binding.tvName.setText(this.receivers.get(0).getFullName());

//                                                            if (receivers.size() == 1) {
//                                                                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(receivers.get(0).getImage()));
//                                                                binding.tvName.setText(this.receivers.get(0).getFullName());
//                                                            } else if (receivers.size() == 0) {
////                                                binding.lyBlock.setVisibility(View.VISIBLE);
////                                                binding.footerView.setVisibility(View.GONE);
//
//                                                                binding.btnUnBlock.setOnClickListener(v -> {
//                                                                    unBlock();
//                                                                });
//                                                            }
                                                            chatAdapter = new ChatAdapter(this, chatMessages, receivers, preferenceManager, this);
                                                            binding.chatRecyclerView.setAdapter(chatAdapter);

//                                            getRoom(roomId);
                                                            listenMessages();
                                                            listenAvailabilityOfReceiver();
                                                        });

                                            });

                                });
                    } else { //Phòng nhiều người
                        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                                .whereEqualTo(Keys.KEY_ROOM_ID, roomId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<String> users = new ArrayList<>();

                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                        String id = documentSnapshot.getString(Keys.KEY_USER_ID);
                                        if (!id.equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                                            users.add(id);
                                        }
                                    }

                                    database.collection(Keys.KEY_COLLECTION_USERS)
                                            .whereIn(Keys.KEY_ID, users)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                                List<Task<QuerySnapshot>> taskList = new ArrayList<>();
                                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1) {

                                                    User user = new User();
                                                    user.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                    user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                                    user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                                                    user.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
                                                    user.setPhone(documentSnapshot.getString(Keys.KEY_PHONE));
                                                    user.setEmail(documentSnapshot.getString(Keys.KEY_EMAIL));
                                                    user.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
                                                    user.setAvailability(documentSnapshot.getString(Keys.KEY_AVAILABILITY));
                                                    receivers.add(user);

//                                                    Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_FRIEND)
//                                                            .whereEqualTo(Keys.KEY_FRIEND_ID, documentSnapshot.getString(Keys.KEY_ID))
//                                                            .get()
//                                                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
//                                                                if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
//                                                                    String status = queryDocumentSnapshots2.getDocuments().get(0).getString(Keys.KEY_STATUS);
//                                                                    if (!status.equals(FriendStatus.BLOCKED)) {
//                                                                        User user = new User();
//                                                                        user.setId(documentSnapshot.getString(Keys.KEY_ID));
//                                                                        user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
//                                                                        user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
//                                                                        user.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
//                                                                        user.setPhone(documentSnapshot.getString(Keys.KEY_PHONE));
//                                                                        user.setEmail(documentSnapshot.getString(Keys.KEY_EMAIL));
//                                                                        user.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
//                                                                        user.setAvailability(documentSnapshot.getString(Keys.KEY_AVAILABILITY));
//                                                                        receivers.add(user);
//                                                                    }
//                                                                }
//                                                            });
//
//                                                    taskList.add(t1);
                                                }

                                                Tasks.whenAllComplete(taskList)
                                                        .addOnSuccessListener(tasks1 -> {
                                                            if (receivers.size() == 1) {
                                                                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(receivers.get(0).getImage()));
                                                                binding.tvName.setText(this.receivers.get(0).getFullName());
                                                            } else if (receivers.size() == 0) {
//                                                binding.lyBlock.setVisibility(View.VISIBLE);
//                                                binding.footerView.setVisibility(View.GONE);

                                                                binding.btnUnBlock.setOnClickListener(v -> {
                                                                    unBlock();
                                                                });
                                                            }
                                                            chatAdapter = new ChatAdapter(this, chatMessages, receivers, preferenceManager, this);
                                                            binding.chatRecyclerView.setAdapter(chatAdapter);

//                                            getRoom(roomId);
                                                            listenMessages();
                                                            listenAvailabilityOfReceiver();
                                                        });

                                            })
                                            .addOnFailureListener(e -> {

                                            });
                                });
                    }
                });


    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        roomId = this.getIntent().getExtras().getString(Keys.KEY_ROOM_ID);

//        receivedUser = (User) getIntent().getSerializableExtra(Keys.KEY_USER);
        //binding.tvName.setText(receivedUser.getFullName());
        //binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(receivedUser.getImage()));
        chatMessages = new ArrayList<>();
        receivers = new ArrayList<>();

//        chatAdapter = new ChatAdapter(
//                chatMessages,
//                preferenceManager.getString(Keys.KEY_USER_ID),
//                Utils.getBitmapFromEncodedString(receivedUser.getImage())
//        );


//        if (receivers.size() == 1) {
//            binding.tvName.setText(receivedUser.getFullName());
//        }
    }

    private void getRoom(String roomId) {
        database.collection(Keys.KEY_COLLECTION_ROOM)
                .document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    this.room.setId(documentSnapshot.getString(Keys.KEY_ID));
                    this.room.setName(documentSnapshot.getString(Keys.KEY_NAME));
                    this.room.setAvatar(documentSnapshot.getString(Keys.KEY_AVATAR));
                    this.room.setType(documentSnapshot.getString(Keys.KEY_TYPE));
                    this.room.setStatus(documentSnapshot.getString(Keys.KEY_STATUS));
                    this.room.setAdminId(documentSnapshot.getString(Keys.KEY_ADMIN_ID));
                    this.room.setCreatedDate(documentSnapshot.getString(Keys.KEY_CREATED_DATE));
                    this.room.setModifiedDate(documentSnapshot.getString(Keys.KEY_MODIFIED_DATE));

                    if (this.receivers.size() > 1) {
                        binding.tvName.setText(this.room.getName());
                        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(documentSnapshot.getString(Keys.KEY_AVATAR)));
                    }
                });
    }

    private void loadReceiverDetails() {
//        binding.tvName.setText(receivedUser.getName());
    }

    private void setListeners() {
        binding.btnExtra.setOnClickListener(v -> {
            ExtraChatDialog extraChatDialog = new ExtraChatDialog(this.getApplicationContext());
            extraChatDialog.show(getSupportFragmentManager(), "");
        });
        binding.btnBack.setOnClickListener(view -> onBackPressed());
        binding.layoutSend.setOnClickListener(view -> {
            if (binding.inputMessage.getText().toString().trim().isEmpty()) {
                MyToast.showShortToast(this, "Please type message");
            } else {
                sendMessageNew(MessageType.MESSAGE, binding.inputMessage.getText().toString(), null);
            }

        });
        binding.btnCall.setOnClickListener(v -> {
            handleOnBtnCallClicked();
        });
        binding.btnCallVideo.setOnClickListener(v -> {
            handleOnBtnCallVideoClicked();
        });
        binding.btnInfo.setOnClickListener(v -> {
            handleOnBtnInfoClicked();
        });
    }

    private void listenAvailabilityOfReceiver() {
        if (receivers.size() == 0) {
            return;
        }

        List<String> ids = new ArrayList<>();
        for (User u : this.receivers) {
            ids.add(u.getId());
        }

        database.collection(Keys.KEY_COLLECTION_USERS)
                .whereIn(Keys.KEY_ID, ids)
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
//                        if (value.getLong(Keys.KEY_AVAILABILITY) != null) {
//                            int availability = Objects.requireNonNull(value.getLong(Keys.KEY_AVAILABILITY)).intValue();
//                            isReceivedAvailable = availability == 1;
//                        }
//                        receivedUser.setToken(value.getString(Keys.KEY_FCM_TOKEN));

                        List<String> availableUser = new ArrayList<>();

                        for (DocumentChange documentChange : value.getDocumentChanges()) {
                            QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
                            if (queryDocumentSnapshot.getString(Keys.KEY_AVAILABILITY) != null) {
                                String userId = queryDocumentSnapshot.getString(Keys.KEY_ID);
                                int index = findReceiver(userId);
                                User u = receivers.get(index);
                                u.setAvailability(queryDocumentSnapshot.getString(Keys.KEY_AVAILABILITY));
                                receivers.set(index, u);

                                if (u.getAvailability().equals(UserAvailability.AVAILABLE)) {
                                    availableUser.add(u.getFullName());
                                }
                            }
                        }

                        if (availableUser.size() == 1) {
                            binding.tvAvailability.setText("Online");
                            binding.tvAvailability.setVisibility(View.VISIBLE);
                        } else if (availableUser.size() > 1) {
                            String str = String.join(", ", availableUser);
                            binding.tvAvailability.setText(str + " are online");
                            binding.tvAvailability.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvAvailability.setVisibility(View.GONE);
                        }
                    }
                });

//        database.collection(Keys.KEY_COLLECTION_USERS)
//                .document(receivedUser.getId())
//                .addSnapshotListener(this, (value, error) -> {
//                    if (error != null) {
//                        return;
//                    }
//                    if (value != null) {
//                        if (value.getLong(Keys.KEY_AVAILABILITY) != null) {
//                            int availability = Objects.requireNonNull(value.getLong(Keys.KEY_AVAILABILITY)).intValue();
//                            isReceivedAvailable = availability == 1;
//                        }
//                        receivedUser.setToken(value.getString(Keys.KEY_FCM_TOKEN));
//                    }
//                    if (isReceivedAvailable) {
//                        binding.tvAvailability.setVisibility(View.VISIBLE);
//                    } else {
//                        binding.tvAvailability.setVisibility(View.GONE);
//                    }
//                });
    }

    private void listenMessages() {
//        database.collection(Keys.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Keys.KEY_SENDER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
//                .whereEqualTo(Keys.KEY_RECEIVER_ID, receivedUser.getId())
//                .addSnapshotListener(eventListener);
//
//        database.collection(Keys.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Keys.KEY_SENDER_ID, receivedUser.getId())
//                .whereEqualTo(Keys.KEY_RECEIVER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
//                .addSnapshotListener(eventListener);

        database.collection(Keys.KEY_COLLECTION_ROOM)
                .document(roomId).addSnapshotListener(listenRoom);

        database.collection(Keys.KEY_COLLECTION_CHAT)
                .whereEqualTo(Keys.KEY_ROOM_ID, roomId)
                .addSnapshotListener(messageListener);

    }

    private final EventListener<DocumentSnapshot> listenRoom = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            //getRoom(roomId);
            this.room.setId(value.getString(Keys.KEY_ID));
            this.room.setName(value.getString(Keys.KEY_NAME));
            this.room.setAvatar(value.getString(Keys.KEY_AVATAR));
            this.room.setStatus(value.getString(Keys.KEY_STATUS));
            this.room.setType(value.getString(Keys.KEY_TYPE));
            //this.room.setAdminId(value.getString(Keys.KEY_ADMIN_ID));
            this.room.setCreatedDate(value.getString(Keys.KEY_CREATED_DATE));
            this.room.setModifiedDate(value.getString(Keys.KEY_MODIFIED_DATE));
//
//            if (this.receivers.size() > 1) {
//                binding.tvName.setText(this.room.getName());
//                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(value.getString(Keys.KEY_AVATAR)));
//            }
        }
    };

    private final EventListener<QuerySnapshot> messageListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
//                        if (!queryDocumentSnapshot.getString(Keys.KEY_USER_ID).equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(queryDocumentSnapshot.getString(Keys.KEY_ID));
                    chatMessage.setUserId(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));
                    chatMessage.setRoomId(queryDocumentSnapshot.getString(Keys.KEY_ROOM_ID));
                    chatMessage.setMessage(queryDocumentSnapshot.getString(Keys.KEY_MESSAGE));
                    chatMessage.setReplyId(queryDocumentSnapshot.getString(Keys.KEY_REPLY_ID));
                    chatMessage.setStatus(queryDocumentSnapshot.getString(Keys.KEY_STATUS));
                    chatMessage.setType(queryDocumentSnapshot.getString(Keys.KEY_TYPE));
                    chatMessage.setCreatedDate(queryDocumentSnapshot.getString(Keys.KEY_CREATED_DATE));
                    chatMessage.setModifiedDate(queryDocumentSnapshot.getString(Keys.KEY_MODIFIED_DATE));

//                    if (chatMessage.getType().equals(MessageType.MEDIA)) {
//
//                    } else if (chatMessage.getType().equals(MessageType.FILE)) {
//
//                    }
                    chatMessages.add(chatMessage);
                    if (!initFlag) {
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    }

                    //}
                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(queryDocumentSnapshot.getString(Keys.KEY_ID));
                    chatMessage.setUserId(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));
                    chatMessage.setRoomId(queryDocumentSnapshot.getString(Keys.KEY_ROOM_ID));
                    chatMessage.setMessage(queryDocumentSnapshot.getString(Keys.KEY_MESSAGE));
                    chatMessage.setReplyId(queryDocumentSnapshot.getString(Keys.KEY_REPLY_ID));
                    chatMessage.setStatus(queryDocumentSnapshot.getString(Keys.KEY_STATUS));
                    chatMessage.setType(queryDocumentSnapshot.getString(Keys.KEY_TYPE));
                    chatMessage.setCreatedDate(queryDocumentSnapshot.getString(Keys.KEY_CREATED_DATE));
                    chatMessage.setModifiedDate(queryDocumentSnapshot.getString(Keys.KEY_MODIFIED_DATE));

                    int index = findMessage(this.chatMessages, chatMessage.getId());
                    if (index != -1) {
                        chatMessages.set(index, chatMessage);
                        if (!initFlag) {
                            chatAdapter.notifyItemChanged(index);
                        }
                    }
                }
            }

            if (initFlag) {
                Collections.sort(chatMessages, (t1, t2) -> t1.getCreatedDate().compareTo(t2.getCreatedDate()));
                if (count == 0) {
                    chatAdapter.notifyDataSetChanged();
                } else {
                    chatAdapter.notifyItemRangeRemoved(0, chatMessages.size());
                    chatAdapter.notifyItemRangeInserted(0, chatMessages.size());

                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                }

                initFlag = false;
            }

            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

//        if (conversationId == null) {
//            checkForConversation();
//        }
    };

    private void sendMessageNew(String messageType, String messageContent, String attachmentId) {
        HashMap<String, Object> message = new HashMap<>();
        String messageId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_CHAT);
        message.put(Keys.KEY_ID, messageId);
        message.put(Keys.KEY_ROOM_ID, roomId);
        message.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
        message.put(Keys.KEY_MESSAGE, messageContent);
        message.put(Keys.KEY_REPLY_ID, this.replyMessage == null ? "" : replyMessage.getId());
        message.put(Keys.KEY_STATUS, "");
        message.put(Keys.KEY_TYPE, messageType);
        message.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
        message.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//        message.put(Keys.KEY_SENDER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
//        message.put(Keys.KEY_RECEIVER_ID, receivedUser.getId());

//        message.put(Keys.KEY_TIMESTAMP, new Date());
        database.collection(Keys.KEY_COLLECTION_CHAT).document(messageId).set(message);

        if (messageType.equals(MessageType.MEDIA) || messageType.equals(MessageType.FILE)) {
            HashMap<String, Object> file = new HashMap<>();
            file.put(Keys.KEY_MESSAGE_ID, messageId);
            file.put(Keys.KEY_ATTACHMENT_ID, attachmentId);
            database.collection(Keys.KEY_COLLECTION_ATTS).add(file);
        }

        if (this.room.getStatus().equals(RoomStatus.ARCHIVED)) {
            database.collection(Keys.KEY_COLLECTION_ROOM).document(this.room.getId()).update(Keys.KEY_STATUS, RoomStatus.NEW);
        }
//                .addOnSuccessListener(documentReference -> {
//                    documentReference.update(Keys.KEY_ID, documentReference.getId());
//                });

//        if (conversationId != null) {
//            updateConversation(binding.inputMessage.getText().toString());
//        } else {
//            HashMap<String, Object> conversation = new HashMap<>();
//            conversation.put(Keys.KEY_SENDER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
//            conversation.put(Keys.KEY_SENDER_NAME, preferenceManager.getString(Keys.KEY_NAME));
//            conversation.put(Keys.KEY_SENDER_IMAGE, preferenceManager.getString(Keys.KEY_AVATAR));
//            conversation.put(Keys.KEY_RECEIVER_ID, receivedUser.getId());
////            conversation.put(Keys.KEY_RECEIVER_NAME, receivedUser.getName());
//            conversation.put(Keys.KEY_RECEIVER_IMAGE, receivedUser.getImage());
//            conversation.put(Keys.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
//            conversation.put(Keys.KEY_TIMESTAMP, new Date());
//            addConversation(conversation);
//        }

//        if (this.receivers.size() == 1) {
        try {
            JSONArray tokens = new JSONArray();
            for (User user : this.receivers) {
                tokens.put(user.getToken());
//                if (user.getAvailability().equals(UserAvailability.UNAVAILABLE)) {
//                    tokens.put(user.getToken());
//                }
            }

            JSONObject data = new JSONObject();
            data.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
            data.put(Keys.KEY_FIRSTNAME, preferenceManager.getString(Keys.KEY_FIRSTNAME));
            data.put(Keys.KEY_LASTNAME, preferenceManager.getString(Keys.KEY_LASTNAME));
            data.put(Keys.KEY_FCM_TOKEN, preferenceManager.getString(Keys.KEY_FCM_TOKEN));
            data.put(Keys.KEY_MESSAGE, messageContent);
            data.put(Keys.KEY_ROOM_ID, this.room.getId());

            data.put(Keys.KEY_NOTIFICATION_TYPE, NotificationType.MESSAGE);

            JSONObject body = new JSONObject();
            body.put(Keys.REMOTE_MSG_DATA, data);
            body.put(Keys.REMOTE_MSG_REGISTRATION_IDS, tokens);

            MessagingService.sendNotification(this.getApplicationContext(), body.toString());

        } catch (Exception ex) {
            MyToast.showShortToast(this, ex.getMessage());
        }
        //}
        binding.inputMessage.setText(null);
        this.replyMessage = null;
        updateUIReply(null, false);
        SoftKeyboard.hideSoftKeyboard(binding.inputMessage, this.getApplicationContext());
    }

    private void updateMessage(String messageId, String status) {
        if (status.equals(MessageStatus.DELETED)) {
            database.collection(Keys.KEY_COLLECTION_CHAT)
                    .document(messageId).update(
                            Keys.KEY_STATUS, MessageStatus.DELETED
                    );
        }
        if (status.equals(MessageStatus.FORWARDED)) {

        }
    }

    private void removeMessage(String roomId, String userId, String messageId) {

    }

    private ChatMessage findMessage(String roomId, String userId, String messageId) {
        ChatMessage chatMessage = new ChatMessage();
        database.collection(Keys.KEY_COLLECTION_CHAT)
                .whereEqualTo(Keys.KEY_ID, messageId)
                .whereEqualTo(Keys.KEY_USER_ID, userId)
                .whereEqualTo(Keys.KEY_ROOM_ID, roomId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() == 0) {
                            return;
                        } else {
                            chatMessage.setMessage(queryDocumentSnapshots.getDocuments().get(0).getString(Keys.KEY_MESSAGE));
                        }
                    }
                });

        return chatMessage;
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    private void handleOnBtnInfoClicked() {
        Intent intent = new Intent(this.getApplicationContext(), InfoActivity.class);
        intent.putExtra(Constants.ROOM, this.room);
        intent.putExtra(Constants.RECEIVERS, (Serializable) this.receivers);
        startActivity(intent);
    }

    private void handleOnBtnCallVideoClicked() {
        Intent intent = new Intent(this.getApplicationContext(), OutgoingCallActivity.class);
        intent.putExtra(Constants.CALL_TYPE, MessageType.VIDEO_CALL);
        intent.putExtra(Constants.ROOM, this.room);
        intent.putExtra(Constants.RECEIVERS, (Serializable) receivers);
        startActivity(intent);
//        Intent intent = new Intent(getApplicationContext(), JitsiActivity.class);
//        startActivity(intent);
    }

    private void handleOnBtnCallClicked() {
        Intent intent = new Intent(this.getApplicationContext(), OutgoingCallActivity.class);
        intent.putExtra(Constants.CALL_TYPE, MessageType.AUDIO_CALL);
        intent.putExtra(Constants.ROOM, this.room);
        intent.putExtra(Constants.RECEIVERS, (Serializable) receivers);
        startActivity(intent);

    }

//    public void sendLocation(LocationDetail locationDetail) {
//        HashMap<String, String> l = new HashMap<>();
//        l.put(Keys.KEY_ID, UUID.randomUUID().toString());
//        l.put(Keys.KEY_LAT, locationDetail.getLat());
//        l.put(Keys.KEY_LNG, locationDetail.getLng());
//        database.collection(Keys.KEY_COLLECTION_LOCATION).add(l);
//    }
//    }

    private void updateUIReply(ChatMessage chatMessage, boolean display) {
        if (display) {
            binding.lyReply.setVisibility(View.VISIBLE);
            binding.btnCancelReply.setOnClickListener(v -> {
                binding.lyReply.setVisibility(View.GONE);
                this.replyMessage = null;
            });

            //Reply to yourself
            if (chatMessage.getUserId().equals(me.getId())) {
                binding.tvReplyTo.setText(me.getFullName());
            } else {
                int userIndex = findReceiver(chatMessage.getUserId());
                binding.tvReplyTo.setText(this.receivers.get(userIndex).getFullName());
            }

            if (chatMessage.getStatus().equals(MessageStatus.DELETED)) {
                binding.tvReplyToMessage.setText("This message has been removed");
            } else {
                binding.tvReplyToMessage.setText(chatMessage.getMessage());
            }

            this.replyMessage = chatMessage;
        } else {
            binding.lyReply.setVisibility(View.GONE);
            this.replyMessage = null;
        }
    }


    @Override
    public void sendDialogResult(ChatOptionDialog.Result result, ChatMessage chatMessage) {
        if (result.equals(ChatOptionDialog.Result.COPY)) {
            CopyToClipBoard.doCopy(this.getApplicationContext(), chatMessage.getMessage());
        }
        if (result.equals(ChatOptionDialog.Result.REPLY)) {
            updateUIReply(chatMessage, true);
        }
        if (result.equals(ChatOptionDialog.Result.FORWARD)) {
            Intent intent = new Intent(this, ForwardActivity.class);
            intent.putExtra(Constants.CHAT_MESSAGE, chatMessage);
            startActivity(intent);
        }
        if (result.equals(ChatOptionDialog.Result.REMOVE)) {
            updateMessage(chatMessage.getId(), MessageStatus.DELETED);
        }
    }

    @Override
    public void sendDialogResult(ExtraChatDialog.Result result) {
        if (result.equals(ExtraChatDialog.Result.FILE)) {
            openFileDialog();
        } else if (result.equals(ExtraChatDialog.Result.LOCATION)) {
            sendLocation();
        } else if (result.equals(ExtraChatDialog.Result.CAMERA)) {
            Intent intent = new Intent(this, CropActivity.class);
            intent.putExtra("request", CropActivity.OPEN_CAMERA_CODE);
            startActivityForResult(intent, Constants.REQUEST_MEDIA);
        } else if (result.equals(ExtraChatDialog.Result.PHOTO)) {
            Intent intent = new Intent(this, CropActivity.class);
            intent.putExtra("request", CropActivity.OPEN_GALLERY_CODE);
//            startActivity(intent);
            startActivityForResult(intent, Constants.REQUEST_MEDIA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_MEDIA) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();

                uploadFile(uri);
            }
        }

    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = data.getData();

                        uploadFile(uri);
                    }
                }
            });

    private void openFileDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent = Intent.createChooser(intent, "Choose a file");
        resultLauncher.launch(intent);
    }

    private void uploadFile(Uri uploadFile) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        File f = null;
        try {
            f = Utils.getFile(getApplicationContext(), uploadFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String extension = f.getName().substring(f.getName().lastIndexOf(".") + 1);
        //String imageName = roomId + "-" + preferenceManager.getString(Keys.KEY_USER_ID) + "-" + Utils.currentTimeMillis() + "." + extension;
        String imageName = roomId + "-" + preferenceManager.getString(Keys.KEY_USER_ID) + "-" + Utils.currentTimeMillis();
        StorageReference imageRef = storageRef.child(roomId + "/" + imageName);
        UploadTask uploadTask = imageRef.putFile(uploadFile);

        uploadTask
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                //MyToast.showLongToast(this, uri.getPath());
                                String imageId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_ATTACHMENT);
                                HashMap<String, Object> attachment = new HashMap<>();
                                attachment.put(Keys.KEY_ID, imageId);
                                attachment.put(Keys.KEY_NAME, imageName);
                                attachment.put(Keys.KEY_SIZE, String.valueOf(taskSnapshot.getMetadata().getSizeBytes()));
                                attachment.put(Keys.KEY_PATH, uri.toString());
                                attachment.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
//                                Attachment attachment = new Attachment();
//                                attachment.setId(imageId);
//                                attachment.setName(imageName);
//                                attachment.setSize(String.valueOf(taskSnapshot.getMetadata().getSizeBytes()));
//                                attachment.setPath(uri.toString());
//                                attachment.setCreatedDate(Utils.currentTimeMillis());

                                if (extension.equals(ImageExtension.JPG) || extension.equals(ImageExtension.PNG)) {
                                    attachment.put(Keys.KEY_EXTENSION, ImageExtension.JPG);
//                                    attachment.setExtension(ImageExtension.JPG);
                                    sendMessageNew(MessageType.MEDIA, "send an image", imageId);
                                } else {
                                    attachment.put(Keys.KEY_EXTENSION, extension);
                                    //attachment.setExtension(extension);
                                    sendMessageNew(MessageType.FILE, "send an attachment", imageId);
                                }

                                database.collection(Keys.KEY_COLLECTION_ATTACHMENT).document(imageId).set(attachment);

                            })
                            .addOnFailureListener(e -> {
                                MyToast.showLongToast(this, e.getMessage());
                            });

                    //MyToast.showLongToast(this, "Uploaded");

                }).addOnFailureListener(e -> {
                    MyToast.showLongToast(this, e.getMessage());
                });

//        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//            @Override
//            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                if (!task.isSuccessful()) {
//                    throw task.getException();
//                }
//
//                // Continue with the task to get the download URL
//                return riversRef.getDownloadUrl();
//            }
//        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//            @Override
//            public void onComplete(@NonNull Task<Uri> task) {
//                if (task.isSuccessful()) {
//                    Uri downloadUri = task.getResult();
//                } else {
//                    // Handle failures
//                    // ...
//                }
//            }
//        });
    }

    private Task<Void> updateAttachmentToDB(Attachment attachment) {
        HashMap<String, Object> file = new HashMap<>();
        file.put(Keys.KEY_ID, attachment.getId());
        file.put(Keys.KEY_NAME, attachment.getName());
        file.put(Keys.KEY_SIZE, attachment.getSize());
        file.put(Keys.KEY_PATH, attachment.getPath());
        file.put(Keys.KEY_EXTENSION, attachment.getExtension());
        file.put(Keys.KEY_CREATED_DATE, attachment.getCreatedDate());


        return database.collection(Keys.KEY_COLLECTION_ATTACHMENT)
                .document(attachment.getId())
                .set(file);
    }

    private void downloadFile(Attachment attachment) {
        try {
            File localFile = File.createTempFile(attachment.getName(), attachment.getExtension());
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference httpsReference = storage.getReferenceFromUrl(attachment.getPath());
            httpsReference
                    .getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        MyToast.showLongToast(this, "downloaded to " + localFile.getAbsolutePath());
                    })
                    .addOnFailureListener(e -> {
                        MyToast.showLongToast(this, e.getMessage());
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getCurrentLocation(100, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Logic to handle location object
//                            LocationDetail locationDetail = new LocationDetail();
//                            locationDetail.setLat(String.valueOf(location.getLatitude()));
//                            locationDetail.setLng(String.valueOf(location.getLongitude()));

                            String message = location.getLatitude() + "," + location.getLongitude();
                            sendMessageNew(MessageType.LOCATION, message, null);
//                                listener.sendLocation(locationDetail);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Timber.tag(this.getClass().getName()).e(e);
//                            listener.sendLocation(null);
                    });
        }
    }

    private int findReceiver(String userId) {
        int result = -1;
        for (int i = 0; i < this.receivers.size(); i++) {
            if (this.receivers.get(i).getId().equals(userId)) {
                result = i;
                break;
            }
        }
        return result;
    }

    private int findMessage(List<ChatMessage> messages, String messageId) {
        int result = -1;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(messageId)) {
                result = i;
                break;
            }
        }
        return result;
    }

//    private void sendNotification(String messageBody) {
//        ApiClient.getClient().create(ApiService.class).sendMessage(
//                Keys.getRemoteMsgHeaders(),
//                messageBody
//        ).enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
//                if (response.isSuccessful()) {
//                    try {
//                        if (response.body() != null) {
//                            JSONObject responseJson = new JSONObject(response.body());
//                            JSONArray results = responseJson.getJSONArray("results");
//                            if (responseJson.getInt("failure") == 1) {
//                                JSONObject error = (JSONObject) results.get(0);
//                                //MyToast.showLongToast(ChatActivity.this, error.getString("error"));
//                                return;
//                            }
//                        }
//                    } catch (JSONException ex) {
//                        ex.printStackTrace();
//                        //MyToast.showLongToast(ChatActivity.this, ex.getMessage());
//                    }
//
//                    //MyToast.showLongToast(ChatActivity.this, "Notification sent successfully");
//                } else {
//                    //MyToast.showLongToast(ChatActivity.this, "error: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                //MyToast.showLongToast(ChatActivity.this, "Error: " + t.getMessage());
//            }
//        });
//    }

    private void unBlock() {
        Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_FRIEND)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .whereEqualTo(Keys.KEY_FRIEND_ID, receivers.get(0).getId())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        documentReference.update(Keys.KEY_STATUS, FriendStatus.NONE);
                    } else {
                        HashMap<String, Object> newFriend = new HashMap<>();
                        newFriend.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                        newFriend.put(Keys.KEY_FRIEND_ID, receivers.get(0).getId());
                        newFriend.put(Keys.KEY_STATUS, FriendStatus.FRIEND);
                        newFriend.put(Keys.KEY_MESSAGE, "");
                        newFriend.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                        newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                        database.collection(Keys.KEY_COLLECTION_FRIEND).add(newFriend);
                    }


                }).addOnFailureListener(e -> {
                    MyToast.showLongToast(this.getApplicationContext(), e.getMessage());
                });

        Task<QuerySnapshot> t2 = database.collection(Keys.KEY_COLLECTION_FRIEND)
                .whereEqualTo(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .whereEqualTo(Keys.KEY_USER_ID, receivers.get(0).getId())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        documentReference.update(Keys.KEY_STATUS, FriendStatus.NONE);
                    } else {
                        HashMap<String, Object> newFriend = new HashMap<>();
                        newFriend.put(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                        newFriend.put(Keys.KEY_USER_ID, receivers.get(0).getId());
                        newFriend.put(Keys.KEY_STATUS, FriendStatus.FRIEND);
                        newFriend.put(Keys.KEY_MESSAGE, "");
                        newFriend.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                        newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                        database.collection(Keys.KEY_COLLECTION_FRIEND).add(newFriend);
                    }

//                        MyToast.showLongToast(this.getApplicationContext(), "You blocked this person");
                }).addOnFailureListener(e -> {
                    MyToast.showLongToast(this.getApplicationContext(), e.getMessage());
                });

        Tasks.whenAllComplete(t1, t2)
                .addOnSuccessListener(tasks -> {
                    changeBlockUI(false);
//                    binding.lyBlock.setVisibility(View.GONE);
//                    binding.footerView.setVisibility(View.VISIBLE);
                });
    }

    private void changeBlockUI(boolean isBlocked) {
        if (isBlocked) {
            binding.btnUnBlock.setVisibility(View.VISIBLE);
            binding.btnUnBlock.setOnClickListener(v -> {
                unBlock();
            });
            binding.btnCall.setVisibility(View.GONE);
            binding.btnCallVideo.setVisibility(View.GONE);
        } else {
            binding.btnUnBlock.setVisibility(View.GONE);
            binding.btnCall.setVisibility(View.VISIBLE);
            binding.btnCallVideo.setVisibility(View.VISIBLE);
        }
    }


    //====================================================================
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(queryDocumentSnapshot.getString(Keys.KEY_SENDER_ID));
                    chatMessage.setReceiverId(queryDocumentSnapshot.getString(Keys.KEY_RECEIVER_ID));
                    chatMessage.setMessage(queryDocumentSnapshot.getString(Keys.KEY_MESSAGE));
                    chatMessage.setDateTime(Utils.getReadableDateTime(queryDocumentSnapshot.getDate(Keys.KEY_TIMESTAMP)));
                    chatMessage.setDateObject(queryDocumentSnapshot.getDate(Keys.KEY_TIMESTAMP));
                    chatMessages.add(chatMessage);
                }
            }

            Collections.sort(chatMessages, (t1, t2) -> t1.getDateObject().compareTo(t2.getDateObject()));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);

//        if (conversationId == null) {
//            checkForConversation();
//        }
    };


    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Keys.KEY_SENDER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
//        message.put(Keys.KEY_RECEIVER_ID, receivedUser.getId());
        message.put(Keys.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Keys.KEY_TIMESTAMP, new Date());
        database.collection(Keys.KEY_COLLECTION_CHAT).add(message);


//        if (conversationId != null) {
//            updateConversation(binding.inputMessage.getText().toString());
//        } else {
        HashMap<String, Object> conversation = new HashMap<>();
        conversation.put(Keys.KEY_SENDER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
        conversation.put(Keys.KEY_SENDER_NAME, preferenceManager.getString(Keys.KEY_NAME));
        conversation.put(Keys.KEY_SENDER_IMAGE, preferenceManager.getString(Keys.KEY_AVATAR));
//            conversation.put(Keys.KEY_RECEIVER_ID, receivedUser.getId());
//            conversation.put(Keys.KEY_RECEIVER_NAME, receivedUser.getName());
//            conversation.put(Keys.KEY_RECEIVER_IMAGE, receivedUser.getImage());
        conversation.put(Keys.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
        conversation.put(Keys.KEY_TIMESTAMP, new Date());
//            addConversation(conversation);
//        }
//
//        binding.inputMessage.setText(null);
    }

    //    private void addConversation(HashMap<String, Object> conversation) {
//        database.collection(Keys.KEY_COLLECTION_CONVERSATIONS)
//                .add(conversation)
//                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
//    }

//    private void updateConversation(String message) {
//        DocumentReference documentReference = database.collection(Keys.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
//        documentReference.update(Keys.KEY_LAST_MESSAGE, message, Keys.KEY_TIMESTAMP, new Date());
//    }

//    private void checkForConversation() {
//        if (chatMessages.size() != 0) {
//            checkForConversationRemotely(
//                    preferenceManager.getString(Keys.KEY_USER_ID),
//                    receivedUser.getId()
//            );
//
//            checkForConversationRemotely(
//                    receivedUser.getId(),
//                    preferenceManager.getString(Keys.KEY_USER_ID));
//        }
//    }

//    private void checkForConversationRemotely(String senderId, String receiverId) {
//        database.collection(Keys.KEY_COLLECTION_CONVERSATIONS)
//                .whereEqualTo(Keys.KEY_SENDER_ID, senderId)
//                .whereEqualTo(Keys.KEY_RECEIVER_ID, receiverId)
//                .get()
//                .addOnCompleteListener(conversationOnCompleteListener);
//    }

//    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
//        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
//            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
//            conversationId = documentSnapshot.getId();
//        }
//    };


    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
        if (this.getIntent().getBooleanExtra(Constants.OPEN_FROM_NOTIFICATION, false)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
        super.onBackPressed();
    }

    @Override
    public void sendDialogResult(ChatAdapter.Result result, ChatMessage chatMessage) {
        if (result == ChatAdapter.Result.AUDIO_CALL) {
            handleOnBtnCallClicked();
        } else if (result == ChatAdapter.Result.VIDEO_CALL) {
            handleOnBtnCallVideoClicked();
        }
    }
}