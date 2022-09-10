package com.example.loqui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.loqui.constants.CallResponse;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.MessageType;
import com.example.loqui.constants.NotificationType;
import com.example.loqui.constants.Receiver;
import com.example.loqui.data.model.CallDetail;
import com.example.loqui.data.model.Room;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityIncomingCallBinding;
import com.example.loqui.firebase.MessagingService;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

public class IncomingCallActivity extends AppCompatActivity {

    //public static IncomingCallActivity incomingCallActivity;
    private ActivityIncomingCallBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    //    private ChatMessage chatMessage;
    private Query query;
    private String roomId;
    private Room room;
    //    private String callId;
    private CallDetail call;
    private User me;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(BROADCAST_OPEN_JITSI)) {
//                openJitsi();
//            }
//        }
//    };

    public static final String BROADCAST_OPEN_JITSI = "open_jitsi";
    public static final String BROADCAST_CLOSE_JITSI = "close_jitsi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomingCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //incomingCallActivity = this;

        init();
        binding.btnAccept.setOnClickListener(v -> {
            onBtnAcceptClicked();
        });
//
        binding.btnDecline.setOnClickListener(v -> {
            onBtnDeclinedClicked();
        });

        registerForBroadcastMessages();

        //this.roomId = getIntent().getExtras().getString(Constants.ROOM);
//        getRoom(this.roomId);

    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        //callId = this.getIntent().getStringExtra(Keys.KEY_ID);
        call = (CallDetail) this.getIntent().getSerializableExtra(Constants.CALL);

//        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(call.getAvatar()));
//        binding.tvName.setText(call.getName());
        if (call.getCallType().equals(MessageType.AUDIO_CALL)) {
            binding.ivCallType.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_round_keyboard_voice_24));
        } else if (call.getCallType().equals(MessageType.VIDEO_CALL)) {
            binding.ivCallType.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_round_videocam_24));
        }

//        if (call.getRoom() != null) {
//            database.collection(Keys.KEY_COLLECTION_ROOM)
//                    .document(call.getRoom().getId())
//                    .get()
//                    .addOnSuccessListener(documentSnapshot -> {
//                        Room room = new Room();
//                        room.setId(documentSnapshot.getString(Keys.KEY_ID));
//                        room.setName(documentSnapshot.getString(Keys.KEY_NAME));
//                        room.setAvatar(documentSnapshot.getString(Keys.KEY_AVATAR));
//                        call.setRoom(room);
//
//                        binding.tvName.setText(room.getName());
//                        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(room.getAvatar()));
//                    });
//        } else {
        FirebaseHelper.findUser(database, call.getCaller().getId())
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        User caller = new User();
                        caller.setId(documentSnapshot.getString(Keys.KEY_ID));
                        caller.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
                        caller.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                        caller.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                        caller.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));

                        call.setCaller(caller);

                        binding.tvName.setText(call.getCaller().getFullName());
                        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(call.getCaller().getImage()));
                    }
                });
        //}

        me = new User();
        me.setId(preferenceManager.getString(Keys.KEY_USER_ID));
        me.setFirstName(preferenceManager.getString(Keys.KEY_FIRSTNAME));
        me.setLastName(preferenceManager.getString(Keys.KEY_LASTNAME));
        me.setImage(preferenceManager.getString(Keys.KEY_AVATAR));
        me.setToken(preferenceManager.getString(Keys.KEY_FCM_TOKEN));


//        chatMessage = (ChatMessage) this.getIntent().getSerializableExtra("chat_message");
//        query = database.collection(Keys.KEY_MESSAGE)
//                .whereEqualTo(Keys.KEY_ROOM_ID, chatMessage.getRoomId());

//        query.addSnapshotListener(eventListener);
    }

    //    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
