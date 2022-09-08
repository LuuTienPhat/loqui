package com.example.loqui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.example.loqui.constants.CallResponse;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.MessageType;
import com.example.loqui.constants.NotificationType;
import com.example.loqui.constants.Receiver;
import com.example.loqui.data.model.CallDetail;
import com.example.loqui.data.model.Room;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityOutgoingCallBinding;
import com.example.loqui.firebase.MessagingService;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class OutgoingCallActivity extends BaseActivity {

    private ActivityOutgoingCallBinding binding;
    private String callType;
    //    private List<Recipient> recipients;
    //private String roomId;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String callId;
    private List<User> receivers;
    private Room room;
    private HashMap<String, String> participants;
    private User me;
    private CallDetail call;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IncomingCallActivity.BROADCAST_OPEN_JITSI.equals(intent.getAction())) {
                openJitsi();
            }
            if (Receiver.CLOSE_OUTGOING_CALL_ACTIVITY.equals(intent.getAction())) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
//        makeCall();

        binding.btnDecline.setOnClickListener(v -> {
            onBtnDeclinedClicked();
        });

//        addListeners();
        //listenCall();
    }


    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        this.callType = getIntent().getStringExtra(Constants.CALL_TYPE);
        this.room = (Room) getIntent().getSerializableExtra(Constants.ROOM);
        this.receivers = (List<User>) getIntent().getSerializableExtra(Constants.RECEIVERS);

        callId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_CHAT);

        if (this.callType.equals(MessageType.AUDIO_CALL)) {
            binding.ivCallType.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_round_keyboard_voice_24));
        } else if (this.callType.equals(MessageType.VIDEO_CALL)) {
            binding.ivCallType.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_round_videocam_24));
        }

        if (receivers.size() == 1) {
            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(receivers.get(0).getImage()));
            binding.tvName.setText(receivers.get(0).getFullName());
        } else {
            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(room.getAvatar()));
            binding.tvName.setText(room.getName());
        }

        this.me = new User();
        this.me.setId(preferenceManager.getString(Keys.KEY_USER_ID));
        this.me.setFirstName(preferenceManager.getString(Keys.KEY_FIRSTNAME));
        this.me.setLastName(preferenceManager.getString(Keys.KEY_LASTNAME));
        this.me.setImage(preferenceManager.getString(Keys.KEY_AVATAR));
        this.me.setToken(preferenceManager.getString(Keys.KEY_FCM_TOKEN));


//
//        this.receivers.add(this.me);

        this.participants = new HashMap<>();

        this.call = new CallDetail();

        try {
            JSONArray tokens = new JSONArray();
            for (User user : this.receivers) {
                tokens.put(user.getToken());
            }
            JSONObject data = new JSONObject();
            data.put(Keys.KEY_NOTIFICATION_TYPE, NotificationType.CALL);
            data.put(Constants.CALL_ID, callId);
            data.put(Constants.CALL_TYPE, callType);
            data.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());

            call.setId(callId);
            call.setCallType(callType);
            call.setCreatedDate(Utils.currentTimeMillis());

            if (this.receivers.size() > 1) {
//                data.put(Keys.KEY_AVATAR, room.getAvatar());
                data.put(Keys.KEY_ROOM_ID, room.getId());
//                data.put(Keys.KEY_NAME, room.getName());
            }
//            else {
////                data.put(Keys.KEY_AVATAR, me.getImage());
//                data.put(Keys.KEY_NAME, me.getFullName());
//            }

            JSONObject caller = new JSONObject();
            caller.put(Keys.KEY_ID, me.getId());
//            caller.put(Keys.KEY_FIRSTNAME, me.getFirstName());
//            caller.put(Keys.KEY_LASTNAME, me.getLastName());
            caller.put(Keys.KEY_FCM_TOKEN, me.getToken());
            data.put(Constants.CALLER, caller);

//            data.put(Keys.KEY_SENDER_ID, me.getId());
            data.put(Keys.KEY_CALL_RESPONSE, CallResponse.NEW_INVITATION);

            call.setCaller(me);

