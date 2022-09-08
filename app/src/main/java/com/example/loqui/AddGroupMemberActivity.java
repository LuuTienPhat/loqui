package com.example.loqui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.loqui.adapter.UsersCheckboxAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.FriendStatus;
import com.example.loqui.constants.MessageType;
import com.example.loqui.constants.RecipientStatus;
import com.example.loqui.data.model.Room;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityAddGroupMemberBinding;
import com.example.loqui.listeners.UserListener;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddGroupMemberActivity extends AppCompatActivity implements UserListener {

    private ActivityAddGroupMemberBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private List<User> friends = null;
    private List<User> users = null;
    private List<User> participants = null;
    private UsersCheckboxAdapter usersCheckboxAdapter;
    private List<User> members = null;
    private Room room;
    private MenuItem btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddGroupMemberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
        getMember();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        room = (Room) getIntent().getSerializableExtra(Constants.ROOM);

        friends = new ArrayList<>();
        participants = new ArrayList<>();
        users = new ArrayList<>();
        usersCheckboxAdapter = new UsersCheckboxAdapter(this.friends, this);
        binding.rvUser.setAdapter(usersCheckboxAdapter);
    }

    private void getMember() {
        loading(true);
        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> recipients = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String id = documentSnapshot.getString(Keys.KEY_USER_ID);
                        String status = documentSnapshot.getString(Keys.KEY_STATUS);
                        if (!id.equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                            if (!status.equals(RecipientStatus.REMOVED)) {
                                recipients.add(id);
                            }
                        }
                    }

                    database.collection(Keys.KEY_COLLECTION_FRIEND)
                            .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
                                    List<String> frs = new ArrayList<>();
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots2.getDocuments()) {
                                        if (documentSnapshot.getString(Keys.KEY_STATUS).equals(FriendStatus.FRIEND)) {
                                            frs.add(documentSnapshot.getString(Keys.KEY_FRIEND_ID));
                                        }
                                    }

                                    List<String> filter = filter(recipients, frs);

                                    if (!filter.isEmpty()) {
                                        database.collection(Keys.KEY_COLLECTION_USERS)
                                                .whereIn(Keys.KEY_ID, filter)
                                                .get()
                                                .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                    if (!queryDocumentSnapshots3.getDocuments().isEmpty()) {
                                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots3.getDocuments()) {
                                                            User user = new User();
                                                            user.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                            user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                                                            user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                                            user.setEmail(documentSnapshot.getString(Keys.KEY_EMAIL));
                                                            user.setPhone(documentSnapshot.getString(Keys.KEY_PHONE));
                                                            user.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
                                                            user.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
                                                            this.friends.add(user);
                                                        }

                                                        usersCheckboxAdapter.notifyItemRangeInserted(0, this.friends.size());
                                                    }
                                                    loading(false);
                                                });
                                    } else {
                                        loading(false);
                                        showErrorMessage();
                                    }
                                } else {
                                    loading(false);
                                    showErrorMessage();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    showErrorMessage();
                });

    }

    private List<String> filter(List<String> recipients, List<String> friends) {

        for (int i = 0; i < recipients.size(); i++) {
            for (int j = 0; j < friends.size(); j++) {
                if (recipients.get(i).equals(friends.get(j))) {
                    friends.remove(j);
                    break;
                }
            }
        }
        return friends;
    }

    @Override
    public void onUserChecked(User user, boolean isChecked) {
        if (isChecked) {
            participants.add(user);
        } else {
            participants.remove(user);
        }

        if (participants.isEmpty()) {
            btnFinish.setEnabled(false);
        } else {
            btnFinish.setEnabled(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_add_group_member, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.btnSearch).getActionView();
        searchView.setQueryHint("Search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        btnFinish = menu.findItem(R.id.btnFinish);
        if (participants.isEmpty()) {
            btnFinish.setEnabled(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btnFinish) {
            List<Task<?>> taskList = new ArrayList<>();
            List<String> names = new ArrayList<>();

//            List<String> ids = new ArrayList<>();
//            for (User participant : participants) {
//                ids.add(participant.getId());
//            }

            for (User participant : participants) {

                Task<QuerySnapshot> t2 = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                        .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                        .whereEqualTo(Keys.KEY_USER_ID, participant.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            HashMap<String, Object> recipient = new HashMap<>();

                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                recipient.put(Keys.KEY_STATUS, RecipientStatus.NEW);
                                recipient.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                                Task<Void> t = queryDocumentSnapshots.getDocuments().get(0).getReference().update(recipient);
                                taskList.add(t);
                            } else {
                                recipient.put(Keys.KEY_ROOM_ID, room.getId());
                                recipient.put(Keys.KEY_USER_ID, participant.getId());
                                recipient.put(Keys.KEY_STATUS, RecipientStatus.NEW);
                                recipient.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                                recipient.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                                Task<DocumentReference> t = database.collection(Keys.KEY_COLLECTION_RECIPIENT).add(recipient);
                                taskList.add(t);
                            }

                        });

                taskList.add(t2);

                names.add(participant.getFullName());

            }

            Tasks
                    .whenAllComplete(taskList).addOnSuccessListener(tasks -> {
                        String n = String.join(", ", names);
                        sendMessage(n + " were added to the group");
                        finish();
                    });
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(String messageContent) {
        HashMap<String, Object> message = new HashMap<>();
        String messageId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_CHAT);
        message.put(Keys.KEY_ID, messageId);
        message.put(Keys.KEY_ROOM_ID, room.getId());
        message.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
        message.put(Keys.KEY_MESSAGE, messageContent);
        message.put(Keys.KEY_REPLY_ID, "");
        message.put(Keys.KEY_STATUS, "");
        message.put(Keys.KEY_TYPE, MessageType.STATUS);
        message.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
        message.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
        database.collection(Keys.KEY_COLLECTION_CHAT).document(messageId).set(message);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
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
    public void onUserRemoved(User user) {

    }
}