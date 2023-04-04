package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.loqui.activities.settings.MeActivity;
import com.example.loqui.adapter.MemberAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.MessageType;
import com.example.loqui.constants.RecipientStatus;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.data.model.Room;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.FragmentListBinding;
import com.example.loqui.dialog.CustomDialog;
import com.example.loqui.dialog.MemberDialog;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberFragment extends Fragment implements MemberAdapter.Listener, MemberDialog.Listener, CustomDialog.Listener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MemberFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberFragment newInstance(String param1, String param2) {
        MemberFragment fragment = new MemberFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentListBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private MemberAdapter adapter;
    private List<User> users;
    private Set<String> members;
    private Room room;
    private User selectedUser = null;
    private String flagAdmin = null;
    private Boolean isAdmin = false;
    private HashMap<String, User> us = null;

    private Boolean isInit = true;

    private final String REQUEST_ADMIN = "request_admin";
    private final String REQUEST_REMOVE = "request_remove";

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
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater, container, false);
        init();

        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        for (DocumentChange documentChange : value.getDocumentChanges()) {
                            QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
                            if (queryDocumentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.ADMIN)) {
                                isAdmin = true;
                            } else {
                                isAdmin = false;
                            }
                        }
                    }

                });

        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .addSnapshotListener(recipientListener);
