package com.example.loqui.constants;

import com.example.loqui.data.model.User;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BaseMethod {
    private void getFriends(FirebaseFirestore database, PreferenceManager preferenceManager) {
//        loading(true);
        database.collection(Keys.KEY_COLLECTION_FRIEND)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String currentUserId = preferenceManager.getString(Keys.KEY_USER_ID);
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        List<String> us = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            us.add(documentSnapshot.getString(Keys.KEY_FRIEND_ID));
                        }

                        database.collection(Keys.KEY_COLLECTION_USERS)
                                .whereIn(Keys.KEY_ID, us)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                            User user = new User();
                                            user.setId(documentSnapshot.getString(Keys.KEY_ID));
                                            user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                                            user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                            user.setEmail(documentSnapshot.getString(Keys.KEY_EMAIL));
                                            user.setPhone(documentSnapshot.getString(Keys.KEY_PHONE));
                                            user.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
                                            user.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
                                            //users.add(user);
                                        }
                                        //updateRecyclerView();
                                    }
                                    //loading(false);
                                });
                    } else {
                        //loading(false);
                        //showErrorMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    //loading(false);
                    //showErrorMessage();
                });
    }
}
