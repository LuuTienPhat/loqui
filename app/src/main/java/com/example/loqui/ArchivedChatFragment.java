package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.loqui.adapter.ConversationAdapter;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.databinding.FragmentChatBinding;
import com.example.loqui.dialog.ExtraConversationDialog;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArchivedChatFragment extends Fragment implements ConversationAdapter.Listener, ExtraConversationDialog.Listener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ArchivedChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ArchivedChatFragment newInstance(String param1, String param2) {
        ArchivedChatFragment fragment = new ArchivedChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentChatBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    //    private RecentConversationAdapter recentConversationAdapter;
    private ConversationAdapter conversationAdapter;
    private FirebaseFirestore database;
    private List<ChatMessage> chatMessages;
    private List<String> rooms;
//    private List<Object> objects;
//    private UsersForwardAdapter usersForwardAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        binding.searchView.setVisibility(View.GONE);
        View view = binding.getRoot();

//        objects = new ArrayList<>();
//        usersForwardAdapter = new UsersForwardAdapter(objects, this);
//        binding.conversationRecyclerView.setAdapter(usersForwardAdapter);

        init();
//        getMyRooms();
//        loadingUserDetails();
//        setListeners();
//        listenConversations();
        return view;
    }

    private void init() {
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
//        conversations = new ArrayList<>();
//        recentConversationAdapter = new RecentConversationAdapter(conversations, this);
//        binding.conversationRecyclerView.setAdapter(recentConversationAdapter);
        database = FirebaseFirestore.getInstance();
        chatMessages = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(this, chatMessages);
        binding.conversationRecyclerView.setAdapter(conversationAdapter);
        rooms = new ArrayList<>();
//        getRequestedMessages();
    }

    private void setListeners() {
//        binding.btnSignOut.setOnClickListener(view -> signOut());
        //binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
//        binding.ivAvatar.setOnClickListener(v -> {
//            Intent intent = new Intent(requireActivity().getApplicationContext(), SettingsActivity.class);
//            startActivity(intent);
//        });
    }

    private void loadingUserDetails() {
//        binding.tvName.setText(preferenceManager.getString(Keys.KEY_NAME));
//        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(preferenceManager.getString(Keys.KEY_AVATAR)));
    }

    private void listenConversations() {
//        database.collection(Keys.KEY_COLLECTION_CONVERSATIONS)
//                .whereEqualTo(Keys.KEY_SENDER_ID, preferenceManager.getString(Keys.KEY_DOCUMENT_REF))
//                .addSnapshotListener(eventListener);
//
//        database.collection(Keys.KEY_COLLECTION_CONVERSATIONS)
//                .whereEqualTo(Keys.KEY_RECEIVER_ID, preferenceManager.getString(Keys.KEY_DOCUMENT_REF))
//                .addSnapshotListener(eventListener);

//        database.collection(Keys.KEY_COLLECTION_ROOM)
//                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
//                .addSnapshotListener(eventListener);

        if (!this.rooms.isEmpty()) {
            database.collection(Keys.KEY_COLLECTION_CHAT)
                    .whereIn(Keys.KEY_ROOM_ID, this.rooms)
                    .orderBy(Keys.KEY_CREATED_DATE, Query.Direction.DESCENDING)
                    .addSnapshotListener(eventListener);
        } else {
            loading(false);
        }

    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            loading(false);
            return;
        }
        if (value != null) {
            Set<String> filter = new HashSet<>();

            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                String roomId = queryDocumentSnapshot.getString(Keys.KEY_ROOM_ID);

                if (!filter.contains(roomId)) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(queryDocumentSnapshot.getString(Keys.KEY_ID));
                    chatMessage.setUserId(queryDocumentSnapshot.getString(Keys.KEY_USER_ID));
                    chatMessage.setRoomId(roomId);
                    chatMessage.setMessage(queryDocumentSnapshot.getString(Keys.KEY_MESSAGE));
                    chatMessage.setReplyId(queryDocumentSnapshot.getString(Keys.KEY_REPLY_ID));
                    chatMessage.setStatus(queryDocumentSnapshot.getString(Keys.KEY_STATUS));
                    chatMessage.setType(queryDocumentSnapshot.getString(Keys.KEY_TYPE));
                    chatMessage.setCreatedDate(queryDocumentSnapshot.getString(Keys.KEY_CREATED_DATE));
                    chatMessage.setModifiedDate(queryDocumentSnapshot.getString(Keys.KEY_MODIFIED_DATE));
                    chatMessages.add(chatMessage);

                    filter.add(roomId);
                }

            }

            conversationAdapter.notifyItemRangeRemoved(0, chatMessages.size());
            conversationAdapter.notifyItemRangeInserted(0, chatMessages.size());
        }
        loading(false);
    };

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void getMyRooms() {
        loading(true);

        Set<String> filter = new HashSet<>();

        Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            filter.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
                        }

                        this.rooms = new ArrayList<>(filter);

                    } else {
                        loading(false);
                    }
                }).addOnFailureListener(e -> {
                    loading(false);
                });


        Tasks.whenAllSuccess(t1)
                .addOnSuccessListener(objects -> {
                    if (!this.rooms.isEmpty()) {
                        Task<QuerySnapshot> t2 = database.collection(Keys.KEY_COLLECTION_ROOM)
                                .whereIn(Keys.KEY_ID, rooms)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                        //List<String> hiddenRoom = new ArrayList<>();
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                            String roomStatus = documentSnapshot.getString(Keys.KEY_STATUS);
                                            if (!roomStatus.equals(RoomStatus.ARCHIVED)) {
                                                //hiddenRoom.add(documentSnapshot.getString(Keys.KEY_ID));
                                                this.rooms.remove(documentSnapshot.getString(Keys.KEY_ID));
                                            }
                                        }

                                        listenConversations();
                                    } else {
                                        loading(false);
                                    }
                                });
                    } else {
                        loading(false);
                    }

                }).addOnFailureListener(e -> {
                    loading(false);
                });

    }

    private void getRequestedMessages() {
        loading(true);
        HashMap<String, List<String>> roomUsers = new HashMap<>();

        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        List<String> rooms = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            rooms.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
                        }

                        database.collection(Keys.KEY_COLLECTION_ROOM)
                                .whereIn(Keys.KEY_ID, rooms)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                        List<String> hiddenRoom = new ArrayList<>();
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                            String roomStatus = documentSnapshot.getString(Keys.KEY_STATUS);
                                            if (!roomStatus.equals(RoomStatus.DELETED) | !roomStatus.equals(RoomStatus.REQUESTED) | !roomStatus.equals(RoomStatus.ARCHIVED)) {
                                                hiddenRoom.add(documentSnapshot.getString(Keys.KEY_ID));
                                            }
                                        }

                                        database.collection(Keys.KEY_COLLECTION_CHAT)
                                                .whereIn(Keys.KEY_ROOM_ID, hiddenRoom)
                                                .orderBy(Keys.KEY_CREATED_DATE, Query.Direction.DESCENDING)
                                                .limit(1)
                                                .get()
                                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                    if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
                                                        for (DocumentSnapshot snapshot : queryDocumentSnapshots2.getDocuments()) {
                                                            ChatMessage chatMessage = new ChatMessage();
                                                            chatMessage.setId(snapshot.getString(Keys.KEY_ID));
                                                            chatMessage.setRoomId(snapshot.getString(Keys.KEY_ROOM_ID));
                                                            chatMessage.setUserId(snapshot.getString(Keys.KEY_USER_ID));
                                                            chatMessage.setMessage(snapshot.getString(Keys.KEY_MESSAGE));
                                                            chatMessage.setType(snapshot.getString(Keys.KEY_TYPE));
                                                            chatMessage.setStatus(snapshot.getString(Keys.KEY_STATUS));
                                                            chatMessage.setCreatedDate(snapshot.get(Keys.KEY_CREATED_DATE).toString());
                                                            chatMessage.setModifiedDate(snapshot.get(Keys.KEY_MODIFIED_DATE).toString());
                                                            chatMessages.add(chatMessage);
                                                        }

                                                        conversationAdapter.notifyItemRangeInserted(0, chatMessages.size());
                                                    }
                                                    loading(false);
                                                })
                                                .addOnFailureListener(e -> {
                                                    MyToast.showLongToast(requireActivity(), e.getMessage());
                                                });
                                    } else {
                                        loading(false);
                                        showErrorMessage();
                                    }

                                });
                    } else {
                        loading(false);
                        showErrorMessage();
                    }
                });

    }

    private void showErrorMessage() {
        binding.tvErrorMessage.setText(String.format("%s", "No chat available"));
        binding.tvErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getMyRooms();
//        loadingUserDetails();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void sendDialogResult(ConversationAdapter.Result result, ChatMessage chatMessage) {
        if (result.equals(ConversationAdapter.Result.SHORT)) {
            Intent intent = new Intent(requireActivity(), ChatActivity.class);
            intent.putExtra(Keys.KEY_ROOM_ID, chatMessage.getRoomId());
            requireActivity().startActivity(intent);
        } else {
            ExtraConversationDialog dialog = new ExtraConversationDialog(chatMessage);
            dialog.show(getChildFragmentManager(), "");
        }
    }

    @Override
    public void sendDialogResult(ExtraConversationDialog.Result result, ChatMessage chatMessage) {
        if (result.equals(ExtraConversationDialog.Result.ARCHIVE)) {
            database.collection(Keys.KEY_COLLECTION_ROOM)
                    .document(chatMessage.getRoomId())
                    .update(Keys.KEY_STATUS, RoomStatus.ARCHIVED);
        } else {
            database.collection(Keys.KEY_COLLECTION_ROOM)
                    .document(chatMessage.getRoomId())
                    .update(Keys.KEY_STATUS, RoomStatus.DELETED);
        }
    }

//    @Override
//    public void sendDialogResult(Object object) {
//
//    }

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
//
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