//        getUsers();
        return binding.getRoot();
    }

    private void init() {
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        database = FirebaseFirestore.getInstance();

        users = new ArrayList<>();
        adapter = new MemberAdapter(this, users);
        room = (Room) getArguments().get(Constants.ROOM);
        flagAdmin = getArguments().getString(Constants.ADMIN);
        binding.rvList.setAdapter(adapter);

        members = new HashSet<>();
        us = new HashMap<>();

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    // Khi nhấn vào nút "..." của mỗi thành viên
    @Override
    public void sendDialogResult(MemberAdapter.Result result, User user) {
        if (result.equals(MemberAdapter.Result.SHORT)) {
            MemberDialog dialog;
            if (flagAdmin != null) {
                if (!isAdmin) {
                    dialog = new MemberDialog(user, MemberDialog.GROUP_MEMBER);
                } else {
                    dialog = new MemberDialog(user, MemberDialog.GROUP_ADMIN);
                }

                //dialog = new MemberDialog(user, MemberDialog.GROUP_ADMIN);
            } else {
                if (!isAdmin) {
                    dialog = new MemberDialog(user, MemberDialog.GROUP_MEMBER);
                } else {
//                    if (this.users.size() == 1) {
//                        dialog = new MemberDialog(user, MemberDialog.GROUP_MEMBER);
//                    } else {
//                        dialog = new MemberDialog(user, "");
//                    }
                    dialog = new MemberDialog(user, "");
                }
            }
            dialog.show(getChildFragmentManager(), "");

        }
    }

    @Override
    public void sendDialogResult(MemberDialog.Result result, User user) {
        if (result.equals(MemberDialog.Result.REMOVE)) {
            CustomDialog customDialog = new CustomDialog(CustomDialog.Type.CONFIRM, "Are you sure?", "Are you sure to remove this person out of group?", REQUEST_REMOVE);
            customDialog.show(getChildFragmentManager(), null);
            this.selectedUser = user;
        } else if (result.equals(MemberDialog.Result.ADMIN)) {
            CustomDialog customDialog = new CustomDialog(CustomDialog.Type.CONFIRM, "Are you sure?", "This person will replace you as an admin", REQUEST_ADMIN);
            customDialog.show(getChildFragmentManager(), null);
            this.selectedUser = user;
        } else if (result.equals(MemberDialog.Result.INFO)) {
            if (user.getId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                Intent intent = new Intent(this.requireContext().getApplicationContext(), MeActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this.requireContext().getApplicationContext(), AccountInformationActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }

        }
    }

    @Override
    public void sendDialogResult(CustomDialog.Result result, String request) {
        if (request.equals(REQUEST_ADMIN)) {
            if (result.equals(CustomDialog.Result.OK)) {
                database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                        .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                        .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                HashMap<String, Object> changed = new HashMap<>();
                                changed.put(Keys.KEY_STATUS, RecipientStatus.NEW);
                                changed.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                                queryDocumentSnapshots.getDocuments().get(0).getReference().update(changed)
                                        .addOnSuccessListener(unused -> {

                                        });
                            }
                        });

                database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                        .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                        .whereEqualTo(Keys.KEY_USER_ID, selectedUser.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                HashMap<String, Object> changed = new HashMap<>();
                                changed.put(Keys.KEY_STATUS, RecipientStatus.ADMIN);
                                changed.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                                queryDocumentSnapshots.getDocuments().get(0).getReference().update(changed)
                                        .addOnSuccessListener(unused -> {
                                            sendMessage(selectedUser.getFullName() + " is now administrator");
                                            this.selectedUser = null;
//                                            Intent intent = new Intent(requireActivity(), MainActivity.class);
//                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                            startActivity(intent);
                                        });
                            }
                        });
            } else {
                this.selectedUser = null;
            }

        } else if (request.equals(REQUEST_REMOVE)) {
            if (result.equals(CustomDialog.Result.OK)) {
                if (selectedUser.getId().equals(preferenceManager.getString(Keys.KEY_USER_ID))) { // REMOVE bản thân là admin khỏi nhóm
                    database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                            .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                    String userId = null;
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                        if (!documentSnapshot.getString(Keys.KEY_USER_ID).equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
                                            if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
                                                userId = documentSnapshot.getString(Keys.KEY_USER_ID);
                                                break;
                                            }
                                        }
                                    }

                                    String finalUserId = userId;
                                    database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                                            .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                                            .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                                if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                                    HashMap<String, Object> changed = new HashMap<>();
                                                    changed.put(Keys.KEY_STATUS, RecipientStatus.REMOVED);
                                                    changed.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                                    queryDocumentSnapshots1.getDocuments().get(0).getReference().update(changed)
                                                            .addOnSuccessListener(unused -> {
//                                                            Intent intent = new Intent(requireActivity(), MainActivity.class);
//                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                            startActivity(intent);
                                                                database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                                                                        .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                                                                        .whereEqualTo(Keys.KEY_USER_ID, finalUserId)
                                                                        .get()
                                                                        .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                                            if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
                                                                                HashMap<String, Object> changed1 = new HashMap<>();
                                                                                changed1.put(Keys.KEY_STATUS, RecipientStatus.ADMIN);
                                                                                changed1.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                                                                queryDocumentSnapshots2.getDocuments().get(0).getReference().update(changed1)
                                                                                        .addOnSuccessListener(unused1 -> {
                                                                                            FirebaseHelper.findUser(database, finalUserId)
                                                                                                    .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                                                                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots3.getDocuments().get(0);
                                                                                                        User user = new User();
                                                                                                        user.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                                                                        user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                                                                                        user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                                                                                                        sendMessage(user.getFullName() + " is now administrator");
                                                                                                    });
                                                                                        });
                                                                            }
                                                                        });

                                                                sendMessage(selectedUser.getFullName() + " has left the group");
                                                                checkGroupAvailable();
                                                                this.selectedUser = null;
                                                            });
                                                }
                                            });

                                    Intent intent = new Intent(requireActivity(), MainActivity.class);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            });
                } else {
                    database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                            .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                            .whereEqualTo(Keys.KEY_USER_ID, this.selectedUser.getId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                    HashMap<String, Object> changed = new HashMap<>();
                                    changed.put(Keys.KEY_STATUS, RecipientStatus.REMOVED);
                                    changed.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
                                    queryDocumentSnapshots.getDocuments().get(0).getReference().update(changed)
                                            .addOnSuccessListener(unused -> {
                                                sendMessage(selectedUser.getFullName() + " has left the group");
//                                                Intent intent = new Intent(requireActivity(), MainActivity.class);
//                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                startActivity(intent);
                                                checkGroupAvailable();
                                                this.selectedUser = null;
                                            });
                                }
                            });
                }
            } else {
                this.selectedUser = null;
            }
        }
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
    public void onResume() {
        super.onResume();
        this.selectedUser = null;
    }

    private final EventListener<QuerySnapshot> recipientListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            //int count = this.users.size();

            for (DocumentChange documentChange : value.getDocumentChanges()) {
                QueryDocumentSnapshot documentSnapshot = documentChange.getDocument();

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String recipientStatus = documentSnapshot.getString(Keys.KEY_STATUS) == null ? RecipientStatus.NEW : documentSnapshot.getString(Keys.KEY_STATUS);

                    if (!recipientStatus.equals(RecipientStatus.REMOVED)) { // Nếu có ADD vào
                        if (flagAdmin != null) {
                            if (recipientStatus.equals(RecipientStatus.ADMIN)) {
                                members.add(documentSnapshot.getString(Keys.KEY_USER_ID));
                            } else {
                                members.remove(documentSnapshot.getString(Keys.KEY_USER_ID));
                            }
                        } else {
                            if (!recipientStatus.equals(RecipientStatus.ADMIN)) {
                                members.add(documentSnapshot.getString(Keys.KEY_USER_ID));
                            } else {
                                members.remove(documentSnapshot.getString(Keys.KEY_USER_ID));
                            }
                        }
                    }

//                    if (documentSnapshot.getString(Keys.KEY_USER_ID).equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
//                        if (recipientStatus.equals(RecipientStatus.ADMIN)) {
//                            isAdmin = true;
//                        }
//                    }
                }

                if (documentChange.getType() == DocumentChange.Type.MODIFIED) { // NẾU CÓ THAY ĐỔI
                    String recipientStatus = documentSnapshot.getString(Keys.KEY_STATUS) == null ? RecipientStatus.NEW : documentSnapshot.getString(Keys.KEY_STATUS);
                    //int index = findUser(documentSnapshot.getString(Keys.KEY_USER_ID));

                    if (!recipientStatus.equals(RecipientStatus.REMOVED)) {
                        if (flagAdmin != null) { // Phía ADMIN
                            if (recipientStatus.equals(RecipientStatus.ADMIN)) {
                                members.clear();
                                members.add(documentSnapshot.getString(Keys.KEY_USER_ID));
                            } else {
                                members.remove(documentSnapshot.getString(Keys.KEY_USER_ID));
                            }
                        } else { // PHÍA ALL
                            if (!recipientStatus.equals(RecipientStatus.ADMIN)) {
                                members.add(documentSnapshot.getString(Keys.KEY_USER_ID));
                            } else {
                                members.remove(documentSnapshot.getString(Keys.KEY_USER_ID));
                            }
                        }
                    }

                    if (recipientStatus.equals(RecipientStatus.REMOVED)) {
                        members.remove(documentSnapshot.getString(Keys.KEY_USER_ID));
                    }
                }

            }

            if (!members.isEmpty()) {
                database.collection(Keys.KEY_COLLECTION_USERS)
                        .whereIn(Keys.KEY_ID, Arrays.asList(members.toArray()))
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots1 -> {
                            if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                us.clear();

                                for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots1.getDocuments()) {
                                    User user = new User();
                                    user.setId(documentSnapshot1.getString(Keys.KEY_ID));
                                    user.setFirstName(documentSnapshot1.getString(Keys.KEY_FIRSTNAME));
                                    user.setLastName(documentSnapshot1.getString(Keys.KEY_LASTNAME));
                                    user.setImage(documentSnapshot1.getString(Keys.KEY_AVATAR));
                                    user.setPhone(documentSnapshot1.getString(Keys.KEY_PHONE));
                                    user.setEmail(documentSnapshot1.getString(Keys.KEY_EMAIL));
                                    user.setToken(documentSnapshot1.getString(Keys.KEY_FCM_TOKEN));
                                    user.setAvailability(documentSnapshot1.getString(Keys.KEY_AVAILABILITY));

                                    us.put(user.getId(), user);
                                    //users.add(user);
                                }

                                int count = this.users.size();
                                this.users.clear();
                                this.users.addAll(transform(us));

//                                adapter = new MemberAdapter(this, this.users);
//                                binding.rvList.setAdapter(adapter);
                                adapter.notifyItemRangeRemoved(0, count);
                                adapter.notifyItemRangeInserted(0, users.size());
                            }
                            loading(false);
                        });
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    };

    private int findUser(String userId) {
        for (int i = 0; i < this.members.size(); i++) {
            if (this.users.get(i).getId().equals(userId)) {
                return i;
            }
        }
        return -1;
    }

    private ArrayList<User> transform(HashMap<String, User> map) {
        Collection<User> values = map.values();
        return new ArrayList<User>(values);
    }

    private void checkGroupAvailable() {
        // Kiểm tra xem nhóm còn ai tham gia không
        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = 0;
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (!documentSnapshot.getString(Keys.KEY_STATUS).equals(RecipientStatus.REMOVED)) {
                                count++;
                            }
                        }

                        if (count == 1 | count == 0) { // Nếu số lượng người trong nhóm là 0 hoặc 1
                            database.collection(Keys.KEY_COLLECTION_ROOM)
                                    .whereEqualTo(Keys.KEY_ID, room.getId())
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                        queryDocumentSnapshots1.getDocuments().get(0).getReference().update(Keys.KEY_STATUS, RoomStatus.DELETED)
                                                .addOnSuccessListener(unused -> {
                                                    Intent intent = new Intent(this.requireContext(), MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                });
                                    });
                        }
