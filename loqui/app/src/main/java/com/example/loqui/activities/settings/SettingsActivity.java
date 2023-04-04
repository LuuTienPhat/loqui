package com.example.loqui.activities.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.loqui.ArchivedChatActivity;
import com.example.loqui.BaseActivity;
import com.example.loqui.CustomStatusActivity;
import com.example.loqui.MessageRequestActivity;
import com.example.loqui.SignInActivity;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.StatusStatus;
import com.example.loqui.data.model.Status;
import com.example.loqui.databinding.ActivitySettingsBinding;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.facebook.AccessToken;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import timber.log.Timber;

public class SettingsActivity extends BaseActivity {
    private ActivitySettingsBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
        setListeners();
        listenDB();
    }

    private void listenDB() {
        database.collection(Keys.KEY_COLLECTION_USERS)
                .whereEqualTo(Keys.KEY_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .addSnapshotListener(listenUser);
        database.collection(Keys.KEY_COLLECTION_YOUR_STATUS)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .addSnapshotListener(listenStatus);
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(preferenceManager.getString(Keys.KEY_AVATAR)));
        String fullName = preferenceManager.getString(Keys.KEY_LASTNAME) + " " + preferenceManager.getString(Keys.KEY_FIRSTNAME);
        binding.tvFullName.setText(fullName);

        String statusName = preferenceManager.getString(Constants.STATUS_NAME);
        if (statusName != null) {
            binding.lyStatus.setVisibility(View.VISIBLE);
            binding.tvStatusName.setText(statusName);
            binding.tvStatusIcon.setText(preferenceManager.getString(Constants.STATUS_ICON));
        }
    }

    private void setListeners() {
        binding.btnMessageRequests.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this.getApplicationContext(), MessageRequestActivity.class);
            startActivity(intent);
        });

        binding.btnLegalPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this.getApplicationContext(), LegalPolicyActivity.class);
            startActivity(intent);
        });

        binding.btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this.getApplicationContext(), SettingsNotificationActivity.class);
            startActivity(intent);
        });

        binding.btnSignOut.setOnClickListener(v -> {
            signOut();
        });

        binding.btnArchiveChats.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), ArchivedChatActivity.class);
            startActivity(intent);
        });

        binding.btnStatus.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), CustomStatusActivity.class);
            startActivity(intent);
        });
        binding.btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), MeActivity.class);
            startActivity(intent);
        });
    }

    private void signOut() {
        AccessToken.expireCurrentAccessToken();

        MyToast.showShortToast(getApplicationContext(), "Signing out...");
//        DocumentReference documentReference = database.collection(Keys.KEY_COLLECTION_USERS).document(
//                preferenceManager.getString(Keys.KEY_USER_ID)
//        );

        database.collection(Keys.KEY_COLLECTION_USERS)
                .whereEqualTo(Keys.KEY_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .whereEqualTo(Keys.KEY_FCM_TOKEN, preferenceManager.getString(Keys.KEY_FCM_TOKEN))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put(Keys.KEY_FCM_TOKEN, "");
                        //updates.put(Keys.KEY_FCM_TOKEN, FieldValue.delete());
                        documentReference.update(updates)
                                .addOnSuccessListener(unused -> {
                                    preferenceManager.clear();
                                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                                    finish();
                                }).addOnFailureListener(e -> {
                                    MyToast.showShortToast(getApplicationContext(), "Unable to sign out");
                                    MyToast.showShortToast(this.getApplicationContext(), e.getMessage());
                                    Timber.d(e);
                                });
                    } else {
                        MyToast.showShortToast(getApplicationContext(), "Unable to sign out");
                    }
                })
                .addOnFailureListener(e -> {
                    MyToast.showShortToast(this.getApplicationContext(), e.getMessage());
                    Timber.d(e);
                });
    }

    private final EventListener<QuerySnapshot> listenUser = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    String avatar = queryDocumentSnapshot.getString(Keys.KEY_AVATAR);
                    String firstname = queryDocumentSnapshot.getString(Keys.KEY_FIRSTNAME);
                    String lastname = queryDocumentSnapshot.getString(Keys.KEY_LASTNAME);
                    String fullName = lastname + " " + firstname;

                    binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(avatar));
                    binding.tvFullName.setText(fullName);
                }
            }
        }
    };

    private final EventListener<QuerySnapshot> listenStatus = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

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

                        preferenceManager.putString(Constants.STATUS_NAME, status.getName());
                        preferenceManager.putString(Constants.STATUS_ICON, status.getIcon());

                        binding.lyStatus.setVisibility(View.VISIBLE);
                        binding.tvStatusIcon.setText(status.getIcon());
                        binding.tvStatusName.setText(status.getName());
                    } else {
                        preferenceManager.putString(Constants.STATUS_NAME, null);
                        preferenceManager.putString(Constants.STATUS_ICON, null);

                        binding.lyStatus.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

//    @Override
//    protected void onResume() {
//        init();
//        super.onResume();
//    }
}