//            JSONArray participants = new JSONArray();
//            for (User user : this.receivers) {
//                JSONObject object = new JSONObject();
//                object.put(Keys.KEY_USER_ID, user.getId());
//                object.put(Keys.KEY_FIRSTNAME, user.getFirstName());
//                object.put(Keys.KEY_LASTNAME, user.getLastName());
//                object.put(Keys.KEY_AVATAR, user.getImage());
//                participants.put(object);
//            }
//
//            data.put(Constants.RECEIVERS, participants);

            JSONObject body = new JSONObject();
            body.put(Keys.REMOTE_MSG_DATA, data);
            body.put(Keys.REMOTE_MSG_REGISTRATION_IDS, tokens);

            MessagingService.sendNotification(getApplicationContext(), body.toString());

        } catch (Exception ex) {
            MyToast.showShortToast(this, ex.getMessage());
        }

    }

    private void openJitsi() {
        Intent intent = new Intent(this, JitsiActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.CALL, (Serializable) call);
        startActivity(intent);
    }

    //    private void listenCall() {
//        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
//                .whereEqualTo(Keys.KEY_ROOM_ID, callId)
//                .addSnapshotListener(eventListener);
//    }
//
//    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
//        if (error != null) {
//            return;
//        }
//        if (value != null) {
//            for (DocumentChange documentChange : value.getDocumentChanges()) {
//                if (documentChange.getType() == DocumentChange.Type.ADDED) {
//                    QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
//                    this.participants.put(queryDocumentSnapshot.getString(Keys.KEY_USER_ID), queryDocumentSnapshot.getString(Keys.KEY_STATUS));
//                }
//                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
//                    QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
//                    this.participants.put(queryDocumentSnapshot.getString(Keys.KEY_USER_ID), queryDocumentSnapshot.getString(Keys.KEY_STATUS));
////                    initJitsi();
//                    Intent intent = new Intent(getApplicationContext(), OutgoingCallActivity.class);
//                    startActivity(intent);
//
//                    if (!isThereAnyOne()) {
//                        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
//                                .whereEqualTo(Keys.KEY_ROOM_ID, callId)
//                                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
//                                .get()
//                                .addOnSuccessListener(queryDocumentSnapshots -> {
//                                    DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
//                                    documentReference.update(Keys.KEY_STATUS, RecipientStatus.DECLINE);
//                                });
//
//                        database.collection(Keys.KEY_COLLECTION_ROOM).document(callId)
//                                .update(Keys.KEY_STATUS, RoomStatus.DELETED);
//
//                        finish();
//                    }
//                }
//            }
//        }
//    };

//    private void addListeners() {
//        database.collection(Keys.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Keys.KEY_ROOM_ID, roomId)
//                .addSnapshotListener(eventListener);
//    }

//    private boolean isThereAnyOne() {
//        int count = 0;
//
//        for (String i : this.participants.keySet()) {
//            if (!i.equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
//                {
//                    String status = this.participants.get(i);
//                    if (status.equals(RecipientStatus.ACCEPT)) {
//                        count++;
//                    }
//                }
//            }
//        }
//
//        return count > 0 ? true : false;
//    }

//    private List<String> filter(HashMap<String, List<String>> data) {
//        List<String> rooms = new ArrayList<>();
//
//        for (String i : data.keySet()) {
//            List<String> values = data.get(i);
//            if (values.size() > 2) {
//                rooms.add(i);
//            }
//        }
//
//        return rooms;
//    }

