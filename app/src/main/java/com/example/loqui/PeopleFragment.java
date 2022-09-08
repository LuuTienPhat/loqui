package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;

import com.example.loqui.adapter.UsersAdapter;
import com.example.loqui.constants.FriendStatus;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.FragmentPeopleBinding;
import com.example.loqui.listeners.UserListener;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PeopleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PeopleFragment extends Fragment implements UserListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PeopleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PeopleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PeopleFragment newInstance(String param1, String param2) {
        PeopleFragment fragment = new PeopleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentPeopleBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<User> users;
    private UsersAdapter usersAdapter;
    private String myId = "";

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
        binding = FragmentPeopleBinding.inflate(inflater, container, false);
        init();
        //getUsers(null);
        return binding.getRoot();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);
        binding.rvUser.setAdapter(usersAdapter);
        myId = preferenceManager.getString(Keys.KEY_USER_ID);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) {
                    getUsers(null);
                } else {
                    getUsers(query);
                }
                return false;
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

//        database.collection(Keys.KEY_COLLECTION_FRIEND)
//                .addSnapshotListener(eventListener);
    }

    private void getUsers(String text) {
        loading(true);
        //showErrorMessage();
        if (text == null) {
            int count = this.users.size();
            this.users.clear();
            //usersAdapter.notifyItemRangeRemoved(0, count);

            database.collection(Keys.KEY_COLLECTION_USERS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                                if (!myId.equals(documentSnapshot.getString(Keys.KEY_ID))) {
                                    User user = new User();
                                    user.setId(documentSnapshot.getString(Keys.KEY_ID));
                                    user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                                    user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                    user.setEmail(documentSnapshot.getString(Keys.KEY_EMAIL));
                                    user.setPhone(documentSnapshot.getString(Keys.KEY_PHONE));
                                    user.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
                                    user.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
                                    users.add(user);
                                }
                            }

                            if (users.isEmpty()) {
                                //showErrorMessage();
                            } else {
//                                List<String> ids = new ArrayList<>();
//                                for (User user : this.users) {
//                                    ids.add(user.getId());
//                                }

                                Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_FRIEND)
                                        .whereEqualTo(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                                        .whereEqualTo(Keys.KEY_STATUS, FriendStatus.BLOCKED)
                                        .get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                                    int i = findUser(documentSnapshot.getString(Keys.KEY_USER_ID));
                                                    this.users.remove(i);
                                                }
                                            }
                                        });

                                Tasks.whenAllComplete(t1).addOnSuccessListener(tasks -> {
                                    usersAdapter = new UsersAdapter(users, this);
                                    binding.rvUser.setAdapter(usersAdapter);

                                    //usersAdapter.notifyItemRangeInserted(0, this.users.size());
                                });

                            }
                        } else {
                            //showErrorMessage();
                        }
                        loading(false);
                    })
                    .addOnFailureListener(e -> {
                        loading(false);
                        //showErrorMessage();
                    });
        } else {
            int count = this.users.size();
            this.users.clear();
            usersAdapter.notifyItemRangeRemoved(0, count);

            database.collection(Keys.KEY_COLLECTION_USERS)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                                if (!myId.equals(documentSnapshot.getString(Keys.KEY_ID))) {
                                    User user = new User();
                                    user.setId(documentSnapshot.getString(Keys.KEY_ID));
                                    user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                                    user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                    user.setEmail(documentSnapshot.getString(Keys.KEY_EMAIL));
                                    user.setPhone(documentSnapshot.getString(Keys.KEY_PHONE));
                                    user.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
                                    user.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));

                                    if (user.getFullName().contains(text)) {
                                        users.add(user);
                                    }

                                }
                            }

                            if (users.isEmpty()) {
                                //showErrorMessage();
                            } else {
                                List<String> ids = new ArrayList<>();
                                for (User user : this.users) {
                                    ids.add(user.getId());
                                }

                                Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_FRIEND)
                                        .whereEqualTo(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                                        .whereEqualTo(Keys.KEY_STATUS, FriendStatus.BLOCKED)
                                        .get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                                    int i = findUser(documentSnapshot.getString(Keys.KEY_USER_ID));
                                                    this.users.remove(i);
                                                }
                                            }
                                        });

                                Tasks.whenAllComplete(t1).addOnSuccessListener(tasks -> {
                                    usersAdapter = new UsersAdapter(users, this);
                                    binding.rvUser.setAdapter(usersAdapter);
//                                    usersAdapter.notifyItemRangeRemoved(0, count);
                                    //usersAdapter.notifyItemRangeInserted(0, this.users.size());
                                });

                            }
                            loading(false);
                        } else {
                            loading(false);
                            //showErrorMessage();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loading(false);
                        //showErrorMessage();
                    });

