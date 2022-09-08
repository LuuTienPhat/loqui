package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;

import androidx.viewpager2.widget.ViewPager2;

import com.example.loqui.activities.settings.SettingsActivity;
import com.example.loqui.adapter.RecentConversationAdapter;
import com.example.loqui.adapter.ViewPagerAdapter;
import com.example.loqui.data.model.ChatMessage;
import com.example.loqui.data.model.LanguageManager;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivityMainBinding;
import com.example.loqui.listeners.ConversationListener;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

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
//        duplicateUser();
    }

//    private void duplicateUser() {
//        FirebaseHelper.findUser(database, preferenceManager.getString(Keys.KEY_USER_ID))
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    queryDocumentSnapshots.getDocuments().get(0).getReference().get()
//                            .addOnSuccessListener(documentSnapshot -> {
//                                HashMap<String, Object> user = new HashMap<>();
//                                user.put(Keys.KEY_ID, documentSnapshot.getString(Keys.KEY_ID));
//                                user.put(Keys.KEY_LASTNAME, "Lưu");
//                                user.put(Keys.KEY_FIRSTNAME, "Phát");
//                                user.put(Keys.KEY_USERNAME, "luuphat");
//                                user.put(Keys.KEY_EMAIL, "luutienphat10@gmail.com");
//                                user.put(Keys.KEY_PASSWORD, "1");
//                                user.put(Keys.KEY_AVATAR, documentSnapshot.getString(Keys.KEY_AVATAR));
//                                user.put(Keys.KEY_CREATED_DATE, System.currentTimeMillis());
//                                user.put(Keys.KEY_MODIFIED_DATE, System.currentTimeMillis());
//                                database.collection(Keys.KEY_COLLECTION_USERS).add(user);
//                            });
//
//                });
//    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        conversations = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversations, this);
//        binding.conversationRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
        setUpViewPager();
    }

    private void setListeners() {
//        binding.btnSignOut.setOnClickListener(view -> signOut());
//        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
        binding.ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        });
        binding.btnNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), UsersActivity.class);
            startActivity(intent);
        });
    }

//    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
//        if (error != null) {
//            return;
//        }
//        if (value != null) {
//            for (DocumentChange documentChange : value.getDocumentChanges()) {
//                if (documentChange.getType() == DocumentChange.Type.ADDED) {
//                    QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
//
//                    String senderId = queryDocumentSnapshot.getString(Keys.KEY_SENDER_ID);
//                    String receiverId = queryDocumentSnapshot.getString(Keys.KEY_RECEIVER_ID);
//
//                    ChatMessage chatMessage = new ChatMessage();
//                    chatMessage.setSenderId(senderId);
//                    chatMessage.setReceiverId(receiverId);
//
//                    if (preferenceManager.getString(Keys.KEY_USER_ID).equals(senderId)) {
//                        chatMessage.setConversationImage(queryDocumentSnapshot.getString(Keys.KEY_RECEIVER_IMAGE));
//                        chatMessage.setConversationName(queryDocumentSnapshot.getString(Keys.KEY_RECEIVER_NAME));
//                        chatMessage.setConversationId(queryDocumentSnapshot.getString(Keys.KEY_RECEIVER_ID));
//                    } else {
//                        chatMessage.setConversationImage(queryDocumentSnapshot.getString(Keys.KEY_SENDER_IMAGE));
//                        chatMessage.setConversationName(queryDocumentSnapshot.getString(Keys.KEY_SENDER_NAME));
//                        chatMessage.setConversationId(queryDocumentSnapshot.getString(Keys.KEY_SENDER_ID));
//                    }
//                    chatMessage.setMessage(queryDocumentSnapshot.getString(Keys.KEY_LAST_MESSAGE));
//                    chatMessage.setDateObject(queryDocumentSnapshot.getDate(Keys.KEY_TIMESTAMP));
//                    conversations.add(chatMessage);
//                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
//                    for (int i = 0; i < conversations.size(); i++) {
//                        QueryDocumentSnapshot queryDocumentSnapshot = documentChange.getDocument();
//
//                        String senderId = queryDocumentSnapshot.getString(Keys.KEY_SENDER_ID);
//                        String receiverId = queryDocumentSnapshot.getString(Keys.KEY_RECEIVER_ID);
//
//                        if (conversations.get(i).getSenderId().equals(senderId) && conversations.get(i).getReceiverId().equals(receiverId)) {
//                            conversations.get(i).setMessage(queryDocumentSnapshot.getString(Keys.KEY_LAST_MESSAGE));
//                            conversations.get(i).setDateObject(queryDocumentSnapshot.getDate(Keys.KEY_TIMESTAMP));
//                            break;
//                        }
//                    }
//                }
//            }
//
//            Collections.sort(conversations, (t1, t2) -> t1.getDateObject().compareTo(t2.getDateObject()));
//            conversationAdapter.notifyDataSetChanged();
//            binding.conversationRecyclerView.smoothScrollToPosition(0);
//            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
//            binding.progressBar.setVisibility(View.GONE);
//        }
//    };

    private void listenConversations() {
//        database.collection(Keys.KEY_COLLECTION_CONVERSATIONS)
//                .whereEqualTo(Keys.KEY_SENDER_ID, preferenceManager.getString(Keys.KEY_DOCUMENT_REF))
//                .addSnapshotListener(eventListener);
//
//        database.collection(Keys.KEY_COLLECTION_CONVERSATIONS)
//                .whereEqualTo(Keys.KEY_RECEIVER_ID, preferenceManager.getString(Keys.KEY_DOCUMENT_REF))
//                .addSnapshotListener(eventListener);
//
//        database.collection(Keys.KEY_COLLECTION_ROOM)
//                .whereEqualTo(Keys.KEY_USER_ID, preferenceManager.getString(Keys.KEY_USER_ID))
//                .addSnapshotListener(eventListener);
    }

    private void loadingUserDetails() {
//        binding.tvTextName.setText(preferenceManager.getString(Keys.KEY_NAME));
        binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(preferenceManager.getString(Keys.KEY_AVATAR)));
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(s -> {
                    //MyToast.showShortToast(this.getApplicationContext(), "Token " + s);
                    updateToken(s);
                })
                .addOnFailureListener(e -> {
                    Timber.tag(FirebaseMessaging.class.getName()).d(e);
                });
    }

    private void updateToken(String token) {
        preferenceManager.putString(Keys.KEY_FCM_TOKEN, token);
        database.collection(Keys.KEY_COLLECTION_USERS)
                .whereEqualTo(Keys.KEY_ID, preferenceManager.getString(Keys.KEY_USER_ID))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        documentReference.update(Keys.KEY_FCM_TOKEN, token)
                                .addOnSuccessListener(unused -> {
                                    MyToast.showShortToast(getApplicationContext(), "Token updated successfully");
                                })
                                .addOnFailureListener(e -> {
                                    //MyToast.showShortToast(getApplicationContext(), "Unable to update token");
                                });
                    }
                })
                .addOnFailureListener(e -> {

                });
