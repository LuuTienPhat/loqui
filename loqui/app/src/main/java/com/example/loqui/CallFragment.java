package com.example.loqui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.loqui.adapter.CallAdapter;
import com.example.loqui.constants.RoomStatus;
import com.example.loqui.data.model.Room;
import com.example.loqui.databinding.FragmentListBinding;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CallFragment extends Fragment implements CallAdapter.Listener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CallFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CallFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CallFragment newInstance(String param1, String param2) {
        CallFragment fragment = new CallFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    private FragmentListBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private CallAdapter callAdapter;
    private List<String> rooms;


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
        listenDB();
        return binding.getRoot();
    }

    private void listenDB() {
    }

    private void init() {
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        database = FirebaseFirestore.getInstance();
        rooms = new ArrayList<>();
//        callAdapter = new CallAdapter(rooms, this);
//        binding.rvList.setAdapter(callAdapter);
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
                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                }
            }
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
                                            if (roomStatus.equals(RoomStatus.DELETED) || roomStatus.equals(RoomStatus.REQUESTED) || roomStatus.equals(RoomStatus.ARCHIVED)) {
                                                //hiddenRoom.add(documentSnapshot.getString(Keys.KEY_ID));
                                                this.rooms.remove(documentSnapshot.getString(Keys.KEY_ID));
                                            }
                                        }

//                                        listenConversations();
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

    @Override
    public void sendDialogResult(Room room) {

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}