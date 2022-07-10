package com.example.loqui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.activities.SignInActivity;
import com.example.loqui.adapter.RecentConversationAdapter;
import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.data.model.LanguageManager;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityMainBinding;
import com.example.loqui.listeners.ConversationListener;
import com.example.loqui.utils.Constants;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversationListener {

    private ActivityMainBinding binding;
    private LanguageManager languageManager;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        languageManager = new LanguageManager(this);
        languageManager.updateResource(languageManager.getLanguage());
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


//        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
//        binding.viewPager2.setAdapter(viewPagerAdapter);

//        binding.bottomNavigation.setOnItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.chats_page:
//                    binding.viewPager2.setCurrentItem(0);
//                    Toast.makeText(this, "Chats page", Toast.LENGTH_LONG).show();
//                    break;
//                case R.id.calls_page:
//                    binding.viewPager2.setCurrentItem(1);
//                    Toast.makeText(this, "Calls page", Toast.LENGTH_LONG).show();
//                    break;
//                case R.id.people_page:
//                    binding.viewPager2.setCurrentItem(2);
//                    Toast.makeText(this, "People page", Toast.LENGTH_LONG).show();
//                    break;
//                case R.id.settings_page:
//                    binding.viewPager2.setCurrentItem(3);
//                    Toast.makeText(this, "Settings page", Toast.LENGTH_LONG).show();
//                    break;
//            }
//            return true;
//        });

//        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                switch (position) {
//                    case 0:
//                        break;
//                    case 1:
//                        break;
//                    case 2:
//                        break;
//                    case 3:
//                        break;
//                }
//            }
//        });

//        binding.filledButton.setOnClickListener(view -> {
//            if (languageManager.getLanguage().equals("en")) {
//                languageManager.updateResource("vi");
//            } else {
//                languageManager.updateResource("en");
//            }
//            recreate();
//        });
        init();
        loadingUserDetails();
        getToken();
        setListeners();
        listenConversations();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        conversations = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversations, this);
        binding.conversationRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.btnSignOut.setOnClickListener(view -> signOut());
        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                    String senderId = queryDocumentSnapshot.getString(Constants.KEY_SENDER_ID);
                    String receiverId = queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_ID);

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setSenderId(senderId);
                    chatMessage.setReceiverId(receiverId);

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.setConversationImage(queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_IMAGE));
                        chatMessage.setConversationName(queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_NAME));
                        chatMessage.setConversationId(queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_ID));
                    } else {
                        chatMessage.setConversationImage(queryDocumentSnapshot.getString(Constants.KEY_SENDER_IMAGE));
                        chatMessage.setConversationName(queryDocumentSnapshot.getString(Constants.KEY_SENDER_NAME));
                        chatMessage.setConversationId(queryDocumentSnapshot.getString(Constants.KEY_SENDER_ID));
                    }
                    chatMessage.setMessage(queryDocumentSnapshot.getString(Constants.KEY_LAST_MESSAGE));
                    chatMessage.setDateObject(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP));
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();

                        String senderId = queryDocumentSnapshot.getString(Constants.KEY_SENDER_ID);
                        String receiverId = queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_ID);

                        if (conversations.get(i).getSenderId().equals(senderId) && conversations.get(i).getReceiverId().equals(receiverId)) {
                            conversations.get(i).setMessage(queryDocumentSnapshot.getString(Constants.KEY_LAST_MESSAGE));
                            conversations.get(i).setDateObject(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP));
                            break;
                        }
                    }
                }
            }

            Collections.sort(conversations, (t1, t2) -> t1.getDateObject().compareTo(t2.getDateObject()));
            conversationAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private void loadingUserDetails() {
        binding.tvTextName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
            updateToken(s);
        });
    }

    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                /*.addOnSuccessListener(unused -> {
                    MyToast.showShortToast(getApplicationContext(), "Token updated successfully");
                })*/
                .addOnFailureListener(e -> {
                    MyToast.showShortToast(getApplicationContext(), "Unable to update token");
                });
    }

    private void signOut() {
        MyToast.showShortToast(getApplicationContext(), "Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }).addOnFailureListener(e -> MyToast.showShortToast(getApplicationContext(), "Unable to sign out"));
    }

    @Override
    public void onConversationListener(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}