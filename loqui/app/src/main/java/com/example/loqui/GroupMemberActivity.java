package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.adapter.GroupMemberPagerAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.RecipientStatus;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.data.model.Room;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityGroupMemberBinding;
import com.example.loqui.dialog.CustomDialog;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupMemberActivity extends AppCompatActivity implements CustomDialog.Listener {

    private ActivityGroupMemberBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Room room;
    private List<User> users;
    private boolean isAdmin = false;
    private MenuItem btnDelete = null;

    private final String REQUEST_DISBAND = "request_disband";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init();

        super.onCreate(savedInstanceState);
        binding = ActivityGroupMemberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        GroupMemberPagerAdapter pagerAdapter = new GroupMemberPagerAdapter(this, this.room);
        binding.pager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.pager, (tab, position) ->
        {
            switch (position) {
                case 0:
                    tab.setText("ALL");
                    break;
                case 1:
                    tab.setText("ADMIN");
                    break;

            }
        }).attach();
    }


    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        room = (Room) this.getIntent().getSerializableExtra(Constants.ROOM);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_group_member, menu);
        btnDelete = menu.findItem(R.id.btnDelete);

        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btnAdd) {
            Intent intent = new Intent(this.getApplicationContext(), AddGroupMemberActivity.class);
            intent.putExtra(Constants.ROOM, (Serializable) room);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.btnDelete) {
            CustomDialog customDialog = new CustomDialog(CustomDialog.Type.CONFIRM, "Are you sure?", "Are you sure to disband this group", REQUEST_DISBAND);
            customDialog.show(getSupportFragmentManager(),  null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot documentSnapshot = documentChange.getDocument();
                String recipientStatus = documentSnapshot.getString(Keys.KEY_STATUS) == null ? RecipientStatus.NEW : documentSnapshot.getString(Keys.KEY_STATUS);
                if (recipientStatus.equals(RecipientStatus.ADMIN)) {
                    isAdmin = true;
                } else {
                    isAdmin = false;
                }

                if (isAdmin) {
                    btnDelete.setVisible(true);
                } else {
                    btnDelete.setVisible(false);
                }
            }
        }
    };

    @Override
    public void sendDialogResult(CustomDialog.Result result, String request) {
        if (result.equals(CustomDialog.Result.OK)) {
            if (request.equals(REQUEST_DISBAND)) {
                database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                        .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                List<Task<?>> taskList = new ArrayList<>();

                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                    HashMap<String, Object> recipient = new HashMap<>();
                                    recipient.put(Keys.KEY_STATUS, RecipientStatus.REMOVED);
                                    recipient.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                    Task<Void> t1 = documentSnapshot.getReference().update(recipient);
                                    taskList.add(t1);
                                }

                                Tasks.whenAllComplete(taskList)
                                        .addOnSuccessListener(tasks -> {
                                            database.collection(Keys.KEY_COLLECTION_ROOM)
                                                    .whereEqualTo(Keys.KEY_ID, room.getId())
                                                    .get()
                                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                                        if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                                            DocumentReference documentReference = queryDocumentSnapshots1.getDocuments().get(0).getReference();
                                                            HashMap<String, Object> room = new HashMap<>();
                                                            room.put(Keys.KEY_STATUS, RoomStatus.DELETED);
                                                            documentReference.update(room)
                                                                    .addOnSuccessListener(unused -> {
                                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(intent);
                                                                    });
                                                        }

                                                    });
                                        });
                            }
                            ;
                        });


            }
        }
    }
}