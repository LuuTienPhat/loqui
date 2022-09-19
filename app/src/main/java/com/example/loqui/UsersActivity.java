package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import com.example.loqui.adapter.UsersAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.FriendStatus;
import com.example.loqui.constants.RecipientStatus;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.constants.RoomType;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityUsersBinding;
import com.example.loqui.listeners.UserListener;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<String> recipients;
    private List<User> users;
    private UsersAdapter usersAdapter;
//    private List<Recipient> recipients;
//    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
        setListeners();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        recipients = new ArrayList<>();
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);
        binding.rvUser.setAdapter(usersAdapter);
        binding.rvUser.setItemAnimator(null);

        database.collection(Keys.KEY_COLLECTION_FRIEND)
                .addSnapshotListener(eventListener);

//        room = new Room();
//        createRoom();

//        User user = new User();
        addToRecipients(preferenceManager.getString(Keys.KEY_USER_ID));
        //user.setId(preferenceManager.getString(Keys.KEY_USER_ID));
//        addToRecipients(room, user);
    }

    private void setListeners() {
//        binding.btnBack.setOnClickListener(view -> onBackPressed());
        binding.btnCreateGroupChat.setOnClickListener(view -> {
            Intent intent = new Intent(UsersActivity.this.getApplicationContext(), CreateGroupActivity.class);
            startActivity(intent);
        });

        binding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUser(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        binding.lySwipe.setOnRefreshListener(() -> {
            getUsers(null);
            binding.lySwipe.setRefreshing(false);
        });
    }

    private void searchUser(String name) {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Keys.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Keys.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getString(Keys.KEY_ID))) {
                                continue;
                            } else {
                                User user = new User();
                                //user.setName(queryDocumentSnapshot.getString(Keys.KEY_NAME));
                                user.setLastName(queryDocumentSnapshot.getString(Keys.KEY_LASTNAME));
                                user.setFirstName(queryDocumentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                user.setEmail(queryDocumentSnapshot.getString(Keys.KEY_EMAIL));
                                user.setToken(queryDocumentSnapshot.getString(Keys.KEY_FCM_TOKEN));
                                user.setImage(queryDocumentSnapshot.getString(Keys.KEY_AVATAR));
                                user.setId(queryDocumentSnapshot.getId());

                                if (user.getFullName().contains(name)) {
                                    users.add(user);
                                }
                            }
                        }

                        if (users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.rvUser.setAdapter(usersAdapter);
                            binding.rvUser.setVisibility(View.VISIBLE);
                        } else {
                            loading(false);
                            //showErrorMessage();
                        }
                    } else {
                        loading(false);
                        //showErrorMessage();
                    }
                });
    }

    private void getUsers(String text) {
        loading(true);
        int count = this.users.size();
        this.users.clear();

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

                        if (!frs.isEmpty()) {
                            database.collection(Keys.KEY_COLLECTION_USERS)
                                    .whereIn(Keys.KEY_ID, frs)
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

                                                if (text != null) {
                                                    if (user.getFullName().contains(text)) {
                                                        this.users.add(user);
                                                    }
                                                } else {
                                                    this.users.add(user);
                                                }

                                            }

                                            usersAdapter.notifyItemRangeRemoved(0, count);
                                            usersAdapter.notifyItemRangeInserted(0, this.users.size());
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
    }

    private void createRoom(User user) {
        HashMap<String, Object> newRoom = new HashMap<>();
        String roomId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_ROOM);
        newRoom.put(Keys.KEY_ID, roomId);
        newRoom.put(Keys.KEY_NAME, "");
        newRoom.put(Keys.KEY_AVATAR, "");
        newRoom.put(Keys.KEY_TYPE, RoomType.TWO);
        newRoom.put(Keys.KEY_STATUS, RoomStatus.NEW);
        newRoom.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
        newRoom.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

        String finalRoomId = roomId;
        database.collection(Keys.KEY_COLLECTION_ROOM)
                .document(roomId)
                .set(newRoom)
                .addOnSuccessListener(unused -> {
                    for (String r : recipients) {
                        HashMap<String, String> recipient = new HashMap<>();
                        recipient.put(Keys.KEY_ROOM_ID, finalRoomId);
                        recipient.put(Keys.KEY_USER_ID, r);
                        recipient.put(Keys.KEY_STATUS, RecipientStatus.NEW);
                        recipient.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                        recipient.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                        //recipient.put(Keys.KEY_USER_ID, r.getUser().getId());
                        database.collection(Keys.KEY_COLLECTION_RECIPIENT).add(recipient);
                    }

                    openChatActivity(user, finalRoomId);
                });
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
    public void onUserClicked(User user) {
        this.recipients.clear();
        addToRecipients(preferenceManager.getString(Keys.KEY_USER_ID));
        addToRecipients(user.getId());
        updateRoom(user);

//        finish();
    }

    private String findRoom(List<String> ids, HashMap<String, List<String>> data) {
        Collections.sort(ids);
        String result = "";
        for (String i : data.keySet()) {
            System.out.println("key: " + i + " value: " + data.get(i));
            List<String> values = data.get(i);
            if (values.size() > 1)
                Collections.sort(values);
            if (values.equals(ids)) {
                result = i;
                break;
            }
        }

        return result;
    }

    private void openChatActivity(User user, String roomId) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Keys.KEY_USER, user);
        intent.putExtra(Keys.KEY_ROOM_ID, roomId);
        intent.putExtra(Constants.RECIPIENTS, (Serializable) recipients);
        startActivity(intent);
    }

    private void updateRoom(User user) {
        List<String> myRooms = new ArrayList<>();
        List<String> friendRooms = new ArrayList<>();
        List<Task<QuerySnapshot>> taskList = new ArrayList<>();

        Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RoomStatus.DELETED)) {
                                myRooms.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
                            }

                        }
                    }
                });

        taskList.add(t1);

        Task<QuerySnapshot> t2 = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, user.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RoomStatus.DELETED)) {
                                friendRooms.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
                            }
                        }
                    }
                });
        taskList.add(t2);
        Tasks.whenAllComplete(taskList)
                .addOnSuccessListener(tasks -> {
                    List<String> rooms = filter(myRooms, friendRooms);

                    if (!rooms.isEmpty()) {
                        database.collection(Keys.KEY_COLLECTION_ROOM)
                                .whereIn(Keys.KEY_ID, rooms)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    String roomId = null;
                                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                        //boolean created = false;
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                            String roomType = documentSnapshot.getString(Keys.KEY_TYPE);
                                            if (roomType.equals(RoomType.TWO)) {
                                                roomId = documentSnapshot.getString(Keys.KEY_ID);
                                            }
                                        }
                                    }

                                    if (roomId == null) {
                                        createRoom(user);
                                    } else {
                                        openChatActivity(user, roomId);
                                    }
                                });
                    } else {
                        createRoom(user);
                    }
                });
    }


    private List<String> filter(List<String> myRooms, List<String> friendRooms) {
        Set<String> commonElements = new HashSet<>();

        for (String s1 : myRooms) {
            for (String s2 : friendRooms) {
                if (s1.equals(s2)) {
                    commonElements.add(s1);
                }
            }
        }
        return new ArrayList<String>(commonElements);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_search, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.btnSearch).getActionView();
        searchView.setQueryHint("Search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

//    private void updateRoom(User user) {
//        HashMap<String, List<String>> roomUsers = new HashMap<>();
////        List<String> myRooms = new ArrayList<>();
////        List<String> friendRooms = new ArrayList<>();
//
//        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
//                .whereIn(Keys.KEY_USER_ID, recipients)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
////                    List<Recipient> arrayList = new ArrayList<>();
//                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
////                            myRooms.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
//                            String roomId = documentSnapshot.getString(Keys.KEY_ROOM_ID);
//                            String userId = documentSnapshot.getString(Keys.KEY_USER_ID);
//                            //rooms.add(roomId);
//                            List<String> u;
//                            if (!roomUsers.containsKey(roomId)) {
//                                u = new ArrayList<>();
//                            } else {
//                                u = roomUsers.get(roomId);
//                            }
//                            u.add(userId);
//                            roomUsers.put(roomId, u);
//                        }
//
//
//                        String receivedRoomId = findRoom(recipients, roomUsers);
//
//                        database.collection(Keys.KEY_COLLECTION_ROOM)
//                                .whereIn(Keys.KEY_ID, Arrays.asList(myRooms.toArray()))
//                                .get()
//                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
//                                    if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
//                                        String roomId = null;
//
//                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
//                                            String roomType = documentSnapshot.getString(Keys.KEY_TYPE);
//                                            if (roomType.equals(RoomType.TWO)) {
//                                                roomId = documentSnapshot.getString(Keys.KEY_ID);
//                                            }
//                                        }
//
//                                        if (roomId == null) {
//                                            HashMap<String, Object> newRoom = new HashMap<>();
//                                            roomId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_ROOM);
//                                            newRoom.put(Keys.KEY_ID, roomId);
//                                            newRoom.put(Keys.KEY_NAME, "");
//                                            newRoom.put(Keys.KEY_AVATAR, "");
//                                            newRoom.put(Keys.KEY_TYPE, RoomType.TWO);
//                                            newRoom.put(Keys.KEY_STATUS, RoomStatus.NEW);
//                                            newRoom.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
//                                            newRoom.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//
//                                            String finalRoomId = roomId;
//                                            database.collection(Keys.KEY_COLLECTION_ROOM)
//                                                    .document(roomId)
//                                                    .set(newRoom)
//                                                    .addOnSuccessListener(unused -> {
//                                                        for (String r : recipients) {
//                                                            HashMap<String, String> recipient = new HashMap<>();
//                                                            recipient.put(Keys.KEY_ROOM_ID, finalRoomId);
//                                                            recipient.put(Keys.KEY_USER_ID, r);
//                                                            recipient.put(Keys.KEY_STATUS, RecipientStatus.NEW);
//                                                            recipient.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
//                                                            recipient.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//                                                            //recipient.put(Keys.KEY_USER_ID, r.getUser().getId());
//                                                            database.collection(Keys.KEY_COLLECTION_RECIPIENT).add(recipient);
//                                                        }
//
//                                                        openChatActivity(user, finalRoomId);
//                                                    });
//                                        } else {
//                                            openChatActivity(user, roomId);
//                                        }
//                                    }
//                                });
//                    }
//                    //String receivedRoomId = findRoom(recipients, roomUsers);
//
//
////                    if (arrayList.size() != 2) {
////
////
////                    } else {
////                        MyToast.showLongToast(this.getApplicationContext(), "Room for userId: " + recipients.get(0).getUser().getId() + " and userId: " + recipients.get(1).getUser().getId() + " exists");
////                    }
//                });
//
//    }

//    private Recipient addToRecipients(Room room, User user) {
//        Recipient recipient = new Recipient();
//        recipient.setRoom(room);
//        recipient.setUser(user);
//        recipients.add(recipient);
//        return recipient;
//    }

    private void addToRecipients(String userId) {
        recipients.add(userId);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            getUsers(null);
        }
    };

    @Override
    public void onUserChecked(User user, boolean isChecked) {

    }

    @Override
    public void onUserRemoved(User user) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showErrorMessage() {
        binding.tvErrorMessage.setText(String.format("%s", "No friend available"));
        binding.tvErrorMessage.setVisibility(View.VISIBLE);
    }

