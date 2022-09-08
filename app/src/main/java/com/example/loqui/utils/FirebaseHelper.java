package com.example.loqui.utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseHelper {

    public static Task<QuerySnapshot> findUser(FirebaseFirestore database, String id) {
        return database.collection(Keys.KEY_COLLECTION_USERS)
                .whereEqualTo(Keys.KEY_ID, id)
                .get();
    }

    public static Task<DocumentSnapshot> findRoom(FirebaseFirestore database, String roomId) {
        return database.collection(Keys.KEY_COLLECTION_ROOM)
                .document(roomId)
                .get();
    }

    public static DocumentReference findUser1(FirebaseFirestore database, String documentId) {
        return database.collection(Keys.KEY_COLLECTION_USERS)
                .document(documentId);
    }

    public static Task<QuerySnapshot> findUsername(FirebaseFirestore database, String username) {
        return database.collection(Keys.KEY_COLLECTION_USERS)
                .whereEqualTo(Keys.KEY_USERNAME, username)
                .get();
    }

    public static String generateId(FirebaseFirestore database, String collection) {
        return database.collection(collection).document().getId();
    }

    public static Task<QuerySnapshot> getSettings(FirebaseFirestore database, String userId) {
        return database.collection(Keys.KEY_COLLECTION_SETTINGS)
                .whereEqualTo(Keys.KEY_USER_ID, userId)
                .get();
    }


}