//    private void makeCall() {
//        HashMap<String, Object> room = new HashMap<>();
//        room.put(Keys.KEY_ID, callId);
//        room.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
//        room.put(Keys.KEY_TYPE, callType);
//        room.put(Keys.KEY_STATUS, RoomStatus.NEW);
//        room.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
//        room.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//        database.collection(Keys.KEY_COLLECTION_ROOM)
//                .document(callId)
//                .set(room)
//                .addOnSuccessListener(unused -> {
//
//                    List<Task<DocumentReference>> tasks = new ArrayList<>();
//                    for (User receiver : this.receivers) {
//                        HashMap<String, Object> recipient = new HashMap<>();
//                        recipient.put(Keys.KEY_ROOM_ID, callId);
//                        recipient.put(Keys.KEY_USER_ID, receiver.getId());
//                        recipient.put(Keys.KEY_STATUS, receiver.getId().equals(preferenceManager.getString(Keys.KEY_USER_ID)) ? RecipientStatus.ACCEPT : RecipientStatus.NONE);
//                        Task<DocumentReference> t = database.collection(Keys.KEY_COLLECTION_RECIPIENT).add(recipient);
//                        tasks.add(t);
//                    }
//
//                    Tasks.whenAllSuccess(tasks)
//                            .addOnSuccessListener(objects -> {
//                                listenCall();
//                            });
//                });
//
//
////        if (callType == CallType.AUDIO_CALL) {
////            call.put(Keys.KEY_TYPE, callType.toString());
////        }
////        if (callType == CallType.VIDEO_CALL) {
////            call.put(Keys.KEY_TYPE, callType.toString());
////        }
//
////        database.collection(Keys.KEY_COLLECTION_CHAT)
////                .add(call)
////                .addOnSuccessListener(documentReference -> {
////
////                })
////                .addOnFailureListener(e -> {
////                    MyToast.showLongToast(this.getApplicationContext(), e.getMessage());
////                    Timber.d(e);
////                });
//    }

    private void initJitsi() {
        try {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL(Constants.JITSI_SERVER))
                    .setRoom(callId)
                    .setSubject(room.getName())
                    .setAudioMuted(false)
                    .setVideoMuted(true)
                    .setAudioOnly(true)
//                    .setWelcomePageEnabled(false)
                    .setConfigOverride("requireDisplayName", true)
                    .build();

            JitsiMeetActivity.launch(this.getApplicationContext(), options);

        } catch (MalformedURLException ex) {
            Timber.tag(this.getClass().getName()).d(ex);
        }
    }

    private void onBtnDeclinedClicked() {
        try {
            JSONArray tokens = new JSONArray();
            for (User user : this.receivers) {
                tokens.put(user.getToken());
            }
            JSONObject data = new JSONObject();
            data.put(Keys.KEY_NOTIFICATION_TYPE, NotificationType.CALL);
            data.put(Constants.CALL_ID, callId);
            data.put(Keys.KEY_CALL_TYPE, callType);
            data.put(Keys.KEY_CALL_RESPONSE, CallResponse.CANCEL_INVITATION);

            JSONObject caller = new JSONObject();
            caller.put(Keys.KEY_ID, me.getId());
            caller.put(Keys.KEY_FCM_TOKEN, me.getToken());
            data.put(Constants.CALLER, caller);

            JSONObject body = new JSONObject();
            body.put(Keys.REMOTE_MSG_DATA, data);
            body.put(Keys.REMOTE_MSG_REGISTRATION_IDS, tokens);

            MessagingService.sendNotification(getApplicationContext(), body.toString());

            finish();

        } catch (Exception ex) {
            MyToast.showShortToast(this, ex.getMessage());
        }

//        List<Task<?>> tasks = new ArrayList<>();
//        Task<Void> t1 = database.collection(Keys.KEY_COLLECTION_ROOM).document(callId).update(Keys.KEY_STATUS, RoomStatus.DELETED);
//        tasks.add(t1);
//
//        for (User receiver : this.receivers) {
//            Task<QuerySnapshot> t = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
//                    .whereEqualTo(Keys.KEY_ROOM_ID, callId)
//                    .whereEqualTo(Keys.KEY_USER_ID, receiver.getId())
//                    .get()
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
//                        documentReference.update(Keys.KEY_STATUS, RecipientStatus.DECLINE);
//                    });
//            tasks.add(t);
//        }
//
//        Tasks.whenAllComplete(tasks)
//                .addOnSuccessListener(tasks1 -> {
//                    finish();
//                });
//        HashMap<String, Object> call = new HashMap<>();
//        call.put(Keys.KEY_STATUS, MessageStatus.CANCEL);
//        call.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//        database.collection(Keys.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Keys.KEY_ID, callId)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                        DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
//                        documentReference.update(Keys.KEY_STATUS, MessageStatus.CANCEL);
//                        finish();
//                    }
//
//                })
//                .addOnFailureListener(e -> {
//                    MyToast.showShortToast(this.getApplicationContext(), e.getMessage());
//                    Timber.e(e);
//                });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IncomingCallActivity.BROADCAST_OPEN_JITSI);
        intentFilter.addAction(Receiver.CLOSE_OUTGOING_CALL_ACTIVITY);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }


}