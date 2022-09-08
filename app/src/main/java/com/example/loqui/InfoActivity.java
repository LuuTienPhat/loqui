package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.loqui.constants.Constants;
import com.example.loqui.constants.FriendStatus;
import com.example.loqui.constants.MessageType;
import com.example.loqui.constants.RecipientStatus;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.constants.RoomType;
import com.example.loqui.data.model.Room;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityInfoBinding;
import com.example.loqui.dialog.CustomDialog;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class InfoActivity extends BaseActivity implements CustomDialog.Listener {

    private ActivityInfoBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    //    private User friend;
    private List<User> receivers;
    private Room room;
    private User me;

    private static final String REQUEST_BLOCKED = "request_block";
    private static final String REQUEST_LEAVE = "request_leave";

    private boolean started = false;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
        setListeners();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

//        friend = (User) getIntent().getSerializableExtra("friend");
        receivers = (List<User>) getIntent().getSerializableExtra(Constants.RECEIVERS);
        room = (Room) getIntent().getSerializableExtra(Constants.ROOM);

        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        for (DocumentChange documentChange : value.getDocumentChanges()) {
                            QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
                            if (queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.ADMIN)) {
                                isAdmin = true;
                            } else {
                                isAdmin = false;
                            }

                            if (queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
                                MyToast.showLongToast(this.getApplicationContext(), "You have been removed from group");

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            }
                        }
                    }

                });

        database.collection(Keys.KEY_COLLECTION_ROOM)
                .document(room.getId()).addSnapshotListener(listenRoom);
//
//        if (room.getAdminId() != null && !room.getAdminId().isEmpty()) {
//            binding.btnGroupMember.setVisibility(View.VISIBLE);
//            binding.btnLeaveGroup.setVisibility(View.VISIBLE);
//            binding.btnCustomGroup.setVisibility(View.VISIBLE);
//        } else {
//            binding.btnGroupMember.setVisibility(View.GONE);
//            binding.btnLeaveGroup.setVisibility(View.GONE);
//            binding.btnCustomGroup.setVisibility(View.GONE);
//        }


        if (receivers.size() > 1) {
            binding.btnGroupMember.setVisibility(View.VISIBLE);
            binding.btnLeaveGroup.setVisibility(View.VISIBLE);
            binding.btnCustomGroup.setVisibility(View.VISIBLE);
            binding.btnBlock.setVisibility(View.GONE);

            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(room.getAvatar()));
            binding.tvName.setText(room.getName());
        } else {
            binding.btnGroupMember.setVisibility(View.GONE);
            binding.btnLeaveGroup.setVisibility(View.GONE);
            binding.btnCustomGroup.setVisibility(View.GONE);
            binding.btnAddMember.setVisibility(View.GONE);

            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(receivers.get(0).getImage()));
            binding.tvName.setText(receivers.get(0).getFullName());
        }

        this.me = new User();
        this.me.setId(preferenceManager.getString(Keys.KEY_USER_ID));
        this.me.setFirstName(preferenceManager.getString(Keys.KEY_FIRSTNAME));
        this.me.setLastName(preferenceManager.getString(Keys.KEY_LASTNAME));
        this.me.setImage(preferenceManager.getString(Keys.KEY_AVATAR));
        this.me.setToken(preferenceManager.getString(Keys.KEY_FCM_TOKEN));

        //getRoom(room.getId());
    }

    private void getRoom(String roomId) {
        database.collection(Keys.KEY_COLLECTION_ROOM)
                .document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.tvName.setText(documentSnapshot.getString(Keys.KEY_NAME));
                    if (receivers.size() > 1) {
                        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(documentSnapshot.getString(Keys.KEY_AVATAR)));
                        binding.tvName.setText(documentSnapshot.getString(Keys.KEY_NAME));
                    } else {
                        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(receivers.get(0).getImage()));
                        binding.tvName.setText(receivers.get(0).getFullName());
                    }
                });
    }

    private void setListeners() {
//        binding.btnNickname.setOnClickListener(v -> {
//
//        });
        binding.btnCustomGroup.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CustomGroupActivity.class);
            intent.putExtra(Constants.ROOM, room);
            startActivity(intent);
        });

        binding.btnGroupMember.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), GroupMemberActivity.class);
            intent.putExtra(Constants.ROOM, room);
            startActivity(intent);
        });

        binding.btnLeaveGroup.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(CustomDialog.Type.CONFIRM, "Leave this group?", "Are you sure you want to leave this conversation? You will no longer receive new messages", REQUEST_LEAVE);
            customDialog.show(getSupportFragmentManager(), null);
        });

        binding.btnBlock.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(CustomDialog.Type.CONFIRM, "Block this person?", "Are you sure to block this person", REQUEST_BLOCKED);
            customDialog.show(getSupportFragmentManager(), null);
        });

        binding.btnCallVideo.setOnClickListener(v -> {
            handleOnBtnCallVideoClicked();
        });

        binding.btnCall.setOnClickListener(v -> {
            handleOnBtnCallClicked();
        });

        binding.btnAddMember.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddGroupMemberActivity.class);
            intent.putExtra(Constants.ROOM, (Serializable) room);
            startActivity(intent);
        });

        binding.btnAttachment.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AttachmentActivity.class);
            intent.putExtra(Constants.ROOM, room);
            startActivity(intent);
        });
    }

    private void handleOnBtnCallVideoClicked() {
        Intent intent = new Intent(getApplicationContext(), OutgoingCallActivity.class);
        intent.putExtra(Constants.CALL_TYPE, MessageType.VIDEO_CALL);
        intent.putExtra(Constants.RECEIVERS, (Serializable) receivers);
        startActivity(intent);
    }

    private void handleOnBtnCallClicked() {
        Intent intent = new Intent(getApplicationContext(), OutgoingCallActivity.class);
        intent.putExtra(Constants.CALL_TYPE, MessageType.AUDIO_CALL);
        intent.putExtra(Constants.RECEIVERS, (Serializable) receivers);
        startActivity(intent);

    }

    private final EventListener<DocumentSnapshot> listenRoom = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            room.setId(value.getString(Keys.KEY_ID));
            room.setName(value.getString(Keys.KEY_NAME));
            room.setAvatar(value.getString(Keys.KEY_AVATAR));
            room.setStatus(value.getString(Keys.KEY_STATUS));
            room.setType(value.getString(Keys.KEY_TYPE));
