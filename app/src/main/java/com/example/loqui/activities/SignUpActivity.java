package com.example.loqui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.MainActivity;
import com.example.loqui.databinding.ActivitySignUpBinding;
import com.example.loqui.utils.Constants;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding = null;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());

        setContentView(binding.getRoot());
        setListeners();
    }

    private String encodedImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.etFullName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.etEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.etPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user).addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, binding.etFullName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }).addOnFailureListener(exception -> {
                    loading(false);
                    MyToast.showShortToast(getApplicationContext(), exception.getMessage());
                });

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setListeners() {
        binding.tvSignIn.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        });

        binding.btnSignUp.setOnClickListener(view -> {
            if (isValidInformation()) {
                signUp();
            }
        });

        binding.ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
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
                    Log.e(getClass().getName(), ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    });

    private Boolean isValidInformation() {
        if (binding.etFullName.getText().toString().trim().isEmpty()) {
            Toasty.warning(getApplicationContext(), "Enter full name", Toasty.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etEmail.getText().toString().trim().isEmpty()) {
            Toasty.warning(getApplicationContext(), "Enter email", Toasty.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etPhone.getText().toString().trim().isEmpty()) {
            Toasty.warning(getApplicationContext(), "Enter phone", Toasty.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etPassword.getText().toString().trim().isEmpty()) {
            Toasty.warning(getApplicationContext(), "Enter password", Toasty.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etConfirm.getText().toString().trim().isEmpty()) {
            Toasty.warning(getApplicationContext(), "Confirm your password", Toasty.LENGTH_SHORT).show();
            return false;
        } else if (!binding.etConfirm.getText().toString().equals(binding.etPassword.getText().toString())) {
            Toasty.warning(getApplicationContext(), "Password & confirm password must be the same", Toasty.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}