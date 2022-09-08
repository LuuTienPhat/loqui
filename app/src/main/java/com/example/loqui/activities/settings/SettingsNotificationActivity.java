package com.example.loqui.activities.settings;

import android.os.Bundle;

import com.example.loqui.BaseActivity;
import com.example.loqui.databinding.ActivitySettingsNotificationBinding;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SettingsNotificationActivity extends BaseActivity {

    private ActivitySettingsNotificationBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private boolean doNotDisturb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

//        FirebaseHelper.getSettings(database, preferenceManager.getString(Keys.KEY_USER_ID))
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
//                        doNotDisturb = Boolean.TRUE.equals(documentSnapshot.getBoolean(Keys.KEY_SETTING_DO_NOT_DISTURB));
//                        binding.swDoNotDisturb.setChecked(doNotDisturb);
//                    }
//                });

        binding.swDoNotDisturb.setChecked(preferenceManager.getBoolean(Keys.KEY_SETTING_DO_NOT_DISTURB));

        binding.swDoNotDisturb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            HashMap<String, Object> settings = new HashMap<>();
            settings.put(Keys.KEY_SETTING_DO_NOT_DISTURB, isChecked);
            settings.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

            database.collection(Keys.KEY_COLLECTION_SETTINGS)
                    .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            queryDocumentSnapshots.getDocuments().get(0).getReference()
                                    .update(settings)
                                    .addOnSuccessListener(unused -> {
                                        MyToast.showLongToast(this.getApplicationContext(), "Setting changed");

                                        preferenceManager.putBoolean(Keys.KEY_SETTING_DO_NOT_DISTURB, isChecked);
                                    });

                        }
                    });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}