//            room.setAdminId(value.getString(Keys.KEY_ADMIN_ID));
            room.setCreatedDate(value.getString(Keys.KEY_CREATED_DATE));
            room.setModifiedDate(value.getString(Keys.KEY_MODIFIED_DATE));

            if (room.getType().equals(RoomType.GROUP)) {
                binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(room.getAvatar()));
                binding.tvName.setText(room.getName());
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
//        if (started) {
//            FirebaseHelper.findRoom(database, room.getId())
//                    .addOnSuccessListener(documentSnapshot -> {
//                        room.setId(documentSnapshot.getString(Keys.KEY_ID));
//                        room.setName(documentSnapshot.getString(Keys.KEY_NAME));
//                        room.setAvatar(documentSnapshot.getString(Keys.KEY_AVATAR));
//                        room.setStatus(documentSnapshot.getString(Keys.KEY_STATUS));
//                        room.setAdminId(documentSnapshot.getString(Keys.KEY_ADMIN_ID));
//                        room.setCreatedDate(documentSnapshot.getString(Keys.KEY_CREATED_DATE));
//                        room.setModifiedDate(documentSnapshot.getString(Keys.KEY_MODIFIED_DATE));
//
//                        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(room.getAvatar()));
//                        binding.tvName.setText(room.getName());
//                    });
//        } else {
//            started = true;
//        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void sendDialogResult(CustomDialog.Result result, String request) {
        if (request.equals(REQUEST_LEAVE) && result.equals(CustomDialog.Result.OK)) {
            if (isAdmin) { // REMOVE bản thân là admin khỏi nhóm
                database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                        .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                String userId = null;
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                    if (!documentSnapshot.getString(Keys.KEY_USER_ID).equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                                        if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
                                            userId = documentSnapshot.getString(Keys.KEY_USER_ID);
                                            break;
                                        }
                                    }
                                }

                                String finalUserId = userId;
                                database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                                        .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                                        .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                                HashMap<String, Object> changed = new HashMap<>();
                                                changed.put(Keys.KEY_STATUS, RecipientStatus.REMOVED);
                                                changed.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                                queryDocumentSnapshots1.getDocuments().get(0).getReference().update(changed)
                                                        .addOnSuccessListener(unused -> {
//                                                            Intent intent = new Intent(requireActivity(), MainActivity.class);
//                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                            startActivity(intent);
                                                            database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                                                                    .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                                                                    .whereEqualTo(Keys.KEY_USER_ID, finalUserId)
                                                                    .get()
                                                                    .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                                        if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
                                                                            HashMap<String, Object> changed1 = new HashMap<>();
                                                                            changed1.put(Keys.KEY_STATUS, RecipientStatus.ADMIN);
                                                                            changed1.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                                                            queryDocumentSnapshots2.getDocuments().get(0).getReference().update(changed1)
                                                                                    .addOnSuccessListener(unused1 -> {
                                                                                        FirebaseHelper.findUser(database, finalUserId)
                                                                                                .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                                                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots3.getDocuments().get(0);
                                                                                                    User user = new User();
                                                                                                    user.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                                                                    user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                                                                                    user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                                                                                                    sendMessage(user.getFullName() + " is now administrator");
                                                                                                });
                                                                                    });
                                                                        }
                                                                    });

                                                            sendMessage(me.getFullName() + " has left the group");
                                                            checkGroupAvailable();
                                                        });
                                            }
                                        });
                            }
                        });
            } else {
                database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                        .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                        .whereEqualTo(Keys.KEY_USER_ID, this.me.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                HashMap<String, Object> changed = new HashMap<>();
                                changed.put(Keys.KEY_STATUS, RecipientStatus.REMOVED);
                                changed.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                queryDocumentSnapshots.getDocuments().get(0).getReference().update(changed)
                                        .addOnSuccessListener(unused -> {
                                            sendMessage(me.getFullName() + " has left the group");
                                            checkGroupAvailable();
//                                                Intent intent = new Intent(requireActivity(), MainActivity.class);
//                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                startActivity(intent);
                                            //this.selectedUser = null;
                                        });
                            }
                        });
            }

