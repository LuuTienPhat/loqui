package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.loqui.constants.Constants;
import com.example.loqui.constants.FriendStatus;
import com.example.loqui.constants.NotificationType;
import com.example.loqui.constants.RecipientStatus;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.constants.RoomType;
import com.example.loqui.constants.StatusStatus;
import com.example.loqui.data.model.Status;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityAccountInformationBinding;
import com.example.loqui.dialog.CustomDialog;
import com.example.loqui.firebase.MessagingService;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountInformationActivity extends BaseActivity implements CustomDialog.Listener {

    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private ActivityAccountInformationBinding binding;
    private User selectedUser;
    private String friendStatus;
    private Query query;
    private User me;
//    private User friend;

    private Query query2;

    private ListenerRegistration friendListenerRegistration;
    private ListenerRegistration friendStatusListenerRegistration;

    private static final String REQUEST_BLOCK = "request_block";
    private static final String REQUEST_UNBLOCK = "request_unblock";
    private static final String REQUEST_UNFRIEND = "request_unfriend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();

        friendListenerRegistration = database.collection(Keys.KEY_COLLECTION_USERS)
                .whereEqualTo(Keys.KEY_ID, selectedUser.getId())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        for (DocumentChange documentChange : value.getDocumentChanges()) {
                            QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                            User user = new User();
                            user.setId(queryDocumentSnapshot.getString(Keys.KEY_ID));
                            user.setLastName(queryDocumentSnapshot.getString(Keys.KEY_LASTNAME));
                            user.setFirstName(queryDocumentSnapshot.getString(Keys.KEY_FIRSTNAME));
                            user.setEmail(queryDocumentSnapshot.getString(Keys.KEY_EMAIL));
                            user.setPhone(queryDocumentSnapshot.getString(Keys.KEY_PHONE));
                            user.setToken(queryDocumentSnapshot.getString(Keys.KEY_FCM_TOKEN));
                            user.setImage(queryDocumentSnapshot.getString(Keys.KEY_AVATAR));

                            this.selectedUser = user;
                            updateUI();
                        }
                    }
                });

        friendStatusListenerRegistration = database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
                .whereEqualTo(Keys.KEY_USER_ID, selectedUser.getId())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        for (DocumentChange documentChange : value.getDocumentChanges()) {
                            QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                String statusType = queryDocumentSnapshot.getString(Keys.KEY_STATUS);
                                if (statusType.equals(StatusStatus.ACTIVE)) {
                                    Status status = new Status();
                                    status.setId(queryDocumentSnapshot.getString(Keys.KEY_ID));
                                    status.setUserId(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));
                                    status.setName(queryDocumentSnapshot.getString(Keys.KEY_NAME));
                                    status.setIcon(queryDocumentSnapshot.getString(Keys.KEY_ICON));
                                    status.setHour(queryDocumentSnapshot.getString(Keys.KEY_HOUR));
                                    status.setEndDate(queryDocumentSnapshot.getString(Keys.KEY_END_DATE));

                                    binding.lyStatus.setVisibility(View.VISIBLE);
                                    binding.tvStatusIcon.setText(status.getIcon());
                                    binding.tvStatusName.setText(status.getName());
                                }
                            }

                            if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                                String statusType = queryDocumentSnapshot.getString(Keys.KEY_STATUS);
                                if (statusType.equals(StatusStatus.ACTIVE)) {
                                    Status status = new Status();
                                    status.setId(queryDocumentSnapshot.getString(Keys.KEY_ID));
                                    status.setUserId(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));
                                    status.setName(queryDocumentSnapshot.getString(Keys.KEY_NAME));
                                    status.setIcon(queryDocumentSnapshot.getString(Keys.KEY_ICON));
                                    status.setHour(queryDocumentSnapshot.getString(Keys.KEY_HOUR));
                                    status.setEndDate(queryDocumentSnapshot.getString(Keys.KEY_END_DATE));

                                    binding.lyStatus.setVisibility(View.VISIBLE);
                                    binding.tvStatusIcon.setText(status.getIcon());
                                    binding.tvStatusName.setText(status.getName());
                                } else {

                                    binding.lyStatus.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                });

        //loadUserDetails();
        setListeners();
        listensFriend();
        checkFriendRequest();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this.getApplicationContext());
        selectedUser = (User) this.getIntent().getSerializableExtra("user");

        query = database.collection(Keys.KEY_COLLECTION_FRIEND)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .whereEqualTo(Keys.KEY_FRIEND_ID, selectedUser.getId());

        query2 = database.collection(Keys.KEY_COLLECTION_FRIEND)
                .whereEqualTo(Keys.KEY_USER_ID, selectedUser.getId())
                .whereEqualTo(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID));

        this.me = new User();
        this.me.setId(preferenceManager.getString(Keys.KEY_USER_ID));
        this.me.setFirstName(preferenceManager.getString(Keys.KEY_FIRSTNAME));
        this.me.setLastName(preferenceManager.getString(Keys.KEY_LASTNAME));
        this.me.setImage(preferenceManager.getString(Keys.KEY_AVATAR));
        this.me.setToken(preferenceManager.getString(Keys.KEY_FCM_TOKEN));

