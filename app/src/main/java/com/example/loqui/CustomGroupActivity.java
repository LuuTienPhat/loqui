package com.example.loqui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.constants.Constants;
import com.example.loqui.constants.MessageType;
import com.example.loqui.data.model.Room;
import com.example.loqui.databinding.ActivityCustomGroupBinding;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.SoftKeyboard;
import com.example.loqui.utils.Utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import timber.log.Timber;

public class CustomGroupActivity extends AppCompatActivity {

    private ActivityCustomGroupBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String encodedImage;
    private String oldEncodedImage;
    private String oldName;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        init();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        room = (Room) getIntent().getSerializableExtra(Constants.ROOM);
        oldEncodedImage = room.getAvatar();
        oldName = room.getName();

        database.collection(Keys.KEY_COLLECTION_ROOM)
                .document(room.getId()).addSnapshotListener(listenRoom);

        binding.ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.btnUpdate.setOnClickListener(v -> {
            SoftKeyboard.hideSoftKeyboard(binding.etName, this.getApplicationContext());

            if (binding.etName.getText() != null && !binding.etName.getText().toString().isEmpty()) {
                loading(true);
                HashMap<String, Object> changedRoom = new HashMap<>();
                changedRoom.put(Keys.KEY_NAME, binding.etName.getText().toString());
                changedRoom.put(Keys.KEY_AVATAR, encodedImage);
                changedRoom.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

                database.collection(Keys.KEY_COLLECTION_ROOM)
                        .document(this.room.getId())
                        .update(changedRoom)
                        .addOnSuccessListener(unused -> {
                            loading(false);

                            String messageContent;
                            if (!oldEncodedImage.equals(encodedImage)) {
                                messageContent = preferenceManager.getString(Keys.KEY_LASTNAME) + " " + preferenceManager.getString(Keys.KEY_FIRSTNAME) + " changed the group avatar";
                                sendMessage(messageContent);
                            }
                            if (!oldName.equals(binding.etName.getText().toString())) {
                                messageContent = preferenceManager.getString(Keys.KEY_LASTNAME) + " " + preferenceManager.getString(Keys.KEY_FIRSTNAME) + " changed group name to " + binding.etName.getText().toString();
                                sendMessage(messageContent);
                            }

                            MyToast.showShortToast(this.getApplicationContext(), "Updated Successfully");
                        })
                        .addOnFailureListener(e -> {
                            loading(false);
                            MyToast.showLongToast(this.getApplicationContext(), e.getMessage());
                            Timber.d(e);
                        });
            } else {
                MyToast.showShortToast(this.getApplicationContext(), "The room name couldn't be blank");
            }
        });

    }

    private final EventListener<DocumentSnapshot> listenRoom = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            this.room.setId(value.getString(Keys.KEY_ID));
            this.room.setName(value.getString(Keys.KEY_NAME));
            this.room.setAvatar(value.getString(Keys.KEY_AVATAR));
            this.room.setStatus(value.getString(Keys.KEY_STATUS));
            this.room.setAdminId(value.getString(Keys.KEY_ADMIN_ID));
            this.room.setCreatedDate(value.getString(Keys.KEY_CREATED_DATE));
            this.room.setModifiedDate(value.getString(Keys.KEY_MODIFIED_DATE));


            binding.etName.setText(this.room.getName());
            binding.ivAvatar.setImageBitmap(Utils.getBitmapFromEncodedString(value.getString(Keys.KEY_AVATAR)));
            encodedImage = room.getAvatar();
        }
    };

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

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                Uri imageUri = result.getData().getData();
                try {
                    InputStream is = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    binding.ivAvatar.setImageBitmap(bitmap);
                    encodedImage = encodedImage(bitmap);
                } catch (FileNotFoundException ex) {
                    Timber.tag(getClass().getName()).d(ex);
                    ex.printStackTrace();
                }
            }
        }
    });

    private String encodedImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnUpdate.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnUpdate.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}