//        if (error != null) {
//            return;
//        }
//        if (value != null) {
//            for (DocumentChange documentChange : value.getDocumentChanges()) {
//                if (documentChange.getType() == DocumentChange.Type.ADDED) {
//
//                }
//            }
//        }
//    };
//
    private void openJitsi() {
        Intent intent = new Intent(this, JitsiActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.CALL, (Serializable) call);
        startActivity(intent);
    }

    private void onBtnAcceptClicked() {

        try {
            JSONArray tokens = new JSONArray();
            tokens.put(call.getCaller().getToken());

            JSONObject data = new JSONObject();
            data.put(Keys.KEY_NOTIFICATION_TYPE, NotificationType.CALL);
            data.put(Constants.CALL_ID, call.getId());
            data.put(Constants.CALL_TYPE, call.getCallType());
            data.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());

//            if (this.receivers.size() > 1) {
//                data.put(Keys.KEY_ROOM_ID, room.getId());
//            }

            JSONObject caller = new JSONObject();
            caller.put(Keys.KEY_ID, call.getCaller().getId());
            caller.put(Keys.KEY_FCM_TOKEN, call.getCaller().getToken());
            data.put(Constants.CALLER, caller);
            data.put(Keys.KEY_CALL_RESPONSE, CallResponse.ACCEPT_INVITATION);

            JSONObject body = new JSONObject();
            body.put(Keys.REMOTE_MSG_DATA, data);
            body.put(Keys.REMOTE_MSG_REGISTRATION_IDS, tokens);

            MessagingService.sendNotification(getApplicationContext(), body.toString());

            openJitsi();

        } catch (Exception ex) {
            MyToast.showShortToast(this, ex.getMessage());
        }

//        HashMap<String, Object> call = new HashMap<>();
//        call.put(Keys.KEY_ROOM_ID, chatMessage.getRoomId());
//        call.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
//        call.put(Keys.KEY_MESSAGE, chatMessage.getMessage());
//        call.put(Keys.KEY_TYPE, chatMessage.getType());
//        call.put(Keys.KEY_STATUS, MessageStatus.ACCEPT);
//        call.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
//        call.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//        database.collection(Keys.KEY_COLLECTION_CHAT)
//                .add(call).
//                addOnSuccessListener(documentReference -> {
//                    documentReference.update(Keys.KEY_ID, documentReference.getId())
//                            .addOnSuccessListener(unused -> {
//                                initJitsi();
//                            });
//
//                }).addOnFailureListener(e -> {
//
//                });

//        try {
//            JSONArray tokens = new JSONArray();
//            tokens.put(call.getCaller().getToken());
//
//            JSONObject data = new JSONObject();
//
//            data.put(Constants.CALL_ID, call.getId());
//            data.put(Keys.KEY_NOTIFICATION_TYPE, NotificationType.CALL);
//            data.put(Keys.KEY_CALL_RESPONSE, CallResponse.ACCEPT_INVITATION);
//
//            JSONObject body = new JSONObject();
//            body.put(Keys.REMOTE_MSG_DATA, data);
//            body.put(Keys.REMOTE_MSG_REGISTRATION_IDS, tokens);
//
//            sendNotification(body.toString());
//
//        } catch (Exception ex) {
//            MyToast.showShortToast(this, ex.getMessage());
//        }

    }

    //
//    private void initJitsi() {
//        try {
//            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
//                    .setServerURL(new URL(Constants.JITSI_SERVER))
//                    .setRoom(chatMessage.getRoomId())
//                    .setSubject(chatMessage.getRoomId())
////                    .setSubject("room1")
//                    .setAudioMuted(false)
//                    .setVideoMuted(true)
//                    .setAudioOnly(true)
////                    .setWelcomePageEnabled(false)
//                    .setConfigOverride("requireDisplayName", true)
//                    .build();
//
//            JitsiMeetActivity.launch(this.getApplicationContext(), options);
//        } catch (MalformedURLException ex) {
//            Timber.tag(this.getClass().getName()).d(ex);
//        }
//
//    }
//
    private void onBtnDeclinedClicked() {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(call.getCaller().getToken());

            JSONObject data = new JSONObject();
            data.put(Keys.KEY_NOTIFICATION_TYPE, NotificationType.CALL);
            data.put(Constants.CALL_ID, call.getId());
            data.put(Constants.CALL_TYPE, call.getCallType());
            data.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());

            data.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));