//        this.friend = new User();
    }

    private void checkFriendRequest() {
        query2.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        if (documentSnapshot.get(Keys.KEY_STATUS).equals(FriendStatus.NEW)) { //if has friend request;
                            binding.btnMakeFriend.setVisibility(View.GONE); //hide btnMakefriend
                            binding.lyInviting.setVisibility(View.VISIBLE); //show layout accept/decline
                            binding.btnAccept.setOnClickListener(v -> { // if accept is clicked
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_STATUS, FriendStatus.FRIEND);
                                newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                documentSnapshot.getReference().update(newFriend); //set status of friend to fiend

                                sendAcceptedNotification();

                                query.get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    queryDocumentSnapshots1.getDocuments().get(0).getReference().update(newFriend); //set status of me to friend
                                }).addOnSuccessListener(unused -> {
                                    binding.btnMakeFriend.setVisibility(View.VISIBLE); //show btnMake friend
                                    binding.lyInviting.setVisibility(View.GONE); //hide lyInviting
                                });

                                friendStatus = FriendStatus.FRIEND;
                                //MyToast.showLongToast(this, "btnAccept clicked");
                            });

                            binding.btnDecline.setOnClickListener(v -> { // if decline is clicked
                                HashMap<String, Object> unFriend = new HashMap<>();
                                unFriend.put(Keys.KEY_STATUS, FriendStatus.NONE);
                                unFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                documentSnapshot.getReference().update(unFriend); //set status of friend to none

                                query.get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    queryDocumentSnapshots1.getDocuments().get(0).getReference().update(unFriend) //set status of me to none
                                            .addOnSuccessListener(unused -> {
                                                binding.btnMakeFriend.setVisibility(View.VISIBLE); //show btnMake friend
                                                binding.lyInviting.setVisibility(View.GONE); //hide lyInviting
                                            });
                                });

                                friendStatus = FriendStatus.NONE;
                                MyToast.showLongToast(this, "btnDecline clicked");
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    MyToast.showLongToast(this, e.getMessage());
                });
    }

    private void listensFriend() {
        query.addSnapshotListener(meListenFriend);
        query2.addSnapshotListener(friendListenMe);
    }

    private final EventListener<QuerySnapshot> meListenFriend = (value, error) -> {
        String userId = preferenceManager.getString(Keys.KEY_USER_ID);

        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                String receivedStatus = queryDocumentSnapshot.getString(Keys.KEY_STATUS);
                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                    if (receivedStatus.equals(FriendStatus.NEW)) { //Nếu status là NEW: có lời mời kết bạn
                        if (queryDocumentSnapshot.getString(Keys.KEY_USER_ID).equals(userId)) {
                            binding.tvMakeFriend.setText("Cancel Request");
                            binding.btnMakeFriend.setOnClickListener(v -> {
                                queryDocumentSnapshot.getReference().update(Keys.KEY_STATUS, FriendStatus.NONE);
                            });
                        } else {
                            checkFriendRequest();
//                            binding.tvMakeFriend.setText("Accept");
//                            binding.btnMakeFriend.setOnClickListener(v -> {
//                                queryDocumentSnapshot.getReference().update(Keys.KEY_STATUS, FriendStatus.FRIEND);
//                            });
                        }

                        friendStatus = FriendStatus.NEW;
                    }

                    //Nếu status là FRIEND: đã là bạn
                    else if (receivedStatus.equals(FriendStatus.FRIEND)) {
                        //binding.btnCall.setVisibility(View.VISIBLE);
                        //binding.btnCallVideo.setVisibility(View.VISIBLE);

                        if (queryDocumentSnapshot.getString(Keys.KEY_USER_ID).equals(userId)) {
                            binding.tvMakeFriend.setText("Unfriend");
                            binding.btnMakeFriend.setOnClickListener(v -> {
                                handleBtnUnFriendClicked();
                                //queryDocumentSnapshot.getReference().update(Keys.KEY_STATUS, FriendStatus.NONE);
                            });
                        }

                        friendStatus = FriendStatus.FRIEND;
                    }

                    //nếu cả hai ở trạng thái block
                    else if (receivedStatus.equals(FriendStatus.BLOCKED)) {
                        binding.btnBlock.setVisibility(View.GONE);
                        binding.btnUnBlock.setVisibility(View.VISIBLE);
                        binding.btnUnBlock.setOnClickListener(v -> {
                            handleBtnUnBlockedClicked();
                        });
                        binding.btnMakeFriend.setVisibility(View.GONE);
                        friendStatus = FriendStatus.BLOCKED;
                    }

                    //nếu cả hai ở trạng thái none hoặc không có gì hết
                    else {
                        if (queryDocumentSnapshot.getString(Keys.KEY_USER_ID).equals(userId)) {
                            binding.tvMakeFriend.setText("Make Friend");
                            binding.btnMakeFriend.setOnClickListener(v -> {
                                makeFriend();
                            });
                        }

                        friendStatus = FriendStatus.NONE;
                    }
                }
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                    // Nếu là NEW
                    if (receivedStatus.equals(FriendStatus.NEW)) {
                        if (queryDocumentSnapshot.getString(Keys.KEY_USER_ID).equals(userId)) {
                            binding.tvMakeFriend.setText("Cancel Request");
                            binding.btnMakeFriend.setOnClickListener(v -> {
                                queryDocumentSnapshot.getReference().update(Keys.KEY_STATUS, FriendStatus.NONE);
                            });
                        } else {
                            checkFriendRequest();
//                            binding.tvMakeFriend.setText("Accept");
//                            binding.btnMakeFriend.setOnClickListener(v -> {
//                                queryDocumentSnapshot.getReference().update(Keys.KEY_STATUS, FriendStatus.FRIEND);
//                            });
                        }

                        friendStatus = FriendStatus.NEW;
                    }

                    //NẾU LÀ FRIEND
                    else if (receivedStatus.equals(FriendStatus.FRIEND)) {
                        //binding.btnCall.setVisibility(View.VISIBLE);
                        //binding.btnCallVideo.setVisibility(View.VISIBLE);

                        if (queryDocumentSnapshot.getString(Keys.KEY_USER_ID).equals(userId)) {
                            binding.tvMakeFriend.setText("Unfriend");
                            binding.btnMakeFriend.setOnClickListener(v -> {
                                handleBtnUnFriendClicked();
                                //queryDocumentSnapshot.getReference().update(Keys.KEY_STATUS, FriendStatus.NONE);
                            });

                            friendStatus = FriendStatus.FRIEND;
                        }
                    }

                    //NẾU LÀ BLOCKED
                    else if (receivedStatus.equals(FriendStatus.BLOCKED)) {
                        binding.btnBlock.setVisibility(View.GONE);
                        binding.btnUnBlock.setVisibility(View.VISIBLE);
                        binding.btnUnBlock.setOnClickListener(v -> {
                            handleBtnUnBlockedClicked();
                        });

                        friendStatus = FriendStatus.BLOCKED;

                        binding.btnMakeFriend.setVisibility(View.GONE);

//                        if (queryDocumentSnapshot.getString(Keys.KEY_USER_ID).equals(userId)) {
//                            binding.btnMakeFriend.setText("Make Friend");
//                            binding.btnMakeFriend.setEnabled(false);
//                        } else {
//                            binding.btnMakeFriend.setText("Make Friend");
//                            binding.btnMakeFriend.setEnabled(false);
//                        }
//                        if (queryDocumentSnapshot.getString(Keys.KEY_USER_ID).equals(selectedUser.getId())) {
//                            binding.tvMakeFriend.setText("Make Friend");
//                            binding.btnMakeFriend.setEnabled(false);
//                        }
                    }

                    //nếu là NONE
                    else if (receivedStatus.equals(FriendStatus.NONE)) {
                        if (queryDocumentSnapshot.getString(Keys.KEY_USER_ID).equals(userId)) {
                            binding.tvMakeFriend.setText("Make Friend");
                            binding.btnMakeFriend.setOnClickListener(v -> {
                                makeFriend();
                            });
                        } else {
                            binding.lyInviting.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    };

    private final EventListener<QuerySnapshot> friendListenMe = (value, error) -> {
        String userId = preferenceManager.getString(Keys.KEY_USER_ID);

        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                String receivedStatus = queryDocumentSnapshot.getString(Keys.KEY_STATUS);
                if (receivedStatus.equals(FriendStatus.NEW)) { //Nếu status là NEW: có lời mời kết bạn
                    checkFriendRequest();
                }

                if (receivedStatus.equals(FriendStatus.NONE)) {
                    if (friendStatus.equals(FriendStatus.BLOCKED)) {

                    } else {
                        binding.btnMakeFriend.setVisibility(View.VISIBLE); //show btnMake friend
                        binding.lyInviting.setVisibility(View.GONE); //hide lyInviting
                        binding.btnMakeFriend.setOnClickListener(v -> {
                            makeFriend();
                        });
                    }
                }

//                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
//                    // Nếu là NEW
//                    if (receivedStatus.equals(FriendStatus.NEW)) {
//                        checkFriendRequest();
//                    }
//
//                    if (receivedStatus.equals(FriendStatus.NONE)) {
//                        binding.btnMakeFriend.setVisibility(View.VISIBLE); //show btnMake friend
//                        binding.lyInviting.setVisibility(View.GONE); //hide lyInviting
//                        binding.btnMakeFriend.setOnClickListener(v -> {
//                            makeFriend();
//                        });
//                    }
//                }
            }
        }
    };

    private void makeFriend() {
        List<Task<QuerySnapshot>> taskList = new ArrayList<>();
        Task<QuerySnapshot> task1 = query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                        HashMap<String, Object> newFriend = new HashMap<>();
                        newFriend.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                        newFriend.put(Keys.KEY_FRIEND_ID, selectedUser.getId());
                        newFriend.put(Keys.KEY_STATUS, FriendStatus.NEW);
                        newFriend.put(Keys.KEY_MESSAGE, "hello");
                        newFriend.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                        newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                        database.collection(Keys.KEY_COLLECTION_FRIEND).add(newFriend);

//                        MyToast.showLongToast(this, "Make friend request sent!");
                    } else {
                        //If the request is exist ?
                        HashMap<String, Object> newFriend = new HashMap<>();
                        newFriend.put(Keys.KEY_STATUS, FriendStatus.NEW);
                        newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                        queryDocumentSnapshots.getDocuments()
                                .get(0)
                                .getReference()
                                .update(newFriend);

//                        MyToast.showLongToast(this, "Make friend request sent!");
                    }

