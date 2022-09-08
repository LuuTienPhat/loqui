package com.example.loqui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.loqui.adapter.AttachmentAdapter;
import com.example.loqui.adapter.ImageAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.constants.MessageType;
import com.example.loqui.data.model.Attachment;
import com.example.loqui.data.model.Room;
import com.example.loqui.databinding.FragmentListBinding;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MediaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MediaFragment() {
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
    public static MediaFragment newInstance(String param1, String param2) {
        MediaFragment fragment = new MediaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentListBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private AttachmentAdapter attachmentAdapter;
    private ImageAdapter imageAdapter;
    private List<Attachment> attachments;
    private Room room;
    private String type;

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
        return binding.getRoot();
    }

    private void init() {
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        database = FirebaseFirestore.getInstance();

        room = (Room) getArguments().get(Constants.ROOM);
        type = getArguments().getString(Constants.ATTACHMENT);

        attachments = new ArrayList<>();
        if (type.equals(Constants.MEDIA)) {
            imageAdapter = new ImageAdapter(attachments, getContext());
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
            layoutManager.setFlexDirection(FlexDirection.ROW);
            layoutManager.setJustifyContent(JustifyContent.FLEX_START);
            layoutManager.setFlexWrap(FlexWrap.NOWRAP);
            binding.rvList.setLayoutManager(layoutManager);
            binding.rvList.setAdapter(imageAdapter);
            getMedia();
        } else {
            attachmentAdapter = new AttachmentAdapter(requireActivity(), attachments);
            binding.rvList.setAdapter(attachmentAdapter);
            getFile();
        }
    }

    private void getMedia() {
        loading(true);
        database.collection(Keys.KEY_COLLECTION_CHAT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .whereEqualTo(Keys.KEY_TYPE, MessageType.MEDIA)
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

                                        database.collection(Keys.KEY_COLLECTION_ATTACHMENT)
                                                .whereIn(Keys.KEY_ID, atts)
                                                .get()
                                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
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

                                                    imageAdapter.notifyItemRangeInserted(0, attachments.size());
                                                    loading(false);
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

    private void getFile() {
        loading(true);
        database.collection(Keys.KEY_COLLECTION_CHAT)
                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
                .whereEqualTo(Keys.KEY_TYPE, MessageType.FILE)
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

                                        database.collection(Keys.KEY_COLLECTION_ATTACHMENT)
                                                .whereIn(Keys.KEY_ID, atts)
                                                .get()
                                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
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

                                                    attachmentAdapter.notifyItemRangeInserted(0, attachments.size());
                                                    loading(false);
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