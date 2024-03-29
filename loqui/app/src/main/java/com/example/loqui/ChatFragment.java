package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;

import com.example.loqui.adapter.ConversationAdapter;
import com.example.loqui.constants.RecipientStatus;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatFragment extends Fragment implements ConversationAdapter.Listener, ExtraConversationDialog.Listener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatFragment() {
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
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentChatBinding binding;
    private PreferenceManager preferenceManager;
    //private List<ChatMessage> conversations;
    //    private RecentConversationAdapter recentConversationAdapter;
    private ConversationAdapter conversationAdapter;
    private FirebaseFirestore database;
    private List<ChatMessage> chatMessages;
    private List<String> rooms;
    //private Set<Room> rooms2 = new HashSet<>();
    private HashMap<String, ChatMessage> roomChatMessages = new HashMap<>();
    private ListenerRegistration listenerRegistrationRecipient = null;
//    private List<Object> objects;
//    private UsersForwardAdapter usersForwardAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

//        objects = new ArrayList<>();
//        usersForwardAdapter = new UsersForwardAdapter(objects, this);
//        binding.conversationRecyclerView.setAdapter(usersForwardAdapter);
        init();
//        getMyRooms();

        listenerRegistrationRecipient = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .addSnapshotListener(recipientListener);


//        binding.lySwipe.setOnRefreshListener(() -> {
//            getMyRooms();
//            binding.lySwipe.setRefreshing(false);
//        });


        binding.lySwipe.setOnRefreshListener(() -> {
            refresh();
            binding.lySwipe.setRefreshing(false);
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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
//        binding.conversationRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(requireActivity()));
        binding.conversationRecyclerView.setAdapter(conversationAdapter);
        rooms = new ArrayList<>();
//        getRequestedMessages();
    }

//    private void listenConversations() {
//        if (!this.rooms.isEmpty()) {
//            database.collection(Keys.KEY_COLLECTION_CHAT)
//                    .whereIn(Keys.KEY_ROOM_ID, this.rooms)
//                    .orderBy(Keys.KEY_CREATED_DATE, Query.Direction.DESCENDING)
//                    .addSnapshotListener(eventListener);
//        } else {
//            loading(false);
//        }
//
//    }

    private List<ListenerRegistration> listenerRegistrations = new ArrayList<>();

    private void listenConversations() {
        if (!rooms.isEmpty()) {
            database.collection(Keys.KEY_COLLECTION_CHAT)
                    .whereIn(Keys.KEY_ROOM_ID, this.rooms)
                    .orderBy(Keys.KEY_CREATED_DATE, Query.Direction.DESCENDING)
                    .addSnapshotListener(eventListener2);
        } else {
            loading(false);
        }

    }

    private void listenConversations2() {
        if (!rooms.isEmpty()) {

            List<List<String>> subList = new ArrayList<>();
            for (int i = 0; i < rooms.size(); i += 10) {
                subList.add(rooms.subList(i, Math.min(i + 10, rooms.size())));
            }

            for (List<String> strings : subList) {
                ListenerRegistration listenerRegistration = database.collection(Keys.KEY_COLLECTION_CHAT)
                        .whereIn(Keys.KEY_ROOM_ID, strings)
                        .orderBy(Keys.KEY_CREATED_DATE, Query.Direction.DESCENDING)
                        .addSnapshotListener(eventListener2);

                listenerRegistrations.add(listenerRegistration);
            }


        } else {
            loading(false);
        }

    }

    private final EventListener<QuerySnapshot> eventListener2 = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
//            this.chatMessages.clear();
//            conversationAdapter.notifyDataSetChanged();

            //roomChatMessages = new HashMap<>();

            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                String roomId = queryDocumentSnapshot.getString(Keys.KEY_ROOM_ID);

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


                if (!roomChatMessages.containsKey(roomId)) {
                    roomChatMessages.put(roomId, chatMessage);

                    chatMessages.add(chatMessage);
                    conversationAdapter.notifyItemInserted(chatMessages.size() - 1);
                } else {
                    int i = isGreaterThan(chatMessage);
                    if (i != -1) {
                        chatMessages.set(i, chatMessage);
                        conversationAdapter.notifyItemChanged(i);
                    }
                }

            }

            //chatMessages = new ArrayList<>(roomChatMessages.values());
//            conversationAdapter = new ConversationAdapter(this, chatMessages);
//            binding.conversationRecyclerView.setAdapter(conversationAdapter);
//            conversationAdapter.notifyDataSetChanged();
//            conversationAdapter.notifyItemRangeRemoved(0, count);
//            conversationAdapter.notifyItemRangeRemoved(0, count);
//            conversationAdapter.notifyItemRangeInserted(0, chatMessages.size());
        }
        loading(false);
    };

    private int findChatMessages(String roomId) {
        for (int i = 0; i < this.chatMessages.size(); i++) {
            if (this.chatMessages.get(i).getRoomId().equals(roomId)) {
                return i;
            }
        }
        return -1;
    }

    private int findChatMessages2(String roomId, String messageId) {
        for (int i = 0; i < this.chatMessages.size(); i++) {
            if (this.chatMessages.get(i).getRoomId().equals(roomId)) {
                if (this.chatMessages.get(i).getId().equals(messageId)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int isGreaterThan(ChatMessage chatMessage) {
        for (int i = 0; i < this.chatMessages.size(); i++) {
            if (this.chatMessages.get(i).getRoomId().equals(chatMessage.getRoomId())) {
                if (Long.parseLong(chatMessage.getCreatedDate()) > Long.parseLong(this.chatMessages.get(i).getCreatedDate())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
//        loading(true);
        if (error != null) {
//            loading(false);
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            chatMessages.clear();

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
            conversationAdapter = new ConversationAdapter(this, chatMessages);
            binding.conversationRecyclerView.setAdapter(conversationAdapter);
//            conversationAdapter.notifyDataSetChanged();
//            conversationAdapter.notifyItemRangeRemoved(0, count);
//            conversationAdapter.notifyItemRangeRemoved(0, count);
//            conversationAdapter.notifyItemRangeInserted(0, chatMessages.size());
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

    private final EventListener<QuerySnapshot> recipientListener = (value, error) -> {
//        loading(true);
        if (error != null) {
//            loading(false);
            return;
        }
        if (value != null) {
//            int count = chatMessages.size();
//            chatMessages.clear();
            //Set<String> filter = new HashSet<>();

            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                if (queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.DECLINE) || queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.ACCEPT)) {
                    continue;
                } else {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        if (!queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
                            rooms.add(queryDocumentSnapshot.getString(Keys.KEY_ROOM_ID));
                        }
                    }

                    if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                        if (queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
                            rooms.remove(queryDocumentSnapshot.getString(Keys.KEY_ROOM_ID));
                        }

                        if (!queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
                            rooms.add(queryDocumentSnapshot.getString(Keys.KEY_ROOM_ID));
                        }
                    }
                }
            }

            for (ListenerRegistration l : listenerRegistrations) {
                l.remove();
            }

            listenConversations2();
        }
    };

    private void getMyRooms() {
        loading(true);

        Set<String> filter = new HashSet<>();
        Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
                                filter.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
                            }

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

                        List<List<String>> subList = new ArrayList<>();
                        for (int i = 0; i < rooms.size(); i += 10) {
                            subList.add(rooms.subList(i, Math.min(i + 10, rooms.size())));
                        }

                        for (List<String> strings : subList) {
                            Task<QuerySnapshot> t2 = database.collection(Keys.KEY_COLLECTION_ROOM)
                                    .whereIn(Keys.KEY_ID, strings)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                        if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                            //List<String> hiddenRoom = new ArrayList<>();
                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                                String roomStatus = documentSnapshot.getString(Keys.KEY_STATUS);
                                                if (roomStatus.equals(RoomStatus.DELETED) || roomStatus.equals(RoomStatus.REQUESTED) || roomStatus.equals(RoomStatus.ARCHIVED)) {
                                                    //hiddenRoom.add(documentSnapshot.getString(Keys.KEY_ID));
                                                    this.rooms.remove(documentSnapshot.getString(Keys.KEY_ID));
                                                }
                                            }


//                                        database.collection(Keys.KEY_COLLECTION_CHAT)
//                                                .whereIn(Keys.KEY_ROOM_ID, this.rooms)
//                                                .orderBy(Keys.KEY_CREATED_DATE, Query.Direction.DESCENDING)
//                                                .get()
//                                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
//                                                    Set<String> filter1 = new HashSet<>();
//                                                    int count = chatMessages.size();
//                                                    chatMessages.clear();
//                                                    conversationAdapter.notifyItemRangeRemoved(0, count);
//
//                                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots2.getDocuments()) {
//
//                                                        String roomId = documentSnapshot.getString(Keys.KEY_ROOM_ID);
//
//                                                        if (!filter1.contains(roomId)) {
//                                                            ChatMessage chatMessage = new ChatMessage();
//                                                            chatMessage.setId(documentSnapshot.getString(Keys.KEY_ID));
//                                                            chatMessage.setUserId(documentSnapshot.getString(Keys.KEY_USER_ID));
//                                                            chatMessage.setRoomId(roomId);
//                                                            chatMessage.setMessage(documentSnapshot.getString(Keys.KEY_MESSAGE));
//                                                            chatMessage.setReplyId(documentSnapshot.getString(Keys.KEY_REPLY_ID));
//                                                            chatMessage.setStatus(documentSnapshot.getString(Keys.KEY_STATUS));
//                                                            chatMessage.setType(documentSnapshot.getString(Keys.KEY_TYPE));
//                                                            chatMessage.setCreatedDate(documentSnapshot.getString(Keys.KEY_CREATED_DATE));
//                                                            chatMessage.setModifiedDate(documentSnapshot.getString(Keys.KEY_MODIFIED_DATE));
//                                                            chatMessages.add(chatMessage);
//
//                                                            filter1.add(roomId);
//                                                        }
//
//                                                    }
//
//                                                    conversationAdapter = new ConversationAdapter(this, chatMessages);
//                                                    binding.conversationRecyclerView.setAdapter(conversationAdapter);
//                                                    loading(false);
//                                                });

                                        } else {
                                            loading(false);
                                        }
                                    });
                            Tasks.whenAllComplete(t2)
                                    .addOnSuccessListener(tasks -> {
                                        listenConversations();
                                    });
                        }

                    } else {
                        loading(false);
                    }

                }).addOnFailureListener(e -> {
                    loading(false);
                });
    }

    private void refresh() {
        listenerRegistrationRecipient.remove();
        //this.rooms2.clear();
        this.rooms.clear();
        this.roomChatMessages.clear();
        this.chatMessages.clear();

        for (ListenerRegistration l : listenerRegistrations) {
            l.remove();
        }

        listenerRegistrationRecipient = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .addSnapshotListener(recipientListener);

    }