//                    sendRequestNotification();
                })
                .addOnFailureListener(e -> {

                });


        Task<QuerySnapshot> task2 = query2
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                        HashMap<String, Object> newFriend = new HashMap<>();
                        newFriend.put(Keys.KEY_USER_ID, selectedUser.getId());
                        newFriend.put(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                        newFriend.put(Keys.KEY_STATUS, FriendStatus.NONE);
                        newFriend.put(Keys.KEY_MESSAGE, "hello");
                        newFriend.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                        newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                        database.collection(Keys.KEY_COLLECTION_FRIEND).add(newFriend);

//                        MyToast.showLongToast(this, "Make friend request sent!");
                    } else {
                        //If the request is exist ?
                        HashMap<String, Object> newFriend = new HashMap<>();
                        newFriend.put(Keys.KEY_STATUS, FriendStatus.NONE);
                        newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                        queryDocumentSnapshots.getDocuments()
                                .get(0)
                                .getReference()
                                .update(newFriend);

//                        MyToast.showLongToast(this, "Make friend request sent!");
                    }


                })
                .addOnFailureListener(e -> {

                });

        taskList.add(task1);
        taskList.add(task2);

        Tasks.whenAllComplete(taskList)
                .addOnSuccessListener(tasks -> {
                    sendRequestNotification();
                    MyToast.showLongToast(this, "Make friend request sent!");
                })
                .addOnFailureListener(e -> {

                });
    }

    private void setListeners() {
        binding.btnBlock.setOnClickListener(v -> {
            CustomDialog customDialog = new CustomDialog(CustomDialog.Type.CONFIRM, "Are you sure?", "Are you sure you want to block this person", REQUEST_BLOCK);
            customDialog.show(getSupportFragmentManager(), null);
        });

        binding.btnMakeFriend.setOnClickListener(v -> {
            makeFriend();
        });

        binding.btnMessage.setOnClickListener(v -> {
            database.collection(Keys.KEY_COLLECTION_FRIEND)
                    .whereEqualTo(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                    .whereEqualTo(Keys.KEY_USER_ID, selectedUser.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            String status = documentSnapshot.getString(Keys.KEY_STATUS);
                            if (status.equals(FriendStatus.FRIEND)) {
                                openChatActivity();
//                                Intent intent = new Intent(this, ChatActivity.class);
//                                intent.putExtra(Constants.ROOM, "");
//                                startActivity(intent);
                            }
                        }

                        database.collection(Keys.KEY_COLLECTION_SETTINGS)
                                .whereEqualTo(Keys.KEY_USER_ID, selectedUser.getId())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots1.getDocuments().get(0);
                                    Boolean allowStranger = documentSnapshot.getBoolean(Keys.KEY_SETTING_MESSAGE_REQUEST);
                                    if (allowStranger) {
                                        openChatActivity();
                                    } else {
                                        MyToast.showLongToast(this, "This user doesn't allow stranger to chat");
                                    }
                                })
                                .addOnFailureListener(e -> {

                                });
                    });


        });

        binding.btnCall.setOnClickListener(v -> {

        });

        binding.btnCallVideo.setOnClickListener(v -> {

        });

    }

