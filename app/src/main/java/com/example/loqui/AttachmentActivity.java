package com.example.loqui;

import android.os.Bundle;

import com.example.loqui.adapter.AttachmentPagerAdapter;
import com.example.loqui.constants.Constants;
import com.example.loqui.data.model.Attachment;
import com.example.loqui.data.model.Room;
import com.example.loqui.databinding.ActivityAttachmentBinding;
import com.example.loqui.utils.PreferenceManager;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AttachmentActivity extends BaseActivity {

    private ActivityAttachmentBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Room room;
    private List<String> chatMessages;
    private List<Attachment> attachments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init();

        super.onCreate(savedInstanceState);
        binding = ActivityAttachmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        AttachmentPagerAdapter AttachmentPagerAdapter = new AttachmentPagerAdapter(this, this.room);
        binding.pager.setAdapter(AttachmentPagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.pager, (tab, position) ->
        {
            switch (position) {
                case 0:
                    tab.setText("Media");
                    break;
                case 1:
                    tab.setText("Files");
                    break;

            }
        }).attach();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        room = (Room) this.getIntent().getSerializableExtra(Constants.ROOM);
    }

//    private void loadAttachments() {
//        database.collection(Keys.KEY_COLLECTION_CHAT)
//                .whereEqualTo(Keys.KEY_ROOM_ID, room.getId())
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                            if (documentSnapshot.getString(Keys.KEY_TYPE).equals(MessageType.MEDIA) | documentSnapshot.getString(Keys.KEY_TYPE).equals(MessageType.FILE)) {
//                                this.chatMessages.add(documentSnapshot.getString(Keys.KEY_ID));
//                            }
//
//                            database.collection(Keys.KEY_COLLECTION_CHAT)
//                                    .whereIn(Keys.KEY_MESSAGE_ID, chatMessages)
//                                    .get()
//                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
//                                        if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
//                                            List<String> atts = new ArrayList<>();
//
//                                            for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots1.getDocuments()) {
//                                                atts.add(documentSnapshot1.getString(Keys.KEY_ATTACHMENT_ID));
//                                            }
//
//                                            database.collection(Keys.KEY_COLLECTION_CHAT)
//                                                    .whereIn(Keys.KEY_ATTACHMENT_ID, atts)
//                                                    .get()
//                                                    .addOnSuccessListener(queryDocumentSnapshots2 -> {
//                                                        if (!queryDocumentSnapshots2.getDocuments().isEmpty()) {
//                                                            for (DocumentSnapshot documentSnapshot2 : queryDocumentSnapshots2.getDocuments()) {
//                                                                Attachment attachment = new Attachment();
//                                                                attachment.setId(documentSnapshot2.getString(Keys.KEY_ID));
//                                                                attachment.setName(documentSnapshot2.getString(Keys.KEY_NAME));
//                                                                attachment.setSize(String.valueOf(documentSnapshot2.getString(Keys.KEY_SIZE)));
//                                                                attachment.setPath(documentSnapshot2.getString(Keys.KEY_PATH));
//                                                                attachment.setExtension(documentSnapshot2.getString(Keys.KEY_EXTENSION));
//
//                                                                this.attachments.add(attachment);
//                                                            }
//
//                                                        }
//                                                    });
//                                        }
//
//
//                                    });
//
//                        }
//                    }
//                });
//    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}