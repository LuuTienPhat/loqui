package com.example.loqui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import com.example.loqui.adapter.UsersForwardAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.FriendStatus;
import com.example.loqui.constants.MessageStatus;
import com.example.loqui.constants.MessageType;
import com.example.loqui.constants.RecipientStatus;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.constants.RoomType;
import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.data.model.Room;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityForwardBinding;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class ForwardActivity extends BaseActivity implements UsersForwardAdapter.Listener {

    private ActivityForwardBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    //    private List<String> recipients;
    //private List<User> users;
    private ChatMessage chatMessage;
    private List<Object> objects;
    private UsersForwardAdapter usersForwardAdapter;
//    private List<Recipient> recipients;
//    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForwardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
        //getFriendsAndRooms(null);
        setListeners();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

//        recipients = new ArrayList<>();
        //users = new ArrayList<>();
        objects = new ArrayList<>();
        usersForwardAdapter = new UsersForwardAdapter(objects, this);
        binding.rvUser.setAdapter(usersForwardAdapter);

        chatMessage = (ChatMessage) this.getIntent().getSerializableExtra(Constants.CHAT_MESSAGE);
    }

    private void setListeners() {

    }

    private void getFriendsAndRooms(String text) {
        loading(true);
        int count = this.objects.size();
        this.objects.clear();
        usersForwardAdapter.notifyItemRangeRemoved(0, count);

        /// Lay danh sach ban be
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
                                                        this.objects.add(user);
                                                    }
                                                } else {
                                                    this.objects.add(user);
                                                }

                                            }
                                            usersForwardAdapter = new UsersForwardAdapter(this.objects, this);
                                            binding.rvUser.setAdapter(usersForwardAdapter);
                                            binding.rvUser.setItemAnimator(null);
