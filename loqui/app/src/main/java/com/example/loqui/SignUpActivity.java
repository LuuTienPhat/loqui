package com.example.loqui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.loqui.data.model.User;
import com.example.loqui.databinding.ActivitySignUpBinding;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.example.loqui.utils.Utils;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding = null;
    private String encodedImage;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private User facebookUser = new User();
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "SignUpActivity";
    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());

        setContentView(binding.getRoot());
//        if(AccessToken.getCurrentAccessToken() != null)  {
//            AccessToken accessToken = AccessToken.getCurrentAccessToken();
//            LoginManager.getInstance().logOut();
//        }
//        facebookUser = (User) this.getIntent().getSerializableExtra("user");
//
//        if (facebookUser != null) {
//            binding.etFullName.setText(facebookUser.getName());
//            binding.etPhone.setText(facebookUser.getPhone());
//            binding.etEmail.setText(facebookUser.getEmail());
//        }

        init();
        setListeners();
        handleFacebookToken();
    }

    private void init() {
        accessToken = AccessToken.getCurrentAccessToken();
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
    }

    private void handleFacebookToken() {
        Timber.tag(TAG).d("handleFacebookToken %s", accessToken.getToken());

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken, (jsonObject, graphResponse) -> {
                    try {
                        assert jsonObject != null;
                        String imageUri = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url");
                        Glide
                                .with(this.getApplicationContext())
                                .asBitmap()
                                .load(imageUri)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        binding.ivAvatar.setImageBitmap(resource);
                                        encodedImage = encodedImage(resource);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                });

                        facebookUser.setId(accessToken.getUserId());
                        String email = jsonObject.getString("email");
                        String lastName = jsonObject.getString("last_name");
                        String firstName = jsonObject.getString("first_name");
//                        String gender = jsonObject.getString("gender");
//                        String birthday = jsonObject.getString("birthday");

                        facebookUser.setEmail(email);
                        facebookUser.setFirstName(firstName);
                        facebookUser.setLastName(lastName);

                        binding.etEmail.setText(email);
                        binding.etLastName.setText(lastName);
                        binding.etFirstname.setText(firstName);


//                        if (email == null) {
//                            binding.etEmail.setVisibility(View.VISIBLE);
//                        } else {
//
//                            binding.etEmail.setVisibility(View.GONE);
//                        }

                    } catch (Exception ex) {
                        Timber.tag(TAG).d(ex);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,picture.type(large),email,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();

//        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
//        firebaseAuth.signInWithCredential(credential)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Timber.tag(TAG).d("Sign in with credential: successful");
//                        FirebaseUser user = firebaseAuth.getCurrentUser();
//                        Glide
//                                .with(this.getApplicationContext())
//                                .load(user.getPhotoUrl())
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .into(binding.ivAvatar);
//                        updateUI(user);
//
//                    } else {
//                        Timber.tag(TAG).d(task.getException(), "Sign in with credential: failed");
//                        MyToast.showShortToast(SignUpActivity.this.getApplicationContext(), "Authentication Failed");
//                    }
//                }).addOnFailureListener(e -> {
//                    Timber.tag(TAG).d(e);
//                    MyToast.showShortToast(SignUpActivity.this.getApplicationContext(), "Authentication Failed");
//                });
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
        HashMap<String, Object> user = new HashMap<>();
        user.put(Keys.KEY_ID, facebookUser.getId());
        user.put(Keys.KEY_LASTNAME, binding.etLastName.getText().toString());
        user.put(Keys.KEY_FIRSTNAME, binding.etFirstname.getText().toString());
        user.put(Keys.KEY_USERNAME, binding.etUsername.getText().toString());
        user.put(Keys.KEY_EMAIL, binding.etEmail.getText().toString());
        user.put(Keys.KEY_PHONE, binding.etPhone.getText().toString());
        user.put(Keys.KEY_PASSWORD, binding.etPassword.getText().toString());
        user.put(Keys.KEY_AVATAR, encodedImage);
        user.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
        user.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

        HashMap<String, Object> settings = new HashMap<>();
        settings.put(Keys.KEY_USER_ID, facebookUser.getId());
        settings.put(Keys.KEY_SETTING_MESSAGE_REQUEST, false);
        settings.put(Keys.KEY_SETTING_DO_NOT_DISTURB, false);
        settings.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
        settings.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());

        Task<DocumentReference> t1 = database.collection(Keys.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
//                    documentReference.update(Keys.KEY_ID, documentReference.getId());

//                    preferenceManager.putBoolean(Keys.KEY_IS_SIGNED_IN, true);
//                    preferenceManager.putString(Keys.KEY_USER_ID, facebookUser.getId());
//                    preferenceManager.putString(Keys.KEY_LASTNAME, binding.etLastName.getText().toString());
//                    preferenceManager.putString(Keys.KEY_FIRSTNAME, binding.etFirstname.getText().toString());
//                    preferenceManager.putString(Keys.KEY_AVATAR, encodedImage);
//

                }).addOnFailureListener(exception -> {
                    loading(false);
                    MyToast.showShortToast(getApplicationContext(), exception.getMessage());
                });

        Task<DocumentReference> t2 = database.collection(Keys.KEY_COLLECTION_SETTINGS)
                .add(settings);

        Tasks.whenAllComplete(t1, t2)
                .addOnSuccessListener(tasks -> {
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
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
        });

        binding.btnSignUp.setOnClickListener(view -> {
            if (isValidInformation()) {
                FirebaseHelper.findUsername(database, binding.etUsername.getText().toString())
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                Toasty.warning(getApplicationContext(), "Username is existed", Toasty.LENGTH_SHORT).show();
                            } else {
                                signUp();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Timber.d(e);
                        });
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
                    Timber.tag(getClass().getName()).d(ex);
                    ex.printStackTrace();
                }
            }
        }
    });

    private Boolean isValidInformation() {
        Boolean result = true;
        if (binding.ivAvatar.getDrawable() == null) {
            Toasty.warning(getApplicationContext(), "Please choose an image", Toasty.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etFirstname.getText().toString().trim().isEmpty()) {
            Toasty.warning(getApplicationContext(), "Enter first name", Toasty.LENGTH_SHORT).show();
            return false;
        }
        if (binding.etLastName.getText().toString().trim().isEmpty()) {
            Toasty.warning(getApplicationContext(), "Enter last name", Toasty.LENGTH_SHORT).show();
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
        if (binding.etUsername.getText().toString().trim().isEmpty()) {
            Toasty.warning(getApplicationContext(), "Enter username", Toasty.LENGTH_SHORT).show();
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