//    private void getMyRooms() {
//        loading(true);
//
//        Set<String> filter = new HashSet<>();
//        Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_RECIPIENT)
//                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                            if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
//                                filter.add(documentSnapshot.getString(Keys.KEY_ROOM_ID));
//                            }
//
//                        }
//
//                        this.rooms = new ArrayList<>(filter);
//
//                    } else {
//                        loading(false);
//                    }
//                }).addOnFailureListener(e -> {
//                    loading(false);
//                });
//
//
//        Tasks.whenAllSuccess(t1)
//                .addOnSuccessListener(objects -> {
//                    if (!this.rooms.isEmpty()) {
//
//                        List<List<String>> subList = new ArrayList<>();
//                        for (int i = 0; i < rooms.size(); i += 10) {
//                            subList.add(rooms.subList(i, Math.min(i + 10, rooms.size())));
//                        }
//
//                        for (List<String> strings : subList) {
//                            Task<QuerySnapshot> t2 = database.collection(Keys.KEY_COLLECTION_ROOM)
//                                    .whereIn(Keys.KEY_ID, strings)
//                                    .get()
//                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
//                                        if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
//                                            //List<String> hiddenRoom = new ArrayList<>();
//                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
//                                                String roomStatus = documentSnapshot.getString(Keys.KEY_STATUS);
//                                                if (roomStatus.equals(RoomStatus.DELETED) | roomStatus.equals(RoomStatus.REQUESTED) | roomStatus.equals(RoomStatus.ARCHIVED)) {
//                                                    //hiddenRoom.add(documentSnapshot.getString(Keys.KEY_ID));
//                                                    this.rooms.remove(documentSnapshot.getString(Keys.KEY_ID));
//                                                }
//                                            }
//
//
////                                        database.collection(Keys.KEY_COLLECTION_CHAT)
////                                                .whereIn(Keys.KEY_ROOM_ID, this.rooms)
////                                                .orderBy(Keys.KEY_CREATED_DATE, Query.Direction.DESCENDING)
////                                                .get()
////                                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
////                                                    Set<String> filter1 = new HashSet<>();
////                                                    int count = chatMessages.size();
////                                                    chatMessages.clear();
////                                                    conversationAdapter.notifyItemRangeRemoved(0, count);
////
////                                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots2.getDocuments()) {
////
////                                                        String roomId = documentSnapshot.getString(Keys.KEY_ROOM_ID);
////
////                                                        if (!filter1.contains(roomId)) {
////                                                            ChatMessage chatMessage = new ChatMessage();
////                                                            chatMessage.setId(documentSnapshot.getString(Keys.KEY_ID));
////                                                            chatMessage.setUserId(documentSnapshot.getString(Keys.KEY_USER_ID));
////                                                            chatMessage.setRoomId(roomId);
////                                                            chatMessage.setMessage(documentSnapshot.getString(Keys.KEY_MESSAGE));
////                                                            chatMessage.setReplyId(documentSnapshot.getString(Keys.KEY_REPLY_ID));
////                                                            chatMessage.setStatus(documentSnapshot.getString(Keys.KEY_STATUS));
////                                                            chatMessage.setType(documentSnapshot.getString(Keys.KEY_TYPE));
////                                                            chatMessage.setCreatedDate(documentSnapshot.getString(Keys.KEY_CREATED_DATE));
////                                                            chatMessage.setModifiedDate(documentSnapshot.getString(Keys.KEY_MODIFIED_DATE));
////                                                            chatMessages.add(chatMessage);
////
////                                                            filter1.add(roomId);
////                                                        }
////
////                                                    }
////
////                                                    conversationAdapter = new ConversationAdapter(this, chatMessages);
////                                                    binding.conversationRecyclerView.setAdapter(conversationAdapter);
////                                                    loading(false);
////                                                });
//
//                                        } else {
//                                            loading(false);
//                                        }
//                                    });
//                            Tasks.whenAllComplete(t2)
//                                    .addOnSuccessListener(tasks -> {
//                                        listenConversations(strings);
//                                    });
//                        }
//
//                    } else {
//                        loading(false);
//                    }
//
//                }).addOnFailureListener(e -> {
//                    loading(false);
//                });
//    }

    private int findChatMessage(String chatMessageId) {
        int index = -1;
        for (int i = 0; i < chatMessages.size(); i++) {
            if (chatMessages.get(i).getId().equals(chatMessageId)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void onResume() {
//        chatMessages = new ArrayList<>();
//        getMyRooms();
        super.onResume();
        //getMyRooms();
//        loadingUserDetails();
    }

    @Override
    public void onPause() {
        super.onPause();
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
                                            if (!roomStatus.equals(RoomStatus.DELETED) || !roomStatus.equals(RoomStatus.REQUESTED) || !roomStatus.equals(RoomStatus.ARCHIVED)) {
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
                                    }

                                });
                    } else {
                        loading(false);
                    }
                });

    }

    private void search(String text) {
        if (text.isEmpty()) {
            getMyRooms();
        } else {
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
                                                if (roomStatus.equals(RoomStatus.DELETED) || roomStatus.equals(RoomStatus.REQUESTED) | roomStatus.equals(RoomStatus.ARCHIVED)) {
                                                    //hiddenRoom.add(documentSnapshot.getString(Keys.KEY_ID));
                                                    this.rooms.remove(documentSnapshot.getString(Keys.KEY_ID));
                                                }
                                            }

                                            //listenConversations();

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