//        FirebaseHelper.findUser(database, preferenceManager.getString(Keys.KEY_USER_ID))
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
//                        DocumentReference documentReference = queryDocumentSnapshots.getDocuments().get(0).getReference();
//                        documentReference.update(Keys.KEY_FCM_TOKEN, token)
//                                .addOnSuccessListener(unused -> {
//                                    //MyToast.showShortToast(getApplicationContext(), "Token updated successfully");
//                                })
//                                .addOnFailureListener(e -> {
//                                    MyToast.showShortToast(getApplicationContext(), "Unable to update token");
//                                    MyToast.showShortToast(this.getApplicationContext(), MainActivity.class.getName() + " " + e.getMessage());
//                                });
//                    }
//                });
        preferenceManager.putString(Keys.KEY_FCM_TOKEN, token);
    }

//    private void signOut() {
//        MyToast.showShortToast(getApplicationContext(), "Signing out...");
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        DocumentReference documentReference = database.collection(Keys.KEY_COLLECTION_USERS).document(
//                preferenceManager.getString(Keys.KEY_USER_ID)
//        );
//
//        HashMap<String, Object> updates = new HashMap<>();
//        updates.put(Keys.KEY_FCM_TOKEN, FieldValue.delete());
//        documentReference.update(updates).addOnSuccessListener(unused -> {
//            preferenceManager.clear();
//            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
//            finish();
//        }).addOnFailureListener(e -> {
//            MyToast.showShortToast(getApplicationContext(), "Unable to sign out");
//            Timber.d(e);
//        });
//    }

    @Override
    public void onConversationListener(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Keys.KEY_USER, user);
//        intent.putExtra(Keys.KEY_ROOM_ID, "N5qEMu27pCAUFCseSJK3");
        startActivity(intent);
    }

    private void setUpViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        binding.viewPager2.setAdapter(viewPagerAdapter);

        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                //int itemId = position == 0 ? R.id.chats_page : position == 1 ? R.id.calls_page : R.id.people_page;
                int itemId = position == 0 ? R.id.chats_page : R.id.people_page;
                binding.bottomNavigation.setSelectedItemId(itemId);
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
//            binding.bottomNavigation.setSelectedItemId(item.getItemId());
            switch (item.getItemId()) {
                case R.id.chats_page:
                    binding.viewPager2.setCurrentItem(0);
//                    Toast.makeText(this, "Chats page", Toast.LENGTH_LONG).show();
                    break;
//                case R.id.calls_page:
//                    binding.viewPager2.setCurrentItem(1);
////                    Toast.makeText(this, "Calls page", Toast.LENGTH_LONG).show();
//                    break;
                case R.id.people_page:
                    binding.viewPager2.setCurrentItem(1);
//                    Toast.makeText(this, "People page", Toast.LENGTH_LONG).show();
                    break;
//                case R.id.settings_page:
//                    binding.viewPager2.setCurrentItem(3);
//                    Toast.makeText(this, "Settings page", Toast.LENGTH_LONG).show();
//                    break;
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadingUserDetails();
//        binding.imageProfile.setImageBitmap(Utils.getBitmapFromEncodedString(preferenceManager.getString(Keys.KEY_AVATAR)));
    }

    @Override
    public void onBackPressed() {
        if (preferenceManager.getBoolean(Keys.KEY_IS_SIGNED_IN)) {
            return;
        }
        super.onBackPressed();
    }
}