//    private void loadUserDetails() {
//        if (selectedUser.getImage() == null) {
//            FirebaseHelper.findUser(database, selectedUser.getId())
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(queryDocumentSnapshots.getDocuments().get(0).getString(Keys.KEY_AVATAR)));
//                    });
//        } else {
//            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(selectedUser.getImage()));
//        }
//        binding.etEmail.setText(selectedUser.getEmail());
//        binding.tvName.setText(selectedUser.getFullName());
//
//        database.collection(Keys.KEY_COLLECTION_FRIEND)
//                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
//                .whereEqualTo(Keys.KEY_FRIEND_ID, selectedUser.getId())
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
////                        String friendStatus = documentSnapshot.getString(Keys.KEY_STATUS);
////                        if (friendStatus.equals(FriendStatus.NEW)) {
////                        }
//
//
//                    }
//                });
//    }

    private void handleBtnUnFriendClicked() {
        CustomDialog customDialog = new CustomDialog(CustomDialog.Type.CONFIRM, "Are you sure?", "Are you sure you want to unfriend this person", REQUEST_UNFRIEND);
        customDialog.show(getSupportFragmentManager(), null);
    }

    private void handleBtnUnBlockedClicked() {
        CustomDialog customDialog = new CustomDialog(CustomDialog.Type.CONFIRM, "Are you sure?", "Are you sure you want to unblock this person", REQUEST_UNBLOCK);
        customDialog.show(getSupportFragmentManager(), null);
    }

    private void sendRequestNotification() {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(selectedUser.getToken());

            JSONObject data = new JSONObject();
            data.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
            data.put(Keys.KEY_FIRSTNAME, preferenceManager.getString(Keys.KEY_FIRSTNAME));
            data.put(Keys.KEY_LASTNAME, preferenceManager.getString(Keys.KEY_LASTNAME));
            data.put(Keys.KEY_FCM_TOKEN, preferenceManager.getString(Keys.KEY_FCM_TOKEN));
            data.put(Keys.KEY_MESSAGE, me.getFullName() + " send you a friend request!");
//            data.put(Keys.KEY_ROOM_ID, roomId);

            data.put(Keys.KEY_NOTIFICATION_TYPE, NotificationType.FRIEND_REQUEST);

            JSONObject body = new JSONObject();
            body.put(Keys.REMOTE_MSG_DATA, data);
            body.put(Keys.REMOTE_MSG_REGISTRATION_IDS, tokens);

            MessagingService.sendNotification(this.getApplicationContext(), body.toString());

        } catch (Exception ex) {
            MyToast.showShortToast(this, ex.getMessage());
        }
    }

    private void sendAcceptedNotification() {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(selectedUser.getToken());

            JSONObject data = new JSONObject();
            data.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
            data.put(Keys.KEY_FIRSTNAME, preferenceManager.getString(Keys.KEY_FIRSTNAME));
            data.put(Keys.KEY_LASTNAME, preferenceManager.getString(Keys.KEY_LASTNAME));
            data.put(Keys.KEY_FCM_TOKEN, preferenceManager.getString(Keys.KEY_FCM_TOKEN));
            data.put(Keys.KEY_MESSAGE, me.getFullName() + " accepted your request! You two are friends now");
//            data.put(Keys.KEY_ROOM_ID, roomId);

            data.put(Keys.KEY_NOTIFICATION_TYPE, NotificationType.FRIEND_ACCEPT);

            JSONObject body = new JSONObject();
            body.put(Keys.REMOTE_MSG_DATA, data);
            body.put(Keys.REMOTE_MSG_REGISTRATION_IDS, tokens);

            MessagingService.sendNotification(this.getApplicationContext(), body.toString());

        } catch (Exception ex) {
            MyToast.showShortToast(this, ex.getMessage());
        }
    }

    private void openChatActivity() {
        List<String> recipients = new ArrayList<>();
        recipients.add(preferenceManager.getString(Keys.KEY_USER_ID));
        recipients.add(selectedUser.getId());

        List<String> myRooms = new ArrayList<>();
        List<String> friendRooms = new ArrayList<>();
        List<Task<QuerySnapshot>> taskList = new ArrayList<>();

        Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RoomStatus.DELETED)) {
                                myRooms.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
                            }
                        }
                    }
                });

        taskList.add(t1);

        Task<QuerySnapshot> t2 = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, selectedUser.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RoomStatus.DELETED)) {
                                friendRooms.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
                            }
                        }
                    }
                });
        taskList.add(t2);
        Tasks.whenAllComplete(taskList)
                .addOnSuccessListener(tasks -> {
                    List<String> rooms = filter(myRooms, friendRooms);
                    if (!rooms.isEmpty()) {
                        database.collection(Keys.KEY_COLLECTION_ROOM)
                                .whereIn(Keys.KEY_ID, rooms)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    String roomId = null;
                                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                        //boolean created = false;
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                            String roomType = documentSnapshot.getString(Keys.KEY_TYPE);
                                            if (roomType.equals(RoomType.TWO)) {
                                                roomId = documentSnapshot.getString(Keys.KEY_ID);
                                            }
                                        }
                                    }

                                    if (roomId == null) {
                                        HashMap<String, Object> newRoom = new HashMap<>();
                                        roomId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_ROOM);
                                        newRoom.put(Keys.KEY_ID, roomId);
                                        newRoom.put(Keys.KEY_NAME, "");
                                        newRoom.put(Keys.KEY_AVATAR, "");
                                        newRoom.put(Keys.KEY_TYPE, RoomType.TWO);
                                        newRoom.put(Keys.KEY_STATUS, RoomStatus.NEW);
                                        newRoom.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                                        newRoom.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                                        String finalRoomId = roomId;
                                        database.collection(Keys.KEY_COLLECTION_ROOM)
                                                .document(roomId)
                                                .set(newRoom)
                                                .addOnSuccessListener(unused -> {
                                                    for (String r : recipients) {
                                                        HashMap<String, String> recipient = new HashMap<>();
                                                        recipient.put(Keys.KEY_ROOM_ID, finalRoomId);
                                                        recipient.put(Keys.KEY_USER_ID, r);
                                                        recipient.put(Keys.KEY_STATUS, RecipientStatus.NEW);
                                                        recipient.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                                                        recipient.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                                        //recipient.put(Keys.KEY_USER_ID, r.getUser().getId());
                                                        database.collection(Keys.KEY_COLLECTION_RECIPIENT).add(recipient);
                                                    }

                                                    openChatActivity(finalRoomId);
                                                });
                                    } else {
                                        openChatActivity(roomId);
                                    }
                                });
                    } else {
                        HashMap<String, Object> newRoom = new HashMap<>();
                        String roomId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_ROOM);
                        newRoom.put(Keys.KEY_ID, roomId);
                        newRoom.put(Keys.KEY_NAME, "");
                        newRoom.put(Keys.KEY_AVATAR, "");
                        newRoom.put(Keys.KEY_TYPE, RoomType.TWO);
                        newRoom.put(Keys.KEY_STATUS, RoomStatus.NEW);
                        newRoom.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                        newRoom.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                        String finalRoomId = roomId;
                        database.collection(Keys.KEY_COLLECTION_ROOM)
                                .document(roomId)
                                .set(newRoom)
                                .addOnSuccessListener(unused -> {
                                    for (String r : recipients) {
                                        HashMap<String, String> recipient = new HashMap<>();
                                        recipient.put(Keys.KEY_ROOM_ID, finalRoomId);
                                        recipient.put(Keys.KEY_USER_ID, r);
                                        recipient.put(Keys.KEY_STATUS, RecipientStatus.NEW);
                                        recipient.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                                        recipient.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                        //recipient.put(Keys.KEY_USER_ID, r.getUser().getId());
                                        database.collection(Keys.KEY_COLLECTION_RECIPIENT).add(recipient);
                                    }

                                    openChatActivity(finalRoomId);
                                });
                    }

                });
    }

    private List<String> filter(List<String> myRooms, List<String> friendRooms) {
        Set<String> commonElements = new HashSet<>();

        for (String s1 : myRooms) {
            for (String s2 : friendRooms) {
                if (s1.equals(s2)) {
                    commonElements.add(s1);
                }
            }
        }
        return new ArrayList<String>(commonElements);
    }

    private void openChatActivity(String roomId) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Keys.KEY_ROOM_ID, roomId);
        startActivity(intent);
    }

    @Override
    public void sendDialogResult(CustomDialog.Result result, String request) {
        if (result.equals(CustomDialog.Result.OK)) {
            if (request.equals(REQUEST_BLOCK)) {
                query
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                                newFriend.put(Keys.KEY_FRIEND_ID, selectedUser.getId());
                                newFriend.put(Keys.KEY_STATUS, FriendStatus.BLOCKED);
                                newFriend.put(Keys.KEY_MESSAGE, "hello");
                                newFriend.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                                newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                                database.collection(Keys.KEY_COLLECTION_FRIEND).add(newFriend);
                            } else {
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_STATUS, FriendStatus.BLOCKED);
                                newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                queryDocumentSnapshots.getDocuments().get(0).getReference().update(newFriend);
                            }
                        })
                        .addOnFailureListener(e -> {

                        });

                query2
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_USER_ID, selectedUser.getId());
                                newFriend.put(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                                newFriend.put(Keys.KEY_STATUS, FriendStatus.NONE);
                                newFriend.put(Keys.KEY_MESSAGE, "hello");
                                newFriend.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                                newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                                database.collection(Keys.KEY_COLLECTION_FRIEND).add(newFriend);
                            } else {
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_STATUS, FriendStatus.NONE);
                                newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                queryDocumentSnapshots.getDocuments().get(0).getReference().update(newFriend);
                            }
                        })
                        .addOnFailureListener(e -> {

                        });
            }
            if (request.equals(REQUEST_UNBLOCK)) {
                Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_FRIEND)
                        .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                        .whereEqualTo(Keys.KEY_FRIEND_ID, selectedUser.getId())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
                                documentReference.update(Keys.KEY_STATUS, FriendStatus.NONE);
                            } else {
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                                newFriend.put(Keys.KEY_FRIEND_ID, selectedUser.getId());
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
                        .whereEqualTo(Keys.KEY_USER_ID, selectedUser.getId())
                        .limit(1)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
                                documentReference.update(Keys.KEY_STATUS, FriendStatus.NONE);
                            } else {
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                                newFriend.put(Keys.KEY_USER_ID, selectedUser.getId());
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
                            binding.btnUnBlock.setVisibility(View.GONE);
                            binding.btnBlock.setVisibility(View.VISIBLE);
                            binding.btnMakeFriend.setVisibility(View.VISIBLE);
//                    changeBlockUI(false);
//                    binding.lyBlock.setVisibility(View.GONE);
//                    binding.footerView.setVisibility(View.VISIBLE);
                        });
            }
            if (request.equals(REQUEST_UNFRIEND)) {
                query
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                                newFriend.put(Keys.KEY_FRIEND_ID, selectedUser.getId());
                                newFriend.put(Keys.KEY_STATUS, FriendStatus.NONE);
                                newFriend.put(Keys.KEY_MESSAGE, "hello");
                                newFriend.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                                newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                                database.collection(Keys.KEY_COLLECTION_FRIEND).add(newFriend);
                            } else {
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_STATUS, FriendStatus.NONE);
                                newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                queryDocumentSnapshots.getDocuments().get(0).getReference().update(newFriend);
                            }
                        })
                        .addOnFailureListener(e -> {

                        });

                query2
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_USER_ID, selectedUser.getId());
                                newFriend.put(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                                newFriend.put(Keys.KEY_STATUS, FriendStatus.NONE);
                                newFriend.put(Keys.KEY_MESSAGE, "hello");
                                newFriend.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                                newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                                database.collection(Keys.KEY_COLLECTION_FRIEND).add(newFriend);
                            } else {
                                HashMap<String, Object> newFriend = new HashMap<>();
                                newFriend.put(Keys.KEY_STATUS, FriendStatus.NONE);
                                newFriend.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                queryDocumentSnapshots.getDocuments().get(0).getReference().update(newFriend);
                            }
                        })
                        .addOnFailureListener(e -> {

                        });
            }
        }

    }

    private void updateUI() {
        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(selectedUser.getImage()));
        binding.etEmail.setText(selectedUser.getEmail());
        binding.tvName.setText(selectedUser.getFullName());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (this.getIntent().getBooleanExtra(Constants.OPEN_FROM_NOTIFICATION, false)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
        return super.onSupportNavigateUp();
    }
}