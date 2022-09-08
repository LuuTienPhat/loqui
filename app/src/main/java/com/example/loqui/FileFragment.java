package com.example.loqui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.loqui.adapter.FileAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.MessageType;
import com.example.loqui.data.model.Attachment;
import com.example.loqui.data.model.Room;
import com.example.loqui.databinding.FragmentListBinding;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FileFragment() {
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
    public static FileFragment newInstance(String param1, String param2) {
        FileFragment fragment = new FileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentListBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private FileAdapter adapter;
    private List<Attachment> attachments;
    private Room room;

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
        getList();
        return binding.getRoot();
    }

    private void init() {
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        database = FirebaseFirestore.getInstance();

        attachments = new ArrayList<>();
        adapter = new FileAdapter(requireActivity(), attachments);
        room = (Room) getArguments().get(Constants.ROOM);
        binding.rvList.setAdapter(adapter);
    }

    private void getList() {
        loading(true);
        database.collection(Keys.KEY_COLLECTION_CHAT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .whereEqualTo(Keys.KEY_STATUS, MessageType.FILE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        List<String> message = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            message.add(documentSnapshot.getString(Keys.KEY_ID));
                        }

                        database.collection(Keys.KEY_COLLECTION_ATTS)
                                .whereIn(Keys.KEY_MESSAGE_ID, message)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                        List<String> atts = new ArrayList<>();
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots1.getDocuments()) {
                                            atts.add(documentSnapshot.getString(Keys.KEY_ATTACHMENT_ID));
                                        }

                                        database.collection(Keys.KEY_COLLECTION_ATTS)
                                                .whereIn(Keys.KEY_MESSAGE_ID, message)
                                                .get()
                                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                    if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
                                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots2.getDocuments()) {
                                                            Attachment attachment = new Attachment();
                                                            attachment.setId(documentSnapshot.getString(Keys.KEY_ID));
                                                            attachment.setName(documentSnapshot.getString(Keys.KEY_NAME));
                                                            attachment.setExtension(documentSnapshot.getString(Keys.KEY_EXTENSION));
                                                            attachment.setSize(documentSnapshot.getString(Keys.KEY_SIZE));
                                                            attachment.setPath(documentSnapshot.getString(Keys.KEY_PATH));
                                                            attachment.setCreatedDate(documentSnapshot.getString(Keys.KEY_CREATED_DATE));
                                                            attachment.setModifiedDate(documentSnapshot.getString(Keys.KEY_MODIFIED_DATE));
                                                            attachments.add(attachment);
                                                        }
                                                    } else {
                                                        loading(false);
                                                    }

                                                    adapter.notifyItemRangeInserted(0, attachments.size());
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

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}