//                                            usersForwardAdapter.notifyItemRangeInserted(0, this.objects.size());
//                                            usersAdapter.notifyItemRangeRemoved(0, count);
//                                            usersAdapter.notifyItemRangeInserted(0, this.users.size());
                                        }
                                        loading(false);
                                    });
                        } else {
                            loading(false);
                            //showErrorMessage();
                        }
                    } else {
                        loading(false);
                        //showErrorMessage();
                    }
                });

        getGroups(text); // lay nhom co minh
    }

    private void getGroups(String text) {
        //Lay danh sach nhom co minh
        loading(true);
//        int count = this.objects.size();
//        this.objects.clear();
//        usersForwardAdapter.notifyItemRangeRemoved(0, count);
        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        List<String> rooms = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
                                rooms.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
                            }
                        }
                        if (!rooms.isEmpty()) {
                            database.collection(Keys.KEY_COLLECTION_ROOM)
                                    .whereIn(Keys.KEY_ID, rooms)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                        if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                                if (documentSnapshot.getString(Keys.KEY_TYPE).equals(RoomType.GROUP)) {
                                                    if (documentSnapshot.getString(Keys.KEY_STATUS).equals(RoomStatus.NEW)) {
                                                        Room room = new Room();
                                                        room.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                        room.setStatus(documentSnapshot.getString(Keys.KEY_STATUS));
                                                        room.setType(documentSnapshot.getString(Keys.KEY_TYPE));
                                                        room.setAvatar(documentSnapshot.getString(Keys.KEY_AVATAR));
                                                        room.setName(documentSnapshot.getString(Keys.KEY_NAME));
                                                        //this.objects.add(room);

                                                        if (text != null) {
                                                            if (room.getName().contains(text)) {
                                                                this.objects.add(room);
                                                            }
                                                        } else {
                                                            this.objects.add(room);
                                                        }
                                                    }
                                                }
                                            }

                                            usersForwardAdapter = new UsersForwardAdapter(this.objects, this);
                                            binding.rvUser.setAdapter(usersForwardAdapter);
                                            binding.rvUser.setItemAnimator(null);
                                        } else {
                                            loading(false);
                                        }
                                    });
                        } else {
                            loading(false);
                        }
                    } else {
                        loading(false);
                    }

                });
    }

    private String findRoom(List<String> ids, HashMap<String, List<String>> data) {
        Collections.sort(ids);
        String result = "";
        for (String i : data.keySet()) {
            System.out.println("key: " + i + " value: " + data.get(i));
            List<String> values = data.get(i);
            Collections.sort(values);
            if (values.equals(ids)) {
                result = i;
                break;
            }
        }

        return result;
    }

    private void updateRoom(User user, Object object) {
        HashMap<String, List<String>> roomUsers = new HashMap<>();
        List<String> recipients = new ArrayList<>();
        recipients.add(preferenceManager.getString(Keys.KEY_USER_ID));
        recipients.add(user.getId());

        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereIn(Keys.KEY_USER_ID, recipients)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                        String roomId = documentSnapshot.getString(Keys.KEY_ROOM_ID);
                        String userId = documentSnapshot.getString(Keys.KEY_USER_ID);
                        List<String> u;
                        if (!roomUsers.containsKey(roomId)) {
                            u = new ArrayList<>();
                        } else {
                            u = roomUsers.get(roomId);
                        }
                        u.add(userId);
                        roomUsers.put(roomId, u);

                    });

                    String receivedRoomId = findRoom(recipients, roomUsers);
                    if (receivedRoomId.isEmpty()) {
                        HashMap<String, Object> newRoom = new HashMap<>();
//                        newRoom.put(Keys.KEY_ID, this.room.getId());
                        String roomId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_ROOM);
                        newRoom.put(Keys.KEY_ID, roomId);
                        newRoom.put(Keys.KEY_NAME, "");
                        newRoom.put(Keys.KEY_AVATAR, "");
                        newRoom.put(Keys.KEY_TYPE, RoomType.TWO);
                        newRoom.put(Keys.KEY_STATUS, RoomStatus.NEW);
                        newRoom.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
                        newRoom.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                        database.collection(Keys.KEY_COLLECTION_ROOM)
                                .document(roomId)
                                .set(newRoom)
                                .addOnSuccessListener(unused -> {
                                    for (String r : recipients) {
                                        HashMap<String, String> recipient = new HashMap<>();
                                        recipient.put(Keys.KEY_ROOM_ID, roomId);
                                        //recipient.put(Keys.KEY_USER_ID, r.getUser().getId());
                                        recipient.put(Keys.KEY_USER_ID, r);
                                        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                                                .add(recipient);
                                    }

                                    sendMessage(chatMessage, roomId, object);
                                });
                    } else {
                        sendMessage(chatMessage, receivedRoomId, object);
                    }
                })
                .addOnFailureListener(e -> {
                    MyToast.showLongToast(this.getApplicationContext(), e.getMessage());
                    Timber.d(e);
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_search, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.btnSearch).getActionView();
        searchView.setQueryHint("Search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getFriendsAndRooms(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            getFriendsAndRooms(null);
            return false;
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFriendsAndRooms(null);
//        init();
    }

    @Override
    public void sendDialogResult(Object object) {
        if (object instanceof Room) {
            Room room = (Room) object;
            sendMessage(chatMessage, room.getId(), object);

        } else if (object instanceof User) {
            User user = (User) object;
            updateRoom(user, object);
        }
    }

    private void updateUI(Object object) {
        TextView textView = usersForwardAdapter.getTextViews().get(objects.indexOf(object));
        textView.setText("Sent");
        textView.setEnabled(false);
        textView.setTextColor(ContextCompat.getColor(this, R.color.steel_blue));
    }

    private void sendMessage(ChatMessage chatMessage, String roomId, Object object) {
        HashMap<String, Object> message = new HashMap<>();
        String messageId = FirebaseHelper.generateId(database, Keys.KEY_COLLECTION_CHAT);
        message.put(Keys.KEY_ID, messageId);
        message.put(Keys.KEY_ROOM_ID, roomId);
        message.put(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID));
        message.put(Keys.KEY_REPLY_ID, "");
        message.put(Keys.KEY_MESSAGE, chatMessage.getMessage());
        message.put(Keys.KEY_STATUS, MessageStatus.FORWARDED);
        message.put(Keys.KEY_TYPE, chatMessage.getType());
        message.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
        message.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

        if (chatMessage.getType().equals(MessageType.MEDIA) || chatMessage.getType().equals(MessageType.FILE)) {

            database.collection(Keys.KEY_COLLECTION_ATTS)
                    .whereEqualTo(Keys.KEY_MESSAGE_ID, chatMessage.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        String attachmentId = null;
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                attachmentId = documentSnapshot.getString(Keys.KEY_ATTACHMENT_ID);
                            }
                        }

                        if (attachmentId != null) {
                            HashMap<String, Object> file = new HashMap<>();
                            file.put(Keys.KEY_MESSAGE_ID, messageId);
                            file.put(Keys.KEY_ATTACHMENT_ID, attachmentId);
                            database.collection(Keys.KEY_COLLECTION_ATTS).add(file);
                        }
                    });
        }

        database.collection(Keys.KEY_COLLECTION_CHAT)
                .document(messageId)
                .set(message)
                .addOnSuccessListener(unused -> {
                    updateUI(object);
                });
    }

    //    private void showErrorMessage() {
