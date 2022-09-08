package com.example.loqui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.loqui.databinding.ActivitySignInBinding;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.MyToast;
import com.example.loqui.utils.PreferenceManager;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import timber.log.Timber;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private FirebaseFirestore database;
    //    private FirebaseAuth firebaseAuth;
    private AccessToken accessToken;
    private static final String TAG = "FacebookAuthentication";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        askPermission();
        setListeners();
    }

    private void init() {
//        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore.setLoggingEnabled(true);
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Keys.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        callbackManager = CallbackManager.Factory.create();

    }

//    private void addDataToDB() {
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("first_name", "Phat");
//        data.put("las_name", "Luu");
//        database.collection("users").add(data).addOnSuccessListener(documentReference -> {
//            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
//        }).addOnFailureListener(e -> {
//            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }

    private void setListeners() {
        binding.btnFacebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Timber.tag(SignInActivity.class.getName()).d("onSuccess " + loginResult);

                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Timber.tag(SignInActivity.class.getName()).d("onCancel");
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Timber.tag(SignInActivity.class.getName()).d("onError");
            }
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), ForgotPasswordActivity.class);
            startActivity(intent);
        });

        binding.btnSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });

        binding.tvSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), FacebookLoginActivity.class);
            startActivity(intent);
        });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private Task<QuerySnapshot> findUser(String id) {
        return database.collection(Keys.KEY_COLLECTION_USERS)
                .whereEqualTo(Keys.KEY_ID, id)
                .get();
    }

    private void handleFacebookToken(AccessToken accessToken) {
        findUser(accessToken.getUserId())
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    AccessToken.expireCurrentAccessToken();

                    if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                        Intent intent = new Intent(this.getApplicationContext(), SignUpActivity.class);
                        startActivity(intent);
                    } else {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        preferenceManager.putBoolean(Keys.KEY_IS_SIGNED_IN, true);
//                        preferenceManager.putString(Keys.KEY_USER_ID, documentSnapshot.getId());
//                        preferenceManager.putString(Keys.KEY_NAME, documentSnapshot.getString(Keys.KEY_NAME));
//                        preferenceManager.putString(Keys.KEY_AVATAR, documentSnapshot.getString(Keys.KEY_AVATAR));
                        preferenceManager.putString(Keys.KEY_DOCUMENT_REF, documentSnapshot.getReference().getId());
                        preferenceManager.putString(Keys.KEY_USER_ID, documentSnapshot.getString(Keys.KEY_ID));
                        preferenceManager.putString(Keys.KEY_FIRSTNAME, documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                        preferenceManager.putString(Keys.KEY_LASTNAME, documentSnapshot.getString(Keys.KEY_LASTNAME));
                        preferenceManager.putString(Keys.KEY_EMAIL, documentSnapshot.getString(Keys.KEY_EMAIL));
                        preferenceManager.putString(Keys.KEY_PHONE, documentSnapshot.getString(Keys.KEY_PHONE));
                        preferenceManager.putString(Keys.KEY_AVATAR, documentSnapshot.getString(Keys.KEY_AVATAR));
                        preferenceManager.putString(Keys.KEY_FCM_TOKEN, documentSnapshot.getString(Keys.KEY_FCM_TOKEN));

                        database.collection(Keys.KEY_COLLECTION_SETTINGS)
                                .whereEqualTo(Keys.KEY_USER_ID, documentSnapshot.getString(Keys.KEY_ID))
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {

                                    if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                        DocumentSnapshot documentSnapshot2 = queryDocumentSnapshots1.getDocuments().get(0);
                                        preferenceManager.putBoolean(Keys.KEY_SETTING_DO_NOT_DISTURB, documentSnapshot2.getBoolean(Keys.KEY_SETTING_MESSAGE_REQUEST));
                                        preferenceManager.putBoolean(Keys.KEY_SETTING_MESSAGE_REQUEST, documentSnapshot2.getBoolean(Keys.KEY_SETTING_MESSAGE_REQUEST));

                                        preferenceManager.putBoolean(Keys.KEY_IS_SIGNED_IN, true);
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }

                                });

