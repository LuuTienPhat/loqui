package com.example.loqui.old;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.loqui.constants.Constants;
import com.example.loqui.constants.MessageType;
import com.example.loqui.data.model.CallDetail;
import com.example.loqui.data.model.User;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.facebook.react.modules.core.PermissionListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;

import java.net.URL;


public class JitsiActivity extends FragmentActivity implements JitsiMeetActivityInterface {
    private JitsiMeetView view;
    private BroadcastReceiver broadcastReceiver;
    private CallDetail call;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
//    private String callId;
//    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction())) {
                    onBackPressed();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction());
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        init();

    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        call = (CallDetail) this.getIntent().getSerializableExtra(Constants.CALL);

        if (call.getRoom() != null) {
            database.collection(Keys.KEY_COLLECTION_ROOM)
                    .document(call.getRoom().getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
//                        Room room = new Room();
//                        room.setId(documentSnapshot.getString(Keys.KEY_ID));
//                        room.setName(documentSnapshot.getString(Keys.KEY_NAME));
//                        room.setAvatar(documentSnapshot.getString(Keys.KEY_AVATAR));
//                        call.setRoom(room);
                        call.setName(documentSnapshot.getString(Keys.KEY_NAME));

                        initJitsi();
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
//                            caller.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
//
//                            call.setCaller(caller);

                            call.setName(caller.getFullName());

                            initJitsi();
                        }
                    });
        }
    }

    private void initJitsi() {
        try {
            view = new JitsiMeetView(this);
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL(Constants.JITSI_SERVER))
                    .setRoom(call.getId())
                    .setSubject(call.getName())
                    .setFeatureFlag("welcomepage.enabled", false)
                    .setFeatureFlag("add-people.enabled", false)
                    .setFeatureFlag("chat.enabled", false)
                    .setFeatureFlag("invite.enabled", false)
                    .setFeatureFlag("raise-hand.enabled", false)
                    .setFeatureFlag("reactions.enabled", false)
                    .setFeatureFlag("recording.enabled", false)
                    .setAudioMuted(true)
                    .setVideoMuted(call.getCallType().equals(MessageType.AUDIO_CALL))
                    .build();

//                    .setServerURL(new URL("https://meet.jit.si"))
//                    .setRoom(call.getId())
//                    .setSubject(call.getName())
//                    .setAudioMuted(false)
//                    .setVideoMuted(false)
//                    .setFeatureFlag("welcomepage.enabled", false)
//                    .setFeatureFlag("add-people.enabled", false)
//                    .setFeatureFlag("chat.enabled", false)
//                    .setFeatureFlag("invite.enabled", false)
//                    .setFeatureFlag("raise-hand.enabled", false)
//                    .setFeatureFlag("reactions.enabled", false)
//                    .setFeatureFlag("recording.enabled", false)
//                    .build();

            view.join(options);
            setContentView(view);

        } catch (Exception ex) {
            MyToast.showLongToast(this, ex.getMessage());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        view.dispose();
        view = null;

        JitsiMeetActivityDelegate.onHostDestroy(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JitsiMeetActivityDelegate.onNewIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JitsiMeetActivityDelegate.onHostResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        JitsiMeetActivityDelegate.onHostPause(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JitsiMeetActivityDelegate.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        JitsiMeetActivityDelegate.onBackPressed();
        finish();
    }

    @Override
    public void requestPermissions(String[] strings, int i, PermissionListener permissionListener) {

    }
}