//            if (this.receivers.size() > 1) {
//                data.put(Keys.KEY_ROOM_ID, room.getId());
//            }

            JSONObject caller = new JSONObject();
            caller.put(Keys.KEY_ID, call.getCaller().getId());
            caller.put(Keys.KEY_FCM_TOKEN, call.getCaller().getToken());
            data.put(Constants.CALLER, caller);
            data.put(Keys.KEY_CALL_RESPONSE, CallResponse.DECLINE_INVITATION);

            JSONObject body = new JSONObject();
            body.put(Keys.REMOTE_MSG_DATA, data);
            body.put(Keys.REMOTE_MSG_REGISTRATION_IDS, tokens);

            MessagingService.sendNotification(getApplicationContext(), body.toString());

            finish();

        } catch (Exception ex) {
            MyToast.showShortToast(this, ex.getMessage());
        }
//        HashMap<String, Object> call = new HashMap<>();
//        call.put(Keys.KEY_ROOM_ID, chatMessage.getRoomId());
//        call.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
//        call.put(Keys.KEY_MESSAGE, chatMessage.getMessage());
//        call.put(Keys.KEY_TYPE, chatMessage.getType());
//        call.put(Keys.KEY_STATUS, MessageStatus.CANCEL);
//        call.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
//        call.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//        database.collection(Keys.KEY_COLLECTION_CHAT)
//                .add(call).
//                addOnSuccessListener(documentReference -> {
//                    documentReference.update(Keys.KEY_ID, documentReference.getId());
//                    finish();
//
//                }).addOnFailureListener(e -> {
//
//                });
    }
//
//    private void getRoom(String roomId) {
//        database.collection(Keys.KEY_COLLECTION_ROOM)
//                .document(roomId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                });
//    }


    //        try {
//            // Somewhere early in your app.
//            JitsiMeetConferenceOptions defaultOptions
//                    = new JitsiMeetConferenceOptions.Builder()
//                    .setServerURL(new URL(Constants.JITSI_SERVER))
//                    // When using JaaS, set the obtained JWT here
//                    //.setToken("MyJWT")
//                    // Different features flags can be set
//                    // .setFeatureFlag("toolbox.enabled", false)
//                    // .setFeatureFlag("filmstrip.enabled", false)
//                    .setFeatureFlag("welcomepage.enabled", false)
//                    .build();
//            JitsiMeet.setDefaultConferenceOptions(defaultOptions);
//            // ...
//            // Build options object for joining the conference. The SDK will merge the default
//            // one we set earlier and this one when joining.
//            JitsiMeetConferenceOptions options
//                    = new JitsiMeetConferenceOptions.Builder()
//                    .setRoom(UUID.randomUUID().toString())
//                    .setAudioOnly(true)
//                    // Settings for audio and video
//                    //.setAudioMuted(true)
//                    //.setVideoMuted(true)
//                    .build();
//            // Launch the new activity with the given options. The launch() method takes care
//            // of creating the required Intent and passing the options.
//            JitsiMeetActivity.launch(this, options);
//
//        } catch (MalformedURLException ex) {
//            Timber.tag(this.getClass().getName()).e(ex.getMessage());
//            ex.printStackTrace();
//        }


//    @Override
//    protected void onStart() {
//        super.onStart();
//        IntentFilter intentFilter = new IntentFilter(BROADCAST_OPEN_JITSI);
//        registerReceiver(broadcastReceiver, intentFilter);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        unregisterReceiver(broadcastReceiver);
//    }

    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();

        /* This registers for every possible event sent from JitsiMeetSDK
           If only some of the events are needed, the for loop can be replaced
           with individual statements:
           ex:  intentFilter.addAction(BroadcastEvent.Type.AUDIO_MUTED_CHANGED.getAction());
                intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
                ... other events
         */
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    // Example for handling different JitsiMeetSDK events
    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);

//            switch (event.getType().getAction()) {
//                case Receiver
//                        .CLOSE_INCOMING_CALL_ACTIVITY:
//
//                    break;
//                case Receiver.CLOSE_INCOMING_CALL_ACTIVITY:
//                    //participantCount++;
//                    Timber.i("Participant joined%s", event.getData().get("name"));
//                    break;
////                case CONFERENCE_TERMINATED:
////                    break;
//            }

            if (event.getType().getAction().equals(Receiver.CLOSE_INCOMING_CALL_ACTIVITY)) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}