//        binding.tvErrorMessage.setText(String.format("%s", "No user available"));
//        binding.tvErrorMessage.setVisibility(View.VISIBLE);
//    }

    private void loading(Boolean isloading) {
        if (isloading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    //    private void get() throws ExecutionException, InterruptedException {
//        loading(true);
//
//        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
//        String myId = preferenceManager.getString(Keys.KEY_USER_ID);
//        List<User> users = new ArrayList<>();
//
//        Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_USERS)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                            if (!myId.equals(documentSnapshot.getString(Keys.KEY_ID))) {
//                                User user = new User();
//                                //user.setName(documentSnapshot.getString(Keys.KEY_NAME));
//                                user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
//                                user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
//                                user.setEmail(documentSnapshot.getString(Keys.KEY_EMAIL));
//                                user.setPhone(documentSnapshot.getString(Keys.KEY_PHONE));
//                                user.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
//                                user.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
//                                user.setId(documentSnapshot.getString(Keys.KEY_ID));
////                                users.add(user);
//                                this.objects.add(user);
//                            }
//                        }
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    loading(false);
//                });
//
//        HashMap<String, List<String>> roomUsers = new HashMap<>();
//
//        Task<QuerySnapshot> t2 = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
//                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                            String roomId = documentSnapshot.getString(Keys.KEY_ROOM_ID);
//                            roomUsers.put(roomId, new ArrayList<>());
//                        }
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    loading(false);
//                });
//
//        tasks.add(t1);
//        tasks.add(t2);
//
//        Tasks.whenAllComplete(tasks)
//                .addOnSuccessListener(os -> {
//                    database.collection(Keys.KEY_COLLECTION_RECIPIENT)
//                            .whereIn(Keys.KEY_ROOM_ID, Arrays.asList(roomUsers.keySet().toArray()))
//                            .get()
//                            .addOnSuccessListener(queryDocumentSnapshots -> {
//                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                                    String roomId = documentSnapshot.getString(Keys.KEY_ROOM_ID);
//                                    String userId = documentSnapshot.getString(Keys.KEY_USER_ID);
//                                    List<String> u;
//                                    if (!roomUsers.containsKey(roomId)) {
//                                        u = new ArrayList<>();
//                                    } else {
//                                        u = roomUsers.get(roomId);
//                                    }
//                                    u.add(userId);
//                                    roomUsers.put(roomId, u);
//                                }
//
//                                List<String> rooms = filter(roomUsers);
//                                if (!rooms.isEmpty()) {
//                                    database.collection(Keys.KEY_COLLECTION_ROOM)
//                                            .whereIn(Keys.KEY_ID, rooms)
//                                            .get()
//                                            .addOnSuccessListener(snapshots -> {
//                                                if (!snapshots.getDocuments().isEmpty()) {
//                                                    for (DocumentSnapshot documentSnapshot : snapshots.getDocuments()) {
//                                                        String roomStatus = documentSnapshot.getString(Keys.KEY_STATUS);
//
//                                                        if (roomStatus.equals(RoomStatus.NEW)) {
//                                                            Room room = new Room();
//                                                            room.setId(documentSnapshot.getString(Keys.KEY_ID));
//                                                            room.setStatus(roomStatus);
//                                                            room.setAvatar(documentSnapshot.getString(Keys.KEY_AVATAR));
//                                                            room.setName(documentSnapshot.getString(Keys.KEY_NAME));
//                                                            this.objects.add(room);
//                                                        }
//                                                    }
//                                                }
//
//                                                usersForwardAdapter.notifyItemRangeInserted(0, this.objects.size());
//                                                loading(false);
//                                            })
//                                            .addOnFailureListener(e -> {
//                                                loading(false);
//                                            });
//                                }
//
//                                loading(false);
//                            })
//                            .addOnFailureListener(e -> {
//
//                            });
//                });
//    }

    //    private List<String> filter(HashMap<String, List<String>> data) {
//        List<String> rooms = new ArrayList<>();
//
//        for (String i : data.keySet()) {
//            List<String> values = data.get(i);
//            if (values.size() > 2) {
//                rooms.add(i);
//            }
//        }
//
//        return rooms;
//    }
}