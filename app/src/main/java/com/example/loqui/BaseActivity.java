package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.constants.UserAvailability;
import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import timber.log.Timber;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        documentReference = database.collection(Keys.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Keys.KEY_DOCUMENT_REF));

//        database.collection(Keys.KEY_COLLECTION_CHAT)
//                .whereIn(Keys.KEY_TYPE, new ArrayList<String>(Arrays.asList(MessageType.AUDIO_CALL, MessageType.VIDEO_CALL)))
//                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    QueryDocumentSnapshot snapshot = documentChange.getDocument();

                    String roomId = snapshot.getString(Keys.KEY_ROOM_ID);
                    database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                            .whereEqualTo(Keys.KEY_ROOM_ID, roomId)
                            .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (queryDocumentSnapshots.getDocuments().size() == 1) {
                                    Intent intent = new Intent(this.getApplicationContext(), IncomingCallActivity.class);

                                    ChatMessage chatMessage = new ChatMessage();
                                    chatMessage.setId(snapshot.getString(Keys.KEY_ID));
                                    chatMessage.setRoomId(snapshot.getString(Keys.KEY_ROOM_ID));
                                    chatMessage.setUserId(snapshot.getString(Keys.KEY_USER_ID));
                                    chatMessage.setMessage(snapshot.getString(Keys.KEY_MESSAGE));
                                    chatMessage.setType(snapshot.getString(Keys.KEY_TYPE));
                                    chatMessage.setStatus(snapshot.getString(Keys.KEY_STATUS));

                                    intent.putExtra("chat_message", chatMessage);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(e -> {
                                MyToast.showShortToast(this.getApplicationContext(), e.getMessage());
                                Timber.e(e);
                            });
                }
            }
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Keys.KEY_AVAILABILITY, UserAvailability.UNAVAILABLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Keys.KEY_AVAILABILITY, UserAvailability.AVAILABLE);
    }
}
