package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.adapter.UsersCheckboxAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.FriendStatus;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityCreateGroupBinding;
import com.example.loqui.listeners.UserListener;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity implements UserListener {

    private ActivityCreateGroupBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private MenuItem btnNext;
    private List<User> participants = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private UsersCheckboxAdapter usersCheckboxAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
        //getUsers();
        getFriends();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        users = new ArrayList<>();
    }


    private void getFriends() {
        loading(true);
        database.collection(Keys.KEY_COLLECTION_FRIEND)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String currentUserId = preferenceManager.getString(Keys.KEY_USER_ID);
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        List<String> us = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (documentSnapshot.getString(Keys.KEY_STATUS).equals(FriendStatus.FRIEND)) {
                                us.add(documentSnapshot.getString(Keys.KEY_FRIEND_ID));
                            }
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
                                            user.setAvailability(documentSnapshot.getString(Keys.KEY_AVAILABILITY));
                                            users.add(user);
                                        }
                                        updateRecyclerView();
                                    }
                                    loading(false);
                                });
                    } else {
                        loading(false);
                        showErrorMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showErrorMessage();
                });
    }

    private void updateRecyclerView() {
        usersCheckboxAdapter = new UsersCheckboxAdapter(users, this);
        binding.rvUser.setAdapter(usersCheckboxAdapter);
        binding.rvUser.setVisibility(View.VISIBLE);
    }

//    private void showErrorMessage() {
//        binding.tvErrorMessage.setText(String.format("%s", "No user available"));
//        binding.tvErrorMessage.setVisibility(View.VISIBLE);
//    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu_create_group, menu);
        btnNext = menu.findItem(R.id.btnNext);
        btnNext.setEnabled(false);
        btnNext.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnNext:
                Intent intent = new Intent(CreateGroupActivity.this.getApplicationContext(), RenameGroupActivity.class);
                intent.putExtra(Constants.PARTICIPANTS, (Serializable) participants);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showErrorMessage() {
        binding.tvErrorMessage.setText(String.format("%s", "No friend available"));
        binding.tvErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserClicked(User user) {
    }

    @Override
    public void onUserChecked(User user, boolean isChecked) {
        if (isChecked) {
            participants.add(user);
        } else {
            participants.remove(user);
        }

        if (participants.size() >= 2) {
            btnNext.setEnabled(true);
            btnNext.setVisible(true);
        } else {
            btnNext.setEnabled(false);
            btnNext.setVisible(false);
        }
    }

    @Override
    public void onUserRemoved(User user) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}