//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
                    }
                }).addOnFailureListener(e -> {
                    loading(false);
                    MyToast.showShortToast(getApplicationContext(), "Unable to sign In");
                    Timber.e(e);
                });
    }

    private void signIn() {
        loading(true);
        database.collection(Keys.KEY_COLLECTION_USERS)
                .whereEqualTo(Keys.KEY_USERNAME, binding.etUsername.getText().toString())
                .whereEqualTo(Keys.KEY_PASSWORD, binding.etPassword.getText().toString())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        preferenceManager.putString(Keys.KEY_DOCUMENT_REF, documentSnapshot.getReference().getId());
                        preferenceManager.putString(Keys.KEY_USER_ID, documentSnapshot.getString(Keys.KEY_ID));
                        preferenceManager.putString(Keys.KEY_FIRSTNAME, documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                        preferenceManager.putString(Keys.KEY_LASTNAME, documentSnapshot.getString(Keys.KEY_LASTNAME));
                        preferenceManager.putString(Keys.KEY_EMAIL, documentSnapshot.getString(Keys.KEY_EMAIL));
                        preferenceManager.putString(Keys.KEY_PHONE, documentSnapshot.getString(Keys.KEY_PHONE));
                        preferenceManager.putString(Keys.KEY_AVATAR, documentSnapshot.getString(Keys.KEY_AVATAR));
//                        preferenceManager.putBoolean(Keys.KEY_MESSAGE_REQUEST, documentSnapshot.getBoolean(Keys.KEY_MESSAGE_REQUEST));

//                        HashMap<String, Object> settings = new HashMap<>();
//                        settings.put(Keys.KEY_USER_ID, documentSnapshot.getString(Keys.KEY_ID));
//                        settings.put(Keys.KEY_SETTING_MESSAGE_REQUEST, false);
//                        settings.put(Keys.KEY_SETTING_DO_NOT_DISTURB, false);
//                        settings.put(Keys.KEY_CREATED_DATE, Utils.currentTimeMillis());
//                        settings.put(Keys.KEY_MODIFIED_DATE, Utils.currentTimeMillis());
//
//                        database.collection(Keys.KEY_COLLECTION_SETTINGS).add(settings);

                        database.collection(Keys.KEY_COLLECTION_SETTINGS)
                                .whereEqualTo(Keys.KEY_USER_ID, documentSnapshot.getString(Keys.KEY_ID))
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots1 -> {

                                    if (!queryDocumentSnapshots1.getDocuments().isEmpty()) {
                                        DocumentSnapshot documentSnapshot2 = queryDocumentSnapshots1.getDocuments().get(0);
                                        preferenceManager.putBoolean(Keys.KEY_SETTING_DO_NOT_DISTURB, documentSnapshot2.getBoolean(Keys.KEY_SETTING_MESSAGE_REQUEST));
                                        preferenceManager.putBoolean(Keys.KEY_SETTING_MESSAGE_REQUEST, documentSnapshot2.getBoolean(Keys.KEY_SETTING_MESSAGE_REQUEST));

                                        preferenceManager.putBoolean(Keys.KEY_IS_SIGNED_IN, true);
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }

                                });

                    } else {
                        loading(false);
                        MyToast.showShortToast(getApplicationContext(), "Unable to sign In " + queryDocumentSnapshots.getDocuments().size());
                    }

                })
                .addOnFailureListener(e -> {
                    loading(false);
                    MyToast.showShortToast(getApplicationContext(), "Unable to sign In" + e.getMessage());

                });
    }

    private Boolean isValidSignInDetails() {
        if (binding.etUsername.getText().toString().trim().isEmpty()) {
            MyToast.showShortToast(getApplicationContext(), "Enter email");
            return false;
        } else if (binding.etPassword.getText().toString().trim().isEmpty()) {
            MyToast.showShortToast(getApplicationContext(), "Enter password");
            return false;
        }
        return true;
    }

    public void askPermission() {
        String[] PERMISSIONS = {
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.ACCESS_FINE_LOCATION

        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }
    }

    private boolean hasPermissions(Context context, String... PERMISSIONS) {
        if (context != null && PERMISSIONS != null) {
            for (String p : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "INTERNET", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(this, "Please provide the required permission", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "CAMERA", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(this, "Please provide the required permission", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {

            } else {
                //Toast.makeText(this, "Please provide the required permission", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {

            } else {
                //Toast.makeText(this, "Please provide the required permission", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[4] == PackageManager.PERMISSION_GRANTED) {

            } else {
                //Toast.makeText(this, "Please provide the required permission", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[5] == PackageManager.PERMISSION_GRANTED) {

            } else {
                //Toast.makeText(this, "Please provide the required permission", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[6] == PackageManager.PERMISSION_GRANTED) {

            } else {
                //Toast.makeText(this, "Please provide the required permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
}