//                        else {
//                            Intent intent = new Intent(this.requireActivity(), MainActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(intent);
//                        }
                    }
                });
    }

    private void getUsers() {
        loading(true);
        int count = users.size();
        this.users.clear();

        database.collection(Keys.KEY_COLLECTION_RECIPIENT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        List<String> ids = new ArrayList<>();

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String recipientStatus = documentSnapshot.getString(Keys.KEY_STATUS) == null ? RecipientStatus.NEW : documentSnapshot.getString(Keys.KEY_STATUS);
                            if (!recipientStatus.equals(RecipientStatus.REMOVED)) {
                                if (flagAdmin != null) {
                                    if (recipientStatus.equals(RecipientStatus.ADMIN)) {
                                        ids.add(documentSnapshot.getString(Keys.KEY_USER_ID));
                                    }
                                } else {
                                    if (!recipientStatus.equals(RecipientStatus.ADMIN)) {
                                        ids.add(documentSnapshot.getString(Keys.KEY_USER_ID));
                                    }
                                }

//                                if (documentSnapshot.getString(Keys.KEY_USER_ID).equals(preferenceManager.getString(Keys.KEY_USER_ID))) {
//                                    if (recipientStatus.equals(RecipientStatus.ADMIN)) {
//                                        isAdmin = true;
//                                    }
//                                }
                            }
                        }

                        if (!ids.isEmpty()) {
                            database.collection(Keys.KEY_COLLECTION_USERS)
                                    .whereIn(Keys.KEY_ID, ids)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                        if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                            for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots1.getDocuments()) {
                                                User user = new User();
                                                user.setId(documentSnapshot1.getString(Keys.KEY_ID));
                                                user.setFirstName(documentSnapshot1.getString(Keys.KEY_FIRSTNAME));
                                                user.setLastName(documentSnapshot1.getString(Keys.KEY_LASTNAME));
                                                user.setImage(documentSnapshot1.getString(Keys.KEY_AVATAR));
                                                user.setPhone(documentSnapshot1.getString(Keys.KEY_PHONE));
                                                user.setEmail(documentSnapshot1.getString(Keys.KEY_EMAIL));
                                                user.setToken(documentSnapshot1.getString(Keys.KEY_FCM_TOKEN));
                                                user.setAvailability(documentSnapshot1.getString(Keys.KEY_AVAILABILITY));
                                                users.add(user);
                                            }
                                            adapter.notifyItemRangeRemoved(0, count);
                                            adapter.notifyItemRangeInserted(0, users.size());
                                        }
                                        loading(false);
                                    });
                        } else {
                            loading(false);
                        }
                    } else {
                        loading(false);
                    }
                });
    }
}