package com.example.loqui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.loqui.constants.Constants;
import com.example.loqui.constants.MessageType;
import com.example.loqui.constants.RecipientStatus;
import com.example.loqui.data.model.CallDetail;
import com.example.loqui.data.model.User;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class JitsiActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private CallDetail call;
    private User me;
    private Integer participantCount = null;
    private List<String> participants; // tính luôn cả admin

    private ListenerRegistration listenerRegistration = null;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        initJitsi();
        init();


        registerForBroadcastMessages();

        listenerRegistration = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_ROOM_ID, call.getId())
                .addSnapshotListener(recipientListener);
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        call = (CallDetail) this.getIntent().getSerializableExtra(Constants.CALL);

        this.me = new User();
        this.me.setId(preferenceManager.getString(Keys.KEY_USER_ID));
        this.me.setFirstName(preferenceManager.getString(Keys.KEY_FIRSTNAME));
        this.me.setLastName(preferenceManager.getString(Keys.KEY_LASTNAME));

        participantCount = 0;
        this.participants = new ArrayList<>();

        acceptCall();

        if (call.getRoom() != null) {
            database.collection(Keys.KEY_COLLECTION_ROOM)
                    .document(call.getRoom().getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        call.setName(documentSnapshot.getString(Keys.KEY_NAME));
                        openJitsi();
                    });
        } else {
            FirebaseHelper.findUser(database, call.getCaller().getId())
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            User caller = new User();
                            caller.setId(documentSnapshot.getString(Keys.KEY_ID));
                            caller.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
                            caller.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                            caller.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                            call.setName(caller.getFullName());
                            openJitsi();
                        }
                    });
        }
    }

    private void acceptCall() {
        //if (!this.me.getId().equals(call.getCaller().getId())) { // Người tham gia không phải là người gọi thì đổi thành accept
        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_ROOM_ID, call.getId())
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        DocumentReference reference = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        reference.update(Keys.KEY_STATUS, RecipientStatus.ACCEPT);
                    }
                });
        // }
    }

    private void cancelCall() {
        if (this.me.getId().equals(call.getCaller().getId())) { // Người gọi hủy cuộc gọi
            database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                    .whereEqualTo(Keys.KEY_ROOM_ID, call.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                DocumentReference reference = documentSnapshot.getReference();
                                reference.update(Keys.KEY_STATUS, RecipientStatus.DECLINE);
                            }
                        }
                    });
        } else { // nếu một trong những người nghe rời phòng
            database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                    .whereEqualTo(Keys.KEY_ROOM_ID, call.getId())
                    .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            DocumentReference reference = queryDocumentSnapshots.getDocuments().get(0).getReference();
                            reference.update(Keys.KEY_STATUS, RecipientStatus.DECLINE);
                        }
                    });
        }
    }

    private final EventListener<QuerySnapshot> recipientListener = (value, error) -> {
//        loading(true);
        if (error != null) {
//            loading(false);
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    if (!queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.DECLINE)) {
                        participants.add(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));
                    }
                }

                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    if (queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.DECLINE)) {
                        participants.remove(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));

                        if (participants.size() == 1) {
                            sendMessage();
                            stop();
                        }
                    }

                    if (queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.ACCEPT)) {
                        participants.add(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));
                    }
                }
            }
        }
    };

    private void initJitsi() {
        // Initialize default options for Jitsi Meet conferences.
        URL serverURL;
        try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            serverURL = new URL(Constants.JITSI_SERVER);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                // When using JaaS, set the obtained JWT here
                //.setToken("MyJWT")
                // Different features flags can be set
                // .setFeatureFlag("toolbox.enabled", false)
                // .setFeatureFlag("filmstrip.enabled", false)
                .setFeatureFlag("welcomepage.enabled", false)
                .setFeatureFlag("add-people.enabled", false)
                .setFeatureFlag("chat.enabled", false)
                .setFeatureFlag("invite.enabled", false)
                .setFeatureFlag("raise-hand.enabled", false)
                .setFeatureFlag("reactions.enabled", false)
                .setFeatureFlag("recording.enabled", false)
                .setFeatureFlag("prejoinpage.enabled", false)
                .setFeatureFlag("meeting-name.enabled", false)
                .build();

        JitsiMeet.setDefaultConferenceOptions(defaultOptions);
    }

    private void openJitsi() {
        // Build options object for joining the conference. The SDK will merge the default
        // one we set earlier and this one when joining.
        String subject = null;
//        if (call.getCaller().getId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
//            subject = call.getName();
//        } else {
//            subject.
//        }
        JitsiMeetConferenceOptions options
                = new JitsiMeetConferenceOptions.Builder()
                .setRoom(call.getId())
                .setSubject(call.getName())
                .setAudioMuted(false)
                .setVideoMuted(call.getCallType().equals(MessageType.AUDIO_CALL))


                // Settings for audio and video
                //.setAudioMuted(true)
                //.setVideoMuted(true)
                .build();
        // Launch the new activity with the given options. The launch() method takes care
        // of creating the required Intent and passing the options.
        JitsiMeetActivity.launch(this, options);
    }

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

            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    Timber.i("Conference Joined with url%s", event.getData().get("url"));
                    break;
                case PARTICIPANT_JOINED:
                    //participantCount++;
                    Timber.i("Participant joined%s", event.getData().get("name"));
                    break;
                case CONFERENCE_TERMINATED:
                    cancelCall();
                    stop();
                    break;
            }
        }
    }

    // Example for sending actions to JitsiMeetSDK
    private void stop() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
        finish();
    }

    // Example for sending actions to JitsiMeetSDK
    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void sendMessage() {
        String messageContent = this.me.getFullName() + " started" + (call.getCallType().equals(MessageType.AUDIO_CALL) ? " an audio call" : " a video call");
        HashMap<String, Object> message = new HashMap<>();
        String messageId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_CHAT);
        message.put(Keys.KEY_ID, messageId);
        message.put(Keys.KEY_ROOM_ID, call.getRoom().getId());
        message.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
        message.put(Keys.KEY_MESSAGE, messageContent);
        message.put(Keys.KEY_REPLY_ID, "");
        message.put(Keys.KEY_STATUS, "");
        message.put(Keys.KEY_TYPE, call.getCallType().equals(MessageType.AUDIO_CALL) ? MessageType.AUDIO_CALL : MessageType.VIDEO_CALL);
        message.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
        message.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
        database.collection(Keys.KEY_COLLECTION_CHAT).document(messageId).set(message);

    }
}