//    private void getUsers() {
//        loading(true);
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        database.collection(Keys.KEY_COLLECTION_USERS)
//                .get()
//                .addOnCompleteListener(task -> {
//                    loading(false);
//                    String currentUserId = preferenceManager.getString(Keys.KEY_USER_ID);
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        List<User> users = new ArrayList<>();
//                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
//                            if (currentUserId.equals(queryDocumentSnapshot.getString(Keys.KEY_ID))) {
//                                continue;
//                            } else {
//                                User user = new User();
//                                //user.setName(queryDocumentSnapshot.getString(Keys.KEY_NAME));
//                                user.setLastName(queryDocumentSnapshot.getString(Keys.KEY_LASTNAME));
//                                user.setFirstName(queryDocumentSnapshot.getString(Keys.KEY_FIRSTNAME));
//                                user.setEmail(queryDocumentSnapshot.getString(Keys.KEY_EMAIL));
//                                user.setPhone(queryDocumentSnapshot.getString(Keys.KEY_PHONE));
//                                user.setToken(queryDocumentSnapshot.getString(Keys.KEY_FCM_TOKEN));
//                                user.setImage(queryDocumentSnapshot.getString(Keys.KEY_AVATAR));
//                                user.setId(queryDocumentSnapshot.getString(Keys.KEY_ID));
//                                users.add(user);
//                            }
//                        }
//
//                        if (users.size() > 0) {
//                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
//                            binding.rvUser.setAdapter(usersAdapter);
//                            binding.rvUser.setVisibility(View.VISIBLE);
//                        } else {
//                            loading(false);
////                            showErrorMessage();
//                        }
//                    } else {
//                        loading(false);
////                        showErrorMessage();
//                    }
//                });
//    }