//            database.collection(Keys.KEY_COLLECTION_RECIPIENT)
//                    .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
//                    .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
//                    .get()
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                            HashMap<String, Object> changed = new HashMap<>();
//                            changed.put(Keys.KEY_STATUS, RecipientStatus.REMOVED);
//                            changed.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//                            queryDocumentSnapshots.getDocuments().get(0).getReference().update(changed)
//                                    .addOnSuccessListener(unused -> {
//                                        Intent intent = new Intent(this, MainActivity.class);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(intent);
//                                    });
//                        }
//                    });
        }

        if (request.equals(REQUEST_BLOCKED) && result.equals(CustomDialog.Result.OK)) {
            Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_FRIEND)
                    .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                    .whereEqualTo(Keys.KEY_FRIEND_ID, receivers.get(0).getId())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
                            documentReference.update(Keys.KEY_STATUS, FriendStatus.BLOCKED);
                        } else {
                            HashMap<String, Object> newFriend = new HashMap<>();
                            newFriend.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                            newFriend.put(Keys.KEY_FRIEND_ID, receivers.get(0).getId());
                            newFriend.put(Keys.KEY_STATUS, FriendStatus.BLOCKED);
                            newFriend.put(Keys.KEY_MESSAGE, "");
                            newFriend.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                            newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                            database.collection(Keys.KEY_COLLECTION_FRIEND).add(newFriend);
                        }


                    }).addOnFailureListener(e -> {
                        MyToast.showLongToast(getApplicationContext(), e.getMessage());
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
                            newFriend.put(Keys.KEY_STATUS, FriendStatus.NONE);
                            newFriend.put(Keys.KEY_MESSAGE, "");
                            newFriend.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                            newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                            database.collection(Keys.KEY_COLLECTION_FRIEND).add(newFriend);
                        }

//                        MyToast.showLongToast(getApplicationContext(), "You blocked this person");
                    }).addOnFailureListener(e -> {
                        MyToast.showLongToast(getApplicationContext(), e.getMessage());
                    });

            Tasks.whenAllComplete(t1, t2)
                    .addOnSuccessListener(tasks -> {
                        MyToast.showLongToast(getApplicationContext(), "You blocked this person");
                    });
        }

    }

    private void checkGroupAvailable() {
        // Kiểm tra xem nhóm còn ai tham gia không
        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = 0;
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
                                count++;
                            }
                        }

                        if (count == 1 | count == 0) { // Nếu số lượng người trong nhóm là 0 hoặc 1
                            database.collection(Keys.KEY_COLLECTION_ROOM)
                                    .whereEqualTo(Keys.KEY_ID, room.getId())
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                        queryDocumentSnapshots1.getDocuments().get(0).getReference().update(Keys.KEY_STATUS, RoomStatus.DELETED)
                                                .addOnSuccessListener(unused -> {
                                                    Intent intent = new Intent(this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                });
                                    });
                        } else {
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                });
    }

    private void sendMessage(String messageContent) {
        HashMap<String, Object> message = new HashMap<>();
        String messageId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_CHAT);
        message.put(Keys.KEY_ID, messageId);
        message.put(Keys.KEY_ROOM_ID, room.getId());
        message.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
        message.put(Keys.KEY_MESSAGE, messageContent);
        message.put(Keys.KEY_REPLY_ID, "");
        message.put(Keys.KEY_STATUS, "");
        message.put(Keys.KEY_TYPE, MessageType.STATUS);
        message.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
        message.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
        database.collection(Keys.KEY_COLLECTION_CHAT).document(messageId).set(message);

    }
}