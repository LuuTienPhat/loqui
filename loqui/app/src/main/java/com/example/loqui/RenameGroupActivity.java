package com.example.loqui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.loqui.adapter.UsersAvatarAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.MessageType;
import com.example.loqui.constants.RecipientStatus;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.constants.RoomType;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityRenameGroupBinding;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class RenameGroupActivity extends BaseActivity implements UsersAvatarAdapter.Listener {

    private ActivityRenameGroupBinding binding;
    private MenuItem btnNext;
    private List<User> participants = new ArrayList<>();
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private UsersAvatarAdapter usersAvatarAdapter;
    private User me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRenameGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
        setListeners();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        participants = (List<User>) this.getIntent().getSerializableExtra(Constants.PARTICIPANTS);
        usersAvatarAdapter = new UsersAvatarAdapter(participants, this);
        binding.rvParticipant.setAdapter(usersAvatarAdapter);

        String text = String.valueOf(participants.size()) + " participants";
        binding.tvParticipant.setText(text);

        if (participants == null || participants.size() == 0) {
            MyToast.showShortToast(this, "There are error while creating group");
            finish();
        }

        User currentUser = new User();
        currentUser.setId(preferenceManager.getString(Keys.KEY_USER_ID));
        participants.add(0, currentUser);

        this.me = new User();
        this.me.setId(preferenceManager.getString(Keys.KEY_USER_ID));
        this.me.setFirstName(preferenceManager.getString(Keys.KEY_FIRSTNAME));
        this.me.setLastName(preferenceManager.getString(Keys.KEY_LASTNAME));
        this.me.setImage(preferenceManager.getString(Keys.KEY_AVATAR));
        this.me.setToken(preferenceManager.getString(Keys.KEY_FCM_TOKEN));
    }

    private void setListeners() {
        binding.etGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!binding.etGroupName.getText().toString().isEmpty()) {
                    btnNext.setEnabled(true);
                    btnNext.setVisible(true);
                } else {
                    btnNext.setEnabled(false);
                    btnNext.setVisible(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu_create_group, menu);
        btnNext = menu.findItem(R.id.btnNext);
        btnNext.setTitle("Create");
        if (binding.etGroupName.getText().toString().isEmpty()) {
            btnNext.setEnabled(false);
            btnNext.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnNext:
                handleOnBtnNextClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleOnBtnNextClicked() {
        Bitmap avatar = Utils.getBitmapFromVectorDrawable(this.getApplicationContext(), R.drawable.ic_round_people_24);

        String roomId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_ROOM);
        HashMap<String, String> room = new HashMap<>();
        room.put(Keys.KEY_ID, roomId);
        room.put(Keys.KEY_NAME, binding.etGroupName.getText().toString());
        room.put(Keys.KEY_AVATAR, Utils.encodedImage(avatar));
        room.put(Keys.KEY_TYPE, RoomType.GROUP);
        room.put(Keys.KEY_STATUS, RoomStatus.NEW);
        room.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
        room.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
        database.collection(Keys.KEY_COLLECTION_ROOM)
                .document(roomId)
                .set(room)
                .addOnSuccessListener(unused -> {

                    List<Task<DocumentReference>> taskList = new ArrayList<>();

                    for (User participant : participants) {
                        HashMap<String, String> recipient = new HashMap<>();
                        recipient.put(Keys.KEY_ROOM_ID, roomId);
                        recipient.put(Keys.KEY_USER_ID, participant.getId());
                        if (participant.getId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                            recipient.put(Keys.KEY_STATUS, RecipientStatus.ADMIN);
                        } else {
                            recipient.put(Keys.KEY_STATUS, RecipientStatus.NEW);
                        }
                        recipient.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                        recipient.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                        Task<DocumentReference> t = database.collection(Keys.KEY_COLLECTION_RECIPIENT).add(recipient);
                        taskList.add(t);
                    }

                    Tasks.whenAllComplete(taskList)
                            .addOnSuccessListener(tasks -> {

                                HashMap<String, Object> message = new HashMap<>();
                                String messageContent = me.getFullName() + " created the group";
                                String messageId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_CHAT);
                                message.put(Keys.KEY_ID, messageId);
                                message.put(Keys.KEY_ROOM_ID, roomId);
                                message.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                                message.put(Keys.KEY_MESSAGE, messageContent);
                                message.put(Keys.KEY_STATUS, RoomStatus.NEW);
                                message.put(Keys.KEY_ADMIN_ID, preferenceManager.getString(Keys.KEY_USER_ID));
                                message.put(Keys.KEY_TYPE, MessageType.STATUS);
                                message.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                                message.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                                database.collection(Keys.KEY_COLLECTION_CHAT)
                                        .document(messageId)
                                        .set(message).addOnSuccessListener(unused1 -> {
                                            Intent intent = new Intent(this, ChatActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            intent.putExtra(Keys.KEY_ROOM_ID, roomId);
                                            startActivity(intent);
                                        });

                            })
                            .addOnFailureListener(e -> {
                                MyToast.showLongToast(this, e.getMessage());
                                Timber.e(e);
                            });

                });
    }

    @Override
    public void sendDialogResult(UsersAvatarAdapter.Result result, User user) {
        if (result == UsersAvatarAdapter.Result.CLICKED) {
            Intent intent = new Intent(this.getApplicationContext(), AccountInformationActivity.class);
            intent.putExtra(Constants.USER, user);
            intent.putExtra(Constants.OPEN_FROM_NOTIFICATION, true);
            startActivity(intent);


        } else if (result == UsersAvatarAdapter.Result.REMOVE) {

            if (participants.size() > 3) {
                int i = findUser(user.getId());
                usersAvatarAdapter.notifyItemRemoved(i);
                this.participants.remove(i);

                String text = String.valueOf(participants.size()) + " participants";
                binding.tvParticipant.setText(text);
            } else {
                MyToast.showShortToast(this.getApplicationContext(), "The Group must contain at least two participants");
            }


        }
    }

    private int findUser(String userId) {
        for (int i = 0; i < this.participants.size(); i++) {
            if (this.participants.get(i).getId().equals(userId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }


}