package com.example.loqui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.databinding.ActivityFacebookLoginBinding;
import com.example.loqui.utils.FirebaseHelper;
import com.example.loqui.utils.Keys;
import com.example.loqui.utils.PreferenceManager;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import timber.log.Timber;


public class FacebookLoginActivity extends AppCompatActivity {

    private ActivityFacebookLoginBinding binding;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;
    private static final String TAG = "FacebookAuthentication";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFacebookLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        database = FirebaseFirestore.getInstance();
        setListeners();
    }

    private void setListeners() {
        binding.btnFacebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                FirebaseHelper.findUser(database, loginResult.getAccessToken().getUserId())
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                preferenceManager.putBoolean(Keys.KEY_IS_SIGNED_IN, true);
//                                preferenceManager.putString(Keys.KEY_USER_ID, documentSnapshot.getId());
//                                preferenceManager.putString(Keys.KEY_FIRSTNAME, documentSnapshot.getString(Keys.KEY_FIRSTNAME));
//                                preferenceManager.putString(Keys.KEY_LASTNAME, documentSnapshot.getString(Keys.KEY_LASTNAME));
//                                preferenceManager.putString(Keys.KEY_AVATAR, documentSnapshot.getString(Keys.KEY_AVATAR));
//                                preferenceManager.putString(Keys.KEY_FCM_TOKEN, documentSnapshot.get(Keys.KEY_FCM_TOKEN));
//                                preferenceManager.putString(Keys.KEY);
                                preferenceManager.putString(Keys.KEY_DOCUMENT_REF, documentSnapshot.getReference().getId());
                                preferenceManager.putString(Keys.KEY_USER_ID, documentSnapshot.getString(Keys.KEY_ID));
                                preferenceManager.putString(Keys.KEY_FIRSTNAME, documentSnapshot.getString(Keys.KEY_FIRSTNAME));
                                preferenceManager.putString(Keys.KEY_LASTNAME, documentSnapshot.getString(Keys.KEY_LASTNAME));
                                preferenceManager.putString(Keys.KEY_EMAIL, documentSnapshot.getString(Keys.KEY_EMAIL));
                                preferenceManager.putString(Keys.KEY_PHONE, documentSnapshot.getString(Keys.KEY_PHONE));
                                preferenceManager.putString(Keys.KEY_AVATAR, documentSnapshot.getString(Keys.KEY_AVATAR));

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

//                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(FacebookLoginActivity.this.getApplicationContext(), SignUpActivity.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Timber.tag(TAG).e(e);
                        });

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Log.d(TAG, "onError");
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    updateUI(user);
//                } else {
//                    updateUI(null);
//                }
            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(@Nullable AccessToken accessToken, @Nullable AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    firebaseAuth.signOut();
                }
            }
        };
    }

//    private void handleFacebookToken(AccessToken accessToken) {
//        Log.d(TAG, "handleFacebookToken " + accessToken.getToken());
//
//        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
//        firebaseAuth.signInWithCredential(credential)
//                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "Sign in with credential: successful");
//                            FirebaseUser user = firebaseAuth.getCurrentUser();
//
//                            Intent intent = new Intent(FacebookLoginActivity.this.getApplicationContext(), SignUpActivity.class);
//                            User registerUser = new User();
//                            registerUser.setId(user.getUid());
//                            registerUser.setName(user.getDisplayName());
//                            registerUser.setEmail(user.getEmail());
//                            registerUser.setImage(user.getPhotoUrl().toString() + "?type=large");
//                            registerUser.setPhone(user.getPhoneNumber());
//
//                            intent.putExtra("user", registerUser);
//                            startActivity(intent);
//
//                            finish();
//                            //updateUI(user);
//                        } else {
//                            Log.d(TAG, "Sign in with credential: failed", task.getException());
//                            MyToast.showShortToast(FacebookLoginActivity.this.getApplicationContext(), "Authentication Failed");
//                            //updateUI(null);
//                        }
//                    }
//                }).addOnFailureListener(e -> {
//                    Log.d(TAG, "Sign in with credential: failed", e);
//                    MyToast.showShortToast(FacebookLoginActivity.this.getApplicationContext(), "Authentication Failed");
//                });
//    }

//    private void updateUI(FirebaseUser user) {
//        if (user != null) {
//
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}