//            database.collection(Keys.KEY_COLLECTION_USERS)
//                    .get()
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                                if (!myId.equals(documentSnapshot.getString(Keys.KEY_ID))) {
//                                    String firstname = documentSnapshot.getString(Keys.KEY_LASTNAME);
//                                    String lastname = documentSnapshot.getString(Keys.KEY_FIRSTNAME);
//                                    if (firstname.contains(text) || lastname.contains(text)) {
//                                        User user = new User();
//                                        user.setId(documentSnapshot.getString(Keys.KEY_ID));
//                                        user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
//                                        user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
//                                        user.setEmail(documentSnapshot.getString(Keys.KEY_EMAIL));
//                                        user.setPhone(documentSnapshot.getString(Keys.KEY_PHONE));
//                                        user.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
//                                        user.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
//                                        users.add(user);
//                                    }
//                                }
//                            }
//
//                            if (users.isEmpty()) {
//                                showErrorMessage();
//                            } else {
//                                usersAdapter.notifyItemRangeRemoved(0, count);
//                                usersAdapter.notifyItemRangeInserted(0, users.size());
//                            }
//                            loading(false);
//                        } else {
//                            loading(false);
//                            showErrorMessage();
//                        }
//                    })
//                    .addOnFailureListener(e -> {
//                        loading(false);
//                        showErrorMessage();
//                    });
        }
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            //getUsers(null);
            loading(true);
            int count = this.users.size();
            this.users.clear();
            //usersAdapter.notifyItemRangeRemoved(0, count);

            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    DocumentSnapshot documentSnapshot = documentChange.getDocument();
                    if (!myId.equals(documentSnapshot.getString(Keys.KEY_ID))) {
                        User user = new User();
                        user.setId(documentSnapshot.getString(Keys.KEY_ID));
                        user.setLastName(documentSnapshot.getString(Keys.KEY_LASTNAME));
                        user.setFirstName(documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                        user.setEmail(documentSnapshot.getString(Keys.KEY_EMAIL));
                        user.setPhone(documentSnapshot.getString(Keys.KEY_PHONE));
                        user.setToken(documentSnapshot.getString(Keys.KEY_FCM_TOKEN));
                        user.setImage(documentSnapshot.getString(Keys.KEY_AVATAR));
                        users.add(user);
                    }
                }
            }

            if (!users.isEmpty()) {
                Task<QuerySnapshot> t1 = database.collection(Keys.KEY_COLLECTION_FRIEND)
                        .whereEqualTo(Keys.KEY_FRIEND_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                        .whereEqualTo(Keys.KEY_STATUS, FriendStatus.BLOCKED)
                        .get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                            if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                    int i = findUser(documentSnapshot.getString(Keys.KEY_USER_ID));
                                    this.users.remove(i);
                                }
                            }
                        });
                if (!users.isEmpty()) {
                    Tasks.whenAllComplete(t1).addOnSuccessListener(tasks -> {
                        usersAdapter = new UsersAdapter(users, this);
                        binding.rvUser.setAdapter(usersAdapter);
//                usersAdapter.notifyItemRangeRemoved(0, count);
                        //usersAdapter.notifyItemRangeInserted(0, this.users.size());
                        loading(false);
                    });
                } else {
                    loading(false);
                }
            } else {
                loading(false);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        database.collection(Keys.KEY_COLLECTION_USERS)
                .addSnapshotListener(eventListener);
        //getUsers(null);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            int count = this.users.size();
            this.users.clear();
            usersAdapter.notifyItemRangeRemoved(0, count);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

//    private void showErrorMessage() {
//        if (this.users.isEmpty()) {
//            binding.tvErrorMessage.setText(String.format("%s", "No people available"));
//            binding.tvErrorMessage.setVisibility(View.VISIBLE);
//        } else {
//            binding.tvErrorMessage.setVisibility(View.GONE);
//        }
//    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(requireActivity(), AccountInformationActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    @Override
    public void onUserChecked(User user, boolean isChecked) {

    }

    @Override
    public void onUserRemoved(User user) {

    }

    private void getFriends() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Keys.KEY_COLLECTION_FRIEND)
                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
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
                                users.add(user);
                            }
                        }

                        if (users.size() > 0) {
//                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
//                            binding.usersRecyclerView.setAdapter(usersAdapter);
//                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            //showErrorMessage();
                        }
                    } else {
                        //showErrorMessage();
                    }
                });
    }

    private int findUser(String userId) {
        for (int i = 0; i < this.users.size(); i++) {
            if (this.users.get(i).getId().equals(userId)) {
                return i;
            }
        }
        return -1;
    }
}