//    private void updateRoom(User user) {
//
////        List<String> ids = new ArrayList<>();
////        for (Recipient recipient : recipients) {
////            ids.add(recipient.getUser().getId());
////        }
//
//        HashMap<String, List<String>> roomUsers = new HashMap<>();
//
//        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
//                .whereIn(Keys.KEY_USER_ID, recipients)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
////                    List<Recipient> arrayList = new ArrayList<>();
//
//                    queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
//                        String roomId = documentSnapshot.getString(Keys.KEY_ROOM_ID);
//                        String userId = documentSnapshot.getString(Keys.KEY_USER_ID);
//                        List<String> u;
//                        if (!roomUsers.containsKey(roomId)) {
//                            u = new ArrayList<>();
//                        } else {
//                            u = roomUsers.get(roomId);
//                        }
//                        u.add(userId);
//                        roomUsers.put(roomId, u);
//
//                    });
//
//                    String receivedRoomId = findRoom(recipients, roomUsers);
//                    if (receivedRoomId.isEmpty()) {
//                        HashMap<String, Object> newRoom = new HashMap<>();
////                        newRoom.put(Keys.KEY_ID, this.room.getId());
//                        String roomId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_ROOM);
//                        newRoom.put(Keys.KEY_ID, roomId);
//                        newRoom.put(Keys.KEY_NAME, "");
//                        newRoom.put(Keys.KEY_AVATAR, "");
//                        newRoom.put(Keys.KEY_STATUS, RoomStatus.NEW);
//                        newRoom.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
//                        newRoom.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//
//                        database.collection(Keys.KEY_COLLECTION_ROOM)
//                                .document(roomId)
//                                .set(newRoom)
//                                .addOnSuccessListener(unused -> {
//                                    for (String r : recipients) {
//                                        HashMap<String, String> recipient = new HashMap<>();
//                                        recipient.put(Keys.KEY_ROOM_ID, roomId);
//                                        recipient.put(Keys.KEY_USER_ID, r);
//                                        recipient.put(Keys.KEY_STATUS, RecipientStatus.NEW);
//                                        recipient.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
//                                        recipient.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//                                        //recipient.put(Keys.KEY_USER_ID, r.getUser().getId());
//                                        database.collection(Keys.KEY_COLLECTION_RECIPIENT).add(recipient);
//                                    }
//
//                                    openChatActivity(user, roomId);
//                                });
//                    } else {
//                        openChatActivity(user, receivedRoomId);
//                    }
//
////                    if (arrayList.size() != 2) {
////
////
////                    } else {
////                        MyToast.showLongToast(this.getApplicationContext(), "Room for userId: " + recipients.get(0).getUser().getId() + " and userId: " + recipients.get(1).getUser().getId() + " exists");
////                    }
//                })
//                .addOnFailureListener(e -> {
//                    MyToast.showLongToast(this.getApplicationContext(), e.getMessage());
//                    Timber.d(e);
//                });
//    }
}